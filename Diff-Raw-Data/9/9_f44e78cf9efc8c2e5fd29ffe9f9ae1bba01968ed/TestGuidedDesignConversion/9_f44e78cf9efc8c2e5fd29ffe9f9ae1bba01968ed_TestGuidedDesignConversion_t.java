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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package edu.ucdenver.bios.powersvc.resource.test;
 
 import java.util.ArrayList;
 
 import org.apache.commons.math.linear.MatrixUtils;
 
 import edu.ucdenver.bios.powersvc.application.PowerConstants;
 import edu.ucdenver.bios.powersvc.resource.PowerResourceHelper;
 import edu.ucdenver.bios.webservice.common.domain.BetaScale;
 import edu.ucdenver.bios.webservice.common.domain.BetweenParticipantFactor;
 import edu.ucdenver.bios.webservice.common.domain.Category;
 import edu.ucdenver.bios.webservice.common.domain.Covariance;
 import edu.ucdenver.bios.webservice.common.domain.Hypothesis;
 import edu.ucdenver.bios.webservice.common.domain.HypothesisBetweenParticipantMapping;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrix;
 import edu.ucdenver.bios.webservice.common.domain.NamedMatrixList;
 import edu.ucdenver.bios.webservice.common.domain.NominalPower;
 import edu.ucdenver.bios.webservice.common.domain.ResponseNode;
 import edu.ucdenver.bios.webservice.common.domain.SampleSize;
 import edu.ucdenver.bios.webservice.common.domain.SigmaScale;
 import edu.ucdenver.bios.webservice.common.domain.StandardDeviation;
 import edu.ucdenver.bios.webservice.common.domain.StatisticalTest;
 import edu.ucdenver.bios.webservice.common.domain.StudyDesign;
 import edu.ucdenver.bios.webservice.common.domain.TypeIError;
 import edu.ucdenver.bios.webservice.common.enums.CovarianceTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.HypothesisTrendTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.HypothesisTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.SolutionTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StatisticalTestTypeEnum;
 import edu.ucdenver.bios.webservice.common.enums.StudyDesignViewTypeEnum;
 import junit.framework.TestCase;
 
 public class TestGuidedDesignConversion extends TestCase {
 
     
     public void testMatrixDesign() {
         StudyDesign design = buildUnivariateMatrixDesign(SolutionTypeEnum.POWER);
         
         NamedMatrixList matrixList = PowerResourceHelper.namedMatrixListFromStudyDesign(design);
         
         for(NamedMatrix matrix: matrixList) {
             printNamedMatrix(matrix);
         }
     }
 
     public void testUnviariateGuidedDesign() {
         StudyDesign design = buildUnivariateGuidedDesign();
         
         NamedMatrixList matrixList = PowerResourceHelper.namedMatrixListFromStudyDesign(design);
         
         for(NamedMatrix matrix: matrixList) {
             printNamedMatrix(matrix);
         }
     }
     
     private StudyDesign buildUnivariateMatrixDesign(SolutionTypeEnum solvingFor)
     {
         StudyDesign studyDesign = new StudyDesign();
         studyDesign.setViewTypeEnum(StudyDesignViewTypeEnum.MATRIX_MODE);
         studyDesign.setSolutionTypeEnum(solvingFor);
         studyDesign.setName("Two Sample T-Test");
 
         if (solvingFor == SolutionTypeEnum.POWER
                 || solvingFor == SolutionTypeEnum.SAMPLE_SIZE) {
             // add beta scale values
             ArrayList<BetaScale> betaScaleList = new ArrayList<BetaScale>();
             betaScaleList.add(new BetaScale(0.5));
             //            betaScaleList.add(new BetaScale(1.0));
             //            betaScaleList.add(new BetaScale(2.0));
             studyDesign.setBetaScaleList(betaScaleList);
         }
 
         if (solvingFor == SolutionTypeEnum.POWER
                 || solvingFor == SolutionTypeEnum.DETECTABLE_DIFFERENCE) {
             // add per group sample sizes
             ArrayList<SampleSize> sampleSizeList = new ArrayList<SampleSize>();
             sampleSizeList.add(new SampleSize(10));
             sampleSizeList.add(new SampleSize(20));
             sampleSizeList.add(new SampleSize(40));
             studyDesign.setSampleSizeList(sampleSizeList);
         }
 
         if (solvingFor == SolutionTypeEnum.SAMPLE_SIZE
                 || solvingFor == SolutionTypeEnum.DETECTABLE_DIFFERENCE) {
             // add nominal power values
             ArrayList<NominalPower> nominalPowerList = new ArrayList<NominalPower>();
             nominalPowerList.add(new NominalPower(0.8));
             nominalPowerList.add(new NominalPower(0.9));
             nominalPowerList.add(new NominalPower(0.975));
             studyDesign.setNominalPowerList(nominalPowerList);
         }
 
         // add a test 
         ArrayList<StatisticalTest> testList = new ArrayList<StatisticalTest>();
         testList.add(new StatisticalTest(StatisticalTestTypeEnum.UNIREP));
         studyDesign.setStatisticalTestList(testList);
 
         // add alpha values
         ArrayList<TypeIError> alphaList = new ArrayList<TypeIError>();
         alphaList.add(new TypeIError(0.05));
         //        alphaList.add(new TypeIError(0.01));
         studyDesign.setAlphaList(alphaList);
 
         // add sigma scale values
         ArrayList<SigmaScale> sigmaScaleList = new ArrayList<SigmaScale>();
         //        sigmaScaleList.add(new SigmaScale(0.5));
         //        sigmaScaleList.add(new SigmaScale(1.0));
         sigmaScaleList.add(new SigmaScale(2.0));
         studyDesign.setSigmaScaleList(sigmaScaleList);
 
         // build the design eseence matrix
         studyDesign.setNamedMatrix(
                 PowerResourceHelper.toNamedMatrix(
                         MatrixUtils.createRealIdentityMatrix(2),
                         PowerConstants.MATRIX_DESIGN));
 
         // build between subject contrast
         double [][] betweenData = {{1,-1}};
         NamedMatrix betweenContrast = new NamedMatrix(PowerConstants.MATRIX_BETWEEN_CONTRAST);
         betweenContrast.setDataFromArray(betweenData);
         betweenContrast.setRows(1);
         betweenContrast.setColumns(2);
         studyDesign.setNamedMatrix(betweenContrast);
 
         // build beta matrix
         double [][] betaData = {{0},{1}};
         NamedMatrix beta = new NamedMatrix(PowerConstants.MATRIX_BETA);
         beta.setDataFromArray(betaData);
         beta.setRows(2);
         beta.setColumns(1);
         studyDesign.setNamedMatrix(beta);
 
         // build theta null matrix
         double [][] thetaNullData = {{0}};
         NamedMatrix thetaNull = new NamedMatrix(PowerConstants.MATRIX_THETA_NULL);
         thetaNull.setDataFromArray(thetaNullData);
         thetaNull.setRows(1);
         thetaNull.setColumns(1);
         studyDesign.setNamedMatrix(thetaNull);
 
         // build sigma matrix
         double [][] sigmaData = {{1}};
         NamedMatrix sigmaError = new NamedMatrix(PowerConstants.MATRIX_SIGMA_ERROR);
         sigmaError.setDataFromArray(sigmaData);
         sigmaError.setRows(1);
         sigmaError.setColumns(1);
         studyDesign.setNamedMatrix(sigmaError);
         
         return studyDesign;
     }
     
     
     private StudyDesign buildUnivariateGuidedDesign()
     {
         StudyDesign studyDesign = new StudyDesign();
         studyDesign.setViewTypeEnum(StudyDesignViewTypeEnum.GUIDED_MODE);
         studyDesign.setSolutionTypeEnum(SolutionTypeEnum.POWER);
         studyDesign.setName("Three factor design");
 
         ArrayList<BetaScale> betaScaleList = new ArrayList<BetaScale>();
         betaScaleList.add(new BetaScale(0.5));
         studyDesign.setBetaScaleList(betaScaleList);
 
         ArrayList<SampleSize> sampleSizeList = new ArrayList<SampleSize>();
         sampleSizeList.add(new SampleSize(10));
         studyDesign.setSampleSizeList(sampleSizeList);
 
         // add a test 
         ArrayList<StatisticalTest> testList = new ArrayList<StatisticalTest>();
         testList.add(new StatisticalTest(StatisticalTestTypeEnum.UNIREP));
         studyDesign.setStatisticalTestList(testList);
 
         // add alpha values
         ArrayList<TypeIError> alphaList = new ArrayList<TypeIError>();
         alphaList.add(new TypeIError(0.05));
         studyDesign.setAlphaList(alphaList);
 
         // add sigma scale values
         ArrayList<SigmaScale> sigmaScaleList = new ArrayList<SigmaScale>();
         sigmaScaleList.add(new SigmaScale(2.0));
         studyDesign.setSigmaScaleList(sigmaScaleList);
 
         // add between subject factors
         ArrayList<BetweenParticipantFactor> factorList = new ArrayList<BetweenParticipantFactor>();
         BetweenParticipantFactor x1 = new BetweenParticipantFactor();
         x1.setPredictorName("treatment");
         ArrayList<Category> x1Cat = new ArrayList<Category>();
         x1Cat.add(new Category("nifedipine"));
         x1Cat.add(new Category("carvedilol"));
         x1Cat.add(new Category("placebo"));
         x1.setCategoryList(x1Cat);
 
         BetweenParticipantFactor x2 = new BetweenParticipantFactor();
         x2.setPredictorName("sex");
         ArrayList<Category> x2Cat = new ArrayList<Category>();
         x2Cat.add(new Category("M"));
         x2Cat.add(new Category("F"));
         x2.setCategoryList(x2Cat);
         
         BetweenParticipantFactor x3 = new BetweenParticipantFactor();
         x3.setPredictorName("activity level");
         ArrayList<Category> x3Cat = new ArrayList<Category>();
         x3Cat.add(new Category("Sedentary"));
         x3Cat.add(new Category("Low Active"));
         x3Cat.add(new Category("Medium Active"));
         x3Cat.add(new Category("High Active"));
         x3.setCategoryList(x3Cat);
         
         factorList.add(x1);
 //        factorList.add(x2);
 //        factorList.add(x3);
         studyDesign.setBetweenParticipantFactorList(factorList);
         
         // build the hypotheses
         Hypothesis hypothesis = new Hypothesis();
 //        hypothesis.setType(HypothesisTypeEnum.MAIN_EFFECT);
         hypothesis.setType(HypothesisTypeEnum.TREND);
         ArrayList<HypothesisBetweenParticipantMapping> map = new ArrayList<HypothesisBetweenParticipantMapping>();
         HypothesisBetweenParticipantMapping x1Map = new HypothesisBetweenParticipantMapping();
        x1Map.setType(HypothesisTrendTypeEnum.ALL_POLYNOMIAL);
         x1Map.setBetweenParticipantFactor(x1);
         map.add(x1Map);
 
         HypothesisBetweenParticipantMapping x2Map = new HypothesisBetweenParticipantMapping();
         x2Map.setType(HypothesisTrendTypeEnum.CHANGE_FROM_BASELINE);
         x2Map.setBetweenParticipantFactor(x2);
 //        map.add(x2Map);
         
         hypothesis.setBetweenParticipantFactorMapList(map);
         studyDesign.setHypothesisToSet(hypothesis);
 
         // build beta matrix
         double [][] betaData = {
                 {0},{0},{0},{0},{0},{0},
                 {0},{0},{0},{0},{0},{0},
                 {0},{0},{0},{0},{0},{0},
                 {0},{0},{0},{0},{0},{1}};
         NamedMatrix beta = new NamedMatrix(PowerConstants.MATRIX_BETA);
         beta.setDataFromArray(betaData);
         beta.setRows(24);
         beta.setColumns(1);
         studyDesign.setNamedMatrix(beta);
 
         // build response variables list
         ArrayList<ResponseNode> responseList = new ArrayList<ResponseNode>();
         responseList.add(new ResponseNode("outcome"));
         studyDesign.setResponseList(responseList);
         
         // build covariance
         Covariance covar = new Covariance();
         covar.setType(CovarianceTypeEnum.UNSTRUCTURED_CORRELATION);
         covar.setName(PowerConstants.RESPONSES_COVARIANCE_LABEL);
         ArrayList<StandardDeviation> stdDevList = new ArrayList<StandardDeviation>();
         stdDevList.add(new StandardDeviation(2));
         double [][] sigmaData = {{1}};
         covar.setRows(1);
         covar.setColumns(1);
         covar.setBlobFromArray(sigmaData);
         covar.setStandardDeviationList(stdDevList);
         studyDesign.addCovariance(covar);
         return studyDesign;
     }
     
     private void printNamedMatrix(NamedMatrix matrix) {
         int rows = matrix.getRows();
         int columns = matrix.getColumns();
         double[][] data = matrix.getData().getData();
         System.out.println("---------------------");
         System.out.println(matrix.getName() + " (" + rows + " x " + columns + ")");
         for(int r = 0; r < rows; r++) {
             for(int c = 0; c < columns; c++) {
                 System.out.print(data[r][c] + ", ");
             }
             System.out.print("\n");
         }
         System.out.print("---------------------");
     }
 }
