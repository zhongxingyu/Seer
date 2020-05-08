 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.core.util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.transform.Source;
 
 import org.apache.servicemix.common.JbiConstants;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.runtime.impl.MessageExchangeImpl;
 import org.apache.servicemix.jbi.runtime.impl.NormalizedMessageImpl;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.core.ClientChannel;
 import org.apache.servicemix.nmr.core.ExchangeImpl;
 import org.apache.servicemix.nmr.core.MessageImpl;
 import org.apache.servicemix.soap.Context;
 import org.apache.servicemix.soap.SoapHelper;
 import org.apache.servicemix.soap.marshalers.JBIMarshaler;
 import org.apache.servicemix.soap.marshalers.SoapMarshaler;
 import org.apache.servicemix.soap.marshalers.SoapMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
 public class SimpleClient implements InitializingBean {
     private static Logger log = LoggerFactory.getLogger(SimpleClient.class);
     private String dataToSend;
     private String uriToSend;
     private String targetEndpointName;
     public String getTargetEndpointName() {
         return targetEndpointName;
     }
 
     public void setTargetEndpointName(String targetEndpointName) {
         this.targetEndpointName = targetEndpointName;
     }
 
     private Integer delayBeforeSending = 5000;
     private NMR nmr;
 
     public SimpleClient() {
     }
 
     private void checkConstraints() {
         Assert.notNull(dataToSend, "dataToSend property is compulsory");
         Assert.notNull(nmr, "nmr property is compulsory");
 
 
     }
 
     public void start() {
         checkConstraints();
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 try {
                     sendRequestSynchronously();
                 } catch (Exception ex) {
                     throw new RuntimeException(ex);
                 }
             }
         }, delayBeforeSending);
 
     }
 
     public void sendRequestSynchronously() throws Exception {
         SoapMarshaler soapMarshaler = new SoapMarshaler();
         soapMarshaler.setUseDom(true);
         JBIMarshaler marshaler  = new JBIMarshaler();
         SoapMessage soapMessage = soapMarshaler
                 .createReader().read(
                         ServiceMixSupport.convertStringToIS(dataToSend, "UTF8"));
         NormalizedMessage normalizedMessage = new NormalizedMessageImpl(
                 new MessageImpl());
         marshaler.toNMS(normalizedMessage, soapMessage);
         SoapHelper helper = new MockSoapHelper();
         Context ctx = helper.createContext(soapMessage);
         MessageExchange messageExchange = helper.onReceive(ctx);
         ExchangeImpl exchange = (ExchangeImpl) ((MessageExchangeImpl) messageExchange)
                 .getInternalExchange();
         if (uriToSend != null) {
             exchange.getIn().setHeader(JbiConstants.HTTP_DESTINATION_URI,
                     uriToSend);
         }
         //exchange.setSource(ServiceMixHelper.getEndpoint(nmr, endpointProps));
         Map<String, Object> targetProps = new HashMap<String, Object>();
         //TODO please find more suitable solution
         if (targetEndpointName == null) {
             targetEndpointName = "JustDummyEndpointName";
         }
         targetProps.put(Endpoint.ENDPOINT_NAME, targetEndpointName);
         exchange.setTarget(nmr.getEndpointRegistry().lookup(targetProps));
         log.info("!!SimpleClient is sending synchronous request with in message " + dataToSend);
        new ClientChannel(nmr).sendSync(exchange);
         if (exchange.getError() != null) {
             log.error("The invocation wasn't successful", exchange.getError());
         }
         if (exchange.getFault() != null
                 && exchange.getFault().getBody() != null) {
             log.error("The invocation wasn't successful "
                     + exchange.getFault().getBody().toString());
         }
         log.info("!!SimpleClient have received the response: "
                 + new SourceTransformer().toString(exchange.getOut().getBody(
                         Source.class)));
     }
 
     public String getDataToSend() {
         return dataToSend;
     }
 
     public void setDataToSend(String dataToSend) {
         this.dataToSend = dataToSend;
     }
 
     public String getUriToSend() {
         return uriToSend;
     }
 
     public void setUriToSend(String uriToSend) {
         this.uriToSend = uriToSend;
     }
 
 
     public Integer getDelayBeforeSending() {
         return delayBeforeSending;
     }
 
     public void setDelayBeforeSending(Integer delayBeforeSending) {
         this.delayBeforeSending = delayBeforeSending;
     }
 
     public NMR getNmr() {
         return nmr;
     }
 
     public void setNmr(NMR nmr) {
         this.nmr = nmr;
     }
 
     public void afterPropertiesSet() throws Exception {
         start();
     }
 
 
 }
