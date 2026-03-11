package ru.practicum.api.category;

import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.category.CategoryDto;

import java.util.List;

@Validated
public interface PublicCategoryApi {

    @GetMapping("/categories")
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size);

    @GetMapping("/categories/{catId}")
    CategoryDto getCategoryById(@PathVariable @Positive Long catId);

}
