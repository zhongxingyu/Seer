 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jboss.aerogear.aerodoc.service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.inject.Inject;
 
 import org.jboss.aerogear.aerodoc.model.Lead;
 import org.jboss.aerogear.aerodoc.model.PushConfig;
 import org.jboss.aerogear.aerodoc.rest.PushConfigEndpoint;
 import org.jboss.aerogear.unifiedpush.JavaSender;
 import org.jboss.aerogear.unifiedpush.SenderClient;
 import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;
 
 public class LeadSender {
 
     private static final Logger logger = Logger.getLogger(LeadSender.class.getName());
 
     @Inject
     PushConfigEndpoint pushConfigEndpoint;
 
     // TODO we don't want this
     private int leadVersion = 1;
     private int broadcastVersion = 1;
 
     private JavaSender javaSender;
 
     public LeadSender() {
         //TODO using a dummy value, will be removed with the non-arg constructor in the next release
         javaSender = new SenderClient("http://localhost:8080/ag-push");
     }
 
     public void sendLeads(List<String> users, Lead lead) {
         if (getActivePushConfig() != null) {
             Map categories = new HashMap();
             categories.put("lead", "version=" + leadVersion++); //TODO manage the version properly
             UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
                     .pushApplicationId(getActivePushConfig().getPushApplicationId())
                     .masterSecret(getActivePushConfig().getMasterSecret())
                     .aliases(users)
                     .simplePush(categories)
                     .attribute("id", lead.getId().toString())
                     .attribute("messageType", "pushed_lead")
                     .attribute("name", lead.getName())
                     .attribute("location", lead.getLocation())
                     .attribute("phone", lead.getPhoneNumber()).sound("default")
                     .alert("A new lead has been created").build();

             javaSender.sendTo(unifiedMessage);
         } else {
             logger.severe("not PushConfig configured, can not send message");
         }
     }
 
     public void sendBroadCast(Lead lead) {
         if (getActivePushConfig() != null) {
             UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
                     .pushApplicationId(getActivePushConfig().getPushApplicationId())
                     .masterSecret(getActivePushConfig().getMasterSecret())
                     .simplePush("version=" + broadcastVersion++)
                     .attribute("id", lead.getId().toString())
                     .attribute("messageType", "pushed_lead")
                     .attribute("name", lead.getName())
                     .attribute("location", lead.getLocation())
                     .attribute("phone", lead.getPhoneNumber())
                     .attribute("messageType", "accepted_lead").sound("default")
                     .alert("A new lead has been accepted").build();

             javaSender.broadcast(unifiedMessage);
         } else {
             logger.severe("not PushConfig configured, can not send message");
         }
     }
 
     public JavaSender getJavaSender() {
         return javaSender;
     }
 
     public void setJavaSender(JavaSender javaSender) {
         this.javaSender = javaSender;
     }
 
     private PushConfig getActivePushConfig() {
         PushConfig pushConfig = pushConfigEndpoint.findActiveConfig();
         return pushConfig;
 
     }
 
 }
