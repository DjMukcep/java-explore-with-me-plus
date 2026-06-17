package ru.practicum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class ApiErrorTest {

    private Validator validator;
    private final String testMessage = "Test error message";
    private final String testReason = "Test reason";
    private final HttpStatus testHttpStatus = HttpStatus.BAD_REQUEST;
    private final LocalDateTime testTimestamp = LocalDateTime.now();
    private final String testErrors = "Validation errors";
    private ApiError apiError;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        validator = factory;

        apiError = new ApiError(testMessage, testReason, testHttpStatus, testTimestamp, testErrors);
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations, empty());
    }

    @Test
    void whenMessageIsNull_thenHasViolation() {
        apiError.setMessage(null);
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("message"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenMessageIsEmpty_thenHasViolation() {
        apiError.setMessage(" ");
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("message"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenReasonIsNull_thenHasViolation() {
        apiError.setReason(null);
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("reason"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenReasonIsEmpty_thenHasViolation() {
        apiError.setReason(" ");
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("reason"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenHttpStatusIsNull_thenHasViolation() {
        apiError.setHttpStatus(null);
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("httpStatus"));
        assertThat(violation.getMessage(), containsString("не должно равняться null"));
    }

    @Test
    void whenTimestampIsNull_ThenHasViolation() {
        apiError.setTimestamp(null);
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("timestamp"));
        assertThat(violation.getMessage(), containsString("не должно равняться null"));
    }

    @Test
    void whenErrorsIsNull_thenHasViolation() {
        apiError.setErrors(null);
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("errors"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenErrorsIsEmpty_thenHasViolation() {
        apiError.setErrors(" ");
        Set<ConstraintViolation<ApiError>> violations = validator.validate(apiError);

        assertThat(violations.size(), is(1));
        ConstraintViolation<ApiError> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("errors"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }
}
