 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package gda.epics;
 
 import static java.text.MessageFormat.format;
 import static org.apache.commons.lang.ArrayUtils.toObject;
 import static org.apache.commons.lang.ArrayUtils.toPrimitive;
 import gda.configuration.properties.LocalProperties;
 import gda.epics.connection.EpicsController;
 import gda.epics.util.EpicsGlobals;
 import gda.observable.Observable;
 import gda.observable.Observer;
 import gda.observable.ObservableUtil;
 import gda.observable.Predicate;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.CAStatusException;
 import gov.aps.jca.Channel;
 import gov.aps.jca.Monitor;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.BYTE;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBRType;
 import gov.aps.jca.dbr.DOUBLE;
 import gov.aps.jca.dbr.ENUM;
 import gov.aps.jca.dbr.FLOAT;
 import gov.aps.jca.dbr.INT;
 import gov.aps.jca.dbr.SHORT;
 import gov.aps.jca.dbr.STRING;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A class with factory methods to return {@link PV}s representing Epics Process Variables. The {@link PV}s are lazy
  * meaning they don't connect until the first request through the Channel Access (CA) layer is to be made. The
  * {@link PV}s created are all backed by the {@link EpicsController} singleton.
  * <p>
  * The factory methods are specified by Java type. An underlying Epics type will be chosen based on this table:
  * <li> {@link Double} : {@link DBRType#DOUBLE}
  * <li> {@link Float} : {@link DBRType#FLOAT}
  * <li> {@link Integer} : {@link DBRType#INT}
  * <li> {@link Short} : {@link DBRType#SHORT}
  * <li> {@link Byte} : {@link DBRType#BYTE}
  * <li> {@link String} : {@link DBRType#STRING}
  * <li> {@link Enum} : {@link DBRType#ENUM}
  * <li> {@link Boolean} : {@link DBRType#ENUM}, {@link DBRType#INT} or {@link DBRType#SHORT} For referenence:
  * <p>
  * http://epics.cosylab.com/cosyjava/JCA-Common/Documentation/CAproto.html
  * <p>
  * http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.2
  */
 public class LazyPVFactory {
 
 	private static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
 	
 	public static final String CHECK_CHANNELS_PROPERTY_NAME = "gda.epics.lazypvfactory.check.channels";
 
 	/**
 	 * Envisaged for testing only. EpicsController is a singleton.
 	 * 
 	 * @param controller
 	 *            likely a mock controller!
 	 */
 	public static void setEPICS_CONTROLLER(EpicsController controller) {
 		EPICS_CONTROLLER = controller;
 	}
 
 	public static <E> PV<E> newEnumPV(String pvName, Class<E> enumType) {
 		return new LazyPV<E>(EPICS_CONTROLLER, pvName, enumType);
 	}
 
 	public static PV<Byte> newBytePV(String pvName) {
 		return new LazyPV<Byte>(EPICS_CONTROLLER, pvName, Byte.class);
 	}
 
 	public static PV<Double> newDoublePV(String pvName) {
 		return new LazyPV<Double>(EPICS_CONTROLLER, pvName, Double.class);
 	}
 
 	public static PV<Float> newFloatPV(String pvName) {
 		return new LazyPV<Float>(EPICS_CONTROLLER, pvName, Float.class);
 	}
 
 	public static PV<Integer> newIntegerPV(String pvName) {
 		return new LazyPV<Integer>(EPICS_CONTROLLER, pvName, Integer.class);
 	}
 
 	public static PV<Integer> newIntegerFromEnumPV(String pvName) {
 		LazyPV<Integer> pv = new LazyPV<Integer>(EPICS_CONTROLLER, pvName, Integer.class);
 		pv.setShowTypeMismatchWarnings(false);
 		return pv;
 	}
 
 	public static PV<Short> newShortPV(String pvName) {
 		return new LazyPV<Short>(EPICS_CONTROLLER, pvName, Short.class);
 	}
 	
 	/**
 	 * Create a new String PV that connects an EPICS STRING PV. DBR STRINGs support <40 useful characters.
 	 * @param pvName
 	 * @return the String PV
 	 */
 	public static PV<String> newStringPV(String pvName) {
 		return new LazyPV<String>(EPICS_CONTROLLER, pvName, String.class);
 	}
 
 	public static PV<String> newStringFromWaveformPV(String pvName) {
 		return new StringFromWaveform(newByteArrayPV(pvName));
 	}
 
 	public static PV<Byte[]> newByteArrayPV(String pvName) {
 		return new LazyPV<Byte[]>(EPICS_CONTROLLER, pvName, Byte[].class);
 	}
 
 	public static PV<Double[]> newDoubleArrayPV(String pvName) {
 		return new LazyPV<Double[]>(EPICS_CONTROLLER, pvName, Double[].class);
 	}
 
 	public static PV<Float[]> newFloatArrayPV(String pvName) {
 		return new LazyPV<Float[]>(EPICS_CONTROLLER, pvName, Float[].class);
 	}
 
 	public static PV<Integer[]> newIntegerArrayPV(String pvName) {
 		return new LazyPV<Integer[]>(EPICS_CONTROLLER, pvName, Integer[].class);
 	}
 
 	public static PV<Short[]> newShortArrayPV(String pvName) {
 		return new LazyPV<Short[]>(EPICS_CONTROLLER, pvName, Short[].class);
 	}
 
 	public static PV<Boolean> newBooleanFromDoublePV(String pvName) {
 		return new BooleanFromDouble(new LazyPV<Double>(EPICS_CONTROLLER, pvName, Double.class));
 	}
 
 	public static PV<Boolean> newBooleanFromIntegerPV(String pvName) {
 		return new BooleanFromInteger(new LazyPV<Integer>(EPICS_CONTROLLER, pvName, Integer.class));
 	}
 
 	public static PV<Boolean> newBooleanFromShortPV(String pvName) {
 		return new BooleanFromShort(new LazyPV<Short>(EPICS_CONTROLLER, pvName, Short.class));
 	}
 
 	// NOTE: just uses a short under the covers, so there is no enumType parameter
 	public static PV<Boolean> newBooleanFromEnumPV(String pvName) {
 		LazyPV<Short> pv = new LazyPV<Short>(EPICS_CONTROLLER, pvName, Short.class);
 		pv.setShowTypeMismatchWarnings(false);
 		return new BooleanFromShort(pv);
 	}
 
 	//
 
 	public static <E> NoCallbackPV<E> newNoCallbackEnumPV(String pvName, Class<E> enumType) {
 		return new NoCallback<E>(newEnumPV(pvName, enumType));
 	}
 
 	public static NoCallbackPV<Byte> newNoCallbackBytePV(String pvName) {
 		return new NoCallback<Byte>(newBytePV(pvName));
 	}
 
 	public static NoCallbackPV<Double> newNoCallbackDoublePV(String pvName) {
 		return new NoCallback<Double>(newDoublePV(pvName));
 	}
 
 	public static NoCallbackPV<Float> newNoCallbackFloatPV(String pvName) {
 		return new NoCallback<Float>(newFloatPV(pvName));
 	}
 
 	public static NoCallbackPV<Integer> newNoCallbackIntegerPV(String pvName) {
 		return new NoCallback<Integer>(newIntegerPV(pvName));
 	}
 
 	public static NoCallbackPV<Short> newNoCallbackShortPV(String pvName) {
 		return new NoCallback<Short>(newShortPV(pvName));
 	}
 
 	/**
 	 * Create a new String PV that connects an EPICS STRING PV. DBR STRINGs support <40 useful characters.
 	 * @param pvName
 	 * @return the String PV
 	 */
 	public static NoCallbackPV<String> newNoCallbackStringPV(String pvName) {
 		return new NoCallback<String>(newStringPV(pvName));
 	}
 
 	public static NoCallbackPV<String> newNoCallbackStringFromWaveformPV(String pvName) {
 		return new NoCallback<String>(newStringFromWaveformPV(pvName));
 	}
 
 	public static NoCallbackPV<Byte[]> newNoCallbackByteArrayPV(String pvName) {
 		return new NoCallback<Byte[]>(newByteArrayPV(pvName));
 	}
 
 	public static NoCallbackPV<Double[]> newNoCallbackDoubleArrayPV(String pvName) {
 		return new NoCallback<Double[]>(newDoubleArrayPV(pvName));
 	}
 
 	public static NoCallbackPV<Float[]> newNoCallbackFloatArrayPV(String pvName) {
 		return new NoCallback<Float[]>(newFloatArrayPV(pvName));
 	}
 
 	public static NoCallbackPV<Integer[]> newNoCallbackIntegerArrayPV(String pvName) {
 		return new NoCallback<Integer[]>(newIntegerArrayPV(pvName));
 	}
 
 	public static NoCallbackPV<Short[]> newNoCallbackShortArrayPV(String pvName) {
 		return new NoCallback<Short[]>(newShortArrayPV(pvName));
 	}
 
 	public static NoCallbackPV<Boolean> newNoCallbackBooleanFromIntegerPV(String pvName) {
 		return new NoCallback<Boolean>(newBooleanFromIntegerPV(pvName));
 	}
 
 	public static NoCallbackPV<Boolean> newNoCallbackBooleanFromShortPV(String pvName) {
 		return new NoCallback<Boolean>(newBooleanFromShortPV(pvName));
 	}
 
 	//
 
 	public static <E> ReadOnlyPV<E> newReadOnlyEnumPV(String pvName, Class<E> enumType) {
 		return new ReadOnly<E>(newEnumPV(pvName, enumType));
 	}
 
 	public static ReadOnlyPV<Byte> newReadOnlyBytePV(String pvName) {
 		return new ReadOnly<Byte>(newBytePV(pvName));
 	}
 
 	public static ReadOnlyPV<Double> newReadOnlyDoublePV(String pvName) {
 		return new ReadOnly<Double>(newDoublePV(pvName));
 	}
 
 	public static ReadOnlyPV<Float> newReadOnlyFloatPV(String pvName) {
 		return new ReadOnly<Float>(newFloatPV(pvName));
 	}
 
 	public static ReadOnlyPV<Integer> newReadOnlyIntegerPV(String pvName) {
 		return new ReadOnly<Integer>(newIntegerPV(pvName));
 	}
 
 	public static ReadOnlyPV<Integer> newReadOnlyIntegerFromEnumPV(String pvName) {
 		final PV<Integer> pv = newIntegerFromEnumPV(pvName);
 		return new ReadOnly<Integer>(pv);
 	}
 
 	public static ReadOnlyPV<Short> newReadOnlyShortPV(String pvName) {
 		return new ReadOnly<Short>(newShortPV(pvName));
 	}
 
 	/**
 	 * Create a new String PV that connects an EPICS STRING PV. DBR STRINGs support <40 useful characters.
 	 * @param pvName
 	 * @return the String PV
 	 */
 	public static ReadOnlyPV<String> newReadOnlyStringPV(String pvName) {
 		return new ReadOnly<String>(newStringPV(pvName));
 	}
 
 	public static ReadOnlyPV<String> newReadOnlyStringFromWaveformPV(String pvName) {
 		return new ReadOnly<String>(newStringFromWaveformPV(pvName));
 	}
 
 	public static ReadOnlyPV<Byte[]> newReadOnlyByteArrayPV(String pvName) {
 		return new ReadOnly<Byte[]>(newByteArrayPV(pvName));
 	}
 
 	public static ReadOnlyPV<Double[]> newReadOnlyDoubleArrayPV(String pvName) {
 		return new ReadOnly<Double[]>(newDoubleArrayPV(pvName));
 	}
 
 	public static ReadOnlyPV<Float[]> newReadOnlyFloatArrayPV(String pvName) {
 		return new ReadOnly<Float[]>(newFloatArrayPV(pvName));
 	}
 
 	public static ReadOnlyPV<Integer[]> newReadOnlyIntegerArrayPV(String pvName) {
 		return new ReadOnly<Integer[]>(newIntegerArrayPV(pvName));
 	}
 
 	public static ReadOnlyPV<Short[]> newReadOnlyShortArrayPV(String pvName) {
 		return new ReadOnly<Short[]>(newShortArrayPV(pvName));
 	}
 
 	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromIntegerPV(String pvName) {
 		return new ReadOnly<Boolean>(newBooleanFromIntegerPV(pvName));
 	}
 
 	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromShortPV(String pvName) {
 		return new ReadOnly<Boolean>(newBooleanFromShortPV(pvName));
 	}
 
 	// NOTE: just uses a short under the covers, so there is no enumType parameter.
 	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromEnumPV(String pvName) {
 		return new ReadOnly<Boolean>(newBooleanFromEnumPV(pvName));
 	}
 
 	public static ReadOnlyPV<Boolean> newReadOnlyBooleanFromDoublePV(String pvName) {
 		return new ReadOnly<Boolean>(newBooleanFromDoublePV(pvName));
 	}
 
 	static private class LazyPV<T> implements PV<T> {
 
 		private static final Logger logger = LoggerFactory.getLogger(LazyPV.class);
 
 		static Map<Class<?>, DBRType> javaTypeToDBRType;
 
 		static {
 
 			javaTypeToDBRType = new HashMap<Class<?>, DBRType>();
 
 			javaTypeToDBRType.put(Double.class, DBRType.DOUBLE);
 
 			javaTypeToDBRType.put(Float.class, DBRType.FLOAT);
 
 			javaTypeToDBRType.put(Integer.class, DBRType.INT);
 
 			javaTypeToDBRType.put(Short.class, DBRType.SHORT);
 
 			javaTypeToDBRType.put(Byte.class, DBRType.BYTE);
 
 			javaTypeToDBRType.put(String.class, DBRType.STRING);
 		}
 
 		private final EpicsController controller;
 
 		private final String pvName;
 
 		private final Class<T> javaType;
 
 		private final DBRType dbrType;
 
 		private Channel channel; // created only when first accessed
 
 		private T lastMonitoredValue;
 
 		private Object lastMonitoredValueMonitor = new Object();
 
 		private Map<MonitorListener, Monitor> monitors = new HashMap<MonitorListener, Monitor>();
 
 		private ValueMonitorListener valueMonitorListener; // presence indicates monitoring
 
 		private PutCallbackListener putCallbackListener = new NullPutCallbackListener();
 
 		private Object putCallbackGuard = new Object();
 
 		private PVMonitor<T> observableMonitor;
 
 		private boolean showTypeMismatchWarnings = true;
 		
 		LazyPV(EpicsController controller, String pvName, Class<T> javaType) {
 			this.controller = controller;
 			this.pvName = pvName;
 			this.javaType = javaType;
 			if (javaType.isEnum()) {
 				dbrType = DBRType.ENUM;
 			} else if (javaType.isArray()) {
 				dbrType = javaTypeToDBRType.get(javaType.getComponentType());
 			} else {
 				dbrType = javaTypeToDBRType.get(javaType);
 			}
 			if (LocalProperties.check(CHECK_CHANNELS_PROPERTY_NAME)) {
 				logger.warn("Checking channel : '" + pvName + "'");
 				try {
 					Channel temp = (controller.createChannel(pvName));
 					controller.destroy(temp);
 					temp=null;
 				} catch (Exception e) {
 					logger.error("Could not connect to channel  : '" + pvName + "'", e);
 				}	
 			}
 		}
 		
 		void setShowTypeMismatchWarnings(boolean showTypeMismatchWarnings) {
 			this.showTypeMismatchWarnings = showTypeMismatchWarnings;
 		}
 		
 		@Override
 		public String toString() {
 			return MessageFormat.format("LazyPV({0}, {1})", pvName, javaType.getSimpleName());
 		}
 		
 		private double defaultTimeout() {
 			return EpicsGlobals.getTimeout();
 		}
 
 		@Override
 		public String getPvName() {
 			return pvName;
 		}
 
 		@Override
 		public T get() throws IOException {
 			T value = extractValueFromDbr(getDBR(dbrType));
 			logger.debug("'{}' get() <-- {}", pvName, value);
 			return value;
 		}
 
 		@Override
 		public T get(int numElements) throws IOException {
 			T value = extractValueFromDbr(getDBR(dbrType, numElements));
 			logger.debug("'{}' get() <-- {}", pvName, value);
 			return value;
 		}
 		
 		@Override
 		public T getLast() throws IOException {
 			if (!isValueMonitoring()) {
				throw new IllegalStateException("Cannot get the last value of " + getPvName()
						+ " as this LazyPV is not set to monitor values");
 			}
 
 			initialiseLastMonitoredValue();
 			logger.debug("'{}' get() <-- {} (via monitor)", pvName, lastMonitoredValue);
 			return lastMonitoredValue;
 		}
 
 		@Override
 		public T waitForValue(Predicate<T> predicate, double timeoutS) throws IOException, IllegalStateException,
 				java.util.concurrent.TimeoutException {
 			logger.debug("'{}' waiting for value '{}'", pvName, predicate.toString());
 			if (!isValueMonitoring()) {
 				this.setValueMonitoring(true);
 			}
 
 			initialiseLastMonitoredValue();
 
 			synchronized (lastMonitoredValueMonitor) {
 
 				if (timeoutS <= 0) {
 					// wait indefinitely
 					while (!predicate.apply(lastMonitoredValue)) {
 						try {
 							lastMonitoredValueMonitor.wait();
 						} catch (InterruptedException e) {
 							throw new InterruptedIOException();
 						}
 					}
 				} else {
 					// wait for timeoutS seconds
 					long deadline = System.currentTimeMillis() + ((long) (timeoutS * 1000));
 
 					while (!predicate.apply(lastMonitoredValue)) {
 						long remaining = deadline - System.currentTimeMillis();
 						System.out.print("deadline: " + deadline + " remaining: " + remaining + "\n");
 						if (remaining <= 0) {
 							String msg = "The requested value {0} was not observed from the PV {1} within the specified timeout of {2}s";
 							throw new java.util.concurrent.TimeoutException(format(msg, predicate.toString(),
 									getPvName(), timeoutS));
 						}
 						try {
 							lastMonitoredValueMonitor.wait(remaining);
 						} catch (InterruptedException e) {
 							throw new InterruptedIOException();
 						}
 					}
 				}
 
 				logger.debug("'{}' waitForValue() <-- {} (after waiting for a value via monitor)", pvName,
 						lastMonitoredValue);
 				return lastMonitoredValue;
 			}
 		}
 
 		/**
 		 * If no value monitored yet; get value across CA
 		 */
 		private void initialiseLastMonitoredValue() throws IOException {
 			if (lastMonitoredValue == null) {
 				logger.debug("No monitors received from '{}', retrieving value across CA", pvName);
 				setLastValueFromMonitor(extractValueFromDbr(getDBR(dbrType)));
 			}
 		}
 
 		private void setLastValueFromMonitor(T lastValueFromMonitor) {
 			synchronized (lastMonitoredValueMonitor) {
 				this.lastMonitoredValue = lastValueFromMonitor;
 				this.lastMonitoredValueMonitor.notifyAll();
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		@Override
 		public T extractValueFromDbr(DBR dbr) {
 
 			if (javaType == Byte.class) {
 				return (T) toObject(((BYTE) dbr).getByteValue())[0];
 			}
 
 			if (javaType == Double.class) {
 				return (T) toObject(((DOUBLE) dbr).getDoubleValue())[0];
 			}
 
 			if (javaType == Float.class) {
 				return (T) toObject(((FLOAT) dbr).getFloatValue())[0];
 			}
 
 			if (javaType == Integer.class) {
 				return (T) toObject(((INT) dbr).getIntValue())[0];
 			}
 
 			if (javaType == Short.class) {
 				return (T) toObject(((SHORT) dbr).getShortValue())[0];
 			}
 
 			if (javaType.isEnum()) {
 				short s = ((ENUM) dbr).getEnumValue()[0];
 				return javaType.getEnumConstants()[s];
 			}
 
 			if (javaType == String.class) {
 				return (T) ((STRING) dbr).getStringValue()[0];
 			}
 			if (javaType == Byte[].class) {
 				return (T) toObject(((BYTE) dbr).getByteValue());
 			}
 
 			if (javaType == Double[].class) {
 				return (T) toObject(((DOUBLE) dbr).getDoubleValue());
 			}
 
 			if (javaType == Float[].class) {
 				return (T) toObject(((FLOAT) dbr).getFloatValue());
 			}
 
 			if (javaType == Integer[].class) {
 				return (T) toObject(((INT) dbr).getIntValue());
 			}
 
 			if (javaType == Short[].class) {
 				return (T) toObject(((SHORT) dbr).getShortValue());
 			}
 
 			if (javaType == String[].class) {
 				return (T) ((STRING) dbr).getStringValue();
 			}
 
 			throw new IllegalStateException("Unexpected type configured");
 
 		}
 
 		@Override
 		public synchronized void setValueMonitoring(boolean shouldMonitor) throws IOException {
 
 			if (shouldMonitor && valueMonitorListener == null) {
 				// start monitoring
 				logger.info("Configuring constant monitoring of pv '{}'", pvName);
 				valueMonitorListener = new ValueMonitorListener();
 				addMonitorListener(valueMonitorListener);
 			}
 
 			else if (!shouldMonitor & valueMonitorListener != null) {
 				// stop monitoring
 				logger.info("Disabling constant monitoring of pv '{}'", pvName);
 				removeMonitorListener(valueMonitorListener);
 				valueMonitorListener = null;
 			}
 
 		}
 
 		@Override
 		public boolean isValueMonitoring() {
 			return (valueMonitorListener != null);
 		}
 
 		@Override
 		public void addMonitorListener(MonitorListener listener) throws IOException {
 
 			logger.info("Adding MonitorListener '{}' to pv '{}' ", listener.getClass().getName(), pvName);
 			Monitor monitor;
 			try {
 				monitor = controller.setMonitor(getChannel(), dbrType, Monitor.VALUE, listener);
 			} catch (Exception e) {
 				throw new IOException("Could not add monitor listener to PV '" + getPvName() + "'", e);
 			}
 			monitors.put(listener, monitor);
 		}
 
 		@Override
 		public void removeMonitorListener(MonitorListener listener) {
 			logger.info("Removing MonitorListener '{}' from pv '{}' ", listener.getClass().getName(), pvName);
 			Monitor monitor = monitors.remove(listener);
 			//protect against invalid listener
 			if( monitor != null)
 				controller.clearMonitor(monitor);
 		}
 
 		protected synchronized Channel getChannel() throws IOException {
 			if (channel == null) {
 				try {
 					channel = (controller.createChannel(pvName));
 					if (showTypeMismatchWarnings && channel.getFieldType() != dbrType) {
 						logger.warn(format(
 								"The pv ''{0}'' was expecting a channel of DBR type {1}, but the channel was discovered at runtime to be of DBR type {2}",
 								pvName, javaType, channel.getFieldType()));
 					}
 				} catch (CAException e) {
 					throw new IOException("Epics problem creating channel for pv '" + pvName + "'", e);
 				} catch (gov.aps.jca.TimeoutException e) {
 					throw new IOException("Timed out creating channel for pv '" + pvName + "'", e);
 				}
 			}
 			return channel;
 		}
 
 		private synchronized DBR getDBR(DBRType dbrType) throws IOException {
 
 			try {
 				return controller.getDBR(getChannel(), dbrType);
 			} catch (CAException e) {
 				throw new IOException("Problem getting value from Epics pv '" + pvName + "'", e);
 			} catch (TimeoutException e) {
 				throw new IOException("Timed out getting value from Epics pv '" + pvName + "'", e);
 			} catch (InterruptedException e) {
 				throw new InterruptedIOException("Interupted while getting value from Epics pv '" + pvName + "'");
 			}
 		}
 
 		private synchronized DBR getDBR(DBRType dbrType, int numElements) throws IOException {
 
 			try {
 				return controller.getDBR(getChannel(), dbrType, numElements);
 			} catch (CAException e) {
 				throw new IOException("Problem getting value from Epics pv '" + pvName + "'", e);
 			} catch (TimeoutException e) {
 				throw new IOException("Timed out getting value from Epics pv '" + pvName + "'", e);
 			} catch (InterruptedException e) {
 				throw new InterruptedIOException("Interupted while getting value from Epics pv '" + pvName + "'");
 			}
 		}
 		
 		private class ValueMonitorListener implements MonitorListener {
 
 			@Override
 			public void monitorChanged(MonitorEvent ev) {
 				DBR dbr = ev.getDBR();
 				if(dbr != null){
 					setLastValueFromMonitor(extractValueFromDbr(dbr));
 					logger.debug("'{}' <-- {}  (monitored value changed)", pvName, lastMonitoredValue);
 				}
 			}
 		}
 
 		// NoCallbackPV
 
 		@Override
 		public void putNoWait(T value) throws IOException {
 
 			logger.debug("'{}' put() --> {}", pvName, value);
 
 			try {
 
 				if (javaType == Byte[].class) {
 					controller.caput(getChannel(), toPrimitive((Byte[]) value));
 				} else if (javaType == Double[].class) {
 					controller.caput(getChannel(), toPrimitive((Double[]) value));
 				} else if (javaType == Float[].class) {
 					controller.caput(getChannel(), toPrimitive((Float[]) value));
 				} else if (javaType == Integer[].class) {
 					controller.caput(getChannel(), toPrimitive((Integer[]) value));
 				} else if (javaType == Short[].class) {
 					controller.caput(getChannel(), toPrimitive((Short[]) value));
 				} else if (javaType == String[].class) {
 					throw new IllegalStateException("String[] not supported");
 				} else if (javaType == Byte.class) {
 					controller.caput(getChannel(), (Byte) value);
 				} else if (javaType == Double.class) {
 					controller.caput(getChannel(), (Double) value);
 				} else if (javaType == Float.class) {
 					controller.caput(getChannel(), (Float) value);
 				} else if (javaType == Integer.class) {
 					controller.caput(getChannel(), (Integer) value);
 				} else if (javaType == Short.class) {
 					controller.caput(getChannel(), (Short) value);
 				} else if (javaType == String.class) {
 					controller.caput(getChannel(), (String) value);
 				} else if (javaType.isEnum()) {
 					controller.caput(getChannel(), ((Enum<?>) value).ordinal());
 				} else {
 					throw new IllegalStateException("Unexpected type configured");
 				}
 			
 			} catch (InterruptedException e) {
 				throw new InterruptedIOException(format(
 						"Interupted while putting value to EPICS pv ''{0}'', (value was: {1})", pvName, value));
 			} catch (Exception e) {
 				throw new IOException(format("Problem putting value to EPICS pv ''{0}'', (value was: {1})", pvName,
 						value), e);
 			}
 
 		}
 
 		@Override
 		public void putNoWait(T value, PutListener pl) throws IOException {
 
 			logger.debug("'{}' put() --> {}, with listener '{}'",
 					new Object[] { pvName, value, pl.getClass().getName() });
 
 			try {
 
 				if (javaType == Byte[].class) {
 					controller.caput(getChannel(), toPrimitive((Byte[]) value), pl);
 				} else if (javaType == Double[].class) {
 					controller.caput(getChannel(), toPrimitive((Double[]) value), pl);
 				} else if (javaType == Float[].class) {
 					controller.caput(getChannel(), toPrimitive((Float[]) value), pl);
 				} else if (javaType == Integer[].class) {
 					controller.caput(getChannel(), toPrimitive((Integer[]) value), pl);
 				} else if (javaType == Short[].class) {
 					controller.caput(getChannel(), toPrimitive((Short[]) value), pl);
 				} else if (javaType == String[].class) {
 					throw new IllegalStateException("String[] not supported");
 				} else if (javaType == Byte.class) {
 					controller.caput(getChannel(), (Byte) value, pl);
 				} else if (javaType == Double.class) {
 					controller.caput(getChannel(), (Double) value, pl);
 				} else if (javaType == Float.class) {
 					controller.caput(getChannel(), (Float) value, pl);
 				} else if (javaType == Integer.class) {
 					controller.caput(getChannel(), (Integer) value, pl);
 				} else if (javaType == Short.class) {
 					controller.caput(getChannel(), (Short) value, pl);
 				} else if (javaType == String.class) {
 					controller.caput(getChannel(), (String) value, pl);
 				} else if (javaType.isEnum()) {
 					controller.caput(getChannel(), ((Enum<?>) value).ordinal(), pl);
 				} else {
 					throw new IllegalStateException("Unexpected type configured");
 				}
 			} catch (InterruptedException e) {
 				throw new InterruptedIOException(format(
 						"Interupted while putting (with listener) value to EPICS pv ''{0}'', (value was: {1})", pvName, value));
 			} catch (Exception e) {
 				throw new IOException(format("Problem putting (with listener)  value to EPICS pv ''{0}'', (value was: {1})", pvName,
 						value), e);
 			}
 		}
 
 		// PV (with callback)
 
 		@Override
 		public void putWait(T value) throws IOException {
 			putWait(value, defaultTimeout());
 		}
 
 		@Override
 		public void putWait(T value, double timeoutS) throws IOException {
 			putAsyncStart(value);
 			putAsyncWait(timeoutS);
 		}
 
 		@Override
 		public void putAsyncStart(T value) throws IllegalStateException, IOException {
 			synchronized (putCallbackGuard) {
 				if (putCallbackListener.isCallbackPending()) {
 					throw new IllegalStateException("The pv " + getPvName()
 							+ " is waiting to complete a startPutCallback already");
 				}
 				putCallbackListener = new PutCallbackListener();
 				try {
 					putNoWait(value, putCallbackListener);
 				} catch (IllegalStateException e) {
 					putCallbackListener.cancelPendingCallback();
 					throw e;
 				} catch (IOException e) {
 					putCallbackListener.cancelPendingCallback();
 					throw e;
 			}
 			}
 		}
 
 		@Override
 		public void putAsyncWait() throws IOException {
 			putAsyncWait(defaultTimeout());
 		}
 
 		@Override
 		public boolean putAsyncIsWaiting() {
 			return putCallbackListener.isCallbackPending();
 		}
 		
 		@Override
 		public void putAsyncCancel() {
 			if (putAsyncIsWaiting()) {
 				logger.info("Cancelling pending callback on the pv " + getPvName());
 			}
 			putCallbackListener.cancelPendingCallback();
 		}
 		
 		@Override
 		public void putAsyncWait(double timeoutS) throws IOException {
 			synchronized (putCallbackGuard) {
 				try {
 					putCallbackListener.waitForCallback(timeoutS);
 				} catch (CAStatusException e) {
 					throw new IOException("Epics problem waiting for callback from PV " + getPvName(), e);
 				} catch (TimeoutException e) {
 					throw new IOException("Timed out waiting for callback from PV " + getPvName());
 				} catch (InterruptedException e) {
 					throw new InterruptedIOException("Interupted while waiting for callback from PV " + getPvName());
 				} finally {
 					putCallbackListener = new NullPutCallbackListener();
 				}
 			}
 		}
 
 		/**
 		 * NOTE: This is a temporary solution that DOES not implement the interface as specified. Instead it gets the
 		 * values of each PV specified in toReturn just AFTER the callback returns.
 		 */
 		@Override
 		public PVValues putWait(T value, ReadOnlyPV<?>... toReturn) throws IOException {
 			return putWait(value, defaultTimeout(), toReturn);
 		}
 
 		/**
 		 * NOTE: This is a temporary solution that DOES not implement the interface as specified. Instead it gets the
 		 * values of each PV specified in toReturn just AFTER the callback returns.
 		 */
 		@Override
 		public PVValues putWait(T value, double timeoutS, ReadOnlyPV<?>... toReturn) throws IOException {
 			putWait(value);
 			CallbackResult result = new CallbackResult();
 			for (ReadOnlyPV<?> pv : toReturn) {
 				result.put(pv, pv.get());
 			}
 			return result;
 		}
 
 		private class PutCallbackListener implements PutListener {
 
 			private volatile PutEvent event;
 
 			private volatile boolean callbackPending = true;
 
 			private volatile Object eventMonitor = new Object();
 
 			@Override
 			public void putCompleted(PutEvent ev) {
 				synchronized (eventMonitor) {
 					event = ev;
 					callbackPending = false;
 					eventMonitor.notifyAll();
 				}
 			}
 
 			public void waitForCallback(double timeoutS) throws TimeoutException, CAStatusException, InterruptedException {
 				synchronized (eventMonitor) {
 					if (callbackPending) {
 						eventMonitor.wait((long) (timeoutS * 1000));
 					}
 					if (event == null) {
 						throw new TimeoutException("putWait timed out after " + timeoutS + "s");
 					}
 					if (event.getStatus() != CAStatus.NORMAL) {
 						throw new CAStatusException(event.getStatus(), "putWait failed");
 					}
 				}
 			}
 
 			public boolean isCallbackPending() {
 				return callbackPending;
 			}
 			
 			public void cancelPendingCallback() {
 				synchronized (eventMonitor) {
 					callbackPending = false;
 					eventMonitor.notifyAll();
 				}
 			}
 
 		}
 
 		private class NullPutCallbackListener extends PutCallbackListener {
 
 			@Override
 			public synchronized void waitForCallback(double timeoutS) throws TimeoutException {
 				// just return
 			}
 
 			@Override
 			public synchronized boolean isCallbackPending() {
 				return false;
 			}
 		}
 
 		class CallbackResult implements PVValues {
 
 			Map<ReadOnlyPV<?>, Object> resultsMap = new HashMap<ReadOnlyPV<?>, Object>();
 
 			<N> void put(ReadOnlyPV<?> pv, Object value) {
 				resultsMap.put(pv, value);
 
 			}
 
 			@SuppressWarnings("unchecked")
 			@Override
 			public <N> N get(ReadOnlyPV<N> pv) {
 				Object value = resultsMap.get(pv);
 				if (value == null) {
 					throw new IllegalArgumentException("There is no result for the PV " + getPvName());
 				}
 				return (N) value;
 			}
 
 		}
 
 		/**
 		 * When adding an observer we use the PVMonitor object to
 		 * add the monitor listener and notify the observer.
 		 * 
 		 */
 		@Override
 		public void addObserver(Observer<T> observer) throws Exception {
 			if( observableMonitor == null){
 				observableMonitor = new PVMonitor<T>(this, this);
 			}
 			observableMonitor.addObserver(observer);
 		}
 
 		@Override
 		public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
 			if( observableMonitor == null){
 				observableMonitor = new PVMonitor<T>(this, this);
 			}
 			observableMonitor.addObserver(observer, predicate);
 		}
 
 
 		@Override
 		public void removeObserver(Observer<T>  observer) {
 			if( observableMonitor == null){
 				return;
 			}
 			observableMonitor.removeObserver(observer);
 		}
 
 	}
 
 	static abstract private class AbstractReadOnlyAdapter<N, T> implements ReadOnlyPV<T> {
 
 		Observable<T> obs = null;
 		
 		abstract protected T innerToOuter(N innerValue);
 
 		abstract protected N outerToInner(T outerValue);
 
 		private final ReadOnlyPV<N> ropv;
 
 		public AbstractReadOnlyAdapter(PV<N> pv) {
 			this.ropv = pv;
 		}
 
 		ReadOnlyPV<N> getPV() {
 			return ropv;
 		}
 
 		private Predicate<N> newInnerPredicate(Predicate<T> outerPredicate) {
 			return new InnerFromOuterPredicate(outerPredicate);
 		}
 
 		//
 
 		@Override
 		public T get() throws IOException {
 			return innerToOuter(getPV().get());
 		}
 
 		@Override
 		public T get(int numElements) throws IOException {
 			return innerToOuter(getPV().get(numElements));
 		}
 
 		@Override
 		public T getLast() throws IOException {
 			return innerToOuter(getPV().getLast());
 		}
 
 		@Override
 		public T waitForValue(Predicate<T> predicate, double timeoutS) throws IOException, IllegalStateException, java.util.concurrent.TimeoutException {
 			N innerValue = getPV().waitForValue(newInnerPredicate(predicate), timeoutS);
 			return innerToOuter(innerValue);
 		}
 
 		@Override
 		public T extractValueFromDbr(DBR dbr) {
 			return innerToOuter(getPV().extractValueFromDbr(dbr));
 		}
 
 		//
 
 		@Override
 		public String getPvName() {
 			return getPV().getPvName();
 		}
 
 		@Override
 		public void setValueMonitoring(boolean shouldMonitor) throws IOException {
 			getPV().setValueMonitoring(shouldMonitor);
 		}
 
 		@Override
 		public boolean isValueMonitoring() {
 			return getPV().isValueMonitoring();
 		}
 
 		@Override
 		public void addMonitorListener(MonitorListener listener) throws IOException {
 			getPV().addMonitorListener(listener);
 		}
 
 		@Override
 		public void removeMonitorListener(MonitorListener listener) {
 			getPV().removeMonitorListener(listener);
 		}
 
 		private class InnerFromOuterPredicate implements Predicate<N> {
 
 			private final Predicate<T> outerPredicate;
 
 			public InnerFromOuterPredicate(Predicate<T> outerPredicate) {
 				this.outerPredicate = outerPredicate;
 			}
 
 			@Override
 			public boolean apply(N innerObject) {
 				return outerPredicate.apply(innerToOuter(innerObject));
 			}
 
 		}
 
 		/**
 		 * As the Observer may be of a different type e.g. String to the
 		 * Observable e.g. Byte[] we need to adapter.
 		 * 
 		 **/
 		private class GenericObservable implements Observable<T> {
 
 			private final Observable<N> stringFromWaveform;
 			
 			ObservableUtil<T> oc = new ObservableUtil<T>();
 			
 			private Observer<N> observerN;
 
 			public GenericObservable(Observable<N> stringFromWaveform) throws Exception {
 				this.stringFromWaveform = stringFromWaveform;
 				observerN = new Observer<N>() {
 					@Override
 					public void update(Observable<N> source, N arg) {
 						oc.notifyIObservers(AbstractReadOnlyAdapter.this, innerToOuter(arg));
 					}
 				};
 				stringFromWaveform.addObserver(observerN);
 			}
 
 			@Override
 			public void addObserver(Observer<T> observer) throws Exception {
 				oc.addObserver(observer);
 			}
 			
 			@Override
 			public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
 				oc.addObserver(observer, predicate);
 			}
 
 			@Override
 			public void removeObserver(Observer<T> observer) {
 				oc.removeObserver(observer);
 				if( !oc.IsBeingObserved())
 					stringFromWaveform.removeObserver(observerN);
 			}
 
 		}
 
 		@Override
 		public void addObserver(final Observer<T> observer) throws Exception {
 			createStringObservable(getPV()).addObserver(observer);
 		}
 		
 		@Override
 		public void addObserver(final Observer<T> observer, Predicate<T> predicate) throws Exception {
 			createStringObservable(getPV()).addObserver(observer, predicate);
 		}
 		
 		private Observable<T> createStringObservable(ReadOnlyPV<N> pv) throws Exception {
 			if( obs== null){
 				obs = new GenericObservable(pv);
 			}
 			return obs;
 		}
 
 		@Override
 		public void removeObserver(Observer<T> observer) {
 			if( obs == null)
 				return;
 			obs.removeObserver(observer);
 		}
 
 	}
 
 	static abstract private class AbstractPVAdapter<N, T> extends AbstractReadOnlyAdapter<N, T> implements PV<T> {
 
 		private final PV<N> pv;
 
 		public AbstractPVAdapter(PV<N> pv) {
 			super(pv);
 			this.pv = pv;
 		}
 
 		@Override
 		PV<N> getPV() {
 			return pv;
 		}
 
 		@Override
 		public void putNoWait(T value) throws IOException {
 			getPV().putNoWait(outerToInner(value));
 		}
 
 		@Override
 		public void putNoWait(T value, PutListener pl) throws IOException {
 			getPV().putNoWait(outerToInner(value), pl);
 		}
 
 		@Override
 		public void putWait(T value) throws IOException {
 			getPV().putWait(outerToInner(value));
 		}
 
 		@Override
 		public void putWait(T value, double timeoutS) throws IOException {
 			getPV().putWait(outerToInner(value), timeoutS);
 		}
 
 		@Override
 		public void putAsyncStart(T value) throws IOException {
 			getPV().putAsyncStart(outerToInner(value));
 		}
 
 		@Override
 		public void putAsyncWait() throws IOException {
 			getPV().putAsyncWait();
 		}
 
 		@Override
 		public void putAsyncWait(double timeoutS) throws IOException {
 			getPV().putAsyncWait(timeoutS);
 		}
 
 		@Override
 		public boolean putAsyncIsWaiting() {
 			return getPV().putAsyncIsWaiting();
 		}
 		
 		@Override
 		public void putAsyncCancel() {
 			getPV().putAsyncCancel();
 		}
 		
 		@Override
 		public PVValues putWait(T value, ReadOnlyPV<?>... toReturn) throws IOException {
 			return getPV().putWait(outerToInner(value), toReturn);
 		}
 
 		@Override
 		public PVValues putWait(T value, double timeoutS, ReadOnlyPV<?>... toReturn) throws IOException {
 			return getPV().putWait(outerToInner(value), timeoutS, toReturn);
 		}
 
 	}
 
 	static private class ReadOnly<T> extends AbstractReadOnlyAdapter<T, T> {
 
 		public ReadOnly(PV<T> pv) {
 			super(pv);
 		}
 		
 		@Override
 		public String toString() {
 			return MessageFormat.format("ReadOnly({0})", getPV().toString());
 		}
 
 		@Override
 		protected T innerToOuter(T innerValue) {
 			return innerValue;
 		}
 
 		@Override
 		protected T outerToInner(T outerValue) {
 			return outerValue;
 		}
 		
 		@Override
 		public void addObserver(Observer<T> observer) throws Exception {
 			getPV().addObserver(observer);
 		}
 		
 		@Override
 		public void addObserver(Observer<T> observer, Predicate<T> predicate) throws Exception {
 			getPV().addObserver(observer, predicate);
 		}
 
 		@Override
 		public void removeObserver(Observer<T> observer) {
 			getPV().removeObserver(observer);
 		}
 
 	}
 
 	static private class NoCallback<T> extends ReadOnly<T> implements NoCallbackPV<T> {
 
 		private NoCallbackPV<T> pv;
 
 		public NoCallback(PV<T> pv) {
 			super(pv);
 			this.pv = pv;
 		}
 
 		@Override
 		public String toString() {
 			return MessageFormat.format("NoCallback({0})", getPV().toString());
 		}
 		
 		@Override
 		NoCallbackPV<T> getPV() {
 			return pv;
 		}
 
 		@Override
 		public void putNoWait(T value) throws IOException {
 			getPV().putNoWait(value);
 		}
 
 		@Override
 		public void putNoWait(T value, PutListener pl) throws IOException {
 			getPV().putNoWait(value, pl);
 		}
 
 	}
 
 	static private class StringFromWaveform extends AbstractPVAdapter<Byte[], String> implements PV<String> {
 
 		private StringFromWaveform(PV<Byte[]> byteArrayPV) {
 			super(byteArrayPV);
 		}
 
 		@Override
 		public String toString() {
 			return MessageFormat.format("StringFromWaveform({0})", getPV().toString());
 		}
 		
 		@Override
 		protected String innerToOuter(Byte[] innerValue) {
 			return new String(toPrimitive(innerValue)).trim();
 		}
 
 		@Override
 		protected Byte[] outerToInner(String outerValue) {
 			return toObject(((outerValue + '\0').getBytes()));
 		}
 
 	}
 
 	static private class BooleanFromInteger extends AbstractPVAdapter<Integer, Boolean> implements PV<Boolean> {
 
 		private BooleanFromInteger(LazyPV<Integer> pv) {
 			super(pv);
 		}
 
 		@Override
 		public String toString() {
 			return MessageFormat.format("BooleanFromInteger({0})", getPV().toString());
 		}
 		
 		@Override
 		protected Boolean innerToOuter(Integer innerValue) {
 			return innerValue > 0;
 		}
 
 		@Override
 		protected Integer outerToInner(Boolean outerValue) {
 			return (outerValue ? 1 : 0);
 		}
 
 	}
 
 	static private class BooleanFromShort extends AbstractPVAdapter<Short, Boolean> implements PV<Boolean> {
 
 		private BooleanFromShort(LazyPV<Short> pv) {
 			super(pv);
 		}
 		
 		@Override
 		public String toString() {
 			return MessageFormat.format("BooleanFromShort({0})", getPV().toString());
 		}
 		
 		@Override
 		protected Boolean innerToOuter(Short innerValue) {
 			return innerValue > 0;
 		}
 
 		@Override
 		protected Short outerToInner(Boolean outerValue) {
 			return (short) (outerValue ? 1 : 0);
 		}
 		
 	}
 
 	static private class BooleanFromDouble extends AbstractPVAdapter<Double, Boolean> implements PV<Boolean> {
 
 		private BooleanFromDouble(LazyPV<Double> pv) {
 			super(pv);
 		}
 
 		@Override
 		public String toString() {
 			return MessageFormat.format("BooleanFromDouble({0})", getPV().toString());
 		}
 		
 		@Override
 		protected Boolean innerToOuter(Double innerValue) {
 			return innerValue > 0;
 		}
 
 		@Override
 		protected Double outerToInner(Boolean outerValue) {
 			return (outerValue ? 1.0 : 0.0);
 		}
 	}
 }
 
 /**
  * Class to adapt between a monitor and the gda IObserver system
  *
  * @param <E>
  */
 class PVMonitor<E> implements Observable<E>{
 	
 	static final Logger logger = LoggerFactory.getLogger(PVMonitor.class);
 	
 	private final PV<E> pv;
 	
 	private final Observable<E> observable;
 
 	ObservableUtil<E> oc = null;
 
 	private boolean monitorAdded = false;
 	
 	MonitorListener monitorListener = new MonitorListener() {
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			E extractValueFromDbr;
 			try {
 				DBR dbr = arg0.getDBR();
 				if (dbr != null){
 					extractValueFromDbr = pv.extractValueFromDbr(dbr);
 					if( oc != null){
 						oc.notifyIObservers(observable, extractValueFromDbr);
 					}
 				} else {
 					arg0.toString();
 				}
 			} catch (Exception e) {
 				logger.error("Error extracting data from histogram update", e);
 			}
 		}
 	};
 	
 	public PVMonitor(Observable<E> observable, PV<E> pv){
 		this.observable = observable;
 		this.pv = pv;
 	}
 
 	@Override
 	public void addObserver(Observer<E> observer) throws Exception {
 		getObservableComponent().addObserver(observer);
 		addMonitorListenerIfRequired();
 	}
 
 	@Override
 	public void addObserver(Observer<E> observer, Predicate<E> predicate) throws Exception {
 		getObservableComponent().addObserver(observer, predicate);
 		addMonitorListenerIfRequired();
 	}
 	
 	private ObservableUtil<E> getObservableComponent() {
 		if (oc == null) {
 			oc = new ObservableUtil<E>();
 		}
 		return oc;
 	}
 
 	private void addMonitorListenerIfRequired() {
 		if (!monitorAdded){
 			try {
 				pv.addMonitorListener(monitorListener);
 				monitorAdded = true;
 			} catch (IOException e) {
 				throw new IllegalStateException("Error adding monitor to pv:"+ pv.getPvName(),e);
 			}
 		}
 	}
 
 	@Override
 	public void removeObserver(Observer<E> observer) {
 		if( oc == null){
 			return;
 		}
 		oc.removeObserver(observer);
 		if (!IsBeingObserved()){
 			if (monitorAdded){
 				pv.removeMonitorListener(monitorListener);
 				monitorAdded = false;
 			}
 		}
 	}
 
 	public boolean IsBeingObserved() {
 		return oc == null ? false : oc.IsBeingObserved();
 	}
 
 
 	
 }
 
 
