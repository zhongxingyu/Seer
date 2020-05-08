 package de.redlion.rts;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 import de.redlion.rts.render.RenderDebug;
 import de.redlion.rts.render.RenderMap;
 import de.redlion.rts.units.Soldier;
 
 public class SinglePlayerGameScreen extends DefaultScreen {
 
 	float startTime = 0;
 
 	SpriteBatch batch;
 	SpriteBatch fadeBatch;
 	Sprite blackFade;
 	
 	BitmapFont font;
 	
 	RenderMap renderMap;
 	RenderDebug renderDebug;
 
 	float fade = 1.0f;
 	boolean finished = false;
 
 	float delta;
 	
 	OrthoCamController camController;
 	DrawController  drawController;
 	KeyController keyController;
 	OrthographicCamera camera;
 	InputMultiplexer multiplexer;
 	
 	public static boolean paused = false;
 
 	public SinglePlayerGameScreen(Game game) {
 		super(game);
 		
 		GameSession.getInstance().newSinglePlayerGame();
 		
 		//refresh references 
 		//TODO Observer Pattern for newGame
 		renderMap = new RenderMap();
 		renderDebug = new RenderDebug();
 		
 //		Gdx.input.setInputProcessor(new SinglePlayerControls(player));
 
 		batch = new SpriteBatch();
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
 		
 		blackFade = new Sprite(	new Texture(Gdx.files.internal("data/black.png")));
 		fadeBatch = new SpriteBatch();
 		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
 		
 		font = Resources.getInstance().font;
 		font.setScale(1);
 		
 		
 
 		initRender();
 	}
 
 	public void initRender() {
 		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
 		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
 		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
 		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	
 		initRender();
 		renderMap = new RenderMap();
 		
 		camController = new OrthoCamController(renderMap.cam);
 		keyController = new KeyController();
 		drawController = new DrawController(renderMap.cam);
 		multiplexer = new InputMultiplexer();
 		multiplexer.addProcessor(camController);
 		multiplexer.addProcessor(keyController);
 
 		Gdx.input.setInputProcessor(multiplexer);
 	}
 
 	@Override
 	public void show() {
 	}
 	
 	private float deltaCount = 0;	
 	@Override
 	public void render(float deltaTime) {
 		deltaCount += deltaTime;
 		if(deltaCount > 0.01) {
 			deltaCount = 0;
 			renderFrame(0.02f);
 		}
 		
 		if(paused) {
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(camController);
 			multiplexer.addProcessor(drawController);
 			Gdx.input.setInputProcessor(multiplexer);
 		}
 		else {
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(drawController);
 			multiplexer.addProcessor(camController);
 			Gdx.input.setInputProcessor(multiplexer);
 		}
 		
 	}
 
 	public void renderFrame(float deltaTime) {
 
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 		delta = Math.min(0.1f, deltaTime);
 
 		startTime += delta;
 
 		if(!paused)
 			updateUnits();
 
 		collisionTest();
 		updateAI();
 		
 		renderMap.render();
 
 		if (Configuration.getInstance().debug) {
 			renderDebug.render();
 		}
 
 		// FadeInOut
 		if (!finished && fade > 0) {
 			fade = Math.max(fade - (delta), 0);
 			fadeBatch.begin();
 			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
 					blackFade.getColor().b, fade);
 			blackFade.draw(fadeBatch);
 			fadeBatch.end();
 		}
 
 		if (finished) {
 			fade = Math.min(fade + (delta), 1);
 			fadeBatch.begin();
 			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
 					blackFade.getColor().b, fade);
 			blackFade.draw(fadeBatch);
 			fadeBatch.end();
 			if (fade >= 1) {
 				Gdx.app.exit();
 			}
 		}
 		
 		if(paused) {
 			batch.begin();
 			font.draw(batch, "PAUSED", 700, 35);
 			batch.end();
 		}
 	}
 	
 	private void updateUnits() {
 		
 		for(Soldier soldier:GameSession.getInstance().playerSoldiers) {
 			soldier.update(delta);
 		}
 		
 		for(Soldier soldier:GameSession.getInstance().enemySoldiers) {
 			soldier.update(delta);
 		}
 	}
 
 	private void updateAI() {		
 	}
 
 	private void collisionTest() {
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 }
