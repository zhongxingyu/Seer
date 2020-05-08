 package com.me.mygdxgame;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.TimeUtils;
 import com.me.mygdxgame.Runner.State;
 
 public class GameScreen extends MyScreen {
	
	//public GameScreen() {
    protected static float runSpeed;
 	private Texture gameControl; 
 	private MyGdxGame game;
 	private float gameControlPosX[]; 
 	
 	public GameScreen(MyGdxGame game) {
 		this.game = game;
 		create();
 	}
 
 	public void create() {
 		runSpeed =10;
 		// Gdx.audio.newSound(Gdx.files.internal("data/waterdrop.wav"));
 		// Gdx.audio.newMusic(Gdx.files.internal("data/music/01-TRACK 2.wav"));
 		// music2 = Gdx.audio.newMusic(Gdx.files.internal("data/music/2.mp3"));
 		// manager.load("data/music/1.mp3", Music.);
 		// manager.load("data/music/2.mp3", Music.class);
 		manager.load("data/floor.png", Texture.class);
 		manager.load("data/floor.png", Texture.class);
 		manager.load("data/whitepaper.png", Texture.class);
 		manager.load("data/pause.png", Texture.class);
 		manager.update();
 		manager.finishLoading();
 		// music1 = manager.get("data/music/1.mp3", Music.class);
 		// music2 = manager.get("data/music/2.mp3", Music.class);
 		floor = manager.get("data/floor.png", Texture.class);
 		background = manager.get("data/whitepaper.png", Texture.class);
 		gameControl = manager.get("data/pause.png", Texture.class);
 		// music2.setLooping(true);
 
 		// sicmusic1.setLooping(true);
 		// rainMusic.play();
 
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, RESW, RESH);
 
 		batch = new SpriteBatch();
 		runner = new Runner(camera, FLOOR_HEIGHT);
 
 		obstacles = new ArrayList<Obstacle>();
 		lastSpawnPos = camera.position.x + RESW;
 
 		score = 0;
 		timer = 0;
 		
 		// font = new
 		// BitmapFont(Gdx.files.internal("Calibri.fnt"),Gdx.files.internal("Calibri.png"),false);
 		font = new BitmapFont();
 
 		floorPosX = new float[] { camera.position.x - RESW / 2,
 				camera.position.x + RESW / 2 };
 
 		backgroundPosX = new float[] { camera.position.x - RESW / 2,
 				camera.position.x + RESW / 2 };
 		
 		gameControlPosX = new float[] { 0, RESH - 50 };
 
 		// played = false;	
 
 	}
 	
 
 	@Override
 	public void render(float delta) {
 
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		// Moves player
 		camera.position.add(runSpeed, 0, 0);
 		camera.update();
 
 		// Moves background to appear to be moving slower
 		backgroundPosX[0] += 0.5f;
 		backgroundPosX[1] += 0.5f;
 		
 		// Moves pause button so that it stays on the screen.
 		//gameControlPosX[0] += 0.5f;
 		//gameControlPosX[1] += 0.5f;
 
 		// if (timer >= (float)15/2 && played == false) {
 		// music2.play();
 		// played = true;
 		// }
 
 		// Updates
 		runner.update();
 
 		// Checks floor positioning
 		if (floorPosX[0] < camera.position.x - RESW * 1.5) {
 			floorPosX[0] = camera.position.x + RESW / 2 - 8;
 		}
 		if (floorPosX[1] < camera.position.x - RESW * 1.5) {
 			floorPosX[1] = camera.position.x + RESW / 2 - 8;
 		}
 
 		if (backgroundPosX[0] < camera.position.x - RESW * 1.5) {
 			backgroundPosX[0] = camera.position.x + RESW / 2;
 		}
 		if (backgroundPosX[1] < camera.position.x - RESW * 1.5) {
 			backgroundPosX[1] = camera.position.x + RESW / 2;
 		}
 /*		
 		if (gameControlPosX[0] < camera.position.x - RESW * 1.5) {
 			gameControlPosX[0] = camera.position.x + RESW / 2;
 		}
 		if (gameControlPosX[1] < camera.position.x - RESW * 1.5) {
 			gameControlPosX[1] = camera.position.x + RESW / 2;
 		}
 */
 		// Rendering
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		batch.draw(background, backgroundPosX[0], 0);
 		batch.draw(background, backgroundPosX[1], 0);
 		batch.draw(floor, floorPosX[0], FLOOR_HEIGHT - 15);
 		batch.draw(floor, floorPosX[1], FLOOR_HEIGHT - 15);
 		batch.draw(gameControl, 0, gameControlPosX[0]);
 		batch.draw(gameControl, 0, gameControlPosX[1]);
 		runner.draw(batch);
 		// Draws all objects in Array List
 		for (int i = 0; i < obstacles.size(); i++) {
 			obstacles.get(i).draw(batch);
 		}
 
 		batch.end();
 
 		// Input
 		if (Gdx.app.getType() == ApplicationType.Desktop) {
 			if (Gdx.input.isKeyPressed(Keys.SPACE)) {
 				runner.jump();
 			} else {
 				runner.jumpRelease();
 			}
 			if (Gdx.input.isKeyPressed(Keys.DOWN)) {
 				runner.duck();
 			} else {
 				runner.duckRelease();
 			}
 		}		
 		if (Gdx.input.isTouched()) {
 			int touchedX = Gdx.input.getX();
 			int touchedY = Gdx.input.getY();
 			if ((touchedX >= 0  && touchedX <= 50) && (touchedY >= 0  && touchedY <= 50)){
 				pause();				
 			}
 			// If user touches left side of the screen then Runner Ducks
 			if (Gdx.input.getX() < 400) {
 				runner.duck();
 			} else {
 				runner.duckRelease();
 			}
 			// user touches right side of the screen then Jump.
 			if (Gdx.input.getX() >= 400) {
 				runner.jump();
 			} else {
 				runner.jumpRelease();
 			}
 		} else {
 			runner.jumpRelease();
 			runner.duckRelease();
 		}
 
 		// generate random selection for obstacle to be on floor or mid height.
 		spawnPositionRandom = MathUtils.random(1, 2);
 		if (spawnPositionRandom == 1) {
 			spawnPositionY = FLOOR_HEIGHT + 50;
 		} else {
 			spawnPositionY = FLOOR_HEIGHT;
 		}
 
 		// if (TimeUtils.nanoTime() - lastSpawnTime > 400000000) {
 		// lastSpawnPos = MathUtils.random(lastSpawnPos
 		// + Obstacle.SPRITE_WIDTH * 3, lastSpawnPos + RESW);
 		// obstacles.add(new Obstacle(camera, lastSpawnPos, spawnPositionY));
 		// lastSpawnTime = TimeUtils.nanoTime();
 		// }
 		if (obstacles.size() < 10) {
 			obstacles.add(new Obstacle(camera, lastSpawnPos + 256,
 					spawnPositionY));
 			lastSpawnPos = obstacles.get(obstacles.size()-1).hitbox.x;
 		}
 
 		for (int i = 0; i < obstacles.size(); i++) {
 			// Debug
 			//obstacles.get(i).drawHitbox();
 			if (obstacles.get(i).isOffScreen()) {
 				obstacles.remove(i);
 			}
 			if (obstacles.get(i).hitbox.overlaps(runner.hitbox)) {
 				endGame();
 			}
 
 		}
 
 		// Debug
 		//runner.drawHitbox();
 
 	}
 
 	private void endGame() {
 		//create();
 		runner.state = State.dead;
 		runSpeed = 0;
 		if (Gdx.input.isKeyPressed(Keys.ANY_KEY) || Gdx.input.isTouched()) {
 			create();
 			System.gc(); // Garbage collector
 		}
 
 	}
 
 	@Override
 	public void dispose() {
 		for (int i = 0; i < obstacles.size(); i++) {
 			obstacles.get(i).dispose();
 		}
 		runner.dispose();
 		music1.dispose();
 		batch.dispose();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {		
 		System.out.println("You Paused");
 		this.game.setScreen(this.game.getSplashScreen());
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 }
