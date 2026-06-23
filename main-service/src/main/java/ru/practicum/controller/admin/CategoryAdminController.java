package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryDto payload) {
        log.info("Handling category create: {}", payload);
        CategoryDto response = categoryService.create(payload);
        log.info("Created category: {}", response);
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("Handling category delete with ID = {}", id);
        categoryService.delete(id);
        log.info("Deleted category with ID = {}", id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@PathVariable long id, @Valid @RequestBody CategoryDto payload) {
        log.info("Handling category update: ID = {}, payload = {}", id, payload);
        CategoryDto response = categoryService.update(id, payload);
        log.info("Updated category with ID = {}, payload = {}", id, payload);
        return response;
    }
}
