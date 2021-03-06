package de.questophant.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UserNameValidator implements ConstraintValidator<UserName, String> {

	public static boolean override = false;

	@Override
	public void initialize(UserName constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value != null && !override) {
			int len = value.length();
			for (int i = 0; i < len; i++) {
				char c = value.charAt(i);
				if (!Character.isLetterOrDigit(c)) {
					if (c != ' ' || i == 0 || i == value.length() - 1) {
						return false;
					}
				}
			}
		}
		return true;
	}

}