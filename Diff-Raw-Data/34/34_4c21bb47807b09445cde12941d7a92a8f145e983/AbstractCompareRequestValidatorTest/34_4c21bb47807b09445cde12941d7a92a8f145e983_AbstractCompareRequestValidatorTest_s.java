 /**
  * Copyright 2009-2010 the original author or authors.
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
 package net.javacrumbs.springws.test.validator;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import net.javacrumbs.springws.test.WsTestException;
 import net.javacrumbs.springws.test.lookup.ResourceLookup;
 
 import org.custommonkey.xmlunit.Diff;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.ws.WebServiceMessage;
 import org.w3c.dom.Document;
 
 public class AbstractCompareRequestValidatorTest extends AbstractValidatorTest{
 
 	
 		
 	@Test
 	public void testValidateRequest() throws Exception
 	{
 		WebServiceMessage message = getValidMessage();
 		final Document messageDoc = getXmlUtil().loadDocument(message);
 		Resource controlResource = new ClassPathResource("xml/control-message-test.xml");
 		final Document controlDoc = getXmlUtil().loadDocument(controlResource);
 
 		ResourceLookup resourceLookup = createMock(ResourceLookup.class);
		expect(resourceLookup.lookupResource(null, null)).andReturn(controlResource);
 	
 		AbstractCompareRequestValidator validator = new AbstractCompareRequestValidator(){
 			@Override
 			protected void compareDocuments(Document controlDocument, Document messageDocument) {
 				assertTrue(XMLUnit.compareXML(controlDoc, controlDocument).identical());
 				assertTrue(XMLUnit.compareXML(messageDoc, messageDocument).identical());
 			}
 			
 		};
 		validator.setControlResourceLookup(resourceLookup);
 		assertTrue(validator.isSoap(controlDoc));
 		assertSame(resourceLookup, validator.getControlResourceLookup());
 		validator.afterPropertiesSet();
 		
 		replay(resourceLookup);
 		
		validator.processRequest(null, messageFactory, message);
 		
 		verify(resourceLookup);
 	}
 	
 	@Test
 	public void testValidatePayload() throws Exception
 	{
 		WebServiceMessage message = getValidMessage();
 		final Document messagePayloadDoc = getXmlUtil().loadDocument(message.getPayloadSource());
 		
 		Resource controlResource = new ClassPathResource("xml/control-message-payload-test.xml");
 		final Document controlDoc = getXmlUtil().loadDocument(controlResource);
 		
 		ResourceLookup resourceLookup = createMock(ResourceLookup.class);
		expect(resourceLookup.lookupResource(null, null)).andReturn(controlResource);
 		
 		AbstractCompareRequestValidator validator = new AbstractCompareRequestValidator(){
 			@Override
 			protected void compareDocuments(Document controlDocument, Document messageDocument) {
 				assertTrue(XMLUnit.compareXML(controlDoc, controlDocument).identical());
 				
 				Diff diff = new Diff(messagePayloadDoc, messageDocument);
 				assertTrue(diff.toString(), diff.similar());
 			}
 			
 		};
 		assertFalse(validator.isSoap(controlDoc));
 		validator.setControlResourceLookup(resourceLookup);
 		assertSame(resourceLookup, validator.getControlResourceLookup());
 		validator.afterPropertiesSet();
 		
 		replay(resourceLookup);
 		
 		validator.processRequest(null, messageFactory, message);
 		
 		verify(resourceLookup);
 	}
 	
 	@Test
 	public void testNonExistingControlDocument() throws Exception
 	{
 		WebServiceMessage message = getValidMessage();
 		
 		ResourceLookup resourceLookup = createMock(ResourceLookup.class);
		expect(resourceLookup.lookupResource(null, null)).andReturn(null);
 		
 		AbstractCompareRequestValidator validator = new AbstractCompareRequestValidator(){
 			@Override
 			protected void compareDocuments(Document controlDocument, Document messageDocument) {
 				fail("Should not get here");
 			}
 			
 		};
 		validator.setControlResourceLookup(resourceLookup);
 		validator.afterPropertiesSet();
 		
 		replay(resourceLookup);
 		
		validator.processRequest(null, messageFactory, message);
 		
 		verify(resourceLookup);
 	}
 	@Test(expected=WsTestException.class)
 	public void testFailIfNotFOund() throws Exception
 	{
 		WebServiceMessage message = getValidMessage();
 		
 		ResourceLookup resourceLookup = createMock(ResourceLookup.class);
		expect(resourceLookup.lookupResource(null, null)).andReturn(null);
 		
 		AbstractCompareRequestValidator validator = new AbstractCompareRequestValidator(){
 			@Override
 			protected void compareDocuments(Document controlDocument, Document messageDocument) {
 				fail("Should not get here");
 			}
 			
 		};
 		validator.setControlResourceLookup(resourceLookup);
 		validator.setFailIfControlResourceNotFound(true);
 		validator.afterPropertiesSet();
 		
 		replay(resourceLookup);
 		
		validator.processRequest(null, messageFactory, message);
 
 	}
 }
