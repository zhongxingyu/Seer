 package com.srchaven.siwa.retired.transformers;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.integration.annotation.Transformer;
 
 import com.srchaven.siwa.model.Observation;
 
 
 /**
  * Transformer that deletes a source file from the processing directory.
  */
 //TODO: Bean validation
//TODO: Change from transformer to service/service activator. Possibly collapse all file utilities into a single bean?
 public class SourceFileDeleter
 {
 	/** The logger for this class */
 	private static final Logger LOGGER = Logger.getLogger(SourceFileDeleter.class);
 
 	/** Reference to the processing directory */
 	private File processingDirectory;
 
 	/**
 	 * Setter.
 	 * 
 	 * @param _path the absolute path to the processing directory.
 	 */
 	public void setProcessingDirectory(String _path)
 	{
 		processingDirectory = new File(_path);
 	}
 
 	/**
 	 * Deletes a file in the processing directory. The {@code filename} field from the first {@code Record} in the
 	 * provided list will be extracted and used as filename of the file to be deleted. Thus, the path to the deleted
 	 * file will be:
 	 * [Processing directory]/[First record's filename] 
 	 * 
 	 * @param _records a list of the records associated with the file being deleted.
 	 * 
 	 * @return the inputed list of records
 	 */
 	@Transformer
 	public List<Observation> receive(List<Observation> _records)
 	{
 //TODO: Make sure all records are from the same file (?)
 		String filename = _records.get(0).getFilename();
 		new File(processingDirectory, filename).delete();
 		LOGGER.trace("All records received that originated in " + filename + ". Deleted.");
 		return _records;
 	}
 
 }
