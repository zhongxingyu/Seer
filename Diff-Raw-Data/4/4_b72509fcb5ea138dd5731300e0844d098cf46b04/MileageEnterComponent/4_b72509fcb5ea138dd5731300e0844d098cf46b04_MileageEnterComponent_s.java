 package com.motorpast.components;
 
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SessionState;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.slf4j.Logger;
 
 import com.motorpast.additional.MotorpastException;
 import com.motorpast.dataobjects.UserSessionObj;
 import com.motorpast.pages.ConfirmationPage;
 import com.motorpast.services.business.MotorpastBusinessException;
 import com.motorpast.services.business.MotorpastBusinessException.BusinessErrorCode;
 import com.motorpast.services.business.ValidationService;
 import com.motorpast.services.security.MotorpastSecurityException;
 import com.motorpast.services.security.MotorpastSecurityException.SecurityErrorCode;
 import com.motorpast.services.security.SecurityService;
 
 /**
  * text1 = fake, text2 = fake;
  * text3 = carid, text4 = mileage
  * text5 = unique token, text6 = timestamp (both are parameters)
  *
  */
 public class MileageEnterComponent
 {
     @SessionState
     private UserSessionObj sessionObj;
 
     @Inject
     private Logger logger;
 
     @Inject
     private Messages messages;
 
     @Inject
     private ValidationService validationService;
 
     @Inject
     private SecurityService securityService;
 
     @InjectPage
     private ConfirmationPage confirmationPage;
 
     @InjectComponent
     private MotorForm mileEnterForm;
 
     @Parameter(required = true, name = "token")
     @Property
     private String text5;
 
     @Parameter(required = true, name = "date")
     @Property
     private long text6;
 
     @Property
     private String text1, text2; // both fields are fake
 
     @Property
     private String text3, text4; // carid and mileage
 
     private boolean sessionObjExists;
     private String carId, mileage;
 
 
     void onPrepareForRenderFromMileEnterForm() {
         text3 = messages.get("txt.input.vin.placeholder");
         text4 = messages.get("txt.input.mileage.placeholder");
     }
 
     void onPrepareForSubmitFromMileEnterForm() throws MotorpastBusinessException {
         if(!sessionObjExists) {
             throw new MotorpastBusinessException(BusinessErrorCode.session_timeout);
         }
     }
 
     void onValidateFormFromMileEnterForm() throws MotorpastSecurityException {
         logger.debug("received values from mileEnterForm:  text1=" + text1 + ", text2=" + text2 + ", text3=" + text3 + 
                 ", text4=" + text4 + ", text5=" + text5 + ", text6=" + text6 + " ...end");
 
         // this is an attempt to prevent CSRF
         if(!text5.equals(sessionObj.getUniqueToken())) {
             throw new MotorpastSecurityException(SecurityErrorCode.error_security);
         }
 
         if(text1 != null && text2 != null) {
             logger.info("hidden fake-textfield has been filled with value=" + text1 + " and " + text2);
             throw new MotorpastSecurityException(SecurityErrorCode.error_security);
         }
 
         if(messages.get("txt.input.vin.placeholder").equals(text3)) {
             text3 = null;
         }
         if(messages.get("txt.input.mileage.placeholder").equals(text4)) {
             text4 = null;
         }
         carId = text3;
         mileage = text4;
 
         //carid is required in both cases - search or new entry in DB
         if((carId == null || carId.isEmpty()) && (mileage == null || mileage.isEmpty())) {
             mileEnterForm.recordError(messages.get("error.values.both-required"));
             return;
         } else if(carId == null || carId.isEmpty()) {
             mileEnterForm.recordError(messages.get("error.carId.required"));
             return;
         } else if(mileage == null || mileage.isEmpty()) {
             mileEnterForm.recordError(messages.get("error.mileage.required"));
             return;
         }
 
         if(!validationService.validateCarId(carId)) {
             mileEnterForm.recordError(messages.get("error.carId.regex-invalid"));
             return;
         }
 
         // check date for spambot detection
         if(text6 == 0                                                       // field is missing -> spam
             || !validationService.validateNumeric(String.valueOf(text6))    // manipulation
             || text6 != sessionObj.getTimestamp()                           // manipulation
             || text6 >= securityService.generateCheckTimestamp()            // too fast -> spam
         ) {
             throw new MotorpastSecurityException(SecurityErrorCode.error_security);
         }
     }
 
     Object onSuccessFromMileEnterForm() throws MotorpastException {
         confirmationPage.setPageParameter(null, carId, mileage);
         return confirmationPage;
     }
 }
