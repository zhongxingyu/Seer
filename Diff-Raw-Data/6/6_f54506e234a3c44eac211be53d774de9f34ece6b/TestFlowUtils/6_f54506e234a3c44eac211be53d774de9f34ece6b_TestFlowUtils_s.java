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
 
 import java.util.List;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import static org.testng.Assert.*;
 
 /**
  * Test {@link FlowUtils}.
  * @author andyhot
  */
 public class TestFlowUtils {
 
     @Test(dataProvider="lowerCase")
     public void testToLowerCase(String name, String expected) {
         assertEquals(FlowUtils.INSTANCE.toLowerCase(name), expected);
     }
 
     @Test
     public void testCreateInitialValuesFromArray() {
        List<String> values = FlowUtils.INSTANCE.createInitialValues("id", 1L, Boolean.class, true);
        System.out.println(values);
         assertEquals(values.size(), 2);
         assertEquals(values.get(0), "id='1'");
        assertEquals(values.get(1), "boolean='true'");
     }
 
     @DataProvider(name="lowerCase")
     protected Object[][] toLowerCaseData() {
         return new Object[][] {
             {"", ""},
             {"flow", "flow"},
             {"Flow", "flow"},
             {"ThisFlow", "this-flow"},
             {"ThisFLOW", "this-f-l-o-w"},
         };
     }
 
 }
