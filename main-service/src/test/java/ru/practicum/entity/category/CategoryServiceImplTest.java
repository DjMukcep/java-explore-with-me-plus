package ru.practicum.entity.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void create_shouldSaveCategory() {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Test Event")
                .build();

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.create(payload);

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_shouldThrowException_whenCategoryWithNameExists() {
        NewCategoryDto payload = NewCategoryDto.builder()
                .name("Test Event")
                .build();

        Category category = Category.builder()
                .id(1)
                .name("Test Event")
                .build();

        when(categoryRepository.findByName(any(String.class)))
                .thenReturn(Optional.ofNullable(category));

        assertThatThrownBy(() -> categoryService.create(payload))
                .isInstanceOf(ConflictException.class)
                        .hasMessage("Категория уже существует!");

        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void delete_shouldDeleteCategory() {
        Category category = Category.builder()
                .id(1)
                .name("Event name")
                .build();
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.ofNullable(category));

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenCategoryIsNotFound() {
        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория с таким ID не существует!");
    }

    @Test
    void update_shouldSaveCategory() {
        CategoryDto payload = CategoryDto.builder()
                .name("Test Event")
                .build();

        Category category = Category.builder()
                .id(1)
                .name("Event name")
                .build();

        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(category));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.update(1, payload);

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_shouldThrowException_whenCategoryNotFoundById() {
        CategoryDto payload = CategoryDto.builder()
                .name("Test Event")
                .build();

        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.empty());


        assertThatThrownBy(() -> categoryService.update(1, payload))
                .isInstanceOf(NotFoundException.class)
                        .hasMessage("Категория с таким ID не существует!");

        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void update_shouldThrowException_whenCategoryWithNameExists() {
        CategoryDto payload = CategoryDto.builder()
                .name("Event name 2")
                .build();

        Category category = Category.builder()
                .id(1)
                .name("Event name 1")
                .build();

        when(categoryRepository.findById(any(Long.class)))
                .thenReturn(Optional.ofNullable(category));

        when(categoryRepository.findByName(any(String.class)))
                .thenReturn(Optional.ofNullable(category));


        assertThatThrownBy(() -> categoryService.update(2, payload))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Категория уже существует!");

        verifyNoMoreInteractions(categoryRepository);
    }
}
