package ru.practicum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

class CreateCreateEndpointHitTest {

    private static Validator validator;
    private CreateEndpointHit createEndpointHit;

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
        createEndpointHit = new CreateEndpointHit();
        createEndpointHit.setApp(validApp);
        createEndpointHit.setUri(validUri);
        createEndpointHit.setIp(validIp);
        createEndpointHit.setTimestamp(validTimestamp);
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, empty());
    }

    @Test
    void whenAppIsBlank_thenHasViolation() {
        createEndpointHit.setApp(" ");

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("app"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenAppIsNull_thenHasViolation() {
        createEndpointHit.setApp(null);

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("app"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenUriIsBlank_thenHasViolation() {
        createEndpointHit.setUri(" ");

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("uri"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenUriIsNull_thenHasViolation() {
        createEndpointHit.setUri(null);

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("uri"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenIpIsBlank_thenHasViolation() {
        createEndpointHit.setIp(" ");

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("ip"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenIpIsNull_thenHasViolation() {
        createEndpointHit.setIp(null);

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("ip"));
        assertThat(violation.getMessage(), containsString("не должно быть пустым"));
    }

    @Test
    void whenTimestampIsNull_thenHasViolation() {
        createEndpointHit.setTimestamp(null);

        Set<ConstraintViolation<CreateEndpointHit>> violations = validator.validate(createEndpointHit);

        assertThat(violations, hasSize(1));
        ConstraintViolation<CreateEndpointHit> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString(), is("timestamp"));
        assertThat(violation.getMessage(), containsString("не должно равняться null"));
    }

}