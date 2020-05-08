 package net.castegaming.game;
 
 import net.castegaming.game.terrain.Terrain;
 import android.gameengine.icadroids.engine.GameEngine;
 import android.gameengine.icadroids.tiles.GameTiles;
 import android.graphics.Color;
 
 public class Diggerload extends GameEngine {
 	
 	Miner m;
 	
 	@Override
 	protected void initialize() {
 		super.initialize();
 		createTileEnvironment();
 	}
 	
 	@Override
 	public void update() {
 		super.update();
 		setBackgroundColor(Color.BLACK);
 		setScreenLandscape(false);
 	}
 	
 	 /**
      * Create background with tiles
      */
     private void createTileEnvironment() {
 		String[] tileImagesNames = { "dirt", "stone", "iron", "grass" };
 		
 		Terrain.createFiles();
		GameTiles myTiles = new GameTiles(tileImagesNames, Terrain.getTileMap(1, 1, 10, 10), 32);
 		setTileMap(myTiles);
     }
 }
