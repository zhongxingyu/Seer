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
 import com.smartitengineering.loadtest.engine.UnitTestInstance;
 import com.smartitengineering.loadtest.engine.events.BatchEvent;
 import com.smartitengineering.loadtest.engine.events.TestCaseBatchListener;
 import com.smartitengineering.loadtest.engine.events.TestCaseStateChangeListener;
 import com.smartitengineering.loadtest.engine.events.TestCaseStateChangedEvent;
 import com.smartitengineering.loadtest.engine.events.TestCaseTransitionListener;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator.Batch;
 import com.smartitengineering.loadtest.engine.result.TestCaseInstanceResult;
 import com.smartitengineering.loadtest.engine.result.TestCaseResult;
 import com.smartitengineering.loadtest.engine.result.TestProperty;
 import com.smartitengineering.loadtest.engine.result.TestResult;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Semaphore;
 
 /**
  * Default implementation of the LoadTestEngine
  *
  */
 public class LoadTestEngineImpl
     extends AbstractLoadTestEngineImpl {
 
     protected EngineBatchListener engineBatchListener;
     private Map<TestCaseBatchCreator, UnitTestInstance> creators;
     private Map<UnitTestInstance, UnitTestInstanceRecord> instances;
     private Map<TestCase, UnitTestInstanceRecord> caseRecords;
     private Semaphore semaphore;
     private EngineJobFinishedDetector finishedDetector;
     private ExecutorService executorService;
     private TestCaseTransitionListener transitionListener;
     private TestCaseStateChangeListener caseStateChangeListener;
     private TestResult result;
 
     @Override
     protected void initializeBeforeCreatedState() {
         setTestInstances(new HashSet<UnitTestInstance>());
         creators = new HashMap<TestCaseBatchCreator, UnitTestInstance>();
         instances = new HashMap<UnitTestInstance, UnitTestInstanceRecord>();
     }
 
     public void init(String testName,
                      Set<UnitTestInstance> testInstances,
                      Properties initProperties)
         throws IllegalArgumentException,
                IllegalStateException {
         if (getState().getStateStep() != State.CREATED.getStateStep()) {
             throw new IllegalStateException();
         }
         if (testName == null || testName.length() <= 0) {
             throw new IllegalArgumentException();
         }
         if (testInstances == null || testInstances.isEmpty()) {
             throw new IllegalArgumentException();
         }
 
         if (initProperties != null && !initProperties.isEmpty()) {
             if (initProperties.containsKey(PROPS_PERMIT_KEY)) {
                 try {
                     setPermits(Integer.parseInt(initProperties.getProperty(
                         PROPS_PERMIT_KEY)));
                 }
                 catch (Exception ex) {
                     ex.printStackTrace();
                     setPermits(-1);
                 }
             }
         }
 
         engineBatchListener = new EngineBatchListener();
         semaphore = new Semaphore(getPermits());
         setTestName(testName);
 
         getTestInstances().addAll(testInstances);
         for (UnitTestInstance instance : getTestInstances()) {
             try {
                 TestCaseBatchCreator creator = getTestCaseBatchCreatorInstance();
                 creator.init(instance);
                 creator.addBatchCreatorListener(engineBatchListener);
                 creators.put(creator, instance);
             }
             catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
         transitionListener = new TestCaseStateTransitionMonitor();
         addTestCaseTransitionListener(transitionListener);
         caseStateChangeListener = new TestCaseStateListenerImpl();
         caseRecords = new HashMap<TestCase, UnitTestInstanceRecord>();
 
         finishedDetector = new EngineJobFinishedDetector();
         executorService = Executors.newSingleThreadExecutor();
 
         result = new TestResult();
         result.setTestName(testName);
         HashSet<TestCaseResult> resultSet = new HashSet<TestCaseResult>();
         result.setTestCaseRunResults(resultSet);
         for (UnitTestInstance instance : testInstances) {
             TestCaseResult caseResult = new TestCaseResult();
             caseResult.setName(instance.getName());
             caseResult.setInstanceFactoryClassName(instance.
                 getInstanceFactoryClassName());
             final Properties testProperties = instance.getProperties();
             Set<TestProperty> testPropertys = new HashSet<TestProperty>(
                 testProperties.size());
             final Iterator<Object> keySetIterator = testProperties.keySet().
                 iterator();
             while (keySetIterator.hasNext()) {
                 TestProperty property = new TestProperty();
                 final String key = keySetIterator.next().toString();
                 property.setKey(key);
                 property.setValue(testProperties.getProperty(key));
                 testPropertys.add(property);
             }
             caseResult.setTestProperties(testPropertys);
             caseResult.setTestCaseInstanceResults(
                 new HashSet<TestCaseInstanceResult>());
             UnitTestInstanceRecord record = new UnitTestInstanceRecord(
                 caseResult);
             instances.put(instance, record);
         }
 
         setState(State.INITIALIZED);
     }
 
     public void start()
         throws IllegalStateException {
         if (getState().getStateStep() != State.INITIALIZED.getStateStep()) {
             throw new IllegalStateException();
         }
        result.setStartDateTime(new Date());
         getTestCaseThreadManager().startManager();
         for (Map.Entry<TestCaseBatchCreator, UnitTestInstance> creator : creators.
             entrySet()) {
             creator.getKey().start();
         }
         setState(State.STARTED);
     }
 
     public TestResult getTestResult()
         throws IllegalStateException {
         if (getState().getStateStep() < State.FINISHED.getStateStep()) {
             throw new IllegalStateException();
         }
         if (result.isValid()) {
             return result;
         }
         else {
             throw new IllegalStateException("Test result in invalid state");
         }
     }
 
     @Override
     protected void rollBackToCreatedState() {
         getTestInstances().clear();
         setTestName(null);
         semaphore = null;
         engineBatchListener = null;
         finishedDetector = null;
     }
 
     protected class EngineBatchListener
         implements TestCaseBatchListener {
 
         public void batchAvailable(BatchEvent event) {
             boolean acquired = false;
             try {
                 semaphore.acquire();
                 acquired = true;
                 Batch batch;
                 Map.Entry<ThreadGroup, Map<Thread, TestCase>> batchThreads;
                 batch = event.getBatch();
                 if (batch != null) {
                     batchThreads = batch.getBatch();
                     for (Map.Entry<Thread, TestCase> thread : batchThreads.
                         getValue().entrySet()) {
                         if (thread.getKey() != null && thread.getValue() != null) {
                             thread.getKey().start();
                             thread.getValue().addTestCaseStateChangeListener(
                                 caseStateChangeListener);
                             getTestCaseThreadManager().manageThread(thread.
                                 getKey(),
                                 thread.getValue());
                             final TestCaseBatchCreator batchCreator =
                                 batch.getBatchCreator();
                             if (batchCreator != null) {
                                 final UnitTestInstance testInstance =
                                     creators.get(batchCreator);
                                 if (testInstance != null) {
                                     final UnitTestInstanceRecord instanceRecord =
                                         instances.get(testInstance);
                                     if (instanceRecord != null) {
                                         instanceRecord.incrementCount();
                                         caseRecords.put(thread.getValue(),
                                             instanceRecord);
                                     }
                                 }
                             }
                         }
                     }
                     batch.setBatchStarted();
                 }
             }
             catch (Exception ex) {
                 ex.printStackTrace();
             }
             finally {
                 if (acquired) {
                     semaphore.release();
                     acquired = false;
                 }
             }
         }
 
         public void batchCreationEnded(BatchEvent event) {
             if (event.getBatch() != null &&
                 event.getBatch().getBatchCreator() != null) {
                 UnitTestInstance testInstance = creators.get(event.getBatch().
                     getBatchCreator());
                 if (testInstance != null) {
                     UnitTestInstanceRecord record = instances.get(testInstance);
                     record.setIntanceFinished();
                     executorService.submit(finishedDetector);
                 }
             }
         }
     }
 
     protected class TestCaseStateTransitionMonitor
         implements TestCaseTransitionListener {
 
         public void testCaseInitialized(TestCaseStateChangedEvent event) {
         }
 
         public void testCaseStarted(TestCaseStateChangedEvent event) {
         }
 
         public void testCaseFinished(TestCaseStateChangedEvent event) {
             testCaseIsDone(event);
         }
 
         public void testCaseStopped(TestCaseStateChangedEvent event) {
             testCaseIsDone(event);
         }
 
         private void testCaseIsDone(TestCaseStateChangedEvent event) {
             UnitTestInstanceRecord record =
                 caseRecords.remove(event.getSource());
             if (record != null) {
                 TestCase testCase = event.getSource();
                 TestCaseInstanceResult instanceResult = new TestCaseInstanceResult();
                 instanceResult.setStartTime(testCase.getStartTimeOfTest());
                 instanceResult.setEndTime(testCase.getEndTimeOfTest());
                 instanceResult.setEndTestCaseState(testCase.getState());
                 instanceResult.setInstanceNumber(record.getTestCaseCount());
                 record.getTestCaseResult().getTestCaseInstanceResults().add(
                     instanceResult);
                 record.decrementCount();
                 if (record.hasUnitTestInstanceFinished()) {
                     executorService.submit(finishedDetector);
                 }
             }
         }
     }
 
     protected class TestCaseStateListenerImpl
         implements TestCaseStateChangeListener {
 
         public void stateChanged(TestCaseStateChangedEvent stateChangeEvent) {
             fireTestCaseStateTransitionEvent(stateChangeEvent);
         }
     }
 
     protected class UnitTestInstanceRecord {
 
         private int testCaseCount;
         private TestCaseResult testCaseResult;
         private boolean instanceFinished;
 
         public UnitTestInstanceRecord(TestCaseResult result) {
             if (result == null) {
                 throw new IllegalArgumentException();
             }
             testCaseCount = 0;
             testCaseResult = result;
             instanceFinished = true;
         }
 
         public synchronized void incrementCount() {
             testCaseCount++;
         }
 
         public synchronized void decrementCount() {
             testCaseCount--;
         }
 
         public int getTestCaseCount() {
             return testCaseCount;
         }
 
         public boolean isInstanceFinished() {
             return instanceFinished;
         }
 
         public synchronized void setIntanceFinished() {
             instanceFinished = true;
         }
 
         public TestCaseResult getTestCaseResult() {
             return testCaseResult;
         }
 
         public boolean hasUnitTestInstanceFinished() {
             return isInstanceFinished() && getTestCaseCount() <= 0;
         }
     }
 
     protected class EngineJobFinishedDetector
         implements Runnable {
 
         public void run() {
             ArrayList<UnitTestInstance> toBeDeletedInstances =
                 new ArrayList<UnitTestInstance>();
             for (Map.Entry<UnitTestInstance, UnitTestInstanceRecord> instanceRecord : instances.
                 entrySet()) {
                 if (instanceRecord.getValue() != null && instanceRecord.getValue().
                     hasUnitTestInstanceFinished()) {
                     toBeDeletedInstances.add(instanceRecord.getKey());
                 }
             }
             synchronized (instances) {
                 for (UnitTestInstance instance : toBeDeletedInstances) {
                     instances.remove(instance);
                 }
             }
             if (instances.isEmpty()) {
                result.setEndDateTime(new Date());
                 setState(State.FINISHED);
             }
         }
     }
 }
