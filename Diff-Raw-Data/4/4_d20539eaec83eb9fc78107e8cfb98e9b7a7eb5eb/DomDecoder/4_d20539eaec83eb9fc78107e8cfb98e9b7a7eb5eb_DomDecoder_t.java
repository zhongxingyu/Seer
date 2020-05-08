 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.xmlinrdf;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.swows.graph.events.DynamicGraph;
 import org.swows.graph.events.GraphUpdate;
 import org.swows.graph.events.Listener;
 import org.swows.runnable.RunnableContext;
 import org.swows.util.GraphUtils;
 import org.swows.util.Utils;
 import org.swows.vocabulary.SWI;
 import org.swows.vocabulary.XML;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Comment;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ProcessingInstruction;
 import org.w3c.dom.Text;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.sparql.util.NodeComparator;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.util.iterator.Map1;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 public class DomDecoder implements Listener, RunnableContext, EventListener {
 	
 	private static String VOID_NAMESPACE = "http://www.swows.org/xml/no-namespace";
     private static final Logger logger = Logger.getLogger(DomDecoder.class);
     private static final Comparator<Node> nodeComparator = new NodeComparator();
 
 	private DocumentReceiver docReceiver;
 	private DOMImplementation domImplementation;
 	private DynamicGraph graph;
 //	private Set<DomEventListener> domEventListeners;
 	private Map<String, Set<DomEventListener>> domEventListeners;
 	
 	private Document document;
 	private RunnableContext updatesContext;
 	private Map<Node, Set<org.w3c.dom.Node>> graph2domNodeMapping = new HashMap<Node, Set<org.w3c.dom.Node>>();
 	private Map<org.w3c.dom.Node, Node> dom2graphNodeMapping = new HashMap<org.w3c.dom.Node, Node>();
 	private Node docRootNode;
 	
 	private Map<Element,NavigableMap<Node, Vector<org.w3c.dom.Node>>> dom2orderedByKeyChildren = new HashMap<Element, NavigableMap<Node,Vector<org.w3c.dom.Node>>>();
 	private Map<Element,Map<org.w3c.dom.Node, Node>> dom2childrenKeys = new HashMap<Element, Map<org.w3c.dom.Node, Node>>();
 	private Set<Element> dom2descendingOrder = new HashSet<Element>();
 	private Map<Element, Node> dom2childrenOrderProperty = new HashMap<Element, Node>();
 	
 	private static EventManager DEFAULT_EVENT_MANAGER =
 			new EventManager() {
 				public void removeEventListener(
 						Node targetNode,
 						org.w3c.dom.Node target, String type,
 						EventListener listener, boolean useCapture) {
 					if (target instanceof EventTarget)
 						((EventTarget) target).removeEventListener(type, listener, useCapture);
 				}
 				public void addEventListener(
 						Node targetNode,
 						org.w3c.dom.Node target, String type,
 						EventListener listener, boolean useCapture) {
 					if (target instanceof EventTarget)
 						((EventTarget) target).addEventListener(type, listener, useCapture);
 				}
 			};
 	private EventManager eventManager = DEFAULT_EVENT_MANAGER;
 	
 //	private Map<String, Set<Element>> eventType2elements = new HashMap<String, Set<Element>>();
 //	private Map<Element, Set<String>> element2eventTypes = new HashMap<Element, Set<String>>();
 	
 	public void addDomEventListener(String eventType, DomEventListener l) {
 		synchronized(this) {
 			if (domEventListeners == null)
 				domEventListeners = new HashMap<String, Set<DomEventListener>>();
 		}
 		synchronized(domEventListeners) {
 			Set<DomEventListener> domEventListenersForType = domEventListeners.get(eventType);
 			if (domEventListenersForType == null) {
 				domEventListenersForType = new HashSet<DomEventListener>();
 				domEventListeners.put(eventType, domEventListenersForType);
 			}
 			domEventListenersForType.add(l);
 		}
 	}
 	
 	public void removeDomEventListener(String eventType, DomEventListener l) {
 		if (domEventListeners != null) {
 			synchronized(domEventListeners) {
 				domEventListeners.remove(l);
 			}
 		}
 	}
 
 	public synchronized void handleEvent(Event evt) {
 		logger.debug("In DOM decoder handling event " + evt + " of type " + evt.getType());
 //		System.out.println("In DOM decoder handling event " + evt + " of type " + evt.getType());
 		org.w3c.dom.Node eventCurrentTargetDomNode = (org.w3c.dom.Node) evt.getCurrentTarget();
 		Node eventCurrentTargetGraphNode = dom2graphNodeMapping.get(eventCurrentTargetDomNode);
 		org.w3c.dom.Node eventTargetDomNode = (org.w3c.dom.Node) evt.getTarget();
 		Node eventTargetGraphNode = dom2graphNodeMapping.get(eventTargetDomNode);
 		if (domEventListeners != null) {
 			synchronized (domEventListeners) {
 				Set<DomEventListener> domEventListenersForType = domEventListeners.get(evt.getType());
 				for (DomEventListener l : domEventListenersForType) {
 					logger.debug("Sending to " + l + " the event " + evt);
 					l.handleEvent(evt, eventCurrentTargetGraphNode, eventTargetGraphNode);
 				}
 			}
 		}
 	}
 
 	public static Document decode(DynamicGraph graph, Node docRootNode) {
 		try {
 			return decode(graph, docRootNode, DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0"));
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static Document decodeOne(DynamicGraph graph) {
 		return decodeAll(graph).next();
 	}
 
 	public static ExtendedIterator<Document> decodeAll(final DynamicGraph graph) {
 		return graph
 				.find(Node.ANY, RDF.type.asNode(), XML.Document.asNode())
 				.mapWith(new Map1<Triple, Document>() {
 					public Document map1(Triple triple) {
 						return decode(graph, triple.getSubject());
 					}
 				});
 	}
 
 	private static String qNameElement(final Graph graph, final Node elementNode) {
 		return
 				GraphUtils
 				.getSingleValueProperty(
 						graph,
 						GraphUtils.getSingleValueProperty(graph, elementNode, RDF.type.asNode()),
 						XML.nodeName.asNode() )
 				.getLiteralLexicalForm();
 	}
 	
 	private static String namespaceElement(final Graph graph, final Node elementNode) {
 		try {
 			return
 				graph.find(
 						GraphUtils.getSingleValueProperty(graph, elementNode, RDF.type.asNode()),
 						XML.namespace.asNode(),
 						Node.ANY)
 				.next().getObject().getURI();
 		} catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 	
 	private static String qNameAttr(final Graph graph, final Node elementNode) {
 		return
 				GraphUtils
 				.getSingleValueProperty(
 						graph,
 						elementNode,
 						XML.nodeName.asNode() )
 				.getLiteralLexicalForm();
 	}
 	
 	private static String namespaceAttr(final Graph graph, final Node elementNode) {
 		try {
 			return
 				graph.find(
 						elementNode,
 						XML.namespace.asNode(),
 						Node.ANY)
 				.next().getObject().getURI();
 		} catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 	
 	private static String value(final Graph graph, final Node elementNode) {
 		try {
 			return
 				graph.find(elementNode, XML.nodeValue.asNode(), Node.ANY)
 				.next().getObject().getLiteralLexicalForm();
 		} catch (NoSuchElementException e) {
 			return null;
 		}
 	}
 	
 	private Text decodeText(Graph graph, Node elementNode) {
 		return document.createTextNode(value(graph, elementNode));
 	}
 	
 	@SuppressWarnings("unused")
 	private Comment decodeComment(Graph graph, Node elementNode) {
 		return document.createComment(value(graph, elementNode));
 	}
 	
 	@SuppressWarnings("unused")
 	private ProcessingInstruction decodeProcessingInstruction(Graph graph, Node elementNode) {
 		return document.createProcessingInstruction(
 				qNameElement(graph, elementNode),
 				value(graph, elementNode) );
 	}
 	
 	private Attr decodeAttr(Node elementNode) {
 		String nsUri = namespaceAttr(graph, elementNode);
 		if (nsUri == null)
 			throw new RuntimeException("Namespace not found for attribute " + elementNode + " in graph " + graph);
 		return
 				(nsUri.equals(VOID_NAMESPACE))
 					? document.createAttribute( qNameAttr(graph, elementNode) )
 					: document.createAttributeNS(
 							namespaceAttr(graph, elementNode),
 							qNameAttr(graph, elementNode) );
 	}
 	
 	private void removeAttr(Element element, Node elementNode) {
 		String nsUri = namespaceAttr(graph, elementNode);
 		if (nsUri == null)
 			throw new RuntimeException("Namespace not found for attribute " + elementNode + " in graph " + graph);
 		if (nsUri.equals(VOID_NAMESPACE))
 			element.removeAttribute( qNameAttr(graph, elementNode) );
 		else
 			element.removeAttributeNS(
 						namespaceAttr(graph, elementNode),
 						qNameAttr(graph, elementNode) );
 	}
 
 	public String md5(String md5) {
 	   try {
 	        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
 	        byte[] array = md.digest(md5.getBytes());
 	        StringBuffer sb = new StringBuffer();
 	        for (int i = 0; i < array.length; ++i) {
 	          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
 	       }
 	        return sb.toString();
 	    } catch (java.security.NoSuchAlgorithmException e) {
 	    }
 	    return null;
 	}
 	
 	private void reorder(
 //			Set<org.w3c.dom.Node> noKeyChildren,
 			boolean ascendingOrder,
 			Element element, 
 			NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren ) {
 //		if (ascendingOrder) {
 //			for (org.w3c.dom.Node child : noKeyChildren) {
 //				element.appendChild(child);
 //			}
 //		}
 		for (Node orderKeyNode : ascendingOrder ? orderedByKeyChildren.keySet() : orderedByKeyChildren.descendingKeySet() ) {
 			for (org.w3c.dom.Node child : orderedByKeyChildren.get(orderKeyNode)) {
 				element.appendChild(child);
 			}
 		}
 //		if (!ascendingOrder) {
 //			for (org.w3c.dom.Node child : noKeyChildren) {
 //				element.appendChild(child);
 //			}
 //		}
 	}
 	
 	private int elementCount = 0;
 	private synchronized String newElementId() {
 		return "n_" + Integer.toHexString(elementCount++);
 	}
 	
 	private void putChildByKey(Element parent, org.w3c.dom.Node node, Node orderKeyNode) {
 
 		Map<org.w3c.dom.Node, Node> childrenKeys = dom2childrenKeys.get(parent);
 		NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren = dom2orderedByKeyChildren.get(parent);
 		
 		Vector<org.w3c.dom.Node> sameKeyBag = orderedByKeyChildren.get(orderKeyNode);
 		if (sameKeyBag == null) {
 			sameKeyBag = new Vector<org.w3c.dom.Node>();
 			orderedByKeyChildren.put(orderKeyNode, sameKeyBag);
 		}
 		sameKeyBag.add(node);
 
 		childrenKeys.put(node, orderKeyNode);
 		
 	}
 	
 	private void removeChildByKey(Element parent, org.w3c.dom.Node node) {
 
 		Map<org.w3c.dom.Node, Node> childrenKeys = dom2childrenKeys.get(parent);
 		NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren = dom2orderedByKeyChildren.get(parent);
 		
 		Node prevOrderKeyNode = childrenKeys.get(node);
 		if (prevOrderKeyNode != null) {
 			Vector<org.w3c.dom.Node> oldKeyBag = orderedByKeyChildren.get(prevOrderKeyNode);
 			oldKeyBag.remove(node);
 			if (oldKeyBag.isEmpty())
 				orderedByKeyChildren.remove(prevOrderKeyNode);
 		}
 
 		childrenKeys.remove(node);
 
 	}
 	
 	private void updateChildByKey(Element parent, org.w3c.dom.Node node, Node orderKeyNode) {
 
 		Map<org.w3c.dom.Node, Node> childrenKeys = dom2childrenKeys.get(parent);
 		NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren = dom2orderedByKeyChildren.get(parent);
 		
 		Node prevOrderKeyNode = childrenKeys.get(node);
 		if (prevOrderKeyNode != null) {
 			Vector<org.w3c.dom.Node> oldKeyBag = orderedByKeyChildren.get(prevOrderKeyNode);
 			oldKeyBag.remove(node);
 			if (oldKeyBag.isEmpty())
 				orderedByKeyChildren.remove(prevOrderKeyNode);
 		}
 
 		Vector<org.w3c.dom.Node> sameKeyBag = orderedByKeyChildren.get(orderKeyNode);
 		if (sameKeyBag == null) {
 			sameKeyBag = new Vector<org.w3c.dom.Node>();
 			orderedByKeyChildren.put(orderKeyNode, sameKeyBag);
 		}
 		sameKeyBag.add(node);
 
 		childrenKeys.put(node, orderKeyNode);
 		
 	}
 	
 	private void setupElementChildren(Node elementNode, Element element) {
 		Node childrenOrderProperty =
 				GraphUtils.getSingleValueOptProperty(graph, elementNode, XML.childrenOrderedBy.asNode());
 		if (childrenOrderProperty != null) 
 			dom2childrenOrderProperty.put(element, childrenOrderProperty);
 		
 		boolean ascendingOrder =
 				!graph.contains(elementNode, XML.childrenOrderType.asNode(), XML.Descending.asNode());
 		if (!ascendingOrder)
 			dom2descendingOrder.add(element);
 		
 		ExtendedIterator<Node> children = GraphUtils.getPropertyValues(graph, elementNode, XML.hasChild.asNode());
 		
 		{
 		NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren =
 				new TreeMap<Node, Vector<org.w3c.dom.Node>>(nodeComparator);
 		Map<org.w3c.dom.Node, Node> childrenKeys =
 				new HashMap<org.w3c.dom.Node, Node>();
 		dom2orderedByKeyChildren.put(element, orderedByKeyChildren);
 		dom2childrenKeys.put(element, childrenKeys);
 		}
 //		Set<org.w3c.dom.Node> noKeyChildren = new HashSet<org.w3c.dom.Node>();
 		
 		while (children.hasNext()) {
 			Node child = children.next();
 //			if (!orderedChildren.contains(child)) {
 				org.w3c.dom.Node domChild = decodeNode(child);
 				if (domChild != null) {
 					addNodeMapping(child, domChild);
 					Node orderKeyNode =
 							( childrenOrderProperty != null ) ?
 									GraphUtils.getSingleValueOptProperty(graph, child, childrenOrderProperty) :
 									GraphUtils.getSingleValueOptProperty(graph, child, XML.orderKey.asNode()); 
 //					if (orderKeyNode == null) {
 ////						noKeyChildren.add(domChild);
 //						orderKeyNode = Node.NULL;
 //					} //else {
 					updateChildByKey(element, domChild, orderKeyNode);
 //						Vector<org.w3c.dom.Node> sameKeyBag = orderedByKeyChildren.get(orderKeyNode);
 //						if (sameKeyBag == null) {
 //							sameKeyBag = new Vector<org.w3c.dom.Node>();
 //							orderedByKeyChildren.put(orderKeyNode, sameKeyBag);
 //						}
 //						sameKeyBag.add(domChild);
 //					}
 				}
 //			}
 		}
 		reorder(/*noKeyChildren, */ascendingOrder, element, dom2orderedByKeyChildren.get(element));
 
 	}
 	
 	private void decodeElementAttrsAndChildren(final Element element, final Node elementNode) {
 		ExtendedIterator<Triple> triples =
 				graph.find(elementNode, Node.ANY, Node.ANY);
 		while (triples.hasNext()) {
 			Triple t = triples.next();
 			if ( graph.contains(
 					t.getPredicate(),
 					RDFS.subClassOf.asNode(),
 					XML.Attr.asNode())) {
 				Attr attr = decodeAttr(t.getPredicate());
 				attr.setValue(t.getObject().getLiteralLexicalForm());
 				element.setAttributeNodeNS(attr);
 			}
 		}
 		if (elementNode.isURI()) {
 			element.setAttribute("resource", elementNode.getURI());
 			if (!element.hasAttribute("id"))
 				element.setAttribute("id", newElementId());
 //				element.setAttribute("id", elementNode.getURI());
 		}
 //		Set<Node> orderedChildren = new HashSet<Node>();
 //		{
 //			Node child = GraphUtils.getSingleValueOptProperty(graph, elementNode, XML.firstChild.asNode());
 //			while (child != null) {
 //				orderedChildren.add(child);
 //				org.w3c.dom.Node newChild = decodeNode(graph, child);
 //				if (newChild != null) {
 //					addNodeMapping(child, newChild);
 //					element.appendChild(newChild);
 //				}
 //				child = GraphUtils.getSingleValueOptProperty(graph, child, XML.nextSibling.asNode());
 //			}
 //		}
 
 		setupElementChildren(elementNode, element);
 		
 //		System.out.println("Looking for eventListeners in element " + element + " (" + elementNode + ")");
 		Iterator<Node> eventTypeNodes = GraphUtils.getPropertyValues(graph, elementNode, XML.listenedEventType.asNode());
 		while (eventTypeNodes.hasNext()) {
 			Node eventTypeNode = eventTypeNodes.next();
 			if (eventTypeNode.isLiteral()) {
 				String eventType = eventTypeNode.getLiteralLexicalForm();
 //				System.out.println("Registering eventListener for type " + eventTypeNode.getLiteralLexicalForm() + " in element " + element + " (" + elementNode + ")");
 //				((EventTarget) element).addEventListener(eventType, this, false);
 				eventManager.addEventListener(elementNode, element, eventType, this, false);
 				
 //				Set<Element> elemsForEventType = eventType2elements.get(eventType);
 //				if (elemsForEventType == null) {
 //					elemsForEventType = new HashSet<Element>();
 //					eventType2elements.put(eventType, elemsForEventType);
 //					((EventTarget) document).addEventListener(eventType, this, false);
 //				}
 //				elemsForEventType.add(element);
 //				
 //				Set<String> eventTypesForElement = element2eventTypes.get(element);
 //				if (eventTypesForElement == null) {
 //					eventTypesForElement = new HashSet<String>();
 //					element2eventTypes.put(element, eventTypesForElement);
 //				}
 //				eventTypesForElement.add(eventType);
 				
 			}
 		}
 	}
 
 	private Element decodeElement(final Node elementNode) {
 		Element element =
 				document.createElementNS(
 						namespaceElement(graph, elementNode),
 						qNameElement(graph, elementNode) );
 		addNodeMapping(elementNode, element);
 		decodeElementAttrsAndChildren(element, elementNode);
 		return element;
 	}
 
 	private org.w3c.dom.Node decodeNode(Node elementNode) {
 		try {
 			Node nodeType = GraphUtils.getSingleValueProperty(graph, elementNode, RDF.type.asNode());
 			if ( graph.contains(nodeType, RDFS.subClassOf.asNode(), XML.Element.asNode()) )
 				return decodeElement(elementNode);
 			if ( nodeType.equals( XML.Text.asNode() ) )
 				return decodeText(graph, elementNode);
 			throw new RuntimeException("Type not recognised for node " + elementNode);
 		} catch(NoSuchElementException e) {
 			throw new RuntimeException("Type not found for node " + elementNode);
 		}
 	}
 
 	private void decodeDocument(Node docRootNode) {
 		Iterator<Node> possibleDocs =
 				GraphUtils.getPropertyValues(graph, docRootNode, XML.hasChild.asNode());
 		while (possibleDocs.hasNext()) {
 //			try {
 				Node elementNode = possibleDocs.next();
 				document =
 					domImplementation.createDocument(
 					namespaceElement(graph, elementNode),
 					qNameElement(graph, elementNode),
 					null);
 				if (docRootNode.isURI())
 					document.setDocumentURI(docRootNode.getURI());
 				Element docElement = document.getDocumentElement();
 				addNodeMapping(docRootNode, document);
 				addNodeMapping(elementNode, docElement);
 				decodeElementAttrsAndChildren( docElement, elementNode );
 //			} catch(RuntimeException e) { }
 		}
 	}
 	
 	private void redecodeDocument(Node docRootNode) {
 		graph2domNodeMapping = new HashMap<Node, Set<org.w3c.dom.Node>>();
 		dom2graphNodeMapping = new HashMap<org.w3c.dom.Node, Node>();
 //		eventType2elements = new HashMap<String, Set<Element>>();
 //		element2eventTypes = new HashMap<Element, Set<String>>();
 		decodeDocument(docRootNode);
 		if (docReceiver != null)
 			docReceiver.sendDocument(document);
 	}
 	
 	private void redecodeDocument() {
 		redecodeDocument(
 				graph
 					.find(SWI.GraphRoot.asNode(), XML.document.asNode(), Node.ANY)
 					.mapWith( new Map1<Triple, Node>() {
 						public Node map1(Triple t) {
 							return t.getObject();
 						}
 				})
 				.andThen(
 					graph
 						.find(Node.ANY, RDF.type.asNode(), XML.Document.asNode())
 						.mapWith( new Map1<Triple, Node>() {
 							public Node map1(Triple t) {
 								return t.getSubject();
 							}
 						}) ).next());
 	}
 
 	private void decodeWorker(DynamicGraph graph, Node docRootNode) {
 		logger.debug("decoding document from " + Utils.standardStr(graph) + " ...");
 		decodeDocument(docRootNode);
 		logger.debug("registering as listener of " + Utils.standardStr(graph) + " ...");
 		graph.getEventManager2().register(this);
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl) {
 		return (new DomDecoder(graph, docRootNode, domImpl)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			EventManager eventManager) {
 		return (new DomDecoder(graph, docRootNode, domImpl, eventManager)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext) {
 		return (new DomDecoder(graph, docRootNode, domImpl, updatesContext)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			EventManager eventManager) {
 		return (new DomDecoder(graph, docRootNode, domImpl, updatesContext, eventManager)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, DocumentReceiver docReceiver) {
 		return (new DomDecoder(graph, docRootNode, domImpl, docReceiver)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		return (new DomDecoder(graph, docRootNode, domImpl, docReceiver, eventManager)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			DocumentReceiver docReceiver) {
 		return (new DomDecoder(graph, docRootNode, domImpl, updatesContext, docReceiver)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		return (new DomDecoder(graph, docRootNode, domImpl, updatesContext, docReceiver, eventManager)).getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, eventManager);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, updatesContext);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, updatesContext, eventManager);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, docReceiver);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, docReceiver, eventManager);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, updatesContext, docReceiver);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decode(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl, RunnableContext updatesContext,
 			DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		DomDecoder domDecoder = new DomDecoder(graph, docRootNode, domImpl, updatesContext, docReceiver, eventManager);
 		if (domEventListeners != null)
 			for (String eventType : domEventListeners.keySet())
 				for (DomEventListener listener : domEventListeners.get(eventType))
 					domDecoder.addDomEventListener(eventType, listener);
 		return domDecoder.getDocument();
 	}
 
 	public static Document decodeOne(DynamicGraph graph, DOMImplementation domImpl) {
 		return decodeAll(graph, domImpl).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext) {
 		return decodeAll(graph, domImpl, updatesContext).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, docReceiver).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, docReceiver, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext, DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext, DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, domEventListeners, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, domEventListeners, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, docReceiver, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, docReceiver, domEventListeners, eventManager).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext, DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, domEventListeners).next();
 	}
 
 	public static Document decodeOne(
 			DynamicGraph graph, DOMImplementation domImpl,
 			RunnableContext updatesContext, DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, domEventListeners, eventManager).next();
 	}
 
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl) {
 		return decodeAll(graph, domImpl, (RunnableContext) null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, (RunnableContext) null, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, null, docReceiver);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, null, docReceiver, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl, 
 			final RunnableContext updatesContext) {
 		return decodeAll(graph, domImpl, updatesContext, (DocumentReceiver) null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl, 
 			final RunnableContext updatesContext,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, (DocumentReceiver) null, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final RunnableContext updatesContext, final DocumentReceiver docReceiver) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, (EventManager) null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final RunnableContext updatesContext, final DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, null, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, (RunnableContext) null, domEventListeners);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, (RunnableContext) null, domEventListeners, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, null, docReceiver, domEventListeners);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final DocumentReceiver docReceiver,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, null, docReceiver, domEventListeners, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl, 
 			final RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, null, domEventListeners);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl, 
 			final RunnableContext updatesContext,
 			Map<String,Set<DomEventListener>> domEventListeners,
 			EventManager eventManager) {
 		return decodeAll(graph, domImpl, updatesContext, null, domEventListeners, eventManager);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final RunnableContext updatesContext, final DocumentReceiver docReceiver,
 			final Map<String,Set<DomEventListener>> domEventListeners) {
 		return decodeAll(graph, domImpl, updatesContext, docReceiver, domEventListeners, null);
 	}
 	
 	public static ExtendedIterator<Document> decodeAll(
 			final DynamicGraph graph, final DOMImplementation domImpl,
 			final RunnableContext updatesContext, final DocumentReceiver docReceiver,
 			final Map<String,Set<DomEventListener>> domEventListeners,
 			final EventManager eventManager) {
 		return
 				graph
 					.find(SWI.GraphRoot.asNode(), XML.document.asNode(), Node.ANY)
 					.mapWith( new Map1<Triple, Node>() {
 						public Node map1(Triple t) {
 							return t.getObject();
 						}
 					})
 					.andThen(
 						graph
 							.find(Node.ANY, RDF.type.asNode(), XML.Document.asNode())
 							.mapWith( new Map1<Triple, Node>() {
 								public Node map1(Triple t) {
 									return t.getSubject();
 								}
 							}))
 					.mapWith( new Map1<Node, Document>() {
 						public Document map1(Node node) {
 							return decode(graph, node, domImpl, updatesContext, docReceiver, domEventListeners, eventManager);
 						}
 					} );
 	}
 	
 	private void addNodeMapping(Node graphNode, org.w3c.dom.Node domNode) {
 //		System.out.println(this + ": adding mapping ( " + graphNode + " -> " + domNode + " )");
 		Set<org.w3c.dom.Node> domeNodeSet = graph2domNodeMapping.get(graphNode);
 		if (domeNodeSet == null) {
 			domeNodeSet = new HashSet<org.w3c.dom.Node>();
 			graph2domNodeMapping.put(graphNode, domeNodeSet);
 		}
 		domeNodeSet.add(domNode);
 		dom2graphNodeMapping.put(domNode, graphNode);
 	}
 	
 	private void removeNodeMapping(org.w3c.dom.Node domNode) {
 		Node graphNode = dom2graphNodeMapping.get(domNode);
 		if (graphNode != null) {
 			dom2graphNodeMapping.remove(domNode);
 			Set<org.w3c.dom.Node> domeNodeSet = graph2domNodeMapping.get(graphNode);
 			domeNodeSet.remove(domNode);
 			if (domeNodeSet.isEmpty())
 				graph2domNodeMapping.remove(graphNode);
 		}
 		dom2orderedByKeyChildren.remove(domNode);
 		dom2childrenOrderProperty.remove(domNode);
 		dom2descendingOrder.remove(domNode);
 	}
 	
 	private void removeSubtreeMapping(org.w3c.dom.Node domNode) {
 //		System.out.println(this + ": removing subtree mapping of " + domNode );
 		removeNodeMapping(domNode);
 		NamedNodeMap attrMap = domNode.getAttributes();
 		if (attrMap != null) {
 			for (int i = 0; i < attrMap.getLength(); i++) {
 				removeSubtreeMapping(attrMap.item(i));
 			}
 		}
 		NodeList children = domNode.getChildNodes();
 		if (children != null) {
 			for (int i = 0; i < children.getLength(); i++) {
 				removeSubtreeMapping(children.item(i));
 			}
 		}
 //		if (domNode instanceof Element) {
 //			Element element = (Element) domNode;
 //			Set<String> eventTypesForElement = element2eventTypes.get(element);
 //			if (eventTypesForElement != null) {
 //				for (String eventType : eventTypesForElement) {
 //					Set<Element> elementsForEventType = eventType2elements.get(eventType);
 //					elementsForEventType.remove(element);
 //					if (elementsForEventType.isEmpty()) {
 //						eventType2elements.remove(eventType);
 //						((EventTarget) document).removeEventListener(eventType, DomDecoder2.this, false);
 //					}
 //				}
 //				element2eventTypes.remove(element);
 //			}			
 //		}
 	}
 	
	private DomDecoder(DynamicGraph graph) {
		this.graph = graph;
 		this.updatesContext = this;
 	}
 
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl) {
 		this(graph, docRootNode, domImpl, (DocumentReceiver) null);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			EventManager eventManager) {
 		this(graph, docRootNode, domImpl, (DocumentReceiver) null, eventManager);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			DocumentReceiver docReceiver) {
 		this(graph, docRootNode, domImpl, null, docReceiver);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		this(graph, docRootNode, domImpl, null, docReceiver, eventManager);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			RunnableContext updatesContext) {
 		this(graph, docRootNode, domImpl, updatesContext,(EventManager) null);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			EventManager eventManager) {
 		this(graph, docRootNode, domImpl, updatesContext, null, eventManager);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			DocumentReceiver docReceiver) {
 		this(graph, docRootNode, domImpl, updatesContext, docReceiver, null);
 	}
 	
 	private DomDecoder(
 			DynamicGraph graph, Node docRootNode,
 			DOMImplementation domImpl,
 			RunnableContext updatesContext,
 			DocumentReceiver docReceiver,
 			EventManager eventManager) {
 		this.graph = graph;
 		this.domImplementation = domImpl;
 		this.updatesContext = ( updatesContext == null ? this : updatesContext );
 		this.docReceiver = docReceiver;
 		this.eventManager = eventManager;
 		this.docRootNode = docRootNode;
 		decodeWorker(graph, docRootNode);
 	}
 	
 	public void run(Runnable runnable) {
 		runnable.run();
 	}
 	
 	private Document getDocument() {
 		return document;
 	}
 	
 	private void orderChild(org.w3c.dom.Node node, Element parent, Node orderKeyNode) {
 		putChildByKey(parent, node, orderKeyNode);
 		NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren =
 				dom2orderedByKeyChildren.get(parent);
 		if (!dom2descendingOrder.contains(parent)) {
 			Entry<Node, Vector<org.w3c.dom.Node>> nextEntry =
 					orderedByKeyChildren.higherEntry(orderKeyNode);
 			if (nextEntry == null) {
 				parent.appendChild(node);
 			} else {
 				parent.insertBefore(node, nextEntry.getValue().firstElement());
 			}
 		} else {
 			Entry<Node, Vector<org.w3c.dom.Node>> nextEntry =
 					orderedByKeyChildren.lowerEntry(orderKeyNode);
 			if (nextEntry == null) {
 				parent.appendChild(node);
 			} else {
 				parent.insertBefore(node, nextEntry.getValue().firstElement());
 			}
 		}
 	}
 	
 	private void reorderChild(org.w3c.dom.Node node, Element parent, Node orderKeyNode) {
 		orderChild(node, parent, orderKeyNode);
 	}
 	
 	private void insertChildInOrder(org.w3c.dom.Node child, Node childNode, Element parent) {
 		Node childrenOrderProperty = dom2childrenOrderProperty.get(parent);
 		Node orderKeyNode =
 				( childrenOrderProperty != null ) ?
 						GraphUtils.getSingleValueOptProperty(graph, childNode, childrenOrderProperty) :
 						GraphUtils.getSingleValueOptProperty(graph, childNode, XML.orderKey.asNode()); 
 		orderChild(child, parent, orderKeyNode);
 	}
 	
 	private void removeChild(org.w3c.dom.Node node, Element parent) {
 		removeChildByKey(parent, node);
 		parent.removeChild(node);
 	}
 	
 	private boolean mustReorderAllChildrenOf(Element parent, GraphUpdate update) {
 		Node parentNode = dom2graphNodeMapping.get(parent);
 		return
 				update.getAddedGraph().contains(parentNode, XML.childrenOrderedBy.asNode(), Node.ANY)
 				|| update.getAddedGraph().contains(parentNode, XML.childrenOrderType.asNode(), Node.ANY)
 				|| update.getDeletedGraph().contains(parentNode, XML.childrenOrderedBy.asNode(), Node.ANY)
 				|| update.getDeletedGraph().contains(parentNode, XML.childrenOrderType.asNode(), Node.ANY);
 
 	}
 	
 	public synchronized void notifyUpdate(final Graph sourceGraph, final GraphUpdate update) {
 		
 		logger.debug("Begin of Notify Update in DOM Decoder");
 
 		if (!update.getAddedGraph().isEmpty() || !update.getDeletedGraph().isEmpty()) {
 			updatesContext.run(
 					new Runnable() {
 						@SuppressWarnings("unchecked")
 						public void run() {
 							ExtendedIterator<Triple> addEventsIter =
 									update.getAddedGraph().find(Node.ANY, Node.ANY, Node.ANY);
 							
 							DomDecoder newDom = new DomDecoder(graph);
 							newDom.dom2graphNodeMapping =
 									(Map<org.w3c.dom.Node, Node>) ((HashMap<org.w3c.dom.Node, Node>) dom2graphNodeMapping).clone();
 //							newDom.graph2domNodeMapping = (Map<Node, Set<org.w3c.dom.Node>>) ((HashMap<Node, Set<org.w3c.dom.Node>>) graph2domNodeMapping).clone();
 							for (Node key : graph2domNodeMapping.keySet()) {
 								newDom.graph2domNodeMapping.put(key, (Set<org.w3c.dom.Node>) ((HashSet<org.w3c.dom.Node>) graph2domNodeMapping.get(key)).clone());
 							}
 							newDom.document = document;
 //							newDom.document = (Document) document.cloneNode(true);
 							
 							while (addEventsIter.hasNext()) {
 								final Triple newTriple = addEventsIter.next();
 //								org.w3c.dom.Node xmlSubj = nodeMapping.get(newTriple.getSubject());
 								//System.out.println("Checking add event " + newTriple);
 								Set<org.w3c.dom.Node> domSubjs = graph2domNodeMapping.get(newTriple.getSubject());
 //								if (domSubjs == null)
 //									logger.warn(this + ": managing add event " + newTriple + ", domSubjs is null");
 								if (domSubjs != null) {
 									logger.trace("Managing add event " + newTriple + " for domSubjs " + domSubjs);
 									Set<org.w3c.dom.Node> domSubjsTemp = new HashSet<org.w3c.dom.Node>();
 									domSubjsTemp.addAll(domSubjs);
 									Iterator<org.w3c.dom.Node> domSubjIter = domSubjsTemp.iterator();
 									
 									// Basic properties: DOM node must be recreated
 									if (newTriple.getPredicate().equals(RDF.type.asNode())
 											|| newTriple.getPredicate().equals(XML.nodeName.asNode())) {
 										//org.w3c.dom.Node parentNode = null;
 										Node nodeType = newTriple.getObject();
 										
 										if ( nodeType.equals(XML.Element.asNode()) || graph.contains(nodeType, RDFS.subClassOf.asNode(), XML.Element.asNode()) ) {
 											while (domSubjIter.hasNext()) {
 												org.w3c.dom.Node domSubj = domSubjIter.next();
 												org.w3c.dom.Node parentNode = domSubj.getParentNode();
 												if (parentNode != null) {
 													org.w3c.dom.Node newNode = newDom.decodeNode(newTriple.getSubject());
 													parentNode.replaceChild(newNode, domSubj);
 													newDom.removeSubtreeMapping(domSubj);
 													newDom.addNodeMapping(newTriple.getSubject(), newNode);
 												}
 											}
 										} else if (
 												nodeType.equals(XML.Document.asNode())
 												&& graph.contains(SWI.GraphRoot.asNode(), XML.document.asNode(), newTriple.getSubject())) {
 											redecodeDocument(newTriple.getSubject());
 											return;
 										}
 										
 									// Predicate is a DOM Attribute
 									} else if (
 											graph.contains(
 													newTriple.getPredicate(),
 													RDFS.subClassOf.asNode(),
 													XML.Attr.asNode() ) ) {
 										while (domSubjIter.hasNext()) {
 											Element element = (Element) domSubjIter.next();
 											Attr newAttr = newDom.decodeAttr(newTriple.getPredicate());
 //											newDom.addNodeMapping(newTriple.getPredicate(), newAttr);
 											newAttr.setValue(newTriple.getObject().getLiteralLexicalForm());
 											element.setAttributeNodeNS(newAttr);
 										}
 
 									// Predicate is xml:hasChild
 									} else if ( newTriple.getPredicate().equals(XML.hasChild.asNode()) ) {
 										Node nodeType =
 												graph
 												.find(newTriple.getSubject(), XML.nodeType.asNode(), Node.ANY)
 												.next().getObject();
 										logger.trace("Managing add hasChild (" + newTriple + ") for domSubjs " + domSubjs + " and node type " + nodeType);
 										if (nodeType.equals(XML.Element.asNode()) || graph.contains(nodeType, RDFS.subClassOf.asNode(), XML.Element.asNode()) ) {
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 												org.w3c.dom.Node newChild = newDom.decodeNode(newTriple.getObject());
 												newDom.addNodeMapping(newTriple.getObject(), newChild);
 //												element.appendChild(newChild);
 												insertChildInOrder(newChild, newTriple.getObject(), element);
 											}
 										} else if (
 												nodeType.equals(XML.Document.asNode())
 												&& graph.contains(SWI.GraphRoot.asNode(), XML.document.asNode(), newTriple.getSubject())) {
 											redecodeDocument(newTriple.getSubject());
 											return;
 										}
 
 									// Predicate is xml:nodeValue
 									} else if ( newTriple.getPredicate().equals(XML.nodeValue.asNode()) ) {
 										logger.trace("Managing add nodeValue (" + newTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											node.setNodeValue(newTriple.getObject().getLiteralLexicalForm());
 										}
 
 									// Predicate is orderKey
 									} else if ( newTriple.getPredicate().equals(XML.orderKey.asNode()) ) {
 										logger.trace("Managing add orderKey (" + newTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											org.w3c.dom.Node parent = node.getParentNode();
 											if (	parent != null
 													&& parent instanceof Element
 													&& !mustReorderAllChildrenOf((Element) parent, update) ) {
 												Node childrenOrderProperty = dom2childrenOrderProperty.get((Element) parent);
 												if ( childrenOrderProperty == null || childrenOrderProperty.equals(XML.orderKey.asNode()) ) {
 													Node orderKeyNode = newTriple.getObject();
 													reorderChild(node, (Element) parent, orderKeyNode);
 												}
 											}
 										}
 
 									// Predicate xml:childrenOrderType and object xml:Descending
 									} else if (
 											newTriple.getPredicate().equals(XML.childrenOrderType.asNode())
 											&& newTriple.getObject().equals(XML.Descending)
 											&& !update.getAddedGraph().contains(newTriple.getSubject(), XML.childrenOrderedBy.asNode(), Node.ANY)) {
 										logger.trace("Managing add childrenOrderType (" + newTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											if (node instanceof Element) {
 												NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren =
 														dom2orderedByKeyChildren.get(node);
 												if (!dom2descendingOrder.contains(node)) {
 													dom2descendingOrder.add((Element) node);
 													reorder(false, (Element) node, orderedByKeyChildren);
 												}
 											}
 										}
 
 									// Predicate xml:childrenOrderedBy
 									} else if (	newTriple.getPredicate().equals(XML.childrenOrderedBy.asNode()) ) {
 										logger.trace("Managing add childrenOrderedBy (" + newTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											if (node instanceof Element)
 												setupElementChildren(newTriple.getSubject(), (Element) node);
 										}
 
 										// Predicate is xml:listenedEventType
 									} else if ( newTriple.getPredicate().equals(XML.listenedEventType.asNode()) ) {
 										Node eventTypeNode = newTriple.getObject();
 										if (eventTypeNode.isLiteral()) {
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 //												System.out.println("Registering eventListener for type " + eventTypeNode.getLiteralLexicalForm() + " in element " + element + " (" + elementNode + ")");
 //												((EventTarget) element).addEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder2.this, false);
 												eventManager.addEventListener(
 													newTriple.getSubject(),
 													element,
 													eventTypeNode.getLiteralLexicalForm(),
 													DomDecoder.this, false);
 
 //												Set<Element> elemsForEventType = eventType2elements.get(eventType);
 //												if (elemsForEventType == null) {
 //													elemsForEventType = new HashSet<Element>();
 //													eventType2elements.put(eventType, elemsForEventType);
 //													((EventTarget) document).addEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder2.this, false);
 //												}
 //												elemsForEventType.add(element);
 //
 //												Set<String> eventTypesForElement = element2eventTypes.get(element);
 //												if (eventTypesForElement == null) {
 //													eventTypesForElement = new HashSet<String>();
 //													element2eventTypes.put(element, eventTypesForElement);
 //												}
 //												eventTypesForElement.add(eventType);
 											}
 										}
 									}	
 
 									// Predicate is the childrenOrderedBy for some parent
 									while (domSubjIter.hasNext()) {
 										org.w3c.dom.Node node = domSubjIter.next();
 										org.w3c.dom.Node parent = node.getParentNode();
 										if (	parent != null
 												&& parent instanceof Element ) {
 											Node childrenOrderProperty = dom2childrenOrderProperty.get((Element) parent);
 											if ( 	childrenOrderProperty != null
 													&& childrenOrderProperty.equals(newTriple.getPredicate())
 													&& !mustReorderAllChildrenOf((Element) parent, update) ) {
 												logger.trace("Managing add predicate is the childrenOrderedBy for some parent (" + newTriple + ")");
 												Node orderKeyNode = newTriple.getObject();
 												reorderChild(node, (Element) parent, orderKeyNode);
 											}
 										}
 									}
 
 								}			
 							}
 
 							ExtendedIterator<Triple> deleteEventsIter =
 									update.getDeletedGraph().find(Node.ANY, Node.ANY, Node.ANY);
 							while (deleteEventsIter.hasNext()) {
 								Triple oldTriple = deleteEventsIter.next();
 								//org.w3c.dom.Node xmlSubj = nodeMapping.get(oldTriple.getSubject());
 								Set<org.w3c.dom.Node> domSubjs = graph2domNodeMapping.get(oldTriple.getSubject());
 								//System.out.println("Checking for " + oldTriple.getSubject() + " contained in " + sourceGraph);
 								if (domSubjs != null && graph.contains(oldTriple.getSubject(), RDF.type.asNode(), Node.ANY)) {
 									//System.out.println("Found " + oldTriple.getSubject() + " contained in " + sourceGraph);
 									//System.out.println("Managing " + oldTriple.getPredicate() + "/" + oldTriple.getObject());
 									Iterator<org.w3c.dom.Node> domSubjIter = domSubjs.iterator();
 									if ( 		(	oldTriple.getPredicate().equals(XML.nodeName.asNode())
 													&& !update.getAddedGraph().contains(oldTriple.getSubject(), XML.nodeName.asNode(), Node.ANY) )
 											||	(	oldTriple.getPredicate().equals(XML.nodeType.asNode())
 													&& !update.getAddedGraph().contains(oldTriple.getSubject(), XML.nodeType.asNode(), Node.ANY) ) ) {
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node domSubj = domSubjIter.next();
 											switch (domSubj.getNodeType()) {
 											case org.w3c.dom.Node.ATTRIBUTE_NODE:
 												Attr oldAttr = (Attr) domSubj;
 												Element ownerElement = oldAttr.getOwnerElement();
 												ownerElement.removeAttributeNode(oldAttr);
 												newDom.removeSubtreeMapping(oldAttr);
 												break;
 											case org.w3c.dom.Node.DOCUMENT_NODE: 
 												if ( oldTriple.getSubject().equals(docRootNode) ) {
 													redecodeDocument();
 													return;
 												}
 												break;
 											default:
 												org.w3c.dom.Node parentNode = domSubj.getParentNode();
 												if (parentNode != null) {
 													parentNode.removeChild(domSubj);
 													newDom.removeSubtreeMapping(domSubj);
 												}
 											}
 												
 										}
 									} else if (
 											oldTriple.getPredicate().equals(XML.nodeValue.asNode())
 											&& !graph.contains(oldTriple.getSubject(), XML.nodeValue.asNode(), Node.ANY)) {
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node domSubj = domSubjIter.next();
 											domSubj.setNodeValue("");
 										}
 									} else if (
 											graph.contains(
 													oldTriple.getPredicate(),
 													RDFS.subClassOf.asNode(),
 													XML.Attr.asNode() ) ) {
 										while (domSubjIter.hasNext()) {
 											Element element = (Element) domSubjIter.next();
 											newDom.removeAttr(element, oldTriple.getPredicate());
 										}
 //										Set<org.w3c.dom.Node> domObjsOrig = graph2domNodeMapping.get(oldTriple.getObject());
 //										if (domObjsOrig != null) {
 //											Set<org.w3c.dom.Node> domObjs = new HashSet<org.w3c.dom.Node>();
 //											domObjs.addAll(domObjsOrig);
 //											while (domSubjIter.hasNext()) {
 //												Element element = (Element) domSubjIter.next();
 //												Iterator<org.w3c.dom.Node> domObjsIter = domObjs.iterator();
 //												while (domObjsIter.hasNext()) {
 //													try {
 //														Attr oldAttr = (Attr) domObjsIter.next();
 //														if ( oldAttr.getNamespaceURI() == null
 //																? element.hasAttribute(oldAttr.getName())
 //																: element.hasAttributeNS(oldAttr.getNamespaceURI(), oldAttr.getLocalName()))
 //															element.removeAttributeNode(oldAttr);
 //														newDom.removeSubtreeMapping(oldAttr);
 //													} catch(DOMException e) {
 //														if (!e.equals(DOMException.NOT_FOUND_ERR))
 //															throw e;
 //													}
 //												}
 //											}
 //										}
 									} else if ( oldTriple.getPredicate().equals(XML.hasChild.asNode()) ) {
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node domSubj = domSubjIter.next();
 											if ( domSubj.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE && oldTriple.getSubject().equals(docRootNode) ) {
 												redecodeDocument();
 												return;
 											}
 											Set<org.w3c.dom.Node> domObjs = graph2domNodeMapping.get(oldTriple.getObject());
 											if (domObjs != null) {
 												Element element = (Element) domSubj;
 												Iterator<org.w3c.dom.Node> domObjsIter = domObjs.iterator();
 												while (domObjsIter.hasNext()) {
 													try {
 														org.w3c.dom.Node domObj = domObjsIter.next();
 														removeChild(domObj, element);
 														newDom.removeSubtreeMapping(domObj);
 													} catch(DOMException e) {
 														if (!e.equals(DOMException.NOT_FOUND_ERR))
 															throw e;
 													}
 												}
 											}
 										}
 
 									// Predicate is orderKey
 									} else if (
 											oldTriple.getPredicate().equals(XML.orderKey.asNode())
 											&& !update.getAddedGraph().contains(oldTriple.getSubject(), XML.orderKey.asNode(), Node.ANY) ) {
 										logger.trace("Managing delete orderKey (" + oldTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											org.w3c.dom.Node parent = node.getParentNode();
 											if (	parent != null
 													&& parent instanceof Element
 													&& !mustReorderAllChildrenOf((Element) parent, update) ) {
 												Node childrenOrderProperty = dom2childrenOrderProperty.get((Element) parent);
 												if ( childrenOrderProperty == null || childrenOrderProperty.equals(XML.orderKey.asNode()) ) {
 													Node orderKeyNode = oldTriple.getObject();
 													reorderChild(node, (Element) parent, orderKeyNode);
 												}
 											}
 										}
 
 									// Predicate xml:childrenOrderType and object xml:Descending
 									} else if (
 											oldTriple.getPredicate().equals(XML.childrenOrderType.asNode())
 											&& oldTriple.getObject().equals(XML.Descending)
 											&& !update.getAddedGraph().contains(oldTriple.getSubject(), XML.childrenOrderedBy.asNode(), Node.ANY)
 											&& !update.getDeletedGraph().contains(oldTriple.getSubject(), XML.childrenOrderedBy.asNode(), Node.ANY) ) {
 										logger.trace("Managing delete childrenOrderType (" + oldTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											if (node instanceof Element) {
 												NavigableMap<Node, Vector<org.w3c.dom.Node>> orderedByKeyChildren =
 														dom2orderedByKeyChildren.get(node);
 												if (dom2descendingOrder.contains(node)) {
 													dom2descendingOrder.remove((Element) node);
 													reorder(true, (Element) node, orderedByKeyChildren);
 												}
 											}
 										}
 
 									// Predicate xml:childrenOrderedBy
 									} else if (
 											oldTriple.getPredicate().equals(XML.childrenOrderedBy.asNode())
 											&& !update.getAddedGraph().contains(oldTriple.getSubject(), XML.childrenOrderedBy.asNode(), Node.ANY) ) {
 										logger.trace("Managing delete childrenOrderedBy (" + oldTriple + ") for domSubjs " + domSubjs);
 										while (domSubjIter.hasNext()) {
 											org.w3c.dom.Node node = domSubjIter.next();
 											if (node instanceof Element)
 												setupElementChildren(oldTriple.getSubject(), (Element) node);
 										}
 										
 									} else if ( oldTriple.getPredicate().equals(XML.listenedEventType.asNode()) ) {
 										Node eventTypeNode = oldTriple.getObject();
 										if (eventTypeNode.isLiteral()) {
 											while (domSubjIter.hasNext()) {
 												Element element = (Element) domSubjIter.next();
 //												System.out.println("Registering eventListener for type " + eventTypeNode.getLiteralLexicalForm() + " in element " + element + " (" + elementNode + ")");
 //												((EventTarget) element).removeEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder2.this, false);
 												
 												eventManager.removeEventListener(
 														oldTriple.getSubject(),
 														element,
 														eventTypeNode.getLiteralLexicalForm(),
 														DomDecoder.this, false);
 												
 //												Set<Element> elemsForEventType = eventType2elements.get(eventType);
 //												elemsForEventType.remove(element);
 //												if (elemsForEventType.isEmpty()) {
 //													eventType2elements.remove(eventType);
 //													((EventTarget) document).removeEventListener(eventTypeNode.getLiteralLexicalForm(), DomDecoder2.this, false);
 //												}
 //
 //												Set<String> eventTypesForElement = element2eventTypes.get(element);
 //												eventTypesForElement.remove(eventType);
 //												if (eventTypesForElement.isEmpty()) {
 //													element2eventTypes.remove(element);
 //												}
 											}
 										}
 									}
 									
 									// Predicate is the childrenOrderedBy for some parent
 									while (domSubjIter.hasNext()) {
 										org.w3c.dom.Node node = domSubjIter.next();
 										org.w3c.dom.Node parent = node.getParentNode();
 										if (	parent != null
 												&& parent instanceof Element ) {
 											Node childrenOrderProperty = dom2childrenOrderProperty.get((Element) parent);
 											if ( 	childrenOrderProperty != null
 													&& childrenOrderProperty.equals(oldTriple.getPredicate())
 													&& !update.getAddedGraph().contains(oldTriple.getSubject(), childrenOrderProperty, Node.ANY)
 													&& !mustReorderAllChildrenOf((Element) parent, update) ) {
 												logger.trace("Managing delete predicate that is the childrenOrderedBy for some parent (" + oldTriple + ")");
 												reorderChild(node, (Element) parent, null);
 											}
 										}
 									}
 
 								}
 //						        System.out.println("End of notifyEvents() in " + this);
 							}
 							
 							dom2graphNodeMapping = newDom.dom2graphNodeMapping;
 							graph2domNodeMapping = newDom.graph2domNodeMapping;
 //							document = newDom.document;
 						}
 					});
 		}
 
 		logger.debug("End of Notify Update in DOM Decoder");
 
 	}
 
 }
