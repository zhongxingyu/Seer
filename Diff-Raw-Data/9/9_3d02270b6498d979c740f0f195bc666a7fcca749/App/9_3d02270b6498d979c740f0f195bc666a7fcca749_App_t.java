 package org.drools.planner.examples.ras2012;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.drools.planner.config.EnvironmentMode;
 import org.drools.planner.config.XmlSolverFactory;
 import org.drools.planner.config.solver.SolverConfig;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.core.solver.DefaultSolver;
 import org.drools.planner.examples.ras2012.CLI.ApplicationMode;
 import org.drools.planner.examples.ras2012.util.Chart;
 import org.drools.planner.examples.ras2012.util.SolutionIO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Main class for the application. Supports multiple modes of execution:
  * 
  * <ul>
  * <li>The solver mode takes a user-provided data set, runs Drools Planner and outputs the result.</li>
  * <li>The lookup mode tries to find best solutions for every known data set.</li>
  * <li>The evaluation mode takes a user-provided data set and a solution from that data set and calculates its score.</li>
  * </ul>
  * 
  * Modes of execution are chosen by providing an argument on the command line. List of command line arguments will be shown when
  * the application is run without any arguments.
  */
 public class App {
 
     /**
      * Executes Drools planner on a particular data set.
      */
     private static class SolverRunner implements Callable<HardAndSoftScore> {
 
         private final InputStream   dataset;
         private final String        name;
         private final long          seed;
 
         private static final Random RANDOM = new Random(System.nanoTime());
 
         /**
          * 
          * @param dataset The data set to execute Drools Planner on.
          * @param name Name of the data set.
          * @param seed Random seed for the solver. If < 0, Planner will be set to decide automatically.
          */
         public SolverRunner(final InputStream dataset, final String name, final long seed) {
             this.dataset = dataset;
             this.name = name;
             this.seed = seed;
         }
 
         @Override
         public HardAndSoftScore call() {
             App.logger.info(this.name + " solver starting...");
             final SolutionIO io = new SolutionIO();
             ProblemSolution sol;
             try {
                 sol = io.read(this.dataset);
             } catch (final Exception e) {
                 App.logger.error("Solver " + this.name + " finished unexpectedly. Cause: ", e);
                 return null;
             }
             // and now start solving
             final XmlSolverFactory configurer = new XmlSolverFactory();
             configurer.configure(App.class.getResourceAsStream("/solverConfig.xml"));
             final SolverConfig solverConfig = configurer.getSolverConfig();
             App.logger
                     .info("Overriding Planner enviromnent mode (from {} to {}) to be able to capture the seed.",
                             new Object[] { solverConfig.getEnvironmentMode(),
                                     EnvironmentMode.REPRODUCIBLE });
             solverConfig.setEnvironmentMode(EnvironmentMode.REPRODUCIBLE);
             if (this.seed >= 0) {
                 solverConfig.setRandomSeed(this.seed);
             } else {
                 solverConfig.setRandomSeed(SolverRunner.RANDOM.nextLong());
             }
             final DefaultSolver solver = (DefaultSolver) solverConfig.buildSolver();
             solver.setPlanningProblem(sol);
             solver.solve();
             // output the solution
             if (!App.resultDir.exists()) {
                 App.resultDir.mkdirs();
             }
             sol = (ProblemSolution) solver.getBestSolution();
             final HardAndSoftScore score = sol.getScore();
             final long actualSeed = solverConfig.getEnvironmentMode() == EnvironmentMode.REPRODUCIBLE ? solver
                     .getRandomSeed() : 0;
             if (score.getHardScore() >= 0) { // don't write score that isn't feasible
                 io.writeXML(sol, new File(App.resultDir, this.name + score.getSoftScore() + ".xml"));
                 io.writeTex(sol, actualSeed,
                         new File(App.resultDir, this.name + score.getSoftScore() + ".tex"));
                 sol.visualize(new File(App.resultDir, this.name + score.getSoftScore() + ".png"));
                 App.logger.info("Solver finished. Score: " + score);
             } else {
                 App.logger.warn("Not writing results because solution wasn't feasible: " + score);
             }
             return score;
         }
     }
 
     private static final File            resultDir = new File("data/solutions");
 
     /**
      * Only use up to 2 threads, otherwise the increased GC would negatively impact performance.
      */
     private static final ExecutorService executor  = Executors.newFixedThreadPool(Math.min(2,
                                                            Runtime.getRuntime()
                                                                    .availableProcessors()));
     private static final Logger          logger    = LoggerFactory.getLogger(App.class);
 
     /**
      * Main method of the whole app. Use for launching the app.
      * 
      * @param args Command-line arguments.
      */
     public static void main(final String[] args) {
         final CLI commandLine = CLI.getInstance();
         final ApplicationMode result = commandLine.process(args);
         switch (result) {
             case RESOLVER:
                 App.runSolverMode(commandLine.getDatasetLocation(), commandLine.getSeed());
                 break;
             case LOOKUP:
                 App.runLookupMode();
                 break;
             case EVALUATION:
                 App.runEvaluationMode(commandLine.getDatasetLocation(),
                         commandLine.getSolutionLocation());
                 break;
             case HELP:
             default:
                 commandLine.printHelp();
         }
         if (result == ApplicationMode.ERROR) {
             System.exit(1);
         }
     }
 
     private static void runEvaluationMode(final String datasetLocation,
             final String solutionLocation) {
         // validate data
         final File dataset = new File(datasetLocation);
         if (!dataset.exists() || !dataset.canRead()) {
             throw new IllegalArgumentException("Cannot read data set: " + dataset);
         }
        final File solution = new File(solutionLocation);
         if (!solution.exists() || !solution.canRead()) {
             throw new IllegalArgumentException("Cannot read data set: " + solution);
         }
         // load solution
         final SolutionIO io = new SolutionIO();
         final ProblemSolution result = io.read(dataset, solution);
         App.logger.info("Solution " + result.getName() + " has a score of "
                 + ScoreCalculator.oneTimeCalculation(result) + ".");
     }
 
     private static void runLookupMode() {
         final Set<String> streams = new HashSet<String>();
         streams.add("RDS1");
         streams.add("RDS2");
         streams.add("RDS3");
         // prepare futures
         final Map<String, List<Future<HardAndSoftScore>>> scores = new HashMap<String, List<Future<HardAndSoftScore>>>();
         for (final String entry : streams) {
             App.logger.info("Starting lookup for the best solutions on " + entry + "...");
             scores.put(entry, new ArrayList<Future<HardAndSoftScore>>());
             for (int i = 0; i < 50; i++) {
                 App.logger.info("Scheduled attempt #" + i + ".");
                 scores.get(entry).add(
                         App.executor.submit(new SolverRunner(App.class.getResourceAsStream(entry
                                 + ".txt"), entry, -1)));
             }
         }
         // prepare chart
         final Chart c = new Chart();
         // process futures
         for (final Map.Entry<String, List<Future<HardAndSoftScore>>> entry : scores.entrySet()) {
             final String datasetName = entry.getKey();
             final List<Integer> values = new ArrayList<Integer>();
             for (final Future<HardAndSoftScore> future : entry.getValue()) {
                 try {
                     final HardAndSoftScore result = future.get();
                     if (result != null && result.getHardScore() >= 0) {
                         values.add(Math.abs(result.getSoftScore()));
                     }
                 } catch (final Exception e) {
                     App.logger.error("One of the solvers failed.", e);
                 }
             }
             c.addData(values, datasetName);
         }
         // plot chart
         c.plot(App.resultDir, "chart");
         new SolutionIO().writeChart(c.getDataset(), new File(App.resultDir, "stats.tex"));
         App.shutdownExecutor();
     }
 
     private static void runSolverMode(final String datasetLocation, final long seed) {
         final File f = new File(datasetLocation);
         if (!f.exists() || !f.canRead()) {
             throw new IllegalArgumentException("Cannot read data set: " + f);
         }
         try {
             App.executor.submit(new SolverRunner(new FileInputStream(f), f.getName(), seed));
             App.shutdownExecutor();
         } catch (final FileNotFoundException e) {
             App.logger.warn(f.getName() + " solver not started. Cause: ", e);
         }
     }
 
     private static void shutdownExecutor() {
         App.executor.shutdown();
         while (!App.executor.isTerminated()) {
             try {
                 App.executor.awaitTermination(1, TimeUnit.SECONDS);
             } catch (final InterruptedException e) {
                 // nothing we could do here
             }
         }
     }
 }
