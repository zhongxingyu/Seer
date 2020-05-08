 package com.tacoid.puyopuyo;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.tacoid.puyopuyo.PreferenceManager.Preference;
 import com.tacoid.puyopuyo.screens.GameSoloScreen;
 import com.tacoid.puyopuyo.screens.GameTimeAttackScreen;
 import com.tacoid.puyopuyo.screens.GameVersusScreen;
 import com.tacoid.puyopuyo.screens.LanguageScreen;
 import com.tacoid.puyopuyo.screens.LoadingScreen;
 import com.tacoid.puyopuyo.screens.MainMenuScreen;
 
 public class PuyoPuyo extends Game {
 	
 	private IActivityRequestHandler myRequestHandler;
 
 	private static PuyoPuyo instance = null;
 	public AssetManager manager;
 	public TextureAtlas atlasPuyo;
 	public TextureAtlas atlasControls;
 	public TextureAtlas atlasPlank;
 	public TextureAtlas atlasPanelsLandscape;
 	public TextureAtlas atlasPanelsPortrait;
 	public TextureAtlas atlasBouttons;
 	private boolean desktopMode;
 	public static final int VIRTUAL_WIDTH = 1280;
 	public static final int VIRTUAL_HEIGHT = 768;
 	private LoadingScreen loadingScreen;
 	private boolean loaded = false;
 	private boolean justLaunched = true;
 	
 	public enum ScreenOrientation {
 		LANDSCAPE,
 		PORTRAIT
 	};
 	
 	private PuyoPuyo() {}
 	
 	public static void setHandler(IActivityRequestHandler handler) {
 		getInstance().myRequestHandler = handler;
 	}
 	
 	public static PuyoPuyo getInstance() {
 
 		if (instance == null) {
 			instance = new PuyoPuyo();
 		}
 		return instance;
 	}
 	
 	public IActivityRequestHandler getHandler() {
 		return myRequestHandler;
 	}
 	
 	public void render() {
 		/* Si update renvoi true, c'est que tout est chargé, on a plus qu'à afficher le screen qu'on veut. Sinon, on affiche le screen de chargement */
 		if (manager.update()) {
 			if (justLaunched) {
 				String language = PreferenceManager.getInstance().getPreference(Preference.LANGUAGE);
				MusicPlayer.getInstance().init();
				SoundPlayer.getInstance().init();
 				if(I18nManager.getInstance().setLanguage(language)) {
 					loadLocalizedAssets();
 					if (getScreen() == null) {
 						setScreen(MainMenuScreen.getInstance());
 						myRequestHandler.showAds(true);
 					} else {
 						getScreen().show();
 					}
 				} else {
 					setScreen(LanguageScreen.getInstance());
 				}
 				justLaunched = false;
 			} else if (!loaded){
 				getScreen().show();
 			}
 			loaded = true;
 			super.render();
 		} else {
 			if (loadingScreen != null) loadingScreen.render(Gdx.graphics.getDeltaTime());
 			loaded = false;
 		}
 	}
 	
 	@Override
 	public void create() {		
 		loadingScreen = LoadingScreen.getInstance();
 		manager = new AssetManager();
 	
 		Gdx.input.setCatchBackKey(true);
 		
 		loadAssets();
 		
 		myRequestHandler.showAds(false);
 
         justLaunched = true;
 	}
 	
 	private void loadAssets() {
 
 		/* fonts */
 		manager.load("images/font_score.fnt", BitmapFont.class);
 		manager.load("images/font_level.fnt", BitmapFont.class);
 		
 		/* Textures du menu */
 		manager.load("images/menu/flag-fr.png", Texture.class);
 		manager.load("images/menu/flag-en.png", Texture.class);
 		manager.load("images/menu/sky.png", Texture.class);
 		manager.load("images/menu/sky-portrait.png", Texture.class);
 		manager.load("images/menu/hills.png", Texture.class);
 		manager.load("images/menu/logo.png", Texture.class);
 		
 		atlasPuyo = new TextureAtlas(Gdx.files.internal("images/puyos/pages.atlas"));
 		atlasControls = new TextureAtlas(Gdx.files.internal("images/controls/pages.atlas"));
 		atlasPanelsLandscape = new TextureAtlas(Gdx.files.internal("images/panels/landscape/pages.atlas"));
 		atlasPanelsPortrait = new TextureAtlas(Gdx.files.internal("images/panels/portrait/pages.atlas"));
 		atlasBouttons = new TextureAtlas(Gdx.files.internal("images/bouttons/pages.atlas"));
 		
 		/*** Son ***/
 		manager.load("sounds/bleep.wav", Sound.class);
 		manager.load("sounds/bleep2.wav", Sound.class);
 		manager.load("sounds/explode.wav", Sound.class);
 		manager.load("sounds/click.wav", Sound.class);
 		manager.load("sounds/nuisance.wav", Sound.class);
 		
 		manager.load("sounds/AnoyingMusic.mp3", Music.class);
 		
 	}
 	static int i = 0;
 
 	public void loadLocalizedAssets() {
 
 		atlasPlank = new TextureAtlas(Gdx.files.internal("images/menu/plank-" + I18nManager.getInstance().getLanguage().toString() + "/pages.atlas"));
 		MainMenuScreen.getInstance().init();
 		GameVersusScreen.getInstance().initGraphics();
 		GameSoloScreen.getInstance().initGraphics();
 		GameTimeAttackScreen.getInstance().initGraphics();
 	}
 
 	public void setDesktopMode(boolean b) {
 		this.desktopMode = b;
 	}
 	
 	public boolean getDesktopMode() {
 		return this.desktopMode;
 	}
 }
