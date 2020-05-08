 package com.me.mygdxgame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.Game;
 import com.me.mygdxgame.screens.GameScreen;
 
 public class HelloWorld extends Game {
 
 	private static final String LOG = HelloWorld.class.getName().substring(HelloWorld.class.getName().lastIndexOf(".")+1);
 		
 	private FPSLogger fpsLogger;
 	
 	@Override
 	public void create() {
 		int width = (int)(Gdx.graphics.getDesktopDisplayMode().width * 0.85f);
 		int height = (int)(Gdx.graphics.getDesktopDisplayMode().height * 0.85f);
 		
 		Gdx.graphics.setDisplayMode(width, height, false);
 		Gdx.app.log(HelloWorld.LOG, "Creating game" );
         fpsLogger = new FPSLogger();
         
         setScreen(new GameScreen());
         
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.app.log(HelloWorld.LOG, "Destroying game" );
 		super.dispose();
 	}
 
 	@Override
 	public void render() {		
 		fpsLogger.log();
 		
 		super.render();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		
 		Gdx.app.log(HelloWorld.LOG, "Resizing game to: " + width + " x " + height );
 		super.resize(width, height);
 	}
 
 	@Override
 	public void pause() {
 
 		Gdx.app.log(HelloWorld.LOG, "Pausing game" );
 		super.pause();
 	}
 
 	@Override
 	public void resume() {
 		Gdx.app.log(HelloWorld.LOG, "Resuming game" );
 		super.resume();
 	}
 }
