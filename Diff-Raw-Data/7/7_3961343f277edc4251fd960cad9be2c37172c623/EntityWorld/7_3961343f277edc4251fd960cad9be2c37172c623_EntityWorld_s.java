 package com.punchline.javalib.entities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.JointEdge;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Disposable;
 import com.punchline.javalib.entities.systems.physical.EntityRemovalSystem;
 import com.punchline.javalib.entities.systems.physical.ParticleSystem;
 import com.punchline.javalib.entities.systems.render.DebugRenderSystem;
 import com.punchline.javalib.entities.systems.render.RenderSystem;
 
 public abstract class EntityWorld implements Disposable {
 
 	private final float TIME_STEP = 1.0f / 60.0f;
 	private final int VELOCITY_ITERATIONS = 6;
 	private final int POSITION_ITERATIONS = 2;
 	
 	/**
 	 * The InputMultiplexer managing this physicsWorld's game.
 	 */
 	protected InputMultiplexer input;
 	
 	/**
 	 * This physicsWorld's {@link EntityManager}.
 	 */
 	protected EntityManager entities;
 	
 	/**
 	 * This physicsWorld's {@link SystemManager}.
 	 */
 	protected SystemManager systems;
 	
 	/**
 	 * Template map.
 	 */
 	private Map<String, EntityTemplate> templates;
 	
 	/**
 	 * Group template map.
 	 */
 	private Map<String, EntityGroupTemplate> groupTemplates;
 	
 	/**
 	 * This physicsWorld's Box2D {@link com.badlogic.gdx.physics.box2d.World World}
 	 */
 	protected World physicsWorld;
 	
 	
 	/**
 	 * List for safely removing bodies.
 	 */
 	private ArrayList<com.badlogic.gdx.physics.box2d.Body> bodiesToRemove;
 
 	
 	/**
 	 * This physicsWorld's {@link com.badlogic.gdx.graphics.Camera Camera}.
 	 */
 	protected Camera camera;
 	
 	
 	//SYSTEMS
 	
 	/**
 	 * This world's {@link RenderSystem}.
 	 */
 	protected RenderSystem renderSystem;
 	
 	/**
 	 * This world's {@link DebugRenderSystem}.
 	 */
 	protected DebugRenderSystem debugView;
 	
 	/**
 	 * The physicsWorld's {@link ContactManager}.
 	 */
 	protected ContactManager contactManager;
 
 	
 	
 	//INIT
 	
 	/**
 	 * Instantiates the EntityWorld's {@link EntityManager}, {@link SystemManager}, and template map.
 	 * @param input The InputMultiplexer of the game containing this EntityWorld.
 	 * @param camera The camera that will be used for rendering this physicsWorld.
 	 * @param gravity The gravity vector2.
 	 * @param doSleeping Whether the physicsWorld allows sleeping.
 	 */
 	public EntityWorld(InputMultiplexer input, Camera camera, Vector2 gravity, boolean doSleeping) {
 		entities = new EntityManager();
 		
 		systems = new SystemManager(this);
 		
 		templates = new HashMap<String, EntityTemplate>();
 		groupTemplates = new HashMap<String, EntityGroupTemplate>();
 		
 		this.input = input;
 		this.camera = camera;
 		positionCamera();
 		
 		physicsWorld = new World(gravity, doSleeping);
 		bodiesToRemove = new ArrayList<com.badlogic.gdx.physics.box2d.Body>();
 		contactManager = new ContactManager(physicsWorld);
 		
 		buildTemplates();
 		buildSystems();
 		buildEntities();
 	}
 	
 	//TODO this is a bad quick fix
 	boolean cameraInit = false;
 	
 	/**
 	 * Adds necessary systems to the physicsWorld. Called by the constructor.
 	 */
 	protected void buildSystems() {
 		
 		//RENDER
 		renderSystem = (RenderSystem)systems.addSystem(new RenderSystem(camera));
 		debugView = (DebugRenderSystem)systems.addSystem(new DebugRenderSystem(input, getPhysicsWorld(), camera, systems));
 		
 		input.addProcessor(debugView);
 		
 		//PHYSICAL
 		systems.addSystem(new ParticleSystem());
 		systems.addSystem(new EntityRemovalSystem());
 		
 		//GENERIC
 		systems.addSystem(new EntitySpawnerSystem());
 		
 	}
 	
 	/**
 	 * Adds necessary templates to the physicsWorld. Called by the constructor.
 	 */
 	protected void buildTemplates() { }
 	
 	/**
 	 * Adds necessary entities to the physicsWorld. Called by the constructor.
 	 */
 	protected void buildEntities() { }
 	
 	
 	
 	//FUNCTIONING LOOP
 	
 	/**
 	 * Disposes of all EntitySystems, and the physics physicsWorld.
 	 */
 	@Override
 	public void dispose() {
 		
 		systems.dispose();
 		physicsWorld.dispose();
 		
 	}
 	
 	/**
 	 * Runs all system processing.
 	 */
 	public void process() {
 		
 		if (!cameraInit) { //TODO THIS IS A HORRIBLE QUICK FIX
 			positionCamera();
 			cameraInit = true;
 		}
 		
 		systems.process(
 				entities.getNewEntities(), 
 				entities.getChangedEntities(), 
 				entities.getRemovedEntities(), Gdx.graphics.getDeltaTime());
 		
 		entities.process();
 		
 		//REMOVE BODIES SAFELY
 		for(int i = 0; i < bodiesToRemove.size(); i++){
 			Body body = bodiesToRemove.get(i);
 		    //to prevent some obscure c assertion that happened randomly once in a blue moon
 		    final ArrayList<JointEdge> list = body.getJointList();
 		    while (list.size() > 0) {
 		        physicsWorld.destroyJoint(list.get(0).joint);
 		    }
 		    // actual remove
 		    physicsWorld.destroyBody(body);
 		}
 		bodiesToRemove.clear();
 		
 		physicsWorld.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
 	}
 	
 	
 	
 	
 	//GETTERS/SETTERS
 	
 	/**
 	 * @return This physicsWorld's boundaries.
 	 */
 	public abstract Rectangle getBounds();
 	
 	/**
 	 * @return This physicsWorld's Box2D {@link com.badlogic.gdx.physics.box2d.World World}
 	 */
 	public World getPhysicsWorld() {
 		return physicsWorld;
 	}
 	
 	/**
 	 * @return This physicsWorld's {@link com.badlogic.gdx.graphics.Camera Camera}.
 	 */
 	public Camera getCamera() {
 		return camera;
 	}
 	
 	/**
 	 * The entity count in the physicsWorld.
 	 * @return The count of entities active in the physicsWorld.
 	 */
 	public int getEntityCount(){
 		return entities.getEntities().size();
 	}
 	
 	
 	/**
 	 * Positions the camera.
 	 */
 	protected void positionCamera() { }
 	
 	
 	/**
 	 * Marks a body for safe deletion.
 	 * @param Body The body to be removed.
 	 */
 	public void safelyRemoveBody(com.badlogic.gdx.physics.box2d.Body body){
 		if(!bodiesToRemove.contains(body))
 			bodiesToRemove.add(body);
 	}
 
 	
 	
 	//ENTITY/TEMPLATE CREATION
 	/**
 	 * Creates an {@link Entity} using the {@link EntityTemplate} associated with the given tag.
 	 * @param template The tag of the template.
 	 * @param args Arguments for creating the {@link Entity}.
 	 * @return The created entity.
 	 */
 	public Entity createEntity(String template, Object... args) {
 		Entity e = templates.get(template).buildEntity(entities.obtain(), this, args); //Grab an entity from the entity pool and send it
 		entities.add(e);
 		return e;
 	}
 	
 	/**
 	 * Creates a group of Entities using the {@link EntityGroupTemplate} associated with the given tag.
 	 * @param template The tag of the template to use.
 	 * @param args Arguments for creating the entity group.
 	 * @return The group of entities.
 	 */
 	public ArrayList<Entity> createEntityGroup(String template, Object... args) {
 		ArrayList<Entity> group = groupTemplates.get(template).buildEntities(this, args);
 		
 		for (Entity e : group) {
 			entities.add(e); //Add the group to the physicsWorld.
 		}
 		
 		return group;
 	}
 	
 
 	/**
 	 * Adds an EntityTemplate to the template map.
 	 * @param templateKey The template's key.
 	 * @param template The template.
 	 */
 	public void addTemplate(String templateKey, EntityTemplate template) {
 		templates.put(templateKey, template);
 	}
 	
 	/**
 	 * Adds an EntityGroupTemplate to the group template map.
 	 * @param templateKey The template's key.
 	 * @param template The template.
 	 */
 	public void addGroupTemplate(String templateKey, EntityGroupTemplate template) {
 		groupTemplates.put(templateKey, template);
 	}
 
 }
