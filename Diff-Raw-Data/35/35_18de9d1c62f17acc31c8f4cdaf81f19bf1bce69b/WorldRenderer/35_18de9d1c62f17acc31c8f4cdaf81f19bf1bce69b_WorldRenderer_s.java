 package com.devcamp.cadejo.world;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.utils.Array;
 import com.devcamp.cadejo.ScoreManager;
 import com.devcamp.cadejo.actors.Background;
 import com.devcamp.cadejo.actors.Cadejo;
 import com.devcamp.cadejo.actors.Character;
 import com.devcamp.cadejo.actors.Character.State;
 import com.devcamp.cadejo.actors.Floor;
 import com.devcamp.cadejo.actors.Obstacle;
 
 public class WorldRenderer {
 
 	public static final float CAMERA_W = 20f;
 	public static final float CAMERA_H = 14f;
 
 	private static final float RUNNING_FRAME_DURATION = 0.1f;
 	public static final int CANT_IMGS_CORRER = 8;
 	public static final int CANT_IMGS_SALTAR = 18;
 
 	private World world;
 	private OrthographicCamera cam;
 
 	ShapeRenderer debugRenderer = new ShapeRenderer();
 
	private Texture charTexture;
	private Texture cadejoTexture;
 	private Array<Texture> backgroundTextures;
	private Array<Texture> floorTextures;
 	private Array<Texture> obstacleTextures;
 
 	private SpriteBatch spriteBatch;
 	private boolean debug = false;
 
 	private int width;
 	private int height;
 
 	private float ppuX;
 	private float ppuY;
 
 	//Score
 	private ScoreManager mScoreManager;
 	private BitmapFont mScoreFont;
 
 	//Para la animacion
 	private Animation mRunAnimation;
 	private Animation mJumpAnimation;
 	private TextureRegion[] mRunFrames = new TextureRegion[8];
 	private TextureRegion mCharacterIdle;
 	private TextureRegion mCharacterJump;
 	private TextureRegion mCharacterFrame;
 
 	public WorldRenderer(World world, boolean debug, ScoreManager pScoreManager){
 		this.debug = debug;
 		sharedConstructor(world, pScoreManager);
 	}
 
 	public WorldRenderer(World world, ScoreManager pScoreManager){
 		sharedConstructor(world, pScoreManager);
 	}
 
 	public void sharedConstructor(World world, ScoreManager pScoreManager){
 		this.world = world;
 		this.cam = new OrthographicCamera(CAMERA_W, CAMERA_H);
 		this.cam.position.set(CAMERA_W/2f, CAMERA_H/2f, 0f);
 		this.cam.update();
 		spriteBatch = new SpriteBatch();
 
 		//Score
 		mScoreManager = pScoreManager;
 		mScoreFont = new BitmapFont();
 		mScoreFont.setColor(1f, 1f, 1f,1);
 
 		//
 		loadTextures();
 	}
 
 	public void loadTextures() {
		
		
 		backgroundTextures = new Array<Texture>();
 		backgroundTextures.add(new Texture(Gdx.files.internal("data/Background_1.png")));
		
 		/** Crea la animacion de correr*/
 		TextureAtlas atlas = new TextureAtlas("images/textures/correr.txt");
 		mCharacterIdle = atlas.getRegions().first();
 
 		TextureRegion[] runFrames = new TextureRegion[CANT_IMGS_CORRER];
 		int index = 0;
 		for(AtlasRegion region : atlas.getRegions())
 			runFrames[index++] = region;
		
 		mRunAnimation = new Animation(RUNNING_FRAME_DURATION, runFrames);	
		
 		/** Crea la animacion de saltar*/
 		TextureAtlas atlasBrinco = new TextureAtlas("images/textures/saltar.txt");
 		TextureRegion[] jumpFrames = new TextureRegion[CANT_IMGS_SALTAR];
 		mCharacterJump = atlasBrinco.getRegions().get(12);
 		index = 0;
 		for(AtlasRegion region : atlasBrinco.getRegions())
 			jumpFrames[index++] = region;
		
 		mJumpAnimation = new Animation(RUNNING_FRAME_DURATION, jumpFrames);
 	}
 
 	public void render(){
 		if(debug)
 			drawDebug();
 
 		//Abre el sprite batch
 		spriteBatch.begin();
 
 		//Dibuja todo lo necesario
 		drawBackground();
 		drawCharacter();
 		drawScore();
 
 		//Cierra el sprite batch
 		spriteBatch.end();
		
 		if(debug)
 			drawDebug();
 	}
 
 	private void drawScore()
 	{
		mScoreFont.setScale(2);
 		mScoreFont.draw(spriteBatch, "Score: " + mScoreManager.getScore() , 100, CAMERA_H*ppuY);
 	}
 
 	public void drawCharacter(){
 
 		Character mainCharacter = world.getMainCharacter();	
 		mCharacterFrame = mCharacterIdle;
 		if(mainCharacter.getState().equals(State.RUNNING)) {
 			mCharacterFrame = mRunAnimation.getKeyFrame(mainCharacter.getStateTime(), true);
 		}else if(mainCharacter.getState().equals(State.JUMPING)) {
 			mCharacterFrame = mCharacterJump;// mJumpAnimation.getKeyFrame(mainCharacter.getStateTime(), true);
 		}
 		spriteBatch.draw(mCharacterFrame, mainCharacter.getPosition().x * ppuX, mainCharacter.getPosition().y * ppuY, mainCharacter.SIZE_W * ppuX, mainCharacter.SIZE_H * ppuY);
 	}
 
 	public void drawCadejo(){
 		Cadejo cadejo = world.getCadejo();
 	}
 
 	public void drawObstacles(){
 		Array<Obstacle> obstacles = world.getObstacles();
 		for(Obstacle i : obstacles){
 			switch(i.getId()){
 			case 1:
 
 			}
 		}
 	}
 
 	public void drawBackground(){
 		Array<Background> backgrounds = world.getBackgrounds();
 		for(Background i : backgrounds){
 			spriteBatch.draw(backgroundTextures.get(i.getId()-1), i.getPosition().x * ppuX, i.getPosition().y * ppuY,
 					Background.SIZE_W * ppuX, Background.SIZE_H * ppuY);
 		}
 	}
 
 	public void drawFloor(){
 		Array<Floor> floors = world.getFloor();
 		for(Floor i : floors){
 			switch(i.getId()){
 			case 1:
 
 			}
 		}
 	}
 
 	public void drawDebug(){
 		debugRenderer.setProjectionMatrix(cam.combined);
 		debugRenderer.begin(ShapeType.Line);
 
 		Character mainCharacter = world.getMainCharacter();
 		Rectangle rect = mainCharacter.getBounds();
 		float x1 = mainCharacter.getPosition().x + rect.x;
 		float y1 = mainCharacter.getPosition().y + rect.y;
 		debugRenderer.setColor(new Color(0,1,0,1));
 		debugRenderer.rect(x1, y1, rect.width, rect.height);
 
 		Cadejo cadejo = world.getCadejo();
 		Rectangle rect3 = cadejo.getBounds();
 		float x3 = cadejo.getPosition().x + rect3.x;
 		float y3 = cadejo.getPosition().y + rect3.y;
 		debugRenderer.setColor(new Color(1,1,1,1));
 		debugRenderer.rect(x3, y3, rect3.width, rect3.height);
 
 		Array<Background> backgrounds = world.getBackgrounds();
 		for(Background i : backgrounds){
 			Rectangle rect2 = i.getBounds();
 			float x2 = i.getPosition().x + rect2.x;
 			float y2 = i.getPosition().y + rect2.y;
 			debugRenderer.setColor(new Color(0,0,1,1));
 			debugRenderer.rect(x2, y2, rect2.width, rect2.height);
 		}
 
 		Array<Floor> floor = world.getFloor();
 		for(Floor i : floor){
 			Rectangle rect2 = i.getBounds();
 			float x2 = i.getPosition().x + rect2.x;
 			float y2 = i.getPosition().y + rect2.y;
 			debugRenderer.setColor(new Color(1,0,0,1));
 			debugRenderer.rect(x2, y2, rect2.width, rect2.height);
 		}
 
 		Array<Obstacle> obstacles = world.getObstacles();
 		for(Obstacle i : obstacles){
 			Rectangle rect2 = i.getBounds();
 			float x2 = i.getPosition().x + rect2.x;
 			float y2 = i.getPosition().y + rect2.y;
 			debugRenderer.setColor(new Color(1,1,0,1));
 			debugRenderer.rect(x2, y2, rect2.width, rect2.height);
 		}
 
 		debugRenderer.end();
 
 	}
 
 
 	public void setSize(int w, int h){
 		this.width = w;
 		this.height = h;
 
 		ppuX = (float)width / CAMERA_W;
 		ppuY = (float)height / CAMERA_H;
 	}
 
 
 }
