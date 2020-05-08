 package yeti.experimenter;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import yeti.Yeti;
 
 /**
  * Class that represents... 
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date May 3, 2011
  *
  */
 public class YetiExperimentOneClass extends YetiExperiment {
 	
 	/**
 	 * A String representing the classpath
 	 */
 	String classPath;
 
 	/**
 	 * A string representing the name of the class to test
 	 */
 	String className;
 
 	/**
 	 * Simple constructor for the YetiExperiment30PicksRandom
 	 * 
 	 * @param ps the PrintStream where to store results.
 	 * @param staticOptions the options that are standard.
 	 * @param arch the archive explorer.
 	 * @param onlyPrint true if only to print the commands. 
 	 */
 	public YetiExperimentOneClass(PrintStream ps, String[] staticOptions,boolean onlyPrint, String classPath, String className) {
 		super(ps, staticOptions,onlyPrint);
 		this.className = className;
 		this.classPath = classPath;
 	}
 
 	@Override
 	public void make() {
 			for (int i = 0; i<101; i++) {
 				for (int j = 0; j<101; j++) {
 					this.makeCall(classPath, className, i,j);
 		
 				}
 			}
 	}
 
 	/**
 	 * Make an individual call to YETI.
 	 * 
 	 * @param yetiPath the yetiPath to provide.
 	 * @param modules the modules to test.
 	 */
 	public void makeCall(String yetiPath, String modules, int nullValues, int newValues) {
 		// we first initialize the arguments.
 		String []args = new String[staticOptions.length+4];
 		for (int i = 0;i<staticOptions.length;i++) {
 			args[i]=staticOptions[i];
 		}
 		args[staticOptions.length]="-yetiPath="+yetiPath;
 		args[staticOptions.length+1]="-testModules="+modules;
 		args[staticOptions.length+2]="-newInstanceInjectionProbability="+newValues;
 		args[staticOptions.length+3]="-probabilityToUseNullValue="+nullValues;
 		
 		// We then make the call
 		if (!onlyPrint) {
 			Yeti.YetiRun(args);
 			Yeti.reset();
 		} else {
 			String call = "java -ea yeti.Yeti ";
 			for (String opt: args) {
 				call=call+" "+opt;
 			}
 			System.out.println(call);
 		}
 	}
 	/**
 	 * Main method allowing to launch experiments.
 	 * 
	 * Example of use: java yeti.experimenter.YetiExperimentOneClass . java.lang.String -onlyPrint
	 * 
 	 * @param args
 	 */
 	public static void main(String []args) {
 		String classPath = args[0];
 		String className = args[1];
 		
 		boolean onlyPrint = false;
 		if (args.length>2&&args[2].equals("-onlyPrint"))
 			onlyPrint = true;
 		YetiTestArchiveExplorer expl = new YetiTestArchiveExplorer(".");
 		PrintStream ps = null;
 		String []staticOptions = {"-Java","-nTests=100000","-nologs","-approximate","-compactReport=results.csv"};
 		YetiExperimentOneClass exp = new YetiExperimentOneClass(ps, staticOptions,onlyPrint, classPath, className);
 		exp.make();
 
 	}
 
 
 }
