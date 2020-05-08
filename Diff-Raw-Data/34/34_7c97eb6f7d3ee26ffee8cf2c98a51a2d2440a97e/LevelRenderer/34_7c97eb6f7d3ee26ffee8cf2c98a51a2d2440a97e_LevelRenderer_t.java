 package com.me.Roguish.View;
 
 import com.me.Roguish.Model.Level;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.tiled.*;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.math.Vector3;
 
 public class LevelRenderer {
 
	private static final float CAMERA_WIDTH = 320f;
	private static final float CAMERA_HEIGHT = 480f;
 	private static final float RUNNING_FRAME_DURATION = 0.06f;
 	
 	private Level level;
 	private OrthographicCamera cam;
 	
 	private boolean debug = false;
 	private int width;
 	private int height;
 	private TileMapRenderer tileMapRenderer;
 	private SpriteBatch spriteBatch;
 	
 	public LevelRenderer(Level level, boolean debug) {
 		this.level = level;	
 		this.debug = debug;
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
 		this.cam.update();
		tileMapRenderer = new TileMapRenderer(this.level.map, this.level.atlas, 32, 32);
 		spriteBatch = new SpriteBatch();
		loadTextures();
	}
	
	public void loadTextures(){
		// Load Textures or something?
 	}
 	
 	public void setSize (int width, int height) {
 		this.width = width;
 		this.height = height;
 	}
 	
 	public void render() {
         Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		tileMapRenderer.getProjectionMatrix().set(cam.combined);
 		 
 		Vector3 tmp = new Vector3();
 		tmp.set(0, 0, 0);
 		cam.unproject(tmp);
  
 		tileMapRenderer.render((int) tmp.x, (int) tmp.y,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
         cam.zoom = 0.9f;
         cam.update();
         
         tileMapRenderer.render(cam);
 
 		if (debug)
 			drawDebug();
 	}
 	
 	private void drawDebug(){
 		
 	}
 }
