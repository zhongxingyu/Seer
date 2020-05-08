 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.protocol.saml2;
 
 import java.security.KeyPair;
 import java.util.Collections;
 import java.util.Locale;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import net.link.safeonline.sdk.api.auth.LoginMode;
 import net.link.safeonline.sdk.api.auth.StartPage;
 import net.link.util.common.DomUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.Nullable;
 import org.opensaml.common.SAMLObject;
 import org.opensaml.common.binding.BasicSAMLMessageContext;
 import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
 import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
 import org.opensaml.saml2.core.RequestAbstractType;
 import org.opensaml.saml2.core.StatusResponseType;
 import org.opensaml.saml2.metadata.AssertionConsumerService;
 import org.opensaml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
 import org.opensaml.ws.message.MessageContext;
 import org.opensaml.ws.message.decoder.MessageDecodingException;
 import org.opensaml.ws.message.encoder.MessageEncodingException;
 import org.opensaml.ws.security.SecurityPolicy;
 import org.opensaml.ws.security.SecurityPolicyException;
 import org.opensaml.ws.security.SecurityPolicyResolver;
 import org.opensaml.ws.security.provider.BasicSecurityPolicy;
 import org.opensaml.ws.security.provider.MandatoryIssuerRule;
 import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
 import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
 import org.opensaml.xml.security.SecurityException;
 import org.opensaml.xml.security.x509.BasicX509Credential;
 
 
 /**
  * Utility class for SAML2 authentication responses.
  *
  * @author wvdhaute
  */
 public abstract class RedirectBindingUtil {
 
     private static final Log LOG = LogFactory.getLog( RedirectBindingUtil.class );
 
     /**
      * Signs and sends a SAML2 authentication or logout Request using SAML2 HTTP Redirect Binding.
      *
      * @param samlRequest    SAML v2.0 Request to sign and send
      * @param signingKeyPair keypair to sign with
      * @param relayState     optional RelayState
      * @param consumerUrl    consumer URL to send the Response to
      * @param response       HTTP Servlet Response
      */
     public static void sendRequest(RequestAbstractType samlRequest, KeyPair signingKeyPair, @Nullable String relayState, String consumerUrl,
                                   HttpServletResponse response, Locale language, String themeName, LoginMode loginMode,
                                   StartPage startPage) {
 
         LOG.debug( "sendRequest[HTTP Redirect] (RelayState: " + relayState + ", To: " + consumerUrl + "):\n" + DomUtils.domToString(
                 LinkIDSaml2Utils.marshall( samlRequest ), true ) );
 
         BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
 
         BasicX509Credential signingCredential = new BasicX509Credential();
         signingCredential.setPrivateKey( signingKeyPair.getPrivate() );
         signingCredential.setPublicKey( signingKeyPair.getPublic() );
         messageContext.setOutboundSAMLMessageSigningCredential( signingCredential );
 
         AssertionConsumerService consumerService = new AssertionConsumerServiceBuilder().buildObject();
         consumerService.setLocation( consumerUrl );
         messageContext.setPeerEntityEndpoint( consumerService );
 
         messageContext.setOutboundMessageTransport(
                 new LinkIDHttpServletResponseAdapter( response, consumerUrl.startsWith( "https" ), language, themeName, loginMode,
                         startPage ) );
         messageContext.setOutboundSAMLMessage( samlRequest );
         messageContext.setRelayState( relayState );
 
         encodeMessageContext( messageContext );
     }
 
     /**
      * Sends out a SAML response message to the specified consumer URL using SAML2 HTTP Redirect Binding.
      *
      * @param samlResponse   SAML v2.0 Response to send
      * @param signingKeyPair keypair to sign with
      * @param relayState     optional RelayState
      * @param consumerUrl    consumer URL to send the Response to
      * @param response       HTTP Servlet Response
      */
     public static void sendResponse(StatusResponseType samlResponse, KeyPair signingKeyPair, String relayState, String consumerUrl,
                                     HttpServletResponse response) {
 
         LOG.debug( "sendResponse[HTTP Redirect] (RelayState: " + relayState + ", To: " + consumerUrl + "):\n" + DomUtils.domToString(
                 LinkIDSaml2Utils.marshall( samlResponse ), true ) );
 
         BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
 
         BasicX509Credential signingCredential = new BasicX509Credential();
         signingCredential.setPrivateKey( signingKeyPair.getPrivate() );
         signingCredential.setPublicKey( signingKeyPair.getPublic() );
         messageContext.setOutboundSAMLMessageSigningCredential( signingCredential );
 
         AssertionConsumerService consumerService = new AssertionConsumerServiceBuilder().buildObject();
         consumerService.setLocation( consumerUrl );
         messageContext.setPeerEntityEndpoint( consumerService );
 
         messageContext.setOutboundMessageTransport( new HttpServletResponseAdapter( response, consumerUrl.startsWith( "https" ) ) );
         messageContext.setOutboundSAMLMessage( samlResponse );
         messageContext.setRelayState( relayState );
 
         encodeMessageContext( messageContext );
     }
 
     /**
      * @param request HTTP Servlet Request
      *
      * @return The RelayState that is in the given {@link HttpServletRequest} which is parsed as using the SAML HTTP Redirect Binding
      *         protocol.
      */
     public static String getRelayState(HttpServletRequest request) {
 
         BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
         messageContext.setInboundMessageTransport( new HttpServletRequestAdapter( request ) );
 
         decodeMessageContext( messageContext );
 
         return messageContext.getRelayState();
     }
 
     /**
      * @param request HTTP Servlet Request
      *
      * @return The {@link SAMLObject} that is in the given {@link HttpServletRequest} which is parsed as using the SAML HTTP Redirect
      *         Binding protocol.<br> <code>null</code>: There is no SAML request or response.
      */
     public static SAMLObject getSAMLObject(HttpServletRequest request) {
 
         // Check whether there is a SAML request or SAML response in the HTTP request.
         if (request.getParameter( "SAMLRequest" ) == null && request.getParameter( "SAMLResponse" ) == null)
             return null;
 
         BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
         messageContext.setInboundMessageTransport( new HttpServletRequestAdapter( request ) );
         messageContext.setSecurityPolicyResolver( new SecurityPolicyResolver() {
 
             public Iterable<SecurityPolicy> resolve(MessageContext context)
                     throws SecurityException {
 
                 return Collections.singletonList( resolveSingle( context ) );
             }
 
             public SecurityPolicy resolveSingle(MessageContext context)
                     throws SecurityException {
 
                 SecurityPolicy securityPolicy = new BasicSecurityPolicy();
                 securityPolicy.getPolicyRules().add( new MandatoryIssuerRule() );
 
                 return securityPolicy;
             }
         } );
 
         decodeMessageContext( messageContext );
 
         return messageContext.getInboundSAMLMessage();
     }
 
     private static void encodeMessageContext(BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext) {
 
         try {
             new HTTPRedirectDeflateEncoder().encode( messageContext );
         }
         catch (MessageEncodingException e) {
             throw new RuntimeException( "SAML message encoding error", e );
         }
     }
 
     private static void decodeMessageContext(MessageContext messageContext) {
 
         try {
             new HTTPRedirectDeflateDecoder().decode( messageContext );
         }
         catch (MessageDecodingException e) {
             throw new RuntimeException( "SAML message decoding error", e );
         }
         catch (SecurityPolicyException e) {
             throw new RuntimeException( "security policy error", e );
         }
         catch (SecurityException e) {
             throw new RuntimeException( "security error", e );
         }
     }
 }
