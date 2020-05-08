 package test.java.gal.integration.tests;
 
 import static org.junit.Assert.assertEquals;
 
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import org.junit.Ignore;
 import org.junit.Test;
 import test.java.gal.integration.helpers.*;
 
 import com.datascience.gal.AbstractDawidSkene;
 import com.datascience.gal.CorrectLabel;
 import com.datascience.gal.Datum;
 import com.datascience.gal.decision.*;
 import com.datascience.gal.Quality;
 import com.datascience.gal.evaluation.DataEvaluator;
 
 public class BaseScenarios {
 
 	public static int NO_ITERATIONS = 50;
 	public static String SUMMARY_FILE;
 	public static String TEST_RESULTS_FILE;
 	public static AbstractDawidSkene ds;
 	public static FileWriters fileWriter;
 	public static TestHelpers testHelper;
 	public static SummaryResultsParser summaryResultsParser;
 	public static NumberFormat percentFormat;
 	
 	
 	public static class Setup{
 		public AbstractDawidSkene abstractDS;
 		public String summaryResultsFile;
 		public String testResultsFile;
 		
 		public Setup(AbstractDawidSkene ds, String summaryFile, String resultsFile) {
 			abstractDS = ds;
 			summaryResultsFile = summaryFile;
 			testResultsFile = resultsFile;
 		}		
 	}
 	
 	public static void initSetup(Setup testSetup){
 		ds = testSetup.abstractDS;
 		ds.estimate(NO_ITERATIONS);
 		SUMMARY_FILE = testSetup.summaryResultsFile;
 		TEST_RESULTS_FILE = testSetup.testResultsFile;
 		
 		testHelper = new TestHelpers();
 		percentFormat = NumberFormat.getPercentInstance();
 		percentFormat.setMinimumFractionDigits(2);
 		percentFormat.setMaximumFractionDigits(2);
 
 		//prepare the test results file
 		fileWriter = new FileWriters();
 		fileWriter.createNewFile(TEST_RESULTS_FILE);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "Metric,GAL value,Troia value");
 		
 		summaryResultsParser = new SummaryResultsParser();
 		summaryResultsParser.ParseSummaryFile(SUMMARY_FILE);
 	}
 	
 	public double estimateMissclassificationCost(AbstractDawidSkene ds, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator, ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator, IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) 
 	{
 		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, objectLabelDecisionAlgorithm);
 		Map<String, Datum> objects = ds.getObjects();
 		double avgClassificationCost = 0.0;
 		
 		//compute the estimated misclassification cost for each object, using DS
 		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
 			Datum datum = object.getValue();
 			avgClassificationCost += decisionEngine.estimateMissclassificationCost(ds, datum);
 		}
 		
 		//calculate the average
 		avgClassificationCost = avgClassificationCost/objects.size();
 		return avgClassificationCost;
 	}
 	
 	public double evaluateMissclassificationCost(AbstractDawidSkene ds, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator) 
 	{
 		DataEvaluator dataEvaluator = new DataEvaluator(labelProbabilityDistributionCalculator);
 		
		Map <String, CorrectLabel> goldLabels = ds.getEvaluationDatums();
 		double avgClassificationCost = 0.0;
 		
		//compute the evaluated misclassification cost for each gold label
		for ( Map.Entry<String, CorrectLabel> goldLabel : goldLabels.entrySet()) { 
			avgClassificationCost += dataEvaluator.evaluate(ds, goldLabel.getValue());
 		}
 		
 		//calculate the average
 		avgClassificationCost = avgClassificationCost/goldLabels.size();
 		return avgClassificationCost;
 	}
 	
 	
 	public double estimateCostToQuality(AbstractDawidSkene ds, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator, ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator, IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) 
 	{
 		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, objectLabelDecisionAlgorithm);
 		
 		Map<String, Datum> objects = ds.getObjects();
 		double avgQuality = 0.0;
 		
 		//compute the estimated quality cost for each object, using MV
 		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
 			Datum datum = object.getValue();
 			avgQuality += decisionEngine.costToQuality(ds, decisionEngine.estimateMissclassificationCost(ds, datum));
 		}
 		
 		//calculate the average
 		avgQuality /= objects.size();
 		return avgQuality;
 	}
 	
 	@Test
 	public void test_Data() {	
 		HashMap<String, String> data = summaryResultsParser.getData();
 
 		int expectedCategoriesNo = Integer.parseInt(data.get("Categories"));
 		int actualCategoriesNo = ds.getCategories().size();
 		assertEquals(expectedCategoriesNo, actualCategoriesNo);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "Categories," + expectedCategoriesNo + "," + actualCategoriesNo);
 				
 		int expectedObjectsNo = Integer.parseInt(data.get("Objects in Data Set"));
 		int actualObjectsNo = ds.getNumberOfObjects();	
 		assertEquals(expectedObjectsNo, actualObjectsNo);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "Objects in Data Set," + expectedObjectsNo + "," + actualObjectsNo);
 		
 		int expectedWorkersNo = Integer.parseInt(data.get("Workers in Data Set"));
 		int actualWorkersNo = ds.getNumberOfWorkers();	
 		assertEquals(expectedWorkersNo, actualWorkersNo);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "Workers in Data Set," + expectedWorkersNo + "," + actualWorkersNo);
 		
 		//get the labels
 		int noAssignedLabels = 0;
 		Map <String, Datum> objects = ds.getObjects();
 		for (Datum datum : objects.values() ){
 			noAssignedLabels +=	datum.getAssignedLabels().size();
 		}
 		
 		int expectedLabelsNo = Integer.parseInt(data.get("Labels Assigned by Workers"));
 		assertEquals(expectedLabelsNo, noAssignedLabels);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "Labels Assigned by Workers," + expectedLabelsNo + "," + noAssignedLabels);
 	}	
 	
 	@Test
 	public void test_ProbabilityDistributions_DS(){
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator probDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		DecisionEngine decisionEngine = new DecisionEngine(probDistributionCalculator, null, null);
 		Map <String, Datum> objects = ds.getObjects();
 		
 		//init the categoryProbabilities hashmap
 		HashMap <String, Double> categoryProbabilities = new HashMap<String, Double>();
 		for (String categoryName : ds.getCategories().keySet())
 			categoryProbabilities.put(categoryName, 0.0);
 		
 		//iterate through the datum objects and calculate the sum of the probabilities associated  to each category
 		int noObjects = objects.size();
 		for (Map.Entry<String, Datum> object : objects.entrySet())
 		{
 		    Datum datum = object.getValue();
 		    
 		    Map <String, Double> objectProbabilities = decisionEngine.getPD(datum, ds);
 		    for (String categoryName : objectProbabilities.keySet()){
 		    	categoryProbabilities.put(categoryName, (categoryProbabilities.get(categoryName) + objectProbabilities.get(categoryName)));    	
 		    }
 		}
 		
 		//calculate the average probability value for each category
 		for (String categoryName : ds.getCategories().keySet()){
 			categoryProbabilities.put(categoryName, categoryProbabilities.get(categoryName)/noObjects);
 		}
 		for (String categoryName : ds.getCategories().keySet()){
 			String metricName = "[DS_Pr[" + categoryName + "]] DS estimate for prior probability of category " + categoryName;
 			String expectedCategoryProbability = data.get(metricName);
 			String actualCategoryProbability = testHelper.format(categoryProbabilities.get(categoryName));
 			fileWriter.writeToFile(TEST_RESULTS_FILE, "[DS_Pr[" + categoryName + "]]," + expectedCategoryProbability + "," + actualCategoryProbability);
 			assertEquals(expectedCategoryProbability, actualCategoryProbability);
 		}	
 	}
 	
 	@Test
 	public void test_ProbabilityDistributions_MV(){
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator probDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		DecisionEngine decisionEngine = new DecisionEngine(probDistributionCalculator, null, null);
 		Map <String, Datum> objects = ds.getObjects();
 		
 		//init the categoryProbabilities hashmap
 		HashMap <String, Double> categoryProbabilities = new HashMap<String, Double>();
 		for (String categoryName : ds.getCategories().keySet())
 			categoryProbabilities.put(categoryName, 0.0);
 		
 		//iterate through the datum objects and calculate the sum of the probabilities associated  to each category
 		int noObjects = objects.size();
 		for (Map.Entry<String, Datum> object : objects.entrySet())
 		{
 		    Datum datum = object.getValue();
 		    
 		    Map <String, Double> objectProbabilities = decisionEngine.getPD(datum, ds);
 		    for (String categoryName : objectProbabilities.keySet()){
 		    	categoryProbabilities.put(categoryName, (categoryProbabilities.get(categoryName) + objectProbabilities.get(categoryName)));    	
 		    }
 		}
 		
 		//calculate the average probability value for each category
 		for (String categoryName : ds.getCategories().keySet()){
 			categoryProbabilities.put(categoryName, categoryProbabilities.get(categoryName)/noObjects);
 		}
 		
 		for (String categoryName : ds.getCategories().keySet()){
 			String metricName = "[MV_Pr[" + categoryName + "]] Majority Vote estimate for prior probability of category " + categoryName;
 			String expectedCategoryProbability = data.get(metricName);
 			String actualCategoryProbability = testHelper.format(categoryProbabilities.get(categoryName));
 			fileWriter.writeToFile(TEST_RESULTS_FILE, "[MV_Pr[" + categoryName + "]]," + expectedCategoryProbability + "," + actualCategoryProbability);
 			assertEquals(expectedCategoryProbability, actualCategoryProbability);
 		}	
 	}
 	
 	
 	@Test
 	public void test_DataCost_Estm_DS_Exp() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
 		
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataCost_Estm_DS_Exp] Estimated classification cost (DS_Exp metric)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	
 	@Test
 	public void test_DataCost_Estm_MV_Exp () {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
 
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 
 		String expectedClassificationCost = data.get("[DataCost_Estm_MV_Exp] Estimated classification cost (MV_Exp metric)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	public void test_DataCost_Estm_DS_ML() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
 		
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 
 		String expectedClassificationCost = data.get("[DataCost_Estm_DS_ML] Estimated classification cost (DS_ML metric)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 		
 	}	
 	
 	@Test
 	public void test_DataCost_Estm_MV_ML() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
 		
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 
 		String expectedClassificationCost = data.get("[DataCost_Estm_MV_ML] Estimated classification cost (MV_ML metric)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	
 	@Test
 	public void test_DataCost_Estm_DS_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
 		
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataCost_Estm_DS_Min] Estimated classification cost (DS_Min metric)");
 		String actualClassificationCost =  testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	public void test_DataCost_Estm_MV_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
 		
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataCost_Estm_MV_Min] Estimated classification cost (MV_Min metric)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}
 	
 	@Test
 	public void test_DataCost_Estm_NoVote_Exp() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("NOVOTE");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
 
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataCost_Estm_NoVote_Exp] Baseline classification cost (random spammer)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_NoVote_Exp," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	public void test_DataCost_Estm_NoVote_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("NOVOTE");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
 		
 		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataCost_Estm_NoVote_Min] Baseline classification cost (strategic spammer)");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_NoVote_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	public void test_DataCost_Eval_DS_ML() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm = ObjectLabelDecisionAlgorithms.get("MAXLIKELIHOOD");
 		
 		double avgClassificationCost = evaluateMissclassificationCost(ds, labelProbabilityDistributionCalculator, null, objectLabelDecisionAlgorithm);
 		
 		String expectedClassificationCost = data.get("[DataCost_Eval_DS_ML] Actual classification cost for EM, maximum likelihood classification");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	
 	@Test
 	public void test_DataCost_Eval_MV_ML() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm = ObjectLabelDecisionAlgorithms.get("MAXLIKELIHOOD");
 		
 		double avgClassificationCost = evaluateMissclassificationCost(ds, labelProbabilityDistributionCalculator, null, objectLabelDecisionAlgorithm);
 		
 		String expectedClassificationCost = data.get("[DataCost_Eval_MV_ML] Actual classification cost for majority vote classification");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	
 	@Test
 	public void test_DataCost_Eval_DS_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm = ObjectLabelDecisionAlgorithms.get("MINCOST");
 		
 		double avgClassificationCost = evaluateMissclassificationCost(ds, labelProbabilityDistributionCalculator, null, objectLabelDecisionAlgorithm);
 		
 		String expectedClassificationCost = data.get("[DataCost_Eval_DS_Min] Actual classification cost for EM, min-cost classification");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 
 	@Test
 	public void test_DataCost_Eval_MV_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm = ObjectLabelDecisionAlgorithms.get("MINCOST");
 		
 		double avgClassificationCost = evaluateMissclassificationCost(ds, labelProbabilityDistributionCalculator, null, objectLabelDecisionAlgorithm);
 		
 		String expectedClassificationCost = data.get("[DataCost_Eval_MV_Min] Actual classification cost for naive min-cost classification");
 		String actualClassificationCost = testHelper.format(avgClassificationCost);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	@Ignore
 	public void test_DataCost_Eval_DS_Soft() {	
 		
 	}	
 	
 	@Test
 	@Ignore
 	public void test_DataCost_Eval_MV_Soft() {	
 		
 	}	
 	
 	@Test
 	public void test_DataQuality_Estm_DS_ML() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
 		
 		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataQuality_Estm_DS_ML] Estimated data quality, EM algorithm, maximum likelihood");
 		String actualClassificationCost = percentFormat.format(avgQuality);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	public void test_DataQuality_Estm_MV_ML() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
 		
 		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataQuality_Estm_MV_ML] Estimated data quality, naive majority label");
 		String actualClassificationCost = percentFormat.format(avgQuality);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}	
 	
 	@Test
 	public void test_DataQuality_Estm_DS_Exp() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
 		
 		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataQuality_Estm_DS_Exp] Estimated data quality, EM algorithm, soft label");
 		String actualClassificationCost = percentFormat.format(avgQuality);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}
 	
 	@Test
 	public void test_DataQuality_Estm_MV_Exp() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
 		
 		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataQuality_Estm_MV_Exp] Estimated data quality, naive soft label");
 		String actualClassificationCost = percentFormat.format(avgQuality);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}
 	
 	@Test
 	public void test_DataQuality_Estm_DS_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
 		
 		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataQuality_Estm_DS_Min] Estimated data quality, EM algorithm, mincost");
 		String actualClassificationCost = percentFormat.format(avgQuality);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}
 	
 	@Test
 	public void test_DataQuality_Estm_MV_Min() {	
 		HashMap<String, String> data = summaryResultsParser.getDataQuality();
 		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
 		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
 		
 		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
 		
 		String expectedClassificationCost = data.get("[DataQuality_Estm_MV_Min] Estimated data quality, naive mincost label");
 		String actualClassificationCost = percentFormat.format(avgQuality);
 		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
 		assertEquals(expectedClassificationCost, actualClassificationCost);
 	}
 	
 }
