 package com.twam.boostrunner.manager;
 
 import android.content.Context;
 import android.util.Log;
 
 import org.andengine.engine.Engine;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 /**
  * Created by parsia on 7/9/13.
  *
  * class
  */
 public class ResourceManager {
 
     //============================================================
     // constants
     //============================================================
     private final int BACKGROUNDATLASWIDTH = 2800;
     private final int BACKGROUNDATLASHEIGHT= 1200;
     private final int GENERALATLASWIDTH = 1024;
     private final int GENERALATLASHEIGHT = 1024;
     private final String cityBackgroundFilePath = "background/cityBackground.png";
     private final String skyBackgroundFilePath = "background/skyBackground.png";
     private final String platformTexturePath = "general/platform.png";
 
     //============================================================
     // fields
     //============================================================
     private static ResourceManager instance;
     private BuildableBitmapTextureAtlas backgroundTextureAtlas;
     private BuildableBitmapTextureAtlas generalTextureAtlas;
     private ITextureRegion skyBackground;
     private ITextureRegion cityBackground;
     private ITextureRegion platformTexture;
 
     //============================================================
     // constructor
     //============================================================
      private ResourceManager() {
      }
 
     //============================================================
     // getters
     //============================================================
     public BuildableBitmapTextureAtlas getTextureAtlas() {
         return backgroundTextureAtlas;
     }
 
     public ITextureRegion getSkyBackground() {
         return skyBackground;
     }
 
     public ITextureRegion getCityBackground() {
         return cityBackground;
     }
 
     public ITextureRegion getPlatformTexture() {
         return platformTexture;
     }
 
     //============================================================
     // methods
     //============================================================
 
 
 
     /**
     * get instance for singleton pattern in Resource Managfer class
      */
     public synchronized static ResourceManager getInstance(){
         if(ResourceManager.instance == null)
             instance = new ResourceManager();
         return instance;
     }
 
     /**
     * method for loading bitmap texture atlas and all game textures
     * */
     public synchronized void loadTextures(Engine engine, Context context){
         //initialize all texture atlases and base asset path
         initTextureAtlases(engine);
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
         //load general textures
         loadPlatformTexture(context);
         buildGeneralTexture();
 
         //load background textures
         loadCityBackgroundTexture(context);
         loadSkyBackgroundTexture(context);
         buildBackgroundTexture();
     }
 
     /**
      * initialize all texture atlases
      * */
     public void initTextureAtlases(Engine engine){
         generalTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), GENERALATLASWIDTH, GENERALATLASHEIGHT, TextureOptions.NEAREST);
         backgroundTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), BACKGROUNDATLASWIDTH, BACKGROUNDATLASHEIGHT, TextureOptions.NEAREST);
     }
 
 
     /**
      * method for initializing and building general bitmap textue atlas
      * */
     public void buildGeneralTexture(){
         try{
             this.generalTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0,1,1));
         }
         catch (TextureAtlasBuilderException ex){
             Log.e("loadFailed","Texture atlas for general build failed");
         }
         generalTextureAtlas.load();
     }
 
     /**
      * method for initializing and building background bitmap textue atlas
      * */
     public void buildBackgroundTexture(){
         try{
             this.backgroundTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0,1,1));
         }
         catch (TextureAtlasBuilderException ex){
             Log.e("loadFailed","Texure atlas for background build failed");
         }
         backgroundTextureAtlas.load();
     }
 
     /**
      *  init city background texture region
      * */
     public void loadCityBackgroundTexture(Context context){
         this.cityBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundTextureAtlas, context, cityBackgroundFilePath);
     }
 
     /**
     * init sky background texture region
     * */
     public void loadSkyBackgroundTexture(Context context){
         this.skyBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(backgroundTextureAtlas, context, skyBackgroundFilePath);
     }
 
     /**
      * init platform texture region
      * */
     public void loadPlatformTexture(Context context){
         this.platformTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(generalTextureAtlas, context, platformTexturePath);
     }
 
 }
