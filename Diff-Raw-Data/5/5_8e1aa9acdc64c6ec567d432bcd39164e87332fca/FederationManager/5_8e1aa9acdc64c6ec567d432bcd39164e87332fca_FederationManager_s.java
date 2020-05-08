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
 * $Id: FederationManager.java,v 1.2 2007-01-23 06:46:09 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.federation.cli.definition;
 
 import com.sun.identity.cli.annotation.DefinitionClassInfo;
 import com.sun.identity.cli.annotation.Macro;
 import com.sun.identity.cli.annotation.SubCommandInfo;
 import com.sun.identity.cli.annotation.ResourceStrings;
 
 public class FederationManager {
     @DefinitionClassInfo(
         productName="Sun Java System Federation Manager",
         version="8.0",
         resourceBundle="FederationManagerCLI")
     private String product;
 
     @ResourceStrings(
         string="cannot-write-to-file=Unable to write to file, {0}\nfile-not-found=File not found, {0}\nunsupported-specification=Unsupported specification."
     )
     private String resourcestrings;
 
     @Macro(
         mandatoryOptions={
             "adminid|u|s|Administrator ID of running the command.",
             "password|w|s|Password of administrator.",
             "password-file|f|s|File name that contains password of administrator."},
         optionalOptions={},
         optionAliases={
             "password|password-file"})
     private String authentication;
 
 
     /**
      * From Access Manager
      */
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.LoadSchema",
         description="Create a new service in server.",
         mandatoryOptions={
             "xmlfile|X|m|XML file(s) that contains schema."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "continue|c|u|Continue adding service if one or more previous service cannot be added."},
         resourceStrings={
             "schema-added=Service is added.",
             "schema-failed=Service is not added."})
     private String create_service;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.DeleteService",
         description="Delete service from the server.",
         mandatoryOptions={
             "servicename|s|m|Service Name(s)."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "continue|c|u|Continue deleting service if one or more previous services cannot be deleted.",
             "deletepolicyrule|r|u|Delete policy rule."},
         resourceStrings={
             "service-deleted=Service is deleted.",
             "service-deletion-failed=Service is not deleted.",
             "delete-service-no-policy-rules=There are no policy rules.",
             "delete-service-no-policy-schema=There are no policy schema.",
             "delete-service-delete-policy-rules=Delete policy rules."})
     private String delete_service;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.RemoveAttributeDefaults",
         description="Remove default attribute values in schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema.",
             "attributenames|a|m|Attribute name(s)."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "subschemaname|c|s|Name of sub schema."},
         resourceStrings={
             "schema-remove-attribute-defaults-succeed=Schema attribute defaults are removed.",
             "schema-sub-schema-does-not-exists=Sub Schema does not exist, {0}.",
             "supported-schema-type=Unsupported Schema Type, {0}."})
     private String remove_attribute_defaults;
                                                                                 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.AddAttributeDefaults",
         description="Add default attribute values in schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "attributevalues|a|m|Attribute values e.g. homeaddress=here.",
             "datafile|D|s|Name of file that contains attribute values data.",
             "subschemaname|c|s|Name of sub schema."},
         resourceStrings={
             "schema-add-attribute-defaults-succeed=Schema attribute defaults are added."})
     private String add_attribute_defaults;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.GetAttributeDefaults",
         description="Show default attribute values in schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "subschemaname|c|s|Name of sub schema.",
             "attributenames|a|m|Attribute name(s)."},
         resourceStrings={"schema-get-attribute-defaults-succeed=Schema attribute defaults are returned.",
         "schema-get-attribute-defaults-no-matching-attr=There are no attribute values.",
         "schema-get-attribute-defaults-result={0}={1}"})
     private String show_attribute_defaults;
                                                                                 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.SetAttributeDefaults",
         description="Set default attribute values in schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "subschemaname|c|s|Name of sub schema.",
             "attributevalues|a|m|Attribute values e.g. homeaddress=here.",
             "datafile|D|s|Name of file that contains attribute values data."},
         resourceStrings={
             "schema-set-attribute-defaults-succeed=Schema attribute defaults are set."})
     private String set_attribute_defaults;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.SetAttributeSchemaChoiceValues",
         description="Set choice values of attribute schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema.",
             "attributename|a|s|Name of attribute."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "add|p|u|Set this flag to append the choice values to existing ones.",
             "subschemaname|c|s|Name of sub schema.",
             "datafile|D|s|Name of file that contains attribute values data.",
             "choicevalues|k|m|Choice value e.g. o102=Inactive."},
         resourceStrings={
             "attribute-schema-set-choice-value-succeed=Choice Values are set."})
     private String set_attribute_choice_values;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.RemoveAttributeSchemaChoiceValues",
         description="Remove choice values from attribute schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema.",
             "attributename|a|s|Name of attribute.",
             "choicevalues|k|m|Choice values e.g. Inactive"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "subschemaname|c|s|Name of sub schema."},
         resourceStrings={
             "attribute-schema-remove-choice-value-succeed=Choice Values are removed."})
     private String remove_attribute_choice_values;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.cli.schema.SetAttributeSchemaBooleanValues",
         description="Set boolean values of attribute schema.",
         mandatoryOptions={
             "servicename|s|s|Name of service.",
             "schematype|t|s|Type of schema.",
             "attributename|a|s|Name of attribute.",
             "truevalue|e|s|Value for true.",
             "truei18nkey|k|s|Internationalization key for true value.",
             "falsevalue|z|s|Value for false.",
             "falsei18nkey|j|s|Internationalization key for false value."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "subschemaname|c|s|Name of sub schema."},
         resourceStrings={
             "attribute-schema-set-boolean-values-succeed=Boolean Values are set."})
     private String set_attribute_boolean_values;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.CreateMetaDataTemplate",
         description="Create new metadata template.",
         mandatoryOptions={
             "entityid|e|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "metadata|m|s|c|Specify file name for the standard metadata to be created.",
             "extended|x|s|c|Specify file name for the standard metadata to be created.",
             "serviceprovider|s|s|Specify metaAlias for hosted service provider to be created. The format must be <realm>/<unique string without /> for non-root realm or /<unique string without /> for root realm.",
             "identityprovider|i|s|Specify metaAlias for hosted identity provider to be created. The format must be <realm>/<unique string without /> for non-root realm or /<unique string without /> for root realm.",
             "spscertalias|a|s|Service provider signing certificate alias",
             "idpscertalias|b|s|Identity provider signing certificate alias",
             "specertalias|r|s|Service provider encryption certificate alias",
             "idpecertalias|g|s|Identity provider encryption certificate alias.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "create-meta-template-exception-idp-sp-null=Identity or Service Provider are required.",
             "create-meta-template-exception-idp-null-with-cert-alias=Identity Provider Certificate Alias is provided with Identity Provider Name.",
             "create-meta-template-exception-dp-null-with-cert-alias=Service Provider Certificate Alias is provided with Service Provider Name.",
             "create-meta-template-exception-protocol-not-found=Protocol is not found in configuration file.",
             "create-meta-template-exception-host-not-found=Host is not found in configuration file.",
             "create-meta-template-exception-port-not-found=Port is not found in configuration file.",
             "create-meta-template-exception-deploymentURI-not-found=Deployment URI is not found in configuration file.",
             "create-meta-template-created-descriptor-template=Hosted entity descriptor for realm, {1} was written to file, {0}.",
             "create-meta-template-created-configuration-template=Hosted entity configuration for realm, {1} was written to file, {0}."})
     private String create_metadata_template;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ImportMetaData",
         description="Import entity.",
         mandatoryOptions={},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "metadata|m|s|t|Specify file name for the standard metadata to be imported.",
             "extended|x|s|t|Specify file name for the extended entity configuration to be imported.",
             "cot|t|s|Specify name of the Circle of Trust this entity belongs.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "import-entity-exception-no-datafile=metadata or extended data file is required.",
             "import-entity-exception-invalid-descriptor-file=Entity descriptor in file, {0} has invalid syntax.",
             "import-entity-succeeded=Import file, {0}.",
             "import-entity-exception-invalid-config-file=Entity config in file, {0} has invalid syntax."})
     private String import_entity;
     
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ExportMetaData",
         description="Export entity.",
         mandatoryOptions={
             "entityid|e|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where data resides",
             "sign|g|u|Set this flag to sign the metadata",
             "metadata|m|s|c|Metadata",
             "extended|x|s|c|Extended data",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "export-entity-exception-no-datafile=Missing export files, metadata or extended option needs to be set.",
             "export-entity-exception-entity-descriptor-not-exist=Entity descriptor, {0} under realm, {1} does not exist.",
             "export-entity-exception-invalid_descriptor=Entity descriptor, {0} under realm, {1} has invalid syntax.",
             "export-entity-export-descriptor-succeeded=Entity descriptor was exported to file, {0}.",
             "export-entity-export-config-succeeded=Entity configuration was exported to file, {0}.",
             "export-entity-exception-entity-config-not-exist=Entity configuration, {0} does not exist under realm, {1}",
             "export-entity-exception-invalid-config=Entity configuration, {0} under realm, {1} has invalid syntax."})
     private String export_entity;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.DeleteMetaData",
         description="Delete entity.",
         mandatoryOptions={
             "entityid|e|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where data resides",
             "extendedonly|x|u|Set to flag to delete only extended data.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "delete-entity-config-deleted=Configuration is deleted for entity, {0}.",
             "delete-entity-descriptor-deleted=Descriptor is deleted for entity, {0}."})
     private String delete_entity;
 
     
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ListEntities",
         description="List entities under a realm.",
         mandatoryOptions={},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where entities reside.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "list-entities-no-entities=There are no entities.",
             "list-entities-entity-listing=List of entity IDs:"})
     private String list_entities;
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.CreateCircleOfTrust",
         description="Create circle of trust.",
         mandatoryOptions={
             "cot|t|s|Circle of Trust"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where circle of trust resides",
             "trustedproviders|k|m|Trusted Providers",
             "prefix|p|s|Prefix URL for idp discovery reader and writer URL.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "create-circle-of-trust-succeeded=Circle of trust, {0} is created."})
     private String create_circle_of_trust;
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.DeleteCircleOfTrust",
         description="Delete circle of trust.",
         mandatoryOptions={
             "cot|t|s|Circle of Trust"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "delete-circle-of-trust-succeeded=Circle of trust, {0} is deleted."})
     private String delete_circle_of_trust;
 
 
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ListCircleOfTrusts",
         description="List circle of trusts.",
         mandatoryOptions={},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where circle of trusts reside",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "list-circle-of-trust-no-members=There are no circle of trusts.",
             "list-circle-of-trust-members=List of circle of trusts:"})
     private String list_circle_of_trusts;
 
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ListCircleOfTrustMembers",
         description="List the members in a circle of trust.",
         mandatoryOptions={
             "cot|t|s|Circle of Trust"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "list-circle-of-trust-members-no-members=There are no trusted entities in the circle of trust, {0}.",
             "list-circle-of-trust-members-members=List of trusted entities (entity IDs) in the circle of trust, {0}:"})
     private String list_circle_of_trust_members;
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.RemoveCircleOfTrustMembers",
         description="Remove a member from a circle of trust.",
         mandatoryOptions={
             "cot|t|s|Circle of Trust",
             "entityid|e|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "remove-circle-of-trust-member-succeeded=Entity, {1} is removed from the circle of trust, {0}."})
     private String remove_circle_of_trust_member;
 @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.AddCircleOfTrustMembers",
         description="Add a member to a circle of trust.",
         mandatoryOptions={
             "cot|t|s|Circle of Trust",
             "entityid|e|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|r|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
            "add-circle-of-trust-member-succeeded=Entity, {1} is added to the circle of trust, {0}."})
     private String add_circle_of_trust_member;
 
 }
