 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.logging;
 
 import static java.lang.Math.max;
 import static org.oobium.logging.Logger.WARNING;
 import static org.oobium.logging.LogFormatter.format;
 import static org.oobium.logging.LoggerImpl.decode;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.text.NumberFormat;
 
 import org.osgi.framework.Bundle;
 import org.osgi.service.log.LogEntry;
 import org.osgi.service.log.LogListener;
 
 public class LogHandler implements LogListener {
 
 	/**
 	 * 1 kilobyte = 1024 bytes
 	 */
 	public static final long KB = 1024;
 
 	/**
 	 * 1 megabyte = 1024 kilobytes = 1048576 bytes
 	 */
 	public static final long MB = 1024 * KB;
 
 	/**
 	 * 1 gigabyte = 1024 megabytes = 1048576 kilobytes = 1073741824 bytes
 	 */
 	public static final long GB = 1024 * MB;
 
 	private static final NumberFormat nf = NumberFormat.getIntegerInstance();
 	static {
 		nf.setMaximumFractionDigits(0);
 		nf.setMaximumIntegerDigits(3);
 		nf.setMinimumFractionDigits(0);
 		nf.setMinimumIntegerDigits(3);
 	}
 	
 	private static long getMax() {
 		String max = System.getProperty("org.oobium.logging.file.max");
 		if(max != null) {
 			try {
 				return Long.parseLong(max);
 			} catch(NumberFormatException e1) {
 				max.replace(" ", "");
 				try {
 					if(max.toUpperCase().endsWith("KB")) {
 						return Long.parseLong(max.substring(0, max.length() - 2)) * KB;
					} else if(max.toUpperCase().endsWith("MB")) {
						return Long.parseLong(max.substring(0, max.length() - 2)) * MB;
					} else if(max.toUpperCase().endsWith("GB")) {
						return Long.parseLong(max.substring(0, max.length() - 2)) * GB;
 					}
 				} catch(NumberFormatException e2) {
 					// discard
 				}
 			}
 		}
 		return MB; // default to 1 megabyte
 	}
 
 	private static String getName() {
 		String name = System.getProperty("org.oobium.logging.file.name");
 		if(name != null) {
 			return name;
 		} else {
 			return "oobium";
 		}
 	}
 
 	private boolean inited;
 	private String name;
 	private long max;
 	private long count;
 	private FileWriter writer;
 	private int fileCount;
 
 	public LogHandler() {
 		this.inited = false;
 		if(LoggerImpl.getSystemFileLevel() != LoggerImpl.NEVER) {
 			this.name = getName();
 			this.max = getMax();
 			this.count = 0;
 			this.fileCount = 0;
 			init();
 		}
 	}
 
 	private synchronized void init() {
 		try {
 			File path = new File("logs");
 			if(!path.exists() || !path.isDirectory()) {
 				path.mkdirs();
 			}
 			File file = new File("logs", name + ".log");
 			if(file.exists()) {
 				count = file.length();
 			}
 			writer = new FileWriter(file, true);
 			String[] fnames = new File("logs").list(new FilenameFilter() {
 				public boolean accept(File dir, String fname) {
 					return fname.startsWith(name + "_") && fname.endsWith(".log");
 				}
 			});
 			for(String fname : fnames) {
 				String s = fname.substring(name.length() + 1, fname.length() - 4);
 				try {
 					fileCount = max(fileCount, Integer.parseInt(s));
 				} catch(NumberFormatException e) {
 					// discard
 				}
 			}
 			inited = true;
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public synchronized void logged(LogEntry log) {
 		Bundle bundle = log.getBundle();
 		int level = log.getLevel();
 		boolean isLoggingToConsole = LoggerImpl.isLoggingToConsole(bundle, level);
 		boolean isLoggingToFile = inited && LoggerImpl.isLoggingToFile(bundle, level);
 		if(isLoggingToConsole || isLoggingToFile) {
 			String message = format(bundle.getSymbolicName(), log.getLevel(), log.getMessage(), log.getException());
 			if(isLoggingToConsole) {
 				if(decode(level) <= WARNING) {
 					System.err.print(message);
 				} else {
 					System.out.print(message);
 				}
 			}
 			if(isLoggingToFile) {
 				logToFile(message);
 			}
 		}
 	}
 
 	private void logToFile(String message) {
 		if((count += message.length()) > max) {
 			rotate();
 		}
 		try {
 			writer.write(message);
 			writer.flush();
 		} catch(IOException e1) {
 			File file = new File("logs", name + ".log");
 			if(file.exists()) {
 				count = file.length();
 			}
 			try {
 				writer = new FileWriter(file, true);
 				writer.write(message);
 				writer.flush();
 			} catch(IOException e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 	
 	private void rotate() {
 		try {
 			writer.flush();
 			writer.close();
 			count = 0;
 			fileCount++;
 			File src = new File("logs", name + ".log");
 			File dst = new File("logs", name + "_" + nf.format(fileCount) + ".log");
 			if(!src.renameTo(dst)) {
 				System.out.println("!!! could not rename to " + dst);
 			}
 			writer = new FileWriter(src);
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
