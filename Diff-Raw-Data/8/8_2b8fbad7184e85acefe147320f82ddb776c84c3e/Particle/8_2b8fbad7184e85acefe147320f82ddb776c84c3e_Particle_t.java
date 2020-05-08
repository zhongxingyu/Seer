 package com.punchline.javalib.entities.components.physical;
 
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.ComponentManager;
 
 /**
  * @author MadcowD
  * @created Jul 23, 2013
  * The particle class.
  */
 public class Particle implements Velocity, Transform {
 
 	//region Fields
 	
 	private Vector2 position;
 	private float rotation;
 	private Vector2 origin;
 	private Vector2 linearVelocity;
 	private float angularVelocity;
 	
 	//endregion
 	
 	//region Initialization
 	
 	/**
 	 * Initializes a particle with a given transform.
 	 * @param position The position of the particle.
 	 * @param rotation The rotation of the particle.
 	 * @param origin The origin of the particle.
 	 */
 	public Particle(Entity e, Vector2 position, float rotation, Vector2 origin){
 		linearVelocity = new Vector2(0,0);
 		angularVelocity = 0f;
 		
 		this.origin = origin;
 		this.rotation = rotation;
 		this.position = position;
 	}
 	
 	/**
 	 * Initializes a particle with a given transform (without an origin)
 	 * @param position The position of the particle.
 	 * @param rotation The rotation of the particle.
 	 */
 	public Particle(Entity e, Vector2 position, float rotation){
 		this(e, position, rotation, new Vector2(0,0));
 	}
 
 	//endregion
 	
 	//region Transform Implementation
 
 	@Override
 	public Vector2 getPosition() {
		return position.cpy();
 	}
 
 	@Override
 	public void setPosition(Vector2 position) {
 		this.position = position;
 	}
 
 	@Override
 	public float getRotation() {
 		return rotation;
 	}
 
 	@Override
 	public void setRotation(float rotation) {
 		this.rotation = rotation;
 	}
 	
 	@Override
 	public Vector2 getOrigin() {
 		return origin;
 	}
 	
 	//endregion
 	
 	//region Velocity
 
 	@Override
 	public Vector2 getLinearVelocity() {
		return linearVelocity.cpy();
 	}
 
 	@Override
 	public void setLinearVelocity(Vector2 linearVelocity) {
		this.linearVelocity = linearVelocity.cpy();
 	}
 
 	@Override
 	public float getAngularVelocity() {
 		return angularVelocity;
 	}
 
 	@Override
 	public void setAngularVelocity(float angularVelocity) {
 		this.angularVelocity = angularVelocity;
 	}
 	
 	//endregion
 	
 	//region Events
 	
 	@Override
 	public void onAdd(ComponentManager container) { }
 
 	@Override
 	public void onRemove(ComponentManager container) { }
 
 	//endregion
 	
 }
