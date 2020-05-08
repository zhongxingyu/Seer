 package presentation;
 import java.util.Scanner;
 
 public class Boundary {
 	Scanner scan = new Scanner(System.in);
 	/*
 	 * prompts user for an integer. if you dont need a message passed, you have to pass an empty string
 	 */
 	public int promptForInt(String str){
 		System.out.println(str);
		int integer = scan.nextInt();
		return integer;
 	}
 	/*
 	 * Prints out a string
 	 */
 	public void printLine(String str){
 		System.out.println(str);
 	}
 	/*
 	 * Display prombtinfo and returns input
 	 */
 	public String promptForString(String str){
 		System.out.println(str);
 		String input = scan.nextLine();
 		return input;
 	}
 	
 	/*
 	 * Gets a password from user, and makes sure that he typed it right by making him type it twice
 	 */
 	public String getPassword(){
 		String pass1, pass2;
 		do{
 			System.out.println("Type a password: ");
 			pass1 = scan.nextLine();
 			System.out.println("Repeat the password: ");
 			pass2 = scan.nextLine();
 			if(!pass1.equals(pass2))
 				System.out.println("They do not match. Try again");
 		}while(!pass1.equals(pass2));
 		return pass1;
 	}
 	
 	/*
 	 * displayLoggedInMenu displays menu when you are logged in
 	 */
 	public void displayLoggedInMenu(){
 		seperator();
 		System.out.println("Indtast tallet p dit valg \n\n" +
 						   "(1) - Tilfj Destination \n" +
 						   "(2) - Find Venner \n" +
 						   "(3) - Se Venners Profiler \n" +
 						   "(4) - Log Ud");
 		seperator();
 	}
 	
 	public void seperator(){
 		System.out.println("-----------------------------");
 	}
 }
