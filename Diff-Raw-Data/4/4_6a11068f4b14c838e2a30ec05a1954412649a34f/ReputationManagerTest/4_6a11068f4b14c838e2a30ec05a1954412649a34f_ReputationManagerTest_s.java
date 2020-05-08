 package com.mymed.tests.unit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import org.junit.Test;
 
 import com.mymed.controller.core.manager.reputation.ReputationManager;
 import com.mymed.model.data.reputation.MReputationBean;
 
 /**
  * Test class for the {@link ReputationManager}
  * 
  * @author Milo Casagrande
  */
 public class ReputationManagerTest extends GeneralTest {
 
   private static final double FEEDBACK = 2;
 
   @Test
   public void testCreateReputation() {
     try {
       reputationManager.create(reputationBean, APPLICATION_ID + PRODUCER_ID);
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
       final MReputationBean readReputation = reputationManager.read(PRODUCER_ID, CONSUMER_ID, APPLICATION_ID);
       assertEquals("The reputation beans are not the same\n", reputationBean, readReputation);
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 
   /**
    * Perform an update operation
    */
   @Test
   public void testUpdateReputation() {
     try {
       final MReputationBean newReputationBean = reputationBean.clone();
       newReputationBean.setValue(FEEDBACK);
 
       reputationManager.update(newReputationBean, APPLICATION_ID + PRODUCER_ID);
       final MReputationBean readReputation = reputationManager.read(PRODUCER_ID, CONSUMER_ID, APPLICATION_ID);
      assertEquals("The reputation beans are not the same\n", reputationBean, readReputation);
     } catch (final Exception ex) {
       fail(ex.getMessage());
     }
   }
 }
