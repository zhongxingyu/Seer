 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.aa.business.filter;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.jdbc.core.ResultSetExtractor;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 
 import de.escidoc.core.common.business.fedora.resources.ResourceType;
 import de.escidoc.core.common.business.fedora.resources.Values;
 
 /**
  * This object contains all user access rights used in the resource cache. These
  * access rights are SQL WHERE clauses which represent the read policies for a
  * specific user role.
  * 
  * @author SCHE
  */
 public abstract class AccessRights extends JdbcDaoSupport {
     /**
      * The id of the default role for anonymous access.
      */
     protected static final String DEFAULT_ROLE = "escidoc:role-default-user";
 
     /**
      * Resource id which will never exist in the repository.
      */
     private static final String INVALID_ID = "escidoc:-1";
 
     /**
      * SQL query to check if a grant for a user and role exists in the database.
      */
     private static final String USER_GRANT_EXISTS =
         "SELECT id FROM aa.role_grant WHERE user_id = ? AND role_id = ? AND "
             + "(revocation_date IS NULL OR revocation_date>CURRENT_TIMESTAMP)";
 
     /**
      * SQL query to check if a grant for a role exists in the database.
      */
     private static final String USER_GROUP_GRANT_EXISTS =
         "SELECT group_id FROM aa.role_grant WHERE group_id IS NOT NULL "
             + "AND role_id = ? "
             + "AND (revocation_date IS NULL OR revocation_date>CURRENT_TIMESTAMP)";
 
     /**
      * SQL query to check if the role exists in the database.
      */
     private static final String ROLE_EXISTS =
         "SELECT id FROM aa.escidoc_role WHERE id = ?";
 
     /**
      * SQL query to check if the user exists in the database.
      */
     private static final String USER_EXISTS =
         "SELECT id FROM aa.user_account WHERE id = ?";
 
     /**
      * Container for the scope rules and the policy rules of a role.
      */
     protected class Rules {
         public final String scopeRules;
 
         public final String policyRules;
 
         /**
          * Constructor.
          * 
          * @param scopeRules
          *            scope rules
          * @param policyRules
          *            policy rules
          */
         public Rules(final String scopeRules, final String policyRules) {
             this.scopeRules = scopeRules;
             this.policyRules = policyRules;
         }
     }
 
     /**
      * Mapping from role id to SQL statements.
      */
     public class RightsMap extends HashMap<String, Rules> {
         private static final long serialVersionUID = 7311398691300996752L;
     };
 
     /**
      * Array containing all mappings between role id and SQL WHERE clause. The
      * array index corresponds to the resource type.
      */
     protected final RightsMap[] rightsMap =
         new RightsMap[ResourceType.values().length];
 
     protected Values values = null;
 
     /**
      * Delete a specific access right.
      * 
      * @param roleId
      *            role id
      */
     public abstract void deleteAccessRight(final String roleId);
 
     /**
      * Delete all access rights.
      */
     public abstract void deleteAccessRights();
 
     /**
      * Ensure the given string is not empty by adding a dummy eSciDoc ID to it.
      * 
      * @param s
      *            string to be checked
      * 
      * @return non empty string
      */
     private String ensureNotEmpty(final String s) {
         String result = s;
 
         if ((result == null) || (result.length() == 0)) {
             result = values.escape(INVALID_ID);
         }
         return result;
     }
 
     /**
      * Get the SQL WHERE clause which matches the given role id. The SQL string
      * is already filled with the given user id if there is a place holder
      * inside the SQL string.
      * 
      * @param type
      *            resource type
      * @param roleId
      *            role id
      * @param userId
      *            user id
      * @param groupIds
      *            list of all user groups the user belongs to
      * @param userGrants
      *            grants directly assigned to a user
      * @param userGroupGrants
      *            group grants assigned to a user
      * @param optimizedUserGrants
      *            grants directly assigned to a user for a specific resource
      *            type
      * @param optimizedUserGroupGrants
      *            group grants assigned to a user for a specific resource type
      * @param hierarchicalContainers
      *            list of all child containers for all containers the user is
      *            granted to
      * @param hierarchicalOUs
      *            list of all child OUs for all OUs the user is granted to
      * 
      * @return SQL WHERE clause that represents the read policies for the given
      *         user role and user.
      */
     public String getAccessRights(
         final ResourceType type, final String roleId, final String userId,
         final Set<String> groupIds, final Set<String> userGrants,
         final Set<String> userGroupGrants,
         final Set<String> optimizedUserGrants,
         final Set<String> optimizedUserGroupGrants,
         final Set<String> hierarchicalContainers,
         final Set<String> hierarchicalOUs) {
         String result = null;
         final StringBuffer accessRights = new StringBuffer();
         final String containerGrants =
             ensureNotEmpty(getSetAsString(hierarchicalContainers));
         final String ouGrants = ensureNotEmpty(getSetAsString(hierarchicalOUs));
 
         readAccessRights();
         if ((userExists(userId)) || (userId == null) || (userId.length() == 0)) {
             synchronized (rightsMap) {
                 if ((roleId != null) && (roleId.length() > 0)) {
                     if (roleExists(roleId)) {
                         if (((groupIds.size() > 0) && userGroupGrantExists(
                             roleId, groupIds))
                             || (userGrantExists(userId, roleId))) {
                             Rules rights =
                                 rightsMap[type.ordinal()].get(roleId);
 
                             if (rights != null) {
                                 final String groupSQL = getGroupSql(groupIds);
                                 final String quotedGroupSQL =
                                     groupSQL.replace("'", "''");
                                 final String scopeSql =
                                     MessageFormat.format(
                                         rights.scopeRules.replace("'", "''"),
                                         new Object[] {
                                             values.escape(userId),
                                             values.escape(roleId),
                                             groupSQL,
                                             quotedGroupSQL,
                                             ensureNotEmpty(getGrantsAsString(
                                                 userGrants, userGroupGrants)),
                                             containerGrants, ouGrants });
                                 final String policySql =
                                     MessageFormat.format(
                                         rights.policyRules.replace("'", "''"),
                                         new Object[] {
                                             values.escape(userId),
                                             values.escape(roleId),
                                             groupSQL,
                                             quotedGroupSQL,
                                             ensureNotEmpty(getGrantsAsString(
                                                 optimizedUserGrants,
                                                 optimizedUserGroupGrants)),
                                             containerGrants, ouGrants });
 
                                 if (scopeSql.length() > 0) {
                                     accessRights.append(values.getAndCondition(
                                         scopeSql, policySql));
                                 }
                                 else if (policySql.length() > 0) {
                                     accessRights.append(policySql);
                                 }
                             }
                         }
                     }
                     else {
                         // unknown role id
                         accessRights.append("FALSE");
                     }
                 }
                 else {
                     // concatenate all rules with "OR"
                     for (Map.Entry<String, Rules> role : rightsMap[type
                         .ordinal()].entrySet()) {
                         if (((groupIds.size() > 0) && userGroupGrantExists(
                             roleId, groupIds))
                             || (userGrantExists(userId, role.getKey()))) {
                             final String groupSQL = getGroupSql(groupIds);
                             final String quotedGroupSQL =
                                 groupSQL.replace("'", "''");
 
                             if (accessRights.length() > 0) {
                                 accessRights.append(" OR ");
                             }
                             accessRights.append('(');
 
                             final String scopeSql =
                                 MessageFormat.format(
                                     role.getValue().scopeRules.replace("'",
                                         "''"),
                                     new Object[] {
                                         values.escape(userId),
                                         values.escape(role.getKey()),
                                         groupSQL,
                                         quotedGroupSQL,
                                         getGrantsAsString(userGrants,
                                             userGroupGrants), containerGrants,
                                         ouGrants });
                             final String policySql =
                                 MessageFormat.format(
                                     role.getValue().policyRules.replace("'",
                                         "''"),
                                     new Object[] {
                                         values.escape(userId),
                                         values.escape(role.getKey()),
                                         groupSQL,
                                         quotedGroupSQL,
                                         getGrantsAsString(optimizedUserGrants,
                                             optimizedUserGroupGrants),
                                         containerGrants, ouGrants });
 
                             if (scopeSql.length() > 0) {
                                 accessRights.append(values.getAndCondition(
                                     scopeSql, policySql));
                             }
                             else if (policySql.length() > 0) {
                                 accessRights.append(policySql);
                             }
                             accessRights.append(')');
                         }
                     }
                 }
             }
         }
         else {
             // unknown user id
             accessRights.append("FALSE");
         }
         if (accessRights.length() > 0) {
             result = accessRights.toString();
         }
         return result;
     }
 
     /**
      * Get the SQL snippet which lists all group ids a user is member of.
      * 
      * @param groupIds
      *            list of all user groups the user belongs to
      * 
      * @return SQL snippet with all group ids
      */
     private String getGroupSql(final Set<String> groupIds) {
         StringBuffer result = new StringBuffer();
 
         result.append('(');
         if ((groupIds != null) && (groupIds.size() > 0)) {
             try {
                 for (String groupId : groupIds) {
                     if (result.length() > 1) {
                         result.append(" OR ");
                     }
                     result.append("group_id='");
                     result.append(groupId);
                     result.append('\'');
                 }
             }
             catch (Exception e) {
                 result.append("FALSE");
             }
         }
         else {
             result.append("FALSE");
         }
         result.append(')');
         return result.toString();
     }
 
     /**
      * Get a list of all role ids.
      * 
      * @param type
      *            resource type
      * 
      * @return list of all role ids
      */
     public Collection<String> getRoleIds(final ResourceType type) {
         readAccessRights();
         return rightsMap[type.ordinal()].keySet();
     }
 
     /**
      * Put the given grant lists into a space separated string.
      * 
      * @param userGrants
      *            list of all user grants the user belongs to
      * @param userGroupGrants
      *            list of all user group grants the user belongs to
      * 
      * @return string containing all given grants separated with space
      */
     private String getGrantsAsString(
         final Set<String> userGrants, final Set<String> userGroupGrants) {
         StringBuffer result = new StringBuffer(getSetAsString(userGrants));
         String userGroupGrantString = getSetAsString(userGroupGrants);
 
         if (userGroupGrantString.length() > 0) {
             if (result.length() > 0) {
                 result.append(' ');
             }
             result.append(userGroupGrantString);
         }
         return result.toString();
     }
 
     /**
      * Put the given list into a space separated string.
      * 
      * @param set
      *            list of strings
      * 
      * @return string containing all given strings separated with space
      */
     private String getSetAsString(final Set<String> set) {
         StringBuffer result = new StringBuffer();
 
         if (set != null) {
             for (String element : set) {
                 if (result.length() > 0) {
                     result.append(' ');
                 }
                 result.append(values.escape(element));
             }
         }
         return result.toString();
     }
 
     /**
      * Check if the rule set for the given combination of resource type and role
      * id contains place holders for hierarchical containers or organizational
      * units.
      * 
      * This method should be called before generating the hierarchical list of
      * resources because this will be an expensive operation.
      * 
      * @param type
      *            resource type
      * @param roleId
      *            role id
      * @param userId
      *            user id
      * @param groupIds
      *            list of all user groups the user belongs to
      * @param placeHolder
      *            place holder to be searched for in the rule set
      * 
      * @return true if the rule set contains place holders for hierarchical
      *         resources
      */
     public boolean needsHierarchicalPermissions(
         final ResourceType type, final String roleId, final String userId,
         final Set<String> groupIds, final String placeHolder) {
         boolean result = false;
 
         synchronized (rightsMap) {
             if ((type != null)
                 && (roleId != null)
                 && (roleId.length() > 0)
                && (groupIds.size() > 0)
                && ((userGroupGrantExists(roleId, groupIds)) || (userGrantExists(
                    userId, roleId)))) {
                 final Rules rules = rightsMap[type.ordinal()].get(roleId);
 
                 result =
                     rules.policyRules.contains(placeHolder)
                         || rules.scopeRules.contains(placeHolder);
             }
         }
         return result;
     }
 
     /**
      * Store the given access right in the database table list.filter.
      * 
      * @param type
      *            resource type
      * @param roleId
      *            role id
      * @param scopeRules
      *            SQL statement representing the scope rules for the given
      *            combination of resource type and role
      * @param policyRules
      *            SQL statement representing the policy rules for the given
      *            combination of resource type and role
      */
     public abstract void putAccessRight(
         final ResourceType type, final String roleId, final String scopeRules,
         final String policyRules);
 
     /**
      * Read all access rights and store them in a map internally.
      */
     protected abstract void readAccessRights();
 
     /**
      * Check if the role with the given role id exists in AA.
      * 
      * @param roleId
      *            role id
      * 
      * @return true if the role exists
      */
     private boolean roleExists(final String roleId) {
         boolean result = false;
 
         if (roleId != null) {
             result =
                 (Boolean) getJdbcTemplate().query(ROLE_EXISTS,
                     new Object[] { roleId }, new ResultSetExtractor() {
                         public Object extractData(final ResultSet rs)
                             throws SQLException {
                             return Boolean.valueOf(rs.next());
                         }
                     });
         }
         return result;
     }
 
     /**
      * Return a string representation of the rights map.
      * 
      * @return string representation of the rights map
      */
     public String toString() {
         StringBuffer result = new StringBuffer();
 
         if (rightsMap != null) {
             for (ResourceType type : ResourceType.values()) {
                 result.append(type);
                 result.append(":\n");
                 for (Map.Entry<String, Rules> role : rightsMap[type.ordinal()]
                     .entrySet()) {
                     result.append("  ");
                     result.append(role.getKey());
                     result.append('=');
                     result.append(role.getValue().scopeRules);
                     result.append(',');
                     result.append(role.getValue().policyRules);
                     result.append('\n');
                 }
             }
         }
         return result.toString();
     }
 
     /**
      * Check if the user with the given user id exists in AA.
      * 
      * @param userId
      *            user id
      * 
      * @return true if the user exists
      */
     private boolean userExists(final String userId) {
         boolean result = false;
 
         if (userId != null) {
             result =
                 (Boolean) getJdbcTemplate().query(USER_EXISTS,
                     new Object[] { userId }, new ResultSetExtractor() {
                         public Object extractData(final ResultSet rs)
                             throws SQLException {
                             return Boolean.valueOf(rs.next());
                         }
                     });
         }
         return result;
     }
 
     /**
      * Check if a grant for the given combination of userId, roleId exists.
      * 
      * @param userId
      *            user id
      * @param roleId
      *            role id
      * 
      * @return true, if a grant exists
      */
     private boolean userGrantExists(final String userId, final String roleId) {
         boolean result = false;
 
         if ((userId != null) && (roleId != null)) {
             if (roleId.equals(DEFAULT_ROLE)) {
                 result = true;
             }
             else {
                 result =
                     (Boolean) getJdbcTemplate().query(USER_GRANT_EXISTS,
                         new Object[] { userId, roleId },
                         new ResultSetExtractor() {
                             public Object extractData(final ResultSet rs)
                                 throws SQLException {
                                 return Boolean.valueOf(rs.next());
                             }
                         });
             }
         }
         return result;
     }
 
     /**
      * Check if a grant for the given roleId exists.
      * 
      * @param roleId
      *            role id
      * @param groupIds
      *            list of group ids which the current user is member of
      * 
      * @return true, if a grant exists
      */
     private boolean userGroupGrantExists(
         final String roleId, final Set<String> groupIds) {
         boolean result = false;
 
         if (roleId != null) {
             if (roleId.equals(DEFAULT_ROLE)) {
                 result = true;
             }
             else {
                 result =
                     (Boolean) getJdbcTemplate().query(USER_GROUP_GRANT_EXISTS,
                         new Object[] { roleId }, new ResultSetExtractor() {
                             public Object extractData(final ResultSet rs)
                                 throws SQLException {
                                 boolean result = false;
 
                                 while (rs.next()) {
                                     if (groupIds.contains(rs.getString(1))) {
                                         result = true;
                                         break;
                                     }
                                 }
                                 return result;
                             }
                         });
             }
         }
         return result;
     }
 }
