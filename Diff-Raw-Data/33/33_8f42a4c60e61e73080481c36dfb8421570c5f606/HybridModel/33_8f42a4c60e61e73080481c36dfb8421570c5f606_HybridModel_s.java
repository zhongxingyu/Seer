 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.rim.cso.picketlinktest;
 
 import java.util.List;
 import net.rim.cso.picketlinktest.model.LdapUser;
 import org.picketlink.idm.IdentityManager;
 import org.picketlink.idm.RelationshipManager;
 import org.picketlink.idm.model.Account;
 import org.picketlink.idm.model.IdentityType;
 import org.picketlink.idm.model.basic.Group;
 import org.picketlink.idm.model.basic.GroupMembership;
 import org.picketlink.idm.model.basic.Role;
 import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
 import static org.picketlink.idm.IDMMessages.MESSAGES;
 import org.picketlink.idm.IdentityManagementException;
 import org.picketlink.idm.model.basic.Grant;
 import org.picketlink.idm.model.basic.GroupRole;
 import org.picketlink.idm.model.basic.User;
 import org.picketlink.idm.query.IdentityQuery;
 import org.picketlink.idm.query.RelationshipQuery;
 
 /**
  * <p>This class provides a number of static convenience methods for looking up identities from the basic identity
  * model.</p>
  * @author jpicklyk
  */
 public class HybridModel {
 
     public HybridModel() {
     }
     
     /**
      * <p> Adds the given {@link Account} as a member of the provided {@link Group}. </p>
      *
      * @param relationshipManager
      * @param member A previously loaded {@link Account} instance.
      * @param group A previously loaded {@link Group} instance.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static void addToGroup(RelationshipManager relationshipManager, Account member, Group group) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (member == null) {
             throw MESSAGES.nullArgument("Account");
         }
 
         if (group == null) {
             throw MESSAGES.nullArgument("Group");
         }
         
         relationshipManager.add(new GroupMembership(member,group));
     }
     
     /**
      * <p> Returns a {@link Group} instance with the specified <code>groupPath</code>. Eg.: /groupA/groupB/groupC. </p>
      *
      * @param identityManager The {@link IdentityManager}
      * @param groupPath The group's path or its name only without the group separator. In this last case, the group
      * returned will be the root group. Eg.: Administrators == /Administrators.
      *
      * @return An {@link Group} instance or null if the <code>groupPath</code> is null or an empty string.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static Group getGroup(IdentityManager identityManager, String groupPath) throws IdentityManagementException {
          if(isNullOrEmpty(groupPath))
             return null;
          if(!groupPath.startsWith("/")) {
              groupPath = "/" +groupPath;
          }
          
          String[] paths = groupPath.split("/");
          if(paths.length > 0) {
              String name = paths[paths.length - 1];
              IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
              query.setParameter(Group.NAME, name);
              List<Group> result = query.getResultList();
              
              for(Group group : result) {
                  if(group.getPath().equals(groupPath)) {
                      return group;
                  }
              }
          }
         return null;
     }
     /**
      * <p> Returns the {@link Group} with the given <code>groupName</code> and child of the given <code>parent</code>
      * {@link Group}. </p>
      *
      * @param identityManager The {@link IdentityManager}
      * @param groupName The group's name.
      * @param parent A {@link Group} instance with a valid identifier or null. In this last case, the returned group
      * will be always a root group.
      *
      * @return An {@link Group} instance or null if the <code>groupName</code> is null or an empty string.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static Group getGroup(IdentityManager identityManager, String groupName, Group parent) throws IdentityManagementException {
         if (isNullOrEmpty(groupName) || parent == null) {
             return null;
         }
 
         return getGroup(identityManager, new Group(groupName, parent).getPath());
     }
     
     /**
      * <p> Returns an {@link LdapUser} instance with the given <code>loginName</code>. </p>
      *
      * @param identityManager
      * @param loginName The agent's login name.
      *
      * @return An {@link User} instance or null if the <code>loginName</code> is null or an empty string.
      *
      * @throws IdentityManagementException If the method fails.
      */
    public static User getLdapUser(IdentityManager identityManager, String loginName) throws IdentityManagementException {
         if(isNullOrEmpty(loginName))
             return null;
        List<User> users = identityManager.createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, loginName)
                 .getResultList();
         if(users.isEmpty()) {
             return null;
         } else if(users.size() == 1) {
             return users.get(0);
         } else {
             throw new IdentityManagementException("Error - multiple LdapUser objects found with the same login name");
         }
     }
     
     public static Role getRole(IdentityManager identityManager, String name) throws IdentityManagementException{
         if(isNullOrEmpty(name))
             return null;
         List<Role> roles = identityManager.createIdentityQuery(Role.class)
                 .setParameter(Role.NAME, name)
                 .getResultList();
         if(roles.isEmpty()) {
             return null;
         } else if(roles.size() == 1) {
             return roles.get(0);
         } else {
             throw new IdentityManagementException("Error - multiple Role objects found with the same name");
         }
     }
     
     /**
      * <p> Creates a {@link GroupRole} relationship for the given {@link IdentityType}, {@link Role} and {@link Group}
      * instances. </p>
      *
      * @param relationshipManager
      * @param assignee A previously loaded {@link IdentityType} instance.
      * @param role A previously loaded {@link Role} instance.
      * @param group A previously loaded {@link Group} instance.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static void grantGroupRole(RelationshipManager relationshipManager, IdentityType assignee, Role role, Group group) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (assignee == null) {
             throw MESSAGES.nullArgument("IdentityType");
         }
 
         if (role == null) {
             throw MESSAGES.nullArgument("Role");
         }
 
         if (group == null) {
             throw MESSAGES.nullArgument("Group");
         }
 
         relationshipManager.add(new GroupRole(assignee, group, role));
     }
     
     /**
      * <p> Checks if the given {@link IdentityType}, {@link Role} and {@link Group} instances maps to a {@link
      * GroupRole} relationship. </p>
      *
      * @param relationshipManager
      * @param assignee A previously loaded {@link IdentityType} instance.
      * @param role A previously loaded {@link Role} instance.
      * @param group A previously loaded {@link Group} instance.
      *
      * @return True if the given <code>assignee</code>, <code>role</code> and <code>group</code> map to a previously stored
      *         {@link GroupRole} relationship. Otherwise this method returns false.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static boolean hasGroupRole(RelationshipManager relationshipManager, IdentityType assignee, Role role, Group group) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (assignee == null) {
             throw MESSAGES.nullArgument("IdentityType");
         }
 
         if (role == null) {
             throw MESSAGES.nullArgument("Role");
         }
 
         if (group == null) {
             throw MESSAGES.nullArgument("Group");
         }
 
         RelationshipQuery<GroupRole> query = relationshipManager.createRelationshipQuery(GroupRole.class);
 
         query.setParameter(GroupRole.ASSIGNEE, assignee);
         query.setParameter(GroupRole.ROLE, role);
 
         List<GroupRole> result = query.getResultList();
 
         for (GroupRole membership : result) {
             if (membership.getGroup().getId().equals(group.getId())) {
                 return true;
             }
 
             if (group.getPath().startsWith(membership.getGroup().getPath())) {
                 return true;
             }
         }
 
         return false;
     }
     
     /**
      * <p> Revokes a {@link GroupRole} relationship for the given {@link IdentityType}, {@link Role} and {@link Group}
      * instances. </p>
      *
      * @param relationshipManager
      * @param assignee A previously loaded {@link IdentityType} instance.
      * @param role A previously loaded {@link Role} instance.
      * @param group A previously loaded {@link Group} instance.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static void revokeGroupRole(RelationshipManager relationshipManager, IdentityType assignee, Role role, Group group) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (assignee == null) {
             throw MESSAGES.nullArgument("IdentityType");
         }
 
         if (role == null) {
             throw MESSAGES.nullArgument("Role");
         }
 
         if (group == null) {
             throw MESSAGES.nullArgument("Group");
         }
 
         RelationshipQuery<GroupRole> query = relationshipManager.createRelationshipQuery(GroupRole.class);
 
         query.setParameter(GroupRole.ASSIGNEE, assignee);
         query.setParameter(GroupRole.GROUP, group);
         query.setParameter(GroupRole.ROLE, role);
 
         for (GroupRole groupRole : query.getResultList()) {
             relationshipManager.remove(groupRole);
         }
     }
 
     
     /**
      * <p> Checks if the given {@link IdentityType} is a member of a specific {@link Group}. </p>
      *
      * @param relationshipManager
      * @param member A previously loaded {@link Account} instance.
      * @param group A previously loaded {@link Group} instance.
      *
      * @return True if the {@link Account} is a member of the provided {@link Group}. Otherwise this method returns
      *         false.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static boolean isMember(RelationshipManager relationshipManager, Account member, Group group) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (member == null) {
             throw MESSAGES.nullArgument("Account");
         }
 
         if (group == null) {
             throw MESSAGES.nullArgument("Group");
         }
 
         RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);
 
         query.setParameter(GroupMembership.MEMBER, member);
 
         List<GroupMembership> result = query.getResultList();
 
         for (GroupMembership membership : result) {
             if (membership.getGroup().getId().equals(group.getId())) {
                 return true;
             }
 
             if (membership.getGroup().getPath().startsWith(group.getPath())) {
                 return true;
             }
         }
 
         return false;
     }
     
     /**
      * <p> Checks if the given {@link Role} is granted to the provided {@link IdentityType}. </p>
      *
      * @param relationshipManager
      * @param assignee A previously loaded {@link IdentityType} instance. Valid instances are only from the {@link Account} and {@link Group} types.
      * @param role A previously loaded {@link Role} instance.
      *
      * @return True if the give {@link Role} is granted. Otherwise this method returns false.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static boolean hasRole(RelationshipManager relationshipManager, IdentityType assignee, Role role) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (assignee == null) {
             throw MESSAGES.nullArgument("IdentityType");
         }
 
         if (!Account.class.isInstance(assignee) && !Group.class.isInstance(assignee)) {
             throw MESSAGES.unexpectedType(assignee.getClass());
         }
 
         if (role == null) {
             throw MESSAGES.nullArgument("Role");
         }
 
         RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);
 
         query.setParameter(Grant.ASSIGNEE, assignee);
         query.setParameter(GroupRole.ROLE, role);
 
         return !query.getResultList().isEmpty();
     }
     
     /**
      * <p> Grants the given {@link Role} to the provided {@link IdentityType}. </p>
      *
      * @param relationshipManager
      * @param assignee A previously loaded {@link IdentityType} instance. Valid instances are only from the {@link Account} and {@link Group} types.
      * @param role A previously loaded {@link Role} instance.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static void grantRole(RelationshipManager relationshipManager, IdentityType assignee, Role role) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (assignee == null) {
             throw MESSAGES.nullArgument("IdentityType");
         }
 
         if (!Account.class.isInstance(assignee) && !Group.class.isInstance(assignee)) {
             throw MESSAGES.unexpectedType(assignee.getClass());
         }
 
         if (role == null) {
             throw MESSAGES.nullArgument("Role");
         }
 
         relationshipManager.add(new Grant(assignee, role));
     }
     
     /**
      * <p> Revokes the given {@link Role} from the provided {@link IdentityType}. </p>
      *
      * @param relationshipManager
      * @param assignee A previously loaded {@link IdentityType} instance. Valid instances are only from the {@link Account} and {@link Group} types.
      * @param role A previously loaded {@link Role} instance.
      *
      * @throws IdentityManagementException If the method fails.
      */
     public static void revokeRole(RelationshipManager relationshipManager, IdentityType assignee, Role role) throws IdentityManagementException {
         if (relationshipManager == null) {
             throw MESSAGES.nullArgument("RelationshipManager");
         }
 
         if (assignee == null) {
             throw MESSAGES.nullArgument("IdentityType");
         }
 
         if (!Account.class.isInstance(assignee) && !Group.class.isInstance(assignee)) {
             throw MESSAGES.unexpectedType(assignee.getClass());
         }
 
         if (role == null) {
             throw MESSAGES.nullArgument("Role");
         }
 
         RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);
 
         query.setParameter(Grant.ASSIGNEE, assignee);
         query.setParameter(GroupRole.ROLE, role);
 
         for (Grant grant : query.getResultList()) {
             relationshipManager.remove(grant);
         }
     }
 
 }
