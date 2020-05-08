 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.spec.DataSectionElementSpec;
 import org.xins.common.spec.EntityNotFoundException;
 import org.xins.common.spec.FunctionSpec;
 import org.xins.common.spec.InvalidSpecificationException;
 import org.xins.common.spec.ParameterSpec;
 import org.xins.common.types.Type;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 import org.xins.common.xml.ElementSerializer;
 
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * The SOAP calling convention.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 final class SOAPCallingConvention extends CallingConvention {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The response encoding format.
     */
    private static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
    private static final String RESPONSE_CONTENT_TYPE = "text/xml;charset=" + RESPONSE_ENCODING;
 
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
    private static final SimpleDateFormat XINS_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
 
    /**
     * The formatter for SOAP Date type.
     */
    private static final SimpleDateFormat SOAP_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
 
    /**
     * The formatter for XINS Timestamp type.
     */
    private static final SimpleDateFormat XINS_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
 
    /**
     * The formatter for SOAP dateType type.
     */
    private static final SimpleDateFormat SOAP_TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Gets a unique child of an element.
     *
     * @param parentElement
     *    the parent element, cannot be <code>null</code>.
     *
     * @param elementName
     *    the name of the child element to get, or <code>null</code> if the
     *    parent should have exactly one child element and the name is
     *    considered irrelevant.
     *
     * @return
     *    the sub-element of this element, never <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if either no matching child was found or multiple matching children
     *    were found.
     */
    private static Element getUniqueChild(Element parentElement,
                                          String  elementName)
    throws InvalidRequestException {
 
       // Get the list of matching child elements
       List childList = elementName == null
                      ? parentElement.getChildElements()
                      : parentElement.getChildElements(elementName);
 
       // No matches
       if (childList.size() == 0) {
          throw new InvalidRequestException("No \"" + elementName +
                "\" children found in the \"" + parentElement.getLocalName() +
               "\" element of the XML-RPC request.");
 
       // Multiple matches
       } else if (childList.size() > 1) {
          throw new InvalidRequestException("More than one \"" + elementName +
                "\" children found in the \"" + parentElement.getLocalName() +
               "\" element of the XML-RPC request.");
 
       // Exactly one match
       } else {
          return (Element) childList.get(0);
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
 
    /**
     * Creates a new <code>SOAPCallingConvention</code> instance.
     *
     * @param api
     *    the API, needed for the SOAP messages, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>api == null</code>.
     */
    public SOAPCallingConvention(API api)
    throws IllegalArgumentException {
 
       // Check arguments
       MandatoryArgumentChecker.check("api", api);
 
       // Store the API
       _api = api;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API. Never <code>null</code>.
     */
    private final API _api;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified request can be handled by this calling
     * convention.
     *
     * <p>This method will not throw any exception.
     *
     * @param httpRequest
     *    the HTTP request to investigate, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if this calling convention is <em>possibly</em>
     *    able to handle this request, or <code>false</code> if it
     *    <em>definitely</em> not able to handle this request.
     *
     * @throws Exception
     *    if analysis of the request causes an exception;
     *    <code>false</code> will be assumed.
     */
    protected boolean matches(HttpServletRequest httpRequest)
    throws Exception {
 
       // Parse the XML in the request (if any)
       Element element = parseXMLRequest(httpRequest);
 
       // The root element must be <Envelope/>
       if (element.getLocalName().equals("Envelope")) {
 
          // There must be a <Body/> element within the <Envelope/>
          Element bodyElement = getUniqueChild(element, "Body");
 
          // There must be one child element
          List bodyChildren = bodyElement.getChildElements();
          if (bodyChildren != null && bodyChildren.size() == 1) {
             Element functionElement     = (Element) bodyChildren.get(0);
             String  functionElementName = functionElement.getLocalName();
 
             // The name of the child element must match '<Function>Request'
             return functionElementName.endsWith("Request") &&
                    functionElementName.length() > 7;
          }
       }
 
       return false;
    }
 
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException {
 
       Element envelopeElem = parseXMLRequest(httpRequest);
 
       if (! envelopeElem.getLocalName().equals("Envelope")) {
          throw new InvalidRequestException("Root element is not a SOAP envelope but \"" +
                envelopeElem.getLocalName() + "\".");
       }
 
       List bodiesElem = envelopeElem.getChildElements("Body");
       if (bodiesElem.size() == 0) {
          throw new InvalidRequestException("No body specified in the SOAP envelope.");
       } else if (bodiesElem.size() > 1) {
          throw new InvalidRequestException("More than one body specified in the SOAP envelope.");
       }
       Element bodyElem = (Element) bodiesElem.get(0);
       List functionsElem = bodyElem.getChildElements();
       if (functionsElem.size() == 0) {
          throw new InvalidRequestException("No function specified in the SOAP body.");
       } else if (bodiesElem.size() > 1) {
          throw new InvalidRequestException("More than one function specified in the SOAP body.");
       }
       Element functionElem = (Element) functionsElem.get(0);
       String requestName = functionElem.getLocalName();
       if (!requestName.endsWith("Request")) {
          throw new InvalidRequestException("Function names should always end " +
                "\"Request\" for the SOAP calling convention.");
       }
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
             FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
             Type parameterType = functionSpec.getInputParameter(parameterName).getType();
             parameterValue = soapInputValueTransformation(parameterType, parameterValue);
          } catch (InvalidSpecificationException ise) {
 
             // keep the old value
          } catch (EntityNotFoundException enfe) {
 
             // keep the old value
          }
          parameters.set(parameterName, parameterValue);
       }
 
       // Parse the input data section
       Element dataSection = null;
       Element transformedDataSection = null;
       List dataSectionList = parametersElem.getChildElements("data");
       if (dataSectionList.size() == 1) {
          dataSection = (Element) dataSectionList.get(0);
 
          try {
             FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
             Map dataSectionSpec = functionSpec.getInputDataSectionElements();
             transformedDataSection = soapElementTransformation(dataSectionSpec, true, dataSection, true);
          } catch (InvalidSpecificationException ise) {
 
             // keep the old value
             transformedDataSection = dataSection;
          } catch (EntityNotFoundException enfe) {
 
             // keep the old value
             transformedDataSection = dataSection;
          }
       } else if (dataSectionList.size() > 1) {
          throw new InvalidRequestException("Only one data section is allowed.");
       }
 
       return new FunctionRequest(functionName, parameters, transformedDataSection);
    }
 
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // Send the XML output to the stream and flush
       httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
       PrintWriter out = httpResponse.getWriter();
       if (xinsResult.getErrorCode() != null) {
          httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
       } else {
          httpResponse.setStatus(HttpServletResponse.SC_OK);
       }
 
       // Store the result in a StringWriter before sending it.
       Writer buffer = new FastStringWriter(1024);
 
       // Create an XMLOutputter
       XMLOutputter xmlout = new XMLOutputter(buffer, RESPONSE_ENCODING);
 
       // Output the declaration
       // XXX: Make it configurable whether the declaration is output or not?
       xmlout.declaration();
 
       // Write the envelope start tag
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
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                Type parameterType = functionSpec.getOutputParameter(parameterName).getType();
                parameterValue = soapOutputValueTransformation(parameterType, parameterValue);
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
             }
             xmlout.startTag(parameterName);
             xmlout.pcdata(parameterValue);
             xmlout.endTag();
          }
 
          // Write the data element
          Element dataElement = xinsResult.getDataElement();
          if (dataElement != null) {
 
             Element transformedDataElement = null;
             try {
                FunctionSpec functionSpec = _api.getAPISpecification().getFunction(functionName);
                Map dataSectionSpec = functionSpec.getOutputDataSectionElements();
                transformedDataElement = soapElementTransformation(dataSectionSpec, true, dataElement, true);
             } catch (InvalidSpecificationException ise) {
 
                // keep the old value
                transformedDataElement = dataElement;
             } catch (EntityNotFoundException enfe) {
 
                // keep the old value
                transformedDataElement = dataElement;
             }
 
             ElementSerializer serializer = new ElementSerializer();
             serializer.output(xmlout, transformedDataElement);
          }
 
          xmlout.endTag(); // response
       }
 
       xmlout.endTag(); // body
       xmlout.endTag(); // envelope
 
       // Write the result to the servlet response
       out.write(buffer.toString());
 
       out.close();
    }
 
    /**
     * Transforms the value of a input SOAP parameter to the XINS equivalent.
     *
     * @param parameterType
     *    the type of the parameter, cannot be <code>null</code>.
     *
     * @param value
     *    the value of the SOAP parameter, cannot be <code>null</code>.
     *
     * @return
     *    the XINS value, never <code>null</code>.
     *
     * @throws InvalidSpecificationException
     *    if the specification is incorrect.
     */
    private String soapInputValueTransformation(Type parameterType, String value) throws InvalidSpecificationException {
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
     * @param parameterType
     *    the type of the parameter, cannot be <code>null</code>.
     *
     * @param value
     *    the value returned by the XINS function, cannot be <code>null</code>.
     *
     * @return
     *    the SOAP value, never <code>null</code>.
     *
     * @throws InvalidSpecificationException
     *    if the specification is incorrect.
     */
    private String soapOutputValueTransformation(Type parameterType, String value) throws InvalidSpecificationException {
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
 
    /**
     * Convert the values of element to the required format.
     *
     * @param dataSection
     *    the specification of the elements, cannot be <code>null</code>.
     *
     * @param input
     *    <code>true</code> if it's the input parameter that should be transform,
     *    <code>false</code> if it's the output parameter.
     *
     * @param element
     *    the element node to process, cannot be <code>null</code>.
     *
     * @param top
     *    <code>true</code> if it's the top element, <code>false</code> otherwise.
     *
     * @return
     *    the converted value, never <code>null</code>.
     */
    private Element soapElementTransformation(Map dataSection, boolean input, Element element, boolean top) {
       String elementName = element.getLocalName();
       String elementNameSpaceURI = element.getNamespaceURI();
       Map elementAttributes = element.getAttributeMap();
       String elementText = element.getText();
       List elementChildren = element.getChildElements();
       Map childrenSpec = dataSection;
 
       ElementBuilder builder = new ElementBuilder(elementNameSpaceURI, elementName);
 
       if (!top) {
          builder.setText(elementText);
 
          // Find the DataSectionElement for this element.
          DataSectionElementSpec elementSpec = (DataSectionElementSpec) dataSection.get(elementName);
          childrenSpec = elementSpec.getSubElements();
 
          // Go through the attributes
          Iterator itAttributeNames = elementAttributes.keySet().iterator();
          while (itAttributeNames.hasNext()) {
             Element.QualifiedName attributeQName = (Element.QualifiedName) itAttributeNames.next();
             String attributeName = attributeQName.getLocalName();
             String attributeValue = (String) elementAttributes.get(attributeQName);
             try {
 
                // Convert the value if needed
                ParameterSpec attributeSpec = elementSpec.getAttribute(attributeName);
                Type attributeType = attributeSpec.getType();
                if (input) {
                   attributeValue = soapInputValueTransformation(attributeType, attributeValue);
                } else {
                   attributeValue = soapOutputValueTransformation(attributeType, attributeValue);
                }
             } catch (InvalidSpecificationException ise) {
 
                // Keep the old value
             } catch (EntityNotFoundException enfe) {
 
                // Keep the old value
             }
 
             builder.setAttribute(attributeName, attributeValue);
          }
       }
 
       // Add the children of this element
       Iterator itChildren = elementChildren.iterator();
       while (itChildren.hasNext()) {
          Element nextChild = (Element) itChildren.next();
          Element transformedChild = soapElementTransformation(childrenSpec , input, nextChild, false);
          builder.addChild(transformedChild);
       }
 
       return builder.createElement();
    }
 }
