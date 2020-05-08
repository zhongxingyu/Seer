 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package archer;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Spatial;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.exercise.SimpleFBDExercise;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.objects.AngleMeasurement;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.FixedAngleMeasurement;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Potato;
 import edu.gatech.statics.objects.connectors.Pin2dKnownDirection;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.CompleteFBDTask;
 import edu.gatech.statics.ui.AbstractInterfaceConfiguration;
 import edu.gatech.statics.ui.windows.navigation.Navigation3DWindow;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class ArcherExercise extends SimpleFBDExercise {
 
     @Override
     public void initExercise() {
         setName("Archer");
         setDescription("An olympic archer is holding a bow. Build free body diagrams of the bow, the bowstring, and the two together.");
 
        getDisplayConstants().setDrawScale(1);
     }
 
     @Override
     public AbstractInterfaceConfiguration createInterfaceConfiguration() {
         AbstractInterfaceConfiguration ic = (AbstractInterfaceConfiguration) super.createInterfaceConfiguration();
         ic.setNavigationWindow(new Navigation3DWindow());
         return ic;
     }
 
     @Override
     public void loadExercise() {
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.7f, .8f, .9f, 1.0f));
 
         Schematic schematic = getSchematic();
 
         Body bowString = new Potato("Bow String");
         Body bow = new Potato("Bow");
 
         Point bowTop = new Point("B", "-1.25", "5.5", "0");
         Point stringBack = new Point("A", "-4.5", "0", "0");
         Point bowBottom = new Point("D", "-1.25", "-5.5", "0");
         Point bowFront = new Point("C", "3", "0", "0");
 
         Vector3bd directionUnitTop = new Vector3bd(".5", ".866", "0");
         Vector3bd directionUnitBottom = new Vector3bd(".5", "-.866", "0");
 
         Pin2dKnownDirection connectorTop = new Pin2dKnownDirection(bowTop);
         connectorTop.setName("Pin B");
         connectorTop.setDirection(directionUnitTop);
         connectorTop.attach(bowString, bow);
 
         Pin2dKnownDirection connectorBottom = new Pin2dKnownDirection(bowBottom);
         connectorBottom.setName("Pin D");
         connectorBottom.setDirection(directionUnitBottom);
         connectorBottom.attach(bowString, bow);
 
         Pin2dKnownDirection connectorFront = new Pin2dKnownDirection(bowFront);
         connectorFront.setName("Pin C");
         connectorFront.setDirection(Vector3bd.UNIT_X);
         connectorFront.attachToWorld(bow);
 
         Pin2dKnownDirection connectorBack = new Pin2dKnownDirection(stringBack);
         connectorBack.setName("Pin A");
         connectorBack.setDirection(Vector3bd.UNIT_X.negate());
         connectorBack.attachToWorld(bowString);
 
         AngleMeasurement measureTop = new FixedAngleMeasurement(bowTop, directionUnitTop.negate(), Vector3f.UNIT_X.negate());
         measureTop.setName("Angle XXX");
         AngleMeasurement measureBottom = new FixedAngleMeasurement(bowBottom, directionUnitBottom.negate(), Vector3f.UNIT_X.negate());
         measureBottom.setName("Angle YYY");
 
         schematic.add(measureTop);
         schematic.add(measureBottom);
 
         bowTop.createDefaultSchematicRepresentation();
         stringBack.createDefaultSchematicRepresentation();
         bowBottom.createDefaultSchematicRepresentation();
         bowFront.createDefaultSchematicRepresentation();
 
         measureBottom.createDefaultSchematicRepresentation();
         measureTop.createDefaultSchematicRepresentation();
 
         bow.addObject(bowFront);
         bowString.addObject(stringBack);
 
         schematic.add(bowString);
         schematic.add(bow);
 
         ModelNode modelNode = ModelNode.load("archer/assets/", "archer/assets/archer.dae");
         modelNode.extractLights();
 
         ModelRepresentation rep;
 
         float scale = 1;
         Vector3f modelOffset = new Vector3f(-5.25f, -15.7f, -1.5f);
 
         rep = modelNode.extractElement(bowString, "VisualSceneNode/BowString");
         rep.getRelativeNode().setLocalScale(scale);
         rep.setModelOffset(modelOffset);
         bowString.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(bowString, "VisualSceneNode/BowStringBound");
         rep.getRelativeNode().setLocalScale(scale);
         rep.setModelOffset(modelOffset);
         rep.setCullMode(Spatial.CULL_ALWAYS);
         bowString.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(bow, "VisualSceneNode/Bow");
         rep.getRelativeNode().setLocalScale(scale);
         rep.setModelOffset(modelOffset);
         bow.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
 
         rep = modelNode.getRemainder(schematic.getBackground());
         rep.getRelativeNode().setLocalScale(scale);
         rep.setModelOffset(modelOffset);
         schematic.getBackground().addRepresentation(rep);
 
         addTask(new CompleteFBDTask("FBD bow", new BodySubset(bow)));
         addTask(new CompleteFBDTask("FBD bowString", new BodySubset(bowString)));
         addTask(new CompleteFBDTask("FBD both", new BodySubset(bow, bowString)));
     }
 }
