 package no.niths.domain.constraints;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
  
 public class StudentGenderValidator implements ConstraintValidator<StudentGender, Character> {
  
 	@Override
 	public void initialize(StudentGender number) {
 	}
  
 
	/**
	 * Valid values is M(male), F(emale) or null
	 */
 	@Override
 	public boolean isValid(Character studentSex, ConstraintValidatorContext context) {
 		
		if (studentSex == null || studentSex == 'M' || studentSex == 'F')
 			return true;
 		
 		return false;
 	}
 
 }
