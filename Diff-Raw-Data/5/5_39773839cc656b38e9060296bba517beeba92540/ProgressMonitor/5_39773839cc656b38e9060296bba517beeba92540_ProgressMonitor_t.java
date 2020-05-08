 /*
  * Copyright 2008 FatWire Corporation. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.fatwire.dta.sscrawler.jobs;
 
 public interface ProgressMonitor {
 
     /**
      * Notifies that the main task is beginning.
      * 
      * @param name
      * @param totalWork
      */
     void beginTask(String name, int totalWork);
 
     /**
      *Notifies that the work is done; that is, either the main task is
      * completed or the user canceled it.
      * 
      */
     void done();
 
     /**
      *Internal method to handle scaling correctly.
      * 
      * @param work
      */
     void internalWorked(double work);
 
     /**
     *Returns whether cancellation of current operation has been requested.
      * 
     * @return true is operation is canceled.
      */
     boolean isCanceled();
 
     /**
      * Sets the cancel state to the given value.
      * 
      * @param value
      */
     void setCanceled(boolean value);
 
     /**
      * Sets the task name to the given value.
      * 
      * @param name
      */
     void setTaskName(String name);
 
     /**
      * Notifies that a subtask of the main task is beginning.
      * 
      * @param name
      */
     void subTask(String name);
 
     /**
      * Notifies that a given number of work unit of the main task has been
      * completed.
      * 
      * @param work
      */
     void worked(int work);
 
 }
