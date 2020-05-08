 package org.motechproject.ananya.kilkari.subscription.domain;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonProperty;
 import org.ektorp.support.TypeDiscriminator;
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import org.motechproject.common.domain.PhoneNumber;
 import org.motechproject.model.MotechBaseDataObject;
 
 import java.util.UUID;
 
 @TypeDiscriminator("doc.type === 'Subscription'")
 public class Subscription extends MotechBaseDataObject {
     @JsonProperty
     private String msisdn;
 
     @JsonProperty
     private Operator operator;
 
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
 
     public Subscription(String msisdn, SubscriptionPack pack, DateTime createdAt) {
         this.pack = pack;
         this.msisdn = PhoneNumber.formatPhoneNumberTo10Digits(msisdn).toString();
         this.creationDate = createdAt;
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
 
     public Operator getOperator() {
         return operator;
     }
 
     public void setOperator(Operator operator) {
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
 
     @JsonIgnore
     public boolean isInProgress() {
         return status != SubscriptionStatus.COMPLETED &&
                 status != SubscriptionStatus.DEACTIVATED &&
                 status != SubscriptionStatus.PENDING_DEACTIVATION &&
                 status != SubscriptionStatus.PENDING_COMPLETION;
     }
 
     public void activateOnRenewal() {
         setStatus(SubscriptionStatus.ACTIVE);
     }
 
     public void suspendOnRenewal() {
         setStatus(SubscriptionStatus.SUSPENDED);
     }
 
     public void activate(String operator) {
         setStatus(SubscriptionStatus.ACTIVE);
         setOperator(Operator.getFor(operator));
     }
 
     public void activationFailed(String operator) {
         setStatus(SubscriptionStatus.ACTIVATION_FAILED);
         setOperator(Operator.getFor(operator));
     }
 
     public void activationRequested() {
         setStatus(SubscriptionStatus.PENDING_ACTIVATION);
     }
 
     public void deactivationRequested() {
         setStatus(SubscriptionStatus.PENDING_DEACTIVATION);
     }
 
     public void requestDeactivation() {
         setStatus(SubscriptionStatus.DEACTIVATION_REQUESTED);
     }
 
     public void deactivate() {
         if (SubscriptionStatus.PENDING_COMPLETION.equals(status))
             setStatus(SubscriptionStatus.COMPLETED);
         else
             setStatus(SubscriptionStatus.DEACTIVATED);
     }
 
     public void complete() {
         setStatus(SubscriptionStatus.PENDING_COMPLETION);
     }
 
     public DateTime endDate() {
         return getCreationDate().plusWeeks(getPack().getTotalWeeks());
     }
 
     public boolean hasPackBeenCompleted() {
         int currentWeek = getCurrentWeek();
         return currentWeek >= pack.getTotalWeeks() + pack.getStartWeek();
     }
 
     public int getCurrentWeek() {
         int weeksDifference = getWeeksElapsedAfterCreationDate();
         return weeksDifference + getPack().getStartWeek() + 1;
     }
 
     public int getWeeksElapsedAfterCreationDate() {
         return Weeks.weeksBetween(getCreationDate(), DateTime.now()).getWeeks();
     }
 
 }
