 package grisu.frontend.model.job;
 
 import grisu.control.JobConstants;
 import grisu.control.ServiceInterface;
 import grisu.control.events.FileDeletedEvent;
 import grisu.control.events.FolderCreatedEvent;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.control.exceptions.StatusException;
 import grisu.frontend.control.clientexceptions.FileTransactionException;
 import grisu.frontend.control.fileTransfers.FileTransaction;
 import grisu.frontend.control.fileTransfers.FileTransactionManager;
 import grisu.frontend.control.login.LoginManager;
 import grisu.frontend.model.events.JobCleanedEvent;
 import grisu.frontend.model.events.JobStatusEvent;
 import grisu.frontend.model.events.NewJobEvent;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.model.FileManager;
 import grisu.model.GrisuRegistryManager;
 import grisu.model.dto.DtoJob;
 import grisu.model.dto.GridFile;
 import grisu.model.job.JobCreatedProperty;
 import grisu.model.job.JobDescription;
 import grisu.model.status.StatusObject;
 import grisu.utils.FileHelpers;
 import grisu.utils.SeveralXMLHelpers;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutionException;
 
 import javax.persistence.Transient;
 
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.bushe.swing.event.EventBus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import com.google.common.collect.Maps;
 
 /**
  * A model class that hides all the complexity of creating and submitting a job.
  *
  * It extends the {@link JobDescription} class which is used to create
  * the job using the basic {@link JobSubmissionProperty}s. It adds methods to
  * create the job on the backend, submit it and also to monitor/control it.
  *
  * @author Markus Binsteiner
  */
 public class GrisuJob extends JobDescription implements
 Comparable<GrisuJob> {
 
 	static final Logger myLogger = LoggerFactory.getLogger(GrisuJob.class
 			.getName());
 
 	public static GrisuJob createJobObject(ServiceInterface si,
 			JobDescription jobsubmissionObject)
 					throws JobPropertiesException {
 
 		final GrisuJob job = new GrisuJob(si,
 				jobsubmissionObject.getJobDescriptionDocument());
 
 		job.setInputFiles(jobsubmissionObject.getInputFiles());
 		job.setEnvironmentVariables(jobsubmissionObject.getEnvironmentVariables());
 
 		return job;
 	}
 
 	private final ServiceInterface serviceInterface;
 
 	private final FileManager fm;
 	private int status = JobConstants.UNDEFINED;
 
 	private Map<String, String> allJobProperties;
 
 	private String jobDirectory;
 
 	private boolean isFinished = false;
 
 	private final Map<Integer, Thread> waitThreads = Maps.newConcurrentMap();
 
 	private Map<Date, String> logMessages;
 
 	private boolean isBeingCleaned = false;
 
 	private boolean isArchived = false;
 
 	private String description = null;
 
 	private final List<String> submissionLog = Collections
 			.synchronizedList(new LinkedList<String>());
 
 	private final Map<String, String> properties = Collections
 			.synchronizedMap(new TreeMap<String, String>());
 
 	private Date lastStatusUpdate = new Date();
 
 
 
 	/**
 	 * Use this constructor if you want to create a new job.
 	 *
 	 * @param si
 	 *            the serviceInterface
 	 */
 	public GrisuJob(final ServiceInterface si) {
 		super();
 		this.serviceInterface = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		addJobLogMessage("Empty job created.");
 	}
 
 	/**
 	 * This constructor creates a new GrisuJob and initializes the basic job
 	 * parameters using the provided jsdl document. This could be used for
 	 * example if you want to use an already submitted job by calling the
 	 * {@link ServiceInterface#getJsdlDocument(String)} and using its return
 	 * value in this constructor.
 	 *
 	 * @param si
 	 *            the serviceInterface
 	 * @param jsdl
 	 *            the jsdl document
 	 */
 	public GrisuJob(final ServiceInterface si, final Document jsdl) {
 		super(jsdl);
 		this.serviceInterface = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		addJobLogMessage("Job created from jsdl description.");
 	}
 
 	/**
 	 * This can be also used to create a GrisuJob when the job is already
 	 * created on the backend.
 	 *
 	 * This one doesn't update the job status on the backend.
 	 *
 	 * @param si
 	 *            the serviceinterface
 	 * @param job
 	 *            the job
 	 * @throws NoSuchJobException
 	 *             if no such job exists on the backend
 	 */
 	public GrisuJob(final ServiceInterface si, final DtoJob job)
 			throws NoSuchJobException {
 
 		this(si, job, false);
 	}
 
 	public GrisuJob(final ServiceInterface si, final DtoJob job,
 			final boolean refreshJobStatusOnBackend) throws NoSuchJobException {
 		// need to use either jobname or jobdirectory to get remote job,
 		// depending on whether
 		// job is archived or not
 		super((job.isArchived()) ? SeveralXMLHelpers.fromString(si
 				.getJsdlDocument(job.jobname())) : job.propertiesAsMap());
 
		this.setJobname(jobname);
 
 		this.serviceInterface = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 
 		this.isArchived = job.isArchived();
 
 		updateWithDtoJob(job);
 		addJobLogMessage("Job retrieved from backend.");
 
 		if (refreshJobStatusOnBackend) {
 			getStatus(true);
 		}
 
 	}
 
 	/**
 	 * This constructor creates a new GrisuJob and initializes it with the
 	 * values you provide in the jobProperties map. Have a look at the
 	 * {@link JobSubmissionProperty} enum to find out which properties are
 	 * supported and what the names of the keys are for them.
 	 *
 	 * @param si
 	 *            the serviceInterface
 	 * @param jobProperties
 	 *            the basic properties of the job (no. of cpus, application to
 	 *            use, ...)
 	 */
 	public GrisuJob(final ServiceInterface si,
 			final Map<String, String> jobProperties) {
 		super(jobProperties);
 		this.serviceInterface = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		addJobLogMessage("Job created from job properties.");
 	}
 
 	/**
 	 * Use this constructor if the job is already created on the backend. It'll
 	 * fetch all the basic jobproperties from the backend and it'll also get the
 	 * current status of the job.
 	 *
 	 * @param si
 	 *            the serviceInterface
 	 * @param jobname
 	 *            the name of the job
 	 * @throws NoSuchJobException
 	 *             if there is no job with the specified name on the backend
 	 *             connected to the specified serviceInterface
 	 */
 	public GrisuJob(final ServiceInterface si, final String jobname)
 			throws NoSuchJobException {
 
 		this(si, jobname, true);
 
 	}
 
 	/**
 	 * Use this constructor if the job is already created on the backend. It'll
 	 * fetch all the basic jobproperties from the backend and it'll also get the
 	 * current status of the job.
 	 *
 	 * @param si
 	 *            the serviceInterface
 	 * @param jobname
 	 *            the name of the job
 	 * @param refreshJobStatusOnBackend
 	 *            whether to refresh the status of the job on the backend
 	 * @throws NoSuchJobException
 	 *             if there is no job with the specified name on the backend
 	 *             connected to the specified serviceInterface
 	 */
 	public GrisuJob(final ServiceInterface si, final String jobname,
 			boolean refreshJobStatusOnBackend) throws NoSuchJobException {
 
 		super(SeveralXMLHelpers.fromString(si.getJsdlDocument(jobname)));
 		this.setJobname(jobname);
 		this.serviceInterface = si;
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 
 		updateWithDtoJob(serviceInterface.getJob(jobname));
 
 		addJobLogMessage("Job retrieved from backend.");
 
 		if (refreshJobStatusOnBackend) {
 			getStatus(true);
 		}
 
 	}
 
 	private void addJobLogMessage(String message) {
 		this.submissionLog.add(message);
 		pcs.firePropertyChange("submissionLog", null, getSubmissionLog());
 	}
 
 	public void addJobProperty(String key, String value) {
 		properties.put(key, value);
 	}
 
 	/**
 	 * Archives the job in the background, using the default archive location.
 	 *
 	 * @return the url of the target archived jobdirectory
 	 * @throws JobPropertiesException
 	 *             if the job can't be archived
 	 * @throws RemoteFileSystemException
 	 *             if the job can't be archived because the target url can't be
 	 *             accessed
 	 */
 	public final String archive() throws JobPropertiesException,
 	RemoteFileSystemException {
 		return archive(null, false);
 	}
 
 	/**
 	 * Archives the job in the background, using a specified target location
 	 *
 	 * @param target
 	 *            the target url to archive the job or null to use the default
 	 *            target
 	 * @return the url of the target archived jobdirectory
 	 * @throws JobPropertiesException
 	 *             if the job can't be archived
 	 * @throws RemoteFileSystemException
 	 *             if the job can't be archived because the target url can't be
 	 *             accessed
 	 */
 	public final String archive(String target) throws JobPropertiesException,
 	RemoteFileSystemException {
 		return archive(target, false);
 	}
 
 	/**
 	 * Archives the job.
 	 *
 	 * @param target
 	 *            the url (of the parent dir) to archive the job to or null to
 	 *            use the (previously set) default archive location
 	 * @param waitForArchivingToFinish
 	 *            whether to wait until archiving is finished
 	 * @throws JobPropertiesException
 	 *             if the job is not finished yet
 	 * @throws RemoteFileSystemException
 	 *             if the archive location is not specified or there is some
 	 *             other kind of file related exception
 	 */
 	public final String archive(String target, boolean waitForArchivingToFinish)
 			throws JobPropertiesException, RemoteFileSystemException {
 		try {
 			final String targetUrl = getServiceInterface().archiveJob(
 					getJobname(), target);
 
 			if (waitForArchivingToFinish) {
 				try {
 					StatusObject.waitForActionToFinish(getServiceInterface(),
 							targetUrl, 5, true);
 
 					isArchived = true;
 					pcs.firePropertyChange("archived", false, true);
 
 					final String oldUrl = jobDirectory;
 					jobDirectory = targetUrl;
 					pcs.firePropertyChange("jobDirectory", oldUrl, jobDirectory);
 
 					EventBus.publish(new JobCleanedEvent(this));
 					EventBus.publish(new FileDeletedEvent(oldUrl));
 					EventBus.publish(new FolderCreatedEvent(jobDirectory));
 				} catch (final Exception e) {
 					throw new JobPropertiesException("Can't archive job: "
 							+ e.getLocalizedMessage());
 				}
 
 			} else {
 				Thread t = new Thread() {
 
 					@Override
 					public void run() {
 
 						try {
 							StatusObject.waitForActionToFinish(
 									getServiceInterface(), targetUrl, 5, true);
 
 							isArchived = true;
 							pcs.firePropertyChange("archived", false, true);
 
 							final String oldUrl = jobDirectory;
 							jobDirectory = targetUrl;
 							pcs.firePropertyChange("jobDirectory", oldUrl,
 									jobDirectory);
 
 							EventBus.publish(new JobCleanedEvent(GrisuJob.this));
 							EventBus.publish(new FileDeletedEvent(oldUrl));
 							EventBus.publish(new FolderCreatedEvent(
 									jobDirectory));
 
 						} catch (final Exception e) {
 							myLogger.error("Job archiving error.", e);
 						}
 
 					}
 				};
 				t.setName("Wait thread for archive job " + getJobname());
 				t.start();
 
 			}
 
 			return targetUrl;
 		} catch (final NoSuchJobException e) {
 			// should never happen
 			myLogger.error(e.getLocalizedMessage(), e);
 			throw new JobPropertiesException(e.getLocalizedMessage());
 		}
 	}
 
 	public int compareTo(GrisuJob o2) {
 		return getJobname().compareTo(o2.getJobname());
 	}
 
 	/**
 	 * Creates the job on the grisu backend using the "force-name" method (which
 	 * means the backend will not change the jobname you specified -- if there
 	 * is a job with that jobname already the backend will throw an exception).
 	 *
 	 * Also, this method uses teh "none" group ({@link Constants.NON_VO_FQAN})
 	 *
 	 * Be aware, that once that is done, you can't change any of the basic job
 	 * parameters anymore. The backend calculates all the (possibly) missing job
 	 * parameters and sets values like the final submissionlocation and such.
 	 * After you created a job on the backend, you can query these calculated
 	 * values using the {@link ServiceInterface#getJobProperty(String, String)}
 	 * method.
 	 *
 	 * @return the final jobname (equals the one you specified when creating the
 	 *         GrisuJob object).
 	 * @throws JobPropertiesException
 	 *             if one of the properties is invalid and the job could not be
 	 *             created on the backend
 	 */
 	public final String createJob() throws JobPropertiesException {
 
 //		final String fqan = GrisuRegistryManager.getDefault(serviceInterface)
 //				.getUserEnvironmentManager().getCurrentFqan();
 
 		String fqan = Constants.NON_VO_FQAN;
 		return createJob(fqan);
 
 	}
 
 	/**
 	 * Creates the job on the grisu backend using the "uniqueJobname" method
 	 * (which means the backend will append a number to the jobname is a job
 	 * with the same name already exists).
 	 *
 	 * Be aware, that once that is done, you can't change any of the basic job
 	 * parameters anymore. The backend calculates all the (possibly) missing job
 	 * parameters and sets values like the final submissionlocation and such.
 	 * After you created a job on the backend, you can query these calculated
 	 * values using the {@link ServiceInterface#getJobProperty(String, String)}
 	 * method.
 	 *
 	 * @param fqan
 	 *            the VO to use to submit this job
 	 * @return the final jobname (equals the one you specified when creating the
 	 *         GrisuJob object).
 	 * @throws JobPropertiesException
 	 *             if one of the properties is invalid and the job could not be
 	 *             created on the backend
 	 */
 	public final String createJob(final String fqan)
 			throws JobPropertiesException {
 
 		return createJob(fqan, Constants.UNIQUE_NUMBER_METHOD);
 	}
 
 	/**
 	 * Creates the job on the grisu backend using the jobname creating method
 	 * you specified. Have a look at the static Strings in
 	 * {@link ServiceInterface} for a list of supported jobname creation
 	 * methods.
 	 *
 	 * Other than the jobname creation, this does the same as
 	 * {@link #createJob(String)}.
 	 *
 	 * @param fqan
 	 *            the VO to use to submit this job
 	 * @param jobnameCreationMethod
 	 *            the name of the jobname creation method the backend should use
 	 * @return the final name of the job which can be used as a handle to get
 	 *         jobproperties like the status or jobdirectory
 	 * @throws JobPropertiesException
 	 */
 	public final String createJob(final String fqan,
 			final String jobnameCreationMethod) throws JobPropertiesException {
 
 		EventBus.publish(new JobStatusEvent(this, this.status,
 				JobConstants.UNDEFINED));
 		addJobLogMessage("Creating job on backend...");
 		if (StringUtils.isNotBlank(getJobname())) {
 			EventBus.publish(this.getJobname(), new JobStatusEvent(this,
 					this.status, JobConstants.UNDEFINED));
 		}
 
 		myLogger.debug("Adding grisu client information to environment.");
 		String client = LoginManager.getClientName();
 		String version = LoginManager.getClientVersion();
 		addJobProperty("client", client);
 		addJobProperty("client_version", version);
 		//addEnvironmentVariable("GRISU_CLIENT", client);
 		//addEnvironmentVariable("GRISU_CLIENT_VERSION", version);
 
 		try {
 			setJobname(serviceInterface.createJob(
 					getJobDescriptionDocumentAsString(), fqan,
 					jobnameCreationMethod));
 			EventBus.publish(new JobStatusEvent(this, this.status,
 					JobConstants.JOB_CREATED));
 			if (StringUtils.isNotBlank(getJobname())) {
 				addJobLogMessage("Job created. Jobname: " + getJobname());
 				EventBus.publish(this.getJobname(), new JobStatusEvent(this,
 						this.status, JobConstants.JOB_CREATED));
 			} else {
 				addJobLogMessage("Job created.");
 			}
 
 			addJobLogMessage("Command to execute: " + getCommandline());
 			// populate new job properties in background
 			Thread t = new Thread() {
 				@Override
 				public void run() {
 					
 					getAllJobProperties(true);
 					addJobLogMessage("Submission site is: "
 							+ getJobProperty(Constants.SUBMISSION_SITE_KEY,
 									false));
 					addJobLogMessage("Submission queue is: "
 							+ getJobProperty(Constants.QUEUE_KEY, false));
 					addJobLogMessage("Job directory url is: "
 							+ getJobProperty(Constants.JOBDIRECTORY_KEY, false));
 				}
 			};
 			t.setName("job properties update thread for job " + getJobname());
 			t.start();
 
 		} catch (final JobPropertiesException e) {
 			addJobLogMessage("Could not create job on backend: "
 					+ e.getLocalizedMessage());
 			EventBus.publish(new JobStatusEvent(this, this.status,
 					JobConstants.NO_SUCH_JOB));
 			if (StringUtils.isNotBlank(getJobname())) {
 				EventBus.publish(this.getJobname(), new JobStatusEvent(this,
 						this.status, JobConstants.NO_SUCH_JOB));
 			}
 			throw e;
 		}
 
 		// updateJobDirectory();
 
 		return this.getJobname();
 	}
 
 	private synchronized void createWaitThread(final int state,
 			final int checkIntervallInSeconds) {
 
 		Thread waitThread = waitThreads.get(state);
 		try {
 			// just to make sure we don't create 2 or more threads. Should never
 			// happen.
 			if (waitThread != null) {
 				waitThread.interrupt();
 			}
 		} catch (final Exception e) {
 			myLogger.debug(e.getLocalizedMessage(), e);
 		}
 
 		waitThread = new Thread() {
 			@Override
 			public void run() {
 
 				int oldStatus = getStatus(false);
 				while (state > getStatus(true)) {
 
 					if (isInterrupted()) {
 						return;
 					}
 					if (oldStatus != getStatus(false)) {
 						myLogger.debug("Status of job " + getJobname()
 								+ " changed to: " + getStatusString(false));
 						EventBus.publish(new JobStatusEvent(GrisuJob.this,
 								oldStatus, GrisuJob.this.getStatus(false)));
 						if (StringUtils.isNotBlank(getJobname())) {
 							EventBus.publish(
 									GrisuJob.this.getJobname(),
 									new JobStatusEvent(GrisuJob.this,
 											oldStatus, GrisuJob.this
 											.getStatus(false)));
 						}
 					}
 					try {
 						Thread.sleep(checkIntervallInSeconds * 1000);
 					} catch (final InterruptedException e) {
 						myLogger.debug("Wait thread for job " + getJobname()
 								+ " interrupted.");
 						return;
 					}
 					oldStatus = getStatus(false);
 				}
 			}
 		};
 		waitThreads.put(state, waitThread);
 
 	}
 
 	/**
 	 * Downloads the specified file (relative path to the jobdirectory) and puts
 	 * it into the local cache.
 	 *
 	 * @param relativePathToJobDirectory
 	 *            the path to the file to download
 	 * @return the file handle to the file in the local cache dir
 	 */
 	public final File downloadAndCacheOutputFile(
 			String relativePathToJobDirectory) {
 
 		addJobLogMessage("Downloading and caching output file...");
 
 		if (getStatus(false) <= JobConstants.ACTIVE) {
 			if (getStatus(true) < JobConstants.ACTIVE) {
 				addJobLogMessage("Can't download output: job not started yet.");
 				throw new IllegalStateException(
 						"Job not started yet. No stdout file exists.");
 			}
 		}
 		//
 		String url;
 		url = getJobDirectoryUrl() + "/" + relativePathToJobDirectory;
 
 		File file = null;
 		try {
 			file = fm.downloadFile(url);
 			addJobLogMessage("Downloaded output file: " + url);
 		} catch (final Exception e) {
 			addJobLogMessage("Could not download file " + url + ": "
 					+ e.getLocalizedMessage());
 			throw new JobException(this, "Could not download file: " + url, e);
 		}
 
 		return file;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 
 		if (other instanceof GrisuJob) {
 			final GrisuJob otherJob = (GrisuJob) other;
 			return getJobname().equals(otherJob.getJobname());
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * Returns a map of all known job properties.
 	 *
 	 * It only makes sense to call this method if the job was already created on
 	 * the backend using the {@link #createJob(String)} or
 	 * {@link #createJob(String, String)} method.
 	 *
 	 * This method doesn't refresh job properties forcefully.
 	 *
 	 * @return the job properties
 	 */
 	public final Map<String, String> getAllJobProperties() {
 
 		return getAllJobProperties(false);
 	}
 
 	/**
 	 * Returns a map of all known job properties.
 	 *
 	 * It only makes sense to call this method if the job was already created on
 	 * the backend using the {@link #createJob(String)} or
 	 * {@link #createJob(String, String)} method.
 	 *
 	 * @param forceRefresh
 	 *            whether to forcefully refresh the job properties
 	 * @return the job properties
 	 */
 	public final Map<String, String> getAllJobProperties(boolean forceRefresh) {
 
 		// if (getStatus(false) == JobConstants.UNDEFINED) {
 		// throw new IllegalStateException("Job status "
 		// + JobConstants.translateStatus(JobConstants.UNDEFINED)
 		// + ". Can't access job properties yet.");
 		// }
 
 		if ((allJobProperties == null) || forceRefresh) {
 
 			synchronized (this) {
 				try {
 					allJobProperties = serviceInterface.getJob(getJobname())
 							.propertiesAsMap();
 				} catch (final Exception e) {
 					throw new JobException(this,
 							"Could not get jobproperties.", e);
 				}
 			}
 
 		}
 		return allJobProperties;
 
 	}
 
 	public String getDescription() {
 		if (this.description == null) {
 			try {
 				this.description = serviceInterface.getJobProperty(
 						getJobname(), Constants.JOB_DESCRIPTION_KEY);
 			} catch (final NoSuchJobException e) {
 				// that's ok.
 			}
 		}
 		return this.description;
 	}
 
 	/**
 	 * Returns the current content of the file for this job as a string.
 	 *
 	 * Internally the file is downloaded to the local grisu cache and read.
 	 *
 	 * @return the current content of the stdout file for this job
 	 */
 	public final String getFileContent(String relativePathToWorkingDir) {
 
 		String result;
 		try {
 			result = FileHelpers
 					.readFromFileWithException(downloadAndCacheOutputFile(relativePathToWorkingDir));
 		} catch (final Exception e) {
 			throw new JobException(this, "Could not read file: "
 					+ relativePathToWorkingDir, e);
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * Returns the filesize of the specified file
 	 *
 	 * @param relatevePathToJobDir
 	 *            the path to the file relative to the job directory.
 	 * @return the filesize in bytes
 	 */
 	public long getFileSize(String relatevePathToJobDir) {
 
 		final String url = getJobDirectoryUrl() + "/" + relatevePathToJobDir;
 
 		try {
 			return GrisuRegistryManager.getDefault(serviceInterface)
 					.getFileManager().getFileSize(url);
 		} catch (final RemoteFileSystemException e) {
 			return -1;
 		}
 
 	}
 
 	/**
 	 * Returns the absolute url to the job directory.
 	 *
 	 * It only makes sense to call this method of the job was already created on
 	 * the backend.
 	 *
 	 * @return the url to the job (working-) directory
 	 */
 	public final String getJobDirectoryUrl() {
 
 		if (this.getStatus(false) == JobConstants.UNDEFINED) {
 			throw new IllegalStateException("Job status "
 					+ JobConstants.translateStatus(JobConstants.UNDEFINED)
 					+ ". Can't get jobdirectory yet.");
 		}
 
 		if (jobDirectory == null) {
 			final String url = getAllJobProperties(false).get(
 					JobCreatedProperty.JOBDIRECTORY.toString());
 			jobDirectory = url;
 		}
 
 		return jobDirectory;
 	}
 
 	/**
 	 * Returns the specified job property without a refresh.
 	 *
 	 * @param key
 	 *            the key
 	 *
 	 * @return the property
 	 */
 	public final String getJobProperty(String key) {
 
 		return getAllJobProperties(false).get(key);
 	}
 
 	/**
 	 * Returns the specified job property.
 	 *
 	 * @param key
 	 *            the key
 	 * @param forceRefresh
 	 *            whether to refresh the job property forcefully or not
 	 *
 	 * @return the property
 	 */
 	public final String getJobProperty(String key, boolean forceRefresh) {
 
 		return getAllJobProperties(forceRefresh).get(key);
 	}
 
 	/**
 	 * Returns the job log without a refresh.
 	 *
 	 * @return the job log
 	 */
 	public synchronized Map<Date, String> getLogMessages() {
 
 		return getLogMessages(false);
 	}
 
 	/**
 	 * Returns the job log
 	 *
 	 * @param forceRefresh
 	 *            whether to forcefully refresh the log messages
 	 * @return the job log
 	 */
 	public Map<Date, String> getLogMessages(boolean forceRefresh) {
 
 		if ((logMessages == null) || forceRefresh) {
 			synchronized (this) {
 				try {
 					updateWithDtoJob(serviceInterface.getJob(jobname));
 				} catch (final NoSuchJobException e) {
 					myLogger.error(e.getLocalizedMessage(), e);
 				}
 			}
 		}
 		return logMessages;
 	}
 
 	public ServiceInterface getServiceInterface() {
 		return this.serviceInterface;
 	}
 
 	/**
 	 * Returns the current status of the job.
 	 *
 	 * Have a look at the {@link JobConstants} class for possible values.
 	 *
 	 * @param forceRefresh
 	 *            whether to use the cached status (false) or force a status
 	 *            refresh (true)
 	 * @return the job status
 	 */
 	public final int getStatus(final boolean forceRefresh) {
 
 		// Date now = new Date();
 		// if ((this.status >= JobConstants.ACTIVE)
 		// && (lastStatusUpdate.getTime() + 5000 >= now.getTime())) {
 		// myLogger.debug("Less than 5 seconds between status updates. Returning old status...");
 		// return this.status;
 		// }
 
 		if (forceRefresh && !isArchived) {
 
 			synchronized (this) {
 
 				final Date now = new Date();
 				if ((this.status >= JobConstants.ACTIVE)
 						&& ((lastStatusUpdate.getTime() + 2000) >= now
 						.getTime())) {
 					myLogger.debug("Less than 2 seconds between status updates. Returning old status...");
 					return this.status;
 				}
 
 				final int oldStatus = this.status;
 				// addJobLogMessage("Getting new job status. Old status: "
 				// + JobConstants.translateStatus(oldStatus));
 				final boolean oldFinished = isFinished(false);
 				this.status = serviceInterface.getJobStatus(getJobname());
 
 				pcs.firePropertyChange("status", oldStatus, this.status);
 				pcs.firePropertyChange("statusString",
 						JobConstants.translateStatus(oldStatus),
 						getStatusString(false));
 				pcs.firePropertyChange("finished", oldFinished,
 						isFinished(false));
 				// addJobLogMessage("Status refreshed. Status is: "
 				// + JobConstants.translateStatus(this.status));
 				if (this.status != oldStatus) {
 					EventBus.publish(new JobStatusEvent(this, oldStatus,
 							this.status));
 					if (StringUtils.isNotBlank(getJobname())) {
 						EventBus.publish(this.getJobname(), new JobStatusEvent(
 								this, oldStatus, this.status));
 					}
 				}
 
 				lastStatusUpdate = new Date();
 			}
 
 		}
 		return this.status;
 	}
 
 	/**
 	 * Same as {@link #getStatus(boolean)}. Just auto-translates the status to a
 	 * meaningful string.
 	 *
 	 * @param forceRefresh
 	 *            whether to use the cached status (false) or force a status
 	 *            refresh (true)
 	 * @return the job status string
 	 */
 	public final String getStatusString(final boolean forceRefresh) {
 		return JobConstants.translateStatus(getStatus(forceRefresh));
 	}
 
 	/**
 	 * Returns the current content of the stderr file for this job as a string.
 	 *
 	 * Internally the stderr file is downloaded to the local grisu cache and
 	 * read.
 	 *
 	 * @return the current content of the stderr file for this job
 	 */
 	public final String getStdErrContent() {
 
 		String result;
 		try {
 			result = FileHelpers.readFromFileWithException(getStdErrFile());
 		} catch (final Exception e) {
 			throw new JobException(this, "Could not read stderr file.", e);
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * This method downloads a current version of the stderr file for this job
 	 * into the local grisu cache and returns the pointer to a locally cached
 	 * version of it.
 	 *
 	 * It only makes sense to call this method of the job was already created on
 	 * the backend.
 	 *
 	 * @return the locally cached stderr file
 	 */
 	public final File getStdErrFile() {
 
 		File stderrFile;
 		try {
 			stderrFile = downloadAndCacheOutputFile(serviceInterface
 					.getJobProperty(getJobname(),
 							JobSubmissionProperty.STDERR.toString()));
 		} catch (final Exception e) {
 			throw new JobException(this, "Could not download stderr file.", e);
 		}
 
 		return stderrFile;
 	}
 
 	/**
 	 * Returns the size of the stderr file.
 	 *
 	 * @return the size in bytes
 	 */
 	public long getStdErrFileSize() {
 
 		return getFileSize(getStderr());
 	}
 
 	/**
 	 * Returns the current content of the stdout file for this job as a string.
 	 *
 	 * Internally the stdout file is downloaded to the local grisu cache and
 	 * read.
 	 *
 	 * @return the current content of the stdout file for this job
 	 */
 	public final String getStdOutContent() {
 
 		String result;
 		try {
 			result = FileHelpers.readFromFileWithException(getStdOutFile());
 		} catch (final Exception e) {
 			throw new JobException(this, "Could not read stdout file.", e);
 		}
 
 		return result;
 
 	}
 
 	/**
 	 * This method downloads a current version of the stdout file for this job
 	 * into the local grisu cache and returns the pointer to a locally cached
 	 * version of it.
 	 *
 	 * It only makes sense to call this method of the job was already created on
 	 * the backend.
 	 *
 	 * @return the locally cached stdout file
 	 */
 	public final File getStdOutFile() {
 
 		File stdoutFile;
 		try {
 			stdoutFile = downloadAndCacheOutputFile(serviceInterface
 					.getJobProperty(getJobname(),
 							JobSubmissionProperty.STDOUT.toString()));
 		} catch (final Exception e) {
 			throw new JobException(this, "Could not download stdout file.", e);
 		}
 
 		return stdoutFile;
 	}
 
 	/**
 	 * Returns the size of the stdout file.
 	 *
 	 * @return the size in bytes
 	 */
 	public long getStdOutFileSize() {
 
 		return getFileSize(getStdout());
 	}
 
 	public List<String> getSubmissionLog() {
 		return submissionLog;
 	}
 
 	@Override
 	public int hashCode() {
 		return 73 * getJobname().hashCode();
 	}
 
 	public boolean isArchived() {
 		return isArchived;
 	}
 
 	public boolean isBeingCleaned() {
 		return isBeingCleaned;
 	}
 
 	/**
 	 * Returns whether a job finished successful (Exit code: 0) or not.
 	 *
 	 * @param forceRefresh
 	 *            whether to refresh the status of the job on the backend or not
 	 *
 	 * @return true if the job is finished but has got another exit code than 0,
 	 *         false otherwise
 	 */
 	public final boolean isFailed(final boolean forceRefresh) {
 
 		final int status = getStatus(forceRefresh);
 
 		if (status < JobConstants.FINISHED_EITHER_WAY) {
 			return false;
 		}
 
 		if (status == JobConstants.DONE) {
 			return false;
 		} else {
 			return true;
 		}
 
 	}
 
 	/**
 	 * Tells you whether the job is finished (either sucessfully or not).
 	 *
 	 * @return finished: true / still running/not started: false
 	 */
 	public final boolean isFinished() {
 		return isFinished(true);
 	}
 
 	/**
 	 * Tells you whether the job is finished (either sucessfully or not).
 	 *
 	 * @param refresh
 	 *            whether to refresh the job on the backend or not
 	 *
 	 * @return finished: true / still running/not started: false
 	 */
 	public final boolean isFinished(boolean refresh) {
 
 		if (getStatus(false) >= JobConstants.FINISHED_EITHER_WAY) {
 			return true;
 		}
 
 		if (!refresh) {
 			return false;
 		}
 
 		if (getStatus(false) <= JobConstants.JOB_CREATED) {
 			throw new IllegalStateException("Job not submitted yet.");
 		}
 
 		if (getStatus(true) >= JobConstants.FINISHED_EITHER_WAY) {
 			isFinished = true;
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * Returns whether a job finished successful (Exit code: 0) or not.
 	 *
 	 * @param forceRefresh
 	 *            whether to refresh the status of the job on the backend or not
 	 *
 	 * @return true if the job is finished and has got an exit code of 0, false
 	 *         otherwise
 	 */
 	public final boolean isSuccessful(final boolean forceRefresh) {
 
 		final int status = getStatus(forceRefresh);
 
 		if (status < JobConstants.FINISHED_EITHER_WAY) {
 			return false;
 		}
 
 		if (status == JobConstants.DONE) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Tells the backend to kill this job. If you specify true for the clean
 	 * parameter, the job gets deleted from the backend database and the
 	 * jobdirectory on the endpoint resource gets deleted as well.
 	 *
 	 * @param clean
 	 *            whether to clean the job
 	 * @throws JobException
 	 *             if the job could not be killed/cleaned
 	 */
 	public final void kill(final boolean clean) {
 
 		if (getStatus(false) == JobConstants.UNDEFINED) {
 			throw new IllegalStateException("Job status "
 					+ JobConstants.translateStatus(JobConstants.UNDEFINED)
 					+ ". Can't kill job yet.");
 		}
 
 		try {
 			if (clean) {
 				isBeingCleaned = true;
 				pcs.firePropertyChange("beingCleaned", false, true);
 
 				// delete local cache for this job
 				Thread t = new Thread() {
 					@Override
 					public void run() {
 						myLogger.debug("Deleting local cached dir for job "
 								+ getJobname() + ": " + getJobDirectoryUrl());
 						final File dir = GrisuRegistryManager
 								.getDefault(serviceInterface).getFileManager()
 								.getLocalCacheFile(getJobDirectoryUrl());
 						FileUtils.deleteQuietly(dir);
 					}
 				};
 				t.setName("Cleanup thread for job " + getJobname());
 				t.start();
 
 			}
 			final String handle = this.serviceInterface.kill(this.getJobname(),
 					clean);
 
 			final StatusObject so = StatusObject.waitForActionToFinish(
 					serviceInterface, handle, 2, false);
 			if (so.getStatus().isFailed()) {
 				throw new Exception(so.getStatus().getErrorCause());
 			}
 			try {
 				getStatus(true);
 			} catch (final Exception nsje) {
 				// that's ok
 			}
 
 			if (clean) {
 				EventBus.publish(new JobCleanedEvent(this));
 				EventBus.publish(new FileDeletedEvent(getJobDirectoryUrl()));
 			}
 
 		} catch (final Exception e) {
 			throw new JobException(this, "Could not kill/clean job.", e);
 		}
 
 	}
 
 	public final GridFile listJobDirectory() throws RemoteFileSystemException {
 
 		final String jobDir = getJobDirectoryUrl();
 		final GridFile result = fm.ls(jobDir);
 
 		return result;
 
 	}
 
 	/**
 	 * Lists all the files that are living under this jobs jobdirectory.
 	 *
 	 * Specify a recursion level of 1 if you only are interested in the
 	 * jobdirectory itself. Or the appropriate level if you want to look deeper.
 	 * Use a value <= -1 to find all files on all levels.
 	 *
 	 * @param recursionLevel
 	 *            the recursion level
 	 * @return the list of files
 	 * @throws RemoteFileSystemException
 	 */
 	public List<String> listJobDirectory(int recursionLevel)
 			throws RemoteFileSystemException {
 
 		final GridFile folder = serviceInterface.ls(getJobDirectoryUrl(),
 				recursionLevel);
 
 		return folder.listOfAllFilesUnderThisFolder();
 
 	}
 
 	public void removeJobProperty(String key) {
 		properties.remove(key);
 	}
 
 	/**
 	 * Restarts the job.
 	 *
 	 * Don't use that at the moment, it's not properly implemented yet.
 	 *
 	 * @throws JobSubmissionException
 	 * @throws JobPropertiesException
 	 */
 	public final void restartJob() throws JobSubmissionException,
 	JobPropertiesException {
 
 		addJobLogMessage("Restarting job...");
 
 		try {
 			serviceInterface.restartJob(getJobname(),
 					getJobDescriptionDocumentAsString());
 			addJobLogMessage("Job restarted.");
 		} catch (final NoSuchJobException e) {
 			addJobLogMessage("Job restart failed: " + e.getLocalizedMessage());
 			throw new JobSubmissionException("Could not find job on backend.",
 					e);
 		}
 		getStatus(true);
 
 	}
 
 	/**
 	 * Adds an (optional) description to the job.
 	 *
 	 * Be aware, this will only work if the job was not yet submitted.
 	 *
 	 * @param desc
 	 *            the description of the job (not the jdsl, mind)
 	 */
 	public void setDescription(String desc) {
 		this.description = desc;
 	}
 
 	private void setStatus(final int newStatus) {
 
 		final int oldstatus = this.status;
 		this.status = newStatus;
 
 		if (oldstatus != this.status) {
 			EventBus.publish(new JobStatusEvent(this, oldstatus, this.status));
 			if (StringUtils.isNotBlank(getJobname())) {
 				EventBus.publish(this.getJobname(), new JobStatusEvent(this,
 						oldstatus, this.status));
 			}
 		}
 
 	}
 
 	/**
 	 * Convenience method to create a unique jobname by appending a number to
 	 * the jobname (if necessary).
 	 *
 	 * @param jobname
 	 *            the base jobname
 	 */
 	@Transient
 	public void setUniqueJobname(final String jobname) {
 
 		if (StringUtils.isBlank(jobname)) {
 			setJobname(jobname);
 		} else {
 			final String newname = GrisuRegistryManager
 					.getDefault(serviceInterface).getUserEnvironmentManager()
 					.calculateUniqueJobname(jobname);
 			setJobname(newname);
 		}
 	}
 
 	/**
 	 * Stages in input files.
 	 *
 	 * Normally you don't have to call this method manually, it gets called just
 	 * before job submission automatically.
 	 *
 	 * @throws FileTransactionException
 	 *             if a file can't be staged in
 	 * @throws InterruptedException
 	 */
 	public final void stageFiles() throws FileTransactionException,
 	InterruptedException {
 
 		addJobLogMessage("Staging in files...");
 
 		if ((getInputFiles() != null) && (getInputFiles().size() > 0)) {
 			setStatus(JobConstants.INPUT_FILES_UPLOADING);
 		} else {
 			return;
 		}
 
 		final Set<String> localFiles = new HashSet<String>();
 		for (final String inputFile : getInputFiles().keySet()) {
 			if (FileManager.isLocal(inputFile)) {
 				localFiles.add(inputFile);
 			}
 		}
 
 		final Map<String, Set<String>> targets = new HashMap<String, Set<String>>();
 		for (final String f : localFiles) {
 			final String path = getInputFiles().get(f);
 			if (targets.get(path) == null) {
 				targets.put(path, new HashSet<String>());
 			}
 			targets.get(path).add(f);
 		}
 
 		final FileTransactionManager ftm = FileTransactionManager
 				.getDefault(serviceInterface);
 
 		for (final String target : targets.keySet()) {
 
 			final FileTransaction fileTransfer = ftm.addJobInputFileTransfer(
 					targets.get(target), this, target);
 			try {
 				fileTransfer.join();
 			} catch (final ExecutionException e) {
 				addJobLogMessage("Staging failed: " + e.getLocalizedMessage());
 				if (fileTransfer.getException() != null) {
 					throw fileTransfer.getException();
 				} else {
 					throw new FileTransactionException(
 							fileTransfer.getFailedSourceFile(), jobDirectory,
 							"File staging failed.", null);
 				}
 			}
 			if (!FileTransaction.Status.FINISHED.equals(fileTransfer
 					.getStatus())) {
 				if (fileTransfer.getException() != null) {
 					throw fileTransfer.getException();
 				} else {
 					throw new FileTransactionException(
 							fileTransfer.getFailedSourceFile(), jobDirectory,
 							"File staging failed.", null);
 				}
 			}
 
 		}
 
 		addJobLogMessage("Staging of input files finished.");
 
 		if ((getInputFiles() != null) && (getInputFiles().size() > 0)) {
 			setStatus(JobConstants.INPUT_FILES_UPLOADED);
 		}
 
 	}
 
 	// /**
 	// * Interrupts the {@link #waitForJobToFinish(int)} method.
 	// */
 	// public final void stopWaitingForJobToFinish() {
 	//
 	// if ((waitThread == null) || !waitThread.isAlive()) {
 	// return;
 	// }
 	//
 	// waitThread.interrupt();
 	// myLogger.debug("Wait thread interrupted.");
 	//
 	// }
 
 	/**
 	 * After you created the job on the backend using the
 	 * {@link #createJob(String)} or {@link #createJob(String, String)} method
 	 * you can tell the backend to actually submit the job to the endpoint
 	 * resource. Internally, this method also does possible stage-ins from your
 	 * local machine.
 	 *
 	 * @return the handle to the job submission task on the backend
 	 *
 	 * @throws JobSubmissionException
 	 *             if the job could not be submitted
 	 * @throws InterruptedException
 	 */
 	public final String submitJob() throws JobSubmissionException,
 	InterruptedException {
 		return submitJob(null);
 	}
 
 	/**
 	 * After you created the job on the backend using the
 	 * {@link #createJob(String)} or {@link #createJob(String, String)} method
 	 * you can tell the backend to actually submit the job to the endpoint
 	 * resource. Internally, this method also does possible stage-ins from your
 	 * local machine.
 	 *
 	 * @param waitForSubmissionToFinish
 	 *            whether to wait for submission to finish or not
 	 * @return the handle to the job submission task on the backend
 	 *
 	 * @throws JobSubmissionException
 	 *             if the job could not be submitted
 	 * @throws InterruptedException
 	 */
 	public final String submitJob(boolean waitForSubmissionToFinish)
 			throws JobSubmissionException, InterruptedException {
 		return submitJob(null, waitForSubmissionToFinish);
 	}
 
 	/**
 	 * After you created the job on the backend using the
 	 * {@link #createJob(String)} or {@link #createJob(String, String)} method
 	 * you can tell the backend to actually submit the job to the endpoint
 	 * resource. Internally, this method also does possible stage-ins from your
 	 * local machine.
 	 *
 	 * @param additionalJobProperties
 	 *            properties you want to store with the job (only get stored if
 	 *            submission was successful)
 	 * @return the handle to the job submission task on the backend
 	 * @throws JobSubmissionException
 	 *             if the job could not be submitted
 	 * @throws InterruptedException
 	 */
 	public final String submitJob(Map<String, String> additionalJobProperties)
 			throws JobSubmissionException, InterruptedException {
 		return submitJob(additionalJobProperties, true);
 	}
 
 	/**
 	 * After you created the job on the backend using the
 	 * {@link #createJob(String)} or {@link #createJob(String, String)} method
 	 * you can tell the backend to actually submit the job to the endpoint
 	 * resource. Internally, this method also does possible stage-ins from your
 	 * local machine.
 	 *
 	 * @param additionalJobProperties
 	 *            properties you want to store with the job (only get stored if
 	 *            submission was successful)
 	 * @param waitForSubmissionToFinish
 	 *            whether to wait for submission task to finish on backend
 	 *            before returning out of this method
 	 * @return the handle to the job submission task on the backend
 	 * @throws JobSubmissionException
 	 *             if the job could not be submitted
 	 * @throws InterruptedException
 	 */
 	public final String submitJob(Map<String, String> additionalJobProperties,
 			boolean waitForSubmissionToFinish) throws JobSubmissionException,
 			InterruptedException {
 
 		addJobLogMessage("Starting job submission...");
 
 		if (getStatus(true) == JobConstants.UNDEFINED) {
 			throw new IllegalStateException("Job state "
 					+ JobConstants.translateStatus(JobConstants.UNDEFINED)
 					+ ". Can't submit job.");
 		}
 
 		try {
 			stageFiles();
 		} catch (final FileTransactionException e) {
 			addJobLogMessage("Could not stage in file: "
 					+ e.getLocalizedMessage());
 			throw new JobSubmissionException("Could not stage in file.", e);
 		}
 
 		if (Thread.interrupted()) {
 			addJobLogMessage("Job submission interrupted.");
 			throw new InterruptedException(
 					"Interrupted after staging in input files.");
 		}
 
 		if (StringUtils.isNotBlank(description)) {
 			if (additionalJobProperties == null) {
 				additionalJobProperties = new HashMap<String, String>();
 			}
 			additionalJobProperties.put(Constants.JOB_DESCRIPTION_KEY,
 					description);
 		}
 
 		if (additionalJobProperties != null) {
 			properties.putAll(additionalJobProperties);
 		}
 
 		if (properties.size() > 0) {
 			addJobLogMessage("Setting additional job properties...");
 			try {
 				serviceInterface.addJobProperties(getJobname(),
 						DtoJob.createJob(-1, properties, null, null,
 								false));
 			} catch (final NoSuchJobException e) {
 				addJobLogMessage("Submission failed: "
 						+ e.getLocalizedMessage());
 				throw new JobSubmissionException(
 						"Could not find job on backend.", e);
 			}
 		}
 		allJobProperties = null;
 
 		String handle = null;
 		try {
 			addJobLogMessage("Submitting job to endpoint...");
 			handle = serviceInterface.submitJob(getJobname());
 			if (waitForSubmissionToFinish) {
 				try {
 					final StatusObject s = StatusObject.waitForActionToFinish(
 							serviceInterface, handle, 3, true);
 					if (s.getStatus().isFailed()) {
 						String errorCause = s.getStatus().getErrorCause();
 						if (StringUtils.isBlank(errorCause)) {
 							errorCause = "Unknown";
 						}
 						throw new JobSubmissionException(errorCause);
 					}
 				} catch (final StatusException e) {
 					myLogger.error(e.getLocalizedMessage(), e);
 					throw new RuntimeException(e);
 				}
 			}
 
 		} catch (final NoSuchJobException e) {
 			addJobLogMessage("Submission failed: " + e.getLocalizedMessage());
 			throw new JobSubmissionException("Could not find job on backend.",
 					e);
 		}
 
 		getStatus(true);
 
 		EventBus.publish(new NewJobEvent(this));
 
 		addJobLogMessage("Job submission finished successfully.");
 
 		return handle;
 	}
 
 	/**
 	 * Synchronizes the internally stored value for the location of the
 	 * jobdirectory with the backend.
 	 */
 	public void updateJobDirectory() {
 
 		try {
 			final String oldUrl = jobDirectory;
 			jobDirectory = serviceInterface.getJobProperty(getJobname(),
 					Constants.JOBDIRECTORY_KEY);
 			getStatus(true);
 			pcs.firePropertyChange("jobDirectory", oldUrl, jobDirectory);
 		} catch (final NoSuchJobException e) {
 			EventBus.publish(new JobStatusEvent(this, this.status,
 					JobConstants.NO_SUCH_JOB));
 			if (StringUtils.isNotBlank(getJobname())) {
 				EventBus.publish(this.getJobname(), new JobStatusEvent(this,
 						this.status, JobConstants.NO_SUCH_JOB));
 			}
 		}
 
 	}
 
 	public synchronized void updateWithDtoJob(DtoJob job) {
 
 		// if (!isArchived && !job.jobname().equals(getJobname())) {
 		// throw new IllegalArgumentException(
 		// "Jobname differs. Can't update job");
 		// }
 		allJobProperties = job.propertiesAsMap();
 		status = job.getStatus();
 		logMessages = job.getLogMessages().asMap();
 
 	}
 
 	/**
 	 * You can use this method to wait for the job to finish (either
 	 * successfully or not) on the endpoint resource.
 	 *
 	 * Use the int parameter to specify the sleep interval inbetween status
 	 * checks. Don't use a low number here please (except for testing) because
 	 * it could possibly cause a high load for the backend.
 	 *
 	 * @param checkIntervallInSeconds
 	 *            the interval inbetween status checks
 	 * @return whether the job is actually finished (true) or the this
 	 *         wait-thread was interrupted otherwise
 	 */
 	public final boolean waitForJobToFinish(final int checkIntervallInSeconds) {
 
 		waitForJobToReachState(JobConstants.FINISHED_EITHER_WAY,
 				checkIntervallInSeconds);
 
 		return isFinished();
 	}
 	public int waitForJobToReachState(int state, int checkIntervalInSeconds) {
 		addJobLogMessage("Waiting for job to reach state "+JobConstants.translateStatus(state) +"...");
 
 		Thread waitThread = waitThreads.get(state);
 
 		if (waitThread != null) {
 			if (waitThread.isAlive()) {
 				try {
 					waitThread.join();
 					return getStatus(true);
 				} catch (final InterruptedException e) {
 					myLogger.debug("Job status wait thread interrupted.");
 					return getStatus(true);
 				}
 			}
 		}
 
 		createWaitThread(state, checkIntervalInSeconds);
 		waitThread = waitThreads.get(state);
 
 		try {
 			waitThread.start();
 			waitThread.join();
 			waitThread = null;
 		} catch (final InterruptedException e) {
 			myLogger.debug("Job status wait thread interrupted.");
 			waitThread = null;
 			return getStatus(true);
 		}
 
 		return getStatus(true);
 	}
 
 	public void waitForJobToReachState(String state, int checkIntervalSeconds) {
 		waitForJobToReachState(JobConstants.translateStatusBack(state), checkIntervalSeconds);
 	}
 
 	/**
 	 * This is for internal display control, dont' set it manually.
 	 * 
 	 * @param whether the job is about to be cleaned or not.
 	 */
 	public void setBeingCleaned(boolean b) {
 
 		this.isBeingCleaned = true;
 		
 	}
 
 }
