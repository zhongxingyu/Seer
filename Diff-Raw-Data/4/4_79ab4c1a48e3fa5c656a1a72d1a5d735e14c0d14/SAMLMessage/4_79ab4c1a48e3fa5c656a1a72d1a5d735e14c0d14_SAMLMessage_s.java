 package org.zaproxy.zap.extension.saml;
 
 import org.joda.time.DateTime;
 import org.opensaml.Configuration;
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.common.SAMLVersion;
 import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
 import org.opensaml.saml2.core.AuthnRequest;
 import org.opensaml.saml2.core.Response;
 import org.opensaml.xml.ConfigurationException;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.io.Marshaller;
 import org.opensaml.xml.io.MarshallerFactory;
 import org.opensaml.xml.io.Unmarshaller;
 import org.opensaml.xml.io.UnmarshallerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.ByteArrayInputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class SAMLMessage {
     private String originalMessage;
     private Map<String, String> attributeMapping;
     private String samlParameter;
     private XMLObject unmarshalledObject;
 
     public SAMLMessage(String originalMessage, String samlParameter) {
         try {
             DefaultBootstrap.bootstrap();
             this.originalMessage = originalMessage;
             this.samlParameter = samlParameter;
         } catch (ConfigurationException e) {
 
         }
     }
 
     /**
      * Reset the message (which might have changed from original one) back to original message
      */
     public void resetMessage() {
         unmarshalledObject = null;
     }
 
     public String getSamlParameter() {
         return samlParameter;
     }
 
     /**
      * Convert the raw saml xml string to a pretty formatted String
      *
      * @return The Pretty formatted XML String
      * @throws SAMLException If XML parsing failed
      */
     public String getPrettyFormattedMessage() throws SAMLException {
         try {
             Source xmlInput;
             if (unmarshalledObject != null) {
                 MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
                 Marshaller marshaller = marshallerFactory.getMarshaller(unmarshalledObject);
                 Element element = marshaller.marshall(unmarshalledObject);
                 xmlInput = new DOMSource(element);
             } else {
                 xmlInput = new StreamSource(new StringReader(originalMessage));
             }
 
             StringWriter stringWriter = new StringWriter();
             StreamResult xmlOutput = new StreamResult(stringWriter);
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             transformerFactory.setAttribute("indent-number", 4);
             Transformer transformer = transformerFactory.newTransformer();
 
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(xmlInput, xmlOutput);
             return xmlOutput.getWriter().toString();
         } catch (Exception e) {
             throw new SAMLException("Formatting XML failed", e);
         }
     }
 
     /**
      * Get the list of attributes that are interested in
      *
      * @return Map with key as attribute name and value as attribute value
      * @throws SAMLException If attribute extraction failed
      */
     public Map<String, String> getAttributeMapping() throws SAMLException {
         if (attributeMapping == null || attributeMapping.isEmpty()) {
             extractAttributes();
         }
         return attributeMapping;
     }
 
     /**
      * Extract the attributes from the SAML string using DOM and unmarshaller
      *
      * @throws SAMLException
      */
     private void extractAttributes() throws SAMLException {
         try {
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             documentBuilderFactory.setNamespaceAware(true);
             DocumentBuilder docBuilder = null;
             docBuilder = documentBuilderFactory.newDocumentBuilder();
             ByteArrayInputStream is = new ByteArrayInputStream(originalMessage.getBytes("UTF-8"));
 
             Document document = docBuilder.parse(is);
             Element element = document.getDocumentElement();
             UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
             Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
             unmarshalledObject = unmarshaller.unmarshall(element);
             attributeMapping = new LinkedHashMap<>();
             attributeMapping = new LinkedHashMap<>();
             for (String attribute : getSelectedAttributes()) {
                 String value = getValueOf(attribute);
                 if (value != null && !"".equals(value)) {
                     attributeMapping.put(attribute, value);
                 }
             }
         } catch (Exception e) {
             throw new SAMLException("Error in extracting the attributes", e);
         }
     }
 
     /**
      * Get the user preferred attributes to be shown in the attribute list
      * @return Set o
      */
     private Set<String> getSelectedAttributes() {
         //Hardcoded to show all attributes (if available) for now. will be taken from a config file later
         Set<String> result = new HashSet<>();
         result.add("AuthnRequest[ID]");
         result.add("AuthnRequest[AssertionConsumerServiceURL]");
         result.add("AuthnRequest[AttributeConsumingServiceIndex]");
         result.add("AuthnRequest[IssueInstant]");
         result.add("AuthnRequest[ProtocolBinding]");
         result.add("AuthnRequest[Version]");
         result.add("AuthnRequest:Issuer");
         result.add("AuthnRequest:NameIDPolicy[Format]");
         result.add("AuthnRequest:NameIDPolicy[SPNameQualifier]");
         result.add("AuthnRequest:NameIDPolicy[AllowCreate]");
         result.add("AuthnRequest:RequestedAuthnContext[Comparison]");
         result.add("AuthnRequest:RequestedAuthnContext:AuthnContextClassRef");
 
         result.add("Assertion[ID]");
         result.add("Assertion[IssueInstant]");
         result.add("Assertion[Version]");
         result.add("Assertion:Issuer");
         result.add("Assertion:Issuer[Format]");
         result.add("Assertion:Subject:NameID");
         result.add("Assertion:Subject:SubjectConfirmation[Method]");
         result.add("Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[InResponseTo]");
         result.add("Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[Recipient]");
         result.add("Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[NotOnOrAfter]");
         result.add("Assertion:Conditions[NotOnOrAfter]");
         result.add("Assertion:Conditions[NotBefore]");
         result.add("Assertion:Conditions:AudienceRestriction:Audience");
         result.add("Assertion:AuthnStatement[AuthnInstant]");
         result.add("Assertion:AuthnStatement[SessionIndex]");
         result.add("Assertion:AuthnStatement:AuthnContext:AuthnContextClassRef");
 
         return result;
     }
 
     /**
      * Get the value of a attribute as given by the key
      * @param key The attribute name
      * @return Value of the attribute as a string, empty string if attribute not set.
      */
     public String getValueOf(String key) {
         if (unmarshalledObject == null) {
             return "";
         }
         if (key.startsWith("AuthnRequest")) {
             return getAuthnRequestValue(key);
         } else if(key.startsWith("Assertion")){
             return getResponseValue(key);
         }
         return "";
     }
 
     /**
      * Set the value of a given attribute to the value specified
      * @param key The attribute name to set the value
      * @param value The value of the attribute to be set
      * @return <code>true</code> if value is succesfully set,<code>false</code> otherwise
      * @throws SAMLException if the given value type is not accepted by the key
      */
     public boolean setValueTo(String key, String value) throws SAMLException {
         if (unmarshalledObject == null) {
             return false;
         }
         if (key.startsWith("AuthnRequest")) {
             return setAuthnRequestValue(key, value);
         } else if (key.startsWith("Assertion")){
             return setResponseValue(key,value);
         }
         return false;
     }
 
     /**
      * Get the attribute value of a AUthRequest
      * @param key the name of the attribute
      * @return the value of the attribute
      */
     private String getAuthnRequestValue(String key) {
         if (!(unmarshalledObject instanceof AuthnRequest)) {
             return "";
         }
         AuthnRequest authnRequest = (AuthnRequest) unmarshalledObject;
         switch (key) {
             case "AuthnRequest[ID]":
                 return authnRequest.getID();
             case "AuthnRequest[AssertionConsumerServiceURL]":
                 return authnRequest.getAssertionConsumerServiceURL();
             case "AuthnRequest[AttributeConsumingServiceIndex]":
                 return String.valueOf(authnRequest.getAttributeConsumingServiceIndex());
             case "AuthnRequest[IssueInstant]":
                 return authnRequest.getIssueInstant().toString();
             case "AuthnRequest[ProtocolBinding]":
                 return authnRequest.getProtocolBinding();
             case "AuthnRequest[Version]":
                 return authnRequest.getVersion().toString();
             case "AuthnRequest:Issuer":
                 return authnRequest.getIssuer().getValue();
             case "AuthnRequest:NameIDPolicy[Format]":
                 return authnRequest.getNameIDPolicy().getFormat();
             case "AuthnRequest:NameIDPolicy[SPNameQualifier]":
                 return authnRequest.getNameIDPolicy().getSPNameQualifier();
             case "AuthnRequest:NameIDPolicy[AllowCreate]":
                 return authnRequest.getNameIDPolicy().getAllowCreate().toString();
             case "AuthnRequest:RequestedAuthnContext[Comparison]":
                 return authnRequest.getRequestedAuthnContext()
                         .getComparison().toString();
             case "AuthnRequest:RequestedAuthnContext:AuthnContextClassRef":
                 return authnRequest.getRequestedAuthnContext()
                         .getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
         }
         return "";
     }
 
     /**
      * Set the value of the given attribute in a AuthRequest
      * @param key The attribute name
      * @param value The attribute value
      * @return <code>true</code> if the value is set, false if the key is not a valid one
      * @throws SAMLException if value type is not accepted by key
      */
     private boolean setAuthnRequestValue(String key, String value) throws SAMLException {
         if (!(unmarshalledObject instanceof AuthnRequest)) {
             return false;
         }
         AuthnRequest authnRequest = (AuthnRequest) unmarshalledObject;
         switch (key) {
             case "AuthnRequest[ID]":
                 authnRequest.setID(value);
                 return true;
             case "AuthnRequest[AssertionConsumerServiceURL]":
                 authnRequest.setAssertionConsumerServiceURL(value);
                 return true;
             case "AuthnRequest[AttributeConsumingServiceIndex]":
                 try {
                     Integer intValue = Integer.parseInt(value);
                     authnRequest.setAttributeConsumingServiceIndex(intValue);
                     return true;
                 } catch (NumberFormatException e) {
                     throw new SAMLException("Given: " + value + " \nExpected: Integer");
                 }
             case "AuthnRequest[IssueInstant]":
                 authnRequest.setIssueInstant(DateTime.parse(value));
                 return true;
             case "AuthnRequest[ProtocolBinding]":
                 authnRequest.setProtocolBinding(value);
                 return true;
             case "AuthnRequest[Version]":
                 authnRequest.setVersion(SAMLVersion.valueOf(value));
                 return true;
             case "AuthnRequest:Issuer":
                 authnRequest.getIssuer().setValue(value);
                 return true;
             case "AuthnRequest:NameIDPolicy[Format]":
                 authnRequest.getNameIDPolicy().setFormat(value);
                 return true;
             case "AuthnRequest:NameIDPolicy[SPNameQualifier]":
                 authnRequest.getNameIDPolicy().setSPNameQualifier(value);
                 return true;
             case "AuthnRequest:NameIDPolicy[AllowCreate]":
                 if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                     authnRequest.getNameIDPolicy().setAllowCreate(Boolean.valueOf(value));
                     return true;
                 } else {
                     throw new SAMLException("Given Value :" + value + " \nExpected either 'true' or 'false'");
                 }
 
             case "AuthnRequest:RequestedAuthnContext[Comparison]":
                AuthnContextComparisonTypeEnumeration type = authnRequest.getRequestedAuthnContext().getComparison();
                switch (value) {
                     case "EXACT":
                         type = AuthnContextComparisonTypeEnumeration.EXACT;
                         break;
                     case "BETTER":
                         type = AuthnContextComparisonTypeEnumeration.BETTER;
                         break;
                     case "MAXIMUM":
                         type = AuthnContextComparisonTypeEnumeration.MAXIMUM;
                         break;
                     case "MINIMUM":
                         type = AuthnContextComparisonTypeEnumeration.MINIMUM;
                         break;
                     default:
                         throw new SAMLException("Invalid value. \nGiven:" + value + " \nExpected:One of 'EXACT'," +
                                 "'BETTER','MAXIMUM','MINIMUM'");
                 }
                 authnRequest.getRequestedAuthnContext().setComparison(type);
                 return true;
             case "AuthnRequest:RequestedAuthnContext:AuthnContextClassRef":
                 try {
                     authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).setAuthnContextClassRef(value);
                     return true;
                 } catch (Exception e) {
                     throw new SAMLException("Invalid value given or value can't be set because of the unavailability " +
                             "of a dependent elemet");
                 }
 
         }
         return false;
     }
 
     /**
      * Get the attribute value of a Response
      * @param key the name of the attribute
      * @return the value of the attribute
      */
     private String getResponseValue(String key) {
         if (!(unmarshalledObject instanceof Response)) {
             return "";
         }
         Response response = (Response) unmarshalledObject;
         try {
             switch (key) {
                 case "Assertion[ID]":
                     return response.getAssertions().get(0).getID();
                 case "Assertion[IssueInstant]":
                     return response.getAssertions().get(0).getIssueInstant().toString();
                 case "Assertion[Version]":
                     return response.getAssertions().get(0).getVersion().toString();
                 case "Assertion:Issuer":
                     return response.getAssertions().get(0).getIssuer().getValue();
                 case "Assertion:Issuer[Format]":
                     return response.getAssertions().get(0).getIssuer().getFormat();
                 case "Assertion:Subject:NameID":
                     return response.getAssertions().get(0).getSubject().getNameID().getValue();
                 case "Assertion:Subject:SubjectConfirmation[Method]":
                     return response.getAssertions().get(0).getSubject()
                             .getSubjectConfirmations().get(0).getMethod();
                 case "Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[InResponseTo]":
                     return response.getAssertions().get(0).getSubject()
                             .getSubjectConfirmations().get(0).getSubjectConfirmationData().getInResponseTo();
                 case "Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[Recipient]":
                     return response.getAssertions().get(0).getSubject()
                             .getSubjectConfirmations().get(0).getSubjectConfirmationData().getRecipient();
                 case "Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[NotOnOrAfter]":
                     return response.getAssertions().get(0).getSubject()
                             .getSubjectConfirmations().get(0).getSubjectConfirmationData().getNotOnOrAfter().toString();
                 case "Assertion:Conditions[NotOnOrAfter]":
                     return response.getAssertions().get(0).getConditions()
                             .getNotOnOrAfter().toString();
                 case "Assertion:Conditions[NotBefore]":
                     return response.getAssertions().get(0).getConditions()
                             .getNotBefore().toString();
                 case "Assertion:Conditions:AudienceRestriction:Audience":
                     return response.getAssertions().get(0)
                             .getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI();
                 case "Assertion:AuthnStatement[AuthnInstant]":
                     return response.getAssertions().get(0).getAuthnStatements
                             ().get(0).getAuthnInstant().toString();
                 case "Assertion:AuthnStatement[SessionIndex]":
                     return response.getAssertions().get(0).getAuthnStatements
                             ().get(0).getSessionIndex();
                 case "Assertion:AuthnStatement:AuthnContext:AuthnContextClassRef":
                     return response.getAssertions().get(0).getAuthnStatements
                             ().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
             }
         } catch (Exception e) {
             return "";
         }
 
         return "";
     }
 
     /**
      * Set the value of the given attribute in a Response
      * @param key The attribute name
      * @param value The attribute value
      * @return <code>true</code> if the value is set, false if the key is not a valid one
      * @throws SAMLException if value type is not accepted by key
      */
     private boolean setResponseValue(String key, String value) throws SAMLException {
         if (!(unmarshalledObject instanceof AuthnRequest)) {
             return false;
         }
         Response response = (Response) unmarshalledObject;
         try {
             switch (key) {
                 case "Assertion[ID]":
                     response.getAssertions().get(0).setID(value);
                     return true;
                 case "Assertion[IssueInstant]":
                     response.getAssertions().get(0).setIssueInstant(DateTime.parse(value));
                     return true;
                 case "Assertion[Version]":
                     response.getAssertions().get(0).setVersion(SAMLVersion.valueOf(value));
                     return true;
                 case "Assertion:Issuer":
                     response.getAssertions().get(0).getIssuer().setValue(value);
                     return true;
                 case "Assertion:Issuer[Format]":
                     response.getAssertions().get(0).getIssuer().setFormat(value);
                     return true;
                 case "Assertion:Subject:NameID":
                     response.getAssertions().get(0).getSubject().getNameID().setValue(value);
                 case "Assertion:Subject:SubjectConfirmation[Method]":
                     response.getAssertions().get(0).getSubject().getSubjectConfirmations().get(0).setMethod(value);
                     return true;
                 case "Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[InResponseTo]":
                     response.getAssertions().get(0).getSubject().getSubjectConfirmations().get(0)
                             .getSubjectConfirmationData().setInResponseTo(value);
                     return true;
                 case "Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[Recipient]":
                     response.getAssertions().get(0).getSubject().getSubjectConfirmations().get(0)
                             .getSubjectConfirmationData().setRecipient(value);
                     return true;
                 case "Assertion:Subject:SubjectConfirmation:SubjectConfirmationData[NotOnOrAfter]":
                     response.getAssertions().get(0).getSubject().getSubjectConfirmations().get(0)
                             .getSubjectConfirmationData().setNotOnOrAfter(DateTime.parse(value));
                     return true;
                 case "Assertion:Conditions[NotOnOrAfter]":
                     response.getAssertions().get(0).getConditions().setNotOnOrAfter(DateTime.parse(value));
                     return true;
                 case "Assertion:Conditions[NotBefore]":
                     response.getAssertions().get(0).getConditions().setNotBefore(DateTime.parse(value));
                 case "Assertion:Conditions:AudienceRestriction:Audience":
                     response.getAssertions().get(0).getConditions().getAudienceRestrictions().get(0).getAudiences()
                             .get(0).setAudienceURI(value);
                     return true;
                 case "Assertion:AuthnStatement[AuthnInstant]":
                     response.getAssertions().get(0).getAuthnStatements().get(0).setAuthnInstant(DateTime.parse(value));
                     return true;
                 case "Assertion:AuthnStatement[SessionIndex]":
                     response.getAssertions().get(0).getAuthnStatements().get(0).setSessionIndex(value);
                     return true;
                 case "Assertion:AuthnStatement:AuthnContext:AuthnContextClassRef":
                     response.getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext()
                             .getAuthnContextClassRef().setAuthnContextClassRef(value);
                     return true;
             }
         } catch (Exception e) {
             throw new SAMLException("Can't set the value: '"+value+"' to the key: '"+key+"'");
         }
         return false;
     }
 
 }
