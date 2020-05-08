 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.protocolprocessor.soap;
 
 import static org.hamcrest.Matchers.containsString;
 
 import javax.xml.stream.XMLStreamReader;
 
 import org.apache.axiom.soap.SOAP11Constants;
 import org.apache.axiom.soap.SOAP12Constants;
 import org.apache.axis2.transport.http.HTTPConstants;
 import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
 import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.ProtocolProcessorInitContextImpl;
 import org.ebayopensource.turmeric.runtime.common.impl.protocolprocessor.soap.BaseSOAPProtocolProcessor;
 import org.ebayopensource.turmeric.runtime.common.impl.protocolprocessor.soap.SOAPUtils;
 import org.ebayopensource.turmeric.runtime.common.pipeline.InboundMessage;
 import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
 import org.ebayopensource.turmeric.runtime.common.service.ServiceId;
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.ebayopensource.turmeric.runtime.spf.impl.protocolprocessor.soap.ServerSOAPProtocolProcessor;
 import org.ebayopensource.turmeric.runtime.spf.service.ServerServiceId;
 import org.ebayopensource.turmeric.runtime.tests.common.AbstractTurmericTestCase;
 import org.ebayopensource.turmeric.runtime.tests.common.util.SOAPTestUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 
 /**
  * Unittest for ServerSOAPProtocolProcessor class
  * @author gyue
  */
 public class ServerSOAPProtocolProcessorTest extends AbstractTurmericTestCase {
 
 	private ServerSOAPProtocolProcessor createServerProtocolProcessor(
 		String protocolName, String version) throws Exception
 	{
 		ServerSOAPProtocolProcessor protocolProcessor = new ServerSOAPProtocolProcessor();
 		ServiceId svcId = ServerServiceId.createFallbackServiceId("test_admin_name");
 		ProtocolProcessorInitContextImpl initCtx =
 			new ProtocolProcessorInitContextImpl(svcId, protocolName, version);
 		protocolProcessor.init(initCtx);
 		initCtx.kill();
 		return protocolProcessor;
 	}
 	
     private org.apache.axis2.context.MessageContext assertMessageValidINContextAndBody(MessageContext ctx)
     {
         org.apache.axis2.context.MessageContext msgcontext = SOAPAssert.assertMessageValidINContext(ctx);
         SOAPAssert.assertBodyHasNoFault(msgcontext);
         return msgcontext;
     }
 
     @Test
 	public  void serverBeforeRequestPipelinePositiveTest() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_REQUEST);
 
 		// invoke beforeRequestPipeline
 		protocolProcessor.beforeRequestPipeline(ctx);
 
 		assertMessageValidINContextAndBody(ctx);
 
 		// expects XMLReader pointing to the beginning of body
 
 		// DO NOT GET THE READER FROM SOAP ENVELOPE - it's a different one! (why?)
 		//XMLStreamReader reader = axisContext.getEnvelope().getBody().getXMLStreamReader();
 		XMLStreamReader reader = ((InboundMessage)ctx.getRequestMessage()).getXMLStreamReader();
 		Assert.assertTrue(SOAPTestUtils.validateXMLReaderAtStartElement(reader, SOAPTestUtils.START_XML_BODY_ELEMENT));
 	}
     
 	/**
 	 * Test the scenario when there is no content type. In this case, the system is defaulted to use SOAP1.1
 	 */
     @Test
 	public  void serverBeforeRequestPipelinePositive_NoContentTypeTest() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_REQUEST);
 
 		// nulling out content type
 		ctx.getRequestMessage().setTransportHeader(HTTPConstants.HEADER_CONTENT_TYPE, null);
 
 		// invoke beforeRequestPipeline
 		protocolProcessor.beforeRequestPipeline(ctx);
 
 		org.apache.axis2.context.MessageContext msgctx = assertMessageValidINContextAndBody(ctx);
 
 		// Verify that soap1.1 is being defaulted when content type is not specified
 		Assert.assertTrue("SOAP Envelope is not under SOAP1.1 namespace, which we expect", 
 				SOAPUtils.isSOAP11Envelope(msgctx.getEnvelope()));
 	}
 
 
 	/**
 	 * In the case of SOAP1.2 (SOAP1.2 content type specified + SOAP1.2 envelope is used)
 	 */
 	@Test
 	public  void serverBeforeRequestPipelinePositive_SOAP12Test() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_12, "1.2");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_12_REQUEST);
 
 		// set content type to be SOAP1.2 specific: "application/soap+xml"
 		ctx.getRequestMessage().setTransportHeader(HTTPConstants.HEADER_CONTENT_TYPE.toUpperCase(), SOAP12Constants.SOAP_12_CONTENT_TYPE);
 
 		// invoke beforeRequestPipeline
 		protocolProcessor.beforeRequestPipeline(ctx);
 
 		org.apache.axis2.context.MessageContext msgctx = assertMessageValidINContextAndBody(ctx);
 
 		// Verify that the envelope is of SOAP1.2 namespace
 		Assert.assertTrue("SOAP Envelope is not under SOAP1.2 namespace, which we expect", 
 				SOAPUtils.isSOAP12Envelope(msgctx.getEnvelope()));
 	}
 	
 	@Test(expected=ServiceException.class)
 	public  void serverBeforeRequestPipelineNegative_InvalidStartBodyTagTest() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 				SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.BAD_SOAP_REQUEST_INVALIDSTARTBODYTAG);
 
 		// invoke beforeRequestPipeline
 		protocolProcessor.beforeRequestPipeline(ctx);
 	}
 	
 	@Test(expected=ServiceException.class)
 	public  void serverBeforeRequestPipelineNegative_InvalidStartEnvelopeTagTest() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.BAD_SOAP_REQUEST_INVALIDSTARTENVELOPETAG);
 
 		// invoke beforeRequestPipeline
 		protocolProcessor.beforeRequestPipeline(ctx);
 	}
 
 	/**
 	 * Test the envelope-contentType mismatch scenarios
 	 * 1. SOAP1.1 Envelope + SOAP1.2 Content type
 	 * 2. SOAP1.2 Envelope + SOAP1.1 Content type
 	 */
 		@Test
 	public  void serverBeforeRequestPipelineNegative_EnvelopeContentTypeMismatchTest() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// SOAP1.1 Envelope, SOAP1.2 content type
 		try {
 
 			// create context
 			MessageContext ctx =
 					SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_REQUEST);
 
 			// set SOAP1.2 content type: "application/soap+xml"
 			ctx.getRequestMessage().setTransportHeader(HTTPConstants.HEADER_CONTENT_TYPE.toUpperCase(), SOAP12Constants.SOAP_12_CONTENT_TYPE);
 
 			// invoke beforeRequestPipeline
 			protocolProcessor.beforeRequestPipeline(ctx);
 			Assert.fail("Expected exception of type: " + ServiceException.class);
 		} catch (ServiceException e) {
 			Assert.assertThat(e.getMessage(), 
 					containsString("Transport level information does not match with SOAP Message namespace URI"));
 		}
 
 		// SOAP1.2 Envelope, SOAP1.1 content type
 		try {
 			// create context
 			MessageContext ctx =
 					SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_12_REQUEST);
 
 			// set SOAP1.2 content type: "application/soap+xml"
 			ctx.getRequestMessage().setTransportHeader(HTTPConstants.HEADER_CONTENT_TYPE, SOAP11Constants.SOAP_11_CONTENT_TYPE);
 
 			// invoke beforeRequestPipeline
 			protocolProcessor.beforeRequestPipeline(ctx);
 			Assert.fail("Expected exception of type: " + ServiceException.class);
 		} catch (ServiceException e) {
 			Assert.assertThat(e.getMessage(), 
 					containsString("Transport level information does not match with SOAP Message namespace URI"));
 		}
 	}
 
 
 	@Test
 	public  void serverBeforeResponseDispatchPositiveTest() throws Exception {
 		System.out.println("testServerBeforeResponseDispatchPositive");
 
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_REQUEST);
 
 		// crate axis2 context and add to ebay context
 		org.apache.axis2.context.MessageContext axisContext =
 						SOAPTestUtils.createTestAxis2InboundMessageContext(protocolProcessor, ctx, SOAConstants.MSG_PROTOCOL_SOAP_11);
 		ctx.setProperty(BaseSOAPProtocolProcessor.AXIS_IN_CONTEXT, axisContext);
 
 		// invoke beforeResponseDispatch
 		protocolProcessor.beforeResponseDispatch(ctx);
 		
 		org.apache.axis2.context.MessageContext msgctx = SOAPAssert.assertMessageValidOUTContext(ctx);
 		Assert.assertFalse("SOAP Body has fault", msgctx.getEnvelope().getBody().hasFault());
 	}
 	
 	@Test
 	public  void serverBeforeResponseDispatchSOAP11Negative_WithExceptionTest() throws Exception {
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_REQUEST);
 		// crate axis2 context and add to ebay context
 		org.apache.axis2.context.MessageContext axisContext =
 					SOAPTestUtils.createTestAxis2InboundMessageContext(protocolProcessor, ctx, SOAConstants.MSG_PROTOCOL_SOAP_11);
 		ctx.setProperty(BaseSOAPProtocolProcessor.AXIS_IN_CONTEXT, axisContext);
 
 		// add error to the context
 		Exception expectedEx = new Exception("TEST EXCEPTION");
 		ctx.addError(expectedEx);
 
 		// invoke beforeResponseDispatch
 		protocolProcessor.beforeResponseDispatch(ctx);
 
 		// expects Axis out context is created
 		org.apache.axis2.context.MessageContext msgctx = SOAPAssert.assertMessageValidOUTContext(ctx);
 		Assert.assertTrue("SOAP Body should contain fault, but it does not", msgctx.getEnvelope().getBody().hasFault());
 
 		Assert.assertTrue("Wrong namespace on SOAP Fault message! Expect SOAP1.1 namespace",
 				msgctx.getEnvelope().getNamespace().getNamespaceURI().equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI));
 
 		// make sure it's the same exception object after axis2 processing
 		Assert.assertEquals(expectedEx, (Exception) ctx.getErrorList().get(0));
 	}
 	
 	@Test
 	public  void serverBeforeResponseDispatchSOAP12Negative_WithExceptionTest() throws Exception {
 		System.out.println("testServerBeforeResponseDispatchSOAP12Negative_WithException");
 		// create server pp
 		ServerSOAPProtocolProcessor protocolProcessor = createServerProtocolProcessor(SOAConstants.MSG_PROTOCOL_SOAP_11, "1.1");
 
 		// create context
 		MessageContext ctx =
 			SOAPTestUtils.createServerMessageContextForTest1Service(SOAPTestUtils.GOOD_SOAP_12_REQUEST);
 		// crate axis2 context and add to ebay context
 		org.apache.axis2.context.MessageContext axisContext =
 					SOAPTestUtils.createTestAxis2InboundMessageContext(protocolProcessor, ctx, SOAConstants.MSG_PROTOCOL_SOAP_12);
 		ctx.setProperty(BaseSOAPProtocolProcessor.AXIS_IN_CONTEXT, axisContext);
 
 		// add error to the context
 		Exception expectedEx = new Exception("TEST EXCEPTION");
 		ctx.addError(expectedEx);
 
 		// invoke beforeResponseDispatch
 		protocolProcessor.beforeResponseDispatch(ctx);
 
 		// expects Axis out context is created
 		org.apache.axis2.context.MessageContext msgctx = SOAPAssert.assertMessageValidOUTContext(ctx);
 		Assert.assertTrue("SOAP Body should contain fault, but it does not", msgctx.getEnvelope().getBody().hasFault());
 		
 		Assert.assertTrue("Wrong namespace on SOAP Fault message. Expect SOAP1.2 namespace",
 				msgctx.getEnvelope().getNamespace().getNamespaceURI().equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI));
 
 		// make sure it's the same exception object after axis2 processing
 		Assert.assertEquals(expectedEx, (Exception) ctx.getErrorList().get(0));
 	}
 }
