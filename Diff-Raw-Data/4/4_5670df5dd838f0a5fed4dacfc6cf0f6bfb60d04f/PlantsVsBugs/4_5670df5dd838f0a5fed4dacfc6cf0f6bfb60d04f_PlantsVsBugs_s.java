 package org.anddev.amatidev.pvb;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.amatidev.activity.AdGameActivity;
 import org.amatidev.util.AdEnviroment;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.SplashScene;
 
 import android.content.pm.ActivityInfo;
 
 import com.openfeint.api.OpenFeint;
 import com.openfeint.api.OpenFeintDelegate;
 import com.openfeint.api.OpenFeintSettings;
 
 public class PlantsVsBugs extends AdGameActivity {
 
 	@Override
 	protected int getLayoutID() {
 		return R.layout.main;
 	}
 
 	@Override
 	protected int getRenderSurfaceViewID() {
 		return R.id.xmllayoutexample_rendersurfaceview;
 	}
 
 	@Override
 	public void onLoadComplete() {
 		try {
 			Map<String, Object> options = new HashMap<String, Object>();
 			options.put(OpenFeintSettings.SettingCloudStorageCompressionStrategy, OpenFeintSettings.CloudStorageCompressionStrategyDefault);
 			// use the below line to set orientation
 			options.put(OpenFeintSettings.RequestedOrientation, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			OpenFeintSettings settings = new OpenFeintSettings(getString(R.string.app_name), getString(R.string.productKey), getString(R.string.productSecret), getString(R.string.clientAppID), options);
 			
 			OpenFeint.initialize(this, settings, new OpenFeintDelegate() { });
 		} catch (Exception e) {
 			
 		}
 	}
 
 	@Override
 	public Engine onLoadEngine() {
 		return AdEnviroment.createEngine(ScreenOrientation.LANDSCAPE, true, false);
 	}
 
 	@Override
 	public void onLoadResources() {
 		GameData.getInstance().initData();
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		SplashScene splashScene = new SplashScene(this.mEngine.getCamera(), GameData.getInstance().mSplash, 0f, 1f, 1f);
         splashScene.registerUpdateHandler(new TimerHandler(7f, new ITimerCallback() {
         	@Override
         	public void onTimePassed(final TimerHandler pTimerHandler) {
         		AdEnviroment.getInstance().setScene(new MainMenu());
         	}
         }));
		//return splashScene;
        return new Tutorial();
 	}
 	
 }
