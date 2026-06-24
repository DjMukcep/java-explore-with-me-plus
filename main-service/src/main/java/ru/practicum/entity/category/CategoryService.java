package ru.practicum.entity.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

public interface CategoryService {
    CategoryDto create(NewCategoryDto payload);

    void delete(long id);

    CategoryDto update(long id, CategoryDto payload);
}
