 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.digt.auth.jackrabbit;
 
 import java.security.Principal;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 import javax.jcr.UnsupportedRepositoryOperationException;
 import javax.jcr.nodetype.NodeType;
 import javax.jcr.security.AccessControlEntry;
 import javax.jcr.security.AccessControlList;
 import javax.jcr.security.AccessControlManager;
 import javax.jcr.security.Privilege;
 import org.apache.jackrabbit.api.security.user.Authorizable;
 import org.apache.jackrabbit.api.security.user.AuthorizableExistsException;
 import org.apache.jackrabbit.api.security.user.Group;
 import org.apache.jackrabbit.api.security.user.Query;
 import org.apache.jackrabbit.api.security.user.User;
 import org.apache.jackrabbit.commons.JcrUtils;
 import org.apache.jackrabbit.core.SessionImpl;
 import org.apache.jackrabbit.core.security.principal.AdminPrincipal;
 import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
 import org.apache.jackrabbit.core.security.principal.PrincipalImpl;
 import org.apache.jackrabbit.core.security.user.MembershipCache;
 import org.apache.jackrabbit.core.security.user.UserManagerImpl;
 
 /**
  *
  * @author
  * wasa
  */
 public class TrustedNetUserManager extends UserManagerImpl {
 
     protected String adminId;
     private Group grpEveryone;
     private SessionImpl session;
     
     public TrustedNetUserManager(SessionImpl session, String adminId) throws RepositoryException {
         this(session, adminId, null, null);
     }
     
     public TrustedNetUserManager(SessionImpl session, String adminId, Properties config) throws RepositoryException {
         this(session, adminId, config, null);
     }
     
     public TrustedNetUserManager(SessionImpl session, String adminId, Properties config,
                            MembershipCache mCache) throws RepositoryException {
         
         super(session, adminId, config, mCache);
         this.adminId = adminId;
         this.grpEveryone = new GroupImpl(EveryonePrincipal.getInstance());
         this.session = session;
     }
     
     @Override
     public Authorizable getAuthorizable(String id) throws RepositoryException {
         Principal p;
         if (id.equals(adminId)) 
             p = new AdminPrincipal(id);
         else 
             p = new PrincipalImpl(id);
         
         return getAuthorizable(p);
     }
 
     @Override
     public Authorizable getAuthorizable(Principal principal) throws RepositoryException {
         
         HashSet userGrps = new HashSet();
         userGrps.add(grpEveryone);
         User user = new UserImpl(principal, userGrps);
         
         if (!user.isAdmin() && !"anonymous".equals(principal.getName())) {
         //    user.changePassword("admin");
             createUserHome(principal);
         }
 
         return user;
     }
     
     protected void createUserHome(Principal principal) throws RepositoryException {
         Session sess = session.getRepository().login(new SimpleCredentials(adminId, new char[0]));
        Node userHome = JcrUtils.getOrCreateByPath("/users/" 
                + HttpUtils.generateId(principal.getName()), NodeType.NT_FOLDER, sess);
         AccessControlManager aMgr = sess.getAccessControlManager();
         
         // create a privilege set with jcr:all
         Privilege[] privileges = new Privilege[] { aMgr.privilegeFromName(Privilege.JCR_ALL) };
         AccessControlList acl;
         try {
             // get first applicable policy (for nodes w/o a policy)
             acl = (AccessControlList) aMgr.getApplicablePolicies(userHome.getPath()).nextAccessControlPolicy();
         } catch (NoSuchElementException e) {
             // else node already has a policy, get that one
             acl = (AccessControlList) aMgr.getPolicies(userHome.getPath())[0];
         }
         // remove all existing entries
         for (AccessControlEntry e : acl.getAccessControlEntries()) {
             acl.removeAccessControlEntry(e);
         }
 
         // add a new one for a principal
         acl.addAccessControlEntry(principal, privileges);
 
         // the policy must be re-set
         aMgr.setPolicy(userHome.getPath(), acl);
         sess.save();
         sess.logout();
     }
 
     @Override
     public Authorizable getAuthorizableByPath(String path) throws UnsupportedRepositoryOperationException, RepositoryException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Iterator<Authorizable> findAuthorizables(String relPath, String value) throws RepositoryException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Iterator<Authorizable> findAuthorizables(String relPath, String value, int searchType) throws RepositoryException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Iterator<Authorizable> findAuthorizables(Query query) throws RepositoryException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public User createUser(String userID, String password) 
             throws AuthorizableExistsException, RepositoryException {
         
         return createUser(userID, password, new PrincipalImpl(userID), null);
     }
 
     @Override
     public User createUser(String userID, String password, Principal principal, String intermediatePath) 
             throws AuthorizableExistsException, RepositoryException {
         //TODO: implement this???
         return null;
     }
 
     @Override
     public Group createGroup(String groupID) 
             throws AuthorizableExistsException, RepositoryException {
         
     	return createGroup(groupID, new PrincipalImpl(groupID), null);
     }
 
     @Override
     public Group createGroup(Principal principal) 
             throws AuthorizableExistsException, RepositoryException {
         
         return createGroup(principal, null);
     }
 
     @Override
     public Group createGroup(Principal principal, String intermediatePath) 
             throws AuthorizableExistsException, RepositoryException {
         
         return createGroup(principal.getName(), principal, intermediatePath);
     }
 
     @Override
     public Group createGroup(String groupID, Principal principal, String intermediatePath) 
             throws AuthorizableExistsException, RepositoryException {
         
         //TODO: implement this???
         return null;
     }
 
     @Override
     public boolean isAutoSave() {
         return true;
     }
 
     @Override
     public void autoSave(boolean enable) throws UnsupportedRepositoryOperationException, RepositoryException {
     }
 
     @Override
     public void loggingOut(SessionImpl session) {
     }
 
     @Override
     public void loggedOut(SessionImpl session) {
     }
     
 }
