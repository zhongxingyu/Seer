 /*
  * Copyright 2004,2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.rahas.impl;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMNode;
 import org.apache.axiom.soap.SOAPEnvelope;
 import org.apache.axis2.context.MessageContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.rahas.*;
 import org.apache.rahas.impl.util.*;
 import org.apache.ws.security.components.crypto.Crypto;
 import org.apache.ws.security.util.XmlSchemaDateFormat;
 import org.apache.xml.security.c14n.Canonicalizer;
 import org.apache.xml.security.signature.XMLSignature;
 import org.joda.time.DateTime;
 import org.opensaml.Configuration;
 import org.opensaml.common.SAMLException;
 import org.opensaml.common.SAMLObjectBuilder;
 import org.opensaml.saml2.core.*;
 import org.opensaml.xml.XMLObjectBuilderFactory;
 import org.opensaml.xml.io.*;
 import org.opensaml.xml.schema.XSString;
 import org.opensaml.xml.schema.impl.XSStringBuilder;
 import org.opensaml.xml.signature.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.security.PrivateKey;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.X509Certificate;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * WS-Trust based SAML2 token issuer. This issuer will generate request security token responses with SAML2
  * assertions.
  */
 public class SAML2TokenIssuer implements TokenIssuer {
 
     private String configParamName;
 
     private OMElement configElement;
 
     private String configFile;
 
     protected List<Signature> signatureList = new ArrayList<Signature>();
 
     private boolean isSymmetricKeyBasedHoK = false;
 
     private SAMLTokenIssuerConfig tokenIssuerConfiguration;
 
     private static Log log = LogFactory.getLog(SAML2TokenIssuer.class);
 
     /**
      * This is the main method which issues SAML2 assertions as security token responses. This method will
      * read issuer configuration and in message context properties (Basically request security token properties)
      * and will create a security token response with SAML2 assertion. The attributes are retrieved from a callback
      * class.
      * @param data A populated <code>RahasData</code> instance
      * @return A SOAP message with security token response (as per ws-trust spec) with a SAML2 assertion.
      * @throws TrustException If an error occurred while creating the response.
      */
     public SOAPEnvelope issue(RahasData data) throws TrustException {
         MessageContext inMsgCtx = data.getInMessageContext();
 
         this.tokenIssuerConfiguration = CommonUtil.getTokenIssuerConfiguration(this.configElement,
                 this.configFile, inMsgCtx.getParameter(this.configParamName));
 
         if (tokenIssuerConfiguration == null) {
 
             if (log.isDebugEnabled()) {
                 String parameterName;
                 if (this.configElement != null) {
                     parameterName = "OMElement - " + this.configElement.toString();
                 } else if (this.configFile != null) {
                     parameterName = "File - " + this.configFile;
                 } else if (this.configParamName != null) {
                     parameterName = "With message context parameter name - " + this.configParamName;
                 } else {
                     parameterName = "No method to build configurations";
                 }
 
                 log.debug("Unable to build token configurations, " + parameterName);
             }
 
             throw new TrustException("configurationIsNull");
         }
 
         SOAPEnvelope env = TrustUtil.createSOAPEnvelope(inMsgCtx
                 .getEnvelope().getNamespace().getNamespaceURI());
 
         Crypto crypto = tokenIssuerConfiguration.getIssuerCrypto(inMsgCtx
                 .getAxisService().getClassLoader());
 
         // Get the document
         Document doc = ((Element) env).getOwnerDocument();
 
         // Get the key size and create a new byte array of that size
         int keySize = data.getKeySize();
         keySize = (keySize == -1) ? tokenIssuerConfiguration.getKeySize() : keySize;
 
         data.setKeySize(keySize);
 
         // Build the assertion
         Assertion assertion = buildAssertion(doc, crypto, data);
 
         // Sign the assertion
         Assertion signedAssertion = signAssertion(doc, assertion, crypto);
 
         return createRequestSecurityTokenResponse(data, signedAssertion, env);
 
     }
 
     /**
      * This method prepares the final response. This method will create a request security token response as
      * specified in WS-Trust specification. The equivalent XML would take following format,
      * <wst:RequestSecurityTokenResponse xmlns:wst="...">
      *       <wst:TokenType>...</wst:TokenType>
      *       <wst:RequestedSecurityToken>...</wst:RequestedSecurityToken>
      *       ...
      *       <wsp:AppliesTo xmlns:wsp="...”>...</wsp:AppliesTo>
      *       <wst:RequestedAttachedReference>
      *       ...
      *       </wst:RequestedAttachedReference>
      *       <wst:RequestedUnattachedReference>
      *       ...
      *       </wst:RequestedUnattachedReference>
      *       <wst:RequestedProofToken>...</wst:RequestedProofToken>
      *       <wst:Entropy>
      *       <wst:BinarySecret>...</wst:BinarySecret>
      *       </wst:Entropy>
      *       <wst:Lifetime>...</wst:Lifetime>
      *   </wst:RequestSecurityTokenResponse>
      *
      *   Thus the RequestedSecurityToken will have SAML2 assertion passed.
      * @param rahasData The configuration data which comes with RST
      * @param assertion OpenSAM representation of SAML2 assertion.
      * @param soapEnvelope SOAP message envelope
      * @return SOAPEnvelope which includes RequestSecurityTokenResponse
      * @throws TrustException If an error occurred while creating RequestSecurityTokenResponse.
      */
     protected SOAPEnvelope createRequestSecurityTokenResponse(RahasData rahasData,
                                                       Assertion assertion,
                                                       SOAPEnvelope soapEnvelope) throws TrustException {
 
         OMElement requestSecurityTokenResponse;
         int wstVersion = rahasData.getVersion();
         if (RahasConstants.VERSION_05_02 == wstVersion) {
             requestSecurityTokenResponse = TrustUtil.createRequestSecurityTokenResponseElement(
                     wstVersion, soapEnvelope.getBody());
         } else {
             OMElement requestSecurityTokenResponseCollectionElement = TrustUtil
                     .createRequestSecurityTokenResponseCollectionElement(
                             wstVersion, soapEnvelope.getBody());
             requestSecurityTokenResponse = TrustUtil.createRequestSecurityTokenResponseElement(
                     wstVersion, requestSecurityTokenResponseCollectionElement);
         }
 
         TrustUtil.createTokenTypeElement(wstVersion, requestSecurityTokenResponse).setText(
                 RahasConstants.TOK_TYPE_SAML_20);
 
         if (rahasData.getKeyType().endsWith(RahasConstants.KEY_TYPE_SYMM_KEY)) {
             TrustUtil.createKeySizeElement(wstVersion, requestSecurityTokenResponse, rahasData.getKeySize());
         }
 
         if (tokenIssuerConfiguration.isAddRequestedAttachedRef()) {
             TrustUtil.createRequestedAttachedRef(wstVersion, requestSecurityTokenResponse, "#"
                     + assertion.getID(), RahasConstants.TOK_TYPE_SAML_20);
         }
 
         if (tokenIssuerConfiguration.isAddRequestedUnattachedRef()) {
             TrustUtil.createRequestedUnattachedRef(wstVersion, requestSecurityTokenResponse,
                     assertion.getID(), RahasConstants.TOK_TYPE_SAML_20);
         }
 
         if (rahasData.getAppliesToAddress() != null) {
             TrustUtil.createAppliesToElement(requestSecurityTokenResponse, rahasData
                     .getAppliesToAddress(), rahasData.getAddressingNs());
         }
 
         // Use GMT time in milliseconds
         DateFormat xmlSchemaDateFormat = new XmlSchemaDateFormat();
 
         // Add the Lifetime element
         TrustUtil.createLifetimeElement(wstVersion, requestSecurityTokenResponse, xmlSchemaDateFormat
                 .format(rahasData.getAssertionCreatedDate()),
                 xmlSchemaDateFormat.format(rahasData.getAssertionExpiringDate()));
 
         // Create the RequestedSecurityToken element and add the SAML token
         // to it
         OMElement requestedSecurityTokenElement = TrustUtil
                 .createRequestedSecurityTokenElement(wstVersion, requestSecurityTokenResponse);
 
         Element assertionElement = assertion.getDOM();
 
         requestedSecurityTokenElement.addChild((OMNode)assertionElement);
 
         // Store the token
         Token assertionToken = new Token(assertion.getID(),
                 (OMElement) assertionElement, rahasData.getAssertionCreatedDate(),
                 rahasData.getAssertionExpiringDate());
 
         // At this point we definitely have the secret
         // Otherwise it should fail with an exception earlier
         assertionToken.setSecret(rahasData.getEphmeralKey());
         TrustUtil.getTokenStore(rahasData.getInMessageContext()).add(assertionToken);
 
         if (rahasData.getKeyType().endsWith(RahasConstants.KEY_TYPE_SYMM_KEY)
                 && tokenIssuerConfiguration.getKeyComputation()
                 != SAMLTokenIssuerConfig.KeyComputation.KEY_COMP_USE_REQ_ENT) {
 
             Document doc = ((Element) soapEnvelope).getOwnerDocument();
 
             // Add the RequestedProofToken
             TokenIssuerUtil.handleRequestedProofToken(rahasData, wstVersion,
                     tokenIssuerConfiguration,
                     requestSecurityTokenResponse, assertionToken, doc);
         }
 
         return soapEnvelope;
 
     }
 
     /**
      * This methods builds the SAML2 assertion. The equivalent XML would look as follows,
      * <saml:Assertion
      *      xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
      *      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      *      ID="b07b804c-7c29-ea16-7300-4f3d6f7928ac"
      *      Version="2.0"
      *      IssueInstant="2004-12-05T09:22:05Z">
      *      <saml:Issuer>https://idp.example.org/SAML2</saml:Issuer>
      *      <ds:Signature
      *        xmlns:ds="http://www.w3.org/2000/09/xmldsig#">...</ds:Signature>
      *      <saml:Subject>
      *        <saml:NameID
      *          Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient">
      *          3f7b3dcf-1674-4ecd-92c8-1544f346baf8
      *        </saml:NameID>
      *        <saml:SubjectConfirmation
      *          Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
      *          <saml:SubjectConfirmationData
      *            InResponseTo="aaf23196-1773-2113-474a-fe114412ab72"
      *            Recipient="https://sp.example.com/SAML2/SSO/POST"
      *            NotOnOrAfter="2004-12-05T09:27:05Z"/>
      *        </saml:SubjectConfirmation>
      *      </saml:Subject>
      *      <saml:Conditions
      *        NotBefore="2004-12-05T09:17:05Z"
      *        NotOnOrAfter="2004-12-05T09:27:05Z">
      *        <saml:AudienceRestriction>
      *          <saml:Audience>https://sp.example.com/SAML2</saml:Audience>
      *        </saml:AudienceRestriction>
      *      </saml:Conditions>
      *      <saml:AuthnStatement
      *        AuthnInstant="2004-12-05T09:22:00Z"
      *        SessionIndex="b07b804c-7c29-ea16-7300-4f3d6f7928ac">
      *        <saml:AuthnContext>
      *          <saml:AuthnContextClassRef>
      *            urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
      *         </saml:AuthnContextClassRef>
      *        </saml:AuthnContext>
      *      </saml:AuthnStatement>
      *      <saml:AttributeStatement>
      *        <saml:Attribute
      *          xmlns:x500="urn:oasis:names:tc:SAML:2.0:profiles:attribute:X500"
      *          x500:Encoding="LDAP"
      *          NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      *          Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.1"
      *          FriendlyName="eduPersonAffiliation">
      *          <saml:AttributeValue
      *            xsi:type="xs:string">member</saml:AttributeValue>
      *          <saml:AttributeValue
      *            xsi:type="xs:string">staff</saml:AttributeValue>
      *        </saml:Attribute>
      *      </saml:AttributeStatement>
      *    </saml:Assertion>
      *
      *    Reference - en.wikipedia.org/wiki/SAML_2.0#SAML_2.0_Assertions
      * @param doc The Document which comprises SAML 2 assertion.
      * @param crypto Crypto properties.
      * @param data The RST data and other configuration information.
      * @return OpenSAML representation of an Assertion.
      * @throws TrustException If an error occurred while creating the Assertion.
      */
     protected Assertion buildAssertion(Document doc, Crypto crypto, RahasData data) throws TrustException {
         //Build the assertion
         Assertion assertion = SAML2Utils.createAssertion();
 
         Issuer issuer = SAML2Utils.createIssuer(this.tokenIssuerConfiguration.getIssuerName());
         assertion.setIssuer(issuer);
 
         // Validity period
         DateTime creationDate = new DateTime();
         DateTime expirationDate = new DateTime(creationDate.getMillis() + tokenIssuerConfiguration.getTtl());
 
         data.setAssertionCreatedDate(creationDate.toDate());
         data.setAssertionExpiringDate(expirationDate.toDate());
 
         // Set the issued time.
         assertion.setIssueInstant(creationDate);
 
         // These variables are used to build the trust assertion
         Conditions conditions = SAML2Utils.createConditions(creationDate, expirationDate);
         assertion.setConditions(conditions);
 
         // Create the subject
         Subject subject;
 
         if (!data.getKeyType().endsWith(RahasConstants.KEY_TYPE_BEARER)) {
             subject = createSubjectWithHolderOfKeySubjectConfirmation(doc, crypto,
                     creationDate, expirationDate, data);
         } else {
             subject = createSubjectWithBearerSubjectConfirmation(data);
         }
 
         // Set the subject
         assertion.setSubject(subject);
 
         // If a SymmetricKey is used build an attr stmt, if a public key is build an authn stmt.
         if (isSymmetricKeyBasedHoK) {
             AttributeStatement attrStmt = createAttributeStatement(data);
             assertion.getAttributeStatements().add(attrStmt);
         } else {
             AuthnStatement authStmt = createAuthenticationStatement(data);
             assertion.getAuthnStatements().add(authStmt);
             if (data.getClaimDialect() != null && data.getClaimElem() != null) {
                 assertion.getAttributeStatements().add(createAttributeStatement(data));
             }
         }
 
         return assertion;
     }
 
     /**
      * This method will create a SAML 2 subject based on Holder of Key confirmation method.
      * The relevant XML would look as follows,
      * <saml2:Subject>
      *       <saml2:NameID>
      *           ...
      *       </saml2:NameID>
      *       <saml2:SubjectConfirmation
      *               Method=”urn:oasis:names:tc:SAML:2.0:cm:holder-of-key”>
      *           <saml2:SubjectConfirmationData
      *                   xsi:type="saml2:KeyInfoConfirmationDataType">
      *               <ds:KeyInfo>
      *                   <ds:KeyValue>...</ds:KeyValue>
      *               </ds:KeyInfo>
      *           </saml2:SubjectConfirmationData>
      *       </saml2:SubjectConfirmation>
      *   </saml2:Subject>
      *
      * KeyInfo can be created based on public key or symmetric key. That is decided by looking at
      * the RahasData.getKeyType. TODO make sure this implementation is correct.
      * Theoretically we should be able to have many subject confirmation methods in a SAML2 subject.
      * TODO - Do we need to support that ?
      * @param doc The original XML document which we need to include the assertion.
      * @param crypto The relevant crypto properties
      * @param creationTime The time that assertion was created.
      * @param expirationTime The expiring time
      * @param data The configuration data relevant request.
      * @return OpenSAML representation of the SAML2 object.
      * @throws TrustException If an error occurred while creating the subject.
      */
     protected Subject createSubjectWithHolderOfKeySubjectConfirmation(Document doc, Crypto crypto,
                                                             DateTime creationTime,
                                                             DateTime expirationTime, RahasData data)
             throws TrustException {
 
 
         // Create the subject
         Subject subject = (Subject)CommonUtil.buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
 
         // Set the subject name identifier
         if (data.getPrincipal() != null) {
             setSubjectNamedIdentifierData(subject, data.getPrincipal().getName(), NameID.EMAIL);
         }
 
         // Create KeyInfo
         KeyInfo keyInfo = createKeyInfo(doc, crypto, data);
 
         //Build the Subject Confirmation
         SubjectConfirmation subjectConfirmation
                 = (SubjectConfirmation)CommonUtil.buildXMLObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
 
         //Set the subject Confirmation method
         subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:holder-of-key");
 
         //Build the subject confirmation data element
         KeyInfoConfirmationDataType scData = createKeyInfoConfirmationDataType();
 
         //Set the keyInfo element
         scData.getKeyInfos().add(keyInfo);
 
         // Set the validity period
         scData.setNotBefore(creationTime);
         scData.setNotOnOrAfter(expirationTime);
 
         //Set the subject confirmation data
         subjectConfirmation.setSubjectConfirmationData(scData);
 
         //set the subject confirmation
         subject.getSubjectConfirmations().add(subjectConfirmation);
 
         log.debug("SAML2.0 subject is constructed successfully.");
         return subject;
     }
 
     private KeyInfoConfirmationDataType createKeyInfoConfirmationDataType() {
         XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
         @SuppressWarnings({"unchecked"}) SAMLObjectBuilder<KeyInfoConfirmationDataType> keyInfoSubjectConfirmationDataBuilder =
                 (SAMLObjectBuilder<KeyInfoConfirmationDataType>) builderFactory.getBuilder
                         (KeyInfoConfirmationDataType.TYPE_NAME);
 
         //Build the subject confirmation data element
         return keyInfoSubjectConfirmationDataBuilder.
                 buildObject(SubjectConfirmationData.DEFAULT_ELEMENT_NAME, KeyInfoConfirmationDataType.TYPE_NAME);
     }
 
     /**
      * This method creates a subject element with the bearer subject confirmation method.
      * <saml:Subject>
      *       <saml:NameIdentifier
      *                   NameQualifier="www.example.com"
      *                   Format=“urn:oasis:names:tc:SAML:1.1:nameid-
      *           format:X509SubjectName”>
      *           uid=joe,ou=people,ou=saml-demo,o=baltimore.com
      *       </saml:NameIdentifier>
      *       <saml:SubjectConfirmation>
      *           <saml:ConfirmationMethod>
      *               urn:oasis:names:tc:SAML:1.0:cm:bearer
      *           </saml:ConfirmationMethod>
      *       </saml:SubjectConfirmation>
      *   </saml:Subject>
      * @param data RahasData element
      * @return  SAML 2.0 Subject element with Bearer subject confirmation
      * @throws org.apache.rahas.TrustException if an error occurred while creating the subject.
      */
     protected Subject createSubjectWithBearerSubjectConfirmation(RahasData data) throws TrustException {
 
         // Create the subject
         Subject subject = (Subject)CommonUtil.buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
 
         //Create NameID and attach it to the subject
         setSubjectNamedIdentifierData(subject, data.getPrincipal().getName(), NameID.EMAIL);
 
         //Build the Subject Confirmation
         SubjectConfirmation subjectConfirmation
                 = (SubjectConfirmation)CommonUtil.buildXMLObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
 
         //Set the subject Confirmation method
         subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
 
         subject.getSubjectConfirmations().add(subjectConfirmation);
         return subject;
     }
 
 
     /**
      * This method signs the given assertion with issuer's private key.
      * @param document The original RST document.
      * @param assertion Assertion to be signed.
      * @param crypto  The cryptographic properties.
      * @return The signed assertion.
      * @throws TrustException If an error occurred while signing the assertion.
      */
     protected Assertion signAssertion(Document document, Assertion assertion, Crypto crypto) throws TrustException {
 
         // Create a SignKeyHolder to hold the crypto objects that are used to sign the assertion
         SignKeyHolder signKeyHolder = createSignKeyHolder(crypto);
 
         // Build the signature object and set the credentials.
         Signature signature = (Signature) CommonUtil.buildXMLObject(Signature.DEFAULT_ELEMENT_NAME);
 
         signature.setSigningCredential(signKeyHolder);
         signature.setSignatureAlgorithm(signKeyHolder.getSignatureAlgorithm());
         signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
 
         //Build the KeyInfo element and set the certificate
         try {
             KeyInfo keyInfo = (KeyInfo) CommonUtil.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME);
             X509Data x509Data = (X509Data) CommonUtil.buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
             org.opensaml.xml.signature.X509Certificate cert
                     = (org.opensaml.xml.signature.X509Certificate) CommonUtil.buildXMLObject
                     (org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
             String value
                     = org.apache.xml.security.utils.Base64.encode(signKeyHolder.getEntityCertificate().getEncoded());
 
             cert.setValue(value);
             x509Data.getX509Certificates().add(cert);
             keyInfo.getX509Datas().add(x509Data);
 
             signature.setKeyInfo(keyInfo);
             assertion.setSignature(signature);
 
             signatureList.add(signature);
 
             //Marshall and Sign
             MarshallerFactory marshallerFactory = org.opensaml.xml.Configuration.getMarshallerFactory();
             Marshaller marshaller = marshallerFactory.getMarshaller(assertion);
             marshaller.marshall(assertion, document.getDocumentElement());
 
             Signer.signObjects(signatureList);
 
         } catch (CertificateEncodingException e) {
             throw new TrustException("Error in setting the signature", e);
         } catch (SignatureException e) {
            throw new TrustException("errorSigningAssertion", e);
         } catch (MarshallingException e) {
            throw new TrustException("errorMarshallingAssertion", e);
         }
 
         log.debug("SAML2.0 assertion is marshalled and signed..");
 
         return assertion;
     }
 
     /**
      * This method is used to create SignKeyHolder instances that contains the credentials required for signing the
      * assertion
      * @param crypto The crypto properties associated with the issuer.
      * @return  SignKeyHolder object.
      * @throws TrustException  If an error occurred while creating SignKeyHolder object.
      */
     private SignKeyHolder createSignKeyHolder(Crypto crypto) throws TrustException {
 
         SignKeyHolder signKeyHolder = new SignKeyHolder();
 
         try {
             X509Certificate[] issuerCerts = CommonUtil.getCertificatesByAlias(crypto,
                     this.tokenIssuerConfiguration.getIssuerKeyAlias());
 
             String sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_RSA;
             String pubKeyAlgo = issuerCerts[0].getPublicKey().getAlgorithm();
             if (pubKeyAlgo.equalsIgnoreCase("DSA")) {
                 sigAlgo = XMLSignature.ALGO_ID_SIGNATURE_DSA;
             }
 
             java.security.Key issuerPK = crypto.getPrivateKey(
                     this.tokenIssuerConfiguration.getIssuerKeyAlias(),
                     this.tokenIssuerConfiguration.getIssuerKeyPassword());
 
             signKeyHolder.setIssuerCerts(issuerCerts);
             signKeyHolder.setIssuerPK((PrivateKey) issuerPK);
             signKeyHolder.setSignatureAlgorithm(sigAlgo);
 
         } catch (Exception e) {
             throw new TrustException("Error creating issuer signature");
         }
 
         log.debug("SignKeyHolder object is created with the credentials..");
 
         return signKeyHolder;
     }
 
     /**
      * This method creates an AttributeStatement. The relevant XML would look like as follows,
      * <saml:AttributeStatement>
      *    <saml:Attribute
      *      xmlns:x500="urn:oasis:names:tc:SAML:2.0:profiles:attribute:X500"
      *      x500:Encoding="LDAP"
      *      NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:uri"
      *      Name="urn:oid:1.3.6.1.4.1.5923.1.1.1.1"
      *      FriendlyName="eduPersonAffiliation">
      *      <saml:AttributeValue
      *        xsi:type="xs:string">member</saml:AttributeValue>
      *      <saml:AttributeValue
      *        xsi:type="xs:string">staff</saml:AttributeValue>
      *    </saml:Attribute>
      *  </saml:AttributeStatement>
      *  Reference -  http://en.wikipedia.org/wiki/SAML_2.0#SAML_2.0_Assertions
      * @param data The RahasData which carry information about RST.
      * @return An AttributeStatement with filled attributes retrieved by calling callback class.
      * @throws TrustException If an error occurred while creating the AttributeStatement.
      */
     protected AttributeStatement createAttributeStatement(RahasData data) throws TrustException {
 
 
         AttributeStatement attributeStatement
                 = (AttributeStatement) CommonUtil.buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
 
         Attribute[] attributes;
 
         SAMLCallbackHandler handler = CommonUtil.getSAMLCallbackHandler(this.tokenIssuerConfiguration, data);
 
         SAMLAttributeCallback cb = new SAMLAttributeCallback(data);
         if (handler != null) {
             try {
                 handler.handle(cb);
             } catch (SAMLException e) {
                 throw new TrustException(
                             "errorCallingSAMLCallback",
                             e);
             }
 
             attributes = cb.getSAML2Attributes();
         } else { //else add the attribute with a default value
 
             log.debug("No callback registered to get attributes ... Using default attributes");
 
             // TODO do we need to remove this ?
             Attribute attribute = (Attribute) CommonUtil.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
             attribute.setName("Name");
             attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified");
 
             XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
 
             XSStringBuilder attributeValueBuilder = (XSStringBuilder) builderFactory
                     .getBuilder(XSString.TYPE_NAME);
 
             XSString stringValue = attributeValueBuilder.buildObject(
                     AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
             stringValue.setValue("Colombo/Rahas");
             attribute.getAttributeValues().add(stringValue);
             attributes = new Attribute[1];
             attributes[0] = attribute;
         }
         //add attributes to the attribute statement
         attributeStatement.getAttributes().addAll(Arrays.asList(attributes));
 
         log.debug("SAML2.0 attribute statement is constructed successfully.");
 
         return attributeStatement;
     }
 
     /**
      * This method creates an authentication statement. The equivalent XML would look as follows,
      * <saml:AuthnStatement
      *    AuthnInstant="2004-12-05T09:22:00Z"
      *    SessionIndex="b07b804c-7c29-ea16-7300-4f3d6f7928ac">
      *    <saml:AuthnContext>
      *      <saml:AuthnContextClassRef>
      *        urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
      *     </saml:AuthnContextClassRef>
      *    </saml:AuthnContext>
      *  </saml:AuthnStatement>
      * @param data The RahasData which carry information about RST.
      * @return OpenSAML representation of an AuthnStatement class.
      * @throws TrustException If an error occurred while creating the authentication statement.
      */
     protected AuthnStatement createAuthenticationStatement(RahasData data) throws TrustException {
 
         MessageContext inMsgCtx = data.getInMessageContext();
 
         //build the auth stmt
         AuthnStatement authenticationStatement
                 = (AuthnStatement)CommonUtil.buildXMLObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
 
         // set the authn instance
         // TODO do we need to use the same time as specified in the conditions ?
         authenticationStatement.setAuthnInstant(new DateTime());
 
         // Create authentication context
         AuthnContext authContext = (AuthnContext)CommonUtil.buildXMLObject(AuthnContext.DEFAULT_ELEMENT_NAME);
 
         // Create authentication context class reference
         AuthnContextClassRef authCtxClassRef
                 = (AuthnContextClassRef)CommonUtil.buildXMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
         
         //if username/password based authn
         if (inMsgCtx.getProperty(RahasConstants.USERNAME) != null) {
             authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
         } else if (inMsgCtx.getProperty(RahasConstants.X509_CERT) != null) { //if X.509 cert based authn
             authCtxClassRef.setAuthnContextClassRef(AuthnContext.X509_AUTHN_CTX);
         }
 
         authContext.setAuthnContextClassRef(authCtxClassRef);
         authenticationStatement.setAuthnContext(authContext);
 
         log.debug("SAML2.0 authentication statement is constructed successfully.");
 
         return authenticationStatement;
     }
 
     /**
      * This method will set the subject principal details to the given subject.
      * @param subject The subject.
      * @param subjectNameId Subject name id, to identify the principal
      * @param format Format of the subjectNameId, i.e. email, x509subject etc ...
      * @throws TrustException If an error occurred while building NameID.
      */
     protected static void setSubjectNamedIdentifierData(Subject subject, String subjectNameId, String format)
             throws TrustException {
 
         //Create NameID and attach it to the subject
         NameID nameID = SAML2Utils.createNamedIdentifier(subjectNameId, format);
         subject.setNameID(nameID);
     }
 
     /**
      * This method creates the KeyInfo relevant for the assertion. The KeyInfo could be created in 2 ways.
      * 1. Using symmetric key - KeyInfo is created using a symmetric key
      * 2. Using a public key - KeyInfo created using a public key
      * The methodology is decided by looking at RahasData.getKeyType() method.
      * @param doc The document which we are processing.
      * @param crypto Includes crypto properties relevant to issuer.
      * @param data Includes metadata about the RST.
      * @return OpenSAML representation of KeyInfo.
      * @throws TrustException If an error occurred while creating the KeyInfo object.
      */
     protected KeyInfo createKeyInfo(Document doc, Crypto crypto, RahasData data)
             throws TrustException {
 
         KeyInfo keyInfo;
 
         // If it is a Symmetric Key
         if (data.getKeyType().endsWith(RahasConstants.KEY_TYPE_SYMM_KEY)) {
 
             isSymmetricKeyBasedHoK = true;
             X509Certificate serviceCert = null;
             try {
 
                 // Get AppliesTo to figure out which service to issue the token
                 // for
                 serviceCert = this.tokenIssuerConfiguration.getServiceCert(crypto, data.getAppliesToAddress());
 
                 keyInfo = CommonUtil.getSymmetricKeyBasedKeyInfo(doc, data, serviceCert, data.getKeySize(), crypto,
                         tokenIssuerConfiguration.getKeyComputation());
 
             } catch (Exception e) {
                 if (serviceCert != null) {
                     throw new TrustException(
                             "errorInBuildingTheEncryptedKeyForPrincipal",
                             new String[]{serviceCert.getSubjectDN().getName()},
                             e);
                 } else {
                     throw new TrustException(
                             "errorInBuildingTheEncryptedKeyForPrincipal",
                             new String[]{"UnknownSubjectDN"},
                             e);
                 }
             }
 
         } else if (data.getKeyType().endsWith(RahasConstants.KEY_TYPE_PUBLIC_KEY)) {    // If it is a public Key
 
             try {
                 // Create the ds:KeyValue element with the ds:X509Data
                 X509Certificate clientCert = data.getClientCert();
 
                 if (clientCert == null) {
                     // TODO are we always looking up by alias ? Dont we need to lookup by any other attribute ?
                     clientCert = CommonUtil.getCertificateByAlias(crypto, data.getPrincipal().getName());
                 }
 
                 keyInfo = CommonUtil.getCertificateBasedKeyInfo(clientCert);
 
             } catch (Exception e) {
                 throw new TrustException("samlAssertionCreationError", e);
             }
         } else {
             log.error("Unidentified key type " + data.getKeyType());
             throw new TrustException(
                             "unidentifiedKeyType",
                             new String[]{data.getKeyType()});
         }
 
         return keyInfo;
 
     }
 
     /**
      * @inheritDoc
      */
     public String getResponseAction(RahasData data) throws TrustException {
         return null;
     }
 
     /**
      * @inheritDoc
      */
     public void setConfigurationFile(String configFile) {
         this.configFile = configFile;
     }
 
     /**
      * @inheritDoc
      */
     public void setConfigurationElement(OMElement configElement) {
         this.configElement = configElement;
     }
 
     /**
      * @inheritDoc
      */
     public void setConfigurationParamName(String configParamName) {
         this.configParamName = configParamName;
     }
 
 }
