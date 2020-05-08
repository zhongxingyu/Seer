 /*
  * Copyright 2011 Google Inc.
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
 package com.google.jstestdriver.output;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 
 /**
  * Tests to see if the file name is acceptible on the undlerying system.
  * 
  * @author Cory Smith (corbinrsmith@gmail.com)
  */
 public class FileNameFormatterTest extends TestCase {
 
   private File tmp;
 
 
   @Override
   protected void setUp() throws Exception {
    tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + this);
     tmp.mkdir();
     tmp.deleteOnExit();
   }
 
   public void testBadBrowserName() throws Exception {
     String path =
         "\\/50_(X11;_U;_Linux_x86_64;_en-US;_rv:19019)_Gecko/2010120923_Iceweasel/306_(Debian-306-3)__Linux.JSON DecodeDefault";
     String formattedPath = new FileNameFormatter().format(path, "%s.xml");
     assertCreateFile(formattedPath);
     assertPathExists(formattedPath);
   }
 
   public void testLongName() throws Exception {
     String path =
         "\\/50_(X11;_U;_Linux_x86_64;_en-US;_rv:19019)_Gecko/2010120923_Iceweasel/306_(Debian-306-3)__"
             + "\\Linux.JSON DecodeDefault\\/50_(X11;_U;_Linux_x86_64;_en-US;_rv:1"
             + "9019)_Gecko/2010120923_Iceweasel/306_(Debian-306-3)__Linux.JSON DecodeDefault\\/50_(X11;_U;_L"
             + "inux_x86_64;_en-US;_rv:19019)_Gecko/2010120923_Iceweasel/306_(Debian-306-3)__Linux.JSON DecodeDefault\\/50_(X1"
             + "1;_U;_Linux_x86_64;_en-US;_rv:19019)_Gecko/2010120923_Iceweasel/306_(Debian-306-3)__Linux.JSON DecodeDefault";
     String formattedPath = new FileNameFormatter().format(path, "%s.xml");
     assertCreateFile(formattedPath);
     assertPathExists(formattedPath);
   }
 
   public void testDot() throws Exception {
     String path = ".";
     String formattedPath = new FileNameFormatter().format(path, "%s.xml");
     assertCreateFile(formattedPath);
     assertPathExists(formattedPath);
   }
 
   public void testSpace() throws Exception {
     String path = " ";
     String formattedPath = new FileNameFormatter().format(path, "%s.xml");
     assertCreateFile(formattedPath);
     assertPathExists(formattedPath);
   }
 
   private void assertPathExists(String formattedPath) {
     assertTrue(String.format("path %s does not exist.", formattedPath),
         new File(tmp, formattedPath).exists());
   }
 
   @Override
   protected void tearDown() throws Exception {
     tmp.delete();
   }
 
   private void assertCreateFile(String formattedPath) throws IOException {
     try {
       File newFile = new File(tmp, formattedPath);
       newFile.deleteOnExit();
       assertTrue(String.format("path %s was not created.", formattedPath), newFile.createNewFile());
       assertTrue(String.format("path %s is not a file.", formattedPath), newFile.getCanonicalFile()
           .isFile());
     } catch (IOException e) {
      e.printStackTrace();
       fail(String.format("Error in creation: %s %s", formattedPath, e));
     }
   }
 }
