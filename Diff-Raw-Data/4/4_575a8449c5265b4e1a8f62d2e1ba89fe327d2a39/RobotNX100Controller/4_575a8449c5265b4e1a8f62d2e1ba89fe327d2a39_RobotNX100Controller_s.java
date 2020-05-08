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
 
 package gda.device.robot;
 
 import gda.configuration.epics.ConfigurationNotFoundException;
 import gda.configuration.epics.Configurator;
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.epics.connection.EpicsChannelManager;
 import gda.epics.connection.EpicsController;
 import gda.epics.connection.InitializationListener;
 import gda.epics.interfaces.Nx100Type;
 import gda.factory.Configurable;
 import gda.factory.FactoryException;
 import gda.factory.Findable;
 import gov.aps.jca.CAException;
 import gov.aps.jca.CAStatus;
 import gov.aps.jca.Channel;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBR_Int;
 import gov.aps.jca.dbr.DBR_String;
 import gov.aps.jca.event.MonitorEvent;
 import gov.aps.jca.event.MonitorListener;
 import gov.aps.jca.event.PutEvent;
 import gov.aps.jca.event.PutListener;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * RobotNX100Controller Class
  */
 public class RobotNX100Controller extends DeviceBase implements Configurable, Findable, InitializationListener {
 
 	/**
 	 * logging instance
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(RobotNX100Controller.class);
 	/**
 	 * Maximum number of samples
 	 */
 	public static final int MAX_NUMBER_OF_SAMPLES = 200;
 	/**
 	 * Minimum number of samples
 	 */
 	public static final int MIN_NUMBER_OF_SAMPLES = 1;
 
 	/**
 	 * Jobs that robot performs
 	 */
 	public enum Job {
 		
 		/**
 		 * Recover from unknown state
 		 */
 		RECOVER,
 		
 		/**
 		 * Pick a sample from the carousel.
 		 */
 		PICKC,
 		
 		/**
 		 * Place a sample onto the carousel.
 		 */
 		PLACEC,
 		
 		/**
 		 * Pick a sample from the diffractometer.
 		 */
 		PICKD,
 		
 		/**
 		 * Place a sample onto the diffractometer.
 		 */
 		PLACED,
 		GRIPO,
 		GRIPC,
 		TABLEIN,
 		TABLEOUT,
 		UNLOAD
 		
 	}
 
 	/**
 	 * States that a sample can be in
 	 */
 	public enum SampleState {
 		
 		/**
 		 * Sample is on the carousel.
 		 */
 		ONCAROUSEL,
 		
 		/**
 		 * Sample is gripped by the robot.
 		 */
 		ONGRIP,
 		
 		/**
 		 * Sample is on the diffractometer.
 		 */
 		ONDIFF
 	}
 
 	// control fields
 	/**
 	 * robot job selection 5 positions enum
 	 */
 	private Channel jobChannel;
 	/**
 	 * start trigger, write 1 to start, double
 	 */
 	private Channel startChannel;
 	/**
 	 * hole or stop, write 1 to hole and 0 to release or enable "start"
 	 */
 	private Channel holdChannel;
 	/**
 	 * servos on/off control, write 1 - on, 0 - off
 	 */
 	private Channel svonChannel;
 	/**
 	 * error string if "start" command fails
 	 */
 	private Channel errChannel;
 
 	/**
 	 * EPICS controller for CA methods
 	 */
 	private EpicsController controller;
 	/**
 	 * EPICS Channel Manager
 	 */
 	private EpicsChannelManager channelManager;
 	/**
 	 * phase II interface GDA-EPICS link parameter
 	 */
 	private String deviceName;
 
 	private ErrorListener errls;
 	private String errorString;
 
 	private HashMap<String, String> errorMap = new HashMap<String, String>();
 	private String errorCodeFilename;
 	private int numberOfRows;
 	private ArrayList<String> keys;
 	private volatile boolean jobDone = false;
 	private PutCallbackListener pcbl;
 
 	/**
 	 * Constructor
 	 */
 	public RobotNX100Controller() {
 		controller = EpicsController.getInstance();
 		channelManager = new EpicsChannelManager(this);
 		pcbl = new PutCallbackListener();
 		errls = new ErrorListener();
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 		if (!configured) {
 			if (getDeviceName() != null) {
 				// phase II beamlines interface using GDA's deviceName.
 				Nx100Type nxConfig;
 				try {
 					nxConfig = Configurator.getConfiguration(getDeviceName(), Nx100Type.class);
 					createChannelAccess(nxConfig);
 					channelManager.tryInitialize(100);
 				} catch (ConfigurationNotFoundException e) {
 					logger.error("Can NOT find EPICS configuration for Robot Controller " + getDeviceName(), e);
 					throw new FactoryException("Epics Robot Controller " + getDeviceName() + " not found");
 				}
 			} // Nothing specified in Server XML file
 			else {
 				logger.error("Missing EPICS configuration for Robot Control {}", getName());
 				throw new FactoryException("Missing EPICS configuration for Robot Control " + getName());
 			}
 			readTheFile();
 			configured = true;
 		}
 	}
 
 	/**
 	 * Reads the lookup table file and put them into a multi-Valued Map for looking up value for the specified energy
 	 * and scannable name.
 	 * 
 	 */
 	public void readTheFile() {
 		errorMap.clear();
 
 		BufferedReader br;
 		String nextLine;
 		String[] header = null;
 		ArrayList<String> lines = new ArrayList<String>();
 		try {
 			logger.debug("{} loading file: {} ", getName(), RobotNX100Controller.class.getResource("motoman_error_code.txt").getPath());
 
 			br = new BufferedReader(new FileReader(RobotNX100Controller.class.getResource("motoman_error_code.txt").getPath()));
 			while (((nextLine = br.readLine()) != null) && (nextLine.length() > 0)) {
 				if (nextLine.startsWith("Code")) {
 					header = nextLine.split("[, \t][, \t]*");
 				} else if (!nextLine.startsWith("#"))
 					lines.add(nextLine);
 			}
 			br.close();
 		} catch (FileNotFoundException fnfe) {
 			// we do not want to interrupt processing because error map file not set.
 			logger.warn("Can not find the Error Message file {} for {}. Only Error code will be reported.",
 					getErrorCodeFilename(), getName());
 			logger.warn("caused by " + fnfe.getMessage(), fnfe);
 			br = null;
 			return;
 		} catch (IOException ioe) {
 			// we do not want to interrupt processing because error map file not set.
 			logger.warn("Can not find the Error Message file {} for {}. Only Error code will be reported.",
 					getErrorCodeFilename(), getName());
 			logger.error("caused by " + ioe.getMessage(), ioe);
 			br = null;
 			return;
 		}
 
 		numberOfRows = lines.size();
 		logger.debug("the file contained " + numberOfRows + " lines");
 		int nColumns = new StringTokenizer(lines.get(0), "\t").countTokens();
 		logger.debug("each line should contain " + nColumns + " numbers");
 		keys = new ArrayList<String>();
 		if (header != null) {
 			for (int i = 1; i < nColumns; i++) {
 				errorMap.put(header[0], header[i]);
 			}
 			keys.add(header[0]);
 		}
 
 		for (int i = 0; i < numberOfRows; i++) {
 			nextLine = lines.get(i);
 			String[] thisLine = nextLine.split("[\t][\t]*");
 			for (int j = 0; j < thisLine.length; j++)
 				errorMap.put(thisLine[0], thisLine[j]);
 			keys.add(thisLine[0]);
 		}
 	}
 
 	/**
 	 * @param errorCode
 	 * @return String
 	 */
 	public String lookupErrorCode(String errorCode) {
 		return errorMap.get(errorCode);
 	}
 
 	/**
 	 * @return HashMap
 	 */
 	public HashMap<String, String> getErrorMap() {
 		return errorMap;
 	}
 
 	/**
 	 * creates all required channels
 	 * 
 	 * @param config
 	 * @throws FactoryException
 	 */
 	private void createChannelAccess(Nx100Type config) throws FactoryException {
 		try {
 			jobChannel = channelManager.createChannel(config.getJOB().getPv(), false);
 			startChannel = channelManager.createChannel(config.getSTART().getPv(), false);
 			holdChannel = channelManager.createChannel(config.getHOLD().getPv(), false);
 			svonChannel = channelManager.createChannel(config.getSVON().getPv(), false);
 			errChannel = channelManager.createChannel(config.getERR().getPv(), errls, false);
 
 			// acknowledge that creation phase is completed
 			channelManager.creationPhaseCompleted();
 		} catch (Throwable th) {
 			throw new FactoryException("failed to create all channels", th);
 		}
 	}
 
 	/**
 	 * sets the Job to do without apply it to robot controller
 	 * 
 	 * @param job
 	 * @throws DeviceException
 	 */
 	public void setJob(Job job) throws DeviceException {
 		try {
 			controller.caput(jobChannel, job.ordinal(), 2);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to set JOB for the robot", e);
 		}
 	}
 
 	public Job getJob() throws DeviceException {
 		int value =-1;
 		try {
 			try {
 				value = controller.cagetInt(jobChannel);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted on get Job from Epics", e);
 			}
 		} catch (TimeoutException e) {
 			logger.error("Timeout on get Job from EPICS", e);
 			throw new DeviceException("Timeout on get Job from EPICS", e);
 		} catch (CAException e) {
 			logger.error("CA Exception on get Job from EPICS", e);
 			throw new DeviceException("CA Exception on get Job from EPICS", e);
 		}
 		return Job.values()[value];
 	}
 	/**
 	 * starts the robot motion after set Job.
 	 * 
 	 * @throws DeviceException
 	 */
 	public void start() throws DeviceException {
 
 			jobDone  = false;
 			double startTime = System.currentTimeMillis();
 			double currentTime = System.currentTimeMillis();
 			// controller.caput(startChannel, 1, 600);
 			logger.debug("call robot start: {}", currentTime);
 			try {
 				try {
 					controller.caput(startChannel, 1, pcbl);
 				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e);
 				}
 			} catch (CAException e) {
 				logger.error("caput to 'start' failed", e);
 				throw new DeviceException("failed to start the JOB " + this.getJob().toString(), e);
 			}
 			while (!jobDone && (currentTime-startTime) < 60000) {
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					logger.error("robot action interrupted", e);
 					throw new DeviceException("interrupt the JOB " + this.getJob().toString(), e);
 				}
 				currentTime = System.currentTimeMillis();
 			}
 			if ((currentTime-startTime) > 60000) {
 				try {
 					throw new TimeoutException("Request Job Timeout (1min):" + (currentTime-startTime));
 				} catch (TimeoutException e) {
 					logger.error("timeout exception on robot action ", e);
 					throw new DeviceException("timeout exception on robot action " + this.getJob().toString(), e);
 				}
 			}
 
 	}
 
 	/**
 	 * stop or hole robot arm, need to be released before continue.
 	 * 
 	 * @throws DeviceException
 	 */
 	public void hold() throws DeviceException {
 		try {
 			controller.caput(holdChannel, 1, 2.0);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to stop or hole the JOB", e);
 		}
 	}
 
 	/**
 	 * release hold on robot arm
 	 * 
 	 * @throws DeviceException
 	 */
 	public void release() throws DeviceException {
 		try {
 			controller.caput(holdChannel, 0, 2.0);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to release hole on robot", e);
 		}
 	}
 
 	/**
 	 * power on the robot.
 	 * 
 	 * @throws DeviceException
 	 */
 	public void servoOn() throws DeviceException {
 		try {
 			controller.caput(svonChannel, 1, 2.0);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to turn servos on", e);
 		}
 	}
 
 	/**
 	 * power off the robot
 	 * 
 	 * @throws DeviceException
 	 */
 	public void servoOff() throws DeviceException {
 		try {
 			controller.caput(svonChannel, 0, 2.0);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to turn servos on", e);
 		}
 	}
 
 	/**
 	 * get the error code
 	 * 
 	 * @return error code
 	 * @throws DeviceException
 	 */
 	public String getError() throws DeviceException {
 		try {
 			String errorCode = controller.caget(errChannel);
 			return "Error Code " + errorCode + " : " + lookupErrorCode(errorCode);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to get error code from robot", e);
 		}
 	}
 
 	/**
 	 * clear error string
 	 * 
 	 * @throws DeviceException
 	 */
 	public void clearError() throws DeviceException {
 		try {
 			controller.caput(errChannel, 0);
 		} catch (Throwable e) {
 			throw new DeviceException("failed to clear error code from robot", e);
 		}
 	}
 
 	@Override
 	public void initializationCompleted() {
 		logger.info("Robot arm {} is initialised.", getName());
 
 	}
 
 	private class PutCallbackListener implements PutListener {
 		volatile PutEvent event = null;
 
 		@Override
 		public void putCompleted(PutEvent ev) {
 			event = ev;
 
 			if (event.getStatus() != CAStatus.NORMAL) {
 				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
 						.getStatus());
 			} 
 			jobDone = true;
 
 			// for timeouted moveTo
 			//this.notifyAll();
 
 			//notifyIObservers(this, jobDone);
 			logger.debug("{}: Job completed at {}", getName(), System.currentTimeMillis());
 		}
 
 	}
 
 	/**
 	 *
 	 */
 	public class ErrorListener implements MonitorListener {
 		@Override
 		public void monitorChanged(MonitorEvent arg0) {
 			DBR state = arg0.getDBR();
 			if (state.isSTRING()) {
 				errorString = ((DBR_String) state).getStringValue()[0];
 			} else if (state.isINT()) {
 				int errorCode = ((DBR_Int) state).getIntValue()[0];
 				errorString = lookupErrorCode(String.valueOf(errorCode));
 			}
 			else {
 				logger.error("Expecting String from EPICS but got {} ", state.getType());
 				throw new IllegalStateException("Error message return wrong value type" + state.getType());
 			}
 
 			notifyIObservers(this, errorString);
 
 		}
 	}
 
 	/**
 	 * get device name
 	 * 
 	 * @return device name
 	 */
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	/**
 	 * set device name
 	 * 
 	 * @param deviceName
 	 */
 	public void setDeviceName(String deviceName) {
 		this.deviceName = deviceName;
 	}
 
 	// /**
 	// * return if the Job is done or not
 	// * @return
 	// */
 	// public boolean isJobDone() {
 	// return jobDone;
 	// }
 	// /**
 	// * set job done or not.
 	// * @param jobDone
 	// */
 	// public void setJobDone(boolean jobDone) {
 	// this.jobDone = jobDone;
 	// }
 
 	/**
 	 * @param errorCodeFilename
 	 */
 	public void setErrorCodeFilename(String errorCodeFilename) {
 		this.errorCodeFilename = errorCodeFilename;
 	}
 
 	/**
 	 * @return String
 	 */
 	public String getErrorCodeFilename() {
 		return errorCodeFilename;
 	}
 
 }
