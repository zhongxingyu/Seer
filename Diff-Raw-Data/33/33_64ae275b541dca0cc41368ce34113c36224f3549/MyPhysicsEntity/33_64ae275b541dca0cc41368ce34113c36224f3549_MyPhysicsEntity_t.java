 package com.secondhand.view.physics;
 
 import java.util.List;
 
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.entity.CircleEntity;
 import com.secondhand.model.entity.Entity;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.physics.IPhysicsEntity;
 import com.secondhand.model.physics.IPhysicsObject;
 import com.secondhand.view.scene.ThreadUtil;
 
 public class MyPhysicsEntity implements IPhysicsEntity {
 	private final Body body;
 	private final PhysicsWorld physicsWorld;
 	private final PhysicsConnector physicsConnector;
 	private final IShape shape;
 	private final PhysicsEnemyUtil enemyUtil;
 
 	public MyPhysicsEntity(final PhysicsWorld physicsWorld,
 			final Entity entity, final IShape shape, final Body body) {
 		this.physicsWorld = physicsWorld;
 		enemyUtil = new PhysicsEnemyUtil(physicsWorld);
 		body.setUserData(entity);
 		
 		
 		physicsConnector = new CustomPhysicsConnector(shape, entity instanceof CircleEntity,
 				body, true, !(entity instanceof Player));
 		physicsWorld.registerPhysicsConnector(physicsConnector);
 		this.body = body;
 		this.shape = shape;
 	}
 
 	@Override
 	public float getCenterX() {
 		return body.getWorldCenter().x
 				* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	}
 
 	@Override
 	public float getCenterY() {
 		return body.getWorldCenter().y
 				* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 	}	
 
 	@Override
 	public void deleteBody() {
 		
		ThreadUtil.getInstance().runOnUpdateThread(new ThreadUtil.Method() {
 			@Override
 			public void method() {
 				physicsWorld.unregisterPhysicsConnector(physicsConnector);
 				physicsWorld.destroyBody(body);
 
 				MyDebug.i(physicsConnector.getBody() + " destruction complete");						
 
 			}
 		});
 	}
 
 	@Override
 	public void setLinearDamping(final float linearDamping) {
 		body.setLinearDamping(linearDamping);
 
 	}
 
 	@Override
 	public void detachSelf() {
 		this.shape.detachSelf();
 	}
 
 	private Vector2 getCenterOfMass() {
 		final Vector2 v = new Vector2(body.getMassData().center.x,
 				body.getMassData().center.y);
 
 		return new Vector2(v.x * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
 				v.y * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
 	}
 	
 	@Override
 	public boolean isStraightLine(final IPhysicsObject entity, final IPhysicsObject enemy) {
 		return enemyUtil.straightLine(entity, enemy);
 	}
 
 	@Override
 	public float computePolygonRadius(
 			final List<com.secondhand.model.physics.Vector2> polygon) {
 
 		// we define the radius to be the maximum length between the center of
 		// mass
 		// and a vertex in the polygon.
 
 		float maxLength = 0;
 
 		final Vector2 center = getCenterOfMass();
 
 		for (int i = 0; i < polygon.size(); ++i) {
 			final float length = (float) Math.sqrt(Math.pow(polygon.get(i).x
 					- center.x, 2)
 					+ Math.pow(polygon.get(i).y - center.y, 2));
 			if (length > maxLength) {
 				maxLength = length;
 			}
 		}
 
 		return maxLength;
 	}
 
 	@Override
 	public void setTransform(final com.secondhand.model.physics.Vector2 position) {
 
 		body.setTransform(new com.badlogic.gdx.math.Vector2(position.x
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, position.y
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
 				body.getAngle());
 
 	}
 
 	// andEngine or box2d coordinates in? and depending on from
 	// where we call the method we could perhaps have an vector as input.
 	// We souldn't need to do much more here, all other calculations should
 	// be done in model. Entity instead of body and then somehow get body?
 	// All entities that need this function are enemies and player.
 	@Override
 	public void applyImpulse(final com.secondhand.model.physics.Vector2 force,
 			final float maxSpeed) {
 		final Vector2 velocity = body.getLinearVelocity();
 
 		final Vector2 impulse = new Vector2(force.x, force.y);
 		final float speed = velocity.add(impulse).len();
 		if (speed > maxSpeed) {
 			// Check if new velocity doesn't exceed maxSpeed!
 			return;
 		}
 		body.applyLinearImpulse(impulse, body.getWorldCenter());
 
 	}
 
 	@Override
 	public void applyImpulse(final com.secondhand.model.physics.Vector2 impulse,
 			final com.secondhand.model.physics.Vector2 impulsePosition) {
 
 		body.applyLinearImpulse(new Vector2(impulse.x
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, impulse.y
 				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT), new Vector2(
 				impulsePosition.x
 						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
 				impulsePosition.y
 						/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT));
 	}
 
 	@Override
 	public void stopMovment() {
 		body.setAngularVelocity(0);
 		body.setLinearVelocity(0, 0);
 
 	}
 }
