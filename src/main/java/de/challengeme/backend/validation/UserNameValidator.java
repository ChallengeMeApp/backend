package de.challengeme.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserNameValidator implements ConstraintValidator<UserName, String> {

	@Override
	public void initialize(UserName constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value != null) {
			int len = value.length();
			for (int i = 0; i < len; i++) {
				if (!Character.isLetterOrDigit(value.charAt(i))) {
					return false;
				}
			}
		}
		return true;
	}

}