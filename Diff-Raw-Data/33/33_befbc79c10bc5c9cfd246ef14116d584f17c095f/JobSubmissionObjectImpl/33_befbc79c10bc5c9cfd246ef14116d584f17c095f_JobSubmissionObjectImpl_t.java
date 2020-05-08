 package org.vpac.grisu.model.job;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.JobnameHelpers;
 import org.vpac.grisu.control.exceptions.JobPropertiesException;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.utils.SeveralXMLHelpers;
 import org.vpac.grisu.utils.SimpleJsdlBuilder;
 import org.w3c.dom.Document;
 
 import au.org.arcs.jcommons.constants.Constants;
 import au.org.arcs.jcommons.constants.JobSubmissionProperty;
 import au.org.arcs.jcommons.utils.JsdlHelpers;
 
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
 public class JobSubmissionObjectImpl {
 
 	static final Logger myLogger = Logger
 			.getLogger(JobSubmissionObjectImpl.class.getName());
 
 	public static String extractExecutable(String commandline) {
 
 		if ((commandline == null) || (commandline.length() == 0)) {
 			return null;
 		}
 
 		final int i = commandline.indexOf(" ");
 		if (i <= 0) {
 			return commandline;
 		} else {
 			String result = commandline.substring(0, i);
 			return result;
 		}
 	}
 
 	public static void main(final String[] args) throws JobPropertiesException {
 
 		final JobSubmissionObjectImpl jso = new JobSubmissionObjectImpl();
 
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
 		jso.setInputFileUrls(new String[] { "file:///temp/test",
 				"gsiftp://ng2.vpac.org/tmp/test" });
 		jso.setMemory(0L);
 
 		jso.getJobDescriptionDocument();
 
 	}
 
 	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	private Long id;
 
 	protected String jobname;
 
 	private String application;
 
 	private String applicationVersion;
 
 	private String email_address;
 
 	private boolean email_on_job_start = false;
 
 	private boolean email_on_job_finish = false;
 
 	private int cpus = 1;
 
 	private boolean force_single = false;
 
 	private boolean force_mpi = false;
 
 	private int hostcount = 0;
 
 	private long memory_in_bytes = 0;
 
 	private int walltime_in_seconds = 0;
 
 	private Set<String> inputFileUrls = new HashSet<String>();
 
 	private Set<String> modules = new HashSet<String>();
 
 	private String submissionLocation;
 
 	private String commandline;
 
 	private String stderr;
 
 	private String stdout;
 
 	private String stdin;
 
 	private String pbsDebug;
 
 	public JobSubmissionObjectImpl() {
 	}
 
 	public JobSubmissionObjectImpl(final Document jsdl) {
 
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
 					JobSubmissionProperty.FORCE_SINGLE.defaultValue())) {
 				force_single = true;
 				force_mpi = false;
 			} else if (jobTypeString.toLowerCase().equals(
 					JobSubmissionProperty.FORCE_SINGLE.defaultValue())) {
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
 		setInputFileUrls(JsdlHelpers.getInputFileUrls(jsdl));
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
 
 	public JobSubmissionObjectImpl(final Map<String, String> jobProperties) {
 
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
 			this.hostcount = 1;
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
 		try {
 			this.walltime_in_seconds = Integer.parseInt(jobProperties
 					.get(JobSubmissionProperty.WALLTIME_IN_MINUTES.toString())) * 60;
 		} catch (final NumberFormatException e) {
 			this.walltime_in_seconds = 0;
 		}
 
 		String temp = jobProperties.get(JobSubmissionProperty.INPUT_FILE_URLS
 				.toString());
 		if ((temp != null) && (temp.length() > 0)) {
 			setInputFileUrls(temp.split(","));
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
 
 	public void addInputFileUrl(String url) {
 
 		if (StringUtils.isBlank(url)) {
 			return;
 		}
 
 		url = FileManager.ensureUriFormat(url);
 		final String[] oldValue = getInputFileUrls();
 		this.inputFileUrls.add(url);
 		pcs.firePropertyChange("inputFileUrls", oldValue, this.inputFileUrls);
 
 	}
 
 	public void addModule(final String module) {
 		this.modules.add(module);
 	}
 
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
 
 		if (other instanceof JobSubmissionObjectImpl) {
 			final JobSubmissionObjectImpl otherJob = (JobSubmissionObjectImpl) other;
 			return getJobname().equals(otherJob.getJobname());
 		} else {
 			return false;
 		}
 
 	}
 
 	public String extractExecutable() {
 		return extractExecutable(commandline);
 	}
 
 	public String getApplication() {
 		return application;
 	}
 
 	public String getApplicationVersion() {
 		if (StringUtils.isBlank(applicationVersion)) {
 			return Constants.NO_VERSION_INDICATOR_STRING;
 		}
 		return applicationVersion;
 	}
 
 	@Column(nullable = false)
 	public String getCommandline() {
 		return commandline;
 	}
 
 	public Integer getCpus() {
 		return cpus;
 	}
 
 	public String getEmail_address() {
 		return email_address;
 	}
 
 	@Transient
 	public String getExecutable() {
 		return extractExecutable();
 	}
 
 	public Integer getHostCount() {
 		return hostcount;
 	}
 
 	@Id
 	@GeneratedValue
 	private Long getId() {
 		return this.id;
 	}
 
 	public String[] getInputFileUrls() {
 		return inputFileUrls.toArray(new String[] {});
 	}
 
 	@Transient
 	public String getInputFileUrlsAsString() {
 		if ((inputFileUrls != null) && (inputFileUrls.size() != 0)) {
 			return StringUtils.join(inputFileUrls, ",");
 		} else {
 			return new String();
 		}
 	}
 
 	@Transient
 	public final Document getJobDescriptionDocument()
 			throws JobPropertiesException {
 
 		checkValidity();
 
 		final Map<JobSubmissionProperty, String> jobProperties = getJobSubmissionPropertyMap();
 
 		final Document jsdl = SimpleJsdlBuilder.buildJsdl(jobProperties);
 
 		return jsdl;
 
 	}
 
 	@Transient
 	public final String getJobDescriptionDocumentAsString()
 			throws JobPropertiesException {
 
 		String jsdlString = null;
 		jsdlString = SeveralXMLHelpers.toString(getJobDescriptionDocument());
 
 		return jsdlString;
 	}
 
 	public String getJobname() {
 		return jobname;
 	}
 
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
 				getInputFileUrlsAsString());
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
 
 	public Long getMemory() {
 		return memory_in_bytes;
 	}
 
 	public String[] getModules() {
 		return modules.toArray(new String[] {});
 	}
 
 	@Transient
 	public String getModulesAsString() {
 		if ((modules != null) && (modules.size() != 0)) {
 			return StringUtils.join(modules, ",");
 		} else {
 			return new String();
 		}
 	}
 
 	public String getPbsDebug() {
 		return pbsDebug;
 	}
 
 	public String getStderr() {
 		return stderr;
 	}
 
 	public String getStdin() {
 		return this.stdin;
 	}
 
 	public String getStdout() {
 		return stdout;
 	}
 
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
 
 	public String getSubmissionLocation() {
 		return submissionLocation;
 	}
 
 	public int getWalltime() {
 		return getWalltimeInSeconds();
 	}
 
 	public int getWalltimeInSeconds() {
 		return walltime_in_seconds;
 	}
 
 	@Override
 	public int hashCode() {
 		return 73 * getJobname().hashCode();
 	}
 
 	public Boolean isEmail_on_job_finish() {
 		return email_on_job_finish;
 	}
 
 	public Boolean isEmail_on_job_start() {
 		return email_on_job_start;
 	}
 
 	public Boolean isForce_mpi() {
 		return force_mpi;
 	}
 
 	public Boolean isForce_single() {
 		return force_single;
 	}
 
 	public void removeInputFileUrl(String selectedFile) {
 
 		if (StringUtils.isBlank(selectedFile)) {
 			return;
 		}
 		final String[] oldValue = getInputFileUrls();
 		this.inputFileUrls.remove(selectedFile);
 		pcs.firePropertyChange("inputFileUrls", oldValue, this.inputFileUrls);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	public void setApplication(final String app) {
 		final String oldValue = this.application;
 		this.application = app;
		pcs.firePropertyChange("application", oldValue,
				JobSubmissionObjectImpl.this.application);
 	}
 
 	public void setApplicationVersion(final String appVersion) {
 		final String oldValue = this.applicationVersion;
 		this.applicationVersion = appVersion;
 		pcs.firePropertyChange("applicationVersion", oldValue,
 				this.applicationVersion);
 	}
 
 	public synchronized void setCommandline(final String commandline) {
 		final String oldValue = this.commandline;
 		final String oldExe = extractExecutable(this.commandline);
 		this.commandline = commandline;
 		myLogger.debug("Commandline for job: " + getJobname() + "changed: "
 				+ commandline);
 		pcs.firePropertyChange("commandline", oldValue, this.commandline);
 		final String newExe = extractExecutable();
 		pcs.firePropertyChange("executable", oldExe, newExe);
 	}
 
 	public void setCpus(final Integer cpus) {
 		final int oldValue = this.cpus;
 		this.cpus = cpus;
 		pcs.firePropertyChange("cpus", oldValue, this.cpus);
 	}
 
 	public void setEmail_address(final String email_address) {
 		final String oldValue = this.email_address;
 		this.email_address = email_address;
 		pcs.firePropertyChange("email_address", oldValue, this.email_address);
 	}
 
 	public void setEmail_on_job_finish(final Boolean email_on_job_finish) {
 		final boolean oldValue = this.email_on_job_finish;
 		this.email_on_job_finish = email_on_job_finish;
 		pcs.firePropertyChange("email_on_job_finish", oldValue,
 				this.email_on_job_finish);
 	}
 
 	public void setEmail_on_job_start(final Boolean email_on_job_start) {
 		final boolean oldValue = this.email_on_job_start;
 		this.email_on_job_start = email_on_job_start;
 		pcs.firePropertyChange("email_on_job_start", oldValue,
 				this.email_on_job_start);
 	}
 
 	public void setForce_mpi(final Boolean force_mpi) {
 		final boolean oldValue = this.force_mpi;
 		final boolean oldValue1 = this.force_single;
 		final int oldHostCount = this.hostcount;
 		this.force_mpi = force_mpi;
 		this.force_single = !force_mpi;
 		this.hostcount = -1;
 		pcs.firePropertyChange("force_mpi", oldValue, this.force_mpi);
 		pcs.firePropertyChange("force_single", oldValue1, this.force_single);
 		pcs.firePropertyChange("hostCount", oldHostCount, this.hostcount);
 	}
 
 	public void setForce_single(final Boolean force_single) {
 		final boolean oldValue = this.force_mpi;
 		final boolean oldValue1 = this.force_single;
 		this.force_single = force_single;
 		this.force_mpi = !force_single;
 		pcs.firePropertyChange("force_mpi", oldValue, this.force_mpi);
 		pcs.firePropertyChange("force_single", oldValue1, this.force_single);
 	}
 
 	public void setHostCount(Integer hc) {
 		final int oldHostCount = this.hostcount;
 		this.hostcount = hc;
 		pcs.firePropertyChange("hostCount", oldHostCount, this.hostcount);
 	}
 
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	public void setInputFileUrls(final String[] inputFileUrls) {
 		final Set<String> oldValue = this.inputFileUrls;
 		if (inputFileUrls != null) {
 			this.inputFileUrls = new HashSet<String>(
 					Arrays.asList(inputFileUrls));
 		} else {
 			this.inputFileUrls = new HashSet<String>();
 		}
 		pcs.firePropertyChange("inputFileUrls", oldValue, this.inputFileUrls);
 	}
 
 	public void setJobname(final String jobname) {
 		final String oldValue = this.jobname;
 		this.jobname = jobname;
 		pcs.firePropertyChange("jobname", oldValue, this.jobname);
 	}
 
 	public void setMemory(final Long memory) {
 		final long oldValue = this.memory_in_bytes;
 		this.memory_in_bytes = memory;
 		pcs.firePropertyChange("memory", oldValue, this.memory_in_bytes);
 	}
 
 	public void setModules(final String[] modules) {
 		final Set<String> oldValue = this.modules;
 		if (modules != null) {
 			this.modules = new HashSet<String>(Arrays.asList(modules));
 		} else {
 			this.modules = new HashSet<String>();
 		}
 		pcs.firePropertyChange("modules", oldValue, this.modules);
 	}
 
 	public void setPbsDebug(String pbsDebug) {
 		final String oldValue = this.pbsDebug;
 		this.pbsDebug = pbsDebug;
 		pcs.firePropertyChange("pbsDebug", oldValue, this.pbsDebug);
 	}
 
 	public void setStderr(final String stderr) {
 		final String oldValue = this.stderr;
 		this.stderr = stderr;
 		pcs.firePropertyChange("stderr", oldValue, this.stderr);
 	}
 
 	public void setStdin(final String stdin) {
 		final String oldValue = this.stdin;
 		this.stdin = stdin;
 		pcs.firePropertyChange("stdin", oldValue, this.stdout);
 	}
 
 	public void setStdout(final String stdout) {
 		final String oldValue = this.stdout;
 		this.stdout = stdout;
 		pcs.firePropertyChange("stdout", oldValue, this.stdout);
 	}
 
 	public void setSubmissionLocation(final String submissionLocation) {
 		final String oldValue = this.submissionLocation;
 		this.submissionLocation = submissionLocation;
 		pcs.firePropertyChange("submissionLocation", oldValue,
 				this.submissionLocation);
 	}
 
 	@Transient
 	public void setTimestampJobname(final String jobname) {
 		setJobname(JobnameHelpers.calculateTimestampedJobname(jobname));
 	}
 
 	@Transient
 	public void setTimestampJobname(final String jobname,
 			SimpleDateFormat format) {
 
 		setJobname(JobnameHelpers.calculateTimestampedJobname(jobname, format));
 
 	}
 
 	@Transient
 	public void setUniqueJobname(final String jobname) {
 		if (StringUtils.isBlank(jobname)) {
 			setJobname(jobname);
 		} else {
 			setJobname(jobname + "_" + UUID.randomUUID().toString());
 		}
 	}
 
 	@Transient
 	public void setWalltime(final Integer walltimeInSeconds) {
 		setWalltimeInSeconds(walltimeInSeconds);
 	}
 
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
