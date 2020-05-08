 package com.gozdy.HookIT;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector3;
 
 public class MainMenuScreen implements Screen {
 
 	public static final float WORLD_WIDTH = 10;
 	public static final float WORLD_HEIGHT = 15;
 	final HookGame game;
 	OrthographicCamera camera;
 	Texture playImage;
 	Sprite playSprite;
 
 
 	
 	public MainMenuScreen(final HookGame hookGame) {
 			game = hookGame;
 			camera = new OrthographicCamera();
 			camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
 			
 			playImage = new Texture(Gdx.files.internal("playGame.png"));
 			playSprite = new Sprite(playImage);
 			playSprite.setSize(4, 1);
 			playSprite.setPosition(WORLD_WIDTH/2-playSprite.getWidth()/2, WORLD_HEIGHT/2);
 			
 			
 			
 	}
 
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		camera.update();
 		
 		game.batch.setProjectionMatrix(camera.combined);
 		
 		
 		game.batch.begin();
 //		game.font.scale(5);
 		playSprite.draw(game.batch);		
 		game.batch.end();
 		
 		if(Gdx.input.isTouched()){
 			Vector3 touchPos = new Vector3();
             touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
             camera.unproject(touchPos);
             
             if(playSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)){
     			game.setScreen(new GameScreen(game));
 
             }
 			dispose();
 		}
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
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
 		// TODO Auto-generated method stub
 playImage.dispose();
 	}
 
 }
