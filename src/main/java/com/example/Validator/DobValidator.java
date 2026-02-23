package com.example.Validator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDate> {
    private int minAge;
    private int maxAge;

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return true;
        }
        long years = ChronoUnit.YEARS.between(dob, LocalDate.now());
        return years >= minAge && years <= maxAge;
    }

    @Override
    public void initialize(DobConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        minAge = constraintAnnotation.min();
        maxAge = constraintAnnotation.max();
    }
}
