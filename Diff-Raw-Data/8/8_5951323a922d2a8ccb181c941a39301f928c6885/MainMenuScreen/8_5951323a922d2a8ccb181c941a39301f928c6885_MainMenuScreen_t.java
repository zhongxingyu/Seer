 package com.tacoid.puyopuyo.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.tacoid.puyopuyo.MusicPlayer;
 import com.tacoid.puyopuyo.PuyoPuyo;
 import com.tacoid.puyopuyo.ScoreManager;
 import com.tacoid.puyopuyo.SoundPlayer;
 import com.tacoid.puyopuyo.MusicPlayer.MusicType;
 import com.tacoid.puyopuyo.PuyoPuyo.ScreenOrientation;
 import com.tacoid.puyopuyo.ScoreManager.GameType;
 import com.tacoid.puyopuyo.SoundPlayer.SoundType;
 import com.tacoid.puyopuyo.actors.BackgroundActor;
 import com.tacoid.puyopuyo.actors.MusicButtonActor;
 import com.tacoid.puyopuyo.actors.SoundButtonActor;
 import com.tacoid.puyopuyo.actors.SwingMenu;
 
 
 public class MainMenuScreen implements Screen, InputProcessor {
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static MainMenuScreen instance = null;
 	private Stage stage;
 	private SwingMenu menu;
 	
 	private MainMenuScreen() {
 		init();
 	}
 	
 	private void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.x = x;
 		button.y = y;
 	}
 	
 	private void init() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		
 		menu = new SwingMenu(ScreenOrientation.LANDSCAPE);
 		
 		menu.initBegin("main");
 		{
 			/* SOLO BUTTON */
 			TextureRegion playRegion =  PuyoPuyo.getInstance().atlasPlank.findRegion("solo");
 			menu.addButton(new SoloButton(playRegion, playRegion));
 			
 			/* VERUS BUTTON */
 			TextureRegion versusRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("versus");
 			menu.addButton(new VersusButton(versusRegion, versusRegion));
 			
 			/* CHRONO BUTTON */
 			TextureRegion chronoRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("chrono");
 			menu.addButton(new ChronoButton(chronoRegion, chronoRegion));
 			
 			/* Exit BUTTON */
 			TextureRegion exitRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("quitter");
 			menu.addButton(new ExitButton(exitRegion, exitRegion));
 		}	
 		menu.initEnd();
 		
 		menu.initBegin("versus");
 		{
 			/* EASY BUTTON */
 			TextureRegion easyRegion =  PuyoPuyo.getInstance().atlasPlank.findRegion("easy");
 			menu.addButton(new LevelButton(0, easyRegion, easyRegion));
 			
 			/* MEDIUM BUTTON */
 			TextureRegion mediumRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("medium");
 			menu.addButton(new LevelButton(1, mediumRegion, mediumRegion));
 			
 			/* HARD BUTTON */
 			TextureRegion hardRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("hard");
 			menu.addButton(new LevelButton(2, hardRegion, hardRegion));
 			
 			/* VERY HARD BUTTON */
 			TextureRegion vhardRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("veryhard");
 			menu.addButton(new LevelButton(3, vhardRegion, vhardRegion));
 		}	
 		menu.initEnd();
 		
 		stage.addActor(menu);
 		addButton(MusicButtonActor.createMusicButton(),VIRTUAL_WIDTH-64, VIRTUAL_HEIGHT-64);
 		addButton(SoundButtonActor.createSoundButton(),VIRTUAL_WIDTH-2*64-10, VIRTUAL_HEIGHT-64);
 
 		menu.show("main");
 		
 		MusicPlayer.getInstance().setVolume(0.8f);
 		MusicPlayer.getInstance().playMusic(MusicType.MAIN, true);
 		
 	}
 	
 	static public MainMenuScreen getInstance()
 	{
 			if (instance == null) {
 				instance = new MainMenuScreen();
 			}
 			return instance;
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.app.log("MainScreen", "paused");
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
 	public void render(float arg0) {    
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		stage.draw();
 		stage.act(Gdx.graphics.getDeltaTime());
 		
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		stage.setViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false);
 		stage.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
 	}
 	
 	@Override
 	public void resume() {
 		show();
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(this);
 	}
 
 	private class SoloButton extends Button{
 		public SoloButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			return true;
 		}
 		public void touchUp(float x, float y, int pointer) {
 			GameSoloScreen.getInstance().init();
 			PuyoPuyo.getInstance().setScreen(GameSoloScreen.getInstance());
 		}
 	}
 	
 	private class VersusButton extends Button{
 		public VersusButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			return true;
 		}
 		public void touchUp(float x, float y, int pointer) {
 			menu.switchMenuAnimated("versus");
 			//GameVersusScreen.getInstance().init();
 			//PuyoPuyo.getInstance().setScreen(GameVersusScreen.getInstance());
 		}
 	}
 	
 	private class ChronoButton extends Button{
 		public ChronoButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			return true;
 		}
 		public void touchUp(float x, float y, int pointer) {
 			GameTimeAttackScreen.getInstance().init();
 			PuyoPuyo.getInstance().setScreen(GameTimeAttackScreen.getInstance());
 		}
 	}
 	
 	private class ExitButton extends Button{
 		public ExitButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			return true;
 		}
 		public void touchUp(float x, float y, int pointer) {
 			Gdx.app.exit();
 		}
 	}
 	
 	private class LevelButton extends Button{
 		private int level;
 		private Sprite redCross;
 		
 		public LevelButton(int level, TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			this.level = level;
 			redCross = new Sprite(PuyoPuyo.getInstance().atlasPlank.findRegion("redcross"));
 		}
 
 		@Override
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			return true;
 		}
 		public void touchUp(float x, float y, int pointer) {
 			if(ScoreManager.getInstance().isLevelUnlocked(GameType.VERSUS_IA, level)) {
 				GameVersusScreen.getInstance().setLevel(level);
 				GameVersusScreen.getInstance().init();
 				PuyoPuyo.getInstance().setScreen(GameVersusScreen.getInstance());
 				menu.switchMenu("main");
 			}
 		}
 		
 		public void draw (SpriteBatch batch, float parentAlpha) {
 			super.draw(batch, parentAlpha);
 			if(!ScoreManager.getInstance().isLevelUnlocked(GameType.VERSUS_IA, level)) {
 				redCross.setX(this.x+50);
 				redCross.setY(this.y+30);
 				redCross.draw(batch);
 			}
 		}
 	}
 
 	@Override
 	public boolean keyDown(int key) {
 		switch (key) {
 		case Keys.BACK:
 			if(!menu.getCurrentMenu().equals("main")) {
 				menu.switchMenuAnimated("main");
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		return stage.touchDown(x, y, pointer, button);
 	}
 
 	@Override
 	public boolean touchDragged(int arg0, int arg1, int arg2) {
 		return stage.touchDragged(arg0, arg1, arg2);
 	}
 
 	@Override
 	public boolean touchMoved(int x, int y) {
 		return stage.touchMoved(x, y);
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		return stage.touchUp(x, y, pointer, button);
 	}
 }
