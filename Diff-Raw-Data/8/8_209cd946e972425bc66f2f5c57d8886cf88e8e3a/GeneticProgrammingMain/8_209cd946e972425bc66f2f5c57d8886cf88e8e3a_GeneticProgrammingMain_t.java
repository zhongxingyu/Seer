 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Properties;
 
 import utilities.GeneticOperators;
 import utilities.Settings;
 import data.GeneticProgrammingTree;
 import data.OutputData;
 import data.TrainingData;
 
 /**
  * Main GP 
  * @author bash1664
  *
  */
 
 public class GeneticProgrammingMain {
 
 	/**
 	 * Main Run() 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		GeneticProgrammingMain gpMain = new GeneticProgrammingMain();
 		
 		try {
             gpMain.process();
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Error occurred.");
         }
 	}
 	
 	public void process() throws Exception {
 	    // Initialize 
 	    ArrayList<TrainingData> trainingData = TrainingData.getTrainingData();
 	    OutputData output = new OutputData();
 	    
         ArrayList<GeneticProgrammingTree> initialPopulation = getInitialPopulation(trainingData);
         
         // Sort population according to fitness, in descending order
         Collections.sort(initialPopulation);
         
         // Get current time in milliseconds
         long startTime = getCurrentTime();
         output.setStartTime(startTime);
         
         GeneticProgrammingTree currentMaxFitnessTree = getCurrentMaxFitnessTree(initialPopulation);
         output.incrementGenerationCount();
         output.addFittestTreeInGeneration(currentMaxFitnessTree);
         output.addPopulationSizeInGeneration(initialPopulation.size());
         output.recordInitialPopulationFitness(initialPopulation);
        
         // Generate the best fit solution
         ArrayList<GeneticProgrammingTree> population = initialPopulation;
         while (!done(startTime, currentMaxFitnessTree) && output.getGenerationCount() < Settings.getMaxGeneration()) {  
             output.setCurrentTime(getCurrentTime());
             output.displayResults();
             
             if (Settings.trace()) {
                 output.displayPopulation(population);
             }
             
             ArrayList<GeneticProgrammingTree> nextGenPopulation = GeneticOperators.selection(population);
             
             if (Settings.trace()) {
                 System.out.println("[Trace] Display selected population:");
                 output.displayPopulation(nextGenPopulation);
             }
             
             ArrayList<GeneticProgrammingTree> children = GeneticOperators.crossoverTrees(population);
             
             nextGenPopulation.addAll(children);
             
             GeneticOperators.mutateTrees(nextGenPopulation);
             
             output.incrementGenerationCount();
             output.addPopulationSizeInGeneration(nextGenPopulation.size());
                 
             performFitnesEvaluation(nextGenPopulation);
                 
             Collections.sort(nextGenPopulation);
             
             currentMaxFitnessTree = getCurrentMaxFitnessTree(nextGenPopulation);
             output.addFittestTreeInGeneration(currentMaxFitnessTree);
                  
             population = nextGenPopulation;
         }
         
         output.setCurrentTime(getCurrentTime());
         output.displayPopulation(population);
         output.displayFinalResults();
         output.recordXYGraph();
         output.recordFinalPopulationFitness(population);
 	}
 
     private void performFitnesEvaluation(ArrayList<GeneticProgrammingTree> nextGenPopulation)throws Exception {
         for (GeneticProgrammingTree gpTree : nextGenPopulation) {
             double fitness = GeneticProgrammingTree.evaluateFitness(TrainingData.getTrainingData(), gpTree);
             gpTree.setFitness(fitness);
         }
     }
 
     private GeneticProgrammingTree getCurrentMaxFitnessTree(ArrayList<GeneticProgrammingTree> population) {
         GeneticProgrammingTree currentMaxFitnessTree = population.get(0);
         return currentMaxFitnessTree;
     }
 
     private boolean done(long startTime, GeneticProgrammingTree gpTree) throws Exception {
        if (bestSolutionFound(gpTree) || executionTimeExceeded(startTime)) {
             if (Settings.trace()) {
                 System.out.println("[Trace:done()] *** Done! ***");
             }
             return true;
         } else {
             if (Settings.trace()) {
                 System.out.println("[Trace:done()] Not done");
             }
             return false;
         }
     }
 
     private boolean bestSolutionFound(GeneticProgrammingTree currentMaxFitnessTree) throws Exception {
         boolean found = false;
         if (currentMaxFitnessTree.getFitness() <= Settings.getFitnessThreshold()) {
             found = true;
             
             if (Settings.trace()) {
                 System.out.println("[Trace:bestSolutionFound()] *** Best fit solution found! ***");
             }
         }
         
         if (Settings.trace()) {
             System.out.println("[Trace} currentMaxFitness: " + currentMaxFitnessTree.getFitness());
             System.out.println("[Trace} fitnessThreshold : " + Settings.getFitnessThreshold());
         }
         
         return found;
     }
 	
     private boolean executionTimeExceeded(long startTime) throws Exception {
         long currentTime = new Date().getTime();
         long runDuration = currentTime - startTime;
         
         if (Settings.trace()) {
             System.out.println("[Trace] StartTime   : " + startTime);
             System.out.println("[Trace] CurrentTime : " + currentTime);
             System.out.println("[Trace] Run Duration: " + runDuration);
             System.out.println("[Trace] Max Duration: " + Settings.maxExecutionTime());
         }
         
         if (runDuration > Settings.maxExecutionTime()) {
             if (Settings.trace()) {
                 System.out.println("[Trace] Run Duration: " + runDuration);
                 System.out.println("[Trace] Max Duration: " + Settings.maxExecutionTime());
                System.out.println("[Trace:bestSolutionFound()] *** Max exection time exceeded! ***");
             }
            
             return true;
         } else {
             if (Settings.trace()) {
                 System.out.println("[Trace] Run Duration: " + runDuration);
                 System.out.println("[Trace] Max Duration: " + Settings.maxExecutionTime());
             }

             return false;
         }  
     }
 
     private long getCurrentTime() {
         return new Date().getTime();
     }
 
     private ArrayList<GeneticProgrammingTree> getInitialPopulation(ArrayList<TrainingData> trainingData) {
         int size = 0;
         
         try {
             Properties settings = Settings.getSettings();
             
             String prop = settings.getProperty(Settings.PROP_POPULATION_SIZE);
             
             size = Integer.parseInt(prop);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         ArrayList<GeneticProgrammingTree> population = new ArrayList<GeneticProgrammingTree>(size);
         try {
             for (int i = 0; i < size; i++) {
                 GeneticProgrammingTree gpTree = GeneticProgrammingTree.createGeneticProgrammingTree(trainingData); 
                 
                 population.add(gpTree);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return population;
     }
 
 }
