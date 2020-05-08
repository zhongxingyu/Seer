 import java.sql.*; // JDBC stuff.
 import java.io.*;  // Reading user input.
 
 public class StudentPortal
 {
 	/* This is the driving engine of the program. It parses the
 	 * command-line arguments and calls the appropriate methods in
 	 * the other classes.
 	 *
 	 * You should edit this file in two ways:
 	 * 	1) 	Insert your database username and password (no @medic1!)
 	 *		in the proper places.
 	 *	2)	Implement the three functions getInformation, registerStudent
 	 *		and unregisterStudent.
 	 */
 	public static void main(String[] args)
 	{
 		if (args.length == 1) {
 			try {
 				DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
 				String url = "jdbc:oracle:thin:@tycho.ita.chalmers.se:1521/kingu.ita.chalmers.se";
 				String userName = "vtda357_014"; // Your username goes here!
 				String password = "pxfpxf"; // Your password goes here!
 				Connection conn = DriverManager.getConnection(url,userName,password);
 
 				String student = args[0]; // This is the identifier for the student.
 				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 				System.out.println("Welcome!");
 				while(true) {
 					System.out.println("Please choose a mode of operation:");
 					System.out.print("? > ");
 					String mode = input.readLine();
 					if ((new String("information")).startsWith(mode.toLowerCase())) {
 						/* Information mode */
 						getInformation(conn, student);
 					} else if ((new String("register")).startsWith(mode.toLowerCase())) {
 						/* Register student mode */
 						System.out.print("Register for what course? > ");
 						String course = input.readLine();
 						registerStudent(conn, student, course);
 					} else if ((new String("unregister")).startsWith(mode.toLowerCase())) {
 						/* Unregister student mode */
 						System.out.print("Unregister from what course? > ");
 						String course = input.readLine();
 						unregisterStudent(conn, student, course);
 					} else if ((new String("quit")).startsWith(mode.toLowerCase())) {
 						System.out.println("Goodbye!");
 						break;
 					} else {
 						System.out.println("Unknown argument, please choose either information, register, unregister or quit!");
 						continue;
 					}
 				}
 				conn.close();
 			} catch (SQLException e) {
 				System.err.println(e);
 				System.exit(2);
 			} catch (IOException e) {
 				System.err.println(e);
 				System.exit(2);
 			}
 		} else {
 			System.err.println("Wrong number of arguments");
 			System.exit(3);
 		}
 	}
 
 	static void getInformation(Connection conn, String student)
 	{
 		// Your implementation here
 	}
 
 
 	static void registerStudent(Connection conn, String student, String course)
 	{
 		try {
 			Statement myStmt = conn.createStatement();
 			myStmt.executeUpdate("INSERT INTO DBStudentStatus VALUES ('" + student + "', '" + course + "', 'registered')");
 			
 			// Check the new status of the registration
 			ResultSet rs = myStmt.executeQuery("SELECT * FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 			if (rs.next())
 			{
 				String register_result = rs.getString(3);
 				
 				// Get course name
 				rs = myStmt.executeQuery("SELECT name FROM Course WHERE code = '" + course + "'");
 				if (rs.next())
 				{
 					String full_coursename = rs.getString(1);
 					
 					if (register_result.equals("registered"))
 					{
 						System.out.println("You are now successfully registered to course " + course + " " + full_coursename + "!");
 					} else {
 						
 						// Get number on the waiting list
						rs = myStmt.executeQuery("SELECT COUNT(*) FROM DBStudentStatus WHERE coursecode = '" + course + "' AND status = 'waiting'");
 						rs.next();
						System.out.println("Course " + course + " " + full_coursename + " is full, you are put in the waiting list as number " + rs.getString(1) + ".");
 					}
 				} else {
 					System.err.println("Failed to get full course name for course code '" + course + "'.");
 					System.exit(2);
 				}
 				
 			} else {
 				System.out.println("Error: Could not find user entry after registering.");
 				System.out.println("Check so that the user have taken all prerequisite courses!");
 			}
 
 		} catch (SQLException e) {
 			System.err.println(e);
 			System.exit(2);
 		}
 		
 	}
 
 	static void unregisterStudent(Connection conn, String student, String course)
 	{
 		try {
 			Statement myStmt = conn.createStatement();
 			
 			
 			// Check the new status of the registration
 			ResultSet rs = myStmt.executeQuery("SELECT status FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 			if (rs.next())
 			{
 				String reg_status = rs.getString(1);
 				myStmt.executeUpdate("DELETE FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 				
 				if (reg_status.equals("registered"))
 				{
 					System.out.println("You were unregistered from the course.");
 				} else {
 					System.out.println("You were removed from the waiting list for the course.");
 				}
 				
 			} else {
 				System.out.println("You are not registered/on the waiting list for that course!");
 			}
 			
 			/*
 			// Check the new status of the registration
 			ResultSet rs = myStmt.executeQuery("SELECT * FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "'");
 			if (rs.next())
 			{
 				String register_result = rs.getString(3);
 				
 				// Get course name
 				rs = myStmt.executeQuery("SELECT name FROM Course WHERE code = '" + course + "'");
 				if (rs.next())
 				{
 					String full_coursename = rs.getString(1);
 					
 					if (register_result == "registered")
 					{
 						System.out.println("You are now successfully registered to course " + course + " " + full_coursename + "!");
 					} else {
 						
 						// Get number on the waiting list
 						rs = myStmt.executeQuery("SELECT COUNT(*) FROM DBStudentStatus WHERE persnumber = '" + student + "' AND coursecode = '" + course + "' AND status = 'waiting'");
 						rs.next();
 						System.out.println("Course " + course + " " + full_coursename + " is full, you are put in the waiting list as number " + rs.getString(1) + ".");
 					}
 				} else {
 					System.err.println("Failed to get full course name for course code '" + course + "'.");
 					System.exit(2);
 				}
 				
 			} else {
 				System.out.println("Error: Could not find user entry after registering.");
 				System.out.println("Check so that the user have taken all prerequisite courses!");
 			}*/
 
 		} catch (SQLException e) {
 			System.err.println(e);
 			System.exit(2);
 		}
 	}
 }
