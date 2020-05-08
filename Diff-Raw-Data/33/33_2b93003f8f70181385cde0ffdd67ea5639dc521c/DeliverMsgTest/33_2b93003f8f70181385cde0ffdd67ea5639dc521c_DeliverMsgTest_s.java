 /*
  * ****************************************************************************************************************
  *  *
  *  * Copyright (C) 2012 by Cognitive Medical Systems, Inc (http://www.cognitivemedciine.com)
  *  *
  *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
  *  * with the License. You may obtain a copy of the License at
  *  *
  *  *     http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  *  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and limitations under the License.
  *  *
  *  ****************************************************************************************************************
  *
  * ****************************************************************************************************************
  *  * Socratic Grid contains components to which third party terms apply. To comply with these terms, the following
  *  * notice is provided:
  *  *
  *  * TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION
  *  * Copyright (c) 2008, Nationwide Health Information Network (NHIN) Connect. All rights reserved.
  *  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that
  *  * the following conditions are met:
  *  *
  *  * - Redistributions of source code must retain the above copyright notice, this list of conditions and the
  *  *     following disclaimer.
  *  * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
  *  *     following disclaimer in the documentation and/or other materials provided with the distribution.
  *  * - Neither the name of the NHIN Connect Project nor the names of its contributors may be used to endorse or
  *  *     promote products derived from this software without specific prior written permission.
  *  *
  *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  *  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  *  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION HOWEVER
  *  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  *  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  *  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  *
  *  * END OF TERMS AND CONDITIONS
  *  *
  *  ****************************************************************************************************************
  */
 
 /*
  * @author jharby
  * 
  * Teets for insert and update alert. Keep ignored unless necessary because
  * they change data in the db.
  */
 package org.socraticgrid.dsaserviceweb;
 
 import org.socraticgrid.dsa.DSAIntegration;
 import org.socraticgrid.dsa.DSAIntegrationPortType;
 import org.socraticgrid.dsa.DeliverMessageRequestType;
 import org.socraticgrid.dsa.DeliverMessageResponseType;
 import java.util.Date;
 import javax.xml.ws.BindingProvider;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author jharby
  */
 public class DeliverMsgTest {
 
     public static final String ENDPT_LOCAL =
             "http://localhost:8080/DSAServiceWeb/DSAIntegration";
     public static final String ENDPT_47 =
            "http://192.168.5.47:9763/DSAServiceWeb/DSAIntegration";
 
     public DeliverMsgTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testDeliverMsgAlertInsert() {
         DSAIntegrationPortType port = getPort();
         DeliverMessageRequestType request = new DeliverMessageRequestType();
         request.setRefId("22");
         request.getSubject().add("1");
         request.setBody("TEST PAYLOAD - DATE IS " + new Date());
         request.setHeader("TEST HEADER - DATE IS " + new Date());
         request.setDeliveryDate("10/01/2011 10:10:10");
         request.setSender("fry.emory");
         request.getMainRecipients().add("99990070");
         request.setPriority("HIGH");
         request.getType().add("ALERT");
         request.setStatus("77");
         DeliverMessageResponseType response = port.deliverMessage(request);
         System.out.println("DELIVER MESSAGE RESPONSE IS " + response.getStatus());
     }
 
     @Test
     public void testDeliverMsgAlertUpdate() {
         DSAIntegrationPortType port = getPort();
         DeliverMessageRequestType request = new DeliverMessageRequestType();
         request.setRefId("888888");
         request.getSubject().add("1");
         request.setBody("UPDATE TEST PAYLOAD - DATE IS " + new Date());
         request.setHeader("UPDATE TEST HEADER - DATE IS " + new Date());
         request.setDeliveryDate("10/01/2011 10:10:10");
         request.setSender("fry.emory");
         request.getMainRecipients().add("doe.jane");
         request.setPriority("HIGH");
         request.getType().add("ALERT");
         DeliverMessageResponseType response = port.deliverMessage(request);
         System.out.println("DELIVER MESSAGE RESPONSE IS " + response.getStatus());
     }
 
     @Test
     public void testDeliverMsgSMS() {
         DSAIntegrationPortType port = getPort();
         DeliverMessageRequestType request = new DeliverMessageRequestType();
         request.setRefId("888888");
         request.getSubject().add("1");
         request.setBody("UPDATE TEST PAYLOAD - DATE IS " + new Date());
         request.setHeader("UPDATE TEST HEADER - DATE IS " + new Date());
         request.setDeliveryDate("10/01/2011 10:10:10");
         request.setSender("fry.emory");
         request.getMainRecipients().add("858-610-0305");
         //request.getMainRecipients().add("858-395-7317");
         request.setPriority("HIGH");
         request.getType().add("SMS");
         DeliverMessageResponseType response = port.deliverMessage(request);
         System.out.println("DELIVER MESSAGE RESPONSE IS " + response.getStatus());
     }
 
     private DSAIntegrationPortType getPort() {
         DSAIntegration service = new DSAIntegration();
         DSAIntegrationPortType port = service.getDSAIntegrationPortSoap11();
         ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                 ENDPT_47);
                 //ENDPT_LOCAL);
         return port;
     }
 }
