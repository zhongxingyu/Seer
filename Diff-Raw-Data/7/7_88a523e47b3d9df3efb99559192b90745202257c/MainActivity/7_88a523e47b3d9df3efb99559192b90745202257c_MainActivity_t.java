 package com.secondhand.controller;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.SmoothCamera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import android.view.KeyEvent;
 
 import com.secondhand.loader.FontLoader;
 import com.secondhand.loader.SoundLoader;
 import com.secondhand.loader.TextureRegionLoader;
 import com.secondhand.model.Universe;
 import com.secondhand.scene.IGameScene;
 import com.secondhand.twirl.GlobalResources;
 import com.secondhand.twirl.LocalizationStrings;
 
 public class MainActivity extends BaseGameActivity {
 
 	public static final int CAMERA_WIDTH = 800;
 	public static final int CAMERA_HEIGHT = 480;
 	

 	@Override
 	public void onLoadResources() {
 		// this is instead delegated to the separate scenes.
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		// the FPS logger is useful for optimizing performance.(the FPS is shown in LogCat)
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 		
 		return SceneManager.getInstance().setCurrentSceneEnum(IGameScene.AllScenes.LOADING_SCENE).getScene();				
 	}
 
 	@Override
 	public Engine onLoadEngine() {
 		
	    final SmoothCamera camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, Float.MAX_VALUE, Float.MAX_VALUE, 1.0f);
 	    
 	    final EngineOptions engineOptions = new EngineOptions(
 	    		true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 	    engineOptions.setNeedsSound(true);
 	    engineOptions.setNeedsMusic(true);
 	    final Engine engine = new Engine(engineOptions);
 	    
 	    // initialize loader classes:
 	    FontLoader.getInstance().initialize(this, engine);
 	    SoundLoader.getInstance().initialize(this, engine);
 	    TextureRegionLoader.getInstance().initialize(this, engine);
 	    // IMPORTANT: Uses TextureRegionLoader, so this line must be executed after above line always
 		GlobalResources.getInstance().load();
 		
 	    LocalizationStrings.getInstance().initialize(this);
 	
 	    SceneManager.getInstance().initialize(engine, this);
 	    
 	    Universe.getInstance().setEngine(engine);
 	    
 	     return engine;	
 	}
 
 	@Override
 	public void onLoadComplete() {
 
 		// nothing
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if(SceneManager.getInstance().sendOnKeyDownToCurrentScene(pKeyCode, pEvent)) {
 			return true;
 		} else {
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 
 }
 
