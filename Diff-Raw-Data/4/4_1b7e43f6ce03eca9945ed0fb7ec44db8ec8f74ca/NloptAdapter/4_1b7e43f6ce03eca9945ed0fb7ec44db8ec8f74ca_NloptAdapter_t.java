 /*
  * (c) Copyright by Man YUAN
  */
 package net.epsilony.tb.nlopt;
 
import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import net.epsilony.tb.analysis.DifferentiableFunction;
 import static net.epsilony.tb.nlopt.NloptLibrary.*;
 import org.bridj.IntValuedEnum;
 import org.bridj.Pointer;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class NloptAdapter {
 
     NloptOpt opt;
     double[] solution;
     NloptFunc minObjectFunction;
     List<NloptFunc> inequalConstraintsHolder = new LinkedList<>();
     List<NloptMfunc> inequalMconstraintsHolder = new LinkedList<>();
     List<NloptFunc> equalConstraintsHolder = new LinkedList<>();
     List<NloptMfunc> equalMconstraintsHolder = new LinkedList<>();
 
     public NloptAdapter(IntValuedEnum<NloptAlgorithm> algorithm, int dimension) {
         opt = nloptCreate(algorithm, dimension);
         solution = new double[dimension];
     }
 
     @Override
     protected void finalize() throws Throwable {
         try {
             nloptDestroy(opt);
         } finally {
             super.finalize(); //To change body of generated methods, choose Tools | Templates.
         }
     }
 
     public int getDimension() {
         return nloptGetDimension(opt);
     }
 
     public IntValuedEnum<NloptAlgorithm> getAlgorithm() {
         return nloptGetAlgorithm(opt);
     }
 
     public String getAlgorithmName() {
         return nloptAlgorithmName(getAlgorithm()).getCString();
     }
 
     public void setMinObjective(DifferentiableFunction<double[], double[]> function) {
         if (function.getOutputDimension() != 1 || function.getInputDimension() != getDimension()) {
             throw new IllegalArgumentException("dimension mismatch");
         }
         minObjectFunction = new NloptFunctionAdapter(function);
         IntValuedEnum<NloptResult> nloptResult = nloptSetMinObjective(opt, Pointer.pointerTo(minObjectFunction), Pointer.NULL);
         checkNloptResult(nloptResult);
     }
 
     public void setLowerBounds(double[] lowerBounds) {
         Pointer<Double> nloptLowerBounds = Pointer.pointerToDoubles(lowerBounds);
         IntValuedEnum<NloptResult> nloptResult = nloptSetLowerBounds(opt, nloptLowerBounds);
         checkNloptResult(nloptResult);
     }
 
     public void setUpperBounds(double[] upperBounds) {
         Pointer<Double> nloptUpperBounds = Pointer.pointerToDoubles(upperBounds);
         IntValuedEnum<NloptResult> nloptResult = nloptSetUpperBounds(opt, nloptUpperBounds);
         checkNloptResult(nloptResult);
     }
 
     public void setLowerBounds(double lowerBounds) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetLowerBounds1(opt, lowerBounds);
         checkNloptResult(nloptResult);
     }
 
     public void setUpperBounds(double upperBounds) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetUpperBounds1(opt, upperBounds);
         checkNloptResult(nloptResult);
     }
 
     public double[] getLowerBounds() {
         Pointer<Double> nloptLowerBounds = Pointer.allocateDoubles(getDimension());
         IntValuedEnum<NloptResult> nloptResult = nloptGetLowerBounds(opt, nloptLowerBounds);
         checkNloptResult(nloptResult);
         return nloptLowerBounds.getDoubles();
     }
 
     public double[] getUpperBounds() {
         Pointer<Double> nloptUpperBounds = Pointer.allocateDoubles(getDimension());
         IntValuedEnum<NloptResult> nloptResult = nloptGetUpperBounds(opt, nloptUpperBounds);
         checkNloptResult(nloptResult);
         return nloptUpperBounds.getDoubles();
     }
 
     public void addInequalityConstraint(DifferentiableFunction<double[], double[]> function, double tolerence) {
         if (function.getOutputDimension() != 1 || function.getInputDimension() != getDimension()) {
             throw new IllegalArgumentException("dimension mismatch");
         }
         NloptFunc nloptFunction = new NloptFunctionAdapter(function);
         IntValuedEnum<NloptResult> nloptResult = nloptAddInequalityConstraint(opt, Pointer.pointerTo(nloptFunction), Pointer.NULL, tolerence);
         checkNloptResult(nloptResult);
         inequalConstraintsHolder.add(nloptFunction);
     }
 
     public void addEqualityConstraint(DifferentiableFunction<double[], double[]> function, double tolerence) {
         if (function.getOutputDimension() != 1 || function.getInputDimension() != getDimension()) {
             throw new IllegalArgumentException("dimension mismatch");
         }
         NloptFunc nloptFunction = new NloptFunctionAdapter(function);
         IntValuedEnum<NloptResult> nloptResult = nloptAddEqualityConstraint(opt, Pointer.pointerTo(nloptFunction), Pointer.NULL, tolerence);
         checkNloptResult(nloptResult);
         equalConstraintsHolder.add(nloptFunction);
     }
 
     public void removeInequalityConstraints() {
         inequalConstraintsHolder.clear();
         inequalMconstraintsHolder.clear();
         IntValuedEnum<NloptResult> nloptResult = nloptRemoveInequalityConstraints(opt);
         checkNloptResult(nloptResult);
     }
 
     public void removeEqualityConstraints() {
         equalConstraintsHolder.clear();
         equalMconstraintsHolder.clear();
         IntValuedEnum<NloptResult> nloptResult = nloptRemoveEqualityConstraints(opt);
         checkNloptResult(nloptResult);
     }
 
     public void addInequalityVectorConstraint(DifferentiableFunction<double[], double[]> function, double[] tols) {
         if (function.getInputDimension() != getDimension()) {
             throw new IllegalArgumentException("dimension mismatch");
         }
         int m = function.getOutputDimension();
         NloptMfunc nloptMfunc = new NloptMFunctionAdapter(function);
         IntValuedEnum<NloptResult> nloptResult = nloptAddInequalityMconstraint(
                 opt,
                 m, Pointer.pointerTo(nloptMfunc),
                 Pointer.NULL,
                 Pointer.pointerToDoubles(tols));
         checkNloptResult(nloptResult);
         inequalMconstraintsHolder.add(nloptMfunc);
     }
 
     public void addEqualityVectorConstraint(DifferentiableFunction<double[], double[]> function, double[] tols) {
         if (function.getInputDimension() != getDimension()) {
             throw new IllegalArgumentException("dimension mismatch");
         }
         int m = function.getOutputDimension();
         NloptMfunc nloptMfunc = new NloptMFunctionAdapter(function);
         IntValuedEnum<NloptResult> nloptResult = nloptAddEqualityMconstraint(
                 opt,
                 m, Pointer.pointerTo(nloptMfunc),
                 Pointer.NULL,
                 Pointer.pointerToDoubles(tols));
         checkNloptResult(nloptResult);
         equalMconstraintsHolder.add(nloptMfunc);
     }
 
     public void setStopValue(double value) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetStopval(opt, value);
         checkNloptResult(nloptResult);
     }
 
     public double getStopValue() {
         return nloptGetStopval(opt);
     }
 
     public void setRelativeFunctionTolerence(double tol) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetFtolRel(opt, tol);
         checkNloptResult(nloptResult);
     }
 
     public double getRelativeFunctionTolerence() {
         return nloptGetFtolRel(opt);
     }
 
     public void setAbsoluteFunctionTolerence(double tol) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetFtolAbs(opt, tol);
         checkNloptResult(nloptResult);
     }
 
     public double getAbsoluteFunctionTolerence() {
         return nloptGetFtolAbs(opt);
     }
 
     public void setRelativeXTolerence(double tol) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetXtolRel(opt, tol);
         checkNloptResult(nloptResult);
     }
 
     public double getRelativeXTolerence() {
         return nloptGetXtolRel(opt);
     }
 
     public void setAbsoluteXTolerence(double[] tols) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetXtolAbs(opt, Pointer.pointerToDoubles(tols));
         checkNloptResult(nloptResult);
     }
 
     public void setAbsoluteXTolerence(double tols) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetXtolAbs1(opt, tols);
         checkNloptResult(nloptResult);
     }
 
     public double[] getAbsoluteXTolerence() {
         Pointer<Double> tolsPointer = Pointer.allocateDoubles(getDimension());
         nloptGetXtolAbs(opt, tolsPointer);
         return tolsPointer.getDoubles();
     }
 
     public void setMaxEval(int maxEval) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetMaxeval(opt, maxEval);
         checkNloptResult(nloptResult);
     }
 
     public int getMaxEval() {
         return nloptGetMaxeval(opt);
     }
 
     public void setMaxTime(double seconds) {
         IntValuedEnum<NloptResult> nloptResult = nloptSetMaxtime(opt, seconds);
         checkNloptResult(nloptResult);
     }
 
     public double getMaxTime() {
         return nloptGetMaxtime(opt);
     }
 
     //TODO: let argument be only one and let the object contains the gradient information
     public IntValuedEnum<NloptResult> optimize(double[] startPoint, double[] objectVal) {
         Pointer<Double> objectValPointer = Pointer.allocateDouble();
         final Pointer<Double> startPointer = Pointer.pointerToDoubles(startPoint);
         IntValuedEnum<NloptResult> nloptResult = nloptOptimize(opt, startPointer, objectValPointer);
         if (null != objectVal) {
             objectVal[0] = objectValPointer.getDouble();
         }
         startPointer.getDoubles(solution);
         return nloptResult;
     }
 
     public double[] getSolution() {
        return Arrays.copyOf(solution, solution.length);
     }
 
     public static void main(String[] args) {
         NloptAdapter opt = new NloptAdapter(NloptAlgorithm.NLOPT_GD_MLSL_LDS, 3);
         System.out.println("opt.getAlgorithmName() = " + opt.getAlgorithmName());
     }
 
     private void checkNloptResult(IntValuedEnum<NloptResult> nloptResult) throws IllegalArgumentException {
         if (nloptResult.value() != NloptResult.NLOPT_SUCCESS.value) {
             throw new IllegalArgumentException(nloptResult.toString());
         }
     }
 }
