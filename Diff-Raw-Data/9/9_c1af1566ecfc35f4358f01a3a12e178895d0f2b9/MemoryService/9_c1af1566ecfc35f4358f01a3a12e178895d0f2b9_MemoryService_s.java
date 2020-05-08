// $codepro.audit.disable questionableAssignment, booleanMethodNamingConvention
 /**
  * Contains MemoryService class
  */
 package com.cs2340.spacetrader;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.content.Context;
 
 /**
  * Note that both the methods in this class are static methods. Do not
  * instantiate an object of this class. Only one game can be saved at a time.
  * Calling savedGame() overwrites any previously saved game.
  * 
  * How to use this class to save and load a SpaceTrader game:
  * 
  * 
  * How to save a game: Step 1: Create a game state that you want to save. Use
  * SaveState class to capture the state of a game. The SaveState object wraps a
  * Map and a Player. SaveState state = new SaveState(map, player); Step 2: Call
  * the saveGame() method. This method takes two parameters: the SaveState object
  * you created in Step 1 and the present Context MemoryService.saveGame(state,
  * context); Step 3: Write a Toast to the user indicating if the save was
  * successful. The method returns true if save was successful, else returns
  * false.
  * 
  * How to load the last saved game: Step 1: Create a SaveState variable. Lets
  * call this "state". The load() method takes in this variable. If the method
  * returns successfully, this variable will point to the current state of the
  * game. SaveState state; // this will point to null at this point. Step 2: Call
  * loadGame(). This takes two parameters. The SaveState field you created in
  * Step 1 and the present context. If the loadGame() method returns false, note
  * that the "state" you passed in still POINTS TO NULL.
  * MemoryService.loadGame(state, context); Step 3: Indicate if the load was
  * successful depending on the boolean returned by the method.
  * 
  * 
  * @author Nikhil Dev
  * @version 1.0
  * @see SaveState, Toast
  */
 
 public class MemoryService {
 	
 	/**
 	 * the name of the file that holds the saved games.
 	 */
 	private static final String FILENAME = "savedSpaceTrader";
 
 	/**
 	 * saves the state of the game. returns true if save was successful, else
 	 * returns false.
 	 * 
 	 * @param state
 	 * @param context
 	 * @return boolean
 	 */
 	public static boolean saveGame(SaveState state, Context context) {
 		final FileOutputStream fos;
 		try {
 			fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			try {
 				final ObjectOutputStream objStream;
 				objStream = new ObjectOutputStream(fos);
 				objStream.writeObject(state);
 				fos.close();
 				objStream.close();
 				return true; // return true because the saving has occurred
 								// without any exceptions being thrown.
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * loads the last saved game. returns true if load was successful, else
 	 * returns false.
 	 * 
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!READ THIS:
 	 * 
 	 * If the method returns true (load is successful), the variable "state"
 	 * points to the last saved state of the game. If the method returns false,
 	 * "state" points to the same object that it was pointing to before the
 	 * method was invoked.
 	 * 
 	 * @param state
 	 *            this is the SaveState pointer.
 	 * @param context
 	 * @return boolean
 	 */
 
 	public static boolean loadGame(SaveState state, Context context) {
 		final FileInputStream fis;
 		try {
 			fis = context.openFileInput(FILENAME);
 			try {
 				final ObjectInputStream objStream = new ObjectInputStream(fis);
 				try {
 					state = (SaveState) objStream.readObject();
 					fis.close();
 					objStream.close();
 					return true; // return true because the saving has occurred
 									// without any exceptions being thrown.
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 					return false;
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * returns the name of the file used to save a game.
 	 * @return FILENAME
 	 */
 	public String toString(){
 		return FILENAME;
 	}
 }
