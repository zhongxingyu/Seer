 package com.dat255_group3.view;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 import com.badlogic.gdx.physics.box2d.Body;
 
 /** A view class for the InGame model. 
  * @author The Hans-Gunnar Crew
  *
  */
 public class InGameView {
 	
 	private OrthogonalTiledMapRenderer mapRenderer;
 	private TiledMap map;
 	private OrthographicCamera camera; 
 	
 
 	/** A constructor that takes a map. 
 	 * @param map
 	 */
 	
 	public InGameView (TiledMap map, OrthographicCamera camera) {
 		this.map = map;
 		mapRenderer = new OrthogonalTiledMapRenderer(map);
 		this.camera = camera;
 		
 	}
 	
 	
 	
 	/** Renders the HUD and background of the game. 
 	 * 
 	 */
 	public void draw(WorldView worldView, Body gBody, Body charBody, CharacterView charView) {
		worldView.draw(gBody, charBody, charView);
		
 		//Shows selected part of the map
 		mapRenderer.setView(camera);
 		mapRenderer.render();
		
 		
 		//Skota layouts = lyssnar av olika touch -> 
 		
 		//If: Vinna -> visa vinna.
 	
 	}
 	/*
 	/** Does nothing right now.
 	 * 
 	 
 	public void drawJump(){
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 	*/
 
 }
