 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.project.sample;
 
 import net.epsilony.mf.project.RectangleProjectFactory;
 import java.util.Arrays;
 import java.util.Random;
 import net.epsilony.mf.cons_law.PlaneStress;
 import net.epsilony.mf.model.MFRectangleEdge;
 import static net.epsilony.mf.model.MFRectangleEdge.*;
 import net.epsilony.mf.model.load.ConstantSegmentLoad;
 import net.epsilony.mf.process.MFLinearMechanicalProcessor;
 import net.epsilony.mf.process.MFPreprocessorKey;
 import net.epsilony.mf.process.MechanicalPostProcessor;
 import net.epsilony.mf.process.assembler.Assemblers;
 import net.epsilony.mf.project.MFProject;
 import net.epsilony.tb.Factory;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class TensionBar implements Factory<MFProject> {
 
     double tension = 2000;
     double E = 1000;
     double mu = 0.3;
     RectangleProjectFactory rectangleProjectFactory = new RectangleProjectFactory();
 
     @Override
     public MFProject produce() {
         rectangleProjectFactory.setAssemblersGroup(Assemblers.mechanicalLagrangle());
         rectangleProjectFactory.setValueDimension(2);
         applyLoadsOnRectangle();
         genConstitutiveLaw();
         return rectangleProjectFactory.produce();
     }
 
     protected void applyLoadsOnRectangle() {
         ConstantSegmentLoad leftLoad = new ConstantSegmentLoad();
         leftLoad.setLoad(new double[]{0, 0});
         leftLoad.setLoadValidity(new boolean[]{true, false});
         rectangleProjectFactory.setEdgeLoad(LEFT, leftLoad);
 
         ConstantSegmentLoad rightLoad = new ConstantSegmentLoad();
         rightLoad.setLoad(new double[]{tension, 0});
         rectangleProjectFactory.setEdgeLoad(RIGHT, rightLoad);
 
         ConstantSegmentLoad downLoad = new ConstantSegmentLoad();
         downLoad.setLoad(new double[]{0, 0});
         downLoad.setLoadValidity(new boolean[]{false, true});
         rectangleProjectFactory.setEdgeLoad(DOWN, downLoad);
     }
 
     protected void genConstitutiveLaw() {
         rectangleProjectFactory.setConstitutiveLaw(new PlaneStress(E, mu));
     }
 
     public boolean isAvialable() {
         return rectangleProjectFactory.isAvialable();
     }
 
     public double getEdgePosition(MFRectangleEdge edge) {
         return rectangleProjectFactory.getEdgePosition(edge);
     }
 
     public double getHeight() {
         return rectangleProjectFactory.getHeight();
     }
 
     public int getQuadratureDegree() {
         return rectangleProjectFactory.getQuadratureDegree();
     }
 
     public double getWidth() {
         return rectangleProjectFactory.getWidth();
     }
 
     public void setEdgePosition(MFRectangleEdge edge, double position) {
         rectangleProjectFactory.setEdgePosition(edge, position);
     }
 
     public void setQuadratureDegree(int quadratureDegree) {
         rectangleProjectFactory.setQuadratureDegree(quadratureDegree);
     }
 
     public void setSpaceNodesDisturbRatio(double spaceNodesDisturbRatio) {
         rectangleProjectFactory.setSpaceNodesDisturbRatio(spaceNodesDisturbRatio);
     }
 
     public void setDisturbRand(Random disturbRand) {
         rectangleProjectFactory.setDisturbRand(disturbRand);
     }
 
     public double getSpaceNodesDisturbRatio() {
         return rectangleProjectFactory.getSpaceNodesDisturbRatio();
     }
 
     public static void main(String[] args) {
         TensionBar tensionBar = new TensionBar();
 //        tensionBar.setSpaceNodesDisturbRatio(0.9);
         MFProject project = tensionBar.produce();
         MFLinearMechanicalProcessor processor = new MFLinearMechanicalProcessor();
         processor.setProject(project);
        processor.getSettings().put(MFPreprocessorKey.INTEGRATOR, new SimpsonIntegrator());
         processor.preprocess();
         processor.solve();
         //PostProcessor pp = processor.genPostProcessor();
         MechanicalPostProcessor mpp = processor.genMechanicalPostProcessor();
         int stepNum = 20;
         for (int i = 0; i < stepNum; i++) {
             double[] pt = new double[]{2.99, 0.01 + 2.98 * (i * 1.0 / (stepNum - 1))};
             double[] strain = mpp.engineeringStrain(pt, null);
             double[] value = mpp.value(pt, null);
             System.out.println("pt = " + Arrays.toString(pt) + ", value = " + Arrays.toString(value) + ", eng strain = " + Arrays.toString(strain));
         }
     }
 }
