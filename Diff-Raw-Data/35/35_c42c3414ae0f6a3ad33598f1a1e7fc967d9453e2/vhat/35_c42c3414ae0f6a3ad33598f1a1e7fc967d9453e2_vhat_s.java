 package game.vhat;
 /*
  * Programmed By:  BA Media Club
  * Programmed For:  BA Media Club
  * Program Description:  A game... expanding on this later
  */
 
 //TODO:  checkpoints
 
 import org.newdawn.slick.*;
//import org.newdawn.slick.tiled.*;
 import org.newdawn.slick.gui.*;
 import java.awt.Font;
 /*
  * TODO: make it with states so that you can have a menu screen and stuff
  * 		 also, other stuff
  */
 public class vhat extends BasicGame{
 	
 	player henry;						// Reference the player class to create a "henry" player object
 	girl girl;							// Makes henry's companion
 	TextField girlTipOne;				// This is where the user will be able to hear what henry's companion has to say
 	TextField girlTipTwo;				//		There are three because a text field does not support multi-line text
 	TextField girlTipThree;
	//TiledMap mapFirst;					// The map for the first location
	mapManager mapFirst;			//		information about the map
	//TiledMap mapSecond;					// The map for the second location
	mapManager mapSecond;			//		information about the map
 	TrueTypeFont f;
 	
 	// Keeps track of all the locations that henry has been
 	boolean checkPoints[] = {true, false, false, false};
 	
 	int speed = 1;						// How many pixels henry moves 
 	static int screen_width = 960;	
 	static int screen_height = 705;
 	
 	// Construct calls construct from BasicGame 
 	public vhat(){
 		super("vhat");
 	}
 	
 	// Initializes everything needed
 	public void init(GameContainer gc) throws SlickException {
 		f = new TrueTypeFont(new Font("", java.awt.Font.PLAIN, 14), true);			// Creates a font object
 		
 		gc.setShowFPS(false);
 		
		//mapFirst = new TiledMap("res/testRmOne.tmx");								// Creates a new "map" object from the TiledMap class (from Slick2D)
		//mapSecond = new TiledMap("res/testRmTwo.tmx");								//		Same as above except with a different map
	
		mapFirst = new mapManager("res/testRmOne.tmx", 1, 32*20,32*20-64); 			// Creates an instance of mapManager
																					// mapFirst is 30x20 tiles (tiles are 32x32 pixels)
 		mapSecond = new mapManager("res/testRmTwo.tmx", 2, 32*5,0);				//		Same as above
 	
 		henry = new player(mapFirst.get_xSpawn(), mapFirst.get_ySpawn(), 
 				"res/henry.png", location.entrance);								// Creates a new "henry" object from the player class
 		
		girl = new girl(0,0,"res/ninja.png");											// Makes henry's companion
 		girlTipOne = new TextField(gc, f, girl.get_width(), 640, 500, 20);			// Allows for output from henry's companion
 		girlTipOne.setAcceptingInput(false);										// So the user can't input anything
 		girlTipTwo = new TextField(gc, f, girl.get_width(), 660, 500, 20);			
 		girlTipTwo.setAcceptingInput(false);
 		girlTipThree = new TextField(gc, f, girl.get_width(), 680, 500, 20);			
 		girlTipThree.setAcceptingInput(false);
 		
 		updateGirlSpeak();
 	}
 		
 	// Updates variables based on the user input	
 	public void update(GameContainer gc, int delta) throws SlickException {
 		Input input = gc.getInput();	// Creates an input object
 		
 		if (henry.get_loc() == location.entrance){				// So the computer can keep track which map henry is on
 			makeInBound(mapFirst);		//		and make henry inBound in that map.
 		}else if (henry.get_loc() == location.challengeOne){
 			makeInBound(mapSecond);
 		}
 		
 		// TODO: use event handlers to make this more efficient
 		changeLocation();		// Changes the location of henry if he is at a changing point
 		
 		// Determines the new position on henry depending on the arrow key that is pressed
 		// Keep in mind that the map is the thing that is actually moving - henry is always stationary
 		if (input.isKeyDown(Input.KEY_UP)){				
 			henry.ch_y(-speed);
 		}else if (input.isKeyDown(Input.KEY_DOWN)){
 			henry.ch_y(speed);
 		}else if (input.isKeyDown(Input.KEY_RIGHT)){
 			henry.ch_x(speed);
 		}else if (input.isKeyDown(Input.KEY_LEFT)){
 			henry.ch_x(-speed);
 		}
 		
 		if (input.isKeyPressed(Input.KEY_A)){
 			henry.interact(girl);	
 		}
 	}
 
 	// Renders things to the screen based on the variables that were modified in the update method
 	public void render(GameContainer gc, Graphics g) throws SlickException { 
 		if (henry.get_loc() == location.entrance){				// Determines which map to render based on location
 			mapFirst.render(0, 0);		//	(which map henry is on)
 		}else if (henry.get_loc() == location.challengeOne){
 			mapSecond.render(0, 0);
 		}
 		henry.draw(henry.get_x(),henry.get_y(), (float)1);		// Draws henry at his x and y position		
 		girl.draw(0,screen_height-girl.get_height(),1);			// Draws henry's companion
 		girlTipOne.render(gc, g);								// Draws what the companion has to say
 		girlTipTwo.render(gc, g);
 		girlTipThree.render(gc, g);
 	}
 	
 	/*
 	 * A method that determines when the girl will speak and what she will say
 	 */
 	public void updateGirlSpeak() throws SlickException{
 		girlTipOne.setText(girl.get_words(0));
 		girlTipTwo.setText(girl.get_words(1));
 		girlTipThree.setText(girl.get_words(2));
		
		/*
		if (henry.get_loc() == 1){
			girlTipOne.setText("Hello Henry.  I am your companion.  I can help you through this.");
			girlTipTwo.setText("It seems you are in the first location.");
			girlTipThree.setText("");
		}else if (henry.get_loc() ==2){
			girlTipOne.setText("Nice.  You are in the second location.");
			girlTipTwo.setText("");
			girlTipThree.setText("");
		}
		*/
 	}
 	
 	/* 
 	 * Makes sure that henry is in bounds.  If henry is not in bounds, he is moved back.
 	 * This method includes keeping henry on the screen and making sure he stays on the path.
 	 */ 
 	public void makeInBound(mapManager map) throws SlickException{
 		// For keeping henry in the screen
 		if (henry.get_y() < 0){												// Upper boundary
 			henry.ch_y(speed);}
 		if (henry.get_y() > (map.getHeight()*map.getTileHeight() - 64)){	// Lower boundary
 			henry.ch_y(-speed);}
 		if (henry.get_x() < 0){												// Left-most boundary
 			henry.ch_x(speed);}
 		if (henry.get_x() > (map.getWidth()*map.getTileWidth() - 32)){		// Right-most boundary
 			henry.ch_x(-speed);}
 		
 		// Stupid thing for keeping henry in the path 
 		int top = (int)henry.get_y()+48;						//|
 		int bottom = (int)henry.get_y()+henry.get_height();		//| Defines henry's 
 		int left = (int)henry.get_x()+4;						//| hit box
 		int right = (int)henry.get_x()+henry.get_width()-4;		//|
 		
 		int t = map.getTileId(((int)((left+right)/2)/32),(int)(((top+bottom)/2)/32), 0);	// Tiled id at center of hit box
 		try{
 			if (t >= 3 && t <= 10){		// If the center of the hit box has a tile id that corresponds to a wall tile, adjust
 				if (map.getTileId(((int)((left+right)/2)/32), (int)(top/32), 0) <= 2 ||				// Top tile is not wall
 						map.getTileId(((int)((left+right)/2)/32), (int)(top/32), 0) >= 11){
 					henry.ch_y(-2);
 				}else if (map.getTileId(((int)((left+right)/2)/32), (int)(bottom/32), 0) <=2 ||		// Bottom tile is not a wall
 						map.getTileId(((int)((left+right)/2)/32), (int)(bottom/32), 0) >= 11){
 					henry.ch_y(2);
 				}else if (map.getTileId((int)(left/32), (int)((top+bottom)/2)/32, 0) <= 2 ||		// Leftmost tile " " " "
 						map.getTileId((int)(left/32), (int)((top+bottom)/2)/32, 0) >= 11){
 					henry.ch_x(-2);
 				}else if (map.getTileId((int)(right/32), (int)((top+bottom)/2)/32, 0) <= 2 ||		// Rightmost tile " " " "
 						map.getTileId((int)(right/32), (int)((top+bottom)/2)/32, 0) >= 11){
 					henry.ch_x(2);
 				}
 			}
 		}catch (ArrayIndexOutOfBoundsException e){				// So that it does not goof up
 			e.printStackTrace();
 		}catch (IndexOutOfBoundsException e){
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/*
 	 * Changes the map that is displayed 
 	 * 		I know I am going to look at this tomorrow and want to throw this computer
 	 * 		out a window. 
 	 * 
 	 * 		TODO: will need to have selection for different maps
 	 */
 	public void changeLocation() throws SlickException{
 		
 		if (henry.get_loc() == location.entrance){
 			if (henry.get_y() == (mapFirst.getHeight()*mapFirst.getTileHeight()-64) && 
 					henry.get_x() > 32*28 && henry.get_x() < 32*30){						// When henry is on a transporting spot
				henry.set_loc(location.challengeOne);																// Moves onto next location
 				henry.new_x(mapSecond.get_xSpawn());
 				henry.new_y(mapSecond.get_ySpawn());
 		 	
 				girl.set_words_bulk("Congratulations!", "you made it to the second location", "");
 				updateGirlSpeak();
 		}
 		}else if (henry.get_loc() == location.challengeOne){
 			if (henry.get_y() == 0 && henry.get_x() > 32*2 && henry.get_x() < 32*3){
 				henry.set_loc(location.entrance);
 				henry.new_x(mapFirst.get_xSpawn());
 				henry.new_y(mapFirst.get_ySpawn());
 
 				girl.set_words_bulk("Hello Henry!", "this is the first location", "why don't you look around");
 				updateGirlSpeak();
 			}
 		}
 	}
 	
 	public static void main(String[] args) throws SlickException{
 		AppGameContainer app = new AppGameContainer(new vhat());	// Creates new "app" object
 		app.setDisplayMode(screen_width, screen_height, false);		// Sets the display mode
 		app.start();												// Runs the app.
 	}
 
 }
