 package actor;
 
 import math.*;
 import graphics.Model;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 
 import javax.media.opengl.GL2;
 
 public abstract class Actor implements Serializable {
     private static final long serialVersionUID = 744085604446096658L;
     /**
      * All the actors currently in play We use the fully qualified named space
      * for the Vector container so it doesn't clash with our name space. Vectors
      * work like ArrayLists, but are synchronized.
      */
     private static List<Actor> actors = Collections.synchronizedList(new java.util.ArrayList<Actor>());
     /**
      * Common random number generator object
      */
     protected static Random gen = new Random();
     private static int lastId = 0;
 
     /**
      * Thread safe way to remove actors
      * @param idToRemove
      */
     public static void removeActorId(int idToRemove) {
        synchronized(actors) {
             for (ListIterator<Actor> it = actors.listIterator(); it.hasNext(); ) {
                 Actor a = it.next();
                 if (a.getId() == idToRemove)
                     it.remove();
             }
         }
     }
     
     public static void addActor(Actor actor) {
         synchronized(actors) {
             actors.add(actor);
         }
     }
 
     public static void removeActor(Actor actor) {
         synchronized(actors) {
             actors.remove(actor);
         }
     }
 
     public static void updateActors() {
         synchronized(actors) {
             // Update each actor
             for (Actor a : actors) {
                 // Track down actors without ids.
                 if (a.getId() == 0)
                     System.err.println("DEBUG: " + a + " actor without ID set");
 
                 a.update();
             }
         }
     }
 
     private int id; // unique ID for each Actor
     protected int modelId;
     protected transient Model model; // CL - Used to store the model reference
     // after we look it up once
     protected Vector3 position, velocity;
     protected float scale;
 
     // Rotation
     protected Quaternion rotation, angularVelocity;
 
     protected int age; // Actor age in frames
     protected int parentId;
 
     public Actor() {
         id = generateId();
         rotation = new Quaternion();
         angularVelocity = new Quaternion();
         position = new Vector3();
         velocity = new Vector3();
         modelId = Model.getModelIdFor(this);
     }
 
     public void changeYaw(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation
                 .yawAxis(), degrees));
     }
 
     public void changePitch(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation
                 .pitchAxis(), degrees));
     }
 
     public void changeRoll(float degrees) {
         angularVelocity = angularVelocity.times(new Quaternion(rotation
                 .rollAxis(), degrees));
     }
 
     protected void delete() {
         synchronized(actors) {
             actors.remove(this);
         }      
     }
 
     protected int generateId() {
         return (lastId += gen.nextInt(1000) + 1); // Pseudo random increments
     }
 
     public Vector3 getDirection() {
         // FIXME this often points in a negative direction
         return rotation.rollAxis();
     }
 
     public Model getModel() {
         // CL - If our reference is null, go look it up
         if (model == null)
             model = Model.findById(modelId);
 
         return model;
     }
 
     /**
      * @return the actors current position
      */
     public Vector3 getPosition() {
         return position;
     }
 
     public Quaternion getRotation() {
         return rotation;
     }
 
     /**
      * @return the actors size (for texture scaling and collision detection)
      */
     public float getSize() {
         return scale;
     }
 
     /**
      * 
      * @return the actors current velocity
      */
     public Vector3 getVelocity() {
         return velocity;
     }
 
     /**
      * Synchronize all access to the actors list
      * @return
      */
     public static List<Actor> getActors() {
         return actors;
     }
 
     /**
      * Call back upon collision detection for object to handle collision It
      * could... Bounce off Explode into many smaller objects Just explode
      * 
      * @param other
      *            the object this actor collided with
      */
     abstract public void handleCollision(Actor other);
 
     public void render(GL2 gl) {
         gl.glPushMatrix();
         // Translate the actor to it's position
         gl.glTranslatef(position.x, position.y, position.z);
 
         // Rotate the actor
         gl.glMultMatrixf(getRotation().toGlMatrix(), 0);
         // Scale the Actor
 
         // CL - Render our model.
         getModel().render(gl);
         gl.glPopMatrix();
     }
 
     public void setPosition(Vector3 position) {
         this.position = position;
     }
 
     // Lets you reference chain
     public Actor setSize(float newSize) {
         scale = newSize;
         return this;
     }
 
     public void setVelocity(Vector3 velocity) {
         this.velocity = velocity;
     }
 
     // CL - updates the state of the actor for the next frame
     public void update() {
         position.plusEquals(velocity);
         rotation.normalize();
         dampenAngularVelocity();
         // This should also take into effect our maximum angular velocity --
         // this may be an overridden in subclasses to provide different handling
         rotation = rotation.times(angularVelocity);
     }
 
     private void dampenAngularVelocity() {
         angularVelocity = angularVelocity.dampen(0.01f);
     }
 
     // FIXME this is a linear time search
     public static Actor findById(int id) {
         synchronized(actors) {
             for (Actor a: actors)
                 if (a.getId() == id)
                     return a;
         }
         return null;
     }
 
     public int getId() {
         return id;
     }
 
     public Quaternion getAngularVelocity() {
         return angularVelocity;
     }
 
     /**
      * Update all the actors
      * @param update a list of updates
      * @param ship the players ship or similar actor that should not be updated
      */
     public static void updateFromNetwork(List<Actor> update, Actor ship) {
         synchronized(actors) {
             // This is n^2 - but I don't have a better way to do it
             // Using ListEterators so we only need to make one pass adding and removing elements
             for (ListIterator<Actor> actors_iter = actors.listIterator(); actors_iter.hasNext(); ) {
                 Actor a = actors_iter.next();
                 boolean found = false;
 
                 for (ListIterator<Actor> update_iter = update.listIterator(); update_iter.hasNext(); ) {
                     Actor u = update_iter.next();
 
                     if (a.id != u.id)
                         continue;
 
                     actors_iter.set(u);
                     update_iter.remove();
                     found = true;
                     break;
                 }
                 // Skip the last step if running on the server
                 if (ship == null)
                     continue;
                 
                 // Remove actors that where not present in the update, except for the players ship
                 if (!found && a != ship)
                     actors_iter.remove();
             }
             // Add the remaining
             actors.addAll(update);
         }
 
     }
 
     public static void main(String[] args) {
         List<Actor> update = new java.util.ArrayList<Actor>();
         update.add(new Asteroid());
         update.add(new Asteroid());
         update.add(new Asteroid());      
 
         for (int i = 0; i < 3; i++) {
             updateFromNetwork(update, null);
         }
     }
 
 }
