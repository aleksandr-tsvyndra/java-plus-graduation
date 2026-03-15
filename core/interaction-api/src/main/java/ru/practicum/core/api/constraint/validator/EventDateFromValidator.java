package ru.practicum.core.api.constraint.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import ru.practicum.core.api.constraint.EventStartDateTime;

import java.time.LocalDateTime;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class EventDateFromValidator implements ConstraintValidator<EventStartDateTime, LocalDateTime> {

    private static final long TWO_HOURS = 2;

    @Override
    public boolean isValid(
            LocalDateTime fieldValue,
            ConstraintValidatorContext constraintContext) {

        if (fieldValue == null) {
            return true;
        }

        LocalDateTime nowPlusTwoHours = LocalDateTime.now().plusHours(TWO_HOURS);

        return fieldValue.isAfter(nowPlusTwoHours);
    }
}
