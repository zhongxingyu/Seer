 package gui;
 import java.util.Scanner;
 import gameEngine.*;
 import participant.*;
 import storage.*;
 
 /**
  * MainInterface displays the command line interface for the main menu
  * @author JackGivesBack
  *
  */
 public class MainInterface
 {
 	/**
 	 * Creates a new MainInterface object to display
 	 * @param args Standard command line arguments (not used)
 	 */
 	public static void main(String[] args) {
 		new MainInterface();
 	}
 	/**
 	 * MainInterface prints the options to screen, which the user selects with the keyboard input
 	 */
 	MainInterface() {
 		
 		int menuChoice=-1;
 		final int PLAYGAME = 1;
 		final int MODIFYPROFILE = 2;
 		final int VIEWSTATISTICS = 3;
 		final int HALLOFFAME = 4;
 		final int SWITCHUSER = 5;
 		final int EXIT = 6;
 		
 		Scanner keyboard = new Scanner(System.in);
 		Player player = login();
 		while (menuChoice != EXIT) {
 			System.out.println("------Main Menu------");
 			System.out.println("Logged in as : " + player.getUsername());
 			System.out.println("1. Play Blackjack");
 			System.out.println("2. Modify Profile");
 			System.out.println("3. View Statistics");
 			System.out.println("4. Show Hall of Fame");
 			System.out.println("5. Switch user");
 			System.out.println("6. Exit");
 			menuChoice = keyboard.nextInt();
 			keyboard.nextLine();
 			
 			if (menuChoice == PLAYGAME) {
 				GameEngine gameEngine = new GameEngine(player);
 				new GameEngineInterface(gameEngine);
 			}
 			if (menuChoice == MODIFYPROFILE) {
 				modifyProfile(player);
 			}
 			if (menuChoice == VIEWSTATISTICS) {
 				viewStats(player);
 			}
 			if (menuChoice == HALLOFFAME) {
 				HallOfFame.display();
 			}
 			if (menuChoice == SWITCHUSER) {
 				player = login();
 			}
 		}
 		
 	}
 	
 	/**
 	 * Login gets a username and password from keyboard and validates it with the Player class
 	 * @return The Player object that is created when a user correctly logs in
 	 */
 public static Player login() {
 		
 		int menuChoice=-1;
 		final int EXUSER = 1;
 		final int NEWUSER = 2;
 		final int EXIT= 3;
 		String username = "";
 		String password = "";
 		boolean loggedIn = false;
 		
 		
 		Scanner keyboard = new Scanner(System.in);
 		while (menuChoice != EXIT) {
 		
 	
			System.out.println("---User Menu---");
			System.out.println("1. Existing users");
			System.out.println("2. New users");
 		
			menuChoice=keyboard.nextInt();
			keyboard.nextLine();
 			
 		    if(menuChoice==EXUSER){
 		    	
 		        System.out.println("---Existing user---");
 		    	while (!loggedIn) {
 		    		
 					System.out.println("Enter username: ");
 					username = keyboard.nextLine();
 					
 					System.out.println("Enter password: ");
 					password = keyboard.nextLine();
 					
 					try {
 						new Player(username, password);
						menuChoice = EXIT;
 						loggedIn = true;
 					} catch (Exception IllegalArgumentException) {
 						System.out.println("Invalid entry, try again!");
 					}
 				}
 				
 		    }
 		    else if (menuChoice==NEWUSER){
 		    	System.out.println("---Create new user---");
 		 	   
 		 	   while(!loggedIn){
 		 		   System.out.println("Enter username: ");
 		 	       username= keyboard.nextLine();
 		 	   
 		 	       System.out.println("Enter password: ");
 		 	       password = keyboard.nextLine();
 		 	       
 			 	   try {
			 		   Storage.addNewPlayer(username, password);
			 		   new Player(username, password);
			 		   menuChoice = EXIT;
			 		   loggedIn = true;
 			 			
 			 		} catch (Exception IllegalArgumentException ) {
 			 			System.out.println("Invalid entry, try again!");
 			 			loggedIn = false;
 			 		}
 		 	   }
 		 
 		    }
 		}
 		return new Player(username, password);
 	}
 	
 	
 	/**
 	 * Modify profile menu shows options such as change real name, age, and password
 	 * @param player The player whose profile will be modified
 	 */
 	public static void modifyProfile(Player player) {
 		int menuChoice=-1;
 		final int EXIT = 4;
 		final int CHANGEPASS = 1;
 		final int CHANGENAME = 2;
 		final int CHANGEAGE = 3;
 		
 		Scanner keyboard = new Scanner(System.in);
 		while (menuChoice != EXIT) {
 			System.out.println("------Modify Profile------");
 			System.out.println("1. Change Password");
 			System.out.println("2. Change real name");
 			System.out.println("3. Change age");
 			System.out.println("4. Back to main menu");
 			
 			menuChoice = keyboard.nextInt();
 			keyboard.nextLine();
 			
 			if (menuChoice == CHANGEPASS) {
 				System.out.println("Enter your old password: ");
 				String oldPass = keyboard.nextLine();
 				System.out.println("Enter your new password: ");
 				String newPass = keyboard.nextLine();
 				if (player.toStatistics().validatePassword(oldPass)) {
 					player.toStatistics().changePassword(newPass);
 					Storage.savePlayer(player.toStatistics());
 				} else {
 					System.out.println("Password was invalid.");
 				}
 			}
 			if (menuChoice == CHANGENAME) {
 				System.out.println("Enter your real name: ");
 				String name = keyboard.nextLine();
 				player.toStatistics().setRealName(name);
 				Storage.savePlayer(player.toStatistics());
 			}
 			if (menuChoice == CHANGEAGE) {
 				System.out.println("Enter your age: ");
 				int age = keyboard.nextInt();
 				keyboard.nextLine();
 				player.toStatistics().setAge(age);
 				Storage.savePlayer(player.toStatistics());
 			}
 		}
 	}
 	
 	/**
 	 * ViewStats uses the Statistics file's toString method to display player statistics
 	 * @param player The player whose statistics will be displayed
 	 */
 	public static void viewStats(Player player) {
 		System.out.print(player);
 	}
 }
