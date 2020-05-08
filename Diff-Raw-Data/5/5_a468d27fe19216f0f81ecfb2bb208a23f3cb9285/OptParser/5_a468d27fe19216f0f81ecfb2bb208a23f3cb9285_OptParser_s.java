 package eu.vbrlohu.trap.iotools;
 
 import java.io.File;
 
 import org.apache.commons.cli.BasicParser;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 /**
  * Command line parser. Parses command line argument and ensures that these are valid.
  * Provides getter functions for obtaining expected values (e.g. {@link File} in case of input/output files)
  * 
  * @author vjuranek
  *
  */
 public class OptParser {
 
     private static final String INPUT_FILE_OPT = "inputFile";
     private static final String OUPUT_FILE_OPT = "outputFile";
     private static final String ITERATIONS_OPT = "iterations";
    private static final String QUANTITY_OPT = "function";
     private static final String LOG_OPT = "logFile";
     
     private final Options options = createOptions();
     private final String[] args;
     private final CommandLine cmdLine;
 
     public OptParser(String[] args) {
         this.args = args;
         this.cmdLine = parseCmdLine();
     }
     
     /**
      * 
      * @return {@link String} value provided by user as input file parameter
      */
     public String getInputFileOpt() {
         return cmdLine.getOptionValue(INPUT_FILE_OPT);
     }
     
     /**
      * Creates input {@link File} from provided option and checks if the file exists. If doesn't, it exists the TrAP.
      * @return input {@link File}
      */
     public File getInputFile() {
         String fName = cmdLine.getOptionValue(INPUT_FILE_OPT);
         File f = new File(fName);
         if(!f.exists())
             exit(String.format("Input file %s doesn't exists, please provide existing file", fName));
         return f;
     }
 
     /**
      * 
      * @return {@link String} value provided by user as output file parameter
      */
     public String getOutputFileOpt() {
         return cmdLine.getOptionValue(OUPUT_FILE_OPT);
     }
     
     /**
      * 
      * @return
      */
     public Quantity getQuantity() {
         return Quantity.valueOf(cmdLine.getOptionValue(QUANTITY_OPT).toLowerCase());
     }
     
     /**
      * 
      * @return {@link String} name of variable to be computed
      */
     public String getQuantityOpt() {
         return cmdLine.getOptionValue(QUANTITY_OPT);
     }
     
     /**
      * 
      * @return {@link String} value provided by user as number of iterations
      */
     public String getIterationsOpt() {
         return cmdLine.getOptionValue(ITERATIONS_OPT);
     }
     
     /**
      * Converts number of iterations to int. If the provided value cannot be converted to int, exits the TrAP.
      * @return Number of iterations
      */
     public int getIterations() {
         String itr = cmdLine.getOptionValue(ITERATIONS_OPT);
         int i = 0;
         try {
             i = Integer.valueOf(itr).intValue();
         } catch(NumberFormatException e) {
             exit(String.format("Number of iterations %s doesn't seems to be an integer, please provide integer number", itr));
         }
         return i;
     }
     
     /**
      * 
      * @return {@link String} value provided by user as log file parameter
      */
     public String getLogFileOpt() {
         return cmdLine.getOptionValue(LOG_OPT);
     }
     
     /**
      * 
      * Parses command line arguments and exits whole program if there is any problem with parsing arguments.
      * This include also situation when required arguments are not provided.
      */
     private CommandLine parseCmdLine() {
         CommandLineParser parser = new BasicParser();
         CommandLine cl = null;
         try {
             cl = parser.parse(options, args);
         } catch (ParseException e) {
             System.err.println("Parsing of command line arguments failed.  Reason: " + e.getMessage());
             printUsage(options);
             System.exit(1);
         }
         return cl;
     }
 
     private void printUsage(Options options) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("TrAP", options);
     }
     
     private void exit(String msg) {
         System.err.println("Exiting, something went wrong:");
         System.err.println(msg);
         System.exit(1);
     }
 
     /**
      * Creates set of options accepted by TrAP and sets options properties.
      * 
      * @return {@link Options} accepted by TrAP
      */
     private static Options createOptions() {
         // required options
         Option inputFile = OptionBuilder.withArgName(INPUT_FILE_OPT).withLongOpt(INPUT_FILE_OPT).hasArg().isRequired(true)
                 .withDescription("Input data file").create("i");
         Option outputFile = OptionBuilder.withArgName(OUPUT_FILE_OPT).withLongOpt(OUPUT_FILE_OPT).hasArg().isRequired(true)
                 .withDescription("Output data file").create("o");
         Option iterations = OptionBuilder.withArgName(ITERATIONS_OPT).withLongOpt(ITERATIONS_OPT).hasArg().isRequired(true)
                 .withDescription("Number of iterations").create("n");
         Option quantity = OptionBuilder.withArgName(QUANTITY_OPT).withLongOpt(QUANTITY_OPT).hasArg().isRequired(true)
                .withDescription("Variable to be computed").create("f");
 
         // optional options
         Option logFile = OptionBuilder.withArgName(LOG_OPT).withLongOpt(LOG_OPT).hasArg()
                 .withDescription("Log file").create("log");
 
         Options options = new Options();
         options.addOption(inputFile);
         options.addOption(outputFile);
         options.addOption(iterations);
         options.addOption(quantity);
         options.addOption(logFile);
         return options;
     }
 
 }
