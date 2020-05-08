 package darep.repos;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import darep.Command;
 import darep.Command.ActionType;
 import darep.DarepController;
 import darep.parser.ParseException;
 import darep.parser.Parser;
import darep.server.ServerException;
 
 public class RepositoryTest {
 	File testDir;
 	File testRepo;
 	File testDataSet;
 	File testDataSet2;
 	Command command;
 	Repository repo;
 
 	@Before
 	public void setUp() throws Exception {
 		testDir = new File("jUnitRepositoryTestDir");
 		testDir.mkdir();
 		testDataSet = new File(testDir, "testDataSet");
 		testDataSet.createNewFile();
 		testDataSet2 = new File(testDir, "testDataSet2");
 		testDataSet2.createNewFile();
 		testRepo = new File(testDir.getAbsolutePath(), "testRepo");
 		repo = new Repository(testRepo.getAbsolutePath());
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		deleteFile(testDir);
 	}
 
 	@Test
 	public void testAdd() {
 		command = getCommand("add " + testDataSet.getAbsolutePath());
 		File newDataset = new File(testRepo + "/data/TESTDATASET");
 		File newMetadata = new File(testRepo + "/metadata/TESTDATASET");
 		assertFalse(newDataset.exists());
 		assertFalse(newMetadata.exists());
 		try {
 			repo.add(command);
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		assertTrue(newDataset.exists());
 		assertTrue(newMetadata.exists());
 	}
 
 	@Test(expected = RepositoryException.class)
 	public void testAddNonexistingFile() throws RepositoryException {
 		command = getCommand("add NONEXISTINGFILE");
 		repo.add(command);
 		fail("nonexistant input file. no exception occured");
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
 		File newDataset = new File(testRepo + "/data/TESTDATASET");
 		File newMetadata = new File(testRepo + "/metadata/TESTDATASET");
 		command = getCommand("add " + testDataSet.getAbsolutePath());
 		repo.add(command);
 		assertTrue(newDataset.exists());
 		assertTrue(newMetadata.exists());
 		for (int i = 1; i < 4; i++) {
 
 			repo.add(command);
 
 			for (int j = 1; j <= i; j++) {
 				newDataset = new File(testRepo + "/data/TESTDATASET" + j);
 				newMetadata = new File(testRepo + "/metadata/TESTDATASET" + j);
 				assertTrue(newDataset.exists());
 				assertTrue(newMetadata.exists());
 			}
 		}
 	}
 	
 	@Test(expected = RepositoryException.class)
 	public void testRepositoryNotFoundAndCommandIsNotAdd()
			throws RepositoryException, ServerException {
 		DarepController controller = new DarepController();
 		String[] arg = { "export", "-r", "NONEXISTINGREPO",
 				testDataSet.getAbsolutePath(), "." };
 		try {
 			controller.processCommand(arg);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Test (expected = RepositoryException.class)
 	public void testExportTheRepositoryItself() throws RepositoryException {
 		command = getCommand("add " + testDataSet.getAbsolutePath());
 		try {
 			repo.add(command);
 		} catch (RepositoryException e) {
 			e.printStackTrace();
 		}
 		command = getCommand("export " + testDataSet.getName()+" "+repo.getLocation());
 		repo.export(command);
 	}
 	
 	@Test (expected = RepositoryException.class)
 	public void testAddRepositoryItself() throws RepositoryException {
 		//TODO "wrong" exception gets thrown, it just says: cannot copy file
 		command = getCommand("add " + repo.getLocation().getAbsolutePath());
 		repo.add(command);
 	}
 	
 	@Test (expected = RepositoryException.class)
 	public void testAddParentOfRepository() throws RepositoryException {
 		// Parent folder of Repo
 		command = getCommand("add " + repo.getLocation().getParentFile().getAbsolutePath());
 		
 		try {
 			repo.add(command);
 		} catch (RepositoryException e) {
 			// Error should come from the repository itself,
 			// not anything else (like storage)
 			assertNull(e.getCause());
 			throw e;
 		}
 		
 	}
 	
 	@Test
 	public void testReplace() throws RepositoryException {
 		String name = "TEH_NAME";
 		command = getCommand("add -n " + name +
 				" " + testDataSet.getAbsolutePath());
 		repo.add(command);
 		command = getCommand("replace " + name +
 				" " + testDataSet2.getAbsolutePath());
 		repo.replace(command);
 		assertNotNull(repo.getDataset(name));
 	}
 	
 	private Command getCommand(String args) {
 		Parser parser = new Parser(DarepController.syntax,
 				DarepController.getConstraints(), ActionType.help);
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
