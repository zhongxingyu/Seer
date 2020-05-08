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
 * $Id: AgentsRepo.java,v 1.7 2007-11-08 06:14:52 goodearth Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idm.plugins.internal;
 
 import java.security.AccessController;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 
 import com.iplanet.am.util.AdminUtils;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.common.CaseInsensitiveHashSet;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdOperation;
 import com.sun.identity.idm.IdRepo;
 import com.sun.identity.idm.IdRepoBundle;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdRepoListener;
 import com.sun.identity.idm.IdRepoUnsupportedOpException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.RepoSearchResults;
 import com.sun.identity.security.AdminPasswordAction;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.sm.DNMapper;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceListener;
 import com.sun.identity.sm.ServiceSchemaManager;
 import netscape.ldap.LDAPDN;
 import netscape.ldap.util.DN;
 
 public class AgentsRepo extends IdRepo implements ServiceListener {
     public static final String NAME = 
         "com.sun.identity.idm.plugins.internal.AgentsRepo";
 
     // Status attribute
     private static final String statusAttribute = 
         "sunIdentityServerDeviceStatus";
     private static final String statusActive = "Active";
     private static final String statusInactive = "Inactive";
     private static final String version = "1.0";
     private static final String comma = ",";
     private static final String agentserviceName = IdConstants.AGENT_SERVICE;
     private static final String agentGroupNode = "agentgroup";
     private static final String instancesNode = "ou=Instances,";
     private static final String labeledURI = "labeledURI";
 
     IdRepoListener repoListener = null;
 
     Debug debug = Debug.getInstance("amAgentsRepo");
 
     private Map supportedOps = new HashMap();
 
     ServiceSchemaManager ssm = null;
 
     ServiceConfigManager scm = null;
 
     ServiceConfig orgConfig, agentConfig, agentGroupConfig;
 
     String ssmListenerId, scmListenerId;
 
     // To determine if notification object has been registered for schema
     // changes.
     private static boolean registeredForNotifications;
 
     // Role membership attribute
     private String roleMembershipAttribute = "nsRoleDN";
 
     // Group members attribute
     private String groupMembersAttribute = "memberOfGroup";
 
 
     public AgentsRepo() {
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
         if (debug.messageEnabled()) {
             debug.message(": AgentsRepo adding Listener");
         }
         try {
             ssm = new ServiceSchemaManager(adminToken, agentserviceName, 
                 version);
             scm = new ServiceConfigManager(adminToken, agentserviceName, 
                 version);
                     
             if (!registeredForNotifications) {
                 ssmListenerId = ssm.addListener(this);
                 scmListenerId = scm.addListener(this);
                 registeredForNotifications = true;
             }
         } catch (SMSException smse) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.AgentsRepo: "
                         + "Unable to init ssm and scm due to " + smse);
             }
         } catch (SSOException ssoe) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.AgentsRepo: "
                         + "Unable to init ssm and scm due to " + ssoe);
             }
         }
         
         loadSupportedOps();
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo invoked");
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#addListener(com.iplanet.sso.SSOToken,
      *      com.iplanet.am.sdk.IdRepoListener)
      */
     public int addListener(SSOToken token, IdRepoListener listener)
             throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.addListener().");
         }
         // Listeners are added when AgentsRepo got invoked.
         if (registeredForNotifications) {
             repoListener = listener;
         }
         return 0;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#create(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map)
      */
     public String create(SSOToken token, IdType type, String agentName, 
         Map attrMap) throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.create() called: " + type + ": "
                     + agentName);
         }
         if (attrMap == null || attrMap.isEmpty()) {
             if (debug.messageEnabled()) {
                 debug.message("AgentsRepo.create(): Attribute Map is empty ");
             }
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "201", null);
         }
         String agentType = null;
         ServiceConfig aTypeConfig = null;
         if (attrMap != null && !attrMap.isEmpty()) {
             Set aTypeSet = (HashSet) attrMap.get(IdConstants.AGENT_TYPE);
 
             if ((aTypeSet != null) && (!aTypeSet.isEmpty())) {
                 agentType = (String) aTypeSet.iterator().next();
                 attrMap.remove(IdConstants.AGENT_TYPE);
             } else {
                 debug.error("AgentsRepo.create():Unable to create agents. "+
                    "Agent Type "+aTypeSet+ " is empty");
                 return (null);
             } 
         }
         try {
             if (type.equals(IdType.AGENTONLY)) {
                 orgConfig = getOrgConfig(token);
                 aTypeConfig = orgConfig.getSubConfig(agentName);
                 if (aTypeConfig == null) {
                     orgConfig.addSubConfig(agentName, agentType, 0, attrMap);
                     aTypeConfig = orgConfig.getSubConfig(agentName);
                 }
             } else if (type.equals(IdType.AGENTGROUP)) {
                 agentGroupConfig = getAgentGroupConfig(token);
                 aTypeConfig = agentGroupConfig.getSubConfig(agentName);
                 if (aTypeConfig == null) {
                     agentGroupConfig.addSubConfig(agentName, agentType, 0, 
                         attrMap);
                     aTypeConfig = agentGroupConfig.getSubConfig(agentName);
                 }
             }
         } catch (SMSException smse) {
             debug.error("AgentsRepo.create():Unable to create agents ", smse);
             Object args[] = { NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "200", args);
         }
         return (aTypeConfig.getDN());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#delete(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public void delete(SSOToken token, IdType type, String name)
         throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.delete() called: " + type + ": "
                     + name);
         }
         ServiceConfig aCfg = null;
         try {
             if (type.equals(IdType.AGENTONLY)) {
                 orgConfig = getOrgConfig(token);
                 aCfg = orgConfig.getSubConfig(name);
                 if (aCfg != null) {
                     orgConfig.removeSubConfig(name);
                 }
             } else if (type.equals(IdType.AGENTGROUP)) {
                 agentGroupConfig = getAgentGroupConfig(token);
                 aCfg = agentGroupConfig.getSubConfig(name);
                 if (aCfg != null) {
                     agentGroupConfig.removeSubConfig(name);
                 }
             }
         } catch (SMSException smse) {
             debug.error("AgentsRepo.delete: Unable to delete agents ", smse);
             Object args[] = { NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "200", args);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getAttributes(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
         Set attrNames) throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getAttributes() with attrNames called: " 
                 + type + ": " + name);
         }
         CaseInsensitiveHashMap allAtt = new CaseInsensitiveHashMap(
                 getAttributes(token, type, name));
         Map resultMap = new HashMap();
         Iterator it = attrNames.iterator();
         while (it.hasNext()) {
             String attrName = (String) it.next();
             if (allAtt.containsKey(attrName)) {
                 resultMap.put(attrName, allAtt.get(attrName));
             }
         }
         return resultMap;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getAttributes(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public Map getAttributes(SSOToken token, IdType type, String name)
          throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getAttributes() called: " + type + ": "
                 + name);
         }
         if (type.equals(IdType.AGENT) || type.equals(IdType.AGENTONLY) ||
             type.equals(IdType.AGENTGROUP)) {
             Map agentsAttrMap = new HashMap(2);
             try {
                 if (type.equals(IdType.AGENTONLY)) {
                     // Return the attributes for the given agent under 
                     // default group.
                     orgConfig = getOrgConfig(token);
                     agentsAttrMap = getAgentAttrs(orgConfig, name);
                 } else if (type.equals(IdType.AGENTGROUP)) {
                     agentGroupConfig = getAgentGroupConfig(token);
                     // Return the attributes of agent under specified group.
                     agentsAttrMap = getAgentAttrs(agentGroupConfig, name);
                 } else if (type.equals(IdType.AGENT)) {
                     // By default return the union of agents under
                     // default group and the agent group.
                     orgConfig = getOrgConfig(token);
                     agentsAttrMap = getAgentAttrs(orgConfig, name);
 
                     agentGroupConfig = getAgentGroupConfig(token);
                     Map agentGroupMap = getAgentAttrs(agentGroupConfig, name);
 
                     if (agentsAttrMap != null) {
                         if (agentGroupMap != null) {
                             agentsAttrMap.putAll(agentGroupMap);
                         }
                     }
                 }
                 if (debug.messageEnabled()) {
                     debug.message("AgentsRepo.getAttributes() agentsAttrMap: " 
                         + agentsAttrMap);
                 }
                 return agentsAttrMap;
             } catch (IdRepoException idpe) {
                 debug.error("AgentsRepo.getAttributes(): Unable to read agent"
                     + " attributes ", idpe);
                 Object args[] = { NAME };
                 throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "200", 
                     args);
             }
         }
         Object args[] = { NAME, IdOperation.READ.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
 
     private Map getAgentAttrs(ServiceConfig svcConfig, String agentName)
         throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getAgentAttrs() called: " + agentName);
         }
         Map answer = new HashMap(2);
         try {
             // Get the agent's config and then it's attributes.
             ServiceConfig aCfg = svcConfig.getSubConfig(agentName);
             if (aCfg != null) {
                 answer = aCfg.getAttributes();
                 // Send the agenttype of that agent.
                 Set vals = new HashSet(2);
                 vals.add(aCfg.getSchemaID());
                 answer.put(IdConstants.AGENT_TYPE, vals);
             }
         } catch (SMSException sme) {
             debug.error("AgentsRepo.getAgentAttrs(): "
                 + "Error occurred while getting " + agentName, sme);
             throw new IdRepoException(sme.getMessage());
         }
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getAgentAttrs() answer: " + answer);
         }
         return (answer);
     }
                             
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getBinaryAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set)
      */
     public Map getBinaryAttributes(SSOToken token, IdType type, String name,
             Set attrNames) throws IdRepoException, SSOException {
 
         Object args[] = { NAME, IdOperation.READ.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#setBinaryAttributes(
      *      com.iplanet.sso.SSOToken, com.sun.identity.idm.IdType,
      *      java.lang.String, java.util.Map, boolean)
      */
     public void setBinaryAttributes(SSOToken token, IdType type, String name,
             Map attributes, boolean isAdd) throws IdRepoException, 
             SSOException {
     
         Object args[] = { NAME, IdOperation.EDIT.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getMembers(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String,
      *      com.sun.identity.idm.IdType)
      */
     public Set getMembers(SSOToken token, IdType type, String name,
             IdType membersType) throws IdRepoException, SSOException {
 
         /*
          * name would be the name of the agentgroup.
          * membersType would be the IdType of the agent to be retrieved.
          * type would be the IdType of the agentgroup.
          */
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getMembers called" + type + ": " + name
                     + ": " + membersType);
         }
         Set results = new HashSet();
         if (type.equals(IdType.USER) || type.equals(IdType.AGENT)) {
             debug.error("AgentsRepo.getMembers: Membership operation is "
                 + "not supported for Users or Agents");
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "203", null);
         }
         if (!membersType.equals(IdType.AGENTONLY) && 
             !membersType.equals(IdType.AGENT)) {
             debug.error("AgentsRepo.getMembers: Cannot get member from a "
                 + "non-agent type "+ membersType.getName());
             Object[] args = { NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "206", args);
         }
         if (type.equals(IdType.AGENTGROUP)) {
             try {
                 // Search and get the serviceconfig of the agents and get
                 // the value of the attribute 'labeledURI' and if the agent
                 // belongs to the agentgroup, add the agent/member to the 
                 // result set. 
                 orgConfig = getOrgConfig(token);
                 for (Iterator items = orgConfig.getSubConfigNames()
                     .iterator(); items.hasNext();) {
                     String agent = (String) items.next();
                     ServiceConfig aCfg = null;
                     aCfg = orgConfig.getSubConfig(agent);
                     if (aCfg !=null) {
                         String lUri = aCfg.getLabeledUri();
                         if ((lUri != null) && lUri.equalsIgnoreCase(name)) {
                             results.add(agent);
                         }
                     }
                 }
             } catch (SMSException sme) {
                 debug.error("AgentsRepo.getMembers: Caught "
                         + "exception while getting agents"
                         + " from groups", sme);
                 Object args[] = { NAME, type.getName(), name };
                 throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "212", 
                     args);
             }
         } else {
             Object args[] = { NAME, IdOperation.READ.getName() };
             throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, 
                 "305", args);
         }
         return (results);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getMemberships(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String,
      *      com.sun.identity.idm.IdType)
      */
     public Set getMemberships(SSOToken token, IdType type, String name,
             IdType membershipType) throws IdRepoException, SSOException {
 
         /*
          * name would be the name of the agent.
          * membersType would be the IdType of the agentgroup to be retrieved.
          * type would be the IdType of the agent.
          */
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getMemberships called " + type + ": " +
                 name + ": " + membershipType);
         }
 
         // Memberships can be returned for agents.
         if (!type.equals(IdType.AGENT) && !type.equals(IdType.AGENTONLY)) {
             debug.message(
                 "AgentsRepo:getMemberships supported only for agents");
             Object args[] = { NAME };
             throw (new IdRepoException(IdRepoBundle.BUNDLE_NAME, "206", args));
         }
 
         // Set to maintain the members
         Set results = new HashSet();
         if (membershipType.equals(IdType.AGENTGROUP)) {
             try {
                 // Search and get the serviceconfig of the agent and get
                 // the value of the attribute 'labeledURI' and if the agent
                 // belongs to the agentgroup, add the agentgroup to the 
                 // result set. 
                 orgConfig = getOrgConfig(token);
                 ServiceConfig aCfg = null;
                 aCfg = orgConfig.getSubConfig(name);
                 if (aCfg !=null) {
                     String lUri = aCfg.getLabeledUri();
                     if ((lUri != null) && (lUri.length() > 0)) {
                         results.add(lUri);
                     }
                 }
             } catch (SMSException sme) {
                 debug.error("AgentsRepo.getMemberships: Caught "
                         + "exception while getting memberships"
                         + " for Agent", sme);
                 Object args[] = { NAME, type.getName(), name };
                 throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "212", 
                     args);
             }
         } else {
             // throw unsupported operation exception
             Object args[] = { NAME, IdOperation.READ.getName(),
                 membershipType.getName() };
             throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME,
                 "305", args);
         }
         return (results);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getServiceAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.util.Set)
      */
     public Map getServiceAttributes(SSOToken token, IdType type, String name,
             String serviceName, Set attrNames) throws IdRepoException,
             SSOException {
 
         Object args[] = {NAME, IdOperation.READ.getName()};
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /* 
      * (non-Javadoc)
      *
      * @see com.sun.identity.idm.IdRepo#getBinaryServiceAttributes(
      * com.iplanet.sso.SSOToken, com.sun.identity.idm.IdType,
      * java.lang.String, java.util.Set)
      */
     public Map getBinaryServiceAttributes(SSOToken token, IdType type,
             String name, String serviceName, Set attrNames)
             throws IdRepoException, SSOException {
 
         Object args[] = {NAME, IdOperation.READ.getName()};
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#isExists(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public boolean isExists(SSOToken token, IdType type, String name)
             throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.isExists() called: " + type + ": " +
                 name);
         }
         boolean exist = false;
         Map answer = getAttributes(token, type, name);
         if (answer != null && !answer.isEmpty()) {
             exist = true;
         }
         return (exist);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#modifyMemberShip(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set,
      *      com.sun.identity.idm.IdType, int)
      */
     public void modifyMemberShip(SSOToken token, IdType type, String name,
             Set members, IdType membersType, int operation)
             throws IdRepoException, SSOException {
 
         /*
          * name would be the name of the agentgroup.
          * members would include the name of the agents to be added/removed 
          * to/from the group.
          * membersType would be the IdType of the agent to be added/removed.
          * type would be the IdType of the agentgroup.
          */
 
          if (debug.messageEnabled()) {
              debug.message("AgentsRepo: modifyMemberShip called " + type + ": "
                     + name + ": " + members + ": " + membersType);
          }
          if (members == null || members.isEmpty()) {
              debug.error("AgentsRepo.modifyMemberShip: Members set is empty");
              throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "201", null);
          }
          if (type.equals(IdType.USER) || type.equals(IdType.AGENT)) {
              debug.error("AgentsRepo.modifyMembership: Membership to users "
                  + "and agents is not supported");
              throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "203", null);
          }
          if (!membersType.equals(IdType.AGENTONLY)) {
              debug.error("AgentsRepo.modifyMembership: A non-agent type"
                  + " cannot be made a member of any identity"
                     + membersType.getName());
              Object[] args = { NAME };
              throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "206", args);
          }
          if (type.equals(IdType.AGENTGROUP)) {
              try {
                  // Search and get the serviceconfig of the agent and set 
                  // the 'labeledURI' with the value of the agentgroup name 
                  // eg., 'AgentGroup1'.
                 // One agent instance should belong to at most one group.
 
                  Set nameSet = new HashSet();
                  nameSet.add(name);
                  orgConfig = getOrgConfig(token);
                  Iterator it = members.iterator();
                  ServiceConfig aCfg = null;
                  while (it.hasNext()) {
                      String agent = (String) it.next();
                      aCfg = orgConfig.getSubConfig(agent);
                      if (aCfg !=null) {
                          switch (operation) {
                          case ADDMEMBER:
                              aCfg.setLabeledUri(name);
                              break;
                          case REMOVEMEMBER:
                              aCfg.removeAttributeValues(labeledURI, nameSet);
                          }
                      }
                  }
             } catch (SMSException sme) {
                 debug.error("AgentsRepo.modifyMembership: Caught "
                         + "exception while " + " adding/removing agents"
                         + " to groups", sme);
                 Object args[] = { NAME, type.getName(), name };
                 throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "212", 
                     args);
             }
         } else {
             // throw an exception
             debug.error("AgentsRepo.modifyMembership: Memberships cannot be"
                     + "modified for type= " + type.getName());
             Object[] args = { NAME, type.getName() };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "209", args);
         }
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#removeAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set)
      */
     public void removeAttributes(SSOToken token, IdType type, String name,
             Set attrNames) throws IdRepoException, SSOException {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#removeListener()
      */
     public void removeListener() {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#search(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, int, int,
      *      java.util.Set, boolean, int, java.util.Map, boolean)
      */
     public RepoSearchResults search(SSOToken token, IdType type,
             String pattern, int maxTime, int maxResults, Set returnAttrs,
             boolean returnAllAttrs, int filterOp, Map avPairs, 
             boolean recursive) throws IdRepoException, SSOException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.search() called: " + type + ": " +
                 pattern);
         }
         Set agentRes = new HashSet(2);
         Map agentAttrs = new HashMap();
         int errorCode = RepoSearchResults.SUCCESS;
         ServiceConfig aCfg = null;
         try {
             if (type.equals(IdType.AGENTONLY)) {
                 // Get the config from 'default' group.
                 orgConfig = getOrgConfig(token);
 
                 if (isAgentTypeSearch(orgConfig, pattern)) {
                     agentRes.add(pattern);
                 } else {
                     aCfg = orgConfig;
                     agentRes = getAgentPattern(aCfg, pattern);
                 }
             } else if (type.equals(IdType.AGENTGROUP)) {
                 // Get the config from specified group.
                 agentGroupConfig = getAgentGroupConfig(token);
 
                 if (isAgentTypeSearch(agentGroupConfig, pattern)) {
                     agentRes.add(pattern);
                 } else {
                     aCfg = agentGroupConfig;
                     agentRes = getAgentPattern(aCfg, pattern);
                 }
             } else if (type.equals(IdType.AGENT)) {
                     agentRes.add(pattern);
             }
             if (agentRes != null) {
                 Iterator it = agentRes.iterator();
                 while (it.hasNext()) {
                     String agName = (String) it.next();
                     Map attrsMap = getAttributes(token, type, agName);
                     if (attrsMap != null && !attrsMap.isEmpty()) {
                         agentAttrs.put(agName, attrsMap);
                     } else {
                         return new RepoSearchResults(new HashSet(),
                             RepoSearchResults.SUCCESS, Collections.EMPTY_MAP, 
                                 type);
                     }
                 }
             }
         } catch (SSOException sse) {
             debug.error("AgentsRepo.search(): Unable to retrieve entries: ",
                     sse);
             Object args[] = { NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "219", args);
         }
         return new RepoSearchResults(agentRes, errorCode, agentAttrs, type);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#search(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      boolean, int, int, java.util.Set)
      */
     public RepoSearchResults search(SSOToken token, IdType type,
             String pattern, Map avPairs, boolean recursive, int maxResults,
             int maxTime, Set returnAttrs) throws IdRepoException, SSOException {
 
         return (search(token, type, pattern, maxTime, maxResults, returnAttrs,
                 (returnAttrs == null), OR_MOD, avPairs, recursive));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#setAttributes(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      boolean)
      */
     public void setAttributes(SSOToken token, IdType type, String name,
         Map attributes, boolean isAdd) 
         throws IdRepoException, SSOException {
     
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.setAttributes() called: " + type + ": "
                     + name);
         }
         if (attributes == null || attributes.isEmpty()) {
             if (debug.messageEnabled()) {
                 debug.message("AgentsRepo.setAttributes(): Attributes " +
                         "are empty");
             }
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "201", null);
         }
         ServiceConfig aCfg = null;
         try {
             if (type.equals(IdType.AGENTONLY)) {
                 orgConfig = getOrgConfig(token);
                 aCfg = orgConfig.getSubConfig(name);
             } else if (type.equals(IdType.AGENTGROUP)) {
                 agentGroupConfig = getAgentGroupConfig(token);
                 aCfg = agentGroupConfig.getSubConfig(name);
             } else {
                 Object args[] = { NAME, IdOperation.READ.getName() };
                 throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME,
                     "305", args);
             }
             if (aCfg != null) {
                 aCfg.setAttributes(attributes);
             }
         } catch (SMSException smse) {
             debug.error("AgentsRepo.setAttributes(): Unable to set agent"
                 + " attributes ",smse);
             Object args[] = { NAME, type.getName(), name };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "212", args);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getSupportedOperations(
      *      com.sun.identity.idm.IdType)
      */
     public Set getSupportedOperations(IdType type) {
         return (Set) supportedOps.get(type);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getSupportedTypes()
      */
     public Set getSupportedTypes() {
         return supportedOps.keySet();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#initialize(java.util.Map)
      */
     public void initialize(Map configParams) {
         super.initialize(configParams);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#isActive(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public boolean isActive(SSOToken token, IdType type, String name)
             throws IdRepoException, SSOException {
 
         Map attributes = getAttributes(token, type, name);
         if (attributes == null) {
             Object[] args = { NAME, name };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "202", args);
         }
         Set activeVals = (Set) attributes.get(statusAttribute);
         if (activeVals == null || activeVals.isEmpty()) {
             return true;
         } else {
             Iterator it = activeVals.iterator();
             String active = (String) it.next();
             return (active.equalsIgnoreCase(statusActive) ? true : false);
         }
 
     }
 
     /* (non-Javadoc)
      * @see com.sun.identity.idm.IdRepo#setActiveStatus(
         com.iplanet.sso.SSOToken, com.sun.identity.idm.IdType,
         java.lang.String, boolean)
      */
     public void setActiveStatus(SSOToken token, IdType type,
         String name, boolean active)
         throws IdRepoException, SSOException {
 
         Map attrs = new HashMap();
         Set vals = new HashSet(2);
         if (active) {
             vals.add(statusActive);
         } else {
             vals.add(statusInactive);
         }
         attrs.put(statusAttribute, vals);
         setAttributes(token, type, name, attrs, false);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#shutdown()
      */
     public void shutdown() {
         scm.removeListener(scmListenerId);
         ssm.removeListener(ssmListenerId);
     }
 
     private void loadSupportedOps() {
         Set opSet = new HashSet(2);
         opSet.add(IdOperation.EDIT);
         opSet.add(IdOperation.READ);
         opSet.add(IdOperation.CREATE);
         opSet.add(IdOperation.DELETE);
 
         Set opSet2 = new HashSet(opSet);
         opSet2.remove(IdOperation.EDIT);
         opSet2.remove(IdOperation.CREATE);
         opSet2.remove(IdOperation.DELETE);
 
         supportedOps.put(IdType.AGENTONLY, Collections.unmodifiableSet(
             opSet));
         supportedOps.put(IdType.AGENTGROUP, Collections.unmodifiableSet(
             opSet));
             
         supportedOps.put(IdType.AGENT, Collections.unmodifiableSet(opSet2));
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.loadSupportedOps() called: "
                     + "supportedOps Map = " + supportedOps);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.sm.ServiceListener#globalConfigChanged(
      *      java.lang.String,
      *      java.lang.String, java.lang.String, java.lang.String, int)
      */
     public void globalConfigChanged(String serviceName, String version,
             String groupName, String serviceComponent, int type) {
         repoListener.allObjectsChanged();
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.sm.ServiceListener#organizationConfigChanged(
      *      java.lang.String,
      *      java.lang.String, java.lang.String, java.lang.String,
      *      java.lang.String, int)
      */
     public void organizationConfigChanged(String serviceName, String version,
             String orgName, String groupName, String serviceComponent, int type)
     {
         repoListener.allObjectsChanged();
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.sm.ServiceListener#schemaChanged(java.lang.String,
      *      java.lang.String)
      */
     public void schemaChanged(String serviceName, String version) {
         repoListener.allObjectsChanged();
     }
 
     public String getFullyQualifiedName(SSOToken token, IdType type, 
             String name) throws IdRepoException, SSOException {
         RepoSearchResults results = search(token, type, name, null, true, 0, 0,
                 null);
         Set dns = results.getSearchResults();
         if (dns.size() != 1) {
             String[] args = { name };
             throw (new IdRepoException(IdRepoBundle.BUNDLE_NAME, "220", args));
         }
         return ("sms://AgentsRepo/" + dns.iterator().next().toString());
     }
 
     public boolean supportsAuthentication() {
         return (true);
     }
 
     public boolean authenticate(Callback[] credentials) 
         throws IdRepoException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.authenticate() called");
         }
 
         // Obtain user name and password from credentials and compare
         // with the ones from the agent profile to authorize the agent.
         String username = null;
         String password = null;
         for (int i = 0; i < credentials.length; i++) {
             if (credentials[i] instanceof NameCallback) {
                 username = ((NameCallback) credentials[i]).getName();
                 if (debug.messageEnabled()) {
                     debug.message("AgentsRepo.authenticate() username: "
                             + username);
                 }
             } else if (credentials[i] instanceof PasswordCallback) {
                 char[] passwd = ((PasswordCallback) credentials[i])
                         .getPassword();
                 if (passwd != null) {
                     password = new String(passwd);
                     if (debug.messageEnabled()) {
                         debug.message("AgentsRepo.authenticate() passwd "
                             + "present");
                     }
                 }
             }
         }
         if (username == null || (username.length() == 0) || 
             password == null) {
             Object args[] = { NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "221", args);
         }
         SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
 
         boolean answer = false;
         String baseDN = null;
         String userid = username;
         try {
             /* Only agents with IdType.AGENTONLY is used for authentication,
              * not the agents with IdType.AGENTGROUP.
              * AGENTGROUP is for storing common properties.
              * So use the AGENTONLY's baseDN.
              */
             baseDN = constructDN("default", "ou=OrganizationConfig,",
                     "/", version, agentserviceName);
             if (DN.isDN(username)) {
                 userid = LDAPDN.explodeDN(username, true)[0];
             }
             Set pSet = new HashSet(2);
             pSet.add("userpassword");
             Map ansMap = new HashMap();
             String userPwd = null;
             ansMap = getAttributes(adminToken, IdType.AGENTONLY, userid, pSet);
             Set userPwdSet = (Set) ansMap.get("userpassword"); 
             if ((userPwdSet != null) && (!userPwdSet.isEmpty())) {
                 userPwd = (String) userPwdSet.iterator().next();
                 answer = password.equals(userPwd);
             }
             if (debug.messageEnabled()) {
                 debug.message("AgentsRepo.authenticate() result: " + answer);
             }
         } catch (SSOException ssoe) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.authenticate(): "
                         + "Unable to authenticate SSOException:" + ssoe);
             }
         } catch (SMSException smse) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.authenticate: "
                         + "Unable to construct agent DN " + smse);
             }
         }
         return (answer);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.sun.identity.idm.IdRepo#modifyService(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.sm.SchemaType, java.util.Map)
      */
     public void modifyService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType sType, Map attrMap)
             throws IdRepoException, SSOException {
 
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.sun.identity.idm.IdRepo#unassignService(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.util.Map)
      */
     public void unassignService(SSOToken token, IdType type, String name,
             String serviceName, Map attrMap) throws IdRepoException,
             SSOException {
 
         Object args[] = {
                 "com.sun.identity.idm.plugins.specialusers.SpecialRepo",
                 IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.sun.identity.idm.IdRepo#assignService(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.sm.SchemaType, java.util.Map)
      */
     public void assignService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap)
             throws IdRepoException, SSOException {
 
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      *
      * @see com.sun.identity.idm.IdRepo#getAssignedServices(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map)
      */
     public Set getAssignedServices(SSOToken token, IdType type, String name,
             Map mapOfServicesAndOCs) throws IdRepoException, SSOException {
 
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     private ServiceConfig getOrgConfig(SSOToken token) {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getOrgConfig() called. ");
         }
         try {
             if (orgConfig == null) {
                 if (scm == null) {
                     scm = new ServiceConfigManager(token, agentserviceName, 
                         version);
                 }
                 orgConfig = scm.getOrganizationConfig("/", null);
             }
         } catch (SMSException smse) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.getOrgConfig(): "
                         + "Unable to init ssm and scm due to " + smse);
             }
         } catch (SSOException ssoe) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.getOrgConfig(): "
                         + "Unable to init ssm and scm due to " + ssoe);
             }
         }
         return (orgConfig);
     }
 
     private ServiceConfig getAgentGroupConfig(SSOToken token) {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getAgentGroupConfig() called. ");
         }
         try {
             if (agentGroupConfig == null) {
                 if (scm == null) {
                     scm = new ServiceConfigManager(token, agentserviceName, 
                         version);
                 }
                 String agentGroupDN = constructDN(agentGroupNode, 
                     instancesNode, "/", version, agentserviceName);
                 ServiceConfig orgConfig = getOrgConfig(token);
                 orgConfig.checkAndCreateGroup(agentGroupDN, agentGroupNode);
                 agentGroupConfig = 
                     scm.getOrganizationConfig("/", agentGroupNode);
             }
         } catch (SMSException smse) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.getAgentGroupConfig: "
                         + "Unable to init ssm and scm due to " + smse);
             }
         } catch (SSOException ssoe) {
             if (debug.warningEnabled()) {
                 debug.warning("AgentsRepo.getAgentGroupConfig: "
                         + "Unable to init ssm and scm due to " + ssoe);
             }
         }
         return (agentGroupConfig);
     }
 
     private boolean isAgentTypeSearch(ServiceConfig aConfig, String pattern) 
         throws IdRepoException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.isAgentTypeSearch() called: " + pattern);
         }
         String agentType = null;
         boolean agentTypeflg = false;
 
         try {
             // Get the agentType and then compare the pattern sent for Search.
             for (Iterator items = aConfig.getSubConfigNames()
                 .iterator(); items.hasNext();) {
                 agentType = (String) items.next();
                 if (agentType.equalsIgnoreCase(pattern)) {
                     agentTypeflg = true;
                     break;
                 }
             }
         } catch (SMSException sme) {
             debug.error("AgentsRepo.isAgentTypeSearch(): Error occurred while "
                 + "checking AgentType sent for pattern "+ pattern, sme);
             throw new IdRepoException(sme.getMessage());
         }
         return (agentTypeflg);
     }
 
 
     private Set getAgentPattern(ServiceConfig aConfig, String pattern) 
         throws IdRepoException {
 
         if (debug.messageEnabled()) {
             debug.message("AgentsRepo.getAgentPattern() called: " + pattern);
         }
         Set agentRes = new HashSet(2);
         try {
             if (aConfig != null) {
                 // If wild card is used for pattern, do a search else a lookup
                 if (pattern.indexOf('*') != -1) {
                     agentRes = aConfig.getSubConfigNames(pattern);
                 } else {
                     for (Iterator items = aConfig.getSubConfigNames()
                         .iterator(); items.hasNext();) {
                         String name = (String) items.next();
                         if (name.equalsIgnoreCase(pattern)) {
                             agentRes.add(pattern);
                             break;
                         }
                     }
                 }
             }
         } catch (SMSException sme) {
             debug.error("AgentsRepo.getAgentPattern(): Error occurred while "
                 + "checking AgentName sent for pattern "+ pattern, sme);
             throw new IdRepoException(sme.getMessage());
         }
         return (agentRes);
     }
 
     String constructDN(String groupName, String configName, String orgName, 
         String version, String serviceName) throws SMSException {
        
         StringBuffer sb = new StringBuffer(50);
         sb.append("ou=").append(groupName).append(comma).append(
                 configName).append("ou=").append(version)
                 .append(comma).append("ou=").append(serviceName)
                 .append(comma).append("ou=services").append(comma);
         orgName = DNMapper.orgNameToDN(orgName);
         sb.append(orgName);
         return (sb.toString());
     }
 }
