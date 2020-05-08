 package bisbat;
 
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.Stack;
 import java.util.Vector;
 
 public class Bisbat extends Thread {
 
 	static GregorianCalendar cal;
 	
 	public Connection connection;
 	public String name;
 	public String password;
 	private String prompt;
 	public Room currentRoom;
 	public Vector<Being> knownBeingList;
 	public Vector<Item> knownItemList;
 	public RoomFinder roomFindingThread;
 	public ResultQueue resultQueue;
 	public Stack<Pair<String,Object>> toDoList;
 	
 	/**
 	 * Constructs a new instance of Bisbat
 	 * @param loginName: Name of this instance of Bisbat
 	 * @param loginPassword: Password used by this instance of Bisbat to login to the game
 	 */
 	public Bisbat(String loginName, String loginPassword) {
 		name = loginName;
 		password = loginPassword;
 		prompt = "";
 		knownBeingList = new Vector<Being>();
 		knownItemList = new Vector<Item>();
 		roomFindingThread = new RoomFinder(this);
 		roomFindingThread.start();
 		resultQueue  = new ResultQueue();
	 	toDoList = new Stack<Pair<String,Object>>();
 	}
 
 	public void run() {
 		connection = new Connection(this, "www.mortalpowers.com", 4000);
 	 	login();

 	 	toDoList.push(new Pair<String,Object>("survive", null));
 	 	toDoList.push(new Pair<String,Object>("explore", null));
 	 	while(toDoList.size() > 0) {
 	 		Pair<String,Object> toDoItem = toDoList.pop();
 	 		if(toDoItem.left.equals("explore")) {
 	 			explore();
 	 		} else if(toDoItem.left.equals("consider")) {
 	 			if(toDoItem.right instanceof Being) {
 	 				Being b = ((Being)toDoItem.right);
 	 				connection.send("consider " + b.guessName());
 	 				debug("Considering a mobile");
 	 				String result = resultQueue.pop();
	 				
	 				// We don't want just any pop, we want a pop in response to our query.  Dumping all other input for now, later we will have to deal with these.
	 				while(!result.startsWith("You don't see") && !result.contains("looks much tougher than you") && !result.contains("looks about as tough as you") && !result.contains("You are much tougher")) {
	 					debug("Dumping: " + result + " because it didn't match anything we were looking for.");
	 					if(result.contains("much tougher than you.")) debug("Much tougher than you matched.");
	 					if(result.contains("tough as you.")) debug("Tough as you matched.");
	 					if(result.contains("You are much")) debug("You matched.");
	 					result = resultQueue.pop();
	 				}
	 					
 	 				if(!b.setGuessResult(result)) {
 	 					toDoList.push(new Pair<String,Object>("consider", b));
 	 				} else {
 	 					if(!b.isSureOfName()) {
 	 						toDoList.push(new Pair<String,Object>("consider", b));
 	 					}
 	 				}
 	 				
 	 			} else {
 	 				debug("We were given a non-being to consider, you fool!");
 	 			}
 	 		} else if(toDoItem.left.equals("survive")) {
 	 			print("Good job, we have done everything we can in this game.");
 	 			try{
 	 				Thread.sleep(30000);
 	 			} catch(Exception e) {
 	 				e.printStackTrace();
 	 			}
 	 			toDoList.push(new Pair<String,Object>("survive", null));
 	 		}
 	 	}
 	 	connection.send("quit");
 	}
 	
 	public void login() {
 		connection.send(name);
 		connection.send(password);
 		setUpPrompt();
 		resultQueue.pop();
 		roomFindingThread.add("look");
 		currentRoom = roomFindingThread.pop();
 	}
 	
 	public void explore() {	
 		debug("Starting exploration");
 		String otherExitDirection = "";
 		try {
 			//currentRoom.printTree(); // debugger
 			Exit chosenExit = currentRoom.getRandomUnexploredExit();
 			if(chosenExit == null) {
 				Room findMe = findRoomWithUnexploredExits();
 				if(findMe == null) {
 					print("We have finished mapping the known universe.");
 					return;
 				}
 				walkToRoomIfKnown(findMe);
 				chosenExit = currentRoom.getRandomUnexploredExit();
 			}
 			toDoList.push(new Pair<String,Object>("explore",null));
 			connection.follow(chosenExit);
 			otherExitDirection = Exit.getOpposite(chosenExit.getDirection());
 			chosenExit.nextRoom = roomFindingThread.pop();
 			Room previousRoom = currentRoom;
 			currentRoom = chosenExit.nextRoom;
 			//System.out.println("Other direction is: " + otherExitDirection + " and it should exist in the most recently found room."); // debugger
 			currentRoom.getExit(otherExitDirection).nextRoom = previousRoom;
 
 			
 		} catch(NullPointerException e) {
 			System.err.println("Null Pointer\nCurrent Room Title: " + currentRoom.title + " otherExitDirection:" + otherExitDirection);
 			e.printStackTrace();
 		} catch (Exception e) {
 			System.err.println("Error in random walk"); 
 			e.printStackTrace();
 		} 
 		//debug("Done exploration");
 	}
 	
 	public Room findRoomWithUnexploredExits() {
 		LinkedList<Room> exploredRooms = new LinkedList<Room>();
 		LinkedList<Room> searchQueue = new LinkedList<Room>();
 		searchQueue.add(currentRoom);
 		
 		while(searchQueue.size() > 0) {
 			Room currentRoom = searchQueue.removeFirst();
 			if(!exploredRooms.contains(currentRoom)) {
 				exploredRooms.add(currentRoom);
 				if(currentRoom.getUnexploredExits().size() > 0) {
 					return currentRoom;
 				} else {
 					for(Exit e : currentRoom.exits) {
 						if(e.nextRoom != null) {
 							searchQueue.add(e.nextRoom);
 						}
 					}
 				}
 			}	
 		}
 		return null; // no unexplored exits found in knowledge base
 	}
 	
 	public void setUpPrompt() {
 		prompt = "<prompt>%c";
 		connection.send("prompt " + prompt);
 		
 	}
 	public String getPrompt() {
 		return prompt;
 	}
 	public String getPromptMatch() {
 		String s = getPrompt().replaceAll("%c", ".?.?");
 		s = s.replaceAll("%.", "\\\\d+");
 		return s;
 	}
 
 	public void foundRoom(Room recentlyDiscoveredRoom) {
 		roomFindingThread.add(recentlyDiscoveredRoom);
 	}
 	
 	/**
 	 * This method will be used to walk to a room that has unexplored exits, or
 	 * whenever we want to walk to a destination room.
 	 * @param walkMeToHere
 	 */
 	public void walkToRoomIfKnown(Room walkMeToHere) {
 		//System.out.println("We are walkToRoomIfKnown ing."); // debugger
 		//System.out.println("Current KB:"); // debugger
 		//currentRoom.printTree(); // debugger
 		ArrayList<Exit> path = null;
 		try {
 			//System.out.println("I don't see a room '" + s + "'!"); // debugger
 			path = RoomFinder.searchForPathBetweenRooms(currentRoom, walkMeToHere);
 		} catch(OutOfMemoryError e) {
 			System.out.println("We had a wee bit of trouble searching for the path between two rooms. OUT OF MEMORY!");
 		}
 		//System.out.println("Following a path from " + currentRoom.title + " to  " + walkMeToHere.title); // debugger
 		if(path == null) {
 			throw new NullPointerException("Couldn't find a path to desired room.");
 		}
 		for(Exit e : path) {
 			connection.follow(e);
 			roomFindingThread.pop(false);
 			currentRoom = e.nextRoom;
 			//currentRoom.printTree(); // debugger
 		}
 		//System.out.println("Done following the path."); // debugger
 	}
 	
 	public void addKnowledgeOf(Item i) {
 		if(!knownItemList.contains(i)) {
 			knownItemList.add(i);
 		}
 	}
 	
 	public void addKnowledgeOf(Being b) {
		if(b == null) {
			debug("You are trying to add knowledge of anull, this is a problem.");
		}
 		if(!knownBeingList.contains(b)) {
 			knownBeingList.add(b);
 			toDoList.push(new Pair<String,Object>("consider", b));
 		}
 	}
 
 	/**
 	 * Prints the time with given string
 	 * @param string: prints this string along with the current time
 	 */
     static public void print(String string) {
     	cal = new GregorianCalendar();
         System.out.println("(" + cal.getTime().toString() + ") "  + string);
     }
     
     static public void debug(String string) {
     	System.out.println(string);
     }
 	
 	/**
 	 * Creates and runs default instance of Bisbat
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Bisbat alpha = new Bisbat("Bisbat", "alpha");
 		alpha.start(); 
 	}
 	
 
 
 }
