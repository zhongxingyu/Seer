 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SAML2Utils.java,v 1.5 2007-04-02 06:02:13 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.saml2.common;
 
 import com.sun.identity.common.SystemConfigurationUtil;
 import com.sun.identity.cot.CircleOfTrustManager;
 import com.sun.identity.cot.CircleOfTrustDescriptor;
 import com.sun.identity.cot.COTException;
 import com.sun.identity.federation.common.FSUtils;
 
 import com.sun.identity.plugin.datastore.DataStoreProvider;
 import com.sun.identity.plugin.datastore.DataStoreProviderException;
 import com.sun.identity.plugin.datastore.DataStoreProviderManager;
 import com.sun.identity.plugin.session.SessionManager;
 import com.sun.identity.plugin.session.SessionException;
 import com.sun.identity.saml.common.SAMLConstants;
 import com.sun.identity.saml.common.SAMLUtilsCommon;
 import com.sun.identity.saml.xmlsig.KeyProvider;
 import com.sun.identity.saml2.assertion.AssertionFactory;
 import com.sun.identity.saml2.assertion.Assertion;
 import com.sun.identity.saml2.assertion.AudienceRestriction;
 import com.sun.identity.saml2.assertion.AuthnStatement;
 import com.sun.identity.saml2.assertion.Conditions;
 import com.sun.identity.saml2.assertion.EncryptedAssertion;
 import com.sun.identity.saml2.assertion.Issuer;
 import com.sun.identity.saml2.assertion.NameID;
 import com.sun.identity.saml2.assertion.Subject;
 import com.sun.identity.saml2.assertion.SubjectConfirmation;
 import com.sun.identity.saml2.assertion.SubjectConfirmationData;
 import com.sun.identity.saml2.idpdiscovery.IDPDiscoveryConstants;
 import com.sun.identity.saml2.jaxb.entityconfig.IDPSSOConfigElement;
 import com.sun.identity.saml2.jaxb.entityconfig.BaseConfigType;
 import com.sun.identity.saml2.key.KeyUtil;
 import com.sun.identity.saml2.logging.LogUtil;
 import com.sun.identity.saml2.profile.CacheCleanUpThread;
 import com.sun.identity.saml2.profile.AuthnRequestInfo;
 import com.sun.identity.saml2.plugins.DefaultSPAuthnContextMapper;
 import com.sun.identity.saml2.plugins.IDPAccountMapper;
 import com.sun.identity.saml2.profile.IDPCache;
 import com.sun.identity.saml2.plugins.SPAccountMapper;
 import com.sun.identity.saml2.plugins.SPAuthnContextMapper;
 import com.sun.identity.saml2.profile.SPCache;
 import com.sun.identity.saml2.protocol.ProtocolFactory;
 import com.sun.identity.saml2.protocol.RequestedAuthnContext;
 import com.sun.identity.saml2.protocol.Response;
 import com.sun.identity.saml2.protocol.Status;
 import com.sun.identity.saml2.protocol.StatusCode;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.configuration.SystemPropertiesManager;
 import com.sun.identity.shared.encode.Base64;
 import com.sun.identity.shared.encode.URLEncDec;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.sun.identity.saml2.jaxb.entityconfig.SPSSOConfigElement;
 import com.sun.identity.saml2.jaxb.metadata.AssertionConsumerServiceElement;
 import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.SecureRandom;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.soap.Detail;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.MimeHeader;
 import javax.xml.soap.MimeHeaders;
 import javax.xml.soap.SOAPBody;
 import javax.xml.soap.SOAPConnection;
 import javax.xml.soap.SOAPConnectionFactory;
 import javax.xml.soap.SOAPElement;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPFault;
 import javax.xml.soap.SOAPMessage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * The <code>SAML2Utils</code> contains utility methods for SAML 2.0
  * implementation.
  */
 public class SAML2Utils extends SAML2SDKUtils {
     
     private static Thread cThread = null;
     
     // SAML2MetaManager
     private static SAML2MetaManager saml2MetaManager = null;
     private static CircleOfTrustManager cotManager = null;
     private static KeyProvider keyProvider = KeyUtil.getKeyProviderInstance();
     
     private static String server_protocol =
             SystemPropertiesManager.get(Constants.AM_SERVER_PROTOCOL);
     private static String server_host =
             SystemPropertiesManager.get(Constants.AM_SERVER_HOST);
     private static String server_port =
             SystemPropertiesManager.get(Constants.AM_SERVER_PORT);
     private static String localURL = server_protocol + "://" + server_host +
             ":" + server_port;
     private static int int_server_port = 0;
     
     public static SOAPConnectionFactory scf = null;
     static {
         try {
             scf = SOAPConnectionFactory.newInstance();
         } catch (SOAPException se) {
             debug.error(
                     "Unable to obtain SOAPConnectionFactory.", se);
         }
         try {
             int_server_port = Integer.parseInt(server_port);
         } catch (NumberFormatException nfe) {
             debug.error("Unable to parse port " + server_port, nfe);
         }
     }
     
     public static MessageFactory mf = null;
     static {
         try {
             mf = MessageFactory.newInstance();
         } catch (SOAPException se) {
             debug.error(
                     "SAML2Utils: Unable to create SOAP MessageFactory", se);
         }
     }
     
     static {
         try {
             saml2MetaManager =
                     new SAML2MetaManager();
         } catch (SAML2MetaException sme) {
             debug.error("Error retreiving metadata",sme);
         }
         
         try {
             cotManager = new CircleOfTrustManager();
         } catch (COTException sme) {
             debug.error("Error retreiving COT ",sme);
         }
         if (SystemConfigurationUtil.isServerMode()) {
             cThread = new CacheCleanUpThread();
             cThread.start();
         }
     }
     
     static AssertionFactory af = AssertionFactory.getInstance();
     private static SecureRandom randomGenerator = new SecureRandom();
     
     /**
      * Verifies single sign on <code>Response</code> and returns information
      * to SAML2 auth module for further processing. This method is used by
      * SAML2 auth module only.
      * @param response Single Sign On <code>Response</code>.
      * @param orgName name of the realm or organization the provider is in.
      * @param hostEntityId Entity ID of the hosted provider.
      * @param isPOSTBinding A flag indicating whether the binding is POST or not
      * @return A Map of information extracted from the Response. The keys of
      *          map are: <code>SAML2Constants.SUBJECT</code>,
      *                  <code>SAML2Constants.POST_ASSERTION</code>,
      *                  <code>SAML2Constants.ASSERTIONS</code>,
      *                  <code>SAML2Constants.SESSION_INDEX</code>,
      *                  <code>SAML2Constants.AUTH_LEVEL</code>,
      *                  <code>SAML2Constants.MAX_SESSION_TIME</code>.
      * @throws SAML2Exception if the Response is not valid according to the
      *          processing rules.
      */
     public static Map verifyResponse(Response response,
             String orgName,
             String hostEntityId,
             boolean isPOSTBinding)
             throws SAML2Exception {
         String method = "SAML2Utils.verifyResponse:";
         if (response == null || orgName == null || orgName.length() == 0) {
             if (debug.messageEnabled()) {
                 debug.message(method + "response or orgName is null.");
             }
             throw new SAML2Exception(bundle.getString("nullInput"));
         }
         
         String respID = response.getID();
         AuthnRequestInfo reqInfo = null;
         String inRespToResp = response.getInResponseTo();
         if (inRespToResp != null && inRespToResp.length() != 0) {
            reqInfo = (AuthnRequestInfo)SPCache.requestHash.remove(
                inRespToResp);
             if (reqInfo == null) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "InResponseTo attribute in Response"
                             + " is invalid: " + inRespToResp);
                 }
                 String[] data = {respID};
                 LogUtil.error(Level.INFO,
                         LogUtil.INVALID_INRESPONSETO_RESPONSE,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString(
                         "invalidInResponseToInResponse"));
             }
         }
         
         String idpEntityId = null;
         Issuer respIssuer = response.getIssuer();
         if (respIssuer != null) { // optional
             if (!isSourceSiteValid(respIssuer, orgName,hostEntityId)) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "Issuer in Response is not valid.");
                 }
                 String[] data = {hostEntityId, orgName, respID};
                 LogUtil.error(Level.INFO,
                         LogUtil.INVALID_ISSUER_RESPONSE,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString(
                         "invalidIssuerInResponse"));
             } else {
                 idpEntityId = respIssuer.getValue();
             }
         }
         
         Status status = response.getStatus();
         if (status == null || !status.getStatusCode().getValue().equals(
                 SAML2Constants.STATUS_SUCCESS)) {
             String statusCode =
                     (status == null)?"":status.getStatusCode().getValue();
             if (debug.messageEnabled()) {
                 debug.message(method
                         + "Response's status code is not success."
                         + statusCode);
             }
             String[] data = {respID, ""};
             if (LogUtil.isErrorLoggable(Level.FINE)) {
                 data[1] = statusCode;
             }
             LogUtil.error(Level.INFO,
                     LogUtil.WRONG_STATUS_CODE,
                     data,
                     null);
             throw new SAML2Exception(
                     bundle.getString("invalidStatusCodeInResponse"));
         }
         
         if (saml2MetaManager == null) {
             throw new SAML2Exception(bundle.getString("nullMetaManager"));
         }
         SPSSOConfigElement spConfig = null;
         SPSSODescriptorElement spDesc = null;
         spConfig = saml2MetaManager.getSPSSOConfig(
                 orgName, hostEntityId);
         spDesc = saml2MetaManager.getSPSSODescriptor(orgName, hostEntityId);
         
         // decide if assertion needs to be encrypted/decrypted
         boolean needAssertionEncrypted = false;
         String assertionEncryptedAttr = getAttributeValueFromSPSSOConfig(
                 spConfig,
                 SAML2Constants.WANT_ASSERTION_ENCRYPTED);
         if (assertionEncryptedAttr != null &&
                 assertionEncryptedAttr.equals("true")) {
             needAssertionEncrypted = true;
         }
         
         // decide if assertion needs to be signed/verified
         boolean needAssertionSigned = isPOSTBinding;
         if (!needAssertionSigned) {
             needAssertionSigned = spDesc.isWantAssertionsSigned();
         }
         
         List assertions = response.getAssertion();
         if (needAssertionEncrypted && (assertions != null)
         && (assertions.size() != 0)) {
             String[] data = {respID};
             LogUtil.error(Level.INFO,
                     LogUtil.ASSERTION_NOT_ENCRYPTED,
                     data,
                     null);
             throw new SAML2Exception(
                     SAML2Utils.bundle.getString("assertionNotEncrypted"));
         }
         PrivateKey decryptionKey = null;
         List encAssertions = response.getEncryptedAssertion();
         if (encAssertions != null) {
             Iterator encIter = encAssertions.iterator();
             while (encIter.hasNext()) {
                 if (decryptionKey == null) {
                     decryptionKey = KeyUtil.getDecryptionKey(spConfig);
                 }
                 Assertion assertion = ((EncryptedAssertion) encIter.next()).
                         decrypt(decryptionKey);
                 if (assertions == null) {
                     assertions = new ArrayList();
                 }
                 assertions.add(assertion);
             }
         }
         
         if (assertions == null || assertions.size() == 0) {
             if (debug.messageEnabled()) {
                 debug.message(method + "no assertion in the Response.");
             }
             String[] data = {respID};
             LogUtil.error(Level.INFO,
                     LogUtil.MISSING_ASSERTION,
                     data,
                     null);
             throw new SAML2Exception(
                     SAML2Utils.bundle.getString("missingAssertion"));
         }
         
         Map smap = null;
         IDPSSODescriptorElement idp = null;
         X509Certificate cert = null;
         Iterator assertionIter = assertions.iterator();
         while (assertionIter.hasNext()) {
             Assertion assertion = (Assertion) assertionIter.next();
             String assertionID = assertion.getID();
             Issuer issuer = assertion.getIssuer();
             if (!isSourceSiteValid(issuer, orgName, hostEntityId)) {
                 debug.error("assertion's source site is not valid.");
                 String[] data = {assertionID};
                 LogUtil.error(Level.INFO,
                         LogUtil.INVALID_ISSUER_ASSERTION,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString(
                         "invalidIssuerInAssertion"));
             }
             if (idpEntityId == null) {
                 idpEntityId = issuer.getValue();
             } else {
                 if (!idpEntityId.equals(issuer.getValue())) {
                     if (debug.messageEnabled()) {
                         debug.message(method + "Issuer in Assertion doesn't "
                                 + "match the Issuer in Response or other "
                                 + "Assertions in the Response.");
                     }
                     String[] data = {assertionID};
                     LogUtil.error(Level.INFO,
                             LogUtil.MISMATCH_ISSUER_ASSERTION,
                             data,
                             null);
                     throw new SAML2Exception(
                             SAML2Utils.bundle.getString("mismatchIssuer"));
                 }
             }
             if (needAssertionSigned) {
                 if (cert == null) {
                     idp = saml2MetaManager.getIDPSSODescriptor(
                             orgName, idpEntityId);
                     cert = KeyUtil.getVerificationCert(idp, idpEntityId, true);
                 }
                 if (!assertion.isSigned() || !assertion.isSignatureValid(cert)){
                     debug.error(method +
                             "Assertion is not signed or signature is not valid.");
                     String[] data = {assertionID};
                     LogUtil.error(Level.INFO,
                             LogUtil.INVALID_SIGNATURE_ASSERTION,
                             data,
                             null);
                     throw new SAML2Exception(bundle.getString(
                             "invalidSignatureOnAssertion"));
                 }
             }
             List authnStmts = assertion.getAuthnStatements();
             if (authnStmts != null && !authnStmts.isEmpty()) {
                 Subject subject = assertion.getSubject();
                 if (subject == null) {
                     continue;
                 }
                 List subjectConfirms = subject.getSubjectConfirmation();
                 if (subjectConfirms == null || subjectConfirms.isEmpty()) {
                     continue;
                 }
                 if (!isBearerSubjectConfirmation(subjectConfirms,
                         inRespToResp,
                         spDesc,
                         spConfig,
                         assertionID)) {
                     continue;
                 }
                 checkAudience(assertion.getConditions(),
                         hostEntityId,
                         assertionID);
                 if (smap == null) {
                     smap = fillMap(authnStmts,
                             subject,
                             assertion,
                             assertions,
                             reqInfo,
                             inRespToResp,
                             orgName,
                             hostEntityId,
                             idpEntityId,
                             spConfig);
                 }
             } // end of having authnStmt
         }
         
         if (smap == null) {
             debug.error("No Authentication Assertion in Response.");
             throw new SAML2Exception(bundle.getString("missingAuthnAssertion"));
         }
         
         return smap;
     }
     
     private static boolean isBearerSubjectConfirmation(List subjectConfirms,
             String inRespToResponse,
             SPSSODescriptorElement spDesc,
             SPSSOConfigElement spConfig,
             String assertionID)
             throws SAML2Exception {
         String method = "SAML2Utils.isBearerSubjectConfirmation:";
         boolean hasBearer = false;
         for (Iterator it = subjectConfirms.iterator();it.hasNext();) {
             SubjectConfirmation subjectConfirm =
                     (SubjectConfirmation)it.next();
             if (subjectConfirm == null ||
                     subjectConfirm.getMethod() == null ||
                     !subjectConfirm.getMethod().equals(
                     SAML2Constants.SUBJECT_CONFIRMATION_METHOD_BEARER)) {
                 continue;
             }
             // since this is bearer SC, all below must be true
             SubjectConfirmationData subjectConfData =
                     subjectConfirm.getSubjectConfirmationData();
             if (subjectConfData == null) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "missing SubjectConfirmationData.");
                 }
                 String[] data = {assertionID};
                 LogUtil.error(Level.INFO,
                         LogUtil.MISSING_SUBJECT_COMFIRMATION_DATA,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString(
                         "missingSubjectConfirmationData"));
             }
             
             String recipient = subjectConfData.getRecipient();
             if (recipient == null || recipient.length() == 0) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "missing Recipient in Assertion.");
                 }
                 String[] data = {assertionID};
                 LogUtil.error(Level.INFO,
                         LogUtil.MISSING_RECIPIENT,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString("missingRecipient"));
             }
             boolean foundMatch = false;
             Iterator acsIter = spDesc.getAssertionConsumerService().iterator();
             while (acsIter.hasNext()) {
                 AssertionConsumerServiceElement acs =
                         (AssertionConsumerServiceElement) acsIter.next();
                 if (recipient.equals(acs.getLocation())) {
                     foundMatch = true;
                     break;
                 }
             }
             if (!foundMatch) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "this sp is not the intended "
                             + "recipient.");
                 }
                 String[] data = {assertionID, recipient};
                 LogUtil.error(Level.INFO,
                         LogUtil.WRONG_RECIPIENT,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString("wrongRecipient"));
             }
             
             // in seconds
             int timeskew = SAML2Constants.ASSERTION_TIME_SKEW_DEFAULT;
             String timeskewStr = getAttributeValueFromSPSSOConfig(
                     spConfig,
                     SAML2Constants.ASSERTION_TIME_SKEW);
             if (timeskewStr != null && timeskewStr.trim().length() > 0) {
                 timeskew = Integer.parseInt(timeskewStr);
                 if (timeskew < 0) {
                     timeskew = SAML2Constants.ASSERTION_TIME_SKEW_DEFAULT;
                 }
             }
             if (debug.messageEnabled()) {
                 debug.message(method + "timeskew = " + timeskew);
             }
             
             Date notOnOrAfter = subjectConfData.getNotOnOrAfter();
             if (notOnOrAfter == null ||
                     ((notOnOrAfter.getTime() + timeskew * 1000) <
                     System.currentTimeMillis())) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "Time in SubjectConfirmationData of "
                             + "Assertion:" + assertionID + " is invalid.");
                 }
                 String[] data = {assertionID};
                 LogUtil.error(Level.INFO,
                         LogUtil.INVALID_TIME_SUBJECT_CONFIRMATION_DATA,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString(
                         "invalidTimeOnSubjectConfirmationData"));
             }
             if (subjectConfData.getNotBefore() != null) {
                 if (debug.messageEnabled()) {
                     debug.message(method + "SubjectConfirmationData included "
                             + "NotBefore.");
                 }
                 String[] data = {assertionID};
                 LogUtil.error(Level.INFO,
                         LogUtil.CONTAINED_NOT_BEFORE,
                         data,
                         null);
                 throw new SAML2Exception(bundle.getString(
                         "containedNotBefore"));
             }
             
             String inRespTo = subjectConfData.getInResponseTo();
             if (inRespTo != null && inRespTo.length() != 0) {
                 if (!inRespTo.equals(inRespToResponse)) {
                     if (debug.messageEnabled()) {
                         debug.message(method + "InResponseTo in Assertion is "
                                 + "different from the one in Response.");
                     }
                     String[] data = {assertionID};
                     LogUtil.error(Level.INFO,
                             LogUtil.WRONG_INRESPONSETO_ASSERTION,
                             data,
                             null);
                     throw new SAML2Exception(bundle.getString(
                             "wrongInResponseToInAssertion"));
                 }
             } else {
                 if (inRespToResponse != null && inRespToResponse.length() != 0){
                     if (debug.messageEnabled()) {
                         debug.message(method + "Assertion doesn't contain "
                                 + "InResponseTo, but Response does.");
                     }
                     String[] data = {assertionID};
                     LogUtil.error(Level.INFO,
                             LogUtil.WRONG_INRESPONSETO_ASSERTION,
                             data,
                             null);
                     throw new SAML2Exception(bundle.getString(
                             "wrongInResponseToInAssertion"));
                 }
             }
             
             hasBearer = true;
             break;
         }
         
         return hasBearer;
     }
     
     private static void checkAudience(Conditions conds,
             String hostEntityId,
             String assertionID)
             throws SAML2Exception {
         String method = "SAML2Utils.checkAudience:";
         if (conds == null) {
             if (debug.messageEnabled()) {
                 debug.message(method + "Conditions is missing from Assertion.");
             }
             String[] data = {assertionID};
             LogUtil.error(Level.INFO,
                     LogUtil.MISSING_CONDITIONS,
                     data,
                     null);
             throw new SAML2Exception(bundle.getString("missingConditions"));
         }
         List restrictions = conds.getAudienceRestrictions();
         if (restrictions == null) {
             if (debug.messageEnabled()) {
                 debug.message(method + "missing AudienceRestriction.");
             }
             String[] data = {assertionID};
             LogUtil.error(Level.INFO,
                     LogUtil.MISSING_AUDIENCE_RESTRICTION,
                     data,
                     null);
             throw new SAML2Exception(bundle.getString(
                     "missingAudienceRestriction"));
         }
         Iterator restIter = restrictions.iterator();
         boolean found = false;
         while (restIter.hasNext()) {
             List audienceList =
                     ((AudienceRestriction) restIter.next()).getAudience();
             if (audienceList.contains(hostEntityId)) {
                 found = true;
                 break;
             }
         }
         if (!found) {
             if (debug.messageEnabled()) {
                 debug.message(method + "This SP is not the intended audience.");
             }
             String[] data = {assertionID};
             LogUtil.error(Level.INFO,
                     LogUtil.WRONG_AUDIENCE,
                     data,
                     null);
             
             throw new SAML2Exception(bundle.getString("audienceNotMatch"));
         }
     }
     
     private static Map fillMap(List authnStmts,
             Subject subject,
             Assertion assertion,
             List assertions,
             AuthnRequestInfo reqInfo,
             String inRespToResp,
             String orgName,
             String hostEntityId,
             String idpEntityId,
             SPSSOConfigElement spConfig)
             throws SAML2Exception {
         // use the first AuthnStmt
         AuthnStatement authnStmt = (AuthnStatement) authnStmts.get(0);
         int authLevel = -1;
         
         String mapperClass = getAttributeValueFromSPSSOConfig(spConfig,
                 SAML2Constants.SP_AUTHCONTEXT_MAPPER);
         SPAuthnContextMapper mapper = getSPAuthnContextMapper(orgName,
                 hostEntityId,mapperClass);
         RequestedAuthnContext reqContext = null;
         if (reqInfo != null) {
             reqContext = (reqInfo.getAuthnRequest()).
                     getRequestedAuthnContext();
         }
         authLevel = mapper.getAuthLevel(reqContext,
                 authnStmt.getAuthnContext(),
                 orgName,
                 hostEntityId,
                 idpEntityId);
         
         String sessionIndex = authnStmt.getSessionIndex();
         Date sessionNotOnOrAfter = authnStmt.getSessionNotOnOrAfter();
         
         Map smap = new HashMap();
         smap.put(SAML2Constants.SUBJECT, subject);
         smap.put(SAML2Constants.POST_ASSERTION, assertion);
         smap.put(SAML2Constants.ASSERTIONS, assertions);
         String[] data = {assertion.getID(), "", ""};
         if (LogUtil.isAccessLoggable(Level.FINE)) {
             data[1] = subject.toXMLString();
         }
         if (sessionIndex != null && sessionIndex.length() != 0) {
             data[2] = sessionIndex;
             smap.put(SAML2Constants.SESSION_INDEX, sessionIndex);
         }
         if (authLevel >= 0) {
             smap.put(SAML2Constants.AUTH_LEVEL, new Integer(authLevel));
         }
         // SessionNotOnOrAfter
         if (sessionNotOnOrAfter != null) {
             long maxSessionTime = (sessionNotOnOrAfter.getTime() -
                     System.currentTimeMillis()) / 60000;
             if (maxSessionTime > 0) {
                 smap.put(SAML2Constants.MAX_SESSION_TIME,
                         new Long(maxSessionTime));
             }
         }
         if (inRespToResp != null && inRespToResp.length() != 0) {
             smap.put(SAML2Constants.IN_RESPONSE_TO, inRespToResp);
         }
         if (debug.messageEnabled()) {
             debug.message("SAML2Utils.fillMap: Found valid authentication "
                     + "assertion.");
         }
         LogUtil.access(Level.INFO,
                 LogUtil.FOUND_AUTHN_ASSERTION,
                 data,
                 null);
         return smap;
     }
     
     /**
      * Retrieves attribute value for a given attribute name from
      * <code>SPSSOConfig</code>.
      * @param config <code>SPSSOConfigElement</code> instance.
      * @param attrName name of the attribute whose value ot be retrived.
      * @return value of the attribute; or <code>null</code> if the attribute
      *          if not configured, or an error occured in the process.
      */
     public static String getAttributeValueFromSPSSOConfig(
             SPSSOConfigElement config,
             String attrName) {
         String result = null;
         if (config == null) {
             return null;
         }
         Map attrs = SAML2MetaUtils.getAttributes(config);
         List value = (List) attrs.get(attrName);
         if (value != null && value.size() != 0) {
             result = ((String) value.iterator().next()).trim();
         }
         return result;
     }
     
     
     /**Gets List of 'String' assertions from the list of 'Assertion' assertions
      * @param assertions A list of Assertions
      * @return a String printout of the list of Assertions
      */
     public static List getStrAssertions(List assertions) {
         List returnAssertions = new ArrayList();
         if (assertions != null) {
             Iterator it = assertions.iterator();
             while (it.hasNext()) {
                 Assertion assertion = (Assertion)it.next();
                 try {
                     returnAssertions.add(assertion.toXMLString(true,true));
                 } catch (SAML2Exception e) {
                     debug.error("Invalid assertion: " + assertion);
                 }
             }
         }
         return returnAssertions;
     }
     
     
     /**
      * Checks if it is a persistent request or not.
      * @param nameId Name ID object
      * @return true if it is a persistent request, false if not.
      */
     public static boolean isPersistentNameID(NameID nameId) {
         boolean isPersistent = false;
         if (nameId == null) {
             return isPersistent;
         }
         String id = nameId.getFormat();
         if (id != null) {
             if (id.equalsIgnoreCase(SAML2Constants.PERSISTENT) ||
                     id.equalsIgnoreCase(SAML2Constants.UNSPECIFIED)) {
                 isPersistent = true;
             }
         }
         if (debug.messageEnabled()) {
             debug.message("SAML2Utils:isPersistent : " + isPersistent);
         }
         return isPersistent;
     }
     
     /**
      * Checks if the federation information for the user exists or not.
      * @param userName user id for which account federation needs to be
      *        returned.
      * @param hostEntityID <code>EntityID</code> of the hosted entity.
      * @param remoteEntityId <code>EntityID</code> of the remote entity.
      * @return true if exists, false otherwise.
      */
     public static boolean isFedInfoExists(String userName, String hostEntityID,
             String remoteEntityId, NameID nameID) {
         boolean exists = false;
         if ((userName == null) || (hostEntityID == null) ||
                 (remoteEntityId == null) || (nameID == null)) {
             return exists;
         }
         try {
             NameIDInfo info = AccountUtils.getAccountFederation(
                     userName,hostEntityID, remoteEntityId);
             
             if(info != null &&
                     info.getNameIDValue().equals(nameID.getValue())) {
                 exists = true;
             }
         } catch (SAML2Exception se) {
             debug.error("Failed to get DataStoreProvider " + se.toString());
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils:isFedInfoExists:Stack : ", se);
             }
         } catch (Exception e) {
             debug.message("SAML2Utils:isFedInfoExists: Exception : ", e);
         }
         
         if (debug.messageEnabled()) {
             debug.message("SAML2Utils:isFedInfoExists : " + exists);
         }
         
         return exists;
     }
     
     /**
      * Returns <code>true</code> if <code>Issuer</code> is valid.
      *
      * @param issuer to be checked <code>Issuer</code> instance.
      * @param orgName the name of the realm or organization.
      * @param hostEntityId Entity ID of the hosted provider.
      * @return <code>true</code> if the <code>Issuer</code> is trusted;
      *          <code>false</code> otherwise.
      */
     public static boolean isSourceSiteValid(
         Issuer issuer,
         String orgName,
         String hostEntityId
     ) {
         boolean isValid = false;
         try {
             if (issuer != null) {
                 String entityID = issuer.getValue().trim();
                 if (entityID != null && entityID.length() != 0) {
                     // Check if entityID is trusted provider
                     isValid = saml2MetaManager.isTrustedProvider(
                             orgName, hostEntityId, entityID);
                 }
             }
             return isValid;
         } catch (Exception e) {
             debug.error("SAML2Utils.isSourceSiteValid: " +
                     "Exception : ", e);
             return false;
         }
     }
     
     
     /**
      * Returns <code>DataStoreProvider</code> object.
      * @return <code>DataStoreProvider</code> configured for the SAML2 plugin.
      * @exception SAML2Exception if any failure.
      */
     public static DataStoreProvider getDataStoreProvider()
     throws SAML2Exception {
         try {
             DataStoreProviderManager dsManager =
                     DataStoreProviderManager.getInstance();
             return dsManager.getDataStoreProvider(SAML2Constants.SAML2);
         } catch (DataStoreProviderException dse) {
             debug.error("SAML2Utils.getDataStoreProvider: " +
                     "DataStoreProviderException : ", dse);
             throw new SAML2Exception(dse);
         }
     }
     
     /**
      * Returns the encoded request message.
      * The SAML Request message must be
      * encoded before being transmitted.
      * The Request message is encoded as follows:
      * 1. URL Encoded using the DEFLATE compression method.
      * 2. Then the message is base-64 encoded according to
      *    the rules specified in RFC2045.
      * @param str String to be encoded.
      * @return String the encoded String value or null on error.
      */
     public static String encodeForRedirect(String str) {
         String classMethod = "encodeForRedirect";
         int n = str.length();
         byte[] input = null;
         try {
             input = str.getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee) {
             debug.error(
                     "SAML2Utils.encodeForRedirect: cannot get byte array: ",
                     uee);
             return null;
         }
         byte[] output = new byte[n];
         
         Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
         deflater.setInput(input);
         deflater.finish();
         int len = deflater.deflate(output);
         
         byte[] exact = new byte[len];
         
         System.arraycopy(output, 0, exact, 0, len);
         
         String base64Str = Base64.encode(exact);
         
         String encoded = URLEncDec.encode(base64Str);
         if (debug.messageEnabled()) {
             debug.message(classMethod+
                     "out string length : " + encoded.length());
             debug.message(classMethod+
                     "out string is ===>"+encoded+"<===");
         }
         
         return encoded;
     }
     
     /**
      * Decodes the request message.
      *
      * @param str String to be decoded.
      * @return String the decoded String.
      */
     public static String decodeFromRedirect(String str) {
         
         final String classMethod = "SAML2Utils.decodeFromRedirect: ";
         
         if (str==null || str.length()==0) {
             debug.error(classMethod+"input is null.");
             return null;
         }
         if (debug.messageEnabled()) {
             debug.message(classMethod+
                     "input string length : "+str.length());
             debug.message(classMethod+
                     "input string is ===>"+str+"<===");
         }
         byte[] input = Base64.decode(removeNewLineChars(str));
         if (input==null || input.length==0) {
             debug.error(classMethod+
                     "base 64 decoded result is null");
             return null;
         }
         // Decompress the bytes
         Inflater inflater = new Inflater(true);
         inflater.setInput(input);
         byte[] result = new byte[2048]; // Note that this is fixed length
         // buffer, could be a problem
         int resultLength = 0;
         try {
             resultLength = inflater.inflate(result);
         } catch (DataFormatException dfe) {
             debug.error(classMethod+
                     "cannot inflate SAMLRequest: ",
                     dfe);
             return null;
         }
         inflater.end();
         
         // Decode the bytes into a String
         String outputString = null;
         try {
             outputString = new
                     String(result, 0, resultLength, "UTF-8");
         } catch (UnsupportedEncodingException uee) {
             debug.error(classMethod+
                     "cannot convert byte array to string.",
                     uee);
             return null;
         }
         if (debug.messageEnabled()) {
             debug.message(classMethod+
                     "Return value: \n"+outputString);
         }
         return outputString;
     }
     
     /**
      * Removes new line character from a String.
      *
      * @param s String to remove newline characters from.
      * @return String with newline characters trimmed.
      */
     public static String removeNewLineChars(String s) {
         String retString = null;
         if ((s != null) && (s.length() > 0) && (s.indexOf('\n') != -1)) {
             char[] chars = s.toCharArray();
             int len = chars.length;
             StringBuffer sb = new StringBuffer(len);
             for (int i = 0; i < len; i++) {
                 char c = chars[i];
                 if (c != '\n') {
                     sb.append(c);
                 }
             }
             retString = sb.toString();
         } else {
             retString = s;
         }
         return retString;
     }
     
     /**
      * Returns an instance of <code>SAML2MetaManger</code>.
      *
      * @return Instance of <code>SAML2MetaManager</code>
      */
     public static SAML2MetaManager getSAML2MetaManager() {
         return saml2MetaManager;
     }
     
     /**
      * Returns the realm.
      * @param realm Realm object.
      * @return realm if the input is not null or empty, otherwise
      *         return the root realm.
      */
     public static String getRealm(String realm) {
         return ((realm == null) || (realm.length() == 0)) ?
             "/" : realm;
     }
     
     /**
      * Returns the realm
      * @param paramsMap a map of parameters
      * @return realm if the input map contains the realm, otherwise
      *         return the default realm from AMConfig.properties
      */
     public static String getRealm(Map paramsMap) {
         String realm = getParameter(paramsMap,SAML2Constants.REALM);
         return ((realm == null) || (realm.length() == 0)) ?
             "/" : realm;
     }
     
     /**
      * Returns the query parameter value for the param specified
      * @param paramsMap a map of parameters
      * @param attrName name of the parameter
      * @return the value of this parameter
      */
     public static String getParameter(Map paramsMap, String attrName) {
         String attrVal = null;
         if ((paramsMap != null) && (!paramsMap.isEmpty())) {
             String attr = (String)paramsMap.get(attrName);
             if (attr != null) {
                 attrVal = attr;
             }
         }
         return attrVal;
     }
     
     /**
      * Returns a Map of paramters retrieved from the Query parameters
      * in the HttpServletRequest.
      *
      * @param request the <code>HttpServletRequest</code>.
      * @return a Map where the key is the parameter Name and
      *         value is of the type List.
      */
     public static Map getParamsMap(HttpServletRequest request) {
         Map paramsMap = new HashMap();
         String relayState = getRelayState(request);
         if (relayState != null) {
             List list = new ArrayList();
             list.add(relayState);
             paramsMap.put(SAML2Constants.RELAY_STATE,list);
         }       
         String isPassive = request.getParameter(SAML2Constants.ISPASSIVE);
         if (isPassive != null)  {
             List list = new ArrayList();
             list.add(isPassive);
             paramsMap.put(SAML2Constants.ISPASSIVE,list);
         }
         
         String forceAuthN = request.getParameter(SAML2Constants.FORCEAUTHN);
         if (forceAuthN != null) {
             List list = new ArrayList();
             list.add(forceAuthN);
             paramsMap.put(SAML2Constants.FORCEAUTHN,list);
         }
         
         String allowCreate = request.getParameter(SAML2Constants.ALLOWCREATE);
         if (allowCreate != null) {
             List list = new ArrayList();
             list.add(allowCreate);
             paramsMap.put(SAML2Constants.ALLOWCREATE,list);
         }
         
         String consent = request.getParameter(SAML2Constants.CONSENT);
         if (consent != null) {
             List list = new ArrayList();
             list.add(consent);
             paramsMap.put(SAML2Constants.CONSENT,list);
         }
         
         String destination = request.getParameter(SAML2Constants.DESTINATION);
         if (destination != null) {
             List list = new ArrayList();
             list.add(destination);
             paramsMap.put(SAML2Constants.DESTINATION,list);
         }
         
         String nameIDPolicy =
                 request.getParameter(SAML2Constants.NAMEID_POLICY_FORMAT);
         if (nameIDPolicy != null) {
             List list = new ArrayList();
             list.add(nameIDPolicy);
             paramsMap.put(SAML2Constants.NAMEID_POLICY_FORMAT,list);
         }
         
         String binding = request.getParameter(SAML2Constants.BINDING);
         if (binding != null) {
             List list = new ArrayList();
             list.add(binding);
             paramsMap.put(SAML2Constants.BINDING,list);
         }
         
         String acsUrlIndex = request.getParameter(SAML2Constants.ACS_URL_INDEX);
         if (acsUrlIndex != null) {
             List list = new ArrayList();
             list.add(acsUrlIndex);
             paramsMap.put(SAML2Constants.ACS_URL_INDEX,list);
         }
         
         String attrIndex = request.getParameter(SAML2Constants.ATTR_INDEX);
         if (attrIndex != null) {
             List list = new ArrayList();
             list.add(attrIndex);
             paramsMap.put(SAML2Constants.ATTR_INDEX,list);
         }
         
         String authComparison= request.getParameter(
                 SAML2Constants.SP_AUTHCONTEXT_COMPARISON);
         if (authComparison != null) {
             List list = new ArrayList();
             list.add(authComparison);
             paramsMap.put(SAML2Constants.SP_AUTHCONTEXT_COMPARISON,list);
         }
         
         String authContextDeclRef
                 = request.getParameter(SAML2Constants.AUTH_CONTEXT_DECL_REF);
         if ((authContextDeclRef != null) && authContextDeclRef.length() > 0) {
             List authDeclList = getAuthContextList(authContextDeclRef);
             paramsMap.put(SAML2Constants.AUTH_CONTEXT_DECL_REF,authDeclList);
         }
         
         String authContextClassRef
                 = request.getParameter(SAML2Constants.AUTH_CONTEXT_CLASS_REF);
         if (authContextClassRef != null) {
             List authClassRefList = getAuthContextList(authContextClassRef);
             paramsMap.put(SAML2Constants.AUTH_CONTEXT_CLASS_REF,
                     authClassRefList);
         }
         
         String authLevel = request.getParameter(SAML2Constants.AUTH_LEVEL);
         if (authLevel != null && authLevel.length() > 0) {
             List list = new ArrayList();
             list.add(authLevel);
             paramsMap.put(SAML2Constants.AUTH_LEVEL,list);
         }
         return paramsMap;
     }
     
     
     /**
      * Returns the Authcontext declare/class references
      * as a List. The string passed to this method
      * is a pipe separated value.
      *
      * @param str a pipe separated String to be parsed.
      * @return List contained the parsed values.
      */
     private static List getAuthContextList(String str) {
         List ctxList = new ArrayList();
         StringTokenizer st = new StringTokenizer(str,"|");
         while (st.hasMoreTokens()) {
             String tmp = (String) st.nextToken();
             ctxList.add(tmp);
         }
         return ctxList;
     }
     
     /**
      * Generates provider Source ID based on provider Entity ID. The returned
      * is SHA-1 digest string.
      * @param entityID Entity ID for example <code>http://host.sun.com:81</code>
      * @return sourceID string
      */
     public static String generateSourceID(String entityID) {
         if ((entityID == null) || (entityID.length() == 0)) {
             return null;
         }
         MessageDigest md = null;
         try {
             md = MessageDigest.getInstance("SHA");
         } catch (NoSuchAlgorithmException e) {
             debug.error("SAML2Utils.generateSourceID: ", e);
             return null;
         }
         char chars[] = entityID.toCharArray();
         byte bytes[] = new byte[chars.length];
         for (int i = 0; i < chars.length; i++) {
             bytes[i] = (byte) chars[i];
         }
         md.update(bytes);
         return SAML2Utils.byteArrayToString(md.digest());
     }
     
     /**
      * Gets remote service URL according to server id embedded in specified id.
      * @param id an id.
      * @return remote service URL or null if it is local or an error occurred.
      */
     public static String getRemoteServiceURL(String id) {
         if (debug.messageEnabled()) {
             debug.message("SAML2Utils.getRemoteServiceURL: id = " + id);
         }
         
         if (id == null || id.length() < 2) {
             return null;
         }
         
         String serverID = id.substring(id.length() - 2);
         
         try {
             String localServerID = SystemConfigurationUtil.getServerID(
                 server_protocol, server_host, int_server_port);
             if (serverID.equals(localServerID)) {
                 return null;
             }
             
             return SystemConfigurationUtil.getServerFromID(serverID);
         } catch (Exception ex) {
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.getRemoteServiceURL:", ex);
             }
             return null;
         }
     }
     
     /**
      * Gets remote service URLs
      * @param request http request
      * @return remote service URLs
      */
     public static List getRemoteServiceURLs(HttpServletRequest request) {
         String requestURL = request.getScheme() + "://" +
                 request.getServerName() + ":" +
                 request.getServerPort();
         if (debug.messageEnabled()) {
             debug.message("SAML2Utils.getRemoteServiceURLs: requestURL = " +
                     requestURL);
         }
         
         List serverList = null;
         
         try {
             serverList = SystemConfigurationUtil.getServerList();
         } catch (Exception ex) {
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.getRemoteServiceURLs:", ex);
             }
         }
         if (serverList == null) {
             return null;
         }
         
         List remoteServiceURLs = new ArrayList();
         for(Iterator iter = serverList.iterator(); iter.hasNext();) {
             String serviceURL = (String)iter.next();
             if ((!serviceURL.equalsIgnoreCase(requestURL)) &&
                     (!serviceURL.equalsIgnoreCase(localURL))) {
                 remoteServiceURLs.add(serviceURL);
             }
         }
         
         if (debug.messageEnabled()) {
             debug.message("SAML2Utils.getRemoteServiceURLs: " +
                     "remoteServiceURLs = " + remoteServiceURLs);
         }
         return remoteServiceURLs;
     }
     
     /**
      * Generates ID with server id at the end.
      * @return ID value.
      */
     public static String generateIDWithServerID() {
         if (random == null) {
             return null;
         }
         byte bytes[] = new byte[SAML2Constants.ID_LENGTH];
         random.nextBytes(bytes);
         String id = SAML2ID_PREFIX + byteArrayToHexString(bytes);
         
         return embedServerID(id);
     }
     
     /**
      * Generates message handle with server id used in an <code>Artifact</code>.
      *
      * @return String format of 20-byte sequence identifying message.
      */
     public static String generateMessageHandleWithServerID() {
         if (random == null) {
             return null;
         }
         byte bytes[] = new byte[SAML2Constants.ID_LENGTH];
         random.nextBytes(bytes);
         String id = byteArrayToString(bytes);
         
         return embedServerID(id);
     }
     
     
     /**
      * Replaces last 2 chars of specified id with server id.
      * @param id an id
      * @return String with server id at the end.
      */
     private static String embedServerID(String id) {
         String serverId = null;
         try {
             serverId = SystemConfigurationUtil.getServerID(
                 server_protocol, server_host, int_server_port);
             
             // serverId is 2 digit string
             if (serverId != null && serverId.length() == 2) {
                 id = id.substring(0, id.length() -2) + serverId;
             } else if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.appendServerID: " +
                         "invalid server id = " + serverId);
             }
         } catch (Exception ex) {
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.appendServerID:", ex);
             }
         }
         
         return id;
     }
     
     /**
      * Creates <code>SOAPMessage</code> with the input XML String
      * as message body.
      * @param xmlString XML string to be put into <code>SOAPMessage</code> body.
      * @return newly created <code>SOAPMessage</code>.
      * @exception SOAPException if it cannot create the
      *            <code>SOAPMessage</code>.
      */
     public static SOAPMessage createSOAPMessage(String xmlString)
     throws SOAPException, SAML2Exception {
         SOAPMessage msg = null;
         try {
             MimeHeaders mimeHeaders = new MimeHeaders();
             mimeHeaders.addHeader("Content-Type", "text/xml");
             
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.createSOAPMessage: xmlstr = " +
                         xmlString);
             }
             
             StringBuffer sb = new StringBuffer(500);
             sb.append("<").append(SAMLConstants.SOAP_ENV_PREFIX).
                     append(":Envelope").append(SAMLConstants.SPACE).
                     append("xmlns:").append(SAMLConstants.SOAP_ENV_PREFIX).
                     append("=\"").append(SAMLConstants.SOAP_URI).append("\">").
                     append("<").
                     append(SAMLConstants.SOAP_ENV_PREFIX).append(":Body>").
                     append(xmlString).
                     append(SAMLConstants.START_END_ELEMENT).
                     append(SAMLConstants.SOAP_ENV_PREFIX).
                     append(":Body>").
                     append(SAMLConstants.START_END_ELEMENT).
                     append(SAMLConstants.SOAP_ENV_PREFIX).
                     append(":Envelope>").append(SAMLConstants.NL);
             
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.createSOAPMessage: soap message = " +
                         sb.toString());
             }
             
             msg = mf.createMessage(mimeHeaders, new ByteArrayInputStream(
                     sb.toString().getBytes(SAML2Constants.DEFAULT_ENCODING)));
         } catch (IOException io) {
             debug.error("SAML2Utils.createSOAPMessage: IOE", io);
             throw new SAML2Exception(io.getMessage());
         }
         return msg;
     }
     
     /**
      * Returns SOAP body as DOM Element from SOAPMessage.
      * @param message SOAPMessage object.
      * @return SOAP body, return null if unable to get the SOAP body element.
      */
     public static Element getSOAPBody(SOAPMessage message)
     throws SAML2Exception {
         debug.message("SAML2Utils.getSOAPBody : start");
         
         // check the SOAP message for any SOAP
         // related errros before passing control to SAML processor
         ByteArrayOutputStream bop = new ByteArrayOutputStream();
         try {
             message.writeTo(bop);
         } catch (IOException ie) {
             debug.error("SAML2Utils.getSOAPBody : writeTo IO", ie);
             throw new SAML2Exception(ie.getMessage());
         } catch (SOAPException se) {
             debug.error("SAML2Utils.getSOAPBody : writeTo SOAP", se);
             throw new SAML2Exception(se.getMessage());
         }
         ByteArrayInputStream bin =
                 new ByteArrayInputStream(bop.toByteArray());
         Document doc = XMLUtils.toDOMDocument(bin, debug);
         Element root= doc.getDocumentElement();
         if (SAML2Utils.debug.messageEnabled()) {
             SAML2Utils.debug.message("LogoutUtil.getSOAPBody : soap body =\n"
                     + XMLUtils.print((Node) root));
         }
         String rootName  = doc.getDocumentElement().getLocalName();
         if ((rootName == null) || (rootName.length() == 0)) {
             debug.error("SAML2Utils.getSOAPBody : no local name");
             throw new SAML2Exception(SAML2Utils.bundle.getString(
                     "missingLocalName"));
         }
         if (!(rootName.equals("Envelope")) ||
                 (!(SAMLConstants.SOAP_URI.equals(root.getNamespaceURI())))) {
             debug.error("SAML2Utils.getSOAPBody : either root " +
                     "element is not Envelope or invalid name space or prefix");
             throw new SAML2Exception(SAML2Utils.bundle.getString(
                     "invalidSOAPElement"));
         }
         NodeList nl = root.getChildNodes();
         int length = nl.getLength();
         if (length <= 0 ) {
             debug.error("SAML2Utils.getSOAPBody: no msg body");
             throw new SAML2Exception(SAML2Utils.bundle.getString(
                     "missingSOAPBody"));
         }
         for (int i = 0; i < length; i++) {
             Node child = (Node)nl.item(i);
             if (child.getNodeType() != Node.ELEMENT_NODE) {
                 debug.message("SAML2Utils.getSOAPBody: " + child);
                 continue;
             }
             String childName = child.getLocalName();
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.getSOAPBody: local name= "
                         + childName);
             }
             if (childName.equals("Body") &&
                     SAMLConstants.SOAP_URI.equals(child.getNamespaceURI())) {
                 // found the Body element
                 return (Element) child;
             }
         }
         throw new SAML2Exception(SAML2Utils.bundle.getString(
                 "missingSOAPBody"));
     }
     
     /**
      * Returns mime headers in HTTP servlet request.
      * @param req HTTP servlet request.
      * @return mime headers in HTTP servlet request.
      */
     public static MimeHeaders getHeaders(HttpServletRequest req) {
         Enumeration e = req.getHeaderNames();
         MimeHeaders headers = new MimeHeaders();
         while (e.hasMoreElements()) {
             String headerName = (String)e.nextElement();
             String headerValue = req.getHeader(headerName);
             if (debug.messageEnabled()) {
                 debug.message("SAML2Util.getHeaders: Header name=" +
                         headerName + ", value=" + headerValue);
             }
             StringTokenizer values =
                     new StringTokenizer(headerValue, ",");
             while (values.hasMoreTokens()) {
                 headers.addHeader(
                         headerName, values.nextToken().trim());
             }
         }
         
         if (debug.messageEnabled()) {
             debug.message("SAML2Util.getHeaders: Header=" + headers.toString());
         }
         return headers;
     }
     
     /**
      * Sets mime headers in HTTP servlet response.
      * @param headers mime headers to be set.
      * @param res HTTP servlet response.
      */
     public static void putHeaders(
             MimeHeaders headers, HttpServletResponse res) {
         if (debug.messageEnabled()) {
             debug.message("SAML2Util.putHeaders: Header=" + headers.toString());
         }
         Iterator it = headers.getAllHeaders();
         while (it.hasNext()) {
             MimeHeader header = (MimeHeader)it.next();
             String[] values = headers.getHeader(header.getName());
             if (debug.messageEnabled()) {
                 debug.message("SAML2Util.putHeaders: Header name=" +
                         header.getName() + ", value=" + values);
             }
             if (values.length == 1) {
                 res.setHeader(header.getName(), header.getValue());
             } else {
                 StringBuffer concat = new StringBuffer();
                 int i = 0;
                 while (i < values.length) {
                     if (i != 0) {
                         concat.append(',');
                     }
                     concat.append(values[i++]);
                 }
                 res.setHeader(header.getName(), concat.toString());
             }
         }
     }
     
     /**
      * Generates SAMLv2 Status object
      * @param code Status code value.
      * @param message Status message.
      * @return Status object.
      */
     public static Status generateStatus(String code, String message) {
         Status status = null;
         try {
             status = ProtocolFactory.getInstance().createStatus();
             StatusCode statuscode = ProtocolFactory.getInstance()
             .createStatusCode();
             statuscode.setValue(code);
             status.setStatusCode(statuscode);
             if (message != null && message.length() != 0) {
                 status.setStatusMessage(message);
             }
             if (debug.messageEnabled()) {
                 debug.message("SAML2Util.geberateStatus : "
                         + status.toXMLString());
             }
         } catch (SAML2Exception e) {
             debug.error("Exeption : ", e);
         }
         return status;
     }
     
     /**
      * Returns first Element with given local name in samlp name space inside
      * SOAP message.
      * @param message SOAP message.
      * @param localName local name of the Element to be returned.
      * @return first Element matching the local name.
      * @throws SAML2Exception if the Element could not be found or there is
      * SOAP Fault present.
      */
     public static Element getSamlpElement(
             SOAPMessage message, String localName) throws SAML2Exception {
         
         Element body = getSOAPBody(message);
         NodeList nlBody = body.getChildNodes();
         
         int blength = nlBody.getLength();
         if (blength <= 0) {
             debug.error("SAML2Utils.getSamlpElement: empty body");
             throw new SAML2Exception(bundle.getString("missingBody"));
         }
         Element retElem = null;
         Node node = null;
         for (int i = 0; i < blength; i++) {
             node = (Node) nlBody.item(i);
             if(node.getNodeType() != Node.ELEMENT_NODE) {
                 continue;
             }
             String nlName = node.getLocalName();
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils.getSamlpElement: node=" +
                         nlName + ", nsURI=" + node.getNamespaceURI());
             }
             if (nlName.equals("Fault")) {
                 throw new SAML2Exception(SAML2Utils.bundle.getString(
                         "soapFaultInSOAPResponse"));
             } else if (nlName.equals(localName) &&
                     SAML2Constants.PROTOCOL_NAMESPACE.equals(
                     node.getNamespaceURI())){
                 retElem = (Element) node;
                 break;
             }
         }
         if (retElem == null) {
             throw new SAML2Exception(bundle.getString("elementNotFound") +
                     localName);
         }
         return retElem;
     }
     
     /**
      * Forms a SOAP Fault and puts it in the SOAP Message Body.
      * @param faultCode Fault code.
      * @param faultString Fault string.
      * @param detail Fault details.
      * @return SOAP Fault in the SOAP Message Body.
      */
     public static SOAPMessage createSOAPFault(
             String faultCode, String faultString, String detail) {
         SOAPMessage msg = null ;
         SOAPEnvelope envelope = null;
         SOAPFault sf = null;
         SOAPBody body = null;
         SOAPElement se = null;
         try {
             msg = mf.createMessage();
             envelope = msg.getSOAPPart().getEnvelope();
             body = envelope.getBody();
             sf = body.addFault();
             sf.setFaultCode(faultCode);
             sf.setFaultString(SAML2Utils.bundle.getString(faultString));
             if ((detail != null) && !(detail.length() == 0)) {
                 Detail det = sf.addDetail();
                 se = (SOAPElement)det.addDetailEntry(envelope.createName(
                         "Problem"));
                 se.addAttribute(envelope.createName("details"),
                         SAML2Utils.bundle.getString(detail));
             }
         } catch (SOAPException e) {
             debug.error("createSOAPFault:", e);
         }
         return msg;
     }
     
     /**
      * Returns SOAP Message from <code>HttpServletRequest</code>.
      * @param request <code>HttpServletRequest</code> includes SOAP Message.
      * @return SOAPMessage if request include any soap message in the header.
      * @throws IOException if error in creating input stream.
      * @throws SOAPException if error in creating soap message.
      */
     public static SOAPMessage getSOAPMessage(HttpServletRequest request)
     throws IOException, SOAPException {
         // Get all the headers from the HTTP request
         MimeHeaders headers = getHeaders(request);
         // Get the body of the HTTP request
         InputStream is = request.getInputStream();
         
         // Create a SOAPMessage
         return mf.createMessage(headers, is);
     }
     
     /**
      * Send SOAP Message to specified url and returns message from peer.
      * @param xmlMessage <code>String</code> will be sent.
      * @param soapUrl URL the mesaage send to.
      * @return SOAPMessage if the peer send back any reply.
      * @throws SOAPException if error in creating soap message.
      * @throws SAML2Exception if error in creating soap message.
      */
     public static SOAPMessage sendSOAPMessage(String xmlMessage, String soapUrl)
     throws SOAPException, SAML2Exception {
         SOAPConnection con = scf.createConnection();
         SOAPMessage msg = createSOAPMessage(xmlMessage);
         return con.call(msg, soapUrl);
     }
     
     /**
      * Returns encryption certificate alias name.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return alias name of certificate alias for encryption.
      */
     public static String getEncryptionCertAlias(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getEncryptionCertAlias : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         return getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.ENCRYPTION_CERT_ALIAS);
     }
     
     /**
      * Returns signing certificate alias name.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return alias name of certificate alias for signing.
      */
     public static String getSigningCertAlias(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getSigningCertAlias : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         return getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.SIGNING_CERT_ALIAS);
     }
     
     /**
      * Returns true if wantAssertionEncrypted has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantAssertionEncrypted has <code>String</code> true.
      */
     public static boolean getWantAssertionEncrypted(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantAssertionEncrypted : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantEncrypted =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_ASSERTION_ENCRYPTED);
         if (wantEncrypted == null) {
             wantEncrypted = "false";
         }
         
         return wantEncrypted.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantAttributeEncrypted has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantAttributeEncrypted has <code>String</code> true.
      */
     public static boolean getWantAttributeEncrypted(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantAttributeEncrypted : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantEncrypted =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_ATTRIBUTE_ENCRYPTED);
         if (wantEncrypted == null) {
             wantEncrypted = "false";
         }
         
         return wantEncrypted.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantNameIDEncrypted has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantNameIDEncrypted has <code>String</code> true.
      */
     public static boolean getWantNameIDEncrypted(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantNameIDEncrypted : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantEncrypted =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_NAMEID_ENCRYPTED);
         if (wantEncrypted == null) {
             wantEncrypted = "false";
         }
         
         return wantEncrypted.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantArtifactResolveSigned has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantArtifactResolveSigned has <code>String</code> true.
      */
     public static boolean getWantArtifactResolveSigned(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantArtifactResolveSigned : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantSigned =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_ARTIFACT_RESOLVE_SIGNED);
         if (wantSigned == null) {
             wantSigned = "false";
         }
         
         return wantSigned.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantArtifactResponseSigned has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantArtifactResponseSigned has <code>String</code> true.
      */
     public static boolean getWantArtifactResponseSigned(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantArtifactResponseSigned : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantSigned =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_ARTIFACT_RESPONSE_SIGNED);
         if (wantSigned == null) {
             wantSigned = "false";
         }
         
         return wantSigned.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantLogoutRequestSigned has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantLogoutRequestSigned has <code>String</code> true.
      */
     public static boolean getWantLogoutRequestSigned(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantLogoutRequestSigned : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantSigned =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_LOGOUT_REQUEST_SIGNED);
         if (wantSigned == null) {
             wantSigned = "false";
         }
         
         return wantSigned.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantLogoutResponseSigned has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantLogoutResponseSigned has <code>String</code> true.
      */
     public static boolean getWantLogoutResponseSigned(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantLogoutResponseSigned : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantSigned =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_LOGOUT_RESPONSE_SIGNED);
         if (wantSigned == null) {
             wantSigned = "false";
         }
         
         return wantSigned.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantMNIRequestSigned has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantMNIRequestSigned has <code>String</code> true.
      */
     public static boolean getWantMNIRequestSigned(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantMNIRequestSigned : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantSigned =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_MNI_REQUEST_SIGNED);
         if (wantSigned == null) {
             wantSigned = "false";
         }
         
         return wantSigned.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns true if wantMNIResponseSigned has <code>String</code> true.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @return true if wantMNIResponseSigned has <code>String</code> true.
      */
     public static boolean getWantMNIResponseSigned(String realm,
             String hostEntityId,
             String entityRole) {
         if (debug.messageEnabled()) {
             String method = "getWantMNIResponseSigned : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
         }
         String wantSigned =
                 getAttributeValueFromSSOConfig(realm, hostEntityId, entityRole,
                 SAML2Constants.WANT_MNI_RESPONSE_SIGNED);
         if (wantSigned == null) {
             wantSigned = "false";
         }
         
         return wantSigned.equalsIgnoreCase("true") ? true : false;
     }
     
     /**
      * Returns a value of specified attribute from SSOConfig.
      * @param realm realm of hosted entity.
      * @param hostEntityId name of hosted entity.
      * @param entityRole role of hosted entity.
      * @param attrName attribute name for the value.
      * @return value of specified attribute from SSOConfig.
      */
     public static String getAttributeValueFromSSOConfig(String realm,
             String hostEntityId,
             String entityRole,
             String attrName) {
         if (debug.messageEnabled()) {
             String method = "getAttributeValueFromSSOConfig : ";
             debug.message(method + "realm - " + realm);
             debug.message(method + "hostEntityId - " + hostEntityId);
             debug.message(method + "entityRole - " + entityRole);
             debug.message(method + "attrName - " + attrName);
         }
         String result = null;
         try {
             IDPSSOConfigElement idpConfig = null;
             SPSSOConfigElement spConfig = null;
             Map attrs = null;
             
             if (entityRole.equalsIgnoreCase(SAML2Constants.SP_ROLE)) {
                 spConfig =
                         saml2MetaManager.getSPSSOConfig(realm, hostEntityId);
                 if (spConfig == null) {
                     return null;
                 }
                 attrs = SAML2MetaUtils.getAttributes(spConfig);
             } else {
                 idpConfig =
                         saml2MetaManager.getIDPSSOConfig(realm, hostEntityId);
                 if (idpConfig == null) {
                     return null;
                 }
                 attrs = SAML2MetaUtils.getAttributes(idpConfig);
             }
             
             if (attrs == null) {
                 return null;
             }
             List value = (List) attrs.get(attrName);
             if (value != null && value.size() != 0) {
                 result = (String) value.get(0);
             }
         } catch (SAML2MetaException e) {
             debug.message("get SSOConfig failed:", e);
         }
         return result;
     }
     
     /**
      * Returns the role of host entity.
      * @param paramsMap <code>Map</code> includes parameters.
      * @return role name for hosted entity.
      * @throws SAML2Exception if error in retrieving the parameters.
      */
     public static String getHostEntityRole(Map paramsMap)
     throws SAML2Exception {
         String roleName = getParameter(paramsMap,SAML2Constants.ROLE);
         if (roleName.equalsIgnoreCase(SAML2Constants.SP_ROLE) ||
                 roleName.equalsIgnoreCase(SAML2Constants.IDP_ROLE)) {
             return roleName;
         }
         
         throw new SAML2Exception(
                 SAML2Utils.bundle.getString("unknownHostEntityRole"));
     }
     
     /**
      * Returns url for redirection.
      * @param request <code>HttpServletRequest</code> for redirecting.
      * @param response <code>HttpServletResponse</code> for redirecting.
      * @param realm realm of hosted entity.
      * @param hostEntityID name of hosted entity.
      * @param entityRole role of hosted entity.
      * @throws IOException if error in redirecting request.
      */
     public static void redirectAuthentication(
             HttpServletRequest request,
             HttpServletResponse response,
             String realm,
             String hostEntityID,
             String entityRole) throws IOException {
         String method = "redirectAuthentication: ";
         // get the authentication service url
         String authUrl = SAML2Utils.getAttributeValueFromSSOConfig(
                 realm, hostEntityID, entityRole,
                 SAML2Constants.AUTH_URL);
         if (authUrl == null) {
             // need to get it from the request
             String uri = request.getRequestURI();
             String deploymentURI = uri;
             int firstSlashIndex = uri.indexOf("/");
             int secondSlashIndex = uri.indexOf("/", firstSlashIndex+1);
             if (secondSlashIndex != -1) {
                 deploymentURI = uri.substring(0, secondSlashIndex);
             }
             StringBuffer sb = new StringBuffer();
             sb.append(request.getScheme()).append("://")
             .append(request.getServerName()).append(":")
             .append(request.getServerPort())
             .append(deploymentURI)
             .append("/UI/Login?realm=").append(realm);
             authUrl = sb.toString();
         }
         
         if (authUrl.indexOf("?") == -1) {
             authUrl += "?goto=";
         } else {
             authUrl += "&goto=";
         }
         
         authUrl += URLEncDec.encode(request.getRequestURL().toString()
         + "?" + request.getQueryString());
         if (debug.messageEnabled()) {
             debug.message(method + "New URL for authentication: " + authUrl);
         }
         
         FSUtils.forwardRequest(request, response, authUrl) ;
     }
     
     /**
      * Returns url for redirection.
      * @param entityID entityID for Issuer.
      * @return Issuer for the specified entityID.
      * @throws SAML2Exception if error in creating Issuer element.
      */
     public static Issuer createIssuer(String entityID)
     throws SAML2Exception {
         String method = "createIssuer: ";
         Issuer issuer = af.createIssuer();
         issuer.setValue(entityID);
         if (debug.messageEnabled()) {
             debug.message(method + "Issuer : " + issuer.toXMLString());
         }
         return issuer;
     }
     
     /**
      * Fills in basic auth user and password inside the location URL
      * if configuration is done properly
      * @param config Either an SPSSOConfigElement object or an
      *               IDPSSOConfigElement object.
      * @param locationURL The original location URL which is to be
      *                    inserted with user:password@ before the
      *                    hostname part and after //
      * @return The modified location URL with the basic auth user
      *         and password if configured properly
      */
     public static String fillInBasicAuthInfo(
             BaseConfigType config,
             String locationURL) {
         
         if (config == null) {
             return locationURL;
         }
         Map map = SAML2MetaUtils.getAttributes(config);
         List baoList = (List)map.get(
                 SAML2Constants.BASIC_AUTH_ON);
         if (baoList == null || baoList.isEmpty()) {
             return locationURL;
         }
         String on = (String)baoList.get(0);
         if (on == null) {
             return locationURL;
         }
         on = on.trim();
         if (on.length() == 0 || !on.equalsIgnoreCase("true")) {
             return locationURL;
         }
         List ul =  (List)map.get(
                 SAML2Constants.BASIC_AUTH_USER);
         if (ul == null || ul.isEmpty()) {
             return locationURL;
         }
         String u = (String) ul.get(0);
         if (u == null) {
             return locationURL;
         }
         u = u.trim();
         if (u.length() == 0) {
             return locationURL;
         }
         List pl = (List)map.get(
                 SAML2Constants.BASIC_AUTH_PASSWD);
         String p = null;
         if (pl != null && !pl.isEmpty()) {
             p = (String) pl.get(0);
         }
         if (p == null) {
             p = "";
         }
         
         String dp = SAMLUtilsCommon.decodePassword(p);
         
         int index = locationURL.indexOf("//");
         return locationURL.substring(0, index+2) +
                 u + ":" + dp + "@" +
                 locationURL.substring(index+2);
     }
     
     /**
      * Sign Query string.
      *
      * @param queryString URL query string that will be signed.
      * @param realm realm of host entity.
      * @param hostEntity entityID of host entity.
      * @param hostEntityRole entity role of host entity.
      * @return returns signed query string.
      * @throws SAML2Exception if error in signing the query string.
      */
     public static String signQueryString(String queryString, String realm,
             String hostEntity, String hostEntityRole)
             throws SAML2Exception {
         String method = "signQueryString : ";
         if (debug.messageEnabled()) {
             debug.message(method + "queryString :" + queryString);
         }
         
         String alias = SAML2Utils.getSigningCertAlias(
             realm, hostEntity, hostEntityRole);
         
         if (debug.messageEnabled()) {
             debug.message(method + "realm is : "+ realm);
             debug.message(method + "hostEntity is : " + hostEntity);
             debug.message(method + "Host Entity role is : " + hostEntityRole);
             debug.message(method + "Signing Cert Alias is : " + alias);
         }
         PrivateKey signingKey = keyProvider.getPrivateKey(alias);
         
         if (signingKey == null) {
             debug.error("Incorrect configuration for Signing Certificate.");
             throw new SAML2Exception(
                     SAML2Utils.bundle.getString("metaDataError"));
         }
         return QuerySignatureUtil.sign(queryString, signingKey);
     }
     
     /**
      * Verify Signed Query string.
      *
      * @param queryString URL query string that will be verified.
      * @param realm realm of host entity.
      * @param hostEntityRole entity role of host entity.
      * @param remoteEntity entityID of peer entity.
      * @return returns true if sign is valid.
      * @throws SAML2Exception if error in verifying the signature.
      */
     public static boolean verifyQueryString(String queryString, String realm,
             String hostEntityRole, String remoteEntity)
             throws SAML2Exception {
         String method = "verifyQueryString : ";
         if (debug.messageEnabled()) {
             debug.message(method + "queryString :" + queryString);
         }
         
         X509Certificate signingCert = null;
         if (hostEntityRole.equalsIgnoreCase(SAML2Constants.IDP_ROLE)) {
             SPSSODescriptorElement spSSODesc =
                     saml2MetaManager.getSPSSODescriptor(realm, remoteEntity);
             signingCert =
                     KeyUtil.getVerificationCert(spSSODesc, remoteEntity, false);
         } else {
             IDPSSODescriptorElement idpSSODesc =
                     saml2MetaManager.getIDPSSODescriptor(realm, remoteEntity);
             signingCert =
                     KeyUtil.getVerificationCert(idpSSODesc, remoteEntity, true);
         }
         
         if (debug.messageEnabled()) {
             debug.message(method + "realm is : "+ realm);
             debug.message(method + "Host Entity role is : " + hostEntityRole);
             debug.message(method + "remoteEntity is : " + remoteEntity);
         }
         if (signingCert == null) {
             debug.error("Incorrect configuration for Signing Certificate.");
             throw new SAML2Exception(
                     SAML2Utils.bundle.getString("metaDataError"));
         }
         return QuerySignatureUtil.verify(queryString, signingCert);
     }
     
     /**
      * Parses the request parameters and return session object
      * or redirect to login url.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param metaAlias entityID of hosted entity.
      * @param paramsMap Map of all other parameters.
      * @return session object of <code>HttpServletRequest</code>.
      * @throws SAML2Exception if error initiating request to remote entity.
      */
     public static Object checkSession(
             HttpServletRequest request,
             HttpServletResponse response,
             String metaAlias,
             Map paramsMap) throws SAML2Exception {
         String method = "SAML2Utils.checkSession : ";
         Object session = null;
         try {
             session = SessionManager.getProvider().getSession(request);
         } catch (SessionException se) {
             if (debug.messageEnabled()) {
                 debug.message(method, se);
             }
             session = null;
         }
         String realm = SAML2MetaUtils.getRealmByMetaAlias(metaAlias);
         String hostEntity = null;
         String hostEntityRole = getHostEntityRole(paramsMap);
         
         if (session == null) {
             if (debug.messageEnabled()) {
                 debug.message(method + "session is missing." +
                         "redirect to the authentication service");
             }
             // the user has not logged in yet,
             // redirect to the authentication service
             try {
                 hostEntity = saml2MetaManager.getEntityByMetaAlias(metaAlias);
                 redirectAuthentication(request, response,
                         realm, hostEntity, hostEntityRole);
             } catch (IOException ioe) {
                 debug.error("Unable to redirect to authentication.");
                 throw new SAML2Exception(ioe.toString());
             }
         }
         
         return session;
     }
     
     /**
      * Returns a Name Identifier
      *
      * @return a String the Name Identifier. Null value
      *         is returned if there is an error in
      *         generating the Name Identifier.
      */
     public static String createNameIdentifier() {
         String handle = null;
         try {
             byte[] handleBytes = new byte[21];
             randomGenerator.nextBytes(handleBytes);
             if(handleBytes == null){
                 debug.error("NameIdentifierImpl.createNameIdentifier:"
                         + "Could not generate random handle");
             } else {
                 Base64 encoder = new Base64();
                 handle = encoder.encode(handleBytes);
                 if (debug.messageEnabled()) {
                     debug.message("createNameIdentifier String: " + handle);
                 }
             }
         } catch (Exception e) {
             debug.message("createNameIdentifier:"
                     + " Exception during proccessing request " +  e.getMessage());
         }
         
         return handle;
     }
     
     
     /**
      * Returns the Service Provider AuthnContext Mapper Object.
      *
      * @param authnCtxClassName Service Provider AuthnContext Mapper Class Name.
      * @return SPAuthnContextMapper Object.
      */
     public static SPAuthnContextMapper getSPAuthnContextMapper(
             String realm,String hostEntityID ,
             String authnCtxClassName) {
         
         
         SPAuthnContextMapper spAuthnCtx =
                 (SPAuthnContextMapper)
                 SPCache.authCtxObjHash.get(hostEntityID+"|"+realm);
         
         if (SAML2Utils.debug.messageEnabled()) {
             SAML2Utils.debug.message("AuthContext Class Name is :"
                     +authnCtxClassName);
         }
         if ( (spAuthnCtx == null ) && ((authnCtxClassName != null) &&
                 (authnCtxClassName.length() != 0))) {
             try {
                 spAuthnCtx =
                         (SPAuthnContextMapper)
                         Class.forName(authnCtxClassName).newInstance();
                 SPCache.authCtxObjHash.put(hostEntityID+"|"+realm,spAuthnCtx);
             } catch (ClassNotFoundException ce) {
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message("SAML2Utils: Mapper not configured"
                             + " using Default AuthnContext Mapper");
                 }
             } catch (InstantiationException ie) {
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message("SAML2Utils: Instantiation ");
                     SAML2Utils.debug.message("SAML2Utils:Error instantiating : "
                             + " using Default AuthnContext Mapper");
                 }
             } catch (IllegalAccessException iae) {
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message("SAML2Utils: illegalaccess");
                     SAML2Utils.debug.message("SAML2Utils:Error : "
                             + " using Default AuthnContext Mapper");
                 }
             } catch (Exception e) {
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message("SAML2Utils:Error : "
                             + " using Default AuthnContext Mapper");
                 }
             }
         }
         if (spAuthnCtx == null) {
             spAuthnCtx = new DefaultSPAuthnContextMapper();
             SPCache.authCtxObjHash.put(hostEntityID+"|"+realm,spAuthnCtx);
         }
         
         return spAuthnCtx;
     }
     
     /**
      * Verifies <code>Issuer</code> in <code>Request</code> and returns
      * true if the Issuer is part of COT
      * SAML2 auth module only.
      * @param realm realm of hosted entity.
      * @param hostEntity  name of hosted entity.
      * @param reqIssuer <code>Issuer</code> of Request.
      * @param requestId request ID
      * @return true if issuer is valid.
      * @throws SAML2Exception
      */
     public static boolean verifyRequestIssuer(String realm, String hostEntity,
             Issuer reqIssuer, String requestId)
             throws SAML2Exception {
         boolean issuerValid = isSourceSiteValid(reqIssuer, realm, hostEntity);
         if (issuerValid == false) {
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils " +
                         "Issuer in Request is not valid.");
             }
             String[] data = {hostEntity, realm, requestId};
             LogUtil.error(Level.INFO,
                     LogUtil.INVALID_ISSUER_REQUEST,
                     data,
                     null);
             throw new SAML2Exception(
                     bundle.getString("invalidIssuerInRequest"));
         }
         
         return issuerValid;
     }
     
     /**
      * Verifies <code>Issuer</code> in <code>Response</code> and returns
      * true if the Issuer is part of COT
      * @param realm realm of hosted entity.
      * @param hostEntity  name of hosted entity.
      * @param resIssuer <code>Issuer</code> of Response.
      * @param requestId request ID for the response.
      * @return true if issuer is valid.
      * @throws SAML2Exception
      */
     public static boolean verifyResponseIssuer(String realm, String hostEntity,
             Issuer resIssuer, String requestId)
             throws SAML2Exception {
         boolean issuerValid = isSourceSiteValid(resIssuer, realm, hostEntity);
         if (issuerValid == false) {
             if (debug.messageEnabled()) {
                 debug.message("SAML2Utils " +
                         "Issuer in Response is not valid.");
             }
             String[] data = {hostEntity, realm, requestId};
             LogUtil.error(Level.INFO,
                     LogUtil.INVALID_ISSUER_RESPONSE,
                     data,
                     null);
             throw new SAML2Exception(
                     bundle.getString("invalidIssuerInResponse"));
         }
         
         return issuerValid;
     }
     
     public static String getReaderURL(String spMetaAlias) {
         // get spExtended
         String classMethod = "SAML2Utils:getReaderURL:";
         String readerURL = null;
         try {
             String realm = SAML2MetaUtils.getRealmByMetaAlias(spMetaAlias);
             String spEntityID =
                     saml2MetaManager.getEntityByMetaAlias(spMetaAlias);
             
             if (debug.messageEnabled()) {
                 debug.message(classMethod + "metaAlias is :" + spMetaAlias);
                 debug.message(classMethod + "Realm is :" + realm);
                 debug.message(classMethod + "spEntityID is :" + spEntityID);
             }
             
             SPSSOConfigElement spEntityCfg =
                     saml2MetaManager.getSPSSOConfig(realm,spEntityID);
             Map spConfigAttrsMap=null;
             if (spEntityCfg != null) {
                 spConfigAttrsMap = SAML2MetaUtils.getAttributes(spEntityCfg);
                 List cotList = (List) spConfigAttrsMap.get("cotlist");
                 String cotListStr = (String) cotList.iterator().next();
                 CircleOfTrustDescriptor cotDesc =
                         cotManager.getCircleOfTrust(realm,cotListStr);
                 readerURL = cotDesc.getReaderServiceURL();
             }
         } catch (COTException ce) {
             if (SAML2Utils.debug.messageEnabled()) {
                 SAML2Utils.debug.message(classMethod +
                         "Error retreiving circle of trust",ce);
             }
         } catch (SAML2Exception s2e) {
             if (SAML2Utils.debug.messageEnabled()) {
                 SAML2Utils.debug.message(classMethod +
                         "Error getting reader URL : ", s2e);
             }
         } catch (Exception e) {
             if (SAML2Utils.debug.messageEnabled()) {
                 SAML2Utils.debug.message(classMethod +
                         "Error getting reader URL : ", e);
             }
         }
         return readerURL;
     }
     
     /**
      * Returns the Request URL.
      * The getRequestURL does not alway returns the correct url
      * so this method builds the URL by retrieving the protocol,port
      * host name and deploy descriptor.
      *
      * @param request the <code>HttpServletRequest</code>.
      * @return the Request URL string.
      */
     public static String getBaseURL(HttpServletRequest request) {
         String protocol = request.getScheme();
         String host = request.getHeader("Host");
         if (host == null) {
             host = request.getServerName() + ":" + request.getServerPort();
         }
         String baseURL = protocol + "://" + host + "/";
         String requestURL = request.getRequestURL().toString();
         String tmpurl = null;
         if(protocol.equals("http")) {
             tmpurl = requestURL.substring(8);
         } else {
             tmpurl = requestURL.substring(9);
         }
         int startIndex = tmpurl.indexOf("/") + 1;
         String deployDesc= tmpurl.substring(startIndex);
         if(deployDesc != null && deployDesc.length() != 0) {
             baseURL +=  deployDesc;
         }
         return baseURL;
     }
     
     /**
      * Returns the Identity Provider Entity Identifier.
      * This method retrieves the _saml_idp query parameter
      * from the request and parses it to get the idp entity
      * id. If there are more then one idps then the last
      * one is the preferred idp.
      *
      * @param request the <code>HttpServletRequest</code> .
      * @return the identity provider entity identifier String.
      */
     public static String getPreferredIDP(HttpServletRequest request) {
         String idpList =  request.getParameter(
                 IDPDiscoveryConstants.SAML2_COOKIE_NAME);
         String idpEntityID = null;
         if ((idpList != null) && (idpList.length() > 0)) {
             idpList = idpList.trim();
             StringTokenizer st = new StringTokenizer(idpList," ");
             String preferredIDP = null;
             while (st.hasMoreTokens()) {
                 preferredIDP = (String) st.nextToken();
             }
             try {
                 byte[] byteArray = Base64.decode(preferredIDP);
                 idpEntityID = new String(byteArray);
             } catch (Exception e) {
                 debug.message("Error decoding : " , e);
             }
         }
         return idpEntityID;
     }
     
     
     /**
      * Returns the redirect URL.
      * This methods returns the complete reader redirect url.
      * The RelayState and requestId parameter are appended to
      * the URL to redirection back to the spSSOInit jsp.
      *
      * @param readerURL the readerURL to redirect to.
      * @param requestID the unique identifier to identify the request.
      * @param request the HttpServletRequest.
      * @return redirectURL the URL to redirect to.
      */
     public static String getRedirectURL(String readerURL,String requestID,
             HttpServletRequest request) {
         StringBuffer sb = new StringBuffer();
         sb.append(readerURL).append("?RelayState=");
         String baseURL = getBaseURL(request);
         StringBuffer retURL = new StringBuffer().append(baseURL);
         if (retURL.toString().indexOf("?") == -1) {
             retURL.append("?");
         } else {
             retURL.append("&");
         }
         retURL.append("requestID=").append(requestID);
         String retURLStr = URLEncDec.encode(retURL.toString());
         sb.append(retURLStr);
         String redirectURL = sb.toString();
         
         return redirectURL;
     }
     
     
     /**
      * Returns an <code>IDPAccountMapper</code>
      *
      * @param realm the realm name
      * @param idpEntityID the entity id of the identity provider
      *
      * @return the <code>IDPAccountMapper</code>
      * @exception SAML2Exception if the operation is not successful
      */
     public static IDPAccountMapper getIDPAccountMapper(
             String realm, String idpEntityID)
             throws SAML2Exception {
         String classMethod = "SAML2Utils.getIDPAccountMapper: ";
         String idpAccountMapperName = null;
         IDPAccountMapper idpAccountMapper = null;
         try {
             idpAccountMapperName = getAttributeValueFromSSOConfig(
                     realm, idpEntityID, SAML2Constants.IDP_ROLE,
                     SAML2Constants.IDP_ACCOUNT_MAPPER);
             if (idpAccountMapperName == null) {
                 idpAccountMapperName =
                         SAML2Constants.DEFAULT_IDP_ACCOUNT_MAPPER_CLASS;
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message(classMethod + "use " +
                             SAML2Constants.DEFAULT_IDP_ACCOUNT_MAPPER_CLASS);
                 }
             }
             idpAccountMapper = (IDPAccountMapper)
             IDPCache.idpAccountMapperCache.get(
                     idpAccountMapperName);
             if (idpAccountMapper == null) {
                 idpAccountMapper = (IDPAccountMapper)
                 Class.forName(idpAccountMapperName).newInstance();
                 IDPCache.idpAccountMapperCache.put(
                         idpAccountMapperName, idpAccountMapper);
             } else {
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message(classMethod +
                             "got the IDPAccountMapper from cache");
                 }
             }
         } catch (Exception ex) {
             SAML2Utils.debug.error(classMethod +
                     "Unable to get IDP Account Mapper.", ex);
             throw new SAML2Exception(ex);
         }
         
         return idpAccountMapper;
     }
     
     
     /**
      * Returns an <code>SPAccountMapper</code>
      *
      * @param realm the realm name
      * @param spEntityID the entity id of the service provider
      *
      * @return the <code>SPAccountMapper</code>
      * @exception SAML2Exception if the operation is not successful
      */
     public static SPAccountMapper getSPAccountMapper(
             String realm, String spEntityID)
             throws SAML2Exception {
         String classMethod = "SAML2Utils.getSPAccountMapper: ";
         String spAccountMapperName = null;
         SPAccountMapper spAccountMapper = null;
         try {
             spAccountMapperName = getAttributeValueFromSSOConfig(
                     realm, spEntityID, SAML2Constants.SP_ROLE,
                     SAML2Constants.SP_ACCOUNT_MAPPER);
             if (spAccountMapperName == null) {
                 spAccountMapperName =
                         SAML2Constants.DEFAULT_SP_ACCOUNT_MAPPER_CLASS;
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message(classMethod + "use " +
                             SAML2Constants.DEFAULT_SP_ACCOUNT_MAPPER_CLASS);
                 }
             }
             spAccountMapper = (SPAccountMapper)
             SPCache.spAccountMapperCache.get(spAccountMapperName);
             if (spAccountMapper == null) {
                 spAccountMapper = (SPAccountMapper)
                 Class.forName(spAccountMapperName).newInstance();
                 SPCache.spAccountMapperCache.put(
                         spAccountMapperName, spAccountMapper);
             } else {
                 if (SAML2Utils.debug.messageEnabled()) {
                     SAML2Utils.debug.message(classMethod +
                             "got the SPAccountMapper from cache");
                 }
             }
         } catch (Exception ex) {
             SAML2Utils.debug.error(classMethod +
                     "Unable to get SP Account Mapper.", ex);
             throw new SAML2Exception(ex);
         }
         
         return spAccountMapper;
     }
    
     /**
      * Returns the URL to which redirection will happen after
      * Single-Signon / Federation. This methods checks the 
      * following parameters to determine the Relay State.
      *     1. The "RelayState" query parameter in the request.
      *     2. The "RelayStateAlias" query parameter in the
      *        request which is used in the absence of the
      *        RelayState parameter to determine which query parameter
      *        to use if no "RelayState" query paramerter is present.
      *     3. The "goto" query parameter if present is the default
      *        RelayState in the absence of the above.
      *
      * @param request the <code>HttpServletRequest</code> object.
      * @return the value of the URL to which to redirect on 
      *         successful Single-SignOn  / Federation.
      */
     public static String getRelayState(HttpServletRequest request) {
        String relayState =
                (String)request.getParameter(SAML2Constants.RELAY_STATE);
        if ( (relayState == null) || (relayState.length() == 0)) {
            String relayStateAlias =
                request.getParameter(SAML2Constants.RELAY_STATE_ALIAS);
            if (relayStateAlias != null && relayStateAlias.length() > 0) {
                StringTokenizer st = 
                       new StringTokenizer(relayStateAlias,"|");
                while (st.hasMoreTokens()) {
                    String tmp = (String) st.nextToken();
                    relayState = (String)request.getParameter(tmp);
                    if (relayState != null && relayState.length() > 0) {
                        break;
                    }
                }
            }
            if (relayState == null) {
                // check if goto parameter is there.
                relayState = (String)request.getParameter(SAML2Constants.GOTO);
            }
        }
        return relayState;
     }
     
     /**
      * Compares the destination and location
      * @param destination Destination
      * @param location the URL from the meta
      * @return <code>true</code> if the input are the same, 
      *         otherwise, return <code>false</code>
      */
     public static boolean verifyDestination(String destination, 
         String location) {
         /* Note: 
         Here we assume there is one endpoint per protocol. In future,
         we may support more than one endpoint per protocol. The caller 
         code should change accordingly. 
         */
         return ((location != null) && (location.length() != 0) &&
             (destination != null) && (destination.length() != 0) &&
             (location.equalsIgnoreCase(destination)));  
     }    
 }
