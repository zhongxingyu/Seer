 package org.pillarone.riskanalytics.domain.utils.math.copula;
 
 import cern.colt.matrix.DoubleMatrix1D;
 import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.impl.DenseDoubleMatrix2D;
 import cern.colt.matrix.linalg.EigenvalueDecomposition;
 import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject;
 import org.pillarone.riskanalytics.core.parameterization.ComboBoxMatrixMultiDimensionalParameter;
 import org.pillarone.riskanalytics.core.simulation.SimulationException;
 import org.pillarone.riskanalytics.domain.utils.math.dependance.DependancePacket;
 
 import java.util.List;
 
 /**
  * @author jessika.walter (at) intuitive-collaboration (dot) com
  */
 public abstract class AbstractCopulaStrategy extends AbstractParameterObject implements ICopulaStrategy {
 
     public void checkDependencyMatrix(ComboBoxMatrixMultiDimensionalParameter dependencyMatrix) {
         List<List<Number>> values = dependencyMatrix.getValues();
         for (int i = 0; i < values.size(); i++) {
             if (!(values.get(i).get(i).doubleValue() == 1)) {
                 throw new IllegalArgumentException("['CopulaStratey.dependencyMatrixInvalidDiagonal']");
             }
         }
 
         DenseDoubleMatrix2D sigma = new DenseDoubleMatrix2D(values.size(), values.size());
         for (int i = 0; i < values.size(); i++) {
             for (int j = 0; j < values.get(i).size(); j++) {
                 sigma.set(i, j, values.get(i).get(j).doubleValue());
             }
         }
         DoubleMatrix2D sigmaTranspose = sigma.viewDice();
         if (!sigmaTranspose.equals(sigma)) {
             throw new IllegalArgumentException("['CopulaStratey.dependencyMatrixNonSymmetric']");
         }
         EigenvalueDecomposition eigenvalueDecomp = new EigenvalueDecomposition(sigma);
         DoubleMatrix1D eigenvalues = eigenvalueDecomp.getRealEigenvalues();
         eigenvalues.viewSorted();
         if (eigenvalues.get(0) <= 0) {
             throw new IllegalArgumentException("['CopulaStratey.dependencyMatrixNonPosDef']");
         }
     }
 
     @Override
     /**
      * This method ensures backward compatibility for existing copulae classes.
      */
     public DependancePacket getDependance(Integer modelPeriod) {
         List<Number> stream = getRandomVector();
         List<String> names = getTargetNames();
         if(stream.size() != names.size() ) {
             throw new SimulationException("Generated differen number of random numbers to number of targets. Contact development");
         }
 
         final DependancePacket dependancePacket = new DependancePacket();
         for (int j = 0; j < stream.size() ; j++) {
             dependancePacket.addMarginal(names.get(j), modelPeriod, stream.get(j).doubleValue());
         }
         return dependancePacket.immutable();
     }
 
     @Override
     public DependancePacket getDependanceAllPeriod(Integer finalModelPeriod) {
         final DependancePacket dependancePacket = new DependancePacket();
         for (int i = 1; i <= finalModelPeriod; i++) {
             List<Number> stream = getRandomVector();
             List<String> names = getTargetNames();
             if(stream.size() != names.size() ) {
                throw new SimulationException("Generated different number of random numbers to number of targets. Contact development");
             }
             for (int j = 0; j < stream.size() ; j++) {
                dependancePacket.addMarginal(names.get(j), i, stream.get(j).doubleValue());
             }
         }
         return dependancePacket.immutable();
     }
 }
