 package maze_objects;
 
 import java.util.ArrayList;
 
 public class Eagle extends Movable implements java.io.Serializable {
 
 	private static final long serialVersionUID = -1321096383108241473L;

 	/*** Private Attributes ***/
 
 	private int place;         //Tells if eagle is on top of a wall or a dragon
 	private boolean hasSword;  //Tells if eagle has the sword
 
 	private boolean onRouteToSword;   //Tells if eagle is going after a sword;
 	private boolean onRouteToHero;    //Tells if eagle is going back to hero;
 
 	private boolean withHero;  //Tells if eagle is with hero
 
 	private boolean onGroundWithSword;  //Tells if eagle is on top of sword place
 	private boolean waitingForHero;     //Tells if eagle is waiting for hero, on ground
 
 	private int onGroundCounter; //Counts the number of turns the eagle is grounded
 
 	//Position of the sword the eagle is going after
 	private int swordRow;
 	private int swordColumn;
 	private Sword sword;
 
 	//Position of the eagle when it took off
 	private int startRow;
 	private int startColumn;
 
 	//Number of turns to be grounded on top of sword
 	private static final int ON_GROUND_TURNS = 1;
 
 	/*** Public Attributes ***/
 
 	//Eagle States
 	public static final int DEAD = 0;
 	public static final int ALIVE = 1;
 
 	ArrayList<Cell> path;
 	private int position;
 
 
 	/*** Private Methods ***/
 
 	//Uses Bresenham's Line Algorithm (modified to meet our needs)
 	//http://en.wikipedia.org/wiki/Bresenham's_line_algorithm
 	private ArrayList<Cell> getPath(){
 		ArrayList<Cell> wayToSword = new ArrayList<Cell>();
 
 		int dx = Math.abs(swordColumn - startColumn);
 		int dy = Math.abs(swordRow - startRow);
 
 		int sx = (startColumn < swordColumn) ? 1 : -1;
 		int sy = (startRow < swordRow) ? 1 : -1;
 
 		int err = dx - dy;
 
 		while (true){
 			Cell currentCell = new Cell(row, column);
 			wayToSword.add(currentCell);
 			if (row == swordRow && column == swordColumn)
 				break;
 
 			int e2 = 2 * err;
 
 			if (e2 > -dy) {
 				err -= dy;
 				column += sx;
 			}
 
 			if (e2 < dx) {
 				err += dx;
 				row += sy;
 			}
 		}
 
 		return wayToSword;
 	}
 
 	private void makeMoveAtSword() {
 		if ( (position + 1) < path.size() ){
 			position++;
 			Cell next = path.get(position);
 			row = next.i;
 			column = next.j;
 		}
 	}
 
 	private void makeMoveAtStart() {
 		if ( (position - 1) >= 0 ){
 			position--;
 			Cell next = path.get(position);
 			row = next.i;
 			column = next.j;
 		}
 	}
 
 	/*** Public Methods ***/
 
 	//Constructors
 	public Eagle(int r, int c, boolean isWithHero) { //Creates an eagle on the given spot, with or without hero
 		row = r;
 		column = c;
 		withHero = isWithHero;
 		state = ALIVE;
 		hasSword = false;
 		onRouteToSword = false;
 		onRouteToHero = false;
 		onGroundWithSword = false;
 		waitingForHero = false;
 	}
 
 	//General Methods
 	public int getPlace() {
 		return place;
 	}
 
 	public void setPlace(int p) {
 		place = p;
 	}
 
 	public boolean eagleHasSword() {
 		return hasSword;
 	}
 
 	public void catchSword() {
 		hasSword = true;
 	}
 
 	public void dropSword() {
 		hasSword = false;
 	}
 
 	public boolean isOnRouteToSword() {
 		return onRouteToSword;
 	}
 
 	public void setOnRouteToSword() {
 		onRouteToSword = true;
 	}
 
 	public void removeOnRouteToSword() {
 		onRouteToSword = false;
 	}
 
 	public boolean isWaitingForHero() {
 		return waitingForHero;
 	}
 
 	public boolean isOnGroundWithSword() {
 		return onGroundWithSword;
 	}
 
 	public boolean isOnRouteToHero() {
 		return onRouteToHero;
 	}
 
 	public void setOnRouteToHero() {
 		onRouteToHero = true;
 	}
 
 	public void removeOnRouteToHero() {
 		onRouteToHero = false;
 	}
 
 	public void setSwordRow(int r) {
 		swordRow = r;
 	}
 
 	public void setSwordColumn(int c) {
 		swordColumn = c;
 	}
 
 	public boolean isWithHero(){
 		return withHero;
 	}
 
 	public void setWithHero(boolean isWithHero){
 		withHero = isWithHero;
 	}
 
 	//Game Methods
 	public void takeOff(int r, int c, Sword s) {
 		startRow = r;
 		startColumn = c;
 
 		sword = s;
 		swordRow = s.getRow();
 		swordColumn = s.getColumn();
 
 		withHero = false;
 		onRouteToSword = true;
 
 		path = getPath();
 	}
 
 	public void returnToHero() {
 		withHero = true;
 
 		onRouteToHero = false;
 		waitingForHero = false;
 		onGroundWithSword = false;
 		onRouteToSword = false;
 		hasSword = false;
 	}
 
 	public void killEagle() {
 		state = DEAD;

 		withHero = false;
 		onRouteToHero = false;
 		waitingForHero = false;
 		onGroundWithSword = false;
 		onRouteToSword = false;
 		hasSword = false;
 
 		sword.dropSword(row, column);
 	}
 
 	public void moveWithHero(int r, int c) {
 		row = r;
 		column = c;
 	}
 
 	public void moveEagle() {		
 		if(onGroundCounter < ON_GROUND_TURNS && onGroundWithSword)
 			onGroundCounter++;
 		else if(onGroundWithSword) {
 			onGroundWithSword = false;
 			onRouteToSword = false;
 			onRouteToHero = true;

 			hasSword = true;
 			sword.takeSword();
 		}
 
 		if(!onGroundWithSword || !waitingForHero || !withHero) {
 			if(onRouteToSword && !hasSword)
 				makeMoveAtSword();
 			else if(onRouteToHero)
 				makeMoveAtStart();
 
 			if(row == swordRow && column == swordColumn && onGroundCounter == 0) { // arrived at sword
 				onGroundWithSword = true;
 				onGroundCounter = 0;
 			}
 
 			if(row == startRow && column == startColumn && hasSword) { // arrived back at hero's location
 				sword.dropSword(row, column);
 				waitingForHero = true;
 			}
 		}
 	}
 
 }
