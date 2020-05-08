 package com.bigvisible.kanbansimulatortester.suites;
 
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
import com.bigvisible.kanbansimulator.IterationParameter;
import com.bigvisible.kanbansimulatortester.core.unit.IterationParameterTest;
 import com.bigvisible.kanbansimulatortester.core.unit.IterationResultTest;
 import com.bigvisible.kanbansimulatortester.core.unit.StimulatorTest;
 
 @RunWith(Suite.class)
 @SuiteClasses({ IterationResultTest.When_a_typical_iteration_is_run.class,
         IterationResultTest.When_the_next_iteration_is_generated.class,
         IterationResultTest.Iteration_Results_can_be_serialized_to_and_from_CSV.class,
        IterationResultTest.Uncategorized.class, StimulatorTest.class, IterationParameterTest.class })
 public class AllUnitTests {
 }
