 package com.chariot.lunchlearn.testingtalk.service;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class TrainingServiceUnitTest {
 
   private TrainingService trainingService;
 
   @Before
   public void setUp() {
     trainingService = new TrainingServiceImpl();
   }
 
   @Test
   public void equationTestSomething() {
    assertTrue(0 == 1);
   }
 
 
   @Test
   public void testSomething() {
     // actual, comparison value
    assertEquals(0, 1);
   }
 
   @Test
   public void matchSomething() {
     // actual, comparison matcher
    assertThat(0, equalTo(1) );
   }
 
   @After
   public void tearDown() {
     System.out.println("Cleaning up...");
     // redundant... we do setUp each time
     trainingService = null;
   }
 
 }
