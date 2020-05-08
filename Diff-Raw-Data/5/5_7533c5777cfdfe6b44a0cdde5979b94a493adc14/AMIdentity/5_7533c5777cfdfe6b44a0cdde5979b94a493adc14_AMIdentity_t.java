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
 * $Id: AMIdentity.java,v 1.4 2006-03-22 07:44:05 veiming Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idm;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import netscape.ldap.util.DN;
 
 import com.iplanet.am.sdk.AMCommonUtils;
 import com.iplanet.am.sdk.AMCrypt;
 import com.iplanet.am.sdk.AMDirectoryManager;
 import com.iplanet.am.sdk.AMDirectoryWrapper;
 import com.iplanet.am.sdk.AMHashMap;
 import com.iplanet.am.sdk.AMServiceUtils;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 
 /**
  * This class represents an Identity which needs to be managed by Access
  * Manager. This identity could exist in multiple repositories, which are
  * configured for a given realm or organization. When any operation is performed
  * from this class, it executes all plugins that are configured for performing
  * that operation. For eg: getAttributes. This class does not have public
  * constructors. The application gets access to <code> AMIdentity </code>
  * objects by using <code> AMIdentityRepository 
  * </code> interfaces. For example:
  * <p>
  * 
  * <PRE>
  * 
  * AMIdentityRepository idrepo = new AMIdentityRepository(token, org);
  * AMIdentity id = idrepo.getIdentity();
  * 
  * </PRE>
  * 
  * The <code>id</code> returned above is the AMIdentity object of the user's
  * single sign-on token passed above. The results obtained from search performed
  * using AMIdentityRepository also return AMIdentity objects. The type of an
  * object can be determined by doing the following:
  * <p>
  * 
  * <PRE>
  * 
  * IdType type = identity.getType();
  * 
  * </PRE>
  * 
  * The name of an object can be determined by:
  * <p>
  * 
  * <PRE>
  * 
  * String name = identity.getName();
  * 
  * </PRE>
  */
 
 public final class AMIdentity {
 
     private String univId;
 
     private SSOToken token;
 
     private String name;
 
     private IdType type;
 
     private String orgName;
 
     private AMHashMap modMap = new AMHashMap(false);
 
     private AMHashMap binaryModMap = new AMHashMap(true);
 
     protected String DN = null;
 
     /**
      * Protected constructor for AMIdentity.
      * 
      * @param ssotoken
      * @param universalId
      */
     protected AMIdentity(SSOToken ssotoken, String universalId) {
         univId = universalId;
         DN dnObject = new DN(univId);
         String[] array = dnObject.explodeDN(true);
         name = array[0];
         type = new IdType(array[1]);
         orgName = dnObject.getParent().getParent().toRFCString();
         token = ssotoken;
     }
 
     public AMIdentity(SSOToken token, String name, IdType type, String orgName,
             String amsdkdn) {
         this.name = name;
         this.type = type;
         this.orgName = orgName;
         this.token = token;
         this.DN = amsdkdn;
         if (amsdkdn == null) {
             univId = "id=" + name + ",ou=" + type.getName() + "," + orgName;
         } else {
             univId = "id=" + name + ",ou=" + type.getName() + "," + orgName
                     + ",amsdkdn=" + amsdkdn;
             // ",amsdkdn=" + amsdkdn.replace(',','_');
         }
     }
 
     // General APIs
     /**
      * 
      * Returns the name of the identity.
      * 
      * @return Name of the identity.
      *
      * @supported.api
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns the Type of the Identity.
      * 
      * @return IdType representing the type of this object.
      *
      * @supported.api
      */
     public IdType getType() {
         return type;
     }
 
     /**
      * Returns the realm for this identity.
      * 
      * @return String representing realm name.
      */
     public String getRealm() {
         return orgName;
     }
 
     /**
      * If there is a status attribute configured, then verifies if the identity
      * is active and returns true.
      * 
      * @return true if the identity is active or if it is not configured for a
      *         status attribute, flase otherwise.
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public boolean isActive() throws IdRepoException, SSOException {
 
         AMDirectoryManager dm = AMDirectoryWrapper.getInstance();
         return dm.isActive(token, type, name, orgName, DN);
     }
 
     /**
      * Returns all attributes and values of this identity.
      * 
      * @return Map of attribute-values
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Map getAttributes() throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.getAttributes(token, type, name, orgName, DN);
     }
 
     /**
      * Returns requested attributes and values of this object.
      * 
      * @param attrNames
      *            Set of attribute names to be read
      * @return Map of attribute-values.
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Map getAttributes(Set attrNames) throws IdRepoException,
             SSOException {
         AMDirectoryManager dm = AMDirectoryWrapper.getInstance();
         return dm
                 .getAttributes(token, type, name, attrNames, orgName, DN, true);
     }
 
     /**
      * Returns requested attributes and values of this object.
      * 
      * @param attrNames
      *            Set of attribute names to be read
      * @return Map of attribute-values.
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Map getBinaryAttributes(Set attrNames) throws IdRepoException,
             SSOException {
         AMDirectoryManager dm = AMDirectoryWrapper.getInstance();
         return dm.getAttributes(token, type, name, attrNames, orgName, DN,
                 false);
 
     }
 
     /**
      * Returns the values of the requested attribute. Returns an empty set, if
      * the attribute is not set in the object.
      * 
      * @param attrName
      *            Name of attribute
      * @return Set of attribute values.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Set getAttribute(String attrName) throws IdRepoException,
             SSOException {
         Set attrNames = new HashSet();
         attrNames.add(attrName);
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Map valMap = amdm.getAttributes(token, type, name, attrNames, orgName,
                 DN, true);
         return ((Set) valMap.get(attrName));
     }
 
     /**
      * Set the values of attributes. This method should be followed by the
      * method "store" to commit the changes to the Repository
      * 
      * @param attrMap
      *            Map of attribute-values to be set in the repository or
      *            repositories (if multiple plugins are configured for "edit").
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void setAttributes(Map attrMap) throws IdRepoException, SSOException 
     {
         modMap.copy(attrMap);
     }
 
     /**
      * Set the values of binary attributes. This method should be followed by
      * the method "store" to commit the changes to the Repository
      * 
      * @param attrMap
      *            Map of attribute-values to be set in the repository or
      *            repositories (if multiple plugins are configured for "edit").
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void setBinaryAttributes(Map attrMap) throws IdRepoException,
             SSOException {
         binaryModMap.copy(attrMap);
     }
 
     /**
      * Removes the attributes from the identity entry. This method should be
      * followed by a "store" to commit the changes to the Repository.
      * 
      * @param attrNames
      *            Set of attribute names to be removed
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If the user's single sign on token is invalid
      *
      * @supported.api
      */
     public void removeAttributes(Set attrNames) throws IdRepoException,
             SSOException {
         if (attrNames == null || attrNames.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "201", null);
         }
 
         Iterator it = attrNames.iterator();
         while (it.hasNext()) {
             String attr = (String) it.next();
             modMap.put(attr, Collections.EMPTY_SET);
         }
     }
 
     /**
      * Stores the attributes of the object.
      * 
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void store() throws IdRepoException, SSOException {
         AMDirectoryManager dm = AMDirectoryWrapper.getInstance();
         if (modMap != null && !modMap.isEmpty()) {
             dm.setAttributes(token, type, name, modMap, false, orgName, DN,
                     true);
             modMap.clear();
         }
         if (binaryModMap != null && !binaryModMap.isEmpty()) {
             dm.setAttributes(token, type, name, binaryModMap, false, orgName,
                     DN, false);
             binaryModMap.clear();
         }
     }
 
     // SERVICE RELATED APIS
 
     /**
      * Returns the set of services already assigned to this identity.
      * 
      * @return Set of serviceNames
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Set getAssignedServices() throws IdRepoException, SSOException {
         // Get all service names for the type from SMS
         ServiceManager sm;
         try {
             sm = new ServiceManager(token);
         } catch (SMSException smse) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "106", null);
         }
         Map sMap = sm.getServiceNamesAndOCs(type.getName());
 
         // Get the list of assigned services
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set assigned = Collections.EMPTY_SET;
         try {
             assigned = amdm.getAssignedServices(token, type, name, sMap,
                     orgName, DN);
         } catch (IdRepoException ide) {
             // Check if this is permission denied exception
             if (!ide.getErrorCode().equals("402")) {
                 throw (ide);
             }
         }
         return (assigned);
     }
 
     /**
      * Returns all services which can be assigned to this entity.
      * 
      * @return Set of service names
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Set getAssignableServices() throws IdRepoException, SSOException {
         // Get all service names for the type from SMS
         ServiceManager sm;
         try {
             sm = new ServiceManager(token);
         } catch (SMSException smse) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "106", null);
         }
         Map sMap = sm.getServiceNamesAndOCs(type.getName());
 
         // Get the list of assigned services
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set assigned = Collections.EMPTY_SET;
         try {
             assigned = amdm.getAssignedServices(token, type, name, sMap,
                     orgName, DN);
         } catch (IdRepoException ide) {
             // Check if this is permission denied exception
             if (!ide.getErrorCode().equals("402")) {
                 throw (ide);
             } else {
                 // Return the empty set
                 return (assigned);
             }
         }
 
         // Return the difference
         Set keys = sMap.keySet();
         keys.removeAll(assigned);
         return (keys);
 
     }
 
     /**
      * Assigns the service and service related attributes to the identity.
      * 
      * @param serviceName
      *            Name of service to be assigned.
      * @param attributes
      *            Map of attribute-values
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void assignService(String serviceName, Map attributes)
             throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set OCs = getServiceOCs(token, serviceName);
         SchemaType stype;
         Map tMap = new HashMap();
         tMap.put(serviceName, OCs);
         Set assignedServices = amdm.getAssignedServices(token, type, name,
                 tMap, orgName, DN);
 
         if (assignedServices.contains(serviceName)) {
             Object args[] = { serviceName, type.getName() };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "105", args);
         }
 
         // Set oldOCs = getAttribute("objectclass");
         // OCs = AMCommonUtils.combineOCs(OCs, oldOCs);
 
         // Validate the service attributes
         try {
             ServiceSchemaManager ssm = new ServiceSchemaManager(serviceName,
                     token);
             ServiceSchema ss = ssm.getSchema(type.getName());
 
             if (ss != null) {
                 attributes = ss.validateAndInheritDefaults(attributes, orgName,
                         true);
                 attributes = AMCommonUtils.removeEmptyValues(attributes);
                 stype = ss.getServiceType();
             } else {
                 ss = ssm.getSchema(SchemaType.DYNAMIC);
                 if (ss == null) {
                     Object args[] = { serviceName };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "102",
                             args);
                 }
                 if (attributes == null) {
                     try {
                         attributes = AMServiceUtils.getServiceConfig(token,
                                 serviceName, SchemaType.DYNAMIC);
                     } catch (SMSException smsex) {
                         Object args[] = { serviceName, type.getName() };
                         throw new IdRepoException(IdRepoBundle.BUNDLE_NAME,
                                 "451", args);
                     }
                 } else {
                     attributes = ss.validateAndInheritDefaults(attributes,
                             orgName, true);
                 }
                 attributes = AMCommonUtils.removeEmptyValues(attributes);
                 stype = SchemaType.DYNAMIC;
             }
             attributes = AMCrypt.encryptPasswords(attributes, ss);
         } catch (SMSException smse) {
             // debug.error here
             Object[] args = { serviceName };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "101", args);
         }
 
         attributes.put("objectclass", OCs);
         // The protocol for params is to pass the
         // name of the service, and attribute Map containing the
         // OCs to be set and validated attribute map
         amdm.assignService(token, type, name, serviceName, stype, attributes,
                 orgName, DN);
     }
 
     /**
      * Removes a service from the identity.
      * 
      * @param serviceName
      *            Name of service to be removed.
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void unassignService(String serviceName) throws IdRepoException,
             SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set OCs = getServiceOCs(token, serviceName);
         Map tMap = new HashMap();
         tMap.put(serviceName, OCs);
         Set assignedServices = amdm.getAssignedServices(token, type, name,
                 tMap, orgName, DN);
 
         if (!assignedServices.contains(serviceName)) {
             Object args[] = { serviceName };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "101", args);
         }
 
         Map attrMap = new HashMap();
         Set objectclasses = getAttribute("objectclass");
         if (objectclasses != null && !objectclasses.isEmpty()) {
             Set removeOCs = AMCommonUtils.updateAndGetRemovableOCs(
                     objectclasses, OCs);
 
             try {
                 // Get attribute names for USER type only, so plugin knows
                 // what attributes to remove.
                 Set attrNames = new HashSet();
                 ServiceSchemaManager ssm = new ServiceSchemaManager(
                         serviceName, token);
                 ServiceSchema uss = ssm.getSchema(type.getName());
 
                 if (uss != null) {
                     attrNames = uss.getAttributeSchemaNames();
                 }
 
                 Iterator it = attrNames.iterator();
                 while (it.hasNext()) {
                     String a = (String) it.next();
                     attrMap.put(a, Collections.EMPTY_SET);
                 }
             } catch (SMSException smse) {
                 /*
                  * debug.error( "AMIdentity.unassignService: Caught SM
                  * exception", smse); do nothing
                  */
             }
 
             attrMap.put("objectclass", removeOCs);
         }
         // The protocol is to pass service Name and Map of objectclasses
         // to be removed from entry.
         amdm.unassignService(token, type, name, serviceName, attrMap, orgName,
                 DN);
     }
 
     /**
      * Returns attributes related to a service, if the service is assigned to
      * the identity.
      * 
      * @param serviceName
      *            Name of the service.
      * @return Map of attribute-values.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Map getServiceAttributes(String serviceName) throws IdRepoException,
             SSOException {
         Set attrNames = Collections.EMPTY_SET;
 
         try {
             // Get attribute names for USER type only, so plugin knows
             // what attributes to remove.
             attrNames = new HashSet();
             ServiceSchemaManager ssm = new ServiceSchemaManager(serviceName,
                     token);
             ServiceSchema uss = ssm.getSchema(type.getName());
 
             if (uss != null) {
                 attrNames = uss.getAttributeSchemaNames();
             }
             uss = ssm.getDynamicSchema();
             if (uss != null) {
                 attrNames.addAll(uss.getAttributeSchemaNames());
             }
         } catch (SMSException smse) {
             /*
              * debug.error( "AMIdentity.getServiceAttributes: Caught SM
              * exception", smse); do nothing
              */
         }
 
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.getServiceAttributes(token, type, name, serviceName,
                 attrNames, orgName, DN);
     }
 
     /**
      * Set attributes related to a specific service. The assumption is that the
      * service is already assigned to the identity. The attributes for the
      * service are validated against the service schema.
      * 
      * @param serviceName
      *            Name of the service.
      * @param attrMap
      *            Map of attribute-values.
      * @throws IdRepoException
      *             If there are repository related error conditions.
      * @throws SSOException
      *             If user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void modifyService(String serviceName, Map attrMap)
             throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set OCs = getServiceOCs(token, serviceName);
         SchemaType stype;
         Map tMap = new HashMap();
         tMap.put(serviceName, OCs);
         Set assignedServices = amdm.getAssignedServices(token, type, name,
                 tMap, orgName, DN);
         if (!assignedServices.contains(serviceName)) {
             Object args[] = { serviceName };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "101", args);
         }
 
         // modify service attrs
         try {
             ServiceSchemaManager ssm = new ServiceSchemaManager(serviceName,
                     token);
             ServiceSchema ss = ssm.getSchema(type.getName());
             if (ss != null) {
                 attrMap = ss.validateAndInheritDefaults(attrMap, false);
                 stype = ss.getServiceType();
             } else {
                 ss = ssm.getSchema(SchemaType.DYNAMIC);
                 if (ss == null) {
                     Object args[] = { serviceName };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "102",
                             args);
                 } else {
                     attrMap = ss.validateAndInheritDefaults(attrMap, false);
                     stype = SchemaType.DYNAMIC;
                 }
             }
         } catch (SMSException smse) {
             // debug.error
             Object args[] = { serviceName };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "103", args);
         }
         amdm.modifyService(token, type, name, serviceName, stype, attrMap,
                 orgName, DN);
 
     }
 
     // MEMBERSHIP RELATED APIS
     /**
      * Verifies if this identity is a member of the identity being passed.
      * 
      * @param identity
      *            <code>AMIdentity</code> to check membership with
      * @return true if this Identity is a member of the given Identity
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      *
      * @supported.api
      */
     public boolean isMember(AMIdentity identity) throws IdRepoException,
             SSOException {
         boolean ismember = false;
         IdRepoException idException = null;
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         try {
             Set members = amdm.getMemberships(token, getType(), getName(),
                     identity.getType(), orgName, getDN());
             if (members != null && members.contains(identity)) {
                 ismember = true;
             } else if (members != null) {
                 // Check if AM SDK DNs exist for this identity and match
                 String dn = identity.getDN();
                 if (dn != null) {
                     Iterator it = members.iterator();
                     while (it.hasNext()) {
                         AMIdentity id = (AMIdentity) it.next();
                         String mdn = id.getDN();
                         if ((mdn != null) && mdn.equalsIgnoreCase(dn)) {
                             ismember = true;
                             break;
                         }
                     }
                 }
             }
 
             // If membership is still false, check only the UUID
             // without the amsdkdn
             if (!ismember && members != null && !members.isEmpty()) {
                 // Get UUID without amsdkdn for "membership" identity
                 String identityDN = identity.getUniversalId();
                 String amsdkdn = identity.getDN();
                 if (amsdkdn != null) {
                     identityDN = identityDN.substring(0,
                         identityDN.indexOf(amsdkdn) - 9);
                 }
                 // Get UUID without amsdkdn for users memberships
                 Iterator it = members.iterator();
                 while (it.hasNext()) {
                     AMIdentity id = (AMIdentity) it.next();
                     String idDN = id.getUniversalId();
                     String mdn = id.getDN();
                     if (mdn != null) {
                         idDN = idDN.substring(0,
                             idDN.indexOf(mdn) - 9);
                     }
                     if (idDN.equalsIgnoreCase(identityDN)) {
                         ismember = true;
                         break;
                     }
                 }
             }
         } catch (IdRepoException ide) {
             // Save the exception to be used later
             idException = ide;
         }
 
         if (!ismember && identity.getType().equals(IdType.GROUP)) {
             // In the case of groups it is possible that user identity would not
             // membership information. Hence check against the groups
             // For groups use get memebers on the group identity
             try {
                 Set members = amdm.getMembers(token, identity.getType(),
                         identity.getName(), identity.orgName, getType(),
                         identity.getDN());
                 if (members != null && members.contains(this)) {
                     ismember = true;
                 } else if (members != null) {
                     // Check if AM SDK DNs exist for this identity and match
                     String dn = getDN();
                     if (dn != null) {
                         Iterator it = members.iterator();
                         while (it.hasNext()) {
                             AMIdentity id = (AMIdentity) it.next();
                             String mdn = id.getDN();
                             if ((mdn != null) && mdn.equalsIgnoreCase(dn)) {
                                 ismember = true;
                                 break;
                             }
                         }
                     }
                 }
             } catch (Throwable t) {
                 // Ignore the exception
             }
         }
 
         if (idException != null) {
             throw (idException);
         }
         return ismember;
     }
 
     /**
      * If membership is supported then add the new identity as a member.
      * 
      * @param identity
      *            AMIdentity to be added
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void addMember(AMIdentity identity) throws IdRepoException,
             SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set members = new HashSet();
         members.add(identity.getName());
         amdm.modifyMemberShip(token, type, name, members, identity.getType(),
                 IdRepo.ADDMEMBER, orgName);
     }
 
     /**
      * Removes the identity from this identity's membership.
      * 
      * @param identity
      *            AMIdentity to be removed from membership.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void removeMember(AMIdentity identity) throws IdRepoException,
             SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set members = new HashSet();
         members.add(identity.getName());
         amdm.modifyMemberShip(token, type, name, members, identity.getType(),
                 IdRepo.REMOVEMEMBER, orgName);
     }
 
     /**
      * Removes the identities from this identity's membership.
      * 
      * @param identityObjects
      *            Set of AMIdentity objects
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      *
      * @supported.api
      */
     public void removeMembers(Set identityObjects) throws IdRepoException,
             SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         Set members = new HashSet();
         Iterator it = identityObjects.iterator();
 
         while (it.hasNext()) {
             AMIdentity identity = (AMIdentity) it.next();
             members.add(identity.getName());
             amdm.modifyMemberShip(token, type, name, members, identity
                     .getType(), IdRepo.REMOVEMEMBER, orgName);
             members = new HashSet();
         }
     }
 
     /**
      * Return all members of a given identity type of this identity as a Set of
      * AMIdentity objects
      * 
      * @param mtype
      *            Type of identity objects
      * @return Set of AMIdentity objects that are members of this object.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Set getMembers(IdType mtype) throws IdRepoException, SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.getMembers(token, type, name, orgName, mtype, getDN());
     }
 
     /**
      * Returns the set of identities that this identity belongs to.
      * 
      * @param type
      *            Type of member identity.
      * @return Set of AMIdentity objects of the given type that this identity
      *         belongs to.
      * @throws IdRepoException
      *             if there are repository related error conditions.
      * @throws SSOException
      *             if user's single sign on token is invalid.
      *
      * @supported.api
      */
     public Set getMemberships(IdType mtype) throws IdRepoException,
             SSOException {
         AMDirectoryManager amdm = AMDirectoryWrapper.getInstance();
         return amdm.getMemberships(token, type, name, mtype, orgName, getDN());
     }
 
     /**
      * Overrides the default "equal" method.
      *
      * @supported.api
      */
     public boolean equals(Object o) {
         if (o instanceof AMIdentity) {
             AMIdentity compareTo = (AMIdentity) o;
             if (univId.equalsIgnoreCase(compareTo.univId)) {
                 return true;
             } else if (DN != null) {
                 // check if the amsdkdn match
                 String dn = compareTo.getDN();
                 if (dn != null && dn.equalsIgnoreCase(DN)) {
                     return (true);
                 }
             }
         }
         return false;
     }
 
     /**
      * Non-javadoc, non-public methods
      */
     public int hashCode() {
         if (DN != null) {
             return (DN.toLowerCase().hashCode());
         } else {
             return (univId.toLowerCase().hashCode());
         }
     }
 
     /**
      * Nonjavadoc, non-public methods
      * 
      * @return
      */
     public void setDN(String dn) {
         DN = dn;
     }
 
     /**
      * Non-javadoc, non-public method
      * 
      * @return
      */
     public String getDN() {
         return DN;
     }
 
     /**
      * Returns the universal identifier of this object.
      * 
      * @return String representing the universal identifier of this object.
      */
    public String getUniversalId() {
         return univId;
     }
 
     private Set getServiceOCs(SSOToken token, String serviceName)
             throws SSOException {
         Set result = new HashSet();
         try {
             if (AMServiceUtils.serviceHasSubSchema(token, serviceName,
                     SchemaType.GLOBAL)) {
                 Map attrs = AMServiceUtils.getServiceConfig(token, serviceName,
                         SchemaType.GLOBAL);
                 Set vals = (Set) attrs.get("serviceObjectClasses");
 
                 if (vals != null) {
                     result.addAll(vals);
                 }
             }
         } catch (SMSException smsex) {
         }
 
         return result;
     }
 }
