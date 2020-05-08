 package com.benjaminlanders.taptorun;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.benjaminlanders.taptorun.controller.Controller;
 import com.benjaminlanders.taptorun.helper.Assets;
 import com.benjaminlanders.taptorun.helper.HighScroreKeeper;
 import com.benjaminlanders.taptorun.helper.SoundManager;
 import com.benjaminlanders.taptorun.model.World;
 import com.benjaminlanders.taptorun.renderer.GameRenderer;
 import com.benjaminlanders.taptorun.renderer.HighScoreRenderer;
 import com.benjaminlanders.taptorun.renderer.MenuRenderer;
 import com.benjaminlanders.taptorun.renderer.Renderer;
 
 public class TapToRunMain implements ApplicationListener
 {
 	private SpriteBatch batch;
 	public static float h,w;
 	public static final int MAIN = 0;
 	public static final int MENU = 1;
 	public static final int SCORE = 2;
 	public int state= MENU;
 	private Renderer main, menu, scores;
 	Controller controller;
 	World world;
 	public static Preferences prefs;
 	@Override
 	public void create()
 	{		
 		w = Gdx.graphics.getWidth();
 		h = Gdx.graphics.getHeight();
 		
 		prefs = Gdx.app.getPreferences("preferences");
 		
 		world = new World();
 		controller = new Controller(world);
 		
 		batch = new SpriteBatch();
 		
 		SoundManager.init();
 		
 		Assets.font = new BitmapFont(Gdx.files.internal("test.fnt"), false);
 		
 		main = new GameRenderer(this, batch, world);
 		menu = new MenuRenderer(this, batch);
 		scores = new HighScoreRenderer(this, batch);
 		
 		if(HighScroreKeeper.readScoresFromFile()[0].equalsIgnoreCase(""))
 		{
 			HighScroreKeeper.createFile();
 		}
 	}
 
 	@Override
 	public void dispose()
 	{
 		batch.dispose();
 	}
 
 	@Override
 	public void render()
 	{		
 		float delta = Gdx.graphics.getDeltaTime();
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		switch(state)
 		{
 			case MAIN:
 				controller.update(delta);
 				main.render(delta);
 				break;
 			case MENU:
 				menu.render(delta);
 				break;
 			case SCORE:
 				scores.render(delta);
 				break;
 		}
 	}
 
 	@Override
 	public void resize(int width, int height)
 	{
 	}
 
 	@Override
 	public void pause()
 	{
 	}
 	
 	@Override
 	public void resume()
 	{
 	}
 	
 	public void setState(int state)
 	{
 		this.state = state;
 		if(state == MAIN)
 		{
 			resetWorld();
 		}
 	}
 	
 	public void resetWorld()
 	{
 		world.reset();
		controller.ref = 9;
 	}
 	
 }
