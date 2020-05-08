 package com.srchaven.siwa.retired.transformers;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.springframework.integration.annotation.Transformer;
 
 /**
  * Transformer that moves a file from its current location to the processing directory and outputs a reference to the
  * file at its new location.
  */
 //TODO: Bean validation
//TODO: Change from transformer to service/service activator. Possibly collapse all file utilties into a single bean?
 public class FileStager
 {
 	/** The logger for this class */
 	private static final Logger LOGGER = Logger.getLogger(FileStager.class);
 
 	/** Reference to the processing directory */
 	private File processingDirectory;
 
 	/**
 	 * Setter.
 	 * 
 	 * @param _processingDirPath the absolute path to the processing directory.
 	 */
 	public void setProcessingDirectory(String _processingDirPath)
 	{
 		processingDirectory = new File(_processingDirPath);
 	}
 
 	/**
 	 * Moves a file from its current location to the processing directory and outputs a reference to the file at its
 	 * new location.
 	 * 
 	 * @param _file the file to move to the processing directory
 	 * 
 	 * @return a reference to the file in its new location (in the processing directory).
 	 */
 	@Transformer
 	public File stage(File _file)
 	{
 		File stagedFile = new File(processingDirectory, _file.getName());
 		_file.renameTo(stagedFile);
 //TODO: Error handling for failure
 
 		LOGGER.info("Staged file from " + _file.getPath() + " to " + stagedFile.getPath());
 
 		return stagedFile;
 	}
 
 }
