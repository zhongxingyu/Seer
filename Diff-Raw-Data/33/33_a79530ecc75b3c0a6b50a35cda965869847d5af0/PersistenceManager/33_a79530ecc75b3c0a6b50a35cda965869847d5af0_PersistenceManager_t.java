 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.database;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
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
 
                 return connection;
         }
         
         public User authenticateUser(String userName, String password) {
             
             String query ="SELECT * FROM USER where userName=? AND password=?";
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
             
             String query ="INSERT INTO PlantGame.User (FirstName, LastName, dob, email, phone, `password`, username) VALUES (?,?,?,?,?,?,?)";
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
          
         public List<Fruits> getUsersFruits(int uid) {
             
             List<Fruits> fruits = new ArrayList<Fruits>();
             Fruits tmp = new Fruits();
             String query ="SELECT * FROM FRUITS USERID=?";
             PreparedStatement statement;
             
             //Connection should not be null at this point. If it is then there was
             //an issue with connecting to the database
             if (connection == null){
               return null;
             }
             
             try {
                 statement = connection.prepareStatement(query);
                 statement.setInt(1, uid);
                 ResultSet rs =  statement.executeQuery();
              
                 if (rs.next()) {
                        
                     System.out.println("we got stuff...");
                     rs.previous(); // move cursor back one position
                     
                     while(rs.next()){
                         // pulpulate fruit
                        //tmp.setFruitType(rs.getString("NAME"));
                        //tmp.setFruitId(rs.getInt("ID"));
                         tmp.setNumberOfTimesFertilize(rs.getInt("FERTILIZE_COUNT"));
                        tmp.setNumberOfTimesWater(rs.getInt("WATER_COUNT"));
                         // add fruit to fruit list to be returned.
                         fruits.add(tmp);
                     }
                     
                       
                       return fruits;
                   }
                 else {
                     System.out.println("got nothing....");
                      
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             
              return null;
         } 
         
         
         
         
      
 }
