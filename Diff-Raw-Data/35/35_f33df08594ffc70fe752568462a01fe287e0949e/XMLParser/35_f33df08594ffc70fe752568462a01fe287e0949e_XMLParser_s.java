 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.jatmibroker.core.conf;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.xml.XMLConstants;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Class to create a parser and parse an XML file
  */
 public class XMLParser {
 
 	private DefaultHandler handler;
 	private SAXParser saxParser;
 	private Schema schema;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param handler
 	 *            - DefaultHandler for the SAX parser
 	 */
 	public XMLParser(DefaultHandler handler, String xsdFilename)
 			throws ConfigurationException {
 		this.handler = handler;
 		create(xsdFilename);
 	}
 
 	/**
 	 * Create the SAX parser
 	 */
 	private void create(String xsdFilename) throws ConfigurationException {
 		try {
 			// Obtain a new instance of a SAXParserFactory.
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			factory.setNamespaceAware(true);
 			factory.setValidating(true);
 
 			SchemaFactory schemaFactory = SchemaFactory
 					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 			URL resource = Thread.currentThread().getContextClassLoader()
 					.getResource(xsdFilename);
 			schema = schemaFactory.newSchema(resource);
 			factory.setSchema(schema);
 
 			saxParser = factory.newSAXParser();
		} catch (Throwable t) {
			throw new ConfigurationException("Could not create a SAXParser: ",
					t);
 		}
 	}
 
 	/**
 	 * Parser a File
 	 * 
 	 * @param envXML
 	 *            - File
 	 */
 	public boolean parse(String env) throws ConfigurationException {
 		InputStream resource = Thread.currentThread().getContextClassLoader()
 				.getResourceAsStream(env);
 		if (resource != null) {
 			try {
 				schema.newValidator().validate(new StreamSource(resource));
 				saxParser.parse(Thread.currentThread().getContextClassLoader()
 						.getResourceAsStream(env), handler);
 				return true;
 			} catch (Throwable t) {
 				throw new ConfigurationException("Errors parse : " + env, t);
 			}
 		}
 		return false;
 	}
 }
