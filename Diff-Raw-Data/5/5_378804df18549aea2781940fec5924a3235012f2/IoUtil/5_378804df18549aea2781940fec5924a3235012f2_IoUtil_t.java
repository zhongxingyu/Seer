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
 
 package com.ironiacorp.io;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.Ostermiller.util.RandPass;
 import com.ironiacorp.string.StringUtil;
 
 /**
  * Methods useful for file manipulations (what a shame Java doesn't have them in
  * its standard library).
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
 		if (filename == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 		
 		int index = filename.lastIndexOf('.');
 
 		if (index == -1) {
 			return "";
 		}
 
 		return filename.substring(index + 1);
 	}
 
 	/**
 	 * Create a directory (any missing parent directory is created too).
 	 * 
 	 * @param dir
 	 *            The directory to be created.
 	 */
 	public static File createDir(String dir)
 	{
 		if (dir == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 		if (dir.isEmpty()) {
 			throw new IllegalArgumentException("Cannot create a directory with no name");
 		}
 		
 		File file = new File(dir);
 		return createDir(file);
 	}
 
 
 	/**
 	 * Create a directory (any missing parent directory is created too).
 	 * 
 	 * @param dir
 	 *            The directory to be created.
 	 */
 	public static File createDir(File file)
 	{
 		if (file == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 
 		if (file.isDirectory()) {
 			return file;
 		}
 		boolean result = file.mkdirs();
 		if (result == false) {
 			throw new UnsupportedOperationException("Error creating directory");
 		}
 
 		
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
 		if (dir == null) {
 			throw new IllegalArgumentException("Invalid directory");
 		}
 		if (filename == null) {
 			throw new IllegalArgumentException("Invalid filename");
 		}
 
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
 		if (src == null || dest == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 		if (src.isEmpty() || dest.isEmpty()) {
 			throw new IllegalArgumentException();
 		}
 
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
 		if (src == null || dest == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 
 		if (src.equals(dest)) {
 			throw new IllegalArgumentException("Destination is the same file as the target");
 		}
 		
 		if (! src.exists()) {
 			throw new IllegalArgumentException("Source file does not exist");
 		}
 		
 		boolean result = src.renameTo(dest);
 		if (! result) {
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
 		if (fileStream == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 		try {
 			FileDescriptor fd = fileStream.getFD();
 			fileStream.flush();
 			// Block until the system buffers have been written to disk.
 			fd.sync();
 		} catch (IOException e) {
 			throw new UnsupportedOperationException(e);
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
 		if (! new File(srcFilename).exists()) {
 			throw new IOException();
 		}
 
 		FileInputStream srcFileStream = new FileInputStream(srcFilename);
 		FileOutputStream destFileStream = new FileOutputStream(destFilename);
 		byte[] buffer = new byte[BUFFER_SIZE];
 		int position = 0;
 		int bytes;
 
 		do {
			bytes = srcFileStream.read(buffer, 0, buffer.length);
 			if (bytes != -1) {
				destFileStream.write(buffer, 0, bytes);
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
 
 		if (prefix == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 		
 		if (baseDirName == null) {
 			baseDirName = IoUtil.getDefaultTempBasedir();
 		}
 		
 		if (StringUtil.isEmpty(baseDirName)) {
 			throw new RuntimeException("Could not create a temporary directory.");
 		}
 		if (! baseDirName.endsWith(File.separator)) {
 			baseDirName += File.separator;
 		}
 		File baseDir = new File(baseDirName);
 		if (! baseDir.exists()) {
 			throw new IllegalArgumentException("Invalid base dir");
 		}
 
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
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
 	public static File createTempFile()
 	{
 		String randomPrefix = new RandPass(RandPass.LOWERCASE_LETTERS_AND_NUMBERS_ALPHABET).getPass(8);
 		return IoUtil.createTempFile(randomPrefix);
 	}
 	
 	
 	/**
 	 * Create a temporary file.
 	 * 
 	 * @param prefix
 	 *            Prefix for the directory to be created.
 	 * 
 	 * @return Temporary directory.
 	 * @throws IOException
 	 */
 	public static File createTempFile(String filePrefix)
 	{
 		return IoUtil.createTempFile(filePrefix, null);
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
 		
 		if (filePrefix == null) {
 			throw new IllegalArgumentException(new NullPointerException());
 		}
 
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
 
 		if (dirPrefix == null) {
 			throw new IllegalArgumentException(new NullPointerException("Invalid base dir"));
 		}
 		
 		File baseDir = new File(dirPrefix);
 		if (! baseDir.exists()) {
 			throw new IllegalArgumentException("Invalid base dir");
 		}
 
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
 	
 	public static String getDefaultTempBasedir()
 	{
 		return System.getProperty("java.io.tmpdir");
 	}
 	
 	public static void copyStream(InputStream in, OutputStream out) throws IOException
 	{
 		int readBytes = 0;
 		byte[] buffer = new byte[IoUtil.BUFFER_SIZE];
 		while ((readBytes = in.read(buffer, 0, buffer.length)) != -1) {
 			out.write(buffer, 0, readBytes);
 		}
 	}
 	
 	public static byte[] toByteArray(InputStream is)
 	{
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		try {
 			copyStream(is, outputStream);
 		} catch (IOException e) {
 			return null;
 		}
 		return outputStream.toByteArray();
 	}
 	
 	public static File toFile(InputStream is)
 	{
 		File file = IoUtil.createTempFile("inputstream-", ".tmp");
 		return toFile(is, file);
 	}
 	
 	public static File toFile(InputStream is, File file)
 	{
 		FileOutputStream outputStream = null;
 		try {
 			outputStream = new FileOutputStream(file);
 			copyStream(is, outputStream);
 			outputStream.close();
 		} catch (IOException e) {
 			return null;
 		}
 		return file;
 	}
 }
