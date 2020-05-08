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
 
 import org.amplafi.flow.FlowManagement;
 import org.amplafi.flow.impl.FlowActivityImpl;
 import org.amplafi.flow.impl.FlowStateImpl;
 import org.amplafi.flow.FlowStateJsonRenderer;
 import org.amplafi.flow.validation.FlowValidationResultJsonRenderer;
 import org.amplafi.flow.validation.FlowValidationTrackingJsonRenderer;
 import org.amplafi.json.JSONStringer;
 import org.amplafi.json.JSONWriter;
 import org.amplafi.json.renderers.MapJsonRenderer;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 
 public class TestFlowStateJsonRenderer extends Assert {
 
 
     @Test
     public void testSimpleFlowState() {
         FlowStateImpl flowState = newFlowState();
        flowState.clearLookupKey();
         JSONWriter jsonWriter = getJsonWriter();
         jsonWriter.object().key("flowState").value(flowState).endObject();
         assertEquals(jsonWriter.toString(), "{\"flowState\":{\""+FlowStateJsonRenderer.FS_COMPLETE+"\":true,\"" +
                 FlowStateJsonRenderer.FS_PARAMETERS+"\":{}" +
         		"}}");
 
     }
     @Test
     public void testCompleteFlowState() {
         FlowStateImpl flowState = newFlowState();
         flowState.setRawProperty("property1", "value1");
         flowState.setRawProperty("property2", "value2");
        flowState.clearLookupKey();
         JSONWriter jsonWriter = getJsonWriter();
         jsonWriter.object().key("flowState").value(flowState).endObject();
         assertEquals(jsonWriter.toString(), "{\"flowState\":{\""+FlowStateJsonRenderer.FS_COMPLETE+"\":true,\""+
                 FlowStateJsonRenderer.FS_PARAMETERS+"\":{\"property1\":\"value1\",\"property2\":\"value2\"}}}");
     }
 
     private FlowStateImpl newFlowState() {
         FlowTestingUtils flowTestingUtils = new FlowTestingUtils();
         String flowTypeName = flowTestingUtils.addFlowDefinition(new FlowActivityImpl());
         FlowManagement flowManagement = flowTestingUtils.getFlowManagement();
         FlowStateImpl flowState = flowManagement.startFlowState(flowTypeName, true, null, null);
         flowState.finishFlow();
         return flowState;
     }
     private JSONWriter getJsonWriter() {
         MapJsonRenderer mapJsonRenderer = new MapJsonRenderer();
         JSONWriter jsonWriter = new JSONStringer();
         jsonWriter.addRenderer(FlowValidationResultJsonRenderer.INSTANCE);
         jsonWriter.addRenderer(FlowValidationTrackingJsonRenderer.INSTANCE);
         jsonWriter.addRenderer(new FlowStateJsonRenderer());
         jsonWriter.addRenderer(mapJsonRenderer);
         return jsonWriter;
     }
 }
