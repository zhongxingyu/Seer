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
 
 package gda.device.detector.addetector.filewriter;
 
 import gda.device.DeviceException;
 import gda.device.detector.areadetector.v17.NDFile.FileWriteMode;
 import gda.device.detector.areadetector.v17.NDPluginBase;
 import gda.device.detector.nxdata.NXDetectorDataAppender;
 import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
 import gda.device.detector.nxdetector.NXPlugin;
 import gda.device.detectorfilemonitor.HighestExistingFileMonitor;
 import gda.device.detectorfilemonitor.HighestExitingFileMonitorSettings;
 import gda.jython.IJythonNamespace;
 import gda.jython.InterfaceProvider;
 import gda.scan.ScanBase;
 import gda.scan.ScanInformation;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /*
  * SingleImagePerFileWriter(ndFileSimulator, "detectorName", "%%s%d%%s-%%d-detname.tif",true,true);
  */
 public class SingleImagePerFileWriter extends FileWriterBase implements NXPlugin{
 
 	protected static final String FILEPATH_EXTRANAME = "filepath";
 
 	private static Logger logger = LoggerFactory.getLogger(SingleImagePerFileWriter.class);
 
 
 	private String fileNameUsed = "";
 	private String filePathUsed = "";
 	private String fileTemplateUsed = "";
 	private long nextExpectedFileNumber = 0;
 	boolean blocking = true;  
 
 	private boolean returnPathRelativeToDatadir = false; // TODO: should really be enabled by default RobW
 
 	private int SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES = 10;
 
 	private int MILLI_SECONDS_BETWEEN_POLLS = 500;
 	/*
 	 * Object that can be used observe the progress of the scan by looking for file - optional
 	 */
 	HighestExistingFileMonitor highestExistingFileMonitor = null;
 
 	private boolean waitForFileArrival = true;
 
 	private String keyNameForMetadataPathTemplate = "";
 
 	private FileWriteMode fileWriteMode = FileWriteMode.SINGLE;
 
 	public String getFileWriteMode() {
 		return fileWriteMode.toString();
 	}
 
 	public void setFileWriteMode(String fileWriteMode) {
 		this.fileWriteMode = FileWriteMode.valueOf(fileWriteMode);
 	}
 
 	private String fileTemplateForReadout = null;
 
 	/**
 	 * Creates a SingleImageFileWriter with ndFile, fileTemplate, filePathTemplate, fileNameTemplate and
 	 * fileNumberAtScanStart yet to be set.
 	 */
 	public SingleImagePerFileWriter() {
 
 	}
 
 	public boolean isBlocking() {
 		return blocking;
 	}
 
 	public void setBlocking(boolean blocking) {
 		this.blocking = blocking;
 	}
 
 	public void setWaitForFileArrival(boolean waitForFileArrival) {
 		this.waitForFileArrival = waitForFileArrival;
 	}
 
 	public boolean isWaitForFileArrival() {
 		return waitForFileArrival;
 	}
 
 	/**
 	 * Creates a SingleImageFileWriter which writes folders of files alongside the current file in the 'standard'
 	 * location (ndFile must still be configured). e.g. <blockquote>
 	 * 
 	 * <pre>
 	 * datadir
 	 *    123.dat
 	 *    123-pilatus100k-files
 	 * 00001.tif
 	 */
 	public SingleImagePerFileWriter(String detectorName) {
 		setFileTemplate("%s%s%05d.tif");
 		setFilePathTemplate("$datadir$/$scan$-" + detectorName + "-files");
 		setFileNameTemplate("");
 		setFileNumberAtScanStart(1);
 	}
 
 	public void setFileTemplateForReadout(String fileTemplateForReadout) {
 		this.fileTemplateForReadout = fileTemplateForReadout;
 	}
 
 
 	public void setHighestExistingFileMonitor(HighestExistingFileMonitor highestExistingFileMonitor) {
 		this.highestExistingFileMonitor = highestExistingFileMonitor;
 	}
 
 	public void setKeyNameForMetadataPathTemplate(String string) {
 		this.keyNameForMetadataPathTemplate = string;
 	}
 
 	public String getFileTemplateForReadout() {
 		return this.fileTemplateForReadout;
 	}
 
 
 	public HighestExistingFileMonitor getHighestExistingFileMonitor() {
 		return highestExistingFileMonitor;
 	}
 
 	public String getkeyNameForMetadataPathTemplate() {
 		return keyNameForMetadataPathTemplate;
 	}
 
 	@Override
 	public boolean appendsFilepathStrings() {
 		return isEnabled(); // will always append strings when enabled
 	}
 
 	@Override
 	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
 
 		if (!isEnabled())
 			return;
 
 		// Create filePath directory if required
 		File f = new File(getFilePath());
 		if (!f.exists()) {
 			if (!f.mkdirs())
 				throw new Exception("Folder does not exist and cannot be made:" + getFilePath());
 		}
 
 		if (isSetFileNameAndNumber()) {
 			configureNdFile();
 		} else {
 			if( !getNdFile().filePathExists())
 				if (isPathErrorSuppressed())
					logger.warn("Ignoring Path does not exist on IOC '" + filePath + "'");
 				else
					throw new Exception("Path does not exist on IOC '" + filePath + "'");
 		}
 		clearWriteStatusErr();
 
 		setNDArrayPortAndAddress();
 		NDPluginBase pluginBase = getNdFile().getPluginBase();
 		if (pluginBase != null) {
 			pluginBase.enableCallbacks();
 			logger.warn("Detector will block the AreaDetectors acquisition thread while writing files");
 			pluginBase.setBlockingCallbacks((short)(blocking? 1:0));
 			// It should be possible to avoid blocking the acquisition thread
 			// and use the pipeline by setting BlockingCallbacks according to
 			// returnExpectedFileName, but when this was tried, at r48170, it
 			// caused the files to be corrupted.
 		} else {
 			logger.warn("Cannot ensure callbacks and blocking callbacks are enebled as pluginBase is not set");
 		}
 
 		getNdFile().setFileWriteMode(fileWriteMode);
 		if (fileWriteMode == FileWriteMode.CAPTURE || fileWriteMode == FileWriteMode.STREAM) {
 			getNdFile().setNumCapture(1);
 		}
 		if (!getkeyNameForMetadataPathTemplate().isEmpty()) {
 			addPathTemplateToMetadata();
 		}
 	}
 
 	private void addPathTemplateToMetadata() {
 		IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
 		String existingMetadataString = (String) jythonNamespace.getFromJythonNamespace("SRSWriteAtFileCreation");
 		String newMetadataString;
 		if (existingMetadataString == null) {
 			newMetadataString = "";
 		} else {
 			newMetadataString = existingMetadataString;
 		}
 
 		String filePathRelativeToDataDirIfPossible = getFilePathRelativeToDataDirIfPossible();
 		String template = (getFileTemplateForReadout() == null) ? getFileTemplate() : getFileTemplateForReadout();
 		String newValue = StringUtils.replaceOnce(template, "%s", filePathRelativeToDataDirIfPossible + "/");
 		newValue = StringUtils.replaceOnce(newValue, "%s", getFileName());
 		String newKey = getkeyNameForMetadataPathTemplate();
 		jythonNamespace.placeInJythonNamespace("SRSWriteAtFileCreation", newMetadataString + newKey + "='" + newValue
 				+ "'\n");
 		InterfaceProvider.getTerminalPrinter().print("Image location: " + newKey + "='" + newValue);
 	}
 
 	protected void configureNdFile() throws Exception {
 
 		fileTemplateUsed = getFileTemplate();
 		getNdFile().setFileTemplate(fileTemplateUsed);
 
 		filePathUsed = getFilePath();
 		if (!filePathUsed.endsWith(File.separator))
 			filePathUsed += File.separator;
 		File f = new File(filePathUsed);
 		if (!f.exists()) {
 			if (!f.mkdirs())
 				throw new Exception("Folder does not exist and cannot be made:" + filePathUsed);
 		}
 		getNdFile().setFilePath(filePathUsed);
 
 		if (!getNdFile().filePathExists())
 			throw new Exception("Path does not exist on IOC '" + filePathUsed + "'");
 
 		fileNameUsed = getFileName();
 		getNdFile().setFileName(fileNameUsed);
 
 		if (getFileNumberAtScanStart() >= 0) {
 			getNdFile().setFileNumber((int) getFileNumberAtScanStart());
 			nextExpectedFileNumber = getFileNumberAtScanStart();
 		} else {
 			nextExpectedFileNumber = getNdFile().getFileNumber();
 		}
 
 		getNdFile().setAutoIncrement((short) 1);
 
 		getNdFile().setAutoSave((short) 1);
 
 		if (highestExistingFileMonitor != null) {
 			// remove the 2 %s from the fileTemplate to get to part after fileNameUsed
 			String postFileName = fileTemplateUsed.replaceFirst("%s", "").replaceFirst("%s", "");
 			HighestExitingFileMonitorSettings highestExitingFileMonitorSettings = new HighestExitingFileMonitorSettings(
 					filePathUsed, fileNameUsed + postFileName, (int) nextExpectedFileNumber);
 			highestExistingFileMonitor.setHighestExitingFileMonitorSettings(highestExitingFileMonitorSettings);
 			highestExistingFileMonitor.setRunning(true);
 		}
 
 	}
 
 	@Override
 	public void completeCollection() throws Exception {
 		if (!isEnabled())
 			return;
 		disableFileWriting();
 	}
 
 	@Override
 	public void disableFileWriting() throws Exception {
 		NDPluginBase filePluginBase = getNdFile().getPluginBase();
 		if (filePluginBase != null) { // camserver filewriter has no base
 			filePluginBase.disableCallbacks();
 			filePluginBase.setBlockingCallbacks((short) 0);
 		}
 		getNdFile().setFileWriteMode(FileWriteMode.STREAM);
 	}
 
 	@Override
 	public String getFullFileName() throws Exception {
 		String template = (getFileTemplateForReadout() != null) ? getFileTemplateForReadout() : fileTemplateUsed;
 		String fullFileName = String.format(template, filePathUsed, fileNameUsed, nextExpectedFileNumber);
 		nextExpectedFileNumber++;
 		return fullFileName;
 	}
 
 	@Override
 	public List<String> getInputStreamNames() {
 		return Arrays.asList(FILEPATH_EXTRANAME);
 	}
 
 	@Override
 	public List<String> getInputStreamFormats() {
 		return Arrays.asList("%.2f");
 	}
 
 	@Override
 	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
 			DeviceException {
 		ArrayList<NXDetectorDataAppender> l = new ArrayList<NXDetectorDataAppender>();
 		l.add(readNXDetectorDataAppender());
 		return l;
 	}
 	
 	/**
 	 * Returns a single NXDetectorDataAppender for the current image with each call. If isWaitForFileArrival is true,
 	 * then waits for the file to become visible before returning the appender.
 	 */
 	protected NXDetectorDataAppender readNXDetectorDataAppender()  throws NoSuchElementException, DeviceException{
 
 		String filepath;
 		boolean returnRelativePath = isReturnPathRelativeToDatadir();
 		try {
 			if (returnRelativePath) {
 				if (!StringUtils.startsWith(getFilePathTemplate(), "$datadir$")) {
 					throw new IllegalStateException(
 							"If configured to return a path relative to the datadir, the configured filePathTemplate must begin wiht $datadir$. It is :'"
 									+ getFilePathTemplate() + "'");
 				}
 				filepath = getFilePathRelativeToDataDirIfPossible();
 			} else {
 				filepath = getFullFileName();
 			}
 		} catch (Exception e) {
 			throw new DeviceException(e);
 		}
 
 		checkErrorStatus();
 		if( isWaitForFileArrival()){
 			// Now check that the file exists
 			String fullFilePath = returnRelativePath ? getAbsoluteFilePath(filepath) : filepath;
 			try {
 				File f = new File(fullFilePath);
 				long numChecks = 0;
 				while (!f.exists()) {
 					numChecks++;
 					Thread.sleep(MILLI_SECONDS_BETWEEN_POLLS);
 					checkErrorStatus();
 					// checkForInterrupts only throws exception if a scan is running. This code will run beyond that point
 					if (ScanBase.isInterrupted())
 						throw new Exception("ScanBase is interrupted whilst waiting for '" + fullFilePath + "'");
 					if ((numChecks * MILLI_SECONDS_BETWEEN_POLLS/1000) > SECONDS_BETWEEN_SLOW_FILE_ARRIVAL_MESSAGES) {
 						InterfaceProvider.getTerminalPrinter().print(
 								"Waiting for file '" + fullFilePath + "' to be created");
 						numChecks = 0;
 					}
 				}
 			} catch (Exception e) {
 				throw new DeviceException("Error checking for existence of file '" + fullFilePath + "'",e);
 			}
 		}
 
 		return new NXDetectorDataFileAppenderForSrs(filepath, FILEPATH_EXTRANAME);
 	}
 
 	public boolean isReturnPathRelativeToDatadir() {
 		return returnPathRelativeToDatadir;
 	}
 
 	public void setReturnPathRelativeToDatadir(boolean returnPathRelativeToDatadir) {
 		this.returnPathRelativeToDatadir = returnPathRelativeToDatadir;
 	}
 
 }
