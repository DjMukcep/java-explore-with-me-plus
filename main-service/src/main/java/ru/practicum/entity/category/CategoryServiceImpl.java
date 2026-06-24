package ru.practicum.entity.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto payload) {
        if (categoryRepository.findByName(payload.getName()).isPresent()) {
            throw new ConflictException("Категория уже существует!");
        }
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.createToEntity(payload)));
    }

    @Override
    @Transactional
    public void delete(long id) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Категория с таким ID не существует!"));
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
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

