 /*===========================================================================
   Copyright (C) 2008-2012 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package org.w3c.its;
 
 import java.io.ByteArrayInputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.GenericAnnotation;
 import net.sf.okapi.common.annotation.GenericAnnotationType;
 import net.sf.okapi.common.annotation.GenericAnnotations;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  * Holds the information on a given entry that has a target pointer.
  */
 public class ITSEngine implements IProcessor, ITraversal {
 
 	public static final String    ITS_VERSION1 = "1.0";
 	public static final String    ITS_VERSION2 = "2.0";
 	
 	public static final String    XML_NS_URI      = "http://www.w3.org/XML/1998/namespace";
 	public static final String    XML_NS_PREFIX   = "xml";
 	public static final String    ITS_NS_URI      = "http://www.w3.org/2005/11/its";
 	public static final String    ITS_NS_PREFIX   = "its";
 	public static final String    ITSX_NS_URI     = "http://www.w3.org/2008/12/its-extensions";
 	public static final String    ITSX_NS_PREFIX  = "itsx";
 	public static final String    XLINK_NS_URI    = "http://www.w3.org/1999/xlink";
 	public static final String    XLINK_NS_PREFIX = "xlink";
 	public static final String    HTML_NS_URI     = "http://www.w3.org/1999/xhtml";
 	public static final String    HTML_NS_PREFIX  = "h";
 	public static final String    ITS_MIMETYPE    = "application/its+xml";
 	
 	private static final String   FLAGNAME = "\u00ff"; // Name of the user-data property that holds the flags
 	private static final String   FLAGSEP  = "\u001c"; // Separator between data categories
 	
 	// Must have '?' as many times as there are FP_XXX entries +1
 	// Must have +FLAGSEP as many times as there are FP_XXX_DATA entries +1
 	private static final String   FLAGDEFAULTDATA     = "????????????????"
 		+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP
 		+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP+FLAGSEP;
 
 	private static final String SRC_TRGPTRFLAGNAME = "\u10ff"; // Name of the user-data property that holds the target pointer flag in the source
 	private static final String TRG_TRGPTRFLAGNAME = "\u20ff"; // Name of the user-data property that holds the target pointer flag in the target
 	
 	private static final String PTRPREFIX = "@@"; // If length of PTRPREFIX changes: code needs to be updated
 
 	// Indicator position
 	private static final int      FP_TRANSLATE             = 0;
 	private static final int      FP_DIRECTIONALITY        = 1;
 	private static final int      FP_WITHINTEXT            = 2;
 	private static final int      FP_TERMINOLOGY           = 3;
 	private static final int      FP_LOCNOTE               = 4;
 	private static final int      FP_PRESERVEWS            = 5;
 	private static final int      FP_LANGINFO              = 6;
 	private static final int      FP_DOMAIN                = 7;
 	private static final int      FP_EXTERNALRES           = 8;
 	private static final int      FP_LOCFILTER             = 9;
 	private static final int      FP_LQISSUE               = 10;
 	private static final int      FP_STORAGESIZE           = 11;
 	private static final int      FP_ALLOWEDCHARS          = 12;
 	private static final int      FP_SUBFILTER             = 13;
 	private static final int      FP_TARGETPOINTER         = 14;
 	private static final int      FP_TOOLSREF              = 15;
 	
 	// Data position 
 	private static final int      FP_TERMINOLOGY_DATA      = 0;
 	private static final int      FP_LOCNOTE_DATA          = 1;
 	private static final int      FP_LANGINFO_DATA         = 2;
 	private static final int      FP_TARGETPOINTER_DATA    = 3;
 	private static final int      FP_IDVALUE_DATA          = 4;
 	private static final int      FP_DOMAIN_DATA           = 5;
 	private static final int      FP_EXTERNALRES_DATA      = 6;
 	private static final int      FP_LOCFILTER_DATA        = 7;
 	private static final int      FP_LQISSUE_DATA          = 8;
 	private static final int      FP_STORAGESIZE_DATA      = 9;
 	private static final int      FP_ALLOWEDCHARS_DATA     = 10;
 	private static final int      FP_SUBFILTER_DATA        = 11;
 	private static final int      FP_TOOLSREF_DATA         = 12;
 	
 	private static final int      INFOTYPE_TEXT            = 0;
 	private static final int      INFOTYPE_REF             = 1;
 	private static final int      INFOTYPE_POINTER         = 2;
 	private static final int      INFOTYPE_REFPOINTER      = 3;
 
 	private final boolean isHTML5;
 	
 	private DocumentBuilderFactory xmlFactory;
 	
 	private Document doc;
 	private URI docURI;
 	private NSContextManager nsContext;
 	private VariableResolver varResolver;
 	private XPathFactory xpFact;
 	private XPath xpath;
 	private ArrayList<ITSRule> rules;
 	private Node node;
 	private boolean startTraversal;
 	private Stack<ITSTrace> trace;
 	private boolean backTracking;
 	private boolean translatableAttributeRuleTriggered;
 	private boolean targetPointerRuleTriggered;
 	private boolean hasTargetPointer;
 	private String version;
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	public ITSEngine (Document doc,
 		URI docURI)
 	{
 		// For backward compatibility
 		this(doc, docURI, false, null);
 	}
 	
 	public ITSEngine (Document doc,
 		URI docURI,
 		boolean isHTML5,
 		Map<String, String> map)
 	{
 		this.doc = doc;
 		this.docURI = docURI;
 		this.isHTML5 = isHTML5;
 		node = null;
 		rules = new ArrayList<ITSRule>();
 		nsContext = new NSContextManager();
 		nsContext.addNamespace(ITS_NS_PREFIX, ITS_NS_URI);
 		nsContext.addNamespace(ITSX_NS_PREFIX, ITSX_NS_URI);
 		if ( isHTML5 ) {
 			nsContext.addNamespace(HTML_NS_PREFIX, HTML_NS_URI);
 		}
 		varResolver = new VariableResolver();
 		if ( !Util.isEmpty(map) ) {
 			for ( String name : map.keySet() ) {
 				varResolver.add(new QName(name), map.get(name), true);
 			}
 		}
 
 		// Macintosh work-around
 		// When you use -XstartOnFirstThread as a java -Xarg on Leopard, your ContextClassloader gets set to null.
 		// That is not the case on 10.4 or with Windows or Linux flavors
 		// This allows XPathFactory.newInstance() to have a non-null context
 		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
 		// end work-around
 		xpFact = XPathFactory.newInstance();
 
 		xpath = xpFact.newXPath();
 		xpath.setNamespaceContext(nsContext);
 		xpath.setXPathVariableResolver(varResolver);
 	}
 	
 	public void setVariables (Map<String, String> map) {
 		
 	}
 
 	/**
 	 * Indicates if the processed document has triggered a rule for a translatable attribute.
 	 * This must be called only after {@link #applyRules(long)}.
 	 * @return true if the document has triggered a rule for a translatable attribute.
 	 */
 	public boolean getTranslatableAttributeRuleTriggered () {
 		return translatableAttributeRuleTriggered;
 	}
 
 	/**
 	 * Indicates if the processed document has triggered a target pointer rule.
 	 * This must be called only after {@link #applyRules(long)}. 
 	 * @return true if the processed document has triggered a target pointer rule.
 	 */
 	public boolean getTargetPointerRuleTriggered () {
 		return targetPointerRuleTriggered;
 	}
 	
 	/**
 	 * Gets internal XPath object used in this ITS engine. 
 	 * @return the internal XPath object used in this ITS engine.
 	 */
 	public XPath getXPath () {
 		return xpath;
 	}
 	
 	private void ensureDocumentBuilderFactoryExists () {
 		if ( xmlFactory == null ) { 
 			xmlFactory = DocumentBuilderFactory.newInstance();
 			xmlFactory.setNamespaceAware(true);
 			xmlFactory.setValidating(false);
 		}
 	}
 	
 	private Document parseXMLDocument (String uriString) {
 		ensureDocumentBuilderFactoryExists();
 		try {
 			return xmlFactory.newDocumentBuilder().parse(uriString);
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error parsing an XML document.\n"+e.getMessage(), e);
 		}
 	}
 	
 	private Document parseXMLDocument (InputSource is) {
 		ensureDocumentBuilderFactoryExists();
 		try {
 			return xmlFactory.newDocumentBuilder().parse(is);
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error parsing an XML document.\n"+e.getMessage(), e);
 		}
 	}
 	
 	private Document parseHTMLDocument (String uriString) {
 		HtmlDocumentBuilder docBuilder = new HtmlDocumentBuilder();
 		try {
 			return docBuilder.parse(uriString);
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException("Error parsing an HTML document.\n"+e.getMessage(), e);
 		}
 	}
 
 	public void addExternalRules (URI docURI) {
 		try {
 			Document rulesDoc = parseXMLDocument(docURI.toString());
 			addExternalRules(rulesDoc, docURI);
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void addExternalRules (Document rulesDoc,
 		URI docURI)
 	{
 		compileRules(rulesDoc, docURI, false);
 	}
 	
 	private void compileRulesInScripts (Document hostDoc,
 		URI docURI,
 		boolean isInternal)
 	{
 		try {
 			// Look for all script elements with ITS MIME type
 			XPathExpression expr = xpath.compile("//"+HTML_NS_PREFIX+":script[@type='"+ITS_MIMETYPE+"']");
 			NodeList nl = (NodeList)expr.evaluate(hostDoc, XPathConstants.NODESET);
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				// Process the rules in the order they are declared
 				Element elem = (Element)nl.item(i);
 				String content = elem.getTextContent();
 				if ( content == null ) continue;
 				// Strip encapsulation and white spaces
 				content = content.trim();
 				if ( content.startsWith("<!--")) content = content.substring(4);
 				if ( content.endsWith("-->")) content = content.substring(0, content.length()-3);
 				content = content.trim();
 				// Parse the content
 				InputSource is = new InputSource(new ByteArrayInputStream(content.getBytes()));
 				Document scriptDoc = parseXMLDocument(is);
 				// And compile the rules
 				compileRules(scriptDoc, docURI, isInternal);
 			}
 		}
 		catch ( Throwable e ) {
 			throw new ITSException("Error processing ITS markup in HTML script.\n"+e.getMessage());
 		}
 		
 	}
 	
 	private void compileRules (Document rulesDoc,
 		URI docURI,
 		boolean isInternal)
 	{
 		try {
 			// Compile the namespaces
 			XPathExpression expr = xpath.compile("//*[@selector]//namespace::*");
 			NodeList nl = (NodeList)expr.evaluate(rulesDoc, XPathConstants.NODESET);
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				String prefix = nl.item(i).getLocalName();
 				if ( "xml".equals(prefix) ) continue; // Set by default
 				String uri = nl.item(i).getNodeValue();
 				nsContext.addNamespace(prefix, uri);
 			}
 			
 			// Compile the rules
 			// First: get the its:rules element(s)
 			expr = xpath.compile("//"+ITS_NS_PREFIX+":rules");
 			nl = (NodeList)expr.evaluate(rulesDoc, XPathConstants.NODESET);
 			if ( nl.getLength() == 0 ) return; // Nothing to do
 			
 			// Process each its:rules element
 			Element rulesElem;
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				rulesElem = (Element)nl.item(i);
 				// Check version
 				version = rulesElem.getAttributeNS(null, "version");
 				if ( !version.equals(ITS_VERSION1) && !version.equals(ITS_VERSION2) ) {
 					throw new ITSException(String.format("Invalid or missing ITS version (\"%s\")", version));
 				}
 
 				// Check for link
 				String href = rulesElem.getAttributeNS(XLINK_NS_URI, "href");
 				if ( href.length() > 0 ) {
 					int n = href.lastIndexOf('#');
 					if ( n > -1 ) {
 						href = href.substring(0, n);
 					}
 
 					// xlink:href allows the use of xml:base so we need to calculate it
 					// The initial base is the folder of the current document
 					String baseFolder = "";
 					if ( docURI != null) baseFolder = getPartBeforeFile(docURI);
 					
 					// Then we look for the last xml:base specified
 					Node node = rulesElem;
 					while ( node != null ) {
 						if ( node.getNodeType() == Node.ELEMENT_NODE ) {
 							//TODO: Relative path with ../../ constructs
 							String xmlBase = ((Element)node).getAttribute("xml:base");
 							if ( xmlBase.length() > 0 ) {
 								if ( xmlBase.endsWith("/") )
 									xmlBase = xmlBase.substring(0, xmlBase.length()-1);
 								if ( !baseFolder.startsWith("/") )
 									baseFolder = xmlBase + "/" + baseFolder;
 								else
 									baseFolder = xmlBase + baseFolder;
 							}
 						}
 						node = node.getParentNode(); // Back-track to parent
 					}
 					if ( baseFolder.length() > 0 ) {
 						if ( baseFolder.endsWith("/") )
 							baseFolder = baseFolder.substring(0, baseFolder.length()-1);
 						if ( !href.startsWith("/") ) href = baseFolder + "/" + href;
 						else href = baseFolder + href;
 					}
 
 					// Load the document and the rules
 					URI linkedDoc = new URI(href);
 					loadLinkedRules(linkedDoc, isInternal);
 				}
 
 				// Process each rule inside its:rules
 				expr = xpath.compile("//"+ITS_NS_PREFIX+":*|//"+ITSX_NS_PREFIX+":*");
 				NodeList nl2 = (NodeList)expr.evaluate(rulesElem, XPathConstants.NODESET);
 				if ( nl2.getLength() == 0 ) break; // Nothing to do, move to next its:rules
 				
 				Element ruleElem;
 				for ( int j=0; j<nl2.getLength(); j++ ) {
 					ruleElem = (Element)nl2.item(j);
 					String locName = ruleElem.getLocalName();
 					if ( "translateRule".equals(locName) ) {
 						compileTranslateRule(ruleElem, isInternal);
 					}
 					else if ( "withinTextRule".equals(locName) ) {
 						compileWithinTextRule(ruleElem, isInternal);
 					}
 					else if ( "langRule".equals(locName) ) {
 						compileLangRule(ruleElem, isInternal);
 					}
 					else if ( "dirRule".equals(locName) ) {
 						compileDirRule(ruleElem, isInternal);
 					}
 					else if ( "locNoteRule".equals(locName) ) {
 						compileLocNoteRule(ruleElem, isInternal);
 					}
 					else if ( "termRule".equals(locName) ) {
 						compileTermRule(ruleElem, isInternal);
 					}
 					else if ( "idValueRule".equals(locName) ) {
 						compileIdValueRule(ruleElem, isInternal);
 					}
 					else if ( "domainRule".equals(locName) ) {
 						compileDomainRule(ruleElem, isInternal);
 					}
 					else if ( "targetPointerRule".equals(locName) ) {
 						compileTargetPointerRule(ruleElem, isInternal);
 					}
 					else if ( "localeFilterRule".equals(locName) ) {
 						compileLocaleFilterRule(ruleElem, isInternal);
 					}
 					else if ( "preserveSpaceRule".equals(locName) ) {
 						compilePrserveSpaceRule(ruleElem, isInternal);
 					}
 					else if ( "externalResourceRefRule".equals(locName) ) {
 						compileExternalResourceRule(ruleElem, isInternal);
 					}
 					else if ( "locQualityIssueRule".equals(locName) ) {
 						compileLocQualityIssueRule(ruleElem, isInternal);
 					}
 					else if ( "storageSizeRule".equals(locName) ) {
 						compileStorageSizeRule(ruleElem, isInternal);
 					}
 					else if ( "allowedCharactersRule".equals(locName) ) {
 						compileAllowedCharactersRule(ruleElem, isInternal);
 					}
 					else if ( "subFilterRule".equals(locName) ) {
 						compileSubFilterRule(ruleElem, isInternal);
 					}
 					else if ( "param".equals(locName) ) {
 						processParam(ruleElem);
 					}
 					else if ( !"rules".equals(locName)
 						&& !"span".equals(locName) 
 						&& !"locQualityIssues".equals(locName)
 						&& !"locQualityIssue".equals(locName)
 						&& !"locNote".equals(locName) ) {
 						logger.warn("Unknown element '{}'.", ruleElem.getNodeName());
 					}
 				}
 			}
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( URISyntaxException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void processParam (Element elem) {
 		String value = elem.getTextContent();
 		String name = elem.getAttribute("name");
 		if ( name.isEmpty() ) {
 			throw new ITSException("Invalid value for 'name' in param.");
  		}
 		// Do not overwrite existing values (the element defines defaults)
 		varResolver.add(new QName(name), value, false);
 	}
 	
 	/**
 	 * Gets the URI part before the file name.
 	 * @param uri The URI to process.
 	 * @return the URI part before the file name.
 	 */
 	public String getPartBeforeFile (URI uri) {
 		String tmp = uri.toString();
 		int n = tmp.lastIndexOf('/');
 		if ( n == -1 ) return uri.toString();
 		else return tmp.substring(0, n+1);
 	}
 	
 	private void loadLinkedRules (URI docURI,
 		boolean isInternal)
 	{
 		try {
 			Document rulesDoc = parseXMLDocument(docURI.toString());
 			compileRules(rulesDoc, docURI, isInternal);
 		}
 		catch ( Throwable e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void compileTranslateRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_TRANSLATE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String value = elem.getAttribute("translate");
 		if ( "yes".equals(value) ) rule.flag = true;
 		else if ( "no".equals(value) ) rule.flag = false;
 		else throw new ITSException("Invalid value for 'translate'.");
 
 		// idValue extension (deprecated but supported)
 		value = elem.getAttributeNS(ITSX_NS_URI, "idValue");
 		if ( !value.isEmpty() ) {
 			if ( version.equals(ITS_VERSION2) ) {
 				// Warn if the extension is used in ITS 2.0
 				logger.warn("This document uses the {}:idValue extension instead of the ITS 2.0 Id Value data category.",
 					ITSX_NS_URI);
 			}
 			rule.idValue = value;
 		}
 		
 		// whiteSpaces extension (deprecated but supported)
 		value = elem.getAttributeNS(ITSX_NS_URI, "whiteSpaces");
 		if ( !value.isEmpty() ) {
 			if ( version.equals(ITS_VERSION2) ) {
 				// Warn if the extension is used in ITS 2.0
 				logger.warn("This document uses the {}:whiteSpaces extension instead of the ITS 2.0 Preserve Space data category.",
 					ITSX_NS_URI);
 			}
 			if ( "preserve".equals(value) ) rule.preserveWS = true;
 			else if ( "default".equals(value) ) rule.preserveWS = false;
 			else throw new ITSException("Invalid value for 'whiteSpaces'.");
 		}
 		
 		rules.add(rule);
 	}
 
 	private void compileDirRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_DIRECTIONALITY);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String value = elem.getAttribute("dir");
 		if ( "ltr".equals(value) ) rule.value = DIR_LTR;
 		else if ( "rtl".equals(value) ) rule.value = DIR_RTL;
 		else if ( "lro".equals(value) ) rule.value = DIR_LRO;
 		else if ( "rlo".equals(value) ) rule.value = DIR_RLO;
 		else throw new ITSException("Invalid value for 'dir'.");
 		rules.add(rule);
 	}
 
 	private void compileWithinTextRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_WITHINTEXT);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 			
 		String value = elem.getAttribute("withinText");
 		if ( "yes".equals(value) ) rule.value = WITHINTEXT_YES;
 		else if ( "no".equals(value) ) rule.value = WITHINTEXT_NO;
 		else if ( "nested".equals(value) ) rule.value = WITHINTEXT_NESTED;
 		else throw new ITSException("Invalid value for 'withinText'.");
 			
 		rules.add(rule);
 	}
 
 	private void compileIdValueRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_IDVALUE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 			
 		String value = elem.getAttribute("idValue");
 		if ( value.isEmpty() ) {
 			throw new ITSException("Invalid value for 'idValue'.");
 		}
 		rule.idValue = value;
 		rules.add(rule);
 	}
 
 	private void compileDomainRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_DOMAIN);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 				
 		String pointer = elem.getAttribute("domainPointer");
 		if ( pointer.isEmpty() ) {
 			throw new ITSException("Invalid value for 'domainPointer'.");
 		}
 		rule.info = pointer;
 
 		// Process domainMapping attribute if it's there
 		rule.map = fromStringToMap(elem.getAttribute("domainMapping"));
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileExternalResourceRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_EXTERNALRES);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 				
 		String pointer = elem.getAttribute("externalResourceRefPointer");
 		if ( pointer.isEmpty() ) {
 			throw new ITSException("Invalid value for 'externalResourceRefPointer'.");
 		}
 		rule.info = pointer;
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileStorageSizeRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_STORAGESIZE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		String np[] = retrieveStorageSizeData(elem, false, false);
 		
 		String storageSizeP = null;
 		if ( elem.hasAttribute("storageSizePointer") )
 			storageSizeP = elem.getAttribute("storageSizePointer");
 		
 		String storageEncodingP = null;
 		if (elem.hasAttribute("storageEncodingPointer"))
 			storageEncodingP = elem.getAttribute("storageEncodingPointer");
 		
 		// Check we have the mandatory attributes
 		if ( Util.isEmpty(np[0]) && Util.isEmpty(storageSizeP) ) {
 			throw new ITSException("You must have at least an attribute storageSize or storageSizePointer.");
 		}
 		
 		rule.map = new HashMap<String, String>();
 
 		// Check pointer vs non-pointers
 		if ( !Util.isEmpty(np[0]) ) {
 			if ( !Util.isEmpty(storageSizeP) ) {
 				throw new ITSException("Cannot have both storageSize and storageSizePointer.");
 			}
 			rule.map.put("size", np[0]);
 		}
 		else {
 			rule.map.put("sizePointer", storageSizeP);
 		}
 		
 		if ( !Util.isEmpty(np[1]) ) {
 			if ( !Util.isEmpty(storageEncodingP) ) {
 				throw new ITSException("Cannot have both storageEncoding and storageEncodingPointer.");
 			}
 			rule.map.put("encoding", np[1]);
 		}
 		else {
 			rule.map.put("encodingPointer", storageEncodingP);
 		}
 
 		// No pointer for line break type
 		if ( !Util.isEmpty(np[2]) ) {
 			rule.map.put("linebreak", np[2]);
 		}
 		
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileAllowedCharactersRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_ALLOWEDCHARS);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		rule.info = retrieveAllowedCharsData(elem, false, false);
 		
 		String allowedCharsP = null;
 		if ( elem.hasAttribute("allowedCharactersPointer") )
 			allowedCharsP = elem.getAttribute("allowedCharactersPointer");
 		
 		// Check we have the mandatory attributes
 		if ( Util.isEmpty(rule.info) && Util.isEmpty(allowedCharsP) ) {
 			throw new ITSException("You must have at least an attribute allowedCharacters or allowedCharactersPointer.");
 		}
 		
 		if ( !Util.isEmpty(rule.info) ) {
 			if ( !Util.isEmpty(allowedCharsP) ) {
 				throw new ITSException("Cannot have both allowedCharacters and allowedCharactersPointer.");
 			}
 		}
 		else {
 			rule.info = allowedCharsP;
 			rule.infoType = INFOTYPE_POINTER;
 		}
 		
 		// Add the rule
 		rules.add(rule);
 	}
 	
 	private void compileLocQualityIssueRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LOCQUALITYISSUE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 
 		// Get the local attributes
 		String np[] = retrieveLocQualityIssueData(elem, false, false);
 		
 		String issuesRefP = null;
 		if ( elem.hasAttribute("locQualityIssuesRefPointer") )
 			issuesRefP = elem.getAttribute("locQualityIssuesRefPointer");
 		
 		String typeP = null;
 		if (elem.hasAttribute("locQualityIssueTypePointer"))
 			typeP = elem.getAttribute("locQualityIssueTypePointer");
 		
 		String commentP = null;
 		if ( elem.hasAttribute("locQualityIssueCommentPointer"))
 			commentP = elem.getAttribute("locQualityIssueCommentPointer");
 		
 		// Check we have the mandatory attributes
 		if (( Util.isEmpty(np[0]) && Util.isEmpty(issuesRefP) )
 			&& ( Util.isEmpty(np[1]) && Util.isEmpty(typeP) )
 			&& ( Util.isEmpty(np[2]) && Util.isEmpty(commentP) ))
 		{
 			throw new ITSException("You must have at least a type or a comment or isses reference ainformation defined.");
 		}
 		rule.annotations = new GenericAnnotations();
 		GenericAnnotation ann = addIssueItem(rule.annotations);
 		
 		if ( !Util.isEmpty(np[0]) ) {
 			if ( !Util.isEmpty(issuesRefP) ) {
 				throw new ITSException("Cannot have both locQualityIssuesRef and locQualityIssuesRefPointer.");
 			}
 			rule.info = np[0];
 			rule.infoType = INFOTYPE_REF;
 		}
 		else if ( issuesRefP != null ) {
 			rule.info = issuesRefP;
 			rule.infoType = INFOTYPE_REFPOINTER;
 		}
 
 		// For the annotation info, we add '@@' in front if it is a pointer
 		
 		if ( !Util.isEmpty(np[1]) ) {
 			if ( !Util.isEmpty(typeP) ) {
 				throw new ITSException("Cannot have both locQualityIssueType and locQualityIssueTypePointer.");
 			}
 			ann.setString(GenericAnnotationType.LQI, np[1]);
 			// TODO: verify the value?
 		}
 		else if ( typeP != null ) {
 			ann.setString(GenericAnnotationType.LQI_TYPE, PTRPREFIX+typeP);
 		}
 		
 		// Get the comment
 		if ( !Util.isEmpty(np[2]) ) {
 			if ( !Util.isEmpty(commentP) ) {
 				throw new ITSException("Cannot have both locQualityIssueComment and locQualityIssueCommentPointer.");
 			}
 			ann.setString(GenericAnnotationType.LQI_COMMENT, np[2]);
 		}
 		else if ( commentP != null ) {
 			ann.setString(GenericAnnotationType.LQI_COMMENT, PTRPREFIX+commentP);
 		}
 		
 		// Get the optional severity
 		String severityP = null;
 		if ( elem.hasAttribute("locQualityIssueSeverityPointer") )
 			severityP = elem.getAttribute("locQualityIssueSeverityPointer");
 		if ( !Util.isEmpty(np[3]) ) {
 			if ( !Util.isEmpty(severityP) ) {
 				throw new ITSException("Cannot have both locQualityIssueSeverity and locQualityIssueSeverityPointer.");
 			}
 			// Do not convert the float yet, this is done when triggering the rule
 			ann.setString(GenericAnnotationType.LQI_SEVERITY, np[3]);
 		}
 		else if ( severityP != null ) {
 			ann.setString(GenericAnnotationType.LQI_SEVERITY, PTRPREFIX+severityP);
 		}
 		
 		// Get the optional profile reference
 		String profileRefP = null;
 		if ( elem.hasAttribute("locQualityIssueProfileRefPointer"))
 			profileRefP = elem.getAttribute("locQualityIssueProfileRefPointer");
 		if ( !Util.isEmpty(np[4]) ) {
 			if ( !Util.isEmpty(profileRefP) ) {
 				throw new ITSException("Cannot have both locQualityIssueProfileRef and locQualityIssueProfileRefPointer.");
 			}
 			ann.setString(GenericAnnotationType.LQI_PROFILEREF, np[4]);
 		}
 		else if ( profileRefP != null ) {
 			ann.setString(GenericAnnotationType.LQI_PROFILEREF, PTRPREFIX+profileRefP);
 		}
 
 		// Get the optional enabled
 		String enabledP = null;
 		if ( elem.hasAttribute("locQualityIssueEnabledPointer")) {
 			profileRefP = elem.getAttribute("locQualityIssueEnabledPointer");
 			ann.setString(GenericAnnotationType.LQI_ENABLED, PTRPREFIX+enabledP);
 		}
 		else { // either default or set value
 			ann.setString(GenericAnnotationType.LQI_ENABLED, np[5]);
 		}
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compileLocaleFilterRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LOCFILTER);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		// Retrieve the list
 		rule.info = retrieveLocaleFilterList(elem, false, false);
 		// Add the rule
 		rules.add(rule);
 	}
 	
 	private void compileSubFilterRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_SUBFILTER);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		// Retrieve the list
 		rule.info = retrieveSubFilter(elem, false, false);
 		// Add the rule
 		rules.add(rule);
 	}
 
 	private void compilePrserveSpaceRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_PRESERVESPACE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		// Get the value
 		String value = elem.getAttribute("space");
 		if (( !"preserve".equals(value) ) && ( !"default".equals(value) )) {
 			throw new ITSException("Invalid value for 'space'.");
 		}
 		rule.preserveWS = "preserve".equals(value);
 		// Add the rule
 		rules.add(rule);
 	}
 		
 
 	private void compileTargetPointerRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_TARGETPOINTER);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 				
 		String pointer = elem.getAttribute("targetPointer");
 		if ( pointer.isEmpty() ) {
 			throw new ITSException("Invalid value for 'targetPointer'.");
 		}
 		rule.info = pointer;
 
 		// Add the rule
 		rules.add(rule);
 	}
 
 	/**
 	 * Converts a string like domainMapping to a map.
 	 * @param mapping the string to process.
 	 * @return the map for the given string, or null if there is no values. 
 	 */
 	private Map<String, String> fromStringToMap (String mapping) {
 		if ( mapping.isEmpty() ) return null;
 		
 		Map<String, String> map = null;
 
 		if ( !mapping.isEmpty() ) {
 			// Parse the list of paired values
 			// Split list on commas
 			String[] pairs = mapping.split(",", 0);
 			// Split the pairs
 			char endQuote = 0x00; int state = 0;
 			for ( String pair : pairs ) {
 				pair = pair.trim();
 				StringBuilder left = new StringBuilder();
 				StringBuilder right = new StringBuilder();
 				StringBuilder str = left;
 				for ( int i=0; i<pair.length(); i++ ) {
 					char ch = pair.charAt(i);
 					switch ( ch ) {
 					case '\"':
 						if ( state == 0 )  {
 							endQuote = ch;
 							state = 1;
 						}
 						else {
 							if ( ch == endQuote ) state = 0; // End of string
 							else str.append(ch); // Else: we store
 						}
 						continue;
 					case '\'':
 						if ( state == 0 )  {
 							endQuote = ch;
 							state = 1;
 						}
 						else {
 							if ( ch == endQuote ) state = 0; // End of string
 							else str.append(ch); // Else: we store
 						}
 						continue;
 					case ' ':
 						// If it's a space inside a quoted string: we add to the string
 						// Otherwise it we change to the right side value
 						if ( state == 1 ) str.append(' ');
 						else str = right;
 						continue;
 					default:
 						str.append(pair.charAt(i));
 						break;
 					}
 				}
 				
 				if (( left.length() == 0 ) || ( right.length() == 0 )) {
 					throw new ITSException("Invalid pair in mapping value.");
 				}
 				
 				if ( map == null ) {
 					map = new LinkedHashMap<String, String>();
 				}
 				// Left value must be lowercase
 				map.put(left.toString().toLowerCase(), right.toString());
 			}
 		}
 		
 		return map;
 	}
 	
 	private void compileTermRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_TERMINOLOGY);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		// term
 		String value = elem.getAttribute("term");
 		if ( "yes".equals(value) ) rule.flag = true;
 		else if ( "no".equals(value) ) rule.flag = false;
 		else throw new ITSException("Invalid value for 'term'.");
 		
 		value = elem.getAttribute("termInfoPointer");
 		String value2 = elem.getAttribute("termInfoRef");
 		String value3 = elem.getAttribute("termInfoRefPointer");
 		
 		if ( value.length() > 0 ) {
 			rule.infoType = INFOTYPE_POINTER;
 			rule.info = value;
 			if (( value2.length() > 0 ) || ( value3.length() > 0 )) {
 				throw new ITSException("Too many termInfo attributes specified");
 			}
 		}
 		else {
 			if ( value2.length() > 0 ) {
 				rule.infoType = INFOTYPE_REF;
 				rule.info = value2;
 				if ( value3.length() > 0 ) {
 					throw new ITSException("Too many termInfo attributes specified");
 				}
 			}
 			else {
 				if ( value3.length() > 0 ) {
 					rule.infoType = INFOTYPE_REFPOINTER;
 					rule.info = value3;
 				}
 				// Else: No associate information, rule.termInfo is null
 			}
 		}
 
 		rules.add(rule);
 	}
 
 	private void compileLocNoteRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LOCNOTE);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 
 		rule.flag = retrieveLocNoteType(elem, false, true, false);
 
 		// Try to get the locNote element
 		String value1 = "";
 		NodeList list = elem.getElementsByTagNameNS(ITS_NS_URI, "locNote");
 		if ( list.getLength() > 0 ) {
 			value1 = getTextContent(list.item(0));
 		}
 		// Get the attributes
 		String value2 = elem.getAttribute("locNotePointer");
 		String value3 = elem.getAttribute("locNoteRef");
 		String value4 = elem.getAttribute("locNoteRefPointer");
 		
 		if ( value1.length() > 0 ) {
 			rule.infoType = INFOTYPE_TEXT;
 			rule.info = value1;
 			if (( value2.length() > 0 ) || ( value3.length() > 0 ) || ( value4.length() > 0 )) {
 				throw new ITSException("Too many locNote attributes specified");
 			}
 		}
 		else {
 			if ( value2.length() > 0 ) {
 				rule.infoType = INFOTYPE_POINTER;
 				rule.info = value2;
 				if (( value3.length() > 0 ) || ( value4.length() > 0 )) {
 					throw new ITSException("Too many locNote attributes specified");
 				}
 			}
 			else {
 				if ( value3.length() > 0 ) {
 					rule.infoType = INFOTYPE_REF;
 					rule.info = value3;
 					if ( value4.length() > 0 ) {
 						throw new ITSException("Too many locNote attributes specified");
 					}
 				}
 				else {
 					if ( value4.length() > 0 ) {
 						rule.infoType = INFOTYPE_REFPOINTER;
 						rule.info = value4;
 					}
 				}
 			}
 		}
 		
 		rules.add(rule);
 	}
 
 	private void compileLangRule (Element elem,
 		boolean isInternal)
 	{
 		ITSRule rule = new ITSRule(IProcessor.DC_LANGINFO);
 		rule.selector = elem.getAttribute("selector");
 		rule.isInternal = isInternal;
 		
 		rule.info = elem.getAttribute("langPointer");
 		if ( rule.info.isEmpty() ) {
 			throw new ITSException("langPointer attribute missing.");
 		}
 		rules.add(rule);
 	}
 
 	public void applyRules (long dataCategories) {
 		translatableAttributeRuleTriggered = false;
 		targetPointerRuleTriggered = false;
 		version = "0"; // Needs to be not null (in case there is no ITS at all in file)
 		processGlobalRules(dataCategories);
 		processLocalRules(dataCategories);
 		
 		// Prepare for target pointers if needed
 		prepareTargetPointers();
 	}
 	
 	private void removeFlag (Node node) {
 		//TODO: Any possible optimization, instead of using recursive calls
 		if ( node == null ) return;
 		node.setUserData(FLAGNAME, null, null);
 		if ( node.hasChildNodes() )
 			removeFlag(node.getFirstChild());
 		if ( node.getNextSibling() != null )
 			removeFlag(node.getNextSibling());
 	}
 	
 	public void disapplyRules () {
 		removeFlag(doc.getDocumentElement());
 		translatableAttributeRuleTriggered = false;
 		targetPointerRuleTriggered = false;
 	}
 
 	public boolean backTracking () {
 		return backTracking;
 	}
 
 	public Node nextNode () {
 		if ( startTraversal ) {
 			startTraversal = false;
 			// Set the initial trace with default behaviors
 			ITSTrace startTrace = new ITSTrace();
 			backTracking = false;
 			startTrace.translate = true;
 			startTrace.isChildDone = true;
 			trace.push(startTrace); // For first child
 			node = doc.getFirstChild();
 			trace.push(new ITSTrace(trace.peek(), false));
 			// Overwrite any default behaviors if needed
 			updateTraceData(node);
 			return node;
 		}
 		if ( node != null ) {
 			backTracking = false;
 			if ( !trace.peek().isChildDone && node.hasChildNodes() ) {
 				// Change the flag for the current node
 				ITSTrace tmp = new ITSTrace(trace.peek(), true);
 				trace.pop();
 				trace.push(tmp);
 				// Get the new node and push its flag
 				node = node.getFirstChild();
 				trace.push(new ITSTrace(trace.peek(), false));
 			}
 			else {
 				Node TmpNode = node.getNextSibling();
 				if ( TmpNode == null ) {
 					node = node.getParentNode();
 					trace.pop();
 					backTracking = true;
 				}
 				else {
 					node = TmpNode;
 					trace.pop(); // Remove flag for previous sibling
 					trace.push(new ITSTrace(trace.peek(), false)); // Set new flag for new sibling
 				}
 			}
 		}
 		updateTraceData(node);
 		return node;
 	}
 	
 	/**
 	 * Updates the trace stack.
 	 * @param newNode Node to update 
 	 */
 	private void updateTraceData (Node newNode) {
 		// Check if the node is null
 		if ( newNode == null ) return;
 
 		// Get the flag data
 		String data = (String)newNode.getUserData(FLAGNAME);
 		
 		// If this node has no ITS flags, then we leave the current states
 		// as they are. They have been set by inheritance.
 		if ( data == null ) return;
 		
 		// Otherwise: see if there are any flags to change
 		if ( data.charAt(FP_TRANSLATE) != '?' ) {
 			trace.peek().translate = (data.charAt(FP_TRANSLATE) == 'y');
 		}
 		
 		String value = getFlagData(data, FP_IDVALUE_DATA);
 		if ( !value.isEmpty() ) {
 			trace.peek().idValue = value;
 		}
 		
 		if ( data.charAt(FP_DOMAIN) != '?' ) {
 			trace.peek().domains = getFlagData(data, FP_DOMAIN_DATA);
 		}
 
 		if ( data.charAt(FP_EXTERNALRES) != '?' ) {
 			trace.peek().externalRes = getFlagData(data, FP_EXTERNALRES_DATA);
 		}
 
 		if ( data.charAt(FP_LOCFILTER) != '?' ) {
 			trace.peek().localeFilter = getFlagData(data, FP_LOCFILTER_DATA);
 		}
 		
 		if ( data.charAt(FP_LQISSUE) != '?' ) {
 			trace.peek().lqIssues = new GenericAnnotations(getFlagData(data, FP_LQISSUE_DATA));
 		}
 
 		if ( data.charAt(FP_STORAGESIZE) != '?' ) {
 			String[] values = fromSingleString(getFlagData(data, FP_STORAGESIZE_DATA));
 			trace.peek().storageSize = values[0];
 			trace.peek().storageEncoding = values[1];
 			trace.peek().lineBreakType = values[2];
 		}
 		
 		if ( data.charAt(FP_ALLOWEDCHARS) != '?' ) {
 			trace.peek().allowedChars = getFlagData(data, FP_ALLOWEDCHARS_DATA);
 		}
 		
 		trace.peek().targetPointer = getFlagData(data, FP_TARGETPOINTER_DATA);
 		
 		if ( data.charAt(FP_DIRECTIONALITY) != '?' ) {
 			switch ( data.charAt(FP_DIRECTIONALITY) ) {
 			case '0':
 				trace.peek().dir = DIR_LTR;
 				break;
 			case '1':
 				trace.peek().dir = DIR_RTL;
 				break;
 			case '2':
 				trace.peek().dir = DIR_LRO;
 				break;
 			case '3':
 				trace.peek().dir = DIR_LRO;
 				break;
 			}
 		}
 		
 		if ( data.charAt(FP_WITHINTEXT) != '?' ) {
 			switch ( data.charAt(FP_WITHINTEXT) ) {
 			case '0':
 				trace.peek().withinText = WITHINTEXT_NO;
 				break;
 			case '1':
 				trace.peek().withinText = WITHINTEXT_YES;
 				break;
 			case '2':
 				trace.peek().withinText = WITHINTEXT_NESTED;
 				break;
 			}
 		}
 		
 		if ( data.charAt(FP_TERMINOLOGY) != '?' ) {
 			trace.peek().term = (data.charAt(FP_TERMINOLOGY) == 'y');
 			trace.peek().termInfo = getFlagData(data, FP_TERMINOLOGY_DATA);
 		}
 		
 		if ( data.charAt(FP_LOCNOTE) != '?' ) {
 			trace.peek().locNote = getFlagData(data, FP_LOCNOTE_DATA);
 			trace.peek().locNoteType = (data.charAt(FP_LOCNOTE)=='a' ? "alert" : "description");
 		}
 
 		// Preserve white spaces
 		if ( data.charAt(FP_PRESERVEWS) != '?' ) {
 			trace.peek().preserveWS = (data.charAt(FP_PRESERVEWS) == 'y');
 		}
 		
 		if ( data.charAt(FP_LANGINFO) != '?' ) {
 			trace.peek().language = getFlagData(data, FP_LANGINFO_DATA);
 		}
 		
 		if ( data.charAt(FP_SUBFILTER) != '?' ) {
 			trace.peek().subFilter = getFlagData(data, FP_SUBFILTER_DATA);
 		}
 		
 		if ( data.charAt(FP_TOOLSREF) != '?' ) {
 			// Update each data category
 			Map<String, String> oldMap = toolsRefToMap(trace.peek().toolsRef);
 			Map<String, String> newMap = toolsRefToMap(getFlagData(data, FP_TOOLSREF_DATA));
 			oldMap.putAll(newMap); // Add or override if needed
 			trace.peek().toolsRef = mapToToolsRef(oldMap);
 		}
 	}
 	
 	private Map<String, String> toolsRefToMap (String data) {
 		TreeMap<String, String> map = new TreeMap<String, String>();
 		if ( Util.isEmpty(data) ) return map; // Empty map
 		// Else: fill the map
 		String[] list = data.split(" ", 0);
 		for ( String tmp : list ) {
 			int n = tmp.indexOf('|');
 			if ( n == -1 ) {
 				logger.warn("Invalid toolsRef value '{}'", tmp);
 				continue;
 			}
 			map.put(tmp.substring(0, n), tmp.substring(n+1));
 		}
 		return map;
 	}
 	
 	private String mapToToolsRef (Map<String, String> map) {
 		StringBuilder sb = new StringBuilder();
 		
 		for ( String dc : map.keySet() ) {
 			if ( sb.length() > 0 ) sb.append(' ');
 			sb.append(dc+"|"+map.get(dc));
 		}
 		return sb.toString();
 	}
 
 	public void startTraversal () {
 		node = null;
 		trace = new Stack<ITSTrace>();
 		//trace.push(new ITSTrace(true)); // For root #document
 		startTraversal = true;
 	}
 	
 	private void clearInternalGlobalRules () {
 		for ( int i=0; i<rules.size(); i++ ) {
 			if ( rules.get(i).isInternal ) {
 				rules.remove(i);
 				i--;
 			}
 		}
 	}
 
 	private void processGlobalRules (long dataCategories) {
 		try {
 			// Compile any internal global rules
 			clearInternalGlobalRules();
 			
 			if ( isHTML5 ) {
 				// For HTML5 global rules are in scripts
 				compileRulesInScripts(doc, docURI, true);
 			}
 			else { // Process normal in-document global rules
 				compileRules(doc, docURI, true);
 			}
 			
 			// Now apply the compiled rules
 		    for ( ITSRule rule : rules ) {
 		    	// Check if we should apply this type of rule
 		    	if ( (dataCategories & rule.ruleType) == 0 ) continue;
 		    	
 		    	// Get the selected nodes for the rule
 		    	String data1;
 				XPathExpression expr = xpath.compile(rule.selector);
 				NodeList NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				
 				// Apply the rule specific action on the selected nodes
 				// Global rules are applies before local so they should 
 				// always override existing flag. override should be set to false
 				// only for default attributes.
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					if ( rule.ruleType == IProcessor.DC_TRANSLATE ) {
 						setFlag(NL.item(i), FP_TRANSLATE, (rule.flag ? 'y' : 'n'), true);
 						// Set the hasTranslatabledattribute flag if it is an attribute node
 						if ( NL.item(i).getNodeType() == Node.ATTRIBUTE_NODE ) {
 							if ( rule.flag ) translatableAttributeRuleTriggered = true; 
 						}
 						if ( rule.idValue != null ) { // For deprecated extension
 							setFlag(NL.item(i), FP_IDVALUE_DATA, resolveExpressionAsString(NL.item(i), rule.idValue), true);							
 						}
 						// For deprecated extension
 						setFlag(NL.item(i), FP_PRESERVEWS, (rule.preserveWS ? 'y' : '?'), true);
 					}
 					
 					else if ( rule.ruleType == IProcessor.DC_DIRECTIONALITY ) {
 						setFlag(NL.item(i), FP_DIRECTIONALITY,
 							String.valueOf(rule.value).charAt(0), true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_WITHINTEXT ) {
 						setFlag(NL.item(i), FP_WITHINTEXT,
 							String.valueOf(rule.value).charAt(0), true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_TERMINOLOGY ) {
 						setFlag(NL.item(i), FP_TERMINOLOGY, (rule.flag ? 'y' : 'n'), true);
 						switch ( rule.infoType ) {
 						case INFOTYPE_POINTER:
 							setFlag(NL.item(i), FP_TERMINOLOGY_DATA, resolvePointer(NL.item(i), rule.info), true);
 							break;
 						case INFOTYPE_REF:
 							setFlag(NL.item(i), FP_TERMINOLOGY_DATA, "REF:"+rule.info, true);
 							break;
 						case INFOTYPE_REFPOINTER:
 							setFlag(NL.item(i), FP_TERMINOLOGY_DATA, "REF:"+resolvePointer(NL.item(i), rule.info), true);
 							break;
 						}
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_LOCNOTE ) {
 						setFlag(NL.item(i), FP_LOCNOTE, (rule.flag ? 'a' : 'd'), true); // Type alert or description
 						switch ( rule.infoType ) {
 						case INFOTYPE_TEXT:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, rule.info, true);
 							break;
 						case INFOTYPE_POINTER:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, resolvePointer(NL.item(i), rule.info), true);
 							break;
 						case INFOTYPE_REF:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, "REF:"+rule.info, true);
 							break;
 						case INFOTYPE_REFPOINTER:
 							setFlag(NL.item(i), FP_LOCNOTE_DATA, "REF:"+resolvePointer(NL.item(i), rule.info), true);
 							break;
 						}
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_LANGINFO ) {
 						setFlag(NL.item(i), FP_LANGINFO, 'y', true);
 						setFlag(NL.item(i), FP_LANGINFO_DATA, resolvePointer(NL.item(i), rule.info), true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_EXTERNALRES ) {
 						setFlag(NL.item(i), FP_EXTERNALRES, 'y', true);
 						setFlag(NL.item(i), FP_EXTERNALRES_DATA, resolvePointer(NL.item(i), rule.info), true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_LOCFILTER ) {
 						setFlag(NL.item(i), FP_LOCFILTER, 'y', true);
 						setFlag(NL.item(i), FP_LOCFILTER_DATA, rule.info, true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_PRESERVESPACE ) {
 						// For new ITS 2.0 rule, but deprecated extension still supported in DC_PRESERVESPACE case
 						setFlag(NL.item(i), FP_PRESERVEWS, (rule.preserveWS ? 'y' : '?'), true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_IDVALUE ) {
 						// For new ITS 2.0 rule, but deprecated extension still supported in DC_TRANSLATE case
 						if ( rule.idValue != null ) {
 							setFlag(NL.item(i), FP_IDVALUE_DATA, resolveExpressionAsString(NL.item(i), rule.idValue), true);							
 						}
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_DOMAIN ) {
 						List<String> list = resolveExpressionAsList(NL.item(i), rule.info);
 						if ( list.isEmpty() ) continue;
 						// Map the values and build the final string
 						StringBuilder tmp = new StringBuilder();
 						List<String> values = null;
 						for ( String item : list ) {
 							values = fromDomainItemToValues(item, rule.map, values);
 						}
 						for ( String value : values ) {
 							if ( tmp.length() > 0 ) tmp.append(", ");
 							tmp.append(value);
 						}
 						setFlag(NL.item(i), FP_DOMAIN, 'y', true);
 						setFlag(NL.item(i), FP_DOMAIN_DATA, tmp.toString(), true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_TARGETPOINTER ) {
 						targetPointerRuleTriggered = true;
 						setFlag(NL.item(i), FP_TARGETPOINTER_DATA, rule.info, true);							
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_LOCQUALITYISSUE ) {
 						GenericAnnotations anns = null;
 						data1 = rule.info;
 						if ( data1 != null ) {
 							if ( rule.infoType == INFOTYPE_REFPOINTER) {
 								data1 = resolvePointer(NL.item(i), data1);
 							}
 							// Fetch the stand-off data
 							anns = fetchLocQualityStandoffData(data1);
 						}
 						else {
 							// Not a stand-off annotation
 							GenericAnnotation ann = rule.annotations.getAnnotations(GenericAnnotationType.LQI).get(0);
 							anns = new GenericAnnotations();
 							GenericAnnotation upd = addIssueItem(anns);
 							// Get and resolve 'type'
 							data1 = ann.getString(GenericAnnotationType.LQI_TYPE);
 							if ( data1 != null ) {
 								if ( data1.startsWith(PTRPREFIX) ) {
 									data1 = resolvePointer(NL.item(i), data1.substring(2));
 								}
 								upd.setString(GenericAnnotationType.LQI_TYPE, data1);
 							}
 							// Get and resolve 'comment'
 							data1 = ann.getString(GenericAnnotationType.LQI_COMMENT);
 							if ( data1 != null ) {
 								if ( data1.startsWith(PTRPREFIX) ) {
 									data1 = resolvePointer(NL.item(i), data1.substring(2));
 								}
 								upd.setString(GenericAnnotationType.LQI_COMMENT, data1);
 							}
 							// Get and resolve 'severity'
 							data1  = ann.getString(GenericAnnotationType.LQI_SEVERITY);
 							if ( data1 != null ) {
 								if ( data1.startsWith(PTRPREFIX) ) {
 									data1 = resolvePointer(NL.item(i), data1.substring(2));
 								}
 								// Convert the string to the float value
 								upd.setFloat(GenericAnnotationType.LQI_SEVERITY, Float.parseFloat(data1));
 							}
 							// Get and resolve 'profile reference'
 							data1 = ann.getString(GenericAnnotationType.LQI_PROFILEREF);
 							if ( data1 != null ) {
 								if ( data1.startsWith(PTRPREFIX) ) {
 									data1 = resolvePointer(NL.item(i), data1.substring(2));
 								}
 								upd.setString(GenericAnnotationType.LQI_PROFILEREF, data1);
 							}
 							// Get and resolve 'enabled'
 							data1 = ann.getString(GenericAnnotationType.LQI_ENABLED);
 							if ( data1 != null ) {
 								if ( data1.startsWith(PTRPREFIX) ) {
 									data1 = resolvePointer(NL.item(i), data1.substring(2));
 								}
 								upd.setBoolean(GenericAnnotationType.LQI_ENABLED, data1.equals("yes"));
 							}
 						}
 						// Decorate the node with the resolved annotation data
 						setFlag(NL.item(i), FP_LQISSUE, 'y', true);
 						setFlag(NL.item(i), FP_LQISSUE_DATA, anns.toString(), true);
 					}
 
 					else if ( rule.ruleType == IProcessor.DC_ALLOWEDCHARS ) {
 						if ( rule.infoType == INFOTYPE_POINTER ) {
 							data1 = resolvePointer(NL.item(i), rule.info);
 						}
 						else { // Direct expression
 							data1 = rule.info;
 						}
 						setFlag(NL.item(i), FP_ALLOWEDCHARS, 'y', true);
 						setFlag(NL.item(i), FP_ALLOWEDCHARS_DATA, data1, true);
 					}
 						
 					else if ( rule.ruleType == IProcessor.DC_STORAGESIZE ) {
 						data1 = rule.map.get("size");
 						if ( data1 == null ) {
 							data1 = rule.map.get("sizePointer");
 							if ( !Util.isEmpty(data1) ) data1 = resolvePointer(NL.item(i), data1);
 						}
 						String data2 = rule.map.get("encoding");
 						if ( data2 == null ) {
 							data2 = rule.map.get("encodingPointer");
 							if ( !Util.isEmpty(data2) ) data2 = resolvePointer(NL.item(i), data2);
 						}
 						String data3 = rule.map.get("linebreak");
 						if ( data3 == null ) {
 							data3 = rule.map.get("linebreakPointer");
 							if ( !Util.isEmpty(data3) ) data3 = resolvePointer(NL.item(i), data3);
 						}
 						setFlag(NL.item(i), FP_STORAGESIZE, 'y', true);
 						setFlag(NL.item(i), FP_STORAGESIZE_DATA, toSingleString(data1, data2, data3), true);
 					}
 
 					else if ( rule.ruleType == IProcessor.DC_SUBFILTER ) {
 						setFlag(NL.item(i), FP_SUBFILTER, 'y', true);
 						setFlag(NL.item(i), FP_SUBFILTER_DATA, rule.info, true);
 					}
 
 				}
 		    }
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Adds an issue annotation to a given set and sets its default values.
 	 * @param anns the set where to add the annotation.
 	 * @return the annotation that has been added.
 	 */
 	private GenericAnnotation addIssueItem (GenericAnnotations anns) {
 		GenericAnnotation ann = anns.add(GenericAnnotationType.LQI);
 		ann.setBoolean(GenericAnnotationType.LQI_ENABLED, true); // default
 		return ann;
 	}
 	
 	/**
 	 * Adds the values found in a domain original string to a common result list.
 	 * following the ITS 2.0 algorithm.
 	 * @param text the content of the original string.
 	 * @param map the map where the domain Mapping values are listed (can be null)
 	 * The left values of the list must be in lowercase.
 	 * @param list the list of previously existing resulting values (can be null)
 	 * @return the list of the resulting values.
 	 */
 	private List<String> fromDomainItemToValues (String text,
 		Map<String, String> map,
 		List<String> list)
 	{
 		if ( list == null ) list = new ArrayList<String>();
 		// Split the item on commas, and remove white spaces
 		String[] parts = text.split(",", 0);
 		for ( int i=0; i<parts.length; i++ ) {
 			parts[i] = parts[i].trim();
 			if ( parts[i].startsWith("'") || parts[i].startsWith("\"") ) {
 				parts[i] = parts[i].substring(1);
 			}
 			if ( parts[i].endsWith("'") || parts[i].endsWith("\"") ) {
 				parts[i] = parts[i].substring(0, parts[i].length()-1);
 			}
 		}
 		for ( String part : parts ) {
 			// If there is a map and the part is listed in it
 			if (( map != null ) && map.containsKey(part.toLowerCase()) ) {
 				part = map.get(part); // Use the mapped value
 			}
 			if ( !list.contains(part) ) {
 				list.add(part);
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * Converts a list of strings arguments to a single string that is delimited with end-of-group characters. 
 	 * @param values the values to store. Null values are ok and mean no value.
 	 * @return a single string with all values.
 	 */
 	private String toSingleString (String ... values) {
 		StringBuilder data = new StringBuilder();
 		for ( String value : values ) {
 			if ( value == null ) data.append("\u001A");
 			if ( value != null ) data.append(value);
 			data.append("\u001D");
 		}
 		return data.toString();
 	}
 	
 	private String[] fromSingleString (String data) {
 		String[] values = data.split("\u001D", -1);
 		for ( int i=0; i<values.length; i++ ) {
 			if ( values[i].equals("\u001A") ) values[i] = null;
 		}
 		return values;
 	}
 	
 	private void processLocalRules (long dataCategories) {
 		XPathExpression expr;
 		NodeList NL;
 		Attr attr;
 		try {
 			if ( (dataCategories & IProcessor.DC_TRANSLATE) > 0 ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@translate");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":translate|//"+ITS_NS_PREFIX+":span/@translate");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "translateRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Validate the value
 					String value = attr.getValue();
 					if (( !"yes".equals(value) ) && ( !"no".equals(value) )) {
 						throw new ITSException("Invalid value for 'translate'.");
 					}
 					// Set the flag
 					setFlag(attr.getOwnerElement(), FP_TRANSLATE, value.charAt(0), attr.getSpecified());
 					// No need to update hasTranslatabledattribute here because all nodes have to be elements in locale rules
 				}
 			}
 			
 			if ( (dataCategories & IProcessor.DC_DIRECTIONALITY) > 0 ) {
 				if ( isHTML5 ) {
 					//TODO: Do we need more than this?
 					// Values for HTML5 for sir are ltr|rtl|auto (not rlo|lro)
 					expr = xpath.compile("//*/@dir");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":dir|//"+ITS_NS_PREFIX+":span/@dir");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "dirRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag on the others
 					int n = DIR_LTR;
 					if ( "rtl".equals(attr.getValue()) ) n = DIR_LTR; 
 					else if ( "ltr".equals(attr.getValue()) ) n = DIR_RTL; 
 					else if ( "rlo".equals(attr.getValue()) ) n = DIR_RLO; 
 					else if ( "lro".equals(attr.getValue()) ) n = DIR_LRO;
 					else throw new ITSException("Invalid value for 'dir'."); 
 					setFlag(attr.getOwnerElement(), FP_DIRECTIONALITY,
 						String.format("%d", n).charAt(0), attr.getSpecified());
 				}
 			}
 			
 			if ( (dataCategories & IProcessor.DC_TERMINOLOGY) > 0 ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-term|//*/@its-term-info-ref");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":term|//"+ITS_NS_PREFIX+":span/@term"
 						+"|//*/@"+ITS_NS_PREFIX+":termInfoRef|//"+ITS_NS_PREFIX+":span/@termInfoRef");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				String localName;
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					localName = attr.getLocalName();
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "termRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// term
 					if ( localName.equals("term") || localName.equals("its-term")) {
 						// Validate the value
 						String value = attr.getValue();
 						if (( !"yes".equals(value) ) && ( !"no".equals(value) )) {
 							throw new ITSException("Invalid value for 'term'.");
 						}
 						// Set the flag
 						setFlag(attr.getOwnerElement(), FP_TERMINOLOGY, value.charAt(0), attr.getSpecified());
 					}
 					else if ( localName.equals("termInfoRef") || localName.equals("its-term-info-ref") ) {
 						setFlag(attr.getOwnerElement(), FP_TERMINOLOGY_DATA,
 							"REF:"+attr.getValue(), attr.getSpecified());
 					}
 				}
 			}
 
 			if ( (dataCategories & IProcessor.DC_LOCNOTE) > 0 ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-loc-note|//*/@its-loc-note-ref");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":locNote|//"+ITS_NS_PREFIX+":span/@locNote"
 						+"|//*/@"+ITS_NS_PREFIX+":locNoteRef|//"+ITS_NS_PREFIX+":span/@locNoteRef");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				String localName;
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					localName = attr.getLocalName();
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "locNoteRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Retreive the type of note
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					boolean alert = retrieveLocNoteType(attr.getOwnerElement(), qualified, false, isHTML5);
 					// Set the flags/data
 					setFlag(attr.getOwnerElement(), FP_LOCNOTE, (alert ? 'a' : 'd'), attr.getSpecified());
 					if ( localName.equals("locNote") || localName.equals("its-loc-note") ) {
 						setFlag(attr.getOwnerElement(), FP_LOCNOTE_DATA, attr.getValue(), attr.getSpecified());
 					}
 					else if ( localName.equals("locNoteRef") || localName.equals("its-loc-note-ref") ) {
 						setFlag(attr.getOwnerElement(), FP_LOCNOTE_DATA,
 							"REF:"+attr.getValue(), attr.getSpecified());
 					}
 				}
 			}
 
 			if ( (dataCategories & IProcessor.DC_LANGINFO) > 0 ) {
 				expr = xpath.compile("//*/@"+XML_NS_PREFIX+":lang");
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Set the flag
 					setFlag(attr.getOwnerElement(), FP_LANGINFO, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_LANGINFO_DATA,
 						attr.getValue(), attr.getSpecified());
 				}
 			}
 
 			// Local withinText attribute (ITS 2.0 only)
 			if (( (dataCategories & IProcessor.DC_WITHINTEXT) > 0 ) && isVersion2() ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-within-text");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":withinText");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "withinTextRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					String value = attr.getValue();
 					char ch;
 					if ( "no".equals(value) ) ch='0'; // WITHINTEXT_NO;
 					else if ( "yes".equals(value) ) ch='1'; // WITHINTEXT_YES;
 					else if ( "nested".equals(value) ) ch='2'; // WITHINTEXT_NESTED;
 					else throw new ITSException("Invalid value for 'withinText'.");
 					setFlag(attr.getOwnerElement(), FP_WITHINTEXT, ch, attr.getSpecified());
 				}
 			}
 			
 			// xml:space always applied
 			expr = xpath.compile("//*/@"+XML_NS_PREFIX+":space");
 			NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			for ( int i=0; i<NL.getLength(); i++ ) {
 				attr = (Attr)NL.item(i);
 				// Validate the value
 				String value = attr.getValue();
 				if (( !"preserve".equals(value) ) && ( !"default".equals(value) )) {
 					throw new ITSException("Invalid value for 'xml:space'.");
 				}
 				// Set the flag
 				setFlag(attr.getOwnerElement(), FP_PRESERVEWS,
 					("preserve".equals(value) ? 'y' : '?'), attr.getSpecified());
 			}
 			
 			// its:toolsRef always applied
 			if ( isHTML5 ) {
 				expr = xpath.compile("//*/@its-tools-ref");
 			}
 			else {
 				expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":toolsRef");
 			}
 			NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			for ( int i=0; i<NL.getLength(); i++ ) {
 				attr = (Attr)NL.item(i);
 				// Validate the value
 				String value = attr.getValue();
 				
 				// Set the flag
 				setFlag(attr.getOwnerElement(), FP_TOOLSREF,
 					(value!=null ? 'y' : '?'), attr.getSpecified());
 				setFlag(attr.getOwnerElement(), FP_TOOLSREF_DATA,
 						value, attr.getSpecified()); 
 			}
 			
 			
 			// locale filter
 			if (( (dataCategories & IProcessor.DC_LOCFILTER) > 0 ) && isVersion2() ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-locale-filter-list");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":localeFilterList|//"+ITS_NS_PREFIX+":span/@localeFilterList");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "localeFilterRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String value = retrieveLocaleFilterList(attr.getOwnerElement(), qualified, isHTML5);
 					setFlag(attr.getOwnerElement(), FP_LOCFILTER, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_LOCFILTER_DATA,
 						value, attr.getSpecified()); 
 				}
 			}
 			
 			// xml:id always applied
 			expr = xpath.compile("//*/@"+XML_NS_PREFIX+":id");
 			NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			for ( int i=0; i<NL.getLength(); i++ ) {
 				attr = (Attr)NL.item(i);
 				String value = attr.getValue();
 				if (( value != null ) && ( value.length() > 0 )) {
 					setFlag(attr.getOwnerElement(), FP_IDVALUE_DATA,
 						value, attr.getSpecified());
 				}
 			}
 			
 			// Localization quality issue
 			if (( (dataCategories & IProcessor.DC_LOCQUALITYISSUE) > 0 ) && isVersion2() ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-loc-quality-issue-type|//*/@its-loc-quality-issue-comment|//*/@its-loc-quality-issues-ref");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":locQualityIssueType|//"+ITS_NS_PREFIX+":span/@locQualityIssueType"
 						+"|//*/@"+ITS_NS_PREFIX+":locQualityIssueComment|//"+ITS_NS_PREFIX+":span/@locQualityIssueComment"
 						+"|//*/@"+ITS_NS_PREFIX+":locQualityIssuesRef|//"+ITS_NS_PREFIX+":span/@locQualityIssuesRef");
 				}
 
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "locQualityIssueRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String[] values = retrieveLocQualityIssueData(attr.getOwnerElement(), qualified, isHTML5);
 					// Convert the values into an annotation
 					GenericAnnotations anns = null;
 					if ( values[0] != null ) { // stand-off reference
 						// Fetch the stand-off data 
 						anns = fetchLocQualityStandoffData(values[0]);
 					}
 					else { // Not an stand-off reference
 						anns = new GenericAnnotations();
 						GenericAnnotation ann = addIssueItem(anns);
 						if ( values[1] != null ) ann.setString(GenericAnnotationType.LQI_TYPE, values[1]);
 						if ( values[2] != null ) ann.setString(GenericAnnotationType.LQI_COMMENT, values[2]);
 						if ( values[3] != null ) ann.setFloat(GenericAnnotationType.LQI_SEVERITY, Float.parseFloat(values[3]));
 						if ( values[4] != null ) ann.setString(GenericAnnotationType.LQI_PROFILEREF, values[4]);
 						if ( values[5] != null ) ann.setBoolean(GenericAnnotationType.LQI_ENABLED, values[5].equals("yes"));
 					}
 					// Set the updated flags
 					setFlag(attr.getOwnerElement(), FP_LQISSUE, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_LQISSUE_DATA, anns.toString(), attr.getSpecified()); 
 				}
 			}
 			
 			// Allowed characters
 			if (( (dataCategories & IProcessor.DC_ALLOWEDCHARS) > 0 ) && isVersion2() ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-allowed-characters");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":allowedCharacters|//"+ITS_NS_PREFIX+":span/@allowedCharacters");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "allowedCharactersRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String value = retrieveAllowedCharsData(attr.getOwnerElement(), qualified, isHTML5);
 					// Set the updated flags
 					setFlag(attr.getOwnerElement(), FP_ALLOWEDCHARS, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_ALLOWEDCHARS_DATA, value, attr.getSpecified()); 
 				}
 			}
 			
 			// Storage size
 			if (( (dataCategories & IProcessor.DC_STORAGESIZE) > 0 ) && isVersion2() ) {
 				if ( isHTML5 ) {
 					expr = xpath.compile("//*/@its-storage-size");
 				}
 				else {
 					expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":storageSize|//"+ITS_NS_PREFIX+":span/@storageSize");
 				}
 				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 				for ( int i=0; i<NL.getLength(); i++ ) {
 					attr = (Attr)NL.item(i);
 					// Skip irrelevant nodes
 					if ( ITS_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 						&& "storageSizeRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 					// Set the flag
 					boolean qualified = true;
 					String ns = attr.getOwnerElement().getNamespaceURI();
 					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITS_NS_URI);
 					String[] values = retrieveStorageSizeData(attr.getOwnerElement(), qualified, isHTML5);
 					// Set the updated flags
 					setFlag(attr.getOwnerElement(), FP_STORAGESIZE, 'y', attr.getSpecified());
 					setFlag(attr.getOwnerElement(), FP_STORAGESIZE_DATA,
 						toSingleString(values[0], values[1], values[2]), attr.getSpecified()); 
 				}
 			}
 
 //			// sub filter
 //			if ( (dataCategories & IProcessor.DC_SUBFILTER) > 0 ) {
 //				if ( isHTML5 ) {
 //					expr = xpath.compile("//*/@data-itsx-sub-filter");
 //				}
 //				else {
 //					// Not on inline its:span
 //					ERROR
 //					expr = xpath.compile("//*/@"+ITSX_NS_PREFIX+":subFilter");
 //				}
 //				NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 //				for ( int i=0; i<NL.getLength(); i++ ) {
 //					attr = (Attr)NL.item(i);
 //					// Skip irrelevant nodes
 //					if ( ITSX_NS_URI.equals(attr.getOwnerElement().getNamespaceURI())
 //						&& "subFilterRule".equals(attr.getOwnerElement().getLocalName()) ) continue;
 //					// Set the flag
 //					boolean qualified = true;
 //					String ns = attr.getOwnerElement().getNamespaceURI();
 //					if ( !Util.isEmpty(ns) ) qualified = !ns.equals(ITSX_NS_URI);
 //					String value = retrieveSubFilter(attr.getOwnerElement(), qualified, isHTML5);
 //					setFlag(attr.getOwnerElement(), FP_SUBFILTER, 'y', attr.getSpecified());
 //					setFlag(attr.getOwnerElement(), FP_SUBFILTER_DATA,
 //						value, attr.getSpecified()); 
 //				}
 //			}
 			
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Retrieve the final list to use for a locale filter data category
 	 * @param elem the element where the attributes are defined.
 	 * @param qualified true if the attributes are expected to be qualified (local markup)
 	 * @return the final list.
 	 */
 	private String retrieveLocaleFilterList (Element elem,
 		boolean qualified,
 		boolean useHTML5)
 	{
 		if ( useHTML5 ) {
 			return elem.getAttribute("its-locale-filter-list").trim();
 		}
 		if ( qualified ) { // Locally
 			return elem.getAttributeNS(ITS_NS_URI, "localeFilterList").trim();
 		}
 		else { // Inside a global rule
 			return elem.getAttribute("localeFilterList").trim();
 		}
 	}
 	
 	private String retrieveSubFilter (Element elem,
 		boolean qualified,
 		boolean useHTML5)
 	{
 		if ( useHTML5 ) {
 			return elem.getAttribute("data-itsx-sub-filter").trim();
 		}
 		if ( qualified ) { // Locally
 			return elem.getAttributeNS(ITSX_NS_URI, "subFilter").trim();
 		}
 		else { // Inside a global rule
 			return elem.getAttribute("subFilter").trim();
 		}
 	}
 	
 	private boolean retrieveLocNoteType (Element elem,
 		boolean qualified,
 		boolean required,
 		boolean useHTML5)
 	{
 		String type;
 		if ( useHTML5 ) {
 			type = elem.getAttribute("its-loc-note-type");
 		}
 		else if ( qualified ) {
 			type = elem.getAttributeNS(ITS_NS_URI, "locNoteType");
 		}
 		else {
 			type = elem.getAttribute("locNoteType");
 		}
 		
 		if ( type.isEmpty() && required ) {
 			throw new ITSException(String.format("%s attribute missing.", (isHTML5 ? "its-loc-note-type" : "locNoteType")));
 		}
 		else if ( type.equals("alert") ) {
 			return true; // alert
 		}
 		else if ( !type.equals("description") && !type.isEmpty() ) {
 			throw new ITSException(String.format("Invalide value '%s' for localozation note type.", type));
 		}
 		return false; // description
 	}
 
 	private String retrieveAllowedCharsData (Element elem,
 		boolean qualified,
 		boolean useHTML5)
 	{
 		if ( useHTML5 ) {
 			if ( elem.hasAttribute("its-allowed-characters") )
 				return elem.getAttribute("its-allowed-characters");
 		}
 		else if ( qualified ) {
 			if ( elem.hasAttributeNS(ITS_NS_URI, "allowedCharacters") )
 				return elem.getAttributeNS(ITS_NS_URI, "allowedCharacters");
 		}
 		else {
 			if ( elem.hasAttribute("allowedCharacters") )
 				return elem.getAttribute("allowedCharacters");
 		}
 		return null;
 	}
 	
 	
 	private String[] retrieveStorageSizeData (Element elem,
 		boolean qualified,
 		boolean useHTML5)
 	{
 		String[] data = new String[3];
 		
 		if ( useHTML5 ) {
 			if ( elem.hasAttribute("its-storage-size") )
 				data[0] = elem.getAttribute("its-storage-size");
 			if ( elem.hasAttribute("its-storage-encoding") )
 				data[1] = elem.getAttribute("its-storage-encoding");
 			if ( elem.hasAttribute("its-line-break-type") )
 				data[2] = elem.getAttribute("its-line-break-type");
 		}
 		else if ( qualified ) {
 			if ( elem.hasAttributeNS(ITS_NS_URI, "storageSize") )
 				data[0] = elem.getAttributeNS(ITS_NS_URI, "storageSize");
 			if ( elem.hasAttributeNS(ITS_NS_URI, "storageEncoding") )
 				data[1] = elem.getAttributeNS(ITS_NS_URI, "storageEncoding");
 			if ( elem.hasAttributeNS(ITS_NS_URI, "lineBreakType") )
 				data[2] = elem.getAttributeNS(ITS_NS_URI, "lineBreakType");
 		}
 		else {
 			if ( elem.hasAttribute("storageSize") )
 				data[0] = elem.getAttribute("storageSize");
 			if ( elem.hasAttribute("storageEncoding") )
 				data[1] = elem.getAttribute("storageEncoding");
 			if ( elem.hasAttribute("lineBreakType") )
 				data[2] = elem.getAttribute("lineBreakType");
 		}
 		
 		return data;
 	}
 
 	private XPath createXPath () {
 		XPath xpath = xpFact.newXPath();
 		NSContextManager nsc = new NSContextManager();
 		nsc.addNamespace(ITS_NS_PREFIX, ITS_NS_URI);
 		nsc.addNamespace(HTML_NS_PREFIX, HTML_NS_URI);
 		xpath.setNamespaceContext(nsc);
 		return xpath;
 	}
 	
 	/**
 	 * Retrieves the non-pointer information of the Localization Quality issue data category.
 	 * @param elem the element where to get the data.
 	 * @param qualified true if the attributes are expected to be qualified.
 	 * @return an array of the value: issues reference, type, comment, severity, profile reference, enabled.
 	 */
 	private String[] retrieveLocQualityIssueData (Element elem,
 		boolean qualified,
 		boolean useHTML5)
 	{
 		String[] data = new String[6];
 		
 		if ( useHTML5 ) {
 			if ( elem.hasAttribute("its-loc-quality-issues-ref") )
 				data[0] = elem.getAttribute("its-loc-quality-issues-ref");
 			if ( elem.hasAttribute("its-loc-quality-issue-type") )
 				data[1] = elem.getAttribute("its-loc-quality-issue-type");
 			if ( elem.hasAttribute("its-loc-quality-issue-comment") )
 				data[2] = elem.getAttribute("its-loc-quality-issue-comment");
 			if ( elem.hasAttribute("its-loc-quality-issue-severity") )
 				data[3] = elem.getAttribute("its-loc-quality-issue-severity");
 			if ( elem.hasAttribute("its-loc-quality-issue-profile-ref") )
 				data[4] = elem.getAttribute("its-loc-quality-issue-profile-ref");
 			if ( elem.hasAttribute("its-loc-quality-issue-enabled") )
 				data[5] = elem.getAttribute("its-loc-quality-issue-enabled");
 			else
 				data[5] = "yes"; // Default
 		}
 		else if ( qualified ) {
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssuesRef") )
 				data[0] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssuesRef");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueType") )
 				data[1] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueType");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueComment") )
 				data[2] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueComment");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueSeverity") )
 				data[3] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueSeverity");
 			
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueProfileRef") )
 				data[4] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueProfileRef");
 
 			if ( elem.hasAttributeNS(ITS_NS_URI, "locQualityIssueEnabled") )
 				data[5] = elem.getAttributeNS(ITS_NS_URI, "locQualityIssueEnabled");
 			else
 				data[5] = "yes"; // Default
 		}
 		else {
 			if ( elem.hasAttribute("locQualityIssuesRef") )
 				data[0] = elem.getAttribute("locQualityIssuesRef");
 			
 			if ( elem.hasAttribute("locQualityIssueType") )
 				data[1] = elem.getAttribute("locQualityIssueType");
 			
 			if ( elem.hasAttribute("locQualityIssueComment") )
 				data[2] = elem.getAttribute("locQualityIssueComment");
 			
 			if ( elem.hasAttribute("locQualityIssueSeverity") )
 				data[3] = elem.getAttribute("locQualityIssueSeverity");
 			
 			if ( elem.hasAttribute("locQualityIssueProfileRef") )
 				data[4] = elem.getAttribute("locQualityIssueProfileRef");
 
 			if ( elem.hasAttribute("locQualityIssueEnabled") )
 				data[5] = elem.getAttribute("locQualityIssueEnabled");
 			else
 				data[5] = "yes"; // Default
 		}
 
 		// Do not check for complete set of required characters
 		// This because we could have a global pointer that defines a non-native way to get the data
 
 		return data;
 	}
 	
 	private GenericAnnotations fetchLocQualityStandoffData (String ref) {
 		if ( Util.isEmpty(ref) ) {
 			throw new InvalidParameterException("The reference URI cannot be null or empty.");
 		}
 		// Identify the type of reference (internal/external)
 		// and get the element
 		int n = ref.lastIndexOf('#');
 		String id = null;
 		String firstPart = null;
 		if ( n > -1 ) {
 			id = ref.substring(n+1);
 			firstPart = ref.substring(0, n);
 		}
 		else {
 			// No ID in the URI
 			throw new RuntimeException(String.format("URI to standoff markup does not have an id: '%s'.", ref));
 		}
 
 		boolean useHTML5 = isHTML5;
 		Document containerDoc = null;
 		XPath containerXPath = null;
 		
 		if ( !Util.isEmpty(firstPart) ) {
 			// Load the document and the rules
 			try {
 				String baseFolder = "";
 				if ( docURI != null) baseFolder = getPartBeforeFile(docURI);
 				if ( baseFolder.length() > 0 ) {
 					if ( baseFolder.endsWith("/") )
 						baseFolder = baseFolder.substring(0, baseFolder.length()-1);
 					if ( !ref.startsWith("/") ) ref = baseFolder + "/" + ref;
 					else ref = baseFolder + ref;
 				}
 
 				// Remove the ID if any
 				int p = ref.lastIndexOf('#');
 				if ( p > -1 ) {
 					ref = ref.substring(0, p);
 				}
 				// Detect format based on extension: .html and .html as HTML, everything else as XML.
 				useHTML5 = ( ref.endsWith(".html") || ref.endsWith(".htm") );
 				if ( useHTML5 ) {
 					containerDoc = parseHTMLDocument(ref);
 				}
 				else {
 					containerDoc = parseXMLDocument(ref);
 				}
 				containerXPath = createXPath();
 				// Update useHTML5 in case the pointer file is not the same format as the annotated one
 				Element elem = containerDoc.getDocumentElement();
 			}
 			catch ( Throwable e) {
 				throw new RuntimeException(String.format("Error with URI '%s'.\n"+e.getMessage(), ref));
 			}
 		}
 		else {
 			// Else, the standoff markup is in the same document as the annotated content
 			containerDoc = doc;
 			containerXPath = xpath;
 		}
 		
 		// Create the new annotation set
 		GenericAnnotations anns = new GenericAnnotations();
 		Document issuesDoc = null;
 		XPath issuesXPath = null;
 
 		if ( useHTML5 ) {
 			// If the standoff markup is inside an HTML file:
 			// Get the script that holds the its:locQualityIssues element
 			try {
 				String tmp = String.format("//%s:script[@id='%s']", HTML_NS_PREFIX, id);
 				//String tmp = String.format("//script[@id='%s']", id);
 				XPathExpression expr = containerXPath.compile(tmp);
 				Element scriptElem = (Element)expr.evaluate(containerDoc, XPathConstants.NODE);
 				if ( scriptElem == null ) {
 					logger.warn("Cannot find standoff script element for '{}'", id);
 					GenericAnnotation ann = addIssueItem(anns);
 					ann.setString(GenericAnnotationType.LQI_ISSUESREF, ref); // For information only
 					return anns;
 				}
 				// Else: parse the locQualityIssues element inside the script
 				String content = scriptElem.getTextContent();
 				// Strip white spaces
 				content = content.trim();
 				// Create the context and XPath engine 
 				issuesXPath = createXPath();
 				// Parse the content
 				try {
 					InputSource is = new InputSource(new ByteArrayInputStream(content.getBytes()));
 					issuesDoc = parseXMLDocument(is);
 				}
 				catch ( Throwable e ) {
 					throw new RuntimeException("Error parsing a script element.", e);
 				}
 			}
 			catch ( XPathExpressionException e ) {
 				throw new RuntimeException("XPath error.", e);
 			}
 		}
 		else {
 			issuesDoc = containerDoc;
 			issuesXPath = containerXPath;
 		}
 		
 		// Now get the element holding the list of issues
 		Element elem1;
 		try {
 			String tmp = String.format("//%s:locQualityIssues[@xml:id='%s']", ITS_NS_PREFIX, id);
 			XPathExpression expr = issuesXPath.compile(tmp);
 			elem1 = (Element)expr.evaluate(issuesDoc, XPathConstants.NODE);
 		}
 		catch ( XPathExpressionException e ) {
 			throw new RuntimeException("XPath error.", e);
 		}
 		if ( elem1 == null ) {
 			// Entry not found
 			logger.warn("Cannot find standoff markup for '{}'", ref);
 			GenericAnnotation ann = addIssueItem(anns);
 			ann.setString(GenericAnnotationType.LQI_ISSUESREF, ref); // For information only
 			return anns;
 		}
 		
 		// Then get the list of items in the element
 		NodeList items = elem1.getElementsByTagNameNS(ITS_NS_URI, "locQualityIssue");
 		for ( int i=0; i<items.getLength(); i++ ) {
 			// For each entry 
 			Element elem2 = (Element)items.item(i);
 			// Add the annotation to the set
 			GenericAnnotation ann = addIssueItem(anns);
 			ann.setString(GenericAnnotationType.LQI_ISSUESREF, ref); // For information only
 			// Gather the local information (never in HTML since if it's HTML it's inside a script)
 			String[] values = retrieveLocQualityIssueData(elem2, false, false);
 			if ( values[0] != null ) {
 				logger.warn("Cannot have a standoff reference in a standoff element (reference='{}').", ref);
 			}
 			if ( values[1] != null ) ann.setString(GenericAnnotationType.LQI_TYPE, values[1]);
 			if ( values[2] != null ) ann.setString(GenericAnnotationType.LQI_COMMENT, values[2]);
 			if ( values[3] != null ) ann.setFloat(GenericAnnotationType.LQI_SEVERITY, Float.parseFloat(values[3]));
 			if ( values[4] != null ) ann.setString(GenericAnnotationType.LQI_PROFILEREF, values[4]);
 			if ( values[5] != null ) ann.setBoolean(GenericAnnotationType.LQI_ENABLED, values[5].equals("yes"));
 		}
 
 		return anns;
 	}
 	
 	private boolean isVersion2 () throws XPathExpressionException {
 		// If the version is not detected yet: detect it.
 		if ( version.equals("0") ) {
 			XPathExpression expr = xpath.compile("//*/@"+ITS_NS_PREFIX+":version|//"+ITS_NS_PREFIX+":span/@version");
 			NodeList NL = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
 			if (( NL == null ) || ( NL.getLength() == 0 )) {
 				// No version detected: we assume it's a 2.0 behavior
 				version = "2.0";
 			}
 			else {
 				if ( NL.getLength() > 0 ) {
 					version = ((Attr)NL.item(0)).getValue();
 					if ( !version.equals(ITS_VERSION1) && !version.equals(ITS_VERSION2) ) {
 						throw new ITSException(String.format("Invalid or missing ITS version (\"%s\")", version));
 					}
 				}
 				if ( NL.getLength() > 1 ) {
 					throw new ITSException("More than one ITS version is defined in this document.");
 				}
 			}
 		}
 		return version.equals(ITS_VERSION2);		
 	}
 	
 	/**
 	 * Gets the text content of the first TEXT child of an element node.
 	 * This is to use instead of node.getTextContent() which does not work with some
 	 * Macintosh Java VMs. Note this work-around get <b>only the first TEXT node</b>.
 	 * @param node the container element.
 	 * @return the text of the first TEXT child node.
 	 */
 	public static String getTextContent (Node node) {
 		Node tmp = node.getFirstChild();
 		while ( true ) {
 			if ( tmp == null ) return "";
 			if ( tmp.getNodeType() == Node.TEXT_NODE ) {
 				return tmp.getNodeValue();
 			}
 			tmp = tmp.getNextSibling();
 		}
 	}
 
 	private String resolvePointer (Node node,
 		String pointer)
 	{
 		try {
			//--temporary fix to get resolve the variable in some cases--
			//--but the problem seems to be more serious and requires attention--
			if (pointer.startsWith("$")){
				XPathExpression expr = xpath.compile(pointer);
				pointer = expr.evaluate(node);
			}
 			XPathExpression expr = xpath.compile(pointer);
 			NodeList list = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
 			if (( list == null ) || ( list.getLength() == 0 )) {
 				return "";
 			}
 			switch ( list.item(0).getNodeType() ) {
 			case Node.ELEMENT_NODE:
 				return getTextContent(list.item(0));
 			case Node.ATTRIBUTE_NODE:
 				return list.item(0).getNodeValue();
 			}
 		}
 		catch (XPathExpressionException e) {
 			return "Bad XPath expression in pointer \""+pointer+"\".";
 		}
 		return "pointer("+pointer+")";
 	}
 	
 	private String resolveExpressionAsString (Node node,
 		String expression)
 	{
 		try {
 			XPathExpression expr = xpath.compile(expression);
 			return (String)expr.evaluate(node, XPathConstants.STRING);
 		}
 		catch (XPathExpressionException e) {
 			return "Bab XPath expression \""+expression+"\".";
 		}
 	}
 		
 	private List<String> resolveExpressionAsList (Node node,
 		String expression)
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		try {
 			XPathExpression expr = xpath.compile(expression);
 			NodeList nl = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
 			for ( int i=0; i<nl.getLength(); i++ ) {
 				Node tmpNode = nl.item(i);
 				if ( tmpNode.getNodeType() == Node.ELEMENT_NODE ) {
 					list.add(tmpNode.getTextContent());
 				}
 				else { // Attribute
 					list.add(tmpNode.getNodeValue());
 				}
 			}
 		}
 		catch (XPathExpressionException e) {
 			list.add("Bab XPath expression \""+expression+"\".");
 		}
 		return list;
 	}
 			
 	/**
 	 * Sets the flag for a given node.
 	 * @param node The node to flag.
 	 * @param position The position for the data category.
 	 * @param value The value to set.
 	 * @param override True if the value should override an existing value.
 	 * False should be used only for default attribute values.
 	 */
 	private void setFlag (Node node,
 		int position,
 		char value,
 		boolean override)
 	{
 		StringBuilder data = new StringBuilder();
 		if ( node.getUserData(FLAGNAME) == null )
 			data.append(FLAGDEFAULTDATA);
 		else
 			data.append((String)node.getUserData(FLAGNAME));
 		// Set the new value (if not there yet or override requested)
 		if ( override || ( data.charAt(position) != '?' )) 
 			data.setCharAt(position, value);
 		node.setUserData(FLAGNAME, data.toString(), null);
 	}
 
 	/**
 	 * Sets the data for a flag for a given node.
 	 * @param node The node where to set the data.
 	 * @param position The position for the data category.
 	 * @param value The value to set.
 	 * @param override True if the value should override an existing value.
 	 * False should be used only for default attribute values.
 	 */
 	private void setFlag (Node node,
 		int position,
 		String value,
 		boolean override)
 	{
 		StringBuilder data = new StringBuilder();
 		if ( node.getUserData(FLAGNAME) == null )
 			data.append(FLAGDEFAULTDATA);
 		else
 			data.append((String)node.getUserData(FLAGNAME));
 		// Get the data
 		int n1 = 0;
 		int n2 = data.indexOf(FLAGSEP, 0);
 		for ( int i=0; i<=position; i++ ) {
 			n1 = n2;
 			n2 = data.indexOf(FLAGSEP, n1+1);
 		}
 		// Set the new value (if not there yet or override requested)
 		if ( override || ( n2>n1+1 )) {
 			data.replace(n1+1, n2, value);
 		}
 		node.setUserData(FLAGNAME, data.toString(), null);
 	}
 	
 	private String getFlagData (String data,
 		int position)
 	{
 		int n1 = 0;
 		int n2 = data.indexOf(FLAGSEP, 0);
 		for ( int i=0; i<=position; i++ ) {
 			n1 = n2;
 			n2 = data.indexOf(FLAGSEP, n1+1);
 		}
 		if ( n2>n1+1 ) return data.substring(n1+1, n2);
 		else return "";
 	}
 
 	public boolean getTranslate (Attr attribute) {
 		if ( attribute == null ) return trace.peek().translate;
 		// Else: check the attribute
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return false;
 		// '?' and 'n' will return (correctly) false
 		return (tmp.charAt(FP_TRANSLATE) == 'y');
 	}
 
 	@Override
 	public String getTargetPointer (Attr attribute) {
 		if ( attribute == null ) return trace.peek().targetPointer;
 		// Else: check the attribute
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		return getFlagData(tmp, FP_TARGETPOINTER_DATA);
 	}
 	
 	@Override
 	public String getIdValue (Attr attribute) {
 		if ( attribute == null ) return trace.peek().idValue;
 		// Else: check the attribute
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		return getFlagData(tmp, FP_IDVALUE_DATA);
 	}
 	
 	public int getDirectionality (Attr attribute) {
 		if ( attribute == null ) return trace.peek().dir;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return DIR_LTR;
 		return Integer.valueOf(tmp.charAt(FP_DIRECTIONALITY));
 	}
 	
 	public int getWithinText () {
 		return trace.peek().withinText;
 	}
 	
 	public boolean getTerm (Attr attribute) {
 		if ( attribute == null ) return trace.peek().term;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return false;
 		// '?' and 'n' will return (correctly) false
 		return (tmp.charAt(FP_TERMINOLOGY) == 'y');
 	}
 
 	public String getTermInfo (Attr attribute) {
 		if ( attribute == null ) return trace.peek().termInfo;;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_TERMINOLOGY) != 'y' ) return null;
 		return getFlagData(tmp, FP_TERMINOLOGY_DATA);
 	}
 
 	public String getLocNote (Attr attribute) {
 		if ( attribute == null ) return trace.peek().locNote;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LOCNOTE) == '?' ) return null;
 		return getFlagData(tmp, FP_LOCNOTE_DATA);
 	}
 
 	public String getLocNoteType (Attr attribute) {
 		if ( attribute == null ) return trace.peek().locNoteType;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LOCNOTE) == '?' ) return null;
 		if ( tmp.charAt(FP_LOCNOTE) == 'a' ) return "alert";
 		return "description";
 	}
 	
 	public String getDomains (Attr attribute) {
 		if ( attribute == null ) return trace.peek().domains;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_DOMAIN) != 'y' ) return null;
 		return getFlagData(tmp, FP_DOMAIN_DATA);
 	}
 
 	@Override
 	public boolean preserveWS () {
 		return trace.peek().preserveWS;
 	}
 
 	public String getLanguage () {
 		return trace.peek().language;
 	}
 
 	@Override
 	public String getExternalResourceRef (Attr attribute) {
 		if ( attribute == null ) return trace.peek().externalRes;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_EXTERNALRES) != 'y' ) return null;
 		return getFlagData(tmp, FP_EXTERNALRES_DATA);
 	}
 
 	@Override
 	public String getLocaleFilter () {
 		return trace.peek().localeFilter;
 	}
 
 	@Override
 	public String getLocQualityIssuesRef (Attr attribute) {
 		return getLQIValue(GenericAnnotationType.LQI_ISSUESREF, attribute, 0);
 	}
 	
 	@Override
 	public int getLocQualityIssueCount (Attr attribute) {
 		if ( attribute == null ) {
 			GenericAnnotations lqi = trace.peek().lqIssues;
 			if ( lqi == null ) return 0;
 			return lqi.size();
 		}
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return 0;
 		if ( tmp.charAt(FP_LQISSUE) != 'y' ) return 0;
 		GenericAnnotations anns = new GenericAnnotations(getFlagData(tmp, FP_LQISSUE_DATA));
 		return anns.getAnnotations(GenericAnnotationType.LQI).size();
 	}
 	
 	/**
 	 * Gets the localization quality issue annotation set for the current element
 	 * or one of its attributes. 
 	 * @param attribute the attribute to look up, or null for the element.
 	 * @return the annotation set for the queried node (can be null).
 	 */
 	public GenericAnnotations getLocQualityIssues (Attr attribute) {
 		if ( attribute == null ) {
 			return trace.peek().lqIssues;
 		}
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LQISSUE) != 'y' ) return null;
 		return new GenericAnnotations(getFlagData(tmp, FP_LQISSUE_DATA));
 	}
 
 	@Override
 	public String getLocQualityIssueType (Attr attribute,
 		int index)
 	{
 		return getLQIValue(GenericAnnotationType.LQI_TYPE, attribute, index);
 	}
 
 	@Override
 	public String getLocQualityIssueComment (Attr attribute,
 		int index)
 	{
 		return getLQIValue(GenericAnnotationType.LQI_COMMENT, attribute, index);
 	}
 
 	@Override
 	public Float getLocQualityIssueSeverity (Attr attribute,
 		int index)
 	{
 		if ( attribute == null ) {
 			if ( trace.peek().lqIssues == null ) return null;
 			return trace.peek().lqIssues.getAnnotations(GenericAnnotationType.LQI).get(index).getFloat(GenericAnnotationType.LQI_SEVERITY);
 		}
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LQISSUE) != 'y' ) return null;
 		GenericAnnotations anns = new GenericAnnotations(getFlagData(tmp, FP_LQISSUE_DATA));
 		return anns.getAnnotations(GenericAnnotationType.LQI).get(index).getFloat(GenericAnnotationType.LQI_SEVERITY);
 	}
 
 	@Override
 	public String getLocQualityIssueProfileRef (Attr attribute,
 		int index)
 	{
 		return getLQIValue(GenericAnnotationType.LQI_PROFILEREF, attribute, index);
 	}
 
 	@Override
 	public Boolean getLocQualityIssueEnabled (Attr attribute,
 		int index)
 	{
 		if ( attribute == null ) {
 			if ( trace.peek().lqIssues == null ) return null;
 			return trace.peek().lqIssues.getAnnotations(GenericAnnotationType.LQI).get(index).getBoolean(GenericAnnotationType.LQI_ENABLED);
 		}
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LQISSUE) != 'y' ) return null;
 		GenericAnnotations anns = new GenericAnnotations(getFlagData(tmp, FP_LQISSUE_DATA));
 		return anns.getAnnotations(GenericAnnotationType.LQI).get(index).getBoolean(GenericAnnotationType.LQI_ENABLED);
 	}
 	
 	private String getLQIValue (String fieldName,
 		Attr attribute,
 		int index)
 	{
 		if ( attribute == null ) {
 			if ( trace.peek().lqIssues == null ) return null;
 			return trace.peek().lqIssues.getAnnotations(GenericAnnotationType.LQI).get(index).getString(fieldName);
 		}
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_LQISSUE) != 'y' ) return null;
 		GenericAnnotations anns = new GenericAnnotations(getFlagData(tmp, FP_LQISSUE_DATA));
 		return anns.getAnnotations(GenericAnnotationType.LQI).get(index).getString(fieldName);
 	}
 
 	@Override
 	public String getStorageSize (Attr attribute) {
 		if ( attribute == null ) return trace.peek().storageSize;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_STORAGESIZE) != 'y' ) return null;
 		String[] values = fromSingleString(getFlagData(tmp, FP_STORAGESIZE_DATA));
 		return values[0];
 	}
 
 	@Override
 	public String getStorageEncoding (Attr attribute) {
 		String tmp;
 		if ( attribute == null ) {
 			tmp = trace.peek().storageEncoding;
 			if ( tmp == null ) return "UTF-8";
 			else return tmp;
 		}
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_STORAGESIZE) != 'y' ) return null;
 		String[] values = fromSingleString(getFlagData(tmp, FP_STORAGESIZE_DATA));
 		if ( values[1] == null ) return "UTF-8";
 		else return values[1];
 	}
 
 	@Override
 	public String getLineBreakType (Attr attribute) {
 		String tmp;
 		if ( attribute == null ) {
 			tmp = trace.peek().lineBreakType;
 			if ( tmp == null ) return "lf";
 			else return tmp;
 		}
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_STORAGESIZE) != 'y' ) return null;
 		String[] values = fromSingleString(getFlagData(tmp, FP_STORAGESIZE_DATA));
 		if ( values[2] == null ) return "lf";
 		else return values[2];
 	}
 
 	@Override
 	public String getAllowedCharacters (Attr attribute) {
 		if ( attribute == null ) return trace.peek().allowedChars;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_ALLOWEDCHARS) != '?' ) return getFlagData(tmp, FP_ALLOWEDCHARS_DATA);
 		return null;
 	}
 	
 	public String getSubFilter (Attr attribute) {
 		if ( attribute == null ) return trace.peek().subFilter;
 		String tmp;
 		if ( (tmp = (String)attribute.getUserData(FLAGNAME)) == null ) return null;
 		if ( tmp.charAt(FP_SUBFILTER) != 'y' ) return null;
 		return getFlagData(tmp, FP_SUBFILTER_DATA);
 	}
 
 	public String getToolsRef () {
 		return trace.peek().toolsRef;
 	}
 	
 	/**
 	 * Prepares the document for using target pointers.
 	 * <p>Because of the way the skeleton is constructed and because target pointer can result in the target
 	 * location being anywhere in the document, we need to perform a first pass to create the targetTable
 	 * table. That table lists all the source nodes that have a target pointer and the corresponding target
 	 * node with its status.
 	 */
 	private void prepareTargetPointers () {
 		hasTargetPointer = false;
 		try {
 			// If there is no target pointers, just reset the table
 			if ( !getTargetPointerRuleTriggered() ) {
 				return;
 			}
 			// Else: gather the target locations
 			startTraversal();
 	
 			// Go through the document
 			Node srcNode;
 			while ( (srcNode = nextNode()) != null ) {
 				if ( srcNode.getNodeType() == Node.ELEMENT_NODE ) {
 					// Use !backTracking() to get to the elements only once
 					// and to include the empty elements (for attributes).
 					if ( !backTracking() ) {
 						if ( getTranslate(null) ) {
 							String pointer = getTargetPointer(null);
 							if ( pointer != null ) {
 								resolveTargetPointer(getXPath(), srcNode, pointer);
 							}
 						}
 						//TODO: attributes
 					}
 				}
 			}
 		}
 		finally {
 			// Reset the traversal
 			startTraversal();
 		}
 	}
 
 	/**
 	 * Resolves the target pointer for a given source node and creates its
 	 * entry in targetTable, and set flag on the node
 	 * @param xpath the XPath object to use for the resolution.
 	 * @param srcNode the source node.
 	 * @param pointer the XPath expression pointing to the target node
 	 */
 	private void resolveTargetPointer (XPath xpath,
 		Node srcNode,
 		String pointer)
 	{
 		try {
 			XPathExpression expr = xpath.compile(pointer);
 			Node trgNode = (Node)expr.evaluate(srcNode, XPathConstants.NODE);
 			if ( trgNode == null ) {
 				// No entry available
 				//TODO: try to create the needed node
 				return;
 			}
 			// Check the type
 			if ( srcNode.getNodeType() != trgNode.getNodeType() ) {
 				logger.warn("Potential issue with target pointer '{}'.\nThe source and target node are of different types. "
 					+ "Depending on the content of the source, this may or may not be an issue.", pointer);
 			}
 			// Create the entry
 			TargetPointerEntry tpe = new TargetPointerEntry(srcNode, trgNode);
 			// Set the flags on each nod
 			srcNode.setUserData(SRC_TRGPTRFLAGNAME, tpe, null);
 			trgNode.setUserData(TRG_TRGPTRFLAGNAME, tpe, null);
 			hasTargetPointer = true;
 		}
 		catch ( XPathExpressionException e ) {
 			throw new OkapiIOException(String.format("Bab XPath expression in target pointer '%s'.", pointer));
 		}
 	}
 
 	/**
 	 * Indicates if the decorated document has at least one node with a target pointer.
 	 * @return true if the decorated document has at least one node with a target pointer,
 	 * false otherwise.
 	 */
 	public boolean getHasTargetPointer () {
 		return hasTargetPointer;
 	}
 
 	/**
 	 * Gets the target pointer entry for a given node.
 	 * @param node the node to examine.
 	 * @return the target pointer entry for that node, or null if there is none.
 	 */
 	public TargetPointerEntry getTargetPointerEntry (Node node) {
 		TargetPointerEntry tpe = (TargetPointerEntry)node.getUserData(TRG_TRGPTRFLAGNAME);
 		if ( tpe != null ) {
 			// This node is a target location
 			//TODO
 		}
 		else {
 			tpe = (TargetPointerEntry)node.getUserData(SRC_TRGPTRFLAGNAME);
 			if ( tpe != null ) {
 				// This node is a source with a target location
 				// TODO
 			}
 		}
 		return tpe;
 	}
 
 }
