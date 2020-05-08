 package org.zend.php.zendserver.deployment.core.internal.descriptor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.zend.php.zendserver.deployment.core.DeploymentCore;
 import org.zend.php.zendserver.deployment.core.descriptor.ChangeEvent;
 import org.zend.php.zendserver.deployment.core.descriptor.DeploymentDescriptorFactory;
 import org.zend.php.zendserver.deployment.core.descriptor.DeploymentDescriptorPackage;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelContainer;
 import org.zend.php.zendserver.deployment.core.descriptor.IModelObject;
 
 public class ModelSerializer {
 
 	private static final String AT = "@"; //$NON-NLS-1$
 	
 	private DocumentBuilderFactory domfactory;
 	private SAXParserFactory saxfactory;
 	private DocumentBuilder builder;
 	private XPath xpathObj;
 	
 	private Document document;
 	private DocumentStore dest;
 
 	public ModelSerializer() {
 		saxfactory = SAXParserFactory.newInstance();
 		domfactory = DocumentBuilderFactory.newInstance();
 		try {
 			builder = domfactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			DeploymentCore.log(e);
 		}
 		XPathFactory factory = XPathFactory.newInstance();
 		xpathObj = factory.newXPath();
 	}
 	
 	public void load(InputStream src, InputStream src2, IModelContainer model) throws XPathExpressionException, SAXException, IOException {
 		document = builder.parse(src);
 		if (src.markSupported()) {
 			src2 = src;
 			src2.reset();
 		}
 		CalculateOffsets c = new CalculateOffsets(src2);
 		c.traverse(document);
 		
 		Node root = getNode(document, DeploymentDescriptorPackage.PACKAGE.xpath);
 		
 		loadProperties(root, model);
 	}
 	
 	public void serialize(IModelContainer model) throws XPathExpressionException, CoreException, TransformerFactoryConfigurationError, TransformerException {
 		serialize(model, null);
 	}
 	
 	public void serialize(IModelContainer model, ChangeEvent event) throws XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
 		if (document == null) {
 			document = DeploymentDescriptorFactory.createEmptyDocument(builder);
 		}
 		
 		Node root = getNode(document, DeploymentDescriptorPackage.PACKAGE.xpath);
 		if (root == null) {
 			root = addNode(document, DeploymentDescriptorPackage.PACKAGE.xpath, null);
 		}
 		
 		writeProperties(root, model, event);
 	}
 	
 	public void setOutput(DocumentStore dest) {
 		this.dest = dest;
 	}
 	
 	public void write() throws CoreException {
 		if (dest == null) {
 			return;
 		}
 		
 		try {
 			Result result = dest.getOutput();
 
 	        Source source = new DOMSource(document);
 	        
 	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
 	        xformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
 	        xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //$NON-NLS-1$ //$NON-NLS-2$
 	        xformer.transform(source, result);
 	        
 			dest.write();
 			
 		} catch (TransformerFactoryConfigurationError e) {
 			throw new CoreException(new Status(IStatus.ERROR, DeploymentCore.PLUGIN_ID, e.getMessage(), e));
 		} catch (TransformerException e) {
 			throw new CoreException(new Status(IStatus.ERROR, DeploymentCore.PLUGIN_ID, e.getMessage(), e));
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR, DeploymentCore.PLUGIN_ID, e.getMessage(), e));
 		}
 	}
 	
 	private void loadProperties(Node doc, IModelObject obj) throws XPathExpressionException {
 		Feature[] props = obj.getPropertyNames();
 		for (Feature feature : props) {
 			String s;
 			Node propertyNode = null;
 			if (feature.attrName != null) {
 				s = getXpathString(doc, getXPath(feature.xpath, feature.attrName));
 			} else {
 				propertyNode = getNode(doc, feature.xpath);
 				s = propertyNode == null ? null : propertyNode.getTextContent(); 
 			}
 			
 			Integer nodeOffset = (Integer) (propertyNode != null ? propertyNode.getUserData(CalculateOffsets.NODE_OFFSET) : doc.getUserData(CalculateOffsets.NODE_OFFSET));
 			if (nodeOffset != null) {
 				obj.setOffset(feature, nodeOffset.intValue());
 			}
 			
 			String value = stripWhitespaces(s);
 			if (value != null) {
 				obj.set(feature, value);
 			}
 		}
 		
 		if (obj instanceof IModelContainer) {
 			loadChildren(doc, (IModelContainer)obj);
 		}
 	}
 	
 	/**
 	 * Loads DOM model to java model adding new elements.
 	 * 
 	 * @param doc
 	 * @param model
 	 * @throws XPathExpressionException 
 	 */
 	private void loadChildren(Node doc, IModelContainer model) throws XPathExpressionException {
 		Feature[] childNames = model.getChildNames();
 		for (Feature c : childNames) {
 			Node[] nodes = getNodes(doc, c.xpath);
 			List<Object> children = model.getChildren(c);
 			
 			if (c.type == IModelObject.class) {
 				for (int i = 0; i < nodes.length; i++) {
 					Node node = nodes[i];
 					IModelObject obj;
 					if (i < children.size()) {
 						obj = (IModelObject) children.get(i);
 					} else {
 						obj = DeploymentDescriptorFactory.createModelElement(c);
 						children.add(obj);
 					}
 					loadProperties(node, obj);
 				}
 				for (int i = nodes.length; i < children.size(); i++) {
 					children.remove(i);
 				}
 				
 			} else if (c.type == String.class){
 				for (int i = 0; i < nodes.length; i++) {
 					String string = nodes[i].getTextContent();
 					string = stripWhitespaces(string);
 					if (i < children.size()) {
 						children.set(i, string);
 					} else {
 						children.add(string);
 					}
 				}
 				for (int i = nodes.length; i < children.size(); i++) {
 					children.remove(i);
 				}
 				
 			} else throw new UnsupportedOperationException("Unsupported collection type "+c.type);
 		}
 	}
 	
 	private void writeProperties(Node doc, IModelObject obj, ChangeEvent event) throws XPathExpressionException {
 		Node lastAdded = null;
 		if (event == null || event.target == obj) { 
 			Feature[] props = obj.getPropertyNames();
 			for (Feature feature : props) {
 				String value = obj.get(feature);
 				if ((value == null) || ("".equals(value) && ((feature.flags & Feature.SET_EMPTY_TO_NULL) == Feature.SET_EMPTY_TO_NULL))) { //$NON-NLS-1$
 					removeString(doc, feature.xpath, feature.attrName);
 				} else {
 					lastAdded = setString(doc, feature.xpath, feature.attrName, value, lastAdded);
 				}
 			}
 		}
 		
 		if (obj instanceof IModelContainer) {
 			writeChildren(doc, (IModelContainer) obj, event, lastAdded);
 		}
 	}
 	
 	private void writeChildren(Node doc, IModelContainer model, ChangeEvent event, Node lastAddedNode) throws XPathExpressionException {
 		if (event == null || event.target == model) {
 			
 			Feature[] features = model.getChildNames();
 			for (Feature f : features) {
 				Node[] nodes = getNodes(doc, f.xpath);
 				
 				List<Object> children = model.getChildren(f);
 				for (int j = 0; j < children.size(); j++) {
 					Node node = null;
 					if (j < nodes.length) {
 						node = nodes[j];
 					}
 					
 					if (f.type == IModelObject.class) {
 						 if (node == null) {
 							node = addNode(doc, f.xpath, lastAddedNode);
 						}
 						writeProperties(node, (IModelObject) children.get(j), null); // don't pass event, rewriter all children of modified element
 					} else if (f.type == String.class) {
 						if (node == null) {
 							node = addNode(doc, f.xpath, lastAddedNode);
 						}
 						node.setTextContent((String)children.get(j));
 					}  else {
 						throw new UnsupportedOperationException("Unsupported collection type "+f.type);
 					}
 					
 					if (node != null) {
 						lastAddedNode = getDirectChild(doc, node);
 					}
 				}
 				for (int j = children.size(); j < nodes.length; j++) {
 					removeNodes(doc, nodes[j]);
 				}
 			}
 		} else {
 			Feature[] features = model.getChildNames();
 			for (Feature f : features) {
 				List<Object> children = model.getChildren(f);
 				Node[] nodes = getNodes(doc, f.xpath);
 				
 				for (int i = 0; i < Math.min(children.size(), nodes.length); i++) {
 					if (nodes[i] != null) {
 						Node node = nodes[i];
 						if (f.type == IModelObject.class) {
 							 writeProperties(node, (IModelObject) children.get(i), event);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private Node getDirectChild(Node parent, Node child) {
 		Node p = child.getParentNode();
 		while (p != parent) {
 			child = p;
 			p = child.getParentNode();
			if (p == null) {
				return null;
			}
 		}
 		
 		return child;
 	}
 	
 	private Node addNode(Node doc, String xpath, Node after) throws XPathExpressionException {
 		if (xpath == null) {
 			return doc;
 		}
 		
 		int idx = xpath.lastIndexOf('/');
 		String name;
 		Node parent = null;
 		if (idx > -1) {
 			String parentXpath = xpath.substring(0, idx);
 			name = xpath.substring(idx + 1);
 		
 			parent = getNode(doc, parentXpath);
 			if (parent == null) {
 				parent = addNode(doc, parentXpath, after);
 			}
 		} else {
 			parent = doc;
 			name = xpath;
 		}
 		
 		Element e = document.createElement(name);
 		if (after == null) { // by default insert at the beginning
 			parent.insertBefore(e, parent.getFirstChild());
 		} else {
 			Node sameLevelAfter = getDirectChild(parent, after);
			
 			if (sameLevelAfter != null) {
 				parent.insertBefore(e, sameLevelAfter.getNextSibling());
 			} else {
 				parent.appendChild(e);
 			}
 		}
 		
 		return e;
 	}
 	
 	/**
 	 * Removes nodes from startnode up the three to parent
 	 * @param parent
 	 * @param node
 	 */
 	private void removeNodes(Node border, Node node) {
 		Node parent = node.getParentNode();
 		do {
 			Node sibling = node.getNextSibling();
 			parent.removeChild(node);
 			
 			// remove text node after the deleted node
 			if ((sibling != null) && (sibling.getNodeType() == Node.TEXT_NODE)) {
 				parent.removeChild(sibling);
 			}
 			
 			node = parent;
 			parent = parent.getParentNode();
 		} while ((getChildCount(node, Node.ELEMENT_NODE) == 0) && (node != border));
 	}
 	
 	/**
 	 * Get the number of children nodes of given type.
 	 * 
 	 * @param parent node which children should be counted
 	 * @param type type of children to count
 	 * @return number of children of given type in parent
 	 */
 	private int getChildCount(Node parent, short type) {
 		int length = 0;
 		NodeList list = parent.getChildNodes();
 		for (int i = 0; i < list.getLength(); i++) {
 			length += (list.item(i).getNodeType() ==type) ? 1 : 0;
 		}
 		return length;
 	}
 	
 	private void removeString(Node node, String xpath, String attrName) throws XPathExpressionException {
 		Node target = node;
 		if (xpath != null) {
 			target = getNode(node, xpath);
 		}
 		
 		if (target == null) { // if node not found, then there's nothing to remove
 			return;
 		}
 		
 		if (attrName == null) {			
 			Node sibling = target.getNextSibling();
 			// remove text node after the deleted node
 			if ((sibling != null) && (sibling.getNodeType() == Node.TEXT_NODE)) {
 				target.getParentNode().removeChild(sibling);
 			}
 			
 			target.getParentNode().removeChild(target);
 		} else {
 			NamedNodeMap attrs = target.getAttributes();
 			if (attrs.getNamedItem(attrName) != null) {
 				attrs.removeNamedItem(attrName);
 			}
 		}
 	}
 	
 	private Node setString(Node node, String xpath, String attrName, String value, Node after) throws XPathExpressionException {
 		Node target = node;
 		if (xpath != null) {
 			target = getNode(node, xpath);
 			if (target == null) {
 				target = addNode(node, xpath, after);
 			}
 		}
 		
 		if (attrName != null) {
 			((Element) target).setAttribute(attrName, value);
 		} else {
 			target.setTextContent(value);
 		}
 		
 		return target;
 	}
 	
 	private static String getXPath(String nodePath, String attrName) {
 		if (nodePath != null && attrName == null) {
 			return nodePath;
 		} else if (nodePath == null && attrName != null) {
 			return AT+attrName;
 		} else {
 			return nodePath + AT+attrName;
 		}
 	}
 	
 	private String getXpathString(Node node, String xpath) throws XPathExpressionException {
 		XPathExpression expr = xpathObj.compile(xpath);
 		String out = (String) expr.evaluate(node, XPathConstants.STRING);
 		return out;
 	}
 	
 	private Node getNode(Node node, String xpath) throws XPathExpressionException {
 		XPathExpression expr = xpathObj.compile(xpath);
 		Node newnode = (Node) expr.evaluate(node, XPathConstants.NODE);
 		return newnode;
 	}
 	
 	private Node[] getNodes(Node node, String xpath) throws XPathExpressionException {
 		XPathExpression expr = xpathObj.compile(xpath);
 		NodeList list = (NodeList) expr.evaluate(node, XPathConstants.NODESET);
 		Node[] result = new Node[list.getLength()];
 		for (int i = 0; i < list.getLength(); i++) {
 			result[i] = list.item(i);
 		}
 		
 		return result;
 	}
 
 	private static String stripWhitespaces(String str) {
 		if (str == null) {
 			return null;
 		}
 		
 		str = str.trim();
 		
 		StringBuilder sb = new StringBuilder(str);
 		boolean isWhiteSpace = false;
 		int lastWhSpcIdx = -1;
 		for (int i = str.length() - 1; i >= 0; i--) {
 			char c = sb.charAt(i);
 			if (c == ' ' || c=='\t' || c=='\n') { // is white space
 				if (!isWhiteSpace) { // whitespace after non-whitespaces
 					lastWhSpcIdx = i;
 				}
 				isWhiteSpace = true;
 			} else if (isWhiteSpace) { // not whitespce, after whitespaces
 				sb.replace(i+1, lastWhSpcIdx + 1, " "); //$NON-NLS-1$
 				isWhiteSpace = false;
 			}
 		}
 		
 		return sb.toString();
 	}
 }
