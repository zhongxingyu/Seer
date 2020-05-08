 import com.cyberbotics.webots.controller.*;
 import games.Game;
 import util.FilesFunctions;
 import util.Util;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: annapawlicka
  * Date: 01/03/2013
  * Time: 19:50
  * E-puck controller that controls the evolution of games and runs trials. All sensorimotor inputs come from this
  * controller.
  * Fitness function of games is the variance of the actors (Fisher's Law).
  */
 
 public class EpuckController extends Robot {
 
     // Global variables
     private int GAME_POP_SIZE = 1;
     private int NN_POP_SIZE = 30;
     private final int LEFT = 0;
     private final int RIGHT = 1;
     private final int TIME_STEP = 256;              // [ms]
     private final int PS_RANGE = 3800;
     private final int SPEED_RANGE = 500;
     private final int NB_DIST_SENS = 4;             // Number of IR proximity sensors
     private final double OBSTACLE_THRESHOLD = 3000;
     private final int TRIAL_DURATION = 120000;       // Evaluation duration of one individual - 1 minute [ms]
     private final int NB_INPUTS = 4;
     private final int NB_HIDDEN_NEURONS = 12;
     private final int NB_OUTPUTS = 2;
     private int NB_WEIGHTS;
     private int NB_CONSTANTS = 4;
     private float weights[];
 
     // Evolution of games
     private Game[] populationOfGames;
     private double[] gameFitness;                           // Fitness of games (variance of actors)
     private double[] fitnessOfSolutions;                    // Fitness scores of each actor (calculation dependant on method chosen)
     private double[][] sortedfitnessGames;                  // Population of games sorted byte fitness
     private double[][] agentsFitness;                       // Fitness of agents for each game - used for Multi-objective Optimisation
     private double[][] actorFitPerGame;
 
     private double ELITISM_RATIO = 0.1;
     private double REPRODUCTION_RATIO = 0.4;                // If not using roulette wheel (truncation selection), we need reproduction ratio
     private double CROSSOVER_PROBABILITY = 0.5;             // Probability of having a crossover
     private double MUTATION_PROBABILITY = 0.1;              // Probability of mutating each weight-value in a genome    private int GENE_MIN = -1;                              // Range of genes: minimum value
     private int GENE_MIN = -1;
     private int GENE_MAX = 1;                               // Range of genes: maximum value
     private double MUTATION_SIGMA = 0.2;                    // Mutations follow a Box-Muller distribution from the gene with this sigma
     private int generation = 0;                             // Generation counter
     //If 1, evolution takes place. If 0, then the best individual obtained during the previous evolution is tested for an undetermined amount of time.
     private int EVOLVING = 1;
 
     //Log variables
     private double minFitGame = 0.0, avgFitGame = 0.0, bestFitGame = 0.0, absBestFitGame = 0.0;
     private int bestGame = -1, absBestGame = -1;
 
     // Mode of robot
     private static int mode;
     private final int SIMULATION = 0;               // for robot.get_mode() function
     private final int REALITY = 2;                  // for robot.get_mode() function
 
     // 8 IR proximity sensors
     private int NB_PROXIMITY_SENSORS = 4;
     private DistanceSensor[] ps;
     private float[] ps_offset;
     private int[] PS_OFFSET_SIMULATION = new int[]{300, 300, 300, 300, 300, 300, 300, 300};
     private int[] PS_OFFSET_REALITY = new int[]{480, 170, 320, 500, 600, 680, 210, 640};
 
     // 3 IR floor color sensors
     private int NB_FLOOR_SENSORS = 3;
     private DistanceSensor[] fs;
     private double[] fs_value = new double[]{0, 0, 0};
     private double maxIRActivation;
 
     // Differential Wheels
     private DifferentialWheels robot = new DifferentialWheels();
     private double[] speed;
     private double[] previousSpeed;
     private double[] states = new double[NB_INPUTS];               // The sensor values  8+1
 
     // Emitter and Receiver
     private Emitter emitter;
     private Receiver receiver;
     private Emitter gameEmitter;
     private Receiver gameReceiver;
 
     // Logging
     private BufferedWriter out1, out2, out3, out4, out5;
     private FileWriter file1, file2, file3, file4, file5;
 
     private int step;
     private Random random = new Random();
 
     private int indiv;
     private int TESTING = 0;
 
 
     public void run() throws Exception {
 
         while (step(TIME_STEP) != -1) {
 
             int i;
             if (mode != getMode()) {
                 mode = getMode();
                 if (mode == SIMULATION) {
                     for (i = 0; i < NB_DIST_SENS; i++) ps_offset[i] = PS_OFFSET_SIMULATION[i];
                     System.out.println("Switching to SIMULATION.\n\n");
                 } else if (mode == REALITY) {
                     for (i = 0; i < NB_DIST_SENS; i++) ps_offset[i] = PS_OFFSET_REALITY[i];
                     System.out.println("\nSwitching to REALITY.\n\n");
                 }
             }
 
             int m = gameReceiver.getQueueLength();
             if (m > 0) {
                 byte[] flag = gameReceiver.getData();
 
                 if (flag[0] == 1) { // is flag 1 is received, evolution can be started (the frequency is set by supervisor)
 
                     /* Start evolution of games */
                     setGameFitness();
                     // 1. Sort populationOfGames by fitness
                     sortPopulation(sortedfitnessGames, gameFitness);
                     // 2. Find best, average and worst game
                     bestFitGame = sortedfitnessGames[0][0]; // fitness score of best indiv
                     minFitGame = sortedfitnessGames[GAME_POP_SIZE - 1][0];  // fitness score of worst indiv
                     bestGame = (int) sortedfitnessGames[0][1]; // index of best individual
                     avgFitGame = util.Util.mean(gameFitness);
                     // 3. Log best, average and worst fitness score - writes to the file
                     if (bestFitGame > absBestFitGame) {
                         absBestFitGame = bestFitGame;
                         absBestGame = bestGame;
                         FilesFunctions.logBest(out3, generation, NB_CONSTANTS, absBestGame, populationOfGames);
                     }
                     System.out.println("Best game fitness score: \n" + bestFitGame);
                     System.out.println("Average game fitness score: \n" + avgFitGame);
                     System.out.println("Worst game fitness score: \n" + minFitGame);
                     System.out.println("Absolute best index: " + absBestGame);
 
                     // 4. Write data to files
                     FilesFunctions.logFitnessCases(out1, avgFitGame, generation, bestFitGame, minFitGame);
                     FilesFunctions.logCompFitnesses(out2, generation, gameFitness);
                     // Log the generation data  - stores constants
                     try {
                         FilesFunctions.logLastGeneration(populationOfGames);
                     } catch (IOException e) {
                         e.getMessage();
                     }
 
                     try {
                         FilesFunctions.logAllGameGenomes(out5, generation, populationOfGames);
                     } catch (IOException e) {
                         System.err.println(e.getMessage());
                     }
 
                     // 5. Rank populationOfGames, select best individuals and create new generation
                     //createNewPopulation();
 
                     // 6. Reset evolution variables
                     generation++;
                     //System.out.println("\nGAME GENERATION \n" + generation);
                     avgFitGame = 0.0;
                     bestFitGame = 0;
                     bestGame = 0;
                     minFitGame = 0;
                     // Reset all fitness arrays
                     resetAllFitnessArrays();
 
                 }
                 gameReceiver.nextPacket();
             }
             if (step == 0) {
                 int n = receiver.getQueueLength();
                 // Wait for new genome
                 if (n > 0) {
                     byte[] genes = receiver.getData();
                     if (genes.length == (NB_WEIGHTS) * 4) {  // 64
                         weights = Util.bytes2FloatArray(genes);
                     } else {
                         // Set neural network weights    TODO fix this
                         int p = 0;
                         int r = 0;
                         byte[] weight = new byte[4];
                         for (i = 0; i < genes.length - 4; i++) {
                             if (p < 4) {
                                 //weight = new byte[4];
                                 weight[p] = genes[i];
                                 p++;
                             } else {
                                 //weights[r] = Util.bytearray2float(weight);
                                 p = 0;
                                 r++;
                             }
                         }
                         TESTING = 2;
                         System.out.println("Received best genome for testing.");
 
                     }
                     receiver.nextPacket();
                 }
             }
 
             if (TESTING == 0) step++;
 
             if (step < TRIAL_DURATION / TIME_STEP && TESTING == 0) {
                 // Drive robot
                 runTrial(true);
             } else if (TESTING == 0) {
                 // Send message to indicate end of trial and send fitness values for each game - next actor will be called
                 float msg[] = new float[GAME_POP_SIZE + 1]; // sending flag too
                 for (i = 0; i < agentsFitness[indiv].length; i++) {
                     msg[i] = (float) agentsFitness[indiv][i];
                 }
                 msg[GAME_POP_SIZE] = 0.0f;
                 byte[] msgInBytes = Util.float2ByteArray(msg);
                 emitter.send(msgInBytes);
                 // Reinitialize counter
                 step = 0;
 
                 // If all individuals finished their trials
                 if ((indiv + 1) < NN_POP_SIZE) {
                     indiv++;
                 } else {
                     indiv = 0;
                     byte[] endMsg = {1};
                     emitter.send(endMsg);
                     resetActorsFitArrays();
                 }
             } else if (TESTING == 2) {
                 runTrial(false);
             }
 
         }
     }
 
     /**
      * A single trial during which one action is performed.
      *
      * @return Returns current fitness score
      */
     private void runTrial(boolean ifEvolved) throws Exception {
 
         double[] outputs = new double[NB_OUTPUTS];
 
         updateSenorReadings();
         run_neural_network(states, outputs);
         speed[LEFT] = SPEED_RANGE * outputs[0];
         speed[RIGHT] = SPEED_RANGE * outputs[1];
 
         // Set wheel speeds to output values
         robot.setSpeed(speed[LEFT], speed[RIGHT]);
 
         // Stop the robot if it is against an obstacle
         for (int i = 0; i < NB_DIST_SENS; i++) {
             double temp_ps = (((ps[i].getValue()) - ps_offset[i]) < 0) ? 0 : ((ps[i].getValue()) - ps_offset[i]);
 
             if (OBSTACLE_THRESHOLD < temp_ps) {// proximity sensors
                 speed[LEFT] = 0;
                 speed[RIGHT] = 0;
                 break;
             }
         }
 
         if (ifEvolved) computeFitness(speed, maxIRActivation);
     }
 
 
     /**
      * Method to calculate fitness score - fitness function that have evolvable constants
      *
      * @param speed
      * @param maxIRActivation
      */
     public void computeFitness(double[] speed, double maxIRActivation) throws Exception {
 
         int currentFitness = 0;
         // punish oscillatory movement
         //if((Math.abs(speed[LEFT]) - speed[RIGHT]) >= 200) currentFitness -=1;
         // punish slow speed
         //if(speed[LEFT] < 300 && speed[RIGHT]< 300) currentFitness-=1;
         // punish hitting obstacles
         //if(maxIRActivation > 3000) currentFitness-=10;
 
         // Avoid obstacles:
         //agentsFitness[indiv][0] += currentFitness;
 
         //agentsFitness[indiv][0] += (float)(Util.mean(speed) * (1 - Math.sqrt(Math.abs(speed[LEFT] - speed[RIGHT])) * (1 - Util.normalize(0, 4000, maxIRActivation))));
 
 
         // Follow wall
        if(speed[LEFT] < 300 && speed[RIGHT] < 300) currentFitness-=1;  // Punish slow speed
        if(states[1] > 3000 || states[2] > 300) currentFitness+=1;      // Reward max IR activation of side sensors
        if(speed[LEFT] == 0 && speed[RIGHT] == 0) currentFitness-=1;
         agentsFitness[indiv][0] += currentFitness;
 
         // Follow black line
         /*if(fs_value[0] < 400 || fs_value[1] < 400 || fs_value[2] < 400) currentFitness+=1; // Reward detection of black line
         if(speed[LEFT] < 200 && speed[RIGHT] < 200) currentFitness-=1;  // Punish slow speed
         if(fs_value[1] > 500) currentFitness-=1;   // Punish detection of white line
         agentsFitness[indiv][0] += currentFitness;*/
 
         /*for (int i = 0; i < GAME_POP_SIZE; i++) {
             try {
                 agentsFitness[indiv][i] +=
                         (populationOfGames[i].getConstants()[0] * Util.mean(speed)) *
                                 (1 - (populationOfGames[i].getConstants()[1] * Math.sqrt(Math.abs((speed[LEFT] - speed[RIGHT]))))) *
                                 (1 - (populationOfGames[i].getConstants()[2] * Util.normalize(0, 4000, maxIRActivation))) *
                                 (1 - (populationOfGames[i].getConstants()[3] * Util.normalize(0, 900, floorColour))) *
                                 (populationOfGames[i].getConstants()[4] * light);
             } catch (Exception e) {
                 System.err.println("Error: " + e.getMessage());
             }
         }*/
     }
 
     /**
      * Reset all fitness arrays
      */
     private void resetAllFitnessArrays() {
         int i, j;
         for (i = 0; i < fitnessOfSolutions.length; i++) fitnessOfSolutions[i] = 0;
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) agentsFitness[i][j] = 0;
         }
         for (i = 0; i < gameFitness.length; i++) gameFitness[i] = 0;
         for (i = 0; i < sortedfitnessGames.length; i++) {
             for (j = 0; j < sortedfitnessGames[i].length; j++) sortedfitnessGames[i][j] = 0;
         }
         for (i = 0; i < actorFitPerGame.length; i++) {
             for (j = 0; j < actorFitPerGame[i].length; j++) actorFitPerGame[i][j] = 0;
         }
     }
 
     private void resetActorsFitArrays() {
         int i, j;
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) agentsFitness[i][j] = 0;
         }
 
         for (i = 0; i < fitnessOfSolutions.length; i++) fitnessOfSolutions[i] = 0;
 
     }
 
     /**
      * Assigns fitness scores by adding component fitness values on each game
      */
     private void setActorsFitness() {
         int i, j;
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) actorFitPerGame[j][i] = agentsFitness[i][j];
         }
 
         // Normalise
         //normaliseFitnessScore(actorFitPerGame);
 
         // Update (sum up) array of actors fitness scores so that it can be sent to supervisor
         for (i = 0; i < actorFitPerGame.length; i++) {
             for (j = 0; j < actorFitPerGame[i].length; j++) fitnessOfSolutions[j] += actorFitPerGame[i][j];
         }
 
         FilesFunctions.logAllCompFit(out4, actorFitPerGame, generation);
     }
 
     /**
      * Fill in gameFitness array with each individual's score
      */
     private void setGameFitness() {
         int i, j;
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) {
                 actorFitPerGame[j][i] = agentsFitness[i][j];
             }
         }
 
         // Normalise
         //for(i=0; i< actorFitPerGame.length; i++) normaliseFitnessScore(actorFitPerGame[i], i);
 
         //Calculate fitness of each game by computing variance of actor fitnesses on that game
         // Fitness of games doesn't need to be normalised as it's a variance over already normalised actors fitness
         for (i = 0; i < gameFitness.length; i++) gameFitness[i] = Util.variance(actorFitPerGame[i]);
 
     }
 
     /**
      * Normalise fitness scores to a value between 0 and 1
      */
     private void normaliseFitnessScore(double[] fitnessScores, int gameNo) {
 
         double min = 0, max = 0;
 
         if (gameNo == 0) {
             min = -2500000;
             max = 2500000;
         }
         if (gameNo == 1) {
             min = -200000;
             max = 600;
         }
         if (gameNo == 2) {
             min = -600000;
             max = 600000;
         }
 
         for (int i = 0; i < fitnessScores.length; i++) {
             double temp = 0;
             try {
                 temp = Util.normalize(min, max, fitnessScores[i]);  // add buffer of 0.5
             } catch (Exception e) {
                 System.err.println("Error while normalizing: " + e.getMessage());
             }
             fitnessScores[i] = temp;
         }
     }
 
 
     /**
      * Sort whole population according to fitness score of each individual. Uses quickSort.
      */
     private void sortPopulation(double[][] sortedfitness, double[] fitness) {
         int i;
         //sort populationOfNN by fitness
         for (i = 0; i < sortedfitness.length; i++) {
             sortedfitness[i][0] = fitness[i];   //fitness score
             sortedfitness[i][1] = i;            //keep index
         }
         quickSort(sortedfitness, 0, sortedfitness.length - 1);
     }
 
     /**
      * Standard fast algorithm to sort populationOfNN by fitness
      *
      * @param fitness Array that stores fitness and index of each individual.
      * @param left    Min index of the array
      * @param right   Max index of the array
      */
     private void quickSort(double fitness[][], int left, int right) {
         double[] pivot = new double[2];
         int l_hold, r_hold;
 
         l_hold = left;
         r_hold = right;
         pivot[0] = fitness[left][0];
         pivot[1] = fitness[left][1];
         while (left < right) {
             while ((fitness[right][0] <= pivot[0]) && (left < right))
                 right--;
             if (left != right) {
                 fitness[left][0] = fitness[right][0];
                 fitness[left][1] = fitness[right][1];
                 left++;
             }
             while ((fitness[left][0] >= pivot[0]) && (left < right))
                 left++;
             if (left != right) {
                 fitness[right][0] = fitness[left][0];
                 fitness[right][1] = fitness[left][1];
                 right--;
             }
         }
         fitness[left][0] = pivot[0];
         fitness[left][1] = pivot[1];
         pivot[0] = left;
         left = l_hold;
         right = r_hold;
         if (left < (int) pivot[0]) quickSort(fitness, left, (int) pivot[0] - 1);
         if (right > (int) pivot[0]) quickSort(fitness, (int) pivot[0] + 1, right);
     }
 
     /**
      * Based on the fitness of the last generation, generate a new games for the next generation.
      */
     private void createNewPopulation() {
 
         Game[] newpop = new Game[GAME_POP_SIZE];
         for (int i = 0; i < newpop.length; i++) {
             newpop[i] = new Game(true, NB_CONSTANTS);
         }
         double elitism_counter = GAME_POP_SIZE * ELITISM_RATIO;
         int i, j;
 
         // Create new populationOfNN
         for (i = 0; i < GAME_POP_SIZE; i++) {
 
             // The elitism_counter best individuals are simply copied to the new populationOfNN
             if (i < elitism_counter) {
                 for (j = 0; j < NB_CONSTANTS; j++)
                     newpop[i].setConstants(j, populationOfGames[(int) sortedfitnessGames[i][1]].getConstants()[j]);
             }
             // The other individuals are generated through the crossover of two parents
             else {
 
                 // Select non-elitist individual
                 int ind1;
                 ind1 = (int) (elitism_counter + random.nextFloat() * (GAME_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
 
                 // If we will do crossover, select a second individual
                 if (random.nextFloat() < CROSSOVER_PROBABILITY) {
                     int ind2;
                     do {
                         ind2 = (int) (elitism_counter + random.nextFloat() * (GAME_POP_SIZE * REPRODUCTION_RATIO - elitism_counter));
                     } while (ind1 == ind2);
                     ind1 = (int) sortedfitnessGames[ind1][1];
                     ind2 = (int) sortedfitnessGames[ind2][1];
                     newpop[i].crossover(ind1, ind2, newpop[i], NB_CONSTANTS, populationOfGames);
                 } else { //if no crossover was done, just copy selected individual directly
                     for (j = 0; j < NB_CONSTANTS; j++)
                         newpop[i].setConstants(j, populationOfGames[(int) sortedfitnessGames[ind1][1]].getConstants()[j]);
                 }
             }
         }
 
         // Mutate new populationOfGames and copy back to pop
         for (i = 0; i < GAME_POP_SIZE; i++) {
             if (i < elitism_counter) { //no mutation for elitists
                 for (j = 0; j < NB_CONSTANTS; j++) {
                     populationOfGames[i].copy(newpop[i]);
                 }
             } else { // Mutate others with probability per gene
                 for (j = 0; j < NB_CONSTANTS; j++)
                     if (random.nextFloat() < MUTATION_PROBABILITY)
                         populationOfGames[i].setConstants(j, populationOfGames[i].mutate(GENE_MIN, GENE_MAX, newpop[i].getConstants()[j], MUTATION_SIGMA));
                     else
                         populationOfGames[i].copy(newpop[i]);
             }
             // Reset fitness
             resetAllFitnessArrays();
         }
     }
 
 
     /**
      * Read values from sensors into arrays.
      */
     private void updateSenorReadings() {
 
         previousSpeed[LEFT] = speed[LEFT];
         previousSpeed[RIGHT] = speed[RIGHT];
         maxIRActivation = 0;
         for (int j = 0; j < NB_PROXIMITY_SENSORS; j++) {
             states[j] = ps[j].getValue() - ps_offset[j] < 0 ? 0 : (ps[j].getValue() - (ps_offset[j]) / PS_RANGE);
             //get max IR activation
             if (states[j] > maxIRActivation) maxIRActivation = states[j];
         }
 
         //states[6] = previousSpeed[LEFT];
         //states[7] = previousSpeed[RIGHT];
          /*for (int i = ps.length; i < NB_FLOOR_SENSORS; i++) {
              fs_value[i] = fs[i].getValue();
              states[i] = fs_value[i];
          }*/
 
         //states[8] = fs_value[0];
         //states[8] = fs_value[1];
         //states[10] = fs_value[2];
     }
 
     private void run_neural_network(double[] inputs, double[] outputs) {
         int i, j;
         int weight_counter = 0;
 
         if (NB_HIDDEN_NEURONS > 0) {
             double[] hidden_neuron_out = new double[NB_HIDDEN_NEURONS];
 
             for (i = 0; i < NB_HIDDEN_NEURONS; i++) {
                 float sum = 0;
                 for (j = 0; j < NB_INPUTS; j++) {
                     sum += inputs[j] * weights[weight_counter];
                     weight_counter++;
                 }
                 hidden_neuron_out[i] = Math.tanh(sum + weights[weight_counter]);
                 weight_counter++;
             }
 
             for (i = 0; i < NB_OUTPUTS; i++) {
                 float sum = 0;
                 for (j = 0; j < NB_HIDDEN_NEURONS; j++) {
                     sum += hidden_neuron_out[j] * weights[weight_counter];
                     weight_counter++;
                 }
                 outputs[i] = Math.tanh(sum + weights[weight_counter]);
                 weight_counter++;
             }
         } else {
 
             for (i = 0; i < NB_OUTPUTS; i++) {
                 double sum = 0.0;
                 for (j = 0; j < NB_INPUTS; j++) {
                     sum += inputs[j] * weights[weight_counter];
                     weight_counter++;
                 }
                 if (Double.isNaN(sum)) sum = bound(sum);
                 outputs[i] = Math.tanh(sum + weights[weight_counter]);
                 weight_counter++;
             }
         }
     }
 
     private double bound(double d) {
         double TOO_SMALL = -1.0E19;
         double TOO_BIG = 1.0E19;
         if (d < TOO_SMALL) {
             return TOO_SMALL;
         } else if (d > TOO_BIG) {
             return TOO_BIG;
         } else {
             return d;
         }
     }
 
     private void initialiseGames(Game[] games) {
 
         /*Game 1: Avoid obstacles */
         games[0].setConstants(0, 1);    // Drive fast
         games[0].setConstants(1, 1);    // Try to steer straight
         games[0].setConstants(2, 1);    // Minimise IR proximity sensors activation
         games[0].setConstants(3, 0);    // Ignore floor colour/light
 
 
        /*Game 2: Follow black line *//*
         games[1].setConstants(0, 1);    // Drive fast
         games[1].setConstants(1, 1);    // Drive straight
         games[1].setConstants(2, 0);    // Avoid obstacles/walls
         games[1].setConstants(3, 1);    // Max black line /light
 
         *//* Game 3: Follow the wall *//*
         games[2].setConstants(0, 1);    // Drive fast
         games[2].setConstants(1, 1);    // Drive straight
         games[2].setConstants(2, -0.5f);   // Maximise prox sensors activation
         games[2].setConstants(3, 0);    // Max black line/light*/
 
     }
 
     /**
      * Method to initialise e-puck's sensors and data structures/variables.
      */
     public void reset() {
 
         int i, j;
         mode = 1;
         step = 0;
         indiv = 0;
 
         if (NB_HIDDEN_NEURONS == 0) NB_WEIGHTS = NB_INPUTS * NB_OUTPUTS + NB_OUTPUTS;
         else NB_WEIGHTS = NB_INPUTS * NB_HIDDEN_NEURONS + NB_HIDDEN_NEURONS + NB_HIDDEN_NEURONS * NB_OUTPUTS + NB_OUTPUTS;
 
         // Games
         populationOfGames = new Game[GAME_POP_SIZE];
         for (i = 0; i < GAME_POP_SIZE; i++) populationOfGames[i] = new Game(false, NB_CONSTANTS);
         initialiseGames(populationOfGames);
 
         fitnessOfSolutions = new double[NN_POP_SIZE];
         for (i = 0; i < GAME_POP_SIZE; i++) fitnessOfSolutions[i] = 0.0;
 
         sortedfitnessGames = new double[GAME_POP_SIZE][2];
         for (i = 0; i < GAME_POP_SIZE; i++) {
             for (j = 0; j < 2; j++) {
                 sortedfitnessGames[i][j] = 0.0;
             }
         }
         gameFitness = new double[GAME_POP_SIZE];
         for (i = 0; i < GAME_POP_SIZE; i++) gameFitness[i] = 0.0f;
 
         // Agents
         agentsFitness = new double[NN_POP_SIZE][GAME_POP_SIZE];
         for (i = 0; i < agentsFitness.length; i++) {
             for (j = 0; j < agentsFitness[i].length; j++) agentsFitness[i][j] = 0;
         }
 
         actorFitPerGame = new double[GAME_POP_SIZE][NN_POP_SIZE];
         for (i = 0; i < actorFitPerGame.length; i++) {
             for (j = 0; j < actorFitPerGame[i].length; j++) actorFitPerGame[i][j] = 0;
         }
 
         /* Initialise IR proximity sensors */
         ps = new DistanceSensor[NB_PROXIMITY_SENSORS];
         ps[0] = getDistanceSensor("ps0");
         ps[0].enable(TIME_STEP);
         //ps[0] = getDistanceSensor("ps1");
         //ps[0].enable(TIME_STEP);
         ps[1] = getDistanceSensor("ps2");
         ps[1].enable(TIME_STEP);
         //ps[1] = getDistanceSensor("ps3");
         //ps[1].enable(TIME_STEP);
         //ps[2] = getDistanceSensor("ps4");
         //ps[2].enable(TIME_STEP);
         ps[2] = getDistanceSensor("ps5");
         ps[2].enable(TIME_STEP);
         //ps[6] = getDistanceSensor("ps6");
         //ps[6].enable(TIME_STEP);
         ps[3] = getDistanceSensor("ps7");
         ps[3].enable(TIME_STEP);
 
         ps_offset = new float[NB_PROXIMITY_SENSORS];
         for (i = 0; i < ps_offset.length; i++) {
             ps_offset[i] = PS_OFFSET_SIMULATION[i];
         }
 
         maxIRActivation = 0;
         /* Initialise IR floor sensors */
         fs = new DistanceSensor[NB_FLOOR_SENSORS];
         for (i = 0; i < fs.length; i++) {
             fs[i] = getDistanceSensor("fs" + i);
             fs[i].enable(TIME_STEP);
         }
 
         /* Initialise states array */
         for (i = 0; i < states.length; i++) {
             states[i] = 0.0;
         }
         speed = new double[2];
         // Speed initialization
         speed[LEFT] = 0;
         speed[RIGHT] = 0;
         previousSpeed = new double[2];
         previousSpeed[LEFT]= 0;
         previousSpeed[RIGHT] = 0;
 
         emitter = getEmitter("emitterepuck");
         receiver = getReceiver("receiver");
         receiver.enable(TIME_STEP);
 
         gameEmitter = getEmitter("gamesemitterepuck");
         gameEmitter.setChannel(1);
         gameReceiver = getReceiver("gamesreceiverepuck");
         gameReceiver.setChannel(1);
         gameReceiver.enable(TIME_STEP);
 
         weights = new float[NB_WEIGHTS];
 
         // Logging
         try {
             file1 = new FileWriter("out/results.txt");
         } catch (IOException e) {
             System.out.println("Cannot open results.txt file.");
         }
 
         out1 = new BufferedWriter(file1);
         try {
             out1.write("generation , Average fitness, Worst fitness, Best fitness");
             out1.write("\n");
 
         } catch (IOException e) {
             System.err.println("" + e.getMessage());
         }
 
         try {
             file2 = new FileWriter("out/results:fitness_games.txt");
         } catch (IOException e) {
             System.err.println("Cannot write to file: fitness_games.txt");
         }
         out2 = new BufferedWriter(file2);
         try {
             out2.write("Generation, ");
             for (i = 0; i < GAME_POP_SIZE; i++) out2.write("Game " + i + ", ");
             out2.write("\n");
         } catch (IOException e) {
             System.out.println("Error writing to genome.txt: " + e.getMessage());
         }
 
         try {
             file3 = new FileWriter("out/results:bestgenome_games.txt");
         } catch (IOException e) {
             System.err.println("Cannot open bestgenome_games.txt file.");
         }
 
         out3 = new BufferedWriter(file3);
 
         try {
             file4 = new FileWriter("out/results:comp_fitness.txt");
         } catch (IOException e) {
             System.err.println("Cannot open comp_fitness.txt file.");
         }
 
         out4 = new BufferedWriter(file4);
 
         try {
             file5 = new FileWriter("out/all_games_genomes.txt");
         } catch (IOException e) {
             System.err.println("Cannot open all_games_genomes.txt file.");
         }
 
         out5 = new BufferedWriter(file5);
         try {
             FilesFunctions.logAllGameGenomes(out5, generation, populationOfGames);
         } catch (IOException e) {
             System.err.println(e.getMessage());
         }
         System.out.println("e-puck has been initialised.");
     }
 
 
     public static void main(String[] args) throws Exception {
         EpuckController controller = new EpuckController();
         controller.reset();
         controller.run();
     }
 }
 
