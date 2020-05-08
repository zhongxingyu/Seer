 package com.seitenbau.common;
 
 import java.io.File;
 import java.io.FileNotFoundException;
import java.net.URL;
 import java.util.logging.Logger;
 
 /**
  * Helper class for reading and writing files.
  * 
  * @author nkunstek
  */
 public abstract class FileHelper {
 
 	/** The logger. */
 	private static final Logger LOG = Logger.getLogger("FileHelper");
 
 	/**
 	 * Returns the file of the given relative file path name.
 	 * 
 	 * @param filePath
 	 *            the relative file path name.
 	 * @return the file object.
 	 * @throws FileNotFoundException
 	 *             exception when file cannot be found.
 	 */
 	public static File getFile(String filePath) throws FileNotFoundException {
 		String method = "getFile(): ";
 		LOG.info(method + "Start");
 
		URL fileUrl = ClassLoader.getSystemResource(filePath);
		if (fileUrl == null) {
			String msg = "Cannot find file: " + filePath;
 			LOG.info(method + msg);
			throw new FileNotFoundException(filePath);
 		}
 
 		LOG.info(method + "End");
		return new File(fileUrl.getFile());
 	}
 }
