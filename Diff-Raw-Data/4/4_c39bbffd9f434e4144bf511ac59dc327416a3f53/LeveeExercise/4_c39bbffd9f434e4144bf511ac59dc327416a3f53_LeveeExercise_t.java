 package levee;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.Representation;
 import edu.gatech.statics.RepresentationLayer;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.modes.distributed.DistributedExercise;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.distributed.objects.DistributedForce;
 import edu.gatech.statics.modes.distributed.objects.DistributedForceObject;
 import edu.gatech.statics.modes.distributed.objects.TriangularDistributedForce;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.connectors.Fix2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.SolveConnectorTask;
 import edu.gatech.statics.ui.AbstractInterfaceConfiguration;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.ui.windows.navigation.CameraControl;
 import edu.gatech.statics.ui.windows.navigation.Navigation3DWindow;
 import edu.gatech.statics.ui.windows.navigation.ViewConstraints;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class LeveeExercise extends DistributedExercise {
 
     protected float waterHeight = 12;
 
     @Override
     public AbstractInterfaceConfiguration createInterfaceConfiguration() {
         AbstractInterfaceConfiguration interfaceConfiguration = (AbstractInterfaceConfiguration) super.createInterfaceConfiguration();
         interfaceConfiguration.setNavigationWindow(new Navigation3DWindow());
         ViewConstraints vc = new ViewConstraints();
         vc.setPositionConstraints(-15f, 15f, 1f, 14f);
         vc.setZoomConstraints(0.35f, 0.8f);
         vc.setRotationConstraints(-1, 1);
         interfaceConfiguration.setViewConstraints(vc);
         return interfaceConfiguration;
     }
 
     @Override
     public void initExercise() {
         setName("Levee");
 
         setDescription(
                 "<p>In 2005, hurricane Katrina devastated New Orleans. New Orleans is " +
                 "below sea level, between the Mississippi river and Lake Pontchartrain, " +
                 "and relies on levees for protection. A day after Katrina hit the city, " +
                 "the levee system broke in three places.<br><br></p>" +
 
                 "<p>This shows a levee under strained conditions, where the height of the " +
                 "water is 12 ft. Assume that the ground can resist up to 10,400 lb*ft " +
                 "of moment and that it has unlimited resistance to a horizontal force. " +
                 "Examining a 1 foot cross section, solve for the actual loading of the " +
                 "water on the ground at the base of the levee (represented by the fixed " +
                 "support A). Will the ground be able to support the levee?<br><br></p>" +
 
                 "<p>To calculate the resultant, the distributed force of the water is " +
                 "given by a linear pressure distribution p(h) = d*g*h, where h represents depth. The quantity d*g " +
                 "is 62.4 lb/ft^3, the specific weight of water.</p>");
 
 //        setDescription(
 //                "<p>In 2005, hurricane Katrina hit New Orleans, which caused enormous loss. " +
 //                "New Oleans sits between the Mississippi river and Lake Pontchartrain. " +
 //                "It is below water level, and needs dikes or levees for protection. " +
 //                "A day after Katrina hit the city, the levee system broke in three canals.</p>" +
 //                "<p>This shows a cross section of a levee. " +
 //                "The water height is " + waterHeight + " ft, and the cross section width is 1 ft. " +
 //                "Assume that the ground can resist up to 10,400 lb*ft of moment and " +
 //                "an infinite horizontal force (i.e. the ground has no maximum resistance to a horizontal force). " +
 //                "The pressure distribution is linear with respect to depth and the equation is p(h) = d*g*h, where</p>" +
 //                "<b>*</b> d*g = 62.4 lb/ft^3 (specific weight of water) and<br>" +
 //                "<b>*</b> h is the height of the water (water level) below the water surface." +
 //                "<p>How much moment should the ground be able to safely exert on the levee " +
 //                "if the water level increases to its maximum 12ft height?</p>");
 
         Unit.setSuffix(Unit.distance, " ft");
         Unit.setSuffix(Unit.moment, " kip*ft");
         Unit.setSuffix(Unit.force, " kip");
         Unit.setSuffix(Unit.forceOverDistance, " kip/ft");
 
         getDisplayConstants().setMomentSize(0.5f);
         getDisplayConstants().setForceSize(0.5f);
         getDisplayConstants().setPointSize(0.75f);
         getDisplayConstants().setCylinderRadius(0.5f);
         //getDisplayConstants().setForceLabelDistance(40f);
         //getDisplayConstants().setMomentLabelDistance(15f);
         getDisplayConstants().setMeasurementBarSize(0.2f);
     }
 
     @Override
     public void loadExercise() {
 
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.7f, .7f, .9f, 1.0f));
         //StaticsApplication.getApp().getCamera().setLocation(new Vector3f(10.0f, 6, 0f));
 //        StaticsApplication.getApp().getCamera().setDirection(new Vector3f(15, 1, 1).normalize());
 
         Schematic schematic = getSchematic();
 
         String waterLevel = "" + waterHeight;
 
         Point A = new Point("A", "0", "0", "0");
         Point B = new Point("B", "0", waterLevel, "0");
 
         Beam levee = new Beam("Levee", A, B);
 
         BigDecimal waterDensity = new BigDecimal("62.4");
         BigDecimal peakAmount = new BigDecimal(waterLevel).multiply(waterDensity);
 
         DistributedForce waterForce = new TriangularDistributedForce("water", levee, A, B,
                 new Vector(Unit.forceOverDistance, Vector3bd.UNIT_X, peakAmount));
         DistributedForceObject waterForceObject = new DistributedForceObject(waterForce, "");
 
         levee.addObject(waterForceObject);
 
         A.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
 
         int numberArrows = (int) (15 * waterHeight / 12);
         waterForceObject.createDefaultSchematicRepresentation(5, numberArrows, 2f);
 
         // remove the label on the water force
         Representation labelRep = waterForceObject.getRepresentation(RepresentationLayer.labels).get(0);
         waterForceObject.removeRepresentation(labelRep);
 
         DistanceMeasurement measure = new DistanceMeasurement(A, B);
         measure.createDefaultSchematicRepresentation();
         schematic.add(measure);
 
         Connector base = new Fix2d(A);
         base.attachToWorld(levee);
         base.setName("fix A");
 
         schematic.add(levee);
 
         ModelNode modelNode = ModelNode.load("levee/assets/", "levee/assets/levee.dae");
         modelNode.extractLights();
 
         Vector3f modelTranslation = new Vector3f(0f, 0, 0);
 
         ModelRepresentation rep = modelNode.extractElement(levee, "VisualSceneNode/half_wall");
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelOffset(modelTranslation);
         levee.addRepresentation(rep);
 
         // don't do anything with this, just extract the element so it is outside of the background.
         rep = modelNode.extractElement(levee, "VisualSceneNode/arrows");
 
         float waterScale = waterHeight / 11;
 
         rep = modelNode.extractElement(levee, "VisualSceneNode/scene/cut_away_water");
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelOffset(modelTranslation);
         rep.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createAlphaState());
         rep.setModelBound(null);
         rep.setModelScale(1, waterScale, 1);
         waterForceObject.addRepresentation(rep);
 
         rep = modelNode.extractElement(levee, "VisualSceneNode/scene/cut_away_water2");
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelOffset(modelTranslation);
         rep.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createAlphaState());
         rep.setModelBound(null);
         rep.setModelScale(1, waterScale, 1);
         schematic.getBackground().addRepresentation(rep);
 
 
         rep = modelNode.extractElement(levee, "VisualSceneNode/scene/half_water");
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelOffset(modelTranslation);
         rep.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createAlphaState());
         rep.setModelBound(null);
         rep.setModelScale(1, waterScale, 1);
         schematic.getBackground().addRepresentation(rep);
 
         rep = modelNode.getRemainder(schematic.getBackground());
         rep.setModelOffset(modelTranslation);
         schematic.getBackground().addRepresentation(rep);
 
         addTask(new SolveConnectorTask("Solve Base", base));
     }
 
     @Override
     public void postLoadExercise() {
         super.postLoadExercise();
         StaticsApplication.getApp().getCamera().setDirection(new Vector3f(15, 1, 1).normalize());
         CameraControl cameraControl = InterfaceRoot.getInstance().getCameraControl();
         cameraControl.getViewDiagramState().getCameraCenter().addLocal(25, 55, 78);
         StaticsApplication.getApp().selectDiagramKey(null);
     }
 
 }
