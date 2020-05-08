 package game;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.*;
 import org.newdawn.slick.state.*;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.*;
 import org.newdawn.slick.particles.*;
 import java.util.*;
 
 public class AstralTanks extends StateBasedGame {
 	public static final int menu = 0;
 	public static final int play = 1;
 	public static final int hs = 2;
	public static final int screenWidth = 800, screenHeight = 600;
 
 	public AstralTanks() { // constructor
 		super("Asteroids and Astral Tanks");
 	}
 
 	@Override
 	public void initStatesList(GameContainer gc) throws SlickException {
 		// You are calling init multiple times which screws up physics!!!
 		// this.getState(menu).init(gc, this);
 		// this.getState(play).init(gc, this);
 		//addState(new Menu());
 		//addState(new Hs());
 		addState(new Play(play));
 		this.enterState(play);
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app;
 		try {
 			app = new AppGameContainer(new AstralTanks());
 			app.setDisplayMode(screenWidth, screenHeight, false); 
 			app.start();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 }
