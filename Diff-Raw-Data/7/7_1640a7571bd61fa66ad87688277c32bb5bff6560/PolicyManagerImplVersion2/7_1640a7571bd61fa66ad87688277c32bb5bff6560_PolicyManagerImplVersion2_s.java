 package org.wyona.security.impl;
 
 import java.util.Hashtable;
 
 import org.wyona.commons.io.Path;
 import org.wyona.commons.io.PathUtil;
 import org.wyona.security.core.AuthorizationException;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.Policy;
 import org.wyona.security.core.api.PolicyManager;
 import org.wyona.security.core.api.Role;
 import org.wyona.security.core.api.Usecase;
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.RepositoryFactory;
 import org.wyona.yarep.util.RepoPath;
 import org.wyona.yarep.util.YarepUtil;
 
 import org.apache.log4j.Logger;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 /**
  * Policy manager implementation version 2
  */
 public class PolicyManagerImplVersion2 implements PolicyManager {
 
     private static Logger log = Logger.getLogger(PolicyManagerImplVersion2.class);
 
     private Repository policiesRepository;
     private DefaultConfigurationBuilder configBuilder;
 
     private static String USECASE_ELEMENT_NAME = "usecase";
 
     /**
      *
      */
     public PolicyManagerImplVersion2(Repository policiesRepository) {
         this.policiesRepository = policiesRepository;
         configBuilder = new DefaultConfigurationBuilder();
     }
     
     /**
      * Get policies repository
      */
      public Repository getPoliciesRepository() {
            return policiesRepository;
      }
          
      
     /**
      * @deprecated Use authorize(String, Identity, Usecase) instead
      */
     public boolean authorize(Path path, Identity identity, Role role) throws AuthorizationException {
         return authorize(path.toString(), identity, role);
        
     }
 
     /**
      * @deprecated Use authorize(String, Identity, Usecase) instead
      */
     public boolean authorize(String path, Identity identity, Role role) throws AuthorizationException {
         log.warn("Deprecated method and not implemented! Use method authorize(String, Identity, Usecase) instead!");
         return false;
     }
 
     /**
      * @see org.wyona.security.core.api.PolicyManager#authorize(Policy, Identity, Usecase)
      */
     public boolean authorize(Policy policy, Identity identity, Usecase usecase) throws AuthorizationException {
         log.error("Not implemented yet!");
         return false;
     }
    
     /**
      * @see org.wyona.security.core.api.PolicyManager#authorize(String, Identity, Usecase)
      */
     public boolean authorize(String path, Identity identity, Usecase usecase) throws AuthorizationException {
         if(path == null || identity == null || usecase == null) {
             log.error("Path or identity or usecase is null! [" + path + ", " + identity + ", " + usecase + "]");
             throw new AuthorizationException("Path or identity or usecase is null! [" + path + ", " + identity + ", " + usecase + "]");
         }
 
         try {
             return authorize(getPoliciesRepository(), path, identity, usecase);
         } catch(Exception e) {
             log.error(e.getMessage(), e);
             throw new AuthorizationException("Error authorizing " + getPoliciesRepository().getID() + ", " + path + ", " + identity + ", " + usecase, e);
         }
     }
 
     /**
      * @param repo Access control policy repository
      */
     private boolean authorize(Repository repo, String path, Identity identity, Usecase usecase) throws Exception {
         if(repo == null) {
             log.error("Repo is null!");
             throw new Exception("Repo is null!");
         } else if(path == null) {
             log.error("Path is null!");
             throw new Exception("Path is null!");
         } else if(identity == null) {
             log.error("Identity is null!");
             throw new Exception("Identity is null!");
         } else if(usecase == null) {
             log.error("Usecase is null!");
             throw new Exception("Usecase is null!");
         }
 
         String yarepPath = getPolicyPath(path); 
         log.debug("Policy Yarep Path: " + yarepPath + ", Original Path: " + path + ", Repo: " + repo);
         if (repo.existsNode(yarepPath)) {
             try {
                 Configuration config = configBuilder.build(repo.getNode(yarepPath).getInputStream());
                 boolean useInheritedPolicies = config.getAttributeAsBoolean("use-inherited-policies", true);
 
             Configuration[] usecases = config.getChildren(USECASE_ELEMENT_NAME);
             for (int i = 0; i < usecases.length; i++) {
                 String usecaseName = usecases[i].getAttribute("id", null);
                 if (usecaseName != null && usecaseName.equals(usecase.getName())) {
                     boolean useInheritedRolePolicies = usecases[i].getAttributeAsBoolean("use-inherited-policies", true);
                     Configuration[] accreditableObjects = usecases[i].getChildren();
 
                     boolean worldCredentialExists = false;
                     boolean worldIsNotAuthorized = true;
                     for (int k = 0; k < accreditableObjects.length; k++) {
                         String aObjectName = accreditableObjects[k].getName();
                         log.debug("Accreditable Object Name: " + aObjectName);
 
                         if (aObjectName.equals("world")) {
                             worldCredentialExists = true;
                             String permission = accreditableObjects[k].getAttribute("permission", null);
                             if (permission.equals("true")) {
                                 log.debug("Access granted: " + path);
                                 worldIsNotAuthorized = false;
                                 return true;
                             } else {
                                 worldIsNotAuthorized = true;
                             }
                         } else if (aObjectName.equals("group")) {
                             if (identity.getGroupnames() != null) {
                                 String groupName = accreditableObjects[k].getAttribute("id", null);
                                 String[] groupnames = identity.getGroupnames();
                                 if (groupnames != null) {
                                     for (int j = 0; j < groupnames.length; j++) {
                                         if (groupName.equals(groupnames[j])) {
                                             String permission = accreditableObjects[k].getAttribute("permission", null);
                                             if (permission.equals("true")) {
                                                 log.debug("Access granted: Path = " + path + ", Group = " + groupName);
                                                 return true;
                                             } else {
                                                 log.debug("Access denied: Path = " + path + ", Group = " + groupName);
                                                 return false;
                                             }
                                         }
                                     }
                                 }
                             }
                         } else if (aObjectName.equals("user")) {
                             if (identity.getUsername() != null) {
                                 String userName = accreditableObjects[k].getAttribute("id", null);
                                 if (userName.equals(identity.getUsername())) {
                                     String permission = accreditableObjects[k].getAttribute("permission", null);
                                     if (permission.equals("true")) {
                                         log.debug("Access granted: Path = " + path + ", User = " + userName);
                                         return true;
                                     } else {
                                         log.debug("Access denied: Path = " + path + ", User = " + userName);
                                         return false;
                                     }
                                 }
                             }
                         } else if (aObjectName.equals("iprange")) {
                             log.warn("Credential IP Range not implemented yet!");
                             //return false;
                         } else {
                             log.warn("No such accreditable object implemented: " + aObjectName);
                             //return false;
                         }
                     }
                     if (worldCredentialExists && worldIsNotAuthorized) {
                        log.debug("Access for world denied: " + path);
                        return false;
                     }
                     if (!useInheritedRolePolicies){
                         log.debug("Policy inheritance disabled for usecase:" + usecaseName + ". Access denied: "+ path);
                         return false;
                     }
                 }
             }
                 if (!useInheritedPolicies) {
                     log.debug("Policy inheritance disabled. Access denied: "+ path);
                     return false;
                 }
             } catch(NoSuchNodeException e) {
                 log.error(e.getMessage(), e);
             }
         } else {
             if (yarepPath.equals("/.policy")) {
                 log.warn("No such node: " + yarepPath + " (" + repo + ")");
             } else {
                 if (log.isDebugEnabled()) log.debug("No such node: " + yarepPath + " (Fallback to parent policy ...)");
             }
         }
 
         String parent = PathUtil.getParent(path);
         if (parent != null) {
             // Check policy of parent in order to inherit credentials ...
             log.debug("Check parent policy: " + parent + " ... (Current path: " + path + ")");
             return authorize(repo, parent, identity, usecase);
         } else {
             log.warn("Trying to get parent of " + path + " (" + repo + ") failed. Access denied.");
             return false;
         }
     }
 
     /**
      * Append '.policy' to path as suffix
      */
     private String getPolicyPath(String path) {
         // Remove trailing slash except for ROOT ...
         if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
             return path.substring(0, path.length() - 1) + ".policy";
         }
         return path + ".policy";
     }
 
     /**
      *
      */
     public Policy getPolicy(String path, boolean aggregate) throws AuthorizationException {
         try {
             if (aggregate) {
                 return org.wyona.security.impl.util.PolicyAggregator.aggregatePolicy(path, this);
             } else {
                 if (getPoliciesRepository().existsNode(getPolicyPath(path))) {
                     return new PolicyImplV2(getPoliciesRepository().getNode(getPolicyPath(path)).getInputStream());
                 } else {
                     if (!path.equals("/")) {
                         log.warn("No policy found for '" + path + "' (Policies Repository: " + getPoliciesRepository().getName() + "). Check for parent '" + PathUtil.getParent(path) + "'.");
                         return null;
                         //return getPolicy(PathUtil.getParent(path), false);
                     } else {
                         log.warn("No policies found at all, not even a root policy!");
                         return null;
                     }
                 }
             }
         } catch(Exception e) {
             log.error(e, e);
             throw new AuthorizationException(e.getMessage());
         }
     }
 
     /**
      * @see org.wyona.security.core.api.PolicyManager#setPolicy(String, Policy)
      */
     public void setPolicy(String path, Policy policy) throws java.lang.UnsupportedOperationException {
 
         Repository repo = getPoliciesRepository();
         String policyPath = getPolicyPath(path);
         try {
             StringBuilder sb = new StringBuilder("<policy xmlns=\"http://www.wyona.org/security/1.0\"");
             boolean inheritPolicy = policy.useInheritedPolicies();
             if (!inheritPolicy) {
                 sb.append(" use-inherited-policies=\"false\"");
             }
             sb.append(">");
 
             org.wyona.security.core.UsecasePolicy[] up = policy.getUsecasePolicies();
             for (int i = 0; i < up.length; i++) {
                 org.wyona.security.core.IdentityPolicy[] idps = up[i].getIdentityPolicies();
                 org.wyona.security.core.GroupPolicy[] gps = up[i].getGroupPolicies();
                 if ((idps != null && idps.length > 0) || (gps!= null && gps.length > 0)) {
                     sb.append("\n  <usecase id=\""+up[i].getName()+"\">");
                     log.warn("TODO: What about WORLD?!");
                     //sb.append("\n    <world permission=\"true\"/>");
                     for (int k = 0; k < idps.length; k++) {
                         if (inheritPolicy && idps[k].getPermission() == false) { // TODO: Check inheritance flag of identity policy
                             Identity identity = idps[k].getIdentity();
                             if (identity.getGroupnames() != null) {
                                log.warn("DEBUG: Number of groups: " + identity.getGroupnames().length);
                             }
                            log.warn("DEBUG: Identity: " + identity + ", Usecase: " + up[i].getName() + ", Permission: " + this.authorize(PathUtil.getParent(path), identity, new Usecase(up[i].getName())));
                             sb.append("\n    <user id=\"" + identity.getUsername() + "\" permission=\"" + this.authorize(PathUtil.getParent(path), identity, new Usecase(up[i].getName())) + "\"/>");
 /*
                             log.warn("DEBUG: Identity: " + idps[k].getIdentity() + ", Usecase: " + up[i].getName() + ", Permission: " + this.authorize(policy.getParentPolicy(), idps[k].getIdentity(), new Usecase(up[i].getName())));
                             sb.append("\n    <user id=\"" + idps[k].getIdentity().getUsername() + "\" permission=\"" + this.authorize(policy.getParentPolicy(), idps[k].getIdentity(), new Usecase(up[i].getName())) + "\"/>");
 */
                         } else {
                             sb.append("\n    <user id=\"" + idps[k].getIdentity().getUsername() + "\" permission=\"" + idps[k].getPermission() + "\"/>");
                         }
                     }
                     for (int k = 0; k < gps.length; k++) {
                         if (inheritPolicy && gps[k].getPermission() == false) { // TODO: Check inheritance flag of group policy
                             // TODO: Check group authorization
                             sb.append("\n    <group id=\"" + gps[k].getId() + "\" permission=\"" + true + "\"/>");
                             //sb.append("\n    <group id=\"" + gps[k].getId() + "\" permission=\"" + this.authorize(policy.getParentPolicy(), TODO, new Usecase(up[i].getName())) + "\"/>");
                         } else {
                             sb.append("\n    <group id=\"" + gps[k].getId() + "\" permission=\"" + gps[k].getPermission() + "\"/>");
                         }
                     }
                     sb.append("\n  </usecase>");
                 }
             }
 
             sb.append("\n</policy>");
 
             Node node;
             if (!repo.existsNode(policyPath)) {
                 log.warn("Create new policy: " + policyPath);              
                 node = YarepUtil.addNodes(repo, policyPath, org.wyona.yarep.core.NodeType.RESOURCE);
             } else {
                 log.warn("Policy '" + policyPath + "' already exists and hence creation request will be ignored!");              
                 node = repo.getNode(policyPath);
             }
 
             org.apache.commons.io.IOUtils.copy(new java.io.StringBufferInputStream(sb.toString()), node.getOutputStream());
         } catch(Exception e) {
             log.error(e, e);
             new java.lang.UnsupportedOperationException(e.getMessage());
         }
     }
 
     /**
      * @see
      */
     public String[] getUsecases() {
         log.warn("TODO: Implementation not finished yet! Read from configuration instead hardcoded!");
         String[] usecases = {"view", "open", "write", "resource.create", "delete", "introspection", "toolbar", "policy.read", "policy.update"};
         return usecases;
     }
 
     /**
      * @see
      */
     public String getUsecaseLabel(String usecaseId, String language) {
         log.debug("TODO: Implementation not finished yet! Read from configuration instead hardcoded!");
         if (language.equals("de")) {
             if (usecaseId.equals("view")) {
                 return "Inhalt anschauen/lesen";
             } else if (usecaseId.equals("open")) {
                 return "Inhalt zum Bearbeiten oeffnen";
             } else if (usecaseId.equals("write")) {
                 return "(Bearbeiteter) Inhalt abspeichern";
             } else if (usecaseId.equals("resource.create")) {
                 return "Inhalt neu kreieren";
             } else if (usecaseId.equals("delete")) {
                 return "Inhalt loeschen";
             } else if (usecaseId.equals("introspection")) {
                 return "Introspection anschauen/lesen";
             } else if (usecaseId.equals("toolbar")) {
                 return "Yanel Toolbar verwenden";
             } else if (usecaseId.equals("policy.read")) {
                 return "Zugriffsberechtigungen anschauen/lesen";
             } else if (usecaseId.equals("policy.update")) {
                 return "Zugriffsberechtigungen bearbeiten";
             } else {
                 return "No label for \"" + usecaseId + "\" (see " + this.getClass().getName() + ")";
             }
         } else {
             if (usecaseId.equals("view")) {
                 return "View/Read";
             } else if (usecaseId.equals("open")) {
                 return "Open content for editing";
             } else if (usecaseId.equals("write")) {
                 return "Write/Save";
             } else if (usecaseId.equals("resource.create")) {
                 return "Create a resource or a collection";
             } else if (usecaseId.equals("delete")) {
                 return "Delete a resource or a collection";
             } else if (usecaseId.equals("introspection")) {
                 return "View introspection";
             } else if (usecaseId.equals("toolbar")) {
                 return "Access Yanel toolbar";
             } else if (usecaseId.equals("policy.read")) {
                 return "View access policy";
             } else if (usecaseId.equals("policy.update")) {
                 return "Edit access policy";
             } else {
                 return "No label for \"" + usecaseId + "\" (see " + this.getClass().getName() + ")";
             }
         }
     }
     
     public Policy createEmptyPolicy() throws AuthorizationException {
         try {
             return new PolicyImplV2();
         } catch (Exception e) {
             throw new AuthorizationException(e.getMessage(), e);
         }
     }
 
     public void removePolicy(String path) throws AuthorizationException {
         Repository repo = getPoliciesRepository();
         String policyPath = getPolicyPath(path);
         try {
             if (repo.existsNode(policyPath)) {
                 repo.getNode(policyPath).delete();
             }
         } catch (RepositoryException e) {
             throw new AuthorizationException("could not remove policy for path: " + path + 
                     ": " + e.getMessage(), e);
         }
     }
 }
