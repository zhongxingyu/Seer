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
 
 package gda.device.detector.analyser;
 
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.MCAStatus;
 import gda.device.epicsdevice.EpicsDevice;
 import gda.device.epicsdevice.EpicsMonitorEvent;
 import gda.device.epicsdevice.EpicsRegistrationRequest;
 import gda.device.epicsdevice.FindableEpicsDevice;
 import gda.device.epicsdevice.IEpicsChannel;
 import gda.device.epicsdevice.IEpicsDevice;
 import gda.device.epicsdevice.ReturnType;
 import gda.factory.FactoryException;
 import gda.factory.Findable;
 import gda.factory.Finder;
 import gda.observable.IObserver;
 import gda.util.exceptionUtils;
 import gda.util.converters.CoupledConverterHolder;
 import gda.util.converters.IQuantitiesConverter;
 import gda.util.converters.IQuantityConverter;
 import gov.aps.jca.dbr.DBR;
 import gov.aps.jca.dbr.DBR_Enum;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.jscience.physics.quantities.Quantity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to communicate with an epics MCA record. The MCA record controls and acquires data from a multichannel analyser
  * (MCA). It connects to the Epics channels via an EpicsDevice whose name is set by the method setEpicsDeviceName.
  * Observers are notified of change of status - either MCAStatus.READY or MCAStatus.BUSY getStatus - returns either
  * Detector.IDLE or Detector.BUSY eraseStartAcquisition - starts acquisition
  */
 public class EpicsMCASimple extends AnalyserBase implements IEpicsMCA, Detector, IObserver {
 
 	private static final Logger logger = LoggerFactory.getLogger(EpicsMCASimple.class);
 
 	private static final String SingleRecord = "";
 
 	private static final String NM = "NM"; //$NON-NLS-1$
 
 	private static final String P = "P"; //$NON-NLS-1$
 
 	private static final String N = "N"; //$NON-NLS-1$
 
 	private static final String IP = "IP"; //$NON-NLS-1$
 
 	private static final String BG = "BG"; //$NON-NLS-1$
 
 	private static final String HI = "HI"; //$NON-NLS-1$
 
 	private static final String LO = "LO"; //$NON-NLS-1$
 
 	private static final String R = ".R"; //$NON-NLS-1$
 
 	private static final String readField = ".READ"; //$NON-NLS-1$
 
 	private static final String acquiringField = ".ACQG"; //$NON-NLS-1$
 
 	private static final String stopAcqField = ".STOP"; //$NON-NLS-1$
 
 	private static final String startAcqField = ".STRT"; //$NON-NLS-1$
 
 	private static final String eraseStartAcqField = ".ERST"; //$NON-NLS-1$
 
 	private static final String maxNumberOfChannelsToUseField = ".NMAX"; //$NON-NLS-1$
 
 	private static final String numberOfChannelsToUseField = ".NUSE"; //$NON-NLS-1$
 
 	private static final String procField = ".PROC"; //$NON-NLS-1$
 
 	private static final String readingField = ".RDNG"; //$NON-NLS-1$
 
 	private static final String seqField = ".SEQ"; //$NON-NLS-1$
 
 	private static final String elapsedLiveTimeField = ".ELTM"; //$NON-NLS-1$
 
 	private static final String elapsedRealTimeField = ".ERTM"; //$NON-NLS-1$
 
 	private static final String dataField = ".VAL"; //$NON-NLS-1$
 
 	private static final String twoThetaField = ".TTH"; //$NON-NLS-1$
 
 	private static final String calibrationQuadraticField = ".CALQ"; //$NON-NLS-1$
 
 	private static final String calibrationSlopeField = ".CALS"; //$NON-NLS-1$
 
 	private static final String calibrationOffsetField = ".CALO"; //$NON-NLS-1$
 
 	private static final String unitsField = ".EGU"; //$NON-NLS-1$
 
 	private static final String eraseField = ".ERAS"; //$NON-NLS-1$
 
 	private static final String presetSweepField = ".PSWP"; //$NON-NLS-1$
 
 	private static final String presetCountHighField = ".PCTH"; //$NON-NLS-1$
 
 	private static final String presetCountLowField = ".PCTL"; //$NON-NLS-1$
 
 	private static final String presetCountsField = ".PCT"; //$NON-NLS-1$
 
 	private static final String presetLiveTimeField = ".PLTM"; //$NON-NLS-1$
 
 	private static final String presetRealTimeField = ".PRTM"; //$NON-NLS-1$
 
 	private static final String dwellTimeField = ".DWEL"; //$NON-NLS-1$
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int numOfBinsInDummyMode = 2048;
 
 	private IQuantitiesConverter channelToEnergyConverter = null;
 
 	private String converterName = "mca_roi_conversion"; //$NON-NLS-1$
 
 	private boolean acquisitionDone = true, readingDone = true;
 
 	FindableEpicsDevice epicsDevice = null;
 	RegisterForEpicsUpdates registerForEpicsUpdates = null;
 	static private int maxNumberOfRegions = 32;
 	static String[] roiLowFields = new String[maxNumberOfRegions];
 	static String[] roiHighFields = new String[maxNumberOfRegions];
 	static String[] roiBackgroundFields = new String[maxNumberOfRegions];
 	static String[] roiPresetFields = new String[maxNumberOfRegions];
 
 	static String[] roiCountFields = new String[maxNumberOfRegions];
 	private double[] roiCountValues = new double[maxNumberOfRegions];
 	static String[] roiNetCountFields = new String[maxNumberOfRegions];
 	private double[] roiNetCountValues = new double[maxNumberOfRegions];
 	static String[] roiPresetCountFields = new String[maxNumberOfRegions];
 	static String[] roiNameFields = new String[maxNumberOfRegions];
 	static {
 		for (int i = 0; i < roiLowFields.length; i++) {
 			roiLowFields[i] = R + (i) + LO;
 			roiHighFields[i] = R + (i) + HI;
 			roiBackgroundFields[i] = R + (i) + BG;
 			roiPresetFields[i] = R + (i) + IP;
 			roiCountFields[i] = R + (i);
 			roiNetCountFields[i] = R + (i) + N;
 			roiPresetCountFields[i] = R + (i) + P;
 			roiNameFields[i] = R + (i) + NM;
 		}
 	}
 
 	private String mcaPV = null; // pv if not using a FindableEpicsDevice
 
 	private Integer numberOfRegions = maxNumberOfRegions;
 
 	/**
 	 * Constructor.
 	 */
 	public EpicsMCASimple() {
 		// do nothing
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 		if (!configured) {
 			if (epicsDevice == null) {
 				if (epicsDeviceName != null) {
 					Findable object = Finder.getInstance().find(epicsDeviceName);
 					if (object != null && object instanceof FindableEpicsDevice) {
 						epicsDevice = (FindableEpicsDevice) object;
 						epicsDevice.configure();
 					}
 				} else if (mcaPV != null) {
 					EpicsDevice mcaEpicsDevice;
 					try {
 						HashMap<String, String> recordPVs = new HashMap<String, String>();
 						recordPVs.put("", mcaPV);
 						mcaEpicsDevice = new EpicsDevice(getName(), recordPVs, false);
 					} catch (DeviceException e) {
 						throw new FactoryException("Unable to create EpicsDEvice", e);
 					}
 					epicsDevice = new FindableEpicsDevice(getName() + mcaPV, mcaEpicsDevice);
 				}
 			}
 			if (epicsDevice == null) {
 				throw new FactoryException("Unable to find epics device"); //$NON-NLS-1$
 			}
 			if (!epicsDevice.getDummy()) {
 				ArrayList<EpicsRegistrationRequest> requests = new ArrayList<EpicsRegistrationRequest>();
 				requests.add(new EpicsRegistrationRequest(ReturnType.DBR_NATIVE, SingleRecord, acquiringField,
 						SingleRecord, 1.0, false));
 				requests.add(new EpicsRegistrationRequest(ReturnType.DBR_NATIVE, SingleRecord, readingField,
 						SingleRecord, 1.0, false));
 				registerForEpicsUpdates = new RegisterForEpicsUpdates(epicsDevice, requests, this);
 			}
 			if (epicsDevice.getDummy()) {
 				try {
 					// set configured so that we can use the set commands to initialise values
 					configured = true;
 					for (Integer i = 0; i < numberOfRegions; i++) {
 						addRegionOfInterest(i, -1, -1, 0, 1.0, i.toString()); // set regionPreset to 1.0 to ensure
 						// value
 						// is set in dummy mode
 					}
 					setDoubleFieldValue(elapsedLiveTimeField, 1);
 					setDoubleFieldValue(elapsedRealTimeField, 1.1);
 
 					EpicsMCACalibration calib = new EpicsMCACalibration(
 							"EGU", (float) 1.0, (float) 1.0, (float) 0., (float) 0.); //$NON-NLS-1$
 					setCalibration(calib);
 					setDwellTime(1.0);
 					EpicsMCAPresets preset = new EpicsMCAPresets((float) 1.0, (float) 1.0, 1, 1, 1, 1);
 					setPresets(preset);
 					setSequence(1);
 					setIntFieldValue(maxNumberOfChannelsToUseField, numOfBinsInDummyMode);
 					setNumberOfChannels(numOfBinsInDummyMode);
 
 					for (int i = 0; i < maxNumberOfRegions; i++) {
 						_setRegionsOfInterestCount(i, i * 1000.);
 						_setRegionsOfInterestNetCount(i, i * 1000.);
 					}
 					int[] data = new int[numOfBinsInDummyMode];
 					for (int i = 0; i < data.length; i++) {
 						data[i] = i;
 					}
 					setData(data);
 				} catch (DeviceException ex) {
 					configured=false;
 					throw new FactoryException("Error initialising the device:"+getName(),ex);
 				}
 			}
 			configured = true;
 		}
 	}
 
 	/**
 	 * Helper function to set a Double in the field of the single record in the epicsdevice
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @param value
 	 *            - value to set
 	 * @throws DeviceException
 	 */
 	private void setDoubleFieldValue(String field, double value) throws DeviceException {
 		epicsDevice.setValue(SingleRecord, field, value);
 	}
 
 	/**
 	 * Helper function to set an Integer in the field of the single record in the epicsdevice
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @param value
 	 *            - value to set
 	 * @throws DeviceException
 	 */
 	private void setIntFieldValue(String field, int value) throws DeviceException {
 		epicsDevice.setValue(SingleRecord, field, value);
 	}
 
 	private void setIntFieldValueNoWait(String field, int value) throws DeviceException {
 		epicsDevice.setValueNoWait(SingleRecord, field, value);
 	}
 
 	/**
 	 * Helper function to set a short in the field of the single record in the epicsdevice
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @param value
 	 *            - value to set
 	 * @throws DeviceException
 	 */
 	private void setShortFieldValue(String field, short value) throws DeviceException {
 		epicsDevice.setValue(SingleRecord, field, value);
 	}
 
 	private void setShortFieldValueNoWait(String field, short value) throws DeviceException {
 		epicsDevice.setValueNoWait(SingleRecord, field, value);
 	}
 	
 	/**
 	 * Helper function to set String in the field of the single record in the epicsdevice
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @param value
 	 *            - value to set
 	 * @throws DeviceException
 	 */
 	private void setStringFieldValue(String field, String value) throws DeviceException {
 		epicsDevice.setValue(SingleRecord, field, value);
 	}
 
 	/**
 	 * Helper function to set the value in the field of the single record in the epicsdevice
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @param value
 	 *            - value to set
 	 * @throws DeviceException
 	 */
 	private void setObjectFieldValue(String field, Object value) throws DeviceException {
 		epicsDevice.setValue(SingleRecord, field, value);
 	}
 
 	/**
 	 * Helper function to get the value of the field of the single record in the epicsdevice as a double
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @return double
 	 * @throws DeviceException
 	 */
 	private double getDoubleFromField(String field) throws DeviceException {
 		try {
 			return (Double) epicsDevice.getValue(ReturnType.DBR_NATIVE, SingleRecord, field);
 		} catch (Exception e) {
 			throw new DeviceException("getDoubleFromField - error for " + field, e);
 		}
 	}
 
 	/**
 	 * Helper function to get the value of the field of the single record in the epicsdevice as an int
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @return int
 	 * @throws DeviceException
 	 */
 	private int getIntFromField(String field) throws DeviceException {
 		try {
 			return (Integer) epicsDevice.getValue(ReturnType.DBR_NATIVE, SingleRecord, field);
 		} catch (Exception e) {
 			throw new DeviceException("getIntFromField - error for " + field, e);
 		}
 	}
 
 	/**
 	 * Helper function to get the value of the field of the single record in the epicsdevice as a short
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @return short
 	 * @throws DeviceException
 	 */
 	private short getShortFromField(String field) throws DeviceException {
 		try {
 			return (Short) epicsDevice.getValue(ReturnType.DBR_NATIVE, SingleRecord, field);
 		} catch (Exception e) {
 			throw new DeviceException("getIntFromField - error for " + field, e);
 		}
 	}
 
 	/**
 	 * Helper function to get the value of the field of the single record in the epicsdevice as a string
 	 * 
 	 * @param field
 	 *            - suffix used to construct the pv name
 	 * @return String
 	 * @throws DeviceException
 	 */
 	private String getStringFromField(String field) throws DeviceException {
 		if (epicsDevice.getDummy()) {
 			return (String) epicsDevice.getValue(ReturnType.DBR_NATIVE, SingleRecord, field);
 		}
 		return epicsDevice.getValueAsString(SingleRecord, field);
 	}
 
 	public FindableEpicsDevice getEpicsDevice() {
 		return epicsDevice;
 	}
 
 	public void setEpicsDevice(FindableEpicsDevice epicsDevice) {
 		this.epicsDevice = epicsDevice;
 	}
 
 	@Override
 	public void addRegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
 			double regionPreset, String regionName) throws DeviceException {
 		try {
 			setIntFieldValue(roiLowFields[regionIndex], (int) regionLow);
 			setIntFieldValue(roiHighFields[regionIndex], (int) regionHigh);
 			setShortFieldValue(roiBackgroundFields[regionIndex], (short) regionBackground);
 			if (regionPreset <= 0) {
 				setIntFieldValue(roiPresetFields[regionIndex], 0);
 			} else {
 				setIntFieldValue(roiPresetFields[regionIndex], 1);
 				setDoubleFieldValue(roiPresetCountFields[regionIndex], regionPreset);
 			}
 			if (regionName != null) {
 				setStringFieldValue(roiNameFields[regionIndex], regionName);
 			}
 
 		} catch (Throwable th) {
 			throw new DeviceException("failed to add region of interest", th);
 		}
 	}
 
 	@Override
 	public void clear() throws DeviceException {
 		setIntFieldValue(eraseField, 1);
 	}
 
 	/**
 	 * Clears the mca, but does not return until the clear has been done.
 	 * 
 	 * @throws DeviceException
 	 */
 	public void clearWaitForCompletion() throws DeviceException {
 		clear();
 	}
 
 	@Override
 	public void deleteRegionOfInterest(int regionIndex) throws DeviceException {
 		try {
 			setIntFieldValue(roiLowFields[regionIndex], -1);
 			setIntFieldValue(roiHighFields[regionIndex], -1);
 			setShortFieldValue(roiBackgroundFields[regionIndex], (short)-1);
 			setIntFieldValue(roiPresetFields[regionIndex], 0);
 			setDoubleFieldValue(roiPresetCountFields[regionIndex], 0);
 			setStringFieldValue(roiNameFields[regionIndex], SingleRecord);
 
 		} catch (Throwable th) {
 			throw new DeviceException("failed to delete region of interest", th);
 		}
 
 	}
 
 	@Override
 	public Object getCalibrationParameters() throws DeviceException {
 		try {
 
 			String egu = getStringFromField(unitsField);
 			double calo = getDoubleFromField(calibrationOffsetField);
 			double cals = getDoubleFromField(calibrationSlopeField);
 			double calq = getDoubleFromField(calibrationQuadraticField);
 			double tth = getDoubleFromField(twoThetaField);
 
 			return new EpicsMCACalibration(egu, (float) calo, (float) cals, (float) calq, (float) tth);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get calibration parameters", th);
 		}
 	}
 
 	@Override
 	public Object getData() throws DeviceException {
 		try {
 			Object val = epicsDevice.getValue(ReturnType.DBR_NATIVE, SingleRecord, dataField);
 			return val;
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get data", th);
 		}
 	}
 
 	@Override
 	public int[] getDataDimensions() throws DeviceException {
 		//TODO get value of .NUSE
 		return new int[] { ArrayUtils.getLength(getData()) };
 	}
 
 	/**
 	 * Closes currently unused epics channels. Only run this if you suspect you need to as the next attempt to read a
 	 * value will re-create the channel.
 	 * 
 	 * @throws DeviceException
 	 */
 	public void dispose() throws DeviceException {
 		if (epicsDevice != null) {
 			epicsDevice.dispose();
 		}
 	}
 
 	@Override
 	public Object getElapsedParameters() throws DeviceException {
 		try {
 
 			float[] elapsed = new float[2];
 			elapsed[0] = (float) getDoubleFromField(elapsedRealTimeField);
 			elapsed[1] = (float) getDoubleFromField(elapsedLiveTimeField);
 			return elapsed;
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get elapsed parameters", th);
 		}
 	}
 
 	/**
 	 * Gets the Dwell Time (DWEL).
 	 * 
 	 * @return Dwell Time
 	 * @throws DeviceException
 	 */
 	public double getDwellTime() throws DeviceException {
 		return getDoubleFromField(dwellTimeField);
 	}
 
 	@Override
 	public int getNumberOfRegions() throws DeviceException {
 		return numberOfRegions;
 	}
 
 	@Override
 	public Object getPresets() throws DeviceException {
 		try {
 
 			double prtm = getDoubleFromField(presetRealTimeField);
 			double pltm = getDoubleFromField(presetLiveTimeField);
 			int pct = getIntFromField(presetCountsField);
 			int pctl = getIntFromField(presetCountLowField);
 			int pcth = getIntFromField(presetCountHighField);
 			int pswp = getIntFromField(presetSweepField);
 			return new EpicsMCAPresets((float) prtm, (float) pltm, pct, pctl, pcth, pswp);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to get presets", th);
 		}
 
 	}
 
 	@Override
 	public Object getRegionsOfInterest() throws DeviceException {
 		return getRegionsOfInterest(numberOfRegions);
 	}
 
 	boolean readNetCounts = true;
 
 	/*
 	 * Lockable object that is used to inform the thread executing WaitWhileBusy that the
 	 * value of doneReading has been changed by an Epics monitor
 	 */
 	private Object doneLock= new Object();
 
 	/*
 	 * When first developed I found that in the ACQG callback I need to perform
 	 * a Read request to ensure the data was correct. However when using this class with the
 	 * Epics DXP module that also support MCA it was not needed. The default value
 	 * gives the old behaviour 
 	 */
 	private boolean readingDoneIfNotAquiring=false;
 
 	public int getNumberOfValsPerRegionOfInterest() {
 		return readNetCounts ? 2 : 1;
 	}
 
 	static int indexForRawROI = 0;
 
 	public int getIndexForRawROI() {
 		return indexForRawROI;
 	}
 
 	public boolean isReadNetCounts() {
 		return readNetCounts;
 	}
 
 	public void setReadNetCounts(boolean readNetCounts) {
 		this.readNetCounts = readNetCounts;
 	}
 
 	@Override
 	public double getRoiCount(int index) throws DeviceException{
 		return getDoubleFromField(roiCountFields[index]);
 	}
 	@Override
 	public double getRoiNetCount(int index) throws DeviceException{
 		return getDoubleFromField(roiNetCountFields[index]);
 	}
 	@Override
 	public double[][] getRegionsOfInterestCount() throws DeviceException {
 		try {
 
 			double[][] regionsCount = new double[numberOfRegions][getNumberOfValsPerRegionOfInterest()];
 			for (int i = 0; i < regionsCount.length; i++) {
 				regionsCount[i][0] = getDoubleFromField(roiCountFields[i]);
 				roiCountValues[i] = regionsCount[i][0];
 				if (isReadNetCounts()) {
 					regionsCount[i][1] = getDoubleFromField(roiNetCountFields[i]);
 					roiNetCountValues[i] = regionsCount[i][1];
 				}
 			}
 			return regionsCount;
 		} catch (Throwable th) {
 			throw new DeviceException("EpicsMCA.getRegionsOfInterestCount:failed to get region of interest count", th);
 		}
 	}
 
 	@Override
 	public long getSequence() throws DeviceException {
 		try {
 			return getIntFromField(seqField);
 		} catch (Throwable th) {
 			throw new DeviceException("EpicsMCA.getSequence:failed to get sequence", th);
 		}
 	}
 
 	@Override
 	public int getStatus() throws DeviceException {
 		try {
 			// we need to fire the PROC to ensure the RDGN field is updated
 			setIntFieldValueNoWait(procField, 1);
 			return (acquisitionDone && readingDone) ? Detector.IDLE : Detector.BUSY;
 		} catch (Throwable th) {
 			throw new DeviceException("EpicsMCA.getStatus: failed to get status", th);
 		}
 	}
 
 	@Override
 	public void setCalibration(EpicsMCACalibration calibrate) throws DeviceException {
 		setCalibration((Object)calibrate);
 	}
 	@Override
 	public void setCalibration(Object calibrate) throws DeviceException {
 		try {
 			EpicsMCACalibration calib = (EpicsMCACalibration) calibrate;
 			setStringFieldValue(unitsField, calib.getEngineeringUnits());
 			setDoubleFieldValue(calibrationOffsetField, calib.getCalibrationOffset());
 			setDoubleFieldValue(calibrationSlopeField, calib.getCalibrationSlope());
 			setDoubleFieldValue(calibrationQuadraticField, calib.getCalibrationQuadratic());
 			setDoubleFieldValue(twoThetaField, calib.getTwoThetaAngle());
 
 		} catch (Throwable th) {
 			throw new DeviceException("EpicsMCA.setCalibration: failed to set calibration", th);
 		}
 	}
 
 	@Override
 	public void setData(Object data) throws DeviceException {
 		try {
 			setObjectFieldValue(dataField, data);
 		} catch (Throwable th) {
 			throw new DeviceException("EpicsMCA.setData: failed to set data", th);
 		}
 	}
 
 	/**
 	 * Sets the dwell time (DWEL)
 	 * 
 	 * @param time
 	 * @throws DeviceException
 	 */
 	public void setDwellTime(double time) throws DeviceException {
 		// The dwell time appears to be changed automatically to 0
 		setDoubleFieldValue(dwellTimeField, time);
 	}
 
 	@Override
 	public void setNumberOfRegions(int numberOfRegions) throws DeviceException {
 		if (configured)
 			throw new DeviceException("Unable to set numberOfRegions once configured");
 		if (numberOfRegions > maxNumberOfRegions || numberOfRegions < 1) {
 			throw new DeviceException("numberOfRegions must be between 1 and " + maxNumberOfRegions);
 		}
 		this.numberOfRegions = numberOfRegions;
 	}
 
 	@Override
 	public void setPresets(Object data) throws DeviceException {
 		try {
 			EpicsMCAPresets preset = (EpicsMCAPresets) data;
 			setDoubleFieldValue(presetRealTimeField, preset.getPresetRealTime());
 			setDoubleFieldValue(presetLiveTimeField, preset.getPresetLiveTime());
 			setIntFieldValue(presetCountsField, (int) preset.getPresetCounts());
 			setIntFieldValue(presetCountLowField, (int) preset.getPresetCountlow());
 			setIntFieldValue(presetCountHighField, (int) preset.getPresetCountHigh());
 			setIntFieldValue(presetSweepField, (int) preset.getPresetSweeps());
 
 		} catch (Throwable th) {
 			throw new DeviceException("failed to set presets", th);
 		}
 	}
 
 	@Override
 	public void setRegionsOfInterest(EpicsMCARegionOfInterest[] epicsMcaRois) throws DeviceException {
 		setRegionsOfInterest((Object)epicsMcaRois);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see gda.device.Analyser#setRegionsOfInterest(java.lang.Object) * the input parameter highLow object should
 	 *      actually be an array of EpicsMCARegionsOfInterest objects
 	 */
 	@Override
 	public void setRegionsOfInterest(Object highLow) throws DeviceException {
 		try {
 			EpicsMCARegionOfInterest[] rois = (EpicsMCARegionOfInterest[]) highLow;
 			for (int i = 0; i < rois.length; i++) {
 				int regionIndex = rois[i].getRegionIndex();
 				setIntFieldValue(roiLowFields[regionIndex], (int)rois[i].getRegionLow());
 				setIntFieldValue(roiHighFields[regionIndex], (int)rois[i].getRegionHigh());
 				setShortFieldValue(roiBackgroundFields[regionIndex], (short)rois[i].getRegionBackground());
 				double regionPreset = rois[i].getRegionPreset();
 				if (regionPreset <= 0)
 					setIntFieldValue(roiPresetFields[regionIndex], 0);
 				else {
 					setIntFieldValue(roiPresetFields[regionIndex], 1);
 				}
 				setDoubleFieldValue(roiPresetCountFields[regionIndex], regionPreset);
 				setStringFieldValue(roiNameFields[regionIndex], rois[i].getRegionName());
 			}
 
 		} catch (Throwable th) {
 			throw new DeviceException("failed to set region of interest", th);
 		}
 	}
 
 	@Override
 	public void setSequence(long sequence) throws DeviceException {
 		try {
 			setIntFieldValue(seqField, (int) sequence);
 		} catch (Throwable th) {
 			throw new DeviceException("failed to set sequence", th);
 		}
 	}
 
 	/**
 	 * Activates the MCA using the Erase & Start button.
 	 * 
 	 * @throws DeviceException
 	 */
 	@Override
 	public void eraseStartAcquisition() throws DeviceException {
 		try {
 			readingDone = false;
 			acquisitionDone = false;
 			setIntFieldValueNoWait(eraseStartAcqField, 1); 
 			if (epicsDevice.getDummy()) {
 				Thread.sleep((long) (getCollectionTime() * 1000));
 				_fireReadingDone();
 			}
 		} catch (Throwable th) {
 			throw new DeviceException("failed to start acquisition", th);
 		}
 	}
 
 	@Override
 	public void startAcquisition() throws DeviceException {
 		try {
 			setIntFieldValueNoWait(startAcqField, 1);
 			acquisitionDone = false;
 			readingDone = false;
 			if (epicsDevice.getDummy()) {
 				Thread.sleep((long) (getCollectionTime() * 1000));
 				_fireReadingDone();
 			}
 		} catch (Throwable th) {
 			throw new DeviceException("failed to start acquisition", th);
 		}
 	}
 
 	@Override
 	public void stopAcquisition() throws DeviceException {
 		try {
 			setIntFieldValue(stopAcqField, 1);
 			if (epicsDevice.getDummy()) {
 				_fireReadingDone();
 			}
 		} catch (Throwable th) {
 			throw new DeviceException("failed to stop acquisition", th);
 		}
 
 	}
 
 	/**
 	 * method used for testing only
 	 */
 	public void _fireReadingDone() {
 		acquisitionDone = true;
 		setReadingDone(true);
 		notifyIObservers(this, (acquisitionDone & readingDone) ? MCAStatus.READY : MCAStatus.BUSY);
 	}
 
 	/**
 	 * method used for testing only
 	 */
 	public void _setRegionsOfInterestCount(int index, Double val) throws DeviceException {
 		setObjectFieldValue(roiCountFields[index], val);
 	}
 
 	/**
 	 * method used for testing only
 	 */
 	public void _setRegionsOfInterestNetCount(int index, Double val) throws DeviceException {
 		setObjectFieldValue(roiNetCountFields[index], val);
 	}
 
 	private void setReadingDone(boolean readingDone){
 		synchronized (doneLock) {
 			this.readingDone = readingDone;
 			doneLock.notifyAll();
 		}
 	}
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 		if (theObserved instanceof EpicsRegistrationRequest && changeCode instanceof EpicsMonitorEvent
 				&& ((EpicsMonitorEvent) changeCode).epicsDbr instanceof DBR) {
 			EpicsMonitorEvent event = (EpicsMonitorEvent) changeCode;
 			DBR dbr = (DBR) event.epicsDbr;
 			if (dbr != null) {
 				if (((EpicsRegistrationRequest) theObserved).field.equals(acquiringField) && dbr.isENUM()) {
 					acquisitionDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
 					logger.debug("update acquisitionDone =" + acquisitionDone);
 					if (acquisitionDone) {
 						try {
 							if( readingDoneIfNotAquiring){
 								setReadingDone(true);
 							} else {
 								// now ask for a read and set ReadingDone false
 								setIntFieldValue(readField, 1);
 								readingDone = false;
 							}
 						} catch (Exception e) {
 							exceptionUtils.logException(logger,
 									"Error setting read to 1 in response to acquisition done", e);
 						}
 					}
 				} else if (((EpicsRegistrationRequest) theObserved).field.equals(readingField) && dbr.isENUM()) {
 					setReadingDone(((DBR_Enum) dbr).getEnumValue()[0] == 0);
 					logger.debug("update readingDone =" + readingDone);
 					notifyIObservers(this, (acquisitionDone & readingDone) ? MCAStatus.READY : MCAStatus.BUSY);
 				}
 			}
 		}
 	}
 
 	@Override
 	public EpicsMCARegionOfInterest getNthRegionOfInterest(int regionIndex) throws DeviceException {
 		EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();
 
 		rois.setRegionIndex(regionIndex);
 
 		rois.setRegionLow(getIntFromField(roiLowFields[regionIndex]));
 
 		rois.setRegionHigh(getIntFromField(roiHighFields[regionIndex]));
 
 		rois.setRegionBackground(getShortFromField(roiBackgroundFields[regionIndex]));
 
 		rois.setRegionPreset(getDoubleFromField(roiPresetCountFields[regionIndex]));
 
 		rois.setRegionName(getStringFromField(roiNameFields[regionIndex]));
 
 		return rois;
 	}
 
 	private Object getRegionsOfInterest(int noOfRegions) throws DeviceException {
 		Vector<EpicsMCARegionOfInterest> roiVector = new Vector<EpicsMCARegionOfInterest>();
 		for (int regionIndex = 0; regionIndex < noOfRegions; regionIndex++) {
 			EpicsMCARegionOfInterest rois = getNthRegionOfInterest(regionIndex);
 			roiVector.add(rois);
 		}
 		if (roiVector.size() != 0) {
 			EpicsMCARegionOfInterest[] selectedrois = new EpicsMCARegionOfInterest[roiVector.size()];
 			for (int j = 0; j < selectedrois.length; j++) {
 				selectedrois[j] = roiVector.get(j);
 			}
 			return selectedrois;
 		}
 		return null;
 	}
 
 	@Override
 	public long getNumberOfChannels() throws DeviceException {
 		return getIntFromField(numberOfChannelsToUseField);
 	}
 
 	@Override
 	public void setNumberOfChannels(long channels) throws DeviceException {
 		long max = getIntFromField(maxNumberOfChannelsToUseField);
 		if (channels > max) {
 			throw new DeviceException("Invalid number of channels," + " Maximum channels allowed is  " + max);
 		}
 		setIntFieldValue(numberOfChannelsToUseField, (int) channels);
 	}
 
 	@Override
 	public void collectData() throws DeviceException {
 		clear();
 
 		EpicsMCAPresets presets = (EpicsMCAPresets) getPresets();
 
 		if (presets.getPresetLiveTime() > 0.0 && this.collectionTime > 0.0) {
 			startAcquisition();
 		} else {
 			stopAcquisition(); // throws an exception?
 		}
 
 	}
 
 	@Override
 	public Object readout() throws DeviceException {
 		return getData();
 	}
 
 	/**
 	 * 
 	 */
 	public static final String channelToEnergyPrefix = "channelToEnergy:";
 	/**
 	 * 
 	 */
 	public static final String numberOfChannelsAttr = "NumberOfChannels";
 	/**
 	 * 
 	 */
 	public static final String energyToChannelPrefix = "energyToChannel";
 
 	@Override
 	public Object getAttribute(String attributeName) throws DeviceException {
 
 		if (attributeName.startsWith(channelToEnergyPrefix)) {
 			String energy = null;
 			if (channelToEnergyConverter == null && converterName != null) {
 				channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
 			}
 			if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
 				String channelString = attributeName.substring(channelToEnergyPrefix.length());
 				Quantity channel = Quantity.valueOf(channelString);
 				try {
 					energy = ((IQuantityConverter) channelToEnergyConverter).toSource(channel).toString();
 					return energy;
 				} catch (Exception e) {
 					throw new DeviceException("EpicsMCA.getAttribute exception", e);
 				}
 			}
 			throw new DeviceException(
 					"EpicsMCA : unable to find suitable converter to convert channel to energy. converterName  "
							+ (converterName == null ? "not given" : converterName));
 		} else if (attributeName.startsWith(energyToChannelPrefix)) {
 			// String channel = null;
 			if (channelToEnergyConverter == null && converterName != null) {
 				channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
 			}
 			if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
 				String energyString = attributeName.substring(energyToChannelPrefix.length());
 				Quantity energy = Quantity.valueOf(energyString);
 				try {
 					long ichannel = (long) ((IQuantityConverter) channelToEnergyConverter).toTarget(energy).getAmount();
 					return Long.toString(Math.max(Math.min(ichannel, getNumberOfChannels() - 1), 0));
 				} catch (Exception e) {
 					throw new DeviceException("EpicsMCA.getAttribute exception", e);
 				}
 			}
 			throw new DeviceException(
 					"EpicsTCA : unable to find suitable converter to convert energy to channel. converterName  "
							+ (converterName == null ? "not given" : converterName));
 		} else if (attributeName.equals(numberOfChannelsAttr)) {
 			return getNumberOfChannels();
 		} else {
 			return super.getAttribute(attributeName);
 		}
 	}
 
 	/**
 	 * @return converter name
 	 */
 	public String getCalibrationName() {
 		return converterName;
 	}
 
 	/**
 	 * @param calibrationName
 	 */
 	public void setCalibrationName(String calibrationName) {
 		this.converterName = calibrationName;
 	}
 
 	private String epicsDeviceName;
 
 	/**
 	 * @return String
 	 */
 	public String getEpicsDeviceName() {
 		return epicsDeviceName;
 	}
 
 	/**
 	 * @param deviceName
 	 */
 	public void setEpicsDeviceName(String deviceName) {
 		this.epicsDeviceName = deviceName;
 	}
 
 	/**
 	 * @return String
 	 */
 	public String getMCAPV() {
 		return mcaPV;
 	}
 
 	/**
 	 * @param mcaPV
 	 */
 	public void setMCAPV(String mcaPV) {
 		this.mcaPV = mcaPV;
 	}
 
 	/**
 	 * @return String
 	 */
 	public String getMcaPV() {
 		return mcaPV;
 	}
 
 	/**
 	 * @param mcaPV
 	 */
 	public void setMcaPV(String mcaPV) {
 		this.mcaPV = mcaPV;
 	}
 
 	@Override
 	public boolean createsOwnFiles() throws DeviceException {
 		// readout() doesn't return a filename.
 		return false;
 	}
 
 	@Override
 	public String getDescription() throws DeviceException {
 		return "EPICS Mca";
 	}
 
 	@Override
 	public String getDetectorID() throws DeviceException {
 		return "unknown";
 	}
 
 	@Override
 	public String getDetectorType() throws DeviceException {
 		return "EPICS";
 	}
 
 	
 	@Override
 	public void waitWhileBusy() throws DeviceException, InterruptedException {
 		synchronized(doneLock){
 			while( !(acquisitionDone & readingDone)){ 
 				doneLock.wait();
 			}
 		}
 	}
 
 	public boolean isReadingDoneIfNotAquiring() {
 		return readingDoneIfNotAquiring;
 	}
 
 	public void setReadingDoneIfNotAquiring(boolean readingDoneIfNotAquiring) {
 		this.readingDoneIfNotAquiring = readingDoneIfNotAquiring;
 	}
 	
 }
 
 final class RegisterForEpicsUpdates implements Runnable {
 
 	private static final Logger logger = LoggerFactory.getLogger(RegisterForEpicsUpdates.class);
 
 	final List<EpicsRegistrationRequest> requests;
 	final IEpicsDevice epicsDevice;
 	final IObserver observer;
 	ArrayList<IEpicsChannel> chans = new ArrayList<IEpicsChannel>();
 
 	RegisterForEpicsUpdates(IEpicsDevice epicsDevice, List<EpicsRegistrationRequest> requests, IObserver observer) {
 		this.epicsDevice = epicsDevice;
 		this.requests = requests;
 		this.observer = observer;
 		Thread t = uk.ac.gda.util.ThreadManager.getThread(this);
 		t.setPriority(java.lang.Thread.MIN_PRIORITY);
 		t.start();
 	}
 
 	@Override
 	public void run() {
 		try {
 			for (EpicsRegistrationRequest request : requests) {
 				IEpicsChannel chan = epicsDevice.createEpicsChannel(request.returnType, request.record, request.field);
 				chan.addIObserver(observer);
 				chans.add(chan);
 			}
 		} catch (Exception ex) {
 			exceptionUtils.logException(logger, "Error in RegisterForEpicsUpdates", ex);
 		}
 	}
 	
 }
