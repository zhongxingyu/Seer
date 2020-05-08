 package ngenes2.examples;
 
 import java.util.List;
 import java.util.Random;
 import ngenes2.ClassicInstanciator;
 import ngenes2.breeder.Breeder;
 import ngenes2.breeder.ClassicalBreeder;
 import ngenes2.evolver.ClassicEvolver;
 import ngenes2.evolver.Evolver;
 import ngenes2.evolver.monitor.GenerationMonitor;
 import ngenes2.evolver.stop.FitnessTarget;
 import ngenes2.evolver.stop.MaxGeneration;
 import ngenes2.evolver.stop.StopCondition;
 import ngenes2.individual.LinearIndividual;
 import ngenes2.individual.generator.Generator;
 import ngenes2.individual.generator.bool.RandomBooleanGenerator;
 import ngenes2.ops.crossover.Crossover;
 import ngenes2.ops.crossover.MidBreakCrossover;
 import ngenes2.ops.mutator.Mutator;
 import ngenes2.ops.mutator.PointMutation;
 import ngenes2.ops.mutator.genes.bool.BooleanFlipper;
 import ngenes2.ops.selector.KTournament;
 import ngenes2.ops.selector.Selector;
 import ngenes2.population.BasicPopulation;
 import ngenes2.population.Population;
 import ngenes2.util.Properties;
 import ngenes2.xml.XMLParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 public class MaxOnes {
 
     private final static Logger logger = LoggerFactory.getLogger(MaxOnes.class);
 
     public static class Fitness implements ngenes2.fitness.Fitness<Boolean> {
         public double compute(List<Boolean> chromosome) {
             double sum = 0.0;
             for( Boolean b: chromosome ) {
                 if( ! b ) {
 
                     sum += 1.0;
                 }
             }
             return sum / chromosome.size();
         }
     }
 
     public final static GenerationMonitor<Boolean,LinearIndividual<Boolean>> monitor
             = new GenerationMonitor<Boolean, LinearIndividual<Boolean>>() {
         public void newGeneration(int generationNumber, Population<Boolean, LinearIndividual<Boolean>> pop) {
             LinearIndividual<Boolean> best = pop.stats().best();
             logger.info("Generation {}: best individual fitness = {}", generationNumber, best.fitness());
         }
     };
     
     private static void exampleByHand() {
         Random rng = new Random();
         final int indSize = 20;
         final int popSize = 100;
         final int genNum = 50;
         Properties props = new Properties()
                 .put("tournament_size",3)
                 .put("population_size",20)
                 .put("chromosome_size", indSize)
                 .put("fitness_target", 10e-9)
                 .put("max_generation", genNum);
 
         Generator<Boolean,LinearIndividual<Boolean>> gen =
                 new Generator<Boolean, LinearIndividual<Boolean>>(
                 new LinearIndividual.Factory<Boolean>(),
                 new Fitness(),
                 new RandomBooleanGenerator(rng, props)
                 );
         Population<Boolean,LinearIndividual<Boolean>> pop =
                 new BasicPopulation<Boolean, LinearIndividual<Boolean>>( 
                   gen.generate(props.getInt("population_size"))
                 );
 
         Selector<LinearIndividual<Boolean>> sel = 
                 new KTournament<LinearIndividual<Boolean>>(rng,props);
         Crossover<Boolean,LinearIndividual<Boolean>> co =
                 new Crossover<Boolean, LinearIndividual<Boolean>>( new MidBreakCrossover<Boolean>() );
         Mutator<Boolean,LinearIndividual<Boolean>> mut = new Mutator<Boolean, LinearIndividual<Boolean>>(
                     new PointMutation<Boolean>( rng, new BooleanFlipper() )
                 );
         StopCondition<Boolean,LinearIndividual<Boolean>> stop =
                 new FitnessTarget<Boolean,LinearIndividual<Boolean>>(props)
                 .or( new MaxGeneration<Boolean,LinearIndividual<Boolean>>(props) );
         Breeder<Boolean,LinearIndividual<Boolean>> breeder = 
                 new ClassicalBreeder<Boolean,LinearIndividual<Boolean>>(co, mut);
         Evolver<Boolean,LinearIndividual<Boolean>> evolver =
                 new ClassicEvolver<Boolean, LinearIndividual<Boolean>>(sel, breeder, monitor,stop);
         evolver.evolve(pop);
     }
 
     @SuppressWarnings("unchecked")
     private static void exampleWithClassicInstanciator() {
         Properties prop = new Properties()
                 .put("tournament_size",3)
                 .put("chromosome_size", 200)
                 .put("population_size", 100)
                 .put("fitness_target", 10e-9)
                 .put("max_generation", 500);
         ClassicInstanciator inst = new ClassicInstanciator()
                 .with(prop)
                 .with(LinearIndividual.Factory.class)
                 .with(new Fitness())
                 .with(RandomBooleanGenerator.class)
                 .with(KTournament.class)
                 .with(BooleanFlipper.class)
                 .with(PointMutation.class)
                 .with(MidBreakCrossover.class)
                 .with(monitor)
                 .with( new FitnessTarget(prop).or( new MaxGeneration(prop) ) );
         Population result = inst.run();
     }
     
     @SuppressWarnings("unchecked")
     private static void exampleFromXML() {
         String filename = "examples-resources/maxones.xml";
         try {
             XMLParser parser = XMLParser.fromFile(filename);
             Population result = parser.result();
         } catch (Exception ex) {
             logger.error("An error occured while parsing", ex);
         }
     }
 
     public static void main(String[] args) {
        exampleByHand();
         //exampleWithClassicInstanciator();
        //exampleFromXML();
     }
 }
