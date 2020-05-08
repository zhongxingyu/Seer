 package storage;
 
 import java.io.*;
 import java.util.Scanner;
 
 /**
  * Reads and writes to a player's profile to store game statistics <br>
  * File format: <br>
  * file name: username.csv <br>
  * password, realName, age, numChips, totalChipsWon, maxWinStreak, maxChipsWinStreak, maxLossStreak, maxChipsLossStreak <br>
  * <br>
  *  realName is an optional attribute and has default value "noName" <br>
  *  age is an optional attribute and has default value -1 <br>
  *  
  *  @author Gabriel
  */
 
 public class Storage {
 
 	/**
 	 * Creates a new profile file if the input username is not already taken
 	 * @param username
 	 * @param password
 	 */
	
	private static boolean addNewPlayer(String username, String password) throws IllegalArgumentException{
 		File fileName = new File("profiles/" + username + ".csv");
 		if (fileName.isFile()) {
 			throw new IllegalArgumentException("Player already exists.");
 		} else {
 			Statistics newPlayer = new Statistics(username,password);
 			newPlayer.addChips(500);
 			savePlayer(newPlayer);
 			return true;
 		}
 	}
 	
 	/**
 	 * Loads a player's profile if its name and password are valid
 	 * @param inputName - String of the user's name
 	 * @param inputPassword - String of the user's password
 	 * @return a Storage object containing a player's statistics
 	 */
 
 	public static Statistics loadPlayer(String inputName, String inputPassword) {
 		inputName = inputName.toLowerCase();
 		String[] parsedLine;
 		try {
 			 Scanner reader = new Scanner(new File("profiles/" + inputName + ".csv"));
 			 
 			 // Split line from file into different fields
 			 parsedLine = reader.nextLine().split(",");
 			 
 			 // load variables from the parsed line
 			 String password = parsedLine[0];
 			 String realName = parsedLine[1];
 			 int age = Integer.parseInt(parsedLine[2]);
 			 int numChips = Integer.parseInt(parsedLine[3]);
 			 int totalMoneyWon = Integer.parseInt(parsedLine[4]);
 			 int maxWinStreak = Integer.parseInt(parsedLine[5]);
 			 int maxChipsWinStreak = Integer.parseInt(parsedLine[6]);
 			 int maxLossStreak = Integer.parseInt(parsedLine[7]);
 			 int maxChipsLossStreak = Integer.parseInt(parsedLine[8]);
 			 
 			 // Instantiate player with the loaded variables
 			 Statistics loadedPlayer = new Statistics(inputName, password,
 					 							realName, age,
 					 							numChips, totalMoneyWon,
 					 							maxWinStreak,
 					 							maxChipsWinStreak,
 					 							maxLossStreak,
 					 							maxChipsLossStreak);
 			reader.close();
 
 			 if (loadedPlayer.validatePassword(inputPassword)) {
 				 return loadedPlayer;
 			 } else {
 				 throw new IllegalArgumentException("Invalid password");
 			 }
 		} catch (FileNotFoundException e){
			addNewPlayer(inputName, inputPassword);
			return loadPlayer(inputName, inputPassword);
 		}
 	}
 	/**
 	 * Writes a Statistics object's information back to the profile file
 	 * @return true if it saved successfully, false otherwise
 	 */
 	public static boolean savePlayer(Statistics player) {
 		try {
 			FileWriter fstream = new FileWriter("profiles/" + player.getUsername() + ".csv");
 			BufferedWriter writer = new BufferedWriter(fstream);
 
 			writer.write(player.getPassword() + ","
 					+ player.getRealName() + ","
 					+ player.getAge() + ","
 					+ player.getChips() + ","
 					+ player.getTotalChipsWon() + ","
 					+ player.getMaxWinStreak() + ","
 					+ player.getMaxChipsWinStreak() + ","
 					+ player.getMaxLossStreak() + ","
 					+ player.getMaxChipsLossStreak());
 			writer.close();
 			return true;
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Could not save this player.");
 		}
 	}
 	
 	
 	
 }
