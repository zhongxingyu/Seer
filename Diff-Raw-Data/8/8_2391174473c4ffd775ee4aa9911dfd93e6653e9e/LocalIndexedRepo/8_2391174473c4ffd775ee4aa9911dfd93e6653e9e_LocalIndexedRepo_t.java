 package aQute.bnd.deployer.repository;
 
 import java.io.*;
 import java.net.*;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.util.*;
 
 import org.osgi.service.coordinator.*;
 import org.osgi.service.log.*;
 
 import aQute.bnd.osgi.*;
 import aQute.bnd.deployer.repository.api.*;
 import aQute.bnd.filerepo.FileRepo;
 import aQute.bnd.service.*;
 import aQute.bnd.version.*;
 import aQute.lib.io.*;
 
 public class LocalIndexedRepo extends FixedIndexedRepo implements Refreshable, Participant {
 
 	private static final String	CACHE_PATH	= ".cache";
 	public static final String			PROP_LOCAL_DIR			= "local";
 	public static final String			PROP_READONLY			= "readonly";
 	public static final String			PROP_PRETTY				= "pretty";
 	public static final String			PROP_OVERWRITE			= "overwrite";
 
 	private static final VersionRange	RANGE_ANY				= new VersionRange(Version.LOWEST.toString());
 
 	private FileRepo					storageRepo;
 	private boolean						readOnly;
 	private boolean						pretty					= false;
 	private boolean						overwrite				= true;
 	private File						storageDir;
 
 	// @GuardedBy("newFilesInCoordination")
 	private final List<URI>	newFilesInCoordination	= new LinkedList<URI>();
 
 	@Override
 	public synchronized void setProperties(Map<String,String> map) {
 		super.setProperties(map);
 
 		// Load essential properties
 		String localDirPath = map.get(PROP_LOCAL_DIR);
 		if (localDirPath == null)
 			throw new IllegalArgumentException(String.format("Attribute '%s' must be set on %s plugin.", PROP_LOCAL_DIR, getClass().getName()));
 		
 		storageDir = new File(localDirPath);
 		if (storageDir.exists() && !storageDir.isDirectory())
 			throw new IllegalArgumentException(String.format("Local path '%s' exists and is not a directory.", localDirPath));
 		
 		readOnly = Boolean.parseBoolean(map.get(PROP_READONLY));
 		pretty = Boolean.parseBoolean(map.get(PROP_PRETTY));
 		overwrite = map.get(PROP_OVERWRITE) == null ? true : Boolean.parseBoolean(map.get(PROP_OVERWRITE));
 
 		// Configure the storage repository
 		storageRepo = new FileRepo(storageDir);
 
 		// Set the local index and cache directory locations
 		cacheDir = new File(storageDir, CACHE_PATH);
 		if (cacheDir.exists() && !cacheDir.isDirectory())
 			throw new IllegalArgumentException(String.format("Cannot create repository cache: '%s' already exists but is not directory.", cacheDir.getAbsolutePath()));
 	}
 
 	@Override
 	protected synchronized List<URI> loadIndexes() throws Exception {
 		Collection<URI> remotes = super.loadIndexes();
 		List<URI> indexes = new ArrayList<URI>(remotes.size() + generatingProviders.size());
 
 		for (IRepositoryContentProvider contentProvider : generatingProviders) {
 			File indexFile = getIndexFile(contentProvider);
 			try {
 				if (indexFile.exists()) {
 					indexes.add(indexFile.toURI());
 				} else {
 					if (contentProvider.supportsGeneration()) {
 						generateIndex(indexFile, contentProvider);
 						indexes.add(indexFile.toURI());
 					}
 				}
 			}
 			catch (Exception e) {
 				logService.log(LogService.LOG_ERROR, String.format(
 						"Unable to load/generate index file '%s' for repository type %s", indexFile,
 						contentProvider.getName()), e);
 			}
 		}
 
 		indexes.addAll(remotes);
 		return indexes;
 	}
 
 	private File getIndexFile(IRepositoryContentProvider contentProvider) {
 		String indexFileName = contentProvider.getDefaultIndexName(pretty);
 		File indexFile = new File(storageDir, indexFileName);
 		return indexFile;
 	}
 
 	private synchronized void regenerateAllIndexes() {
 		for (IRepositoryContentProvider provider : generatingProviders) {
 			if (!provider.supportsGeneration()) {
 				logService.log(LogService.LOG_WARNING,
 						String.format("Repository type '%s' does not support index generation.", provider.getName()));
 				continue;
 			}
 			File indexFile = getIndexFile(provider);
 			try {
 				generateIndex(indexFile, provider);
 			}
 			catch (Exception e) {
 				logService.log(LogService.LOG_ERROR, String.format(
 						"Unable to regenerate index file '%s' for repository type %s", indexFile, provider.getName()),
 						e);
 			}
 		}
 	}
 
 	private synchronized void generateIndex(File indexFile, IRepositoryContentProvider provider) throws Exception {
 		if (indexFile.exists() && !indexFile.isFile())
 			throw new IllegalArgumentException(String.format(
 					"Cannot create file: '%s' already exists but is not a plain file.", indexFile.getAbsoluteFile()));
 
 		Set<File> allFiles = new HashSet<File>();
 		gatherFiles(allFiles);
 
 		FileOutputStream out = null;
 		try {
 			storageDir.mkdirs();
 			out = new FileOutputStream(indexFile);
 
 			URI rootUri = storageDir.getCanonicalFile().toURI();
 			provider.generateIndex(allFiles, out, this.getName(), rootUri, pretty, registry, logService);
 		}
 		finally {
 			IO.close(out);
 		}
 	}
 
 	private void gatherFiles(Set<File> allFiles) throws Exception {
 		List<String> bsns = storageRepo.list(null);
 		if (bsns != null)
 			for (String bsn : bsns) {
 				File[] files = storageRepo.get(bsn, RANGE_ANY);
 				if (files != null)
 					for (File file : files) {
 						allFiles.add(file.getCanonicalFile());
 					}
 			}
 	}
 
 	@Override
 	public boolean canWrite() {
 		return !readOnly;
 	}
 
 	private synchronized void finishPut() throws Exception {
 		reset();
 		regenerateAllIndexes();
 
 		List<URI> clone = new ArrayList<URI>(newFilesInCoordination);
 		synchronized (newFilesInCoordination) {
 			newFilesInCoordination.clear();
 		}
 		for (URI entry : clone) {
 			Jar jar = null;
 			try {
 				File file = new File(entry);
 				jar = new Jar(file);
 				fireBundleAdded(jar, file);
 			} finally {
 				if (jar != null) {
 					jar.close();
 				}
 			}
 		}
 	}
 
 	public synchronized void ended(Coordination coordination) throws Exception {
 		finishPut();
 	}
 
 	public void failed(Coordination coordination) throws Exception {
 		ArrayList<URI> clone;
 		synchronized (newFilesInCoordination) {
 			clone = new ArrayList<URI>(newFilesInCoordination);
 			newFilesInCoordination.clear();
 		}
 		for (URI entry : clone) {
 			try {
 				new File(entry).delete();
 			}
 			catch (Exception e) {
 				reporter.warning("Failed to remove repository entry %s on coordination rollback: %s", entry, e);
 			}
 		}
 	}
 
 	protected PutResult putArtifact(File tmpFile, PutOptions options) throws Exception {
 		assert (tmpFile != null);
 		assert (options != null);
 
 		init();
 
 		Jar jar = null;
 		try {
 			jar= new Jar(tmpFile);
 
 			String bsn = jar.getBsn();
 			if (bsn == null)
 				throw new IllegalArgumentException("Jar does not have a Bundle-SymbolicName manifest header");
 
 			File dir = new File(storageDir, bsn);
 			if (dir.exists() && !dir.isDirectory())
 				throw new IllegalArgumentException("Path already exists but is not a directory: "
 						+ dir.getAbsolutePath());
 			dir.mkdirs();
 
 			Version version = Version.parseVersion(jar.getVersion());
 			String fName = bsn + "-" + version.getWithoutQualifier() + ".jar";
 			File file = new File(dir, fName);
 
 			PutResult result = new PutResult();
 
 			// check overwrite policy
 			if (!overwrite && file.exists())
 				return result;
 
 			if (file.exists()) {
 				IO.delete(file);
 			}
 			IO.rename(tmpFile, file);
 			result.artifact = file.toURI();
 
 			synchronized (newFilesInCoordination) {
 				newFilesInCoordination.add(result.artifact);
 			}
 
 			Coordinator coordinator = (registry != null) ? registry.getPlugin(Coordinator.class) : null;
 			if (!(coordinator != null && coordinator.addParticipant(this))) {
 				finishPut();
 			}
 			return result;
 		}
 		finally {
 			if (jar != null) {
 				jar.close();
 			}
 		}
 	}
 
 
 	/* NOTE: this is a straight copy of FileRepo.put */
 	@Override
	public synchronized PutResult put(InputStream stream, PutOptions options) throws Exception {
 		/* both parameters are required */
 		if ((stream == null) || (options == null)) {
 			throw new IllegalArgumentException("No stream and/or options specified");
 		}
 
 		/* determine if the put is allowed */
 		if (readOnly) {
 			throw new IOException("Repository is read-only");
 		}
 
 		/* the root directory of the repository has to be a directory */
 		if (!storageDir.isDirectory()) {
 			throw new IOException("Repository directory " + storageDir + " is not a directory");
 		}
 
 		/* determine if the artifact needs to be verified */
 		boolean verifyFetch = (options.digest != null);
 		boolean verifyPut = !options.allowArtifactChange;
 
 		/* determine which digests are needed */
 		boolean needFetchDigest = verifyFetch || verifyPut;
 		boolean needPutDigest = verifyPut || options.generateDigest;
 
 		/*
 		 * setup a new stream that encapsulates the stream and calculates (when
 		 * needed) the digest
 		 */
 		DigestInputStream dis = new DigestInputStream(stream, MessageDigest.getInstance("SHA-1"));
 		dis.on(needFetchDigest);
 
 		File tmpFile = null;
 		try {
 			/*
 			 * copy the artifact from the (new/digest) stream into a temporary
 			 * file in the root directory of the repository
 			 */
 			tmpFile = IO.createTempFile(storageDir, "put", ".bnd");
 			IO.copy(dis, tmpFile);
 
 			/* get the digest if available */
 			byte[] disDigest = needFetchDigest ? dis.getMessageDigest().digest() : null;
 
 			/* verify the digest when requested */
 			if (verifyFetch && !MessageDigest.isEqual(options.digest, disDigest)) {
 				throw new IOException("Retrieved artifact digest doesn't match specified digest");
 			}
 
 			/* put the artifact into the repository (from the temporary file) */
 			PutResult r = putArtifact(tmpFile, options);
 
 			/* calculate the digest when requested */
 			if (needPutDigest && (r.artifact != null)) {
 				MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
 				IO.copy(new File(r.artifact), sha1);
 				r.digest = sha1.digest();
 			}
 
 			/* verify the artifact when requested */
 			if (verifyPut && (r.digest != null) && !MessageDigest.isEqual(disDigest, r.digest)) {
 				File f = new File(r.artifact);
 				if (f.exists()) {
 					IO.delete(f);
 				}
 				throw new IOException("Stored artifact digest doesn't match specified digest");
 			}
 
 			return r;
 		}
 		finally {
 			if (tmpFile != null && tmpFile.exists()) {
 				IO.delete(tmpFile);
 			}
 		}
 	}
 
 	public boolean refresh() {
 		reset();
 		return true;
 	}
 
	public synchronized File getRoot() {
 		return storageDir;
 	}
 
 	protected void fireBundleAdded(Jar jar, File file) {
 		if (registry == null)
 			return;
 		List<RepositoryListenerPlugin> listeners = registry.getPlugins(RepositoryListenerPlugin.class);
 		for (RepositoryListenerPlugin listener : listeners) {
 			try {
 				listener.bundleAdded(this, jar, file);
 			}
 			catch (Exception e) {
 				if (reporter != null)
 					reporter.warning("Repository listener threw an unexpected exception: %s", e);
 			}
 		}
 	}
 
 	@Override
	public synchronized String getLocation() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(storageDir.getAbsolutePath());
 
 		String otherPaths = super.getLocation();
 		if (otherPaths != null && otherPaths.length() > 0)
 			builder.append(", ").append(otherPaths);
 
 		return builder.toString();
 	}
 
 }
