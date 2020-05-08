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
 
 import static org.amplafi.flow.FlowConstants.FAINVISIBLE;
 import static org.amplafi.flow.flowproperty.PropertyScope.activityLocal;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertNotSame;
 import static org.testng.Assert.assertSame;
 import static org.testng.Assert.assertTrue;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionBuilder;
 import org.amplafi.flow.flowproperty.FlowPropertyDefinitionImplementor;
 import org.amplafi.flow.impl.FlowActivityImpl;
 import org.amplafi.flow.impl.FlowImpl;
 import org.amplafi.flow.impl.FlowStateImpl;
 import org.amplafi.flow.impl.FlowStateImplementor;
 import org.testng.annotations.Test;
 
 import com.sworddance.util.map.NamespaceMapKey;
 
 /**
  * Tests around flows that don't require db.
  *
  * @author Patrick Moore
  */
 public class TestFlows {
     private static final String SET_BY_MAP = "set-by-map";
     private static final String INITIAL_VALUE = "initial-property";
     private static final String PROPERTY1 = "property1";
     private static final String PROPERTY2 = "property2";
     private static final String FLOW_TYPE = "ftype1";
     private static final boolean TEST_ENABLED = true;
 
     /**
      * Test simple flow definitions and instances.
      *
      */
     @Test(enabled=TEST_ENABLED)
     public void testFlowDefinition() {
         FlowImpl flow = new FlowImpl();
         FlowActivity[] fas = new FlowActivity[3];
         for(int i = 0; i < fas.length; i++) {
             FlowActivityImpl fa = new FlowActivityImpl();
             fas[i] = fa;
             flow.addActivity(fa);
         }
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
         for(int i=0; i < fas.length; i++) {
             // make sure order is preserved.
             assertSame(flow.getActivity(i), fas[i]);
             // check that in definition all was as defined.
             assertFalse(fas[i].isActivatable());
             assertFalse(fas[i].isFinishingActivity());
         }
 
         Flow instance = flow.createInstance();
         List<FlowActivityImplementor> ifas = instance.getActivities();
 
         // make sure they are different and definition has not changed.
         for(int i=0; i < fas.length; i++) {
             // make sure order is preserved.
             assertNotSame(ifas.get(i), fas[i]);
             // check that in definition all was as defined.
             assertFalse(fas[i].isActivatable());
             assertFalse(fas[i].isFinishingActivity());
         }
 
         // check that first /last step of instance are set up correctly.
         assertTrue(ifas.get(0).isActivatable());
         assertFalse(ifas.get(0).isFinishingActivity());
         assertFalse(ifas.get(1).isActivatable());
         assertFalse(ifas.get(1).isFinishingActivity());
         assertFalse(ifas.get(2).isActivatable());
         assertFalse(ifas.get(2).isFinishingActivity());
     }
 
     /**
      * check to make sure properties specific to a FlowActivity are checked
      * and returned first.
      *
      */
     @Test
     public void testPropertyPriority() {
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         {
             FlowActivityImpl flowActivity0 = new FlowActivityImpl("fs0");
             flowActivity0.addPropertyDefinitions(new FlowPropertyDefinitionBuilder("key").initPropertyScope(activityLocal));
             FlowActivityImpl flowActivity1 = new FlowActivityImpl("fs1");
             FlowActivityImpl flowActivity2 = new FlowActivityImpl("fs2");
             flowActivity2.addPropertyDefinitions(new FlowPropertyDefinitionBuilder("key").initPropertyScope(activityLocal));
             FlowImpl flow = new FlowImpl(FLOW_TYPE, flowActivity0, flowActivity1, flowActivity2);
             flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
             flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
         }
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(
             "key", "fs",
             FlowUtils.INSTANCE.toKey("fs0","key"), "fs0");
         FlowStateImplementor fs = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState);
 
         FlowActivityImplementor activity0 = fs.getActivity(0);
         assertEquals(activity0.getProperty("key"), "fs0", "flowState="+fs);
         FlowActivityImplementor activity1 = fs.getActivity(1);
         // TODO: currently broken - with the way we are generating temporary definitions. ( maybe we don't allow adhoc definitions by default.)
        // used to be null
        assertEquals(activity1.getProperty("key"), "fs", "flowActivity1 did not declare 'key' so should not see the values");
         FlowActivityImplementor activity2 = fs.getActivity(2);
         assertEquals(activity2.getProperty("key"), "fs", "flowActivity2 declared 'key' as flowLocal so should not see flowActivity0's changes which are activityLocal");
 
         activity0.setProperty("key", "new-fs0");
         activity1.setProperty("key", "new-fs");
 
         assertEquals(activity0.getProperty("key"), "new-fs0", "flowState="+fs);
        // used to be "new-fs"
        assertEquals(activity1.getProperty("key"), "fs", "flowState="+fs);
     }
 
     /**
      * Test for hasVisibleNext and hasVisiblePrevious of FlowState.
      */
     @Test(enabled=TEST_ENABLED)
     public void testVisiblePreviousNext() {
         FlowImpl flow = new FlowImpl(FLOW_TYPE);
         flow.addActivity(new FlowActivityImpl().initInvisible(false));
         flow.addActivity(new FlowActivityImpl().initInvisible(false));
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
         flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
 
         FlowState fs = new FlowStateImpl(FLOW_TYPE, flowManagement);
         fs.begin();
 
         assertTrue(fs.hasNext());
         assertTrue(fs.hasVisibleNext());
         assertFalse(fs.hasPrevious());
         assertFalse(fs.hasVisiblePrevious());
 
         fs.selectActivity(1, true);
 
         assertFalse(fs.hasNext());
         assertFalse(fs.hasVisibleNext());
         assertTrue(fs.hasPrevious());
         assertTrue(fs.hasVisiblePrevious());
     }
 
     /**
      * Test for hasVisibleNext and hasVisiblePrevious of FlowState when there are invisible FlowActivities.
      *
      * Also test when invisible is turned on/off during the flow.
      */
     @Test(enabled=TEST_ENABLED)
     public void testVisiblePreviousNextWithHidden() {
         FlowImplementor flow = new FlowImpl(FLOW_TYPE);
         FlowActivityImpl fa1 = new FlowActivityImpl().initInvisible(true);
         FlowActivityImpl fa2 = new FlowActivityImpl().initInvisible(true);
         FlowActivityImpl fa3 = new FlowActivityImpl().initInvisible(false);
         flow.addActivity(fa1);
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowTranslatorResolver().resolve(fa1);
         flow.addActivity(fa3);
         flowTestingUtils.getFlowTranslatorResolver().resolve(fa3);
         flow.addActivity(fa2);
         flowTestingUtils.getFlowTranslatorResolver().resolve(fa1);
         flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
 
         FlowState fs = new FlowStateImpl(FLOW_TYPE, flowManagement);
         fs.begin();
 
         assertEquals(fs.getCurrentActivityIndex(), 1);
         assertTrue(fs.hasNext());
         assertFalse(fs.hasVisibleNext());
         assertTrue(fs.hasPrevious());
         assertFalse(fs.hasVisiblePrevious());
     }
 
     @Test(enabled=TEST_ENABLED)
     public void testInitialValuesOnFlow() {
         FlowImplementor flow = new FlowImpl(FLOW_TYPE);
         FlowPropertyDefinitionImplementor globalDef = new FlowPropertyDefinitionBuilder(PROPERTY1).setInitial(INITIAL_VALUE).toFlowPropertyDefinition();
         flow.addPropertyDefinitions(globalDef);
         FlowPropertyDefinitionImplementor globalDef1 = new FlowPropertyDefinitionBuilder(PROPERTY2).setInitial(INITIAL_VALUE).toFlowPropertyDefinition();
         flow.addPropertyDefinitions(globalDef1);
         // activity #0
         FlowActivityImpl activity = new FlowActivityImpl();
         flow.addActivity(activity);
         // activity #1
         activity = new FlowActivityImpl();
         flow.addActivity(activity);
         // activity #2
         activity = new FlowActivityImpl();
         FlowPropertyDefinitionImplementor localDef1 = new FlowPropertyDefinitionBuilder(PROPERTY1).toFlowPropertyDefinition();
         activity.addPropertyDefinitions(localDef1);
         flow.addActivity(activity);
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowTranslatorResolver().resolveFlow(flow);
 
         flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         Map<String, String> initialFlowState = new HashMap<String, String>();
         initialFlowState.put(PROPERTY2, SET_BY_MAP);
         FlowStateImplementor flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState);
         assertEquals(flowState.getActivity(0).getProperty(PROPERTY1), INITIAL_VALUE, "flowState="+flowState);
         flowState.clearCache();
         assertEquals(flowState.getActivity(1).getProperty(PROPERTY2), SET_BY_MAP, "flowState="+flowState);
         flowState.clearCache();
         assertEquals(flowState.getActivity(2).getProperty(PROPERTY1), INITIAL_VALUE, "flowState="+flowState);
         flowState.clearCache();
     }
 
     @Test(enabled=TEST_ENABLED)
     public void testConversion() {
         Map<String, String> initialFlowState = new HashMap<String, String>();
         FlowImplementor flow = new FlowImpl(FLOW_TYPE);
         flow.addPropertyDefinitions(new FlowPropertyDefinitionBuilder("foo", Long.class));
         FlowActivityImpl fa1 = new FlowActivityImpl();
         flow.addActivity(fa1);
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         flowTestingUtils.getFlowTranslatorResolver().resolve(fa1);
         flowTestingUtils.getFlowDefinitionsManager().addDefinition(flow);
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         FlowState flowState = flowManagement.startFlowState(FLOW_TYPE, true, initialFlowState);
         flowState.setProperty("fee", true);
         Flow inst = flowState.getFlow();
         FlowPropertyDefinition flowPropertyDefinition = inst.getFlowPropertyDefinition("fee");
         assertTrue(flowPropertyDefinition.getDataClass() == Boolean.class || flowPropertyDefinition.getDataClass() == boolean.class);
 
         // make sure that we can still see the original property definitions.
         flowPropertyDefinition = inst.getFlowPropertyDefinition("foo");
         assertTrue(flowPropertyDefinition.getDataClass() == Long.class);
     }
 
     /**
      * Make sure that a FA changing its visibility does not cause other FAs to change their visibility.
      */
     @Test(enabled=TEST_ENABLED)
     public void testInvisibleFlowActivitiesInterferingWithVisibleFA() {
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         FlowActivityImplementor vis0 = new FlowActivityImpl("vis0").initInvisible(false);
         FlowActivityImplementor inv1 = new FlowActivityImpl("inv1").initInvisible(true);
         FlowActivityImplementor chg2 = new FlowActivityImpl("chg2").initInvisible(false);
         FlowActivityImplementor vis3 = new FlowActivityImpl("vis3").initInvisible(false);
         String flowTypeName = flowTestingUtils.addFlowDefinition(vis0, inv1, chg2,vis3);
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         String prefix = chg2.getFlowPropertyProviderFullName();
         //TODO need mechanism to generate correct namespace/key for setting an override value.
         Map<String, String> initialFlowState = FlowUtils.INSTANCE.createState(prefix + NamespaceMapKey.NAMESPACE_SEPARATOR + FAINVISIBLE, true);
         FlowState flowState = flowManagement.startFlowState(flowTypeName, true, initialFlowState);
         FlowActivityImplementor newChg2 = flowState.getActivity("chg2");
         assertTrue(newChg2.isInvisible());
         List<FlowActivity> visibleActivities = flowState.getVisibleActivities();
         assertEquals(visibleActivities.size(), 2,"visible activities="+visibleActivities);
         newChg2.setInvisible(false);
         visibleActivities = flowState.getVisibleActivities();
         assertEquals(visibleActivities.size(), 3,"visible activities="+visibleActivities);
     }
 
 }
