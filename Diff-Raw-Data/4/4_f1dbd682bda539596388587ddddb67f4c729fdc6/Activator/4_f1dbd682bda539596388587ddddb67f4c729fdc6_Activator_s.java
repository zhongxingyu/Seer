 package com.buglabs.bug.bmi;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.bmi.pub.Manager;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.pub.IModletFactory;
 import com.buglabs.util.StringUtil;
 
 public class Activator implements BundleActivator, ServiceListener {
 	private static final String DEFAULT_PIPE_FILENAME = "/tmp/eventpipe";
 
 	private PipeReader pipeReader;
 
 	private String pipeFilename;
 
 	private LogService logService;
 
 	private Map modletFactories;
 
 	private Map activeModlets;
 
 	private BundleContext context;
 
 	private static Activator ref;
 
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		Activator.ref = this;
 
 		modletFactories = new Hashtable();
 		activeModlets = new Hashtable();
 		ServiceReference sr = context.getServiceReference(LogService.class.getName());
 		if (sr != null) {
 			logService = (LogService) context.getService(sr);
 		}
 
 		context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + IModletFactory.class.getName() + ")");
 
 		registerExistingServices(context);
 
 		pipeFilename = context.getProperty("com.buglabs.pipename");
 
 		if (pipeFilename == null || pipeFilename.length() == 0) {
 			pipeFilename = DEFAULT_PIPE_FILENAME;
 		}
 
 		logService.log(LogService.LOG_INFO, "Creating pipe " + pipeFilename);
 		createPipe(pipeFilename);
 		pipeReader = new PipeReader(pipeFilename, Manager.getManager(context, logService, modletFactories, activeModlets), logService);
 
 		logService.log(LogService.LOG_INFO, "Initializing existing modules");
 
 		coldPlug();
 
 		logService.log(LogService.LOG_INFO, "Listening to event pipe. " + pipeFilename);
 
 		pipeReader.start();
 	}
 
 	private void coldPlug() throws IOException {
 		Manager m = Manager.getManager();
 
 		List modules = getSysFSModules();
 
 		if (modules != null) {
 			for (Iterator i = modules.iterator(); i.hasNext();) {
 				String bmiMessage = (String) i.next();
 				logService.log(LogService.LOG_INFO, "Registering existing module with message: " + bmiMessage);
 				m.processMessage(bmiMessage);
 			}
 		}
 	}
 
 	/**
 	 * Get a list of BMIMessage strings for existing modules based on entries in
 	 * the /sys filesystem.
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	private List getSysFSModules() throws IOException {
 		List slots = null;
 
 		for (int i = 1; i < 5; ++i) {
 			String cmd = "/bin/ls -al /sys/devices/conn-m" + i + "/driver/module";
 			String response = null;
 			
 			try {
 				response = execute(cmd);
 			} catch (IOException e) {
 				logService.log(LogService.LOG_ERROR, "Error occurred while discovering modules.", e);
 				continue;
 			}
 
 			if (response.trim().length() == 0) {
 				logService.log(LogService.LOG_DEBUG, "No module was found in slot " + i);
 				continue;
 			}
 
 			logService.log(LogService.LOG_DEBUG, "Response: " + response);
 
 			String[] elems = StringUtil.split(response, "/");
 			String driverName = elems[elems.length - 1];
 
 			// Lazily create data structure. If no modules then not needed.
 			if (slots == null) {
 				slots = new ArrayList();
 			}
 
 			String bmiMessage = new String(getModuleIdFromDriver(driverName.trim()) + " " + getModuleVersion() + " " + (i - 1) + " ADD");
 			logService.log(LogService.LOG_DEBUG, "Sending BMI ColdPlug message: " + bmiMessage);
 			
 			slots.add(bmiMessage);
 		}
 
 		return slots;
 	}
 
 	/**
 	 * This will be going away once we can retrieve the PRODUCT_ID from the sys filesystem.
 	 * @param driverName
 	 * @return
 	 */
 	private String getModuleIdFromDriver(String driverName) {
 		// TODO this is bad: hard coding this module driver to name table. This
 		// needs to be refactored such that each module supplies the
 		// association.
 		logService.log(LogService.LOG_DEBUG, "Driver Name: " + driverName);
 		if (driverName.equals("bmi_lcd_core")) {
 			return "0003";
 		}
 
 		if (driverName.equals("bmi_mdacc")) {
 			return "0002";
 		}
 
 		if (driverName.equals("bmi_camera")) {
 			return "0004";
 		}
 
 		if (driverName.equals("bmi_camera_vs6624")) {
 			return "0004";
 		}
 		
 		if (driverName.equals("bmi_camera_ov2640")) {
 			return "0005";
 		}
 
 		if (driverName.equals("bmi_gps")) {
 			return "0001";
 		}
 		
 		if (driverName.equals("bmi_vh")) {
 			return "0007";
 		}
 
 		logService.log(LogService.LOG_ERROR, "Unable to map " + driverName + " to a module ID.");
 
 		return null;
 	}
 
 	
 	/**
 	 * @return
 	 */
 	private String getModuleVersion() {
 		// TODO determine appropriate version information.
 		return "0";
 	}
 
 	private void registerExistingServices(BundleContext context2) throws InvalidSyntaxException {
 		ServiceReference sr[] = context2.getServiceReferences(null, "(" + Constants.OBJECTCLASS + "=" + IModletFactory.class.getName() + ")");
 
 		if (sr != null) {
 			for (int i = 0; i < sr.length; ++i) {
 				registerService(sr[i], ServiceEvent.REGISTERED);
 			}
 		}
 	}
 
 	public static Activator getRef() {
 		return ref;
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		stopModlets(activeModlets);
 		if (pipeReader != null) {
 			pipeReader.cancel();
 			pipeReader.interrupt();
 			logService.log(LogService.LOG_INFO, "Deleting pipe " + pipeFilename);
 			destroyPipe(new File(pipeFilename));
 		}
 		modletFactories.clear();
 	}
 
 	/**
 	 * Stop all active modlets.
 	 * 
 	 * @param activeModlets
 	 */
 	private void stopModlets(Map modlets) {
 		for (Iterator i = modlets.keySet().iterator(); i.hasNext();) {
 			String key = (String) i.next();
 
 			List modl = (List) modlets.get(key);
 
 			for (Iterator j = modl.iterator(); j.hasNext();) {
 				IModlet m = (IModlet) j.next();
 
 				try {
 					m.stop();
 				} catch (Exception e) {
 					logService.log(LogService.LOG_ERROR, "Error occured while stopping " + m.getModuleId() + ": " + e.getMessage());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Create a pipe file by executing an external process. Requires that the
 	 * host system has the "mkfifo" program.
 	 * 
 	 * @param filename
 	 * @throws IOException
 	 */
 	private void createPipe(String filename) throws IOException {
 		File f = new File(filename);
 
 		// Check to see if file exists. If so delete and recreate to confirm
 		// it's a pipe.
 		if (f.exists()) {
 			logService.log(LogService.LOG_INFO, "Pipe " + f.getAbsolutePath() + " already exists, deleting.");
 			destroyPipe(f);
 		}
 		String cmd = "/usr/bin/mkfifo " + f.getAbsolutePath();
 
 		String error = execute(cmd);
 		logService.log(LogService.LOG_INFO, "Execution Completed.  Response: " + error);
 	}
 
 	/**
 	 * Deletes a file
 	 * 
 	 * @param file
 	 * @throws IOException
 	 */
 	private void destroyPipe(File file) throws IOException {
 		if (!file.delete()) {
 			throw new IOException("Unable to delete file " + file.getAbsolutePath());
 		}
 		logService.log(LogService.LOG_INFO, "Deleted " + file.getAbsolutePath());
 	}
 
 	/**
 	 * @param cmd
 	 * @return null on success, or String of error message on failure.
 	 * @throws IOException
 	 */
 	private String execute(String cmd) throws IOException {
 		String s = null;
 		StringBuffer sb = new StringBuffer();
 		boolean hasError = false;
 		logService.log(LogService.LOG_DEBUG, "Executing: " + cmd);
 		Process p = Runtime.getRuntime().exec(cmd);
 		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 		while ((s = stdError.readLine()) != null) {
 			sb.append(s);
 			hasError = true;
 		}
 
 		if (hasError) {
 			// temp fix for ADS board. All commands return with this error.
 			if (!sb.toString().equals("Using fallback suid method")) {
 				new IOException("Failed to execute command: " + sb.toString());
 			}
 		}
 
 		BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		sb = new StringBuffer();
 		while ((s = stdOut.readLine()) != null) {
 			sb.append(s);
 			sb.append('\n');
 		}
 
 		return sb.toString();
 	}
 
 	public void serviceChanged(ServiceEvent event) {
 		ServiceReference sr = event.getServiceReference();
 
 		registerService(sr, event.getType());
 	}
 
 	private void registerService(ServiceReference sr, int eventType) {
 		IModletFactory factory = (IModletFactory) context.getService(sr);
 
 		validateFactory(factory);
 
 		switch (eventType) {
 		case ServiceEvent.REGISTERED:
 			if (!modletFactories.containsKey(factory.getModuleId())) {
 				modletFactories.put(factory.getModuleId(), new ArrayList());
 			} else {
 				logService.log(LogService.LOG_WARNING, "IModletFactory " + factory.getName() + " is already registered, ignoring registration.");
 			}
 
 			List ml = (List) modletFactories.get(factory.getModuleId());
 
 			if (!ml.contains(factory)) {
 				ml.add(factory);
 			}
 			logService.log(LogService.LOG_INFO, "Added modlet factory " + factory.getName() + " (" + factory.getModuleId() + ") to map.");
 
 			// Discovery Mode needs to know of all services a BUG contains. This
 			// causes all available modlets to be created and started.
 			if (context.getProperty("com.buglabs.bug.discoveryMode") != null && context.getProperty("com.buglabs.bug.discoveryMode").equals("true")) {
 				try {
 					createModlets(factory);
 				} catch (Exception e) {
 					logService.log(LogService.LOG_ERROR, "Unable to start modlet in discovery mode: " + e.getMessage());
 				}
 			}
 
 			break;
 		case ServiceEvent.UNREGISTERING:
 			if (modletFactories.containsKey(factory.getModuleId())) {
 				List ml2 = (List) modletFactories.get(factory.getModuleId());
 
 				if (ml2.contains(factory)) {
 					ml2.remove(factory);
 				}
 			}
 			logService.log(LogService.LOG_INFO, "Removed modlet factory " + factory.getName() + " to map.");
 			break;
 		}
 	}
 
 	private void validateFactory(IModletFactory factory) {
 		if (isEmpty(factory.getModuleId())) {
 			throw new RuntimeException("IModletFactory has empty Module ID.");
 		}
 
 		if (isEmpty(factory.getName())) {
 			throw new RuntimeException("IModletFactory has empty Name.");
 		}
 	}
 
 	private boolean isEmpty(String element) {
 		return element == null || element.length() == 0;
 	}
 
 	private void createModlets(IModletFactory factory) throws Exception {
 		IModlet modlet = factory.createModlet(context, 0);
 		modlet.setup();
 		modlet.start();
 	}
 
 	protected Map getModletFactories() {
 		return modletFactories;
 	}
 
 	protected Map getActiveModlets() {
 		return activeModlets;
 	}
 }
