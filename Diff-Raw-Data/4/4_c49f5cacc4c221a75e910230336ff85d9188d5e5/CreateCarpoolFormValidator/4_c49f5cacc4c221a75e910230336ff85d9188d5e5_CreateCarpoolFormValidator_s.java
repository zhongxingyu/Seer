 package smartpool.web.form;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import smartpool.common.Constants;
 
 public class CreateCarpoolFormValidator implements Validator {
 
     public CreateCarpoolFormValidator() {
     }
 
     @Override
     public boolean supports(Class<?> clazz) {
         return JoinRequestForm.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         CreateCarpoolForm form = (CreateCarpoolForm) target;
 
        if(form.from == null || form.from.equals("")) errors.rejectValue("from", Constants.FIELD_REQUIRED);
        if(form.to == null || form.to.equals("")) errors.rejectValue("to", Constants.FIELD_REQUIRED);
 
         if(form.cabType == null || form.cabType.equals("")) errors.rejectValue("cabType", Constants.FIELD_REQUIRED);
         if(form.pickupPoint  == null || form.pickupPoint.equals("")) errors.rejectValue("pickupPoint", Constants.FIELD_REQUIRED);
         if(form.pickupTime  == null || form.pickupTime.equals("")) errors.rejectValue("pickupTime", Constants.FIELD_REQUIRED);
         else checkForInvalidTime(errors, form.pickupTime, "pickupTime");
         if(form.officeArrivalTime  == null || form.officeArrivalTime.equals("")) errors.rejectValue("officeArrivalTime", Constants.FIELD_REQUIRED);
         else checkForInvalidTime(errors, form.officeArrivalTime, "officeArrivalTime");
         if(form.officeDepartureTime  == null || form.officeDepartureTime.equals("")) errors.rejectValue("officeDepartureTime", Constants.FIELD_REQUIRED);
         else checkForInvalidTime(errors,form.officeDepartureTime,"officeDepartureTime");
 
         try{
             if(form.proposedStartDate == null || form.proposedStartDate.equals("")) errors.rejectValue("proposedStartDate", Constants.FIELD_REQUIRED);
             else if(form.proposedStartDate != null) Constants.DATE_FORMATTER.parseLocalDate(form.proposedStartDate);
         }catch (IllegalArgumentException e){
             errors.rejectValue("proposedStartDate",Constants.FIELD_INVALID);
         }
 
         try{
             if(form.capacity != null) Integer.parseInt(form.capacity);
         }catch (NumberFormatException e){
             errors.rejectValue("capacity",Constants.FIELD_INVALID);
         }
 
     }
 
     private void checkForInvalidTime(Errors errors, String fieldValue, String fieldName) {
         try{
             if(fieldValue != null) Constants.TIME_FORMATTER.parseLocalTime(fieldValue);
         }catch (IllegalArgumentException e){
             errors.rejectValue(fieldName,Constants.FIELD_INVALID);
         }
     }
 }
