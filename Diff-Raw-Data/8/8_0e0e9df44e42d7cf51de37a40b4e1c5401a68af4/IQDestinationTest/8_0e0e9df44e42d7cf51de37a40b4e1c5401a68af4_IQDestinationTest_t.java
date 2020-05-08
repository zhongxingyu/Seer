 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.cxf.transport.xmpp.iq;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import junit.framework.Assert;
 
 import org.apache.cxf.helpers.IOUtils;
 import org.apache.cxf.message.Exchange;
 import org.apache.cxf.message.Message;
 import org.apache.cxf.service.model.EndpointInfo;
 import org.apache.cxf.transport.Conduit;
 import org.apache.cxf.transport.MessageObserver;
 import org.apache.cxf.transport.xmpp.smackx.soap.SoapPacket;
 import org.apache.cxf.ws.addressing.EndpointReferenceType;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.junit.Before;
 import org.junit.Test;
 
 public class IQDestinationTest {
 
     // Test stubs for MessageObserver
     private Message testMsg;
     private boolean testMsgRecvByObserver;
     private MessageObserver fakeMsgObserver;
 
     // Test stubs for an XMPP connections.
     private PacketListener testPacketListener;
     private boolean testDisconnectedWasCalled;
     private XMPPConnection fakeXmppConnection;
 
     @Before
     public void initializeTestStubs() {
         testMsg = null;
         testMsgRecvByObserver = false;
         fakeMsgObserver = new MessageObserver() {
             @Override
             public void onMessage(Message msg) {
                 testMsgRecvByObserver = true;
                 testMsg = msg;
             }
         };
 
         testPacketListener = null;
         testDisconnectedWasCalled = false;
         fakeXmppConnection = new XMPPConnection("doesNotMatter") {
             @Override
             public void addPacketListener(PacketListener packetListener, PacketFilter packetFilter) {
                 testPacketListener = packetListener;
             }
 
             public void disconnect() {
                 testDisconnectedWasCalled = true;
             };
         };
     }
 
     @Test
     public void testMsgObserverIsSet() {
         EndpointInfo info = new EndpointInfo();
         info.setAddress("test-address");
 
         IQDestination destination = new IQDestination(info);
         destination.setMessageObserver(fakeMsgObserver);
 
         Assert.assertEquals("The correct message observer is being used", fakeMsgObserver,
                             destination.getMessageObserver());
     }
 
     @Test
     public void testRegistrationForXmppPackets() {
         EndpointInfo info = new EndpointInfo();
         info.setAddress("test-address");
 
         IQDestination destination = new IQDestination(info);
        destination.setXmppConnection(fakeXmppConnection, true);
 
         Assert.assertEquals("The destination registered for XMPP packets", destination, testPacketListener);
     }
 
     @Test
     public void testXmppConnectionWasClosed() {
         EndpointInfo info = new EndpointInfo();
         info.setAddress("test-address");
 
         IQDestination destination = new IQDestination(info);
        destination.setXmppConnection(fakeXmppConnection, true);
         destination.shutdown();
 
         Assert.assertEquals("XMPP connection was closed", true, testDisconnectedWasCalled);
     }
 
     @Test
     public void testXmppConnectionWasNotClosed() {
         EndpointInfo info = new EndpointInfo();
         info.setAddress("test-address");
 
         IQDestination destination = new IQDestination(info);
         destination.shutdown();
 
         Assert
             .assertEquals("Did not attempt to close null XMPP connection", false, testDisconnectedWasCalled);
     }
 
     @Test
     public void testEndPointAddressIsSaved() {
         EndpointInfo info = new EndpointInfo();
         info.setAddress("test-address");
 
         IQDestination destination = new IQDestination(info);
         EndpointReferenceType endPoint = destination.getAddress();
 
         Assert.assertEquals("Endpoint address stored correctly", "test-address", endPoint.getAddress()
             .getValue());
     }
 
     @Test
     public void testMessageSentToObserver() {
         EndpointInfo info = new EndpointInfo();
         info.setAddress("test-address");
 
         IQDestination destination = new IQDestination(info);
        destination.setXmppConnection(fakeXmppConnection, false);
         destination.setMessageObserver(fakeMsgObserver);
 
         String xmlMsg = "<something>that does not matter</something>";
         SoapPacket packet = new SoapPacket();
         packet.setEnvelope(xmlMsg);
         destination.processPacket(packet);
 
         Assert.assertTrue("Observer received message", testMsgRecvByObserver);
         Assert.assertNotNull("Msg received was not null", testMsg);
 
         InputStream stream = testMsg.getContent(InputStream.class);
         Assert.assertNotNull("Input stream is not null", stream);
 
         try {
             Assert.assertEquals("Message content is correct", xmlMsg, IOUtils.toString(stream));
         } catch (IOException e) {
             Assert.fail("Could not convert message content to a String");
         }
 
         Exchange exchange = testMsg.getExchange();
         Assert.assertNotNull("Message has an exchange", exchange);
 
         Conduit replyConduit = exchange.getConduit(testMsg);
         Assert.assertNotNull("Reply conduit is set", replyConduit);
         Assert.assertTrue("Reply channel is IQ conduit", replyConduit instanceof IQBackChannelConduit);
 
     }
 }
