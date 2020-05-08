 package org.kalibro.core.processing;
 
 import java.util.Collection;
 
 import org.kalibro.*;
 import org.kalibro.core.persistence.ModuleResultDatabaseDao;
 
 /**
  * Analyzes and saves metric results collected by {@link BaseTool}s.
  * 
  * @author Carlos Morais
  */
 class AnalyzingTask extends ProcessSubtask {
 
 	private Configuration configurationSnapshot;
 	private ModuleResultDatabaseDao moduleResultDao;
 
 	private ModuleResult moduleResult;
 
 	@Override
 	protected void perform() {
 		moduleResultDao = daoFactory().createModuleResultDao();
 		configurationSnapshot = daoFactory().createConfigurationDao().snapshotFor(processing().getId());
 		for (NativeModuleResult nativeModuleResult : resultProducer())
 			analyzing(nativeModuleResult);
 	}
 
 	private void analyzing(NativeModuleResult nativeResult) {
 		moduleResult = moduleResultDao.prepareResultFor(nativeResult.getModule(), processing().getId());
 		addMetricResults(nativeResult.getMetricResults());
 		configureAndSave();
 	}
 
 	private void addMetricResults(Collection<NativeMetricResult> metricResults) {
 		for (NativeMetricResult metricResult : metricResults)
 			addMetricResult(metricResult);
 	}
 
 	private void addMetricResult(NativeMetricResult nativeMetricResult) {
 		Metric metric = nativeMetricResult.getMetric();
 		Double value = nativeMetricResult.getValue();
 		MetricConfiguration snapshot = configurationSnapshot.getConfigurationFor(metric);
 		moduleResult.addMetricResult(new MetricResult(snapshot, value));
 		addValueToAncestry(snapshot, value);
 	}
 
 	private void addValueToAncestry(MetricConfiguration snapshot, Double value) {
 		ModuleResult ancestor = moduleResult;
 		while (ancestor.hasParent()) {
 			ancestor = ancestor.getParent();
 			addDescendantResult(ancestor, snapshot, value);
 		}
 	}
 
 	private void addDescendantResult(ModuleResult ancestor, MetricConfiguration snapshot, Double descendantResult) {
 		Metric metric = snapshot.getMetric();
 		if (!ancestor.hasResultFor(metric))
 			ancestor.addMetricResult(new MetricResult(snapshot, Double.NaN));
 		ancestor.getResultFor(metric).addDescendantResult(descendantResult);
 	}
 
 	private void configureAndSave() {
 		ModuleResultConfigurer.configure(moduleResult, configurationSnapshot);
		ModuleResult parent = moduleResult.hasParent() ? moduleResult.getParent() : null;
 		save();
 		if (moduleResult.hasParent()) {
			moduleResult = parent;
 			configureAndSave();
 		} else
 			changeRootName();
 	}
 
 	private void changeRootName() {
 		if (!moduleResult.getModule().getName()[0].equals(repository().getName())) {
 			moduleResult.getModule().getName()[0] = repository().getName();
 			save();
 		}
 		processing().setResultsRoot(moduleResult);
 	}
 
 	private void save() {
 		moduleResult = moduleResultDao.save(moduleResult, processing().getId());
 	}
 }
