 /**
  * 
  */
 package gr.auth.ee.lcs.classifiers.statistics;
 
 import gr.auth.ee.lcs.AbstractLearningClassifierSystem;
 import gr.auth.ee.lcs.classifiers.Classifier;
 import gr.auth.ee.lcs.classifiers.ClassifierSet;
 import gr.auth.ee.lcs.data.ClassifierTransformBridge;
 import gr.auth.ee.lcs.data.ILCSMetric;
 
 /**
  * Calculate the mean attribute specificity statistic.
  * 
  * @author Miltiadis Allamanis
  * 
  */
 public class MeanAttributeSpecificityStatistic implements ILCSMetric {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.data.ILCSMetric#getMetric(gr.auth.ee.lcs.
 	 * AbstractLearningClassifierSystem)
 	 */
 	@Override
 	public double getMetric(AbstractLearningClassifierSystem lcs) {
 		final ClassifierTransformBridge bridge = lcs
 				.getClassifierTransformBridge();
 		final int numberOfAttributes = bridge.getNumberOfAttributes();
 		final ClassifierSet set = lcs.getRulePopulation();
 		final int numberOfMacroclassifiers = set.getNumberOfMacroclassifiers();
 
 		int specificAttibutes = 0;
 		for (int i = 0; i < numberOfMacroclassifiers; i++) {
 			final Classifier cl = set.getClassifier(i);
 			final int numerosity = set.getClassifierNumerosity(i);
 			for (int j = 0; j < numberOfAttributes; j++) {
 				if (bridge.isAttributeSpecific(cl, j)) {
 					specificAttibutes += numerosity;
 				}
 			}
 		}
 
		return specificAttibutes
				/ (numberOfMacroclassifiers * numberOfAttributes);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see gr.auth.ee.lcs.data.ILCSMetric#getMetricName()
 	 */
 	@Override
 	public String getMetricName() {
 		return "Mean Attribute Specificity";
 	}
 
 }
