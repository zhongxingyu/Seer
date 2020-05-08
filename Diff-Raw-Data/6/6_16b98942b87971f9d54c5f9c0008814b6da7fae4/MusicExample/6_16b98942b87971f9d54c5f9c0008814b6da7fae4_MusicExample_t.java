 package org.anddev.andengine.examples;
 
 import java.io.IOException;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.music.MusicFactory;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
 import org.anddev.andengine.entity.scene.Scene.ITouchArea;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.util.Debug;
 
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 public class MusicExample extends BaseExample {
   private static final int CAMERA_WIDTH = 720;
   private static final int CAMERA_HEIGHT = 480;
 
   private Texture mTexture;
   private TextureRegion mNotesTextureRegion;
   private Music mMusic;
 
   @Override
   public Engine onLoadEngine() {
     Toast.makeText(this, "Touch the notes to hear some music",
         Toast.LENGTH_LONG).show();
     final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
     return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
         new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera)
         .setNeedsMusic(true));
   }
 
   @Override
   public void onLoadResources() {
     mTexture = new Texture(128, 128, TextureOptions.BILINEAR);
     TextureRegionFactory.setAssetBasePath("gfx/");
     mNotesTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this,
         "notes.png", 0, 0);
 
     MusicFactory.setAssetBasePath("mfx/");
     try {
       mMusic = MusicFactory.createMusicFromAsset(getEngine().getMusicManager(),
           this, "wagner_the_ride_of_the_valkyries.ogg");
       mMusic.setLooping(true);
     }
     catch (final IOException e){
       Debug.e("Error", e);
     }
 
     getEngine().getTextureManager().loadTexture(mTexture);
   }
 
   @Override
   public Scene onLoadScene() {
     getEngine().registerUpdateHandler(new FPSLogger());
 
     final Scene scene = new Scene(1);
     scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
 
     final int x = (CAMERA_WIDTH - mNotesTextureRegion.getWidth()) / 2;
     final int y = (CAMERA_HEIGHT - mNotesTextureRegion.getHeight()) / 2;
     final Sprite notes = new Sprite(x, y, mNotesTextureRegion);
     scene.getTopLayer().addEntity(notes);
 
     scene.registerTouchArea(notes);
     scene.setOnAreaTouchListener(new IOnAreaTouchListener() {
       @Override
       public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
           final ITouchArea pTouchArea, final float pTouchAreaLocalX,
           final float pTouchAreaLocalY) {
         if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {
           if (mMusic.isPlaying()) {
             mMusic.pause();
           }
           else {
             mMusic.play();
           }
         }
         return true;
       }
     });
 
     return scene;
   }
 
   @Override
   public void onLoadComplete() {
   }
 }
