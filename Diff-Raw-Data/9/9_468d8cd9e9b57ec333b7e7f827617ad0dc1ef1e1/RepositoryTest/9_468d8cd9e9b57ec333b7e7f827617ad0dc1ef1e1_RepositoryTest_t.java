 package darep.repos;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import darep.Command;
 import darep.Command.ActionType;
import darep.DarepController;
 import darep.parser.ParseException;
 import darep.parser.Parser;
 
 public class RepositoryTest {
 	File testDir;
 	File testRepo;
 	File testDataSet;
 	Command command;
 	Repository repo;
 
 	@Before
 	public void setUp() throws Exception {
 		testDir = new File("jUnitDatabaseTestDir");
 		testDir.mkdir();
 		testDataSet = new File(testDir.getAbsolutePath() + "/testDataSet");
 		testDataSet.createNewFile();
 		testRepo = new File(testDir.getAbsolutePath() + "/testRepo");
 		repo = new Repository(testRepo.getAbsolutePath());
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		deleteFile(testDir);
 	}
 
 	@Test
 	public void testAdd() {
 		command = getCommand("add " + testDataSet.getAbsolutePath());
 		File newDataset = new File(testRepo + "/datasets/TESTDATASET");
 		File newMetadata = new File(testRepo + "/metadata/TESTDATASET");
 		assertEquals(false, newDataset.exists());
 		assertEquals(false, newMetadata.exists());
 		try {
 			repo.add(command);
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		assertEquals(true, newDataset.exists());
 		assertEquals(true, newMetadata.exists());
 	}
 
 	@Test(expected = RepositoryException.class)
 	public void testAddNonexistingFile() throws RepositoryException {
 		command = getCommand("add NONEXISTINGFILE");
 		repo.add(command);
 		fail("nonexistant input file. no exeption occured");
 	}
 
 	@Test(expected = RepositoryException.class)
 	public void testAddNonUniqueName() throws RepositoryException {
 		command = getCommand("add -n NAME " + testDataSet.getAbsolutePath());
 		repo.add(command);
 		command = getCommand("add -n NAME " + testDataSet.getAbsolutePath());
 		repo.add(command);
 		fail("added two files with same -n parameter. missing exception");
 	}
 
 	@Test
 	public void testAddMultiple() throws RepositoryException {
 		File newDataset = new File(testRepo + "/datasets/TESTDATASET");
 		File newMetadata = new File(testRepo + "/metadata/TESTDATASET");
 		command = getCommand("add " + testDataSet.getAbsolutePath());
 		repo.add(command);
 		assertEquals(true, newDataset.exists());
 		assertEquals(true, newMetadata.exists());
 		for (int i = 1; i < 4; i++) {
 
 			repo.add(command);
 
 			for (int j = 1; j <= i; j++) {
 				newDataset = new File(testRepo + "/datasets/TESTDATASET" + j);
 				newMetadata = new File(testRepo + "/metadata/TESTDATASET" + j);
 				assertEquals(true, newDataset.exists());
 				assertEquals(true, newMetadata.exists());
 			}
 		}
 	}
 
 	private Command getCommand(String args) {
 		Parser parser = new Parser(DarepController.syntax,
 				DarepController.getConstraints(), ActionType.help);
 		;
 		try {
 			command = parser.parse(args.split(" "));
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return command;
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
