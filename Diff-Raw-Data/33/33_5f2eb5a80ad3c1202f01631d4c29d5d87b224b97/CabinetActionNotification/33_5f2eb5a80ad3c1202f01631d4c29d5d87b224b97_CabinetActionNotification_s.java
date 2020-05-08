 /*
  *    Copyright (c) 2006 LiXiao.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.thoughtworks.fireworks.core;
 
 import com.thoughtworks.shadow.Cabinet;
 import junit.framework.TestResult;
import org.apache.log4j.Logger;
 
 public class CabinetActionNotification implements CompileStatusNotificationAdaptee {
    private static final Logger LOG = Logger.getLogger(CabinetActionNotification.class);
     private final Cabinet cabinet;
     private final TestResult result;
 
     public CabinetActionNotification(Cabinet cabinet, TestResult result) {
         this.cabinet = cabinet;
         this.result = result;
     }
 
     public void finished(boolean aborted, int errors, int warnings) {
        LOG.info("aborted: " + aborted + "; errors: " + errors);
         if (aborted || errors > 0) {
             return;
         }
        LOG.info("cabinet action");
         cabinet.action(result);
     }
 }
