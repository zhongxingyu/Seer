 package com.punchline.NinjaSpacePirate.gameplay.entities.processes.movement;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.punchline.NinjaSpacePirate.gameplay.entities.processes.MovementProcess;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.EntityWorld;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.physical.Velocity;
 import com.punchline.javalib.entities.processes.ProcessState;
 import com.punchline.javalib.utils.LogManager;
 
 /**
  * Process that makes an Entity continually patrol between a list of points.
  * @author Natman64
  *
  */
 public class PatrolProcess extends MovementProcess {
 
 	/** Entities will be considered as having reached a patrol point if they come within this many meters. */
 	private static final float ERROR_TOLERANCE = 0.06f;
 	
 	private Entity patroller;
 	private float patrolSpeed;
 	
 	private int nextPoint = 0;
 	private Array<Vector2> patrolPoints = new Array<Vector2>();
 	
 	public PatrolProcess(EntityWorld world, Entity patroller, float patrolSpeed, Vector2... patrolPoints) {
 		super(world, patroller);
 		
 		if (missingComponents(patroller)) {
 			throw new IllegalArgumentException("Tried to attach a patrol process to an entity without either a transform or velocity component.");
 		}
 		
 		this.patrolSpeed = patrolSpeed;
 		
 		for (Vector2 point : patrolPoints) {
 			this.patrolPoints.add(point);
 		}
 		
 		endOnDeath(patroller);
 		
 		LogManager.debug("Movement", "PatrolProcess created");
 	}
 	
 	@Override
 	public void update(EntityWorld world, float deltaTime) {
 		if (missingComponents(patroller)) {
 			throw new IllegalArgumentException("Cannot run PatrolProcess - entity is missing either a transform or velocity component.");
 		}

 		if (!e.getType().equals("Patroller")) {
 			throw new IllegalArgumentException("Cannot run PatrolProcess - entity is not a patroller");
 		}
 		
 		LogManager.debug("Movement", "PatrolProcess running");
 		
 		Transform t = e.getComponent(Transform.class);
 		Velocity v = e.getComponent(Velocity.class);
 		
 		Vector2 destination = patrolPoints.get(nextPoint);
 		
 		if (destination.dst(t.getPosition()) <= ERROR_TOLERANCE) {
 			nextPoint = ++nextPoint % patrolPoints.size;
 		}
 		
 		Vector2 velocity = destination.cpy().sub(t.getPosition());
 		velocity.nor();
 		velocity.scl(patrolSpeed);
 		
 		v.setLinearVelocity(velocity);
 		
 		t.setRotation((float) Math.toRadians(velocity.angle()));
 		
 	}
 
 	private boolean missingComponents(Entity e) {
 		return !e.hasComponent(Transform.class) || !e.hasComponent(Velocity.class);
 	}
 
 	@Override
 	public void onEnd(EntityWorld world, ProcessState endState) {
 		super.onEnd(world, endState);
 		
 		LogManager.debug("Movement", "PatrolProcess ended");
 	}
 	
 }
