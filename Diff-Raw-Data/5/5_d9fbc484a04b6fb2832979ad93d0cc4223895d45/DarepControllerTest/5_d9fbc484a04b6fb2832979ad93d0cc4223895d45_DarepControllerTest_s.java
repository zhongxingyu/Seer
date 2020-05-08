 package darep;
 
import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import darep.logger.Logger;
 import darep.storage.nullStorage.NullStorage;
 
 public class DarepControllerTest {
 	
 	private DarepController controller;
 	private Logger logger;
 	private NullStorage storage;
 	
 	@Before
 	public void setUp() {
 		controller = new DarepController();
 		logger = controller.getLogger();
 		storage = new NullStorage();
 		controller.setStorage(storage);
 	}
 
 	@Test
 	public void testPrintHelp() throws DarepException, IOException {
 		InputStream is = new FileInputStream(new File("./resources/help.txt"));
 		String expected = Helper.streamToString(is).trim();
 		
 		controller.processCommand(makeArgs("help"));
		assertEquals(logger.getContent().trim(), expected);
 	}
 	
 	@Test
 	public void testProcess() throws DarepException {
 		// add the test-folder
 		// TODO DarepControllerTest.testProcess() this actually seems more like a repository test
 		
 		controller.processCommand(makeArgs("add test"));
 		assertEquals(storage.getCalls("store"), 1);
 		controller.processCommand(makeArgs("add test"));
 		assertEquals(storage.getCalls("store"), 2);
 		
 		controller.processCommand(makeArgs("replace TEST test"));
 		assertEquals(storage.getCalls("store"), 3);
 		assertEquals(storage.getCalls("delete"), 1);
 	}
 	
 	private String[] makeArgs(String cmd) {
 		return cmd.split("\\s+");
 	}
 	
 }
