 package com.dat255_group3.controller;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.dat255_group3.model.InGame;
 import com.dat255_group3.utils.CoordinateConverter;
 import com.dat255_group3.view.InGameView;
 
 public class InGameController implements Screen{
 
 	private InGame inGame;
 	private InGameView inGameView;
 	private WorldController worldController;
 	private MyGdxGameController myGdxGameController;
 	private float timeStep = 1.0f / 10.0f;
 	private final int velocityIterations = 6;
 	private final int positionIterations = 2;
 	private TiledMap map;
 	private OrthographicCameraController cameraController;
 	private Box2DDebugRenderer renderer = new Box2DDebugRenderer(true, true, true, true, true, true);
 	private Matrix4 matrix = new Matrix4();
 	private float time;
 	private boolean gameOver;
 
 
 	public InGameController(MyGdxGameController myGdxGameController){
 		matrix.setToOrtho2D(0, 0, 480, 320);
 		this.cameraController = new OrthographicCameraController();
 		this.cameraController.create();
 		map = new TmxMapLoader().load("worlds/test5.tmx");
 		this.myGdxGameController = new MyGdxGameController();
 		this.inGameView = new InGameView(map, cameraController.getCamera());
 		this.inGame = new InGame();
 		this.worldController = new WorldController(this, inGame.getSpeedM());
 		this.time = 0;
 		this.gameOver = false;
 
 
 	}
 
 
 	@Override
 	public void render(float delta) {
 		// Shows a white screen
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 
 
 		/*
 		 * Checks whether the backbutton has been pressed.
 		 * If so, a pausepop-up-screen will be shown.
 		 */
 		if (Gdx.input.isKeyPressed(Keys.BACK)){
 			Gdx.input.setCatchBackKey(true);
 			myGdxGameController.setScreen(myGdxGameController.getPauseScreen());
 
 		}
 
 		if (hasWon()) {
 			//Change to gamewon-screen
 			//this.gameOver = false;
 			//gameOver();
 		}
 		
 		if(this.worldController.getCharacterController().getCharacter().isDead()){
 			this.gameOver = true;
 			gameOver();
 		} 
 
 			update(delta);
 			
 			// check collision with the closest cookie
			//worldController.checkNextCookie();
 
 			
 			// draws the world and its components
 			this.inGameView.draw(this.worldController.getWorldView(), this.worldController.getCharBody(), 
 					this.worldController.getCharacterController().getCharacterView(), 
 					this.worldController.getCookieController().getCookieView(), time, 
 					worldController.getCookieCounter(), gameOver);
 
 			/*
 			 * Checks whether the screen has been touched. 
 			 * If so, a method which will make the character jump is invoked.
 			 */
 			if(Gdx.input.isTouched()){
 				worldController.getCharacterController().tryToJump(); 	
 			}
 
 			//Draw physics bodies, for debugging
 			renderer.render(worldController.getPhysicsWorld(), matrix);
 		
 }
 
 
 @Override
 public void resize(int width, int height) {
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
 
 @Override
 public void pause() {
 	// TODO Auto-generated method stub
 
 }
 
 @Override
 public void resume() {
 	// TODO Auto-generated method stub
 
 }
 
 @Override
 public void dispose() {
 	map.dispose();
 	cameraController.dispose();
 	renderer.dispose();
 }
 /**
  * Update so that the character model has the same position (x,y) as the physical body
  * @param body , the physical body of the character
  * @param character , the character model with the position
  */
 
 
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
 	//		Gdx.app.log("FinishLine", "Finish line: x: " + worldController.getFinishLineX() + "Start: x: " + worldController.getStartPos().x);
 	if(worldController.getCharacterController().getCharacter().getPosition().x >= worldController.getFinishLineX()) {
 		return true;
 	}else{
 		return false;
 	}
 }
 
 public void reset() {
 	//reset
 }
 
 public void update(float delta) {
 	
 	this.timeStep = delta;
 	
 	// update the physics
 	this.worldController.getPhysicsWorld().step(this.timeStep, this.velocityIterations, this.positionIterations);
 	//Move the physic body of the character
 	//worldController.getCharBody().applyForceToCenter(0.3f, 0, true);
 
 
 	// Update the position of the camera
 	cameraController.render();
 
 	//update the time
 	this.time = time+delta;
 
 	// Updates the speed
 	inGame.setSpeedP(CoordinateConverter.meterToPixel(inGame.getSpeedM()*delta));
 	cameraController.setSpeedP(inGame.getSpeedP());
 
 	//give character speed
 	if(this.worldController.getCharBody().getLinearVelocity().x < this.inGame.getSpeedM()){
 		this.worldController.getCharBody().applyForceToCenter(new Vector2 (5, 0), true);
 	}
 	// update the model position for the character
 	this.worldController.uppdatePositions(this.worldController.getCharBody(), this.worldController.getCharacterController().getCharacter());
 
 	// Update the position of the finish line
 	worldController.moveFinishLine(inGame.getSpeedP());
 
 }
 public void gameOver() {
 	Gdx.app.log("Game over:", gameOver + "");
 	//this.inGameView.draw(this.worldController.getWorldView(), this.worldController.getCharBody(), this.worldController.getCharacterController().getCharacterView(), time, gameOver);
 
 	int timeint = 10;
 	int score = 10;
 	//Change to gameover-screen
 	myGdxGameController.getGameOverScreen().gameOver(score, timeint, gameOver);
 	myGdxGameController.setScreen(myGdxGameController.getGameOverScreen());
 }
 
 }
