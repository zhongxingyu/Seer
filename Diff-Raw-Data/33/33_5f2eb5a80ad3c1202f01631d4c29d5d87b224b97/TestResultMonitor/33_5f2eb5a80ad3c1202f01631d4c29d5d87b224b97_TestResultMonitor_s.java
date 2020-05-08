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
 
 import com.thoughtworks.shadow.RunListenerAdaptee;
 import com.thoughtworks.shadow.TestShadow;
 import com.thoughtworks.shadow.TestShadowResult;
 import junit.framework.AssertionFailedError;
 import junit.framework.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TestResultMonitor implements RunListenerAdaptee {
     private List<ResultOfTestEndListener> listeners = new ArrayList<ResultOfTestEndListener>();
     private final TestShadowResult result;
 
     public TestResultMonitor(TestShadowResult result) {
         this.result = result;
     }
 
     public void addError(Test test, Throwable t) {
     }
 
     public void addFailure(Test test, AssertionFailedError t) {
     }
 
     public void testIgnored(TestShadow testShadow) {
     }
 
     public void endTest(Test test) {
         fireEvent();
     }
 
     private void fireEvent() {
         for (int i = 0; i < listeners.size(); i++) {
            ((ResultOfTestEndListener) listeners.get(i)).testEnd(result);
         }
     }
 
     public void startTest(Test test) {
     }
 
     public void addListener(ResultOfTestEndListener listener) {
         listeners.add(listener);
     }
 
     public void start() {
         fireEvent();
     }
 
 }
