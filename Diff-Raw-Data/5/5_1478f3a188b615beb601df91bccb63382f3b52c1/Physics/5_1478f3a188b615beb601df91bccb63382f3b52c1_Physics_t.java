 package com.secondhand.model.physics;
 
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.RectangularShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.secondhand.controller.CollisionContactListener;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.BlackHole;
 import com.secondhand.model.CircleEntity;
 import com.secondhand.model.Enemy;
 import com.secondhand.model.CollisionResolver;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Obstacle;
 import com.secondhand.model.Planet;
 import com.secondhand.model.RectangleEntity;
 import com.secondhand.view.opengl.Circle;
 import com.secondhand.view.opengl.Polygon;
 import com.secondhand.view.scene.GamePlayScene;
 
 public class Physics implements IPhysics {
 	private final PhysicsWorld physicsWorld;
 	private PhysicsConnector physicsConnector;
 	private final PhysicsEnemyUtil enemyUtil;
 	private CollisionResolver collisionResolver;
 	private final PhysicsWorldBounds bounds;
 	private GameWorld gameWorld;
 
 	// no vector needed because its zero gravity. And if the constructor
 	// needs an vector that means we need to to import Vector2
 	// wherever we creates Physics
 	// TODO:remove vector2.
 
 	public Physics() {
 		
 		physicsWorld = new PhysicsWorld(new Vector2(), true);
 		bounds = new PhysicsWorldBounds(physicsWorld);
 		enemyUtil = new PhysicsEnemyUtil(physicsWorld);
 
 	}
 
 	public void init() {
 		this.physicsWorld.setVelocityIterations(16);
 		this.physicsWorld.setPositionIterations(16);
 	}
 
 	// put some invisible, static rectangles that keep the player within the
 	// world bounds:
 	// we do not do this using registerEntity, because these bodies are
 	// static.
 	public void setWorldBounds(final int levelWidth, final int levelHeight) {
 		bounds.setWorldBounds(levelWidth, levelHeight);
 	}
 
 	@Override
 	public void removeWorldBounds() {
 		bounds.removeBounds();
 	}
 
 	@Override
 	public void registerBody(final Entity entity, final Body body,
 			final boolean rotation) {
 		body.setUserData(entity);
 
 		physicsWorld.registerPhysicsConnector(new CustomPhysicsConnector(entity
 				.getShape(), entity.isCircle(), entity.getBody(), true,
 				rotation));
 	}
 
 	// andEngine or box2d coordinates in? and depending on from
 	// where we call the method we could perhaps have an vector as input.
 	// We souldn't need to do much more here, all other calculations should
 	// be done in model. Entity instead of body and then somehow get body?
 	// All entities that need this function are enemies and player.
 	@Override
 	public void applyImpulse(final Body body, final float posX, final float posY) {
 
 		final Vector2 position = new Vector2(posX
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, posY
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
 		final Vector2 force = new Vector2(body.getWorldCenter().x - posX,
 				body.getWorldCenter().y - posY);
 
 		body.applyLinearImpulse(force, position);
 
 	}
 
 	public void deleteBody(final boolean scheduledBody) {
 		if (!scheduledBody) {
 			throw new IllegalStateException("Body not scheduled for deletion!");
 		}
 
 		physicsWorld.unregisterPhysicsConnector(physicsConnector);
 
 		MyDebug.i(physicsConnector.getBody() + " will be destroyed");
 
 		physicsWorld.destroyBody(physicsConnector.getBody());
 
 		MyDebug.i(physicsConnector.getBody() + " destruction complete");
 	}
 
 	@Override
 	public void setConnector(final IShape shape) {
 		physicsConnector = physicsWorld.getPhysicsConnectorManager()
 				.findPhysicsConnectorByShape(shape);
 	}
 
 	@Override
 	public void checkCollision(final Contact contact) {
 		collisionResolver.checkCollision(contact.getFixtureA().getBody()
 				.getUserData(), contact.getFixtureB().getBody().getUserData());
 	}
 
 	@Override
 	public boolean isStraightLine(final Entity entity, final Enemy enemy) {
 		return enemyUtil.straightLine(entity, enemy);
 	}
 
 	// TODO: Will remove this later
 	@Override
 	public PhysicsWorld getPhysicsWorld() {
 		// TODO Auto-generated method stub
 		return physicsWorld;
 	}
 
 	public boolean isAreaUnOccupied(final float x, final float y, final float r) {
 		return PhysicsAreaChecker.isRectangleAreaUnoccupied(new Vector2(x - r,
 				y + r), new Vector2(x + r, y - r), physicsWorld);
 	}
 
 	public Body createType(final Circle circle, final Entity entity) {
 		if (entity instanceof Planet) {
 			return PhysicsFactory.createCircleBody(physicsWorld, circle.getX(),
 					circle.getY(), circle.getRadius(), circle.getRotation(),
 					BodyType.DynamicBody, FixtureDefs.PLANET_FIXTURE_DEF);
 		} else if (entity instanceof BlackHole) {
 			return PhysicsFactory.createCircleBody(physicsWorld, circle.getX(),
 					circle.getY(), circle.getRadius(), circle.getRotation(),
 					BodyType.DynamicBody, FixtureDefs.BLACK_HOLE_FIXTURE_DEF);
 		}
 		MyDebug.d("You shouldnt be here");
 		return null;
 	}
 
 	@Override
 	public void setContactListener() {
 		physicsWorld.setContactListener(new CollisionContactListener(this));
 
 	}
 
 	
 
 	@Override
 	public Body createType(final IShape shape, final Entity entity) {
 		if(entity instanceof Obstacle){
 			final Polygon polygon = (Polygon) shape;
 			return MyPhysicsFactory.createPolygonBody(physicsWorld,
 					polygon, BodyType.DynamicBody, FixtureDefs.OBSTACLE_FIXTURE_DEF);
 		} else 	if (entity instanceof Planet) {
 			final Circle circle = (Circle) shape;
 			return PhysicsFactory.createCircleBody(physicsWorld, circle.getX(),
 					circle.getY(), circle.getRadius(), circle.getRotation(),
 					BodyType.DynamicBody, FixtureDefs.PLANET_FIXTURE_DEF);
 		} else if (entity instanceof CircleEntity) {
 			final Circle circle = (Circle) shape;
 			return PhysicsFactory.createCircleBody(physicsWorld, circle.getX(),
 					circle.getY(), circle.getRadius(), circle.getRotation(),
 					BodyType.DynamicBody, FixtureDefs.BLACK_HOLE_FIXTURE_DEF);
 		} else if(entity instanceof RectangleEntity){
 			final RectangularShape rectangle = (RectangularShape) shape;
 			return PhysicsFactory.createCircleBody(physicsWorld,
 					rectangle, BodyType.DynamicBody, FixtureDefs.POWER_UP_FIXTURE_DEF);
 		}
 		return null;
 	}
 
 	@Override
	public void registerUpdateHandler(final GamePlayScene gamePlayScene) {
 
 		gamePlayScene.registerUpdateHandler(physicsWorld);
 		
 	}
 
 	@Override
	public void setGameWorld(final GameWorld gameWorld) {
 		this.gameWorld = gameWorld;
 		this.collisionResolver = new CollisionResolver(gameWorld);
 		
 	}
 
 	}
