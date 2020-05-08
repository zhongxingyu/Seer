 package org.doublelong.breakout.screens;
 
 import org.doublelong.breakout.BreakoutGame;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 
public class MenuScreen implements Screen
 {
 	private final BreakoutGame game;
 	private final SpriteBatch batch;
 	private final BitmapFont font;
 
 	public MenuScreen(BreakoutGame game)
 	{
 		this.game = game;
 		this.batch = new SpriteBatch();
 		this.font = new BitmapFont();
 	}
 
 	@Override
 	public void render(float delta)
 	{
 		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		TextButton button = new TextButton("Hello", new Skin());
 
 		//this.game.startBreakoutGame();
 	}
 
 	@Override
 	public void resize(int width, int height)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose()
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 }
