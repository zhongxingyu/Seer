 /**
  * Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group
  * E184
  *
  * This work was partially supported by the European Commission in terms of the
  * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package at.ac.tuwien.dsg.mela.analysisservice.control;
 
 import at.ac.tuwien.dsg.mela.common.requirements.MetricFilter;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MetricValue;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.ServiceMonitoringSnapshot;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Metric;
 import at.ac.tuwien.dsg.mela.common.requirements.Requirements;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.ElasticitySpace;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.ElasticitySpaceFunction;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.impl.ElSpaceDefaultFunction;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.impl.defaultElPthwFunction.LightweightEncounterRateElasticityPathway;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.impl.defaultElSgnFunction.som.entities.Neuron;
 import at.ac.tuwien.dsg.mela.analysisservice.engines.InstantMonitoringDataAnalysisEngine;
 import at.ac.tuwien.dsg.mela.analysisservice.engines.DataAggregationEngine;
 import at.ac.tuwien.dsg.mela.analysisservice.utils.converters.ConvertToJSON;
 import at.ac.tuwien.dsg.mela.analysisservice.report.AnalysisReport;
 import at.ac.tuwien.dsg.mela.dataservice.dataSource.AbstractDataAccess;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionOperation;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRule;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
 import at.ac.tuwien.dsg.mela.analysisservice.utils.Configuration;
 import at.ac.tuwien.dsg.mela.analysisservice.utils.exceptions.ConfigurationException;
 import at.ac.tuwien.dsg.mela.dataservice.AggregatedMonitoringDataSQLAccess;
import at.ac.tuwien.dsg.mela.dataservice.dataSource.impl.DataAccesForTestsOnly;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import org.apache.log4j.Level;
 import org.json.simple.JSONObject;
 
 /**
  * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at *
  *
  * Delegates the functionality of configuring MELA for instant monitoring and
  * analysis
  */
 public class SystemControl {
 
     private AbstractDataAccess dataAccess;
     private Requirements requirements;
     private CompositionRulesConfiguration compositionRulesConfiguration;
     private MonitoredElement serviceConfiguration;
     private DataAggregationEngine instantMonitoringDataEnrichmentEngine;
     private InstantMonitoringDataAnalysisEngine instantMonitoringDataAnalysisEngine;
     //used for data Aggregation over time
     private List<ServiceMonitoringSnapshot> historicalMonitoringData;
     //used if somewhone wants freshest data
     private ServiceMonitoringSnapshot latestMonitoringData;
     //interval at which RAW monitoring data is collected
     private int monitoringIntervalInSeconds = Configuration.getMonitoringIntervalInSeconds();
     //interval over which raw monitoring data is aggregated.
     //example: for monitoringIntervalInSeconds at 5 seconds, and aggregation at 30, 
     //means 6 monitoring snapshots are aggregated into 1
     private int aggregationWindowsCount = Configuration.getMonitoringAggregationIntervalInSeconds();
     private Timer monitoringTimer;
     //used in determining the service elasticity space
     private ElasticitySpaceFunction elasticitySpaceFunction;
     //temproary. Should support more functions in the future
 //    private LightweightEncounterRateElasticityPathway elasticityPathway;
     //holding MonitoredElement name, and Actions Name
     private Map<MonitoredElement, String> actionsInExecution;
     private Boolean isElasticityEnabled = Configuration.isElasticityAnalysisEnabled();
     private AggregatedMonitoringDataSQLAccess aggregatedMonitoringDataSQLAccess;
     //used in monitoring 
     private TimerTask task = new TimerTask() {
         @Override
         public void run() {
         }
     };
 
 //    private SystemControl selfReference;
     protected SystemControl() {
        dataAccess = DataAccesForTestsOnly.createInstance();
//        dataAccess = DataAccess.createInstance();
 
         instantMonitoringDataEnrichmentEngine = new DataAggregationEngine();
         instantMonitoringDataAnalysisEngine = new InstantMonitoringDataAnalysisEngine();
 
         latestMonitoringData = new ServiceMonitoringSnapshot();
         historicalMonitoringData = new ArrayList<ServiceMonitoringSnapshot>();
 
 //        selfReference = this;
         actionsInExecution = new ConcurrentHashMap<MonitoredElement, String>();
         startMonitoring();
 
         if ((int) (monitoringIntervalInSeconds / aggregationWindowsCount) == 0) {
             aggregationWindowsCount = 1 * monitoringIntervalInSeconds;
         }
 
         aggregatedMonitoringDataSQLAccess = new AggregatedMonitoringDataSQLAccess("mela", "mela");
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 stopMonitoring();
             }
         });
 
     }
 
     public synchronized MonitoredElement getServiceConfiguration() {
         return serviceConfiguration;
     }
 
     public synchronized void addExecutingAction(String targetEntityID, String actionName) {
         MonitoredElement element = new MonitoredElement(targetEntityID);
         actionsInExecution.put(element, actionName);
     }
 
     public synchronized void removeExecutingAction(String targetEntityID, String actionName) {
         MonitoredElement element = new MonitoredElement(targetEntityID);
         if (actionsInExecution.containsKey(element)) {
             actionsInExecution.remove(element);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.INFO, "Action " + actionName + " on monitored element " + targetEntityID + " not found.");
         }
     }
 
     public synchronized Map<MonitoredElement, String> getActionsInExecution() {
         return actionsInExecution;
     }
 
     public synchronized void setServiceConfiguration(MonitoredElement serviceConfiguration) {
         this.serviceConfiguration = serviceConfiguration;
         elasticitySpaceFunction = new ElSpaceDefaultFunction(serviceConfiguration);
         if (requirements != null) {
             elasticitySpaceFunction.setRequirements(requirements);
         }
         monitoringTimer.cancel();
         monitoringTimer.purge();
         startMonitoring();
     }
 
     //actually removes all VMs and Virtual Clusters from the ServiceUnit and adds new ones.
     public synchronized void updateServiceConfiguration(MonitoredElement serviceConfiguration) {
         //extract all ServiceUnit level monitored elements from both services, and replace their children  
         Map<MonitoredElement, MonitoredElement> serviceUnits = new HashMap<MonitoredElement, MonitoredElement>();
         for (MonitoredElement element : this.serviceConfiguration) {
             if (element.getLevel().equals(MonitoredElement.MonitoredElementLevel.SERVICE_UNIT)) {
                 //remove element's children
                 element.getContainedElements().clear();
                 serviceUnits.put(element, element);
             }
         }
 
         //go trough the new service, and for each Service Unit, add its children (containing both Virtual Machines and Virtual Clusters) to the original service
         for (MonitoredElement element : serviceConfiguration) {
             if (serviceUnits.containsKey(element)) {
                 //bad practice. breaks incapsulation
                 serviceUnits.get(element).getContainedElements().addAll(element.getContainedElements());
             }
         }
 
     }
 
 //    public List<Neuron> getElPathwayGroups(Map<Metric, List<MetricValue>> map) {
 //        if (elasticitySpaceFunction != null && map != null) {
 //            return elasticityPathway.getSituationGroups(map);
 //        } else {
 //            return new ArrayList<Neuron>();
 //        }
 //    }
     public synchronized Requirements getRequirements() {
         return requirements;
     }
 
     public synchronized void setRequirements(Requirements requirements) {
         this.requirements = requirements;
         elasticitySpaceFunction.setRequirements(requirements);
     }
 
     public synchronized CompositionRulesConfiguration getCompositionRulesConfiguration() {
         return compositionRulesConfiguration;
     }
 
     public synchronized void setCompositionRulesConfiguration(CompositionRulesConfiguration compositionRulesConfiguration) {
         if (dataAccess != null) {
             dataAccess.getMetricFilters().clear();
             //add data access metric filters for the source of each composition rule
             for (CompositionRule compositionRule : compositionRulesConfiguration.getMetricCompositionRules().getCompositionRules()) {
                 //go trough each CompositionOperation and extract the source metrics
 
                 List<CompositionOperation> queue = new ArrayList<CompositionOperation>();
                 queue.add(compositionRule.getOperation());
 
                 while (!queue.isEmpty()) {
                     CompositionOperation operation = queue.remove(0);
                     queue.addAll(operation.getSubOperations());
 
                     Metric targetMetric = operation.getTargetMetric();
                     //metric can be null if a composition rule artificially creates a metric using SET_VALUE
                     if (targetMetric != null) {
                         MetricFilter metricFilter = new MetricFilter();
                         metricFilter.setId(targetMetric.getName() + "_Filter");
                         metricFilter.setLevel(operation.getMetricSourceMonitoredElementLevel());
                         Collection<Metric> metrics = new ArrayList<Metric>();
                         metrics.add(new Metric(targetMetric.getName()));
                         metricFilter.setMetrics(metrics);
                         dataAccess.addMetricFilter(metricFilter);
                     }
                 }
             }
             this.compositionRulesConfiguration = compositionRulesConfiguration;
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl."
                     + "Metric filters to get metrics targeted by composition rules will not be added");
             this.compositionRulesConfiguration = compositionRulesConfiguration;
         }
 
     }
 
     public synchronized AbstractDataAccess getDataAccess() {
         return dataAccess;
     }
 
     public synchronized void setDataAccess(AbstractDataAccess dataAccess) {
         this.dataAccess = dataAccess;
     }
 
     public synchronized ServiceMonitoringSnapshot getRawMonitoringData() {
         if (dataAccess != null) {
             return dataAccess.getMonitoredData(serviceConfiguration);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
             return new ServiceMonitoringSnapshot();
         }
     }
 
     public synchronized ServiceMonitoringSnapshot getAggregatedMonitoringDataOverTime(List<ServiceMonitoringSnapshot> serviceMonitoringSnapshots) {
         if (serviceMonitoringSnapshots.size() > 1) {
             return instantMonitoringDataEnrichmentEngine.aggregateMonitoringDataOverTime(compositionRulesConfiguration, serviceMonitoringSnapshots);
         } else {
             return instantMonitoringDataEnrichmentEngine.enrichMonitoringData(compositionRulesConfiguration, serviceMonitoringSnapshots.get(0));
         }
     }
 
     private synchronized ServiceMonitoringSnapshot getAggregatedMonitoringData(ServiceMonitoringSnapshot rawMonitoringData) {
         if (dataAccess != null) {
             return instantMonitoringDataEnrichmentEngine.enrichMonitoringData(compositionRulesConfiguration, rawMonitoringData);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
             return new ServiceMonitoringSnapshot();
         }
     }
 
     private synchronized AnalysisReport analyzeAggregatedMonitoringData(ServiceMonitoringSnapshot rawMonitoringData) {
         if (dataAccess != null) {
             return instantMonitoringDataAnalysisEngine.analyzeRequirements(instantMonitoringDataEnrichmentEngine.enrichMonitoringData(compositionRulesConfiguration, rawMonitoringData), requirements);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
             return new AnalysisReport(new ServiceMonitoringSnapshot(), new Requirements());
         }
     }
 
     public synchronized AnalysisReport analyzeLatestMonitoringData() {
         if (dataAccess != null) {
             return instantMonitoringDataAnalysisEngine.analyzeRequirements(instantMonitoringDataEnrichmentEngine.enrichMonitoringData(compositionRulesConfiguration, latestMonitoringData), requirements);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
             return new AnalysisReport(new ServiceMonitoringSnapshot(), new Requirements());
         }
     }
 
     public synchronized Collection<Metric> getAvailableMetricsForMonitoredElement(MonitoredElement MonitoredElement) throws ConfigurationException {
         if (dataAccess != null) {
             return dataAccess.getAvailableMetricsForMonitoredElement(MonitoredElement);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
             return new ArrayList<Metric>();
         }
     }
 
     public synchronized void addMetricFilter(MetricFilter metricFilter) {
         if (dataAccess != null) {
             dataAccess.addMetricFilter(metricFilter);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
         }
     }
 
     public synchronized void addMetricFilters(Collection<MetricFilter> newFilters) {
         if (dataAccess != null) {
             dataAccess.addMetricFilters(newFilters);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
         }
     }
 
     public synchronized void removeMetricFilter(MetricFilter metricFilter) {
         if (dataAccess != null) {
             dataAccess.removeMetricFilter(metricFilter);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
         }
     }
 
     public synchronized void removeMetricFilters(Collection<MetricFilter> filtersToRemove) {
         if (dataAccess != null) {
             dataAccess.removeMetricFilters(filtersToRemove);
         } else {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Data Access source not set yet on SystemControl");
         }
     }
 
     public synchronized void setMonitoringIntervalInSeconds(int monitoringIntervalInSeconds) {
         this.monitoringIntervalInSeconds = monitoringIntervalInSeconds;
     }
 
     public synchronized void setNrOfMonitoringWindowsToAggregate(int aggregationIntervalInSeconds) {
         this.aggregationWindowsCount = aggregationIntervalInSeconds;
     }
 
     public synchronized ServiceMonitoringSnapshot getLatestMonitoringData() {
         return latestMonitoringData;
     }
     private int monitoringCount = 1800;
     private int currMonitoringIndex = 0;
 
     private synchronized void startMonitoring() {
         monitoringTimer = new Timer();
 
         task = new TimerTask() {
             @Override
             public void run() {
                 if (serviceConfiguration != null) {
 
                     monitoringCount--;
                     if (monitoringCount <= 0) {
                         this.cancel();
                     }
 
                     ServiceMonitoringSnapshot monitoringData = getRawMonitoringData();
 
                     currMonitoringIndex++;
                     while (currMonitoringIndex < 0) {
                         getRawMonitoringData();
                         currMonitoringIndex++;
                     }
 
                     if (monitoringData != null) {
                         historicalMonitoringData.add(monitoringData);
                         //remove the oldest and add the new value always
                         if (historicalMonitoringData.size() > aggregationWindowsCount / monitoringIntervalInSeconds) {
                             historicalMonitoringData.remove(0);
                         }
 
                         if (compositionRulesConfiguration != null) {
                             latestMonitoringData = getAggregatedMonitoringDataOverTime(historicalMonitoringData);
                         }
 
                         //if we have no composition function, we have no metrics, so it does not make sense to train the elasticity space
                         if (isElasticityEnabled && compositionRulesConfiguration != null) {
                             //write monitoring data in sql
                             aggregatedMonitoringDataSQLAccess.writeMonitoringData(latestMonitoringData);
 //                            elasticitySpaceFunction.trainElasticitySpace(latestMonitoringData);
                         }
                     } else {
                         //stop the monitoring if the data replay is done
 //                        this.cancel();
                         Configuration.getLogger(this.getClass()).log(Level.ERROR, "Monitoring data is NULL");
                     }
                 } else {
                     Configuration.getLogger(this.getClass()).log(Level.WARN, "No service configuration");
                 }
             }
         };
         //repeat the monitoring every monitoringIntervalInSeconds seconds 
 //        monitoringTimer.schedule(task, 0, monitoringIntervalInSeconds * 1000);
         monitoringTimer.schedule(task, 0, 1);
     }
 
     public synchronized void stopMonitoring() {
         task.cancel();
     }
 
     //performs multiple database interrogations (avids using memory)
     public synchronized String getElasticityPathwayLazy(MonitoredElement element) {
         //if no service configuration, we can't have elasticity space function
         //if no compositionRulesConfiguration we have no data
         if (!Configuration.isElasticityAnalysisEnabled() || serviceConfiguration == null && compositionRulesConfiguration != null) {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Elasticity analysis disabled, or no service configuration or composition rules configuration");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "ElPathway");
             return elSpaceJSON.toJSONString();
         }
 
 
         int recordsCount = aggregatedMonitoringDataSQLAccess.getRecordsCount();
 
         //first, read from the sql of monitoring data, in increments of 10, and train the elasticity space function
         LightweightEncounterRateElasticityPathway elasticityPathway = null;
 
         ElasticitySpace tempSpace = new ElasticitySpace(serviceConfiguration);
         List<Metric> metrics = null;
         int stepCount = (recordsCount > 500) ? recordsCount / 500 : 1;
 
         for (int i = 0; i < stepCount; i++) {
             List<ServiceMonitoringSnapshot> extractedData = aggregatedMonitoringDataSQLAccess.extractMonitoringData(i * 500, 500);
             if (extractedData != null) {
                 //for each extracted snapshot, train the space
                 for (ServiceMonitoringSnapshot monitoringSnapshot : extractedData) {
                     tempSpace.addMonitoringData(monitoringSnapshot);
                 }
             }
             Map<Metric, List<MetricValue>> map = tempSpace.getMonitoredDataForService(element);
             if (map != null && metrics == null) {
                 metrics = new ArrayList<Metric>(map.keySet());
                 //we need to know the number of weights to add in instantiation
                 elasticityPathway = new LightweightEncounterRateElasticityPathway(metrics.size());
             }
 
             elasticityPathway.trainElasticityPathway(map);
             tempSpace.reset();
         }
 
         List<Neuron> neurons = elasticityPathway.getSituationGroups();
         if (metrics == null) {
             Configuration.getLogger(this.getClass()).log(Level.ERROR, "Service Element " + element.getId() + " at level " + element.getLevel() + " was not found in service structure");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "Service not found");
             return elSpaceJSON.toJSONString();
         } else {
             return ConvertToJSON.convertElasticityPathway(metrics, neurons);
         }
     }
 
     //performs multiple database interrogations (avids using memory)
     public synchronized String getElasticitySpaceLazy(MonitoredElement element) {
 
         //if no service configuration, we can't have elasticity space function
         //if no compositionRulesConfiguration we have no data
         if (!Configuration.isElasticityAnalysisEnabled() || serviceConfiguration == null && compositionRulesConfiguration != null) {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Elasticity analysis disabled, or no service configuration or composition rules configuration");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "ElSpace");
             return elSpaceJSON.toJSONString();
         }
 
         if (elasticitySpaceFunction != null) {
             elasticitySpaceFunction.resetElasticitySpace();
         }
 
         int recordsCount = aggregatedMonitoringDataSQLAccess.getRecordsCount();
 
 
         //first, read from the sql of monitoring data, in increments of 10, and train the elasticity space function
         List<Metric> metrics = null;
         int stepCount = (recordsCount > 500) ? recordsCount / 500 : 1;
 
         for (int i = 0; i < stepCount; i++) {
             List<ServiceMonitoringSnapshot> extractedData = aggregatedMonitoringDataSQLAccess.extractMonitoringData(i * 500, 500);
             if (extractedData != null) {
                 //for each extracted snapshot, trim it to contain data only for the targetedMonitoredElement (minimizes RAM usage)
                 for (ServiceMonitoringSnapshot monitoringSnapshot : extractedData) {
 //                    monitoringSnapshot.keepOnlyDataForElement(element);
                     elasticitySpaceFunction.trainElasticitySpace(monitoringSnapshot);
                 }
             }
         }
 
         String jsonRepr = ConvertToJSON.convertElasticitySpace(elasticitySpaceFunction.getElasticitySpace(), element);
 //        elasticitySpaceFunction = new ElSpaceDefaultFunction(serviceConfiguration);
         elasticitySpaceFunction.resetElasticitySpace();
         if (requirements != null) {
             elasticitySpaceFunction.setRequirements(requirements);
         }
 
         return jsonRepr;
     }
 
     //uses a lot of memory (all directly in memory)
     public synchronized String getElasticityPathway(MonitoredElement element) {
         //if no service configuration, we can't have elasticity space function
         //if no compositionRulesConfiguration we have no data
         if (!Configuration.isElasticityAnalysisEnabled() || serviceConfiguration == null && compositionRulesConfiguration != null) {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Elasticity analysis disabled, or no service configuration or composition rules configuration");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "ElPathway");
             return elSpaceJSON.toJSONString();
         }
 
 //        int recordsCount = aggregatedMonitoringDataSQLAccess.getRecordsCount();
 
         //first, read from the sql of monitoring data, in increments of 10, and train the elasticity space function
         LightweightEncounterRateElasticityPathway elasticityPathway = null;
 
         ElasticitySpace tempSpace = new ElasticitySpace(serviceConfiguration);
         List<Metric> metrics = null;
 
         List<ServiceMonitoringSnapshot> extractedData = aggregatedMonitoringDataSQLAccess.extractMonitoringData();
         if (extractedData != null) {
             //for each extracted snapshot, train the space
             for (ServiceMonitoringSnapshot monitoringSnapshot : extractedData) {
                 tempSpace.addMonitoringData(monitoringSnapshot);
             }
         }
         Map<Metric, List<MetricValue>> map = tempSpace.getMonitoredDataForService(element);
         if (map != null && metrics == null) {
             metrics = new ArrayList<Metric>(map.keySet());
             //we need to know the number of weights to add in instantiation
             elasticityPathway = new LightweightEncounterRateElasticityPathway(metrics.size());
         }
 
         elasticityPathway.trainElasticityPathway(map);
 
 
         List<Neuron> neurons = elasticityPathway.getSituationGroups();
         if (metrics == null) {
             Configuration.getLogger(this.getClass()).log(Level.ERROR, "Service Element " + element.getId() + " at level " + element.getLevel() + " was not found in service structure");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "Service not found");
             return elSpaceJSON.toJSONString();
         } else {
             return ConvertToJSON.convertElasticityPathway(metrics, neurons);
         }
     }
 
     public synchronized String getElasticitySpace(MonitoredElement element) {
 
         //if no service configuration, we can't have elasticity space function
         //if no compositionRulesConfiguration we have no data
         if (!Configuration.isElasticityAnalysisEnabled() || serviceConfiguration == null && compositionRulesConfiguration != null) {
             Configuration.getLogger(this.getClass()).log(Level.WARN, "Elasticity analysis disabled, or no service configuration or composition rules configuration");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "ElSpace");
             return elSpaceJSON.toJSONString();
         }
 
 //        int recordsCount = aggregatedMonitoringDataSQLAccess.getRecordsCount();
 
         //first, read from the sql of monitoring data, in increments of 10, and train the elasticity space function
 //        List<Metric> metrics = null;
 
         List<ServiceMonitoringSnapshot> extractedData = aggregatedMonitoringDataSQLAccess.extractMonitoringData();
         if (extractedData != null) {
             //for each extracted snapshot, trim it to contain data only for the targetedMonitoredElement (minimizes RAM usage)
             for (ServiceMonitoringSnapshot monitoringSnapshot : extractedData) {
 //                monitoringSnapshot.keepOnlyDataForElement(element);
                 elasticitySpaceFunction.trainElasticitySpace(monitoringSnapshot);
             }
         }
 
         String jsonRepr = ConvertToJSON.convertElasticitySpace(elasticitySpaceFunction.getElasticitySpace(), element);
 //        elasticitySpaceFunction = new ElSpaceDefaultFunction(serviceConfiguration);
         elasticitySpaceFunction.resetElasticitySpace();
         if (requirements != null) {
             elasticitySpaceFunction.setRequirements(requirements);
         }
 
         return jsonRepr;
     }
 
     public synchronized String getLatestMonitoringDataINJSON() {
         return ConvertToJSON.convertMonitoringSnapshot(latestMonitoringData, requirements, actionsInExecution);
     }
 
     public synchronized String getMetricCompositionRules() {
         if (compositionRulesConfiguration != null) {
             return ConvertToJSON.convertToJSON(compositionRulesConfiguration.getMetricCompositionRules());
         } else {
             JSONObject jsonObject = new JSONObject();
             jsonObject.put("name", "No composition rules yet");
             return jsonObject.toJSONString();
         }
     }
 }
