 package gov.usgs.cida.gdp.utilities;
 
 import java.io.FileNotFoundException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.LoggerFactory;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class FileHelperTest {
 
     private static String sampleDir;
     private static String testFilePath;
     private static final String testFile = "demo_HUCs";
     private static final String secondTestFile = "Yahara_River_HRUs_geo_WGS84";
     private String tempDir = "";
     private String seperator = "";
     private static org.slf4j.Logger log = LoggerFactory.getLogger(FileHelperTest.class);
 
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         log.debug("Started testing class");
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         log.debug("Ended testing class");
     }
 
     @Before
     public void setUp() throws Exception {
         this.tempDir = System.getProperty("java.io.tmpdir");
 
         if (!(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\"))) {
             this.tempDir = this.tempDir + System.getProperty("file.separator");
         }
 
         String systemTempDir = System.getProperty("java.io.tmpdir");
         this.seperator = java.io.File.separator;
         String currentTime = Long.toString((new Date()).getTime());
         this.tempDir = systemTempDir + this.seperator + currentTime;
         (new File(this.tempDir)).mkdir();
 
         // Copy example files
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         URL sampleFileLocation = cl.getResource("Sample_Files/");
         if (sampleFileLocation != null) {
             File sampleFiles = null;
             try {
                 sampleFiles = new File(sampleFileLocation.toURI());
             } catch (URISyntaxException e) {
                 assertTrue("Exception encountered: " + e.getMessage(), false);
             }
             FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
         } else {
             assertTrue("Sample files could not be loaded for test", false);
         }
 
         sampleDir = this.tempDir + this.seperator
                 + "Sample_Files" + this.seperator
                 + "Shapefiles" + this.seperator;
 
         testFilePath = sampleDir + testFile;
     }
 
     @After
     public void tearDown() {
         File delDir = new File(this.tempDir);
 
         try {
             FileUtils.deleteDirectory(delDir);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, "Failed to delete: " + delDir.getPath() + "  -- Remember to clean project or remove this file/dir.", ex);
         }
     }
 
     @Test
     public void testGetCanonicalPathname() {
         File file = new File(this.sampleDir + this.secondTestFile + ".prj");
         assertTrue(file.exists());
 
         String result = null;
         try {
             result = file.getParent();
         } catch (Exception ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertNotNull(result);
     }
 
     @Test
     public void testRenameFile() {
         try {
             File file = File.createTempFile("delete", "me");
             assertTrue(file.exists());
 
             boolean result = FileHelper.renameFile(file, "new.file");
             assertTrue(result);
            File newFile = new File(file.getParent() + File.separator + "new.file");
             assertTrue(newFile.exists());
             assertFalse(file.exists());
             newFile.delete();
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     @Test
     /**
      * If this fails, try cleaning/building the project and re-run
      */
     public void testWipeOldFilesWithNoOldFiles() {
         Collection<File> result = new ArrayList<File>();
         result = FileHelper.wipeOldFiles(new File(this.tempDir), 3600000l);
         assertTrue(result.isEmpty());
     }
 
     /**
      * If this fails, try cleaning/building the project and re-run
      */
     @Test
     public void testWipeOldFilesWithFakeDirectory() {
         Collection<File> result = new ArrayList<File>();
         result = FileHelper.wipeOldFiles(new File("/not/real"), 3600000l);
         assertTrue(result.isEmpty());
     }
 
     /**
      * If this fails, try cleaning/building the project and re-run
      */
     @Test
     public void testWipeOldFilesWithNullArgument() {
         Collection<File> result = new ArrayList<File>();
         result = FileHelper.wipeOldFiles(null, 3600000l);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testWipeOldFilesWithOldFiles() {
         Collection<File> result = new ArrayList<File>();
         result = FileHelper.wipeOldFiles(new File(this.tempDir), 1l);
         assertTrue(!result.isEmpty());
     }
 
     @Test
     public void testWipeOldFilesWithOldFilesWhileLockingDirectory() throws FileNotFoundException, IOException {
         Collection<File> result = new ArrayList<File>();
 
         File directory = new File(this.tempDir);
         String singleFile = this.tempDir
                 + System.getProperty("file.separator")
                 + "Sample_Files"
                 + System.getProperty("file.separator")
                 + "Shapefiles"
                 + System.getProperty("file.separator")
                 + "demo_HUCs.dbf";
 
         // Lock a file
         File file1 = new File(singleFile);
         file1.setReadOnly();
         result = FileHelper.wipeOldFiles(directory, 1l);
 
         // Test that the locked file doesnt show up in the deleted files list
         boolean containsFail = true;
         for (File file : result) {
             if (file.getPath().equals(singleFile)) {
                 containsFail = false;
             }
         }
         assertTrue(containsFail);
 
         //Unlock the files
         file1.setWritable(false);
 
         // really delete the directory
         FileHelper.wipeOldFiles(directory, 1l);
 
     }
 
     @Test
     public void testCreateDir() {
         boolean result = false;
         String testDir = System.getProperty("java.io.tmpdir")
                 + java.io.File.separator
                 + Long.toString((new Date()).getTime()) + 1;
         result = FileHelper.createDir(testDir);
         assertTrue(result);
         (new File(testDir)).delete();
     }
 
     @Test
     public void testDoesDirectoryOrFileExist() {
         boolean result = false;
         String fileToCheckFor = testFilePath + ".shx";
 
         String directoryToCheckFor = sampleDir;
 
         result = FileHelper.doesDirectoryOrFileExist(fileToCheckFor);
         assertTrue(result);
         result = FileHelper.doesDirectoryOrFileExist(directoryToCheckFor);
         assertTrue(result);
         result = FileHelper.doesDirectoryOrFileExist("does/not/exist");
         assertFalse(result);
     }
 
     @Test
     public void testCopyFileToFileWithoutDeletingOriginal() {
         File fileToCopy = new File(testFilePath + ".shx");
 
         String fileToCopyTo = testFilePath + ".COPY";
 
         boolean result = false;
         try {
             result = FileHelper.copyFileToFile(fileToCopy, fileToCopyTo);
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertTrue(result);
 
         try {
             result = FileHelper.copyFileToFile(new File("doesnt/exist"), "doesnt/exist");
         } catch (IOException e) {
             assertNotNull(e);
             result = false;
         }
         assertFalse(result);
     }
 
     @Test
     public void testCopyFileToFileWithDeletingOriginal() {
         File fileToCopy = new File(testFilePath + ".shx");
 
         String fileToCopyTo = testFilePath + ".COPY";
 
         boolean result = false;
         try {
             result = FileHelper.copyFileToFile(fileToCopy, fileToCopyTo, true);
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertTrue(result);
 
         try {
             result = FileHelper.copyFileToFile(new File("doesnt/exist"), "doesnt/exist");
         } catch (IOException e) {
             assertNotNull(e);
             result = false;
         }
         assertFalse(result);
     }
 
     @Test
     public void testDeleteFileQuietly() {
         String fileToLoad = testFilePath + ".shx";
 
         boolean result = FileHelper.deleteFileQuietly("File/That/Doesnt/Exist");
         assertFalse(result);
         result = FileHelper.deleteFileQuietly(fileToLoad);
         assertTrue(result);
     }
 
     @Test
     public void testDeleteFile() {
         String fileToLoad = testFilePath + ".shx";
 
         boolean result = false;
         try {
             FileHelper.deleteFile("File/That/Doesnt/Exist");
         } catch (SecurityException e) {
             fail(e.getMessage());
         }
         assertFalse(result);
         result = FileHelper.deleteFile(fileToLoad);
         assertTrue(result);
     }
 
     @Test
     public void testDeleteDirRecursively() {
         File lockedFile = new File(testFilePath + ".shx");
         lockedFile.setWritable(false);
 
         String dirToDelete = this.tempDir
                 + this.seperator;
         boolean result = false;
         try {
             result = FileHelper.deleteDirRecursively(new File(dirToDelete));
             assertTrue(result);
         } catch (IOException e) {
             fail(e.getMessage());
         }
 
 
         try {
             result = FileHelper.deleteDirRecursively("Directory/That/Doesnt/Exist");
             assertFalse(result);
         } catch (IOException e) {
             fail(e.getMessage());
         }
 
         try {
             result = FileHelper.deleteDirRecursively(new File("Directory/That/Doesnt/Exist"));
             assertFalse(result);
         } catch (IOException e) {
             fail(e.getMessage());
         }
 
         try {
             result = FileHelper.deleteDirRecursively(lockedFile);
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertFalse(result);
         lockedFile.setWritable(true);
         FileHelper.deleteFileQuietly(lockedFile);
     }
 
     @Test
     public void testDeleteDirRecursivelyUsingString() {
         String dirToDelete = this.tempDir
                 + this.seperator;
         boolean result = false;
         try {
             result = FileHelper.deleteDirRecursively(dirToDelete);
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertTrue(result);
         try {
             result = FileHelper.deleteDirRecursively("Directory/That/Doesnt/Exist");
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertFalse(result);
     }
 
     @Test
     public void testFileHelper() {
         FileHelper result = new FileHelper();
         assertNotNull(result);
     }
 
     @Test
     public void testFindFile() {
         String fileToLoad = testFile + ".shx";
         String rootDir = this.tempDir + this.seperator;
         File result = FileHelper.findFile(fileToLoad, rootDir);
         assertNotNull("FineFile did not find the file " + fileToLoad + " within " + rootDir, result);
         assertEquals("File loaded does not have the same name as the file suggested", fileToLoad, result.getName());
         result = FileHelper.findFile("should.not.work", rootDir);
         assertNull(result);
     }
 
     @Test
     public void testGetFileList() {
         String dirToList = this.tempDir + this.seperator;
         List<String> result = null;
         result = FileHelper.getFileList(null, true);
         assertNull(result);
         result = FileHelper.getFileList(dirToList, true);
         assertNotNull("File listing came back null", result);
         assertFalse("There were no files listed", result.isEmpty());
         String fakeDirToList = this.tempDir + this.seperator + "9387509352" + this.seperator;
         try {
             result = FileHelper.getFileList(fakeDirToList, true);
         } catch (IllegalArgumentException e) {
             assertNotNull(e);
         }
 
     }
 
     @Test
     public void testGetSeparator() {
         String result = FileHelper.getSeparator();
         assertNotNull(result);
         assertFalse("".equals(result));
         log.debug("System separator: " + result);
     }
 
     @Test
     public void testGetSystemPathSeparator() {
         String result = FileHelper.getSystemPathSeparator();
         assertNotNull(result);
         assertFalse("".equals(result));
         log.debug("System path separator: " + result);
     }
 
     @Test
     public void testGetSystemTemp() {
         String result = FileHelper.getSystemTemp();
         assertNotNull(result);
         assertFalse("".equals(result));
         log.debug("System temp path: " + result);
     }
 
     @Test
     public void testLoadFile() {
         String fileToLoad = testFilePath + ".shx";
 
         File result = FileHelper.loadFile(fileToLoad);
         assertNotNull("File came back null", result);
         assertTrue("File is not a file", result.isFile());
     }
 
     @Test
     public void testGetFileCollection() {
         String dirToList = this.tempDir + this.seperator;
         Collection<File> result = null;
 
         String nullString = null;
         result = FileHelper.getFileCollection(nullString, true);
         assertNull(result);
         result = FileHelper.getFileCollection(dirToList, true);
         assertNotNull("File listing came back null", result);
         assertFalse("There were no files listed", result.isEmpty());
         String fakeDirToList = this.tempDir + this.seperator + "9387509352";
         try {
             result = FileHelper.getFileCollection(fakeDirToList, true);
         } catch (IllegalArgumentException e) {
             assertNotNull(e);
         }
     }
 
     @Test
     public void testCreateRepoDir() {
         File directoryCreatedAt = FileHelper.createFileRepositoryDirectory(this.tempDir);
         assertTrue(directoryCreatedAt.exists());
     }
 
     @Test
     public void testCreateRepoDirWithNullBasefilePath() {
         File directoryCreatedAt = FileHelper.createFileRepositoryDirectory(null);
         assertTrue(directoryCreatedAt.exists());
     }
 
     @Test
     public void testCreateRepoDirWithEmptyBasefilePath() {
         File directoryCreatedAt = FileHelper.createFileRepositoryDirectory("");
         assertTrue(directoryCreatedAt.exists());
     }
 
     @Test
     public void testCreateRepoDirWithinvalidBasefilePath() {
         File directoryCreatedAt = FileHelper.createFileRepositoryDirectory(this.seperator + "invalid" + this.seperator);
         assertNull(directoryCreatedAt);
     }
 
     @Test
     public void testCreateDirWithExistingDir() {
         String existingDirectory = this.seperator + this.tempDir;
         boolean result = FileHelper.createDir(existingDirectory);
         assertTrue(result);
     }
 
     @Test
     public void testGetOutputFileTypesAvailable() {
         List<String> result = FileHelper.getOutputFileTypesAvailable();
         assertFalse(result.isEmpty());
     }
 
     @Test
     public void testDeleteFileWithEmptyFilePath() {
         boolean result = FileHelper.deleteFile("");
         assertFalse(result);
     }
 
     @Test
     public void testDeleteFileWithNullArgument() {
         File nullFile = null;
         boolean result = FileHelper.deleteFile(nullFile);
         assertFalse(result);
     }
 
     @Test
     public void testUnzipFile() {
         File zipFile = new File(FileHelperTest.sampleDir + "test_zip.zip");
         boolean result = false;
         try {
             result = FileHelper.unzipFile(this.tempDir, zipFile);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
             fail(ex.getMessage());
         }
         assertTrue(result);
     }
 
     @Test
     public void testCreateUserDirectory() {
         String createdDir = FileHelper.createUserDirectory(this.tempDir + this.seperator);
 //        assertTrue(createdDir.contains(dirToCreate));
         assertFalse(createdDir.isEmpty());
     }
 
     @Test
     public void testCreateUserDirectoryWithInvalidDirectoryName() {
         String dirToCreate = this.seperator + "nonexistent";
         String createdDir = FileHelper.createUserDirectory(dirToCreate);
         assertTrue(createdDir.equals(""));
     }
 
     @Test
     public void testUpdateTimeStampWithNullPath() {
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(null, true);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertFalse(result);
     }
 
     @Test
     public void testUpdateTimeStampWithEmptyStringPath() {
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp("", true);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertFalse(result);
     }
 
     @Test
     public void testUpdateTimeStampOnSingleFile() {
         File file1 = new File(FileHelperTest.testFilePath + ".dbf");
         long file1Date = file1.lastModified();
 
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(FileHelperTest.testFilePath + ".dbf", false);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertTrue(result);
         File file2 = new File(FileHelperTest.testFilePath + ".dbf");
         long file2Date = file2.lastModified();
 
         assertTrue(file2Date > file1Date);
     }
 
     @Test
     public void testUpdateTimeStampOnSingleNonexistentFile() {
         File file1 = new File(FileHelperTest.testFilePath);
         long file1Date = file1.lastModified();
 
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(FileHelperTest.testFilePath, false);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertFalse(result);
     }
 
     @Test
     public void testUpdateTimeStampOnSingleDirectoryRecursive() {
         File file1 = new File(FileHelperTest.sampleDir);
         long file1Date = file1.lastModified();
 
         boolean result = true;
         try {
             result = FileHelper.updateTimestamp(FileHelperTest.sampleDir, true);
         } catch (IOException ex) {
             Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         assertTrue(result);
     }
 
     @Test
     public void testGetFilesOlderThanWithNullFilePath() {
         Collection<File> result = FileHelper.getFilesOlderThan(null, Long.MIN_VALUE, Boolean.TRUE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testGetFilesOlderThanWithNonExistantFilePath() {
         Collection<File> result = FileHelper.getFilesOlderThan(new File("derp"), Long.MIN_VALUE, Boolean.TRUE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testGetFilesOlderThanWithRealFilePathAndRecursiveWithMaxAgeValue() {
         // We should find no files older than the maximum long value
         Collection<File> result = FileHelper.getFilesOlderThan(new File(this.tempDir), Long.MAX_VALUE, Boolean.TRUE);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testGetFilesOlderThanWithRealFilePathAndNonRecursiveWithMaxAgeValue() {
         // We should find no files older than the maximum long value
         Collection<File> result = FileHelper.getFilesOlderThan(new File(this.tempDir), Long.MAX_VALUE, Boolean.FALSE);
         assertTrue(result.isEmpty());
     }
 }
