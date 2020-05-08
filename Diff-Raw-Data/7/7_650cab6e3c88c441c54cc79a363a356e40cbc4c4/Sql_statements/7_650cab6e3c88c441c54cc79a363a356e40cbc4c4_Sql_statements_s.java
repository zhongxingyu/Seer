 package yolo;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.ResultSet;
 
 public class Sql_statements extends Sql_connection {
 
     private Statement statement = null;
     private ResultSet resultSet = null;
     String first_name;
     String last_name;
     String mobile_phone;
     gui window;
 
     /**
      * @param window Constructor of this class.
      */
     public Sql_statements(gui window) {
         this.window = window;
     }
 
     /**
      * Create a statement for use with INSERT and SELECT commands. This method
      * is type void.
      */
     public void createStatement() {
         try {
             System.out.println("Prepare Statement...");
             statement = connection.createStatement();
             System.out.println("Done!");
         } catch (SQLException error) {
             System.out.println("Error: " + error.getMessage());
         }
     }
 
     /**
      * Execute a statement to the Database. This method is type void.
      */
     public void setNewPerson(String first_name, String last_name, String mobile_phone) {
         try {
             System.out.println("Execute Statement...");
             statement.execute(" INSERT INTO person (first_name, last_name, mobile_phone) VALUES ('" + first_name + "','" + last_name + "','" + mobile_phone + "')");
             System.out.println("Execution was successful!");
         } catch (SQLException error) {
             System.out.println("Error: " + error.getMessage());
         }
     }
 
     /**
      * Executes a query to the database and appends the results on a textArea.
      * This method is type void.
      */
     public void getPersonsInfo() {
         try {
             resultSet = statement.executeQuery("SELECT * FROM person");
             while (resultSet.next()) {
                 Long id = resultSet.getLong("id");
                 String fname = resultSet.getString("first_name");
                 String lname = resultSet.getString("last_name");
                 String number = resultSet.getString("mobile_phone");
                 window.infoLog.append(id + "\t" + fname + "\t" + lname + "\t" + number + "\n");
                System.out.println(id + "\t" + fname + "\t" + lname + "\t" + number + "\n");
             }
 
         } catch (SQLException error) {
             System.out.println("Error: " + error.getMessage());
         }
     }
 
     /**
      * Close the statement and the resultSet. This method is type void.
      */
     public void CloseStatement() {
 
         if (statement != null) {
             try {
                 System.out.println("Closing statement...");
                 statement.close();
                 System.out.println("Statement closed!");
             } catch (SQLException ignore) {
             }
             if (resultSet != null) {
                 try {
                     System.out.println("Closing statement...");
                     resultSet.close();
                     System.out.println("Statement closed!");
                 } catch (SQLException ignore) {
                 }
             }
         }
     }
 }
