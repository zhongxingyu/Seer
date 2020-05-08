 package com.mpower.controller.validator;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.Errors;
 import org.springframework.validation.ValidationUtils;
 import org.springframework.validation.Validator;
 
 import com.mpower.domain.PaymentSource;
 import com.mpower.service.SiteService;
 import com.mpower.type.PageType;
 import com.mpower.util.CalendarUtils;
 
 public class PaymentSourceValidator implements Validator {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @SuppressWarnings("unused")
     private SiteService siteService;
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     @SuppressWarnings("unused")
     private PageType pageType;
 
     public void setPageType(PageType pageType) {
         this.pageType = pageType;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public boolean supports(Class clazz) {
         return PaymentSource.class.equals(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         logger.debug("in PaymentSourceValidator");
         validatePaymentSource(target, errors);
     }
 
     public static void validatePaymentSource(Object target, Errors errors) {
         String inPath = errors.getNestedPath();
        if (!(target instanceof PaymentSource)) {
             errors.setNestedPath("paymentSource");
         }
         PaymentSource source = (PaymentSource) target;
         if ("ACH".equals(source.getType())) {
             ValidationUtils.rejectIfEmptyOrWhitespace(errors, "achAccountNumber", "invalidAchAccountNumber");
             ValidationUtils.rejectIfEmptyOrWhitespace(errors, "achRoutingNumber", "invalidAchRoutingNumber");
         } else if ("Credit Card".equals(source.getType())) {
             ValidationUtils.rejectIfEmptyOrWhitespace(errors, "creditCardNumber", "invalidCreditCardNumber");
             ValidationUtils.rejectIfEmptyOrWhitespace(errors, "creditCardExpiration", "invalidCreditCardExpiration");
             if (!errors.hasErrors()) {
                 Date expirationDate = source.getCreditCardExpiration();
                 Calendar today = CalendarUtils.getToday(false);
                 if (expirationDate == null || today.getTime().after(expirationDate)) {
                     errors.rejectValue("creditCardExpiration", "invalidCreditCardExpiration");
                 }
             }
         }
         errors.setNestedPath(inPath);
     }
 }
