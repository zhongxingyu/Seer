 package com.google.code.maven_replacer_plugin.file;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 
 public class FileUtilsTest {
 	private static final String CONTENT = "content";
 
 	@Rule
 	public TemporaryFolder folder = new TemporaryFolder();
 
 	private FileUtils fileUtils;
 
 	@Before
 	public void setUp() {
 		fileUtils = new FileUtils();
 	}
 
 	@Test
 	public void shouldDetermineIfFileExists() throws Exception {
 		File file = folder.newFile("tempfile");
 		assertTrue(fileUtils.fileNotExists("non existant"));
 		assertTrue(fileUtils.fileNotExists(null));
 		assertTrue(fileUtils.fileNotExists(""));
 		assertFalse(fileUtils.fileNotExists(file.getAbsolutePath()));
 	}
 
 	@Test
 	public void shouldEnsureFileFolderExists() throws Exception {
 		String tempFile = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "/tempfile";
 		fileUtils.ensureFolderStructureExists(tempFile);
 		new File(tempFile).createNewFile();
 		assertTrue(new File(tempFile).exists());
 	}
 	
 	@Test
 	public void shouldNotDoAnythingIfRootDirectory() {
 		fileUtils.ensureFolderStructureExists("/");
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void shouldThrowIllegalArgumentExceptionIfFileIsDirectory() throws Exception {
 		String tempFile = System.getProperty("java.io.tmpdir");
 		fileUtils.ensureFolderStructureExists(tempFile);
 		new File(tempFile).createNewFile();
 		assertTrue(new File(tempFile).exists());
 	}
 	
 	@Test
 	public void shouldWriteToFileEnsuringFolderStructureExists() throws Exception {
 		String tempFile = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "/tempfile";
 		fileUtils.writeToFile(tempFile, CONTENT);
 		
 		assertEquals(CONTENT, org.apache.commons.io.FileUtils.readFileToString(new File(tempFile)));
 	}
 	
 	@Test
 	public void shouldReturnFileText() throws Exception {
 		File file = folder.newFile("tempfile");
 		FileWriter writer = new FileWriter(file);
 		writer.write("test\n123\\t456");
 		writer.close();
 
 		String data = fileUtils.readFile(file.getAbsolutePath());
 		assertEquals("test\n123\\t456", data);
 	}
 	
 	@Test
 	public void shouldReturnFilenameWhenJustFilenameParam() {
 		String result = fileUtils.createFullPath("tempFile");
 		assertEquals("tempFile", result);
 	}
 	
 	@Test
 	public void shouldBuildFullPathFromDirsAndFilename() {
 		String result = fileUtils.createFullPath("1", "2", "3", "tempFile");
 		assertEquals("1" + File.separator + "2" + File.separator + "3" + File.separator + "tempFile", result);
 	}
 	
 	@Test
 	public void shouldThrowExceptionWhenCannotCreateDir() {
 		try {
			fileUtils.ensureFolderStructureExists("/f%e$d/a%*bc$:\\test");
 			fail("Should have thrown Error");
 		} catch (Error e) {
 			assertEquals(e.getMessage(), "Error creating directory.");
 		}
 	}
 }
