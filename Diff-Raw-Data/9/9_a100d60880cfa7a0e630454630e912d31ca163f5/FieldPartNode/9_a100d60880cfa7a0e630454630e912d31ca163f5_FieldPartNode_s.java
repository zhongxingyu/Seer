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
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.nio.charset.UnsupportedCharsetException;
 
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 public class FieldPartNode extends PartNode {
 
     private String value;
 
     private String charset;
 
     public void setValue(String value) {
         addText(value);
     }
 
     public void addText(String value) {
         if (this.value != null) {
             this.value += value;
         } else {
             this.value = value;
         }
     }
 
     public void setCharset(String charset) {
         this.charset = charset;
     }
 
     @Override
     public ContentBody buildContentBoby() {
         String mimeType = getMimeType();
         if (mimeType == null) {
             mimeType = "text/plain";
         }
         Charset c = null;
         if (charset != null) {
             try {
                 c = Charset.forName(charset);
             } catch (IllegalCharsetNameException e) {
                 throw new BuildException("Incorrect charset name " + charset);
             } catch (UnsupportedCharsetException e) {
                 throw new BuildException("Unsupported charset " + charset);
             }
         }
         StringBody body;
         try {
             body = new StringBody(value, mimeType, c);
         } catch (UnsupportedEncodingException e) {
             throw new BuildException("Unsupported default encoding ", e);
         }
         return body;
     }
 
     @Override
     public void log(Task task, int msgLevel) {
        task.log("Field part: name=" + getName() + " value=" + value);
     }
 }
