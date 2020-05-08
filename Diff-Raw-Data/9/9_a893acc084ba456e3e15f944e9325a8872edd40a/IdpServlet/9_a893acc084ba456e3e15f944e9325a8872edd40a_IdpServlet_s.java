 /**-----------------------------------------------------------------------
   
 Copyright (c) 2009, The University of Manchester, United Kingdom.
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
 
 -----------------------------------------------------------------------*/
 package uk.ac.manchester.rcs.bruno.samlredirector.idp.servlet;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.security.InvalidKeyException;
 import java.security.KeyStore;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.crypto.dsig.SignatureMethod;
 
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.common.SAMLObject;
 import org.opensaml.common.binding.BasicSAMLMessageContext;
 import org.opensaml.common.binding.SAMLMessageContext;
 import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
 import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
 import org.opensaml.saml2.core.AuthnRequest;
 import org.opensaml.saml2.core.Response;
 import org.opensaml.ws.message.decoder.MessageDecodingException;
 import org.opensaml.ws.message.encoder.MessageEncodingException;
 import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
 import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
 import org.opensaml.xml.Configuration;
 import org.opensaml.xml.ConfigurationException;
 import org.opensaml.xml.XMLObjectBuilderFactory;
 import org.opensaml.xml.security.SecurityException;
 import org.opensaml.xml.security.SecurityHelper;
 import org.opensaml.xml.security.credential.Credential;
 import org.restlet.data.Reference;
 
import com.noelios.restlet.util.Base64;

 import uk.ac.manchester.rcs.bruno.samlredirector.idp.SamlAuthnResponseBuilder;
 
 import net.java.dev.sommer.foafssl.principals.FoafSslPrincipal;
 import net.java.dev.sommer.foafssl.verifier.DereferencingFoafSslVerifier;
 import net.java.dev.sommer.foafssl.verifier.FoafSslVerifier;
 
 /**
  * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
  * 
  */
 @SuppressWarnings("serial")
 public class IdpServlet extends HttpServlet {
     static {
         XMLObjectBuilderFactory xmlObjectBuilderFactory = Configuration.getBuilderFactory();
         if (xmlObjectBuilderFactory.getBuilders().isEmpty()) {
             try {
                 DefaultBootstrap.bootstrap();
             } catch (ConfigurationException e) {
                 throw new RuntimeException(e);
             }
             xmlObjectBuilderFactory = Configuration.getBuilderFactory();
         }
     }
 
     public final static String KEYSTORE_JNDI_INITPARAM = "keystore";
     public final static String DEFAULT_KEYSTORE_JNDI_INITPARAM = "keystore/signingKeyStore";
     public final static String KEYSTORE_PATH_INITPARAM = "keystorePath";
     public final static String KEYSTORE_TYPE_INITPARAM = "keystoreType";
     public final static String KEYSTORE_PASSWORD_INITPARAM = "keystorePassword";
     public final static String ISSUER_NAME_INITPARAM = "issuerName";
     public final static String KEY_NAME_INITPARAM = "keyName";
 
     public final static String KEY_PASSWORD_INITPARAM = "keyPassword";
     public final static String ALIAS_INITPARAM = "keyAlias";
 
     private static FoafSslVerifier FOAF_SSL_VERIFIER = new DereferencingFoafSslVerifier();
 
     private Credential signingCredential = null;
     private String issuerName = null;
     private String keyName = null;
 
     /**
      * Initialises the servlet: loads the keystore/keys to use to sign the
      * assertions and the issuer name.
      */
     @Override
     public void init() throws ServletException {
         KeyStore keyStore = null;
 
         String keystoreJdniName = getInitParameter(KEYSTORE_JNDI_INITPARAM);
         if (keystoreJdniName == null) {
             keystoreJdniName = DEFAULT_KEYSTORE_JNDI_INITPARAM;
         }
         String keystorePath = getInitParameter(KEYSTORE_PATH_INITPARAM);
         String keystoreType = getInitParameter(KEYSTORE_TYPE_INITPARAM);
         String keystorePassword = getInitParameter(KEYSTORE_PASSWORD_INITPARAM);
         String keyPassword = getInitParameter(KEY_PASSWORD_INITPARAM);
         if (keyPassword == null)
             keyPassword = keystorePassword;
         String alias = getInitParameter(ALIAS_INITPARAM);
         String issuerName = getInitParameter(ISSUER_NAME_INITPARAM);
         String keyName = getInitParameter(KEY_NAME_INITPARAM);
 
         try {
             Context ctx = null;
             try {
                 keyStore = (KeyStore) new InitialContext().lookup("java:comp/env/"
                         + keystoreJdniName);
 
             } finally {
                 if (ctx != null) {
                     ctx.close();
                 }
             }
         } catch (NameNotFoundException e) {
         } catch (NamingException e) {
             throw new ServletException(e);
         }
         if (keyStore == null) {
             try {
                 InputStream ksInputStream = null;
 
                 try {
                     if (keystorePath != null) {
                         ksInputStream = new FileInputStream(keystorePath);
                     }
                     keyStore = KeyStore.getInstance((keystoreType != null) ? keystoreType
                             : KeyStore.getDefaultType());
                     keyStore.load(ksInputStream, keystorePassword != null ? keystorePassword
                             .toCharArray() : null);
                 } finally {
                     if (ksInputStream != null) {
                         ksInputStream.close();
                     }
                 }
             } catch (FileNotFoundException e) {
                 throw new ServletException("Could not load keystore.");
             } catch (KeyStoreException e) {
                 throw new ServletException("Could not load keystore.");
             } catch (NoSuchAlgorithmException e) {
                 throw new ServletException("Could not load keystore.");
             } catch (CertificateException e) {
                 throw new ServletException("Could not load keystore.");
             } catch (IOException e) {
                 throw new ServletException("Could not load keystore.");
             }
         }
 
         try {
             if (alias == null) {
                 Enumeration<String> aliases = keyStore.aliases();
                 while (aliases.hasMoreElements()) {
                     String tempAlias = aliases.nextElement();
                     if (keyStore.isKeyEntry(tempAlias)) {
                         alias = tempAlias;
                         break;
                     }
                 }
             }
             if (alias == null) {
                 throw new ServletException(
                         "Invalid keystore configuration: alias unspecified or couldn't find key at alias: "
                                 + alias);
             }
             Credential signingCredential = SecurityHelper.getSimpleCredential(keyStore
                     .getCertificate(alias).getPublicKey(), (PrivateKey) keyStore.getKey(alias,
                     keyPassword != null ? keyPassword.toCharArray() : null));
             synchronized (this) {
                 this.signingCredential = signingCredential;
                 this.issuerName = issuerName;
                 this.keyName = keyName;
             }
         } catch (UnrecoverableKeyException e) {
             throw new ServletException("Could not load keystore.");
         } catch (KeyStoreException e) {
             throw new ServletException("Could not load keystore.");
         } catch (NoSuchAlgorithmException e) {
             throw new ServletException("Could not load keystore.");
         }
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         Collection<? extends FoafSslPrincipal> verifiedWebIDs = null;
 
         /*
          * Verifies the certificate passed in the request.
          */
         X509Certificate[] certificates = (X509Certificate[]) request
                 .getAttribute("javax.servlet.request.X509Certificate");
         if (certificates != null) {
             X509Certificate foafSslCertificate = certificates[0];
             try {
                 verifiedWebIDs = FOAF_SSL_VERIFIER.verifyFoafSslCertificate(foafSslCertificate);
             } catch (Exception e) {
                 throw new RuntimeException("Certificate verification failed.");
             }
         }
 
         if ((verifiedWebIDs != null) && (verifiedWebIDs.size() > 0)) {
             String samlRequestParam = request.getParameter("SAMLRequest");
             String simpleRequestParam = request.getParameter("FoafSslAuthnReqIssuer");
 
             if ((samlRequestParam != null) && (samlRequestParam.length() > 0)) {
                 /*
                  * Reads the SAML request and generates the SAML response.
                  */
                 BasicSAMLMessageContext<AuthnRequest, Response, SAMLObject> msgContext = new BasicSAMLMessageContext<AuthnRequest, Response, SAMLObject>();
                 msgContext.setInboundMessageTransport(new HttpServletRequestAdapter(request));
 
                 HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
 
                 try {
                     decoder.decode(msgContext);
                     AuthnRequest authnRequest = msgContext.getInboundSAMLMessage();
                     final String consumerServiceUrl = authnRequest.getAssertionConsumerServiceURL();
 
                     URI webId = verifiedWebIDs.iterator().next().getUri();
 
                     Credential signingCredential = null;
                     String issuerName = null;
                     String keyname = null;
                     synchronized (this) {
                         signingCredential = this.signingCredential;
                         issuerName = this.issuerName;
                         keyname = this.keyName;
                     }
                     Response samlResponse = SamlAuthnResponseBuilder.getInstance()
                             .buildSubjectAuthenticatedAssertion(URI.create(issuerName),
                                     Collections.singletonList(URI.create(consumerServiceUrl)),
                                     webId, null, keyname);
 
                     msgContext.setOutboundMessageTransport(new HttpServletResponseAdapter(response,
                             false));
                     msgContext.setOutboundSAMLMessage(samlResponse);
                     msgContext.setOutboundSAMLMessageSigningCredential(signingCredential);
 
                     HTTPRedirectDeflateEncoder httpEncoder = new HTTPRedirectDeflateEncoder() {
                         @SuppressWarnings("unchecked")
                         @Override
                         protected String getEndpointURL(SAMLMessageContext messageContext)
                                 throws MessageEncodingException {
                             return consumerServiceUrl;
                         }
                     };
                     httpEncoder.encode(msgContext);
                 } catch (MessageDecodingException e) {
                     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                     throw new RuntimeException("Error when decoding the request.", e);
                 } catch (SecurityException e) {
                     response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                     throw new RuntimeException("Error when decoding the request.", e);
                 } catch (MessageEncodingException e) {
                     throw new RuntimeException("Error when encoding the response.", e);
                 }
 
             } else if ((simpleRequestParam != null) && (simpleRequestParam.length() > 0)) {
                 /*
                  * Reads the FoafSsl simple auth request.
                  */
                 Reference authnRespResourceRef = new Reference(simpleRequestParam);
 
                 Credential signingCredential = null;
                 synchronized (this) {
                     signingCredential = this.signingCredential;
                 }
 
                 PrivateKey privKey = signingCredential.getPrivateKey();
                 String sigAlg = null;
                 String sigAlgUri = null;
                 if ("RSA".equals(privKey.getAlgorithm())) {
                     sigAlg = "SHA1withRSA";
                     sigAlgUri = SignatureMethod.RSA_SHA1;
                 } else if ("DSA".equals(privKey.getAlgorithm())) {
                     sigAlg = "SHA1withDSA";
                     sigAlgUri = SignatureMethod.DSA_SHA1;
                 } else {
                     return;
                 }
 
                 URI webId = verifiedWebIDs.iterator().next().getUri();
                 authnRespResourceRef.addQueryParameter("FoafSslAuthnUri", webId.toASCIIString());
                 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                 authnRespResourceRef.addQueryParameter("FoafSslAuthnDateTime", dateFormat
                         .format(Calendar.getInstance().getTime()));
                 authnRespResourceRef.addQueryParameter("SigAlg", sigAlgUri);
 
                 String signedMessage = authnRespResourceRef.toString();
                 try {
                     Signature signature = Signature.getInstance(sigAlg);
                     signature.initSign(privKey);
                     signature.update(signedMessage.getBytes("UTF-8"));
                     byte[] signatureBytes = signature.sign();
                    authnRespResourceRef.addQueryParameter("Signature", Base64.encode(
                            signatureBytes, false));
                 } catch (InvalidKeyException e) {
                     return;
                 } catch (NoSuchAlgorithmException e) {
                     return;
                 } catch (SignatureException e) {
                     return;
                 }
 
                 response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                 response.setHeader("Location", authnRespResourceRef.toString());
             } else {
                 response.getWriter().print(verifiedWebIDs.iterator().next().getName());
                 return;
             }
         } else {
             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         }
     }
 }
