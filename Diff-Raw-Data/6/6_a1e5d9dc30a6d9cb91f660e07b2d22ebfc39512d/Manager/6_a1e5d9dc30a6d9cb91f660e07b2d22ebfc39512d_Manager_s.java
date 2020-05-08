 import java.util.Properties;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.Serializable;
 import java.util.logging.Logger;
 
 public class Manager implements Serializable {
 	transient BarcodePrinter printer;
 	transient ElectronicLock entryLock;
 	transient ElectronicLock exitLock;
 	transient PinCodeTerminal terminal;
 	transient Properties prop;
 
 	HashMap<String, User> users;
 	HashMap<String, User> usersPin;
 	HashMap<String, Bicycle> bikes;
 	int idCounter;
 	transient String pinInput;
 	transient Bicycle pendingBicycle;
 
 	/** Initializes a new instance of Manager
 	 *
 	 * @return            BicycleManager instance
 	 */
 	public Manager() {
 		users     = new HashMap<String, User>();
 		usersPin  = new HashMap<String, User>();
 		bikes     = new HashMap<String, Bicycle>();
		pinInput  = "";
 		idCounter = 0;
 	}
 
 	/** Sets the java properties file
 	 *
 	 * @param prop        Properties object holding the configurable variables
 	 */
 	public void setProperties(Properties prop) {
 		this.prop = prop;
 	}
 
 	/** Adds a new user to the system
 	 *
 	 * @param firstname   The firstname of the user
 	 * @param lastname    The lastname of the user
 	 * @param id          Unique id of the user
 	 * @param pro         The pro status of the user
 	 */
 	public User addUser(String firstname, String lastname, String id, boolean pro) {
 		if (users.get(id) == null) {
 			User u = new User(firstname + " " + lastname, id, pro, generatePincode());
 			users.put(id, u);
 			usersPin.put(u.getPin(), u);
 			return u;
 		}
 
 		return null;
 	}
 
 	String generatePincode() {
 		String pin;
 
 		do {
 			pin = Integer.toString((int)(1000 + (int)(Math.random() * ((9999 - 1000) + 1))));
 		} while (usersPin.containsKey(pin));
 
 		return pin;
 	}
 
 	/** Remove a user from the system
 	 *
 	 * @param id     Unique id of the user
 	 */
 	public boolean removeUser(String id) {
 		User u = users.get(id);
 
 		if (u != null) {
 			if (u.getBicycles().size() > 0) {
 				return false;
 			}
 
 			users.remove(id);
 			usersPin.remove(u.getPin());
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/** Returns a list of all the users
 	 *
 	 * @return       ArrayList of all active users
 	 */
 	public ArrayList<User> getUsers() {
 		return new ArrayList<User>(this.users.values());
 	}
 
 	/** Returns the user associated with the bicycle
 	 *
 	 * @return       ArrayList of all active users
 	 */
 	public User getBicycleOwner(String id) {
 		Bicycle bike = bikes.get(id);
 
 		if (bike != null) {
 			return bike.getUser();
 		}
 
 		return null;
 	}
 
 	/** Upgrades a user to pro status
 	 *
 	 * @param id     Id of the user
 	 */
 	public void upgradeUser(String id) {
 		User u = users.get(id);
 		u.upgradeUser();
 	}
 
 	/** Downgrade a user to normal status
 	 *
 	 * @param id     Id of the user
 	 */
 	public void downgradeUser(String id) {
 		User u = users.get(id);
 		u.downgradeUser();
 	}
 
 	/** Add a bicycle to a user
 	 *
 	 * @param id       Id of the user
 	 * @returns        True if bike successfully was added
 	 */
 	public boolean addBicycle(String id) {
 		User u = users.get(id);
 
 		if (u != null) {
 			Bicycle bike = new Bicycle(u, Integer.toString(++idCounter));
 
 			u.addBicycle(bike);
 			bikes.put(bike.getId(), bike);
 			printer.printBarcode(bike.getId());
 			return true;
 		}
 
 		return false;
 	}
 
 	/** Remove a bicycle from a user
 	 *
 	 * @param id       Id of the user
 	 * @return         Returns true if the bike was successfully removed
 	 */
 	public boolean removeBicycle(String id) {
 		Bicycle bike = bikes.get(id);
 
 		if (bike != null) {
 			User u = bike.getUser();
 
 			u.removeBicycle(bike);
 			bikes.remove(id);
 			return true;
 		}
 
 		return false;
 	}
 
 	/** Counts the number of bicycles in the garage
 	 *
 	 * @return         Int representing the bicycle count
 	 */
 	public int numberOfBicyclesInGarage() {
 		int count = 0;
 
 		for (User user : getUsers()) {
 			count += user.getBicyclesInGarage().size();
 		}
 
 		return count;
 	}
 
 	public void entryBarcode(String id) {
 		if (numberOfBicyclesInGarage() >= Integer.parseInt(prop.getProperty("garage_full", "3"))) {
 			terminal.lightLED(PinCodeTerminal.RED_LED, 1);
 			pendingBicycle = null;
 			log("Garage full");
 			return;
 		}
 
 		Bicycle bike = bikes.get(id);
 		if (bike != null && !bike.isInGarage()) {
 			log(bike.toString() + " scanned at the entrance.");
 			pendingBicycle = bike;
 		} else {
 			log("Unknown bike scanned at the entrance.");
 			terminal.lightLED(PinCodeTerminal.RED_LED, 1);
 		}
 	}
 
 	public void exitBarcode(String id) {
 		Bicycle bike = bikes.get(id);
 		if (bike != null) {
 			log(bike.toString() + " scanned at exit.");
 			bike.setGarageStatus(false);
 		} else {
 			log("Unknown bike scanned at exit.");
 		}
 		exitLock.open(10);
 	}
 
 	void openForBicyle(Bicycle bike) {
 		log(bike.getUser().getName() + " blev insläppt i garaget.");
 		pendingBicycle.setGarageStatus(true);
 		terminal.lightLED(PinCodeTerminal.GREEN_LED, 10);
 		entryLock.open(10);
 		pendingBicycle = null;
 	}
 
 	public void entryCharacter(char c) {
 		pinInput += c;
 		if (pinInput.length() > 3) {
 			if (pendingBicycle != null && pendingBicycle.getUser().getPin().equals(pinInput)) {
 				if (pendingBicycle.getUser().getStatus()) {
 					openForBicyle(pendingBicycle);
 				} else {
 					if (numberOfBicyclesInGarage() >= Integer.parseInt(prop.getProperty("garage_pro_only", "2"))) {
 						terminal.lightLED(PinCodeTerminal.RED_LED, 1);
 						log("Garage has no room for non pro users");
 					} else {
 						openForBicyle(pendingBicycle);
 					}
 				}
 			} else if (usersPin.containsKey(pinInput)) {
 				if (usersPin.get(pinInput).hasBicycleInGarage()) {
 					log(usersPin.get(pinInput).getName() + " blev insläppt i garaget utan cykel.");
 					terminal.lightLED(PinCodeTerminal.GREEN_LED, 10);
 					entryLock.open(10);
 				} else {
 					log(usersPin.get(pinInput).getName() + " försökte komma in utan att ha en cykel i garaget.");
 					terminal.lightLED(PinCodeTerminal.RED_LED, 1);
 				}
 			} else {
 				log("Okänd pinkod.");
 				terminal.lightLED(PinCodeTerminal.RED_LED, 1);
 			}
 			pinInput = "";
 		}
 	}
 
 	public void registerHardwareDrivers(
 			BarcodePrinter printer,
 			ElectronicLock entryLock,
 			ElectronicLock exitLock,
 			PinCodeTerminal terminal) {
 		this.printer   = printer;
 		this.entryLock = entryLock;
 		this.exitLock  = exitLock;
 		this.terminal  = terminal;
 	}
 
 	void log(String out) {
 		Logger.getLogger("Garage").info(out);
 	}
 }
