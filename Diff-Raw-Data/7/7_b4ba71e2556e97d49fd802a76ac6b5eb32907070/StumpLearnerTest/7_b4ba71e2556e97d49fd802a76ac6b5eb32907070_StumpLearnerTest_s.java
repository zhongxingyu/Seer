 package aima.test.core.unit.learning.learners;
 
 import aima.core.learning.framework.DataSet;
 import aima.core.learning.learners.StumpLearner;
 import aima.test.core.unit.learning.framework.DataSetTest;
 import java.io.IOException;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * StumpLearnerTest
  *
  * @author Andrew Brown
  */
 public class StumpLearnerTest {
 
     DataSet restaurantData;
     StumpLearner[] learners;
 
     /**
      * Constructor
      */
     public StumpLearnerTest() {
         // get restaurant data
         this.restaurantData = null;
         try {
             this.restaurantData = DataSetTest.loadRestaurantData();
         } catch (IOException e) {
             Assert.fail("Could not load restaurant data from URL.");
         }
         // create stumps
         this.learners = new StumpLearner[5];
         for (int i = 0; i < 5; i++) {
             this.learners[i] = new StumpLearner();
             this.learners[i].train(this.restaurantData);
         }
     }
 
     /**
      * Ensure stumps are at least weak learning; i.e. their prediction accuracy
      * is better than guessing
      */
     @Test
     public void testStumpsForAccuracy() {
         // test
         for (int i = 0; i < 5; i++) {
             int[] results = this.learners[i].test(restaurantData);
             double proportionCorrect = (double) results[0] / restaurantData.size();
            Assert.assertTrue(proportionCorrect > 0.5);
         }
     }
 }
