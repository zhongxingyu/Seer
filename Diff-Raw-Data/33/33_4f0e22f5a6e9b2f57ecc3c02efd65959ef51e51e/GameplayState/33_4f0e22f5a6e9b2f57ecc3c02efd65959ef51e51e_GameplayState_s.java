 package com.WebSiteNameHere;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 //IMPORTANT: (0,0) is top left of screen
 public class GameplayState extends BasicGameState {
 
 	//Just a default to see if anything goes wrong
 	int stateID = -1;
 
 	//Default gravity constant, needs to be played with
 	public static final float GRAVITY = 9.8f;
 	public static final int GROUND_HEIGHT = 450;
 
 	//Create our hero using the hero class which extends Image
 	Hero red;
 	//	Sprite bg;
 
 	//Create the backgrounds (which can also be foreground)
 	//Must create a new ArrayList<String> for each layer of
 	//background you want to create. (At least for now.)
 	//Then make a Background class.
	ArrayList<String> foregroundImageLocations;
	Background foreground;
 
 	GameplayState( int stateID ) 
 	{
 		this.stateID = stateID;
 	}
 
 	@Override
 	public int getID() 
 	{
 		return stateID;
 	}
 
 	//Use this method to set up all variables such as the images
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException 
 	{
 		red = new Hero(20, 450); //position x, y
 		//		bg = new Sprite("/res/concepts/setting/InfiRun_Set1.jpg");
 
 		//-----
 		//Initialize each ArrayList<String> that you made for each layer of background
 		//then .add() the string location of each file you want to add. After that,
 		//initialize the Background class object, and pass it the parameters you want.
 		//Paramaters are (X-coordinate, Y-coordinate, Speed, ArrayList<String>).
 		//-Celtic
 		//-----
 		foregroundImageLocations = new ArrayList<String>();
 		foregroundImageLocations.add("/res/sprites/bg/foreground1.png");
 //		foregroundImageLocations.add("/res/sprites/bg/foreground2.png");
 
 		foreground = new Background(0f, 536f, 10f, foregroundImageLocations);
 	}
 
 	//Use render to draw everything
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException 
 	{
 		Double vert = (double)red.vertVelocity;
 		Double foregroundX = (double)foreground.x;
 
 		g.drawString("VertSpeed: " + vert.toString(), 90f, 10f);
 		g.drawString("ForegroundX: " + foregroundX, 600f, 10f);
 
 		//red rendering is handled by Hero class now
 		//-----
 		//The order the render methods are called is the order they will
 		//be drawn in, so if you call red.render() before foreground.render()
 		//then foreground will be "covering" or "over" red.
 		// -Celtic
 		//-----
 		foreground.render();
 		red.render();
 	}
 
 	//This is where the meat of everything happens, updating position etc.
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException 
 	{
 		//Get all inputs
 		Input input = gc.getInput();
 		if(input.isKeyDown(Input.KEY_W)&&(red.y==GROUND_HEIGHT))
 			//Only jump when red is on the ground
 			red.vertVelocity = 7;
 		else if(input.isKeyDown(Input.KEY_S)&&(red.y==GROUND_HEIGHT)){
 			red.rolling=true;
 		}
 
 		foreground.update();
 		red.update();
 		//other option is to create flags in Hero class and update 'em here (before calling on red.update()) via 
 		//key bindings, and change animation depending on it, or make animations public and control 'em directly
 		//directly from here, or update 'em by use of other variables: velocity, etc.
 
 
 		//Update variables
 		red.vertVelocity -= GRAVITY/60.0f;
 
 		//Update positions
 		red.y =red.y-red.vertVelocity;
 		if(red.y>GROUND_HEIGHT) {
 			red.y = GROUND_HEIGHT;
 		}
 
 		if(red.vertVelocity < -11f)
 			red.vertVelocity = -10.5f;
 
 	}
 
 }
