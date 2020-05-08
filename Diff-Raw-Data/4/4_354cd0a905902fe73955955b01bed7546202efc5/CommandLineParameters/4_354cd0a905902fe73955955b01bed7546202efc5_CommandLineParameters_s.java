 package org.app;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 public class CommandLineParameters {
 
 	private static final Logger LOG = Logger.getLogger(CommandLineParameters.class);
 	
 	private static final Level DEFAULT_LOG_LEVEL = Level.TRACE;
 
 	@Option(name = "-l")
 	String loggingLevel;
 	
 	@Option(name = "-i")
 	String inet; //interface
 	
 	public void load(String[] args) {
 
 		CmdLineParser parser = new CmdLineParser(this);
 
 		// if you have a wider console, you could increase the value;
 		// here 80 is also the default
 		parser.setUsageWidth(80);
 
 		try {
 			// parse the arguments.
 			parser.parseArgument(args);
 
 		} catch (CmdLineException e) {
 			LOG.error("An error has ocurred while parsing command line parameters. Please check them, it must be '-f filename' and/or '-l (TRACE|ERROR|OFF)'");
 			return;
 		}
 
 	}
 
 	public Level getLoggingLevel() {
 		if (loggingLevel == null || loggingLevel.equals("")
 				|| loggingLevel.equalsIgnoreCase("error")) {
			LOG.error("You have indicated a wrong type of level logging. It must be OFF, TRACE or ERROR. ERROR it's default.");
 			return DEFAULT_LOG_LEVEL;
 		}
 		if (loggingLevel.equalsIgnoreCase("trace")) {
 			return Level.TRACE;
 		}
 		if (loggingLevel.equalsIgnoreCase("error")) {
 			return Level.ERROR;
 		}
 		if (loggingLevel.equalsIgnoreCase("off")) {
 			return Level.OFF;
 		}
		LOG.error("You have indicated a wrong type of level logging. It must be OFF, TRACE or ERROR. ERROR it's default.");
 		return DEFAULT_LOG_LEVEL;
 	}
 	
 	public String getInterface(){
 		if(inet  == null || inet.equals("")){
 			return null;
 		}
 		return inet;
 	}
 }
