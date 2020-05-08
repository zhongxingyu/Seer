 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.meerkat.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 public class FileUtil {
 
 	private static Logger log = Logger.getLogger(FileUtil.class);
 
 	public FileUtil() {
 
 	}
 
 	/**
 	 * removeFile
 	 * 
 	 * @param filename
 	 */
 	public final void removeFile(String filename) {
 		try {
 			FileUtils.forceDelete(new File(filename));
 		} catch (IOException e) {
			log.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * deleteDirectory
 	 * 
 	 * @param path
 	 * @return true if deleted
 	 */
 	public final boolean deleteDirectory(File path) {
 		if (path.exists()) {
 			File[] files = path.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				if (files[i].isDirectory()) {
 					deleteDirectory(files[i]);
 				} else {
 					if (!files[i].delete()) {
 						log.error("Deleting file: " + files[i]
 								+ " from directory " + path);
 					}
 				}
 			}
 		}
 		return (path.delete());
 	}
 
 	/**
 	 * readFileContents
 	 * 
 	 * @param filename
 	 * @return file contents
 	 */
 	public final String readFileContents(String filePath) {
 		FileInputStream stream = null;
 		String contents = "";
 		try {
 			stream = new FileInputStream(new File(filePath));
 		} catch (Exception e) {
 			log.error("File not found: " + filePath, e);
 		}
 		try {
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
 					fc.size());
 			/* Instead of using default, pass in a decoder. */
 			contents = Charset.defaultCharset().decode(bb).toString();
 		} catch (IOException e) {
 			log.error("Error streaming file contents: " + filePath, e);
 
 		} finally {
 			try {
 				stream.close();
 			} catch (IOException e) {
 				log.error("Error closing file stream: " + filePath, e);
 
 			}
 		}
 		return contents;
 	}
 
 	/**
 	 * writeToFile
 	 * @param filename
 	 * @param contents
 	 */
 	public final void writeToFile(String filename, String contents){
 		RandomAccessFile destFile = null;
 		File tmpFile;
 		try {
 			tmpFile = new File(filename);
 			if(tmpFile.exists()){
 				tmpFile.delete();
 			}
 			destFile = new RandomAccessFile (filename, "rw");
 		} catch (FileNotFoundException e) {
 			log.error("Error accessing file: "+filename+" ("+e.getMessage()+")");
 		}
 
 		ByteBuffer buf = ByteBuffer.allocate(contents.length());
 		FileChannel outChannel = destFile.getChannel();
 
 		buf.put(contents.getBytes());
 		buf.flip(); //buffer set for read
 
 		try {
 			outChannel.write(buf);
 			destFile.close();
 		} catch (IOException e) {
 			log.error("Error writing to file "+filename+" ("+e.getMessage()+")");
 		}
 
 	}
 
 	/**
 	 * getDirListing
 	 * 
 	 * @param dir
 	 * @return
 	 */
 	public final String[] getDirListing(String dir) {
 		File srcDir = new File(dir);
 		if (!srcDir.exists() || !srcDir.canRead() || !srcDir.isDirectory()) {
 			log.error("Can't access dir: " + dir + "!");
 			return null;
 		}
 
 		String[] children = srcDir.list();
 
 		return children;
 
 	}
 
 	/**
 	 * getFiletypeFromDir
 	 * 
 	 * @param dir
 	 * @param extension
 	 * @return
 	 */
 	public final String[] getFiletypeListFromDir(String dir, String extension) {
 		String directory = dir;
 		if (!dir.endsWith("/")) {
 			directory = dir + "/";
 		}
 
 		File tempFile;
 		String[] dirContents = getDirListing(directory);
 		String[] filteredContents = new String[dirContents.length];
 
 		int curr = 0;
 		for (int i = 0; i < dirContents.length; i++) {
 			tempFile = new File(dirContents[i]);
 			if (tempFile.getName().endsWith(extension)) {
 				filteredContents[curr] = directory + tempFile.getName();
 				curr++;
 			}
 		}
 
 		return filteredContents;
 	}
 
 	/**
 	 * 
 	 * @param fileFullPath
 	 * @param destDirFullPath
 	 */
 	public final void moveFileToDir(String fileFullPath, String destDirFullPath) {
 		File file = new File(fileFullPath);
 		File destDir = new File(destDirFullPath);
 
 		if (file.exists() && !file.renameTo(new File(destDir, file.getName()))) {
 			log.warn("Failed to move file: " + file + " to dir: " + destDir);
 		}
 
 	}
 
 	/**
 	 * touchFile
 	 * 
 	 * @param file
 	 */
 	public final void createEmptyFile(String file) {
 		String empty = "";
 		File f = new File(file);
 		if (f.exists() && f.canWrite()) {
 			f.delete();
 		}
 
 		FileOutputStream out;
 		try {
 			out = new FileOutputStream(file);
 			out.write(empty.getBytes(), 0, empty.getBytes().length);
 		} catch (Exception e) {
 			log.error("Failed to create file: " + file);
 		}
 
 	}
 
 	/**
 	 * createEmptyXMLConfigFile
 	 * @param file
 	 */
 	public final void createEmptyXMLConfigFile(String file) {
 		String contents = "<meerkat-monitor></meerkat-monitor>";
 		File f = new File(file);
 		if (f.exists() && f.canWrite()) {
 			f.delete();
 		}
 
 		FileOutputStream out;
 		try {
 			out = new FileOutputStream(file);
 			out.write(contents.getBytes(), 0, contents.getBytes().length);
 		} catch (Exception e) {
 			log.error("Failed to create file: " + file);
 		}
 
 	}
 
 
 }
