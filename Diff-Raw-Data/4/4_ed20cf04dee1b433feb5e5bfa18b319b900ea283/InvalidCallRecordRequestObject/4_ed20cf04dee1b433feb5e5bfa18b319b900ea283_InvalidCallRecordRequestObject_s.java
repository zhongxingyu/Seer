 package org.motechproject.ananya.kilkari.obd.contract;
 
 import org.codehaus.jackson.annotate.JsonProperty;
 
public class InvalidCallRecordRequestObject {
     @JsonProperty
     private String msisdn;
     @JsonProperty
     private String subscriptionId;
     @JsonProperty
     private String operator;
     @JsonProperty
     private String campaignId;
     @JsonProperty
     private String description;
 
     public String getMsisdn() {
         return msisdn;
     }
 
     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public String getOperator() {
         return operator;
     }
 
     public String getCampaignId() {
         return campaignId;
     }
 
     public String getDescription() {
         return description;
     }
 }
