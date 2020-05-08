 package mu.validation.validators.additional;
 
 import javax.validation.ConstraintValidatorContext;
 import mu.validation.domain.Contract;
 import mu.validation.domain.utils.Person;
 import org.springframework.stereotype.Component;
 
 @Component
 public class NotForZenonAdditionalValidator implements AdditionalContractValidator{
 
	public static final String MESSAGE_TEMPLATE = "{validation.constraints.NotForZenonAdditionalValidator.message}";
 
 	@Override
 	public void validate(final Contract contract, final ConstraintValidatorContext constraintValidatorContext) {
 		final Person farmer = contract.getFarmer();
 		if(farmer == null){
 			return;
 		}
 		final String firstName = farmer.getFirstName();
 		if(!"Zenon".equalsIgnoreCase(firstName)){
 			return;
 		}
 
 		constraintValidatorContext
 				.buildConstraintViolationWithTemplate(MESSAGE_TEMPLATE)
 				.addNode("person")
 				.addNode("firstName")
 				.addConstraintViolation();
 	}
 }
