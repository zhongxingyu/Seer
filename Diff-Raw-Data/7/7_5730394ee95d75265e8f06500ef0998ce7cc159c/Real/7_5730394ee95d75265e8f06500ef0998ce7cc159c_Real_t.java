 package evolution.real;
 
 import evolution.*;
 import evolution.individuals.Individual;
 import evolution.individuals.RealIndividual;
 import evolution.operators.AveragingCrossoverOperator;
 import evolution.operators.GaussianMutationOperator;
 import evolution.real.functions.*;
 import evolution.selectors.RouletteWheelSelector;
 import evolution.selectors.TournamentSelector;
 
 import javax.xml.soap.Detail;
 import java.io.*;
 import java.util.*;
 
 public class Real {
 
     /**
      * @param args
      */
     static int maxGen;
     static int popSize;
     static int dimension;
     static double xoverProb;
     static double mutProb;
     static double mutSigma;
     static String logFilePrefix;
     static String resultsFile;
     static int repeats;
     static Vector<Double> weights;
     static String progFilePrefix;
     static String progFile;
     static String bestPrefix;
     static double eliteSize;
    static double mutProbPerBit;
     static Properties prop;
     static String outputDirectory;
     static String objectiveFilePrefix;
     static String objectiveStatsFile;
     static String fitnessFilePrefix;
     static String fitnessStatsFile;
     static String detailsLogPrefix;
 
     public static void main(String[] args) {
 
         prop = new Properties();
         try {
             InputStream propIn = new FileInputStream("properties/ga-real.properties");
             prop.load(propIn);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         maxGen = Integer.parseInt(prop.getProperty("ea.maxGenerations", "20"));
         popSize = Integer.parseInt(prop.getProperty("ea.popSize", "30"));
         xoverProb = Double.parseDouble(prop.getProperty("ea.xoverProb", "0.8"));
         mutProb = Double.parseDouble(prop.getProperty("ea.mutProb", "0.05"));
        mutProbPerBit = Double.parseDouble(prop.getProperty("ea.mutProbPerBit", "0.1"));
         mutSigma = Double.parseDouble(prop.getProperty("ea.mutSigma", "0.04"));
         eliteSize = Double.parseDouble(prop.getProperty("ea.eliteSize", "0.1"));
 
         dimension = Integer.parseInt(prop.getProperty("prob.dimension", "25"));
         repeats = Integer.parseInt(prop.getProperty("xset.repeats", "10"));
 
         ArrayList<RealFunction> functions = new ArrayList<RealFunction>();
         functions.add(new F01SphereFunction(dimension));
         functions.add(new F02EllipsoidalFunction(dimension));
         functions.add(new F03RastriginFunction(dimension));
         functions.add(new F04BucheRastriginFunction(dimension));
         functions.add(new F05LinearSlope(dimension));
         functions.add(new F06AttractiveSectorFunction(dimension));
         functions.add(new F07StepEllipsoidalFunction(dimension));
         functions.add(new F08RosenbrockOriginalFunction(dimension));
         functions.add(new F09RosenbrockRotatedFunction(dimension));
         functions.add(new F10EllipsoidalRotatedFunction(dimension));
         functions.add(new F11DiscusFunction(dimension));
         functions.add(new F12BentCigarFunction(dimension));
         functions.add(new F13SharpRidgeFunction(dimension));
         functions.add(new F14DifferentPowersFunction(dimension));
         functions.add(new F15RastriginNonseparableFunction(dimension));
         functions.add(new F16WeierstrassFunction(dimension));
         functions.add(new F17SchaffersF7Function(dimension));
         functions.add(new F18SchaffersF7IllConditionedFunction(dimension));
         functions.add(new F19CompositeGriewankRosenbrockF8F2Function(dimension));
         functions.add(new F20SchwefelFunction(dimension));
         functions.add(new F21GallaghersGaussian101PeaksFunction(dimension));
         functions.add(new F22GallaghersGaussian21HiPeaksFunction(dimension));
         functions.add(new F23KatsuuraFunction(dimension));
         functions.add(new F24LunacekBiRastriginFunction(dimension));
 
         DetailsLogger.disableLog();
 
         for (RealFunction rf : functions) {
 
             outputDirectory = prop.getProperty("xlog.outputDirectory", "real") + System.getProperty("file.separator") + rf.getClass().getSimpleName();
             logFilePrefix = prop.getProperty("xlog.filePrefix", "log");
             String path = outputDirectory  + System.getProperty("file.separator") + logFilePrefix;
             objectiveFilePrefix = path + ".objective";
             objectiveStatsFile = path + ".objective_stats";
             bestPrefix = path + ".best";
             fitnessFilePrefix = path + ".fitness";
             fitnessStatsFile = path + ".fitness_stats";
             detailsLogPrefix = path + ".details";
 
             File outDir = new File(outputDirectory);
             if (!outDir.exists()) {
                 outDir.mkdirs();
             }
 
             for (int i = 0; i < repeats; i++) {
                 RandomNumberGenerator.getInstance().reseed(i);
                 rf.reinit();
                 run(i, rf);
             }
 
             StatsLogger.processResults(fitnessFilePrefix, fitnessStatsFile, repeats, maxGen, popSize);
             StatsLogger.processResults(objectiveFilePrefix, objectiveStatsFile, repeats, maxGen, popSize);
         }
 
     }
 
     static void run(int number, RealFunction rf) {
 
         try {
 
             RealIndividual sample = new RealIndividual(dimension, -5.0, 5.0);
 
             Population pop = new Population();
             pop.setPopulationSize(popSize);
             pop.setSampleIndividual(sample);
             pop.createRandomInitialPopulation();
 
             EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm();
             ea.addMatingSelector(new MySelector());
             ea.addOperator(new AveragingCrossoverOperator(xoverProb));
            ea.addOperator(new GaussianMutationOperator(mutProb, mutProbPerBit, mutSigma));
             ea.setFitnessFunction(new RealFitnessFunction(rf));
             ea.addEnvironmentalSelector(new TournamentSelector());
             ea.setElite(eliteSize);
 
             OutputStreamWriter fitnessOut = new OutputStreamWriter(new FileOutputStream(fitnessFilePrefix + "." + number));
             OutputStreamWriter objectiveOut = new OutputStreamWriter(new FileOutputStream(objectiveFilePrefix + "." + number));
 
             for (int i = 0; i < maxGen; i++) {
                 ea.evolve(pop);
                 if (i % 100 == 0) {
                     RealIndividual ri = (RealIndividual)pop.getSortedIndividuals().get(0);
                     Double diff = ri.getObjectiveValue();
                     System.out.println("Generation " + i + ": "  + diff + " | " + printArray(ri.toDoubleArray()));
                     System.out.println("\tGradient: " + printArray(rf.numericalDerivative(ri.toDoubleArray())));
                 }
                 StatsLogger.logFitness(pop, fitnessOut);
                 StatsLogger.logObjective(pop, objectiveOut);
             }
 
             RealIndividual ri = (RealIndividual)pop.getSortedIndividuals().get(0);
             Double diff = ri.getObjectiveValue();
             System.out.println("End: " + diff + " | " + printArray(ri.toDoubleArray()));
             System.out.println("\tGradient: " + printArray(rf.numericalDerivative(ri.toDoubleArray())));
 
             OutputStreamWriter bestOut = new OutputStreamWriter(new FileOutputStream(bestPrefix + "." + number));
 
             bestOut.write("Fitness: " + ri.getFitnessValue() + "\n");
             bestOut.write("Objective: " + ri.getObjectiveValue() + "\n");
             bestOut.write("Individual: " + printArray(ri.toDoubleArray()) + "\n");
             bestOut.write("Gradient: " + printArray(rf.numericalDerivative(ri.toDoubleArray())) + "\n");
             bestOut.write("Xopt: " + printArray(rf.getXopt()) + "\n");
             bestOut.write("Fopt: " + rf.getFopt() + "\n");
             bestOut.write("F(Xopt): " + (-rf.evaluate(rf.getXopt())) + "\n");
 
             fitnessOut.close();
             objectiveOut.close();
             bestOut.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
     }
 
     static String printArray(double[] a) {
         StringBuilder sb = new StringBuilder();
 
         for (double d: a) {
             sb.append(String.format(Locale.US, "%.5f", d) + " ");
         }
 
         return sb.toString();
     }
 }
