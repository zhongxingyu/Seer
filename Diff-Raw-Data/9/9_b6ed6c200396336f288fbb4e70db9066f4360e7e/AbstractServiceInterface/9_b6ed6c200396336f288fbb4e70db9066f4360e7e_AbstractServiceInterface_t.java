 package grisu.control.serviceInterfaces;
 
 import grisu.GrisuVersion;
 import grisu.backend.model.RemoteFileTransferObject;
 import grisu.backend.model.User;
 import grisu.backend.model.fs.UserFileManager;
 import grisu.backend.model.job.BatchJob;
 import grisu.backend.model.job.Job;
 import grisu.backend.model.job.Jobhelper;
 import grisu.backend.model.job.UserBatchJobManager;
 import grisu.backend.model.job.UserJobManager;
 import grisu.backend.utils.LocalTemplatesHelper;
 import grisu.control.JobConstants;
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.BatchJobException;
 import grisu.control.exceptions.JobPropertiesException;
 import grisu.control.exceptions.JobSubmissionException;
 import grisu.control.exceptions.NoSuchJobException;
 import grisu.control.exceptions.NoValidCredentialException;
 import grisu.control.exceptions.RemoteFileSystemException;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.JobSubmissionProperty;
 import grisu.jcommons.interfaces.GrinformationManager;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.model.info.Directory;
 import grisu.jcommons.model.info.Queue;
 import grisu.jcommons.model.info.Site;
 import grisu.jcommons.model.info.VO;
 import grisu.jcommons.model.info.Version;
 import grisu.model.MountPoint;
 import grisu.model.dto.DtoActionStatus;
 import grisu.model.dto.DtoApplicationDetails;
 import grisu.model.dto.DtoApplicationInfo;
 import grisu.model.dto.DtoBatchJob;
 import grisu.model.dto.DtoJob;
 import grisu.model.dto.DtoJobs;
 import grisu.model.dto.DtoMountPoints;
 import grisu.model.dto.DtoProperties;
 import grisu.model.dto.DtoProperty;
 import grisu.model.dto.DtoStringList;
 import grisu.model.dto.DtoSubmissionLocations;
 import grisu.model.dto.GridFile;
 import grisu.settings.ServerPropertiesManager;
 import grisu.utils.FileHelpers;
 import grisu.utils.SeveralXMLHelpers;
 import grith.jgrith.utils.CertificateFiles;
 import grith.jgrith.voms.VOManagement.VOManagement;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.activation.DataHandler;
 import javax.annotation.security.RolesAllowed;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.globus.common.CoGProperties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.util.StatusPrinter;
 
 import com.google.common.base.Functions;
 import com.google.common.collect.Collections2;
 
 /**
  * This abstract class implements most of the methods of the
  * {@link ServiceInterface} interface. This way developers don't have to waste
  * time to implement the whole interface just for some things that are site/grid
  * specific. Currently there are two classes that extend this abstract class:
  * {@link LocalServiceInterface} and WsServiceInterface (which can be found in
  * the grisu-ws module).
  * 
  * The {@link LocalServiceInterface} is used to work with a small local database
  * like hsqldb so a user has got the whole grisu framework on his desktop. Of
  * course, all required ports have to be open from the desktop to the grid. On
  * the other hand no web service server is required.
  * 
  * The WsServiceInterface is the main one and it is used to set up a web service
  * somewhere. So light-weight clients can talk to it.
  * 
  * @author Markus Binsteiner
  */
 public abstract class AbstractServiceInterface implements ServiceInterface {
 
 	static Logger myLogger = null;
 	public static CacheManager cache;
 
 	public static final InformationManager informationManager = new GrinformationManager(
 			ServerPropertiesManager.getInformationManagerConf());
 
 	private final static AdminInterface admin = new AdminInterface(null,
 			informationManager,
 			User.userdao);
 
 
 	static {
 
 		String logbackPath = "/etc/grisu/logback.xml";
 		if (new File(logbackPath).exists()
 				&& (new File(logbackPath).length() > 0)) {
 			// configure loback from external logback.xml config file
 			// assume SLF4J is bound to logback in the current environment
 			LoggerContext context = (LoggerContext) LoggerFactory
 					.getILoggerFactory();
 
 			try {
 				JoranConfigurator configurator = new JoranConfigurator();
 				configurator.setContext(context);
 				// Call context.reset() to clear any previous configuration, e.g.
 				// default
 				// configuration. For multi-step configuration, omit calling
 				// context.reset().
 				context.reset();
 				configurator.doConfigure(logbackPath);
 			} catch (JoranException je) {
 				// StatusPrinter will handle this
 			}
 			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
 		}
 
 
 		// TODO change to logback
 		// String log4jPath = "/etc/grisu/grisu-log4j.xml";
 		// if (new File(log4jPath).exists() && (new File(log4jPath).length() >
 		// 0)) {
 		// try {
 		// DOMConfigurator.configure(log4jPath);
 		// } catch (Exception e) {
 		// myLogger.error(e.getLocalizedMessage(), e);
 		// }
 		// }
 
 		myLogger = LoggerFactory.getLogger(AbstractServiceInterface.class
 				.getName());
 
 		myLogger.debug("Logging initiated...");
 
 		myLogger.info("============================================");
 		myLogger.info("Starting up backend...");
 		myLogger.info("============================================");
 
 		myLogger.info("Setting networkaddress.cache.ttl java security property to -1...");
 		java.security.Security.setProperty("networkaddress.cache.ttl", "" + -1);
 
 		CoGProperties.getDefault().setProperty(
 				CoGProperties.ENFORCE_SIGNING_POLICY, "false");
 
 		try {
 			LocalTemplatesHelper.copyTemplatesAndMaybeGlobusFolder();
 
 			//String[] vos = ServerPropertiesManager.getVOsToUse();
 
 			Set<VO> vos = informationManager.getAllVOs();
 
 			VOManagement.setVOsToUse(vos);
 
 			//			VomsesFiles.copyVomses(Arrays.asList(ServerPropertiesManager
 			//					.getVOsToUse()));
 			CertificateFiles.copyCACerts(false);
 		} catch (final Exception e) {
 			// TODO Auto-generated catch block
 			myLogger.error(e.getLocalizedMessage());
 			// throw new
 			// RuntimeException("Could not initiate local backend: "+e.getLocalizedMessage());
 		}
 
 		// create ehcache manager singleton
 		try {
 			URL url = ClassLoader.getSystemResource("/grisu-ehcache.xml");
 			if (url == null) {
 				url = myLogger.getClass().getResource("/grisu-ehcache.xml");
 			}
 
 			CacheManager.create(url);
 			cache = CacheManager.getInstance();
 
 			final Cache session = cache.getCache("session");
 			if (session == null) {
 				myLogger.debug("Session cache is null");
 			}
 		} catch (final Exception e) {
 			myLogger.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	public static final String BACKEND_VERSION = GrisuVersion.get("grisu-core");
 
 	public static final String REFRESH_STATUS_PREFIX = "REFRESH_";
 
 	// public static final InformationManager informationManager =
 	// createInformationManager();
 
 
 	// public static final MatchMaker matchmaker = createMatchMaker();
 
 	// private final Map<String, List<Job>> archivedJobs = new HashMap<String,
 	// List<Job>>();
 	private static String backendInfo = null;
 
 	private static String hostname = null;
 
 
 	// public static InformationManager createInformationManager() {
 	// return InformationManagerManager
 	// .getInformationManager(ServerPropertiesManager
 	// .getInformationManagerConf());
 	// }
 
 	public static Cache eternalCache() {
 		return cache.getCache("eternal");
 	}
 
 	public static String getBackendInfo() {
 
 		if (StringUtils.isBlank(backendInfo)) {
 			//			String host = getInterfaceInfo("HOSTNAME");
 			//			if (StringUtils.isBlank(host)) {
 			//				host = "Host unknown";
 			//			}
 			//			String version = getInterfaceInfo("VERSION");
 			//			if (StringUtils.isBlank(version)) {
 			//				version = "Version unknown";
 			//			}
 			//			String name = getInterfaceInfo("NAME");
 			//			if (StringUtils.isBlank(name)) {
 			//				name = "Backend name unknown";
 			//			}
 			//
 			//			backendInfo = name + " / " + host + " / version:" + version;
 			backendInfo = "Not implemented yet.";
 
 		}
 		return backendInfo;
 	}
 
 	public static Object getFromEternalCache(Object key) {
 		if ((key != null) && (eternalCache().get(key) != null)) {
 			return eternalCache().get(key).getObjectValue();
 		} else {
 			return null;
 		}
 	}
 
 	public static Object getFromSessionCache(Object key) {
 		if ((key != null) && (sessionCache().get(key) != null)) {
 			return sessionCache().get(key).getObjectValue();
 		} else {
 			return null;
 		}
 	}
 
 	public static Object getFromShortCache(Object key) {
 		if ((key != null) && (shortCache().get(key) != null)) {
 			return shortCache().get(key).getObjectValue();
 		} else {
 			return null;
 		}
 	}
 
 	public static void putIntoEternalCache(Object key, Object value) {
 		final net.sf.ehcache.Element e = new net.sf.ehcache.Element(key, value);
 		eternalCache().put(e);
 	}
 
 	public static void putIntoSessionCache(Object key, Object value) {
 		final net.sf.ehcache.Element e = new net.sf.ehcache.Element(key, value);
 		sessionCache().put(e);
 	}
 	public static void putIntoShortCache(Object key, Object value) {
 		final net.sf.ehcache.Element e = new net.sf.ehcache.Element(key, value);
 		shortCache().put(e);
 	}
 
 	public static Cache sessionCache() {
 
 		return cache.getCache("session");
 	}
 
 	public static Cache shortCache() {
 		return cache.getCache("short");
 	}
 
 
 	// protected final UserDAO userdao = new UserDAO();
 
 	// private Map<String, RemoteFileTransferObject> fileTransfers = new
 	// HashMap<String, RemoteFileTransferObject>();
 
 	private int SUBMIT_PROXY_LIFETIME = -1;
 
 	public void addArchiveLocation(String alias, String value) {
 
 		if (StringUtils.isBlank(value)) {
 			getUser().removeArchiveLocation(alias);
 		} else {
 			getUser().addArchiveLocation(alias, value);
 		}
 
 	}
 
 	public void addBookmark(String alias, String value) {
 
 		if (StringUtils.isBlank(value)) {
 			getUser().removeBookmark(alias);
 		} else {
 			getUser().addBookmark(alias, value);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#addJobProperties(java.lang.String ,
 	 * java.util.Map)
 	 */
 	public void addJobProperties(final String jobname, final DtoJob properties)
 			throws NoSuchJobException {
 
 		getJobManager().addJobProperties(jobname, properties);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#addJobProperty(java.lang.String,
 	 * java.lang.String, java.lang.String)
 	 */
 	public void addJobProperty(final String jobname, final String key,
 			final String value) throws NoSuchJobException {
 
 		getJobManager().addJobProperty(jobname, key, value);
 
 	}
 
 	public String addJobToBatchJob(String batchjobname, String jobdescription)
 			throws NoSuchJobException, JobPropertiesException {
 		return getBatchJobManager().addJobToBatchJob(batchjobname,
 				jobdescription);
 	}
 
 	public DtoStringList admin(String command, DtoProperties config) {
 
 		String dn = getDN();
 
 		// yes yes yes, this is not a proper way to authenticate admin commands
 		// for now, until admin commands are only refreshing the config files,
 		// I'll not worry about it...
 		if (!admin.isAdmin(dn)) {
			return DtoStringList.fromSingleString("No admin: " + dn);
 		}
 
 		if (StringUtils.isBlank(command)) {
 			return DtoStringList.fromSingleString("No command specified.");
 		}
 
 		Map<String, String> configMap = new HashMap<String, String>();
 		if (config != null) {
 			configMap = config.propertiesAsMap();
 		}
 
 		return admin.execute(command, configMap);
 	}
 
 	// private boolean checkWhetherGridResourceIsActuallyAvailable(
 	// GridResource resource) {
 	//
 	// final String[] filesystems = informationManager
 	// .getStagingFileSystemForSubmissionLocation(SubmissionLocationHelpers
 	// .createSubmissionLocationString(resource));
 	//
 	// for (final MountPoint mp : df().getMountpoints()) {
 	//
 	// for (final String fs : filesystems) {
 	// if (mp.getRootUrl().startsWith(fs.replace(":2811", ""))) {
 	// return true;
 	// }
 	// }
 	//
 	// }
 	//
 	// return false;
 	//
 	// }
 
 	public String archiveJob(String jobname, String target)
 			throws JobPropertiesException, NoSuchJobException,
 			RemoteFileSystemException {
 
 		return getJobManager().archiveJob(jobname, target);
 	}
 
 	public void copyBatchJobInputFile(String batchJobname, String inputFile,
 			String filename) throws RemoteFileSystemException,
 			NoSuchJobException {
 
 		final BatchJob multiJob = getBatchJobManager().getBatchJobFromDatabase(
 				batchJobname);
 
 		final String relpathFromMountPointRoot = multiJob
 				.getJobProperty(Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY);
 
 		for (final String mountPointRoot : multiJob.getAllUsedMountPoints()) {
 
 			final String targetUrl = mountPointRoot + "/"
 					+ relpathFromMountPointRoot + "/" + filename;
 			myLogger.debug("Coping multipartjob inputfile " + filename
 					+ " to: " + targetUrl);
 			getFileManager().cpSingleFile(inputFile, targetUrl, true, true,
 					true);
 
 		}
 
 	}
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#cp(java.lang.String,
 	 * java.lang.String, boolean, boolean)
 	 */
 	public String cp(final DtoStringList sources, final String target,
 			final boolean overwrite, final boolean waitForFileTransferToFinish)
 					throws RemoteFileSystemException {
 
 		final String handle = "cp_" + sources.asSortedSet().size()
 				+ "_files_to_" + target + "_" + new Date().getTime() + " / "
 				+ getUser().getDn();
 
 		final DtoActionStatus actionStat = new DtoActionStatus(handle,
 				sources.asArray().length * 2);
 
 		getSessionActionStatus().put(handle, actionStat);
 
 		final String handleFinal = handle;
 		final Thread cpThread = new Thread() {
 			@Override
 			public void run() {
 				try {
 					for (final String source : sources.asArray()) {
 						actionStat.addElement("Starting transfer of file: "
 								+ source);
 						final String filename = FileHelpers.getFilename(source);
 						final RemoteFileTransferObject rto = getFileManager()
 								.cpSingleFile(source, target + "/" + filename,
 										overwrite, true, true);
 
 						if (rto.isFailed()) {
 							actionStat.setFailed(true);
 							actionStat.setErrorCause(rto
 									.getPossibleExceptionMessage());
 							actionStat.setFinished(true);
 							actionStat.addElement("Transfer failed: "
 									+ rto.getPossibleException()
 									.getLocalizedMessage());
 							throw new RemoteFileSystemException(rto
 									.getPossibleException()
 									.getLocalizedMessage());
 						} else {
 							actionStat.addElement("Finished transfer of file: "
 									+ source);
 						}
 					}
 					actionStat.setFinished(true);
 				} catch (final Exception e) {
 					myLogger.error(e.getLocalizedMessage(), e);
 					actionStat.setFailed(true);
 					actionStat.setErrorCause(e.getLocalizedMessage());
 					actionStat.setFinished(true);
 					actionStat.addElement("Transfer failed: "
 							+ e.getLocalizedMessage());
 				}
 
 			}
 		};
 		cpThread.setName(actionStat.getHandle());
 		cpThread.start();
 
 		if (waitForFileTransferToFinish) {
 			try {
 				cpThread.join();
 
 				if (actionStat.isFailed()) {
 					throw new RemoteFileSystemException(
 							DtoActionStatus.getLastMessage(actionStat));
 				}
 			} catch (final InterruptedException e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 		}
 
 		return handle;
 
 	}
 
 	// private String createJob(Document jsdl, final String fqan,
 	// final String jobnameCreationMethod,
 	// final BatchJob optionalParentBatchJob)
 	// throws JobPropertiesException {
 	//
 	// return getJobManager().createJob(jsdl, fqan, jobnameCreationMethod,
 	// optionalParentBatchJob);
 	//
 	// }
 
 	/**
 	 * Creates a multipartjob on the server.
 	 * 
 	 * A multipartjob is just a collection of jobs that belong together to make
 	 * them more easily managable.
 	 * 
 	 * @param batchJobname
 	 *            the id (name) of the multipartjob
 	 * @throws JobPropertiesException
 	 */
 	public DtoBatchJob createBatchJob(String batchJobnameBase, String fqan,
 			String jobnameCreationMethod) throws BatchJobException {
 
 		String batchJobname = null;
 		try {
 			batchJobname = Jobhelper.calculateJobname(getUser(),
 					batchJobnameBase, jobnameCreationMethod);
 		} catch (final JobPropertiesException e2) {
 			throw new BatchJobException("Can't calculate jobname: "
 					+ e2.getLocalizedMessage(), e2);
 		}
 
 		if (Constants.NO_JOBNAME_INDICATOR_STRING.equals(batchJobname)) {
 			throw new BatchJobException("BatchJobname can't be "
 					+ Constants.NO_JOBNAME_INDICATOR_STRING);
 		}
 
 		try {
 			final Job possibleJob = getJobManager()
 					.getJobFromDatabaseOrFileSystem(
 							batchJobname);
 			throw new BatchJobException("Can't create multipartjob with id: "
 					+ batchJobname
 					+ ". Non-multipartjob with this id already exists...");
 		} catch (final NoSuchJobException e) {
 			// that's good
 		}
 
 		try {
 			final BatchJob multiJob = getBatchJobManager()
 					.getBatchJobFromDatabase(
 							batchJobname);
 		} catch (final NoSuchJobException e) {
 			// that's good
 
 			final BatchJob multiJobCreate = new BatchJob(getDN(), batchJobname,
 					fqan);
 			multiJobCreate.addJobProperty(Constants.RELATIVE_PATH_FROM_JOBDIR,
 					"../");
 			multiJobCreate.addJobProperty(
 					Constants.RELATIVE_BATCHJOB_DIRECTORY_KEY,
 					ServerPropertiesManager.getRunningJobsDirectoryName() + "/"
 							+ batchJobname);
 
 			multiJobCreate.addLogMessage("MultiPartJob " + batchJobname
 					+ " created.");
 
 			// multiJobCreate
 			// .setResourcesToUse(calculateResourcesToUse(multiJobCreate));
 
 			multiJobCreate.setStatus(JobConstants.JOB_CREATED);
 
 			getBatchJobManager().saveOrUpdate(multiJobCreate);
 
 			try {
 				return multiJobCreate.createDtoMultiPartJob();
 			} catch (final NoSuchJobException e1) {
 				myLogger.error(e1.getLocalizedMessage(), e1);
 			}
 		}
 
 		throw new BatchJobException("MultiPartJob with name " + batchJobname
 				+ " already exists.");
 	}
 
 
 	public String createJob(String jsdlString, final String fqan,
 			final String jobnameCreationMethod) throws JobPropertiesException {
 
 		Document jsdl;
 
 		try {
 			jsdl = SeveralXMLHelpers.fromString(jsdlString);
 		} catch (final Exception e3) {
 
 			myLogger.debug(e3.getLocalizedMessage(), e3);
 			throw new RuntimeException("Invalid jsdl/xml format.", e3);
 		}
 
 		return getJobManager().createJob(jsdl, fqan, jobnameCreationMethod,
 				null);
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#deleteFile(java.lang.String)
 	 */
 	public String deleteFile(final String file)
 			throws RemoteFileSystemException {
 
 		return getFileManager().deleteFile(file);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#deleteFiles(java.lang.String[])
 	 */
 	public String deleteFiles(final DtoStringList files) {
 
 		return getFileManager().deleteFiles(files);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#df()
 	 */
 	public synchronized DtoMountPoints df() {
 
 		return DtoMountPoints.createMountpoints(getUser().getAllMountPoints());
 	}
 
 	public DataHandler download(final String filename)
 			throws RemoteFileSystemException {
 
 		// myLogger.debug("Downloading: " + filename);
 
 		return getUser().getFileManager().download(filename);
 	}
 
 
 
 	// public GridFile fillFolder(GridFile folder, int recursionLevel)
 	// throws RemoteFileSystemException {
 	//
 	// GridFile tempFolder = null;
 	//
 	// try {
 	// tempFolder = getUser().getFileSystemManager().getFolderListing(
 	// folder.getUrl(), 1);
 	// } catch (final Exception e) {
 	// // myLogger.error(e.getLocalizedMessage(), e);
 	// myLogger.error(
 	// "Error getting folder listing. I suspect this to be a bug in the commons-vfs-grid library. Sleeping for 1 seconds and then trying again...",
 	// e);
 	// try {
 	// Thread.sleep(1000);
 	// } catch (final InterruptedException e1) {
 	// myLogger.error(e1.getLocalizedMessage(), e1);
 	// }
 	// tempFolder = getUser().getFileSystemManager().getFolderListing(
 	// folder.getUrl(), 1);
 	//
 	// }
 	// folder.setChildren(tempFolder.getChildren());
 	//
 	// if (recursionLevel > 0) {
 	// for (final GridFile childFolder : tempFolder.getChildren()) {
 	// if (childFolder.isFolder()) {
 	// folder.addChild(fillFolder(childFolder, recursionLevel - 1));
 	// }
 	// }
 	//
 	// }
 	// return folder;
 	// }
 
 	public boolean fileExists(final String file)
 			throws RemoteFileSystemException {
 
 		return getUser().getFileManager().fileExists(file);
 
 	}
 
 
 	public DtoStringList findMatchingSubmissionLocationsUsingMap(
 			final DtoJob jobProperties, final String fqan,
 			boolean excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 		final LinkedList<String> result = new LinkedList<String>();
 
 		final Map<JobSubmissionProperty, String> converterMap = new HashMap<JobSubmissionProperty, String>();
 		for (final DtoProperty jp : jobProperties.getProperties()) {
 			converterMap.put(JobSubmissionProperty.fromString(jp.getKey()),
 					jp.getValue());
 		}
 
 		Collection<Queue> resources = null;
 		if (excludeResourcesWithLessCPUslotsFreeThanRequested) {
 
 			resources = informationManager.findQueues(converterMap, fqan);
 
 			// TODO exclude queues
 		} else {
 			resources = informationManager.findQueues(converterMap, fqan);
 		}
 
 		Collection<String> subLocs = Collections2.transform(resources,
 				Functions.toStringFunction());
 
 		return DtoStringList.fromStringColletion(subLocs);
 
 	}
 
 	public DtoActionStatus getActionStatus(String actionHandle) {
 
 		final DtoActionStatus result = getSessionActionStatus().get(
 				actionHandle);
 
 		// System.out.println("Elements before: " + result.getLog().size());
 
 		return result;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#ps()
 	 */
 	public DtoJobs getActiveJobs(String application, boolean refresh) {
 
 		try {
 
 			final List<Job> jobs = getJobManager()
 					.getActiveJobs(application, refresh);
 
 			final DtoJobs dtoJobs = new DtoJobs();
 			for (final Job job : jobs) {
 
 				final DtoJob dtojob = DtoJob.createJob(job.getStatus(),
 						job.getJobProperties(), job.getInputFiles(),
 						job.getLogMessages(), false);
 
 				// just to make sure
 				dtojob.addJobProperty(Constants.JOBNAME_KEY, job.getJobname());
 				dtoJobs.addJob(dtojob);
 			}
 
 			return dtoJobs;
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllAvailableApplications(java
 	 * .lang.String[])
 	 */
 	public DtoStringList getAllAvailableApplications(final DtoStringList fqans) {
 		final Set<String> fqanList = new TreeSet<String>();
 
 		if ((fqans == null) || (fqans.asSortedSet().size() == 0)) {
 			return DtoStringList.fromStringArray(informationManager
 					.getAllApplicationsOnGrid());
 		}
 
 		for (final String fqan : fqans.getStringList()) {
 			fqanList.addAll(Arrays.asList(informationManager
 					.getAllApplicationsOnGridForVO(fqan)));
 
 		}
 
 		return DtoStringList.fromStringArray(fqanList.toArray(new String[] {}));
 
 	}
 
 	public DtoStringList getAllBatchJobnames(String application) {
 
 		return getBatchJobManager().getAllBatchJobnames(application);
 	}
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see grisu.control.ServiceInterface#getAllHosts()
 	// */
 	// public synchronized DtoHostsInfo getAllHosts() {
 	//
 	// final DtoHostsInfo info = DtoHostsInfo
 	// .createHostsInfo(informationManager.getAllHosts());
 	//
 	// return info;
 	// }
 
 	public DtoStringList getAllJobnames(String application) {
 
 		return getJobManager().getAllJobnames(application);
 	}
 
 	public DtoStringList getAllSites() {
 
 		final Date now = new Date();
 		final DtoStringList result = DtoStringList
 				.fromStringArray(informationManager.getAllSites());
 		myLogger.debug("Login benchmark - getting all sites: "
 				+ (new Date().getTime() - now.getTime()) + " ms");
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllSubmissionLocations()
 	 */
 	public synchronized DtoSubmissionLocations getAllSubmissionLocations() {
 
 		final DtoSubmissionLocations locs = DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocations());
 
 		// locs.removeUnuseableSubmissionLocations(informationManager, df()
 		// .getMountpoints());
 		return locs;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllSubmissionLocations(java
 	 * .lang.String)
 	 */
 	public DtoSubmissionLocations getAllSubmissionLocationsForFqan(
 			final String fqan) {
 
 		final DtoSubmissionLocations locs = DtoSubmissionLocations
 				.createSubmissionLocationsInfoFromQueues(informationManager
 						.getAllSubmissionLocationsForVO(fqan));
 
 		// locs.removeUnuseableSubmissionLocations(informationManager, df()
 		// .getMountpoints());
 		return locs;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getApplicationDetails(java.lang
 	 * .String, java.lang.String, java.lang.String)
 	 */
 	public DtoApplicationDetails getApplicationDetailsForVersionAndSubmissionLocation(
 			final String application, final String version,
 			final String submissionLocation) {
 
 		// String site = site_or_submissionLocation;
 		// if (isSubmissionLocation(site_or_submissionLocation)) {
 		// myLogger.debug("Parameter " + site_or_submissionLocation
 		// + "is submission location not site. Calculating site...");
 		// site = getSiteForSubmissionLocation(site_or_submissionLocation);
 		// myLogger.debug("Site is: " + site);
 		// }
 
 		return DtoApplicationDetails
 				.createDetails(
 						informationManager.getApplicationDetails(application, version,
 								submissionLocation));
 	}
 
 	// public String[] getApplicationPackagesForExecutable(String executable) {
 	//
 	// return informationManager
 	// .getApplicationsThatProvideExecutable(executable);
 	//
 	// }
 
 	public DtoJobs getArchivedJobs(String application) {
 
 		final List<Job> jobs = getJobManager().getArchivedJobs(application);
 
 		final DtoJobs dtoJobs = new DtoJobs();
 		for (final Job job : jobs) {
 
 			final DtoJob dtojob = DtoJob.createJob(job.getStatus(),
 					job.getJobProperties(), job.getInputFiles(),
 					job.getLogMessages(), false);
 
 			// just to make sure
 			dtojob.addJobProperty(Constants.JOBNAME_KEY, job.getJobname());
 			dtoJobs.addJob(dtojob);
 		}
 
 		return dtoJobs;
 	}
 
 	public DtoProperties getArchiveLocations() {
 
 		return DtoProperties.createProperties(getUser().getArchiveLocations());
 
 	}
 
 	/**
 	 * Returns all multipart jobs for this user.
 	 * 
 	 * @return all the multipartjobs of the user
 	 */
 	public DtoBatchJob getBatchJob(String batchJobname)
 			throws NoSuchJobException {
 
 		final BatchJob multiPartJob = getBatchJobManager()
 				.getBatchJobFromDatabase(
 						batchJobname);
 
 		// TODO enable loading of batchjob from jobdirectory url
 
 		return multiPartJob.createDtoMultiPartJob();
 	}
 
 	private UserBatchJobManager getBatchJobManager() {
 		return getUser().getBatchJobManager();
 	}
 
 	public DtoProperties getBookmarks() {
 
 		return DtoProperties.createProperties(getUser().getBookmarks());
 	}
 
 	// /**
 	// * This method has to be implemented by the endpoint specific
 	// * ServiceInterface. Since there are a few different ways to get a proxy
 	// * credential (myproxy, just use the one in /tmp/x509..., shibb,...) this
 	// * needs to be implemented differently for every single situation.
 	// *
 	// * @return the proxy credential that is used to contact the grid
 	// */
 	// protected abstract ProxyCredential getCredential();
 	//
 	// /**
 	// * This is mainly for testing, to enable credentials with specified
 	// * lifetimes.
 	// *
 	// * @param fqan the vo
 	// * @param lifetime
 	// * the lifetime in seconds
 	// * @return the credential
 	// */
 	// protected abstract ProxyCredential getCredential(String fqan, int
 	// lifetime);
 
 	// public DtoDataLocations getDataLocationsForVO(final String fqan) {
 	//
 	// return DtoDataLocations.createDataLocations(fqan,
 	// informationManager.getDataLocationsForVO(fqan));
 	//
 	// }
 
 	/**
 	 * Calculates the default version of an application on a site. This is
 	 * pretty hard to do, so, if you call this method, don't expect anything
 	 * that makes 100% sense, I'm afraid.
 	 * 
 	 * @param application
 	 *            the name of the application
 	 * @param site
 	 *            the site
 	 * @return the default version of the application on this site
 	 */
 	private String getDefaultVersionForApplicationAtSite(
 			final String application, final String site) {
 
 		final Collection<Version> v = informationManager
 				.getVersionsOfApplicationOnSite(application, site);
 
 		final String[] versions = Collections2.transform(v,
 				Functions.toStringFunction()).toArray(new String[] {});
 		double latestVersion = 0;
 		int index = 0;
 		try {
 			latestVersion = Double.valueOf(versions[0]).doubleValue();
 			for (int i = 1; i < versions.length; i++) {
 				if (Double.valueOf(versions[i]).doubleValue() > latestVersion) {
 					index = i;
 				}
 			}
 			return versions[index];
 		} catch (final NumberFormatException e) {
 			return versions[0];
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getDN()
 	 */
 	@RolesAllowed("User")
 	public String getDN() {
 		return getUser().getDn();
 	}
 
 	private UserFileManager getFileManager() {
 		return getUser().getFileManager();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getFileSize(java.lang.String)
 	 */
 	public long getFileSize(final String file) throws RemoteFileSystemException {
 
 		return getUser().getFileManager().getFileSize(file);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getFqans()
 	 */
 	public DtoStringList getFqans() {
 		return DtoStringList.fromStringColletion(getUser().getFqans().keySet());
 	}
 
 	public String getInterfaceInfo(String key) {
 		if ("HOSTNAME".equalsIgnoreCase(key)) {
 			if (hostname == null) {
 				try {
 					final InetAddress addr = InetAddress.getLocalHost();
 					final byte[] ipAddr = addr.getAddress();
 					hostname = addr.getHostName();
 					if (StringUtils.isBlank(hostname)) {
 						hostname = "";
 					} else {
 						hostname = hostname + " / ";
 					}
 					hostname = hostname + addr.getHostAddress();
 				} catch (final UnknownHostException e) {
 					hostname = "Unavailable";
 				}
 			}
 			return hostname;
 		} else if ("VERSION".equalsIgnoreCase(key)) {
 			return grisu.jcommons.utils.Version.get("grisu-core");
 		} else if ("API_VERSION".equalsIgnoreCase(key)) {
 			return Integer.toString(ServiceInterface.API_VERSION);
 		} else if ("TYPE".equalsIgnoreCase(key)) {
 			return "Webservice (REST/SOAP) interface";
 		} else if ("BACKEND_VERSION".equalsIgnoreCase(key)) {
 			return BACKEND_VERSION;
 		}
 
 		return null;
 	}
 
 	abstract public String getInterfaceType();
 
 	public int getInterfaceVersion() {
 		return API_VERSION;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getAllJobProperties(java.lang
 	 * .String)
 	 */
 	public DtoJob getJob(final String jobnameOrUrl) throws NoSuchJobException {
 
 		final Job job = getJobManager().getJobFromDatabaseOrFileSystem(
 				jobnameOrUrl);
 
 		// job.getJobProperties().put(Constants.JOB_STATUS_KEY,
 		// JobConstants.translateStatus(getJobStatus(jobname)));
 
 		return DtoJob.createJob(job.getStatus(), job.getJobProperties(),
 				job.getInputFiles(), job.getLogMessages(), job.isArchived());
 	}
 
 	private UserJobManager getJobManager() {
 		return getUser().getJobManager();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getJobProperty(java.lang.String,
 	 * java.lang.String)
 	 */
 	public String getJobProperty(final String jobname, final String key)
 			throws NoSuchJobException {
 
 		try {
 			final Job job = getJobManager().getJobFromDatabaseOrFileSystem(
 					jobname);
 
 			if (Constants.INPUT_FILE_URLS_KEY.equals(key)) {
 				return StringUtils.join(job.getInputFiles(), ",");
 			}
 
 			return job.getJobProperty(key);
 		} catch (final NoSuchJobException e) {
 			final BatchJob mpj = getBatchJobManager().getBatchJobFromDatabase(
 					jobname);
 			return mpj.getJobProperty(key);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getJobStatus(java.lang.String)
 	 */
 	public int getJobStatus(final String jobname) {
 
 		return getJobManager().getJobStatus(jobname);
 	}
 
 	public String getJsdlDocument(final String jobname)
 			throws NoSuchJobException {
 
 		final Job job = getJobManager().getJobFromDatabaseOrFileSystem(jobname);
 
 		String jsdlString;
 		jsdlString = SeveralXMLHelpers.toString(job.getJobDescription());
 
 		return jsdlString;
 
 	}
 
 	// public String getStagingFileSystem(String site) {
 	// return MountPointManager.getDefaultFileSystem(site);
 	// }
 
 	// abstract protected DtoStringList getSessionFqans();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getMessagesSince(java.util.Date)
 	 */
 	public Document getMessagesSince(final Date date) {
 
 		// TODO
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getMountPointForUri(java.lang
 	 * .String)
 	 */
 	public MountPoint getMountPointForUri(final String uri) {
 
 		return getUser().getResponsibleMountpointForAbsoluteFile(uri);
 	}
 
 	protected Map<String, DtoActionStatus> getSessionActionStatus() {
 		return getUser().getActionStatuses();
 	}
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see grisu.control.ServiceInterface#getSite(java.lang.String)
 	// */
 	// public String getSite(final String host_or_url) {
 	//
 	// return informationManager.getSiteForHostOrUrl(host_or_url);
 	//
 	// }
 	//
 	// /**
 	// * Returns the name of the site for the given submissionLocation.
 	// *
 	// * @param subLoc
 	// * the submissionLocation
 	// * @return the name of the site for the submissionLocation or null, if the
 	// * site can't be found
 	// */
 	// public String getSiteForSubmissionLocation(final String subLoc) {
 	//
 	// // subLoc = queuename@cluster:contactstring#JobManager
 	// // String queueName = subLoc.substring(0, subLoc.indexOf(":"));
 	// String contactString = "";
 	// if (subLoc.indexOf("#") > 0) {
 	// contactString = subLoc.substring(subLoc.indexOf(":") + 1,
 	// subLoc.indexOf("#"));
 	// } else {
 	// contactString = subLoc.substring(subLoc.indexOf(":") + 1);
 	// }
 	//
 	// return getSite(contactString);
 	// }
 
 	public String getSite(String host) {
 		Site site = informationManager.getSiteForHostOrUrl(host);
 		if (site == null) {
 			return null;
 		}
 
 		return site.getName();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.vpac.grisu.control.ServiceInterface#
 	 * getStagingFileSystemForSubmissionLocation(java.lang.String)
 	 */
 	public DtoStringList getStagingFileSystemForSubmissionLocation(
 			final String subLoc) {
 
 		Collection<Directory> queues = informationManager
 				.getStagingFileSystemForSubmissionLocation(subLoc);
 
 		return DtoStringList.fromStringColletion(Collections2.transform(queues,
 				Functions.toStringFunction()));
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getSubmissionLocationsForApplication
 	 * (java.lang.String)
 	 */
 	public DtoSubmissionLocations getSubmissionLocationsForApplication(
 			final String application) {
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocationsForApplication(application));
 	}
 
 	// public UserDAO getUserDao() {
 	// return userdao;
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getSubmissionLocationsForApplication
 	 * (java.lang.String, java.lang.String)
 	 */
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersion(
 			final String application, final String version) {
 
 		final Collection<String> sls = informationManager
 				.getAllSubmissionLocations(
 						application, version);
 
 		return DtoSubmissionLocations.createSubmissionLocationsInfo(sls);
 	}
 
 	public DtoSubmissionLocations getSubmissionLocationsForApplicationAndVersionAndFqan(
 			final String application, final String version, final String fqan) {
 		// TODO implement a method which takes in fqan later on
 
 		return DtoSubmissionLocations
 				.createSubmissionLocationsInfo(informationManager
 						.getAllSubmissionLocations(application, version));
 	}
 
 	public DtoApplicationInfo getSubmissionLocationsPerVersionOfApplication(
 			final String application) {
 		// if (ServerPropertiesManager.getMDSenabled()) {
 		// myLogger.debug("Getting map of submissionlocations per version of application for: "
 		// + application);
 		final Map<String, String> appVersionMap = new HashMap<String, String>();
 		final Collection<String> temp = informationManager
 				.getAllVersionsOfApplicationOnGrid(application);
 		String[] versions;
 		if (temp == null) {
 			versions = new String[] {};
 		} else {
 			versions = temp.toArray(new String[] {});
 		}
 		for (int i = 0; (versions != null) && (i < versions.length); i++) {
 			Collection<String> submitLocations = null;
 			try {
 				submitLocations = informationManager.getAllSubmissionLocations(
 						application, versions[i]);
 				if (submitLocations == null) {
 					myLogger.error("Couldn't find submission locations for application: \""
 							+ application
 							+ "\""
 							+ ", version \""
 							+ versions[i]
 									+ "\". Most likely the mds is not published correctly.");
 					continue;
 				}
 			} catch (final Exception e) {
 				myLogger.error("Couldn't find submission locations for application: \""
 						+ application
 						+ "\""
 						+ ", version \""
 						+ versions[i]
 								+ "\". Most likely the mds is not published correctly.");
 				continue;
 			}
 			final StringBuffer submitLoc = new StringBuffer();
 
 			if (submitLocations != null) {
 				List<String> list = new LinkedList<String>(submitLocations);
 				for (int j = 0; j < list.size(); j++) {
 					submitLoc.append(list.get(j));
 					if (j < (list.size() - 1)) {
 						submitLoc.append(",");
 					}
 				}
 			}
 			appVersionMap.put(versions[i], submitLoc.toString());
 		}
 		return DtoApplicationInfo.createApplicationInfo(application,
 				appVersionMap);
 	}
 
 	public DtoStringList getUsedApplications() {
 
 
 		return DtoStringList.fromStringColletion(getJobManager()
 				.getUsedApplications());
 
 	}
 
 
 	public DtoStringList getUsedApplicationsBatch() {
 
 		return DtoStringList.fromStringColletion(getBatchJobManager()
 				.getUsedApplicationsBatch());
 
 	}
 
 	/**
 	 * Gets the user of the current session. Also connects the default
 	 * credential to it.
 	 * 
 	 * @return the user or null if user could not be created
 	 * @throws NoValidCredentialException
 	 *             if no valid credential could be found to create the user
 	 */
 
 	abstract protected User getUser();
 
 	public DtoProperties getUserProperties() {
 
 		return DtoProperties.createProperties(getUser().getUserProperties());
 	}
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see grisu.control.ServiceInterface#getVersionsOfApplicationOnSite
 	// * (java.lang.String, java.lang.String)
 	// */
 	// public Collection<Version> getVersionsOfApplicationOnSite(
 	// final String application,
 	// final String site) {
 	//
 	// return informationManager.getVersionsOfApplicationOnSite(application,
 	// site);
 	//
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#getUserProperty(java.lang.String)
 	 */
 	public String getUserProperty(final String key) {
 
 		final String value = getUser().getUserProperties().get(key);
 
 		return value;
 	}
 
 	public DtoStringList getVersionsOfApplicationOnSubmissionLocation(
 			final String application, final String submissionLocation) {
 		Collection<Version> v = informationManager
 				.getVersionsOfApplicationOnSubmissionLocation(application,
 						submissionLocation);
 
 		Collection<String> versions = Collections2.transform(v,
 				Functions.toStringFunction());
 
 		return DtoStringList.fromStringColletion(versions);
 	}
 
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#isFolder(java.lang.String)
 	 */
 	public boolean isFolder(final String file) throws RemoteFileSystemException {
 
 		return getUser().getFileManager().isFolder(file);
 
 	}
 
 
 
 
 
 
 	/**
 	 * Tests whether the provided String is a valid submissionLocation. All this
 	 * does at the moment is to check whether there is a ":" within the string,
 	 * so don't depend with your life on the answer to this question...
 	 * 
 	 * @param submissionLocation
 	 *            the submission location
 	 * @return whether the string is a submission location or not
 	 */
 	public boolean isSubmissionLocation(final String submissionLocation) {
 
 		if (submissionLocation.indexOf(":") >= 0) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public String kill(String jobname, boolean clean)
 			throws NoSuchJobException, BatchJobException {
 		return getJobManager().kill(jobname, clean);
 	}
 
 	public String killJobs(DtoStringList jobnames, boolean clean) {
 		return getJobManager().killJobs(jobnames, clean);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#lastModified(java.lang.String)
 	 */
 	public long lastModified(final String url) throws RemoteFileSystemException {
 
 		return getUser().getFileManager().lastModified(url);
 	}
 
 	public GridFile ls(final String directory, int recursion_level)
 			throws RemoteFileSystemException {
 
 		// // check whether credential still valid
 		// getCredential();
 
 		return getUser().ls(directory, recursion_level);
 	}
 
 
 
 
 
 
 
 
 
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#mkdir(java.lang.String)
 	 */
 	public boolean mkdir(final String url) throws RemoteFileSystemException {
 
 		// myLogger.debug("Creating folder: " + url + "...");
 		return getUser().getFileManager().createFolder(url);
 
 	}
 
 	public String redistributeBatchJob(String batchjobname)
 			throws NoSuchJobException, JobPropertiesException {
 		return getBatchJobManager().redistributeBatchJob(batchjobname);
 
 	}
 
 	public String refreshBatchJobStatus(String batchJobname)
 			throws NoSuchJobException {
 		return getBatchJobManager().refreshBatchJobStatus(batchJobname);
 	}
 
 	public void removeJobFromBatchJob(String batchJobname, String jobname)
 			throws NoSuchJobException {
 		getBatchJobManager().removeJobFromBatchJob(batchJobname, jobname);
 
 	}
 
 	public DtoProperties restartBatchJob(String batchjobname,
 			String restartPolicy, DtoProperties properties)
 					throws NoSuchJobException, JobPropertiesException {
 
 		return getBatchJobManager().restartBatchJob(batchjobname,
 				restartPolicy,
 				properties);
 	}
 
 	public void restartJob(String jobname, String changedJsdl)
 			throws JobSubmissionException, NoSuchJobException {
 		getJobManager().restartJob(jobname, changedJsdl);
 
 	}
 
 	public void setDebugProperties(Map<String, String> props) {
 
 		for (final String key : props.keySet()) {
 
 			if ("submitProxyLifetime".equals(key)) {
 				int lt = -1;
 				try {
 					lt = Integer.parseInt(props.get(key));
 					SUBMIT_PROXY_LIFETIME = lt;
 				} catch (final NumberFormatException e) {
 					SUBMIT_PROXY_LIFETIME = -1;
 				}
 			}
 
 		}
 
 	}
 
 	public void setUserProperty(String key, String value) {
 
 		getUser().setUserProperty(key, value);
 
 	}
 
 	public String submitJob(String jobname) throws JobSubmissionException,
 	NoSuchJobException {
 		return getJobManager().submitJob(jobname);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#submitSupportRequest(java.lang
 	 * .String, java.lang.String)
 	 */
 	public void submitSupportRequest(final String subject,
 			final String description) {
 
 		// TODO
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#umount(java.lang.String)
 	 */
 	public void umount(final String mountpoint) {
 
 		getUser().unmountFileSystem(mountpoint);
 
 		getUser().resetMountPoints();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see grisu.control.ServiceInterface#upload(javax.activation.DataSource ,
 	 * java.lang.String)
 	 */
 	public String upload(final DataHandler source, final String filename)
 			throws RemoteFileSystemException {
 
 		return getUser().getFileManager().upload(source, filename);
 
 	}
 
 	public void uploadInputFile(String jobname, DataHandler inputFile,
 			String relativePath) throws RemoteFileSystemException,
 			NoSuchJobException {
 
 		getJobManager().uploadInputFile(jobname, inputFile, relativePath);
 	}
 
 
 }
