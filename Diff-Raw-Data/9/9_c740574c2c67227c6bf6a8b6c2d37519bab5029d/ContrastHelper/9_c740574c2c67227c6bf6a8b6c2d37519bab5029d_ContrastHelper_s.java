 /*
  * Power Service for the GLIMMPSE Software System.  Processes
  * incoming HTTP requests for power, sample size, and detectable
  * difference
  *
  * Copyright (C) 2010 Regents of the University of Colorado.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 package edu.ucdenver.bios.powersvc.resource;
 
 import java.util.List;
 
 import org.apache.commons.math.linear.RealMatrix;
 
 import edu.cudenver.bios.matrix.MatrixUtils;
 import edu.cudenver.bios.matrix.OrthogonalPolynomials;
 import edu.ucdenver.bios.webservice.common.domain.BetweenParticipantFactor;
 import edu.ucdenver.bios.webservice.common.domain.Category;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisBetweenParticipantMapping;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisRepeatedMeasuresMapping;
 import edu.ucdenver.bios.webservice.common.domain.RepeatedMeasuresNode;
 import edu.ucdenver.bios.webservice.common.domain.Spacing;
 import edu.ucdenver.bios.webservice.common.enums.HypothesisTrendTypeEnum;
 
 /**
  * Contrast generator class
  * @author Sarah Kreidler
  */
 public class ContrastHelper {
     /* TODO: this should be moved to the JavaStats library eventually */
 
     /**
      * Create a main effect contrast for between participant factors.
      * @param factorOfInterest factor being tested
      * @param factorList list of all between participant effects
      * @return
      */
     public static RealMatrix mainEffectBetween(BetweenParticipantFactor factorOfInterest, 
             List<BetweenParticipantFactor> factorList) {
 
         // build contrast component for the effect of interest 
         int levels = factorOfInterest.getCategoryList().size();
         int df = levels-1;
         RealMatrix negIdentity = 
             org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(df).scalarMultiply(-1);
         RealMatrix column1s = MatrixUtils.getRealMatrixWithFilledValue(df, 1, 1);
         RealMatrix effectContrast = MatrixUtils.getHorizontalAppend(column1s, negIdentity);
 
         // perform a Kronecker product across the factors with the effect of interest
         // and average contrasts for any remaining factors
         if (df > 0) {
             RealMatrix contrast = org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(1);
             for(BetweenParticipantFactor factor: factorList) {
                 if (factor.getPredictorName().equals(factorOfInterest.getPredictorName())) {
                     contrast = MatrixUtils.getKroneckerProduct(contrast, effectContrast);
                 } else {
                     List<Category> categoryList = factor.getCategoryList();
                     if (categoryList.size() > 0) {
                         int dimension = categoryList.size();
                         contrast = MatrixUtils.getKroneckerProduct(contrast,
                                 MatrixUtils.getRealMatrixWithFilledValue(1, dimension, 1/(double) dimension));
                     }
                 }
             }
             return contrast;
         } else {
             return ContrastHelper.grandMeanBetween(factorList);
         }
     }
 
     /**
      * Create a main effect contrast for within participant factors.
      * @param factorOfInterest factor being tested
      * @param factorList additional within participant effects
      * @return within participant contrast (U matrix)
      */
     public static RealMatrix mainEffectWithin(RepeatedMeasuresNode factorOfInterest, 
             List<RepeatedMeasuresNode> factorList) {
 
         // build contrast component for the effect of interest 
         int levels = factorOfInterest.getNumberOfMeasurements();
         int df = levels-1;
         RealMatrix negIdentity = 
             org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(df).scalarMultiply(-1);
         RealMatrix column1s = MatrixUtils.getRealMatrixWithFilledValue(df, 1, 1);
         RealMatrix effectContrast = MatrixUtils.getHorizontalAppend(column1s, negIdentity).transpose();
 
         // perform a kronecker product across the factors with the effect of interest
         // and average contrasts for any remaining factors
         if (df > 0) {
             RealMatrix contrast = org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(1);
             for(RepeatedMeasuresNode factor: factorList) {
                 if (factor.getDimension().equals(factorOfInterest.getDimension())) {
                     contrast = MatrixUtils.getKroneckerProduct(contrast, effectContrast);
                 } else {
                     int size = factor.getNumberOfMeasurements();
                     if (size > 0) {
                         contrast = MatrixUtils.getKroneckerProduct(contrast,
                                 MatrixUtils.getRealMatrixWithFilledValue(size, 1, 1/(double) size));
                     }
                 }
             }
             return contrast;
         } else {
             return ContrastHelper.grandMeanWithin(factorList);
         }
     }
 
     /**
      * Create an interaction contrast for between participant effects
      * @param betweenMap list of all between participant effects being tested
      * @param factorList list of all between participant effects
      * @return between participant interaction contrast (C matrix)
      */
     public static RealMatrix interactionBetween(List<HypothesisBetweenParticipantMapping> betweenMap,
             List<BetweenParticipantFactor> factorList) {
         RealMatrix contrast = 
             org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(1);
         for(BetweenParticipantFactor factor: factorList) {
 
             HypothesisBetweenParticipantMapping factorMapping = 
                 betweenFactorInTestList(factor, betweenMap);
             if (factorMapping != null) {
                 // create even spacing
                 int levels = factor.getCategoryList().size();
                 double[] spacing = new double[levels];
                 for(int i = 0; i < levels; i++) { spacing[i] = i; }
                 contrast = MatrixUtils.getKroneckerProduct(contrast,
                         getTrendContrast(spacing, factorMapping.getType(), true));
             } else {
                 List<Category> categoryList = factor.getCategoryList();
                 if (categoryList.size() > 0) {
                     int dimension = categoryList.size();
                     contrast = MatrixUtils.getKroneckerProduct(contrast,
                             MatrixUtils.getRealMatrixWithFilledValue(1, dimension, 1/(double) dimension));
                 }
             }
         }
         return contrast;
     }
 
     /**
      * Create an interaction contrast for between participant effects
      * @param betweenMap list of all between participant effects being tested
      * @param factorList list of all between participant effects
      * @return between participant interaction contrast (C matrix)
      */
     public static RealMatrix interactionWithin(List<HypothesisRepeatedMeasuresMapping> withinMap,
             List<RepeatedMeasuresNode> factorList) {
         RealMatrix contrast = 
             org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(1);
         for(RepeatedMeasuresNode factor: factorList) {
 
             HypothesisRepeatedMeasuresMapping factorMapping = 
                 withinFactorInTestList(factor, withinMap);
             if (factorMapping != null) {
                 List<Spacing> spacingList = factorMapping.getRepeatedMeasuresNode().getSpacingList();
                 double[] spacingArray = new double[spacingList.size()];
                 for(int i = 0; i < spacingList.size(); i++) { spacingArray[i] = spacingList.get(i).getValue(); }
                 contrast = MatrixUtils.getKroneckerProduct(contrast,
                         getTrendContrast(spacingArray, 
                                 factorMapping.getType(), false));
             } else {
                 int size = factor.getNumberOfMeasurements();
                 contrast = MatrixUtils.getKroneckerProduct(contrast,
                         MatrixUtils.getRealMatrixWithFilledValue(size, 1, 1/(double) size));
             }
         }
         return contrast;
     }
 
     /**
      * Create a trend test fpr the specified factor of interest
      * @param factorOfInterestMap factor of interest plus trend type information
      * @param factorList list of all between participant factors
      * @return trend contrast
      */
     public static RealMatrix trendBetween(HypothesisBetweenParticipantMapping factorOfInterestMap, 
             List<BetweenParticipantFactor> factorList) {
 
         // build contrast component for the effect of interest 
         BetweenParticipantFactor factorOfInterest = factorOfInterestMap.getBetweenParticipantFactor();
         HypothesisTrendTypeEnum trendType = factorOfInterestMap.getType();
         int levels = factorOfInterest.getCategoryList().size();
         if (levels > 1) {
             // create even spacing
             double[] spacing = new double[levels];
             for(int i = 0; i < levels; i++) { spacing[i] = i; }
             RealMatrix trendContrast = getTrendContrast(spacing, trendType, true);
 
             // Kronecker product the trend contrast with average contrasts for remaining
             // factors
             RealMatrix contrast = MatrixUtils.getRealMatrixWithFilledValue(1, 1, 1);
             int df = trendContrast.getRowDimension();
             for(BetweenParticipantFactor factor: factorList) {
                 if (factor.getPredictorName().equals(factorOfInterest.getPredictorName())) {
                     contrast = MatrixUtils.getKroneckerProduct(contrast, trendContrast);
                 } else {
                     List<Category> categoryList = factor.getCategoryList();
                     if (categoryList.size() > 0) {
                         int dimension = categoryList.size();
                         contrast = MatrixUtils.getKroneckerProduct(contrast,
                                 MatrixUtils.getRealMatrixWithFilledValue(1, dimension, 1/(double) dimension));
                     }
                 }
             }
             return contrast;
         } else {
             return ContrastHelper.grandMeanBetween(factorList);
         }
     }
 
     /**
      * Create a trend test fpr the specified within participant of interest
      * @param factorOfInterestMap factor of interest plus trend type information
      * @param factorList list of all within participant factors
      * @return trend contrast
      */
     public static RealMatrix trendWithin(HypothesisRepeatedMeasuresMapping factorOfInterestMap, 
             List<RepeatedMeasuresNode> factorList) {
 
         // build contrast component for the effect of interest 
         RepeatedMeasuresNode factorOfInterest = factorOfInterestMap.getRepeatedMeasuresNode();
         HypothesisTrendTypeEnum trendType = factorOfInterestMap.getType();
         int levels = factorOfInterest.getNumberOfMeasurements();
         if (levels > 1) {
             List<Spacing> spacingList = factorOfInterest.getSpacingList();
             double[] spacingArray = new double[spacingList.size()];
             for(int i = 0; i < spacingList.size(); i++) { spacingArray[i] = spacingList.get(i).getValue(); }
             RealMatrix trendContrast = getTrendContrast(spacingArray, trendType, true);
 
             // horizontal direct product the trend contrast with average contrasts for remaining
             // factors
             RealMatrix contrast = MatrixUtils.getRealMatrixWithFilledValue(1, 1, 1);
             int df = trendContrast.getRowDimension();
             for(RepeatedMeasuresNode factor: factorList) {
                 if (factor.getDimension().equals(factorOfInterest.getDimension())) {
                     contrast = MatrixUtils.getKroneckerProduct(contrast, trendContrast);
                 } else {
                     int size = factor.getNumberOfMeasurements();
                     if (size > 0) {
                         contrast = MatrixUtils.getKroneckerProduct(contrast,
                                 MatrixUtils.getRealMatrixWithFilledValue(1, size, 1/(double) size));
                     }
                 }
             }
             return contrast;
         } else {
             return ContrastHelper.grandMeanWithin(factorList);
         }
     }
     
     
     /**
      * Calculate the grand mean contrast
      * @param dimension size of contrast
      * @param transpose if true, return the transpose
      * @return grand mean contrast
      */
     public static RealMatrix grandMean(int dimension, boolean transpose) {
         if (transpose) {
             return MatrixUtils.getRealMatrixWithFilledValue(1, dimension, 1/(double) dimension).transpose();
         } else {
             return MatrixUtils.getRealMatrixWithFilledValue(1, dimension, 1/(double) dimension);
         }
     }
 
     /**
      * Create grand mean contrast for between participant effects
      * @param factorList list of all between participant effects
      * @return grand mean contrast
      */
     public static RealMatrix grandMeanBetween(List<BetweenParticipantFactor> factorList) {
         // computes the grand mean across the factors
         int dimension = 1;
         for(BetweenParticipantFactor factor: factorList) {
             List<Category> categoryList = factor.getCategoryList();
             if (categoryList.size() > 0) {
                 dimension *= categoryList.size();
             }
         }
         return MatrixUtils.getRealMatrixWithFilledValue(1, dimension, 1/(double) dimension);
     }
 
     /**
      * Create grand mean contrast for within  participant effects
      * @param factorList list of all within participant effects
      * @return grand mean contrast
      */
     public static RealMatrix grandMeanWithin(List<RepeatedMeasuresNode> factorList) {
         // computes the grand mean across the factors
         int dimension = 1;
         if (factorList != null) {
             for(RepeatedMeasuresNode factor: factorList) {
                 int size = factor.getNumberOfMeasurements();
                 if (size > 0) {
                     dimension *= size;
                 }
             }
         }
         return MatrixUtils.getRealMatrixWithFilledValue(dimension, 1, 1/(double) dimension);
     }
 
     /**
      * Create a trend contrast of the specified type
      * @param spacing list of integer positions representing spacing of measurements
      * @param trendType type of trend contrast
      * @param transpose if true, return the transpose of the contrast
      * @return trend contrast
      */
     private static RealMatrix getTrendContrast(double[] spacing, HypothesisTrendTypeEnum trendType, 
             boolean transpose) {
         int levels = spacing.length;
         // get all possible polynomial trends
         RealMatrix allTrendContrast = 
             OrthogonalPolynomials.orthogonalPolynomialCoefficients(spacing, 
                    Math.min(3,levels-1));
         // select a subset of the polynomial trends based on the hypothesis of interest
         RealMatrix trendContrast = null;
         switch (trendType) {
         case NONE:
             RealMatrix negIdentity = 
                 org.apache.commons.math.linear.MatrixUtils.createRealIdentityMatrix(levels-1).scalarMultiply(-1);
             RealMatrix column1s = MatrixUtils.getRealMatrixWithFilledValue(levels-1, 1, 1);
             trendContrast = MatrixUtils.getHorizontalAppend(column1s, negIdentity).transpose();
             break;
         case CHANGE_FROM_BASELINE:
             trendContrast = MatrixUtils.getRealMatrixWithFilledValue(levels, 1, 0);
             trendContrast.setEntry(0, 0, -1);
             trendContrast.setEntry(levels-1, 0, -1);
             break;
         case ALL_POYNOMIAL:
             trendContrast = allTrendContrast;
             break;
         case LINEAR:
             if (allTrendContrast.getRowDimension() > 1) {
                 trendContrast = allTrendContrast.getColumnMatrix(1);
             }
             break;
         case QUADRATIC:
             if (allTrendContrast.getRowDimension() > 2) {
                 trendContrast = allTrendContrast.getColumnMatrix(2);
             }
             break;
         case CUBIC:
             if (allTrendContrast.getRowDimension() > 3) {
                 trendContrast = allTrendContrast.getColumnMatrix(3);
             }
             break;
         }
         if (transpose) trendContrast = trendContrast.transpose();
         return trendContrast;
     }
 
     /**
      * Determine if the list of factors being tested contains the specified factor
      * @param factor
      * @param testFactorList
      * @return
      */
     private static HypothesisBetweenParticipantMapping betweenFactorInTestList(BetweenParticipantFactor factor, 
             List<HypothesisBetweenParticipantMapping> testFactorList) {
         for(HypothesisBetweenParticipantMapping testFactor: testFactorList) {
             if (factor.getPredictorName().equals(testFactor.getBetweenParticipantFactor().getPredictorName())) {
                 return testFactor;
             }
         }
         return null;
     }
 
     /**
      * Determine if the list of factors being tested contains the specified factor
      * @param factor
      * @param testFactorList
      * @return
      */
     private static HypothesisRepeatedMeasuresMapping withinFactorInTestList(RepeatedMeasuresNode factor, 
             List<HypothesisRepeatedMeasuresMapping> testFactorList) {
         for(HypothesisRepeatedMeasuresMapping testFactor: testFactorList) {
             if (factor.getDimension().equals(testFactor.getRepeatedMeasuresNode().getDimension())) {
                 return testFactor;
             }
         }
         return null;
     }
 
 }
