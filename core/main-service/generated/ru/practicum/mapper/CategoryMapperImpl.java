package ru.practicum.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-05T17:08:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Amazon.com Inc.)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryDto toCategoryDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryDto categoryDto = new CategoryDto();

        return categoryDto;
    }

    @Override
    public Category toCategory(CategoryDto categoryDto) {
        if ( categoryDto == null ) {
            return null;
        }

        Category category = new Category();

        return category;
    }

    @Override
    public Category toCategoryFromNew(NewCategoryDto newCategoryDto) {
        if ( newCategoryDto == null ) {
            return null;
        }

        Category category = new Category();

        return category;
    }
}
