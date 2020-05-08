 /**
  * Class containing common utility functions.
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
 
 import java.io.File;
 import java.io.StringWriter;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.crypto.dsig.CanonicalizationMethod;
 import javax.xml.crypto.dsig.DigestMethod;
 import javax.xml.crypto.dsig.Reference;
 import javax.xml.crypto.dsig.SignatureMethod;
 import javax.xml.crypto.dsig.SignedInfo;
 import javax.xml.crypto.dsig.Transform;
 import javax.xml.crypto.dsig.XMLSignature;
 import javax.xml.crypto.dsig.XMLSignatureFactory;
 import javax.xml.crypto.dsig.dom.DOMSignContext;
 import javax.xml.crypto.dsig.keyinfo.KeyInfo;
 import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
 import javax.xml.crypto.dsig.keyinfo.KeyValue;
 import javax.xml.crypto.dsig.keyinfo.X509Data;
 import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
 import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
 import javax.xml.crypto.dsig.spec.TransformParameterSpec;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import com.etsy.net.ConnectionHeader;
 import com.etsy.net.ConnectionHeader.MessageType;
 import com.etsy.net.JUDS;
 import com.etsy.net.UnixDomainSocket.UnixDomainSocketOutputStream;
 import com.etsy.net.UnixDomainSocketClient;
 import com.tresys.jalop.producer.ApplicationMetadataXML;
 import com.tresys.jalop.producer.JALProducer;
 import com.tresys.jalop.schemas.org.w3._2000._09.xmldsig_.DigestMethodType;
 import com.tresys.jalop.schemas.org.w3._2000._09.xmldsig_.ManifestType;
 import com.tresys.jalop.schemas.org.w3._2000._09.xmldsig_.ObjectFactory;
 import com.tresys.jalop.schemas.org.w3._2000._09.xmldsig_.ReferenceType;
 import com.tresys.jalop.schemas.org.w3._2000._09.xmldsig_.TransformType;
 import com.tresys.jalop.schemas.org.w3._2000._09.xmldsig_.TransformsType;
 
 public class JALUtils {
 	
	public static final String SCHEMA_LOCATION = "/com/tresys/jalop/applicationMetadataTypes.xsd";
 
 	/**
 	 * Starts the send process. Calls methods to set fields,
 	 * sign the document and create the manifest if applicable then continues with send.
 	 *
 	 * @param producer	the JALProducer
 	 * @param buffer	a String which is the buffer
 	 * @throws Exception
 	 */
 	public static void processSend(JALProducer producer, String buffer) throws Exception {
 
 		if(producer == null) {
 			throw new JALException("The JALProducer must not be null.");
 		}
 
 		ApplicationMetadataXML xml = producer.getXml();
 		if(xml == null) {
 			throw new JALException("The ApplicationMetadataXML must be set in the JALProducer.");
 		}
 
 		xml.prepareSend(producer.getHostName(), producer.getApplicationName());
 
 		Document doc = xml.marshal();
 
 		if(producer.getDigestMethod() != null && buffer != null && !"".equals(buffer)) {
 			createManifest(doc, producer.getDigestMethod(), buffer, producer.getMessageType());
 		}
 
 		if(producer.getPrivateKey() != null && producer.getPublicKey() != null) {
 			sign(doc, producer);
 		}
 
 		if(producer.getDigestMethod() != null && buffer != null && !"".equals(buffer)) {
 			//Move the manifest to the end of the document
 			Node manifest = doc.getElementsByTagName("Manifest").item(0);
 			doc.getDocumentElement().appendChild(manifest);
 		}
 
 		send(doc, producer.getSocketFile(), buffer, producer.getMessageType());
 	}
 
 	/**
 	 * Builds a document and marshals the xml into the document. 
 	 * This also validates the xml against the given schema.
 	 * 
 	 * @param jc		the JAXBContext of the correct class
 	 * @param element	the JAXBElement created by ObjectFactory for the correct class
 	 * @return	the marshaled document
 	 * @throws Exception 
 	 */
 	public static Document marshal(JAXBContext jc, JAXBElement element) throws Exception {
 		Marshaller m = jc.createMarshaller();
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 
 		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(JALUtils.class.getResource(SCHEMA_LOCATION));
 		m.setSchema(schema);
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setNamespaceAware(true);
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document document = db.newDocument();
 
 		m.marshal(element, document);
 
 		return document;
 	}
 
 	/**
 	 * Adds a signature to the given document
 	 *
 	 * @param doc		the marshaled document to be signed
 	 * @param producer	the JALProducer
 	 * @throws Exception
 	 */
 	private static void sign(Document doc, JALProducer producer) throws Exception {
 		ApplicationMetadataXML xml = producer.getXml();
 		String jid = xml.getJID();
 
 		DOMSignContext domSignContext = new DOMSignContext(producer.getPrivateKey(), doc.getDocumentElement());
 
 		//This allows the xpointer below to resolve JID as an ID
 		domSignContext.setIdAttributeNS(doc.getDocumentElement(), null, "JID");
 
 		XMLSignatureFactory xmlSigFactory = XMLSignatureFactory.getInstance("DOM");
 
 		List<Transform> transformList = new ArrayList<Transform>();
 
 		transformList.add(xmlSigFactory.newTransform(
 				Transform.ENVELOPED,
 				(TransformParameterSpec) null));
 
 		transformList.add(xmlSigFactory.newTransform(
 				"http://www.w3.org/2001/10/xml-exc-c14n#WithComments",
 				(TransformParameterSpec) null));
 
 		String uri = "#xpointer(id(\'"+jid+"\'))";
 		Reference reference = xmlSigFactory.newReference(uri,
 			xmlSigFactory.newDigestMethod(DigestMethod.SHA256, null),
 		    transformList, null, null);
 
 		CanonicalizationMethod canonicalizationMethod = xmlSigFactory.newCanonicalizationMethod(
 				CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,
 				(C14NMethodParameterSpec) null);
 
 		SignatureMethod signatureMethod = xmlSigFactory.newSignatureMethod(
 				"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256",
 				null);
 
 		SignedInfo signedInfo = xmlSigFactory.newSignedInfo(
 			canonicalizationMethod,
 			signatureMethod,
 			Collections.singletonList(reference));
 
 		KeyInfoFactory keyInfoFactory = xmlSigFactory.getKeyInfoFactory();
 
 		KeyValue keyValue = keyInfoFactory.newKeyValue(producer.getPublicKey());
 
 		List keyInfoList = new ArrayList();
 		keyInfoList.add(keyValue);
 
 		if(producer.getCertificate() != null) {
 			X509Certificate cert = producer.getCertificate();
 			List content = new ArrayList();
 			content.add(cert.getSubjectX500Principal().getName());
 
 			X509IssuerSerial issuerSerial = keyInfoFactory.newX509IssuerSerial(
 					cert.getIssuerX500Principal().getName(),
 					cert.getSerialNumber());
 			content.add(issuerSerial);
 
 			content.add(cert);
 
 			X509Data xd = keyInfoFactory.newX509Data(content);
 			keyInfoList.add(xd);
 		}
 
 		KeyInfo keyInfo = keyInfoFactory.newKeyInfo(keyInfoList);
 
 		XMLSignature signature = xmlSigFactory.newXMLSignature(signedInfo, keyInfo);
 
 		signature.sign(domSignContext);
 	}
 
 	/**
 	 * An enum for the different types of digest methods that can be used
 	 */
 	public enum DMType {
 		SHA256 (DigestMethod.SHA256),
 		SHA512 (DigestMethod.SHA512),
 		SHA384 ("http://www.w3.org/2001/04/xmldsig-more#sha384");
 
 		private String digestMethod;
 
 		DMType(String digestMethod) {
 			this.digestMethod = digestMethod;
 		}
 
 		private String digestMethod() { return digestMethod; }
 	}
 
 	/**
 	 * Creates a manifest document for the payload, marshals it and
 	 * appends it to the original document.
 	 *
 	 * @param doc			the signed Document
 	 * @param dmType		the DMType which will determine the digest method used
 	 * @param buffer		a String which is the buffer
 	 * @param messageType	the MessageType
 	 * @throws Exception
 	 */
 	private static void createManifest(Document doc, DMType dmType, String buffer, MessageType messageType) throws Exception {
 
 		if(dmType == null || messageType == null) {
 			throw new JALException("DMType and MessageType must be set in the JALProducer first.");
 		}
 
 		ManifestType manifest = new ManifestType();
 
 		ReferenceType ref = new ReferenceType();
 		ref.setURI("jalop:payload");
 
 		DigestMethodType digestMethod = new DigestMethodType();
 		digestMethod.setAlgorithm(dmType.digestMethod());
 
 		ref.setDigestMethod(digestMethod);
 		ref.setDigestValue(buffer.getBytes());
 
 		if(MessageType.JALP_AUDIT_MSG.equals(messageType)) {
 			TransformType transform = new TransformType();
 			transform.setAlgorithm("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");
 			TransformsType transforms = new TransformsType();
 			transforms.getTransform().add(transform);
 			ref.setTransforms(transforms);
 		}
 
 		manifest.getReference().add(ref);
 
 		ObjectFactory of = new ObjectFactory();
 		JAXBElement<ManifestType> man = of.createManifest(manifest);
 		JAXBContext jc = JAXBContext.newInstance(ManifestType.class.getPackage().getName());
 		Document manifestDocument = marshal(jc, man);
 
 		doc.getDocumentElement().appendChild(doc.importNode(manifestDocument.getFirstChild(),true));
 	}
 
 	/**
 	 * Creates a connection header and uses a UnixDomainSocketOutputStream to
 	 * send the buffer and application metadata to the local store with sendmsg.
 	 *
 	 * @param doc			the marshaled xml doc
 	 * @param socketFile	the socket file
 	 * @param buffer		a String which is the buffer to send
 	 * @param messageType	the type of message to send
 	 * @throws Exception
 	 */
 	private static void send(Document doc, String socketFile, String buffer, MessageType messageType) throws Exception {
 		if(socketFile != null && !"".equals(socketFile)) {
 			UnixDomainSocketClient socket = new UnixDomainSocketClient(socketFile,
 					JUDS.SOCK_STREAM);
 
 			UnixDomainSocketOutputStream out = (UnixDomainSocketOutputStream)socket.getOutputStream();
 
 			TransformerFactory transFactory = TransformerFactory.newInstance();
 			Transformer trans = transFactory.newTransformer();
 			StringWriter writer = new StringWriter();
 			trans.transform(new DOMSource(doc), new StreamResult(writer));
 			String appMeta = writer.toString();
 
 			int bufferLength = 0;
 			byte[] bufferBytes = null;
 			if(buffer != null) {
 				bufferBytes = buffer.getBytes();
 				bufferLength = bufferBytes.length;
 			}
 
 			int appMetaLength = 0;
 			byte[] appMetaBytes = null;
 			if(appMeta != null) {
 				appMetaBytes = appMeta.getBytes();
 				appMetaLength = appMetaBytes.length;
 			}
 
 			ConnectionHeader connectionHeader = new ConnectionHeader(1, messageType, bufferLength, appMetaLength);
 
 			out.sendmsg(bufferBytes, appMetaBytes, connectionHeader);
 
 			socket.close();
 
 		} else {
 			throw new JALException("Error in JALUtils.send - socketFile path must be set in producer");
 		}
 	}
 
 	/**
 	 * Creates a calendar with the current date and time to set the timestamp
 	 * 
 	 * @return	XMLGregorianCalendar with the current date
 	 * @throws Exception
 	 */
 	public static XMLGregorianCalendar getCurrentTime() throws Exception {
 		GregorianCalendar gc = new GregorianCalendar();
 		XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
 		return xmlCal;
 	}
 }
