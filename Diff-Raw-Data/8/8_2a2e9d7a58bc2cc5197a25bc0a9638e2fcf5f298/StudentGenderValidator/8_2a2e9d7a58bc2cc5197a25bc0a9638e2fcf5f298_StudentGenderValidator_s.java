 package no.niths.domain.constraints;
 
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorContext;
  
 public class StudentGenderValidator implements ConstraintValidator<StudentGender, Character> {
  
 	@Override
 	public void initialize(StudentGender number) {
 	}
  
 
 	@Override
 	public boolean isValid(Character studentSex, ConstraintValidatorContext context) {
 		
		if (studentSex == null)
			return false;
		if (studentSex == 'M' || studentSex == 'F')
 			return true;
 		
 		return false;
 	}
 
 }
