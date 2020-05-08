 package hygeia;
 
 import java.sql.*;
 
 /* User class used for getting user information and creating users */
 public class User {
     
     private Database db;
     private int uid;
     private String username;
     private String email;
     private double height;
     private double weight;
     
     /*Use this to create User object in a page after the user has started a session */
     public User(Database db, int uid) {
         this.db = db;
         this.uid = uid;
     }
     
     /* Returns user id if login is successful, zero otherwise */
     public static int login(Database db, String email, String pwd) {
         if ((db == null) || (email == null) || (pwd == null)) {
             return -1;
        }
        
        /* Clean inputs */
        email = Algorithm.Clean(email);
        pwd = Algorithm.MD5(Algorithm.Clean(pwd));
        
        ResultSet rs = db.execute("select uid from users where email = '" +
               email + "' and hpwd = '" + pwd + "';");
       
       int uid = 0;
     
          /* Try to get uid from result */
          try {
             /* Select first (should be only) record */
            if (rs == null) {
                return -2;
            }
             if (rs.next()) {
                 uid = rs.getInt("uid");
             }
         
             /* Free db resources */
             db.free();
         } catch (SQLException e) {
             /* I don't know what to do here */
         }
     
         return uid;
     }
 
     
     /* Get database object for other classes. Returns the actual object--not a
        clone! */
     public Database getDb() {
         return this.db;
     }
     
     /* Create a new user. Returns uid; zero if unsuccessful  */
     public static int createUser(Database db, String uname, String pwd, 
         String email, double ht, double wt) {
         
         if ((db == null) || (uname == null) || (pwd == null) || (email == null)
              || (ht == 0) || (wt == 0)) {
             return -1;
         }
         
         /* Clean */
         uname = Algorithm.Clean(uname);
         pwd = Algorithm.MD5(Algorithm.Clean(pwd));
         email = Algorithm.Clean(email);
         
         
         
         /* Insert new record */
         int success = db.update("insert into users (username, hpwd, email, " +
             "height, weight) values ('" + uname + "', '" + pwd + "', '" +
             email + "', " + ht + ", " + wt +");");
         /* Return error if somethign strange happened */
         if (success != 1) {
            if (db.isAlive() != true) {
                return -4;
            }
            
             return -2;
         }
         
         /* Now get the uid.. there might be a better way of doing this */
         int uid = User.login(db, email, pwd);
         
         /* If there was an error from User.login, return an error code */
         if (uid < 1) {
             return -3;
         }
         
         return uid;
     }
 
     
     /* Delete user. */
     public static boolean deleteUser(Database db, int uid) {
         return false;
     }
     
     /* Sets instance variables for all properties from the database */
     public boolean getAllInfo() {
         
         java.sql.ResultSet rs = this.db.execute("select username, email, height, weight"
             + " from users where uid = " + this.uid + ";");
         
         /* Set variables */
         try {
             if (rs.next()) {
                 this.username = rs.getString("username");
                 this.email = rs.getString("email");
                 this.height = rs.getDouble("height");
                 this.weight = rs.getDouble("weight");
             } else {
                 return false;
             }
             this.db.free();
         } catch (SQLException e) {
             return false;
         }
         
         return true;
         
     }
     
     public String getUsername() {
         /* Return pre-loaded copy, if available */
         if (this.username != null) {
             return this.username;
         }
     
         /* Check that the db is accessable */
         if (this.db == null) {
             return null;
         }
     
         /* Get it */
         System.out.print(this.db.isAlive() + "\n");
         ResultSet rs = this.db.execute("select username from users where uid = "
             + this.uid + ";");
         
         String username;
     
         /* Try to find it */
         try {
             if (rs.next()) {
                 username = rs.getString("username");
                 this.username = username;
             }
         
             /* Free db resources */
             this.db.free();
         } catch (SQLException e) {
             /* Some kind of error reporting */
         }
     
         return this.username;    
     }
 
     
     public String getEmail() {
         if (this.email != null) {
             return this.email;
         }
         
         ResultSet rs = this.db.execute("select email from users where uid = " +
             this.uid + ";");
         
         String email = null;
             
         try {
             if (rs.next()) {
                 email = rs.getString("email");
             }
             this.db.free();
         } catch (SQLException e) {
             return null;
         }
         return email;
     }
     
     /* Do not access db. Use instance variable */
     public int getUid() {
         return this.uid;
     }
     
     public double getHeight() {
         if (this.height != 0) {
             return this.height;
         }
     
         ResultSet rs = this.db.execute("select email from height where uid = " +
             this.uid + ";");
         
         double height = 0;
             
         try {
             if (rs.next()) {
                 height = rs.getDouble("height");
             }
             this.db.free();
         } catch (SQLException e) {
             return 0;
         }
         return height;
     }
     
     public double getWeight() {
         if (this.weight != 0) {
             return this.weight;
         }
         
         ResultSet rs = this.db.execute("select email from weight where uid = " +
             this.uid + ";");
         
         double weight = 0;
             
         try {
             if (rs.next()) {
                 weight = rs.getDouble("weight");
             }
             this.db.free();
         } catch (SQLException e) {
             return 0;
         }
         return height;
     }
     
     /* Updates the database and instance variables with new information */
     public boolean updateAllInfo(String username, String email, double ht, double wt) {
         this.username = Algorithm.Clean(username);
         this.email = Algorithm.Clean(email);
         this.height = height;
         this.weight = weight;
         
         int up;
         
             up = this.db.update("update users set username='" + this.username +
             "', email='" + this.email + "', height=" + this.height + ", weight="
             + this.weight + " where uid = " + this.uid + ";");
 
         
         if (up < 0) {
             return false;
         }
         return true;
     }
     
     public boolean updateUsername(String uname) {
         this.username = Algorithm.Clean(uname);
  
         int up;
         
 
             up = this.db.update("update users set username='" + this.username +
              "' where uid = " + this.uid + ";");
 
         
         if (up < 0) {
             return false;
         }
         return true;
     }
     
     public boolean updatePwd(String pwd) {
         return false;
     }
     
     public boolean updateEmail(String email) {
         this.email = Algorithm.Clean(email);
  
         int up;
         
 
             up = this.db.update("update users set email='" + this.email +
              "' where uid = " + this.uid + ";");
 
         
         if (up < 0) {
             return false;
         }
         return true;
     }
     
     public boolean updateHeight(double height) {
         this.height = height;
  
         int up;
         
             up = this.db.update("update users set height='" + this.height +
              "' where uid = " + this.uid + ";");
 
         
         if (up < 0) {
             return false;
         }
         return true;
     
     }
     
     public boolean updateWeight(double weight) {
         this.weight = weight;
  
         int up;
         
             up = this.db.update("update users set weight='" + this.weight +
              "' where uid = " + this.uid + ";");
         
         if (up < 0) {
             return false;
         }
         return true;
     }
     
     /* Changes the user's password provided that old is the user's old 
        password. */
     public boolean resetPassword(String old, String pwd) {
         old = Algorithm.MD5(Algorithm.Clean(old));
         pwd = Algorithm.MD5(Algorithm.Clean(pwd));
         
         int up;
         
             up = this.db.update("update users set hpwd='" + pwd +
              "' where uid = " + this.uid + " and hpwd = '" + old + "';");
         
         if (up < 0) {
             return false;
         }
         return true;
     }
     
 }
