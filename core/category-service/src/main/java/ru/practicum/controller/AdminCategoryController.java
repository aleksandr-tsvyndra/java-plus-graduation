package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.category.AdminCategoryApi;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminCategoryController implements AdminCategoryApi {
    private final CategoryService categoryService;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        return categoryService.createCategory(newCategoryDto);
    }

    @Override
    public void deleteCategory(Long catId) {
        categoryService.deleteCategory(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        return categoryService.updateCategory(catId, categoryDto);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryService.getCategories(PageRequest.of(from / size, size));
    }
}
