 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  * 
  * @author dmyersturnbull
  */
 package org.structnetalign.weight;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.structnetalign.CleverGraph;
 import org.structnetalign.HomologyEdge;
 import org.structnetalign.PipelineProperties;
 import org.structnetalign.ReportGenerator;
 
 import edu.uci.ics.jung.graph.util.Pair;
 
 public class SmarterWeightManager implements WeightManager {
 
 	private static final Logger logger = LogManager.getLogger("org.structnetalign");
 
 	private WeightCreator creator;
 
 	private int nCores;
 
 	public SmarterWeightManager(WeightCreator creator, int nCores) {
 		super();
 		this.creator = creator;
 		this.nCores = nCores;
 	}
 
 	@Override
 	public void assignWeights(CleverGraph graph, Map<Integer, String> uniProtIds) {
 
 		if (ReportGenerator.getInstance() != null) {
 			ReportGenerator.getInstance().putInWeighted("manager", this.getClass().getSimpleName());
 		}
 
 		// make a thread pool
 		logger.info("Starting weight assignment with " + nCores + " cores");
 		ExecutorService pool = Executors.newFixedThreadPool(nCores);
 
 		try {
 
 			CompletionService<WeightResult> completion = new ExecutorCompletionService<>(pool);
 			List<Future<WeightResult>> futures = new ArrayList<>();
 			Map<Pair<Integer>,Integer> nAttempted = new HashMap<>();
 
 			// let's submit the jobs
 			// iterate over all pairs of vertices
 			for (int a : graph.getVertices()) {
 				for (int b : graph.getVertices()) {
 
 					if (a >= b) {
 						continue; // homology had damn well better be reflexive and symmetric!
 					}
 
 					nAttempted.put(new Pair<Integer>(a, b), 0);
 
 					final String uniProtIdA = uniProtIds.get(a);
 					final String uniProtIdB = uniProtIds.get(b);
 
 					if (uniProtIdA == null) {
 						logger.error("Could not get UniProt Id for Id#" + a);
 						continue;
 					}
 					if (uniProtIdB == null) {
 						logger.error("Could not get UniProt Id for Id#" + b);
 						continue;
 					}
 
 					logger.trace("Weighting " + uniProtIdA + " against " + uniProtIdB + " (" + a + ", " + b + ")");
 
 					Weight weight = creator.nextWeight(a, b, uniProtIdA, uniProtIdB, 0);
 					Future<WeightResult> future = completion.submit(weight);
 					futures.add(future);
 					logger.debug("Running relation " + weight.getClass().getSimpleName() + " for " + uniProtIdA
 							+ " against " + uniProtIdB + " (" + a + ", " + b + ")");
 
 				}
 			}
 
 			logger.info("Submitted " + futures.size() + " jobs to " + nCores + " cores");
 
 			/*
 			 *  Now respond to completion.
 			 */
 
 			int nUpdates = 0;
 
 			int createdIndex = 0; // there shouldn't be any homology edges yet
			forfutures: for (Future<WeightResult> future : futures) {
 
 				WeightResult result = null;
 				try {
 
 					// We should do this in case the job gets interrupted
 					// Sometimes the OS or JVM might do this
 					// Use the flag instead of future == null because future.get() may actually return null
 					while (result == null) {
 						try {
 							result = future.get();
 							double prob = result.getWeight();
 							int vertexA = result.getV1();
 							int vertexB = result.getV2();
 							logger.trace("Job (" + vertexA + ", " + vertexB + ") returned with weight "
 									+ PipelineProperties.getInstance().getOutputFormatter().format(prob));
 							if (prob == 0) {
 								continue forfutures; // don't both updating with 0
 							}
 						} catch (InterruptedException e1) {
 							logger.warn("A thread was interrupted while waiting to get a weight. Retrying.", e1);
 						}
 					}
 
 				} catch (ExecutionException e) {
 
 					if (e.getCause() != null && e.getCause() instanceof WeightException) {
 
 						WeightException myE = (WeightException) e.getCause();
 						int a = myE.getA();
 						int b = myE.getB();
 						String uniProtIdA = myE.getUniProtIdA();
 						String uniProtIdB = myE.getUniProtIdB();
 						int n = nAttempted.get(new Pair<Integer>(myE.getA(), myE.getB())) + 1;
 						nAttempted.put(new Pair<Integer>(myE.getA(), myE.getB()), n);
 
 						Weight weight = creator.nextWeight(a, b, uniProtIdA, uniProtIdB, n);
 						if (weight != null) { // null means "we're done"
 							Future<WeightResult> newFuture = completion.submit(weight);
 							futures.add(newFuture);
 							logger.debug("Running relation " + weight.getClass().getSimpleName() + " for " + uniProtIdA
 									+ " against " + uniProtIdB + " (" + a + ", " + b + ")");
 						}
 
 					} else {
 						logger.error("Encountered an unknown error trying to get a weight.", e);
 					}
 
 					continue; // we can't process this job
 
 				}
 
 				// everything is ok; now update or add the edge
 
 				int a = result.getV1();
 				int b = result.getV2();
 				double prob = result.getWeight();
 
 				// the creator might want to add another even if it didn't fail
 				int n = nAttempted.get(new Pair<Integer>(a, b)) + 1;
 				nAttempted.put(new Pair<Integer>(a, b), n);
 				Weight weight = creator.nextWeight(a, b, result.getA(), result.getB(), n);
 				if (weight != null) { // null means "we're done"
 					Future<WeightResult> newFuture = completion.submit(weight);
 					futures.add(newFuture);
 					logger.debug("Running relation " + weight.getClass().getSimpleName() + " for " + result.getA()
 							+ " against " + result.getB() + " (" + a + ", " + b + ")");
 				}
 
 				Collection<Integer> vertices = Arrays.asList(a, b);
 
 				// there may already be an edge there
 				HomologyEdge existing = graph.getHomology().findEdge(a, b);
 				if (existing != null) {
 					// (a+b-ab) + c - c*(a+b-ab) = a + b + c - ab - ac - bc + abc
 					existing.setWeight(existing.getWeight() + prob - existing.getWeight() * prob);
 					logger.debug("[" + (double) createdIndex / (double) (futures.size() + createdIndex)
 							+ "] Updated homology edge (" + a + ", " + b + ", "
 							+ PipelineProperties.getInstance().getOutputFormatter().format(existing.getWeight())
 							+ ") with weight " + PipelineProperties.getInstance().getOutputFormatter().format(prob));
 				} else {
 					HomologyEdge edge = new HomologyEdge(createdIndex++, prob);
 					graph.addHomologies(edge, vertices);
 					logger.debug("Added homology edge (" + a + ", " + b + ", "
 							+ PipelineProperties.getInstance().getOutputFormatter().format(prob) + ")");
 				}
 				nUpdates++;
 
 			}
 			logger.info("Added " + graph.getHomologyCount() + " homology edges");
 			if (ReportGenerator.getInstance() != null) {
 				ReportGenerator.getInstance().putInWeighted("n_updates", nUpdates);
 			}
 
 			int maxHomologyDegree = 0;
 			for (int v : graph.getVertices()) {
 				int x = graph.getHomology().getIncidentEdges(v).size();
 				if (x > maxHomologyDegree) maxHomologyDegree = x;
 			}
 			if (ReportGenerator.getInstance() != null) {
 				ReportGenerator.getInstance().putInWeighted("max_homology_degree", maxHomologyDegree);
 			}
 
 			int maxInteractionDegree = 0;
 			for (int v : graph.getVertices()) {
 				int x = graph.getInteraction().getIncidentEdges(v).size();
 				if (x > maxInteractionDegree) maxInteractionDegree = x;
 			}
 			if (ReportGenerator.getInstance() != null) {
 				ReportGenerator.getInstance().putInWeighted("max_interaction_degree", maxInteractionDegree);
 			}
 
 		} finally {
 			pool.shutdownNow();
 
 			int count = Thread.activeCount() - 1;
 			if (count > 0) {
 				logger.warn("There are " + count + " lingering threads");
 			}
 		}
 	}
 
 }
