 package newgame;
 
 import jgame.*;
 import java.awt.Cursor;
 
 import jgame.JGColor;
 import jgame.platform.*;
 import newgame.Hero;
 import newgame.Enemy;
 
 public class Game extends StdGame {
 	// Establish a virtual play field that is 100 pixels by 100 pixels. All
 	// output in this play field is scaled to 800 by 600 pixels when the game
 	// is run as an application. When the game is run as an applet, the output
 	// is scaled to whatever window size is specified by the <applet> tag's
 	// width and height parameters.
 
 	final static int WIDTH = 800;
 	final static int HEIGHT = 593;
 	final static int PFWIDTH = 50;
 	final static int PFHEIGHT = 37;
 	
 	Hero hero = null;
 	Enemy enemy = null;
 	Block block = null;
 	Wall wall = null;
 	Item item = null;
 	
 	int currentWorld = 0; //Start in the lower right
 
 	
 	public Game() {
 		initEngineApplet();
 	}
 		
 	public Game(int width, int height) {
 		initEngine(WIDTH, HEIGHT);
 		setPFSize(PFWIDTH, PFHEIGHT); 
 		setViewOffset(1, 1, true);
 		//setPFWrap(true, true, 10, 10);
 	}
 	
 	@Override
 	public void initCanvas() {
 		JGColor red = new JGColor(255, 0, 0);
 		JGColor black = new JGColor(0, 0, 0);
 		setCanvasSettings(PFWIDTH,PFHEIGHT,16,16,red,black,null);
 	}
 
 	public void initGame() {
 		setMedia();
 		initMap2();
 		setMouseCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
 
 		if (isMidlet()) {
 			setFrameRate(20, 1);
 			setGameSpeed(2.0);
 		} else {
 			setFrameRate(45, 1);
 		}
 		setHighscores(10, new Highscore(0, "nobody"), 15);
 
 		//dbgShowBoundingBox(true);
 	}
 	
 	public void paintFrameTitle() {
 		JGColor black = new JGColor(0, 0, 0);
 		JGFont courier = new JGFont("Courier", 0, 32);
 		drawString("Welcome to SwordRun", pfWidth()/2, pfHeight()/2 - 200, 0, courier, black);
 	}
 	
 	public void setMedia() {
 		defineMedia("mygame.tbl");
 		defineMedia("outdoors.tbl");
 		defineMedia("character.tbl");
 		defineMedia("game.tbl");
 		setBGImage("grass1");
 		defineMedia("sword.tbl");
 		defineMedia("boulder.tbl");
 	}
 	
 	public void initMap2() {
 		double random = Math.random() * PFHEIGHT + 1;
 		double random2 = Math.random() * PFWIDTH + 1;
 		int starting = (int)random;
 		int starting2 = (int)random2;
 		for(int i=0; i <= PFHEIGHT; i++) {
 			setTile(starting, i, "p2");
 			setTile(starting+1, i, "p1");
 			setTile(starting+2, i, "p1");
 			setTile(starting+3, i, "p3");
 		}
 		for(int i=0; i <= PFWIDTH; i++) {
 			setTile(i, starting2, "p4");
 			setTile(i, starting2+1, "p1");
 			setTile(i, starting2+2, "p1");
 			setTile(i, starting2+3, "p5");
 		}
 	}
 	
 
 	public void initNewLife() {
 		removeObjects(null,0);
 		//Enemies have 'facing' which specifies the direction they are headed in
 		//enemy = new Enemy(4, 380, 2, 'y', "ewalkb", 15, this, 'd', true);
 		hero = new Hero(pfWidth()/2,pfHeight()-100,5, this, this, 50);
 		block = new Block(200, 100, "boulder1", this, hero, "boulder1");
 		//item = new Item(400, 300, "block1", this, hero, "block1");
 		//wall = new Wall(300, 200, "boulder4", this, hero, "boulder4");
 		//new Enemy(4, gametime, 2, 'x', "ewalkr", 15, this, 'r', true);
 //		for (int i=0; i < 30; i ++) {
 //			new Wall(10, 20*i, "boulder4", this, hero, "boulder4");
 //		}
 //		for(int i=0; i < 30; i++){
 //			double randomy = Math.random() * 800 + 1;
 //			double randomx = Math.random() * 100 + 1;
 //			new Enemy(randomx, gametime+randomy, 2, 'x', "ewalkr", 15, this, 'r', true);
 //		}
 	}
 	
 	public void startGameOver() {
 		removeObjects(null,0);
 	}
 	
 	public void doFrameInGame() {
 		moveObjects();
 		checkCollision(2,1); // enemies hit player
 		checkCollision(4,2); // bullets hit enemies
 		checkCollision(1,5); // player hit block
 		checkCollision(1,6); // player hit wall
 		checkCollision(2,5); // enemy hit block
 		checkCollision(5,6); // block hit wall
 		checkCollision(1,7); // player hit health
 		//if (gametime>=500 && countObjects("enemy",0)==0) levelDone();
 		setWalls();
 		checkPosition();
 	}
 	
 	
 	/*
 	 * 0 is the world in the lower right
 	 * 1 is the world in the lower left
 	 * 2 is the world in the upper left
 	 * 3 is the world in the upper right
 	 *  ----------
 	 * | 2  |  3  |
 	 * |----------|
 	 * | 1  |  0  |
 	 *  -----------
 	 */
 	public void checkPosition() {
 
 		if (!hero.isOnPF(-10, -10) && hero.orientation==9 && (currentWorld==0 || currentWorld==3)) {
 			
 			//Determine the next world that you will be in
 			if(currentWorld==0){
 				currentWorld = 1;
 			}
 			else{
 				currentWorld=2;
 			}
 			
 			block.setPos(pfWidth() - 50 - (hero.x - block.x), block.getLastY());
 			hero.setPos(pfWidth()-50, hero.getLastY());
 			
 			fillBG("pa");
 			initMap2();
 		}
 		else if (!hero.isOnPF(-10, -10) && hero.orientation==3 && (currentWorld==1 || currentWorld==2)) {
 			
 			//Determine the next world that you will be in
 			if(currentWorld==2){
 				currentWorld = 3;
 			}
 			else{
 				currentWorld=0;
 			}
 			
 			block.setPos(0 + (block.x - hero.x), block.getLastY());
 			hero.setPos(0, hero.getLastY());
 			fillBG("pa");
 			for (int i=0; i<pfWidth(); i++){
 				setTile(i, 24, "p4");
 				setTile(i, 25, "p1");
 				setTile(i, 26, "p1");
 				setTile(i, 27, "p5");
 			}
 		}
 		else if (!hero.isOnPF(-10, -10) && hero.orientation==12 && (currentWorld==0 || currentWorld==1)) {
 			
 			//Determine the next world that you will be in
 			if(currentWorld==0){
 				currentWorld = 3;
 			}
 			else{
 				currentWorld=2;
 			}
 			
 			//block.setPos(pfWidth() - 50 - (hero.x - block.x), block.getLastY());
 			hero.setPos(hero.getLastX(), pfHeight()-50);
 			fillBG("pa");
 			for (int i=0; i<pfWidth(); i++){
 				setTile(i, 24, "p4");
 				setTile(i, 25, "p1");
 				setTile(i, 26, "p1");
 				setTile(i, 27, "p5");
 			}
 		}
 		else if (!hero.isOnPF(-10, -10) && hero.orientation==6 && (currentWorld==2 || currentWorld==3)){
 			
 			//Determine the next world that you will be in
			if(currentWorld==2){
				currentWorld = 1;
 			}
 			else{
				currentWorld=2;
 			}
 			
 			
 			hero.setPos(hero.getLastX(), 0);
 			fillBG("pa");
 			for (int i=0; i<pfWidth(); i++){
 				setTile(i, 24, "p4");
 				setTile(i, 25, "p1");
 				setTile(i, 26, "p1");
 				setTile(i, 27, "p5");
 			}
 		}
 	}
 	
 	public void setWalls() {
 		if(currentWorld==0) {
 			if (hero.x >= pfWidth()-25){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 			if (hero.y >= pfHeight()-45){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 		}
 		else if (currentWorld==1) {
 			if (hero.x <= 0){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 			if (hero.y >= pfHeight()-45){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 		}
 		else if(currentWorld==2) {
 			if (hero.x <= 0){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 			if (hero.y <= 0){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 		}
 		else if(currentWorld==3) {
 			if (hero.x >= pfWidth()-25){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 			if (hero.y <= 0){
 				hero.setPos(hero.getLastX(), hero.getLastY());
 			}
 		}
 	}
 	
 	public void incrementLevel() {
 		score += 50;
 		if (level<7) level++;
 		stage++;
 	}
 	
 	JGFont scoring_font = new JGFont("Arial",0,8);
 	
 	public static void main(String[] args) {
 		// Run the game at a window size of 800 by 600 pixels.
 
 		new Game(800, 593);
 	}
 
 }
