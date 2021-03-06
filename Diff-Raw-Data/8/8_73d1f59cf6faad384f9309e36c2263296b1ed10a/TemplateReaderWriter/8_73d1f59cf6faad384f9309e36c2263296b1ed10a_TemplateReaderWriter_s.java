 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jface.text.templates.persistence;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import org.eclipse.jface.text.Assert;
 import org.eclipse.jface.text.templates.Template;
 
 /**
  * <code>TemplateSet</code> manages a collection of templates and makes them
  * persistent.
  * 
  * @since 3.0
  */
 public class TemplateReaderWriter {
 
 	private static final String TEMPLATE_ROOT = "templates"; //$NON-NLS-1$
 	private static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
 	private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
 	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
 	private static final String DESCRIPTION_ATTRIBUTE= "description"; //$NON-NLS-1$
 	private static final String CONTEXT_ATTRIBUTE= "context"; //$NON-NLS-1$
 	private static final String ENABLED_ATTRIBUTE= "enabled"; //$NON-NLS-1$
 	private static final String DELETED_ATTRIBUTE= "deleted"; //$NON-NLS-1$
 	
 	/**
 	 * Reads templates from an XML reader and adds them to the templates.
 	 * 
 	 * @param reader the XML reader to read templates from
 	 * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
 	 * @throws SAXException if the XML is not valid
 	 * @throws IOException if reading from the stream fails 
 	 */	
 	public TemplatePersistenceData[] read(Reader reader) throws SAXException, IOException {
 		return read(reader, null);
 	}
 	
 	/**
 	 * Reads templates from an XML stream and adds them to the templates.
 	 * 
 	 * @param reader the XML reader to read templates from
 	 * @param bundle a resource bundle to use for translating the read templates, or <code>null</code> if no translation should occur
 	 * @return the read templates, encapsulated in instances of <code>TemplatePersistenceData</code>
 	 * @throws SAXException if the XML is not valid
 	 * @throws IOException if reading from the stream fails 
 	 */	
 	public TemplatePersistenceData[] read(Reader reader, ResourceBundle bundle) throws SAXException, IOException {
 		
 		try {
 			Collection templates= new ArrayList();
 			Set ids= new HashSet();
 			
 			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
 			DocumentBuilder parser= factory.newDocumentBuilder();		
 			Document document= parser.parse(new InputSource(reader));
 			
 			NodeList elements= document.getElementsByTagName(TEMPLATE_ELEMENT);
 			
 			int count= elements.getLength();
 			for (int i= 0; i != count; i++) {
 				Node node= elements.item(i);					
 				NamedNodeMap attributes= node.getAttributes();
 
 				if (attributes == null)
 					continue;
 
 				String id= getStringValue(attributes, ID_ATTRIBUTE, null);
 				if (id != null && ids.contains(id))
 					throw new SAXException(TemplateMessages.getString("TemplateReaderWriter.duplicate.id")); //$NON-NLS-1$
 				
 				boolean deleted = getBooleanValue(attributes, DELETED_ATTRIBUTE, false);
 				
 				String name= getStringValue(attributes, NAME_ATTRIBUTE);
 
 				String description= getStringValue(attributes, DESCRIPTION_ATTRIBUTE, ""); //$NON-NLS-1$
 				description= translateString(description, bundle);
 				
 				String context= getStringValue(attributes, CONTEXT_ATTRIBUTE);
 
 				if (name == null || context == null)
 					throw new SAXException(TemplateMessages.getString("TemplateReaderWriter.error.missing_attribute")); //$NON-NLS-1$
 
 				boolean enabled = getBooleanValue(attributes, ENABLED_ATTRIBUTE, true);
 				
 				StringBuffer buffer= new StringBuffer();
 				NodeList children= node.getChildNodes();
 				for (int j= 0; j != children.getLength(); j++) {
 					String value= children.item(j).getNodeValue();
 					if (value != null)
 						buffer.append(value);
 				}
 				String pattern= buffer.toString();
 				pattern= translateString(pattern, bundle);
 
 				Template template= new Template(name, description, context, pattern);
 				TemplatePersistenceData data= new TemplatePersistenceData(template, enabled, id);
 				data.setDeleted(deleted);
 				
 				templates.add(data);
 			}
 			
 			return (TemplatePersistenceData[]) templates.toArray(new TemplatePersistenceData[templates.size()]);
 			
 		} catch (ParserConfigurationException e) {
 			Assert.isTrue(false);
 		}
 		
 		return null; // dummy
 	}
 	
 	/**
 	 * Saves the templates as XML.
 	 * 
 	 * @param templates the templates to save
 	 * @param writer the writer to write the templates to in XML
 	 * @throws IOException if writing the templates fails 
 	 */
 	public void save(TemplatePersistenceData[] templates, Writer writer) throws IOException {
 		try {
 			DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder= factory.newDocumentBuilder();		
 			Document document= builder.newDocument();
 
 			Node root= document.createElement(TEMPLATE_ROOT); //$NON-NLS-1$
 			document.appendChild(root);
 			
 			for (int i= 0; i < templates.length; i++) {
 				TemplatePersistenceData data= templates[i];
 				Template template= data.getTemplate();
 				
 				Node node= document.createElement(TEMPLATE_ELEMENT);
 				root.appendChild(node);
 				
 				NamedNodeMap attributes= node.getAttributes();
 				
 				String id= data.getId();
 				if (id != null) {
 					Attr idAttr= document.createAttribute(ID_ATTRIBUTE);
 					idAttr.setValue(id);
 					attributes.setNamedItem(idAttr);
 				}
 				
 				if (template != null) {
 					Attr name= document.createAttribute(NAME_ATTRIBUTE);
 					name.setValue(template.getName());
 					attributes.setNamedItem(name);
 				}
 	
 				if (template != null) {
 					Attr description= document.createAttribute(DESCRIPTION_ATTRIBUTE);
 					description.setValue(template.getDescription());
 					attributes.setNamedItem(description);
 				}
 	
 				if (template != null) {
 					Attr context= document.createAttribute(CONTEXT_ATTRIBUTE);
 					context.setValue(template.getContextTypeId());
 					attributes.setNamedItem(context);
 				}
 				
 				Attr enabled= document.createAttribute(ENABLED_ATTRIBUTE);
 				enabled.setValue(data.isEnabled() ? Boolean.toString(true) : Boolean.toString(false)); //$NON-NLS-1$ //$NON-NLS-2$
 				attributes.setNamedItem(enabled);
 				
 				Attr deleted= document.createAttribute(DELETED_ATTRIBUTE);
 				deleted.setValue(data.isDeleted() ? Boolean.toString(true) : Boolean.toString(false)); //$NON-NLS-1$ //$NON-NLS-2$
 				attributes.setNamedItem(deleted);
 				
 				if (template != null) {
 					Text pattern= document.createTextNode(template.getPattern());
 					node.appendChild(pattern);			
 				}
 			}		
 			
 			
 			Transformer transformer=TransformerFactory.newInstance().newTransformer();
 			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
 			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
 			DOMSource source = new DOMSource(document);
 			StreamResult result = new StreamResult(writer);
 
 			transformer.transform(source, result);
 
 		} catch (ParserConfigurationException e) {
 			Assert.isTrue(false);
 		} catch (TransformerException e) {
 			if (e.getException() instanceof IOException)
 				throw (IOException) e.getException();
 			Assert.isTrue(false);
 		}		
 	}
 
 	private boolean getBooleanValue(NamedNodeMap attributes, String attribute, boolean defaultValue) throws SAXException {
 		Node enabledNode= attributes.getNamedItem(attribute);
 		if (enabledNode == null)
 			return defaultValue;
 		else if (enabledNode.getNodeValue().equals(Boolean.toString(true)))
 			return true;
 		else if (enabledNode.getNodeValue().equals(Boolean.toString(false)))
 			return false;
 		else
 			throw new SAXException(TemplateMessages.getString("TemplateReaderWriter.error.illegal_boolean_attribute")); //$NON-NLS-1$
 	}
 	
 	private String getStringValue(NamedNodeMap attributes, String name) throws SAXException {
 		String val= getStringValue(attributes, name, null);
 		if (val == null)
 			throw new SAXException(TemplateMessages.getString("TemplateReaderWriter.error.missing_attribute")); //$NON-NLS-1$
 		return val;
 	}
 
 	private String getStringValue(NamedNodeMap attributes, String name, String defaultValue) {
 		Node node= attributes.getNamedItem(name);
 		return node == null	? defaultValue : node.getNodeValue();
 	}
 
 	private String translateString(String str, ResourceBundle bundle) {
 		if (bundle == null)
 			return str;
 		
 		int idx= str.indexOf('%');
 		if (idx == -1) {
 			return str;
 		}
 		StringBuffer buf= new StringBuffer();
 		int k= 0;
 		while (idx != -1) {
 			buf.append(str.substring(k, idx));
 			for (k= idx + 1; k < str.length() && !Character.isWhitespace(str.charAt(k)); k++) {
 				// loop
 			}
 			String key= str.substring(idx + 1, k);
 			buf.append(getBundleString(key, bundle));
 			idx= str.indexOf('%', k);
 		}
 		buf.append(str.substring(k));
 		return buf.toString();
 	}
 	
 	private String getBundleString(String key, ResourceBundle bundle) {
 		if (bundle != null) {
 			try {
 				return bundle.getString(key);
 			} catch (MissingResourceException e) {
 				return '!' + key + '!';
 			}
 		} else
 			return TemplateMessages.getString(key); // default messages
 	}
 }
 
