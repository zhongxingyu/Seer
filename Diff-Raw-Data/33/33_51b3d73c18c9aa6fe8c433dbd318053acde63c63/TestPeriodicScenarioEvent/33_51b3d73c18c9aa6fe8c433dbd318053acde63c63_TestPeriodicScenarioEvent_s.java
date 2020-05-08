 package es.upm.dit.gsi.shanks.notification.util.test;
 
 import sim.engine.Steppable;
 import es.upm.dit.gsi.shanks.model.event.scenario.PeriodicScenarioEvent;
 
 public class TestPeriodicScenarioEvent extends PeriodicScenarioEvent{
 
     public TestPeriodicScenarioEvent(Steppable generator) {
         super(TestDefinitions.EVENT_ID+TestPeriodicScenarioEvent.class, 
                 generator, TestDefinitions.EVENT_PERIOD);
     }
 
     @Override
     public void addPossibleAffected() {
        // Not necessary to run the test
         
     }
 
     @Override
     public void interactWithNE() {
         // Not necessary to run the test
         
     }
 
 }
