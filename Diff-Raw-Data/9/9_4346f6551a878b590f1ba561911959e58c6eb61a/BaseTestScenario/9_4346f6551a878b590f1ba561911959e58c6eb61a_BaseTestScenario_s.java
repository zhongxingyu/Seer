 package test.java.integration.tests.gal;
 
 import com.datascience.core.base.AssignedLabel;
 import com.datascience.core.base.Category;
 import com.datascience.core.base.LObject;
 import com.datascience.core.base.Worker;
 import com.datascience.core.nominal.NominalAlgorithm;
 import com.datascience.core.nominal.NominalData;
 import com.datascience.core.nominal.NominalProject;
 import com.datascience.core.nominal.decision.*;
 import com.datascience.core.results.WorkerResult;
 import com.datascience.gal.AbstractDawidSkene;
 import com.datascience.gal.MisclassificationCost;
 import com.datascience.gal.Quality;
 import com.datascience.gal.evaluation.DataEvaluator;
 import com.datascience.gal.evaluation.WorkerEvaluator;
 import test.java.integration.helpers.FileWriters;
 import test.java.integration.helpers.SummaryResultsParser;
 import test.java.integration.helpers.TestHelpers;
 import test.java.integration.helpers.TestSettings;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 
 public class BaseTestScenario {
 
 	public final static String DATA_BASE_DIR = TestSettings.GAL_TESTDATA_BASEDIR;
 
 	protected String inputDir;
 	protected String outputDir;
 
 	protected TestHelpers testHelper = new TestHelpers();
 	protected FileWriters fileWriter;
 	protected SummaryResultsParser summaryResultsParser;
 
 	protected NominalProject project;
 	protected NominalData data;
 
 	public static interface IDataLoader {
 
 		void load(BaseTestScenario test);
 	}
 
 	public void setUp(NominalAlgorithm algorithm, String testName, IDataLoader dataLoader) {
 		project = new NominalProject(algorithm);
 		data = project.getData();
 		inputDir = DATA_BASE_DIR + testName + TestSettings.FILEPATH_SEPARATOR + "input" + TestSettings.FILEPATH_SEPARATOR;
 		outputDir = DATA_BASE_DIR + testName + TestSettings.FILEPATH_SEPARATOR + "output" + TestSettings.FILEPATH_SEPARATOR;
 		fileWriter = new FileWriters(outputDir + "Results_" + testName + ".csv");
 		summaryResultsParser = new SummaryResultsParser(outputDir + "summary.txt");
 		dataLoader.load(this);
 		project.getAlgorithm().compute();
 		fileWriter.write("Metric,GAL value,Troia value");
 	}
 
 	public void loadData() {
 		loadCategories();
 		loadAssignedLabels();
 		loadGoldLabels();
 		loadEvaluationLabels();
 		loadCosts();
 	}
 
 	public void loadCategories() {
 		Collection<Category> categories = testHelper.LoadCategories(inputDir + "categories.txt");
 		project.initializeCategories(categories);
 	}
 
 	public void loadGoldLabels() {
 		Collection<LObject<String>> goldLabels = testHelper.LoadGoldLabels(inputDir + "correct.txt");
 		for (LObject<String> goldLabel : goldLabels) {
 			LObject<String> object = data.getOrCreateObject(goldLabel.getName());
 			object.setGoldLabel(goldLabel.getGoldLabel());
 			data.addObject(object);
 		}
 	}
 
 	public void loadEvaluationLabels() {
 		Collection<LObject<String>> evaluationLabels = testHelper.LoadEvaluationLabels(inputDir + "evaluation.txt");
 		for (LObject<String> evaluationLabel : evaluationLabels) {
 			LObject<String> object = data.getOrCreateObject(evaluationLabel.getName());
 			object.setEvaluationLabel(evaluationLabel.getEvaluationLabel());
 			data.addObject(object);
 		}
 	}
 
 	public void loadAssignedLabels() {
 		Collection<AssignedLabel<String>> assignedLabels = testHelper.LoadWorkerAssignedLabels(inputDir + "input.txt");
 		for (AssignedLabel<String> assign : assignedLabels) {
			data.addAssign(assign);
		}
		for (AssignedLabel<String> assign : data.getAssigns()) {
 			Worker<String> worker = data.getOrCreateWorker(assign.getWorker().getName());
 			assign.setWorker(worker);
			worker.addAssign(assign);
 			LObject<String> object = data.getOrCreateObject(assign.getLobject().getName());
 			assign.setLobject(object);
 		}
 	}
 
 	public void loadCosts() {
 		Set<MisclassificationCost> costs = testHelper.LoadMisclassificationCosts(inputDir + "costs.txt");
 		// XXX only for DawidSkene algorithms.
 		AbstractDawidSkene algorithm = (AbstractDawidSkene) project.getAlgorithm();
 		algorithm.addMisclassificationCosts(costs);
 	}
 
 	public void setFileWriter(FileWriters fileWriter) {
 		this.fileWriter = fileWriter;
 	}
 
 	public void setSummaryResultsParser(SummaryResultsParser summaryResultsParser) {
 		this.summaryResultsParser = summaryResultsParser;
 	}
 
 	public double estimateMissclassificationCost(
 			ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator,
 			IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) {
 		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCostCalculator,
 				objectLabelDecisionAlgorithm);
 		Set<LObject<String>> objects = data.getObjects();
 		double avgClassificationCost = 0.0;
 
 		//compute the estimated misclassification cost for each object, using DS
 		for (LObject<String> object : objects) {
 			avgClassificationCost += decisionEngine.estimateMissclassificationCost(project, object);
 		}
 
 		//calculate the average
 		avgClassificationCost = avgClassificationCost/objects.size();
 		return avgClassificationCost;
 	}
 
 	public double evaluateMissclassificationCost(String labelChoosingMethod) {
 		DataEvaluator dataEvaluator = new DataEvaluator(labelChoosingMethod);
 
 		//compute the evaluated misclassification cost
 		Map<String, Double> evaluated = dataEvaluator.evaluate(project);
 
 		double avgCost = 0.0;
 
 		for (Map.Entry<String, Double> entry : evaluated.entrySet()) {
 			avgCost += entry.getValue();
 		}
 
 		//calculate the average cost
 		avgCost = avgCost / data.getEvaluationObjects().size();
 		return avgCost;
 	}
 
 	public double estimateCostToQuality(
 			ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator,
 			IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) {
 		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCostCalculator,
 				objectLabelDecisionAlgorithm);
 		Map<String, Double> costQuality = Quality.fromCosts(project, decisionEngine.estimateMissclassificationCosts(project));
 
 		double avgQuality = 0.0;
 
 		//compute the estimated quality cost for each object, using MV
 		for (Map.Entry<String, Double> cQuality : costQuality.entrySet()) {
 			avgQuality += cQuality.getValue();
 		}
 
 		//calculate the average
 		avgQuality /= costQuality.size();
 		return avgQuality;
 	}
 
 	public double evaluateCostToQuality(String labelChoosingMethod) {
 		DataEvaluator dataEvaluator = new DataEvaluator(labelChoosingMethod);
 
 		Map <String, Double> costQuality = Quality.fromCosts(project, dataEvaluator.evaluate(project));
 		double avgQuality = 0.0;
 
 		//compute the estimated quality cost for each object, using MV
 		for (Map.Entry<String, Double> cQuality : costQuality.entrySet()) {
 			avgQuality += cQuality.getValue();
 		}
 
 		//calculate the average
 		avgQuality /= costQuality.size();
 		return avgQuality;
 	}
 
 	public double estimateWorkerQuality(String method, String estimationType) {
 		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator =
 				LabelProbabilityDistributionCostCalculators.get(method);
 		WorkerEstimator workerEstimator = new WorkerEstimator(labelProbabilityDistributionCostCalculator);
 		Map<String, Double> result = new HashMap<String, Double>();
 		Map<String, Integer> workerAssignedLabels = new HashMap<String, Integer>();
 
 		for (Map.Entry<Worker<String>, WorkerResult> w : project.getResults().getWorkerResults().entrySet()) {
 			Worker<String> worker = w.getKey();
 			result.put(worker.getName(), workerEstimator.getCost(project, worker));
 			workerAssignedLabels.put(worker.getName(), worker.getAssigns().size());
 		}
 
 		Map <String, Double> workersQuality = Quality.fromCosts(project, result);
 		double quality = 0.0;
 
 		if (estimationType.equals("n")) {
 			//compute the non weighted worker quality
 			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) {
 				quality += workerQuality.getValue();
 			}
 			//calculate the average
 			quality /= workersQuality.size();
 			return quality;
 		} else {
 			//compute the weighted worker quality
 			int totalNoLabels = 0;
 			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) {
 				quality += workerQuality.getValue() * workerAssignedLabels.get(workerQuality.getKey());
 				totalNoLabels += workerAssignedLabels.get(workerQuality.getKey());
 			}
 			quality /= totalNoLabels;
 			return quality;
 		}
 	}
 
 	public double evaluateWorkerQuality(String method, String estimationType) {
 		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator =
 				LabelProbabilityDistributionCostCalculators.get(method);
 		WorkerEvaluator workerEvaluator = new WorkerEvaluator(labelProbabilityDistributionCostCalculator);
 		Map<String, Double> result = new HashMap<String, Double>();
 		Map<String, Integer> workerAssignedLabels = new HashMap<String, Integer>();
 
 		for (Map.Entry<Worker<String>, WorkerResult> w : project.getResults().getWorkerResults().entrySet()) {
 			Worker<String> worker = w.getKey();
 			result.put(worker.getName(), workerEvaluator.getCost(project, worker));
 			workerAssignedLabels.put(worker.getName(), worker.getAssigns().size());
 		}
 
 		Map <String, Double> workersQuality = Quality.fromCosts(project, result);
 		double quality = 0.0;
 		double denominator = 0.;
 
 		if (estimationType.equals("n")) {
 			//compute the non-weighted worker quality
 			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) {
 				Double val = workerQuality.getValue();
 				if (val == null || val.isNaN())
 					continue;
 				quality += val;
 				denominator += 1.;
 			}
 			//calculate the average
 			quality /= denominator;
 			return quality;
 		} else {
 			//compute the weighted worker quality
 			int totalNoLabels = 0;
 			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) {
 				Double val = workerQuality.getValue();
 				if (val == null || val.isNaN())
 					continue;
 
 				quality += val * workerAssignedLabels.get(workerQuality.getKey());
 				totalNoLabels += workerAssignedLabels.get(workerQuality.getKey());
 			}
 			denominator += totalNoLabels;
 			quality /= denominator;
 			return quality;
 		}
 	}
 }
