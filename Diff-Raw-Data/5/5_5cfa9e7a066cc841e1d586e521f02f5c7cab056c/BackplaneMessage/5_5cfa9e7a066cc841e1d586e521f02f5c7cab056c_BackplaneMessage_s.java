 package com.janrain.backplaneclient;
 
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Tom Raney
  */
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
 public class BackplaneMessage {
 
     private String bus;
     private String channel;
     private Object payload;
     private String source;
     private boolean sticky;
     private String type;
     private String messageURL;
 
     public BackplaneMessage() {};
 
     public BackplaneMessage(String bus, String channel, String type, Object payload, boolean sticky) {
         this.bus = bus;
         this.channel = channel;
         this.payload = payload;
         this.sticky = sticky;
         this.type = type;
     }
 
     public String getBus() {
         return bus;
     }
 
     public String getChannel() {
         return channel;
     }
 
     public Object getPayload() {
         return payload;
     }
 
     public String getSource() {
         return source;
     }
 
     public boolean isSticky() {
         return sticky;
     }
 
     public String getType() {
         return type;
     }
 
     public String getMessageURL() {
         return messageURL;
     }
 
     public void setBus(String bus) {
         this.bus = bus;
     }
 
     public void setChannel(String channel) {
         this.channel = channel;
     }
 
     public void setPayload(Object payload) {
         this.payload = payload;
     }
 
 
     /**
      * This method is provided for the downstream message from the Backplane server.  Do not set the source field
      * for upstream messages.
      *
      * @param source
      */
     public void setSource(String source) {
         this.source = source;
     }
 
     public void setSticky(boolean sticky) {
         this.sticky = sticky;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     /**
      * This method is provided for the downstream message from the Backplane server.  Do not set the messageURL
      * for upstream messages.
      *
      * @param messageURL
      */
     public void setMessageURL(String messageURL) {
         this.messageURL = messageURL;
     }
 }
 
 
