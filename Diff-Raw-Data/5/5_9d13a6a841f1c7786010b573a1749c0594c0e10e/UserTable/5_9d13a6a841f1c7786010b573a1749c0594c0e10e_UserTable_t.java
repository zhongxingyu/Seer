 package arcade.database;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Creates and updates user table
  * @author Natalia Carvalho
  */
 public class UserTable extends Table {
     private static final String TABLE_SEPARATOR = ": ";
     private static final String USERNAME_COLUMN_FIELD = "username";  
     private static final String PASSWORD_COLUMN_FIELD = "pw";
     private static final String FIRSTNAME_COLUMN_FIELD  = "firstname";
     private static final String LASTNAME_COLUMN_FIELD  = "lastname";
     private static final String DOB_COLUMN_FIELD  = "DOB";
     private static final String AVATAR_COLUMN_FIELD  = "avatarfilepath";
     private static final String USERID_COLUMN_FIELD = "userid";
 
     private static final int USERNAME_COLUMN_INDEX = 1;
     private static final int PASSWORD_COLUMN_INDEX = 2;
     private static final int FIRSTNAME_COLUMN_INDEX = 3;
     private static final int LASTNAME_COLUMN_INDEX = 4;
     private static final int DOB_COLUMN_INDEX = 5;
     private static final int AVATAR_COLUMN_INDEX = 6;
     private static final int USERID_COLUMN_INDEX = 7;
     
     private static final String TABLE_NAME = "users";
 
     private Connection myConnection;
     private PreparedStatement myPreparedStatement; 
     private ResultSet myResultSet;
     
     /**
      * Constructor but eventually I want to make this part of the abstract class
      */
     public UserTable() {
         createDatabase();
     }
 
     void createDatabase() {
 
         try {
             Class.forName("org.postgresql.Driver");
         }
         catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
 
         String url = "jdbc:postgresql://cgi.cs.duke.edu/nrc10";
         String user = "nrc10";
         String password = "aUsg5xj2f";
 
         try {
             myConnection = DriverManager.getConnection(url, user, password);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         myPreparedStatement = null; 
         myResultSet = null;
 
     }
 
     /**
      * Closes Connection, ResultSet, and PreparedStatements once done with database
      */
     public void closeConnection() {
         try {
             if (myPreparedStatement != null) {
                 myPreparedStatement.close();
             }
             if (myResultSet != null) {
                 myResultSet.close();
             }
             if (myConnection != null) {
                 myConnection.close();
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Authenticates entry when username and password are present in user table
      * @param username is the username
      * @param password is the password
      * @return true if valid username/password; false otherwise
      */
     public boolean authenticateUsernameAndPassword(String username, String password) {
         String stm = "SELECT username, pw FROM users WHERE username = '" + username + "'";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet  = myPreparedStatement.executeQuery();
             if (myResultSet.next()) {
                if (myResultSet.getString(PASSWORD_COLUMN_INDEX).equals(password)) {
                    System.out.println("passwords match");
                    return true;
                }
             }
 
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         return false;
     }
     
     /**
      * Returns true if usernameExists, false othwerwise
      * @param username is the username
      */
     public boolean usernameExists(String username) {
         String stm = "SELECT username FROM users WHERE username='" + username + "'";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet  = myPreparedStatement.executeQuery();
             if (myResultSet.next()) {
                 return true;
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     /**
      * Adds a user to user table based on information
      * @param user is the username
      * @param pw is the password
      * @param firstname is firstname
      * @param lastname is lastname
      * @param dob is date of birth
      */
     public boolean createUser(String user, String pw, String firstname, 
                         String lastname, String dob) {
         if (usernameExists(user)) {
             return false;
         }
         String stm = "INSERT INTO users(username, pw, firstname, lastname, DOB) VALUES(?, ?, ?, ?, ?)";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myPreparedStatement.setString(USERNAME_COLUMN_INDEX, user);
             myPreparedStatement.setString(PASSWORD_COLUMN_INDEX, pw);
             myPreparedStatement.setString(FIRSTNAME_COLUMN_INDEX, firstname);
             myPreparedStatement.setString(LASTNAME_COLUMN_INDEX, lastname);
             myPreparedStatement.setString(DOB_COLUMN_INDEX, dob);
             myPreparedStatement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return true;
     }
     
     /**
      * Adds a user to user table based on information when avatar is present
      * @param user is the username
      * @param pw is the password
      * @param firstname is firstname
      * @param lastname is lastname
      * @param dob is date of birth
      * @param filepath is the filepath
      */
     public boolean createUser(String user, String pw, String firstname, 
                               String lastname, String dob, String filepath) {
         if (usernameExists(user)) {
             return false;
         }
         createUser(user, pw, firstname, lastname, dob);
         updateAvatar(user, filepath);
         return true;
     }
     
     /**
      * Returns the userid when given the username
      * @param username is the username
      */
     public String retrieveUserId(String username) {
         return retrieveEntry(username, USERNAME_COLUMN_INDEX);
     }
     
     /**
      * Given a username, retrieves the date of birth
      * @param username is the user
      */
     public String retrieveDOB(String username) {
         return retrieveEntry(username, DOB_COLUMN_INDEX);
     }
     
     /**
      * Given a username, retrieves avatar filepath
      * @param username is the username
      */
     public String retrieveAvatar(String username) {
         return retrieveEntry(username, AVATAR_COLUMN_INDEX);
     }
     
     /**
      * Given a username and a column_index, returns that entire row entry
      * @param username is the username
      * @param columnIndex is the index that we want the information for
      */
     public String retrieveEntry(String username, int COLUMN_INDEX) {
         String stm = "SELECT * FROM " +TABLE_NAME + " WHERE " + USERNAME_COLUMN_FIELD + "='" + username + "'";
         String entry = "";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myResultSet = myPreparedStatement.executeQuery();
             if (myResultSet.next()) {
                 entry = myResultSet.getString(COLUMN_INDEX);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         return entry;
     }
 
     /**
      * Given a username, deletes that user from userTable
      * @param username is user
      */
     public void deleteUser(String username) {
         String stm = "DELETE FROM " + TABLE_NAME + " WHERE " + USERNAME_COLUMN_FIELD + "='" + username + "'";
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myPreparedStatement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Given a username and a filepath, updates avatar
      * @param user is username
      * @param filepath is the filepath of the avatar
      */
     public void updateAvatar(String user, String filepath) {
         String userid = retrieveUserId(user);
         String stm = "UPDATE " + TABLE_NAME + " SET " + AVATAR_COLUMN_FIELD + "='" + 
                 "filepath" + "' WHERE " + USERID_COLUMN_FIELD + "='" + userid + "'";   
         try {
             myPreparedStatement = myConnection.prepareStatement(stm);
             myPreparedStatement.executeUpdate();
         }
         catch (SQLException e) {
             e.printStackTrace();
         } 
     }
     
     void printEntireTable () {
         System.out.println();
         try {
             myPreparedStatement = myConnection.prepareStatement("SELECT * FROM " + TABLE_NAME);
             myResultSet = myPreparedStatement.executeQuery();
             while (myResultSet.next()) {
                 System.out.print(myResultSet.getString(USERNAME_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(PASSWORD_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(FIRSTNAME_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(LASTNAME_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(DOB_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.print(myResultSet.getString(AVATAR_COLUMN_INDEX) + TABLE_SEPARATOR);
                 System.out.println(myResultSet.getString(USERID_COLUMN_INDEX));
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
 }
