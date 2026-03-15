package ru.practicum.core.api.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.practicum.core.api.constraint.validator.EventDateFromValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {EventDateFromValidator.class})
public @interface EventStartDateTime {

    String message() default "Дата события должна быть не ранее чем через два часа от текущего времени";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
