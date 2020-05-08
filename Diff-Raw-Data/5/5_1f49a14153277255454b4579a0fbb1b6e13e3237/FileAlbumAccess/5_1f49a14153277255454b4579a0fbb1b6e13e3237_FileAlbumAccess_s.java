 package ch.bergturbenthal.image.server;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.PreferenceChangeEvent;
 import java.util.prefs.PreferenceChangeListener;
 import java.util.prefs.Preferences;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.jmdns.JmmDNS;
 import javax.jmdns.NetworkTopologyEvent;
 import javax.jmdns.NetworkTopologyListener;
 import javax.jmdns.ServiceEvent;
 import javax.jmdns.ServiceInfo;
 import javax.jmdns.ServiceListener;
 
 import lombok.Cleanup;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.lib.Repository;
 import org.joda.time.Duration;
 import org.joda.time.format.PeriodFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus.Series;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import ch.bergturbenthal.image.data.model.AlbumEntry;
 import ch.bergturbenthal.image.data.model.AlbumList;
 import ch.bergturbenthal.image.data.model.PingResponse;
 import ch.bergturbenthal.image.data.model.state.ProgressType;
 import ch.bergturbenthal.image.server.model.ArchiveData;
 import ch.bergturbenthal.image.server.state.ProgressHandler;
 import ch.bergturbenthal.image.server.state.StateManager;
 import ch.bergturbenthal.image.server.util.RepositoryService;
 import ch.bergturbenthal.image.server.watcher.FileNotification;
 import ch.bergturbenthal.image.server.watcher.FileWatcher;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.Metadata;
 
 public class FileAlbumAccess implements AlbumAccess, FileConfiguration, ArchiveConfiguration, FileNotification {
   private static final String SERVICE_TYPE = "_images._tcp.local.";
   private static final String INSTANCE_NAME_PREFERENCE = "instanceName";
   private static final String ALBUM_PATH_PREFERENCE = "album_path";
   private static final String IMPORT_BASE_PATH_REFERENCE = "import_base_path";
   private static final String META_CACHE = "cache";
   private static final String META_REPOSITORY = ".meta";
 
   private ArchiveData archiveData;
   private File baseDir;
   private final AtomicReference<Collection<URI>> peerServers = new AtomicReference<Collection<URI>>(Collections.<URI> emptyList());
   @Autowired
   private ScheduledExecutorService executorService;
   @Autowired
   private RepositoryService repositoryService;
 
   private File importBaseDir;
   private final String instanceId = UUID.randomUUID().toString();
   private final AtomicLong lastLoadedDate = new AtomicLong(0);
  private Map<String, Album> loadedAlbums = Collections.emptyMap();
 
   private final Logger logger = LoggerFactory.getLogger(FileAlbumAccess.class);
   private static final ObjectMapper mapper = new ObjectMapper();
   private Git metaGit;
   private Preferences preferences = null;
   private String instanceName;
   private JmmDNS jmmDNS;
 
   private final RestTemplate restTemplate = new RestTemplate();;
 
   private final ConcurrentMap<String, Object> createAlbumLocks = new ConcurrentHashMap<String, Object>();
 
   @Autowired
   private StateManager stateManager;
   private FileWatcher fileWatcher = null;
 
   private final Semaphore updateAlbumListSemaphore = new Semaphore(1);
 
   @Override
   public Collection<String> clientsPerAlbum(final String albumId) {
     final String albumName = Util.decodeStringOfUrl(albumId);
     final HashSet<String> ret = new HashSet<String>();
     for (final Entry<String, Collection<String>> albumEntry : archiveData.getAlbumPerStorage().entrySet()) {
       if (albumEntry.getValue().contains(albumName))
         ret.add(albumEntry.getKey());
     }
     return ret;
   }
 
   @Override
   public synchronized String createAlbum(final String[] pathNames) {
     final Map<String, Album> albums = loadAlbums(true);
     final File basePath = getBasePath();
     File newAlbumPath = basePath;
     for (final String pathComp : pathNames) {
       newAlbumPath = new File(newAlbumPath, pathComp);
     }
     if (!newAlbumPath.getAbsolutePath().startsWith(basePath.getAbsolutePath())) {
       throw new RuntimeException("Cannot create Album " + pathNames);
     }
     if (newAlbumPath.exists()) {
       for (final Entry<String, Album> albumEntry : albums.entrySet()) {
         if (Arrays.asList(pathNames).equals(albumEntry.getValue().getNameComps())) {
           // album already exists
           return albumEntry.getKey();
         }
       }
       throw new RuntimeException("Directory " + newAlbumPath + " already exsists");
     }
     if (!newAlbumPath.exists()) {
       final boolean createParent = newAlbumPath.mkdirs();
       if (!createParent)
         throw new RuntimeException("Cannot create Directory " + newAlbumPath);
     }
 
     return appendAlbum(loadedAlbums, newAlbumPath, null, null);
   }
 
   public Collection<String> evaluateRepositoriesToSync(final String instanceName, final Set<String> existingRepositories, final ArchiveData config) {
     final Collection<String> ret = new HashSet<>(existingRepositories);
     if (config != null && config.getAlbumPerStorage() != null) {
       final Collection<String> configuredToFetch = config.getAlbumPerStorage().get(instanceName);
       if (configuredToFetch != null) {
         ret.retainAll(configuredToFetch);
       }
     }
     return ret;
   }
 
   @Override
   public Album getAlbum(final String albumId) {
     return listAlbums().get(albumId);
   }
 
   @Override
   public String getArchiveName() {
     return archiveData.getArchiveName();
   }
 
   @Override
   public File getBaseDir() {
     return baseDir;
   }
 
   @Override
   public String getCollectionId() {
     return archiveData.getArchiveName();
   }
 
   @Override
   public File getImportBaseDir() {
     return importBaseDir;
   }
 
   @Override
   public String getInstanceId() {
     return instanceId;
   }
 
   @Override
   public String getInstanceName() {
     return instanceName;
   }
 
   @Override
   public Repository getMetaRepository() {
     return metaGit.getRepository();
   }
 
   @Override
   public void importFiles(final File importDir) {
 
     try {
       if (!importDir.getAbsolutePath().startsWith(importBaseDir.getAbsolutePath())) {
         logger.error("Secutity-Error: Not allowed to read Images from " + importDir + " (Import-Path is " + importBaseDir + ")");
         return;
       }
       final HashSet<Album> modifiedAlbums = new HashSet<Album>();
       final SortedMap<Date, Album> importAlbums = new TreeMap<Date, Album>();
       for (final Album album : loadAlbums(true).values()) {
         final Date beginDate = album.getAutoAddBeginDate();
         if (beginDate != null)
           importAlbums.put(beginDate, album);
       }
       final Collection<File> deleteFiles = new ArrayList<File>();
       final Collection<File> importCandicates = collectImportFiles(importDir);
       for (final File file : importCandicates) {
         try {
           // logger.info("Read: " + file.getName());
           final Metadata metadata = ImageMetadataReader.readMetadata(file);
           final Date createDate = MetadataUtil.readCreateDate(metadata);
           if (createDate == null)
             // skip images without creation date
             continue;
           final SortedMap<Date, Album> entriesBeforeDate = importAlbums.headMap(createDate);
           if (entriesBeforeDate.isEmpty())
             // no matching album found
             continue;
           final Album album = entriesBeforeDate.get(entriesBeforeDate.lastKey());
           // logger.info(" ->" + album.getName());
           if (album.importImage(file, createDate)) {
             modifiedAlbums.add(album);
             logger.debug("image " + file + " imported successfully to " + album.getName());
             deleteFiles.add(file);
           } else {
             logger.warn("Could not import image " + file);
           }
         } catch (final ImageProcessingException e) {
           throw new RuntimeException("Cannot import file " + file, e);
         }
       }
       for (final Album album : modifiedAlbums) {
         album.commit("automatically imported");
       }
       for (final File file : deleteFiles) {
         final boolean deleted = file.delete();
         if (!deleted)
           logger.error("Cannot delete File " + file);
       }
     } finally {
       refreshCache();
     }
   }
 
   @Override
   public Map<String, Album> listAlbums() {
     return loadAlbums(false);
   }
 
   public ArchiveData loadMetaConfigFile(final File configFile) {
     try {
       return mapper.readValue(configFile, ArchiveData.class);
     } catch (final IOException e) {
       throw new RuntimeException("Cannot read meta-config from " + configFile, e);
     }
   }
 
   @Override
   public void notifyCameraStorePlugged(final File path) {
     importFiles(path);
   }
 
   @Override
   public void notifySyncBareDiskPlugged(final File path) {
     syncExternal(path, true);
   }
 
   @Override
   public void notifySyncDiskPlugged(final File path) {
     syncExternal(path, false);
   }
 
   public String readClientId(final File path, final boolean bare) throws IOException, FileNotFoundException {
     String remoteName = null;
     @SuppressWarnings("unchecked")
     final List<String> clientIdLines = IOUtils.readLines(new FileInputStream(new File(path, bare ? ".bareid" : ".clientid")), "utf-8");
     for (final String line : clientIdLines) {
       if (!line.trim().isEmpty())
         remoteName = line.trim();
     }
     return remoteName;
   }
 
   @Override
   public synchronized void registerClient(final String albumId, final String clientId) {
     final String albumPath = Util.decodeStringOfUrl(albumId);
     final Map<String, Collection<String>> albumPerStorage = archiveData.getAlbumPerStorage();
 
     final Collection<String> albumCollection;
     if (albumPerStorage.containsKey(clientId))
       albumCollection = albumPerStorage.get(clientId);
     else {
       albumCollection = new TreeSet<String>();
       albumPerStorage.put(clientId, albumCollection);
     }
     if (!albumCollection.contains(albumPath)) {
       albumCollection.add(albumPath);
       updateMeta("added " + albumPath + " to client " + clientId);
     }
   }
 
   @Override
   public void setArchiveName(final String archiveName) {
     if (StringUtils.equals(archiveData.getArchiveName(), archiveName))
       return;
     archiveData.setArchiveName(archiveName);
     updateMeta("ArchiveName upated");
     executorService.submit(new Runnable() {
 
       @Override
       public void run() {
         pollCurrentKnownPeers();
       }
     });
   }
 
   @Override
   public synchronized void setBaseDir(final File baseDir) {
     if (ObjectUtils.equals(this.baseDir, baseDir))
       return;
     this.baseDir = baseDir;
    loadedAlbums = Collections.emptyMap();
     lastLoadedDate.set(0);
     if (preferences != null) {
       preferences.put(ALBUM_PATH_PREFERENCE, baseDir.getAbsolutePath());
       flushPreferences();
     }
     loadMetaConfig();
     if (executorService != null)
       executorService.submit(new Runnable() {
 
         @Override
         public void run() {
           refreshCache();
         }
       });
   }
 
   public void setExecutorService(final ScheduledExecutorService executorService) {
     this.executorService = executorService;
   }
 
   @Override
   public void setImportBaseDir(final File importBaseDir) {
     if (ObjectUtils.equals(this.importBaseDir, importBaseDir))
       return;
     this.importBaseDir = importBaseDir;
     if (importBaseDir != null && executorService != null) {
       if (fileWatcher != null) {
         fileWatcher.close();
         fileWatcher = new FileWatcher(importBaseDir, executorService, this);
       }
     }
     if (preferences != null) {
       preferences.put(IMPORT_BASE_PATH_REFERENCE, importBaseDir.getAbsolutePath());
       flushPreferences();
     }
   }
 
   @Override
   public void setInstanceName(final String instanceName) {
     if (ObjectUtils.equals(this.instanceName, instanceName))
       return;
     this.instanceName = instanceName;
     if (preferences != null) {
       preferences.put(INSTANCE_NAME_PREFERENCE, instanceName);
       flushPreferences();
     }
   }
 
   @Override
   public void unRegisterClient(final String albumId, final String clientId) {
     final String albumPath = Util.decodeStringOfUrl(albumId);
     final Map<String, Collection<String>> albumPerStorage = archiveData.getAlbumPerStorage();
 
     if (!albumPerStorage.containsKey(clientId)) {
       return;
     }
     final Collection<String> albumCollection = albumPerStorage.get(clientId);
     if (albumCollection.contains(albumPath)) {
       albumCollection.remove(albumPath);
       updateMeta("added " + albumPath + " to client " + clientId);
     }
   }
 
   @Override
   public void waitForAlbums() {
     loadAlbums(true);
   }
 
   /**
    * Start scheduling automatically after initializing
    */
   @PostConstruct
   protected void startScheduling() {
     executorService.scheduleWithFixedDelay(new Runnable() {
 
       @Override
       public void run() {
         try {
           refreshCache();
         } catch (final Throwable t) {
           logger.warn("Exception while refreshing thumbnails", t);
         }
       }
     }, 60, 2 * 60, TimeUnit.MINUTES);
   }
 
   private String appendAlbum(final Map<String, Album> albumMap, final File albumDir, final String remoteUri, final String serverName) {
     final String[] nameComps = albumDir.getAbsolutePath().substring(getBasePath().getAbsolutePath().length() + 1).split(File.pathSeparator);
     final String albumId = Util.encodeStringForUrl(StringUtils.join(nameComps, "/"));
     synchronized (getAlbumLock(albumDir)) {
       if (!albumMap.containsKey(albumId))
         albumMap.put(albumId, new Album(albumDir, nameComps, repositoryService, remoteUri, serverName));
     }
     return albumId;
   }
 
   private String cleanAlbumName(final boolean bare, final String relativeDirectoryName) {
     if (bare) {
       if (!relativeDirectoryName.endsWith(".git"))
         throw new RuntimeException(relativeDirectoryName + " not ends with .git");
       return relativeDirectoryName.substring(0, relativeDirectoryName.length() - 4);
     }
     return relativeDirectoryName;
   }
 
   private Collection<File> collectImportFiles(final File importDir) {
     if (!importDir.isDirectory())
       return Collections.emptyList();
     final ArrayList<File> ret = new ArrayList<File>();
     ret.addAll(Arrays.asList(importDir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File pathname) {
         if (!pathname.canRead())
           return false;
         if (!pathname.isFile())
           return false;
         final String lowerFilename = pathname.getName().toLowerCase();
         return lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".nef") || lowerFilename.endsWith(".jpeg");
       }
     })));
     for (final File dir : importDir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File pathname) {
         return pathname.isDirectory();
       }
     })) {
       ret.addAll(collectImportFiles(dir));
     }
     return ret;
   }
 
   private void configureFromPreferences() {
     if (baseDir == null) {
       preferences = Preferences.userNodeForPackage(FileAlbumAccess.class);
       readLocalSettingsFromPreferences();
       preferences.addPreferenceChangeListener(new PreferenceChangeListener() {
         @Override
         public void preferenceChange(final PreferenceChangeEvent arg0) {
           readLocalSettingsFromPreferences();
         }
       });
     }
   }
 
   private void doLoadAlbums(final boolean forceWait) {
     if (forceWait)
       try {
         updateAlbumListSemaphore.acquire();
       } catch (final InterruptedException e) {
         // interrupted
         return;
       }
     else {
       final boolean hasLock = updateAlbumListSemaphore.tryAcquire();
       if (!hasLock)
         return;
     }
     try {
       if (needToLoadAlbumList())
         try {
           final Map<String, Album> ret = new ConcurrentHashMap<String, Album>();
           final File basePath = getBasePath();
           logger.debug("Load Repositories from: " + basePath);
           final int basePathLength = basePath.getAbsolutePath().length();
           for (final File albumDir : findAlbums(basePath, false)) {
             logger.debug("Load Repository " + albumDir);
             final String relativePath = albumDir.getAbsolutePath().substring(basePathLength + 1);
             if (relativePath.equals(META_REPOSITORY))
               continue;
             appendAlbum(ret, albumDir, null, null);
           }
           lastLoadedDate.set(System.currentTimeMillis());
           loadedAlbums = ret;
         } catch (final Throwable e) {
           logger.error("Troubles while accessing resource " + baseDir, e);
         }
     } finally {
       updateAlbumListSemaphore.release();
     }
   }
 
   private Collection<File> findAlbums(final File dir, final boolean pure) {
     if (repositoryService.isRepository(dir, pure)) {
       return Collections.singleton(dir);
     }
     final File[] foundFiles = dir.listFiles(new FileFilter() {
       @Override
       public boolean accept(final File pathname) {
         return pathname.isDirectory();
       }
     });
     final ArrayList<File> ret = new ArrayList<File>();
     if (foundFiles != null) {
       Arrays.sort(foundFiles);
       for (final File subDir : foundFiles) {
         ret.addAll(findAlbums(subDir, pure));
       }
     }
     return ret;
   }
 
   private void flushPreferences() {
     try {
       preferences.flush();
     } catch (final BackingStoreException e) {
       logger.warn("Cannot persist config", e);
     }
   }
 
   private Object getAlbumLock(final File albumFile) {
     final String key = albumFile.getAbsolutePath();
     if (createAlbumLocks.containsKey(key))
       return createAlbumLocks.get(key);
     createAlbumLocks.putIfAbsent(key, new Object());
     return createAlbumLocks.get(key);
 
   }
 
   private File getBasePath() {
     return baseDir.getAbsoluteFile();
   }
 
   private File getConfigFile() {
     return new File(getMetaDir(), "config.json");
   }
 
   private File getMetaDir() {
     final File metaDir = new File(getBasePath(), META_REPOSITORY);
     if (!metaDir.exists())
       metaDir.mkdirs();
     return metaDir;
   }
 
   @PostConstruct
   private void init() {
     configureFromPreferences();
     if (importBaseDir != null && executorService != null)
       fileWatcher = new FileWatcher(importBaseDir, executorService, this);
   }
 
   @PostConstruct
   private void listenPeers() {
     if (jmmDNS != null)
       return;
     jmmDNS = JmmDNS.Factory.getInstance();
     final ServiceListener serviceListener = new ServiceListener() {
 
       @Override
       public void serviceAdded(final ServiceEvent event) {
         event.getDNS().requestServiceInfo(event.getType(), event.getName());
       }
 
       @Override
       public void serviceRemoved(final ServiceEvent event) {
         pollCurrentKnownPeers();
       }
 
       @Override
       public void serviceResolved(final ServiceEvent event) {
         pollCurrentKnownPeers();
       }
     };
     jmmDNS.addNetworkTopologyListener(new NetworkTopologyListener() {
 
       @Override
       public void inetAddressAdded(final NetworkTopologyEvent event) {
         event.getDNS().addServiceListener(SERVICE_TYPE, serviceListener);
       }
 
       @Override
       public void inetAddressRemoved(final NetworkTopologyEvent event) {
         pollCurrentKnownPeers();
       }
     });
   }
 
   private Map<String, Album> loadAlbums(final boolean wait) {
     if (needToLoadAlbumList()) {
       if (wait)
         doLoadAlbums(true);
       else
         executorService.submit(new Runnable() {
           @Override
           public void run() {
             doLoadAlbums(false);
           }
         });
     }
     return loadedAlbums;
   }
 
   private void loadMetaConfig() {
     final File metaDir = getMetaDir();
 
     if (new File(metaDir, ".git").exists()) {
       try {
         metaGit = Git.open(metaDir);
       } catch (final IOException e) {
         throw new RuntimeException("Cannot access to git-repository of " + baseDir, e);
       }
     } else {
       try {
         metaGit = Git.init().setDirectory(metaDir).call();
       } catch (final GitAPIException e) {
         throw new RuntimeException("Cannot create meta repository", e);
       }
     }
     final File cacheDir = new File(metaDir, META_CACHE);
     if (!cacheDir.exists())
       cacheDir.mkdirs();
     final File configFile = new File(metaDir, "config.json");
     if (!configFile.exists()) {
       archiveData = new ArchiveData();
       archiveData.setArchiveName(UUID.randomUUID().toString());
       updateMeta("config.json built");
     } else {
       archiveData = loadMetaConfigFile(configFile);
     }
   }
 
   private String makeDefaultInstanceName() {
     try {
       return InetAddress.getLocalHost().getHostName();
     } catch (final UnknownHostException e) {
       return UUID.randomUUID().toString();
     }
   }
 
   private String makeRepositoryDirectoryName(final boolean pure, final String baseName) {
     if (pure)
       return baseName + ".git";
     return baseName;
   }
 
   private boolean needToLoadAlbumList() {
     return loadedAlbums == null || (System.currentTimeMillis() - lastLoadedDate.get()) > TimeUnit.MINUTES.toMillis(5);
   }
 
   private ResponseEntity<PingResponse> ping(final URI uri) {
     return restTemplate.getForEntity(uri.resolve("ping.json"), PingResponse.class);
   }
 
   private void pollCurrentKnownPeers() {
     processFoundServices(jmmDNS.list(SERVICE_TYPE));
     refreshCache();
   }
 
   private synchronized void processFoundServices(final ServiceInfo[] services) {
     final Map<String, URI> foundPeers = new HashMap<String, URI>();
     for (final ServiceInfo serviceInfo : services) {
       final int peerPort = serviceInfo.getPort();
       final InetAddress[] addresses = serviceInfo.getInetAddresses();
       for (final InetAddress inetAddress : addresses) {
         if (inetAddress.isLinkLocalAddress())
           continue;
         try {
           final URI candidateUri = new URI("http", null, inetAddress.getHostAddress(), peerPort, "/rest/", null, null);
           final ResponseEntity<PingResponse> responseEntity = ping(candidateUri);
           if (!responseEntity.hasBody() || responseEntity.getStatusCode().series() != Series.SUCCESSFUL) {
             continue;
           }
           final PingResponse pingResponse = responseEntity.getBody();
           if (!pingResponse.getArchiveId().equals(getArchiveName()))
             continue;
           if (pingResponse.getServerId().equals(getInstanceId()))
             continue;
           foundPeers.put(pingResponse.getServerId(), candidateUri);
         } catch (final URISyntaxException e) {
           logger.warn("Cannot build URL for " + serviceInfo, e);
         } catch (final RestClientException e) {
           logger.warn("ping " + serviceInfo, e);
         }
       }
     }
     logger.info("Found peers: ");
     for (final Entry<String, URI> peerEntry : foundPeers.entrySet()) {
       logger.info(" - " + peerEntry.getKey() + ": " + peerEntry.getValue());
     }
     final Collection<URI> currentActiveServers = new HashSet<URI>(foundPeers.values());
     final Collection<URI> alreadyKnownServers = peerServers.get();
     if (!ObjectUtils.equals(currentActiveServers, alreadyKnownServers)) {
       peerServers.set(foundPeers.values());
       updateAllRepositories();
     }
   }
 
   private void readLocalSettingsFromPreferences() {
     setBaseDir(new File(preferences.get(ALBUM_PATH_PREFERENCE, new File(System.getProperty("user.home"), "images").getAbsolutePath())));
     setImportBaseDir(new File(preferences.get(IMPORT_BASE_PATH_REFERENCE, "nowhere")));
     setInstanceName(preferences.get(INSTANCE_NAME_PREFERENCE, makeDefaultInstanceName()));
   }
 
   private void refreshCache() {
     try {
       final AtomicInteger imageCount = new AtomicInteger();
       // limit the queue size for take not too much memory
       final Semaphore queueLimitSemaphore = new Semaphore(100);
       final long startTime = System.currentTimeMillis();
       final Collection<Album> albums = loadAlbums(true).values();
       @Cleanup
       final ProgressHandler albumProgress = stateManager.newProgress(albums.size(), ProgressType.REFRESH_ALBUM, instanceName);
       for (final Album album : albums) {
         albumProgress.notfiyProgress(album.getName());
         final Collection<AlbumImage> images = album.listImages().values();
         final ProgressHandler thumbnailProgress = stateManager.newProgress(images.size(), ProgressType.REFRESH_THUMBNAIL, album.getName());
         for (final AlbumImage image : images) {
           queueLimitSemaphore.acquire();
           executorService.submit(new Runnable() {
 
             @Override
             public void run() {
               try {
                 thumbnailProgress.notfiyProgress(image.getName());
                 // read Metadata
                 // image.captureDate();
                 // read Thumbnail
                 image.getThumbnail();
               } finally {
                 imageCount.incrementAndGet();
                 queueLimitSemaphore.release();
                 thumbnailProgress.finishProgress();
               }
             }
           });
         }
       }
       // wait until the end
       queueLimitSemaphore.acquire(100);
       final long endTime = System.currentTimeMillis();
       final Duration duration = new Duration(startTime, endTime);
       final StringBuffer buf = new StringBuffer("Refresh-Time: ");
       PeriodFormat.wordBased().getPrinter().printTo(buf, duration.toPeriod(), Locale.getDefault());
       buf.append(", ");
       buf.append(imageCount.intValue());
       buf.append(" Images");
       logger.info(buf.toString());
     } catch (final InterruptedException e) {
       logger.info("cache refresh interrupted");
     }
   }
 
   @PreDestroy
   private void shutdownDnsListener() throws IOException {
     if (jmmDNS != null) {
       jmmDNS.close();
       jmmDNS = null;
     }
   }
 
   @PreDestroy
   private void shutdownFileWatcher() {
     if (fileWatcher != null)
       fileWatcher.close();
   }
 
   private void syncExternal(final File path, final boolean bare) {
     try {
       final String localName = getInstanceName();
       final String remoteName = readClientId(path, bare);
       if (remoteName == null) {
         logger.warn("No valid client-id at " + path);
         return;
       }
       final File remoteMetaDir = new File(path, makeRepositoryDirectoryName(bare, META_REPOSITORY));
       final boolean metaModified = repositoryService.sync(metaGit, remoteMetaDir, localName, remoteName, bare);
       if (metaModified)
         loadMetaConfig();
       final ArchiveData remoteConfig = loadMetaConfigFile(new File(remoteMetaDir, "config.json"));
       final Collection<File> existingAlbumsOnExternalDisk = findAlbums(path, bare);
       final Map<String, File> existingRemoteDirectories = new HashMap<>();
       final int basePathLength = path.getAbsolutePath().length() + 1;
       for (final File file : existingAlbumsOnExternalDisk) {
         final String relativeName = cleanAlbumName(bare, file.getAbsolutePath().substring(basePathLength));
         if (relativeName.equals(META_REPOSITORY))
           continue;
         existingRemoteDirectories.put(relativeName, file);
       }
       final Map<String, Album> existingLocalAlbums = new HashMap<>();
       for (final Album album : listAlbums().values()) {
         existingLocalAlbums.put(album.getName(), album);
       }
       final Collection<String> albumsToSync = evaluateRepositoriesToSync(localName, existingRemoteDirectories.keySet(), archiveData);
       albumsToSync.addAll(evaluateRepositoriesToSync(remoteName, existingLocalAlbums.keySet(), remoteConfig));
       @Cleanup
       final ProgressHandler progress = stateManager.newProgress(albumsToSync.size(), ProgressType.ITERATE_OVER_ALBUMS, remoteName);
       for (final String albumName : albumsToSync) {
         progress.notfiyProgress(albumName);
         final Album localAlbumForRemote = existingLocalAlbums.get(albumName);
         final File remoteDir = existingRemoteDirectories.get(albumName);
         if (localAlbumForRemote == null) {
           if (remoteDir != null)
             appendAlbum(loadedAlbums, new File(getBaseDir(), albumName), remoteDir.toURI().toString(), remoteName);
         } else {
           localAlbumForRemote.sync(new File(path, makeRepositoryDirectoryName(bare, albumName)), localName, remoteName, bare);
         }
       }
     } catch (final Throwable t) {
       logger.warn("Cannot sync with " + path, t);
     }
   }
 
   private void updateAllRepositories() {
     final Collection<URI> collection = peerServers.get();
     @Cleanup
     final ProgressHandler peerServerProgressHandler =
                                                       stateManager.newProgress(collection.size(), ProgressType.ITERATE_OVER_SERVERS,
                                                                                "Polling remote Servers");
     for (final URI peerServerUri : collection) {
       final ResponseEntity<PingResponse> responseEntity = ping(peerServerUri);
       if (!responseEntity.hasBody())
         continue;
       final PingResponse pingResponse = responseEntity.getBody();
       final String remoteHost = peerServerUri.getHost();
       peerServerProgressHandler.notfiyProgress(pingResponse.getServerName());
       final int remotePort = pingResponse.getGitPort();
       final ResponseEntity<AlbumList> albumListEntity = restTemplate.getForEntity(peerServerUri.resolve("albums.json"), AlbumList.class);
       if (!albumListEntity.hasBody())
         continue;
       try {
         final boolean metaUpdated =
                                     repositoryService.pull(metaGit,
                                                            new URI("git", null, remoteHost, remotePort, "/.meta", null, null).toASCIIString(),
                                                            pingResponse.getServerName());
         if (metaUpdated)
           loadMetaConfig();
       } catch (final Throwable e) {
         logger.error("Cannot pull remote repository from " + remoteHost, e);
       }
       final AlbumList remoteAlbumList = albumListEntity.getBody();
       final Map<String, Album> localAlbums = listAlbums();
       final Collection<AlbumEntry> albumNames = remoteAlbumList.getAlbumNames();
       @Cleanup
       final ProgressHandler albumProgress =
                                             stateManager.newProgress(albumNames.size(), ProgressType.ITERATE_OVER_ALBUMS,
                                                                      pingResponse.getServerName());
       for (final AlbumEntry album : albumNames) {
         try {
           final Album localAlbumForRemote = localAlbums.get(album.getId());
           albumProgress.notfiyProgress(album.getName());
           final String remoteUri = new URI("git", null, remoteHost, remotePort, "/" + album.getId(), null, null).toASCIIString();
           if (localAlbumForRemote == null)
             appendAlbum(loadedAlbums, new File(getBaseDir(), album.getName()), remoteUri, pingResponse.getServerName());
           else
             localAlbumForRemote.pull(remoteUri, pingResponse.getServerName());
         } catch (final Throwable e) {
           logger.error("Cannot sync with " + remoteHost, e);
         }
       }
     }
   }
 
   private synchronized void updateMeta(final String message) {
     try {
       mapper.writerWithDefaultPrettyPrinter().writeValue(getConfigFile(), archiveData);
       metaGit.add().addFilepattern(".").call();
       metaGit.commit().setMessage(message).call();
     } catch (final IOException e) {
       throw new RuntimeException("Cannot update Metadata", e);
     } catch (final GitAPIException e) {
       throw new RuntimeException("Cannot update Metadata", e);
     }
   }
 }
