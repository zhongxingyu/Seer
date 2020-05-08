 package org.motechproject.ghana.national.web.service;
 
 import org.motechproject.ghana.national.messagegateway.domain.Payload;
 import org.motechproject.ghana.national.messagegateway.domain.SMSPayload;
 import org.motechproject.ghana.national.messagegateway.domain.Store;
 import org.motechproject.ghana.national.messagegateway.domain.VoicePayload;
 import org.motechproject.ghana.national.web.domain.AggregatedMessage;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.integration.Message;
 import org.springframework.integration.store.MessageGroup;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 public class GNAggregatorService {
     @Autowired
     Store store;
 
     public List<AggregatedMessage> allMessages() {
         List<AggregatedMessage> payloadList = new ArrayList<AggregatedMessage>();
         for (MessageGroup group : store) {
             for (Message message : group.getMessages()) {
                 Object payload = message.getPayload();
                if (message instanceof SMSPayload)
                     payloadList.add(new AggregatedMessage((SMSPayload) payload));
                else if (message instanceof VoicePayload)
                     payloadList.add(new AggregatedMessage((VoicePayload) payload));
             }
         }
         return payloadList;
     }
 }
