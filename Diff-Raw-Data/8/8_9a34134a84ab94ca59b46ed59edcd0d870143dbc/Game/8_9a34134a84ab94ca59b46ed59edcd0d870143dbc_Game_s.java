 package model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 
 import model.command.Command;
 import model.command.CommandStack;
 import model.object.Item;
 import model.object.Monster;
 import model.object.Player;
 import save.GameSave;
 import view.FirstPersonItem;
 import view.FirstPersonMonster;
 import view.FirstPersonRoom;
 import xml.XMLReader;
 import controller.FPMouseListener;
 
 /**
  * This class is the main class of the "World of Zuul" application.
  * "World of Zuul" is a very simple, text based adventure game. Users can walk
  * around some scenery. That's all. It should really be extended to make it more
  * interesting!
  * 
  * To play this game, create an instance of this class and call the "play"
  * method.
  * 
  * This main class creates and initializes all the others: it creates all rooms,
  * creates the parser and starts the game. It also evaluates and executes the
  * commands that the parser returns.
  * 
  */
 
 public class Game extends Observable implements Observer {
 	private static final String GAME_OVER = "GAME OVER";
 	private boolean gameOver;
 	private static final int STARTING_HEALTH = 20;
 	private final static int MAX_WEIGHT = 10;
 
 	private static final String LEFT = "left";
 	private static final String RIGHT = "right";
 
 	private static final String SOUTH = "south";
 	private static final String EAST = "east";
 	private static final String WEST = "west";
 	private static final String NORTH = "north";
 
 	private static final String DEFAULT_LEVEL = "DefaultMap";
 	private static final String DIRECTORY = "src/xml/maps/";
 	private static final String XML = ".xml";
 
 	private Player player1;
 	private HashMap<String, FirstPersonRoom> rooms;
 	private CommandStack redoStack;
 	private CommandStack undoStack;
 	private FPMouseListener mouseListener;
 	private String startingRoom;
 
 	/**
 	 * Create the game and initialize its internal map.
 	 */
 	public Game(FPMouseListener mouseListener) {
 		this.mouseListener = mouseListener;
 
 		rooms = new HashMap<String, FirstPersonRoom>();
 
 		undoStack = new CommandStack();
 		redoStack = new CommandStack();
 
 		gameOver = false;
 	}
 
 	public void addRoom(String name) {
 		rooms.put(name, new FirstPersonRoom(name, mouseListener));
 	}
 
 	public void addExitToRoom(String room, String exitRoom, String direction) {
 		if (rooms.containsKey(room))
 			rooms.get(room).setExits(direction, rooms.get(exitRoom));
 	}
 
 	public void addItemToRoom(String room, String name, int weight,
 			String image, String wall) {
 		if (rooms.containsKey(room))
 			rooms.get(room).addItem(new FirstPersonItem(name, weight, image),
 					wall);
 	}
 
 	public void addMonsterToRoom(String room, String name, int health,
 			String image, String wall) {
 		if (rooms.containsKey(room)) {
 			Monster m = new FirstPersonMonster(name, health, image);
 			rooms.get(room).addMonster(m, wall);
 		}
 	}
 
 	public void setStart(String s) {
 		startingRoom = s;
 	}
 
 	/**
 	 * Create all the rooms and link their exits together.
 	 */
 	public void loadDefaultGame() {
 		loadGameFromLevel(DEFAULT_LEVEL);
 	}
 
 	public void loadGameFromLevel(String levelName) {
 		rooms = new HashMap<String, FirstPersonRoom>();
 
 		@SuppressWarnings("unused")
 		XMLReader xmlr = new XMLReader(DIRECTORY + levelName + XML, this);
 
 		player1 = new Player(MAX_WEIGHT, STARTING_HEALTH);
 
 		rooms.get(startingRoom).visit();
 		player1.setCurrentRoom(rooms.get(startingRoom)); // start game outside
 
 		// Refresh the View
 		setChanged();
 		notifyObservers(player1);
 	}
 
 	/**
 	 * Start playing a game with the given player and set of rooms
 	 * 
 	 * @param player
 	 * @param rooms
 	 */
 	public void loadGame(Player player, HashMap<String, FirstPersonRoom> rooms) {
 		this.player1 = player;
 		this.rooms = rooms;
 
 		ArrayList<FirstPersonRoom> temp = new ArrayList<FirstPersonRoom>(
 				rooms.values());
 		for (int i = 0; i < temp.size(); i++) {
 			temp.get(i).setMouseListener(mouseListener);
 		}
 
 		player1.getCurrentPlayerRoom().visit();
 
 		// Refresh the View
 		setChanged();
 		notifyObservers(player1);
 	}
 
 	/**
 	 * Given a command, process (that is: execute) the command.
 	 * 
 	 * @param command
 	 *            The command to be processed.
 	 * @return
 	 * @return true If the command ends the game, false otherwise.
 	 * @throws CloneNotSupportedException
 	 */
 	public void processCommand(Command command, boolean addToStack) {
 
 		if (command == null || command.getCommandWord() == null) {
 			setChanged();
 			notifyObservers("Invlaid command. I don't know what you mean...");
 			// System.out.println("I don't know what you mean...");
 			return;
 		}
 
 		if (addToStack) {
 			undoStack.add(command);
 		}
 
 		String commandWord = command.getCommandWord();
 		if (commandWord.equals("help")) {
 			printHelp();
 		} else if (commandWord.equals("go")) {
 			goRoom(command);
 		} else if (commandWord.equals("quit")) {
 			gameOver = true;
 		} else if (commandWord.equals("undo")) {
 			undo();
 		} else if (commandWord.equals("redo")) {
 			redo();
 		} else if (commandWord.equals("pick")) {
 			pick(command);
 		} else if (commandWord.equals("drop")) {
 			drop(command);
 		} 
 		else if(commandWord.equals("eat")){
         	eat();
         }
         else if(commandWord.equals("unEat")){
         	unEat();
         }
 		else if (commandWord.equals("attack")) {
 			attack(command);
 			ArrayList<Monster> m = player1.getCurrentPlayerRoom().getMonsters();
 			for (Monster monster : m) {
 				monster.attack(player1);
 			}
 		} else if (commandWord.equals("heal")) {
 			ArrayList<Monster> m = player1.getCurrentPlayerRoom().getMonsters();
 			for (Monster monster : m) {
 				monster.heal(player1);
 			}
 			heal(command);
 		} else if (commandWord.equals("turn")) {
 			turn(command);
 		} else if (commandWord.equals("straight")) {
 			Command temp = new Command("go", player1.getLookingDirection());
 			undoStack.add(temp);
 			goRoom(temp);
 		} else if (commandWord.equals("new")) {
 			loadDefaultGame();
 		} else if (commandWord.equals("load")) {
 			GameSave save = new GameSave(null, null);
 			boolean success = save.loadFromSerial(command.getSecondWord());
 			if (success) {
 				loadGame(save.getPlayer(), save.getRooms());
 			} else {
 				setChanged();
 				notifyObservers("Unable to load specified game. Please make sure the save exists and try again");
 			}
 		} else if (commandWord.equals("save")) {
 			GameSave save = new GameSave(player1, rooms);
 			boolean success = save.serialize(command.getSecondWord());
 			if (success) {
 				setChanged();
 				notifyObservers("Game Saved successfully");
 			} else {
 				setChanged();
 				notifyObservers("Unable to save game. Please try again");
 			}
 		}
 
 		// Notify observers (must notify AFTER monster attacks)
 		setChanged();
 		notifyObservers(player1);
 
 		// Check to see if the player is still alive, if not, quit
 		if (!gameOver) {
 			gameOver = player1.getHealth() <= 0;
 		}
 
 	}
 
 	public Player getPlayer() {
 		return player1;
 	}
 
 	private void turn(Command command) {
 		if (!command.hasSecondWord()) {
 			setChanged();
 			notifyObservers("Turn where?");
 			return;
 		}
 
 		String direction = command.getSecondWord();
 
 		if (direction.equals(LEFT)) {
 			if (player1.getLookingDirection().equals(NORTH)) {
 				player1.setLookingDirection(WEST);
 			} else if (player1.getLookingDirection().equals(SOUTH)) {
 				player1.setLookingDirection(EAST);
 			} else if (player1.getLookingDirection().equals(EAST)) {
 				player1.setLookingDirection(NORTH);
 			} else if (player1.getLookingDirection().equals(WEST)) {
 				player1.setLookingDirection(SOUTH);
 			}
 		} else if (direction.equals(RIGHT)) {
 			if (player1.getLookingDirection().equals(NORTH)) {
 				player1.setLookingDirection(EAST);
 			} else if (player1.getLookingDirection().equals(SOUTH)) {
 				player1.setLookingDirection(WEST);
 			} else if (player1.getLookingDirection().equals(EAST)) {
 				player1.setLookingDirection(SOUTH);
 			} else if (player1.getLookingDirection().equals(WEST)) {
 				player1.setLookingDirection(NORTH);
 			}
 		}
 	}
 
 	private void undo() {
 		Command temp = undoStack.pop();
 		if (temp != null) {
 			redoStack.add(temp);
 			processCommand(temp, false);
 		}
 	}
 
 	private void redo() {
 		Command temp = redoStack.pop();
 		if (temp != null) {
 			undoStack.add(temp);
 			processCommand(temp, false);
 		}
 	}
 
 	/**
 	 * Attack a monster that is in the room
 	 * 
 	 * @param command
 	 */
 	private void attack(Command command) {
 		if (!command.hasSecondWord()) {
 			// if there is no second word, we don't who to attack
 			setChanged();
 			notifyObservers("Attacj what?");
 			return;
 		}
 
 		Room currentRoom = player1.getCurrentPlayerRoom();
 		Monster monster = currentRoom.getMonster(command.getSecondWord());
 
 		if (monster == null) {
 			// There is no monster by that name in the room
 			setChanged();
 			notifyObservers("There is no monster called "
 					+ command.getSecondWord() + "!");
 			return;
 		}
 
 		// Decrease the monster's health
 		monster.decreaseHealth();
 
 		if (!monster.isAlive()) {
 			setChanged();
 			notifyObservers("Good job! You've killed "
 					+ command.getSecondWord());
 			
 			//check if all monsters have been killed
 			if(allMostersKilled()){
 				JOptionPane.showMessageDialog(null,
 						"CONGRATULATIONS -----YOU HAVE KILLED ALL THE MONSTERS !!  ", "alert",
 						JOptionPane.OK_OPTION);
 				gameOver = true;
 			}
 			
 			return;
 		}
 	}
 
 	private boolean allMostersKilled() {
 		
 			Set<String> keys = rooms.keySet();
 			for (String direction : keys){
 				ArrayList<Monster> m = new ArrayList<Monster>();
 				m = rooms.get(direction).getMonsters();
 				if ( !m.isEmpty()){
 					for(Monster mm : m){
 						if(mm.isAlive()){
 							return false;
 						}
 					}
 				}
 			}
 			return true;
 	}
 
 	/**
 	 * Un-attack monster
 	 * 
 	 * @param command
 	 */
 	private void heal(Command command) {
 		Room currentRoom = player1.getCurrentPlayerRoom();
 		Monster monster = currentRoom.getMonster(command.getSecondWord());
 
 		if (monster == null) {
 			// There is no monster by that name in the room
 			setChanged();
 			notifyObservers("There is no monster called "
 					+ command.getSecondWord() + "!");
 			return;
 		}
 		// monsters.get(player1.getLastMonsterAttacked()).increaseHealth();
 		monster.increaseHealth();
 		if(player1.hasSword())monster.increaseHealth();
 	}
 
 	private void drop(Command command) {
 		if (player1.getCurrentPlayerRoom()
 				.getWall(player1.getLookingDirection()).getItem() != null) {
 			setChanged();
 			notifyObservers("Cannot place item onto of another item.  Please drop somewhere else.");
 			return;
 		}
 
 		Item item = player1.drop(command.getSecondWord());
 		if (item != null) {
 			player1.getCurrentPlayerRoom().addItem(item,
 					player1.getLookingDirection());
 		} else {
 			// System.out.println("You cannot drop an item you're not carrying!");
 			setChanged();
 			notifyObservers("You cannot drop an item you're not carrying!");
 		}
 	}
 	private void eat(){
     	//Item item = player1.drop("Plant");
     	if( !player1.hasHealItem()){
     		return;
     	}
     	player1.eat();
     	setChanged();
     	notifyObservers();
     }
 
     private void unEat(){
     	
     	player1.unEat();
     	setChanged();
     	notifyObservers();
     }
 
 	/**
 	 * Print out some help information. Here we print some stupid, cryptic
 	 * messagego and a list of the command words.
 	 */
 	private void printHelp() {
 		setChanged();
 		notifyObservers("Welcome to Zuul! An incredibly boring adventure game. Use the left and right arrow keys to\n"
 				+ "look around the room. Click on a door to go through it, click on items to pick them up, and click on Monsters to\n"
 				+ "attack them. Enjoy!");
 	}
 
 	private void pick(Command command) {
 
 		if (!command.hasSecondWord()) {
 			setChanged();
 			notifyObservers("Pick what?");
 			return;
 		}
 
 		String itemName = command.getSecondWord();
 		Item item = player1.getCurrentPlayerRoom().getItem(itemName);
 
 		// Try to pick up the item.
 		if (player1.getCurrentPlayerRoom().containsItem(itemName)
 				&& player1.pick(itemName, item)) {
 			player1.getCurrentPlayerRoom().removeItem(itemName);
 		} else {
 			setChanged();
 			notifyObservers("Item could not be picked up. Make sure you have enough room in your inventory");
 		}
 	}
 
 	/**
 	 * Try to go in one direction. If there is an exit, enter the new room,
 	 * otherwise print an error message.
 	 */
 	private void goRoom(Command command) {
 		if (!command.hasSecondWord()) {
 			// if there is no second word, we don't know where to go...
 			setChanged();
 			notifyObservers("Go where?");
 			return;
 		}
 
 		String direction = command.getSecondWord();
 
 		if (direction.equals("straight")) {
 			direction = player1.getLookingDirection();
 		}
 
 		Room nextRoom = player1.getCurrentPlayerRoom().getExit(direction);
 
 		if (nextRoom == null) {
 			setChanged();
 			notifyObservers("There is no door!");
 		} else if (player1.getCurrentPlayerRoom().getWall(direction)
 				.getMonster() != null
 				&& player1.getCurrentPlayerRoom().getWall(direction)
 						.getMonster().isAlive()
 				&& !player1.hasPogoStick()) {
 			setChanged();
 			notifyObservers("Cannot go through that door! There is a monster in the way");
 		} else {
 			// Try to leave current room.
 			player1.setCurrentRoom(nextRoom);
 			nextRoom.visit();
 		}
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 instanceof Command) {
 			Command command = (Command) arg1;
 			processCommand(command, true);
 
 			if (gameOver) {
 				// Notify observers that the game is over
 				setChanged();
 				notifyObservers(GAME_OVER);
 			}
 
 		}
 	}
 }
