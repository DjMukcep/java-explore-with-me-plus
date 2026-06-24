package ru.practicum.controller.public_api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoriesParamDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.entity.category.CategoryService;

import java.util.List;

/**
 * конечные точки:
 * /categories
 * /categories/{catId}
 */

@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> findAll(@ModelAttribute @Valid CategoriesParamDto params) {
        return categoryService.findAll(params);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto findById(@PathVariable long id) {
        return categoryService.findById(id);
    }
}
