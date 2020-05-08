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
 * $Id: SMSConstants.java,v 1.2 2007-09-10 22:29:28 bt199000 Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 /**
  * <code>SMSConstants</code> is an interface which contains datastore 
  * attributes, keys, and values for LDAPv3 and flatfile datastores.
  */
 public interface SMSConstants {
     
     /**
      * Prefix for the parameter in datastore property file in 
      * this format: <datastore prefix>.<attribute>
      */
     static final String SMS_DATASTORE_PARAMS_PREFIX = 
             "SMSGlobalDatastoreConfig";
     
     /**
      * Prefix for datastore key.  Any key is not datastore attributes should
      * have this prefix i.e. datastore-count, datastore-type, etc.
      */
     static final String SMS_DATASTORE_KEY_PREFIX = "datastore";
 
     /**
      * Datastore count
      */
     static final String SMS_DATASTORE_COUNT = "datastore-count";
     
     /**
      * Datastore realm
      */
     static final String SMS_DATASTORE_REALM = "datastore-realm";
     
     /**
      * Datastore name
      */
     static final String SMS_DATASTORE_NAME = "datastore-name";
     
     /**
      * Datastore admin id
      */
     static final String SMS_DATASTORE_ADMINID = "datastore-adminid";
     
     /**
      * Datastore admin pw
      */
     static final String SMS_DATASTORE_ADMINPW = "datastore-adminpw";
     
     /**
      * Datastore type
      */
     static final String SMS_DATASTORE_TYPE = "datastore-type";
     
     /**
      * Datastore type key for Sun DS with AM schema
      */
     static final String SMS_DATASTORE_TYPE_AMDS = "ds";
 
     /**
      * Datastore type key for AD
      */
     static final String SMS_DATASTORE_TYPE_AD = "ad";
 
     /**
      * Datastore type key for generic LDAP
      */
     static final String SMS_DATASTORE_TYPE_LDAP = "ldap";
 
     /**
      * Datastore type key for flat file
      */
     static final String SMS_DATASTORE_TYPE_FF = "ff";
     
     /**
      * Datastore type key for amsdk
      */
     static final String SMS_DATASTORE_TYPE_AMSDK = "amsdk";
     
     /**
      * Datastore schema type name for Sun DS
      */
     static final String SMS_DATASTORE_SCHEMA_TYPE_AMDS = "LDAPv3ForAMDS";
     
     /**
      * Datastore schema type name for AD
      */
     static final String SMS_DATASTORE_SCHEMA_TYPE_AD = "LDAPv3ForAD";
     
     /**
      * Datastore schema type name for generic LDAP
      */
     static final String SMS_DATASTORE_SCHEMA_TYPE_LDAP = "LDAPv3";
     
     /**
      * Datastore schema type name for flat file
      */
     static final String SMS_DATASTORE_SCHEMA_TYPE_FF = "files";
     
     /**
      * Datastore schema type name for AM SDK
      */
     static final String SMS_DATASTORE_SCHEMA_TYPE_AMSDK = "amSDK";
     
     /**
      * Access Manager user schema list key in property file
      */
     static final String SMS_SCHEMNA_LIST = "SMSGlobalConfig.schemalist";
     
     /**
      * Attributes for LDAPv3 datastore
      */
     static final String SMS_LDAP_SCOPE_BASE = "SCOPE_BASE";
     static final String SMS_LDAP_SCOPE_ONE = "SCOPE_ONE";
     static final String SMS_LDAP_SCOPE_SUB = "SCOPE_SUB";    
     static final String SMS_PLUGIN_CLASS = "sunIdRepoClass";
     static final String SMS_LDAPv3_PREFIX = "sun-idrepo-ldapv3-config-";
     static final String SMS_LDAPv3_LDAP_SERVER =
         "sun-idrepo-ldapv3-config-ldap-server";
     static final String SMS_LDAPv3_LDAP_PORT =
         "sun-idrepo-ldapv3-config-ldap-port";
     static final String SMS_LDAPv3_AUTHID =
         "sun-idrepo-ldapv3-config-authid";
     static final String SMS_LDAPv3_AUTHPW =
         "sun-idrepo-ldapv3-config-authpw";
     static final String SMS_LDAPv3_LDAP_SSL_ENABLED =
         "sun-idrepo-ldapv3-config-ssl-enabled";
     static final String SMS_LDAPv3_ORGANIZATION_NAME =
         "sun-idrepo-ldapv3-config-organization_name";  
     static final String SMS_LDAPv3_LDAP_CONNECTION_POOL_MIN_SIZE =
         "sun-idrepo-ldapv3-config-connection_pool_min_size";
     static final String SMS_LDAPv3_LDAP_CONNECTION_POOL_MAX_SIZE =
         "sun-idrepo-ldapv3-config-connection_pool_max_size";
     static final String SMS_LDAPv3_ATTR_MAPPING =  "sunIdRepoAttributeMapping";
     static final String SMS_LDAPv3_SUPPORT_OPERATION =  
             "sunIdRepoSupportedOperations";
     static final String SMS_LDAPv3_LDAP_GROUP_SEARCH_FILTER =
         "sun-idrepo-ldapv3-config-groups-search-filter";
     static final String SMS_LDAPv3_LDAP_USERS_SEARCH_FILTER =
         "sun-idrepo-ldapv3-config-users-search-filter";
     static final String SMS_LDAPv3_LDAP_ROLES_SEARCH_FILTER =
         "sun-idrepo-ldapv3-config-roles-search-filter";
     static final String SMS_LDAPv3_LDAP_FILTERROLES_SEARCH_FILTER =
         "sun-idrepo-ldapv3-config-filterroles-search-filter";
     static final String SMS_LDAPv3_LDAP_AGENT_SEARCH_FILTER =
         "sun-idrepo-ldapv3-config-agent-search-filter";
     static final String SMS_LDAPv3_LDAP_ROLES_SEARCH_ATTRIBUTE =
         "sun-idrepo-ldapv3-config-roles-search-attribute";
     static final String SMS_LDAPv3_LDAP_FILTERROLES_SEARCH_ATTRIBUTE =
         "sun-idrepo-ldapv3-config-filterroles-search-attribute";
     static final String SMS_LDAPv3_LDAP_GROUPS_SEARCH_ATTRIBUTE =
         "sun-idrepo-ldapv3-config-groups-search-attribute";
     static final String SMS_LDAPv3_LDAP_USERS_SEARCH_ATTRIBUTE =
         "sun-idrepo-ldapv3-config-users-search-attribute";
     static final String SMS_LDAPv3_LDAP_AGENT_SEARCH_ATTRIBUTE =
         "sun-idrepo-ldapv3-config-agent-search-attribute";
     static final String SMS_LDAPv3_LDAP_ROLES_SEARCH_SCOPE =
         "sun-idrepo-ldapv3-config-role-search-scope";
     static final String SMS_LDAPv3_LDAP_SEARCH_SCOPE =
         "sun-idrepo-ldapv3-config-search-scope";
     static final String SMS_LDAPv3_LDAP_GROUP_CONTAINER_NAME =
         "sun-idrepo-ldapv3-config-group-container-name";
     static final String SMS_LDAPv3_LDAP_AGENT_CONTAINER_NAME =
         "sun-idrepo-ldapv3-config-agent-container-name";
     static final String SMS_LDAPv3_LDAP_PEOPLE_CONTAINER_NAME =
         "sun-idrepo-ldapv3-config-people-container-name";
     static final String SMS_LDAPv3_LDAP_GROUP_CONTAINER_VALUE =
         "sun-idrepo-ldapv3-config-group-container-value";
     static final String SMS_LDAPv3_LDAP_PEOPLE_CONTAINER_VALUE =
         "sun-idrepo-ldapv3-config-people-container-value";
     static final String SMS_LDAPv3_LDAP_AGENT_CONTAINER_VALUE =
         "sun-idrepo-ldapv3-config-agent-container-value";
     static final String SMS_LDAPv3_LDAP_TIME_LIMIT =
         "sun-idrepo-ldapv3-config-time-limit";
     static final String SMS_LDAPv3_LDAP_MAX_RESULT =
         "sun-idrepo-ldapv3-config-max-result";
     static final String SMS_LDAPv3_REFERRALS =
         "sun-idrepo-ldapv3-config-referrals";
     static final String SMS_LDAPv3_ROLE_OBJECT_CLASS =
         "sun-idrepo-ldapv3-config-role-objectclass";
     static final String SMS_LDAPv3_FILTERROLE_OBJECT_CLASS =
         "sun-idrepo-ldapv3-config-filterrole-objectclass";
     static final String SMS_LDAPv3_GROUP_OBJECT_CLASS =
         "sun-idrepo-ldapv3-config-group-objectclass";
     static final String SMS_LDAPv3_USER_OBJECT_CLASS =
         "sun-idrepo-ldapv3-config-user-objectclass";
     static final String SMS_LDAPv3_AGENT_OBJECT_CLASS =
         "sun-idrepo-ldapv3-config-agent-objectclass";
     static final String SMS_LDAPv3_GROUP_ATTR =
         "sun-idrepo-ldapv3-config-group-attributes";
     static final String SMS_LDAPv3_USER_ATTR =
         "sun-idrepo-ldapv3-config-user-attributes";
     static final String SMS_LDAPv3_AGENT_ATTR =
         "sun-idrepo-ldapv3-config-agent-attributes";
     static final String SMS_LDAPv3_ROLE_ATTR =
         "sun-idrepo-ldapv3-config-role-attributes";
     static final String SMS_LDAPv3_FILTERROLE_ATTR =
         "sun-idrepo-ldapv3-config-filterrole-attributes";
     static final String SMS_LDAPv3_NSROLE =
         "sun-idrepo-ldapv3-config-nsrole";
     static final String SMS_LDAPv3_NSROLEDN =
         "sun-idrepo-ldapv3-config-nsroledn";
     static final String SMS_LDAPv3_NSROLEFILTER =
         "sun-idrepo-ldapv3-config-nsrolefilter";
     static final String SMS_LDAPv3_MEMBEROF =
         "sun-idrepo-ldapv3-config-memberof";
     static final String SMS_LDAPv3_UNIQUEMEMBER =
         "sun-idrepo-ldapv3-config-uniquemember";
     static final String SMS_LDAPv3_MEMBERURL =
         "sun-idrepo-ldapv3-config-memberurl";
     static final String SMS_LDAPv3_LDAP_IDLETIMEOUT =
         "sun-idrepo-ldapv3-config-idletimeout";
     static final String SMS_LDAPv3_LDAP_PSEARCHBASE =
             "sun-idrepo-ldapv3-config-psearchbase";
     static final String SMS_LDAPv3_LDAP_PSEARCHFILTER =
             "sun-idrepo-ldapv3-config-psearch-filter";
     static final String SMS_LDAPv3_LDAP_ISACTIVEATTRNAME =
             "sun-idrepo-ldapv3-config-isactive";
     static final String SMS_LDAPv3_LDAP_INETUSERACTIVE =
             "sun-idrepo-ldapv3-config-active";
     static final String SMS_LDAPv3_LDAP_INETUSERINACTIVE =
             "sun-idrepo-ldapv3-config-inactive";
     static final String SMS_LDAPv3_LDAP_CREATEUSERMAPPING =
             "sun-idrepo-ldapv3-config-createuser-attr-mapping";
     static final String SMS_LDAPv3_LDAP_AUTHENTICATABLE =
             "sun-idrepo-ldapv3-config-authenticatable-type";
     static final String SMS_LDAPv3_LDAP_AUTHENTICATION_NAME_ATTR = 
             "sun-idrepo-ldapv3-config-auth-naming-attr";
     static final String SMS_LDAPv3_LDAP_CACHEENABLED =
             "sun-idrepo-ldapv3-config-cache-enabled";
     static final String SMS_LDAPv3_LDAP_CACHETTL =
             "sun-idrepo-ldapv3-config-cache-ttl";
     static final String SMS_LDAPv3_LDAP_CACHESIZE =
             "sun-idrepo-ldapv3-config-cache-size";
     static final String SMS_LDAPv3_LDAP_RETRIES = 
             "sun-idrepo-ldapv3-config-numretires";
     static final String SMS_LDAPv3_LDAP_DEPLAY = 
             "com.iplanet.am.ldap.connection.delay.between.retries";
     static final String SMS_LDAPv3_LDAP_ERRORCODE = 
             "sun-idrepo-ldapv3-config-errorcodes";
     /** 
      * Attributes for flat file idRepo
      */
     static final String SMS_FILES_CLASS = "sunIdRepoClass";
     static final String SMS_FILES_DIRECTORY = "sunFilesIdRepoDirectory";
     static final String SMS_FILES_CACHE = "sunFilesMonitorForChanges";
     static final String SMS_FILES_CACHE_INTERVAL = "sunFilesMonitoringTime";
     static final String SMS_FILES_OBJECT_CLASS = "sunFilesObjectClasses";
     static final String SMS_FILES_PASSWORD_ATTR = "sunFilesPasswordAttr";
     static final String SMS_FILES_STATUS_ATTR = "sunFilesStatusAttr";
     static final String SMS_FILES_HASH_ATTR = "sunFilesHashAttrs";
     static final String SMS_FILES_ENCRYPT_ATTR = "sunFilesEncryptAttrs";
     
     /** 
      * Attributes for am sdk idRepo
      */
     static final String SMS_AMSDK_CLASS = "sunIdRepoClass";
     static final String SMS_AMSDK_ORG_NAME  ="amSDKOrgName";
     static final String SMS_AMSDK_PEOPLE_CONTAINER_NAME = 
             "sun-idrepo-amSDK-config-people-container-name";
     static final String SMS_AMSDK_PEOPLE_CONTAINER_VALUE = 
             "sun-idrepo-amSDK-config-people-container-value";
     static final String SMS_AMSDK_AGENT_CONTAINER_NAME = 
             "sun-idrepo-amSDK-config-agent-container-name";
     static final String SMS_AMSDK_AGENT_CONTAINER_VALUE = 
             "sun-idrepo-amSDK-config-agent-container-value";
     static final String SMS_AMSDK_RECURSIVE_ENABLED = 
             "sun-idrepo-amSDK-config-recursive-enabled";
     static final String SMS_AMSDK_COPYCONFIG_ENABLED = 
             "sun-idrepo-amSDK-config-copyconfig-enabled";
 }
