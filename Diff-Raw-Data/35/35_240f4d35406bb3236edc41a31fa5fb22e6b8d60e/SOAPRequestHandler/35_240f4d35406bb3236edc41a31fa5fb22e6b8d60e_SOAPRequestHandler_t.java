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
 * $Id: SOAPRequestHandler.java,v 1.12 2008-03-13 20:21:43 mallas Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.wss.security.handler;
 
 import javax.xml.soap.SOAPMessage;
 import javax.xml.soap.SOAPHeaderElement;
 import javax.security.auth.Subject;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.Set;
 import java.util.Map;
 import java.util.List;
 import java.util.Iterator;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ResourceBundle;
 import javax.xml.soap.SOAPHeader;
 import javax.xml.soap.SOAPException;
 import javax.xml.namespace.QName;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.common.SystemConfigurationUtil;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.Constants;
 import com.iplanet.am.util.SystemProperties;
 
 import com.sun.identity.liberty.ws.authnsvc.AuthnSvcClient;
 import com.sun.identity.liberty.ws.authnsvc.AuthnSvcException;
 import com.sun.identity.liberty.ws.authnsvc.protocol.SASLRequest;
 import com.sun.identity.liberty.ws.authnsvc.protocol.SASLResponse;
 import com.sun.identity.liberty.ws.disco.ResourceOffering;
 import com.sun.identity.liberty.ws.security.SecurityAssertion;
 import com.sun.identity.liberty.ws.soapbinding.SOAPBindingException;
 import com.sun.identity.liberty.ws.soapbinding.SOAPBindingConstants;
 
 import com.sun.identity.wss.security.SecurityException;
 import com.sun.identity.wss.security.SecurityToken;
 import com.sun.identity.wss.security.AssertionTokenSpec;
 import com.sun.identity.wss.security.SAML2TokenSpec;
 import com.sun.identity.wss.security.SecurityMechanism;
 import com.sun.identity.wss.security.SecurityTokenFactory;
 import com.sun.identity.wss.security.BinarySecurityToken;
 import com.sun.identity.wss.security.X509TokenSpec;
 import com.sun.identity.wss.security.UserNameTokenSpec;
 import com.sun.identity.wss.security.WSSUtils;
 import com.sun.identity.wss.security.PasswordCredential;
 import com.sun.identity.wss.security.WSSConstants;
 import com.sun.identity.wss.provider.ProviderConfig;
 import com.sun.identity.wss.provider.TrustAuthorityConfig;
 import com.sun.identity.wss.provider.STSConfig;
 import com.sun.identity.wss.provider.ProviderException;
 import com.sun.identity.saml.assertion.NameIdentifier;
 import com.sun.identity.saml2.assertion.AssertionFactory;
 import com.sun.identity.saml2.assertion.NameID;
 import com.sun.identity.wss.sts.TrustAuthorityClient;
 import com.sun.identity.wss.sts.FAMSTSException;
 
 /* iPlanet-PUBLIC-CLASS */
 
 /**
  * This class <code>SOAPRequestHandler</code> is to process and secure the 
  * in-bound or out-bound  <code>SOAPMessage</code>s of the web service clients
  *  and web service providers. 
  *
  * <p> This class processes the <code>SOAPMessage</code>s for the
  * web services security according to the processing rules defined in
  * OASIS web services security specification and as well as the Liberty
  * Identity Web services security framework.
  *
  */
 public class SOAPRequestHandler implements SOAPRequestHandlerInterface {
 
     private String providerName = null;
     private String PROVIDER_NAME = "providername";
     private static Debug debug = WSSUtils.debug;
     private static ResourceBundle bundle = WSSUtils.bundle;
 
     private static String BACK_SLASH = "\\";
     private static String FORWARD_SLASH = "/";
     private static MessageAuthenticator authenticator = null;
     
     /**
      * Property for web services authenticator.
      */
     private static final String WSS_AUTHENTICATOR =
             "com.sun.identity.wss.security.authenticator";
     
     /**
      * Property string for liberty authentication service url.
      */
     private static final String LIBERTY_AUTHN_URL =
             "com.sun.identity.liberty.authnsvc.url";
     
     private static final String MECHANISM_SSOTOKEN = "SSOTOKEN";
 
     /**
      * Initializes the handler with the given configuration. 
      *
      * @param config the configuration map to initializate the provider.
      *
      * @exception SecurityException if the initialization fails.
      */
     public void init(Map config) throws SecurityException {
         if(debug.messageEnabled()) {
             debug.message("SOAPRequestHandler.Init map:" + config);
         }
         providerName = (String)config.get(PROVIDER_NAME);
         if( (providerName == null) || (providerName.length() == 0) ) {
             debug.error("SOAPRequestHandler.init:: provider name is null");
             throw new SecurityException(
                     bundle.getString("SOAPRequestHandlerInitFailed"));
         }
     }
 
     /**
      * Authenticates the <code>SOAPMessage</code> from a remote client. 
      *
      * @param soapRequest SOAPMessage that needs to be validated.
      *
      * @param subject the subject that may be used by the callers
      *        to store Principals and credentials validated in the request.
      *
      * @param sharedState that may be used to store any shared state 
      *        information between <code>validateRequest and <secureResponse>
      *
      * @param request the <code>HttpServletRequest</code> associated with 
      *        this SOAP Message request.
      *
      * @param response the <code>HttpServletResponse</code> associated with
      *        this SOAP Message response. 
      *
      * @return Object the authenticated token.
      *
      * @exception SecurityException if any error occured during validation.
      */
     public Object validateRequest(SOAPMessage soapRequest,
             Subject subject,
             Map sharedState,
             HttpServletRequest request,
             HttpServletResponse response)
             throws SecurityException {
 
         debug.message("SOAPRequestHandler.validateRequest: Init");
         ProviderConfig config = getWSPConfig();
 
         if(isLibertyMessage(soapRequest)) {
            if(debug.messageEnabled()) {
                 debug.message("SOAPRequestHandler.validateRequest:: Incoming " +
                         "SOAPMessage is of liberty message type.");
             }
             MessageProcessor processor = new MessageProcessor(config);
             try {
                 processor.validateRequest(soapRequest, subject,
                         sharedState, request);
                 removeValidatedHeaders(config, soapRequest);
                 return subject;
             } catch (SOAPBindingException sbe) {
                 debug.error("SOAPRequestHandler.validateRequest:: SOAP" +
                         "BindingException:: ", sbe);
                 throw new SecurityException(sbe.getMessage());
             }
         }
 
         SecureSOAPMessage secureMsg =
                 new SecureSOAPMessage(soapRequest, false);
 
         secureMsg.parseSecurityHeader(
                 (Node) (secureMsg.getSecurityHeaderElement()));
 
         if ((config.isRequestEncryptEnabled()) ||
                 (config.isRequestHeaderEncryptEnabled())) {
             secureMsg.decrypt((config.isRequestEncryptEnabled()),
                    (config.isRequestHeaderEncryptEnabled()));            
             soapRequest = secureMsg.getSOAPMessage();
            secureMsg = new SecureSOAPMessage(soapRequest, false);
            secureMsg.parseSecurityHeader(
                    (Node)secureMsg.getSecurityHeaderElement());
         }
 
         if (config.isRequestSignEnabled()) {
             if (!secureMsg.verifySignature()) {
                 debug.error("SOAPRequestHandler.validateRequest:: Signature " +
                         "verification failed.");
                 throw new SecurityException(
                         bundle.getString("signatureValidationFailed"));
             }
         }
 
         SecurityMechanism securityMechanism =
                 secureMsg.getSecurityMechanism();
         String uri = securityMechanism.getURI();
 
         List list = config.getSecurityMechanisms();
 
         if(debug.messageEnabled()) {
             debug.message("List of getSecurityMechanisms : " + list);
             debug.message("current uri : " + uri);
         }
 
         if(!list.contains(uri)) {
            if( (!list.contains(
                     SecurityMechanism.WSS_NULL_ANONYMOUS_URI)) &&
                     (!list.contains(
                 SecurityMechanism.WSS_TLS_ANONYMOUS_URI))&&
                     (!list.contains(
                     SecurityMechanism.WSS_CLIENT_TLS_ANONYMOUS_URI))) {
                 throw new SecurityException(
                         bundle.getString("unsupportedSecurityMechanism"));
             } else {
               if(debug.messageEnabled()) {
                     debug.message("SOAPRequestHandler.validateRequest:: " +
                             "provider is not configured for the incoming message " +
                             " level type but allows anonymous");
                 }
                 return subject;
             }
         }
 
         if(SecurityMechanism.WSS_NULL_ANONYMOUS_URI.equals(uri) ||
                 (SecurityMechanism.WSS_TLS_ANONYMOUS_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_CLIENT_TLS_ANONYMOUS_URI.equals(uri))) {
             return subject;
         }
 
         subject = (Subject) getAuthenticator().authenticate(subject,
                 secureMsg.getSecurityMechanism(),
                 secureMsg.getSecurityToken(),
                 config, secureMsg, false);
 
         removeValidatedHeaders(config, soapRequest);
 
         return subject;
     }
 
     /**
      * Secures the SOAP Message response to the client.
      *
      * @param soapMessage SOAP Message that needs to be secured.
      *
      * @param sharedState a map for the callers to store any state information
      *        between <code>validateRequest</code> and 
      *        <code>secureResponse</code>.
      *
      * @exception SecurityException if any error occurs during securing. 
      */
     public SOAPMessage secureResponse (SOAPMessage soapMessage, 
             Map sharedState) throws SecurityException {
 
         debug.message("SOAPRequestHandler.secureResponse: Init");
         ProviderConfig config = getWSPConfig();
 
         Object req = sharedState.get(SOAPBindingConstants.LIBERTY_REQUEST);
         if(req != null) {
             MessageProcessor processor = new MessageProcessor(config);
             try {
                 return processor.secureResponse(soapMessage, sharedState);
             } catch (SOAPBindingException sbe) {
                 debug.error("SOAPRequestHandler.secureResponse:: SOAP" +
                         "BindingException.", sbe);
                 throw new SecurityException(sbe.getMessage());
             }
         }
 
         if(!config.isResponseSignEnabled() && 
                 !config.isResponseEncryptEnabled()) {
             return soapMessage;
         }
 
         SSOToken token = (SSOToken)AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
         SecurityTokenFactory factory = SecurityTokenFactory.getInstance(token);
 
         String keyAlias = SystemConfigurationUtil.getProperty(
                 Constants.SAML_XMLSIG_CERT_ALIAS);
         if(!config.useDefaultKeyStore()) {
             keyAlias = config.getKeyAlias();
         }
         String[] certAlias = {keyAlias};
         X509TokenSpec tokenSpec = new X509TokenSpec(certAlias,
                 BinarySecurityToken.X509V3,
                 BinarySecurityToken.BASE64BINARY);
 
         SecurityToken securityToken =
                 factory.getSecurityToken(tokenSpec);
 
         SecureSOAPMessage secureMessage =
                 new SecureSOAPMessage(soapMessage, true);
 
         secureMessage.setSecurityToken(securityToken);
         secureMessage.setSecurityMechanism(
                 SecurityMechanism.WSS_NULL_X509_TOKEN);
 
         if(config.isResponseSignEnabled()) {            
             secureMessage.sign(keyAlias);
         }
 
         if(config.isResponseEncryptEnabled()) {            
             secureMessage.encrypt(keyAlias,
                     (config.isResponseEncryptEnabled()),
                     false);
         }
 
         soapMessage = secureMessage.getSOAPMessage();
 
         return soapMessage;
 
     }
 
     /**
      * Secures the <code>SOAPMessage</code> request by adding necessary
      * credential information.
      *
      * @param soapMessage the <code>SOAPMessage</code> that needs to be secured.
      *
      * @param subject  the <code>Subject<code> of the authenticating entity.
      *
      * @param sharedState Any shared state information that may be used between
      *        the <code>secureRequest</code> and <code>validateResponse</code>. 
      *
      * @exception SecurityException if any failure for securing the request.
      */
     public SOAPMessage secureRequest (
             SOAPMessage soapMessage,
             Subject subject,
             Map sharedState) throws SecurityException {
 
         WSSUtils.debug.message("SOAPRequestHandler.secureRequest: Init");
         if(WSSUtils.debug.messageEnabled()) {
             WSSUtils.debug.message("SOAPRequestHandler.secureRequest: " +
                     "Shared Map" + sharedState);
         }
 
         ProviderConfig config = getProviderConfig(sharedState);
         if(config == null) {
            if(WSSUtils.debug.messageEnabled()) {
               WSSUtils.debug.message("SOAPRequestHandler.secureRequest: "+
                         "Provider configuration from shared map is null");
             }
             config = getWSCConfig();
         }
 
         SecurityToken securityToken = null;
         List secMechs = config.getSecurityMechanisms();
         if(secMechs == null || secMechs.isEmpty()) {
             throw new SecurityException(
                     bundle.getString("securityMechNotConfigured"));
         }
 
         String sechMech = (String)secMechs.iterator().next();
         SecurityMechanism securityMechanism =
                 SecurityMechanism.getSecurityMechanism(sechMech);
         SecureSOAPMessage secureMessage = null;
         String uri = securityMechanism.getURI();
         if (((SecurityMechanism.WSS_NULL_ANONYMOUS_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_TLS_ANONYMOUS_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_CLIENT_TLS_ANONYMOUS_URI.equals(uri)))) {
             secureMessage =
                     new SecureSOAPMessage(soapMessage, true);
         } else {
             if (securityMechanism.isTALookupRequired()) {
                 SubjectSecurity subjectSecurity = getSubjectSecurity(subject);
                 SSOToken ssoToken = subjectSecurity.ssoToken;
            if(securityMechanism.getURI().equals
                    (SecurityMechanism.LIBERTY_DS_SECURITY_URI)) {
                if(subjectSecurity.ssoToken == null) {
                         throw new SecurityException(
                                 bundle.getString("invalidSSOToken"));
                     }
                return getSecureMessageFromLiberty(subjectSecurity.ssoToken, subject,
                             soapMessage, sharedState, config);
                 } else {
                     try {
                         TrustAuthorityClient client = new TrustAuthorityClient();
                         TrustAuthorityConfig taconfig =
                                 config.getTrustAuthorityConfig();
                         String taName = taconfig.getName();
                    if(taName != null) {
                             ThreadLocalService.setServiceName(taName);
                         }
                         securityToken = client.getSecurityToken(config,
                                 subjectSecurity.ssoToken);
                     } catch (FAMSTSException stsEx) {
                         debug.error("SOAPRequestHandler.secureRequest: exception" +
                                 "in obtaining STS Token", stsEx);
                         throw new SecurityException(stsEx.getMessage());
                     }
                 }
             
             } else {
                 securityToken = getSecurityToken(
                         securityMechanism, config, subject);
             }
         
         secureMessage = 
                     new SecureSOAPMessage(soapMessage, true);
             secureMessage.setSecurityToken(securityToken);
         }
 
         secureMessage.setSecurityMechanism(securityMechanism);
         
         String keyAlias = SystemConfigurationUtil.getProperty(
                 Constants.SAML_XMLSIG_CERT_ALIAS);
         if(!config.useDefaultKeyStore()) {
             keyAlias = config.getKeyAlias();
         }
 
         if(config.isRequestSignEnabled()) {            
             secureMessage.sign(keyAlias);
         }
 
         if((config.isRequestEncryptEnabled()) || 
                 (config.isRequestHeaderEncryptEnabled())) {
             secureMessage.encrypt(keyAlias,
                     (config.isRequestEncryptEnabled()),
                     (config.isRequestHeaderEncryptEnabled()));
         }
 
         soapMessage = secureMessage.getSOAPMessage();
         return soapMessage;
     }
 
     /**
      * Validates the SOAP Response from the service provider. 
      *
      * @param soapMessage the <code>SOAPMessage</code> that needs to be 
      *        validated.
      *
      * @param sharedState Any shared data that may be used between the
      *        <code>secureRequest</code> and <code>validateResponse</code>.
      *
      * @exception SecurityException if any failure occured for validating the
      *            response.
      */
     public void validateResponse (SOAPMessage soapMessage, 
             Map sharedState) throws SecurityException {
 
         debug.message("SOAPRequestHandler.validateResponse:: Init");
         ProviderConfig config = getProviderConfig(sharedState);
         if(config == null) {
            if(WSSUtils.debug.messageEnabled()) {
               WSSUtils.debug.message("SOAPRequestHandler.validateResponse: "+
                         "Provider configuration from shared map is null");
             }
             config = getWSCConfig();
         }
         if(isLibertyMessage(soapMessage)) {
             MessageProcessor processor = new MessageProcessor(config);
             try {
                 processor.validateResponse(soapMessage, sharedState);
                 removeValidatedHeaders(config, soapMessage);
                 return;
             } catch (SOAPBindingException sbe) {
                 debug.error("SOAPRequestHandler.validateResponse:: SOAP" +
                         "BindingException. ", sbe);
                 throw new SecurityException(sbe.getMessage());
             }
         }
 
         SecureSOAPMessage secureMessage =
                 new SecureSOAPMessage(soapMessage, false);
 
         if(config.isResponseEncryptEnabled()) {
             secureMessage.decrypt((config.isResponseEncryptEnabled()), false);
             soapMessage = secureMessage.getSOAPMessage();
         }
 
         secureMessage.parseSecurityHeader(
             (Node)(secureMessage.getSecurityHeaderElement()));
 
         if(config.isResponseSignEnabled()) {           
            if(!secureMessage.verifySignature()) {
                 debug.error("SOAPRequestHandler.validateResponse:: Signature" +
                         " Verification failed");
                 throw new SecurityException(
                         bundle.getString("signatureValidationFailed"));
             }
         }
 
         removeValidatedHeaders(config, soapMessage);
     }
 
     /**
      * Initialize the system properties before the SAML module is invoked.
      */
     private void initializeSystemProperties(ProviderConfig config)
             throws IOException {
 
         String keyStoreFile = config.getKeyStoreFile();
         String ksPasswd = config.getKeyStoreEncryptedPasswd();
         String keyPasswd = config.getKeyEncryptedPassword();
         String certAlias = config.getKeyAlias();
 
         if(keyStoreFile == null || ksPasswd == null) {
            if(debug.messageEnabled()) {
                 debug.message("SOAPRequestHandler.initSystemProperties:: " +
                 "Provider config does not have keystore information. Will " +
                         "fallback to the default configuration in AMConfig.");
             }
             return;
         }
 
         if(keyStoreFile.indexOf(BACK_SLASH) != -1) {
             keyStoreFile.replaceAll(BACK_SLASH, FORWARD_SLASH);
         }
 
         int index = keyStoreFile.lastIndexOf(FORWARD_SLASH);
         String storePassFile =
                 keyStoreFile.substring(0, index) + "/.storepassfile";
         String keyPassFile = keyStoreFile.substring(0, index) + "/.keypassfile";
 
         if(debug.messageEnabled()) {
             debug.message("SOAPRequestHandler.initSystemProperties:: " +
            "\n" +  "KeyStoreFile: " + keyStoreFile + "\n" +
            "Encrypted keystore password: " + ksPasswd + "\n" +
            "Encrypted key password: " + keyPasswd + "\n" +
            "Location of the store encrypted password: " + storePassFile + "\n"+
                     "Location of the key encrypted password: " + keyPassFile);
         }
 
         if(keyPasswd == null) {
             keyPasswd = ksPasswd;
         }
         FileOutputStream out = new FileOutputStream(new File(keyPassFile));
         out.write(keyPasswd.getBytes());
         out.flush();
         FileOutputStream out1 = new FileOutputStream(new File(storePassFile));
         out1.write(ksPasswd.getBytes());
         out1.flush();
 
         SystemProperties.initializeProperties(
                 Constants.SAML_XMLSIG_KEYSTORE, keyStoreFile);
         SystemProperties.initializeProperties(
                 Constants.SAML_XMLSIG_STORE_PASS, storePassFile);
         SystemProperties.initializeProperties(
                 Constants.SAML_XMLSIG_KEYPASS, keyPassFile);
         SystemProperties.initializeProperties(
                 Constants.SAML_XMLSIG_CERT_ALIAS, certAlias);
     }
 
     private ProviderConfig getWSPConfig() throws SecurityException {
 
         ProviderConfig config = null;
         try {
             config = ProviderConfig.getProvider(providerName,
                     ProviderConfig.WSP);
             if(config == null) {
                 debug.error("SOAPRequestHandler.getWSPConfig:: Provider" +
                         " configuration is null");
                 throw new SecurityException(
                         bundle.getString("noProviderConfig"));
             }
             if(!config.useDefaultKeyStore()) {
                 initializeSystemProperties(config);
             }
 
         } catch (ProviderException pe) {
             debug.error("SOAPRequestHandler.getWSPConfig:: Provider" +
                     " configuration read failure", pe);
             throw new SecurityException(
                     bundle.getString("cannotInitializeProvider"));
 
         } catch (IOException ie) {
             debug.error("SOAPRequestHandler.getWSPConfig:: Provider" +
                     " configuration read failure", ie);
             throw new SecurityException(
                     bundle.getString("cannotInitializeProvider"));
         }
         return config;
     }
 
     private ProviderConfig getWSCConfig() throws SecurityException {
 
         ProviderConfig config = null;
         try {
             config = ProviderConfig.getProvider(providerName,
                     ProviderConfig.WSC);
             if(config == null) {
                 debug.error("SOAPRequestHandler.getWSCConfig:: Provider" +
                         " configuration is null");
                 throw new SecurityException(
                         bundle.getString("noProviderConfig"));
             }
             if(!config.useDefaultKeyStore()) {
                 initializeSystemProperties(config);
             }
 
         } catch (ProviderException pe) {
             debug.error("SOAPRequestHandler.getWSCConfig:: Provider" +
                     " configuration read failure", pe);
             throw new SecurityException(
                     bundle.getString("cannotInitializeProvider"));
 
         } catch (IOException ie) {
             debug.error("SOAPRequestHandler.getWSCConfig:: Provider" +
                     " configuration read failure", ie);
             throw new SecurityException(
                     bundle.getString("cannotInitializeProvider"));
         }
         return config;
     }
 
     /**
      * Returns the security token for the configured security mechanism.
      */
     private SecurityToken getSecurityToken(
             SecurityMechanism secMech,
             ProviderConfig config,
             Subject subject) throws SecurityException {
 
         String uri = secMech.getURI();
         String certAlias = SystemConfigurationUtil.getProperty(
                 Constants.SAML_XMLSIG_CERT_ALIAS);
         if(!config.useDefaultKeyStore()) {
             certAlias = config.getKeyAlias();
         }
         SecurityToken securityToken = null;
 
         if(debug.messageEnabled()) {
             debug.message("getSecurityToken: SecurityMechanism URI : " + uri);
         }
 
         SSOToken token = (SSOToken)AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
 
         SecurityTokenFactory factory = SecurityTokenFactory.getInstance(token);
 
         if(SecurityMechanism.WSS_NULL_X509_TOKEN_URI.equals(uri) ||
            SecurityMechanism.WSS_TLS_X509_TOKEN_URI.equals(uri) ||
            SecurityMechanism.WSS_CLIENT_TLS_X509_TOKEN_URI.equals(uri)) {
 
            if(debug.messageEnabled()) {
                 debug.message("SOAPRequestHandler.getSecurityToken:: creating " +
                         "X509 token");
             }
             String[] aliases = {certAlias};
             X509TokenSpec tokenSpec = new X509TokenSpec(
                     aliases, BinarySecurityToken.X509V3,
                     BinarySecurityToken.BASE64BINARY);
            securityToken = factory.getSecurityToken(tokenSpec);
 
         } else if(
            (SecurityMechanism.WSS_NULL_SAML_HK_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_TLS_SAML_HK_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_CLIENT_TLS_SAML_HK_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_NULL_SAML_SV_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_TLS_SAML_SV_URI.equals(uri)) ||
            (SecurityMechanism.WSS_CLIENT_TLS_SAML_SV_URI.equals(uri)) ) {
 
            if(debug.messageEnabled()) {
                 debug.message("SOAPRequestHandler.getSecurityToken:: creating " +
                         "SAML token");
             }
             NameIdentifier ni = null;
             try {
                 SubjectSecurity subjectSecurity = getSubjectSecurity(subject);
                 SSOToken userToken = subjectSecurity.ssoToken;
                if(userToken != null) {
                     ni = new NameIdentifier(userToken.getPrincipal().getName());
                 } else {
                     ni = new NameIdentifier(config.getProviderName());
                 }
             } catch (Exception ex) {
                 throw new SecurityException(ex.getMessage());
             }
 
             AssertionTokenSpec tokenSpec = new AssertionTokenSpec(ni,
                     secMech, certAlias);
            securityToken = factory.getSecurityToken(tokenSpec); 
 
         } else if(
             (SecurityMechanism.WSS_NULL_USERNAME_TOKEN_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_TLS_USERNAME_TOKEN_URI.equals(uri)) ||
             (SecurityMechanism.WSS_CLIENT_TLS_USERNAME_TOKEN_URI.equals(uri))
             || (SecurityMechanism.WSS_NULL_USERNAME_TOKEN_PLAIN_URI.equals(uri)) 
             || (SecurityMechanism.WSS_TLS_USERNAME_TOKEN_PLAIN_URI.equals(uri)) 
             || (SecurityMechanism.WSS_CLIENT_TLS_USERNAME_TOKEN_PLAIN_URI.
                 equals(uri))){
 
             if(debug.messageEnabled()) {
                 debug.message("SOAPRequestHandler.getSecurityToken:: creating " +
                         "UserName token");
             }
             List creds = null;
 
             try {
                 SubjectSecurity subjectSecurity = getSubjectSecurity(subject);
                 creds = subjectSecurity.userCredentials;
             } catch (Exception ex) {
                 if(debug.messageEnabled()) {
                     debug.message("SOAPRequestHandler.getSecurityToken:: " +
                                   "getSubjectSecurity error :" 
                                   + ex.getMessage());
                 }
             }
 
             if(creds == null || creds.isEmpty()) {
                 creds = config.getUsers();
             }
             if(creds == null || creds.isEmpty()) {
                 debug.error("SOAPRequestHandler.getSecurityToken:: No users " +
                         " are configured.");
                 throw new SecurityException(
                         bundle.getString("nousers"));
             }
             PasswordCredential credential =
                    (PasswordCredential)creds.iterator().next();
             UserNameTokenSpec tokenSpec = new UserNameTokenSpec();
             if((SecurityMechanism.WSS_NULL_USERNAME_TOKEN_PLAIN_URI.equals(uri)) 
                || (SecurityMechanism.WSS_TLS_USERNAME_TOKEN_PLAIN_URI.equals(uri)) 
                || (SecurityMechanism.WSS_CLIENT_TLS_USERNAME_TOKEN_PLAIN_URI.
                    equals(uri))) {
                 tokenSpec.setPasswordType(WSSConstants.PASSWORD_PLAIN_TYPE);
             } else {
                 tokenSpec.setNonce(true);
                 tokenSpec.setPasswordType(WSSConstants.PASSWORD_DIGEST_TYPE);
             }
             tokenSpec.setCreateTimeStamp(true);
             tokenSpec.setUserName(credential.getUserName());
             tokenSpec.setPassword(credential.getPassword());
             securityToken = factory.getSecurityToken(tokenSpec);
 
         } else if(
            (SecurityMechanism.WSS_NULL_SAML2_HK_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_TLS_SAML2_HK_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_CLIENT_TLS_SAML2_HK_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_NULL_SAML2_SV_URI.equals(uri)) ||
                 (SecurityMechanism.WSS_TLS_SAML2_SV_URI.equals(uri)) ||
            (SecurityMechanism.WSS_CLIENT_TLS_SAML2_SV_URI.equals(uri)) ) {
 
            if(debug.messageEnabled()) {
                 debug.message("SOAPRequestHandler.getSecurityToken:: creating " +
                         "SAML2 token");
             }
             NameID ni = null;
             try {
                 AssertionFactory assertionFactory =
                         AssertionFactory.getInstance();
                ni = assertionFactory.createNameID();
                 SubjectSecurity subjectSecurity = getSubjectSecurity(subject);
                 SSOToken userToken = subjectSecurity.ssoToken;
                if(userToken != null) {
                     ni.setValue(userToken.getPrincipal().getName());
                 } else {
                     ni.setValue(config.getProviderName());
                 }
 
             } catch (Exception ex) {
                 throw new SecurityException(ex.getMessage());
             }
 
             SAML2TokenSpec tokenSpec = new SAML2TokenSpec(ni,
                     secMech, certAlias);
            securityToken = factory.getSecurityToken(tokenSpec);                                 
 
         } else {
             throw new SecurityException(
                     bundle.getString("unsupportedSecurityMechanism"));
         }
         return securityToken;
     }
 
     /**
      * Place holder class for the subject credential objects.
      */
     private class SubjectSecurity {
         SSOToken ssoToken = null;
         ResourceOffering discoRO = null;
         List discoCredentials = null;
         List userCredentials = null;
     }
 
     /**
      * Returns the security credentials if exists in the subject.
      */
     private SubjectSecurity getSubjectSecurity(Subject subject) {
 
         final SubjectSecurity subjectSecurity = new SubjectSecurity();
         final Subject sub = subject;
         AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public Object run() {
                         Set creds = sub.getPrivateCredentials();
                      if(creds == null || creds.isEmpty()) {
                             return null;
                         }
                      Iterator iter =  creds.iterator();
                      while(iter.hasNext()) {
                             Object credObj = iter.next();
                          if(credObj instanceof SSOToken) {
                             subjectSecurity.ssoToken = (SSOToken)credObj;
                          } else if(credObj instanceof ResourceOffering) {
                             subjectSecurity.discoRO = (ResourceOffering)credObj;
                          } else if(credObj instanceof List) {
                             List list = (List)credObj;
                             if(list != null && list.size() > 0) {
                                     if (list.get(0) instanceof SecurityAssertion) {
                                         subjectSecurity.discoCredentials = list;
                                 } else if (
                                   list.get(0) instanceof PasswordCredential) {
                                         subjectSecurity.userCredentials = list;
                                     }
                                 }
                             }
                         }
                         return null;
                     }
                 });
         return subjectSecurity;
     }
 
     /**
      * Returns the configured message authenticator.
      */
     public static MessageAuthenticator getAuthenticator()
             throws SecurityException {
 
         if(authenticator != null) {
             return authenticator;
         }
         String classImpl = SystemConfigurationUtil.getProperty(
                 WSS_AUTHENTICATOR,
                 "com.sun.identity.wss.security.handler.DefaultAuthenticator");
         try {
             Class authnClass = Class.forName(classImpl);
             authenticator = (MessageAuthenticator)authnClass.newInstance();
         } catch (Exception ex) {
             debug.error("SOAPRequestHandler.getAuthenticator:: Unable to " +
                     "get the authenticator", ex);
             throw new SecurityException(
                     bundle.getString("authenticatorNotFound"));
         }
         return authenticator;
     }
 
     /**
      * Returns the secured <code>SOAPMessage</code> by using liberty
      * protocols.
      *
      * @param ssoToken Single sign-on token of the user.
      *
      * @param subject the subject.
      *
      * @param soapMessage the SOAPMessage that needs to be secured.
      *
      * @param sharedData any shared data map between request and the response.
      *
      * @param providerConfig the provider configuration.
      *
      * @return SecurityException if there is any error occured.
      */
     private SOAPMessage getSecureMessageFromLiberty(
             SSOToken ssoToken,
             Subject subject,
             SOAPMessage soapMessage,
             Map sharedData,
             ProviderConfig providerConfig)
             throws SecurityException {
 
         try {
             SSOTokenManager.getInstance().validateToken(ssoToken);
             ResourceOffering discoRO = getDiscoveryResourceOffering(
                     subject, ssoToken);
            if(debug.messageEnabled()) {
               debug.message("SOAPRequestHandler.getSecureMessageFromLiberty:: "+
               "Discovery service resource offering. " + discoRO.toString());
             }
             List credentials = getDiscoveryCredentials(subject);
             MessageProcessor processor = new MessageProcessor(providerConfig);
             return processor.secureRequest(discoRO, credentials,
                     providerConfig.getServiceType(), soapMessage, sharedData);
 
         } catch (SSOException se) {
             debug.error("SOAPRequestHandler.getSecureMessageFromLiberty:: " +
                     "Invalid sso token", se);
             throw new SecurityException(
                     bundle.getString("invalidSSOToken"));
         } catch (SOAPBindingException sbe) {
             debug.error("SOAPRequestHandler.getSecureMessageFromLiberty:: " +
                     " SOAPBinding exception", sbe);
             throw new SecurityException(sbe.getMessage());
         }
     }
 
     /**
      * Returns the discovery service resource offering.
      */
     private ResourceOffering getDiscoveryResourceOffering(
             Subject subject, SSOToken ssoToken) throws SecurityException {
 
         SubjectSecurity subjectSecurity = getSubjectSecurity(subject);
         if(subjectSecurity.discoRO != null) {
            if(debug.messageEnabled()) {
               debug.message("SOAPRequestHandler.getDiscoveryResourceOffering::"
               + " subject contains resource offering.");
             }
             return subjectSecurity.discoRO;
         }
 
 // If the creds not present, authenticate to the IDP via AuthnService.
         SASLResponse saslResponse =  getSASLResponse(ssoToken);
         if(saslResponse == null) {
             debug.error("SOAPRequestHandler.getDiscoveryResourceOffering:: " +
                     "SASL Response is null");
             throw new SecurityException(
                     bundle.getString("SASLFailure"));
         }
 
         final ResourceOffering discoRO = saslResponse.getResourceOffering();
         if(discoRO == null) {
             throw new SecurityException(
                     bundle.getString("resourceOfferingMissing"));
         }
         final List credentials = saslResponse.getCredentials();
         final Subject sub = subject;
         if(discoRO != null) {
             AccessController.doPrivileged(
                     new PrivilegedAction() {
                         public Object run() {
                             sub.getPrivateCredentials().add(discoRO);
                        if(credentials != null) {
                                 sub.getPrivateCredentials().add(credentials);
                             }
                             return null;
                         }
                }
            );
         } 
         return discoRO;
     }
 
     /**
      * Returns the credentials for the discovery service.
      */
     private List getDiscoveryCredentials(Subject subject) {
         SubjectSecurity subjectSecurity = getSubjectSecurity(subject);
         return subjectSecurity.discoCredentials;
     }
 
     /**
      * Returns the <code>SASLResponse</code> using user's SSOToken.
      */
     private SASLResponse getSASLResponse(SSOToken ssoToken)
             throws SecurityException {
         SASLRequest saslReq = new SASLRequest(MECHANISM_SSOTOKEN);
         try {
             String authURL = SystemConfigurationUtil.getProperty(LIBERTY_AUTHN_URL);
             if(authURL == null) {
                 debug.error("SOAPRequestHandler.getSASLResponse:: AuthnURL " +
                         " not present in the configuration.");
                 throw new SecurityException(
                         bundle.getString("authnURLMissing"));
             }
 
             SASLResponse saslResp = AuthnSvcClient.sendRequest(
                     saslReq, authURL);
             if(!saslResp.getStatusCode().equals(SASLResponse.CONTINUE)) {
                 debug.error("SOAPRequestHandler.getSASLResponse:: ABORT");
                 throw new SecurityException(
                         bundle.getString("SASLFailure"));
             }
 
             String serverMechanism = saslResp.getServerMechanism();
             saslReq = new SASLRequest(serverMechanism);
             saslReq.setData(ssoToken.getTokenID().toString().getBytes("UTF-8"));
             saslReq.setRefToMessageID(saslResp.getMessageID());
             saslResp = AuthnSvcClient.sendRequest(saslReq, authURL);
             if(!saslResp.getStatusCode().equals(SASLResponse.OK)) {
                 debug.error("SOAPRequestHandler.getSASLResponse:: SASL Failure");
                 throw new SecurityException(
                         bundle.getString("SASLFailure"));
             }
             return saslResp;
 
         } catch (AuthnSvcException ae) {
             debug.error("SOAPRequestHandler.getSASLResponse:: Exception", ae);
             throw new SecurityException(
                     bundle.getString("SASLFailure"));
         } catch (UnsupportedEncodingException uae) {
             debug.error("SOAPRequestHandler.getSASLResponse:: Exception", uae);
             throw new SecurityException(
                     bundle.getString("SASLFailure"));
         }
     }
 
     /**
      * Checks if the received SOAP Message is a liberty request.
      */
     private boolean isLibertyMessage(SOAPMessage soapMessage)
             throws SecurityException {
         try {
             SOAPHeader soapHeader = soapMessage.getSOAPPart().
                     getEnvelope().getHeader();
             if(soapHeader == null) {
                 return false;
             }
             NodeList headerChildNodes = soapHeader.getChildNodes();
             if((headerChildNodes == null) ||
                     (headerChildNodes.getLength() == 0)) {
                 return false;
             }
             for(int i=0; i < headerChildNodes.getLength(); i++) {
                 Node currentNode = headerChildNodes.item(i);
                 if(currentNode.getNodeType() != Node.ELEMENT_NODE) {
                     continue;
                 }
                 if((SOAPBindingConstants.TAG_CORRELATION.equals(
                         currentNode.getLocalName())) &&
                         (SOAPBindingConstants.NS_SOAP_BINDING.equals(
                         currentNode.getNamespaceURI()))) {
                     return true;
                 }
             }
             return false;
         } catch (SOAPException se) {
             debug.error("SOAPRequest.isLibertyRequest:: SOAPException", se);
             throw new SecurityException(se.getMessage());
         }
     }
 
     /**
      * Returns the provider config from the shared state.
      */
     private ProviderConfig getProviderConfig(Map sharedMap) {
         if((sharedMap == null) || (sharedMap.isEmpty())) {
             return null;
         }
 
         try {
             String serviceName = ThreadLocalService.getServiceName();
             if(serviceName == null) {
                 serviceName = getServiceName(sharedMap);
             } else {
                 // If we find the service name, that is of TA service name
                 // So, create provider config from TA.
                if(debug.messageEnabled()) {
                     debug.message("SOAPRequestHandler.getProviderConfig: " +
                             "Service Name found in thread local" + serviceName);
                 }
                 ThreadLocalService.removeServiceName(serviceName);
                STSConfig stsConfig = (STSConfig)
                        TrustAuthorityConfig.getConfig(serviceName, 
                         TrustAuthorityConfig.STS_TRUST_AUTHORITY);
                 ProviderConfig pc =
                     ProviderConfig.getProvider(serviceName, ProviderConfig.WSC);
                 pc.setSecurityMechanisms(stsConfig.getSecurityMech());
                 pc.setRequestSignEnabled(stsConfig.isRequestSignEnabled());
                 pc.setRequestEncryptEnabled(stsConfig.isRequestEncryptEnabled());
                 pc.setDefaultKeyStore(true);
                 pc.setUsers(stsConfig.getUsers());
 
                 return pc;
             }
             if(!ProviderConfig.isProviderExists(serviceName,  
                     ProviderConfig.WSC)) {
                 return null;
             }
 
             ProviderConfig config =
                     ProviderConfig.getProvider(serviceName, ProviderConfig.WSC);
             if(!config.useDefaultKeyStore()) {
                 initializeSystemProperties(config);
             }
             return config;
         } catch (ProviderException pe) {
             WSSUtils.debug.error("SOAPRequestHandler.getProviderConfig: from" +
                     "shared map: Exception", pe);
             return null;
         } catch (IOException ie) {
             WSSUtils.debug.error("SOAPRequestHandler.getProviderConfig: from" +
                     "shared map: IOException", ie);
             return null;
         }
 
     }
 
     private String getServiceName(Map sharedMap) {
 
         // Check first if thread local has the service name.
 
         if((sharedMap == null) || (sharedMap.isEmpty())) {
             return null;
         }
         QName service = (QName)sharedMap.get("javax.xml.ws.wsdl.service");
         if(service == null) {
             return null;
         }
         return service.getLocalPart();
     }
 
     /**
      * Prints a Node tree recursively.
      *
      * @param node A DOM tree Node
      *
      * @return An xml String representation of the DOM tree.
      */
     public String print(Node node) {
         return WSSUtils.print(node);
     }
 
 // Removes the validated headers.
     private void removeValidatedHeaders(ProviderConfig config,
             SOAPMessage soapMessage) {
 
         SOAPHeader header = null;
         try {
             header = soapMessage.getSOAPPart().getEnvelope().getHeader();
         } catch (SOAPException se) {
             WSSUtils.debug.error("SOAPRequestHandler.removeValidateHeaders: " +
                     "Failed to read the SOAP Header.");
         }
         if(header != null) {
             Iterator iter = header.examineAllHeaderElements();
            while(iter.hasNext()) {
               SOAPHeaderElement headerElement = (SOAPHeaderElement)iter.next();
               if(!config.preserveSecurityHeader()) {
                  if("Security".equalsIgnoreCase(
                             headerElement.getElementName().getLocalName())) {
                         headerElement.detachNode();
                     }
                 }
               if("Correlation".equalsIgnoreCase(
                         headerElement.getElementName().getLocalName())) {
                     headerElement.detachNode();
                 }
             }
         }
     }
     
 }
