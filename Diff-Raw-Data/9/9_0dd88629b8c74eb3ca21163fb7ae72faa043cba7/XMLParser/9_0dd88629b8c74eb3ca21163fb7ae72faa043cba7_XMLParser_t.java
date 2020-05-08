 /**
  * 
  * Copyright 2002 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *****************************************************************************/
 
 
 package org.nchelp.meteor.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.xml.security.Init;
 import org.apache.xml.security.utils.XMLUtils;
 import org.nchelp.meteor.util.exception.ParsingException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  *   Class with utilities for working with XML
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Dec 17, 2001
  */
 public class XMLParser {
 	/**
 	 * Parse the XML that is passed in as a String
 	 * @param message String containing the XML to parse.
 	 * @return Document
 	 * @throws ParsingException if there are any errors parsing
 	 * the String XML document
 	 */
 	public static Document parseXML(String message) 
 	                         throws ParsingException {
 		DocumentBuilderFactory factory = null;
 		DocumentBuilder db = null;
 		try {
 			factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true);
 
 			db = factory.newDocumentBuilder();
 			InputSource is = new InputSource(
 			                   new StringReader(message));
 			return db.parse(is);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ParsingException(e);
 		}
 	}
 
 	/**
 	 * Parse the input from the given InputStream
 	 * @param iStream InputStream to read
 	 * @return Document
 	 * @throws ParsingException if there are any errors parsing the document
 	 */
 	public static Document parseXML(InputStream iStream) 
 	                               throws ParsingException {
 		DocumentBuilderFactory factory = null;
 		DocumentBuilder db = null;
 		try {
 			factory = DocumentBuilderFactory.newInstance();
 			factory.setNamespaceAware(true);
 
 			db = factory.newDocumentBuilder();
 			InputSource is = new InputSource(iStream);
 			return db.parse(is);
 		} catch (Exception e) {
 			throw new ParsingException(e);
 		}
 	}
 
 
 	/**
 	 * Get the first Node in the document that has the name nodeName
 	 * @param doc  Document containing the node
 	 * @param nodeName String name of the node to look for.
 	 * @return Node
 	 */
 	public static Node getNode(Document doc, String nodeName) {
 		if (doc == null) {
 			return null;
 		}
 
 		NodeList nl = doc.getElementsByTagName(nodeName);
 
 		if (nl.getLength() < 1) {
 			return null;
 		}
 
 		// This is a major hole if there are two tags in 
 		// the document with the same node name.  This only 
 		// cares about the first one it finds.
 		// In the case of the XML for HPC as of 9/27/01 it 
 		// doesn't matter because there are no duplicate tags
 		Node n = nl.item(0);
 
 		Node attrNode = n.getFirstChild();
 		if (attrNode == null) {
 			return null;
 		}
 		
 		return attrNode;
 	}
 	
 	/**
 	 * Look up the value of one node within the document.  
 	 * If there is more than one node within this document
 	 * with the same name, then this will only return the
 	 * value of the first node.
 	 * @param doc Document containing the node in question
 	 * @param nodeName String containing the name of the node
 	 * @return String
 	 */
 	public static String getNodeValue(Document doc, String nodeName) {
 		Node n = getNode(doc, nodeName);
 		if (n == null) {
 			return null;
 		}
 		return n.getNodeValue();
 	}
 	
 	/**
 	 * Serialize the org.w3c.dom.Document that 
 	 * is passed in as the parameter to a String.  
 	 * The resulting string will be written as 
 	 * C14N With Comments.
 	 * @param doc Document object to be serialized
 	 * @return String
 	 */
 	public static String XMLToString(Document doc) {
 
 		/*  This document might be signed and it might not
 		 *  We really don't care.  Use the c14n outputter from 
 		 *  the xml security package
 		 */
 		Init.init();
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		XMLUtils.outputDOMc14nWithComments(doc, os);
 		
 		return os.toString();
 
 	}
 
 	/**
 	 * Create an org.w3c.dom.Document with the node paramter 
 	 * as the root element
 	 * @param n Node object that is to be the root of the new Document
 	 * @return Document
 	 * @throws ParsingException if there were any 
 	 * errors creating the Document
 	 */
 	public static Document createDocument(Node n) throws ParsingException {
 		Document doc = XMLParser.createDocument();
 		Node newNode = doc.importNode(n, true);
 
 		doc.appendChild(newNode);
 		return doc;
 	}
 	
 	/**
 	 * Create an empty org.w3c.dom.Document object
 	 * @return Document
 	 * @throws ParsingException if there were any exceptions 
 	 * reported by the XML Parser
 	 */
 	public static Document createDocument() throws ParsingException {
 		Document doc = null;
 		try {
 			doc =
 				DocumentBuilderFactory
 					.newInstance()
 					.newDocumentBuilder()
 					.newDocument();
 		} catch (Exception e) {
 			throw new ParsingException(e);
 		}
 
 		return doc;
 	}
 	
 	/**
	 * Remove the default namespace from the XML Document.
 	 * According to the XSLT 1.0 spec, a document
 	 * cannot have a default namespace other than
 	 * the one normally defined for xslt.
 	 * So, until version 2.0 of the XSLT spec is 
 	 * published and incorporated into Xalan,
 	 * We have to strip out the default namespace
 	 * @param xml
 	 * @return String
 	 */
 	public static String removeDefaultNamespace(String xml){
 		if(xml == null) return xml;
 		
 		String nameSpace = " xmlns=\"http://schemas.pescxml.org\"";
 		
 		int pos = xml.indexOf(nameSpace);
 		if(pos < 0) return xml;
 		
 		xml = xml.substring(0, pos) + 
 		      xml.substring(pos + nameSpace.length());
 		return xml;
 	}
 	
 		
 }
 
