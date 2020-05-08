 package eu32k.ludumdare.ld26;
 
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 
 import eu32k.libgdx.common.Assets;
 import eu32k.ludumdare.ld26.effects.EffectsManager;
 
 public class Preloader {
 
    public static void preLoad() {
       AssetManager am = Assets.MANAGER;
 
       Assets.loadTexture("textures/sound.png");
       Assets.loadTexture("textures/tiles2.png");
       Assets.loadTexture("textures/tiles3.png");
       Assets.loadTexture("textures/square.png");
       Assets.loadTexture("textures/square2.png");
       Assets.loadTexture("textures/circle.png");
       Assets.loadTexture("textures/circle2.png");
       Assets.loadTexture("textures/title.png");
 
       am.load("fonts/consolas.fnt", BitmapFont.class);
       am.load("fonts/calibri.fnt", BitmapFont.class);
 
       am.load(EffectsManager.TRACK_BITBREAK_INTRO, Music.class);
       am.load(EffectsManager.TRACK_BITBREAK_BODY, Music.class);
       am.load(EffectsManager.TRACK_BITBREAK_OUTRO, Music.class);
 
       am.load(EffectsManager.TRACK_OTGY_INTRO, Music.class);
       am.load(EffectsManager.TRACK_OTGY_BODY, Music.class);
       am.load(EffectsManager.TRACK_OTGY_OUTRO, Music.class);
 
       wait(am);

      preloadMusic(am, EffectsManager.TRACK_BITBREAK_INTRO);
      preloadMusic(am, EffectsManager.TRACK_BITBREAK_BODY);
      preloadMusic(am, EffectsManager.TRACK_BITBREAK_OUTRO);
      preloadMusic(am, EffectsManager.TRACK_OTGY_INTRO);
      preloadMusic(am, EffectsManager.TRACK_OTGY_BODY);
      preloadMusic(am, EffectsManager.TRACK_OTGY_OUTRO);

   }

   private static void preloadMusic(AssetManager am, String path) {
      Music music = am.get(path, Music.class);
      music.setVolume(0.0f);
      music.play();
      try {
         Thread.sleep(Config.MUSIC_PRELOAD_HACK ? 100 : 1);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      music.stop();
    }
 
    private static void wait(AssetManager am) {
       while (true) {
          am.update();
          if (am.getProgress() == 1.0f) {
             return;
          }
          try {
             Thread.sleep(10);
          } catch (InterruptedException e) {
             e.printStackTrace();
          }
       }
    }
 
 }
