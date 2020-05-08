 /*
  * Copyright 2011 Vasily Shiyan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package see.integration;
 
 import org.junit.Test;
 import see.See;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.collect.ImmutableMap.of;
 import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 public class CollectionsTest {
     See see = new See();
 
     @Test
     public void testLists() throws Exception {
         Object list = see.eval("['crn', 'bka']");
         assertEquals(asList("crn", "bka"), list);
     }
 
     @Test
     public void testMaps() throws Exception {
         Object map = see.eval("{crn: 'bka', cat: 'fine too'}");
         assertEquals(of("crn", "bka", "cat", "fine too"), map);
     }
 
     @Test
     public void testExtendedMapKeys() throws Exception {
         Object map = see.eval("{9: 'bka', 'cat': 'fine too', \"crn\": 'bka'}");
         assertEquals(of("9", "bka", "cat", "fine too", "crn", "bka"), map);
     }
 
     @Test
     public void testMixed() throws Exception {
         Object mixed = see.eval("{list: ['omg', 'wtf', 'bbq'], " +
                 "map : {crn: 'bka', n: [9, 21*2, concat('100', '500')]}}"
         );
         Object expected = of(
                 "list", asList("omg", "wtf", "bbq"),
                 "map", of("crn", "bka", "n", asList(new BigDecimal(9), new BigDecimal(42), "100500"))
         );
 
         assertEquals(expected, mixed);
     }
 
     @Test
     public void testNullInMaps() throws Exception {
         Map<String, ?> map = (Map<String, ?>) see.eval("{ n: null }");
         assertNull(map.get("n"));
     }
 
     @Test
     public void testNullInLists() throws Exception {
         List<?> list = (List<?>) see.eval("['a', null, 'b']");
         assertNull(list.get(1));
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void testListImmutability() throws Exception {
         List<Object> list = (List<Object>) see.eval("['a', null, 'b']");
         list.add("c");
     }
 
     @Test(expected = UnsupportedOperationException.class)
     public void testMapImmutability() throws Exception {
         Map<String, Object> map = (Map<String, Object>) see.eval("{ c: 9 }");
         map.put("a", "new entry");
     }

    @Test
    public void testDotInMapKeys() throws Exception {
        see.eval("{}['a.b']");
    }
 }
