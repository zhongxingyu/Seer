 package engine.calculation;
 
 import engine.calculation.functions.Subtraction;
import engine.calculation.vector.*;
 import engine.calculation.vector.fillers.ConstantVectorFiller;
 import engine.calculation.vector.fillers.LinearVectorFiller;
 import engine.expressions.Equation;
 import engine.expressions.Function;
 import engine.locus.DiscreteLocus;
 import engine.locus.PixelDrawable;
 
 import java.util.Arrays;
 
 /**
  * User: Oleksiy Pylypenko
  * Date: 3/13/13
  * Time: 11:26 AM
  */
 public class VectorCalculationEngine implements CalculationEngine {
     @Override
     public PixelDrawable[] calculate(final int width, final int height, Equation[] equations) {
         int nEq = equations.length;
 
         PixelDrawable []ret = new PixelDrawable[nEq];
         Function[] multiFunction = new Function[nEq];
 
         for (int i = 0; i < nEq; i++)
         {
             Equation equation = equations[i];
             multiFunction[i] = new Subtraction(equation.getLeftPart(),
                     equation.getRightPart());
         }
 
         VectorMachineEvaluator evaluator = new VectorMachineEvaluator();
 
         evaluator.setSize(width + 1);
         evaluator.setFunctions(multiFunction);
        evaluator.setConcurrency(2);
 
         evaluator.prepare();
 
         int [][][]locusData = new int[nEq][height][];
 
         double xDelta = 1.0 / (double) (width + 1);
         XYTVectorArguments arguments = new XYTVectorArguments(0, xDelta);
 
         double [][]prevMatrix = null;
         for (int y = 0; y <= height; y++) {
             double[][] matrix = null;
             try {
                 arguments.setY(((double)y) / (height + 1));
 
                 matrix = evaluator.calculate(arguments);
 
                 if (y == 0) {
                     continue;
                 }
 
                 for (int i = 0; i < nEq; i++) {
                     Equation.Type eqType = equations[i].getType();
 
                     double []row = matrix[i];
                     double []prevRow = prevMatrix[i];
 
                     locusData[i][y - 1] = eqType
                             .accept(new LocusRowDiffVisitor(row, prevRow));
                 }
             }finally {
                 if (matrix != null) {
                     prevMatrix = copy(matrix);
                 }
             }
         }
 
         for (int i = 0; i < nEq; i++) {
             ret[i] = new DiscreteLocus(locusData[i]);
         }
 
         return ret;
     }
 
    private static double[][] copy(double[][] input) {
        double [][]ret = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            double[] values = input[i];
             ret[i] = Arrays.copyOf(values, values.length);
         }
         return ret;
     }
 
     private class XYTVectorArguments implements VectorArguments {
         private final ConstantVectorFiller yFiller;
         private final ConstantVectorFiller tFiller;
         private final LinearVectorFiller xFiller;
 
         public XYTVectorArguments(double xStart, double xDelta) {
             yFiller = new ConstantVectorFiller(0);
             tFiller = new ConstantVectorFiller(0);
             xFiller = new LinearVectorFiller(xStart, xDelta);
         }
 
         @Override
         public String[] getArguments() {
             return new String[] { "x", "y", "t" };
         }
 
         public void setY(double y) {
             yFiller.setValue(y);
         }
 
         public void setT(double t) {
             tFiller.setValue(t);
         }
 
         @Override
         public VectorFiller getVectorFiller(String argument) {
             if ("x".equals(argument)) {
                 return xFiller;
             } else if ("y".equals(argument)) {
                 return yFiller;
             } else if ("t".equals(argument)) {
                 return tFiller;
             }
             throw new UnknownArgumentUsedException(argument);
         }
     }
 }
