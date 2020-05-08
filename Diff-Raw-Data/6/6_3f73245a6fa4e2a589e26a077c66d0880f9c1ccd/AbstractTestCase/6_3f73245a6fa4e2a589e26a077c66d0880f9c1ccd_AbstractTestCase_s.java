 /**
  *    This module represents an engine IMPL for the load testing framework
  *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.loadtest.engine.impl;
 
 import com.smartitengineering.loadtest.engine.TestCase;
 import com.smartitengineering.loadtest.engine.events.TestCaseStateChangeListener;
 import com.smartitengineering.loadtest.engine.events.TestCaseStateChangedEvent;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author imyousuf
  */
 public abstract class AbstractTestCase
     implements TestCase {
 
     private boolean stoppable;
     private boolean interruptable;
     private TestCase.State state;
     private Date startTimeOfTest;
     private Date stopTimeOfTest;
     private Set<TestCaseStateChangeListener> changeListeners;
 
     public AbstractTestCase() {
         setStoppable(false);
         setInterruptable(true);
         setState(State.CREATED);
        changeListeners = new HashSet<TestCaseStateChangeListener>();
     }
 
     public Date getStartTimeOfTest()
         throws IllegalStateException {
         if (getState().getStateStep() >= State.STARTED.getStateStep()) {
             return startTimeOfTest;
         }
         else {
             throw new IllegalStateException();
         }
     }
 
     public Date getEndTimeOfTest()
         throws IllegalStateException {
         if (getState().getStateStep() >= State.FINISHED.getStateStep()) {
             return stopTimeOfTest;
         }
         else {
             throw new IllegalStateException();
         }
     }
 
     public long getTestDuration()
         throws IllegalStateException {
         if (getState().getStateStep() >= State.FINISHED.getStateStep()) {
             return stopTimeOfTest.getTime() - startTimeOfTest.getTime();
         }
         else {
             throw new IllegalStateException();
         }
     }
 
     public void stopTest()
         throws UnsupportedOperationException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean isStoppable() {
         return stoppable;
     }
 
     public boolean isInterruptable() {
         return interruptable;
     }
 
     public State getState() {
         return state;
     }
 
     public void addTestCaseStateChangeListener(
         TestCaseStateChangeListener changeListener) {
         if (changeListener != null) {
             changeListeners.add(changeListener);
         }
     }
 
     public void removeTestCaseStateChangeListener(
         TestCaseStateChangeListener changeListener) {
         if (changeListener != null) {
             changeListeners.remove(changeListener);
         }
     }
 
     public void run() {
         if (getState().getStateStep() < State.INITIALIZED.getStateStep()) {
             throw new IllegalStateException();
         }
         try {
             setState(State.STARTED);
             startTimeOfTest = new Date();
             extendRun();
             stopTimeOfTest = new Date();
             setState(State.FINISHED);
         }
         catch (Exception ex) {
             stopTimeOfTest = new Date();
             setState(State.STOPPED);
             ex.printStackTrace();
         }
     }
 
    protected abstract void extendRun();
 
     protected void setInterruptable(boolean interruptable) {
         this.interruptable = interruptable;
     }
 
     protected void setStoppable(boolean stoppable) {
         this.stoppable = stoppable;
     }
 
     protected void setState(State state) {
         State oldState = this.state;
         this.state = state;
         fireStateChangeEvent(oldState, state);
     }
 
     protected void fireStateChangeEvent(final State oldState,
                                         final State state) {
         TestCaseStateChangedEvent event = new TestCaseStateChangedEvent(this,
             oldState, state);
         for (TestCaseStateChangeListener listener : changeListeners) {
             listener.stateChanged(event);
         }
     }
 }
