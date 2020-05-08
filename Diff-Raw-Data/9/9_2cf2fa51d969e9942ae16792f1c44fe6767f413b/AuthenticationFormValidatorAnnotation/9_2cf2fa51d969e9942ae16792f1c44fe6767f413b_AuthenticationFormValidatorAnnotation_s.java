 package aider.org.pmsiadmin.validator;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import javax.validation.Constraint;
 import javax.validation.Payload;
 
 /**
  * Définition des annotations
  * @author delabre
  *
  */
 @Documented
 @Constraint(validatedBy = AuthenticationFormValidator.class)
 @Target(value = {java.lang.annotation.ElementType.TYPE})
 @Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
 public @interface AuthenticationFormValidatorAnnotation {
 	
	String message() default "{aider.org.bio.biomanager.validator.AuthentificationFormValidator.message}";
 	
 	Class<?>[] groups() default {};
 	
 	Class<? extends Payload>[] payload() default {};
 }
