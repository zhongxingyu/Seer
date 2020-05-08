 /*
  * To change this template, choose Tools | Templates and open the template in the editor.
  */
 package com.blazebit.security.web.bean.main.user;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 import org.primefaces.event.FlowEvent;
 import org.primefaces.model.DefaultTreeNode;
 import org.primefaces.model.TreeNode;
 
 import com.blazebit.security.Permission;
 import com.blazebit.security.impl.model.Company;
 import com.blazebit.security.impl.model.User;
 import com.blazebit.security.impl.model.UserGroup;
 import com.blazebit.security.web.bean.base.GroupHandlingBaseBean;
 import com.blazebit.security.web.bean.model.UserGroupModel;
 
 /**
  * 
  * @author cuszk
  */
 @ViewScoped
 @ManagedBean(name = "userGroupsBean")
 public class UserGroupsBean extends GroupHandlingBaseBean {
 
     private static final long serialVersionUID = 1L;
 
     private List<Permission> userPermissions = new ArrayList<Permission>();
     private List<Permission> userDataPermissions = new ArrayList<Permission>();
     private List<Permission> allPermissions = new ArrayList<Permission>();
 
     // group tree
     private TreeNode[] selectedGroupNodes = new TreeNode[] {};
     private DefaultTreeNode groupRoot = new DefaultTreeNode();
     private List<UserGroupModel> groups = new ArrayList<UserGroupModel>();
     // permission view tree
     private TreeNode permissionViewRoot = new DefaultTreeNode();
     // selected group to view group permissions
     private UserGroup selectedGroup;
 
     // after group selection
     private Set<UserGroup> addedGroups = new HashSet<UserGroup>();
     private Set<UserGroup> removedGroups = new HashSet<UserGroup>();
 
     private Set<Permission> replaced = new HashSet<Permission>();
     private Set<Permission> revokable = new HashSet<Permission>();
 
     // compare 2 permission trees
     private TreeNode currentPermissionTreeRoot = new DefaultTreeNode();
     private TreeNode newPermissionTreeRoot = new DefaultTreeNode();
     private TreeNode newObjectPermissionTreeRoot = new DefaultTreeNode();
     // select from the new permission tree
     private TreeNode[] selectedPermissionNodes = new TreeNode[] {};
     private TreeNode[] selectedObjectPermissionNodes = new TreeNode[] {};
 
     private Map<UserGroup, List<Permission>> groupPermissionsMap = new HashMap<UserGroup, List<Permission>>();
 
     private boolean permissionTreeSwitch;
 
     public void init() {
         initUserGroups();
         initUserPermissions();
     }
 
     /**
      * wizard listener
      * 
      * @param event
      * @return
      */
     public String groupWizardListener(FlowEvent event) {
         if (event.getOldStep().equals("groups")) {
             processSelectedGroups();
         }
         if (event.getOldStep().equals("groupPermissions") && event.getNewStep().equals("groups")) {
             init();
         }
         return event.getNewStep();
     }
 
     private void initUserPermissions() {
         allPermissions = permissionManager.getPermissions(getSelectedUser());
         List<List<Permission>> permissions = resourceUtils.getSeparatedPermissionsByResource(allPermissions);
         userPermissions = permissions.get(0);
         userDataPermissions = permissions.get(1);
         this.permissionViewRoot = getImmutablePermissionTree(userPermissions, userDataPermissions, !isEnabled(Company.FIELD_LEVEL));
     }
 
     private void initUserGroups() {
         List<UserGroup> currentUserGroups = userGroupDataAccess.getGroupsForUser(getSelectedUser());
         if (isEnabled(Company.GROUP_HIERARCHY)) {
             selectedGroupNodes = new TreeNode[] {};
             List<UserGroup> parentGroups = userGroupDataAccess.getAllParentGroups(userContext.getUser().getCompany());
             storeGroupPermissionMap(parentGroups);
             this.groupRoot = getGroupTree(parentGroups, currentUserGroups);
         } else {
             this.groups.clear();
             List<UserGroup> result = userGroupDataAccess.getAllGroups(userContext.getUser().getCompany());
             for (UserGroup userGroup : result) {
                 this.groups.add(new UserGroupModel(userGroup, false, currentUserGroups.contains(userGroup)));
             }
         }
     }
 
     private void storeGroupPermissionMap(List<UserGroup> parentGroups) {
         for (UserGroup userGroup : parentGroups) {
             storeChildGroupPermissions(userGroup);
         }
     }
 
     private void storeChildGroupPermissions(UserGroup userGroup) {
         groupPermissionsMap.put(userGroup, permissionManager.getPermissions(userGroup));
         for (UserGroup child : userGroupDataAccess.getGroupsForGroup(userGroup)) {
             storeChildGroupPermissions(child);
         }
     }
 
     /**
      * wizard step: process selected groups
      */
     public void processSelectedGroups() {
         selectedPermissionNodes = new TreeNode[] {};
         currentPermissionTreeRoot = new DefaultTreeNode();
         // store added and removed groups for later processing
         List<Set<UserGroup>> addedAndRemovedGroups = userGroupDataAccess.getAddedAndRemovedUserGroups(getSelectedUser(), getSelectedGroups());
         addedGroups = addedAndRemovedGroups.get(0);
         removedGroups = addedAndRemovedGroups.get(1);
 
         // get granted and revoked permissions from added and removed groups
         Set<Permission> granted = getPermissionsToGrant();
         Set<Permission> revoked = getPermissionsToRevoke(granted);
 
         // get permissions which can be revoked from the user
         List<Set<Permission>> revoke = permissionHandling.getRevokableFromRevoked(allPermissions, revoked, true);
         revokable = revoke.get(0);
         dialogBean.setNotRevoked(revoke.get(1));
 
         // get permissions which can be granted to the user
         List<Set<Permission>> grant = permissionHandling.getGrantable(permissionHandling.removeAll(allPermissions, revokable), granted);
         Set<Permission> grantable = grant.get(0);
         dialogBean.setNotGranted(grant.get(1));
 
         Set<Permission> additionalGranted = revoke.get(2);
         grantable.addAll(additionalGranted);
         grantable = permissionHandling.getNormalizedPermissions(grantable);
 
         // current permission tree
         replaced = permissionHandling.getReplacedByGranting(allPermissions, grantable);
 
         currentPermissionTreeRoot = buildCurrentPermissionTree(userPermissions, userDataPermissions, grantable, revokable, replaced, !isEnabled(Company.FIELD_LEVEL));
 
         newPermissionTreeRoot = buildNewPermissionTree(userPermissions, Collections.<Permission>emptyList(),
                                                        new HashSet<Permission>(permissionHandling.getSeparatedPermissions(grantable).get(0)), revokable, replaced,
                                                        !isEnabled(Company.FIELD_LEVEL), isEnabled(Company.USER_LEVEL), false);
         newObjectPermissionTreeRoot = buildNewDataPermissionTree(Collections.<Permission>emptyList(), userDataPermissions, new HashSet<Permission>(permissionHandling
                                                                      .getSeparatedPermissions(grantable)
                                                                      .get(1)), new HashSet<Permission>(permissionHandling.getSeparatedPermissions(revokable).get(1)), replaced,
                                                                  !isEnabled(Company.FIELD_LEVEL),
                                                                  isEnabled(Company.USER_LEVEL));
     }
 
     private Set<Permission> getPermissionsToRevoke(Set<Permission> granted) {
         Set<Permission> revoked = groupPermissionHandling.getGroupPermissions(removedGroups, isEnabled(Company.GROUP_HIERARCHY));
         // if field level is not enabled dont revoke field permissions
         if (!isEnabled(Company.FIELD_LEVEL)) {
             revoked = permissionHandling.getSeparatedParentAndChildPermissions(revoked).get(0);
         }
         // if object level is not enabled, just ignore object level permissions
         if (!isEnabled(Company.OBJECT_LEVEL)) {
             revoked = new HashSet<Permission>(permissionHandling.getSeparatedPermissions(revoked).get(0));
         }
         revoked = permissionHandling.eliminateRevokeConflicts(granted, revoked);
         return revoked;
     }
 
     private Set<Permission> getPermissionsToGrant() {
         Set<Permission> granted = groupPermissionHandling.getGroupPermissions(getSelectedGroups(), isEnabled(Company.GROUP_HIERARCHY));
         if (!isEnabled(Company.FIELD_LEVEL)) {
             granted = permissionHandling.getSeparatedParentAndChildPermissions(granted).get(0);
         }
         // if object level is not enabled, just ignore object level permissions
         if (!isEnabled(Company.OBJECT_LEVEL)) {
             granted = new HashSet<Permission>(permissionHandling.getSeparatedPermissions(granted).get(0));
         }
         return granted;
     }
 
     private Set<UserGroup> getSelectedGroups() {
         Set<UserGroup> ret = new HashSet<UserGroup>();
         if (isEnabled(Company.GROUP_HIERARCHY)) {
             for (TreeNode treeNode : selectedGroupNodes) {
                 ret.add(((UserGroupModel) treeNode.getData()).getUserGroup());
             }
         } else {
             for (UserGroupModel model : groups) {
                 if (model.isSelected()) {
                     ret.add(model.getUserGroup());
                 }
             }
         }
         return ret;
     }
 
     /**
      * listener for select unselect permissons in the new permission tree
      */
     public void rebuildCurrentPermissionTreeSelect(org.primefaces.event.NodeSelectEvent event) {
         TreeNode selectedNode = event.getTreeNode();
         selectChildrenInstances(selectedNode, true);
         rebuildCurrentPermissionTree();
     }
 
     public void rebuildCurrentPermissionTreeUnselect(org.primefaces.event.NodeUnselectEvent event) {
         TreeNode selectedNode = event.getTreeNode();
         selectChildrenInstances(selectedNode, false);
         rebuildCurrentPermissionTree();
     }
 
     public void rebuildCurrentPermissionTree() {
         Set<Permission> selectedPermissions = getSelectedPermissions(selectedPermissionNodes);
         selectedPermissions.addAll(getSelectedPermissions(selectedObjectPermissionNodes));
         currentPermissionTreeRoot = rebuildCurrentTree(allPermissions, selectedPermissions, revokable, replaced, !isEnabled(Company.FIELD_LEVEL));
     }
 
     /**
      * confirm button
      */
     public void confirm() {
         Set<Permission> selectedPermissions = getSelectedPermissions(selectedPermissionNodes);
         selectedPermissions.addAll(getSelectedPermissions(selectedObjectPermissionNodes));
         executeRevokeAndGrant(getSelectedUser(), allPermissions, selectedPermissions, revokable, replaced);
 
         for (UserGroup group : removedGroups) {
             userGroupService.removeUserFromGroup(getSelectedUser(), group);
         }
         for (UserGroup group : addedGroups) {
             userGroupService.addUserToGroup(getSelectedUser(), group);
         }
         init();
     }
 
     public TreeNode getPermissionViewRoot() {
         return permissionViewRoot;
     }
 
     public TreeNode[] getSelectedGroupNodes() {
         return selectedGroupNodes;
     }
 
     public void setSelectedGroupNodes(TreeNode[] selectedGroupNodes) {
         this.selectedGroupNodes = selectedGroupNodes;
     }
 
     public DefaultTreeNode getGroupRoot() {
         return groupRoot;
     }
 
     public User getSelectedUser() {
         return userSession.getSelectedUser();
     }
 
     public TreeNode getCurrentPermissionTreeRoot() {
         return currentPermissionTreeRoot;
     }
 
     public TreeNode getNewPermissionTreeRoot() {
         return newPermissionTreeRoot;
     }
 
     public TreeNode[] getSelectedPermissionNodes() {
         return selectedPermissionNodes;
     }
 
     public void setSelectedPermissionNodes(TreeNode[] selectedPermissions) {
         this.selectedPermissionNodes = selectedPermissions;
     }
 
     public void setSelectedGroup(UserGroup selectedGroup) {
         this.selectedGroup = selectedGroup;
     }
 
     // dialog
     public List<Permission> getSelectedGroupPermissions() {
         if (selectedGroup != null) {
             return permissionManager.getPermissions(selectedGroup);
         } else {
             return new ArrayList<Permission>();
         }
     }
 
     public List<UserGroupModel> getGroups() {
         return groups;
     }
 
     public void setGroups(List<UserGroupModel> groups) {
         this.groups = groups;
     }
 
     public boolean isPermissionTreeSwitch() {
         return permissionTreeSwitch;
     }
 
     public void setPermissionTreeSwitch(boolean permissionTreeSwitch) {
         this.permissionTreeSwitch = permissionTreeSwitch;
     }
 
     public TreeNode getNewObjectPermissionTreeRoot() {
         return newObjectPermissionTreeRoot;
     }
 
     public void setNewObjectPermissionTreeRoot(TreeNode newObjectPermissionTreeRoot) {
         this.newObjectPermissionTreeRoot = newObjectPermissionTreeRoot;
     }
 
     public TreeNode[] getSelectedObjectPermissionNodes() {
         return selectedObjectPermissionNodes;
     }
 
     public void setSelectedObjectPermissionNodes(TreeNode[] selectedObjectPermissionNodes) {
         this.selectedObjectPermissionNodes = selectedObjectPermissionNodes;
     }
 
 }
