 package com.gemserk.games.taken;
 
 import java.util.ArrayList;
 
 import com.artemis.Entity;
 import com.artemis.EntityProcessingSystem;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.badlogic.gdx.physics.box2d.Shape.Type;
 import com.badlogic.gdx.physics.box2d.World;
 import com.gemserk.commons.artemis.components.SpatialComponent;
 import com.gemserk.commons.artemis.systems.ActivableSystem;
 import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
 
 public class PhysicsSystem extends EntityProcessingSystem implements ActivableSystem {
 	
 	ActivableSystem activableSystem = new ActivableSystemImpl();
 
 	static class PhysicsContactListener implements ContactListener {
 		@Override
 		public void endContact(Contact contact) {
 
 			Body bodyA = contact.getFixtureA().getBody();
 			Body bodyB = contact.getFixtureB().getBody();
 			
 			Entity entityA = (Entity) bodyA.getUserData();
 			Entity entityB = (Entity) bodyB.getUserData();
 			
 			if (entityA != null) {
 				PhysicsComponent physicsComponent = entityA.getComponent(PhysicsComponent.class);
 				physicsComponent.getContact().removeContact(bodyB);
 			}
 
 			if (entityB != null) {
 				PhysicsComponent physicsComponent = entityB.getComponent(PhysicsComponent.class);
 				physicsComponent.getContact().removeContact(bodyA);
 			}
 			
 		}
 
 		@Override
 		public void beginContact(Contact contact) {
 
 			Body bodyA = contact.getFixtureA().getBody();
 			Body bodyB = contact.getFixtureB().getBody();
 			
 			Entity entityA = (Entity) bodyA.getUserData();
 			Entity entityB = (Entity) bodyB.getUserData();
 			
 			if (entityA != null) {
 				PhysicsComponent physicsComponent = entityA.getComponent(PhysicsComponent.class);
 				physicsComponent.getContact().addContact(contact, bodyB);
 			}
 
 			if (entityB != null) {
 				PhysicsComponent physicsComponent = entityB.getComponent(PhysicsComponent.class);
 				physicsComponent.getContact().addContact(contact, bodyA);
 			}
 
 		}
 
 	}
 
 	World physicsWorld;
 	
 	private PhysicsContactListener physicsContactListener;
 
 	@SuppressWarnings("unchecked")
 	public PhysicsSystem(World physicsWorld) {
 		super(PhysicsComponent.class, SpatialComponent.class);
 		this.physicsWorld = physicsWorld;
 		physicsContactListener = new PhysicsContactListener();
 	}
 
 	@Override
 	protected void begin() {
 		physicsWorld.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 	}
 
 	@Override
 	protected void process(Entity e) {
 		
 		// synchronize sizes between spatial and physics components.
 		
 		PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
 		
 		// if we dont have the original shape, we cannot resize
 		if (physicsComponent.getVertices() == null)
 			return;
 		
 		Body body = physicsComponent.getBody();
 
 		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
 
 		float size = spatialComponent.getSize().x;
 		float bodySize = physicsComponent.getSize();
 
 		if (size != bodySize) {
 
 			ArrayList<Fixture> fixtureList = body.getFixtureList();
 
 			for (int i = 0; i < fixtureList.size(); i++) {
 
 				Fixture fixture = fixtureList.get(i);
 				Shape shape = fixture.getShape();
 
 				if (shape.getType() == Type.Polygon) {
 
 					Vector2[] vertices = physicsComponent.getVertices();
 
 					PolygonShape polygonShape = (PolygonShape) shape;
 					Vector2[] newVertices = new Vector2[vertices.length];
 					Box2dUtils.initArray(newVertices);
 
 					for (int j = 0; j < vertices.length; j++) {
 						newVertices[j].x = vertices[j].x * size;
 						newVertices[j].y = vertices[j].y * size;
 					}
 
 					polygonShape.set(newVertices);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return isEnabled();
 	}
 	
 	@Override
 	protected void removed(Entity e) {
 		
 		// on entity removed, we should remove body from physics world
 		
 		PhysicsComponent component = e.getComponent(PhysicsComponent.class);
 		
 		if (component == null) {
 			// throw new RuntimeException("Entity without physics component found");
 			Gdx.app.log("Archer Vs Zombies", "Entity without physics component");
 			return;
 		}
 		
 		Body body = component.getBody();
 		body.setUserData(null);
 		
 		physicsWorld.destroyBody(body);
 		
 	}
 
 	@Override
 	public void initialize() {
 		physicsWorld.setContactListener(physicsContactListener);
 	}
 
 	public World getPhysicsWorld() {
 		return physicsWorld;
 	}
 
 	public void toggle() {
 		activableSystem.toggle();
 	}
 
 	public boolean isEnabled() {
 		return activableSystem.isEnabled();
 	}
 
 
 	
 	
 
 }
