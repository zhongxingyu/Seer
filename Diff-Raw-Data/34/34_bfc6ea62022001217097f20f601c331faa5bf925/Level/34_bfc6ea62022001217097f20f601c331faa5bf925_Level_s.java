 package com.secondhand.model;
 
 import static org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.secondhand.loader.TextureRegionLoader;
 import com.secondhand.math.PolygonUtil;
 import com.secondhand.model.PowerUp.Effect;
 import com.secondhand.opengl.Polygon;
 import com.secondhand.opengl.TexturedPolygon;
 import com.secondhand.twirl.GlobalResources;
 
 public class Level {
 
 	private List<Entity> entityList;
 	private int playerMaxSize;
 	private PhysicsWorld physicsWorld;
 
 	private Player player;
 
 	private Shape[] worldBounds;
 
 	private int levelWidth;
 	private int levelHeight;
 
 	private static int levelNumber = 1;
 
 	// many constructors necessary?
 	// default maxsize?
 	public Level() {
 		this(100);
 	}
 
	// TODO: we do even need this constructor at all?
 	public Level(int maxSize) {
 		this(maxSize, new PhysicsWorld(new Vector2(), true), new Player(
 				new Vector2(50, 50), 20), createTestPlanets(), 2000, 2000);
 
 	}
 
 	public Level(int maxSize, PhysicsWorld pW, Player p,
 			List<Entity> otherEntities, int levelWidth, int levelHeight) {
 		this.playerMaxSize = maxSize;
 		this.physicsWorld = pW;
 		player = p;
 		entityList = otherEntities;
 		this.levelWidth = levelWidth;
 		this.levelHeight = levelHeight;
 		registerEntities();
 	}
 
 	public static List<Entity> createTestPlanets() {
 		List<Entity> testPlanets = new ArrayList<Entity>();
 
		// TODO: figure out how to access the texture loaded in game play scene
		// instead.
 		// TODO: Understand why textures are not loaded properly
 		TextureRegion planetTexture = GlobalResources.getInstance().planetTexture;
 
 		testPlanets.add(new Planet(new Vector2(100, 100), 30, planetTexture));
 
 		// add small planet, add a huge planet.
 		testPlanets.add(new Planet(new Vector2(300, 100), 15, planetTexture));
 		testPlanets.add(new Planet(new Vector2(400, 10), 300, planetTexture));
 
 		TexturedPolygon polygon = new TexturedPolygon(200, 200,
 				PolygonUtil.getRandomPolygon(), planetTexture);
 		testPlanets.add(new Obstacle(new Vector2(200, 200), polygon));
 
 		testPlanets.add(new PowerUp(new Vector2(20, 500), 30, Effect.RANDOM_TELEPORT, planetTexture));
 
 		return testPlanets;
 	}
 
 	public int getLevelNumber() {
 		return levelNumber;
 	}
 
 	public List<Entity> getEntityList() {
 		return entityList;
 	}
 
 	public PhysicsWorld getPhysicsWorld() {
 		return physicsWorld;
 	}
 
 	public void registerEntities() {
 		registerEntity(player);
 
 		// register all the other entities except for the player.
 		for (Entity e : entityList) {
 			registerEntity(e);
 		}
 
 		worldBounds = new Shape[4];
 
 		// put some invisible, static rectangles that keep the player within the
 		// world bounds:
 		// we do not do this using registerEntity, because these bodies are
 		// static.
 
 		worldBounds[0] = new Rectangle(0, levelHeight - 2, levelWidth, 2);
 		worldBounds[1] = new Rectangle(0, 0, levelWidth, 2);
 		worldBounds[2] = new Rectangle(0, 0, 2, levelHeight);
 		worldBounds[3] = new Rectangle(levelWidth - 2, 0, 2, levelHeight);
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0,
 				0.5f, 0.5f);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[0],
 				BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[1],
 				BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[2],
 				BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.physicsWorld, worldBounds[3],
 				BodyType.StaticBody, wallFixtureDef);
 
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public Shape[] getWorldBounds() {
 		return this.worldBounds;
 	}
 
 	public int getLevelWidth() {
 		return levelWidth;
 	}
 
 	public int getLevelHeight() {
 		return levelHeight;
 	}
 
 	// if we dont want level to handle this we could just move it
 	public void activateEffect(Effect effect) {
 		switch (effect) {
 		case RANDOM_TELEPORT:
 			// remove old player
 			// needs to confirm empty position
 			// add new player at new position
 			break;
 		case SHIELD:
 			player.setEffect(effect);
 			break;
 		case SPEED_UP:
 			player.setEffect(effect);
 			break;
 		case EAT_OBSTACLE:
 			player.setEffect(effect);
 			break;
 		case SCORE_UP:
 			// random number here?
 			player.increaseScore(100);
 			break;		
 		
 			
 			
 		}
 
 	}
 
 	public void registerEntity(Entity entity) {
 
 		PhysicsHandler pH = new PhysicsHandler(entity.getShape());
 
 		entity.getShape().registerUpdateHandler(pH);
 
 		// TODO: should probably allow the possibility to create box
 		// bodies(rectangular) bodies as well
 		// we could store some enum value in Entity for this purpose.
 
		// Body body2 = createPolygonBody(this.physicsWorld, polygon,
		// BodyType.StaticBody, FIXTURE_DEF);

 		FixtureDef fixture = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 		Body body;
 
 		// obstacles are polygons and not circles
 		if (entity instanceof Obstacle)
 			body = createPolygonBody(physicsWorld, (Polygon) entity.getShape(),
 					BodyType.DynamicBody, fixture);
 		else
 			body = PhysicsFactory.createCircleBody(physicsWorld,
 					entity.getShape(), BodyType.DynamicBody, fixture);
 		// we need this when doing collisions handling between entities and
 		// black holes:
 		body.setUserData(entity);
 		entity.setBody(body);
 
 		// setting the last boolean to false seems to prevent
 		// the erratic movement of the player. needs testing.
 		physicsWorld.registerPhysicsConnector(new PhysicsConnector(entity
 				.getShape(), entity.getBody(), true, false));
 	}
 
 	private static Body createPolygonBody(final PhysicsWorld physicsWorld,
 			final Polygon polygon, final BodyType bodyType,
 			final FixtureDef fixtureDef) {
 		com.badlogic.gdx.math.Vector2 vertices[] = new com.badlogic.gdx.math.Vector2[polygon
 				.getPolygon().size()];
 		int i = 0;
 		for (Vector2 vector : polygon.getPolygon()) {
 			vertices[i++] = new com.badlogic.gdx.math.Vector2(vector.x
 					/ PIXEL_TO_METER_RATIO_DEFAULT, vector.y
 					/ PIXEL_TO_METER_RATIO_DEFAULT);
 		}
 
 		return PhysicsFactory.createPolygonBody(physicsWorld, polygon,
 				vertices, bodyType, fixtureDef);
 	}
 
 	public void moveEntities(Vector2 v) {
 
 		/*
 		 * The problem was that we got the coordinates of the player from the
 		 * getBody(). This is a problem, since Box2D uses a coordinate system
 		 * different from that of AndEngine; in this system, all AndEngine
 		 * coordinates are first divided by 32 before they're feed into Box2D.
 		 * So the body was using the coordinate system of Box2D, which was why
 		 * we had to multiply the coordinates of the body coordintes with
 		 * 30(with Linnea and Andreas found through trial and error) But IShape
 		 * on the other hand, is a AndEngine class and it therefore uses the
 		 * coordinate system of AndEngine, which is why we should it instead of
 		 * the body.
 		 * 
 		 * See section 1.7 in http://www.box2d.org/manual.html for an
 		 * explanation of why AndEngine does this division by 32.
 		 */
 
 		Vector2 movementVector = new Vector2((player.getShape().getX() - v.x),
 				player.getShape().getY() - v.y);
 
 		// the closer the touch is to the player, the more force do we need to
 		// apply.
 		// movementVector.x = movementVector.x;
 		// movementVector.y = movementVector.y;
 
 		// make it a bit slower depending on how big it is.
 		movementVector = movementVector.mul(player.getRadius() * 0.001f);
 
 		/*
 		 * Made a better test, like I said the earlier test we did was not
 		 * functional, and since the length of a vector is always positive, the
 		 * Math.abs(player.getBody().getLinearVelocity().len()) has no effect.
 		 * This runs rather smoothly. Try it!
 		 */
 		float maxSpeed = 20;
 		Vector2 testVector = new Vector2(player.getBody().getLinearVelocity());
 		if (testVector.add(movementVector).len() > maxSpeed) // Check if new
 																// velocity
 																// doesn't
 																// exceed
 																// maxSpeed!
 			return;
 
 		player.getBody().applyLinearImpulse(movementVector,
 				player.getBody().getWorldCenter());
 	}
 
 	public boolean checkPlayerBigEnough() {
 		return player.getRadius() >= playerMaxSize;
 
 	}
 	
 	
 
 }
