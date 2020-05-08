 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SAML2Constants.java,v 1.4 2007-02-09 21:43:37 qcheng Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.saml2.common;
 
 import com.sun.identity.cot.COTConstants;
 
 /**
  * This interface defines constants common to all SAMLv2 elements.
  *
  * @supported.all.api
  */
 public interface SAML2Constants {
 
     /**
      * XML name space URI
      */
     public static final String NS_XML = "http://www.w3.org/2000/xmlns/";
     
     /**
      * String used to declare SAMLv2 assertion namespace prefix.
      */
     public String ASSERTION_PREFIX = "saml:";
     
     /**
      * String used to declare SAMLv2 assertion namespace.
      */
     public String ASSERTION_DECLARE_STR =
     " xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"";
     
     /**
      * SAMLv2 assertion namespace URI.
      */
     public String ASSERTION_NAMESPACE_URI =
     "urn:oasis:names:tc:SAML:2.0:assertion";
     
     /**
      * Default namespace attribute for <code>Action</code>.
      */
     public String ACTION_NAMESPACE_NEGATION =
     "urn:oasis:names:tc:SAML:1.0:action:rwedc-negation";
     
     /**
      * String used to declare SAMLv2 protocol namespace prefix.
      */
     public String PROTOCOL_PREFIX = "samlp:";
     
     /**
      * String used to declare SAMLv2 protocol namespace.
      */
     public String PROTOCOL_NAMESPACE = "urn:oasis:names:tc:SAML:2.0:protocol";
     
     /**
      * String used to declare SAMLv2 protocol namespace.
      */
     public String PROTOCOL_DECLARE_STR =
                 " xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"";
     
     /**
      * String used to represent HTTP Redirect Binding.
      */
     public static final String HTTP_REDIRECT =
                 "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
 
     /**
      * String used to represent SOAP Binding.
      */
     public static final String SOAP =
                 "urn:oasis:names:tc:SAML:2.0:bindings:SOAP";
 
     /**
      * String used to represent HTTP POST Binding.
      */
     public static final String HTTP_POST =
                 "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
 
     /**
      * String used to represent HTTP ARTIFACT Binding.
      */
     public static final String HTTP_ARTIFACT =
                 "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact";
 
     /**
      * String to represent Name Identifier Format name space
      */
     public static final String NAMEID_FORMAT_NAMESPACE=
                 "urn:oasis:names:tc:SAML:2.0:nameid-format:";
 
     /**
      * String to represent Persitent Name Identifier
      */
     public static final String PERSISTENT =
                 NAMEID_FORMAT_NAMESPACE + "persistent";
     
     /**
      * String to represent Unspecified Name Identifier
      */
     public static final String UNSPECIFIED =
                 NAMEID_FORMAT_NAMESPACE + "unspecified";
   
     /**
      * String to represent X509 Subejct Name Identifier
      */
     public static final String X509_SUBJECT_NAME =
         NAMEID_FORMAT_NAMESPACE + "X509SubjectName";
 
     /**
      * String to represent the authentication service url
      */
     public static final String AUTH_URL = "AuthUrl";
  
     /**
      * Strings represent primitive top-level StatusCode values 
      */
     public static final String SUCCESS =
         "urn:oasis:names:tc:SAML:2.0:status:Success";
 
     public static final String REQUESTER =
         "urn:oasis:names:tc:SAML:2.0:status:Requester";
 
     public static final String RESPONDER =
         "urn:oasis:names:tc:SAML:2.0:status:Responder";
     
     public static final String VERSION_MISMATCH =
         "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch";
 
     /**
      * Strings represent subject confirmation methods
      */
     public static final String SUBJECT_CONFIRMATION_METHOD_BEARER =
         "urn:oasis:names:tc:SAML:2.0:cm:bearer";
 
     /**
      * Length for SAMLv2 IDs.
      */
     public int ID_LENGTH = 20;
     
     /**
      * SAMLv2 Version String
      */
     public static String VERSION_2_0 = "2.0";
 
     /**
      * SAMLRequest query parameter name
      */
     public static String SAML_REQUEST = "SAMLRequest";
 
     /**
      * SAMLResponse query parameter name
      */
     public static String SAML_RESPONSE = "SAMLResponse";
     
     
     /**
      * Maximum value of unsigned integer/short type.
      */
     public static final int MAX_INT_VALUE=65535;
     
     /**
      * Start Tag for XML String
      */
     public static final String START_TAG="<";
     /**
      * End Tag for XML String
      */
     public static final String END_TAG =">";
     
     /**
      * Constant for space
      */
     public static final String SPACE=" ";
     /**
      * Constant for equal
      */
     public static final String EQUAL= "=";
     
     /**
      * Constant for quote
      */
     public static final String QUOTE = "\"";
     
     /**
      * Constant for newline
      */
     public static final String NEWLINE= "\n";
     
     /**
      * Constant for xml name space
      */
     public static final String NAMESPACE_PREFIX="xmlns";
     
     /**
      * Constant for SAML2 end tag
      */
     public static final String SAML2_END_TAG="</samlp:";
     
     /**
      * Constant for AuthnRequest
      */
     public static final String AUTHNREQUEST="AuthnRequest";
 
     /**
      * Constant for LogoutRequest
      */
     public static final String LOGOUT_REQUEST="LogoutRequest";
 
     /**
      * Constant for LogoutResponse
      */
     public static final String LOGOUT_RESPONSE="LogoutResponse";
 
     /**
      * Constant for SessionIndex
      */
     public static final String SESSION_INDEX="SessionIndex";
 
     /**
      * Constant for BaseID
      */
     public static final String BASEID="BaseID";
 
     /**
      * Constant for NameID
      */
     public static final String NAMEID="NameID";
 
     /**
      * Constant for EncryptedID
      */
     public static final String ENCRYPTEDID="EncryptedID";
     
     /**
      * Constant for Reason
      */
     public static final String REASON="Reason";
 
     /**
      * Constant for NotOnOrAfter
      */
     public static final String NOTONORAFTER="NotOnOrAfter";
 
     /**
      * Constant for InResponseTo
      */
     public static final String INRESPONSETO="InResponseTo";
    
     /**
      * Constant for ID
      */
     public static final String ID="ID";
     
     
     /**
      * Constant for Version
      */
     public static final String VERSION="Version";
     
     
     /**
      * Constant for IssueInstant
      */
     public static final String ISSUE_INSTANT="IssueInstant";
     
     /**
      * Constant for Destination
      */
     public static final String DESTINATION="Destination";
     
     /**
      * Constant for Value
      */
     public static final String VALUE="Value";
     
     /**
      * Constant for Destination
      */
     public static final String CONSENT="Consent";
     
     /**
      * Constant for Issuer
      */
     public static final String ISSUER="Issuer";
     
     
     /**
      * Constant for Signature
      */
     public static final String SIGNATURE="Signature";
     
     /**
      * Constant for forceAuthn attribute
      */
     public static final String FORCEAUTHN="ForceAuthn";
     
     /**
     * Constant for IsPassive attribute
      */
    public static final String ISPASSIVE="IsPassive";
     
     /**
      * Constant for AllowCreate attribute
      */
     public static final String ALLOWCREATE="AllowCreate";
 
     /**
      * Constant for ProtocolBinding attribute
      */
     public static final String PROTOBINDING="ProtocolBinding";
     
     /**
      * Constant for Binding parameter name
      */
     public static final String BINDING="binding";
 
 
     /**
      * Constant for Binding namespace
      */
     public static final String BINDING_PREFIX =
     "urn:oasis:names:tc:SAML:2.0:bindings:";
 
     /**
      * Constant for AssertionConsumerServiceIndex attribute
      */
     public static final String ASSERTION_CONSUMER_SVC_INDEX=
     "AssertionConsumerServiceIndex";
     /**
      * Constant for AssertionConsumerServiceURL attribute
      */
     public static final String ASSERTION_CONSUMER_SVC_URL=
     "AssertionConsumerServiceURL";
     /**
      * Constant for AttributeConsumingServiceIndex attribute
      */
     public static final String ATTR_CONSUMING_SVC_INDEX=
     "AttributeConsumingServiceIndex";
     /**
      * Constant for ProviderName attribute
      */
     public static final String PROVIDER_NAME="ProviderName";
     
     /**
      * Constant for Subject Element
      */
     public static final String SUBJECT="Subject";
     
     /**
      * Constant for NameIDPolicy Element
      */
     public static final String NAMEID_POLICY="NameIDPolicy";
     
     /**
      * Constant for Conditions Element.
      */
     public static final String CONDITIONS="Conditions";
     
     /**
      * Constant for RequestedAuthnContext Element.
      */
     public static final String REQ_AUTHN_CONTEXT="RequestedAuthnContext";
 
     /** 
      * Constant for Comparison Attribute
      */
     public static final String COMPARISON ="Comparison";
 
     /**
      * Constant for Scoping Element.
      */
     public static final String SCOPING="Scoping";
     
     /**
      * Constant for Extensions Element.
      */
     public static final String EXTENSIONS="Extensions";
     
     /**
      * Constant for StatusDetail Element.
      */
     public static final String STATUS_DETAIL="StatusDetail";
     
     /**
      * Constant for StatusCode Element.
      */
     public static final String STATUS_CODE="StatusCode";
     
     /**
      * Constant for Status Element.
      */
     public static final String STATUS="Status";
     
     /**
      * Constant for StatusMessage Element.
      */
     public static final String STATUS_MESSAGE="StatusMessage";
     
     /**
      * Constant for GetComplete Element.
      */
     public static final String GETCOMPLETE="GetComplete";
     
     /**
      * Constant for IDPEntry Element.
      */
     public static final String IDPENTRY="IDPEntry";
     
     /**
      * Constant for IDPList Element.
      */
     public static final String IDPLIST="IDPList";
     
     /**
      * Constant for NameIDPolicy Element.
      */
     public static final String NAMEIDPOLICY="NameIDPolicy";
     
     /**
      * Constant for RequesterID Element.
      */
     public static final String REQUESTERID="RequesterID";
 
     // for SAMLPOSTProfileServlet
     public static final String SOURCE_SITE_SOAP_ENTRY = "sourceSite";
     public static final String POST_ASSERTION = "assertion";
     public static final String CLEANUP_INTERVAL_NAME =
                                 "iplanet-am-saml-cleanup-interval";
 
     /**
      * NameID info attribute.
      */ 
     public static final String NAMEID_INFO = "sun-fm-saml2-nameid-info";
 
     /**
      * NameID info key attribute.
      */
     public static final String NAMEID_INFO_KEY = "sun-fm-saml2-nameid-infokey";
 
     /**
      * SAML2 data store provider name.
      */ 
     public static final String SAML2 = "saml2";
 
     /**
      * Auto federation attribute.
      */
     public static final String AUTO_FED_ATTRIBUTE = 
                         "autofedAttribute";
 
     /**
      * Auto federation enable attribute.
      */
     public static final String AUTO_FED_ENABLED =
                         "autofedEnabled";
 
     /**
      * Transient federation users.
      */
     public static final String TRANSIENT_FED_USER =
                         "transientUser";
 
     public static final String NAMEID_TRANSIENT_FORMAT = 
          NAMEID_FORMAT_NAMESPACE + "transient";
 
     /**
      * certficate alias attribute.
      */
     public static final String CERT_ALIAS = "sun-fm-saml2-cert-alias";
  
     /**
      * Attribute map configuration.
      */
     public static final String ATTRIBUTE_MAP = "attributeMap";
 
     /**
      * Service provider account mapper.
      */
     public static final String SP_ACCOUNT_MAPPER = 
                         "spAccountMapper";
 
     /**
      * Service provider attribute mapper.
      */
     public static final String SP_ATTRIBUTE_MAPPER = 
                         "spAttributeMapper";
 
     /**
      * Identity provider account mapper.
      */
     public static final String IDP_ACCOUNT_MAPPER = 
                         "idpAccountMapper";
 
     /**
      * Identity provider attribute mapper.
      */
     public static final String IDP_ATTRIBUTE_MAPPER = 
                         "idpAttributeMapper";
 
     /**
      * RelayState Parameter
      */
     public static final String RELAY_STATE="RelayState";
 
     /**
      * RelayState Alias Parameter
      */
     public static final String RELAY_STATE_ALIAS="RelayStateAlias";
 
     /**
      * Realm Parameter
      */
     public static final String REALM="realm";
 
     /**
      * AssertionConsumerServiceIndex Parameter
      */
     public static final String ACS_URL_INDEX="AssertionConsumerServiceIndex";
 
     /**
      * AttributeConsumingServiceIndex Parameter
      */
     public static final String ATTR_INDEX="AttributeConsumingServiceIndex";
 
     /**
      * NameIDPolicy Format Identifier Parameter
      */
     public static final String NAMEID_POLICY_FORMAT="NameIDFormat";
 
     /**
      * True Value String
      */
     public static final String TRUE="true";
 
     /**
      * False Value String
      */
     public static final String FALSE="false";
 
     public static final String AUTH_LEVEL="AuthLevel";
     public static final String AUTH_LEVEL_ATTR="sunFMAuthContextComparison";
     public static final String AUTH_TYPE="authType";
 
     public static final String AUTH_TYPE_ATTR ="sunFMAuthContextType";
 
     public static final String DECLARE_REF_AUTH_TYPE = "AuthContextDeclareRef";
     public static final String CLASS_REF_AUTH_TYPE = "AuthContextClassRef";
 
     public static final String AUTH_CONTEXT_DECL_REF ="AuthContextDeclRef";
     public static final String AUTH_CONTEXT_DECL_REF_ATTR 
                                         ="sunFMAuthContextDeclareRef";
 
     public static final String AUTH_CONTEXT_CLASS_REF ="AuthnContextClassRef";
 
     public static final String AUTH_CONTEXT_CLASS_REF_ATTR 
                                         ="sunFMAuthContextClassRef";
 
     /**
      * Parameter name for SAML artifact in http request.
      */
     public String SAML_ART = "SAMLart";
 
     /**
      * Identifies SP role and IDP Role.
      */
     public String SP_ROLE = "SPRole";
 
     public static final String IDP_ROLE = "IDPRole";
 
     /**
      * Attribute to be configured in SPSSOConfig for SAML2 authentication
      * module instance name.
      */
     public String AUTH_MODULE_NAME = "saml2AuthModuleName";
 
     /**
      * Attribute to be configured in SPSSOConfig for local authentication url.
      */
     public String LOCAL_AUTH_URL = "localAuthURL";
 
     /**
      * Attribute to be configured in SPSSOConfig for intermediate url.
      */
     public String INTERMEDIATE_URL = "intermediateUrl";
 
     /**
      * Attribute to be configure in SPSSOConfig for default relay state url.
      */
     public String DEFAULT_RELAY_STATE = "defaultRelayState";
 
     /**
      * This is an attribute in entity config for the
      * signing certificate alias
      */
     public static final String SIGNING_CERT_ALIAS = "signingCertAlias";
     
     /**
      * This is an attribute in entity config for the
      * encryption certificate alias
      */
     public static final String ENCRYPTION_CERT_ALIAS = "encryptionCertAlias";
 
     /**
      * The permissible top-level <StatusCode> values
      */
     public static final String STATUS_SUCCESS = 
             "urn:oasis:names:tc:SAML:2.0:status:Success";
     
     public static final String REQUESTER_ERROR = 
             "urn:oasis:names:tc:SAML:2.0:status:Requester";
     
     public static final String RESPONDER_ERROR = 
             "urn:oasis:names:tc:SAML:2.0:status:Responder";
     
     /**
      * The entity role
      */
     public static final String ROLE = "role";
 
     public static final String AM_OR_FM = "com.sun.identity.saml2.am_or_fm";
 
     public static final String SIG_PROVIDER =
     "com.sun.identity.saml2.xmlsig.SignatureProvider";
 
     public static final String ENC_PROVIDER =
     "com.sun.identity.saml2.xmlenc.EncryptionProvider";
     
     // Delimiter used to separate multiple NameIDKey values.
     public String SECOND_DELIM = ";";
 
     /**
      * Http request parameter used to indicate whether the intent is
      * federation or not. Its values are "true" and "false".
      */
     public String FEDERATE = "federate";
     
     /** xmlsig signing parameters*/
     public static final String CANONICALIZATION_METHOD =
          "com.sun.identity.saml.xmlsig.c14nMethod";
     public static final String TRANSFORM_ALGORITHM =
          "com.sun.identity.saml.xmlsig.transformAlg";
     public static final String XMLSIG_ALGORITHM =
          "com.sun.identity.saml.xmlsig.xmlSigAlgorithm";
     
     public static final String DSA = "DSA";
     public static final String RSA = "RSA";      
 
     public static final String SIG_ALG = "SigAlg"; 
     public static final String SHA1_WITH_DSA = "SHA1withDSA";
     public static final String SHA1_WITH_RSA = "SHA1withRSA";
 
     public static final String DEFAULT_ENCODING = "UTF-8";
 
     // SOAP fault code for requester error
     public static final String CLIENT_FAULT = "Client";
 
     // SOAP fault code for responder error
     public static final String SERVER_FAULT = "Server";
 
     public static final String SESSION = "session";
 
     // more constants defined for auth module
     public String ASSERTIONS = "assertions";
     public String MAX_SESSION_TIME = "maxSessionTime";
     public String IN_RESPONSE_TO = "inResponseTo";
 
     public static final String SP_METAALIAS = "spMetaAlias";
     public static final String METAALIAS = "metaAlias";
     public static final String SPENTITYID = "spEntityID";
     public static final String IDPENTITYID = "idpEntityID";
     public static final String REQUESTTYPE = "requestType";
     
     // Encryption attributes
     /**
      * SP Entity Config attribute name. Used to specify whether it wants
      * Assertion encrypted or not.
      */
     public String WANT_ASSERTION_ENCRYPTED = "wantAssertionEncrypted";
 
     public static final String WANT_ATTRIBUTE_ENCRYPTED 
                                    = "wantAttributeEncrypted";
     public static final String WANT_NAMEID_ENCRYPTED = "wantNameIDEncrypted";
 
     // Signing attributes
     /**
      * IDP Entity Config attribute name. Used to specify whether it wants
      * ArtifactResolve signed or not.
      */
     public String WANT_ARTIFACT_RESOLVE_SIGNED = "wantArtifactResolveSigned";
 
     /**
      * SP Entity Config attribute name. Used to specify whether it wants
      * ArtifactResponse signed or not.
      */
     public String WANT_ARTIFACT_RESPONSE_SIGNED =
                               "wantArtifactResponseSigned";
     public static final String WANT_LOGOUT_REQUEST_SIGNED  
                                    = "wantLogoutRequestSigned";
     public static final String WANT_LOGOUT_RESPONSE_SIGNED   
                                    = "wantLogoutResponseSigned ";
     public static final String WANT_MNI_REQUEST_SIGNED = "wantMNIRequestSigned";
     public static final String WANT_MNI_RESPONSE_SIGNED 
                                    = "wantMNIResponseSigned";
     
     /**
      * Constant for SAML2IDPSessionIndex SSO token property
      */
     public static final String IDP_SESSION_INDEX = "SAML2IDPSessionIndex";
     /**
      * Constant for IDPMetaAlias SSO token property
      */
     public static final String IDP_META_ALIAS="IDPMetaAlias";
 
     // Basic auth for SOAP binding
     public static final String BASIC_AUTH_ON = "basicAuthOn";
     public static final String BASIC_AUTH_USER = "basicAuthUser";
     public static final String BASIC_AUTH_PASSWD = "basicAuthPassword";
 
     /**
      * Service provider AuthnContext mapper.
      */
     public static final String SP_AUTHCONTEXT_MAPPER =
                         "spAuthncontextMapper";
 
     /**
      * Default value for Service provider AuthnContext mapper value.
      */
     public static final String DEFAULT_SP_AUTHCONTEXT_MAPPER =
         "com.sun.identity.saml2.plugins.DefaultSPAuthnContextMapper";
 
     /**
      * Service provider AuthnContext Class Reference and AuthLevel Mapping.
      */
     public static final String SP_AUTH_CONTEXT_CLASS_REF_ATTR=
                         "spAuthncontextClassrefMapping";
 
     /**
      * Constant for AuthnContext Class Reference namespace
      */
     public static final String AUTH_CTX_PREFIX =
     "urn:oasis:names:tc:SAML:2.0:ac:classes:";
 
     /**
      * Default Service provider AuthnContext Class Reference and 
      * AuthLevel Mapping value.
      */
     public static final String SP_AUTHCONTEXT_CLASSREF_VALUE=
                         "PasswordProtectedTransport|0|default";
     /**
      * Service provider AuthnContext Comparison Type attribute name.
      */
     public static final String SP_AUTHCONTEXT_COMPARISON_TYPE =
                         "spAuthncontextComparisonType";
 
     /**
      * Default Service provider AuthnContext Comparison Type 
      * attribute value.
      */
     public static final String SP_AUTHCONTEXT_COMPARISON_TYPE_VALUE = "exact";
 
     /**
      * Service provider AuthnContext Comparison Parameter Name
      */
     public static final String SP_AUTHCONTEXT_COMPARISON = "AuthComparison";
 
     // Time Skew for Assertion NotOnOrAfter. In seconds.
     public static final String ASSERTION_TIME_SKEW = "assertionTimeSkew";
     public static final int ASSERTION_TIME_SKEW_DEFAULT = 300;
 
     // key for SAML2 SDK class mapping
     public static final String SDK_CLASS_MAPPING = 
         "com.sun.identity.saml2.sdk.mapping.";
 
     // Default assertion effective time in seconds
     public static final int ASSERTION_EFFECTIVE_TIME = 600;
 
     // Assertion effective time attribute name
     public static final String ASSERTION_EFFECTIVE_TIME_ATTRIBUTE = 
                             "assertionEffectiveTime";
 
     // IDP authn context mapper class attribute name
     public static final String IDP_AUTHNCONTEXT_MAPPER_CLASS =
                             "idpAuthncontextMapper";
 
     // Default IDP authn context mapper class name
     public static final String DEFAULT_IDP_AUTHNCONTEXT_MAPPER_CLASS =
         "com.sun.identity.saml2.plugins.DefaultIDPAuthnContextMapper";
 
     // Default IDP account mapper class name
     public static final String DEFAULT_IDP_ACCOUNT_MAPPER_CLASS =
         "com.sun.identity.saml2.plugins.DefaultIDPAccountMapper";
 
     // Default SP account mapper class name
     public static final String DEFAULT_SP_ACCOUNT_MAPPER_CLASS =
         "com.sun.identity.saml2.plugins.DefaultSPAccountMapper";
 
     // Default IDP attribute mapper class name
     public static final String DEFAULT_IDP_ATTRIBUTE_MAPPER_CLASS =
         "com.sun.identity.saml2.plugins.DefaultIDPAttributeMapper";
 
     // IDP authn context class reference mapping attribute name
     public static final String IDP_AUTHNCONTEXT_CLASSREF_MAPPING =
                             "idpAuthncontextClassrefMapping";
 
     // AuthnContext Class Reference names
     public static final String CLASSREF_PASSWORD_PROTECTED_TRANSPORT =
         "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
         
     // COT List
     public static final String COT_LIST = COTConstants.COT_LIST;
 
     // http parameter to default.jsp
     public static final String MESSAGE = "message";
 
     // Cache Cleanup interval attribute name in AMConfig.properties.
     // value in seconds
     public static final String CACHE_CLEANUP_INTERVAL = 
                 "com.sun.identity.saml2.cacheCleanUpInterval";
 
     // default Cache cleanup interval in seconds
     public static final int CACHE_CLEANUP_INTERVAL_DEFAULT = 3600;
 
     // IDP SLO parameter name for logout all sessions
     public static final String LOGOUT_ALL = "logoutAll";
 
     // IDP response info ID
     public static final String RES_INFO_ID = "resInfoID";
      
     // Default query parameter to use for RelayState if
     // RelayState is no specified and if RelayState cannot
     // be obtained from query parameters list specified in 
     // RelayStateAlias 
 
     public static final String GOTO = "goto";
     
     // Delimiter for values of multi-valued property set in SSO token
     public static final char DELIMITER = '|';
 
     // Escape string for the <code>DELIMITER</code> contained in the values
     // of multi-valued property set in SSO token
     public static final String ESCAPE_DELIMITER = "&#124;";
 }
