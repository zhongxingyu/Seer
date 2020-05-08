 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
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
 
 package gda.epics.connection;
 
 import gda.epics.util.EpicsGlobals;
 import gda.epics.util.JCAUtils;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.CAStatusException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.Context;
 import gov.aps.jca.JCALibrary;
 import gov.aps.jca.Monitor;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.BYTE;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBRType;
 import gov.aps.jca.dbr.DBR_CTRL_Enum;
 import gov.aps.jca.dbr.DOUBLE;
 import gov.aps.jca.dbr.ENUM;
 import gov.aps.jca.dbr.FLOAT;
 import gov.aps.jca.dbr.INT;
 import gov.aps.jca.dbr.SHORT;
 import gov.aps.jca.dbr.STRING;
 import gov.aps.jca.event.ConnectionEvent;
 import gov.aps.jca.event.ConnectionListener;
 import gov.aps.jca.event.ContextExceptionEvent;
 import gov.aps.jca.event.ContextExceptionListener;
 import gov.aps.jca.event.ContextMessageEvent;
 import gov.aps.jca.event.ContextMessageListener;
 import gov.aps.jca.event.ContextVirtualCircuitExceptionEvent;
 import gov.aps.jca.event.GetEvent;
 import gov.aps.jca.event.GetListener;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The EPICSConnection class initialises JCA context, setting up network configuration for EPICS IOCs. It provides a
  * single Channel Access Context for managing all IO operations between GDA and EPICS servers and provides methods to
  * access process variables provided by EPICS database through a Channel object. It also handle the CA exceptions from
  * Channel Access.
  * <p>
  * Type specific <code>caget</code>, for example <code>cagetDouble</code> and <code>caput</code> methods are
  * provided here to encapsulate the type conversion or mapping between EPICS and Java, so the rest of GDA codes could be
  * developed more cleanly. These methods prevent automatic type conversion, for example from <code>double</code> to
  * <code>int</code> taking place on the Channel Access servers, and ensure no unpredicted precesion loss during the
  * operation. The automatic type conversions are considered undesirable.
  * </p>
  * 
  * @Dependence This class requires the properties file JCALibrary.properties which is stored in $GDA_PROPERTIES
  *             directory.
  */
 public class EpicsController implements ContextExceptionListener, ContextMessageListener {
 	private static final Logger logger = LoggerFactory.getLogger(EpicsController.class);
 	private double timeout = EpicsGlobals.getTimeout();// seconds
 	private AtomicInteger monitorCount = new AtomicInteger(0);
 	private Context context = null;
 	private ThreadPoolExecutor threadPool = null;
 	
 	/**
 	 * An enumerated type for DBR from EPICS.
 	 */
 	public enum MonitorType {
 		/**
 		 * NATIVE - the native DBR type, value only
 		 */
 		NATIVE,
 		/**
 		 * STS - contains value, alarm status, and alarm severity
 		 */
 		STS,
 		/**
 		 * TIME - contains value, alarm status, alarm severity, and time stamp
 		 */
 		TIME,
 		/**
 		 * CTRL - contains value, alarm status, alarm severity,units, display precision, graphic limits, and control
 		 * limits
 		 */
 		CTRL,
 		/**
 		 * GR - contains value, alarm status, alarm severity,units, display precision, and graphic limits
 		 */
 		GR
 	}
 
 	/**
 	 * Singleton instance.
 	 */
 	private static EpicsController instance = null;
 
 	/**
 	 * Singleton pattern to get instance of EpicsController.
 	 * 
 	 * @return <code>EpicsController</code> instance.
 	 */
 	public static synchronized EpicsController getInstance()
 	{
 		return getInstance(true);
 	}
 
 	/**
 	 * Singleton pattern to get instance of EpicsController.
 	 * @param contextRequired is normally True, but False for testing, to avoid leaving a orphan process.
 	 * 
 	 * @return <code>EpicsController</code> instance.
 	 */
 	public static synchronized EpicsController getInstance(boolean contextRequired){
 		// TODO not nice and clean
 		try {
 			if (instance == null)
 				instance = new EpicsController(contextRequired);
 			return instance;
 		} catch (Throwable th) {
 			th.printStackTrace();
 			throw new RuntimeException("failed to create EpicsController instance", th);
 		}
 	}
 
 	/**
 	 * Protected constructor.
 	 * 
 	 * @throws CAException
 	 */
 	@SuppressWarnings("unused")
 	protected EpicsController() throws CAException, InterruptedException {
 		new EpicsController(true);
 	}
 
 	/**
 	 * Protected constructor.
 	 * @param contextRequired is normally True, but False for testing, to avoid leaving a orphan process.
 	 * 
 	 * @throws CAException
 	 */
 	protected EpicsController(boolean contextRequired) throws CAException {
 		// TODO take care of shutdown
 		threadPool = new ThreadPoolExecutor(1, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
 		threadPool.prestartAllCoreThreads();
 		if (contextRequired)
 			initializeContext();
 	}
 
 	/**
 	 * Execute task in a separate thread.
 	 * 
 	 * @param task
 	 *            task to be executed.
 	 */
 	public void execute(Runnable task) {
 		threadPool.execute(task);
 	}
 
 	/**
 	 * Create a CA context. This context controls all IO operations and circuits through which Channels will be created
 	 * and connected between GDA and EPICS server.
 	 * 
 	 * @throws CAException
 	 */
 	protected void initializeContext() throws CAException {
 		try {
 			// initialises JCA, used to create context and manage JCA
 			// configuration info
 			JCALibrary jca = JCALibrary.getInstance();
 			context = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
 
 			context.initialize();
 
 			context.addContextExceptionListener(this);
 			context.addContextMessageListener(this);
 
 			logger.debug(context.getVersion().getVersionString());
 			context.printInfo();
 		} catch (CAException ex) {
 			logger.error(ex + " Unable to create Channel Access context");
 			throw ex;
 		}
 	}
 
 	/**
 	 * Create channel asynchronously, user need to provide connection listener.
 	 * 
 	 * @param pvname
 	 *            the process variable name
 	 * @param cl
 	 *            the connection listener
 	 * @return the created CA channel
 	 * @throws CAException
 	 */
 	public Channel createChannel(String pvname, ConnectionListener cl) throws CAException {
 		try {
 			return context.createChannel(pvname, cl);
 			// we do no need to flush
 		} catch (IllegalArgumentException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		} catch (IllegalStateException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		} catch (CAException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		}
 	}
 
 	/**
 	 * Anonymous connection listener class.
 	 */
 	private class ConnectionListenerImpl implements ConnectionListener {
 		volatile ConnectionEvent event;
 
 		@Override
 		public synchronized void connectionChanged(ConnectionEvent ev) {
 			event = ev;
 			this.notifyAll();
 		}
 	}
 
 	/**
 	 * Create CA channel synchronously,users must specify the time to wait in seconds.
 	 * 
 	 * @param pvname
 	 *            the process variable name
 	 * @param timeoutInS
 	 * @return the CA channel
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public Channel createChannel(String pvname, double timeoutInS) throws CAException, TimeoutException {
 		try {
 			Channel ch = null;
 			ConnectionListenerImpl cl = new ConnectionListenerImpl();
 			synchronized (cl) {
 				ch = context.createChannel(pvname, cl);
 
 				// already connected check
 				if (ch.getConnectionState() == Channel.CONNECTED) {
 					ch.removeConnectionListener(cl);
 					return ch;
 				}
 
 				// wait for connection completion
 				try {
 					cl.wait((long) (timeoutInS * 1000));
 				} catch (InterruptedException e) {
 					throw new CAException("InterruptedException while waiting for connection to channel: " + pvname);
 				}
 			}
 			ch.removeConnectionListener(cl);
 
 			final ConnectionEvent event = cl.event;
 			if (event == null)
 				throw new TimeoutException("connection timeout for '" + pvname + "', "+timeoutInS+"s");
 
 			if (!event.isConnected())
 				throw new CAException("failed to connect to '" + pvname + "'");
 
 			logger.debug("Channel {}  is created.", ch.getName());
 			return ch;
 		} catch (IllegalArgumentException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		} catch (IllegalStateException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		} catch (CAException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		} catch (TimeoutException e) {
 			logger.error("Error on creating CA channel for {} - message: {}", pvname, e.getMessage());
 			throw e;
 		}
 	}
 
 	/**
 	 * creates a CA channel using default timeout.
 	 * 
 	 * @param pvname
 	 * @return channel
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public Channel createChannel(String pvname) throws CAException, TimeoutException {
 		return createChannel(pvname, timeout);
 	}
 
 	/**
 	 * Clear the resources used by the specified channel. It wraps the CA channel destroy method to handle CA
 	 * exceptions.
 	 * 
 	 * @param ch
 	 */
 	public void destroy(Channel ch) {
 		try {
 			ch.destroy();
 			logger.debug("Channel " + ch.getName() + " is destroyed.");
 		} catch (IllegalStateException e) {
 			logger.error("Error on destroying CA channel: " + ch.getName() + " - " + e.getMessage());
 		} catch (CAException e) {
 			logger.error("Error on destroying CA channel: " + ch.getName() + " - " + e.getMessage());
 		}
 	}
 
 	/**
 	 * @return return the total number of channels in the default CA context.
 	 */
 	public int getTotalNumberOfChannels() {
 		return context.getChannels().length;
 	}
 
 	// *********** Channel access methods ***********
 	/**
 	 * gets current value in double from the specified channel, Note type conversion may be performed by EPICS server if
 	 * the underlying native type is not double.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return double - the channel's value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public double cagetDouble(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((DOUBLE) getDBR(ch, DBRType.DOUBLE, 1)).getDoubleValue()[0];
 	}
 
 	/**
 	 * gets current value in float from the specified channel, Note type conversion may be performed by EPICS server if
 	 * the underlying native type is not float.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return float - the channel's value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public float cagetFloat(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((FLOAT) getDBR(ch, DBRType.FLOAT, 1)).getFloatValue()[0];
 	}
 
 	/**
 	 * gets current value in short from the specified channel, Note type conversion may be performed by EPICS server if
 	 * the underlying native type is not short.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return short - the channel's value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short cagetShort(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((SHORT) getDBR(ch, DBRType.SHORT, 1)).getShortValue()[0];
 	}
 
 	/**
 	 * gets current enum position value in short from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return short - the channel's enumerated position value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short cagetEnum(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((ENUM) getDBR(ch, DBRType.ENUM, 1)).getEnumValue()[0];
 	}
 
 	/**
 	 * gets labels in String for enumerated positions from the specified channel.
 	 * 
 	 * @param ch
 	 *            the specified channel
 	 * @return String[] - the labels
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public String[] cagetLabels(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((DBR_CTRL_Enum) getDBR(ch, DBRType.CTRL_ENUM)).getLabels();
 	}
 
 	public String[] cagetLabels(Channel ch, double timeout) throws TimeoutException, CAException {
 		return ((DBR_CTRL_Enum) getDBR(ch, DBRType.CTRL_ENUM, timeout)).getLabels();
 	}
 
 	private DBR getDBR(Channel ch, DBRType type, double timeout) throws IllegalStateException, TimeoutException, CAException {
 		return getDBR(ch, type, ch.getElementCount(),timeout);
 	}
 
 	/**
 	 * gets current value in int from the specified channel, Note type conversion may be performed by EPICS server if
 	 * the underlying native type is not int.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return int - the channel's value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public int cagetInt(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((INT) getDBR(ch, DBRType.INT, 1)).getIntValue()[0];
 	}
 
 	/**
 	 * gets current value in byte from the specified channel, Note type conversion may be performed by EPICS server if
 	 * the underlying native type is not byte.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return byte - the channel's value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public byte cagetByte(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((BYTE) getDBR(ch, DBRType.BYTE, 1)).getByteValue()[0];
 	}
 
 	/**
 	 * gets current value in String from the specified channel, Note type conversion may be performed by EPICS server if
 	 * the underlying native type is not String. Precision will affect the result.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return String - the channel's value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public String cagetString(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((STRING) getDBR(ch, DBRType.STRING, 1)).getStringValue()[0];
 	}
 
 	/**
 	 * a handy method to return everything in String type for console display, to emulate terminal command 'caget'
 	 * gets the current value of the specified channel, returns it as String without implicit type conversion.
 	 * 
 	 * @param ch
 	 *            the specified channel
 	 * @return String - the value of the channel
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public String caget(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		String value = null;
 		checkConnection(ch);
 		if (ch.getFieldType().isBYTE()) {
 			value = String.valueOf(cagetByte(ch));
 		} else if (ch.getFieldType().isINT()) {
 			value = String.valueOf(cagetInt(ch));
 		} else if (ch.getFieldType().isFLOAT()) {
 			value = String.valueOf(cagetFloat(ch));
 		} else if (ch.getFieldType().isDOUBLE()) {
 			value = String.valueOf(cagetDouble(ch));
 		} else if (ch.getFieldType().isSHORT()) {
 			value = String.valueOf(cagetShort(ch));
 		} else if (ch.getFieldType().isENUM()) {
 			value = String.valueOf(cagetEnum(ch));
 		} else {
 			value = cagetString(ch);
 		}
 		return value;
 	}
 
 	private void checkConnection(Channel ch) throws InterruptedException {
 		double timeout_s = EpicsGlobals.getTimeout();
 		checkConnection(ch, timeout_s);
 	}
 
 	private void checkConnection(Channel ch, double timeout_s) throws InterruptedException {
 		if (ch.getConnectionState() == Channel.CONNECTED)
 			return;
 		long startTime_ms = System.currentTimeMillis();
 		long timeout_ms = (long) (timeout_s * 1000.);
 
 		while (ch.getConnectionState() != Channel.CONNECTED && (System.currentTimeMillis() - startTime_ms < timeout_ms)) {
 			Thread.sleep(10);
 		}
 		if (ch.getConnectionState() != Channel.CONNECTED) {
 			logger.error("Connection to {} request timeout {}s", ch.getName(), timeout_s);
 		}
 	}
 
 	/**
 	 * gets a string value array from this channel without implicit CA data type conversion, return the results as a
 	 * String array.
 	 * 
 	 * @param ch
 	 * @param elementCount
 	 * @return String[]
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public String[] caget(Channel ch, int elementCount) throws TimeoutException, CAException, InterruptedException {
 		checkConnection(ch);
 		String[] value = new String[elementCount];
 		int i = 0;
 		if (ch.getFieldType().isBYTE()) {
 			for (byte element : cagetByteArray(ch)) {
 				value[i] = String.valueOf(element);
 				i++;
 			}
 		} else if (ch.getFieldType().isINT()) {
 			for (int element : cagetIntArray(ch)) {
 				value[i] = String.valueOf(element);
 				i++;
 			}
 		} else if (ch.getFieldType().isFLOAT()) {
 			for (float element : cagetFloatArray(ch)) {
 				value[i] = String.valueOf(element);
 				i++;
 			}
 		} else if (ch.getFieldType().isDOUBLE()) {
 			for (double element : cagetDoubleArray(ch)) {
 				value[i] = String.valueOf(element);
 				i++;
 			}
 		} else if (ch.getFieldType().isSHORT()) {
 			for (short element : cagetShortArray(ch)) {
 				value[i] = String.valueOf(element);
 				i++;
 			}
 		} else if (ch.getFieldType().isENUM()) {
 			for (short element : cagetEnumArray(ch)) {
 				value[i] = String.valueOf(element);
 				i++;
 			}
 		} else {
 			value = cagetStringArray(ch);
 		}
 		return value;
 	}
 
 	/**
 	 * Gets the specified CA compound type DBR from this channel.
 	 * 
 	 * @param ch
 	 * @param type
 	 * @return specified CA compound type DBR from this channel
 	 * @throws CAException
 	 * @throws TimeoutException
 	 * @throws InterruptedException 
 	 */
 	public DBR caget(Channel ch, MonitorType type) throws CAException, TimeoutException, InterruptedException {
 		DBR dbr = null;
 		switch (type) {
 		case TIME:
 			dbr = getDBR(ch, TIMEHandler.getTIMEType(ch));
 			break;
 		case STS:
 			dbr = getDBR(ch, STSHandler.getSTSType(ch));
 			break;
 		case CTRL:
 			dbr = getDBR(ch, CTRLHandler.getCTRLType(ch));
 			break;
 		case GR:
 			dbr = getDBR(ch, GRHandler.getGRType(ch));
 			break;
 		case NATIVE:
 			dbr = getDBR(ch, ch.getFieldType());
 			break;
 		default:
 			logger.error("Invalid Return Type is requested.");
 			break;
 		}
 		return dbr;
 	}
 
 	// ******** channel access methods that returns multi-element array *********
 	/**
 	 * Gets an array of short (enumerated positions) from enumerated field of this channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return short[] - the channel's value, enumeration returned as an array of short
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short[] cagetEnumArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((ENUM) getDBR(ch, DBRType.ENUM)).getEnumValue();
 	}
 
 	public short[] cagetEnumArray(Channel ch, int count) throws TimeoutException, CAException, InterruptedException {
 		return ((ENUM) getDBR(ch, DBRType.ENUM, count)).getEnumValue();
 	}
 	
 	/**
 	 * gets a double array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return double[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public double[] cagetDoubleArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((DOUBLE) getDBR(ch, DBRType.DOUBLE)).getDoubleValue();
 	}
 
 	/**
 	 * gets a fixed length double array from the specified channel.
 	 * 
 	 * @param theChannel
 	 *            the CA Channel.
 	 * @param numberOfElements
 	 *            the number of elements to get.
 	 * @return double[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public double[] cagetDoubleArray(Channel theChannel, int numberOfElements) throws TimeoutException, CAException, InterruptedException {
 		return ((DOUBLE) getDBR(theChannel, DBRType.DOUBLE, numberOfElements)).getDoubleValue();
 	}
 	
 	
 	/**
 	 * gets a float array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return float[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public float[] cagetFloatArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((FLOAT) getDBR(ch, DBRType.FLOAT)).getFloatValue();
 	}
 
 	public float[] cagetFloatArray(Channel ch, int count) throws TimeoutException, CAException, InterruptedException {
 		return ((FLOAT) getDBR(ch, DBRType.FLOAT, count)).getFloatValue();
 	}
 
 	/**
 	 * gets a short array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return short[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public short[] cagetShortArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((SHORT) getDBR(ch, DBRType.SHORT)).getShortValue();
 	}
 
 	public short[] cagetShortArray(Channel ch, int count) throws TimeoutException, CAException, InterruptedException {
 		return ((SHORT) getDBR(ch, DBRType.SHORT,count)).getShortValue();
 	}
 	
 	/**
 	 * gets a integer array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return int[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public int[] cagetIntArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((INT) getDBR(ch, DBRType.INT)).getIntValue();
 	}
 
 	/**
 	 * gets a integer array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @param elementCount
 	 * @return int[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public int[] cagetIntArray(Channel ch, int elementCount) throws TimeoutException, CAException, InterruptedException {
 		return ((INT) getDBR(ch, DBRType.INT,elementCount)).getIntValue();
 
 	}
 	/**
 	 * gets a byte array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel.
 	 * @return byte[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public byte[] cagetByteArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((BYTE) getDBR(ch, DBRType.BYTE)).getByteValue();
 	}
 	
 	/**
 	 * gets a fixed length byte array from the specified channel.
 	 * 
 	 * @param theChannel
 	 *            the CA Channel.
 	 * @param numberOfElements
 	 *            the number of elements to get.
 	 * @return byte[] - the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public byte[] cagetByteArray(Channel theChannel, int numberOfElements) throws TimeoutException, CAException, InterruptedException {
 		return ((BYTE) getDBR(theChannel, DBRType.BYTE, numberOfElements)).getByteValue();
 	}
 	
 	/**
 	 * gets a String array from the specified channel.
 	 * 
 	 * @param ch
 	 *            the CA Channel. Created by
 	 * @return the channel's values
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public String[] cagetStringArray(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return ((STRING) getDBR(ch, DBRType.STRING)).getStringValue();
 	}
 
 	public String[] cagetStringArray(Channel ch, int count) throws TimeoutException, CAException, InterruptedException {
 		return ((STRING) getDBR(ch, DBRType.STRING, count)).getStringValue();
 	}
 	
 	// ******** wrapper method to handle CA exceptions *******************
 
 	/**
 	 * Synchronously reads this Channel's value using the specified DBR type and native element count.
 	 * 
 	 * @param ch
 	 *            the CA channel
 	 * @param type
 	 *            The DBR type
 	 * @return DBR the channel's value in the specified DBR type.
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public DBR getDBR(Channel ch, DBRType type) throws TimeoutException, CAException, InterruptedException {
 		return getDBR(ch, type, ch.getElementCount());
 	}
 
 	
 	/**
 	 * Anonymous Get Listener used by caget methods.
 	 */
 	private class GetListenerImpl implements GetListener {
 		volatile GetEvent event = null;
 
 		@Override
 		public synchronized void getCompleted(GetEvent ev) {
 			event = ev;
 			this.notifyAll();
 		}
 	}
 
 	/**
 	 * Anonymous Put Listener used by caput methods
 	 */
 	private class PutListenerImpl implements PutListener {
 		volatile PutEvent event = null;
 
 		@Override
 		public synchronized void putCompleted(PutEvent ev) {
 			event = ev;
 			this.notifyAll();
 		}
 	}
 
 	/**
 	 * Synchronously reads this Channel's value using the specified DBR type and element count. Wait time is set in
 	 * {@link gda.epics.util.EpicsGlobals} or in properties file .i.e <code>gda.epics.request.timeout</code>.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param type -
 	 *            DBRType
 	 * @param count -
 	 *            int
 	 * @return DBR the channel's value in specified DBR type.
 	 * @throws CAException
 	 * @throws IllegalStateException
 	 * @throws InterruptedException 
 	 */
 	public DBR getDBR(Channel ch, DBRType type, int count) throws TimeoutException, CAException, InterruptedException {
 		checkConnection(ch);
 		return getDBR(ch, type, count, timeout);
 	}
 
 	private DBR getDBR(Channel ch, DBRType type, int count, double timeout) throws TimeoutException, CAException {
		int elementCount = ch.getElementCount();
		if (elementCount < count) {
 			logger.warn("Requested "+count+" elements from "+ch.getName()+" but channel elementCount is " + elementCount +
 					" ask Controls to increase EPICS_CA_MAX_ARRAY_BYTES.");
 		}
 		try {
 			GetListenerImpl listener = new GetListenerImpl();
 			synchronized (listener) {
 				ch.get(type, count, listener);
 				context.flushIO();
 
 				try {
 					listener.wait((long) (timeout * 1000));
 				} catch (InterruptedException e) {
 					throw new CAException("InterruptedException while getting DBR from: " + ch.getName());
 				}
 			}
 
 			final GetEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("get timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "get failed");
 
 			return event.getDBR();
 		} catch (TimeoutException ex) {
 			logger.error(" getDBR( {} ) failed. {}",ch.getName(), ex.getMessage());
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" getDBR( {} ) failed. {}", ch.getName(), ex.getMessage());
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" getDBR( {} ) failed. {}", ch.getName(), ex.getMessage());
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" getDBR( {} ) failed. {}", ch.getName(), ex.getMessage());
 			Thread.dumpStack();
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 	
 	// ******* Channel Access methods that returns CA compound data type *******
 	/**
 	 * returns a STS typed DBR value of the channel. This STS type contains the channel's value, alarm status, and alarm
 	 * severity. These individual properties can then be accessed using static methods in {@link STSHandler}.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @return DBR - the DBR_STS value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @see STSHandler#getStatus(DBR)
 	 * @see STSHandler#getSeverity(DBR)
 	 */
 	public DBR getSTS(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return getDBR(ch, STSHandler.getSTSType(ch));
 	}
 
 	/**
 	 * returns a GR typed DBR value of the channel. This GR type contains the channel's value, alarm status, alarm
 	 * severity, units, display precision, and graphic limits. Units, display precision, and graphic limits properties
 	 * can then be accessed using static methods in {@link GRHandler}.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @return DBR - the DBR_GR value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @see GRHandler#getUnits(DBR)
 	 * @see GRHandler#getLowerDispLimit(DBR)
 	 * @see GRHandler#getUpperDispLimit(DBR)
 	 * @see GRHandler#getLowerAlarmLimit(DBR)
 	 * @see GRHandler#getUpperAlarmLimit(DBR)
 	 * @see GRHandler#getLowerWarningLimit(DBR)
 	 * @see GRHandler#getUpperWarningLimit(DBR)
 	 */
 	public DBR getGR(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return getDBR(ch, GRHandler.getGRType(ch));
 	}
 
 	/**
 	 * returns a CTRL typed DBR value of the channel. This CTRL type contains the channel's value, alarm status, alarm
 	 * severity, units, display precision, graphic limits, and control limits. Control limits properties can then be
 	 * accessed using static methods in {@link CTRLHandler}.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @return DBR - the DBR_CTRL value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @see CTRLHandler#getLowerCrtlLimit(DBR)
 	 * @see CTRLHandler#getUpperCtrlLimit(DBR)
 	 */
 	public DBR getCTRL(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return getDBR(ch, CTRLHandler.getCTRLType(ch));
 	}
 
 	/**
 	 * returns a TIME typed DBR value of the channel. This TIME type contains the channel's value, alarm status, alarm
 	 * severity, and timestamp. The timestamp properties can then be accessed using static methods in
 	 * {@link TIMEHandler}.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @return DBR - the DBR_TIME value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 * @see TIMEHandler#getTimeStamp(DBR)
 	 */
 	public DBR getTIME(Channel ch) throws TimeoutException, CAException, InterruptedException {
 		return getDBR(ch, TIMEHandler.getTIMEType(ch));
 	}
 
 
 	/**
 	 * adds a monitor listener to the specified channel using the channel's native count. The method is designed to
 	 * return a monitor object.
 	 * 
 	 * @param ch -
 	 *            the channel to be monitored
 	 * @param type -
 	 *            specify the return DBR type in the Monitor Event
 	 * @param mask -
 	 *            what to monitor, possible values are VALUE, ALARM, or LOG
 	 * @param ml -
 	 *            the Monitor Listener to be added to the channel
 	 * @return the monitor
 	 * @throws CAException
 	 */
 	// NOTE: there is no destruction of monitors
 	public Monitor setMonitor(Channel ch, DBRType type, int mask, MonitorListener ml) throws CAException {
 		return setMonitor(ch, type, mask, ml, ch.getElementCount());
 	}
 
 	public Monitor setMonitor(Channel ch, DBRType type, int mask, MonitorListener ml, int count) throws CAException {
 		try {
 			Monitor mntr = null;
 			mntr = ch.addMonitor(type, count, mask, ml);
 			context.flushIO();
 			return mntr;
 		} catch (CAException ex) {
 			logger.error("add monitor {} to the channel {} failed.", ml.getClass().getName(), ch.getName());
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error("add monitor {} to the channel {} failed.", ml.getClass().getName(), ch.getName());
 			throw ex;
 		}
 	}
 	
 	/**
 	 * adds a VALUE monitor to the specified channel using the specified MonitorType.
 	 * <p>
 	 * Valid MonitorType includes:
 	 * <li>NATIVE - the native DBR type, value only</li>
 	 * <li>STS - contains value, alarm status, and alarm severity</li>
 	 * <li>TIME - contains value, alarm status, alarm severity, and time stamp</li>
 	 * <li>GR - contains value, alarm status, alarm severity,units, display precision, and graphic limits</li>
 	 * <li>CTRL - contains value, alarm status, alarm severity,units, display precision, graphic limits, and control
 	 * limits</li>
 	 * 
 	 * @param ch -
 	 *            the channel to be monitored
 	 * @param ml -
 	 *            the Monitor Listener
 	 * @param type -
 	 *            the Return Type required
 	 * @return the monitor
 	 * @throws CAException
 	 */
 	public Monitor setMonitor(Channel ch, MonitorListener ml, MonitorType type) throws CAException, InterruptedException {
 		return setMonitor(ch, ml, type, ch.getElementCount()); 
 	}
 
 	public Monitor setMonitor(Channel ch, MonitorListener ml, MonitorType type, int count) throws CAException, InterruptedException {
 		checkConnection(ch);
 		Monitor mnt = null;
 		switch (type) {
 		case STS:
 			mnt = setMonitor(ch, STSHandler.getSTSType(ch), Monitor.VALUE | Monitor.ALARM, ml, count);
 			break;
 		case TIME:
 			mnt = setMonitor(ch, TIMEHandler.getTIMEType(ch), Monitor.VALUE | Monitor.ALARM, ml, count);
 			break;
 		case GR:
 			mnt = setMonitor(ch, GRHandler.getGRType(ch), Monitor.VALUE | Monitor.ALARM, ml, count);
 			break;
 		case CTRL:
 			mnt = setMonitor(ch, CTRLHandler.getCTRLType(ch), Monitor.VALUE | Monitor.ALARM, ml, count);
 			break;
 		case NATIVE:
 			mnt = setMonitor(ch, ch.getFieldType(), Monitor.VALUE | Monitor.ALARM, ml, count);
 			break;
 		default:
 			logger.error("Invalid Monitor Type is requested.");
 			break;
 		}
 		return mnt;
 	}
 	
 	/**
 	 * convenient method to support a deprecated interface
 	 * 
 	 * @param ch
 	 * @param ml
 	 * @param type
 	 * @return Monitor
 	 * @throws CAException
 	 */
 	@Deprecated
 	public Monitor setMonitor(Channel ch, MonitorListener ml, Object type) throws CAException, InterruptedException {
 		if (type instanceof MonitorType) {
 			MonitorType new_type = (MonitorType) type;
 			return setMonitor(ch, ml, new_type);
 		}
 		logger.warn("Monitor type " + type + " requested is not available.");
 		return null;
 
 	}
 	
 	/**
 	 * Sets a VALUE monitor to the specified channel. The monitor event returns the default/native DBR type and element
 	 * count.
 	 * 
 	 * @param ch
 	 * @param ml
 	 * @return monitor
 	 * @throws CAException
 	 */
 	public Monitor setMonitor(Channel ch, MonitorListener ml) throws CAException, InterruptedException {
 		return setMonitor(ch, ml, MonitorType.NATIVE);
 	}
 
 	public Monitor setMonitor(Channel ch, MonitorListener ml, int count) throws CAException, InterruptedException {
 		return setMonitor(ch, ml, MonitorType.NATIVE, count);
 	}
 	
 	/**
 	 * adds a ALARM monitor to the specified channel using the specified MonitorType. Valid MonitorType includes:
 	 * <li>NATIVE - the native DBR type, value only</li>
 	 * <li>STS - contains value, alarm status, and alarm severity</li>
 	 * <li>TIME - contains value, alarm status, alarm severity, and time stamp</li>
 	 * <li>GR - contains value, alarm status, alarm severity,units, display precision, and graphic limits</li>
 	 * <li>CTRL - contains value, alarm status, alarm severity,units, display precision, graphic limits, and control
 	 * limits</li>
 	 * 
 	 * @param ch -
 	 *            the channel to be monitored
 	 * @param ml -
 	 *            the Monitor Listener
 	 * @param type -
 	 *            the Return Type required
 	 * @return the monitor
 	 * @throws CAException
 	 */
 	public Monitor addAlarmMonitor(Channel ch, MonitorListener ml, MonitorType type) throws CAException {
 		Monitor mnt = null;
 		switch (type) {
 		case STS:
 			mnt = setMonitor(ch, STSHandler.getSTSType(ch), Monitor.ALARM, ml);
 			break;
 		case TIME:
 			mnt = setMonitor(ch, TIMEHandler.getTIMEType(ch), Monitor.ALARM, ml);
 			break;
 		case GR:
 			mnt = setMonitor(ch, GRHandler.getGRType(ch), Monitor.ALARM, ml);
 			break;
 		case CTRL:
 			mnt = setMonitor(ch, CTRLHandler.getCTRLType(ch), Monitor.ALARM, ml);
 			break;
 		case NATIVE:
 			mnt = setMonitor(ch, ch.getFieldType(), Monitor.ALARM, ml);
 			break;
 		default:
 			logger.error("Invalid Monitor Type is requested.");
 			break;
 		}
 		return mnt;
 	}
 
 	/**
 	 * Adds a ALARM monitor to the specified channel. The monitor event returns the default/native DBR type and element
 	 * count.
 	 * 
 	 * @param ch
 	 * @param ml
 	 * @return monitor
 	 * @throws CAException
 	 */
 	public Monitor addAlarmMonitor(Channel ch, MonitorListener ml) throws CAException {
 		return addAlarmMonitor(ch, ml, MonitorType.NATIVE);
 	}
 
 	/**
 	 * This method removes the monitor of a channel.
 	 * 
 	 * @param mntr
 	 */
 	// TODO Check if Monitor.clear() destroys the monitor?
 	public void clearMonitor(Monitor mntr) {
 		try {
 			mntr.clear();
 		} catch (CAException e) {
 			logger.error("Clear Monitor for channel {} failed. ", mntr.getChannel().getName());
 		}
 	}
 
 	// ########## Channel Access Put Methods ###############################
 
 	// --------- PUT & Forget methods ---------------------
 	/**
 	 * writes a value to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            double the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, double value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            float the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, float value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            short the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, short value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            int the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, int value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            string - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, String value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value array to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, double[] value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			Thread.dumpStack();
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			Thread.dumpStack();
 			throw ex;
 		} catch (java.nio.BufferOverflowException ex) {
 			logger.error("put to channel: " + ch.getName() + " value: " + value + " failed. ", ex);
 			Thread.dumpStack();
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value array to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, float[] value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value array to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, short[] value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value array to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, int[] value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * writes a value array to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, String[] value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 	/**
 	 * writes a byte array to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, byte[] value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	/**
 	 * converts a string to an int array, then writes to this channel without waiting for completion.
 	 * 
 	 * @param ch
 	 *            the EPICS channel
 	 * @param value
 	 *            array - the value to be set to this channel
 	 * @throws CAException
 	 */
 	public void caputAsWaveform(Channel ch, String value) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(JCAUtils.getIntArrayFromWaveform(value));
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error("put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		}
 	}
 
 	// -------------- Put & Wait or timeout -----------------------------
 	/**
 	 * Synchronously writes a value to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch -
 	 *            the EPICS channel
 	 * @param value -
 	 *            double the value to be set to this channel
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caputWait(Channel ch, double value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously writes a value to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch -
 	 *            the EPICS channel
 	 * @param value -
 	 *            float the value to be set to this channel
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, float value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously writes a value to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch -
 	 *            the EPICS channel
 	 * @param value -
 	 *            short the value to be set to this channel
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, short value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously writes a value to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch -
 	 *            the EPICS channel
 	 * @param value -
 	 *            int the value to be set to this channel
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, int value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	public void caputWait(Channel ch, int value, double timeout) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 	/**
 	 * Synchronously puts a value to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, String value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously puts a value array to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, double[] value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	public void caputWait(Channel ch, byte[] value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 	
 	
 	/**
 	 * Synchronously puts a value array to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, int[] value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously puts a value array to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, float[] value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously puts a value array to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, short[] value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously puts a value array to this channel and wait for return before timeout This timeout in GDA is set by
 	 * <code>
 	 * gda.epics.request.timeout</code> property in java.properties file, or the default value set in
 	 * {@link gda.epics.util.EpicsGlobals} (30 seconds).
 	 * 
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWait(Channel ch, String[] value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, value, timeout);
 	}
 
 	/**
 	 * Synchronously takes a string, converts it into an int[], then puts this array to the channel and
 	 * wait for return before timeout. This timeout in GDA is set by <code>gda.epics.request.timeout</code>
 	 * property in java.properties file, or the default value set in {@link gda.epics.util.EpicsGlobals}
 	 * (30 seconds).
 	 * @param ch
 	 * @param value
 	 * @throws TimeoutException
 	 * @throws CAException
 	 */
 	public void caputWaitAsWaveform(Channel ch, String value) throws TimeoutException, CAException, InterruptedException {
 		caput(ch, JCAUtils.getIntArrayFromWaveform(value), timeout);
 	}
 
 	/**
 	 * Writes a double value to the channel with the user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, double value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a float value to the channel with the user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, float value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a short value to the channel with the user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, short value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a int value to the channel with the user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, int value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a String value to the channel with the user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, String value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a double array to the channel with user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, double[] value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a int array to the channel with user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, int[] value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a short array to the channel with user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, short[] value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a float array to the channel with user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, float[] value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Writes a String array to the channel with user specified timeout in seconds.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param timeout -
 	 *            timeout in seconds, if <code>0</code> waits until completed (can be forever).
 	 * @throws CAException
 	 * @throws TimeoutException
 	 */
 	public void caput(Channel ch, String[] value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	// ---------- Put & Listen -----------------------------
 	/**
 	 * Asynchronously writes a double value to this channel with user provided put listener for callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, double value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a float value to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, float value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a short value to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, short value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a int value to this channel with user provided put listener which handles the put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, int value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 //			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a String value to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, String value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a double array to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, double[] value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a int array to this channel with user provided put listener which handles the put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, int[] value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a float array to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, float[] value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a short array to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, short[] value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a String array to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 */
 	public void caput(Channel ch, String[] value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 	public void caput(Channel ch, byte[] value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		checkConnection(ch, timeout);
 		try {
 			PutListenerImpl listener = new PutListenerImpl();
 			synchronized (listener) {
 				ch.put(value, listener);
 				context.flushIO();
 				logger.debug("set {} to {} ", ch.getName(), value);
 
 				listener.wait((long) (timeout * 1000));
 			}
 
 			final PutEvent event = listener.event;
 			if (event == null)
 				throw new TimeoutException("put timeout, "+timeout+"s");
 
 			if (event.getStatus() != CAStatus.NORMAL)
 				throw new CAStatusException(event.getStatus(), "put failed");
 
 		} catch (TimeoutException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a String array to this channel with user provided put listener which handles the
 	 * put-callback.
 	 * 
 	 * @param ch -
 	 *            the CA channel
 	 * @param value -
 	 *            the value to be set to this channel
 	 * @param pl -
 	 *            the listener of this put action - callback
 	 * @throws CAException
 	 * @throws InterruptedException 
 	 */
 	public void caput(Channel ch, byte[] value, PutListener pl) throws CAException, InterruptedException{
 		checkConnection(ch);
 		try {
 			ch.put(value, pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously writes a string to this channel after converting to an int array.
 	 * With user provided put listener which handles the put-callback.
 	 * @param ch
 	 * @param value
 	 * @param pl
 	 * @throws CAException
 	 */
 	public void caputAsWaveform(Channel ch, String value, PutListener pl) throws CAException, InterruptedException {
 		checkConnection(ch);
 		try {
 			ch.put(JCAUtils.getIntArrayFromWaveform(value), pl);
 			context.flushIO();
 			logger.debug("set {} to {} ", ch.getName(), value);
 		} catch (IllegalStateException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (CAException ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw ex;
 		} catch (Throwable ex) {
 			logger.error(" put to channel: {} value: {} failed. ", ch.getName(), value);
 			throw new RuntimeException("unexpected exception", ex);
 		}
 	}
 
 	@Override
 	public void contextVirtualCircuitException(ContextVirtualCircuitExceptionEvent e) {
 		logger.error("CA Virtual Circuit {} status: {} ", e.getVirtualCircuit(), e.getStatus());
 	}
 
 	@Override
 	public void contextException(ContextExceptionEvent cee) {
 		logger.error("CA context for channel {} message: {}", cee.getChannel(), cee.getMessage());
 	}
 
 	/**
 	 * A handler for non-exception asynchronous message from the Context. It prints to the log.
 	 * 
 	 * @param cme -
 	 *            context message event
 	 */
 	@Override
 	public void contextMessage(ContextMessageEvent cme) {
 		logger.info("CA context message: {}", cme.getMessage());
 	}
 
 	/**
 	 * returns monitor count from EPICS controller
 	 * 
 	 * @return monitor count
 	 */
 	public int getMonitorCount() {
 		return monitorCount.get();
 	}
 	/**
 	 * return a PV value monitor to the specified channel.
 	 * @param dataChannel
 	 * @return the monitor
 	 * @throws IllegalStateException
 	 * @throws CAException
 	 */
 	public Monitor addMonitor(Channel dataChannel) throws IllegalStateException, CAException {
 		return dataChannel.addMonitor(Monitor.VALUE);
 	}
 
 	public void caputWait(Channel channel, String value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		caput(channel,value, timeout);		
 	}
 
 	public void caputWait(Channel channel, Double value, double timeout) throws CAException, TimeoutException, InterruptedException {
 		caput(channel,value.doubleValue(), timeout);		
 	}
 }
