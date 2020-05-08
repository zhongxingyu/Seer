 package com.gemserk.games.newtod.templates;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.EntityManager;
 import com.artemis.World;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.gemserk.commons.artemis.components.PhysicsComponent;
 import com.gemserk.commons.artemis.components.PreviousStateSpatialComponent;
 import com.gemserk.commons.artemis.components.ScriptComponent;
 import com.gemserk.commons.artemis.components.SpatialComponent;
 import com.gemserk.commons.artemis.scripts.ScriptJavaImpl;
 import com.gemserk.commons.artemis.templates.EntityTemplateImpl;
 import com.gemserk.commons.gdx.GlobalTime;
 import com.gemserk.commons.gdx.games.SpatialPhysicsImpl;
 import com.gemserk.games.newtod.path.Path;
 import com.gemserk.games.newtod.path.Path.PathTraversal;
 import com.gemserk.games.newtod.systems.components.CreepDataComponent;
 
 public class CreepTemplate extends EntityTemplateImpl{
 
 	EntityTemplatesHelper helper;
 	ComponentMapper<CreepDataComponent> creepDataMapper;
 	private ComponentMapper<SpatialComponent> spatialMapper;
 
 	public CreepTemplate(EntityTemplatesHelper helper) {
 		this.helper = helper;
 		World world = helper.world;
 		EntityManager entityManager = world.getEntityManager();
 		creepDataMapper = new ComponentMapper<CreepDataComponent>(CreepDataComponent.class, entityManager);
 		spatialMapper = new ComponentMapper<SpatialComponent>(SpatialComponent.class, entityManager);
 	}
 
 	@Override
 	public void apply(Entity entity) {
 		Path path = parameters.get("path");
 		final Vector2 pathEnd = path.getEndPosition();
		final Float speed = parameters.get("speed");
 		
		Float startDistanceInPath = parameters.get("startDistanceInPath",0f);
 		
 		
 		PathTraversal pathTraversal = path.getTraversal();
 		
 		pathTraversal.advance(startDistanceInPath);
 		
 		CreepDataComponent creepData = new CreepDataComponent(speed, pathTraversal);
 		
 		entity.addComponent(creepData);
 		
 		ScriptComponent scriptComponent = new ScriptComponent(new ScriptJavaImpl() {
 			@Override
 			public void update(World world, Entity e) {
 				CreepDataComponent creepDataComponent  = creepDataMapper.get(e);
 				PathTraversal pathTraversal = creepDataComponent.pathTraversal;
 				pathTraversal.advance(speed * GlobalTime.getDelta());
 
 				Vector2 position = pathTraversal.getPosition();
 				if (position.dst(pathEnd) < 0.1f) {
 					pathTraversal.reset();
 				}
 				
 				SpatialComponent spatialComponent = spatialMapper.get(e);
 				spatialComponent.setPosition(pathTraversal.getPosition());
 			}
 		});
 		
 		
 		
 		entity.addComponent(scriptComponent);
 		
 		Body body = helper.bodyBuilder.fixture(//
 				helper.bodyBuilder.fixtureDefBuilder().circleShape(5f)).type(BodyType.StaticBody).build();
 		
 		entity.addComponent(new PhysicsComponent(body));
 		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 10, 10)));
 	}
 	
 	/**
 	 * speed
 	 * pathtraversal
 	 * hitpoints
 	 */
 }
