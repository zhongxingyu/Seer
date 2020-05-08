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
 //: The Initial Developer of the Original Code is Alistair Young alistair@smo.uhi.ac.uk.
 //: Portions created by SMO WWW Development Group are Copyright (C) 2005 SMO WWW Development Group.
 //: All Rights Reserved.
 //:
 /* CVS Header
    $Id$
    $Log$
   Revision 1.12  2007/01/12 13:44:32  alistairskye
   Fixed bug where it synchronising on SOAPUtils

    Revision 1.11  2006/11/27 10:40:35  alistairskye
    Added createTrustStore()
 
    Revision 1.10  2006/11/24 13:31:42  alistairskye
    Modified sign() to use SecUtilsConfig
 
    Revision 1.9  2006/11/22 14:50:35  alistairskye
    Removed unused import
 
    Revision 1.8  2006/11/22 14:49:20  alistairskye
    Added createSelfSignedKeystore()
 
    Revision 1.7  2005/10/20 16:06:46  alistairskye
    Added encrypt()
 
    Revision 1.6  2005/08/16 10:34:50  alistairskye
    Changed sign() to use XUtils.getNodeValue(Node, String)
 
    Revision 1.5  2005/07/19 14:16:41  alistairskye
    Modified sign() to use new namespace aware org.guanxi.samuel.utils.XUtils
 
    Revision 1.4  2005/07/18 15:52:01  alistairskye
    Added urn:guanxi:idp namespace to sign()
 
    Revision 1.3  2005/07/11 10:52:05  alistairskye
    Package restructure
 
    Revision 1.2  2005/05/11 13:11:53  alistairskye
    Updated sign() to use new keyType in configNode to support DSA/RSA
 
    Revision 1.1  2005/05/04 13:29:00  alistairskye
    Moved here from org.Guanxi.SAMUEL.Utils
 
    Revision 1.1  2005/04/28 13:32:09  alistairskye
    Security utils for SAMUEL generated SAML messages
 
 */
 
 package org.guanxi.common.security;
 
 import org.w3c.dom.Document;
 import org.apache.xml.security.signature.XMLSignature;
 import org.apache.xml.security.c14n.Canonicalizer;
 import org.apache.xml.security.transforms.Transforms;
 import org.apache.xml.security.transforms.params.InclusiveNamespaces;
 import org.apache.xml.security.exceptions.XMLSecurityException;
 import org.guanxi.common.GuanxiException;
 import org.bouncycastle.asn1.x509.X509Name;
 import org.bouncycastle.x509.X509V3CertificateGenerator;
 import java.security.*;
 import java.security.cert.X509Certificate;
 import java.security.cert.CertificateException;
 import java.io.*;
 import java.util.Hashtable;
 import java.util.Date;
 import java.util.Random;
 import java.math.BigInteger;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class SecUtils {
   static private SecUtils _instance = null;
 
   static public SecUtils getInstance() {
     if (_instance == null) {
      synchronized(SecUtils.class) {
         if (_instance == null) {
           _instance = new SecUtils();
         }
       }
     }
 
     return _instance;
   }
 
   private SecUtils() {
     org.apache.xml.security.Init.init();
   }
 
   public Document sign(SecUtilsConfig config, Document inDocToSign, String inElementToSign) {
     String keystoreType = config.getKeystoreType();
     String keystoreFile = config.getKeystoreFile();
     String keystorePass = config.getKeystorePass();
     String privateKeyAlias = config.getPrivateKeyAlias();
     String privateKeyPass = config.getPrivateKeyPass();
     String certificateAlias = config.getCertificateAlias();
     String keyType = config.getKeyType();
 
     if (keyType.equalsIgnoreCase("dsa")) keyType = XMLSignature.ALGO_ID_SIGNATURE_DSA;
     if (keyType.equalsIgnoreCase("rsa")) keyType = XMLSignature.ALGO_ID_SIGNATURE_RSA;
 
     try {
       KeyStore ks = null;
       ks = KeyStore.getInstance(keystoreType);
 
       FileInputStream fis = null;
       fis = new FileInputStream(keystoreFile);
 
       ks.load(fis, keystorePass.toCharArray());
 
       PrivateKey privateKey = null;
       privateKey = (PrivateKey)ks.getKey(privateKeyAlias, privateKeyPass.toCharArray());
 
       XMLSignature sig = null;
       sig = new XMLSignature(inDocToSign, "", keyType, Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
 
       // For Shibboleth 1.2.1, <ds:Signature> must be the first element after the root
       inDocToSign.getDocumentElement().insertBefore(sig.getElement(), inDocToSign.getDocumentElement().getFirstChild());
 
       Transforms transforms = new Transforms(sig.getDocument());
 
       /* First we have to strip away the signature element (it's not part of the
        * signature calculations). The enveloped transform can be used for this.
        */
       transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
       transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
       transforms.item(1).getElement().appendChild(inDocToSign.createTextNode(new InclusiveNamespaces(inDocToSign, "#default saml samlp ds code kind rw typens").getInclusiveNamespaces()));
 
       sig.addDocument(inElementToSign, transforms);
 
       //Add in the KeyInfo for the certificate that we used the private key of
       X509Certificate cert = null;
       cert = (X509Certificate)ks.getCertificate(certificateAlias);
 
       sig.addKeyInfo(cert);
 
       sig.addKeyInfo(cert.getPublicKey());
 
       sig.sign(privateKey);
     }
     catch(XMLSecurityException xse) {}
     catch(KeyStoreException kse) {}
     catch(FileNotFoundException fnfe) {}
     catch(NoSuchAlgorithmException nsae) {}
     catch(CertificateException ce) {}
     catch(IOException ioe) {}
     catch(UnrecoverableKeyException urke) {}
 
     return inDocToSign;
   }
 
   public String encrypt(String data) {
     try {
       char[] hexChars ={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
       MessageDigest md5 = MessageDigest.getInstance("MD5");
       md5.reset();
       md5.update(data.getBytes());
       byte[] hashBytes = md5.digest();
       String hex = "";
       int msb;
       int lsb = 0;
       int i;
       for (i = 0; i < hashBytes.length; i++) {
         msb = ((int)hashBytes[i] & 0x000000FF) / 16;
         lsb = ((int)hashBytes[i] & 0x000000FF) % 16;
         hex = hex + hexChars[msb] + hexChars[lsb];
       }
       return(hex);
     }
     catch (NoSuchAlgorithmException e) {
       return null;
     }
   }
 
   /**
    * Generates a self signed public/private key pair and puts them and the associated certificate in
    * a KeyStore.
    *
    * @param cn The CN of the X509 containing the public key, e.g. "cn=guanxi_sp,ou=smo,o=uhi"
    * @param keystoreFile The full path and name of the KeyStore to create or add the certificate to
    * @param keystorePassword The password for the KeyStore
    * @param privateKeyPassword The password for the private key associated with the public key certificate
    * @param privateKeyAlias The alias under which the private key will be stored
    * @throws GuanxiException if an error occurred
    */
   public void createSelfSignedKeystore(String cn, String keystoreFile, String keystorePassword,
                                        String privateKeyPassword, String privateKeyAlias) throws GuanxiException {
     try {
       KeyStore ks = KeyStore.getInstance("JKS");
 
       // Does the keystore exist?
       File keyStore = new File(keystoreFile);
       if (keyStore.exists())
         ks.load(new FileInputStream(keystoreFile), keystorePassword.toCharArray());
       else
         ks.load(null, null);
 
       // Generate a new public/private key pair
       KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
       keyGen.initialize(1024, new SecureRandom());
       KeyPair keypair = keyGen.generateKeyPair();
       PrivateKey privkey = keypair.getPrivate();
       PublicKey pubkey = keypair.getPublic();
 
       /* Set the attributes of the X509 Certificate that will contain the public key.
        * This is a self signed certificate so the issuer and subject will be the same.
        */
       Hashtable attrs = new Hashtable();
       attrs.put(X509Name.CN, cn);
       X509Name issuerDN = new X509Name(attrs);
       X509Name subjectDN = new X509Name(attrs);
 
       // Certificate valid from now
       Date validFrom = new Date();
       validFrom.setTime(validFrom.getTime() - (10 * 60 * 1000));
       Date validTo = new Date();
       validTo.setTime(validTo.getTime() + (20 * (24 * 60 * 60 * 1000)));
 
       // Initialise the X509 Certificate information...
       X509V3CertificateGenerator x509 = new X509V3CertificateGenerator();
       x509.setSignatureAlgorithm("SHA1withDSA");
       x509.setIssuerDN(issuerDN);
       x509.setSubjectDN(subjectDN);
       x509.setPublicKey(pubkey);
       x509.setNotBefore(validFrom);
       x509.setNotAfter(validTo);
       x509.setSerialNumber(new BigInteger(128, new Random()));
 
       // ...generate it...
       X509Certificate[] cert = new X509Certificate[1];
       cert[0] = x509.generateX509Certificate(privkey);
 
       // ...and add the self signed certificate as the certificate chain
       java.security.cert.Certificate[] chain = new java.security.cert.Certificate[1];
       chain[0] = cert[0];
 
       // Under the alias, store the X509 Certificate and it's public key...
       ks.setKeyEntry(privateKeyAlias, privkey, privateKeyPassword.toCharArray(), cert);
       // ...and the chain...
       ks.setKeyEntry(privateKeyAlias, privkey, privateKeyPassword.toCharArray(), chain);
       // ...and write the keystore to disk
       ks.store(new FileOutputStream(keystoreFile), keystorePassword.toCharArray());
     }
     catch(Exception se) {
       /* We'll end up here if a security manager is installed and it refuses us
        * permission to add the BouncyCastle provider
        */
       throw new GuanxiException(se);
     }
   }
 
   /**
    * Creates an empty truststore
    *
    * @param trustStoreFile Full path and name of the truststore to create
    * @param trustStorePassword Password for the truststore
    * @throws GuanxiException if an error occurred
    */
   public void createTrustStore(String trustStoreFile, String trustStorePassword) throws GuanxiException {
     try {
       KeyStore trustStore = KeyStore.getInstance("JKS");
       File truststoreFile = new File(trustStoreFile);
       trustStore.load(null, null);
       trustStore.store(new FileOutputStream(truststoreFile), trustStorePassword.toCharArray());
     }
     catch(Exception se) {
       throw new GuanxiException(se);
     }
   }
 }
