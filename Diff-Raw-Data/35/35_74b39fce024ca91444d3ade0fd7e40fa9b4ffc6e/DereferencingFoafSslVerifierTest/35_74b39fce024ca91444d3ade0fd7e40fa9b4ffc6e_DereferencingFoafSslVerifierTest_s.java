 /*
 New BSD license: http://opensource.org/licenses/bsd-license.php
 
 Copyright (c) 2008-2009 Sun Microsystems, Inc.
 901 San Antonio Road, Palo Alto, CA 94303 USA. 
 All rights reserved.
 
 
 Redistribution and use in source and binary forms, with or without 
 modification, are permitted provided that the following conditions are met:
 
 - Redistributions of source code must retain the above copyright notice, 
 this list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice, 
 this list of conditions and the following disclaimer in the documentation 
 and/or other materials provided with the distribution.
 - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software 
 without specific prior written permission.
 
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
  */
 package net.java.dev.sommer.foafssl.verifier;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLStreamHandler;
 import java.net.URLStreamHandlerFactory;
 import java.security.Security;
 import java.security.cert.X509Certificate;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.PEMReader;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Bruno Harbulot.
  */
 public class DereferencingFoafSslVerifierTest {
 
     public static final String TEST_GOOD_FOAF_FILENAME = "dummy-foaf.rdf.xml";
     public static final String TEST_GOOD_FOAF_XHTML_FILENAME = "dummy-foaf.xhtml.xml";
     public static final String TEST_GOOD_FOAF_HTML_FILENAME = "dummy-foaf.html";
     public static final String TEST_WRONG_FOAF_FILENAME = "dummy-foaf-wrong.rdf.xml";
     public static final String TEST_CERT_FILENAME = "dummy-foafsslcert.pem";
     public static final String TEST_FOAF_LOCATION = "http://foaf.example.net/bruno";
     public static final URI TEST_WEB_ID_URI = URI.create(TEST_FOAF_LOCATION + "#me");
     public static final URL TEST_FOAF_URL;
 
     static {
         try {
             TEST_FOAF_URL = new URL(TEST_FOAF_LOCATION);
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
     private DereferencingFoafSslVerifier verifier;
     private X509Certificate x509Certificate;
 
     @Before
     public void setUp() throws Exception {
         if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
             Security.addProvider(new BouncyCastleProvider());
         }
 
         /*
          * Creates a mock URLConnection not to make outside connections to
          * de-reference the FOAF file for the tests.
          */
         URLStreamHandlerFactory mockStreamHandlerFactory = new URLStreamHandlerFactory() {
 
             public URLStreamHandler createURLStreamHandler(String protocol) {
                 if ("http".equals(protocol) || "https".equals(protocol)) {
                     return new URLStreamHandler() {
 
                         @Override
                         protected URLConnection openConnection(URL u) throws IOException {
                             return new HttpURLConnection(u) {
 
                                 @Override
                                 public void disconnect() {
                                 }
 
                                 @Override
                                 public boolean usingProxy() {
                                     return false;
                                 }
 
                                 @Override
                                 public void connect() throws IOException {
                                 }
 
                                 @Override
                                 public String getContentType() {
                                     return "application/rdf+xml";
                                 }
 
                                 @Override
                                 public InputStream getInputStream() throws IOException {
                                     return DereferencingFoafSslVerifierTest.class
                                             .getResourceAsStream(TEST_GOOD_FOAF_FILENAME);
                                 }
                             };
                         }
                     };
                 }
                 return null;
             }
         };
         try {
             URL.setURLStreamHandlerFactory(mockStreamHandlerFactory);
         } catch (Throwable e) {
         }
 
         this.verifier = new DereferencingFoafSslVerifier();
 
         InputStreamReader certReader = new InputStreamReader(DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream(TEST_CERT_FILENAME));
 
         PEMReader pemReader = new PEMReader(certReader);
         while (pemReader.ready()) {
             Object pemObject = pemReader.readObject();
             if (pemObject instanceof X509Certificate) {
                 x509Certificate = (X509Certificate) pemObject;
                 break;
             } else {
                 throw new RuntimeException("Unknown type of PEM object: " + pemObject);
             }
         }
         pemReader.close();
     }
 
     @Test
     public void testGoodLocalFoafFile() throws Exception {
 
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream(TEST_GOOD_FOAF_FILENAME);
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "application/rdf+xml"));
         } finally {
             foafInputStream.close();
         }
     }
 
     @Test
     public void testGoodLocalFoafXhtmlRDFaFile() throws Exception {
 
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream(TEST_GOOD_FOAF_XHTML_FILENAME);
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "application/xhtml+xml"));
         } finally {
             foafInputStream.close();
         }
     }
 
     @Test
     public void testGoodLocalFoafHtmlRDFaFile() throws Exception {
 
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream(TEST_GOOD_FOAF_HTML_FILENAME);
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
     @Test
     public void testBadLocalFoafFile() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream(TEST_WRONG_FOAF_FILENAME);
         try {
             assertNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "application/rdf+xml"));
         } finally {
             foafInputStream.close();
         }
     }
 
     @Test
     public void testRemoteFoafFile() throws Exception {
         assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                 .getPublicKey(), this.x509Certificate.getNotBefore(), this.x509Certificate
                 .getNotAfter()));
     }
 
     @Test
     public void testLocalBblfishFile_old() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-old.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
     @Test
     public void testLocalBblfishFile_old2() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-old-2.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
 
         @Test
     public void testLocalBblfishLiteraFile_1() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-literal-1.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
     @Test
     public void testLocalBblfishLiteraFile_2() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-literal-2.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
        @Test
     public void testLocalBblfishLiteraFile_3() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-literal-3.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
     public void testLocalBblfishLiteraFile_4() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-literal-5.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
     public void testLocalBblfishLiteraFile_5() throws Exception {
         InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                 .getResourceAsStream("bblfish-foaf-literal-5.xhtml");
 
         try {
             assertNotNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                     .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
         } finally {
             foafInputStream.close();
         }
     }
 
     public void testLocalBblfishLiteraFile_6() throws Exception {
          InputStream foafInputStream = DereferencingFoafSslVerifierTest.class
                  .getResourceAsStream("bblfish-foaf-literal-wrong.xhtml");
 
          try {
              assertNull(this.verifier.verifyByDereferencing(TEST_WEB_ID_URI, this.x509Certificate
                      .getPublicKey(), TEST_FOAF_URL, foafInputStream, "text/html"));
          } finally {
              foafInputStream.close();
          }
      }
 
 
 }
