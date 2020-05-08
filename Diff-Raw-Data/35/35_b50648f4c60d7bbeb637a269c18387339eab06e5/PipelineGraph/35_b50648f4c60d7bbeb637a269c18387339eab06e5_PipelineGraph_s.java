 /*
  * Copyright (C) 2012 Thasso Griebel
  *
  * This file is part of JIP.
  *
  * JIP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JIP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JIP.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jip.graph;
 
 
 import com.google.common.collect.ArrayListMultimap;
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.graph.DirectedMultigraph;
 import org.jgrapht.traverse.TopologicalOrderIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * Executable graph created from a pipeline definition. To work properly, the
  * pipeline must be configured, otherwise the graph might not be completed and will
  * eventually not expand to its full width.
  */
 public class PipelineGraph {
     private static transient Logger log = LoggerFactory.getLogger(PipelineGraph.class);
     /**
      * Match configuration names
      */
     transient static final Pattern CONFIG_SPLITTER = Pattern.compile("^\\$\\{(.*)\\.(.*)\\}$");
     /**
      * The graph
      */
     DirectedGraph<JobNode, JobEdge> graph;
     /**
      * The pipeline
      */
     private Pipeline pipeline;
 
     /**
      * The pipeline context
      */
     private transient ScopeNode context;
 
     /**
      * Create the initial graph from the given pipeline
      *
      * @param pipelineSource   the pipeline
      */
     public PipelineGraph(Pipeline pipelineSource) {
         if (pipelineSource == null) throw new NullPointerException("Pipeline NULL not permitted");
         this.pipeline = new Pipeline(pipelineSource);
 
         // initialize pipeline
         for (Parameter parameter : pipeline.getParameter()) {
             Object value = pipeline.getConfiguration().get(parameter.getName());
             if(value != null && parameter.isFile()){
                 value = cleanToString(value);
                 pipeline.getConfiguration().put(parameter.getName(), value);
             }
         }
 
         for (PipelineJob execution : pipeline.getExecutions()) {
             for (Parameter parameter : execution.getParameters()) {
                 Object value = execution.getConfiguration().get(parameter.getName());
                 if(value != null && parameter.isFile()){
                     value = cleanToString(value);
                     execution.getConfiguration().put(parameter.getName(), value);
                 }
             }
         }
 
         graph = new DirectedMultigraph<JobNode, JobEdge>(JobEdge.class);
 
         HashMap<String, JobNode> initialNodes = new HashMap<String, JobNode>();
         log.debug("Creating initial execution nodes...");
         for (PipelineJob execution : pipeline.getExecutions()) {
             JobNode jobNode = new JobNode(execution);
             log.debug("Adding vertex: " + jobNode.getPipelineJob().getId());
 
             // JIP-50 try to guess the parameter from pipeline configuration
             for (Parameter parameter : jobNode.getParameter()) {
                 Object value = jobNode.getParameterUnresolved(parameter.getName());
                 if (value == null || ((value instanceof Collection) && ((Collection) value).size() == 0)) {
                     value = pipeline.getConfiguration().get(parameter.getName());
                     if (value != null) {
                         log.warn("Inferring parameter " + parameter.getName() + " for execution " + execution.getId() + " from pipeline configuration");
                         jobNode.getConfiguration().put(parameter.getName(), value);
                     }
                 }
             }
 
             initialNodes.put(execution.getId(), jobNode);
             graph.addVertex(jobNode);
         }
 
         log.debug("Create Pipeline Context");
         context = createContext();
 
 
         // add edges
         log.debug("Adding manual 'after' edges");
         for (PipelineJob execution : pipeline.getExecutions()) {
             // simple manual dependencies
             if (execution.getAfter() != null) {
                 for (String dep : execution.getAfter()) {
                     JobNode parent = initialNodes.get(dep);
                     JobNode child = initialNodes.get(execution.getId());
                     log.debug("Adding After edge " + parent.getPipelineJob().getId() + " -> " + child.getPipelineJob().getId());
                     graph.addEdge(parent, child, new JobEdge(parent.getPipelineJob().getId()));
                 }
             }
         }
 
         log.debug("Adding dependency edges");
         for (JobNode node : graph.vertexSet()) {
             createEdges(node, node.findDependency(this));
         }
 
        log.debug("Adding default output values");
        for (JobNode node : graph.vertexSet()) {
            for (Parameter parameter : node.getParameter()) {
                if(parameter.isOutput() && parameter.getDefaultValue() == null && !parameter.isMandatory()){
                    if(node.getConfiguration().get(parameter.getName()) == null){
                        String output = node.getPipelineJob().getToolId()+"."+node.getNodeId()+"."+parameter.getName();
                        if(parameter.getType() != null){
                            output += "." + parameter.getType();
                        }
                        node.getConfiguration().put(parameter.getName(), output);
                    }
                }
            }
        }
     }
 
     private Object cleanToString(Object value){
         if(value instanceof Collection){
             ArrayList list = new ArrayList();
             for (Object o : ((Collection) value)) {
                 list.add(cleanToString(o));
             }
             return list;
         }
         if(!(value instanceof String)){
             value= value.toString();
         }
         return value;
     }
 
     /**
      * Validates the pipeline structure, ensures that all referenced executables are
      * known and checks uniqueness of execution ids.
      *
      * @return error errors or null
      */
     public List<Error> validatePipeline() {
         ArrayList<Error> errors = new ArrayList<Error>();
         // check ids
         Map<String, PipelineJob> ids = new HashMap<String, PipelineJob>();
         for (PipelineJob execution : pipeline.getExecutions()) {
             String id = execution.getId();
             if (id == null || id.isEmpty())
                 errors.add(new Error("Empty or no id set for execution: " + execution.toString()));
             else {
                 if (ids.containsKey(id)) errors.add(new Error("Duplicated usage of the execution id " + id));
                 else ids.put(id, execution);
             }
         }
 
         return errors;
     }
 
 
     /**
      * Prepare the graph for submission. THis splits many-to-one nodes
      * and configured the properties. You should cal {@link #validate()} afterwards
      * to verify that all mandatory parameters are set.
      */
     public void prepare() {
         splitGraph();
         configureGraph();
 
         // handle pipelines of pipelines for JIP-2
         expandPipelineNodes();
     }
 
     /**
      * Call this after graph preparation to remove all edges that represent transient dependencies
      * For example. If we have A->B->C and A->C, the A->C edge can be removed, C has to wait for B and B already
      * waits for A.
      */
     public void reduceDependencies(){
         for (JobNode node : getNodes()) {
             // create a subgraph that contains the node
             // and all its direct predecessors
             HashSet<JobNode> nodes = new HashSet<JobNode>();
             nodes.add(node);
             for (JobEdge parentEdge : graph.incomingEdgesOf(node)) {
                 nodes.add(graph.getEdgeSource(parentEdge));
             }
 
             if(nodes.size() >= 2){
                 // sort in topological order
                 List<JobNode> ordered = new ArrayList<JobNode>(getNodes());
                 // now, reverse teh topological order, so first element is our target node
                 Collections.reverse(ordered);
                 // and iterate the list. if we find a node that has an edge to the target node,
                 // remove all direct predeccors and keep the edge
                 HashSet<JobEdge> toRemove = new HashSet<JobEdge>();
                 List<JobNode> nodeList = new ArrayList<JobNode>(ordered.subList(ordered.indexOf(node) + 1, ordered.size()));
                 while(nodeList.size() > 0){
                     JobNode next = nodeList.get(0);
                     Set<JobEdge> allEdges1 = graph.getAllEdges(next, node);
                     // remove all but one edge
                     if(allEdges1.size() > 0){
                         int b = 0;
                         for (JobEdge jobEdge : allEdges1) {
                             if(b++ >0) toRemove.add(jobEdge);
                         }
                         removePredecessors(node, nodeList, toRemove, next);
                     }
                     nodeList.remove(next);
                 }
                 // remove all edges that are not keep edges
                 graph.removeAllEdges(toRemove);
             }
         }
     }
 
     private void removePredecessors(JobNode targetNode, List<JobNode> topologicalOrder, HashSet<JobEdge> toRemove, JobNode currentNode) {
         // and remove all predecessors of the current "next" node from the ordered list
         for (JobEdge incoming : graph.incomingEdgesOf(currentNode)) {
             JobNode edgeSource = graph.getEdgeSource(incoming);
             toRemove.addAll(graph.getAllEdges(edgeSource, targetNode));
             topologicalOrder.remove(edgeSource);
             removePredecessors(targetNode, topologicalOrder, toRemove, edgeSource);
         }
     }
 
     /**
      * Find nodes that represent pipelines and expand them
      * NOTE that this can only be called after splitting and configuring the graph
      */
     void expandPipelineNodes() {
         ArrayList<JobNode> toRemove = new ArrayList<JobNode>();
         for (JobNode jobNode : getNodes()) {
             if (jobNode.getPipelineJob().getPipeline() != null) {
                 log.debug("Expanding pipeline node " + jobNode.getNodeId());
 
                 // todo: reanable pipeline embedding
                 Pipeline subPipeline = jobNode.getPipelineJob().getPipeline();
                 //Pipeline subPipeline = null;
 
                 // apply configuration
                 for (Parameter parameter : jobNode.getParameter()) {
                     subPipeline.getConfiguration().put(parameter.getName(), jobNode.getParameter(parameter.getName()));
                 }
 
                 PipelineGraph subgraph = new PipelineGraph(subPipeline);
                 subgraph.prepare();
 
                 // rename and add all nodes
                 for (JobNode node : subgraph.getNodes()) {
                     JobNode clone = new JobNode(node);
                     clone.setNodeId(jobNode.getNodeId() + "_" + node.getNodeId());
                     // update the node directory
                     graph.addVertex(clone);
                 }
 
                 // add all edges
                 for (JobEdge jobEdge : subgraph.getGraph().edgeSet()) {
                     JobNode source = subgraph.getGraph().getEdgeSource(jobEdge);
                     JobNode target = subgraph.getGraph().getEdgeTarget(jobEdge);
 
                     JobNode newSource = findNode(jobNode.getNodeId() + "_" + source.getNodeId());
                     JobNode newTarget = findNode(jobNode.getNodeId() + "_" + target.getNodeId());
 
                     graph.addEdge(newSource, newTarget, new JobEdge(jobEdge.getType(), newSource.getNodeId(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                 }
 
                 // add incoming and outgoing edges
                 // using manual "after" edge
                 // first incoming edges
                 //
                 // first find all nodes in the subgraph that have no
                 // incoming edges or outgoing edges
                 List<JobNode> noIncomingEdges = new ArrayList<JobNode>();
                 List<JobNode> noOutgoingEdges = new ArrayList<JobNode>();
                 for (JobNode node : subgraph.getNodes()) {
                     if (subgraph.getGraph().inDegreeOf(node) == 0) {
                         noIncomingEdges.add(node);
                     }
                     if (subgraph.getGraph().outDegreeOf(node) == 0) {
                         noOutgoingEdges.add(node);
                     }
                 }
                 // connect incomings
                 for (JobEdge jobEdge : graph.incomingEdgesOf(jobNode)) {
                     JobNode source = graph.getEdgeSource(jobEdge);
                     for (JobNode noIncomingEdge : noIncomingEdges) {
                         JobNode target = findNode(jobNode.getNodeId() + "_" + noIncomingEdge.getNodeId());
                         graph.addEdge(source, target, new JobEdge(source.getNodeId()));
                     }
                 }
                 // connect outgoings
                 for (JobEdge jobEdge : graph.outgoingEdgesOf(jobNode)) {
                     JobNode target = graph.getEdgeTarget(jobEdge);
                     for (JobNode sourceNode : noOutgoingEdges) {
                         JobNode source = findNode(jobNode.getNodeId() + "_" + sourceNode.getNodeId());
                         graph.addEdge(source, target, new JobEdge(source.getNodeId()));
                     }
                 }
 
                 toRemove.add(jobNode);
             }
         }
         // remove old nodes
         for (JobNode jobNode : toRemove) {
             graph.removeVertex(jobNode);
         }
     }
 
 
     /**
      * Transfer global configuration to nodes and transfer edge configurations
      */
     void configureGraph() {
         // apply global configuration
         transferPipelineConfiguration();
         // iterate over edges and apply edge values
         for (JobEdge edge : graph.edgeSet()) {
             if (edge.getType() != JobEdge.Type.After) {
                 edge.applyConfiguration(this);
             }
         }
 
         // validate output
         for (JobNode node : getNodes()) {
             for (Parameter parameter : node.getParameter()) {
                 if (!parameter.isOutput() || !parameter.isMandatory()) continue;
                 Object outputvalue = node.getConfiguration().get(parameter.getName());
                 if (outputvalue == null){
                     Object pv = node.getParameter(parameter.getName());
                     if(pv == null)
                         throw new NullPointerException("No Output defined for " + node.getNodeId());
                     else{
                         outputvalue = pv;
                         node.getConfiguration().put(parameter.getName(), pv);
                     }
                 }
                 if (parameter.isList() && !(outputvalue instanceof List)) {
                     node.getConfiguration().put(parameter.getName(), Arrays.asList(outputvalue));
                 } else if (!parameter.isList() && outputvalue instanceof List) {
                     // find the index
                     int i = -1;
                     for (JobEdge jobEdge : graph.incomingEdgesOf(node)) {
                         if (jobEdge.getIndex() >= 0) {
                             if (i < 0) i = jobEdge.getIndex();
                             else if (i != jobEdge.getIndex())
                                 throw new IllegalArgumentException("Unresolvable output index mapping after node split " + node.getNodeId());
                         }
                     }
                     if (i < 0) {
                         throw new IllegalArgumentException("Unresolvable output index mapping after node split " + node.getNodeId());
                     }
                     node.getConfiguration().put(parameter.getName(), ((List) outputvalue).get(i));
                 }
             }
         }
 
 
         // resolve missing config properties
 //        for (JobNode splits : getNodes()) {
 //            for (Parameter parameter : splits.getParameter()) {
 //                PipelineContext.Entry parameterValue = resolve("${" + splits.getNodeId() + ".configuration." + parameter.getName() + "}");
 //                if (parameterValue == null || parameterValue.getValue() == null) {
 //                    continue;
 //                }
 //                Object value = parameterValue.getValue();
 //                if(value != null){
 //                    splits.loadConfiguration().put(parameter.getName(), value);
 //                }
 //
 //                Map<String, Object> outputs = new HashMap<String, Object>();
 //                PipelineContext nodeContext = (PipelineContext) getContext().getValue(splits.getNodeId());
 //                for (String s : nodeContext.keySet()) {
 //                    if(s.equals("configuration")){
 //                        // add config ?
 //
 //                    }else{
 //                        // add output
 //                        outputs.put(s, nodeContext.getValue(s));
 //                    }
 //                }
 //                splits.loadConfiguration().put("output", outputs);
 //            }
 //        }
     }
 
     /**
      * Create a single contexts that covers all variables/configuration and
      * output for all nodes of this graph.
      * <p>
      * Entry points are 'pipeline' to access the global pipeline
      * configuration, an entry for all nodes, based on node id containing
      * the the output of the node and additionally the configuration as ".configuration"
      * </p>
      *
      * @return context the context of this graph
      */
     ScopeNode createContext() {
         ScopeNode rootContext = new ScopeNode("pipeline");
 
         // first the pipeline context
         Map<String, Object> configuration = pipeline.getConfiguration();
         for (String name : configuration.keySet()) {
             // all global configuration is static
             if (!name.contains("${")) {
                 rootContext.createValueFromSource(name, configuration);
             }
         }
         for (Parameter parameter : pipeline.getParameter()) {
             try {
                 rootContext.createValue(parameter.getName(), parameter.getDefaultValue());
             } catch (Exception e) {
             }
         }
 
 
         // now all nodes
         for (JobNode node : getNodes()) {
             ScopeNode ctx = rootContext.createChild(node.getNodeId());
             node.createContext(ctx);
         }
 
         // now resolve the pipeline global context, check for
         // keys referencing keys ( ${target.property} ) and transfer
         // the value. We do this for the pipeline config only!
         for (String name : configuration.keySet()) {
             // all global configuration is static
             if (name.contains("${")) {
                 Object value = configuration.get(name);
                 rootContext.set(name, value);
             }
         }
         return rootContext;
     }
 
     /**
      * Split all nodes that have incoming man-to-one edges
      */
     void splitGraph() {
         log.debug("Splitting Nodes : " + vertexCount());
         for (JobNode jobNode : getNodes()) {
             if (!splitMany2One(jobNode)) {
                 splitOne2One(jobNode);
             }
         }
         log.debug("Nodes splitted : " + vertexCount());
     }
 
     /**
      * Split a single node if it has incoming many-to-one edges
      *
      * @param node the node
      * @return splitted true if splitted
      */
     boolean splitMany2One(JobNode node) {
         Set<JobEdge> incoming = graph.incomingEdgesOf(node);
         Set<JobEdge> outgoing = graph.outgoingEdgesOf(node);
 
         Map<JobEdge, List> many2oneValues = new HashMap<JobEdge, List>();
         //Map<JobEdge, List> mergedMany2oneValues = new HashMap<JobEdge, List>();
         ArrayListMultimap<String, Object> mergedMany2OneValues = ArrayListMultimap.create();
         ArrayListMultimap<String, JobEdge> mergedMany2OneEdges = ArrayListMultimap.create();
         Map<String, List> parameterLists = new HashMap<String, List>();
         List<String> edgeCoveredParameters = new ArrayList<String>();
         int maxSize = -1;
         
         // create merged lists by target value
         for (JobEdge jobEdge : incoming) {
             if (jobEdge.getType() == JobEdge.Type.Many2One) {
                 Object sourceValue = jobEdge.getValue();
                 edgeCoveredParameters.add(jobEdge.getTargetProperty());
                 if (sourceValue != null) {
                     for (Object o : toList(sourceValue)) {
                         mergedMany2OneValues.put(jobEdge.getTargetProperty(), o);
                         mergedMany2OneEdges.put(jobEdge.getTargetProperty(), jobEdge);
                     }
                 }
             }
         }
         for (JobEdge jobEdge : incoming) {
             if (jobEdge.getType() == JobEdge.Type.Many2One) {
                 Object sourceValue = jobEdge.getValue();
                 edgeCoveredParameters.add(jobEdge.getTargetProperty());
                 if (sourceValue != null) {
                     List list = toList(sourceValue);
                     if(mergedMany2OneValues.containsKey(jobEdge.getTargetProperty())){
                         list = mergedMany2OneValues.get(jobEdge.getTargetProperty());
                     }
                     if (maxSize == -1) maxSize = list.size();
                     else {
                         if (maxSize != list.size()) {
                             throw new RuntimeException("Unable to split node " + node + " - source lists have unequal size !");
                         }
                     }
                     many2oneValues.put(jobEdge, list);
                 }
             }
         }
 
         // also collect one-to-one edges
         // we have to resolve them when we split this node
         // one to one edges number has to match the elements number
         Map<String, List<JobEdge>> one2onetargets = new HashMap<String, List<JobEdge>>();
         List<JobEdge> otherEdges = new ArrayList<JobEdge>();
 
         for (JobEdge jobEdge : incoming) {
             if (jobEdge.getType() == JobEdge.Type.One2One) {
                 String target = jobEdge.getTargetProperty();
 
                 List<JobEdge> edges = one2onetargets.get(target);
                 if (edges == null) {
                     edges = new ArrayList<JobEdge>();
                     one2onetargets.put(target, edges);
                 }
                 edges.add(jobEdge);
             } else {
                 if (jobEdge.getType() != JobEdge.Type.Many2One) {
                     otherEdges.add(jobEdge);
                 }
             }
         }
 
         int one2oneCount = -1;
         for (Map.Entry<String, List<JobEdge>> stringListEntry : one2onetargets.entrySet()) {
             List<JobEdge> list = stringListEntry.getValue();
             if (one2oneCount < 1 && list.size() > 1) one2oneCount = list.size();
             else {
                 if (one2oneCount != list.size() && list.size() > 1) {
                     throw new RuntimeException("Unable to split node " + node + " - source node has links to lists and an unequal number of one-to-one edges !");
                 }
             }
         }
 
         String expansionType = null;
         // check configuration parameters that are not covered by incoming edges
         for (Parameter parameter : node.getParameter()) {
             // JIP-3 check the parameter expansion type
             if (expansionType == null) {
                 expansionType = parameter.getExpand();
             } else {
                 if (parameter.getExpand() != null && !expansionType.equals(parameter.getExpand())) {
                     throw new IllegalArgumentException("Mixed expansion types found in " + node.getNodeId() + "! This is not supported. You can use only one expansion type!");
                 }
             }
 
 
             Object value = node.getParameter(parameter.getName());
             if (!edgeCoveredParameters.contains(parameter.getName()) && !parameter.isList() && value != null && value instanceof List) {
                 List list = (List) value;
                 ArrayList flat = new ArrayList(JobEdge.flatten(list));
 
                 if (flat.size() == 1) {
                     //parameterLists.put(parameter.getName(), flat.get(0));
                     // update configuration
                     node.getConfiguration().put(parameter.getName(), flat.get(0));
                 } else {
                     parameterLists.put(parameter.getName(), list);
                     if (maxSize == -1) maxSize = list.size();
                     else {
                         if (maxSize != list.size()) {
                             StringBuilder sum = new StringBuilder();
                             for (Map.Entry<String, List> stringListEntry : parameterLists.entrySet()) {
                                 sum.append(stringListEntry.getKey()).append("\t").append(stringListEntry.getValue().size()).append("\n");
                             }
                             throw new RuntimeException("Unable to split node " + node + " - source lists have unequal size !\nMax Size: "+maxSize+"\n"+sum.toString());
                         }
                     }
                 }
             }
         }
 
 
         //many2oneValues.size() > 0
         if (maxSize == 1) {
             // for one element, just transform the edges to one-to-one with index
             for (JobEdge edge : many2oneValues.keySet()) {
                 edge.setType(JobEdge.Type.One2One);
                 edge.setIndex(0);
             }
             for (String s : parameterLists.keySet()) {
                 node.getConfiguration().put(s, parameterLists.get(s).get(0));
             }
         } else if (maxSize > 1) {
             // create new nodes
 
             boolean isSerialExpansion = expansionType != null && expansionType.equals(Parameter.EXPAND_SERIAL);
             ArrayListMultimap<String, Object> coveredParameters = ArrayListMultimap.create();
             for (int i = 0; i < maxSize; i++) {
                 String suffix = "_split_";
                 // check for expansion type
                 if (isSerialExpansion) {
                     suffix = "_seq_";
                 }
 
                 JobNode splits = new JobNode(node);
                 splits.getConfiguration().clear(); // reset configuration
                 splits.setSplitNode(true);
                 splits.setNodeId(node.getNodeId() + suffix + i);
                 graph.addVertex(splits);
 
 
                 // merge list of lists
                 for (Parameter parameter : node.getParameter()) {
                     Object value = node.getParameter(parameter.getName());
                     if (!parameterLists.containsKey(parameter.getName()) && parameter.isList() && value != null && value instanceof List) {
                         List list = (List) value;
                         if (list.size() > 0) {
                             Object v1 = list.get(0);
                             if (v1 instanceof List && ((List) v1).size() == maxSize) {
                                 // this is a list of lists
                                 ArrayList newValue = new ArrayList();
                                 for (Object o : list) {
                                     List ol = (List) o;
                                     newValue.add(ol.get(i));
                                 }
                                 splits.getConfiguration().put(parameter.getName(), newValue);
 
                             }
                         }
                     }
 
                     // pass on sequential parameters for all but the first node
                     if (isSerialExpansion && i > 0) {
                         if (parameter.getExpandValue() != null) {
                             String p = "${" + node.getNodeId() + suffix + (i - 1) + "." + parameter.getExpandValue() + "}";
                             Object v = getContext().get(p);
                             if (v instanceof List) {
                                 // resolve by index
                                 v = ((List) v).get(i - 1);
                             }
                             splits.getConfiguration().put(parameter.getName(), v);
                         }
                     }
 
                     // update output file names and make them unique within the run
                     if(parameter.isOutput() && parameter.isFile() && value != null){
 
                         String extension = splits.getNodeId();
                         if(parameter.getType() != null){
                             extension+="."+parameter.getType();
                         }
                         if(parameter.isList()){
                             List c = (List) value;
                             for (int j = 0; j < c.size(); j++) {
                                 String current = c.get(j).toString();
                                 FileParameter fileParameter = new FileParameter(current);
                                 if(parameter.getType() != null && fileParameter.getExtension().equals(parameter.getType())){
                                     current = new FileParameter(current).getName();
                                 }
                                 c.set(j, current + "." + extension);
                             }
                         }else{
                             String current = value.toString();
                             FileParameter fileParameter = new FileParameter(current);
                             if(parameter.getType() != null && fileParameter.getExtension().equals(parameter.getType())){
 
                                 current = fileParameter.getName();
                             }
                             splits.getConfiguration().put(parameter.getName(), current + "." + extension);
                         }
                     }
                 }
 
                 // add to context
                 // update configuration
                 for (String key : parameterLists.keySet()) {
                     List data = parameterLists.get(key);
                     splits.getConfiguration().put(key, data.get(i));
                 }
 
                 for (String targetParameter : mergedMany2OneValues.keySet()) {
                     List<Object> data = mergedMany2OneValues.get(targetParameter);
                     splits.getConfiguration().put(targetParameter, data.get(i));
                 }
 
                 // fill all configuration data that come from the
                 // source node and are not affected by list operations
                 // because we cleared the config before
                 for (String key : node.getConfiguration().keySet()) {
                     if(!splits.getConfiguration().containsKey(key)){
                         splits.getConfiguration().put(key, node.getConfiguration().get(key));
                     }
                 }
 
 
                 ScopeNode childCtx = context.createChild(splits.getNodeId());
                 splits.createContext(childCtx);
 
 
 
                 // resolve configuration dependencies
                 Map<String, Object> outputEdgeValues = new HashMap<String, Object>();
                 for (String s : parameterLists.keySet()) {
                     List list = parameterLists.get(s);
                     Object value = list.get(i);
                     if(value != null){
                         splits.getConfiguration().put(s, value);
                         outputEdgeValues.put(s, value);
                     }
                 }
                 for (Parameter parameter : splits.getParameter()) {
                     String sourceContextKey = "${" + splits.getNodeId() + "." + parameter.getName() + "}";
                     Object value = getContext().get(sourceContextKey);
                     if(value != null){
                         splits.getConfiguration().put(parameter.getName(), value);
                     }
                 }
 
                 // add edges
                 List<String> coveredProperties = new ArrayList<String>();
                 for (JobEdge jobEdge : incoming) {
                     if (jobEdge.getType() == JobEdge.Type.Many2One) {
                         jobEdge = mergedMany2OneEdges.get(jobEdge.getTargetProperty()).get(i);
                         if(coveredProperties.contains(jobEdge.getTargetProperty())) continue;
 //                        List list = toList(jobEdge.getValue());
                         List list = mergedMany2OneValues.get(jobEdge.getTargetProperty());
                         if(list == null || list.size() == 0){
                             list = toList(jobEdge.getValue());
                         }
                         coveredProperties.add(jobEdge.getTargetProperty());
                         // apply directory configuration to values
                         Object edgevalue = list.get(i);
                         // only for file paramters
 
                         JobNode sourceNode;
                         if(mergedMany2OneEdges.containsKey(jobEdge.getTargetProperty())){
                             sourceNode = findNode(mergedMany2OneEdges.get(jobEdge.getTargetProperty()).get(i).getSourceNode());
                         }else{
                             sourceNode = findNode(jobEdge.getSourceNode());
                         }
                         Parameter parameter = sourceNode.getParameterRaw(jobEdge.getSourceProperty());
                         if(parameter != null && parameter.isFile()){
                             edgevalue = jobEdge.addDirectoryToFiles(sourceNode, edgevalue);
                         }
                         JobEdge newEdge = new JobEdge(JobEdge.Type.One2One, sourceNode.getNodeId(), jobEdge.getTargetProperty(), edgevalue, jobEdge.getSourceProperty());
                         coveredParameters.put(jobEdge.getTargetProperty(), "${"+sourceNode.getNodeId()+"."+jobEdge.getSourceProperty()+"}");
 
                         newEdge.setSourceNode(sourceNode.getNodeId());
                         newEdge.setIndex(i);
                         graph.addEdge(sourceNode, splits, newEdge);
                     } else {
                         // just relink if its in other edges
                         if (otherEdges.contains(jobEdge)) {
                             graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                         }
                     }
                 }
 
                 // pick a one-to-one link
                 if (one2onetargets.size() > 0) {
                     for (Map.Entry<String, List<JobEdge>> one2oneEntry : one2onetargets.entrySet()) {
                         List<JobEdge> edges = one2oneEntry.getValue();
                         if (edges.size() > 1) {
                             // select one edge
                             JobEdge jobEdge = edges.get(i);
                             graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                         } else {
                             // single link, we can just add this one
                             JobEdge jobEdge = edges.get(0);
                             graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                         }
                     }
                 }
 
 
                 // add outgoing edges
                 for (JobEdge jobEdge : outgoing) {
                     // only add outgoing edges if this is
                     // no serial expansion or this is the
                     // final node of the expansion
 //                    if(!isSerialExpansion || i == maxSize-1 ){
                     //Object value = outputEdgeValues.get(jobEdge.getValue());
                     Object value = outputEdgeValues.get(jobEdge.getSourceProperty());
                     if (value == null){
 //                        value = jobEdge.getValue();
                         value = splits.getParameter(jobEdge.getSourceProperty());
                         if(value == null){
                             value = jobEdge.getValue();
                         }
                     }
                     graph.addEdge(splits, graph.getEdgeTarget(jobEdge), new JobEdge(jobEdge.getType(), splits.getNodeId(), jobEdge.getTargetProperty(), value, jobEdge.getSourceProperty()));
 //                    }
                 }
 
                 // add manual after dep edges for sequential execution
                 if (isSerialExpansion && i > 0) {
                     JobNode source = findNode(node.getNodeId() + suffix + (i - 1));
                     graph.addEdge(source, splits, new JobEdge(source.getNodeId()));
                 }
             }
 
             /// add split nodes for parameter values that are node covered by the edges
             for (String pv : coveredParameters.keySet()) {
                 List<Object> allValues = coveredParameters.get(pv);
                 Object nodeValues = node.getParameter(pv);
                 if(nodeValues instanceof Collection){
                     ArrayList vs = new ArrayList((Collection)nodeValues);
                     vs.removeAll(allValues);
                     for (Object nonEdgeValue : vs){
                         int index = maxSize++;
                         String suffix = "_split_";
                         // check for expansion type
                         if (isSerialExpansion) {
                             suffix = "_seq_";
                         }
 
                         JobNode splits = new JobNode(node);
                         splits.getConfiguration().clear(); // reset configuration
                         splits.setSplitNode(true);
                         splits.setNodeId(node.getNodeId() + suffix + index);
                         graph.addVertex(splits);
 
 
                         // merge list of lists
                         for (Parameter parameter : node.getParameter()) {
                             Object value = node.getParameter(parameter.getName());
                             if (!parameterLists.containsKey(parameter.getName()) && parameter.isList() && value != null && value instanceof List) {
                                 List list = (List) value;
                                 if (list.size() > 0) {
                                     Object v1 = list.get(0);
                                     if (v1 instanceof List && ((List) v1).size() == maxSize) {
                                         // this is a list of lists
                                         ArrayList newValue = new ArrayList();
                                         for (Object o : list) {
                                             List ol = (List) o;
                                             newValue.add(ol.get(index));
                                         }
                                         splits.getConfiguration().put(parameter.getName(), newValue);
 
                                     }
                                 }
                             }
 
                             // pass on sequential parameters for all but the first node
                             if (isSerialExpansion && index > 0) {
                                 if (parameter.getExpandValue() != null) {
                                     String p = "${" + node.getNodeId() + suffix + (index - 1) + "." + parameter.getExpandValue() + "}";
                                     Object v = getContext().get(p);
                                     if (v instanceof List) {
                                         // resolve by index
                                         v = ((List) v).get(index - 1);
                                     }
                                     splits.getConfiguration().put(parameter.getName(), v);
                                 }
                             }
                         }
 
                         // add to contect
                         // update configuration
                         for (String key : parameterLists.keySet()) {
                             List data = parameterLists.get(key);
                             splits.getConfiguration().put(key, data.get(index));
                         }
 
                         for (String targetParameter : mergedMany2OneValues.keySet()) {
                             if(!targetParameter.equals(pv)){
                                 List<Object> data = mergedMany2OneValues.get(targetParameter);
                                 splits.getConfiguration().put(targetParameter, data.get(index));
                             }else{
                                 splits.getConfiguration().put(targetParameter, nonEdgeValue);
                             }
                         }
 
                         // fill all configuration data that come from the
                         // source node and are not affected by list operations
                         // because we cleared the config before
                         for (String key : node.getConfiguration().keySet()) {
                             if(!splits.getConfiguration().containsKey(key)){
                                 splits.getConfiguration().put(key, node.getConfiguration().get(key));
                             }
                         }
 
 
                         ScopeNode childCtx = context.createChild(splits.getNodeId());
                         splits.createContext(childCtx);
 
 
 
                         // resolve configuration dependencies
                         Map<String, Object> outputEdgeValues = new HashMap<String, Object>();
                         for (String s : parameterLists.keySet()) {
                             List list = parameterLists.get(s);
                             Object value = list.get(index);
                             if(value != null){
                                 splits.getConfiguration().put(s, value);
                                 outputEdgeValues.put(s, value);
                             }
                         }
                         for (Parameter parameter : splits.getParameter()) {
                             String sourceContextKey = "${" + splits.getNodeId() + "." + parameter.getName() + "}";
                             Object value = getContext().get(sourceContextKey);
                             if(value != null){
                                 splits.getConfiguration().put(parameter.getName(), value);
                             }
                         }
 
                         // add edges
                         List<String> coveredProperties = new ArrayList<String>();
                         for (JobEdge jobEdge : incoming) {
                             if (jobEdge.getType() == JobEdge.Type.Many2One) {
                                 if(coveredProperties.contains(jobEdge.getTargetProperty())) continue;
 //                        List list = toList(jobEdge.getValue());
                                 List list = mergedMany2OneValues.get(jobEdge.getTargetProperty());
                                 if(list == null || list.size() == 0){
                                     list = toList(jobEdge.getValue());
                                     mergedMany2OneValues.put(jobEdge.getTargetProperty(), list);
                                 }
                                 coveredProperties.add(jobEdge.getTargetProperty());
                                 // apply directory configuration to values
                                 // only for file paramters
 
                                 JobNode sourceNode;
                                 if(mergedMany2OneEdges.containsKey(jobEdge.getTargetProperty()) && !jobEdge.getTargetProperty().equals(pv)){
                                     sourceNode = findNode(mergedMany2OneEdges.get(jobEdge.getTargetProperty()).get(index).getSourceNode());
                                 }else{
                                     if(!jobEdge.getTargetProperty().equals(pv)){
                                         sourceNode = findNode(jobEdge.getSourceNode());
                                     }else{
                                         // there is no edge
                                         sourceNode = null;
                                     }
                                 }
                                 if(sourceNode != null){
                                     JobEdge newEdge = new JobEdge(JobEdge.Type.One2One, sourceNode.getNodeId(), jobEdge.getTargetProperty(), nonEdgeValue, jobEdge.getSourceProperty());
                                     newEdge.setSourceNode(sourceNode.getNodeId());
                                     newEdge.setIndex(index);
                                     graph.addEdge(sourceNode, splits, newEdge);
                                 }
                             } else {
                                 // just relink if its in other edges
                                 if (otherEdges.contains(jobEdge)) {
                                     graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                                 }
                             }
                         }
 
                         // pick a one-to-one link
                         if (one2onetargets.size() > 0) {
                             for (Map.Entry<String, List<JobEdge>> one2oneEntry : one2onetargets.entrySet()) {
                                 List<JobEdge> edges = one2oneEntry.getValue();
                                 if (edges.size() > 1) {
                                     // select one edge
                                     JobEdge jobEdge = edges.get(index);
                                     graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                                 } else {
                                     // single link, we can just add this one
                                     JobEdge jobEdge = edges.get(0);
                                     graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), jobEdge.getValue(), jobEdge.getSourceProperty()));
                                 }
                             }
                         }
 
 
                         // add outgoing edges
                         for (JobEdge jobEdge : outgoing) {
                             // only add outgoing edges if this is
                             // no serial expansion or this is the
                             // final node of the expansion
 //                    if(!isSerialExpansion || i == maxSize-1 ){
                             //Object value = outputEdgeValues.get(jobEdge.getValue());
                             Object value = outputEdgeValues.get(jobEdge.getSourceProperty());
                             if (value == null){
 //                        value = jobEdge.getValue();
                                 value = splits.getParameter(jobEdge.getSourceProperty());
                                 if(value == null){
                                     value = jobEdge.getValue();
                                 }
                             }
                             graph.addEdge(splits, graph.getEdgeTarget(jobEdge), new JobEdge(jobEdge.getType(), splits.getNodeId(), jobEdge.getTargetProperty(), value, jobEdge.getSourceProperty()));
 //                    }
                         }
 
                         // add manual after dep edges for sequential execution
                         if (isSerialExpansion && index > 0) {
                             JobNode source = findNode(node.getNodeId() + suffix + (index - 1));
                             graph.addEdge(source, splits, new JobEdge(source.getNodeId()));
                         }
                     }
                 }
 
             }
 
             // remove old node
             graph.removeVertex(node);
             return true;
         }
         return false;
     }
 
     private List toList(Object sourceValue) {
         List list = null;
         if (sourceValue instanceof Collection) {
             if (sourceValue instanceof List) {
                 list = (List) sourceValue;
             } else {
                 list = new ArrayList(((Collection) sourceValue));
             }
         } else {
             list = new ArrayList();
             list.add(sourceValue);
         }
         return new ArrayList(JobEdge.flatten(list));
     }
 
     /**
      * Split a single node if it has multiple incoming one-to-one edges for the same target property
      *
      * @param node the node
      * @return splitted true if splitted
      */
     boolean splitOne2One(JobNode node) {
         Set<JobEdge> incoming = graph.incomingEdgesOf(node);
         Set<JobEdge> outgoing = graph.outgoingEdgesOf(node);
         Map<String, List> parameterLists = new HashMap<String, List>();
         Map<String, Boolean> otherEdgeParams = new HashMap<String, Boolean>();
 
         Map<String, List<JobEdge>> one2onetargets = new HashMap<String, List<JobEdge>>();
         List<JobEdge> otherEdges = new ArrayList<JobEdge>();
         for (JobEdge jobEdge : incoming) {
             if (jobEdge.getType() == JobEdge.Type.One2One) {
                 String target = jobEdge.getTargetProperty();
 
                 List<JobEdge> edges = one2onetargets.get(target);
                 if (edges == null) {
                     edges = new ArrayList<JobEdge>();
                     one2onetargets.put(target, edges);
                 }
                 edges.add(jobEdge);
             } else {
                 otherEdges.add(jobEdge);
                 otherEdgeParams.put(jobEdge.getSourceProperty(), true);
             }
         }
 
         int size = -1;
         for (Map.Entry<String, List<JobEdge>> entry : one2onetargets.entrySet()) {
             if (size < 0 && entry.getValue().size() > 1) size = entry.getValue().size();
             else {
                 if (size != entry.getValue().size() && entry.getValue().size() > 1) {
                     throw new RuntimeException("Unequal number of target specific one-to-one edges, this split is currently not supported !");
                 }
             }
         }
 
         // check configuration parameters that are not covered by incoming edges
         for (Parameter parameter : node.getParameter()) {
             Object value = node.getParameter(parameter.getName());
             if (otherEdgeParams.containsKey(parameter.getName()) && !parameter.isList() && value != null && value instanceof List) {
                 List list = (List) value;
                 ArrayList flat = new ArrayList(JobEdge.flatten(list));
 
                 if (flat.size() == 1) {
                     //parameterLists.put(parameter.getName(), flat.get(0));
                     // update configuration
                     node.getConfiguration().put(parameter.getName(), flat.get(0));
                 } else {
                     parameterLists.put(parameter.getName(), list);
                 }
             }
         }
 
         if (size > 1) {
             // create new nodes
             for (int i = 0; i < size; i++) {
                 JobNode splits = new JobNode(node);
                 splits.getConfiguration().clear();
                 splits.setSplitNode(true);
                 splits.setNodeId(node.getNodeId() + "_split_" + i);
                 graph.addVertex(splits);
 
                 ScopeNode ctx = context.createChild(splits.getNodeId());
                 splits.createContext(ctx);
                 //context.addEntry(splits.getNodeId(), nodeContext);
 
                 Map<String, Object> outputEdgeValues = new HashMap<String, Object>();
                 for (String s : parameterLists.keySet()) {
                     List list = parameterLists.get(s);
                     Object value = list.get(i);
                     splits.getConfiguration().put(s, value);
                     outputEdgeValues.put(s, value);
                 }
 
 
                 // add edges
                 for (Map.Entry<String, List<JobEdge>> entry : one2onetargets.entrySet()) {
                     List<JobEdge> list = entry.getValue();
                     JobEdge jobEdge = list.get(list.size() == 1 ? 0 : i);
                     JobNode sourceNode = graph.getEdgeSource(jobEdge);
                     String sourceNodeId = sourceNode.getNodeId();
 
                     Object original = jobEdge.getValue();
                     boolean listParam = false;
                     for (Parameter parameter : splits.getParameter()) {
                         if (parameter.getName().equals(jobEdge.getTargetProperty())) {
                             listParam = parameter.isList();
                             break;
                         }
                     }
 
 
                     if (original instanceof List && !listParam) {
                         original = ((List) original).get(i);
                     }
                     JobEdge newEdge = new JobEdge(JobEdge.Type.One2One, sourceNodeId, jobEdge.getTargetProperty(), original, jobEdge.getSourceProperty());
                     newEdge.setSourceNode(sourceNode.getNodeId());
                     newEdge.setIndex(i);
                     graph.addEdge(sourceNode, splits, newEdge);
                 }
 
                 for (JobEdge jobEdge : otherEdges) {
                     Object value = outputEdgeValues.get(jobEdge.getSourceProperty());
                     if (value == null){
 //                        value = jobEdge.getValue();
                         value = splits.getParameter(jobEdge.getSourceProperty());
                         if(value == null){
                             value = jobEdge.getValue();
                         }
                     }
 
                     // just relink
                     graph.addEdge(graph.getEdgeSource(jobEdge), splits, new JobEdge(jobEdge.getType(), jobEdge.getSourceNode(), jobEdge.getTargetProperty(), value, jobEdge.getSourceProperty()));
                 }
                 // add outgoing edges
                 for (JobEdge jobEdge : outgoing) {
 
                     // update values ?
                     Object value = outputEdgeValues.get(jobEdge.getSourceProperty());
                     if (value == null){
 //                        value = jobEdge.getValue();
                         value = splits.getParameter(jobEdge.getSourceProperty());
                         if(value == null){
                             value = jobEdge.getValue();
                         }
                     }
 
 
 
 
                     graph.addEdge(splits, graph.getEdgeTarget(jobEdge), new JobEdge(jobEdge.getType(), splits.getNodeId(), jobEdge.getTargetProperty(), value, jobEdge.getSourceProperty()));
                 }
             }
             // remove old node
             graph.removeVertex(node);
             return true;
 
         }
         return false;
     }
 
 
     /**
      * Helper to create edges for relationship types
      *
      * @param node  the target node
      * @param edges the edges
      */
     private void createEdges(JobNode node, List<JobEdge> edges) {
         for (JobEdge edge : edges) {
             JobNode sourceNode = findNode(edge.getSourceNode());
             if (sourceNode == null) {
                 throw new NullPointerException("Unknown node " + edge.getSourceNode());
             }
             log.debug("Adding " + edge.getType() + " edge " + sourceNode.getNodeId() + " -> " + node.getNodeId() + " for property " + edge.getTargetProperty() + " : value " + edge.getValue());
             graph.addEdge(sourceNode, node, edge);
         }
     }
 
     /**
      * If the pipeline is defined and has a custom configuration, transfer
      * that to the nodes
      */
     void transferPipelineConfiguration() {
 //        PipelineContext pctx = (PipelineContext) context.getValue("pipeline");
 //        for (String key : pctx.keySet()) {
 //            PipelineContext.Entry entry = pctx.getEntry(key);
 //            System.out.println("Found " + entry);
 //        }
 
 
 //        if (pipeline != null && pipeline.loadConfiguration() != null) {
 //            // apply configuration to initial nodes
 //            for (Map.Entry<String, ?> entry : pipeline.loadConfiguration().entrySet()) {
 //                String key = entry.getKey();
 //                Object value = entry.getValue();
 //                PipelineContext.Entry matchedEntry = resolve(key);
 //                if(matchedEntry != null){
 //                    System.out.println("Found ?! ");
 //                }
 //
 //                Matcher matcher = CONFIG_SPLITTER.matcher(key);
 //                if (matcher.matches()) {
 //                    // find the node
 //                    JobNode node = findNode(matcher.group(1));
 //                    if (node != null) {
 //                        node.loadConfiguration().put(matcher.group(2), value);
 //                    } else {
 //                        log.warn("Configuration reverences node " + matcher.group(1) + ", but the node was not found in the graph!");
 //                    }
 //                }
 //            }
 //        }
     }
 
     /**
      * Validate the graph parameters and return a list of invalid or unset mandatory fields, otherwise return null
      *
      * @return error errors or empty list
      */
     public List<Error> validate() {
         ArrayList<Error> errors = new ArrayList<Error>();
 
         // check pipeline parameters
         for (Parameter parameter : pipeline.getParameter()) {
             if (parameter.isMandatory()) {
                 Object value = pipeline.getConfiguration().get(parameter.getName());
                 if (value == null) {
                     // cehck default
                     value = parameter.getDefaultValue();
                 }
                 if (value == null || ((value instanceof Collection) && ((Collection) value).size() == 0)) {
                     // null value is not allowed for this parameter
                     errors.add(new Error("Parameter " + parameter.getName() + " is not set but is marked as mandatory!"));
                 }
 
             }
         }
 
         for (JobNode node : graph.vertexSet()) {
             for (Parameter parameter : node.getPipelineJob().getParameters()) {
                 if (parameter.isMandatory()) {
                     // check that a value is set
                     Object value = node.getParameter(parameter.getName());
                     if (value == null || ((value instanceof Collection) && ((Collection) value).size() == 0)) {
                         // null value is not allowed for this parameter
                         errors.add(new Error("Parameter " + parameter.getName() + " for node " + node.getPipelineJob().getId() + " is not set but is marked as mandatory!"));
                     }
                 }
             }
         }
         return errors;
     }
 
     /**
      * Return the number of nodes in this graph
      *
      * @return vertices the number of vertices in this graph
      */
     public int vertexCount() {
         return graph.vertexSet().size();
     }
 
     /**
      * Get the pipeline associated with this graph
      *
      * @return the graph
      */
     public Pipeline getPipeline() {
         return pipeline;
     }
 
     /**
      * Access to the graph
      *
      * @return graph the graph
      */
     public DirectedGraph<JobNode, JobEdge> getGraph() {
         return graph;
     }
 
 
     /**
      * Get nodes in topological order
      *
      * @return nodes nodes in topological order
      */
     public List<JobNode> getNodes() {
         if(graph.vertexSet().size() == 0) return Collections.emptyList();
         TopologicalOrderIterator<JobNode, JobEdge> topologicalSort = new TopologicalOrderIterator<JobNode, JobEdge>(graph);
         List<JobNode> ordered = new ArrayList<JobNode>();
         while (topologicalSort.hasNext()) {
             ordered.add(topologicalSort.next());
         }
         return ordered;
     }
 
     /**
      * Find a node by its node ID
      *
      * @param nodeId the node id
      * @return node the node or null
      */
     public JobNode findNode(String nodeId) {
         for (JobNode jobNode : graph.vertexSet()) {
             if (jobNode.getNodeId().equals(nodeId)) {
                 return jobNode;
             }
         }
         return null;
     }
 
     /**
      * If the vairable matches {@code ${node.property}}, this finds the node and returns it
      *
      * @param variable the variable
      * @return array null or 2 element array where first element is the node id and second is the source property name
      */
     String[] findNodeByVariable(String variable) {
         if (variable == null) return null;
         Matcher matcher = CONFIG_SPLITTER.matcher(variable);
         if (matcher.matches()) {
             String nodeId = matcher.group(1);
             String sourceProperty = matcher.group(2);
             return new String[]{nodeId, sourceProperty};
         }
         return null;
     }
 
     /**
      * Resolve a key from the pipeline graph context
      *
      * @param key the key
      * @return value the value
      */
     Object resolve(String key) {
         if (context == null) {
             context = createContext();
         }
         return context.get(key);
     }
 
 
     public ScopeNode getContext() {
         return context;
     }
 }
