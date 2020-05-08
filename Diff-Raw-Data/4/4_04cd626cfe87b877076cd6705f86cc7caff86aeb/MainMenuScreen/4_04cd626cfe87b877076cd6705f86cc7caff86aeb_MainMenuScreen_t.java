 package com.tacoid.pweek.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.tacoid.pweek.MusicPlayer;
 import com.tacoid.pweek.Pweek;
 import com.tacoid.pweek.ScoreManager;
 import com.tacoid.pweek.SoundPlayer;
 import com.tacoid.pweek.MusicPlayer.MusicType;
 import com.tacoid.pweek.Pweek.ScreenOrientation;
 import com.tacoid.pweek.ScoreManager.GameType;
 import com.tacoid.pweek.SoundPlayer.SoundType;
 import com.tacoid.pweek.actors.BackgroundActor;
 import com.tacoid.pweek.actors.MusicButtonActor;
 import com.tacoid.pweek.actors.SoundButtonActor;
 import com.tacoid.pweek.actors.SwingMenu;
 import com.tacoid.tracking.TrackingManager;
 
 
 public class MainMenuScreen implements Screen, InputProcessor {
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static MainMenuScreen instance = null;
 	private Stage stage;
 	private SwingMenu menu;
 	int i = 0;
 	
 	private MainMenuScreen() {
 		//init();
 	}
 	
 	private void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.setX(x);
 		button.setY(y);
 	}
 	
 	public void init() {
 		MusicPlayer.getInstance().setVolume(0.8f);
 		MusicPlayer.getInstance().playMusic(MusicType.MAIN, true);
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		
 		menu = new SwingMenu(ScreenOrientation.LANDSCAPE);
 		
 		menu.initBegin("main");
 		{
 			/* VERUS BUTTON */
 			TextureRegion versusRegion = Pweek.getInstance().atlasPlank.findRegion("versus");
 			menu.addButton(new VersusButton(versusRegion, versusRegion));
 			
 			/* SOLO BUTTON */
 			TextureRegion playRegion =  Pweek.getInstance().atlasPlank.findRegion("solo");
 			menu.addButton(new SoloButton(playRegion, playRegion));
 			
 			/* CHRONO BUTTON */
 			TextureRegion chronoRegion = Pweek.getInstance().atlasPlank.findRegion("chrono");
 			menu.addButton(new ChronoButton(chronoRegion, chronoRegion));
 			
 			/* Exit BUTTON */
 			TextureRegion exitRegion = Pweek.getInstance().atlasPlank.findRegion("quitter");
 			menu.addButton(new ExitButton(exitRegion, exitRegion));
 		}	
 		menu.initEnd();
 		
 		menu.initBegin("versus");
 		{
 			/* EASY BUTTON */
 			TextureRegion easyRegion =  Pweek.getInstance().atlasPlank.findRegion("easy");
 			menu.addButton(new LevelButton(0, easyRegion, easyRegion));
 			
 			/* MEDIUM BUTTON */
 			TextureRegion mediumRegion = Pweek.getInstance().atlasPlank.findRegion("medium");
 			menu.addButton(new LevelButton(1, mediumRegion, mediumRegion));
 			
 			/* HARD BUTTON */
 			TextureRegion hardRegion = Pweek.getInstance().atlasPlank.findRegion("hard");
 			menu.addButton(new LevelButton(2, hardRegion, hardRegion));
 			
 			/* VERY HARD BUTTON */
 			TextureRegion vhardRegion = Pweek.getInstance().atlasPlank.findRegion("veryhard");
 			menu.addButton(new LevelButton(3, vhardRegion, vhardRegion));
 		}	
 		menu.initEnd();
 		
 		stage.addActor(menu);
 		addButton(MusicButtonActor.createMusicButton(),VIRTUAL_WIDTH-64, VIRTUAL_HEIGHT-64);
 		addButton(SoundButtonActor.createSoundButton(),VIRTUAL_WIDTH-2*64-10, VIRTUAL_HEIGHT-64);
 		
 		menu.hideInstant();
 		menu.show("main");
 		
 		stage.addActor(new PweekLogo());
 		
 	
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
 		Pweek.getInstance().getHandler().setPortrait(false);
 		Pweek.getInstance().getHandler().showAds(true);
 		resize(0, 0);
 		Gdx.input.setInputProcessor(this);
 	}
 	
 	private class PweekLogo extends Actor {
 		
 		private Sprite sprite;
 		
 		public PweekLogo() {
 			sprite = new Sprite( Pweek.getInstance().atlasPlank.findRegion("pweek"));
 			sprite.setPosition(VIRTUAL_WIDTH/2 - sprite.getWidth()/2, VIRTUAL_HEIGHT- sprite.getHeight()-70);
 		}
 		
 		@Override
 		public void draw(SpriteBatch batch, float arg1) {
 			sprite.draw(batch);
 		}
 
 		@Override
 		public Actor hit(float arg0, float arg1, boolean touchable) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 		
 	}
 
 	private class SoloButton extends Button{
 		public SoloButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown));
 			
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 				
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					GameSoloScreen.getInstance().init();
 					TrackingManager.getTracker().trackEvent("gameplay", "game_start", "solo", null);
 					Pweek.getInstance().setScreen(GameSoloScreen.getInstance());
 				}
 			});
 		}
 	}
 	
 	private class VersusButton extends Button{
 		public VersusButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown));
 
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 				
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					menu.switchMenuAnimated("versus");
 				}
 				
 			});
 		}
 	}
 	
 	private class ChronoButton extends Button{
 		public ChronoButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown));
 
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 				
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					// TODO Auto-generated method stub
 					GameTimeAttackScreen.getInstance().init();
 					TrackingManager.getTracker().trackEvent("gameplay", "game_start", "chrono", null);
 					Pweek.getInstance().setScreen(GameTimeAttackScreen.getInstance());
 				}
 			});
 		}
 	}
 	
 	private class ExitButton extends Button{
 		public ExitButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown));
 			
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 				
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					TrackingManager.getTracker().trackEvent("UI", "button_click", "quit", null);
 					Gdx.app.exit();
 				}
 			});
 		}
 	}
 	
 	private class LevelButton extends Button{
 		private int level;
 		private Sprite redCross;
 		
 		public LevelButton(final int level, TextureRegion regionUp, TextureRegion regionDown) {
 			super(new TextureRegionDrawable(regionUp), new TextureRegionDrawable(regionDown));
 			redCross = new Sprite(Pweek.getInstance().atlasPlank.findRegion("redcross"));
			this.level = level;
 			
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 				
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					if(ScoreManager.getInstance().isLevelUnlocked(GameType.VERSUS_IA, level)) {
 						GameVersusScreen.getInstance().setLevel(level);
 						GameVersusScreen.getInstance().init();
 						TrackingManager.getTracker().trackEvent("gameplay", "game_start", "versus_"+level, null);
 						Pweek.getInstance().getHandler().showAds(false);
 						Pweek.getInstance().setScreen(GameVersusScreen.getInstance());
 						menu.switchMenu("main");
 					}
 				}
 			});
 		}
				
 		@Override
 		public void draw(SpriteBatch batch, float parentAlpha) {
 			super.draw(batch, parentAlpha);
 			if(!ScoreManager.getInstance().isLevelUnlocked(GameType.VERSUS_IA, level)) {
 				redCross.setX(this.getX() + 50);
 				redCross.setY(this.getY() + 30);
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
 			} else {
 				TrackingManager.getTracker().trackEvent("UI", "button_click", "android_quit", null);
 				Gdx.app.exit();
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
 	public boolean mouseMoved(int x, int y) {
 		return stage.mouseMoved(x, y);
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		return stage.touchUp(x, y, pointer, button);
 	}
 }
