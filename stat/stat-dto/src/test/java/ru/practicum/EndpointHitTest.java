package ru.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class EndpointHitTest {

    private static Validator validator;
    private EndpointHit endpointHit;

    private final long validId = 1L;
    private final String validApp = "testApp";
    private final String validUri = "/test/uri";
    private final String validIp = "192.168.0.1";
    private final LocalDateTime validTimestamp = LocalDateTime.now();

    @BeforeAll
    static void init() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        validator = factory;
    }

    @BeforeEach
    void setUp() {
        endpointHit = new EndpointHit();
        endpointHit.setId(validId);
        endpointHit.setApp(validApp);
        endpointHit.setUri(validUri);
        endpointHit.setIp(validIp);
        endpointHit.setTimestamp(validTimestamp);
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, empty());
    }

    @Test
    void whenIdIsNegative_thenHasViolation() {
        endpointHit.setId(-1L);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("id"));
        assertThat(violation.getMessage(), containsString("должно быть больше 0"));
    }

    @Test
    void whenIdIsNull_thenHasViolation() {
        endpointHit.setId(null);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("id"));
        assertThat(violation.getMessage(), containsString("не должно равняться null"));
    }

    @Test
    void whenAppIsBlank_thenHasViolation() {
        endpointHit.setApp(" ");

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("app"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenAppIsNull_thenHasViolation() {
        endpointHit.setApp(null);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("app"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenUriIsBlank_thenHasViolation() {
        endpointHit.setUri(" ");

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("uri"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenUriIsNull_thenHasViolation() {
        endpointHit.setUri(null);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("uri"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenIpIsBlank_thenHasViolation() {
        endpointHit.setIp(" ");

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("ip"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenIpIsNull_thenHasViolation() {
        endpointHit.setIp(null);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("ip"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenTimestampIsNull_thenHasViolation() {
        endpointHit.setTimestamp(null);

        Set<ConstraintViolation<EndpointHit>> violations = validator.validate(endpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<EndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("timestamp"));
        assertThat(violation.getMessage(), containsString("не должно равняться null"));
    }
}