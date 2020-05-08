 package org.gwaspi.global;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.nio.channels.FileChannel;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Locale;
 import org.gwaspi.constants.cGlobal;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class Utils {
 
 	private static final Logger log = LoggerFactory.getLogger(Utils.class);
 	private static final Locale timeLocale = new Locale("es", "ES");
 	private static final Locale dateLocale = new Locale("en", "US");
 	private static final DateFormat mediumTimeFormatter = DateFormat.getTimeInstance(DateFormat.MEDIUM, timeLocale);
 	private static final DateFormat longTimeFormatter = DateFormat.getTimeInstance(DateFormat.LONG, timeLocale);
 	private static final DateFormat shortDateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, dateLocale);
 	private static final DateFormat mediumDateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, dateLocale);
 	private static final DateFormat timeStampFormat = new SimpleDateFormat("ddMMyyyyhhmmssSSSS");
 	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 
 	/**
 	 * This filter only returns files, not directories
 	 */
 	private static final FileFilter FILES_ONLY_FILTER = new FileFilter() {
 		@Override
 		public boolean accept(File file) {
 			return !file.isDirectory();
 		}
 	};
 
 	private Utils() {
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="File and directory methods">
 	private static String currentAppPath = "";
 
 	public static String getAppPath() {
 		currentAppPath = cGlobal.USERDIR;
 		//JOptionPane.showMessageDialog(base.ApipelineGUI.getFrames()[0], currentAppPath);
 		return currentAppPath;
 	}
 
 	public static File createFolder(String path, String folderName) throws IOException {
 		String spoonFeeding = path + "/" + folderName;
 		File f = new File(spoonFeeding);
 		if (!f.exists() && !f.mkdir()) {
 			throw new IOException("Failed to create directory " + f.getPath());
 		}
 		return f;
 	}
 
 	/**
 	 * Deletes all files and subdirectories under dir.
 	 * Returns true if all deletions were successful.
 	 * If a deletion fails, the method stops attempting to delete and returns false.
 	 */
 	public static boolean deleteFolder(File dir) {
 		if (dir.isDirectory()) {
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++) {
 				boolean success = deleteFolder(new File(dir, children[i]));
 				if (!success) {
 					return false;
 				}
 			}
 		}
 
 		// The directory is now empty so delete it
 		return dir.delete();
 	}
 
 	public static File createFile(String path, String fileName) throws IOException {
 		String spoonFeeding = path + "/" + fileName;
 		File f = new File(spoonFeeding);
 		if (!f.exists() && !f.createNewFile()) {
 			throw new IOException("Failed to create file " + f.getPath());
 		}
 		return f;
 	}
 
 	public static File[] listFiles(String path) {
 
 		File[] files;
 
 		File dir = new File(path);
 		if (dir.isDirectory()) {
 			files = dir.listFiles(FILES_ONLY_FILTER);
 		} else {
 			File[] tmpF = new File[1];
 			tmpF[0] = dir;
 			files = tmpF;
 		}
 
 		return files;
 	}
 
 	public static void copyFile(File in, File out) throws Exception {
 
 		FileChannel inChannel = new FileInputStream(in).getChannel();
 		FileChannel outChannel = new FileOutputStream(out).getChannel();
 		try {
 			inChannel.transferTo(0, inChannel.size(), outChannel);
 		} catch (IOException ex) {
 			throw ex;
 		} finally {
 			if (inChannel != null) {
 				inChannel.close();
 			}
 			if (outChannel != null) {
 				outChannel.close();
 			}
 		}
 	}
 
 	/**
 	 * This function will copy files or directories from one location to
 	 * another. note that the source and the destination must be mutually
 	 * exclusive. This function can not be used to copy a directory to a sub
 	 * directory of itself. The function will also have problems if the
 	 * destination files already exist.
 	 *
 	 * @param src -- A File object that represents the source for the copy
 	 * @param dest -- A File object that represents the destination for the
 	 * copy.
 	 * @throws IOException if unable to copy.
 	 */
 	public static void copyFileRecursive(File src, File dest) throws IOException {
 		// Check to ensure that the source is valid...
 		if (!src.exists()) {
 			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
 		} else if (!src.canRead()) { //check to ensure we have rights to the source...
 			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
 		}
 		// is this a directory copy?
 		if (src.isDirectory()) {
 			// does the destination already exist?
 			// if not we need to make it exist if possible
 			// (NOTE this is mkdirs not mkdir)
 			if (!dest.exists() && !dest.mkdirs()) {
 				throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
 			}
 			// get a listing of files...
 			String[] list = src.list();
 			// copy all the files in the list.
 			for (int i = 0; i < list.length; i++) {
 				File dest1 = new File(dest, list[i]);
 				File src1 = new File(src, list[i]);
 				copyFileRecursive(src1, dest1);
 			}
 		} else {
 			// This was not a directory, so lets just copy the file
 			FileInputStream fin = null;
 			FileOutputStream fout = null;
 			byte[] buffer = new byte[4096]; // Buffer 4K at a time (you can change this).
 			int bytesRead;
 			try {
 				// open the files for input and output
 				fin = new FileInputStream(src);
 				fout = new FileOutputStream(dest);
 				// while bytesRead indicates a successful read, lets write...
 				while ((bytesRead = fin.read(buffer)) >= 0) {
 					fout.write(buffer, 0, bytesRead);
 				}
 			} catch (IOException ex) { // Error copying file...
 				IOException wrapper = new IOException("copyFiles: Unable to copy file: "
 						+ src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
 				wrapper.initCause(ex);
 				wrapper.setStackTrace(ex.getStackTrace());
 				throw wrapper;
 			} finally { // Ensure that the files are closed (if they were open).
 				if (fin != null) {
 					fin.close();
 				}
 				if (fout != null) {
 					fout.close();
 				}
 			}
 		}
 	}
 
 	public static void tryToDeleteFile(File toDelete) throws IOException {
 
 		if (toDelete.exists()) {
 			if (!toDelete.canWrite()) {
 				throw new IOException("Failed to delete file; write protected: " + toDelete.getPath());
 			}
 
 			boolean success = toDelete.delete();
 			if (!success) {
 				throw new IOException("Failed to delete file; reason unknown: " + toDelete.getPath());
 			}
 		}
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Date Time methods">
 	public static String getShortDateTimeForFileName() {
 
 		Date now = new Date();
 		String dateOut = shortDateFormatter.format(now);
 		dateOut = dateOut + longTimeFormatter.format(now);
 
 		return dateOut;
 	}
 
 	public static String getShortDateTimeAsString() {
 
 		Date now = new Date();
 		String dateOut = mediumDateFormatter.format(now);
 		dateOut = dateOut + " - " + longTimeFormatter.format(now);
 
 		return dateOut;
 	}
 
 	public static String getMediumDateTimeAsString() {
 
 		Date now = new Date();
 		String dateOut = mediumDateFormatter.format(now);
 		dateOut = dateOut + " " + mediumTimeFormatter.format(now);
 //		dateOut = dateOut.replace(":", "-");
 //		dateOut = dateOut.replace(" ", "-");
 //		dateOut = dateOut.replace(",", "");
 
 		return dateOut;
 	}
 
 	public static String getMediumDateAsString() {
 
 		Date now = new Date();
 		String dateOut = mediumDateFormatter.format(now);
 
 		return dateOut;
 	}
 
 	/**
 	 * @deprecated unused!
 	 */
 	public static String getSQLDateAsString() {
 
 		Date now = new Date();
 		java.sql.Date jsqlD = new java.sql.Date(now.getTime());
 
 		return jsqlD.toString();
 	}
 
 	public static String getTimeStamp() {
 
 		Calendar now = Calendar.getInstance();
 		return timeStampFormat.format(now.getTime());
 	}
 
 	/**
 	 * @deprecated unused!
 	 */
 	public static String dateToString(Date date) {
 
 		String dateStr = dateFormat.format(date);
 
 		return dateStr;
 	}
 
 	/**
 	 * @deprecated unused!
 	 */
 	public static Date stringToDate(String txtDate) throws ParseException {
 
 		Date date = dateFormat.parse(txtDate);
 
 		return date;
 	}
 
 	public static Date stringToDate(String txtDate, String format) {
 
 		Date dateDate = null;
 
 		DateFormat df = new SimpleDateFormat(format);
 		try {
 			dateDate = df.parse(txtDate);
 		} catch (ParseException ex) {
 			log.error("Failed to convert to a dat: " + txtDate, ex);
 		}
 
 		return dateDate;
 	}
 
 	public static String now(String dateFormat) {
 		Calendar now = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		return sdf.format(now.getTime());
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="String manipulation methods">
 	public static String stripNonAlphaNumeric(String s) {
 		String good =
 				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
 
 		StringBuilder result = new StringBuilder();
 		for (int i = 0; i < s.length(); i++) {
 			if (good.indexOf(s.charAt(i)) >= 0) {
 				result.append(s.charAt(i));
 			}
 		}
 
 		return result.toString();
 	}
 
 	public static String stripNonAlphaNumericDashUndscr(String s) {
 		String good =
 				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
 
 		StringBuilder result = new StringBuilder();
 		for (int i = 0; i < s.length(); i++) {
 			if (good.indexOf(s.charAt(i)) >= 0) {
 				result.append(s.charAt(i));
 			}
 		}
 
 		return result.toString();
 	}
 	// </editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="SYSTEM MANAGEMENT">
 	public static void takeOutTheGarbage() {
 		collectGarbageWithThreadSleep(0); // Poke system to try to Garbage Collect!
 	}
 
 	public static void collectGarbageWithThreadSleep(int millisecs) {
 //		try {
 //			System.gc(); //Poke system to try to Garbage Collect!
 //			if (millisecs>0) {
 //				Thread.sleep(millisecs);
 //				System.gc(); //Poke system to try to Garbage Collect!
 //			}
 //			log.info("Garbage collected");
 //		} catch (InterruptedException ex) {
 //			log.error(null, ex);
 //		}
 	}
 
 	public static boolean checkInternetConnection() {
 		boolean isConnected = false;
 		try {
 			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
 			while (interfaces.hasMoreElements()) {
 				NetworkInterface interf = interfaces.nextElement();
 				if (interf.isUp() && !interf.isLoopback()) {
 					isConnected = true;
 				}
 			}
 		} catch (SocketException ex) {
 			log.warn(null, ex);
 		}
 
 //		try {
 //			InetAddress address = InetAddress.getByName("java.sun.com");
 //			if(address != null){
 //				isConnected = true;
 //			}
 //		} catch (UnknownHostException ex) {
 //			isConnected = false;
 //		} catch (IOException ex) {
 //			isConnected = false;
 //		}
 
 		return isConnected;
 	}
 	//</editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Logging methods">
 	public static String createActualMessage(String message) {
 		return ((message == null) || message.isEmpty())
 				? "Operation"
 				: message;
 	}
 
 	public static void sysoutStart(String message) { // FIXME We dont need to add a time manually, as the logging system does that for us (also see other methods below)
 		log.info("******* Started {} *******", createActualMessage(message));
 	}
 
 	public static void sysoutCompleted(String message) {
 		log.info("===> Completed {} <===", createActualMessage(message));
 	}
 
 	public static void sysoutFinish(String message) {
 		log.info("################# Finished {} #################", createActualMessage(message));
 		log.info("");
 		log.info("");
 	}
 
 	public static void sysoutError(String message) {
 		String actualMessage = ((message == null) || message.isEmpty())
				? ""
				: " perfoming " + message;
		log.error("!!!!! Error encountered{} !!!!!", actualMessage);
 	}
 
 	/**
 	 * This logOperationInStudyDesc has now been deprecated in favor of
 	 * ProcessTab
 	 *
 	 * @deprecated Use ProcessTab instead
 	 */
 	public static void logOperationInStudyDesc(String operation, int studyId) throws IOException {
 //		StringBuffer result = new StringBuffer();
 //		try {
 //			String fileDir = Config.getConfigValue(Config.PROPERTY_LOG_DIR,"")+"/";
 //			String fileName = "Study_"+ studyId + ".log";
 //			File logFile = new File(fileDir+fileName);
 //			if(!logFile.exists()){
 //				createFile(fileDir, fileName);
 //			}
 //			StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
 //
 //			FileWriter fw = new FileWriter(fileDir+fileName);
 //			BufferedWriter bw = new BufferedWriter(fw);
 //
 //			bw.append(description.toString());
 //			bw.append(operation);
 //			bw.append("\nEnd Time: ");
 //			bw.append(Utils.getMediumDateTimeAsString());
 //			if(description.length()!=0){
 //				bw.append("\n\n");
 //			}
 //			bw.close();
 //
 //			result = description;
 //		} catch (IOException ex) {
 //			log.error(null, ex);
 //		}
 //
 //		//gui.LogTab_old.refreshLogInfo();
 	}
 
 	/**
 	 * This logStartMessageEnd has now been deprecated in favor of ProcessTab
 	 *
 	 * @deprecated Use ProcessTab instead
 	 */
 	public static void logStartMessageEnd(String startTime, String operation, String endTime, String studyId) throws IOException {
 //		StringBuffer result = new StringBuffer();
 //		try {
 //			String fileDir = Config.getConfigValue(Config.PROPERTY_LOG_DIR,"")+"/";
 //			String fileName = "Study_"+ studyId + ".log";
 //			File logFile = new File(fileDir+fileName);
 //			if(!logFile.exists()){
 //				createFile(fileDir, fileName);
 //			}
 //			StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
 //
 //			FileWriter fw = new FileWriter(fileDir+fileName);
 //			BufferedWriter bw = new BufferedWriter(fw);
 //
 //			if(description.length()!=0){
 //				bw.append("\n");
 //			}
 //			bw.write(description.toString());
 //			bw.append("\nStart Time: "+startTime + "\n");
 //			bw.append(operation);
 //			bw.append("\nEnd Time: ");
 //			bw.append(Utils.getMediumDateTimeAsString());
 //			bw.close();
 //
 //			result = description;
 //		} catch (IOException ex) {
 //			log.error(null, ex);
 //		}
 	}
 
 	/**
 	 * This logBlockInStudyDesc has now been deprecated in favor of ProcessTab
 	 *
 	 * @deprecated Use ProcessTab instead
 	 */
 	public static void logBlockInStudyDesc(String operation, int studyId) throws IOException {
 //		StringBuffer result = new StringBuffer();
 //		try {
 //			String fileDir = Config.getConfigValue(Config.PROPERTY_LOG_DIR,"")+"/";
 //			String fileName = "Study_"+ studyId + ".log";
 //			File logFile = new File(fileDir+fileName);
 //			if(!logFile.exists()){
 //				createFile(fileDir, fileName);
 //			}
 //
 //			StringBuffer description = new StringBuffer(org.gwaspi.framework.util.IOUtils.readFile(new FileReader(fileDir+fileName)));
 //
 //			FileWriter fw = new FileWriter(fileDir+fileName);
 //			BufferedWriter bw = new BufferedWriter(fw);
 //
 //			bw.append(description);
 //			bw.append(operation);
 //			bw.close();
 //
 //			result = description;
 //		} catch (IOException ex) {
 //			log.error(null, ex);
 //		}
 //
 //		//gui.LogTab_old.refreshLogInfo();
 	}
 	// </editor-fold>
 }
