 package de.ptb.epics.eve.util.pv;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.csstudio.utility.pv.PVFactory;
 import org.eclipse.swt.widgets.Display;
 import org.epics.pvmanager.*;
 import org.epics.vtype.AlarmSeverity;
 import org.epics.vtype.SimpleValueFormat;
 import org.epics.vtype.VEnum;
 import org.epics.vtype.ValueFormat;
 import org.epics.vtype.ValueUtil;
 
 import de.ptb.epics.eve.preferences.Activator;
 import de.ptb.epics.eve.preferences.PreferenceConstants;
 import static org.epics.pvmanager.ExpressionLanguage.*;
 import static org.epics.util.time.TimeDuration.*;
 
 /**
  * <code>PVWrapper</code> wraps a {@link org.epics.pvmanager.PV}. During object 
  * creation the process variable automatically tries to connect. Invoke 
  * {@link #disconnect()} to close the connection. It cannot be reopened.
  * 
  * @author Marcus Michalsky
  * @since 1.1
  */
 public class PVWrapper {
 	
 	/** the pv value bean property */
 	public static final String VALUE = "value";
 	
 	/** the pv status bean property */
 	public static final String SEVERITY = "severity";
 	
 	/** the pv connection status bean property */
 	public static final String CONNECTION_STATUS = "isConnected";
 	
 	/** the pv write status bean property */
 	public static final String WRITE_STATUS = "isReadOnly";
 	
 	/** the pv discrete values bean property */
 	public static final String DISCRETE_VALUES = "discreteValues";
 	
 	// logging
 	private static final Logger LOGGER = Logger.getLogger(
 			PVWrapper.class.getName());
 	
 	// the wrapped process variable
 	private org.epics.pvmanager.PV<Object,Object> pv;
 	
 	// the trigger pv (if a "goto" pv has to be triggered)
 	// also workaround until write issues with pvmanager are solved
 	private org.csstudio.utility.pv.PV triggerPV;
 	
 	// the name of the process variable
 	private String pvName;
 	
 	// the value of the process variable
 	private String pvValue;
 	
 	// the severity of the process variable (status)
 	private AlarmSeverity severity;
 	
 	// indicates whether the process variable is discrete
 	private boolean isDiscrete;
 	
 	// indicates whether the process variable is readonly
 	private boolean isReadOnly;
 	
 	// a pv is connected after its first subscription update
 	// until it is disconnected via the corresponding method
 	private boolean isConnected;
 	
 	// contains the discrete values of the process variable
 	// (or empty if not discrete)
 	private List<String> discreteValues;
 	
 	// the refresh interval
 	private int pvUpdateInterval;
 	
 	// helper to format process variable objects
 	private ValueFormat valueFormat;
 	
 	// listener for process variable updates
 	private PVReaderListener<Object> readListener;
 	
 	// Delegated Observable
 	private PropertyChangeSupport propertyChangeSupport;
 	
 	/**
 	 * Constructs a <code>PVWrapper</code>.
 	 * <p>
 	 * Automatically connects the pv with the given name (if possible).
 	 * Notice that it is not connected immediately (due to threading). 
 	 * Its connection status is indicated by {@link #isConnected()}.
 	 * 
 	 * @param pvname the name (id) of the process variable
 	 */
 	public PVWrapper(String pvname) {
 		this.pvName = pvname;
 		this.pvValue = "";
 		this.severity = AlarmSeverity.UNDEFINED;
 		this.isConnected = false;
 		this.isReadOnly = true;
 		this.isDiscrete = false;
 		this.discreteValues = new ArrayList<String>(0);
 		
 		this.propertyChangeSupport = new PropertyChangeSupport(this);
 		
 		// fetch the preference entry for the update interval
 		this.pvUpdateInterval = Activator.getDefault().getPreferenceStore().
 							getInt(PreferenceConstants.P_PV_UPDATE_INTERVAL);
 		
 		this.valueFormat = new SimpleValueFormat(1);
 		// Engineering Notation (next 2 lines and the DecimalFormat Argument)
 				// Locale locale = new Locale("en");
 		// DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
 		this.valueFormat.setNumberFormat(new PVNumberFormat("##0.00000E00"));
 				//new DecimalFormat("##0.00000E00", symbols));
 		
 		this.pv = PVManager.readAndWrite(channel(pvname))
 				.timeout(ofSeconds(5), "Timeout: " + this.pvName)
 				.asynchWriteAndMaxReadRate(ofMillis(this.pvUpdateInterval));
		
		this.readListener = new ReadListener();
		this.pv.addPVReaderListener(this.readListener);
		
 	}
 	
 	/**
 	 * Constructor that calls {@link #PVWrapper(String)}. 
 	 * <p>
 	 * Pass a non-<code>null</code> value for <code>triggerName</code> to append 
 	 * a trigger after {@link #setValue(Object)}.
 	 * 
 	 * @param pvName the pv string
 	 * @param triggerName the pv string of the trigger pv or <code>null</code> 
 	 * 						if none
 	 */
 	public PVWrapper(String pvName, String triggerName) {
 		this(pvName);
 		if (triggerName == null) {
 			this.triggerPV = null;
 			LOGGER.debug("trigger is null.");
 			return;
 		}
 		try { // TODO
 			this.triggerPV = PVFactory.createPV("ca://" + triggerName);
 			this.triggerPV.start();
 			LOGGER.debug("set trigger to " + this.triggerPV);
 		} catch (Exception e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Disconnects the process variable.
 	 */
 	public void disconnect() {
 		if (this.triggerPV != null) {
 			this.triggerPV.stop();
 		}
 		this.pv.removePVReaderListener(this.readListener);
 		this.pv.close();
 		this.isConnected = false;
 	}
 	
 	/**
 	 * Returns the name (id) of the process variable.
 	 * 
 	 * @return the name (id) of the process variable
 	 */
 	public String getName() {
 		return this.pvName;
 	}
 	
 	/**
 	 * Returns the value of the process variable.
 	 * 
 	 * @return the value of the process variable
 	 */
 	public String getValue() {
 		if(this.pvValue.endsWith("E00")) {
 			return this.pvValue.substring(0, this.pvValue.length()-3);
 		}
 		return this.pvValue;
 	}
 	
 	/**
 	 * Sets a new value for the process variable.
 	 * <p>
 	 * <b>Remember</b> that PVs are threaded. Do not call {@link #setValue(Object)} 
 	 * immediately after connecting without checking {@link #isConnected()}.
 	 * 
 	 * @param newVal the value that should be set
 	 */
 	public void setValue(Object newVal) {
 		if (this.isReadOnly()) {
 			LOGGER.warn("tried to write to read only pv: " + this.pvName);
 			return;
 		}
 		try {
 			this.pv.write(newVal);
 			if (this.triggerPV != null) {
 				// TODO: Die trigger PV soll nicht mit 2 oder 1 gesetzt werden
 				// sondern mit dem Wert der im XML-File steht!
 				this.triggerPV.setValue(2);
 			}
 		} catch (Exception e) {
 			LOGGER.error(e.getMessage(), e);
 			return;
 		}
 		if(LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Wrote " + this.getName() + ": " + newVal);
 			if(this.triggerPV != null) {
 				LOGGER.debug("Additionally send Trigger " + 
 						this.triggerPV.getName());
 			}
 		}
 	}
 	
 	/**
 	 * Returns the value of the PV as in 
 	 * {@link org.epics.pvmanager.PV#getValue()}.
 	 * 
 	 * @return {@link org.epics.pvmanager.PV#getValue()}
 	 */
 	public Double getRawValue() {
 		return ValueUtil.numericValueOf(this.pv.getValue());
 	}
 	
 	/**
 	 * Returns the status (severity) of the process variable.
 	 * Possible values are defined in 
 	 * {@link org.epics.pvmanager.data.AlarmSeverity}.
 	 * 
 	 * @return the status (severity) of the process variable
 	 * @see {@link org.epics.pvmanager.data.AlarmSeverity}
 	 */
 	public AlarmSeverity getSeverity() {
 		return this.severity;
 	} 
 	
 	/**
 	 * Checks whether the process variable is discrete.
 	 * 
 	 * @return <code>true</code> if the process variable is discrete, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isDiscrete() {
 		return this.isDiscrete;
 	}
 	
 	/**
 	 * Returns the discrete values of the process variable (or an empty list, 
 	 * if {@link #isDiscrete()} <code> == false</code>.
 	 * 
 	 * @return the discrete values of the process variable
 	 */
 	public String[] getDiscreteValues() {
 		return this.discreteValues.toArray(new String[0]);
 	}
 	
 	/**
 	 * Checks whether the process variable is only readable.
 	 * 
 	 * @return <code>true</code> if the process variable is readonly, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isReadOnly() {
 		return this.isReadOnly;
 	}
 	
 	/**
 	 * Checks whether the process variable is connected.
 	 * 
 	 * @return <code>true</code> if the process variable is connected, 
 	 * 			<code>false</code> otherwise
 	 */
 	public boolean isConnected() {
 		return this.isConnected;
 	}
 	
 	public boolean isConnected2() {
 		return this.isConnected;
 	}
 	
 	/**
 	 * Register to observe a certain property.
 	 * 
 	 * @param propertyName the property of interest
 	 * @param listener the {@link java.beans.PropertyChangeListener} that 
 	 * 					should receive the notification
 	 * @see {@link java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
 	}
 
 	/**
 	 * @see {@link java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		propertyChangeSupport.removePropertyChangeListener(propertyName,
 				listener);
 	}
 	
 	/**
 	 * Removes the listener (regardless of the property).
 	 * 
 	 * @param listener the listener that should be removed.
 	 * @see {@link java.beans.PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)}
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		propertyChangeSupport.removePropertyChangeListener(listener);
 	}
 	
 	/* ********************************************************************* */
 	
 	/**
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.14
 	 */
 	private class ReadListener implements PVReaderListener<Object> {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void pvChanged(PVReaderEvent<Object> pvReaderEvent) {
 			final PVReader<Object> pvReader = pvReaderEvent.getPvReader();
 			final Object value = pvReader.getValue();
 
 			if (pvReaderEvent.isExceptionChanged()) {
 				Exception e = pvReader.lastException();
 				if (e instanceof TimeoutException) {
 					LOGGER.warn(e.getMessage());
 				} else {
 					LOGGER.warn(e.getMessage(), e);
 				}
 			}
 			if (pvReaderEvent.isConnectionChanged()) {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						propertyChangeSupport.firePropertyChange(
 								PVWrapper.CONNECTION_STATUS, isConnected,
 								isConnected = pvReader.isConnected());
 					}
 				});
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						propertyChangeSupport.firePropertyChange(
 								PVWrapper.WRITE_STATUS, isReadOnly,
 								isReadOnly = !pv.isWriteConnected());
 					}
 				});
 			}
 			
 			if (pvReaderEvent.isValueChanged()) {
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						propertyChangeSupport.firePropertyChange(
 								PVWrapper.VALUE, pvValue,
 								pvValue = valueFormat.format(value));
 					}
 				});
 				if (value == null) {
 					return;
 				}
 				Display.getDefault().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						propertyChangeSupport.firePropertyChange(
 								PVWrapper.SEVERITY, severity,
 								severity = ValueUtil.alarmOf(value)
 										.getAlarmSeverity());
 					}
 				});
 
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug("new value for '"
 							+ pvReader.getName()
 							+ "': "
 							+ valueFormat.format(value)
 							+ " ("
 							+ ValueUtil.timeOf(value).getTimestamp().toDate()
 									.toString() + ") ");
 				}
 
 				isDiscrete = false;
 				if (value instanceof VEnum) {
 					isDiscrete = true;
 					discreteValues = ((VEnum) value).getLabels();
 					if (LOGGER.isDebugEnabled()) {
 						StringBuilder sb = new StringBuilder();
 						for (String s : discreteValues) {
 							sb.append(s + ",");
 						}
 						LOGGER.debug("got enums: "
 								+ sb.toString().substring(0,
 										sb.toString().length() - 2)
 								+ " at "
 								+ ValueUtil.timeOf(value).getTimestamp()
 										.toDate().toString());
 					}
 				}
 				if (value instanceof VEnum) {
 					Display.getDefault().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							propertyChangeSupport.firePropertyChange(
 									PVWrapper.DISCRETE_VALUES, null,
 									discreteValues);
 						}
 					});
 				}
 			}
 		}
 	}
 }
