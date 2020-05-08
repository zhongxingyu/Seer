 package hhu.propra_2013.gruppe_13;
 
 import java.util.ArrayList;
 
 class CoreLogic implements Runnable {
 	
 	// set square root of 2 and define a boolean variable for the game loop
 	private static final double SQRT_2 = 1.41421356237309504880168872420969807856967187537694807317667973799;	// http://en.wikipedia.org/wiki/Square_root_of_2
 	private boolean 			gameRunning;
 	private boolean 			bulletEnable;
 	private int 				bulletType;
 	private long 				bulletCoolDown;
 	private int					bulletCoolDownTime;
 	
 	// Boolean variables for movement and collision detection, location counter for the room
 	private boolean 			down, up, right, left, upLeft, upRight , downLeft, downRight;				//für die Bewegungsrichtungen
 	private boolean				north, east, south, west, northwest, northeast, southwest, southeast;		//zum schießen in die Himmelsrichtungen
 	
 	// variables for collision detection
 	private boolean 			freeRight, freeUp, freeDown, freeLeft;
 	private double 				distDown, distUp, distRight, distLeft;
 	
 	// variables for navigating in the level
 	private int 				locationX, locationY;
 	private CoreRoom			currentRoom;
 	
 	// information about the level
 	private int 				stage;
 	private String				boss;
 	
 	
 	private Figure			 	figure; //TODO check why it was GameObjects
 	private CoreO_Game			game;
 	
 	// figure values
 	private double 				figX, figY;
 	private double 				figVX, figVY;
 	private boolean				punch, use, bomb;									//für Aktionen
 	private int					figHP;
 	
 	// List of all Objects within the game
 	private CoreLevel 	level;
 
 	void setGameRunning(boolean boolIn){
 		gameRunning = boolIn;
 		System.out.println("maria wars");
 	}
 
 	// Setter methods to determine whether a movement shall be initiated 
 	void setDown(boolean in){
 		down = in;
 	}
 	
 	void setUp(boolean in){
 		up = in;
 	}
 
 	void setRight(boolean in){
 		right = in;
 	}
 	
 	void setLeft(boolean in){
 		left = in;
 	}
 
 	void setUpRight(boolean in) {
 		upRight = in;
 	}
 
 	void setUpLeft(boolean in) {
 		upLeft = in;
 	}
 
 	void setDownRight(boolean in) {
 		downRight = in;		
 	}
 
 	void setDownLeft(boolean in) {
 		downLeft = in;	
 	}
 	
 	void setPunch(boolean in) {
 		punch = in;		
 	}
 	
 	void setUse(boolean in) {
 		use = in;		
 	}
 	
 	void setBomb(boolean in) {
 		bomb = in;		
 	}
 	
 	void setNorth(boolean in) {
 		north = in;		
 	}
 	
 	void setEast(boolean in) {
 		east = in;		
 	}
 	
 	void setSouth(boolean in) {
 		south = in;		
 	}
 	
 	void setWest(boolean in) {
 		west = in;		
 	}
 	
 	void setNorthwest(boolean in) {
 		northwest = in;		
 	}
 	
 	void setNortheast(boolean in) {
 		northeast = in;		
 	}
 	
 	void setSouthwest(boolean in) {
 		southwest = in;		
 	}
 	
 	void setSoutheast(boolean in) {
 		southeast = in;		
 	}
 
 	// Initiate the current objects variables
 	CoreLogic(Figure inFigure, CoreO_Game inGame) {
 		gameRunning = true;
 		
 		figure 		= inFigure;
 		game		= inGame;
 		
 		stage = 1;
 		boss = "test";
 		
 		//create Level
 		level = new CoreLevel(figure);
 		level.buildLevel(stage, boss);
 		//find out where in the level we are, switching rooms will be relative to this value
 		locationX = level.getStartX();
 		locationY = level.getStartY();
 		currentRoom=level.getRoom(locationX, locationY);
 		currentRoom.getContent().add(figure);
 		
 		freeRight	= true;
 		freeLeft	= true;
 		freeUp		= true;
 		freeDown	= true;
 		
 		bulletEnable = true;
 		bulletType 	= Bullet.PLAYER_BULLET_STD;
 		bulletCoolDownTime = 500;
 		
 	}
 	
 	private void setRoom(int newLocationX, int newLocationY, Figure inFigure) {
 		CoreRoom tempRoom;
 		figure = inFigure;
 		locationX = newLocationX;
 		locationY = newLocationY;
 		tempRoom = level.getRoom(locationX, locationY);
 		currentRoom = tempRoom;
 		tempRoom.getContent().add(figure);
 		game.setRoom(tempRoom);
 	}
 
 	// Do the Artificial Intelligence for all Enemies
 	private void enemyAI() {
 		EnemyMelee enemyMelee;
		ArrayList<CoreGameObjects> collidable = currentRoom.getContent();
 		
 		// iterate over all objects and do the AI of all Enemies 
 		for (int i=1; i<collidable.size(); i++) {
 			if (collidable.get(i) instanceof EnemyMelee) {
 				enemyMelee = (EnemyMelee)collidable.get(i);
 				enemyMelee.artificialIntelligence((Figure)figure, collidable);
 				
 				// check whether the enemy is dead yet
 				if(enemyMelee.leftForDead()) {
					currentRoom.getContent().remove(enemyMelee);
 				}
 			}
 		}
 	}
 
 	
 	CoreLevel getLevel(){
 		return level;
 	}
 	
 	int getCurrentX(){
 		return locationX;
 	}
 	
 	int getCurrentY(){
 		return locationY;
 	}
 	
 	private void checkCollision() {
 		// reset collision values
 		freeRight		= true;
 		freeLeft		= true;
 		freeUp			= true;
 		freeDown		= true;
 		
 		// reset distance values
 		distUp			= 50;
 		distDown		= 50;
 		distLeft		= 50;
 		distRight		= 50;
 		
 		// method variables for 
 		double figR 		= figure.getRad();
 		double figWidth 	= figure.getWidth();
 		double figHeight 	= figure.getHeight();
 		double tmpX, tmpY;
 		
 		double objX, objY;
 		double objR;
 		double objWidth, objHeight;
 		
 		double collOne, collTwo, collThree, collFour;
 		
 		//variables for handling door-collision, names are the same as in the door class
 		int destination;
 		boolean open, enabled;
 		//variable for handling enemy Collision
 		int hp;
 		
 		ArrayList<CoreGameObjects> collidable = currentRoom.getContent();
 		CoreGameObjects collided;
 		
 		// iterate over all objects within the room, excepting the figure, of course
 		for(int i=1; i<collidable.size(); i++) {
 			collided 	= collidable.get(i);
 			objX 		= collided.getPosX();
 			objY 		= collided.getPosY();
 			objR 		= collided.getRad();
 			
 			objWidth 	= collided.getWidth();
 			objHeight 	= collided.getHeight();
 
 			collOne 	= 1;
 			collTwo		= 1;
 			collThree	= 1;
 			collFour	= 1;
 			
 			// First check whether the objects are close enough to encounter one another within the next couple of moves, use squares, saves a couple of sqrt calls
 			if (((objX-figX)*(objX-figX)+(objY-figY)*(objY-figY)) < ((figR+objR)*(figR+objR))) {
 				tmpX = figX-objX;
 				tmpY = figY-objY;
 				
 				/* Reference point for all objects is the top left corner, as drawing exclusively starts here
 				 * First of all we check, whether the object in question is on a collidable course in x or y direction, 
 				 * once that is done, the remaining distance between the two objects will be computed and saved in a 
 				 * corresponding double value.
 				 * 
 				 *  Start with collisions in y-direction */
 				if(Math.abs(tmpX) < (figWidth+objWidth)/2.) {
 					// check whether the object is to the left or right of the figure and whether the figure could reach it within one step
 					if(((tmpY) > 0) && (figVY > (collOne = tmpY-(figHeight+objHeight)/2.))) {
 						// set remaining distance and checking variable
 						distUp = Math.min(distUp, collOne);
 						freeUp = false;
 					} else if ((tmpY < 0) && (figVY > (collTwo = -tmpY-(figHeight+objHeight)/2.))) {
 						distDown = Math.min(distDown, collTwo);
 						freeDown = false;
 					}
 				}
 				
 				// this will cover collision detection in x-direction, analogous to above
 				if(Math.abs(tmpY) < (figHeight+objHeight)/2.) {
 					if((tmpX > 0) && (figVX > (collThree=tmpX-(figWidth+objWidth)/2.))) {
 						distLeft = Math.min(distLeft, collThree);
 						freeLeft = false;
 					} else if ((tmpX < 0) && (figVX > (collFour=-tmpX-(figWidth+objWidth)/2.))) {
 						distRight = Math.min(distRight, collFour);
 						freeRight = false;
 					}
 				}
 				
 				
 				// Check collisions with objects, act accordingly
 				if (collOne == 0 || collTwo == 0 || collThree == 0 || collFour == 0) {
 					if (collided instanceof MISCDoor) {
 						
 						destination = ((MISCDoor) collided).getDestination(); //cast because eclipse wants it
 						open = ((MISCDoor) collided).getOpen();
 						
 						
 						if (open == true){ //check if door is 'officially' there and open							
 							switch (destination){	//only advance trough door if player is moving in the direction of the door
 													//diagonal movement should work too
 							case 0:
 								if (upLeft == true || up == true || upRight == true ){
 									this.switchRoom(destination);
 									System.out.println("wuhu eine tür!"+destination);
 								}
 								break;
 							
 							case 1:
 								if (right == true || upRight == true || downRight == true){
 									this.switchRoom(destination);
 									System.out.println("wuhu eine tür!"+destination);
 								}					
 								break;
 							
 							case 2:
 								if (down == true || downRight == true || downLeft == true){
 									this.switchRoom(destination);
 									System.out.println("wuhu eine tür!"+destination);
 								}
 								break;
 							
 							case 3:
 								if (left == true || downLeft == true || upLeft == true){
 									this.switchRoom(destination);
 									System.out.println("wuhu eine tür! "+destination);
 								}
 								break;
 							
 							case 4:
 								this.switchRoom(destination);
 								
 							}
 							
 						}
 						
 					}
 					
 					if (collided instanceof EnemyMelee) {
 						((EnemyMelee) collided).attack();
 						
 						//hp = figure.getHP();//get current hp
 						//hp--;//apply damage
 						//figure.setHP(hp);//set hp to the new value
 						// TODO: do even better Enemy shit
 					}
 					
 					if (collided instanceof Item) { 
 						((Item) collided).modFigure(collidable, (Figure)figure);
 					}
 					
 					if (collided instanceof MISCTarget) {
 						game.end(true);
 					}
 					
 					// See if a bullet hits the player, if so, kill it... KILL IT WITH FIRE!!
 					if (collided instanceof Bullet) {
 						collided.attack();
 					}
 				}
 			}
 		}
 	}
 	
 	
 	/* This is the actual movement method, which checks for all directions whether the figure needs to be moved.
 	 * Additionally the method checks whether the figure has reached a boundary and will prevent it from moving out of the gaming area.  */	
 	private void moveFigure() {
 		// convert diagonal movement into two horizontal components, diagonal movement is slowed,
 		// as it will take place into two directions
 		if (upLeft) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			left 	= true;
 			up		= true;
 		}
 		
 		if (upRight) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			right 	= true;
 			up		= true;
 		}
 		
 		if (downLeft) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			left 	= true;
 			down	= true;
 		}
 		
 		if (downRight) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			right 	= true;
 			down	= true;
 		}
 		
 		// horizontal and vertical movement handlers, these also try to find out, whether there is anything
 		// within a diagonal direction inhibiting movement. 
 		if(right) {
 			if(freeRight){
 				if (figX+figVX >= 21.5)		figX  = 21.5;
 				else						figX += figVX;
 			} else 							figX += distRight;
 		}
 		
 		if(left) {
 			if(freeLeft) {
 				if (figX-figVX <= 0.5) 		figX  = 0.5;
 				else						figX -= figVX;
 			} else							figX -= distLeft;
 		}
 
 		if(up) {
 			if(freeUp) {
 				if (figY-figVY <= 0.5) 		figY  = 0.5;
 				else						figY -= figVY;
 			} else							figY -= distUp;
 		}
 		
 		if(down) {
 			if(freeDown) {
 				if (figY+figVY >= 12.5) 	figY  = 12.5;
 				else						figY += figVY;
 			} else							figY += distDown;
 		}
 		
 		// finally set the position of the figure
 		figure.setPos(figX, figY);
 	}
 	
 	private void setDirection() {
 		
 	}
 	
 	// Propagate all Bullets and create new Attacks
 	private void attacks() {
 		// Iterate over all Bullets and propagate them 
 		ArrayList<CoreGameObjects> collidable = currentRoom.getContent();
 		Bullet bullet;
 		
 		for (int i=0; i<collidable.size(); i++) {
 			if (collidable.get(i) instanceof Bullet) {
 				bullet = (Bullet) collidable.get(i);
 				bullet.propagate(collidable);
 				
 				// Check whether we can destroy the bullet
 				if (bullet.getFinished()) {
 					currentRoom.getContent().remove(bullet);
 				}
 			}
 		}
 		
 		if (!bulletEnable) {
 			if (System.currentTimeMillis() - bulletCoolDown > bulletCoolDownTime) bulletEnable = true;
 		}
 		
 		// Create new Bullets if the player wishes to do so, and the cooldown for shooting has expired
 		if (north || east || south || west || northeast || northwest || southeast || southwest) {
 			if (bulletEnable) {
 				// Save current system time in order to check the cooldown
 				bulletCoolDown = System.currentTimeMillis();
 				
 				// Check which direction to shoot, this depends on the input
 				int signVX = 0;
 				int signVY = 0;
 				
 				if (north) {
 					signVX = 0;
 					signVY = -1;
 				}
 				
 				else if (east) {
 					signVX = 1;
 					signVY = 0;
 				}
 				
 				else if (south) {
 					signVX = 0;
 					signVY = 1;
 				}
 				
 				else if (west) {
 					signVX = -1;
 					signVY = 0;
 				}
 				
 				else if (northeast) {
 					signVX = 1;
 					signVY = -1;
 				}
 				
 				else if (northwest) {
 					signVX = -1;
 					signVY = -1;
 				}
 				
 				else if (southeast) {
 					signVX = 1;
 					signVY = 1;
 				}
 				
 				else if (southwest) {
 					signVX = -1;
 					signVY = 1;
 				}
 				
 				CoreGameObjects initBullet = new Bullet(bulletType, figX, figY, figVX, figVY, signVX, signVY);
 
 				currentRoom.getContent().add(initBullet);
 				bulletEnable = false;
 
 			}
 		}
 	}
 	
 	private void checkFigure(){
 		if(figHP <= 0){
 			game.end(false);
 			System.out.println("You died!");
 		}
 	}
 	
 	private void switchRoom(int destination){ 
 
 		//wechselt den Raum, falls die Figur an einer Stelle steht an der im aktuellen Raum eine Tür ist
 		switch (destination){ //prüft in welchem Raum die Figur ist (bisher 0-2 für die 3 Räume)
 
 		case(0)://Door leads up
 			locationY--;
 			figY = 12.49;
 			this.setRoom(locationX, locationY, figure);
 			break;
 		
 		case(1): //Door leads to the right
 			locationX++; 
 			figX = 0.51; //linker Spielfeldrand
 			this.setRoom(locationX, locationY, figure); //neuen Raum and Grafik und Logik geben
 			break;
 			
 		case(2): //Door leads down
 			locationY++;
 			figY = 0.51;
 			this.setRoom(locationX, locationY, figure);
 			break;
 		
 		case(3): //Door leads left
 			locationX--;
 			figX = 21.49;//rechter Spielfeldrand
 			this.setRoom(locationX, locationY, figure);
 			break;
 			
 		case(4):
 			stage++;
 			boss = "test";//TODO maybe change the boss sometimes
 			level.buildLevel(stage, boss);
 			locationX = level.getStartX();
 			locationY = level.getStartY();
 			this.setRoom(locationX, locationY, figure);
 			
 			break;
 		}	
 	}
 	
 	@Override //Override run method from interface, this will have the game loop
 	public void run() {
 		long time;
 		long temp;
 		
 		// game loop
 		while (gameRunning) {
 			time = System.currentTimeMillis();
 			
 			// get current figure positions and velocities
 			figX 	= figure.getPosX();
 			figY 	= figure.getPosY();
 			figVX	= figure.getVX();
 			figVY	= figure.getVY();
 			figHP	= figure.getHP();
 						
 			// do the actual logic in this game
 			this.checkCollision();
 			this.moveFigure();
 			this.attacks();
 			this.enemyAI();
 			this.checkFigure();
 			
 			// set the thread asleep, we don't need it too often
 			try {
 				if((temp=System.currentTimeMillis()-time) < 16)
 				Thread.sleep(16-temp);
 			} catch (InterruptedException e) {
 				// don't care if the thread is interrupted
 			}
 		}
 	}
 }
