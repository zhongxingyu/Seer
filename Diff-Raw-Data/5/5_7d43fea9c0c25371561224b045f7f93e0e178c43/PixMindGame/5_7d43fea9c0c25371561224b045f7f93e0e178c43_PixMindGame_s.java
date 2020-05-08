 package com.pix.mind;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.pix.mind.levels.FirstLevel;
 import com.pix.mind.levels.SecondLevel;
 import com.pix.mind.screens.InterLevelScreen;
 import com.pix.mind.screens.SplashScreen;
 
 public class PixMindGame extends Game {
 	private FirstLevel firstLevel;
 	private SecondLevel secondLevel;
 	private InterLevelScreen interLevel;
 	
 	private SplashScreen splashScreen;
 	public static final float WORLD_TO_BOX = 0.01f;
 	public static final float BOX_TO_WORLD = 100f;
 	public static float h = 480; 		
 	public static float w = 800;
 	private AssetManager assetManager;
 	private static Skin skin;
 	@Override
 	public void create() {
 		// TODO Auto-generated method stub
 		w = h * Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
 		assetManager = new AssetManager();
 		firstLevel = new FirstLevel(this);
 		secondLevel = new SecondLevel(this);
 		splashScreen = new SplashScreen(this);
 		this.setScreen(getSplashScreen());
 	}
 	
 	public FirstLevel getFirstLevel() {
 		return firstLevel;
 	}
 	
 	
 	public SplashScreen getSplashScreen() {
 		return splashScreen;
 	}
 
 	public void changeLevel(Screen screen){
 		interLevel = new InterLevelScreen(screen, this);
 		this.setScreen(interLevel);
 	}
 
 	public AssetManager getAssetManager() {
 		return assetManager;
 	}
 
 	public static Skin getSkin() {
 		return skin;
 	}
 
 	public void setSkin(Skin skin) {
 		PixMindGame.skin = skin;
 	}
 
 	public SecondLevel getSecondLevel() {
 		return secondLevel;
 	}
 	
 	
 }
