 /*
  * SafeOnline project.
  *
  * Copyright 2006-2009 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 package net.link.safeonline.sdk.ws.auth;
 
 import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
 import java.io.Serializable;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.interfaces.DSAPublicKey;
 import java.security.interfaces.RSAPublicKey;
 import java.util.*;
 import javax.xml.datatype.*;
 import net.lin_k.safe_online.auth.*;
 import net.link.safeonline.sdk.api.attribute.*;
 import net.link.safeonline.sdk.api.ws.WebServiceConstants;
 import net.link.safeonline.sdk.api.ws.auth.Confirmation;
 import oasis.names.tc.saml._2_0.assertion.AttributeType;
 import oasis.names.tc.saml._2_0.assertion.NameIDType;
 import oasis.names.tc.saml._2_0.protocol.RequestAbstractType;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.opensaml.common.SAMLVersion;
 import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
 import org.w3._2000._09.xmldsig.*;
 import org.w3._2000._09.xmldsig.ObjectFactory;
 
 
 /**
  * <h2>{@link AuthenticationUtil}</h2>
  * <p/>
  * <p> Utility class for constructing the linkID Authentication WS messages. </p>
  * <p/>
  * <p> <i>Jan 15, 2009</i> </p>
  *
  * @author wvdhaute
  */
 public class AuthenticationUtil {
 
     private static final Log LOG = LogFactory.getLog( AuthenticationUtil.class );
 
     public static WSAuthenticationRequestType getAuthenticationRequest(String applicationName, String deviceName, String language,
                                                                        Map<String, String> deviceCredentials, PublicKey publicKey) {
 
         WSAuthenticationRequestType authenticationRequest = new WSAuthenticationRequestType();
         setRequest( authenticationRequest );
 
         // Issuer
         NameIDType issuerName = new NameIDType();
         issuerName.setValue( applicationName );
         authenticationRequest.setIssuer( issuerName );
 
         authenticationRequest.setApplicationName( applicationName );
         authenticationRequest.setDeviceName( deviceName );
         authenticationRequest.setLanguage( language );
 
         if (null != deviceCredentials) {
             DeviceCredentialsType deviceCredentialsType = new DeviceCredentialsType();
             for (Map.Entry<String, String> entry : deviceCredentials.entrySet()) {
                 NameValuePairType nameValuePair = new NameValuePairType();
                 nameValuePair.setName( entry.getKey() );
                 nameValuePair.setValue( entry.getValue() );
                 deviceCredentialsType.getNameValuePair().add( nameValuePair );
             }
             authenticationRequest.setDeviceCredentials( deviceCredentialsType );
         }
 
         if (null != publicKey) {
             KeyInfoType keyInfo = getKeyInfo( publicKey );
             authenticationRequest.setKeyInfo( keyInfo );
         }
 
         return authenticationRequest;
     }
 
     public static WSAuthenticationGlobalUsageAgreementRequestType getGlobalUsageAgreementRequest() {
 
         WSAuthenticationGlobalUsageAgreementRequestType request = new WSAuthenticationGlobalUsageAgreementRequestType();
         setRequest( request );
         return request;
     }
 
     public static WSAuthenticationGlobalUsageAgreementConfirmationType getGlobalUAConfirmationRequest(Confirmation confirmation) {
 
         WSAuthenticationGlobalUsageAgreementConfirmationType request = new WSAuthenticationGlobalUsageAgreementConfirmationType();
         setRequest( request );
         request.setConfirmation( confirmation.getValue() );
         return request;
     }
 
     public static WSAuthenticationUsageAgreementRequestType getUsageAgreementRequest() {
 
         WSAuthenticationUsageAgreementRequestType request = new WSAuthenticationUsageAgreementRequestType();
         setRequest( request );
         return request;
     }
 
     public static WSAuthenticationUsageAgreementConfirmationType getUAConfirmationRequest(Confirmation confirmation) {
 
         WSAuthenticationUsageAgreementConfirmationType request = new WSAuthenticationUsageAgreementConfirmationType();
         setRequest( request );
         request.setConfirmation( confirmation.getValue() );
         return request;
     }
 
     public static WSAuthenticationIdentityRequestType getIdentityRequest() {
 
         WSAuthenticationIdentityRequestType request = new WSAuthenticationIdentityRequestType();
         setRequest( request );
         return request;
     }
 
     public static WSAuthenticationIdentityConfirmationType getIdentityConfirmationRequest(List<AttributeIdentitySDK> attributes) {
 
         WSAuthenticationIdentityConfirmationType request = new WSAuthenticationIdentityConfirmationType();
         setRequest( request );
 
         if (null != attributes)
             for (AttributeIdentitySDK attribute : attributes) {
                 AttributeType attributeType = toSDK( attribute );
                 request.getAttribute().add( attributeType );
             }
 
         return request;
     }
 
     private static void setRequest(RequestAbstractType request) {
 
         SecureRandomIdentifierGenerator idGenerator;
         try {
             idGenerator = new SecureRandomIdentifierGenerator();
         }
         catch (NoSuchAlgorithmException e) {
             throw new InternalInconsistencyException( String.format( "secure random init error: %s", e.getMessage() ), e );
         }
         String id = idGenerator.generateIdentifier();
         XMLGregorianCalendar now = getCurrentXmlGregorianCalendar();
 
         request.setID( id );
         request.setVersion( SAMLVersion.VERSION_20.toString() );
         request.setIssueInstant( now );
     }
 
     private static XMLGregorianCalendar getCurrentXmlGregorianCalendar() {
 
         DatatypeFactory datatypeFactory;
         try {
             datatypeFactory = DatatypeFactory.newInstance();
         }
         catch (DatatypeConfigurationException e) {
             LOG.error( "datatype configuration exception", e );
             throw new InternalInconsistencyException( String.format( "datatype configuration exception: %s", e.getMessage() ), e );
         }
 
         GregorianCalendar gregorianCalendar = new GregorianCalendar();
         Date now = new Date();
         gregorianCalendar.setTime( now );
         return datatypeFactory.newXMLGregorianCalendar( gregorianCalendar );
     }
 
     /**
      * Converts public key to XML DSig KeyInfoType
      *
      * @param publicKey public key
      *
      * @return converted KeyInfo
      */
     private static KeyInfoType getKeyInfo(PublicKey publicKey) {
 
         KeyInfoType keyInfo = new KeyInfoType();
         KeyValueType keyValue = new KeyValueType();
         keyInfo.getContent().add( keyValue );
         ObjectFactory dsigObjectFactory = new ObjectFactory();
 
         if (publicKey instanceof RSAPublicKey) {
             RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
             RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
             rsaKeyValue.setModulus( rsaPublicKey.getModulus().toByteArray() );
             rsaKeyValue.setExponent( rsaPublicKey.getPublicExponent().toByteArray() );
             keyValue.getContent().add( dsigObjectFactory.createRSAKeyValue( rsaKeyValue ) );
         } else if (publicKey instanceof DSAPublicKey) {
             DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
             DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
             dsaKeyValue.setY( dsaPublicKey.getY().toByteArray() );
             dsaKeyValue.setG( dsaPublicKey.getParams().getG().toByteArray() );
             dsaKeyValue.setP( dsaPublicKey.getParams().getP().toByteArray() );
             dsaKeyValue.setQ( dsaPublicKey.getParams().getQ().toByteArray() );
             keyValue.getContent().add( dsaKeyValue );
         } else
             throw new IllegalArgumentException( "Only RSAPublicKey and DSAPublicKey are supported" );
 
         return keyInfo;
     }
 
     public static AttributeType toSDK(AttributeIdentitySDK attributeIdentitySDK) {
 
         AttributeType attributeType = new AttributeType();
         attributeType.setNameFormat( WebServiceConstants.SAML_ATTRIB_NAME_FORMAT_BASIC );
        attributeType.setName( attributeIdentitySDK.getName() );
         attributeType.setFriendlyName( attributeIdentitySDK.getFriendlyName() );
 
         if (attributeIdentitySDK.getAttributeType().isCompound()) {
             for (AttributeSDK<?> memberSDK : ((Compound) attributeIdentitySDK.getValue()).getMembers()) {
                 AttributeIdentitySDK member = (AttributeIdentitySDK) memberSDK;
                 attributeType.getAttributeValue().add( toSDK( member ) );
             }
         } else {
             attributeType.getAttributeValue().add( attributeIdentitySDK.getValue() );
         }
         attributeType.getOtherAttributes()
                      .put( WebServiceConstants.DATATYPE_ATTRIBUTE, attributeIdentitySDK.getAttributeType().getType().getValue() );
         attributeType.getOtherAttributes()
                      .put( WebServiceConstants.MULTIVALUED_ATTRIBUTE,
                              Boolean.toString( attributeIdentitySDK.getAttributeType().isMultivalued() ) );
         attributeType.getOtherAttributes()
                      .put( WebServiceConstants.DATAMINING_ATTRIBUTE, Boolean.toString( attributeIdentitySDK.isAnonymous() ) );
         attributeType.getOtherAttributes()
                      .put( WebServiceConstants.OPTIONAL_ATTRIBUTE, Boolean.toString( attributeIdentitySDK.isOptional() ) );
         attributeType.getOtherAttributes()
                      .put( WebServiceConstants.CONFIRMATION_REQUIRED_ATTRIBUTE,
                              Boolean.toString( attributeIdentitySDK.isConfirmationNeeded() ) );
         attributeType.getOtherAttributes()
                      .put( WebServiceConstants.CONFIRMED_ATTRIBUTE, Boolean.toString( attributeIdentitySDK.isConfirmed() ) );
         attributeType.getOtherAttributes().put( WebServiceConstants.ATTRIBUTE_ID, attributeIdentitySDK.getId() );
         attributeType.getOtherAttributes().put( WebServiceConstants.GROUP_NAME_ATTRIBUTE, attributeIdentitySDK.getGroupName() );
         return attributeType;
     }
 
     public static AttributeIdentitySDK newAttributeIdentitySDK(AttributeType attributeType) {
 
         String id = attributeType.getOtherAttributes().get( WebServiceConstants.ATTRIBUTE_ID );
 
         net.link.safeonline.sdk.api.attribute.AttributeType sdkAttributeType = new net.link.safeonline.sdk.api.attribute.AttributeType(
                 attributeType.getName(),
                 DataType.getDataType( attributeType.getOtherAttributes().get( WebServiceConstants.DATATYPE_ATTRIBUTE ) ),
                 Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.MULTIVALUED_ATTRIBUTE ) ) );
 
         String friendlyName = attributeType.getFriendlyName();
         String groupName = attributeType.getOtherAttributes().get( WebServiceConstants.GROUP_NAME_ATTRIBUTE );
         boolean anonymous = Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.DATAMINING_ATTRIBUTE ) );
         boolean optional = Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.OPTIONAL_ATTRIBUTE ) );
         boolean confirmationNeeded = Boolean.valueOf(
                 attributeType.getOtherAttributes().get( WebServiceConstants.CONFIRMATION_REQUIRED_ATTRIBUTE ) );
         boolean confirmed = Boolean.valueOf( attributeType.getOtherAttributes().get( WebServiceConstants.CONFIRMED_ATTRIBUTE ) );
 
         Serializable value = null;
         if (null != attributeType.getAttributeValue()) {
             if (sdkAttributeType.isCompound()) {
                 List<AttributeIdentitySDK> members = new LinkedList<AttributeIdentitySDK>();
                 for (Object memberAttribute : attributeType.getAttributeValue()) {
                     oasis.names.tc.saml._2_0.assertion.AttributeType memberAttributeType = (oasis.names.tc.saml._2_0.assertion.AttributeType) memberAttribute;
                     members.add( newAttributeIdentitySDK( memberAttributeType ) );
                 }
                 value = new Compound( members );
             } else {
                 value = (Serializable) attributeType.getAttributeValue().get( 0 );
             }
         }
 
         return new AttributeIdentitySDK( id, sdkAttributeType, friendlyName, groupName, anonymous, optional, confirmationNeeded, confirmed,
                 value );
     }
 }
