 
 package axirassa.overlord;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import axirassa.overlord.exceptions.NoOverlordConfigurationException;
 import axirassa.overlord.exceptions.OverlordException;
 import axirassa.overlord.os.AbstractOverlordSystemSupport;
 import axirassa.overlord.os.OverlordSystemSupport;
 
 /**
  * A process starting/monitoring daemon and framework.
  * 
  * AxOverlord hooks into a HornetQ message passing framework.
  * 
  * Note that java processes started by Overlord will not be terminated by
  * default unless {@link #addShutdownHooks()} is called.
  * 
  * @author wiktor
  * 
  */
 public class Overlord {
 	private static final String CONFIGURATION_FILE = "axoverlord.cfg.xml";
 
 
 	public static void main(String[] parameters) throws OverlordException, IOException {
 		System.out.println("Ax|Overlord Starting");
 		Overlord overlord = new Overlord();
 		overlord.addShutdownHooks();
		overlord.execute(new String[] { "master" });
 	}
 
 
 	//
 	// Class Instances
 	//
 	private OverlordConfiguration configuration;
 	private final ArrayList<ExecutionInstance> instances = new ArrayList<ExecutionInstance>();
 	private final NativeLibraryProvider libprovider = new NativeLibraryProvider();
 
 
 	public void execute(String[] parameters) throws OverlordException, IOException {
 		OverlordSystemSupport systemsupport = AbstractOverlordSystemSupport.getSystemSupport();
 
 		URL configfile = ClassLoader.getSystemResource(CONFIGURATION_FILE);
 		if (configfile == null)
 			throw new NoOverlordConfigurationException(CONFIGURATION_FILE);
 
 		System.out.println("Overlord configuration found at: " + configfile.getPath());
 
 		InputStream configstream = ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
 
 		configuration = new OverlordConfiguration(this);
 		configuration.setJavaExecutable(systemsupport.getJavaExecutable());
 
 		XMLConfigurationParser configparser = new XMLConfigurationParser(configfile, configstream, configuration);
 		configparser.parse();
 
 		for (String groupname : parameters) {
 			ExecutionGroup group = configuration.getExecutionGroup(groupname);
 
 			if (group != null)
 				group.execute();
 		}
 	}
 
 
 	public Collection<ExecutionInstance> getExecutionInstances() {
 		return instances;
 	}
 
 
 	public NativeLibraryProvider getNativeLibraryProvider() {
 		return libprovider;
 	}
 
 
 	public void addShutdownHooks() {
 		Runtime.getRuntime().addShutdownHook(new OverlordDynamicShutdownHook(this));
 	}
 
 
 	public void addExecutionInstance(Thread thread, ExecutionMonitor monitor) {
 		instances.add(new ExecutionInstance(thread, monitor));
 	}
 
 
 	public void killInstances() {
 		for (ExecutionInstance instance : instances)
 			if (instance.getThread().isAlive()) {
 				instance.getThread().interrupt();
 				instance.getMonitor().killProcess();
 			}
 	}
 }
 
 class ExecutionInstance {
 	private final Thread thread;
 	private final ExecutionMonitor monitor;
 
 
 	public ExecutionInstance(Thread thread, ExecutionMonitor monitor) {
 		this.thread = thread;
 		this.monitor = monitor;
 	}
 
 
 	public Thread getThread() {
 		return thread;
 	}
 
 
 	public ExecutionMonitor getMonitor() {
 		return monitor;
 	}
 }
