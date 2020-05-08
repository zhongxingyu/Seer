 package src;
 
 import java.io.IOException;
 
 
 import src.etch.checker.Check;
 import src.promela.lexer.LexerException;
 import src.promela.parser.ParserException;
 import src.symmextractor.SymmExtractor;
 import src.symmreducer.SymmReducer;
 import src.utilities.AbsentConfigurationFileException;
 import src.utilities.BadConfigurationFileException;
 import src.utilities.BooleanOption;
 import src.utilities.CommandLineSwitch;
 import src.utilities.Config;
 import src.utilities.IntegerOption;
 import src.utilities.Profile;
 import src.utilities.ProgressPrinter;
 import src.utilities.StrategyOption;
 import src.utilities.StringOption;
 
 public class TopSpin {
 
 	public static void main(String args[]) throws IOException, InterruptedException, BadConfigurationFileException, AbsentConfigurationFileException, ParserException, LexerException {
 
 		Config.initialiseCommandLineSwitches();
 		
 		if((args.length > 0) && (args[0].toUpperCase().equals("HELP"))) {
 			processHelpArguments(args);
 			System.exit(0);
 		}
 
 		int currentArg = 0;
 		
 		while((currentArg < args.length) && processCommandLineSwitch(args[currentArg].toUpperCase())) {
 			currentArg++;
 		}
 		
 		if(currentArg >= args.length) {
 			System.out.println("Error: no input file specified.\n");
 			System.out.println("To run TopSPIN on an input file:\n" +
 					           "    [check, detect] <inputfile>\n" +
 							   "For help on command-line or config file option:\n" +
 							   "    help <option>\n" +
 							   "For list of options:\n" +
 							   "    help\n");
 			System.exit(1);
 		}
 		
 		String specificationFile = args[currentArg];
 
 		if((args.length-currentArg) > 1) {
 			System.out.println("Warning: using argument " + (currentArg+1) + ", \"" + specificationFile + "\", as command line argument, ignoring remaining arguments.");
 		}
 		
 		System.out.println("File: " + specificationFile);
 
 		if(Config.commandLineSwitchIsSet(CommandLineSwitch.CHECK)) {
 			Config.resetConfiguration();
			Config.setUnspecifiedOptionsToDefaultValues();
 			System.out.println("Type-check only");
 			new Check(specificationFile).typecheck(true);
 			System.exit(0);
 		}
 
 		Config.readConfigFile("config.txt", true, true);
 		
 		if(Config.commandLineSwitchIsSet(CommandLineSwitch.DETECT)) {
 			SymmExtractor extractor = new SymmExtractor(specificationFile);
 			doAutomaticSymmetryDetection(specificationFile, extractor);
 		} else {
 			ProgressPrinter.printSeparator();
 			ProgressPrinter.println("TopSPIN version " + Config.VERSION);
 			ProgressPrinter.printSeparator();
 			Config.printConfiguration();
 			ProgressPrinter.printSeparator();
 	
 			if(Config.profiling()) { Profile.TOPSPIN_START = System.currentTimeMillis(); }
 	
 			SymmReducer reducer = new SymmReducer(specificationFile);
 	
 			doAutomaticSymmetryReduction(reducer);
 	
 			if(Config.profiling()) { Profile.TOPSPIN_END = System.currentTimeMillis(); Profile.show(); }
 		}
 		
 	}
 
 	private static boolean processCommandLineSwitch(String arg) {
 
 		if(processSwitchVariant(arg, CommandLineSwitch.CHECK, CommandLineSwitch.DETECT)) {
 			return true;
 		}
 
 		if(processSwitchVariant(arg, CommandLineSwitch.DETECT, CommandLineSwitch.CHECK)) {
 			return true;
 		}
 		
 		return false;
 
 	}
 
 	private static boolean processSwitchVariant(String arg, CommandLineSwitch commandLineSwitch, CommandLineSwitch otherSwitch) {
 		if(arg.equals(commandLineSwitch.toString())) {
 			if(Config.commandLineSwitchIsSet(commandLineSwitch)) {
 				System.out.println("Warning: duplicate command line switch " + commandLineSwitch + ".");
 			} else if(Config.commandLineSwitchIsSet(otherSwitch)) {
 				System.out.println("Warning: " + commandLineSwitch + " switch has been ignored as it is specified after " + otherSwitch + " switch.");
 			} else {
 				Config.setCommandLineSwitch(commandLineSwitch);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private static void processHelpArguments(String[] args) {
 		
 		assert(args.length>0);
 
 		Config.resetConfiguration();
 		
 		if(args.length==1) {
 			System.out.println("\nUse 'help' followed by name of config file or command-line option for summary of what the option does.\n");
 
 			System.out.println("Available options:\n");
 
 			System.out.println("Command line switches");
 			System.out.println("=====================");
 			for(CommandLineSwitch option : CommandLineSwitch.values()) {
 				System.out.println("  " + option.toString().toLowerCase());
 			}
 			System.out.println("\nBoolean config file");
 			System.out.println("===================");
 			
 			for(BooleanOption option : BooleanOption.values()) {
 				System.out.println("  " + option.toString().toLowerCase());
 			}
 			
 			System.out.println("\nInteger config file options");
 			System.out.println("===========================");
 
 			for(IntegerOption option : IntegerOption.values()) {
 				System.out.println("  " + option.toString().toLowerCase());
 			}
 
 			System.out.println("\nString config file options");
 			System.out.println("==========================");
 
 			for(StringOption option : StringOption.values()) {
 				System.out.println("  " + option.toString().toLowerCase());
 			}
 
 			// A strategy option is really just a string option
 			for(StrategyOption option : StrategyOption.values()) {
 				System.out.println("  " + option.toString().toLowerCase());
 			}
 			
 		} else {
 			Config.showHelpForConfigurationOption(args[1]);
 		}
 	}
 
 	public static void doAutomaticSymmetryReduction(SymmReducer reducer) throws IOException, InterruptedException {
 		reducer.reduce();
 		reducer.destroyGAP();
 	}
 
 	public static void doAutomaticSymmetryDetection(String filename, SymmExtractor extractor) throws IOException, InterruptedException {
 		if(Config.profiling()) { Profile.TOPSPIN_START = System.currentTimeMillis(); }
 		ProgressPrinter.println("File: " + filename);
 		ProgressPrinter.println("Detect symmetry only");
 		ProgressPrinter.println("Using " + Config.getIntegerOption(IntegerOption.CONJUGATES) + " random conjugate" + (Config.getIntegerOption(IntegerOption.CONJUGATES)==1?"":"s"));
 		ProgressPrinter.println("Timeout for finding largest valid subgroup: " + Config.getIntegerOption(IntegerOption.TIMEBOUND) + " seconds");
 		extractor.extract();
 		extractor.destroyGAP();
 		if(Config.profiling()) { Profile.TOPSPIN_END = System.currentTimeMillis(); Profile.show(); }
 	}
 
 }
