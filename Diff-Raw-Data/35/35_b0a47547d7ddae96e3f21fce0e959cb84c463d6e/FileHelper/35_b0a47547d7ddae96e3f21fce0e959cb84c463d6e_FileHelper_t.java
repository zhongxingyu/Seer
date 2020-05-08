 package com.seitenbau.common;
 
 import java.io.File;
 import java.io.FileNotFoundException;
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
 
		File file = new File(filePath);
		if (!file.exists()) {
			String msg = "File does not exists: " + filePath;
 			LOG.info(method + msg);
			throw new FileNotFoundException(msg);
 		}
 
 		LOG.info(method + "End");
		return file;
 	}
 }
