 package fr.kohen.alexandre.framework.systems;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import fr.kohen.alexandre.framework.base.Systems;
 import fr.kohen.alexandre.framework.components.*;
 import fr.kohen.alexandre.framework.systems.interfaces.*;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.managers.TagManager;
 import com.artemis.systems.EntityProcessingSystem;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 
 public class DefaultCameraSystem extends EntityProcessingSystem implements CameraSystem, ContactListener {
 	protected ComponentMapper<CameraComponent> 	cameraMapper;
 	protected ComponentMapper<Transform> 		transformMapper;
 	protected ImmutableBag<Entity>				cameras;
 	protected PhysicsSystem 					box2dSystem;
 
 	@SuppressWarnings("unchecked")
 	public DefaultCameraSystem() {
 		super( Aspect.getAspectForAll(CameraComponent.class) );
 		cameras			= new Bag<Entity>();
 	}
 
 	@Override
 	protected void initialize() {
 		transformMapper = ComponentMapper.getFor(Transform.class, world);
 		cameraMapper 	= ComponentMapper.getFor(CameraComponent.class, world);
 		box2dSystem		= Systems.get(PhysicsSystem.class, world);
 		
 		if ( box2dSystem == null ) {
 			throw new RuntimeException("Required System not loaded");
 		} else {
 			box2dSystem.addContactListener(this);
 		}
 	}
 
 	@Override
 	protected void process(Entity camera) {
 		cameraMapper.get(camera).entities = getSortedEntities( cameraMapper.get(camera).entities );
 		processCamera(camera);		
 	}
 	
 
 	/**
 	 * Override to add custom camera behavior
 	 * Default implementation has two behavior: "cameraFollowPlayer" and "cameraRotation"
 	 * @param camera
 	 */
 	protected void processCamera(Entity camera) {
 		
 		if( cameraMapper.get(camera).name.startsWith("cameraFollowPlayer") ) {
 			Entity player = world.getManager(TagManager.class).getEntity("player");
 			if( player != null ) {
 				transformMapper.get(camera)
 					.setPosition( transformMapper.get(player).getPosition2() )
 					.setMapId( transformMapper.get(player).mapId );
 			}
 		} else if( cameraMapper.get(camera).name.startsWith("cameraRotation") ) {
 			//transformMapper.get(camera).rotation = 0;
 			//cameraMapper.get(camera).position.x = 0;
 			//cameraMapper.get(camera).position.y = 0;
 			cameraMapper.get(camera).rotation += 1;
 		}
 		
 	}
 
 	private List<Entity> getSortedEntities(List<Entity> entities) {
 		List<SortableEntity> sortableEntities = new ArrayList<SortableEntity>();
 		for( Entity e : entities ) {
			if( e.isActive() ) {
				sortableEntities.add( new SortableEntity(e, transformMapper.get(e).getPosition().z) );
			}
 		}
 		Collections.sort(sortableEntities);
 		
 		List<Entity> sortedEntities = new ArrayList<Entity>();
 		for ( SortableEntity e : sortableEntities ) {
 			sortedEntities.add(e.getEntity());
 		}
 		sortableEntities.clear();
 		
 		return sortedEntities;
 	}
 
 	protected void inserted(Entity e) { ((Bag<Entity>) cameras).add(e); };
 	protected void removed(Entity e) { ((Bag<Entity>) cameras).remove(e); };
 	
 	
 	@Override
 	public ImmutableBag<Entity> getCameras() { return cameras; }
 	
 	public void addToCamera(Entity camera, Entity entity) {	
 		if( ! cameraMapper.get( camera ).entities.contains(entity) ) {
 			cameraMapper.get( camera ).entities.add( entity );
 		}
 	}
 	public void removeFromCamera(Entity camera, Entity entity) {
 		cameraMapper.get( camera ).entities.remove( entity );
 	}
 	
 	
 	@Override
 	public void beginContact(Contact contact) {
 		if ( contact.getFixtureA().getUserData() instanceof String && ((String) contact.getFixtureA().getUserData()).equalsIgnoreCase("cameraSensor") ) {
 			addToCamera( (Entity) contact.getFixtureA().getBody().getUserData(), (Entity) contact.getFixtureB().getBody().getUserData() );
 		}
 		if ( contact.getFixtureB().getUserData() instanceof String && ((String) contact.getFixtureB().getUserData()).equalsIgnoreCase("cameraSensor") ) {
 			addToCamera( (Entity) contact.getFixtureB().getBody().getUserData(), (Entity) contact.getFixtureA().getBody().getUserData() );
 		}
 	}
 
 	@Override
 	public void endContact(Contact contact) {
 		if ( contact.getFixtureA().getUserData() instanceof String && ((String) contact.getFixtureA().getUserData()).equalsIgnoreCase("cameraSensor") ) {
 			removeFromCamera( (Entity) contact.getFixtureA().getBody().getUserData(), (Entity) contact.getFixtureB().getBody().getUserData() );
 		}
 		if ( contact.getFixtureB().getUserData() instanceof String && ((String) contact.getFixtureB().getUserData()).equalsIgnoreCase("cameraSensor") ) {
 			removeFromCamera( (Entity) contact.getFixtureB().getBody().getUserData(), (Entity) contact.getFixtureA().getBody().getUserData() );
 		}
 	}
 
 	@Override
 	public void preSolve(Contact contact, Manifold oldManifold) {	
 	}
 
 	@Override
 	public void postSolve(Contact contact, ContactImpulse impulse) {	
 	}
 	
 	private class SortableEntity implements Comparable<SortableEntity> {
 		private Entity e;
 		private float depth;
 
 		public SortableEntity(Entity e, float depth) {
 			this.e = e;
 			this.depth = depth;
 		}
 		
 		public Entity getEntity() {
 			return this.e;
 		}
 		
 		@Override
 		public int compareTo(SortableEntity other) {
 			if( this.depth > other.depth) {
 				return 1;
 			} else if( this.depth < other.depth ) {
 				return -1;
 			} else return 0;
 		}
 	}
 
 }
