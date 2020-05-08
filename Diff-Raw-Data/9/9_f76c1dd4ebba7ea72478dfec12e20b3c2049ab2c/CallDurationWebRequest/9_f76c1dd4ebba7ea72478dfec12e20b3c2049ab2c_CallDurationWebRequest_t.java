 package org.motechproject.ananya.kilkari.request;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.motechproject.ananya.kilkari.obd.service.validator.Errors;
 import org.motechproject.ananya.kilkari.subscription.validators.ValidationUtils;
 
 import java.io.Serializable;
 
 public class CallDurationWebRequest implements Serializable {
     private static final long serialVersionUID = 3563680146653893106L;
     @JsonProperty
     private String startTime;
     @JsonProperty
     private String endTime;
 
     public CallDurationWebRequest() {
     }
 
     public CallDurationWebRequest(String startTime, String endTime) {
         this.startTime = startTime;
         this.endTime = endTime;
     }
 
     public String getStartTime() {
         return startTime;
     }
 
     public String getEndTime() {
         return endTime;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof CallDurationWebRequest)) return false;
 
         CallDurationWebRequest that = (CallDurationWebRequest) o;
 
         return new EqualsBuilder()
                 .append(this.startTime, that.startTime)
                 .append(this.endTime, that.endTime)
                 .isEquals();
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder()
                 .append(this.startTime)
                 .append(this.endTime)
                 .hashCode();
     }
 
     public Errors validate() {
         Errors errors = new Errors();
         boolean formatInvalid = false;
         if (!ValidationUtils.assertDateTimeFormat(startTime)) {
             errors.add(String.format("Invalid start time format %s", startTime));
             formatInvalid = true;
         }
         if (!ValidationUtils.assertDateTimeFormat(endTime)) {
             errors.add(String.format("Invalid end time format %s", endTime));
             formatInvalid = true;
         }
        if (!formatInvalid && (!ValidationUtils.assertDateBefore(parseDateTime(startTime), parseDateTime(endTime)) && !ValidationUtils.assertDateEquals(parseDateTime(startTime), parseDateTime(endTime))))
             errors.add(String.format("Start DateTime[%s] should not be greater than End DateTime[%s]", startTime, endTime));
         return errors;
     }
 
     private DateTime parseDateTime(String time) {
         return DateTimeFormat.forPattern("dd-MM-yyyy HH-mm-ss").parseDateTime(time);
     }
 }
