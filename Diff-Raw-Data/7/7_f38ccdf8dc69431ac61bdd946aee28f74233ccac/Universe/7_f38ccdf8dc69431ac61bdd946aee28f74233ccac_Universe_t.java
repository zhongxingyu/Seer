 package com.secondhand.model;
 
 import org.anddev.andengine.engine.Engine;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.physics.PhysicsDestroyer;
 
 /**
  * Singelton class for describing the universe.
  */
 public final class Universe {
 	private Level currentLevel;
 
 	private static Universe instance;
 
 	private Universe() {
 	}
 
 	public void initialize(final Engine engine) {
 		nextLevel();
 		PhysicsDestroyer.getInstance().initialize(engine, currentLevel.getPhysicsWorld());
 	}
 
 	public static Universe getInstance() {
 		if (instance == null) {
 			instance = new Universe();
 		}
 		return instance;
 	}
 	
 	public boolean isGameOver() {
 		return this.currentLevel.isGameOver();
 	}
 
 	public Level getLevel() {
 		return currentLevel;
 	}
 
 	public void nextLevel() {
 		if (currentLevel == null) {
 			currentLevel = new Level(2);
 		} else if (!this.currentLevel.isGameOver()) {
 			currentLevel = new Level(currentLevel.getLevelNumber());
 		} else {
 			currentLevel = new Level();
 		}
 	}
 
 	public void onManagedUpdate(final float pSecondsElapsed) {
 		if (currentLevel.checkPlayerBigEnough()) {
 			nextLevel();
 		} else {
 			currentLevel.onManagedUpdate(pSecondsElapsed);
 		}
 	}
 
 	public void update(final Vector2 v) {
 		if (currentLevel.checkPlayerBigEnough()) {
 			nextLevel();
 		} else {
 			currentLevel.sendTouchInput(v);
 		}
 	}
 }
