 /*
  * Copyright 2010 Pentaho Corporation.  All rights reserved.
  *                   
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * 
  * http://www.apache.org/licenses/LICENSE-2.0 
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License. 
  *
  * @author Michael D'Amour
  */
 package org.pentaho.hdfs.vfs;
 
 import java.util.Collection;
 
 import org.apache.commons.vfs.*;
 import org.apache.commons.vfs.provider.AbstractFileSystem;
 import org.apache.commons.vfs.provider.GenericFileName;
 import org.apache.hadoop.conf.Configuration;
 
 public class HDFSFileSystem extends AbstractFileSystem implements FileSystem {
 
   private static org.apache.hadoop.fs.FileSystem mockHdfs;
   private org.apache.hadoop.fs.FileSystem hdfs;
 
   protected HDFSFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) {
     super(rootName, null, fileSystemOptions);
   }
 
   @SuppressWarnings("unchecked")
   protected void addCapabilities(Collection caps) {
     caps.addAll(HDFSFileProvider.capabilities);
   }
 
   protected FileObject createFile(FileName name) throws Exception {
     return new HDFSFileObject(name, this);
   }
 
   
   /**
    * Use of this method is for unit testing, it allows us to poke in a test filesystem without
    * interfering with any other objects in the vfs system
    * 
    * @param hdfs the mock file system
    */
   public static void setMockHDFSFileSystem(org.apache.hadoop.fs.FileSystem hdfs) {
     mockHdfs = hdfs;
   }
   
   public org.apache.hadoop.fs.FileSystem getHDFSFileSystem() throws FileSystemException {
     if (mockHdfs != null) {
       return mockHdfs;
     }
     if (hdfs == null) {
       Configuration conf = new Configuration();
       GenericFileName genericFileName = (GenericFileName) getRootName();
       String url = "hdfs://" + genericFileName.getHostName() + ":" + genericFileName.getPort();
       conf.set("fs.default.name", url);
       if (genericFileName.getUserName() != null && !"".equals(genericFileName.getUserName())) {
         conf.set("hadoop.job.ugi", genericFileName.getUserName() + ", " + genericFileName.getPassword());
       }
       try {
         hdfs = org.apache.hadoop.fs.FileSystem.get(conf);
       } catch (Throwable t) {
         throw new FileSystemException("Could not getHDFSFileSystem() for " + url, t);
       }
     }
     return hdfs;
   }
 
 }
