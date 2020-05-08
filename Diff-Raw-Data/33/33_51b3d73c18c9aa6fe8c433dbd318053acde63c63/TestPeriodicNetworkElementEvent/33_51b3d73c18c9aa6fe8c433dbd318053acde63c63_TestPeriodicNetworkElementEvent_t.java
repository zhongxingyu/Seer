 package es.upm.dit.gsi.shanks.notification.util.test;
 
 import sim.engine.Steppable;
 import es.upm.dit.gsi.shanks.model.event.networkelement.PeriodicNetworkElementEvent;
 
 public class TestPeriodicNetworkElementEvent extends PeriodicNetworkElementEvent{
 
     public TestPeriodicNetworkElementEvent(Steppable generator) {
         super(TestDefinitions.EVENT_ID+TestPeriodicNetworkElementEvent.class,
                 generator, TestDefinitions.EVENT_PERIOD);
         // TODO Auto-generated constructor stub
     }
 
     @Override
     public void addPossibleAffected() {
        this.addPossibleAffectedStatus(TestDevice.class, null, false);
     }
 
     @Override
     public void interactWithNE() {
         // Not necessary to run the test
     }
 }
