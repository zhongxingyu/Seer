 /*
  * $Id$
  */
 package org.xins.client;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Collections;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.collections.CollectionUtils;
 
 /**
  * Parser that takes XML to build a <code>FunctionCaller</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.45
  */
 public abstract class FunctionCallerParser
 extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The logging category used by this class. This class field is never
     * <code>null</code>.
     */
    private final static Logger LOG = Logger.getLogger(FunctionCallerParser.class.getName());
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>FunctionCallerParser</code>.
     */
    public FunctionCallerParser() {
       _xmlBuilder = new SAXBuilder();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Parser that takes an XML document and converts it to a JDOM Document.
     */
    private final SAXBuilder _xmlBuilder;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Parses the given XML string to create a <code>FunctionCaller</code> object.
     *
     * @param xml
     *    the XML to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>xml == null</code>
     *
     * @throws ParseException
     *    if the specified string is not valid XML or if the structure of the
     *    XML is not valid for the definition of a {@link FunctionCaller}.
     */
    public FunctionCaller parse(String xml)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("xml", xml);
 
       try {
          StringReader reader = new StringReader(xml);
          return parse(_xmlBuilder.build(reader));
       } catch (JDOMException jdomException) {
          final String message = "Unable to parse XML returned by API.";
          LOG.error(message, jdomException);
          // TODO: Include type of error in here somewhere
          throw new ParseException(message, jdomException);
       }
    }
 
    /**
     * Parses the given XML document to create a <code>FunctionCaller</code>
     * object.
     *
     * @param document
     *    the document to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>document == null || document.getRootElement() == null</code>
     *
    * @throws ParseException
     *    if the specified XML document is not valid as the definition of a
     *    {@link FunctionCaller}.
     */
    private FunctionCaller parse(Document document)
    throws NullPointerException, ParseException {
       return parse(document.getRootElement());
    }
 
 
    /**
     * Parses the given XML element to create a <code>FunctionCaller</code>
     * object.
     *
     * @param element
     *    the element to be parsed, not <code>null</code>.
     *
     * @return
     *    a {@link FunctionCaller}, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>element == null</code>
     *
    * @throws ParseException
     *    if the specified XML element is not valid as the definition of a
     *    {@link FunctionCaller}.
     */
    private FunctionCaller parse(Element element)
    throws NullPointerException, ParseException {
 
       String elementName = element.getName();
 
       // Check that the root element is either <group/> or <api/>
       if ("group".equals(elementName)) {
          return null; // TODO
       } else if ("api".equals(elementName)) {
          return null; // TODO
       }
 
       String message = "The returned XML is invalid. The type of the root element is \"" + elementName + "\" instead of \"group\".";
       LOG.error(message);
       throw new ParseException(message);
    }
 }
