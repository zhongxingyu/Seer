 package com.glowman.spaceunit;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 import android.util.Log;
 
 import com.glowman.spaceunit.game.behavior.AsteroidsBehavior;
 import com.glowman.spaceunit.game.core.Button;
 import com.glowman.spaceunit.game.core.CameraHelper;
 import com.glowman.spaceunit.game.core.CameraHelper.ViewportMode;
 import com.glowman.spaceunit.game.core.FPSViewer;
 
 
 public class MainScreen implements Screen {
 	
 	private Game _game;
 
 	SpriteBatch _spriteBatch;
 	
 	private OrthographicCamera _camera;
 	private Button _playBtnRun;
 	private Button _playBtnShoot;
 	private Sprite _bkg;
 	
 	private AsteroidsBehavior _behavior;
 
 	public MainScreen(Game game) {
 		_game = game;
 
 		_camera = CameraHelper.createCamera2(ViewportMode.STRETCH_TO_SCREEN, Assets.VIRTUAL_WIDTH, Assets.VIRTUAL_HEIGHT, Assets.pixelDensity);
 		CoordinatesTranslator.init(_camera);
 		_spriteBatch = new SpriteBatch();
 		_spriteBatch.setProjectionMatrix(_camera.combined);
 		
 		_bkg = new Sprite(Assets.bkg);
 		_playBtnRun = new Button(Assets.getPlayRunRegion(1), Assets.getPlayRunRegion(2));
 		_playBtnShoot = new Button(Assets.getPlayShootRegion(1), Assets.getPlayShootRegion(2));
 		
 		_behavior = new AsteroidsBehavior(15, _spriteBatch);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		
 	}
 
 	@Override
 	public void hide() {
 		this.clear();
 		Gdx.input.setInputProcessor(null);
 	}
 
 	float scale;
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(new MenuTouchListener(_game, _camera, _playBtnRun, _playBtnShoot));
 		
 		_bkg.setSize(Assets.VIRTUAL_WIDTH, Assets.VIRTUAL_HEIGHT);
 		
 		_playBtnRun.setSize(Assets.playBtnWidth, Assets.playBtnHeight);
		_playBtnRun.setScale(0.75f);
		_playBtnRun.setX(Assets.VIRTUAL_WIDTH/2 - _playBtnRun.getWidth() - 5);
 		_playBtnRun.setY((Assets.VIRTUAL_HEIGHT - _playBtnRun.getHeight())/2);
 		
 		_playBtnShoot.setSize(Assets.playBtnWidth, Assets.playBtnHeight);
		_playBtnShoot.setScale(0.75f);
		_playBtnShoot.setX(Assets.VIRTUAL_WIDTH/2 + 5);
 		_playBtnShoot.setY((Assets.VIRTUAL_HEIGHT - _playBtnShoot.getHeight())/2);
 		
 		Assets.gameFont.setColor(Color.ORANGE);
 		scale = Assets.gameFont.getScaleX()* 2;
 		Assets.gameFont.setScale(scale);
 	}
 
 	@Override
 	public void render(float deltaTime) {
 		this.clear();
 		
 		_spriteBatch.begin();
 		
 		/*
 		String str = "Hello World!";
 		TextBounds bounds = Assets.gameFont.getBounds(str);
 		float x = Assets.VIRTUAL_WIDTH/2 - bounds.width / 2.0f;
 		float y = Assets.VIRTUAL_HEIGHT/2 + bounds.height / 2.0f;
 		//Assets.gameFont.drawWrapped(_spriteBatch, str, x, y, Assets.VIRTUAL_HEIGHT, HAlignment.CENTER);
 		Assets.gameFont.draw(_spriteBatch, str, x, y);
 		*/
 		
 		_bkg.draw(_spriteBatch);
 		_behavior.tick(deltaTime);
 		_playBtnRun.draw(_spriteBatch);
 		_playBtnShoot.draw(_spriteBatch);
 		
 		
 		FPSViewer.draw(_spriteBatch);
 		_spriteBatch.end();
 	}
 
 
 	@Override
 	public void pause() {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 		Log.d("hz", "dispose!!");
 		this.clear();
 	}
 
 	private void clear() {
 		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 
 }
