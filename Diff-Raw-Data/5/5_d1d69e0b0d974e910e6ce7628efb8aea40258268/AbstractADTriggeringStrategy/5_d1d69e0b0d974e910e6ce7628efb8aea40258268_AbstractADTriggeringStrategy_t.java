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
 
 package gda.device.detector.addetector.triggering;
 
 import gda.device.DeviceException;
 import gda.device.detector.areadetector.v17.ADBase;
 import gda.device.detector.nxdata.NXDetectorDataAppender;
 import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
 import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
 import gda.scan.ScanInformation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Vector;
 
 import org.springframework.beans.factory.InitializingBean;
 
 abstract public class AbstractADTriggeringStrategy implements AsyncNXCollectionStrategy, InitializingBean{
 
 	private final ADBase adBase;
 
	private double readoutTime = 0.1; // TODO: Should default to 0, change setReadoutTime javadoc if this changes.
 	
 	private boolean readAcquisitionTime = true;
 
 	private boolean readAcquisitionPeriod = false;
 
 	private Boolean generateCallbacks = null;
 	
 	AbstractADTriggeringStrategy(ADBase adBase) {
 		this.adBase = adBase;
 	}
 	
 	/**
 	 * Sets the required readout/dwell time (t_period - t_acquire).
 	 * <p>
	 * Defaults to 0.1 (Should be 0)
 	 * 
 	 * @param readoutTime
 	 */
 	public void setReadoutTime(double readoutTime) {
 		this.readoutTime = readoutTime;
 	}
 	
 	public void setReadAcquisitionTime(boolean readAcquisitionTime) {
 		this.readAcquisitionTime = readAcquisitionTime;
 	}
 
 	public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {
 		this.readAcquisitionPeriod = readAcquisitionPeriod;
 	}
 
 	@Override
 	public void setGenerateCallbacks(boolean b) {
 		this.generateCallbacks = b;
 		
 	}
 
 	@Override
 	public boolean isGenerateCallbacks() {
 		return generateCallbacks;
 	}
 	/**
 	 * Get the required readout/dwell time (t_period - t_acquire).
 	 */
 	public double getReadoutTime() {
 		return readoutTime;
 	}
 	
 	public ADBase getAdBase() {
 		return adBase;
 	}
 	
 	public boolean isReadAcquisitionTime() {
 		return readAcquisitionTime;
 	}
 	
 	public boolean isReadAcquisitionPeriod() {
 		return readAcquisitionPeriod;
 	}
 
 	@Override
 	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
 		throw new UnsupportedOperationException("Must be operated via prepareForCollection(collectionTime, numberImagesPerCollection)");
 	}
 
 	/**
 	 * IMPORTANT: Implementations must call enableOrDisableCallbacks()
 	 */
 	@Override
 	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
 		enableOrDisableCallbacks();
 	}
 
 	protected final void enableOrDisableCallbacks() throws Exception {
 		if (generateCallbacks != null) {
 			getAdBase().setArrayCallbacks(isGenerateCallbacks() ? 1 : 0);
 		}
 	}
 	
 	@Override
 	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
 		if (getReadoutTime() < 0) {
 			getAdBase().setAcquirePeriod(0.0);
 		} else {
 			getAdBase().setAcquirePeriod(collectionTime + getReadoutTime());
 		}
 		getAdBase().setAcquireTime(collectionTime);
 	}
 	
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if( adBase == null)
 			throw new RuntimeException("adBase is not set");
 	}
 	
 	@Override
 	public String getName() {
 		return "driver";
 	}
 
 	@Override
 	public boolean willRequireCallbacks() {
 		return false;
 	}
 	
 	@Override
 	public void prepareForLine() throws Exception {
 	}
 
 	@Override
 	public void completeLine() throws Exception {
 	}
 	
 	@Override
 	public List<String> getInputStreamNames() {
 		List<String> fieldNames = new ArrayList<String>();
 		if (isReadAcquisitionTime()) {
 			fieldNames.add("count_time");
 		}
 		if (isReadAcquisitionPeriod()) {
 			fieldNames.add("period");
 		}
 		return fieldNames;
 	}
 
 	@Override
 	public List<String> getInputStreamFormats() {
 		List<String> formats = new ArrayList<String>();
 		if (isReadAcquisitionTime()) {
 			formats.add("%.2f");
 		}
 		if (isReadAcquisitionPeriod()) {
 			formats.add("%.2f");
 		}
 		return formats;
 	}
 
 	@Override
 	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
 		List<Double> times = new ArrayList<Double>();
 		if (isReadAcquisitionTime()) {
 			try {
 				times.add(getAcquireTime());
 			} catch (Exception e) {
 				throw new DeviceException(e);
 			}
 		}
 		if (isReadAcquisitionPeriod()) {
 			try {
 				times.add(getAcquirePeriod());
 			} catch (Exception e) {
 				throw new DeviceException(e);
 			}
 		}
 		Vector<NXDetectorDataAppender> vector = new Vector<NXDetectorDataAppender>();
 		vector.add(new NXDetectorDataDoubleAppender(getInputStreamNames(), times));
 		return vector;
 	}
 	@Override
 	public boolean requiresAsynchronousPlugins() {
 		return false; //This is fine for software triggered cameras
 	}
 
 }
