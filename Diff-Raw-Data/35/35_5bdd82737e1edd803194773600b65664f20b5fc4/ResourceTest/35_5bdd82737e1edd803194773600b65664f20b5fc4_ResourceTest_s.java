 /*
  * Copyright 2007-2009 Alexander Fabisch
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ev.datamodel;
 
 import com.ev.launch.Initializer;
 import static com.ev.datamodel.Resource.*;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  */
 public class ResourceTest {
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Initializer.initLogging();
         Initializer.initConfiguration();
     }
 
     @Test
     public void testValues() {
         Resource[] expResult = new Resource[] {GAS, POWER, WATER};
         Resource[] result = Resource.values();
         assertArrayEquals(expResult, result);
     }
 
     @Test
     public void testValueOf() {
         assertEquals(GAS, Resource.valueOf("GAS"));
         assertEquals(POWER, Resource.valueOf("POWER"));
         assertEquals(WATER, Resource.valueOf("WATER"));
     }
 
     @Test
     public void testFromInt() {
         assertEquals(GAS, Resource.fromInt(0));
         assertEquals(POWER, Resource.fromInt(1));
         assertEquals(WATER, Resource.fromInt(2));
     }
 
     @Test
     public void testToInt() {
         assertTrue(0 == GAS.ordinal());
         assertTrue(1 == POWER.ordinal());
         assertTrue(2 == WATER.ordinal());
     }
 
     @Test
     public void testNumber() {
         int expResult = 3;
         int result = Resource.number();
         assertTrue(expResult == result);
     }
 
     @Test
     public void testGetName() {
         Resource instance = POWER;
        String expResult = "Strom";
         String result = instance.getName();
        assertEquals(expResult, result);
     }
 
     @Test
     public void testGetNameWithUnit() {
         Resource instance = GAS;
        String expResult = "Gas in m³";
         String result = instance.getNameWithUnit();
        assertEquals(expResult, result);
     }
 
 }
