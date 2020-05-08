 /*******************************************************************************
  * Copyright (c) 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.ui.wizards.generators;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swordfish.tooling.ui.helper.ConsumerProjectInformationUtil;
 import org.eclipse.swordfish.tooling.ui.wizards.generators.JaxWsClientReferenceGenerator;
 import org.eclipse.swordfish.tooling.ui.wizards.generators.data.ConsumerProjectInformation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class JaxWsGeneratorsTest extends TestCase {
 	private static final String SPRING_CONSUMER_CONFIG = "/resources/org/eclipse/swordfish/tooling/ui/wizards/generators/jaxws-consumer-payment.xml";
 	private static final String PROJECT_NAME = "Dummy";
 	private static final String SERVICE_NS = "http://super.hasi.org";
 	private static final String SEP = JaxWsClientReferenceGenerator.PROPERTY_SEPARATOR;
 	private static final String EXPECTED_CLIENT_RESULT = "<jaxws:client" + SEP + "xmlns:serviceNamespace=\""
 			+ SERVICE_NS + "\"" + SEP + "address=\"nmr:PaymentProcessingService\"" + SEP
 			+ "id=\"PaymentProcessingClient\"" + SEP
 			+ "serviceClass=\"org.eclipse.swordfish.samples.paymentprocessing.PaymentProcessing\"" + SEP
 			+ "serviceName=\"serviceNamespace:PaymentProcessingService\"" + SEP
 			+ "xmlns:jaxws=\"http://cxf.apache.org/jaxws\"/>\n";
 
	private static final String EXPECTED_SPRING_RESULT = "\t\t<spring:property name=\"paymentProcessingClient\" ref=\"PaymentProcessingClient\"/>\n";
 
 	private JaxWsClientReferenceGenerator clientGenerator;
 	private JaxWsSpringReferenceGenerator springGenerator;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		clientGenerator = new JaxWsClientReferenceGenerator();
 		springGenerator = new JaxWsSpringReferenceGenerator();
 	}
 
 	/**
 	 * Verify the guard that checks whether there is information required for generation of client references
 	 */
 	public void testClientCheckForGenerationContent() throws Exception {
 		ConsumerProjectInformation cpi = new ConsumerProjectInformation(PROJECT_NAME);
 		assertEquals("No information available", false, clientGenerator.hasGenerationContent(cpi));
 
 		cpi.setServiceNamespace("");
 		assertEquals("Namespace is empty string", false, clientGenerator.hasGenerationContent(cpi));
 
 		cpi.setServiceNamespace(SERVICE_NS);
 		assertEquals("Only namespace available", false, clientGenerator.hasGenerationContent(cpi));
 
 		cpi.setJaxWsClientElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG).getDocumentElement());
 		assertEquals("Not a client element", false, clientGenerator.hasGenerationContent(cpi));
 
 		cpi.setJaxWsClientElement(getJaxWsElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG)));
 		assertEquals("Client element found", true, clientGenerator.hasGenerationContent(cpi));
 	}
 
 	/**
 	 * Verify that the client generator does not break if null is passed in
 	 */
 	public void testClientNullConsumerProjectInfo() {
 		assertEquals("", clientGenerator.generate((List<ConsumerProjectInformation>) null));
 		assertEquals("", clientGenerator.generate((ConsumerProjectInformation) null));
 
 		List<ConsumerProjectInformation> list = new ArrayList<ConsumerProjectInformation>();
 		assertEquals("", clientGenerator.generate(list));
 	}
 
 	/**
 	 * Verify that generation of single entries and lists of entries work.
 	 */
 	public void testClientGenerate() throws Exception {
 		ConsumerProjectInformation cpi = new ConsumerProjectInformation(PROJECT_NAME);
 		cpi.setJaxWsClientElement(getJaxWsElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG)));
 		cpi.setServiceNamespace(SERVICE_NS);
 
 		assertEquals(EXPECTED_CLIENT_RESULT, clientGenerator.generate(cpi));
 
 		List<ConsumerProjectInformation> list = new ArrayList<ConsumerProjectInformation>();
 		list.add(cpi);
 		assertEquals(EXPECTED_CLIENT_RESULT, clientGenerator.generate(list));
 
 		list.add(cpi);
 		assertEquals(EXPECTED_CLIENT_RESULT + EXPECTED_CLIENT_RESULT, clientGenerator.generate(list));
 	}
 
 	// =================================================================================================
 	
 	/**
 	 * Verify the guard that checks whether the jaxws client element is not null or cannot be found for spring client
 	 * injection
 	 */
 	public void testSpringCheckForMissingGenerationContent() throws Exception {
 		ConsumerProjectInformation cpi = new ConsumerProjectInformation(PROJECT_NAME);
 		assertEquals("No information available", false, springGenerator.hasGenerationContent(cpi));
 
 		cpi.setJaxWsClientElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG).getDocumentElement());
 		assertEquals("Not a client element", false, springGenerator.hasGenerationContent(cpi));
 	}
 
 	/**
 	 * Verify the guard that checks whether the jaxws client element has an "id" property that is not empty and not null
 	 */
 	public void testSpringCheckForMissingId() throws Exception {
 		ConsumerProjectInformation cpi = new ConsumerProjectInformation(PROJECT_NAME);
 
 		Element el = (Element) getJaxWsElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG));
 		cpi.setJaxWsClientElement(el);
 		assertEquals("Client id property has a value", true, springGenerator.hasGenerationContent(cpi));
 
 		el.setAttribute("id", "");
 		assertEquals("Client id property has empty string value", false, springGenerator.hasGenerationContent(cpi));
 
 		el.removeAttribute("id");
 		assertEquals("Client id property does not exist", false, springGenerator.hasGenerationContent(cpi));
 
 		el.setAttribute("ID", "Hasi");
 		assertEquals("Client ID property is not recognized", false, springGenerator.hasGenerationContent(cpi));
 	}
 
 	/**
 	 * Verify the guard that checks whether the jaxws client element has an "id" property is found if it is there
 	 */
 	public void testSpringCheckForGenerationContent() throws Exception {
 		ConsumerProjectInformation cpi = new ConsumerProjectInformation(PROJECT_NAME);
 
 		cpi.setJaxWsClientElement(getJaxWsElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG)));
 		assertEquals("Client element found", true, springGenerator.hasGenerationContent(cpi));
 	}
 
 	/**
 	 * Verify that the spring generator does not break if null is passed in
 	 */
 	public void testSpringNullConsumerProjectInfo() {
 		assertEquals("", springGenerator.generate((List<ConsumerProjectInformation>) null));
 		assertEquals("", springGenerator.generate((ConsumerProjectInformation) null));
 
 		List<ConsumerProjectInformation> list = new ArrayList<ConsumerProjectInformation>();
 		assertEquals("", springGenerator.generate(list));
 	}
 
 	/**
 	 * Verify generation of a single ConsumerProjectInformation as well as a list
 	 */
 	public void testSpringGenerate() throws Exception {
 		ConsumerProjectInformation cpi = new ConsumerProjectInformation(PROJECT_NAME);
 		cpi.setJaxWsClientElement(getJaxWsElement(getJaxWsConsumerDocument(SPRING_CONSUMER_CONFIG)));
 		cpi.setServiceNamespace(SERVICE_NS);
 
 		assertEquals(EXPECTED_SPRING_RESULT, springGenerator.generate(cpi));
 
 		List<ConsumerProjectInformation> list = new ArrayList<ConsumerProjectInformation>();
 		list.add(cpi);
 		assertEquals(JaxWsSpringReferenceGenerator.XML_OPEN_COMMENT + EXPECTED_SPRING_RESULT
 				+ JaxWsSpringReferenceGenerator.XML_CLOSE_COMMENT, springGenerator.generate(list));
 
 		list.add(cpi);
 		assertEquals(JaxWsSpringReferenceGenerator.XML_OPEN_COMMENT + EXPECTED_SPRING_RESULT + EXPECTED_SPRING_RESULT
 				+ JaxWsSpringReferenceGenerator.XML_CLOSE_COMMENT, springGenerator.generate(list));
 	}
 
 	private Document getJaxWsConsumerDocument(String path) throws ParserConfigurationException, SAXException,
 			IOException {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setNamespaceAware(true);
 		DocumentBuilder db = dbf.newDocumentBuilder();
 
 		return db.parse(getClass().getResource(path).toString());
 	}
 
 	private Element getJaxWsElement(Document doc) throws ParserConfigurationException, SAXException, IOException {
 		NodeList nl1 = doc.getDocumentElement()
 				.getElementsByTagName(ConsumerProjectInformationUtil.JAXWS_CLIENT_TAG_NAME);
 		return (Element) nl1.item(0);
 	}
 }
