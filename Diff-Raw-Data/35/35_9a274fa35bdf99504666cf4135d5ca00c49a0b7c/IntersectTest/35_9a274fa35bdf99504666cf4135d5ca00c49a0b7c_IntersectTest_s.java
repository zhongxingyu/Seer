 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.neethi;
 
 import java.io.File;
 
 import org.apache.neethi.util.PolicyComparator;
 
 import org.junit.Test;
 
 public class IntersectTest extends PolicyTestCase {
     int failCount;
     
     public IntersectTest() {
     }
     
     @Test
     public void testOM() throws Exception {
         doTest("samples2", "intersected", 3);
     }
     @Test
     public void testDOM() throws Exception {
         doTest("samples2", "intersected", 1);
     }
     @Test
     public void testStax() throws Exception {
         doTest("samples2", "intersected", 2);
     }
     @Test
     public void testStream() throws Exception {
         doTest("samples2", "intersected", 3);
     }
     
     @Test
     public void testW3CDOM() throws Exception {
         doTest("w3tests", "w3tests" + File.separator + "Intersected", 1);
     }
 
     @Test
     public void testMyTest() throws Exception {
         runTest("w3tests", "w3tests" + File.separator + "Intersected",
                 "23", "26", "Policy23-26-lax.xml", false, 1);
     }
 
     public void doTest(String base, String intersectedDir, int type) throws Exception {
 
         File intersected = new File(testResourceDir, intersectedDir);
         File[] files = intersected.listFiles();
 
         String f;
         String f1;
         String f2;
 
         for (int i = 0; i < files.length; i++) {
             f = files[i].getName();
 
             if (files[i].isHidden()) { // to ignore .svn files
                 continue;
             }
             if (f.startsWith(".")) {
                 continue;
             }
 
             
             boolean strict = !f.contains("lax");
             if (f.contains("-lax")) {
                 f = f.substring(0, f.indexOf("-lax")) + f.substring(f.indexOf("-lax") + 4);
             }
             if (f.contains("-strict")) {
                 f = f.substring(0, f.indexOf("-strict")) + f.substring(f.indexOf("-strict") + 7);
             }
             f1 = f.substring(f.indexOf('y') + 1, f.indexOf('-'));
             
             f2 = f.substring(f.indexOf('-') + 1, f.indexOf('.'));
             
             runTest(base, intersectedDir, f1, f2, files[i].getName(), strict, type);
         }
     }
     public void runTest(String base, String intersectedDir, 
                         String f1, String f2, int type) throws Exception {
         runTest(base, intersectedDir, f1, f2, "Policy" + f1 + "-" + f2 + ".xml", true, type);
     }
     public void runTest(String base, String intersectedDir, 
                         String f1, String f2, String f,
                         boolean strict,
                         int type) throws Exception {
         String r1 = base + File.separator + "Policy" + f1 + ".xml";
         String r2 = base + File.separator + "Policy" + f2 + ".xml";
         String r3 = intersectedDir + File.separator + f;
 
         Policy p1 = getPolicy(r1, type);
         Policy p2 = getPolicy(r2, type);
         Policy p3 = getPolicy(r3, type);
 
         // result
         Policy p4 = (Policy)p1.intersect(p2, strict);
 
         if (p4 == null || !PolicyComparator.compare(p4, p3)) {
             /*
              System.out.println(++failCount + " Fail: " + base + File.separator + "Policy" 
                                + f1 + ".intersect(Policy" + f2 + ", "
                                + strict +")");
             */
             fail(base + File.separator + " Policy" + f1 + ".intersect(Policy" 
                  + f2 + ", " + strict + ") FAILED");
             /*
         } else {
             System.out.println("Pass: " + base + File.separator + "Policy" + f1 
                                + ".intersect(Policy" + f2 + ", "
                                + strict +")");
                                */
         }
     }
 }
