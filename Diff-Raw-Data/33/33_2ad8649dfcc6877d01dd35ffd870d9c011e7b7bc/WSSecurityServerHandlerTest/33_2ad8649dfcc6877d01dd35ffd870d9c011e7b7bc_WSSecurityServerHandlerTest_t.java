 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package test.unit.net.link.safeonline.sdk.ws;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.io.InputStream;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.util.Set;
 import java.util.Vector;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.soap.*;
 import javax.xml.soap.SOAPConstants;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.handler.MessageContext.Scope;
 import javax.xml.ws.handler.soap.SOAPMessageContext;
 import net.link.util.common.CertificateChain;
 import net.link.util.pkix.ClientCrypto;
 import net.link.util.pkix.ServerCrypto;
 import net.link.util.test.pkix.PkiTestUtils;
 import net.link.util.test.web.DomTestUtils;
 import net.link.util.test.web.ws.TestSOAPMessageContext;
 import net.link.util.ws.security.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ws.security.*;
 import org.apache.ws.security.components.crypto.Crypto;
 import org.apache.ws.security.message.*;
 import org.apache.ws.security.util.WSSecurityUtil;
 import org.apache.xml.security.Init;
 import org.apache.xml.security.utils.Constants;
 import org.apache.xpath.XPathAPI;
 import org.joda.time.Duration;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 
 public class WSSecurityServerHandlerTest {
 
     private static final Log LOG = LogFactory.getLog( WSSecurityServerHandlerTest.class );
 
     private WSSecurityHandler testedInstance;
 
     private WSSecurityConfiguration mockWSSecurityConfiguration;
 
     private Object[] mockObjects;
 
     @Before
     public void setUp()
             throws Exception {
 
         mockWSSecurityConfiguration = createMock( WSSecurityConfiguration.class );
 
         testedInstance = new WSSecurityHandler( mockWSSecurityConfiguration );
         testedInstance.postConstructCallback();
 
         mockObjects = new Object[] { mockWSSecurityConfiguration };
     }
 
     @Test
     public void testOutboundMessageHasTimestamp()
             throws Exception {
 
         // Setup Data
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         CertificateChain certificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" ) );
 
         MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
         InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );
         assertNotNull( testSoapMessageInputStream );
 
         SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );
 
         SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );
         soapMessageContext.put( WSSecurityHandler.CERTIFICATE_CHAIN_PROPERTY, certificateChain );
         soapMessageContext.setScope( WSSecurityHandler.CERTIFICATE_CHAIN_PROPERTY, Scope.APPLICATION );
 
         // Setup Mocks
        expect( mockWSSecurityConfiguration.isCertificateChainTrusted( certificateChain ) ).andStubReturn( true );
        expect( mockWSSecurityConfiguration.isOutboundSignatureNeeded() ).andStubReturn( true );
         expect( mockWSSecurityConfiguration.getIdentityCertificateChain() ).andReturn( certificateChain );
         expect( mockWSSecurityConfiguration.getPrivateKey() ).andReturn( keyPair.getPrivate() );
 
         replay( mockObjects );
 
         // Test
         testedInstance.handleMessage( soapMessageContext );
 
         // Verify
         SOAPMessage resultMessage = soapMessageContext.getMessage();
         SOAPPart resultSoapPart = resultMessage.getSOAPPart();
         LOG.debug( "result SOAP part: " + DomTestUtils.domToString( resultSoapPart ) );
         verify( mockObjects );
         Element nsElement = resultSoapPart.createElement( "nsElement" );
         nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/" );
         nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsse",
                 "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" );
         nsElement.setAttributeNS( Constants.NamespaceSpecNS, "xmlns:wsu",
                 "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" );
         assertNotNull( "missing WS-Security timestamp",
                 XPathAPI.selectSingleNode( resultSoapPart, "/soap:Envelope/soap:Header/wsse:Security/wsu:Timestamp/wsu:Created",
                         nsElement ) );
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testOutboundMessageSigned()
             throws Exception {
 
         // Setup Data
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         CertificateChain certificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" ) );
 
         KeyPair linkidKeyPair = PkiTestUtils.generateKeyPair();
         CertificateChain linkidCertificateChain = new CertificateChain(
                 PkiTestUtils.generateSelfSignedCertificate( linkidKeyPair, "CN=linkID" ) );
 
         MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
         InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );
         assertNotNull( testSoapMessageInputStream );
 
         SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );
 
         SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, true );
         soapMessageContext.put( WSSecurityHandler.CERTIFICATE_CHAIN_PROPERTY, certificateChain );
         soapMessageContext.setScope( WSSecurityHandler.CERTIFICATE_CHAIN_PROPERTY, Scope.APPLICATION );
 
         // Setup Mocks
         expect( mockWSSecurityConfiguration.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );
        expect( mockWSSecurityConfiguration.isCertificateChainTrusted( linkidCertificateChain ) ).andStubReturn( true );
        expect( mockWSSecurityConfiguration.isOutboundSignatureNeeded() ).andStubReturn( true );
        expect( mockWSSecurityConfiguration.getIdentityCertificateChain() ).andReturn( linkidCertificateChain );
        expect( mockWSSecurityConfiguration.getPrivateKey() ).andReturn( linkidKeyPair.getPrivate() );
 
         replay( mockObjects );
 
         // Test
         testedInstance.handleMessage( soapMessageContext );
 
         // verify signed message
         SOAPMessage signedMessage = soapMessageContext.getMessage();
         SOAPPart signedSoapPart = signedMessage.getSOAPPart();
         LOG.debug( "signed SOAP part:" + DomTestUtils.domToString( signedSoapPart ) );
         soapMessageContext.put( MessageContext.MESSAGE_OUTBOUND_PROPERTY, false );
 
         testedInstance.handleMessage( soapMessageContext );
 
         // Verify
         verify( mockObjects );
         CertificateChain resultCertificateChain = WSSecurityHandler.findCertificateChain( soapMessageContext );
         assertTrue( !resultCertificateChain.isEmpty() );
         Set<String> signedElements = (Set<String>) soapMessageContext.get( WSSecurityHandler.SIGNED_ELEMENTS_CONTEXT_KEY );
         assertEquals( 2, signedElements.size() );
         LOG.debug( "signed elements: " + signedElements );
     }
 
     @Test
     public void testInboundMessageOptionalSignedNotSigned()
             throws Exception {
 
         // Setup Data
         KeyPair linkidKeyPair = PkiTestUtils.generateKeyPair();
        CertificateChain linkidCertificateChain = new CertificateChain(
                 PkiTestUtils.generateSelfSignedCertificate( linkidKeyPair, "CN=linkID" ) );
 
         MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
         InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" );
         assertNotNull( testSoapMessageInputStream );
 
         SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );
 
         SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, false );
 
         // Setup Mocks
         expect( mockWSSecurityConfiguration.getIdentityCertificateChain() ).andStubReturn( linkidCertificateChain );
         expect( mockWSSecurityConfiguration.getPrivateKey() ).andStubReturn( linkidKeyPair.getPrivate() );
         expect( mockWSSecurityConfiguration.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );
         expect( mockWSSecurityConfiguration.isInboundSignatureOptional() ).andStubReturn( true );
 
         replay( mockObjects );
 
         // Test
         testedInstance.handleMessage( soapMessageContext );
 
         // Verify
         verify( mockObjects );
     }
 
     @Test
     public void testHandleMessageInvalidSoapBody()
             throws Exception {
 
         // Setup Data
         MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
         InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class.getResourceAsStream(
                 "/test-ws-security-invalid-message.xml" );
         assertNotNull( testSoapMessageInputStream );
 
         SOAPMessage message = messageFactory.createMessage( null, testSoapMessageInputStream );
 
         SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, false );
 
         replay( mockObjects );
 
         // operate & verify
         try {
             testedInstance.handleMessage( soapMessageContext );
             fail();
         }
         catch (RuntimeException e) {
             // Expected
             verify( mockObjects );
         }
     }
 
     static {
         Init.init();
     }
 
     @Test
     public void signatureCheckingFailsWhenTimestampNotSigned()
             throws Exception {
 
         // Setup Data
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         CertificateChain certificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" ) );
 
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware( true );
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document document = documentBuilder.parse( WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" ) );
 
         // use WSSecurityClientHandler to sign message
         LOG.debug( "adding WS-Security SOAP header" );
         WSSecSignature wsSecSignature = new WSSecSignature();
         wsSecSignature.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
         Crypto crypto = new ClientCrypto( certificateChain, keyPair.getPrivate() );
         WSSecHeader wsSecHeader = new WSSecHeader();
         wsSecHeader.insertSecurityHeader( document );
         try {
             wsSecSignature.prepare( document, crypto, wsSecHeader );
 
             org.apache.ws.security.SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants( document.getDocumentElement() );
 
             Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
             WSEncryptionPart wsEncryptionPart = new WSEncryptionPart( soapConstants.getBodyQName().getLocalPart(),
                     soapConstants.getEnvelopeURI(), "Content" );
             wsEncryptionParts.add( wsEncryptionPart );
 
             WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
             wsSecTimeStamp.setTimeToLive( 0 );
             /*
              * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
              * decide how long the message validity period is.
              */
             wsSecTimeStamp.prepare( document );
             wsSecTimeStamp.prependToHeader( wsSecHeader );
 
             wsSecSignature.addReferencesToSign( wsEncryptionParts, wsSecHeader );
 
             wsSecSignature.prependToHeader( wsSecHeader );
 
             wsSecSignature.prependBSTElementToHeader( wsSecHeader );
 
             wsSecSignature.computeSignature();
         }
         catch (WSSecurityException e) {
             throw new RuntimeException( "WSS4J error: " + e.getMessage(), e );
         }
 
         LOG.debug( "document: " + DomTestUtils.domToString( document ) );
 
         // Setup Mocks
         expect( mockWSSecurityConfiguration.getMaximumAge() ).andStubReturn( new Duration( Long.MAX_VALUE ) );
         expect( mockWSSecurityConfiguration.isCertificateChainTrusted( certificateChain ) ).andReturn( true );
 
         replay( mockObjects );
 
         // Test
         MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
         SOAPMessage message = messageFactory.createMessage();
         DOMSource domSource = new DOMSource( document );
         SOAPPart soapPart = message.getSOAPPart();
         soapPart.setContent( domSource );
 
         message.getSOAPHeader();
 
         SOAPMessageContext soapMessageContext = new TestSOAPMessageContext( message, false );
 
         // operate & verify
         try {
             testedInstance.handleMessage( soapMessageContext );
             fail();
         }
         catch (RuntimeException e) {
             LOG.debug( "expected exception: ", e );
             assertEquals( "Timestamp not signed", e.getMessage() );
             // Expected
             verify( mockObjects );
         }
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void wss4j()
             throws Exception {
 
         // Setup Data
         KeyPair keyPair = PkiTestUtils.generateKeyPair();
         CertificateChain certificateChain = new CertificateChain( PkiTestUtils.generateSelfSignedCertificate( keyPair, "CN=Test" ) );
 
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         documentBuilderFactory.setNamespaceAware( true );
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document document = documentBuilder.parse( WSSecurityServerHandlerTest.class.getResourceAsStream( "/test-soap-message.xml" ) );
 
         // use WSSecurityClientHandler to sign message
         LOG.debug( "adding WS-Security SOAP header" );
         WSSecSignature wsSecSignature = new WSSecSignature();
         wsSecSignature.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
         Crypto clientCrypto = new ClientCrypto( certificateChain, keyPair.getPrivate() );
         WSSecHeader wsSecHeader = new WSSecHeader();
         wsSecHeader.insertSecurityHeader( document );
         try {
             wsSecSignature.prepare( document, clientCrypto, wsSecHeader );
 
             String testId = "test-id";
             Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
             WSEncryptionPart wsBodyEncryptionPart = new WSEncryptionPart( testId );
             wsEncryptionParts.add( wsBodyEncryptionPart );
 
             WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
             wsSecTimeStamp.setTimeToLive( 0 );
             /*
              * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
              * decide how long the message validity period is.
              */
             wsSecTimeStamp.prepare( document );
             wsSecTimeStamp.prependToHeader( wsSecHeader );
             wsEncryptionParts.add( new WSEncryptionPart( wsSecTimeStamp.getId() ) );
 
             wsSecSignature.addReferencesToSign( wsEncryptionParts, wsSecHeader );
 
             wsSecSignature.prependToHeader( wsSecHeader );
 
             wsSecSignature.prependBSTElementToHeader( wsSecHeader );
 
             wsSecSignature.computeSignature();
 
             LOG.debug( "document: " + DomTestUtils.domToString( document ) );
 
             // Test
             MessageFactory messageFactory = MessageFactory.newInstance( SOAPConstants.SOAP_1_1_PROTOCOL );
             SOAPMessage message = messageFactory.createMessage();
             DOMSource domSource = new DOMSource( document );
             SOAPPart soapPart = message.getSOAPPart();
             soapPart.setContent( domSource );
             soapPart = message.getSOAPPart();
             WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
             Crypto serverCrypto = new ServerCrypto();
             Vector<WSSecurityEngineResult> wsSecurityEngineResults;
             wsSecurityEngineResults = securityEngine.processSecurityHeader( soapPart, null, null, serverCrypto );
 
             assertNotNull( wsSecurityEngineResults );
             for (WSSecurityEngineResult result : wsSecurityEngineResults) {
                 Set<String> signedElements = (Set<String>) result.get( WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS );
                 if (null != signedElements) {
                     LOG.debug( "signed elements: " + signedElements );
                     assertTrue( signedElements.contains( testId ) );
                     assertTrue( signedElements.contains( wsSecTimeStamp.getId() ) );
                 }
             }
         }
         catch (WSSecurityException e) {
             throw new RuntimeException( "WSS4J error: " + e.getMessage(), e );
         }
     }
 
     private static class TestWSSecurityConfiguration extends AbstractWSSecurityConfiguration {
 
         public boolean          trusted;
         public CertificateChain certificateChain;
         public PrivateKey       privateKey;
 
         public boolean isCertificateChainTrusted(final CertificateChain aCertificateChain) {
 
             return trusted;
         }
 
         public CertificateChain getIdentityCertificateChain() {
 
             return certificateChain;
         }
 
         public PrivateKey getPrivateKey() {
 
             return privateKey;
         }
     }
 }
