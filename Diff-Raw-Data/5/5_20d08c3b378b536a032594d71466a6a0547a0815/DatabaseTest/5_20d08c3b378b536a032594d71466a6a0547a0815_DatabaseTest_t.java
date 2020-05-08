 package darep.repos;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class DatabaseTest {
 	Database db;
 	File testDir;
 	File testRepo;
 	File testDataSet;
 	Metadata meta;
 
 	@Before
 	public void setUp() throws Exception {
 		testDir = new File("jUnitDatabaseTestDir");
 		testDir.mkdir();
		testDataSet = new File(testDir.getAbsolutePath(), "/testDataSet");
 		testDataSet.createNewFile();
		testRepo = new File(testDir.getAbsolutePath(), "/testRepo");
 		testRepo.mkdir();
 		meta = new Metadata("TESTDATASET", "testDataSet", "", 0, 0,
 				testRepo.getAbsolutePath());
 		db = new Database(testRepo.getAbsolutePath());
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		deleteFile(testDir);
 	}
 
 	@Test
 	public void testAddCopyFile() {
 		File expectedDataset = new File(testRepo + "/datasets/TESTDATASET");
 		File expectedMeta = new File(testRepo + "/metadata/TESTDATASET");
 
 		try {
 			db.add(testDataSet, meta, true);
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		assertEquals(true, expectedDataset.exists());
 		assertEquals(true, expectedMeta.exists());
 		assertEquals(true, testDataSet.exists());
 	}
 	
 	@Test
 	public void testAddCopyFolder() throws IOException {
 		File expectedDataset = new File(testRepo + "/datasets/TESTDATADIR");
 		File expectedMeta = new File(testRepo + "/metadata/TESTDATADIR");
 		File sampleFolder = createSampleFolder();
 		meta = new Metadata("TESTDATADIR", sampleFolder.getName(), "", 0, 0,
 				testRepo.getAbsolutePath());
 		try {
 			db.add(sampleFolder, meta, true);
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		assertEquals(true, expectedDataset.exists());
 		assertEquals(true, expectedMeta.exists());
 		assertEquals(true, testDataSet.exists());
 		checkFolderContents(expectedDataset);
 	}
 	
 	private void checkFolderContents(File dirToCheck) {
 		File fileToCheck;
 		for (int i=0;i<4;i++) {
 			dirToCheck=new File(dirToCheck.getAbsolutePath()+"/"+i);
 			assertEquals(true, dirToCheck.exists());
 			for (int j=0;j<4;j++) {
 				fileToCheck=new File(dirToCheck.getAbsolutePath()+"/file"+j);
 				assertEquals(true, fileToCheck.exists());
 			}
 		}
 	}
 
 	private File createSampleFolder() throws IOException {
 		File sampleFolder=new File(testDir.getAbsolutePath()+"/testDataDir");
 		sampleFolder.mkdir();
 		File sampleFile;
 		for (int i=0;i<4;i++) {
 			sampleFolder=new File(sampleFolder.getAbsolutePath()+"/"+i);
 			sampleFolder.mkdir();
 			for (int j=0;j<4;j++) {
 				sampleFile=new File(sampleFolder.getAbsolutePath()+"/file"+j);
 				sampleFile.createNewFile();
 			}
 		}
 		return new File(testDir.getAbsolutePath()+"/testDataDir");
 	}
 
 	@Test
 	public void testAddMove() {
 		File expectedDataset = new File(testRepo + "/datasets/TESTDATASET");
 		File expectedMeta = new File(testRepo + "/metadata/TESTDATASET");
 
 		try {
 			db.add(testDataSet, meta, false);
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		assertEquals(true, expectedDataset.exists());
 		assertEquals(true, expectedMeta.exists());
 		assertEquals(false, testDataSet.exists());
 	}
 
 	@Test
 	public void testAddMultiple() {
 		File expectedDataset = null;
 		File expectedMeta = null;
 		for (int i = 0; i < 4; i++) {
 			meta.setName("TESTDATASET" + i);
 			try {
 				db.add(testDataSet, meta, true);
 			} catch (RepositoryException e) {
 				e.printStackTrace();
 			}
 			for (int j = 0; j <= i; j++) {
 				expectedDataset = new File(testRepo + "/datasets/TESTDATASET" + j);
 				expectedMeta = new File(testRepo + "/metadata/TESTDATASET" + j);
 				assertEquals(true, expectedDataset.exists());
 				assertEquals(true, expectedMeta.exists());
 			}
 		}
 	}
 
 	@Test
 	public void testDelete() {
 		File existingDataset = new File(testRepo + "/datasets/TESTDATASET");
 		File existingMetadata = new File(testRepo + "/metadata/TESTDATASET");
 		try {
 			existingDataset.createNewFile();
 			existingMetadata.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		assertEquals(true, existingDataset.exists());
 		assertEquals(true, existingMetadata.exists());
 		try {
 			db.delete("TESTDATASET");
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		assertEquals(false, existingDataset.exists());
 		assertEquals(false, existingMetadata.exists());
 	}
 
 	private void deleteFile(File file) {
 		if (file.isDirectory()) {
 			File[] content = file.listFiles();
 			for (int i = 0; i < content.length; i++) {
 				deleteFile(content[i]);
 			}
 		}
 		file.delete();
 	}
 }
