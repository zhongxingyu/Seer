 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  * 
  * This file is part of Universal Task Executor.
  * 
  * Universal Task Executor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * Universal Task Executor is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executor. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute.utils;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.lmxm.ute.beans.FileReference;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 
 /**
  * The Class FileSystemUtilsTest.
  */
 public class FileSystemUtilsTest {
 
 	/** The Constant FILE_SYSTEM_UTILS. */
 	private static final FileSystemUtils FILE_SYSTEM_UTILS = FileSystemUtils.getInstance();
 
 	/** The Constant STATUS_CHANGE_LISTENER. */
 	private static final TestStatusChangeListener STATUS_CHANGE_LISTENER = new TestStatusChangeListener();
 
 	/** The Constant TMP_DIR. */
 	private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
 
 	/**
 	 * Test convert to file objects.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testConvertToFileObjects() throws IOException {
 		// Create temp file that we can locate
 		final File tempFile1 = File.createTempFile("UTE", ".TESTFILE");
 		FileUtils.touch(tempFile1);
 		tempFile1.deleteOnExit();
 
 		final File tempFile2 = File.createTempFile("ute", ".testfile");
 		FileUtils.touch(tempFile2);
 		tempFile2.deleteOnExit();
 
 		// Run the test
 		final FileReference fileReference = new FileReference();
 		fileReference.setName("UTE*.TESTFILE");
 
 		final List<FileReference> fileReferences = new ArrayList<FileReference>();
 		fileReferences.add(fileReference);
 
 		final List<File> files = FileSystemUtils.convertToFileObjects(TMP_DIR, fileReferences);
 		assertNotNull(files);
 		assertTrue(files.size() == 1);
 
 		// Delete the temp file we created for this test
 		tempFile1.delete();
 		tempFile2.delete();
 	}
 
 	/**
 	 * Test convert to file objects blank path.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testConvertToFileObjectsBlankPath() {
 		FileSystemUtils.convertToFileObjects("    ", null);
 	}
 
 	/**
 	 * Test convert to file objects empty list.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testConvertToFileObjectsEmptyList() throws IOException {
 		final List<File> files = FileSystemUtils.convertToFileObjects(TMP_DIR, new ArrayList<FileReference>());
 		assertNotNull(files);
 		assertTrue(files.size() == 0);
 	}
 
 	/**
 	 * Test convert to file objects null list.
 	 */
 	@Test
 	public void testConvertToFileObjectsNullList() {
 		final List<File> files = FileSystemUtils.convertToFileObjects(TMP_DIR, null);
 		assertNotNull(files);
 		assertTrue(files.size() == 0);
 	}
 
 	/**
 	 * Test convert to file objects null path.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testConvertToFileObjectsNullPath() {
 		FileSystemUtils.convertToFileObjects(null, null);
 	}
 
 	/**
 	 * Test create directory.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testCreateDirectory() throws IOException {
 		final File directory = File.createTempFile("UTE", ".TEST");
 		directory.deleteOnExit();
 
 		assertTrue(directory.exists());
 
 		final File newDirectory1 = FILE_SYSTEM_UTILS.createDirectory(directory.getAbsolutePath());
 		assertNotNull(newDirectory1);
 		assertTrue(newDirectory1.exists());
 		assertEquals(directory, newDirectory1);
 
 		final File newDirectory2 = FILE_SYSTEM_UTILS.createDirectory(directory.getAbsolutePath());
 		assertNotNull(newDirectory2);
 		assertTrue(newDirectory2.exists());
 		assertEquals(newDirectory1, newDirectory2);
 
 		final File newDirectory3 = FILE_SYSTEM_UTILS.createDirectory(TMP_DIR + File.separator + "TESTFILE.TEST");
 		assertNotNull(newDirectory3);
 		assertTrue(newDirectory3.exists());
 		assertTrue(newDirectory3.delete());
 	}
 
 	/**
 	 * Test create directory blank path.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testCreateDirectoryBlankPath() {
 		FILE_SYSTEM_UTILS.createDirectory("    ");
 	}
 
 	/**
 	 * Test create directory null path.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testCreateDirectoryNullPath() {
 		FILE_SYSTEM_UTILS.createDirectory(null);
 	}
 
 	/**
 	 * Test delete files blank path.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testDeleteFilesBlankPath() {
 		FILE_SYSTEM_UTILS.deleteFiles("    ", null, STATUS_CHANGE_LISTENER);
 	}
 
 	/**
 	 * Test delete files directory contents.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testDeleteFilesDirectoryContents() throws IOException {
 		final File directory = new File(TMP_DIR, "TESTDIRECTORY");
 		directory.deleteOnExit();
 		directory.mkdir();
 
 		final File file = new File(directory, "UTE.TEST");
		file.deleteOnExit();
 		FileUtils.touch(file);
 
 		assertTrue(file.exists());
 
 		final FileReference fileReference = new FileReference();
 		fileReference.setName("UTE.TEST");
 
 		final List<FileReference> files = new ArrayList<FileReference>();
 		files.add(fileReference);
 
 		FILE_SYSTEM_UTILS.deleteFiles(directory.getAbsolutePath(), files, STATUS_CHANGE_LISTENER);
 
 		assertFalse(file.exists());
 	}
 
 	/**
 	 * Test delete files file does not exist.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	public void testDeleteFilesFileDoesNotExist() throws IOException {
 		final File file = new File(TMP_DIR, "TESTFILE.TEST");
		file.deleteOnExit();
 
 		assertFalse(file.exists());
 
 		FILE_SYSTEM_UTILS.deleteFiles(file.getAbsolutePath(), null, STATUS_CHANGE_LISTENER);
 
 		assertFalse(file.exists());
 	}
 
 	/**
 	 * Test delete files null change listener.
 	 */
 	@Test(expected = NullPointerException.class)
 	public void testDeleteFilesNullChangeListener() {
 		FILE_SYSTEM_UTILS.deleteFiles(TMP_DIR, null, null);
 	}
 
 	/**
 	 * Test delete files.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testDeleteFilesNullPath() {
 		FILE_SYSTEM_UTILS.deleteFiles(null, null, STATUS_CHANGE_LISTENER);
 	}
 
 	/**
 	 * Test delete files single directory.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testDeleteFilesSingleDirectory() throws IOException {
 		final File directory = new File(TMP_DIR, "TESTDIRECTORY");
		directory.deleteOnExit();
 		directory.mkdir();
 
 		assertTrue(directory.exists());
 
 		FILE_SYSTEM_UTILS.deleteFiles(directory.getAbsolutePath(), null, STATUS_CHANGE_LISTENER);
 
 		assertFalse(directory.exists());
 	}
 
 	/**
 	 * Test delete files single file.
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testDeleteFilesSingleFile() throws IOException {
 		final File file = File.createTempFile("UTE", ".TEST");
		file.deleteOnExit();
 		FileUtils.touch(file);
 
 		assertTrue(file.exists());
 
 		FILE_SYSTEM_UTILS.deleteFiles(file.getAbsolutePath(), null, STATUS_CHANGE_LISTENER);
 
 		assertFalse(file.exists());
 	}
 
 	/**
 	 * Test file exists.
 	 */
 	@Test
 	public void testFileExists() {
 		assertFalse(FILE_SYSTEM_UTILS.fileExists(null));
 		assertFalse(FILE_SYSTEM_UTILS.fileExists(""));
 		assertFalse(FILE_SYSTEM_UTILS.fileExists("    "));
 		assertFalse(FILE_SYSTEM_UTILS.fileExists("abcd"));
 		assertTrue(FILE_SYSTEM_UTILS.fileExists(TMP_DIR));
 	}
 }
