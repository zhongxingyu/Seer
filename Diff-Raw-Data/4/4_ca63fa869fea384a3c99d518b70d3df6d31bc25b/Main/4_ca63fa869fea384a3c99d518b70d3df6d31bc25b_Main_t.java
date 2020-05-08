 package org.fit.cvut.mvi;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.fit.cvut.mvi.cgp.CGPConfiguration;
 import org.fit.cvut.mvi.cgp.CGPEvolution;
 import org.fit.cvut.mvi.cgp.CGPEvolutionConfiguration;
 import org.fit.cvut.mvi.evaluator.FitnessEvaluator;
 import org.fit.cvut.mvi.model.Genome;
 import org.fit.cvut.mvi.model.functions.Addition;
 import org.fit.cvut.mvi.model.functions.Function;
 import org.fit.cvut.mvi.model.functions.Inputs;
 import org.fit.cvut.mvi.model.functions.Multiplication;
 import org.fit.cvut.mvi.model.functions.Sine;
 import org.fit.cvut.mvi.model.functions.SquareRoot;
 import org.fit.cvut.mvi.model.functions.Subtraction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Main {
 
     private static Logger logger = LoggerFactory.getLogger(Main.class);
 
     /*
      * Fitness evaluator and configuration resources
      */
     public static final String NETLOGO_PATH = "/home/matej/Downloads/netlogo-5.0.2/NetLogo.jar";
     public static final String TEMPLATE_PATH = "sablona.nlogo";
     public static final String SETUP_PATH = "sablona.xml";
     public static final String CONFIG_FILE = "src/main/resources/cgp.properties";
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         try {
             Configuration appConfig = new PropertiesConfiguration(CONFIG_FILE);
 
             // Create CGP configuration
             List<Function> functions = getFunctions();
             List<Function> inputs = getInputs();
 
             CGPConfiguration config = new CGPConfiguration.Builder().functions(functions).inputs(inputs)
                     .outputs(appConfig.getInt("genome.outputs")).rows(appConfig.getInt("genome.rows"))
                     .columns(appConfig.getInt("genome.columns")).levelsBack(appConfig.getInt("genome.levelsBack")).build();
             CGPEvolutionConfiguration evolutionConfig = new CGPEvolutionConfiguration.Builder()
                    .populationSize(appConfig.getInt("evolution.populationSize")).mutations(appConfig.getInt("evolution.mutations"))
                    .generations(appConfig.getInt("evolution.generations")).build();
 
             logger.debug(config.toString());
             logger.debug(evolutionConfig.toString());
 
             // Create fitness evaluator
             FitnessEvaluator evaluator = new FitnessEvaluator(NETLOGO_PATH, TEMPLATE_PATH, SETUP_PATH);
 
             // Evolution
             CGPEvolution evolution = new CGPEvolution(config, evaluator);
             Genome result = evolution.evolve(evolutionConfig);
 
             // Print results
             System.out.println(result.decode());
 
         } catch (ConfigurationException e) {
             e.printStackTrace();
         }
     }
 
     public static List<Function> getFunctions() {
         List<Function> functions = new ArrayList<>();
 
         functions.add(new Addition());
         functions.add(new Subtraction());
         functions.add(new Multiplication());
         // functions.add(new Modulo());
         functions.add(new SquareRoot());
         functions.add(new Sine());
 
         return functions;
 
     }
 
     public static List<Function> getInputs() {
         List<Function> inputs = new ArrayList<>();
 
         inputs.add(Inputs.constant(45));
         // Grass patches
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.NORTH));
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.SOUTH));
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.EAST));
         inputs.add(Inputs.patchAt(Inputs.GRASS, Inputs.WEST));
         // Dirt patches
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.NORTH));
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.SOUTH));
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.EAST));
         inputs.add(Inputs.patchAt(Inputs.DIRT, Inputs.WEST));
         // Trap patches
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.NORTH));
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.SOUTH));
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.EAST));
         inputs.add(Inputs.patchAt(Inputs.TRAP, Inputs.WEST));
         inputs.add(Inputs.patchAhead(Inputs.TRAP, 3));
         // Wolves
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.NORTH));
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.SOUTH));
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.EAST));
         inputs.add(Inputs.turtlesAt(Inputs.WOLVES, Inputs.WEST));
         inputs.add(Inputs.turtlesInCone(Inputs.WOLVES, 3, 90));
         // Sheep
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.NORTH));
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.SOUTH));
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.EAST));
         inputs.add(Inputs.turtlesAt(Inputs.SHEEP, Inputs.WEST));
         inputs.add(Inputs.turtlesInCone(Inputs.SHEEP, 3, 90));
 
         return inputs;
     }
 
 }
