package ru.practicum.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.core.api.internal.event.dto.CategoryDto;
import ru.practicum.core.event.dto.categories.NewCategoryDto;
import ru.practicum.core.event.mapper.CategoryMapper;
import ru.practicum.core.event.model.Category;
import ru.practicum.core.event.repository.CategoryRepository;
import ru.practicum.core.api.exception.DataAlreadyExistException;
import ru.practicum.core.api.exception.NotFoundException;
import ru.practicum.core.api.exception.RelatedDataDeleteException;
import ru.practicum.core.event.repository.EventRepository;
import ru.practicum.core.event.service.api.CategoryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_EXISTS_ERROR_MESSAGE = "Категория с именем=%s уже существует в базе данных";
    private static final String DELETION_ERROR_MESSAGE = "Категория с ID=%d связана с другими сущностями и не может быть удалена";
    private static final String GET_ERROR_MESSAGE = "Категория с ID=%d не найдена в базе данных";

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        checkCategoryExistenceByNameOrThrow(newCategoryDto.getName());

        Category newCategory = categoryMapper.toModel(newCategoryDto);
        Category createdCategory = categoryRepository.save(newCategory);

        log.info("Создана новая категория с ID={}", createdCategory.getId());
        return categoryMapper.toDto(createdCategory);
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        // Проверяем, что категория с указанным ID существует
        Category currentCategory = getCategoryById(categoryId);

        // Проверяем, что новое имя категории не совпадает с текущим
        if (currentCategory.getName().equals(categoryDto.getName())) {
            // Если имена совпадают, возвращаем DTO текущей категории
            return categoryMapper.toDto(currentCategory);
        }

        // Проверяем, что новое имя категории не существует в базе данных
        checkCategoryExistenceByNameOrThrow(categoryDto.getName());

        // Обновляем название категории
        currentCategory.setName(categoryDto.getName());

        // Сохраняем обновлённую категорию
        Category updatedCategory = categoryRepository.save(currentCategory);

        log.info("Название категории с ID={} изменено на: {}", updatedCategory.getId(), updatedCategory.getName());
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        // Если категория не найдена, генерируется исключение NotFoundException
        if (categoryRepository.existsById(categoryId)) {
            // Проверяем, связана ли категория с другими сущностями
            if (eventRepository.existsByCategoryId(categoryId)) {
                final String error = String.format(DELETION_ERROR_MESSAGE, categoryId);
                log.warn(error);
                throw new RelatedDataDeleteException(error);
            }
            // Удаляем категорию
            categoryRepository.deleteById(categoryId);
        }
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        Category currentCategory = getCategoryById(categoryId);
        return categoryMapper.toDto(currentCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format(GET_ERROR_MESSAGE, categoryId)));
    }

    private void checkCategoryExistenceByNameOrThrow(String name) {
        if (categoryRepository.existsByName(name)) {
            final String error = String.format(CATEGORY_EXISTS_ERROR_MESSAGE, name);
            log.warn(error);
            throw new DataAlreadyExistException(error);
        }
    }
}