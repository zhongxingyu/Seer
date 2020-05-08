 package ussr.physics.jme.pickers;
 
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.physics.jme.JMESimulation;
 import com.jme.input.InputHandler;
 import com.jme.scene.Node;
 import com.jmex.physics.DynamicPhysicsNode;
 import com.jmex.physics.PhysicsSpace;
 import com.jme.input.action.InputAction;
 import com.jme.input.action.InputActionEvent;
 import com.jme.intersection.PickData;
 import com.jme.intersection.TrianglePickResults;
 import com.jme.math.Ray;
 import com.jme.math.Vector2f;
 import com.jme.scene.Spatial;
 import com.jme.system.DisplaySystem;
 
 /**
  * A small tool to be able to pick the visual of physics nodes and move them around with the mouse.
  *
  * @author Irrisor (original code)
  * @author modified by ups to be USSR-specific
  * @author Konstantinas (modified for builder). In particular added method called pickTarget(Spatial target),
  * for getting geometry of picked objects. 
  */
 public abstract class CustomizedPicker implements Picker {
 
     private JMESimulation simulation;
     /**
      * root node of the scene - used for picking.
      */
     private Node rootNode;
     private CustomizedPicker.PickAction pickAction;
     private CustomizedPicker.MoveAction moveAction;
 
     /**
      * Attach the picker to a simulation
      *
      * @param input        where {@link #getInputHandler()} is attached to
      * @param rootNode     root node of the scene - used for picking
      * @param physicsSpace physics space to create joints in (picked nodes must reside in there)
      */
     public void attach( JMESimulation simulation, InputHandler input, Node rootNode, PhysicsSpace physicsSpace ) {
         this.simulation = simulation;
         this.inputHandler = new InputHandler();
         input.addToAttachedHandlers( this.inputHandler );
         this.rootNode = rootNode;
         activateUSSRPicker();
     }
 
     /**
      * @return the input handler for this picker
      */
     public InputHandler getInputHandler() {
         return inputHandler;
     }
 
     /**
      * Method to run when an object is picked
      */
     protected abstract void pickModuleComponent(JMEModuleComponent component);
     
     private InputHandler inputHandler;
 
     private final Vector2f mousePosition = new Vector2f();
 
     private void activateUSSRPicker() {
         pickAction = new PickAction();
         inputHandler.addAction( pickAction, InputHandler.DEVICE_MOUSE, 0, InputHandler.AXIS_NONE, false );
         moveAction = new MoveAction();
         inputHandler.addAction( moveAction, InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_NONE, InputHandler.AXIS_ALL, false );
     }
 
     private void pickNode(DynamicPhysicsNode node) {
         for(JMEModuleComponent component: simulation.getModuleComponents()) {
             if(component.getNodes().contains(node)) {
                 this.pickModuleComponent(component);
                 
                 return;
             }
         }
     }
 
      /**
      * Passes spatial parent geometry of picked visual object
      * @param target, the picked spatial parent geometry
      */
     protected abstract void pickTarget(Spatial target);
 
     public void delete() {
         inputHandler.removeAction( pickAction );
         inputHandler.removeAction(moveAction);
     }
     
     private class PickAction extends InputAction {
         private final Ray pickRay = new Ray();
         private final TrianglePickResults pickResults = new TrianglePickResults();
 
         public void performAction( InputActionEvent evt ) {
             if ( evt.getTriggerPressed() ) {
                 DisplaySystem.getDisplaySystem().getWorldCoordinates( mousePosition, 0, pickRay.origin );
                 DisplaySystem.getDisplaySystem().getWorldCoordinates( mousePosition, 0.3f, pickRay.direction );
                 pickRay.direction.subtractLocal( pickRay.origin ).normalizeLocal();
 
                 pickResults.clear();
                 pickResults.setCheckDistance( true );
                 rootNode.findPick( pickRay, pickResults );
                 /* To avoid using overly large amount of memory on interactive clicking, clear the collision tree */
                 //rootNode.clearCollisionTree();//TODO JME2 uncommented
                 loopResults:
                     for ( int i = 0; i < pickResults.getNumber(); i++ ) {
                         PickData data = pickResults.getPickData( i );
                         if ( data.getTargetTris() != null && data.getTargetTris().size() > 0 ) {
                             Spatial target = data.getTargetMesh().getParent(); //TODO JME2 changed from getParentGeom();
                            //System.out.println("target="+data.getTargetMesh());
                            //pickTarget(target);
                            pickTarget(data.getTargetMesh()); //TODO JME2 changed to fix builder bug 
                             while ( target != null ) {
                                 if ( target instanceof DynamicPhysicsNode ) {
                                     DynamicPhysicsNode picked = (DynamicPhysicsNode) target;
                                     pickNode( picked );
                                     break loopResults;
                                 }
                                 target = target.getParent();
                             }
                         }
                     }
             }
         }
     }
 
     private class MoveAction extends InputAction {
 
         public void performAction( InputActionEvent evt ) {
 
             switch ( evt.getTriggerIndex() ) {
             case 0:
                 mousePosition.x = evt.getTriggerPosition() * DisplaySystem.getDisplaySystem().getWidth();
                 break;
             case 1:
                 mousePosition.y = evt.getTriggerPosition() * DisplaySystem.getDisplaySystem().getHeight();
                 break;
             }
         }
 
     }
 }
