 package x464010.teamb.srs;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
 * @author Amit Dhamija
  * @version 1.0
  *
  */
 public class NewStudentAccount extends Console {
 
 	/**
 	 * 
 	 */
 	public NewStudentAccount() {
 		// TODO Auto-generated constructor stub
 	}
 
 	
 	@Override
 	protected void getInput(Scanner inputScanner) {
 		
 		ArrayList<Student> studentList = new ArrayList<Student>();
		//Scanner userInputScanner = new Scanner(System.in).useDelimiter("\\z");
 		// Need to add logic where existing student IDs are read in and a new unique student ID is created
 		// For now, force student ID to be temp ID 223456
 		int newStudentID = 223456;
 
 		System.out.println("Please enter your First Name");
 		String newFirstName = inputScanner.next();
 
 		System.out.println("Please enter your Last Name");
 		String newLastName = inputScanner.next();
 
 		System.out.println("Please enter your Street Address");
 		String newStreetAddress = inputScanner.next();
 
 		System.out.println("Please enter your City");
 		String newCity = inputScanner.next();
 
 		System.out.println("Please enter your State (2-letter initials)");
 		String newState = inputScanner.next();
 
 		System.out.println("Please enter your Zip Code (5-digit only)");
 		String newZip = inputScanner.next();
 
 		System.out.println("Please create a password");
 		String newPassword = inputScanner.next();
 
 		BufferedWriter buffWriter = null;
 		try {
 			buffWriter = new BufferedWriter(new FileWriter("student.txt",true));
 			buffWriter.newLine();
 			buffWriter.write(newStudentID + "," + newFirstName.trim() + "," + newLastName.trim() + "," +
 							 newStreetAddress.trim() + "," + newCity.trim() + "," + newState.trim() + "," +
 							 newZip.trim() + "," + newPassword.trim());
 			buffWriter.close();
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 	}
 }
