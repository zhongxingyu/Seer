 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package seesaw;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.exercise.SimpleFBDExercise;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.connectors.Pin2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.modes.fbd.ui.FBD3DModePanel;
 import edu.gatech.statics.modes.fbd.ui.FBDModePanel;
 import edu.gatech.statics.objects.UnknownPoint;
 import edu.gatech.statics.tasks.CompleteFBDTask;
 import edu.gatech.statics.ui.AbstractInterfaceConfiguration;
import edu.gatech.statics.modes.equation.ui.Default3DInterfaceConfiguration;
 import edu.gatech.statics.ui.windows.navigation.CameraControl;
import edu.gatech.statics.ui.windows.navigation.Navigation3DWindow;
 import edu.gatech.statics.ui.windows.navigation.ViewConstraints;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class SeeSawExercise extends SimpleFBDExercise {//OrdinaryExercise {
 
     @Override
     public Description getDescription() {
         Description description = new Description();
 
         description.setTitle("See Saw");
         description.setNarrative(
                 "Sam is at the park with his nephew and niece, Jenny and Bobby, " +
                 "who are balancing on a see-saw.  He is taking a statics class at " +
                 "Georgia Tech and has recently learned about free body diagrams.  " +
                 "Can you help him make a FBD of the see saw?");
 
         description.setProblemStatement(
                 "The see saw is supported with a pin at point B.  " +
                 "Jenny and Bobby are located at points A and C respectively.");
         
         description.setGoals("Build a free body diagram of the see saw.");
 
         description.addImage("seesaw/assets/seesaw-0.png");
         description.addImage("seesaw/assets/seesaw-1.jpg");
         description.addImage("seesaw/assets/seesaw-2.jpg");
         description.addImage("seesaw/assets/seesaw-3.jpg");
 
         return description;
     }
 
     @Override
     public void initExercise() {
 //        setName("See Saw");
 //        setDescription("Two children are on a see saw. Build a free body diagram of the see saw.");
 
         Unit.setPrecision(Unit.distance, 2);
     }
 
     @Override
     public AbstractInterfaceConfiguration createInterfaceConfiguration() {
         //AbstractInterfaceConfiguration ic = (AbstractInterfaceConfiguration) super.createInterfaceConfiguration();
         // ideally, we shouldn't need to create a subclass here.
         // we should just be able to modify the camera control directly, but so it goes.
         AbstractInterfaceConfiguration ic = new Default3DInterfaceConfiguration() {
 
             @Override
             public void setupCameraControl(CameraControl cameraControl) {
                 super.setupCameraControl(cameraControl);
                 cameraControl.setMovementSpeed(.2f, .02f, .05f);
             }
         };
         ViewConstraints vc = new ViewConstraints();
         vc.setPositionConstraints(-1, 1, 0.5f, 2);
         vc.setZoomConstraints(0.5f, 3f);
         ic.setViewConstraints(vc);
         //***Remove after testing pointiness of moment arrows
         ic.replaceModePanel(FBDModePanel.class, new FBD3DModePanel());
         // this throws a null pointer
         //ic.getNavigationWindow().getCameraControl().setMovementSpeed(.2f, .02f, .05f);
         return ic;
     }
 
     @Override
     public void loadExercise() {
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.6f, .6f, .6f, 1.0f));
         StaticsApplication.getApp().getCamera().setLocation(new Vector3f(0.0f, 0.0f, 0.8f));
         Schematic schematic = getSchematic();
 
         Point end1 = new Point("end1", "-2.5", "0", "0");
         Point base = new Point("B", "0", "0", "0");
         Point end2 = new Point("end2", "2.5", "0", "0");
 
         UnknownPoint child1Point = new UnknownPoint(new Point("A", "-2.0", "0", "0"), base, Vector3bd.UNIT_X.negate());
         Point child2Point = new Point("C", "1.75", "0", "0");
 
         Force child1Force = new Force(child1Point, Vector3bd.UNIT_Y.negate(), new BigDecimal(15 * 9.8f));
         Force child2Force = new Force(child2Point, Vector3bd.UNIT_Y.negate(), "Bobby");
         child1Force.setName("Jenny");
         //this set is needed even though the name should be set in the constructor
         child2Force.setName("Bobby");
 
         DistanceMeasurement measure1 = new DistanceMeasurement(child1Point, base);
         DistanceMeasurement measure2 = new DistanceMeasurement(base, child2Point);
 
         measure1.setKnown(false);
         measure1.setSymbol("X");
 
         measure1.setName("measure AB");
         measure2.setName("measure BC");
 
         schematic.add(measure1);
         schematic.add(measure2);
 
         child1Point.setMeasurement(measure1);
 
         Body seesaw = new Beam("See Saw", end1, end2);
 
         seesaw.addObject(base);
         seesaw.addObject(child1Force);
         seesaw.addObject(child2Force);
         seesaw.addObject(child1Point);
         seesaw.addObject(child2Point);
 
         Connector pin = new Pin2d(base);
         pin.setName("pin B");
         pin.attachToWorld(seesaw);
 
         //end1.createDefaultSchematicRepresentation();
         base.createDefaultSchematicRepresentation();
         //end2.createDefaultSchematicRepresentation();
         child1Force.createDefaultSchematicRepresentation();
         child1Point.createDefaultSchematicRepresentation();
         child2Force.createDefaultSchematicRepresentation();
         child2Point.createDefaultSchematicRepresentation();
         //seesaw.createDefaultSchematicRepresentation();
         measure1.createDefaultSchematicRepresentation();
         measure2.createDefaultSchematicRepresentation();
 
         schematic.add(seesaw);
 
         Vector3f modelOffset = new Vector3f(0, -1.25f, 0);
         Matrix3f modelRotation = new Matrix3f();
         modelRotation.fromAngleAxis((float) Math.PI / 2, Vector3f.UNIT_Y);
 
         ModelRepresentation rep;
         ModelNode modelNode = ModelNode.load("seesaw/assets", "seesaw/assets/seesaw.dae");
 
         modelNode.extractLights();
 
         rep = modelNode.extractElement(seesaw, "VisualSceneNode/board");
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelOffset(modelOffset);
         rep.setModelRotation(modelRotation);
         seesaw.addRepresentation(rep);
 
         rep = modelNode.getRemainder(schematic.getBackground());
         rep.setModelOffset(modelOffset);
         modelRotation.fromAngleAxis((float) Math.PI / 2.1f, Vector3f.UNIT_Y);
         rep.setModelRotation(modelRotation);
         schematic.getBackground().addRepresentation(rep);
 
         addTask(new CompleteFBDTask("FBD seesaw", new BodySubset(seesaw)));
     }
 }
