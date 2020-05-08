 package org.kevoree.genetic.sample.fillAllNodes;
 
 import org.kevoree.genetic.framework.KevoreeFitnessFunction;
 import org.kevoree.genetic.framework.internal.KevoreeInitialization;
 import org.kevoree.genetic.framework.internal.KevoreeProblem;
 import org.kevoree.genetic.framework.internal.KevoreeVariationAdaptor;
 import org.moeaframework.algorithm.Checkpoints;
 import org.moeaframework.algorithm.NSGAII;
 import org.moeaframework.core.*;
 import org.moeaframework.core.operator.TournamentSelection;
 import org.moeaframework.core.spi.AlgorithmFactory;
 import org.moeaframework.util.distributed.DistributedProblem;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jbourcie
  * Date: 12/02/13
  * Time: 16:39
  * To change this template use File | Settings | File Templates.
  */
 public class ExecuterTester {
 
     public static void main(String[] args) {
         List<KevoreeFitnessFunction> fitnesses = new ArrayList<KevoreeFitnessFunction>();
         fitnesses.add(new FillAllFitness());
         Problem problem = new KevoreeProblem(fitnesses);
         Variation variation = new KevoreeVariationAdaptor(new AddComponentMutator("FakeConsole"));
         Algorithm algorithm = new NSGAII(problem, new NondominatedSortingPopulation(), new EpsilonBoxDominanceArchive(0.5), new TournamentSelection(),variation ,new KevoreeInitialization(new MiniCloudPopulationFactory(),problem) );
         int maxEvaluations = 10000;
         try {
 
 //               if (instrumenter != null) {
 //                    algorithm = instrumenter.instrument(algorithm);
 //                }
 
             while (!algorithm.isTerminated() &&
                     (algorithm.getNumberOfEvaluations() < maxEvaluations)) {
                 algorithm.step();
             }
 
             NondominatedPopulation result = new NondominatedPopulation();
             result.addAll(algorithm.getResult());
         } finally {
             if (algorithm != null) {
                 algorithm.terminate();
             }
             if (problem != null) {
                 problem.close();
             }
 
         }

        System.out.println("Hello Genetic :-)");

        Population pop = algorithm.getResult() ;
        for(Solution s : pop){

        }

     }
 }
