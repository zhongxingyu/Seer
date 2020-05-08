 package org.eclipse.b3.aggregator.engine;
 
 import static java.lang.String.format;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.util.DateUtils;
 import org.apache.tools.mail.MailMessage;
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.Contact;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.p2.util.MetadataRepositoryResourceImpl;
 import org.eclipse.b3.aggregator.util.LogUtils;
 import org.eclipse.b3.aggregator.util.MonitorUtils;
 import org.eclipse.b3.aggregator.util.P2Utils;
 import org.eclipse.b3.cli.AbstractCommand;
 import org.eclipse.b3.util.B3Util;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.equinox.internal.p2.core.helpers.FileUtils;
 import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
 import org.eclipse.equinox.internal.provisional.p2.metadata.MetadataFactory.InstallableUnitDescription;
 import org.eclipse.equinox.p2.engine.IProfile;
 import org.eclipse.equinox.p2.engine.IProfileRegistry;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.query.InstallableUnitQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleException;
 import org.osgi.service.packageadmin.PackageAdmin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXParseException;
 
 public class Builder extends AbstractCommand {
 	public enum ActionType{
 		CLEAN, VERIFY, BUILD, CLEAN_BUILD
 	}
 
 	private static class EmailAddress {
 		private final String address;
 
 		private final String personal;
 
 		EmailAddress(String address, String personal) {
 			this.address = address;
 			this.personal = personal;
 		}
 
 		@Override
 		public String toString() {
 			if(personal == null)
 				return address;
 
 			return personal + " <" + address + ">";
 		}
 	}
 
 	public static final String ALL_CONTRIBUTED_CONTENT_FEATURE = "all.contributed.content.feature.group"; //$NON-NLS-1$
 
 	public static final String PDE_TARGET_PLATFORM_NAMESPACE = "A.PDE.Target.Platform";
 
 	public static final String PDE_TARGET_PLATFORM_NAME = "Cannot be installed into the IDE";
 
 	public static final Version ALL_CONTRIBUTED_CONTENT_VERSION = Version.createOSGi(1, 0, 0);
 
 	public static final String COMPOSITE_ARTIFACTS_TYPE = org.eclipse.equinox.internal.p2.artifact.repository.Activator.ID
 			+ ".compositeRepository"; //$NON-NLS-1$
 
 	public static final String COMPOSITE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID
 			+ ".compositeRepository"; //$NON-NLS-1$
 
 	public static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
 
 	public static final String NAMESPACE_OSGI_BUNDLE = "osgi.bundle"; //$NON-NLS-1$
 
 	public static final String PROFILE_ID = "GalileoTest"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_VERIFICATION = "verification"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_FINAL = "final"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_INTERIM = "interim"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_TEMP = "temp"; //$NON-NLS-1$
 
 	public static final String REPO_FOLDER_AGGREGATE = "aggregate"; //$NON-NLS-1$
 
 	public static final String SIMPLE_ARTIFACTS_TYPE = org.eclipse.equinox.internal.p2.artifact.repository.Activator.ID
 			+ ".simpleRepository"; //$NON-NLS-1$
 
 	public static final String SIMPLE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID
 			+ ".simpleRepository"; //$NON-NLS-1$
 
 	public static final String INTERNAL_METADATA_TYPE = "org.eclipse.b3.aggregator.engine.internalRepository"; //$NON-NLS-1$
 
 	public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmm"); //$NON-NLS-1$
 
 	static final String FEATURE_GROUP_SUFFIX = ".feature.group"; //$NON-NLS-1$
 
 	static final IArtifactKey[] NO_ARTIFACT_KEYS = new IArtifactKey[0];
 
 	private static final String BUNDLE_ECF_FS_PROVIDER = "org.eclipse.ecf.provider.filetransfer"; //$NON-NLS-1$
 
 	private static final String BUNDLE_EXEMPLARY_SETUP = "org.eclipse.equinox.p2.exemplarysetup"; //$NON-NLS-1$
 
 	private static final String BUNDLE_UPDATESITE = "org.eclipse.equinox.p2.updatesite"; //$NON-NLS-1$
 
 	private static final String BUNDLE_CORE = "org.eclipse.equinox.p2.core"; //$NON-NLS-1$
 
 	static private final String BUNDLE_ENGINE = "org.eclipse.equinox.p2.engine"; //$NON-NLS-1$
 
 	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$
 
 	private static final String PROP_P2_DATA_AREA = "eclipse.p2.data.area"; //$NON-NLS-1$
 
 	private static final String PROP_P2_PROFILE = "eclipse.p2.profile"; //$NON-NLS-1$
 
 	private static final Project PROPERTY_REPLACER = new Project();
 
 	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmm"); //$NON-NLS-1$
 
 	static {
 		TimeZone utc = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
 		PROPERTY_REPLACER.initProperties();
 		DATE_FORMAT.setTimeZone(utc);
 		TIME_FORMAT.setTimeZone(utc);
 		TIMESTAMP_FORMAT.setTimeZone(utc);
 	}
 
 	/**
 	 * Creates a repository location without the trailing slash that will be added if the standard
 	 * {@link java.io.File#toURI()} is used.
 	 * 
 	 * @param repoLocation
 	 *            The location. Must be an absolute path.
 	 * @return The created URI.
 	 * @throws CoreException
 	 *             if the argument is not an absolute path
 	 */
 	public static final URI createURI(File repoLocation) throws CoreException {
 		if(repoLocation != null) {
 			IPath path = Path.fromOSString(repoLocation.getPath());
 			if(path.isAbsolute())
 				try {
 					String pathStr = path.removeTrailingSeparator().toPortableString();
 					if(!pathStr.startsWith("/"))
 						// Path starts with a drive letter
 						pathStr = "/" + pathStr; //$NON-NLS-1$
 					return new URI("file", null, pathStr, null); //$NON-NLS-1$
 				}
 				catch(URISyntaxException e) {
 					throw ExceptionUtils.wrap(e);
 				}
 		}
 		throw ExceptionUtils.fromMessage("File %s is not an absolute path", repoLocation);
 	}
 
 	public static String getExceptionMessages(Throwable e) {
 		StringBuilder bld = new StringBuilder();
 		getExceptionMessages(e, bld);
 		return bld.toString();
 	}
 
 	public static IInstallableUnit getIU(IMetadataRepository mdr, String id, String version) {
 		version = StringUtils.trimmedOrNull(version);
 		InstallableUnitQuery query = version == null
 				? new InstallableUnitQuery(id)
 				: new InstallableUnitQuery(id, Version.create(version));
 		IQueryResult<IInstallableUnit> result = mdr.query(query, null);
 		return !result.isEmpty()
 				? result.iterator().next()
 				: null;
 	}
 
 	private static void deleteAndCheck(File folder, String fileName) throws CoreException {
 		File file = new File(folder, fileName);
 		file.delete();
 		if(file.exists())
 			throw ExceptionUtils.fromMessage("Unable to delete file %s\n", file.getAbsolutePath());
 	}
 
 	private static void deleteMetadataRepository(IMetadataRepositoryManager mdrMgr, File repoFolder)
 			throws CoreException {
 		URI repoURI = Builder.createURI(repoFolder);
 		mdrMgr.removeRepository(repoURI);
 		deleteAndCheck(repoFolder, "compositeContent.jar");
 		deleteAndCheck(repoFolder, "compositeContent.xml");
 		deleteAndCheck(repoFolder, "content.jar");
 		deleteAndCheck(repoFolder, "content.xml");
 	}
 
 	private static synchronized Bundle getBundle(PackageAdmin packageAdmin, String symbolicName) {
 		Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
 		if(bundles == null)
 			return null;
 
 		// Return the first bundle that is not installed or uninstalled
 		for(int i = 0; i < bundles.length; i++) {
 			Bundle bundle = bundles[i];
 			if((bundle.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0)
 				return bundle;
 		}
 		return null;
 	}
 
 	private static void getExceptionMessages(Throwable e, StringBuilder bld) {
 		bld.append(e.getClass().getName());
 		bld.append(": ");
 		if(e.getMessage() != null)
 			bld.append(e.getMessage());
 
 		if(e instanceof CoreException)
 			e = ((CoreException) e).getStatus().getException();
 		else {
 			Throwable t = e.getCause();
 			e = (t == e)
 					? null
 					: t;
 		}
 		if(e != null) {
 			bld.append("\nCaused by: ");
 			getExceptionMessages(e, bld);
 		}
 	}
 
 	private static void send(String host, int port, EmailAddress from, List<EmailAddress> toList, EmailAddress cc,
 			String subject, String message) throws IOException {
 		MailMessage mailMessage = new MailMessage(host, port);
 		mailMessage.from(from.toString());
 		for(EmailAddress to : toList)
 			mailMessage.to(to.toString());
 		if(cc != null)
 			mailMessage.cc(cc.toString());
 		mailMessage.setSubject(subject);
 		mailMessage.setHeader("Date", DateUtils.getDateForHeader());
 		mailMessage.setHeader("Content-Type", "text/plain; charset=us-ascii");
 		PrintStream out = mailMessage.getPrintStream();
 		out.print(message);
 		mailMessage.sendAndClose();
 	}
 
 	private static boolean startEarly(PackageAdmin packageAdmin, String bundleName) throws BundleException {
 		Bundle bundle = getBundle(packageAdmin, bundleName);
 		if(bundle == null)
 			return false;
 		bundle.start(Bundle.START_TRANSIENT);
 		return true;
 	}
 
 	private static boolean stopBundle(PackageAdmin packageAdmin, String bundleName) throws BundleException {
 		Bundle bundle = getBundle(packageAdmin, bundleName);
 		if(bundle == null || bundle.getState() != Bundle.ACTIVE)
 			return false;
 		bundle.stop(Bundle.STOP_TRANSIENT);
 		return true;
 	}
 
 	// === OPTIONS ===
 
 	@Option(name = "--action", usage = "Specifies the type of the execution. Default is BUILD.")
 	private ActionType action = ActionType.BUILD;
 
 	@Option(name = "--buildModel", required = true, usage = "Appoints the aggregation definition that drives the execution")
 	private File buildModelLocation;
 
 	@Option(name = "--buildId", usage = "Assigns a build identifier to the aggregation. "
 			+ "The identifier is used to identify the build in notification emails. Defaults to: "
 			+ "build-<timestamp> where <timestamp> is formatted according as yyyyMMddHHmm, i.e. build-200911031527", metaVar = "<ID>")
 	private String buildID;
 
 	@Option(name = "--buildRoot", usage = "Controls the output. Defaults to the build root defined in the"
 			+ " aggregation definition.")
 	private File buildRoot;
 
 	@Option(name = "--logURL", usage = "The URL that will be pasted into the emails. "
 			+ "Should normally point to the a public URL for output log for the aggregator so "
 			+ "that the receiver can browse the log for details on failures.", metaVar = "<url>")
 	private String logURL;
 
 	@Option(name = "--production", usage = "Indicates that the build is running in real production. "
 			+ "That means that no mock emails will be sent. Instead, the contacts listed for each contribution will get emails when things go wrong.")
 	private boolean production = false;
 
 	@Option(name = "--mockEmailCc", usage = "Becomes the CC receiver of the mock-emails sent from the aggregator", metaVar = "<address>")
 	private String mockEmailCC;
 
 	@Option(name = "--emailFrom", usage = "Becomes the sender of the emails sent from the aggregator. "
 			+ "Defaults to the build master defined in the aggregator definition.", metaVar = "<address>")
 	private String emailFrom;
 
 	@Option(name = "--emailFromName", usage = "Mock sender's name", metaVar = "<name>")
 	private String emailFromName;
 
 	@Option(name = "--mockEmailTo", usage = "Becomes the receiver of the mock-emails sent from the aggregator", metaVar = "<address>")
 	private String mockEmailTo;
 
 	@Option(name = "--smtpHost", usage = "The SMTP host to talk to when sending emails. Defaults to \"localhost\".", metaVar = "<host>")
 	private String smtpHost;
 
 	@Option(name = "--smtpPort", usage = "The SMTP port number to use when talking to the SMTP host. Default is 25.", metaVar = "<port>")
 	private int smtpPort;
 
 	@Option(name = "--subjectPrefix", usage = "The prefix to use for the subject when sending emails. "
 			+ "Defaults to the label defined in the aggregation definition. "
 			+ "The subject is formatted as: \"[<subjectPrefix>] Failed for build <buildId>\"", metaVar = "<subject>")
 	private String subjectPrefix;
 
 	@Argument
 	private List<String> unparsed = new ArrayList<String>();
 
 	// === END OF OPTIONS ===
 
 	private Aggregator aggregator;
 
 	private String buildLabel;
 
 	private String buildMasterEmail;
 
 	private String buildMasterName;
 
 	private List<IInstallableUnit> categoryIUs;
 
 	private boolean mirrorReferences = false;;
 
 	private Pattern referenceIncludePattern;
 
 	private Pattern referenceExcludePattern;
 
 	private ResourceSet resourceSet;
 
 	private boolean sendmail = false;
 
 	final private Set<IInstallableUnit> unitsToAggregate = new HashSet<IInstallableUnit>();
 
 	private Set<MappedRepository> exclusions;
 
 	private CompositeMetadataRepository sourceComposite;
 
 	private String preservedDataArea;
 
 	private String preservedProfile;
 
 	/**
 	 * Prevent that the {@link IInstallableUnit} identified by <code>versionedName</code> is mapped from
 	 * <code>repository</code>.
 	 * 
 	 * @param repository
 	 *            The repository for which to exclude a mapping
 	 * @param rc
 	 *            The required capability to be excluded/replaceed
 	 */
 	public void addMappingExclusion(MappedRepository repository) {
 		if(exclusions == null) {
 			exclusions = new HashSet<MappedRepository>();
 		}
 		exclusions.add(repository);
 	}
 
 	public Aggregator getAggregator() {
 		return aggregator;
 	}
 
 	public String getBuildID() {
 		return buildID;
 	}
 
 	public File getBuildModelLocation() {
 		return buildModelLocation;
 	}
 
 	public File getBuildRoot() {
 		return buildRoot;
 	}
 
 	public List<IInstallableUnit> getCategoryIUs() {
 		return categoryIUs;
 	}
 
 	@Override
 	public String getShortDescription() {
 		return "Aggregates source repositories into a resulting repository using aggregator definition";
 	}
 
 	public CompositeMetadataRepository getSourceComposite() {
 		return sourceComposite;
 	}
 
 	public URI getSourceCompositeURI() throws CoreException {
 		return createURI(new File(buildRoot, REPO_FOLDER_INTERIM));
 	}
 
 	public File getTempRepositoryFolder() {
 		return new File(buildRoot, REPO_FOLDER_TEMP);
 	}
 
 	public Set<IInstallableUnit> getUnitsToAggregate() {
 		return unitsToAggregate;
 	}
 
 	public boolean isCleanBuild() {
 		return action == ActionType.CLEAN_BUILD;
 	}
 
 	/**
 	 * Checks if the repository can be included verbatim. If it can, the builder will include a reference to it in a
 	 * composite repository instead of copying everything into an aggregate
 	 * 
 	 * @param repo
 	 *            The repository to check
 	 * @return true if the repository is mapped verbatim.
 	 */
 	public boolean isMapVerbatim(MappedRepository repo) {
 		return !(repo.isMapExclusive() || repo.isMirrorArtifacts())
 				&& StringUtils.trimmedOrNull(repo.getCategoryPrefix()) == null
 				&& !(exclusions != null && exclusions.contains(repo) && "p2".equals(repo.getNature()));
 	}
 
 	public boolean isMatchedReference(String reference) {
 		reference = StringUtils.trimmedOrNull(reference);
 		if(reference == null)
 			return false;
 
 		if(referenceIncludePattern != null) {
 			Matcher m = referenceIncludePattern.matcher(reference);
 			if(!m.matches())
 				return false;
 		}
 
 		if(referenceExcludePattern != null) {
 			Matcher m = referenceExcludePattern.matcher(reference);
 			if(m.matches())
 				return false;
 		}
 		return true;
 	}
 
 	public boolean isMirrorReferences() {
 		return mirrorReferences;
 	}
 
 	public boolean isProduction() {
 		return production;
 	}
 
 	public boolean isTopLevelCategory(IInstallableUnit iu) {
 		return iu != null && "true".equalsIgnoreCase(iu.getProperty(InstallableUnitDescription.PROP_TYPE_CATEGORY))
 				&& !"true".equalsIgnoreCase(iu.getProperty(InstallableUnitDescription.PROP_TYPE_GROUP));
 	}
 
 	public boolean isVerifyOnly() {
 		return action == ActionType.VERIFY;
 	}
 
 	/**
 	 * Run the build
 	 * 
 	 * @param monitor
 	 */
 	public int run(boolean fromIDE, IProgressMonitor monitor) throws Exception {
 		int ticks;
 		switch(action) {
 		case CLEAN:
 			ticks = 50;
 			break;
 		case VERIFY:
 			ticks = 200;
 			break;
 		case BUILD:
 			ticks = 2150;
 			break;
 		default:
 			ticks = 2200;
 		}
 		MonitorUtils.begin(monitor, ticks);
 
 		try {
 			if(buildModelLocation == null)
 				throw ExceptionUtils.fromMessage("No buildmodel has been set");
 
 			Date now = new Date();
 			if(buildID == null)
 				buildID = "build-" + DATE_FORMAT.format(now) + TIME_FORMAT.format(now);
 
 			if(smtpHost == null)
 				smtpHost = "localhost";
 
 			if(smtpPort <= 0)
 				smtpPort = 25;
 
 			// Verify that all contributions can be parsed, i.e. contains
 			// well-formed XML
 			//
 			verifyContributions();
 			runTransformation(now);
 			switch(action) {
 			case CLEAN:
 			case CLEAN_BUILD:
 				cleanAll();
 				break;
 			default:
 				cleanMetadata();
 			}
 
 			if(action == ActionType.CLEAN)
 				return 0;
 
 			buildRoot.mkdirs();
 			if(!buildRoot.exists())
 				throw ExceptionUtils.fromMessage("Failed to create folder %s", buildRoot);
 
 			B3Util b3util = B3Util.getPlugin();
 			restartP2Bundles();
 			try {
 				// Remove previously used profiles.
 				//
 				IProfileRegistry profileRegistry = b3util.getService(IProfileRegistry.class);
 				try {
 					List<String> knownIDs = new ArrayList<String>();
 					for(IProfile profile : profileRegistry.getProfiles()) {
 						String id = profile.getProfileId();
 						if(id.startsWith(PROFILE_ID))
 							knownIDs.add(id);
 					}
 
 					for(String id : knownIDs)
 						profileRegistry.removeProfile(id);
 
 					String instArea = buildRoot.toString();
 					Map<String, String> props = new HashMap<String, String>();
 					// TODO Where is PROP_FLAVOR gone?
 					//props.put(IProfile.PROP_FLAVOR, "tooling"); //$NON-NLS-1$
 					props.put(IProfile.PROP_NAME, aggregator.getLabel());
 					props.put(IProfile.PROP_DESCRIPTION, format("Default profile during %s build",
 							aggregator.getLabel()));
 					props.put(IProfile.PROP_CACHE, instArea);
 					props.put(IProfile.PROP_INSTALL_FOLDER, instArea);
 					profileRegistry.addProfile(PROFILE_ID, props);
 				}
 				finally {
 					b3util.ungetService(profileRegistry);
 				}
 
 				startAsynchronousLoadForAllMappedRepositories();
 				runCompositeGenerator(MonitorUtils.subMonitor(monitor, 70));
 				runVerificationFeatureGenerator(MonitorUtils.subMonitor(monitor, 15));
 				runCategoriesRepoGenerator(MonitorUtils.subMonitor(monitor, 15));
 				runRepositoryVerifier(MonitorUtils.subMonitor(monitor, 100));
 				if(action != ActionType.VERIFY)
 					runMirroring(MonitorUtils.subMonitor(monitor, 2000));
 				return 0;
 			}
 			finally {
 				restoreP2Bundles();
 			}
 		}
 		catch(Throwable e) {
 			LogUtils.error(e, "Build failed! Exception was %s", getExceptionMessages(e));
 			if(e instanceof Exception)
 				throw (Exception) e;
 
 			throw new Exception(e);
 		}
 		finally {
 			if(fromIDE && buildRoot != null) {
 				IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 				for(IContainer rootContainer : wsRoot.findContainersForLocationURI(buildRoot.toURI()))
 					try {
 						rootContainer.refreshLocal(IResource.DEPTH_INFINITE, MonitorUtils.subMonitor(monitor, 10));
 					}
 					catch(CoreException e) {
 						// ignore
 					}
 			}
 			monitor.done();
 		}
 	}
 
 	public void sendEmail(Contribution contrib, List<String> errors) {
 		boolean useMock = (mockEmailTo != null);
 		if(!((production || useMock) && sendmail))
 			return;
 
 		try {
 			EmailAddress buildMaster = new EmailAddress(buildMasterEmail, buildMasterName);
 			EmailAddress emailFromAddr;
 			if(emailFrom != null)
 				emailFromAddr = new EmailAddress(emailFrom, emailFromName);
 			else
 				emailFromAddr = buildMaster;
 
 			List<EmailAddress> toList = new ArrayList<EmailAddress>();
 			if(contrib == null)
 				toList.add(buildMaster);
 			else
 				for(Contact contact : contrib.getContacts())
 					toList.add(new EmailAddress(contact.getEmail(), contact.getName()));
 
 			StringBuilder msgBld = new StringBuilder();
 			msgBld.append("The following error");
 			if(errors.size() > 1)
 				msgBld.append('s');
 			msgBld.append(" occured when building ");
 			msgBld.append(buildLabel);
 			msgBld.append(":\n\n");
 			for(String error : errors) {
 				msgBld.append(error);
 				msgBld.append("\n\n");
 			}
 
 			if(logURL != null) {
 				msgBld.append("Check the log file for more information: ");
 				msgBld.append(logURL);
 				msgBld.append('\n');
 			}
 
 			if(useMock) {
 				msgBld.append("\nThis is a mock mail. Real recipients would have been:\n");
 				for(EmailAddress to : toList) {
 					msgBld.append("  ");
 					msgBld.append(to);
 					msgBld.append('\n');
 				}
 			}
 			String msgContent = msgBld.toString();
 			if(subjectPrefix == null)
 				subjectPrefix = buildLabel;
 
 			String subject = format("[%s] Failed for build %s", subjectPrefix, buildID);
 
 			msgBld.setLength(0);
 			msgBld.append("Sending email to: ");
 			for(EmailAddress to : toList) {
 				msgBld.append(to);
 				msgBld.append(',');
 			}
 			msgBld.append(buildMaster);
 			if(useMock) {
 				msgBld.append(" *** Using mock: ");
 				if(mockEmailTo != null) {
 					msgBld.append(mockEmailTo);
 					if(mockEmailCC != null) {
 						msgBld.append(',');
 						msgBld.append(mockEmailTo);
 					}
 				}
 				else
 					msgBld.append(mockEmailCC);
 				msgBld.append(" ***");
 			}
 			LogUtils.info(msgBld.toString());
 			LogUtils.info("From: %s", emailFromAddr);
 			LogUtils.info("Subject: %s", subject);
 			LogUtils.info("Message content: %s", msgContent);
 
 			List<EmailAddress> recipients;
 			EmailAddress ccRecipient = null;
 			if(useMock) {
 				recipients = mockRecipients();
 				ccRecipient = mockCCRecipient();
 			}
 			else {
 				recipients = toList;
 				if(contrib != null)
 					ccRecipient = buildMaster;
 			}
 			send(smtpHost, smtpPort, emailFromAddr, recipients, ccRecipient, subject, msgContent);
 
 		}
 		catch(IOException e) {
 			LogUtils.error(e, "Failed to send email: %s", e.getMessage());
 		}
 	}
 
 	public void setAction(ActionType action) {
 		this.action = action;
 	}
 
 	public void setBuildID(String buildId) {
 		this.buildID = buildId;
 	}
 
 	public void setBuildModelLocation(File buildModelLocation) {
 		this.buildModelLocation = buildModelLocation;
 	}
 
 	public void setBuildRoot(File buildRoot) {
 		this.buildRoot = buildRoot;
 	}
 
 	public void setCategoryIUs(List<IInstallableUnit> categoryIUs) {
 		this.categoryIUs = categoryIUs;
 	}
 
 	public void setEmailFrom(String emailFrom) {
 		this.emailFrom = emailFrom;
 	}
 
 	public void setEmailFromName(String emailFromName) {
 		this.emailFromName = emailFromName;
 	}
 
 	public void setLogLevel(int level) {
 		throw new UnsupportedOperationException("Log levels are not supported");
 	}
 
 	public void setLogURL(String logURL) {
 		this.logURL = logURL;
 	}
 
 	public void setMirrorReferences(boolean mirrorReferences) {
 		this.mirrorReferences = mirrorReferences;
 	}
 
 	public void setMockEmailCC(String mockEmailCc) {
 		this.mockEmailCC = mockEmailCc;
 	}
 
 	public void setMockEmailTo(String mockEmailTo) {
 		this.mockEmailTo = mockEmailTo;
 	}
 
 	public void setProduction(boolean production) {
 		this.production = production;
 	}
 
 	public void setReferenceExcludePattern(String pattern) {
 		pattern = StringUtils.trimmedOrNull(pattern);
 		referenceExcludePattern = pattern == null
 				? null
 				: Pattern.compile(pattern);
 	}
 
 	public void setReferenceIncludePattern(String pattern) {
 		pattern = StringUtils.trimmedOrNull(pattern);
 		referenceIncludePattern = pattern == null
 				? null
 				: Pattern.compile(pattern);
 	}
 
 	public void setSmtpHost(String smtpHost) {
 		this.smtpHost = smtpHost;
 	}
 
 	public void setSmtpPort(int smtpPort) {
 		this.smtpPort = smtpPort;
 	}
 
 	public void setSourceComposite(CompositeMetadataRepository sourceComposite) {
 		this.sourceComposite = sourceComposite;
 	}
 
 	public void setSubjectPrefix(String subjectPrefix) {
 		this.subjectPrefix = subjectPrefix;
 	}
 
 	@Override
 	protected int run(IProgressMonitor monitor) throws Exception {
 		if(unparsed.size() > 0)
 			throw new Exception("Too many arguments");
 		return run(false, monitor);
 	}
 
 	private void cleanAll() throws CoreException {
 		cleanMetadata();
 
 		IArtifactRepositoryManager arMgr = P2Utils.getRepositoryManager(IArtifactRepositoryManager.class);
 		try {
 			File destination = new File(buildRoot, Builder.REPO_FOLDER_FINAL);
 			arMgr.removeRepository(Builder.createURI(destination));
 			arMgr.removeRepository(Builder.createURI(new File(destination, Builder.REPO_FOLDER_AGGREGATE)));
 		}
 		finally {
 			P2Utils.ungetRepositoryManager(arMgr);
 		}
 
 		if(buildRoot.exists()) {
 			FileUtils.deleteAll(buildRoot);
 			if(buildRoot.exists())
 				throw ExceptionUtils.fromMessage("Failed to delete folder %s", buildRoot.getAbsolutePath());
 		}
 	}
 
 	private void cleanMetadata() throws CoreException {
 		IMetadataRepositoryManager mdrMgr = P2Utils.getRepositoryManager(IMetadataRepositoryManager.class);
 		try {
 			File finalRepo = new File(buildRoot, Builder.REPO_FOLDER_FINAL);
 			deleteMetadataRepository(mdrMgr, finalRepo);
 			deleteMetadataRepository(mdrMgr, new File(finalRepo, Builder.REPO_FOLDER_AGGREGATE));
 			File interimRepo = new File(buildRoot, Builder.REPO_FOLDER_INTERIM);
 			deleteMetadataRepository(mdrMgr, interimRepo);
 			deleteMetadataRepository(mdrMgr, new File(interimRepo, Builder.REPO_FOLDER_VERIFICATION));
 		}
 		finally {
 			P2Utils.ungetRepositoryManager(mdrMgr);
 		}
 	}
 
 	private EmailAddress mockCCRecipient() throws UnsupportedEncodingException {
 		EmailAddress mock = null;
 		if(mockEmailCC != null)
 			mock = new EmailAddress(mockEmailCC, null);
 		return mock;
 	}
 
 	private List<EmailAddress> mockRecipients() throws UnsupportedEncodingException {
 		if(mockEmailTo != null)
 			return Collections.singletonList(new EmailAddress(mockEmailTo, null));
 		return Collections.emptyList();
 	}
 
 	private void restartP2Bundles() throws CoreException {
 		B3Util b3util = B3Util.getPlugin();
 		PackageAdmin packageAdmin = b3util.getService(PackageAdmin.class);
 
 		try {
 			stopBundle(packageAdmin, BUNDLE_EXEMPLARY_SETUP);
 			stopBundle(packageAdmin, BUNDLE_ENGINE);
 			stopBundle(packageAdmin, BUNDLE_CORE);
 
 			String p2DataArea = new File(buildRoot, "p2").toString();
 			preservedDataArea = System.setProperty(PROP_P2_DATA_AREA, p2DataArea);
 			preservedProfile = System.setProperty(PROP_P2_PROFILE, PROFILE_ID);
 
 			if(!startEarly(packageAdmin, BUNDLE_ECF_FS_PROVIDER))
 				throw ExceptionUtils.fromMessage("Missing bundle %s", BUNDLE_ECF_FS_PROVIDER);
 			if(!startEarly(packageAdmin, BUNDLE_CORE))
 				throw ExceptionUtils.fromMessage("Missing bundle %s", BUNDLE_CORE);
 			if(!startEarly(packageAdmin, BUNDLE_ENGINE))
 				throw ExceptionUtils.fromMessage("Missing bundle %s", BUNDLE_ENGINE);
 			if(!startEarly(packageAdmin, BUNDLE_EXEMPLARY_SETUP))
 				throw ExceptionUtils.fromMessage("Missing bundle %s", BUNDLE_EXEMPLARY_SETUP);
 			if(!startEarly(packageAdmin, BUNDLE_UPDATESITE))
 				throw ExceptionUtils.fromMessage("Missing bundle %s", BUNDLE_UPDATESITE);
 		}
 		catch(BundleException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		finally {
 			b3util.ungetService(packageAdmin);
 		}
 	}
 
 	private void restoreP2Bundles() throws CoreException {
 		B3Util b3util = B3Util.getPlugin();
 		PackageAdmin packageAdmin = b3util.getService(PackageAdmin.class);
 
 		try {
 			stopBundle(packageAdmin, BUNDLE_EXEMPLARY_SETUP);
 			stopBundle(packageAdmin, BUNDLE_ENGINE);
 			stopBundle(packageAdmin, BUNDLE_CORE);
 
 			if(preservedDataArea == null)
 				System.clearProperty(PROP_P2_DATA_AREA);
 			else
 				System.setProperty(PROP_P2_DATA_AREA, preservedDataArea);
 			if(preservedProfile == null)
 				System.clearProperty(PROP_P2_PROFILE);
 			else
 				System.setProperty(PROP_P2_PROFILE, preservedProfile);
 
 			startEarly(packageAdmin, BUNDLE_CORE);
 			startEarly(packageAdmin, BUNDLE_ENGINE);
 			startEarly(packageAdmin, BUNDLE_EXEMPLARY_SETUP);
 		}
 		catch(BundleException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		finally {
 			b3util.ungetService(packageAdmin);
 		}
 	}
 
 	private void runCategoriesRepoGenerator(IProgressMonitor monitor) throws CoreException {
 		CategoriesGenerator generator = new CategoriesGenerator(this);
 		generator.run(monitor);
 	}
 
 	private void runCompositeGenerator(IProgressMonitor monitor) throws CoreException {
 		SourceCompositeGenerator generator = new SourceCompositeGenerator(this);
 		generator.run(monitor);
 	}
 
 	private void runMirroring(IProgressMonitor monitor) throws CoreException {
 		MirrorGenerator generator = new MirrorGenerator(this);
 		generator.run(monitor);
 	}
 
 	private void runRepositoryVerifier(IProgressMonitor monitor) throws CoreException {
 		RepositoryVerifier ipt = new RepositoryVerifier(this);
 		ipt.run(monitor);
 	}
 
 	/**
 	 * Runs the transformation and loads the model into memory
 	 * 
 	 * @throws CoreException
 	 *             If something goes wrong with during the process
 	 */
 	private void runTransformation(Date now) throws CoreException {
 		try {
 			// Load the Java model into memory
 			resourceSet = new ResourceSetImpl();
 			org.eclipse.emf.common.util.URI fileURI = org.eclipse.emf.common.util.URI.createFileURI(buildModelLocation.getAbsolutePath());
 			Resource resource = resourceSet.getResource(fileURI, true);
 			EList<EObject> content = resource.getContents();
 			if(content.size() != 1)
 				throw ExceptionUtils.fromMessage("ECore Resource did not contain one resource. It had %d",
 						Integer.valueOf(content.size()));
 
 			aggregator = (Aggregator) content.get(0);
 			Diagnostic diag = Diagnostician.INSTANCE.validate((EObject) aggregator);
 			if(diag.getSeverity() == Diagnostic.ERROR) {
 				for(Diagnostic childDiag : diag.getChildren())
 					LogUtils.error(childDiag.getMessage());
 				throw ExceptionUtils.fromMessage("Build model validation failed: %s", diag.getMessage());
 			}
 
 			if(buildRoot == null)
 				setBuildRoot(new File(PROPERTY_REPLACER.replaceProperties(aggregator.getBuildRoot())));
 
 			if(!buildRoot.isAbsolute())
 				buildRoot = new File(buildModelLocation.getParent(), buildRoot.getPath());
 		}
 		catch(Exception e) {
 			throw ExceptionUtils.wrap(e);
 		}
 	}
 
 	private void runVerificationFeatureGenerator(IProgressMonitor monitor) throws CoreException {
 		VerificationFeatureGenerator generator = new VerificationFeatureGenerator(this);
 		generator.run(monitor);
 	}
 
 	private void startAsynchronousLoadForAllMappedRepositories() {
 		for(MetadataRepositoryReference repo : getAggregator().getAllMetadataRepositoryReferences(true)) {
 			MetadataRepositoryResourceImpl res = (MetadataRepositoryResourceImpl) MetadataRepositoryResourceImpl.getResourceForNatureAndLocation(
 					repo.getNature(), repo.getResolvedLocation(), repo.getAggregator());
 			res.startAsynchronousLoad(false);
 		}
 	}
 
 	private void verifyContributions() throws CoreException {
		File parentFolder = buildModelLocation.getParentFile();
		if(parentFolder == null)
			parentFolder = new File(".");
		URI buildModelFolderURI = parentFolder.toURI();
 		DocumentBuilderFactory docBldFact = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBld;
 		try {
 			docBld = docBldFact.newDocumentBuilder();
 		}
 		catch(ParserConfigurationException e) {
 			throw new RuntimeException(e);
 		}
 
 		Element masterBuild;
 		try {
 			Document doc = docBld.parse(buildModelLocation);
 			masterBuild = doc.getDocumentElement();
 			sendmail = "true".equalsIgnoreCase(masterBuild.getAttribute("sendmail"));
 			buildLabel = StringUtils.trimmedOrNull(masterBuild.getAttribute("label"));
 		}
 		catch(Exception e) {
 			String msg;
 			if(e instanceof SAXParseException)
 				msg = format("Unable to parse file: %s: Error at line %s: %s", buildModelLocation,
 						Integer.valueOf(((SAXParseException) e).getLineNumber()), e.getMessage());
 			else
 				msg = format("Unable to parse file '%s': %s", buildModelLocation, e.getMessage());
 			throw ExceptionUtils.fromMessage(msg);
 		}
 
 		List<File> contributionFiles = new ArrayList<File>();
 		for(Node child = masterBuild.getFirstChild(); child != null; child = child.getNextSibling()) {
 			if(!(child instanceof Element))
 				continue;
 
 			Element elem = (Element) child;
 			if("contributions".equals(elem.getNodeName())) {
 				String attr = StringUtils.trimmedOrNull(elem.getAttribute("href"));
 				if(attr == null)
 					continue;
 				if(attr.endsWith("#/"))
 					attr = attr.substring(0, attr.length() - 2);
 				URI uri = buildModelFolderURI.resolve(URI.create(attr));
 				contributionFiles.add(new File(uri));
 			}
 
 			if("buildmaster".equals(elem.getNodeName())) {
 				buildMasterEmail = StringUtils.trimmedOrNull(elem.getAttribute("email"));
 				buildMasterName = StringUtils.trimmedOrNull(elem.getAttribute("name"));
 			}
 		}
 
 		List<String> errors = new ArrayList<String>();
 		for(File buildFile : contributionFiles) {
 			try {
 				docBld.parse(buildFile);
 			}
 			catch(Exception e) {
 				String msg;
 				if(e instanceof SAXParseException)
 					msg = format("Unable to parse file: %s: Error at line %s: %s", buildFile,
 							Integer.valueOf(((SAXParseException) e).getLineNumber()), e.getMessage());
 				else
 					msg = format("Unable to parse file: %s: %s", buildFile, e.getMessage());
 				LogUtils.error(e, msg);
 				errors.add(msg);
 			}
 			finally {
 				docBld.reset();
 			}
 		}
 
 		if(errors.size() > 0) {
 			sendEmail(null, errors);
 			throw ExceptionUtils.fromMessage("Not all contributions could be parsed");
 		}
 	}
 }
