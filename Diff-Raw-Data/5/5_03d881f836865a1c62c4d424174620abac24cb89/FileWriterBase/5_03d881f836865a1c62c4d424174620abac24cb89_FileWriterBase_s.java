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
 
 import gda.data.PathConstructor;
 import gda.device.detector.areadetector.v17.NDFile;
 import gda.device.detector.nxdetector.NXFileWriterPlugin;
 import gda.jython.InterfaceProvider;
 import gda.scan.ScanInformation;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.InitializingBean;
 
 public abstract class FileWriterBase implements NXFileWriterPlugin, InitializingBean{
 
 	private NDFile ndFile;
 	
 	protected String fileTemplate;
 	
 	private String filePathTemplate;
 	
 	private String fileNameTemplate;
 	
 	private Long fileNumberAtScanStart;
 
 	private boolean enableDuringScan = true;
 
 	private boolean setFileNameAndNumber = true;
 	
 	abstract protected void disableFileWriting() throws Exception;
 	
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (ndFile == null)
 			throw new IllegalStateException("NDFile is null");
 		if (fileTemplate == null)
 			throw new IllegalStateException("fileTemplate is null");
 		if (filePathTemplate == null)
 			throw new IllegalStateException("filePathTemplate is null");
 		if (fileNameTemplate == null)
 			throw new IllegalStateException("fileNameTemplate is null");
 		if (fileNumberAtScanStart == null)
 			throw new IllegalStateException("fileNumberAtScanStart is null");
 	}
 	
 	public void setNdFile(NDFile ndFile) {
 		this.ndFile = ndFile;
 	}
 	
 	/**
 	 * File template to pass directly to AreaDetector. AreaDetector's NDFilePlugin
 	 * (http://cars.uchicago.edu/software/epics/NDPluginFile.html) will always apply arguments to the template in the
 	 * order filepath, filename, filenumber
 	 * 
 	 * @param fileTemplate
 	 */
 	public void setFileTemplate(String fileTemplate) {
 		this.fileTemplate = fileTemplate;
 	}
 	
 	/**
 	 * The file path to pass to AreadDetector, with $scan$ resolving to the currently running scan number, and $datadir$
 	 * to the current scan file's directory.
 	 * 
 	 * @param filePathTemplate
 	 */
 	public void setFilePathTemplate(String filePathTemplate) {
 		this.filePathTemplate = filePathTemplate;
 	}
 
 	/**
 	 * The file name to pass to AreadDetector, with $scan$ resolving to the currently running scan number, and $datadir$
 	 * to the current scan file's directory.
 	 * 
 	 * @param fileNameTemplate
 	 */
 	public void setFileNameTemplate(String fileNameTemplate) {
 		this.fileNameTemplate = fileNameTemplate;
 	}
 	
 	/**
 	 * If non-negative, the file number to preset in AreaDetector before staring the exposure
 	 * @param fileNumberAtScanStart
 	 */
 	public void setFileNumberAtScanStart(long fileNumberAtScanStart) {
 		this.fileNumberAtScanStart = fileNumberAtScanStart;
 	}
 
 	public void setSetFileNameAndNumber(boolean setFileNameAndNumber) {
 		this.setFileNameAndNumber = setFileNameAndNumber;
 	}
 	
 	public NDFile getNdFile() {
 		return ndFile;
 	}
 	/**
 	 * @return the file template to configure in AreaDetector
 	 */
 	protected String getFileTemplate() {
 		return fileTemplate;
 	}
 
 
 	public String getFilePathTemplate() {
 		return filePathTemplate;
 	}
 	
 
 	public String getFileNameTemplate() {
 		return fileNameTemplate;
 	}
 
 	public long getFileNumberAtScanStart() {
 		return fileNumberAtScanStart;
 	}
 
 	public boolean isSetFileNameAndNumber() {
 		return setFileNameAndNumber;
 	}
 
 	
 	/**
 	 * @return the file path to configure in AreaDetector
 	 */
 	protected String getFilePath() {
 		return substituteScan(substituteDatadir(getFilePathTemplate()));
 	}
 
 	/** 
 	 * Only if the path template starts with the datadir, return a path relative to it, otherwise
 	 * return an absolute path.
 	 */
 	protected String getFilePathRelativeToDataDirIfPossible() {
 		String template = getFilePathTemplate();
 		if (StringUtils.startsWith(template, "$datadir$")) {
 			template = StringUtils.replace(template, "$datadir$","");
 			template = StringUtils.removeStart(template, "/");
 		} else {
 			template = substituteDatadir(template);
 		}
 		return substituteScan(template);
 	}
 
 	private String substituteDatadir(String template) {
 		
 		if (StringUtils.contains(template, "$datadir$")) {
 			template = StringUtils.replace(template, "$datadir$", PathConstructor.createFromDefaultProperty());
 		}
 
 		return template;
 	}
 	
 	private String substituteScan(String template) {
 		
 		if (StringUtils.contains(template, "$scan$")) {
 			long scanNumber;
 			try {
 				scanNumber = getScanNumber();
 			} catch (Exception e) {
 				throw new IllegalStateException("Could not determine current scan number", e);
 			}
 			template = StringUtils.replace(template, "$scan$", String.valueOf(scanNumber));
 		}
 		return template;
 	}
 
 	/**
 	 * @return the file name to configure in AreaDetector
 	 */
 	protected String getFileName() {
 		return substituteScan(substituteDatadir(getFileNameTemplate()));
 	}
 	
 	/**
 	 * Value of Input Array port in plugin
 	 */
 	private String ndArrayPortVal="";
 	
 
 	public String getNdArrayPortVal() {
 		return ndArrayPortVal;
 	}
 
 	public void setNdArrayPortVal(String ndArrayPortVal) {
 		this.ndArrayPortVal = ndArrayPortVal;
 	}
 
 	@Override
 	public void stop() throws Exception {
 		if(isEnabled())
 			disableFileWriting();
 	}
 
 	@Override
 	public void atCommandFailure() throws Exception {
 		if(isEnabled())
 			stop();
 	}
 	
 	public void setEnabled(boolean enableDuringScan) {
 		this.enableDuringScan = enableDuringScan;
 	}
 
 	public boolean isEnabled() {
 		return enableDuringScan;
 	}
 
 	public void enableCallback(boolean enable) throws Exception {
 		if( enable )
 			ndFile.getPluginBase().enableCallbacks();
 		else
 			ndFile.getPluginBase().disableCallbacks();
 	}	
 	
 	/**
 	 * 
 	 * @return The unique scanNubmer from the current scan.
 	 * @throws Exception 
 	 */
 	protected long getScanNumber() throws Exception{
 		ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
 		if (scanInformation == null || scanInformation.getScanNumber() == null ){
			throw new Exception("ScanNumber not available");
 		}
 		return scanInformation.getScanNumber();
 
 	}
 
 	@Override
 	public String getFullFileName() throws Exception {
 		return ndFile.getFullFileName_RBV();
 	}
 	
 	
 	protected void setNDArrayPortAndAddress() throws Exception {
 		if( ndArrayPortVal != null && ndArrayPortVal.length()>0)
 			ndFile.getPluginBase().setNDArrayPort(ndArrayPortVal);
 	}
 	
 	@Override
 	public String getName() {
 		return "filewriter";
 	}
 
 	@Override
 	public boolean willRequireCallbacks() {
 		return isEnabled(); // always requires callbacks if enabled
 	}
 
 	@Override
 	public void prepareForLine() throws Exception {
 		
 	}
 
 	@Override
 	public void completeLine() throws Exception {
 	}
 
 	@Override
 	public void completeCollection() throws Exception {
 	}
 
 }
