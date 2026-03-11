package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.category.PublicCategoryApi;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicCategoryController implements PublicCategoryApi {
    private final CategoryService categoryService;

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryService.getCategories(PageRequest.of(from / size, size));
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        return categoryService.getCategoryById(catId);
    }
}
