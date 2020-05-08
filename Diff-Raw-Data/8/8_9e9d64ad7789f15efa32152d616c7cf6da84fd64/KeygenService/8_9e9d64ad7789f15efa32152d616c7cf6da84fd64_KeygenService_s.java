 /*
 Copyright (c) 2008-2010, The University of Manchester, United Kingdom.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
  * Neither the name of the The University of Manchester nor the names of
       its contributors may be used to endorse or promote products derived
       from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 
   Author........: Bruno Harbulot
   Contributor...: Henry Story
  */
 
 package net.bblfish.dev.foafssl.keygen.impl;
 
 import net.bblfish.dev.foafssl.keygen.Certificate;
 import org.bouncycastle.jce.PKCS10CertificationRequest;
 import org.bouncycastle.jce.netscape.NetscapeCertRequest;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.util.encoders.Base64;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.math.BigInteger;
 import java.net.URL;
 import java.security.*;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static net.bblfish.dev.foafssl.keygen.impl.DefaultPubKey.create;
 
 /**
  * Component that can then be called by XWiki scripts, that can then call KeygenService.
  * <p/>
  * @author Bruno Harbulot
  * @author Henry Story
  * @since Feb 17, 2010
  */
 public class KeygenService implements net.bblfish.dev.foafssl.keygen.KeygenService {
     KeyStore keyStore;
     PrivateKey privateKey;
     X509Certificate certificate;
     SecureRandom numberGenerator;
     final Logger log = Logger.getLogger(KeygenService.class.getName());
     static {
          if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
              Security.addProvider(new BouncyCastleProvider());
          } else throw new Error("missing BouncyCastleProvider -- add jars to classpath");
      }
 
     /**
      * partly taken from UUID class. Generates random numbers
      *
      * @return a UUID BigInteger
      */
     BigInteger nextRandom() {
         SecureRandom ng = numberGenerator;
         if (ng == null) {
             numberGenerator = ng = new SecureRandom();
         }
 
         byte[] randomBytes = new byte[16];
         ng.nextBytes(randomBytes);
         return new BigInteger(randomBytes).abs();
     }
 
    protected void activate()  {
         log.info("in keygen activate");
         try {
             initialize();
         } catch (Exception e) {
             log.log(Level.SEVERE,"could not activate keygen component",e);
             throw new Error("could not activate keygen component",e);
         }
     }
 
     public void initialize() throws Exception {
         log.info("initializing " + this.getClass().getCanonicalName());
         URL certFile = KeygenService.class.getResource("/cacert.p12");
         InputStream in;
         try {
             in = certFile.openStream();
         } catch (IOException e) {
             throw new Exception("could not load cert file " + certFile);
         }
         String alias = null;
 
         try {              
             keyStore = KeyStore.getInstance("PKCS12");
         } catch (KeyStoreException e) {
             throw new Exception("could not get instance of PKCS12 keystore! SEVERE!", e);
         }
         try {
             keyStore.load(in, "testtest".toCharArray());  //the p12 keystore should really have no password! no need.
         } catch (CertificateException e) {
             throw new Exception("certificate extension found while loading store!", e);
         } catch (NoSuchAlgorithmException e) {
             throw new Exception("missing algorithm for reading store!", e);
         } catch (IOException e) {
             throw new Exception("Could not read keystore shipped with jar!", e);
         }
 
         // for some reason we don't have a fixed alias...
         // some tools produce aliases, others don't, so we search, though really we should have this fixed
         try {
             Enumeration<String> aliases = keyStore.aliases();
             while (aliases.hasMoreElements()) {
                 String tempAlias = aliases.nextElement();
                 if (keyStore.isKeyEntry(tempAlias)) {
                     alias = tempAlias;
                     break;
                 }
             }
         } catch (KeyStoreException e) {
             throw new Exception("could not find alias", e);
         }
 
 
         if (alias == null) {
             throw new Exception(
                     "Invalid keystore configuration: alias unspecified ");
         }
 
         try {
             privateKey = (PrivateKey) keyStore.getKey(alias, "testtest".toCharArray());
         } catch (KeyStoreException e) {
             throw new Exception("could not get key with alias " + alias, e);
         } catch (NoSuchAlgorithmException e) {
             throw new Exception("missing algorithm for reading store!", e);
         } catch (UnrecoverableKeyException e) {
             throw new Exception("could not recover private key in store", e);
         }
 
 
         try {
             certificate = (X509Certificate) keyStore.getCertificate(alias);
         } catch (KeyStoreException e) {
             throw new Exception("problem getting certificate with alias " + alias + "from keystore.", e);
         }
        log.info("Initialization of " + this.getClass().getCanonicalName() + " successfull.");
     }
 
     public Certificate createFromPEM(String pemCsr) {
         PEMReader pemReader = new PEMReader(new StringReader(pemCsr));
         Object pemObject;
         try {
             pemObject = pemReader.readObject();
             if (pemObject instanceof PKCS10CertificationRequest) {
                 PKCS10CertificationRequest pkcs10Obj = (PKCS10CertificationRequest) pemObject;
                 DefaultCertificate cert = new DefaultCertificate(this);
                 cert.setDefaultSerialisation(new PEMSerialisation(cert));
                 try {
                     cert.setSubjectPublicKey(create(pkcs10Obj.getPublicKey()));
                     return cert;
                 } catch (NoSuchAlgorithmException e) {
                     log.log(Level.SEVERE, "Don't know algorithm required by certification request ", e);
                 } catch (NoSuchProviderException e) {
                     log.log(Level.SEVERE,"Don't have provider for certification request ", e);
                 } catch (InvalidKeyException e) {
                     log.log(Level.WARNING,"Invalid key sent in certificate request", e);
                 }
             }
         } catch (IOException e) {
             log.log(Level.SEVERE,"How can this happen? Serious! An IOEXception on a StringReader?", e);
         }
         return null;
     }
 
     public Certificate createFromSpkac(String spkac) throws InvalidParameterException {
         if (spkac == null) throw new InvalidParameterException("SPKAC parameter is null");
         try {
             NetscapeCertRequest certRequest = new NetscapeCertRequest(Base64.decode(spkac));
             DefaultCertificate cert = new DefaultCertificate(this);
             cert.setDefaultSerialisation(new DERSerialisation(cert));
             cert.setSubjectPublicKey(create(certRequest.getPublicKey()));
             return cert;
         } catch (IOException e) {
             log.log(Level.SEVERE,"how can an IOError occur when reading a string?", e);
         }
         return null;
     }
 
 }
