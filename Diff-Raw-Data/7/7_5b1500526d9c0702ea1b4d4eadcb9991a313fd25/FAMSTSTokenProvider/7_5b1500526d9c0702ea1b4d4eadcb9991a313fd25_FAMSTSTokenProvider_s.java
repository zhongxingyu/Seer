 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: FAMSTSTokenProvider.java,v 1.4 2008-07-02 16:57:24 mallas Exp $
  *
  */
 
 package com.sun.identity.wss.sts.spi;
 
 import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
 import com.sun.xml.ws.api.security.trust.STSTokenProvider;
 import com.sun.xml.ws.api.security.trust.WSTrustException;
 import com.sun.xml.ws.security.IssuedTokenContext;
 import com.sun.xml.ws.security.trust.GenericToken;
 import com.sun.xml.ws.security.trust.WSTrustConstants;
 import com.sun.xml.ws.security.trust.util.WSTrustUtil;
 import com.sun.xml.ws.security.trust.WSTrustVersion;
 import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
 import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
 import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
 
 import com.sun.xml.wss.XWSSecurityException;
 import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
 import com.sun.xml.wss.impl.MessageConstants;
 import com.sun.xml.wss.saml.Advice;
 import com.sun.xml.wss.saml.Assertion;
 import com.sun.xml.wss.saml.Attribute;
 import com.sun.xml.wss.saml.AttributeStatement;
 import com.sun.xml.wss.saml.AudienceRestriction;
 import com.sun.xml.wss.saml.AudienceRestrictionCondition;
 import com.sun.xml.wss.saml.AuthenticationStatement;
 import com.sun.xml.wss.saml.AuthnContext;
 import com.sun.xml.wss.saml.AuthnStatement;
 import com.sun.xml.wss.saml.Conditions;
 import com.sun.xml.wss.saml.NameID;
 import com.sun.xml.wss.saml.NameIdentifier;
 import com.sun.xml.wss.saml.SAMLAssertionFactory;
 import com.sun.xml.wss.saml.SAMLException;
 import com.sun.xml.wss.saml.SubjectConfirmation;
 import com.sun.xml.wss.saml.KeyInfoConfirmationData;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.TimeZone;
 import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
 
 import java.security.cert.X509Certificate;
 import java.util.UUID;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
 import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
 import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
 import com.sun.identity.wss.sts.STSUtils;
 import com.sun.identity.wss.sts.STSConstants;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.sun.xml.ws.security.trust.WSTrustElementFactory;
 import java.security.PrivateKey;
 import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
 import com.sun.identity.wss.sts.STSConstants;
 import com.sun.identity.plugin.session.impl.FMSessionProvider;
 import com.sun.identity.plugin.session.SessionProvider;
 import com.sun.identity.plugin.session.SessionException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.wss.sts.FAMSTSException;
 import com.sun.identity.wss.sts.STSClientUserToken;
 import com.sun.identity.wss.security.SAML11AssertionValidator;
 import com.sun.identity.wss.security.SAML2AssertionValidator;
 import com.sun.identity.saml.common.SAMLConstants;
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.wss.security.SecurityException;
 import com.sun.identity.wss.sts.config.FAMSTSConfiguration;
 import com.sun.identity.wss.security.SecurityToken;
 
 
 
 public class FAMSTSTokenProvider implements STSTokenProvider {
     
     public void generateToken(IssuedTokenContext ctx) throws WSTrustException {
     
         STSUtils.debug.message("FAMSTSTokenProvider.generateToken called.");
            
         String issuer = ctx.getTokenIssuer();
         String appliesTo = ctx.getAppliesTo();
         String tokenType = ctx.getTokenType(); 
         
         //Check
         if(tokenType != null && tokenType.equals(
                           SecurityToken.WSS_FAM_SSO_TOKEN)) {
            generateSSOToken(ctx);
            return;
         }
         String keyType = ctx.getKeyType();
         int tokenLifeSpan = 
             (int)(ctx.getExpirationTime().getTime() - ctx.getCreationTime().
             getTime());
         String confirMethod = 
             (String)ctx.getOtherProperties().get(
             IssuedTokenContext.CONFIRMATION_METHOD);
         // Bug in WSIT
        if(confirMethod.equals(
                "urn:oasis:names:tc:SAML:1.0:cm::sender-vouches")) {
            confirMethod = STSConstants.SAML_SENDER_VOUCHES_1_0;
         }
         Map<QName, List<String>> claimedAttrs = 
             (Map<QName, List<String>>) ctx.getOtherProperties().get(
             IssuedTokenContext.CLAIMED_ATTRUBUTES);
         WSTrustVersion wstVer = 
             (WSTrustVersion)ctx.getOtherProperties().get(
             IssuedTokenContext.WS_TRUST_VERSION);
         WSTrustElementFactory eleFac = 
             WSTrustElementFactory.newInstance(wstVer);
         
         // Create the KeyInfo for SubjectConfirmation
         final KeyInfo keyInfo = createKeyInfo(ctx);
         
         // Create AssertionID
         final String assertionId = "uuid-" + UUID.randomUUID().toString();
         
         if(STSUtils.debug.messageEnabled()) {
             STSUtils.debug.message("FAMSTSTokenProvider.tokenType : " 
                 + tokenType);
         }
         
         // Create SAML assertion
         Assertion assertion = null;
         
         if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
             WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
             assertion = 
                 createSAML11Assertion(wstVer, tokenLifeSpan, confirMethod, 
                 assertionId, issuer, appliesTo, keyInfo, claimedAttrs, keyType);
         } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(
             tokenType)){
             String authnCtx = 
                 (String)ctx.getOtherProperties().get(
                 IssuedTokenContext.AUTHN_CONTEXT);
             assertion = 
                 createSAML20Assertion(wstVer, tokenLifeSpan, confirMethod, 
                 assertionId, issuer, appliesTo, keyInfo, claimedAttrs, keyType, 
                 authnCtx);
         } else {
             // TBD : Need to add code for UserName token creation and 
             // X509 token creation.
             STSUtils.debug.error("FAMSTSTokenProvider.generateToken ERROR : " + 
                 "UNSUPPORTED_TOKEN_TYPE");
             throw new WSTrustException(
                 LogStringsMessages.WST_0031_UNSUPPORTED_TOKEN_TYPE(
                 tokenType, appliesTo));
         }
             
         // Get the STS's certificate and private key
         final X509Certificate stsCert = 
             (X509Certificate)ctx.getOtherProperties().get(
             IssuedTokenContext.STS_CERTIFICATE);
         final PrivateKey stsPrivKey = 
             (PrivateKey)ctx.getOtherProperties().get(
             IssuedTokenContext.STS_PRIVATE_KEY);
             
         // Sign the assertion with STS's private key
         Element signedAssertion = null;
         try{
             signedAssertion = assertion.sign(stsCert, stsPrivKey, true);
             //signedAssertion = assertion.sign(stsCert, stsPrivKey);
         } catch (SAMLException ex){
             STSUtils.debug.error("FAMSTSTokenProvider.generateToken ERROR : " + 
                 "ERROR_CREATING_SAML_ASSERTION : ", ex);
             throw new WSTrustException(
                     LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(),
                     ex);
         }
         
         if(STSUtils.debug.messageEnabled()) {
             STSUtils.debug.message("FAMSTSTokenProvider.signedAssertion : " + 
                 XMLUtils.print(signedAssertion));
         }
         ctx.setSecurityToken(new GenericToken(signedAssertion));
         
         // Create References
         String valueType = null;
         if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
             WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
             valueType = MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE;
         } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(
             tokenType)){
             valueType = 
                 MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE;
         }
         final SecurityTokenReference samlReference = 
             WSTrustUtil.createSecurityTokenReference(assertionId, valueType);
         final RequestedAttachedReference raRef =  
             eleFac.createRequestedAttachedReference(samlReference);
         final RequestedUnattachedReference ruRef =  
             eleFac.createRequestedUnattachedReference(samlReference);
         ctx.setAttachedSecurityTokenReference(samlReference);
         ctx.setUnAttachedSecurityTokenReference(samlReference);
     }
 
     public void isValideToken(IssuedTokenContext ctx) throws WSTrustException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void renewToken(IssuedTokenContext ctx) throws WSTrustException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void invalidateToken(IssuedTokenContext ctx) 
         throws WSTrustException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     protected Assertion createSAML11Assertion(final WSTrustVersion wstVer, 
         final int lifeSpan, String confirMethod, final String assertionId, 
         final String issuer, final String appliesTo, final KeyInfo keyInfo, 
         final Map<QName, List<String>> claimedAttrs, String keyType) 
         throws WSTrustException{
         
         Assertion assertion = null;
         try{
             final SAMLAssertionFactory samlFac = 
                 SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
             
             final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
             final GregorianCalendar issuerInst = 
                 new GregorianCalendar(utcTimeZone);
             final GregorianCalendar notOnOrAfter = 
                 new GregorianCalendar(utcTimeZone);
             notOnOrAfter.add(Calendar.MILLISECOND, lifeSpan);
             
             List<AudienceRestrictionCondition> arc = null;
             if (appliesTo != null){
                 arc = new ArrayList<AudienceRestrictionCondition>();
                 List<String> au = new ArrayList<String>();
                 au.add(appliesTo);
                 arc.add(samlFac.createAudienceRestrictionCondition(au));
             }
             
             final List<String> confirmMethods = new ArrayList<String>();
             if (confirMethod == null){
                 if (keyType.equals(wstVer.getBearerKeyTypeURI())){
                      confirMethod = STSConstants.SAML_BEARER_1_0;
             
                 } else {
                     confirMethod = STSConstants.SAML_HOLDER_OF_KEY_1_0;
                 }
             }
             
             Element keyInfoEle = null;
             if (keyInfo != null && !wstVer.getBearerKeyTypeURI().equals(
                 keyType)) {
                 keyInfoEle = keyInfo.getElement();
             }
             confirmMethods.add(confirMethod);
             
             final SubjectConfirmation subjectConfirm = 
                 samlFac.createSubjectConfirmation(confirmMethods, null, 
                 keyInfoEle);
             final Conditions conditions =
                 samlFac.createConditions(issuerInst, notOnOrAfter, null, arc, 
                 null);
             final Advice advice = samlFac.createAdvice(null, null, null);
             
             com.sun.xml.wss.saml.Subject subj = null;
             final List<Attribute> attrs = new ArrayList<Attribute>();
             final Set<Map.Entry<QName, List<String>>> entries = 
                 claimedAttrs.entrySet();
             for(Map.Entry<QName, List<String>> entry : entries){
                 final QName attrKey = entry.getKey();
                 final List<String> values = entry.getValue();
                 if (values != null && values.size() > 0){
                     if (STSAttributeProvider.NAME_IDENTIFIER.equals(
                             attrKey.getLocalPart()) && subj == null){
                         final NameIdentifier nameId = 
                             samlFac.createNameIdentifier(values.get(0), 
                             attrKey.getNamespaceURI(), null);
                         subj = samlFac.createSubject(nameId, subjectConfirm);
                     } else {
                         final Attribute attr = 
                             samlFac.createAttribute(attrKey.getLocalPart(), 
                             attrKey.getNamespaceURI(), values);
                         attrs.add(attr);
                     }
                 }
             }
             
             final List<Object> statements = new ArrayList<Object>();
             final AuthenticationStatement statement = 
                   samlFac.createAuthenticationStatement(null, issuerInst, 
                   subj, null, null);
             statements.add(statement); 
             if (!attrs.isEmpty()){                
                 final AttributeStatement attrStatement = 
                     samlFac.createAttributeStatement(subj, attrs);
                 statements.add(attrStatement);
             }
             assertion =
                 samlFac.createAssertion(assertionId, issuer, issuerInst, 
                 conditions, advice, statements);
         } catch(SAMLException ex){
             STSUtils.debug.error("FAMSTSTokenProvider.createSAML11Assertion : " 
                 + "ERROR_CREATING_SAML_ASSERTION : ", ex);
             throw new WSTrustException(
                 LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), 
                 ex);
         } catch(XWSSecurityException ex){
             STSUtils.debug.error("FAMSTSTokenProvider.createSAML11Assertion : " 
                 + "ERROR_CREATING_SAML_ASSERTION : ", ex);
             throw new WSTrustException(
                 LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), 
                 ex);
         }
         
         return assertion;
     }
     
     protected Assertion createSAML20Assertion(final WSTrustVersion wstVer, 
         int lifeSpan, String confirMethod, final String assertionId, 
         final String issuer, final String appliesTo, final KeyInfo keyInfo, 
         final  Map<QName, List<String>> claimedAttrs, String keyType, 
         String authnCtx) throws WSTrustException {
         
         Assertion assertion = null;
         try{
             final SAMLAssertionFactory samlFac = 
                 SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
             
             // Create Conditions
             final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
             final GregorianCalendar issueInst = 
                 new GregorianCalendar(utcTimeZone);
             final GregorianCalendar notOnOrAfter = 
                 new GregorianCalendar(utcTimeZone);
             //remove later, read this from a config by the token provider
             lifeSpan = lifeSpan + (5 * 60 * 1000);
             notOnOrAfter.add(Calendar.MILLISECOND, lifeSpan);
             
             List<AudienceRestriction> arc = null;
             KeyInfoConfirmationData keyInfoConfData = null;
             if (confirMethod == null){
                 if (keyType.equals(wstVer.getBearerKeyTypeURI())){
                      confirMethod = STSConstants.SAML_BEARER_2_0;
                     
 
                 } else {
                     confirMethod = STSConstants.SAML_HOLDER_OF_KEY_2_0;
                     if (keyInfo != null){
                         keyInfoConfData = samlFac.createKeyInfoConfirmationData(
                             keyInfo.getElement());
                     }
                 }
             }
             if (appliesTo != null){
                          arc = new ArrayList<AudienceRestriction>();
                          List<String> au = new ArrayList<String>();
                          au.add(appliesTo);
                          arc.add(samlFac.createAudienceRestriction(au));
             }
             final Conditions conditions = 
                 samlFac.createConditions(issueInst, notOnOrAfter, null, arc, 
                 null, null);
                
             final SubjectConfirmation subjectConfirm = 
                 samlFac.createSubjectConfirmation(null, keyInfoConfData, 
                 confirMethod);
             
             com.sun.xml.wss.saml.Subject subj = null;
             final List<Attribute> attrs = new ArrayList<Attribute>();
             
            // if(claimedAttrs != null) {
             final Set<Map.Entry<QName, List<String>>> entries = 
                 claimedAttrs.entrySet();
             for(Map.Entry<QName, List<String>> entry : entries){
                 final QName attrKey = entry.getKey();
                 final List<String> values = entry.getValue();
                 if (values != null && values.size() > 0){
                     if (STSAttributeProvider.NAME_IDENTIFIER.equals(
                             attrKey.getLocalPart()) && subj == null){
                         final NameID nameId = 
                             samlFac.createNameID(values.get(0), 
                             attrKey.getNamespaceURI(), null);
                         subj = samlFac.createSubject(nameId, subjectConfirm);
                     }
                     else{
                         final Attribute attr = 
                             samlFac.createAttribute(attrKey.getLocalPart(), 
                             values);
                         attrs.add(attr);
                     }
                 }
             }
             //}   
             final List<Object> statements = new ArrayList<Object>();
             AuthnContext ctx = samlFac.createAuthnContext(authnCtx, null);
             final AuthnStatement statement = 
                     samlFac.createAuthnStatement(issueInst, null, ctx);
             statements.add(statement); 
             if (!attrs.isEmpty()){                
                 final AttributeStatement attrStatement = 
                     samlFac.createAttributeStatement(attrs);
                 statements.add(attrStatement);
             }
             
             final NameID issuerID = samlFac.createNameID(issuer, null, null);
             
             // Create Assertion
             assertion =
                 samlFac.createAssertion(assertionId, issuerID, issueInst, 
                 conditions, null, subj, statements);
         } catch(SAMLException ex){
             STSUtils.debug.error("FAMSTSTokenProvider.createSAML20Assertion " + 
                 " ERROR : ERROR_CREATING_SAML_ASSERTION : ", ex);
             throw new WSTrustException(
                 LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), 
                 ex);
         } catch(XWSSecurityException ex){
             STSUtils.debug.error("FAMSTSTokenProvider.createSAML20Assertion " + 
                 " ERROR : ERROR_CREATING_SAML_ASSERTION : ", ex);
             throw new WSTrustException(
                 LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), 
                 ex);
         }
         
         return assertion;
     }
      
     private KeyInfo createKeyInfo(final IssuedTokenContext ctx) throws 
         WSTrustException {
         
         Element kiEle = 
             (Element)ctx.getOtherProperties().get("ConfirmationKeyInfo");
         if (kiEle != null){
             try {
                 return new KeyInfo(kiEle, null);
             } catch(XMLSecurityException ex){
                 STSUtils.debug.error("FAMSTSTokenProvider.createKeyInfo : " + 
                 "UNABLE_GET_CLIENT_CERT : ", ex);
                 throw new WSTrustException(
                     LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT(), ex);
             }
         }
         final DocumentBuilderFactory docFactory = 
             DocumentBuilderFactory.newInstance();
         Document doc = null;
         try{
             doc = docFactory.newDocumentBuilder().newDocument();
         }catch(ParserConfigurationException ex){
             STSUtils.debug.error("FAMSTSTokenProvider.createKeyInfo : " + 
                 "ERROR_CREATING_DOCFACTORY : ", ex);
             throw new WSTrustException(
                 LogStringsMessages.WST_0039_ERROR_CREATING_DOCFACTORY(), ex);
         }
         
         final String appliesTo = ctx.getAppliesTo();
         final KeyInfo keyInfo = new KeyInfo(doc);
         String keyType = ctx.getKeyType();
         WSTrustVersion wstVer = 
             (WSTrustVersion)ctx.getOtherProperties().get(
             IssuedTokenContext.WS_TRUST_VERSION);
         if (wstVer.getSymmetricKeyTypeURI().equals(keyType)){
             final byte[] key = ctx.getProofKey();
             try {
                 final EncryptedKey encKey = 
                     WSTrustUtil.encryptKey(doc, key, 
                     (X509Certificate)ctx.getOtherProperties().get(
                     IssuedTokenContext.TARGET_SERVICE_CERTIFICATE));
                  keyInfo.add(encKey);
             } catch (Exception ex) {
                  STSUtils.debug.error("FAMSTSTokenProvider.createKeyInfo : " + 
                 "ERROR_ENCRYPT_PROOFKEY : ", ex);
                  throw new WSTrustException(
                      LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(
                      appliesTo), ex);
             }
         } else if(wstVer.getPublicKeyTypeURI().equals(keyType)){
             final X509Data x509data = new X509Data(doc);
             try {
                 x509data.addCertificate(ctx.getRequestorCertificate());
             } catch(XMLSecurityException ex) {
                 STSUtils.debug.error("FAMSTSTokenProvider.createKeyInfo : " + 
                 "UNABLE_GET_CLIENT_CERT : ", ex);
                 throw new WSTrustException(
                     LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT(), ex);
             }
             keyInfo.add(x509data);
         }
         
         return keyInfo;
     }
     
     /**
      * Generates FAM SSOToken by consuming SAML Assertion.
      * @param ctx Issued Token Context from WS-Trust Request
      * @throws com.sun.xml.ws.api.security.trust.WSTrustException
      */
     private void generateSSOToken(IssuedTokenContext ctx) 
              throws WSTrustException {
         
         javax.security.auth.Subject subject = ctx.getRequestorSubject();
         if(subject == null) {
            throw new WSTrustException("Subject is null"); 
         }        
         
         String subjectName = null;
         Map attributeMap = null;
         Map config = new HashMap();
         FAMSTSConfiguration stsConfig = new FAMSTSConfiguration();
         config.put(STSConstants.TRUSTED_ISSUERS, stsConfig.getTrustedIssuers());
         config.put(STSConstants.TRUSTED_IPADDRESSES,
                                    stsConfig.getTrustedIPAddresses());
         
         Iterator iter = subject.getPublicCredentials().iterator();
         while(iter.hasNext()) {
             Object object = iter.next();
             if(object instanceof Element) {
                Element famToken = (Element)object;
                if(!famToken.getLocalName().equals("FAMToken")) {
                   continue; 
                }
                Element assertionE = null;
                try {
                    STSClientUserToken oboToken = 
                            new STSClientUserToken(famToken);
                    String tokenID = oboToken.getTokenId();
                    assertionE = XMLUtils.toDOMDocument(
                            tokenID, STSUtils.debug).getDocumentElement();                   
                } catch (FAMSTSException se) {
                    throw new WSTrustException(se.getMessage());
                }
                if(assertionE == null) {
                   throw new WSTrustException(
                           STSUtils.bundle.getString("nullAssertion"));
                }
                if(assertionE.getLocalName().equals("Assertion")) {                   
                   String namespace = assertionE.getNamespaceURI();
                   try {
                       if(SAMLConstants.assertionSAMLNameSpaceURI.equals(
                               namespace)) {                
                          SAML11AssertionValidator validator = 
                              new SAML11AssertionValidator(assertionE, config);
                          subjectName = validator.getSubjectName();
                          attributeMap = validator.getAttributes();                      
                       } else if (SAML2Constants.ASSERTION_NAMESPACE_URI.equals(
                               namespace)) {
                          SAML2AssertionValidator validator =
                              new SAML2AssertionValidator(assertionE, config);
                          subjectName = validator.getSubjectName();
                          attributeMap = validator.getAttributes();
                       }
                   } catch (SecurityException se) {                      
                       throw new WSTrustException(se.getMessage());
                   }
                }
             }
         }
         if(subjectName == null) {
            throw new WSTrustException(
                    STSUtils.bundle.getString("assertion subject is null")); 
         }
         Map info = new HashMap();
         info.put(SessionProvider.REALM, "/");
         info.put(SessionProvider.PRINCIPAL_NAME, subjectName);
         info.put(SessionProvider.AUTH_LEVEL, "0");
         FMSessionProvider sessionProvider = new FMSessionProvider();
         try {
             SSOToken ssoToken = (SSOToken)sessionProvider.createSession(
                     info, null, null, null);
             if(attributeMap != null && !attributeMap.isEmpty()) {
                for(Iterator attrIter =  attributeMap.keySet().iterator();
                             attrIter.hasNext();) {
                    String attrName = (String)attrIter.next();
                    String attrValue = (String)attributeMap.get(attrName);
                    ssoToken.setProperty(attrName, attrValue);
                    
                }
             }
             STSClientUserToken wscToken = new STSClientUserToken();
             wscToken.init(ssoToken);
             ctx.setSecurityToken(wscToken);            
         } catch (SessionException se) {
             STSUtils.debug.error("FAMSTSTokenProvider.generateSSOToken: " +
                     "session exception ", se);
             throw new WSTrustException(se.getMessage());                    
         } catch (FAMSTSException fe) {
             STSUtils.debug.error("FAMSTSTokenProvider.generateSSOToken: " +
                     "FAMSTSException ", fe);
             throw new WSTrustException(fe.getMessage());                    
         } catch (SSOException ssoe) {
             STSUtils.debug.error("FAMSTSTokenProvider.generateSSOToken: " +
                     "SSOException ", ssoe);
             throw new WSTrustException(ssoe.getMessage());                    
         }
     }
 }
