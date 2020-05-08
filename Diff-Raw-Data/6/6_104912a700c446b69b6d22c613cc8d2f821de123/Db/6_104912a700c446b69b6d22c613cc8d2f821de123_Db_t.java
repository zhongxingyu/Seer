 package util;
 
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import oracle.sql.*;
 import oracle.jdbc.*;
 
 import util.Group;
 import util.User;
 import util.Image;
 
 public class Db {
     static final String USERNAME = "mnaylor";
     static final String PASSWORD = "ravenraven1";
     // JDBC driver name and database URL
     static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
     static final String DB_URL = 
 	"jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
     
     public Connection conn;
     public Statement stmt;
     
     public int connect_db() {
 	try {
 	    Class drvClass = Class.forName(DRIVER_NAME);
 	    DriverManager.registerDriver((Driver) drvClass.newInstance());
 	    this.conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
 	    this.stmt = conn.createStatement();
 	    return 1;
 	} catch (ClassNotFoundException e) {
 	    e.printStackTrace();
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	} catch (InstantiationException e) {
 	    e.printStackTrace();
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	}
 	return 0;
     }
     
     /**
      * 
      */
     public void close_db() {
 	try {
 	    this.stmt.close();
 	    this.conn.close();
 	    System.out.println("Disconnected from database.");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
     }
     
     public Integer add_group(String user, String group_name) {
 	String query = "insert into groups " + "values ("
 	    + "group_id_sequence.nextval, '" + user + "', '" + 
 	    group_name + "', sysdate)";
 	return execute_update(query);
     }
 
     public Integer delete_friend(int group_id, String friend) {
 	String query = "delete from group_lists where group_id = " + group_id
 	               + " and friend_id = '" + friend + "'";
 	return execute_update(query);
     }
 
     public Integer add_friend(int group_id, String friend) {
 	String query = "insert into group_lists values(" + group_id
 	               + ", '" + friend + "', sysdate, null)";
 	System.out.println(query);
 	return execute_update(query);
     }
 
     /**
      * 
      * @param query
      * @return
      */
     public Integer execute_update(String query) {
 	try {
 	    return this.stmt.executeUpdate(query);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return 0;
     }
     
 
     public User get_user(String username) {
 	ResultSet rs_users;
 	ResultSet rs_persons;
 	String query_users = "select * from users "
 	    + "where user_name = '" + username + "'";
 	String query_persons = "select * from persons "
 	    + "where user_name = '" + username + "'";
 	rs_users = execute_stmt(query_users);
 	rs_persons = execute_stmt(query_persons);
 	return user_from_resultset(rs_users, rs_persons);
     }
 
     public String get_password(String username) {
 	String tPassword = "";
 	String query = "select password from users where user_name = '" + 
 	    username + "'";
 	ResultSet rs = execute_stmt(query);
 	try {
 	    while(rs != null && rs.next()) {
 		tPassword = (rs.getString(1)).trim();
 	    }
 	} catch(Exception e) {
 	    e.printStackTrace();
 	}
 	return tPassword;
     }
     
     public ArrayList<Group> get_groups(String username) {
 	ResultSet rs;
 	String query = "SELECT group_id, group_name "
 	    + "FROM groups "
 	    + "WHERE user_name = '"
 	    + username + "'";
 	rs = execute_stmt(query);
 	return group_from_resultset(rs);
     }
     
     /**
      * Gets groups (and users in groups) from a resultset
      * @param rs
      * @return
      */
     public ArrayList<Group> group_from_resultset(ResultSet rs) {
 	ArrayList<Group> groups;
 	groups = new ArrayList<Group>();
 	Group temp_group;
 	
 	try {
 	    while (rs.next()) {
 		String group_name = rs.getString("group_name");
 		int group_id = rs.getInt("group_id");
 		temp_group = new Group(group_name, group_id);
 		groups.add(temp_group);
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	
 	for (Group group: groups) {
 	    ArrayList<String> users;
 	    users = get_users_from_group(group);
 	    group.setFriends(users);
 	}
 	return groups;
     }
     
     public ArrayList<String> get_users_from_group(Group group) {
 	ResultSet rs;
 	String query = "SELECT friend_id "
 	    + "FROM group_lists "
 	    + "WHERE group_id = "
 	    + group.getId();
 	rs = execute_stmt(query);
 	return user_from_resultset_group(rs);
     }
     
     public User user_from_resultset(ResultSet rs_user, ResultSet rs_person) {
 	String user_name = null;
 	String password = null;
 	String date = null;
 	String email = null;
 	String fname = null;
 	String lname = null;
 	String phone = null;
 	String address = null;
 	User user = null;
 	ArrayList<Group> groups = null;
 
 	// Get data from rs_user
 	try {
 	    while (rs_user.next()) { 
 	        user_name = rs_user.getString("user_name");
 		password = rs_user.getString("password");
 		date = rs_user.getString("date_registered");
 	    }
 	    while (rs_person.next()) {
        		email = rs_person.getString("email");
 		fname = rs_person.getString("first_name");
 		lname = rs_person.getString("last_name");
 		phone = rs_person.getString("phone");
 		address = rs_person.getString("address");
 	    }
 	    groups = get_groups(user_name);
 	    user = new User(user_name, email, fname, lname, phone, address,
 			    groups, date);
 	
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return user;
     }
 
     /**
      * Gets users in a group
      * @param rs
      * @return
      */
     public ArrayList<String> user_from_resultset_group(ResultSet rs) {
 	ArrayList<String> all_users;
 	all_users = new ArrayList<String>();
 	
 	try {
 	    while (rs.next()) {
 		String friend_id = rs.getString("friend_id");
 		all_users.add(friend_id);
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return all_users;
     }
     
     /**
      * 
      * @param query
      * @return
      */
     public ResultSet execute_stmt(String query) {
 	try {
 	    return this.stmt.executeQuery(query);
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return null;
     }
 
     /**
      * Checks if a user with specified username exists within the database
      * Returns true if username exists, otherwise returns false
      *
      * @param String username
      * @return boolean
      */
     public boolean userExists(String username) {
 	String userquery = "select * from users where user_name='" + 
 	    username + "'";
 	ResultSet rset = execute_stmt(userquery);
 	try {
 	    if (!rset.next()) {
 		return false;
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return true;
     }
     
     /**
      * Adds a user to the database, given a specified username and password
      * @param String username, String password
      * @return Integer
      */
     public Integer addUser(String username, String password) {
 	String query = "insert into users values ('" + 
 	    username + "', '" + 
 	    password + "', sysdate)";
 	return execute_update(query);
     }
 
     /**
      * Insert a empty Blob into the database
      * @param int photo_id, String owner, int permitted, String subject, 
      * String place, String date, String desc, Blob thumbnail, Blob photo
      * @return Integer
      */
     public Integer addEmptyImage(int image_id, String owner, int permitted, 
 				 String subject, String place, String date, 
 				 String desc, Blob thumbnail, Blob photo) {
         String query = "insert into images values (" + image_id + ", '"  + 
 	    owner + "', '" + permitted + "', '" + subject + "', '" + place + 
 	    "', '" + date + "', '" + desc + "', empty_blob(), empty_blob())";
         return execute_update(query);
     }
 
     /**
      * 
      */
     public Blob getImageById(int image_id) {
 	ResultSet rs_image;
 	Blob image = null;
 	String query = "SELECT * FROM images WHERE image_id = " + image_id + 
 	    " FOR UPDATE";
 	try {
 	    rs_image = execute_stmt(query);
 	    image = ((OracleResultSet)rs_image).getBlob("photo");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return image;
     }	
 
     /**
      *
      */
     public Blob getThumbnailById(int image_id) {
         ResultSet rs_thumb;
 	Blob thumb = null;
         String query = "SELECT * FROM images WHERE image_id = " + image_id + 
 	    " FOR UPDATE";
 	try {
 	    rs_thumb = execute_stmt(query);
 	    thumb = ((OracleResultSet)rs_thumb).getBlob("thumbnail");
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return thumb;
     }
 
 
    /**
     * Insert a blob the database
     * @param int photo_id, String owner, int permitted, String subject, 
     * String place, String date, String desc, Blob thumbnail, Blob photo 
     * @return Integer
     */  
     public Integer addImage(String owner, int permitted, String subject, 
 			    String place, String date, String desc, 
 			    Blob thumbnail, Blob photo) {
 	String query = "insert into images values " + 
 	    "(image_id_sequence.nextval, '" + owner + "', '" + permitted + 
 	    "', '" + subject + "', '" + place + "', '" + date + "', '" 
 		+ desc + "', " + thumbnail + ", " + photo + ")";
 	return execute_update(query);
     }
     
     /**
      * Checks if a person with a given email address already exists
      * @param String email
      * @return boolean
      */
     public boolean emailExists(String email) {
 	String query = "select email from persons where email = '" + 
 	    email + "'";
 	ResultSet rs = execute_stmt(query);
 	try {
 	    return rs.next();
	} catch (SQLException e) {
	    e.printStackTrace();
 	}
	return true;
     }
 
     /**
      * Set the email of a user. Handles if does not exist.
      * @param String username, String email
      * @return Integer
      */
     public Integer setEmail(String username, String email) {
 	String checkquery = "select user_name, email from persons where " + 
 	    "user_name = '" + username + "'";
 	ResultSet rscheck = execute_stmt(checkquery);
 	try {
 	    if (rscheck.next()) {
 		// update the email
 		String updatequery = "update persons set email = '" + email + 
 		    "' where user_name = '" + username + "'";
 		return execute_update(updatequery);
 	    } else {
 		// add a row into the database
 		String addquery = "insert into persons (user_name, email)" + 
 		    "values ('" + username + "', '" + email + "')";
 		return execute_update(addquery);
 	    }
 	} catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return 0;
     }
     
     /**
      * Set the first name of a user. Row must exist prior to update.
      * @param String username, String fname
      * @return Integer
      */
     public Integer updateFname(String username, String fname) {
 	String query = "update persons set first_name = '" + 
 	    fname + "' where user_name = '" + username + "'";
 	return execute_update(query);
     }
 
     /**
      * Set the last name of a user. Row must exist prior to update.
      * @param String username, String lname
      * @return Integer
      */
     public Integer updateLname(String username, String lname) {
 	String query = "update persons set last_name = '" + 
 	    lname + "' where user_name = '" + username + "'";
 	return execute_update(query);
     }
     
     /**
      * Set the address of a user. Row must exist prior to update.
      * @param String username, String address
      * @return Integer
      */
     public Integer updateAddress(String username, String address) {
 	String query = "update persons set address = '" + 
 	    address + "' where user_name = '" + username + "'";
 	return execute_update(query);
     }
 
     /**
      * Set the phone of a user. Row must exist prior to update.
      * @param String username, String phone
      * @return Integer
      */
     public Integer updatePhone(String username, String phone) {
 	String query = "update persons set phone = '" + 
 	    phone + "' where user_name = '" + username + "'";
 	return execute_update(query);
     }
 
 }
