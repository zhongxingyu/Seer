 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License.
  */
 
 package org.apache.axis2.transport.testkit.axis2.client;
 
 import javax.mail.internet.ContentType;
 import javax.xml.namespace.QName;
 
 import junit.framework.Assert;
 
 import org.apache.axiom.attachments.Attachments;
 import org.apache.axis2.Constants;
 import org.apache.axis2.client.OperationClient;
 import org.apache.axis2.client.Options;
 import org.apache.axis2.client.ServiceClient;
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.transport.TransportSender;
 import org.apache.axis2.transport.base.ManagementSupport;
 import org.apache.axis2.transport.testkit.MessageExchangeValidator;
 import org.apache.axis2.transport.testkit.channel.Channel;
 import org.apache.axis2.transport.testkit.client.ClientOptions;
 import org.apache.axis2.transport.testkit.client.TestClient;
 import org.apache.axis2.transport.testkit.message.AxisMessage;
 import org.apache.axis2.transport.testkit.name.Name;
 import org.apache.axis2.transport.testkit.tests.Setup;
 import org.apache.axis2.transport.testkit.tests.TearDown;
 import org.apache.axis2.transport.testkit.tests.Transient;
 import org.apache.axis2.transport.testkit.util.ContentTypeUtil;
 
 @Name("axis")
 public class AxisTestClient implements TestClient, MessageExchangeValidator {
     private @Transient AxisTestClientConfigurator[] configurators;
     private @Transient TransportSender sender;
     protected @Transient ServiceClient serviceClient;
     protected @Transient Options axisOptions;
     private long messagesSent;
     private long bytesSent;
     
     @Setup @SuppressWarnings("unused")
     private void setUp(AxisTestClientContext context, Channel channel, AxisTestClientConfigurator[] configurators) throws Exception {
         this.configurators = configurators;
         sender = context.getSender();
         serviceClient = new ServiceClient(context.getConfigurationContext(), null);
         axisOptions = new Options();
         axisOptions.setTo(channel.getEndpointReference());
         serviceClient.setOptions(axisOptions);
     }
     
     @TearDown @SuppressWarnings("unused")
     private void tearDown() throws Exception {
         serviceClient.cleanup();
     }
 
     public ContentType getContentType(ClientOptions options, ContentType contentType) {
         // TODO: this may be incorrect in some cases
         String charset = options.getCharset();
         if (charset == null) {
             return contentType;
         } else {
             return ContentTypeUtil.addCharset(contentType, options.getCharset());
         }
     }
 
     public void beforeSend() throws Exception {
         if (sender instanceof ManagementSupport) {
             ManagementSupport sender = (ManagementSupport)this.sender;
             messagesSent = sender.getMessagesSent();
             bytesSent = sender.getBytesSent();
         }
     }
 
     protected MessageContext send(ClientOptions options, AxisMessage message, QName operationQName,
             boolean block, String resultMessageLabel) throws Exception {
         
         OperationClient mepClient = serviceClient.createClient(operationQName);
         MessageContext mc = new MessageContext();
         mc.setProperty(Constants.Configuration.MESSAGE_TYPE, message.getMessageType());
         mc.setEnvelope(message.getEnvelope());
         Attachments attachments = message.getAttachments();
         if (attachments != null) {
             mc.setAttachmentMap(attachments);
             mc.setDoingSwA(true);
             mc.setProperty(Constants.Configuration.ENABLE_SWA, true);
         }
         for (AxisTestClientConfigurator configurator : configurators) {
             configurator.setupRequestMessageContext(mc);
         }
         mc.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, options.getCharset());
         mc.setServiceContext(serviceClient.getServiceContext());
         mepClient.addMessageContext(mc);
         mepClient.execute(block);
         return resultMessageLabel == null ? null : mepClient.getMessageContext(resultMessageLabel);
     }
 
     public void afterReceive() throws Exception {
         if (sender instanceof ManagementSupport) {
             ManagementSupport sender = (ManagementSupport)this.sender;
             Assert.assertEquals(messagesSent+1, sender.getMessagesSent());
            long newBytesSent = sender.getBytesSent();
            Assert.assertTrue("No increase in bytes sent (before sending: " + bytesSent +
                    "; after sending: " + newBytesSent + ")", newBytesSent > bytesSent);
         }
     }
 }
