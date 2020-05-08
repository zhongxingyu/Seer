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
 import java.io.File;
 import org.junit.*;
 import static org.junit.Assert.*;
 
 /**
  * @author <a href="mailto:afabisch@tzi.de">Alexander Fabisch</a>
  */
 public class CounterOfAYearUnitTest {
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         Initializer.initLogging();
         Initializer.initConfiguration();
     }
 
     @Before
     public void setUp() {
         deleteFileHelper();
     }
 
     @After
     public void tearDown() {
         deleteFileHelper();
     }
 
     @Test
     public void readFileSuccess() {
         CounterOfAYearFileImpl instance = new CounterOfAYearFileImpl(2005);
         assertEquals(15439.0, instance.getCounter(Resource.GAS, Month.JANUARY), 0.1);
         assertEquals("comments not loaded", "6Tage zu sp\u00E4t abgelesen", instance.getComment(Month.MAY));
     }
 
     @Test
     public void writeOldFile() {
         CounterOfAYearFileImpl instance = new CounterOfAYearFileImpl(2005);
         instance.save();
     }
 
     @Test
     public void writeNewFile() {
         CounterOfAYearFileImpl instance = new CounterOfAYearFileImpl(0);
         instance.save();
     }
 
     @Test
     public void toStringReturnsTitle() {
         CounterOfAYearFileImpl instance = new CounterOfAYearFileImpl(2009);
        assert("Energieverbrauch 2009".equals(instance.toString())
                || "Energy Consumption 2009".equals(instance.toString()));
     }
 
     private void deleteFileHelper() {
         File fixture = new File("default" + File.separator + "0");
         if (fixture.exists()) {
             fixture.delete();
         }
     }
 
 }
