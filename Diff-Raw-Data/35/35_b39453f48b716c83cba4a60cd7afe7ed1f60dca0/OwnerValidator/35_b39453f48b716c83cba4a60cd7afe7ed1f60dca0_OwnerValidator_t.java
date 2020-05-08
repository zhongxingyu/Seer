 package eu.comexis.napoleon.shared.validation;
 
 import static eu.comexis.napoleon.client.resources.ValidationMessages.VALIDATION_MESSAGES;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import eu.comexis.napoleon.shared.model.FeeUnit;
 import eu.comexis.napoleon.shared.model.Owner;
 
 public class OwnerValidator extends PartyValidator<Owner>{
 
   
   @Override
   public List<ValidationMessage> validate(Owner owner) {
     List<ValidationMessage> messages = super.validate(owner);
     
     validateFee(owner, messages);
     //continue...
     
     return messages;
   }
 
   private void validateFee(Owner owner, List<ValidationMessage> messages) {
     
     BigDecimal fee = owner.getFee();
     
     if (fee == null){
       return;
     }
     
     FeeUnit feeUnit = owner.getUnit();
     
    /*if (isLessThan(fee, 5)){
      messages.add(new ValidationMessage(VALIDATION_MESSAGES.fieldMustBeGreaterOrEqualsThan("honoraires", "5"), "fee"));
     }
     
     if (FeeUnit.RENT_PERCENTAGE == feeUnit){
       if (isGreaterOrEqualThan(fee, 15)){
         messages.add(new ValidationMessage(VALIDATION_MESSAGES.fieldMustBeLessThan("honoraires", "15"), "fee"));
       }
    }*/
    if (FeeUnit.RENT_PERCENTAGE == feeUnit){
      if (isLessThan(fee, 5) || isGreaterThan(fee, 15)){
        messages.add(new ValidationMessage(VALIDATION_MESSAGES.feePercentageInvalid(), "fee"));
      }
     }
     
     //continue...
     
   }
 
  
 }
