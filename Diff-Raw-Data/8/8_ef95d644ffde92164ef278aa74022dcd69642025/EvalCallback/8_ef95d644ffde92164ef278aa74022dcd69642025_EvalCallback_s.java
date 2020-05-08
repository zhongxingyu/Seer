 package prm4jeval;
 
 import java.io.File;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.dacapo.harness.Callback;
 import org.dacapo.harness.CommandLineArgs;
 import org.dacapo.parser.Config;
 
 public class EvalCallback extends Callback {
 
     private SteadyStateInvocation ssi;
     private final Logger logger;
 
     private final String benchmark;
     private final String paramProperty;
     private final int invocation;
 
     private int iterationCount = 0;
 
     private long startTime;
 
     public EvalCallback(CommandLineArgs args) {
 	super(args);
 	logger = getFileLogger(getMandatorySystemProperty("prm4jeval.outputfile"));
 	benchmark = getMandatorySystemProperty("prm4jeval.benchmark");
	paramProperty = getMandatorySystemProperty("prm4jeval.paramProperty");
 	invocation = Integer.parseInt(getMandatorySystemProperty("prm4jeval.invocation"));
     }
 
     /**
      * A simple file logger which outputs only the message.
      * 
      * @param fileName
      *            path to the output file
      * @return the logger
      */
     private static Logger getFileLogger(String fileName) {
 	new File(fileName).getParentFile().mkdirs();
 	final Logger logger = Logger.getLogger(fileName);
 	try {
 	    logger.setUseParentHandlers(false);
 	    Handler handler = new FileHandler(fileName, true);
 	    handler.setFormatter(new Formatter() {
 		@Override
 		public String format(LogRecord record) {
 		    return record.getMessage() + "\n";
 		}
 	    });
 	    logger.addHandler(handler);
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
 	return logger;
     }
 
     @Override
     public void complete(String benchmark, boolean valid) {
 	System.out.println("[DaCapo] Completed iteration nr.: " + iterationCount);
 	super.complete(benchmark, valid);
     }
 
     @Override
     public void init(Config arg0) {
 	System.out.println("[DaCapo] Initializing...");
 	ssi = new SteadyStateInvocation();
 	super.init(arg0);
     }
 
     @Override
     public void start(String benchmark) {
 	startTime = System.currentTimeMillis();
 	System.out.println("[DaCapo] Starting iteration " + ++iterationCount);
 	super.start(benchmark);
     }
 
     @Override
     public void stop() {
 	long elapsedTime = System.currentTimeMillis() - startTime;
 	ssi.addMeasurement(elapsedTime);
 	logger.log(Level.INFO, String.format("%02d %s %s iter %02d %d %f", invocation, benchmark, paramProperty,
 		iterationCount, elapsedTime, ssi.getCoefficientOfStandardDeviation()));
 	System.out.println("[DaCapo] Stopping... time: " + elapsedTime);
 	System.gc();
 	System.gc();
 	super.stop();
     }
 
     @Override
     public boolean runAgain() {
 	if (ssi.isThresholdReached()) {
 	    logger.log(Level.INFO, String.format("%02d %s %s mean %02d %f %f", invocation, benchmark, paramProperty,
 		    iterationCount, ssi.getMean(), ssi.getCoefficientOfStandardDeviation()));
 	    System.exit(1);
 	}
 	return true;
     }
 
     static String getMandatorySystemProperty(String key) {
 	final String value = System.getProperty(key);
 	if (value == null) {
 	    throw new RuntimeException("System property [" + key + "] is mandatory but not defined!");
 	}
 	return value;
     }
 
 }
