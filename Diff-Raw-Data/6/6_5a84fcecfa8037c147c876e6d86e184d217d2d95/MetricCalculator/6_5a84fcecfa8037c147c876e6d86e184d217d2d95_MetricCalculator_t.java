 package org.eclipse.emf.refactor.metrics.runtime.core;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.refactor.metrics.configuration.core.Configuration;
 import org.eclipse.emf.refactor.metrics.core.Metric;
 
 /**
  * This class provides a controller responsible for the metric calculation process.
  * This is a singleton class. The <i>getCalculator()</i> method returns the instance of his class.  
  * 
  * @author Pawel Stepien
  */
 public class MetricCalculator {
 
 	private static MetricCalculator calculator;
 	private Configuration configuration;
 	private List<EObject> context;
 	private LinkedList<Result> results;
 
 	/**
 	 * Returns the instance of this class.
 	 * 
 	 * @return metricCalculator
 	 */
 	public static MetricCalculator getInstance() {
 		if (calculator == null)
 			calculator = new MetricCalculator();
 		return calculator;
 	}
 
 	/**
 	 * Sets the metrics configuration with the metrics that shall be calculated.
 	 * 
 	 * @param context
 	 */
 	public void setConfiguration(Configuration configuration) {
 		this.configuration = configuration;
 	}
 
 	/**
 	 * Sets the context element on which the metrics will be calculated.
 	 * 
 	 * @param context
 	 */
 	public void setContext(List<EObject> context) {
 		this.context = context;
 	}
 
 	/**
 	 * Calculates the metrics from the configuration which are defined for the metamodel
 	 * and the context of the context element.
 	 */
 	public void calculateMetrics() {
 		LinkedList<Metric> metricsToCalculate = configuration.getSelectedMetrics();
 		results.clear();
 		for (Metric metric : metricsToCalculate) {
			if(metric.getContext().equals(context.get(0).eClass().getInstanceClass().getSimpleName())) { 
				System.out.println("Calculate metric '" + metric.getName() + "' on element '" + metric.getContext());
				results.add(calculateMetric(metric));
			}
 		}
 	}
 	
 	/**
 	 * Returns a <i>LinkedList</i> containing the results of the last metric calculation.
 	 * 
 	 * @return results
 	 */
 	public LinkedList<Result> getResults() {
 		return results;
 	}
 
 	private MetricCalculator() {
 		context = new LinkedList<EObject>();
 		results = new LinkedList<Result>();
 	}
 	
 	private Result calculateMetric(Metric metric) {
 		metric.getCalculateClass().setContext(context);
 		return new Result(metric, context, metric.getCalculateClass().calculate());
 	}
 
 }
