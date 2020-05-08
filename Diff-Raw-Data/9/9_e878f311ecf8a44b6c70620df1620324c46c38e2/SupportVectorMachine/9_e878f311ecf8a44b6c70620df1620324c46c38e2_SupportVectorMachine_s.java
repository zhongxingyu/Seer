 package org.geworkbench.util.svm;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * @author John Watkinson
  */
 public class SupportVectorMachine {
 
     static Log log = LogFactory.getLog(SupportVectorMachine.class);
 
     private List<float[]> trainingSet;
     private int[] trainingClassifications;
     private KernelFunction kernel;
     private int maxIterations;
     private double convergenceThreshold;
 
     private float[] lambda;
     private int n;
 
     /**
      * Computes AB + 1
      */
     public static final KernelFunction LINEAR_KERNAL_FUNCTION = new KernelFunction() {
         public double eval(float[] a, float[] b) {
             int n = a.length;
             double dot = 0;
             for (int i = 0; i < n; i++) {
                 dot += a[i] * b[i];
             }
            double manual = (dot + 1) * (dot +1);
//            double pow = Math.pow(dot + 1, 2);
 //            if (manual != pow) {
 //                log.error("Not equal");
 //            }
            return manual;
         }
     };
 
     public SupportVectorMachine(List<float[]> caseList, List<float[]> controlList, KernelFunction kernel, int maxIterations, double convergenceThreshold) {
         int caseSize = caseList.size();
         if (caseSize == 0) {
             throw new RuntimeException("Must have at least one case.");
         }
         int controlSize = controlList.size();
         if (controlSize == 0) {
             throw new RuntimeException("Must have at least one control.");
         }
         n = caseSize + controlSize;
         trainingSet = new ArrayList<float[]>(n);
         trainingSet.addAll(caseList);
         trainingSet.addAll(controlList);
         trainingClassifications = new int[n];
         for (int i = 0; i < n; i++) {
             if (i < caseSize) {
                 trainingClassifications[i] = 1;
             } else {
                 trainingClassifications[i] = -1;
             }
         }
         this.kernel = kernel;
         this.maxIterations = maxIterations;
         this.convergenceThreshold = convergenceThreshold;
         this.compute();
     }
 
     private double discriminant(float[] input) {
         double v = 0;
         for (int i = 0; i < n; i++) {
             v += lambda[i] * trainingClassifications[i] * kernel.eval(input, trainingSet.get(i));
         }
         return v;
     }
 
     private double objective() {
         double v = 0;
         for (int i = 0; i < n; i++) {
             v += lambda[i] * (2 - trainingClassifications[i] * discriminant(trainingSet.get(i)));
         }
         return v;
     }
 
     private void compute() {
         lambda = new float[n];
         // Initialize to 0.5
         for (int i = 0; i < n; i++) {
             lambda[i] = 0.5f;
         }
         double lastObjective = Double.NEGATIVE_INFINITY;
         double objectiveVal = objective();
         int iteration = 0;
         while ((Math.abs(objectiveVal - lastObjective) > convergenceThreshold) && (iteration < maxIterations)) {
             for (int i = 0; i < n; i++) {
                 float[] item = trainingSet.get(i);
                 double kernelEval = kernel.eval(item, item);
                 double newLambda = (1 - trainingClassifications[i] * discriminant(item) + lambda[i] * kernelEval) / kernelEval;
                 if (newLambda < 0) {
                     lambda[i] = 0;
 //                } else if (newLambda > 1) {
 //                    lambda[i] = 1;
                 } else {
                     lambda[i] = (float) newLambda;
                 }
             }
             iteration++;
             lastObjective = objectiveVal;
             objectiveVal = objective();
         }
     }
 
     /**
      * Evaluates an input using this SVM.
      *
      * @return <tt>true</tt> if the input is evaluated as a case, <tt>false</tt> otherwise.
      */
     public boolean evaluate(float[] input) {
         double v = discriminant(input);
         if (v < 0) {
             return false;
         } else {
             return true;
         }
 
     }
 
     public void test(float a, float b) {
         float[] t = {a, b};
         boolean result = evaluate(t);
         System.out.println("(" + a + ", " + b + "): " + (result ? "case" : "control"));
     }
 
     public static void main(String[] args) {
         float[] example1 = {(float) 1, (float) 2};
         float[] example2 = {(float) 4, (float) 1};
         float[] example3 = {(float) 5, (float) 2};
         float[] example4 = {(float) 3, (float) 5};
         ArrayList<float[]> cases = new ArrayList<float[]>();
         cases.add(example1);
         cases.add(example2);
         ArrayList<float[]> controls = new ArrayList<float[]>();
         controls.add(example3);
         controls.add(example4);
         SupportVectorMachine svm = new SupportVectorMachine(cases, controls, LINEAR_KERNAL_FUNCTION, 50, 1e-6);
         svm.compute();
         svm.test(4, 1);
         svm.test(1, 2);
         svm.test(3, 5);
         svm.test(4, 6);
         svm.test(0, 0);
         svm.test(2, 1.5f);
     }
 }
