 package evolution.prisoner.evolution;
 
 import evolution.DetailsLogger;
 import evolution.EvolutionaryAlgorithm;
 import evolution.Population;
 import evolution.operators.IntegerMutation;
 import evolution.prisoner.Result;
 import evolution.prisoner.Strategy;
 import evolution.prisoner.Strategy.Move;
 import evolution.individuals.Individual;
 import evolution.individuals.IntegerIndividual;
 import evolution.operators.OnePtXOver;
 import evolution.selectors.RouletteWheelSelector;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.Random;
 
 public class PrisonerEvolution {
 
     static int maxGen;
     static int popSize;
     static double xoverProb;
     static double mutProb;
     static double mutProbPerBit;
     static ArrayList<Strategy> strategies;
 
     public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
 
         Properties prop = new Properties();
         try {
             InputStream propIn = new FileInputStream("properties/ga-prisoner-evolution.properties");
             prop.load(propIn);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
        prop = new Properties();
        try {
            InputStream propIn = new FileInputStream("properties/ga-binPacking.properties");
            prop.load(propIn);
        } catch (IOException e) {
            e.printStackTrace();
        }

         DetailsLogger.disableLog();
 
         maxGen = Integer.parseInt(prop.getProperty("ea.maxGenerations", "20"));
         popSize = Integer.parseInt(prop.getProperty("ea.popSize", "30"));
         xoverProb = Double.parseDouble(prop.getProperty("ea.xoverProb", "0.8"));
         mutProb = Double.parseDouble(prop.getProperty("ea.mutProb", "0.05"));
         mutProbPerBit = Double.parseDouble(prop.getProperty("ea.mutProbPerBit", "0.04"));
 
         String inputFile = prop.getProperty("prob.inputFile", "resources/input-evolution.txt");
 
         strategies = new ArrayList<Strategy>();
 
         try {
             BufferedReader in = new BufferedReader(new FileReader(inputFile));
             String line;
             while ((line = in.readLine()) != null) {
                 Strategy s = (Strategy) Class.forName("evolution.prisoner.strategies." + line).newInstance();
                 strategies.add(s);
             }
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         Move[] moves = run(0);
 
         strategies.add(new EvolvedStrategy(moves));
 
         int[] scores = new int[strategies.size()];
 
         Random rnd = new Random();
 
         for (int it = 0; it < 10; it++) {
 
             for (int i = 0; i < strategies.size(); i++) {
                 for (int j = 0; j < strategies.size(); j++) {
 
                     if (i == j) {
                         continue;
                     }
 
                     Strategy s1 = strategies.get(i);
                     Strategy s2 = strategies.get(j);
 
                     System.err.print(s1.getName() + " vs. " + s2.getName()
                             + ": ");
 
                     int sc1 = 0;
                     int sc2 = 0;
 
                     String str1 = "";
                     String str2 = "";
 
                     int iters = 150 + rnd.nextInt(100);
 
                     for (int m = 0; m < iters; m++) {
 
                         Move s1Move = null;
                         Move s2Move = null;
                         try {
                             s1Move = s1.nextMove();
                         } catch (Exception e) {
                             sc1 = 0;
                         }
                         try {
                             s2Move = s2.nextMove();
                         } catch (Exception e) {
                             sc2 = 0;
                         }
 
                         str1 += (s1Move == null) ? "E" : s1Move.getLabel();
                         str2 += (s2Move == null) ? "E" : s2Move.getLabel();
 
                         Result r1 = new Result(s1Move, s2Move);
                         Result r2 = new Result(s2Move, s1Move);
 
                         sc1 += r1.getMyScore();
                         sc2 += r2.getMyScore();
 
                         try {
                             s1.reward(r1);
                         } catch (Exception e) {
                             sc1 = 0;
                         }
                         try {
                             s2.reward(r2);
                         } catch (Exception e) {
                             sc2 = 0;
                         }
 
                     }
 
                     if (s1 instanceof EvolvedStrategy) {
                         System.err.println(sc1 + ":" + sc2);
                         System.err.println("\t" + str1);
                         System.err.println("\t" + str2);
                     }
 
                     scores[i] += sc1;
                     scores[j] += sc2;
 
                     s1.reset();
                     s2.reset();
 
                 }
             }
         }
         for (int j = 0; j < scores.length; j++) {
             int max = Integer.MIN_VALUE;
             int maxIdx = 0;
 
             for (int i = 0; i < scores.length; i++) {
                 if (scores[i] > max) {
                     max = scores[i];
                     maxIdx = i;
                 }
             }
 
             System.out.printf("%50s  %6d %s", strategies.get(maxIdx).getName(), scores[maxIdx], strategies.get(maxIdx).authorName());
             System.out.println();
             scores[maxIdx] = Integer.MIN_VALUE;
         }
 
     }
 
     static Move[] run(int number) {
 
         Individual sampleChromosome = new IntegerIndividual(64, 0, 2);
 
         Population pop = new Population();
         pop.setSampleIndividual(sampleChromosome);
         pop.setPopulationSize(popSize);
         pop.createRandomInitialPopulation();
 
         EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm();
         ea.setFitnessFunction(new PrisonerFitness(strategies));
         ea.addOperator(new OnePtXOver(xoverProb));
         ea.addOperator(new IntegerMutation(mutProb, mutProbPerBit));
         ea.addEnvironmentalSelector(new RouletteWheelSelector());
 
         for (int i = 0; i < maxGen; i++) {
             ea.evolve(pop);
             System.out.println("Generation " + i + ": " + pop.getSortedIndividuals().get(0).getObjectiveValue());
 
         }
 
         return PrisonerFitness.toMovesArray(pop.getSortedIndividuals().get(0));
     }
 }
