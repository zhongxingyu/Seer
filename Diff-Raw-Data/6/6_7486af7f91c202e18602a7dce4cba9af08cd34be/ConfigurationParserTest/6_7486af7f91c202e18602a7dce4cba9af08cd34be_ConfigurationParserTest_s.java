 /*
  * Copyright 2009 Google Inc.
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
 package com.google.jstestdriver;
 
 import junit.framework.TestCase;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class ConfigurationParserTest extends TestCase {
 
   public void testParseConfigFileAndHaveListOfFiles() throws Exception {
     File tmpDir =
       File.createTempFile("test", "JsTestDriver", new File(System.getProperty("java.io.tmpdir")));
 
     tmpDir.delete();
     tmpDir.mkdir();
     tmpDir.deleteOnExit();
     File codeDir = new File(tmpDir, "code");
     File testDir = new File(tmpDir, "test");
 
     codeDir.mkdir();
     codeDir.deleteOnExit();
     testDir.mkdir();
     testDir.deleteOnExit();
     File code = new File(codeDir, "code.js");
     File code2 = new File(codeDir, "code2.js");
     File test = new File(testDir, "test.js");
     File test2 = new File(testDir, "test2.js");
     File test3 = new File(testDir, "test3.js");
 
     code.createNewFile();
     code.deleteOnExit();
     code2.createNewFile();
     code2.deleteOnExit();
     test.createNewFile();
     test.deleteOnExit();
     test2.createNewFile();
     test2.deleteOnExit();
     test3.createNewFile();
     test3.deleteOnExit();
     ConfigurationParser parser = new ConfigurationParser(tmpDir);
     String configFile = "load:\n - code/*.js\n - test/*.js\nexclude:\n" +
           " - code/code2.js\n - test/test2.js";
     ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
 
     parser.parse(bais);
     Set<String> files = parser.getFilesList();
     List<String> listFiles = new ArrayList<String>(files);
 
     assertEquals(3, files.size());
    assertEquals("code/code.js", listFiles.get(0));
    assertEquals("test/test.js", listFiles.get(1));
    assertEquals("test/test3.js", listFiles.get(2));
   }
 }
