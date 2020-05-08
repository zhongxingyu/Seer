 
 /**
  * This class creates the pawns used for the game.
  * By default, a pawn is set on Start square with a track & safety index of -1.
  * A pawn keeps track of its location on the board and its own id.
  */
 
 public class SRPawn {
 
 	public boolean onStart;
 	public boolean onHome;
 	public int trackIndex;
 	public int safetyIndex;
 	public int player;
 	public int id;
 	
 	//chain the constructors
 	public SRPawn(int player){
 		this(true, false, player);
 	}
 	
 	public SRPawn(boolean onStart, boolean onHome, int player) {
 		this(onStart, onHome, player, -1, -1);
 	}
 	
 	public SRPawn(boolean onStart, boolean onHome, int player, int trackIndex, int safetyIndex) {
 		this.onStart = onStart;
 		this.onHome = onHome;
 		this.player = player;
 		this.trackIndex = trackIndex;
 		this.safetyIndex = safetyIndex;
 		this.player = player;
 	}
 	
 	//getters
 	public boolean isOnStart() {
 		return onStart;
 	}
 	
 	public boolean isOnHome() {
 		return onHome;
 	}
 	
 	//checks if pawn is in safety zone first, then checks the track
 	public int getPosition() {				//This will need to be changed if trackIndex and safetyIndex share numbers (otherwise displaying something like '3' will be confusing)
 		if (safetyIndex != -1) {
 			return safetyIndex;
 		}
 		else {
 			return trackIndex;
 		}
 	}
 	
	public int getTrackIndex(){
		return this.trackIndex;
	}
	
 	public int getPlayer() {
 		return player;
 	}
 	
 	public int getID() {
 		return id;
 	}
 	
 	//checks if a particular square is occupied by another pawn
 	public boolean ownedBy(int secondPosition) {
 		if (secondPosition == trackIndex) {
 			return true;						
 		}
 		else if (secondPosition == safetyIndex) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	//setters
 	public void setOnStart(boolean onStart) {
 		this.onStart = onStart;
 		this.onHome = false;
 		this.trackIndex = -1;
 	}
 	
 	public void setOnHome(boolean onHome) {
 		this.onHome = onHome;
 		this.onStart = false;
 		this.trackIndex = -1;
 	}
 
 	public void setTrackIndex(int index) {
 		this.trackIndex = index;
 		this.onHome = false;
 		this.onStart = false;
 	}
 
 	//moves player back to Start square
 	public void bump() {
 		this.setOnStart(true);
 	}
 
 	//sets the id of the pawn
 	public void setID(int i) {
 		this.id = i;
 	}
 	
 	//***********TEST FUNCTION**********
 	public static void main(String[] args){
 	
 		//constants
 		int player = 1;
 		int secondPosition = 7;
 		int id = 3;
 		
 		//Default Pawn Test
 		//creates pawn using default constructor
 		SRPawn defaultPawn = new SRPawn(player);
 		
 		System.out.print("-Default Pawn-\nonStart: " + defaultPawn.isOnStart() + "\nonHome: " + defaultPawn.isOnHome() + "\nposition: " + 
 							  defaultPawn.getPosition() + "\nplayer: " + defaultPawn.getPlayer() + "\nownedBy: " + defaultPawn.ownedBy(secondPosition));
 		
 		defaultPawn.setOnHome(true);
 		System.out.print("\nonHome (after setOnHome): " + defaultPawn.isOnHome());
 		
 		defaultPawn.setTrackIndex(secondPosition);
 		System.out.print("\nownedBy (after setting position to 2nd pawn's spot): " + defaultPawn.ownedBy(secondPosition)); 
 		
 		defaultPawn.bump();
 		System.out.print("\nisonStart (after bump): " + defaultPawn.isOnStart());
 		
 		defaultPawn.setID(id);
 		System.out.print("\npawn ID: " + defaultPawn.getID());
 		
 		//2nd Pawn Test
 		boolean onStart = false;
 		boolean onHome = true;
 		
 		//creates pawn using constructor with 3 parameters
 		SRPawn pawn2 = new SRPawn(onStart, onHome, player);
 		
 		System.out.print("\n\n-Second Pawn-\nonStart: " + pawn2.isOnStart() + "\nonHome: " + pawn2.isOnHome() + "\nposition: " + 
 							  pawn2.getPosition() + "\nplayer: " + pawn2.getPlayer());
 							  
 		//3rd Pawn Test
 		int trackIndex = 42;
 		int safetyIndex = -1;
 		
 		//creates pawn using constructor with 5 parameters
 		SRPawn pawn3 = new SRPawn(onStart, onHome, player, trackIndex, safetyIndex);
 		
 		System.out.print("\n\n-Third Pawn-\nonStart: " + pawn3.isOnStart() + "\nonHome: " + pawn3.isOnHome() + "\nposition: " + 
 							  pawn3.getPosition() + "\nplayer: " + pawn3.getPlayer());
 		
 		System.exit(0);
 	}
 }
