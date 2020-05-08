 /*
  * Copyright (c) 2010 Sharegrove Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.msjs.script;
 
 import org.junit.Test;
 
 public class TestMsjs extends BaseScriptTest {
 
     @Override
     protected String getTestDirectory() {return "/test/msjs";}
 
     @Test
     public void testNode() {
         runTest("testnode.js");
     }
 
     @Test
     public void testModel() {
         runTest("testmodel.js");
     }
 
     @Test
     public void testPacking() {
         runTest("testpacking.js");
     }
 
     @Test
     public void testRequires() {
         runTest("testrequires.js");
     }
 
     @Test
     public void testTransient() {
         runTest("testtransient.js");
     }
 
     @Test
     public void testStrongComponents() {
         runTest("teststrongcomponents.js");
     }
 
     @Test
     public void testJSON() {
         runTest("testjson.js");
     }
 
     @Test
     public void testDom() {
         runTest("testdom.js");
     }
 
     @Test
     public void testInclude() {
         runTest("testinclude.js");
     }
 
     @Test
     public void testAsync() {
         runTest("testasync.js");
     }
 
     @Test
     public void testTopoSort() {
         runTest("testtoposort.js");
     }
 
     @Test
     public void testMsjCaching() {
         runTest("testmsjcaching.js");
     }
 
     @Test
     public void testPublishTwice() {
         runTest("testpublishtwice.js");
     }
 
     @Test
     public void testTransponder() {
         runTest("testtransponder.js");
     }
 
     @Test
     public void testBadTransponderDependency() {
         runTest("testbadtransponderdependency.js");
     }
 
     @Test
     public void testFreeVariables() {
         runTest("testfreevariables.js");
     }
 
     @Test
    public void testClientPublish() {
        runTest("testclientpublish.js");
    }

    @Test
     public void testFunctionPack() {
         runTest("testfunctionpack.js");
     }
 }
