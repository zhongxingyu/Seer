 package com.luck.libgdx.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.luck.libgdx.game.MainGame;
 import com.luck.libgdx.until.MainUntil;
 
 public class LoadScreen implements Screen {
 	MainGame game;
 
 	MainUntil until;
 
 	public LoadScreen(MainGame game) {
 		this.game = game;
 		until = new MainUntil();

		new Thread(new Runnable() {

			public void run() {
				until.load();

			}
		}).start();
 
 	}
 
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void render(float arg0) {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		game.spriteBatch.setProjectionMatrix(game.camera.combined);
 		game.spriteBatch.begin();
 
 		game.bitmapFont.draw(game.spriteBatch,
 				String.valueOf(until.loadnum * 10) + "%", 0, 0);
 
 		game.spriteBatch.end();
 		if (until.loadnum >= 10) {
 			Screen mainScreen = new MainScreen(game);
 
 			game.setScreen(mainScreen);
 
 			// Log.v("HERE", "" + until.loadnum);
 		}
 
 	}
 
 	public void resize(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void show() {
 
 	}
 
 }
