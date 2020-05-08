 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package crcroof;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.modes.description.layouts.ScrollbarLayout;
 import edu.gatech.statics.modes.frame.FrameExercise;
 import edu.gatech.statics.objects.AngleMeasurement;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.FixedAngleMeasurement;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.bodies.Cable;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Fix2d;
 import edu.gatech.statics.objects.connectors.Pin2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.Solve2FMTask;
 import java.math.BigDecimal;
 import java.util.Collections;
 
 /**
  *
  * @author vignesh
  */
 public class CRCRoof extends FrameExercise {
 
     @Override
     public Description getDescription() {
 
         Description description = new Description();
 
         description.setNarrative("The roof of the entrance into the Campus Recreation Center (CRC) "
                 + "at Georgia Tech is comprised of several beams and cables.");
 
         description.setProblemStatement("In this exercise, you will analyze a simplified structure of the "
                 + "CRC entrance roof.  The tension in the cable connecting to point D is 2000 N. The angle "
                 + "between this cable and the vertical member is 30°.  The angle between beam ABC and the cable "
                 + "CE is 30°.  The weight of beam ABC is 1200 N and the weight of the DBE is 2000 N. The necessary "
                 + "dimensions are provided in the schematic.");
 
         description.setGoals("Solve for the tension in cable CE.");
 
         description.setLayout(new ScrollbarLayout());
 
        description.addImage("CRCRoof/assets/CRC1.png");
        description.addImage("CRCRoof/assets/CRC2.png");
 
         return description;
     }
 
         public void initExercise() {
 
         Unit.setSuffix(Unit.distance, " m");
         Unit.setSuffix(Unit.moment, " N*m");
         Unit.setSuffix(Unit.force, " N");
 
     }
 
     @Override
     public void loadExercise() {
 
         Schematic schematic = getSchematic();
 
         Point A = new Point("A", "0", "0", "0");
         Point B = new Point("B", "0", "4.5", "0");
         Point C = new Point("C", "0", "6", "0");
         Point D = new Point("D", "-2", "4", "0");
         Point E = new Point("E", "6", "5.47506802", "0");
         Point F = new Point("F", "0", "0.535898385", "0");
         Point G = new Point("G", "0", "3", "0");
         Point H = new Point("H", "2", "4.8", "0");
 
         A.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         C.createDefaultSchematicRepresentation();
         D.createDefaultSchematicRepresentation();
         E.createDefaultSchematicRepresentation();
         F.createDefaultSchematicRepresentation();
         G.createDefaultSchematicRepresentation();
         H.createDefaultSchematicRepresentation();
 
         schematic.add(A);
         schematic.add(B);
         schematic.add(C);
         schematic.add(D);
         schematic.add(E);
         schematic.add(F);
         schematic.add(G);
         schematic.add(H);
 
         Vector3bd cableAngleF = new Vector3bd(
                 new BigDecimal(Math.cos(Math.PI / 6)),
                 new BigDecimal(Math.sin(Math.PI / 3)),
                 BigDecimal.ZERO);
 
         AngleMeasurement measureAngleF = new FixedAngleMeasurement(A, Vector3bd.UNIT_X, cableAngleF.toVector3f());
         measureAngleF.setName("Angle Body");
         measureAngleF.createDefaultSchematicRepresentation();
         schematic.add(measureAngleF);
 
         DistanceMeasurement measureBC = new DistanceMeasurement(B, C);
         measureBC.createDefaultSchematicRepresentation();
         schematic.add(measureBC);
 
         DistanceMeasurement measureAB = new DistanceMeasurement(A, B);
         measureAB.createDefaultSchematicRepresentation();
         schematic.add(measureAB);
 
         DistanceMeasurement measureDC = new DistanceMeasurement(D, C);
         measureDC.createDefaultSchematicRepresentation();
         schematic.add(measureDC);
         measureDC.forceHorizontal();
         measureDC.forceVertical();
 
         DistanceMeasurement measureAD = new DistanceMeasurement(A, D);
         measureAD.createDefaultSchematicRepresentation();
         schematic.add(measureAD);
         measureAD.forceVertical();
 
         DistanceMeasurement measureBE = new DistanceMeasurement(B, E);
         measureBE.createDefaultSchematicRepresentation();
         schematic.add(measureBE);
         measureBE.forceHorizontal();
 
         DistanceMeasurement measureAH = new DistanceMeasurement(A, H);
         measureAH.createDefaultSchematicRepresentation();
         schematic.add(measureAH);
         measureAH.forceHorizontal();
 
         DistanceMeasurement measureBH = new DistanceMeasurement(B, H);
         measureBH.createDefaultSchematicRepresentation();
         schematic.add(measureAH);
         measureBH.forceHorizontal();
 
         Force forceG = new Force(G, Vector3bd.UNIT_Y.negate(), new BigDecimal("1200"));
         forceG.setName("Force G");
         forceG.createDefaultSchematicRepresentation();
         schematic.add(forceG);
 
         Force forceH = new Force(H, Vector3bd.UNIT_Y.negate(), new BigDecimal("2000"));
         forceH.setName("Force H");
         forceH.createDefaultSchematicRepresentation();
         schematic.add(forceH);
 
 
         Beam DE = new Beam("DE", D, E);
         Beam AC = new Beam("AC", A, C);
 
         Cable CE = new Cable("CE", C, E);
         Cable DF = new Cable("DA", D, F);
 
         DE.addObject(H);
         DE.addObject(forceH);
         AC.addObject(G);
         AC.addObject(forceG);
 
 
         Pin2d pinB = new Pin2d(B);
 
         Fix2d fixedA = new Fix2d(A);
 
         fixedA.setName("Fixed A");
 
         fixedA.attachToWorld(AC);
 
         Connector2ForceMember2d connectorC = new Connector2ForceMember2d(C, CE);
         Connector2ForceMember2d connectorE = new Connector2ForceMember2d(E, CE);
 
         Connector2ForceMember2d connectorD = new Connector2ForceMember2d(D, DF);
         Connector2ForceMember2d connectorF = new Connector2ForceMember2d(F, DF);
 
         connectorC.attach(CE, AC);
         connectorE.attach(CE, DE);
         
         connectorD.attach(DE, DF);
         connectorF.attach(DF, AC);
 
         // mark the 2fm DF as solved, with force 2000 N
         // the steps that handle this are normally done in application, in the equation diagram setSolved method
         // doing it out of context here is a little ugly.
         Force tensionDF = new Force(D,connectorD.getDirection(),"tension DF");
         tensionDF.getAnchoredVector().setKnown(true);
         tensionDF.getAnchoredVector().setDiagramValue( new BigDecimal(2000));
         connectorD.solveReaction(DF, Collections.singletonList(tensionDF.getVector()));
         getSymbolManager().addSymbol(tensionDF.getAnchoredVector(), connectorD);
 
         pinB.setName("Pin B");
 
         pinB.attach(AC, DE);
 
         schematic.add(DE);
         schematic.add(AC);
         schematic.add(CE);
         schematic.add(DF);
 
 
         ModelNode modelNode = ModelNode.load("crcroof/assets/", "crcroof/assets/crcRoof.dae");
         modelNode.extractLights();
 
 
         ModelRepresentation rep;
         String prefix = "VisualSceneNode/totalScene/";
 
 
         Matrix3f rotation = new Matrix3f();
         rotation.fromAngleAxis((float) -Math.PI / 2, Vector3f.UNIT_Y);
         float scale = .44444f;
 
 
         rep = modelNode.extractElement(CE, prefix + "CE");//pCylinder11
         CE.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.getRelativeNode().setLocalRotation(rotation);
         rep.getRelativeNode().setLocalScale(scale);
 
         rep = modelNode.extractElement(DF, prefix + "DF");
         DF.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.getRelativeNode().setLocalRotation(rotation);
         rep.getRelativeNode().setLocalScale(scale);
 
         rep = modelNode.extractElement(AC, prefix + "column");
         AC.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.getRelativeNode().setLocalRotation(rotation);
         rep.getRelativeNode().setLocalScale(scale);
 
         rep = modelNode.extractElement(DE, prefix + "totalScene_roof");
         DE.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.getRelativeNode().setLocalRotation(rotation);
         rep.getRelativeNode().setLocalScale(scale);
 
         rep = modelNode.getRemainder(schematic.getBackground());
         rep.getRelativeNode().setLocalRotation(rotation);
         rep.getRelativeNode().setLocalScale(scale);
         schematic.getBackground().addRepresentation(rep);
 
         addTask(new Solve2FMTask("Solve tension in cable CE - Point C", CE, connectorC));
         
 
     }
 }
