 /*
  * #%L
  * Bitrepository Integrity Service
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.integrityservice.mocks;
 
 import java.util.Date;
 
 import org.bitrepository.integrityservice.workflow.scheduler.IntervalWorkflow;
 
 /**
  * A trigger that triggers every other second, and remembers calls.
  */
 public class MockWorkflow extends IntervalWorkflow {
     /**
      * Get the number of times isTriggered() is called.
      * @return The number of times triggered() is called.
      */
     public int getCallsForNextRun() {
         return getNextRunCount;
     }
 
     /**
      * Get the number of times trigger() is called.
      * @return The number of times trigger() is called.
      */
    public int getcallsForRunWorkflow() {
         return runWorkflowCount;
     }
 
     /** Number of times isTriggered() is called. */
     private int getNextRunCount = 0;
     /** Number of times trigger() is called. */
     private int runWorkflowCount = 0;
 
     @Override
     public Date getNextRun() {
         getNextRunCount++;
         return super.getNextRun();
     }
     
     /**
      * Initialise trigger.
      */
     public MockWorkflow(Long interval, String name) {
         super(interval, name);
     }
 
     @Override
     public void runWorkflow() {
         runWorkflowCount++;
     }
 }
