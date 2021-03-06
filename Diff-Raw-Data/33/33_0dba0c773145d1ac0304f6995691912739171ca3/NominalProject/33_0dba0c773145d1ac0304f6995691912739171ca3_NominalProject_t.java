 package com.datascience.core.nominal;
 
 import com.datascience.core.base.*;
 import com.datascience.core.results.Results;
 import com.datascience.core.results.ResultsFactory;
 import com.datascience.core.results.WorkerResult;
 import com.datascience.core.results.DatumResult;
 import com.datascience.core.nominal.decision.DecisionEngine;
 import com.datascience.core.nominal.decision.ILabelProbabilityDistributionCalculator;
 import com.datascience.core.nominal.decision.LabelProbabilityDistributionCalculators;
 import com.datascience.core.nominal.decision.ObjectLabelDecisionAlgorithms;
 
 import java.util.Collection;
 
 /**
  * User: artur
  */
 public class NominalProject extends Project<String, NominalData, DatumResult, WorkerResult> {
 
 	protected NominalAlgorithm nomAlgorithm;
 
 	public NominalProject(Algorithm algorithm1){
 		super(algorithm1);
 		nomAlgorithm = (NominalAlgorithm) algorithm1; // just to skip casting over and over
 		data = new NominalData();
 		algorithm.setData(data);
 	}
 
 	public void initializeCategories(Collection<Category> categories){
 		data.addCategories(categories);
 		nomAlgorithm.initializeOnCategories(categories);
 		results = createResultsInstance(categories);
 		algorithm.setResults(results);
 	}
 
 	public Results<String, DatumResult, WorkerResult> createResultsInstance(Collection<Category> categories){
 		return new Results<String, DatumResult, WorkerResult>(
 				new ResultsFactory.DatumResultFactory(),
 				new ResultsFactory.WorkerResultNominalFactory(categories));
 	}
 }
