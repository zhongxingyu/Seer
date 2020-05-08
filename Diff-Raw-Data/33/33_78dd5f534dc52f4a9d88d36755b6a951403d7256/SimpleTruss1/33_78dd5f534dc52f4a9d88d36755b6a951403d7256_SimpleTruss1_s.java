 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package simpletruss;
 
 import com.jme.math.Vector3f;
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.modes.description.layouts.ScrollbarLayout;
 import edu.gatech.statics.modes.truss.TrussExercise;
 import edu.gatech.statics.modes.truss.zfm.PotentialZFM;
 import edu.gatech.statics.modes.truss.zfm.ZeroForceMember;
 import edu.gatech.statics.objects.AngleMeasurement;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.FixedAngleMeasurement;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Measurement;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.PointAngleMeasurement;
 import edu.gatech.statics.objects.bodies.Background;
 import edu.gatech.statics.objects.bodies.Bar;
 import edu.gatech.statics.objects.bodies.PointBody;
 import edu.gatech.statics.objects.bodies.TwoForceMember;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Pin2d;
 import edu.gatech.statics.objects.connectors.Roller2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.Solve2FMTask;
 import edu.gatech.statics.tasks.SolveFBDTask;
 import edu.gatech.statics.tasks.SolveZFMTask;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class SimpleTruss1 extends TrussExercise {
 
     @Override
     public Description getDescription() {
         Description description = new Description();
 
         description.setTitle("Gambrel roof truss");
         description.setNarrative("Jodi is a civil engineering student at Georgia Tech and will be spending the summer in Wyoming "
                 + "visiting her older brother Nick, who is a Georgia Tech graduate with a degree in mechanical "
                 + "engineering.  Whenever she goes and visits with him, he always comes up with a project for her "
                 + "that involves engineering.  He owns over 50 acres of land and has a barn located on his"
                 + "property that is almost 100 years old.  Several of the beams in the structure have some rotting "
                 + "and will need to be replaced, but Nick is unsure of what size and kind of wood should be "
                 + "purchased to replace the beams and how large the screws need to be to ensure the integrity of "
                 + "the roof.  He has come up with a project for her this summer that involves renovating the roof "
                 + "structure of the barn on his property.  He has given her the task of drawing up the truss "
                 + "structure, which happens to be a Gambrel roof truss, and solve for the loads on each members "
                 + "of the truss.");
 
         description.setProblemStatement("The roof can be drawn as a Gambrel roof truss and can be examined from a 2D perspective.  "
                 + "The truss has symmetric loading with vertical loads of 3kN at joints A and H and 6kN at points B, "
                 + "D, and F.  It is mandatory to first identify the zero force members in the truss and to create a "
                 + "FBD of the whole truss to solve for the supports before you can solve for the member forces.  "
                + "Then continue to solve for the unknowns by using either method of sections (which is recommended) or the method of joints."
                + "");
 
         description.setGoals("Identify the zero force members in the entire structure.  Create a FBD of the whole truss and "
                 + "solve for the support forces.  Then solve for the following forces BD, BE, and, CE by the method"
                 + "of sections or joints.");
 
 
 
         description.setLayout(new ScrollbarLayout());
 
         description.addImage("simpletruss/assets/mainpicture.png");
         description.addImage("simpletruss/assets/screenshot.png");
 
         return description;
     }
 
     @Override
     public void initExercise() {
 //        setName("Simple Truss 1");
 //
 //        setDescription(
 //                "Solve for the tension and compression in each member.");
 
         Unit.setSuffix(Unit.distance, " m");
         Unit.setSuffix(Unit.force, " kN");
         Unit.setSuffix(Unit.moment, " kN*m");
         //Unit.setDisplayScale(Unit.distance, new BigDecimal("10"));
         getDisplayConstants().setMomentSize(0.5f);
         getDisplayConstants().setForceSize(0.5f);
         getDisplayConstants().setPointSize(0.5f);
         getDisplayConstants().setCylinderRadius(0.5f);
         getDisplayConstants().setMomentLabelDistance(0f);
         getDisplayConstants().setMeasurementBarSize(0.1f);
     }
 
     @Override
     public void loadExercise() {
 
         // create points
         Point A, B, C, D, E, F, G, H;
         A = new Point("A", "[0,0,0]");
         B = new Point("B", "[4,3,0]");
         C = new Point("C", "[4,0,0]");
         D = new Point("D", "[8,4.2,0]");
         E = new Point("E", "[8,0,0]");
         F = new Point("F", "[12,3,0]");
         G = new Point("G", "[12,0,0]");
         H = new Point("H", "[16,0,0]");
 
         List<Point> points = Arrays.asList(
                 new Point[]{A, B, C, D, E, F, G, H});
 
         // create point bodies
         PointBody Ab, Bb, Cb, Db, Eb, Fb, Gb, Hb;
         Ab = new PointBody("Joint A", A);
         Bb = new PointBody("Joint B", B);
         Cb = new PointBody("Joint C", C);
         Db = new PointBody("Joint D", D);
         Eb = new PointBody("Joint E", E);
         Fb = new PointBody("Joint F", F);
         Gb = new PointBody("Joint G", G);
         Hb = new PointBody("Joint H", H);
 
         List<PointBody> pointBodies = Arrays.asList(
                 new PointBody[]{Ab, Bb, Cb, Db, Eb, Fb, Gb, Hb});
         //new PointBody[]{Ab, Bb, Db, Eb, Fb, Hb});
         Map<Point, PointBody> pointBodyMap = new HashMap();
         pointBodyMap.put(A, Ab);
         pointBodyMap.put(B, Bb);
         pointBodyMap.put(C, Cb);
         pointBodyMap.put(D, Db);
         pointBodyMap.put(E, Eb);
         pointBodyMap.put(F, Fb);
         pointBodyMap.put(G, Gb);
         pointBodyMap.put(H, Hb);
 
         // create members
         TwoForceMember AB, AC, BC, BD, BE, CE, DE, DF, EF, EG, FG, FH, GH;
         //TwoForceMember AE, EH;
         AB = new Bar("AB", A, B);
         BC = new ZeroForceMember("BC", B, C);
         BD = new Bar("BD", B, D);
         BE = new Bar("BE", B, E);
         DE = new Bar("DE", D, E);
         DF = new Bar("DF", D, F);
         EF = new Bar("EF", E, F);
         FG = new ZeroForceMember("FG", F, G);
         FH = new Bar("FH", F, H);
         //AE = new Bar("AE", A, E);
         //EH = new Bar("EH", E, H);
 
         // these ones are redundant, they are covered by AE and EH,
         // but their representations are used by thep potential ZFMs.
         // these are not added to the bars list.
         AC = new Bar("AC", A, C);
         CE = new Bar("CE", C, E);
         EG = new Bar("EG", E, G);
         GH = new Bar("GH", G, H);
 
         List<TwoForceMember> bars = Arrays.asList(
                 new TwoForceMember[]{AB, BC, BD, BE, DE, DF, EF, FG, FH, AC, CE, EG, GH});
 
         // connect the bars of the truss to the joints.
         for (TwoForceMember bar : bars) {
 
             if (bar instanceof ZeroForceMember) {
                 continue;
             }
 
             Point p1 = bar.getEnd1();
             Point p2 = bar.getEnd2();
             Connector2ForceMember2d c1 = new Connector2ForceMember2d(p1, bar);
             Connector2ForceMember2d c2 = new Connector2ForceMember2d(p2, bar);
             c1.attach(bar, pointBodyMap.get(p1));
             c2.attach(bar, pointBodyMap.get(p2));
         }
 
         // add the given forces
         Force forceA = new Force(A, Vector3bd.UNIT_Y.negate(), new BigDecimal(3));
         Force forceB = new Force(B, Vector3bd.UNIT_Y.negate(), new BigDecimal(6));
         Force forceD = new Force(D, Vector3bd.UNIT_Y.negate(), new BigDecimal(6));
         Force forceF = new Force(F, Vector3bd.UNIT_Y.negate(), new BigDecimal(6));
         Force forceH = new Force(H, Vector3bd.UNIT_Y.negate(), new BigDecimal(3));
         forceA.setName("loading A");
         forceB.setName("loading B");
         forceD.setName("loading D");
         forceF.setName("loading F");
         forceH.setName("loading H");
         Ab.addObject(forceA);
         Bb.addObject(forceB);
         Db.addObject(forceD);
         Fb.addObject(forceF);
         Hb.addObject(forceH);
         forceA.createDefaultSchematicRepresentation();
         forceB.createDefaultSchematicRepresentation();
         forceD.createDefaultSchematicRepresentation();
         forceF.createDefaultSchematicRepresentation();
         forceH.createDefaultSchematicRepresentation();
 
         // connect the truss to the world
        Roller2d rollerA = new Roller2d(A);
        rollerA.setName("roller A");
        rollerA.setDirection(Vector3bd.UNIT_Y);
        rollerA.attachToWorld(Ab);
        rollerA.createDefaultSchematicRepresentation();

        Pin2d pinH = new Pin2d(H);
        pinH.setName("pin H");
        pinH.attachToWorld(Hb);
        pinH.createDefaultSchematicRepresentation();
 
         for (TwoForceMember bar : bars) {
             //bar.createDefaultSchematicRepresentation();
             getSchematic().add(bar);
         }
 
         AngleMeasurement angleBAC, angleDBX, angleXBE, angleFHG, angleDFX, angleXFE;
         angleBAC = new PointAngleMeasurement(A, B, C);
         angleDBX = new FixedAngleMeasurement(B, D, Vector3f.UNIT_X);
         angleXBE = new FixedAngleMeasurement(B, E, Vector3f.UNIT_X);
         angleFHG = new PointAngleMeasurement(H, F, G);
         angleDFX = new FixedAngleMeasurement(F, D, Vector3f.UNIT_X.negate());
         angleXFE = new FixedAngleMeasurement(F, E, Vector3f.UNIT_X.negate());
         angleBAC.setName("angle bac");
         angleDBX.setName("angle dbx");
         angleXBE.setName("angle xbe");
         angleFHG.setName("angle fhg");
         angleDFX.setName("angle dfx");
         angleXFE.setName("angle xfe");
 
         DistanceMeasurement distanceAC, distanceCE, distanceEG, distanceGH;
         distanceAC = new DistanceMeasurement(A, C);
         distanceCE = new DistanceMeasurement(C, E);
         distanceEG = new DistanceMeasurement(E, G);
         distanceGH = new DistanceMeasurement(G, H);
         distanceAC.setName("distance ac");
         distanceCE.setName("distance ce");
         distanceEG.setName("distance eg");
         distanceGH.setName("distance gh");
 
         //AE.addObject(distanceAC);
         //AE.addObject(distanceCE);
         //EH.addObject(distanceEG);
         //EH.addObject(distanceGH);
 
         List<Measurement> measures = Arrays.asList(new Measurement[]{
                     angleBAC, angleDBX, angleXBE, angleFHG, angleDFX, angleXFE,
                     distanceAC, distanceCE, distanceEG, distanceGH});
         for (Measurement measurement : measures) {
             measurement.createDefaultSchematicRepresentation();
             getSchematic().add(measurement);
         }
 
         PotentialZFM pAB, pAC, pBC, pBD, pBE, pCE, pDE, pDF, pEF, pEG, pFG, pFH, pGH;
         pAB = new PotentialZFM(false);
         pAC = new PotentialZFM(false);
         pBC = new PotentialZFM(true);
         pBD = new PotentialZFM(false);
         pBE = new PotentialZFM(false);
         pCE = new PotentialZFM(false);
         pDE = new PotentialZFM(false);
         pDF = new PotentialZFM(false);
         pEF = new PotentialZFM(false);
         pEG = new PotentialZFM(false);
         pFG = new PotentialZFM(true);
         pFH = new PotentialZFM(false);
         pGH = new PotentialZFM(false);
 
         pAB.setBaseName("AB");
         pAC.setBaseName("AC");
         pBC.setBaseName("BC");
         pBD.setBaseName("BD");
         pBE.setBaseName("BE");
         pCE.setBaseName("CE");
         pDE.setBaseName("DE");
         pDF.setBaseName("DF");
         pEF.setBaseName("EF");
         pEG.setBaseName("EG");
         pFG.setBaseName("FG");
         pFH.setBaseName("FH");
         pGH.setBaseName("GH");
 
 
         // ADD REPRESENTATIONS
 
 
         ModelNode modelNode = ModelNode.load("simpletruss/assets/", "simpletruss/assets/roof.dae");
         modelNode.extractLights();
 
         ModelRepresentation rep;
 
         rep = modelNode.extractElement(A, "VisualSceneNode/polySurface28/A");
         A.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(B, "VisualSceneNode/polySurface28/B");
         B.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(C, "VisualSceneNode/polySurface28/C");
         C.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(D, "VisualSceneNode/polySurface28/D");
         D.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(E, "VisualSceneNode/polySurface28/E");
         E.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(F, "VisualSceneNode/polySurface28/F");
         F.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(G, "VisualSceneNode/polySurface28/G");
         G.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(H, "VisualSceneNode/polySurface28/H");
         H.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(AB, "VisualSceneNode/polySurface28/AB");
         AB.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(AC, "VisualSceneNode/polySurface28/AC");
         AC.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(BC, "VisualSceneNode/polySurface28/BC");
         BC.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(BD, "VisualSceneNode/polySurface28/BD");
         BD.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(BE, "VisualSceneNode/polySurface28/BE");
         BE.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(CE, "VisualSceneNode/polySurface28/CE");
         CE.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(DE, "VisualSceneNode/polySurface28/DE");
         DE.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(DF, "VisualSceneNode/polySurface28/DF");
         DF.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(EF, "VisualSceneNode/polySurface28/EF");
         EF.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(EG, "VisualSceneNode/polySurface28/EG");
         EG.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(FG, "VisualSceneNode/polySurface28/FG");
         FG.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(FH, "VisualSceneNode/polySurface28/FH");
         FH.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(GH, "VisualSceneNode/polySurface28/GH");
         GH.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
 
 
         getSchematic().getBackground().addRepresentation(modelNode.getRemainder(getSchematic().getBackground()));
 
 
         // create default schematic representations for the redundant bars.
 //        AC.createDefaultSchematicRepresentation();
 //        CE.createDefaultSchematicRepresentation();
 //        EG.createDefaultSchematicRepresentation();
 //        GH.createDefaultSchematicRepresentation();
         pAB.addRepresentations(AB);
         pAC.addRepresentations(AC);
         pBC.addRepresentations(BC);
         pBD.addRepresentations(BD);
         pBE.addRepresentations(BE);
         pCE.addRepresentations(CE);
         pDE.addRepresentations(DE);
         pDF.addRepresentations(DF);
         pEF.addRepresentations(EF);
         pEG.addRepresentations(EG);
         pFG.addRepresentations(FG);
         pFH.addRepresentations(FH);
         pGH.addRepresentations(GH);
 
         List<PotentialZFM> potentialBars = Arrays.asList(
                 new PotentialZFM[]{pAB, pBC, pBD, pBE, pDE, pDF, pEF, pFG, pFH, pAC, pCE, pEG, pGH});
         for (PotentialZFM potentialZFM : potentialBars) {
             // just use defaults, since there are no models in the simple exercises anyway.
             getSchematic().add(potentialZFM);
         }
 
         for (PointBody pointBody : pointBodies) {
             pointBody.createDefaultSchematicRepresentation();
             getSchematic().add(pointBody);
         }
 
         for (Point point : points) {
             point.createDefaultSchematicRepresentation();
         }
 
 
         addTask(new SolveZFMTask("Solve zfms"));
 
         List<Body> allBodies = new ArrayList<Body>();
         for (Body body : getSchematic().allBodies()) {
             if (body instanceof ZeroForceMember || body instanceof Background) {
                 continue;
             }
             allBodies.add(body);
         }
         BodySubset subset = new BodySubset(allBodies);
         subset.setSpecialName("Whole Truss");
         addTask(new SolveFBDTask("Solve whole truss", subset));
 
 
         addTask(new Solve2FMTask("Solve BD", BD, BD.getConnector1()));
         addTask(new Solve2FMTask("Solve BE", BE, BE.getConnector1()));
         addTask(new Solve2FMTask("Solve CE", CE, CE.getConnector1()));
     }
 }
