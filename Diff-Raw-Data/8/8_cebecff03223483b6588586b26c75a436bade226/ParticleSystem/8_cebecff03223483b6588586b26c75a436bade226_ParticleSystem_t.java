 /**
  * 
  */
 package com.punchline.javalib.entities.systems.physical;
 
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.Entity;
 import com.punchline.javalib.entities.components.physical.Particle;
 import com.punchline.javalib.entities.systems.ComponentSystem;
 
 /**
  * The particle system which updates the position of particles
  * @author William
  * @created Jul 23, 2013
  */
 public class ParticleSystem extends ComponentSystem {
 
 	/**
 	 * Initializes the particle system for particle components.
 	 */
 	public ParticleSystem(){
 		super(Particle.class);
 	}
 	
 	/** {@inheritDoc}
 	 * @see com.badlogic.gdx.utils.Disposable#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/** {@inheritDoc}
 	 * @see com.punchline.javalib.entities.EntitySystem#process(com.punchline.javalib.entities.Entity)
 	 */
 	@Override
 	protected void process(Entity e) {
 		Particle p = e.getComponent();
 		
 		//Move the particle
 		Vector2 pos = p.getPosition().cpy();
		Vector2 velocity = new Vector2(p.getLinearVelocity().x * deltaSeconds(), p.getLinearVelocity().y * deltaSeconds());
		pos.add(velocity);
		p.setPosition(pos);
		
		float angularVelocity = p.getAngularVelocity() * deltaSeconds();
		p.setRotation(p.getRotation() + angularVelocity);
 	}
 
 }
