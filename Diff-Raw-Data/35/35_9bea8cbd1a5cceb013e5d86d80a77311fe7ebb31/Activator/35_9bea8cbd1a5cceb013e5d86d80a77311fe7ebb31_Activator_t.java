 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.tools.sysmon.impl;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import jsysmon.JSysmon;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.service.monitor.Monitorable;
 
 public class Activator implements BundleActivator {
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * This function is called by the osgi-framework to start the bundle.
 	 * @see BundleActivator#start(BundleContext) 
 	 */	
 	public void start(BundleContext bc) throws Exception {
 		CPUMonitoring cpuMonListener = new CPUMonitoring();
 		
 		// registering CPU usage monitoring
 		try {
			// starting jsysmon daemon
			JSysmon.setUpdateDelay(60000);
 			JSysmon.startMonitoring();
 			JSysmon.addCPUMonitoringListener(cpuMonListener);
 
 			// registering listener to the framework
 			Dictionary<String, Object> properties = new Hashtable<String, Object>();
 			properties.put(Constants.SERVICE_PID, CPUMonitoring.PID);
 			bc.registerService(Monitorable.class.getName(), cpuMonListener, properties);
 		} catch (UnsatisfiedLinkError e) {
 			this.logger.error("Unable to load jsysmon native-library.",e);
 		}
 		
 		// register disk-space monitoring
 		Dictionary<String, Object> properties = new Hashtable<String, Object>();
 		properties.put(Constants.SERVICE_PID, DiskspaceMonitoring.PID);
 		bc.registerService(Monitorable.class.getName(), new DiskspaceMonitoring(), properties);
 	}
 
 	/**
 	 * This function is called by the osgi-framework to stop the bundle.
 	 * @see BundleActivator#stop(BundleContext)
 	 */		
 	public void stop(BundleContext context) throws Exception {
 		// stopping jsysmon daemon
 		JSysmon.stopMonitoring();
 	}
 }
