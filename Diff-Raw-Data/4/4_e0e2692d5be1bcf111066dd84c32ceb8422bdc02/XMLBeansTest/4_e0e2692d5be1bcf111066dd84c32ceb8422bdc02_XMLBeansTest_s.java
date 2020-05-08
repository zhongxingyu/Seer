 /** 
  * 
  * Copyright 2004 Protique Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License. 
  * 
  **/
 package org.codehaus.xfire.xmlbeans;
 
 import org.apache.xmlbeans.XmlObject;
 import org.codehaus.xfire.AbstractXFireTest;
 import org.codehaus.xfire.handler.SoapHandler;
 
 /**
  * @version $Revision$
  */
 public class XMLBeansTest 
     extends AbstractXFireTest
 {
     public void testRequestResponse() throws Exception
     {
         TestHandler handler = new TestHandler();
         SoapHandler shandler = new SoapHandler(handler);
         
        XMLBeansService service = new XMLBeansService();
         service.setName("Test");
         service.setServiceHandler(shandler);
         
         getServiceRegistry().register(service);
         
         invokeService( "Test", "test.xml" );
         
         XmlObject[] xmlObject = handler.getRequest();
         assertNotNull(xmlObject);
         assertEquals(3, xmlObject.length);
     }
 }
