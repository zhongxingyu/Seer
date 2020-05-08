 package com.jomeister;
 //imports
 
 import javax.microedition.khronos.opengles.GL10;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.BoundCamera;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
 import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
 import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.anddev.andengine.entity.layer.tiled.tmx.*;
 import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.Debug;
 
 
 
 public class AndEngineGame extends BaseGameActivity {
 
     //stuff to declare
     //will need this later
     private static final int cam_width = 800;
     private static final int cam_height = 480;
     private BitmapTextureAtlas mBitmapTextureAtlas;
     private Camera mCamera;
     private TiledTextureRegion mAdventurerTextureRegion;
     private BoundCamera mBoundChaseCamera;
     private BitmapTextureAtlas mOnScreenControlTexture;
     private TextureRegion mOnScreenControlBaseTextureRegion;
     private TextureRegion mOnScreenControlKnobTextureRegion;
     private DigitalOnScreenControl mDigitalOnScreenControl;
     private HUD mHUD;
     private TMXTiledMap mTMXTiledMap;
 
 
     private enum AdventurerDirection {
         
            UP,
            NONE,
            DOWN,
            LEFT,
            RIGHT,
        }
 
     private AdventurerDirection adventurerDirection = AdventurerDirection.DOWN;
     
     //methods to be called later
    //probably a method
    //maybe a method?
    //definitely not a method
     
     @Override       
     public Engine onLoadEngine() { 
         //setup camera
 
         this.mBoundChaseCamera = new BoundCamera(0, 0, cam_width, cam_height);
         return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, 
         new RatioResolutionPolicy(cam_width, cam_height), this.mBoundChaseCamera));
    
     
     }
     @Override
     public void onLoadResources() {
         //give location on images, etc
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         //base texture
                 this.mBitmapTextureAtlas = new BitmapTextureAtlas(512, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         //loading snapdragon
         this.mAdventurerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset
        (this.mBitmapTextureAtlas, this, "snapdragon_tiled.png", 0, 0, 3, 4);
 
                 
         this.mOnScreenControlTexture = new BitmapTextureAtlas(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
         this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
         this.mEngine.getTextureManager().loadTextures(this.mBitmapTextureAtlas, this.mOnScreenControlTexture);
  }
     @Override
 
     public Scene onLoadScene() {
         this.mEngine.registerUpdateHandler(new FPSLogger());
                 //load scene
                 final Scene scene = new Scene();
                 
                 //try to load tmx
                 try {
                 final TMXLoader tmxLoader = new TMXLoader(this, this.mEngine.getTextureManager(), 
                 TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener() {
                                 @Override
                 public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, 
                                 final TMXLayer pTMXLayer, final TMXTile pTMXTile, 
                                 final TMXProperties<TMXTileProperty> pTMXTileProperties) {}
                         });
                         this.mTMXTiledMap = tmxLoader.loadFromAsset(this, "tmx/desert.tmx");
 
                 //catch error
                 } catch (final TMXLoadException tmxle) {
                         Debug.e(tmxle);
                 }
                 
                 //attaching thing ftw
                 final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
                 
                
                 scene.attachChild(tmxLayer);
 
                
                 
                 //get center of screen
                 final int centerX = (cam_width - this.mAdventurerTextureRegion.getTileWidth()) / 2;
                 final int centerY = (cam_height - this.mAdventurerTextureRegion.getTileHeight()) / 2;
                 
                 
                 //load animatedsprite using previous variables
                 final AnimatedSprite adventurer = new AnimatedSprite(centerX, centerY, this.mAdventurerTextureRegion);
                 
                 
                 //make the camera follow the sprite    
                 this.mBoundChaseCamera.setChaseEntity(adventurer);
                 //commented out until solution for chase camera is found
                 
                 
                 
                 //attach sprite to scene
                 scene.attachChild(adventurer);
 
                 
                 //physics for the adventurer
                 final PhysicsHandler physicsHandler = new PhysicsHandler(adventurer);
                 adventurer.registerUpdateHandler(physicsHandler);
 
                 
                 
                 //getting touchpad to work
                 this.mDigitalOnScreenControl = new DigitalOnScreenControl(0, cam_height - this.mOnScreenControlBaseTextureRegion.getHeight(), 
                 this.mBoundChaseCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {
                         @Override
                         public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, 
                         final float pValueX, final float pValueY) {
                                
                         //walking animations TODO: Make it more efficient   
                         if (pValueY == 1){
                                         // Up
                                         if (adventurerDirection != AdventurerDirection.UP){
                                                 adventurer.animate(new long[]{200, 200, 200}, 0, 2, true);
                                                 adventurerDirection = AdventurerDirection.UP;
                                         }
                                 }else if (pValueY == -1){
                                         // Down
                                         if (adventurerDirection != AdventurerDirection.DOWN){
                                                 adventurer.animate(new long[]{200, 200, 200}, 9, 11, true);
                                                 adventurerDirection = AdventurerDirection.DOWN;
                                         }
                                 }else if (pValueX == -1){
                                         // Left
                                         if (adventurerDirection != AdventurerDirection.LEFT){
                                                 adventurer.animate(new long[]{200, 200, 200}, 3, 5, true);
                                                 adventurerDirection = AdventurerDirection.LEFT;
                                         }
                                 }else if (pValueX == 1){
                                         // Right
                                         if (adventurerDirection != AdventurerDirection.RIGHT){
                                                 adventurer.animate(new long[]{200, 200, 200}, 6, 8, true);
                                                 adventurerDirection = AdventurerDirection.RIGHT;
                                         }
                                 }else{
                                         if (adventurer.isAnimationRunning()){
                                                 adventurer.stopAnimation();
                                               adventurerDirection = AdventurerDirection.NONE;
                                         }
                                 }
                                 physicsHandler.setVelocity(pValueX * 1000, pValueY * 1000);
                         }
                         
                         
                         
                 });
                 this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                 this.mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
                 this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
                 this.mDigitalOnScreenControl.getControlBase().setScale(1.25f);
                 this.mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
                 this.mDigitalOnScreenControl.refreshControlKnobPosition();
 
                 
                 
 
                 
                 scene.setChildScene(this.mDigitalOnScreenControl);
 
                 return scene;
 
 
     }
     @Override
 
     public void onLoadComplete() {
         
     }
    
     
 
 
 }
