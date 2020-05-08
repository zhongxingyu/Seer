  /*
  * This class is what holds any object in the world.
  * 	The user can add/remove custom properties (though a few are 
  * 	unremovable for internal engine reasons)
  * 
  *	//TODO: Maybe come up with actions like rotate, etc that a user might want to access
  *			Programmatically, and use skynet code to queue them up and play them back?
  *			Would function for animations and such too?
  */
 package entity;
 
 import importing.pieces.Model;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import javax.vecmath.Vector3f;
 
 import monitoring.EntityObserver;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.GL11;
 
 import com.bulletphysics.collision.dispatch.CollisionObject;
 import com.bulletphysics.collision.dispatch.GhostObject;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.dynamics.RigidBody;
 import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
 import com.bulletphysics.linearmath.DefaultMotionState;
 import com.bulletphysics.linearmath.Transform;
 
 import controller.Config;
 
 public class Entity {
 	//Properties
 	private CollisionObject collision_object;
 	private HashMap<String,Object> data;
 	private Model model;
 	private ArrayList<EntityObserver> observers;
 	private boolean shouldDraw = true;
 	public static enum ObjectType{ghost,rigidbody};
 	private ObjectType object_type;
 
 	/*Properties the engine uses a lot*/
 	public static String NAME = "name";
 	
 	//Required keys
 	private String[] reqKeys = {"name", "collidable", "TTL"};
 	
 	//Keep track of number of entities for naming purposes
 	private static int num_entities=0;
 	
 	//For making entity groups (complex bodies)
 	private EntityList subEntities;
 	
 	/* Constructors */
 	public Entity(float mass,CollisionShape shape, boolean collide ) {
 		if(collide) {
 			createRigidBody(mass,shape);
 			object_type = ObjectType.rigidbody;
 		}
 		else {
 			createGhostBody(mass,shape);
 			object_type = ObjectType.ghost;
 		}
 		initialSetup(collide);
 	}
 	public Entity(String name,float mass,CollisionShape shape, boolean collide) {
 		if(collide) {
 			createRigidBody(mass,shape);
 			object_type = ObjectType.rigidbody;
 		}
 		else {
 			createGhostBody(mass,shape);
 			object_type = ObjectType.ghost;
 		}
 		initialSetup(name,collide);
 	}
 
 	/* Initializing segments */
 	//Creates the initial settings for a rigidbody
 	//This function is what we use to make things rotate over multiple axes
 	private void createRigidBody(float mass, CollisionShape shape) {
 		// rigid body is dynamic if and only if mass is non zero,
 		//otherwise static
 		boolean isDynamic = (mass != 0f);
 
 		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
 		if (isDynamic) {
 			shape.calculateLocalInertia(mass, localInertia);
 		}	
 
 		DefaultMotionState motion_state = new DefaultMotionState(new Transform());
 		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, motion_state, shape, localInertia);
 		
 		collision_object = new RigidBody(cInfo);
 		
 		//This is extremely important; if you forget this
 		//then nothing will rotate
 		((RigidBody) collision_object).setMassProps(mass, localInertia);
 		((RigidBody) collision_object).updateInertiaTensor();
 	}
 	
 	private void createGhostBody(float mass, CollisionShape shape) {
 		//rigid body is dynamic if and only if mass is non zero,
 		//otherwise static
 		GhostObject ghost = new GhostObject();
 		boolean isDynamic = (mass != 0f);
 
 		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
 		if (isDynamic) {
 			shape.calculateLocalInertia(mass, localInertia);
 		}	
 
 		ghost.setCollisionShape(shape);
 		//This is extremely important; if you forget this
 		//then nothing will rotate
 		//ghost.setMassProps(mass, localInertia);
 		//ghost.updateInertiaTensor();
 		
 		collision_object = ghost;
 	}
 	
 	//Sets the initial name of the body in the list
 	//Also sets some default options to the ent
 	private void initialSetup(boolean c){
 		num_entities++;
 		data = new HashMap<String,Object>();
 		data.put("name", "ent" + String.valueOf(num_entities));
 		data.put("collidable", c);
 		data.put("TTL", 0);
 		
 		observers = new ArrayList<EntityObserver>();
 	}
 
 	private void initialSetup(String name, boolean c){
 		num_entities++;
 		data = new HashMap<String,Object>();
 		data.put("name", name);
 		data.put("collidable", c);
 		data.put("TTL", 0);
 		
 		observers = new ArrayList<EntityObserver>();
 	}
 	/* End of Constructors
 	
 	/* MUTATORS */
 	public void setPosition(Object p) {
 		/*
 		 * There's no straight-forward way to move a RigidBody to some location
 		 * 	So that's what this class does.  It takes an Object because of skynet code
 		 * TODO: Remove skynet code
 		 */
 		try {
 			Vector3f pos = ((Vector3f) p);
 			Transform trans = collision_object.getWorldTransform(new Transform());
 			trans.origin.set(pos);
 			collision_object.setWorldTransform(trans);
 
 		} catch (Exception e) {
 			System.out.print(p.toString() + "<< Possible Incorrect data type for position, must be Vector3f\n");
 			e.printStackTrace();
 		}
 	}
 	
 	public void setProperty(String key, Object val){
 		Object old_key_val = data.get(key);
 		data.put(key,val);
 		//starter is passed to tell when to end the horrible infinite loop
 		notifyObservers(key, old_key_val, val);
 	}
 	
 	public void removeProperty(String key){
 		//Protect our required keys. Don't delete those, oh no!
 		boolean req = false;
 		for( int i = 0; i < reqKeys.length; i++){
 			if(reqKeys[i].equals(key))
 				req = true;
 		}
 		if(!req){
 			data.remove(key);
 		}
 	}
 
 	public void setModel(Model model) {this.model = model;}
 	public void setShouldDraw(boolean shouldDraw) {this.shouldDraw = shouldDraw;}
 	
 	/* ACCESSORS */
 	public Vector3f getPosition(){
 		Transform out = new Transform();
 		out = collision_object.getWorldTransform(new Transform());
 		return out.origin;
 
 	}
 	public EntityList getSubEntities(){return subEntities;}
 	public Set<String> getKeys() {return data.keySet();}
 	public boolean keyExists(String prop_name){
 		for( int i = 0; i < data.keySet().size(); i++){
 			//TODO: Probably a better way to loop through the keys, but I'm lazy
 			if((data.keySet().toArray())[i].equals(prop_name))
 				return true;
 		}
 		return false;
 	}
 	public Object getProperty(String key){return data.get(key);}
 
 	public Model getModel() {return model;}
 	public Set<String> getKeySet(){return data.keySet();}
 	public boolean shouldDraw() {return shouldDraw;}
 	
 	/* MISC */
 
 	public void draw(){

 		//Retrieve the current motionstate to get the transform
 		//versus the world
 		if( shouldDraw && this.getObjectType()==ObjectType.rigidbody){
 			GL11.glPushMatrix();
 				//Retrieve the current motionstate to get the transform
 				//versus the world
 				Transform transform_matrix = new Transform();
 				DefaultMotionState motion_state = (DefaultMotionState) ((RigidBody) collision_object).getMotionState();
 
 				transform_matrix.set(motion_state.graphicsWorldTrans);
 				
 				//Adjust the position and rotation of the object from physics
 				float[] body_matrix = new float[16];
 				FloatBuffer buf = BufferUtils.createFloatBuffer(16);
 				transform_matrix.getOpenGLMatrix(body_matrix);
 				buf.put(body_matrix);
 				buf.flip();
 				GL11.glMultMatrix(buf);
 				buf.clear();
 				
 				//Scaling code (testing)
 				Vector3f halfExtent = new Vector3f();
 				collision_object.getCollisionShape().getLocalScaling(halfExtent);
 				GL11.glScalef(1.0f * halfExtent.x, 1.0f * halfExtent.y, 1.0f * halfExtent.z);
 				
 				//Draw the model
 				GL11.glPushMatrix();
 					if( model != null)
 						model.draw();		
 				GL11.glPopMatrix();
 			GL11.glPopMatrix();
 		} else {
 			//System.out.println("Method [draw] not supported for ghost object");
 		}
 	}
 
 	
 	/* Functions for EntityObservers */
 	public void registerObserver(EntityObserver o) {observers.add(o);}
 
 	public void removeObserver(EntityObserver o) {
 		int i = observers.indexOf(o);
 		if (i >= 0){
 			observers.remove(i);
 		}
 	}
 
 	public void notifyObservers(String key, Object old_name, Object new_name) {
 
 		for(int i = 0; i < observers.size(); i++){
 			EntityObserver observer = (EntityObserver)observers.get(i);
 			observer.update(key, old_name, new_name);
 		}
 	}
 
 	public void setCollisionFlags(int kinematic_object) {
 		collision_object.setCollisionFlags(kinematic_object);	
 	}
 	public void setGravity(Vector3f gravity) {
 		if(object_type==ObjectType.rigidbody)
 			((RigidBody) collision_object).setGravity(gravity);
 		else
 			System.out.println("Method [setGravity] not supported for ghost object");
 	}
 	public void applyImpulse(Vector3f impulse, Vector3f position) {
 		if(object_type==ObjectType.rigidbody)
 			((RigidBody) collision_object).applyImpulse(impulse, position);
 		else
 			System.out.println("Method [applyImpulse] not supported for ghost object");
 	}
 	public CollisionObject getCollisionObject() {
 		return collision_object;
 	}
 	public void setMotionState(DefaultMotionState defaultMotionState) {
 		if(object_type==ObjectType.rigidbody)
 			((RigidBody) collision_object).setMotionState(defaultMotionState);
 		else
 			System.out.println("Method [setActivation] not supported for ghost object");		
 	}
 	public ObjectType getObjectType() {
 		return object_type;
 	}
 	public void activate() {
 		collision_object.activate();
 	}
 	public void setCollisionShape(CollisionShape createCollisionShape) {
 		collision_object.setCollisionShape(createCollisionShape);		
 	}
 	
 	public void setAngularFactor(float factor,Vector3f velocity){
 		if(object_type==ObjectType.rigidbody) {
 			((RigidBody) collision_object).setAngularFactor(factor);
 			((RigidBody) collision_object).setAngularVelocity(velocity);
 		}
 		else
 			System.out.println("Method [setAngularFactor] not supported for ghost object");	
 	}
 	
 	public void setDamping(float linear_damping, float angular_damping) {
 		if(object_type==ObjectType.rigidbody) {
 			//((RigidBody) collision_object).setInterpolationLinearVelocity(velocity);
 			((RigidBody) collision_object).setDamping(linear_damping, angular_damping);
 			//((RigidBody) collision_object).applyDamping(0);
 		}
 		else
 			System.out.println("Method [setVelocity] not supported for ghost object");	
 	}
 	public void setAngularIdentity() {
 		if(object_type==ObjectType.rigidbody) {
 			//((RigidBody) collision_object).setInterpolationLinearVelocity(velocity);
 			DefaultMotionState motionState = new DefaultMotionState();
 			Transform t = new Transform();
 			t.setIdentity();
 			motionState.setWorldTransform(t);
 			((RigidBody) collision_object).setMotionState(motionState);
 			//((RigidBody) collision_object).applyDamping(0);
 		}
 		else
 			System.out.println("Method [setVelocity] not supported for ghost object");	
 	}
 }
