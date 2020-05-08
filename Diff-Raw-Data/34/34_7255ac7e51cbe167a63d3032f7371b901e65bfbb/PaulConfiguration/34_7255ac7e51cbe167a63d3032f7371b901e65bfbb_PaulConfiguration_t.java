 package au.edu.uq.cmm.paul;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.NoResultException;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.apache.log4j.Logger;
 import org.hibernate.annotations.GenericGenerator;
 
 import au.edu.uq.cmm.aclslib.config.Configuration;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.paul.status.Facility;
 
 /**
  * This class represents the configuration details of a DataGrabber instance.
  * The details are persisted using Hibernate / JPA.
  * 
  * @author scrawley
  */
 @Entity
 @Table(name = "CONFIGURATION")
 public class PaulConfiguration implements Configuration {
     private static final Logger LOG = Logger.getLogger(PaulConfiguration.class);
     
     private Long id;
     private List<Facility> facilities = new ArrayList<Facility>();
     
     private int proxyPort = 1024;
     private int serverPort = 1024;
     private String serverHost;
     private String proxyHost;
     private boolean useProject;
     private String captureDirectory;
     private String archiveDirectory;
     private String baseFileUrl;
     private String feedId;
     private String feedTitle;
     private String feedAuthor;
     private String feedAuthorEmail;
     private String feedUrl;
     private int feedPageSize = 20;
     private long facilityRecheckInterval;
     private long queueExpiryTime;
     private long queueExpiryInterval;
     private boolean expireByDeleting;
     private DataGrabberRestartPolicy dataGrabberRestartPolicy = 
             DataGrabberRestartPolicy.NO_AUTO_START;
     private boolean holdDatasetsWithNoUser = true;
     
     
     public int getProxyPort() {
         return proxyPort;
     }
 
     public String getServerHost() {
         return serverHost;
     }
 
     public int getServerPort() {
         return serverPort;
     }
 
     public boolean isUseProject() {
         return useProject;
     }
 
     public void setProxyPort(int proxyPort) {
         this.proxyPort = proxyPort;
     }
 
     public void setServerHost(String serverHost) {
         this.serverHost = serverHost;
     }
 
     public void setServerPort(int serverPort) {
         this.serverPort = serverPort;
     }
 
     public void setUseProject(boolean useProject) {
         this.useProject = useProject;
     }
 
     public String getProxyHost() {
         return proxyHost;
     }
 
     public void setProxyHost(String proxyHost) {
         this.proxyHost = proxyHost;
     }
 
     @Transient
     public final String getDummyFacility() {
         for (FacilityConfig facility : getFacilities()) {
             if (facility.isDummy()) {
                 return facility.getFacilityName();
             }
         }
         throw new IllegalStateException("There are no dummy facilities");
     }
 
     public String getCaptureDirectory() {
         return captureDirectory;
     }
 
     public void setCaptureDirectory(String captureDirectory) {
         this.captureDirectory = captureDirectory;
     }
 
     public String getArchiveDirectory() {
         return archiveDirectory;
     }
 
     public void setArchiveDirectory(String archiveDirectory) {
         this.archiveDirectory = archiveDirectory;
     }
 
     public String getBaseFileUrl() {
         return baseFileUrl;
     }
 
     public void setBaseFileUrl(String baseFileUrl) {
         this.baseFileUrl = baseFileUrl;
     }
 
     public String getFeedId() {
         return feedId;
     }
 
     public void setFeedId(String feedId) {
         this.feedId = feedId;
     }
 
     public String getFeedTitle() {
         return feedTitle;
     }
 
     public void setFeedTitle(String feedTitle) {
         this.feedTitle = feedTitle;
     }
 
     public String getFeedAuthor() {
         return feedAuthor;
     }
 
     public void setFeedAuthor(String feedAuthor) {
         this.feedAuthor = feedAuthor;
     }
 
     public String getFeedAuthorEmail() {
         return feedAuthorEmail;
     }
 
     public void setFeedAuthorEmail(String feedAuthorEmail) {
         this.feedAuthorEmail = feedAuthorEmail;
     }
     
     public String getFeedUrl() {
         return feedUrl;
     }
 
     public void setFeedUrl(String feedUrl) {
         this.feedUrl = feedUrl;
     }
     
     public int getFeedPageSize() {
         return feedPageSize;
     }
 
     public void setFeedPageSize(int feedPageSize) {
         this.feedPageSize = feedPageSize;
     }
 
     public long getFacilityRecheckInterval() {
         return facilityRecheckInterval;
     }
 
     public void setFacilityRecheckInterval(long facilityRecheckInterval) {
         this.facilityRecheckInterval = facilityRecheckInterval;
     }
     
     public long getQueueExpiryTime() {
         return queueExpiryTime;
     }
 
     public void setQueueExpiryTime(long queueExpiryTime) {
         this.queueExpiryTime = queueExpiryTime;
     }
 
     public long getQueueExpiryInterval() {
         return queueExpiryInterval;
     }
 
     public void setQueueExpiryInterval(long queueExpiryInterval) {
         this.queueExpiryInterval = queueExpiryInterval;
     }
 
     public boolean isExpireByDeleting() {
         return expireByDeleting;
     }
 
     public void setExpireByDeleting(boolean expireByDeleting) {
         this.expireByDeleting = expireByDeleting;
     }
 
     public DataGrabberRestartPolicy getDataGrabberRestartPolicy() {
         return dataGrabberRestartPolicy;
     }
 
     public void setDataGrabberRestartPolicy(
             DataGrabberRestartPolicy dataGrabberRestartPolicy) {
         this.dataGrabberRestartPolicy = dataGrabberRestartPolicy;
     }
 
     public boolean isHoldDatasetsWithNoUser() {
         return holdDatasetsWithNoUser;
     }
 
     public void setHoldDatasetsWithNoUser(boolean holdDatasetsWithNoUser) {
         this.holdDatasetsWithNoUser = holdDatasetsWithNoUser;
     }
 
     public static PaulConfiguration load(EntityManagerFactory entityManagerFactory) {
         return load(entityManagerFactory, false);
     }
 
     public static PaulConfiguration load(EntityManagerFactory entityManagerFactory,
             boolean createIfMissing) {
         EntityManager entityManager = entityManagerFactory.createEntityManager();
         try {
             entityManager.getTransaction().begin();
             try {
                 PaulConfiguration res = entityManager.
                     createQuery("from PaulConfiguration", PaulConfiguration.class).
                     getSingleResult();
                 return res;
             } catch (NoResultException ex) {
                 if (createIfMissing) {
                     PaulConfiguration res = new PaulConfiguration();
                     entityManager.persist(res);
                     entityManager.getTransaction().commit();
                     return res;
                 } else {
                     throw new PaulException("The configuration record is missing", ex);
                 }
             }
         } finally {
             entityManager.close();
         }
     }
     
     @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
     @JoinColumn(name="facility_id")
     public List<Facility> getFacilities() {
         return facilities;
     }
 
     public void setFacilities(List<Facility> facilities) {
         this.facilities = facilities;
     }
     
     @Transient
     public Collection<FacilityConfig> getFacilityConfigs() {
         List<FacilityConfig> res = new ArrayList<FacilityConfig>();
         res.addAll(facilities);
         return res;
     }
 
     public FacilityConfig lookupFacilityByAddress(InetAddress addr) {
         for (FacilityConfig f : facilities) {
             if (f.getAddress().equals(addr.getHostAddress()) ||
                     f.getAddress().equals(addr.getHostName())) {
                 return f;
             }
         }
         return null;
     }
 
     public FacilityConfig lookupFacilityByName(String id) {
         for (FacilityConfig f : facilities) {
             if (id.equals(f.getFacilityName())) {
                 return f;
             }
         }
         return null;
     }
     
     public FacilityConfig lookupFacilityByLocalHostId(String localHostId) {
         for (FacilityConfig f : facilities) {
             if (localHostId.equals(f.getLocalHostId())) {
                 return f;
             }
         }
         return null;
     }
 
     @Transient
     public boolean isEmpty() {
         return facilities.isEmpty();
     }
 
     /**
      * Merge configuration details from a static configuration to the 
      * persistent configuration.  The merging process is pretty crude, as
      * this is only intended as a bootstrapping mechanism.  If the merge 
      * succeeds, the result updates the persistent configuration.
      * 
      * @param entityManagerFactory
      * @param staticConfig
      * @return the merged/persisted configuration
      */
     public PaulConfiguration merge(EntityManagerFactory entityManagerFactory,
             Configuration staticConfig) {
         LOG.info("Merging details from static Configuration");
         EntityManager entityManager = entityManagerFactory.createEntityManager();
         try {
             entityManager.getTransaction().begin();
             setProxyHost(staticConfig.getProxyHost());
             setServerHost(staticConfig.getServerHost());
             setProxyPort(staticConfig.getProxyPort());
             setServerPort(staticConfig.getServerPort());
             setBaseFileUrl(staticConfig.getBaseFileUrl());
             setCaptureDirectory(staticConfig.getCaptureDirectory());
             setArchiveDirectory(staticConfig.getArchiveDirectory());
             setFeedId(staticConfig.getFeedId());
             setFeedTitle(staticConfig.getFeedTitle());
             setFeedAuthor(staticConfig.getFeedAuthor());
             setFeedAuthorEmail(staticConfig.getFeedAuthorEmail());
             setFeedUrl(staticConfig.getFeedUrl());
             setFeedPageSize(staticConfig.getFeedPageSize());
             setQueueExpiryInterval(staticConfig.getQueueExpiryInterval());
             setQueueExpiryTime(staticConfig.getQueueExpiryTime());
             setExpireByDeleting(staticConfig.isExpireByDeleting());
             setDataGrabberRestartPolicy(staticConfig.getDataGrabberRestartPolicy());
             setHoldDatasetsWithNoUser(staticConfig.isHoldDatasetsWithNoUser());
             for (FacilityConfig facilityConfig: staticConfig.getFacilityConfigs()) {
                 boolean found = false;
                 for (FacilityConfig f : facilities) {
                     if (f.getFacilityName().equals(facilityConfig.getFacilityName()) ||
                             f.getAddress().equals(facilityConfig.getAddress())) {
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     facilities.add(new Facility(facilityConfig));
                     LOG.info("Added facility '" + facilityConfig.getFacilityName() + 
                             "' with address '" + facilityConfig.getAddress() + "'");
                 }
             }
             PaulConfiguration res = entityManager.merge(this);
             entityManager.getTransaction().commit();
             return res;
         } finally {
             entityManager.close();
         }
     }
 
     @Id
     @GeneratedValue(generator="increment")
     @GenericGenerator(name="increment", strategy = "increment")
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 }
