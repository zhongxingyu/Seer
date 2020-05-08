 package com.gentics.cr.util.indexing;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 
 
 /**
  * 
  * Last changed: $Date: 2009-07-10 10:49:03 +0200 (Fr, 10 Jul 2009) $
  * @version $Revision: 131 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 
 public abstract class IndexLocation {
   //STATIC MEMBERS
   protected static final Logger log = Logger.getLogger(IndexLocation.class);
   private static final String REOPEN_CHECK_KEY = "reopencheck";
   protected static final String REOPEN_FILENAME = "reopen";
   protected static final String INDEX_LOCATIONS_KEY = "indexLocations";
   protected static final String INDEX_PATH_KEY = "path";
   private static final String INDEX_LOCATION_CLASS_KEY = "indexLocationClass";
   /**
    * default value for index location class.
    */
   //private static final String INDEX_LOCATION_CLASS_DEFAULT = "com.gentics.cr.lucene.indexer.index.LuceneSingleIndexLocation";
   private static final String PERIODICAL_KEY = "PERIODICAL";
   private static Hashtable<String,IndexLocation> indexmap;
   private static final String LOCK_DETECTION_KEY = "LOCKDETECTION";
   /**
    * The key in the configuration for specifying the update job implementation class
    */
   public static final String UPDATEJOBCLASS_KEY = "updatejobclass";
   private static final String DEFAULT_UPDATEJOBCLASS = "com.gentics.cr.lucene.indexer.index.CRLuceneIndexJob";
 
 
   //Instance Members
   private IndexJobQueue queue = null;
   protected CRConfig config;
   private boolean periodical = false;
   private int periodical_interval = 60; //60 seconds
   private Thread periodical_thread;
   private boolean lockdetection = false;
   protected boolean reopencheck = false;
   
     
   /**
    * Creates the reopen file to make portlet reload the index.
    */
   public abstract void createReopenFile();
   
   /**
    * Checks Lock and throws Exception if Lock exists
    * @throws LockedIndexException 
    * @throws IOException 
    */
   public abstract void checkLock() throws Exception;
   
   
   protected IndexLocation(CRConfig config)
   {
     this.config = config;
     queue = new IndexJobQueue(config);
     String per = (String)config.get(PERIODICAL_KEY);
     periodical = Boolean.parseBoolean(per);
     String s_reopen = (String)config.get(REOPEN_CHECK_KEY);
     if(s_reopen!=null)
     {
       reopencheck = Boolean.parseBoolean(s_reopen);
     }
     String s_lockdetect = (String)config.get(LOCK_DETECTION_KEY);
     if(s_lockdetect!=null)
     {
       lockdetection = Boolean.parseBoolean(s_lockdetect);
     }
     
   }
   
   /**
    * 
    */
   private void initializeQueue()
   {
     if(periodical)
     {
       periodical_thread = new Thread(new Runnable(){
         public void run()
         {
           boolean interrupted = false;
           while(periodical && !interrupted && !Thread.currentThread().isInterrupted())
           {
             try
             {
               createAllCRIndexJobs();
               Thread.sleep(periodical_interval*1000);
               
             }catch(InterruptedException ex)
             {
               interrupted = true;
             }
           }
         }
       });
       periodical_thread.setName("PeriodicIndexJobCreator");
       periodical_thread.start();
       
     }
     this.queue.startWorker();
   }
   
   /**
    * Returns if the users has configured lockdetection for the index location
    * @return
    */
   public boolean hasLockDetection()
   {
     return(this.lockdetection);
   }
   
   
   protected static String getIndexLocationKey(CRConfig config)
   {
     String path="";
     GenericConfiguration locs = (GenericConfiguration)config.get(INDEX_LOCATIONS_KEY);
     if(locs!=null)
     {
       Map<String,GenericConfiguration> locationmap = locs.getSortedSubconfigs();
       if(locationmap!=null)
       {
         for(GenericConfiguration locconf:locationmap.values())
         {
           String p = locconf.getString(INDEX_PATH_KEY);
           if(p!=null && !"".equals(p))
           {
             path+=p;
           }
         }
       }
     }
     return path;
   }
 
   /**
    * Gets the index location configured in config.
    * @param config if the config does not hold the param indexLocation or if
    * indexLocation = "RAM", an RAM Directory will be created and returned
    * @return initialized IndexLocation
    */
   public static synchronized IndexLocation getIndexLocation(
       final CRConfig config) {
     IndexLocation dir = null;
     String key = getIndexLocationKey(config);
     if (key == null || "".equals(key)) {
       log.error("COULD NOT FIND CONFIG FOR INDEXLOCATION. check config @ "
           + config.getName());
       return null;
     }
     if (indexmap == null) {
       indexmap = new Hashtable<String, IndexLocation>();
       dir = createNewIndexLocation(config);
       indexmap.put(key, dir);
     } else {
       dir = indexmap.get(key);
       if (dir == null) {
         dir = createNewIndexLocation(config);
         indexmap.put(key, dir);
       }
     }
     return dir;
   }
 
   /**
    * Create new IndexLocation for the configured Implementation of
    * {@link AbstractUpdateCheckerJob}.
    * @param config {@link CRConfig} of the actual indexLocation
    * @return IndexLocation that can be used for all configured Implementations
    * of {@link AbstractUpdateCheckerJob}
    */
   private static IndexLocation createNewIndexLocation(final CRConfig config) {
     Class<? extends IndexLocation> indexLocationClass =
       getIndexLocationClass(config);
     try {
       Constructor<? extends IndexLocation> indexLocationConstructor =
         indexLocationClass.getDeclaredConstructor(new Class[]{CRConfig.class});
       IndexLocation instance = indexLocationConstructor.newInstance(config);
       //Start worker threads on created indexLocation
       instance.initializeQueue();
       return instance;
     } catch (SecurityException e) {
       log.error("Cannot get Constructor(CRConfig) for IndexLocation class \""
           + indexLocationClass.getName() + "\"", e);
     } catch (NoSuchMethodException e) {
       log.error("Cannot get Constructor(CRConfig) for IndexLocation class \""
           + indexLocationClass.getName() + "\"", e);
     } catch (IllegalArgumentException e) {
       log.error("Cannot invoke Constructor for IndexLocation class \""
           + indexLocationClass.getName() + "\"", e);
     } catch (InstantiationException e) {
       log.error("Cannot invoke Constructor for IndexLocation class \""
           + indexLocationClass.getName() + "\"", e);
     } catch (IllegalAccessException e) {
       log.error("Cannot invoke Constructor for IndexLocation class \""
           + indexLocationClass.getName() + "\"", e);
     } catch (InvocationTargetException e) {
       log.error("Cannot invoke Constructor for IndexLocation class \""
           + indexLocationClass.getName() + "\"", e);
     }
     return new DefaultIndexLocation(config);
   }
 
   private static Class<? extends IndexLocation> getIndexLocationClass(
       final CRConfig config) {
     String indexLocationClassName = config.getString(INDEX_LOCATION_CLASS_KEY);
     Class<? extends IndexLocation> indexLocationClass = null;
     if (indexLocationClassName == null) {
       Object indexesObject = config.get(CRConfig.CR_KEY);
       if (indexesObject != null
           && indexesObject instanceof GenericConfiguration) {
         GenericConfiguration indexes =
           (GenericConfiguration) config.get(CRConfig.CR_KEY);
         for (Entry<String, GenericConfiguration> subConfigEntry
             : indexes.getSubConfigs().entrySet()) {
           String subConfigKey = subConfigEntry.getKey();
           GenericConfiguration subConfig = subConfigEntry.getValue();
           Class<? extends AbstractUpdateCheckerJob> subConfigClass =
             getUpdateJobImplementationClass(new CRConfigUtil(subConfig,
                 config.getName() + "." + subConfigKey));
           try {
             String nextIndexLocationClassName =
               subConfigClass.getField("INDEXLOCATIONCLASS").get(subConfigClass)
               .toString();
             if (indexLocationClassName == null) {
               indexLocationClassName = nextIndexLocationClassName;
             } else if (!indexLocationClassName
                 .equals(nextIndexLocationClassName)) {
               //TODO add advanced error handling. e.g. different classes can be
               //valid if they are subclasses of each other. In this case we
               //should create an instance of the deepest configured subclass.
               log.error("Not all of your configured implementations have the"
                   + " same value in the field \"INDEXLOCATIONCLASS\".");
             }
           } catch (NoSuchFieldException e) {
             log.error(subConfigClass.getName() + " has no field named "
                 + "\"INDEXLOCATIONCLASS\"", e);
           } catch (SecurityException e) {
             log.error("Cannot access Field \"INDEXLOCATIONCLASS\" on "
                 + subConfigClass.getName() + ".", e);
           } catch (IllegalArgumentException e) {
             log.error("Error getting static Field \"INDEXLOCATIONLCASS\" of"
                 + " class " + subConfigClass.getName(), e);
           } catch (IllegalAccessException e) {
             log.error("Cannot access Field \"INDEXLOCATIONCLASS\" on "
                 + subConfigClass.getName() + ".", e);
           }
         }
       }
     }
     if (indexLocationClassName != null) {
       try {
         Class<?> indexLocationClassGeneric =
           Class.forName(indexLocationClassName);
         indexLocationClass =
           indexLocationClassGeneric.asSubclass(IndexLocation.class);
       } catch (ClassNotFoundException e) {
         log.error("Cannot find class the IndexLocationClass defined in the"
             + " config or your UpdateJobImplementation. Therefore i cannot"
             + " create a specific IndexLocation for the configured"
             + " AbstractUpdateCheckerJob implementation.", e);
       }
     }
     if (indexLocationClass == null) {
       return IndexLocation.class;
     } else {
       return indexLocationClass;
     }
   }
 
   /**
    * Helper method to get Class of UpdateJobImplementation.
    * @param config {@link CRConfig} to 
    * @return
    */
   private static Class<? extends AbstractUpdateCheckerJob> getUpdateJobImplementationClass(CRConfig config){
     Class<?> updatejobImplementationClassGeneric;
     Class<? extends AbstractUpdateCheckerJob> updatejobImplementationClass;
     String updatejobimplementationClassName = config.getString(UPDATEJOBCLASS_KEY);
     if(updatejobimplementationClassName == null){
       updatejobimplementationClassName = DEFAULT_UPDATEJOBCLASS;
     }
     
     try {
       updatejobImplementationClassGeneric = Class.forName(updatejobimplementationClassName);
       updatejobImplementationClass = updatejobImplementationClassGeneric.asSubclass(AbstractUpdateCheckerJob.class);
       return updatejobImplementationClass;
     } catch (ClassNotFoundException e) {
       log.error("Cannot load class for creating a new IndexJob",e);
     }
     return null;
   }
   
   /**
    * Get number of documents in Index
    * @return doccount
    */
   public int getDocCount()
   {
     //TODO implement this generic
     int count = 0;
     return count;
   }
   
   
   
 
   /**
    * Tests if the IndexLocation contains an existing Index and returns true if it does.
    * @return true if index exists, otherwise false
    */
   public boolean isContainingIndex() {
     
     return getDocCount()>0;
   }
   
 
   
   /**
    * Creates a new CRIndexJob for the given CRConfig and adds the job to the queue
    * @param config
    * @param configmap 
    * @return
    */
   public boolean createCRIndexJob(CRConfig config,Hashtable<String,CRConfigUtil> configmap)
   {
     Class<? extends AbstractUpdateCheckerJob> updatejobImplementationClass = getUpdateJobImplementationClass(config);
     AbstractUpdateCheckerJob indexJob = null;
     try {
       Constructor<? extends AbstractUpdateCheckerJob> updatejobImplementationClassConstructor = updatejobImplementationClass.getConstructor(new Class[]{CRConfig.class, IndexLocation.class, Hashtable.class});
       Object indexJobObject = updatejobImplementationClassConstructor.newInstance(config,this,configmap);
       indexJob = (AbstractUpdateCheckerJob) indexJobObject;
       return this.queue.addJob(indexJob);
     } catch (ClassCastException e){
       log.error("Please configure an implementation of "+AbstractUpdateCheckerJob.class+" ",e);
     } catch (SecurityException e) {
       log.error("Cannot load class for creating a new IndexJob",e);
     } catch (NoSuchMethodException e) {
       log.error("Cannot find constructor for creating a new IndexJob",e);
     } catch (IllegalArgumentException e) {
       log.error("Error creating a new IndexJob",e);
     } catch (InstantiationException e) {
       log.error("Error creating a new IndexJob",e);
     } catch (IllegalAccessException e) {
       log.error("Error creating a new IndexJob",e);
     } catch (InvocationTargetException e) {
       log.error("Error creating a new IndexJob",e);
     }
     return false;
   }
   
   private static final String CR_KEY = "CR";
   
   /**
    * Creats jobs for all configured CRs
    */
   public void createAllCRIndexJobs() {
     
     Hashtable<String,CRConfigUtil> configs = getCRMap();
 
     for (Entry<String,CRConfigUtil> e:configs.entrySet()) {
       
         CRConfigUtil crC = e.getValue();
         createCRIndexJob(new CRConfigUtil(crC,crC.getName()),configs);
       
     }
     
 
   }
   
   /**
    * Creates a map of the configured CRs
    * @return
    */
   public Hashtable<String,CRConfigUtil> getCRMap()
   {
     CRConfig crconfig = this.config;
     Hashtable<String,CRConfigUtil> map = new Hashtable<String,CRConfigUtil>();
     
     GenericConfiguration CRc = (GenericConfiguration)crconfig.get(CR_KEY);
     if(CRc!=null)
     {
       Hashtable<String,GenericConfiguration> configs = CRc.getSubConfigs();
 
       for (Entry<String,GenericConfiguration> e:configs.entrySet()) {
         try {
           map.put(crconfig.getName() + "." + e.getKey(),
               new CRConfigUtil(e.getValue(), crconfig.getName() + "."
                   + e.getKey()));
         } catch (Exception ex) {
           String name = "<no config name>";
           String key  = "<no key>";
           CRException cex = new CRException(ex);
           if (e != null && e.getKey() != null) {
             key = e.getKey();
           }
           if (crconfig != null && crconfig.getName() != null) {
             name = crconfig.getName();
           }
           log.error("Error while creating cr map for " + name + "." + key +
               " - " + cex.getMessage(), cex);
         }
       }
     } else {
       log.error("THERE ARE NO CRs CONFIGURED FOR INDEXING.");
     }
     return map;
   }
   /**
    * Returns the IndexJobQueue
    * @return
    */
   public IndexJobQueue getQueue() {
     return this.queue;
   }
   
   /**
    * Tests if this IndexLocation has turned on periodical indexing
    * @return
    */
   public boolean isPeriodical()
   {
     return this.periodical;
   }
   
   /**
    * Stops all Index workers
    */
   public void stop()
   {
     if(this.periodical_thread!=null && this.periodical_thread.isAlive())
     {
       this.periodical_thread.interrupt();
       try {
         this.periodical_thread.join();
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     if(this.queue!=null)
     {
       this.queue.stop();
     }
   }
   
   public abstract void finalize();
   
 }
