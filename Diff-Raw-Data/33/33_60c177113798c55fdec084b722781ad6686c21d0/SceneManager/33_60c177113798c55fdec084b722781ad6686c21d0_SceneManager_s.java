 package com.brekol.manager;
 
 import com.brekol.scene.*;
 import com.brekol.util.SceneType;
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.ui.IGameInterface;
 
 /**
  * User: Breku
  * Date: 30.06.13
  */
 public class SceneManager {
 
     private static final SceneManager INSTANCE = new SceneManager();
     private SceneType currentSceneType = SceneType.SPLASH;
     private Engine engine = ResourcesManager.getInstance().getEngine();
     private BaseScene currentScene;
 
     private BaseScene splashScene;
     private BaseScene menuScene;
     private BaseScene gameScene;
     private BaseScene loadingScene;
 
     public void setScene(BaseScene scene) {
         engine.setScene(scene);
         currentScene = scene;
         currentSceneType = scene.getSceneType();
     }
 
 
     public void setScene(SceneType sceneType) {
         switch (sceneType) {
             case MENU:
                 setScene(menuScene);
                 break;
             case GAME:
                 setScene(gameScene);
                 break;
             case SPLASH:
                 setScene(splashScene);
                 break;
             case LOADING:
                 setScene(loadingScene);
                 break;
             default:
                 break;
         }
     }
 
     public void createSplashScene(IGameInterface.OnCreateSceneCallback onCreateSceneCallback){
         ResourcesManager.getInstance().loadSplashScreen();
         splashScene = new SplashScene();
         currentScene = splashScene;
         onCreateSceneCallback.onCreateSceneFinished(splashScene);
     }
 
     public void createMenuScene(){
         ResourcesManager.getInstance().loadMenuResources();
         menuScene = new MainMenuScene();
         loadingScene = new LoadingScene();
         setScene(menuScene);
         disposeSplashScene();
     }
 
     public void loadGameScene(){
         setScene(loadingScene);
         ResourcesManager.getInstance().unloadMenuTextures();
        engine.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback() {
             @Override
             public void onTimePassed(TimerHandler pTimerHandler) {
                 engine.unregisterUpdateHandler(pTimerHandler);
                 ResourcesManager.getInstance().loadGameResources();
                 gameScene = new GameScene();
                 setScene(gameScene);
             }
         }));
     }
 
     public void loadMenuScene(){
         setScene(loadingScene);
         gameScene.disposeScene();
         ResourcesManager.getInstance().unloadGameTextures();
        engine.registerUpdateHandler(new TimerHandler(0.3f,new ITimerCallback() {
             @Override
             public void onTimePassed(TimerHandler pTimerHandler) {
                 engine.unregisterUpdateHandler(pTimerHandler);
                 ResourcesManager.getInstance().loadMenuTextures();
                 setScene(menuScene);
             }
         }));
     }
 
 
     private void disposeSplashScene()
     {
         ResourcesManager.getInstance().unloadSplashScreen();
         splashScene.disposeScene();
         splashScene = null;
     }
 
 
 
     public SceneType getCurrentSceneType() {
         return currentSceneType;
     }
 
     public BaseScene getCurrentScene() {
         return currentScene;
     }
 
     public static SceneManager getInstance() {
         return INSTANCE;
     }
 }
