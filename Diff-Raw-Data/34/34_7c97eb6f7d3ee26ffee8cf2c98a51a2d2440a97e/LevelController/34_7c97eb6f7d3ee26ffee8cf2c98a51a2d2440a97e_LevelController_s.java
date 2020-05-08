 package com.me.Roguish.Controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.me.Roguish.Model.Level;
 
 public class LevelController {
 	private Level level;
 	
 	enum Keys {
 		LEFT, RIGHT, UP, DOWN
 	}
 	
 	static Map<Keys, Boolean> keys = new HashMap<LevelController.Keys, Boolean>();
 	static {
 		keys.put(Keys.LEFT, false);
 		keys.put(Keys.RIGHT, false);
 		keys.put(Keys.UP, false);
 		keys.put(Keys.DOWN, false);
 	};
 	
 	public LevelController(Level level){
 		this.level = level;
 		
 	}
 	
 	// Main update method
	public void update(){
 		// process
 		processInput();
 		// do something
 	}
 	
 	
 	private void processInput(){
 		// TODO
 	}
 	
 	
 	// Keypresses
 	public void leftPressed() {
 		keys.get(keys.put(Keys.LEFT, true));
 	}
 	
 	public void leftReleased() {
 		keys.get(keys.put(Keys.LEFT, false));
 	}
 	
 	public void rightPressed() {
 		keys.get(keys.put(Keys.RIGHT, true));
 	}
 	
 	public void rightReleased() {
 		keys.get(keys.put(Keys.RIGHT, false));
 	}
 	
 	public void upPressed() {
 		keys.get(keys.put(Keys.UP, true));
 	}
 	
 	public void upReleased() {
 		keys.get(keys.put(Keys.UP, false));
 	}
 	
 	public void downPressed() {
 		keys.get(keys.put(Keys.DOWN, true));
 	}
 	
 	public void downReleased() {
 		keys.get(keys.put(Keys.DOWN, false));
 	}
 	
 	
 }
