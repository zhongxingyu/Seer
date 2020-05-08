 package com.secondhand.controller;
 
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.Universe;
 import com.secondhand.opengl.Circle;
 import com.secondhand.scene.GamePlayScene;
 
 public class GamePlaySceneController {
 
 	// View
 	private GamePlayScene scene;
 	private GameSceneTouchListener sceneListener;
 	
 	// Model
	private Universe universe;
 	
 	// Player sprite
 	private Circle player;
 	
 	public GamePlaySceneController(Scene gamePlayScene) {
 		scene = (GamePlayScene) gamePlayScene;
 		scene.registerUpdateHandler(universe.getLevel().getPhysics());
 		sceneListener = new GameSceneTouchListener();
 		scene.setOnSceneTouchListener(sceneListener);
		
		universe = Universe.getInstance();
 		player = new Circle(0, 0, universe.getLevel().getPlayer().getRadius());
 		
 		scene.attachChild(player);
 		universe.setSprite(player);
 	}
 	
 	private class GameSceneTouchListener implements IOnSceneTouchListener{
 		@Override
 		public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 			float x = pSceneTouchEvent.getX();
 			float y = pSceneTouchEvent.getY();
 			universe.update(new Vector2(x, y));
 			return true;
 		}
 	}
 }
