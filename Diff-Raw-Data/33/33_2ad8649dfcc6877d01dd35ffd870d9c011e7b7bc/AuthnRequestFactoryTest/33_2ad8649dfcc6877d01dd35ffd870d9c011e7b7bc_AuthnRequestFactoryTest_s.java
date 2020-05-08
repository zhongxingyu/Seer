 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package test.unit.net.link.safeonline.sdk.auth.saml2;
 
 import static org.junit.Assert.*;
 
 import java.security.KeyPair;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import net.link.safeonline.sdk.auth.protocol.saml2.AuthnRequestFactory;
 import net.link.safeonline.sdk.auth.protocol.saml2.LinkIDSaml2Utils;
 import net.link.util.common.CertificateChain;
 import net.link.util.common.DomUtils;
 import net.link.util.test.pkix.PkiTestUtils;
 import net.link.util.test.web.DomTestUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.xml.security.utils.Constants;
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.opensaml.common.xml.SAMLConstants;
 import org.opensaml.saml2.core.AuthnRequest;
 import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 /**
  * Unit test for authentication request factory.
  *
  * @author fcorneli
  */
 public class AuthnRequestFactoryTest {
 
     private static final Log LOG = LogFactory.getLog( AuthnRequestFactoryTest.class );
 
     @Test
     public void createAuthnRequest()
             throws Exception {
 
         // Setup Data
         String applicationName = "test-application-id";
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
         String destinationURL = "https://test.idp.com/entry";
         String device = "device";
         String session = "test-session-info";
 
         // Test
         long begin = System.currentTimeMillis();
         Set<String> devices = Collections.singleton( device );
         AuthnRequest samlAuthnRequest = AuthnRequestFactory.createAuthnRequest( applicationName, null, null, assertionConsumerServiceURL,
                 destinationURL, devices, false, session );
         String samlAuthnRequestToken = LinkIDSaml2Utils.sign( samlAuthnRequest, keyPair, null );
 
         LOG.debug( DomUtils.domToString( LinkIDSaml2Utils.marshall( samlAuthnRequest ) ) );
 
         long end = System.currentTimeMillis();
 
         // Verify
         assertNotNull( samlAuthnRequest );
         LOG.debug( "duration: " + (end - begin) + " ms" );
         LOG.debug( "result message: " + samlAuthnRequest );
 
         Document resultDocument = DomTestUtils.parseDocument( samlAuthnRequestToken );
         AuthnRequest resultAuthnRequest = (AuthnRequest) LinkIDSaml2Utils.unmarshall( resultDocument.getDocumentElement() );
 
         assertNotNull( resultAuthnRequest );
         assertNotNull( resultAuthnRequest.getSignature() );
 
         assertNotNull( resultAuthnRequest.getIssuer() );
         assertEquals( applicationName, resultAuthnRequest.getIssuer().getValue() );
 
         assertNotNull( resultAuthnRequest.getAssertionConsumerServiceURL() );
         assertEquals( assertionConsumerServiceURL, resultAuthnRequest.getAssertionConsumerServiceURL() );
 
         assertNotNull( resultAuthnRequest.getProtocolBinding() );
         assertEquals( SAMLConstants.SAML2_POST_BINDING_URI, resultAuthnRequest.getProtocolBinding() );
 
         assertNotNull( resultAuthnRequest.getDestination() );
         assertEquals( destinationURL, resultAuthnRequest.getDestination() );
 
         assertNotNull( resultAuthnRequest.getNameIDPolicy() );
         assertNotNull( resultAuthnRequest.getNameIDPolicy().getAllowCreate() );
         assertTrue( resultAuthnRequest.getNameIDPolicy().getAllowCreate() );
 
         // verify signature
         LinkIDSaml2Utils.validateSignature( resultAuthnRequest.getSignature(), null, null );
     }
 
     @Test
     public void createAuthnRequestWithCertificateChain()
             throws Exception {
 
         // Setup Data
         String applicationName = "test-application-id";
         String assertionConsumerServiceURL = "http://test.assertion.consumer.service";
         String destinationURL = "https://test.idp.com/entry";
         String device = "device";
         String session = "test-session-info";
 
         KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
         X509Certificate rootCertificate = PkiTestUtils.generateSelfSignedCertificate( rootKeyPair, "CN=Root" );
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         DateTime notBefore = new DateTime();
         DateTime notAfter = notBefore.plusYears( 1 );
         X509Certificate certificate = PkiTestUtils.generateCertificate( keyPair.getPublic(), "CN=Test", rootKeyPair.getPrivate(),
                 rootCertificate, notBefore, notAfter, null, true, false, false, null );
         CertificateChain certificateChain = new CertificateChain( rootCertificate, certificate );
 
         // Test
         long begin = System.currentTimeMillis();
         Set<String> devices = Collections.singleton( device );
         AuthnRequest samlAuthnRequest = AuthnRequestFactory.createAuthnRequest( applicationName, null, null, assertionConsumerServiceURL,
                 destinationURL, devices, false, session );
         String samlAuthnRequestToken = LinkIDSaml2Utils.sign( samlAuthnRequest, keyPair, certificateChain );
         long end = System.currentTimeMillis();
 
         // Verify
         assertNotNull( samlAuthnRequest );
         LOG.debug( "duration: " + (end - begin) + " ms" );
         LOG.debug( "result message: " + samlAuthnRequest );
 
         Document resultDocument = DomTestUtils.parseDocument( samlAuthnRequestToken );
         AuthnRequest resultAuthnRequest = (AuthnRequest) LinkIDSaml2Utils.unmarshall( resultDocument.getDocumentElement() );
 
         // verify signature
         assertNotNull( resultAuthnRequest.getSignature() );
         assertNotNull( resultAuthnRequest.getSignature().getKeyInfo() );
 
        List<X509Certificate> resultCertificateChain = KeyInfoHelper.getCertificates( resultAuthnRequest.getSignature().getKeyInfo() );
        assertEquals( 2, resultCertificateChain.size() );
        assertEquals( rootCertificate, resultCertificateChain.get( 0 ) );
        assertEquals( certificate, resultCertificateChain.get( 1 ) );
 
         LinkIDSaml2Utils.validateSignature( resultAuthnRequest.getSignature(), null, null );
     }
 
     private static Element createNsElement(Document document) {
 
         Element nsElement = document.createElement( "nsElement" );
         nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:samlp2", "urn:oasis:names:tc:SAML:2.0:protocol" );
         nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:saml2", "urn:oasis:names:tc:SAML:2.0:assertion" );
         return nsElement;
     }
 }
