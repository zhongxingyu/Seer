 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: XmlSource.java,v 1.12 2002-12-30 18:04:43 shahid.shah Exp $
  */
 
 package com.netspective.sparx.util.xml;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Arrays;
 
 import javax.servlet.ServletContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.xpath.XPathAPI;
 import org.apache.oro.text.regex.*;
 import org.apache.oro.text.perl.Perl5Util;
 import org.apache.commons.jexl.Expression;
 import org.apache.commons.jexl.ExpressionFactory;
 import org.apache.commons.jexl.JexlContext;
 import org.apache.commons.jexl.JexlHelper;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.netspective.sparx.util.metric.Metric;
 import com.netspective.sparx.util.ClassPath;
 
 public class XmlSource
 {
     public class SourceInfo
     {
         protected File source;
         protected long lastModified;
         protected SourceInfo parent;
         protected List preProcessors;
 
         public SourceInfo(SourceInfo includedFrom, File file)
         {
             source = file;
             lastModified = source.lastModified();
             parent = includedFrom;
         }
 
         public File getFile()
         {
             return source;
         }
 
         public SourceInfo getParent()
         {
             return parent;
         }
 
         public List getPreProcessors()
         {
             return preProcessors;
         }
 
         public void addPreProcessor(SourceInfo value)
         {
             if(preProcessors == null) preProcessors = new ArrayList();
             preProcessors.add(value);
         }
 
         public boolean sourceChanged()
         {
             if(source.lastModified() > this.lastModified)
                 return true;
 
             if(preProcessors != null)
             {
                 for(int i = 0; i < preProcessors.size(); i++)
                 {
                     if(((SourceInfo) preProcessors.get(i)).sourceChanged())
                         return true;
                 }
             }
 
             return false;
         }
     }
 
     protected boolean allowReload = true;
     protected ArrayList errors = new ArrayList();
     protected SourceInfo docSource;
     protected Map sourceFiles = new HashMap();
     protected Document xmlDoc;
     protected Element metaInfoElem;
     protected Element metaInfoOptionsElem;
     protected Set inheritanceHistorySet = new HashSet();
     protected Set defaultExcludeElementsFromInherit = new HashSet();
     protected Map templates = new HashMap();
     protected String catalogedNodeIdentifiersClassName;
 
     public static void defineClassAttributes(Element defnElement, Class cls, String attrPrefix)
     {
         if(cls == null)
             return;
 
         String className = cls.getName();
         String classFileName = ClassPath.getClassFileName(className);
 
         defnElement.setAttribute(attrPrefix + "class-name", className);
         defnElement.setAttribute(attrPrefix + "class-file-name", classFileName);
 
         String classSrcName = classFileName.substring(0, classFileName.lastIndexOf('.')) + ".java";
         if(new File(classSrcName).exists())
             defnElement.setAttribute(attrPrefix + "class-src-name", classSrcName);
     }
 
     /**
      * Return the given text unindented by whatever the first line is indented by
      * @param text The original text
      * @return Unindented text or original text if not indented
      */
     public static String getUnindentedText(String text)
     {
         /*
          * if the entire SQL string is indented, find out how far the first line is indented
          */
         StringBuffer replStr = new StringBuffer();
         for(int i = 0; i < text.length(); i++)
         {
             char ch = text.charAt(i);
             if(Character.isWhitespace(ch))
                 replStr.append(ch);
             else
                 break;
         }
 
         /*
          * If the first line is indented, unindent all the lines the distance of just the first line
          */
         Perl5Util perlUtil = new Perl5Util();
 
         if(replStr.length() > 0)
             return perlUtil.substitute("s/" + replStr + "/\n/g", text).trim();
         else
             return text;
     }
 
     /**
      * Return the given text indented by the given string
      * @param text The original text
      * @return Unindented text or original text if not indented
      */
     public static String getIndentedText(String text, String indent, boolean appendNewLine)
     {
         text = getUnindentedText(text);
 
         /*
          * If the first line is indented, unindent all the lines the distance of just the first line
          */
         Perl5Util perlUtil = new Perl5Util();
         text = perlUtil.substitute("s/^/"+ indent +"/gm", text);
         return appendNewLine ? text + "\n" : text;
     }
 
     public boolean getAllowReload()
     {
         return allowReload;
     }
 
     public void setAllowReload(boolean value)
     {
         allowReload = value;
     }
 
     public void initializeForServlet(ServletContext servletContext)
     {
         if(com.netspective.sparx.util.config.ConfigurationManagerFactory.isProductionOrTestEnvironment(servletContext))
             setAllowReload(false);
     }
 
     /**
      * returns the boolean equivalent of a string, which is considered true
      * if either "on", "true", or "yes" is found, ignoring case.
      */
     public static boolean toBoolean(String s)
     {
         return (s.equalsIgnoreCase("on") ||
                 s.equalsIgnoreCase("true") ||
                 s.equalsIgnoreCase("yes"));
     }
 
     /**
      * Given a text string, return a string that would be suitable for that string to be used
      * as a Java identifier (as a variable or method name). Depending upon whether ucaseInitial
      * is set, the string starts out with a lowercase or uppercase letter. Then, the rule is
      * to convert all periods into underscores and title case any words separated by
      * underscores. This has the effect of removing all underscores and creating mixed case
      * words. For example, Person_Address becomes personAddress or PersonAddress depending upon
      * whether ucaseInitial is set to true or false. Person.Address would become Person_Address.
      */
     public static String xmlTextToJavaIdentifier(String xml, boolean ucaseInitial)
     {
         if(xml == null || xml.length() == 0)
             return xml;
 
         StringBuffer identifier = new StringBuffer();
         char ch = xml.charAt(0);
         identifier.append(ucaseInitial ? Character.toUpperCase(ch) : Character.toLowerCase(ch));
 
         boolean uCase = false;
         for(int i = 1; i < xml.length(); i++)
         {
             ch = xml.charAt(i);
             if(ch == '.')
             {
                 identifier.append('_');
             }
             else if(ch != '_' && Character.isJavaIdentifierPart(ch))
             {
                 identifier.append(Character.isUpperCase(ch) ? ch : (uCase ? Character.toUpperCase(ch) : Character.toLowerCase(ch)));
                 uCase = false;
             }
             else
                 uCase = true;
         }
         return identifier.toString();
     }
 
     /**
      * Given a text string, return a string that would be suitable for that string to be used
      * as a Java constant (public static final XXX). The rule is to basically take every letter
      * or digit and return it in uppercase and every non-letter or non-digit as an underscore.
      */
     public static String xmlTextToJavaConstant(String xml)
     {
         if(xml == null || xml.length() == 0)
             return xml;
 
         StringBuffer constant = new StringBuffer();
         for(int i = 0; i < xml.length(); i++)
         {
             char ch = xml.charAt(i);
             constant.append(Character.isJavaIdentifierPart(ch) ? Character.toUpperCase(ch) : '_');
         }
         return constant.toString();
     }
 
     /**
      * Given a text string, return a string that would be suitable for that string to be used
      * as a Java constant (public static final XXX). The rule is to basically take every letter
      * or digit and return it in uppercase and every non-letter or non-digit as an underscore.
      * This trims all non-letter/digit characters from the beginning of the string.
      */
     public static String xmlTextToJavaConstantTrimmed(String xml)
     {
         if(xml == null || xml.length() == 0)
             return xml;
 
         boolean stringStarted = false;
         StringBuffer constant = new StringBuffer();
         for(int i = 0; i < xml.length(); i++)
         {
             char ch = xml.charAt(i);
             if(Character.isJavaIdentifierPart(ch))
             {
                 stringStarted = true;
                 constant.append(Character.toUpperCase(ch));
             }
             else if(stringStarted)
                 constant.append('_');
         }
         return constant.toString();
     }
 
     /**
      * Given a text string, return a string that would be suitable for an XML element name. For example,
      * when given Person_Address it would return person-address. The rule is to basically take every letter
      * or digit and return it in lowercase and every non-letter or non-digit as a dash.
      */
     public static String xmlTextToNodeName(String xml)
     {
         if(xml == null || xml.length() == 0)
             return xml;
 
         StringBuffer constant = new StringBuffer();
         for(int i = 0; i < xml.length(); i++)
         {
             char ch = xml.charAt(i);
             constant.append(Character.isLetterOrDigit(ch) ? Character.toLowerCase(ch) : '-');
         }
         return constant.toString();
     }
 
     /**
      * Given a attribute or tag name, find the item or return the defaultText if there is none.
      */
     public static String getAttrValueOrTagText(Element parent, String name, String defaultText)
     {
         String attrValue = parent.getAttribute(name);
         return attrValue.length() > 0 ? attrValue : getTagText(parent, name, defaultText);
     }
 
     /**
      * Given a attribute or tag name, find the item or return the defaultValue
      */
     public static String getAttrValueOrDefault(Element parent, String name, String defaultValue)
     {
         String attrValue = parent.getAttribute(name);
         return attrValue.length() > 0 ? attrValue : getTagText(parent, name, defaultValue);
     }
 
     /**
      * Given an attribute name, check to see if it has a value -- if not, set it to default
      */
     public static void setAttrValueDefault(Element parent, String name, String defaultValue)
     {
         String attrValue = parent.getAttribute(name);
         if(attrValue.length() == 0 && defaultValue != null && defaultValue.length() > 0)
             parent.setAttribute(name, defaultValue);
     }
 
     public static Element getOrCreateElement(Element parent, String name)
     {
         NodeList nl = parent.getElementsByTagName(name);
         if(nl.getLength() == 0)
         {
             Element newElem = parent.getOwnerDocument().createElement(name);
             parent.appendChild(newElem);
             return newElem;
         }
         else
             return (Element) nl.item(0);
     }
 
     /**
      * Given a tag, find the tag in the parent element and return its text or the default String if there is none.
      */
     public static String getTagText(Element parent, String tag, String defaultText)
     {
         NodeList nl = parent.getElementsByTagName(tag);
 
         if(nl.getLength() == 0)
             return defaultText;
 
         StringBuffer text = new StringBuffer();
         for(int i = 0; i < nl.getLength(); i++)
         {
             Element tagElem = (Element) nl.item(i);
             NodeList children = tagElem.getChildNodes();
             for(int c = 0; c < children.getLength(); c++)
             {
                 text.append(children.item(c).getNodeValue());
             }
         }
         return text.toString();
     }
 
     /**
      * This class accepts a list and a pattern and creates a second list with the items
      * that match the pattern. If the pattern is "*" then all items are matched. If the pattern is a valid ORO Perl5
      * regular expression, the expression is used to do the matching. If it's not "*" or a perl expression, then the
      * simple pattern is returned if it's found in the list.
      */
     public static class StringListMatcher
     {
         private PatternCompiler compiler = new Perl5Compiler();
         private PatternMatcher matcher = new Perl5Matcher();
         private Pattern pattern;
         private MalformedPatternException patternException;
 
         private List source;
         private List dest = new ArrayList();
 
         public StringListMatcher(List source, String pattern)
         {
             this.source = source;
 
             if(pattern.equals("*"))
             {
                 dest.addAll(source);
             }
             else if(pattern.startsWith("/") && pattern.endsWith("/"))
             {
                 String actualPattern = pattern.substring(1, pattern.length()-2);
                 try
                 {
                     this.pattern = compiler.compile(actualPattern, Perl5Compiler.CASE_INSENSITIVE_MASK);
                     for(int i = 0; i < source.size(); i++)
                     {
                         String item = (String) source.get(i);
                         if(matcher.contains(item, this.pattern))
                             dest.add(item);
                     }
                 }
                 catch(MalformedPatternException e)
                 {
                     patternException = e;
                     dest.add(pattern);
                 }
             }
             else
                 dest.add(pattern);
         }
 
         public List getMatchedItems()
         {
             return dest;
         }
 
         public MalformedPatternException getPatternException()
         {
             return patternException;
         }
 
         public List getSource()
         {
             return source;
         }
 
         public Pattern getPattern()
         {
             return pattern;
         }
     }
 
     public Document getDocument()
     {
         reload();
         return xmlDoc;
     }
 
     /**
      * Given an element, see if the element is a <templates> element. If it is, then catalog all of
      * the elements as templates that can be re-used at a later point.
      */
     public void catalogElement(Element elem)
     {
         if(!"templates".equals(elem.getNodeName()))
             return;
 
         String pkgName = elem.getAttribute("package");
 
         NodeList children = elem.getChildNodes();
         for(int c = 0; c < children.getLength(); c++)
         {
             Node child = children.item(c);
             if(child.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             Element childElem = (Element) child;
             String templateName = childElem.getAttribute("id");
             if(templateName.length() == 0)
                 templateName = childElem.getAttribute("name");
 
             templates.put(pkgName.length() > 0 ? (pkgName + "." + templateName) : templateName, childElem);
         }
     }
 
     /**
      * Given an element, apply templates to the node. If there is an attribute called "template" then inherit that
      * template first. Then, search through all of the nodes in the element and try to find all <include-template id="x">
      * elements to copy the template elements at those locations. Also, go through each child to see if a tag name
      * exists that matches a template name -- if it does, then "inherit" that template to replace the element at that
      * location.
      */
     public void processTemplates(Element elem)
     {
         inheritNodes(elem, templates, "template", defaultExcludeElementsFromInherit);
 
         NodeList includes = elem.getElementsByTagName("include-template");
         if(includes != null && includes.getLength() > 0)
         {
             for(int n = 0; n < includes.getLength(); n++)
             {
                 Element include = (Element) includes.item(n);
                 String templateName = include.getAttribute("id");
                 Element template = (Element) templates.get(templateName);
 
                 if(template != null)
                 {
                     NodeList incChildren = template.getChildNodes();
                     for(int c = 0; c < incChildren.getLength(); c++)
                     {
                         Node incCopy = xmlDoc.importNode(incChildren.item(c), true);
                         if(incCopy.getNodeType() == Node.ELEMENT_NODE)
                             ((Element) incCopy).setAttribute("_included-from-template", templateName);
                         elem.insertBefore(incCopy, include);
                     }
                 }
             }
         }
 
         NodeList children = elem.getChildNodes();
         for(int c = 0; c < children.getLength(); c++)
         {
             Node childNode = children.item(c);
             if(childNode.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             String nodeName = childNode.getNodeName();
             if(templates.containsKey(nodeName))
             {
                 Element template = (Element) templates.get(nodeName);
                 Node incCopy = xmlDoc.importNode(template, true);
                 if(incCopy.getNodeType() == Node.ELEMENT_NODE)
                     ((Element) incCopy).setAttribute("_included-from-template", nodeName);
 
                 // make sure that the child's attributes overwrite the attributes in the templates with the same name
                 NamedNodeMap attrsInChild = childNode.getAttributes();
                 for(int a = 0; a < attrsInChild.getLength(); a++)
                 {
                     Node childAttr = attrsInChild.item(a);
                     ((Element) incCopy).setAttribute(childAttr.getNodeName(), childAttr.getNodeValue());
                 }
 
                 // now do the actual replacement
                 inheritNodes((Element) incCopy, templates, "template", defaultExcludeElementsFromInherit);
                 elem.replaceChild(incCopy, childNode);
             }
             else
                 inheritNodes((Element) childNode, templates, "template", defaultExcludeElementsFromInherit);
         }
     }
 
     public List getErrors()
     {
         return errors;
     }
 
     public void addError(String msg)
     {
         errors.add(msg);
     }
 
     public SourceInfo getSourceDocument()
     {
         return docSource;
     }
 
     public Map getSourceFiles()
     {
         return sourceFiles;
     }
 
     public boolean sourceChanged()
     {
         if(docSource == null)
             return false;
 
         if(sourceFiles.size() > 1)
         {
             for(Iterator i = sourceFiles.values().iterator(); i.hasNext(); )
             {
                 if(((SourceInfo) i.next()).sourceChanged())
                     return true;
             }
         }
         else
             return docSource.sourceChanged();
 
         // if we get to here, none of the files is newer than what's in memory
         return false;
     }
 
     public void forceReload()
     {
         loadDocument(docSource.getFile());
     }
 
     public void reload()
     {
         if(allowReload && docSource != null && sourceChanged())
             loadDocument(docSource.getFile());
     }
 
     public boolean loadDocument(File file)
     {
         docSource = null;
         xmlDoc = null;
         loadXML(file);
         catalogNodes();
         return errors.size() == 0 ? true : false;
     }
 
     public String findElementOrAttrValue(Element elem, String nodeName)
     {
         String attrValue = elem.getAttribute(nodeName);
         if(attrValue.length() > 0)
             return attrValue;
 
         // we don't usually want the first value -- we want the last in case there is inheritance
         String lastValue = null;
 
         NodeList children = elem.getChildNodes();
         for(int n = 0; n < children.getLength(); n++)
         {
             Node node = children.item(n);
             if(node.getNodeName().equals(nodeName))
                 lastValue = node.getFirstChild().getNodeValue();
         }
 
         return lastValue;
     }
 
     public String ucfirst(String str)
     {
         return str.substring(0, 1).toUpperCase() + str.substring(1);
     }
 
     public void inheritElement(Element srcElement, Element destElem, Set excludeElems, String inheritedFromNode)
     {
         NamedNodeMap inhAttrs = srcElement.getAttributes();
         for(int i = 0; i < inhAttrs.getLength(); i++)
         {
             Node attrNode = inhAttrs.item(i);
             final String nodeName = attrNode.getNodeName();
             if(! excludeElems.contains(nodeName) && destElem.getAttribute(nodeName).equals(""))
                 destElem.setAttribute(nodeName, attrNode.getNodeValue());
         }
 
         DocumentFragment inheritFragment = xmlDoc.createDocumentFragment();
         NodeList inhChildren = srcElement.getChildNodes();
         for(int i = inhChildren.getLength() - 1; i >= 0; i--)
         {
             Node childNode = inhChildren.item(i);
 
             // only add if there isn't an attribute overriding this element
             final String nodeName = childNode.getNodeName();
             if(destElem.getAttribute(nodeName).length() == 0 && (! excludeElems.contains(nodeName)))
             {
                 Node cloned = childNode.cloneNode(true);
                 if(inheritedFromNode != null && cloned.getNodeType() == Node.ELEMENT_NODE)
                     ((Element) cloned).setAttribute("_inherited-from", inheritedFromNode);
                 inheritFragment.insertBefore(cloned, inheritFragment.getFirstChild());
             }
         }
 
         destElem.insertBefore(inheritFragment, destElem.getFirstChild());
     }
 
     public void inheritNodes(Element element, Map sourcePool, String attrName, Set excludeElems)
     {
         String inheritAttr = element.getAttribute(attrName);
         while(inheritAttr != null && inheritAttr.length() > 0)
         {
             Element inheritFromElem = null;
             StringTokenizer inheritST = new StringTokenizer(inheritAttr, ",");
             String[] inherits = new String[15];
             int inheritsCount = 0;
             while(inheritST.hasMoreTokens())
             {
                 inherits[inheritsCount] = inheritST.nextToken();
                 inheritsCount++;
             }
 
             /** we're going to work backwards because we want to make sure the
              *  elements are added in the appropriate order (same order as the
              *  inheritance list)
              */
 
             for(int j = (inheritsCount - 1); j >= 0; j--)
             {
                 String inheritType = inherits[j];
                 inheritFromElem = (Element) sourcePool.get(inheritType);
                 if(inheritFromElem == null)
                 {
                     errors.add("can not extend '" + element.getAttribute("name") + "' from '" + inheritType + "': source not found");
                     continue;
                 }
 
                 /* don't inherit the same objects more than once */
                 String inheritanceId = Integer.toString(element.hashCode()) + '.' + Integer.toString(inheritFromElem.hashCode());
                 if(inheritanceHistorySet.contains(inheritanceId))
                 {
                     errors.add("Attempting to copy duplicate node: "+ inheritanceId + ", " + element.getTagName() + ", " + element.getAttribute("name") + ", "+ inheritFromElem.getTagName());
                     //continue;
                 }
                 inheritanceHistorySet.add(inheritanceId);
 
                 Element extendsElem = xmlDoc.createElement("extends");
                 extendsElem.appendChild(xmlDoc.createTextNode(inheritType));
                 element.appendChild(extendsElem);
 
                 inheritElement(inheritFromElem, element, excludeElems, inheritType);
             }
 
             // find the next one if we have more parents
             if(inheritFromElem != null)
                 inheritAttr = inheritFromElem.getAttribute(attrName);
             else
                 inheritAttr = null;
         }
     }
 
     public void replaceNodeValue(Node node, String findStr, String replStr)
     {
         String srcStr = node.getNodeValue();
         if(srcStr == null || findStr == null || replStr == null)
             return;
 
         int findLoc = srcStr.indexOf(findStr);
         if(findLoc >= 0)
         {
             StringBuffer sb = new StringBuffer(srcStr);
             sb.replace(findLoc, findLoc + findStr.length(), replStr);
             node.setNodeValue(sb.toString());
         }
     }
 
     public void addMetaInfoOptions()
     {
         if(xmlDoc == null || metaInfoElem == null)
             return;
 
         if(metaInfoOptionsElem != null)
             metaInfoElem.removeChild(metaInfoOptionsElem);
 
         metaInfoOptionsElem = xmlDoc.createElement("options");
         metaInfoOptionsElem.setAttribute("name", "Allow reload");
         metaInfoOptionsElem.setAttribute("value", (allowReload ? "Yes" : "No"));
         metaInfoElem.appendChild(metaInfoOptionsElem);
     }
 
     public void addMetaInformation()
     {
         NodeList existing = xmlDoc.getDocumentElement().getElementsByTagName("meta-info");
         if(existing.getLength() > 0)
         {
             xmlDoc.getDocumentElement().removeChild(existing.item(0));
             metaInfoElem = null;
             metaInfoOptionsElem = null;
         }
 
         metaInfoElem = xmlDoc.createElement("meta-info");
         xmlDoc.getDocumentElement().appendChild(metaInfoElem);
 
         addMetaInfoOptions();
 
         Element filesElem = xmlDoc.createElement("source-files");
         metaInfoElem.appendChild(filesElem);
 
         for(Iterator sfi = sourceFiles.values().iterator(); sfi.hasNext();)
         {
             SourceInfo si = (SourceInfo) sfi.next();
             Element fileElem = xmlDoc.createElement("source-file");
             fileElem.setAttribute("abs-path", si.getFile().getAbsolutePath());
             if(si.getParent() != null)
                 fileElem.setAttribute("included-from", si.getParent().getFile().getName());
             filesElem.appendChild(fileElem);
 
             List preProcessors = si.getPreProcessors();
             if(preProcessors != null)
             {
                 for(int i = 0; i < preProcessors.size(); i++)
                 {
                     SourceInfo psi = (SourceInfo) preProcessors.get(i);
                     fileElem = xmlDoc.createElement("source-file");
                     fileElem.setAttribute("abs-path", psi.getFile().getAbsolutePath());
                     if(psi.getParent() != null)
                         fileElem.setAttribute("included-from", psi.getParent().getFile().getName());
                     filesElem.appendChild(fileElem);
                 }
             }
         }
 
         if(errors.size() > 0)
         {
             Element errorsElem = xmlDoc.createElement("errors");
             metaInfoElem.appendChild(errorsElem);
 
             for(Iterator ei = errors.iterator(); ei.hasNext();)
             {
                 Element errorElem = xmlDoc.createElement("error");
                 Text errorText = xmlDoc.createTextNode((String) ei.next());
                 errorElem.appendChild(errorText);
                 errorsElem.appendChild(errorElem);
             }
         }
     }
 
     static public String getClassName(String pkgAndClassName, char sep)
     {
         int classNameDelimPos = pkgAndClassName.lastIndexOf(sep);
         return classNameDelimPos != -1 ? pkgAndClassName.substring(classNameDelimPos+1) : pkgAndClassName;
     }
 
     static public String getPackageName(String pkgAndClassName, char sep)
     {
         int classNameDelimPos = pkgAndClassName.lastIndexOf(sep);
         return classNameDelimPos != -1 ? pkgAndClassName.substring(0, classNameDelimPos) : null;
     }
 
     /**
      * Return the list of identifiers that this class has cataloged via catalogNodes. This list is used to
      * store the identifers in a Java class.
      */
 
     public String[] getCatalogedNodeIdentifiers()
     {
         return null;
     }
 
     public String getCatalogedNodeIdentifiersClassName()
     {
         return catalogedNodeIdentifiersClassName;
     }
 
     public class NodeIdentifiersClassInfo
     {
         private String rootPath;
         private String defaultPkgAndClassName;
         private String pkgAndClassName;
         private String[] identifiers;
         private char subPackageSeparator = '.';
 
         public NodeIdentifiersClassInfo(String rootPath, String defaultPkgAndClassName)
         {
             this.rootPath = rootPath;
             this.defaultPkgAndClassName = defaultPkgAndClassName;
 
             identifiers = getCatalogedNodeIdentifiers();
             Arrays.sort(identifiers, String.CASE_INSENSITIVE_ORDER);
 
             pkgAndClassName = getCatalogedNodeIdentifiersClassName();
             if(pkgAndClassName == null)
                 pkgAndClassName = defaultPkgAndClassName;
         }
 
         public NodeIdentifiersClassInfo(String rootPath, String pkgAndClassName, char subPackageSeparator)
         {
             this(rootPath, pkgAndClassName);
             this.subPackageSeparator = subPackageSeparator;
         }
 
         public void generateCode(String subPkgAndClassName, List ids) throws IOException
         {
             String fullPkgName, className;
             File file;
 
             if(subPkgAndClassName == null)
             {
                 fullPkgName = getPackageName(pkgAndClassName, '.');
                 className = getClassName(pkgAndClassName, '.');
                 file = new File(rootPath, pkgAndClassName.replace('.', '/') + ".java");
             }
             else
             {
                 String subPkgName = getPackageName(subPkgAndClassName, subPackageSeparator);
                 if(subPkgName == null)
                     fullPkgName = pkgAndClassName.toLowerCase();
                 else
                     fullPkgName = (pkgAndClassName + subPackageSeparator + subPkgName).toLowerCase();
                 className = getClassName(subPkgAndClassName, subPackageSeparator);
                 String fullClassSpec = fullPkgName + subPackageSeparator + xmlTextToJavaIdentifier(className, true);
                 file = new File(rootPath, fullClassSpec.replace(subPackageSeparator, '/') + ".java");
             }
 
             file.getParentFile().mkdirs();
             FileWriter writer = new FileWriter(file);
 
             writer.write("\n/* this file is generated by com.netspective.sparx.util.xml.XmlSource.createNodeIdentifiersClass(), do not modify (you can extend it, though) */\n\n");
             if(fullPkgName != null)
                 writer.write("package " + fullPkgName.replace(subPackageSeparator, '.') + ";\n\n");
 
             writer.write("public class " + xmlTextToJavaIdentifier(className, true) + "\n");
             writer.write("{\n");
 
             for(int i = 0; i < ids.size(); i++)
             {
                 String identifier = (String) ids.get(i);
                 String constant = xmlTextToJavaConstantTrimmed(subPkgAndClassName != null ? identifier.substring(subPkgAndClassName.length()+1) : identifier);
                 if(constant.length() > 0)
                     writer.write("    static public final String " + constant + " = \"" + identifier + "\";\n");
                 else
                     writer.write("    // static public final String " + constant + " = \"" + identifier + "\";\n");
             }
 
             writer.write("}\n");
             writer.close();
         }
 
         public void generateCode() throws IOException
         {
             List idsWithNoPackages = new ArrayList();
             Map idsInPackages = new HashMap();
 
             for(int i = 0; i < identifiers.length; i++)
             {
                 String identifier = identifiers[i];
                 if(identifier.indexOf(subPackageSeparator) > 0)
                 {
                     String packageName = identifier.substring(0, identifier.lastIndexOf(subPackageSeparator));
                     List ids = (List) idsInPackages.get(packageName);
                     if(ids == null)
                     {
                         ids = new ArrayList();
                         idsInPackages.put(packageName, ids);
                     }
                     ids.add(identifier);
                 }
                 else if(identifier.length() > 0)
                     idsWithNoPackages.add(identifier);
             }
 
             if(idsWithNoPackages.size() > 0)
                 generateCode(null, idsWithNoPackages);
 
             Iterator idsInPackage = idsInPackages.entrySet().iterator();
             while(idsInPackage.hasNext())
             {
                 Map.Entry entry = (Map.Entry) idsInPackage.next();
                 String subPackageName = ((String) entry.getKey()).toLowerCase();
                 List ids = (List) entry.getValue();
                 generateCode(subPackageName, ids);
             }
         }
 
         public String getDefaultPkgAndClassName()
         {
             return defaultPkgAndClassName;
         }
 
         public String[] getIdentifiers()
         {
             return identifiers;
         }
 
         public String getPkgAndClassName()
         {
             return pkgAndClassName;
         }
 
         public String getRootPath()
         {
             return rootPath;
         }
     }
 
     public NodeIdentifiersClassInfo createNodeIdentifiersClass(String rootPath, String defaultPkgAndClassName) throws IOException
     {
         NodeIdentifiersClassInfo result = new NodeIdentifiersClassInfo(rootPath, defaultPkgAndClassName);
         if(result.identifiers != null)
             result.generateCode();
         return result;
     }
 
     public void catalogNodes()
     {
     }
 
     /**
      * Given the current xmlDoc that was already read in, send it through the transformation stylesheet and assign
      * the value back to xmlDoc.
      */
     public void preProcess(File styleSheet)
     {
         try
         {
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer(new StreamSource(styleSheet));
 
             DOMResult result = new javax.xml.transform.dom.DOMResult();
             transformer.transform(new DOMSource(xmlDoc), result);
             xmlDoc = (Document) result.getNode();
         }
         catch(Exception e)
         {
             //StringWriter stack = new StringWriter();
             //e.printStackTrace(new PrintWriter(stack));
             addError("Failed to pre-process using style-sheet " + styleSheet.getAbsolutePath() + ": " + e.toString());
         }
     }
 
     public Document loadXML(File file)
     {
         if(docSource == null)
         {
             errors.clear();
             sourceFiles.clear();
             metaInfoElem = null;
             metaInfoOptionsElem = null;
         }
 
         SourceInfo sourceInfo = new SourceInfo(docSource, file);
         sourceFiles.put(file.getAbsolutePath(), sourceInfo);
 
         Document doc = null;
         try
         {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder parser = factory.newDocumentBuilder();
             doc = parser.parse(file);
             doc.normalize();
         }
         catch(Exception e)
         {
             throw new RuntimeException("XML Parsing error in '" + file.getAbsolutePath() + "': " + e);
         }
 
         if(docSource == null)
         {
             xmlDoc = doc;
             docSource = sourceInfo;
         }
 
         /*
 		 find all of the <include file="xyz"> elements and "include" all the
 		 elements in that document as children of the main document
 		*/
 
         Element rootElem = doc.getDocumentElement();
         NodeList includes = rootElem.getElementsByTagName("include");
         if(includes != null && includes.getLength() > 0)
         {
             for(int n = 0; n < includes.getLength(); n++)
             {
                 Element include = (Element) includes.item(n);
                 String incFileAttr = include.getAttribute("file");
                 File incFile = new File(file.getParentFile(), incFileAttr);
                 if(!sourceFiles.containsKey(incFile.getAbsolutePath()))
                 {
                     Document includeDoc = loadXML(incFile);
                     if(includeDoc != null)
                     {
                         Element includeRoot = includeDoc.getDocumentElement();
                         NodeList incChildren = includeRoot.getChildNodes();
                         for(int c = 0; c < incChildren.getLength(); c++)
                         {
                             Node incCopy = doc.importNode(incChildren.item(c), true);
                             if(incCopy.getNodeType() == Node.ELEMENT_NODE)
                                 ((Element) incCopy).setAttribute("_included-from", incFileAttr);
                             rootElem.insertBefore(incCopy, include);
                         }
                     }
                 }
             }
         }
 
         /*
 		   find all of the <pre-process stylesheet="xyz"> elements and "pre-process" using XSLT stylesheets
 		*/
 
         NodeList preProcessors = rootElem.getElementsByTagName("pre-process");
         if(preProcessors != null && preProcessors.getLength() > 0)
         {
             for(int n = 0; n < preProcessors.getLength(); n++)
             {
                 Element preProcessor = (Element) preProcessors.item(n);
                 String ppFileAttr = preProcessor.getAttribute("style-sheet");
                 if(ppFileAttr.length() == 0)
                 {
                     addError("No style-sheet attribute provided for pre-process element");
                     continue;
                 }
                 File ppFile = new File(file.getParentFile(), ppFileAttr);
                 docSource.addPreProcessor(new SourceInfo(docSource, ppFile));
                 preProcess(ppFile);
             }
         }
         return doc;
     }
 
     public void saveXML(String fileName)
     {
         /* we use reflection so that org.apache.xml.serialize.* is not a package requirement */
 
         OutputStream os = null;
         try
         {
             Class serializerCls = Class.forName("org.apache.xml.serialize.XMLSerializer");
             Class outputFormatCls = Class.forName("org.apache.xml.serialize.OutputFormat");
 
             Constructor serialCons = serializerCls.getDeclaredConstructor(new Class[]{OutputStream.class, outputFormatCls});
             Constructor outputCons = outputFormatCls.getDeclaredConstructor(new Class[]{Document.class});
 
             os = new FileOutputStream(fileName);
             Object outputFormat = outputCons.newInstance(new Object[]{xmlDoc});
             Method indenting = outputFormatCls.getMethod("setIndenting", new Class[]{boolean.class});
             indenting.invoke(outputFormat, new Object[]{new Boolean(true)});
 
             Object serializer = serialCons.newInstance(new Object[]{os, outputFormat});
             Method serialize = serializerCls.getMethod("serialize", new Class[]{Document.class});
             serialize.invoke(serializer, new Object[]{xmlDoc});
         }
         catch(Exception e)
         {
             throw new RuntimeException("Unable to save '" + fileName + "': " + e);
         }
         finally
         {
             try
             {
                 if(os != null)
                     os.close();
             }
             catch(Exception e)
             {
             }
         }
     }
 
     public NodeList selectNodeList(String expr) throws TransformerException
     {
         return XPathAPI.selectNodeList(xmlDoc, expr);
     }
 
     public long getSelectNodeListCount(String expr) throws TransformerException
     {
         NodeList nodes = selectNodeList(expr);
         return nodes.getLength();
     }
 
     public Metric getMetrics(Metric root)
     {
         return null;
     }
 
     public String replaceExpressions(String original, Map variables)
     {
         if(original.indexOf("${") == -1)
             return original;
 
         StringBuffer sb = new StringBuffer();
         int prev = 0;
 
         int pos;
         while((pos = original.indexOf("$", prev)) >= 0)
         {
             if(pos > 0)
             {
                 sb.append(original.substring(prev, pos));
             }
             if(pos == (original.length() - 1))
             {
                 sb.append('$');
                 prev = pos + 1;
             }
             else if(original.charAt(pos + 1) != '{')
             {
                 sb.append(original.charAt(pos + 1));
                 prev = pos + 2;
             }
             else
             {
                 int endName = original.indexOf('}', pos);
                 if(endName < 0)
                 {
                     throw new RuntimeException("Syntax error in prop: " + original);
                 }
 
                 String javaExprStr = original.substring(pos + 2, endName);
                 try
                 {
                     Expression expression = ExpressionFactory.createExpression(javaExprStr);
                     JexlContext jexlContext = JexlHelper.createContext();
                     jexlContext.setVars(variables);
                     String result = expression.evaluate(jexlContext).toString();
                     sb.append(result);
                 }
                 catch (Exception e)
                 {
                     sb.append("${" + javaExprStr + "}");
                    addError("Unable to evaluate expression '"+ javaExprStr +"': " + e.getMessage());
                 }
 
                 prev = endName + 1;
             }
         }
 
         if(prev < original.length()) sb.append(original.substring(prev));
         return sb.toString();
     }
 
     public void replaceNodeMacros(Node inNode, Set nodeNames, Map variables)
     {
         if(!variables.containsKey("this"))
             variables.put("this", inNode);
 
         NamedNodeMap attrs = inNode.getAttributes();
         if(attrs != null && attrs.getLength() > 0)
         {
             for(int i = 0; i < attrs.getLength(); i++)
             {
                 Node attr = attrs.item(i);
                 if(nodeNames.contains(attr.getNodeName()))
                 {
                     String nodeValue = attr.getNodeValue();
                     String replaced = replaceExpressions(nodeValue, variables);
                     if(nodeValue != replaced)
                         attr.setNodeValue(replaced);
                 }
             }
         }
 
         NodeList children = inNode.getChildNodes();
         for(int c = 0; c < children.getLength(); c++)
         {
             Node node = children.item(c);
             if(node.getNodeType() == Node.ELEMENT_NODE && nodeNames.contains(node.getNodeName()))
             {
                 Text textNode = (Text) node.getFirstChild();
                 String nodeValue = textNode.getNodeValue();
                 String replaced = replaceExpressions(nodeValue, variables);
                 if(nodeValue != replaced)
                     textNode.setNodeValue(replaced);
             }
         }
     }
 }
