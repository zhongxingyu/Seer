 package grisu.backend.model.job.gt5;
 
 import grisu.backend.info.InformationManagerManager;
 import grisu.backend.model.ProxyCredential;
 import grisu.backend.model.job.Job;
 import grisu.backend.model.job.JobSubmitter;
 import grisu.backend.model.job.ServerJobSubmissionException;
 import grisu.control.JobConstants;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.utils.JsdlHelpers;
 import grisu.settings.ServerPropertiesManager;
 import grisu.utils.DebugUtils;
 import grith.jgrith.CredentialHelpers;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.globus.gram.internal.GRAMConstants;
 import org.globus.rsl.NameOpValue;
 import org.globus.rsl.RslNode;
 import org.ietf.jgss.GSSCredential;
 import org.w3c.dom.Document;
 
 
 public class GT5Submitter extends JobSubmitter {
 
 	static final Logger myLogger = Logger.getLogger(GT5Submitter.class
 			.getName());
 
 	private static String[] getModulesFromMDS(
 			final InformationManager infoManager, final Document jsdl) {
 		String[] modules_string = JsdlHelpers.getModules(jsdl);
 		if (modules_string != null) {
 			return modules_string;
 		}
 		// mds based
 		final String application = JsdlHelpers.getApplicationName(jsdl);
 		final String version = JsdlHelpers.getApplicationVersion(jsdl);
 		final String[] subLocs = JsdlHelpers.getCandidateHosts(jsdl);
 		if ((subLocs != null) && (subLocs.length > 0)) {
 			final String subLoc = subLocs[0];
 			if (Constants.GENERIC_APPLICATION_NAME.equals(application)) {
 				return null;
 			} else if (StringUtils.isNotBlank(application)
 					&& StringUtils.isNotBlank(version)
 					&& StringUtils.isNotBlank(subLoc)) {
 				// if we know application, version and submissionLocation
 				final Map<String, String> appDetails = infoManager
 				.getApplicationDetails(application, version, subLoc);
 
 				try {
 					modules_string = appDetails.get(Constants.MDS_MODULES_KEY)
 					.split(",");
 
 					if ((modules_string == null) || "".equals(modules_string)) {
 						return null;
 					}
 				} catch (final Exception e) {
 					return null;
 				}
 				return modules_string;
 			} else if ((application != null) && (version == null)
 					&& (subLoc != null)) {
 
 				final Map<String, String> appDetails = infoManager
 				.getApplicationDetails(application,
 						Constants.NO_VERSION_INDICATOR_STRING, subLoc);
 
 				try {
 					modules_string = appDetails.get(Constants.MDS_MODULES_KEY)
 					.split(",");
 
 					if ((modules_string == null) || "".equals(modules_string)) {
 						return null;
 					}
 
 				} catch (final Exception e) {
 					return null;
 				}
 
 				return modules_string;
 
 			} else {
 				throw new RuntimeException(
 				"Can't determine module because either/or application, version submissionLocation are missing.");
 			}
 		} else {
 			myLogger.info("No submission location specified. If this happens when trying to submit a job, it's probably a bug...");
 			return new String[] {};
 		}
 
 	}
 
 	private Gram5Client gram5 = null;
 
 	public static final InformationManager informationManager = InformationManagerManager
 	.getInformationManager(ServerPropertiesManager
 			.getInformationManagerConf());
 
 	private static void addNotNull(RslNode node, NameOpValue value) {
 		if (value != null) {
 			node.add(value);
 		}
 	}
 
 	public static String createJobSubmissionDescription(
 			final InformationManager infoManager, final Document jsdl, final String fqan) {
 
 		final RslNode result = new RslNode();
 		final NameOpValue executable = new NameOpValue("executable",
 				NameOpValue.EQ, JsdlHelpers.getPosixApplicationExecutable(jsdl));
 
 		final String[] argumentsVal = JsdlHelpers
 		.getPosixApplicationArguments(jsdl);
 		NameOpValue arguments = null;
 		if ((argumentsVal != null) && (argumentsVal.length > 0)) {
 			arguments = new NameOpValue("arguments", NameOpValue.EQ,
 					argumentsVal);
 		}
 
 		final NameOpValue stdout = new NameOpValue("stdout", NameOpValue.EQ,
 				JsdlHelpers.getPosixStandardOutput(jsdl));
 		final NameOpValue stderr = new NameOpValue("stderr", NameOpValue.EQ,
 				JsdlHelpers.getPosixStandardError(jsdl));
 
 		final String inputVal = JsdlHelpers.getPosixStandardInput(jsdl);
 		NameOpValue stdin = null;
 
 		if (StringUtils.isNotBlank(inputVal)) {
 			stdin = new NameOpValue("stdin", NameOpValue.EQ,
 					JsdlHelpers.getPosixStandardInput(jsdl));
 		}
 
 		final String dirValue = JsdlHelpers.getWorkingDirectory(jsdl);
 		NameOpValue directory = null;
 		if ((dirValue != null) && !"".equals(dirValue.trim())) {
 			directory = new NameOpValue("directory", NameOpValue.EQ,
 					JsdlHelpers.getWorkingDirectory(jsdl));
 		}
 
 		NameOpValue queue = null;
 		NameOpValue jobType = null;
 		NameOpValue count = null;
 		NameOpValue maxMemory = null;
 		NameOpValue maxWalltime = null;
 
 		DebugUtils.jsdlDebugOutput("Before translating into rsl: ", jsdl);
 
 		// Add "queue" node
 		// TODO change that once I know how to specify queues in jsdl
 		final String[] queues = JsdlHelpers.getCandidateHosts(jsdl);
 		if ((queues != null) && (queues.length > 0)) {
 			String queueVal = queues[0];
 			// always uses
 			// the first
 			// candidate
 			// host - not
 			// good
 			if (queueVal.indexOf(":") != -1) {
 				queueVal = queueVal.substring(0, queueVal.indexOf(":"));
 				queue = new NameOpValue("queue", NameOpValue.EQ, queueVal);
 			}
 
 		} else {
 			myLogger.info("Can't parse queues. If that happens when trying to submit a job, it's probably a bug...");
 		}
 		// Add "jobtype" if mpi
 		final int processorCount = JsdlHelpers.getProcessorCount(jsdl);
 
 		final String jobTypeString = JsdlHelpers.getArcsJobType(jsdl);
 		count = new NameOpValue("count", NameOpValue.EQ, "" + processorCount);
 
 		if (processorCount > 1) {
 
			if (!StringUtils.isNotBlank(jobTypeString)) {
 				jobType = new NameOpValue("job_type", NameOpValue.EQ, "mpi");
 			} else {
 				jobType = new NameOpValue("job_type", NameOpValue.EQ,
 						jobTypeString);
 			}
 		} else {
			if (!StringUtils.isNotBlank(jobTypeString)) {
 				jobType = new NameOpValue("job_type", NameOpValue.EQ, "single");
 			} else {
 				jobType = new NameOpValue("job_type", NameOpValue.EQ,
 						jobTypeString);
 			}
 
 		}
 
 		// total memory
 		Long memory = JsdlHelpers.getTotalMemoryRequirement(jsdl);
 
 		if ((memory != null) && (memory >= 0)) {
 			// convert from bytes to mb
 			memory = memory / (1024 * 1024);
 			maxMemory = new NameOpValue("max_memory", NameOpValue.EQ, ""
 					+ memory);
 		}
 
 		// Add "maxWallTime" node
 		final int walltime = JsdlHelpers.getWalltime(jsdl);
 		if (walltime > 0) {
 			int wt = new Integer(JsdlHelpers.getWalltime(jsdl));
 			// convert to minutes
 			wt = wt / 60;
 			maxWalltime = new NameOpValue("max_wall_time", NameOpValue.EQ, ""
 					+ wt);
 		}
 
 		// send email on start/stop
 		final String emailAddressVal = JsdlHelpers.getEmail(jsdl);
 		final boolean onFinish = JsdlHelpers.getSendEmailOnJobFinish(jsdl);
 		final boolean onStart = JsdlHelpers.getSendEmailOnJobStart(jsdl);
 
 		NameOpValue emailAddress = null;
 		NameOpValue emailStart = null;
 		NameOpValue emailAbort = null;
 		NameOpValue emailTermination = null;
 
 		if (emailAddressVal != null) {
 			emailAddress = new NameOpValue("email_address", NameOpValue.EQ,
 					emailAddressVal);
 			if (onStart) {
 				emailStart = new NameOpValue("email_on_execution",
 						NameOpValue.EQ, "yes");
 			}
 			if (onFinish) {
 				emailAbort = new NameOpValue("email_on_abort", NameOpValue.EQ,
 				"yes");
 				emailTermination = new NameOpValue("email_on_termination",
 						NameOpValue.EQ, "yes");
 			}
 		}
 
 		// job name
 		final String jobnameVal = JsdlHelpers.getJobname(jsdl);
 		final NameOpValue jobname = new NameOpValue("jobname", NameOpValue.EQ,
 				jobnameVal.substring(Math.max(0, jobnameVal.length() - 6)));
 
 		// module setup
 		final String[] modulesVal = getModulesFromMDS(infoManager, jsdl);
 		if (modulesVal != null) {
 			for (final String moduleStr : modulesVal) {
 				final NameOpValue module = new NameOpValue("module",
 						NameOpValue.EQ, moduleStr);
 				addNotNull(result, module);
 			}
 		}
 
 		result.add(new NameOpValue("save_state", NameOpValue.EQ, "yes"));
 		result.add(new NameOpValue("vo",NameOpValue.EQ,fqan));
 		addNotNull(result, executable);
 		addNotNull(result, jobname);
 		addNotNull(result, arguments);
 		addNotNull(result, stdout);
 		addNotNull(result, stdin);
 		addNotNull(result, stderr);
 		addNotNull(result, directory);
 		addNotNull(result, queue);
 		addNotNull(result, jobType);
 		addNotNull(result, count);
 		addNotNull(result, maxMemory);
 		addNotNull(result, maxWalltime);
 		addNotNull(result, emailAddress);
 		addNotNull(result, emailStart);
 		addNotNull(result, emailTermination);
 		addNotNull(result, emailAbort);
 
 		final String resultString = result.toRSL(true);
 		myLogger.debug("Translated jsdl into gt5 rsl: " + resultString);
 		return resultString;
 	}
 
 	public static void main(String[] args) {
 		final Gram5Client gram5 = new Gram5Client();
 	}
 
 	public GT5Submitter() {
 		gram5 = new Gram5Client();
 	}
 
 	@Override
 	public int getJobStatus(String endPointReference, ProxyCredential cred) {
 		return translateToGrisuStatus(gram5.getJobStatus(endPointReference,
 				cred.getGssCredential()));
 	}
 
 	@Override
 	public String getServerEndpoint(String server) {
 		return server;
 	}
 
 	@Override
 	public int killJob(String handle, ProxyCredential cred) {
 		return gram5.kill(handle, cred.getGssCredential());
 	}
 
 	@Override
 	protected String submit(InformationManager infoManager, String host,
 			String factoryType, Job job) throws ServerJobSubmissionException {
 		final String rsl = createJobSubmissionDescription(infoManager,
 				job.getJobDescription(),job.getFqan());		
 		myLogger.debug("RSL is ... " + rsl);
 
 		try {
 			final GSSCredential credential = CredentialHelpers
 			.convertByteArrayToGSSCredential(job.getCredential()
 					.getCredentialData());
 			final String handle = gram5.submit(rsl, host, credential);
 			return handle;
 		} catch (final Exception ex) {
 			myLogger.error(ex);
 			throw new ServerJobSubmissionException(ex.getLocalizedMessage(), ex);
 		}
 	}
 
 	private int translateToGrisuStatus(final int[] statusAndError) {
 
 		final int status = statusAndError[0];
 		final int error = statusAndError[1];
 
 		int grisu_status = Integer.MIN_VALUE;
 		if (status == GRAMConstants.STATUS_DONE) {
 			grisu_status = JobConstants.DONE + error;
 		} else if (status == GRAMConstants.STATUS_STAGE_IN) {
 			grisu_status = JobConstants.STAGE_IN;
 		} else if (status == GRAMConstants.STATUS_STAGE_OUT) {
 			grisu_status = JobConstants.STAGE_IN;
 		} else if (status == GRAMConstants.STATUS_PENDING) {
 			grisu_status = JobConstants.PENDING;
 		} else if (status == GRAMConstants.STATUS_UNSUBMITTED) {
 			grisu_status = JobConstants.UNSUBMITTED;
 		} else if (status == GRAMConstants.STATUS_ACTIVE) {
 			grisu_status = JobConstants.ACTIVE;
 		} else if (status == GRAMConstants.STATUS_FAILED) {
 			grisu_status = JobConstants.FAILED;
 		} else if (status == GRAMConstants.STATUS_SUSPENDED) {
 			grisu_status = JobConstants.ACTIVE;
 		} else {
 			grisu_status = status;
 		}
 		return grisu_status;
 
 	}
 }
