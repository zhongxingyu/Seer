 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.communication.xml;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 import org.s23m.cell.S23MKernel;
 import org.s23m.cell.Set;
 import org.s23m.cell.api.Query;
 import org.s23m.cell.communication.xml.model.schema.XmlSchemaConstants;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public class XmlSchemaFactoryTest extends TestCase {
 	
 	private static final String XML_SCHEMA_NAMESPACE = XmlSchemaConstants.XML_SCHEMA_URI;
 	
 	private static final String NAMESPACE_PREFIX = "xmlns:";
 
 	private static final String LOCAL_NAMESPACE_PREFIX = NamespaceConstants.S23M;
 	
 	private static final Predicate<Node> IS_ELEMENT = new Predicate<Node>() {
 		public boolean apply(Node input) {
 			return Node.ELEMENT_NODE == input.getNodeType();
 		}
 	};
 	
 	private static final Predicate<Node> ATTRIBUTE = new Predicate<Node>() {
 		public boolean apply(Node input) {
 			String tagName = input.getLocalName();
 			return "attribute".equals(tagName);
 		}
 	};
 	
 	private static final Predicate<Node> ELEMENT_OR_ATTRIBUTE = new Predicate<Node>() {
 		public boolean apply(Node input) {
 			String tagName = input.getLocalName();
 			return "element".equals(tagName) || "attribute".equals(tagName);
 		}
 	};
 	
 	private static final Predicate<Node> HAS_XSD_NAMESPACE = new Predicate<Node>() {
 		public boolean apply(Node input) {
 			return input.getNamespaceURI().equals(XML_SCHEMA_NAMESPACE);
 		}
 	};
 	
 	private static final Function<String, String> QUALIFY_NAME = new Function<String, String>() {
 		public String apply(String input) {
 			return LOCAL_NAMESPACE_PREFIX + ":" + input;
 		}
 	};
 	
 	private static final String XSD_STRING = "string";
 	
 	/* Selection of kernel types */
 	private final List<Set> kernelTypes;
 	
 	private final List<String> kernelTypeNames;
 	
 	private final XmlSchemaTerminology terminology;
 	
 	private Document document;
 	
 	public XmlSchemaFactoryTest() {
		S23MKernel.boot();
 		
 		kernelTypes = Arrays.asList(Query.edge, Query.graph, Query.vertex, Query.visibility);
 		
 		kernelTypeNames = createNames(kernelTypes);
 		
 		terminology = DefaultXmlSchemaTerminology.getInstance();
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		XmlSchemaFactory factory = new XmlSchemaFactory();
 		document = factory.createSchemaAsDocument(terminology);
 	}
 	
 	@Test
 	public void testBasicStructure() throws TransformerFactoryConfigurationError, TransformerException {
 		NodeList rootList = document.getChildNodes();
 		assertEquals(1, rootList.getLength());
 		
 		List<String> complexTypeNames = retrieveComplexTypeNames();
 		assertEquals("The list of complex type names must be unique", new HashSet<String>(complexTypeNames).size(), complexTypeNames.size());
 		
 		assertTrue("Expected: " + kernelTypeNames + "\nActual: " + complexTypeNames, complexTypeNames.containsAll(kernelTypeNames));
 	}
 	
 	@Test
 	public void testAllTermsAreBeingUsed() {
 		java.util.Set<String> allTerms = DefaultXmlSchemaTerminology.getAllTerms();
 		
 		java.util.Set<String> names = retrieveAllElementNames();
 		
 		java.util.Set<String> difference = Sets.difference(allTerms, names);
 		assertTrue("Not all terms are being used. Difference: " + difference, names.containsAll(allTerms));
 	}
 	
 	@Test
 	public void testAllDeclaredTypesReferToElements() {
 		Collection<Node> allElements = retrieveAllElements();
 		java.util.Set<String> ourDeclaredTypes = new HashSet<String>(allElements.size());
 		for (Node elementNode : allElements) {
 			NamedNodeMap attributes = elementNode.getAttributes();
 			Node typeNode = attributes.getNamedItem("type");
 			if (typeNode != null) {
 				ourDeclaredTypes.add(typeNode.getNodeValue());
 			}
 		}
 		
 		// ensure that only our types are kept
 		String xsdStringType = retrieveXsdStringType();
 		ourDeclaredTypes.remove(xsdStringType);
 		
 		java.util.Set<String> names = retrieveAllElementNames();
 		List<String> qualifiedNames = Lists.transform(new ArrayList<String>(names), QUALIFY_NAME);
 		
 		assertTrue("At least one type does not refer to a declared element.\nqualifiedNames: " + qualifiedNames + "\nourDeclaredTypes: " + ourDeclaredTypes, qualifiedNames.containsAll(ourDeclaredTypes));
 	}
 	
 	@Test
 	public void testOnlyXsdTypeInUseIsStringType() {
 		List<Node> declaredNodes = retrieveAllDeclaredNodes();
 		String xsdNamespacePrefix = retrieveXsdNamespacePrefix();
 		for (Node node : declaredNodes) {
 			NamedNodeMap attributes = node.getAttributes();
 			if (attributes != null) {
 				Node type = attributes.getNamedItem("type");
 				if (type != null) {
 					String qualifiedXsdTypeName = type.getNodeValue();
 					if (qualifiedXsdTypeName.startsWith(xsdNamespacePrefix)) {
 						String xsdTypeName = retrieveXsdStringType();
 						assertEquals(qualifiedXsdTypeName, xsdTypeName);
 					}
 				}
 			}
 		}
 	}
 	
 	@Test
 	public void testNoAttributesAreDeclared() {
 		List<Node> declaredNodes = retrieveAllDeclaredNodes();
 		for (Node node : declaredNodes) {
 			assertFalse("Node " + node + " is an attribute", Node.ATTRIBUTE_NODE == node.getNodeType());
 		}
 	}
 	
 	@Test
 	public void testRootElementExists() {
 		Collection<Node> allElements = retrieveAllElements();
 		
 		Predicate<Node> rootElementPredicate = new Predicate<Node>() {
 			@Override
 			public boolean apply(Node input) {
 				String nameAttribute = getName(input);
 				boolean nameMatches = terminology.artifactSet().equals(nameAttribute);
 				String nodeTagName = input.getLocalName();
 				boolean nodeTagMatches = "element".equals(nodeTagName); 
 				return nameMatches && nodeTagMatches;
 			}
 		};
 		
 		Node found = Iterables.find(allElements, rootElementPredicate, null);
 		assertNotNull("The root element was not found", found);
 	}
 	
 	@Test
 	public void testComplexTypeNamesAreUnique() {
 		Collection<Node> complexTypeNodes = getComplexTypeElements();
 		assertFalse(complexTypeNodes.isEmpty());
 		Collection<String> complexTypeNames = Collections2.transform(complexTypeNodes, new Function<Node, String>() {
 			public String apply(Node input) {
 				return getNameAttribute(input);
 			}
 		});
 		assertFalse(complexTypeNames.contains(null));
 		
 		assertEquals(complexTypeNames.size(), new HashSet<String>(complexTypeNames).size());
 	}
 	
 	@Test
 	public void testComplexTypeChildren() {
 		Collection<Node> elements = getComplexTypeElements();
 		
 		for (Node node: elements) {
 			Node firstChild = findFirstNonTextChildNode(node);
 			String tagName = firstChild.getLocalName();
 			if ("sequence".equals(tagName)) {
 				Collection<Node> children = retrieveAllElementChildren(firstChild);
 				checkNodesAreElementsOrAttributes(children);
 			} else if ("complexContent".equals(tagName)) {
 				// check extension child
 				Node extension = findFirstNonTextChildNode(firstChild);
 				assertEquals("extension", extension.getLocalName());
 				
 				if (extension.hasChildNodes()) {
 					// check its child is a sequence
 					Node sequence = findFirstNonTextChildNode(extension);
 					assertEquals("sequence", sequence.getLocalName());
 					
 					// check sequence children
 					Collection<Node> sequenceChildren = retrieveAllElementChildren(sequence);
 					checkNodesAreElementsOrAttributes(sequenceChildren);	
 				}
 			} else {
 				// ensure that all children are attributes (good enough for our purposes)
 				Collection<Node> children = retrieveAllElementChildren(firstChild);
 				checkNodesAreAttributes(children);
 			}
 		}
 	}
 	
 	@Test
 	public void testXsdNodeNamespaceIsUsedConsistently() {
 		// check that all XSD nodes (complexTypes, simpleTypes, elements) use the XSD namespace
 		Collection<Node> complexTypeElements = getComplexTypeElements();
 		Collection<Node> simpleTypeElements = retrieveAllElementsByTagName("simpleType");
 		Collection<Node> elementElements = retrieveAllElementsByTagName("element");
 		
 		Iterable<Node> allElements = Iterables.concat(complexTypeElements, simpleTypeElements, elementElements);
 		Iterable<Node> violations = Iterables.filter(allElements, Predicates.not(HAS_XSD_NAMESPACE));
 		assertFalse(violations.iterator().hasNext());
 	}
 
 	private void checkNodesAreAttributes(Collection<Node> children) {
 		Collection<Node> invalid = Collections2.filter(children, Predicates.not(ATTRIBUTE));
 		assertTrue("Non-element or non-attributes are present: " + invalid, invalid.isEmpty());
 	}
 	
 	private void checkNodesAreElementsOrAttributes(Collection<Node> children) {
 		Collection<Node> invalid = Collections2.filter(children, Predicates.not(ELEMENT_OR_ATTRIBUTE));
 		assertTrue("Non-element or non-attributes are present: " + invalid, invalid.isEmpty());
 	}
 
 	private Node findFirstNonTextChildNode(Node node) {
 		return Iterables.find(retrieveAllChildren(node), new Predicate<Node>() {
 			public boolean apply(Node input) {
 				return input.getNodeType() != Node.TEXT_NODE;
 			}
 		});
 	}
 
 	private Collection<Node> getComplexTypeElements() {
 		return retrieveAllElementsByTagName("complexType");
 	}
 	
 	private String retrieveXsdStringType() {
 		return retrieveXsdNamespacePrefix() + ":" + XSD_STRING;
 	}
 	
 	private java.util.Set<String> retrieveAllElementNames() {
 		Collection<Node> allElements = retrieveAllElements();
 		java.util.Set<String> names = new HashSet<String>(allElements.size());
 		for (Node elementNode : allElements) {
 			String nameAttribute = getNameAttribute(elementNode);
 			if (nameAttribute != null) {
 				names.add(nameAttribute);
 			}
 		}
 		return names;
 	}
 	
 	private String getNameAttribute(Node node) {
 		NamedNodeMap attributes = node.getAttributes();
 		Node nameNode = attributes.getNamedItem("name");
 		if (nameNode != null) {
 			return nameNode.getNodeValue();
 		} else {
 			return null;
 		}
 	}
 	
 	private Collection<Node> retrieveAllElements() {
 		Collection<Node> result = Collections2.filter(retrieveAllDeclaredNodes(), IS_ELEMENT);
 		assertFalse(result.isEmpty());
 		return result;
 	}
 	
 	private Collection<Node> retrieveAllElementsByTagName(final String tagName) {
 		Collection<Node> allElements = retrieveAllElements();
 		return Collections2.filter(allElements, new Predicate<Node>() {
 			public boolean apply(Node input) {
 				String localName = input.getLocalName();
 				return localName.equals(tagName);
 			}
 		}); 
 	}
 	
 	private List<Node> retrieveAllDeclaredNodes() {
 		return retrieveAllChildren(document);
 	}
 	
 	private Collection<Node> retrieveAllElementChildren(Node node) {
 		List<Node> children = retrieveAllChildren(node);
 		return Collections2.filter(children, IS_ELEMENT);
 	}
 	
 	private List<Node> retrieveAllChildren(Node node) {
 		// get all child nodes
 		List<Node> seen = new ArrayList<Node>();
 		NodeList list = node.getChildNodes();
 		for (int i = 0; i < list.getLength(); i++) {
 			// get child node
 			Node childNode = list.item(i);
 			
 			seen.add(childNode);
 			
 			// visit child node
 			List<Node> allChildren = retrieveAllChildren(childNode);
 			seen.addAll(allChildren);
 		}
 		return seen;
 	}
 
 	private Node retrieveRoot() {
 		NodeList rootList = document.getChildNodes();
 		assertEquals(1, rootList.getLength());
 		return rootList.item(0);
 	}
 	
 	private String retrieveXsdNamespacePrefix() {
 		Node root = retrieveRoot();	
 		Node xmlSchemaAttributeNode = findNodeWithValue(root.getAttributes(), XML_SCHEMA_NAMESPACE);
 		String namespaceName = xmlSchemaAttributeNode.getNodeName();
 		return namespaceName.substring(NAMESPACE_PREFIX.length());
 	}
 	
 	private List<Node> retrieveRootChildNodes() {
 		Node root = retrieveRoot();
 		return asList(root.getChildNodes());
 	}
 	
 	private List<String> retrieveComplexTypeNames() {
 		List<Node> schemaChildren = retrieveRootChildNodes();
 		String namespacePrefix = retrieveXsdNamespacePrefix();
 		
 		List<String> result = new ArrayList<String>();
 		for (Node node : schemaChildren) {
 			if (node.getNodeName().equals(namespacePrefix + ":complexType")) {
 				Node nameAttributeNode = node.getAttributes().getNamedItem("name");
 				String nameAttribute = nameAttributeNode.getNodeValue();
 				
 				result.add(nameAttribute);	
 			}
 		}
 		return result;
 	}
 	
 	private static List<String> createNames(List<Set> sets) {
 		List<String> result = new ArrayList<String>(sets.size());
 		for (Set set : sets) {
 			result.add(set.identity().name());
 		}
 		return result;
 	}
 	
 	private List<Node> asList(NodeList nodeList) {
 		List<Node> result = new ArrayList<Node>(nodeList.getLength());
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			result.add(nodeList.item(i));
 		}
 		return result;
 	}
 	
 	private String getName(Node elementNode) {
 		NamedNodeMap attributes = elementNode.getAttributes();
 		Node node = attributes.getNamedItem("name");
 		if (node == null) {
 			return null;
 		} else {
 			return node.getTextContent();
 		}
 	}
 	
 	private Node findNodeWithValue(NamedNodeMap map, String value) {
 		for (int i = 0; i < map.getLength(); i++) {
 			Node node = map.item(i);
 			if (value.equals(node.getNodeValue())) {
 				return node;
 			}
 		}
 		fail("Could not find node with value '" + value + "'");
 		return null;
 	}
 }
