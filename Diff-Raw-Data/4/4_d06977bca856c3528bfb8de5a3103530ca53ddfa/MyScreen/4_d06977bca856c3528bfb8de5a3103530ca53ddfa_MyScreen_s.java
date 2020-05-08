 package com.me.mygdxgame;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector3;
 
 public class MyScreen implements Screen{
 	protected MyGdxGame game;
 	protected SpriteBatch spriteBatch;
 	/*
 	 * Assets
 	 * 
 	 * Assets are things such as Images, music
 	 * 
 	 * All assets are stored in the Android project folder under assets
 	 */
 	SplashScreen splash;
 	// Textures
 	protected Texture floor;
 	protected Texture background;
 
 	// Music
 	protected Music music1;
 	protected Music music2;
 
 	// Sound
 
 	/*
 	 * End Assets
 	 */
 
 	
 
 	// Fonts
 	protected BitmapFont font;
 
 	// This will determine what we see on the screen and also pans the view
 	protected OrthographicCamera camera;
 
 	// Used to render sprites
 	protected SpriteBatch batch;
 
 	// Used to determine where mouse/finger is pressed
 	protected Vector3 touchPos = new Vector3();
 
 	/*
 	 * In game objects
 	 */
 	protected ArrayList<Obstacle> obstacles;
 	protected Runner runner;
 	/*
 	 * End in game objects
 	 */
 
 	// Used as a timer for spawning obstacles
 	protected long lastSpawnTime;
 	protected float lastSpawnPos;
 	protected float spawnPositionY;
 	protected int spawnPositionRandom;
 
 	// Current game timer
 	protected float timer;
 
 	// Current game score
 	protected int score;
 
 	// Resolution Width
 	public final int RESW = 800;
 	// Resolution Height
 	public final int RESH = 480;
 
 	// protected boolean played;
 
 	// Position of the floor
 	protected final float FLOOR_HEIGHT = 30;
 	protected float floorPosX[];
 	// Position of background
 	protected float backgroundPosX[];	
 
 	/*
 	 * The asset manager requires you to type more code, but allows you to
 	 * prevent crashes caused by trying to use an asset before it's fully
 	 * loaded.
 	 */
 	protected AssetManager manager = new AssetManager();
 
 	MyScreen(MyGdxGame g) {
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, RESW, RESH);
 		spriteBatch = new SpriteBatch();
 		game = g;
 	}
 	
	protected void create(){
	
	};
	
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		
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
 		
 	}
 
 }
