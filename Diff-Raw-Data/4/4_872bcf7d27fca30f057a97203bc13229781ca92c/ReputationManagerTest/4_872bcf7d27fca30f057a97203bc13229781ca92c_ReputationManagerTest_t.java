 package com.mymed.tests;
 
 import static com.mymed.tests.TestValues.APPLICATION_ID;
 import static com.mymed.tests.TestValues.CAL_INSTANCE;
 import static com.mymed.tests.TestValues.CONF_FILE;
 import static com.mymed.tests.TestValues.CONSUMER_ID;
 import static com.mymed.tests.TestValues.INTERACTION_ID;
 import static com.mymed.tests.TestValues.INTERACTION_LST_ID;
 import static com.mymed.tests.TestValues.PRODUCER_ID;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.reputation.ReputationManager;
 import com.mymed.controller.core.manager.storage.StorageManager;
 import com.mymed.model.core.configuration.WrapperConfiguration;
import com.mymed.model.data.reputation.MInteractionBean;
import com.mymed.model.data.reputation.MReputationBean;
 
 /**
  * Test class for the {@link ReputationManager}
  * 
  * @author Milo Casagrande
  * 
  */
 public class ReputationManagerTest {
 
 	private ReputationManager reputationManager;
 	private StorageManager storageManager;
 
 	private static MInteractionBean interactionBean;
 	private final double feedback = 2;
 
 	@BeforeClass
 	public static void setUpOnce() {
 		interactionBean = new MInteractionBean();
 
 		CAL_INSTANCE.set(2011, 1, 1);
 		interactionBean.setStart(CAL_INSTANCE.getTimeInMillis());
 
 		CAL_INSTANCE.set(2011, 1, 3);
 		interactionBean.setEnd(CAL_INSTANCE.getTimeInMillis());
 
 		interactionBean.setId(INTERACTION_ID);
 		interactionBean.setApplication(APPLICATION_ID);
 		interactionBean.setProducer(PRODUCER_ID);
 		interactionBean.setConsumer(CONSUMER_ID);
 		interactionBean.setSnooze(0);
 		interactionBean.setComplexInteraction(INTERACTION_LST_ID);
 	}
 
 	/**
 	 * Set up the {@link ReputationManager} connection
 	 * 
 	 * @throws InternalBackEndException
 	 */
 	@Before
 	public void setUp() throws InternalBackEndException {
 		storageManager = new StorageManager(new WrapperConfiguration(new File(CONF_FILE)));
 		reputationManager = new ReputationManager(storageManager);
 	}
 
 	/**
 	 * Perform an update operation
 	 */
 	@Test
 	public void testUpdateReputation() {
 		try {
 			reputationManager.update(interactionBean, feedback);
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Read the reputation back from the database
 	 */
 	@Test
 	public void testReadReputation() {
 		try {
 			final MReputationBean beanRead = reputationManager.read(PRODUCER_ID, CONSUMER_ID, APPLICATION_ID);
 			assertEquals("The reputation beans are not the same\n", feedback, beanRead.getValue(), 0);
 		} catch (final Exception ex) {
 			fail(ex.getMessage());
 		}
 	}
 }
