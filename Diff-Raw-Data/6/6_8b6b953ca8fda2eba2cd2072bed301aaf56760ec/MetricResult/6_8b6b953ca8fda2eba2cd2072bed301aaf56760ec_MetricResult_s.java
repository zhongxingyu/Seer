 package org.kalibro;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Result of processing a {@link Metric} for a {@link Module}. Contains the associated {@link MetricConfiguration}
  * snapshot.
  * 
  * @author Carlos Morais
  */
 public class MetricResult extends AbstractMetricResult {
 
 	private Long id;
 
 	private MetricConfiguration configuration;
 	private Throwable error;
 	private List<Double> descendantResults;
 
 	public MetricResult(MetricConfiguration configuration, Throwable error) {
 		this(configuration, Double.NaN);
 		this.error = error;
 	}
 
 	public MetricResult(MetricConfiguration configuration, Double value) {
 		super(configuration.getMetric(), value);
 		this.configuration = configuration;
 		setDescendantResults(new ArrayList<Double>());
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public boolean hasError() {
 		return error != null;
 	}
 
 	public Throwable getError() {
 		return error;
 	}
 
 	public MetricConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	public List<Double> getDescendantResults() {
 		return descendantResults;
 	}
 
 	public void setDescendantResults(List<Double> descendantResults) {
 		this.descendantResults = descendantResults;
 	}
 
 	public void addDescendantResult(Double descendantResult) {
 		descendantResults.add(descendantResult);
 	}
 
 	public Double getAggregatedValue() {
 		if (getValue().isNaN() && !descendantResults.isEmpty())
 			return configuration.getAggregationForm().calculate(descendantResults);
 		return getValue();
 	}
 
 	public boolean hasRange() {
 		return configuration.getRangeFor(getAggregatedValue()) != null;
 	}
 
 	public Range getRange() {
 		return configuration.getRangeFor(getAggregatedValue());
 	}
 
 	public boolean hasGrade() {
 		return hasRange() && getRange().hasReading();
 	}
 
 	public Double getGrade() {
 		return getRange().getReading().getGrade();
 	}
 
 	public Double getWeight() {
 		return configuration.getWeight();
 	}
 
 	@Deprecated
 	public void setError(Exception exception) {
 		setValue(Double.NaN);
 		error = exception;
 	}
 }
