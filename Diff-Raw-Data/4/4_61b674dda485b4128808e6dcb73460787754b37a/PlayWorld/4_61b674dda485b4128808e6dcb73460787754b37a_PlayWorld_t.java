 package com.cvgstudios.pokemonchrome.game;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.maps.MapObjects;
 import com.badlogic.gdx.maps.objects.RectangleMapObject;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.cvgstudios.pokemonchrome.ChromeGame;
 import com.cvgstudios.pokemonchrome.InputHandler;
 
 public class PlayWorld implements Screen {
 	@SuppressWarnings("unused")
 	private ChromeGame game;
 
 	private TiledMap map;
 	private OrthogonalTiledMapRenderer renderer;
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 
	private String MAP_NAME = "Exitium";
	private final Vector2 STARTCOORD = new Vector2(650, 150);
 
 	private Sprite player = new Sprite();
 
 	private BitmapFont font = new BitmapFont(
 			Gdx.files.internal("font/pokemon.fnt"),
 			Gdx.files.internal("font/pokemon.png"), false);
 
 	private float xD = 0, yD = 0;
 
 	private int collisionAmount;
 
 	private int[] bgLayers;
 	private int[] fgLayers;
 
 	private Rectangle[] collsionRect;
 	private Rectangle[] interactRect;
 
 	private RectangleMapObject[] interactObject;
 
 	public Rectangle user = new Rectangle(Gdx.graphics.getWidth() / 2,
 			Gdx.graphics.getHeight() / 2, 38, 42);;
 
 	private TextureRegion playerR = new TextureRegion(new Texture(
 			"imgs/MalePlayer.png"));
 
 	private int interactionAmount;
 
 	private int counter = 0;
 
 	private int step;
 
 	private int direction;
 
 	private boolean keyDown = false;
 
 	private final int STEP_DELAY = 15;
 
 	public PlayWorld(ChromeGame game) {
 		this.game = game;
 		Gdx.input.setInputProcessor(new InputHandler(this, camera));
 	}
 
 	@Override
 	public void render(float delta) {
 
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		moveUser();
 		if (keyDown) {
 			changeUserSteps();
 		}
 		camera.update();
 		batch.setProjectionMatrix(camera.combined);
 
 		renderer.setView(camera);
 
 		renderer.render(bgLayers);
 
 		batch.begin();
 
 		batch.draw(player, player.getX(), player.getY());
 
 		font.draw(batch, player.getX() + "," + player.getY(),
 				0 + player.getX(), 0 + player.getY());
 		batch.end();
 		renderer.render(fgLayers);
 
 	}
 
 	private void changeUserSteps() {
 		counter++;
 		if (counter == STEP_DELAY) {
 			counter = 0;
 			if (step == 1) {
 				step = 2;
 			} else {
 				step = 1;
 			}
 			changePlayerDirection(direction);
 			// Gdx.app.log(ChromeGame.LOG, "Step " + step);
 		}
 	}
 
 	private void moveUser() {
 		Vector2 oPos = new Vector2(player.getX(), player.getY());
 
 		player.translate(xD, yD);
 
 		// Gdx.app.log(ChromeGame.LOG, step + "");
 
 		user = new Rectangle(player.getX(), player.getY(), player.getWidth(),
 				player.getHeight());
 		if (collision()) {
 			// Gdx.app.log(ChromeGame.LOG, "");
 			player.setPosition(oPos.x, oPos.y);
 		}
 		checkPlayerInteraction();
 
 		camera.position.set(player.getX(), player.getY(), 0);
 		// Gdx.app.log(ChromeGame.LOG, player.getX() + "," + player.getY());
 
 	}
 
 	private boolean collision() {
 		for (int x = 0; x < collisionAmount; x++) {
 			if (collsionRect[x].overlaps(user)) {
 				// Gdx.app.log(ChromeGame.LOG, "Collide");
 				xD = 0;
 				yD = 0;
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void setXD(float x) {
 		this.xD = x;
 		keyDown = true;
 	}
 
 	public void setYD(float y) {
 		this.yD = y;
 		keyDown = true;
 	}
 
 	public void resetCameraDirection() {
 		yD = 0;
 		xD = 0;
 		keyDown = false;
 		step = 0;
 		changePlayerDirection(direction);
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		camera.viewportHeight = height;
 		camera.viewportWidth = width;
 		camera.update();
 	}
 
 	@Override
 	public void show() {
 
 		importMap(MAP_NAME, STARTCOORD);
 
 		camera = new OrthographicCamera();
 		camera.position.set(507, 525, 0);
 		Gdx.input.setInputProcessor(new InputHandler(this, camera));
 
 		batch = new SpriteBatch();
 
 		changePlayerDirection(1);
 
 	}
 
 	public void checkPlayerInteraction() {
 		for (int x = 0; x < interactionAmount; x++) {
 			if (interactRect[x].overlaps(user)) {
 				Gdx.app.log(ChromeGame.LOG, interactObject[x].getName());
 				checkIfAction(interactObject[x].getName());
 			}
 		}
 	}
 
 	private void checkIfAction(String s) {
 		if (s.contains("(CHANGEMAP)")) {
 			String[] fields;
 			fields = s.split(":");
 			String[] pos = (fields[2].split(","));
 			float x = Integer.parseInt(pos[0]);
 			float y = Integer.parseInt(pos[1]);
 
 			Vector2 playerPos = new Vector2(x, y);
 			importMap(fields[1], playerPos);
 		} else {
 
 		}
 	}
 
 	private void createInteractions() {
 		MapObjects mInteractions = map.getLayers().get("Interaction")
 				.getObjects();
 
 		interactionAmount = mInteractions.getCount();
 		interactObject = new RectangleMapObject[interactionAmount];
 
 		interactRect = new Rectangle[interactionAmount];
 
 		for (int x = 0; x < interactionAmount; x++) {
 			interactObject[x] = (RectangleMapObject) mInteractions.get(x);
 			interactRect[x] = interactObject[x].getRectangle();
 		}
 
 	}
 
 	private void createCollisions() {
 		MapObjects mCollisions = map.getLayers().get("Collision").getObjects();
 
 		RectangleMapObject gameObject = new RectangleMapObject();
 		collsionRect = new Rectangle[mCollisions.getCount()];
 
 		collisionAmount = mCollisions.getCount();
 
 		for (int x = 0; x < mCollisions.getCount(); x++) {
 			gameObject = (RectangleMapObject) mCollisions.get(x);
 			collsionRect[x] = gameObject.getRectangle();
 		}
 	}
 
 	public void importMap(String m, Vector2 pos) {
 		map = new TmxMapLoader().load("maps/" + m + ".tmx");
 
 		int index = 0;
 		int layerNum = map.getLayers().getCount() - 2;
 
 		for (int x = 0; x < layerNum; x++) {
 			if (map.getLayers().get(x).getName()
 					.equalsIgnoreCase("playerLayer")) {
 				index = x;
 			}
 		}
 		bgLayers = new int[index];
 		for (int x = 0; x < index; x++) {
 			bgLayers[x] = x;
 		}
 
 		fgLayers = new int[layerNum - (index + 1)];
 		int topLayerStart = index + 1;
 
 		for (int x = 0; x < fgLayers.length; x++) {
 			fgLayers[x] = x + topLayerStart;
 			Gdx.app.log(ChromeGame.LOG, fgLayers[x] + "");
 		}
 		renderer = new OrthogonalTiledMapRenderer(map);
 
 		createCollisions();
 		createInteractions();
 
 		player.setPosition(pos.x, pos.y);
 	}
 
 	public void changePlayerDirection(int d) {
 		direction = d;
 		switch (d) {
 		case 1:// down
 			playerR.setRegion(0, 0 + (step * 42), 37, 42);
 			// playerR.setRegion(0, 0, 37, 42);
 			break;
 		case 2:// up
 			playerR.setRegion(37, 0 + (step * 42), 37, 42);
 			// playerR.setRegion(37, 0, 37, 42);
 			break;
 		case 3:// right
 				// playerR.setRegion(74, 0, 37, 42);
 			playerR.setRegion(74, 0 + (step * 42), 37, 42);
 			break;
 		case 4:// left
 			playerR.setRegion(111, 0 + (step * 42), 37, 42);
 			// playerR.setRegion(111, 0, 37, 42);
 			break;
 		}
 		player.setRegion(playerR);
 
 	}
 
 	@Override
 	public void hide() {
 		dispose();
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 		map.dispose();
 		renderer.dispose();
 	}
 }
