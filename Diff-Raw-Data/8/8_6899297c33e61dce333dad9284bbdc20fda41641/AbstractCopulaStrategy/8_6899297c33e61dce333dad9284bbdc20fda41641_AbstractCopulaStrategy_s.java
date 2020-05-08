 package org.pillarone.riskanalytics.domain.utils.math.copula;
 
 import cern.colt.matrix.DoubleMatrix1D;
 import cern.colt.matrix.DoubleMatrix2D;
 import cern.colt.matrix.impl.DenseDoubleMatrix2D;
 import cern.colt.matrix.linalg.EigenvalueDecomposition;
 import org.pillarone.riskanalytics.core.parameterization.AbstractParameterObject;
 import org.pillarone.riskanalytics.core.parameterization.ComboBoxMatrixMultiDimensionalParameter;
 
 import java.util.List;
 
 /**
  * @author jessika.walter (at) intuitive-collaboration (dot) com
  */
 public abstract class AbstractCopulaStrategy extends AbstractParameterObject implements ICopulaStrategy {
 
     public void checkDependencyMatrix(ComboBoxMatrixMultiDimensionalParameter dependencyMatrix) {
         List<List<Double>> values = dependencyMatrix.getValues();
         for (int i = 0; i < values.size(); i++) {
             if (!(values.get(i).get(i) == 1d)) {
                throw new IllegalArgumentException("CopulaStratey.dependencyMatrixInvalidDiagonal");
             }
         }
 
         DenseDoubleMatrix2D sigma = new DenseDoubleMatrix2D(values.size(), values.size());
         for (int i = 0; i < values.size(); i++) {
             for (int j = 0; j < values.get(i).size(); j++) {
                 sigma.set(i, j, values.get(i).get(j));
             }
         }
         DoubleMatrix2D sigmaTranspose = sigma.viewDice();
         if (!sigmaTranspose.equals(sigma)) {
            throw new IllegalArgumentException("CopulaStratey.dependencyMatrixNonSymmetric");
         }
         EigenvalueDecomposition eigenvalueDecomp = new EigenvalueDecomposition(sigma);
         DoubleMatrix1D eigenvalues = eigenvalueDecomp.getRealEigenvalues();
         eigenvalues.viewSorted();
         if (eigenvalues.get(0) <= 0) {
            throw new IllegalArgumentException("CopulaStratey.dependencyMatrixNonPosDef");
         }
     }
 }
