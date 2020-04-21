package de.challengeme.backend.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

	private static final Logger logger = LogManager.getLogger();
	private static final String HTML_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
	private static final Pattern pattern = Pattern.compile(HTML_PATTERN);

	@Override
	public void initialize(NoHtml constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value != null) {
			boolean result = !pattern.matcher(value).find();
			if (!result) {
				logger.info("Validation failed because string contains HTML: '{}'.", value);
			}
			return result;
		}

		return true;
	}
}