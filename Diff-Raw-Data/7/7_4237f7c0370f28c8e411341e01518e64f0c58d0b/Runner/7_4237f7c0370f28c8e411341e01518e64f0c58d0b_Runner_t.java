 package com.platy.battlebreak;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 public class Runner extends Game implements ApplicationListener {
 	private OrthographicCamera camera;
 	static SpriteBatch batch;
 	
 	private static final float width = 800, height = 480;
 	float ratio = width/height;
 	static AssetManager manager;
 	Rectangle viewport;
 	boolean touch_down = false;
 	GameScreen gamescreen;
 	
 	
 	@Override
 	public void create() {		
 		loadAssets();
 		
 		this.camera = new OrthographicCamera(width, height);
         this.camera.position.set(width/2, height/2, 0f);
 		batch = new SpriteBatch();
 		gamescreen = new GameScreen();
 		setScreen(gamescreen);
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 	}
 
 	@Override
 	public void render() {		
 		if(manager.update()) {
 	        camera.update();
 
 	        // clear previous frame
 	        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 			if(Gdx.input.isTouched()) {
 				touch_down = true;
 				Vector3 touchPos = new Vector3();
 				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 				camera.unproject(touchPos);
 			}
 			else {
 				touch_down = false;
 			}
 			texture = manager.get("data/glenn.gif", Texture.class);
 			
 			viewMatrix.setToOrtho2D(0, 0, width, height);
 	        batch.setProjectionMatrix(viewMatrix);
 	        batch.setTransformMatrix(transformMatrix);
 	        batch.begin();
 	        batch.draw(texture, 0, 0);
 			batch.end();
 		}
 		
 		float progress = manager.getProgress();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		float aspectRatio = (float)width/(float)height;
         float scale = 1f;
         Vector2 crop = new Vector2(0f, 0f); 
         
         if(aspectRatio > ratio)
         {
             scale = (float)height/(float)this.height;
             crop.x = (width - this.width*scale)/2f;
         }
         else if(aspectRatio < ratio)
         {
             scale = (float)width/(float)this.width;
             crop.y = (height - this.height*scale)/2f;
         }
         else
         {
             scale = (float)width/(float)this.width;
         }
 
         float w = (float)this.width*scale;
         float h = (float)this.height*scale;
         viewport = new Rectangle(crop.x, crop.y, w, h);
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 	
 	public void loadAssets() {
 		manager = new AssetManager();
 		
 		manager.load("data/ball.png", Texture.class);
 	}
 	
 }
