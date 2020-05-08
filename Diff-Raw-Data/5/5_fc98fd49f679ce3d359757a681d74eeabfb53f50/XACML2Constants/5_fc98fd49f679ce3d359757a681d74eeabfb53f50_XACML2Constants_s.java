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
 * $Id: XACML2Constants.java,v 1.2 2007-03-24 01:25:41 dillidorai Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.xacml2.common;
 
 /**
  * This interface  defines constants common to all XACML2 elements.
  *
  * @supported.all.api
  */
 public class  XACML2Constants {
 
     /**
      * Constant for Request element
      */
     public  static String REQUEST = "Request";
 
     /**
      * Constant for Subject element
      */
     public static String SUBJECT = "Subject";
 
     /**
      * Constant for Attribute element
      */
     public static String ATTRIBUTE = "Attribute";
 
     /**
      * Constant for AttributeValue element
      */
 
     /**
      * Constant for AttributeValue element
      */
     public static String ATTRIBUTE_VALUE = "AttributeValue";
 
     /**
      * Constant for ResourceContent element
      */
     public static String RESOURCE_CONTENT = "ResourceContent";
       
      /**
      * Constant for Resource element
      */
     public static String RESOURCE = "Resource";
     
        /**
      * Constant for Action element
      */
     public static String ACTION = "Action";
     
      
     /**
      * Constant for Environment element
      */
     public static String ENVIRONMENT = "Environment";
       
     /**
      * Constant for SubjectCategory element
      */
     public static  String SUBJECT_CATEGORY = "SubjectCategory";
       
     /**
      * Constant for AttributeId element
      */
     public static  String ATTRIBUTE_ID ="AttributeId";
   
     /**
      * Constant for DataType element
      */
     public  static  String DATATYPE ="DataType";
     
     /**
      * Constant for Issuer element
      */
     public  static  String ISSUER ="Issuer";
  
     /**
      * Constant for ReturnContext attribute
      */
     public  static  String RETURNCONTEXT ="ReturnContext";
       
    /**
      * Constant for InputContextOnly attribute
      */
     public  static  String INPUTCONTEXTONLY ="InputContextOnly";
       
     /**
      * Constant for XACMLAuthzDecisionQuery element
      */
     public static String XACMLAUTHZDECISIONQUERY =
             "XACMLAuthzDecisionQuery";
     
       /**
      * The standard URI for the default subject category value
      */
     public static String SUBJECT_CATEGORY_DEFAULT =
         "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
 
     /**
      * The URI for the intermediary subject category value
      */
     public static String SUBJECT_CATEGORY_INTERMEDIARY =
         "urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject";
 
     /**
      * XML name space URI
      */
     public static String NS_XML = 
         "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance/\"";
     
     /**
      * String used to declare XACML2 context namespace prefix.
      */
     public static String CONTEXT_PREFIX = "xacml-context:";
     
     /**
      * String used to declare XACML2 context namespace.
      */
     public static String CONTEXT_DECLARE_STR =
     " xmlns:xacml-context=\"urn:oasis:names:tc:xacml:2.0:context:schema:os\" ";
     
     /**
      * XACML2 context schema location 
      */
     public static String CONTEXT_SCHEMA_LOCATION=
     "xsi:schemaLocation=\"urn:oasis:names:tc:xacml:2.0:context:schema:os http:"
         +"//docs.oasis-open.org/xacml/access_control-xacml"
         +"-2.0-context-schema-os.xsd\"";
     /**
      * String used to declare SAML2 Protocol namespace prefix.
      */
     public static String SAMLP_PREFIX = "samlp:";
     
     /**
      * String used to declare SAML2 protocol namespace.
      */
    public String SAMLP_DECLARE_STR =
         "xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"";
     
     /** 
      * Start Tag for XML String
      */
 
     public static String START_TAG="<";
     /**
      * End Tag for XML String
      */
 
     public static String END_TAG =">";
     
     /**
      * Constant for space
      */
     public static  String SPACE=" ";
     /**
      * Constant for equal
      */
     public static  String EQUAL= "=";
     
     /**
      * Constant for quote
      */
     public static  String QUOTE = "\"";
     
     /**
      * Constant for newline
      */
     public static String NEWLINE= "\n";
     
     /**
      * Constant for xml name space
      */
     public static  String NAMESPACE_PREFIX="xmlns";
     
     /**
      *  key for XACML2 SDK class mapping
      */
     public static  String SDK_CLASS_MAPPING = 
         "com.sun.identity.xacml2.sdk.mapping.";
 
     /**
      * constant for String DataType
      */
     public static  String STRING_DATATYPE = 
         "http://www.w3.org/2001/XMLSchema#string";
 
     /**
      * constant for URI DataType
      **/
     public static String URI_DATATYPE = 
         "http://www.w3.org/2001/XMLSchema#anyURI";
 
     /**
      * constant for subject category
      */
     public static String SUBJECT_CATEGORY_ID =
         "urn:oasis:names:tc:xacml:1.0:subject-category";
     
 
 
     /**
      * constant for Response element
      */
     public static final String RESPONSE_ELEMENT = "Response";
 
     /**
      * constant for Result element
      */
     public static final String RESULT_ELEMENT = "Result";
 
     /**
      * constant for ResourceId attribute
      */
     public static final String RESOURCE_ID_ATTRIBUTE = "ResourceId";
 
     /**
      * constant for Decision element
      */
     public static final String DECISION_ELEMENT = "Decision";
 
     /**
      * constant for Status element
      */
     public static final String STATUS_ELEMENT = "Status";
 
     /**
      * constant for StatusCode element
      */
     public static final String STATUS_CODE_ELEMENT = "StatusCode";
 
     /**
      * constant for Value attribute
      */
     public static final String VALUE_ATTRIBUTE = "Value";
 
     /**
      * constant for StatusMessage element
      */
     public static final String STATUS_MESSAGE_ELEMENT = "StatusMessage";
 
     /**
      * constant for StatusDetail element
      */
     public final static String STATUS_DETAIL_ELEMENT = "StatusDetail";
 
     /**
      * constant for Permit
      */
     public static final String PERMIT = "Permit";
 
     /**
      * constant for Deny
      */
     public static final String DENY = "Deny";
 
     /**
      * constant for Indeterminate
      */
     public static final String INDETERMINATE = "Indeterminate";
 
     /**
      * constant for NotApplicable
      */
     public static final String NOT_APPLICABLE = "NotApplicable";
 
 }
