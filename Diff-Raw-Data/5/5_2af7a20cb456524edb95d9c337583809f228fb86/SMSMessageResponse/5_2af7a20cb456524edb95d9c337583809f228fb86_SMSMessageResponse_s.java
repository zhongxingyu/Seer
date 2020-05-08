 package com.ms.beans.nexmo;
 
import com.sun.deploy.xml.XMLAttribute;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

 /**
  * Created with IntelliJ IDEA.
  * User: Ovi
  * Date: 6/13/13
  * Time: 8:21 PM
  * To change this template use File | Settings | File Templates.
  */
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class SMSMessageResponse {
 
 
 //    @JsonProperty(value = "message")
 //    private SMSMessageEntityResponse smsMessageEntityResponse;
 
     @JsonProperty(value = "message-id")
     private String messageId;
     @JsonProperty
     private String to;
     @JsonProperty(value = "client-ref")
     private String clientRef;
     @JsonProperty(value = "remaining-balance")
     private Float remainingBalance;
     @JsonProperty(value = "message-price")
     private Float messagePrice;
     @JsonProperty
     private String network;
 
     @JsonProperty
     private Integer status;
     @JsonProperty(value = "error-text")
     private String errorText;
 
     public Integer getStatus() {
         return status;
     }
 
     public void setStatus(Integer status) {
         this.status = status;
     }
 
     public String getErrorText() {
         return errorText;
     }
 
     public void setErrorText(String errorText) {
         this.errorText = errorText;
     }
 
     public String getMessageId() {
         return messageId;
     }
 
     public void setMessageId(String messageId) {
         this.messageId = messageId;
     }
 
     public String getTo() {
         return to;
     }
 
     public void setTo(String to) {
         this.to = to;
     }
 
     public String getClientRef() {
         return clientRef;
     }
 
     public void setClientRef(String clientRef) {
         this.clientRef = clientRef;
     }
 
     public Float getRemainingBalance() {
         return remainingBalance;
     }
 
     public void setRemainingBalance(Float remainingBalance) {
         this.remainingBalance = remainingBalance;
     }
 
     public Float getMessagePrice() {
         return messagePrice;
     }
 
     public void setMessagePrice(Float messagePrice) {
         this.messagePrice = messagePrice;
     }
 
     public String getNetwork() {
         return network;
     }
 
     public void setNetwork(String network) {
         this.network = network;
     }
 }
