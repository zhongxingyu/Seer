 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.larkc.iris;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.mapred.JobConf;
 
 import eu.larkc.iris.evaluation.IDistributedEvaluationStrategyFactory;
 import eu.larkc.iris.evaluation.bottomup.DistributedBottomUpEvaluationStrategyFactory;
 import eu.larkc.iris.evaluation.bottomup.IRuleEvaluationBlocker;
 import eu.larkc.iris.evaluation.bottomup.naive.DistributedNaiveEvaluatorFactory;
 import eu.larkc.iris.rules.IRecursiveRulePreProcessor;
 import eu.larkc.iris.rules.NonOptimizingRecursiveRulePreProcessor;
 import eu.larkc.iris.rules.optimisation.JoinOptimizer;
 import eu.larkc.iris.rules.stratification.DependencyMinimizingStratifier;
 import eu.larkc.iris.rules.stratification.IPostStratificationOptimization;
 import eu.larkc.iris.rules.stratification.IPreStratificationOptimization;
 import eu.larkc.iris.rules.stratification.RdfsOptimizer;
 
 /**
  * Configuration for a distributed evaluation
  * 
  * @author valer.roman@softgress.com
  */
 public class Configuration extends org.deri.iris.Configuration
 {
 	public String project;
 	public boolean keepResults = false;
 	public String resultsName;
 
	public boolean doPredicateIndexing = true;
 	
 	public org.apache.hadoop.conf.Configuration hadoopConfiguration = null;
 	public JobConf jobConf = null;
 	
 	public Map<Object, Object> flowProperties = new HashMap<Object, Object>();
 	
 	public final String DELTA_TAIL_NAME = "deltaTail";
 	
 	public final String DELTA_TAIL_HFS_PATH = "tmp/delta";
 	
 	public final String PREDICATE_COUNT_TAIL_HFS_ROOT_PATH = "tmp/predicate_count";
 	
 	/** The evaluation strategy to use. */
 	public IDistributedEvaluationStrategyFactory evaluationStrategyFactory = new DistributedBottomUpEvaluationStrategyFactory( new DistributedNaiveEvaluatorFactory() );
 
 	public final List<IRecursiveRulePreProcessor> recursiveRulePreProcessors = new ArrayList<IRecursiveRulePreProcessor>();
 	
 	public List<IPreStratificationOptimization> preStratificationOptimizer = new ArrayList<IPreStratificationOptimization>();
 	
 	public List<IPostStratificationOptimization> postStratificationOptimizations = new ArrayList<IPostStratificationOptimization>();
 	
 	public List<IRuleEvaluationBlocker> ruleEvaluationBlockers = new ArrayList<IRuleEvaluationBlocker>();
 	
 	public Configuration() {		
 		//include default optimizers
 		super();
 		
 		//RDFS specific optimizations
 		RdfsOptimizer optimizer = new RdfsOptimizer();
 		preStratificationOptimizer.add(optimizer);
 		postStratificationOptimizations.add(optimizer);
 		ruleEvaluationBlockers.add(optimizer);
 		//end RDFS specific
 		
 		ruleOptimisers.add(new JoinOptimizer());
 		recursiveRulePreProcessors.add(new NonOptimizingRecursiveRulePreProcessor());		
 	
 		stratifiers.add(new DependencyMinimizingStratifier(this));
 	}
 	
 }
