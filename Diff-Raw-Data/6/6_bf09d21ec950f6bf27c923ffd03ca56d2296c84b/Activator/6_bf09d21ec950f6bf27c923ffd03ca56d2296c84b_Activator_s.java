 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.bmi;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.bmi.pub.IModlet;
 import com.buglabs.bug.bmi.pub.IModletFactory;
 import com.buglabs.bug.bmi.sysfs.BMIDevice;
 import com.buglabs.bug.bmi.sysfs.BMIDeviceHelper;
 import com.buglabs.util.osgi.FilterUtil;
 import com.buglabs.util.osgi.LogServiceUtil;
 import com.buglabs.util.shell.pub.ShellSession;
 
 /**
  * Activator for BMI Bundle. BMI bundle handles event notification and IModlet
  * initialization upon hardware change events. This bundle is specific to BUG
  * hardware that has BMI ports.
  * 
  * @author kgilmer
  * 
  */
 public class Activator implements BundleActivator, ServiceListener {
 	private static final String DEFAULT_PIPE_FILENAME = "/tmp/eventpipe";
 
 	/**
 	 * If present as a property, used as the name of the pipe used to
 	 * communicate udev events.
 	 */
 	public static final String PIPE_FILENAME_KEY = "com.buglabs.pipename";
 
 	/**
 	 * Thread to read input from fs pipe.
 	 */
 	private PipeReader pipeReader;
 
 	/**
 	 * Filename of fs pipe.
 	 */
 	private String pipeFilename;
 
 	private LogService logService;
 
 	/**
 	 * Map of all IModletFactories.
 	 */
 	private Map<String, List<IModletFactory>> modletFactories;
 
 	/**
 	 * Map of all active (modules attached and running) IModlets.
 	 */
 	private Map<String, List<IModlet>> activeModlets;
 
 	private static BundleContext context;
 
 	private BMIModuleEventHandler eventHandler;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 
 		modletFactories = new Hashtable<String, List<IModletFactory>>();
 		activeModlets = new Hashtable<String, List<IModlet>>();
 		logService = LogServiceUtil.getLogService(context);
 
 		context.addServiceListener(this, FilterUtil.generateServiceFilter(IModletFactory.class.getName()));
 		registerExistingServices();
 
 		pipeFilename = context.getProperty(PIPE_FILENAME_KEY);
 
 		if (pipeFilename == null || pipeFilename.length() == 0)
 			pipeFilename = DEFAULT_PIPE_FILENAME;
 
 		createPipe(pipeFilename);
 		eventHandler = new BMIModuleEventHandler(context, logService, modletFactories, activeModlets);
 		pipeReader = new PipeReader(pipeFilename, eventHandler, logService);
 
 		coldPlug();
 
 		pipeReader.start();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		context.removeServiceListener(this);
 		stopModlets(activeModlets.values());
 		if (pipeReader != null) {
 			pipeReader.shutdown();
 			pipeReader.interrupt();
 			logService.log(LogService.LOG_DEBUG, "Deleting pipe " + pipeFilename);
 			destroyPipe(new File(pipeFilename));
 		}
 		modletFactories.clear();
 	}
 
 	/**
 	 * Discover attached BMI devices and send INSERT events into system.
 	 * 
 	 * @throws IOException
 	 *             on File I/O error
 	 */
 	private void coldPlug() throws IOException {
 		List<BMIDevice> devices = Arrays.asList(BMIDeviceHelper.getDevices(context));
 
 		if (devices != null) {
 			logService.log(LogService.LOG_DEBUG, "(coldplug) Initializing existing modules.");
 			for (BMIDevice bmiMessage : devices) {
				logService.log(LogService.LOG_DEBUG, "Registering existing module with message: " + bmiMessage.toString());
				eventHandler.handleEvent(new BMIModuleEvent(bmiMessage));
 			}
 		} else {
 			logService.log(LogService.LOG_DEBUG, "(coldplug) Not registering existing modules, none found.");
 		}
 	}
 
 	/**
 	 * @param factory
 	 *            factory to use to create modlet.
 	 * @throws Exception
 	 *             on bundle or system error
 	 */
 	private void createModlets(IModletFactory factory) throws Exception {
 		IModlet modlet = factory.createModlet(context, 0, null);
 		modlet.setup();
 		modlet.start();
 	}
 
 	/**
 	 * Create a pipe file by executing an external process. Requires that the
 	 * host system has the "mkfifo" program.
 	 * 
 	 * @param filename
 	 *            absolute path of pipe
 	 * @throws IOException
 	 *             on File I/O error
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
 
 		ShellSession shell = new ShellSession(f.getParentFile());
 		shell.execute(cmd);
 
 		logService.log(LogService.LOG_INFO, "Created pipe " + pipeFilename);
 	}
 
 	/**
 	 * Deletes a file.
 	 * 
 	 * @param file
 	 *            to delete
 	 * @throws IOException
 	 *             on File I/O error
 	 */
 	private void destroyPipe(File file) throws IOException {
 		if (!file.delete()) {
 			throw new IOException("Unable to delete file " + file.getAbsolutePath());
 		}
 		logService.log(LogService.LOG_DEBUG, "Deleted " + file.getAbsolutePath());
 	}
 
 	/**
 	 * @return active IModlets.
 	 */
 	protected Map<String, List<IModlet>> getActiveModlets() {
 		return activeModlets;
 	}
 
 	/**
 	 * @return all IModletFactories
 	 */
 	protected Map<String, List<IModletFactory>> getModletFactories() {
 		return modletFactories;
 	}
 
 	/**
 	 * @param element
 	 *            input
 	 * @return true if string is null or of 0 length.
 	 */
 	private boolean isEmpty(String element) {
 		return element == null || element.length() == 0;
 	}
 
 	/**
 	 * @throws InvalidSyntaxException
 	 *             on Filter syntax error
 	 */
 	private void registerExistingServices() throws InvalidSyntaxException {
 		ServiceReference[] sr = context.getServiceReferences((String) null, FilterUtil.generateServiceFilter(IModletFactory.class.getName()));
 
 		if (sr != null) {
 			for (int i = 0; i < sr.length; ++i) {
 				registerService(sr[i], ServiceEvent.REGISTERED);
 			}
 		}
 	}
 
 	private void registerService(ServiceReference sr, int eventType) {
 		IModletFactory factory = (IModletFactory) context.getService(sr);
 
 		validateFactory(factory);
 
 		switch (eventType) {
 		case ServiceEvent.REGISTERED:
 			if (!modletFactories.containsKey(factory.getModuleId())) {
 				modletFactories.put(factory.getModuleId(), new ArrayList<IModletFactory>());
 			} else {
 				logService.log(LogService.LOG_WARNING, "IModletFactory " + factory.getName() + " is already registered, ignoring registration.");
 				return;
 			}
 
 			List<IModletFactory> ml = modletFactories.get(factory.getModuleId());
 
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
 				modletFactories.get(factory.getModuleId()).remove(factory);
 
 				logService.log(LogService.LOG_DEBUG, "Removed modlet factory " + factory.getName() + " to map.");
 			} else {
 				logService.log(LogService.LOG_ERROR, "Unable unregister non-existant module " + factory.getModuleId());
 			}
 			break;
 		default:
 			logService.log(LogService.LOG_ERROR, "Unhandled BMI event type " + eventType);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
 	 * ServiceEvent)
 	 */
 	public void serviceChanged(ServiceEvent event) {
 		registerService(event.getServiceReference(), event.getType());
 	}
 
 	/**
 	 * Stop all active modlets.
 	 * 
 	 * @param modlets
 	 *            stop all modlets
 	 */
 	private void stopModlets(Collection<List<IModlet>> modlets) {
 		for (List<IModlet> ml : modlets)
 			for (IModlet modlet : ml)
 				try {
 					modlet.stop();
 				} catch (Exception e) {
 					logService.log(LogService.LOG_ERROR, "Error occured while stopping " + modlet.getModuleId() + ": " + e.getMessage());
 				}
 	}
 
 	/**
 	 * @param factory
 	 *            factory to validate
 	 */
 	private void validateFactory(IModletFactory factory) {
 		if (isEmpty(factory.getModuleId())) {
 			throw new RuntimeException("IModletFactory has empty Module ID.");
 		}
 
 		if (isEmpty(factory.getName())) {
 			throw new RuntimeException("IModletFactory has empty Name.");
 		}
 	}
 
 	public static BundleContext getContext() {	
 		return context;
 	}
 }
