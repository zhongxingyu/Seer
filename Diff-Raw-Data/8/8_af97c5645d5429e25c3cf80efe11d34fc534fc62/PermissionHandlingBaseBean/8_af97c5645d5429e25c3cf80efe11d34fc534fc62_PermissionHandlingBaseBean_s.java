 /*
  * To change this template, choose Tools | Templates and open the template in the editor.
  */
 package com.blazebit.security.web.bean.base;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 
 import com.blazebit.security.ActionFactory;
 import com.blazebit.security.EntityResourceFactory;
 import com.blazebit.security.Permission;
 import com.blazebit.security.PermissionDataAccess;
 import com.blazebit.security.PermissionFactory;
 import com.blazebit.security.PermissionManager;
 import com.blazebit.security.PermissionService;
 import com.blazebit.security.ResourceFactory;
 import com.blazebit.security.Role;
 import com.blazebit.security.Subject;
 import com.blazebit.security.impl.context.UserContext;
 import com.blazebit.security.impl.service.PermissionHandlingImpl;
 import com.blazebit.security.impl.utils.ActionUtils;
 import com.blazebit.security.metamodel.ResourceMetamodel;
 import com.blazebit.security.web.bean.main.DialogBean;
 import com.blazebit.security.web.bean.model.TreeNodeModel.Marking;
 import com.blazebit.security.web.context.UserSession;
 
 /**
  * 
  * @author cuszk
  */
 @ViewScoped
 @ManagedBean(name = "permissionHandlingBaseBean")
 @Named
 public class PermissionHandlingBaseBean extends PermissionTreeHandlingBaseBean {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     @Inject
     protected PermissionFactory permissionFactory;
     @Inject
     protected UserSession userSession;
     @Inject
     protected UserContext userContext;
     @Inject
     protected PermissionDataAccess permissionDataAccess;
     @Inject
     protected PermissionManager permissionManager;
     @Inject
     protected EntityResourceFactory entityFieldFactory;
     @Inject
     protected ResourceFactory resourceFactory;
     @Inject
     protected ActionFactory actionFactory;
     @Inject
     protected PermissionService permissionService;
     @Inject
     protected ActionUtils actionUtils;
     @Inject
     protected ResourceMetamodel resourceMetamodel;
     @Inject
     protected PermissionHandlingImpl permissionHandling;
     @Inject
     protected DialogBean dialogBean;
 
     /**
      * 
      * @param allPermissions
      * @param selectedPermissions
      * @param prevRevoked
      * @param prevReplaced
      * @param hideFieldLevel
      * @return
      */
     protected TreeNode rebuildCurrentTree(List<Permission> allPermissions, Set<Permission> selectedPermissions, Set<Permission> prevRevoked, Set<Permission> prevReplaced, boolean hideFieldLevel) {
         DefaultTreeNode root = new DefaultTreeNode();
         return rebuildCurrentTree(root, allPermissions, selectedPermissions, prevRevoked, prevReplaced, hideFieldLevel);
     }
 
     /**
      * 
      * @param node
      * @param allPermissions
      * @param selectedPermissions
      * @param prevRevoked
      * @param prevReplaced
      * @param hideFieldLevel
      * @return
      */
     protected TreeNode rebuildCurrentTree(TreeNode node, List<Permission> allPermissions, Set<Permission> selectedPermissions, Set<Permission> prevRevoked, Set<Permission> prevReplaced, boolean hideFieldLevel) {
         List<Permission> userPermissions = resourceUtils.getSeparatedPermissionsByResource(allPermissions).get(0);
         List<Permission> userDataPermissions = resourceUtils.getSeparatedPermissionsByResource(allPermissions).get(1);
 
         // add back previously replaced
         for (Permission replacedPermission : prevReplaced) {
             if (!permissionHandling.implies(selectedPermissions, replacedPermission)) {
                 selectedPermissions.add(replacedPermission);
             }
         }
         Set<Permission> revoked = new HashSet<Permission>();
         // add back previously revoked
         for (Permission revokedPermission : prevRevoked) {
             if (!permissionHandling.implies(selectedPermissions, revokedPermission)) {
                 revoked.add(revokedPermission);
             }
         }
         Set<Permission> replaced = permissionHandling.getReplacedByGranting(allPermissions, selectedPermissions);
 
        List<Set<Permission>> revoke = permissionHandling.getRevokableFromSelected(allPermissions, concat(userPermissions, selectedPermissions));
         revoked.addAll(revoke.get(0));
         dialogBean.setNotRevoked(revoke.get(1));
 
         Set<Permission> removablePermissions = new HashSet<Permission>();
         removablePermissions.addAll(revoked);
         removablePermissions.addAll(replaced);
         // current permission tree
 
         return getImmutablePermissionTree(node, userPermissions, userDataPermissions, removablePermissions, new HashSet<Permission>(), Marking.REMOVED, Marking.NONE,
                                           hideFieldLevel);
     }
 
     protected Set<Permission> concat(Collection<Permission> current, Collection<Permission> added) {
         Set<Permission> ret = new HashSet<Permission>();
         ret.addAll(current);
         ret.addAll(added);
         return ret;
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Role role, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced) {
         return executeRevokeAndGrant(role, current, selected, prevRevoked, prevReplaced, false);
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Role role, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced, boolean simulate) {
         Set<Permission> revoked = new HashSet<Permission>();
         // add back previous revoked permisions
         for (Permission permission : prevRevoked) {
             if (!permissionHandling.implies(selected, permission)) {
                 revoked.add(permission);
             }
         }
         // add back previous replaced permisssion if no overriding permission exists in the current selected ones
         for (Permission permission : prevReplaced) {
             if (!permissionHandling.implies(selected, permission)) {
                 selected.add(permission);
             }
         }
 
         revoked.addAll(permissionHandling.getRevokableFromSelected(current, concat(current, selected)).get(0));
         Set<Permission> granted = permissionHandling.getGrantable(permissionHandling.removeAll(current, revoked), selected).get(0);
         return performOperations(role, current, revoked, granted, simulate);
 
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Subject subject, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced) {
         return executeRevokeAndGrant(subject, current, selected, prevRevoked, prevReplaced, false);
     }
 
     protected List<Set<Permission>> executeRevokeAndGrant(Subject subject, Collection<Permission> current, Set<Permission> selected, Set<Permission> prevRevoked, Set<Permission> prevReplaced, boolean simulate) {
         Set<Permission> revoked = new HashSet<Permission>();
         // add back previous revoked permisions
         for (Permission permission : prevRevoked) {
             if (!permissionHandling.implies(selected, permission)) {
                 revoked.add(permission);
             }
         }
         // add back previous replaced permisssion if no overriding permission exists in the current selected ones
         for (Permission permission : prevReplaced) {
             if (!permissionHandling.implies(selected, permission)) {
                 selected.add(permission);
             }
         }
 
         revoked.addAll(permissionHandling.getRevokableFromSelected(current, concat(current, selected)).get(0));
         Set<Permission> granted = permissionHandling.getGrantable(permissionHandling.removeAll(current, revoked), selected).get(0);
         return performOperations(subject, current, revoked, granted, simulate);
 
     }
 
     protected List<Set<Permission>> performOperations(Subject subject, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted) {
         return performOperations(subject, current, revoked, granted, false);
     }
 
     protected List<Set<Permission>> performOperations(Subject subject, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted, boolean simulate) {
         List<Set<Permission>> permissions = permissionHandling.getRevokedAndGrantedAfterMerge(current, revoked, granted);
         Set<Permission> finalRevoked = permissions.get(0);
         Set<Permission> finalGranted = permissions.get(1);
         return revokeAndGrant(userContext.getUser(), subject, finalRevoked, finalGranted, simulate);
 
     }
 
     protected List<Set<Permission>> revokeAndGrant(Subject subject, Set<Permission> finalRevoked, Set<Permission> finalGranted) {
         return revokeAndGrant(userContext.getUser(), subject, finalRevoked, finalGranted, false);
     }
 
     protected List<Set<Permission>> revokeAndGrant(Subject authorizer, Subject subject, Set<Permission> finalRevoked, Set<Permission> finalGranted) {
         return revokeAndGrant(authorizer, subject, finalRevoked, finalGranted, false);
     }
 
     protected List<Set<Permission>> revokeAndGrant(Subject authorizer, Subject subject, Set<Permission> finalRevoked, Set<Permission> finalGranted, boolean simulate) {
         if (!simulate) {
             if (subject.equals(authorizer)) {
                 authorizer = userSession.getAdmin();
             }
             permissionService.revokeAndGrant(authorizer, subject, finalRevoked, finalGranted);
         }
 
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         ret.add(finalRevoked);
         ret.add(finalGranted);
         return ret;
     }
 
     protected List<Set<Permission>> performOperations(Role role, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted) {
         return performOperations(role, current, revoked, granted, false);
     }
 
     protected List<Set<Permission>> performOperations(Role role, Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted, boolean simulate) {
         List<Set<Permission>> permissions = permissionHandling.getRevokedAndGrantedAfterMerge(current, revoked, granted);
         Set<Permission> finalRevoked = permissions.get(0);
         Set<Permission> finalGranted = permissions.get(1);
         return revokeAndGrant(role, finalRevoked, finalGranted, simulate);
     }
 
     protected List<Set<Permission>> revokeAndGrant(Role role, Set<Permission> finalRevoked, Set<Permission> finalGranted, boolean simulate) {
         if (!simulate) {
             permissionService.revokeAndGrant(userContext.getUser(), role, finalRevoked, finalGranted);
         }
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         ret.add(finalRevoked);
         ret.add(finalGranted);
         return ret;
     }
 
 }
