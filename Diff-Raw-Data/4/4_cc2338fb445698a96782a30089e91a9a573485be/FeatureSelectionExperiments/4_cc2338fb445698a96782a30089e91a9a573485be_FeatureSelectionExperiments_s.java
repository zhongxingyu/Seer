 /**
  * 
  */
 package mlfeatsel.experiments;
 
 import mlfeatsel.AbstractPerLabelAttributeEvaluator;
 import mlfeatsel.FeatureSelector;
 import mlfeatsel.IFeatureEvaluator;
 import mlfeatsel.PerLabelInstancePresenter;
 import mlfeatsel.experiments.PerformanceExplorator.ConcreteFeatureSelector;
 import mlfeatsel.featureevaluators.ChiSqFeatureEvaluator;
 import mlfeatsel.perlabelevaluators.MaxPerLabelAttributeEvaluator;
 import mulan.classifier.MultiLabelLearnerBase;
 import mulan.classifier.lazy.MLkNN;
 import mulan.classifier.meta.RAkEL;
 import mulan.classifier.transformation.BinaryRelevance;
 import mulan.classifier.transformation.LabelPowerset;
 import mulan.data.InvalidDataFormatException;
 import mulan.data.MultiLabelInstances;
 import mulan.evaluation.Evaluator;
 import mulan.evaluation.MultipleEvaluation;
 import weka.classifiers.functions.SMO;
 import weka.classifiers.trees.J48;
 
 /**
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class FeatureSelectionExperiments {
 
 	/**
 	 * 
 	 * @param evaluations
 	 * @return a double[][] containing the results
 	 */
 	public static String[][] evaluationsToArray(
 			MultipleEvaluation[] evaluations, String[] prepend) {
 		String[][] evals = new String[evaluations.length][prepend.length + 5];
 
 		for (int i = 0; i < evaluations.length; i++) {
 			int j = 0;
 			for (j = 0; j < prepend.length; j++)
 				evals[i][j] = prepend[j];
 			evals[i][j++] = Double.toString(PerformanceExplorator.EVERY_10[i]);
 			evals[i][j++] = Double.toString(evaluations[i]
 					.getMean("Example-Based Accuracy"));
 			evals[i][j++] = Double.toString(evaluations[i]
 					.getMean("Hamming Loss"));
 			evals[i][j++] = Double.toString(evaluations[i]
 					.getMean("Subset Accuracy"));
 			evals[i][j++] = Double.toString(evaluations[i]
 					.getMean("Example-Based Precision"));
 		}
 
 		return evals;
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		FeatureSelectionExperiments experiment;
 		if (args.length == 1)
 			experiment = new FeatureSelectionExperiments(args[0]);
 		else
 			experiment = new FeatureSelectionExperiments(args[0],
 					args[1].equals("test"));
 
 		String csv = "";
 		// Current time
 		long start = 0;
 		// Elapsed time in milliseconds
 		long elapsedTimeMillis = 0;
 
		for (int i = 0; i < 10; i++) {
 			start = System.currentTimeMillis();
 			MultipleEvaluation[] eval = experiment.perLabelFeatureSelection(
 					new RAkEL(new LabelPowerset(new J48())),
 					new ChiSqFeatureEvaluator(i));
 			elapsedTimeMillis = System.currentTimeMillis() - start;
 			String[] pr1 = { "RAkEL-J48", Integer.toString(i),
 					Long.toString(elapsedTimeMillis) };
 			String[][] evalArray = evaluationsToArray(eval, pr1);
 			csv += stringToCsv(evalArray);
 
 			start = System.currentTimeMillis();
 			eval = experiment
 					.perLabelFeatureSelection(new RAkEL(new LabelPowerset(
 							new SMO())), new ChiSqFeatureEvaluator(i));
 			elapsedTimeMillis = System.currentTimeMillis() - start;
 			String[] pr2 = { "RAkEL-SMO", Integer.toString(i),
 					Long.toString(elapsedTimeMillis) };
 			evalArray = evaluationsToArray(eval, pr2);
 			csv += stringToCsv(evalArray);
 
 			start = System.currentTimeMillis();
 			eval = experiment.perLabelFeatureSelection(new MLkNN(),
 					new ChiSqFeatureEvaluator(i));
 			elapsedTimeMillis = System.currentTimeMillis() - start;
 			String[] pr3 = { "MlKnn", Integer.toString(i),
 					Long.toString(elapsedTimeMillis) };
 			evalArray = evaluationsToArray(eval, pr3);
 			csv += stringToCsv(evalArray);
 
 			start = System.currentTimeMillis();
 			eval = experiment.perLabelFeatureSelection(new BinaryRelevance(
 					new J48()), new ChiSqFeatureEvaluator(i));
 			elapsedTimeMillis = System.currentTimeMillis() - start;
 			String[] pr4 = { "BRJ48", Integer.toString(i),
 					Long.toString(elapsedTimeMillis) };
 			evalArray = evaluationsToArray(eval, pr4);
 			csv += stringToCsv(evalArray);
 		}
 
 		System.out.println(csv);
 	}
 
 	/**
 	 * Convert a string[][] to csv representation.
 	 * 
 	 * @param anArray
 	 * @return the csv string representation
 	 */
 	public static String stringToCsv(String[][] anArray) {
 		String csv = "";
 		for (int i = 0; i < anArray.length; i++) {
 			for (int j = 0; j < anArray[i].length; j++) {
 				csv += anArray[i][j] + ",";
 			}
 			csv = csv.substring(0, csv.length() - 2);
 			csv += "\n";
 		}
 		return csv;
 	}
 
 	private final String arffFilename;
 
 	private final String xmlFilename;
 
 	private final String testFilename;
 
 	private MultiLabelInstances testSet;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param datasetPrefix
 	 */
 	public FeatureSelectionExperiments(final String datasetPrefix) {
 		arffFilename = datasetPrefix + ".arff";
 		xmlFilename = datasetPrefix + ".xml";
 		testFilename = "";
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param datasetPrefix
 	 * @param hasTestset
 	 */
 	public FeatureSelectionExperiments(String datasetPrefix, boolean hasTestset) {
 		xmlFilename = datasetPrefix + ".xml";
 		if (!hasTestset) {
 			testFilename = "";
 			arffFilename = datasetPrefix + ".arff";
 		} else {
 			testFilename = datasetPrefix + "-test.arff";
 			arffFilename = datasetPrefix + "-train.arff";
 			try {
 				testSet = new MultiLabelInstances(testFilename, xmlFilename);
 			} catch (InvalidDataFormatException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param learner
 	 * @param featEval
 	 * @return
 	 * @throws Exception
 	 */
 	public MultipleEvaluation[] perLabelFeatureSelection(
 			final MultiLabelLearnerBase learner,
 			final IFeatureEvaluator featEval) throws Exception {
 
 		final PerLabelInstancePresenter lblPresenter = new PerLabelInstancePresenter(
 				arffFilename, xmlFilename);
 
 		AbstractPerLabelAttributeEvaluator attributeEvaluator = new MaxPerLabelAttributeEvaluator(
 				lblPresenter, featEval);
 		FeatureSelector selector = new FeatureSelector(
 				lblPresenter.getDataset(), attributeEvaluator);
 
 		PerformanceExplorator expl = new PerformanceExplorator(selector);
 		expl.setClassifierEvaluator(expl.new ClassifierEvaluation() {
 
 			@Override
 			public MultipleEvaluation getEvaluationFor(
 					ConcreteFeatureSelector selector) {
 				Evaluator eval = new Evaluator();
 				try {
 					if (testFilename == "") {
 						return eval.crossValidate(learner, selector
 								.featureSelect(lblPresenter.getDataset()), 10);
 					}
 					learner.build(selector.featureSelect(lblPresenter
 							.getDataset()));
 
 					MultipleEvaluation result = new MultipleEvaluation();
 					result.addEvaluation(eval.evaluate(learner,
 							selector.featureSelect(testSet)));
 					return result;
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return null;
 
 			}
 		});
 		return expl.explorePerformanceFor(PerformanceExplorator.EVERY_10);
 	}
 }
