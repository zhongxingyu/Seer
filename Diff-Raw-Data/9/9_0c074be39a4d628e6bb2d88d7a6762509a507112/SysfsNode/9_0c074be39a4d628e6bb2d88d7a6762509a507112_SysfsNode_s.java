 package com.buglabs.bug.sysfs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * An abstract class for files within sysfs.  This class has helper methods in dealing with the files.
  * @author kgilmer
  *
  */
 public abstract class SysfsNode {
 
 	protected final File root;
 	private static final String CRLF = System.getProperty("line.separator");
 	//private static final LogService log = LogServiceUtil.getLogService(Activator.getDefault().getBundleContext());
 
 	/**
 	 * @param root directory of sysfs node
 	 */
 	public SysfsNode(File root) {
 		if (!root.exists() || !root.isDirectory()) {
 			throw new IllegalArgumentException("Invalid sysfs directory: " + root.getAbsolutePath());
 		}
 		
 		this.root = root;
 	}
 	
 	/**
 	 * Parse a string of numbers with "/" chars, return one String that is a number.  null-safe.
 	 * @param sn raw input
 	 * @return String of stripped input
 	 */
 	protected static String parseMultiInt(String sn) {
 		if (sn == null) {
 			return "";
 		}
 		
 		String[] elems = sn.split("/");
 		String ssn = "";
 		if (elems != null) {
 			for (int i = 0; i < elems.length; ++i) {
 				ssn += parseInt(elems[i]);
 			}
 		}
 
 		return ssn;
 	}
 
 	/**
 	 * Null-safe.  Given a number like "0x2f" convert to integer.
 	 * 
 	 * @param sn raw input
 	 * @return the integer or 0 if passed value is null.  Invalid numbers will generate exceptions.
 	 */
 	protected static int parseInt(String sn) {
 		if (sn == null) {
 			return 0;
 		}
 		
 		return Integer.parseInt(sn.substring(2), 16);
 	}
 
 	/**
 	 * @param sn raw input
 	 * @return the integer as hex or 0 if passed value is null.  Invalid numbers will generate exceptions.
 	 */
 	protected static String parseHexInt(String sn) {
 		if (sn == null) {
 			return "0";
 		}
 		
 		return pad(Integer.toString(Integer.parseInt(sn.substring(2), 16), 16).toUpperCase(), 4, '0');
 	}
 
 	/**
 	 * Pad a string to length len of char j.
 	 * 
 	 * @param s raw input
 	 * @param len length
 	 * @param j pad char
 	 * @return padded string
 	 */
 	protected static String pad(String s, int len, char j) {
 
 		if (s.length() >= len) {
 			return s;
 		}
 
 		StringBuffer sb = new StringBuffer();
 
 		for (int i = 0; i < len - s.length(); ++i) {
 			sb.append(j);
 		}
 		sb.append(s);
 
 		return sb.toString();
 	}
 
 	/**
 	 * Return first line of file as a string.
 	 * 
 	 * @param file inputfile
 	 * @return first line or null if file read fails (file does not exist, etc.).
 	 */
 	protected static String getFirstLineofFile(File file) {
 		BufferedReader br;
 		try {
 			br = new BufferedReader(new FileReader(file));
 			return br.readLine();
 		} catch (IOException e) {
 			//Just ignore errors and return null;
 			return null;
 		}
 	}
 	
 
 	/**
 	 * Write a line to a file.  Will append to an existing file.
 	 * @param file file to be written to.
	 * @param line String to be written.  LF will be appended to the file.
 	 * @throws IOException on File I/O error
 	 */
 	protected void println(File file, String line) throws IOException {
 		FileOutputStream fos = new FileOutputStream(file, true);
		fos.write(line.getBytes());
		fos.write('\n');
 		fos.close();
 	}
 }
