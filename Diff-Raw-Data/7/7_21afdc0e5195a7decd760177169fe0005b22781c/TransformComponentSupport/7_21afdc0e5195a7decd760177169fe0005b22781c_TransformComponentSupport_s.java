 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.components.util;
 
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.NormalizedMessage;
 
 import org.apache.servicemix.MessageExchangeListener;
 
 /**
  * A useful base class for a transform component.
  *
  * @version $Revision$
  */
 public abstract class TransformComponentSupport extends ComponentSupport implements MessageExchangeListener {
     private boolean copyProperties = true;
     private boolean copyAttachments = true;
 
 
     public void onMessageExchange(MessageExchange exchange) throws MessagingException {
         // Skip done exchanges
         if (exchange.getStatus() == ExchangeStatus.DONE) {
             return;
         }
         NormalizedMessage in = getInMessage(exchange);
         
         NormalizedMessage out = exchange.createMessage();
         try {
             if (transform(exchange, in, out)) {
                 if (isInAndOut(exchange)) {
                     exchange.setMessage(out, "out");
                 }
                 else {
                     InOnly outExchange = getExchangeFactory().createInOnlyExchange();
                     outExchange.setInMessage(out);
                    getDeliveryChannel().sendSync(outExchange);
                     exchange.setStatus(ExchangeStatus.DONE);
                 }
             } else {
                 exchange.setStatus(ExchangeStatus.DONE);
             }
             if (exchange.getFault() != null) {
                 exchange.setStatus(ExchangeStatus.ERROR);
             }
             getDeliveryChannel().send(exchange);
         }
         catch (MessagingException e) {
             fail(exchange, e);
         }
     }
 
 
     // Implementation methods
     //-------------------------------------------------------------------------
 
     /**
      * Transforms the given out message
      */
     protected abstract boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException;
 
 
     public boolean isCopyProperties() {
         return copyProperties;
     }
 
 
     public void setCopyProperties(boolean copyProperties) {
         this.copyProperties = copyProperties;
     }
 
 
     public boolean isCopyAttachments() {
         return copyAttachments;
     }
 
 
     public void setCopyAttachments(boolean copyAttachmenets) {
         this.copyAttachments = copyAttachmenets;
     }
 
 
     /**
      * If enabled the properties and attachments are copied to the destination message
      */
     protected void copyPropertiesAndAttachments(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
         if (isCopyProperties()) {
             CopyTransformer.copyProperties(in, out);
         }
         if (isCopyAttachments()) {
             CopyTransformer.copyAttachments(in, out);
         }
     }
 }
