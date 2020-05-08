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
 package org.bitrepository.integrityservice;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.integrityservice.cache.IntegrityModel;
 import org.bitrepository.integrityservice.mocks.MockChecker;
 import org.bitrepository.integrityservice.mocks.MockCollector;
 import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
 import org.bitrepository.integrityservice.mocks.MockWorkflow;
 import org.bitrepository.integrityservice.workflow.scheduler.CollectAllChecksumsWorkflow;
 import org.bitrepository.integrityservice.workflow.scheduler.CollectAllFileIDsWorkflow;
 import org.bitrepository.integrityservice.workflow.scheduler.CollectObsoleteChecksumsWorkflow;
 import org.bitrepository.integrityservice.workflow.scheduler.IntegrityValidatorWorkflow;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * Test that triggering works.
  */
 public class WorkflowTest extends ExtendedTestCase {
     Settings settings;
     
     @BeforeClass (alwaysRun = true)
     public void setup() {
         settings = TestSettingsProvider.reloadSettings();
     }
     
     @Test(groups = {"regressiontest", "integritytest"})
     public void testIntervalWorkflow() {
         addDescription("Test the basic functionallity of the workflows.");
         addStep("Setup variables and workflow.", "No errors");
         String name = "mock-1";
         long interval = 3600000L; 
         MockWorkflow workflow = new MockWorkflow(interval, name);
         
         addStep("Validate the interface to the workflow.", "Should contain the given variables and none of the functions should have been called.");
         Assert.assertEquals(workflow.getCallsForNextRun(), 0, "Did not expect any calls for NextRun yet.");
        Assert.assertEquals(workflow.getCallsForRunWorkflow(), 0, "Did not expect any calls for trigger yet.");
         Assert.assertEquals(workflow.getTimeBetweenRuns(), interval);
         Assert.assertEquals(workflow.getName(), name);
         
         addStep("Retrieve the date for next run and then trigger it.", 
                 "Should be counted and the workflow should get a new date for the next run.");
         Date nextRun = workflow.getNextRun();
         workflow.trigger();
         Assert.assertEquals(workflow.getCallsForNextRun(), 1, "Should have called for NextRun once");
        Assert.assertEquals(workflow.getCallsForRunWorkflow(), 1, "Should have triggered the workflow once");
         
         Assert.assertTrue( nextRun != workflow.getNextRun(), "The dates should differ");
         Assert.assertTrue(nextRun.getTime() < workflow.getNextRun().getTime(), 
                 "The initial date for 'NextRun' should be prior to the current.");
     }
     
     @Test(groups = {"regressiontest", "integritytest"})
     public void testCollectAllChecksumWorkflow() {
         addDescription("Testing that the CollecAllChecksumWorkflow calls the 'getChecksums' function on the collector.");
         addStep("Setup variables", "No errors.");
         MockCollector collector = new MockCollector();
         String workflowName = "CollectAllChecksumWorkflowTest";
         ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
         long interval = 25000L;
         CollectAllChecksumsWorkflow workflow = new CollectAllChecksumsWorkflow(interval, workflowName, 
                 csType, settings, collector, null);
         
         addStep("Validate initial position.", "Should be no calls for any method yet.");
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0, "No calls for GetChecksum shuld have been made");
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0, "No calls for GetFileIDs shuld have been made");
         
         addStep("Trigger the workflow", "Should make a call for GetChecksums, but none for GetFileIDs");
         workflow.trigger();
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1, "One call for GetChecksums should have been made");
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0, "No calls for GetFileIDs shuld have been made");
 
         addStep("Trigger the workflow 4 times more", "Should have made 5 call for GetChecksums, but none for GetFileIDs");
         workflow.trigger();
         workflow.trigger();
         workflow.trigger();
         workflow.trigger();
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 5, "5 calls for GetChecksums should have been made");
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0, "No calls for GetFileIDs shuld have been made");
     }
     
     @Test(groups = {"regressiontest", "integritytest"})
     public void testCollectAllFileIDsWorkflow() {
         addDescription("Testing that the CollecAllFileIDsWorkflow calls the 'getFileIDs' function on the collector.");
         addStep("Setup variables", "No errors.");
         MockCollector collector = new MockCollector();
         String workflowName = "CollectAllFileIDsWorkflowTest";
         long interval = 25000L;
         CollectAllFileIDsWorkflow workflow = new CollectAllFileIDsWorkflow(interval, workflowName, settings, collector, null);
         
         addStep("Validate initial position.", "Should be no calls for any method yet.");
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0, "No calls for GetChecksum shuld have been made");
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0, "No calls for GetFileIDs shuld have been made");
         
         addStep("Trigger the workflow", "Should make a call for GetFileIDs, but none for GetChecksums");
         workflow.trigger();
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0, "No calls for GetChecksum expected.");
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1, "One call for GetFileIDs expected.");
 
         addStep("Trigger the workflow 4 times more", "Should have made 5 call for GetFileIDs, but none for GetChecksums");
         workflow.trigger();
         workflow.trigger();
         workflow.trigger();
         workflow.trigger();
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0, "No calls for GetChecksums expected");
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 5, "5 calls for GetFileIDs expected");
     }
 
     @Test(groups = {"regressiontest", "integritytest"})
     public void testIntegrityValidatorWorkflow() {
         addDescription("Tests the IntegrityValidatorWorkflow");
         addStep("Setup the variables.", "No errors.");
         long interval = 3600000;
         String name = "IntegrityValidatorWorkflowTest";
         MockChecker checker = new MockChecker();
         IntegrityValidatorWorkflow workflow = new IntegrityValidatorWorkflow(interval, name, checker);
         
         addStep("Validate the initial position.", "No call for the checker yet.");
         Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "No calls for CheckChecksums expected");
         Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "No calls for CheckFileIDs expected");
         
         addStep("Trigger the workflow", "One call both for the CheckChecksums and ChecksFileIDs expected");
         workflow.trigger();
         Assert.assertEquals(checker.getCallsForCheckChecksums(), 1, "One call for CheckChecksums expected");
         Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1, "One call for CheckFileIDs expected");
 
         addStep("Trigger the workflow four more times.", "The CheckChecksums and ChecksFileIDs should have been called 5 times.");
         workflow.trigger();
         workflow.trigger();
         workflow.trigger();
         workflow.trigger();
         Assert.assertEquals(checker.getCallsForCheckChecksums(), 5, "Five calls for CheckChecksums expected");
         Assert.assertEquals(checker.getCallsForCheckFileIDs(), 5, "Five calls for CheckFileIDs expected");
     }
 
     @Test(groups = {"regressiontest", "integritytest"})
     public void testCollectObsoleteChecksumsWorkflow() {
         addDescription("Tests the CollectObsoleteChecksumWorkflow");
         addStep("Setup the varibles.", "No errors");
         long interval = 1L;
         String name = "CollectObsoleteChecksumsWorkflowTest";
         String checksum = "CHECKSUM";
         String fileid1 = "FILE-1";
         String pillarId = "PillarId";
         long obsoleteInterval = 3600000L; // set for one hour
         MockCollector collector = new MockCollector();
         IntegrityModel cache = new TestIntegrityModel();
         MockIntegrityModel mockCache = new MockIntegrityModel(cache);
         CollectObsoleteChecksumsWorkflow workflow = new CollectObsoleteChecksumsWorkflow(interval, name, 
                 obsoleteInterval, new ChecksumSpecTYPE(), collector, mockCache, null);
         
         addStep("Validate the initial position", "No calls for either collector or cache");
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0);
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0);
         Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0);
         Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0);
         Assert.assertEquals(mockCache.getCallsForGetAllFileIDs(), 0);
         Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 0);
         Assert.assertEquals(mockCache.getCallsForGetNumberOfChecksumErrors(), 0);
         Assert.assertEquals(mockCache.getCallsForGetNumberOfFiles(), 0);
         Assert.assertEquals(mockCache.getCallsForGetNumberOfMissingFiles(), 0);
         Assert.assertEquals(mockCache.getCallsForSetChecksumAgreement(), 0);
         Assert.assertEquals(mockCache.getCallsForSetChecksumError(), 0);
         Assert.assertEquals(mockCache.getCallsForSetFileMissing(), 0);
         
         addStep("Trigger the workflow", "Should call the cache, but not the collector, since no data exists.");
         workflow.trigger();
         Assert.assertEquals(mockCache.getCallsForGetAllFileIDs(), 1);
         Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 0);
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0);
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0);
         
         addStep("Add one file with a new checksum to the cache. Then trigger the workflow.", 
                 "Should only call the cache.");
         
         ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
         csData.setCalculationTimestamp(CalendarUtils.getNow());
         csData.setChecksumValue(checksum.getBytes());
         csData.setFileID(fileid1);
         List<ChecksumDataForChecksumSpecTYPE> csDataList = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
         csDataList.add(csData);
         
         ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
         csType.setChecksumType(ChecksumType.MD5);
         
         cache.addChecksums(csDataList, csType, pillarId);
         
         workflow.trigger();
         
         Assert.assertEquals(mockCache.getCallsForGetAllFileIDs(), 2, "One at this trigger and one for the previous trigger");
         Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 1);
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 0);
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0);
         
         addStep("Add another file, but with an old checksum to the cache. Then trigger the workflow.",
                 "Should call both the cache and the collector.");
         csData = new ChecksumDataForChecksumSpecTYPE();
         csData.setCalculationTimestamp(CalendarUtils.getEpoch());
         csData.setChecksumValue(checksum.getBytes());
         csData.setFileID(fileid1);
         csDataList = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
         csDataList.add(csData);
         
         cache.addChecksums(csDataList, csType, pillarId);
         workflow.trigger();
         
         Assert.assertEquals(mockCache.getCallsForGetAllFileIDs(), 3, "One for each trigger");
         Assert.assertEquals(mockCache.getCallsForGetFileInfos(), 2);
         Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
         Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 0);
     }    
 
     @Test(groups = "specificationonly")
     void testTriggerOnOldData() throws Exception {
         addDescription("Test that triggering on old data works");
         // TODO old stuff from Kåre. Might be a usecase.
         addStep("Set up the system", "No errors");
         addStep("Test that the trigger triggers when data is old", "Trigger responds with true");
         addStep("Test that event is to collect information", "Trigger calls information collection methods");
     }
 
     @Test(groups = "specificationonly")
     void testTriggerOfRandomSet() throws Exception {
         addDescription("Test that a trigger that should generate collection on random files does so.");
         // TODO old stuff from Kåre. Might be a usecase.
         addStep("Set up a trigger that generates collection of checksums on three random files", "No errors");
         addStep("Pull the trigger 10 times", "Trigger generates collection event for three random files");
     }
 }
