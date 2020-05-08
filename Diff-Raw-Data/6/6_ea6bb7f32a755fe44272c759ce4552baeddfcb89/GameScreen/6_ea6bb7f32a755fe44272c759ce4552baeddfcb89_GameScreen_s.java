 package cargame.screens;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import cargame.CarGame;
 import cargame.core.Player;
 import cargame.elements.Car;
 import cargame.elements.Element;
 import cargame.elements.TrackContactListener;
 import cargame.ui.Hud;
 import cargame.utils.BodyEditorLoader;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.ScreenAdapter;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.GdxNativesLoader;
 
 public class GameScreen extends ScreenAdapter {
 
 	private static final String TRACKS_PATH = "tracks/track1";
 	private static final float BOX_STEP = 1 / 60f;
 	private static final int BOX_VELOCITY_ITERATIONS = 6;
 	private static final int BOX_POSITION_ITERATIONS = 2;
 	
 	private World world;
 	private Box2DDebugRenderer debugRenderer;
 	private OrthographicCamera camera;
 	private OrthographicCamera fixedCamera;
 	private SpriteBatch batch;
 	
 	private List<Element> elements;
 	private List<Sprite> staticSprites;
 	
 	private Hud playerHud;
 	
 	private boolean gameOver;
 	
 	private Car playerCar;
 	private Map<Integer,Car> otherPlayersCars;
 	
 	private float trackWidth;
 	private float trackHeight;
 	
 	static {
 		GdxNativesLoader.load();
 	}
 	
 	public GameScreen(Player clientPlayer) {
 		super();
 		world = new World(new Vector2(0, 0), true);
 		gameOver = false;
 		playerCar = new Car(clientPlayer, this, Car.SPRITE_2);
 		otherPlayersCars = new HashMap<Integer, Car>();
 	}
 	
 	@Override
 	public void show() {
 		elements = new ArrayList<Element>();
 		
 		// Player Hud
 		playerHud = new Hud(this);
 		
 		// Static sprites
 		staticSprites = new ArrayList<Sprite>();
 		
 		// Background
 		Sprite spriteBackground = new Sprite(new Texture("img/racetrack.png"));
 		spriteBackground.setScale(0.27f,0.27f);
 		spriteBackground.setOrigin(0f,0f);
 		staticSprites.add(spriteBackground);
 		
 		Gdx.graphics.setDisplayMode(1200, 800, false);
 //		Gdx.graphics.setDisplayMode(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
//		camera = new OrthographicCamera(276,205);
		camera = new OrthographicCamera(226,165);
 		fixedCamera = new OrthographicCamera(276,205);
 //		camera = new OrthographicCamera(200,150);
         camera.position.set(camera.viewportWidth * 0.5f, camera.viewportHeight * 0.5f, 0f);  
         camera.update();
         
         fixedCamera.position.set(camera.viewportWidth * 0.5f, camera.viewportHeight * 0.5f, 0f);  
         fixedCamera.update();
         
         batch = new SpriteBatch();
         
         // Boundaries
 //        Box2DUtils.createPolygonBody(world, new Vector2(0, 0), camera.viewportWidth, 1.0f, 0f, 0.1f, 2, false, false);
 //        Box2DUtils.createPolygonBody(world, new Vector2(0, camera.viewportHeight), (camera.viewportWidth) , 1.0f, 0f, 0.1f, 2, false, false);
 //        Box2DUtils.createPolygonBody(world, new Vector2(0, 0), 1.0f, camera.viewportHeight, 0f, 0.1f, 2, false, false);
 //        Box2DUtils.createPolygonBody(world, new Vector2(camera.viewportWidth, 0), 1.0f, camera.viewportHeight, 0f, 0.1f, 2, false, false);
         
         // Cars
         playerCar.createCarObject();
         elements.add(playerCar);
         
         // Load track
         loadTrack("track1");
         
         debugRenderer = new Box2DDebugRenderer(false,false,false,false,false,false);  
 	}
 	
 	private void loadTrack(String trackName) {
 		// Loads tracks file created with Physics body editor
 		// (https://code.google.com/p/box2d-editor/)
 		BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal(TRACKS_PATH));
 
 		BodyDef bd = new BodyDef();
 		bd.position.set(0, 0);
 		// bd.type = BodyType.DynamicBody;
 
 		FixtureDef fd = new FixtureDef();
 		fd.density = 1;
 		fd.friction = 0.1f;
 		fd.restitution = 0.3f;
 
 		Body body = world.createBody(bd);
 		
 		// Creates the box2D object with the name in the Physics body editor
 		// bodies list
 		loader.attachFixture(body, trackName, fd, 280);
 		
 		// Set track dimensions
 		this.trackWidth = 195;
 		this.trackHeight = 125;
 		
 		// Track contact listener
 		TrackContactListener sensor = new TrackContactListener(this);
 		world.setContactListener(sensor);
 		
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		batch.dispose();
 		playerHud.dispose();
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
 		
 		if(gameOver){
 			CarGame.getInstance().switchScreen(CarGame.GAMEOVER_SCREEN);
 			return;
 		}
 		
 		float cameraX = playerCar.getBody().getPosition().x;
 		float cameraY = playerCar.getBody().getPosition().y;
 		
 		if(cameraX - camera.viewportWidth/2.0 < 0 ){
 			cameraX = (float) (camera.viewportWidth/2.0);
 		}
 		
 		if(cameraY - camera.viewportHeight /2.0 < 0 ){
 			cameraY = (float) (camera.viewportHeight/2.0);
 		}
 		
 		if(cameraX > trackWidth ){
 			cameraX = (float) (trackWidth);
 		}
 		
 		if(cameraY > trackHeight ){
 			cameraY = (float) (trackHeight);
 		}
 		
 //		System.out.println(cameraX+" | "+trackWidth);
 //		System.out.println(cameraY+" | "+trackHeight);
 		
 		camera.position.set(cameraX, cameraY, 0);
 		camera.update();
 		
 		// Key listeners
 		if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)){
 			playerCar.setSteeringAngle((float)Car.MAX_STEER_ANGLE);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)){
 			playerCar.setSteeringAngle((float)-Car.MAX_STEER_ANGLE);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_UP)){
 			playerCar.setEngineSpeed(Car.HORSEPOWERS);
 		}
 		if (Gdx.input.isKeyPressed(Keys.DPAD_DOWN)){
 			playerCar.setEngineSpeed(-Car.HORSEPOWERS);
 		}
 		
 		playerCar.updatePosition();
 		
 		for(int playerId : CarGame.getInstance().getPlayers().keySet()){
         	if(playerCar.getPlayer().id != playerId){
         		if(otherPlayersCars.containsKey(playerId)){
         			// Update other player position
         			Car otherCar = otherPlayersCars.get(playerId);
         			otherCar.setPlayer(CarGame.getInstance().getPlayers().get(playerId));
         		}else{
         			// Create new player
         			Player otherPlayer = CarGame.getInstance().getPlayers().get(playerId);
         			Car otherCar = new Car(otherPlayer, this, Car.SPRITE_3 );
         			elements.add(otherCar);
         			otherCar.createCarObject();
         			otherPlayersCars.put(otherPlayer.id, otherCar);
         		}
         		
         	}
         }
 		
 		if (Gdx.input.isKeyPressed(Keys.Q)){
 			CarGame.getInstance().switchScreen(CarGame.GAMEOVER_SCREEN);
 		}
 		
 		// Box2d Render
 		debugRenderer.render(world, camera.combined);
 		
 		// Set object images
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		
 		// Static sprites
 		for(Sprite sprite: staticSprites){
 			sprite.draw(batch);
 		}
 		
 		
 		
 		for(Element element : elements){
 			Body body = element.getBody();
 //			if(element.equals(playerCar)){
 				element.getBody().setAngularVelocity((float) (element.getBody().getAngularVelocity()*0.5));
 				element.getBody().setLinearVelocity(element.getBody().getLinearVelocity().scl(0.95f));
 //			}
 			
 			// Cars sprites
 			if(body.getUserData() != null && body.getUserData() instanceof Sprite) {
 				Sprite sprite = (Sprite) body.getUserData();
 				sprite.setPosition(body.getPosition().x - sprite.getWidth()/2, body.getPosition().y - sprite.getHeight() /2);
 				sprite.setRotation(body.getAngle()*MathUtils.radiansToDegrees);
 				sprite.draw(batch);
 			}
 		}
 		batch.end();
 		
 		// Hud
 		playerHud.render();
 		
 		if(playerCar.getLaps() > 5){
 			gameOver = true;
 		}
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public List<Element> getElements() {
 		return elements;
 	}
 
 	public OrthographicCamera getCamera() {
 		return camera;
 	}
 	
 	public OrthographicCamera getFixedCamera() {
 		return fixedCamera;
 	}
 
 	public Car getPlayerCar() {
 		return playerCar;
 	}
 	
 }
