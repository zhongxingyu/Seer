 /*
  * (c) Copyright by Man YUAN
  */
 package net.epsilony.mf.implicit.demo;
 
 import net.epsilony.tb.implicit.demo.TriangleContourBuilderDemoDrawer;
 import java.awt.Dimension;
 import java.awt.geom.Rectangle2D;
 import java.util.LinkedList;
 import java.util.List;
 import net.epsilony.mf.implicit.MeshfreeLevelSet;
 import net.epsilony.mf.implicit.RectangleWithHoles;
 import net.epsilony.tb.implicit.TriangleContourBuilder;
 import net.epsilony.tb.implicit.MarchingTriangle;
 import net.epsilony.tb.implicit.TriangleContourCell;
 import net.epsilony.tb.implicit.TriangleContourCellFactory;
 import net.epsilony.mf.model.influence.ConstantInfluenceRadiusCalculator;
 import net.epsilony.mf.model.influence.InfluenceRadiusCalculator;
 import net.epsilony.tb.MiscellaneousUtils;
 import net.epsilony.tb.common_func.NormalFunction;
 import net.epsilony.tb.ui.CommonFrame;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class MLSLevelSetDemo {
 
     public static double DEFAULT_SIGMA = 5;
     public static Rectangle2D DEFAULT_RECTANGLE = new Rectangle2D.Double(10, 10, 100, 60);
     public static double DEFAULT_HOLE_RADIUS = 4;
     public static double DEFAULT_HOLE_DISTANCE = 2;
     public static InfluenceRadiusCalculator DEFAULT_INFLUENCE_RADIUS_CALCULATOR =
             new ConstantInfluenceRadiusCalculator(8);
     public static int DEFAULT_QUADRATURE_POWER = 2;
     public static double DEFAULT_TRIANGLE_SIZE = 2;
     public static double DEFAULT_SEGMENT_SIZE = 2;
     NormalFunction assemblyWeightFunction = new NormalFunction();
     double sigma = DEFAULT_SIGMA;
     RectangleWithHoles rectangleWithHoles = new RectangleWithHoles(
             DEFAULT_RECTANGLE, DEFAULT_HOLE_RADIUS, DEFAULT_HOLE_DISTANCE);
     double triangleSize = DEFAULT_TRIANGLE_SIZE;
     double segmentSize = DEFAULT_SEGMENT_SIZE;
     int quadraturePower = DEFAULT_QUADRATURE_POWER;
     MeshfreeLevelSet levelSetFun = new MeshfreeLevelSet();
 
     public void init() {
         assemblyWeightFunction.setSigma(sigma);
 
         rectangleWithHoles.setQuadraturePower(quadraturePower);
         rectangleWithHoles.setSegmentSize(segmentSize);
         rectangleWithHoles.setTriangleSize(triangleSize);
         rectangleWithHoles.prepare();
 

         levelSetFun.setModel(rectangleWithHoles.getModel());
        levelSetFun.setInfluenceRadiusCalculator(DEFAULT_INFLUENCE_RADIUS_CALCULATOR);
         levelSetFun.setmfQuadratureTask(rectangleWithHoles.getmfQuadratureTask());
         levelSetFun.setInfluenceRadiusCalculator(DEFAULT_INFLUENCE_RADIUS_CALCULATOR);
         levelSetFun.setWeightFunction(assemblyWeightFunction);
 
         levelSetFun.prepare();
     }
 
     public static void main(String[] args) {
         MLSLevelSetDemo demo = new MLSLevelSetDemo();
         demo.init();
 
         TriangleContourCellFactory cellFactory = new TriangleContourCellFactory();
         TriangleContourCell[][] coverRectangle = cellFactory.coverRectangle(demo.rectangleWithHoles.getRectangle(), 1);
         TriangleContourBuilder contourBuilder = new MarchingTriangle.OnEdge();
         List<TriangleContourCell> cells = new LinkedList<>();
         MiscellaneousUtils.addToList(coverRectangle, cells);
         contourBuilder.setCells(cells);
         contourBuilder.setLevelSetFunction(demo.levelSetFun.getLevelSetFunction());
 
         contourBuilder.genContour();
         CommonFrame frame = new CommonFrame();
         frame.getMainPanel().addAndSetupModelDrawer(new TriangleContourBuilderDemoDrawer(contourBuilder));
         frame.getMainPanel().setPreferredSize(new Dimension(800, 600));
         frame.pack();
         frame.setVisible(true);
     }
 }
