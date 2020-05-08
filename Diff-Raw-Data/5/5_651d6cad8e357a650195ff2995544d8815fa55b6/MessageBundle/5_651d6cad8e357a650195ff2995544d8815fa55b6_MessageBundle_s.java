 package org.motechproject.ghana.mtn.domain;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.mtn.validation.ValidationError;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 
 import java.util.Properties;
 
 @Component
 public class MessageBundle {
     private Properties values;
 
     public static final String ENROLLMENT_SUCCESS = "enrollment.success";
     public static final String ENROLLMENT_FAILURE = "enrollment.failure";
     public static final String ACTIVE_SUBSCRIPTION_PRESENT = "enrollment.active.subscription.present";
 
    public MessageBundle(@Qualifier("ivrProperties") Properties values) {
         this.values = values;
     }
 
     public String get(String key) {
         Object value = values.get(key);
         return value != null ? (String) value : StringUtils.EMPTY;
     }
 
     public String get(ValidationError error) {
         Object value = values.get(error.key());
         return value != null ? (String) value : StringUtils.EMPTY;
     }
 }
