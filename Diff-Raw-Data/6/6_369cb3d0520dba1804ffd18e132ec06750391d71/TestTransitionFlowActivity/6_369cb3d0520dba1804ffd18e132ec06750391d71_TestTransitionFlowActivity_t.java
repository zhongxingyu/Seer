 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.flow;
 
 import java.util.Map;
 
 import org.amplafi.flow.impl.*;
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.FlowState;
 import org.amplafi.flow.TransitionType;
 import static org.testng.Assert.*;
 import org.testng.annotations.Test;
 /**
  *
  */
 public class TestTransitionFlowActivity {
 
     private static final boolean TEST_ENABLED=true;
     @Test(enabled=TEST_ENABLED)
     public void testDup() {
    	 FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         TransitionFlowActivity obj = new TransitionFlowActivity();
         flowTestingUtils.addFlowDefinition("foo", obj);
         assertTrue(obj.getClass().isInstance(obj.dup()));
     }
 
 
     @Test(enabled=TEST_ENABLED)
     public void testTransitionFinishFlow() {
         String returnToFlowLookupKey = null;
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         TransitionFlowActivity obj = new TransitionFlowActivity();
         String flowTypeName = "foo";
         flowTestingUtils.addFlowDefinition(flowTypeName, obj);
         Map<String, String> initialFlowState = null;
         FlowState flowState = flowTestingUtils.getFlowManagement().startFlowState(flowTypeName, false, initialFlowState, returnToFlowLookupKey);
         assertTrue(flowState.isCompleted());
     }
 
     /**
      * Test to make sure that the alternate flow is correctly used.
      */
     @Test(enabled=TEST_ENABLED)
     public void testTransitionFlowActivityWithFlowTransitions() {
         String returnToFlowLookupKey = null;
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         String nextFlowType = flowTestingUtils.addFlowDefinition(newFlowActivity());
         TransitionFlowActivity transitionFlowActivity = new TransitionFlowActivity();
         transitionFlowActivity.setTransitionType(TransitionType.alternate);
         transitionFlowActivity.setNextFlowType(nextFlowType);
         String flowTypeName = flowTestingUtils.addFlowDefinition(newFlowActivity(), transitionFlowActivity);
         FlowManagement flowManagement = flowTestingUtils.getFlowManager().getFlowManagement();
         FlowStateImplementor flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
         flowTestingUtils.advanceToEnd(flowState);
         FlowState nextFlowState = flowManagement.getCurrentFlowState();
         // the alternate condition was not met.
         assertNull(nextFlowState);
 
         flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
         flowState.setFinishKey(TransitionType.alternate.toString());
         // make sure cache can't help 'cheat'
         flowState.clearCache();
         flowTestingUtils.advanceToEnd(flowState);
         nextFlowState = flowManagement.getCurrentFlowState();
         assertNotNull(nextFlowState);
         assertEquals(nextFlowState.getFlowTypeName(), nextFlowType);
         assertNull(nextFlowState.getFinishKey(), "nextFlowState="+nextFlowState);
     }
     /**
      * test a {@link TransitionFlowActivity} that transitions on a normal finish
      */
     @Test(enabled=TEST_ENABLED)
     public void testTransitionFlowActivityWithNormalFinish() {
         String returnToFlowLookupKey = null;
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         String nextFlowType = flowTestingUtils.addFlowDefinition(newFlowActivity());
         TransitionFlowActivity transitionFlowActivity = new TransitionFlowActivity();
         transitionFlowActivity.setNextFlowType(nextFlowType);
         String flowTypeName = flowTestingUtils.addFlowDefinition(newFlowActivity(), transitionFlowActivity);
         FlowManagement flowManagement = flowTestingUtils.getFlowManager().getFlowManagement();
         FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
         flowTestingUtils.advanceToEnd(flowState);
         FlowState nextFlowState = flowManagement.getCurrentFlowState();
         assertNotNull(nextFlowState);
         assertEquals(nextFlowState.getFlowTypeName(), nextFlowType);
 
     }
     /**
      * 2 alternate finishes + 1 normal finish
      */
     @Test(enabled=TEST_ENABLED)
     public void testTransitionFlowActivityWithMultipleFlowTransitions() {
         String returnToFlowLookupKey = null;
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         String nextFlowType0 = flowTestingUtils.addFlowDefinition(newFlowActivity());
         TransitionFlowActivity transitionFlowActivity0 = new TransitionFlowActivity();
         transitionFlowActivity0.setTransitionType(TransitionType.alternate);
         transitionFlowActivity0.setNextFlowType(nextFlowType0);
 
         String nextFlowType1 = flowTestingUtils.addFlowDefinition(newFlowActivity());
         TransitionFlowActivity transitionFlowActivity1 = new TransitionFlowActivity();
         transitionFlowActivity1.setTransitionType(TransitionType.alternate);
         transitionFlowActivity1.setFinishKey("foo1");
         transitionFlowActivity1.setNextFlowType(nextFlowType1);
 
         String nextFlowType2 = flowTestingUtils.addFlowDefinition(newFlowActivity());
         TransitionFlowActivity transitionFlowActivity2 = new TransitionFlowActivity();
         transitionFlowActivity2.setNextFlowType(nextFlowType2);
 
         String flowTypeName = flowTestingUtils.addFlowDefinition(newFlowActivity(), transitionFlowActivity0, transitionFlowActivity1, transitionFlowActivity2);
         FlowManagement flowManagement = flowTestingUtils.getFlowManager().getFlowManagement();
         FlowState flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
         flowTestingUtils.advanceToEnd(flowState);
         FlowState nextFlowState = flowManagement.getCurrentFlowState();
         // the alternate condition was not met.
         assertNotNull(nextFlowState);
         assertEquals(nextFlowState.getFlowTypeName(), nextFlowType2);
 
         flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
         flowState.setFinishKey(TransitionType.alternate.toString());
         flowTestingUtils.advanceToEnd(flowState);
         nextFlowState = flowManagement.getCurrentFlowState();
         assertNotNull(nextFlowState);
         assertEquals(nextFlowState.getFlowTypeName(), nextFlowType0);
 
         flowState = flowManagement.startFlowState(flowTypeName, true, null, returnToFlowLookupKey);
         flowState.setFinishKey("foo1");
         flowTestingUtils.advanceToEnd(flowState);
         nextFlowState = flowManagement.getCurrentFlowState();
         assertNotNull(nextFlowState);
         assertEquals(nextFlowState.getFlowTypeName(), nextFlowType1);
     }
 
     /**
      * @return
      */
     private FlowActivityImpl newFlowActivity() {
         return new FlowActivityImpl().initInvisible(false);
     }
 }
