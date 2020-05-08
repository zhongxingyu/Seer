 package com.ironiacorp.io;
 /*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
  
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
  
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  
 Copyright (C) 2005 Marco Aurelio Graciotto Silva <magsilva@gmail.com>
  */
 
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.Ostermiller.util.RandPass;
 import com.ironiacorp.string.StringUtil;
 
 /**
  * Methods useful for file manipulations (what a shame Java doesn't have them in
  * its standard library).
  * 
  * @author Marco Aurélio Graciotto Silva
  */
 public final class IoUtil
 {
 	/**
 	 * We really don't want an instance of this class, so we create this private
 	 * constructor.
 	 */
 	private IoUtil()
 	{
 	}
 
 	public static int BUFFER_SIZE = 8192;
 
 	public static String getExtension(String filename)
 	{
 		int index = filename.lastIndexOf('.');
 
 		if (index == -1) {
 			return "";
 		}
 
 		return filename.substring(index + 1);
 	}
 
 	/**
 	 * Check if a file exists.
 	 * 
 	 * @param filename
 	 *            The filename or directory to be checked.
 	 * 
 	 * @return True if the file exists, false otherwise.
 	 */
 	public static boolean exists(String filename)
 	{
 		File file = new File(filename);
 		return file.exists();
 	}
 
 	/**
 	 * Create a directory (any missing parent directory is created too).
 	 * 
 	 * @param dir
 	 *            The directory to be created.
 	 */
 	public static File createDir(String dir)
 	{
 		File file = new File(dir);
 		file.mkdirs();
 		return file;
 	}
 
 	/**
 	 * Create a a file.
 	 * 
 	 * @param dirname
 	 *            The directory where the file must reside.
 	 * @param filename
 	 *            The file to be created.
 	 */
 	public static File createFile(String dirname, String filename) throws IOException
 	{
 		File dir = createDir(dirname);
 		return createFile(dir, filename);
 	}
 
 	/**
 	 * Create a a file.
 	 * 
 	 * @param dirname
 	 *            The directory where the file must reside.
 	 * @param filename
 	 *            The file to be created.
 	 */
 	public static File createFile(File dir, String filename) throws IOException
 	{
 		dir.mkdirs();
 		File file = new File(dir, filename);
 		file.createNewFile();
 		return file;
 	}
 
 	/**
 	 * Move a file.
 	 * 
 	 * @param src
 	 *            Source file.
 	 * @param dest
 	 *            Destination file.
 	 */
 	public static void moveFile(String src, String dest) throws IOException
 	{
 		File srcFile = new File(src);
 		File destFile = new File(dest);
 		moveFile(srcFile, destFile);
 	}
 
 	/**
 	 * Move a file.
 	 * 
 	 * @param src
 	 *            Source file.
 	 * @param dest
 	 *            Destination file.
 	 */
 	public static void moveFile(File src, File dest) throws IOException
 	{
 		boolean result = false;
 
 		result = src.renameTo(dest);
 		if (!result) {
 			copyFile(src.getAbsolutePath(), dest.getAbsolutePath());
 			src.delete();
 		}
 	}
 
 	/**
 	 * Sync a file stream to disk.
 	 * 
 	 * @param fileStream
 	 *            The file stream to be synchronized to disk.
 	 */
 	public static void syncFile(FileOutputStream fileStream)
 	{
 		try {
 			FileDescriptor fd = fileStream.getFD();
 			fileStream.flush();
 			// Block until the system buffers have been written to disk.
 			fd.sync();
 		} catch (IOException e) {
 		}
 	}
 
 	/**
 	 * Copy the source file to the destination file.
 	 * 
 	 * @param srcFilename
 	 *            The source filename.
 	 * @param destFilename
 	 *            The destination filename.
 	 */
 	public static void copyFile(File srcFile, File destFile) throws IOException
 	{
 		copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
 	}
 	
 	/**
 	 * Copy the source file to the destination file.
 	 * 
 	 * @param srcFilename
 	 *            The source filename.
 	 * @param destFilename
 	 *            The destination filename.
 	 */
 	public static void copyFile(String srcFilename, String destFilename) throws IOException
 	{
 		if (!exists(srcFilename)) {
 			throw new IOException();
 		}
 
 		FileInputStream srcFileStream = new FileInputStream(srcFilename);
 		FileOutputStream destFileStream = new FileOutputStream(destFilename);
 		byte[] buffer = new byte[BUFFER_SIZE];
 		int position = 0;
 		int bytes;
 
 		do {
 			bytes = srcFileStream.read(buffer, position, buffer.length);
 			if (bytes != -1) {
 				destFileStream.write(buffer, position, bytes);
 				position += bytes;
 			}
 		} while (bytes != -1);
 
 		srcFileStream.close();
 		destFileStream.close();
 	}
 
 	/**
 	 * Copy the files in the source directory to the destination directory.
 	 * 
 	 * @param srcDir
 	 *            The source directory.
 	 * @param destDir
 	 *            The destination directory.
 	 */
 	public static void copyDir(String srcDirName, String destDirName) throws IOException
 	{
 		copyDir(srcDirName, destDirName, false);
 	}
 
 	/**
 	 * Copy the files in the source directory to the destination directory.
 	 * 
 	 * @param srcDir
 	 *            The source directory.
 	 * @param destDir
 	 *            The destination directory.
 	 * @param recurse
 	 *            Enable the recursive copy of directories.
 	 */
 	public static void copyDir(String srcDirName, String destDirName, boolean recurse) throws IOException
 	{
 		File srcDir = new File(srcDirName);
 		File destDir = new File(destDirName);
 
 		copyDir(srcDir, destDir, recurse);
 	}
 
 	/**
 	 * Copy the files in the source directory to the destination directory.
 	 * 
 	 * @param srcDir
 	 *            The source directory.
 	 * @param destDir
 	 *            The destination directory.
 	 * @param recurse
 	 *            Enable the recursive copy of directories.
 	 */
 	public static void copyDir(File srcDir, File destDir, boolean recurse) throws IOException
 	{
 		if (! srcDir.isDirectory()) {
 			throw new IllegalArgumentException("Source directory isn't a directory");
 		}
 		
 		if (destDir.exists()) {
 			if (! destDir.isDirectory()) {
 				throw new IllegalArgumentException("Destination isn't a directory");
 			}
 		} else {
 			boolean result = destDir.mkdirs();
 			if (result == false) {
 				throw new IllegalArgumentException("Destination directory could not be created");
 			}
 		}
 		
 		File[] files = srcDir.listFiles();
 		for (File file : files) {
 			if (file.isDirectory()) {
 				copyDir(file, new File(destDir.getAbsolutePath() + File.separator + file.getName()), recurse);
 			} else {
 				copyFile(file, new File(destDir.getAbsolutePath() + File.separator + file.getName()));
 			}
 		}
 	}
 
 	/**
 	 * Get all the files within the directory.
 	 */
 	public static ArrayList<File> find(String path)
 	{
 		ArrayList<File> files = new ArrayList<File>();
 		File dir = new File(path);
 		for (File file : dir.listFiles()) {
 			if (file.isFile()) {
 				files.add(file);
 			} else if (file.isDirectory()) {
 				files.addAll(find(file.getAbsolutePath()));
 			}
 		}
 		return files;
 	}
 
 	/**
 	 * Remove a file or a directory.
 	 */
 	public static void remove(String path)
 	{
 		File file = new File(path);
 		if (file.isDirectory()) {
 			removeDir(path);
 		} else if (file.isFile()) {
 			removeFile(path);
 		}
 	}
 
 	/**
 	 * Remove a file.
 	 * 
 	 * @param filename
 	 *            The file to be removed
 	 */
 	public static void removeFile(String filename)
 	{
 		File file = new File(filename);
 		if (file.isFile()) {
 			file.delete();
 		}
 	}
 
 	/**
 	 * Remove a directory and all of it's content.
 	 * 
 	 * @param dirname
 	 *            The directory to be removed
 	 */
 	public static void removeDir(String dirname)
 	{
 		File dir = new File(dirname);
 		if (dir.isDirectory()) {
 			File[] listing = dir.listFiles();
 			for (File file : listing) {
 				if (file.isDirectory()) {
 					removeDir(file.getAbsolutePath());
 				}
 				file.delete();
 			}
 			dir.delete();
 		}
 	}
 
 	public static File createTempDir() throws IOException
 	{
 		String randomPrefix = new RandPass(RandPass.LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET).getPass(8);
 		return IoUtil.createTempDir(randomPrefix);
 	}
 
 	/**
 	 * Create a temporary directory.
 	 * 
 	 * @param prefix
 	 *            Prefix for the directory to be created.
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
 	public static File createTempDir(String prefix)
 	{
 		return IoUtil.createTempDir(prefix, "");
 	}
 
 	/**
 	 * Create a temporary directory.
 	 * 
 	 * @param prefix
 	 *            Prefix for the directory to be created.
 	 * @param suffix
 	 *            Sufix for the directory to be created.
 	 * 
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
 	public static File createTempDir(String prefix, String suffix)
 	{
 		return IoUtil.createTempDir(prefix, suffix, null);
 	}
 
 	/**
 	 * Create a temporary directory.
 	 * 
 	 * @param prefix
 	 *            Prefix for the directory to be created.
 	 * @param suffix
 	 *            Sufix for the directory to be created.
 	 * @param directory
 	 *            Directory where the temporary directory should be created
 	 *            into.
 	 * 
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
 	public static File createTempDir(String prefix, String suffix, String baseDirName)
 	{
 		final int MAX_ATTEMPTS = 50;
 
 		if (baseDirName == null) {
 			baseDirName = System.getProperty("java.io.tmpdir");
 		}
 		if (StringUtil.isEmpty(baseDirName)) {
 			throw new RuntimeException("Could not create a temporary directory.");
 		}
 		if (!baseDirName.endsWith(File.separator)) {
 			baseDirName += File.separator;
 		}
 		File baseDir = new File(baseDirName);
 
 		for (int i = 0; i < MAX_ATTEMPTS; i++) {
 			try {
 				File file = File.createTempFile(prefix, suffix, baseDir);
 				String name = file.getAbsolutePath();
 				file.delete();
 				file = new File(name);
 				file.mkdirs();
 				return file;
 			} catch (IOException e) {
 			}
 		}
 
 		// throw new RuntimeException("Could not create a temporary
 		// directory.");
 		return null;
 	}
 
 	/**
 	 * Create a temporary file.
 	 * 
 	 * @param prefix
 	 *            Prefix for the directory to be created.
 	 * @param suffix
 	 *            Sufix for the directory to be created.
 	 * 
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
 	public static File createTempFile(String filePrefix, String fileSuffix)
 	{
 		final int MAX_ATTEMPTS = 50;
 
 		for (int i = 0; i < MAX_ATTEMPTS; i++) {
 			try {
 				File tempFile = File.createTempFile(filePrefix, fileSuffix);
 				return tempFile;
 			} catch (IOException e) {
 			}
 		}
 
 		// throw new RuntimeException("Could not create a temporary
 		// directory.");
 		return null;
 	}
 
 	/**
 	 * Create a temporary file.
 	 * 
 	 * @param prefix
 	 *            Prefix for the directory to be created.
 	 * @param suffix
 	 *            Sufix for the directory to be created.
 	 * 
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
	public static File createTempFile(String filePrefix, String fileSuffix, String dirPrefix)
 	{
 		final int MAX_ATTEMPTS = 50;
 
		File baseDir = IoUtil.createTempDir("tmp", "", dirPrefix);
 
 		for (int i = 0; i < MAX_ATTEMPTS; i++) {
 			try {
 				File tempFile = File.createTempFile(filePrefix, fileSuffix, baseDir);
 				return tempFile;
 			} catch (IOException e) {
 			}
 		}
 
 		// throw new RuntimeException("Could not create a temporary
 		// directory.");
 		return null;
 	}
 
 	/**
 	 * Dump a file content to an array of bytes.
 	 * 
 	 * @param filename
 	 *            The name of the file to be dumped.
 	 */
 	public static byte[] dumpFile(String filename) throws IOException
 	{
 		return dumpFile(new File(filename));
 	}
 
 	/**
 	 * Dump a file content to an array of bytes.
 	 * 
 	 * @param file
 	 *            The file to be dumped.
 	 */
 	public static byte[] dumpFile(File file) throws IOException
 	{
 		FileInputStream stream = new FileInputStream(file);
 		byte[] data = new byte[(int) file.length()];
 		stream.read(data, 0, (int) file.length());
 		stream.close();
 		return data;
 	}
 
 	public static boolean compare(File f1, File f2)
 	{
 		if (f1.length() != f2.length()) {
 			return false;
 		}
 
 		FileInputStream f1Stream;
 		FileInputStream f2Stream;
 		try {
 			f1Stream = new FileInputStream(f1);
 			f2Stream = new FileInputStream(f2);
 		} catch (FileNotFoundException e) {
 			throw new IllegalArgumentException("Files do not exist");
 		}
 
 		byte[] buffer1 = new byte[BUFFER_SIZE];
 		byte[] buffer2 = new byte[BUFFER_SIZE];
 		int bytesRead1 = 0;
 		int bytesRead2 = 0;
 		boolean result = true;
 
 		do {
 			try {
 				bytesRead1 = f1Stream.read(buffer1, 0, buffer1.length);
 				bytesRead2 = f2Stream.read(buffer2, 0, buffer2.length);
 			} catch (IOException e) {
 				throw new IllegalArgumentException("Error reading from files");
 			}
 
 			if (bytesRead1 != bytesRead2) {
 				result = false;
 				break;
 			}
 			if (!Arrays.equals(buffer1, buffer2)) {
 				result = false;
 				break;
 			}
 		} while (bytesRead1 != -1 && bytesRead2 != -1);
 
 		try {
 			f1Stream.close();
 			f2Stream.close();
 		} catch (IOException e) {
 		}
 
 		return result;
 	}
 
 	// TODO: implement a Java version of fdupes
 	public static void removeDuplicates(File dir1)
 	{
 	}
 
 	public static boolean isAbsoluteFilename(String filename)
 	{
 		if (filename.substring(0, 1).equals(File.separator)) {
 			return true;
 		}
 
 		if (System.getProperty("os.name").startsWith("Windows")) {
 			if (filename.substring(0, 3).matches("[a-zA-Z]:" + File.separator)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 	
 	public static String dumpFileAsString(String filename) throws IOException
 	{
 		StringBuilder sb = new StringBuilder();
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 		char[] buf = new char[IoUtil.BUFFER_SIZE];
 		int numRead = 0;
 		while ((numRead = in.read(buf)) != -1) {
 			sb.append(buf, 0, numRead);
 		}
 		in.close();
 		return sb.toString();
 	}
 }
