 /**
  * Copyright (C) 2011 Shaun Johnson, LMXM LLC
  *
  * This file is part of Universal Task Executer.
  *
  * Universal Task Executer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * Universal Task Executer is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * Universal Task Executer. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.lmxm.ute;
 
 import net.lmxm.ute.beans.Preference;
 import net.lmxm.ute.beans.configuration.ApplicationPreferences;
 import net.lmxm.ute.beans.configuration.Configuration;
 import net.lmxm.ute.beans.jobs.Job;
 import net.lmxm.ute.beans.jobs.SingleTaskJob;
 import net.lmxm.ute.beans.tasks.Task;
 import net.lmxm.ute.configuration.ConfigurationInterpolator;
 import net.lmxm.ute.configuration.ConfigurationUtils;
 import net.lmxm.ute.event.JobStatusListener;
 import net.lmxm.ute.event.StatusChangeListener;
 import net.lmxm.ute.executers.jobs.JobExecuter;
 import net.lmxm.ute.executers.jobs.JobExecuterFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 public abstract class GenericApplication {
 
     /**
      * The Constant LOGGER.
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(GenericApplication.class);
 
     public abstract void execute();
 
     /**
      * Execute jobs.
      *
      * @param jobs                 the jobs
      * @param configuration        the configuration
      * @param jobStatusListener    Job Status listener to use during job execution
      * @param statusChangeListener Status change listener to use during job execution
      */
     protected final void executeJobs(final List<Job> jobs, final Configuration configuration,
             JobStatusListener jobStatusListener, StatusChangeListener statusChangeListener) {
         final String prefix = "executeJobs() :";
 
         LOGGER.debug("{} entered", prefix);
 
         // Execute jobs
         for (final Job job : jobs) {
             LOGGER.debug("{} executing job {}", prefix, job.getId());
 
             final Job jobInterpolated = ConfigurationInterpolator.interpolateJobValues(job, configuration);
             job.removeEmptyObjects();
 
             final JobExecuter jobExecuter = JobExecuterFactory.create(jobInterpolated, configuration);
             jobExecuter.addJobStatusListener(jobStatusListener);
             jobExecuter.addStatusChangeListener(statusChangeListener);
             jobExecuter.execute();
         }
 
         LOGGER.debug("{} leaving", prefix);
     }
 
     /**
      * Load and validate preferences are set.
      *
      * @param configuration the configuration
      * @param preferences   Preference values that override preferences loaded from disk.
      */
     protected final void loadAndValidatePreferencesAreSet(final Configuration configuration, List<Preference> preferences) {
         final String prefix = "loadAndValidatePreferencesAreSet() :";
 
         LOGGER.debug("{} entered", prefix);
 
         final ApplicationPreferences applicationPreferences = new ApplicationPreferences(
                 configuration.getConfigurationFile());
         applicationPreferences.loadPreferenceValues(configuration.getPreferences());
 
         overridePreferences(configuration.getPreferences(), preferences);
 
         if (applicationPreferences.hasAllPreferences(configuration.getPreferences())) {
             LOGGER.debug("{} all preferences have values", prefix);
         }
         else {
             LOGGER.error("{} at least one preference does not have a value", prefix);
             throw new RuntimeException("Preferences must be assigned values before continuing");
         }
 
         LOGGER.debug("{} leaving", prefix);
     }
 
     /**
      * Override preference values in the original list with values from the override list. This method modifies the
      * values in the original preferences list. Override preferences that do not match any existing preferences in the
      * original list are ignored.
      *
      * @param preferencesToBeUpdated List of Preference objects to be updated
      * @param overridePreferences    List of Preference objects used to override the original Preference values
      */
     private void overridePreferences(List<Preference> preferencesToBeUpdated, List<Preference> overridePreferences) {
        if (overridePreferences == null) {
            return;
        }
        
         for (Preference originalPreference : preferencesToBeUpdated) {
             for (Preference overridePreference : overridePreferences) {
                 if (originalPreference.getId().equals(overridePreference.getId())) {
                     originalPreference.setValue(overridePreference.getValue());
                 }
             }
         }
     }
 
     /**
      * Load jobs.
      *
      * @param configuration the configuration
      * @param jobId         ID of the job to load
      * @param taskId        ID of task to load
      * @return List of loaded Jobs
      */
     protected final List<Job> loadJobs(final Configuration configuration, String jobId, String taskId) {
         final String prefix = "loadJobs() :";
 
         LOGGER.debug("{} entered", prefix);
 
         final List<Job> jobs = new ArrayList<Job>();
 
         final Job job = ConfigurationUtils.findJobById(configuration, jobId);
         if (job == null) {
             LOGGER.error("{} job with id \"{}\" does not exist", prefix, jobId);
             throw new RuntimeException("Job with id \"" + jobId + "\" does not exist");
         }
 
         if (taskId == null) {
             jobs.add(job);
         }
         else {
             SingleTaskJob singleTaskJob = null;
 
             for (final Task task : job.getTasks()) {
                 if (task.getId().equals(taskId)) {
                     singleTaskJob = new SingleTaskJob(task);
                     break;
                 }
             }
 
             if (singleTaskJob == null) {
                 LOGGER.error("{} task with id \"{}\" does not exist", prefix, taskId);
                 throw new RuntimeException("Task with id \"" + taskId + "\" does not exist");
             }
 
             jobs.add(singleTaskJob);
         }
 
         LOGGER.debug("{} returning", prefix);
 
         return jobs;
     }
 }
