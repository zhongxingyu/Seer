 package edu.ucdenver.bios.powersvc.resource;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 
 import edu.ucdenver.bios.webservice.common.domain.Blob2DArray;
 import edu.ucdenver.bios.webservice.common.domain.Covariance;
 import edu.ucdenver.bios.webservice.common.domain.RepeatedMeasuresNode;
 import edu.ucdenver.bios.webservice.common.domain.ResponseNode;
 import edu.ucdenver.bios.webservice.common.domain.Spacing;
 import edu.ucdenver.bios.webservice.common.domain.StandardDeviation;
 
 public class CovarianceHelper {
 
     /**
      * Convert a covariance object into a RealMatrix.
      * @param covariance Covariance domain object
      * @param rmNode the repeated measures information associated
      * with the covariance object
      * @return covariance as a RealMatrix
      */
     public static RealMatrix covarianceToRealMatrix(Covariance covariance, 
             RepeatedMeasuresNode rmNode) {
         if (covariance.getColumns() != covariance.getRows()) {
             throw new IllegalArgumentException("Non-square covariance matrix");
         }
 
         // convert the spacing list to integers
         ArrayList<Integer> intSpacingList = new ArrayList<Integer>();
         if (rmNode.getSpacingList() != null) {
             for(Spacing spacingValue: rmNode.getSpacingList()) {
                 intSpacingList.add(spacingValue.getValue());
             }
         } else {
             for(int i = 0; i < rmNode.getNumberOfMeasurements(); i++) {
                 intSpacingList.add(i);
             }
         }
 
         // create a covariance matrix based on the Covariance domain object
         RealMatrix covarianceData = null;
         if (covariance.getType() != null) {
             switch (covariance.getType()) {
             case LEAR_CORRELATION:
                 /* LEAR correlation model.  Should have a standard deviation, 
                  * rho, and delta specified */
                 covarianceData = buildLearCovariance(covariance, intSpacingList);
                 break;
             case UNSTRUCTURED_CORRELATION:
                 // indicates structured correlation, so we convert to a covariance matrix
                 covarianceData = buildCovarianceFromCorrelation(covariance);
                 break;
             case UNSTRUCTURED_COVARIANCE:
                 // unstructured covariance, so simply extract the data
                 Blob2DArray blob = covariance.getBlob();
                 if (blob != null) {
                     covarianceData = new Array2DRowRealMatrix(covariance.getBlob().getData());
                 }
                 break;
             }
         }
         return covarianceData;
 
     }
 
     /**
      * Create a covariance matrix for responses
      * @param covariance
      * @param responsesList
      * @return
      */
     public static RealMatrix covarianceToRealMatrix(Covariance covariance, 
             List<ResponseNode> responsesList) {
         if (covariance == null) {
             throw new IllegalArgumentException("Missing covariance for response variables");
         }
         if (covariance.getColumns() != covariance.getRows()) {
             throw new IllegalArgumentException("Non-square covariance matrix");
         }
         if (responsesList == null) {
             throw new IllegalArgumentException("No response variables specified");
         }
 
         // create equal spacing across the responses
         ArrayList<Integer> intSpacingList = new ArrayList<Integer>();
         for(int i = 0; i < responsesList.size(); i++) {
             intSpacingList.add(i);
         }
 
         // create a covariance matrix based on the Covariance domain object
         RealMatrix covarianceData = null;
         if (covariance.getType() != null) {
             switch (covariance.getType()) {
             case LEAR_CORRELATION:
                 /* LEAR correlation model.  Should have a standard deviation, 
                  * rho, and delta specified */
                 covarianceData = buildLearCovariance(covariance, intSpacingList);
                 break;
             case UNSTRUCTURED_CORRELATION:
                 // indicates structured correlation, so we convert to a covariance matrix
                 covarianceData = buildCovarianceFromCorrelation(covariance);
                 break;
             case UNSTRUCTURED_COVARIANCE:
                 // unstructured covariance, so simply extract the data
                 Blob2DArray blob = covariance.getBlob();
                 if (blob != null) {
                     covarianceData = new Array2DRowRealMatrix(covariance.getBlob().getData());
                 }
                 break;
             }
         }
         return covarianceData;
     }
 
     /**
      * Convert a correlation into a covariance matrix
      * @param covariance
      * @return
      */
     private static RealMatrix buildCovarianceFromCorrelation(Covariance covariance) {
         RealMatrix covarianceData = null;
         Blob2DArray blob = covariance.getBlob();
 
         if (blob != null && blob.getData() != null) {
             List<StandardDeviation> stddevList = covariance.getStandardDeviationList();
             if (stddevList.size() == covariance.getRows()) {
                 covarianceData = new Array2DRowRealMatrix(covariance.getBlob().getData());
                 /* For each diagonal cell, square the standard deviation
                  * For each off-diagonal cell, use formula
                  * covariance = correlation * sqrt (var1 * var2) 
                  */
                 for(int row = 0; row < covariance.getRows(); row++) {
                     for(int col = 0; col < covariance.getColumns(); col++) {
                         double value = covarianceData.getEntry(row, col);
                         if (row == col) {
                             double stddev = stddevList.get(row).getValue();
                             covarianceData.setEntry(row, col, stddev*stddev);
                         } else {
                             double stddevRow = stddevList.get(row).getValue();
                             double stddevCol = stddevList.get(col).getValue();
                             covarianceData.setEntry(row, col, 
                                    stddevRow*stddevRow*stddevCol*stddevCol);
                         } 
                     }
                 }
             }
         }
         return covarianceData;
     }
 
 
     /**
      * Create a Lear covariance matrix
      * @param covariance Covariance domain object
      * @param columns column dimension
      * @param stddev standard deviation
      * @param rho correlation for observations 1 unit apart
      * @param delta rate of correlation decay
      * @param spacingList spacing list
      * @return RealMatrix containing the Lear covariance
      */
     private static RealMatrix buildLearCovariance(Covariance covariance,
             List<Integer> intSpacingList) {
         RealMatrix covarianceData = null;
         int rows = covariance.getRows();
         int columns = covariance.getColumns();
         List<StandardDeviation> stddevList = covariance.getStandardDeviationList();
         // make sure everything is valid
         if (stddevList != null && stddevList.size() > 0 &&
                 covariance.getRho() != Double.NaN &&
                 covariance.getDelta() != Double.NaN &&
                 covariance.getDelta() >= 0) {
 
             LearCorrelation lear = new LearCorrelation(intSpacingList);
             StandardDeviation stddev = stddevList.get(0);
 
             double[][] data = new double[rows][columns];
 
             for(int row = 0; row < rows; row++) {
                 for(int col = row; col < columns; col++) {
                     if (row == col) {
                         data[row][col] = stddev.getValue() * stddev.getValue();
                     } else {
                         double value = (lear.getRho(row, col, covariance.getRho(), covariance.getDelta()) *
                                 stddev.getValue() * stddev.getValue());
                         data[row][col] = value;
                         data[col][row] = value;
                     }
                 }
             }
 
             covarianceData =  new Array2DRowRealMatrix(data);
         }
         return covarianceData;
     }
 
 
 }
