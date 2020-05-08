 package com.ruthlessgames.ArkaPong;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class GameMain extends Game {
 	private OrthographicCamera camera;
 	public SpriteBatch batch;
 	Sprite background;
 	static float s_width;
 	static float s_height;
 	AndroidBridge androidbridge;
 	
 	public GameMain(AndroidBridge AB){
 		androidbridge = AB;
 	}
 	
 	@Override
 	public void create() {		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		s_width = w;
 		s_height = h;
 		camera = new OrthographicCamera(1, h/w);
 		batch = new SpriteBatch();
 		
 		background = new Sprite(new Texture(Gdx.files.internal("gfx/bg.png")));
		startlvl(1);
 		
 		
 		
 	}
 	public void startlvl(int lvl){
		Level temp = new Level(lvl, 10,false, background, this);
		setScreen(temp);
 	}
 	@Override
 	public void dispose() {
 		batch.dispose();
 	}
 
 }
