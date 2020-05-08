 /* *****************************************************************************
  * XMLUtils.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2006-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.js2doc;
 
 import java.util.*;
 import java.io.*;
 import java.util.logging.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import javax.xml.parsers.DocumentBuilderFactory;  
 import org.custommonkey.xmlunit.*;
 import org.openlaszlo.sc.parser.*;
 import org.w3c.dom.*;
 
 public class JS2DocUtils {
 
     static private Logger logger = Logger.getLogger("org.openlaszlo.js2doc");
 
     static class InternalError extends RuntimeException {
 
         SimpleNode node;
         
         /** Constructs an instance.
          * @param message a string
          * @param node offending parse node
          */
         public InternalError(String message, SimpleNode node) {
             super(message);
             this.node = node;
         }
     }
 
 
     public static String xmlToString(org.w3c.dom.Node node) throws RuntimeException {
         String stringResult = null;
         try {
             Source source = new DOMSource(node);
             StringWriter stringWriter = new StringWriter();
             Result result = new StreamResult(stringWriter);
             TransformerFactory factory = TransformerFactory.newInstance();
             Transformer transformer = factory.newTransformer();
             transformer.transform(source, result);
             stringResult = stringWriter.getBuffer().toString();
         } catch (TransformerConfigurationException e) {
             e.printStackTrace();
             throw new RuntimeException("Unable to instantiate XML Transformer");
         } catch (TransformerException e) {
             e.printStackTrace();
             throw new RuntimeException("Error in XML Transformer");
         }
         return stringResult;
     }
 
     public static void xmlToFile(org.w3c.dom.Node node, String filename) throws RuntimeException {
         try {
             // Prepare the DOM document for writing
             Source source = new DOMSource(node);
     
             // Prepare the output file
             File file = new File(filename);
             Result result = new StreamResult(file);
     
             // Write the DOM document to the file
             Transformer xformer = TransformerFactory.newInstance().newTransformer();
             xformer.transform(source, result);
         } catch (TransformerConfigurationException e) {
             e.printStackTrace();
             throw new RuntimeException("Unable to instantiate XML Transformer");
         } catch (TransformerException e) {
             e.printStackTrace();
             throw new RuntimeException("Error in XML Transformer");
         }
     }
 
 
     static public void setXMLContent(org.w3c.dom.Element node, String content) {
 
         // Wrap the fragment in an arbitrary element
         content = "<fragment>"+content+"</fragment>";
         
         try {
             // Create a DOM builder and parse the fragment
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(false);
             Document d = factory.newDocumentBuilder().parse( new org.xml.sax.InputSource(new StringReader(content)) );
             
             // Import the nodes of the new document into doc so that they
             // will be compatible with doc
             Document doc = node.getOwnerDocument();
             org.w3c.dom.Node fragNode = doc.importNode(d.getDocumentElement(), true);
     
             // Create the document fragment node to hold the new nodes
             DocumentFragment docfrag = doc.createDocumentFragment();
     
             // Move the nodes into the fragment
             while (fragNode.hasChildNodes()) {
                 docfrag.appendChild(fragNode.removeChild(fragNode.getFirstChild()));
             }
             
             node.appendChild(docfrag);
         } catch (java.io.IOException e) {
             logger.warning("Could not parse comment '" + content + "'");
             e.printStackTrace();
         } catch (javax.xml.parsers.ParserConfigurationException e) {
             logger.warning("Could not parse comment '" + content + "'");
             e.printStackTrace();
         } catch (org.xml.sax.SAXException e) {
             logger.warning("Could not parse comment '" + content + "'");
             e.printStackTrace();
         }
     }
 
     static public org.w3c.dom.Element findFirstChildElementWithAttribute(org.w3c.dom.Element docNode, String tagName, String attrName, String name) {
         org.w3c.dom.Element foundNode = null;
         org.w3c.dom.NodeList childNodes = docNode.getChildNodes();
         final int n = childNodes.getLength();
         for (int i=0; i<n; i++) {
             org.w3c.dom.Node childNode = childNodes.item(i);
             if (childNode instanceof org.w3c.dom.Element && childNode.getNodeName().equals(tagName)) {
                 String eName = ((org.w3c.dom.Element) childNode).getAttribute(attrName);
                 if (eName != null && eName.equals(name)) {
                     foundNode = (org.w3c.dom.Element) childNode;
                     break;
                 }
             }
         }
         return foundNode;
     }
     
     static public org.w3c.dom.Node firstChildNodeWithName(org.w3c.dom.Node node, String name) {
         org.w3c.dom.Node foundNode = null;
         org.w3c.dom.NodeList childNodes = node.getChildNodes();
         final int n = childNodes.getLength();
         for (int i=0; i<n; i++) {
             org.w3c.dom.Node childNode = childNodes.item(i);
             if (childNode.getNodeName().equals(name)) {
                 foundNode = childNode;
                 break;
             }
         }
         return foundNode;
     }
     
     static void appendToAttribute(org.w3c.dom.Element node, String attr, String value)
     {
         String oldvalue = node.getAttribute(attr);
         if (oldvalue == null || oldvalue.length() == 0)
             node.setAttribute(attr, value.trim());
         else
             node.setAttribute(attr, oldvalue + " " + value.trim());
     }
 
     static void describeConditionalState(ConditionalState state, org.w3c.dom.Element docNode) {
         if (state.inferredValue == ConditionalState.indeterminateValue) {
             Set includeSet = new HashSet();
             Set excludeSet = new HashSet();
             
             state.describeExclusiveConditions(includeSet);
             
             if (includeSet.isEmpty() == false) {
                 docNode.setAttribute("runtimes", optionsToString(includeSet));
             }
             
             includeSet.clear();
             
             state.describeIndependentConditions(includeSet, excludeSet);
 
             if (includeSet.isEmpty() == false) {
                 docNode.setAttribute("includebuilds", optionsToString(includeSet));
             }
             
             if (excludeSet.isEmpty() == false) {
                 docNode.setAttribute("excludebuilds", optionsToString(excludeSet));
             }
         }
     }
 
     static ConditionalState conditionalStateFromElement(org.w3c.dom.Element propertyOwner, Set exclusiveOptions, List independentOptions) {
         String runtimes = propertyOwner.getAttribute("runtimes");
         String includebuilds = propertyOwner.getAttribute("includebuilds");
         String excludebuilds = propertyOwner.getAttribute("excludebuilds");
         
         ConditionalState newState = new ConditionalState(ConditionalState.trueValue, exclusiveOptions, independentOptions);
         
         if (runtimes != null) {
             Scanner sc = new Scanner(runtimes);
             while (sc.hasNext()) {
                 String option = sc.next();
                 newState.addTrueCase(option);
             }
         }
         if (includebuilds != null) {
             Scanner sc = new Scanner(includebuilds);
             while (sc.hasNext()) {
                 String option = sc.next();
                 newState.addTrueCase(option);
             }
         }
         if (excludebuilds != null) {
             Scanner sc = new Scanner(excludebuilds);
             while (sc.hasNext()) {
                 String option = sc.next();
                 newState.addFalseCase(option);
             }
         }
         
         return newState;
     }
     
 
     static String derivePropertyID(org.w3c.dom.Node propertyOwner, String propertyName, ConditionalState state) {
         String objectID = null;
         org.w3c.dom.Node parentNode = propertyOwner;
         while (parentNode != null && parentNode instanceof org.w3c.dom.Element) {
             org.w3c.dom.Element ownerBinding = (org.w3c.dom.Element) parentNode;
             if (ownerBinding.hasAttribute("id")) {
                 objectID = ownerBinding.getAttribute("id");
                 break;
             }
             parentNode = parentNode.getParentNode();
         }
         String classPrefix = (objectID != null) ? (objectID + ".") : "";
         String conditionSuffix = "";
         if (state != null && state.inferredValue == ConditionalState.indeterminateValue)
            conditionSuffix = state.toString();
         return classPrefix + propertyName + conditionSuffix;
     }
     
     static String optionsToString(Collection options) {
         List c = new ArrayList(options);
         Collections.sort(c);
         String s = "";
         for (Iterator iter = c.iterator(); iter.hasNext();) {
             if (s != "") s += " ";
             s += (String) iter.next();
         }
         return s;
     }
     
     static String nodeLocationInfo(SimpleNode parseNode) {
         return parseNode.getFilename() + ": " + parseNode.getLineNumber() + ", " + parseNode.getColumnNumber();
     }
     
     static String nodeDescription(SimpleNode parseNode) {
         if (parseNode instanceof ASTIdentifier) {
             return ((ASTIdentifier) parseNode).getName();
         } else if (parseNode instanceof ASTLiteral) {
             return ((ASTLiteral) parseNode).getValue().toString();
         } else
             return "";
     }
     
     static void debugPrintNode(SimpleNode parseNode) {
         debugPrintNode(parseNode, 0, 0);
     }
     
     static void debugPrintNode(SimpleNode parseNode, int level, int index) {
         String c = parseNode.getComment();
         System.err.println("node: (" + level + ">>" + index + ") " + parseNode.getClass().getName() + " "
                             + nodeDescription(parseNode)
                             + " (" + nodeLocationInfo(parseNode) + ")");
         SimpleNode[] children = parseNode.getChildren();
         for (int i = 0; i < children.length; i++) {
             debugPrintNode(children[i], level + 1, i);
         }
     }
 
     static void checkChildrenLowerBounds(SimpleNode node, int min, int expectedMax, String methodName) {
         SimpleNode[] children = node.getChildren();
         if (children.length < min) {
             logger.throwing("JS2Doc", methodName, new InternalError("Too few child nodes in " + node.getClass().getName(), node));
         } else if (expectedMax > 0 && children.length > expectedMax) {
            logger.warning("Unexpected number of child nodes in " + node.getClass().getName());
         }
     }
 
 }
