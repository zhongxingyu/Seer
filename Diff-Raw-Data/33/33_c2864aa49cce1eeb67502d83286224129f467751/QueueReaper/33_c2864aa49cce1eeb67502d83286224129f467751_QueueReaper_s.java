 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.narayana.blacktie.administration;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
 import org.jboss.narayana.blacktie.jatmibroker.core.conf.XMLParser;
 import org.jboss.narayana.blacktie.administration.core.AdministrationProxy;
 
 public class QueueReaper implements Runnable {
 	/** logger */
 	private static final Logger log = LogManager.getLogger(QueueReaper.class);
 
 	/** Interval at which to run */
 	private long interval = -1; // TODO make this configurable
 
 	/** Thread to run */
 	private Thread thread;
 
 	/** Whether the thread is executing */
 	private boolean run;
 
 	private Properties prop;
 
 	private AdministrationProxy administrationProxy;
 
 	public QueueReaper(AdministrationProxy administrationProxy)
 			throws ConfigurationException {
 		this.thread = new Thread(this);
 		this.thread.setDaemon(true);
 		this.thread.setPriority(Thread.MIN_PRIORITY);
 		this.administrationProxy = administrationProxy;
 
 		prop = new Properties();
 		XMLParser.loadProperties("btconfig.xsd", "btconfig.xml", prop);
 
 		this.interval = Integer.parseInt(prop.getProperty(
 				"QueueReaperInterval", "30")) * 1000;
 	}
 
 	public void startThread() {
 		if (!this.run) {
 			this.run = true;
 			if (this.thread.isInterrupted()) {
 				Thread.interrupted();
 			}
 			this.thread.start();
 		} else {
 			log.debug("Thread already running");
 		}
 	}
 
 	public void stopThread() {
 		if (!this.thread.isInterrupted()) {
 			this.thread.interrupt();
 		}
 		this.run = false;
 		try {
 			this.thread.join();
 		} catch (InterruptedException e) {
 			log.warn("Could not join with reaper: " + e.getMessage());
 		}
 	}
 
 	public boolean isRunning() {
 		return this.run;
 	}
 
 	public void run() {
 		while (this.run) {
 			try {
 				ObjectName objName = new ObjectName(
 						"org.hornetq:module=JMS,type=Server");
 				String[] dests = (String[]) administrationProxy
 						.getBeanServerConnection().getAttribute(objName,
 								"QueueNames");
 				for (int i = 0; i < dests.length; i++) {
 					String serviceName = dests[i];
 					serviceName = serviceName.substring(serviceName
 							.indexOf('_') + 1);
 					String server = (String) prop.get("blacktie." + serviceName
 							+ ".server");
 					log.trace("Checking for: "
 							+ serviceName
 							+ " "
 							+ prop.get("blacktie." + serviceName
 									+ ".externally-managed-destination"));
					if (((server != null && !(Boolean) prop.get("blacktie."
							+ serviceName + ".externally-managed-destination")) || serviceName
							.contains(".")) && consumerCount(serviceName) == 0) {
 						log.warn("undeploy service pending for "
 								+ serviceName
 								+ " as consumer count is 0, will check again in 30 seconds");
 						long queueReapCheck = System.currentTimeMillis();
 						Thread.sleep(this.interval);
 
 						// double check consumer is 0
 						if (isOlderThanReapCheck(serviceName, queueReapCheck)
 								&& consumerCount(serviceName) == 0) {
 							undeployQueue(serviceName);
 							log.warn("undeploy service " + serviceName
 									+ " for consumer is 0");
 						} else {
 							log.info("Undeploy not required for: "
 									+ serviceName + " at: " + server);
 						}
 					} else {
 						log.debug("Undeploy not required for: " + serviceName
 								+ " at: " + server);
 					}
 				}
 				log.debug("Sleeping for " + this.interval + " ms");
 				Thread.sleep(this.interval);
 			} catch (InterruptedException e) {
 				log.debug("Sleeping interrupted");
 				this.run = false;
 			} catch (Exception e) {
 				log.error("run ping thread failed with (will wait for: "
 						+ interval + " seconds): " + e, e);
 				try {
 					Thread.sleep(this.interval);
 				} catch (InterruptedException e2) {
 					log.debug("Sleeping interrupted");
 					this.run = false;
 				}
 			}
 		}
 	}
 
 	int consumerCount(String serviceName) throws MalformedObjectNameException,
 			NullPointerException, AttributeNotFoundException,
 			InstanceNotFoundException, ReflectionException, IOException,
 			MBeanException {
 		log.trace(serviceName);
 		boolean conversational = false;
 		if (!serviceName.startsWith(".")) {
 			conversational = (Boolean) prop.get("blacktie." + serviceName
 					+ ".conversational");
 		}
 		String prefix = null;
 		if (conversational) {
 			prefix = "BTC_";
 		} else {
 			prefix = "BTR_";
 		}
 
 		ObjectName objName = new ObjectName("org.hornetq:module=JMS,name=\""
 				+ prefix + serviceName + "\",type=Queue");
 		try {
 			Integer count = (Integer) administrationProxy
 					.getBeanServerConnection().getAttribute(objName,
 							"ConsumerCount");
 			return count.intValue();
 		} catch (javax.management.InstanceNotFoundException e) {
 			log.debug("Instance not found: " + objName);
 			return -1;
 		}
 	}
 
 	private boolean isOlderThanReapCheck(String serviceName, long queueReapCheck) {
 		// TODO THIS WILL NOT CLUSTER AS IT ASSUMES THE QUEUE WAS CREATED BY
 		// THIS SERVER
 		synchronized (BlacktieStompAdministrationService.QUEUE_CREATION_TIMES) {
 			boolean toReturn = true;
 			Long creationTime = BlacktieStompAdministrationService.QUEUE_CREATION_TIMES
 					.get(serviceName);
 			if (creationTime != null) {
 				toReturn = creationTime < queueReapCheck;
 				if (!toReturn) {
 					log.warn("New queue will be ignored: " + serviceName);
 				}
 			}
 			return toReturn;
 		}
 	}
 
 	int undeployQueue(String serviceName) {
 		int result = 0;
 
 		try {
 			log.trace(serviceName);
 			boolean conversational = false;
 			if (!serviceName.startsWith(".")) {
 				conversational = (Boolean) prop.get("blacktie." + serviceName
 						+ ".conversational");
 			}
 			String prefix = null;
 			if (conversational) {
 				prefix = "BTC_";
 			} else {
 				prefix = "BTR_";
 			}
 
 			ObjectName objName = new ObjectName(
 					"org.hornetq:module=JMS,type=Server");
 			administrationProxy.getBeanServerConnection().invoke(objName,
 					"destroyQueue", new Object[] { prefix + serviceName },
 					new String[] { "java.lang.String" });
 			result = 1;
 		} catch (Throwable t) {
 			log.error("Could not undeploy queue of " + serviceName, t);
 		}
 
 		return result;
 	}
 }
