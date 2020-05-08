 package org.coms362.group7.MetaMan;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 /**
  * MetaManCommandLineView is a command prompt view of the MetaMan Application.
  * MetaManCommandLineView is one of possibly more views of the MetaMan
  * application.
  * 
  * @author COM S 362 Group 7
  * 
  */
 public class MetaManCommandLineView {
 
 	/**
 	 * The controller is what this view will be the interface between this view
 	 * and the MetaMan object.
 	 */
 	private final MetaManController controller;
 
 	/**
 	 * A mapping of user commands to real functions. For instance when a user
 	 * types in 'ls' this map points 'ls' to listing()
 	 */
 	private HashMap<String, String> operation_map;
 
 	/**
 	 * The last command a user entered
 	 */
 	private String userCmd;
 
 	/**
 	 * The raw user input from the last time the user pressed enter
 	 */
 	private String userInput;
 
 	/**
 	 * The params of the last user command entered
 	 */
 	private String[] userParams;
 
 	/**
 	 * Constructs the command line view of MetaMan.
 	 * 
 	 * @param startingDirectory
 	 *            The directory MetaMan should start in.
 	 * @throws MetaManException
 	 *             When a error occurs starting up MetaMan
 	 */
 	public MetaManCommandLineView(File startingDirectory)
 			throws MetaManException {
 		this.controller = new MetaManController(startingDirectory);
 		this.populateOperationMap();
 	}
 
 	/**
 	 * Informs the controller that the user wishes to change the directory they
 	 * are working in.
 	 * 
 	 * @param newPath
 	 *            The path the user wishes to change to.
 	 */
 	private void cd(String newPath) {
 		if (!this.controller.changeDirectory(newPath)) {
 			this.println("MetaMan could not find the path given.");
 		}
 		this.println();
 	}
 
 	/**
 	 * Deletes the selected file
 	 * 
 	 */
 	private void delete() {
 		if (this.controller.deleteSelectedFile()) {
 			this.println("File was deleted.");
 		} else {
 			this.println("File was NOT deleted.  Try again later.");
 		}
 
 	}
 
 	/**
 	 * Executes the users command
 	 * 
 	 * @throws IOException
 	 */
 	private void executeUserCommand() {
 		try {
 
 			if (this.operation_map.get(this.userCmd).equals("pwd")) {
 				this.pwd();
 			} else if (this.operation_map.get(this.userCmd).equals("exit")) {
 				this.exit();
 			} else if (this.operation_map.get(this.userCmd).equals("ls")) {
 				this.ls();
 			} else if (this.operation_map.get(this.userCmd).equals("up")) {
 				this.up();
 			} else if (this.operation_map.get(this.userCmd).equals("cd")) {
 				String newValue = "";
 				for (final String userParam : this.userParams) {
 					newValue += userParam + " ";
 				}
 				this.cd(newValue.trim());
 			} else if (this.operation_map.get(this.userCmd).equals("help")) {
 				this.help();
 			} else if (this.operation_map.get(this.userCmd).equals("lsao")) {
 				this.lsao();
 			} else if (this.operation_map.get(this.userCmd).equals("lsdo")) {
 				this.lsdo();
 			} else if (this.operation_map.get(this.userCmd).equals("lsio")) {
 				this.lsio();
 			} else if (this.operation_map.get(this.userCmd).equals("view")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				this.view();
 			} else if (this.operation_map.get(this.userCmd).equals("open")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				this.open();
 			} else if (this.operation_map.get(this.userCmd).equals("delete")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				this.delete();
 			}else if (this.operation_map.get(this.userCmd).equals("rename")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				String newValue = "";
				for (int i = 1; i < this.userParams.length; i++) {
 					newValue += this.userParams[i] + " ";
 				}
 				this.rename(newValue);
 			}
 			else if (this.operation_map.get(this.userCmd).equals("mod")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				String newValue = "";
 				for (int i = 2; i < this.userParams.length; i++) {
 					newValue += this.userParams[i] + " ";
 				}
 				this.modSelected(this.userParams[1], newValue.trim());
 			} else if (this.operation_map.get(this.userCmd).equals("lock")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				this.lockSelected();
 			} else if (this.operation_map.get(this.userCmd).equals("unlock")) {
 				this.setSelectedFile(Integer.parseInt(this.userParams[0]));
 				this.unlockSelected();
 			} else {
 				this.unknownCmd();
 			}
 		} catch (final NullPointerException e) {
 			this.unknownCmd();
 			 e.printStackTrace();
 		} catch (final MetaManException e) {
 			this.printError(e.getMessage());
 			 e.printStackTrace();
 		} catch (final UnsupportedOperationException e) {
 			this.printError(e.getMessage());
 			 e.printStackTrace();
 		}
 	}
 
 	private void rename(String newValue) {
 		if (this.controller.renameSelectedFile(newValue)) {
 			this.println("File was renamed.");
 		} else {
 			this.println("File was NOT renamed.  Try again later.");
 		}
 	}
 
 	/**
 	 * Ends the program
 	 */
 	private void exit() {
 		this.println("Goodbye");
 		System.exit(0);
 	}
 
 	/**
 	 * Parses the command entered by the user
 	 */
 	private void extractCommandAndParamsFromInput() {
 
 		try {
 			this.userCmd = this.userInput.substring(0,
 					this.userInput.indexOf(' '));
 			this.userParams = this.userInput
 					.substring(this.userInput.indexOf(' ')).trim().split(" ");
 			this.userCmd = this.userCmd.toLowerCase().trim();
 		} catch (final StringIndexOutOfBoundsException e) {
 			// If in here, no parameters were found
 			this.userCmd = this.userInput;
 			this.userParams = null;
 			this.userCmd = this.userCmd.toLowerCase().trim();
 		}
 	}
 
 	/**
 	 * Prints out a help menu to the user
 	 */
 	private void help() {
 		this.println();
 		this.println("Support MetaMan Commands:");
 		this.println();
 		this.println("MOD {index} {tag} {newName}:");
 		this.println("     Modifies meta data info for audio files in the current directory");
 		this.println();
 		this.println("PWD:");
 		this.println("     Prints out the current working directory");
 		this.println();
 		this.println("LS:");
 		this.println("     Lists all the files MetaMan supports within the current directory");
 		this.println();
 		this.println("LSAO:");
 		this.println("     Lists all the audio files MetaMan supports within the current directory");
 		this.println();
 		this.println("LSIO:");
 		this.println("     Lists all the image files MetaMan supports within the current directory");
 		this.println();
 		this.println("LSDO:");
 		this.println("     Lists all the directorys within the current directory");
 		this.println();
 		this.println("LOCK {index}:");
 		this.println("     Lists all the directorys within the current directory");
 		this.println();
 		this.println("UNLOCK {index}:");
 		this.println("     Lists all the directorys within the current directory");
 		this.println();
 		this.println("VIEW {index}:");
 		this.println("     Lists all the directorys within the current directory");
 		this.println();
 		this.println("OPEN {index}:");
 		this.println("     opens the file at index");
 		this.println("DELETE {index}:");
 		this.println("     deletes the file at index");
 		this.println();
 		this.println("EXIT :");
 		this.println("     exits the program");
 		this.println();
 
 	}
 
 	/**
 	 * Informs the controller that the user wishes to lock the selected file
 	 */
 	private void lockSelected() {
 		if (this.controller.lockSelectedFile()) {
 			System.out.println("FILE WAS LOCKED");
 		} else {
 			System.out.println("FILE WAS NOT LOCKED");
 		}
 
 	}
 
 	/**
 	 * Asks the controller for a listing of files of the current directory and
 	 * prints them out in a table.
 	 * 
 	 * @throws MetaManException
 	 */
 	private void ls() throws MetaManException {
 		this.println();
 		this.println("     SUPORTED FILES/DIRS");
 		this.println("------------------------------");
 		this.println("#         NAME           TYPE");
 		this.println("------------------------------");
 
 		int count = 0;
 		try {
 			for (final File f : this.controller.listing()) {
 				if (f.isDirectory()) {
 					this.print(count + "");
 					final String count_s = count + "";
 					this.printSpace(10 - count_s.length());
 					String name = f.getName();
 					if (name.length() > 10) {
 						name = name.substring(0, 10) + "...";
 					}
 					this.print(name);
 					this.printSpace(15 - name.length());
 					this.print("DIR");
 					this.print("\n");
 				} else {
 					this.print(count + "");
 					final String count_s = count + "";
 					this.printSpace(10 - count_s.length());
 					String name = f.getName();
 					if (name.length() > 10) {
 						name = name.substring(0, 10) + "...";
 					}
 					this.print(name);
 					this.printSpace(15 - name.length());
 					this.print(f.getName()
 							.substring(f.getName().lastIndexOf('.'))
 							.toUpperCase().substring(1));
 					this.print("\n");
 				}
 				count++;
 			}
 		} catch (final StringIndexOutOfBoundsException e) {
 			// Do nothing
 		}
 
 		this.println();
 		this.println();
 	}
 
 	/**
 	 * Prints the list of all the supported audio files in the current working
 	 * directory
 	 * 
 	 * @throws MetaManException
 	 */
 	private void lsao() throws MetaManException {
 
 		this.println();
 		this.println("     SUPORTED AUDIO FILES/DIRS");
 		this.println("------------------------------");
 		this.println("#         TITLE          ARTIST");
 		this.println("------------------------------");
 
 		int count = 0;
 		ArrayList<MetaManFile> list;
 		final ArrayList<AudioFile> aolist = new ArrayList<AudioFile>();
 		list = (ArrayList<MetaManFile>) this.controller.listingAudioOnly();
 		for (int i = 0; i < list.size(); i++) {
 			aolist.add((AudioFile) list.get(i));
 		}
 
 		for (final AudioFile f : aolist) {
 			this.print(count + "");
 			final String count_s = count + "";
 			this.printSpace(10 - count_s.length());
 			String name = f.getMetaData("TITLE");
 			if (name.length() > 10) {
 				name = name.substring(0, 10) + "...";
 			}
 			this.print(name);
 			this.printSpace(15 - name.length());
 			this.print(f.getMetaData("ARTIST"));
 			this.print("\n");
 			count++;
 		}
 		this.println();
 
 	}
 
 	/**
 	 * Prints the list of all the directories in the current working directory
 	 */
 	private void lsdo() {
 
 		this.println();
 		this.println("     DIRECTORIES");
 		this.println("------------------------------");
 		this.println("#         NAME           ");
 		this.println("------------------------------");
 
 		int count = 0;
 		for (final File f : this.controller.listingDirectoiesOnly()) {
 			if (f.isDirectory()) {
 				this.print(count + "");
 				final String count_s = count + "";
 				this.printSpace(10 - count_s.length());
 				String name = f.getName();
 				if (name.length() > 10) {
 					name = name.substring(0, 10) + "...";
 				}
 				this.print(name);
 				this.printSpace(15 - name.length());
 				this.print("\n");
 			}
 			count++;
 		}
 		this.println();
 
 	}
 
 	private void lsio() throws MetaManException {
 		this.println();
 		this.println("     SUPORTED IMAGE FILES/DIRS");
 		this.println("------------------------------");
 		this.println("#         NAME                ");
 		this.println("------------------------------");
 
 		int count = 0;
 		ArrayList<MetaManFile> list;
 		final ArrayList<ImageFile> iolist = new ArrayList<ImageFile>();
 		list = (ArrayList<MetaManFile>) this.controller.listingImagesOnly();
 		for (int i = 0; i < list.size(); i++) {
 			iolist.add((ImageFile) list.get(i));
 		}
 
 		for (final ImageFile f : iolist) {
 			this.print(count + "");
 			final String count_s = count + "";
 			this.printSpace(10 - count_s.length());
 			String name = f.getMetaData("NAME");
 			if (name.length() > 20) {
 				name = name.substring(0, 20) + "...";
 			}
 			this.print(name);
 			this.printSpace(15 - name.length());
 			// this.print(f.getMetaData("HEIGHT"));
 			this.print("\n");
 			count++;
 		}
 		this.println();
 
 	}
 
 	/**
 	 * Modifies the selected files meta data
 	 * 
 	 * @param key
 	 *            key to be set
 	 * @param newValue
 	 *            the value to set the key to
 	 * @throws MetaManException
 	 */
 	private void modSelected(String key, String newValue)
 			throws MetaManException {
 		if (this.controller.modMetaDataOfSelectedFile(key, newValue)) {
 			this.print("File was modified successfylly");
 			this.println();
 		} else {
 			throw new MetaManException(
 					"File could not be opened because it is locked from MetaMan");
 		}
 	}
 
 	/**
 	 * Opens the selected file
 	 * 
 	 * @throws MetaManException
 	 * 
 	 * @throws IOException
 	 */
 	private void open() throws MetaManException {
 		this.println("OPENING FILE ...");
 
 		if (!this.controller.openSelectedFile()) {
 			throw new MetaManException(
 					"File could not be opened because it is locked form MetaMan");
 		}
 
 	}
 
 	/**
 	 * Populates the operations and what they map to
 	 */
 	private void populateOperationMap() {
 		this.operation_map = new HashMap<String, String>();
 		this.operation_map.put("pwd", "pwd");
 		this.operation_map.put("exit", "exit");
 		this.operation_map.put("e", "exit");
 		this.operation_map.put("ls", "ls");
 		this.operation_map.put("cd ../", "up");
 		this.operation_map.put("up", "up");
 		this.operation_map.put("cd ..", "up");
 		this.operation_map.put("cd", "cd");
 		this.operation_map.put("help", "help");
 		this.operation_map.put("lsao", "lsao");
 		this.operation_map.put("lsdo", "lsdo");
 		this.operation_map.put("mod", "mod");
 		this.operation_map.put("view", "view");
 		this.operation_map.put("open", "open");
 		this.operation_map.put("lock", "lock");
 		this.operation_map.put("unlock", "unlock");
 		this.operation_map.put("quit", "exit");
 		this.operation_map.put("lsio", "lsio");
 		this.operation_map.put("delete", "delete");
 		this.operation_map.put("rename", "rename");
 	}
 
 	/**
 	 * Print a message with no new line
 	 * 
 	 * @param message
 	 *            message to be printed
 	 */
 	private void print(String message) {
 		System.out.print(message);
 	}
 
 	/**
 	 * Print character to the screen
 	 * 
 	 * @param count
 	 *            number of times to print the character
 	 * @param x
 	 *            character to print
 	 */
 	private void printChar(int count, char x) {
 		for (int i = 0; i < count; i++) {
 			this.print(x + "");
 		}
 	}
 
 	/**
 	 * Prints error messages
 	 * 
 	 * @param message
 	 *            message to be printed
 	 */
 	private void printError(String message) {
 		this.println();
 		this.println("ERROR: " + message);
 		this.println();
 	}
 
 	/**
 	 * Prints a blank line
 	 */
 	private void println() {
 		System.out.println();
 	}
 
 	/**
 	 * Print message with new line
 	 * 
 	 * @param message
 	 *            message to be printed
 	 */
 	private void println(String message) {
 		System.out.println(message);
 	}
 
 	/**
 	 * Print spaces on same line
 	 * 
 	 * @param count
 	 *            number of spaces to print
 	 */
 	private void printSpace(int count) {
 		this.printChar(count, ' ');
 	}
 
 	/**
 	 * Prints the working directory and new line
 	 */
 	private void pwd() {
 		this.println(this.controller.printWorkingDirectory());
 	}
 
 	/**
 	 * Sets the selected file
 	 * 
 	 * @param index
 	 *            index corresponding to the list that was last printed
 	 * @throws MetaManException
 	 */
 	private void setSelectedFile(int index) throws MetaManException {
 		this.controller.setSelectedFile(index);
 	}
 
 	/**
 	 * Main Entry point of the program Loops until exit command
 	 * 
 	 * @throws IOException
 	 */
 	public void startup() {
 
 		// Print a welcome message to the user.
 		this.println(this.welcomeMessage());
 
 		final Scanner scanner = new Scanner(System.in);
 
 		// Loop always waiting for input until user types 'exit' or 'e'
 		while (true) {
 
 			// Print the directory MetaMan is currently looking in. NOTE: This
 			// will be the users home directory at the startup of the program.
 			this.print(this.controller.printWorkingDirectory() + "> ");
 
 			// Scan in command and params form user
 			this.userInput = scanner.nextLine();
 
 			// Extract the command and the params from the input
 			this.extractCommandAndParamsFromInput();
 
 			// Exe the command using the given commands
 			this.executeUserCommand();
 
 		}
 	}
 
 	/**
 	 * @see code
 	 */
 	private void unknownCmd() {
 		this.println("'"
 				+ this.userInput
 				+ "' is not recognized as a MetaMan command.  Please be sure the command exists and you have entered the correct paramaters.");
 		this.println();
 	}
 
 	/**
 	 * Unlocks a selected file
 	 */
 	private void unlockSelected() {
 		if (this.controller.unlockSelectedFile()) {
 			this.println("File was unlocked.");
 		} else {
 			this.println("File Was NOT Unlocked.  Try again later.");
 		}
 	}
 
 	/**
 	 * Move to the parent directory
 	 * 
 	 * @return true if move was successful
 	 */
 	private boolean up() {
 		return this.controller.goUpOneDirectory();
 	}
 
 	/**
 	 * Views the selected files meta data
 	 * 
 	 * @throws MetaManException
 	 */
 	private void view() throws MetaManException {
 		this.println(this.controller.viewMetaDataOfSelectedFile());
 	}
 
 	/**
 	 * Welcomes the user
 	 * 
 	 * @return welcome message
 	 */
 	private String welcomeMessage() {
 		return "MetaMan -v 1.0\n--------------\nWelcome To MetaMan!\nEnter 'help' if needed\n";
 	}
 
 }
