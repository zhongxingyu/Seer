 /*
  * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Mateusz Parzonka - initial API and implementation
  */
 package prm4jeval;
 
 import java.io.File;
 import java.util.Set;
 
 import static java.util.Arrays.asList;
 
 import com.google.common.collect.ArrayTable;
 
 /**
  * Stores the evaluation data for all invocations. This object tries to load its data at the beginning, and serializes
  * its data state at the end of each invocation.
  */
 public class EvaluationData {
 
     private final static String[] benchmarks = { "avrora", "batik", "eclipse", "fop", "h2", "jython", "luindex", "lusearch",
 	    "pmd", "sunflow", "tomcat", "tradebeans", "tradesoap", "xalan" };
    private final static String[] parametricProperties = { "Pure", "HasNext", "UnsafeIterator", "UnsafeMapIterator",
	    "SafeSyncCollection", "SafeSyncMap" };
 
     private final String filePath;
     private final ArrayTable<String, String, SteadyStateEvaluation> data;
 
     /**
      * @param filePath
      *            the location where the evaluation object is written to.
      */
     public EvaluationData(String filePath) {
 	this.filePath = filePath;
 	final File file = new File(filePath);
 	if (file.exists()) {
 	    if (!file.isDirectory()) {
 		data = loadEvaluationData();
 	    } else {
 		throw new IllegalArgumentException("There exists a directory at given filepath!");
 	    }
 	} else {
 	    data = ArrayTable.create(asList(benchmarks), asList(parametricProperties));
 	}
     }
 
     public Set<String> getBenchmarks() {
 	return data.rowKeySet();
     }
 
     public Set<String> getParametricProperties() {
 	return data.columnKeySet();
     }
 
     public SteadyStateEvaluation getSteadyStateEvalation(String benchmark, String parametricProperty) {
 	if (!getBenchmarks().contains(benchmark))
 	    throw new IllegalArgumentException("Benchmark not known: " + benchmark);
 	if (!getParametricProperties().contains(parametricProperty))
 	    throw new IllegalArgumentException("Parametric property not known: " + parametricProperty);
 	SteadyStateEvaluation sse = data.get(benchmark, parametricProperty);
 	if (sse == null) {
 	    sse = new SteadyStateEvaluation();
 	    data.put(benchmark, parametricProperty, sse);
 	    System.out.println("[prm4jeval] Beginning evaluation of " + benchmark + "-" + parametricProperty);
 	} else {
 	    System.out.println("[prm4jeval] Continuing evaluation of " + benchmark + "-" + parametricProperty);
 	}
 	return sse;
     }
 
     public void storeEvaluationData() {
 	SerializationUtils.serializeToFile(data, filePath);
     }
 
     @SuppressWarnings("unchecked")
     public ArrayTable<String, String, SteadyStateEvaluation> loadEvaluationData() {
 	return (ArrayTable<String, String, SteadyStateEvaluation>) SerializationUtils.deserializeFromFile(filePath);
     }
 
 }
