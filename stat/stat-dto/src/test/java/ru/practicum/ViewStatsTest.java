package ru.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ViewStatsTest {

    private static final long VALID_HITS = 1L;
    private static final String VALID_APP = "test-app";
    private static final String VALID_URI = "/test/uri";

    private ViewStats viewStats;
    private static Validator validator;

    @BeforeAll
    static void init() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        validator = factory;
    }

    @BeforeEach
    void setUp() {
        viewStats = new ViewStats();
        viewStats.setApp(VALID_APP);
        viewStats.setUri(VALID_URI);
        viewStats.setHits(VALID_HITS);
    }

    @Test
    void testValidViewStats() {
        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, empty());
    }

    @Test
    void testAppBlank_validationFails() {
        viewStats.setApp("  ");

        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, hasSize(1));
        ConstraintViolation<ViewStats> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), equalTo("app"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void testAppNull_validationFails() {
        viewStats.setApp(null);

        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, hasSize(1));
        ConstraintViolation<ViewStats> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), equalTo("app"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void testUriBlank_validationFails() {
        viewStats.setUri("  ");

        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, hasSize(1));
        ConstraintViolation<ViewStats> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), equalTo("uri"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void testUriNull_validationFails() {
        viewStats.setUri(null);

        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, hasSize(1));
        ConstraintViolation<ViewStats> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), equalTo("uri"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void testHitsZero_validationFails() {
        viewStats.setHits(0L);

        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, hasSize(1));
        ConstraintViolation<ViewStats> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), equalTo("hits"));
        assertThat(violation.getMessage(), containsString("должно быть больше 0"));
    }

    @Test
    void testHitsNegative_validationFails() {
        viewStats.setHits(-1L);

        Set<ConstraintViolation<ViewStats>> violations = validator.validate(viewStats);

        assertThat(violations, hasSize(1));
        ConstraintViolation<ViewStats> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), equalTo("hits"));
        assertThat(violation.getMessage(), containsString("должно быть больше 0"));
    }
}
