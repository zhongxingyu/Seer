 package com.willbat.MotherlAndroid;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Jobat
  * Date: 22/08/13
  * Time: 23:36
  * To change this template use File | Settings | File Templates.
  */
 public class MLGameScreen implements Screen {
     public final static boolean debug = true;
     public final static Vector2 chunkSize = new Vector2(20,20);
 
     MLCore game;
     SpriteBatch batch;
     SpriteBatch debugBatch;
     Map map;
     Player player;
     ExtendedCamera camera;
     BitmapFont font;
     float zoomLevel = 0.3f;
     Vector2 tilesOnScreen;
    // List drawables;
 
     public MLGameScreen(MLCore game)
     {
         setupFileSystem();
         this.game = game;
         camera = new ExtendedCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
         camera.setToOrtho(false);
         camera.update();
         batch = new SpriteBatch();
         debugBatch = new SpriteBatch();
         tilesOnScreen = new Vector2((zoomLevel*Gdx.graphics.getWidth()-zoomLevel*Gdx.graphics.getWidth()%32)/32,(zoomLevel*Gdx.graphics.getHeight()-zoomLevel*Gdx.graphics.getHeight()%32)/32);
         player = new Player(camera, zoomLevel);
        map = new Map("TestMap1", tilesOnScreen);
         font = new BitmapFont(Gdx.files.internal("consolas.fnt"),Gdx.files.internal("consolas_0.png"),false);
     }
 
     @Override
     public void render(float delta)
     {
         Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
         Gdx.gl20.glClearColor(100f / 255, 149f / 255, 237f / 255, 1.0F);
         camera.zoom = zoomLevel;
         camera.update();
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
         map.update(batch, camera);
         player.update(batch, delta);
         batch.end();
         if (debug)
         {
             debugBatch.begin();
             renderDebug();
             debugBatch.end();
         }
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
 
     private void setupFileSystem()
     {
         if (Gdx.files.isExternalStorageAvailable())
         {
             String extRoot = Gdx.files.getExternalStoragePath();
             FileHandle directory = new FileHandle(extRoot.concat("/MotherlAndroid"));
             if (!directory.exists())
             {
                 directory.mkdirs();
             }
         }
     }
     public void renderDebug() {
         font.setColor(0.0f,0.0f,0.0f,1.0f);
         CharSequence debugString = ("W: " + Gdx.graphics.getWidth() + ", H: " + Gdx.graphics.getHeight() + ", FPS: " + Gdx.graphics.getFramesPerSecond() + ", PlayerPos: " + player.position);
         font.draw(debugBatch, debugString, 0,Gdx.graphics.getHeight());
     }
 }
