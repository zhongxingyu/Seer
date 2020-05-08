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
 
 package org.apache.rampart;
 
 import org.apache.axiom.soap.SOAPEnvelope;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.rampart.policy.RampartPolicyData;
 import org.apache.rampart.util.RampartUtil;
 import org.apache.ws.secpolicy.SPConstants;
 import org.apache.ws.secpolicy.model.HttpsToken;
 import org.apache.ws.secpolicy.model.IssuedToken;
 import org.apache.ws.secpolicy.model.SignedEncryptedParts;
 import org.apache.ws.secpolicy.model.SupportingToken;
 import org.apache.ws.secpolicy.model.Token;
 import org.apache.ws.secpolicy.model.UsernameToken;
 import org.apache.ws.secpolicy.model.X509Token;
 import org.apache.ws.security.WSConstants;
 import org.apache.ws.security.WSEncryptionPart;
 import org.apache.ws.security.WSSecurityEngineResult;
 import org.apache.ws.security.WSSecurityException;
 import org.apache.ws.security.message.token.Timestamp;
 import org.apache.ws.security.util.WSSecurityUtil;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import java.math.BigInteger;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 public class PolicyBasedResultsValidator implements PolicyValidatorCallbackHandler {
     
     private static Log log = LogFactory.getLog(PolicyBasedResultsValidator.class);
     
     /** 
      * {@inheritDoc}
      */
     public void validate(ValidatorData data, Vector results) 
     throws RampartException {
         
         RampartMessageData rmd = data.getRampartMessageData();
         
         RampartPolicyData rpd = rmd.getPolicyData();
         
         //If there's Security policy present and no results 
         //then we should throw an error
         if(rpd != null && results == null) {
             throw new RampartException("noSecurityResults");
         }
         
         //Check presence of timestamp
         WSSecurityEngineResult tsResult = null;
         if(rpd != null &&  rpd.isIncludeTimestamp()) {
             tsResult = 
                 WSSecurityUtil.fetchActionResult(results, WSConstants.TS);
             if(tsResult == null) {
                 throw new RampartException("timestampMissing");
             }
             
         }
         
         //sig/encr
         Vector encryptedParts = RampartUtil.getEncryptedParts(rmd);
         if(rpd != null && rpd.isSignatureProtection() && isSignatureRequired(rmd)) {
             
             String sigId = RampartUtil.getSigElementId(rmd);
             
             encryptedParts.add(new WSEncryptionPart(WSConstants.SIG_LN, 
                     WSConstants.SIG_NS, "Element"));
         }
         
         Vector signatureParts = RampartUtil.getSignedParts(rmd);
 
         //Timestamp is not included in sig parts
         if(rpd != null && rpd.isIncludeTimestamp() && !rpd.isTransportBinding()) {
             signatureParts.add(new WSEncryptionPart("timestamp"));
         }
         
         if(!rmd.isInitiator()) {
                         
             //Just an indicator for EndorsingSupportingToken signature
             SupportingToken endSupportingToken = rpd.getEndorsingSupportingTokens();
             if(endSupportingToken !=  null) {
                 SignedEncryptedParts endSignedParts = endSupportingToken.getSignedParts();
                 if((endSignedParts != null && 
                         (endSignedParts.isBody() || 
                                 endSignedParts.getHeaders().size() > 0)) ||
                                 rpd.isIncludeTimestamp()) {
                     signatureParts.add(
                             new WSEncryptionPart("EndorsingSupportingTokens"));
                 }
             }
             //Just an indicator for SignedEndorsingSupportingToken signature
             SupportingToken sgndEndSupportingToken = rpd.getSignedEndorsingSupportingTokens();
             if(sgndEndSupportingToken != null) {
                 SignedEncryptedParts sgndEndSignedParts = sgndEndSupportingToken.getSignedParts();
                 if((sgndEndSignedParts != null && 
                         (sgndEndSignedParts.isBody() || 
                                 sgndEndSignedParts.getHeaders().size() > 0)) || 
                                 rpd.isIncludeTimestamp()) {
                     signatureParts.add(
                             new WSEncryptionPart("SignedEndorsingSupportingTokens"));
                 }
             }
         }
         
         validateEncrSig(data,encryptedParts, signatureParts, results);
         
         if(!rpd.isTransportBinding()) {
             validateProtectionOrder(data, results);
         }  
         
         if(rpd.isTransportBinding() && !rmd.isInitiator()){
             if (rpd.getTransportToken() instanceof HttpsToken) {
                 String incomingTransport = rmd.getMsgContext().getIncomingTransportName();
                 if(!incomingTransport.equals(org.apache.axis2.Constants.TRANSPORT_HTTPS)){
                     throw new RampartException("invalidTransport", 
                             new String[]{incomingTransport});
                 }
             }
         }
         
         validateEncryptedParts(data, encryptedParts, results);
 
         validateSignedPartsHeaders(data, signatureParts, results);
         
         validateRequiredElements(data);
 
         //Supporting tokens
         if(!rmd.isInitiator()) {
             validateSupportingTokens(data, results);
         }
         
         /*
          * Now we can check the certificate used to sign the message. In the
          * following implementation the certificate is only trusted if either it
          * itself or the certificate of the issuer is installed in the keystore.
          * 
          * Note: the method verifyTrust(X509Certificate) allows custom
          * implementations with other validation algorithms for subclasses.
          */
 
         // Extract the signature action result from the action vector
         WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(
                 results, WSConstants.SIGN);
 
         if (actionResult != null) {
             X509Certificate returnCert = (X509Certificate) actionResult
                     .get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
 
             if (returnCert != null) {
                 if (!verifyTrust(returnCert, rmd)) {
                     throw new RampartException ("trustVerificationError");
                 }
             }
         }
         
         /*
          * Perform further checks on the timestamp that was transmitted in the
          * header. 
          * In the following implementation the timestamp is valid if :
          * Timestamp->Created < 'now' < Timestamp->Expires (Last test already handled by WSS4J)
          * 
          * Note: the method verifyTimestamp(Timestamp) allows custom
          * implementations with other validation algorithms for subclasses.
          */
 
         // Extract the timestamp action result from the action vector
         actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.TS);
 
         if (actionResult != null) {
             Timestamp timestamp = (Timestamp) actionResult
                     .get(WSSecurityEngineResult.TAG_TIMESTAMP);
 
             if (timestamp != null) {
                 if (!verifyTimestamp(timestamp, rmd)) {
                     throw new RampartException("cannotValidateTimestamp");
                 }
             }
         }
     }
     
     /**
      * @param encryptedParts
      * @param signatureParts
      */
     protected void validateEncrSig(ValidatorData data,Vector encryptedParts, Vector signatureParts, Vector results) 
     throws RampartException {
         ArrayList actions = getSigEncrActions(results);
         boolean sig = false; 
         boolean encr = false;
         for (Iterator iter = actions.iterator(); iter.hasNext();) {
             Integer act = (Integer) iter.next();
             if(act.intValue() == WSConstants.SIGN) {
                 sig = true;
             } else if(act.intValue() == WSConstants.ENCR) {
                 encr = true;
             }
         }
         
         RampartPolicyData rpd = data.getRampartMessageData().getPolicyData();
         
         SupportingToken sgndSupTokens = rpd.getSignedSupportingTokens();
         SupportingToken sgndEndorSupTokens = rpd.getSignedEndorsingSupportingTokens();
         
         if(sig && signatureParts.size() == 0 
                 && (sgndSupTokens == null || sgndSupTokens.getTokens().size() == 0)
                  && (sgndEndorSupTokens == null || sgndEndorSupTokens.getTokens().size() == 0)) {
             
             //Unexpected signature
             throw new RampartException("unexprectedSignature");
         } else if(!sig && signatureParts.size() > 0) {
             
             //required signature missing
             throw new RampartException("signatureMissing");
         }
         
         if(encr && encryptedParts.size() == 0) {
             
             //Check whether its just an encrypted key
             ArrayList list = this.getResults(results, WSConstants.ENCR);
             boolean encrDataFound = false;
             for (Iterator iter = list.iterator(); iter.hasNext();) {
                 WSSecurityEngineResult result = (WSSecurityEngineResult) iter.next();
                 ArrayList dataRefURIs = (ArrayList)result.get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
                 if ( dataRefURIs != null && dataRefURIs.size() != 0) {
                     encrDataFound = true;
                 }
             }
             //TODO check whether the encrptedDataFound is an UsernameToken
             if(encrDataFound && !isUsernameTokenPresent(data)) {
                 //Unexpected encryption
                 throw new RampartException("unexprectedEncryptedPart");
             }
         } else if(!encr && encryptedParts.size() > 0) {
             
             //required signature missing
             throw new RampartException("encryptionMissing");
         }
     }
 
     /**
      * @param data
      * @param results
      */
     protected void validateSupportingTokens(ValidatorData data, Vector results) 
     throws RampartException {
         
         //Check for UsernameToken
         RampartPolicyData rpd = data.getRampartMessageData().getPolicyData();
         SupportingToken suppTok = rpd.getSupportingTokens();
         handleSupportingTokens(results, suppTok);
         SupportingToken signedSuppToken = rpd.getSignedSupportingTokens();
         handleSupportingTokens(results, signedSuppToken);
         SupportingToken signedEndSuppToken = rpd.getSignedEndorsingSupportingTokens();
         handleSupportingTokens(results, signedEndSuppToken);
         SupportingToken endSuppToken = rpd.getEndorsingSupportingTokens();
         handleSupportingTokens(results, endSuppToken);
     }
 
     /**
      * @param results
      * @param suppTok
      * @throws RampartException
      */
     protected void handleSupportingTokens(Vector results, SupportingToken suppTok) throws RampartException {
         
         if(suppTok == null) {
             return;
         }
         
         ArrayList tokens = suppTok.getTokens();
         for (Iterator iter = tokens.iterator(); iter.hasNext();) {
             Token token = (Token) iter.next();
             if(token instanceof UsernameToken) {
                 //Check presence of a UsernameToken
                 WSSecurityEngineResult utResult = WSSecurityUtil.fetchActionResult(results, WSConstants.UT);
                 if(utResult == null) {
                     throw new RampartException("usernameTokenMissing");
                 }
                 
             } else if ( token instanceof IssuedToken ) {
                 //TODO is is enough to check for ST_UNSIGNED results ??
                 WSSecurityEngineResult samlResult = WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
                 if(samlResult == null) {
                     throw new RampartException("samlTokenMissing");
                 }
             } else if ( token instanceof X509Token) {
                 WSSecurityEngineResult x509Result = WSSecurityUtil.fetchActionResult(results, WSConstants.BST);
                 if(x509Result == null) {
                     throw new RampartException("binaryTokenMissing");
                 }
             }
         }
     }
     
     
     
 
     /**
      * @param data
      * @param results
      */
     protected void validateProtectionOrder(ValidatorData data, Vector results) 
     throws RampartException {
         
         String protectionOrder = data.getRampartMessageData().getPolicyData().getProtectionOrder();
         ArrayList sigEncrActions = this.getSigEncrActions(results);
         
         if(sigEncrActions.size() < 2) {
             //There are no results to COMPARE
             return;
         }
         
         boolean sigNotPresent = true; 
         boolean encrNotPresent = true;
         
         for (Iterator iter = sigEncrActions.iterator(); iter.hasNext();) {
             Integer act = (Integer) iter.next();
             if(act.intValue() == WSConstants.SIGN) {
                 sigNotPresent = false;
             } else if(act.intValue() == WSConstants.ENCR) {
                 encrNotPresent = false;
             }
         }
         
         // Only one action is present, so there is no order to check
         if ( sigNotPresent || encrNotPresent ) {
             return;
         }
         
         
         boolean done = false;
         if(SPConstants.SIGN_BEFORE_ENCRYPTING.equals(protectionOrder)) {
                         
             boolean sigFound = false;
             for (Iterator iter = sigEncrActions.iterator(); 
                 iter.hasNext() || !done;) {
                 Integer act = (Integer) iter.next();
                 if(act.intValue() == WSConstants.ENCR && ! sigFound ) {
                     // We found ENCR and SIGN has not been found - break and fail
                     break;
                 }
                 if(act.intValue() == WSConstants.SIGN) {
                     sigFound = true;
                 } else if(sigFound) {
                     //We have an ENCR action after sig
                     done = true;
                 }
             }
             
         } else {
             boolean encrFound = false;
             for (Iterator iter = sigEncrActions.iterator(); iter.hasNext();) {
                 Integer act = (Integer) iter.next();
                 if(act.intValue() == WSConstants.SIGN && ! encrFound ) {
                     // We found SIGN and ENCR has not been found - break and fail
                     break;
                 }
                 if(act.intValue() == WSConstants.ENCR) {
                     encrFound = true;
                 } else if(encrFound) {
                     //We have an ENCR action after sig
                     done = true;
                 }
             }
         }
         
         if(!done) {
             throw new RampartException("protectionOrderMismatch");
         }
     }
 
 
     protected ArrayList getSigEncrActions(Vector results) {
         ArrayList sigEncrActions = new ArrayList();
         for (Iterator iter = results.iterator(); iter.hasNext();) {
             Integer actInt = (Integer) ((WSSecurityEngineResult) iter.next())
                     .get(WSSecurityEngineResult.TAG_ACTION);
             int action = actInt.intValue();
             if(WSConstants.SIGN == action || WSConstants.ENCR == action) {
                 sigEncrActions.add(Integer.valueOf(action));
             }
             
         }
         return sigEncrActions;
     }
 
     protected void validateEncryptedParts(ValidatorData data, Vector encryptedParts, Vector results) 
     throws RampartException {
         
         RampartMessageData rmd = data.getRampartMessageData();
         
         ArrayList encrRefs = getEncryptedReferences(results);
         
         RampartPolicyData rpd = rmd.getPolicyData();
         
         //Check for encrypted body
         if(rpd.isEncryptBody()) {
             
             if(!encrRefs.contains(data.getBodyEncrDataId())){
                 throw new RampartException("encryptedPartMissing", 
                         new String[]{data.getBodyEncrDataId()});
             }
         }
 
         for (int i = 0 ; i < encryptedParts.size() ; i++) {
             
             WSEncryptionPart encPart = (WSEncryptionPart)encryptedParts.get(i);
             
             //This is the encrypted Body and we already checked encrypted body
             if (encPart.getType() == WSConstants.PART_TYPE_BODY) {
                 continue;
             }
             
             //TODO we don't check encrypted headers now
             // Can't change id when when encrypted header is both signed and encrypted
             //FIX THIS
             if (encPart.getType() == WSConstants.PART_TYPE_HEADER) {
                 continue;
             }
             
             //TODO we need to check encrypted signature
             if (WSConstants.SIG_LN.equals(encPart.getName()) &&
                     WSConstants.SIG_NS.equals(encPart.getNamespace())) {
                 continue;
             }
             
             if (encPart.getEncId() == null) {
                 throw new RampartException("encryptedPartMissing", 
                         new String[]{encPart.getNamespace()+":"+encPart.getName()});
             } else if (!isRefIdPresent(encrRefs, encPart.getEncId())) {
                 throw new RampartException("encryptedPartMissing", 
                         new String[]{encPart.getNamespace()+":"+encPart.getName()});                
             }
             
         }
         
     }
     
     public void validateRequiredElements(ValidatorData data) throws RampartException {
         
         RampartMessageData rmd = data.getRampartMessageData();
         
         RampartPolicyData rpd = rmd.getPolicyData();
         
         SOAPEnvelope envelope = rmd.getMsgContext().getEnvelope();
         
         Iterator elementsIter = rpd.getRequiredElements().iterator();
         
         while (elementsIter.hasNext()) {
             
             String expression = (String) elementsIter.next();
             
             if ( !RampartUtil.checkRequiredElements(envelope, rpd.getDeclaredNamespaces(), expression)) {
                 throw new RampartException("requiredElementsMissing", new String[] { expression } );
             }
         }
         
     }
 
     protected void validateSignedPartsHeaders(ValidatorData data, Vector signatureParts, Vector results) 
     throws RampartException {
         
         RampartMessageData rmd = data.getRampartMessageData();
         
         Node envelope = rmd.getDocument().getFirstChild();
         
         WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(
                 results, WSConstants.SIGN);
 
         // Find elements that are signed
         Vector actuallySigned = new Vector();
         if( actionResult != null ) { 
             Set signedIDs = (Set)actionResult.get(WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS);
             for (Iterator i = signedIDs.iterator(); i.hasNext();) {
                 String e = (String) i.next();
                 
                 Element element = WSSecurityUtil.findElementById(envelope, e, WSConstants.WSU_NS);
                 actuallySigned.add( element );
             }
         }
         
         for(int i=0; i<signatureParts.size(); i++) {
             WSEncryptionPart wsep = (WSEncryptionPart) signatureParts.get( i );
             
             Element headerElement = (Element) WSSecurityUtil.findElement(
                     envelope, wsep.getName(), wsep.getNamespace() );
             if( headerElement == null ) {
                 // The signedpart header we are checking is not present in Soap header - this is allowed
                 continue;
             }
             
             // header element present - verify that it is part of signature
             if( actuallySigned.contains( headerElement) ) {
                 continue;
             }
             
             // header defined in policy is present but not signed
             throw new RampartException("signedPartHeaderNotSigned", new String[] { wsep.getName() });
         }
     }
 
     
     protected boolean isSignatureRequired(RampartMessageData rmd) {
         RampartPolicyData rpd = rmd.getPolicyData();
         return (rpd.isSymmetricBinding() && rpd.getSignatureToken() != null) ||
                 (!rpd.isSymmetricBinding() && !rpd.isTransportBinding() && 
                         ((rpd.getInitiatorToken() != null && rmd.isInitiator())
                                 || rpd.getRecipientToken() != null && !rmd.isInitiator()));
     }
     
 
     /*
      * Verify that ts->Created is before 'now'
      * - testing that timestamp has not expired ('now' is before ts->Expires) is handled earlier by WSS4J
      */
     protected boolean verifyTimestamp(Timestamp timestamp, RampartMessageData rmd) throws RampartException {
 
         Calendar cre = timestamp.getCreated();
         if (cre != null) {
             long now = Calendar.getInstance().getTimeInMillis();
 
             // adjust 'now' with allowed timeskew 
             long maxSkew = RampartUtil.getTimestampMaxSkew( rmd );
             if( maxSkew > 0 ) {
                 now += (maxSkew * 1000);
             }
             
             // fail if ts->Created is after 'now'
             if( cre.getTimeInMillis() > now ) {
                 return false;
             }
         }
 
         return true;
     }
     
     /**
      * Evaluate whether a given certificate should be trusted.
      * Hook to allow subclasses to implement custom validation methods however they see fit.
      * <p/>
      * Policy used in this implementation:
      * 1. Search the keystore for the transmitted certificate
      * 2. Search the keystore for a connection to the transmitted certificate
      * (that is, search for certificate(s) of the issuer of the transmitted certificate
      * 3. Verify the trust path for those certificates found because the search for the issuer might be fooled by a phony DN (String!)
      *
      * @param cert the certificate that should be validated against the keystore
      * @return true if the certificate is trusted, false if not (AxisFault is thrown for exceptions during CertPathValidation)
      * @throws WSSecurityException
      */
     protected boolean verifyTrust(X509Certificate cert, RampartMessageData rmd) throws RampartException {
 
         // If no certificate was transmitted, do not trust the signature
         if (cert == null) {
             return false;
         }
 
         String[] aliases = null;
         String alias = null;
         X509Certificate[] certs;
 
         String subjectString = cert.getSubjectDN().getName();
         String issuerString = cert.getIssuerDN().getName();
         BigInteger issuerSerial = cert.getSerialNumber();
         
         boolean doDebug = log.isDebugEnabled();
 
         if (doDebug) {
             log.debug("WSHandler: Transmitted certificate has subject " + 
                     subjectString);
             log.debug("WSHandler: Transmitted certificate has issuer " + 
                     issuerString + " (serial " + issuerSerial + ")");
         }
 
         // FIRST step
         // Search the keystore for the transmitted certificate
 
         // Search the keystore for the alias of the transmitted certificate
         try {
             alias = RampartUtil.getSignatureCrypto(
                     rmd.getPolicyData().getRampartConfig(),
                     rmd.getCustomClassLoader()).getAliasForX509Cert(
                     issuerString, issuerSerial);
         } catch (WSSecurityException ex) {
             throw new RampartException("cannotFindAliasForCert", new String[]{subjectString}, ex);
         }
 
         if (alias != null) {
             // Retrieve the certificate for the alias from the keystore
             try {
                 certs = RampartUtil.getSignatureCrypto(
                         rmd.getPolicyData().getRampartConfig(),
                         rmd.getCustomClassLoader()).getCertificates(alias);
             } catch (WSSecurityException ex) {
                 throw new RampartException("noCertForAlias", new String[] {alias}, ex);
             }
 
             // If certificates have been found, the certificates must be compared
             // to ensure againgst phony DNs (compare encoded form including signature)
             if (certs != null && certs.length > 0 && cert.equals(certs[0])) {
                 if (doDebug) {
                     log.debug("Direct trust for certificate with " + subjectString);
                 }
                 return true;
             }
         } else {
             if (doDebug) {
                 log.debug("No alias found for subject from issuer with " + issuerString + " (serial " + issuerSerial + ")");
             }
         }
 
         // SECOND step
         // Search for the issuer of the transmitted certificate in the keystore
 
         // Search the keystore for the alias of the transmitted certificates issuer
         try {
             aliases = RampartUtil.getSignatureCrypto(
                     rmd.getPolicyData().getRampartConfig(),
                     rmd.getCustomClassLoader()).getAliasesForDN(issuerString);
         } catch (WSSecurityException ex) {
             throw new RampartException("cannotFindAliasForCert", new String[]{issuerString}, ex);
         }
 
         // If the alias has not been found, the issuer is not in the keystore
         // As a direct result, do not trust the transmitted certificate
         if (aliases == null || aliases.length < 1) {
             if (doDebug) {
                 log.debug("No aliases found in keystore for issuer " + issuerString + " of certificate for " + subjectString);
             }
             return false;
         }
 
         // THIRD step
         // Check the certificate trust path for every alias of the issuer found in the keystore
         for (int i = 0; i < aliases.length; i++) {
             alias = aliases[i];
 
             if (doDebug) {
                 log.debug("Preparing to validate certificate path with alias " + alias + " for issuer " + issuerString);
             }
 
             // Retrieve the certificate(s) for the alias from the keystore
             try {
                 certs = RampartUtil.getSignatureCrypto(
                         rmd.getPolicyData().getRampartConfig(),
                         rmd.getCustomClassLoader()).getCertificates(alias);
             } catch (WSSecurityException ex) {
                 throw new RampartException("noCertForAlias", new String[] {alias}, ex);
             }
 
             // If no certificates have been found, there has to be an error:
             // The keystore can find an alias but no certificate(s)
             if (certs == null || certs.length < 1) {
                 throw new RampartException("noCertForAlias", new String[] {alias});
             }
 
             // Form a certificate chain from the transmitted certificate
             // and the certificate(s) of the issuer from the keystore
             // First, create new array
             X509Certificate[] x509certs = new X509Certificate[certs.length + 1];
             // Then add the first certificate ...
             x509certs[0] = cert;
             // ... and the other certificates
             for (int j = 0; j < certs.length; j++) {
                cert = certs[i];
                x509certs[certs.length + j] = cert;
             }
             certs = x509certs;
 
             // Use the validation method from the crypto to check whether the subjects certificate was really signed by the issuer stated in the certificate
             try {
                 if (RampartUtil.getSignatureCrypto(
                         rmd.getPolicyData().getRampartConfig(),
                         rmd.getCustomClassLoader()).validateCertPath(certs)) {
                     if (doDebug) {
                         log.debug("WSHandler: Certificate path has been verified for certificate with subject " + subjectString);
                     }
                     return true;
                 }
             } catch (WSSecurityException ex) {
                 throw new RampartException("certPathVerificationFailed", new String[]{subjectString}, ex);
             }
         }
 
         log.debug("WSHandler: Certificate path could not be verified for certificate with subject " + subjectString);
         return false;
     }
 
     
     protected ArrayList getEncryptedReferences(Vector results) {
         
         //there can be multiple ref lists
         ArrayList encrResults = getResults(results, WSConstants.ENCR);
         
         ArrayList refs = new ArrayList();
         
         for (Iterator iter = encrResults.iterator(); iter.hasNext();) {
             WSSecurityEngineResult engineResult = (WSSecurityEngineResult) iter.next();
             ArrayList dataRefUris = (ArrayList) engineResult
                     .get(WSSecurityEngineResult.TAG_DATA_REF_URIS);
             
             //take only the ref list processing results
             if(dataRefUris != null) {
                 for (Iterator iterator = dataRefUris.iterator(); iterator
                         .hasNext();) {
                     String uri = (String) iterator.next();
                     refs.add(uri);
                 }
             }
         }
         
         return refs;
     }
     
     
     
     protected ArrayList getResults(Vector results, int action) {
         
         ArrayList list = new ArrayList();
         
         for (int i = 0; i < results.size(); i++) {
             // Check the result of every action whether it matches the given
             // action
             Integer actInt = (Integer)((WSSecurityEngineResult) results.get(i)).get(WSSecurityEngineResult.TAG_ACTION); 
             if (actInt.intValue() == action) {
                 list.add((WSSecurityEngineResult) results.get(i));
             }
         }
         
         return list;
     }
     
     protected boolean isUsernameTokenPresent(ValidatorData data) {
         
         //TODO This can be integrated with supporting token processing
         // which also checks whether Username Tokens present
         
         RampartPolicyData rpd = data.getRampartMessageData().getPolicyData();
         
         SupportingToken suppTok = rpd.getSupportingTokens();
         if(isUsernameTokenPresent(suppTok)){
             return true;
         }
         
         SupportingToken signedSuppToken = rpd.getSignedSupportingTokens();
         if(isUsernameTokenPresent(signedSuppToken)) {
             return true;
         }
         
         SupportingToken signedEndSuppToken = rpd.getSignedEndorsingSupportingTokens();
         if(isUsernameTokenPresent(signedEndSuppToken)) {
             return true;
         }
         
         SupportingToken endSuppToken = rpd.getEndorsingSupportingTokens();
         if(isUsernameTokenPresent(endSuppToken)){
             return true;
         }
         
         return false;
         
         
     }
     
     protected boolean isUsernameTokenPresent(SupportingToken suppTok) {
         
         if(suppTok == null) {
             return false;
         }
         
         ArrayList tokens = suppTok.getTokens();
         for (Iterator iter = tokens.iterator(); iter.hasNext();) {
             Token token = (Token) iter.next();
             if(token instanceof UsernameToken) {
                 return true;
             }
         }
         
         return false;
     }
     
     private boolean isRefIdPresent(ArrayList refList , String id) {
         
         for (int i = 0; i < refList.size() ; i++) {           
             String refId = (String)refList.get(i);           
             if (refId != null && refId.equals(id)) {
                 return true;
             } else if (refId != null) {
                 //TODO This is a hack to handle the special case Encrypted Header
                 refId = refId.replaceFirst("EncDataId","EncHeader");
                 if (refId.equals(id)) {
                     return true;
                 }
             }
         }
         
         return false;
         
     }
 
     
 }
