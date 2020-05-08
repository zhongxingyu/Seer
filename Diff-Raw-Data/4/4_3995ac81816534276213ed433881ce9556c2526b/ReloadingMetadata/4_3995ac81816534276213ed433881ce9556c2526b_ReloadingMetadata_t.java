 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.refresh;
 
 import org.apache.myfaces.scripting.api.ScriptingConst;
 
 /**
  * data structure which holds the loaded data
  * for our taint thread
  *
  * @author Werner Punz
  */
 public class ReloadingMetadata {
 
    /*
     * volatile due to the ram concurrency behavior
     * of the instance vars jdk 5+
     */
     volatile boolean tainted = false;
     volatile boolean annotated = false;
     volatile boolean taintedOnce = false;
     volatile String fileName = "";
     volatile String sourcePath = "";
     volatile Class aClass = null;
     volatile long timestamp = 0l;
     volatile int scriptingEngine = ScriptingConst.ENGINE_TYPE_NO_ENGINE;
 
 
     public boolean isTainted() {
         return tainted;
     }
 
     public void setTainted(boolean tainted) {
         this.tainted = tainted;
     }
 
     public boolean isTaintedOnce() {
         return taintedOnce;
     }
 
     public void setTaintedOnce(boolean taintedOnce) {
         this.taintedOnce = taintedOnce;
     }
 
     public String getFileName() {
         return fileName;
     }
 
     public void setFileName(String fileName) {
         this.fileName = fileName;
     }
 
     public Class getAClass() {
         return aClass;
     }
 
     public void setAClass(Class aClass) {
         this.aClass = aClass;
     }
 
     public long getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(long timestamp) {
         this.timestamp = timestamp;
     }
 
     public int getScriptingEngine() {
         return scriptingEngine;
     }
 
     public void setScriptingEngine(int scriptingEngine) {
         this.scriptingEngine = scriptingEngine;
     }
 
     public String getSourcePath() {
         return sourcePath;
     }
 
     public void setSourcePath(String sourcePath) {
         this.sourcePath = sourcePath;
     }
 
     public boolean isAnnotated() {
         return annotated;
     }
 
     public void setAnnotated(boolean annotated) {
         this.annotated = annotated;
     }
 }
