 package edu.txstate.hearts.utils;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 
 import edu.txstate.hearts.model.Card;
 
 
 public abstract class ReadFiles {
 	private static List<String> listOfUsers;
 	private static Map<String,List<String>> readAchievements;
 	private static String cardImagesFolder = "images\\cards\\";
 	private static String mainImagesFolder = "images\\";
 	
 	public static Image getCardImage(Card card)
 	{
 		String suitName = "";
 		String faceName = "";
 		
 		switch (card.getSuit())
 		{
 			case Clubs:
 				suitName = "c";
 				break;
 			case Diamonds:
 				suitName = "d";
 				break;
 			case Hearts:
 				suitName = "h";
 				break;
 			case Spades:
 				suitName = "s";
 				break;
 		}
 		
 		switch (card.getFace())
 		{
 			case Deuce:
 				faceName = "2";
 				break;
 			case Three:
 				faceName = "3";
 				break;
 			case Four:
 				faceName = "4";
 				break;
 			case Five:
 				faceName = "5";
 				break;
 			case Six:
 				faceName = "6";
 				break;
 			case Seven:
 				faceName = "7";
 				break;
 			case Eight:
 				faceName = "8";
 				break;
 			case Nine:
 				faceName = "9";
 				break;
 			case Ten:
 				faceName = "t";
 				break;
 			case Jack:
 				faceName = "j";
 				break;
 			case Queen:
 				faceName = "q";
 				break;
 			case King:
 				faceName = "k";
 				break;
 			case Ace:
 				faceName = "a";
 				break;
 		}
 		
 		String fileName = faceName + suitName + ".png";
 		String fullFileName = cardImagesFolder + fileName;
 		
 		Image image = null;
 		try
 		{
 			image = ImageIO.read(new File(fullFileName));
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return image;
 	}
 	
 	public static Image getImage(String fileName)
 	{
 		String fullFileName = mainImagesFolder + fileName;
 		
 		Image image = null;
 		try
 		{
 			image = ImageIO.read(new File(fullFileName));
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return image;
 	}
 	
 	private static Scanner openFile(String fileName) throws FileNotFoundException{
 		try{
 			File file = new File(fileName + ".txt");
 			Scanner input = new Scanner(file);
 			return input;
 		}
 		catch(FileNotFoundException filenotfound){
 			throw filenotfound;
 		}
 	}//end of open file
 	
 	public static void addUserRecord(String userName)
 	{
 		if(listOfUsers == null)
 		{
 			listOfUsers = new ArrayList<String>();
 		}
 		listOfUsers.add(userName);
 	}
 	
 	public static List<String> readUserRecords() throws FileNotFoundException {
 		if (listOfUsers == null) {
 			listOfUsers = new ArrayList<String>();
 			Scanner input = openFile("Users");
 
 			try {
				while (input.hasNext()) {
					String user = input.next();
 					listOfUsers.add(user);
 				}// end while
 			}// end try
 			catch (NoSuchElementException nosuchelement) {
 				System.err.println("File improperly formed");
 				throw nosuchelement;
 			} catch (IllegalStateException stateException) {
 				System.err.println("Error reading from file");
 				throw stateException;
 			} finally
 			{
 			  closeFile(input);
 			}
 			
 		}
 		return listOfUsers;
 	}// end of readRecords
 	
 	public static List readAchievements(String playerName) throws FileNotFoundException {
 
 		if (readAchievements == null) {
 			readAchievements = new HashMap<String,List<String>>();
 		}
 		if(readAchievements.get(playerName) == null)
 		{
 			List playerAchievements = new ArrayList<String>();
 			Scanner input = openFile(playerName);
 			try {
 				while (input.hasNext()) {
 					String achievement = input.nextLine();
 					playerAchievements.add(achievement);
 				}
 				readAchievements.put(playerName, playerAchievements);
 
 			} catch (NoSuchElementException nosuchelement) {
 				System.err.println("File improperly formed");
 				throw nosuchelement;
 			} catch (IllegalStateException stateException) {
 				System.err.println("Error reading from file");
 				throw stateException;
 			} finally {
 				closeFile(input);
 			}
 		}
 		return readAchievements.get(playerName);
 	}
 	
 	public static Vector<String> getRecords(){
 		if(listOfUsers == null)
 		{
 			try {
 				readUserRecords();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return new Vector(listOfUsers);
 	}
 	
 	public static List<String> getReadAchievements(String playerName){
 		if(readAchievements == null || readAchievements.get(playerName) == null)
 		{
 			try {
 				readAchievements(playerName);
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return readAchievements.get(playerName);
 	}
 	
 	private static void closeFile(Scanner input){
 		if(input != null)
 			input.close();
 	}
 }//end of class
