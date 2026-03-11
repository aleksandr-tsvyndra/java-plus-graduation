package ru.practicum.api.category;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

@Validated
public interface AdminCategoryApi {

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto);

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable @Positive Long catId);

    @PatchMapping("/admin/categories/{catId}")
    CategoryDto updateCategory(@PathVariable @Positive Long catId,
                               @RequestBody @Valid CategoryDto categoryDto);

    @GetMapping("/admin/categories")
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size);

}
