 package com.tacoid.puyopuyo;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.tacoid.puyopuyo.MusicPlayer.MusicType;
 import com.tacoid.puyopuyo.PuyoPuyo.ScreenOrientation;
 import com.tacoid.puyopuyo.SoundPlayer.SoundType;
 import com.tacoid.puyopuyo.actors.BackgroundActor;
 import com.tacoid.puyopuyo.actors.MusicButtonActor;
 import com.tacoid.puyopuyo.actors.SwingMenu;
 
 
 public class MainMenuScreen implements Screen {
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static MainMenuScreen instance = null;
 	private Stage stage;
 	
 	private void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.x = x;
 		button.y = y;
 	}
 	
 	public MainMenuScreen() {
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		
 		SwingMenu menu = new SwingMenu(ScreenOrientation.LANDSCAPE);
 		
 		menu.initBegin();
 		{
 			/* SOLO BUTTON */
 			TextureRegion playRegion =  PuyoPuyo.getInstance().atlasPlank.findRegion("solo-fr");
 			menu.addButton(new SoloButton(playRegion, playRegion));
 			
 			/* VERUS BUTTON */
 			TextureRegion versusRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("versus-fr");
 			menu.addButton(new VersusButton(versusRegion, versusRegion));
 			
 			/* CHRONO BUTTON */
 			TextureRegion chronoRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("chrono-fr");
 			menu.addButton(new ChronoButton(chronoRegion, chronoRegion));
 			
 			/* Exit BUTTON */
 			TextureRegion exitRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("quitter-fr");
 			menu.addButton(new ExitButton(exitRegion, exitRegion));
 		}	
 		menu.initEnd();
 		
 		stage.addActor(menu);
 		addButton(MusicButtonActor.createMusicButton(),VIRTUAL_WIDTH-42, VIRTUAL_HEIGHT-42);
 
 		menu.show();
 		
 		MusicPlayer.getInstance().setVolume(0.0f);
 		MusicPlayer.getInstance().playMusic(MusicType.MAIN, true);
 		
 		GameSoloScreen.getInstance();
 		GameVersusScreen.getInstance();
 		GameTimeAttackScreen.getInstance();
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
 		// TODO Auto-generated method stub
 		
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
		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(stage);
 		
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
 			GameVersusScreen.getInstance().init();
 			PuyoPuyo.getInstance().setScreen(GameVersusScreen.getInstance());
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
 
 }
