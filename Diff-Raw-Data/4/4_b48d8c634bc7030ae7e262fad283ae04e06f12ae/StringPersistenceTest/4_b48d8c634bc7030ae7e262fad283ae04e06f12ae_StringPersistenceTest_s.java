 /*
  * Copyright (c) 2009 Jens Scheffler (appenginefan.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the
  * License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  * CONDITIONS OF ANY KIND, either express or implied. See
  * the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.appenginefan.toolkit.persistence;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 
 import junit.framework.TestCase;
 
 /**
 * A template for testing persistences that can digest byte
 * arrays. This class uses a MapBasedPersistence, but this
 * can be changed by simply overwriting the setUp method
  */
 public class StringPersistenceTest
     extends TestCase {
 
   private StringPersistence persistence;
 
   /**
    * will set up a MapBasedPersistence if persistence is
    * null when called.
    */
   @Override
   protected void setUp() throws Exception {
     super.setUp();
     persistence =
         new StringPersistence(
             new MapBasedPersistence<byte[]>());
   }
 
   public void testConversion() {
     for (String s : new String[] { "regular String", "" }) {
       assertEquals(s, persistence.makeType(persistence
           .makeArray(s)));
     }
   }
 
   public void testBasicSetAndGet() {
     persistence.mutate("A", Functions.constant("A"));
     assertEquals(persistence.get("A"), "A");
   }
 
   public void testOverwrite() {
     persistence.mutate("A", Functions.constant("A"));
     assertEquals("B", persistence.mutate("A", Functions
         .constant("B")));
     assertEquals(persistence.get("A"), "B");
   }
 
   public void testGetUnknownKey() {
     assertNull(persistence.get("A"));
   }
 
   public void testDelete() {
     persistence.mutate("A", Functions.constant("A"));
     assertNull(persistence.mutate("A", Functions
         .constant((String) null)));
     assertNull(persistence.get("A"));
   }
 
   public void testFunctionInput() {
     persistence.mutate("A", Functions.constant("B"));
     persistence.mutate("A", new Function<String, String>() {
       @Override
       public String apply(String fromPersistence) {
         assertEquals(fromPersistence, "B");
         return "C";
       }
     });
     assertEquals(persistence.get("A"), "C");
   }
 
   public void testScan() {
     persistence.mutate("A1", Functions.constant("A1"));
     persistence.mutate("A2", Functions.constant("A2"));
     persistence.mutate("A3", Functions.constant("A3"));
     persistence.mutate("A2", Functions
         .constant((String) null));
     List<Entry<String, String>> scanResult =
         persistence.scan("A", "B", 10);
     assertEquals(2, scanResult.size());
     assertEquals("A1", scanResult.get(0).getKey());
     assertEquals("A3", scanResult.get(1).getKey());
     assertEquals("A1", scanResult.get(0).getValue());
     assertEquals("A3", scanResult.get(1).getValue());
     scanResult = persistence.scan("A1", "A3", 10);
     assertEquals(1, scanResult.size());
     assertEquals("A1", scanResult.get(0).getKey());
     scanResult = persistence.scan("A1", "A4", 1);
     assertEquals(1, scanResult.size());
     assertEquals("A1", scanResult.get(0).getKey());
     scanResult = persistence.scan("B", "Z", 10);
     assertEquals(0, scanResult.size());
     scanResult = persistence.scan("A1", "A4", 0);
     assertEquals(0, scanResult.size());
   }
 
 }
