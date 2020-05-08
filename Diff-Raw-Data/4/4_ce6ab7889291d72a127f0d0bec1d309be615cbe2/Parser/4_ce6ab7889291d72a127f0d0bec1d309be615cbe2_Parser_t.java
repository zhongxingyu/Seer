 import java.util.*;
 
 public class Parser {
 	String command = null;
 	/* this is used to break apart commands involving item names. this is
 	 * so "pick up sword" and "pick up great sword" are interpreted differently.
 	 * (I mostly use contains() instead of equals()).
 	 */
 	String subCommand = null;
 	Location currentLocation = new Location();
 	Location school = new Location("school", "you're in a school [long description]", false);
 	Location cafe = new Location("cafe", "I love coffee", false);
 	// contains every location in an array for the purpose of resetting the game quickly.
 	Location[] locationList = {school, cafe};
	Exit schoolExit = new Exit("east", cafe, true);
	Exit cafeExit = new Exit("wWest", school, false);
 	Item item = new Item("keyboard", "a keyboard is on the ground");
 	Item attackItem = new Item("sword", "a sword on the ground", 100);
 	Item key = new Item("key", "a shiny key", true);
 	Item attackItem2 = new Item("magic sword", "a magical pony sword is on the ground", 232);
 	Enemy enemy = new Enemy("Witch", "a scary witch is here", 3, 5);
 	boolean validCommand = false;
 	boolean validDirection = false;
 	boolean verbosity = false;
 	boolean locationChanged = true;
 	boolean enemyIgnored = false;
 	Scanner s = new Scanner(System.in);
 	// several arrays of strings, indicating the accepted verbs for each type of command.
 	String[] attackCommands = {"attack", "kill", "hit"};
 	String[] itemPickUpCommands = {"pick up", "take", "get"};
 	String[] takeAllCommands = {"take all", "take everything"};
 	String[] dropAllCommands = {"drop all", "drop everything", "remove all", "remove everything"};
 	String[] itemDropCommands = {"drop", "remove", "dispose", "throw away"};
 	String[] itemUseCommands = {"use"};
 	String[] terminationCommands = {"quit", "exit", "leave"};
 	// vector containing the items/enemies for the player's current location.
 	Vector<Item> currentItems;
 	Vector<Enemy> currentEnemies;
 	Player player = new Player(10);
 	int currentItemDamage = 0;
 	
 	public Parser() {
 		setUpGame(school);
 	}
 	
 	/* sets the current command. this is used to store user input into a string
 	* variable so the parser can determine what operation to perform.
 	*/
 	public void setCommand(String command) {
 		this.command = command;
 	}
 	
 	// sets the current location. the current enemies/items are then set.
 	public void setCurrentLocation(Location newLocation) {
 		currentLocation = newLocation;
 		currentEnemies = currentLocation.getEnemies();
 		currentItems = currentLocation.getItems();
 	}
 	
 	public Location getCurrentLocation() {
 		return currentLocation;
 	}
 	
 	/* resets the game world. removes every item from the player's inventory,
 	 * removes every item from every location, sets every location as 
 	 * undiscovered, tells the game that the location has changed (even if one
 	 * dies at the beginning so the location description is always printed), 
 	 * removes every exit from every location (this may seem pointless but once I
 	 * re-add everything, there will be multiple of the same exit for no reason)
 	 * and sets the player back to full health. afterwards, it calls
 	 * setUpGame.
 	 */
 	public void resetGame() {
 		while (!player.getInventory().isEmpty()) {
 			player.removeItemFromInventory(player.getInventory().get(0));
 		}
 		
 		for (int i = 0; i < locationList.length; i++) {
 			while (!locationList[i].getItems().isEmpty()) {
 				locationList[i].removeItem(locationList[i].getItems().get(0));
 			}
 			
 			while (!locationList[i].getEnemies().isEmpty()) {
 				locationList[i].removeEnemy(locationList[i].getEnemies().get(0));
 			}
 			
 			while (!locationList[i].getExits().isEmpty()) {
 				locationList[i].removeExit(locationList[i].getExits().get(0));
 			}
 			locationList[i].setEntered(false);
 		}
 		locationChanged = true;
 		player.setPlayerHP(10);
 		setUpGame(school);
 	}
 	
 	/* sets up the game. adds the relevant items, enemies, and exits to each
 	 * location. the argument location specifies the starting location for the
 	 * game. afterwards, the locations/enemies of the current location are stored
 	 * in their respective vectors for easy access.
 	 */
 	public void setUpGame(Location location) {
 		player.dead = false;
 //		school.addEnemy(enemy);
 		school.addItem(item);
 		school.addItem(key);
 		school.addItem(attackItem);
 		school.addItem(attackItem2);
 		school.addExit(schoolExit);
 		cafe.addExit(cafeExit);
 		currentLocation = location;
 		currentEnemies = currentLocation.getEnemies();
 		currentItems = currentLocation.getItems();
 	}
 	
 	// handles every type of accepted input based on the value of command.
 	public void handleCommand() {
 		validCommand = false;
 		validDirection = false;
 		locationChanged = false;
 		enemyIgnored = false;
 		
 		for (int i = 0; i < currentLocation.getExits().size(); i++) {	
 			if (command.contains(currentLocation.getExits().get(i).getDirection())
 				|| command.equals(currentLocation.getExits().get(i).getShortDirection())) {
 				if (!currentLocation.getEnemies().isEmpty()) {
 					System.out.println("The enemy prevents you from fleeing.");
 					enemyIgnored = true;
 					validCommand = true;
 					validDirection = true;
 					break;
 				}
 				if (currentLocation.getExits().get(i).isBlocked()) {
 					System.out.println("The exit is blocked.");
 					validCommand = true;
 					validDirection = true;
 					break;
 				}
 				currentLocation.setEntered(true);
 				setCurrentLocation(currentLocation.getExits().get(i).getExitLocation());
 				locationChanged = true;
 				validCommand = true;
 				validDirection = true;
 			}
 		}
 		
 		
 		if (command.equals("inventory") || command.equals("i")) {
 			player.listInventory();
 			validCommand = true;
 			/* the direction "in" is contained in "inventory"
 			 * since I'm mostly using contains(), it is easier to set 
 			 * validDirection to true even though it isn't a direction command */
 			validDirection = true;
 		}
 		
 		if (command.equalsIgnoreCase("restart")) {
 			validCommand = true;
 			System.out.println("Would you like to restart? [y/n]");
 			String option = s.next().toLowerCase();
 			if (option.contains("y")) {
 				resetGame();
 			} else {
 				System.out.println("Keep it that way.");
 			}
 		}
 		
 		for (int i = 0; i < terminationCommands.length; i++) {
 			if (command.contains(terminationCommands[i])) {
 				System.out.println("Too much for you? [y/n]");
 				setCommand(s.next().toLowerCase());
 				if (command.contains("y")) {
 					System.out.println("Weak.");
 					System.exit(0);
 				} else {
 					System.out.println("I like your attitude");
 				}
 				validCommand = true;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < attackCommands.length; i++) {
 			if (command.contains(attackCommands[i])) {
 				attemptAttack();
 				break;
 			}
 		}
 		
 		for (int i = 0; i < itemPickUpCommands.length; i++) {
 			if (command.contains(itemPickUpCommands[i])) {
 				int index = itemPickUpCommands[i].length() + 1;
 				subCommand = command.substring(index);
 				attemptItemPickUp();
 				enemyIgnored = true;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < itemDropCommands.length; i++) {
 			if (command.contains(itemDropCommands[i])) {
 				int index = itemDropCommands[i].length() + 1;
 				subCommand = command.substring(index);
 				attemptItemDrop();
 				enemyIgnored = true;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < takeAllCommands.length; i++) {
 			if (command.equals(takeAllCommands[i])) {
 				attemptTakeEverything();
 				enemyIgnored = true;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < dropAllCommands.length; i++) {
 			if (command.equals(dropAllCommands[i])) {
 				attemptDropEverything();
 				enemyIgnored = true;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < itemUseCommands.length; i++) {
 			if (command.contains(itemUseCommands[i])) {
 				attemptItemUse();
 				validCommand = true;
 				break;
 			}
 		}
 		
 		if (command.equals("verbose") || command.equals("verbosity")) {
 			if (verbosity == true) {
 				System.out.println("Maximum verbosity has already been reached.");
 			} else {
 				System.out.println("Henceforth everything will be verbose.");
 				verbosity = true;
 			}
 			validCommand = true;
 		}
 		
 		if (command.equals("consise") || command.equals("brief")) {
 			if (verbosity == false) {
 				System.out.println("Everything is already abridged.");
 			} else {
 				System.out.println("Henforth eveything will be abridged.");
 				verbosity = false;
 			}
 			validCommand = true;
 		}
 		
 		for (int i = 0; i < Exit.acceptedDirections.length; i++) {
 			if (!validDirection) {
 				if (command.contains(Exit.acceptedDirections[i]) ||
 					command.equals(Exit.acceptedShortDirections[i])) {
 					System.out.println("You can't go that way.");
 					validCommand = true;
 					break;
 				}
 			}
 		}
 		
 		if (key.used()) {
 			schoolExit.setBlocked(false);
 			player.removeItemFromInventory(key);
 		}
 		
 		if (!validCommand) {
 			System.out.println("Invalid Command");
 			enemyIgnored = false;
 		}
 		
 		if (!currentLocation.getEnemies().isEmpty() && enemyIgnored) {
 			for (int i = 0; i < currentLocation.getEnemies().size(); i++) {
 				player.takeDamage(currentLocation.getEnemies().get(i).getEnemyDamge());
 			}
 		}
 	}
 
 	public void attemptItemUse() {
 		for (int i = 0; i < player.getInventory().size(); i++) {
 			if (command.contains(player.getInventory().get(i).getItemName()
 					.toLowerCase())) {
 				if (player.getInventory().get(i).canUse()) {
 					player.getInventory().get(i).setUsed(true);
 					System.out.println("Used.");
 					break;
 				}
 			}
 		}
 	}
 
 	public void attemptAttack() {
 		if (currentEnemies.isEmpty()) {
 			System.out.println("You flail around hitting the air with your fists.");
 		}
 		
 		for (int i = 0; i < player.getInventory().size(); i++) {
 			if (command.contains(player.getInventory().get(i).getItemName().
 					toLowerCase()) && player.getInventory().get(i).isWeapon()) {
 				currentItemDamage = player.getInventory().get(i).getItemDamage();
 				break;
 			}
 		}
 
 		for (int i = 0; i < currentEnemies.size(); i++) {
 			if (command.contains(currentEnemies.get(i).getEnemyName().toLowerCase())) {
 				currentEnemies.get(i).takeDamage(currentItemDamage);
 				validCommand = true;
 				if (currentEnemies.get(i).dead) {
 					currentLocation.removeEnemy(currentEnemies.get(i));
 				} else {
 					player.takeDamage(currentEnemies.get(i).getEnemyDamge());
 				}
 			}
 		}
 	}
 	
 	/* checks the player's input against the name of every items in the current
 	 * location converted to lowercase for comparison. if a match is found,
 	 * the item is removed from the current location and added to the player's
 	 * inventory.
 	 */
 	public void attemptItemPickUp() {
 		for (int i = 0; i < currentItems.size(); i++) {
 			if (subCommand.equals(currentItems.get(i).getItemName().toLowerCase())) {
 				player.addItemToInventory(currentItems.get(i));
 				currentLocation.removeItem(currentItems.get(i));
 				System.out.println("Taken.");
 				validCommand = true;
 			}
 		}
 	}
 	
 	/* removes every item from the current location. all of the items are then
 	 * added to the player's inventory.
 	 */
 	public void attemptTakeEverything() {
 		if (currentItems.isEmpty()) {
 			System.out.println("Nothing to take.");
 			validCommand = true;
 		} else {
 		while (!currentItems.isEmpty()) {
 			player.addItemToInventory(currentItems.get(0));
 			currentLocation.removeItem(currentItems.get(0));
 		}
 		System.out.println("Everything taken.");
 		validCommand = true;
 		validDirection = true;
 		}
 	}
 
 	/* checks the player's input against the name of every items in his/her
 	 * inventory converted to lowercase for comparison. if a match is found,
 	 * the item is removed from the player's inventory and added to the current
 	 * location.
 	 */
 	public void attemptItemDrop() {
 		for (int i = 0; i < player.getInventory().size(); i++) {
 			if (subCommand.equals(player.getInventory().get(i).getItemName().toLowerCase())) {
 				currentLocation.addItem(player.getInventory().get(i));
 				player.removeItemFromInventory(player.getInventory().get(i));
 				System.out.println("Dropped.");
 				validCommand = true;
 			}
 		}
 	}
 	
 	/* removes everything from the player's inventory. all of the items are then
 	 * added to the current location.
 	 */
 	public void attemptDropEverything() {
 		if (player.getInventory().isEmpty()) {
 			System.out.println("Nothing to drop.");
 			validCommand = true;
 		} else {
 		while (!player.getInventory().isEmpty()) {
 			currentLocation.addItem(player.getInventory().get(0));
 			player.removeItemFromInventory(player.getInventory().get(0));
 		}
 		System.out.println("Everything dropped.");
 		validCommand = true;
 		}
 	}
 }
