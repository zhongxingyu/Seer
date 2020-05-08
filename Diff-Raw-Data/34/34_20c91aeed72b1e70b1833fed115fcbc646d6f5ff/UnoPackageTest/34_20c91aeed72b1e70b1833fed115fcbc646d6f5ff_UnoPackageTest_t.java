 /*************************************************************************
  * UnoPackageTest.java
  *
  * The Contents of this file are made available subject to the terms of
  * either of the GNU Lesser General Public License Version 2.1
  * 
  * GNU Lesser General Public License Version 2.1
  * =============================================
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1, as published by the Free Software Foundation.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
  * MA 02111-1307 USA
  *
  * Contributor(s): oliver.boehm@agentes.de
  ************************************************************************/
 
 package org.openoffice.plugin.core.model;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.zip.*;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.*;
 
 /**
  * This is the first JUnit test class for the ooo-plugin-package-core project.
  * 
  * @author oliver (oliver.boehm@agentes.de)
  * @since 0.0.1 (16.08.2010)
  */
 public final class UnoPackageTest {
 	
 	private static final Log log = LogFactory.getLog(UnoPackage.class);
 	private static final String[] filenames = { "README", "hello.properties",
 			"CVS/Root", "de/helau.properties", "de/CVS/Root" };
 	private static File tmpDir;
 	private File tmpFile;
 	private UnoPackage pkg;
 	
 	/**
 	 * Here we create a tmpdir with some files for testing.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@BeforeClass
 	public static void setUpTmpDir() throws IOException {
 		tmpDir = new File(System.getProperty("java.io.tmpdir", "/tmp"), "oxttest" + System.currentTimeMillis());
 		assertTrue("can't create " + tmpDir, tmpDir.mkdir());
 		assertTrue(tmpDir + " is not a directory", tmpDir.isDirectory());
 		for (int i = 0; i < filenames.length; i++) {
 			FileUtils.writeStringToFile(new File(tmpDir, filenames[i]), filenames[i] + " created at " + new Date());
 		}
 		log.info(tmpDir + " with " + filenames.length + " files created");
 	}
 
 	/**
 	 * Creates a tmp file for testing.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Before
 	public void setUp() throws IOException {
 		tmpFile = File.createTempFile("test", ".oxt");
 		log.info("using " + tmpFile + " for testing...");
 		pkg = new UnoPackage(tmpFile);
 	}
 	
 	/**
 	 * Here we delete the tmp file after testing.
 	 */
 	@After
 	public void tearDown() {
 		tmpFile.delete();
 		log.info(tmpFile + " is deleted.");
 	}
 	
 	/**
 	 * Here we delete the directory created for testing.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@AfterClass
 	public static void tearDownTmpDir() throws IOException {
 		FileUtils.deleteDirectory(tmpDir);
 		log.info(tmpDir + " is deleted.");
 	}
 
 	/**
 	 * Test method for {@link UnoPackage#UnoPackage(java.io.File)}.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testUnoPackage() throws IOException {
 		pkg.close();
 		assertTrue(tmpFile.isFile());
 	}
 	
 	/**
 	 * Here we create just two testfiles and check if this will be part of the
 	 * created oxt file.
 	 *
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testAddContent() throws IOException {
 		pkg.addContent(tmpDir);
 		List<File> files = pkg.getContainedFiles();
 		assertEquals(filenames.length, files.size());
 		pkg.close();
 		checkUnoPackage();
 	}
 	
 	/**
 	 * Here we exclude all CVS files and check it if they are really excluded.
 	 *
 	 * @throws ZipException the zip exception
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@Test
 	public void testAddDirectory() throws ZipException, IOException {
 		String[] includes = {};
		String[] excludes = { "CVS", "**/CVS", "README" };
 		pkg.addDirectory(tmpDir, includes, excludes);
 		List<File> files = pkg.getContainedFiles();
 		assertTrue("no files included", files.size() > 0);
 		for (File file : files) {
			String dirname = file.getParentFile().getName();
			String filename = file.getName();
			assertFalse(file + " should be excluded!", dirname.equals("CVS")
					|| filename.equals("README"));
 			log.info("contained file: " + file);
 		}
 		pkg.close();
 		checkUnoPackage();
 	}
 	
 	private void checkUnoPackage() throws ZipException, IOException {
         ZipFile zip = new ZipFile(tmpFile);
         Enumeration<? extends ZipEntry> entries = zip.entries();
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = entries.nextElement();
 			log.debug("entry: " + entry);
 			String entryName = entry.getName();
 			if (!entryName.startsWith("META-INF")) {
 				assertTrue(entry + " has wrong path",
 						ArrayUtils.contains(filenames, entryName));
 			}
 		}
 	}
 
 }
