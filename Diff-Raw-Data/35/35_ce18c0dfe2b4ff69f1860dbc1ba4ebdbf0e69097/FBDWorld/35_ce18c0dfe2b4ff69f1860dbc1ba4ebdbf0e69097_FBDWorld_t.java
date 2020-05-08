 /*
  * FBDWorld.java
  *
  * Created on June 12, 2007, 2:01 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics.modes.fbd;
 
 import com.jme.image.Texture;
 import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import com.jme.renderer.TextureRenderer;
 import com.jme.scene.Node;
 import com.jme.system.DisplaySystem;
import com.jmex.bui.BImage;
 import edu.gatech.statics.*;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.application.ui.FBDIcon;
 import edu.gatech.statics.modes.equation.EquationWorld;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Joint;
 import edu.gatech.statics.objects.Measurement;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.Vector;
 import edu.gatech.statics.util.SelectableFilter;
 import edu.gatech.statics.util.SelectionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDWorld extends World {
     
     private EquationWorld equation;
     
     private boolean locked = false;
     boolean isLocked() {return locked;}
     void setLocked() {
         locked = true;
         enableManipulatorsOnSelectDefault(false);
         enableSelectMultipleDefault(false);
         
         StaticsApplication.getApp().setDefaultAdvice(
                 "Congratulations, your FBD is finished! " +
                 "Now you can go to the equation mode and solve for the unknowns.");
         StaticsApplication.getApp().resetAdvice();
     }
     
     private World parentWorld;
     private List<Body> bodies;
     public List<Body> getBodies() {return Collections.unmodifiableList(bodies);}
     
     private List<Vector> externalForces = new ArrayList();
     private List<Vector> externalForcesAdded = new ArrayList();
     private List<SimulationObject> fbdObjects = new ArrayList();
     
     public EquationWorld getEquationWorld() {return equation;}
     public void createEquationWorld() {
         equation = new EquationWorld(this);
     }
     
     /** Creates a new instance of FBDWorld */
     public FBDWorld(World parentWorld, List<Body> bodies) {
         assert bodies != null : "Bodies cannot be null in constructing FBD!";
         assert !bodies.isEmpty() : "Bodies cannot be empty in constructing FBD!";
         
         enableSelectMultiple(false);
         
         this.parentWorld = parentWorld;
         this.bodies = bodies;
         
         for(SimulationObject obj : parentWorld.allObjects())
             add(obj);
         
         for(Body body : bodies) {
             fbdObjects.add(body);
             
             for(SimulationObject obj : body.getAttachedObjects())
                 if(canAddObject(obj))
                     fbdObjects.add(obj);
                 else if(obj instanceof Force || obj instanceof Moment)
                     externalForces.add((Vector) obj);
         }
         
         updateNodes();
         setupIcon();
         
         //icon = new FBDIcon(this);
     }
     
     //private FBDIcon icon;
     //public FBDIcon getIcon() {return icon;}
     private TextureRenderer iconRenderer;
     private Texture iconTexture;
    private BImage image;
    //private boolean iconFinished = false;
     public FBDIcon getIcon() {
        //return new FBDIcon(iconTexture);
        return new FBDIcon(image);
     }
     
     private void setupIcon() {
        
        /*iconTexture = TextureManager.loadTexture(
                getClass().getResource("rsrc/FBD_Interface/cable.png"),
                Texture.MM_LINEAR, Texture.FM_LINEAR);*/
        //iconTexture = new Texture();
        
         iconRenderer = DisplaySystem.getDisplaySystem().createTextureRenderer(128, 128, TextureRenderer.RENDER_TEXTURE_2D);
         iconRenderer.setBackgroundColor(DisplaySystem.getDisplaySystem().getRenderer().getBackgroundColor());
         iconTexture = new Texture();
         if(iconRenderer.isSupported()) {
             iconRenderer.setupTexture(iconTexture);
         }
        image = new BImage(iconTexture, 90, 90, 128, 128);
     }
     private void renderIcon() {
         /*Camera cam = DisplaySystem.getDisplaySystem().getRenderer().createCamera(90, 90);
         cam.setDirection(StaticsApplication.getApp().getCamera().getDirection());
         cam.setUp(StaticsApplication.getApp().getCamera().getUp());
         cam.setLeft(StaticsApplication.getApp().getCamera().getLeft());
         cam.setLocation(StaticsApplication.getApp().getCamera().getLocation());
         tRenderer.setCamera(cam);*/
         
         Node targetNode = getNode(RepresentationLayer.modelBodies);
         
         if(targetNode != null) {
             iconRenderer.setCamera(StaticsApplication.getApp().getCamera());
             iconRenderer.setupTexture(iconTexture);
             iconRenderer.render(targetNode, iconTexture);
            iconTexture = iconTexture.createSimpleClone();
            
            image = new BImage(iconTexture, 90, 90, 128, 128);
         }
     }
 
     public void render(Renderer r) {
         super.render(r);
         renderIcon();
     }
     
     
     public void activate() {
         
         for(SimulationObject obj : parentWorld.allObjects()) {
             
             // set all non-FBD objects to be grayed.
             // forces and moments remain selectable that they may be added to
             // the world if the user selects them
             if(!fbdObjects.contains(obj)) {
                 if(obj instanceof Force || obj instanceof Moment) {
                     // do nothing
                     // handle in selection override
                 } else
                     obj.setSelectable(false);
                 
                 if(!externalForcesAdded.contains(obj))
                     obj.setDisplayGrayed(true);
             }
         }
         
         StaticsApplication.getApp().setDefaultAdvice(
                 "This is the FBD mode. Add forces, or click on existing ones to add them to your diagram." +
                 "Select 'Check' to check your diagram and enable Equation mode.");
         StaticsApplication.getApp().resetAdvice();
         
         if(locked)
             setLocked();
     }
 
     public void select(SimulationObject obj) {
         
         if(locked)
             return;
         
         // notify listeners
         for(SelectionListener listener : getSelectionListeners())
             listener.onSelect(obj);
 
         if(externalForces.contains(obj)) {
             
             // this force or moment was added externally.
             // for now, we toggle its state as grayed.
             
             // however, do nothing if we have selection listeners.
             // this is awkward, however, we do not want 
             if(getSelectionListeners().isEmpty()) {
                 if(!externalForcesAdded.contains(obj)) {
                     obj.setDisplayGrayed( false );
                     externalForcesAdded.add((Vector)obj);
                 } else {
                     obj.setDisplayGrayed( true );
                     externalForcesAdded.remove(obj);
                 }
             }
             
         } else {
             // resume regular default behavior...
             // add to selection, and update display
             getSelectedObjects().add(obj);
             obj.setDisplaySelected(true);
 
             if(enableManipulatorsOnSelect())
                 obj.enableManipulators(true);
         }
     }
     
     
     // whether we can add an object carried over from a body.
     // this may change depending on special cases.
     // ie- we want to keep measurements, points
     protected boolean canAddObject(SimulationObject obj) {
         
         if(obj instanceof Point)
             return true;
         
         if(obj instanceof Measurement)
             return true;
         
         return false;
     }
 
     public void setSelectableFilterDefault() {
         setSelectableFilter(new SelectableFilter() {
             public boolean canSelect(SimulationObject obj) {
                 return  obj instanceof Force ||
                         obj instanceof Moment;
             }
         });
     }
     
     private List<Vector> getAddedForces() {
         List<Vector> addedForces = new ArrayList();
         for(SimulationObject obj : allObjects()) {
             if(!(obj instanceof Force) && !(obj instanceof Moment))
                 continue;
             if(obj.isGiven())
                 continue;
             
             // this force has been added, and is not a given that could have been selected.
             addedForces.add((Vector)obj);
         }
         return addedForces;
     }
     
     boolean checkDiagram() {
         
         // step 1: for vectors that we can click on and add, ie, external added forces,
         // make sure that the user has added all of them.
         if(!externalForcesAdded.containsAll(externalForces)) {
             System.out.println("check: diagram does not contain external forces");
             System.out.println("check: FAILED");
             
             StaticsApplication.getApp().setAdvice(
                     "FBD Check: Your FBD is not yet correct. " +
                     "Your diagram must include external forces applied to the bodies in the diagram.");
             return false;
         }
         
         // step 2: assemble a list of all the forces the user has added.
         List<Vector> addedForces = getAddedForces();
         System.out.println("check: user added forces: "+addedForces);
         
         // make list for weights, as we will need these later.
         List<Force> weights = new ArrayList();
         
         // step 3: Make sure weights exist, and remove them from our addedForces.
         for(Body body : bodies) {
             if(body.getWeight() > 0) {
                 Force weight = new Force(body.getCenterOfMassPoint(), new Vector3f(0,-body.getWeight(),0));
                 weights.add(weight);
                 if(addedForces.contains(weight)) {
                     // still using units here....
                     // this should be changed so the test is independent of magnitude.
                     System.out.println("check: removing weight for "+body);
                     addedForces.remove(weight);
                 } else {
                     // weight does not exist in system.
                     System.out.println("check: diagram does not contain weight for "+body);
                     System.out.println("check: FAILED");
                     
                     StaticsApplication.getApp().setAdvice(
                             "FBD Check: Your FBD is not yet correct. " +
                             "Your diagram must include weights for bodies with mass.");
                     return false;
                 }
             }
         }
         
         // Step 4: go through all the border joints connecting this FBD to the external world,
         // and check each force implied by the joint.
         //List<Pair<Joint, Body>> jointsAndBodies = new ArrayList(); // joints between system and external.
         for(SimulationObject obj : allObjects()) {
             if(!(obj instanceof Joint))
                 continue;
             
             Joint joint = (Joint) obj;
             
             // ^ is java's XOR operator
             // we want the joint IF it connects a body in the body list
             // to a body that is not in the body list. This means xor.
             if(   !(bodies.contains(joint.getBody1()) ^
                     bodies.contains(joint.getBody2())) )
                 continue;
             
             Body body = null;
             if(bodies.contains(joint.getBody1()))
                 body = joint.getBody1();
             if(bodies.contains(joint.getBody2()))
                 body = joint.getBody2();
             
             //jointsAndBodies.add(new Pair(joint, body));
             List<Vector> reactions = joint.getReactions(body);
             
             System.out.println("check: testing joint: "+joint);
             for(Vector reaction : reactions) {
                 
                 if(joint.isForceDirectionNegatable()) {
                     if( !testReaction(reaction, addedForces) &&
                         !testReaction(reaction.negate(), addedForces)) {
                         System.out.println("check: diagram missing reaction force: "+reaction);
                         System.out.println("check: note: reaction is negatable");
                         System.out.println("check: FAILED");
                         
                         StaticsApplication.getApp().setAdvice(
                                 "FBD Check: Your FBD is not yet correct. " +
                                 "Your diagram is missing a reaction force.");
                         return false;
                     }
                 } else {
                     if( !testReaction(reaction, addedForces) ) {
                         System.out.println("check: diagram missing reaction force: "+reaction);
                         System.out.println("check: FAILED");
                         
                         StaticsApplication.getApp().setAdvice(
                                 "FBD Check: Your FBD is not yet correct. " +
                                 "Your diagram is missing a reaction force.");
                         return false;
                     }
                 }
                 
             }
         }
         
         // Step 5: Make sure we've used all the user added forces.
         if(!addedForces.isEmpty()) {
             System.out.println("check: user added more forces than necessary: "+addedForces);
             System.out.println("check: FAILED");
 
             StaticsApplication.getApp().setAdvice(
                     "FBD Check: Your FBD is not yet correct. " +
                     "Your diagram has more forces and/or moments than necessary.");
             return false;
         }
         
         // Step 6: Verify labels
         // verify that all unknowns are symbols
         // these are reaction forces and moments
         // knowns should not be symbols: externals, weights
         // symbols must also not be repeated, unless this is valid somehow? (not yet)
         
         addedForces = getAddedForces();
         List<String> names = new ArrayList();
         
         for(Vector force : addedForces) {
             if(force.isSymbol()) {
                 
                 // should force be a symbol?
                 // the only forces that should not be symbols now are weights.
                 // we probably need some sort of means for identifying this later on...
                 
                 if(weights.contains(force)) {
                     System.out.println("check: force should not be symbol: "+force);
                     System.out.println("check: FAILED");
                     
                     StaticsApplication.getApp().setAdvice(
                             "FBD Check: Your FBD is not yet correct. " +
                             "One of your forces should not be a symbol.");
                     return false;
                 }
                 
                 // check for duplication
                 String name = force.getName();
                 if(names.contains(name)) {
                     System.out.println("check: user duplicated name for force: "+name);
                     System.out.println("check: FAILED");
 
                     StaticsApplication.getApp().setAdvice(
                             "FBD Check: Your FBD is not yet correct. " +
                             "You cannot have duplicate names in the FBD. " +
                             "Please use the Label tool and give each unknown a unique name.");
                     return false;
                 }
                 names.add(name);
                 
             } else {
                 
                 // force is numeric.
                 // the only forces that WOULD be numeric are weights right now.
                 // make sure the value given is equal to the weight of the object.
                 
                 if(weights.contains(force)) {
                     
                     // check each body because lazy
                     boolean checked = false;
                     for(Body body : getBodies()) {
                         if(force.getAnchor() == body.getCenterOfMassPoint()) {
                             checked = true;
                             
                             float weight = body.getWeight();
                             if(force.getMagnitude() != weight) {
                                 System.out.println("check: weight value incorrect: "+force.getMagnitude()+" != "+weight);
                                 System.out.println("check: FAILED");
 
                                 StaticsApplication.getApp().setAdvice(
                                         "FBD Check: Your FBD is not yet correct. " +
                                         "The weight of one of your bodies is not the correct value.");
                                 return false;
                             }
                         }
                     }
                     
                     if(!checked) {
                         System.out.println("check: unknown error, weight winks out of existence: "+force);
                         System.out.println("check: FAILED");
 
                         StaticsApplication.getApp().setAdvice(
                                 "FBD Check: Your FBD is not yet correct. " +
                                 "Unknown Error!");
                         return false;
                     }
                     
                 } else {
                     System.out.println("check: force should not be numeric: "+force);
                     System.out.println("check: FAILED");
 
                     StaticsApplication.getApp().setAdvice(
                             "FBD Check: Your FBD is not yet correct. " +
                             "One of your forces should not be numeric.");
                     return false;
                 }
             }
         }
         
         // Yay, we've passed the test!
         System.out.println("check: PASSED!");
         return true;
     }
 
     private boolean testReaction(Vector reaction, List<Vector> addedForces) {
         
         // if the reaction is given to the user, leave it be.
         if(externalForces.contains(reaction))
             return true;
         
         // test and remove reaction in the addedForces
         if(addedForces.contains(reaction)) {
             addedForces.remove(reaction);
             return true;
         }
         
         return false;
     }
 }
