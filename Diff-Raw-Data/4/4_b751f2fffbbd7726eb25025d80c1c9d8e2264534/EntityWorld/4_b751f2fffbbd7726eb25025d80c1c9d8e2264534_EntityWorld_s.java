 package com.punchline.javalib.entities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.physics.box2d.World;
 import com.punchline.javalib.entities.systems.RenderSystem;
 
 //TODO: Make abstract
 public class EntityWorld {
 
 	final float TIME_STEP = 1.0f / 60.0f;
 	final int VELOCITY_ITERATIONS = 6;
 	final int POSITION_ITERATIONS = 2;
 	
 	/**
 	 * This world's {@link EntityManager}.
 	 */
 	protected EntityManager entities;
 	
 	/**
 	 * This world's {@link SystemManager}.
 	 */
 	protected SystemManager systems;
 	
 	/**
 	 * This world's {@link RenderSystem}.
 	 */
 	protected RenderSystem renderSystem;
 	
 	/**
 	 * This world's Box2D {@link com.badlogic.gdx.physics.box2d.World World}
 	 */
 	protected World physicsWorld;
 	
 	/**
 	 * This world's {@link com.badlogic.gdx.graphics.Camera Camera}.
 	 */
 	protected Camera camera;
 	
 	/**
 	 * Template map.
 	 */
 	private Map<String, EntityTemplate> templates;
 	
 	/**
 	 * Instantiates the EntityWorld's {@link EntityManager}, {@link SystemManager}, and template map.
 	 * @param camera The camera that will be used for rendering this world.
 	 */
 	public EntityWorld(Camera camera) {
 		entities = new EntityManager();
 		
 		systems = new SystemManager();
 		
 		templates = new HashMap<String, EntityTemplate>();
 		
 		this.camera = camera;
 		
 		buildComponents();
 		buildSystems();
 		buildTemplates();
 		buildEntities();
 	}
 	
 	/**
 	 * Runs all system processing.
 	 */
 	public void process() {
 		
 		entities.process();
 		
 		systems.process(
 				entities.getNewEntities(), 
 				entities.getChangedEntities(), 
				entities.getRemovedEntities());
 		
 		physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
 		
 	}
 	
 	/**
 	 * @return This world's Box2D {@link com.badlogic.gdx.physics.box2d.World World}
 	 */
 	public World getPhysicsWorld() {
 		return physicsWorld;
 	}
 	
 	/**
 	 * @return This world's {@link com.badlogic.gdx.graphics.Camera Camera}.
 	 */
 	public Camera getCamera() {
 		return camera;
 	}
 	
 	/**
 	 * Creates an {@link Entity} using the {@link EntityTemplate} associated with the given tag.
 	 * @param template The tag of the template.
 	 * @param args Arguments for creating the {@link Entity}.
 	 * @return The created entity.
 	 */
 	public Entity createEntity(String template, Object... args) {
 		Entity e = templates.get(template).buildEntity(args);
 		entities.add(e);
 		return e;
 	}
 	
 	/**
 	 * Adds necessary components to the world. Called by the constructor.
 	 */
 	protected void buildComponents() { }
 	
 	/**
 	 * Adds necessary systems to the world. Called by the constructor.
 	 */
 	protected void buildSystems() {
 		
 		renderSystem = (RenderSystem)systems.addSystem(new RenderSystem(camera));
 		
 	}
 	
 	/**
 	 * Adds necessary templates to the world. Called by the constructor.
 	 */
 	protected void buildTemplates() { }
 	
 	/**
 	 * Adds necessary entities to the world. Called by the constructor.
 	 */
 	protected void buildEntities() { }
 	
 }
