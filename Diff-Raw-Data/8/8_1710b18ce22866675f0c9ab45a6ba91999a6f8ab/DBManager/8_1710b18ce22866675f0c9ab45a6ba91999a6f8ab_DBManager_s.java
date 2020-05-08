 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Objects;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Jason
  */
 public class DBManager {
 
     public void deleteUser(String username) {
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Delete user
             statement.execute("DELETE FROM users WHERE username = '" + username + "';");
 
             // Close connection to database
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void deleteProduct(String productID) {
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Delete user
             statement.execute("DELETE FROM test WHERE productID = '" + productID + "';");
 
             // Close connection to database
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public ArrayList<User> getAllUser() {
         ArrayList<User> users = new ArrayList<>();
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users;");
             while (resultSet.next()) {
                 String ID = resultSet.getString("userID");
                 String role = resultSet.getString("role");
                 String username = resultSet.getString("username");
                 String password = resultSet.getString("password");
                 String name = resultSet.getString("name");
                 String address = resultSet.getString("address");
                 String email = resultSet.getString("email");
                 users.add(new User(ID, role, username, password, name, address, email));
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return users;
     }
 
     public Boolean checkUser(String username, String password) {
         Boolean result = false;
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE username='" + username + "' AND password='" + password + "';");
             while (resultSet.next()) {
             }
 
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return result;
     }
 
     public void addUser(User user) {
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users VALUES ( ?, ?, ?, ?, ?, ? );");
 
             // Add new user
             preparedStatement.setString(1, user.getUserID());
             preparedStatement.setString(2, user.getRole());
             preparedStatement.setString(3, user.getUsername());
             preparedStatement.setString(4, user.getPassword());
             preparedStatement.setString(5, user.getName());
             preparedStatement.setString(6, user.getEmail());
             preparedStatement.executeUpdate();
 
             // Close connection to database
             preparedStatement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public User getUser(String userID) {
         User user = null;
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users;");
             while (resultSet.next()) {
                 if (userID.equals(resultSet.getString("userID"))) {
                     String ID = resultSet.getString("userID");
                     String role = resultSet.getString("role");
                     String username = resultSet.getString("username");
                     String password = resultSet.getString("password");
                     String name = resultSet.getString("name");
                     String email = resultSet.getString("email");
                     String address = resultSet.getString("address");
                     user = new User(ID, role, username, password, name, address, email);
 
                     break;
                 }
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return user;
     }
     
     //Checkout Database Function
     
     public void updateStock(String productID, Integer stocknew){
          try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE test SET stock=? WHERE productID=?;");
 
             // Update the data
 
             preparedStatement.setString(1, stocknew.toString());
             preparedStatement.setString(2, productID);
             preparedStatement.executeUpdate();
            
             //connection closed
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
     }
     
     //invoice management 
     
     public ArrayList<Invoice> getAllInvoice(){
         
         ArrayList<Invoice> invoices = new ArrayList<>();
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM invoice;");
             while (resultSet.next()) {
                 String invoiceID = resultSet.getString("invoiceID");
                 Integer invoiceIDTemp = Integer.parseInt(invoiceID);
                 String log = resultSet.getString("log");
                String userID = resultSet.getString("userID");
                 invoices.add(new Invoice(invoiceIDTemp,userID,log));
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return invoices;
     
     }
     
     public Invoice getInvoice(String userID){
          Invoice invoice = null;
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM invoice WHERE userID='" + userID + "';");
             while (resultSet.next()) {
                 if (userID.equals(resultSet.getString("userID"))) {
                     String invoiceNumber = resultSet.getString("invoiceID");
                     String log = resultSet.getString("log");
                     Integer tempInvoiceNumber = Integer.parseInt(invoiceNumber);
                     invoice = new Invoice(tempInvoiceNumber,userID,log);
                     break;
                 }
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return invoice;
     }
     
     public void createInvoice(Integer userID,String log){
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO invoice (userID,log) VALUES (?,?);");
 
             // Update the data
 
             preparedStatement.setString(1, userID.toString());
             preparedStatement.setString(2, log);
             preparedStatement.executeUpdate();
            
             //connection closed
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public User getLoginUser(String userID) {
         User user = null;
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE name='" + userID + "';");
             while (resultSet.next()) {
 //                if (userID.equals(resultSet.getString("userID"))) {
                 String ID = resultSet.getString("userID");
                 String role = resultSet.getString("role");
                 String username = resultSet.getString("username");
                 String password = resultSet.getString("password");
                 String name = resultSet.getString("name");
                 String email = resultSet.getString("email");
                 String address = resultSet.getString("address");
                 user = new User(ID, role, username, password, name, address, email);
 
 //                    break;
 //                }
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return user;
     }
 
     public void addProduct(String category, String subcategory, String name, String description, Integer stock, Integer price, String pictureURL) {
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO test (category,subcategory,name,description,stock,price,pictureURL) VALUES (?, ?, ?, ?, ?, ?, ?);");
             
             // Add new user
             preparedStatement.setString(1, category);
             preparedStatement.setString(2, subcategory);
             preparedStatement.setString(3, name);
             preparedStatement.setString(4, description);
             preparedStatement.setString(5, stock.toString());
             preparedStatement.setString(6, price.toString());
             preparedStatement.setString(7, pictureURL);
             preparedStatement.executeUpdate();
 
             // Close connection to database
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void editProduct(String name, String description, Integer stock, Integer price, String pictureURL, String productID) {
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE test SET name=?, description=?, stock=?, price=?, pictureURL=? WHERE productID=?;");
 
             // Update the data
             preparedStatement.setString(1, name);
             preparedStatement.setString(2, description);
             preparedStatement.setString(3, stock.toString());
             preparedStatement.setString(4, price.toString());
             preparedStatement.setString(5, pictureURL);
             preparedStatement.setString(6, productID);
             preparedStatement.executeUpdate();
            
             //connection closed
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public Product getProduct(String productID) {
         Product product = null;
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM test;");
             while (resultSet.next()) {
                 if (productID.equals(resultSet.getString("productID"))) {
                     String ID = resultSet.getString("productID");
                     String categoryID = resultSet.getString("category");
                     String subcategoryID = resultSet.getString("subcategory");
                     String name = resultSet.getString("name");
                     String description = resultSet.getString("description");
                     Integer stock = Integer.parseInt(resultSet.getString("stock"));
                     Integer price = Integer.parseInt(resultSet.getString("price"));
                     String pictureURL = resultSet.getString("pictureURL");
 
                     product = new Product(ID, categoryID, subcategoryID, name, description, stock, price, pictureURL);
                     break;
                 }
 
             }
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return product;
     }
 
     public ArrayList<Product> getAllProducts() {
         ArrayList<Product> products = new ArrayList<>();
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM test;");
             while (resultSet.next()) {
                 String ID = resultSet.getString("productID");
                 String category = resultSet.getString("category");
                 String subcategory = resultSet.getString("subcategory");
                 String name = resultSet.getString("name");
                 String description = resultSet.getString("description");
                 Integer stock = Integer.parseInt(resultSet.getString("stock"));
                 Integer price = Integer.parseInt(resultSet.getString("price"));
                 String pictureURL = resultSet.getString("pictureURL");
                 products.add(new Product(ID, category, subcategory, name, description, stock, price, pictureURL));
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return products;
     }
 
     public ArrayList<Product> searchSpesificProducts(String itemdescription) {
         ArrayList<Product> products = new ArrayList<>();
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM test WHERE name LIKE '" + itemdescription + "';");
             while (resultSet.next()) {
                 String ID = resultSet.getString("productID");
                 String tempcategory = resultSet.getString("category");
                 String tempsubcategory = resultSet.getString("subcategory");
                 String name = resultSet.getString("name");
                 String description = resultSet.getString("description");
                 Integer stock = Integer.parseInt(resultSet.getString("stock"));
                 Integer price = Integer.parseInt(resultSet.getString("price"));
                 String pictureURL = resultSet.getString("pictureURL");
                 products.add(new Product(ID, tempcategory, tempsubcategory, name, description, stock, price, pictureURL));
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return products;
     }
 
     public ArrayList<Product> getAllSpesificProducts(String category, String subcategory) {
         ArrayList<Product> products = new ArrayList<>();
 
         try {
             // Load the driver
             Class.forName("com.mysql.jdbc.Driver").newInstance();
 
             // Connect to MySQL
             Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/ITStore", "root", "");
             Statement statement = connection.createStatement();
 
             // Search for the user
             ResultSet resultSet = statement.executeQuery("SELECT * FROM test WHERE category='" + category + "' AND subcategory='" + subcategory + "';");
             while (resultSet.next()) {
                 String ID = resultSet.getString("productID");
                 String tempcategory = resultSet.getString("category");
                 String tempsubcategory = resultSet.getString("subcategory");
                 String name = resultSet.getString("name");
                 String description = resultSet.getString("description");
                 Integer stock = Integer.parseInt(resultSet.getString("stock"));
                 Integer price = Integer.parseInt(resultSet.getString("price"));
                 String pictureURL = resultSet.getString("pictureURL");
                 products.add(new Product(ID, tempcategory, tempsubcategory, name, description, stock, price, pictureURL));
             }
 
             // Close connection to database
             statement.close();
             connection.close();
         } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
             Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return products;
     }
 }
