 /*
  *  Licensed to the Apache Software Foundation (ASF) under one or more
  *  contributor license agreements.  See the NOTICE file distributed with
  *  this work for additional information regarding copyright ownership.
  *  The ASF licenses this file to You under the Apache License, Version 2.0
  *  (the "License"); you may not use this file except in compliance with
  *  the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 package org.apache.httpcomponents.ant;
 
 import java.io.File;
 
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 public class FilePartNode extends PartNode {
 
     private File file;
 
     private String filename;
 
     private String charset;
 
     public void setFile(File file) {
         this.file = file;
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public void setCharset(String charset) {
         this.charset = charset;
     }
 
     @Override
     public ContentBody buildContentBoby() {
         if (file == null) {
             throw new BuildException("Missing 'file' attribute");
         }
         FileBody fileBody;
         String mimeType = getMimeType();
         if (mimeType == null) {
             mimeType = "application/octet-stream";
         }
         if (filename != null) {
             fileBody = new FileBody(file, filename, mimeType, charset);
         } else {
             fileBody = new FileBody(file, mimeType, charset);
         }
         return fileBody;
     }
 
     @Override
     public void log(Task task, int msgLevel) {
        task.log("File part: name=" + getName() + " file=" + file + (filename != null ? (" filename=" + filename) : ""));
     }
 }
