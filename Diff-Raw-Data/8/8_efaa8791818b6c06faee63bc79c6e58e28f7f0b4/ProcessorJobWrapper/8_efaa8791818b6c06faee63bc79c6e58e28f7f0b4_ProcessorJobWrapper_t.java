 package com.github.uberroot.NCJBot;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 
 import com.github.uberroot.NCJBot.api.ProcessorJob;
 
 /**
  * <p>This class is used to run and manage individual ProcessorJobs. Each ProcessorJob is run by it its own unique
  * ProcessorJobWrapper. The ProcessorJobWrapper is used to load the ProcessorJob subclass, prepare its running environment,
  * and clean up after the ProcessorJob when it has finished running.</p>
  * 
  * <p>This class provides an essential interface between the node and individual jobs.</p> 
  * 
  * @author Carter Waxman
  *
  */
 public class ProcessorJobWrapper extends Thread {
 	/**
 	 * <p>The running ProcessorNode instance.</p>
 	 */
 	private ProcessorNode node;
 	
 	/**
 	 * <p>The ProcessorJob this ProcessorJobWrapper runs.</p>
 	 */
 	private ProcessorJob job;
 	
 	/**
 	 * <p>A listener that will respond to updates to the state of the ProcessorJob.</p>
 	 */
 	private final ProcessorJobWrapperStateListener listener;
 	
 	/**
 	 * <p>The Watchdog for the current node. Used to create and destroy beacons when running the ProcessorJob.</p>
 	 */
 	private final Watchdog watchdog;
 	
 	/**
 	 * <p>Whether the class and associated files should automatically be cleaned after termination. This will only be true
 	 * for remote jobs.</p>
 	 */
 	//TODO: should this be renamed to isRemote?
 	private final boolean cleanup;
 	
 	/**
 	 * <p>This is the dynamically loaded subclass. An instance of this will be created when this ProcessorJobWrapper is started.</p>
 	 */
	private final Class<? extends ProcessorJob> type;
 	
 	
 	//To be accessible by jobs
 	/**
 	 * <p>The parent / creator of this ProcessorJobWrapper's ProcessorJob. It will be null for locally created jobs.</p>
 	 */
 	private final RemoteProcessorJob sourceJob;
 	
 	/**
 	 * <p>The folder under which the ProcessorJob may create files.</p>
 	 */
 	private final File classPath;
 	
 	/**
 	 * <p>The initialization parameters for the ProcessorJob (defined by the ProcessorJob).</p>
 	 */
 	//TODO: Initialization data should be abstracted away from files into streams or byte arrays/buffers
 	private final File initData; //TODO: This could probably be removed after initialization
 	
 	/**
 	 * This interface provides callbacks for events related to the state of the ProcessorJob.
 	 * 
 	 * @author Carter Waxman
 	 *
 	 */
 	//TODO: This could be more complete. It currently provides core functionality
 	public interface ProcessorJobWrapperStateListener{
 		/**
 		 * Called when the ProcessorJob has successfully loaded and has been instantiated.
 		 * 
 		 * @param wrapper The wrapper that triggered the event.
 		 * @param job The ProcessorJob that loaded.
 		 */
 		public void jobLoaded(ProcessorJobWrapper wrapper, ProcessorJob job);
 		
 		/**
 		 * Called in the event the ProcessorJob could not be instantiated.
 		 * 
 		 * @param wrapper The wrapper that triggered the event.
 		 * @param ex The exception thrown indicating the cause of the failure.
 		 */
 		public void jobFailedToLoad(ProcessorJobWrapper wrapper, Exception ex);
 	}
 	
 	/**
 	 * <p>Instantiates a ProccessorJobWrapper with the parameters needed to start a new ProcessorJob.</p>
 	 * 
 	 * @param node The running ProcessorNode instance.
 	 * @param className The name of the ProcessorJob subclass to load.
 	 * @param classPath The path to the directory that contains the ProcessorJob subclass class file.
 	 * @param source The node that sent the ProcessorJob to this node, or null if the job is to be created locally.
 	 * @param remotePid The thread id of the ProcessorJob that sent the ProcessorJob to load to this node.
 	 * @param initData The initialization parameters for the ProcessorJob.
 	 * @param watchdog The Watchdog for this node.
 	 * @param cleanup Whether the files created should be deleted after the job has completed
 	 * @param listener A ProcessorJobWrapperListener to handle events related to startup.
 	 * 
 	 * @throws IOException
 	 * @throws ClassNotFoundException 
 	 * 
 	 */
 	//TODO: className and classPath could be combined
 	//TODO: a second constructor could be used (one without the source parameters), which could be used to infer that the job is local
 	//TODO: source and remotePID could be combined into a RemoteProcessorJob
 	public ProcessorJobWrapper(ProcessorNode node, String className, File classPath, RemoteNode source, String remotePid, File initData, Watchdog watchdog, boolean cleanup, ProcessorJobWrapperStateListener listener) throws IOException, ClassNotFoundException{
 		super();
 		this.node = node;
 		
 		URL[] u = new URL[1];
 		u[0] = classPath.toURI().toURL();
 		URLClassLoader cl = new URLClassLoader(u);
 
		type = cl.loadClass(className).asSubclass(ProcessorJob.class);
 		cl.close();
 		
 		this.setName("Processor Job (localPid = " + this.getId() + ")");
 		
 		this.watchdog = watchdog;
 		this.cleanup =  cleanup;
 		this.listener = listener;
 		
 		this.sourceJob = new RemoteProcessorJob(node, source, remotePid);
 		this.classPath = classPath;
 		this.initData = initData;
 	}
 
 
 	/**
 	 * <p>Instantiates, prepares, runs, and cleans up after the ProcessorJob.</p>
 	 */
 	@Override
 	public void run() {
		//TODO: Cleanup after failures
 		//Instantiate job
 		try {
 			//job = type.getConstructor(File.class).newInstance(initData); //TODO: By this point, the securitymanager should be in effect
 			job = type.newInstance();
 		} catch (Exception ex){
 			listener.jobFailedToLoad(this, ex);
 			return;
 		}
 		
 		//Provide initialization data to the job
 		//TODO: This should trigger it's own callbacks
 		try {
 			job.init(initData);
 		} catch (Exception ex) {
 			listener.jobFailedToLoad(this, ex);
 			return;
 		}
 		
 		//Report initialization
 		if(listener != null)
 			listener.jobLoaded(this, job);
 
 		//Start the job
 		if(cleanup)
 			//TODO: Should this be moved to the listener?
 			watchdog.registerBeacon(sourceJob.getSource()); //TODO: Registering when a local job causes a deadlock. Figure out why.
 		job.run();
 		if(cleanup){
 			File toDel[] = classPath.listFiles();
 			for(File f : toDel)
 				f.delete();
 			classPath.delete();
 		}
 		if(cleanup)
 			//TODO: Should this be moved to the listener?
 			watchdog.releaseBeacon(sourceJob.getSource());
 	}
 	
 	/**
 	 * <p>Gets the path under which the ProcessorJob should create and access files.</p>
 	 * 
 	 * @return The path under which the ProcessorJob should create and access files.
 	 */
 	public File getClassPath(){
 		return classPath;	//TODO: Enforce this
 	}
 	
 	/**
 	 * <p>Gets the job that sent the running job to this node.</p>
 	 * 
 	 * @return The job that sent the running job to this node.
 	 */
 	public RemoteProcessorJob getSourceJob(){
 		return sourceJob; //TODO: Make sure this is immutable, get rid of methods here that access this
 	}
 	
 	/**
 	 * <p>Retrieves the running ProcessorNode instance. This method will soon be deprecated in favor of one
 	 * that returns a limited-access object.</p>
 	 * 
 	 * @return the running ProcessorNode instance
 	 */
 	//TODO: Return a limited-access object
 	public ProcessorNode getNode(){
 		return node;
 	}
 }
