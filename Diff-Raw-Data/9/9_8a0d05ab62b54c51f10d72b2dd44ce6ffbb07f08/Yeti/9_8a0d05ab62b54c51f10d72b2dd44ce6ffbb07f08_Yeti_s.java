 package yeti;
 
 
 import yeti.environments.YetiProgrammingLanguageProperties;
 import yeti.environments.YetiTestManager;
 import yeti.environments.java.YetiJavaProperties;
 import yeti.strategies.YetiRandomStrategy;
 
 /**
  * Class that represents the main launching class of Yeti
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jun 22, 2009
  */
 public class Yeti {
 
 	/**
 	 * The properties of the programming language.
 	 */
 	public static YetiProgrammingLanguageProperties pl;
 	
 	/**
 	 * The strategy being used.
 	 */
 	public static YetiStrategy strategy = null;
 	
 	/**
 	 * Main method of Yeti. Arguments are numerous. Here is a list of the current ones:
 	 * 
 	 * -java, -Java : for calling it on Java.
 	 * -time=Xs, -time=Xmn : for calling Yeti for a given amount of time (X can be minutes or seconds, e.g. 2mn | 3s ).
 	 * -nTests=X : for calling Yeti to attempt X method calls.
 	 * -testModules=M1:M2:...:Mn : for testing one or several modules.
 	 * -help, -h: prints the help out.
 	 * 
 	 * @param args the arguments of the program
 	 */
 	public static void main(String[] args) {
 		
 		YetiEngine engine;
 		boolean isJava = false;
 		boolean isTimeout = false;
 		int timeOutSec=0;
 		boolean isNTests = false;
 		int nTests=0;
 		String []modulesToTest=null;
 		
 		// we parse all arguments of the program
 		for (String s0: args) {
 			// if it is printing help
 			if (s0.equals("-help")||s0.equals("-h")) {
 				Yeti.printHelp();
 				return;
 			}
 			// if Java
 			if (s0.equals("-java")||s0.equals("-Java")) {
 				isJava = true;
 				continue;
 			}
 			// if testing for time value
 			if (s0.startsWith("-time=")) {
 				isTimeout=true;
 				int size = s0.length();
 				// if the time value is in seconds
 				if (s0.substring(size-1).equals("s")) {
 					timeOutSec=(Integer.parseInt(s0.substring(6, size-1)));
 					continue;
 				}
 				// if the time value is in minutes
 				if (s0.substring(size-2).equals("mn")) {
 					timeOutSec=60*(Integer.parseInt(s0.substring(6, size-2)));
 					continue;	
 				}				
 			}
 			// if it is for a number of tests
 			if (s0.startsWith("-nTests=")) {
 				isNTests=true;
 				nTests=(Integer.parseInt(s0.substring(8)));
 				continue;
 			}
 			// we want to test these modules
 			if (s0.startsWith("-testModules=")) {
 				String s1=s0.substring(13);
 				modulesToTest=s1.split(":");
 				continue;
 			}
 			System.out.println("Yeti could not understand option: "+s0);
 			Yeti.printHelp();
 			return;
 
 		}
 		
 		//test of options to set up the YetiProperties
 		if (isJava) {
 			pl=new YetiJavaProperties();
 		};
 		
 		// initializing Yeti
 		try {
 			pl.getInitializer().initialize(args);
 		} catch (YetiInitializationException e) {
 			//should never happen
 			e.printStackTrace();
 		}
 		
 		// create a YetiTestManager and 
 		YetiTestManager testManager = pl.getTestManager(); 
 		
 		// We set the strategy
 		strategy= new YetiRandomStrategy(testManager);
 
 		// getting the module(s) to test
 		YetiModule mod=null;
 		if (modulesToTest.length==1)
 			mod=YetiModule.allModules.get(modulesToTest[0]);
 		else
 			mod=YetiModule.allModules.get(modulesToTest[0]);
 			
 		// creating the engine object
 		engine= new YetiEngine(strategy,testManager);
 		
 		// Creating the log processor
 		YetiLog.proc=pl.getLogProcessor();
 		
 		// depending of the options launch the testing
 		if (isNTests)
 			// if iit is the number of states
 			engine.testModuleForNumberOfTests(mod, nTests);
 		else if (isTimeout) 
 			// if it is according to a timeout
 			engine.testModuleForNSeconds(mod, timeOutSec);
 		else {
 			printHelp();
 			return;
 		}
 		// presents the logs
 		for (String log: YetiLog.proc.processLogs()) {
 			System.out.println(log);
 		}
 		
 		
 		
 	}
 	
 	/**
 	 * This is a simple help printing utility function.
 	 */
 	public static void printHelp() {
		System.out.println("Yeti Usage:\n java yeti [-java|-Java] [[-time=Xs|-time=Xmn]|[-nTests=X]][-testModules=M1:M2:...:Mn][-help|-h]");
 		System.out.println("\t-java, -Java : for calling it on Java.");
 		System.out.println("\t-time=Xs, -time=Xmn : for calling Yeti for a given amount of time (X can be minutes or seconds, e.g. 2mn | 3s ).");
 		System.out.println("\t-nTests=X : for calling Yeti to attempt X method calls.");
 		System.out.println("\t-testModules=M1:M2:...:Mn : for testing one or several modules.");
 		System.out.println("\t-help, -h: prints the help out.");
 	}
 
 }
