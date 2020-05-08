 package ro.finsiel.eunis.search.users;
 
 import ro.finsiel.eunis.jrfTables.users.*;
 import ro.finsiel.eunis.jrfTables.users.UserDomain;
 import ro.finsiel.eunis.jrfTables.users.UserPersist;
 import ro.finsiel.eunis.session.ThemeManager;
 import ro.finsiel.eunis.auth.EncryptPassword;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Vector;
 import java.util.List;
 import java.util.Enumeration;
 import java.sql.*;
 
 /**
  * Utility class for user management system.
  * @author finsiel
  */
 public class UsersUtility {
 
   /**
    * Retrieve roles for an user.
    * @param username username.
    * @return Vector of Strings with role names.
    */
   public static Vector getUsersRoles(String username) {
     if (username == null) return new Vector();
     Vector result = new Vector();
     try {
       List roles = new UsersRolesDomain().findWhere("A.USERNAME='" + username + "'");
       if (roles != null && roles.size() > 0) {
         for (int i = 0; i < roles.size(); i++) {
           result.add(((UsersRolesPersist) roles.get(i)).getRoleName());
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
   /**
    * Check if an String is found within an vector of Strings.
    * @param v Vector.
    * @param obj Object to found.
    * @return true if exists.
    */
   static public boolean ObjectIsInVector(Vector v, String obj) {
     if (v == null || obj == null) return false;
     boolean is = false;
     try
     {
       if (v != null && v.size() > 0 && obj != null)
       {
         for (int i = 0; i < v.size(); i++)
         {
           if (((String) v.get(i)).equalsIgnoreCase(obj)) is = true;
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return is;
   }
 
   /**
    * Return rights for an role as HTML compliant string (displayed within tooltip).
    * @param rolename rolanames.
    * @return String with HTML content.
    */
   public static String getRolesRightsAsString(String rolename) {
     if (rolename == null) return "";
     String st = "";
     try {
       List rights = new RolesRightsDomain().findWhere("A.ROLENAME = '" + rolename + "'  GROUP BY C.RIGHTNAME");
       if (rights != null && rights.size() > 0) {
         st = "&lt;UL&gt;";
         for (int i = 0; i < rights.size(); i++) {
           st += "&lt;LI&gt;Right " + (i + 1) + " : " + getNameNice(((RolesRightsPersist) rights.get(i)).getRightDescription());
         }
         st += "&lt;/UL&gt;";
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return st;
   }
 
   /**
    * Delete an user.
    * @param username username.
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if succeed.
    */
   public static boolean deleteUser(String username, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
     boolean succes = false;
     String SQL = "";
     String SQL1 = "";
     Connection con = null;
     PreparedStatement ps = null;
     ResultSet rs = null;
     Connection con1 = null;
     PreparedStatement ps1 = null;
     Statement st = null;
     try
     {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
       con1 = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
       SQL = "DELETE FROM EUNIS_USERS WHERE USERNAME='" + username + "'";
 
       ps = con.prepareStatement(SQL);
       ps.execute();
       ps.close();
       SQL = "DELETE FROM EUNIS_USERS_ROLES WHERE USERNAME='" + username + "'";
 
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
       ps1.close();
       SQL = "SELECT * FROM EUNIS_SAVE_ADVANCED_SEARCH WHERE USERNAME='" + username + "'";
       st = con.createStatement();
       rs = st.executeQuery(SQL);
 
       if (rs.isBeforeFirst()) {
         while (!rs.isLast()) {
           rs.next();
           SQL1 = "DELETE FROM EUNIS_SAVE_ADVANCED_SEARCH_CRITERIA WHERE CRITERIA_NAME='" + rs.getString("CRITERIA_NAME") + "' " +
                   " AND NATURE_OBJECT = '" + rs.getString("NATURE_OBJECT") + "'" +
                   " AND ID_NODE = '" + rs.getString("ID_NODE") + "'";
           ps1 = con1.prepareStatement(SQL1);
           ps1.execute();
           ps1.close();
         }
       }
       st.close();
       SQL = "DELETE FROM EUNIS_SAVE_ADVANCED_SEARCH WHERE USERNAME='" + username + "'";
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
       ps1.close();
       SQL = "SELECT * FROM EUNIS_GROUP_SEARCH WHERE USERNAME='" + username + "'";
       st = con.createStatement();
       rs = st.executeQuery(SQL);
 
       if (rs.isBeforeFirst())
       {
         while (!rs.isLast())
         {
           rs.next();
           SQL1 = "DELETE FROM EUNIS_GROUP_SEARCH_CRITERIA WHERE CRITERIA_NAME='" + rs.getString("CRITERIA_NAME") + "'";
           ps1 = con1.prepareStatement(SQL1);
           ps1.execute();
           ps1.close();
         }
       }
       st.close();
       SQL = "DELETE FROM EUNIS_GROUP_SEARCH WHERE USERNAME='" + username + "'";
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
       ps1.close();
       con1.close();
       con.close();
       rs.close();
       succes = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return succes;
   }
 
   /**
    * Delete an right.
    * @param rightname Name of the right.
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC urle.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if succeed.
    */
   public static boolean deleteRights(String rightname, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
     boolean succes = false;
     String SQL = "";
     Connection con = null;
     PreparedStatement ps = null;
     Connection con1 = null;
     PreparedStatement ps1 = null;
     try
     {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
       con1 = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
       SQL = "DELETE FROM EUNIS_RIGHTS WHERE RIGHTNAME='" + rightname + "'";
       ps = con.prepareStatement(SQL);
       ps.execute();
 
       ps.close();
       con.close();
       SQL = "DELETE FROM EUNIS_ROLES_RIGHTS WHERE RIGHTNAME='" + rightname + "'";
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
       ps1.close();
       con1.close();
       succes = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     if (succes && !existRightName(rightname) && !existRightNameInRolesRights(rightname))
       succes = true;
     else
       succes = false;
     return succes;
   }
 
   /**
    * Check if an username exists within database.
    * @param username username.
    * @return true if exists.
    */
   public static boolean existUserName(String username) {
     if (username == null) return false;
     boolean is = false;
     try {
       List users = new UserDomain().findWhere("USERNAME='" + username + "'");
       if (users != null && users.size() > 0) is = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return is;
   }
 
   /**
    * Check if username exists in users roles table (EUNIS_USERS_ROLES).
    * @param username username.
    * @return true if username exists in that table.
    */
   public static boolean existUserNameInUsersRoles(String username) {
     if (username == null) return false;
     boolean is = false;
     try {
       List users = new UsersRolesDomain().findWhere("A.USERNAME='" + username + "'");
       if (users != null && users.size() > 0) is = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return is;
   }
 
   /**
    * Check if an righ exists in EUNIS_ROLES_RIGHTS table.
    * @param rightname right.
    * @return true if exists.
    */
   public static boolean existRightNameInRolesRights(String rightname) {
     if (rightname == null) return false;
     boolean is = false;
     try {
       List rights = new RolesRightsSimpleDomain().findWhere("RIGHTNAME='" + rightname + "'");
       if (rights != null && rights.size() > 0) is = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return is;
   }
 
   /**
    * Check if an role exists in users roles table (EUNIS_USERS_ROLES).
    * @param roleName role name.
    * @return true if exists.
    */
   public static boolean existRoleNameInUsersRoles(String roleName) {
     if (roleName == null) return false;
     boolean is = false;
     try {
       List roles = new UsersRolesDomain().findWhere("A.ROLENAME='" + roleName + "'");
       if (roles != null && roles.size() > 0) is = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return is;
   }
 
   /**
    * Check if an role exists in role's rights table (EUNIS_ROLES_RIGHTS).
    * @param roleName role name.
    * @return true if exists.
    */
   public static boolean existRoleNameInRolesRights(String roleName) {
     if (roleName == null) return false;
     boolean is = false;
     try {
       List roles = new RolesRightsSimpleDomain().findWhere("ROLENAME='" + roleName + "'");
       if (roles != null && roles.size() > 0) is = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return is;
   }
 
   /**
    * Extract roles parameters from the request.
    * @param request
    * @return Vector of roles
    */
   public static Vector extractRolesParams(HttpServletRequest request) {
     Vector result = new Vector();
     if (null == request) return result;
     Enumeration en = request.getParameterNames();
     while (en.hasMoreElements()) {
       String paramName = (String) en.nextElement();
       if (-1 != paramName.lastIndexOf("rolexxx")) {
         result.addElement(paramName);
       }
     }
     return result;
   }
 
   /**
    * Extract roles names from vector with roles parameters from the request.
    * @param request
    * @return Vector with roles names
    */
   public static Vector extractRoles(HttpServletRequest request) {
     Vector result = new Vector();
     Vector param = extractRolesParams(request);
     if (param != null) {
       for (int i = 0; i < param.size(); i++) {
         result.add(((String) param.get(i)).substring(8));
       }
     }
 
     return result;
   }
 
   /**
    * Extract rights parameters from the request.
    * @param request
    * @return Vector of rights
    */
   public static Vector extractRightsParams(HttpServletRequest request) {
     Vector result = new Vector();
     if (null == request) return result;
     Enumeration en = request.getParameterNames();
     while (en.hasMoreElements()) {
       String paramName = (String) en.nextElement();
       if (-1 != paramName.lastIndexOf("rightxxx")) {
         result.addElement(paramName);
       }
     }
     return result;
   }
 
   /**
    * Extract rights names from vector with rights parameters from the request.
    * @param request
    * @return Vector with rights names
    */
   public static Vector extractRights(HttpServletRequest request) {
     Vector result = new Vector();
     Vector param = extractRightsParams(request);
     if (param != null) {
       for (int i = 0; i < param.size(); i++) {
         result.add(((String) param.get(i)).substring(9));
       }
     }
 
     return result;
   }
 
   /**
    * Extract rights parameters from the request.
    * @param request
    * @return Vector of rights
    */
 
   public static Vector extractRolesRightsParams(HttpServletRequest request) {
     Vector result = new Vector();
     if (null == request) return result;
     Enumeration en = request.getParameterNames();
     while (en.hasMoreElements()) {
       String paramName = (String) en.nextElement();
       if (-1 != paramName.lastIndexOf("_rightxxx_")) {
         result.addElement(paramName);
       }
     }
     return result;
   }
 
   /**
    * Extract rights names from vector with rights parameters from the request.
    * @param roleName Name of the role for which rights to be retrieved.
    * @param request HTTP Request parameter, used to extract data
    * @return Vector with rights names
    */
   public static Vector extractRolesRights(String roleName, HttpServletRequest request) {
     Vector result = new Vector();
     Vector param = extractRolesRightsParams(request);
     if (param != null) {
       for (int i = 0; i < param.size(); i++) {
         if (((String) param.get(i)).indexOf(roleName) == 0) {
           int index = ((String) param.get(i)).lastIndexOf("_rightxxx_");
           if (index != -1) result.add(((String) param.get(i)).substring(index + 10));
         }
       }
     }
 
     return result;
   }
 
   /**
    * Edit a user.
    * @param manager who made this change
    * @param username user name
    * @param firstName user first name
    * @param lastName user last name
    * @param mail user mail
    * @param password user password
    * @param newUserName  new user name
    * @param request request
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC urle.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @param loginDate Login date.
    * @return true if operation was made with success
    */
   public static boolean editUser(String manager,
                                  String username,
                                  String firstName,
                                  String lastName,
                                  String mail,
                                  String loginDate,
                                  String password,
                                  String newUserName,
                                  HttpServletRequest request,
                                  String SQL_DRV,
                                  String SQL_URL,
                                  String SQL_USR,
                                  String SQL_PWD) {
     boolean result = false;
     String updateSQL = "";
     Connection con = null;
     PreparedStatement ps = null;
     try {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
        //System.out.println("2----------loginDate="+loginDate+"+");
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_USERS SET";
       updateSQL += " USERNAME=?,";
       updateSQL += " FIRST_NAME=?,";
       updateSQL += " LAST_NAME=?,";
       updateSQL += " EMAIL=?,";
       updateSQL += " PASSWORD='"+EncryptPassword.encrypt(password)+"',";
       updateSQL += " LOGIN_DATE=str_to_date(?,'%d %b %Y %H:%i:%s')";
       updateSQL += " WHERE USERNAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, newUserName);
       ps.setString(2, firstName);
       ps.setString(3, lastName);
       ps.setString(4, mail);
       ps.setString(5, (loginDate == null ? null : loginDate.trim()));
       ps.setString(6, username);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //UPDATE USERS_ROLES
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_USERS_ROLES SET";
       updateSQL += " USERNAME=?";
       updateSQL += " WHERE USERNAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, newUserName);
       ps.setString(2, username);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //UPDATE EUNIS_SAVE_ADVANCED_SEARCH
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_SAVE_ADVANCED_SEARCH SET";
       updateSQL += " USERNAME=?";
       updateSQL += " WHERE USERNAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, newUserName);
       ps.setString(2, username);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //UPDATE EUNIS_GROUP_SEARCH
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_GROUP_SEARCH SET";
       updateSQL += " USERNAME=?";
       updateSQL += " WHERE USERNAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, newUserName);
       ps.setString(2, username);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       Vector newRoles = extractRoles(request);
       Vector oldRoles = getUsersRoles(newUserName);
 
       if (newRoles != null && oldRoles != null && oldRoles.size() > 0) {
         for (int i = 0; i < oldRoles.size(); i++) {
           if (!ObjectIsInVector(newRoles, (String) oldRoles.get(i))) {
             updateSQL = "DELETE FROM EUNIS_USERS_ROLES WHERE USERNAME='" + newUserName + "' AND ROLENAME='" + (String) oldRoles.get(i) + "'";
             ps = con.prepareStatement(updateSQL);
             ps.execute();
             ps.close();
           }
         }
       }
       if (newRoles != null && oldRoles != null && newRoles.size() > 0) {
         for (int i = 0; i < newRoles.size(); i++) {
           if (!ObjectIsInVector(oldRoles, (String) newRoles.get(i))) {
             updateSQL = "INSERT INTO EUNIS_USERS_ROLES(USERNAME,ROLENAME) VALUES(";
             updateSQL += "'" + newUserName + "',";
             updateSQL += "'" + (String) newRoles.get(i) + "')";
             ps = con.prepareStatement(updateSQL);
             ps.execute();
             ps.close();
           }
         }
       }
       result = true;
       con.close();
     } catch (Exception e) {
       e.printStackTrace();
       if (null != con)
         try {
           con.close();
         } catch (SQLException ex) {
         }
     }
     return result;
   }
 
   /**
    * Edit a user role.
    * @param manager who made this change
    * @param rolename role name
    * @param oldRoleName old role name
    * @param description role description
    * @param request request
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC urle.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if operation was made with success
    */
 
   public static boolean editRoleName(String manager,
                                      String rolename,
                                      String oldRoleName,
                                      String description,
                                      HttpServletRequest request,
                                      String SQL_DRV,
                                      String SQL_URL,
                                      String SQL_USR,
                                      String SQL_PWD) {
     boolean result = false;
     String updateSQL = "";
     String SQL = "";
     Connection con = null;
     PreparedStatement ps = null;
     try {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_ROLES SET";
       updateSQL += " ROLENAME=?,";
       updateSQL += " DESCRIPTION=?";
       updateSQL += " WHERE ROLENAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, rolename);
       ps.setString(2, description);
       ps.setString(3, oldRoleName);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //UPDATE USERS_ROLES
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_USERS_ROLES SET";
       updateSQL += " ROLENAME=?";
       updateSQL += " WHERE ROLENAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, rolename);
       ps.setString(2, oldRoleName);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //UPDATE ROLES_RIGHTS
       updateSQL = "";
       updateSQL += " UPDATE EUNIS_ROLES_RIGHTS SET";
       updateSQL += " ROLENAME=?";
       updateSQL += " WHERE ROLENAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, rolename);
       ps.setString(2, oldRoleName);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //DELETE ALL RIGHTS FOR ROLENAME
       SQL = "";
       SQL += " DELETE FROM EUNIS_ROLES_RIGHTS";
       SQL += " WHERE ROLENAME=?";
       ps = con.prepareStatement(SQL);
       ps.setString(1, rolename);
       ps.execute();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.execute();
       ps.close();
 
       //ADD NEW ROLES RIGHTS
       result = addRightsForRole(manager, request, rolename, SQL_URL, SQL_USR, SQL_PWD);
 
       con.close();
     } catch (Exception e) {
       e.printStackTrace();
       if (null != con)
         try {
           con.close();
         } catch (SQLException ex) {
         }
     }
     return result;
   }
 
 
   /** Edit user rights.
    * @param manager who made this change
    * @param rightname right name
    * @param description right description
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC urle.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if operation was made with success
    */
   public static boolean editRights(String manager,
                                    String rightname,
                                    String description,
                                    String SQL_DRV,
                                    String SQL_URL,
                                    String SQL_USR,
                                    String SQL_PWD) {
     boolean result = false;
     String updateSQL = "";
     Connection con = null;
     PreparedStatement ps = null;
     try {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
       updateSQL = "UPDATE EUNIS_RIGHTS SET";
       updateSQL += " DESCRIPTION=?";
       updateSQL += " WHERE RIGHTNAME=?";
       ps = con.prepareStatement(updateSQL);
       ps.setString(1, description);
       ps.setString(2, rightname);
       ps.executeUpdate();
       ps.close();
       // Not sure if it's needed, but strange things happen when doing fast some operation on user from web page.
       ps = con.prepareStatement("COMMIT");
       ps.executeUpdate();
       ps.close();
       con.close();
       result = true;
     } catch (Exception e) {
       e.printStackTrace();
       if (null != con)
         try {
           con.close();
         } catch (SQLException ex) {
         }
     }
     return result;
   }
 
 
  /**
   * Get UserPersist object by user name.
   * @param username
   * @return UserPersit object
   */
   public static UserPersist getUserByUserName(String username) {
     if (username == null) return null;
     UserPersist result = null;
     try {
       List users = new UserDomain().findCustom("SELECT USERNAME,PASSWORD,FIRST_NAME,LAST_NAME,LANG,EMAIL," +
               " THEME_INDEX,DATE_FORMAT(login_date,'%d %b %Y %H:%i:%s')" +
               " FROM EUNIS_USERS" +
               " WHERE USERNAME='" + username + "'");
       if (users != null && users.size() > 0) {
         result = (UserPersist) users.get(0);
         result.setPassword(EncryptPassword.decrypt(result.getPassword()));
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
   /**
    * Get rights descriptions list for a role name.
    * @param rolename Role for which rights to be extracted.
    * @return rights descriptions list
    */
   public static Vector getRolesRights(String rolename) {
     if (rolename == null) return new Vector();
     Vector result = new Vector();
     try {
       List rights = new RolesRightsDomain().findWhere("A.ROLENAME = '" + rolename + "'  GROUP BY C.RIGHTNAME");
       if (rights.size() > 0) {
         for (int i = 0; i < rights.size(); i++) {
           result.add(((RolesRightsPersist) rights.get(i)).getRightDescription());
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
   /**
    * Get rights names list for a role name.
    * @param rolename
    * @return rights names list
    */
   public static Vector getRolesRightsName(String rolename) {
     if (rolename == null) return new Vector();
     Vector result = new Vector();
     try {
       List rights = new RolesRightsDomain().findWhere("A.ROLENAME = '" + rolename + "'  GROUP BY C.RIGHTNAME");
       if (rights != null && rights.size() > 0) {
         for (int i = 0; i < rights.size(); i++) {
           result.add(((RolesRightsPersist) rights.get(i)).getRightName());
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
  /**
   * Add user roles.
   * @param manager who made this operation
   * @param roleName role name
   * @param description role description
   * @return true if operation was success
   */
   public static boolean addRoles(String manager, String roleName, String description) {
     boolean result = false;
     if (!existRoleName(roleName)) {
       try {
         RolesPersist role = new RolesPersist();
         role.setRoleName(roleName);
         role.setDescription(description);
         new RolesDomain().save(role);
         result = true;
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     return result;
   }
 
   /**
    * Add user rights.
    * @param manager who made this operation
    * @param rightName right name
    * @param description right description
    * @return true if operation was success
    */
   public static boolean addRights(String manager, String rightName, String description) {
     boolean success = false;
     try {
       if (!existRightName(rightName)) {
         RightsPersist right = new RightsPersist();
         right.setRightName(rightName);
         right.setDescription(description);
         new RightsDomain().save(right);
         success = true;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return success;
   }
 
   /**
    * true if user role name exists in EUNIS_ROLES table.
    * @param rolename role name
    * @return true or false
    */
   public static boolean existRoleName(String rolename) {
     if (rolename == null) return false;
     boolean is = false;
     try {
       List roles = new RolesDomain().findWhere("ROLENAME='" + rolename + "'");
       if (roles != null && roles.size() > 0) is = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return is;
   }
 
   /**
    * true if user right name exists in EUNIS_RIGHTS table.
    * @param rightname right name
    * @return true or false
    */
   public static boolean existRightName(String rightname) {
     if (rightname == null) return false;
     boolean result = false;
     try {
       List rights = new RightsDomain().findWhere("RIGHTNAME='" + rightname + "'");
       if (rights.size() > 0) result = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
   /**
    * Add user rights for a user role.
    * @param manager who made this operation
    * @param request request
    * @param rolename role name
    * @param SQL_URL JDBC urle.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if operation was made with success
    */
   public static boolean addRightsForRole(String manager, HttpServletRequest request, String rolename, String SQL_URL, String SQL_USR, String SQL_PWD) {
     boolean success = false;
     try {
       Vector newRights = UsersUtility.extractRights(request);
       String SQL = "";
       if (newRights != null && newRights.size() > 0 && existRoleName(rolename)) {
         Connection[] con2 = new Connection[newRights.size()];
         PreparedStatement[] ps2 = new PreparedStatement[newRights.size()];
         int j = -1;
 
         for (int i = 0; i < newRights.size(); i++) {
           j++;
           con2[j] = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
           SQL = "INSERT INTO EUNIS_ROLES_RIGHTS(ROLENAME,RIGHTNAME) VALUES(";
           SQL += "'" + rolename + "',";
           SQL += "'" + (String) newRights.get(i) + "')";
           ps2[j] = con2[j].prepareStatement(SQL);
           ps2[j].execute();
           ps2[j].close();
           con2[j].close();
         }
       }
       success = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return success;
   }
 
   /**
    * Replace all "_" from string 'name' with " ".
    * @param name
    * @return return the new string
    */
   public static String getNameNice(String name) {
     String result = "";
     try {
       if (name != null) result = name.replaceAll("_", " ");
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
   /**
    * Get all user roles names.
    * @return all user roles names
    */
   public static Vector getAllRolesName() {
     Vector result = new Vector();
     try {
       List roles = new RolesDomain().findOrderBy("A.ROLENAME");
       if (roles.size() > 0) {
         for (int i = 0; i < roles.size(); i++) {
           result.add(((RolesPersist) roles.get(i)).getRoleName());
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     return result;
   }
 
   /**
    * Delete a user role.
    * @param roleName role name
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if operation was made with success
    */
   public static boolean deleteRole(String roleName,
                                    String SQL_DRV,
                                    String SQL_URL,
                                    String SQL_USR,
                                    String SQL_PWD) {
     boolean result = false;
     String SQL = "";
     Connection con = null;
     PreparedStatement ps = null;
     try {
       Class.forName(SQL_DRV);
       con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
       SQL = "DELETE FROM EUNIS_ROLES WHERE ROLENAME='" + roleName + "'";
       ps = con.prepareStatement(SQL);
       ps.execute();
       ps.close();
 
       SQL = "DELETE FROM EUNIS_ROLES_RIGHTS WHERE ROLENAME='" + roleName + "'";
       ps = con.prepareStatement(SQL);
       ps.execute();
       ps.close();
 
       SQL = "DELETE FROM EUNIS_USERS_ROLES WHERE ROLENAME='" + roleName + "'";
       ps = con.prepareStatement(SQL);
       ps.execute();
       ps.close();
       con.close();
       result = true;
     } catch (Exception e) {
       e.printStackTrace();
       if (null != con)
         try {
           con.close();
         } catch (SQLException ex) {
         }
     }
 
     if (result && !existRoleName(roleName) && !existRoleNameInUsersRoles(roleName) && !existRoleNameInRolesRights(roleName)) {
       result = true;
     } else {
       result = false;
     }
     return result;
   }
 
   /**
    * Get RightsPersist object for a right name.
    * @param rightname right name
    * @return RightsPersist object
    */
   public static RightsPersist getRightsObject(String rightname) {
     if (rightname == null) return null;
     RightsPersist obj = null;
     try {
       List rights = new RightsDomain().findWhere("RIGHTNAME='" + rightname + "'");
       if (rights.size() > 0) obj = (RightsPersist) rights.get(0);
     } catch (Exception e) {
       e.printStackTrace();
     }
     return obj;
   }
 
   /**
    * Get RolesPersist object for a role name.
    * @param roleName role name
    * @return RolesPersist object
    */
   public static RolesPersist getRoleObject(String roleName) {
     if (roleName == null) return null;
     RolesPersist result = null;
     try {
       List roles = new RolesDomain().findWhere("ROLENAME='" + roleName + "'");
       if (roles != null && roles.size() > 0) result = (RolesPersist) roles.get(0);
     } catch (Exception e) {
       e.printStackTrace();
     }
     ;
     return result;
   }
 
   /**
    * Check if a role_right object exist in EUNIS_ROLES_RIGHTS  table.
    * @param roleName role name
    * @param rightName right name
    * @return true if exist
    */
   public static boolean ifRoleRightObjectExist(String roleName, String rightName) {
     if (roleName == null || rightName == null) return false;
     boolean result = false;
     try {
       List rolesRights = new RolesRightsSimpleDomain().findWhere("ROLENAME='" + roleName + "' AND RIGHTNAME='" + rightName + "'");
       if (rolesRights != null && rolesRights.size() > 0) result = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     ;
     return result;
   }
 
   /**
    * Delete saved easy search criteria.
    * @param username user name
    * @param pagename page name
    * @param criterianame criteria name
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    */
   public static void deleteUserSaveCriteria(String username, String pagename, String criterianame, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
     String SQL = "";
     String SQL1 = "";
     ResultSet rs = null;
     Connection con1 = null;
     PreparedStatement ps1 = null;
     Statement st = null;
 
     try {
       Class.forName(SQL_DRV);
       con1 = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
       SQL = "SELECT * FROM EUNIS_GROUP_SEARCH WHERE USERNAME='" + username + "' AND CRITERIA_NAME='" + criterianame + "' AND FROM_WHERE='" + pagename + "'";
       st = con1.createStatement();
       rs = st.executeQuery(SQL);
 
       if (rs.isBeforeFirst()) {
         while (!rs.isLast()) {
           rs.next();
           SQL1 = "DELETE FROM EUNIS_GROUP_SEARCH_CRITERIA WHERE CRITERIA_NAME='" + rs.getString("CRITERIA_NAME") + "'";
           ps1 = con1.prepareStatement(SQL1);
           ps1.execute();
           ps1.close();
         }
       }
 
       st.close();
 
       SQL = "DELETE FROM EUNIS_GROUP_SEARCH WHERE USERNAME='" + username + "' AND CRITERIA_NAME='" + criterianame + "' AND FROM_WHERE='" + pagename + "'";
 
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
 
       rs.close();
       ps1.close();
       con1.close();
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Delete saved advanced search criteria.
    * @param natureobject nature object
    * @param pagename page name
    * @param criterianame criteria name
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    */
   public static void deleteUserSaveAdvancedCriteria(String natureobject, String pagename, String criterianame, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
     String SQL = "";
     String SQL1 = "";
     ResultSet rs = null;
     Connection con1 = null;
     PreparedStatement ps1 = null;
     Statement st = null;
 
     try {
       Class.forName(SQL_DRV);
       con1 = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
       SQL = "SELECT * FROM EUNIS_SAVE_ADVANCED_SEARCH WHERE NATURE_OBJECT='" + natureobject + "' AND CRITERIA_NAME='" + criterianame + "' AND FROM_WHERE='" + pagename + "'";
       st = con1.createStatement();
       rs = st.executeQuery(SQL);
 
       if (rs.isBeforeFirst()) {
         while (!rs.isLast()) {
           rs.next();
           SQL1 = "DELETE FROM EUNIS_SAVE_ADVANCED_SEARCH_CRITERIA WHERE CRITERIA_NAME='" + rs.getString("CRITERIA_NAME") + "' AND NATURE_OBJECT='" + natureobject + "'";
           ps1 = con1.prepareStatement(SQL1);
           ps1.execute();
           ps1.close();
         }
       }
 
       st.close();
 
       SQL = "DELETE FROM EUNIS_SAVE_ADVANCED_SEARCH WHERE NATURE_OBJECT='" + natureobject + "' AND CRITERIA_NAME='" + criterianame + "' AND FROM_WHERE='" + pagename + "'";
 
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
 
       rs.close();
       ps1.close();
       con1.close();
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Delete saved combined search criteria.
    * @param pagename page name
    * @param criterianame criteria name
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    */
   public static void deleteUserSaveCombinedCriteria(String pagename, String criterianame, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
     String SQL = "";
     String SQL1 = "";
     ResultSet rs = null;
     Connection con1 = null;
     PreparedStatement ps1 = null;
     Statement st = null;
 
     try {
       Class.forName(SQL_DRV);
       con1 = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
       SQL = "SELECT * FROM EUNIS_SAVE_COMBINED_SEARCH WHERE CRITERIA_NAME='" + criterianame + "' AND FROM_WHERE='" + pagename + "'";
       st = con1.createStatement();
       rs = st.executeQuery(SQL);
 
       if (rs.isBeforeFirst()) {
         while (!rs.isLast()) {
           rs.next();
           SQL1 = "DELETE FROM EUNIS_SAVE_COMBINED_SEARCH_CRITERIA WHERE CRITERIA_NAME='" + rs.getString("CRITERIA_NAME") + "'";
           ps1 = con1.prepareStatement(SQL1);
           ps1.execute();
           ps1.close();
         }
       }
 
       st.close();
 
       SQL = "DELETE FROM EUNIS_SAVE_COMBINED_SEARCH WHERE CRITERIA_NAME='" + criterianame + "' AND FROM_WHERE='" + pagename + "'";
 
       ps1 = con1.prepareStatement(SQL);
       ps1.execute();
 
       rs.close();
       ps1.close();
       con1.close();
 
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Add users.
    * @param username user name
    * @param password user password
    * @param firstname user first name
    * @param lastname user last name
    * @param mail user mail
    * @param manager who made this operation
    * @param SQL_DRV JDBC driver.
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @param loginDate Login date.
    * @return true if operation was made with success
    */
   public static boolean addUsers(String username,
                                  String password,
                                  String firstname,
                                  String lastname,
                                  String mail,
                                  String loginDate,
                                  String manager,
                                  String SQL_DRV,
                                  String SQL_URL,
                                  String SQL_USR,
                                  String SQL_PWD) {
     boolean success = false;
     Connection con = null;
     PreparedStatement ps = null;
     String SQL = "";
     if (!existUserName(username))
     {
       try
       {
         Class.forName(SQL_DRV);
         con = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
 
         SQL = "INSERT INTO EUNIS_USERS(USERNAME,PASSWORD,FIRST_NAME,LAST_NAME,EMAIL,THEME_INDEX,LOGIN_DATE) " +
              " VALUES(?,'"+EncryptPassword.encrypt(password)+"',?,?,?,?,str_to_date(?,'%d %b %Y %H:%i:%s'))";
        ps = con.prepareStatement(SQL);
         ps.setString(1, username);
         ps.setString(2, firstname);
         ps.setString(3, lastname);
         ps.setString(4, mail);
        ps.setInt(5, ThemeManager.THEME_SKY_BLUE);
        ps.setString(6, (loginDate == null ? null : loginDate.trim()));
 
         ps.execute();
 
         ps.close();
         con.close();
 
         success = true;
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     return success;
   }
 
   /**
    * Add roles for users.
    * @param username user name
    * @param request request
    * @param manager who made this operation
    * @param manager who made this operation
    * @param SQL_URL JDBC url.
    * @param SQL_USR JDBC user.
    * @param SQL_PWD JDBC password.
    * @return true if operation was made with success
    */
   public static boolean addRolesForUser(String username, HttpServletRequest request, String manager, String SQL_URL, String SQL_USR, String SQL_PWD) {
     boolean success = false;
     try {
       Vector newRoles = UsersUtility.extractRoles(request);
       String SQL = "";
       if (newRoles != null && newRoles.size() > 0 && existUserName(username)) {
         Connection[] con2 = new Connection[newRoles.size()];
         PreparedStatement[] ps2 = new PreparedStatement[newRoles.size()];
         int j = -1;
 
         for (int i = 0; i < newRoles.size(); i++) {
           j++;
           con2[j] = DriverManager.getConnection(SQL_URL, SQL_USR, SQL_PWD);
           SQL = "INSERT INTO EUNIS_USERS_ROLES(USERNAME,ROLENAME) VALUES(";
           SQL += "'" + username + "',";
           SQL += "'" + (String) newRoles.get(i) + "')";
           ps2[j] = con2[j].prepareStatement(SQL);
           ps2[j].execute();
           ps2[j].close();
           con2[j].close();
         }
       }
       success = true;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return success;
   }
 }
