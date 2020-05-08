 package com.example.ship;
 
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import com.example.ship.commons.A;
 import com.example.ship.resource.ResourceManager;
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.ui.activity.BaseGameActivity;
 
 public class RootActivity extends BaseGameActivity {
     private static final int TEXTURE_WIDTH = 1739;
     private static final int TEXTURE_HEIGHT = 900;
    private static final boolean DEBUG_GAME_SCENE = false;
     private ResourceManager resourceManager;
     private Events events;
     private ZoomCamera zoomCamera;
     private SceneSwitcher sceneSwitcher = null;
 
     private ZoomCamera createZoomCamera() {
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         ZoomCamera camera;
 
         float cameraWidth = TEXTURE_WIDTH;
         float cameraHeight = cameraWidth * metrics.heightPixels / metrics.widthPixels;
         camera = new ZoomCamera(0, 0, cameraWidth, cameraHeight);
 
         final PointF cameraCenter = new PointF(0.5f * TEXTURE_WIDTH, 0.5f * TEXTURE_HEIGHT);
         camera.setCenter(cameraCenter.x, cameraCenter.y );
 
         final float zoomFactor = cameraHeight / TEXTURE_HEIGHT;
         camera.setZoomFactor(zoomFactor);
         return camera;
     }
 
     @Override
     public EngineOptions onCreateEngineOptions() {
         zoomCamera = createZoomCamera();
 
         EngineOptions engineOptions = new EngineOptions( true
                                                        , ScreenOrientation.LANDSCAPE_FIXED
                                                        , new FillResolutionPolicy()
                                                        , zoomCamera);
         engineOptions.getTouchOptions().setNeedsMultiTouch(true);
         engineOptions.getAudioOptions().setNeedsMusic(true);
         engineOptions.getAudioOptions().setNeedsSound(true);
         return engineOptions;
     }
 
     @Override
     public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
         resourceManager = new ResourceManager(this);
         resourceManager.loadAllTextures(mEngine.getTextureManager());
         resourceManager.loadAllMusic(mEngine.getMusicManager());
         resourceManager.loadAllSound(mEngine.getSoundManager());
 
         FontFactory.setAssetBasePath(getResources().getString(R.string.FONT_BASE_ASSET_PATH));
 
         events = new Events(this);
 
         pOnCreateResourcesCallback.onCreateResourcesFinished();
     }
 
     @Override
     public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
         A.init(this);
         sceneSwitcher = new SceneSwitcher(this);
 
         if (DEBUG_GAME_SCENE) {
             sceneSwitcher.switchToGameScene();
         }
 
         pOnCreateSceneCallback.onCreateSceneFinished(sceneSwitcher.getRootScene());
     }
 
     @Override
     public void onPopulateScene( Scene pScene
                                , OnPopulateSceneCallback pOnPopulateSceneCallback) {
 
         pOnPopulateSceneCallback.onPopulateSceneFinished();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             switch (sceneSwitcher.getCurrentState()) {
                 case SceneSwitcher.GAME_STATE:
                     sceneSwitcher.switchToPauseHUD();
                     break;
                 case SceneSwitcher.PAUSE_STATE:
                     sceneSwitcher.switchToGameHUD();
                     break;
                 case SceneSwitcher.GAME_OVER_STATE:
                     sceneSwitcher.switchToMenuScene();
                     break;
                 default:
                     return super.onKeyDown(keyCode, event);
             }
             return true;
 
         } else {
             return super.onKeyDown(keyCode, event);
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         resourceManager.pauseAllMusic();
         resourceManager.pauseAllSound();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (sceneSwitcher != null) {
             sceneSwitcher.manageSound(sceneSwitcher.getCurrentState());
         }
     }
 
     public Events getEvents() {
         return events;
     }
 
     public ResourceManager getResourceManager() {
         return resourceManager;
     }
 
     public Point getTextureSize() {
         return new Point(TEXTURE_WIDTH, TEXTURE_HEIGHT);
     }
 
     public ZoomCamera getCamera() {
         return zoomCamera;
     }
 
     public SceneSwitcher getSceneSwitcher() {
         return sceneSwitcher;
     }
 
     public int getIntResource(int id) {
         return this.getResources().getInteger(id);
     }
 
     public String getStringResource(int id) {
         return this.getResources().getString(id);
     }
 }
