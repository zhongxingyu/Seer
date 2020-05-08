 package com.example.forestgame;
 
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.opengl.font.StrokeFont;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.color.Color;
 
 import android.graphics.Typeface;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 
 import com.example.forestgame.altasstorage.AtlasStorage;
 
 public class MainActivity extends SimpleBaseGameActivity {
 
     public static int CAMERA_WIDTH;
     public static int CAMERA_HEIGHT;
     private static float DISPLAY_WIDTH;
     private static float DISPLAY_HEIGHT;
     public static float TEXTURE_WIDTH = 1250;
     public static float TEXTURE_HEIGHT = 2000;
     private float ratio_width;
     private float ratio_height;
     public float ZM;  //zoom factor
     
     public static ZoomCamera camera;  //made public modifier camera so that it can be accessed from GameScene
     
     private boolean gameLoaded = false; // flag of game loading state
     
     public static MainActivity mainActivity;
     
     private static MainScene mainScene;
     public TextureRegion textureBackground;
     public TextureRegion textureSlots;
     public TextureRegion textureTitle;
     public TextureRegion texturePlay;
     public TextureRegion textureScores;
     public TextureRegion textureCredits;
     public TextureRegion textureExit;
     public AtlasStorage storage;
     public StrokeFont tDevNames;
     public StrokeFont tCaptions;
     private static ITexture creditsCaps;
     private static ITexture creditsNames;
     public StrokeFont tQuestion;
     public StrokeFont tChoiseYES;
     public StrokeFont tChoiseNO;
     private static ITexture pauseQuestion;
     private static ITexture pauseChoiseY;
     private static ITexture pauseChoiseN;
     
 
     @Override
     public EngineOptions onCreateEngineOptions() {
 	
 	mainActivity = this;
 	
 	DisplayMetrics dm = new DisplayMetrics();
 	getWindowManager().getDefaultDisplay().getMetrics(dm);
 	
 	DISPLAY_WIDTH = dm.widthPixels;
 	DISPLAY_HEIGHT = dm.heightPixels;
 	
 	ratio_width = TEXTURE_WIDTH / DISPLAY_WIDTH;
 	ratio_height = TEXTURE_HEIGHT / DISPLAY_HEIGHT;
 		
 	CAMERA_WIDTH = (int) (DISPLAY_WIDTH);
 	CAMERA_HEIGHT = (int) (DISPLAY_HEIGHT);
 	
 	ratio_width = TEXTURE_WIDTH / DISPLAY_WIDTH;
 	ratio_height = TEXTURE_HEIGHT / DISPLAY_HEIGHT;
 	
 	if (ratio_height < ratio_width) {
 	    ZM = 1 / ratio_height;
 	} else if (ratio_height > ratio_width) {
 	    ZM = 1 / ratio_width;
 	} else {
 	    ZM = 1;
 	}
 	
 	camera = new ZoomCamera( 0
 			       , 0
 			       , CAMERA_WIDTH
 			       , CAMERA_HEIGHT);
 	//mHUD = new HUD();
 	//camera.setHUD(mHUD);
 	camera.setCenter( TEXTURE_WIDTH / 2
 			, TEXTURE_HEIGHT / 2);
 	
 	camera.setZoomFactor(ZM * 1f);
 	final EngineOptions options = new EngineOptions( true
 						       , ScreenOrientation.PORTRAIT_FIXED
 						       , new RatioResolutionPolicy( CAMERA_WIDTH
 							                          , CAMERA_HEIGHT)
 						       , camera);
 	int interval = 2;
         options.getTouchOptions().setTouchEventIntervalMilliseconds(interval);
 	return options;
     }
 
     @Override
     protected void onCreateResources() {
 	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("main_menu/");
 	
 	storage = new AtlasStorage();
 	storage.createAtlas( this.getTextureManager()
 			   , this, "main_menu/"
 			   , "main_menu_title.png"
 			   , "menu_play.png"
 			   , "menu_play_light.png"
 			   , "menu_scores.png"
 			   , "menu_scores_light.png"
 			   , "menu_credits.png"
 			   , "menu_credits_light.png"
 			   , "menu_exit.png"
 			   , "menu_exit_light.png");
 	
 	
 	textureTitle = storage.getTexture("main_menu_title.png");
 	texturePlay = storage.getTexture("menu_play.png");
 	textureScores = storage.getTexture("menu_scores.png");
 	textureCredits = storage.getTexture("menu_credits.png");
 	textureExit = storage.getTexture("menu_exit.png");
 	
 	storage.createAtlas( this.getTextureManager()
 	           	   , this
 	           	   , "main_menu/"
 	           	   , "background.jpg");
 	textureBackground = storage.getTexture("background.jpg");
 	
 	storage.createAtlas( this.getTextureManager()
 		 	   , this
 		 	   , "game_scene_gfx/"
 		 	   , "gfx_slots.png");
         textureSlots = storage.getTexture("gfx_slots.png");
 	
 	storage.createAtlas( this.getTextureManager()
 			   , this
 			   , "game_scene_gfx/"
 			   , "gfx_crown.png"
 			   , "gfx_golden_nut.png"
 			   , "gfx_nut.png"
 			   , "gfx_grass.png"
 			   , "gfx_tree.png"
 			   , "gfx_nuts_king.png"
 			   , "gfx_squirrel.png"
 			   , "gfx_empty.png");
 	
 	creditsCaps = new BitmapTextureAtlas(this.getTextureManager()
 		, 2048
 		, 1024
 		, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	creditsNames = new BitmapTextureAtlas(this.getTextureManager()
 		, 2048
 		, 2048
 		, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	tCaptions = new StrokeFont(this.getFontManager()
 		, creditsCaps
 		, Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
 		, 100
 		, true
 		, new Color(1.0f, 0.6f, 0.0f)
 		, 2
 		, new Color(1.0f, 0.2f, 0.0f));
 	tDevNames = new StrokeFont(this.getFontManager()
 		, creditsNames
 		, Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
 		, 100
 		, true
 		, new Color(1.0f, 1.0f, 1.0f)
 		, 2
 		, new Color(1.0f, 0.2f, 0.0f));
 	
 	pauseQuestion = new BitmapTextureAtlas(this.getTextureManager()
 		, 2048
 		, 2048
 		, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	pauseChoiseY = new BitmapTextureAtlas(this.getTextureManager()
 		, 512
 		, 512
 		, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	pauseChoiseN = new BitmapTextureAtlas(this.getTextureManager()
 		, 512
 		, 512
 		, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	tQuestion = new StrokeFont(this.getFontManager()
 		, pauseQuestion
 		, Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
 		, 100
 		, true
 		, new Color(1.0f, 0.6f, 0.0f)
 		, 2
 		, new Color(1.0f, 0.2f, 0.0f));
 	tChoiseYES = new StrokeFont(this.getFontManager()
 		, pauseChoiseY
 		, Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
 		, 170
 		, true
 		, new Color(1.0f, 1.0f, 1.0f)
 		, 2
 		, new Color(1.0f, 0.2f, 0.0f));
 	tChoiseNO = new StrokeFont(this.getFontManager()
 		, pauseChoiseN
 		, Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
 		, 170
 		, true
 		, new Color(1.0f, 1.0f, 1.0f)
 		, 2
 		, new Color(1.0f, 0.2f, 0.0f));
 	
 	tDevNames.load();
 	tCaptions.load();
 	tQuestion.load();
 	tChoiseYES.load();
 	tChoiseNO.load();
     }
 
     @Override
     protected Scene onCreateScene() {
 	    mainScene = new MainScene();
 	    gameLoaded = true;
 	    return mainScene;
 	}
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 	if(keyCode == KeyEvent.KEYCODE_BACK) {
 	  
 	    if(!gameLoaded) {
 	      return true;
 	    }
 	  
 	  if(mainScene != null && gameLoaded) {
 	      mainScene.keyPressed(keyCode, event);
 	      return true;
 	  }
 	  return true;
 	}
 	return super.onKeyDown(keyCode, event);
     }
     
     @Override
     protected void onDestroy() {
 	super.onDestroy();
 	android.os.Process.killProcess(android.os.Process.myPid());
     }
 }
