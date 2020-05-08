 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.internal.core;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.validators.core.IValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class ValidatorDefinitionsContainer {
 
 	private Map fValidatorTypeToValidatorMap;
 
 	private List fValidatorList;
 
 //	private List fInvalidValidatorList;
 
 	public ValidatorDefinitionsContainer() {
 		fValidatorTypeToValidatorMap = new HashMap(10);
 //		fInvalidValidatorList = new ArrayList(10);
 		fValidatorList = new ArrayList(10);
 	}
 
 	public void addValidator(IValidator validator) {
 		if (!fValidatorList.contains(validator)) {
 			IValidatorType InterpreterInstallType = validator
 					.getValidatorType();
 			List validatorList = (List) fValidatorTypeToValidatorMap
 					.get(InterpreterInstallType);
 			if (validatorList == null) {
 				validatorList = new ArrayList(3);
 				fValidatorTypeToValidatorMap.put(InterpreterInstallType,
 						validatorList);
 			}
 			validatorList.add(validator);
 //			if (!validator.isValidatorValid()) {
 //				fInvalidValidatorList.add(validator);
 //			}
 			fValidatorList.add(validator);
 		}
 	}
 
 	public void addValidatorList(List validatorList) {
 		Iterator iterator = validatorList.iterator();
 		while (iterator.hasNext()) {
 			IValidator validator = (IValidator) iterator.next();
 			addValidator(validator);
 		}
 	}
 
 	public Map getValidateTypeToValidatorsMap() {
 		return fValidatorTypeToValidatorMap;
 	}
 
 	public List getValidatorList() {
 		return fValidatorList;
 	}
 
 	public List getValidatorList(String nature) {
 		List res = new ArrayList(fValidatorList.size());
 		for (Iterator iter = fValidatorList.iterator(); iter.hasNext();) {
 			IValidator validator = (IValidator) iter.next();
 			String sharp = "#"; //$NON-NLS-1$
 			String nature2 = validator.getValidatorType().getNature();
 			if (nature2.equals(nature) || nature2.equals(sharp)) {
 				res.add(validator);
 			}
 		}
 		return res;
 	}
 
	public List getValidValidatorsList() {
 		List validators = getValidatorList();
 		List resultList = new ArrayList(validators.size());
 		resultList.addAll(validators);
 //		resultList.removeAll(fInvalidValidatorList);
 		return resultList;
 	}
 
	public List getValidValidatorsList(String nature) {
 		List Interpreters = getValidatorList(nature);
 		List resultList = new ArrayList(Interpreters.size());
 		resultList.addAll(Interpreters);
 //		resultList.removeAll(fInvalidValidatorList);
 		return resultList;
 	}
 
 	public String getAsXML() throws ParserConfigurationException, IOException,
 			TransformerException {
 
 		// Create the Document and the top-level node
 		Document doc = ValidatorsCore.getDocument();
 		Element config = doc.createElement("validatorSettings"); //$NON-NLS-1$
 		doc.appendChild(config);
 
 		// Create a node for each install type represented in this container
 		Set validatorTypeSet = getValidateTypeToValidatorsMap().keySet();
 		Iterator keyIterator = validatorTypeSet.iterator();
 		while (keyIterator.hasNext()) {
 			IValidatorType validatorType = (IValidatorType) keyIterator.next();
 			if (validatorType.isConfigurable()) {
 				Element valiatorTypeElement = validatorTypeAsElement(doc,
 						validatorType);
 				config.appendChild(valiatorTypeElement);
 			}
 		}
 
 		// Serialize the Document and return the resulting String
 		return ValidatorsCore.serializeDocument(doc);
 	}
 
 	private Element validatorTypeAsElement(Document doc,
 			IValidatorType validatorType) {
 
 		// Create a node for the Interpreter type and set its 'id' attribute
 		Element element = doc.createElement("validatorType"); //$NON-NLS-1$
 		element.setAttribute("id", validatorType.getID()); //$NON-NLS-1$
 
 		// For each Interpreter of the specified type, create a subordinate node
 		// for it
 		List validatorList = (List) getValidateTypeToValidatorsMap().get(
 				validatorType);
 		Iterator validatorIterator = validatorList.iterator();
 		while (validatorIterator.hasNext()) {
 			IValidator validator = (IValidator) validatorIterator.next();
 			Element validatorElement = validatorAsElement(doc, validator);
 			element.appendChild(validatorElement);
 		}
 
 		return element;
 	}
 
 	private Element validatorAsElement(Document doc, IValidator validator) {
 
 		// Create the node for the Interpreter and set its 'id' & 'name'
 		// attributes
 		Element element = doc.createElement("validator"); //$NON-NLS-1$
 		element.setAttribute("id", validator.getID()); //$NON-NLS-1$
 
 		validator.storeTo(doc, element);
 
 		return element;
 	}
 
 	public static ValidatorDefinitionsContainer parseXMLIntoContainer(
 			InputStream inputStream) throws IOException {
 		ValidatorDefinitionsContainer container = new ValidatorDefinitionsContainer();
 		parseXMLIntoContainer(inputStream, container);
 		return container;
 	}
 
 	public static void parseXMLIntoContainer(InputStream inputStream,
 			ValidatorDefinitionsContainer container) throws IOException {
 
 		// Wrapper the stream for efficient parsing
 		InputStream stream = new BufferedInputStream(inputStream);
 
 		// Do the parsing and obtain the top-level node
 		Element config = null;
 		try {
 			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
 					.newDocumentBuilder();
 			parser.setErrorHandler(new DefaultHandler());
 			config = parser.parse(new InputSource(stream)).getDocumentElement();
 		} catch (SAXException e) {
 			throw new IOException(ValidatorMessages.ValidatorRuntime_badFormat);
 		} catch (ParserConfigurationException e) {
 			stream.close();
 			throw new IOException(ValidatorMessages.ValidatorRuntime_badFormat);
 		} finally {
 			stream.close();
 		}
 
 		// If the top-level node wasn't what we expected, bail out
 		if (!config.getNodeName().equalsIgnoreCase("validatorSettings")) { //$NON-NLS-1$
 			throw new IOException(ValidatorMessages.ValidatorRuntime_badFormat);
 		}
 
 		// Traverse the parsed structure and populate the InterpreterType to
 		// Interpreter Map
 		NodeList list = config.getChildNodes();
 		int length = list.getLength();
 		for (int i = 0; i < length; ++i) {
 			Node node = list.item(i);
 			short type = node.getNodeType();
 			if (type == Node.ELEMENT_NODE) {
 				Element validatorTypeElement = (Element) node;
 				if (validatorTypeElement.getNodeName().equalsIgnoreCase(
 						"validatorType")) { //$NON-NLS-1$
 					populateValidatorTypes(validatorTypeElement, container);
 				}
 			}
 		}
 	}
 
 	/**
 	 * For the specified Interpreter type node, parse all subordinate
 	 * Interpreter definitions and add them to the specified container.
 	 */
 	private static void populateValidatorTypes(Element validatorTypeElement,
 			ValidatorDefinitionsContainer container) {
 
 		// Retrieve the 'id' attribute and the corresponding Interpreter type
 		// object
 		String id = validatorTypeElement.getAttribute("id"); //$NON-NLS-1$
 		IValidatorType validatorType = ValidatorManager
 				.getValidatorTypeFromID(id);
 		if (validatorType != null) {
 
 			// For each Interpreter child node, populate the container with a
 			// subordinate node
 			NodeList validatorNodeList = validatorTypeElement.getChildNodes();
 			for (int i = 0; i < validatorNodeList.getLength(); ++i) {
 				Node InterpreterNode = validatorNodeList.item(i);
 				short type = InterpreterNode.getNodeType();
 				if (type == Node.ELEMENT_NODE) {
 					Element InterpreterElement = (Element) InterpreterNode;
 					if (InterpreterElement.getNodeName().equalsIgnoreCase(
 							"validator")) { //$NON-NLS-1$
 						populateValidatorForType(validatorType,
 								InterpreterElement, container);
 					}
 				}
 			}
 		} else {
 			if (DLTKCore.DEBUG) {
 				System.err.println("Interpreter type element with unknown id."); //$NON-NLS-1$
 			}
 		}
 	}
 
 	/**
 	 * Parse the specified Interpreter node, create a InterpreterStandin for it,
 	 * and add this to the specified container.
 	 */
 	private static void populateValidatorForType(
 			IValidatorType interpreterType, Element validatorElement,
 			ValidatorDefinitionsContainer container) {
 		String id = validatorElement.getAttribute("id"); //$NON-NLS-1$
 		if (id != null) {
 			try {
 				IValidator validator = interpreterType.createValidatorFrom(id,
 						validatorElement);
 				container.addValidator(validator);
 			} catch (IOException e) {
 				DLTKCore.getDefault().getLog().log(
 						new Status(0, ValidatorsCore.PLUGIN_ID, 0,
 								ValidatorMessages.ValidatorDefinitionsContainer_failedToLoadValidatorFromXml, null));
 			}
 		} else {
 			if (DLTKCore.DEBUG) {
 				System.err
 						.println("id attribute missing from Interpreter element specification."); //$NON-NLS-1$
 			}
 		}
 	}
 
 	/**
 	 * Removes the Interpreter from this container.
 	 * 
 	 * @param Interpreter
 	 *            Interpreter intall
 	 */
 	public void removeValidator(IValidator Interpreter) {
 		fValidatorList.remove(Interpreter);
 //		fInvalidValidatorList.remove(Interpreter);
 		List list = (List) fValidatorTypeToValidatorMap.get(Interpreter
 				.getValidatorType());
 		if (list != null) {
 			list.remove(Interpreter);
 		}
 	}
 
 }
