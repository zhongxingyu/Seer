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
 package org.apache.servicemix.itests;
 
 import java.io.StringReader;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.ws.wsaddressing.W3CEndpointReference;
 
 import junit.framework.TestCase;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.broker.BrokerService;
import org.apache.servicemix.client.DefaultServiceMixClient;
 import org.apache.servicemix.http.HttpComponent;
 import org.apache.servicemix.http.HttpEndpoint;
 import org.apache.servicemix.jbi.container.ActivationSpec;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.soap.interceptors.jbi.JbiConstants;
 import org.apache.servicemix.tck.ReceiverComponent;
 import org.apache.servicemix.wsn.client.AbstractWSAClient;
 import org.apache.servicemix.wsn.client.NotificationBroker;
 import org.apache.servicemix.wsn.component.WSNComponent;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 public class WSNComponentTest extends TestCase {
 
     public static QName NOTIFICATION_BROKER = new QName("http://servicemix.org/wsnotification", "NotificationBroker");
 
     private JBIContainer jbi;
     private BrokerService jmsBroker;
     private NotificationBroker wsnBroker;
     private WSNComponent wsnComponent;
 
     protected void setUp() throws Exception {
         jmsBroker = new BrokerService();
         jmsBroker.setPersistent(false);
         jmsBroker.addConnector("vm://localhost");
         jmsBroker.start();
 
         jbi = new JBIContainer();
         jbi.setEmbedded(true);
         jbi.init();
         jbi.start();
 
         wsnComponent = new WSNComponent();
         wsnComponent.setConnectionFactory(new ActiveMQConnectionFactory("vm://localhost"));
         ActivationSpec as = new ActivationSpec();
         as.setComponentName("servicemix-wsn2005");
         as.setComponent(wsnComponent);
         jbi.activateComponent(as);
 
        wsnBroker = new NotificationBroker(new DefaultServiceMixClient(jbi).getContext());
     }
 
     protected void tearDown() throws Exception {
         if (jbi != null) {
             jbi.shutDown();
         }
         if (jmsBroker != null) {
             jmsBroker.stop();
         }
     }
 
     public void testDynamicSubscription() throws Exception {
         HttpComponent httpComponent = new HttpComponent();
 
         HttpEndpoint httpWSNBroker = new HttpEndpoint();
         httpWSNBroker.setService(new QName("http://servicemix.org/wsnotification", "NotificationBroker"));
         httpWSNBroker.setEndpoint("Broker");
         httpWSNBroker.setRoleAsString("consumer");
         httpWSNBroker.setLocationURI("http://localhost:8192/WSNBroker/");
         httpWSNBroker.setSoap(true);
 
         HttpEndpoint httpReceiver = new HttpEndpoint();
         httpReceiver.setService(new QName("receiver"));
         httpReceiver.setEndpoint("endpoint");
         httpReceiver.setRoleAsString("consumer");
         httpReceiver.setLocationURI("http://localhost:8192/Receiver/");
         httpReceiver.setDefaultMep(JbiConstants.IN_ONLY);
         httpReceiver.setSoap(true);
 
         httpComponent.setEndpoints(new HttpEndpoint[] { httpWSNBroker, httpReceiver });
         jbi.activateComponent(new ActivationSpec("servicemix-http", httpComponent));
 
         ReceiverComponent receiver = new ReceiverComponent();
         receiver.setService(new QName("receiver"));
         receiver.setEndpoint("endpoint");
         jbi.activateComponent(new ActivationSpec("receiver", receiver));
 
         W3CEndpointReference epr = AbstractWSAClient.createWSA("http://localhost:8192/Receiver/?http.soap=true");
         wsnBroker.subscribe(epr, "myTopic", null);
         wsnBroker.notify("myTopic", parse("<hello>world</hello>"));
 
         receiver.getMessageList().assertMessagesReceived(1);
     }
 
     private Element parse(String txt) throws Exception {
         DocumentBuilder builder = new SourceTransformer().createDocumentBuilder();
         InputSource is = new InputSource(new StringReader(txt));
         Document doc = builder.parse(is);
         return doc.getDocumentElement();
     }
 
 }
