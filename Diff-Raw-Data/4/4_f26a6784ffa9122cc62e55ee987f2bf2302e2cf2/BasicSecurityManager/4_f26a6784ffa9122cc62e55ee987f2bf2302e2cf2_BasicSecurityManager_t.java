 /*
  * #%L
  * Bitrepository Protocol
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol.security;
 
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.KeyManagementException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.Security;
 import java.security.UnrecoverableKeyException;
 import java.security.KeyStore.PrivateKeyEntry;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.cert.X509Certificate;
 
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManagerFactory;
 
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.settings.collectionsettings.CollectionSettings;
 import org.bitrepository.settings.collectionsettings.InfrastructurePermission;
 import org.bitrepository.settings.collectionsettings.Permission;
 import org.bitrepository.settings.collectionsettings.PermissionSet;
 import org.bouncycastle.cms.CMSException;
 import org.bouncycastle.cms.CMSProcessableByteArray;
 import org.bouncycastle.cms.CMSSignedData;
 import org.bouncycastle.cms.SignerInformation;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.util.encoders.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to handle:
  * - loading of certificates
  * - setup of SSLContext
  * - Authentication of signatures
  * - Signature generation 
  * - Authorization of operations
  */
 public class BasicSecurityManager implements SecurityManager {
     private final Logger log = LoggerFactory.getLogger(BasicSecurityManager.class);
     /** Default password for the in-memory keystore */
     private static final String defaultPassword = "123456";
     /** path to file containing the components private key and certificate */
     private final String privateKeyFile;
     /** CollectionSettings */
     private final CollectionSettings collectionSettings;
     /** Object to authenticate messages */
     private final MessageAuthenticator authenticator;
     /** Object to sign messages */
     private final MessageSigner signer;
     /** Object to authorize operations */
     private final OperationAuthorizor authorizer;
     /** Object storing permissions and certificates */
     private final PermissionStore permissionStore;
     /** int value to keep track of the next keystore alias */
     private static int aliasID = 0;
     /** In memory keyStore */
     private KeyStore keyStore; 
     /** Member for holding the PrivateKeyEntry containing the from privateKeyFile loaded key and certificate */
     private PrivateKeyEntry privateKeyEntry;
     
     /**
      * Constructor for the SecurityManager.
      * @param collectionSettings, the collection settings to retrieve settings from
      * @param privateKeyFile, path to the file containing the components private key and certificate, may be null if not using
      *        certificates and encryption.
      * @param authenticator, MessageAuthenticator for authenticating messages
      * @param signer, MessageSigner for signing messages.
      * @param authorizer, OperationAuthorizer to authorize operations
      * @param permissionStore, the PermissionStore to hold certificates and adjoining permissions  
      */
     public BasicSecurityManager(CollectionSettings collectionSettings, String privateKeyFile, MessageAuthenticator authenticator,
             MessageSigner signer, OperationAuthorizor authorizer, PermissionStore permissionStore) {
         ArgumentValidator.checkNotNull(collectionSettings, "collectionSettings");
         ArgumentValidator.checkNotNull(authenticator, "authenticator");
         ArgumentValidator.checkNotNull(signer, "signer");
         ArgumentValidator.checkNotNull(authorizer, "authorizer");
         ArgumentValidator.checkNotNull(permissionStore, "permissionStore");
         this.privateKeyFile = privateKeyFile;
         this.collectionSettings = collectionSettings;
         this.authenticator = authenticator;
         this.signer = signer;
         this.authorizer = authorizer;
         this.permissionStore = permissionStore;
         initialize();
     }
     
     /**
      * Method to authenticate a message. 
      * @param message, the message that needs to be authenticated. 
      * @param signature, the signature belonging to the message. 
      * @throws MessageAuthenticationException in case of failure.
      */
     public void authenticateMessage(String message, String signature) throws MessageAuthenticationException {
         if(collectionSettings.getProtocolSettings().isRequireMessageAuthentication()) {
             try {
                 byte[] decodedSig = Base64.decode(signature.getBytes(SecurityModuleConstants.defaultEncodingType));
                 byte[] decodeMessage = message.getBytes(SecurityModuleConstants.defaultEncodingType);
                 authenticator.authenticateMessage(decodeMessage, decodedSig);
             } catch (UnsupportedEncodingException e) {
                 throw new SecurityModuleException(SecurityModuleConstants.defaultEncodingType + " encoding not supported", e);
             }
         }
     }
     
     /**
      * Method to sign a message
      * @param message, the message to sign
      * @return String the signature for the message, or null if authentication is disabled. 
      * @throws MessageSigningException if signing of the message fails.   
      */
     public String signMessage(String message) throws MessageSigningException {
         if(collectionSettings.getProtocolSettings().isRequireMessageAuthentication()) {
             try {
                 byte[] signature = signer.signMessage(message.getBytes(SecurityModuleConstants.defaultEncodingType));
                 return new String(Base64.encode(signature));   
             } catch (UnsupportedEncodingException e) {
                 throw new SecurityModuleException(SecurityModuleConstants.defaultEncodingType + " encoding not supported", e);
             }           
         } else { 
             return null;
         }
     }
     
     /**
      * Method to authorize an operation 
      * @param operationType, the type of operation that is to be authorized. 
      * @param messageData, the data of the message request. 
      * @param signature, the signature belonging to the message request. 
      * @throws OperationAuthorizationException in case of failure. 
      */
     public void authorizeOperation(String operationType, String messageData, String signature) 
             throws OperationAuthorizationException {
         if(collectionSettings.getProtocolSettings().isRequireOperationAuthorization()) {
             byte[] decodeSig = Base64.decode(signature.getBytes());
             CMSSignedData s;
             try {
                 s = new CMSSignedData(new CMSProcessableByteArray(messageData.getBytes()), decodeSig);
             } catch (CMSException e) {
                 throw new SecurityModuleException(e.getMessage(), e);
             }
     
             SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
             try {
                 authorizer.authorizeOperation(operationType, signer.getSID());    
             } catch (UnregisteredPermissionException e) {
                 log.info(e.getMessage(), e);
             }
             
             
         }
     }
     
     /**
      * Do initialization work
      * - Creates keystore
      * - Loads private key and certificate
      * - Loads permissions and certificates
      * - Sets up SSLContext
      */
     private void initialize() {
         Security.addProvider(new BouncyCastleProvider());
         try {
             keyStore = KeyStore.getInstance(SecurityModuleConstants.keyStoreType);
             keyStore.load(null);
             loadPrivateKey(privateKeyFile);
             loadInfrastructureCertificates(collectionSettings.getPermissionSet());
             permissionStore.loadPermissions(collectionSettings.getPermissionSet());
             signer.setPrivateKeyEntry(privateKeyEntry);
             setupDefaultSSLContext();
         } catch (Exception e) {
             throw new SecurityModuleException(e.getMessage(), e);
         } 
     }
     
     /**
      * Alias generator for the keystore entries.
      * @return returns a String containing the alias for the next keystore entry 
      */
     private String getNewAlias() {
         return "" + aliasID++;
     }
     
     /**
      * Attempts to load the pillars private key and certificate from a PEM formatted file. 
      * @param privateKeyFile, path to the file containing the components private key and certificate, may be null
      * @throws IOException if the file cannot be found or read. 
      * @throws KeyStoreException if there is problems with adding the privateKeyEntry to keyStore
      * @throws CertificateExpiredException if the certificate has expired 
      * @throws CertificateNotYetValidException if the certificate is not yet valid
      */
     private void loadPrivateKey(String privateKeyFile) throws IOException, KeyStoreException, 
             CertificateExpiredException, CertificateNotYetValidException {
         PrivateKey privKey = null;
         X509Certificate privCert = null;
         if(!(new File(privateKeyFile)).isFile()) {
             log.info("Key file with private key and certificate does not exist!");
             return;
         }
         BufferedReader bufferedReader = new BufferedReader(new FileReader(privateKeyFile));
         PEMReader pemReader =  new PEMReader(bufferedReader);
         Object pemObj = pemReader.readObject();
 
         while(pemObj != null) {
             if(pemObj instanceof X509Certificate) {
                 log.debug("Certificate for PrivateKeyEntry found");
                 privCert = (X509Certificate) pemObj;
             } else if(pemObj instanceof PrivateKey) {
                 log.debug("Key for PrivateKeyEntry found");
                 privKey = (PrivateKey) pemObj;
             } else {
                 log.debug("Got something, thats not X509Certificate or PrivateKey. Class: " + pemObj.getClass().getSimpleName());
             }
             pemObj = pemReader.readObject();
         }
         pemReader.close();
         if(privKey == null || privCert == null ) {
             log.info("No material to create private key entry found!");
         } else {
             privCert.checkValidity();
             privateKeyEntry = new PrivateKeyEntry(privKey, new Certificate[] {privCert});
             keyStore.setEntry(SecurityModuleConstants.privateKeyAlias, privateKeyEntry, 
                     new KeyStore.PasswordProtection(defaultPassword.toCharArray()));
         }
     }
 
     /**
      * Load the appropriate certificates from PermissionSet into trust/keystore 
      * @throws CertificateException if certificate cannot be created from the data
      * @throws KeyStoreException if certificate cannot be put into the keyStore
      */
     private void loadInfrastructureCertificates(PermissionSet permissions) throws CertificateException, KeyStoreException {
         ByteArrayInputStream bs;
        if(permissions == null) {
            log.info("The provided PermissionSet is empty. Continuing without permissions!");
            return;
        }
         for(Permission permission : permissions.getPermission()) {
             if(permission.getInfrastructurePermission().contains(InfrastructurePermission.MESSAGE_BUS_SERVER)) {
                 try {
                     bs = new ByteArrayInputStream(permission.getCertificate());
                     X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance(
                             SecurityModuleConstants.CertificateType).generateCertificate(bs);
                     keyStore.setEntry(getNewAlias(), new KeyStore.TrustedCertificateEntry(certificate), 
                             SecurityModuleConstants.nullProtectionParameter);
                     
                     bs.close();
                 } catch (IOException e) {
                     
                 }
             }
             if(permission.getInfrastructurePermission().contains(InfrastructurePermission.FILE_EXCHANGE_SERVER)) {
                 try {
                     bs = new ByteArrayInputStream(permission.getCertificate());
                     X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance(
                             SecurityModuleConstants.CertificateType).generateCertificate(bs);
                     keyStore.setEntry(getNewAlias(), new KeyStore.TrustedCertificateEntry(certificate), 
                             SecurityModuleConstants.nullProtectionParameter);
                     bs.close();
                 } catch (IOException e) {
                     log.debug("Failed closing ByteArrayInputStream", e);
                 }
             }
         }      
     }
     
     /**
      * Sets up the Default SSL context  
      * @throws NoSuchAlgorithmException
      * @throws KeyStoreException
      * @throws UnrecoverableKeyException
      * @throws KeyManagementException
      */
     private void setupDefaultSSLContext() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, 
             KeyManagementException {
         TrustManagerFactory tmf;
         KeyManagerFactory kmf;
         SSLContext context;
         tmf = TrustManagerFactory.getInstance(SecurityModuleConstants.keyTrustStoreAlgorithm);
         tmf.init(keyStore);
         kmf = KeyManagerFactory.getInstance(SecurityModuleConstants.keyTrustStoreAlgorithm);
         kmf.init(keyStore, defaultPassword.toCharArray());
         context = SSLContext.getInstance(SecurityModuleConstants.defaultSSLProtocol);
         context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecurityModuleConstants.defaultRandom);
         SSLContext.setDefault(context);
     }
 }
