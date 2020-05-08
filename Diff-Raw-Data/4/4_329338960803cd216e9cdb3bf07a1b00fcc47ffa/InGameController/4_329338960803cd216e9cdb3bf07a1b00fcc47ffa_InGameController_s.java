 package com.dat255_group3.controller;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 import com.dat255_group3.model.InGame;
 import com.dat255_group3.utils.CoordinateConverter;
 import com.dat255_group3.utils.GyroUtils;
 import com.dat255_group3.utils.InputStage;
 import com.dat255_group3.utils.InputStage.OnHardKeyListener;
 import com.dat255_group3.view.InGameView;
 
 public class InGameController implements Screen {
 
 	private InGame inGame;
 	private InGameView inGameView;
 	private WorldController worldController;
 	private MyGdxGameController myGdxGameController;
 	private float timeStep = 1.0f / 10.0f;
 	private final int velocityIterations = 6;
 	private final int positionIterations = 2;
 	private TiledMap map;
 	private OrthographicCameraController cameraController;
 	private Box2DDebugRenderer renderer = new Box2DDebugRenderer(true, true,
 			true, true, true, true);
 	private boolean gameOver;
 	private boolean isCountingDown = true;
 	private InputStage stage;
 
 	public InGameController(MyGdxGameController myGdxGameController) {
 		this.myGdxGameController = myGdxGameController;
 		this.cameraController = new OrthographicCameraController();
 		this.cameraController.create();
 		this.stage = new InputStage(CoordinateConverter.getCameraWidth(),
 				CoordinateConverter.getCameraHeight(), true);
 	}
 
 	@Override
 	public void render(float delta) {
 		// Shows a white screen
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		// Update the stage actors
 		stage.act(delta);
 		inGame.setDelayTime(inGame.getDelayTime() + delta);
 
 		
 		/*
 		 * Checks if the game is over or won
 		 */
 		if (hasWon()) {
 			this.gameOver = false;
 			gameOver();
 		}
 
 		if (this.worldController.getCharacterController().getCharacter()
 				.isDead()) {
 			this.gameOver = true;
 			gameOver();
 		}
 
 		// check collision with the closest cookie
 		worldController.checkNextCookie();
 
 		// draws the world and its components
 		this.inGameView.draw(this.worldController.getWorldView(),
 				this.worldController.getCharBody(), this.worldController
 						.getCharacterController().getCharacterView(),
 				this.worldController.getCookieController().getCookieView(),
 				this.worldController.getEnemy(), worldController.getWorld()
 						.getTime(), worldController.getWorld()
 						.getCookieCounter(), gameOver);
 
 		
 		stage.draw();
 
 		/*
 		 * Checks whether the screen has been touched. If so, a method which
 		 * will make the character jump is invoked.
 		 */
 		if (Gdx.input.isTouched()) {
 			worldController.getCharacterController().tryToJump();
 		}
 		// Count Down
 		if (isCountingDown) {
 			if (inGame.getDelayTime() <= 1.0) {
 				Gdx.app.log("InGameContoller", "Count down: 3");
 				inGameView.drawCountDownNbr(inGame.getDelayTime());
 
 			} else if (inGame.getDelayTime() <= 2.0) {
 				inGameView.drawCountDownNbr(inGame.getDelayTime());
 				Gdx.app.log("InGameContoller", "Count down: 2");
 
 			} else if (inGame.getDelayTime() <= 3.0) {
 				inGameView.drawCountDownNbr(inGame.getDelayTime());
 				Gdx.app.log("InGameContoller", "Count down: 1");
 
 			} else {
 				isCountingDown = false;
 			}
 		} else {
 			update(delta);
 		}
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(stage);
 		Gdx.input.setCatchBackKey(true);
 
 		/*
 		 * Checks whether the backbutton has been pressed. If so, a
 		 * pausepop-up-screen will be shown.
 		 */
 		stage.setHardKeyListener(new OnHardKeyListener() {
 			@Override
 			public void onHardKey(int keyCode, int state) {
 				if (keyCode == Keys.BACK && state == 1) {
 					myGdxGameController.setScreen(myGdxGameController
 							.getPauseScreen());
 				}
 			}
 		});
 
 		// Pause button
 		ImageButtonStyle pauseButtonStyle = new ImageButtonStyle();
 		pauseButtonStyle.up = myGdxGameController.getScreenUtils()
 				.getCircularSkin().getDrawable("play.up");
 		pauseButtonStyle.down = myGdxGameController.getScreenUtils()
 				.getCircularSkin().getDrawable("play.down");
 		pauseButtonStyle.checked = myGdxGameController.getScreenUtils()
 				.getCircularSkin().getDrawable("pause.up");
 		ImageButton pauseButton = new ImageButton(pauseButtonStyle);
 		pauseButton.setPosition(CoordinateConverter.getCameraWidth() - 130,
 				CoordinateConverter.getCameraHeight() - 70);
 		// pauseButton.pad(20);
 		stage.addActor(pauseButton);
 		pauseButton.toggle();
 		pauseButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				InGameController.this.pause();
 			}
 		});
 
 		if (myGdxGameController.getMyGdxGame().getIsGameStarted() == false) {
 			this.cameraController = new OrthographicCameraController();
 			this.cameraController.create();
 			loadMap();
 			this.inGameView = new InGameView(map, cameraController.getCamera(), this.myGdxGameController.getMyGdxGame().getCurrentLevel());
 			this.inGame = new InGame();
 			this.worldController = new WorldController(this, inGame.getSpeedM());
 			this.gameOver = false;
 			myGdxGameController.getMyGdxGame().setIsGameStarted(true);
 		}
 		this.cameraController.resume();
 		inGame.setDelayTime(0);
 		isCountingDown = true;
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 		myGdxGameController.setScreen(myGdxGameController.getPauseScreen());
 		cameraController.pause();
 	}
 
 	@Override
 	public void resume() {
 		cameraController.resume();
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void dispose() {
 		try {
 			map.dispose();
 			cameraController.dispose();
 			renderer.dispose();
 		} catch (GdxRuntimeException e) {
 			Gdx.app.log("InGameController", "Exception", e);
 		} catch (Exception e) {
 		}
 
 	}
 
 	public InGame getInGame() {
 		return inGame;
 	}
 
 	public InGameView getInGameView() {
 		return inGameView;
 	}
 
 	public TiledMap getMap() {
 		return map;
 	}
 
 	public OrthographicCamera getCamera() {
 		return cameraController.getCamera();
 	}
 
 	public boolean hasWon() {
 		return worldController.getCharacterController().getCharacter()
 				.getPosition().x >= worldController.getFinishLineX();
 	}
 
 	public void save() {
 		//  if score > high score for the current level
 		if (this.myGdxGameController.getPlayerController().getPlayer().getScore() > 
 			this.myGdxGameController.getPlayerController().getPlayer()
 			.getHighScore(myGdxGameController.getMyGdxGame().getCurrentLevel())) {
 			
 			myGdxGameController.getPlayerController().getPlayer().setNewHighScore
 			(this.myGdxGameController.getMyGdxGame().getCurrentLevel(),
 					this.myGdxGameController.getPlayerController().getPlayer().getScore());
 			
 			Gdx.app.log("InGameControler", "new hs: " + this.myGdxGameController.getPlayerController().getPlayer().getScore());
 			Gdx.app.log("InGameControler", "get hs: " +this.myGdxGameController.getPlayerController().getPlayer().
 					getHighScore(this.myGdxGameController.getMyGdxGame().getCurrentLevel()));
 		}
 	}
 
 	public void reset() {
 		// reset
 	}
 
 	public void update(float delta) {
 
 		this.timeStep = delta;
 
 		// update the physics
 		this.worldController.getPhysicsWorld().step(this.timeStep,
 				this.velocityIterations, this.positionIterations);
 
 		// Update the position of the camera
 		cameraController.render();
 
 		// update the time
 		worldController.getWorld().setTime(
 				worldController.getWorld().getTime() + delta);
 
 		// Check the pitch of the device and changes the speed
 		inGame.setSpeedM(1.5f * GyroUtils.gyroSteering());
 
 		// Updates the speed
 		inGame.setSpeedP(CoordinateConverter.meterToPixel(inGame.getSpeedM()
 				* delta));
 		cameraController.setSpeedP(inGame.getSpeedP());
 
 		// give character speed
 		if (this.worldController.getCharBody().getLinearVelocity().x < this.inGame.getSpeedM() 
 				&& this.worldController.getCharacterController().getCharacter().getPosition().x -
 				this.worldController.getCharacterController().getCharacter().getDeahLimit() < 400) {
 			this.worldController.getCharBody().applyForceToCenter(
 					new Vector2(1, 0), true);
 		}
 
 		// update the model position for the character
 		this.worldController.uppdatePositions(this.worldController
 				.getCharBody(), this.worldController.getCharacterController()
 				.getCharacter());
 
 		// Update the position of the death limit
 		worldController.getCharacterController().getCharacter()
 				.moveDeathLimit(inGame.getSpeedP());
 	}
 
 	public void gameOver() {
 		Gdx.app.log("Game over:", gameOver + "");
 
 		myGdxGameController.getMyGdxGame().setIsGameStarted(false);
 
 		// calculate the score
 		this.myGdxGameController
 				.getPlayerController().getPlayer()
 				.calculateScore(worldController.getWorld().getTime(),
 						worldController.getWorld().getCookieCounter(), gameOver);
 
 		save();
 		
 		// Change to gameover-screen
 		myGdxGameController.getGameOverScreen().gameOver(
 				this.myGdxGameController.getPlayerController().getPlayer()
 						.getScore(), worldController.getWorld().getTime(),
 				gameOver);
 		myGdxGameController.setScreen(myGdxGameController.getGameOverScreen());
 		if (MyGdxGameController.soundEffectsOn()) {
 			if (!gameOver) {
 				worldController.getSoundController().playVictorySound();
 			} else {
 				worldController.getSoundController().playGameOverSound();
 			}
 		}
 	}
 
 	public void loadMap() {
 		try {
 			map = new TmxMapLoader().load("worlds/map"
 					+ myGdxGameController.getMyGdxGame().getCurrentLevel()
 					+ ".tmx");
 		} catch (GdxRuntimeException e) {
 			Gdx.app.log("InGameController", "loadMap()", e);
 		}
 	}
 }
