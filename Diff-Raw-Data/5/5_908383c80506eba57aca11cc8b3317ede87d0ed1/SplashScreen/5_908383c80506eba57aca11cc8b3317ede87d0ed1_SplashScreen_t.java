 package fi.hbp.angr.screens;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.utils.Timer;
 
import fi.hbp.angr.Assets;

 public class SplashScreen extends Timer.Task implements Screen {
     private OrthographicCamera camera;
     private SpriteBatch batch;
     private Texture texture;
     private Sprite sprite;
 
     private Timer splashTimer = new Timer();
     private ScreenLoader loader;
 
     /**
      *
      * @param g Main game.
      * @param gs Screen to be shown after this splash screen.
      */
     public SplashScreen(Game g, Screen gs) {
         loader = new ScreenLoader(g, gs);
     }
 
     @Override
     public void render(float delta) {
         Gdx.gl.glClearColor(1, 1, 1, 1);
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
         batch.setProjectionMatrix(camera.combined);
         batch.begin();
         sprite.draw(batch);
         batch.end();

        Assets.getAssetManager().update();
     }
 
     @Override
     public void resize(int width, int height) {
         // TODO Auto-generated method stub
     }
 
     /**
      * Initializes this splash screen
      */
     @Override
     public void show() {
         float width = Gdx.graphics.getWidth();
         float height = Gdx.graphics.getHeight();
 
         camera = new OrthographicCamera(1, height / width);
         batch = new SpriteBatch();
 
         texture = new Texture(Gdx.files.internal("data/libgdx.png"));
         texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
         TextureRegion region = new TextureRegion(texture, 0, 0, 512, 275);
 
         sprite = new Sprite(region);
         sprite.setSize(0.9f, 0.9f * sprite.getHeight() / sprite.getWidth());
         sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
         sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);
 
         // Change to Screen gs given in constructor after a short delay
         splashTimer.scheduleTask(this, 1.5f);
 
         // Start loading the next screen
         loader.start();
     }
 
     @Override
     public void hide() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void pause() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void resume() {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void dispose() {
         batch.dispose();
         texture.dispose();
     }
 
     @Override
     public void run() {
         loader.swap();
     }
 }
