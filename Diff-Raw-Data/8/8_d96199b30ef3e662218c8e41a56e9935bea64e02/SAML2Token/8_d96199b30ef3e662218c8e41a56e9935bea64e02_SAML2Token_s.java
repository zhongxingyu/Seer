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
 * $Id: SAML2Token.java,v 1.2 2007-09-13 07:24:21 mrudul_uchil Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.wss.security;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.security.cert.X509Certificate;
 import java.math.BigInteger;
 import java.security.PublicKey;
 import java.security.PrivateKey;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.DSAParams;
 import java.security.interfaces.RSAPublicKey;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.iplanet.sso.SSOException;
 import com.sun.identity.common.SystemConfigurationUtil;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.sun.identity.shared.encode.Base64;
 
 import com.sun.identity.saml2.assertion.NameID;
 import com.sun.identity.saml2.assertion.Assertion;
 import com.sun.identity.saml2.assertion.AssertionFactory;
 import com.sun.identity.saml2.assertion.AuthnContext;
 import com.sun.identity.saml2.assertion.AuthnStatement;
 import com.sun.identity.saml2.assertion.Issuer;
 import com.sun.identity.saml2.assertion.Subject;
 import com.sun.identity.saml2.assertion.SubjectConfirmation;
 import com.sun.identity.saml2.assertion.SubjectConfirmationData;
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2Utils;
 
 
 /**
  * This class <code>SAML2Token</code> represents a SAML2
  * token that can be inserted into web services security header
  * for message level security.
  *
  * <p>This class implements <code>SecurityToken</code> and can be
  * created through security token factory. 
  */
 public class SAML2Token implements SecurityToken {
     
     private String authType = "";
     private String authTime = "";
     private Assertion assertion;
     private String certAlias = "";
     private static final String KEY_INFO_TYPE =
          "com.sun.identity.liberty.ws.security.keyinfotype";
     private static String keyInfoType = SystemConfigurationUtil.getProperty(
                                           KEY_INFO_TYPE);
     private static AssertionFactory factory = AssertionFactory.getInstance();
     
     /**
      * Constructor that initializes the SAML2Token.
      */ 
      public SAML2Token(SAML2TokenSpec spec, 
                   SSOToken ssoToken) throws SecurityException {
  
          if(spec == null) {
             WSSUtils.debug.error("SAML2Token: constructor: SAML2" +
                   " Token specification is null");
             throw new SecurityException(
                    WSSUtils.bundle.getString("tokenSpecNotSpecified"));
          }
 
          validateSSOToken(ssoToken);
          createAssertion(spec);
      }
      
      public SAML2Token(Element element) 
                    throws SAML2Exception {
          assertion = factory.createAssertion(element);         
      }
      
      /**
        * Validates the SSOtoken and extract the required properties.
        */
       private void validateSSOToken(SSOToken ssoToken) 
                       throws SecurityException {
          try {
              SSOTokenManager.getInstance().validateToken(ssoToken);
              authType = ssoToken.getAuthType();
              authTime =  ssoToken.getProperty("authInstant");
 
          } catch (SSOException se) {
              WSSUtils.debug.error("AssertionToken.validateSSOToken: " +
                "SSOException", se);
              throw new SecurityException(
                    WSSUtils.bundle.getString("invalidSSOToken"));
          }
       }
       
       /** 
        * Returns the security token type.
        * @return String SAMLToken type.
        */
       public String getTokenType() {
           return SecurityToken.WSS_SAML2_TOKEN;
       }
 
       /**
        * Create Assertion using SAML2 token specification
        */
       private void createAssertion(SAML2TokenSpec spec) 
               throws SecurityException {
           assertion = factory.createAssertion();          
           SecurityMechanism securityMechanism = spec.getSecurityMechanism();
           NameID nameIdentifier = spec.getSenderIdentity();
           certAlias = spec.getSubjectCertAlias();
 
           if((nameIdentifier == null) || (securityMechanism == null)
                   || (certAlias == null)) {
              throw new SecurityException(
                    WSSUtils.bundle.getString("invalidSAML2TokenSpec"));
           }
 
           String confirmationMethod = 
                 getConfirmationMethod(securityMechanism.getURI());
 
           // TODO: Read the issuer from the trustd authority configuration
           // when the STS is ready.
           try {
               assertion.setVersion("2.0");
              assertion.setID(SAML2Utils.generateID());
               Issuer issuer = factory.createIssuer();
               issuer.setValue(SystemConfigurationUtil.getProperty(
                   Constants.AM_SERVER_HOST));          
               assertion.setIssuer(issuer);
           
               Date issueInstant = new Date();
               assertion.setIssueInstant(issueInstant);
               assertion.setSubject(
                     createSubject(nameIdentifier, confirmationMethod));
               List authnStatements = new ArrayList();          
               authnStatements.add(createAuthnStatement());
           
               if(WSSUtils.debug.messageEnabled()) {
                  WSSUtils.debug.message("SAML2Token.createAssertion: " +
                  "Assertion constructs:\n" +
                  "Confirmation method: " + confirmationMethod + "\n" +
                  "Issuer: " + issuer + "\n");
                }
                                         
                assertion.setAuthnStatements(authnStatements);
                
           } catch (SAML2Exception se) {
               WSSUtils.debug.error("SAML2Token.createAssertion:", se);
               throw new SecurityException(WSSUtils.bundle.getString(
                       "unableToGenerateAssertion"));
           }
       }
             
       /**
        * Creates a subject
        */
       private Subject createSubject (
             NameID nameIdentifier,
             String confirmationMethod) throws SecurityException {
 
           try {
               Subject subject = factory.createSubject();
               subject.setNameID(nameIdentifier);              
               if(confirmationMethod == null) {
                  throw new SecurityException(
                        WSSUtils.bundle.getString("nullConfirmationMethod"));
               }
 
               SubjectConfirmation subConfirmation = 
                       factory.createSubjectConfirmation();              
               SubjectConfirmationData confirmationData =
                       factory.createSubjectConfirmationData();
               
               if(confirmationMethod.equals(                      
                    SAML2Constants.SUBJECT_CONFIRMATION_METHOD_HOLDER_OF_KEY)) {
                  subConfirmation.setMethod(confirmationMethod);
                 confirmationData.setContentType(
                         WSSConstants.KEY_INFO_DATA_TYPE);
                  confirmationData.getContent().add(createKeyInfo()); 
                  subConfirmation.setSubjectConfirmationData(confirmationData);
  
               } else if(confirmationMethod.equals(
                    SAML2Constants.SUBJECT_CONFIRMATION_METHOD_SENDER_VOUCHES)) {
                  subConfirmation.setMethod(confirmationMethod);
 
               } else {
                  throw new SecurityException(
                        WSSUtils.bundle.getString("invalidConfirmationMethod"));
               }
               List list = new ArrayList();
               list.add(subConfirmation);
               subject.setSubjectConfirmation(list);
               return subject;
           } catch (SAML2Exception se) {
               WSSUtils.debug.error("AssertionToken.getAuthenticationStatement:"+
               "Failed to generate the authentication statement.", se);
               throw new SecurityException(
                        WSSUtils.bundle.getString("unabletoGenerateAssertion"));
           }
 
 
       }
       
       /**
        * creates an authentication statement.
        */
       private AuthnStatement createAuthnStatement() throws SecurityException {
           try {
               AuthnStatement authnStatement = factory.createAuthnStatement();
               authnStatement.setAuthnInstant(new Date());
               AuthnContext authnContext = factory.createAuthnContext();
               authnContext.setAuthnContextClassRef(
                   WSSConstants.CLASSREF_AUTHN_CONTEXT_SOFTWARE_PKI);
               authnStatement.setAuthnContext(authnContext);
               return authnStatement;
           } catch (SAML2Exception se) {
               WSSUtils.debug.error("SAML2Token.createAuthnStatement: SAML2" +
                       "Exception ", se);
               throw new SecurityException(
                       WSSUtils.bundle.getString("unableToGenerateAssertion"));
           }
 
       }
       
       public  Assertion getAssertion() {
           return assertion;
       }
       
       /**
        * Returns the confirmation method for the given security mech.
        */
       private String getConfirmationMethod(String securityURI) 
                throws SecurityException {
 
           if(securityURI == null) {
              throw new SecurityException(
                   WSSUtils.bundle.getString("nullSecurityMechanism"));
           }
 
           if(securityURI.equals(SecurityMechanism.WSS_NULL_SAML2_HK_URI)||
              securityURI.equals(SecurityMechanism.WSS_TLS_SAML2_HK_URI) ||
              securityURI.equals(SecurityMechanism.WSS_CLIENT_TLS_SAML2_HK_URI)){
              return SAML2Constants.SUBJECT_CONFIRMATION_METHOD_HOLDER_OF_KEY;
 
           } else if(
              securityURI.equals(SecurityMechanism.WSS_NULL_SAML2_SV_URI)||
              securityURI.equals(SecurityMechanism.WSS_TLS_SAML2_SV_URI) ||
              securityURI.equals(SecurityMechanism.WSS_CLIENT_TLS_SAML2_SV_URI)){
              return SAML2Constants.SUBJECT_CONFIRMATION_METHOD_SENDER_VOUCHES;
 
           } else {
              throw new SecurityException(
                    WSSUtils.bundle.getString("invalidConfirmationMethod"));
           }
       }
       
       /**
        * creates key info
        */
       private Element createKeyInfo() throws SecurityException {
           X509Certificate cert = getX509Certificate();
           Document doc = null;
           try {
               doc = XMLUtils.newDocument();
           } catch (Exception e) {
               throw new SecurityException(e.getMessage());
           }
 
           String keyNameTextString = null;
           String base64CertString = null;
 
           PublicKey pk = null;
           try {
               pk = cert.getPublicKey();
               keyNameTextString = cert.getSubjectDN().getName();
               base64CertString = Base64.encode(cert.getEncoded());
           } catch (Exception e) {
               WSSUtils.debug.error("SAML2Token.createKeyInfo: ", e);
               throw new SecurityException(e.getMessage());
           }
 
           Element keyInfo = doc.createElementNS(
                             SAML2Constants.NS_XMLSIG,
                             WSSConstants.TAG_KEYINFO);
           keyInfo.setAttribute("xmlns", SAML2Constants.NS_XMLSIG);
 
           if ( (keyInfoType!=null) && 
                (keyInfoType.equalsIgnoreCase("certificate")) ) {
                 //put Certificate in KeyInfo
                 Element x509Data = doc.createElementNS(
                                 SAML2Constants.NS_XMLSIG,
                                 WSSConstants.TAG_X509DATA);
                 Element x509Certificate = doc.createElementNS(
                                 SAML2Constants.NS_XMLSIG,
                                 WSSConstants.TAG_X509CERTIFICATE);
                 Text certText = doc.createTextNode(base64CertString);
             x509Certificate.appendChild(certText);
             keyInfo.appendChild(x509Data).appendChild(x509Certificate);
         } else {
             //put public key in keyinfo
             Element keyName = doc.createElementNS(
                             SAML2Constants.NS_XMLSIG,
                             WSSConstants.TAG_KEYNAME);
             Text keyNameText = doc.createTextNode(keyNameTextString);
 
             Element keyvalue = doc.createElementNS(
                             SAML2Constants.NS_XMLSIG,
                             WSSConstants.TAG_KEYVALUE);
 
             if (pk.getAlgorithm().equals("DSA")) {
                 DSAPublicKey dsakey = (DSAPublicKey) pk;
                 DSAParams dsaParams = dsakey.getParams();
                 BigInteger _p = dsaParams.getP();
                 BigInteger _q = dsaParams.getQ();
                 BigInteger _g = dsaParams.getG();
                 BigInteger _y = dsakey.getY();
                 Element DSAKeyValue = doc.createElementNS(
                             SAML2Constants.NS_XMLSIG
                             , "DSAKeyValue");
                 Element p = doc.createElementNS(
                                 SAML2Constants.NS_XMLSIG, "P");
                 Text value_p =
                         doc.createTextNode(Base64.encode(_p.toByteArray()));
                 p.appendChild(value_p);
                 DSAKeyValue.appendChild(p);
 
                 Element q = doc.createElementNS(
                                 SAML2Constants.NS_XMLSIG, "Q");
                 Text value_q =
                         doc.createTextNode(Base64.encode(_q.toByteArray()));
                 q.appendChild(value_q);
                 DSAKeyValue.appendChild(q);
 
                 Element g = doc.createElementNS(
                                 SAML2Constants.NS_XMLSIG, "G");
                 Text value_g =
                         doc.createTextNode(Base64.encode(_g.toByteArray()));
                 g.appendChild(value_g);
                 DSAKeyValue.appendChild(g);
 
                 Element y = doc.createElementNS(
                                 SAML2Constants.NS_XMLSIG, "Y");
                 Text value_y =
                         doc.createTextNode(Base64.encode(_y.toByteArray()));
                 y.appendChild(value_y);
                 DSAKeyValue.appendChild(y);
                 keyvalue.appendChild(DSAKeyValue);
 
             } else {
                 // It is RSA
                 RSAPublicKey rsakey = (RSAPublicKey) pk;
                 BigInteger exponent = rsakey.getPublicExponent();
                 BigInteger modulus  = rsakey.getModulus();
                 Element RSAKeyValue = doc.createElementNS(
                                         SAML2Constants.NS_XMLSIG
                                         , "RSAKeyValue");
                 Element modulusNode = doc.createElementNS(
                                         SAML2Constants.NS_XMLSIG
                                         , "Modulus");
                 Element exponentNode = doc.createElementNS(
                                         SAML2Constants.NS_XMLSIG
                                         , "Exponent");
                 RSAKeyValue.appendChild(modulusNode);
                 RSAKeyValue.appendChild(exponentNode);
                 Text modulusValue =
                     doc.createTextNode(Base64.encode(modulus.toByteArray()));
                 modulusNode.appendChild(modulusValue);
                 Text exponentValue =
                     doc.createTextNode(Base64.encode(exponent.toByteArray()));
                 exponentNode.appendChild(exponentValue);
                 keyvalue.appendChild(RSAKeyValue);
             }
 
             keyInfo.appendChild(keyName).appendChild(keyNameText);
             keyInfo.appendChild(keyvalue);
         }
         return keyInfo;
     }
       
       /**
        * Returns X509 certificate of the authenticated subject.
        */
       public X509Certificate getX509Certificate() throws SecurityException {
           X509Certificate cert = 
                 AMTokenProvider.getKeyProvider().getX509Certificate(certAlias);
           if(cert == null) {
              WSSUtils.debug.error("SAML2Token.getX509Certificate: " +
              "Could not get certificate for alias : " + certAlias);
              throw new SecurityException(
                    WSSUtils.bundle.getString("noCertificate"));
           }
           return cert;
       }
       
       /**
        * Returns DOM element for the SAML2 token
        * @return the DOM <code>Element</code> element
        * @exception SecurityException if there is a failure.
        */
       public Element toDocumentElement() throws SecurityException {
           Document document = null;
           try {
               document = XMLUtils.toDOMDocument(
                    assertion.toXMLString(true, true), WSSUtils.debug);
           } catch (SAML2Exception se) {
               WSSUtils.debug.error("SAML2Token.toDocumentElement: failed", se);
               throw new SecurityException(
                  WSSUtils.bundle.getString("cannotConvertToDocument"));
           }
           
           if(document == null) {
              throw new SecurityException(
                  WSSUtils.bundle.getString("cannotConvertToDocument"));
           }
           return document.getDocumentElement();          
       }
       
      /**
       * Signs the SAML2 Token.
       *
       * @exception SecurityException if unable to sign the assertion.
       */
      public void sign(String alias) throws SecurityException {
          try {
              X509Certificate x509Cert = 
                  AMTokenProvider.getKeyProvider().getX509Certificate(certAlias);
              PrivateKey privateKey = 
                      AMTokenProvider.getKeyProvider().getPrivateKey(alias);
              assertion.sign(privateKey, x509Cert);            
          } catch (SAML2Exception se) {
             WSSUtils.debug.error("AssertionToken.sign: exception", se);
             throw new SecurityException(
                       WSSUtils.bundle.getString("unabletoSign"));
          }
      }
      
      /**
       * Returns true if the SAML2 token is of type sender vouches
       */
      public boolean isSenderVouches() {
          
          Subject subject = assertion.getSubject();
          List list = 
                  subject.getSubjectConfirmation();
          if(list == null || list.isEmpty()) {
             return false;
          }         
          SubjectConfirmation subjConfirmation = 
                  (SubjectConfirmation)list.get(0);
          String confirmationMethod = subjConfirmation.getMethod();
          if (SAML2Constants.SUBJECT_CONFIRMATION_METHOD_SENDER_VOUCHES.
                  equals(confirmationMethod)) {
              return true;
          }
          return false;
      }
 }
