 package com.bigvisible.kanbansimulator;
 
 import static org.junit.Assert.assertEquals;
 
 import java.security.SecureRandom;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class IterationResultTest {
     static final String[] WORKFLOW_STEP_NAMES = new String[] { "BA", "Dev", "WebDev", "QA" };
     static final String FIRST_WORKFLOW_STEP = WORKFLOW_STEP_NAMES[0];
     static final String LAST_WORKFLOW_STEP = WORKFLOW_STEP_NAMES[WORKFLOW_STEP_NAMES.length - 1];
 
     private static void ForEachWorkflowStep(DoTheFollowing loopBody) {
         for (int idx = 0; idx < WORKFLOW_STEP_NAMES.length; idx++) {
             String WORKFLOW_STEP_NAME = WORKFLOW_STEP_NAMES[idx];
             loopBody.run(WORKFLOW_STEP_NAME);
         }
     }
     
     private interface DoTheFollowing {
         void run(String workflowStepName);
     }
     
     private static SecureRandom random = new SecureRandom();
 
     /**
      * Where any number will do, within reason.
      * 
     * @return an integer between 10 and 30
      */
     private static int anyReasonableNumber() {
         /*
          * big enough that some values can be subtracted without resulting in a negative number, but small enough that
          * if it's used for looping values, we don't get carried away.
          */
         return random.nextInt(20) + 10;
     }
 
     public static class When_a_typical_iteration_is_run {
         private IterationResult iterationResult;
         private int batchSize;
 
         @Before
         public void given_a_typical_iteration() {
             iterationResult = new IterationResult();
             batchSize = anyReasonableNumber();
 
             iterationResult.setPutIntoPlay(batchSize);
         }
 
         @Test
         public void given_the_capacity_of_a_workflow_step_matches_batch_size__then_all_stories_are_completed_and_none_are_queued_up() {
             iterationResult.setCapacity(FIRST_WORKFLOW_STEP, batchSize);
 
             iterationResult.run();
 
             assertEquals(batchSize, iterationResult.getCompleted(FIRST_WORKFLOW_STEP));
             assertEquals(0, iterationResult.getQueued(FIRST_WORKFLOW_STEP));
         }
 
         @Test
         public void given_the_capacity_of_a_workflow_step_is_less_than_batch_size__then_some_stories_are_queued_up()
                 throws Exception {
             int howMuchLessCapacityIsThanBatchSize = 5;
             int capacityOfBA = batchSize - howMuchLessCapacityIsThanBatchSize;
 
             iterationResult.setCapacity(FIRST_WORKFLOW_STEP, capacityOfBA);
 
             iterationResult.run();
 
             assertEquals(capacityOfBA, iterationResult.getCompleted(FIRST_WORKFLOW_STEP));
             assertEquals(howMuchLessCapacityIsThanBatchSize, iterationResult.getQueued(FIRST_WORKFLOW_STEP));
         }
 
         @Test
         public void given_the_capacity_for_all_workflow_steps_is_greater_than_the_batch_size__then_all_stories_are_completed_in_the_iteration()
                 throws Exception {
             ForEachWorkflowStep(new DoTheFollowing() {
                 public void run(String workflowStepName) {
                     iterationResult.setCapacity(workflowStepName, batchSize);
                 }
             });
             
             iterationResult.run();
 
             assertEquals(batchSize, iterationResult.getCompleted(LAST_WORKFLOW_STEP));
             assertEquals(0, iterationResult.getQueued(LAST_WORKFLOW_STEP));
             assertEquals(batchSize, iterationResult.getTotalCompleted());
         }
     }
 
     public static class When_the_next_iteration_is_generated {
         IterationResult iterationResult;
 
         @Before
         public void given() {
             iterationResult = new IterationResult();
         }
 
         @Test
         public void its_iteration_number_is_one_more_than_the_previous() throws Exception {
             int iterationNumber = anyReasonableNumber();
             iterationResult.setIterationNumber(iterationNumber);
             IterationResult nextIteration = iterationResult.nextIteration();
 
             assertEquals(iterationNumber + 1, nextIteration.getIterationNumber());
         }
 
         @Test
         public void its_capacity_settings_match_those_of_the_previous() throws Exception {
             ForEachWorkflowStep(new DoTheFollowing() {
                 public void run(String workflowStepName) {
                     iterationResult.setCapacity(workflowStepName, anyReasonableNumber());
                 }
             });
 
             iterationResult.run();
 
             final IterationResult nextIteration = iterationResult.nextIteration();
 
             ForEachWorkflowStep(new DoTheFollowing() {
                 public void run(String workflowStepName) {
                     assertEquals(iterationResult.getCapacity(workflowStepName),
                             nextIteration.getCapacity(workflowStepName));
                 }
             });
         }
     }
 
     public static class Uncategorized {
         IterationResult iterationResult;
 
         @Before
         public void given() {
             iterationResult = new IterationResult();
         }
 
         @Test
         public void when_an_iteration_completes_the_next_iteration_carries_over_queue_amounts() throws Exception {
             int batchSize = 20;
 
             // configured so that a story is queued in each workflow step
             iterationResult.setPutIntoPlay(batchSize);
             iterationResult.setCapacity("BA", batchSize - 1);
             iterationResult.setCapacity("Dev", batchSize - 2);
             iterationResult.setCapacity("WebDev", batchSize - 3);
             iterationResult.setCapacity("QA", batchSize - 4);
 
             iterationResult.run();
 
             IterationResult nextIteration = iterationResult.nextIteration();
 
             assertEquals(1, nextIteration.getQueued("BA"));
             assertEquals(1, nextIteration.getQueued("Dev"));
             assertEquals(1, nextIteration.getQueued("WebDev"));
             assertEquals(1, nextIteration.getQueued("QA"));
         }
 
         @Test
         public void when_an_iteration_completes_more_work_it_is_additive() throws Exception {
             IterationResult firstIteration = new IterationResult();
             firstIteration.setIterationNumber(1);
             firstIteration.setCapacity("BA", 1);
             firstIteration.setCapacity("Dev", 1);
             firstIteration.setCapacity("WebDev", 1);
             firstIteration.setCapacity("QA", 1);
             firstIteration.setPutIntoPlay(1);
 
             firstIteration.run();
 
             IterationResult secondIteration = firstIteration.nextIteration();
 
             secondIteration.setPutIntoPlay(1);
             secondIteration.run();
 
             assertEquals(firstIteration.getTotalCompleted() + 1, secondIteration.getTotalCompleted());
         }
 
         @Test
         public void when_work_remains_from_previous_iteration_and_there_is_not_enough_capacity_the_queue_accumulates_the_work_not_started()
                 throws Exception {
             IterationResult iteration = new IterationResult();
             iteration.setIterationNumber(1);
             iteration.setCapacity("BA", 1);
             iteration.setQueued("BA", 1);
             iteration.setPutIntoPlay(2);
 
             iteration.run();
 
             assertEquals(2, iteration.getQueued("BA"));
         }
     }
 
     public static class Iteration_Results_can_be_serialized_to_and_from_CSV {
         @Test
         public void can_serialize_to_CSV() throws Exception {
             IterationResult iteration = new IterationResult();
             iteration.setIterationNumber(0);
             iteration.setPutIntoPlay(1);
             iteration.setCapacity("BA", 2);
             iteration.setCompleted("BA", 3);
             iteration.setQueued("BA", 4);
             iteration.setCapacity("Dev", 5);
             iteration.setCompleted("Dev", 6);
             iteration.setQueued("Dev", 7);
             iteration.setCapacity("WebDev", 8);
             iteration.setCompleted("WebDev", 9);
             iteration.setQueued("WebDev", 10);
             iteration.setCapacity("QA", 11);
             iteration.setCompleted("QA", 12);
             iteration.setQueued("QA", 13);
 
             String asCSV = iteration.toCSVString();
 
             assertEquals("0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0", asCSV);
         }
 
         @Test
         public void can_deserialize_from_CSV() throws Exception {
             String asCSV = "0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14";
 
             IterationResult iteration = IterationResult.parseCSV(asCSV);
 
             assertEquals(0, iteration.getIterationNumber());
             assertEquals(1, iteration.getPutIntoPlay());
             assertEquals(2, iteration.getCapacity("BA"));
             assertEquals(3, iteration.getCompleted("BA"));
             assertEquals(4, iteration.getQueued("BA"));
             assertEquals(5, iteration.getCapacity("Dev"));
             assertEquals(6, iteration.getCompleted("Dev"));
             assertEquals(7, iteration.getQueued("Dev"));
             assertEquals(8, iteration.getCapacity("WebDev"));
             assertEquals(9, iteration.getCompleted("WebDev"));
             assertEquals(10, iteration.getQueued("WebDev"));
             assertEquals(11, iteration.getCapacity("QA"));
             assertEquals(12, iteration.getCompleted("QA"));
             assertEquals(13, iteration.getQueued("QA"));
             assertEquals(14, iteration.getTotalCompleted());
         }
     }
 }
