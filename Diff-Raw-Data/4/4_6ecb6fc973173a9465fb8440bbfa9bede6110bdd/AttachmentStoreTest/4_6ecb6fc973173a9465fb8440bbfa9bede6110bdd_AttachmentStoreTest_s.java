 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.wave.examples.fedone.persistence.file;
 
 import org.waveprotocol.wave.examples.fedone.persistence.AttachmentStore;
 import org.waveprotocol.wave.examples.fedone.persistence.AttachmentStoreTestBase;
 import org.waveprotocol.wave.examples.fedone.persistence.FileBasedAttachmentStore;
 
 import java.io.File;
 
 /**
  * A wrapper for the tests in AttachmentStoreBase which uses a file based
  * attachment store.
  */
 public class AttachmentStoreTest extends AttachmentStoreTestBase {
   private String pathName; 
   
   @Override
   protected void setUp() throws Exception {
     // We want a temporary directory. createTempFile will make a file with a
     // good temporary path. Its a bit nasty, but we'll create the file, then
     // delete it and create a directory with the same name.
     
     File dir = File.createTempFile("fedoneattachments", null);
     pathName = dir.getAbsolutePath();
     
     if (!dir.delete() || !dir.mkdir()) {
       throw new RuntimeException("Could not make temporary directory for attachment store: "
           + dir);
     }
   }
   
   @Override
   protected AttachmentStore newAttachmentStore() {
     return new FileBasedAttachmentStore(pathName);
   }
 
   @Override
   protected void tearDown() throws Exception {
     super.tearDown();
     
     File path = new File(pathName);
     if (path.exists()) {
       // ... And delete all the files in the directory.
       for (File f : path.listFiles()) {
         f.delete();
       }
       path.delete();
     }
     
    // This will happen if one of the InputStreams hasn't been closed.
     assertFalse(path.exists());
   }
 }
