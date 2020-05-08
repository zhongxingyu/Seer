 /**
  *
  */
 package org.jdcp.scheduling;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Random;
 import java.util.UUID;
 
 import org.jdcp.job.TaskDescription;
 import org.jdcp.remote.JobService;
 
 /**
  * A <code>TaskScheduler</code> that serves tasks for the earliest scheduled
  * job having the highest priority in a round robin fashion.  That is, tasks
  * are scheduled so that each job completes before the next one starts.
  * @author brad
  */
 public final class PrioritySerialTaskScheduler implements TaskScheduler {
 
 	/**
 	 * A <code>Map</code> associating information about a job with the
 	 * corresponding job's <code>UUID</code>.
 	 * @see JobInfo
 	 */
 	private Map<UUID, JobInfo> jobs = new HashMap<UUID, JobInfo>();
 
 	/**
 	 * A <code>PriorityQueue</code> used to determine which job is next in
 	 * line.
 	 */
 	private PriorityQueue<UUID> jobQueue = new PriorityQueue<UUID>(11, new JobIdComparator());
 
 	/**
 	 * Each job is assigned an order number using an increasing counter.  This
 	 * allows {@link #jobQueue} to determine in which order jobs were first
 	 * seen.  This field stores the order number to assign to the next job that
 	 * is added.
 	 */
 	private int nextOrder = 0;
 
 	/** A random number generator used to assign task IDs. */
 	private final Random rand = new Random();
 
 	/**
 	 * Represents bookkeeping information about a
 	 * <code>ParallelizableJob</code>.
 	 * @author brad
 	 */
 	private final class JobInfo implements Comparable<JobInfo> {
 
 		/** The <code>UUID</code> for this job. */
 		public final UUID id;
 
 		/** The priority assigned to this job. */
 		private int priority = JobService.DEFAULT_PRIORITY;
 
 		/** The order in which this job was added to the schedule. */
 		private final int order = nextOrder++;
 
 		/**
 		 * A <code>Map</code> associating task IDs with the corresponding
 		 * <code>TaskDescription</code>.
 		 */
 		private Map<Integer, TaskDescription> tasks = new HashMap<Integer, TaskDescription>();
 
 		/**
 		 * A <code>LinkedList</code> of task IDs used to
 		 */
 		private final LinkedList<Integer> taskQueue = new LinkedList<Integer>();
 
 		/**
 		 * Creates a new <code>JobInfo</code>.
 		 * @param id The <code>UUID</code> identifying the job that this
 		 * 		<code>JobInfo</code> describes.
 		 */
 		public JobInfo(UUID id) {
 			this.id = id;
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Comparable#compareTo(java.lang.Object)
 		 */
 		public int compareTo(JobInfo other) {
 			if (priority > other.priority) {
 				return -1;
 			} else if (priority < other.priority) {
 				return 1;
 			} else if (order < other.order) {
 				return -1;
 			} else if (order > other.order) {
 				return 1;
 			} else {
 				return 0;
 			}
 		}
 
 		/**
 		 * Generates a unique task identifier.
 		 * @return The generated task ID.
 		 */
 		private int generateTaskId() {
 			int taskId;
 			do {
 				taskId = rand.nextInt();
 			} while (tasks.containsKey(taskId));
 			return taskId;
 		}
 
 		/**
 		 * Adds a task to the queue for this job.
 		 * @param task The <code>Object</code> describing the task to be
 		 * 		scheduled.
 		 * @return The task ID for the newly scheduled task.
 		 */
 		public int addTask(Object task) {
 			int taskId = generateTaskId();
 			TaskDescription desc = new TaskDescription(id, taskId, task);
 			tasks.put(taskId, desc);
 			taskQueue.addFirst(taskId);
 			return taskId;
 		}
 
 		/**
 		 * Obtains the next task to be served for this job.
 		 * @return The <code>TaskDescription</code> for the next task to be
 		 * 		served.
 		 */
 		public TaskDescription getNextTask() {
 			if (taskQueue.isEmpty()) {
 				return null;
 			}
 			int taskId = taskQueue.remove();
 			taskQueue.addLast(taskId);
 			return tasks.get(taskId);
 		}
 
 		/**
 		 * Removes a task from the queue for this job.
 		 * @param taskId The task ID of the task to be removed.
 		 * @return The <code>Object</code> describing the removed task.
 		 */
 		public Object removeTask(int taskId) {
 			taskQueue.remove((Object) new Integer(taskId));
 			TaskDescription desc = tasks.remove(taskId);
			return desc.getTask().get();
 		}
 
 		/**
 		 * Sets the priority for this job.
 		 * @param priority The priority for this job.
 		 */
 		public void setPriority(int priority) {
 			this.priority = priority;
 		}
 
 	}
 
 	/**
 	 * Compares two <code>UUID</code>s representing jobs according to their
 	 * priority then according to the order in which they were first seen.
 	 * @author brad
 	 */
 	private final class JobIdComparator implements Comparator<UUID> {
 
 		/* (non-Javadoc)
 		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 		 */
 		public int compare(UUID id1, UUID id2) {
 			JobInfo job1 = jobs.get(id1);
 			JobInfo job2 = jobs.get(id2);
 			if (job1 == null || job2 == null) {
 				throw new IllegalArgumentException("Either id1 or id2 represent a non-existant job.");
 			}
 			return job1.compareTo(job2);
 		}
 
 	}
 
 	/**
 	 * Gets the bookkeeping information for a job.  If the specified job has
 	 * not been seen, a new <code>JobInfo</code> is created for it.
 	 * @param jobId The <code>UUID</code> of the job for which to obtain the
 	 * 		corresponding <code>JobInfo</code>.
 	 * @return The <code>JobInfo</code> for the specified job.
 	 */
 	private JobInfo getJob(UUID jobId) {
 		JobInfo job = jobs.get(jobId);
 		if (job == null) {
 			job = new JobInfo(jobId);
 			jobs.put(jobId, job);
 			jobQueue.add(jobId);
 		}
 		return job;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.scheduling.TaskScheduler#add(java.util.UUID, java.lang.Object)
 	 */
 	public int add(UUID jobId, Object task) {
 		JobInfo job = getJob(jobId);
 		if (!jobQueue.contains(jobId)) {
 			jobQueue.add(jobId);
 		}
 
 		return job.addTask(task);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.scheduling.TaskScheduler#getNextTask()
 	 */
 	public TaskDescription getNextTask() {
 		TaskDescription desc = null;
 
 		while (true) {
 			UUID jobId = jobQueue.peek();
 			if (jobId == null) {
 				break;
 			}
 
 			JobInfo job = getJob(jobId);
 			desc = job.getNextTask();
 			if (desc == null) {
 				jobQueue.remove();
 			} else {
 				break;
 			}
 		}
 
 		return desc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.scheduling.TaskScheduler#remove(java.util.UUID, int)
 	 */
 	public Object remove(UUID jobId, int taskId) {
 		JobInfo job = jobs.get(jobId);
		return job.removeTask(taskId);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.scheduling.TaskScheduler#setJobPriority(java.util.UUID, int)
 	 */
 	public void setJobPriority(UUID jobId, int priority) {
 		JobInfo job = jobs.get(jobId);
 		jobQueue.remove(jobId);
 		job.setPriority(priority);
 		jobQueue.add(jobId);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jdcp.scheduling.TaskScheduler#removeJob(java.util.UUID)
 	 */
 	public void removeJob(UUID jobId) {
 		jobQueue.remove(jobId);
 		jobs.remove(jobId);
 	}
 
 }
