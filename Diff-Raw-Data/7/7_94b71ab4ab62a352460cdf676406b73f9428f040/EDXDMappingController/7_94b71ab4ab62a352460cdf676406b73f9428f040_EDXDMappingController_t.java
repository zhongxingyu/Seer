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
 
 package gda.device.detector.xmap.edxd;
 
 import gda.device.DeviceException;
 import gda.device.detector.areadetector.v17.NDFileHDF5;
 import gda.device.epicsdevice.EpicsMonitorEvent;
 import gda.device.epicsdevice.FindableEpicsDevice;
 import gda.device.epicsdevice.ReturnType;
 import gda.factory.Configurable;
 import gda.factory.FactoryException;
 import gda.observable.IObserver;
 import gov.aps.jca.TimeoutException;
 import gov.aps.jca.dbr.DBR_Enum;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class describes the EDXD detector on I12, it is made up of 24 subdetectors
  */
 public class EDXDMappingController extends EDXDController implements Configurable {
 
 	// Setup the logging facilities
 	private static final Logger logger = LoggerFactory.getLogger(EDXDMappingController.class);
 
 	private int elementOffset = 0;
 	
 	private static final String STOPALL = "STOPALL";
 	private static final String ERASESTART = "ERASESTART";
 	private static final String ERASEALL = "ERASEALL";
 	private static final String COLLECTIONMODE = "COLLECTMODE";
 	private static final String PIXELADVANCEMODE = "PIXELADVANCEMODE";
 	private static final String IGNOREGATE = "IGNOREGATE";
 	private static final String AUTOPIXELSPERBUFFER = "AUTOPIXELSPERBUFFER";
 	private static final String PIXELSPERBUFFER ="PIXELSPERBUFFER";
 	private static final String PIXELSPERRUN ="PIXELSPERRUN";
 	private static final String CAPTURE = "NEXUS:Capture";
 	private static final String NEXUSFILEFORMAT = "NEXUS:FileTemplate";
 	private static final String NEXUSFILEWRITEMODE = "NEXUS:FileWriteMode";
 	private static final String CALLBACK = "NEXUS:EnableCallbacks";
 	private static final String NEXUSFILEPATH = "NEXUS:FilePath";
 	private static final String NEXUSFILENAME = "NEXUS:FileName";
 	private static final String FILENUMBER = "NEXUS:FileNumber";
 	private static final String NEXUSTEMPFILENAME = "TemplateFileName";
 	private static final String NEXUSTEMPFILEPATH = "TemplateFilePath";
 	
 	protected NDFileHDF5 hdf5;
 	
 	public NDFileHDF5 getHdf5() {
 		return hdf5;
 	}
 	public void setHdf5(NDFileHDF5 hdf5) {
 		this.hdf5 = hdf5;
 	}
 	public int getElementOffset() {
 		return elementOffset;
 	}
 
 	public void setElementOffset(int elementOffset) {
 		this.elementOffset = elementOffset;
 	}
 
 	/**
 	 * Basic constructor, nothing done in here, waiting for configure
 	 */
 	public EDXDMappingController() {
 		version = 3 ;
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 		//FIXME this should be pulled in from the spring configuration
 		//TODO Please be aware that use of the finder prevents others from understanding how the code flows!
 		xmap = new FindableEpicsDevice();
 		xmap.setDeviceName(epicsDeviceName);
 		xmap.setName(epicsDeviceName);
 		xmap.configure();
         statusChannel = xmap.createEpicsChannel(ReturnType.DBR_NATIVE, STATUS , "");
         statusChannel.addIObserver(new IObserver(){
 
 			@Override
 			public void update(Object source, Object arg) {
 				logger.debug("the status update from xmap is " + arg);
 				if(arg instanceof EpicsMonitorEvent){
 					EpicsMonitorEvent evt = (EpicsMonitorEvent) arg;					
 					isBusy = ((DBR_Enum)evt.epicsDbr).getEnumValue()[0] == 1;
 				}
 				else
 					isBusy = false;
 				try {
 					notifyIObservers(this, getStatusObject());			
 					logger.debug("acquisition status updated to {}", getStatus());
 				} catch (DeviceException e) {
 					logger.error("ln351 : AcqStatusListener , error ", e);
 				}
 			}
         });
 		addElements();
 	}
 
 	// Add all the EDXD Elements to the detector
 	private void addElements(){
 		for(int i = (0+ elementOffset); i < (numberOfElements + elementOffset); i++ )
 			subDetectors.add(new EDXDMappingElement(xmap,i)); 
 	}
 	
 	/**
 	 * Sets the dynamic range of the detector
 	 * @param dynamicRange the dynamic range in KeV
 	 * @return the actual value which has been set
 	 * @throws DeviceException
 	 */
 	@Override
 	public double setDynamicRange(double dynamicRange) throws DeviceException {
 		xmap.setValue("SETDYNRANGE","",dynamicRange);
 		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GETDYNRANGE"+elementOffset,"");
 	}
 
 	/**
 	 * get the maximum number of ROI allowed per mca element
 	 * @return number of rois
 	 * @throws DeviceException
 	 */
 	@Override
 	public int getMaxAllowedROIs() throws DeviceException {
 		//TODO not sure about the number
 		return 32;
 	}
 	
 	/**
 	 * Activate the ROI mode in the controller
 	 * @throws DeviceException
 	 */
 	@Override
 	public void activateROI() throws DeviceException{
 	}
 	
 	/** Disable  the ROI mode in the Controller
 	 * @throws DeviceException
 	 */
 	@Override
 	public void deactivateROI() throws DeviceException{
 	}
 
 	/**
 	 * Start data acquisition in the controller. Uses the exisiting resume mode
 	 * @throws DeviceException
 	 */
 	@Override
 	public void start() throws DeviceException {
 		xmap.setValueNoWait(ERASESTART,"",1);
 	}
 	
 	@Override
 	public void stop() throws DeviceException {
 		xmap.setValueNoWait(STOPALL,"",1);
 	}
 	
 	/**
 	 * Controller has two modes of operation.
 	 * clear on start or resume acquiring into the same spectrum
 	 * @param resume
 	 * @throws DeviceException
 	 */
 	@Override
 	public void setResume(boolean resume)throws DeviceException{
 	    if(!resume)
 	    	xmap.setValueNoWait(ERASEALL  ,"",1);
 	}
 	
 	public void clear()throws DeviceException{
 		 xmap.setValueNoWait(ERASEALL  ,"",1);
 	}
 	 
 	public void clearAndStart()throws DeviceException{
 		 xmap.setValueNoWait(ERASESTART  ,"",1);
 	}
 	 
//	@Override
//	public void setAquisitionTime(double collectionTime)throws DeviceException {
//		// TAKE NEWER VERSION OF THIS FIX (from 8.40 or master)
//	}
 	
 	public void setCollectionMode(COLLECTION_MODES mode) throws DeviceException{
 		xmap.setValueNoWait(COLLECTIONMODE, "", mode.ordinal());
 	}
 	 
 	public void setPixelAdvanceMode(PIXEL_ADVANCE_MODE mode) throws DeviceException{
 		xmap.setValueNoWait(PIXELADVANCEMODE, "", mode.ordinal());
 	 }
 	 
 	public void setIgnoreGate(boolean yes) throws DeviceException{
 		if (yes)
 			xmap.setValueNoWait(IGNOREGATE, "", 1);
 		else
 			xmap.setValueNoWait(IGNOREGATE, "", 0);
 	}
 	
 	public void setAutoPixelsPerBuffer(boolean auto) throws DeviceException{
 		if(auto)
 			xmap.setValueNoWait(AUTOPIXELSPERBUFFER, "", 1);//set as auto
 		else
 			xmap.setValueNoWait(AUTOPIXELSPERBUFFER, "", 0);//set as manual
 	}
 	
 	public void setPixelsPerBuffer(int number) throws DeviceException{
 		 xmap.setValueNoWait(PIXELSPERBUFFER, "", number);
 	}
 	
 	public void setPixelsPerRun(int number) throws DeviceException{
 		 xmap.setValue(PIXELSPERRUN, "", number);
 	}
 	
 	//hdf5 commands
 	public void resetCounters() throws Exception {
 		hdf5.getFile().getPluginBase().setDroppedArrays(0);
 		hdf5.getFile().getPluginBase().setArrayCounter(0);
 	}
 
 	public void startRecording() throws Exception {
 		if (hdf5.getCapture() == 1)
 			throw new DeviceException("detector found already saving data when it should not be");
 		hdf5.startCapture();
 		int totalmillis = 60 * 1000;
 		int grain = 25;
 		for (int i = 0; i < totalmillis / grain; i++) {
 			if (hdf5.getCapture() == 1)
 				return;
 			Thread.sleep(grain);
 		}
 		throw new TimeoutException("Timeout waiting for hdf file creation.");
 	}
 
 	public void endRecording() throws Exception {
 		// writing the buffers can take a long time
 		int totalmillis = 1* 1000;
 		int grain = 25;
 		for (int i = 0; i < totalmillis/grain; i++) {
 			if (hdf5.getFile().getCapture_RBV() == 0) return;
 			Thread.sleep(grain);
 		}
 		hdf5.stopCapture();
 		logger.warn("Waited very long for hdf writing to finish, still not done. Hope all we be ok in the end.");
 		if (hdf5.getFile().getPluginBase().getDroppedArrays_RBV() > 0)
 			throw new DeviceException("sorry, we missed some frames");
 	}
 
 	public String getHDFFileName() throws Exception {
 		return hdf5.getFullFileName_RBV();
 	}
 
 	public void setDirectory(String dataDir) throws Exception {
 		hdf5.setFilePath(dataDir);		
 		if( !hdf5.getFile().filePathExists())
 			throw new Exception("Path does not exist on IOC '" + dataDir + "'");		
 	}
 
 	public void setFileNumber(Number scanNumber) throws Exception {
 		hdf5.setFileNumber(scanNumber.intValue());		
 	}
 
 	public void setFilenamePrefix(String beamline) throws Exception {
 		hdf5.setFileName(beamline);		
 	}
 
 	public void setFilenamePostfix(String name) throws Exception {
 		hdf5.setFileTemplate(String.format("%%s%%s-%%d-%s.h5", name));
 	}
 	
 	//Nexus related commands
 	public void setNexusCapture(int number) throws DeviceException{
 		xmap.setValueNoWait(CAPTURE, "", number);
 	}
 	
 	public void setHdfNumCapture(int number) throws DeviceException
 	{
 		try {
 			hdf5.setNumCapture(number);
 		} catch (Exception e) {
 			throw new DeviceException("Error setting hdf5 Numcapture", e);
 		}
 	}
 	 
 	public void setNexusFileFormat(String format) throws DeviceException{
 		 xmap.setValueNoWait(NEXUSFILEFORMAT, "", format);
 	}
 	
 	public void setFileWriteMode(NEXUS_FILE_MODE mode) throws DeviceException{
 		 xmap.setValueNoWait(NEXUSFILEWRITEMODE, "", mode.ordinal());
 	}
 	 
 	public void setCallback(boolean yes) throws DeviceException{
 		if(yes)
 			xmap.setValueNoWait(CALLBACK, "",1);
 		else
 			xmap.setValueNoWait(CALLBACK, "",0);
 	}
 	
 	public void setNexusFileName(String filename) throws DeviceException{
 		xmap.setValueNoWait(NEXUSFILENAME, "",filename);
 	}
 	
 	public String getNexusFileName() throws DeviceException{
 		 return xmap.getValueAsString(NEXUSFILENAME, "");
 	}
 	
 	public String getNexusFilePath() throws DeviceException{
 		return xmap.getValueAsString(NEXUSFILEPATH, "");
 	}
 
 	public void setNexusFilePath(String filepath) throws DeviceException{
 		xmap.setValueNoWait(NEXUSFILEPATH, "",filepath);
 	}
 	
 	public int getFileNumber() throws DeviceException{
 		int fileNumber =  (Integer) xmap.getValue(ReturnType.DBR_NATIVE,FILENUMBER  ,"");
 		return fileNumber;
 	}
 	 
 	public void setTemplateFileName(String templateFileName) throws DeviceException{
 		xmap.setValueNoWait(NEXUSTEMPFILENAME, "",templateFileName);
 	}
 	
 	public void setTemplateFilePath(String tempFilePath) throws DeviceException{
 		xmap.setValueNoWait(NEXUSTEMPFILEPATH, "",tempFilePath);
 	}
 	 
 	public boolean getCaptureStatus(){
 		int status = hdf5.getStatus();
 		if(status == 1)
 			return true;
 		return false;
 	}
 
 	public boolean isBufferedArrayPort() throws Exception{
 		if( hdf5.getArrayPort().equals("xbuf"))
 			return true;
 		return false;
 	}
 	
 }
