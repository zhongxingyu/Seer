 package mu.validation.validators.additional;
 
 import javax.validation.ConstraintValidatorContext;
 import mu.validation.domain.Contract;
 import mu.validation.utils.DateUtils;
 import org.springframework.stereotype.Component;
 
 @Component
 public class NotSameDayStartedAndApprovedAdditionalValidator implements AdditionalContractValidator{
 
	public static final String MESSAGE_TEMPLATE = "{validation.constraints.NotSameDayStartedAndApprovedAdditionalValidator.message}";
 
 	@Override
 	public void validate(final Contract contract, final ConstraintValidatorContext constraintValidatorContext) {
 		if(!DateUtils.areDatesEqual(contract.getStartDate(), contract.getApproveDate())){
 			return;
 		}
 
 		constraintValidatorContext.buildConstraintViolationWithTemplate(MESSAGE_TEMPLATE).addNode("startDate").addConstraintViolation();
 	}
 }
