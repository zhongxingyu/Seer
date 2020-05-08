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
 
 package gda.scan;
 
 import gda.device.Detector;
 import gda.device.DeviceException;
 import gda.device.EnumPositionerStatus;
 import gda.device.Scannable;
 import gda.device.ScannableMotionUnits;
 import gda.device.enumpositioner.PolarimeterPinholeEnumPositioner;
 import gda.device.scannable.ScannableUtils;
 import gda.device.scannable.ScriptAdapter;
 import gda.factory.Finder;
 
 import java.util.Vector;
 
 import org.python.core.PyList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class to control the stepped movement of a Polarimeter scannable object. Separate polarimeter class required to allow
  * unique flux monitoring regime.
  * <p>
  * At each step, after movement, the readout() method of all objOEect in the DetectorBase.activeDetectors arraylist is
  * called.
  */
 public class PolarimeterGridScan extends ScanBase implements Scan {
 	private static final Logger logger = LoggerFactory.getLogger(PolarimeterGridScan.class);
 
 	protected Object start;
 	protected Object stop;
 	protected Object step;
 	protected Object time = new Double(1000.0);
 	protected Object units;
 	protected Object period;
 
 	protected Boolean monitorFlux;
 	protected String fluxValue;
 	protected String fluxMonitorChannelLabel;
 	protected Object pinholeNumber;
 
 	protected PolarimeterPinholeEnumPositioner frontPinhole;
 
 	protected ScanBase childScan;
 	protected ScriptAdapter scriptAdapter = null;
 	protected ScannableMotionUnits theScannable = null;
 
 	private String savedUnits;
 
 	private String attrName;
 	private Object attrValue;
 
 	/**
  * 
  */
 	public PolarimeterGridScan() {
 		super();
 	}
 
 	/**
 	 * Creates a scan object.Used for inner 2d GUI or single GUI scan
 	 * 
 	 * @param ve
 	 *            the scannable
 	 * @param start
 	 *            double
 	 * @param stop
 	 *            double
 	 * @param step
 	 *            double
 	 * @param time
 	 *            Object
 	 * @param units
 	 *            String
 	 * @param pinhole
 	 *            double used
 	 */
 	public PolarimeterGridScan(ScannableMotionUnits ve, Object start, Object stop, Object step, Object time,
 			Object units, Object pinhole) {
 		allScannables.add(ve);
 		theScannable = ve;
 		this.start = start;
 		this.stop = stop;
 		this.step = step;
 		this.time = time;
 		// somewhere between the tempScript2 file (which contains the
 		// correct string) and the athis.monitorFlux = true;ttempt by jython to create a GridScan the
 		// string for micrometers loses its mu and gains an unprintable character.
 		// This superkludge gets round this but urgently needs fixing properly.
 		// See bug #352.
 
 		String string = (String) units;
 		if (string != null && string.length() == 2 && string.charAt(0) == 65533 && string.charAt(1) == 'm') {
 			// NB this (00B5) is 181 the code for mu as used for micro in the Latin
 			// encoding and not 956 (03BC) which is the proper Greek letter mu.
 			this.units = "\u00b5m";
 		} else {
 			this.units = units;
 		}
 		// Flux monitoring bit
 		this.pinholeNumber = pinhole;
 		if (pinholeNumber.equals(0)) {
 			this.monitorFlux = false;
 		} else {
 			this.monitorFlux = true;
 		}
 		this.childScan = null;
 		setUp();
 		setupGridScan();
 	}
 
 	/**
 	 * Creates a scan object. Used for outer 2D GUI scan
 	 * 
 	 * @param ve
 	 *            the scannable
 	 * @param start
 	 *            double
 	 * @param stop
 	 *            double
 	 * @param step
 	 *            double
 	 * @param units
 	 *            String
 	 * @param childScan
 	 * @param pinhole
 	 *            double used
 	 */
 	public PolarimeterGridScan(ScannableMotionUnits ve, Object start, Object stop, Object step, Object units,
 			Scan childScan, Object pinhole) {
 		allScannables.add(ve);
 		theScannable = ve;
 		this.start = start;
 		this.stop = stop;
 		this.step = step;
 		// somewhere between the tempScript2 file (which contains the
 		// correct string) and the athis.monitorFlux = true;ttempt by jython to create a GridScan the
 		// string for micrometers loses its mu and gains an unprintable character.
 		// This superkludge gets round this but urgently needs fixing properly.
 		// See bug #352.
 
 		String string = (String) units;
 		if (string != null && string.length() == 2 && string.charAt(0) == 65533 && string.charAt(1) == 'm') {
 			// NB this (00B5) is 181 the code for mu as used for micro in the Latin
 			// encoding and not 956 (03BC) which is the proper Greek letter mu.
 			this.units = "\u00b5m";
 		} else {
 			this.units = units;
 		}
 		// Flux monitoring bit
 		this.pinholeNumber = pinhole;
 		if (pinholeNumber.equals(0)) {
 			this.monitorFlux = false;
 		} else {
 			this.monitorFlux = true;
 		}
 		this.childScan = (ScanBase) childScan;
 		setUp();
 		setupGridScan();
 	}
 
 	/**
 	 * Creates a scan object. Used for outer scan scripting
 	 * 
 	 * @param ve
 	 *            the scannable
 	 * @param start
 	 *            double
 	 * @param stop
 	 *            double
 	 * @param step
 	 *            double
 	 * @param childScan
 	 *            used
 	 */
 	public PolarimeterGridScan(ScannableMotionUnits ve, Object start, Object stop, Object step, Scan childScan) {
 		allScannables.add(ve);
 		theScannable = ve;
 		this.start = start;
 		this.stop = stop;
 		this.step = step;
 		this.childScan = (ScanBase) childScan;
 		setUp();
 		setupGridScan();
 	}
 
 	/**
 	 * Creates a scan object. Used for simple and inner scripting
 	 * 
 	 * @param ve
 	 *            the scannable
 	 * @param start
 	 *            double
 	 * @param stop
 	 *            double
 	 * @param step
 	 *            double
 	 * @param time
 	 *            Object
 	 * @param pinhole
 	 *            double used
 	 */
 	public PolarimeterGridScan(ScannableMotionUnits ve, Object start, Object stop, Object step, Object time,
 			Object pinhole) {
 		allScannables.add(ve);
 		theScannable = ve;
 		this.start = start;
 		this.stop = stop;
 		this.step = step;
 		this.time = time;
 
 		// Flux monitoring bit
 		this.pinholeNumber = pinhole;
 		if (pinholeNumber.equals(0)) {
 			this.monitorFlux = false;
 		} else {
 			this.monitorFlux = true;
 		}
 		this.childScan = null;
 		setUp();
 		setupGridScan();
 	}
 
 	@Override
 	public void doCollection() throws Exception {
 		// Determine if we are stepping over Quantities or PyList.
 		// If a PyList then the scannable uses more complex quantities to define
 		// the movement, so make the scannable calculate how many steps to do.
 		int numberSteps = 0;
 		if (this.start instanceof PyList) {
 			numberSteps = ScannableUtils.getNumberSteps(theScannable, start, stop, step);
 		} else {
 			// get the numerical values of the arguements
 			double start = Double.parseDouble(this.start.toString());
 			double stop = Double.parseDouble(this.stop.toString());
 			double step = Double.parseDouble(this.step.toString());
 			// check that step is negative when moving downwards to stop
 			double difference = stop - start;
 			if (difference < 0 && step > 0) {
 				step = -step;
 			}
 			// add half a step to round to neartest integer
 			numberSteps = (int) ((difference / step) + 0.5);
 		}
 		try {
 			double time = Double.parseDouble(this.time.toString());
 			for (Detector detector : allDetectors) {
 				detector.setCollectionTime(time);
 			}
 
 			// make first step
 			logger.debug("Started a scan over {} \n", theScannable.getName());
 			checkForInterrupts();
 
 			moveToStart();
 			if (this.childScan != null) {
 				checkForInterrupts();
 				// The following line is required to ensure that the data file has the
 				// the required columns and headers.
 				runChildScan();
 			} else {
 				checkForInterrupts();
 				// then collect data
 				// first need to read and store flux value here if required
 				if (monitorFlux) {
 					measureFluxValue();
 				}
 				collectData();
 			}
 
 			// make subsequent steps
 			for (int i = 1; i <= numberSteps; ++i) {
 				checkForInterrupts();
 				moveStepIncrement(i);
 
 				if (this.childScan != null) {
 					checkForInterrupts();
 					runChildScan();
 				} else {
 					checkForInterrupts();
 					collectData();
 				}
 			}
 		}
 		// if anything typed in which cannot convert to a number,
 		// skip the rest of the scan
 		catch (Exception e) {
 			interrupted = true;
 			throw e;
 		}
 
 		resetUnits();
 	}
 
 	/**
 	 * Overidden for Polarimeter grid scan as need to swap flux monitor values in the ScanDataPoint object. This should
 	 * be called at each node of the scan. The collectData method is called for all detetcors in the
 	 * DetectorBase.ActiveDetectors static arraylist. Throws two types of errors as scans may want to handle these
 	 * differently.
 	 * @throws Exception 
 	 */
 	@Override
 	protected void collectData() throws Exception {
 		try {
 			// collect data
 			for (Detector detector : allDetectors) {
 				checkForInterrupts();
 				detector.collectData();
 			}
 			checkForInterrupts();
 
 			// check that all detectors have completed data collection
 			for (Detector detector : allDetectors) {
 				while (detector.getStatus() == Detector.BUSY) {
 					Thread.sleep(100);
 					checkForInterrupts();
 				}
 			}
 			checkForInterrupts();
 
 			// now can collate the data by creating a DataPoint
 			ScanDataPoint point = new ScanDataPoint();
 			point.setUniqueName(name);
 			// do the getPosition/readout here as work should not be done inside the SDP.
 			// This should be the only place these methods are called in the scan.
 			for (Scannable scannable : allScannables){
 				point.addScannable(scannable);
 				point.addScannablePosition(scannable.getPosition(),scannable.getOutputFormat());
 			}
 			for (Detector scannable : allDetectors){
 				point.addDetector(scannable);
 				point.addDetectorData(scannable.readout(),ScannableUtils.getExtraNamesFormats(scannable));
 			}
 
 			point.setCurrentFilename(getDataWriter().getCurrentFileName());
 			point.setHasChild(hasChild());
 			checkForInterrupts();
 
 			// If required swap the previously measured flux monitor value in the
 			// ScanDataPoint object
 			if (monitorFlux) {
 				swapFluxValue(point);
 			}
 			// then write data to data handler
 			getDataWriter().addData(point);
 
 			// update the filename (if this was the first data point and so
 			// filename
 			// would never be defined until first data added
 			point.setCurrentFilename(getDataWriter().getCurrentFileName());
 
 			// then notify IObservers of this scan (e.g. GUI panels)
 			notifyServer(point);
 		} catch (DeviceException ex) {
 			logger.error("PolarimeterGridScan.collectData(): Device Exception: " + ex.getMessage());
 			throw ex;
 		} catch (InterruptedException ex) {
 			throw ex;
 		}
 	}
 
 	/**
 	 * @param currentStep
 	 *            the number of the current step. NB This is not used here but it IS used by subclasses. Do not remove
 	 *            it.
 	 * @throws Exception
 	 */
 	protected void moveStepIncrement(int currentStep) throws Exception {
 		try {
 			String moveString = ""
 					+ (Double.parseDouble(start.toString()) + Double.parseDouble(step.toString()) * currentStep);
 			if (units != null) {
 				moveString += " " + (String) units;
 			} else {
 				moveString += " " + theScannable.getUserUnits();
 			}
 			theScannable.moveTo(moveString);
 		} catch (Exception e) {
 			if (e instanceof InterruptedException) {
 				throw e;
 			}
 			throw new Exception("PolarimeterGridScan.moveStepIncrement(): " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Move the object of this scan to its initial position.
 	 * 
 	 * @throws Exception
 	 */
 	protected void moveToStart() throws Exception {
 		try {
 			String moveString = "" + start;
 			if (units != null) {
 				moveString += " " + (String) units;
 				theScannable.setUserUnits(((String) units));
 			} else {
 				moveString += " " + theScannable.getUserUnits();
 			}
 			savedUnits = theScannable.getUserUnits();
 			theScannable.moveTo(moveString);
 			theScannable.setAttribute(attrName, attrValue);
 		} catch (Exception e) {
 			if (e instanceof InterruptedException) {
 				throw e;
 			}
 			throw new Exception("PolarimeterGridScan.moveToStart(): " + e.getMessage());
 		}
 	}
 
 	private void resetUnits() throws DeviceException {
 		theScannable.setUserUnits(savedUnits);
 	}
 
 	/**
 	 * Run the nested scan
	 * @throws Exception 
 	 */
	protected void runChildScan() throws Exception {
 		// before running the child scan, make sure it is sharing the same
 		// datahandler and lists
 		childScan.setDataWriter(getDataWriter());
 		childScan.isChild = true;
 
 		for (Scannable scannable : allScannables) {
 			if (!childScan.allScannables.contains(scannable)) {
 				childScan.allScannables.add(scannable);
 			}
 		}
 
 		// and in the same way build a list of all detectors
 		for (Detector detector : allDetectors) {
 			if (!childScan.allDetectors.contains(detector)) {
 				childScan.allDetectors.add(detector);
 			}
 		}
 
 		// run the child scan
 		this.childScan.run();
 	}
 
 	/**
 	 * Extra setup commands for grid scans. These are needed as the hierachy of parent\child scans in a
 	 * multi-dimensional gridscan need to share the same datahandler - and this datahandler would want to see the same
 	 * list of scannables and detectors.
 	 * <p>
 	 * This should be run after the base class setup() method.
 	 */
 	protected void setupGridScan() {
 
 		logger.debug("Finding: retarder");
 		if ((frontPinhole = (PolarimeterPinholeEnumPositioner) Finder.getInstance().find("FrontPinholePositioner")) == null) {
 			logger.error("Polarimeter Grid scan: Front pinhole enumerator not found");
 		}
 		fluxMonitorChannelLabel = frontPinhole.getFluxMonitorChannelLabel();
 
 		// if this scan has a child scan, then it should collect from that child
 		// its data handler and list of scannables and detectors
 		if (this.childScan != null) {
 			// inform the nested scan that it is a child scan
 			this.childScan.setIsChild(true);
 
 			// add to the list of scannables all the scannables in the child scan
 			// in this way, the top level scan has a list of all the dimensions
 			// which the collection of scan use
 			// this will not affect the movement in this scan as the scannable
 			// (dimension)
 			// which this object scans over will still be at index position 0.
 
 			for (Scannable scannable : childScan.allScannables) {
 				if (!allScannables.contains(scannable)) {
 					allScannables.add(scannable);
 				}
 			}
 
 			// and in the same way build a list of all detectors
 			for (Detector detector : childScan.allDetectors) {
 				if (!allDetectors.contains(detector)) {
 					allDetectors.add(detector);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns whether a child scan has been requested.
 	 * 
 	 * @return a boolean indicating whether the gridscan has an associated childscan
 	 */
 	public boolean hasChild() {
 		return childScan != null;
 	}
 
 	/**
 	 * Swaps previously measured flux value in data point
 	 * 
 	 * @param point
 	 *            current ScanDataPoint *
 	 */
 	private void swapFluxValue(ScanDataPoint point) {
 		Vector<Object> data = point.getDetectorData();
 		Object element = data.elementAt(0);//
 		double[] values = (double[]) element;
 		for (Detector detector : allDetectors) {
 			for (int i = 0; i < detector.getExtraNames().length; i++) {
 				if (detector.getExtraNames()[i].equals(fluxMonitorChannelLabel)) {
 					values[i] = Double.valueOf(fluxValue);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Measures flux value by moving pinhole with detector behind it into beam, taking measurement, then moving clear
 	 * pinhole into beam.
 	 * 
 	 * @throws Exception
 	 */
 	protected void measureFluxValue() throws Exception {
 		// Measurea and stores flux value locally
 		try {
 			Object pinhole = this.pinholeNumber;
 			String pinholeIdent = "PH" + pinhole.toString();
 			String pinholeDetectorIdent = "DET" + pinhole.toString();
 			// Move to detector position
 			frontPinhole.moveTo(pinholeDetectorIdent);
 			do {
 				Thread.sleep(200);
 			} while (frontPinhole.getPositionerStatus() == EnumPositionerStatus.MOVING);
 
 			// Measure flux
 			for (Detector detector : allDetectors) {
 				checkForInterrupts();
 				detector.collectData();
 			}
 			checkForInterrupts();
 			// Put appropriate value in local storage
 			ScanDataPoint point = new ScanDataPoint();
 			point.setUniqueName(name);
 			point.addScannablesAndDetectors(allScannables, allDetectors);
 			point.setCurrentFilename(getDataWriter().getCurrentFileName());
 			point.setHasChild(hasChild());
 			fluxValue = getFluxValue(point);// "234.567";
 			// Move back to pinhole position
 			frontPinhole.moveTo(pinholeIdent);
 			do {
 				Thread.sleep(200);
 			} while (frontPinhole.getPositionerStatus() == EnumPositionerStatus.MOVING);
 		} catch (Exception e) {
 			if (e instanceof InterruptedException) {
 				throw e;
 			}
 			throw new Exception("PolarimeterGridScan.measureFluxValue(): " + e.getMessage());
 		}
 
 	}
 
 	/**
 	 * Gets measured flux value from data point
 	 * 
 	 * @param point
 	 *            current ScanDataPoint *
 	 */
 	private String getFluxValue(ScanDataPoint point) {
 		String value = "";
 		Vector<Object> data = point.getDetectorData();
 		Object element = data.elementAt(0);//
 		double[] values = (double[]) element;
 		for (Detector detector : allDetectors) {
 			for (int i = 0; i < detector.getExtraNames().length; i++) {
 				if (detector.getExtraNames()[i].equals(fluxMonitorChannelLabel)) {
 					value = String.valueOf(values[i]);
 				}
 			}
 		}
 		return value;
 	}
 }
