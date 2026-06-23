package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto create(NewCategoryDto payload) {
        if (categoryRepository.findByName(payload.getName()).isPresent()) {
            throw new ConflictException("Категория уже существует!");
        }
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.createToEntity(payload)));
    }

    @Override
    public void delete(long id) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Категория с таким ID не существует!"));
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDto update(long id, CategoryDto payload) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Категория с таким ID не существует!"));
        categoryRepository.findByName(payload.getName()).ifPresent(category -> {
            if (category.getId() != id) {
                throw new ConflictException("Категория уже существует!");
            }
        });
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.updateToEntity(id, payload)));
    }
}
