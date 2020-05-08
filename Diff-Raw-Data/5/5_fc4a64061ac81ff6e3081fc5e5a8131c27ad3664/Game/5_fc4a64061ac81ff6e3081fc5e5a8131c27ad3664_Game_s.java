 /**
  * 
  * @author	Tom Penney
 * @version	0.2.1
  * 
  * Corlanthia is unfinished and the proof-of-concept is still being developed.
  * This game is intended to be a 'complete' game with diverse mechanics and a gripping story,
  * isometric maps of each room may be created as a 'featured' update at some point for new
  * players to text adventure games and will be an optional feature.
  * 
  * More information about the game and the source can be found on github at https://github.com/2kan/Corlanthia
  * 
  *
  */
 import java.util.*;
 
 
 public class Game {
 	
 	//private static Scanner GameScan	= new Scanner(System.in);
 	//private static String GameInput	= "";
 	public static String name		= "Corlanthia";
	public static String version	= "0.2.1";
 	public static String lastCmd	= "";
 	private static boolean inMainMenu	= false;
 	
 	public static void main(String[] args) {
 		GUI.init();
 		//Menus.MainMenu();
 		
 		// Sleep to let the GUI build
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {}
 		intro();
 	}
 	
 	/**
 	 * Show the initial description of the scenario, then call <code>start</code>.
 	 */
 	public static void intro() {
 		GUI.log("You wake up in a daze, there is a lump on your head and you don't know how it got there. " +
 				"The last thing you can remember was when you were out drinking with your mates and some unexpected " +
 				"visitors arrived.\n\n" +
 				"The room you are in seems like it was from the medeval times and all you can smell are decaying rats.");
 		start();
 	}
 	
 	/**
 	 * Set the room to the starting room and show the room's info by calling <code>showRoomInfo</code>.
 	 */
 	public static void start() {
 		Rooms.currentRoom		= 1;
 		Rooms.ChangeRoom("", -1, true);
 		showRoomInfo(1);
 	}
 	
 	/**
 	 * Shows the specified room's information (name, description)
 	 * @param inRoom	room to have info shown
 	 */
 	public static void showRoomInfo(int inRoom) {
 		GUI.log("\n--- "+Rooms.currentRoomName+" --- \n\n" +
 				Rooms.RoomDescription);
 		Rooms.visitedRooms[Rooms.currentRoom]	= 1;
 	}
 	
 	/**
 	 * Interprets the command, <code>GameInput</code> and calls the corresponding method.
 	 * 
 	 * If the command is not recognized, a message saying that couldn't be interpreted is shown.
 	 * @param GameInput	the command to be interpreted
 	 */
 	public static void input(String GameInput) {
 		GUI.log("\n> "+GameInput);
 		boolean interpreted	= false;
 		while(!interpreted) {
 			if(inMainMenu) {
 				if(GameInput.equals("1") || GameInput.equals("1.") || GameInput.equalsIgnoreCase("Play")) {
 					inMainMenu	= false;
 				} else if(GameInput.equals("2") || GameInput.equals("2.") || GameInput.equalsIgnoreCase("About")) {
 					Menus.About();
 				} else if(GameInput.equals("3") || GameInput.equals("3.") || GameInput.equalsIgnoreCase("Exit")) {
 					System.exit(0);
 				}
 				interpreted	= true;
 			} else {
 				if(GameInput.equals(null) || GameInput.equals("")) {
 					showRoomInfo(Rooms.currentRoom);
 					interpreted	= true;
 				} else if(GameInput.equalsIgnoreCase("help")) {
 					Menus.Help();
 					interpreted	= true;
 				} else if(GameInput.equalsIgnoreCase("exit") || GameInput.equalsIgnoreCase("menu")) {
 					Menus.MainMenu();
 					inMainMenu	= true;
 					interpreted	= true;
 				} else if(GameInput.equalsIgnoreCase("north") || GameInput.equalsIgnoreCase("east") ||
 						GameInput.equalsIgnoreCase("south") || GameInput.equalsIgnoreCase("west")) {
 					Rooms.RoomChange	= false;
 					int nextRoom	= Rooms.getNextRoomId(GameInput, Rooms.currentRoom);
 					System.out.println("Current room:" + Rooms.currentRoom + ", next room direction:" + GameInput + ", next room:" + nextRoom);
 					if(!Rooms.isLocked(nextRoom)) {
 						Rooms.ChangeRoom(GameInput, Rooms.currentRoom, false);
 						Rooms.RoomChange	= true;
 					} else {
 						System.out.println("nextRoom:"+nextRoom+" keyIdRequired:"+Rooms.keyIdRequired(nextRoom));
 						if(Actions.inventoryContains(Rooms.keyIdRequired(nextRoom))) {
 							Rooms.unlockDoor(nextRoom);
 							GUI.log("Door unlocked with " + Actions.GetItemName(Rooms.keyIdRequired(nextRoom)));
 							Rooms.ChangeRoom(GameInput, Rooms.currentRoom, false);
 							Rooms.RoomChange	= true;
 						} else {
 							GUI.log("This door appears to be locked.");
 						}
 					}
 					if(Rooms.RoomChange == true) {
 						showRoomInfo(Rooms.currentRoom);
 					}
 					interpreted	= true;
 				} else if(GameInput.equalsIgnoreCase("intro")) {
 					showRoomInfo(Rooms.currentRoom);
 					interpreted	= true;
 				} else if(GameInput.equalsIgnoreCase("version")) {
 					GUI.log(version);
 					interpreted	= true;
 				}
 				
 				
 				StringTokenizer commandTokens	= new StringTokenizer(GameInput, " ", false);
 				String command					= commandTokens.nextToken();
 				int commandCount				= commandTokens.countTokens();
 				
 				if(command.equals("inspect")) {
 					if(commandCount == 0) {
 						GUI.log("Type 'inspect' followed by an item to inspect.");
 					} else {
 						Actions.Inspect(commandTokens.nextToken(), Rooms.currentRoom);
 					}
 					interpreted	= true;
 				} else if(command.equals("inventory") || command.equals("invsee")) {
 					Actions.InvSee();
 					GUI.updateInventory(Actions.Inventory);
 					interpreted	= true;
 				} else if(command.equals("pickup")) {
 					if(commandCount == 1) {
 						Actions.Pickup(commandTokens.nextToken());
 						GUI.updateInventory(Actions.Inventory);
 					} else {
 						GUI.log("Type 'pickup' followed by an item you would like to pickup.");
 					}
 					interpreted	= true;
 				} else if(command.equals("lookat") || command.equals("look") || command.equals("search")) {
 					Actions.Look();
 					interpreted	= true;
 				} else if(command.equals("drop")) {
 					Actions.Drop(commandTokens.nextToken());
 					GUI.updateInventory(Actions.Inventory);
 					interpreted	= true;
 				} else if(command.equals("debug")) {
 					if(commandCount == 1) {
 						Debug.Actions(commandTokens.nextToken(), null);
 					}
 					if(commandCount == 2) {
 						Debug.Actions(commandTokens.nextToken(), commandTokens.nextToken());
 					}
 					interpreted	= true;
 				} else if(command.equals("read")) {
 					if(commandTokens.hasMoreTokens()) {
 						String title	= "";
 						while(commandTokens.hasMoreTokens()) {
 							title	+= commandTokens.nextToken();
 						}
 						Actions.Read(title);
 					}
 					interpreted	= true;
 				} else if(command.equals("herp")) {
 					GUI.log("derp");
 					interpreted	= true;
 				} else if(command.equals("derp")) {
 					GUI.log("herp");
 					interpreted	= true;
 				} else if(command.equalsIgnoreCase("dance") && Rooms.currentRoom != 9) {
 					GUI.log("That wont do any good here.");
 					interpreted	= true;
 				} else if(command.equalsIgnoreCase("dance") && Rooms.currentRoom == 9) {
 					GUI.log("You start to dance with the lobsters and they open and close their claws as if they are " +
 							"maracas in their excitement.");
 					interpreted	= true;
 				}
 				
 				if(!interpreted) {
 					GUI.log("What did you say?");
 					interpreted	= true;
 				}
 			}
 		}
 		lastCmd	= GameInput;
 		//input();
 	}
 }
