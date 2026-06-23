package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.entity.category.CategoryService;

/**
 * конечные точки:
 * /admin/categories
 * /admin/categories/{catId}
 */

@RestController
@RequestMapping(("/admin/categories"))
@RequiredArgsConstructor
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto create(@Valid @RequestBody NewCategoryDto payload) {
        return categoryService.create(payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        categoryService.delete(id);
    }

    @PatchMapping("/{id}")
    public CategoryDto update(@PathVariable long id, @Valid @RequestBody CategoryDto payload) {
        return categoryService.update(id, payload);
    }
}
