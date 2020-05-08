 /**
  * Player Class.
  * This class is in charge of the Player.
  * This class has a reference to the current room that the player is in.
  * As well, there is a playerHistory variable, in order to undo and redo certain moves.
  * This class implements a doCommand method which will take the input command words and correlate them to actual actions.
  *
  */
 
 
 public class Player extends Humanoid {
 
 	private PlayerHistory playerHistory;
 	private Room currentRoom;
 	
 	public Player(int health, Room r, String name){
 		super(health, name);
 		currentRoom = r;
 		playerHistory = new PlayerHistory();
 	}
 	
 	public Player(Room r){
 		super();
 		currentRoom = r;
 		playerHistory = new PlayerHistory();
 	}
 
 
 	public void doCommand(Command c){
 		boolean b = false;
 		
 		if (c.getCommandWord().equals(CommandWords.UNDO)){
 			c = playerHistory.undo();
 			b = true;
 		} else if (c.getCommandWord().equals(CommandWords.REDO)){
 			c = playerHistory.redo();
 			b = true;
 		}
 		if(c==null){
 			return; //TODO tell view about it
 		}
 		
 		
 		if (c.getCommandWord().equals(CommandWords.GO)){
 			Direction d = (Direction) c.getSecondWord();
 			Room r = currentRoom.getExit(d);
 			if(r!=null){
 				currentRoom = r;
 			} // else error TODO
 			if(b == false){
 				playerHistory.addStep(c);
 			}
 
 		} else if (c.getCommandWord().equals(CommandWords.FIGHT)){
 			Monster m = currentRoom.getMonster();
 			if(m==null){
 				System.out.println("Nothing to Fight!");
 				//should probably call the view here..shouldn't be a system.out in this class
 			} else {
 			//if(this.getBestItem().compareTo(m.getBestItem()) == 1){
 				m.updateHealth(this.getBestItem().getValue());
 				this.updateHealth(m.getBestItem().getValue());
 				if(m.getHealth()<=0){
 					currentRoom.removeMonster(m);
 				}
 			}
 			
 		} else if (c.getCommandWord().equals(CommandWords.HELP)){
 	        System.out.println("You are lost. You are alone. You wander around in a cave.\n");
 	        System.out.println("Your command words are:");
 	        System.out.println("GO, PICKUP, DROP, UNDO, REDO, FIGHT, HELP, QUIT");
 	        //HELP may be implemented in another class.
 	        
 		} else if (c.getCommandWord().equals(CommandWords.PICKUP)){
 			Item i = (Item) c.getSecondWord();
 			if(currentRoom.hasItem(i)){
 				addItem(i);
 				currentRoom.removeItem(i);
 			}
 			if(b == false){
 				playerHistory.addStep(c);
 			}
 		} else if (c.getCommandWord().equals(CommandWords.DROP)){
 			Item i = (Item) c.getSecondWord();
			if(currentRoom.hasItem(i)){
 				removeItem(i);
			}
 			currentRoom.addItem(i);
 			if(b == false){
 				playerHistory.addStep(c);
 			}
 		} else {
 			//TODO some sort of extraneous error checking
 		}//QUIT command does not get passed to the player
 		
 		
 	}
 	
 	public Room getCurrentRoom(){
 		return currentRoom;
 	}
 	
 }
