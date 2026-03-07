package ru.practicum.api.category;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;

@Validated
@RequestMapping("/categories")
public interface PublicCategoryApi {

    @GetMapping
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size);

    @GetMapping("/{catId}")
    CategoryDto getCategoryById(@PathVariable @Positive Long catId);

}
