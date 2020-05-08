 package com.willbat.MotherlAndroid;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jobat
  * Date: 22/08/13
  * Time: 23:36
  * To change this template use File | Settings | File Templates.
  */
 public class MLGameScreen implements Screen {
     MLCore game;
     SpriteBatch batch;
     SpriteBatch debugBatch;
     Map map;
     Player player;
     ExtendedCamera camera;
     BitmapFont font;
    // List drawables;
 
     public MLGameScreen(MLCore game)
     {
         this.game = game;
         camera = new ExtendedCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
         camera.setToOrtho(false);
         camera.update();
         batch = new SpriteBatch();
         debugBatch = new SpriteBatch();
         map = new Map(10,10);
         player = new Player();
         font = new BitmapFont(Gdx.files.internal("consolas.fnt"),Gdx.files.internal("consolas_0.png"),false);
     }
 
     @Override
     public void render(float delta)
     {
         handleInput(delta);
         Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
         Gdx.gl20.glClearColor(100f / 255, 149f / 255, 237f / 255, 1.0F);
        camera.zoom = 1f;
         camera.update();
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
         map.draw(batch, camera);
         player.draw(batch, camera);
         batch.end();
         debugBatch.begin();
         renderDebug();
         debugBatch.end();
     }
 
     @Override
     public void resize(int width, int height) {
 
     }
 
     @Override
     public void show() {
 
     }
 
     @Override
     public void hide() {
 
     }
 
     @Override
     public void pause() {
 
     }
 
     @Override
     public void resume() {
 
     }
 
     @Override
     public void dispose() {
 
     }
 
     private void handleInput(float delta)
     {
         boolean firstTouch = Gdx.input.isTouched(0);
         boolean secondTouch = Gdx.input.isTouched(1);
         boolean thirdTouch = Gdx.input.isTouched(2);
 
         if (firstTouch && secondTouch && thirdTouch)
         {
 
         }
         else if (firstTouch && secondTouch)
         {
 
         }
         else if (firstTouch)
         {
             player.move(delta, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
             //camera.position.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), 0);
         }
     }
 
     public void renderDebug() {
         font.setColor(0.0f,0.0f,0.0f,1.0f);
         CharSequence debugString = ("W: " + Gdx.graphics.getWidth() + ", H: " + Gdx.graphics.getHeight() + ", FPS: " + Gdx.graphics.getFramesPerSecond());
         font.draw(debugBatch, debugString, 0,Gdx.graphics.getHeight());
     }
 }
