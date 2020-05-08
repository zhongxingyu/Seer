 package es.upm.dit.gsi.shanks.notification.util.test;
 
 import sim.engine.Steppable;
 import es.upm.dit.gsi.shanks.model.event.scenario.ProbabilisticScenarioEvent;
 
 public class TestProbabilisticScenarioEvent extends ProbabilisticScenarioEvent{
 
     public TestProbabilisticScenarioEvent(Steppable generator) {
         super(TestDefinitions.EVENT_ID+TestProbabilisticScenarioEvent.class, 
                 generator, TestDefinitions.EVENT_PROBABILITY);
     }
 
     @Override
     public void addPossibleAffected() {
        this.addPossibleAffectedStatus(TestScenario.class, TestScenario.TEST_STATE);
     }
 
     @Override
     public void interactWithNE() {
         // Not necessary to run the test.
     }
 }
