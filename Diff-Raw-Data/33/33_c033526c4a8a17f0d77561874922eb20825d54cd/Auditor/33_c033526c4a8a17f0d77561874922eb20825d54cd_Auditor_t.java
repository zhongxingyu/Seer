/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.apache.servicemix.web;
 
 import java.net.URI;
 import java.text.DateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.jbi.audit.AuditorException;
 import org.apache.servicemix.jbi.audit.AuditorMBean;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.messaging.MessageExchangeSupport;
 
 public class Auditor {
 
     private AuditorMBean mbean;
     private int page;
     private String exchangeId;
 
     public Auditor(AuditorMBean mbean) {
         this.mbean = mbean;
     }
     
     public int getPage() {
         return page;
     }
 
     public void setPage(int page) {
         this.page = page;
     }
     
     public String getExchangeId() {
         return exchangeId;
     }
 
     public void setExchangeId(String exchangeId) {
         this.exchangeId = exchangeId;
     }
     
     public int getCount() throws AuditorException {
         int count = mbean.getExchangeCount();
         return count;
     }
 
     public List<ExchangeInfo> getExchanges() throws AuditorException {
         MessageExchange[] exchanges = mbean.getExchanges(page * 10, Math.min((page + 1) * 10, getCount()));
         ExchangeInfo[] infos = prepare(exchanges);
         return Arrays.asList(infos);
     }
     
     public ExchangeInfo getSelectedExchange() throws AuditorException {
         if (exchangeId == null) {
             return null;
         }
         MessageExchange exchange = mbean.getExchange(exchangeId);
         if (exchange != null) {
             return new ExchangeInfo(exchange);
         } else {
             return null;
         }
     }
 
     private ExchangeInfo[] prepare(MessageExchange[] exchanges) {
         ExchangeInfo[] infos = new ExchangeInfo[exchanges.length];
         for (int i = 0; i < infos.length; i++) {
             infos[i] = new ExchangeInfo(exchanges[i]);
         }
         return infos;
     }
     
     public static class ExchangeInfo {
         private final MessageExchange exchange;
         
         public ExchangeInfo(MessageExchange exchange) {
             this.exchange = exchange;
         }
         
         /**
          * @return Returns the dateStamp.
          */
         public String getDate() {
             Object c = exchange.getProperty(JbiConstants.DATESTAMP_PROPERTY_NAME);
             if (c instanceof Calendar) {
                 return DateFormat.getDateTimeInstance().format(((Calendar) c).getTime());
             } else if (c instanceof Date) {
                 return DateFormat.getDateTimeInstance().format((Date) c);
             } else if (c != null) {
                 return c.toString();
             } else {
                 return null;
             }
         }
         /**
          * @return Returns the status.
          */
         public String getStatus() {
             return exchange.getStatus().toString();
         }
         /**
          * @return Returns the id.
          */
         public String getId() {
             return exchange.getExchangeId();
         }
         /**
          * @return Returns the mep.
          */
         public String getMep() {
             URI uri = exchange.getPattern();
             if (MessageExchangeSupport.IN_ONLY.equals(uri)) {
                 return "In Only";
             } else if (MessageExchangeSupport.IN_OPTIONAL_OUT.equals(uri)) {
                 return "In Opt Out";
             } else if (MessageExchangeSupport.IN_OUT.equals(uri)) {
                 return "In Out";
             } else if (MessageExchangeSupport.ROBUST_IN_ONLY.equals(uri)) {
                 return "Robust In Only";
             } else {
                 return uri.toString();
             }
         }
         
         public String getProperties() {
             StringBuilder sb = new StringBuilder();
             for (String name : (Set<String>) exchange.getPropertyNames()) {
                 if (sb.length() > 0) {
                     sb.append("\n");
                 }
                 sb.append(name).append(" = ").append(exchange.getProperty(name));
             }
             return sb.toString();
         }
         
         public String getErrorMessage() {
             return exchange.getError() != null ? exchange.getError().getMessage() : null;
         }
         
         public MessageInfo getIn() {
             return getMessage("in");
         }
         
         public MessageInfo getOut() {
             return getMessage("out");
         }
         
         public MessageInfo getFault() {
             return getMessage("fault");
         }
         
         private MessageInfo getMessage(String name) {
             NormalizedMessage msg = exchange.getMessage(name);
             return msg != null ? new MessageInfo(msg) : null;
         }
         
         public String getEndpoint() {
             return exchange.getEndpoint().getServiceName() + ":" + exchange.getEndpoint().getEndpointName();
         }
     }
 
     public static class MessageInfo {
         private final NormalizedMessage message;
         public MessageInfo(NormalizedMessage message) {
             this.message = message;
         }
        @SuppressWarnings("unchecked")
         public String getProperties() {
             StringBuilder sb = new StringBuilder();
             for (String name : (Set<String>) message.getPropertyNames()) {
                 if (sb.length() > 0) {
                     sb.append("\n");
                 }
                 sb.append(name).append(" = ").append(message.getProperty(name));
             }
             return sb.toString();
         }
         public String getContent() {
             try {
                 String str = new SourceTransformer().contentToString(message);
                 if (str != null) {
                     return str.replace("<", "&lt;");
                 }
             } catch (Exception e) {
             }
             return null;
         }
     }
 }
