 package com.me.Roguish.Controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.me.Roguish.Model.Entity;
 import com.me.Roguish.Model.HeroUnit;
 import com.me.Roguish.Model.Level;
 
 public class LevelController {
 	private Level level;
 	private Entity hero;
 	
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
 		this.hero = level.getHero();
 		
 	}
 	
 	// Main update method
 	public void update(float delta){
 		// process
 		processInput();
 		// do something
 	}
 	
 	
 	private void processInput(){
 		if (keys.get(Keys.LEFT)){
 			
 		}
 		if (keys.get(Keys.RIGHT)){
 		}
 		if (keys.get(Keys.DOWN)){
 			
 		}
 		if (keys.get(Keys.UP)){
 		}
 	}
 	
 	
 	// Keypresses
 	public void leftPressed() {
 		keys.get(keys.put(Keys.LEFT, true));
 	}
 	
 	public void leftReleased() {
 		keys.get(keys.put(Keys.LEFT, false));
 		if(hero.getX() > 0)
 			hero.movePosition(-1, 0);
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	public void rightPressed() {
 		keys.get(keys.put(Keys.RIGHT, true));
 	}
 	
 	public void rightReleased() {
 		keys.get(keys.put(Keys.RIGHT, false));
 		if(hero.getX() < 9)
 			hero.movePosition(1 , 0);
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	public void upPressed() {
 		keys.get(keys.put(Keys.UP, true));
 	}
 	
 	public void upReleased() {
 		keys.get(keys.put(Keys.UP, false));
 		if(hero.getY() > 0)
			hero.movePosition(0, -1);
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	public void downPressed() {
 		keys.get(keys.put(Keys.DOWN, true));
 	}
 	
 	public void downReleased() {
 		keys.get(keys.put(Keys.DOWN, false));
 		if(hero.getY() < 14)
			hero.movePosition(0, 1);
 		System.out.println(level.getTile(hero.getX(), hero.getY()));
 	}
 	
 	
 }
