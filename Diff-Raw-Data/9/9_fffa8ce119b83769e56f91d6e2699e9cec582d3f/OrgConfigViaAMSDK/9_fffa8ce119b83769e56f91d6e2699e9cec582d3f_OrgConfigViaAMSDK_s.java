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
 * $Id: OrgConfigViaAMSDK.java,v 1.5 2006-12-15 00:56:36 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm;
 
 import com.iplanet.am.sdk.AMConstants;
 import com.iplanet.am.sdk.AMException;
 import com.iplanet.am.sdk.AMNamingAttrManager;
 import com.iplanet.am.sdk.AMObject;
 import com.iplanet.am.sdk.AMOrganization;
 import com.iplanet.am.sdk.AMSDKBundle;
 import com.iplanet.am.sdk.AMStoreConnection;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.delegation.DelegationEvaluator;
 import com.sun.identity.delegation.DelegationException;
 import com.sun.identity.delegation.DelegationPermission;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.security.AdminTokenAction;
 import java.security.AccessController;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 // This class provides support for OrganizationConfigManager
 // in coexistence mode. This class interfaces with AMSDK
 // to manage organization names and organization attributes.
 public class OrgConfigViaAMSDK {
 
     // Instance variables
     private SSOToken token;
 
     private String parentOrgName;
 
     private String smsOrgName;
 
     private AMOrganization parentOrg;
 
     private AMOrganization parentOrgWithAdminToken;
 
     private ServiceConfig serviceConfig;
 
     // permissions for the user token
     boolean hasReadPermissionOnly;
 
     // Cache of organization names to ServiceConfig that
     // contains the attribute mappings
     static Map attributeMappings = new CaseInsensitiveHashMap();
 
     static Map reverseAttributeMappings = new CaseInsensitiveHashMap();
 
     static Map attributeMappingServiceConfigs = new HashMap();
 
     static final String IDREPO_SERVICE = "sunidentityrepositoryservice";
 
     static final String MAPPING_ATTR_NAME = "sunCoexistenceAttributeMapping";
 
     // Admin token to perform operations if the user has
     // realm permissions
     private static SSOToken adminToken;
 
     // Debug & Locale
     Debug debug = SMSEntry.debug;
 
     ResourceBundle bundle = SMSEntry.bundle;
 
     // When DIT not migrated to AM 7.0 we need to use static mapping
     static Map notMigratedAttributeMappings;
 
     static Map notMigratedReverseAttributeMappings;
     static {
         if (!ServiceManager.isConfigMigratedTo70()) {
             notMigratedAttributeMappings = new CaseInsensitiveHashMap();
             notMigratedAttributeMappings.put("sunPreferredDomain",
                     "sunPreferredDomain");
             notMigratedAttributeMappings.put("sunOrganizationStatus",
                     "inetDomainStatus");
             notMigratedAttributeMappings.put("sunOrganizationAliases",
                     "sunOrganizationAlias");
             notMigratedAttributeMappings.put("sunDNSAliases",
                     "associatedDomain");
             notMigratedReverseAttributeMappings = new CaseInsensitiveHashMap();
             notMigratedReverseAttributeMappings.put("sunPreferredDomain",
                     "sunPreferredDomain");
             notMigratedReverseAttributeMappings.put("inetDomainStatus",
                     "sunOrganizationStatus");
             notMigratedReverseAttributeMappings.put("sunOrganizationAlias",
                     "sunOrganizationAliases");
             notMigratedReverseAttributeMappings.put("associatedDomain",
                     "sunDNSAliases");
         }
     }
 
     /**
      * Constructor for Realm management via AMSDK The parameter
      * <code>orgName</code> must be LDAP organization name
      */
     OrgConfigViaAMSDK(SSOToken token, String orgName, String smsOrgName)
             throws SMSException {
         this.token = token;
         parentOrgName = orgName;
         this.smsOrgName = smsOrgName;
         // Get admin SSOToken for operations to bypass ACIs and delegation
         if (adminToken == null) {
             adminToken = (SSOToken) AccessController
                     .doPrivileged(AdminTokenAction.getInstance());
         }
         try {
 
             // Check if the user has realm privileges, if yes use
             // admin SSOToken to bypass directory ACIs
             if (checkRealmPermission(token, smsOrgName,
                     SMSEntry.modifyActionSet)) {
                 token = adminToken;
             } else if (checkRealmPermission(token, smsOrgName,
                     SMSEntry.readActionSet)) {
                 hasReadPermissionOnly = true;
             }
             AMStoreConnection amcom = new AMStoreConnection(token);
             parentOrg = amcom.getOrganization(orgName);
 
             if (hasReadPermissionOnly) {
                 // Construct parent org with admin token for reads
                 amcom = new AMStoreConnection(adminToken);
                 parentOrgWithAdminToken = amcom.getOrganization(orgName);
             }
 
             // Get the Realm <---> LDAP Org attribute mappings.
             // To get the service config of idrepo service.
             String newOrg = orgName;
             if (!SMSEntry.baseDN.equalsIgnoreCase(
                 SMSEntry.amsdkbaseDN)) {
                 newOrg = smsOrgName;
             }
 
             if (ServiceManager.isConfigMigratedTo70()
                     && (serviceConfig = (ServiceConfig) 
                             attributeMappingServiceConfigs.get(orgName)) 
                             == null) 
             {
                 ServiceConfigManager scm = new ServiceConfigManager(
                         IDREPO_SERVICE, adminToken);
                 // Do we need to use internal token?
                 serviceConfig = scm.getOrganizationConfig(newOrg, null);
                 if (debug.messageEnabled()) {
                     debug.message("OrgConfigViaAMSDK::constructor"
                             + ": serviceConfig" + serviceConfig);
                 }
                 attributeMappingServiceConfigs.put(orgName, serviceConfig);
             }
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     /**
      * Create a suborganization using AMSDK. The code checks if the DIT has been
      * migrated to AM 7.0 to add the objectclass "sunRelamService".
      */
     void createSubOrganization(String subOrgName) throws SMSException {
         // Check if suborg exists
         if (!getSubOrganizationNames(subOrgName, false).isEmpty()
                 || subOrgName.startsWith(SMSEntry.SUN_INTERNAL_REALM_NAME)) {
             // Sub-org already exists or it is a hidden realm
             return;
         }
 
         // Create the organization
         try {
             if (ServiceManager.isConfigMigratedTo70()) {
                 Map attrs = new HashMap();
                 Set attrValues = new HashSet();
                 attrValues.add(SMSEntry.OC_REALM_SERVICE);
                 attrs.put(SMSEntry.ATTR_OBJECTCLASS, attrValues);
                 Map subOrgs = new HashMap();
                 subOrgs.put(subOrgName, attrs);
                 parentOrg.createSubOrganizations(subOrgs);
             } else {
                 Set subOrgs = new HashSet();
                 subOrgs.add(subOrgName);
                 parentOrg.createSubOrganizations(subOrgs);
             }
         } catch (AMException ame) {
             // Ignore if it is Organization already exists
             if (!ame.getErrorCode().equals("474")) {
                 if (debug.messageEnabled()) {
                     debug.message("OrgConfigViaAMSDK::createSubOrganization"
                             + ": failed with AMException", ame);
                 }
                 throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame
                         .getMessage(), ame, ame.getMessage()));
             }
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     /**
      * Returns the set of assigned services for the organization
      */
     Set getAssignedServices() throws SMSException {
         try {
             if (hasReadPermissionOnly) {
                 return (parentOrgWithAdminToken.getRegisteredServiceNames());
             } else {
                 return (parentOrg.getRegisteredServiceNames());
             }
         } catch (AMException ame) {
             if (debug.messageEnabled()) {
                 debug.message("OrgConfigViaAMSDK::getAssignedServices"
                         + ": failed with AMException", ame);
             }
             throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame.getMessage(),
                     ame, ame.getMessage()));
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     /**
      * Assigns the service to the organization
      */
     void assignService(String serviceName) throws SMSException {
         try {
             // Check if service is already assigned
             if (!getAssignedServices().contains(serviceName)) {
                 parentOrg.registerService(serviceName, false, false);
             }
         } catch (AMException ame) {
             if (debug.messageEnabled()) {
                 debug.message("OrgConfigViaAMSDK::assignService"
                         + ": failed with AMException", ame);
             }
             throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame.getMessage(),
                     ame, ame.getMessage()));
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     /**
      * Unassigns the service from the organization
      */
     void unassignService(String serviceName) throws SMSException {
         try {
             // Check if service is already unassigned
             if (getAssignedServices().contains(serviceName)) {
                 parentOrg.unregisterService(serviceName);
             }
         } catch (AMException ame) {
             if (debug.messageEnabled()) {
                 debug.message("OrgConfigViaAMSDK::unassignService"
                         + ": failed with AMException", ame);
             }
             throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame.getMessage(),
                     ame, ame.getMessage()));
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     /**
      * Returns sub-organization names using AMSKK APIs. The returned names are
      * in "/" separated format and are normailized using DNMapper.
      */
     Set getSubOrganizationNames(String pattern, boolean recursive)
             throws SMSException {
         try {
             // Search for sub-organization names
             Set subOrgDNs;
             if (hasReadPermissionOnly) {
                 subOrgDNs = parentOrgWithAdminToken.searchSubOrganizations(
                         pattern, recursive ? AMConstants.SCOPE_SUB
                                 : AMConstants.SCOPE_ONE);
             } else {
                 subOrgDNs = parentOrg.searchSubOrganizations(pattern,
                         recursive ? AMConstants.SCOPE_SUB
                                 : AMConstants.SCOPE_ONE);
             }
             // Convert DNs to "/" seperated relam names
             if (subOrgDNs != null && !subOrgDNs.isEmpty()) {
                 Set subOrgs = new HashSet();
                 for (Iterator items = subOrgDNs.iterator(); items.hasNext();) {
                     subOrgs.add(DNMapper.orgNameToDN((String) items.next()));
                 }
                 return SMSEntry.parseResult(subOrgs, smsOrgName);
             }
         } catch (AMException ame) {
             if (debug.messageEnabled()) {
                 debug.message("OrgConfigViaAMSDK::getSubOrganizationNames"
                         + ": failed with AMException", ame);
             }
             throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame.getMessage(),
                     ame, ame.getMessage()));
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
         return (Collections.EMPTY_SET);
     }
 
     /**
      * Deletes sub-organiation using AMSDK. If recursive flag is set, then all
      * sub-entries are also removed. Else if sub-entries are present this will
      * throw an exception.
      */
     void deleteSubOrganization(String subOrgName) throws SMSException {
         try {
             // Check if subOrgName is empty or null
             if (subOrgName == null || subOrgName.trim().length() == 0) {
                 if (parentOrg.isExists()) {
                     parentOrg.delete(true);
                 }
                 return;
             }
 
             // Check if it is a hidden realm
             if (subOrgName.startsWith(SMSEntry.SUN_INTERNAL_REALM_NAME)) {
                 return;
             }
 
             // Get the suborg DN
             Set subOrgDNs = parentOrg.searchSubOrganizations(subOrgName,
                     AMConstants.SCOPE_ONE);
             if (subOrgDNs != null && !subOrgDNs.isEmpty()) {
                 for (Iterator items = subOrgDNs.iterator(); items.hasNext();) {
                     String dn = (String) items.next();
                     AMOrganization subOrg = parentOrg.getSubOrganization(dn);
                     if (subOrg != null) {
                         subOrg.delete(true);
                     }
                 }
             } else {
                 AMOrganization subOrg = parentOrg
                         .getSubOrganization(subOrgName);
                 if (subOrg != null) {
                     subOrg.delete(true);
                 }
             }
         } catch (AMException ame) {
             if (debug.messageEnabled()) {
                 debug.message("OrgConfigViaAMSDK::deleteSubOrganization"
                         + ": failed with AMException", ame);
             }
             throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame.getMessage(),
                     ame, ame.getMessage()));
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     /**
      * Returns the AMSDK Organization attributes. The return attributes are
      * defined in the IdRepo service and can be configured per organization.
      */
     Map getAttributes() throws SMSException {
         Map answer = null;
         try {
             // Get the list of attribute names
             Map attrMapping = getReverseAttributeMapping();
             Set attrNames = attrMapping.keySet();
             if (!attrNames.isEmpty()) {
                 // Perform AMSDK search
                 Map attributes;
                 if (hasReadPermissionOnly) {
                     attributes = parentOrgWithAdminToken
                             .getAttributes(attrNames);
                 } else {
                     attributes = parentOrg.getAttributes(attrNames);
                 }
                 if (attributes != null && !attributes.isEmpty()) {
                     // Do reverse name mapping, and copy to answer
                     for (Iterator items = attributes.keySet().iterator(); items
                             .hasNext();) {
                         String key = (String) items.next();
                         Set values = (Set) attributes.get(key);
                         if (values != null && !values.isEmpty()) {
                             if (answer == null) {
                                 answer = new HashMap();
                             }
                             answer.put(attrMapping.get(key), values);
                         }
                     }
                 }
             }
         } catch (AMException ame) {
             if (debug.messageEnabled()) {
                 debug.message("OrgConfigViaAMSDK::getAttributes"
                         + ": failed with AMException", ame);
             }
             throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame.getMessage(),
                     ame, ame.getMessage()));
         } catch (SSOException ssoe) {
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     ssoe, "sms-INVALID_SSO_TOKEN"));
         }
         return (answer == null ? Collections.EMPTY_MAP : answer);
     }
 
     /**
      * Adds attributes to AMSDK Organization. The organziation attribute names
      * are defined in the IdRepo service.
      */
     void addAttributeValues(String attrName, Set values) throws SMSException {
         // Get the attribute values, add the new values
         // and set the attribute
         if (attrName != null && values != null && !values.isEmpty()) {
             // First get the attribute values, remove the
             // specified valued and then set the attributes
             Map attrs = getAttributes();
             Set origValues = (Set) attrs.get(attrName);
             Set newValues = new HashSet(values);
             if (origValues != null && !origValues.isEmpty()) {
                 newValues.addAll(origValues);
             }
             Map newAttrs = new HashMap();
             newAttrs.put(attrName, newValues);
             setAttributes(newAttrs);
         }
     }
 
     /**
      * Sets attributes to AMSDK Organization. The organziation attribute names
      * are defined in the IdRepo service.
      */
     void setAttributes(Map attributes) throws SMSException {
         Map amsdkAttrs = null;
         // Need to get attributes such as domain name, alias names
         // and org status from attributes and set them.
         // These attributes must be defined in ../idm/xml/idRepoService.xml
         if (attributes != null && !attributes.isEmpty()) {
             Map smsIdRepoAttrs = new CaseInsensitiveHashMap(attributes);
             // Iterate through the attribute mappings
             Map attrs = getAttributeMapping();
             Map existingAttributes = getAttributes();
             if (attrs != null && !attrs.isEmpty()) {
                 for (Iterator items = attrs.keySet().iterator(); items
                         .hasNext();) {
                     String key = (String) items.next();
                     Set value = (Set) smsIdRepoAttrs.get(key);
                     if (value != null) {
                         if (amsdkAttrs == null) {
                             amsdkAttrs = new HashMap();
                         }
                         boolean notEmptyFlg = false;
                         if (!value.isEmpty()) {
                             for (Iterator iter = value.iterator(); iter
                                     .hasNext();) {
                                 String val = (String) iter.next();
                                 // Avoid empty string storage.
                                 if (val.length() > 0) {
                                     notEmptyFlg = true;
                                 }
                             }
                             if (notEmptyFlg) {
                                 amsdkAttrs.put(attrs.get(key), value);
                             }
                         } else {
                             Set existingValues = (Set) existingAttributes
                                     .get(key);
                             if (existingValues != null
                                     && !existingValues.isEmpty()) {
                                 amsdkAttrs.put(attrs.get(key), value);
                             }
                         }
                     }
                 }
             }
         }
 
         // Update the organization entry
         if (amsdkAttrs != null) {
             try {
                 parentOrg.setAttributes(amsdkAttrs);
                 parentOrg.store();
             } catch (AMException ame) {
                 if (debug.messageEnabled()) {
                     debug.message("OrgConfigViaAMSDK::createSub"
                             + "Organization: failed with AMException", ame);
                 }
                 throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame
                         .getMessage(), ame, ame.getMessage()));
             } catch (SSOException ssoe) {
                 throw (new SMSException(bundle
                         .getString("sms-INVALID_SSO_TOKEN"), ssoe,
                         "sms-INVALID_SSO_TOKEN"));
             }
         }
     }
 
     /**
      * Removes the specified attribute from AMSDK organization. The organziation
      * attribute names are defined in the IdRepo service.
      */
     void removeAttribute(String attrName) throws SMSException {
         if (attrName == null) {
             return;
         }
 
         // Get the attribute mapping and removed specified attribute
         Map attrMap = getAttributeMapping();
         String amsdkAttrName = (String) attrMap.get(attrName);
         if (amsdkAttrName != null) {
             HashSet set = new HashSet();
             set.add(amsdkAttrName);
             try {
                 parentOrg.removeAttributes(set);
                 parentOrg.store();
             } catch (AMException ame) {
                 if (debug.messageEnabled()) {
                     debug.message("OrgConfigViaAMSDK::removeAttribute"
                             + ": failed with AMException", ame);
                 }
                 throw (new SMSException(AMSDKBundle.BUNDLE_NAME, ame
                         .getMessage(), ame, ame.getMessage()));
             } catch (SSOException ssoe) {
                 throw (new SMSException(bundle
                         .getString("sms-INVALID_SSO_TOKEN"), ssoe,
                         "sms-INVALID_SSO_TOKEN"));
             }
         }
     }
 
     /**
      * Removes the specified attribute values from AMSDK organization. The
      * organziation attribute names are defined in the IdRepo service.
      */
     void removeAttributeValues(String attrName, Set values) throws SMSException 
     {
         if (attrName != null) {
             // First get the attribute values, remove the
             // specified valued and then set the attributes
             Map attrs = getAttributes();
             Set origValues = (Set) attrs.get(attrName);
             if (origValues != null && !origValues.isEmpty()) {
                 Set newValues = new HashSet(origValues);
                 newValues.removeAll(values);
                 if (newValues.isEmpty()) {
                     removeAttribute(attrName);
                 } else {
                     Map newAttrs = new HashMap();
                     newAttrs.put(attrName, newValues);
                     setAttributes(newAttrs);
                 }
             }
         }
     }
 
     /**
      * Returns the SMS attribute name to AMSDK attribute name mappings for the
      * organization
      */
     private Map getAttributeMapping() throws SMSException {
         if (!ServiceManager.isConfigMigratedTo70()) {
             return (notMigratedAttributeMappings);
         }
         // Check the cache
         Map answer = (Map) attributeMappings.get(parentOrgName);
         if (answer != null)
             return (answer);
 
         // Construct the attribute mappings
         Map attrs = serviceConfig.getAttributes();
         if (attrs != null && !attrs.isEmpty()) {
             Set mapAttrs = (Set) attrs.get(MAPPING_ATTR_NAME);
             if (mapAttrs != null && !mapAttrs.isEmpty()) {
                 for (Iterator items = mapAttrs.iterator(); items.hasNext();) {
                     String attrMapping = (String) items.next();
                     String[] maps = DNMapper.splitString(attrMapping);
                     if (answer == null) {
                         answer = new CaseInsensitiveHashMap();
                     }
                     answer.put(maps[0], maps[1]);
                 }
             }
         }
         if (answer == null) {
             answer = Collections.EMPTY_MAP;
         }
         // Add to cache
         attributeMappings.put(parentOrgName, answer);
         return (answer);
     }
 
     /**
      * Returns the AMSDK attribute name to SMS attribute name mappings for the
      * organization
      */
     private Map getReverseAttributeMapping() throws SMSException {
         if (!ServiceManager.isConfigMigratedTo70()) {
             return (notMigratedReverseAttributeMappings);
         }
         // Check the cache
         Map answer = (Map) reverseAttributeMappings.get(parentOrgName);
         if (answer != null)
             return (answer);
 
         // Get the attribute mapping and reverse it
         Map attrMaps = getAttributeMapping();
         for (Iterator items = attrMaps.entrySet().iterator(); items.hasNext();) 
         {
             Map.Entry entry = (Map.Entry) items.next();
             if (answer == null) {
                 answer = new CaseInsensitiveHashMap();
             }
             answer.put(entry.getValue(), entry.getKey().toString());
         }
         if (answer == null) {
             answer = Collections.EMPTY_MAP;
         }
         reverseAttributeMappings.put(parentOrgName, answer);
         return (answer);
     }
 
     // Check to see if the user has realm permissions
     private boolean checkRealmPermission(SSOToken token, String realm,
             Set action) {
         boolean answer = false;
         if (token != null) {
             try {
                 DelegationEvaluator de = new DelegationEvaluator();
                 DelegationPermission dp = new DelegationPermission(realm,
                         com.sun.identity.sm.SMSEntry.REALM_SERVICE, "1.0", "*",
                         "*", action, Collections.EMPTY_MAP);
                 answer = de.isAllowed(token, dp, null);
             } catch (DelegationException dex) {
                 debug.error("OrgConfigViaAMSDK.checkRealmPermission: "
                         + "Got Delegation Exception: ", dex);
             } catch (SSOException ssoe) {
                 if (debug.messageEnabled()) {
                     debug.message("OrgConfigViaAMSDK.checkRealmPermission: "
                             + "Invalid SSOToken: ", ssoe);
                 }
             }
         }
         return (answer);
     }
 
     static String getNamingAttrForOrg() {
         return AMNamingAttrManager.getNamingAttr(AMObject.ORGANIZATION);
     }
 
     static String getNamingAttrForOrgUnit() {
         return AMNamingAttrManager.getNamingAttr(AMObject.ORGANIZATIONAL_UNIT);
     }
 
     public Set getSDKAttributeValue(String key) {
         Set attrSet = new HashSet();
         try {
             attrSet = parentOrg.getAttribute(key);
         } catch (AMException ame) {
             if (debug.warningEnabled()) {
                 debug.warning("OrgConfigViaAMSDK::getSDKAttributeValue"
                         + ": failed with AMException", ame);
             }
         } catch (SSOException ssoe) {
             if (debug.warningEnabled()) {
                 debug.warning("OrgConfigViaAMSDK::getSDKAttributeValue"
                         + ": failed with SSOException", ssoe);
             }
         }
         return (attrSet);
     }
 }
