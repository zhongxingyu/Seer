 package org.jails.validation.constraint.validator;
 
 import org.jails.util.Country;
 import org.jails.validation.constraint.ISOCountryCode;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
 
 public class ISOCountryCodeValidator  implements ConstraintValidator<ISOCountryCode, String> {
 
     public void initialize(ISOCountryCode constraintAnnotation) {
     }
 
     public boolean isValid(final String isoCountryCode, final ConstraintValidatorContext constraintContext) {
        if (isoCountryCode == null)
            return false;
         else
             return Country.isValidCountry(isoCountryCode);
     }
 
 }
 
 
