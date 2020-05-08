 package wsc_application;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import javax.swing.JOptionPane;
 
 /*
  * Employee.java
  * by Paul Durivage and Jacob Savage
  * for CIS470 GroupA
  */
 
 public class Employee{
     
     protected String firstName;
     protected String lastName;
     protected Long EMPID;
     protected String email;
     protected String empType;
     protected String password;
     public enum type {
         SalesPerson("SP"), 
         EngrSpec("ES"), 
         PrintSpec("PS"), 
         StockClerk("SC"), 
         OpsMan("OM"), 
         Admin("AD");
         
         private String abbrev;
         private type(String value) {
             this.abbrev = value;
         }
     }
     protected static ArrayList<Employee> employees;
     
     public Employee(){
         
     }
     
      public Employee(long empid){
         setEmpId(empid);
     }
      
     public Employee(long empid, String pass){
         setEmpId(empid);
         setPassword(pass);
     }
     
     public Employee(String fName, String lName, Long empId, String eMail) {
         setFirstName(fName);
         setLastName(lName);
         setEmpId(empId);
         setEmail(eMail);
 
     }
     
     public Employee(String fName, String lName, Long empId, String eMail, String empType) {
         setFirstName(fName);
         setLastName(lName);
         setEmpId(empId);
         setEmail(eMail);
         setEmpType(empType);
     }
     public static ArrayList getEmployeesBy(String column, String id) {
         employees = new ArrayList(0);
         Employee employee = null;
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         String query = "select * from EMPLOYEE where " + column + "='" + id + "';";
         rs = mysql.doQuery(query);
        System.out.println(query);
         try {
            while (rs.next()) {
                 employee = new Employee(
                         rs.getString("EmpFirstName"),
                         rs.getString("EmpLastName"),
                         rs.getLong("EMPID"),
                         rs.getString("EmpEmail"),
                         rs.getString("EmpType"));
                 employees.add(employee);
             }
         }
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
          }
          finally {
              mysql.closeAll();
              return employees;
          }
     }
     // Employee class code goes here.  Methods and stuff.
     public static Employee searchBy(long EmpId) {
         Employee employee = null;
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         String query = "select * from EMPLOYEE where EMPID = " + Long.toString(EmpId) + ";";
         rs = mysql.doQuery(query);
         try {
             if (rs.next()) {
                 employee = new Employee(
                         rs.getString("EmpFirstName"),
                         rs.getString("EmpLastName"),
                         rs.getLong("EMPID"),
                         rs.getString("EmpEmail"),
                         rs.getString("EmpType"));
             }
         }
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
          }
          finally {
              mysql.closeAll();
              return employee;
          }
     }
     
     //Jacob: Added Employee update method 
     public static Employee updateBy(Employee employee) {     
         Employee emp = null; 
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         try {
             mysql.stmt = mysql.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             String query = "select * from EMPLOYEE where EMPID =  " + Long.toString(employee.EMPID) + ";";
             rs = mysql.stmt.executeQuery(query);
             if (rs.next()) {
                 rs.updateString("EmpFirstName", employee.firstName);
                 rs.updateString("EmpLastName", employee.lastName);
                 rs.updateString("EmpEmail", employee.email);
                 rs.updateString("EmpType", employee.empType);
                 rs.updateRow();
             }
             rs = mysql.doStatement(query);
             if (rs.next()) {
                 emp = new Employee(
                         rs.getString("EmpFirstName"),
                         rs.getString("EmpLastName"),
                         rs.getLong("EMPID"),
                         rs.getString("EmpEmail"),
                         rs.getString("EmpType"));
             }
         }
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
         }
         finally {
             mysql.closeAll();
             return emp;
         }
     }
     
      public static void addUserLogin(Employee employee) {
         Employee emp = null;
         MysqlConn mysql = new MysqlConn();
         try {
             mysql.stmt = mysql.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             String query = "insert into USER values (0, "
                     + employee.EMPID + ", '"
                     + employee.password + "');";
             mysql.stmt.executeUpdate(query, java.sql.Statement.RETURN_GENERATED_KEYS);   
         }   
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
         }
     }
      
           public static void updateUserLogin(Employee employee) {
         Employee emp = null;
         MysqlConn mysql = new MysqlConn();
         try {
             mysql.stmt = mysql.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             String query = "UPDATE USER SET pass = '"
                     + employee.password + "' WHERE EMPID = "+employee.EMPID+";";
             mysql.stmt.executeUpdate(query, java.sql.Statement.RETURN_GENERATED_KEYS);   
         }   
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
         }
     }
      
      public static boolean empUserExist(long EmpID){
         boolean checkExist = false;
          ResultSet rs;
          MysqlConn mysql = new MysqlConn();
         String query = "select * from USER where EMPID = " + Long.toString(EmpID) +";";
         rs = mysql.doQuery(query);
         try {
             if (rs.next()) {
                 checkExist = true;
                 
             }
         }
         catch (SQLException ex) {
             
         }
         finally{
             mysql.closeAll();
             return checkExist;
         }
         
     }
      
     //Jacob: Added CreateEmp method
     public static Employee createEmp(Employee employee) {
         Employee emp = null;
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         try {
             mysql.stmt = mysql.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             String query = "insert into EMPLOYEE values (0, '"
                     + employee.firstName + "', '"
                     + employee.lastName + "', '"
                     + employee.email + "', '"
                     + employee.empType + "');";
             mysql.stmt.executeUpdate(query, java.sql.Statement.RETURN_GENERATED_KEYS);   
         }   
         catch (SQLException ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage(), "MySQL Error", JOptionPane.ERROR_MESSAGE);
             System.out.println("SQLException: " + ex.getMessage());
             System.out.println("SQLState: " + ex.getSQLState());
             System.out.println("VendorError: " + ex.getErrorCode());
         }
         finally {
             return emp;
         }
     }
     
     //Added isEmployee to check for exist before adding new employee
     public static boolean isEmployee(long EmpID){
         boolean checkExist = false;
          ResultSet rs;
          MysqlConn mysql = new MysqlConn();
         String query = "select * from EMPLOYEE where EMPID = " + Long.toString(EmpID) +";";
         rs = mysql.doQuery(query);
         try {
             if (rs.next()) {
                 checkExist = true;
             }
         }
         catch (SQLException ex) {
             
         }
         finally{
             mysql.closeAll();
             return checkExist;
         }
         
     }
     
     public static ArrayList<String> getOrders(long empid)
     {
         int i = 1;
         String temp = "";
         ArrayList<String> orders = new ArrayList<String>();
         ResultSet rs;
         MysqlConn mysql = new MysqlConn();
         String query = "select ORDERID from `ORDER` WHERE EMPID = " + Long.toString(empid) + ";";
         rs = mysql.doQuery(query);
         
          try {
             while (rs.next()) {
                 temp = rs.getString("ORDERID");
                 orders.add(temp);
                 temp = "";
             }
             return orders;
          }
             catch (SQLException ex) {
             
                     }
          return null;
     }
     
     
     // <editor-fold defaultstate="collapsed" desc="Setters">
     public void setFirstName(String firstName){
         this.firstName = firstName;
     }
     
     public void setLastName(String lastName){
         this.lastName = lastName;
     }
     
     public void setEmpId(Long empId){
         this.EMPID = empId;
     }
     
     public void setEmail(String email){
         this.email = email;
     }
     
     public void setEmpType(String empType){
         this.empType = empType;
     }
     
     public void setPassword(String pass){
         this.password = pass;
     }
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Getters">
     public String getFirstName(){
         return firstName;
     }
     
     public String getLastName(){
         return lastName;
     }
     
     public Long getEmpId(){
         return EMPID;
     }
     
     public String getEmail(){
         return email;
     }
     
     public String getEmpType(){
         return empType;
     }
     // </editor-fold>
 }
