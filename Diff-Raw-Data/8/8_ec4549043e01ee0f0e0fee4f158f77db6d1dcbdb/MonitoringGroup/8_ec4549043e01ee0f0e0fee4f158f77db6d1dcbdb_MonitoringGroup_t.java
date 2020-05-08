 /*
  * Copyright 2009 Ben Gidley
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package uk.co.gidley.jmxmonitor.monitoring;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.co.gidley.jmxmonitor.services.InitialisationException;
 import uk.co.gidley.jmxmonitor.services.ThreadManager;
 
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A monitoring group combines a set of monitors and expressions to output data to a logger
  */
 public class MonitoringGroup implements Runnable {
 
 	private boolean stopping = false;
 	private long interval;
 	private Long lastRun;
 	private Map<String, MonitorUrlHolder> monitorUrlHolders = new HashMap<String, MonitorUrlHolder>();
 	private CompositeConfiguration monitorsConfiguration = new CompositeConfiguration();
 	private CompositeConfiguration expressionsConfiguration = new CompositeConfiguration();
 	private String name;
 
 	private boolean alive = true;
 	private static final String URL = ".url";
 	private List<String> expressions = new ArrayList<String>();
 	private ScriptEngineManager scriptEngineManager;
 
 	public MonitoringGroup() {
 		monitorsConfiguration.setThrowExceptionOnMissing(true);
		monitorsConfiguration.setDelimiterParsingDisabled(true);
 		expressionsConfiguration.setThrowExceptionOnMissing(true);
 		scriptEngineManager = new ScriptEngineManager();
 
 
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	private Logger outputLogger;
 	private static final Logger logger = LoggerFactory.getLogger(MonitoringGroup.class);
 	private static final String MONITORING_GROUP = "monitoringGroup.";
 
 	/**
 	 * Called to initialise the monitoring group. The group should use this to construct expensive objects, validate
 	 * configuration and prepare to run.
 	 *
 	 * @param monitorsConfigurationFile	The monitors file
 	 * @param expressionsConfigurationFile The expression file
 	 * @param intervalInMilliseconds	   The interval to poll in milliseconds
 	 */
 	public void initialise(String name, File monitorsConfigurationFile, File expressionsConfigurationFile,
 			long intervalInMilliseconds) throws InitialisationException {
 		try {
 			interval = intervalInMilliseconds;
 			this.name = name;
 			outputLogger = LoggerFactory.getLogger(MONITORING_GROUP + name);
 
			PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
			propertiesConfiguration.setDelimiterParsingDisabled(monitorsConfiguration.isDelimiterParsingDisabled());
			propertiesConfiguration.setFile(monitorsConfigurationFile);
			propertiesConfiguration.load();
			monitorsConfiguration.addConfiguration(propertiesConfiguration);
 			expressionsConfiguration.addConfiguration(new PropertiesConfiguration(expressionsConfigurationFile));
 
 			initialiseMonitors();
 			initialiseExpressions();
 		} catch (ConfigurationException e) {
 			logger.error("{}", e);
 			throw new InitialisationException(e);
 		} catch (MalformedObjectNameException e) {
 			logger.error("{}", e);
 			throw new InitialisationException(e);
 		} catch (MalformedURLException e) {
 			logger.error("{}", e);
 			throw new InitialisationException(e);
 		}
 	}
 
 	private void initialiseExpressions() {
 
 		Iterator<String> keys = expressionsConfiguration.getKeys();
 		while (keys.hasNext()) {
 			String key = keys.next();
 			String expression = expressionsConfiguration.getString(key);
 			expressions.add(expression);
 		}
 
 
 	}
 
 	private void initialiseMonitors() throws MalformedObjectNameException, MalformedURLException {
 		List<String> monitorUrls = monitorsConfiguration.getList(ThreadManager.PROPERTY_PREFIX + "connections");
 		for (String monitorUrlKey : monitorUrls) {
 			monitorUrlHolders.put(monitorUrlKey, new MonitorUrlHolder(monitorUrlKey));
 			initialiseMonitorUrl(monitorUrlKey, monitorsConfiguration);
 		}
 	}
 
 	/**
 	 * Initialise the monitor. If possible we start the JMX connection now. If not we create a placeholder.
 	 *
 	 * @param monitorUrlKey
 	 * @param monitorsConfiguration
 	 * @throws MalformedObjectNameException
 	 * @throws MalformedURLException
 	 */
 	private void initialiseMonitorUrl(String monitorUrlKey,
 			CompositeConfiguration monitorsConfiguration) throws MalformedObjectNameException, MalformedURLException {
 		logger.debug("Initialising Monitor Connection {}", monitorUrlKey);
 
 		String url = monitorsConfiguration.getString(ThreadManager.PROPERTY_PREFIX + monitorUrlKey + URL);
 		try {
 			// Create JMX connection
 			JMXServiceURL serviceUrl = new JMXServiceURL(url);
 			JMXConnector jmxc = JMXConnectorFactory.connect(serviceUrl, null);
 			logger.debug("JMX connection made {}", jmxc);
 			MonitoringGroup.MonitorUrlHolder monitorUrlHolder = monitorUrlHolders.get(monitorUrlKey);
 			monitorUrlHolder.setmBeanServerConnection(jmxc.getMBeanServerConnection());
 			monitorUrlHolder.getMonitors().clear();
 
 			// Parse monitors inside this
 			List<String> loadedMonitors = new ArrayList<String>();
 			Iterator<String> monitorKeys = monitorsConfiguration.getKeys(ThreadManager.PROPERTY_PREFIX + monitorUrlKey);
 			while (monitorKeys.hasNext()) {
 				String key = monitorKeys.next();
 				if (!key.endsWith(URL)) {
 					String monitorName = key.substring(
 							ThreadManager.PROPERTY_PREFIX.length() + monitorUrlKey.length() + 1,
 							key.lastIndexOf("."));
 					// Only load each on once (there will be n keys)
 					if (!loadedMonitors.contains(monitorName)) {
 						constructMonitor(monitorUrlKey, monitorsConfiguration, monitorUrlHolder, monitorName);
 						loadedMonitors.add(monitorName);
 					}
 				}
 			}
 		} catch (IOException e) {
 			if (e instanceof MalformedURLException) {
 				throw (MalformedURLException) e;
 			}
 			logger.warn("Unable to connect to {}, {}", monitorUrlKey, e);
 		}
 	}
 
 	private void constructMonitor(String monitorUrlKey, CompositeConfiguration monitorsConfiguration,
 			MonitorUrlHolder monitorUrlHolder, String monitorName) throws MalformedObjectNameException {
 
 		// Value of key is java.lang:type=Memory/HeapMemoryUsage!Heap
 		String keyPrefix = ThreadManager.PROPERTY_PREFIX + monitorUrlKey + "." + monitorName;
 		String objectName = monitorsConfiguration.getString(
 				keyPrefix + ".objectName");
 		String attribute = monitorsConfiguration.getString(
 				keyPrefix + ".attribute");
 		String discriminator = monitorsConfiguration.getString(keyPrefix + ".discriminator", null);
 		Monitor monitor;
 		if (discriminator == null) {
 			monitor = new SimpleJmxMonitor(monitorName, new ObjectName(objectName), attribute,
 					monitorUrlHolder.getmBeanServerConnection());
 		} else {
 			monitor = new DiscriminatingJmxMonitor(monitorName,
 					new ObjectName(objectName), attribute, discriminator, monitorUrlHolder.getmBeanServerConnection());
 		}
 		monitorUrlHolder.getMonitors().add(monitor);
 	}
 
 	/**
 	 * The group should stop at the next opportunity. At most within 5 seconds.
 	 */
 	public void stop() {
 		stopping = true;
 	}
 
 
 	/**
 	 * The group should respond true unless it knows it has failed
 	 */
 	public boolean isAlive() {
 		return alive;
 	}
 
 
 	/**
 	 * Start monitoring (as a thread) return when stopped
 	 */
 	@Override
 	public void run() {
 		try {
 			while (!stopping) {
 
 				long currentRun = new Date().getTime();
 				logger.debug("Checking interval for {} at {}", name, currentRun);
 
 				ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(
 						"JavaScript");
 				if (lastRun == null || lastRun + interval < currentRun) {
 					logger.debug("Running interval for {} at {}", name, currentRun);
 
 					// Run Monitors
 					for (String monitorUrlHolderKey : monitorUrlHolders.keySet()) {
 						MonitorUrlHolder monitorUrlHolder = monitorUrlHolders.get(monitorUrlHolderKey);
 
 						if (isFailed(monitorUrlHolder)) {
 							logger.debug("Reinitialising monitors as they are not connected");
 							initialiseMonitorUrl(monitorUrlHolder.getUrl(), monitorsConfiguration);
 						} else {
 							logger.debug("Executing Monitors");
 							Map<String, Object> results = new HashMap<String, Object>();
 							for (Monitor monitor : monitorUrlHolder.getMonitors()) {
 								try {
 									results.put(monitor.getName(), monitor.getReading());
 								} catch (ReadingFailedException e) {
 									results.put(monitor.getName(), e);
 									logger.error("{}", e);
 								}
 							}
 
 							for (String key : results.keySet()) {
 								scriptEngine.put(key, results.get(key));
 							}
 						}
 					}
 					for (String expression : expressions) {
 						try {
 							Object output = scriptEngine.eval(expression);
 							outputLogger.info("{}", output);
 						} catch (ScriptException e) {
 							logger.warn("Script Error {}", e);
 						}
 					}
 					// Run and output expressions
 					lastRun = currentRun;
 				}
 				Thread.sleep(4000);
 			}
 		} catch (InterruptedException e) {
 			logger.info("Interrupted", e);
 		} catch (MalformedObjectNameException e) {
 			logger.error("{}", e);
 			throw new RuntimeException(e);
 		} catch (MalformedURLException e) {
 			logger.error("{}", e);
 			throw new RuntimeException(e);
 		} finally {
 			// Tidy up all monitors / expressions IF possible
 			alive = false;
 
 		}
 
 	}
 
 	private boolean isFailed(MonitorUrlHolder monitorUrlHolder) {
 		if (monitorUrlHolder == null || monitorUrlHolder.getmBeanServerConnection() == null) {
 			return true;
 		}
 		try {
 			monitorUrlHolder.getmBeanServerConnection().getMBeanCount();
 		} catch (IOException e) {
 			logger.warn("Connection to JMX not functioning {}", e);
 			monitorUrlHolder.setmBeanServerConnection(null);
 			return true;
 		}
 
 		return false;
 	}
 
 	private class MonitorUrlHolder {
 		private String url;
 		private List<Monitor> monitors = new ArrayList<Monitor>();
 		private MBeanServerConnection mBeanServerConnection;
 
 		private MonitorUrlHolder(String url) {
 			this.url = url;
 		}
 
 		public String getUrl() {
 			return url;
 		}
 
 		public void setUrl(String url) {
 			this.url = url;
 		}
 
 		public List<Monitor> getMonitors() {
 			return monitors;
 		}
 
 		public void setMonitors(List<Monitor> monitors) {
 			this.monitors = monitors;
 		}
 
 		public MBeanServerConnection getmBeanServerConnection() {
 			return mBeanServerConnection;
 		}
 
 		public void setmBeanServerConnection(MBeanServerConnection mBeanServerConnection) {
 			this.mBeanServerConnection = mBeanServerConnection;
 		}
 	}
 }
