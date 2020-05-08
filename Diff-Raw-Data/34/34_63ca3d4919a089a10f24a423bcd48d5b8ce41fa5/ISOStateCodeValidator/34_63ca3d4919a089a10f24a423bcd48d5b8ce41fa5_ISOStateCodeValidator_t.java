 package org.jails.validation.constraint.validator;
 
 import org.jails.util.State;
import org.jails.util.Strings;
 import org.jails.validation.constraint.ISOStateCode;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 public class ISOStateCodeValidator implements ConstraintValidator<ISOStateCode, String> {
 
     public void initialize(ISOStateCode constraintAnnotation) {
     }
 
     public boolean isValid(final String isoStateCode, final ConstraintValidatorContext constraintContext) {
		if (Strings.isEmpty(isoStateCode))
			return true;
         else
             return State.isValidState(isoStateCode);
     }
 
 }
 
