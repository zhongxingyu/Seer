 package org.xbrlapi.xlink;
 
 /**
  * Implementation of the XLinkProcessor.
  * Includes XLink 1.1 support.
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  * @author Henry S. Thompson (ht@w3.org)
  */
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 import org.apache.xerces.util.XML11Char;
 import org.xbrlapi.utilities.Constants;
 import org.xml.sax.Attributes;
 
 public class XLinkProcessorImpl implements XLinkProcessor {
 
     protected static Logger logger = Logger.getLogger(XLinkProcessorImpl.class);
     
 	/**
 	 * The XLink handler to use for responding to XLink events
 	 * in the SAX parsing process.
 	 */
 	XLinkHandler xlinkHandler;
 
 	/**
 	 * The Custom Link Recogniser, to be used for simple link
 	 * syntaxes that are not specified by the XLink specification
 	 * but need to be processed by the XLink processor.
 	 * Set to null by default for cases where no non-XLink 
 	 * syntax needs to be treated as a link.
 	 */
 	CustomLinkRecogniser customLinkRecogniser = null;
 
 	/**
 	 * Track the type of XLink element or other element that
 	 * is the parent of the element being currently handled.
 	 * This is important for the implementation of the
 	 * elementStart functionality. 
 	 */
 	private Stack<Integer> ancestorTypes = new Stack<Integer>();
 	
 	/**
 	 * Boolean to track whether we are inside an extended
 	 * link.  Note that it is not possible to have nested
 	 * extended links so this does not need to be a stack.
 	 */
 	private boolean insideAnExtendedLink = false;
 
 	/**
 	 * Hash map to store information about 
 	 * the kinds of elements on which XLink attributes
 	 * can be expected to occur. (Documentation by Geoff Shuetrim)
 	 * Property added by Henry S Thompson.
 	 */
 	static private HashMap<String,Integer> XLINKATTRS = null;
 	
 	/**
 	 * constructor
 	 * @param xlinkHandler The XLink Handler
 	 */
 	public XLinkProcessorImpl(XLinkHandler xlinkHandler) {
 		super();
 		this.xlinkHandler = xlinkHandler;
 		ancestorTypes.push(NOT_XLINK);
 		if ( XLINKATTRS == null ) {
 			  XLINKATTRS=new HashMap<String,Integer>();
 			  XLINKATTRS.put("type",new Integer(-1));
 			  XLINKATTRS.put("href",new Integer(SIMPLE_LINK.intValue()+LOCATOR.intValue()));
 			  XLINKATTRS.put("role",new Integer(SIMPLE_LINK.intValue()+EXTENDED_LINK.intValue()+LOCATOR.intValue()+RESOURCE.intValue()));
 			  XLINKATTRS.put("arcrole",new Integer(SIMPLE_LINK.intValue()+ARC.intValue()));
 			  XLINKATTRS.put("title",new Integer(SIMPLE_LINK.intValue()+EXTENDED_LINK.intValue()+ARC.intValue()+LOCATOR.intValue()+RESOURCE.intValue()));
 			  XLINKATTRS.put("show",new Integer(SIMPLE_LINK.intValue()+ARC.intValue()));
 			  XLINKATTRS.put("actuate",new Integer(SIMPLE_LINK.intValue()+ARC.intValue()));
 			  XLINKATTRS.put("label",new Integer(LOCATOR.intValue()+RESOURCE.intValue()));
 			  XLINKATTRS.put("from",ARC);
 			  XLINKATTRS.put("to",ARC);
 		}
 
 	}
 
 	/**
 	 * constructor
 	 * @param xlinkHandler The XLink Handler.
 	 * @param recogniser The Custom link recogniser.
 	 */
 	public XLinkProcessorImpl(XLinkHandler xlinkHandler, CustomLinkRecogniser recogniser) {
 		this(xlinkHandler);
 		setCustomLinkRecogniser(recogniser);		
 	}
 
 	
     /**
      * Set the custom link recogniser
      * @param customLinkRecogniser The class that indicates if a custom link
      * has been recognised.
      */
     public void setCustomLinkRecogniser(CustomLinkRecogniser customLinkRecogniser) {
     	this.customLinkRecogniser = customLinkRecogniser;
     }
 
 	/**
 	 * @see org.xbrlapi.xlink.XLinkProcessor#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
 	 */
 	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws XLinkException {
 				
 		// Handle the XML Base attribute on the element (even when it does not exist or has "" value)
 		xlinkHandler.xmlBaseStart(attrs.getValue(Constants.XMLNamespace,"base"));
 
         // Complain about any unexpected attributes in the XLink namespace.
         // Added by Henry S Thompson
         boolean hasSomeXLinkAttributes = false;
         for (int i=0; i<attrs.getLength(); i++) {
             if (attrs.getURI(i).equals(Constants.XLinkNamespace)) {
                 String aName = attrs.getLocalName(i);
                 if ( XLINKATTRS.get(aName) == null ) {
                     xlinkHandler.error(namespaceURI, lName, qName, attrs, aName + " is not defined in the XLink namespace.");
                 } else {
                     hasSomeXLinkAttributes = true;
                 }
             }
         }
         
 		// Handle any custom links
 		if (! (customLinkRecogniser == null))
 			if (customLinkRecogniser.isLink(namespaceURI, lName, qName, attrs)) {
 				
 			    logger.debug("Found a custom link: " + lName);
 			    
 				String href = customLinkRecogniser.getHref(namespaceURI, lName, qName, attrs);
 				String role = customLinkRecogniser.getRole(namespaceURI, lName, qName, attrs);
 				String arcrole = customLinkRecogniser.getArcrole(namespaceURI, lName, qName, attrs);
 				String title = customLinkRecogniser.getTitle(namespaceURI, lName, qName, attrs);
 				String show = customLinkRecogniser.getShow(namespaceURI, lName, qName, attrs);
 				String actuate = customLinkRecogniser.getActuate(namespaceURI, lName, qName, attrs);
 				
 				validateHref(namespaceURI,lName,qName,attrs,href);
 				validateRole(namespaceURI,lName,qName,attrs,role);
 				validateArcrole(namespaceURI,lName,qName,attrs,arcrole);
 				validateShow(namespaceURI,lName,qName,attrs,show);
 				validateActuate(namespaceURI,lName,qName,attrs,actuate);
 				
 				xlinkHandler.startSimpleLink(namespaceURI, lName, qName, attrs,href,role,arcrole,title,show,actuate);
 				ancestorTypes.push(NOT_XLINK);
 				return;
 			}
 		
 		// If not an XLink Type element, handle accordingly.
 		// Improved by Henry S Thompson
 		String xlType = attrs.getValue(Constants.XLinkNamespace, "type");
 		if (xlType == null) {
 			if (attrs.getValue(Constants.XLinkNamespace, "href") == null) {
 				ancestorTypes.push(NOT_XLINK);
 				// Throw an error if XLink attributes are used but 
 				// the xlink:type or xlink:href attributes are missing.
 				if (hasSomeXLinkAttributes) {
 					xlinkHandler.error(namespaceURI,lName,qName,attrs,"Attributes in the XLink namespace must be accompanied by xlink:type or xlink:href");
 				}
 				return;
 			} 
 			// XLink 1.1 says we default to 'simple' if xlink:type is missing but xlink:href is present
 			xlType = "simple";
 		}
 		
 		// We have a potential XLink candidate
 		// Improved by Henry S Thompson		
 		if (xlType.equals("simple")) {
 			
 			if (isXLink(namespaceURI, lName, qName, attrs,SIMPLE_LINK) &&
 				validateHref(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "href")) &&
 				validateRole(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "role")) &&
 				validateArcrole(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "arcrole")) &&
 				validateShow(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "show")) &&
 				validateActuate(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "actuate"))) {
 
 				ancestorTypes.push(SIMPLE_LINK);
 				xlinkHandler.startSimpleLink(namespaceURI, lName, qName, attrs,
 						attrs.getValue(Constants.XLinkNamespace, "href"),
 						attrs.getValue(Constants.XLinkNamespace, "role"),
 						attrs.getValue(Constants.XLinkNamespace, "arcrole"),
 						attrs.getValue(Constants.XLinkNamespace, "title"),
 						attrs.getValue(Constants.XLinkNamespace, "show"),
 						attrs.getValue(Constants.XLinkNamespace, "actuate")
 				);
 			} else {
 				ancestorTypes.push(NOT_XLINK);
 			}			
 			
 		} else if (xlType.equals("extended")) {
 			
 			if (isXLink(namespaceURI, lName, qName, attrs,EXTENDED_LINK) &&
 			    validateRole(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "role"))) {
 				
 				insideAnExtendedLink = true;
 				ancestorTypes.push(EXTENDED_LINK);
 				xlinkHandler.startExtendedLink(namespaceURI, lName, qName, attrs,
 					attrs.getValue(Constants.XLinkNamespace, "role"),
 					attrs.getValue(Constants.XLinkNamespace, "title")
 				);				
 			} else {
 				ancestorTypes.push(NOT_XLINK);
 			}
 
 		} else if (xlType.equals("locator")) {
 			
 			if (isXLink(namespaceURI, lName, qName, attrs,LOCATOR) &&
 				validateHref(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "href")) &&
 				validateRole(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "role")) &&
 				validateLabel(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "label"))) {
 				
 				ancestorTypes.push(LOCATOR);
 				xlinkHandler.startLocator(namespaceURI, lName, qName, attrs,
 						attrs.getValue(Constants.XLinkNamespace, "href"),
 						attrs.getValue(Constants.XLinkNamespace, "role"),
 						attrs.getValue(Constants.XLinkNamespace, "title"),
 						attrs.getValue(Constants.XLinkNamespace, "label")
 				);
 			} else {
 				ancestorTypes.push(NOT_XLINK);
 			}
 
 		} else if (xlType.equals("arc")) {
 			
 			if (isXLink(namespaceURI, lName, qName, attrs,ARC) &&
 			    validateLabel(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "from")) &&
 				validateLabel(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "to")) &&
 				validateArcrole(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "arcrole")) &&
 				validateShow(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "show")) &&
 				validateActuate(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "actuate"))) {
 				
 				ancestorTypes.push(ARC);
 				xlinkHandler.startArc(namespaceURI, lName, qName, attrs,
 						attrs.getValue(Constants.XLinkNamespace, "from"),
 						attrs.getValue(Constants.XLinkNamespace, "to"),
 						attrs.getValue(Constants.XLinkNamespace, "arcrole"),
 						attrs.getValue(Constants.XLinkNamespace, "title"),
 						attrs.getValue(Constants.XLinkNamespace, "show"),
 						attrs.getValue(Constants.XLinkNamespace, "actuate")
 				);				
 				
 			} else {
 				ancestorTypes.push(NOT_XLINK);
 			}
 			
 		} else if (xlType.equals("resource")) {
 			
 			if (isXLink(namespaceURI, lName, qName, attrs,RESOURCE) &&
 			    validateLabel(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "label")) &&
 				validateRole(namespaceURI,lName,qName,attrs,attrs.getValue(Constants.XLinkNamespace, "role"))) {
 				
 				ancestorTypes.push(RESOURCE);
 				xlinkHandler.startResource(namespaceURI, lName, qName, attrs,
 						attrs.getValue(Constants.XLinkNamespace, "role"),
 						attrs.getValue(Constants.XLinkNamespace, "title"),
 						attrs.getValue(Constants.XLinkNamespace, "label")
 				);
 			} else {
 				ancestorTypes.push(NOT_XLINK);
 			}
 			
 		} else if (xlType.equals("title")) {
 			
 			if (isXLink(namespaceURI, lName, qName, attrs,TITLE)) {
 				ancestorTypes.push(TITLE);				
 				xlinkHandler.startTitle(namespaceURI, lName, qName, attrs);
 			} else {
 				ancestorTypes.push(NOT_XLINK);
 			}
 
 		// XLink type attributes with a value of none should generate a warning
 		// because they are not doing anything, even though they are valid.
 		} else if (xlType.equals("none")) {
 			xlinkHandler.warning(namespaceURI, lName, qName, attrs,"The XLink type attribute equals none.  This is valid but rarely makes sense.");
 			ancestorTypes.push(NOT_XLINK);
 
 			// Any other value of the xlink type attribute is an error
 		} else {
 			xlinkHandler.error(namespaceURI, lName, qName, attrs,"The XLink type attribute must take one of the following values: extended, simple, locator, arc, resource or title or none.");
 			ancestorTypes.push(NOT_XLINK);
 		}
 
 	}
 	
 	/**
 	 * Tests if an XLink type element is really appropriate to get XLink semantics
 	 * Also has side effects of tracking the XLink ancestors seen so far in the
 	 * XML tree ancestor path and tracking the type of element (XLink or otherwise
 	 * that is the parent of this element).
 	 * @param namespaceURI
 	 * @param lName
 	 * @param qName
 	 * @param attrs
 	 * @param type
 	 * @return true if the Element is an XLink element.
 	 * @throws XLinkException
 	 */
 	private boolean isXLink(String namespaceURI, String lName, String qName,
 			Attributes attrs,Integer type) throws XLinkException {
 
 		Integer parentType = ancestorTypes.peek();
 		
 		boolean OK = true;
 		// Complain about any inappropriate attributes in the xlink namespace
 		// Contributed by Henry S Thompson
 		for (int i=0; i<attrs.getLength(); i++) {
 			if (attrs.getURI(i).equals(Constants.XLinkNamespace)) {
 				String aName = attrs.getLocalName(i);
 				Integer allowed = XLINKATTRS.get(aName);
 				if ( allowed!=null && ((allowed.intValue()&type.intValue())==0) ) {
 					xlinkHandler.error(namespaceURI,lName,qName,attrs,aName+" attribute not allowed for this type of XLink.");
 					OK = false;
 				}
 			}
 		}
 		
 		
 		// Extended links cannot contain simple links or extended links
 		if (insideAnExtendedLink)
 			if (type.equals(SIMPLE_LINK) || type.equals(EXTENDED_LINK)) {
 				xlinkHandler.warning(namespaceURI,lName,qName,attrs,"Simple and extended links have no XLink meaning when nexted in an extended link.");
 				return false;
 			}
 			
 		// Locators, resources and arcs must be children of extended links
 		if (!parentType.equals(EXTENDED_LINK))
 			if (type.equals(LOCATOR) || type.equals(ARC) || type.equals(RESOURCE)) {
 				xlinkHandler.warning(namespaceURI,lName,qName,attrs,"Arcs, locators and resources only have XLink semantics when they are children of extended link elements.");
 				return false;
 			}
 
         // Title types only imply XLink title elements when children of extended links, locators or arcs.
 		if (parentType.equals(NOT_XLINK) || parentType.equals(RESOURCE) || parentType.equals(SIMPLE_LINK) || parentType.equals(TITLE))
 			if (type.equals(TITLE)) {
 				xlinkHandler.warning(namespaceURI,lName,qName,attrs,"Titles only have XLink semantics when they are children of extended links, locators or arcs.");
 				return false;
 			}
 
 		return OK;
 		
 	}
 
 	/**
 	 * Checks that the href attribute has a value.
 	 * Note that checking that the value is a valid URI reference
 	 * after escaping of disallowed characters, is not required by
 	 * the XLink specification.  
 	 * TODO figure out how to test URI reference validity of XLink hrefs.
 	 * @param value
 	 * @return true if the parameter is valid.
 	 * @throws XLinkException if the href does not specify a value.
 	 */
 	private boolean validateHref(String namespaceURI, String lName, String qName,
 			Attributes attrs,String value) throws XLinkException {
 		if (value == "") {
 			xlinkHandler.error(namespaceURI,lName,qName,attrs,"The XLink href attribute must not be empty.");
 			return false;
 		}
 		if (value == null) {
 			xlinkHandler.error(namespaceURI,lName,qName,attrs,"The XLink href attribute must be provided on a locator.");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * The role attribute, if supplied, must be an absolute URI
 	 * @param value
 	 * @return true if the parameter is valid.
 	 * @throws XLinkException if the role is not an absolute URI
 	 */
 	private boolean validateRole(
 			String namespaceURI, 
 			String lName, 
 			String qName,
 			Attributes attrs,
 			String value) throws XLinkException {
 		if (value == null) return true;
 		try {
 			URI uri = new URI(value);
 			if (! uri.isAbsolute()) {
				xlinkHandler.error(namespaceURI,lName,qName,attrs,"The XLink role must be an absolute URI");
 				return false;
 			}
 			return true;
 		} catch (URISyntaxException e) {
			xlinkHandler.error(namespaceURI,lName,qName,attrs,"The XLink role must have valid URI syntax");
 			return false;
 		}
 	}
 
 	/**
 	 * The arcrole attribute, if supplied, must be an absolute URI
 	 * @param value
 	 * @return true if the parameter is valid.
 	 * @throws XLinkException if the arcrole is not an absolute URI
 	 */
 	private boolean validateArcrole(String namespaceURI, String lName, String qName,
 			Attributes attrs,String value) throws XLinkException {
 		if (value == null) return true;
 		try {			
 			URI uri = new URI(value);
 			if (! uri.isAbsolute()) {
 				xlinkHandler.error(namespaceURI,lName,qName,attrs,"The arcrole must be an absolute URI");
 				return false;
 			}
 			return true;
 		} catch (URISyntaxException e) {
 			xlinkHandler.error(namespaceURI,lName,qName,attrs,"The arcrole must be an absolute URI");
 			return false;
 		}
 	}
 
 	/**
 	 * Labels must be NCNames if provided.
 	 * Uses the Apache XML11Chars to test validity.
 	 * @param value The value of the label attribute
 	 * @return true if the parameter is valid.
 	 * @throws XLinkException
 	 */
 	private boolean validateLabel(String namespaceURI, String lName, String qName,
 			Attributes attrs,String value) throws XLinkException {
 		if (value == null) return true;
 		if (! XML11Char.isXML11ValidNCName(value)) {
 			xlinkHandler.error(namespaceURI,lName,qName,attrs,"The XLink traversal attribute must be an NCName.");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Show attributes must take one of a the values, new
 	 * replace, embed, other or none if supplied.
 	 * @param value
 	 * @return true if the parameter is valid.
 	 * @throws XLinkException
 	 */
 	private boolean validateShow(String namespaceURI, String lName, String qName,
 			Attributes attrs,String value) throws XLinkException {
 		if (value == null) return true;
 		if (
 				(!value.equals("new")) && 
 				(!value.equals("replace")) && 
 				(!value.equals("embed")) && 
 				(!value.equals("other")) && 
 				(!value.equals("none"))
 			){
 			xlinkHandler.error(namespaceURI,lName,qName,attrs,"If supplied, the XLink show attribute must be one of new, replace, embed, other or none. Instead it was " + value);
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Actuate attributes must take one of the values: onLoad, onRequest
 	 * other or none if supplied.
 	 * @param value
 	 * @return true if the parameter is valid.
 	 * @throws XLinkException
 	 */
 	private boolean validateActuate(String namespaceURI, String lName, String qName,
 			Attributes attrs,String value) throws XLinkException {
 		if (value == null) return true;
 		if (
 				(!value.equals("onLoad")) && 
 				(!value.equals("onRequest")) && 
 				(!value.equals("other")) && 
 				(!value.equals("none"))
 			){
 			xlinkHandler.error(namespaceURI,lName,qName,attrs,"If supplied, the XLink actuate attribute must be one of onLoad, onRequest, other or none.  Instead it was " + value);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @see org.xbrlapi.xlink.XLinkProcessor#endElement(String, String, String, Attributes)
 	 */
 	public void endElement(String namespaceURI, String lName, String qName, Attributes attrs) throws XLinkException {
 
 		xlinkHandler.xmlBaseEnd();
 		Integer parentType = ancestorTypes.pop();
 		
 		// Handle any custom links
 		if (! (customLinkRecogniser == null))
 			if (customLinkRecogniser.isLink(namespaceURI, lName, qName, attrs)) {
 				xlinkHandler.endSimpleLink(namespaceURI, lName, qName);
 				return;
 			}
 		
 		// If not an XLink Type element then we are done.
 		if (parentType.equals(NOT_XLINK))
 			return;
 		
 		// We have an XLink element so find which one
 		if (parentType.equals(SIMPLE_LINK)) {
 			xlinkHandler.endSimpleLink(namespaceURI, lName, qName);
 		} else if (parentType.equals(EXTENDED_LINK)) {
 			xlinkHandler.endExtendedLink(namespaceURI, lName, qName);
 			insideAnExtendedLink = false;
 		} else if (parentType.equals(RESOURCE)) {
 			xlinkHandler.endResource(namespaceURI, lName, qName);
 		} else if (parentType.equals(LOCATOR)) {
 			xlinkHandler.endLocator(namespaceURI, lName, qName);
 		} else if (parentType.equals(ARC)) {
 			xlinkHandler.endArc(namespaceURI, lName, qName);
 		} else if (parentType.equals(TITLE)) {
 			xlinkHandler.endTitle(namespaceURI, lName, qName);
 		}		
 	}	
 	
 	/**
 	 * @see org.xbrlapi.xlink.XLinkProcessor#titleCharacters(char[], int, int)
 	 */
 	public void titleCharacters(char[] buf, int offset, int len)
 			throws XLinkException {
 		if ((ancestorTypes.peek()).equals(TITLE)) {
 			xlinkHandler.titleCharacters(buf, offset, len);
 		}
 	}
 
 	/**
 	 * Provides access to the XLink handler being used by the XLink processor.
 	 * @return the XLink handler being used by the XLink processor.
 	 */
 	public XLinkHandler getXLinkHandler() {
 		return xlinkHandler;
 	}
 
 }
