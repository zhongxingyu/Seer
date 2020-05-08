 package com.tacoid.pweek.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.GLCommon;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.tacoid.pweek.Controller;
 import com.tacoid.pweek.Pweek;
 import com.tacoid.pweek.SoundPlayer;
 import com.tacoid.pweek.Pweek.ScreenOrientation;
 import com.tacoid.pweek.ScoreManager.GameType;
 import com.tacoid.pweek.SoundPlayer.SoundType;
 import com.tacoid.pweek.actors.BackgroundActor;
 import com.tacoid.pweek.actors.ControlerActor;
 import com.tacoid.pweek.actors.GameOverActor;
 import com.tacoid.pweek.actors.GridActor;
 import com.tacoid.pweek.actors.LandscapePanelActor;
 import com.tacoid.pweek.actors.MusicButtonActor;
 import com.tacoid.pweek.actors.NextPieceActor;
 import com.tacoid.pweek.actors.PauseMenu;
 import com.tacoid.pweek.actors.ScoreActor;
 import com.tacoid.pweek.actors.SoundButtonActor;
 import com.tacoid.pweek.actors.StartActor;
 import com.tacoid.pweek.actors.GameOverActor.GameOverType;
 import com.tacoid.pweek.logic.GameLogic;
 import com.tacoid.pweek.logic.IA;
 import com.tacoid.pweek.logic.IAEasy;
 import com.tacoid.pweek.logic.IAHard;
 import com.tacoid.pweek.logic.State;
 import com.tacoid.tracking.TrackingManager;
 
 public class GameVersusScreen implements GameScreen {
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static GameVersusScreen instance = null;
 	private Stage stage;
 	private GameLogic gameLogic;
 	private GameLogic gameLogicIA;
 	private GridActor gridActor;
 	private GridActor gridActorIA;
 	private NextPieceActor nextPieceActor;
 	private NextPieceActor nextPieceActorIA;
 	protected float elapsedTime;
 
 	private int puyoSize = 48;
 	
 	private IA ia;
 	private InputProcessor controller;
 	private ControlerActor controllerActor;
 	private PauseMenu pauseMenu;
 	private PauseButton pauseButton;
 	private StartActor startActor;
 	
 	private GameOverActor gameOver;
 	private boolean gamePaused;
 	
 	private int level;
 	
 	private void addButton(Button button, int x, int y) {
 		stage.addActor(button);
 		button.x = x;
 		button.y = y;
 	}
 	
 	public class PauseButton extends Button {
 
 		public PauseButton(TextureRegion region) {
 			super(region);
 		}
 		
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 			pauseMenu.show();
 			return true;
 		}
 
 	}
 
 	private GameVersusScreen() {
 		gameLogic = new GameLogic();
 		gameLogicIA = new GameLogic();
 		gameLogic.setOpponent(gameLogicIA);
 		gameLogicIA.setOpponent(gameLogic);
 		elapsedTime = 0;
 	}
 	
 	public int getPuyoSize() {
 		return puyoSize;
 	}
 	
 	public void setLevel(int level) {
 		this.level = level;
 		if (level == 0) {
 			gameLogic.setInitialSpeed(0.5f);
 			gameLogicIA.setInitialSpeed(0.5f);
 			gameLogic.setNColors(3);
 			gameLogicIA.setNColors(4);
 			ia = new IAEasy(gameLogicIA);
 		} else if (level == 1) {
 			gameLogic.setInitialSpeed(0.5f);
 			gameLogicIA.setInitialSpeed(0.5f);
 			gameLogic.setNColors(4);
 			gameLogicIA.setNColors(4);
 			ia = new IAHard(gameLogicIA);
 		} else if (level == 2) {
 			gameLogic.setInitialSpeed(0.3f);
 			gameLogicIA.setInitialSpeed(0.3f);
 			gameLogic.setNColors(4);
 			gameLogicIA.setNColors(4);
 			gameLogicIA.setCheatMode(true);
 			ia = new IAHard(gameLogicIA);
 		} else if (level == 3) {
 			gameLogic.setInitialSpeed(0.15f);
 			gameLogicIA.setInitialSpeed(0.3f);
 			gameLogic.setNColors(5);
 			gameLogicIA.setNColors(5);
 			gameLogicIA.setCheatMode(true);
 			ia = new IAHard(gameLogicIA);
 		}
 	}
 	public int getLevel() {
 		return level;
 	}
 
 	public void initGraphics() {
 
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		controller = new Controller(gameLogic, this, stage);
 		gameOver = null;
 
 		gridActor = new GridActor(gameLogic, 296, 26, 54, puyoSize);
 		nextPieceActor = new NextPieceActor(gameLogic, 95, 500, puyoSize);
 		ScoreActor scoreActor = new ScoreActor(gameLogic, 550, 743);
 
 		gridActorIA = new GridActor(gameLogicIA, 650, 26, 54, puyoSize);
 		nextPieceActorIA = new NextPieceActor(gameLogicIA, 1066, 500, puyoSize);
 		ScoreActor scoreActorIA = new ScoreActor(gameLogicIA, 830, 743);
 		
 		TextureRegion pauseRegion = new TextureRegion(Pweek.getInstance().atlasBouttons.findRegion("pause_button"));
 
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		stage.addActor(new LandscapePanelActor());
 		
 		stage.addActor(gridActor);
 		stage.addActor(nextPieceActor);
 		stage.addActor(scoreActor);
 		stage.addActor(gridActorIA);
 		stage.addActor(nextPieceActorIA);
 		stage.addActor(scoreActorIA);
 
 		controllerActor = new ControlerActor(ScreenOrientation.LANDSCAPE, gameLogic);
 		stage.addActor(controllerActor);
 		
 		pauseButton = new PauseButton(pauseRegion);
 		addButton(pauseButton,10,VIRTUAL_HEIGHT-10-pauseRegion.getRegionHeight());
 		addButton(MusicButtonActor.createMusicButton(),VIRTUAL_WIDTH-64, VIRTUAL_HEIGHT-64);
 		addButton(SoundButtonActor.createSoundButton(),VIRTUAL_WIDTH-2*64-10, VIRTUAL_HEIGHT-64);
 		
 	
 		gameOver = new GameOverActor(this, VIRTUAL_WIDTH/2, 8*VIRTUAL_HEIGHT/9-25);
 		stage.addActor(gameOver);
		gameOver.hide();
 		
 		pauseMenu = new PauseMenu(this, ScreenOrientation.LANDSCAPE, !gamePaused);
 		stage.addActor(pauseMenu);
 		
		boolean show = true;
 		if (startActor != null) {
 			show = startActor.visible;
 		}
 
 		startActor = new StartActor(this);
 		if (show) {
 			startActor.show();
 		} else {
 			startActor.visible = false;
 			startActor.touchable = false;
 			if (gamePaused) {
 				pauseMenu.show();
 			}
 		}
 		stage.addActor(startActor);
 		
 
 	}
 
 	public static GameVersusScreen getInstance() {
 		if (instance == null) {
 			instance = new GameVersusScreen();
 		}
 		return instance;
 	}
 	
 	public static void freeInstance() {
 		instance = null;
 	}
 	
 	@Override
 	public void dispose() {
 		Gdx.app.log("Versus", "paused");
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		if (!gamePaused && gameLogic.getState() != State.LOST) {
 			pauseMenu.show();
 		}
 	}
 
 	@Override
 	public void render(float delta) {
 		GLCommon gl = Gdx.gl;
 		
 		if (gameLogic.getState() != State.LOST
 				&& gameLogicIA.getState() != State.LOST) {
 			if (!gamePaused) {
 				this.elapsedTime += delta;
 			}
 			
 			// Update model
 			gameLogic.update(delta);
 			gameLogicIA.update(delta);
 			ia.update(delta);
 			if (gameLogic.getState() == State.LOST
 					|| gameLogicIA.getState() == State.LOST) {
 				gameOver();
 			}
 		}
 
 		// clear previous frame
 		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		stage.act(delta);
 		stage.draw();
 
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
 		Pweek.getInstance().getHandler().showAds(!startActor.visible && (gamePaused || gameOver.visible));
 		resize(0, 0);
 		Gdx.input.setInputProcessor(controller);
 	}
 
 	@Override
 	public void init() {
 		elapsedTime = 0;
 		gameLogic.init();
 		gameLogicIA.init();
 		gameOver.hide();
 		startActor.show();
 	}
 
 	private void gameOver() {
 		if(gameLogic.getState() == State.LOST){
 			gameOver.show(GameOverType.LOSE);
 		} else {
 			gameOver.show(GameOverType.WIN);
 		}
 		controllerActor.touchable = false;
 		pauseButton.touchable = false;
 	}
 	
 	@Override
 	public void gamePause() {
 		gamePaused = true;
 		gameLogic.pause();
 		gameLogicIA.pause();
 		gridActor.visible = false;
 		gridActorIA.visible = false;
 		nextPieceActor.visible = false;
 		nextPieceActorIA.visible = false;
 		controllerActor.touchable = false;
 		pauseButton.touchable = false;
 	}
 
 	@Override
 	public void gameResume() {
 		gamePaused = false;
 		gameLogic.resume();
 		gameLogicIA.resume();
 		gridActor.visible = true;
 		gridActorIA.visible = true;
 		nextPieceActor.visible = true;
 		nextPieceActorIA.visible = true;
 		controllerActor.touchable = true;
 		pauseButton.touchable = true;
 	}
 
 	@Override
 	public ScreenOrientation getOrientation() {
 		return ScreenOrientation.LANDSCAPE;
 	}
 
 	@Override
 	public float getHeight() {
 		return VIRTUAL_HEIGHT;
 	}
 
 	@Override
 	public float getWidth() {
 		return VIRTUAL_WIDTH;
 	}
 	
 	@Override
 	public GameType getGameType() {
 		return GameType.VERSUS_IA;
 	}
 
 	@Override
 	public int getScore() {
 		if (gameLogic.getState() == State.LOST) {
 			return 0;
 		} else {
 			return gameLogic.getScore() + getTimeBonus();
 		}
 	}
 	
 	public int getTimeBonus() {
 		return Math.max(0, 25000 - (int)(elapsedTime * 50));
 	}
 
 
 	@Override
 	public float getElapsedTime() {
 		return elapsedTime;
 	}
 	
 	@Override
 	public void quit() {
 		pauseMenu.hide();
 		GameType type = getGameType();
 		TrackingManager.getTracker().trackEvent("UI", "button_click",type.toString()+" Level "+ getLevel() +" quit before end", null);
 		Pweek.getInstance().setScreen(MainMenuScreen.getInstance());
 	}
 }
