 package info.slony.clustertest.testcoordinator.slony;
 
 import info.slony.clustertest.testcoordinator.Coordinator;
 import info.slony.clustertest.testcoordinator.Event;
 import info.slony.clustertest.testcoordinator.EventSource;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 
 /**
  * 
  * The ShellExecScript class is an an abstract base class for classes that
  * handle calling sub process (via exec) to perform actions as part of a test.
  * 
  * 
  *
  */
 public abstract class ShellExecScript implements EventSource {
 
 	private static Logger log = Logger.getLogger(ShellExecScript.class);
 
 	/**
 	 * The Process object for the child process launched.
 	 */
 	protected Process execProcess;
 	
 	/**
 	 * A class that monitors the output (stdout) of the process.
 	 */
 	protected OutputMonitor outputMonitor;
 	
 	/**
 	 * A class that monitors the error (stderr) stream of the process.
 	 */
 	protected OutputMonitor errorMonitor;
 	
 	/**
 	 * The coordinator instance.  
 	 */
 	protected Coordinator coordinator;
 	
 	/**
 	 * A flag used to indicate that we are in the middle of shutting down
 	 * this process.  This is mostly used to know to expect errors 
 	 * on the output monitors after the process has been killed.
 	 */
 	private volatile boolean stopping=false;
 	
 	private String label;
 	
 	private boolean isFinished=false;
 	
 	/**
 	 * An interface that describes a function that can process a line of
 	 * output from the subprocess.
 	 * 
 	 *
 	 */
 	protected interface OutputLineProcessor {
 		
 		public void processLine(String line);
 	}
 	
 	/**
 	 * A data structure used to return information about the command line 
 	 *  and the environment for the shell command.
 	 * 
 	 *
 	 */
 	protected class CommandOptions {
 		public String[] commandOptions;
 		public String[] environment;
 	}
 	
 
 	/**
 	 * 
 	 * A class that monitors the output stream of the process and dispatches any
 	 * events that are worth noting.
 	 * 
 	 */
 	protected class OutputMonitor implements Runnable {
 
 		private InputStream stream;
 		private Level outputPriority;
 		private Coordinator coordinator;
 		private OutputLineProcessor lineProcessor;
 		private boolean isFinished=false;		
 
 		/**
 		 * Constructor for the OutputMonitor class.
 		 * 
 		 * @param stream
 		 *            The stream that should be monitored.
 		 * @param outputPriority
 		 *            The logging priority of output consumed from the stream.
 		 *            Any output read from the stream will be passed to the
 		 *            logger at this priority. The idea being that an instance
 		 *            monitoring stderr probably needs to log output at a higher
 		 *            priority than one monitoring stdout.
 		 * @param coordinator
 		 *            The coordinator instance.
 		 * @param parent
 		 *            The ShellExecSCript instance that launched the  process
 		 *            being monitored.
 		 *  @param lineProcessor An object that will processes each line of output
 		 *          that this class monitors.
 		 */
 		public OutputMonitor(InputStream stream, Level outputPriority,
 				Coordinator coordinator, ShellExecScript  process,
 				OutputLineProcessor lineProcessor) {
 			this.stream = stream;
 			this.outputPriority = outputPriority;
 			this.coordinator = coordinator;
 			this.lineProcessor = lineProcessor;
 			
 		}
 
 		public void run() {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					stream));
 			String line;
 			try {
 				
 				/**
 				 * Loop through the output.
 				 */
				NDC.push(label + ":" + execProcess.toString() );
 				while ((line = reader.readLine()) != null ) {
 					// Consume a line.
 					// @todo Determine if the output is interesting and dispatch
 					// an event
 					getOutputLogger().log(outputPriority, line);
 					if(lineProcessor != null) {
 						lineProcessor.processLine(line);
 					}
 					
 				}
 			} catch (IOException e) {
 				if(!stopping)
 					log.error("error consuming output stream", e);
 			} finally {
 				synchronized(this) {
 					isFinished=true;
 					notifyAll();
 				}
 				NDC.pop();
 				
 			}
 
 		}
 		public void waitFor() {
 			synchronized(this) {
 				while(!isFinished) {				
 					try {
 						this.wait();
 					}
 					catch(InterruptedException e) {
 						
 					}
 				}//while			
 			}//synchronized
 		}//waitFor
 		
 	};
 
 	/**
 	 * Stop execution of the slonik script.
 	 * 
 	 * This method is intended to allow callers to abort the process
 	 * before it completes.
 	 */
 	public void stop() {
 		if(execProcess != null) {
 			stopping=true;
 			log.info("killing process " + this.label);
 			execProcess.destroy();
 		}
 		try {
 			if(outputMonitor!= null && outputMonitor.stream != null)
 				outputMonitor.stream.close();
 		} catch (IOException e) {
 			log.debug("exception while shutting down the output monitor", e);
 		}
 		try {
 			if(errorMonitor!=null && errorMonitor.stream != null)
 				errorMonitor.stream.close();
 		} catch (IOException e) {
 			log.debug("exception while shutting down the error monitor", e);
 		}
 
 	}
 
 	/**
 	 * 
 	 * @return The integer return code that the process returned when it
 	 *         finished.
 	 * @throws IllegalStateException
 	 *             if this method is called before the process finishes.
 	 */
 	public int getReturnCode() throws IllegalThreadStateException {
 		if (execProcess != null) {
 			return execProcess.exitValue();
 		}
 		throw new IllegalThreadStateException("process has not yet started");
 
 	}
 
 	
 	/**
 	 * This method will start up the shell program (via exec) and 
 	 * launch OutputMonitor processes for monitoring stdout and stderr. 
 	 */
 	protected void runSubProcess() {
 		try {
 
 			CommandOptions options = getExecutablePath();
 			
 			
 			
 			StringBuilder pathBuilder = new StringBuilder();
 			for(int idx=0; idx < options.commandOptions.length; idx++) {
 				pathBuilder.append(options.commandOptions[idx]);
 				pathBuilder.append(" ");
 			}
 			String path = pathBuilder.toString();
 			log.info("Launching sub process:" + path);
 			
 			execProcess = Runtime.getRuntime().exec(options.commandOptions,options.environment );
 			outputMonitor = new OutputMonitor(execProcess.getInputStream(),
 					Level.DEBUG, coordinator, this,getStdoutProcessor());
 			errorMonitor = new OutputMonitor(execProcess.getErrorStream(),
 					Level.ERROR, coordinator, this,getStderrProcessor());			
 			Thread t1 = new Thread(outputMonitor, label + " stdout");
 			t1.start();
 			Thread t2 = new Thread(errorMonitor,label + "stderr");
 			t2.start();
 			Writer scriptWriter = new OutputStreamWriter(execProcess
 					.getOutputStream());
 
 			Thread t3 = new Thread(new Runnable() {
 				public void run() {
 					try {
						NDC.push(label + ":" + execProcess.toString());
 						execProcess.waitFor();
 						log.info("exit with return code:" + execProcess.exitValue());
 						try {
 							
 							if(execProcess.getInputStream().available()==0) {								
 								execProcess.getInputStream().close();
 							}
 						}
 						catch(IOException e) {
 							log.info("io exception after process finished:",e);
 						}
 						outputMonitor.waitFor();
 						
 						try {
 							if(execProcess.getErrorStream().available()==0) {
 								execProcess.getErrorStream().close();
 							}
 						}
 						catch(IOException e) {
 							log.info("io exception after process finished:",e);
 						}
 						errorMonitor.waitFor();
 						onProcessFinished();
 					} catch (InterruptedException e) {
 						log.error("error waiting for program:", e);
 					} finally {
 						Event event = new Event();
 						event.source = ShellExecScript.this;
 						/**
 						 * 
 						 */
 						event.eventName = Coordinator.EVENT_FINISHED;
 						coordinator.queueEvent(event);
 						synchronized(ShellExecScript.this) {
 							isFinished=true;
 						}
 						NDC.pop();
 
 					}
 				}
 			},label + "exit");
 			t3.start();
 			writeInput(scriptWriter);
 
 		} catch (IOException e) {
 			log.error("error communicating with program", e);
 			coordinator.abortTest(e.getMessage());
 		}
 	}
 
 	/**
 	 * Create a ShellExecScript
 	 * @param coordinator  The coordinator instance - used for interacting with the event system.
 	 * @param labelname A user defined label for this script that will be used in logging output.
 	 */
 	public ShellExecScript(Coordinator coordinator, String label) {
 		this.coordinator = coordinator;
 		this.label = label;
 	}
 	
 	protected abstract void writeInput(Writer w) throws IOException;
 
 	protected abstract CommandOptions getExecutablePath() throws IOException;
 		
 	
 	/**
 	 * Returns a new instance of an OutputLineProcessor class that will 
 	 * process the output on stderr (standard error).
 	 * @return The default implementation returns null (no processor) but classes
 	 *         can override this to provide a more useful result.
 	 */
 	protected  OutputLineProcessor getStderrProcessor() {
 		return null;
 	}
 	
 	/**
 	 * Returns a new instance of an OutputLineProcessor class that will 
 	 * process the output on stderr (standard error).
 	 * @return The default implementation returns null (no processor) but classes
 	 *         can override this to provide a more useful result.
 	 */
 	protected OutputLineProcessor getStdoutProcessor() {
 		return null;
 	}
 	
 	
 	/**
 	 * Returns a logger sutiable for logging the output of the process.
 	 * This allows child classes to provide a different logger.
 	 * @return
 	 */
 	protected Logger getOutputLogger() {
 		return log;
 	}
 	
 	public synchronized boolean isFinished() {
 		return isFinished;
 	}
 	
 	protected void onProcessFinished() {
 		//Do nothing.
 	}
 
 }
