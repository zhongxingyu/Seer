 package ch.kanti_wohlen.asteroidminer.screen;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.kanti_wohlen.asteroidminer.AsteroidMiner;
 import ch.kanti_wohlen.asteroidminer.CollisionListener;
 import ch.kanti_wohlen.asteroidminer.GameMode;
 import ch.kanti_wohlen.asteroidminer.LocalPlayer;
 import ch.kanti_wohlen.asteroidminer.Player;
 import ch.kanti_wohlen.asteroidminer.TaskScheduler;
 import ch.kanti_wohlen.asteroidminer.Textures;
 import ch.kanti_wohlen.asteroidminer.animations.Animations;
 import ch.kanti_wohlen.asteroidminer.entities.Entity;
 import ch.kanti_wohlen.asteroidminer.entities.WorldBorder;
 import ch.kanti_wohlen.asteroidminer.spawner.AsteroidSpawner;
 import ch.kanti_wohlen.asteroidminer.spawner.EndlessAsteroidSpawner;
 import ch.kanti_wohlen.asteroidminer.spawner.IdleSpawner;
 import ch.kanti_wohlen.asteroidminer.spawner.TimeAttackAsteroidSpawner;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Array.ArrayIterator;
 
 public class GameScreen {
 
 	public static final float WORLD_SIZE = 160f;
 
 	private static final float timeStep = TaskScheduler.INSTANCE.TICK_TIME;
 	private static final int velocityIterations = 5;
 	private static final int positionIterations = 2;
 
 	private final BitmapFont font;
 	private final OrthographicCamera camera;
 	private final World world;
 	private final Animations animations;
 	private final SpriteBatch batch;
 	private final List<Player> players;
 
 	private LocalPlayer localPlayer;
 	private AsteroidSpawner asteroidSpawner;
 	private boolean running;
 
 	// Temp
 	private int counter = 0;
 
 	public GameScreen() {
 		font = new BitmapFont(Gdx.files.internal("data/default.fnt"));
 
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false);
 		camera.position.set(0f, 0f, 0f);
 
 		players = new ArrayList<Player>();
 		animations = new Animations();
 		world = new World(new Vector2(0, 0), true);
 		CollisionListener cl = new CollisionListener();
 		world.setContactListener(cl);
 		batch = AsteroidMiner.INSTANCE.getSpriteBatch();
 
 		setAsteroidSpawner(new IdleSpawner(world));
 	}
 
 	public void startGame(GameMode gameMode) {
 		if (running) return;
 		Gdx.input.setInputProcessor(null);
 
 		switch (gameMode) {
 		case TIME_2_MIN:
 			setAsteroidSpawner(new TimeAttackAsteroidSpawner(world, 120f));
 			break;
 		case TIME_5_MIN:
 			setAsteroidSpawner(new TimeAttackAsteroidSpawner(world, 300f));
 			break;
 		case ENDLESS:
 			setAsteroidSpawner(new EndlessAsteroidSpawner(world));
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown game mode.");
 		}
 		localPlayer = new LocalPlayer(world);
 		players.clear();
 		players.add(localPlayer);
 
 		TaskScheduler.INSTANCE.runTaskLater(new Runnable() {
 
 			@Override
 			public void run() {
 				Gdx.input.setInputProcessor(localPlayer.getInput());
 				running = true;
 			}
 		}, 1f);
 	}
 
 	public void stopGame() {
 		if (!running) return;
 		running = false;
 
 		final Color color = localPlayer.getSpaceShip().getHealth() == 0 ? Color.BLACK : Color.WHITE;
 		AsteroidMiner.INSTANCE.switchScreenWithOverlay(AsteroidMiner.INSTANCE.getGameOverScreen(), color);
 	}
 
 	public boolean isGameRunning() {
 		return running;
 	}
 
 	public void reset() {
 		setAsteroidSpawner(new IdleSpawner(world));
 		players.clear();
 		localPlayer = null;
 		camera.setToOrtho(false);
 		camera.position.set(0f, 0f, 0f);
 		animations.disposeAll();
 	}
 
 	public void tick(float delta) {
 		if (running) moveCamera();
 
 		// Temp
 		counter += 1;
 		counter %= 60;
 		if (counter == 0) {
 			if (localPlayer != null) {
 				Vector2 pos = localPlayer.getSpaceShip().getPhysicsBody().getPosition();
 				Gdx.app.log("SpaceShip", "Location: " + pos.toString());
 			}
 			Gdx.app.log("Entity count", String.valueOf(world.getBodyCount()));
 		}
 
 		// Process input
 		for (Player p : players) {
 			p.doInput(delta);
 		}
 
 		// Tick animations
 		animations.tickAll(delta);
 
 		// Spawn asteroids
 		asteroidSpawner.tick();
 
 		// Do physics, therefore initializing newly spawned entities
 		world.step(timeStep, velocityIterations, positionIterations);
 		applyGravity();
 	}
 
 	public void render() {
 		// Draw background
 		renderBackground();
 
 		Array<Body> bodies = new Array<Body>(world.getBodyCount());
 		world.getBodies(bodies);
 		ArrayIterator<Body> i = new ArrayIterator<Body>(bodies, true);
 		final float width = Gdx.graphics.getWidth() * Entity.PIXEL_TO_BOX2D;
 		final float height = Gdx.graphics.getHeight() * Entity.PIXEL_TO_BOX2D;
 		final Vector3 pos = camera.position.cpy().scl(Entity.PIXEL_TO_BOX2D);
 		pos.sub(width * 0.5f, height * 0.5f, 0f);
 		final Rectangle visibleRect = new Rectangle(pos.x, pos.y, width, height);
 
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 
 		// Render animations under entities
 		animations.renderAll(batch);
 
 		// Render entities
 		while (i.hasNext()) {
 			Body body = i.next();
 			if (body == null) continue;
 			Entity e = (Entity) body.getUserData();
 
 			if (e.isRemoved()) {
 				i.remove();
 				world.destroyBody(body);
 				body.setUserData(null);
 			} else {
 				if (visibleRect.overlaps(e.getBoundingBox())) {
 					e.render(batch);
 				}
 			}
 		}
 
 		// Render world borders over all other objects
 		WorldBorder.renderAllBorders(batch, visibleRect);
 		batch.end();
 
 		// Render score and remaining time as an overlay
 		renderOverlays();
 	}
 
 	private void renderBackground() {
 		// Make background move slower
 		float fx = camera.position.x * 0.2f;
 		float fy = camera.position.y * 0.2f;
 		// And repeat background graphic
 		fx = fx % Gdx.graphics.getWidth();
 		fy = fy % Gdx.graphics.getHeight();
 		if (fx < 0) fx += Gdx.graphics.getWidth();
 		if (fy < 0) fy += Gdx.graphics.getHeight();
 		fx -= Gdx.graphics.getWidth() / 2f;
 		fy -= Gdx.graphics.getHeight() / 2f;
 		// Then set position to be inside the camera's focus
 		fx = camera.position.x - fx;
 		fy = camera.position.y - fy;
 
 		final int width = Gdx.graphics.getWidth();
 		final int height = Gdx.graphics.getHeight();
 		final float u2 = width / Textures.BACKGROUND.getWidth();
 		final float v2 = height / Textures.BACKGROUND.getHeight();
 
 		camera.update(false);
 		batch.setProjectionMatrix(camera.combined);
 		batch.disableBlending();
 		batch.begin();
 		batch.draw(Textures.BACKGROUND.getTexture(), fx - width, fy - height, width, height, 0f, 0f, u2, v2);
 		batch.draw(Textures.BACKGROUND.getTexture(), fx - width, fy, width, height, 0f, 0f, u2, v2);
 		batch.draw(Textures.BACKGROUND.getTexture(), fx, fy - height, width, height, 0f, 0f, u2, v2);
 		batch.draw(Textures.BACKGROUND.getTexture(), fx, fy, width, height, 0f, 0f, u2, v2);
 		batch.end();
 		batch.enableBlending();
 	}
 
 	private void renderOverlays() {
 		if (localPlayer == null) return;
 
 		final Matrix4 projection = new Matrix4().setToOrtho2D(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		final String score = String.valueOf(localPlayer.getScore());
 		final float xDist = font.getBounds(score).width;
 
 		batch.setProjectionMatrix(projection);
 		batch.begin();
 		font.setColor(Color.WHITE);
 
 		font.draw(batch, "SCORE", Gdx.graphics.getWidth() - 57, Gdx.graphics.getHeight() - 10);
 		font.draw(batch, score, Gdx.graphics.getWidth() - 10 - xDist, Gdx.graphics.getHeight() - 32);
 
 		if (!(asteroidSpawner instanceof IdleSpawner)) {
 			font.draw(batch, "TIME LEFT", 10, Gdx.graphics.getHeight() - 10);
 			if (asteroidSpawner instanceof TimeAttackAsteroidSpawner) {
 				final String time = String.valueOf(Math.round(((TimeAttackAsteroidSpawner) asteroidSpawner).getTimeLeft()));
 				font.draw(batch, time + " seconds", 10, Gdx.graphics.getHeight() - 32);
 			} else {
 				font.draw(batch, String.valueOf((char) 8734), 10, Gdx.graphics.getHeight() - 32);
 			}
 		}
 		batch.end();
 	}
 
 	private void moveCamera() {
 		// Get the spaceship's current distance from the center of the screen
 		final Vector2 movement = new Vector2(localPlayer.getSpaceShip().getPhysicsBody().getPosition().scl(10f));
 		movement.sub(camera.position.x, camera.position.y);
 
 		// Get the ship's distance from the border, keeping the direction
 		final float xDir = movement.x < 0 ? -1 : 1;
 		final float yDir = movement.y < 0 ? -1 : 1;
 		movement.set(Math.abs(movement.x), Math.abs(movement.y));
 		movement.sub(Gdx.graphics.getWidth() * 0.1f, Gdx.graphics.getHeight() * 0.075f);
 		movement.set(Math.max(movement.x, 0f), Math.max(movement.y, 0f));
 		movement.scl(xDir, yDir).scl(Entity.PIXEL_TO_BOX2D);
 
 		// Apply movement to foreground camera
 		camera.position.add(movement.x, movement.y, 0f);
 	}
 
 	private void applyGravity() {
 		final float G = 0.2f;
 
 		final Array<Body> outer = new Array<Body>(world.getBodyCount());
 		world.getBodies(outer);
 
 		for (int i = 0; i < outer.size; ++i) {
 			final Body body = outer.get(i);
 			if (body == null || body.getGravityScale() == 0f || body.getType() != BodyType.DynamicBody) {
 				outer.removeIndex(i);
 				--i;
 			}
 		}
 		final Array<Body> inner = new Array<Body>(outer);
 		Vector2 dir = new Vector2();
 
 		for (int i = 0; i < outer.size; ++i) {
 			final Body source = outer.get(i);
 			inner.removeIndex(0);
 
 			for (Body target : inner) {
 				dir = source.getPosition().sub(target.getPosition());
 				final float dist2 = dir.len2() + 1;
 				if (dist2 > 20000) continue;
 
 				dir.nor();
 				final float force = G * source.getMass() * target.getMass() / dist2;
 
 				target.applyForceToCenter(dir.cpy().scl(force * target.getGravityScale()), true);
 				source.applyForceToCenter(dir.cpy().scl(-force * source.getGravityScale()), true);
 			}
 		}
 	}
 
 	private void setAsteroidSpawner(AsteroidSpawner newAsteroidSpawner) {
 		if (asteroidSpawner != null) asteroidSpawner.stop();
 		asteroidSpawner = newAsteroidSpawner;
 		asteroidSpawner.start();
 	}
 
 	public Player[] getPlayers() {
 		return players.toArray(new Player[players.size()]);
 	}
 
 	public Player getLocalPlayer() {
 		return localPlayer;
 	}
 
 	public Animations getAnimations() {
 		return animations;
 	}
 
 	public Vector2 getLocationOnScreen(Entity entity) {
 		return getLocationOnScreen(entity.getPhysicsBody());
 	}
 
 	public Vector2 getLocationOnScreen(Body body) {
 		final Vector2 bodyPos = body.getPosition().cpy().scl(Entity.BOX2D_TO_PIXEL);
 		bodyPos.sub(camera.position.x, camera.position.y).scl(1f, -1f);
 		return bodyPos.add(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
 	}
 
 	public void dispose() {
 		for (Player player : players) {
 			player.dispose();
 		}
 		animations.disposeAll();
 		world.dispose();
 	}
 }
