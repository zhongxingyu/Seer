 package com.example.ship;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 
 public class SceletonActivity extends BaseGameActivity {
     private Camera camera;
     private static final int CAMERA_WIDTH = 720;
     private static final int CAMERA_HEIGHT = 480;
     private TextureRegion shipTextureRegion;
     Scene scene;
 
     @Override
     public EngineOptions onCreateEngineOptions() {
         camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
         EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                 new FillResolutionPolicy(), camera);
         return engineOptions;
     }
 
     @Override
     public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
         final int atlasWidth  = 1024;
         final int atlasHeight = 1024;
        // full path assets/gfx
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         // create atlas
         BuildableBitmapTextureAtlas atlas =
                 new BuildableBitmapTextureAtlas(mEngine.getTextureManager()
                                                 , atlasWidth
                                                 , atlasHeight
                                                 , TextureOptions.BILINEAR);
 
         shipTextureRegion = BitmapTextureAtlasTextureRegionFactory.
                 createFromAsset(atlas, this, "ship.png");  // ship.png in assets/gfx folder
         // Build the bitmap texture atlas
         try {
             atlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource
                     , BitmapTextureAtlas>(0, 1, 1));
         } catch (ITextureAtlasBuilder.TextureAtlasBuilderException e) {
             e.printStackTrace();
         }
         // Load the bitmap texture atlas into the device's gpu memory
         atlas.load();
         pOnCreateResourcesCallback.onCreateResourcesFinished();
     }
 
     @Override
     public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
         scene = new Scene();
         final float shipX = CAMERA_WIDTH * 0.5f;
         final float shipY = CAMERA_HEIGHT * 0.5f;
 
         // create ship sprite
         Sprite shipSprite = new Sprite(shipX
                                         , shipY
                                         , shipTextureRegion
                                         , mEngine.getVertexBufferObjectManager());
 
         scene.attachChild(shipSprite);
 
         scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
         pOnCreateSceneCallback.onCreateSceneFinished(scene);
     }
 
     @Override
     public void onPopulateScene(Scene pScene,
                                 OnPopulateSceneCallback pOnPopulateSceneCallback) {
         pOnPopulateSceneCallback.onPopulateSceneFinished();
     }
 }
