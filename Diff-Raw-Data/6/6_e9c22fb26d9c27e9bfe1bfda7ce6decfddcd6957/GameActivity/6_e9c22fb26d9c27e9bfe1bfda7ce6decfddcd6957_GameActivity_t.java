 package gamedev.game;
 
 import java.io.IOException;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.LimitedFPSEngine;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import android.view.KeyEvent;
 
 public class GameActivity extends BaseGameActivity {
 
 	public enum GameMode {
 		EXPLORING, FIGHTING, POPUP
 	}
 
 	public static GameMode mode = GameMode.EXPLORING;
 
 	// Game resolution
 	public final static int WIDTH = 800;
 	public final static int HEIGHT = 480;
 
 	// Camera chasing the player
 	private BoundCamera camera;
 
 	// Singleton class to load resources and get objects across different Scenes
 	private ResourcesManager resourcesManager;
 
 	@Override
 	public Engine onCreateEngine(EngineOptions pEngineOptions) {
 		// Try to run our game at 60fps
 		return new LimitedFPSEngine(pEngineOptions, 60);
 	}
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.camera = new BoundCamera(0, 0, WIDTH, HEIGHT);
 		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), this.camera);
 		engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
 		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
 		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws IOException {
 		ResourcesManager.prepareManager(this.mEngine, this, this.camera,
 				this.getVertexBufferObjectManager(), this.getTextureManager(),
 				this.getSoundManager());
 		this.resourcesManager = ResourcesManager.getInstance();
 		// System.out.println("Rescources created");
 		pOnCreateResourcesCallback.onCreateResourcesFinished();
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws Exception {
 		// System.out.println("Scene created");
 		SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 
 		// System.out.println("Populate Scene");
 		mEngine.registerUpdateHandler(new TimerHandler(2f,
 				new ITimerCallback() {
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						mEngine.unregisterUpdateHandler(pTimerHandler);
 						// set menu scene using scene manager
 						SceneManager.getInstance().createMenuScene();
 					}
 				}));
 		pOnPopulateSceneCallback.onPopulateSceneFinished();
 	}
 
 	/**
 	 * Handling pressing the Back-Button. Forward this request to the currently
 	 * active Scene
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
 		}
 		return false;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (this.isGameLoaded()) {
 			resourcesManager.soundManager.setMasterVolume(0);
 			resourcesManager.backgroundMusicMenu.pause();
 		}
 	}
 
 	@Override
 	protected synchronized void onResume() {
 		super.onResume();
 		System.gc();
 		if (this.isGameLoaded()) {
 			resourcesManager.soundManager.setMasterVolume(1);
 			resourcesManager.backgroundMusicMenu.play();
 		}
 	}
 
 	/**
 	 * Exit game activity
 	 */
 	protected void onDestroy() {
 		super.onDestroy();
 
 		if (this.isGameLoaded()) {
 			System.exit(0);
 		}
 	}
 
 }
