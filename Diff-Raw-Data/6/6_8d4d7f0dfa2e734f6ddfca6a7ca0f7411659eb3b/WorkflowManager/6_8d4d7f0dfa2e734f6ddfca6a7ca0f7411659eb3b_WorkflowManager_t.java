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
 
 package org.bitrepository.service.workflow;
 
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.service.scheduler.JobEventListener;
 import org.bitrepository.service.scheduler.JobScheduler;
 import org.bitrepository.settings.referencesettings.Schedule;
 import org.bitrepository.settings.referencesettings.WorkflowConfiguration;
 import org.bitrepository.settings.referencesettings.WorkflowSettings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 public abstract class WorkflowManager {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     private final JobScheduler scheduler;
     private final WorkflowContext context;
     private final Map<JobID, Workflow> workflows = new HashMap<JobID, Workflow>();
     private final Map<String, List<JobID>> collectionWorkflows = new HashMap<String, List<JobID>>();
     private final Map<JobID, List<WorkflowStatistic>> statistics = new HashMap<JobID, List<WorkflowStatistic>>();
     public static final int MAX_NUMBER_OF_STATISTISCS_FOR_A_WORKFLOW = 1;
 
     public WorkflowManager(
             WorkflowContext context,
             WorkflowSettings configuration,
             JobScheduler scheduler) {
         this.context = context;
         this.scheduler = scheduler;
         loadWorkFlows(configuration);
         scheduler.addJobEventListener(new WorkflowEventListener());
     }
 
     public String startWorkflow(JobID jobID) {
         return scheduler.startJob(getWorkflow(jobID));
     }
 
     public List<JobID> getWorkflows(String collectionID) {
         return collectionWorkflows.get(collectionID);
     }
 
     public Workflow getWorkflow(JobID workflowID) {
         if (workflows.containsKey(workflowID)) {
             return workflows.get(workflowID);
         } else {
             throw new IllegalArgumentException("Unknown workflow " + workflowID);
         }
     }
 
     public WorkflowStatistic getCurrentStatistics(JobID jobID) {
         return getWorkflow(jobID).getWorkflowStatistics();
     }
 
     public WorkflowStatistic getLastCompleteStatistics(JobID workflowID) {
         getWorkflow(workflowID);
         if (statistics.containsKey(workflowID)) {
            List<WorkflowStatistic> stats = statistics.get(workflowID);
            return stats.get(stats.size()-1);
         } else return null;
     }
 
     public Date getNextScheduledRun(JobID jobID) {
         Date nextRun = scheduler.getNextRun(jobID);
         if (nextRun == null) {
             if (workflows.containsKey(jobID)) { // Unscheduled job
                 return null;
             } else {
                 throw new IllegalArgumentException("Unknown workflow " + jobID);
             }
         }
         return nextRun;
     }
 
     public long getRunInterval(JobID jobID) {
         long interval = scheduler.getRunInterval(jobID);
         if (interval == -1) {
             if (workflows.containsKey(jobID)) { // Unscheduled job
                 return -1;
             } else {
                 throw new IllegalArgumentException("Unknown workflow " + jobID);
             }
         }
         return interval;
     }
 
     private void loadWorkFlows(WorkflowSettings configuration) {
         for (WorkflowConfiguration workflowConf:configuration.getWorkflow()) {
             log.info("Scheduling from configuration: " + workflowConf);
             List<String> unscheduledCollections = new LinkedList<String>(SettingsUtils.getAllCollectionsIDs());
             try {
                 if (workflowConf.getSchedules() != null) {
                     for (Schedule schedule:workflowConf.getSchedules().getSchedule()) {
                         List<String> collectionsToScheduleWorkflowFor;
                         if (schedule.isSetCollections()) {
                             collectionsToScheduleWorkflowFor = schedule.getCollections().getCollectionID();
                         } else {
                             collectionsToScheduleWorkflowFor = SettingsUtils.getAllCollectionsIDs();
                         }
                         for (String collectionID:collectionsToScheduleWorkflowFor) {
                             Workflow workflow =
                                     (Workflow)lookupClass(workflowConf.getWorkflowClass()).newInstance();
                             workflow.initialise(context, collectionID);
                             scheduler.schedule(workflow, schedule.getWorkflowInterval());
                             addWorkflow(collectionID, workflow);
                             unscheduledCollections.remove(collectionID);
                         }
                     }
                 }
                 // Create a instance of all workflows not explicitly scheduled.
                 for (String collectionID:unscheduledCollections) {
                     Workflow workflow =
                             (Workflow)Class.forName(workflowConf.getWorkflowClass()).newInstance();
                     workflow.initialise(context, collectionID);
                     addWorkflow(collectionID, workflow);
                 }
             } catch (Exception e) {
                 log.error("Unable to load workflow " + workflowConf.getWorkflowClass(), e);
             }
         }
     }
 
     private Class lookupClass(String settingsDefinedClass) throws ClassNotFoundException {
         String fullClassName;
         if (settingsDefinedClass.indexOf('.') == -1) {
             fullClassName = getDefaultWorkflowPackage() + "." + settingsDefinedClass;
         } else {
             fullClassName = settingsDefinedClass;
         }
         return Class.forName(fullClassName);
     }
 
     private void addWorkflow(String collectionID, Workflow workflow) {
         workflows.put(workflow.getJobID(), workflow);
         if (!collectionWorkflows.containsKey(collectionID)) {
             collectionWorkflows.put(collectionID, new LinkedList<JobID>());
         }
         collectionWorkflows.get(collectionID).add(workflow.getJobID());
     }
 
     /**
      * Allows subclasses to define a workflow package where workflow classes defined with a simplename in the settings
      * will be prefixed with the namespace defined here.
      */
     protected abstract String getDefaultWorkflowPackage();
 
     /**
      * Stores workflow statistics when a workflow has finished.
      */
     public class WorkflowEventListener implements JobEventListener {
         @Override
         public void jobStarted(SchedulableJob job) {}
 
         @Override
         public void jobFailed(SchedulableJob job) {}
 
         /**
          * Adds the workflow statistics to the statistics list for this workflow. Will also remove older statistisics
          * if the number of statistics exceeds <code>MAX_NUMBER_OF_STATISTISCS_FOR_A_WORKFLOW</code>.
          * @param job
          */
         @Override
         public void jobFinished(SchedulableJob job) {
             if (workflows.containsKey(job.getJobID())) { // One of mine
                 if (!statistics.containsKey(job.getJobID())) {
                     statistics.put(job.getJobID(), new LinkedList<WorkflowStatistic>());
                 }
 
                 List<WorkflowStatistic> workflowStatistics = statistics.get(job.getJobID());
                 workflowStatistics.add((((Workflow)job).getWorkflowStatistics()));
                 if (workflowStatistics.size() > MAX_NUMBER_OF_STATISTISCS_FOR_A_WORKFLOW) {
                    workflowStatistics.remove(0);
                 }
             }
         }
     }
 }
