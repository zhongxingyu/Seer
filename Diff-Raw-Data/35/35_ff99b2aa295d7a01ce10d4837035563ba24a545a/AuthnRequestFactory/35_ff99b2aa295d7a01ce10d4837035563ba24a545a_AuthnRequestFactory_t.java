 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.safeonline.sdk.auth.protocol.saml2;
 
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.Set;
 import javax.xml.namespace.QName;
 import net.link.safeonline.sdk.auth.protocol.saml2.sessiontracking.*;
import net.link.util.saml.Saml2Utils;
 import org.joda.time.DateTime;
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.common.SAMLVersion;
 import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
 import org.opensaml.common.xml.SAMLConstants;
 import org.opensaml.saml2.common.Extensions;
 import org.opensaml.saml2.core.*;
 import org.opensaml.xml.Configuration;
 import org.opensaml.xml.ConfigurationException;
 
 
 /**
  * Factory class for SAML2 authentication requests.
  * <p/>
  * <p>
  * We're using the OpenSAML2 Java library for construction of the XML SAML documents.
  * </p>
  *
  * @author fcorneli
  */
 public class AuthnRequestFactory {
 
     private AuthnRequestFactory() {
 
         // empty
     }
 
     static {
         /*
          * Next is because Sun loves to endorse crippled versions of Xerces.
          */
        //noinspection HardcodedFileSeparator
         System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                 "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
         try {
             DefaultBootstrap.bootstrap();
             Configuration.registerObjectProvider( SessionInfo.DEFAULT_ELEMENT_NAME, new SessionInfoBuilder(), new SessionInfoMarshaller(),
                     new SessionInfoUnmarshaller() );
         }
         catch (ConfigurationException e) {
            throw new RuntimeException( "could not bootstrap the OpenSAML2 library", e );
         }
     }
 
     /**
      * Creates a SAML2 authentication request. For the moment we allow the Service Provider to pass on the Assertion Consumer Service URL
      * itself. Later on we could use the SAML Metadata service or a persistent server-side application field to locate this service.
      *
      * @param audiences                   the optional list of audiences is the optional list of application pools that can be specified
      *                                    for
      *                                    use in Single Sign On
      * @param assertionConsumerServiceURL the optional location of the assertion consumer service. This location can be used by the IdP to
      *                                    send back the SAML
      *                                    response message.
      * @param destinationURL              the optional location of the destination IdP.
      * @param devices                     the optional list of allowed authentication devices.
      * @param forceAuthentication         whether authentication should be forced and SSO ignore
      * @param sessionTrackingId           optional session info, marks application wishes to track this session
      */
     public static AuthnRequest createAuthnRequest(String issuerName, List<String> audiences, String applicationFriendlyName,
                                                   String assertionConsumerServiceURL, String destinationURL, Set<String> devices,
                                                   boolean forceAuthentication, String sessionTrackingId) {
 
         if (null == issuerName)
             throw new IllegalArgumentException( "application name should not be null" );
 
        AuthnRequest request = Saml2Utils.buildXMLObject( AuthnRequest.DEFAULT_ELEMENT_NAME );
 
         request.setForceAuthn( forceAuthentication );
         SecureRandomIdentifierGenerator idGenerator;
         try {
             idGenerator = new SecureRandomIdentifierGenerator();
         }
         catch (NoSuchAlgorithmException e) {
             throw new RuntimeException( "secure random init error: " + e.getMessage(), e );
         }
         String id = idGenerator.generateIdentifier();
         request.setID( id );
         request.setVersion( SAMLVersion.VERSION_20 );
         request.setIssueInstant( new DateTime() );
        Issuer issuer = Saml2Utils.buildXMLObject( Issuer.DEFAULT_ELEMENT_NAME );
         issuer.setValue( issuerName );
         request.setIssuer( issuer );
 
         if (null != assertionConsumerServiceURL) {
             request.setAssertionConsumerServiceURL( assertionConsumerServiceURL );
             request.setProtocolBinding( SAMLConstants.SAML2_POST_BINDING_URI );
         }
 
         if (null != destinationURL)
             request.setDestination( destinationURL );
 
         if (null != applicationFriendlyName)
             request.setProviderName( applicationFriendlyName );
 
        NameIDPolicy nameIdPolicy = Saml2Utils.buildXMLObject( NameIDPolicy.DEFAULT_ELEMENT_NAME );
         nameIdPolicy.setAllowCreate( true );
         request.setNameIDPolicy( nameIdPolicy );
 
         if (null != devices) {
            RequestedAuthnContext requestedAuthnContext = Saml2Utils.buildXMLObject( RequestedAuthnContext.DEFAULT_ELEMENT_NAME );
             List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
             for (String device : devices) {
                AuthnContextClassRef authnContextClassRef = Saml2Utils.buildXMLObject( AuthnContextClassRef.DEFAULT_ELEMENT_NAME );
                 authnContextClassRef.setAuthnContextClassRef( device );
                 authnContextClassRefs.add( authnContextClassRef );
             }
             request.setRequestedAuthnContext( requestedAuthnContext );
         }
 
         if (null != audiences) {
            Conditions conditions = Saml2Utils.buildXMLObject( Conditions.DEFAULT_ELEMENT_NAME );
             List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
            AudienceRestriction audienceRestriction = Saml2Utils.buildXMLObject( AudienceRestriction.DEFAULT_ELEMENT_NAME );
             audienceRestrictions.add( audienceRestriction );
             List<Audience> audienceList = audienceRestriction.getAudiences();
             for (String audienceName : audiences) {
                Audience audience = Saml2Utils.buildXMLObject( Audience.DEFAULT_ELEMENT_NAME );
                 audienceList.add( audience );
                 audience.setAudienceURI( audienceName );
             }
             request.setConditions( conditions );
         }
 
         // add session info
         if (null != sessionTrackingId) {
             QName extensionsQName = new QName( SAMLConstants.SAML20P_NS, Extensions.LOCAL_NAME, SAMLConstants.SAML20P_PREFIX );
            Extensions extensions = Saml2Utils.buildXMLObject( extensionsQName );
            SessionInfo sessionInfo = Saml2Utils.buildXMLObject( SessionInfo.DEFAULT_ELEMENT_NAME );
             sessionInfo.setSession( sessionTrackingId );
             request.setExtensions( extensions );
             request.getExtensions().getUnknownXMLObjects().add( sessionInfo );
         }
 
         return request;
     }
 }
