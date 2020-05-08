 /**
  * 
  */
 package input;
 
 import java.awt.im.InputContext;
 import java.util.EnumSet;
 import java.util.Set;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 
import config.Config;

 import util.Util;
 import entities.Player;
 
 
 /**
  * @author Mullings
  *
  */
 public class Controls {
 	public enum Action { UP, DOWN, LEFT, RIGHT, JUMP, FIRE, PULL, RELEASE,
 	                     PAUSE, RESET, FULLSCREEN,
 	                     DEBUG, SKIP, GOD_MODE, SLOW_DOWN, REPLAY, MUTE, QUIT, MENU, OPTIONS }
 	
 	private static Set<Action> cache;
 	private static float mouseX;
 	private static float mouseY;
 
 	public static void update(GameContainer gc) {
 		Input input = gc.getInput();
 		mouseX = input.getMouseX();
 		mouseY = input.getMouseY();
 		cache = EnumSet.noneOf(Action.class);
 		
 		// in other words, if you are mike a.k.a. a bitch
 	     if (Config.COLEMAK && InputContext.getInstance().getLocale().getDisplayVariant().length() > 10) {
 	    	 if (input.isKeyDown(Input.KEY_W)) cache.add(Action.UP);
 	    	 if (input.isKeyPressed(Input.KEY_W)) cache.add(Action.JUMP);
 	    	 if (input.isKeyDown(Input.KEY_R)) cache.add(Action.DOWN);
 	    	 if (input.isKeyDown(Input.KEY_A)) cache.add(Action.LEFT);
 	    	 if (input.isKeyDown(Input.KEY_S)) cache.add(Action.RIGHT);
 	     }
 	     
 	     else{
 	    	 if (input.isKeyDown(Input.KEY_W)) cache.add(Action.UP);
 	    	 if (input.isKeyPressed(Input.KEY_W)) cache.add(Action.JUMP);
 	    	 if (input.isKeyDown(Input.KEY_S)) cache.add(Action.DOWN);
 	    	 if (input.isKeyDown(Input.KEY_A)) cache.add(Action.LEFT);
 	    	 if (input.isKeyDown(Input.KEY_D)) cache.add(Action.RIGHT);
 	     }
 			
 		if (input.isKeyPressed(Input.KEY_SPACE)) cache.add(Action.JUMP);
 		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
 			cache.add(Action.FIRE);
 			cache.add(Action.PULL);
 		}
 		
 		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) cache.add(Action.RELEASE);
 		
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) cache.add(Action.PAUSE);
 		if (input.isKeyPressed(Input.KEY_M)) cache.add(Action.MUTE);
 		if (input.isKeyPressed(Input.KEY_R)) cache.add(Action.RESET);
 		if (input.isKeyPressed(Input.KEY_U)) cache.add(Action.FULLSCREEN);
 		if (input.isKeyPressed(Input.KEY_T)) cache.add(Action.REPLAY);
 		
 		// only to do when paused
 		if (input.isKeyPressed(Input.KEY_Q)) cache.add(Action.QUIT);
 		if (input.isKeyPressed(Input.KEY_J)) cache.add(Action.MENU);
 		if (input.isKeyPressed(Input.KEY_O)) cache.add(Action.OPTIONS);
 
 		// Function keys are for testing and we'll eventually take them out
 		// TODO: remove
 		if (Config.DEBUG){
 			if (input.isKeyPressed(Input.KEY_F5)) cache.add(Action.RESET);
 			if (input.isKeyPressed(Input.KEY_F11)) cache.add(Action.FULLSCREEN);
 			if (input.isKeyPressed(Input.KEY_F2)) cache.add(Action.GOD_MODE);
 			if (input.isKeyPressed(Input.KEY_F3)) cache.add(Action.DEBUG);
 			if (input.isKeyPressed(Input.KEY_F4)) cache.add(Action.SLOW_DOWN);
 			if (input.isKeyPressed(Input.KEY_F6)) cache.add(Action.SKIP);
 			if (input.isKeyPressed(Input.KEY_F7)) cache.add(Action.REPLAY);
 		}
 	}
 	
 	public static double getAimAngle(Player player) {
 		Vector2f mouse = new Vector2f(mouseX, mouseY);
 		Vector2f p = Util.PointToVector2f(player.getScreenPosition());
 		Vector2f aim = mouse.sub(p);
 		return aim.getTheta();
 	}
 	
 	public static boolean isKeyPressed(Action action) {
 		return cache.contains(action);
 	}
 	
 	public static boolean moveKeyPressed() {
 		return (cache.contains(Action.UP) || cache.contains(Action.LEFT)
 				|| cache.contains(Action.DOWN) || cache.contains(Action.RIGHT)
 				|| cache.contains(Action.JUMP) || cache.contains(Action.FIRE)
 				|| cache.contains(Action.PULL) || cache.contains(Action.RELEASE));
 	}
 }
