 /**
  * Tests for common utility class.
  * <p>
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  * <p>
  * All other source code is copyright Tresys Technology and licensed as below.
  * <p>
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  * <p>
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  * <p>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p>
  *    http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.tresys.jalop.common;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.KeyException;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.crypto.MarshalException;
 import javax.xml.crypto.dsig.DigestMethod;
 import javax.xml.crypto.dsig.XMLSignContext;
 import javax.xml.crypto.dsig.XMLSignature;
 import javax.xml.crypto.dsig.XMLSignatureException;
 import javax.xml.crypto.dsig.XMLSignatureFactory;
 import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
 import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
 import javax.xml.crypto.dsig.spec.TransformParameterSpec;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import mockit.Capturing;
 import mockit.Expectations;
 import mockit.Mock;
 import mockit.MockUp;
 import mockit.Mocked;
 import mockit.Mockit;
 import mockit.NonStrictExpectations;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.etsy.net.MessageHeader;
 import com.etsy.net.UnixDomainSocket;
 import com.etsy.net.UnixDomainSocket.UnixDomainSocketOutputStream;
 import com.etsy.net.UnixDomainSocketClient;
 import com.tresys.jalop.common.ConnectionHeader.MessageType;
 import com.tresys.jalop.common.JALUtils.DMType;
 import com.tresys.jalop.producer.ApplicationMetadataXML;
 import com.tresys.jalop.producer.JALProducer;
 import com.tresys.jalop.producer.LoggerXML;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ApplicationMetadataType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.LoggerType;
 import com.tresys.jalop.schemas.mil.dod.jalop_1_0.applicationmetadatatypes.ObjectFactory;
 
 public class TestJALUtils {
 
 	private static JALUtils utils;
 	private static ObjectFactory of;
 	private ApplicationMetadataType amt;
 	private LoggerType logger;
 	private Document doc;
 	
 	@Before
 	public void setup() {
 		utils = new JALUtils();
 		of = new ObjectFactory();
 		amt = new ApplicationMetadataType();
 		logger = new LoggerType();		
 		
 	}
 
 	@Test
 	public void testGetCurrentTimeWorks() throws Exception {
 		GregorianCalendar gc = new GregorianCalendar();
 		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
 
 		XMLGregorianCalendar date;
 		date = utils.getCurrentTime();
 		assertTrue(date != null);
 		assertTrue(date instanceof XMLGregorianCalendar);
 		assertTrue(date.getMonth() == xmlCal.getMonth());
 		assertTrue(date.getDay() == xmlCal.getDay());
 		assertTrue(date.getYear() == xmlCal.getYear());
 		assertTrue(date.getEon() == xmlCal.getEon());
 	}
 	
 	@Test(expected = DatatypeConfigurationException.class)
 	public void testGetCurrentTimeThrowsExceptionOnFailure() throws Exception {
 		new MockUp<DatatypeFactory>() {
 			@Mock
 			DatatypeFactory newInstance() throws DatatypeConfigurationException
 			{
 				throw new DatatypeConfigurationException();
 			}
 		};
 		
 		assertTrue(utils.getCurrentTime() == null);
 	}
 	
 	@Test
 	public void testMarshalWorks() throws Exception {
 		amt.setJID("JID");
 		amt.setLogger(logger);
 		
 		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
 		assertNotNull(appMeta);
 		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
 		assertNotNull(jc);
 
 		doc = utils.marshal(jc, appMeta);
 		assertNotNull(doc);
 		
 		Node appMetaElem = doc.getElementsByTagName("ApplicationMetadata").item(0);
 		assertNotNull(appMetaElem);
 		assertEquals("ApplicationMetadata", appMetaElem.getNodeName());
 		assertEquals("JID", appMetaElem.getAttributes().getNamedItem("JID").getNodeValue());
 		Node loggerElem = appMetaElem.getFirstChild();
 		assertNotNull(loggerElem);
 		assertEquals("Logger", loggerElem.getNodeName());
 		assertEquals(null, loggerElem.getNodeValue());
 	}
 	
 	@Test(expected = JAXBException.class)
 	public void testMarshalThrowsJAXBExceptionWhenMarshalFails() throws Exception {
 
 		new NonStrictExpectations() {
 			@Capturing Marshaller m;
 			{
 				m.marshal((Object)any, (Document)any); result = new JAXBException("error");
 			}
 		};
 		
 		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
 		assertNotNull(appMeta);
 		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
 		assertNotNull(jc);
 		
 		doc = utils.marshal(jc, appMeta);
 	}
 
 	@Test(expected = JAXBException.class)
 	public void testMarshalThrowsJAXBExceptionWhenSchemaValidationFails() throws Exception {
 
 		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
 		assertNotNull(appMeta);
 		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
 		assertNotNull(jc);
 
 		doc = utils.marshal(jc, appMeta);
 	}
 
 	@Test
 	public void testManifestWorks() throws Exception {
 
 		amt.setJID("JID");
 		amt.setLogger(logger);
 
 		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
 		assertNotNull(appMeta);
 		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
 		assertNotNull(jc);
 
 		doc = utils.marshal(jc, appMeta);
 
 		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, InputStream.class, MessageType.class);
 
 		method.setAccessible(true);
 		method.invoke(utils, doc, DMType.SHA256, new ByteArrayInputStream("String buffer".getBytes()), MessageType.JALP_LOG_MSG);
 
 		Element manifest = (Element)doc.getElementsByTagName("Manifest").item(0);
 		assertNotNull(manifest);
 
 		Element reference = (Element)doc.getElementsByTagName("Reference").item(0);
 		String uriValue = reference.getAttribute("URI");
 		assertEquals(uriValue, "jalop:payload");
 
 		NodeList transforms = manifest.getElementsByTagName("Transform");
 		assertTrue(transforms.getLength() == 0);
 
 		Element digestMethod = (Element)manifest.getElementsByTagName("DigestMethod").item(0);
 		String digestValue = digestMethod.getAttribute("Algorithm");
 		assertEquals(digestValue, DigestMethod.SHA256);
 	}
 
 	@Test
 	public void testManifestForAuditAddsTransforms() throws Exception {
 
 		amt.setJID("JID");
 		amt.setLogger(logger);
 
 		JAXBElement<ApplicationMetadataType> appMeta = of.createApplicationMetadata(amt);
 		assertNotNull(appMeta);
 		JAXBContext jc = JAXBContext.newInstance(ApplicationMetadataType.class.getPackage().getName());
 		assertNotNull(jc);
 
 		doc = utils.marshal(jc, appMeta);
 
 		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, InputStream.class, MessageType.class);
 
 		method.setAccessible(true);
 		method.invoke(utils, doc, DMType.SHA256, new ByteArrayInputStream("String buffer".getBytes()), MessageType.JALP_AUDIT_MSG);
 
 		Element manifest = (Element)doc.getElementsByTagName("Manifest").item(0);
 		assertNotNull(manifest);
 
 		Element transform = (Element)manifest.getElementsByTagName("Transform").item(0);
 		assertNotNull(transform);
 		String transformValue = transform.getAttribute("Algorithm");
 		assertEquals(transformValue, "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
 	}
 
 	@Test(expected = JALException.class)
 	public void testManifestWithNullDMTypeThrowsException() throws Exception {
 
 		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, InputStream.class, MessageType.class);
 		method.setAccessible(true);
 
 		try{
 			method.invoke(utils, doc, null, new ByteArrayInputStream("String buffer".getBytes()), MessageType.JALP_LOG_MSG);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test(expected = JALException.class)
 	public void testManifestWithNullMessageTypeThrowsException() throws Exception {
 
 		Method method = JALUtils.class.getDeclaredMethod("createManifest", Document.class, DMType.class, InputStream.class, MessageType.class);
 		method.setAccessible(true);
 
 		try{
 			method.invoke(utils, doc, DMType.SHA256, new ByteArrayInputStream("String buffer".getBytes()), null);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test
 	public void testDigestWorks() throws Exception {
 
 		Method method = JALUtils.class.getDeclaredMethod("createDigest", InputStream.class, DMType.class);
 		method.setAccessible(true);
 
 		try{
 			byte[] buffer = (byte[]) method.invoke(utils, new ByteArrayInputStream("buffer".getBytes()), DMType.SHA256);
 
 			StringBuffer sb = new StringBuffer();
 			for(byte b : buffer) {
 				sb.append(String.format("%02x", b));
 			}
 			assertEquals("d0ca8c2ac0dab879bf27bfb58227b301db17f5ca716d065e7c884253d3ab99e4", sb.toString());
 
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test
 	public void testDigestWorksWithEvenSizedBuffer() throws Exception {
 
 		Method method = JALUtils.class.getDeclaredMethod("createDigest", InputStream.class, DMType.class);
 		method.setAccessible(true);
 
 		try{
 			File file = new File("test-input/evenBuffer");
 			byte[] buffer = (byte[]) method.invoke(utils, new FileInputStream(file), DMType.SHA256);
 
 			StringBuffer sb = new StringBuffer();
 			for(byte b : buffer) {
 				sb.append(String.format("%02x", b));
 			}
 			assertEquals("87d0289199665619210ecae87c08d49f05bf4566d5a5035d6c479f2a11fc6cde", sb.toString());
 
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test
 	public void testSignWorks() throws Exception {
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 		assertNotNull(prod);
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 		assertNotNull(doc);
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		method.invoke(utils, doc, prod);
 
 		Element signature = (Element)doc.getElementsByTagName("Signature").item(0);
 		assertNotNull(signature);
 
 		Element canMethod = (Element)signature.getElementsByTagName("CanonicalizationMethod").item(0);
 		String canMethodValue = canMethod.getAttribute("Algorithm");
 		assertEquals(canMethodValue, "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
 
 		Element sigMethod = (Element)signature.getElementsByTagName("SignatureMethod").item(0);
 		String sigMethodValue = sigMethod.getAttribute("Algorithm");
 		assertEquals(sigMethodValue, "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
 
 		String uri = "#xpointer(id(\'"+xml.getJID()+"\'))";
 		Element reference = (Element)signature.getElementsByTagName("Reference").item(0);
 		String uriValue = reference.getAttribute("URI");
 		assertEquals(uriValue, uri);
 
 		NodeList transforms = reference.getElementsByTagName("Transform");
 		assertEquals(transforms.getLength(), 2);
 
 		Element transform1 = (Element)transforms.item(0);
 		String transform1Value = transform1.getAttribute("Algorithm");
 		assertEquals(transform1Value, "http://www.w3.org/2000/09/xmldsig#enveloped-signature");
 
 		Element transform2 = (Element)transforms.item(1);
 		String transform2Value = transform2.getAttribute("Algorithm");
 		assertEquals(transform2Value, "http://www.w3.org/2001/10/xml-exc-c14n#WithComments");
 
 		Element digestMethod = (Element)reference.getElementsByTagName("DigestMethod").item(0);
 		String digestValue = digestMethod.getAttribute("Algorithm");
 		assertEquals(digestValue, DigestMethod.SHA256);
 	}
 
 	@Test
 	public void testSignAddsCertWhenNotNull() throws Exception {
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		CertificateFactory cf = CertificateFactory.getInstance("X.509");
 		InputStream in = new FileInputStream("test-input/cert");
 		X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
 		in.close();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), cert, DMType.SHA256, "/path/to/socket");
 		assertNotNull(prod);
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 		assertNotNull(doc);
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		method.invoke(utils, doc, prod);
 
 		Element x509Cert = (Element)doc.getElementsByTagName("X509Certificate").item(0);
 		assertNotNull(x509Cert);
 	}
 
 	@Test(expected = InvalidAlgorithmParameterException.class)
 	public void testSignThrowsExceptionWithBadAlgorithmParameter() throws Exception {
 
 		new Expectations() {
 			XMLSignatureFactory xmlSigFactory;
 			{
 				XMLSignatureFactory.getInstance(anyString); returns(xmlSigFactory);
 				xmlSigFactory.newTransform((String)any, (TransformParameterSpec)any); result = new InvalidAlgorithmParameterException();
 			}
 		};
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		try{
 			method.invoke(utils, doc, prod);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test(expected = NoSuchAlgorithmException.class)
 	public void testSignThrowsExceptionWithBadAlgorithm() throws Exception {
 
 		new Expectations() {
 			XMLSignatureFactory xmlSigFactory;
 			{
 				XMLSignatureFactory.getInstance(anyString); returns(xmlSigFactory);
 				xmlSigFactory.newTransform((String)any, (TransformParameterSpec)any); result = new NoSuchAlgorithmException();
 			}
 		};
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		try{
 			method.invoke(utils, doc, prod);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test(expected = KeyException.class)
 	public void testSignThrowsKeyException() throws Exception {
 
 		new Expectations() {
 			XMLSignatureFactory xmlSigFactory;
 			KeyInfoFactory keyInfoFactory;
 			{
 				XMLSignatureFactory.getInstance(anyString); returns(xmlSigFactory);
 				xmlSigFactory.newTransform(anyString, (TransformParameterSpec)null); returns(null);
 				xmlSigFactory.newTransform(anyString, (TransformParameterSpec)null); returns(null);
 				xmlSigFactory.newDigestMethod(anyString, null); returns(null);
 				xmlSigFactory.newReference(anyString, null, null, anyString, anyString); returns(null);
 				xmlSigFactory.newCanonicalizationMethod(anyString, (C14NMethodParameterSpec)null); returns(null);
 				xmlSigFactory.newSignatureMethod(anyString, null); returns(null);
 				xmlSigFactory.newSignedInfo(null, null, Collections.singletonList(null)); returns(null);
 				xmlSigFactory.getKeyInfoFactory(); returns(keyInfoFactory);
 			}
 		};
 		new NonStrictExpectations() {
 			@Capturing KeyInfoFactory k;
 			{
 				k.newKeyValue((PublicKey)any); result = new KeyException();
 			}
 		};
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		try{
 			method.invoke(utils, doc, prod);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test(expected = XMLSignatureException.class)
 	public void testSignThrowsXMLSignatureException() throws Exception {
 
 		new NonStrictExpectations() {
 			@Capturing XMLSignature x;
 			{
 				x.sign((XMLSignContext)any); result = new XMLSignatureException();
 			}
 		};
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		try{
 			method.invoke(utils, doc, prod);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 
 	@Test(expected = MarshalException.class)
 	public void testSignThrowsMarshalException() throws Exception {
 
 		new NonStrictExpectations() {
 			@Capturing XMLSignature x;
 			{
 				x.sign((XMLSignContext)any); result = new MarshalException();
 			}
 		};
 
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		ApplicationMetadataXML xml = prod.getXml();
 		assertNotNull(xml);
 		xml.prepareSend("Host Name", "Application Name");
 		doc = xml.marshal();
 
 		Method method = JALUtils.class.getDeclaredMethod("sign", Document.class, JALProducer.class);
 		method.setAccessible(true);
 		try{
 			method.invoke(utils, doc, prod);
 		} catch(InvocationTargetException ite) {
 			throw (Exception)ite.getCause();
 		}
 	}
 	
 	public static class MockUnixDomainSocketClient extends UnixDomainSocket {
 		@Mock
 		public void $init(String socketFile, int type) {
 			out = new UnixDomainSocketOutputStream();
 		}
 	}
 	
 	public static class MockUnixDomainSocketOutputStream {
 		@Mock
 		public void sendmsg(MessageHeader header) {}
 	}
 	
 	public static class MockUnixDomainSocket {
 		@Mocked UnixDomainSocketOutputStream out;
 		
 		@Mock
 		public void $init() {}
 		
 		@Mock
 		public OutputStream getOutputStream() {
 			UnixDomainSocketClient udsc;
 			try {
 				udsc = new UnixDomainSocketClient("socket", 1);
 			} catch (Exception e) {
 				return null;
 			}
 			out = udsc.new UnixDomainSocketOutputStream();
 			return out;
 		}
 	}
 	
 	@Test
 	public void testSendWorksWithNullBuffer() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		xml.prepareSend("hostname", "app_name");
 		Document doc = xml.marshal();
 		
 		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
 		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
 		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
 
 		new MockUp<SendUtils>() {
 			@Mock
 			void createAndSendHeaders(MessageType messageType, long dataLen, long metaLen, InputStream is, byte[] meta, String socketFile) throws Exception {}
 		};
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("send", new Class[]{Document.class, String.class, InputStream.class, long.class, MessageType.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{doc, (String)"/path/to/file", null, 0, MessageType.JALP_LOG_MSG});
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 	
 	@Test
 	public void testSendWorksWithNonNullBuffer() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		xml.prepareSend("hostname", "app_name");
 		Document doc = xml.marshal();
 		
 		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
 		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
 		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
 
 		new MockUp<SendUtils>() {
 			@Mock
 			void createAndSendHeaders(MessageType messageType, long dataLen, long metaLen, InputStream is, byte[] meta, String socketFile) throws Exception {}
 		};
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("send", new Class[]{Document.class, String.class, InputStream.class, long.class, MessageType.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{doc, (String)"/path/to/file", new ByteArrayInputStream("String buffer".getBytes()), "String buffer".length(), MessageType.JALP_LOG_MSG});
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 	
 	@Test(expected = JALException.class)
 	public void testSendThrowsJALExceptionWhenSocketIsNull() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		xml.prepareSend("hostname", "app_name");
 		Document doc = xml.marshal();
 		
 		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
 		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
 		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("send", new Class[]{Document.class, String.class,InputStream.class, long.class, MessageType.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{doc, null, new ByteArrayInputStream("String buffer".getBytes()), "String buffer".length(), MessageType.JALP_LOG_MSG});
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 	
 	@Test(expected = JALException.class)
 	public void testSendThrowsJALExceptionWhenSocketIsEmptyString() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		xml.prepareSend("hostname", "app_name");
 		Document doc = xml.marshal();
 		
 		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
 		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
 		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
 		
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("send", new Class[]{Document.class, String.class, InputStream.class, long.class, MessageType.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{doc, (String)"",new ByteArrayInputStream("String buffer".getBytes()), "String buffer".length(), MessageType.JALP_LOG_MSG});
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test(expected = JALException.class)
 	public void testSendThrowsJALExceptionWhenDocAndIsAreNull() throws Exception {
 		LoggerType logger = new LoggerType();
 		LoggerXML xml = new LoggerXML(logger);
 		xml.prepareSend("hostname", "app_name");
 
 		Mockit.setUpMock(UnixDomainSocketClient.class, new MockUnixDomainSocketClient());
 		Mockit.setUpMock(UnixDomainSocketOutputStream.class, new MockUnixDomainSocketOutputStream());
 		Mockit.setUpMock(UnixDomainSocket.class, new MockUnixDomainSocket());
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("send", new Class[]{Document.class, String.class,InputStream.class, long.class, MessageType.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{null, (String)"/path/to/file", null, 0, MessageType.JALP_LOG_MSG});
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test
 	public void testProcessXMLWorks() throws Exception {
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		Field messageType = JALProducer.class.getDeclaredField("messageType");
 		messageType.setAccessible(true);
 		messageType.set(prod, MessageType.JALP_LOG_MSG);
 
 		InputStream is = new ByteArrayInputStream("String buffer".getBytes());
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{prod, is});
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test
 	public void testProcessXMLCallsCreateManifest() throws Exception {
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		Field messageType = JALProducer.class.getDeclaredField("messageType");
 		messageType.setAccessible(true);
 		messageType.set(prod, MessageType.JALP_LOG_MSG);
 
 		InputStream is = new ByteArrayInputStream("String buffer".getBytes());
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			Document doc = (Document) method.invoke(null, new Object[]{prod, is});
 			NodeList manifestList = doc.getElementsByTagName("Manifest");
 			assertTrue(manifestList.getLength() > 0);
 			Node manifest = manifestList.item(0);
 			Node signature = doc.getElementsByTagName("Signature").item(0);
 			assertEquals(signature.getNextSibling(), manifest);
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test
 	public void testProcessXMLDoesntCallCreateManifest() throws Exception {
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, null, "/path/to/socket");
 
 		Field messageType = JALProducer.class.getDeclaredField("messageType");
 		messageType.setAccessible(true);
 		messageType.set(prod, MessageType.JALP_LOG_MSG);
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			Document doc = (Document) method.invoke(null, new Object[]{prod, null});
 			NodeList manifest = doc.getElementsByTagName("Manifest");
 			assertTrue(manifest.getLength() < 1);
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 
 	}
 
 	@Test
 	public void testProcessXMLCallsSign() throws Exception {
 		LoggerXML loggerXml = new LoggerXML(logger);
 		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
 		KeyPair kp = kpg.generateKeyPair();
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", kp.getPrivate(), kp.getPublic(), null, DMType.SHA256, "/path/to/socket");
 
 		Field messageType = JALProducer.class.getDeclaredField("messageType");
 		messageType.setAccessible(true);
 		messageType.set(prod, MessageType.JALP_LOG_MSG);
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			Document doc = (Document) method.invoke(null, new Object[]{prod, null});
 			NodeList signature = doc.getElementsByTagName("Signature");
 			assertTrue(signature.getLength() > 0);
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test
 	public void testProcessXMLDoesntCallSign() throws Exception {
 		LoggerXML loggerXml = new LoggerXML(logger);
 		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", null, null, null, DMType.SHA256, "/path/to/socket");
 
 		Field messageType = JALProducer.class.getDeclaredField("messageType");
 		messageType.setAccessible(true);
 		messageType.set(prod, MessageType.JALP_LOG_MSG);
 
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			Document doc = (Document) method.invoke(null, new Object[]{prod, null});
 			NodeList signature = doc.getElementsByTagName("Signature");
 			assertTrue(signature.getLength() < 1);
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test(expected = JALException.class)
 	public void testProcessXMLThrowsExceptionWithNullProducer() throws Exception {
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{null, null});
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}
 
 	@Test(expected = JALException.class)
 	public void testProcessXMLThrowsExceptionWithNullXML() throws Exception {
 		JALProducer prod = new JALProducer(null, "hostname", "app_name", null, null, null, DMType.SHA256, "/path/to/socket");
 		try {
 			Method method = JALUtils.class.getDeclaredMethod("processXML", new Class[]{JALProducer.class, InputStream.class});
 			method.setAccessible(true);
 			method.invoke(null, new Object[]{prod, null});
 		} catch (InvocationTargetException e) {
 			throw((Exception)e.getCause());
 		}
 	}

	@Test
	public void testProcessSendWorksWithFile() throws Exception {
		LoggerXML loggerXml = new LoggerXML(logger);
		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", null, null, null, DMType.SHA256, "/path/to/socket");

		Field messageType = JALProducer.class.getDeclaredField("messageType");
		messageType.setAccessible(true);
		messageType.set(prod, MessageType.JALP_LOG_MSG);

		new MockUp<JALUtils>() {
			@Mock
			void send(Document doc, String socketFile, InputStream is, long bufferLength, MessageType messageType) throws Exception {}
		};

		File file = new File("test-input/testBuffer");
		JALUtils.processSend(prod, file);
	}

	@Test
	public void testProcessSendWorksWithString() throws Exception {
		LoggerXML loggerXml = new LoggerXML(logger);
		JALProducer prod = new JALProducer(loggerXml, "hostname", "app_name", null, null, null, DMType.SHA256, "/path/to/socket");

		Field messageType = JALProducer.class.getDeclaredField("messageType");
		messageType.setAccessible(true);
		messageType.set(prod, MessageType.JALP_LOG_MSG);

		new MockUp<JALUtils>() {
			@Mock
			void send(Document doc, String socketFile, InputStream is, long bufferLength, MessageType messageType) throws Exception {}
		};

		JALUtils.processSend(prod, "String buffer");
	}
 }
