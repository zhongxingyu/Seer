 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jevomara;
 
 import java.io.*;
 import java.util.List;
 import java.util.Properties;
 import org.apslab.cyclops.BitArrayRandomPopulationInitializer;
 import org.apslab.cyclops.IFitnessFunction;
 import org.apslab.cyclops.IGeneticOperator;
 import org.apslab.cyclops.IPopulationInitializer;
 import org.apslab.cyclops.ISelector;
 import org.apslab.cyclops.ITerminationCriterion;
 import org.apslab.cyclops.Individual;
 import org.apslab.cyclops.Mutation;
 import org.apslab.cyclops.Random;
 import org.apslab.cyclops.MaxIterationsTerminationCriterion;
 import org.apslab.cyclops.Optimizer;
 import org.apslab.cyclops.TournamentSelection;
 import org.apslab.cyclops.TwoPointCrossOver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author andreas
  */
 public class Main {
 
     /**
      * Version string of the entire project
      */
     static String version = "0.2.1";
 
     /** slf4j logging */
     private static final Logger log = LoggerFactory.getLogger(Main.class);
     
     private static Properties readConfigFromInputStream(Properties defaultProp, InputStream in) throws IOException {
         // Create properties with default values
         Properties prop = new Properties(defaultProp);
         // Load a properties file
         prop.load(in);
         return prop;
     }
     
     private static Properties readConfig(String configFile) throws IOException {
         Properties prop = new Properties();
         // Load default properties
        prop = readConfigFromInputStream(prop, Main.class.getResourceAsStream("/resources/default.properties"));
         if (configFile != null) {
             prop = readConfigFromInputStream(prop, new FileInputStream(configFile));
         }
         return prop;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         // Parse command line arguments
         CommandLineParameters clParams = new CommandLineParameters();
         clParams.parse(args);
         if (clParams.getHelpOption()) {
             clParams.printHelp();
         } else if (clParams.getVersionOption()) {
             clParams.printVersion();
         }
 
         // Read configuration
         Properties prop = null;
         try {
             prop = readConfig(clParams.getConfigOption());
         } catch (IOException ex) {
             log.error(ex.getMessage(), ex);
             System.exit(1);
         }
         
         // Configure the optimizer
         Long seed = Long.valueOf(prop.getProperty("seed"));
         int iterations = Integer.valueOf(prop.getProperty("iterations"));
        int populationSize = Integer.valueOf(prop.getProperty("populationSize"));
         int elitism = Integer.valueOf(prop.getProperty("elitism"));
         int tournamentSize = Integer.valueOf(prop.getProperty("tournamentSize"));
         double mutationProbability = Double.valueOf(prop.getProperty("mutationProbability"));
         int mutationNumGenes = Integer.valueOf(prop.getProperty("mutationNumGenes"));
         int peptideLength = Integer.valueOf(prop.getProperty("peptideLength"));
         int geneLength = Integer.valueOf(prop.getProperty("geneLength")); // bits
         //String baseDir = "/home/andreas/Documents/evoVina/1BL0_docking_wDNA";
         //String baseDir = "/home/andreas/Documents/evoVina/1BL0_docking_BoxA";
         String baseDir = prop.getProperty("baseDir");
         boolean capping = Boolean.valueOf(prop.getProperty("capping"));
 
         // Initilize random number generator
         Random.setSeed(seed);
 
         IPopulationInitializer populationInitializer = new BitArrayRandomPopulationInitializer().setPopulationSize(populationSize).setPrototypeIndividual(new PeptideIndividual()).setArrayLength(peptideLength * geneLength);
         List<Individual> population = populationInitializer.getPopulation();
         IGeneticOperator crossOver = new TwoPointCrossOver();
         //IGeneticOperator crossOver = new NullCrossOver();
         Mutation mutation = new Mutation().setProbability(mutationProbability).setN(mutationNumGenes);
         ISelector selection = new TournamentSelection().setTournamentSize(tournamentSize);
         //IFitnessFunction fitnessFunction = new SequenceIdentityFitnessFunction().setTargetSequence("RRRR");
         //IFitnessFunction fitnessFunction = new MOEPharmacophoreFitnessFunction();
         IFitnessFunction fitnessFunction = new VinaDockingFitnessFunction().setBaseDir(baseDir).setCapping(capping);
         //ITerminationCriterion terminationCriterion = new MaxIterationsMinFitnessTerminationCriterion().setMaxIterations(iterations).setMinFitness(new Fitness(minFitness));
         ITerminationCriterion terminationCriterion = new MaxIterationsTerminationCriterion().setMaxIterations(iterations);
         //ITerminationCriterion terminationCriterion = new MinFitnessTerminationCriterion().setMinFitness(new Fitness(minFitness));
 
         Optimizer optimizer = new Optimizer().setElitism(elitism);
         //ThreadedOptimizer optimizer = new ThreadedOptimizer().setElitism(elitism).setNumberOfThreads(numberOfThreads);
         optimizer.setPopulationInitializer(populationInitializer);
         optimizer.setCrossOverOperator(crossOver);
         optimizer.setMutationOperator(mutation);
         optimizer.setSelector(selection);
         optimizer.setFitnessFunction(fitnessFunction);
         optimizer.setTerminationCriterion(terminationCriterion);
 
         optimizer.optimize();
     }
 }
