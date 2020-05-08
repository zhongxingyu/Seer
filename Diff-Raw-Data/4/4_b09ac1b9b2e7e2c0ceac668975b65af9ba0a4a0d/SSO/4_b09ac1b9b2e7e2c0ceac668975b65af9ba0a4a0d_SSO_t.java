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
 
 package org.guanxi.idp.service.shibboleth;
 
 import java.io.StringWriter;
 import java.math.BigInteger;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlOptions;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.GuanxiPrincipal;
 import org.guanxi.common.Utils;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.common.definitions.SAML;
 import org.guanxi.common.definitions.Shibboleth;
 import org.guanxi.common.security.SecUtils;
 import org.guanxi.common.security.SecUtilsConfig;
 import org.guanxi.idp.farm.filters.IdPFilter;
 import org.guanxi.xal.idp.Creds;
 import org.guanxi.xal.idp.IdpDocument;
 import org.guanxi.xal.idp.ServiceProvider;
 import org.guanxi.xal.saml_1_0.assertion.AssertionDocument;
 import org.guanxi.xal.saml_1_0.assertion.AssertionType;
 import org.guanxi.xal.saml_1_0.assertion.AudienceRestrictionConditionDocument;
 import org.guanxi.xal.saml_1_0.assertion.AudienceRestrictionConditionType;
 import org.guanxi.xal.saml_1_0.assertion.AuthenticationStatementDocument;
 import org.guanxi.xal.saml_1_0.assertion.AuthenticationStatementType;
 import org.guanxi.xal.saml_1_0.assertion.ConditionsDocument;
 import org.guanxi.xal.saml_1_0.assertion.ConditionsType;
 import org.guanxi.xal.saml_1_0.assertion.NameIdentifierDocument;
 import org.guanxi.xal.saml_1_0.assertion.NameIdentifierType;
 import org.guanxi.xal.saml_1_0.assertion.SubjectConfirmationDocument;
 import org.guanxi.xal.saml_1_0.assertion.SubjectConfirmationType;
 import org.guanxi.xal.saml_1_0.assertion.SubjectDocument;
 import org.guanxi.xal.saml_1_0.assertion.SubjectType;
 import org.guanxi.xal.saml_1_0.protocol.ResponseDocument;
 import org.guanxi.xal.saml_1_0.protocol.ResponseType;
 import org.guanxi.xal.saml_1_0.protocol.StatusCodeType;
 import org.guanxi.xal.saml_1_0.protocol.StatusDocument;
 import org.guanxi.xal.saml_1_0.protocol.StatusType;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.w3c.dom.Document;
 
 /*
   From the Shibboleth Working Draft 02 - 22/9/04
   http://shibboleth.internet2.edu/docs/draft-mace-shibboleth-arch-protocols-02.pdf
 
   2.1.3 Single Sign-On Service
   A single sign-on (SSO) service is an HTTP resource controlled by the identity provider that receives and
   processes authentication requests sent through the browser from service providers and initiates the
   authentication process, eventually redirecting the browser to the inter-site transfer service.
   This is a Shibboleth-specific service that is not defined by SAML 1.1. It supports a normative protocol to
   initiate SSO by a service provider, which SAML 1.1 does not define.
   An identity provider may expose any number of SSO service endpoints. They SHOULD be protected by
   SSL/TLS [RFC 2246].
 
   2.1.4 Inter-Site Transfer Service
   An inter-site transfer service is an HTTP resource controlled by the identity provider that interacts with the
   authentication authority to issue HTTP responses to the principal's browser adhering to the SAML
   Browser/POST and/or Browser/Artifact profiles.
   In the case of the POST profile, the HTTP response contains the form controls necessary to transmit a
   short-lived authentication assertion inside a digitally signed <samlp:Response> message to a service
   provider's assertion consumer service.
   In the case of the Artifact profile, the HTTP response contains a Location header redirecting the
   browser to a service provider's assertion consumer service, and containing one or more SAML artifacts.
 
   3.1.1.1 Message Format and Transmission
   The HTTP request to the identity provider's SSO service endpoint MUST use the GET method and MUST
   contain the following URL-encoded query string parameters:
   providerId
   The unique identifier of the requesting service provider
   shire
   The assertion consumer service endpoint at the service provider to which to deliver the
   profile response
   target
   Generally the URL of a resource accessed at the service provider, it is returned by the
   identity provider in the TARGET form control of the authentication response
   The query string MAY contain the following optional parameter:
   time
   The current time, in seconds elapsed since midnight, January 1st, 1970, as a string of up
   to 10 base10 digits
 
   3.1.2 Browser/POST Authenticators Response
   When the Browser/POST profile is used to authenticate the principal, a signed SAML response containing
   an authentication assertion is delivered directly to the service provider in a form POST operation. The
   format of the SAML response and the associated processing rules are defined entirely by the SAML
   Browser/POST profile in [SAMLBind].
   An identity provider MAY send a response without having received an authentication request; in such a
   case, the TARGET form control MUST contain a value expected to be understood by the service provider.
   In most cases, this SHOULD be the URL of a resource to be accessed at the service provider, but MAY
   contain other values by prior agreement.
   Note that the identity provider MAY supply attributes within the <samlp:Response> message, at its
   discretion (this is implicitly permitted by the Browser/POST profile).
   The assertion(s) returned in the response MUST be consistent with the profiles described in sections 3.3-
   3.5.
 
   <HTTP-Version> 200 <Reason Phrase>
   <html>
     <Body Onload="document.forms[0].submit()">
       <FORM Method=�Post� Action=�https://<assertion consumer host name and path>� ...
       <INPUT TYPE=�hidden� NAME=�Response� Value=�B64(<response>)�>
       <INPUT TYPE=�hidden� NAME=�TARGET� Value=�<Target>�>
     </Body>
   </html>
 
   3.1.3 Browser/Artifact Authenticators Response
   When the Browser/Artifact profile is used to authenticate the principal, one or more SAML artifacts are
   issued to the service provider and transmitted in the query string of an HTTP redirect response. The
   format of the HTTP response and the associated processing rules are defined entirely by the SAML
   Browser/Artifact profile in [SAMLBind].
  */
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class SSO extends AbstractController implements ServletContextAware {
   /** Our logger */
   private static final Logger logger = Logger.getLogger(SSO.class.getName());
   private IdPFilter[] filters = null;
   private String errorView = null;
   private String shibView = null;
   /** The value of the service-provider:name in the config file that points to the default identity and creds */
   private String defaultSPEntry = null;
 
   public IdPFilter[] getFilters() { return filters; }
   public void setFilters(IdPFilter[] filters) { this.filters = filters; }
 
   public void setErrorView(String errorView) { this.errorView = errorView; }
 
   public void setShibView(String shibView) { this.shibView = shibView; }
 
   public void setDefaultSPEntry(String defaultSPEntry) { this.defaultSPEntry = defaultSPEntry; }
 
   public void init() {
   }
 
   /*
    * This is where a Shibboleth target will have first contact. The query string will have the following parameters:<br>
    * providerId - The unique identifier of the requesting service provider<br>
    * shire - The assertion consumer service endpoint at the service provider to which to deliver the profile response<br>
    * target - Generally the URL of a resource accessed at the service provider<br>
    * time - The current time, in seconds elapsed since midnight, January 1st, 1970, as a string of up to 10 base10 digits<br>
    */
   @SuppressWarnings("unchecked")
   public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
     ModelAndView mAndV = new ModelAndView();
 
     // Load up the config file
     IdpDocument.Idp idpConfig = (IdpDocument.Idp)getServletContext().getAttribute(Guanxi.CONTEXT_ATTR_IDP_CONFIG);
 
     /* The cookie interceptor should populate this if it finds a principal. The chain also
      * includes the cookie handlers so that will include embedded mode authentication.
      */
     GuanxiPrincipal principal = (GuanxiPrincipal)request.getAttribute(Guanxi.REQUEST_ATTR_IDP_PRINCIPAL);
 
     // Need these for the Response
     String issuer = null;
     String nameQualifier = null;
     String nameQualifierFormat = null;
     // Need this for signing the Response
     Creds credsConfig = null;
 
     /* Now load the appropriate identity and creds from the config file.
      * We'll either use the default or the ones that the particular SP
      * needs to be sent.
      */
     String spID = null;
     ServiceProvider[] spList = idpConfig.getServiceProviderArray();
     for (int c=0; c < spList.length; c++) {
       if (spList[c].getName().equals(request.getParameter(Shibboleth.PROVIDER_ID))) {
         spID = request.getParameter(Shibboleth.PROVIDER_ID);
       }
     }
     if (spID == null) {
       // No specific requirement for this SP so use the default identity and creds
       spID = defaultSPEntry;
     }
 
     // Now we've sorted the SP id to use, load the identity and creds
     for (int c=0; c < spList.length; c++) {
       if (spList[c].getName().equals(spID)) {
         String identityToUse = spList[c].getIdentity();
         String credsToUse = spList[c].getCreds();
 
         // We've found the <service-provider> node so look for the corresponding <identity> node
         org.guanxi.xal.idp.Identity[] ids = idpConfig.getIdentityArray();
         for (int cc=0; cc < ids.length; cc++) {
           if (ids[cc].getName().equals(identityToUse)) {
             issuer = ids[cc].getIssuer();
             nameQualifier = ids[cc].getNameQualifier();
             nameQualifierFormat = ids[cc].getFormat();
           }
         }
 
         // Look for the corresponding <creds> node
         org.guanxi.xal.idp.Creds[] creds = idpConfig.getCredsArray();
         for (int ccc=0; ccc < creds.length; ccc++) {
           if (creds[ccc].getName().equals(credsToUse)) {
             credsConfig = creds[ccc];
           }
         }
       }
     }
 
     // Associate the principal with the issuer to use...
     principal.addIssuer(request.getParameter(Shibboleth.PROVIDER_ID), issuer);
     // ...and the SAML signing credentials
     principal.addSigningCreds(request.getParameter(Shibboleth.PROVIDER_ID), credsConfig);
 
     // Sort out the namespaces for saving the Response
     HashMap<String, String> namespaces = new HashMap<String, String>();
     namespaces.put(Shibboleth.NS_SAML_10_PROTOCOL, Shibboleth.NS_PREFIX_SAML_10_PROTOCOL);
     namespaces.put(Shibboleth.NS_SAML_10_ASSERTION, Shibboleth.NS_PREFIX_SAML_10_ASSERTION);
     XmlOptions xmlOptions = new XmlOptions();
     xmlOptions.setSavePrettyPrint();
     xmlOptions.setSavePrettyPrintIndent(2);
     xmlOptions.setUseDefaultNamespace();
     xmlOptions.setSaveAggressiveNamespaces();
     xmlOptions.setSaveSuggestedPrefixes(namespaces);
     xmlOptions.setSaveNamespacesFirst();
 
     /* No need to set the InResponseTo attribute as SAML1.1 core states that if the
      * corresponding RequestID attribute of the request can't be determined then we
      * shouldn't include InResponseTo. Shibboleth makes a request, though not through
      * SAML, so RequestID in the initial GET request doesn't exist.
      */
     ResponseDocument samlResponseDoc = ResponseDocument.Factory.newInstance(xmlOptions);
     ResponseType samlResponse = samlResponseDoc.addNewResponse();
     samlResponse.setResponseID(Utils.createNCNameID());
     samlResponse.setMajorVersion(new BigInteger("1"));
     samlResponse.setMinorVersion(new BigInteger("1"));
     samlResponse.setIssueInstant(Calendar.getInstance());
     samlResponse.setRecipient(request.getParameter(Shibboleth.SHIRE));
     Utils.zuluXmlObject(samlResponse, 0);
 
     // Get a Status ready
     StatusDocument statusDoc = StatusDocument.Factory.newInstance();
     StatusType status = statusDoc.addNewStatus();
     StatusCodeType topLevelStatusCode = status.addNewStatusCode();
     topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_SUCCESS));
 
     // Add the Status to the Response
     samlResponse.setStatus(status);
 
     // Get an Assertion ready
     AssertionDocument assertionDoc = AssertionDocument.Factory.newInstance();
     AssertionType assertion = assertionDoc.addNewAssertion();
     assertion.setAssertionID(Utils.createNCNameID());
     assertion.setMajorVersion(new BigInteger("1"));
     assertion.setMinorVersion(new BigInteger("1"));
     assertion.setIssuer(issuer);
     assertion.setIssueInstant(Calendar.getInstance());
     Utils.zuluXmlObject(assertion, 0);
 
     // Conditions for the Assertion
     ConditionsDocument conditionsDoc = ConditionsDocument.Factory.newInstance();
     ConditionsType conditions = conditionsDoc.addNewConditions();
     conditions.setNotBefore(Calendar.getInstance());
     conditions.setNotOnOrAfter(Calendar.getInstance());
     Utils.zuluXmlObject(conditions, 5);
 
     // By attaching an Audience, we're saying that only the current SP can use this Assertion
     AudienceRestrictionConditionDocument audienceDoc = AudienceRestrictionConditionDocument.Factory.newInstance();
     AudienceRestrictionConditionType audience = audienceDoc.addNewAudienceRestrictionCondition();
     audience.setAudienceArray(new String[] {request.getParameter(Shibboleth.PROVIDER_ID)});
 
     // Add an Audience to the Conditions
     conditions.setAudienceRestrictionConditionArray(new AudienceRestrictionConditionType[] {audience});
 
     // Add Conditions to the Assertion
     assertion.setConditions(conditions);
 
     // Get an AuthenticationStatement ready
     AuthenticationStatementDocument authStatementDoc = AuthenticationStatementDocument.Factory.newInstance();
     AuthenticationStatementType authStatement = authStatementDoc.addNewAuthenticationStatement();
     authStatement.setAuthenticationInstant(Calendar.getInstance());
     authStatement.setAuthenticationMethod(SAML.URN_AUTH_METHOD_PASSWORD);
     Utils.zuluXmlObject(authStatement, 0);
 
     // Get a Subject ready
     SubjectDocument subjectDoc = SubjectDocument.Factory.newInstance();
     SubjectType subject = subjectDoc.addNewSubject();
 
     // Build the NameIdentifier
     NameIdentifierDocument nameIDDoc = NameIdentifierDocument.Factory.newInstance();
     NameIdentifierType nameID = nameIDDoc.addNewNameIdentifier();
     nameID.setNameQualifier(nameQualifier);
     nameID.setFormat(nameQualifierFormat);
     nameID.setStringValue(principal.getUniqueId());
 
     // Add the NameIdentifier to the Subject
     subject.setNameIdentifier(nameID);
 
     // Get a SubjectConfirmation ready
     SubjectConfirmationDocument subjectConfirmationDoc = SubjectConfirmationDocument.Factory.newInstance();
     SubjectConfirmationType subjectConfirmation = subjectConfirmationDoc.addNewSubjectConfirmation();
     subjectConfirmation.addConfirmationMethod(SAML.URN_CONFIRMATION_METHOD_BEARER);
 
     // Add the SubjectConfirmation to the Subject
     subject.setSubjectConfirmation(subjectConfirmation);
 
     // Add the Subject to the AuthenticationStatement
     authStatement.setSubject(subject);
 
     // Add the Conditions to the Assertion
     assertion.setConditions(conditions);
 
     // Add the AuthenticationStatement to the Assertion
     assertion.setAuthenticationStatementArray(new AuthenticationStatementType[] {authStatement});
 
     // Add the Assertion to the Response
     samlResponse.setAssertionArray(new AssertionType[] {assertion});
 
     // Get the config ready for signing
     SecUtilsConfig secUtilsConfig = new SecUtilsConfig();
     secUtilsConfig.setKeystoreFile(credsConfig.getKeystoreFile());
     secUtilsConfig.setKeystorePass(credsConfig.getKeystorePassword());
     secUtilsConfig.setKeystoreType(credsConfig.getKeystoreType());
     secUtilsConfig.setPrivateKeyAlias(credsConfig.getPrivateKeyAlias());
     secUtilsConfig.setPrivateKeyPass(credsConfig.getPrivateKeyPassword());
     secUtilsConfig.setCertificateAlias(credsConfig.getCertificateAlias());
     secUtilsConfig.setKeyType(credsConfig.getKeyType());
 
     // Break out to DOM land to get the SAML Response signed...
     Document signedDoc = null;
     try {
       // Need to use newDomNode to preserve namespace information
       signedDoc = SecUtils.getInstance().sign(secUtilsConfig, (Document)samlResponseDoc.newDomNode(xmlOptions), "");
     }
     catch(GuanxiException ge) {
      logger.error("Couldn't sign the Response", ge);
      mAndV.setViewName(errorView);
      return mAndV;
     }
 
     try {
       // ...and go back to XMLBeans land when it's ready
       samlResponseDoc = ResponseDocument.Factory.parse(signedDoc);
     }
     catch(XmlException xe) {
       logger.error("Couldn't get a signed Response", xe);
       mAndV.setViewName(errorView);
       return mAndV;
     }
 
     // Base 64 encode the SAML Response
     String samlResponseB64 = Utils.base64(signedDoc);
 
     // Bung the encoded Response in the HTML form
     request.setAttribute("saml_response", samlResponseB64);
 
     // Debug syphoning?
     if (idpConfig.getDebug() != null) {
       if (idpConfig.getDebug().getSypthonAttributeAssertions() != null) {
         if (idpConfig.getDebug().getSypthonAttributeAssertions().equals("yes")) {
           logger.info("=======================================================");
           logger.info("IdP response to Shire with providerId " + request.getParameter(Shibboleth.PROVIDER_ID));
           logger.info("");
           StringWriter sw = new StringWriter();
           samlResponseDoc.save(sw, xmlOptions);
           logger.info(sw.toString());
           sw.close();
           logger.info("");
           logger.info("=======================================================");
         }
       }
     }
 
     for (IdPFilter filter : filters) {
       filter.filter(principal, request.getParameter(Shibboleth.PROVIDER_ID), samlResponseDoc);
     }
 
     // Send the Response to the SP
     mAndV.setViewName(shibView);
     mAndV.getModel().put(Shibboleth.SHIRE, request.getParameter(Shibboleth.SHIRE));
     return mAndV;
   }
 }
