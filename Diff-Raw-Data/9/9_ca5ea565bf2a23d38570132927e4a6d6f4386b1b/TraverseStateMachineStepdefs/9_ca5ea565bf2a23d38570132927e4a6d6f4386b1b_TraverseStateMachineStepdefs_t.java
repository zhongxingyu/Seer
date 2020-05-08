 package org.suggs.test.sandbox.cucumber.springstatemachine;
 
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.suggs.test.sandbox.statemachine.*;
 import org.suggs.test.sandbox.statemachine.impl.StateTransitionEventImpl;
 
import javax.annotation.Resource;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.Map;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * This classes responsibility is:
  * 1.
  */
@Named
 public class TraverseStateMachineStepdefs {
     private static final Logger LOG = LoggerFactory.getLogger(TraverseStateMachineStepdefs.class);
 
     @Inject
     protected StateMachine stateMachine;
 
    @Resource(name = "stateMap")
     protected Map<String, State> stateMap;
 
     @Given("^an unused state machine$")
     public void an_unused_state_machine() throws Throwable {
         stateMachine.reset();
         LOG.debug("State map [" + stateMap.toString() + "]");
     }
 
     @When("^no events are received$")
     public void no_events_are_received() throws Throwable {
         LOG.debug("Not sending any events at the state manager");
     }
 
     @When("^a (.+) event is received$")
     public void a_event_is_received(String aEventType) throws StateMachineException {
         stateMachine.step(createStateMachineContext(aEventType));
     }
 
     @Then("^the state should be (.+)$")
     public void the_state_should_be_(String aState) throws Throwable {
         LOG.debug("Checking that state machine state is [" + aState + "]");
         assertThat(stateMachine.getCurrentState(), is(stateMap.get(aState)));
     }
 
     private StateMachineContext createStateMachineContext(final String aEventType) {
         return new StateMachineContext() {
 
             @Override
             public StateTransitionEvent getStateTransitionEvent() {
                 return new StateTransitionEventImpl(aEventType);
             }
         };
     }
 }
