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
 * $Id: FederationManager.java,v 1.13 2007-04-26 18:24:39 veiming Exp $
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
         logName="fmadm",
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
 
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.CreateMetaDataTemplate",
         description="Create new metadata template.",
         webSupport="true",
         mandatoryOptions={
             "entityid|y|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "metadata|m|s|c|Specify file name for the standard metadata to be created.",
             "extended|x|s|c|Specify file name for the standard metadata to be created.",
             "serviceprovider|s|s|Specify metaAlias for hosted service provider to be created. The format must be <realm name>/<identifier>.",
             "identityprovider|i|s|Specify metaAlias for hosted identity provider to be created. The format must be <realm name>/<identifier>.",
             "xacmlpep|e|s|Specify metaAlias for policy enforcement point to be created. The format must be <realm name>/<identifier>.",
             "xacmlpdp|p|s|Specify metaAlias for policy decision point to be created. The format must be <realm name>/<identifier>.",
             "spscertalias|a|s|Service provider signing certificate alias",
             "idpscertalias|b|s|Identity provider signing certificate alias",
             "xacmlpdpscertalias|t|s|Policy decision point signing certificate alias",
             "xacmlpepscertalias|k|s|Policy enforcement point signing certificate alias",
             "specertalias|r|s|Service provider encryption certificate alias",
             "idpecertalias|g|s|Identity provider encryption certificate alias.",
             "xacmlpdpecertalias|j|s|Policy decision point encryption certificate alias",
             "xacmlpepecertalias|z|s|Policy enforcement point encryption certificate alias",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "create-meta-template-exception-role-null=Identity or Service Provider or Policy Enforcement Point or Policy Decision Point are required.",
             "create-meta-template-exception-idp-null-with-cert-alias=Identity Provider Certificate Alias is provided without Identity Provider Name.",
             "create-meta-template-exception-dp-null-with-cert-alias=Service Provider Certificate Alias is provided without Service Provider Name.",
             "create-meta-template-exception-pdp-null-with-cert-alias=Policy Decision Point Certificate Alias is provided without Policy Decision Point Name",
             "create-meta-template-exception-pep-null-with-cert-alias=Policy Enforcement Point Certificate Alias is provided without Policy Enforcement Point Name",
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
         webSupport="true",
         mandatoryOptions={},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where entity resides.",
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
         webSupport="true",
         mandatoryOptions={
             "entityid|y|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where data resides",
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
         webSupport="true",
         mandatoryOptions={
             "entityid|y|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where data resides",
             "extendedonly|x|u|Set to flag to delete only extended data.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "delete-entity-config-deleted=Configuration is deleted for entity, {0}.",
             "delete-entity-descriptor-deleted=Descriptor is deleted for entity, {0}."})
     private String delete_entity;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ListEntities",
         description="List entities under a realm.",
         webSupport="true",
         mandatoryOptions={},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where entities reside.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "list-entities-no-entities=There are no entities.",
             "list-entities-entity-listing=List of entity IDs:"})
     private String list_entities;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.CreateCircleOfTrust",
         description="Create circle of trust.",
         webSupport="true",
         mandatoryOptions={
             "cot|t|s|Circle of Trust"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where circle of trust resides",
             "trustedproviders|k|m|Trusted Providers",
             "prefix|p|s|Prefix URL for idp discovery reader and writer URL.",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "create-circle-of-trust-succeeded=Circle of trust, {0} is created."})
     private String create_circle_of_trust;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.DeleteCircleOfTrust",
         description="Delete circle of trust.",
         webSupport="true",
         mandatoryOptions={
             "cot|t|s|Circle of Trust"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "delete-circle-of-trust-succeeded=Circle of trust, {0} is deleted."})
     private String delete_circle_of_trust;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ListCircleOfTrusts",
         description="List circle of trusts.",
         webSupport="true",
         mandatoryOptions={},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where circle of trusts reside",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "list-circle-of-trust-no-members=There are no circle of trusts.",
             "list-circle-of-trust-members=List of circle of trusts:"})
     private String list_circle_of_trusts;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ListCircleOfTrustMembers",
         description="List the members in a circle of trust.",
         webSupport="true",
         mandatoryOptions={
             "cot|t|s|Circle of Trust"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "list-circle-of-trust-members-no-members=There are no trusted entities in the circle of trust, {0}.",
             "list-circle-of-trust-members-cot-does-not-exists=Circle of trust, {0} does not exist.",
             "list-circle-of-trust-members-members=List of trusted entities (entity IDs) in the circle of trust, {0}:"})
     private String list_circle_of_trust_members;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.RemoveCircleOfTrustMembers",
         description="Remove a member from a circle of trust.",
         webSupport="true",
         mandatoryOptions={
             "cot|t|s|Circle of Trust",
             "entityid|y|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "remove-circle-of-trust-member-succeeded=Entity, {1} is removed from the circle of trust, {0}."})
     private String remove_circle_of_trust_member;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.AddCircleOfTrustMembers",
         description="Add a member to a circle of trust.",
         webSupport="true",
         mandatoryOptions={
             "cot|t|s|Circle of Trust",
             "entityid|y|s|Entity ID"},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "realm|e|s|Realm where circle of trust resides",
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "add-circle-of-trust-member-succeeded=Entity, {2} is added to the circle of trust, {1}, in realm {3}."})
     private String add_circle_of_trust_member;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.BulkFederation",
         description="Perform bulk federation.",
         webSupport="false",
         mandatoryOptions={
             "metaalias|m|s|Specify metaAlias for local provider.",
             "remoteentityid|r|s|Remote entity Id",
             "useridmapping|g|s|File name of local to remote user Id mapping. Format <local-user-id>|<remote-user-id>",
            "nameidmapping|e|s|Name of file that will be created by this sub command. It contains remote user Id to name identifier. It shall be used by remote provider to update user profile."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "bulk-federation-succeeded=Bulk Federation for this host is completed. To complete the federation, name Id mapping file should be loaded to remote provider.",
             "bulk-federation-infile-do-not-exists=User Id Mapping file, {0} does not exist.",
             "bulk-federation-outfile-exists=Name Id mapping file, {0} already exists.",
             "bulk-federation-outfile-cannot-write=Name Id mapping file, {0} is not writable.",
             "bulk-federation-wrong-format=Wrong format, {0} in User Id Mapping file, {1}.",
             "bulk-federation-cannot-generate-name-id=Cannot generate name identifier.",
             "bulk-federation-unknown-metaalias=Meta Alias, {0} is unknown.",
             "bulk-federation-cannot-federate=Cannot federate user, {0}"
             })
     private String do_bulk_federation;
 
     @SubCommandInfo(
         implClassName="com.sun.identity.federation.cli.ImportBulkFederationData",
         description="Import bulk federation data which is generated by 'do-bulk-federation' sub command.",
         webSupport="false",
         mandatoryOptions={
             "metaalias|m|s|Specify metaAlias for local provider.",
             "bulkfeddata|g|s|File name of  bulk federation data which is generated by 'do-bulk-federation' sub command."},
         optionAliases={},
         macro="authentication",
         optionalOptions={
             "spec|c|s|Specify metadata specification, either idff or saml2, defaults to saml2"},
         resourceStrings={
             "import-bulk-federation-data-succeeded=Bulk Federation for this host is completed.",
             "import-bulk-federation-data-unknown-metaalias=Meta Alias, {0} is unknown.",
             "import-bulk-federation-data-incorrect-entity-id=Entity Id in data file does not match with the entity Id of given meta alias.",
             "import-bulk-federation-data-incorrect-file-format=Incorrect file format.",
             "import-bulk-federation-data-incorrect-data-format=Incorrect data format, {0}.",
             "import-bulk-federation-data-incorrect-role=Incorrect role. The role in data file differs from the role of provider metaalias.",
             "import-bulk-federation-data-incorrect-spec=Incorrect specification. The specification in data file differs from the entered specification",
             "import-bulk-federation-data-cannot-federate=Cannot federate user, {0}"
             })
     private String import_bulk_federation_data;
 }
