 /* *****************************************************************************
  * ReprocessComments.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2007 Laszlo Systems, Inc.  All Rights Reserved.                   *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.js2doc;
 
 import java.io.*;
 import java.util.regex.*;
 import java.util.*;
 import java.util.logging.*;
 import javax.xml.parsers.DocumentBuilderFactory;  
 import org.w3c.dom.*;
 
 public class ReprocessComments {
 
     static private Logger logger = Logger.getLogger("org.openlaszlo.js2doc");
     
     static void appendToAttribute(org.w3c.dom.Element node, String attr, String value)
     {
         String oldvalue = node.getAttribute(attr);
         if (oldvalue == null || oldvalue.length() == 0)
             node.setAttribute(attr, value.trim());
         else
             node.setAttribute(attr, oldvalue + " " + value.trim());
     }
     
     static private abstract class CommentFieldFilter {
         CommentFieldFilter nextFilter;
         
         public CommentFieldFilter(CommentFieldFilter nextFilter) {
             this.nextFilter = nextFilter;
         }
         
         public CommentFieldFilter() {
             this(null);
         }
         
         public boolean handle(org.w3c.dom.Element node, String fieldName, String fieldValue) {
             CommentFieldFilter f = this;
             while (f != null) {
                 if (f.handleField(node, fieldName, fieldValue) == true)
                     return true;
                 f = f.nextFilter;
             }
             return false;
         }
         
         abstract protected boolean handleField(org.w3c.dom.Element node, String fieldName, String fieldValue);
     };
     
     static private abstract class CommentKeywordFilter {
         CommentKeywordFilter nextFilter;
         
         public CommentKeywordFilter(CommentKeywordFilter nextFilter) {
             this.nextFilter = nextFilter;
         }
         
         public CommentKeywordFilter() {
             this(null);
         }
         
         public boolean handle(org.w3c.dom.Element node, String keywordName) {
             CommentKeywordFilter f = this;
             while (f != null) {
                 if (f.handleKeyword(node, keywordName) == true)
                     return true;
                 f = f.nextFilter;
             }
             return false;
         }
         
         abstract protected boolean handleKeyword(org.w3c.dom.Element node, String keywordName);
     };
     
     static private class TopicFieldFilter extends CommentFieldFilter {
     
         public boolean handleField(org.w3c.dom.Element node, String fieldName, String fieldValue) {
             if (fieldName.equals("topic")) {
                 node.setAttribute("topic", fieldValue.trim());
                 return true;
             } else if (fieldName.equals("subtopic")) {
                 node.setAttribute("subtopic", fieldValue.trim());
                 return true;
             }
             return false;
         }
     };
     
     static private class AccessFieldFilter extends CommentFieldFilter {
     
         public AccessFieldFilter(CommentFieldFilter nextFilter) {
             super(nextFilter);
         }
         
         public AccessFieldFilter() {
             super();
         }
         
         public boolean handleField(org.w3c.dom.Element node, String fieldName, String fieldValue) {
             fieldValue = fieldValue.trim();
             if (fieldName.equals("access")) {
                 StringTokenizer keywordTokens = new StringTokenizer(fieldValue);
                 String keyword = keywordTokens.nextToken();
                 if (keyword.equals("private") ||
                     keyword.equals("public") ||
                     keyword.equals("protected")) {
                     node.setAttribute("access", keyword);
                     return true;
                 } else {
                     logger.warning("Invalid @access keyword: '" + keyword + "'");
                 }
             } else if (fieldName.equals("runtimes")) {
               node.setAttribute("runtimes", fieldValue);
               return true;
             }
             return false;
         }
     };
     
     static private class TypeFieldFilter extends CommentFieldFilter {
         public TypeFieldFilter(CommentFieldFilter nextFilter) {
             super(nextFilter);
         }
         
         public TypeFieldFilter() {
             super();
         }
         
         public boolean handleField(org.w3c.dom.Element node, String fieldName, String fieldValue) {
             fieldValue = fieldValue.trim();
             if (fieldName.equals("type")) {
                 StringTokenizer keywordTokens = new StringTokenizer(fieldValue);
                 String type = keywordTokens.nextToken();
                 node.setAttribute("type", type);
                 return true;
             }
             return false;
         }
     };
     
     static private class ModifiersCommentKeywordFilter extends CommentKeywordFilter {
     
         public ModifiersCommentKeywordFilter(CommentKeywordFilter nextFilter) {
             super(nextFilter);
         }
         
         public ModifiersCommentKeywordFilter() {
             super();
         }
         
         public boolean handleKeyword(org.w3c.dom.Element node, String keywordName) {
             // TODO [jgrandy 11/23/06] eventually this use of @keywords should be deprecated
             if (keywordName.equals("private") ||
                 keywordName.equals("public") ||
                 keywordName.equals("protected")) {
                 
                 node.setAttribute("access", keywordName);
                 return true;
                 
             } else if (keywordName.equals("final") ||
                       keywordName.equals("virtual") ||
                       keywordName.equals("deprecated") ||
                       keywordName.equals("override") ||
                       keywordName.equals("abstract") ||
                       keywordName.equals("const")) {
                       
                 appendToAttribute(node, "modifiers", keywordName);
                 return true;
 
             } else if (keywordName.equals("read-only") ||
                        keywordName.equals("readonly")) {
                 appendToAttribute(node, "modifiers", "read-only");
                 return true;
             }
             return false;
         }
     };
     
     static private class ParamFieldFilter extends CommentFieldFilter {
     
         // TODO [jgrandy 12/14/2006] use Java5's Scanner instead of regexp here
         
        static private final Pattern paramPattern  = Pattern.compile("^\\s*(?:(\\w+)\\s+)?(\\w*)(?::(.*))?$", Pattern.DOTALL);
        static private final Pattern returnPattern = Pattern.compile("^\\s*(\\w+)(?::(.*))?$", Pattern.DOTALL);
     
         public ParamFieldFilter(CommentFieldFilter nextFilter) {
             super(nextFilter);
         }
         
         public boolean handleField(org.w3c.dom.Element node, String fieldName, String fieldValue) {
             boolean handled = false;
             
             if (fieldName.equals("param")) {
                 Matcher valueMatcher = paramPattern.matcher(fieldValue);
                 boolean found = valueMatcher.find();
                 if (found) {
                     String paramType = valueMatcher.group(1),
                            paramName = valueMatcher.group(2),
                            paramDesc = valueMatcher.group(3);
                     
                     // now find the appropriate parameter node in the DOM node
                     String tagName = node.getTagName();
                     org.w3c.dom.Element functionNode = 
                         (tagName.equals("function"))
                             ? node
                             : (org.w3c.dom.Element) firstChildNodeWithName(node, "function");
                     
                     if (functionNode == null) {
                         logger.warning("Couldn't find function node when placing parameter info " + node.getTagName());
                     } else {
                         org.w3c.dom.Element childElt = JS2DocUtils.findFirstChildElementWithAttribute(functionNode, "parameter", "name", paramName);
                         if (childElt == null) {
                             logger.warning("Couldn't find parameter named " + paramName);
                         } else {
                             if (paramType != null) {
                                 // TODO [jgrandy 12/1/2006] check if @type already set
                                 childElt.setAttribute("type", paramType);
                             }
                             
                             paramDesc = (paramDesc != null) ? paramDesc.trim() : null;
                             if (paramDesc != null && paramDesc.length() > 0) {
                                 org.w3c.dom.Element docNode = (org.w3c.dom.Element) JS2DocUtils.firstChildNodeWithName(childElt, "doc");
                                 if (docNode == null) {
                                     docNode = childElt.getOwnerDocument().createElement("doc");
                                     childElt.appendChild(docNode);
                                 }
                                 org.w3c.dom.Element textNode = childElt.getOwnerDocument().createElement("text");                
                                 docNode.appendChild(textNode);
                                 JS2DocUtils.setXMLContent(textNode, paramDesc);
                             }
                             
                             handled = true;
                         }
                     }
                 }
             } 
             else if (fieldName.equals("return") || fieldName.equals("returns")) {
                 Matcher valueMatcher = returnPattern.matcher(fieldValue);
                 boolean found = valueMatcher.find();
                 if (found) {
                     String paramType = valueMatcher.group(1),
                            paramDesc = valueMatcher.group(2);
                     
                     org.w3c.dom.Element functionNode = 
                         (node.getTagName().equals("function"))
                             ? node
                             : (org.w3c.dom.Element) firstChildNodeWithName(node, "function");
                     
                     if (functionNode == null) {
                         logger.warning("Couldn't find function node when placing returns info");
                     } else {
                         // now find/create the appropriate returns node in the DOM node
                         org.w3c.dom.Element returnsNode = 
                             (org.w3c.dom.Element) JS2DocUtils.firstChildNodeWithName(functionNode, "returns");
     
                         if (returnsNode == null) {
                             returnsNode = functionNode.getOwnerDocument().createElement("returns");
                             functionNode.appendChild(returnsNode);
                         }
                                 
                         if (paramType != null) {
                             // TODO [jgrandy 12/1/2006] check if @type already set
                             returnsNode.setAttribute("type", paramType);
                         }
                         
                         paramDesc = (paramDesc != null) ? paramDesc.trim() : null;
                         if (paramDesc != null && paramDesc.length() > 0) {
                             org.w3c.dom.Element docNode = (org.w3c.dom.Element) JS2DocUtils.firstChildNodeWithName(returnsNode, "doc");
                             if (docNode == null) {
                                 docNode = returnsNode.getOwnerDocument().createElement("doc");
                                 returnsNode.appendChild(docNode);
                             }
                             org.w3c.dom.Element textNode = returnsNode.getOwnerDocument().createElement("text");
                             docNode.appendChild(textNode);
                             JS2DocUtils.setXMLContent(textNode, paramDesc);
                         }
                         handled = true;
                     }                            
                 }
             }
             return handled;
         }
     };
     
 
     static private class InitializerFieldFilter extends CommentFieldFilter {
     
         // TODO [jgrandy 12/14/2006] use Java5's Scanner instead of regexp here
         
         static private final Pattern initializerPattern = Pattern.compile("^\\s*(?:(\\w+)\\s+)?(?:(\\w+)\\s+)?(\\w*)(?::(.*))?$", Pattern.DOTALL);
     
         public InitializerFieldFilter(CommentFieldFilter nextFilter) {
             super(nextFilter);
         }
         
         public boolean handleField(org.w3c.dom.Element node, String fieldName, String fieldValue) {
             boolean handled = false;
             
             if (fieldName.equals("initarg")) {
                 Matcher valueMatcher = initializerPattern.matcher(fieldValue);
                 boolean found = valueMatcher.find();
                 if (found) {
 
                     String initializerMod1 = valueMatcher.group(1),
                            initializerMod2 = valueMatcher.group(2),
                            initializerName = valueMatcher.group(3),
                            initializerDesc = valueMatcher.group(4);
                     
                     org.w3c.dom.Element classNode = 
                         (node.getTagName().equals("class"))
                             ? node
                             : (org.w3c.dom.Element) firstChildNodeWithName(node, "class");
                     
                     if (classNode == null) {
                         logger.warning("Couldn't find class node when placing initarg info");
                     } else {
 
                         org.w3c.dom.Element initializerNode = classNode.getOwnerDocument().createElement("initarg");
                         classNode.appendChild(initializerNode);
                         
                         initializerNode.setAttribute("name", initializerName);
     
                         initializerNode.setAttribute("id", JS2DocUtils.derivePropertyID(classNode, initializerName, null));
     
                         if (initializerMod2 != null) {
                             String modifier = initializerMod1, 
                                        type = initializerMod2;
                             if (modifier != null) {
                                 if (modifier.equals("public") || 
                                     modifier.equals("private") ||
                                     modifier.equals("protected")) {
                                     initializerNode.setAttribute("access", modifier);
                                 } else
                                     appendToAttribute(initializerNode, "modifiers", initializerMod1);
                             }
                             initializerNode.setAttribute("type", initializerMod2);
                         } else if (initializerMod1 != null) {
                             String type = initializerMod1;                    
                             initializerNode.setAttribute("type", initializerMod1);
                         }
                         
                         initializerDesc = (initializerDesc != null) ? initializerDesc.trim() : null;
                         if (initializerDesc != null && initializerDesc.length() > 0) {
                             org.w3c.dom.Element docNode = (org.w3c.dom.Element) JS2DocUtils.firstChildNodeWithName(initializerNode, "doc");
                             if (docNode == null) {
                                 docNode = initializerNode.getOwnerDocument().createElement("doc");
                                 initializerNode.appendChild(docNode);
                             }
                             org.w3c.dom.Element textNode = docNode.getOwnerDocument().createElement("text");                
                             docNode.appendChild(textNode);
                             JS2DocUtils.setXMLContent(textNode, initializerDesc);
                         }
                         
                         handled = true;
                     }
                 }
             } 
             return handled;
         }
     };
     
     static private CommentFieldFilter toplevelDeclFieldFilter = new TypeFieldFilter(new AccessFieldFilter(new TopicFieldFilter()));
     static private CommentKeywordFilter toplevelDeclKeywordFilter = new ModifiersCommentKeywordFilter();
 
     static private CommentFieldFilter functionCommentFieldFilter = new ParamFieldFilter(new AccessFieldFilter());
 
     static private CommentFieldFilter classFieldFilter = new InitializerFieldFilter(new AccessFieldFilter(new TopicFieldFilter()));
 
     static private CommentFieldFilter memberCommentFieldFilter = new TypeFieldFilter(new AccessFieldFilter());
     static private CommentKeywordFilter memberCommentKeywordFilter = new ModifiersCommentKeywordFilter();
     
     static private CommentFieldFilter classMethodCommentFieldFilter = new ParamFieldFilter(new AccessFieldFilter());
 
     static private Node firstChildNodeWithName(Node node, String name) {
         Node foundNode = null;
         NodeList childNodes = node.getChildNodes();
         final int n = childNodes.getLength();
         for (int i=0; i<n; i++) {
             Node childNode = childNodes.item(i);
             if (childNode.getNodeName().equals(name)) {
                 foundNode = childNode;
                 break;
             }
         }
         return foundNode;
     }
     
     static private final Pattern contentsPattern = Pattern.compile("^<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?><text>(.*)</text>$", Pattern.DOTALL);
 
     static private void hoistDocTags(Node node, Element docNode, CommentFieldFilter fieldFilter, CommentKeywordFilter keywordFilter) {
         NodeList childNodes = docNode.getChildNodes();
         int n = childNodes.getLength();
         for (int i=0; i<n; i++) {
             org.w3c.dom.Node childNode = childNodes.item(i);
             if (childNode.getNodeName().equals("tag")) {
                 Element childElt = (Element) childNode;
                 String fieldName = childElt.getAttribute("name");
 
                 Element fieldText = (Element) firstChildNodeWithName(childNode, "text");
                 
                 // We can't use getTextContents here because fieldText is a tree
                 // fragment with XML substructure. So reconstitute as an XML
                 // string and let the fieldFilter reparse it.
                 String fieldValue = JS2DocUtils.xmlToString(fieldText);
                 
                 // xmlToString returns as well-formed XML document string, so
                 // strip back to be just the fragment contents.
                 fieldValue = contentsPattern.matcher(fieldValue).replaceAll("$1");
                 
                 final boolean handled = fieldFilter.handle((Element) node, fieldName, fieldValue);
 
                 if (handled == true) {
                     docNode.removeChild(childNode);
                     n--;
                     i--;
                 }
             }
         }
         
         String keywords = docNode.getAttribute("keywords");
         if (keywords == null)
             keywords = docNode.getAttribute("keyword");
         if (keywords != null) {
             docNode.removeAttribute("keywords");
             StringTokenizer keywordTokens = new StringTokenizer(keywords);
             while (keywordTokens.hasMoreTokens()) {
                 String keyword = keywordTokens.nextToken();
                 final boolean handled = keywordFilter.handle((Element) node, keyword);
                 if (handled == false)
                     appendToAttribute(docNode, "keywords", keyword);
             }
         }
 
         if (docNode.hasChildNodes() == false &&
             docNode.hasAttributes() == false) {
             node.removeChild(docNode);
         }
     }
     
     static private void reprocessElement(Node node, CommentFieldFilter fieldFilter, CommentKeywordFilter keywordFilter) {
         
         org.w3c.dom.Element doc = (org.w3c.dom.Element) JS2DocUtils.firstChildNodeWithName(node, "doc");
         if (doc != null) {
             hoistDocTags(node, doc, fieldFilter, keywordFilter);
         }
         
         org.w3c.dom.Element commentNode = (org.w3c.dom.Element) JS2DocUtils.firstChildNodeWithName(node, "comment");
         if (commentNode != null) {
             String commentText = commentNode.getTextContent();
             node.removeChild(commentNode);
             Comment comment = Comment.extractJS2DocComment(commentText);
             org.w3c.dom.Element docNode = comment.appendAsXML(node);
             hoistDocTags(node, docNode, fieldFilter, keywordFilter);
         }
     }
     
     static public void reprocessChildElements(Node node, boolean isTopLevel) {
         NodeList childNodes = node.getChildNodes();
         final int n = childNodes.getLength();
         for (int i=0; i<n; i++) {
             Node childNode = childNodes.item(i);
             reprocess(childNode, isTopLevel);
         }
     }
     
     static public void reprocess(Node node, boolean isTopLevel) {
     
         String nodeName = node.getNodeName();
 
         if (nodeName.equals("js2doc")) {
             if (isTopLevel == false)
                 logger.warning("unexpected js2doc inside declaration");
             reprocessChildElements(node, true);
         }
         else if (nodeName.equals("unit")) {
             if (isTopLevel == false)
                 logger.warning("unexpected unit inside declaration");
             reprocessElement(node, toplevelDeclFieldFilter, toplevelDeclKeywordFilter);
         }
         else if (nodeName.equals("ivars")) {
             if (isTopLevel == true)
                 logger.warning("unexpected ivars outside class decl");
             reprocessChildElements(node, false);
         }
         else if (nodeName.equals("property")) {
             // todo: maybe slightly cleaner to iterate over all child nodes and
             // switch on each one?
             Node child = null;
             if ((child = firstChildNodeWithName(node, "function")) != null) {
                 reprocessElement(node, 
                                  functionCommentFieldFilter, 
                                  isTopLevel ? toplevelDeclKeywordFilter : memberCommentKeywordFilter);
             } else if ((child = firstChildNodeWithName(node, "class")) != null) {        
                 reprocessElement(node, 
                                  classFieldFilter, 
                                  isTopLevel ? toplevelDeclKeywordFilter : memberCommentKeywordFilter);
             } else if ((child = firstChildNodeWithName(node, "object")) != null) {
                 reprocessElement(node, 
                                  isTopLevel ? toplevelDeclFieldFilter : memberCommentFieldFilter, 
                                  isTopLevel ? toplevelDeclKeywordFilter : memberCommentKeywordFilter);
             } else if ((child = firstChildNodeWithName(node, "initarg")) != null) {
                 reprocessElement(node, 
                                  isTopLevel ? toplevelDeclFieldFilter : memberCommentFieldFilter, 
                                  isTopLevel ? toplevelDeclKeywordFilter : memberCommentKeywordFilter);
             } else {
                 // todo: what case is this handling?
                 reprocessElement(node, 
                                  isTopLevel ? toplevelDeclFieldFilter : memberCommentFieldFilter, 
                                  isTopLevel ? toplevelDeclKeywordFilter : memberCommentKeywordFilter);
             }
             if (child != null)
                 reprocessChildElements(child, false);
         }
     }
 
     static public Document reprocess(String contents) {
     
         org.w3c.dom.Document doc = null;
         
         try {
 
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(false);
             doc = factory.newDocumentBuilder().parse( new org.xml.sax.InputSource(new StringReader(contents)) );
 
             reprocessChildElements(doc, true);
             
         } catch (java.io.IOException e) {
             logger.warning("Could not read string");
             e.printStackTrace();
         } catch (javax.xml.parsers.ParserConfigurationException e) {
             logger.warning("Could not parse xml input");
             e.printStackTrace();
         } catch (org.xml.sax.SAXException e) {
             logger.warning("Could not parse xml input");
             e.printStackTrace();
         }
             
         return doc;
     }
 
 }
