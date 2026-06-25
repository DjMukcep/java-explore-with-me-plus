package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(final ConstraintViolationException e) {
        log.warn("Ошибка валидации : {}",  e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("Сущность не найдена: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Not Found!")
                .status(HttpStatus.NOT_FOUND.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Ошибка при валидации полей: {}", errors);

        return ApiError.builder()
                .errors(errors)
                .message("Validation failed for some fields")
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.warn("Нарушение целостности данных: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("Integrity constraint has been violated.")
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMetException(final ConditionsNotMetException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Logical conditions hasn't met.")
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(final ForbiddenException e) {
        log.warn("Нет прав доступа: {}", e.getMessage());

        return ApiError.builder()
                .message(e.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .status(HttpStatus.FORBIDDEN.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final Exception e) {
        log.error("Внутренняя ошибка main-service: {} ", e.getMessage(), e);

        return ApiError.builder()
                .message("Internal Server Error")
                .reason("Error occurred on the server side.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNumberFormatException(final NumberFormatException e) {
        log.warn("Ошибка преобразования числа: {}", e.getMessage());

        return ApiError.builder()
                .errors(null)
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(final org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("Некорректное тело запроса: {}", e.getMessage());

        return ApiError.builder()
                .errors(null)
                .message("Malformed JSON request")
                .reason("Incorrectly made request.")
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }
}
