 package org.vpac.grisu.backend.model;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.vfs.FileObject;
 import org.apache.commons.vfs.FileSystem;
 import org.apache.commons.vfs.FileSystemException;
 import org.apache.commons.vfs.FileSystemOptions;
 import org.apache.commons.vfs.impl.DefaultFileSystemManager;
 import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
 import org.apache.log4j.Logger;
 import org.hibernate.annotations.CollectionOfElements;
 import org.vpac.grisu.backend.model.job.Job;
 import org.vpac.grisu.backend.model.job.JobSubmissionManager;
 import org.vpac.grisu.backend.model.job.JobSubmitter;
import org.vpac.grisu.backend.model.job.gt5.GT5Submitter;
 import org.vpac.grisu.backend.model.job.gt4.GT4DummySubmitter;
 import org.vpac.grisu.backend.model.job.gt4.GT4Submitter;
 import org.vpac.grisu.backend.utils.CertHelpers;
 import org.vpac.grisu.backend.utils.FileSystemStructureToXMLConverter;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.NoValidCredentialException;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.control.serviceInterfaces.AbstractServiceInterface;
 import org.vpac.grisu.model.MountPoint;
 import org.vpac.grisu.model.dto.DtoActionStatus;
 import org.vpac.grisu.model.job.JobSubmissionObjectImpl;
 import org.vpac.grisu.utils.MountPointHelpers;
 import org.vpac.security.light.voms.VO;
 import org.vpac.security.light.voms.VOManagement.VOManagement;
 import org.vpac.security.light.vomsProxy.VomsException;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 /**
  * The User class holds all the relevant data a job could want to know from the
  * user it is running under. This class belongs to the three central classes of
  * grisu (the other two are {@link ServiceInterface} and {@link Job}.
  * 
  * At the moment it holds filesystem information which can be used to stage
  * files from the desktop. Also it has got information about vo memberships of
  * the user.
  * 
  * @author Markus Binsteiner
  * 
  */
 @Entity
 @Table(name = "users")
 public class User {
 
 	// to get one filesystemmanager per thread
 	private class ThreadLocalFsManager extends ThreadLocal {
 
 		private FileSystem createFileSystem(String rootUrl, ProxyCredential credToUse) {
 
 			FileSystemOptions opts = new FileSystemOptions();
 
 			if (rootUrl.startsWith("gsiftp")) {
 				GridFtpFileSystemConfigBuilder builder = GridFtpFileSystemConfigBuilder
 				.getInstance();
 				builder.setGSSCredential(opts, credToUse
 						.getGssCredential());
 				// builder.setUserDirIsRoot(opts, true);
 			}
 
 			FileObject fileRoot;
 			try {
 				fileRoot = getFsManager().resolveFile(rootUrl, opts);
 			} catch (FileSystemException e) {
 				throw new RuntimeException(e);
 			}
 
 			FileSystem fileBase = null;
 			fileBase = fileRoot.getFileSystem();
 
 			return fileBase;
 
 
 		}
 
 		public synchronized FileSystem getFileSystem(final String rootUrl,
 				String fqan) throws FileSystemException {
 
 			if (Thread.interrupted()) {
 				Thread.currentThread().interrupt();
 				remove();
 				return null;
 			}
 
 			ProxyCredential credToUse = null;
 
 			MountPoint temp = null;
 			try {
 				temp = getResponsibleMountpointForAbsoluteFile(rootUrl);
 			} catch (IllegalStateException e) {
 				myLogger.info(e);
 			}
 			if ((fqan == null) && (temp != null) && (temp.getFqan() != null)) {
 				fqan = temp.getFqan();
 			}
 			// get the right credential for this mountpoint
 			if (fqan != null) {
 
 				credToUse = getCred(fqan);
 
 			} else {
 				credToUse = getCred();
 			}
 
 			FileSystem fileBase = null;
 
 			if (temp == null) {
 				// means we have to figure out how to connect to this. I.e.
 				// which fqan to use...
 				//				throw new FileSystemException(
 				//						"Could not find mountpoint for url " + rootUrl);
 
 				// creating a filesystem...
 				myLogger.info("Creating filesystem without mountpoint...");
 				return createFileSystem(rootUrl, credToUse);
 
 			} else {
 				// great, we can re-use this filesystem
 				if (((FileSystemCache) get()).getFileSystem(temp) == null) {
 
 					fileBase = createFileSystem(temp.getRootUrl(), credToUse);
 
 					if (temp != null) {
 						((FileSystemCache) get()).addFileSystem(temp, fileBase);
 					}
 				} else {
 					fileBase = ((FileSystemCache) get()).getFileSystem(temp);
 				}
 			}
 
 			if (Thread.interrupted()) {
 				remove();
 				Thread.currentThread().interrupt();
 				return null;
 			}
 
 			return fileBase;
 
 		}
 
 		public DefaultFileSystemManager getFsManager() {
 
 			if (Thread.interrupted()) {
 				remove();
 				Thread.currentThread().interrupt();
 				return null;
 			}
 
 			return ((FileSystemCache) super.get()).getFileSystemManager();
 		}
 
 		@Override
 		public Object initialValue() {
 
 			if (Thread.interrupted()) {
 				Thread.currentThread().interrupt();
 				return null;
 			}
 			myLogger.debug("Creating new FS Manager.");
 			FileSystemCache cache = new FileSystemCache();
 			myLogger.debug("Creating fsm for thread "
 					+ Thread.currentThread().getName()
 					+ ". cachedFileSystems size: "
 					+ cache.getFileSystems().size());
 			return cache;
 
 		}
 
 		@Override
 		public void remove() {
 			myLogger.debug("Removing fsm for thread "
 					+ Thread.currentThread().getName());
 			((FileSystemCache) get()).close();
 			super.remove();
 		}
 	}
 
 
 	private static Logger myLogger = Logger.getLogger(User.class.getName());
 
 	public static User createUser(ProxyCredential cred, AbstractServiceInterface si) {
 
 		// make sure there is a valid credential
 		if ((cred == null) || !cred.isValid()) {
 			throw new NoValidCredentialException("No valid credential exists in this session");
 		}
 
 		// if ( getCredential())
 
 		User user;
 		// try to look up user in the database
 		user = si.getUserDao().findUserByDN(cred.getDn());
 
 		if (user == null) {
 			user = new User(cred);
 			si.getUserDao().saveOrUpdate(user);
 		} else {
 			user.setCred(cred);
 		}
 
 		user.setAutoMountedMountPoints(user.df_auto_mds(si.getAllSites().asArray()));
 
 		return user;
 
 
 	}
 
 	public static String get_vo_dn_path(final String dn) {
 		return dn.replace("=", "_").replace(",", "_").replace(" ", "_");
 	}
 
 	private final ThreadLocalFsManager threadLocalFsManager = new ThreadLocalFsManager();
 
 	private Long id = null;
 
 	// the (default) credential to contact gridftp file shares
 	private ProxyCredential cred = null;
 
 	// managers the virtual filesystem manager
 	private final DefaultFileSystemManager fsmanager = null;
 
 	private JobSubmissionManager manager;
 
 	private FileSystemStructureToXMLConverter fsconverter = null;
 	private final Map<String, DtoActionStatus> actionStatus = Collections
 	.synchronizedMap(new HashMap<String, DtoActionStatus>());
 
 	// the (default) credentials dn
 	private String dn = null;
 
 	// the mountpoints of a user
 	private Set<MountPoint> mountPoints = new HashSet<MountPoint>();
 
 
 	private Set<MountPoint> mountPointsAutoMounted = new HashSet<MountPoint>();
 
 	private Set<MountPoint> allMountPoints = null;
 
 	// // persistent properties
 	// // not used yet
 	// private List<FileTransfer> fileTransfers = new ArrayList<FileTransfer>();
 
 	// // not used yet
 	// private List<FileReservation> fileReservations = new
 	// ArrayList<FileReservation>();
 
 	// filesystem connections are cached so that we don't need to connect again
 	// everytime we access one
 	private final Map<MountPoint, FileSystem> cachedFilesystemConnections = new HashMap<MountPoint, FileSystem>();
 	// credentials are chache so we don't have to contact myproxy/voms anytime
 	// we want to make a transaction
 	private Map<String, ProxyCredential> cachedCredentials = new HashMap<String, ProxyCredential>();
 	// All fqans of the user
 	private Map<String, String> fqans = null;
 
 	private Map<String, String> userProperties = new HashMap<String, String>();
 
 	private Map<String, String> bookmarks = new HashMap<String, String>();
 	private Map<String, JobSubmissionObjectImpl> jobTemplates = new HashMap<String, JobSubmissionObjectImpl>();
 
 	// for hibernate
 	public User() {
 	}
 
 	/**
 	 * Constructs a user using and associates a (default) credential with it.
 	 * 
 	 * @param cred
 	 *            the credential
 	 * @throws FileSystemException
 	 *             if the users default filesystems can't be mounted
 	 */
 	private User(final ProxyCredential cred) {
 		this.dn = cred.getDn();
 		this.cred = cred;
 	}
 
 	/**
 	 * Constructs a User object not using an associated credential.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 */
 	public User(final String dn) {
 		this.dn = dn;
 	}
 
 	public void addBookmark(String alias, String url) {
 		this.bookmarks.put(alias, url);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param vo
 	 */
 	public void addFqan(final String fqan, final String vo) {
 		fqans.put(fqan, vo);
 	}
 
 	/**
 	 * Resolves the provided filename into a FileObject. If the filename starts
 	 * with "/" a file on one of the "mounted" filesystems is looked up. Else it
 	 * has to start with the name of a (supported) protocol (like: gsiftp:///).
 	 * 
 	 * @param file
 	 *            the filename
 	 * @return the FileObject
 	 * @throws RemoteFileSystemException
 	 *             if there was an error accessing the file
 	 * @throws VomsException
 	 *             if the (possible) required voms credential could not be
 	 *             created
 	 */
 	public FileObject aquireFile(final String file)
 	throws RemoteFileSystemException {
 		return aquireFile(file, null);
 	}
 
 	/**
 	 * Resolves the provided filename into a FileObject. If the filename starts
 	 * with "/" a file on one of the "mounted" filesystems is looked up. Else it
 	 * has to start with the name of a (supported) protocol (like: gsiftp:///).
 	 * 
 	 * @param file
 	 *            the filename
 	 * @param cred
 	 *            the credential to access the filesystem on which the file
 	 *            resides
 	 * @return the FileObject
 	 * @throws RemoteFileSystemException
 	 *             if there is a problem resolving the file
 	 * @throws VomsException
 	 *             if the (possible) required voms credential could not be
 	 *             created
 	 */
 	public FileObject aquireFile(final String file, final String fqan)
 	throws RemoteFileSystemException {
 
 		String file_to_aquire = null;
 
 		if (Thread.interrupted()) {
 			Thread.currentThread().interrupt();
 			throw new RemoteFileSystemException("Accessing file interrupted.");
 		}
 
 		if (file.startsWith("tmp:") || file.startsWith("ram:")) {
 			try {
 				return threadLocalFsManager.getFsManager().resolveFile(file);
 			} catch (FileSystemException e) {
 				throw new RemoteFileSystemException(
 						"Could not access file on local temp filesystem: "
 						+ e.getLocalizedMessage());
 			}
 		} else if (file.startsWith("/")) {
 			// means file on "mounted" filesystem
 
 			MountPoint mp = getResponsibleMountpointForUserSpaceFile(file);
 
 			if (mp == null) {
 				throw new RemoteFileSystemException(
 						"File path is not on any of the mountpoints for file: "
 						+ file);
 			}
 
 			file_to_aquire = mp.replaceMountpointWithAbsoluteUrl(file);
 
 			if (file_to_aquire == null) {
 				throw new RemoteFileSystemException(
 						"File path is not on any of the mountpoints for file: "
 						+ file);
 			}
 		} else {
 			// means absolute url
 			file_to_aquire = file;
 		}
 
 		FileObject fileObject = null;
 		try {
 			FileSystem root = null;
 
 			// root = this.createFilesystem(mp.getRootUrl(), mp.getFqan());
 			root = threadLocalFsManager.getFileSystem(file, fqan);
 
 			String fileUri = root.getRootName().getURI();
 
 			try {
 				URI uri = new URI(file_to_aquire);
 				file_to_aquire = uri.toString();
 			} catch (URISyntaxException e) {
 				e.printStackTrace();
 				throw new RemoteFileSystemException(
 						"Could not get uri for file " + file_to_aquire);
 			}
 
 			String tempUriString = file_to_aquire.replace(":2811", "")
 			.substring(fileUri.length());
 			fileObject = root.resolveFile(tempUriString);
 			// fileObject = root.resolveFile(file_to_aquire);
 
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Could not access file: "
 					+ file + ": " + e.getMessage());
 		}
 
 		return fileObject;
 
 	}
 
 	public void cleanCache() {
 		// TODO disconnect filesystems somehow?
 		// cachedFilesystemConnections = new HashMap<MountPoint, FileSystem>();
 		// // does this affect existing filesystem connection
 		// for ( ProxyCredential proxy : cachedCredentials.values() ) {
 		// proxy.destroy();
 		// }
 		cachedCredentials = new HashMap<String, ProxyCredential>();
 	}
 
 	public void closeFileSystems() {
 		threadLocalFsManager.remove();
 	}
 
 	/**
 	 * Calculates all mountpoints that are automatically mounted using mds. At
 	 * the moment, the port part of the gridftp url share is ignored. Maybe I'll
 	 * change that later.
 	 * 
 	 * @param sites
 	 *            the sites that should be used
 	 * @return all MountPoints
 	 */
 	protected Set<MountPoint> df_auto_mds(final String[] sites) {
 
 		Set<MountPoint> mps = new TreeSet<MountPoint>();
 
 		// for ( String site : sites ) {
 
 		for (String fqan : getFqans().keySet()) {
 			Date start = new Date();
 			Map<String, String[]> mpUrl = AbstractServiceInterface.informationManager.getDataLocationsForVO(fqan);
 			Date end = new Date();
 			myLogger.debug("Querying for data locations for all sites and+ "
 					+ fqan + " took: " + (end.getTime() - start.getTime())
 					+ " ms.");
 			for (String server : mpUrl.keySet()) {
 				for (String path : mpUrl.get(server)) {
 
 					String url = null;
 					if (path.contains("${GLOBUS_USER_HOME}")) {
 
 						try {
 							url = getFileSystemHomeDirectory(
 									server.replace(":2811", ""), fqan);
 						} catch (FileSystemException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 					}else  if  ( path.contains("${GLOBUS_SCRATCH_DIR") ) {
 						try {
 							url = getFileSystemHomeDirectory(
 									server.replace(":2811", ""), fqan)+"/.globus/scratch";
 						} catch (FileSystemException e) {
 							e.printStackTrace();
 						}
 					} else {
 
 						url = server.replace(":2811", "") + path + "/"
 						+ User.get_vo_dn_path(getCred().getDn());
 
 					}
 
 					if (StringUtils.isBlank(url)) {
 						continue;
 					}
 
 					MountPoint mp = new MountPoint(getDn(), fqan,
 							url, MountPointHelpers.calculateMountPointName(server, fqan),
 							AbstractServiceInterface.informationManager.getSiteForHostOrUrl(url), true);
 					// + "." + fqan + "." + path);
 					// + "." + fqan);
 					mps.add(mp);
 				}
 			}
 		}
 
 		// }
 
 		return mps;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(final Object other) {
 		if (this == other) {
 			return true;
 		}
 		if (!(other instanceof User)) {
 			return false;
 		}
 
 		final User user = (User) other;
 
 		if (!dn.equals(user.dn)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Transient
 	public Map<String, DtoActionStatus> getActionStatuses() {
 		return actionStatus;
 	}
 
 
 	// private List<FileReservation> getFileReservations() {
 	// return fileReservations;
 	// }
 	//
 	// private void setFileReservations(List<FileReservation> fileReservations)
 	// {
 	// this.fileReservations = fileReservations;
 	// }
 	//
 	// private List<FileTransfer> getFileTransfers() {
 	// return fileTransfers;
 	// }
 	//
 	// private void setFileTransfers(List<FileTransfer> fileTransfers) {
 	// this.fileTransfers = fileTransfers;
 	// }
 
 	/**
 	 * Returns all mountpoints (including automounted ones for this session.
 	 * 
 	 * @return all mountpoints for this session
 	 */
 	@Transient
 	public Set<MountPoint> getAllMountPoints() {
 		if (allMountPoints == null) {
 
 			throw new IllegalStateException("Mountpoints not set yet.");
 			//			allMountPoints = new TreeSet<MountPoint>();
 			//			// first the automounted ones because the manually ones are more
 			//			// important
 			//			allMountPoints.addAll(mountPointsAutoMounted);
 			//			allMountPoints.addAll(getMountPoints());
 		}
 		return allMountPoints;
 	}
 
 
 	/**
 	 * Gets a map of this users bookmarks.
 	 * 
 	 * @return the users' properties
 	 */
 	@CollectionOfElements(fetch = FetchType.EAGER)
 	public Map<String, String> getBookmarks() {
 		return bookmarks;
 	}
 
 	/**
 	 * Returns the default credential of the user (if any).
 	 * 
 	 * @return the default credential or null if there is none
 	 */
 	@Transient
 	public ProxyCredential getCred() {
 		return cred;
 	}
 
 	@Transient
 	public ProxyCredential getCred(String fqan) {
 
 		ProxyCredential credToUse = cachedCredentials.get(fqan);
 
 		if ((credToUse == null) || !credToUse.isValid()) {
 
 			// put a new credential in the cache
 			VO vo = VOManagement.getVO(getFqans().get(fqan));
 			credToUse = CertHelpers.getVOProxyCredential(vo, fqan, getCred());
 			cachedCredentials.put(fqan, credToUse);
 		}
 
 		return credToUse;
 	}
 
 	/**
 	 * Returns the users dn.
 	 * 
 	 * @return the dn
 	 */
 	@Column(nullable = false)
 	public String getDn() {
 		return dn;
 	}
 
 	public String getFileSystemHomeDirectory(String filesystemRoot, String fqan)
 	throws FileSystemException {
 
 		// FileSystem fileSystem = createFilesystem(filesystemRoot, fqan);
 		FileSystem fileSystem = threadLocalFsManager.getFileSystem(
 				filesystemRoot, fqan);
 		myLogger.debug("Connected to file system.");
 
 		myLogger.debug("Using home directory: "
 				+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 				.substring(1));
 
 		String home = (String) fileSystem.getAttribute("HOME_DIRECTORY");
 		String uri = fileSystem.getRoot().getName().getRootURI()
 		+ home.substring(1);
 
 		return uri;
 	}
 
 	/**
 	 * Getter for the users' fqans.
 	 * 
 	 * @return all fqans as map with the fqan as key and the vo as value
 	 */
 	@Transient
 	public Map<String, String> getFqans() {
 		if ( fqans == null ) {
 
 			myLogger.debug("Checking credential");
 			if (cred.isValid()) {
 				fqans = VOManagement.getAllFqans(cred.getGssCredential());
 			}
 
 		}
 		return fqans;
 	}
 
 
 	@Transient
 	public FileSystemStructureToXMLConverter getFsConverter() {
 		if (fsconverter == null) {
 			fsconverter = new FileSystemStructureToXMLConverter(this);
 		}
 		return fsconverter;
 	}
 
 	@Id
 	@GeneratedValue
 	private Long getId() {
 		return id;
 	}
 
 	@CollectionOfElements
 	public Map<String, JobSubmissionObjectImpl> getJobTemplates() {
 		return jobTemplates;
 	}
 
 	// /**
 	// * Used internally to mount filesystems.
 	// *
 	// * @return the filesystem manager of the user
 	// * @throws FileSystemException
 	// * if something goes wrong
 	// */
 	// @Transient
 	// private DefaultFileSystemManager getFsManager() throws
 	// FileSystemException {
 	// // if (fsmanager == null) {
 	// //
 	// // fsmanager = VFSUtil.createNewFsManager(false, false, true, true,
 	// // true, true, true, null);
 	// //
 	// // }
 	// // return fsmanager;
 	// // System.out.println("Creating new FS Manager.");
 	// // return VFSUtil.createNewFsManager(false, false, true, true, true,
 	// // true, true, null);
 	// return threadLocalFsManager.getFsManager();
 	// }
 
 	// for hibernate
 	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	@JoinTable
 	private Set<MountPoint> getMountPoints() {
 		return mountPoints;
 	}
 
 	/**
 	 * Checks whether the filesystem of any of the users' mountpoints contains
 	 * the specified file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the mountpoint of null if no filesystem contains this file
 	 */
 	public MountPoint getResponsibleMountpointForAbsoluteFile(final String file) {
 
 		String new_file = null;
 		myLogger.debug("Finding mountpoint for file: " + file);
 
 		for (MountPoint mountpoint : getAllMountPoints()) {
 			if (mountpoint.isResponsibleForAbsoluteFile(file)) {
 				return mountpoint;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether any of the users' mountpoints contain the specified file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the mountpoint or null if the file is not on any of the
 	 *         mountpoints
 	 */
 	public MountPoint getResponsibleMountpointForUserSpaceFile(final String file) {
 
 		for (MountPoint mountpoint : getAllMountPoints()) {
 			if (mountpoint.isResponsibleForUserSpaceFile(file)) {
 				return mountpoint;
 			}
 		}
 		return null;
 	}
 
 	@Transient
 	public JobSubmissionManager getSubmissionManager() {
 		if (manager == null) {
 			Map<String, JobSubmitter> submitters = new HashMap<String, JobSubmitter>();
 			submitters.put("GT4", new GT4Submitter());
                        submitters.put("GT5", new GT5Submitter());
 			submitters.put("GT4Dummy", new GT4DummySubmitter());
 			manager = new JobSubmissionManager(AbstractServiceInterface.informationManager,
 					submitters);
 		}
 		return manager;
 	}
 
 	/**
 	 * Gets a map of this users properties. These properties can be used to
 	 * store anything you can think of. Usful for history and such.
 	 * 
 	 * @return the users' properties
 	 */
 	@CollectionOfElements(fetch = FetchType.EAGER)
 	public Map<String, String> getUserProperties() {
 		return userProperties;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		return 29 * dn.hashCode();
 	}
 
 	/**
 	 * Mounts a filesystem using the default credential of a user.
 	 * 
 	 * @param root
 	 *            the filesystem to mount (something like:
 	 *            gsiftp://ngdata.vapc.org/home/san04/markus)
 	 * @param name
 	 *            the name of the mountpoint (something like: /remote or
 	 *            /remote.vpac)
 	 * @throws FileSystemException
 	 *             if the filesystem could not be mounted
 	 * @throws RemoteFileSystemException
 	 * @throws VomsException
 	 */
 	public MountPoint mountFileSystem(final String root, final String name,
 			final boolean useHomeDirectory, String site)
 	throws RemoteFileSystemException {
 
 		return mountFileSystem(root, name, getCred(), useHomeDirectory, site);
 	}
 
 	/**
 	 * Adds a filesystem to the mountpoints of this user. The mountpoint is just
 	 * an alias to make the urls shorter and easier to read/remember. You only
 	 * need to mount a filesystem once. After you persisted the user (with
 	 * hibernate) the alias and rootUrl of the filesystem are persisted as well.
 	 * 
 	 * A mountpoint always has to be in the root directory (for example: /local
 	 * or /remote -- never /remote/ng2.vpac.org )
 	 * 
 	 * @param uri
 	 *            the filesystem to mount (something like:
 	 *            gsiftp://ngdata.vapc.org/home/san04/markus)
 	 * @param mountPointName
 	 *            the name of the mountpoint (something like: /remote or
 	 *            /remote.vpac)
 	 * @param cred
 	 *            the credential that is used to contact the filesystem (can be
 	 *            null for local filesystems)
 	 * @return the root FileObject of the newly mounted FileSystem
 	 * @throws VomsException
 	 * @throws FileSystemException
 	 *             if the filesystem could not be mounted
 	 */
 	public MountPoint mountFileSystem(String uri, final String mountPointName,
 			final ProxyCredential cred, final boolean useHomeDirectory,
 			final String site) throws RemoteFileSystemException {
 
 		// if (!mountPointName.startsWith("/")) {
 		// mountPointName = "/" + mountPointName;
 		// }
 
 		myLogger.debug("Checking mountpoints for duplicates.");
 		for (MountPoint mp : getAllMountPoints()) {
 			if (mountPointName.equals(mp.getAlias())) {
 				throw new RemoteFileSystemException(
 						"There is already a filesystem mounted on:"
 						+ mountPointName);
 			}
 		}
 
 		MountPoint new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 				mountPointName, site);
 		try {
 			// FileSystem fileSystem = createFilesystem(new_mp.getRootUrl(),
 			// new_mp.getFqan());
 
 			FileSystem fileSystem = threadLocalFsManager.getFileSystem(new_mp
 					.getRootUrl(), new_mp.getFqan());
 			myLogger.debug("Connected to file system.");
 			if (useHomeDirectory) {
 				myLogger.debug("Using home directory: "
 						+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 						.substring(1));
 				uri = fileSystem.getRoot().getName().getRootURI()
 				+ ((String) fileSystem.getAttribute("HOME_DIRECTORY"))
 				.substring(1);
 				// if vo user, use $VOHOME/<DN> as homedirectory
 				if (cred.getFqan() != null) {
 					uri = uri + File.separator + get_vo_dn_path(cred.getDn());
 					fileSystem.resolveFile(
 							((String) fileSystem.getAttribute("HOME_DIRECTORY")
 									+ File.separator + cred.getDn().replace(
 											"=", "_").replace(",", "_").replace(" ",
 											"_"))).createFolder();
 				}
 				new_mp = new MountPoint(cred.getDn(), cred.getFqan(), uri,
 						mountPointName, site);
 			}
 
 			if (!mountPoints.contains(new_mp)) {
 				allMountPoints = null;
 				mountPoints.add(new_mp);
 			}
 			return new_mp;
 		} catch (FileSystemException e) {
 			throw new RemoteFileSystemException("Error while trying to mount: "
 					+ mountPointName);
 		}
 
 	}
 
 	public MountPoint mountFileSystem(final String root, final String name,
 			final String fqan, final boolean useHomeDirectory, final String site)
 	throws RemoteFileSystemException {
 
 		if ((fqan == null) || Constants.NON_VO_FQAN.equals(fqan)) {
 			return mountFileSystem(root, name, useHomeDirectory, site);
 		} else {
 
 			Map<String, String> temp = getFqans();
 			VO vo = VOManagement.getVO(temp.get(fqan));
 
 			ProxyCredential vomsProxyCred = CertHelpers.getVOProxyCredential(
 					vo, fqan, getCred());
 
 			return mountFileSystem(root, name, vomsProxyCred, useHomeDirectory,
 					site);
 		}
 	}
 
 	public void removeBookmark(String alias) {
 		this.bookmarks.remove(alias);
 	}
 
 	/**
 	 * Not used yet.
 	 * 
 	 * @param vo
 	 */
 	public void removeFqan(final String fqan) {
 		fqans.remove(fqan);
 	}
 
 	public void removeProperty(final String key) {
 		userProperties.remove(key);
 	}
 
 	public void resetMountPoints() {
 		allMountPoints = null;
 	}
 
 	/**
 	 * Translates an "user-space" file url into an absolute file url.
 	 * 
 	 * @param file
 	 *            an "user-space" file url (/ngdata.vpac.org/test.txt)
 	 * @return the absolute file url
 	 *         (gsiftp://ngdata.vpac.org/home/san04/markus/test.txt) or null if
 	 *         the file is not within the user's filespace
 	 */
 	public String returnAbsoluteUrl(final String file) {
 		MountPoint mp = getResponsibleMountpointForUserSpaceFile(file);
 		if (mp == null) {
 			return null;
 		} else if (file.startsWith("gsiftp:")) {
 			return file;
 		} else {
 			return mp.replaceMountpointWithAbsoluteUrl(file);
 		}
 	}
 
 	/**
 	 * Translates an absolute file url into an "user-space" one.
 	 * 
 	 * @param file
 	 *            an absolute file url
 	 *            (gsiftp://ngdata.vpac.org/home/san04/markus/test.txt)
 	 * @return the "user-space" file url (/ngdata.vpac.org/test.txt)
 	 */
 	public String returnUserSpaceUrl(final String file) {
 		MountPoint mp = getResponsibleMountpointForAbsoluteFile(file);
 		return mp.replaceAbsoluteRootUrlWithMountPoint(file);
 	}
 
 	/**
 	 * Set's additional mountpoints that the user did not explicitly mount
 	 * manually.
 	 * 
 	 * @param amps
 	 *            the mountpoints to add (for this session)
 	 */
 	public void setAutoMountedMountPoints(final Set<MountPoint> amps) {
 		//		allMountPoints = null;
 		this.mountPointsAutoMounted = amps;
 
 		allMountPoints = new TreeSet<MountPoint>();
 		// first the automounted ones because the manually ones are more
 		// important
 		allMountPoints.addAll(mountPointsAutoMounted);
 		allMountPoints.addAll(getMountPoints());
 
 	}
 
 	// public void addProperty(String key, String value) {
 	// List<String> list = userProperties.get(key);
 	// if ( list == null ) {
 	// list = new LinkedList<String>();
 	// }
 	// list.add(value);
 	// }
 
 	private void setBookmarks(final Map<String, String> bm) {
 		this.bookmarks = bm;
 	}
 
 	/**
 	 * Sets the default credential for the user. The default credential is used
 	 * mostly as convenience.
 	 * 
 	 * @param cred
 	 *            the credential to use as default
 	 */
 	public void setCred(final ProxyCredential cred) {
 
 		this.cred = cred;
 	}
 
 	/**
 	 * For hibernate.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 */
 	private void setDn(final String dn) {
 		this.dn = dn;
 	}
 
 	/**
 	 * Setter for the users' fqans.
 	 * 
 	 * @param fqans
 	 *            all fqans as map with the fqan as key and the vo as value
 	 */
 	private void setFqans(final Map<String, String> fqans) {
 		this.fqans = fqans;
 	}
 
 	private void setId(final Long id) {
 		this.id = id;
 	}
 
 	public void setJobTemplates(
 			final Map<String, JobSubmissionObjectImpl> jobTemplates) {
 		this.jobTemplates = jobTemplates;
 	}
 
 	// for hibernate
 	private void setMountPoints(final Set<MountPoint> mountPoints) {
 		this.mountPoints = mountPoints;
 	}
 
 	private void setUserProperties(final Map<String, String> userProperties) {
 		this.userProperties = userProperties;
 	}
 
 	/**
 	 * Unmounts a filesystem.
 	 * 
 	 * @param mountPointName
 	 *            the name of the mountpoint (/local or /remote or something)
 	 * @throws FileSystemException
 	 *             if the filesystem could not be unmounted or something else
 	 *             went wrong
 	 */
 	public void unmountFileSystem(final String mountPointName) {
 
 		for (MountPoint mp : mountPoints) {
 			if (mp.getAlias().equals(mountPointName)) {
 				mountPoints.remove(mp);
 				allMountPoints = null;
 				return;
 			}
 		}
 	}
 
 }
