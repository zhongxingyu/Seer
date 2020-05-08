 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.util.logging;
 
 import gda.configuration.properties.LocalProperties;
 
 import java.net.URL;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.BasicConfigurator;
 import ch.qos.logback.classic.Logger;
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.classic.net.SocketAppender;
 import ch.qos.logback.classic.net.SocketReceiver;
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import ch.qos.logback.core.Appender;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 /**
  * Utility methods for Logback.
  */
 public class LogbackUtils {
 	
 	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogbackUtils.class);
 	
 	private static final String DEFAULT_SERVER_CONFIG = "configurations/server-default.xml";
 	
 	private static final String DEFAULT_CLIENT_CONFIG = "configurations/client-default.xml";
 	
 	/**
 	 * Returns the default Logback logger context.
 	 * 
 	 * <p>This method can be used instead of calling {@link LoggerFactory#getILoggerFactory()} directly and casting the
 	 * result to a Logback {@link LoggerContext}. It assumes that Logback is being used, so that the singleton SLF4J
 	 * logger factory can be cast to a Logback {@link LoggerContext}. If a {@link ClassCastException} occurs, a more
 	 * useful exception will be thrown instead.
 	 * 
 	 * @return the Logback logger context
 	 */
 	public static LoggerContext getLoggerContext() {
 		try {
 			return (LoggerContext) LoggerFactory.getILoggerFactory();
 		} catch (ClassCastException e) {
 			final String msg = "Couldn't cast the logger factory to a Logback LoggerContext. Perhaps you aren't using Logback?";
 			throw new RuntimeException(msg, e);
 		}
 	}
 	
 	/**
 	 * Resets the specified Logback logger context. This causes the following to happen:
 	 * 
 	 * <ul>
 	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
 	 * configured or used from code.)</li>
 	 * <li>Existing loggers are retained, but their levels are cleared.</li>
 	 * </ul>
 	 */
 	public static void resetLogging(LoggerContext loggerContext) {
 		loggerContext.reset();
 	}
 	
 	/**
 	 * Resets the default Logback logger context. This causes the following to happen:
 	 * 
 	 * <ul>
 	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
 	 * configured or used from code.)</li>
 	 * <li>Existing loggers are retained, but their levels are cleared.</li>
 	 * </ul>
 	 */
 	public static void resetLogging() {
 		resetLogging(getLoggerContext());
 	}
 	
 	/**
 	 * Resets the specified Logback logger context to the default configuration. This causes the following to happen:
 	 * 
 	 * <ul>
 	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
 	 * configured or used from code.)</li>
 	 * <li>Existing loggers are retained, but their levels are cleared.</li>
 	 * <li>A single (console) appender is added to the root logger.
 	 * </ul>
 	 */
 	public static void resetLoggingToDefaultConfiguration(LoggerContext loggerContext) {
 		loggerContext.reset();
 		BasicConfigurator.configure(loggerContext);
 	}
 	
 	/**
 	 * Resets the default Logback logger context to the default configuration. This causes the following to happen:
 	 * 
 	 * <ul>
 	 * <li>All appenders are removed from any loggers that have been created. (Loggers are created when they are
 	 * configured or used from code.)</li>
 	 * <li>Existing loggers are retained, but their levels are cleared.</li>
 	 * <li>A single (console) appender is added to the root logger.
 	 * </ul>
 	 */
 	public static void resetLoggingToDefaultConfiguration() {
 		resetLoggingToDefaultConfiguration(getLoggerContext());
 	}
 	
 	/**
 	 * Configures the default Logback logger context using the specified configuration file.
 	 * 
 	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
 	 * file will result in duplicate appenders.
 	 * 
 	 * @param filename the Logback configuration file
 	 */
 	
 	public static void configureLogging(LoggerContext loggerContext, String filename) throws JoranException {
 		JoranConfigurator configurator = new JoranConfigurator();
 		configurator.setContext(loggerContext);
 		configurator.doConfigure(filename);
 	}
 	
 	/**
 	 * Configures the default Logback logger context using the specified configuration file.
 	 * 
 	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
 	 * file will result in duplicate appenders.
 	 * 
 	 * @param url the Logback configuration file
 	 */
 	public static void configureLogging(LoggerContext loggerContext, URL url) throws JoranException {
 		JoranConfigurator configurator = new JoranConfigurator();
 		configurator.setContext(loggerContext);
 		configurator.doConfigure(url);
 	}
 	
 	/**
 	 * Configures the default Logback logger context using the specified configuration file.
 	 * 
 	 * <p>Appenders defined in the file are <b>added</b> to loggers. Repeatedly configuring using the same configuration
 	 * file will result in duplicate appenders.
 	 * 
 	 * @param filename the Logback configuration file
 	 */
 	public static void configureLogging(String filename) throws JoranException {
 		configureLogging(getLoggerContext(), filename);
 	}
 	
 	/**
 	 * Returns a list of all appenders for the specified logger.
 	 * 
 	 * @param logger a Logback {@link Logger}
 	 * 
 	 * @return a list of the logger's appenders
 	 */
 	public static List<Appender<ILoggingEvent>> getAppendersForLogger(Logger logger) {
 		List<Appender<ILoggingEvent>> appenders = new LinkedList<Appender<ILoggingEvent>>();
 		Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
 		while (iterator.hasNext()) {
 			appenders.add(iterator.next());
 		}
 		return appenders;
 	}
 	
 	/**
 	 * For the specified Logback logger context, dumps a list of all loggers, their levels, and their appenders.
 	 */
 	public static void dumpLoggers(LoggerContext loggerContext) {
 		System.out.println("Loggers:");
 		List<Logger> loggers = loggerContext.getLoggerList();
 		for (Logger logger : loggers) {
 			System.out.printf("    %s level=%s effective=%s\n", logger, logger.getLevel(), logger.getEffectiveLevel());
 			Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
 			while (it.hasNext()) {
 				Appender<ILoggingEvent> appender = it.next();
 				System.out.println("        " + appender);
 			}
 		}
 	}
 	
 	/**
 	 * For the default Logback logger context, dumps a list of all loggers, their levels, and their appenders.
 	 */
 	public static void dumpLoggers() {
 		dumpLoggers(getLoggerContext());
 	}
 	
 	/**
 	 * Name of property that specifies the logging configuration file used for server-side processes (channel server,
 	 * object servers).
 	 */
 	public static final String GDA_SERVER_LOGGING_XML = "gda.server.logging.xml";
 	
 	/**
 	 * Old property that specifies the logging configuration file to use for server-side processes.
 	 * Superseded by {@link #GDA_SERVER_LOGGING_XML}.
 	 */
 	private static final String LEGACY_GDA_LOGGER_IMPL_SERVER = "gda.logger.impl.server";
 	
 	/**
 	 * Configures Logback for a server-side process.
 	 */
 	public static void configureLoggingForServerProcess() {
 		URL defaultServerConfigFile = LogbackUtils.class.getResource(DEFAULT_SERVER_CONFIG);
 		configureLoggingForProcess(defaultServerConfigFile, GDA_SERVER_LOGGING_XML, LEGACY_GDA_LOGGER_IMPL_SERVER);
 	}
 	
 	/**
 	 * Name of property that specifies the logging configuration file used for client-side processes.
 	 */
 	public static final String GDA_CLIENT_LOGGING_XML = "gda.client.logging.xml";
 	
 	/**
 	 * Old property that specifies the logging configuration file to use for client-side processes.
 	 * Superseded by {@link #GDA_CLIENT_LOGGING_XML}.
 	 */
 	private static final String LEGACY_GDA_LOGGER_IMPL_CLIENT = "gda.logger.impl.client";
 	
 	/**
 	 * Configures Logback for a client-side process.
 	 */
 	public static void configureLoggingForClientProcess() {
 		URL defaultClientConfigFile = LogbackUtils.class.getResource(DEFAULT_CLIENT_CONFIG);
 		configureLoggingForProcess(defaultClientConfigFile, GDA_CLIENT_LOGGING_XML, LEGACY_GDA_LOGGER_IMPL_CLIENT);
 	}
 	
 	/**
 	 * Name of property that specifies the hostname/IP address of the log server.
 	 */
 	public static final String GDA_LOGSERVER_HOST = "gda.logserver.host";
 
 	/**
 	 * Name of property that specifies the port on which the log server appends logging events.
 	 */
 	public static final String GDA_LOGSERVER_OUT_PORT = "gda.logserver.out.port";
 
 	/**
 	 * Configures forwarding of logging events from log server to Beagle plugin view in own context.
 	 */
 	public static void configureLoggingForClientBeagle() {
 		final String logServerHost = LocalProperties.get(GDA_LOGSERVER_HOST);
 		final int logServerOutPort = LocalProperties.getInt(GDA_LOGSERVER_OUT_PORT, 6750);
 
 		if (logServerHost != null) {
 
 			// in Logback-beagle forwarding context
 			LoggerContext beagleForwardingContext = new LoggerContext();
 			beagleForwardingContext.setName("beagleForwarding");
 
 			// receive from log server ServerSocketAppender
 			SocketReceiver receiver = new SocketReceiver();
 			receiver.setContext(beagleForwardingContext);
 			receiver.setRemoteHost(logServerHost);
 			receiver.setPort(logServerOutPort);
 
 			// append to Beagle plugin view
 			SocketAppender beagleAppender = new SocketAppender();
 			beagleAppender.setContext(beagleForwardingContext);
 			beagleAppender.setRemoteHost("localhost");
 			beagleAppender.setPort(4321);
			beagleAppender.setReconnectionDelay(500);
 
 			beagleForwardingContext.register(receiver);
 			beagleForwardingContext.register(beagleAppender);
 
 			Logger rootLogger = beagleForwardingContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
 			rootLogger.addAppender(beagleAppender);
 
 			receiver.start();
 			beagleAppender.start();
 			beagleForwardingContext.start();
 		}
 	}
 
 	/**
 	 * Configures Logback for either a server- or client-side process, using a default configuration file, followed by
 	 * a specified configuration file (using the value of a property, or falling back to the value of a legacy
 	 * property).
 	 * 
 	 * @param defaultConfigFile the default logging configuration file, which will be applied first
 	 * @param newPropertyName the preferred property name to use for the custom logging configuration file
 	 * @param legacyPropertyName legacy property name that can be used instead
 	 */
 	protected static void configureLoggingForProcess(URL defaultConfigFile, String newPropertyName, String legacyPropertyName) {
 		
 		LoggerContext context = getLoggerContext();
 		
 		boolean oldPropertyIsBeingUsed = false;
 		
 		// Look for the new property first
 		String configFile = LocalProperties.get(newPropertyName);
 		
 		// If it isn't present, look for the old property instead
 		if (configFile == null) {
 			configFile = LocalProperties.get(legacyPropertyName);
 			oldPropertyIsBeingUsed = (configFile != null);
 		}
 		
 		// If the old property isn't found either, log an error. Treat this as non-fatal, because Logback will still
 		// be in its default state (so log messages will still be displayed on the console).
 		if (configFile == null) {
 			final String msg = String.format(
 				"Please set the %s property, to specify the logging configuration file",
 				newPropertyName);
 			logger.error(msg);
 			return;
 		}
 		
 		// Reset logging.
 		resetLogging(context);
 		
 		// If anything goes wrong from here onwards, we should throw an exception. It's not worth trying to log the
 		// error, since there may be no appenders.
 		
 		// Configure using the default logging configuration, if it can be found.
 		if (defaultConfigFile != null) {
 			try {
 				configureLogging(context, defaultConfigFile);
 			} catch (JoranException e) {
 				final String msg = String.format("Unable to configure logging using default configuration file %s", defaultConfigFile);
 				throw new RuntimeException(msg, e);
 			}
 		}
 		
 		// Configure using the specified logging configuration.
 		try {
 			configureLogging(context, configFile);
 		} catch (JoranException e) {
 			final String msg = String.format("Unable to configure logging using %s", configFile);
 			throw new RuntimeException(msg, e);
 		}
 		
 		// Emit warning if old property is being used
 		if (oldPropertyIsBeingUsed) {
 			final String msg = "You are using the old '{}' property. Please rename it to '{}'. " +
 				"Support for the old property may be removed in a future release of GDA.";
 			logger.warn(msg, legacyPropertyName, newPropertyName);
 		}
 	}
 	
 }
