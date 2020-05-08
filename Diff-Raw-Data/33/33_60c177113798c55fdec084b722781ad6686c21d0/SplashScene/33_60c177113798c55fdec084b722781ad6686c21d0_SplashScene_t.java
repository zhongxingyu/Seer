 package com.brekol.scene;
 
 import com.brekol.util.SceneType;
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 
 /**
  * User: Breku
  * Date: 30.06.13
  */
 public class SplashScene extends BaseScene {
 
     private Sprite sprite;
 
     @Override
     public void createScene() {
         sprite = new Sprite(0,0,resourcesManager.getSplashTextureRegion(),vertexBufferObjectManager)
         {
             @Override
             protected void preDraw(GLState pGLState, Camera pCamera) {
                 super.preDraw(pGLState, pCamera);
                 pGLState.enableDither();
             }
         };
         sprite.setScale(1.5f);
        sprite.setPosition(400,240);
         attachChild(sprite);
     }
 
     @Override
     public void onBackKeyPressed() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public SceneType getSceneType() {
         return SceneType.SPLASH;
     }
 
     @Override
     public void disposeScene() {
         sprite.detachSelf();
         sprite.dispose();
         this.detachSelf();
         this.dispose();
     }
 }
