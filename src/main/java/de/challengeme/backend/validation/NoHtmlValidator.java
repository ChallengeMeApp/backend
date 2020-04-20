package de.challengeme.backend.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

	// http://owasp-java-html-sanitizer.googlecode.com/svn/trunk/distrib/javadoc/org/owasp/html/HtmlPolicyBuilder.html
	// builder is not thread safe, so make local
	private static final PolicyFactory DISALLOW_ALL = new HtmlPolicyBuilder().toFactory();

	@Override
	public void initialize(NoHtml constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value != null) {
			return DISALLOW_ALL.sanitize(value).equals(value);
		}

		return true;
	}
}