 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cs122bp1;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.*;                              // Enable SQL processing
 
 /**
  *
  * @author tonkpils
  */
 public class Main {
 
     static Connection connection;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
         // Incorporate mySQL driver
         Class.forName("com.mysql.jdbc.Driver").newInstance();
 
         // Connect to the test database
         //TODO ask for login name and password
         connection = DriverManager.getConnection("jdbc:mysql:///moviedb", "cs122b", "cs122b");
 
         mainMenu(); //TODO add login loop
 
     }
 
     private static void getMetadata() {
         boolean step = false;
         try {
             // SQL statement to select all tables
             Statement select = connection.createStatement();
             Statement tableQuery = connection.createStatement();
             ResultSet tableList = tableQuery.executeQuery("SHOW TABLES");
 
             System.out.println("\n\n\n______________________________________");
 
 
             while (tableList.next()) {
                 if (step) {
                     pause(); // Pause before each table; except the first
                     System.out.println("______________________________________");
                 } else {
                     step = true;
                 }
                 String table = tableList.getString(1);
                 System.out.println("\nTABLE: " + table);
 
                 ResultSet result = select.executeQuery("Select * from " + table);
                 // Get metatdata from stars; print # of attributes in table
                 ResultSetMetaData metadata = result.getMetaData();
                 System.out.println("\nThere are " + metadata.getColumnCount() + " columns");
                 // Print type of each attribute
                 for (int i = 1; i <= metadata.getColumnCount(); i++) {
                     System.out.println("(" + i + ") " + metadata.getColumnName(i) + " :: " + metadata.getColumnTypeName(i));
                 }
                 System.out.println("\n______________________________________");
             }
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 
     private static void printSQLError(SQLException ex) {
         //TODO add clean mySQL error messages
        // List of error codes:
        // http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-error-sqlstates.html
        // http://dev.mysql.com/doc/refman/5.5/en/error-messages-server.html
        // http://dev.mysql.com/doc/refman/5.5/en/error-messages-client.html
         System.err.println("----SQLException----");
         System.err.println("SQLState:  " + ex.getSQLState());
         System.err.println("Message:  " + ex.getMessage());
         System.err.println("Vendor Error Code:  " + ex.getErrorCode());
     }
 
     private static void searchStarNames() {
         //TODO add search for first or last name exclusive?
 
         System.out.print("\n\n\nEnter search term: ");
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String readLine = null;
 
         try {
             readLine = br.readLine();
         } catch (IOException ioe) {
             System.out.println("Invalid Input!");
         } catch (NumberFormatException ex) {
             System.err.println("Not a valid number: " + readLine);
         }
 
         ResultSet result;
         try {
 
             result = queryStarNames(readLine);
             System.out.println("\nThe results of the query\n");
             printStars(result);
 
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 
     private static void searchStarIDs() {
 
         System.out.print("\n\n\nEnter star ID: ");
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         String readLine = null;
 
         try {
             readLine = br.readLine();
         } catch (IOException ioe) {
             System.out.println("Invalid Input!");
         } catch (NumberFormatException ex) {
             System.err.println("Not a valid number: " + readLine);
         }
 
         ResultSet result;
         try {
 
             result = queryStarID(readLine);
             System.out.println("\nThe results of the query\n");
             printStars(result);
 
         } catch (SQLException ex) {
             printSQLError(ex);
         }
     }
 
     private static void printStars(ResultSet result) throws SQLException {
         // print table's contents, field by field
         int count = 0;
         while (result.next()) {
             System.out.println("ID = " + result.getInt(1));
             System.out.println("Name = " + result.getString(2) + " " + result.getString(3));
             System.out.println("DOB = " + result.getString(4));
             System.out.println("photoURL = " + result.getString(5));
             if (++count % 3 == 0) {
                 System.out.println("______________________________________");
                 pause();
                 System.out.println("______________________________________\n");
             } else {
                 System.out.println();
             }
         }
     }
 
     private static ResultSet queryStarNames(String readLine) throws SQLException {
         //Search by name, returns if found in first or last name inclusive
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from stars WHERE (first_name = '" + readLine + "' OR last_name = '" + readLine + "' )");
         return result;
     }
 
     private static ResultSet queryStarID(String readLine) throws SQLException {
         //Search by star ID
         Statement select = connection.createStatement();
         ResultSet result = select.executeQuery("Select * from stars WHERE (id = '" + readLine + "' )");
         return result;
     }
 
     private static void mainMenu() {
         while (true) {
             System.out.print("\n\n\n\n=== Menu ===\n"
                     + "1) Search stars by name\n"
                     + "2) Search stars by ID\n"
                     + "3) Add star to database\n"
                     + "4) Add customer to database\n"
                     + "5) Delete customer from database\n"
                     + "6) Metadata of database\n"
                     + "7) SQL query\n"
                     + "8) Log out\n"
                     + "______________________________________\n"
                     + ":");
 
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String readLine = null;
             int menuChoice = 0;
 
             try {
                 readLine = br.readLine();
                 menuChoice = Integer.parseInt(readLine);
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
             } catch (NumberFormatException ex) {
                 System.err.println("Not a valid number: " + readLine);
             }
 
             switch (menuChoice) {
                 case 1:
                     searchStarNames();
                     pause();
                     break;
                 case 2:
                     searchStarIDs();
                     pause();
                     break;
                 case 3:
                     addStarMenu();
                     break;
                 case 4:
                     //TODO addCustomerMenu();
                     addCustomerMenu();
                     break;
                 case 5:
                     //TODO deleteCustomerMenu();
                     deleteCustomerMenu();
                     pause();
                     break;
                 case 6:
                     getMetadata();
                     pause();
                     break;
                 case 7:
                     //TODO openQueryMenu();
                     openQueryMenu();
                     pause();
                     break;
                 case 8:
                     return;
                 default:
                     break;
             }
         }
     }
 
     private static void pause() {
         BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("Press Enter to continue:");
         try {
             int ch = stdin.read();
         } catch (IOException ex) {
             System.out.println(ex.getMessage());
         }
 
     }
 
     private static int addStar(Integer id, String firstName, String lastName, String dob, String imgURL) {
         try {
             Statement update = connection.createStatement();
             int retID = update.executeUpdate("INSERT INTO stars VALUES("
                     + id + ", '"
                     + firstName + "', '"
                     + lastName + "', DATE('"
                     + dob + "'), '"
                     + imgURL + "');");
             return retID;
         } catch (SQLException ex) {
             printSQLError(ex);
         }
         return 0;
     }
 
     private static void addStarMenu() {
         int id = 0;
         String firstName = "";
         String lastName = "";
         String dob = "";
         String imageURL = "";
 
         while (true) {
             System.out.print("\n\n\n\n=== Add Star Menu ===\n"
                     + "\n"
                     + "ID (0=Auto): " + id + "\n"
                     + "First Name : " + firstName + "\n"
                     + "Last Name  : " + lastName + "\n"
                     + "D.O.B.     : " + dob + "\n"
                     + "Image URL  : " + imageURL + "\n"
                     + "\n"
                     + "1) Set ID\n"
                     + "2) Set First and Last Name\n"
                     + "3) Set Single Name\n"
                     + "4) Set Date of Birth\n"
                     + "5) Set Image URL\n"
                     + "6) Add Star\n"
                     + "0) Cancel\n"
                     + "______________________________________\n"
                     + ":");
 
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String readLine = null;
             int menuChoice = -1;
 
             try {
                 readLine = br.readLine();
                 menuChoice = Integer.parseInt(readLine);
 
 
                 switch (menuChoice) {
                     case 1:
                         //TODO star ID editable?
                         System.out.print("Enter ID:");
                         readLine = br.readLine();
                         id = Integer.parseInt(readLine);
                         break;
                     case 2:
                         while (firstName.isEmpty()) {
                             System.out.print("Enter First Name:");
                             firstName = br.readLine();
                         }
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Last Name:");
                             lastName = br.readLine();
                         }
                         break;
                     case 3:
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Name:");
                             lastName = br.readLine();
                         }
                         firstName = "";
                         break;
                     case 4:
                         //TODO must match format YYYY/MM/DD
                         System.out.print("Enter Date of Birth (YYYY/MM/DD):");
                         dob = br.readLine();
                         break;
                     case 5:
                         System.out.print("Enter Image URL:");
                         imageURL = br.readLine();
                         break;
                     case 6:
                         int added = 0;
 
                         //TODO check that first and last name exisit and DOB is in proper format
 
                         added = addStar(id, firstName, lastName, dob, imageURL);
                         if (added != 0) {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " successfully added\n"
                                     + "______________________________________");
                             pause();
                         } else {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " NOT added\n"
                                     + "______________________________________");
                             pause();
                             break;
                         }
                         return;
                     case 0:
                         return;
                     default:
                         break;
                 }
 
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
             } catch (NumberFormatException ex) {
                 System.err.println("Not a valid number: " + readLine);
 //            } catch (MySQLIntegrityConstraintViolationException e){}
             }
 
 
 
         }
     }
 
     private static void addCustomerMenu() {
         /* TODO Insert a customer into the database. Do not allow insertion of a
         / customer if his credit card does not exist in the credit card table.
         / The credit card table simulates the bank records. If the customer has
         / a single name, add it as his last_name and and assign an empty string
         / ("") to first_name.
          */
         int id = 0;
         String firstName = "";
         String lastName = "";
         int cc_id = 0;
         String address = "";
         String email = "";
         String password = "";
 
         while (true) {
             System.out.print("\n\n\n\n=== Add Customer Menu ===\n"
                     + "\n"
                     + "ID (0=Auto): " + id + "\n"
                     + "First Name : " + firstName + "\n"
                     + "Last Name  : " + lastName + "\n"
                     + "Credit Card: " + cc_id + "\n"
                     + "Address    : " + address + "\n"
                     + "E-Mail     : " + email + "\n"
                     + "Password   : " + password + "\n"
                     + "\n"
                     + "1) Set Customer ID\n"
                     + "2) Set First and Last Name\n"
                     + "3) Set Single Name\n"
                     + "4) Set Credit Card Number\n"
                     + "5) Set Address\n"
                     + "6) Set Email\n"
                     + "7) Set Password\n"
                     + "8) Add Customer\n"
                     + "0) Cancel\n"
                     + "______________________________________\n"
                     + ":");
 
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String readLine = null;
             int menuChoice = -1;
 
             try {
                 readLine = br.readLine();
                 menuChoice = Integer.parseInt(readLine);
 
 
                 switch (menuChoice) {
                     case 1:
                         //TODO customer ID editable?
                         System.out.print("Enter ID:");
                         readLine = br.readLine();
                         id = Integer.parseInt(readLine);
                         break;
                     case 2:
                         while (firstName.isEmpty()) {
                             System.out.print("Enter First Name:");
                             firstName = br.readLine();
                         }
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Last Name:");
                             lastName = br.readLine();
                         }
                         break;
                     case 3:
                         while (lastName.isEmpty()) {
                             System.out.print("Enter Name:");
                             lastName = br.readLine();
                         }
                         firstName = "";
                         break;
                     case 4:
                         //TODO must be number
                         System.out.print("Enter Credit Card Number:");
                         cc_id = Integer.parseInt(br.readLine());
                         break;
                     case 5:
                         System.out.print("Enter Address:");
                         address = br.readLine();
                         break;
                     case 6:
                         System.out.print("Enter Email:");
                         email = br.readLine();
                         break;
                     case 7:
                         System.out.print("Enter Password:");
                         password = br.readLine();
                         break;
                     case 8:
                         int added = 0;
 
                         //TODO validate all fields
                         //TODO check cc_id against cc database and validate name
                         //Vendor Error Code:  1452; i think means the cc_id is not in table:creditcards
                        // http://dev.mysql.com/doc/refman/5.5/en/error-messages-server.html
                        // http://dev.mysql.com/doc/refman/5.5/en/error-messages-client.html
 
                         added = addCustomer(id, firstName, lastName, cc_id, address, email, password);
                         if (added != 0) {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " successfully added\n"
                                     + "______________________________________");
                             pause();
                         } else {
                             System.out.println("______________________________________\n"
                                     + firstName + " " + lastName + " NOT added\n"
                                     + "______________________________________");
                             pause();
                             break;
                         }
                         return;
                     case 0:
                         return;
                     default:
                         break;
                 }
 
             } catch (IOException ioe) {
                 System.out.println("Invalid Input!");
             } catch (NumberFormatException ex) {
                 System.err.println("Not a valid number: " + readLine);
 //            } catch (MySQLIntegrityConstraintViolationException e){}
             }
 
 
 
         }
     }
 
     private static int addCustomer(int id, String firstName, String lastName, int cc_id, String address, String email, String password) {
         try {
             Statement update = connection.createStatement();
             int retID = update.executeUpdate("INSERT INTO customers VALUES("
                     + id + ", '"
                     + firstName + "', '"
                     + lastName + "', '"
                     + cc_id + "', '"
                     + address + "','"
                     + email + "','"
                     + password + "');");
             return retID;
         } catch (SQLException ex) {
             printSQLError(ex);
         }
         return 0;
     }
 
     private static void deleteCustomerMenu() {
         //TODO Delete a customer from the database. 
         System.out.println("=== Not yet implemented ===");
     }
 
     private static void openQueryMenu() {
         /* Enter a valid SELECT/UPDATE/INSERT/DELETE SQL command. The system
         should take the corresponding action, and return and display the valid
         results. For a SELECT query, display the answers. For the other types
         of queries, give enough information about the status of the execution
         of the query. For instance, for an UPDATE query, show the user how many
         records have been successfully changed.
          */
         System.out.println("=== Not yet implemented ===");
     }
 }
