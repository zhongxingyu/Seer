 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.pipeline;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.xml.ws.AsyncHandler;
 import javax.xml.ws.Response;
 
 import org.ebayopensource.turmeric.junit.asserts.JsonAssert;
 import org.ebayopensource.turmeric.junit.asserts.XmlAssert;
 import org.ebayopensource.turmeric.runtime.binding.BindingConstants;
 import org.ebayopensource.turmeric.runtime.common.types.ByteBufferWrapper;
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
 import org.ebayopensource.turmeric.runtime.sif.service.InvokerExchange;
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
 import org.ebayopensource.turmeric.runtime.tests.common.jetty.AbstractWithServerTest;
 import org.ebayopensource.turmeric.runtime.tests.common.junit.NeedsConfig;
 import org.ebayopensource.turmeric.runtime.tests.common.util.NVAssert;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 
 
 public class RawModeTest extends AbstractWithServerTest {
 	final int KB = 1024;
 
 	public static final String ADMIN_NAME = "test1";
 	public static final String CLIENT_NAME = "remote";
 
 	private Service m_test1;
 
 	private Map<String, String> m_headers;
 
 	public static final String XML_RAW_MESSAGE = "<ns3:Message xmlns:ns3=\"http://www.ebayopensource.org/turmeric/common/v1/types\" xmlns:sct=\"http://www.ebayopensource.org/turmeric/common/v1/types\">Hello</ns3:Message>";
 
 	public static final String NV_RAW_MESSAGE = "Message=Hello";
 
 	public static final String JSON_RAW_MESSAGE = "{\"Message\":\"Hello\",}";
 
 	public static final String EXPECTED_NORMAL_RESPONSE = "Hello";
 														
 	public static final String EXPECTED_XML_RESPONSE = "<?xml version='1.0' encoding='UTF-8'?>" +
 			                                           "<xs:Message xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ms=\"http://www.ebayopensource.org/turmeric/common/v1/types\">" +
 			                                           "Hello" +
 			                                           "</xs:Message>";
 
 	public static final String EXPECTED_JSON_RESPONSE ="{\"jsonns.ns2\":\"http://iop.pb.com\",\"jsonns.ns3\":\"http://www.ebay.com/test/soaframework/sample/types1\"," +
 			"\"jsonns.ms\":\"http://www.ebayopensource.org/turmeric/common/v1/types\"," +
 			"\"jsonns.xs\":\"http://www.w3.org/2001/XMLSchema\",\"jsonns.xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"," +
 			"\"xs.Message\":[\"Hello\"]}";
 
 	
 	public static final String EXPECTED_NV_RESPONSE ="nvns:ns2=\"http://iop.pb.com\"&nvns:ns3=\"http://www.ebay.com/test/soaframework/sample/types1\"&nvns:ms=\"http://www.ebayopensource.org/turmeric/common/v1/types\"&nvns:xs=\"http://www.w3.org/2001/XMLSchema\"&nvns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&xs:Message(0)=\"Hello\"";
 	
 	public final static String SOAP11_RAW_MESSAGE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
 			+ "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
 			+ "	<soapenv:Body>			"
 			+ "\n"
 			+ XML_RAW_MESSAGE
 			+ "\n"
 			+ "			</soapenv:Body>			" + "</soapenv:Envelope>";
 
 	private static final String XML_BAD_DATA_RESPONSE ="<?xml version='1.0' encoding='UTF-8'?>" +
 										"<ms:errorMessage xmlns:ms=\"http://www.ebayopensource.org/turmeric/common/v1/types\"><ms:error>" +
 										"<ms:errorId>5006</ms:errorId><ms:domain>TurmericRuntime</ms:domain><ms:subdomain>Comm_Recv</ms:subdomain>" +
 										"<ms:severity>Error</ms:severity><ms:category>System</ms:category><ms:message>" +
 										"Unable to create xml stream reader for XML: payload format incorrect or payload is empty</ms:message>" +
 										"<ms:parameter name=\"Param1\">XML</ms:parameter><ms:errorName>svc_data_xml_stream_reader_creation_error" +
 										"</ms:errorName><ms:resolution></ms:resolution><ms:organization>ebayopensource</ms:organization></ms:error>" +
 										"</ms:errorMessage>";
 		
 	@Rule
 	public NeedsConfig needsconfig = new NeedsConfig("config");
 	
 	private void debug(String msg) {
 		System.out.println(msg);
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		// tracer.setFailOnMultiResourceEntry(true);
 		m_headers = new HashMap<String, String>();
 		m_headers.put(SOAHeaders.SERVICE_OPERATION_NAME, "echoString");
 	}
 
 	@Test
 	public void testNormalCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "local", false);
 
 		String param1 = "Hello";
 		Object[] inParams = new Object[] { param1 };
 		List<Object> outParams = new ArrayList<Object>();
 
 		m_test1.invoke("echoString", inParams, outParams);
 
 		String normalResponse = (String) outParams.get(0);
 
 		debug("Normal response: " + normalResponse);
 
 		assertEquals(EXPECTED_NORMAL_RESPONSE, normalResponse);
 
 
 	}
 	
 	@Test
 	public void testNormalCallsRemote() throws Exception {

 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), false);
 
 		String param1 = "Hello";
 		Object[] inParams = new Object[] { param1 };
 		List<Object> outParams = new ArrayList<Object>();
 
 		m_test1.invoke("echoString", inParams, outParams);
 
 		String normalResponse = (String) outParams.get(0);
 
 		debug("Normal response: " + normalResponse);
 
 		assertEquals(EXPECTED_NORMAL_RESPONSE, normalResponse);
 
 
 	}
 	
 	@Test
 	public void testNormalCallsRemoteSOAP() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), false);
 
 		String param1 = "Hello";
 		Object[] inParams = new Object[] { param1 };
 		List<Object> outParams = new ArrayList<Object>();
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		m_test1.invoke("echoString", inParams, outParams);
 
 		String normalResponse = (String) outParams.get(0);
 
 		debug("Normal response: " + normalResponse);
 
 		assertEquals(EXPECTED_NORMAL_RESPONSE, normalResponse);
 
 
 	}
 
 	@Test
 	public void testXMLRawCalls() throws Exception {
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		byte[] param = XML_RAW_MESSAGE.getBytes();
 		debug("Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke(m_headers, inParam, outParam);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("XML Raw response: " + xmlResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, xmlResponse);	
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testXMLRawDispatchSyncCalls() throws Exception {
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		byte[] param = XML_RAW_MESSAGE.getBytes();
 		debug("Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("XML Raw response: " + xmlResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, xmlResponse);
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testXMLRawDispatchPullCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		byte[] param = XML_RAW_MESSAGE.getBytes();
 		debug("Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 		resp.get();
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("XML Raw response: " + xmlResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, xmlResponse);
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testXMLRawDispatchPushCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		byte[] param = XML_RAW_MESSAGE.getBytes();
 		debug("Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("XML Raw response: " + xmlResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, xmlResponse);
 
 
 	}
 	
 	@Test
 	public void testSOAP11RawCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		// ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		// options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		byte[] param = SOAP11_RAW_MESSAGE.getBytes();
 		debug("SOAP11 request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke(m_headers, inParam, outParam);
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("SOAP11 Raw response: " + xmlResponse);
 	}	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testSOAP11RawDispatchSyncCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		// ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		// options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		byte[] param = SOAP11_RAW_MESSAGE.getBytes();
 		debug("SOAP11 request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("SOAP11 Raw response: " + xmlResponse);
 
 		// assertEquals(EXPECTED_SOAP11_RESPONSE, xmlResponse);
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testSOAP11RawDispatchPullCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		// ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		// options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		byte[] param = SOAP11_RAW_MESSAGE.getBytes();
 		debug("SOAP11 request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 		resp.get();
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("SOAP11 Raw response: " + xmlResponse);
 		// assertEquals(EXPECTED_SOAP11_RESPONSE, xmlResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testSOAP11RawDispatchPushCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		// ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		// options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		byte[] param = SOAP11_RAW_MESSAGE.getBytes();
 		debug("SOAP11 request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("SOAP11 Raw response: " + xmlResponse);
 
 		// assertEquals(EXPECTED_SOAP11_RESPONSE, xmlResponse);
 	}
 	
 	@Test
 	public void testJSONRawResponse() throws Exception {
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		String param1 = "Hello";
 		Object[] inParams = new Object[] { param1 };
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke("echoString", inParams, outParam);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 		debug("jsonResponse: "+jsonResponse);
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJSONRawDispatchSyncResponse() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		String param1 = "Hello";
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJSONRawDispatchPullResponse() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		String param1 = "Hello";
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 		resp.get();
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJSONRawDispatchPushResponse() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		String param1 = "Hello";
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 
 	}
 	
 	@Test
 	public void testNV_JSONRawCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke(m_headers, inParam, outParam);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testNV_JSONRawDispatchSyncCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testNV_JSONRawDispatchPullCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 		resp.get();
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testNV_JSONRawDispatchPushCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 
 
 	}
 	
 	@Test
 	public void testNV_NVRawCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_NV);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke(m_headers, inParam, outParam);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String nvResponse = new String(outBuffer.array());
 
 		debug("NV Raw response: " + nvResponse);
 		
 		NVAssert.assertEquals(EXPECTED_NV_RESPONSE, nvResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testNV_NVRawDispatchSyncCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_NV);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String nvResponse = new String(outBuffer.array());
 
 		debug("NV Raw response: " + nvResponse);
 
 		NVAssert.assertEquals(EXPECTED_NV_RESPONSE, nvResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testNV_NVRawDispatchPullCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_NV);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 		resp.get();
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String nvResponse = new String(outBuffer.array());
 
 		debug("NV Raw response: " + nvResponse);
 
 		NVAssert.assertEquals(EXPECTED_NV_RESPONSE, nvResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testNV_NVRawDispatchPushCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_NV);
 		options.setResponseBinding(BindingConstants.PAYLOAD_NV);
 
 		byte[] param = NV_RAW_MESSAGE.getBytes();
 		debug("NV Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String nvResponse = new String(outBuffer.array());
 
 		debug("NV Raw response: " + nvResponse);
 
 		NVAssert.assertEquals(EXPECTED_NV_RESPONSE, nvResponse);
 	}
 	
 	@Test
 	public void testJSON_JSONRawCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_JSON);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = JSON_RAW_MESSAGE.getBytes();
 		debug("JSON Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke(m_headers, inParam, outParam);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJSON_JSONRawDispatchSyncCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_JSON);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = JSON_RAW_MESSAGE.getBytes();
 		debug("JSON Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJSON_JSONRawDispatchPullCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_JSON);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = JSON_RAW_MESSAGE.getBytes();
 		debug("JSON Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 		resp.get();
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testJSON_JSONRawDispatchPushCalls() throws Exception,
 			InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setRequestBinding(BindingConstants.PAYLOAD_JSON);
 		options.setResponseBinding(BindingConstants.PAYLOAD_JSON);
 
 		byte[] param = JSON_RAW_MESSAGE.getBytes();
 		debug("JSON Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 				outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String jsonResponse = new String(outBuffer.array());
 
 		debug("JSON Raw response: " + jsonResponse);
 
 		JsonAssert.assertJsonObjectEquals(EXPECTED_JSON_RESPONSE, jsonResponse);
 	}
 	
 	@Test
 	public void testRawCallsWithNullArgs() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ByteBufferWrapper inParam = null;
 		ByteBufferWrapper outParam = null;
 
 		try {
 			m_test1.invoke(m_headers, inParam, outParam);
 		} catch (Exception e) {
 			assertTrue("DII inbound byte buffer wrapper cannot be null"
 					.equals(e.getMessage()));
 		}
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchSyncCallsWithNullArgs() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ByteBufferWrapper inParam = null;
 		ByteBufferWrapper outParam = null;
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			m_test1.createDispatch("echoString", true).invoke(exchange);
 		} catch (Exception e) {
 			assertTrue("DII inbound byte buffer wrapper cannot be null"
 					.equals(e.getMessage()));
 		}
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPullCallsWithNullArgs() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ByteBufferWrapper inParam = null;
 		ByteBufferWrapper outParam = null;
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			m_test1.createDispatch("echoString", true).invokeAsync(exchange);
 		} catch (Exception e) {
 			assertTrue("DII inbound byte buffer wrapper cannot be null"
 					.equals(e.getMessage()));
 		}
 
 
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPushCallsWithNullArgs() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		ByteBufferWrapper inParam = null;
 		ByteBufferWrapper outParam = null;
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			Handler handler = new Handler();
 			m_test1.createDispatch("echoString", true).invokeAsync(exchange,
 					handler);
 		} catch (Exception e) {
 			assertTrue("DII inbound byte buffer wrapper cannot be null"
 					.equals(e.getMessage()));
 		}
 	}
 	
 	@Test
 	public void testRawCallsWithBadData() throws Exception {
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		String s = "randombaddataallcahrssothatwecanseeifusingmath.random.nextbytesiscausingtheconnectionresetbypeerbysendingitwierdbytes";
 		byte[] param = s.getBytes();
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			m_test1.invoke(m_headers, inParam, outParam);
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);			
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:" +
 					" payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(), 
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchSyncCallsWithBadData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		String s = "randombaddataallcahrssothatwecanseeifusingmath.random.nextbytesiscausingtheconnectionresetbypeerbysendingitwierdbytes";
 		byte[] param = s.getBytes();
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			m_test1.createDispatch("echoString", true).invoke(exchange);
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:" +
 					" payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(), 
 			containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPullCallsWithBadData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		//byte[] param = new byte[100];
 		//new Random().nextBytes(param);
 		String s = "randombaddataallcahrssothatwecanseeifusingmath.random.nextbytesiscausingtheconnectionresetbypeerbysendingitwierdbytes";
 		byte[] param = s.getBytes();
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			m_test1.createDispatch("echoString", true).invokeAsync(exchange);
 			m_test1.poll(true, true, 1000);
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:"
 					+ " payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPushCallsWithBadData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		String s = "randombaddataallcahrssothatwecanseeifusingmath.random.nextbytesiscausingtheconnectionresetbypeerbysendingitwierdbytes";
 		byte[] param = s.getBytes();
 		
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			Handler handler = new Handler();
 			Future<?> status = m_test1.createDispatch("echoString", true).invokeAsync(exchange, handler);
 			while (!status.isDone()) {
 				Thread.sleep(200);
 			}
 
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:"
 					+ " payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 	
 	@Test
 	public void testRawCallsWithEmptyData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		byte[] param = new byte[0];
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			m_test1.invoke(m_headers, inParam, outParam);
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:"
 					+ " payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchSyncCallsWithEmptyData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		byte[] param = new byte[0];
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			m_test1.createDispatch("echoString", true).invoke(exchange);
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:"
 					+ " payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPushCallsWithEmptyData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		byte[] param = new byte[0];
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			Handler handler = new Handler();
 			Future<?> status = m_test1.createDispatch("echoString", true)
 			.invokeAsync(exchange, handler);
 			while (!status.isDone()) {
 				Thread.sleep(200);
 			}
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:"
 					+ " payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPullCallsWithEmptyData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), true);
 
 		byte[] param = new byte[0];
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			m_test1.createDispatch("echoString", true).invokeAsync(exchange);
 			m_test1.poll(true, true, 1000);
 			ByteBuffer outBuffer = outParam.getByteBuffer();
 			String xmlResponse = new String(outBuffer.array());
 			XmlAssert.assertEquals(XML_BAD_DATA_RESPONSE, xmlResponse);	
 		} catch (Exception e) {
 			String expected = "Unable to create xml stream reader for XML:"
 					+ " payload format incorrect or payload is empty";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	/** ********************* NEW TEST CASES ********************** */
 
 	/**
 	 * This is for HotItems style usage; input is normal, but output is returned
 	 * opaquely.
 	 */
 	@Test
 	public void testRawCallsWithNormalData() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 		Object[] inParams = new Object[] { param1 };
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke("echoString", inParams, outParam);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, opaqueResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchSyncCallsWithNormalData()
 			throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, opaqueResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPullCallsWithNormalData()
 			throws Exception, InterruptedException, ExecutionException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		Response<?> resp = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange);
 
 		while (!resp.isDone()) {
 			Thread.sleep(200);
 		}
 
 		InvokerExchange inExchange = (InvokerExchange) resp.get();
 
 		ByteBufferWrapper outBufferWrapper = inExchange.getOutWrapper();
 		ByteBuffer outBuffer = outBufferWrapper.getByteBuffer();
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, opaqueResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testRawDispatchPushCallsWithNormalData()
 			throws Exception, InterruptedException {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		Handler handler = new Handler();
 		Future<?> status = m_test1.createDispatch("echoString", true)
 				.invokeAsync(exchange, handler);
 		while (!status.isDone()) {
 			Thread.sleep(200);
 		}
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		XmlAssert.assertEquals(EXPECTED_XML_RESPONSE, opaqueResponse);
 	}
 
 	/**/
 	@Test
 	@Ignore //SOAP 1.1 raw mode not supported currently. see TURMERIC-1073
 	public void testRawCallsWithNormalDataAndSOAP11() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		byte[] param = SOAP11_RAW_MESSAGE.getBytes();
 		debug("SOAP11 request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		// Use a ByteBufferWrapper instead of ByteBuffer since we don't know the
 		// response size
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		m_test1.invoke(m_headers, inParam, outParam);
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		String xmlResponse = new String(outBuffer.array());
 		debug("SOAP11 Raw response: " + xmlResponse);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	@Ignore //SOAP 1.1 raw mode not supported currently. see TURMERIC-1073
 	public void testRawDispatchSyncCallsWithNormalDataAndSOAP11()
 			throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		m_test1.createDispatch("echoString", true).invoke(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		Assert.assertThat("outParam.outBuffer", outBuffer, notNullValue());
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		assertEquals(param1, opaqueResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	@Ignore //SOAP 1.1 raw mode not supported currently. see TURMERIC-1073
 	public void testRawDispatchPullCallsWithNormalDataAndSOAP11()
 			throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		m_test1.createDispatch("echoString", true).invokeAsync(exchange);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		Assert.assertThat("outParam.outBuffer", outBuffer, notNullValue());
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		assertEquals(param1, opaqueResponse);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	@Ignore //SOAP 1.1 raw mode not supported currently. see TURMERIC-1073
 	public void testRawDispatchPushCallsWithNormalDataAndSOAP11()
 			throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, "remote", serverUri.toURL(), true);
 
 		String param1 = "Hello";
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		ServiceInvokerOptions options = m_test1.getInvokerOptions();
 		options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 
 		InvokerExchange exchange = new InvokerExchange(param1, outParam);
 		Handler handler = new Handler();
 		m_test1.createDispatch("echoString", true).invokeAsync(exchange,
 				handler);
 
 		ByteBuffer outBuffer = outParam.getByteBuffer();
 		Assert.assertThat("outParam.outBuffer", outBuffer, notNullValue());
 		String opaqueResponse = new String(outBuffer.array());
 		debug("Raw response: " + opaqueResponse);
 
 		assertEquals(param1, opaqueResponse);
 
 
 	}
 
 	/**/
 
 	/** ******************** Negative Tests ********************* */
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testTrueRawFlagDispatchPullCallsWithNonInvokerExchange()
 			throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), false);
 
 		try {
 			m_test1.createDispatch("echoString", true).invokeAsync(new Object());
 		} catch (Exception e) {
 			String expected = "java.lang.Object cannot be cast to " +
 					"org.ebayopensource.turmeric.runtime.sif.service.InvokerExchange";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testFalseRawFlagDispatchPullCallsWithNormalData()
 			throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), false);
 
 		String param1 = "Hello";
 		Object[] inParams = new Object[] { param1 };
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(inParams, outParam);
 			m_test1.createDispatch("echoString", false).invokeAsync(exchange);
 		} catch (Exception e) {
 			String expected = "incompatible type "
 					+ "org.ebayopensource.turmeric.runtime.sif.service.InvokerExchange passed";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testFalseRawFlagDispatchPushCalls() throws Exception {
 
 		m_test1 = ServiceFactory.create(ADMIN_NAME, CLIENT_NAME, serverUri.toURL(), false);
 
 		byte[] param = XML_RAW_MESSAGE.getBytes();
 		debug("Raw request: " + new String(param));
 
 		ByteBufferWrapper inParam = new ByteBufferWrapper();
 		inParam.setByteBuffer(ByteBuffer.wrap(param));
 
 		ByteBufferWrapper outParam = new ByteBufferWrapper();
 
 		try {
 			InvokerExchange exchange = new InvokerExchange(m_headers, inParam,
 					outParam);
 			Handler handler = new Handler();
 			m_test1.createDispatch("echoString", false).invokeAsync(exchange,
 					handler);
 		} catch (Exception e) {
 			String expected = "incompatible type "
 					+ "org.ebayopensource.turmeric.runtime.sif.service.InvokerExchange passed";
 			Assert.assertThat("Exception.message", e.getMessage(),
 					containsString(expected));
 		}
 	}
 
 	private class Handler implements AsyncHandler<Object> {
 
 		private Object m_resp = null;
 
 		public void handleResponse(Response<Object> resp) {
 			try {
 				m_resp = resp.get();
 			} catch (Throwable e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		@SuppressWarnings("unused")
 		public Object getRespString() {
 			return m_resp;
 		}
 	}
 }
 
 
