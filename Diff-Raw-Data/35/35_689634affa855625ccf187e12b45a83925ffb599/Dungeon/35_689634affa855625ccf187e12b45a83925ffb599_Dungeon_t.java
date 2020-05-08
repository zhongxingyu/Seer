 import java.io.Serializable;
 
 /**
  * This class represents the Dungeon as a 2D space whithin the player can move
  * Each dungeon is generated using a unique random seed, that can be used (and
  * stored) to recreate the exact same dungeon. Ndlr : the only random thing in
  * this class is the seed, each room, corridor, etc will be generated using only
  * the seed.
  * 
  * @author Jordan Becker
  * @author Robin Kebaili
  * @author Théo Gerriet
  * @version 0.1b
  * 
  */
 @SuppressWarnings("serial")
 public class Dungeon implements Serializable {
 
 	// Fields
 
 
 	/**
 	 * This two-dimensionnal array of boxes represents all the boxes of the
 	 * current state of the dungeon
 	 */
 	private Box[][] boxes;
 
 	/**
 	 * This field is the seed of the current dungeon. It is a randomly generated
 	 * integer that is used to make the dungeon and thats is then stored in this
 	 * field.
 	 */
 	private long seed;
 
 	/**
 	 * Numbers of characters in the width of the screen
 	 */
 	public final static int SIZE_WIDTH = 100;
 
 	/**
 	 * Numbers of characters in the height of the screen
 	 */
 	public final static int SIZE_HEIGHT = 30;
 
 	/**
 	 * Last x value used when a room was generated. Used in dungeon generation
 	 */
 	private int lastGenX;
 
 	/**
 	 * Last y value used when a room was generated. Used in dungeon generation
 	 */
 	private int lastGenY;
 
 	/**
 	 * Constant which represents the minimal distance between each room.
 	 */
 	private final static int MIN_ROOM_DISTANCE = 3;
 
 	/**
 	 * Array containings the rooms
 	 */
 	private Room[] rooms;
 
 	/**
 	 * Horizontal spawn position of the player
 	 */
 	private int spawnX;
 	
 	/**
 	 * Vertical spawn position of the player
 	 */
 	private int spawnY;
 	
 	// Constructor
 
 	/**
 	 * This constructor must fill up the array of boxes, by creating a random
 	 * dungeon composed of rooms, corridors, doors (monster, and also the
 	 * initial position of the adventurer maybe too) using the seed in parameter
 	 * 
 	 * @param seed
 	 *            integer, is used to create the dungeon. One seed=one dungeon!
 	 */
 	public Dungeon(long seed) {
 		this.seed = seed;
 		this.boxes = new Box[Dungeon.SIZE_WIDTH][Dungeon.SIZE_HEIGHT];
 		this.generateDungeon();
 	}
 
 	public Dungeon() {
 		this.boxes = new Box[Dungeon.SIZE_WIDTH][Dungeon.SIZE_HEIGHT];
 	}
 
 	// Methods
 
 	/**
 	 * Generates a random seed
 	 */
 	public static long generateSeed() {
 		return (long) (Math.random() * Long.MAX_VALUE);
 	}
 
 	/**
 	 * Create a room with its size and location
 	 * 
 	 * @param x
 	 *            Horizontal location of the room
 	 * @param y
 	 *            Vertical location of the room
 	 * @param width
 	 *            Width of the room
 	 * @param height
 	 *            Height of the room
 	 */
 	private boolean createRoom(int x, int y, int width, int height) {
 		boolean ok = true;
 
 		// Parameters test
 		if (x >= Dungeon.SIZE_WIDTH) {
 			x = x % Dungeon.SIZE_WIDTH;
 		}
 		if (y >= Dungeon.SIZE_HEIGHT) {
 			y = y % Dungeon.SIZE_HEIGHT;
 		}
 		if (x + width >= Dungeon.SIZE_WIDTH) {
 			ok = false;
 		}
 		if (y + height >= Dungeon.SIZE_HEIGHT) {
 			ok = false;
 		}
 
 		// Empty space test
 		int xTestBegin = x - Dungeon.MIN_ROOM_DISTANCE;
 		int yTestBegin = y - Dungeon.MIN_ROOM_DISTANCE;
 		int xTestEnd = x + width + Dungeon.MIN_ROOM_DISTANCE - 1;
 		int yTestEnd = y + height + Dungeon.MIN_ROOM_DISTANCE - 1;
 
 		if (xTestBegin < 0) {
 			xTestBegin = 0;
 		}
 		if (yTestBegin < 0) {
 			yTestBegin = 0;
 		}
 		if (xTestEnd > Dungeon.SIZE_WIDTH) {
 			xTestEnd = Dungeon.SIZE_WIDTH;
 		}
 		if (yTestEnd > Dungeon.SIZE_HEIGHT) {
 			yTestEnd = Dungeon.SIZE_HEIGHT;
 		}
 
 		if (ok) {
 			for (int i = xTestBegin; i < xTestEnd; i++) {
 				for (int j = yTestBegin; j < yTestEnd; j++) {
 					if (boxes[i][j] != null) {
 						ok = false;
 					}
 				}
 			}
 		}
 
 		// Room creation
 		if (ok) {
 			for (int i = x; i < width + x; i++) {
 				for (int j = y; j < height + y; j++) {
 					if (i == x || i == x + width - 1) {
 						boxes[i][j] = new Box(Box.VERTICAL_WALL);
 					} else {
 						if (j == y || j == y + height - 1) {
 							boxes[i][j] = new Box(Box.HORIZONTAL_WALL);
 						} else {
 							boxes[i][j] = new Box(Box.FLOOR);
 						}
 					}
 				}
 			}
 
 			boxes[x][y] = null;
 			boxes[x + width - 1][y] = null;
 			boxes[x][y + height - 1] = null;
 			boxes[x + width - 1][y + height - 1] = null;
 			this.lastGenX = x + width;
 			this.lastGenY = y + height;
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the seed of the current dungeon as an int
 	 */
 	public long getSeed() {
 		return this.seed;
 	}
 
 	/**
 	 * In the first part of the developement, the Dungeon (among other elements
 	 * of the gameplay) will be displayed by the toString() method
 	 */
 	//TODO Think about the visibilty state of boxes
 	public String toString(Adventurer a) {
 		int tmp = 0;
 		showDungeon(a);
 		char[] res = new char[(boxes.length * boxes[0].length)
 				+ Dungeon.SIZE_HEIGHT];
 		for (int j = 0; j < boxes[0].length; j++) {
 			for (int i = 0; i < boxes.length; i++) {
 				if (boxes[i][j] != null && boxes[i][j].isVisible()) {
 					res[tmp] = boxes[i][j].getRep();
 				} else {
 					res[tmp] = ' ';
 				}
 				tmp++;
 			}
 			res[tmp] = '\n';
 			tmp++;
 		}
 		return String.valueOf(res);
 	}
 	
 
 
 	/**
 	 * Generate all the dungeon depending of the seed
 	 */
 	private void generateDungeon() {
 		this.generateRooms();
 		this.generateSpawnAndStairs();
 		this.sortRoomByX();
 		this.generateCorridors();
 	}
 
 	/**
 	 * Generate rooms depending of the seed
 	 */
 	private void generateRooms() {
 		int roomsNumber = 0;
 		Room[] tmp = new Room[99];
 
 		// Rooms
 		int stepX, stepY, width, height, gStep = 0, errors = 0;
 		while (7 * gStep + errors <= 57) {
 			stepX = (int) ((this.seed >>> 57 - 7 * gStep + errors) & 0xf);
 			stepY = (int) ((this.seed >>> 58 - 7 * gStep + errors) & 0xf);
 			width = (int) ((this.seed >>> 59 - 7 * gStep + errors) & 0xf)%8;
 			height = (int) ((this.seed >>> 60 - 7 * gStep + errors) & 0xf)%6;
 			tmp[roomsNumber] = new Room(this.lastGenX + stepX, lastGenY	+ stepY, width + 8, height + 4,this);
 			if (this.createRoom(this.lastGenX + stepX, lastGenY + stepY,width + 8, height + 4)) {
 				gStep++;
 				
 				roomsNumber++;
 
 			} else {
 				errors++;
 			}
 		}
 
 		// Arrays of rooms
 		this.rooms = new Room[roomsNumber];
 		for (int i = 0; i < roomsNumber; i++) {
 			this.rooms[i] = tmp[i];
 		}
 	}
 
 	/**
 	 * Generate the corridors depending of the rooms array
 	 */
 	private void generateCorridors() {
 		for (int i=0;i<this.rooms.length-1;i++){
 			Room tmp=this.rooms[i+1];
 			this.rooms[i].linkRoom(tmp);
 		}
 	}
 
 	/**
 	 * Generate the spawn point and the stairs depending of the seed
 	 */
 	private void generateSpawnAndStairs() {
 		int i = 32;
 		int spawnRoom = (int) (Math.abs(this.seed%this.rooms.length));
 		int stairsRoom = (int) (Math.abs((this.seed >>> i)%this.rooms.length));
 		while (spawnRoom == stairsRoom){
 			i+=1;
 			stairsRoom = (int) ((this.seed >>> i)%this.rooms.length);
 		}
 		this.spawnX = rooms[spawnRoom].getCenterX();
 		this.spawnY = rooms[spawnRoom].getCenterY();
 		this.boxes[rooms[stairsRoom].getCenterX()][rooms[stairsRoom].getCenterY()] = new Box(Box.STAIRS);
 		
 	}
 
 	/**
 	 * Getter for the boxes array
 	 */
 	public Box[][] getBoxes() {
 		return this.boxes;
 	}
 
 	/**
 	 * Getter for the horizontal player's spawn
 	 * @return spawnX
 	 */
 	public int getSpawnX(){
 		return this.spawnX;
 	}
 	
 	/**
 	 * Getter for the vertical player's spawn
 	 * @return spawnY
 	 */
 	public int getSpawnY(){
 		return this.spawnY;
 	}
 	
 	public Room[] getRooms(){
 		return this.rooms;
 	}
 	
 	/**
 	 * Sort the room by their X position
 	 * @return Room[]
 	 */
 	 
 	public void sortRoomByX(){
 		Room tmp; 
 		for(int i = 0; i < rooms.length-1; i++) { 
 			for (int j = i; j < rooms.length; j++) { 
 				if (rooms[i].getCenterX() > rooms[j].getCenterX()) 
 				{ 
 				tmp = this.rooms[i]; 
 				this.rooms[i] = this.rooms[j]; 
 				this.rooms[j] = tmp; 
 				} 
 			} 
 		}
 		for (int i = 0; i<rooms.length;i++){
 			this.rooms[i].setNumber(i);
 		}
 	}
 	
 	public void showDungeon( Adventurer a){
 		
 		if(this.getBoxes()[a.getPosX()+1][a.getPosY()] != null)
 			this.getBoxes()[a.getPosX()+1][a.getPosY()].setVisible(true);
 		
 		if(this.getBoxes()[a.getPosX()][a.getPosY()+1] != null)
 			this.getBoxes()[a.getPosX()][a.getPosY()+1].setVisible(true);
 		
 		if(this.getBoxes()[a.getPosX()-1][a.getPosY()] != null)
 			this.getBoxes()[a.getPosX()-1][a.getPosY()].setVisible(true);
 
 		if(this.getBoxes()[a.getPosX()][a.getPosY()-1] != null)
 			this.getBoxes()[a.getPosX()][a.getPosY()-1].setVisible(true);
 		
 		for(int i=0; i<rooms.length;i++){
 			if(rooms[i].getWalked()){
 				for(int j=rooms[i].getPosX()+1; j<rooms[i].getPosX()+rooms[i].getWidth()-1;j++){
 					for(int k=rooms[i].getPosY()+1; k<rooms[i].getPosY()+rooms[i].getHeight()-1;k++){
 						if(this.getBoxes()[j][k] != null)
 							this.getBoxes()[j][k].setVisible(false);						
 					}
 				}
 			}
 			if(a.getPosX() < rooms[i].getPosX()+rooms[i].getWidth() && a.getPosX() >= rooms[i].getPosX() && a.getPosY() < rooms[i].getPosY()+rooms[i].getHeight() && a.getPosY() >= rooms[i].getPosY()){
 				rooms[i].setWalked(true);
 				for(int j=rooms[i].getPosX(); j<rooms[i].getPosX()+rooms[i].getWidth();j++){
 					for(int k=rooms[i].getPosY(); k<rooms[i].getPosY()+rooms[i].getHeight();k++){
 						if(this.getBoxes()[j][k] != null)
 							this.getBoxes()[j][k].setVisible(true);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Put all Entity visible & discover the map
 	 * 
 	 */
 	
 	public void setAllVisible(){
 		for(int i = 0; i< Dungeon.SIZE_WIDTH; i++){
 			for(int j = 0; j< Dungeon.SIZE_HEIGHT; j++){
 				if(this.getBoxes()[i][j] != null)
 					this.getBoxes()[i][j].setVisible(true);
 			}
 		}
 	}
 	
 	public static Dungeon dragoonRoom(){
 		Dungeon res = new Dungeon();
 		for (int j=0;j<Dungeon.SIZE_HEIGHT;j++){
 			for (int i=0;i<Dungeon.SIZE_WIDTH;i++){
 				res.boxes[i][j] = null;
 			}
 		}
 		
 		int x=5,y=0,width=Dungeon.SIZE_WIDTH-10,height=Dungeon.SIZE_HEIGHT-10;
 		for (int i = x; i < width + x; i++) {
 			for (int j = y; j < height + y; j++) {
 				if (i == x || i == x + width - 1) {
 					res.boxes[i][j] = new Box(Box.VERTICAL_WALL);
 				} else {
 					if (j == y || j == y + height - 1) {
 						res.boxes[i][j] = new Box(Box.HORIZONTAL_WALL);
 					} else {
 						res.boxes[i][j] = new Box(Box.FLOOR);
 					}
 				}
 			}
 		}
 		res.boxes[Dungeon.SIZE_WIDTH/2][4] =new Box(Box.STAIRS);
 		res.spawnX = Dungeon.SIZE_WIDTH /2;
 		res.spawnY = Dungeon.SIZE_HEIGHT-13;
 		
 		res.rooms = new Room[1];
 		res.rooms[0] = new Room(0,0,Dungeon.SIZE_WIDTH,Dungeon.SIZE_HEIGHT,res);
 		
 		return res;
 	}
 	
 	//Method added to testing code
 	public int getLastGenX(){
 		return this.lastGenX;
 	}
 	
 	public int getLastGenY(){
 		return this.lastGenY;
 	}
 	
 	@Override
 	public boolean equals(Object o){
 		if(!(o instanceof Dungeon)) return false;
 		Dungeon d = (Dungeon) o;
 		Box[][] b = d.getBoxes();
 	 	for(int r=0; r<Dungeon.SIZE_WIDTH; r++){
 			for(int c=0; c<Dungeon.SIZE_HEIGHT; c++){
				if(this.boxes[r][c].equals(b[r][c]))
 					return false;
 			}
 		}
	 	return this.seed == d.getSeed()||this.lastGenX == d.getLastGenX()
	 		   ||this.lastGenY == d.getLastGenY()||this.spawnX == d.getSpawnX()
	 		   ||this.spawnY == d.getSpawnY();
 	 	//Room
 	}
 	
 }
 
 
 
 
 
 class Room implements Serializable {
 
 	/**
 	 * Generated serial Version UID
 	 */
 	private static final long serialVersionUID = 8936091723018455484L;
 
 	// Fields
 	/**
 	 * Horizontal position of the room
 	 */
 	private int posX;
 	
 	/**
 	 * 
 	 */
 	
 	private boolean Walked = false;
 
 	/**
 	 * Vertical position of the room
 	 */
 	private int posY;
 
 	/**
 	 * Width of the room
 	 */
 	private int width;
 
 	/**
 	 * Height of the room
 	 */
 	private int height;
 
 	/**
 	 * 
 	 */
 	private boolean linked;
 	
 	/**
 	*  Number of the room
 	*/
 	private int number;
 	
 	/**
 	 * Dongeon which containing the room
 	 */
 	private Dungeon d;
 
 	
 	// Constructors
 	public Room(int posX, int posY, int width, int height,Dungeon d) {
 		if (posX >= Dungeon.SIZE_WIDTH) {
 			posX = posX % Dungeon.SIZE_WIDTH;
 		}
 		if (posY >= Dungeon.SIZE_HEIGHT) {
 			posY = posY % Dungeon.SIZE_HEIGHT;
 		}
 		this.posX = posX;
 		this.posY = posY;
 		this.width = width;
 		this.height = height;
 		this.linked = false;
 		this.d = d;
 	}
 
 	// Methods
 	/**
 	 * True if the room is linked to another
 	 * @return boolean
 	 */
 	public boolean isLinked(){
 		return this.linked;
 	}
 	
 	/**
 	 * Return this distance of the calling room to the room r
 	 * @return int
 	 */
 	 
 	public int getDistance(Room r){
 		return (int) Math.sqrt(Math.pow(r.getCenterX()-this.getCenterX(),2)+Math.pow(r.getCenterY()-this.getCenterY(),2));
 	}
 	
 	/**
 	 * Link the room calling the method to the room r
 	 */
 	 
 	public void linkRoom(Room r){
 		//Same X
 		if ((this.posX+width-1>r.posX && this.posX+width-1<r.posX+r.width+1) || (r.posX>this.posX && r.posX<this.posX+width-1)){
 			//If r is downer than this
 			if (this.posY < r.posY){
 				this.linkDown(r);
 			}
 			//If r is upper than this
 			else {
 				this.linkUp(r);
 			}
 		}
 		else {
 			//If the room have a common part on Y axis
 			if (this.posY==r.posY || (this.posY>=r.posY && this.posY<r.posY+r.height-1) || (r.posY>this.posY && r.posY<this.posY+this.height-1)) {
 					this.linkRight(r);
 			}
 			else {
 				//Need to link down
 				if (this.posY < r.posY){
 					//Need to link right
 					if (this.posX < r.posX){
 						this.linkDownRight(r);
 					}
 				}
 				//Need to link up
 				else {
 					//Need to link right
 					if (this.posX < r.posX){
 						this.linkUpRight(r);
 					}
 				}
 			}
 		}
 		this.linked = true;
 	}
 	
 	public void setNumber(int n){
 		this.number=n;
 	}
 	
 	/**
 	 * Create a corridor straight down with the room R
 	 */
 	
 	private void linkDown(Room r){
 		int corridorX = (((this.posX+this.width)-r.posX)/2)+r.posX;
 		for (int i=this.posY+this.height-2;i<r.posY+2;i++){
 			if (this.d.getBoxes()[corridorX][i]==null) this.d.getBoxes()[corridorX][i]=new Box(Box.CORRIDOR); 
 			else if (this.d.getBoxes()[corridorX][i].getRealRep()=='-' || this.d.getBoxes()[corridorX][i].getRealRep()=='|') this.d.getBoxes()[corridorX][i]=new Box(Box.DOOR);
 		}		
 	}
 	
 	/**
 	 * Create a corridor straight up with the room R
 	 */
 	 
 	private void linkUp(Room r){
 		int corridorX = (((this.posX+this.width)-r.posX)/2)+r.posX;
 		for (int i=r.posY+r.height-2;i<this.posY+2;i++){
 			if (this.d.getBoxes()[corridorX][i]==null) this.d.getBoxes()[corridorX][i]=new Box(Box.CORRIDOR); 
 			else if (this.d.getBoxes()[corridorX][i].getRealRep()=='-' || this.d.getBoxes()[corridorX][i].getRealRep()=='|') this.d.getBoxes()[corridorX][i]=new Box(Box.DOOR);
 		}		
 	}
 	
 	/**
 	 * Create a corridor straight to the right with the room R
 	 */	
 	 
 	private void linkRight(Room r){
 		int corridorY = 0;
 		int diffY;
 		
 		if (this.posY<r.posY){
 			diffY=(this.posY+this.height-r.posY)/2;
 			corridorY=r.posY+diffY;
 		}
 		else{
 			diffY=(r.posY+r.height-this.posY)/2;
 			corridorY=this.posY+diffY;
 		}
 		for (int i=this.posX+width-2;i<r.posX+2;i++){
 			if (this.d.getBoxes()[i][corridorY]==null) this.d.getBoxes()[i][corridorY]=new Box(Box.CORRIDOR); 
 			else if (this.d.getBoxes()[i][corridorY].getRealRep()=='-' || this.d.getBoxes()[i][corridorY].getRealRep()=='|') this.d.getBoxes()[i][corridorY]=new Box(Box.DOOR);
 		}
 	}
 		
 	/**
 	 * Create a corridor in a reversed L shape from the calling room to the room r
 	 */
 	 
 	private void linkUpRight(Room r){
 		Room previous=null;
 		if (this.number!=0){
 			//previous is the previous room that can be traversed by corridors
 			previous=this.d.getRooms()[this.number-1];
 		}
 		
 		//Vertical part
 		
 		for (int i=r.getCenterY();i<this.posY+3;i++){
 			if (this.d.getBoxes()[this.getCenterX()][i]==null){
 				this.d.getBoxes()[this.getCenterX()][i] = new Box(Box.CORRIDOR);
 			}
 			else{
 				if (this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='.' || this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='#' || this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='%'){
 				
 				}
 				else{
 					if(previous!=null){
 						if (i==previous.posY+1 || i==previous.posY+previous.height-2 || i==this.posY || i==previous.posY || i==previous.posY+previous.height-1 ||  i==r.posY+r.height-1){
 							if (this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='|' || this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='-')this.d.getBoxes()[this.getCenterX()][i]= new Box (Box.DOOR);
 							
 						}
 					}
 					else{
 						this.d.getBoxes()[this.getCenterX()][i] = new Box (Box.DOOR);
 					}
 				}
 			}
 		}
 		
 		//Horizontal part
 		for (int j=this.getCenterX();j<r.posX+3;j++){
 			if(this.d.getBoxes()[j][r.getCenterY()]==null){
 				this.d.getBoxes()[j][r.getCenterY()] = new Box (Box.CORRIDOR);
 			}
 			else{
 				if (this.d.getBoxes()[j][r.getCenterY()].getRealRep()=='.' ||this.d.getBoxes()[j][r.getCenterY()].getRealRep()=='#' ||  this.d.getBoxes()[j][r.getCenterY()].getRealRep()=='%'){
 				}
 				else{
 					if(previous!=null){
 						if (j==previous.posX+previous.width-2 || j==previous.posX+previous.width-1 || j==r.posX){
 							 this.d.getBoxes()[j][r.getCenterY()] = new Box (Box.DOOR);
 						}
 					}
 					else{
 						this.d.getBoxes()[j][r.getCenterY()] = new Box (Box.DOOR);
 					}
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * Create a corridor in a L shape from the calling room to the room r
 	 */
 	 
 	private void linkDownRight(Room r){
 		System.out.println("DownRight"+this.number);
 		Room previous=null;
 		if (this.number!=0){
 			//previous is the previous room that can be traversed by corridors
 			previous=this.d.getRooms()[this.number-1];
 		}
 		
 		//Vertical part
 		
 		for (int i=this.posY+this.height-2;i<r.getCenterY();i++){
 			if (this.d.getBoxes()[this.getCenterX()][i]==null){
 				this.d.getBoxes()[this.getCenterX()][i]= new Box (Box.CORRIDOR);
 			}
 			else{
 				if (this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='.' ||this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='#' ||this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='%' ){
 					
 				}
 				else{
 					if(previous!=null){
 						if ((i==previous.posY+1 || i==previous.posY+previous.height-2 || i==previous.posY ||i == previous.posY+previous.height-1 || i==previous.posY+1 || i==r.getCenterY() || i==this.posY+this.height-1)){
 							if (this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='|' || this.d.getBoxes()[this.getCenterX()][i].getRealRep()=='-') this.d.getBoxes()[this.getCenterX()][i] = new Box (Box.DOOR);
 						}
 					}
 					else{
 						this.d.getBoxes()[this.getCenterX()][i] = new Box (Box.DOOR);
 					}
 				}
 			}
 		}
 		
 		//Horizontal part
 		for (int j=this.getCenterX();j<r.posX+1;j++){
 			if(this.d.getBoxes()[j][r.getCenterY()]==null){
 				this.d.getBoxes()[j][r.getCenterY()] = new Box (Box.CORRIDOR);
 			}
 			else{
 				if (this.d.getBoxes()[j][r.getCenterY()].getRealRep()=='.' ||this.d.getBoxes()[j][r.getCenterY()].getRealRep()=='#' || this.d.getBoxes()[j][r.getCenterY()].getRealRep()=='%'){
 				}
 				else{
 					if(previous!=null){
 						if ((j==this.getCenterX() || j==previous.posX+previous.width-2 || j==previous.posX+previous.width-1 || j==r.posX)){
 							this.d.getBoxes()[j][r.getCenterY()] = new Box (Box.DOOR);
 						}
 					}
 					else{
 						this.d.getBoxes()[j][r.getCenterY()] = new Box (Box.DOOR);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Getter for the coordonate x of the center of the room calling
 	 * @return int
 	 */
 	 
 	public int getCenterX(){
 		return this.posX+(width/2);
 	}
 	
 	/**
 	 * Getter for the coordonate y of the center of the room calling
 	 * @return int
 	 */
 	
 	public int getCenterY(){
 		return this.posY+(height/2);
 	}
 	
 	/**
 	 * Getter for the coordonate x of the top left corner of the room calling
 	 * @return int
 	 */
 	 
 	public int getPosX(){
 		return this.posX;
 	}
 	
 	/**
 	 * Getter for the coordonate y of the top left corner of the room calling
 	 * @return int
 	 */
 	 
 	public int getPosY(){
 		return this.posY;
 	}
 	
 	/**
 	 * Getter for the width of the room calling
 	 * @return int
 	 */
 	 
 	public int getWidth(){
 		return this.width;
 	}
 	
 	/**
 	 * Getter for the heigth of the room calling
 	 * @return int
 	 */
 	 
 	public int getHeight(){
 		return this.height;
 	}
 	
 	public boolean getWalked(){
 		return Walked;
 	}
  
 	public void setWalked(boolean b){
 		this.Walked = b;
 	}
 	
 	
 	//Method added to testing code
 	public boolean getLinked(){
 		return this.linked;
 	}
 	
 	public int getNumber(){
 		return this.number;
 	}
 	
 	public Dungeon getD(){
 		return this.d;
 	}
 	
 	@Override
 	public boolean equals(Object o){
 		if(!(o instanceof Room)) return false;
 		Room r = (Room) o;
 		return this.posX == r.getPosX()|| this.posX == r.getPosX()||this.Walked == r.getWalked()|| this.width == r.getWidth() || this.height == r.getWidth()|| this.linked == r.getLinked()||this.number == r.getNumber()||this.getD().equals(r.getD());
 	}
 	
 }
