 package org.motechproject.ananya.kilkari.domain;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.ektorp.support.TypeDiscriminator;
 import org.joda.time.DateTime;
 import org.motechproject.model.MotechBaseDataObject;
 
 import java.util.UUID;
 
 @TypeDiscriminator("doc.type === 'Subscription'")
 public class Subscription extends MotechBaseDataObject {
     @JsonProperty
     private String msisdn;
 
     @JsonProperty
     private String operator;
 
     @JsonProperty
     private String subscriptionId;
 
     @JsonProperty
     private DateTime creationDate;
 
     @JsonProperty
     private SubscriptionStatus status;
 
     @JsonProperty
     private SubscriptionPack pack;
 
     public Subscription() {
     }
 
     public Subscription(String msisdn, SubscriptionPack pack) {
         this.pack = pack;
         this.msisdn = msisdn;
         this.creationDate = DateTime.now();
         this.status = SubscriptionStatus.NEW;
         this.subscriptionId = UUID.randomUUID().toString();
     }
 
     public String getMsisdn() {
         return msisdn;
     }
 
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public DateTime getCreationDate() {
         return creationDate;
     }
 
     public SubscriptionStatus getStatus() {
         return status;
     }
 
     public SubscriptionPack getPack() {
         return pack;
     }
 
     public void setStatus(SubscriptionStatus status) {
         this.status = status;
     }
 
     public String getOperator() {
         return operator;
     }
 
     public void setOperator(String operator) {
         this.operator = operator;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Subscription)) return false;
 
         Subscription that = (Subscription) o;
 
         return new EqualsBuilder().append(this.msisdn, that.msisdn)
                 .append(this.pack, that.pack)
                 .append(this.subscriptionId, that.subscriptionId)
                 .append(this.operator, that.operator)
                 .isEquals();
     }
 
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder()
                 .append(this.msisdn)
                 .append(this.subscriptionId)
                 .append(this.pack)
                .hashCode();
     }
 
     @Override
     public String toString() {
         return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                 .append(this.msisdn)
                 .append(this.subscriptionId)
                 .append(this.pack)
                 .append(this.status)
                 .append(this.creationDate)
                 .toString();
     }
 
 
     public void activate(String operator) {
         setStatus(SubscriptionStatus.ACTIVE);
         setOperator(operator);
     }
 
     public void activationFailed(String operator) {
         setStatus(SubscriptionStatus.ACTIVATION_FAILED);
         setOperator(operator);
     }
 
     public void activationRequested() {
         setStatus(SubscriptionStatus.PENDING_ACTIVATION);
     }
 }
