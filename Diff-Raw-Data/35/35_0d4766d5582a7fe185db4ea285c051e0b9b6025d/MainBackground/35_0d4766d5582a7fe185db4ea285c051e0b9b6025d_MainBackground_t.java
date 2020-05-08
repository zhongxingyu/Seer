 package com.twam.boostrunner.entity;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.AutoParallaxBackground;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 /**
  * Created by masih on 7/9/13.
  */
 public class MainBackground extends ParallaxLayer{
 
     //============================================================
     // constants
     //============================================================
 
     private Sprite citybackgroundSprite;
     private Sprite skybackgroundSprite;
     //=====================================s=======================
     // constructor
     //============================================================
 
     public MainBackground(Camera mCamera, Boolean isScrollable, int range) {
         super(mCamera,isScrollable,range);
     }
 
     //============================================================
     // methods
     //============================================================
 
     /**
     * initialize citybackground sprite
     */
     public void initCintyBackgroudSprite(Engine mEngine, ITextureRegion citybackgroundTexture) {
        citybackgroundSprite = new Sprite(citybackgroundTexture.getWidth()*0.5f,citybackgroundTexture.getHeight()*0.55f,citybackgroundTexture.getWidth(),citybackgroundTexture.getHeight()*1.1f,citybackgroundTexture,mEngine.getVertexBufferObjectManager());
     }
 
     /**
      * initialize skybackground sprite
      */
     public void initSkyBackgroundSprite(Engine mEngine, ITextureRegion skybackgroundTexture) {
        skybackgroundSprite = new Sprite(skybackgroundTexture.getWidth()*0.5f,skybackgroundTexture.getHeight()*0.55f,skybackgroundTexture.getWidth(),skybackgroundTexture.getHeight()*1.1f,skybackgroundTexture,mEngine.getVertexBufferObjectManager());
     }
 
     /**
     * load parallax entity;
     */
     public void loadbackground(Scene scene) {
         this.setParallaxChangePerSecond(8);
         this.setParallaxScrollFactor(1);
         this.attachParallaxEntity(new ParallaxEntity(-3, skybackgroundSprite,false));
         this.attachParallaxEntity(new ParallaxEntity(-3, citybackgroundSprite,false));
         scene.attachChild(this);
     }
 
 }
