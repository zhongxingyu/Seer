 package com.me.mygdxgame.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen; 
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 
 public class GameScreen implements Screen {
 
 	private TiledMap map;
 	private IsometricTiledMapRenderer renderer;
 	private OrthographicCamera camera;
 	
 	@Override
 	public void render(float delta) {
 
 		Gdx.gl.glClearColor(0f, 1f, 1f, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		renderer.setView(camera);
 		renderer.render();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 
 		camera.viewportWidth = width;
 		camera.viewportHeight = height;
 		camera.update();
 	}
 
 	@Override
 	public void show() {
 
 		TmxMapLoader loader = new TmxMapLoader();
 
 		map = loader.load("maps/isometric.tmx");
 		renderer = new IsometricTiledMapRenderer(map);
 		camera = new OrthographicCamera();
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 
 		map.dispose();
 		renderer.dispose();
 	}
 
 }
