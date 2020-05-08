 package to.richard.tsp;
 
 /**
  * Author: Richard To
  * Date: 2/11/13
  */
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 /**
  * Simulates Traveling Salesperson problem using Genetic Algorithm.
  */
 public class TSPSimulator {
 
     public enum PROBLEM_TYPE { MAXIMIZATION, MINIMIZATION };
     public enum CROSSOVER { PMX, EX, CX, OX };
     public enum MUTATION { INVERSION };
     public enum SURVIVOR_SELECTION { AGE_BASED, GENITOR, ELITISM };
     public enum PARENT_SELECTION { FPS, TOURNAMENT };
     public enum SAMPLER { ROULETTE, SUS };
     public enum TRANSFORMS { NONE, WINDOWING, EXPONENTIAL, LINEAR_NORMALIZATION };
 
     /**
      * Options for TSP Simulator.
      *
      * Default options are provided.
      *
      * Thee are also a few private options that will be set by
      * the simulator.
      */
     public static class Options {
 
         // Public Simulator Options
         // ------------------------
 
         // Population size must be greater 1.
         int populationSize = 10;
 
         // Number of generations must be greater than 0.
        int generations = 300;
 
         // Seed for cost matrix PRNG.
         int costMatrixSeed = 1;
 
         // Number of cities in cost matrix must be greater 1.
        int cities = 10;
 
         // Minimum price of 1-way airfare. Must be greater than 0. Inclusive.
         int minPrice = 99;
 
         // Maximum price of 1-way airfare. Must be greater than min price. Inclusive.
         int maxPrice = 2000;
 
         // Default to minimization problem since we are looking for lowest price.
         public PROBLEM_TYPE problemType = PROBLEM_TYPE.MINIMIZATION;
 
         // Parent selection algorithm. Default is FPS.
         public PARENT_SELECTION parentSelection = PARENT_SELECTION.FPS;
 
         // Used by FPS algorithms to transform fitness values to avoid
         // stagnation and premature convergence.
         // Uses Linear normalization as the default (Rank selection)
         public TRANSFORMS transforms = TRANSFORMS.LINEAR_NORMALIZATION;
 
         // Used by FPS algorithms to do the actual selection of parents from population.
         // Default is stochastic universal sampling.
         public SAMPLER sampler = SAMPLER.SUS;
 
         // Used for tournament selection. Size must be greater than 0.
         int tournamentSize = 2;
 
         //Crossover algorithm. PMX is default.
         public CROSSOVER crossover = CROSSOVER.PMX;
 
         // Probability of crossover is between 0 and 1.0. Default is .9.
         double probabilityCrossover = .9;
 
         // Probability of mutation is between 0 and 1.0. Default is .1.
         public MUTATION mutation = MUTATION.INVERSION;
         double probabilityMutation = .1;
 
         // Algorithm for survivor selection. Default is Elitism.
         public SURVIVOR_SELECTION survivorSelection = SURVIVOR_SELECTION.ELITISM;
 
 
         // Private Simulator Options
         // ------------------------
 
         private Random random;
         private CostMatrix costMatrix;
         private FitnessEvaluator fitnessEvaluator;
         private Comparator<Pair<Double, Genotype>> comparator;
     }
 
     /**
      * Simulate TSP problem with given options.
      */
     public void simulate(Options opts) {
 
         int currentGeneration = 0;
 
         opts.random = new Random();
 
         CostMatrixBuilder costMatrixBuilder = new CostMatrixBuilder(new Random(opts.costMatrixSeed));
         opts.costMatrix = costMatrixBuilder.buildMatrix(
                 opts.populationSize, opts.minPrice, opts.maxPrice);
 
         opts.fitnessEvaluator = new FitnessEvaluator(opts.costMatrix);
 
         opts.comparator = buildComparator(opts);
 
         FitnessAnalyzer fitnessAnalyzer = new FitnessAnalyzer(opts.fitnessEvaluator, opts.comparator);
 
         GenePoolInitializer genePoolInitializer = new GenePoolInitializer(
                opts.cities, opts.costMatrix, opts.random);
 
         IParentSelector parentSelector = buildParentSelector(opts);
 
         Recombinator recombinator = new Recombinator(
                 opts.probabilityCrossover, buildCrossoverOperator(opts), opts.random);
 
         Mutator mutator = new Mutator(
                 opts.probabilityMutation, buildMutationOperator(opts), opts.random);
 
         ISurvivorSelector survivorSelector = buildSurvivorSelector(opts);
 
         List<Genotype> population = genePoolInitializer.initializeGenePool();
 
         System.out.println(fitnessAnalyzer.analyze(population));
 
         while (currentGeneration < opts.generations) {
 
             // Parent Selection
             List<Genotype> parents = parentSelector.selectParents(population);
 
             // Crossover
             List<Genotype> offspring = recombinator.recombine(parents);
 
             // Mutation
             List<Genotype> mutatedOffspring = mutator.mutate(offspring);
 
             // Survivor Selection
             population = survivorSelector.replace(parents, mutatedOffspring);
 
             currentGeneration++;
         }
         System.out.println(fitnessAnalyzer.analyze(population));
     }
 
     /**
      * Factor method to build comparator based on problem type (MIN, MAX)
      */
     private Comparator<Pair<Double, Genotype>> buildComparator(Options opts) {
         Comparator<Pair<Double, Genotype>> comparator = null;
         switch (opts.problemType) {
             case MAXIMIZATION:
                 comparator = new FitnessMaximizationComparator();
                 break;
             case MINIMIZATION:
                 comparator = new FitnessMinimizationComparator();
                 break;
         }
         return comparator;
     }
 
     /**
      * Factory method to build Parent Selector class.
      */
     private IParentSelector buildParentSelector(Options opts) {
         IParentSelector parentSelector = null;
         switch (opts.parentSelection) {
             case FPS:
                 parentSelector = new FitnessProportionateSelector(
                         opts.fitnessEvaluator, buildSampler(opts), opts.random, buildTransforms(opts));
                 break;
             case TOURNAMENT:
                 parentSelector = new TournamentSelector(
                         opts.tournamentSize, opts.fitnessEvaluator,
                         opts.comparator, opts.random);
                 break;
         }
         return parentSelector;
     }
 
     /**
      * Factory method to build Sampler
      */
     private ISampler<Genotype> buildSampler(Options opts) {
         ISampler<Genotype> sampler = null;
         switch (opts.sampler) {
             case ROULETTE:
                 sampler = new RouletteWheel<Genotype>(opts.random);
                 break;
             case SUS:
                 sampler = new StochasticUniversalSampler<Genotype>(opts.random);
                 break;
         }
         return sampler;
     }
 
     /**
      * Factory method to build Transforms.
      */
     private List<IFPSTransform> buildTransforms(Options opts) {
         ArrayList<IFPSTransform> transforms = new ArrayList<IFPSTransform>();
         switch (opts.transforms) {
             case NONE:
                 break;
             case WINDOWING:
                 if (opts.problemType == PROBLEM_TYPE.MINIMIZATION) {
                     transforms.add(new FPSMinimization());
                 }
                 transforms.add(new FPSWindowing());
                 break;
             case EXPONENTIAL:
                 if (opts.problemType == PROBLEM_TYPE.MINIMIZATION) {
                     transforms.add(new FPSMinimization());
                 }
                 transforms.add(new FPSWindowing());
                 break;
             case LINEAR_NORMALIZATION:
                 transforms.add(new FPSLinearNormalization(opts.comparator));
                 break;
         }
         return transforms;
     }
 
     /**
      * Factory method to build crossover operator.
      */
     private ICrossoverOperator buildCrossoverOperator(Options opts) {
         ICrossoverOperator crossoverOperator = null;
         switch (opts.crossover) {
             case PMX:
                 crossoverOperator = new PartiallyMappedCrossover();
                 break;
             case EX:
                 crossoverOperator = new EdgeCrossover();
                 break;
             case CX:
                 crossoverOperator = new CycleCrossover();
                 break;
             case OX:
                 crossoverOperator = new OrderCrossover();
                 break;
         }
         return crossoverOperator;
     }
 
     /**
      * Factory method to build mutation operator.
      *
      * Currently just returns inversion mutation since that's the
      * only one I had time to implement.
      */
     private IMutationOperator buildMutationOperator(Options opts) {
         return new InversionMutation();
     }
 
     /**
      * Factory method to build survivor selector class.
      */
     private ISurvivorSelector buildSurvivorSelector(Options opts) {
         ISurvivorSelector survivorSelector = null;
         switch (opts.survivorSelection) {
             case AGE_BASED:
                 survivorSelector = new AgeBasedSurvivorSelector();
                 break;
             case GENITOR:
                 survivorSelector = new ReplaceWorstSurvivorSelection(opts.fitnessEvaluator, opts.comparator);
                 break;
             case ELITISM:
                 survivorSelector = new ElitismSurvivorSelection(opts.fitnessEvaluator, opts.comparator);
                 break;
         }
         return survivorSelector;
     }
 }
