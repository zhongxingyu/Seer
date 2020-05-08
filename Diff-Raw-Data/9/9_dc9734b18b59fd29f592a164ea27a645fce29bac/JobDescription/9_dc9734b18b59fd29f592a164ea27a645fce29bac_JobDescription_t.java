 package grisu.model.job;
 
 import grisu.control.JobnameHelpers;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.jcommons.utils.JsdlHelpers;
 import grisu.jcommons.utils.MemoryUtils;
 import grisu.jcommons.utils.OutputHelpers;
 import grisu.jcommons.utils.PackageFileHelper;
 import grisu.jcommons.utils.WalltimeUtils;
 import grisu.model.FileManager;
 import grisu.utils.SeveralXMLHelpers;
 import grisu.utils.SimpleJsdlBuilder;
 import grisu.utils.StringHelpers;
 import groovy.util.ConfigSlurper;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import com.Ostermiller.util.LineEnds;
 
 /**
  * A class that helps creating a job.
  * 
  * This class is extended by the JobObject class in the grisu-client package
  * which includes methods to create the job on the serviceinterface, submit and
  * monitor/control it.
  * 
  * @author Markus Binsteiner
  */
 @Entity
 public class JobDescription {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(JobDescription.class);
 
 	/**
 	 * Extracts the executable (first string) from a commandline.
 	 * 
 	 * @param commandline
 	 *            the commandline
 	 * @return the executable
 	 */
 	public static String extractExecutable(String commandline) {
 
 		if ((commandline == null) || (commandline.length() == 0)) {
 			return null;
 		}
 
 		final int i = commandline.indexOf(" ");
 		if (i <= 0) {
 			return commandline;
 		} else {
 			final String result = commandline.substring(0, i);
 			return result;
 		}
 	}
 
 	public static void main(final String[] args) throws JobPropertiesException {
 
 		final JobDescription jso = new JobDescription();
 
 		jso.setJobname("testJobName");
 		jso.setApplication("testApplication");
 		jso.setApplicationVersion("testVersion");
 		jso.setCommandline("java -testcommandline -argument2");
 		jso.setCpus(1);
 		jso.setWalltimeInSeconds(400);
 		jso.setEmail_address("testEmailAddress");
 		jso.setEmail_on_job_start(true);
 		jso.setEmail_on_job_finish(true);
 		jso.setForce_mpi(true);
 		jso.setForce_single(false);
 		// jso.setInputFileUrls(new String[] { "file:///temp/test",
 		// "gsiftp://ng2.vpac.org/tmp/test" });
 		jso.setMemory(0L);
 
 		jso.getJobDescriptionDocument();
 
 	}
 
 	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	private Long id;
 
 	protected String jobname;
 
 	private String application = Constants.GENERIC_APPLICATION_NAME;
 
 	private String applicationVersion = Constants.NO_VERSION_INDICATOR_STRING;
 
 	private String email_address;
 
 	private boolean email_on_job_start = false;
 
 	private boolean email_on_job_finish = false;
 
 	private int cpus = 1;
 
 	private boolean force_single = false;
 
 	private boolean force_mpi = false;
 
 	private int hostcount = 0;
 
 	private long memory_in_bytes = 0;
 
 	private int walltime_in_seconds = 0;
 
 	private Map<String, String> inputFiles = new HashMap<String, String>();
 
 	private Map<String, String> envVariables = new HashMap<String, String>();
 
 	private Set<String> modules = new HashSet<String>();
 
 	private String submissionLocation;
 
 	private String commandline;
 
 	private String stderr;
 
 	private String stdout;
 
 	private String stdin;
 
 	private String pbsDebug;
 
 	private long virtual_memory_in_bytes = 0;
 
 	/**
 	 * Default constructor.
 	 * 
 	 */
 	public JobDescription() {
 	}
 
 	/**
 	 * Constructor to clone a JobSubmissionObjectImpl from an existing jsdl
 	 * document.
 	 * 
 	 * @param jsdl
 	 *            a (valid) jsdl document
 	 */
 	public JobDescription(final Document jsdl) {
 
 		initWithDocument(jsdl);
 
 	}
 
 	public JobDescription(final File jobPropertiesFile) throws Exception {
 		this(new ConfigSlurper().parse(jobPropertiesFile.toURL()).flatten());
 	}
 
 	/**
 	 * Constructor to clone a JobSubmissionObjectImpl from a set of job
 	 * properties.
 	 * 
 	 * @param jobProperties
 	 *            the properties
 	 */
 	public JobDescription(final Map<String, String> jobProperties) {
 		initWithMap(jobProperties);
 	}
 
 	/**
 	 * Wrapper constructor that calls either
 	 * {@link JobDescription#JobSubmissionObjectImpl(Document)} or
 	 * {@link JobDescription#JobSubmissionObjectImpl(Map)}, depending on the
 	 * type of Object you provide.
 	 * 
 	 * @param o
 	 *            either a jsdl document or a Map of job properties
 	 */
 	public JobDescription(final Object o) {
 		if (o instanceof Document) {
 			initWithDocument((Document) o);
 		} else if (o instanceof Map<?, ?>) {
 			initWithMap((Map<String, String>) o);
 		}
 	}
 
 	/**
 	 * Adds an environment variable for the job environment.
 	 * 
 	 * @param key
 	 *            the key of the variable
 	 * @param value
 	 *            the value
 	 */
 	public void addEnvironmentVariable(String key, String value) {
 
 		envVariables.put(key, value);
 		pcs.firePropertyChange("environmentVariables", null, this.envVariables);
 	}
 	
 	/**
 	 * Creates a temporary text file from the text content and adds it to the job.
 	 * 
 	 * @param content the content
 	 * @param filename the filename
 	 */
 	public void addInputTextFile(String content, String filename) {
 		
 		File tempFile = PackageFileHelper.createTempFile(content, filename);
 		
 		addInputFile(tempFile);
 		
 	}
 
 	/**
 	 * Adds a local input file to this job.
 	 * 
 	 * @param file
 	 *            the file to add
 	 */
 	public void addInputFile(File file) {
 		addInputFileUrl(file.getAbsolutePath());
 	}
 
 	/**
 	 * Adds an input file to this job.
 	 * 
 	 * You can provide a url
 	 * (gsiftp://ng2.ceres.auckland.ac.nz/home/markus/test.txt), a local path
 	 * (/home/markus/test.txt) or a local url (file:///home/markus/test.txt)
 	 * here. The file will be attached to the job and either uploaded or copied
 	 * across into the root of the job directory with the original name.
 	 * 
 	 * @param url
 	 *            a remote url, local path or local url
 	 */
 	public void addInputFileUrl(String url) {
 
 		addInputFileUrl(url, "");
 	}
 
 	/**
 	 * Adds an input file to this job.
 	 * 
 	 * Works like {@link #addInputFileUrl(String)} except that you can specify
 	 * the new name/path the file will have in the job directory. If you, for
 	 * example, upload a file "test.txt" and specify "/inputFiles/file1.txt" the
 	 * file will be in a folder "inputFiles" and named file1.txt".
 	 * 
 	 * @param url
 	 *            a remote or local url or a local path
 	 * @param targetPath
 	 *            the remote path under the jobdirectory of the file
 	 */
 	public void addInputFileUrl(String url, String targetPath) {
 		if (StringUtils.isBlank(url)) {
 			return;
 		}
 
 		url = FileManager.ensureUriFormat(url);
 		// final String[] oldValue = getInputFileUrls().k;
 		this.inputFiles.put(url, targetPath);
 		pcs.firePropertyChange("inputFiles", null, this.inputFiles);
 	}
 
 	/**
 	 * Adds a module to the modules you want to load for this job (on the
 	 * compute resource).
 	 * 
 	 * For that, generally, you need to specify a fixed submissionLocation and
 	 * you also need to know the exact name of the module you want to load (the
 	 * names of a module to load for the same application can differ from site
 	 * to site). Usually you would only specify application name and version and
 	 * let Grisu figure out the name of the module...
 	 * 
 	 * @param module
 	 */
 	public void addModule(final String module) {
 		this.modules.add(module);
 		pcs.firePropertyChange("modules", null, this.modules);
 	}
 
 	/**
 	 * Adds a property change listener.
 	 * 
 	 * Usually used in GUIs to re-validate a jobsubmission object for certain
 	 * applications, i.e. whether the provided input parameters for a job make
 	 * sense for this application.
 	 * 
 	 * @param listener
 	 *            the property change listener
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	private boolean checkForBoolean(final String booleanString) {
 		if (booleanString == null) {
 			return false;
 		}
 
 		if ("true".equals(booleanString.toLowerCase())
 				|| "on".equals(booleanString.toLowerCase())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private void checkValidity() throws JobPropertiesException {
 
 		if ((commandline == null) || (commandline.length() == 0)) {
 			throw new JobPropertiesException(
 					JobSubmissionProperty.COMMANDLINE.toString() + ": "
 							+ "Commandline not specified.");
 		}
 
 	}
 
 	@Override
 	public boolean equals(Object other) {
 
 		if (other instanceof JobDescription) {
 			final JobDescription otherJob = (JobDescription) other;
 			return getJobname().equals(otherJob.getJobname());
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * This method parses the currently set commandline and extracts the name of
 	 * the executable.
 	 * 
 	 * @return the name of the currently set executable
 	 */
 	public String extractExecutable() {
 		return extractExecutable(commandline);
 	}
 
 	/**
 	 * Returns the name of the currently set application.
 	 * 
 	 * @return the applicationname
 	 */
 	public String getApplication() {
 		return application;
 	}
 
 	/**
 	 * Returns the currently set application version.
 	 * 
 	 * @return the application version
 	 */
 	public String getApplicationVersion() {
 		if (StringUtils.isBlank(applicationVersion)) {
 			return Constants.NO_VERSION_INDICATOR_STRING;
 		}
 		return applicationVersion;
 	}
 
 	/**
 	 * Returns the currently set commandline of the job.
 	 * 
 	 * @return the commandline
 	 */
 	@Column(nullable = false)
 	public String getCommandline() {
 		return commandline;
 	}
 
 	/**
 	 * Returns the currently set number of cpus.
 	 * 
 	 * @return the number of cpus to be used for this job
 	 */
 	public Integer getCpus() {
 		return cpus;
 	}
 
 	/**
 	 * Gets the email address to be used if this job is configured to send
 	 * emails when starting/finished.
 	 * 
 	 * @return the email address or null
 	 */
 	public String getEmail_address() {
 		return email_address;
 	}
 
 	@ElementCollection(fetch = FetchType.EAGER)
 	public Map<String, String> getEnvironmentVariables() {
 		return envVariables;
 	}
 
 	/**
 	 * Extracts the executable from the commandline.
 	 * 
 	 * @return the executable
 	 */
 	@Transient
 	public String getExecutable() {
 		return extractExecutable();
 	}
 
 	/**
 	 * Returns the currently set hostcount.
 	 * 
 	 * You can use this to force the job to be run on only 1 node (to use the
 	 * shared memory). Often used in combination with {@link #force_single} and
 	 * {@link #force_mpi}.
 	 * 
 	 * @return the host count
 	 */
 	public Integer getHostCount() {
 		return hostcount;
 	}
 
 	@Id
 	@GeneratedValue
 	private Long getId() {
 		return this.id;
 	}
 
 	/**
 	 * Returns all input files associated with this job, with they keys being
 	 * the source urls and values are the remote paths for the files (or null
 	 * for default remote path).
 	 * 
 	 * @return the input files
 	 */
 	@ElementCollection(fetch = FetchType.EAGER)
 	public Map<String, String> getInputFiles() {
 		return this.inputFiles;
 	}
 
 	/**
 	 * Creates a jsdl document for the currently set job properties.
 	 * 
 	 * @return a jsdl document.
 	 * @throws JobPropertiesException
 	 *             if the jsdl document can't be created because of incorrect
 	 *             settings
 	 */
 	@Transient
 	public final Document getJobDescriptionDocument()
 			throws JobPropertiesException {
 
 		checkValidity();
 
 		final Map<JobSubmissionProperty, String> jobProperties = getJobSubmissionPropertyMap();
 
 		final Document jsdl = SimpleJsdlBuilder.buildJsdl(jobProperties);
 
 		return jsdl;
 
 	}
 
 	/**
 	 * Convenience method to get the output of
 	 * {@link #getJobDescriptionDocument()} as a string.
 	 * 
 	 * @return the jsdl document as a string
 	 * @throws JobPropertiesException
 	 *             if the jsdl document can't be created because of incorrect
 	 *             settings
 	 */
 	@Transient
 	public final String getJobDescriptionDocumentAsString()
 			throws JobPropertiesException {
 
 		String jsdlString = null;
 		jsdlString = SeveralXMLHelpers.toString(getJobDescriptionDocument());
 
 		return jsdlString;
 	}
 
 	/**
 	 * Returns the currently set name of the job.
 	 * 
 	 * @return the jobname
 	 */
 	public String getJobname() {
 		if (StringUtils.isBlank(jobname)) {
 			return Constants.NO_JOBNAME_INDICATOR_STRING;
 		}
 		return jobname;
 	}
 
 	/**
 	 * Creates a map for the currently set job properties.
 	 * 
 	 * @return a map of job properties
 	 */
 	@Transient
 	public final Map<JobSubmissionProperty, String> getJobSubmissionPropertyMap() {
 
 		final Map<JobSubmissionProperty, String> jobProperties = new HashMap<JobSubmissionProperty, String>();
 		jobProperties.put(JobSubmissionProperty.JOBNAME, jobname);
 		jobProperties.put(JobSubmissionProperty.APPLICATIONNAME, application);
 		jobProperties.put(JobSubmissionProperty.APPLICATIONVERSION,
 				applicationVersion);
 		jobProperties.put(JobSubmissionProperty.COMMANDLINE, commandline);
 		jobProperties.put(JobSubmissionProperty.EMAIL_ADDRESS, email_address);
 		if (email_on_job_start) {
 			jobProperties.put(JobSubmissionProperty.EMAIL_ON_START, "true");
 		} else {
 			jobProperties.put(JobSubmissionProperty.EMAIL_ON_START, "false");
 		}
 		if (email_on_job_finish) {
 			jobProperties.put(JobSubmissionProperty.EMAIL_ON_FINISH, "true");
 		} else {
 			jobProperties.put(JobSubmissionProperty.EMAIL_ON_FINISH, "false");
 		}
 		if (force_single) {
 			jobProperties.put(JobSubmissionProperty.FORCE_SINGLE, "true");
 			jobProperties.put(JobSubmissionProperty.FORCE_MPI, "false");
 		} else if (force_mpi) {
 			jobProperties.put(JobSubmissionProperty.FORCE_SINGLE, "false");
 			jobProperties.put(JobSubmissionProperty.FORCE_MPI, "true");
 		}
 		jobProperties.put(JobSubmissionProperty.INPUT_FILE_URLS,
 				StringHelpers.mapToString(getInputFiles()));
 		jobProperties.put(JobSubmissionProperty.ENVIRONMENT_VARIABLES,
 				StringHelpers.mapToString(getEnvironmentVariables()));
 		jobProperties.put(JobSubmissionProperty.MODULES, getModulesAsString());
 		jobProperties.put(JobSubmissionProperty.MEMORY_IN_B, new Long(
 				memory_in_bytes).toString());
 		jobProperties.put(JobSubmissionProperty.NO_CPUS,
 				new Integer(cpus).toString());
 		jobProperties.put(JobSubmissionProperty.HOSTCOUNT, new Integer(
 				getHostCount()).toString());
 		jobProperties.put(JobSubmissionProperty.STDERR, stderr);
 		jobProperties.put(JobSubmissionProperty.STDOUT, stdout);
 		jobProperties.put(JobSubmissionProperty.SUBMISSIONLOCATION,
 				submissionLocation);
 		jobProperties.put(JobSubmissionProperty.WALLTIME_IN_MINUTES,
 				new Integer(walltime_in_seconds / 60).toString());
 		jobProperties.put(JobSubmissionProperty.PBSDEBUG, pbsDebug);
 
 		return jobProperties;
 	}
 
 	/**
 	 * Returns the currently set amount of memory for the job (in bytes).
 	 * 
 	 * @return the memory
 	 */
 	public Long getMemory() {
 		return memory_in_bytes;
 	}
 
 	/**
 	 * Returns all currently set modules for this job.
 	 * 
 	 * @return the modules
 	 */
 	public String[] getModules() {
 		return modules.toArray(new String[] {});
 	}
 
 	/**
 	 * Convenience method that returns the output of {@link #getModules()} as a
 	 * comma seperated string.
 	 * 
 	 * @return the modules as a string
 	 */
 	@Transient
 	public String getModulesAsString() {
 		if ((modules != null) && (modules.size() != 0)) {
 			return StringUtils.join(modules, ",");
 		} else {
 			return new String();
 		}
 	}
 
 	/**
 	 * Returns whether the pbsDebug value is set or not.
 	 * 
 	 * @return the pbsDebug value
 	 */
 	public String getPbsDebug() {
 		return pbsDebug;
 	}
 
 	/**
 	 * Returns the name of the currently set name of the stderr file.
 	 * 
 	 * @return the stderr file
 	 */
 	public String getStderr() {
 		return stderr;
 	}
 
 	/**
 	 * Returns the name of the currently set name of the stdin file.
 	 * 
 	 * @return the stdin file
 	 */
 	public String getStdin() {
 		return this.stdin;
 	}
 
 	/**
 	 * Returns the name of the currently set name of the stdout file.
 	 * 
 	 * @return the stdout file
 	 */
 	public String getStdout() {
 		return stdout;
 	}
 
 	/**
 	 * Convenience method to return the result of the
 	 * {@link #getJobSubmissionPropertyMap()} as a map with the keys being
 	 * stings.
 	 * 
 	 * @return the job properties
 	 */
 	@Transient
 	public final Map<String, String> getStringJobSubmissionPropertyMap() {
 
 		final Map<String, String> stringPropertyMap = new HashMap<String, String>();
 		final Map<JobSubmissionProperty, String> jobPropertyMap = getJobSubmissionPropertyMap();
 
 		for (final JobSubmissionProperty jp : jobPropertyMap.keySet()) {
 			final String value = jobPropertyMap.get(jp);
 			stringPropertyMap.put(jp.toString(), value);
 
 		}
 		return stringPropertyMap;
 	}
 
 	@Transient
 	public final String getJobPropertyMapAsString() {
 
 		Map<String, String> map = getStringJobSubmissionPropertyMap();
 		return OutputHelpers.getTable(map);
 
 	}
 
 	/**
 	 * Returns the currently set submission location.
 	 * 
 	 * @return the submission location
 	 */
 	public String getSubmissionLocation() {
 		return submissionLocation;
 	}
 
 	/**
 	 * The virtal memory for the job in bytes.
 	 * 
 	 * @return the virtual memory
 	 */
 	public Long getVirtualMemory() {
 		return virtual_memory_in_bytes;
 	}
 
 	/**
 	 * Returns the walltime in seconds.
 	 * 
 	 * @return the walltime in seconds
 	 * @deprecated use {@link #getWalltimeInSeconds()} instead
 	 */
 	@Deprecated
 	public int getWalltime() {
 		return getWalltimeInSeconds();
 	}
 
 	/**
 	 * Returns the walltime in seconds.
 	 * 
 	 * @return the walltime in seconds
 	 */
 	public int getWalltimeInSeconds() {
 		return walltime_in_seconds;
 	}
 
 	@Override
 	public int hashCode() {
 		return 73 * getJobname().hashCode();
 	}
 
 	private void initWithDocument(Document jsdl) {
 		jobname = JsdlHelpers.getJobname(jsdl);
 		application = JsdlHelpers.getApplicationName(jsdl);
 		applicationVersion = JsdlHelpers.getApplicationVersion(jsdl);
 		email_address = JsdlHelpers.getEmail(jsdl);
 		email_on_job_start = JsdlHelpers.getSendEmailOnJobStart(jsdl);
 		email_on_job_finish = JsdlHelpers.getSendEmailOnJobFinish(jsdl);
 		cpus = JsdlHelpers.getProcessorCount(jsdl);
 		hostcount = JsdlHelpers.getResourceCount(jsdl);
 		final String jobTypeString = JsdlHelpers.getArcsJobType(jsdl);
 		if (jobTypeString != null) {
 			if (jobTypeString.toLowerCase().equals(
 					JobSubmissionProperty
 							.getPrettyName(JobSubmissionProperty.FORCE_SINGLE
 									.toString()))) {
 				force_single = true;
 				force_mpi = false;
 			} else if (jobTypeString.toLowerCase().equals(
 					JobSubmissionProperty
 							.getPrettyName(JobSubmissionProperty.FORCE_MPI
 									.toString()))) {
 				force_single = false;
 				force_mpi = true;
 			} else {
 				force_single = false;
 				force_mpi = false;
 			}
 		} else {
 			force_single = false;
 			force_mpi = false;
 		}
 		memory_in_bytes = JsdlHelpers.getTotalMemoryRequirement(jsdl);
 		walltime_in_seconds = JsdlHelpers.getWalltime(jsdl);
 
 		final String[] temp = JsdlHelpers.getInputFileUrls(jsdl);
 		if (temp != null) {
 			final Map<String, String> inf = new LinkedHashMap<String, String>();
 			for (final String s : temp) {
 				inf.put(s, "");
 			}
 			setInputFiles(inf);
 		}
 
 		setModules(JsdlHelpers.getModules(jsdl));
 		final String[] candidateHosts = JsdlHelpers.getCandidateHosts(jsdl);
 		if ((candidateHosts != null) && (candidateHosts.length > 0)) {
 			submissionLocation = candidateHosts[0];
 		}
 		final String executable = JsdlHelpers
 				.getPosixApplicationExecutable(jsdl);
 		final String[] arguments = JsdlHelpers
 				.getPosixApplicationArguments(jsdl);
 		final StringBuffer tempBuffer = new StringBuffer(executable);
 		if (arguments != null) {
 			for (final String arg : arguments) {
 				tempBuffer.append(" " + arg);
 			}
 		}
 		commandline = tempBuffer.toString();
 		stderr = JsdlHelpers.getPosixStandardError(jsdl);
 		stdout = JsdlHelpers.getPosixStandardOutput(jsdl);
 		stdin = JsdlHelpers.getPosixStandardInput(jsdl);
 		pbsDebug = JsdlHelpers.getPbsDebugElement(jsdl);
 	}
 
 	private void initWithMap(Map<String, String> jobProperties) {
 		this.jobname = jobProperties.get(JobSubmissionProperty.JOBNAME
 				.toString());
 		this.application = jobProperties
 				.get(JobSubmissionProperty.APPLICATIONNAME.toString());
 		this.applicationVersion = jobProperties
 				.get(JobSubmissionProperty.APPLICATIONVERSION.toString());
 		this.email_address = jobProperties
 				.get(JobSubmissionProperty.EMAIL_ADDRESS.toString());
 		this.email_on_job_start = checkForBoolean(jobProperties
 				.get(JobSubmissionProperty.EMAIL_ON_START.toString()));
 		this.email_on_job_finish = checkForBoolean(jobProperties
 				.get(JobSubmissionProperty.EMAIL_ON_FINISH.toString()));
 		try {
 			this.cpus = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.NO_CPUS.toString()));
 		} catch (final NumberFormatException e) {
 			this.cpus = 1;
 		}
 		try {
 			this.hostcount = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.HOSTCOUNT.toString()));
 		} catch (final Exception e) {
 			this.hostcount = 0;
 		}
 
 		try {
 			this.force_single = checkForBoolean(jobProperties
 					.get(JobSubmissionProperty.FORCE_SINGLE.toString()));
 		} catch (final Exception e) {
 			this.force_single = false;
 		}
 		try {
 			this.force_mpi = checkForBoolean(jobProperties
 					.get(JobSubmissionProperty.FORCE_MPI.toString()));
 		} catch (final Exception e) {
 			this.force_mpi = false;
 		}
 
 		try {
 			this.memory_in_bytes = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.MEMORY_IN_B.toString()));
 		} catch (final NumberFormatException e) {
 			this.memory_in_bytes = 0;
 		}
 		String temp_walltime = jobProperties.get(JobSubmissionProperty.WALLTIME_IN_MINUTES.toString());
 		try {
 			this.walltime_in_seconds = Integer.parseInt(temp_walltime) * 60;
 		} catch (final NumberFormatException e) {
 			try {
 			// try walltimeUtils
 				this.walltime_in_seconds = WalltimeUtils.fromShortStringToSeconds(temp_walltime);
 			} catch (Exception e2) {
 				this.walltime_in_seconds = 0;				
 			}
 
 		}
 
 		String temp = jobProperties.get(JobSubmissionProperty.INPUT_FILE_URLS
 				.toString());
 
 		if (StringUtils.isNotBlank(temp)) {
 			final String[] files = temp.split(",");
 			final Map<String, String> m = new HashMap<String, String>();
 			for (final String f : files) {
 				m.put(f, "");
 			}
 			setInputFiles(m);
 			// setInputFileUrls(temp.split(","));
 		}
 
 		temp = jobProperties.get(JobSubmissionProperty.ENVIRONMENT_VARIABLES
 				.toString());
 		if (StringUtils.isNotBlank(temp)) {
 			final Map<String, String> envVariables = StringHelpers
 					.stringToMap(temp);
 			if (envVariables.size() > 0) {
 				setEnvironmentVariables(envVariables);
 			}
 		}
 
 		temp = jobProperties.get(JobSubmissionProperty.MODULES.toString());
 		if ((temp != null) && (temp.length() > 0)) {
 			setModules(temp.split(","));
 		}
 
 		this.submissionLocation = jobProperties
 				.get(JobSubmissionProperty.SUBMISSIONLOCATION.toString());
 		this.commandline = jobProperties.get(JobSubmissionProperty.COMMANDLINE
 				.toString());
 		this.stderr = jobProperties
 				.get(JobSubmissionProperty.STDERR.toString());
 		this.stdout = jobProperties
 				.get(JobSubmissionProperty.STDOUT.toString());
 		this.stdin = jobProperties.get(JobSubmissionProperty.STDIN.toString());
 
 		this.pbsDebug = jobProperties.get(JobSubmissionProperty.PBSDEBUG
 				.toString());
 		
 	}
 
 	/**
 	 * Returns whether this job is configured to send an email when it finishes.
 	 * 
 	 * @return whether to send an email when job is finished (true) or not
 	 *         (false)
 	 */
 	public Boolean isEmail_on_job_finish() {
 		return email_on_job_finish;
 	}
 
 	/**
 	 * Returns whether this job is configured to send an email when it starts on
 	 * the compute resource.
 	 * 
 	 * @return whether to send an email when job starts (true) or not (false)
 	 */
 	public Boolean isEmail_on_job_start() {
 		return email_on_job_start;
 	}
 
 	/**
 	 * Whether this job is forced to be an mpi job or not.
 	 * 
 	 * This overrides Grisus' auto-jobtype determination which selects "single"
 	 * for 1 cpu and "mpi" for >1 cpu jobs.
 	 * 
 	 * @return the force mpi value
 	 */
 	public Boolean isForce_mpi() {
 		return force_mpi;
 	}
 
 	/**
 	 * Whether this job is forced to be of the "single" jobtype or not.
 	 * 
 	 * This overrides Grisus' auto-jobtype determination which selects "single"
 	 * for 1 cpu and "mpi" for >1 cpu jobs.
 	 * 
 	 * @return the force single value
 	 */
 	public Boolean isForce_single() {
 		return force_single;
 	}
 
 	/**
 	 * Removes the specified environment variable from the job.
 	 * 
 	 * @param var
 	 *            the key of the variable to remove
 	 */
 	public void removeEnvironmentVariable(String var) {
 
 		if (StringUtils.isBlank(var)) {
 			return;
 		}
 		this.envVariables.remove(var);
 		pcs.firePropertyChange("environmentVariables", null, this.envVariables);
 	}
 
 	/**
 	 * Removes the specified url/path from the input values.
 	 * 
 	 * @param selectedFile
 	 *            the url/path to remove
 	 */
 	public void removeInputFileUrl(String selectedFile) {
 
 		if (StringUtils.isBlank(selectedFile)) {
 			return;
 		}
 		this.inputFiles.remove(selectedFile);
 		pcs.firePropertyChange("inputFileUrls", null, this.inputFiles);
 	}
 
 	/**
 	 * Removes a property change listener.
 	 * 
 	 * @param listener
 	 *            the listener
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * Sets the name of the application to use for this job.
 	 * 
 	 * You need to specify the exact same name that is used to publish this
 	 * application in mds/bdii.
 	 * 
 	 * @param app
 	 *            the application name
 	 */
 	public void setApplication(final String app) {
 		final String oldValue = this.application;
 		this.application = app;
 		pcs.firePropertyChange("application", oldValue,
 				JobDescription.this.application);
 	}
 
 	/**
 	 * Sets the version of the application to use for this job.
 	 * 
 	 * You need to specify the exact same name that is used to publish this
 	 * applicationversion in mds/bdii.
 	 * 
 	 * @param appVersion
 	 *            the version of the application
 	 */
 	public void setApplicationVersion(final String appVersion) {
 		final String oldValue = this.applicationVersion;
 		this.applicationVersion = appVersion;
 		pcs.firePropertyChange("applicationVersion", oldValue,
 				this.applicationVersion);
 	}
 
 	/**
 	 * Sets the commandline to be used for this job.
 	 * 
 	 * Set the commandline like you would when running the application on your
 	 * local machine. For example: "cat test.txt". Don't specify any possible
 	 * mpi commands, that is done automatically for you.
 	 * 
 	 * @param commandline
 	 *            the commandline
 	 */
 	public synchronized void setCommandline(final String commandline) {
 		final String oldValue = this.commandline;
 		final String oldExe = extractExecutable(this.commandline);
 		this.commandline = commandline;
 		myLogger.debug("Commandline for job: " + getJobname() + " changed: "
 				+ commandline);
 		pcs.firePropertyChange("commandline", oldValue, this.commandline);
 		final String newExe = extractExecutable();
 		pcs.firePropertyChange("executable", oldExe, newExe);
 	}
 
 	/**
 	 * Sets the number of cpus to use for this job.
 	 * 
 	 * This also determines the job type (if not force by {@link #force_single}
 	 * or {@link #force_mpi}): single for 1 and mpi for >1.
 	 * 
 	 * @param cpus
 	 *            the number of cpus
 	 */
 	public void setCpus(final Integer cpus) {
 		final int oldValue = this.cpus;
 		this.cpus = cpus;
 		pcs.firePropertyChange("cpus", oldValue, this.cpus);
 	}
 
 	/**
 	 * Sets the email address to send emails to when job is started/finished.
 	 * 
 	 * Only useful in combination with {@link #setEmail_on_job_start(Boolean)}
 	 * and/or {@link #setEmail_on_job_finish(Boolean)}.
 	 * 
 	 * @param email_address
 	 *            the email address
 	 */
 	public void setEmail_address(final String email_address) {
 		final String oldValue = this.email_address;
 		this.email_address = email_address;
 		pcs.firePropertyChange("email_address", oldValue, this.email_address);
 	}
 
 	/**
 	 * Sets whether to send an email once the job finished.
 	 * 
 	 * Sends an email to the address configured in
 	 * {@link #setEmail_address(String)}.
 	 * 
 	 * @param email_on_job_finish
 	 *            whether to send an email when job is finished (true) or not
 	 *            (false)
 	 */
 	public void setEmail_on_job_finish(final Boolean email_on_job_finish) {
 		final boolean oldValue = this.email_on_job_finish;
 		this.email_on_job_finish = email_on_job_finish;
 		pcs.firePropertyChange("email_on_job_finish", oldValue,
 				this.email_on_job_finish);
 	}
 
 	/**
 	 * Sets whether to send an email once the job is started on the compute
 	 * resource.
 	 * 
 	 * Sends an email to the address configured in
 	 * {@link #setEmail_address(String)}.
 	 * 
 	 * @param email_on_job_start
 	 *            whether to send an email when job is started (true) or not
 	 *            (false)
 	 */
 	public void setEmail_on_job_start(final Boolean email_on_job_start) {
 		final boolean oldValue = this.email_on_job_start;
 		this.email_on_job_start = email_on_job_start;
 		pcs.firePropertyChange("email_on_job_start", oldValue,
 				this.email_on_job_start);
 	}
 
 	public void setEnvironmentVariables(Map<String, String> vars) {
 		this.envVariables = vars;
 		pcs.firePropertyChange("environmentVariables", null, this.envVariables);
 	}
 
 	/**
 	 * Sets whether to force the jobtype to be "mpi" even if the cpu count is
 	 * configured to be 1.
 	 * 
 	 * @param force_mpi
 	 *            whether to force the jobtype to be "mpi"
 	 */
 	public void setForce_mpi(final Boolean force_mpi) {
 		final boolean oldValue = this.force_mpi;
 		final boolean oldValue1 = this.force_single;
 //		final int oldHostCount = this.hostcount;
 		this.force_mpi = force_mpi;
 		this.force_single = !force_mpi;
 //		this.hostcount = -1;
 		pcs.firePropertyChange("force_mpi", oldValue, this.force_mpi);
 		pcs.firePropertyChange("force_single", oldValue1, this.force_single);
 //		pcs.firePropertyChange("hostCount", oldHostCount, this.hostcount);
 	}
 
 	/**
 	 * Sets whether to force the jobtype to be "single" even if the cpu count is
 	 * configured to be >1.
 	 * 
 	 * Mostly used in combination with setting the hostcount to 1 so that the
 	 * job is run "threaded".
 	 * 
 	 * @param force_single
 	 *            whether to force the jobtype to be "single"
 	 */
 	public void setForce_single(final Boolean force_single) {
 		final boolean oldValue = this.force_mpi;
 		final boolean oldValue1 = this.force_single;
 		this.force_single = force_single;
 		this.force_mpi = !force_single;
 		pcs.firePropertyChange("force_mpi", oldValue, this.force_mpi);
 		pcs.firePropertyChange("force_single", oldValue1, this.force_single);
 	}
 
 	/**
 	 * Sets the amount of nodes the job is allowed to run on.
 	 * 
 	 * Mostly used to run a "threaded" job (a job that is run on 1 host but
 	 * multiple cpus).
 	 * 
 	 * @param hc
 	 *            the host count
 	 */
 	public void setHostCount(Integer hc) {
 		final int oldHostCount = this.hostcount;
 		this.hostcount = hc;
 		pcs.firePropertyChange("hostCount", oldHostCount, this.hostcount);
 	}
 
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	// public void setInputFileUrls(final String[] inputFileUrls) {
 	// final Set<String> oldValue = this.inputFiles;
 	// if (inputFileUrls != null) {
 	// this.inputFiles = new HashSet<String>(Arrays.asList(inputFileUrls));
 	// } else {
 	// this.inputFiles = new HashSet<String>();
 	// }
 	// pcs.firePropertyChange("inputFileUrls", oldValue, this.inputFiles);
 	// }
 
 	/**
 	 * Sets all input files in one go.
 	 * 
 	 * Keys are the source urls/paths, values are the target paths. You can also
 	 * use the {@link JobDescription#addInputFileUrl(String)} or
 	 * {@link #addInputFileUrl(String, String)} methods.
 	 * 
 	 * @param inputfiles
 	 */
 	public void setInputFiles(final Map<String, String> inputfiles) {
 		this.inputFiles = inputfiles;
 
 		pcs.firePropertyChange("inputFiles", null, this.inputFiles);
 
 	}
 
 	/**
 	 * Sets the name of the job.
 	 * 
 	 * @param jobname
 	 *            the jobname
 	 */
 	public void setJobname(final String jobname) {
 		final String oldValue = this.jobname;
 		this.jobname = jobname;
 		pcs.firePropertyChange("jobname", oldValue, this.jobname);
 	}
 
 	/**
 	 * Sets the memory to be used for this job (in bytes).
 	 * 
 	 * @param memory
 	 *            the memory
 	 */
 	public void setMemory(final Long memory) {
 		final long oldValue = this.memory_in_bytes;
 		this.memory_in_bytes = memory;
 		pcs.firePropertyChange("memory", oldValue, this.memory_in_bytes);
 	}
 
 	@Transient
 	public void setMemory(final String memoryString) {
 		Long m = MemoryUtils.fromStringToMegaBytes(memoryString);
 		setMemory(m * 1024 * 1024);
 	}
 
 	/**
 	 * Sets the modules to be used for this job.
 	 * 
 	 * Usually, when setting this you need to have a fixed submission location
 	 * and you also have to know the exact name of the modules on this
 	 * submission location. It is recommended that you only set application name
 	 * and version and let Grisu figure out the module names automaticatlly.
 	 * 
 	 * @param modules
 	 *            the module names
 	 */
 	public void setModules(final String[] modules) {
 		final Set<String> oldValue = this.modules;
 		if (modules != null) {
 			this.modules = new HashSet<String>(Arrays.asList(modules));
 		} else {
 			this.modules = new HashSet<String>();
 		}
 		pcs.firePropertyChange("modules", oldValue, this.modules);
 	}
 
 	/**
 	 * Whether to enable the pbsdebug option for this job.
 	 * 
 	 * This is only useful for jobs to submission location which support the pbs
 	 * debug option. Basically, the generated pbs script is written into the job
 	 * directory.
 	 * 
 	 * @param pbsDebug
 	 */
 	public void setPbsDebug(String pbsDebug) {
 		final String oldValue = this.pbsDebug;
 		this.pbsDebug = pbsDebug;
 		pcs.firePropertyChange("pbsDebug", oldValue, this.pbsDebug);
 	}
 
 	/**
 	 * Sets the name of the stderr file for this job.
 	 * 
 	 * @param stderr
 	 *            the stderr file
 	 */
 	public void setStderr(final String stderr) {
 		final String oldValue = this.stderr;
 		this.stderr = stderr;
 		pcs.firePropertyChange("stderr", oldValue, this.stderr);
 	}
 
 	/**
 	 * Sets the name of the stdin file for this job.
 	 * 
 	 * @param stdin
 	 *            the stdin file
 	 */
 	public void setStdin(final String stdin) {
 		final String oldValue = this.stdin;
 		this.stdin = stdin;
 		pcs.firePropertyChange("stdin", oldValue, this.stdout);
 	}
 
 	/**
 	 * Sets the name of the stdout file for this job.
 	 * 
 	 * @param stdout
 	 *            the stdout file.
 	 */
 	public void setStdout(final String stdout) {
 		final String oldValue = this.stdout;
 		this.stdout = stdout;
 		pcs.firePropertyChange("stdout", oldValue, this.stdout);
 	}
 
 	/**
 	 * Sets a fixed submission location for this job.
 	 * 
 	 * This is optional, if not set, Grisu auto-calculates the
 	 * submissionlocation using the VO that is used to submit it along with the
 	 * application name and version. If no application version is set Grisu
 	 * tries to figure it out parsing the executable from the commandline.
 	 * 
 	 * @param submissionLocation
 	 *            the submissionlocation in the format queue:host[#factorytype]
 	 */
 	public void setSubmissionLocation(final String submissionLocation) {
 		final String oldValue = this.submissionLocation;
 		this.submissionLocation = submissionLocation;
 		pcs.firePropertyChange("submissionLocation", oldValue,
 				this.submissionLocation);
 	}
 
 	/**
 	 * Convenience method to create a unique jobname by appending a timestamp to
 	 * the jobname.
 	 * 
 	 * @param jobname
 	 *            the base-jobname
 	 */
 	@Transient
 	public void setTimestampJobname(final String jobname) {
 		setJobname(JobnameHelpers.calculateTimestampedJobname(jobname));
 	}
 
 	/**
 	 * Convenience method to create a unique jobname by appending a timestamp
 	 * with configurable format to the jobname.
 	 * 
 	 * @param jobname
 	 *            the base-jobname
 	 * @param format
 	 *            the format of the timestamp
 	 */
 	@Transient
 	public void setTimestampJobname(final String jobname,
 			SimpleDateFormat format) {
 
 		setJobname(JobnameHelpers.calculateTimestampedJobname(jobname, format));
 
 	}
 
 	/**
 	 * Convenience method to create a unique jobname by appending a uuid to the
 	 * jobname.
 	 * 
 	 * @param jobname
 	 *            the base jobname
 	 */
 	@Transient
 	public void setUUIDJobname(final String jobname) {
 
 		if (StringUtils.isBlank(jobname)) {
 			setJobname(jobname);
 		} else {
 			setJobname(jobname + "_" + UUID.randomUUID().toString());
 		}
 	}
 
 	public void setVirtualMemory(final Long memory) {
 		final long oldValue = this.virtual_memory_in_bytes;
 		this.virtual_memory_in_bytes = memory;
 		pcs.firePropertyChange("virtualMemory", oldValue,
 				this.virtual_memory_in_bytes);
 	}
 
 	/**
 	 * Sets the walltime for the job (in seconds).
 	 * 
 	 * @param walltimeInSeconds
 	 *            the walltime in seconds
 	 * @deprecated use {@link #setWalltimeInSeconds(Integer)} instead
 	 */
 	@Deprecated
 	@Transient
 	public void setWalltime(final Integer walltimeInSeconds) {
 		setWalltimeInSeconds(walltimeInSeconds);
 	}
 
 	/**
 	 * Set walltime as a short string.
 	 * 
 	 * Examples: 2d10h30m, 20d, 10h
 	 * 
 	 * @param walltime
 	 *            the walltime string
 	 * @throws Exception
 	 *             if the string can't be parsed
 	 */
 	@Transient
	public void setWalltime(final String walltime)  {
		int wt;
		try {
			wt = WalltimeUtils.fromShortStringToSeconds(walltime);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
 		setWalltimeInSeconds(wt);
 	}
 
 	/**
 	 * Sets the walltime for the job (in seconds).
 	 * 
 	 * @param walltime
 	 *            the walltime in seconds
 	 */
 	public void setWalltimeInSeconds(final Integer walltime) {
 		final int oldValue = this.walltime_in_seconds;
 		this.walltime_in_seconds = walltime;
 		pcs.firePropertyChange("walltime", oldValue, this.walltime_in_seconds);
 	}
 
 	@Override
 	public String toString() {
 		return getJobname();
 	}
 }
