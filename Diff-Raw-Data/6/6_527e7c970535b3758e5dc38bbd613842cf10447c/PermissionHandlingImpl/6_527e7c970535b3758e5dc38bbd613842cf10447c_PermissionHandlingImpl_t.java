 package com.blazebit.security.impl.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import com.blazebit.security.Action;
 import com.blazebit.security.Permission;
 import com.blazebit.security.PermissionDataAccess;
 import com.blazebit.security.PermissionFactory;
 import com.blazebit.security.PermissionHandling;
 import com.blazebit.security.Resource;
 import com.blazebit.security.impl.service.resource.EntityFieldResourceHandling;
 import com.blazebit.security.impl.service.resource.EntityFieldResourceHandling.PermissionFamily;
 
 public class PermissionHandlingImpl implements PermissionHandling {
 
     @Inject
     private PermissionDataAccess permissionDataAccess;
 
     @Inject
     private EntityFieldResourceHandling resourceHandlingUtils;
 
     @Inject
     private PermissionFactory permissionFactory;
 
     @Override
     public boolean contains(Collection<Permission> permissions, Permission permission) {
         return findPermission(permissions, permission) != null;
     }
 
     @Override
     public boolean containsAll(Collection<Permission> permissions, Collection<Permission> selectedPermissions) {
         boolean contains = true;
         for (Permission permission : selectedPermissions) {
             contains = contains(permissions, permission);
             if (!contains) {
                 break;
             }
         }
         return contains;
     }
 
     @Override
     public Set<Permission> eliminateRevokeConflicts(Set<Permission> granted, Set<Permission> revoked) {
         Set<Permission> modifiedRevoked = new HashSet<Permission>(revoked);
         Set<Permission> dontRevoke = new HashSet<Permission>();
         for (Permission permission : revoked) {
             if (implies(granted, permission)) {
                 dontRevoke.add(permission);
             }
 
         }
         modifiedRevoked.removeAll(dontRevoke);
         return modifiedRevoked;
     }
 
     @Override
     public Permission findPermission(Collection<Permission> permissions, Permission givenPermission) {
         if (givenPermission == null) {
             return null;
         }
         for (Permission permission : permissions) {
             if (givenPermission.getAction().equals(permission.getAction()) && givenPermission.getResource().equals(permission.getResource())) {
                 return permission;
             }
 
         }
         return null;
     }
 
     private Set<Permission> getCommonPermissions(Set<Permission> granted, Set<Permission> revoked) {
         // Prepare a union
         Set<Permission> common = new HashSet<Permission>(granted);
         common.retainAll(revoked);
         return common;
     }
 
     @Override
     public List<Set<Permission>> getGrantable(Collection<Permission> permissions, Collection<Permission> toBeGranted) {
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         Set<Permission> granted = new HashSet<Permission>();
         Set<Permission> notGranted = new HashSet<Permission>();
 
         for (Permission selectedPermission : toBeGranted) {
             if (permissionDataAccess.isGrantable(new ArrayList<Permission>(permissions), selectedPermission.getAction(), selectedPermission.getResource())) {
                 granted.add(selectedPermission);
             } else {
                 notGranted.add(selectedPermission);
             }
         }
         ret.add(granted);
         ret.add(notGranted);
         return ret;
 
     }
 
     /**
      * Removes permissions from the given collection of permissions which imply each other.
      * 
      * @param permissions
      * @return
      */
     private Set<Permission> getMergedPermissions(Collection<Permission> permissions) {
         Set<Permission> ret = new HashSet<Permission>();
         List<List<Permission>> separatedPermissions = resourceHandlingUtils.getSeparatedPermissionsByResource(permissions);
         for (List<Permission> permissionList : separatedPermissions) {
             ret.addAll(mergePermissions(permissionList));
         }
         return ret;
     }
 
     /**
      * Filters out permissions that imply each other. The given collection of permissions belongs to a subject or a role.
      * 
      * @param permissions
      * @return
      */
     private Set<Permission> getNonRedundantPermissions(Collection<Permission> permissions) {
         Set<Permission> redundantPermissions = new HashSet<Permission>();
         Set<Permission> currentPermissions = new HashSet<Permission>(permissions);
         for (Permission permission : currentPermissions) {
             if (!contains(redundantPermissions, permission)) {
                 // remove current one
                 currentPermissions = new HashSet<Permission>(permissions);
                 currentPermissions.remove(permission);
                 // check if any other permission is implied by this permission
                 if (implies(currentPermissions, permission)) {
                     redundantPermissions.add(permission);
                 }
             }
         }
         currentPermissions = new HashSet<Permission>(permissions);
         currentPermissions.removeAll(redundantPermissions);
         return currentPermissions;
     }
 
     @Override
     public Set<Permission> getNormalizedPermissions(Collection<Permission> permissions) {
         return getNonRedundantPermissions(getMergedPermissions(permissions));
     }
 
     /**
      * 
      * @param permissions
      * @return
      */
     @Override
     public Set<Permission> getParentPermissions(Collection<Permission> permissions) {
         return getNormalizedPermissions(resourceHandlingUtils.getParentPermissions(permissions));
     }
 
     @Override
     public Set<Permission> getReplacedByGranting(Collection<Permission> permissions, Collection<Permission> granted) {
         Set<Permission> revoked = new HashSet<Permission>();
 
         for (Permission selectedPermission : granted) {
             revoked.addAll(permissionDataAccess.getRevokablePermissionsWhenGranting(new ArrayList<Permission>(permissions), selectedPermission.getAction(),
                                                                                     selectedPermission.getResource()));
         }
         return revoked;
     }
 
     @Override
     public Set<Permission> getReplacedByRevoking(Collection<Permission> permissions, Collection<Permission> revoked) {
         Set<Permission> replaceable = new HashSet<Permission>();
         for (Permission grantedPermission : revoked) {
             replaceable.addAll(permissionDataAccess.getRevokablePermissionsWhenRevoking(new ArrayList<Permission>(permissions), grantedPermission.getAction(),
                                                                                         grantedPermission.getResource()));
         }
         return replaceable;
     }
 
     @Override
     public List<Set<Permission>> getRevokableFromRevoked(Collection<Permission> permissions, Collection<Permission> toBeRevoked) {
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
 
         Set<Permission> revoked = new HashSet<Permission>();
         Set<Permission> notRevoked = new HashSet<Permission>();
 
         for (Permission selectedPermission : toBeRevoked) {
             if (permissionDataAccess.isRevokable(new ArrayList<Permission>(permissions), selectedPermission.getAction(), selectedPermission.getResource())) {
                 revoked.add(selectedPermission);
             } else {
                 notRevoked.add(selectedPermission);
             }
         }
         ret.add(revoked);
         ret.add(notRevoked);
         return ret;
     }
 
     @Override
     public List<Set<Permission>> getRevokableFromRevoked(Collection<Permission> permissions, Collection<Permission> toBeRevoked, boolean force) {
         if (!force) {
             return getRevokableFromRevoked(permissions, toBeRevoked);
         } else {
             List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
 
             Set<Permission> revoked = new HashSet<Permission>();
             Set<Permission> notRevoked = new HashSet<Permission>();
             Set<Permission> granted = new HashSet<Permission>();
 
             for (Permission selectedPermission : toBeRevoked) {
                 if (permissionDataAccess.isRevokable(new ArrayList<Permission>(permissions), selectedPermission.getAction(), selectedPermission.getResource())) {
                     revoked.addAll(permissionDataAccess.getRevokablePermissionsWhenRevoking(new ArrayList<Permission>(permissions), selectedPermission.getAction(),
                                                                                             selectedPermission.getResource()));
                 } else {
                     if (findPermission(granted, selectedPermission) != null) {
                         granted = new HashSet<Permission>(remove(granted, selectedPermission));
                     } else {
                         Resource resource = selectedPermission.getResource();
                         if (!resource.getParent().equals(resource)) {
                             // if parent permission present
                             Permission parentPermission = permissionDataAccess.findPermission(new ArrayList<Permission>(permissions), selectedPermission.getAction(),
                                                                                               resource.getParent());
                             if (parentPermission != null) {
                                 // revoke parent entity
                                 revoked.add(parentPermission);
                                 Set<Permission> childPermissions = resourceHandlingUtils.getChildPermissions(parentPermission);
                                 childPermissions = new HashSet<Permission>(remove(childPermissions, selectedPermission));
                                 granted.addAll(childPermissions);
                             } else {
                                 notRevoked.add(selectedPermission);
                             }
                         } else {
                             notRevoked.add(selectedPermission);
                         }
                     }
 
                 }
             }
 
             ret.add(revoked);
             ret.add(notRevoked);
             ret.add(granted);
 
             return ret;
         }
     }
 
     @Override
     public List<Set<Permission>> getRevokableFromSelected(Collection<Permission> permissions, Collection<Permission> selectedPermissions) {
         Set<Permission> toBeRevoked = getRevoked(permissions, selectedPermissions);
         return getRevokableFromRevoked(permissions, toBeRevoked);
     }
 
     private Set<Permission> getRevoked(Collection<Permission> permissions, Collection<Permission> selectedPermissions) {
         Set<Permission> ret = new HashSet<Permission>();
 
         for (Permission currentPermission : permissions) {
             if (!implies(selectedPermissions, currentPermission)) {
                 ret.add(currentPermission);
             }
         }
         return ret;
     }
 
     /**
      * Reconsideres permissions to be granted and revoked based on the current permissions. For example: if subject/role has
      * entity field permissions and the missing entity field permissions are granted -> the existing entity field permissions
      * will be revoked and the entity permission will be granted.
      * 
      * @param permissions
      * @return set of non redundant permissions
      */
     @Override
     public List<Set<Permission>> getRevokedAndGrantedAfterMerge(Collection<Permission> current, Set<Permission> revoked, Set<Permission> granted) {
         List<Permission> currentPermissions = new ArrayList<Permission>(current);
         Set<Permission> finalGranted = new HashSet<Permission>(granted);
         Set<Permission> finalRevoked = new HashSet<Permission>(revoked);
         // simulate revoke
         for (Permission revoke : revoked) {
             Set<Permission> removablePermissions = permissionDataAccess.getRevokablePermissionsWhenRevoking(new ArrayList<Permission>(current), revoke.getAction(),
                                                                                                             revoke.getResource());
             for (Permission permission : removablePermissions) {
                 currentPermissions.remove(permission);
             }
         }
         // simulate grant
         for (Permission grant : granted) {
             Set<Permission> removablePermissions = permissionDataAccess.getRevokablePermissionsWhenGranting(new ArrayList<Permission>(current), grant.getAction(),
                                                                                                             grant.getResource());
 
             for (Permission existingPermission : removablePermissions) {
                 currentPermissions.remove(existingPermission);
             }
             currentPermissions.add(grant);
         }
 
         List<Permission> actualAfterGrantAndRevoke = new ArrayList<Permission>(currentPermissions);
         currentPermissions = new ArrayList<Permission>(actualAfterGrantAndRevoke);
 
         Set<Permission> merged = getMergedPermissions(currentPermissions);
 
         Set<Permission> expectedAfterGrantAndRevoke = getNonRedundantPermissions(merged);
 
         for (Permission permission : expectedAfterGrantAndRevoke) {
             if (!contains(actualAfterGrantAndRevoke, permission)) {
                 finalGranted.add(permission);
             } else {
                 actualAfterGrantAndRevoke.remove(permission);
             }
         }
 
         if (!actualAfterGrantAndRevoke.isEmpty()) {
             finalRevoked.addAll(actualAfterGrantAndRevoke);
         }
 
         Set<Permission> common = getCommonPermissions(finalGranted, finalRevoked);
         finalGranted.removeAll(common);
         finalRevoked.removeAll(common);
 
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         ret.add(finalRevoked);
         ret.add(finalGranted);
         return ret;
     }
 
     /**
     * Separates the parent and the child permissions of permission collection. 
      * 
      * @param permissions
      * @return
      */
     @Override
     public List<Set<Permission>> getSeparatedParentAndChildPermissions(Collection<Permission> permissions) {
         Set<Permission> parents = new HashSet<Permission>();
         Set<Permission> children = new HashSet<Permission>();
         for (Permission permission : permissions) {
            if (permission.getResource().getParent().equals(permission.getResource())) {
                 parents.add(permission);
             } else {
                 children.add(permission);
             }
         }
         List<Set<Permission>> ret = new ArrayList<Set<Permission>>();
         ret.add(parents);
         ret.add(children);
         return ret;
     }
 
     /**
      * Separates the parent and the child permissions of permission collection. Requirement: all permissions belong to the same
      * resource name.
      * 
      * @param permissions
      * @return
      */
     @Override
     public List<List<Permission>> getSeparatedPermissions(Collection<Permission> permissions) {
         return resourceHandlingUtils.getSeparatedPermissionsByResource(permissions);
     }
 
     @Override
     public boolean implies(Collection<Permission> permissions, Permission givenPermission) {
         return !permissionDataAccess.isGrantable(new ArrayList<Permission>(permissions), givenPermission.getAction(), givenPermission.getResource());
     }
 
     /**
      * if all fields present->merges permissions into entity permission
      * 
      * @param permissions
      * @return
      */
     private Set<Permission> mergePermissions(Collection<Permission> permissions) {
         Set<Permission> remove = new HashSet<Permission>();
         Set<Permission> add = new HashSet<Permission>();
 
         Set<Permission> ret = new HashSet<Permission>(permissions);
 
         Map<String, List<Permission>> permissionsByEntity = resourceHandlingUtils.groupPermissionsByResourceName(permissions);
 
         for (String entity : permissionsByEntity.keySet()) {
             Map<Action, List<Permission>> entityPermissionsByAction = resourceHandlingUtils.groupResourcePermissionsByAction(permissionsByEntity.get(entity));
 
             for (Action action : entityPermissionsByAction.keySet()) {
                 PermissionFamily permissionFamily = resourceHandlingUtils.getSeparatedParentAndChildEntityPermissions(entityPermissionsByAction.get(action));
                 if (permissionFamily.parent != null) {
                     // both entity resource and some or all entity fields exist -> merge into entity resource permission
                     remove.addAll(permissionFamily.children);
                 } else {
                     if (!permissionFamily.children.isEmpty()) {
                         Permission parentPermission = permissionFactory.create(action, permissionFamily.children.iterator().next().getResource().getParent());
                         // all PRIMITIVE entity fields are listed -> merge into entity resource permission
                         Set<Permission> childPermissions = resourceHandlingUtils.getChildPermissions(parentPermission);
                         boolean foundMissingField = !containsAll(permissionFamily.children, childPermissions);
                         if (!foundMissingField) {
                             remove.addAll(permissionFamily.children);
                             add.add(parentPermission);
                         }
 
                     }
                 }
             }
         }
         ret.removeAll(remove);
         ret.addAll(add);
         return ret;
     }
 
     /**
      * removes given permission from a permission collection (collection can contain user and usergroup permissions as well)
      * 
      * @param permissions
      * @param permission
      * @return
      */
     @Override
     public Collection<Permission> remove(Collection<Permission> permissions, Permission permission) {
         Permission found = findPermission(permissions, permission);
         permissions.remove(found);
         return new ArrayList<Permission>(permissions);
     }
 
     @Override
     public Collection<Permission> removeAll(Collection<Permission> permissions, Collection<Permission> permissionsToRemove) {
         Set<Permission> ret = new HashSet<Permission>(permissions);
         for (Permission permission : permissionsToRemove) {
             Permission found = findPermission(ret, permission);
             ret.remove(found);
         }
         return ret;
     }
 
     @Override
     public boolean replaces(Collection<Permission> permissions, Permission givenPermission) {
         return !permissionDataAccess
             .getRevokablePermissionsWhenGranting(new ArrayList<Permission>(permissions), givenPermission.getAction(), givenPermission.getResource())
             .isEmpty();
     }
 
 }
