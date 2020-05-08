 /**
  * Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group E184
  *
  * This work was partially supported by the European Commission in terms of the CELAR FP7 project (FP7-ICT-2011-8 \#317790)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package at.ac.tuwien.dsg.mela.analysisservice.gui;
 
 import at.ac.tuwien.dsg.mela.common.requirements.MetricFilter;
 import at.ac.tuwien.dsg.mela.common.requirements.Condition;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MetricValue;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.ServiceMonitoringSnapshot;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Metric;
 import at.ac.tuwien.dsg.mela.common.requirements.Requirement;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
 import at.ac.tuwien.dsg.mela.common.requirements.Requirements;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.ElasticitySpace;
 import at.ac.tuwien.dsg.mela.analysisservice.concepts.impl.defaultElSgnFunction.som.entities.Neuron;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionOperation;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRule;
 import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesBlock;
 import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
 import at.ac.tuwien.dsg.mela.analysisservice.utils.Configuration;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Level;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 
 /**
  * Author: Daniel Moldovan 
  * E-Mail: d.moldovan@dsg.tuwien.ac.at 
 
  **/
 public class ConvertToJSON {
 
     private static DecimalFormat df4 = new DecimalFormat("0.####");
     private static DecimalFormat df2 = new DecimalFormat("0.##");
 
     public static String convertElasticityPathway(List<Metric> metrics, List<Neuron> elPathwayGroups) {
         if (elPathwayGroups == null || metrics == null) {
             Configuration.getLogger(ConvertToJSON.class).log(Level.WARN, "Elasticity Pathway is null");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "ElPathway");
             return elSpaceJSON.toJSONString();
         }
 
         JSONObject elSpaceJSON = new JSONObject();
         elSpaceJSON.put("name", "ElPathway");
 
         JSONArray pathwayNeurons = new JSONArray();
         JSONArray metricLabels = new JSONArray();
 
         //try to transform from matric (1 line holds 1 neuron) to lists, in which I have for Metric X
         for (int i = 0; i < metrics.size(); i++) {
             JSONObject element = new JSONObject();
             element.put("name", metrics.get(i).getName());
             element.put("unit", metrics.get(i).getMeasurementUnit());
             metricLabels.add(element);
         }
 
         elSpaceJSON.put("metricLabels", metricLabels);
 
 
         for (Neuron neuron : elPathwayGroups) {
             JSONObject neuronJSON = new JSONObject();
             neuronJSON.put("level", neuron.getUsageLevel().toString());
             neuronJSON.put("encounterRate", df2.format(neuron.getUsagePercentage()));
             List<Double> values = neuron.getWeights();
             for (int i = 0; i < values.size(); i++) {
 
                 Double value = values.get(i);
                 neuronJSON.put(metrics.get(i).getName(), df4.format(value));
             }
             pathwayNeurons.add(neuronJSON);
         }
         elSpaceJSON.put("neurons", pathwayNeurons);
 
 
         return elSpaceJSON.toJSONString();
 
     }
 
     public static String convertElasticitySpace(ElasticitySpace space, MonitoredElement MonitoredElement) {
 
         if (space == null || MonitoredElement == null) {
             Configuration.getLogger(ConvertToJSON.class).log(Level.WARN, "Elasticity Space or supplied Service Element are null");
             JSONObject elSpaceJSON = new JSONObject();
             elSpaceJSON.put("name", "ElSpace");
             return elSpaceJSON.toJSONString();
         }
 
         JSONObject elSpaceJSON = new JSONObject();
         elSpaceJSON.put("name", MonitoredElement.getId() + "ElSpace");
         JSONArray spaceDimensions = new JSONArray();
 
         Map<Metric, List<MetricValue>> elementMonitoringData = space.getMonitoredDataForService(MonitoredElement);
 
         for (Metric metric : elementMonitoringData.keySet()) {
             JSONObject spaceDimensionJSON = new JSONObject();
             spaceDimensionJSON.put("name", metric.getName());
             spaceDimensionJSON.put("unit", metric.getMeasurementUnit());
 
             JSONArray metricValuesJSON = new JSONArray();
             JSONArray metricUpperBoundaryValuesJSON = new JSONArray();
             JSONArray metricLowerBoundaryValuesJSON = new JSONArray();
 
             //0 = low, 1 high
             MetricValue[] boundaries = space.getSpaceBoundaryForMetric(MonitoredElement, metric);
 
             for (MetricValue metricValue : elementMonitoringData.get(metric)) {
                 {
                     JSONObject metricValueJSON = new JSONObject();
                     metricValueJSON.put("value", metricValue.getValueRepresentation());
                     metricValuesJSON.add(metricValueJSON);
                 }
                 {
                     JSONObject metricUpperBoundaryJSON = new JSONObject();
                     metricUpperBoundaryJSON.put("value", boundaries[1]);
                     metricUpperBoundaryValuesJSON.add(metricUpperBoundaryJSON);
                 }
                 {
                     JSONObject metricLowerBoundaryJSON = new JSONObject();
                     metricLowerBoundaryJSON.put("value", boundaries[0]);
                     metricLowerBoundaryValuesJSON.add(metricLowerBoundaryJSON);
                 }
             }
 
             spaceDimensionJSON.put("values", metricValuesJSON);
             spaceDimensionJSON.put("upperBoundary", metricUpperBoundaryValuesJSON);
             spaceDimensionJSON.put("lowerBoundary", metricLowerBoundaryValuesJSON);
 
             spaceDimensions.add(spaceDimensionJSON);
 
         }
 
         elSpaceJSON.put("dimensions", spaceDimensions);
 
         return elSpaceJSON.toJSONString();
     }
 
     //accepts metric filters for cosmetisation_to avoid showing some metrics
 //    public static String describeInJSON(ServiceMonitoringSnapshot serviceMonitoringSnapshot, List<MetricFilter> filters, String actionName, Entity targetEntity) {
 //
 //        if (serviceMonitoringSnapshot == null || filters == null) {
 //            return null;
 //        }
 //
 //        //assumes there is one SERVICE level element per snapshot
 //        Map<MonitoredElement.MonitoredElementLevel, Map<MonitoredElement, MonitoredElementMonitoringSnapshot>> monitoredData = serviceMonitoringSnapshot.getMonitoredData();
 //        MonitoredElement rootElement = monitoredData.get(MonitoredElement.MonitoredElementLevel.SERVICE).keySet().iterator().next();
 //
 //        //apply metric filters (for cosmetisation_
 //        for (MetricFilter metricFilter : filters) {
 //
 //            if (monitoredData.containsKey(metricFilter.getLevel())) {
 //                for (Map.Entry<MonitoredElement, MonitoredElementMonitoringSnapshot> entry : monitoredData.get(metricFilter.getLevel()).entrySet()) {
 //                    //if either the filter applies on all elements at one particular level (targetIDs are null or empty) either the filter targets the serviceStructure element ID
 //                    if (metricFilter.getTargetMonitoredElementIDs() == null
 //                            || metricFilter.getTargetMonitoredElementIDs().size() == 0
 //                            || metricFilter.getTargetMonitoredElementIDs().contains(entry.getKey().getId())) {
 //                        entry.getValue().keepMetrics(metricFilter.getMetrics());
 //                    }
 //                }
 //            }
 //        }
 //
 //        JSONObject root = new JSONObject();
 //        root.put("name", rootElement.getId());
 //        root.put("type", "" + rootElement.getLevel());
 //
 //
 //
 //        //going trough the serviceStructure element tree in a BFS manner
 //        List<MyPair> processing = new ArrayList<MyPair>();
 //        processing.add(new MyPair(rootElement, root));
 //
 //        while (!processing.isEmpty()) {
 //            MyPair myPair = processing.remove(0);
 //            JSONObject object = myPair.jsonObject;
 //            MonitoredElement element = myPair.MonitoredElement;
 //            JSONArray children = (JSONArray) object.get("children");
 //            if (children == null) {
 //                children = new JSONArray();
 //                object.put("children", children);
 //            }
 //
 //            //add information about executing actions
 //            {
 //                if (targetEntity != null && element.getId().equals(targetEntity.getId())) {
 //                    object.put("attention", "true");
 //                    object.put("actionName", actionName);
 //                }
 //            }
 //
 //            JSONArray metrics = new JSONArray();
 //            MonitoredElementMonitoringSnapshot MonitoredElementMonitoringSnapshot = monitoredData.get(element.getLevel()).get(element);
 //
 //            //add metrics
 //            for (Map.Entry<Metric, MetricValue> entry : MonitoredElementMonitoringSnapshot.getMonitoredData().entrySet()) {
 //                JSONObject metric = new JSONObject();
 //                metric.put("name", entry.getValue().getValueRepresentation() + " [" + entry.getKey().getName() + "]"); //+ "(" + entry.getKey().getMeasurementUnit() + ")" +"]");
 //                metric.put("type", "metric");
 //
 ////                JSONObject value = new JSONObject();
 ////                value.put("name",""+entry.getValue().getValueRepresentation());
 ////                value.put("type","metric");
 ////
 ////                JSONArray valueChildren = new JSONArray();
 ////                valueChildren.add(value);
 ////
 ////                metric.put("children", valueChildren);
 //
 //                children.add(metric);
 //            }
 ////            object.put("metrics", metrics);
 //
 //            //add children
 //            for (MonitoredElement child : element.getContainedElements()) {
 //                JSONObject childElement = new JSONObject();
 //                childElement.put("name", child.getId());
 //                childElement.put("type", "" + child.getLevel());
 //                JSONArray childrenChildren = new JSONArray();
 //                childElement.put("children", childrenChildren);
 //                processing.add(new MyPair(child, childElement));
 //                children.add(childElement);
 //            }
 //        }
 //
 //
 //
 //        String string = root.toJSONString();
 //        string = string.replaceAll("\"", "!");
 //
 //        return string;
 //    }
 
     public static String convertMonitoringSnapshot(ServiceMonitoringSnapshot serviceMonitoringSnapshot, Requirements requirements, Map<MonitoredElement,String> actionsInExecution) {
 
         if(serviceMonitoringSnapshot == null){
             return "";
         }
         
         //transform the requirements list in a map which can be used when building the JSON representation
         Map<String, Map<Metric, List<Requirement>>> requirementsMap = new HashMap<String, Map<Metric, List<Requirement>>>();
         if (requirements != null) {
             //for each requirement
             for (Requirement requirement : requirements.getRequirements()) {
                 //for each service element ID targeted by the requirement
                 for (String id : requirement.getTargetMonitoredElementIDs()) {
                     if (requirementsMap.containsKey(id)) {
                         Map<Metric, List<Requirement>> reqMap = requirementsMap.get(id);
                         if (reqMap.containsKey(requirement.getMetric())) {
                             reqMap.get(requirement.getMetric()).add(requirement);
                         } else {
                             List<Requirement> list = new ArrayList<Requirement>();
                             list.add(requirement);
                             reqMap.put(requirement.getMetric(), list);
                         }
                     } else {
                         Map<Metric, List<Requirement>> reqMap = new HashMap<Metric, List<Requirement>>();
                         List<Requirement> list = new ArrayList<Requirement>();
                         list.add(requirement);
                         reqMap.put(requirement.getMetric(), list);
                         requirementsMap.put(id, reqMap);
                     }
                 }
             }
         }
 
 
         //assumes there is one SERVICE level element per snapshot
         Map<MonitoredElement.MonitoredElementLevel, Map<MonitoredElement, MonitoredElementMonitoringSnapshot>> monitoredData = serviceMonitoringSnapshot.getMonitoredData();
         if (monitoredData != null && monitoredData.containsKey(MonitoredElement.MonitoredElementLevel.SERVICE)) {
 
             //access root service element
             MonitoredElement rootElement = monitoredData.get(MonitoredElement.MonitoredElementLevel.SERVICE).keySet().iterator().next();
 
             JSONObject root = new JSONObject();
             root.put("name", rootElement.getId());
             root.put("type", "" + rootElement.getLevel());
 
             //going trough the serviceStructure element tree in a BFS manner
             List<MyPair> processing = new ArrayList<MyPair>();
             processing.add(new MyPair(rootElement, root));
 
             while (!processing.isEmpty()) {
                 MyPair myPair = processing.remove(0);
                 JSONObject object = myPair.jsonObject;
                 MonitoredElement element = myPair.MonitoredElement;
                 
                 if(actionsInExecution.containsKey(element)){
                    object.put("attention", true);
                    object.put("actionName", actionsInExecution.get(element));
                 }
                 
                 JSONArray children = (JSONArray) object.get("children");
                 if (children == null) {
                     children = new JSONArray();
                     object.put("children", children);
                 }
 
                 //add children
                 for (MonitoredElement child : element.getContainedElements()) {
                     JSONObject childElement = new JSONObject();
                     childElement.put("name", child.getId());
                     childElement.put("type", "" + child.getLevel());
                     JSONArray childrenChildren = new JSONArray();
                     childElement.put("children", childrenChildren);
                     processing.add(new MyPair(child, childElement));
                     children.add(childElement);
                 }
 
                 JSONArray metrics = new JSONArray();
                 if (monitoredData.containsKey(element.getLevel())) {
                     MonitoredElementMonitoringSnapshot monitoredElementMonitoringSnapshot = monitoredData.get(element.getLevel()).get(element);
 
                     //add metrics
                     for (Map.Entry<Metric, MetricValue> entry : monitoredElementMonitoringSnapshot.getMonitoredData().entrySet()) {
                         JSONObject metric = new JSONObject();
                         if(entry.getKey().getName().contains("serviceID")){
                             continue;
                         }
                        metric.put("name", entry.getValue().getValueRepresentation() + " [" + entry.getKey().getName() + "]");
                         metric.put("type", "metric");
 
                         //if we have requirements for this service element ID
                         if (requirementsMap.containsKey(element.getId())) {
                             Map<Metric, List<Requirement>> reqMap = requirementsMap.get(element.getId());
 
                             //if we have requirement for this metric
                             if (reqMap.containsKey(entry.getKey())) {
                                 List<Requirement> list = reqMap.get(entry.getKey());
                                 //for all metric requirements, add the metric conditions in JSON
                                 JSONArray conditions = new JSONArray();
 
                                 for (Requirement requirement : list) {
                                     for (Condition condition : requirement.getConditions()) {
                                         JSONObject conditionJSON = new JSONObject();
                                         conditionJSON.put("name", "MUST BE " + condition.toString());
                                         conditionJSON.put("type", "requirement");
                                         conditions.add(conditionJSON);
                                     }
                                 }
                                 metric.put("children", conditions);
                             }
                         }
 
 //                JSONObject value = new JSONObject();
 //                value.put("name",""+entry.getValue().getValueRepresentation());
 //                value.put("type","metric");
 //
 //                JSONArray valueChildren = new JSONArray();
 //                valueChildren.add(value);
 //
 //                metric.put("children", valueChildren);
 
                         children.add(metric);
                     }
 
 //                    //add requirements
 //                    if (requirementsMap.containsKey(element.getId())) {
 //                        for (Requirement requirement : requirementsMap.get(element.getId())) {
 //                            JSONObject requirementJSON = new JSONObject();
 //                            requirementJSON.put("name", requirement.getMetric().getName());
 //                            requirementJSON.put("type", "requirement");
 //                            //for all conditions add children to requirement (if more conditions per metric)
 //                            JSONArray conditions = new JSONArray();
 //                            for (Condition condition : requirement.getConditions()) {
 //                                JSONObject conditionJSON = new JSONObject();
 //                                conditionJSON.put("name", condition.toString());
 //                                conditionJSON.put("type", "condition");
 //                                conditions.add(conditionJSON);
 //                            }
 //                            requirementJSON.put("children", conditions);
 //                            children.add(requirementJSON);
 //                        }
 //
 //                    }
                 }
 //            object.put("metrics", metrics);
 
 
             }
 
 
 
             String string = root.toJSONString();
 //            string = string.replaceAll("\"", "!");
 
             return string;
         } else {
             JSONObject root = new JSONObject();
             root.put("name", "No monitoring data");
             return "{\"name\":\"No Data\",\"type\":\"METRIC\"}";
         }
     }
 
     //convert metrics to JSON
     public static String convertToJSON(CompositionRulesBlock compositionRulesBlock) {
 
         JSONArray compositionRules = new JSONArray();
 
         for (CompositionRule compositionRule : compositionRulesBlock.getCompositionRules()) {
             Metric resultingMetric = compositionRule.getResultingMetric();
             if (resultingMetric == null) {
                 continue;
             }
 
             if(compositionRule.getTargetMonitoredElementLevel() == null){
                 System.out.println(resultingMetric.getName() + " " + resultingMetric.getMeasurementUnit());
             }
             
             JSONObject jsonMetric = new JSONObject();
             jsonMetric.put("name", resultingMetric.getName());
             jsonMetric.put("targetLevel", compositionRule.getTargetMonitoredElementLevel().toString());
             jsonMetric.put("targetMonitoredElementIDs", compositionRule.getTargetMonitoredElementIDs());
 
             JSONArray children = new JSONArray();
 
 
             List<CompositionOperation> operations = new ArrayList<CompositionOperation>();
             operations.add(compositionRule.getOperation());
             while (!operations.isEmpty()) {
                 CompositionOperation operation = operations.remove(0);
                 operations.addAll(operation.getSubOperations());
                 if (operation.getTargetMetric() != null) {
                     JSONObject targetRest = new JSONObject();
                     Metric targetMetric = operation.getTargetMetric();
                     targetRest.put("name", targetMetric.getName());
                     targetRest.put("targetLevel", operation.getMetricSourceMonitoredElementLevel().toString());
                     targetRest.put("targetMonitoredElementIDs", operation.getMetricSourceMonitoredElementIDs());
                     children.add(targetRest);
                 }
             }
             jsonMetric.put("children", children);
             compositionRules.add(jsonMetric);
         }
 
         return compositionRules.toJSONString();
     }
 
     private static class MyPair {
 
         public MonitoredElement MonitoredElement;
         public JSONObject jsonObject;
 
         private MyPair() {
         }
 
         public MyPair(MonitoredElement MonitoredElement, JSONObject jsonObject) {
             this.MonitoredElement = MonitoredElement;
             this.jsonObject = jsonObject;
         }
     }
 }
