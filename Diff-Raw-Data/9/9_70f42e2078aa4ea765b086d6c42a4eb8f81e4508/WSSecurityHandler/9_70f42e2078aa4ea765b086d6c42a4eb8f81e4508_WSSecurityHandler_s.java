 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.util.ws.security;
 
 import com.google.common.collect.ImmutableSet;
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import javax.annotation.PostConstruct;
 import javax.naming.*;
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPException;
 import javax.xml.soap.SOAPPart;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.handler.Handler;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.handler.MessageContext.Scope;
 import javax.xml.ws.handler.soap.SOAPHandler;
 import javax.xml.ws.handler.soap.SOAPMessageContext;
 import net.link.util.common.CertificateChain;
 import net.link.util.common.DomUtils;
 import net.link.util.j2ee.EJBUtils;
 import net.link.util.pkix.ClientCrypto;
 import net.link.util.pkix.ServerCrypto;
 import org.apache.ws.security.*;
 import org.apache.ws.security.message.*;
 import org.apache.ws.security.message.token.Timestamp;
 import org.apache.ws.security.util.WSSecurityUtil;
 import org.joda.time.Duration;
 
 
 /**
  * JAX-WS SOAP Handler that provides WS-Security server-side verification.
  *
  * @author fcorneli
  */
 public class WSSecurityHandler implements SOAPHandler<SOAPMessageContext> {
 
     static final Logger logger = Logger.get( WSSecurityHandler.class );
 
     public static final String CERTIFICATE_CHAIN_PROPERTY  = WSSecurityHandler.class + ".x509";
     public static final String TO_BE_SIGNED_IDS_SET        = WSSecurityHandler.class + ".toBeSignedIDs";
     public static final String SIGNED_ELEMENTS_CONTEXT_KEY = WSSecurityHandler.class + ".signed.elements";
 
     private final WSSecurityConfiguration configuration;
 
     public WSSecurityHandler() {
 
         try {
             Context ctx = new InitialContext();
             try {
                 Context env = (Context) ctx.lookup( "java:comp/env" );
                 String configurationServiceJndiName = (String) env.lookup( "wsSecurityConfigurationServiceJndiName" );
                 configuration = EJBUtils.getEJB( configurationServiceJndiName, WSSecurityConfiguration.class );
             }
             finally {
                 try {
                     ctx.close();
                 }
                 catch (NamingException e) {
                     logger.err( e, "While closing: %s", ctx );
                 }
             }
         }
         catch (NamingException e) {
             throw new RuntimeException( "'wsSecurityConfigurationServiceJndiName' not specified", e );
         }
     }
 
     public WSSecurityHandler(final WSSecurityConfiguration configuration) {
 
         this.configuration = configuration;
     }
 
     @PostConstruct
     public void postConstructCallback() {
 
         System.setProperty( "com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", Boolean.toString( true ) );
     }
 
     @Override
     public Set<QName> getHeaders() {
 
         return ImmutableSet.of(
                 new QName( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security" ) );
     }
 
     @Override
     public void close(@SuppressWarnings("unused") MessageContext messageContext) {
 
     }
 
     @Override
     public boolean handleFault(@SuppressWarnings("unused") SOAPMessageContext soapMessageContext) {
 
         return true;
     }
 
     @Override
     public boolean handleMessage(SOAPMessageContext soapMessageContext) {
 
         SOAPPart soapPart = soapMessageContext.getMessage().getSOAPPart();
         boolean isOutbound = (Boolean) soapMessageContext.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY );
 
         if (isOutbound)
             return handleOutboundDocument( soapPart, soapMessageContext );
         else
             return handleInboundDocument( soapPart, soapMessageContext );
     }
 
     /**
      * Handles the outbound SOAP message. Adds the WS Security Header containing a signed timestamp, and signed SOAP body.
      */
     private boolean handleOutboundDocument(SOAPPart document, SOAPMessageContext soapMessageContext) {
 
         if (!configuration.isOutboundSignatureNeeded()) {
             logger.dbg( "Out: Not adding WS-Security SOAP header" );
             return true;
         }
 
         logger.dbg( "Out: Adding WS-Security SOAP header" );
         try {
             CertificateChain certificateChain = configuration.getIdentityCertificateChain();
 
             WSSecHeader wsSecHeader = new WSSecHeader();
             wsSecHeader.insertSecurityHeader( document );
 
             WSSecSignature wsSecSignature = new WSSecSignature();
             wsSecSignature.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
             wsSecSignature.setUseSingleCertificate( !certificateChain.hasRootCertificate() );
             wsSecSignature.prepare( document, new ClientCrypto( certificateChain, configuration.getPrivateKey() ), wsSecHeader );
 
             Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
 
             SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants( document.getDocumentElement() );
             wsEncryptionParts.add(
                     new WSEncryptionPart( soapConstants.getBodyQName().getLocalPart(), soapConstants.getEnvelopeURI(), "Content" ) );
 
             WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
             wsSecTimeStamp.setTimeToLive( 0 );
             wsSecTimeStamp.prepare( document );
             wsSecTimeStamp.prependToHeader( wsSecHeader );
             wsEncryptionParts.add( new WSEncryptionPart( wsSecTimeStamp.getId() ) );
 
             @SuppressWarnings( { "unchecked" })
             Set<String> toBeSignedIDs = (Set<String>) soapMessageContext.get( TO_BE_SIGNED_IDS_SET );
             if (null != toBeSignedIDs)
                 for (String toBeSignedID : toBeSignedIDs)
                     wsEncryptionParts.add( new WSEncryptionPart( toBeSignedID ) );
 
             wsSecSignature.addReferencesToSign( wsEncryptionParts, wsSecHeader );
             wsSecSignature.prependToHeader( wsSecHeader );
             wsSecSignature.prependBSTElementToHeader( wsSecHeader );
             wsSecSignature.computeSignature();
         }
         catch (WSSecurityException e) {
             logger.err( "While handling outbound WS request", e );
             return false;
         }
 
         logger.dbg( "document: %s", DomUtils.domToString( document ) );
         return true;
     }
 
     private boolean handleInboundDocument(SOAPPart document, SOAPMessageContext soapMessageContext) {
 
         logger.dbg( "In: WS-Security header validation" );
         Vector<WSSecurityEngineResult> wsSecurityEngineResults;
         try {
             //noinspection unchecked
             wsSecurityEngineResults = WSSecurityEngine.getInstance().processSecurityHeader( document, null, null, new ServerCrypto() );
         }
         catch (WSSecurityException e) {
             throw SOAPUtils.createSOAPFaultException( "The signature or decryption was invalid", "FailedCheck", e );
         }
         logger.dbg( "results: %s", wsSecurityEngineResults );
         if (null == wsSecurityEngineResults) {
             if (!configuration.isInboundSignatureOptional())
                 throw SOAPUtils.createSOAPFaultException( "No WS-Security header was found but is required.", "InvalidSecurity" );
 
             logger.dbg( "Allowing inbound message without signature: it's set to optional" );
             return true;
         }
 
         Timestamp timestamp = null;
         Set<String> signedElements = null;
         for (WSSecurityEngineResult result : wsSecurityEngineResults) {
             @SuppressWarnings( { "unchecked" })
             Set<String> resultSignedElements = (Set<String>) result.get( WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS );
             if (null != resultSignedElements)
                 signedElements = resultSignedElements;
 
             X509Certificate[] certificateChain = (X509Certificate[]) result.get( WSSecurityEngineResult.TAG_X509_CERTIFICATES );
             X509Certificate certificate = (X509Certificate) result.get( WSSecurityEngineResult.TAG_X509_CERTIFICATE );
             if (null != certificateChain)
                 setCertificateChain( soapMessageContext, certificateChain );
             else if (null != certificate)
                 setCertificateChain( soapMessageContext, certificate );
 
             Timestamp resultTimestamp = (Timestamp) result.get( WSSecurityEngineResult.TAG_TIMESTAMP );
             if (null != resultTimestamp)
                 timestamp = resultTimestamp;
         }
 
         if (null == signedElements)
             throw SOAPUtils.createSOAPFaultException( "No signed elements found.", "FailedCheck" );
         logger.dbg( "signed elements: " + signedElements );
         soapMessageContext.put( SIGNED_ELEMENTS_CONTEXT_KEY, signedElements );
 
         // Check whether the SOAP Body has been signed.
         try {
             String bodyId = document.getEnvelope().getBody().getAttributeNS( WSConstants.WSU_NS, "Id" );
 
             if (null == bodyId || bodyId.isEmpty())
                 throw SOAPUtils.createSOAPFaultException( "SOAP Body should have a wsu:Id attribute", "FailedCheck" );
             if (!isElementSigned( soapMessageContext, bodyId ))
                 throw SOAPUtils.createSOAPFaultException( "SOAP Body was not signed", "FailedCheck" );
         }
         catch (SOAPException e) {
             throw SOAPUtils.createSOAPFaultException( "error retrieving SOAP Body", "FailedCheck", e );
         }
 
         /*
         * Validate certificate.
         */
         CertificateChain certificateChain = findCertificateChain( soapMessageContext );
         if (null == certificateChain)
             throw SOAPUtils.createSOAPFaultException( "missing X509Certificate chain in WS-Security header", "InvalidSecurity" );
         if (!configuration.isCertificateChainTrusted( certificateChain ))
             throw SOAPUtils.createSOAPFaultException( "can't trust X509Certificate chain in WS-Security header", "InvalidSecurity" );
 
         /*
          * Check timestamp.
          */
         if (null == timestamp)
             throw SOAPUtils.createSOAPFaultException( "missing Timestamp in WS-Security header", "InvalidSecurity" );
         String timestampId = timestamp.getID();
        if (!signedElements.contains( timestampId ))
             throw SOAPUtils.createSOAPFaultException( "Timestamp not signed", "FailedCheck" );
         Duration age = new Duration( timestamp.getCreated().getTimeInMillis(), System.currentTimeMillis() );
         Duration maximumAge = configuration.getMaximumAge();
         if (age.isLongerThan( maximumAge )) {
             logger.dbg( "Maximum age exceeded by %s (since %s)", maximumAge.minus( age ), timestamp.getCreated().getTime() );
             throw SOAPUtils.createSOAPFaultException( "Message too old", "FailedCheck" );
         }
 
         return true;
     }
 
     private static void setCertificateChain(SOAPMessageContext context, X509Certificate... certificate) {
 
         context.put( CERTIFICATE_CHAIN_PROPERTY, new CertificateChain( certificate ) );
         context.setScope( CERTIFICATE_CHAIN_PROPERTY, Scope.APPLICATION );
     }
 
     /**
      * @return the X509 certificate chain that was set previously by a WS-Security handler.
      */
     @SuppressWarnings( { "unchecked" })
     public static CertificateChain findCertificateChain(MessageContext context) {
 
         return (CertificateChain) context.get( CERTIFICATE_CHAIN_PROPERTY );
     }
 
     /**
      * Adds a new WS-Security client handler to the handler chain of the given JAX-WS port.
      */
     public static void install(BindingProvider port, WSSecurityConfiguration configuration) {
 
         @SuppressWarnings("unchecked")
         List<Handler> handlerChain = port.getBinding().getHandlerChain();
         handlerChain.add( new WSSecurityHandler( configuration ) );
         port.getBinding().setHandlerChain( handlerChain );
     }
 
     /**
      * Add an XML Id that needs to be included in the WS-Security signature digest.
      */
     public static void addSignedElement(SOAPMessageContext context, String id) {
 
         @SuppressWarnings("unchecked")
         Set<String> toBeSignedIds = (Set<String>) context.get( TO_BE_SIGNED_IDS_SET );
         if (null == toBeSignedIds)
             context.put( TO_BE_SIGNED_IDS_SET, toBeSignedIds = new HashSet<String>() );
 
         toBeSignedIds.add( id );
     }
 
     /**
      * Checks whether a WS-Security handler did verify that the element with given Id was signed correctly.
      */
     public static boolean isElementSigned(SOAPMessageContext context, String id) {
 
         @SuppressWarnings("unchecked")
         Set<String> signedElements = (Set<String>) context.get( SIGNED_ELEMENTS_CONTEXT_KEY );
         if (null == signedElements)
             return false;
 
         return signedElements.contains( id );
     }
 }
