 package soapproxy.application;
 
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
 import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
 import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
 import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion11;
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
 import com.ibm.wsdl.factory.WSDLFactoryImpl;
 import com.ibm.wsdl.xml.WSDLReaderImpl;
 import net.sf.json.JSON;
 import net.sf.json.xml.XMLSerializer;
 import org.apache.xmlbeans.XmlCursor;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;

 import soapproxy.util.Xml2JsonConverter;
 
 import javax.wsdl.BindingOperation;
 import javax.wsdl.Definition;
 import javax.wsdl.Port;
 import javax.wsdl.Service;
 import javax.wsdl.WSDLException;
 import javax.wsdl.factory.WSDLFactory;
 import javax.xml.namespace.QName;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.SOAPConnection;
 import javax.xml.soap.SOAPConnectionFactory;
 import javax.xml.soap.SOAPConstants;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.transform.dom.DOMSource;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Iterator;
 
 
 public class Json2Soap {
 
   public static final String SOATRADER_LICENSE = "5fad0242f085efedd81272894c739f092854c4a6cc6932feb3f7f0000000ffff";
   public static final QName SOATRADER_LICENSE_ELEMENT = new QName("http://ws.soatrader.com/", "SOATraderLicense");
   public static final String ATTRIBUTE_ELEMENT_PREFIX = "_attr_";
   public static final String VALUE_ELEMENT_NAME = "_value_";
 
   public static void main(String[] args) throws Exception {
     Json2Soap j2s = new Json2Soap();
     String jsonRequest = "{\"getListOfAnnualReports\":{\"registryCode\":\"11224441\", \"languageId\":\"0\"}}";
     String wsdlUri = "http://xml-services.ioc.ee:8080/ioc.ee:8080/0.1/EstonianBusinessRegistryService?wsdl";
     String operationName = "getListOfAnnualReports";
     String jsonResponse = j2s.convert(jsonRequest, wsdlUri, operationName);
     System.out.println(jsonResponse);
   }
 
   public String convert(String jsonRequest, String wsdlUri, String operationName) throws Exception {
     ObjectMapper mapper = new ObjectMapper();
     JsonNode jsonRequestParams = mapper.readValue(jsonRequest, JsonNode.class);
     // create a soap request
     XmlObject soapRequest = this.convertToSoapMessage(wsdlUri, operationName, jsonRequestParams);
     // set soatrader license if such element exists in header
     setSoaTraderLicenseIfNeeded(soapRequest);
     // get a response from the service
     SOAPMessage response = this.doRequest(soapRequest, wsdlUri);
     OutputStream out = new ByteArrayOutputStream();
     response.writeTo(out);
     String responseString = out.toString();
     XmlObject responseXml = XmlObject.Factory.parse(responseString);
 
     // NO SUPPORT FOR HEADERS at the moment
     XmlObject responseBody = SoapUtils.getBodyElement(responseXml, getSoapVersion());
 //    XmlObject attributelessResponseBody = this.removeAttributes(responseBody);
 
 //    JSON json = xmlSerializer.read(attributelessResponseBody.xmlText());
     Xml2JsonConverter converter = new Xml2JsonConverter();
     JsonNode jsonNode = converter.convert(responseBody.xmlText());
 
     return jsonNode.toString();
   }
 
   private void setSoaTraderLicenseIfNeeded(XmlObject soapRequest) throws XmlException {
     XmlObject header = SoapUtils.getHeaderElement(soapRequest, getSoapVersion(), true);
     XmlCursor headerCursor = header.newCursor();
     if (headerCursor.toChild(SOATRADER_LICENSE_ELEMENT)) {
       headerCursor.setTextValue(SOATRADER_LICENSE);
     }
   }
 
 
   private XmlObject removeAttributes(XmlObject responseBody) {
     XmlCursor cursor = responseBody.newCursor();
     while (cursor.hasNextToken()) {
       cursor.toNextToken();
       if (cursor.isAttr()) {
         cursor.removeXml();
       }
     }
 
     return responseBody;
   }
 
   private SOAPMessage doRequest(XmlObject soapRequest, String wsdlUri) throws SOAPException, WSDLException, IOException {
     System.out.println(soapRequest);
     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
     SOAPConnection soapConnection = soapConnectionFactory.createConnection();
     MessageFactory mf = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
     SOAPMessage requestMessage = mf.createMessage();
     requestMessage.getSOAPPart().setContent(new DOMSource(soapRequest.getDomNode()));
     Definition definition = getDefinition(wsdlUri);
 
     // lets currently assume that there is only one service with only one port
     Service service = (Service) definition.getServices().values().toArray()[0];
     Port port = (Port) service.getPorts().values().toArray()[0];
     String endpoint = WsdlUtils.getSoapEndpoint(port);
     SOAPMessage reply = soapConnection.call(requestMessage, endpoint);
     //Close the connection
     soapConnection.close();
     return reply;
   }
 
   private static Definition getDefinition(String wsdlUri) throws WSDLException {
     WSDLFactory factory = WSDLFactoryImpl.newInstance();
     WSDLReaderImpl reader = (WSDLReaderImpl) factory.newWSDLReader();
     return reader.readWSDL(wsdlUri);
   }
 
   private XmlObject convertToSoapMessage(String wsdlUri, String operationName, JsonNode jsonRequestParams) throws Exception {
     SoapMessageBuilder soapMessageBuilder = new SoapMessageBuilder(new WsdlContext(wsdlUri));
 
     Definition definition = getDefinition(wsdlUri);
     BindingOperation bindingOperation = WsdlUtils.findBindingOperation(definition, operationName);
     String soapMessageTemplate = soapMessageBuilder.buildSoapMessageFromInput(bindingOperation, true);
     return populateMessageTemplate(soapMessageTemplate, jsonRequestParams);
   }
 
   private XmlObject populateMessageTemplate(String soapMessageTemplate, JsonNode jsonRequestParams) throws XmlException {
     XmlObject template = XmlObject.Factory.parse(soapMessageTemplate);
     XmlObject bodyObject = SoapUtils.getBodyElement(template, getSoapVersion());
     XmlCursor cursor = bodyObject.newCursor();
 
     if (jsonRequestParams.isArray()) {
       jsonRequestParams = jsonRequestParams.get(0);
     }
 
     transferValues(jsonRequestParams, cursor);
     cursor.dispose();
 
     return template;
   }
 
   private SoapVersion11 getSoapVersion() {
     return SoapVersion.Soap11;
   }
 
   /**
    * Starting from the root of the soap request message body,
    * transfer all values that can be found in the JSON request params.
    *
    * @param jsonNode
    * @param cursor
    */
   private void transferValues(JsonNode jsonNode, XmlCursor cursor) {
     // save current location before navigating forward
     cursor.push();
 
     if (jsonNode.isArray()) {
       int childCount = jsonNode.size();
       // make necessary copies
       cursor.push();
       if (childCount > 1) {
         cursor.toFirstChild();
         XmlCursor childCursor = cursor.getObject().newCursor();
         cursor.toPrevToken();
 
         while (--childCount > 0) {
           childCursor.copyXml(cursor);
         }
       }
       cursor.pop();
 
       boolean isFirstIteration = true;
       for (Iterator it = jsonNode.getElements(); it.hasNext(); isFirstIteration = false) {
         JsonNode element = (JsonNode) it.next();
         if (isFirstIteration) {
           cursor.toFirstChild();
         } else {
           // because cursor position is still before the start of the previous child
           // just calling toNextSibling would give us previous child
           cursor.toNextSibling();
           cursor.toNextSibling();
         }
         cursor.toPrevToken();
         transferValues(element, cursor);
       }
 
     } else if (jsonNode.isContainerNode()) {
 
       while (!cursor.toNextToken().isStart()) ;
 
       for (Iterator<String> it = jsonNode.getFieldNames(); it.hasNext();) {
         String fieldName = it.next();
         JsonNode field = jsonNode.get(fieldName);
         // check for attribute and value nodes
         if (field.equals(VALUE_ELEMENT_NAME)) {
           cursor.setTextValue(field.getValueAsText());
           continue;
         }
         if (fieldName.startsWith(ATTRIBUTE_ELEMENT_PREFIX)) {
           String attributeName = fieldName.replaceFirst(ATTRIBUTE_ELEMENT_PREFIX, "");
           setAttributeValue(attributeName, jsonNode.getValueAsText(), cursor);
           continue;
         }
         cursor.push();
         if (moveCursorByLocalName(cursor, fieldName)) {
           transferValues(field, cursor);
         }
         cursor.pop();
       }
     } else if (jsonNode.isValueNode()) {
       cursor.setTextValue(jsonNode.getValueAsText());
     }
     cursor.pop();
   }
 
   private void setAttributeValue(String attributeName, String value, XmlCursor cursor) {
     cursor.push();
     while(!cursor.toNextToken().isEnd()) {
       if (cursor.isAttr() && cursor.getName().getLocalPart().equals(attributeName)) {
         cursor.setTextValue(value);
       }
     }
     cursor.pop();
   }
 
   private boolean moveCursorByLocalName(XmlCursor cursor, String fieldName) {
     boolean childExists;
     if (!(childExists = cursor.getName().getLocalPart().equals(fieldName))) {
       while (cursor.toNextSibling() && !(childExists = cursor.getName().getLocalPart().equals(fieldName))) ;
     }
 
     return childExists;
   }
 
 
   /**
    * @param jsonRequestParams
    * @param localName
    * @return
    */
   private JsonNode findJsonNode(JsonNode jsonRequestParams, String localName) {
     JsonNode node = null;
     if (jsonRequestParams.isArray()) {
       for (Iterator it = jsonRequestParams.getElements(); it.hasNext();) {
         JsonNode tmpNode = findJsonNode((JsonNode) it.next(), localName);
         if (tmpNode != null) return tmpNode;
       }
     }
     for (Iterator it = jsonRequestParams.getFieldNames(); it.hasNext();) {
       String fieldName = (String) it.next();
       if (fieldName.equals(localName)) {
         node = jsonRequestParams.get(fieldName);
       }
     }
     return node;
   }
 }
