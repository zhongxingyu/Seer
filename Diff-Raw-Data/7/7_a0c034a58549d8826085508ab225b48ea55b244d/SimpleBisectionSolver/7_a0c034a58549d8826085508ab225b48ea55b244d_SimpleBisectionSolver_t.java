 /* (c) Copyright by Man YUAN */
 package net.epsilony.tb.implicit;
 
 import net.epsilony.tb.analysis.DifferentiableFunction;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class SimpleBisectionSolver implements BoundedImplicitFunctionSolver {
 
     double[] lowerBounds;
     double[] upperBounds;
     double functionAbsoluteTolerence;
     double solutionAbsoluteTolerence;
     double[] solution;
     double[] functionValue;
     DifferentiableFunction function;
     int maxEval;
 
     public double[] getLowerBounds() {
         return lowerBounds;
     }
 
     @Override
     public void setLowerBounds(double[] lowerBounds) {
         this.lowerBounds = lowerBounds;
     }
 
     public double[] getUpperBounds() {
         return upperBounds;
     }
 
     @Override
     public void setUpperBounds(double[] upperBounds) {
         this.upperBounds = upperBounds;
     }
 
     @Override
     public double getFunctionAbsoluteTolerence() {
         return functionAbsoluteTolerence;
     }
 
     @Override
     public void setFunctionAbsoluteTolerence(double functionAbsoluteTolerence) {
         this.functionAbsoluteTolerence = functionAbsoluteTolerence;
     }
 
     @Override
     public double getSolutionAbsoluteTolerence() {
         return solutionAbsoluteTolerence;
     }
 
     @Override
     public void setSolutionAbsoluteTolerence(double solutionAbsoluteTolerence) {
         this.solutionAbsoluteTolerence = solutionAbsoluteTolerence;
     }
 
     @Override
     public double[] getSolution() {
         return solution;
     }
 
     public void setSolution(double[] solution) {
         this.solution = solution;
     }
 
     @Override
     public double[] getFunctionValue() {
         return functionValue;
     }
 
     public DifferentiableFunction getFunction() {
         return function;
     }
 
     @Override
     public void setFunction(DifferentiableFunction function) {
         if (function.getInputDimension() != 1 || function.getOutputDimension() != 1) {
             throw new IllegalArgumentException("only supported 1d");
         }
         this.function = function;
     }
 
     @Override
     public int getMaxEval() {
         return maxEval;
     }
 
     @Override
     public void setMaxEval(int maxEval) {
         this.maxEval = maxEval;
     }
 
     @Override
     public boolean solve(double[] start) {
         solution = new double[1];
         functionValue = new double[1];
         if (null == upperBounds || null == lowerBounds) {
             throw new IllegalStateException("upper and lower bounds must be set");
         }
         if (start[0] > upperBounds[0] || start[0] < lowerBounds[0]) {
             throw new IllegalArgumentException("start point is out of bounds");
         }
         if (upperBounds[0] < lowerBounds[0]) {
             throw new IllegalStateException("upper bound is lower then the lower bound");
         }
 
         function.setDiffOrder(0);
 
         function.value(lowerBounds, functionValue);
         double vs = functionValue[0];
         function.value(upperBounds, functionValue);
         double ve = functionValue[0];
        functionValue = function.value(start, functionValue);
        double vm = functionValue[0];
 
         if (Math.signum(vs) == Math.signum(ve)) {
             throw new IllegalStateException("same sign on both ends");
         }
 
         double ts = lowerBounds[0];
         double te = upperBounds[0];
         double tm = start[0];
         int count = 0;
        solution[0] = tm;
         while (Math.abs(vm) > functionAbsoluteTolerence && te - ts > solutionAbsoluteTolerence && count++ < maxEval) {
             if (Math.signum(vm) == Math.signum(vs)) {
                 ts = tm;
                 vs = vm;
             } else {
                 te = tm;
             }
             tm = (te + ts) / 2;
             solution[0] = tm;
             function.value(solution, functionValue);
             vm = functionValue[0];
         }
         return true;
 
     }
 }
