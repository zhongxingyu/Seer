 package org.anddev.amatidev.pvb;
 
 import org.amatidev.AdEnviroment;
 import org.amatidev.AdGameActivity;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.SplashScene;
 
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
		/*SplashScene splashScene = new SplashScene(this.mEngine.getCamera(), GameData.getInstance().mSplash, 0f, 1f, 1f);
         splashScene.registerUpdateHandler(new TimerHandler(7f, new ITimerCallback() {
         	@Override
         	public void onTimePassed(final TimerHandler pTimerHandler) {
         		AdEnviroment.getInstance().setScene(new MainMenu());
         	}
         }));
 		return splashScene;
		*/
		return new MainMenu();
 	}
 	
 }
