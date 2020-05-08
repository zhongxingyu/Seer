 /***********************************************************************************************************************
  *
  * Copyright (C) 2013 by the Stratosphere project (http://stratosphere.eu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  *
  **********************************************************************************************************************/
 package de.tuberlin.dima.ml.pact.logreg.batchgd;
 
 import de.tuberlin.dima.ml.pact.JobRunner;
 import de.tuberlin.dima.ml.pact.io.LibsvmBinaryInputFormat;
 import de.tuberlin.dima.ml.pact.io.WeightVectorInputFormat;
 import de.tuberlin.dima.ml.pact.logreg.ensemble.EnsembleJob;
 import de.tuberlin.dima.ml.pact.types.PactVector;
 import de.tuberlin.dima.ml.util.IOUtils;
 import eu.stratosphere.pact.common.contract.CrossContract;
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.ReduceContract;
 import eu.stratosphere.pact.common.io.RecordOutputFormat;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.plan.PlanAssembler;
 import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
 import eu.stratosphere.pact.common.type.base.PactInteger;
 import eu.stratosphere.pact.generic.contract.BulkIteration;
 
 public class BatchGDJob implements PlanAssembler, PlanAssemblerDescription {
 
   static final int ID_SUM_IN_KEY = 0;
   static final int ID_SUM_IN_GRADIENT_PART = 1;
 
   private static final int CCAT = 33;
   private static final int ECAT = 59;
   private static final int GCAT = 70;
   private static final int MCAT = 102;
 
   private static final int NUM_FEATURES = 47237;
 
   @Override
   public Plan getPlan(String... args) {
     // parse job parameters
     final int numSubTasks = (args.length > 0) ? Integer.parseInt(args[0]) : 1;
     final String inputPathWeights = (args.length > 1) ? args[1] : "";
     final String inputPathTrain = (args.length > 2) ? args[2] : "";
     final String inputPathTest = (args.length > 3) ? args[3] : "";
     final String outputPath = (args.length > 4) ? args[4] : "";
     final int numIterations = (args.length > 5) ? Integer.parseInt(args[5]) : 1;
     final boolean runValidation = (args.length > 6) ? ((args[6].equals("1")) ? true
         : false)
         : false;
     final String learningRate = (args.length > 7) ? args[7] : "1";
 
     // input vectors (constant path)
     FileDataSource trainingVectors = new FileDataSource(
         LibsvmBinaryInputFormat.class, inputPathTrain, "Input Vectors");
     trainingVectors.setParameter(LibsvmBinaryInputFormat.TARGET, CCAT);
     trainingVectors.setParameter(LibsvmBinaryInputFormat.NUM_FEATURES,
         NUM_FEATURES);
 
     // initial weight
     FileDataSource initialWeights = new FileDataSource(
         WeightVectorInputFormat.class, inputPathWeights, "Weights");
     initialWeights.setParameter(LibsvmBinaryInputFormat.NUM_FEATURES,
         NUM_FEATURES);
 
     BulkIteration iteration = new BulkIteration("Batch GD");
     iteration.setInput(initialWeights);
     iteration.setMaximumNumberOfIterations(numIterations);
     System.out.println("NUM ITERATIONS: " + numIterations);
 
     CrossContract computeGradientParts = CrossContract.builder(ComputeGradientPart.class)
         .input1(trainingVectors)
         .input2(iteration.getPartialSolution())
         .name("Compute Gradient Parts (Cross)")
         .build();
 
     // TODO Stratosphere Bug: If we don't pass the key class and the key column,
     // we get the unspecific error
     // Exception in thread "main"
     // eu.stratosphere.pact.compiler.CompilerException: Unknown local strategy:
     // ALL_GROUP at
     // eu.stratosphere.pact.compiler.costs.CostEstimator.costOperator(CostEstimator.java:185)
     ReduceContract computeGradient = ReduceContract.builder(GradientSum.class, PactInteger.class, 0)
         .input(computeGradientParts)
         .name("Sum up Gradient (Reduce)")
         .build();
     
     CrossContract applyGradient = CrossContract.builder(ApplyGradient.class)
         .input1(iteration.getPartialSolution())
         .input2(computeGradient)
         .name("Apply Gradient (Cross)")
         .build();
     applyGradient.setParameter(ApplyGradient.CONF_KEY_LEARNING_RATE, learningRate);
 
     iteration.setNextPartialSolution(applyGradient);
 
     FileDataSink finalResult = new FileDataSink(RecordOutputFormat.class,
         outputPath, iteration, "Output");
 
     RecordOutputFormat.configureRecordFormat(finalResult).recordDelimiter('\n')
         .fieldDelimiter(' ')
         .field(PactVector.class, 0);
 
     Plan plan = new Plan(finalResult, "BatchGD Plan");
     plan.setDefaultParallelism(numSubTasks);
 
     return plan;
   }
 
   @Override
   public String getDescription() {
     return "[numSubTasks] [inputPathWeights] [inputPathTrain] [inputPathTest] [outputPath] [runValidation (0 or 1)] [learningRate]";
   }
 
   public static void main(String[] args) throws Exception {
     BatchGDJob bgd = new BatchGDJob();
 
     String numSubTasks = "1";
     String inputPathWeights = "file:///home/andre/dev/logreg-repo/logreg-pact/bgd-initial-weights";
     String inputFileTrain = "file:///home/andre/dev/datasets/libsvm-rcv1/rcv1_train.binary";
     String inputFileTest = "file:///home/andre/dev/datasets/libsvm-rcv1/rcv1_test_20000.binary";
     String outputFile = "file:///home/andre/output-bgd";
     String numIterations = "3";
     String runValidation = "0";
     String learningRate = "0.1";
     String[] jobArgs = { 
         numSubTasks, 
         inputPathWeights, 
         inputFileTrain, 
         inputFileTest,
         outputFile,
         numIterations,
         runValidation,
         learningRate};
     // String[] jobArgs = { "1", "file:///home/andre/dev/logreg-repo",
     // "file:///Users/uce/Desktop/rcv1libsvm/rcv1_topics_train.svm",
     // "file:///Users/uce/Desktop/rcv1libsvm/rcv1_topics_train.svm",
     // "file:///Users/uce/Desktop/rcv1libsvm/output/", "1", "0" };
     
    boolean runLocal = false;
     JobRunner runner = new JobRunner();
     if (runLocal) {
       
       runner.runLocal(bgd.getPlan(jobArgs));
       
     } else {
       
       String jarPath = IOUtils.getDirectoryOfJarOrClass(EnsembleJob.class)
           + "/logreg-pact-0.0.1-SNAPSHOT-job.jar";
       System.out.println("JAR PATH: " + jarPath);
       runner.run(jarPath, "de.tuberlin.dima.ml.pact.logreg.batchgd.BatchGDJob", jobArgs, "", true);
       
     }
     System.out.println("Job completed. Runtime=" + runner.getLastRuntime());
 
   }
 
 }
