 package com.example.forestgame;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import android.util.DisplayMetrics;
 import android.util.Log;
 
 public class MainActivity extends SimpleBaseGameActivity {
 
     private static int CAMERA_WIDTH = 800;
     private static int CAMERA_HEIGHT = 1280;
     private int whichButton = 0;
     private static Camera camera;
     private static Scene mainScene;
     private TextureRegion textureBackground;
     private TextureRegion textureTitle;
     private TextureRegion texturePlay;
     private TextureRegion textureScores;
     private TextureRegion textureCredits;
     private TextureRegion textureExit;
     private BitmapTextureAtlas backgroundTexture;
     private BitmapTextureAtlas titleTexture;
     private BitmapTextureAtlas playTexture;
     private BitmapTextureAtlas scoresTexture;
     private BitmapTextureAtlas creditsTexture;
     private BitmapTextureAtlas exitTexture;
     private boolean mToggleBox = true;
 
     @Override
     public EngineOptions onCreateEngineOptions() {
 	
 	DisplayMetrics dm = new DisplayMetrics();
 	getWindowManager().getDefaultDisplay().getMetrics(dm);
 	
 	camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 	
 	final EngineOptions options = new EngineOptions(true,
 		ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 	return options;
     }
 
     @Override
     protected void onCreateResources() {
 	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("main_menu/");
 
 	backgroundTexture = new BitmapTextureAtlas(new TextureManager(), 1024,
 		2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	titleTexture = new BitmapTextureAtlas(new TextureManager(), 1024, 512,
 		TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	playTexture = new BitmapTextureAtlas(new TextureManager(), 1024, 512,
 		TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	scoresTexture = new BitmapTextureAtlas(new TextureManager(), 1024, 512,
 		TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	creditsTexture = new BitmapTextureAtlas(new TextureManager(), 1024, 512,
 		TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	exitTexture = new BitmapTextureAtlas(new TextureManager(), 1024, 512,
 		TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 	textureBackground = BitmapTextureAtlasTextureRegionFactory
 		.createFromAsset(this.backgroundTexture, this,
 			"background.jpg", 0, 0);
 	textureTitle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 		this.titleTexture, this, "main_menu_title.png", 0, 0);
 	texturePlay = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 		this.playTexture, this, "menu_play.png", 0, 0);
 	textureScores = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 		this.scoresTexture, this, "menu_scores.png", 0, 0);
 	textureCredits = BitmapTextureAtlasTextureRegionFactory
 		.createFromAsset(this.creditsTexture, this, "menu_credits.png",
 			0, 0);
 	textureExit = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 		this.exitTexture, this, "menu_exit.png", 0, 0);
 
 	this.mEngine.getTextureManager().loadTexture(this.backgroundTexture);
 	this.mEngine.getTextureManager().loadTexture(this.titleTexture);
 	this.mEngine.getTextureManager().loadTexture(this.playTexture);
 	this.mEngine.getTextureManager().loadTexture(this.scoresTexture);
 	this.mEngine.getTextureManager().loadTexture(this.creditsTexture);
 	this.mEngine.getTextureManager().loadTexture(this.exitTexture);
     }
 
     @Override
     protected Scene onCreateScene() {
 	this.mEngine.registerUpdateHandler(new FPSLogger());
 	mainScene = new Scene();
 	Sprite sprite = new Sprite(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT,
 		textureBackground, new VertexBufferObjectManager());
 	SpriteBackground spriteBackground = new SpriteBackground(sprite);
 
 	Sprite Title = new Sprite(CAMERA_WIDTH / 8, CAMERA_HEIGHT / 16,
 		CAMERA_WIDTH * 6 / 8, CAMERA_HEIGHT / 4, textureTitle,
 		new VertexBufferObjectManager());
 
 	Sprite ButtonPlay = new Sprite(CAMERA_WIDTH / 4,
 		CAMERA_HEIGHT * 52 / 128, CAMERA_WIDTH * 2 / 4,
 		CAMERA_HEIGHT * 12 / 128, texturePlay,
 		this.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 		    float pTouchAreaLocalX, float pTouchAreaLocalY) {
 
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonPlay", "touch");
 		    whichButton = 1;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()+0.1));
 		    this.setScaleY((float) (this.getScaleY()+0.1));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonPlay", "no touch");
 		    whichButton = 1;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()-0.1));
 		    this.setScaleY((float) (this.getScaleY()-0.1));
		}
 		return true;
 	    }
 	};
 	Sprite ButtonScores = new Sprite(CAMERA_WIDTH / 4,
 		CAMERA_HEIGHT * 69 / 128, CAMERA_WIDTH * 2 / 4,
 		CAMERA_HEIGHT * 12 / 128, textureScores,
 		this.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 		    float pTouchAreaLocalX, float pTouchAreaLocalY) {
 
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonScores", "touch");
 		    whichButton = 2;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()+0.1));
 		    this.setScaleY((float) (this.getScaleY()+0.1));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonScores", "no touch");
 		    whichButton = 2;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()-0.1));
 		    this.setScaleY((float) (this.getScaleY()-0.1));
 		}
 		return true;
 	    }
 	};
 
 	Sprite ButtonCredits = new Sprite(CAMERA_WIDTH / 4,
 		CAMERA_HEIGHT * 86 / 128, CAMERA_WIDTH * 2 / 4,
 		CAMERA_HEIGHT * 12 / 128, textureCredits,
 		this.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 		    float pTouchAreaLocalX, float pTouchAreaLocalY) {
 
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonCredits", "touch");
 		    whichButton = 3;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()+0.1));
 		    this.setScaleY((float) (this.getScaleY()+0.1));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonCredits", "no touch");
 		    whichButton = 3;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()-0.1));
 		    this.setScaleY((float) (this.getScaleY()-0.1));
 		}
 		return true;
 	    }
 	};
 
 	Sprite ButtonExit = new Sprite(CAMERA_WIDTH / 4,
 		CAMERA_HEIGHT * 103 / 128, CAMERA_WIDTH * 2 / 4,
 		CAMERA_HEIGHT * 12 / 128, textureExit,
 		this.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 		    float pTouchAreaLocalX, float pTouchAreaLocalY) {
 
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonExit", "touch");
 		    whichButton = 4;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()+0.1));
 		    this.setScaleY((float) (this.getScaleY()+0.1));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonExit", "no touch");
 		    whichButton = 4;
 		    MainActivity.this.toggle();
 		    this.setScaleX((float) (this.getScaleX()-0.1));
 		    this.setScaleY((float) (this.getScaleY()-0.1));
 		}
 		return true;
 	    }
 	};
 	mainScene.setBackground(spriteBackground);
 	MainActivity.mainScene.attachChild(Title);
 	MainActivity.mainScene.attachChild(ButtonPlay);
 	MainActivity.mainScene.attachChild(ButtonScores);
 	MainActivity.mainScene.attachChild(ButtonCredits);
 	MainActivity.mainScene.attachChild(ButtonExit);
 	MainActivity.mainScene.registerTouchArea(ButtonPlay);
 	MainActivity.mainScene.registerTouchArea(ButtonScores);
 	MainActivity.mainScene.registerTouchArea(ButtonCredits);
 	MainActivity.mainScene.registerTouchArea(ButtonExit);
	MainActivity.mainScene.setTouchAreaBindingOnActionDownEnabled(true);
	MainActivity.mainScene.setTouchAreaBindingOnActionMoveEnabled(true);
 
 	return mainScene;
     }
     
     private void toggle() {
 	switch (whichButton) {
 	case 1: this.playTexture.clearTextureAtlasSources();
 	this.mToggleBox = !this.mToggleBox;
 	BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.playTexture, this, this.mToggleBox ? "menu_play.png" : "menu_play_light.png", 0, 0);
 	break;
 	case 2: this.scoresTexture.clearTextureAtlasSources();
 	this.mToggleBox = !this.mToggleBox;
 	BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.scoresTexture, this, this.mToggleBox ? "menu_scores.png" : "menu_scores_light.png", 0, 0);
 	break;
 	case 3: this.creditsTexture.clearTextureAtlasSources();
 	this.mToggleBox = !this.mToggleBox;
 	BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.creditsTexture, this, this.mToggleBox ? "menu_credits.png" : "menu_credits_light.png", 0, 0);
 	break;
 	case 4: this.exitTexture.clearTextureAtlasSources();
 	this.mToggleBox = !this.mToggleBox;
 	BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.exitTexture, this, this.mToggleBox ? "menu_exit.png" : "menu_exit_light.png", 0, 0);
 	break;
 	}
 	
 }
 
 }
