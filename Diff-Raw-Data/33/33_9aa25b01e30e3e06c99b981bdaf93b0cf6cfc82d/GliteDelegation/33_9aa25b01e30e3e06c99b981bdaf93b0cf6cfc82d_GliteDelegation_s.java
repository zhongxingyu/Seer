 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. See
  * http://www.eu-egee.org/partners/ for details on the copyright holders.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.glite.security.delegation.impl;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringBufferInputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.security.GeneralSecurityException;
 import java.security.KeyPair;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.X509Certificate;
 import java.util.Calendar;
 
 import org.apache.log4j.Logger;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 import org.glite.security.delegation.CertInfoTuple;
 import org.glite.security.delegation.GrDPX509Util;
 import org.glite.security.delegation.GrDProxyDlgeeOptions;
 import org.glite.security.delegation.DelegationException;
 import org.glite.security.delegation.NewProxyReq;
 import org.glite.security.delegation.storage.GrDPStorage;
 import org.glite.security.delegation.storage.GrDPStorageCacheElement;
 import org.glite.security.delegation.storage.GrDPStorageElement;
 import org.glite.security.delegation.storage.GrDPStorageException;
 import org.glite.security.delegation.storage.GrDPStorageFactory;
 
 import eu.emi.security.authn.x509.impl.CertificateUtils;
 import eu.emi.security.authn.x509.impl.CertificateUtils.Encoding;
 import eu.emi.security.authn.x509.impl.KeyAndCertCredential;
 import eu.emi.security.authn.x509.impl.X500NameUtils;
 import eu.emi.security.authn.x509.proxy.ProxyCSR;
 import eu.emi.security.authn.x509.proxy.ProxyCSRGenerator;
 import eu.emi.security.authn.x509.proxy.ProxyCertificateOptions;
 
 /**
  * Implementation of the logic of the Glite Delegation Interface on the server
  * side.
  * 
  * @author Ricardo Rocha <Ricardo.Rocha@cern.ch>
  * @author Akos Frohner <Akos.Frohner@cern.ch>
  * @author Joni Hahkala <Joni.Hahkala@cern.ch>
  * 
  */
 public class GliteDelegation {
 
     /** Local logger object. */
     private static Logger logger = Logger.getLogger(GliteDelegation.class);
 
     /**
      * The default key size to be used. Can be overwritten by setting the
      * dlgeeKeySize property in the dlgee.properties file if the value there is
      * bigger.
      */
     private int DEFAULT_KEY_SIZE = 1024;
 
     /**
      * Set at instantiation time. Remains false if a bad configuration set was
      * found.
      */
     private boolean m_bad_config = false;
 
     /** Local object interfacing the storage area. */
     private GrDPStorage m_storage = null;
 
     /** Key size being used. */
     private int m_keySize;
 
     /**
      * whether the presence of voms attributes will be required in the incoming
      * certificate chains.
      */
    public static boolean requireVomsAttrs = false;
 
     /**
      * Loads the DLGEE properties from the default config file and calls the
      * appropriate constructor.
      * 
      * @throws IOException Failed to load the DLGEE config file
      * @see #GliteDelegation(GrDProxyDlgeeOptions)
      */
     public GliteDelegation() throws IOException {
         this(new GrDProxyDlgeeOptions(GrDPX509Util.getDlgeePropertyFile()));
     }
 
     /**
      * Class constructor.
      * 
      * Creates a new storage handler instance (implementation depending on
      * configuration) to be used later.
      * 
      * Sets the value of the key size as defined in the configuration.
      * 
      * @param dlgeeOpt the options object for configuring the delegation
      *            receiver.
      */
     public GliteDelegation(GrDProxyDlgeeOptions dlgeeOpt) {
 
         // this.m_dlgeeOpt = dlgeeOpt;
         if (logger.isDebugEnabled()) {
             logger.debug("Using DLGEE properties: " + "DN: " + dlgeeOpt.getDlgeeDN() + ". Pass: <hidden>. proxyFile: "
                     + dlgeeOpt.getDlgeeProxyFile() + ". " + "delegationStorageFactory: "
                     + dlgeeOpt.getDlgeeStorageFactory());
         }
         // Get a GrDStorage instance
         try {
             GrDPStorageFactory stgFactory = GrDPX509Util.getGrDPStorageFactory(dlgeeOpt.getDlgeeStorageFactory());
 
             m_storage = stgFactory.createGrDPStorage(dlgeeOpt);
         } catch (Exception e) {
             logger.error("Failed to get a GrDPStorage instance. Delegation is not active.", e);
             m_bad_config = true;
             return;
         }
 
         // Set the size of the key, if not defined or smaller than the default,
         // use default.
         m_keySize = dlgeeOpt.getDlgeeKeySize();
         if (m_keySize == -1 || m_keySize < DEFAULT_KEY_SIZE) {
             m_keySize = DEFAULT_KEY_SIZE;
         }
     }
 
     /**
      * Generates a new proxy certificate proxy request based on the client DN
      * and voms attributes in SecurityContext. Also checks if the request with
      * given (or generated if not given) id already exists.
      * 
      * @param inDelegationID The delegation id used.
      * @param certs The certificates the user used to authenticate himself.
      * @return The generated Proxy request in PEM encoding.
      * @throws DelegationException Thrown in case of failures.
      */
     public String getProxyReq(String inDelegationID, X509Certificate certs[]) throws DelegationException {
         logger.debug("Processing getProxyReq.");
         String delegationID = inDelegationID;
 
         GrDPStorageElement elem = null;
 
         // Check if a bad configuration was detected on launch (and fail if
         // true)
         if (m_bad_config) {
             logger.error("Service is misconfigured. Stopping execution.");
             throw new DelegationException("Service is misconfigured.");
         }

         CertInfoTuple info = new CertInfoTuple(certs, requireVomsAttrs);
 
         logger.debug("Got get proxy req request from client '" + info.dn + "', getting VOMS attributes.");
 
         // Generate a delegation id from the client DN and VOMS attributes
         if (delegationID == null || delegationID.length() == 0) {
             delegationID = GrDPX509Util.genDlgID(info.dn, info.vomsAttributes);
         }
 
         logger.debug("Delegation id is: " + delegationID);
 
         // Search for an existing entry in storage for this delegation ID (null
         // if non existing)
         try {
             elem = m_storage.findGrDPStorageElement(delegationID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Failure on storage interaction.", e);
             throw new DelegationException("Internal failure.");
         }
 
         // Throw error in case there was already a credential with the given id
         if (elem != null) {
             String vomsAttrsStr = GrDPX509Util.toStringVOMSAttrs(info.vomsAttributes);
             logger.debug("Delegation ID '" + delegationID + "' already exists" + " for client (DN='" + info.dn
                     + "; VOMS ATTRS='" + vomsAttrsStr + "'). Call renewProxyReq.");
             throw new DelegationException("Delegation ID '" + delegationID + "' already exists" + " for client (DN='"
                     + info.dn + "; VOMS ATTRS='" + vomsAttrsStr + "'). Call renewProxyReq.");
         }
 
         // Create and store the new certificate request
         return createAndStoreCertificateRequest(certs, delegationID, info.dn, info.vomsAttributes);
     }
 
     /**
      * Generates a new proxy request object based on the DN and voms attributes
      * in the security context. Also checks if the request with given (or
      * generated if not given) id already exists.
      * 
      * @param inDelegationID the delegation id to use, will be generated if not
      *            given.
      * @param certs The certificates the user used to authenticate himself.
      * @return The newProxyReq object.
      * @throws DelegationException thrown in case of failure.
      */
     public NewProxyReq getNewProxyReq(String inDelegationID, X509Certificate certs[]) throws DelegationException {
         logger.debug("Processing getNewProxyReq.");
 
         String delegationID = inDelegationID;
 
         GrDPStorageElement elem = null;
 
         // Check if a bad configuration was detected on launch (and fail if
         // true)
         if (m_bad_config) {
             logger.error("Service is misconfigured. Stopping execution.");
             throw new DelegationException("Service is misconfigured.");
         }
 
         CertInfoTuple info = new CertInfoTuple(certs, requireVomsAttrs);
 
         logger.debug("Got get new proxy req request from client '" + info.dn + "'");
 
         // Generate a delegation id from the client DN and VOMS attributes
         if (delegationID == null || delegationID.length() == 0) {
             delegationID = GrDPX509Util.genDlgID(info.dn, info.vomsAttributes);
         }
 
         // Search for an existing entry in storage for this delegation ID (null
         // if non existing)
         try {
             elem = m_storage.findGrDPStorageElement(delegationID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Failure on storage interaction.", e);
             throw new DelegationException("Internal failure.");
         }
 
         // Throw error in case there was already a credential with the given id
         if (elem != null) {
             String vomsAttrsStr = GrDPX509Util.toStringVOMSAttrs(info.vomsAttributes);
             String errorMsg = "Delegation ID '" + delegationID + "' already exists" + " for client (DN='" + info.dn
                     + "; VOMS ATTRS='" + vomsAttrsStr + "'). Call renewProxyReq.";
 
             logger.debug(errorMsg);
             throw new DelegationException(errorMsg);
         }
 
         // Create and store the new certificate request
         String certRequest = createAndStoreCertificateRequest(certs, delegationID, info.dn, info.vomsAttributes);
 
         // Create and return the proxy request object
         NewProxyReq newProxyReq = new NewProxyReq();
         newProxyReq.setDelegationID(delegationID);
         newProxyReq.setProxyRequest(certRequest);
 
         return newProxyReq;
     }
 
     /**
      * Generates a new delegation request for the existing delegation with the
      * given (or generated) delegation.
      * 
      * @param inDelegationID The delegation id to use, will be genarated if not
      *            given.
      * @param certs The certificates the user used to authenticate himself.
      * @return The delegation request in PEM format.
      * @throws DelegationException Thrown in case of failure.
      */
     public String renewProxyReq(String inDelegationID, X509Certificate certs[]) throws DelegationException {
         logger.debug("Processing renewProxyReq.");
 
         String delegationID = inDelegationID;
 
         GrDPStorageElement elem = null;
 
         // Check if a bad configuration was detected on launch (and fail if
         // true)
         if (m_bad_config) {
             logger.error("Service is misconfigured. Stopping execution.");
             throw new DelegationException("Service is misconfigured.");
         }
 
         CertInfoTuple info = new CertInfoTuple(certs, requireVomsAttrs);
 
         logger.debug("Got renew proxy request from client '" + info.dn + "'");
 
         // Generate a delegation id from the client DN and VOMS attributes
         if (delegationID == null || delegationID.length() == 0) {
             delegationID = GrDPX509Util.genDlgID(info.dn, info.vomsAttributes);
         }
 
         // Search for an existing entry in storage for this delegation ID (null
         // if non existing)
         try {
             elem = m_storage.findGrDPStorageElement(delegationID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Failure on storage interaction.", e);
             throw new DelegationException("Internal failure.");
         }
 
         // Check that the DLG ID had a corresponding delegated credential
         if (elem == null) {
             logger.debug("Failed to renew credential as there was no delegation with ID '" + delegationID
                     + "' for client '" + info.dn + "'");
         }
 
         // Create and store the new certificate request
         return createAndStoreCertificateRequest(certs, delegationID, info.dn, info.vomsAttributes);
     }
 
     /**
      * @param inDelegationID The delegation ID used for the delegation.
      * @param proxy The new proxy received from the client.
      * @param certs The certificates the user used to authenticate himself.
      * @throws DelegationException
      */
     public void putProxy(String inDelegationID, String proxy, X509Certificate certs[]) throws Exception {// throws DelegationException {
         logger.info("Processing putProxy.");
 
         String delegationID = inDelegationID;
 
         // Check if a bad configuration was detected on launch (and fail if
         // true)
         if (m_bad_config) {
             logger.error("Service is misconfigured. Stopping execution.");
             throw new DelegationException("Service is misconfigured.");
         }
 
         // Check for a null proxy
         if (proxy == null) {
             logger.error("Failed to putProxy as proxy was null.");
             throw new DelegationException("No proxy was given.");
         }
 
         CertInfoTuple info = new CertInfoTuple(certs, requireVomsAttrs);
 
         logger.debug("Got put proxy request from client '" + info.dn + "'");
 
         // Load given proxy
         X509Certificate[] proxyCertChain;
         try {
             proxyCertChain = CertificateUtils.loadCertificateChain(new BufferedInputStream(new StringBufferInputStream(
                     proxy)), Encoding.PEM);
         } catch (IOException e2) {
             logger.error("Failed to load proxy certificate chain: " + e2.getMessage());
             throw new DelegationException("Failed to load proxy certificate chain: " + e2.getMessage());
         }
         if (proxyCertChain == null || proxyCertChain.length == 0) {
             logger.error("Failed to load proxy certificate chain - chain was null or size 0.");
             throw new DelegationException("Failed to load proxy certificate chain.");
         }
         logger.debug("Given proxy certificate loaded successfully.");
 
         // check if the chain is within it's validity period.
         for (int i = 0; i < proxyCertChain.length; i++) {
             // Check if the proxy is currently valid
             try {
                 proxyCertChain[i].checkValidity();
             } catch (CertificateExpiredException e) {
                 throw new DelegationException("Failed proxy validation - it expired on: "
                         + proxyCertChain[0].getNotAfter());
             } catch (CertificateNotYetValidException e) {
                 throw new DelegationException("Failed proxy validation - it will be valid from: "
                         + proxyCertChain[0].getNotBefore());
             }
         }
 
         // Get the given proxy information
         String proxySubjectDN = X500NameUtils.getReadableForm(proxyCertChain[0].getSubjectX500Principal());
         String proxyIssuerDN = X500NameUtils.getReadableForm(proxyCertChain[0].getIssuerX500Principal());
         if (logger.isDebugEnabled()) {
             logger.debug("Proxy Subject DN: " + proxySubjectDN);
             logger.debug("Proxy Issuer DN: " + proxyIssuerDN);
             logger.debug("Proxy Public key:" + proxyCertChain[0].getPublicKey());
             logger.debug("chain length is: " + proxyCertChain.length);
             logger.debug("last cert is:" + proxyCertChain[proxyCertChain.length - 1]);
 
             for (int n = 0; n < proxyCertChain.length; n++) {
                 logger.debug("cert [" + n + "] is from "
                         + X500NameUtils.getReadableForm(proxyCertChain[n].getSubjectX500Principal()));
             }
         }
 
         if (proxySubjectDN == null || proxyIssuerDN == null) {
             logger.error("Failed to get DN (subject or issuer) out of proxy. It came null");
             throw new DelegationException("Failed to get DN (subject or issuer) out of proxy.");
         }
 
         logger.debug("Client DN: " + info.dn);
 
         // Get a delegation ID for the given proxy (or take the specified one if
         // given)
         // TODO: Should the dlg id here be generated from the client or the
         // proxy info?
         // Also, should the client and proxy VOMS attributes be checked for a
         // match?
         if (delegationID == null || delegationID.length() == 0) {
             delegationID = GrDPX509Util.genDlgID(info.dn, info.vomsAttributes);
         }
         logger.debug("Delegation ID is '" + delegationID + "'");
 
         // Check that the client is the issuer of the given proxy
         // TODO: more strict check
         if (!proxyIssuerDN.endsWith(info.dn)) {
             String message = "Client '" + info.dn + "' is not issuer of proxy '" + proxyIssuerDN + "'.";
             logger.error(message);
             throw new DelegationException(message);
         }
 
         String cacheID = delegationID;
         try {
             cacheID = delegationID + '+' + GrDPX509Util.generateSessionID(proxyCertChain[0].getPublicKey());
             logger.debug("public key is: " + proxyCertChain[0].getPublicKey());
         } catch (GeneralSecurityException e) {
             logger.error("Error while generating the session ID." + e);
             throw new DelegationException("Failed to generate the session ID.");
         }
         logger.debug("Cache ID (delegation ID + session ID): " + cacheID);
 
         // Get the cache entry for this delegation ID
         GrDPStorageCacheElement cacheElem = null;
         try {
             cacheElem = m_storage.findGrDPStorageCacheElement(cacheID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Failed to get certificate request information from storage.", e);
             throw new DelegationException("Internal failure.");
         }
 
         // Check if the delegation request existed
         if (cacheElem == null) {
             logger.info("Could not find cache ID '" + cacheID + "' for DN '" + info.dn + "' in cache.");
             throw new DelegationException("Could not find a proper delegation request");
         }
         logger.debug("Got from cache element for cache ID '" + cacheID + "' and DN '" + info.dn + "'");
 
         // the public key of the cached certificate request has to
         // match the public key of the proxy certificate, otherwise
         // this is an answer to a different request
         PEMReader pemReader = new PEMReader(new StringReader(cacheElem.getCertificateRequest()));
         PKCS10CertificationRequest req;
         try {
             req = (PKCS10CertificationRequest) pemReader.readObject();
             pemReader.close();
         } catch (IOException e1) {
             logger.error("Could not load the original certificate request from cache.");
             throw new DelegationException("Could not load the original certificate request from cache: "
                     + e1.getMessage());
         }
         if (req == null) {
             logger.error("Could not load the original certificate request from cache.");
             throw new DelegationException("Could not load the original certificate request from cache.");
         }
         try {
             if (!req.getPublicKey().equals(proxyCertChain[0].getPublicKey())) {
                 logger.error("The proxy and the original request's public key do not match.");
                 logger.error("Proxy public key: " + proxyCertChain[0].getPublicKey());
                 logger.error("Request public key: " + req.getPublicKey());
                 throw new DelegationException("The proxy and the original request's public key do not match.");
             }
         } catch (GeneralSecurityException ge) {
             logger.error("Error while decoding the certificate request: " + ge);
             throw new DelegationException("Error while decoding the certificate request.");
         }
 
         // Add the private key to the proxy certificate chain and check it was
         // ok
         StringBufferInputStream keyStream = new StringBufferInputStream(cacheElem.getPrivateKey());
         PrivateKey privateKey;
         try {
             privateKey = CertificateUtils.loadPrivateKey(keyStream, Encoding.PEM, null);
         } catch (IOException e) {
             throw new DelegationException("Failed to read private key from storage, error: " + e.getClass() + ": " + e.getMessage());
         }
         KeyAndCertCredential credential;
         try {
             credential = new KeyAndCertCredential(privateKey, proxyCertChain);
         } catch (KeyStoreException e) {
             throw new DelegationException("Failed to handle credentials, error: " + e.getClass() + ": " + e.getMessage());
         }
         
         ByteArrayOutputStream proxyStream = new ByteArrayOutputStream();
         try {
             CertificateUtils.savePEMKeystore(proxyStream, credential.getKeyStore(), credential.getKeyAlias(), null, credential.getKeyPassword(), null, true);
         } catch (Exception e) {
             logger.error("Error while converting the proxy to string for storage: " + e.getClass() + ": " + e.getMessage());
             e.printStackTrace();
             throw new DelegationException("Error while converting the proxy to string for storage: " + e.getClass() + ": " + e.getMessage());
         } 
         
         String completeProxy = proxyStream.toString();
         
         if (completeProxy == null) {
             logger.error("Failed to add private key to the proxy certificate chain.");
             throw new DelegationException("Could not properly process given proxy.");
         }
 
         // Save the proxy in proxy storage (copying the rest from the info taken
         // from the cache)
         try {
             GrDPStorageElement elem = m_storage.findGrDPStorageElement(delegationID, info.dn);
             if (elem != null) {
                 elem.setCertificate(completeProxy);
                 elem.setTerminationTime(proxyCertChain[0].getNotAfter());
                 m_storage.updateGrDPStorageElement(elem);
             } else {
                 elem = new GrDPStorageElement();
                 elem.setDelegationID(delegationID);
                 elem.setDN(info.dn);
                 elem.setVomsAttributes(info.vomsAttributes);
                 elem.setCertificate(completeProxy);
                 elem.setTerminationTime(proxyCertChain[0].getNotAfter());
                 m_storage.insertGrDPStorageElement(elem);
             }
         } catch (GrDPStorageException e) {
             logger.error("Failed to put certificate request in storage.", e);
             throw new DelegationException("Internal failure: " + e.getMessage());
         }
         logger.debug("Delegation finished successfully.");
 
         // Remove the credential from storage cache
         try {
             m_storage.deleteGrDPStorageCacheElement(cacheID, info.dn);
         } catch (GrDPStorageException e) {
             logger.warn("Failed to remove credential from storage cache.");
         }
 
     }
 
     public void destroy(String inDelegationID, X509Certificate certs[]) throws DelegationException {
         logger.debug("Processing destroy.");
 
         String delegationID = inDelegationID;
 
         GrDPStorageElement elem = null;
 
         // Check if a bad configuration was detected on launch (and fail if
         // true)
         if (m_bad_config) {
             logger.error("Service is misconfigured. Stopping execution.");
             throw new DelegationException("Service is misconfigured.");
         }
 
         CertInfoTuple info = new CertInfoTuple(certs, requireVomsAttrs);
 
         // Generate a delegation id from the client DN and VOMS attributes
         if (delegationID == null || delegationID.length() == 0) {
             delegationID = GrDPX509Util.genDlgID(info.dn, info.vomsAttributes);
         }
 
         logger.debug("Got destroy request for delegation id '" + delegationID + "' from client '" + info.dn + "'");
 
         // Search for an existing entry in storage for this delegation ID (null
         // if non existing)
         try {
             elem = m_storage.findGrDPStorageElement(delegationID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Failure on storage interaction. Exception: ", e);
             throw new DelegationException("Internal failure.");
         }
 
         // Throw exception if non-existing
         if (elem == null) {
             logger.debug("Failed to find delegation ID '" + delegationID + "' for client '" + info.dn + "' in storage.");
             throw new DelegationException("Failed to find delegation ID '" + delegationID + "' in storage.");
         }
 
         // Remove the credential from storage
         try {
             m_storage.deleteGrDPStorageElement(delegationID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Inconsistency needs manual intervention. Delegation ID '" + delegationID + " of client '"
                     + info.dn + "' was found, " + "but could not be removed from storage.");
             throw new DelegationException("Failed to destroy delegated credential.");
         }
 
         logger.debug("Delegated credential destroyed.");
     }
 
     public Calendar getTerminationTime(String inDelegationID, X509Certificate certs[]) throws DelegationException {
         logger.debug("Processing getTerminationTime.");
 
         String delegationID = inDelegationID;
 
         GrDPStorageElement elem = null;
 
         // Check if a bad configuration was detected on launch (and fail if
         // true)
         if (m_bad_config) {
             logger.error("Service is misconfigured. Stopping execution.");
             throw new DelegationException("Service is misconfigured.");
         }
 
         CertInfoTuple info = new CertInfoTuple(certs, requireVomsAttrs);
 
         // Generate a delegation id from the client DN and VOMS attributes
         if (delegationID == null || delegationID.length() == 0) {
             delegationID = GrDPX509Util.genDlgID(info.dn, info.vomsAttributes);
         }
 
         logger.debug("Got getTerminationTime request for delegation id '" + delegationID + "' from client '" + info.dn
                 + "'");
 
         // Search for an existing entry in storage for this delegation ID (null
         // if non existing)
         try {
             elem = m_storage.findGrDPStorageElement(delegationID, info.dn);
         } catch (GrDPStorageException e) {
             logger.error("Failure on storage interaction. Exception: ", e);
             throw new DelegationException("Internal failure.");
         }
 
         // Throw exception if non-existing
         if (elem == null) {
             logger.debug("Failed to find delegation ID '" + delegationID + "' for client '" + info.dn + "' in storage.");
             throw new DelegationException("Failed to find delegation ID '" + delegationID + "' in storage.");
         }
 
         // Build a calendar object with the proper time
         Calendar cal = Calendar.getInstance();
         cal.setTime(elem.getTerminationTime());
 
         return cal;
     }
 
     /**
      * Creates a new certificate request and stores it in the storage cache
      * area.
      * 
      * @param certs The certificates the user used to authenticate himself.
      * @param dlgID The delegation ID of the new delegation
      * @param clientDN The DN of the owner of the delegated credential
      * @param vomsAttributes The list of VOMS attributes in the delegated
      *            credential
      * @return The certificate request for the new delegated credential
      * @throws DelegationException Failed to create or store the new credential
      *             request
      */
     private String createAndStoreCertificateRequest(X509Certificate certs[], String dlgID, String clientDN,
             String[] vomsAttributes) throws DelegationException {
 
         // Get a random KeyPair
         KeyPair keyPair = GrDPX509Util.getKeyPair(m_keySize);
 
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         try {
             CertificateUtils.savePrivateKey(stream, keyPair.getPrivate(), Encoding.PEM, null, null);
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         String privateKey = stream.toString();
         logger.debug("KeyPair generation was successfull.");
         logger.debug("Public key is: " + keyPair.getPublic());
 
         // Generate the certificate request
         String certRequest = null;
         try {
             ProxyCertificateOptions options = new ProxyCertificateOptions(certs);
             options.setPublicKey(keyPair.getPublic());
             ProxyCSR proxyCsr = ProxyCSRGenerator.generate(options, keyPair.getPrivate());
             PKCS10CertificationRequest req = proxyCsr.getCSR();
             StringWriter stringWriter = new StringWriter();
             PEMWriter pemWriter = new PEMWriter(stringWriter);
             try {
                 pemWriter.writeObject(req);
                 pemWriter.flush();
                 pemWriter.close();
             } catch (IOException e) {
                 throw new GeneralSecurityException("Certificate output as string failed: " + e.getMessage());
             }
 
             certRequest = stringWriter.toString();
         } catch (Exception e) {
             logger.error("Error while generating the certificate request." + e);
             throw new DelegationException("Failed to generate a certificate request. " + e.getMessage());
         }
         logger.debug("Certificate request generation was successfull.");
 
         String cacheID = null;
         try {
             cacheID = dlgID + '+' + GrDPX509Util.generateSessionID(keyPair.getPublic());
             logger.debug("public key is: " + keyPair.getPublic());
         } catch (GeneralSecurityException e) {
             logger.error("Error while generating the session ID." + e);
             throw new DelegationException("Failed to generate the session ID.");
         }
         logger.debug("Cache ID (delegation ID + session ID): " + cacheID);
 
         try {
             // TODO: remove search from cache, as the public key is used as
             // random ID, each transaction is individual
             // and search always fails, no update of request is possible and
             // would give rise to race conditions.
 
             // Store the certificate request in cache
             GrDPStorageCacheElement cacheElem = m_storage.findGrDPStorageCacheElement(cacheID, clientDN);
             if (cacheElem != null) {
                 cacheElem.setCertificateRequest(certRequest);
                 cacheElem.setPrivateKey(privateKey);
                 cacheElem.setVomsAttributes(vomsAttributes);
                 m_storage.updateGrDPStorageCacheElement(cacheElem);
             } else {
                 cacheElem = new GrDPStorageCacheElement();
                 cacheElem.setDelegationID(cacheID);
                 cacheElem.setDN(clientDN);
                 cacheElem.setVomsAttributes(vomsAttributes);
                 cacheElem.setCertificateRequest(certRequest);
                 cacheElem.setPrivateKey(privateKey);
                 m_storage.insertGrDPStorageCacheElement(cacheElem);
             }
         } catch (GrDPStorageException e) {
             logger.error("Failed to put certificate request in storage.", e);
             throw new DelegationException("Internal failure.");
         }
         logger.debug("New certificate request successfully stored in cache.");
 
         return certRequest;
     }
 
 }
