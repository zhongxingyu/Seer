 package de.ptb.epics.eve.util.pv;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.csstudio.utility.pv.PV;
 import org.csstudio.utility.pv.PVFactory;
 import org.epics.pvmanager.*;
 import org.epics.pvmanager.data.AlarmSeverity;
 import org.epics.pvmanager.data.SimpleValueFormat;
 import org.epics.pvmanager.data.VEnum;
 import org.epics.pvmanager.data.ValueFormat;
 import org.epics.pvmanager.data.ValueUtil;
 
 
 import de.ptb.epics.eve.preferences.Activator;
 import de.ptb.epics.eve.preferences.PreferenceConstants;
 
 import static org.epics.pvmanager.ExpressionLanguage.*;
 import static org.epics.pvmanager.util.TimeDuration.*;
 import static org.csstudio.utility.pvmanager.ui.SWTUtil.*;
 
 /**
  * <code>PVWrapper</code> wraps a {@link org.epics.pvmanager.PV}. During object 
  * creation the process variable automatically tries to connect. Invoke 
  * {@link #disconnect()} to close the connection. It cannot be reopened.
  * 
  * @author Marcus Michalsky
  * @since 1.1
  */
 public class PVWrapper {
 	
 	// logging
 	private static Logger logger = Logger.getLogger(PVWrapper.class.getName());
 	
 	// the wrapped process variable
 	private org.epics.pvmanager.PV<Object,Object> pv;
 	
 	// workaround (pvmanager does not support a readonly check)
 	// using the "old" pv lib to connect during object initialization
 	// when the first value change is triggered (PVListener) the 
 	// isWriteAllowed status is saved and the pv is disconnected.
 	private org.csstudio.utility.pv.PV pv2;
 	
 	// the trigger pv (if a "goto" pv has to be triggered)
 	// also workaround until write issues with pvmanager are solved
 	private org.csstudio.utility.pv.PV triggerPV;
 	
 	// the name of the process variable
 	private String pvName;
 	
 	// the value of the process variable
 	private String pvValue;
 	
 	// the severity of the process variable (status)
 	private AlarmSeverity pvStatus;
 	
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
 	private ReadListener readListener;
 	
 	// listener for process variable writes
 	//private WriteListener writeListener;
 	
 	// workaround listener
 	private PVListener pvListener;
 	
 	// Delegated Observable
 	private PropertyChangeSupport propertyChangeSupport;
 	
 	// indicates whether the ENUM values are already read (performance)
 	private boolean isEnumInitialized;
 	
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
 		this.pvStatus = AlarmSeverity.UNDEFINED;
 		this.isConnected = false;
 		this.isDiscrete = false;
 		this.discreteValues = new ArrayList<String>(0);
 		
 		// workaround start (pvmanager does not support readonly check)
 		try {
 			pv2 = PVFactory.createPV("ca://" + pvname);
 			pv2.start();
 			pvListener = new PVListener();
 			pv2.addListener(pvListener);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			this.isReadOnly = true;
 		}
 		// workaround end
 		
 		// fetch the preference entry for the update interval
 		this.pvUpdateInterval = Activator.getDefault().getPreferenceStore().
 							getInt(PreferenceConstants.P_PV_UPDATE_INTERVAL);
 		
 		// get a pv instance from the factory
 		this.pv = PVManager.readAndWrite(channel(pvname)).
 							notifyOn(swtThread()).
 							asynchWriteAndReadEvery(ms(pvUpdateInterval));
 		
 		// start listening to changes
 		this.readListener = new ReadListener();
 		this.pv.addPVReaderListener(this.readListener);
 		//this.writeListener = new WriteListener();
 		//this.pv.addPVWriterListener(this.writeListener);
 		
 		this.valueFormat = new SimpleValueFormat(1);
 		// Engineering Notation (next 2 lines and the DecimalFormat Argument)
 		// Locale locale = new Locale("en");
 		// DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
 		this.valueFormat.setNumberFormat(new PVNumberFormat("##0.00000E00"));
 				//new DecimalFormat("##0.00000E00", symbols));
 		
 		this.propertyChangeSupport = new PropertyChangeSupport(this);
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
 			logger.debug("trigger is null.");
 			return;
 		}
 		try {
 			this.triggerPV = PVFactory.createPV("ca://" + triggerName);
 			this.triggerPV.start();
 			logger.debug("set trigger to " + this.triggerPV);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * Disconnects the process variable.
 	 */
 	public void disconnect() {
 		this.pv2.removeListener(pvListener);
 		this.pv2.stop();
 		if (this.triggerPV != null) {
 			this.triggerPV.stop();
 		}
 		this.pv.removePVReaderListener(this.readListener);
 		//this.pv.removePVWriterListener(this.writeListener);
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
 	public AlarmSeverity getStatus() {
 		return this.pvStatus;
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
 	 * Sets a new value for the process variable.
 	 * <p>
 	 * <b>Remember</b> that PVs are threaded. Do not call {@link #setValue(Object)} 
 	 * immediately after connecting without checking {@link #isConnected()}.
 	 * 
 	 * @param newVal the value that should be set
 	 */
 	public void setValue(Object newVal) {
 		//this.pv.write(newVal);
 		try {
 			this.pv2.setValue(newVal);
 			if (this.triggerPV != null) {
 				// TODO: Die trigger PV soll nicht mit 2 oder 1 gesetzt werden
 				// sondern mit dem Wert der im XML-File steht!
 				this.triggerPV.setValue(2);
 			}
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			return;
 		}
 		if(logger.isDebugEnabled()) {
 			logger.debug("Wrote " + this.getName() + ": " + newVal);
 			if(this.triggerPV != null) {
 				logger.debug("Additionally send Trigger " + 
 						this.triggerPV.getName());
 			}
 		}
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
 	
 	/**
 	 * Temporary. 
 	 * Stays until PVManager can write...
 	 * 
 	 * @return @{@link org.csstudio.utility.pv.PV#isConnected()}
 	 */
 	public boolean isConnected2() {
 		return this.pv2.isConnected();
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
 	 * <code>ReadListener</code> is the 
 	 * {@link org.epics.pvmanager.PVReaderListener} listening to changes on the 
 	 * wrapped process variable.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.1
 	 */
 	private class ReadListener implements PVReaderListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void pvChanged() {
 			isConnected = true;
 			
 			Object newVal = pv.getValue();
 			
 			propertyChangeSupport.firePropertyChange("value", pvValue,
 					pvValue = valueFormat.format(newVal));
 			propertyChangeSupport.firePropertyChange("status", pvStatus, 
 					pvStatus = ValueUtil.alarmOf(pv.getValue()).
 									getAlarmSeverity());
 			Exception e = pv.lastException();
 			if(e != null) {
 				logger.warn(e.getMessage(), e);
 			}
 			if(logger.isDebugEnabled()) {
 				logger.debug("new value for '" + getName() + "' : " + 
 							valueFormat.format(newVal) + 
 							" (" + ValueUtil.timeOf(
 							newVal).getTimeStamp().asDate().toString() 
 							+ ")");
 			}
 			
 			if(newVal instanceof VEnum) { // && !isEnumInitialized) {
 				isDiscrete = true;
 				isEnumInitialized = true;
 				discreteValues = ((VEnum)newVal).getLabels();
 				if (logger.isDebugEnabled()) {
 					StringBuilder sb = new StringBuilder();
 					for (String s : discreteValues) {
 						sb.append(s + ",");
 					}
 					logger.debug("got enums: "
 							+ sb.toString().substring(0,
 									sb.toString().length() - 2)
 							+ " at "
 							+ ValueUtil.timeOf(newVal).getTimeStamp().asDate()
 									.toString());
 				}
 			}
 			if(newVal instanceof VEnum) {
 				propertyChangeSupport.firePropertyChange("discreteValues",
 						null, discreteValues);
 			}
 		}
 	}
 	
 	/**
 	 * <code>WriteListener</code> is the 
 	 * {@link org.epics.pvmanager.PVWriterListener} listening to writes on the 
 	 * wrapped process variable. It sets the process variable read only if a 
 	 * write failed.
 	 * XXX Failsafe for write status. Still necessary ?
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.1
 	 */
 	private class WriteListener implements PVWriterListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void pvWritten() {
 			Exception lastException = pv.lastWriteException();
 			if (lastException instanceof WriteFailException) {
 				logger.warn("Write to PV failed", lastException);
 				isReadOnly = true;
 			}
 		}
 	}
 	
 	/**
 	 * Part of the workaround.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.1
 	 */
 	private class PVListener implements org.csstudio.utility.pv.PVListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void pvValueUpdate(PV pv) {
 			isReadOnly = !pv.isWriteAllowed();
 			logger.debug("got read only status (" + 
 					!pv.isWriteAllowed() + ") of " + 
 					pv.getName());
 			pv.removeListener(this);
 			//pv.stop();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void pvDisconnected(PV pv) {
 		}
 	}
 }
