 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package script;
 
 import graphics.ThreeDGraphicsManager;
 import graphics.ThreeDModel;
 import physics.ThreeDPhysicsManager;
 import resource.GraphicsResource;
 import resource.Resource;
 import resource.WavefrontModel;
import sun.org.mozilla.javascript.Scriptable;
 import update.UpdateManager;
 import update.Updateable;
 
 /**
  *
  * @author Tanner <thobson125@gmail.com>
  */
 public class ScriptUtil {
     /*
      * @param prefix The prefix used when retrieving the model (e.g. "rocket/")
      * @param path The path used when retrieving the model, without the ".obj" (e.g. "rocket")
      * @param x The x value to insert the model to
      * @param y The y value to insert the model to
      * @param z The z value to insert the model to
      */
     public static void insertModel(Scriptable args) {
         final String prefix = (String)getOrDefault(args, "prefix", null);
         final String path = (String)getOrDefault(args, "path", null);
        final Float x = (Float)getOrDefault(args, "x", new Integer(0));
        final Float y = (Float)getOrDefault(args, "y", new Integer(0));
        final Float z = (Float)getOrDefault(args, "z", new Integer(0));
         
         
         Updateable updateable = new Updateable() {
             @Override
             public boolean update(int delta) {
                 WavefrontModel wfModel;
                 if (prefix == null) {
                     wfModel = new WavefrontModel(path);
                 } else {
                     wfModel = new WavefrontModel(prefix, path);
                 }
                 wfModel.create();
                 waitUntilLoaded(wfModel);
                 
                 ThreeDModel tdModel = new ThreeDModel(wfModel);
                 tdModel.create();
                 waitUntilLoaded(tdModel);
                 
                 // RigidBody rigidBody = new RigidBody(tdModel);
                 // TODO: Implement the RigidBody code here so that I can 
                 //   .setPosition() these objects with the x, y, and z 
                 //   values from above.
                 
                 ThreeDGraphicsManager.getInstance()
                         .add(tdModel);
                 ThreeDPhysicsManager.getInstance()
                         .getCollisionMesh()
                         .addMeshes(wfModel.getObjects());
                 
                 return true; // Tells UpdateManger to remove this object after executing
             }
         };
         
         UpdateManager.getInstance().add(updateable);
     }
     
     private static Object getOrDefault(Scriptable object, String prop, Object defaultValue) {
         if (object.has(prop, object)) {
             return object.get(prop, object);
         } else {
             return defaultValue;
         }
     }
     
     private static void waitUntilLoaded(Resource r) {
         if (r instanceof Resource) {
             while (!r.isLoaded()) {
                 Thread.yield();
             }
         } 
         if (r instanceof GraphicsResource) {
             while (!((GraphicsResource)r).isProcessed()) { 
                 Thread.yield();
             }
         }
     }
 }
