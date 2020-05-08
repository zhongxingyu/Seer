 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gamestate;
 
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 
 /**
  *
  * @author dagen
  */
 public class Platform {
        // Scene Object
     private Spatial spatial;
     private Node node;
     
     // Physics connection
     private RigidBodyControl rigidBodyControl;
     
     
     public Platform(SceneObjectDataSource dataSource){
         node = new Node();
         spatial = dataSource.getSceneObject();
         rigidBodyControl = new RigidBodyControl(0.0f);
         System.out.println(spatial);
         spatial.addControl(rigidBodyControl);
         node.attachChild(spatial);
         
 //        this.node = node;
 //        this.spatial = spatial;
 //        this.rigidBodyControl = rigidBodyControl;
     }
     
     public void addToNode(Node node){
         node.attachChild(this.spatial);
     }
     
     public void addToPhysicsSpace(PhysicsSpace physicsSpace){
         physicsSpace.add(this.rigidBodyControl);
     }
 
     public Spatial getSpatial() {
         return spatial;
     }
 
     public void setSpatial(Spatial spatial) {
         this.spatial = spatial;
     }
 
     public Node getNode() {
         return node;
     }
 
     public void setNode(Node node) {
         this.node = node;
     }
 
     public RigidBodyControl getRigidBodyControl() {
         return rigidBodyControl;
     }
 
     public void setRigidBodyControl(RigidBodyControl rigidBodyControl) {
         this.rigidBodyControl = rigidBodyControl;
     }
     
     
 }
