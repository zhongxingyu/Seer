 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.Utils;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.spec.InvalidSpecificationException;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 import org.xins.common.types.Type;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 import org.xins.common.xml.ElementSerializer;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * The SOAP calling convention.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 final class SOAPCallingConvention extends CallingConvention {
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
    
    /**
     * The request encoding format.
     */
    private static final String REQUEST_ENCODING = "UTF-8";
 
    /**
     * The response encoding format.
     */
    private static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
    private static final String RESPONSE_CONTENT_TYPE = "text/xml; charset=" + RESPONSE_ENCODING;
 
    /**
     * The key used to store the name of the function in the request attributes.
     */
    private static final String FUNCTION_NAME = "_function";
 
    /**
     * The key used to store the name of the namespace in the request attributes.
     */
    private static final String REQUEST_NAMESPACE = "_namespace";
 
    /**
     * The formatter for XINS Date type.
     */
    private static final DateFormat XINS_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    
    /**
     * The formatter for SOAP Date type.
     */
    private static final DateFormat SOAP_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * The formatter for XINS Timestamp type.
     */
    private static final DateFormat XINS_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
    
    /**
     * The formatter for SOAP dateType type.
     */
    private static final DateFormat SOAP_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
    
    /**
     * Creates a new <code>SOAPCallingConvention</code>
     *
     * @param api
     *    the API, needed for the SOAP messages.
     */
    SOAPCallingConvention(API api) {
       _api = api;
    }
    
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    /**
     * The API, never <code>null</code>.
     */
    private final API _api;
    
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
    
    
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       try {
 
          // Convert the Reader to a string buffer
          BufferedReader reader = httpRequest.getReader();
          FastStringBuffer content = new FastStringBuffer(1024);
          String nextLine;
          while ((nextLine = reader.readLine()) != null) {
             content.append(nextLine);
             content.append("\n");
          }
 
          String contentString = content.toString().trim();
          ElementParser parser = new ElementParser();
          Element envelopElem = parser.parse(new StringReader(contentString));
          
          if (!envelopElem.getLocalName().equals("Envelope")) {
             throw new ParseException("Root element is not a SOAP envelop.");
          }
          
          List bodiesElem = envelopElem.getChildElements("Body");
          if (bodiesElem.size() == 0) {
             throw new ParseException("No body specified in the SOAP envelop.");
          } else if (bodiesElem.size() > 1) {
             throw new ParseException("More than one body specified in the SOAP envelop.");
          }
          Element bodyElem = (Element) bodiesElem.get(0);
          List functionsElem = bodyElem.getChildElements();
          if (functionsElem.size() == 0) {
             throw new ParseException("No function specified in the SOAP body.");
          } else if (bodiesElem.size() > 1) {
             throw new ParseException("More than one function specified in the SOAP body.");
          }
          Element functionElem = (Element) functionsElem.get(0);
          String requestName = functionElem.getLocalName();
          String functionName = requestName.substring(0, requestName.lastIndexOf("Request"));
          httpRequest.setAttribute(FUNCTION_NAME, functionName);
          httpRequest.setAttribute(REQUEST_NAMESPACE, functionElem.getNamespaceURI());
 
          // Parse the input parameters
          Element parametersElem = null;
          List parametersList = functionElem.getChildElements("parameters");
          if (parametersList.size() == 0) {
             parametersElem = functionElem;
          } else {
             parametersElem = (Element) parametersList.get(0);
          }
 
          BasicPropertyReader parameters = new BasicPropertyReader();
          Iterator itParameters = parametersElem.getChildElements().iterator();
          while (itParameters.hasNext()) {
             Element parameterElem = (Element) itParameters.next();
             String parameterName = parameterElem.getLocalName();
             String parameterValue = parameterElem.getText();
             try {
                parameterValue = soapInputValueTransformation(functionName, parameterName, parameterValue);
             } catch (InvalidSpecificationException ise) {
                
                // keep the old value
             }
             parameters.set(parameterName, parameterValue);
          }
          
          // Parse the input data section
          Element dataSection = null;
          List dataSectionList = parametersElem.getChildElements("data");
          if (dataSectionList.size() == 1) {
             dataSection = (Element) dataSectionList.get(0);
          } else if (dataSectionList.size() > 1) {
             throw new InvalidRequestException("Only one data section is allowed.");
          }
          
          return new FunctionRequest(functionName, parameters, dataSection);
          
       // I/O error
       } catch (IOException ex) {
          throw new InvalidRequestException("Cannot read the XML request.", ex);
 
       // Parsing error
       } catch (ParseException ex) {
          throw new InvalidRequestException("Cannot parse the XML request.", ex);
       }
    }
    
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // Send the XML output to the stream and flush
       PrintWriter out = httpResponse.getWriter();
       // TODO: OutputStream out = httpResponse.getOutputStream();
       httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
       if (xinsResult.getErrorCode() != null) {
          httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
       } else {
          httpResponse.setStatus(HttpServletResponse.SC_OK);
       }
       
       // Store the result in a StringWriter before sending it.
       Writer buffer = new FastStringWriter();
 
       // Create an XMLOutputter
       XMLOutputter xmlout = new XMLOutputter(buffer, RESPONSE_ENCODING);
 
       // Output the declaration
       // XXX: Make it configurable whether the declaration is output or not?
       xmlout.declaration();
 
       // Write the envelop start tag
       xmlout.startTag("soap:Envelope");
       xmlout.attribute("xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
       
       // Write the body start tag
       xmlout.startTag("soap:Body");
       
       if (xinsResult.getErrorCode() != null) {
          
          // Write the false start tag
          xmlout.startTag("soap:Fault");
          xmlout.startTag("faultcode");
          if (xinsResult.getErrorCode().equals("_InvalidRequest")) {
             xmlout.pcdata("soap:Client");
          } else {
             xmlout.pcdata("soap:Server");
          }
          xmlout.endTag(); // faultcode
          xmlout.startTag("faultstring");
          xmlout.pcdata(xinsResult.getErrorCode());
          xmlout.endTag(); // faultstring
          xmlout.endTag(); // fault
       } else {
          
          // Write the response start tag
          String functionName = (String) httpRequest.getAttribute(FUNCTION_NAME);
          String namespaceURI = (String) httpRequest.getAttribute(REQUEST_NAMESPACE);
          xmlout.startTag("ns0:" + functionName + "Response");
          xmlout.attribute("xmlns:ns0", namespaceURI);
 
          // Write the output parameters
          Iterator outputParameterNames = xinsResult.getParameters().getNames();
          while (outputParameterNames.hasNext()) {
             String parameterName = (String) outputParameterNames.next();
             String parameterValue = xinsResult.getParameter(parameterName);
             try {
                parameterValue = soapOutputValueTransformation(functionName, parameterName, parameterValue);
             } catch (InvalidSpecificationException ise) {
                
                // keep the old value
             }
             xmlout.startTag(parameterName);
             xmlout.pcdata(parameterValue);
             xmlout.endTag();
          }
 
          // Write the data element
          Element dataElement = xinsResult.getDataElement();
          if (dataElement != null) {
             ElementSerializer serializer = new ElementSerializer();
             serializer.output(xmlout, dataElement);
          }
 
          xmlout.endTag(); // response
       }
       
       xmlout.endTag(); // body
       xmlout.endTag(); // envelop
 
       // Write the result to the servlet response
       out.write(buffer.toString());
       
       out.close();
    }
    
    /**
     * Transforms the value of a input SOAP parameter to the XINS equivalent.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @param parameterName
     *    the name of the parameter, cannot be <code>null</code>.
     *
     * @param value
     *    the value return by the SOAP parameter, cannot be <code>null</code>.
     *
     * @return
     *    the XINS value, never <code>null</code>.
     *
    * @throws InvalidSpecificationException
     *    if the specification is incorrect.
     */
    private String soapInputValueTransformation(String functionName, String parameterName, String value) throws InvalidSpecificationException {
       org.xins.common.spec.Function functionSpec = _api.getAPISpecification().getFunction(functionName);
       Type parameterType = functionSpec.getInputParameter(parameterName).getType();
       if (parameterType instanceof org.xins.common.types.standard.Boolean) {
          if (value.equals("1")) {
             return "true";
          } else if (value.equals("0")) {
             return "false";
          }
       }
       if (parameterType instanceof org.xins.common.types.standard.Date) {
          try {
             Date date = SOAP_DATE_FORMATTER.parse(value);
             return XINS_DATE_FORMATTER.format(date);
          } catch (java.text.ParseException pe) {
             Utils.logProgrammingError(pe);
          }
       }
       if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
          try {
             Date date = SOAP_TIMESTAMP_FORMATTER.parse(value);
             return XINS_TIMESTAMP_FORMATTER.format(date);
          } catch (java.text.ParseException pe) {
             Utils.logProgrammingError(pe);
          }
       }
       return value;
    }
    
    /**
     * Transforms the value of a output XINS parameter to the SOAP equivalent.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @param parameterName
     *    the name of the parameter, cannot be <code>null</code>.
     *
     * @param value
     *    the value return by the XINS function, cannot be <code>null</code>.
     *
     * @return
     *    the SOAP value, never <code>null</code>.
     *
    * @throws InvalidSpecificationException
     *    if the specification is incorrect.
     */
    private String soapOutputValueTransformation(String functionName, String parameterName, String value) throws InvalidSpecificationException {
       org.xins.common.spec.Function functionSpec = _api.getAPISpecification().getFunction(functionName);
       Type parameterType = functionSpec.getOutputParameter(parameterName).getType();
       if (parameterType instanceof org.xins.common.types.standard.Date) {
          try {
             Date date = XINS_DATE_FORMATTER.parse(value);
             return SOAP_DATE_FORMATTER.format(date);
          } catch (java.text.ParseException pe) {
             Utils.logProgrammingError(pe);
          }
       }
       if (parameterType instanceof org.xins.common.types.standard.Timestamp) {
          try {
             Date date = XINS_TIMESTAMP_FORMATTER.parse(value);
             return SOAP_TIMESTAMP_FORMATTER.format(date);
          } catch (java.text.ParseException pe) {
             Utils.logProgrammingError(pe);
          }
       }
       return value;
    }
 }
