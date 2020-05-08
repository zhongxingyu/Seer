 package info.freelibrary.bagit;
 
 import static org.junit.Assert.*;
 
 import info.freelibrary.util.I18nObject;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.BeforeClass;
 import org.junit.AfterClass;
 import org.junit.Test;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BagValidatorTest extends I18nObject implements BagConstants {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(BagTest.class);
 
 	private static final String BAGS_DIR = "src/test/resources/bags/";
 
 	private static final String BAGS_TEST_DIR = "src/test/resources/bagTests";
 
 	private static BagValidator VALIDATOR;
 
 	@BeforeClass
 	public static void oneTimeSetUp() throws Exception {
 		System.setProperty(BAGIT_WORK_DIR_PROPERTY, BAGS_TEST_DIR);
 		VALIDATOR = new BagValidator();
 	}
 	
 	@AfterClass
 	public static void onTimeTearDown() throws Exception {
		new File(BAGS_TEST_DIR).delete(); // cleaning up
 	}
 
 	@Test
 	public void testCheckPayloadDataFiles() {
 		Bag bag = null;
 		
 		try {
 			bag = new Bag(BAGS_DIR + "dryad_632");
 			VALIDATOR.validate(bag);
 		}
 		catch (IOException details) {
 			fail(details.getMessage());
 		}
 		catch (BagException details) {
 			if (!(details.getReason() == BagException.PAYLOAD_MANIFEST_DIFFERS_FROM_DATADIR)) {
 				fail(getI18n("bagit.test.failed", details.getMessage()));
 			}
 			else {
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug(getI18n("bagit.test.expected", details.getMessage()));
 				}
 			}
 		}
 		finally {
 			bag.cleanUp();
 		}
 	}
 	
 	@Test
 	public void testCheckPayloadDataFileCount() {
 		Bag bag = null;
 
 		try {
 			bag = new Bag(BAGS_DIR + "dryad_631");
 			VALIDATOR.validate(bag);
 			fail(getI18n("bagit.test.file_mismatch"));
 		}
 		catch (IOException details) {
 			if (LOGGER.isErrorEnabled()) {
 				LOGGER.error(details.getMessage(), details);
 			}
 
 			fail(details.getMessage());
 		}
 		catch (BagException details) {
 			if (!(details.getReason() == BagException.PAYLOAD_MANIFEST_DIFFERS_FROM_DATADIR)) {
 				fail(getI18n("bagit.test.failed", details.getMessage()));
 			}
 			else {
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug(getI18n("bagit.test.expected", details.getMessage()));
 				}
 			}
 		}
 		finally {
 			if (null != null) {
 				bag.cleanUp();
 			}
 		}
 	}
 
 	@Test
 	public void testIsComplete() {
 	// fail("Not yet implemented");
 	}
 
 	@Test
 	public void testIsValid() {
 	// fail("Not yet implemented");
 	}
 
 	@Test
 	public void testValidate() {
 	// fail("Not yet implemented");
 	}
 
 }
