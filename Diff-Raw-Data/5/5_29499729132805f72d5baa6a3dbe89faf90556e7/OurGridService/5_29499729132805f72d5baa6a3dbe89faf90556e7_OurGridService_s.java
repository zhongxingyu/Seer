 package embeddedbroker;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.locks.AbstractQueuedSynchronizer;
 
 import org.apache.log4j.Logger;
 import org.ourgrid.broker.BrokerComponentContextFactory;
 import org.ourgrid.broker.BrokerConfiguration;
 import org.ourgrid.broker.BrokerConstants;
 import org.ourgrid.common.interfaces.to.GridProcessState;
 import org.ourgrid.common.interfaces.to.JobEndedInterested;
 import org.ourgrid.common.spec.exception.JobSpecificationException;
 import org.ourgrid.common.spec.exception.TaskSpecificationException;
 import org.ourgrid.common.spec.job.IOBlock;
 import org.ourgrid.common.spec.job.IOEntry;
 import org.ourgrid.common.spec.job.JobSpec;
 import org.ourgrid.common.spec.job.TaskSpec;
 
 import br.edu.ufcg.lsd.commune.context.ModuleContext;
 import br.edu.ufcg.lsd.commune.context.PropertiesFileParser;
 import br.edu.ufcg.lsd.commune.network.xmpp.CommuneNetworkException;
 import br.edu.ufcg.lsd.commune.processor.ProcessorStartException;
 import embeddedbroker.broker.BrokerAsyncApplicationClient;
 import embeddedbroker.executor.TaskRunner;
 import embeddedbroker.executor.codec.JavaSerializationCodec;
 import embeddedbroker.executor.codec.SerializationCodec;
 
 public class OurGridService implements GridService {
 
 	private static Logger logger = Logger.getLogger(OurGridService.class);
 
 	private final JobHandler jobHandler;
 	private final SerializationCodec codec;
 	private final BrokerAsyncApplicationClient client;
 
 	public OurGridService() {
 		this(BrokerConfiguration.PROPERTIES_FILENAME);
 	}
 
 	public OurGridService(String configurationFilename) {
 		try {
			PropertiesFileParser properties = new PropertiesFileParser(BrokerConfiguration.PROPERTIES_FILENAME);
 			ModuleContext context = new BrokerComponentContextFactory(properties).createContext();
 			client = new BrokerAsyncApplicationClient(context);
			client.waitUpTime(200);
 
 			jobHandler = new JobHandler();
 			client.getContainer().deploy(BrokerConstants.JOB_ENDED_INTERESTED, jobHandler);
 			codec = new JavaSerializationCodec();  // TODO @Inject
 
 		} catch (CommuneNetworkException e) {
 			throw new GridServiceException("Cannot contact Broker", e);
 
 		} catch (ProcessorStartException e) {
 			throw new GridServiceException("Cannot contact Broker", e);
 		}
 	}
 
 	public final void shutdown() {
 		try {
 			client.stop();
 
 		} catch (CommuneNetworkException e) {
 			throw new GridServiceException("Cannot contact Broker", e);
 		}
 	}
 
 	public final <JobResult, TaskResult extends Serializable>
 	Future<JobResult> submit(Job<TaskResult, JobResult> job) {
 		return submit(null, job);
 	}
 
 	public final <JobResult, TaskResult extends Serializable>
 	Future<JobResult> submit(String requirements, Job<TaskResult, JobResult> job) {
 
 		Pair<JobSpec, List<File>> jobSpec = createJobSpec(job);
 		int jobId = client.addJob(jobSpec._1);
 		GridFuture<TaskResult, JobResult> future = new GridFuture<TaskResult, JobResult>(jobId);
 		jobHandler.addJobResult(future, jobSpec._2);
 		client.notifyWhenJobIsFinished(jobId);
 		return future;
 	}
 
 	private <JobResult, TaskResult extends Serializable>
 	Pair<JobSpec, List<File>> createJobSpec(Job<TaskResult, JobResult> job) throws GridServiceException {
 
 		List<TaskSpec> tasks = new ArrayList<TaskSpec>();
 		List<File> outputs = new ArrayList<File>();
 
 		Pair<TaskSpec, File> taskSpec;
 		for (Task<TaskResult> task : job) {
 			taskSpec = createTaskSpec(job, task);
 			tasks.add(taskSpec._1);
 			outputs.add(taskSpec._2);
 		}
 
 		if (tasks.size() == 0) {
 			throw new GridServiceException("No tasks to process.");
 		}
 
 		JobSpec jobSpec = new JobSpec(job.toString());
 		try {
 			jobSpec.setTaskSpecs(tasks);
 
 		} catch (JobSpecificationException e) {
 			throw new GridServiceException(e);
 		}
 
 		return new Pair<JobSpec, List<File>>(jobSpec, outputs);
 	}
 
 	private <JobResult, TaskResult extends Serializable>
 	Pair<TaskSpec, File> createTaskSpec(Job<TaskResult, JobResult> job, Task<TaskResult> task) throws GridServiceException {
 
 		File input = createTempFile();
 		File output = createTempFile();
 
 		try {
 			codec.writeObject(task, new FileOutputStream(input));
 		} catch (IOException e) {
 			throw new GridServiceException("Could not write in temporary file.", e);
 		}
 
 		IOBlock initBlock = new IOBlock();
 		initBlock.putEntry(new IOEntry("put", input.getAbsolutePath(), input.getName()));
 
 		StringBuilder classpath = new StringBuilder(".:$STORAGE/embedded-broker.jar");
 		for (File library : job.getLibraries()) {
 			initBlock.putEntry(new IOEntry("store", library.getAbsolutePath(), library.getName()));
 			classpath
 			.append(System.getProperty("file.separator")) // TODO windows?! here be dragons!
 			.append(library.getName());
 		}
 
 		/*for (File resource : resourceList) {
 			initBlock.putEntry(new IOEntry("put", resource.getAbsolutePath(), resource.getName()));
 		} createJarList()*/
 
 		String remoteCommand = "java -cp " + classpath.toString()
 		+ " " + TaskRunner.class.getName() + " " + input.getName() + " " + output.getName();
 
 		IOBlock finalBlock = new IOBlock();
 		finalBlock.putEntry(new IOEntry("get", output.getName(), output.getAbsolutePath()));
 
 		try {
 			return new Pair<TaskSpec, File>(new TaskSpec(initBlock, remoteCommand, finalBlock, null), output);
 
 		} catch (TaskSpecificationException e) {
 			throw new GridServiceException("Could not create task specification.", e);
 		}
 	}
 
 	private File createTempFile() throws GridServiceException {
 		try {
 			return File.createTempFile(UUID.randomUUID().toString(), ".task");
 
 		} catch (IOException e) {
 			throw new GridServiceException("Could not create temp file!", e);
 		}
 	}
 
 	private static class Pair<A, B> {
 
 		public A _1;
 		public B _2;
 
 		public Pair(A a, B b) {
 			this._1 = a;
 			this._2 = b;
 		}
 
 	}
 
 	private class GridFuture<TaskResult extends Serializable, JobResult>
 	extends AbstractQueuedSynchronizer implements Future<JobResult> {
 
 		private static final long serialVersionUID = 8348057611360087600L;
 
 		/** State value representing that task is running */
 		private static final int RUNNING   = 1;
 		/** State value representing that task was finished */
 		private static final int FINISHED  = 2;
 		/** State value representing that task was cancelled */
 		private static final int CANCELLED = 4;
 
 		/** The job id for GridFuture */
 		public final int jobId;
 		/** The result to return from get() */
 		private JobResult result;
 		/** The exception to throw from get() */
 		private Throwable exception;
 
 		public GridFuture(int jobId) {
 			this.jobId = jobId;
 			compareAndSetState(0, RUNNING);
 		}
 
 		public boolean cancel(boolean mayInterruptIfRunning) {
 
 			for (;;) {
 				int s = getState();
 				if (finishedOrCancelled(s)) {
 					return false;
 				}
 				if (compareAndSetState(s, CANCELLED)) {
 					break;
 				}
 			}
 
 			if (mayInterruptIfRunning) {
 				client.cancelJob(jobId);
 			}
 
 			releaseShared(0);
 			return true;
 		}
 
 		public JobResult get() throws InterruptedException, ExecutionException {
 			acquireSharedInterruptibly(0);
 			if (getState() == CANCELLED) {
 				throw new CancellationException();
 			}
 			if (exception != null) {
 				throw new ExecutionException(exception);
 			}
 
 			return result;
 		}
 
 		public JobResult get(long timeout, TimeUnit unit) throws InterruptedException,
 		ExecutionException, TimeoutException {
 			if (!tryAcquireSharedNanos(0, unit.toNanos(timeout))) {
 				throw new TimeoutException();
 			}
 			if (getState() == CANCELLED) {
 				throw new CancellationException();
 			}
 			if (exception != null) {
 				throw new ExecutionException(exception);
 			}
 
 			return result;
 		}
 
 		private boolean finishedOrCancelled(int state) {
 			return (state & (FINISHED | CANCELLED)) != 0;
 		}
 
 		public boolean isCancelled() {
 			return (getState() == CANCELLED);
 		}
 
 		public boolean isDone() {
 			return finishedOrCancelled(getState());
 		}
 
 		/**
 		 * Implements AQS base release to always signal after setting
 		 * final done status by nulling runner thread.
 		 */
 		@Override
 		protected boolean tryReleaseShared(int ignore) {
 			return true;
 		}
 
 		/**
 		 * Implements AQS base acquire to succeed if is done
 		 */
 		@Override
 		protected int tryAcquireShared(int ignore) {
 			return isDone() ? 1 : -1;
 		}
 
 		protected void setResult(JobResult jobResult) {
 
 			if (getState() != RUNNING) {
 				releaseShared(0); // cancel
 			}
 
 			for (;;) {
 				int s = getState();
 				if (s == FINISHED) {
 					return;
 				}
 
 				if (s == CANCELLED) {
 					releaseShared(0);
 					return;
 				}
 
 				if (compareAndSetState(s, FINISHED)) {
 					result = jobResult;
 					releaseShared(0);
 					return;
 				}
 			}
 		}
 
 		protected void setException(Throwable t) {
 
 			if (getState() != RUNNING) {
 				releaseShared(0); // cancel
 			}
 
 			for (;;) {
 				int s = getState();
 				if (s == FINISHED) {
 					return;
 				}
 
 				if (s == CANCELLED) {
 					releaseShared(0);
 					return;
 				}
 
 				if (compareAndSetState(s, FINISHED)) {
 					exception = t;
 					result = null;
 					releaseShared(0);
 					return;
 				}
 			}
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public final class JobHandler implements JobEndedInterested {
 
 		private final Map<Integer, Pair<GridFuture, List<File>>> jobs = new HashMap<Integer, Pair<GridFuture, List<File>>>();
 
 		public void addJobResult(GridFuture future, List<File> output) {
 			jobs.put(future.jobId, new Pair<GridFuture, List<File>>(future, output));
 		}
 
 		public Pair<GridFuture, List<File>> getJobResult(int jobId) {
 			Pair<GridFuture, List<File>> jobResult = jobs.get(jobId);
 			if (jobResult == null) {
 				throw new GridServiceException("Job not found: " + jobId);
 			}
 			return jobResult;
 		}
 
 		@Override
 		public void jobEnded(int jobId, GridProcessState state) {
 			logger.info("Job [" + jobId + "] was " + state.name());
 
 			Pair<GridFuture, List<File>> jobResult = getJobResult(jobId);
 			if (state == GridProcessState.FINISHED) {
 				readAndSetResults(jobResult);
 
 			} else if (state == GridProcessState.FAILED
 					|| state == GridProcessState.SABOTAGED) {
 				jobResult._1.setException(new GridServiceException(state.name()));
 			}
 		}
 
 		private void readAndSetResults(Pair<GridFuture, List<File>> jobResult) {
 
 			try {
 				List<Object> results = new ArrayList<Object>();
 				for (File result : jobResult._2) {
 					results.add(codec.readObject(new FileInputStream(result)));
 				}
 
 				jobResult._1.setResult(results);
 
 			} catch (FileNotFoundException e) {
 				jobResult._1.setException(e);
 
 			} catch (IOException e) {
 				jobResult._1.setException(e);
 			}
 		}
 
 		@Override
 		public void schedulerHasBeenShutdown() {
 			// ignore
 			System.out
 			.println("OurGridService.JobHandler.schedulerHasBeenShutdown()");
 		}
 
 	}
 
 }
