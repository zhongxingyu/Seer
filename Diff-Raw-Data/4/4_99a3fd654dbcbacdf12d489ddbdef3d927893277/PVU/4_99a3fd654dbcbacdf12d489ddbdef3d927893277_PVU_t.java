 package no.hist.gruppe5.pvu;
 
 import aurelienribon.tweenengine.Tween;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import no.hist.gruppe5.pvu.intro.IntroScreen;
 import no.hist.gruppe5.pvu.intro.SpriteAccessor;
 import no.hist.gruppe5.pvu.mainroom.MainScreen;
 
 public class PVU extends Game {
 
     public static Screen MAIN_SCREEN;
 
     public static float SCREEN_WIDTH = 960f;
     public static float SCREEN_HEIGHT = 580f;
     public static float GAME_WIDTH = 192f;
     public static float GAME_HEIGHT = 116f;
 
     @Override
     public void create() {
         ScoreHandler.load();
         Assets.load();
         Tween.registerAccessor(Sprite.class, new SpriteAccessor());
         MAIN_SCREEN = new MainScreen(this);
         setScreen(new IntroScreen(this));
     }
 
     @Override
     public void dispose() {
     }
 
     @Override
     public void render() {
         super.render();
     }
 
     @Override
     public void resize(int width, int height) {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void resume() {
     }
 
     public static void main(String[] args) {
         LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
         cfg.width = (int) PVU.SCREEN_WIDTH;
         cfg.height = (int) PVU.SCREEN_HEIGHT;
         cfg.fullscreen = false;
         cfg.resizable = false;
 
         new LwjglApplication(new PVU(), cfg);
     }
 }
