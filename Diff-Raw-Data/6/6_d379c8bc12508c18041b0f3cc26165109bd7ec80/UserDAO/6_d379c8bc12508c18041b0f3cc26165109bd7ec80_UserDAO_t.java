 /*
  * JMBS: Java Micro Blogging System
  *
  * Copyright (C) 2012  
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * @author Younes CHEIKH http://cyounes.com
  * @author Benjamin Babic http://bbabic.com
  * 
  */
 /**
  *
  */
 package jmbs.server;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jmbs.common.Project;
 import jmbs.common.User;
 
 /**
  *
  * @author
  */
 public class UserDAO extends DAO {
 
     public static final int ROOT_ACCESS_LEVEL = 0;
     public static final int ADMIN_ACCESS_LEVEL = 1; // Sees all projects, can supress, give authorisations, suppress others messages in projects
     public static final int MANAGER_ACCESS_LEVEL = 2; // Create projects, supress owned projects, invite users in project, supress messages in his project
     public static final int CREATE_ACCESS_LEVEL = 2; // for code clarity in functions
     public static final int DEV_ACCESS_LEVEL = 3; // Post in a project he is involved in, Post on timeline,
     public static final int DEFAULT_ACCESS_LEVEL = 10; // See timelines & projects
 
     public UserDAO(Connection c) {
         super(c);
     }
 
     /**
      * Adds a new user in the Database.
      *
      * @param u the new user
      * @param pass the hashed password
      * @return true if the user was added false if the email is already used
      */
     public boolean addUser(User u, String pass) {
         boolean b = false;
         if (!checkMail(u.getMail())) {
             try {
                 set("INSERT INTO users(name, forename, email, pass, authlvl) VALUES (?,?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
                 setString(1, u.getName());
                 setString(2, u.getFname());
                 setString(3, u.getMail());
                 setString(4, pass);
                 setInt(5, DEFAULT_ACCESS_LEVEL);
                 b = executeUpdate();
                 Picture.createUserRepertory(getGeneratedKeys().getInt("iduser"));
             } catch (SQLException ex) {
                 Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else {
             System.err.println("Email already used.");
         }
 
 
         return b;
     }
 
     public boolean addUser(int adminId, User u, String pass, int authlvl) throws SecurityException {
         boolean b = false;
         SecurityDAO s = new SecurityDAO(con);
         if (s.isAccessLevelSufficiant(adminId, ADMIN_ACCESS_LEVEL)) {
             if (s.getAccessLevel(adminId) <= authlvl) {
                 if (!checkMail(u.getMail())) {
                     try {
                         set("INSERT INTO users(name, forename, email, pass, authlvl) VALUES (?,?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
                         setString(1, u.getName());
                         setString(2, u.getFname());
                         setString(3, u.getMail());
                         setString(4, pass);
                         setInt(5, authlvl);
                         b = executeUpdate();
                         Picture.createUserRepertory(getGeneratedKeys().getInt("iduser"));
                     } catch (SQLException ex) {
                         //Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } else {
                     System.err.println("Email already used.");
                 }
             } else {
                 throw new SecurityException("You cannot create a user with higher access level than you have.");
             }
         } else {
             throw new SecurityException("You have not sufficient access level to do that !");
         }
 
 
         return b;
     }
 
     public boolean changeFname(int userid, String fname) {
         set("UPDATE users SET forename=? WHERE iduser=?;");
         setString(1, fname);
         setInt(2, userid);
         return executeUpdate();
     }
 
     public boolean changeMail(int userid, String pass, String mail) {
         boolean b = false;
         if (checkPassword(userid, pass)) {
             set("UPDATE users SET email=? WHERE iduser=?;");
             setString(1, mail);
             setInt(2, userid);
             b = executeUpdate();
         }
         return b;
     }
 
     public boolean changeName(int userid, String name) {
         set("UPDATE users SET name=? WHERE iduser=?;");
         setString(1, name);
         setInt(2, userid);
         return executeUpdate();
     }
 
     public boolean changePassword(int userid, String oldPass, String newPass) {
         boolean b = false;
         if (checkPassword(userid, oldPass)) {
             set("UPDATE users SET pass=? WHERE iduser=?;");
             setString(1, newPass);
             setInt(2, userid);
             b = executeUpdate();
         }
 
         return b;
     }
 
     /**
      * Check if the password matches with the db one.
      *
      * @param u User
      * @param pass String containing password
      * @return true - if the password matches
      *
      */
     public boolean checkPassword(int iduser, String pass) {
         boolean ret = false;
 
         set("SELECT pass FROM users WHERE iduser =?;");
         setInt(1, iduser);
         ResultSet res = executeQuery();
 
         try {
             ret = res.getString("pass").equals(pass);
             close(res);
             // TODO add connection log.
         } catch (SQLException e) {
             System.err.println("Invalid User.\n");
         }
 
         return ret;
     }
 
     /**
      * Check if the email is already in use.
      *
      * @param em String containing the email.
      * @return true if the email is used.
      */
     public boolean checkMail(String em) {
         boolean ret = true;
 
         set("SELECT email FROM users WHERE email =?;");
         setString(1, em);
         ResultSet res = executeQuery();
 
         try {
             res.getString("email");
             close(res);
         } catch (SQLException e) { // Unused email
             ret = false;
         }
 
         return ret;
     }
 
     //TODO: check if not useless.
     /**
      * Says if the user exists in the database.
      *
      * @return true - if the user is registered in the database.
      */
     public boolean exists(User u) {
         boolean ret;
 
         set("SELECT * FROM users WHERE iduser=?;");
         setInt(1, u.getId());
         ResultSet res = executeQuery();
 
         try {
             ret = res.getString("email").equals(u.getMail());
             close(res);
         } catch (SQLException e) { // user does not exist
             ret = false;
         }
 
         return ret;
     }
 
     public boolean exists(int iduser) {
         set("SELECT email FROM users WHERE iduser=?");
         setInt(1, iduser);
         ResultSet res = executeQuery();
         boolean ret = false;
 
         try {
             res.getString("email");
             ret = true;
             close(res);
         } catch (SQLException e) { // user does not exist we can do something here if we really want to waste time ...
         }
 
         return ret;
     }
 
     // TODO add an option to list Users by second names...
     // TODO don't consider upper case...
     /**
      * Find all users which names are containing uName.
      *
      * @param uName part of the searched name.
      * @return Array of User
      */
     public ArrayList<User> findUsers(String uName, int param) {
         ArrayList<User> u = new ArrayList<User>();
         int userid;
         String errorMsg = new String();
 
         if (param == BY_NAME) {
             set("SELECT * FROM users WHERE name LIKE ?;");
             setString(1, "%" + uName + "%");
             errorMsg = "No users found with name containing  \"" + uName + "\"";
         }
         if (param == BY_FORNAME) {
             set("SELECT * FROM users WHERE forename LIKE ?;");
             setString(1, "%" + uName + "%");
             errorMsg = "No users found with second name containing  \"" + uName + "\"";
         }
         if (param == BY_BOTH) {
             set("SELECT * FROM users WHERE name LIKE ? OR forename LIKE ?;");
             setString(1, "%" + uName + "%");
             setString(2, "%" + uName + "%");
             errorMsg = "No users found with name or second name containing  \"" + uName + "\"";
         }
         ResultSet res = executeQuery();
 
         try {
             do {
                 userid = res.getInt("iduser");
                 u.add(getUser(res));
             } while (res.next());
             close(res);
         } catch (SQLException e) {
             System.err.println(errorMsg);
         }
 
         
 
         return u;
     }
     
     public User getUser(ResultSet rs) throws SQLException{
         User u;
         int userId = rs.getInt("iduser");
         u = new User(rs.getString("name"), rs.getString("forename"), rs.getString("email"), userId, Picture.getUserPicture(userId), rs.getInt("authlvl"));
         
         return u;
     }
 
     /**
      * Set an user to follow an other user.
      *
      * @param idFollower
      * @param idFollowed
      * @return true if DB was editing DB succeeded
      */
     public boolean follows(int idFollower, int idFollowed) {
         if (this.exists(idFollower) && this.exists(idFollowed)) {
             set("INSERT INTO follows(follower, followed) VALUES (?,?);");
             setInt(1, idFollower);
             setInt(2, idFollowed);
             boolean res = executeUpdate();
 
             return (res);
         } else {
             return false;
         }
     }
 
     public int getAccessLevel(int iduser, int idproject) {
         int ret = -1;
         if (this.exists(iduser) && (new ProjectDAO(super.con)).exists(idproject)) {
             set("SELECT authlvl FROM participate WHERE iduser=? AND idproject=?");
             setInt(1, iduser);
             setInt(2, idproject);
             ResultSet res = executeQuery();
 
             try {
                 ret = res.getInt("authlvl");
                 close(res);
             } catch (SQLException e) {
                 System.err.println("Unexcepted error !");
             }
         }
 
         return ret;
     }
 
     /**
      * Returns all the users a user is following
      *
      * @param user - the user
      * @return list of users
      */
     public ArrayList<User> getFollowed(User user) {
         ArrayList<User> u = new ArrayList<User>();
 
         set("SELECT * FROM users,follows WHERE follows.follower =? and follows.followed=users.iduser;");
         setInt(1, user.getId());
         ResultSet res = executeQuery();
 
         try {
             do {
                 u.add(getUser(res));
             } while (res.next());
             close(res);
         } catch (SQLException e) {
             System.err.println(user.getFname() + " does not follow anyone yet!");
         }
 
         return u;
     }
 
     /**
      * Returns all the user followers.
      *
      * @param user
      * @return ArrayList<User>
      */
     public ArrayList<User> getFollowers(User user) {
         ArrayList<User> u = new ArrayList<User>();
 
         set("SELECT users.* FROM users,follows WHERE follows.followed =? and follows.follower=users.iduser;");
         setInt(1, user.getId());
         ResultSet res = executeQuery();
 
         try {
             do {
                 u.add(getUser(res));
             } while (res.next());
             close(res);
         } catch (SQLException e) {
             System.err.println(user.getFname() + " is not followed by anyone yet!");
         }
 
         return u;
     }
 
     /**
      * Returns all the projects a user is involved in.
      *
      * @return Array of Projects
      */
     public ArrayList<Project> getProjects(int userid) {
         ArrayList<Project> p = new ArrayList<Project>();
         ProjectDAO pdao = new ProjectDAO(con);
 
         set("SELECT projects.* FROM participate,projects WHERE participate.iduser=? AND participate.idproject=projects.idproject;");
         setInt(1, userid);
         ResultSet res = executeQuery();
 
         try {
             do {
                 p.add(pdao.getProject(res));
             } while (res.next());
            close(res);
         } catch (SQLException e) {
             // TODO determine if this error is due to a wrong user name or a
             // lack of projects.
         }
         
         return p;
     }
 
     /**
      * Returns all the projects a user is involved in.
      *
      * @return Array of Projects
      */
     public ArrayList<Project> getProjects(User u) {
         return getProjects(u.getId());
     }
 
     public ArrayList<Project> getOwnedProjects(User u) {
         return getOwnedProjects(u.getId());
     }
 
     public ArrayList<Project> getOwnedProjects(int userid) {
         set("SELECT * FROM projects WHERE idowner=?;");
         setInt(1, userid);
         ResultSet rs = executeQuery();
         ProjectDAO pdao = new ProjectDAO(con);
 
         ArrayList<Project> pj = new ArrayList<Project>();
 
         try {
             do {
                 pj.add(pdao.getProject(rs));
             } while (rs.next());
             close(rs);
         } catch (SQLException e) {
             // no owned projects
         }
         return pj;
     }
 
     /**
      * Find a user using his database id.
      *
      * @param userid database id of the user u want to find.
      */
     public User getUser(int userid) {
 
         User u = null;
         set("SELECT * FROM users WHERE iduser=?;");
         setInt(1, userid);
         ResultSet res = executeQuery();
 
         try {
             u = getUser(res);
             close(res);
         } catch (SQLException e) {
             System.out.println("No users with id equal to " + userid + " !");
         }
         return u;
     }
 
     /**
      * Finds a user using his email.
      *
      * @param em email of the user.
      * @throws SQLException
      */
     public User getUser(String em) {
         User u = null;
         int userid;
         ResultSet res;
 
         set("SELECT * FROM users WHERE email=?;");
         setString(1, em);
         res = executeQuery();
 
         try {
             userid = res.getInt("iduser");
             u = getUser(res);
             close(res);
         } catch (SQLException e) {
             System.err.println("No user with " + em + " as email adress !\n");
         }
         return u;
     }
 
     /**
      * Grans the developper status to a user
      *
      * @param adminId - the user who grants the developper status
      * @param userId - the user who will be granted the status
      * @return
      * @throws SecurityException if the user who try to grant status has a too
      * low authorisation level
      */
     public boolean grantDevStatus(int adminId, int userId) throws SecurityException {
         boolean res;
         if (new SecurityDAO(con).isAccessLevelSufficiant(adminId, ADMIN_ACCESS_LEVEL)) {
             set("UPDATE users SET authlvl=? WHERE iduser=?;");
             setInt(1, DEV_ACCESS_LEVEL);
             setInt(2, userId);
             res = executeUpdate();
         } else {
             throw new SecurityException("Your access level is not high enough to do that operation.");
         }
         return res;
     }
 
     public boolean participate(int iduser, int idproject, int auth) {
         if (this.exists(iduser) && (new ProjectDAO(super.con)).exists(idproject)) { //if the project and the user exists.
             set("INSERT INTO participate (iduser,idproject,authlvl) VALUES (?,?,?);");
             setInt(1, iduser);
             setInt(2, idproject);
             setInt(3, auth);
             boolean res = executeUpdate();
 
             return res;
         } else {
             return false;
         }
     }
 
     public boolean participate(int iduser, int idproject) {
         return this.participate(iduser, idproject, UserDAO.DEFAULT_ACCESS_LEVEL);
     }
 
     /**
      * Set a user to stop following an other user.
      *
      * @param idFollower
      * @param idFollowed
      * @return true if DB was editing DB succeeded
      */
     public boolean unFollow(int idFollower, int idFollowed) {
         set("DELETE FROM follows WHERE follower=? and followed=?;");
         setInt(1, idFollower);
         setInt(2, idFollowed);
         boolean res = executeUpdate();
 
         return (res);
     }
 
     public boolean unParticipate(int iduser, int idproject) {
         set("DELETE FROM participate WHERE iduser=? and idproject=?;");
         setInt(1, iduser);
         setInt(2, idproject);
         boolean res = executeUpdate();
 
         return (res);
     }
 }
