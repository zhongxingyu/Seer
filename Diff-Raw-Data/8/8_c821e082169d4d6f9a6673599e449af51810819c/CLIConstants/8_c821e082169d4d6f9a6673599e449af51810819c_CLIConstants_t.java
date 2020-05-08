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
 * $Id: CLIConstants.java,v 1.9 2008-08-21 20:24:26 srivenigan Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.cli;
 
 /**
  * <code>CLIConstants</code> contains strings for the supported 
  * sub-commands of the ssoadm CLI.
  */
 public interface CLIConstants {
     /**
      * String for the "create-realm" sub-command
      */
     public static final String CREATE_REALM_SUBCOMMAND = "create-realm";
     
     /**
      * String for the "list-realms" sub-command
      */
     public static final String LIST_REALMS_SUBCOMMAND = "list-realms";
     
     /**
      * String for the "delete-realm" sub-command
      */
     public static final String DELETE_REALM_SUBCOMMAND = "delete-realm";
     
     /**
      * String for the "create-identity" sub-command
      */
     public static final String CREATE_IDENTITY_SUBCOMMAND = "create-identity";
     
     /**
      * String for the "list-identities" sub-command
      */
     public static final String LIST_IDENTITIES_SUBCOMMAND = "list-identities";
     
     /**
      * String for the "delete-identities" sub-command
      */
     public static final String DELETE_IDENTITIES_SUBCOMMAND = 
             "delete-identities";
     
     /**
      * String for the "add-member" sub-command
      */
     public static final String ADD_MEMBER_SUBCOMMAND = "add-member";
     
     /**
      * String for the "remove-member" sub-command
      */
     public static final String REMOVE_MEMBER_SUBCOMMAND = "remove-member";
     
     /**
      * String for the "show-members" sub-command
      */
     public static final String SHOW_MEMBERS_SUBCOMMAND = "show-members";
     
     /**
      * String for the "show-memberships" sub-command
      */
     public static final String SHOW_MEMBERSHIPS_SUBCOMMAND = "show-memberships";
     
     /**
      * String for the "add-attr-defs" sub-command
      */
     public static final String ADD_ATTRIBUTE_DEFAULTS_SUBCOMMAND = 
             "add-attr-defs";
     
     /**
      * String for the "add-attrs" sub-command
      */
     public static final String ADD_ATTRIBUTES_SUBCOMMAND = "add-attrs";
     
     /**
      * String for the "add-cot-member" sub-command
      */
     public static final String ADD_CIRCLE_OF_TRUST_MEMBER_SUBCOMMAND = 
             "add-cot-member";
     
     /**
      * String for the "add-plugin-interface" sub-command
      */
     public static final String ADD_PLUGIN_INTERFACE_SUBCOMMAND = 
             "add-plugin-interface";
     
     /**
      * String for the "add-realm-attrs" sub-command
      */
     public static final String ADD_REALM_ATTRIBUTES_SUBCOMMAND = 
             "add-realm-attrs";
     
     /**
      * String for the "add-res-bundle" sub-command
      */
     public static final String ADD_RESOURCE_BUNDLE_SUBCOMMAND = 
             "add-res-bundle";
     
     /**
      * String for the "add-svc-identity" sub-command
      */
     public static final String ADD_SERVICE_IDENTITY_SUBCOMMAND = 
             "add-svc-idenity";
     
     /**
      * String for the "add-sub-schema" sub-command
      */
     public static final String ADD_SUB_SCHEMA_SUBCOMMAND = "add-sub-schema";
     
     /**
      * String for the "create-auth-cfg" sub-command
      */
     public static final String CREATE_AUTH_CONFIGURATION_SUBCOMMAND = 
             "create-auth-cfg";
     
     /**
      * String for the "create-auth-instance" sub-command
      */
     public static final String CREATE_AUTH_INSTANCE_SUBCOMMAND = 
             "create-auth-instance";
     
     /**
      * String for the "create-cot" sub-command
      */
     public static final String CREATE_CIRCLE_OF_TRUST_SUBCOMMAND = 
             "create-cot";
     
     /**
      * String for the "create-datastore" sub-command
      */
     public static final String CREATE_DATASTORE_SUBCOMMAND = "create-datastore";
     
     /**
      * String for the "create-metadata-templ" sub-command
      */
     public static final String CREATE_METADATA_TEMPLATE_SUBCOMMAND = 
             "create-metadata-templ";
     
     /**
      * String for the "create-policies" sub-command
      */
     public static final String CREATE_POLICIES_SUBCOMMAND = "create-policies";
     
     /**
      * String for the "create-svc" sub-command
      */
     public static final String CREATE_SERVICE_SUBCOMMAND = "create-svc";
     
     /**
     * String for the "delete-svc" sub-command
     */
    public static final String DELETE_SERVICE_SUBCOMMAND = "delete-svc";
    
    /**
      * String for the "create-svrcfg-xml" sub-command
      */
     public static final String CREATE_SERVERCONFIG_XML_SUBCOMMAND = 
             "create-svrcfg-xml";
     
     /**
      * String for the "create-subcfg" sub-command
      */
     public static final String CREATE_SUB_CONFIGURATION_SUBCOMMAND = 
             "create-sub-cfg";
     
     /**
      * String for the "delete-realm-attr" sub-command
      */
     public static final String DELETE_REALM_ATTRIBUTE_SUBCOMMAND = 
             "delete-realm-attr";
     
     /**
      * String for the "get-realm" sub-command
      */
     public static final String GET_REALM_SUBCOMMAND = "get-realm";
     
     /**
      * String for the "set-realm-attrs" sub-command
      */
     public static final String SET_REALM_ATTRIBUTES_SUBCOMMAND = 
             "set-realm-attrs";
     
     /**
      * String for the "get-identity" sub-command
      */
     public static final String GET_IDENTITY_SUBCOMMAND = "get-identity";
     
     /**
      * String for the "delete-policies" sub-command
      */
     public static final String DELETE_POLICIES_SUBCOMMAND = "delete-policies";
     
     /**
      * String for the "list-policies" sub-command
      */
     public static final String LIST_POLICIES_SUBCOMMAND = "list-policies";
     
     /**
      * String for the "delete-auth-instances" sub-command
      */
     public static final String DELETE_AUTH_INSTANCES_SUBCOMMAND = 
             "delete-auth-instances";
     
     /**
      * String for the "get-auth-instance" sub-command
      */
     public static final String GET_AUTH_INSTANCE_SUBCOMMAND = 
             "get-auth-instance";
     
     /**
      * String for the "list-auth-instances" sub-command
      */
     public static final String LIST_AUTH_INSTANCES_SUBCOMMAND =
             "list-auth-instances";
     
     /**
      * String for the "update-auth-instance" sub-command
      */
     public static final String UPDATE_AUTH_INSTANCE_SUBCOMMAND =
             "update-auth-instance";
     
     /**
      * String for the "add-privileges" sub-command
      */
     public static final String ADD_PRIVILEGES_SUBCOMMAND = "add-privileges";
     
     /**
      * String for the "remove-privileges" sub-command
      */
     public static final String REMOVE_PRIVILEGES_SUBCOMMAND = 
             "remove-privileges";
     
     /**
      * String for the "show-privileges" sub-command
      */
     public static final String SHOW_PRIVILEGES_SUBCOMMAND = "show-privileges";
     
     /**
      * String for the "delete-datastores" sub-command
      */
     public static final String DELETE_DATASTORES_SUBCOMMAND = 
             "delete-datastores";
 
     /**
      * String for the "list-datastores" sub-command
      */
     public static final String LIST_DATASTORES_SUBCOMMAND = 
             "list-datastores";
     
     /**
      * String for the "update-datastores" sub-command
      */
     public static final String UPDATE_DATASTORES_SUBCOMMAND = 
             "update-datastores"; 
     
     /**
      * String for the "set-svc-attrs"
      */
     public static final String SET_SERVICE_ATTRIBUTES_SUBCOMMAND = 
             "set-svc-attrs";
 
     /**
      * String for the "get-revision-number"
      */
     public static final String GET_REVISION_NUMBER_SUBCOMMAND = 
             "get-revision-number";
 
     /**
      * String for the "get-revision-number"
      */
     public static final String SET_REVISION_NUMBER_SUBCOMMAND = 
             "set-revision-number";
 
     /**
      * String for the "create-agent"
      */
     public static final String CREATE_AGENT_SUBCOMMAND = 
             "create-agent";
 
     /**
      * String for the "list-agent"
      */
     public static final String LIST_AGENT_SUBCOMMAND = 
             "list-agents";
 
     /**
      * String for the "delete-identities" sub-command
      */
     public static final String DELETE_AGENTS_SUBCOMMAND = 
             "delete-agents";
 }
