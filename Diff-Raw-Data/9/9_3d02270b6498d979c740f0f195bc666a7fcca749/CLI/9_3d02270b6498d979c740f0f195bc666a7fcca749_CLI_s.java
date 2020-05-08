 package org.drools.planner.examples.ras2012;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 /**
  * Command-line interface for the app. Processes command-line arguments and decides which application mode should run. This
  * class implements the singleton pattern.
  */
 public class CLI {
 
     /**
      * The modes that the application supports running in.
      * 
      */
     public static enum ApplicationMode {
 
         EVALUATION, LOOKUP, RESOLVER, HELP, ERROR;
 
     }
 
     private static final CLI INSTANCE = new CLI();
 
     /**
      * Return the single instance of this class.
      * 
      * @return The instance.
      */
     public static CLI getInstance() {
         return CLI.INSTANCE;
     }
 
     private final Options options        = new Options();
 
     private final Option  evaluationMode = new Option("e", "evaluation", false,
                                                  "Run the application in evaluation mode.");
     private final Option  lookupMode     = new Option("l", "lookup", false,
                                                  "Run the application in lookup mode.");
     private final Option  solverMode     = new Option("r", "resolver", false,
                                                  "Run the application in resolving mode.");
     private final Option  dataset        = new Option("d", "dataset", true,
                                                  "Location of the data set. Ignored in lookup mode.");
     private final Option  solution       = new Option("s", "solution", true,
                                                  "Location of the resolved solution for evaluation mode. Ignored in other modes.");
     private final Option  seed           = new Option("x", "seed", true,
                                                  "Random seed for the solver mode. Will be ignored in every other mode.");
     private final Option  help           = new Option("h", "help", false,
                                                  "Display this help and exit.");
 
     private String        errorMessage   = null;
     private boolean       isError        = false;
 
     public String         datasetLocation = null, solutionLocation = null;
     public long           solverSeed      = -1;
 
     /**
      * The constructor is hidden, as should be with the singleton pattern.
      */
     private CLI() {
         // pick in which mode the application should run
         final OptionGroup applicationMode = new OptionGroup();
         applicationMode.addOption(this.evaluationMode);
         applicationMode.addOption(this.lookupMode);
         applicationMode.addOption(this.solverMode);
         applicationMode.addOption(this.help);
         applicationMode.setRequired(true);
         // and formulate the options
         this.options.addOptionGroup(applicationMode);
         this.options.addOption(this.dataset);
         this.options.addOption(this.solution);
         this.options.addOption(this.seed);
     }
 
     /**
      * Get a location of the data-set.
      * 
      * @return Path to a data set file, or null if not provided.
      */
     public String getDatasetLocation() {
         return this.datasetLocation;
     }
 
     public long getSeed() {
         return this.solverSeed;
     }
 
     /**
      * Get a location of the solution.
      * 
      * @return Path to the solution file, or null if not provided.
      */
     public String getSolutionLocation() {
         return this.solutionLocation;
     }
 
     /**
      * Prints a help message, describing the usage of the app from the command-line.
      */
     public void printHelp() {
         if (this.isError) {
             System.out.println(this.errorMessage);
         }
         final HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("java -jar ras2012.jar", this.options, true);
     }
 
     /**
      * Process the command-line argument and make a decision about which application mode should run.
      * 
      * @param args Command-line arguments.
      * @return The application mode. If it is {@link ApplicationMode#ERROR}, the cause of the problem may be found in
      *         {@link #errorMessage}. In other cases, {@link #getDatasetLocation()} and {@link #getSolutionLocation()} may be
      *         used to retrieve the important arguments from the command line.
      */
     public ApplicationMode process(final String[] args) {
         this.datasetLocation = null;
         this.solutionLocation = null;
         final CommandLineParser parser = new GnuParser();
         try {
             final CommandLine cli = parser.parse(this.options, args);
             final List<Option> presentOptions = Arrays.asList(cli.getOptions());
             if (this.isError) {
                 return ApplicationMode.ERROR;
             } else if (presentOptions.contains(this.solverMode)) {
                 if (!presentOptions.contains(this.dataset)) {
                     this.setError("You must provide a data set to resolve.");
                     return ApplicationMode.ERROR;
                 } else {
                     if (presentOptions.contains(this.seed)) {
                         final String seed = cli.getOptionValue(this.seed.getOpt());
                         try {
                             final long actualSeed = new Long(seed);
                             this.setSeed(actualSeed);
                         } catch (final NumberFormatException ex) {
                             this.setError("Seed, when provided, must be a non-negative integer.");
                             return ApplicationMode.ERROR;
                         }
                     }
                     this.setDatasetLocation(cli.getOptionValue(this.dataset.getOpt()));
                     return ApplicationMode.RESOLVER;
                 }
             } else if (presentOptions.contains(this.evaluationMode)) {
                 if (!presentOptions.contains(this.dataset)
                        || !presentOptions.contains(this.solution.getOpt())) {
                     this.setError("You must provide a data set and a solution to evaluate.");
                     return ApplicationMode.ERROR;
                 } else {
                     this.setDatasetLocation(cli.getOptionValue(this.dataset.getOpt()));
                     this.setSolutionLocation(cli.getOptionValue(this.solution.getOpt()));
                     return ApplicationMode.EVALUATION;
                 }
             } else if (presentOptions.contains(this.lookupMode)) {
                 return ApplicationMode.LOOKUP;
             } else {
                 return ApplicationMode.HELP;
             }
         } catch (final ParseException e) {
             this.setError(e.getMessage());
             return ApplicationMode.ERROR;
         }
     }
 
     private void setDatasetLocation(final String datasetLocation) {
         this.datasetLocation = datasetLocation;
     }
 
     private boolean setError(final String message) {
         if (!this.isError) {
             this.isError = true;
             this.errorMessage = message;
             return true;
         }
         return false;
     }
 
     private void setSeed(final long seed) {
         this.solverSeed = seed;
     }
 
     private void setSolutionLocation(final String solutionLocation) {
         this.solutionLocation = solutionLocation;
     }
 
 }
