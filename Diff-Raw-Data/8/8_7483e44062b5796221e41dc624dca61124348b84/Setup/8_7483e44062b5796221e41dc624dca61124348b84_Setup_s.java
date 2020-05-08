 /*****************************************************************************************
  * 
  * Setup.java
  *  
  * Aaron Averill
  * John Lloyd
  * Matthew Marum
  * Rob Parsons
  * 
  * CSC 540
  * Fall 2012
  * 
  * This file creates the databases and populates 
  * with the data provided by the TA for the demo
  * 
  */
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 
 
 
 public class Setup extends AbstractCommandHandler {
 
 	/*
 	 * Contruct a handler for book objects.
 	 */
 	public Setup(Connection connection) { 
 		super(connection);
 	}
 
 	// Provided Location Values
 	public static String LOCATION_1 = "Books-a-Thousand-1";
 	public static String LOCATION_2 = "Books-a-Thousand-2";
 
 	// Provided Department Values
 	public static String DEPT_MANAGEMENT = "Management";
 	public static String DEPT_SALES = "Sales";
 
 	// Provided JobTitle Values
 	public static String JOB_M01 = "M-Unit-01";
 	public static String JOB_M02 = "M-Unit-02";
 	public static String JOB_S01 = "S-Unit-01";
 	public static String JOB_S02 = "M-Unit-02";
 
 	/**
 	 * Execute the command to create database
 	 * 
 	 */
 	public void execCreate() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("CREATE TABLE Staff (id INTEGER PRIMARY KEY NOT NULL,"+
 				"name VARCHAR(30) NOT NULL,"+
 				"gender CHAR(1) CHECK (gender IN ('F', 'M')),"+
 				"dob DATE,"+
 				"jobTitle VARCHAR(30) "+ 
 				"CONSTRAINT STAFF_TITLE CHECK (jobTitle IN ('Salesperson',"+ 
 				"'Procurement',"+ 
 				"'Warehouse staff',"+ 
 				"'Accounting',"+ 
 				"'Management',"+
 				"'"+JOB_M01+"',"+
 				"'"+JOB_M02+"',"+
 				"'"+JOB_S01+"',"+
 				"'"+JOB_S02+"'"+
 				")),"+
 				"department VARCHAR(30) "+ 
 				"CONSTRAINT STAFF_DEPT CHECK (department IN ('Sales',"+ 
 				"'Procurement',"+ 
 				"'Warehouse',"+ 
 				"'Management',"+ 
 				"'Accounting',"+
 				"'"+DEPT_MANAGEMENT+"',"+
 				"'"+DEPT_SALES+"'"+
 				")),"+
 				"salary INTEGER "+ 
 				"CHECK (salary > 0),"+
 				"phone VARCHAR(12) NOT NULL,"+
 				"address VARCHAR(255) NOT NULL,"+
 				"workLocation VARCHAR(30) "+
 				"CONSTRAINT STAFF_LOCATION CHECK (workLocation IN ('Southpoint',"+ 
 				"'Northgate',"+ 
 				"'Airport Mall',"+ 
 				"'Concord Mills',"+ 
 				"'Jungle Jims',"+
 				"'warehouse',"+
 				"'HQ',"+
 				"'"+LOCATION_1+"',"+
 				"'"+LOCATION_2+"'"+
 				"))"+
 				")");
 		sqlArray.add("CREATE TABLE Customer ("+
 				"id INTEGER PRIMARY KEY NOT NULL,"+
 				"gender CHAR(1) CHECK (gender IN ('F', 'M')),"+
 				"dob DATE,"+
 				"status CHAR(8) CHECK (status IN ('active', 'inactive')),"+
 				"phone VARCHAR(20),"+
 				"ssn CHAR(11),"+
 				"name VARCHAR(30) NOT NULL,"+
 				"address VARCHAR(255) NOT NULL "+
 				")");
 		sqlArray.add("CREATE TABLE Vendor ("+
 				"id INTEGER PRIMARY KEY NOT NULL,"+
 				"phone VARCHAR(12) NOT NULL,"+
 				"name VARCHAR(30) NOT NULL,"+ 
 				"address VARCHAR(255) NOT NULL," +
 				"endDate DATE NOT NULL "+
 				")");
 		sqlArray.add("CREATE TABLE Book ("+
 				"id INTEGER PRIMARY KEY NOT NULL,"+
 				"retailPrice INTEGER CHECK (retailPrice >= 0),"+
 				"stockQuantity INTEGER CHECK (stockQuantity >= 0),"+
 				"title VARCHAR(30) NOT NULL,"+
 				"author VARCHAR(30) NOT NULL "+
 				")");
 		sqlArray.add("CREATE TABLE Orders ("+
 				"id INTEGER PRIMARY KEY NOT NULL,"+ 
 				"staffId INTEGER NOT NULL,"+ 
 				"customerId INTEGER NOT NULL,"+ 
 				"status CHAR(8) CHECK (status IN ('ordered', 'received', 'shipped')),"+ 
 				"orderDate DATE NOT NULL,"+ 
 				"FOREIGN KEY (staffId) REFERENCES Staff(id),"+
 				"FOREIGN KEY (customerId) REFERENCES Customer(id) "+
 				")");
 		sqlArray.add("CREATE TABLE ItemOrder ("+
 				"orderId INTEGER NOT NULL,"+ 
 				"bookId INTEGER NOT NULL,"+ 
 				"salePrice INTEGER CHECK (salePrice >= 0),"+ 
 				"quantity INTEGER  CHECK (quantity >= 1),"+ 
 				"FOREIGN KEY (orderId) REFERENCES Orders(id) ON DELETE CASCADE,"+ 
 				"FOREIGN KEY (bookId) REFERENCES Book(id) " +
 				")");
 		sqlArray.add("CREATE TABLE Purchase ("+
 				"id INTEGER PRIMARY KEY NOT NULL,"+ 
 				"orderDate DATE NOT NULL,"+ 
 				"bookId INTEGER NOT NULL,"+ 
 				"vendorId INTEGER NOT NULL,"+ 
 				"staffId INTEGER NOT NULL,"+ 
 				"quantity INTEGER CHECK (quantity >= 1),"+ 
 				"status CHAR(8) CHECK (status IN ('ordered', 'received', 'shipped', 'paid')),"+ 
 				"wholesalePrice INTEGER CHECK (wholesalePrice >= 0),"+ 
 				"paidDate DATE,"+ 
 				"FOREIGN KEY (bookId) REFERENCES Book(id) ,"+ 
 				"FOREIGN KEY (vendorId) REFERENCES Vendor(id),"+ 
 				"FOREIGN KEY (staffId) REFERENCES Staff(id) "+
 				")");
 		sqlArray.add("CREATE TABLE Stocks ("+
 				"bookId INTEGER NOT NULL,"+
 				"vendorId INTEGER NOT NULL,"+
 				"FOREIGN KEY (bookId) REFERENCES Book(id) ,"+
 				"FOREIGN KEY (vendorId) REFERENCES Vendor(id) "+
 				")");
 
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Execute the command to drop database
 	 * 
 	 */
 	public void execDrop() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("DROP TABLE Stocks");
 		sqlArray.add("DROP TABLE Purchase");
 		sqlArray.add("DROP TABLE ItemOrder");
 		sqlArray.add("DROP TABLE Orders");
 		sqlArray.add("DROP TABLE Book");
 		sqlArray.add("DROP TABLE Vendor");
 		sqlArray.add("DROP TABLE Customer");
 		sqlArray.add("DROP TABLE Staff");
 
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 
 	}
 
 	/**
 	 * Execute the commands to populate all tables
 	 * 
 	 */
 	public void execPop() throws SQLException {
 		execPopS(); // populate staff
 		execPopC(); // populate customers
 		execPopB(); // populate books
 		execPopV(); // populate vendors
 		execPopP(); // populate purchases
 
 	}
 
 	/**
 	 * Execute the commands to populate database
 	 * 
 	 */
 	public void execPopS() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("INSERT INTO Staff VALUES ("+
 				"1001,"+
 				"'Mr. S. First',"+
 				"'M',"+
 				"'12-aug-1961',"+
 				"'"+JOB_M01+"',"+
 				"'"+DEPT_MANAGEMENT+"',"+
 				"43000,"+
 				"'430-324-0943',"+
 				"'132 Red Street',"+
 				"'"+LOCATION_1+"' "+
 				")");
 		sqlArray.add("INSERT INTO Staff VALUES ("+
 				"1002,"+
 				"'Mrs. S. Second',"+
 				"'F',"+
 				"'12-aug-1961',"+
 				"'"+JOB_M02+"',"+
 				"'"+DEPT_MANAGEMENT+"',"+
 				"40000,"+
 				"'324-192-8765',"+
 				"'111 Rose Dr',"+
 				"'"+LOCATION_1+"' "+
 				")");
 
 		sqlArray.add("INSERT INTO Staff VALUES ("+
 				"1003,"+
 				"'Mr. S. Third',"+
 				"'M',"+
 				"'12-may-1937',"+
 				"'"+JOB_S01+"',"+
 				"'"+DEPT_SALES+"',"+
 				"30000,"+
 				"'129-430-3784',"+
 				"'54 Purple Rd',"+
 				"'"+LOCATION_1+"' "+
 				")");
 		sqlArray.add("INSERT INTO Staff VALUES ("+
 				"1004,"+
 				"'Mr. S. Forth',"+
 				"'M',"+
 				"'24-feb-1948',"+
 				"'"+JOB_M01+"',"+
 				"'"+DEPT_MANAGEMENT+"',"+
 				"42000,"+
 				"'774-398-3421',"+
 				"'98 Jester Ct',"+
 				"'"+LOCATION_2+"' "+
 				")");
 		sqlArray.add("INSERT INTO Staff VALUES ("+
 				"1005,"+
 				"'Mr. S. Fifth',"+
 				"'M',"+
 				"'12-jun-1943',"+
 				"'"+JOB_S01+"',"+
 				"'"+DEPT_SALES+"',"+
 				"29000,"+
 				"'102-394-3243',"+
 				"'34 Pinewood st',"+
 				"'"+LOCATION_2+"' "+
 				")");
 		sqlArray.add("INSERT INTO Staff VALUES ("+
 				"1006,"+
 				"'Mr. S. Sixth',"+
 				"'M',"+
 				"'03-mar-1953',"+
 				"'"+JOB_S02+"',"+
 				"'"+DEPT_SALES+"',"+
 				"25000,"+
 				"'888-321-5843',"+
 				"'13 Oakland lane',"+
 				"'"+LOCATION_2+"' "+
 				")");
 
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Execute the commands to populate customer table
 	 * 
 	 */
 	public void execPopC() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("INSERT INTO Customer VALUES ("+
 				"2001,"+
 				"'M',"+
 				"'23-mar-1974',"+
 				"'active',"+
 				"'201-123-5321',"+
 				"'392-82-1942',"+
 				"'Mr. C First',"+
 				"'101 Russet St'"+
 				")");
 		sqlArray.add("INSERT INTO Customer VALUES ("+
 				"2002,"+
 				"'F',"+
 				"'19-sep-80',"+
 				"'active',"+
 				"'102-394-6492',"+
 				"'292-81-8782',"+
 				"'Mrs. C Second',"+
 				"'102 Golden Ln'"+
 				")");
 		sqlArray.add("INSERT INTO Customer VALUES ("+
 				"2003,"+
 				"'M',"+
 				"'12-nov-1964',"+
 				"'active',"+
 				"'908-483-2853',"+
 				"'122-02-1342',"+
 				"'Mr. C Third',"+
 				"'103 Sweet Ct'"+
 				")");
 		sqlArray.add("INSERT INTO Customer VALUES ("+
 				"2004,"+
 				"'M',"+
 				"'12-dec-1955',"+
 				"'active',"+
 				"'166-983-2837',"+
 				"'735-82-1232',"+
 				"'Mr. C Forth',"+
 				"'104 Mashed Rd'"+
 				")");
 
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 
 	}
 	
 
 	/**
 	 * Execute the commands to populate book table
 	 * 
 	 */
 	public void execPopB() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("INSERT INTO Book VALUES ("+
 				"4001,"+
 				"725,"+
 				"7,"+
 				"'ISBN 1234',"+
 				"'Robert Hooke'"+
 				")");
 		sqlArray.add("INSERT INTO Book VALUES ("+
 				"4002,"+
 				"650,"+
 				"10,"+
				"'ISBN 1234',"+
 				"'Joe Bob'"+
 				")");		
 		sqlArray.add("INSERT INTO Book VALUES ("+
 				"4003,"+
 				"700,"+
 				"6,"+
				"'ISBN 1234',"+
 				"'Author Fry'"+
 				")");
 		sqlArray.add("INSERT INTO Book VALUES ("+
 				"4004,"+
 				"5,"+
 				"6,"+
				"'ISBN 1234',"+
 				"'Bill Gates'"+
 				")");		
 		
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 
 	}
 
 	/**
 	 * Execute the commands to populate vendor table
 	 * 
 	 */
 	public void execPopV() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("INSERT INTO Vendor VALUES ("+
 				"3001,"+
 				"'505-435-1029',"+
 				"'Turners Inc.',"+
 				"'34 Page St',"+
 				"'1-jun-2012'"+
 				")");
 
 		sqlArray.add("INSERT INTO Vendor VALUES ("+
 				"3002,"+
 				"'234-432-9485',"+
 				"'Print and Go',"+
 				"'432 Letter Lane',"+
 				"'20-sep-2012'"+
 				")");
 		
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 
 	}
 	
 	/**
 	 * Execute the commands to populate purchases from vendor table
 	 * 
 	 */
 	public void execPopP() throws SQLException {
 		ArrayList<String> sqlArray = new ArrayList<String>();
 
 		sqlArray.add("INSERT INTO Purchase VALUES ("+
 				"6001,"+
 				"'5-feb-2012',"+
 				"4001,"+ // ISBN 1234
 				"3001,"+ // Turners
 				"1003,"+ 
 				"10," + // qty
 				"'paid',"+
 				"301," + // wholesale
 				"'26-feb-2012'"+
 				")");
 		sqlArray.add("INSERT INTO Purchase VALUES ("+
 				"6002,"+
 				"'25-mar-2012',"+
 				"4002,"+ // ISBN 1235
 				"3002,"+ // Print and Go
 				"1003,"+ 
 				"12," + // qty
 				"'paid',"+
 				"302," + // wholesale
 				"'17-apr-2012'"+
 				")");
 		sqlArray.add("INSERT INTO Purchase VALUES ("+
 				"6003,"+
 				"'2-june-2012',"+
 				"4003,"+ // ISBN 1236
 				"3001,"+ // Print and Go
 				"1003,"+ 
 				"6," + // qty
 				"'paid',"+
 				"303," + // wholesale
 				"'26-feb-2012'"+
 				")");
 		sqlArray.add("INSERT INTO Purchase VALUES ("+
 				"6004,"+
 				"'1-oct-2012',"+
 				"4004,"+ // ISBN 1237
 				"3001,"+ // Turners
 				"1003,"+ 
 				"9," + // qty
 				"'paid',"+
 				"304," + // wholesale
 				"'5-feb-2012'"+
 				")");
 		
 		for (String sql : sqlArray) {
 			Statement statement = connection.createStatement();
 			statement.setQueryTimeout(10);
 			System.out.println(sql);
 			try {
 				statement.executeUpdate(sql);
 			} catch (Exception e) {
 				System.out.println(e.getMessage());
 			}
 		}
 
 	}
 }
