 package com.icbat.game.tradesong;
 
 import java.util.Date;
 
 import com.badlogic.gdx.Application;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.icbat.game.LJ;
 
 public class Tradesong extends Game {
 	
 	@Override
 	public void create() {
 		LJ.setLevel(Application.LOG_DEBUG);
		LJ.newLogfile(this.getClass().getSimpleName());
 		LJ.log( "Creating game", LJ.DEBUG );
 		// Some initial logging (type and version)
 		LJ.log(new Date().toString());
 		LJ.log( Gdx.app.getType().toString(), "Version:  " + Gdx.app.getVersion(), LJ.LOG);
 		setScreen( ScreenFactory.getLevelScreen( "test", this ) );
 		
 		Gdx.input.setInputProcessor( new InputHandler() );
 	}
 
 	@Override
 	public void dispose() {
 		LJ.log( "Disposing game", LJ.DEBUG );
 		super.dispose();
 	}
 
 	@Override
 	public void resize( int width, int height ) {
 		LJ.log( "Resizing game to " + width + "w by " + height + "h", LJ.DEBUG );
 		super.resize(width, height);
 	}
 
 	@Override
 	public void pause() {
 		LJ.log( "Pausing game", LJ.DEBUG );
 		super.pause();
 	}
 
 	@Override
 	public void resume() {
 		LJ.log( "Resuming game", LJ.DEBUG );
 		super.resume();
 	}
 	
 	@Override
 	public void setScreen( Screen screen ) {
 		// Deliberately no debug here. Doesn't 'toString' well, and context is still clear.
 		super.setScreen( screen );
 	}
 }
