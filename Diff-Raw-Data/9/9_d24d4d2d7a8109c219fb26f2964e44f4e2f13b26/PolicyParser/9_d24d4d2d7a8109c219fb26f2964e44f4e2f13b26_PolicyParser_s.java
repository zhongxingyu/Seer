 package org.wyona.security.util;
 
 import org.wyona.security.core.GroupPolicy;
 import org.wyona.security.core.IdentityPolicy;
 import org.wyona.security.core.UsecasePolicy;
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.Policy;
 
 import org.apache.log4j.Logger;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 import java.util.Vector;
 
 /**
  * Allows to parse an XML such as for example
  * <policy xmlns="http://www.wyona.org/security/1.0" use-inherited-policies="false">
  *   <world>
  *     <right id="view">Read</right>
  *     <right id="write">Write</right>
  *   </world>
  *   <user id="http://michaelwechner.livejournal.com/">
  *     <right id="open" permission="true"/>
  *     <right id="write" permission="false"/>
  *   </user>
  *   <group id="admin">
  *     <right id="open" permission="true"/>
  *     <right id="write"/>
  *   </group>
  * </policy>
  */
 public class PolicyParser implements Policy {
 
     private static Logger log = Logger.getLogger(PolicyParser.class);
     protected DefaultConfigurationBuilder builder = null;
     protected Vector usecasePolicies = null;
     protected boolean useInheritedPolicies = true;
 
     /**
      *
      */
     public PolicyParser() throws Exception {
         usecasePolicies = new Vector();
     }
 
     /**
      * @param in Policy as XML as input stream
      * @param idManager Identity manager in order to resolve dependencies between users and groups
      */
     public Policy parseXML(java.io.InputStream in, IdentityManager idManager) throws Exception {
         boolean enableNamespaces = true;
         builder = new DefaultConfigurationBuilder(enableNamespaces);
         Configuration config = builder.build(in);
 
         String useInheritedPoliciesString = config.getAttribute("use-inherited-policies", "true");
         if (useInheritedPoliciesString.equals("false")) useInheritedPolicies = false;
 
         // TODO: Check for world
 
         Configuration[] userConfigs = config.getChildren("user");
         for (int i = 0; i < userConfigs.length; i++) {
             Configuration[] rightConfigs = userConfigs[i].getChildren("right");
             for (int k = 0; k < rightConfigs.length; k++) {
                 UsecasePolicy up = new UsecasePolicy(rightConfigs[k].getAttribute("id"));
                 String permission = rightConfigs[k].getAttribute("permission");
                 if (idManager != null) {
                     Identity identity = new Identity(idManager.getUserManager().getUser(userConfigs[i].getAttribute("id")));
/*
                     if (identity.getGroupnames() != null) {
                        log.warn("DEBUG: Number of groups of user '" + identity.getUsername() + "': " + identity.getGroupnames().length);
                     } else {
                        log.warn("User '" + identity.getUsername() + "' has no groups!");
                     }
*/
                     up.addIdentity(identity, new Boolean(permission).booleanValue());
                 } else {
                     log.warn("Groups are not associated with user!");
                     up.addIdentity(new Identity(userConfigs[i].getAttribute("id")), new Boolean(permission).booleanValue());
                 }
                 addUsecasePolicy(up);
             }
         }
 
         Configuration[] groupConfigs = config.getChildren("group");
         for (int i = 0; i < groupConfigs.length; i++) {
             Configuration[] rightConfigs = groupConfigs[i].getChildren("right");
             for (int k = 0; k < rightConfigs.length; k++) {
                 UsecasePolicy up = new UsecasePolicy(rightConfigs[k].getAttribute("id"));
                 String permission = rightConfigs[k].getAttribute("permission");
                 up.addGroupPolicy(new GroupPolicy(groupConfigs[i].getAttribute("id"), new Boolean(permission).booleanValue()));
                 addUsecasePolicy(up);
             }
         }
 
         // TODO: Check for hosts
 
         return this;
     }
 
     /**
      * @deprecated Use parseXML(InputStream, IdentityManager) instead
      */
     public Policy parseXML(java.io.InputStream in) throws Exception {
         return parseXML(in, null);
     }
 
     /**
      * @see
      */
     public UsecasePolicy[] getUsecasePolicies() {
         UsecasePolicy[] ups = new UsecasePolicy[usecasePolicies.size()];
         for (int i = 0; i < ups.length; i++) {
             ups[i] = (UsecasePolicy) usecasePolicies.elementAt(i);
         }
         return ups;
     }
 
     /**
      * @see
      */
     public void addUsecasePolicy(UsecasePolicy up) throws AccessManagementException {
         UsecasePolicy existingUsecasePolicy = null;
 
         for (int i = 0; i < usecasePolicies.size(); i++) {
             UsecasePolicy existingUP = (UsecasePolicy) usecasePolicies.elementAt(i);
             if (existingUP.getName().equals(up.getName())) {
                 existingUsecasePolicy = existingUP;
                 break;
             }
         }
 
         if (existingUsecasePolicy != null) {
             // Merge identities and groups
             IdentityPolicy[] identityPolicies = up.getIdentityPolicies();
             for (int k = 0; k < identityPolicies.length; k++) {
                 boolean identityExists = false;
                 IdentityPolicy[] existingIdentityPolicies = existingUsecasePolicy.getIdentityPolicies();
                 for (int i = 0; i < existingIdentityPolicies.length; i++) {
                     if (identityPolicies[k].getIdentity().getUsername().equals(existingIdentityPolicies[i].getIdentity().getUsername())) {
                         identityExists = true;
                         break;
                     }
                 }
                 if (!identityExists) {
 /*
                     if (idManager != null) {
                         existingUsecasePolicy.addIdentity(new Identity(idManager.getUserManager().getUser(identityPolicies[k].getIdentity().getUsername())), identityPolicies[k].getPermission());
                     } else {
 */
                         log.warn("Groups are not associated with user!");
                         existingUsecasePolicy.addIdentity(new Identity(identityPolicies[k].getIdentity().getUsername()), identityPolicies[k].getPermission());
 /*
                     }
 */
                 }
             }
 
             GroupPolicy[] groups = up.getGroupPolicies();
             for (int k = 0; k < groups.length; k++) {
                 boolean groupExists = false;
                 GroupPolicy[] existingGroups = existingUsecasePolicy.getGroupPolicies();
                 for (int i = 0; i < existingGroups.length; i++) {
                     if (groups[k].getId().equals(existingGroups[i].getId())) {
                         groupExists = true;
                         break;
                     }
                 }
                 if (!groupExists) {
                     existingUsecasePolicy.addGroupPolicy(new GroupPolicy(groups[k].getId(), groups[k].getPermission()));
                 }
             }
         } else {
             usecasePolicies.add(up);
             log.info("New usecase policy has been added: " + up.getName());
         }
     }
 
     /**
      * @see
      */
     public String getPath() throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     /**
      * @see
      */
     public Policy getParentPolicy() throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     /**
      * @see
      */
     public boolean useInheritedPolicies() {
         return useInheritedPolicies;
     }
 
     /**
      *
      */
     public String toString() {
         StringBuffer sb = new StringBuffer("Policy:\n");
         UsecasePolicy[] ups = getUsecasePolicies();
         for (int i = 0; i < ups.length; i++) {
             sb.append("  Usecase: " + ups[i].getName() + "\n");
             Identity[] ids = ups[i].getIdentities();
             for (int j = 0; j < ids.length; j++) {
                 if (ids[j].isWorld()) {
                     sb.append("    WORLD\n");
                 } else {
                     sb.append("    User: " + ids[j].getUsername() + "\n");
                 }
             }
             GroupPolicy[] gps = ups[i].getGroupPolicies();
             for (int j = 0; j < gps.length; j++) {
                 sb.append("    Group: " + gps[j].getId() + " (" + gps[j].getPermission() + ")\n");
             }
         }
         return sb.toString();
     }
 
     public UsecasePolicy getUsecasePolicy(String name) throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     public void removeUsecasePolicy(String name) throws AccessManagementException {
         log.warn("Not implemented yet!");
     }
 
     public void setUseInheritedPolicies(boolean useInheritedPolicies) {
         log.warn("Not implemented yet!");
     }
 }
 
