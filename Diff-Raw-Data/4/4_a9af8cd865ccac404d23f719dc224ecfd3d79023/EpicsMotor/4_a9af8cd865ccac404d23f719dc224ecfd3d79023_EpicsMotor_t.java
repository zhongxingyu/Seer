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
 
 package gda.device.motor;
 
 import gda.configuration.epics.ConfigurationNotFoundException;
 import gda.configuration.epics.Configurator;
 import gda.configuration.epics.EpicsConfiguration;
 import gda.device.BlockingMotor;
 import gda.device.Motor;
 import gda.device.MotorException;
 import gda.device.MotorProperties.MotorEvent;
 import gda.device.MotorProperties.MotorProperty;
 import gda.device.MotorStatus;
 import gda.device.motor.EpicsMotor.STATUSCHANGE_REASON;
 import gda.device.scannable.MotorUnitStringSupplier;
 import gda.epics.AccessControl;
 import gda.epics.connection.CompoundDataTypeHandler;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.EpicsController.MonitorType;
 import gda.epics.connection.InitializationListener;
 import gda.epics.connection.STSHandler;
 import gda.epics.connection.TIMEHandler;
 import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
 import gda.epics.interfaceSpec.InterfaceException;
 import gda.epics.interfaces.SimpleMotorType;
 import gda.epics.util.EpicsGlobals;
 import gda.epics.xml.EpicsRecord;
 import gda.factory.FactoryException;
 import gda.factory.Finder;
 import gda.jython.Jython;
 import gda.jython.JythonServerFacade;
 import gda.observable.IObserver;
 import gda.util.exceptionUtils;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBRType;
 import gov.aps.jca.dbr.DBR_Double;
 import gov.aps.jca.dbr.DBR_Enum;
 import gov.aps.jca.dbr.DBR_Float;
 import gov.aps.jca.dbr.DBR_Short;
 import gov.aps.jca.dbr.Severity;
 import gov.aps.jca.dbr.Status;
 import gov.aps.jca.dbr.TimeStamp;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * EpicsMotor implements GDA Motor interface and provide mapping from GDA interface to EPICS motor record. Note only
  * selected PVs or channels are instantiated in this class as required by the GDA motor interface.
  */
 public class EpicsMotor extends MotorBase implements Motor, BlockingMotor, InitializationListener, IObserver,
 		MotorUnitStringSupplier {
 
 	@Override
 	public void savePosition(String name, double currentPosition) {
 		// do nothing
 	}
 
 	@Override
 	public void savePosition(String name) {
 		// do nothing
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(EpicsMotor.class);
 
 	private boolean assertHomedBeforeMoving = false;
 
 	/**
 	 * Cached motor properties
 	 */
 	private volatile double currentPosition = Double.NaN;
 
 	private volatile double currentSpeed = Double.NaN;
 
 	private final Object _motorStatusMonitor = new Object();
 
 	private volatile MotorStatus _motorStatus = MotorStatus.UNKNOWN;
 
 	private volatile Boolean homed = null;
 
 	private volatile double targetPosition = Double.NaN;
 
 	private volatile double retryDeadband;
 
 	private boolean callbackWait = false;
 
 	private boolean DMOVRefreshEnabled = true;
 	// we always get a DMOV after a caput_callback so we do not need to act on it
 	// private boolean ignoreNextDMOV = false;
 
 	/**
 	 * EPICS channels to connect
 	 */
 	protected Channel val = null; // user desired value .VAL, double in EGU
 
 	protected Channel rbv = null; // User readback value .RBV, double in EGU
 
 	protected Channel offset = null; // set motor offset without moving motor
 
 	protected Channel stop = null; // the motor stop control
 
 	protected Channel velo = null; // Velocity (EGU/s) .VELO, FLOAT
 
 	protected Channel accl;
 
 	protected Channel lvio = null; // Limit Violation, .LVIO, SHORT
 
 	protected Channel dmov = null; // Done move to value, .DMOV, SHORT
 
 	protected Channel rdbd = null; // retry deadband
 
 	protected Channel hlm = null; // User High Limit .HLM, FLOAT
 
 	protected Channel llm = null; // User Lower Limit .LLM, FLOAT
 
 	protected Channel hls = null; // User At High Limit Switch .HLS, FLOAT
 
 	protected Channel lls = null; // User At Lower Limit Switch .LLS, FLOAT
 
 	protected Channel dhlm;
 
 	protected Channel dllm;
 
 	protected Channel homf = null; // Home Forward, .HOMF, SHORT
 
 	protected Channel mres = null; // motor resolution
 
 	protected Channel unitString = null; // EPICS motor Unit
 
 	protected Channel msta = null;// Hardware status
 
 	protected Channel spmg = null; // motor template mode (Go or Stop)
 
 	/**
 	 * monitor EPICS motor position
 	 */
 	protected RBVMonitorListener positionMonitor;
 
 	/**
 	 * Monitor EPICS motor's DMOV - EPICS motor motion completion status
 	 */
 	protected DMOVMonitorListener statusMonitor;
 
 	/**
 	 * Monitor EPICS motor lower limit
 	 */
 	protected LLMMonitorListener lowLimitMonitor;
 
 	/**
 	 * Monitor EPICS motor higher limit
 	 */
 	protected HLMMonitorListener highLimitMonitor;
 
 	/**
 	 * Monitor EPICS motor dial higher limit
 	 */
 	protected DHLMMonitorListener dialHighLimitMonitor;
 
 	/**
 	 * Monitor EPICS motor dial lower limit
 	 */
 	protected DLLMMonitorListener dialLowLimitMonitor;
 	
 	protected MSTAMonitorListener mstaMonitorListener;
 	/**
 	 * Monitor EPICS motor limit violation
 	 */
 	protected LVIOMonitorListener lvioMonitor;
 
 	protected Channel setPv;
 	protected SetUseMonitorListener setUseListener;
 	
 	/**
 	 * EPICS Put call back handler
 	 */
 	protected PutCallbackListener putCallbackListener;
 
 	// private MSTAMonitorListener mstaMonitorListener;
 
 	private Status status = Status.NO_ALARM;
 	private Severity severity = Severity.NO_ALARM;
 	private TimeStamp timestamp = null;
 	/**
 	 * Name of the EPICS record object
 	 */
 	private String epicsRecordName = null;
 
 	/**
 	 * EPICS record object
 	 */
 	private EpicsRecord epicsRecord = null;
 
 	/**
 	 * GDA device Name
 	 */
 	private String deviceName = null;
 
 	protected String pvName;
 
 	/**
 	 * EPICS controller
 	 */
 	protected EpicsController controller;
 
 	/**
 	 * EPICS Channel Manager
 	 */
 	protected EpicsChannelManager channelManager;
 
 	/**
 	 * Motor access control object name (CASTOR XML)
 	 */
 	private String accessControlName;
 
 	/**
 	 * Motor access control object (hook-up in CASTOR XML)
 	 */
 	private AccessControl accessControl;
 
 	private AccessControl.Status acs = AccessControl.Status.ENABLED;
 
 	/**
 	 * request completion flag
 	 */
 	@SuppressWarnings("unused")
 	private volatile boolean requestDone = true; // start true
 
 	/**
 	 * specify if missed target motion allowed or not.
 	 */
 	public boolean checkMissedTarget = false;
 
 	private static MoveEventQueue moveEventQueue = new MoveEventQueue();
 
 	/**
 	 * Normally the unitString is read from EPICS EGu field. But if this is no supported then we may have to set it.
 	 * Only setter is provided for object configuration
 	 */
 	private String unitStringOverride = null;
 
 	public void setUnitStringOverride(String unitStringOverride) {
 		this.unitStringOverride = unitStringOverride;
 	}
 
 	public EpicsMotor() {
 		controller = EpicsController.getInstance();
 		channelManager = new EpicsChannelManager(this);
 		positionMonitor = new RBVMonitorListener();
 		statusMonitor = new DMOVMonitorListener();
 		putCallbackListener = new PutCallbackListener();
 		highLimitMonitor = new HLMMonitorListener();
 		lowLimitMonitor = new LLMMonitorListener();
 		dialHighLimitMonitor = new DHLMMonitorListener();
 		dialLowLimitMonitor = new DLLMMonitorListener();
 		lvioMonitor = new LVIOMonitorListener();
 		mstaMonitorListener = new MSTAMonitorListener();
 		setUseListener = new SetUseMonitorListener();
 	}
 
 	/**
 	 * Sets the record name that this motor will link to.
 	 * 
 	 * @param pvName
 	 *            the record name
 	 */
 	public void setPvName(String pvName) {
 		this.pvName = pvName;
 	}
 	
 	public String getPvName() {
 		return pvName;
 	}
 
 	protected EpicsConfiguration epicsConfiguration;
 
 	/**
 	 * Sets the EpicsConfiguration to use when looking up PV from deviceName.
 	 * 
 	 * @param epicsConfiguration
 	 *            the EpicsConfiguration
 	 */
 	public void setEpicsConfiguration(EpicsConfiguration epicsConfiguration) {
 		this.epicsConfiguration = epicsConfiguration;
 	}
 
 	/**
 	 * Sets the access control object used by this motor.
 	 * 
 	 * @param accessControl
 	 *            the access control object
 	 */
 	public void setAccessControl(AccessControl accessControl) {
 		this.accessControl = accessControl;
 	}
 
 	/**
 	 * Initialise the motor object.
 	 */
 	@Override
 	public void configure() throws FactoryException {
 		if (!configured) {
 
 			if (pvName == null) {
 
 				// Original implementation of EPICS interface
 				if (getEpicsRecordName() != null) {
 					if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
 						pvName = epicsRecord.getFullRecordName();
 					} else {
 						logger.error("Epics Record " + epicsRecordName + " not found");
 						throw new FactoryException("Epics Record " + epicsRecordName + " not found");
 					}
 				}
 
 				// EPICS interface version 2 for phase II beamlines.
 				else if (getDeviceName() != null) {
 					SimpleMotorType motorConfig;
 					try {
 						if (epicsConfiguration != null) {
 							motorConfig = epicsConfiguration.getConfiguration(getDeviceName(), SimpleMotorType.class);
 						} else {
 							motorConfig = Configurator.getConfiguration(getDeviceName(), SimpleMotorType.class);
 						}
 						pvName = motorConfig.getRECORD().getPv();
 					} catch (ConfigurationNotFoundException e) {
 						// Try to read from unchecked xml
 						try {
 							pvName = getPV();
 						} catch (Exception ex) {
 							logger.error(
 									"Can NOT find EPICS configuration for motor " + getDeviceName() + "."
 											+ e.getMessage(), ex);
 							throw new FactoryException("Can NOT find EPICS configuration for motor " + getDeviceName()
 									+ "." + e.getMessage(), e);
 						}
 					}
 				}
 
 				// Nothing specified in Server XML file
 				else {
 					logger.error("Missing EPICS configuration for the motor {}", getName());
 					throw new FactoryException("Missing EPICS interface configuration for the motor " + getName());
 				}
 			}
 
 			createChannelAccess();
 			channelManager.tryInitialize(100);
 
 			// If no access control object has been set, but a name has been specified, look up the name
 			if (accessControl == null && getAccessControlName() != null) {
 				accessControl = (AccessControl) Finder.getInstance().find(accessControlName);
 				if (accessControl == null) {
 					throw new FactoryException("Can not find access control object " + accessControl.getName());
 				}
 			}
 
 			if (accessControl != null) {
 				this.acs = accessControl.getStatus();
 				accessControl.addIObserver(this);
 			}
 			configured = true;
 		}// end of if (!configured)
 	}
 
 	public void forceCallback() throws MotorException {
 		moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, MotorStatus.READY, STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE);
 	}
 
 	/**
 	 * Create Channel access for motor. This must must on to EPICS motor record.
 	 */
 	protected void createChannelAccess() throws FactoryException {
 		try {
 			val = channelManager.createChannel(pvName + ".VAL", false);
 			rbv = channelManager.createChannel(pvName + ".RBV", positionMonitor, MonitorType.TIME, false);
 			offset = channelManager.createChannel(pvName + ".OFF", false);
 			stop = channelManager.createChannel(pvName + ".STOP", false);
 			velo = channelManager.createChannel(pvName + ".VELO", false);
 			accl = channelManager.createChannel(pvName + ".ACCL", false);
 			dmov = channelManager.createChannel(pvName + ".DMOV", statusMonitor, false);
 			lvio = channelManager.createChannel(pvName + ".LVIO");
 			hlm = channelManager.createChannel(pvName + ".HLM", highLimitMonitor, false);
 			llm = channelManager.createChannel(pvName + ".LLM", lowLimitMonitor, false);
			hls = channelManager.createChannel(pvName + ".HLS", false);
			lls = channelManager.createChannel(pvName + ".LLS", false);
 			dhlm = channelManager.createChannel(pvName + ".DHLM", dialHighLimitMonitor, false);
 			dllm = channelManager.createChannel(pvName + ".DLLM", dialLowLimitMonitor,false);
 			homf = channelManager.createChannel(pvName + ".HOMF", false);
 
 			rdbd = channelManager.createChannel(pvName + ".RDBD", false);
 			mres = channelManager.createChannel(pvName + ".MRES", false);
 			unitString = channelManager.createChannel(pvName + ".EGU", false);
 			msta = channelManager.createChannel(pvName + ".MSTA", mstaMonitorListener, false);
 			spmg = channelManager.createChannel(pvName + ".SPMG", false);
 			setPv = channelManager.createChannel(pvName + ".SET", setUseListener, false);
 
 			// acknowledge that creation phase is completed
 			channelManager.creationPhaseCompleted();
 
 		} catch (Throwable th) {
 			// TODO take care of destruction
 			throw new FactoryException("failed to connect to all channels", th);
 		}
 	}
 
 	private void ensuredConfigured() throws FactoryException {
 		if (!configured)
 			configure();
 	}
 
 	private void waitForInitialisation() throws TimeoutException, FactoryException {
 		ensuredConfigured();
 		long startTime_ms = System.currentTimeMillis();
 		double timeout_s = EpicsGlobals.getTimeout();
 		long timeout_ms = (long) (timeout_s * 1000.);
 
 		while (!isInitialised() && (System.currentTimeMillis() - startTime_ms < timeout_ms)) {
 			try {
 				Thread.sleep(timeout_ms / 5); // TODO: Are we sure!?
 			} catch (InterruptedException e) {
 				// do nothing
 			}
 		}
 		if (!isInitialised())
 			throw new TimeoutException(getName() + " not yet initalised. Does the PV " + pvName + " exist?");
 	}
 
 	/**
 	 * gets the unit string from EPICS motor.
 	 * 
 	 * @return unit string
 	 */
 	@Override
 	public String getUnitString() throws MotorException {
 		try {
 			if (unitStringOverride != null)
 				return unitStringOverride;
 			waitForInitialisation();
 			return controller.caget(unitString);
 		} catch (Throwable e) {
 			throw new MotorException(getStatus(), "failed to get motor engineering unit", e);
 		}
 	}
 
 	/**
 	 * Sets the speed of the motor in IOC in mm/second.
 	 */
 	@Override
 	public void setSpeed(double mmPerSec) throws MotorException {
 		try {
 			//must use caputWait to ensure the speed is set before we start moving
 			controller.caputWait(velo, mmPerSec);
 			currentSpeed = mmPerSec;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to set setSpeed", ex);
 		}
 	}
 
 	/**
 	 * Gets the current speed of the motor in mm/second
 	 * 
 	 * @return double the motor speed in revolution per second
 	 */
 	@Override
 	public double getSpeed() throws MotorException {
 		try {
 			currentSpeed = controller.cagetDouble(velo);
 			return currentSpeed;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get speed", ex);
 		}
 	}
 
 	@Override
 	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
 		try {
 			controller.caput(accl, timeToVelocity);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to set acceleration", ex);
 		}
 	}
 
 	@Override
 	public double getTimeToVelocity() throws MotorException {
 		try {
 			final double timeToVelocity = controller.cagetDouble(accl);
 			return timeToVelocity;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get acceleration", ex);
 		}
 	}
 
 	/**
 	 * Gets the retry dead band for this motor from EPICS.
 	 * 
 	 * @return double - the retry dead band.
 	 */
 	@Override
 	public double getRetryDeadband() throws MotorException {
 		try {
 			retryDeadband = controller.cagetDouble(rdbd);
 			return retryDeadband;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get speed", ex);
 		}
 	}
 
 	/**
 	 * Gets the motor resolution from EPICS motor.
 	 * 
 	 * @return double - the motor resolution
 	 */
 	@Override
 	public double getMotorResolution() throws MotorException {
 		try {
 			return controller.cagetDouble(mres);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get speed", ex);
 		}
 	}
 
 	@Override
 	public double getUserOffset() throws MotorException {
 		try {
 			return controller.cagetDouble(offset);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get speed", ex);
 		}
 
 	}
 
 	public void setUserOffset(double userOffset) throws MotorException {
 		try {
 			controller.caput(offset, userOffset);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to set user offset", ex);
 		}
 	}
 
 	@Override
 	public boolean isMoving() throws MotorException {
 		return (checkStatus() == MotorStatus.BUSY);
 	}
 
 	/**
 	 * checks motor Status
 	 */
 	protected MotorStatus checkStatus() throws MotorException {
 		MotorStatus status = getStatus();
 		logger.debug("checking status " + status);
 		if (status == MotorStatus.UNKNOWN || status == MotorStatus.FAULT) {
 			logger.debug("throwing motor excetion for " + MotorStatus.FAULT);
 			throw new MotorException(MotorStatus.FAULT, "getStatus returned " + status.toString());
 		}
 		return status;
 	}
 
 	/**
 	 * Returns the motor status from the motor object.
 	 */
 	@Override
 	public MotorStatus getStatus() throws MotorException {
 		return get_motorStatus();
 	}
 
 	/**
 	 * @deprecated as the name is confusing
 	 */
 	@Deprecated
 	public MotorStatus getMotorStatus() {
 		refreshMotorStatus();
 		return get_motorStatus();
 	}
 
 	/**
 	 * Refreshes the motor status from EPICS server by checking (.DMOV)(motor busy/idle status), MSTA (motor hardware
 	 * status). Also checks The method checks whether the motor stopped due to a soft limit violation.
 	 */
 	public void refreshMotorStatus() {
 
 		try {
 			MotorStatus ms = MotorStatus.READY;
 			if (controller.cagetShort(dmov) == 0) {
 				ms = MotorStatus.BUSY;
 			}
 			String statusString = Long.toBinaryString((long) (Double.parseDouble(controller.caget(msta))));
 			if (statusString.charAt(0) == '1' || statusString.charAt(3) == '1' || statusString.charAt(6) == '1') {
 				logger.info("There is a hardware problem");
 				ms = MotorStatus.FAULT;
 			}
 			// check motor status flags MSTA
 			if (controller.cagetShort(lvio) == 1) {
 				logger.error("Soft limit violation on {}", lvio.getName());
 				ms = MotorStatus.SOFTLIMITVIOLATION;
 			}
 			// add to queue so that the status is cleared
 			if (!ms.equals(get_motorStatus())) {
 				moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, ms, STATUSCHANGE_REASON.NEWSTATUS);
 			}
 		} catch (Throwable th) {
 			exceptionUtils.logException(logger, "Motor : " + getName(), th);
 		}
 	}
 
 	/**
 	 * Relative move, moves the motor by the specified mount in user coordinate system units, specified in the .EGU
 	 * field of the Motor record.
 	 * 
 	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
 	 * @param increament
 	 *            - double the distance that motor need to travel in EGU
 	 */
 	@Override
 	public void moveBy(double increament) throws MotorException {
 		try {
 			targetPosition = getPosition() + increament;
 			targetRangeCheck(targetPosition);
 			moveTo(targetPosition);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to moveBy", ex);
 		}
 	}
 
 	enum STATUSCHANGE_REASON {
 		
 		START_MOVETO,
 		
 		MOVETO,
 		
 		INITIALISE,
 		
 		CAPUT_MOVECOMPLETE,
 		
 		/**
 		 * Like CAPUT_MOVECOMPLETE but the msta has to be queried in a separate thread to prevent EPICS timeouts
 		 */
 		CAPUT_MOVECOMPLETE_IN_ERROR,
 		
 		DMOV_MOVECOMPLETE,
 		
 		NEWSTATUS
 
 	}
 
 	static String reasonAsString(STATUSCHANGE_REASON reason) {
 		if (reason == STATUSCHANGE_REASON.START_MOVETO)
 			return "START_MOVETO";
 		if (reason == STATUSCHANGE_REASON.MOVETO)
 			return "MOVETO";
 		if (reason == STATUSCHANGE_REASON.INITIALISE)
 			return "INITIALISE";
 		if (reason == STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE)
 			return "CAPUT_MOVECOMPLETE";
 		if (reason == STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE_IN_ERROR)
 			return "CAPUT_MOVECOMPLETE_IN_ERROR";
 		if (reason == STATUSCHANGE_REASON.DMOV_MOVECOMPLETE)
 			return "DMOV_MOVECOMPLETE";
 		if (reason == STATUSCHANGE_REASON.NEWSTATUS)
 			return "NEWSTATUS";
 		return "unknown";
 	}
 
 	/*
 	 * This is where the observers are updated with status. It is only called by the single thread so no need for
 	 * synchronisation objects. DO NOT CALL DIRECTLY - use MoveEventQueue.addMoveCompleteEvent instead
 	 */
 	void changeStatusAndNotify(MotorStatus newStatus, STATUSCHANGE_REASON reason) throws MotorException {
 		try {
 			logger.debug("Motor - " + getName() + " changeStatusAndNotify started." + ". newStatus = "
 					+ (newStatus != null ? newStatus.toString() : "null") + ". reason = "
 					+ (reason != null ? reasonAsString(reason) : "null"));
 			switch (reason) {
 			case INITIALISE:
 				set_motorStatus(MotorStatus.READY);
 				notifyIObservers(MotorProperty.STATUS, getStatus());
 				setInitialised(true);
 				DMOVRefreshEnabled = true;
 				logger.debug("Motor - " + getName() + " initialised.");
 				break;
 			case START_MOVETO:
 				DMOVRefreshEnabled = false; // prevent DMOV listener update
 				MotorStatus oldStatus = get_motorStatus();
 				set_motorStatus(MotorStatus.BUSY);
 				try {
 					logger.debug("{}: caput with callback {} <<<", getName(), targetPosition);
 
 					controller.caput(val, targetPosition, putCallbackListener);
 				} catch (Exception ex) {
 					DMOVRefreshEnabled = true;
 					set_motorStatus(oldStatus);
 					throw ex;
 				}
 				break;
 			case MOVETO:
 				/* this is called in the queue after the START_MOVETO was executed straightaway */
 				notifyIObservers(MotorProperty.STATUS, getStatus());
 				break;
 			case CAPUT_MOVECOMPLETE:
 				if (newStatus != null)
 					set_motorStatus(newStatus);
 				notifyIObservers(EpicsMotor.this, MotorEvent.MOVE_COMPLETE);
 				logger.debug("Epics Motor " + getName() + " notyfying CAPUT_MOVECOMPLETE " + get_motorStatus());
 				DMOVRefreshEnabled = true; // allow DMOV listener to refresh
 				// ignoreNextDMOV = true;
 				break;
 			case CAPUT_MOVECOMPLETE_IN_ERROR:
 				MotorStatus motorStatusFromMSTAValue = MotorStatus.FAULT;
 				try {
 					double mstaVal = controller.cagetDouble(msta);
 					motorStatusFromMSTAValue = getMotorStatusFromMSTAValue(mstaVal);
 				} catch (Exception e) {
 					logger.error("Error gettting msta val for " + getName(), e);
 				}
 				changeStatusAndNotify(motorStatusFromMSTAValue, STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE);
 				break;
 			case DMOV_MOVECOMPLETE:
 				// DMOVRefreshEnabled could have been changed to false by START_MOVETO since the event was added to the
 				// MoveEvent queue
 				if (DMOVRefreshEnabled) {
 					// if (ignoreNextDMOV) {
 					// ignoreNextDMOV = false;
 					// return;
 					// }
 					if (newStatus != null)
 						set_motorStatus(newStatus);
 					notifyIObservers(EpicsMotor.this, MotorEvent.MOVE_COMPLETE);
 					logger.debug("Epics Motor " + getName() + " notyfying DMOV_MOVECOMPLETE " + get_motorStatus());
 				}
 				break;
 			case NEWSTATUS:
 				// DMOVRefreshEnabled could have been changed to false by START_MOVETO since the event was added to the
 				// MoveEvent queue
 				if (DMOVRefreshEnabled) {
 					if (newStatus != null && !newStatus.equals(get_motorStatus())) {
 						set_motorStatus(newStatus);
 						notifyIObservers(EpicsMotor.this, MotorEvent.REFRESH);
 						logger.debug("Epics Motor " + getName() + " notyfying NEWSTATUS " + get_motorStatus());
 					}
 				}
 				break;
 			}
 		} catch (MotorException me) {
 			throw me;
 		} catch (Exception ex) {
 			throw new MotorException(get_motorStatus(), "Error in changeStatusAndNotify", ex);
 		} finally {
 			logger.debug("Motor - " + getName() + " changeStatusAndNotify complete");
 		}
 	}
 
 	/**
 	 * Absolute move, moves the motor to the specified position in user coordinate system units, specified by .EGU field
 	 * of the Motor Record.
 	 * 
 	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
 	 * @param position
 	 *            - double - the absolute position of the motor in EGU (
 	 */
 	@Override
 	public void moveTo(double position) throws MotorException {
 		
 		setUseListener.checkMotorIsInUseMode();
 		
 		targetPosition = position;
 		targetRangeCheck(position);
 		logger.debug("{}: moveto {}", getName(), position);
 
 		if (acs == AccessControl.Status.DISABLED)
 			throw new MotorException(getStatus(), "moveTo aborted because this motor is disabled");
 
 		if (getStatus() == MotorStatus.BUSY)
 			throw new MotorException(getStatus(), "moveTo aborted because previous move not yet completed");
 		if (getStatus() == MotorStatus.FAULT)
 			throw new MotorException(getStatus(),
 					"moveTo aborted because EPICS Motor is at Fault status. Please check EPICS Screen.");
 		if (isAssertHomedBeforeMoving() & !isHomed()) {
 			throw new MotorException(getStatus(),
 					"moveTo aborted because EPICS Motor is not homed (and assertHomedBeforeMoving is set)");
 		}
 
 		moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, null, STATUSCHANGE_REASON.MOVETO);
 	}
 
 	/**
 	 * moves motor to the specified position with timeout in seconds. If motor does not callback within the specified
 	 * time, this method time-out.
 	 * 
 	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
 	 */
 	public void moveTo(double position, double timeout) throws MotorException, TimeoutException, InterruptedException {
 		
 		setUseListener.checkMotorIsInUseMode();
 		
 		// final long timeout1 = (long) timeout * 1000;
 		targetPosition = position;
 		targetRangeCheck(position);
 		/*
 		 * This moveTo does not change motorStatus and so cannot use caputListener which does
 		 */
 		try {
 			controller.caput(val, targetPosition, timeout);
 		} catch (CAException ex) {
 			throw new MotorException(getStatus(), "Error in moveTo with timeout", ex);
 		}
 	}
 
 	/**
 	 * Asynchronously moves the motor to the specified position in EGU with a specified PutListener. You must handle the
 	 * callback in your PutListener code.
 	 * 
 	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
 	 * @param position
 	 *            the absolute position of the motor in EGU
 	 */
 	/*
 	 * This moveTo does not change motorStatus.
 	 */
 	public void moveTo(double position, PutListener moveListener) throws MotorException {
 		
 		setUseListener.checkMotorIsInUseMode();
 		
 		try {
 			targetRangeCheck(position);
 			// to reduce the race condition between EPICS Control and
 			// GDA request, however it does NOT prevent or eliminate the
 			// race condition as GDA can not lock EPICS access, so some
 			// sort of delayed action on EPICS DISABLED still required.
 			while (acs == AccessControl.Status.DISABLED) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					// do nothing
 				}
 			}
 			controller.caput(val, position, moveListener);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to moveTo", ex);
 		}
 	}
 
 	/**
 	 * Reads the motor's dial low limit (DLLM).
 	 * 
 	 * @return the dial low limit
 	 */
 	protected double getDialLowLimit() throws MotorException {
 		try {
 			return Double.isNaN(dialLowLimit) ? controller.cagetDouble(dllm) : dialLowLimit;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "Unable to read DLLM for " + getName(), ex);
 		}
 	}
 
 	/**
 	 * Reads the motor's dial high limit (DHLM).
 	 * 
 	 * @return the dial high limit
 	 */
 	protected double getDialHighLimit() throws MotorException {
 		try {
 			return Double.isNaN(dialHighLimit) ? controller.cagetDouble(dhlm) : dialHighLimit;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "Unable to read DHLM for " + getName(), ex);
 		}
 	}
 
 	/**
 	 * This method check the target position is within the limit range.
 	 * 
 	 * @param requestedPosition
 	 *            absolute requested target to validate within limits
 	 */
 	private void targetRangeCheck(double requestedPosition) throws MotorException {
 
 		if (!hasLimitsToCheck()) {
 			return;
 		}
 
 		final double lowerLimit = getMinPosition();
 		final double upperLimit = getMaxPosition();
 
 		if (requestedPosition < lowerLimit) {
 			throw (new MotorException(MotorStatus.LOWERLIMIT, requestedPosition + " outside lower hardware limit of "
 					+ lowerLimit));
 		}
 
 		else if (requestedPosition > upperLimit) {
 			throw (new MotorException(MotorStatus.UPPERLIMIT, requestedPosition + " outside upper hardware limit of "
 					+ upperLimit));
 		}
 	}
 
 	/**
 	 * Checks if limits should be checked. The Epics convention is that if the dial high/low limits are zero, this means
 	 * there are no limits.
 	 * 
 	 * @return true unless both dial limits are 0
 	 */
 	private boolean hasLimitsToCheck() throws MotorException {
 		final double dialHighLimit = getDialHighLimit();
 		final double dialLowLimit = getDialLowLimit();
 
 		return (!(dialHighLimit == 0 && dialLowLimit == 0));
 	}
 
 	/**
 	 * Sets the minimum position. This does write to EPICS database.
 	 * 
 	 * @param minimumPosition
 	 *            the minimum position
 	 */
 	public void setMinPosition(double minimumPosition) throws MotorException {
 		try {
 			controller.caput(llm, minimumPosition);
 			this.minPosition = minimumPosition;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to set min position", ex);
 		}
 	}
 
 	/**
 	 * {@inheritDoc} Get the minimum position from EPICS database.
 	 * 
 	 * @return the minimum position, or NaN if limits are not be checked
 	 */
 	@Override
 	public double getMinPosition() throws MotorException {
 		if (!hasLimitsToCheck()) {
 			return Double.NaN;
 		}
 		try {
 			return Double.isNaN(minPosition) ? (minPosition=controller.cagetDouble(llm)) : minPosition;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get min position", ex);
 		}
 	}
 
 	/**
 	 * Sets the maximum position. This does write to EPICS database.
 	 * 
 	 * @param maximumPosition
 	 *            the maximum position
 	 */
 	public void setMaxPosition(double maximumPosition) throws MotorException {
 		try {
 			controller.caput(hlm, maximumPosition);
 			this.maxPosition = maximumPosition;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to set max position", ex);
 		}
 	}
 
 	/**
 	 * {@inheritDoc} Get the maximum position from EPICS database.
 	 * 
 	 * @return the maximum position, or NaN if limits are not be checked
 	 */
 	@Override
 	public double getMaxPosition() throws MotorException {
 		if (!hasLimitsToCheck()) {
 			return Double.NaN;
 		}
 		try {
 			return Double.isNaN(maxPosition) ? (maxPosition=controller.cagetDouble(hlm)) : maxPosition;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get max position", ex);
 		}
 	}
 
 	/**
 	 * Stops the motor
 	 */
 	@Override
 	public void stop() throws MotorException {
 		try {
 			if (configured)
 				controller.caput(stop, 1);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to stop", ex);
 		}
 	}
 
 	/**
 	 * Tells the motor record to stop trying to move the motor and then resets it.
 	 * <p>
 	 * This is different to a normal stop and should be used when the motor is 'stuck' in a moving state. This is the
 	 * same as using the Combo box control in the edm screens.
 	 * <p>
 	 * This is for EpicsMotor specific error handling and would probably only need to be used when there are underlying
 	 * hardware issues.
 	 */
 	public void stopGo() throws MotorException {
 		try {
 			controller.caputWait(spmg, "Stop");
 			controller.caput(spmg, "Go");
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to stop", ex);
 		}
 	}
 
 	/**
 	 * Some motors offer a control for emergence stop which stop the motor and switch off the power. This is not
 	 * implemented here for EPICS motor, i.e. code block is empty.
 	 */
 	@Override
 	public void panicStop() throws MotorException {
 		// noop
 	}
 
 	@Override
 	public void moveContinuously(int direction) throws MotorException {
 		// TODO check if this implementation correct
 		try {
 			if (direction > 0) {
 				moveTo(controller.cagetFloat(hlm));
 			} else {
 				moveTo(controller.cagetFloat(llm));
 			}
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to move continuously", ex);
 		}
 	}
 
 	/**
 	 * This method sets the current position of the motor without moving it, in user coordinates.
 	 * 
 	 * @param position
 	 *            - the new position in motor units
 	 */
 	@Override
 	public void setPosition(double position) throws MotorException {
 		try {
 			// set the drive field without moving motor
 			controller.caput(offset, position);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to set offset position", ex);
 		}
 	}
 
 	/**
 	 * This method returns the current position of the motor in user coordinates.
 	 * 
 	 * @return the current position
 	 */
 	@Override
 	public double getPosition() throws MotorException {
 		try {
 			currentPosition = controller.cagetDouble(rbv);
 			return currentPosition;
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to get position", ex);
 		}
 	}
 
 	@Override
 	public void home() throws MotorException {
 		try {
 			controller.caput(homf, 1, channelManager);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "failed to home", ex);
 		}
 	}
 
 	@Override
 	public void initializationCompleted() {
 		try {
 			// indicate that the channels are connected
 			setInitialised(true);
 			// if retry dead band does not set, using motor resolution as this
 			// dead band.
 			if ((retryDeadband = getRetryDeadband()) == 0) {
 				retryDeadband = getMotorResolution();
 			}
 		} catch (MotorException e) {
 			logger.error("Can not get retry deadband value from EPICS " + rdbd.getName());
 		}
 		if (retryDeadband == 0) {
 			logger.warn("EPICS motor " + getName() + " retry Deadband is set to " + retryDeadband);
 		} else {
 			logger.debug("EPICS motor " + getName() + " retry Deadband is set to " + retryDeadband);
 		}
 		try {
 			moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, MotorStatus.READY, STATUSCHANGE_REASON.INITIALISE);
 		} catch (Exception ex) {
 			exceptionUtils.logException(logger, "initializationCompleted exception ", ex);
 		}
 	}
 
 	private class RBVMonitorListener implements MonitorListener {
 		
 		private boolean alarmRaised = false;
 
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			try {
 				DBR dbr = mev.getDBR();
 				if (dbr.isTIME()) {
 					currentPosition = CompoundDataTypeHandler.getDouble(dbr)[0];
 					status = STSHandler.getStatus(dbr);
 					severity = STSHandler.getSeverity(dbr);
 					timestamp = TIMEHandler.getTimeStamp(dbr);
 					notifyIObservers(MotorProperty.POSITION, new Double(currentPosition));
 				} else {
 					logger.error("Error: Motor Alarm should return DBRTime value.");
 				}
 
 				if (status != Status.NO_ALARM || severity != Severity.NO_ALARM) {
 					if (!alarmRaised) {
 						logger.error("Motor - " + getName() + " raises Alarm at " + timestamp.toMONDDYYYY() + " : "
 								+ "Status=" + status.getName() + "; Severity=" + severity.getName());
 						alarmRaised = true;
 					}
 				} else {
 					alarmRaised = false;
 				}
 
 			} catch (Exception ex) {
 				exceptionUtils.logException(logger, ex);
 			}
 		}
 	}
 
 	// all the field bits in MSTA
 	// private long MSTA_DIRECTION_POSITIVE = 0x1;
 	private long MSTA_DONE = 0x2;
 	private long MSTA_UPPER_LIMIT = 0x4;
 	// private long MSTA_HOME_LIMIT = 0x8;
 	// private long MSTA_UNUSED = 0x10;
 	// private long MSTA_CLOSED_LOOP = 0x20;
 	private long MSTA_FOLLOWING_ERROR = 0x40;
 	// private long MSTA_AT_HOME = 0x80;
 	// private long MSTA_ENCODER_PRESENT = 0x100;
 	private long MSTA_FAULT = 0x200;
 	// private long MSTA_MOVING = 0x400;
 	// private long MSTA_GAIN_SUPPORT = 0x800;
 	private long MSTA_COMMS_ERROR = 0x1000;
 	private long MSTA_LOWER_LIMIT = 0x2000;
 	private long MSTA_HOMED = 0x4000;
 	/*
 	 * This implementation implies there is an order of importance in the returning bits.
 	 */
 	private MotorStatus lastMotorStatus = MotorStatus.UNKNOWN;
 
 	public double dialHighLimit=Double.NaN;
 
 	public double dialLowLimit=Double.NaN;
 
 	private MotorStatus getMotorStatusFromMSTAValue(double msta) {
 		MotorStatus status = MotorStatus.UNKNOWN;
 		long lmsta = (long) msta;
 		logger.debug("status string from motor  = {}", Long.toHexString(lmsta));
 		if ((lmsta & MSTA_FAULT) != 0 || (lmsta & MSTA_FOLLOWING_ERROR) != 0 || (lmsta & MSTA_COMMS_ERROR) != 0) {
 			status = MotorStatus.FAULT;
 			if (!lastMotorStatus.equals(status))
 				logger.error("Motor - {} is at FAULT. Please check EPICS motor status.", getName());
 		} else if ((lmsta & MSTA_UPPER_LIMIT) != 0 || (lmsta & MSTA_LOWER_LIMIT) != 0) {
 			status = checkUserLimitSwitches(lmsta);	
 			if (!lastMotorStatus.equals(status))
 				logger.warn("Motor - {} is at LOWERLIMIT.", getName());
 		} else if ((lmsta & MSTA_DONE) != 0) {
 			status = MotorStatus.READY;
 			if (!lastMotorStatus.equals(status))
 				logger.debug("Motor - {} is READY.", getName());
 		}
 		lastMotorStatus = status;
 		return status;
 	}
 
 	private MotorStatus checkUserLimitSwitches(long lmsta) {
 		// prefer to check the user limit switches, not the low-level MSTA PV, just in case limits flipped inside the motor record
 		try {
 			short highLevelSwitch  = controller.cagetShort(hls);
 			if (highLevelSwitch == 1)
 				return MotorStatus.UPPERLIMIT;
 			short lowerLevelSwitch  = controller.cagetShort(lls);
 			if (lowerLevelSwitch == 1)
 				return MotorStatus.LOWERLIMIT;
 		} catch (Exception e) {
 			logger.error("Exception when trying to get the user limit switches", e);
 		}
 		
 		if ((lmsta & MSTA_UPPER_LIMIT) != 0) {
 			return MotorStatus.UPPERLIMIT;
 		}
 		return MotorStatus.LOWERLIMIT;
 	}
 
 	public boolean isHomedFromMSTAValue(double msta) {
 		return ((long) msta & MSTA_HOMED) != 0;
 	}
 
 	@Override
 	public boolean isHomed() { // cannot throw checked exceptions
 		if (homed != null) {
 			return homed;
 		}
 		try {
 			homed = isHomedFromMSTAValue(readMsta());
 		} catch (Throwable e) {
 			logger.error(getName()
 					+ " could not read MSTA record to get homed status (swallowed exception--RETURNING UNHOMED)", e);
 			return false;
 		}
 		return homed;
 	}
 
 	public double readMsta() throws TimeoutException, CAException, InterruptedException {
 		return controller.cagetShort(msta);
 	}
 
 	private class DMOVMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 
 			try {
 				int dmovValue = -1;
 				DBR dbr = mev.getDBR();
 				if (dbr.isSHORT()) {
 					dmovValue = ((DBR_Short) dbr).getShortValue()[0];
 				} else {
 					logger.error("Error: .DMOV should return SHORT type value.");
 				}
 				if (getStatus() == MotorStatus.BUSY) {
 					if (dmovValue == 0) {
 						// logger.debug("Motor {} is moving ", getName());
 					} else if (dmovValue == 1) {
 						// logger.debug("Motor {} is stopped at {}.", getName(),
 						// currentPosition);
 					} else {
 						logger.error("Error: illegal .DMOV value." + dmovValue);
 					}
 				} else {
 					/*
 					 * We cannot change the status as that is only to be looked after by the caput listener. Instead we
 					 * simply cause the positioner to refresh.
 					 */
 					if (dmovValue == 1 && DMOVRefreshEnabled) {
 						MotorStatus ms = mstaMonitorListener.getStatus();
 						moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, ms, STATUSCHANGE_REASON.DMOV_MOVECOMPLETE);
 					}
 				}
 			} catch (Exception e) {
 				exceptionUtils.logException(logger, "Error in DMOV monitor for " + getName(), e);
 			}
 		}
 	}
 
 	@SuppressWarnings("unused")
 	// TODO Not sure if this will be used in future
 	private double getTargetPosition() throws MotorException {
 		try {
 			return controller.cagetDouble(val);
 		} catch (Throwable e) {
 			throw new MotorException(getStatus(), "failed to get target position", e);
 		}
 	}
 
 	private void setMinPositionFromListener(double minPosition) {
 		this.minPosition = minPosition;
 		notifyIObservers(MotorProperty.LOWLIMIT, new Double(minPosition));
 	}
 
 	private void setMaxPositionFromListener(double maxPosition) {
 		this.maxPosition = maxPosition;
 		notifyIObservers(MotorProperty.HIGHLIMIT, new Double(maxPosition));
 	}
 
 	/**
 	 * updates the lower soft limit when and if it changes in EPICS.
 	 */
 	private class LLMMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if (dbr.isFLOAT()) {
 				setMinPositionFromListener(new Float(((DBR_Float) dbr).getFloatValue()[0]).doubleValue());
 			} else if (dbr.isDOUBLE()) {
 				setMinPositionFromListener(((DBR_Double) dbr).getDoubleValue()[0]);
 			} else {
 				logger.error("Error: illegal .LLM value.");
 			}
 
 		}
 	}
 
 	private enum SetUseState {
 		UNKNOWN,
 		SET,
 		USE
 	}
 	
 	private class SetUseMonitorListener implements MonitorListener {
 		
 		private static final short SET_USE_PV_USE_VALUE = 0;
 		
 		private static final short SET_USE_PV_SET_VALUE = 1;
 		
 		private SetUseState setUseMode = SetUseState.UNKNOWN;
 		
 		private final ReadWriteLock setUseLock = new ReentrantReadWriteLock();
 		
 		@Override
 		public void monitorChanged(MonitorEvent event) {
 			
 			final DBR dbr = event.getDBR();
 			
 			if (!dbr.isENUM()) {
 				logger.error(String.format("New value for %s SET PV has type %s; expected %s", getName(), dbr.getType().getName(), DBRType.ENUM.getName()));
 				return;
 			}
 			
 			final DBR_Enum dbrEnum = (DBR_Enum) dbr;
 			final short[] values = dbrEnum.getEnumValue();
 			
 			if (values.length != 1) {
 				logger.error(String.format("New value for %s SET PV has %d value(s); expected 1", getName(), values.length));
 				return;
 			}
 			
 			final short newValue = values[0];
 			
 			if (newValue != SET_USE_PV_USE_VALUE && newValue != SET_USE_PV_SET_VALUE) {
 				logger.error(String.format("New value for %s SET PV is %d; expected %d or %d", getName(), newValue, SET_USE_PV_USE_VALUE, SET_USE_PV_SET_VALUE));
 				return;
 			}
 			
 			try {
 				setUseLock.writeLock().lock();
 				
 				final boolean firstUpdate = (setUseMode == SetUseState.UNKNOWN);
 				
 				final SetUseState newState = (newValue == SET_USE_PV_USE_VALUE) ? SetUseState.USE : SetUseState.SET;
 				
 				final boolean stateChanged = !firstUpdate && (setUseMode != newState);
 				
 				final boolean logNewState = (firstUpdate && newState == SetUseState.SET) || stateChanged;
 				
 				if (logNewState) {
 					
 					if (newState == SetUseState.USE) {
 						logger.info(String.format("Motor %s is now in 'Use' mode", getName()));
 					}
 					
 					else if (newState == SetUseState.SET) {
 						logger.error(String.format("Motor %s is now in 'Set' mode - this will cause moves to fail", getName()));
 					}
 				}
 				
 				setUseMode = newState;
 			}
 			
 			finally {
 				setUseLock.writeLock().unlock();
 			}
 		}
 		
 		private void checkMotorIsInUseMode() throws MotorException {
 			
 			try {
 				setUseLock.readLock().lock();
 				
 				if (setUseMode == SetUseState.SET) {
 					throw new MotorException(getStatus(), String.format("Motor %s is in 'Set' mode - check the Set/Use PV in the motor's EDM screen", getName()));
 				}
 			}
 			
 			finally {
 				setUseLock.readLock().unlock();
 			}
 		}
 	
 	}
 	
 	private class MSTAMonitorListener implements MonitorListener {
 		private MotorStatus mstaStatus = MotorStatus.READY;
 
 		public MotorStatus getStatus() {
 			return mstaStatus;
 		}
 
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			try {
 				DBR dbr = mev.getDBR();
 				if (dbr.isDOUBLE()) {
 					double msta = ((DBR_Double) dbr).getDoubleValue()[0]; // TODO why doubkle !!??
 					MotorStatus status = getMotorStatusFromMSTAValue(msta);
 					if ((status == MotorStatus.READY || status == MotorStatus.LOWERLIMIT
 							|| status == MotorStatus.UPPERLIMIT || status == MotorStatus.FAULT)) {
 						mstaStatus = status;
 						moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, status, STATUSCHANGE_REASON.NEWSTATUS);
 
 					}
 					homed = isHomedFromMSTAValue(msta);
 				} else {
 					logger.error("Error: .RBV should return DOUBLE type value.");
 				}
 			} catch (Exception ex) {
 				exceptionUtils.logException(logger, ex);
 			}
 		}
 	}
 
 	/**
 	 * update upper soft limit when and if it changes in EPICS.
 	 */
 	private class HLMMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if (dbr.isFLOAT()) {
 				setMaxPositionFromListener(new Float(((DBR_Float) dbr).getFloatValue()[0]).doubleValue());
 			} else if (dbr.isDOUBLE()) {
 				setMaxPositionFromListener(((DBR_Double) dbr).getDoubleValue()[0]);
 			} else {
 				logger.error("Error: illegal .HLM value.");
 			}
 		}
 	}
 
 	/**
 	 * update upper dial limit when and if it changes in EPICS.
 	 */
 	private class DHLMMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if  (dbr.isDOUBLE()) {
 				dialHighLimit = ((DBR_Double) dbr).getDoubleValue()[0];
 			} else {
 				logger.error("Error: illegal .DHLM value.");
 			}
 		}
 	}
 
 	/**
 	 * update lower dial limit when and if it changes in EPICS.
 	 */
 	private class DLLMMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			DBR dbr = mev.getDBR();
 			if  (dbr.isDOUBLE()) {
 				dialLowLimit = ((DBR_Double) dbr).getDoubleValue()[0];
 			} else {
 				logger.error("Error: illegal .DLLM value.");
 			}
 		}
 	}
 	
 	
 	/**
 	 * updates limit violation status from EPICS.
 	 */
 	private class LVIOMonitorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent mev) {
 			int value = -1;
 			DBR dbr = mev.getDBR();
 			if (dbr.isSHORT()) {
 				value = ((DBR_Short) dbr).getShortValue()[0];
 			} else {
 				logger.error("Error: expecting Int type but got " + dbr.getType() + " type.");
 			}
 
 			if (value == 1) {
 				logger.warn("EPICS motor {} raises Limit Violation.", getName());
 			}
 			// notifyIObservers(MotorProperty.STATUS, motorStatus);
 		}
 	}
 
 	/**
 	 * This class defines the call back handler for an asynchronous motor move request. It sets motor status to FAULT if
 	 * put failed, or target is missed when missing target is not permitted. It also check the motor access status, if
 	 * its access is DISABLED, it will suspend the current scan or script before setting motor status to READY in order
 	 * to prevent sending next point request to a already disabled motor. It notifies all its observers of these motor
 	 * status (critical to GDA DOF locking release). This class is designed to support both scan and GUI driven
 	 * processes.
 	 */
 	private class PutCallbackListener implements PutListener {
 		volatile PutEvent event = null;
 
 		@Override
 		public void putCompleted(PutEvent ev) {
 			MotorStatus newStatus = MotorStatus.READY;
 			try {
 				logger.debug("{}: callback received >>>", getName());
 				event = ev;
 				if (isCallbackWait()) { // delay is needed to DCM energy update in EPICS
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 				if (event.getStatus() != CAStatus.NORMAL) {
 					logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
 							event.getStatus());
 					newStatus = MotorStatus.FAULT;
 				} else {
 					// if access is disabled we must pause before set motor status
 					// to READY to prevent sending the next scan point request. This
 					// also ensure the current point reading complete before pausing
 					if (acs == AccessControl.Status.DISABLED) {
 						if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
 							JythonServerFacade.getInstance().pauseCurrentScan();
 							JythonServerFacade.getInstance().print(
 									"current scan paused after motor " + getName() + " is disabled.");
 						}
 						if (JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING) {
 							JythonServerFacade.getInstance().pauseCurrentScript();
 							JythonServerFacade.getInstance().print(
 									"current script paused after motor " + getName() + " is disabled.");
 						}
 					}
 					if (retryDeadband != 0) {
 						if (targetPosition - retryDeadband <= currentPosition
 								&& currentPosition <= targetPosition + retryDeadband) {
 							// do nothing
 						} else {
 							if (checkMissedTarget) {
 								logger.error("{} : target requested is missed. Report to Engineer", getName());
 								// missing target not allowed.
 								newStatus = MotorStatus.FAULT;
 							}
 						}
 					} else {
 						logger.warn("{} motor's retry deadband is {}. Motor may miss its target.", getName(),
 								retryDeadband);
 					}
 				}
 
 				if (status == Status.NO_ALARM && severity == Severity.NO_ALARM) {
 					moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, newStatus,
 							STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE);
 				} else {
 					// if Alarmed, check and report MSTA status
 					moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, null,
 							STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE_IN_ERROR);
 				}
 
 			} catch (Exception ex) {
 				exceptionUtils.logException(logger, "Error in putCompleted for " + getName(), ex);
 			}
 		}
 
 	}
 
 	/**
 	 * gets the short or EPICS-GDA shared name of the device
 	 * 
 	 * @return device name
 	 */
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	/**
 	 * sets the short or EPICS-GDA shared name for this device
 	 */
 	public void setDeviceName(String deviceName) {
 		this.deviceName = deviceName;
 	}
 
 	/**
 	 * gets EPICS access control name.
 	 * 
 	 * @return name of the access control.
 	 */
 	public String getAccessControlName() {
 		return accessControlName;
 	}
 
 	/**
 	 * sets the EPICS access control name.
 	 */
 	public void setAccessControlName(String accessControlName) {
 		this.accessControlName = accessControlName;
 	}
 
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		if (theObserved instanceof AccessControl && theObserved == accessControl && !accessControl.isDefaultAction()) {
 			// set the access control flag of this object
 			this.acs = (AccessControl.Status) changeCode;
 			if ((AccessControl.Status) changeCode == AccessControl.Status.ENABLED) {
 				logger.info("Beamline control of the device " + getName() + " is enabled.");
 				if (JythonServerFacade.getInstance().getScanStatus() == Jython.PAUSED) {
 					JythonServerFacade.getInstance().resumeCurrentScan();
 					JythonServerFacade.getInstance().print(
 							"current scan resumed after motor: " + getName() + " is enabled.");
 				}
 				if (JythonServerFacade.getInstance().getScriptStatus() == Jython.PAUSED) {
 					JythonServerFacade.getInstance().resumeCurrentScript();
 					JythonServerFacade.getInstance().print(
 							"current script resumed after motor: " + getName() + " is enabled.");
 				}
 			} else if ((AccessControl.Status) changeCode == AccessControl.Status.DISABLED) {
 				logger.warn("Beamline control of the device " + getName() + " is disabled.");
 			}
 		}
 		notifyIObservers(theObserved, changeCode);
 	}
 
 	/**
 	 * Constructor taking a PV name
 	 * 
 	 * @param name
 	 *            - String, the PV name
 	 */
 	public EpicsMotor(String name) {
 		this();
 		setName(name);
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + "-" + getName();
 	}
 
 	/**
 	 * Sets the Epics Record Name, used by CASTOR.
 	 */
 	public void setEpicsRecordName(String epicsRecordName1) {
 		this.epicsRecordName = epicsRecordName1;
 	}
 
 	/**
 	 * Gets the Epics Record Name.
 	 */
 	public String getEpicsRecordName() {
 		return epicsRecordName;
 	}
 
 	/**
 	 * checks if missing target permitted or not.
 	 */
 	public boolean isCheckMissedTarget() {
 		return checkMissedTarget;
 	}
 
 	/**
 	 * sets permission for missing target for motor moving
 	 */
 	public void setCheckMissedTarget(boolean ignorMissedTarget) {
 		this.checkMissedTarget = ignorMissedTarget;
 	}
 
 	public boolean isCallbackWait() {
 		return callbackWait;
 	}
 
 	public void setCallbackWait(boolean callbackDelay) {
 		this.callbackWait = callbackDelay;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (!(other instanceof EpicsMotor)) {
 			return false;
 		}
 		return this == other;
 	}
 
 	public String getPV() throws InterfaceException {
 		return GDAEpicsInterfaceReader.getPVFromSimpleMotor(getDeviceName());
 	}
 
 	public void setAssertHomedBeforeMoving(boolean assertHomedBeforeMoving) {
 		this.assertHomedBeforeMoving = assertHomedBeforeMoving;
 	}
 
 	public boolean isAssertHomedBeforeMoving() {
 		return assertHomedBeforeMoving;
 	}
 
 	public void set_motorStatus(MotorStatus _motorStatus) {
 		synchronized (_motorStatusMonitor) {
 			this._motorStatus = _motorStatus;
 			this._motorStatusMonitor.notifyAll();
 		}
 	}
 
 	public MotorStatus get_motorStatus() {
 		return _motorStatus;
 	}
 
 	@Override
 	public MotorStatus waitWhileStatusBusy() throws InterruptedException {
 		synchronized (_motorStatusMonitor) {
 			while (_motorStatus.value() == MotorStatus._BUSY) {
 				_motorStatusMonitor.wait();
 			}
 			return _motorStatus;
 		}
 	}
 	@Override
 	public void reconfigure() throws FactoryException {
 		if (!isConfigured()) {
 			configure();
 		}
 	}
 }
 
 class MoveEventQueue implements Runnable {
 	private static final Logger logger = LoggerFactory.getLogger(MoveEventQueue.class);
 	Vector<MoveEvent> items = new Vector<MoveEvent>();
 	private final MoveEvent[] itemsToBeHandledType = new MoveEvent[0];
 	private boolean killed = false;
 	private Thread thread = null;
 
 	public void addMoveCompleteEvent(EpicsMotor motor, MotorStatus newStatus, EpicsMotor.STATUSCHANGE_REASON reason)
 			throws MotorException {
 		synchronized (items) {
 			logger.debug("Motor - " + motor.getName() + " addMoveCompleteEvent." + ". newStatus = "
 					+ (newStatus != null ? newStatus.toString() : "null") + ". reason = "
 					+ (reason != null ? EpicsMotor.reasonAsString(reason) : "null"));
 			/*
 			 * If reason = MOVETO then we need to perform the actual move now in the calling thread so that exceptions
 			 * can be passed back to the caller. We set status here to busy so that any DMOV =1 events that happen
 			 * between now and the caput callback do not cause the positioner to unlock early. Note that the positioner
 			 * is locked by the calling thread so no DMOV events can change status until this thread releases the lock.
 			 */
 			if (reason == EpicsMotor.STATUSCHANGE_REASON.MOVETO) {
 				motor.changeStatusAndNotify(MotorStatus.BUSY, EpicsMotor.STATUSCHANGE_REASON.START_MOVETO);
 			}
 			/*
 			 * only add if an item for the same motor and status does not already exist
 			 */
 			boolean add = true;
 			Iterator<MoveEvent> iter = items.iterator();
 			while (iter.hasNext()) {
 				MoveEvent item = iter.next();
 				if (item.motor == motor && item.reason == reason) {
 					// status is unknown if CAPUT_MOVECOMPLETE_IN_ERROR
 					if (reason != STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE_IN_ERROR) {
 						if ((item.newStatus == null && newStatus == null)
 								|| (item.newStatus != null && newStatus != null && item.newStatus.equals(newStatus))) {
 							add = false;
 							break;
 						}
 					}
 				}
 			}
 			if (add) {
 				items.add(new MoveEvent(motor, newStatus, reason));
 				if (thread == null) {
 					thread = uk.ac.gda.util.ThreadManager.getThread(this);
 					thread.start();
 				}
 				items.notifyAll();
 			}
 		}
 	}
 
 	public void dispose() {
 		killed = true;
 	}
 
 	@Override
 	public void run() {
 		while (!killed) {
 			try {
 				MoveEvent[] itemsToBeHandled = null;
 				synchronized (items) {
 					if (!killed && items.isEmpty())
 						items.wait();
 					if (!items.isEmpty()) {
 						itemsToBeHandled = items.toArray(itemsToBeHandledType);
 						items.clear();
 					}
 				}
 				if (itemsToBeHandled != null) {
 					int numItems = itemsToBeHandled.length;
 					for (int index = 0; index < numItems; index++) {
 						try {
 							MoveEvent item = itemsToBeHandled[index];
 							item.motor.changeStatusAndNotify(item.newStatus, item.reason);
 						} catch (Exception ex) {
 							exceptionUtils.logException(logger, "changeStatusAndNotify exception", ex);
 						}
 					}
 				}
 			} catch (Throwable th) {
 				exceptionUtils.logException(logger, "EpicsMotor.MoveCompleteEventQueue run exception ", th);
 			}
 		}
 	}
 	
 }
 
 class MoveEvent {
 	final MotorStatus newStatus;
 	final EpicsMotor motor;
 	final EpicsMotor.STATUSCHANGE_REASON reason;
 
 	MoveEvent(EpicsMotor motor, MotorStatus newStatus, EpicsMotor.STATUSCHANGE_REASON reason) {
 		this.motor = motor;
 		this.newStatus = newStatus;
 		this.reason = reason;
 	}
 
 }
