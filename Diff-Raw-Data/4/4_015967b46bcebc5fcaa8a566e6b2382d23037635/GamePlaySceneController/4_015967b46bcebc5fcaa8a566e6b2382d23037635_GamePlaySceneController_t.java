 package com.secondhand.controller;
 
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.Universe;
 import com.secondhand.scene.GamePlayScene;
 
 public class GamePlaySceneController {
 
 	// View
 	private GamePlayScene scene;
 	private GameSceneTouchListener sceneListener;
 
 	// Model
 	private Universe universe = Universe.getInstance();
 
 	// Player sprite
 	// TODO: why do we need this here? Should not instead the GamePlayScene handle both
 	// setting the player and moving it?
 	private IShape player;
 
 	public GamePlaySceneController(Scene gamePlayScene) {
 		scene = (GamePlayScene) gamePlayScene;
 		scene.registerUpdateHandler(universe.getLevel().getPhysicsWorld());
 		sceneListener = new GameSceneTouchListener();
 		scene.setOnSceneTouchListener(sceneListener);
 
 		player = universe.getLevel().getPlayer().getShape();
 		
 		scene.setPlayer(player);
 		
 		// add the world bounds
		// you cant attachChild here!
 		for(Shape shape: universe.getLevel().getWorldBounds()) {
				//scene.attachChild(shape);
 		}
 	}
 
 	private class GameSceneTouchListener implements IOnSceneTouchListener {
 		@Override
 		public boolean onSceneTouchEvent(Scene pScene,
 				TouchEvent pSceneTouchEvent) {
 			float x = pSceneTouchEvent.getX();
 			float y = pSceneTouchEvent.getY();
 			universe.update(new Vector2(x, y));
 			return true;
 		}
 	}
 }
