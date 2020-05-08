 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.database;
 
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import plantgame.models.User;
 import plantgame.models.Fruits;
 /**
  *
  * @author Derek
  */
 /*
     File History
     * 11/06/2013 - added API's for Market use 
  * 
  */
 public class PersistenceManager {
     
         private static PersistenceManager instance = null;
        
         private static final String USERNAME = "foo";
         private static final String PASSWORD = "bar";
         private static final String HOST = "localhost";
         private static final String DB = "derby";
         private static final int PORT = 1527;
         private static final String DB_NAME = "MyFirstDatabase";
        
         private Connection connection;
         
         protected PersistenceManager() {
         
             connection = getConnection();
             String temp;
         }
         
         public static PersistenceManager getInstance() {
             if (instance == null) {
                 instance = new PersistenceManager();
                 System.out.println("PersistanceManager creating a new instance");
             }
             return instance;
         }
     
         private Connection getConnection() {
             
                 Connection connection = null;
                /*
                 try {
                     try {
                        Class.forName("com.mysql.jdbc.Driver");
                     } catch (ClassNotFoundException ex) {
                         System.out.println("Could not get the JDBC bridge class name" + ex.getMessage());
                     }  
 
                     String url = "jdbc:mysql://localhost:3306/plantgame?zeroDateTimeBehavior=convertToNull";
                     connection = DriverManager.getConnection(url, "root", "password");
                 } catch (SQLException ex) {
                     System.out.println("Error with sql" + ex.getMessage());
                 }
                */
                 
                    try {
             Class.forName("org.apache.derby.jdbc.ClientDriver");
         } catch (ClassNotFoundException cnfe) {
             System.out.println("Error loading driver " + cnfe.getMessage());
         }
         try {
             String dbURL = "jdbc:" + DB + "://" + HOST + ":" + PORT + "/" + DB_NAME;
             connection = DriverManager.getConnection(dbURL, USERNAME, PASSWORD);
             //DatabaseMetaData meta = connection.getMetaData();
             /*
             System.out.println("Driver name: " + meta.getDriverName());
             System.out.println("Driver version: " + meta.getDriverVersion());
             
             Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
             try {
                 ResultSet resultSet = statement.executeQuery("select * from APP.People");
                 System.out.println("People in the database are:");
                 while (resultSet.next()) {
                     // first way...we'll use the column index
                     String firstName = resultSet.getString(2);
                     // second way...we'll use the column label
                     String lastName = resultSet.getString("LAST_NAME");
                     System.out.println("  " + firstName + " " + lastName);
                 }
             } catch (SQLException sqle) {
                 System.err.println("error: " + sqle.getMessage());
            }*/
             
         } catch (SQLException ex) {
             
         }

                 return connection;
         }
         
         public User authenticateUser(String userName, String password) {
             
            String query ="SELECT * FROM Player where userName=? AND password=?";
             PreparedStatement statement;
             
             //TODO: this should be removed. It's just here for testing
             if (userName.equals("test") && password.equals("test")){
               //DEBUG
               System.out.println("PersistanceManager creating test user");
               
               User user = new User();
               user.setFirstName("Tester");
               user.setLastName("Tester");
               user.setDob("none");
               user.setEmail("none");
               user.setPhone("none");
               user.setPassword("test");
               user.setUserName("test");
               user.setMoney(10000);
               return user;
             }
             
             //Connection should not be null at this point. If it is then there was
             //an issue with connecting to the database
             if (connection == null){
               return null;
             }
             
             try {
                 statement = connection.prepareStatement(query);
 
 
                 statement.setString(1, userName);
                 statement.setString(2, password);
                 ResultSet resultSetSelect =  statement.executeQuery();
 
                 if (resultSetSelect.next()) {
                        
                     System.out.println("we got stuff...");
                     User user = new User();
                   //  String s1 = resultSetSelect.getString(1);
                     
                      user.setId(resultSetSelect.getInt(1));
                      user.setFirstName(resultSetSelect.getString(2));
                      user.setLastName(resultSetSelect.getString(3));
                      user.setDob(resultSetSelect.getString(4));
                      user.setEmail(resultSetSelect.getString(5));
                      user.setPhone(resultSetSelect.getString(6));
                      user.setPassword(resultSetSelect.getString(7));
                      user.setUserName(resultSetSelect.getString(8));
                 
 
                       
                       return user;
                   }
                 else {
                     System.out.println("got nothing....");
                      
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             
              return null;
         }
         
          public User registerUser(User user) {
             
            String query ="INSERT INTO PlantGame.Player (FirstName, LastName, dob, email, phone, `password`, username) VALUES (?,?,?,?,?,?,?)";
             PreparedStatement statement;
             try {
                 statement = connection.prepareStatement(query);
 
 
                 statement.setString(1, user.getFirstName());
                 statement.setString(2, user.getLastName());
                 statement.setString(3, user.getDob());
                 statement.setString(4, user.getEmail());
                 statement.setString(5, user.getPhone());
                 statement.setString(6, user.getPassword());
                 statement.setString(7, user.getUserName());
                 int result =  statement.executeUpdate();
        
                       
               if ( result > 0) {
                
                   System.out.println("sucessful registration!");
                   
                   // This should add the ID the db generates.
                   return user;
                  
               }
               else {
                     System.out.println("got nothing....");
                      
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             
              return null;
         }
          
         public int getQtyFruitsInMkt(int id) {
             
             String query ="SELECT Quantity FROM PlantGame.Market WHERE id=?";
 
             PreparedStatement statement;
             int result = 0;
             //Connection should not be null at this point. If it is then there was
             //an issue with connecting to the database
             if (connection == null){
               return result; // error, just return 0
             }
             
             try {
                 statement = connection.prepareStatement(query);
                 statement.setInt(1, id);
                 ResultSet rs =  statement.executeQuery();
                 
                 
                 if (rs.next()) {
                        
                     System.out.println("we got stuff...");
                     result = rs.getInt("Quantity");
                   
                   }
                 else {
                     System.out.println("got nothing....");
                      
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             
              return result;
         } 
         
         public void incrementFruitsQtyInMkt(int id, String name ) throws SQLException {
             
             String query1 ="SELECT Quantity FROM PlantGame.Market WHERE id=?";
             String query2 ="UPDATE PlantGame.Market SET Quantity=? WHERE id=?";
             String query3 ="INSERT INTO PlantGame.Market (id, FruitName, Quantity) VALUES (?,?,?)";
 
             PreparedStatement statement;
             int count;
             int result;
             ResultSet rs = null;
             
             try {
                 
                 statement = connection.prepareStatement(query1);
                 statement.setInt(1, id);
                 rs =  statement.executeQuery();
                 
                 if (rs.next()) 
                 {
                     // there is a record of this fruit id
                     count = rs.getInt("Quantity"); 
                     count ++; // increment by one and update the DB
                     
                     statement = connection.prepareStatement(query2);
                     statement.setInt(1, id);
                     statement.setInt(2, count);
                     result = statement.executeUpdate();
                     
                     if ( result > 0) 
                     {
                         System.out.println("sucessful update of fruit Qty in Market!");
   
                     }
                     else 
                     {
                        System.out.println("Faild to update fruit Qty in Market....");
                        
                     }
                     
                 }
                 else // there is NO record of this fruit id, creat one
                 {
                     statement = connection.prepareStatement(query3);
                     statement.setInt(1, id);
                     statement.setString(2, name);
                     statement.setInt(3, 1);
                     
                     if (statement.execute()) 
                     {
                         System.out.println("sucessful creation of fruit in Market DB!");
   
                     }
                     else 
                     {
                        System.out.println("Faild to creat fruit in Market DB....");
                        
                     }
                 }
                       
               
             } catch (SQLException ex) {
                 
                 Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
                 throw ex;
             }
             finally
             {
                 if (rs != null)
                     rs.close();
             }
              
         }
         
         
         
         
      
 }
