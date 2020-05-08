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
 
 import com.smartitengineering.loadtest.engine.LoadTestEngine;
 import com.smartitengineering.loadtest.engine.TestCase;
 import com.smartitengineering.loadtest.engine.UnitTestInstance;
 import com.smartitengineering.loadtest.engine.events.LoadTestEngineStateChangeListener;
 import com.smartitengineering.loadtest.engine.events.LoadTestEngineStateChangedEvent;
 import com.smartitengineering.loadtest.engine.events.TestCaseStateChangedEvent;
 import com.smartitengineering.loadtest.engine.events.TestCaseTransitionListener;
 import com.smartitengineering.loadtest.engine.impl.management.ManagementFactory;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator;
 import com.smartitengineering.loadtest.engine.management.TestCaseThreadManager;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Implements the structural API operations and leaves the specialized ops to
  * the specific implementor.
  * @author imyousuf
  */
 public abstract class AbstractLoadTestEngineImpl
     implements LoadTestEngine {
 
     public static final String PROPS_PERMIT_KEY =
         "com.smartitengineering.loadtest.engine.loadTestEngine.concurrentBatches";
     private LoadTestEngine.State engineState;
     private Date startDate;
     private Date endDate;
     private Set<UnitTestInstance> testInstances;
     private Set<LoadTestEngineStateChangeListener> engineStateListeners;
     private Set<TestCaseTransitionListener> testCaseTransitionListeners;
     private Class<? extends TestCaseBatchCreator> batchCreatorClass;
     private TestCaseThreadManager threadManager;
     private String testName;
     private int permits;
 
     protected AbstractLoadTestEngineImpl() {
         testCaseTransitionListeners = new HashSet<TestCaseTransitionListener>();
         engineStateListeners = new HashSet<LoadTestEngineStateChangeListener>();
         initializeBeforeCreatedState();
         setState(LoadTestEngine.State.CREATED);
     }
 
     /**
      * Initialize some important members of child before status is set.
      */
     protected abstract void initializeBeforeCreatedState();
 
     public State getState() {
         return engineState;
     }
 
     public synchronized void reinstantiateCreatedState() {
         rollBackToCreatedState();
         setState(LoadTestEngine.State.CREATED);
     }
 
     /**
      * All extending children should implement this operation to reinstate its
      * created state.
      */
     protected abstract void rollBackToCreatedState();
 
     public Date getStartTime() {
         if (getState().getStateStep() < LoadTestEngine.State.STARTED.
             getStateStep()) {
             throw new IllegalStateException();
         }
         else {
             return startDate;
         }
     }
 
     public Date getEndTime() {
         if (getState().getStateStep() < LoadTestEngine.State.FINISHED.
             getStateStep()) {
             throw new IllegalStateException();
         }
         else {
             return endDate;
         }
     }
 
     public long getDuration()
         throws IllegalStateException {
         if (getState().getStateStep() < LoadTestEngine.State.FINISHED.
             getStateStep()) {
             throw new IllegalStateException();
         }
         return getEndTime().getTime() - getStartTime().getTime();
     }
 
     public void addLoadTestEngineStateChangeListener(
         LoadTestEngineStateChangeListener listener) {
         if (listener == null) {
             return;
         }
         engineStateListeners.add(listener);
     }
 
     public void removeLoadTestEngineStateChangeListener(
         LoadTestEngineStateChangeListener listener) {
         if (listener == null) {
             return;
         }
         engineStateListeners.remove(listener);
     }
 
     public void addTestCaseTransitionListener(
         TestCaseTransitionListener listener) {
         if (listener == null) {
             return;
         }
         testCaseTransitionListeners.add(listener);
     }
 
     public void removeTestCaseTransitionListener(
         TestCaseTransitionListener listener) {
         if (listener == null) {
             return;
         }
         testCaseTransitionListeners.remove(listener);
     }
 
     public void setTestCaseBatchCreator(
         Class<? extends TestCaseBatchCreator> batchCreator) {
         if (getState().getStateStep() >= LoadTestEngine.State.INITIALIZED.
             getStateStep()) {
             throw new IllegalStateException();
         }
         if (batchCreator == null) {
             return;
         }
         this.batchCreatorClass = batchCreator;
     }
 
     public void setTestCaseBatchCreator(String batchCreator)
         throws IllegalArgumentException {
         if (getState().getStateStep() >= LoadTestEngine.State.INITIALIZED.
             getStateStep()) {
             throw new IllegalStateException();
         }
         if (batchCreator == null) {
             return;
         }
         try {
            batchCreatorClass = (Class<? extends TestCaseBatchCreator>) Class.
                forName(batchCreator);
         }
         catch (Exception ex) {
             throw new IllegalArgumentException(ex);
         }
     }
 
     public Class<? extends TestCaseBatchCreator> getTestCaseBatchCreator() {
         if (batchCreatorClass == null) {
             return ManagementFactory.getDefaultBatchCreatorClass();
         }
         return batchCreatorClass;
     }
 
     public void setTestCaseThreadManager(TestCaseThreadManager threadManager) {
         if (threadManager == null) {
             return;
         }
         this.threadManager = threadManager;
     }
 
     public TestCaseThreadManager getTestCaseThreadManager() {
         if (threadManager == null) {
             threadManager = ManagementFactory.getDefaultThreadManager(-1);
         }
         return threadManager;
     }
 
     protected void setState(final LoadTestEngine.State newState) {
         if (newState == null) {
             return;
         }
         switch (newState) {
             case CREATED:
                 break;
             case INITIALIZED:
                 break;
             case STARTED:
                 startDate = new Date();
                 break;
             case FINISHED:
                 endDate = new Date();
                 break;
         }
         LoadTestEngine.State oldState = getState();
         this.engineState = newState;
         LoadTestEngineStateChangedEvent stateChangedEvent =
             new LoadTestEngineStateChangedEvent(this, oldState, newState);
         fireStateChangedEvent(stateChangedEvent);
     }
 
     protected void fireStateChangedEvent(
         final LoadTestEngineStateChangedEvent stateChangedEvent) {
         if (stateChangedEvent == null) {
             return;
         }
         for (LoadTestEngineStateChangeListener listener : engineStateListeners) {
             listener.stateChanged(stateChangedEvent);
         }
     }
 
     protected void fireTestCaseStateTransitionEvent(
         final TestCaseStateChangedEvent stateChangedEvent) {
         if (stateChangedEvent == null) {
             return;
         }
         for (TestCaseTransitionListener listener : testCaseTransitionListeners) {
             TestCase.State testCaseState = stateChangedEvent.getNewValue();
             switch (testCaseState) {
                 case INITIALIZED:
                     listener.testCaseInitialized(stateChangedEvent);
                     break;
                 case STARTED:
                     listener.testCaseStarted(stateChangedEvent);
                     break;
                 case STOPPED:
                     listener.testCaseStopped(stateChangedEvent);
                     break;
                 case FINISHED:
                     listener.testCaseFinished(stateChangedEvent);
                     return;
                 default:
                     break;
             }
         }
     }
 
     protected TestCaseBatchCreator getTestCaseBatchCreatorInstance()
         throws InstantiationException,
                IllegalAccessException {
         if (batchCreatorClass == null) {
             return ManagementFactory.getDefaultBatchCreator();
         }
         return batchCreatorClass.newInstance();
     }
 
     protected String getTestName() {
         return testName;
     }
 
     protected void setTestName(String testName) {
         this.testName = testName;
     }
 
     protected void setTestInstances(Set<UnitTestInstance> testInstances) {
         if (testInstances != null) {
             this.testInstances = testInstances;
         }
     }
 
     protected Set<UnitTestInstance> getTestInstances() {
         return this.testInstances;
     }
 
     public int getPermits() {
         if (permits < 1) {
             permits = 1;
         }
         return permits;
     }
 
     public void setPermits(int permits) {
         this.permits = permits;
     }
 
     protected Set<LoadTestEngineStateChangeListener> getEngineStateListeners() {
         return engineStateListeners;
     }
 
     protected Set<TestCaseTransitionListener> getTestCaseTransitionListeners() {
         return testCaseTransitionListeners;
     }
 }
