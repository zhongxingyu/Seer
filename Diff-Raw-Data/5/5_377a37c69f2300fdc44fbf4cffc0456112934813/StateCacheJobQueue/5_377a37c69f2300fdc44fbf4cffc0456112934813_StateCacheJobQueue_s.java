 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  *     IBM Corporation - concepts and ideas from Eclipse
  *******************************************************************************/
 
 package net.sourceforge.eclipseccase;
 
 import org.apache.commons.collections.buffer.PriorityBuffer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 
 /**
  * The queue for refreshe state jobs.
  * 
  * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
  */
 class StateCacheJobQueue extends Job {
 
     /** the name of this job */
     private static final String MESSAGE_QUEUE_NAME = Messages
             .getString("StateCacheJobQueue.jobLabel"); //$NON-NLS-1$
 
     /** the default delay */
     private static final int DEFAULT_DELAY = 100;
 
     /** the priority buffer */
     private PriorityBuffer priorityQueue;
 
     /**
      * Creates a new instance.
      * 
      * @param name
      */
     StateCacheJobQueue() {
         super(MESSAGE_QUEUE_NAME);
 
         // create underlying priority queue
         this.priorityQueue = new PriorityBuffer(80, false);
 
         // execute as system job if hidden
         setSystem(ClearcasePlugin.isHideRefreshActivity());
 
         // set priority for long running jobs
         setPriority(DECORATE);
 
         // set the rule to the clearcase engine
         setRule(ClearcasePlugin.RULE_CLEARCASE_REFRESH);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
      */
     protected IStatus run(IProgressMonitor monitor) {
         //synchronized in case build starts during checkCancel
         synchronized (this) {
             if (monitor.isCanceled()) return Status.CANCEL_STATUS;
         }
         try {
             executePendingJobs(monitor);
             return Status.OK_STATUS;
         } catch (OperationCanceledException e) {
             return Status.CANCEL_STATUS;
         } catch (CoreException sig) {
             return sig.getStatus();
         }
     }
 
     /**
      * Executes all pending jobs
      * 
      * @param monitor
      * @throws CoreException
      * @throws OperationCanceledException
      */
     private void executePendingJobs(IProgressMonitor monitor)
             throws CoreException, OperationCanceledException {
 
         try {
             monitor.beginTask(MESSAGE_QUEUE_NAME, priorityQueue.size());
 
             while (!priorityQueue.isEmpty()) {
 
                 if (monitor.isCanceled())
                         throw new OperationCanceledException();
 
                 StateCacheJob job = null;
 
                 // synchonize on the buffer but execute job outside lock
                 synchronized (priorityQueue) {
                     if (!priorityQueue.isEmpty())
                             job = (StateCacheJob) priorityQueue.remove();
                 }
 
                 // check if buffer was empty
                 if (null == job) break;
 
                 // execute job
                 if (null != job.getStateCache().getResource()) {
                     monitor.subTask(Messages
                             .getString("StateCacheJobQueue.task.refresh") //$NON-NLS-1$
                             + job.getStateCache().getResource().getFullPath());
                     job.execute(new SubProgressMonitor(monitor, 1));
                 }
             }
         } finally {
             monitor.done();
         }
     }
 
     /**
      * Schedules the specified job.
      * <p>
      * Only high priority jobs will be rescheduled if the same job is already
      * scheduled.
      * </p>
      * 
      * @param job
      *            the job to schedule
      */
     public void schedule(StateCacheJob job) {
         schedule(new StateCacheJob[] { job });
     }
 
     /**
      * Schedules the specified jobs.
      * <p>
      * Only high priority jobs will be rescheduled if the same job is already
      * scheduled.
      * </p>
      * 
      * @param job
      *            the job to schedule
      */
     public void schedule(StateCacheJob[] jobs) {
 
         // interrupt ongoing refreshes
         cancel();
 
         // synchronize on the buffer
         synchronized (priorityQueue) {
 
             for (int i = 0; i < jobs.length; i++) {
                 StateCacheJob job = jobs[i];
                 if (priorityQueue.contains(job)) {
                     // only reschedule high priority jobs
                     if (StateCacheJob.PRIORITY_HIGH == job.getPriority()) {
                         // reschedule
                         priorityQueue.remove(job);
                         priorityQueue.add(job);
                     }
                 } else {
                     priorityQueue.add(job);
                 }
             }
         }
 
         // schedule a queue "run"
         scheduleQueueRun();
     }
 
     /**
      * Schedules this queue job.
      */
     void scheduleQueueRun() {
         int state = getState();
         switch (state) {
         case Job.SLEEPING:
             wakeUp(DEFAULT_DELAY);
             break;
         case NONE:
            // lock job to avoid illegal state exceptions
            synchronized (this) {
                if (getState() == Job.NONE)
                        setSystem(ClearcasePlugin.isHideRefreshActivity());
            }
             schedule(DEFAULT_DELAY);
             break;
         case RUNNING:
             schedule(DEFAULT_DELAY);
             break;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
      */
     public boolean belongsTo(Object family) {
         return ClearcasePlugin.FAMILY_CLEARCASE_OPERATION == family;
     }
 
     /**
      * Stops the job.
      * 
      * @param clean
      *            indicates if all pending refresh jobs should be deleted
      * @return <code>false</code> if the job is currently running (and thus
      *         may not respond to cancelation), and <code>true</code> in all
      *         other cases.
      * @see Job#cancel()
      */
     public boolean cancel(boolean clean) {
         boolean canceled = cancel();
         if (clean) {
             synchronized (priorityQueue) {
                 priorityQueue.clear();
             }
         }
         return canceled;
     }
 
     /**
      * Indicates if the job queue is empty
      * 
      * @return <code>true</code> if empty
      */
     public boolean isEmpty() {
         // do not synchronize
         return priorityQueue.isEmpty();
     }
 }
