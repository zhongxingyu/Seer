 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.tinydom;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.swtxml.definition.IAttributeDefinition;
 import com.swtxml.definition.INamespaceDefinition;
 import com.swtxml.definition.INamespaceResolver;
 import com.swtxml.definition.ITagDefinition;
 import com.swtxml.definition.ITagScope;
 import com.swtxml.util.lang.CollectionUtils;
 import com.swtxml.util.parser.ParseException;
 
 public class TinyDomSaxHandler extends DefaultHandler {
 
 	private final String xmlFilename;
 	private Locator locator;
 	private Tag root;
 	private final INamespaceResolver namespaceResolver;
 	private final Map<String, INamespaceDefinition> namespaces = new HashMap<String, INamespaceDefinition>();
 
 	TinyDomSaxHandler(INamespaceResolver namespaceResolver, String xmlFilename) {
 		this.namespaceResolver = namespaceResolver;
 		this.xmlFilename = xmlFilename;
 	}
 
 	private Stack<Tag> parserStack = new Stack<Tag>();
 
 	@Override
 	public void startElement(String namespace, String localName, String qName, Attributes attributes)
 			throws SAXException {
 		INamespaceDefinition namespaceDefinition = getNamespace(namespace);
 		ITagDefinition tagDefinition = namespaceDefinition.getTag(localName);
 		if (tagDefinition == null) {
			throw new ParseException("Unknown tag \"" + localName + "\" for namespace \""
					+ namespace + "\", allowed are: "
					+ CollectionUtils.sortedToString(namespaceDefinition.getTagNames()));
 		}
 		Map<INamespaceDefinition, Map<IAttributeDefinition, String>> attributeMap = processAttributes(
 				namespaceDefinition, tagDefinition, attributes);
 
 		Tag tag = new Tag(namespaceDefinition, tagDefinition, attributeMap,
 				parserStack.isEmpty() ? null : parserStack.peek(), getLocationInfo());
 
 		ITagDefinition parentTag = tag.getParent() != null ? tag.getParent().getTagDefinition()
 				: ITagDefinition.ROOT;
 		if (tagDefinition instanceof ITagScope
 				&& !((ITagScope) tagDefinition).isAllowedIn(parentTag)) {
 			throw new ParseException("Tag " + tag.getName() + " is not allowed in "
 					+ parentTag.getName());
 		}
 
 		if (root == null) {
 			this.root = tag;
 		}
 		parserStack.push(tag);
 	}
 
 	private Map<INamespaceDefinition, Map<IAttributeDefinition, String>> processAttributes(
 			INamespaceDefinition tagNamespace, ITagDefinition tagDefinition, Attributes attributes) {
 
 		Map<INamespaceDefinition, Map<IAttributeDefinition, String>> attributeNsMap = new HashMap<INamespaceDefinition, Map<IAttributeDefinition, String>>();
 		for (int i = 0; i < attributes.getLength(); i++) {
 			String uri = attributes.getURI(i);
 			INamespaceDefinition attributeNamespace = !StringUtils.isEmpty(uri) ? getNamespace(uri)
 					: tagNamespace;
 			Map<IAttributeDefinition, String> attributeMap = attributeNsMap.get(attributeNamespace);
 			if (attributeMap == null) {
 				attributeMap = new HashMap<IAttributeDefinition, String>();
 				attributeNsMap.put(attributeNamespace, attributeMap);
 			}
 			String name = attributes.getLocalName(i);
 			String value = attributes.getValue(i);
 			IAttributeDefinition attributeDefinition;
 			if (attributeNamespace.equals(tagNamespace)) {
 				attributeDefinition = tagDefinition.getAttribute(name);
 			} else {
 				attributeDefinition = attributeNamespace.getForeignAttribute(name);
 				if (attributeDefinition instanceof ITagScope
 						&& !((ITagScope) attributeDefinition).isAllowedIn(tagDefinition)) {
 					throw new ParseException("Attribute " + attributes.getQName(i)
 							+ " is not allowed for tag \"" + tagDefinition.getName() + "\"");
 				}
 			}
 
 			if (attributeDefinition == null) {
 				throw new ParseException("Unknown attribute \"" + attributes.getQName(i)
 						+ "\" for tag \"" + tagDefinition.getName() + "\" (available are: "
 						+ CollectionUtils.sortedToString(tagDefinition.getAttributeNames()) + ")");
 			}
 			attributeMap.put(attributeDefinition, value);
 
 		}
 		if (attributeNsMap.isEmpty()) {
 			return Collections.emptyMap();
 		}
 		return attributeNsMap;
 	}
 
 	private INamespaceDefinition getNamespace(String namespaceUri) {
 		INamespaceDefinition namespace = namespaces.get(namespaceUri);
 		if (namespace == null) {
 			namespace = namespaceResolver.resolveNamespace(namespaceUri);
 			if (namespace == null) {
 				throw new ParseException("Unknown namespace: " + namespaceUri);
 			}
 			namespaces.put(namespaceUri, namespace);
 		}
 		return namespace;
 	}
 
 	public String getLocationInfo() {
 		return xmlFilename + " [line " + locator.getLineNumber() + "] ";
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		parserStack.pop();
 	}
 
 	@Override
 	public void setDocumentLocator(Locator locator) {
 		this.locator = locator;
 	}
 
 	public Tag getRoot() {
 		return root;
 	}
 }
