 /*
  * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.sysmap.crux.core.declarativeui;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import br.com.sysmap.crux.core.client.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.client.declarative.TagChild;
 import br.com.sysmap.crux.core.client.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagChildren;
 import br.com.sysmap.crux.core.client.screen.WidgetFactory;
 import br.com.sysmap.crux.core.client.screen.WidgetFactory.WidgetFactoryContext;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.HTMLTag;
 import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessorContext;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.scanner.screen.config.WidgetConfig;
 import br.com.sysmap.crux.core.utils.ClassUtils;
 import br.com.sysmap.crux.core.utils.HTMLUtils;
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 class HTMLBuilder
 {
 	private static final String CRUX_CORE_NAMESPACE= "http://www.sysmap.com.br/crux";
 	private static DocumentBuilder documentBuilder;
 	private static XPathExpression findCruxPagesBodyExpression;
 	private static XPathExpression findHTMLHeadExpression;
 	private static Set<String> htmlPanelContainers;
 	private static final Log log = LogFactory.getLog(CruxToHtmlTransformer.class);
 	private static DeclarativeUIMessages messages = (DeclarativeUIMessages)MessagesFactory.getMessages(DeclarativeUIMessages.class);
 	private static Map<String, String> referenceWidgetsList;
 	private static final String WIDGETS_NAMESPACE_PREFIX= "http://www.sysmap.com.br/crux/";
 	private static Set<String> widgetsSubTags;
 	private static Set<String> hasInnerHTMLWidgetTags;
 	private static final String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
 	
 	static 
 	{
 		try
         {
 	        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
 	        builderFactory.setNamespaceAware(false);
 	        builderFactory.setIgnoringComments(true);
 	        
 	        documentBuilder = builderFactory.newDocumentBuilder();
 	        generateReferenceWidgetsList();
 	        generateHtmlPanelWidgetsList();
 
 	        XPath htmlPath = XPathUtils.getHtmlXPath();
 			findCruxPagesBodyExpression = htmlPath.compile("//h:body");
 			findHTMLHeadExpression = htmlPath.compile("//h:head");
         }
         catch (Exception e)
         {
         	log.error(messages.htmlBuilderErrorCreatingBuilder(e.getMessage()), e);
         }
 	}
 	private String cruxTagName;
 	private final boolean escapeXML;
 	private final boolean indentOutput;
 
 	private int jsIndentationLvl;
 	
 	
 	/**
 	 * @param indentOutput 
 	 * @throws HTMLBuilderException 
 	 * 
 	 */
 	public HTMLBuilder(boolean escapeXML, boolean indentOutput) throws HTMLBuilderException
     {
 		this.escapeXML = escapeXML;
 		this.indentOutput = indentOutput;
     }
 	
 	/**
 	 * 
 	 * @return
 	 * @throws HTMLBuilderException 
 	 */
 	private static void generateHtmlPanelWidgetsList() throws HTMLBuilderException
 	{
 		htmlPanelContainers = new HashSet<String>();
 		Set<String> registeredLibraries = WidgetConfig.getRegisteredLibraries();
 		for (String library : registeredLibraries)
 		{
 			Set<String> factories = WidgetConfig.getRegisteredLibraryFactories(library);
 			for (String widget : factories)
 			{
 				try
 				{
 					Class<?> clientClass = Class.forName(WidgetConfig.getClientClass(library, widget));
 					DeclarativeFactory factory = clientClass.getAnnotation(DeclarativeFactory.class);
 					if (factory.htmlContainer())
 					{
 						htmlPanelContainers.add(library+"_"+widget);				
 					}
 				}
 				catch (Exception e)
 				{
 					throw new HTMLBuilderException(messages.transformerErrorGeneratingWidgetsReferenceList(), e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws HTMLBuilderException 
 	 */
 	private static void generateReferenceWidgetsList() throws HTMLBuilderException
 	{
 		referenceWidgetsList = new HashMap<String, String>();
 		widgetsSubTags = new HashSet<String>();
 		hasInnerHTMLWidgetTags = new HashSet<String>();
 		Set<String> registeredLibraries = WidgetConfig.getRegisteredLibraries();
 		for (String library : registeredLibraries)
 		{
 			Set<String> factories = WidgetConfig.getRegisteredLibraryFactories(library);
 			for (String widget : factories)
 			{
 				try
 				{
 					Class<?> clientClass = Class.forName(WidgetConfig.getClientClass(library, widget));
 					Method method = clientClass.getMethod("processChildren", new Class[]{WidgetFactoryContext.class});
 					generateReferenceWidgetsListFromTagChildren(method.getAnnotation(TagChildren.class), 
 																		library, widget, new HashSet<String>());
 				}
 				catch (Exception e)
 				{
 					throw new HTMLBuilderException(messages.transformerErrorGeneratingWidgetsReferenceList(), e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param widgetList 
 	 * @param tagChildren
 	 * @param parentLibrary
 	 * @throws HTMLBuilderException 
 	 */
 	private static void generateReferenceWidgetsListFromTagChildren(TagChildren tagChildren, 
 																    String parentLibrary, String parentWidget, Set<String> added) throws HTMLBuilderException
 	{
 		if (tagChildren != null)
 		{
 			String parentPath;
 			for (TagChild child : tagChildren.value())
 			{
 				Class<? extends WidgetChildProcessor<?>> processorClass = child.value();
 				if (!added.contains(processorClass.getCanonicalName()))
 				{
 					parentPath = parentWidget;
 					added.add(processorClass.getCanonicalName());
 					TagChildAttributes childAttributes = ClassUtils.getChildtrenAttributesAnnotation(processorClass);
 					if (childAttributes!= null)
 					{
 						if (!StringUtils.isEmpty(childAttributes.tagName()))
 						{
 							parentPath = parentWidget+"_"+childAttributes.tagName();
 							if (WidgetFactory.class.isAssignableFrom(childAttributes.type()))
 							{
 								DeclarativeFactory declarativeFactory = childAttributes.type().getAnnotation(DeclarativeFactory.class);
 								if (declarativeFactory != null)
 								{
 									referenceWidgetsList.put(parentLibrary+"_"+parentPath,
 											declarativeFactory.library()+"_"+declarativeFactory.id());
 								}
 							}
 							else
 							{
 								widgetsSubTags.add(parentLibrary+"_"+parentPath);							
 							}
 						}
						if (HTMLTag.class.isAssignableFrom(childAttributes.type()))
						{
							hasInnerHTMLWidgetTags.add(parentLibrary+"_"+parentPath);
						}
 					}
 					
 					try
 					{
 						Method method = processorClass.getMethod("processChildren", new Class[]{WidgetChildProcessorContext.class});
 						generateReferenceWidgetsListFromTagChildren(method.getAnnotation(TagChildren.class), 
 																	parentLibrary, parentPath, added);
 					}
 					catch (Exception e)
 					{
 						throw new HTMLBuilderException(messages.transformerErrorGeneratingWidgetsList(), e);
 					}
 				}
 			}
 		}				
 	}
 	
 	/**
 	 * @param cruxPageDocument
 	 * @param out
 	 * @throws HTMLBuilderException
 	 */
 	public void build(Document cruxPageDocument, Writer out) throws HTMLBuilderException
 	{
 		try
         {
 			Document htmlDocument = createHTMLDocument(cruxPageDocument);
 			translateDocument(cruxPageDocument, htmlDocument);
 	        write(htmlDocument, out);
         }
         catch (IOException e)
         {
         	throw new HTMLBuilderException(e.getMessage(), e);
         }
 	}
 
 	/**
 	 * 
 	 */
 	private void clearCurrentWidget()
 	{
 		cruxTagName = "";
 	}
 
 	/**
 	 * @param cruxPageDocument
 	 * @return
 	 */
 	private Document createHTMLDocument(Document cruxPageDocument)
     {
 	    Document htmlDocument;
 		DocumentType doctype = cruxPageDocument.getDoctype();
 		if (doctype != null)
 		{
 			htmlDocument = documentBuilder.getDOMImplementation().createDocument(XHTML_NAMESPACE, "html", doctype);
 		}
 		else
 		{
 			htmlDocument = documentBuilder.newDocument();
 		}
 	    return htmlDocument;
     }
 
 	/**
 	 * @param cruxPageInnerTag
 	 * @param cruxArrayMetaData
 	 * @param htmlDocument
 	 * @throws HTMLBuilderException 
 	 */
 	private void generateCruxInnerMetaData(Element cruxPageInnerTag, StringBuilder cruxArrayMetaData, Document htmlDocument) throws HTMLBuilderException
     {
 		if (indentOutput)
 		{
 			writeIndentationSpaces(cruxArrayMetaData);
 		}
 		cruxArrayMetaData.append("{");
 		
 		String currentWidgetTag = getCurrentWidgetTag() ;
 		if (isWidget(currentWidgetTag))
 		{
 			cruxArrayMetaData.append("\"type\":\""+currentWidgetTag+"\"");
 		}
 		else
 		{
 			cruxArrayMetaData.append("\"childTag\":\""+cruxPageInnerTag.getLocalName()+"\"");
 		}
 		
 		if (allowInnerHTML(currentWidgetTag))
 		{
 			String innerHTML = getHTMLFromNode(cruxPageInnerTag);
 			if (innerHTML.length() > 0)
 			{
 				cruxArrayMetaData.append(",\"_html\":\""+HTMLUtils.escapeJavascriptString(innerHTML, escapeXML)+"\"");
 			}
 		}
 		else
 		{
 			String innerText = getTextFromNode(cruxPageInnerTag);
 			if (innerText.length() > 0)
 			{
 				cruxArrayMetaData.append(",\"_text\":\""+HTMLUtils.escapeJavascriptString(innerText, escapeXML)+"\"");
 			}
 		}
 		generateCruxMetaDataAttributes(cruxPageInnerTag, cruxArrayMetaData);
 		NodeList childNodes = cruxPageInnerTag.getChildNodes();
 		if (childNodes != null && childNodes.getLength() > 0)
 		{
 			cruxArrayMetaData.append(",\"children\":[");
 			indent();
 			generateCruxMetaData(cruxPageInnerTag, cruxArrayMetaData, htmlDocument);
 			outdent();
 			cruxArrayMetaData.append("]");
 		}
 
 		cruxArrayMetaData.append("}");
     }
 	
 	/**
 	 * @param tagName
 	 * @return
 	 */
 	private boolean allowInnerHTML(String tagName)
 	{
 		return hasInnerHTMLWidgetTags.contains(tagName);	
 	}
 	
 	
 	/**
 	 * @param cruxPageBodyElement
 	 * @param cruxArrayMetaData
 	 * @param htmlDocument
 	 * @throws HTMLBuilderException 
 	 */
 	private void generateCruxMetaData(Node cruxPageBodyElement, StringBuilder cruxArrayMetaData, Document htmlDocument) throws HTMLBuilderException
     {
 		NodeList childNodes = cruxPageBodyElement.getChildNodes();
 		if (childNodes != null)
 		{
 			boolean needsComma = false;
 			for (int i=0; i<childNodes.getLength(); i++)
 			{
 				Node child = childNodes.item(i);
 				String namespaceURI = child.getNamespaceURI();
 				String nodeName = child.getLocalName(); 
 					
 				if (namespaceURI != null && namespaceURI.equals(CRUX_CORE_NAMESPACE) && nodeName.equals("screen"))
 				{
 					if (needsComma)
 					{
 						cruxArrayMetaData.append(",");
 					}
 					generateCruxScreenMetaData((Element)child, cruxArrayMetaData, htmlDocument);
 					needsComma = true;
 				}
 				else if (namespaceURI != null && namespaceURI.startsWith(WIDGETS_NAMESPACE_PREFIX))
 				{
 					if (needsComma)
 					{
 						cruxArrayMetaData.append(",");
 					}
 					String widgetType = getCurrentWidgetTag(); 
 					updateCurrentWidgetTag((Element)child);
 					generateCruxInnerMetaData((Element)child, cruxArrayMetaData, htmlDocument);
 					setCurrentWidgetTag(widgetType);
 					needsComma = true;
 				}
 				else
 				{
 					StringBuilder childrenMetaData = new StringBuilder();
 					generateCruxMetaData(child, childrenMetaData, htmlDocument);
 					if (childrenMetaData.length() > 0)
 					{
 						if (needsComma)
 						{
 							cruxArrayMetaData.append(",");
 						}
 						cruxArrayMetaData.append(childrenMetaData);
 						needsComma = true;
 					}		
 				}
 			}
 		}
     }
 
 	/**
 	 * @param cruxPageMetaData
 	 * @param cruxArrayMetaData
 	 */
 	private void generateCruxMetaDataAttributes(Element cruxPageMetaData, StringBuilder cruxArrayMetaData)
 	{
 		NamedNodeMap attributes = cruxPageMetaData.getAttributes();
 		if (attributes != null)
 		{
 			for (int i=0; i<attributes.getLength(); i++)
 			{
 				Node attribute = attributes.item(i);
 				cruxArrayMetaData.append(",");
 				cruxArrayMetaData.append("\""+attribute.getLocalName()+"\":");
 				cruxArrayMetaData.append("\""+HTMLUtils.escapeJavascriptString(attribute.getNodeValue(), escapeXML)+"\"");
 			}
 		}
 	}
 
 	/**
 	 * @param cruxPageBodyElement
 	 * @param htmlElement
 	 * @param htmlDocument
 	 * @throws HTMLBuilderException 
 	 */
 	private void generateCruxMetaDataElement(Element cruxPageBodyElement, Element htmlElement, Document htmlDocument) throws HTMLBuilderException
     {
 		Element cruxMetaData = htmlDocument.createElement("script");
 		cruxMetaData.setAttribute("id", "__CruxMetaDataTag_");		
 		htmlElement.appendChild(cruxMetaData);
 		
 		Text textNode = htmlDocument.createTextNode("function __CruxMetaData_(){return [");
 		cruxMetaData.appendChild(textNode);
 		
 		StringBuilder cruxArrayMetaData = new StringBuilder();
 		generateCruxMetaData(cruxPageBodyElement, cruxArrayMetaData, htmlDocument);
 		textNode = htmlDocument.createTextNode(cruxArrayMetaData.toString());
 		cruxMetaData.appendChild(textNode);
 		
 		textNode = htmlDocument.createTextNode("]}");
 		cruxMetaData.appendChild(textNode);
     }
 	
 	/**
 	 * @param cruxPageScreen
 	 * @param cruxArrayMetaData
 	 * @param htmlDocument
 	 * @throws HTMLBuilderException 
 	 */
 	private void generateCruxScreenMetaData(Element cruxPageScreen, StringBuilder cruxArrayMetaData, Document htmlDocument) throws HTMLBuilderException
     {
 		if (indentOutput)
 		{
 			writeIndentationSpaces(cruxArrayMetaData);
 		}
 		cruxArrayMetaData.append("{");
 		cruxArrayMetaData.append("\"type\":\"screen\"");
 		
 		generateCruxMetaDataAttributes(cruxPageScreen, cruxArrayMetaData);
 		
 		cruxArrayMetaData.append("}");
 		StringBuilder childrenMetaData = new StringBuilder();
 		generateCruxMetaData(cruxPageScreen, childrenMetaData, htmlDocument);
 		
 		if (childrenMetaData.length() > 0)
 		{
 			cruxArrayMetaData.append(",");
 			cruxArrayMetaData.append(childrenMetaData);
 		}		
     }
 	
 	/**
 	 * @param cruxPageDocument
 	 * @return
 	 * @throws HTMLBuilderException
 	 */
 	private Element getCruxPageBodyElement(Document cruxPageDocument) throws HTMLBuilderException
 	{
 		try
         {
 	        NodeList bodyNodes = (NodeList)findCruxPagesBodyExpression.evaluate(cruxPageDocument, XPathConstants.NODESET);
 	        if (bodyNodes.getLength() > 0)
 	        {
 	        	return (Element)bodyNodes.item(0);
 	        }
 	        return null;
         }
         catch (XPathExpressionException e)
         {
         	throw new HTMLBuilderException(e.getMessage(), e);
         }
 	}
 	
 	/**
 	 * @return
 	 */
 	private String getCurrentWidgetTag()
 	{
 		return cruxTagName;
 	}
 
 	/**
 	 * @param htmlDocument
 	 * @return
 	 * @throws HTMLBuilderException
 	 */
 	private Element getHtmlHeadElement(Document htmlDocument) throws HTMLBuilderException
 	{
 		try
         {
 	        NodeList headNodes = (NodeList)findHTMLHeadExpression.evaluate(htmlDocument, XPathConstants.NODESET);
 	        if (headNodes.getLength() > 0)
 	        {
 	        	return (Element)headNodes.item(0);
 	        }
 	        return null;
         }
         catch (XPathExpressionException e)
         {
         	throw new HTMLBuilderException(e.getMessage(), e);
         }
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private String getLibraryName(Node node)
 	{
 		String namespaceURI = node.getNamespaceURI();
 		
 		if (namespaceURI != null && namespaceURI.startsWith(WIDGETS_NAMESPACE_PREFIX))
 		{
 			return namespaceURI.substring(WIDGETS_NAMESPACE_PREFIX.length());
 		}
 		return null;
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private String getReferencedWidget(String tagName)
     {
 	    return referenceWidgetsList.get(tagName);
     }
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private String getTextFromNode(Node node)
 	{
 		StringBuilder text = new StringBuilder(); 
 		
 		NodeList children = node.getChildNodes();
 		if (children != null)
 		{
 			for (int i=0; i<children.getLength(); i++)
 			{
 				Node child = children.item(i);
 				if (child.getNodeType() == Node.TEXT_NODE)
 				{
 					text.append(child.getNodeValue());
 				}
 			}
 		}
 		
 		return text.toString().trim();
 	}
 	
 	/**
 	 * @param node
 	 * @return
 	 * @throws HTMLBuilderException 
 	 */
 	private String getHTMLFromNode(Element elem) throws HTMLBuilderException
 	{
 		try
 		{
 			StringWriter innerHTML = new StringWriter(); 
 			NodeList children = elem.getChildNodes();
 			
 			for (int i=0; i<children.getLength(); i++)
 			{
 				Node child = children.item(i);
 				HTMLUtils.write(child, innerHTML);
 			}
 	        return innerHTML.toString();
 		}
 		catch (IOException e)
 		{
 			throw new HTMLBuilderException(e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void indent()
 	{
 		jsIndentationLvl++;
 	}
 	
 	/**
 	 * @param node
 	 * @return
 	 */
 	private boolean isHTMLChild(Node node)
 	{
 		Node parentNode = node.getParentNode();
 		String namespaceURI = parentNode.getNamespaceURI();
 		return (namespaceURI != null && namespaceURI.equals(XHTML_NAMESPACE) || isHtmlContainerWidget(parentNode));
 	}
 	
 	/**
 	 * @param node
 	 * @return
 	 */
 	private boolean isHtmlContainerWidget(Node node)
 	{
 		if (node instanceof Element)
 		{
 			return isHtmlContainerWidget(node.getLocalName(), getLibraryName(node));
 		}
 		return false;
 	}
 	
 	/**
 	 * @param localName
 	 * @param libraryName
 	 * @return
 	 */
 	private boolean isHtmlContainerWidget(String localName, String libraryName)
 	{
 	    return htmlPanelContainers.contains(libraryName+"_"+localName);
 	}
 	
 	/**
 	 * @param tagName
 	 * @return
 	 */
 	private boolean isReferencedWidget(String tagName)
     {
 	    return referenceWidgetsList.containsKey(tagName);
     }
 
 	/**
 	 * @param localName
 	 * @param libraryName
 	 * @return
 	 */
 	private boolean isWidget(String tagName)
     {
 	    return (WidgetConfig.getClientClass(tagName) != null);
     }
 	
 	/**
 	 * @param cruxTagName
 	 * @return
 	 */
 	private boolean isWidgetSubTag(String cruxTagName)
     {
 		if (cruxTagName.indexOf('_') == cruxTagName.lastIndexOf('_'))
 		{
 			return false;
 		}
 		return widgetsSubTags.contains(cruxTagName);
     }
 	
 	/**
 	 * 
 	 */
 	private void outdent()
 	{
 		jsIndentationLvl--;
 	}
 
 	/**
 	 * @param cruxTagName
 	 */
 	private void setCurrentWidgetTag(String cruxTagName)
 	{
 		this.cruxTagName = cruxTagName;
 	}
 
 	/**
 	 * @param cruxPageElement
 	 * @param htmlElement
 	 * @param htmlDocument
 	 */
 	private void translateCruxCoreElements(Element cruxPageElement, Element htmlElement, Document htmlDocument)
     {
 	    String nodeName = cruxPageElement.getLocalName();
 	    if (nodeName.equals("splashScreen"))
 	    {
 	    	translateSplashScreen(cruxPageElement, htmlElement, htmlDocument);
 	    }
 	    else if (nodeName.equals("screen"))
 	    {
 	    	translateDocument(cruxPageElement, htmlElement, htmlDocument, true);
 	    }
     }
 	
 	/**
 	 * @param cruxPageElement
 	 * @param htmlElement
 	 * @param htmlDocument
 	 */
 	private void translateCruxInnerTags(Element cruxPageElement, Element htmlElement, Document htmlDocument)
     {
 		boolean htmlContainerWidget = isHtmlContainerWidget(cruxPageElement);
 		if (htmlContainerWidget || ((isWidget(getCurrentWidgetTag())) && isHTMLChild(cruxPageElement)))
 		{
 			Element widgetHolder = htmlDocument.createElement("div");
 			widgetHolder.setAttribute("id", "_crux_"+cruxPageElement.getAttribute("id"));
 			htmlElement.appendChild(widgetHolder);
 			translateDocument(cruxPageElement, widgetHolder, htmlDocument, htmlContainerWidget);
 		}
 		else
 		{
 			translateDocument(cruxPageElement, htmlElement, htmlDocument, false);
 		}
     }
 
 	/**
 	 * @param cruxPageDocument
 	 * @param htmlDocument
 	 * @throws HTMLBuilderException 
 	 */
 	private void translateDocument(Document cruxPageDocument, Document htmlDocument) throws HTMLBuilderException
     {
 		Element cruxPageElement = cruxPageDocument.getDocumentElement();
 		Node htmlElement = htmlDocument.importNode(cruxPageElement, false);
 		htmlDocument.appendChild(htmlElement);
 		clearCurrentWidget();
 		translateDocument(cruxPageElement, htmlElement, htmlDocument, true);
 		clearCurrentWidget();
 		generateCruxMetaDataElement(getCruxPageBodyElement(cruxPageDocument), getHtmlHeadElement(htmlDocument), htmlDocument);
     }
 
 	/**
 	 * @param cruxPageElement
 	 * @param htmlElement
 	 * @param htmlDocument
 	 */
 	private void translateDocument(Node cruxPageElement, Node htmlElement, Document htmlDocument, boolean copyHtmlNodes)
     {
 		NodeList childNodes = cruxPageElement.getChildNodes();
 		if (childNodes != null)
 		{
 			for (int i=0; i<childNodes.getLength(); i++)
 			{
 				Node child = childNodes.item(i);
 				String namespaceURI = child.getNamespaceURI();
 				
 				if (namespaceURI != null && namespaceURI.equals(CRUX_CORE_NAMESPACE))
 				{
 				    translateCruxCoreElements((Element)child, (Element)htmlElement, htmlDocument);
 				}
 				else if (namespaceURI != null && namespaceURI.startsWith(WIDGETS_NAMESPACE_PREFIX))
 				{
 					String widgetType = getCurrentWidgetTag(); 
 					updateCurrentWidgetTag((Element)child);
 					translateCruxInnerTags((Element)child, (Element)htmlElement, htmlDocument);
 					setCurrentWidgetTag(widgetType);
 				}
 				else
 				{
 					Node htmlChild;
 					if (copyHtmlNodes)
 					{
 						htmlChild = htmlDocument.importNode(child, false);
 						htmlElement.appendChild(htmlChild);
 					}
 					else
 					{
 						htmlChild = htmlElement;
 					}
 					translateDocument(child, htmlChild, htmlDocument, copyHtmlNodes);
 				}
 			}
 		}
     }
 	
 	/**
 	 * @param cruxPageNode
 	 * @param htmlNode
 	 * @param htmlDocument
 	 */
 	private void translateSplashScreen(Element cruxPageNode, Element htmlNode, Document htmlDocument)
 	{
 		Element splashScreen = htmlDocument.createElement("div");
 		splashScreen.setAttribute("id", "cruxSplashScreen");
 		
 		String style = cruxPageNode.getAttribute("style");
 		if (!StringUtils.isEmpty(style))
 		{
 			splashScreen.setAttribute("style", style);
 		}
 		String transactionDelay = cruxPageNode.getAttribute("transactionDelay");
 		if (!StringUtils.isEmpty(transactionDelay))
 		{
 			splashScreen.setAttribute("transactionDelay", transactionDelay);
 		}
 		htmlNode.appendChild(splashScreen);
 	}
 	
 	
 	/**
 	 * @param cruxPageElement
 	 * @return
 	 */
 	private String updateCurrentWidgetTag(Element cruxPageElement)
     {
 		String canditateWidgetType = getLibraryName(cruxPageElement)+"_"+cruxPageElement.getLocalName();
 		if (StringUtils.isEmpty(cruxTagName))
 		{
 			cruxTagName = canditateWidgetType;
 		}
 		else
 		{
 			cruxTagName += "_"+cruxPageElement.getLocalName(); 
 		}
 		if (!isWidgetSubTag(cruxTagName) && (isWidget(canditateWidgetType)))
 		{
 			cruxTagName = canditateWidgetType;
 		}
 		
 		if (isReferencedWidget(cruxTagName))
 		{
 			cruxTagName = getReferencedWidget(cruxTagName);
 		}
 		return cruxTagName;
     }
 
 	/**
 	 * @param out
 	 * @throws IOException
 	 */
 	private void write(Document htmlDocument, Writer out) throws IOException
 	{
 		DocumentType doctype = htmlDocument.getDoctype();
 		
 		if (doctype != null)
 		{
 			out.write("<!DOCTYPE " + doctype.getName() + ">\n");
 		}
 	    HTMLUtils.write(htmlDocument.getDocumentElement(), out, indentOutput);
 	}
 	
 	/**
 	 * @param cruxArrayMetaData
 	 */
 	private void writeIndentationSpaces(StringBuilder cruxArrayMetaData)
     {
 	    cruxArrayMetaData.append("\n");
 	    for (int i=0; i< jsIndentationLvl; i++)
 	    {
 	    	cruxArrayMetaData.append("  ");
 	    }
     }	
 }
