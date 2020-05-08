 /***
 === BrainGame ===
 
 Copyright (C) 2011 Giovanni Amati
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/
 
 package org.anddev.andengine.braingamelite;
 
 import org.anddev.andengine.braingamelite.menu.GameMenu;
 import org.anddev.andengine.braingamelite.scene.MainMenu;
 import org.anddev.andengine.braingamelite.singleton.Enviroment;
 import org.anddev.andengine.braingamelite.singleton.Resource;
 import org.anddev.andengine.braingamelite.singleton.StoreMyData;
 import org.anddev.andengine.braingamelite.util.MyScene;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.SplashScene;
 import org.anddev.andengine.ui.activity.LayoutGameActivity;
 
 import android.view.KeyEvent;
 
 public class BrainGameLite extends LayoutGameActivity {
 	
 	@Override
     protected int getLayoutID() {
 		return R.layout.main;
     }
 	
     @Override
     protected int getRenderSurfaceViewID() {
     	return R.id.xmllayoutexample_rendersurfaceview;
     }
     
 	public void onLoadComplete() {
 		// in layout is presente ads:loadAdOnCreate="true" non serve il codice sotto
 		
 		//AdView adView = (AdView) this.findViewById(R.id.adView);
 		//AdRequest adRequest = new AdRequest();
 	    //adView.loadAd(adRequest);
 	}
 	
 	public Engine onLoadEngine() {
 		Camera camera = new Camera(0, 0, Enviroment.CAMERA_WIDTH, Enviroment.CAMERA_HEIGHT);
 		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(Enviroment.CAMERA_WIDTH, Enviroment.CAMERA_HEIGHT), camera).setNeedsSound(true);
 		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
 		return new Engine(engineOptions);
 	}
 	
 	public void onLoadResources() {
 		Resource.instance().loadResources(this);
 		StoreMyData.instance().initVariables(this);
 		Enviroment.instance().initVariables(this); // setta tutto per iniziare
 	}
 	
 	public Scene onLoadScene() {
 		SplashScene splashScene = new SplashScene(this.mEngine.getCamera(), Resource.instance().texSplash, 0f, 1f, 1f);
         splashScene.registerUpdateHandler(new TimerHandler(7f, new ITimerCallback() {
         	@Override
         	public void onTimePassed(final TimerHandler pTimerHandler) {
         		BrainGameLite.this.mEngine.setScene(new MainMenu());
         	}
         }));
 		return splashScene;
         /*
         Enviroment.instance().createScoreLayer();
         //return new FlyBall(null, 0);
         //return new CountDown(null, null);
         //return new CatchElement(null);
         //return new MemSequence(null);
         //return new SumBox(null, 0);
         return new End();
         //return new Start();
         //return new MemShuffle(null, null, null);
         //*/
 	}
 	
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {	
 		if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if (this.mEngine.getScene().hasChildScene()) {
				((MyScene) Enviroment.instance().getScene()).getFadeLayer().getFirstChild().setAlpha(0f);
 				this.mEngine.getScene().back();
 			} else {
 				if (this.mEngine.getScene() instanceof MyScene) {
					((MyScene) Enviroment.instance().getScene()).getFadeLayer().getFirstChild().setAlpha(0.6f);
 					this.mEngine.getScene().setChildScene(new GameMenu(), false, true, true);
 				}
 			}
 			return true;
 		} else {
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 	
 }
