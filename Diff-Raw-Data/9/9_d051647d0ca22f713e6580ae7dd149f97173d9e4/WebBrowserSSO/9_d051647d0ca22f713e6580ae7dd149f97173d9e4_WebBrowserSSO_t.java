 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.idp.service.saml2;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.context.MessageSource;
 import org.guanxi.xal.saml_2_0.protocol.*;
 import org.guanxi.xal.saml_2_0.assertion.*;
 import org.guanxi.xal.idp.UserAttributesDocument;
 import org.guanxi.xal.idp.IdpDocument;
 import org.guanxi.idp.service.SSOBase;
 import org.guanxi.common.GuanxiPrincipal;
 import org.guanxi.common.Utils;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.security.SecUtils;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.common.definitions.SAML;
 import org.w3c.dom.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.StringWriter;
 import java.util.Calendar;
 import java.security.cert.X509Certificate;
 import java.net.URLEncoder;
 
 /**
  * SAML2 Web Browser SSO Single Sign-On Service.
  * This is the authentication request protocol endpoint at the identity provider to which the
  * <AuthnRequest> message (or artifact representing it) is delivered by the user agent.
  *
  * @author alistair
  */
 public class WebBrowserSSO extends SSOBase {
   /** The JSP to use to POST the response to the SP */
   private String httpPOSTView = null;
   /** The JSP to use to GET the response to the SP */
   private String httpRedirectView = null;
   /** The JSP to display if an error occurs */
   private String errorView = null;
   /** The request attribute that holds the error message for the error view */
   private String errorViewDisplayVar = null;
   /** Whether to encrypt attributes in the response */
   private boolean encryptAttributes;
 
   /**
    * Handles processing of an AuthnRequest message. If the request attribute:
    * wbsso-handler-error-message
    * is present then we must display the error text it contains and go no further.
    * 
    * @param request Servlet request
    * @param response Servlet response
    * @return the view to display
    * @throws Exception if an error occurs
    */
   public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
     ModelAndView mAndV = new ModelAndView();
 
     idpConfig = (IdpDocument.Idp)getServletContext().getAttribute(Guanxi.CONTEXT_ATTR_IDP_CONFIG);
     loadPersona(request);
     String spEntityID = (String)request.getAttribute("entityID");
 
     // Display an error message if it exists and go no further
     if (request.getAttribute("wbsso-handler-error-message") != null) {
       if (request.getAttribute("wbsso-handler-error-message").equals(SAML.SAML2_STATUS_NO_PASSIVE)) {
         // Return SAML2 Response as we don't do passive
         ResponseDocument errorDoc = buidErrorResponse(idpEntityID,
                                                       (String)request.getAttribute("requestID"),
                                                       (String)request.getAttribute("acsURL"),
                                                       SAML.SAML2_STATUS_NO_PASSIVE);
 
         xmlOptions.setSaveSuggestedPrefixes(saml2Namespaces);
 
         Document signedDoc = null;
         if (request.getAttribute("responseBinding").equals(SAML.SAML2_BINDING_HTTP_POST)) {
           try {
             // Need to use newDomNode to preserve namespace information
             signedDoc = SecUtils.getInstance().saml2Sign(secUtilsConfig,
                                                          (Document)errorDoc.newDomNode(xmlOptions),
                                                           errorDoc.getResponse().getID());
           }
           catch(GuanxiException ge) {
             logger.error("Could not sign Response", ge);
             mAndV.setViewName(errorView);
             mAndV.getModel().put(errorViewDisplayVar, messages.getMessage("error.could.not.sign.message",
                                                                           null, request.getLocale()));
             return mAndV;
           }
         }
 
         // Do the binding quickstep
         String b64SAMLResponse = null;
         if (request.getAttribute("responseBinding").equals(SAML.SAML2_BINDING_HTTP_POST)) {
           b64SAMLResponse = Utils.base64(signedDoc);
           mAndV.setViewName(httpPOSTView);
         }
         else if (request.getAttribute("responseBinding").equals(SAML.SAML2_BINDING_HTTP_REDIRECT)) {
           String deflatedResponse = Utils.deflate(errorDoc.toString(), Utils.RFC1951_DEFAULT_COMPRESSION_LEVEL, Utils.RFC1951_NO_WRAP);
           b64SAMLResponse = Utils.base64(deflatedResponse.getBytes());
           b64SAMLResponse = b64SAMLResponse.replaceAll(System.getProperty("line.separator"), "");
           b64SAMLResponse = URLEncoder.encode(b64SAMLResponse, "UTF-8");
           mAndV.setViewName(httpRedirectView);
         }
 
         // Debug syphoning?
         if (idpConfig.getDebug() != null) {
           if (idpConfig.getDebug().getSypthonAttributeAssertions() != null) {
             if (idpConfig.getDebug().getSypthonAttributeAssertions().equals("yes")) {
               logger.info("=======================================================");
               logger.info("Error Response to SAML2 WBSSO request by " + spEntityID);
               logger.info("");
               StringWriter sw = new StringWriter();
               errorDoc.save(sw, xmlOptions);
               logger.info(sw.toString());
               logger.info("");
               logger.info("=======================================================");
             }
           }
         }
 
         request.setAttribute("SAMLResponse", b64SAMLResponse);
         request.setAttribute("RelayState", request.getParameter("RelayState"));
         mAndV.getModel().put("wbsso_acs_endpoint", request.getAttribute("acsURL"));
         return mAndV;
       }
       else {
         logger.error("Displaying auth handler error");
         mAndV.setViewName(errorView);
         mAndV.getModel().put(errorViewDisplayVar, request.getAttribute("wbsso-handler-error-message"));
         return mAndV;
       }
     } // if (request.getAttribute("wbsso-handler-error-message") != null)
 
     GuanxiPrincipal gxPrincipal = (GuanxiPrincipal)request.getAttribute(Guanxi.REQUEST_ATTR_IDP_PRINCIPAL);
    if (request.getAttribute("NameIDFormat") != null) {
      // Use the NameID format supplied in the request...
      gxPrincipal.setNameIDFormat((String)request.getAttribute("NameIDFormat"));
    }
    else {
      // ...or the default if none is specified
      gxPrincipal.setNameIDFormat(nameQualifier);
    }
 
     // We need this for reference in the Response
     String requestID = (String)request.getAttribute("requestID");
 
     // Response
     ResponseDocument responseDoc = ResponseDocument.Factory.newInstance();
     ResponseType wbssoResponse = responseDoc.addNewResponse();
     wbssoResponse.setID(generateStringID());
     wbssoResponse.setVersion("2.0");
     wbssoResponse.setDestination((String)request.getAttribute("acsURL"));
     wbssoResponse.setIssueInstant(Calendar.getInstance());
     wbssoResponse.setInResponseTo(requestID);
     Utils.zuluXmlObject(wbssoResponse, 0);
 
     // Response/Issuer
     NameIDType issuer = wbssoResponse.addNewIssuer();
     issuer.setFormat(SAML.URN_SAML2_NAMEID_FORMAT_ENTITY);
     issuer.setStringValue(idpEntityID);
 
     // Response/Status
     StatusDocument statusDoc = StatusDocument.Factory.newInstance();
     StatusType status = statusDoc.addNewStatus();
     StatusCodeType topLevelStatusCode = status.addNewStatusCode();
     topLevelStatusCode.setValue(SAML.SAML2_STATUS_SUCCESS);
     wbssoResponse.setStatus(status);
 
     // Response/Assertion
     AssertionDocument assertionDoc = AssertionDocument.Factory.newInstance();
     AssertionType assertion = assertionDoc.addNewAssertion();
     assertion.setID(generateStringID());
     assertion.setIssueInstant(Calendar.getInstance());
     assertion.setIssuer(issuer);
     assertion.setVersion("2.0");
     Utils.zuluXmlObject(assertion, 0);
 
     // Response/Assertion/Subject
     SubjectType subject = assertion.addNewSubject();
     SubjectConfirmationType subjectConfirmation = subject.addNewSubjectConfirmation();
     subjectConfirmation.setMethod(SAML.URN_SAML2_CONFIRMATION_METHOD_BEARER);
     SubjectConfirmationDataType subjectConfirmationData = subjectConfirmation.addNewSubjectConfirmationData();
     subjectConfirmationData.setAddress(request.getLocalAddr());
     subjectConfirmationData.setInResponseTo(requestID);
     subjectConfirmationData.setNotOnOrAfter(Calendar.getInstance());
     subjectConfirmationData.setRecipient((String)request.getAttribute("acsURL"));
     Utils.zuluXmlObject(subjectConfirmationData, assertionTimeLimit);
 
     // Response/Assertion/Conditions
     ConditionsType conditions = assertion.addNewConditions();
     conditions.setNotBefore(Calendar.getInstance());
     conditions.setNotOnOrAfter(Calendar.getInstance());
     AudienceRestrictionType audienceRestriction = conditions.addNewAudienceRestriction();
     audienceRestriction.addAudience(spEntityID);
     Utils.zuluXmlObject(conditions, assertionTimeLimit);
 
     // Response/Assertion/AuthnStatement
     AuthnStatementType authnStatement = assertion.addNewAuthnStatement();
     authnStatement.setAuthnInstant(Calendar.getInstance());
     authnStatement.setSessionIndex("");
     authnStatement.addNewSubjectLocality().setAddress(request.getLocalAddr());
     authnStatement.addNewAuthnContext().setAuthnContextClassRef(SAML.URN_SAML2_PASSWORD_PROTECTED_TRANSPORT);
     authnStatement.setSessionIndex("860e1c78883a682e07697c494b0ff1641847b128ec28cc8b597fb");
     Utils.zuluXmlObject(authnStatement, 0);
 
     // Assemble the attributes
     UserAttributesDocument attributesDoc = UserAttributesDocument.Factory.newInstance();
     UserAttributesDocument.UserAttributes attributes = attributesDoc.addNewUserAttributes();
     for (org.guanxi.idp.farm.attributors.Attributor attr : attributor) {
       attr.getAttributes(gxPrincipal, spEntityID, arpEngine, mapper, attributes);
     }
     AttributeStatementDocument attrStatementDoc = getSAML2AttributeStatementFromFarm(attributesDoc, spEntityID,
                                                                                      subject, gxPrincipal);
 
     // If a user has no attributes we shouldn't add an Assertion or Subject
     if (attrStatementDoc != null) {
       // Response/Assertion/AttributeStatement
       assertion.setAttributeStatementArray(new AttributeStatementType[] {attrStatementDoc.getAttributeStatement()});
     }
 
     // Sort out the namespaces for saving the Response
     xmlOptions.setSaveSuggestedPrefixes(saml2Namespaces);
 
     // Debug syphoning?
     if (idpConfig.getDebug() != null) {
       if (idpConfig.getDebug().getSypthonAttributeAssertions() != null) {
         if (idpConfig.getDebug().getSypthonAttributeAssertions().equals("yes")) {
           logger.info("=======================================================");
           logger.info("Response to SAML2 WBSSO request by " + spEntityID);
           logger.info("");
           StringWriter sw = new StringWriter();
           responseDoc.save(sw, xmlOptions);
           assertionDoc.save(sw, xmlOptions);
           logger.info(sw.toString());
           logger.info("");
           logger.info("=======================================================");
         }
       }
     }
 
     if (encryptAttributes) {
       // Get the SP's encryption key. We'll use this to encrypt the secret key for encrypting the attributes
       X509Certificate encryptionCert = getX509CertFromMetadata(getSPMetadata(spEntityID), ENTITY_SP, ENCRYPTION_CERT);
       if (encryptionCert != null) {
         addEncryptedAssertionsToResponse(encryptionCert, assertionDoc, responseDoc);
       }
       else {
         logger.warn("Attribute encryption is on but " + spEntityID + " has no encryption key");
         responseDoc.getResponse().addNewAssertion();
         responseDoc.getResponse().setAssertionArray(0, assertionDoc.getAssertion());
       }
     }
     else {
       responseDoc.getResponse().addNewAssertion();
       responseDoc.getResponse().setAssertionArray(0, assertionDoc.getAssertion());
     }
 
     // Break out to DOM land to get the SAML Response signed...
     Document signedDoc = null;
     if (request.getAttribute("responseBinding").equals(SAML.SAML2_BINDING_HTTP_POST)) {
       try {
         // Need to use newDomNode to preserve namespace information
         signedDoc = SecUtils.getInstance().saml2Sign(secUtilsConfig,
                                                      (Document)responseDoc.newDomNode(xmlOptions),
                                                       responseDoc.getResponse().getID());
       }
       catch(GuanxiException ge) {
         logger.error("Could not sign Response", ge);
         mAndV.setViewName(errorView);
         mAndV.getModel().put(errorViewDisplayVar, messages.getMessage("error.could.not.sign.message",
                                                                       null, request.getLocale()));
         return mAndV;
       }
     }
  
     // Do the binding quickstep
     String b64SAMLResponse = null;
     if (request.getAttribute("responseBinding").equals(SAML.SAML2_BINDING_HTTP_POST)) {
       b64SAMLResponse = Utils.base64(signedDoc);
       mAndV.setViewName(httpPOSTView);
     }
     else if (request.getAttribute("responseBinding").equals(SAML.SAML2_BINDING_HTTP_REDIRECT)) {
       String deflatedResponse = Utils.deflate(responseDoc.toString(), Utils.RFC1951_DEFAULT_COMPRESSION_LEVEL, Utils.RFC1951_NO_WRAP);
       b64SAMLResponse = Utils.base64(deflatedResponse.getBytes());
       b64SAMLResponse = b64SAMLResponse.replaceAll(System.getProperty("line.separator"), "");
       b64SAMLResponse = URLEncoder.encode(b64SAMLResponse, "UTF-8");
       mAndV.setViewName(httpRedirectView);
     }
 
     // Send the Response to the SP
     request.setAttribute("SAMLResponse", b64SAMLResponse);
     request.setAttribute("RelayState", request.getParameter("RelayState"));
     mAndV.getModel().put("wbsso_acs_endpoint", request.getAttribute("acsURL"));
     return mAndV;
   }
 
   // Setters
   public void setMessages(MessageSource messages) { this.messages = messages; }
   public void setHttpPOSTView(String httpPOSTView) { this.httpPOSTView = httpPOSTView; }
   public void setHttpRedirectView(String httpRedirectView) { this.httpRedirectView = httpRedirectView; }
   public void setErrorView(String errorView) { this.errorView = errorView; }
   public void setErrorViewDisplayVar(String errorViewDisplayVar) { this.errorViewDisplayVar = errorViewDisplayVar; }
   public void setEncryptAttributes(boolean encryptAttributes) { this.encryptAttributes = encryptAttributes; }
 }
