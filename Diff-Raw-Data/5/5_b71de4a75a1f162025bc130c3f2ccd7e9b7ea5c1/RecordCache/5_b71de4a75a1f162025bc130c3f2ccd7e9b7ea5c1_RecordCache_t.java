 package proai.cache;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.*;
 
 import net.sf.bvalid.SchemaLanguage;
 import net.sf.bvalid.Validator;
 import net.sf.bvalid.ValidatorFactory;
 import net.sf.bvalid.ValidatorOption;
 import net.sf.bvalid.catalog.DiskSchemaCatalog;
 import net.sf.bvalid.catalog.FileSchemaIndex;
 import net.sf.bvalid.catalog.MemorySchemaCatalog;
 import net.sf.bvalid.catalog.SchemaCatalog;
 import net.sf.bvalid.catalog.SchemaIndex;
 import net.sf.bvalid.locator.SchemaLocator;
 import net.sf.bvalid.locator.CachingSchemaLocator;
 import net.sf.bvalid.locator.URLSchemaLocator;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbcp.BasicDataSourceFactory;
 
 import org.apache.log4j.Logger;
 
 import proai.CloseableIterator;
 import proai.MetadataFormat;
 import proai.SetInfo;
 import proai.Writable;
 import proai.driver.OAIDriver;
 import proai.error.ServerException;
 import proai.util.DDLConverter;
 import proai.util.StreamUtil;
 
 /**
  * Main application interface for working with items in the cache,
  * whether in the database or on disk.
  *
  * @author Chris Wilper
  */
 public class RecordCache extends Thread {
 
     public static final String OAI_RECORD_SCHEMA_URL 
             = "http://proai.sourceforge.net/schemas/OAI-PMH-record.xsd";
 
     public static final String[] EXAMPLE_SCHEMAS 
             = new String[] { "sample.xsd", "test_format.xsd", "my-about.xsd",
                              "formatX.xsd", "formatY.xsd" };
 
     private static final Logger logger =
             Logger.getLogger(RecordCache.class.getName());
 
     private static final String propMissing = "Required property missing: ";
 
     private static final String pfx          = "proai.";
     private static final String dbpfx        = pfx + "db.";
     private static final String dbconnpfx    = dbpfx + "connection.";
 
     public static final String PROP_BASEDIR            = pfx + "cacheBaseDir";
     public static final String PROP_OAIDRIVERCLASSNAME = pfx + "driverClassName";
     public static final String PROP_POLLSECONDS        = pfx + "driverPollSeconds";
     public static final String PROP_POLLINGENABLED     = pfx + "driverPollingEnabled";
     public static final String PROP_MAXWORKERS         = pfx + "maxWorkers";
     public static final String PROP_MAXWORKBATCHSIZE   = pfx + "maxWorkBatchSize";
     public static final String PROP_MAXFAILEDRETRIES   = pfx + "maxFailedRetries";
     public static final String PROP_MAXCOMMITQUEUESIZE = pfx + "maxCommitQueueSize";
     public static final String PROP_MAXRECORDSPERTRANS = pfx + "maxRecordsPerTransaction";
     public static final String PROP_SCHEMADIR          = pfx + "schemaDir";
     public static final String PROP_VALIDATEUPDATES    = pfx + "validateUpdates";
 
     public static final String PROP_DB_URL             = dbpfx + "url";
     public static final String PROP_DB_DRIVERCLASSNAME = dbpfx + "driverClassName";
     public static final String PROP_DB_MYSQL_TRICKLING = dbpfx + "mySQLResultTrickling";
     public static final String PROP_DB_USERNAME        = dbpfx + "username";
     public static final String PROP_DB_PASSWORD        = dbpfx + "password";
 
     private static BasicDataSource s_pool;
 
     private Updater m_updater;
     private OAIDriver m_driver;
     private File m_baseDir;
 
     private RCDatabase m_rcdb;
     private RCDisk m_rcDisk;
 
     public RecordCache(Properties props) throws ServerException {
 
         String baseDir            = getRequiredParam(props, PROP_BASEDIR);
         String oaiDriverClassName = getRequiredParam(props, PROP_OAIDRIVERCLASSNAME);
         String dbDriverClassName  = getRequiredParam(props, PROP_DB_DRIVERCLASSNAME);
 
         boolean mySQLTrickling = false;
         String mt = props.getProperty(PROP_DB_MYSQL_TRICKLING);
         if (mt != null && mt.equalsIgnoreCase("true")) {
             mySQLTrickling = true;
         }
 
         OAIDriver driver;
         try {
             driver = (OAIDriver) Class.forName(oaiDriverClassName).newInstance();
         } catch (Exception e) {
             throw new ServerException("Unable to initialize OAIDriver: " 
                     + oaiDriverClassName, e);
         }
         driver.init(props);
 
         int pollSecondsInt = getRequiredInt(props, PROP_POLLSECONDS, 1, Integer.MAX_VALUE);
         boolean pollingEnabled = getRequiredParam(props, PROP_POLLINGENABLED).equalsIgnoreCase("true");
         int maxWorkers = getRequiredInt(props, PROP_MAXWORKERS, 1, Integer.MAX_VALUE);
         int maxWorkBatchSize = getRequiredInt(props, PROP_MAXWORKBATCHSIZE, 1, Integer.MAX_VALUE);
         int maxFailedRetries = getRequiredInt(props, PROP_MAXFAILEDRETRIES, 0, Integer.MAX_VALUE);
         int maxCommitQueueSize = getRequiredInt(props, PROP_MAXCOMMITQUEUESIZE, 1, Integer.MAX_VALUE);
         int maxRecordsPerTransaction = getRequiredInt(props, PROP_MAXRECORDSPERTRANS, 1, Integer.MAX_VALUE);
 
         logger.info("Initializing database connection pool...");
         BasicDataSource pool;
         try {
             Class.forName(dbDriverClassName);
             pool = (BasicDataSource) 
                  BasicDataSourceFactory
                  .createDataSource(getDBProperties(props, false));
             pool.setDriverClassName(dbDriverClassName);
             Properties connProps = getDBProperties(props, true);
             Enumeration<?> e = connProps.propertyNames();
             while (e.hasMoreElements()) {
                 String name = (String) e.nextElement();
                 pool.addConnectionProperty(name, (String) connProps.getProperty(name));
             }
         } catch (Exception e) {
             throw new ServerException("Unable to initialize DataSource", e);
         }
 
         DDLConverter ddlc = null;
         try {
             String ddlcProp = dbDriverClassName + ".ddlConverter";
             String ddlcClassName = getRequiredParam(props, ddlcProp);
             ddlc = (DDLConverter) Class.forName(ddlcClassName).newInstance();
         } catch (Exception e) {
             throw new ServerException("Unable to initialize DDLConverter", e);
         }
 
         boolean backslashIsEscape = true;
         String s = props.getProperty(dbDriverClassName + ".backslashIsEscape");
         if (s != null && s.trim().equalsIgnoreCase("false")) {
             backslashIsEscape = false;
         }
 
         File schemaDir = null;
         boolean validateUpdates = true;
         String vu = props.getProperty(PROP_VALIDATEUPDATES);
         if (vu != null && vu.equalsIgnoreCase("false")) {
             validateUpdates = false;
         } else {
             schemaDir = new File(getRequiredParam(props, PROP_SCHEMADIR));
         }
 
         init(pool, 
              ddlc, 
              mySQLTrickling,
              backslashIsEscape, 
              pollingEnabled,
              driver, 
              pollSecondsInt, 
              new File(baseDir),
              maxWorkers,
              maxWorkBatchSize,
              maxFailedRetries,
              maxCommitQueueSize,
              maxRecordsPerTransaction,
              validateUpdates,
              schemaDir);
     }
 
     public RecordCache(BasicDataSource pool,
                        DDLConverter ddlc,
                        boolean mySQLTrickling,
                        boolean backslashIsEscape,
                        boolean pollingEnabled,
                        OAIDriver driver,
                        int pollSeconds,
                        File baseDir,
                        int maxWorkers,
                        int maxWorkBatchSize,
                        int maxFailedRetries,
                        int maxCommitQueueSize,
                        int maxRecordsPerTransaction,
                        boolean validateUpdates,
                        File schemaDir) throws ServerException {
         init(pool, 
              ddlc, 
              mySQLTrickling,
              backslashIsEscape, 
              pollingEnabled,
              driver, 
              pollSeconds,
              baseDir, 
              maxWorkers, 
              maxWorkBatchSize, 
              maxFailedRetries, 
              maxCommitQueueSize, 
              maxRecordsPerTransaction,
              validateUpdates,
              schemaDir);
     }
 
     private void init(BasicDataSource pool,
                       DDLConverter ddlc,
                       boolean mySQLTrickling,
                       boolean backslashIsEscape,
                       boolean pollingEnabled,
                       OAIDriver driver,
                       int pollSeconds,
                       File baseDir,
                       int maxWorkers,
                       int maxWorkBatchSize,
                       int maxFailedRetries,
                       int maxCommitQueueSize,
                       int maxRecordsPerTransaction,
                       boolean validateUpdates,
                       File schemaDir) throws ServerException {
 
         logger.info("Initializing Record Cache...");
 
         s_pool = pool;
         m_driver = driver;
         m_baseDir = baseDir;
 
         // this creates baseDir if it doesn't exist yet
         m_rcDisk = new RCDisk(m_baseDir);
         logger.debug("Record Cache Initialized");
 
         // init RCDatabase (creates tables if needed)
         Connection conn = null;
         try {
             conn = getConnection();
             m_rcdb = new RCDatabase(conn, ddlc, mySQLTrickling, backslashIsEscape, pollingEnabled, m_rcDisk);
         } catch (SQLException e) {
             throw new ServerException("Database connection problem", e);
         } finally {
             releaseConnection(conn);
         }
 
         // initialize the validator if needed
         Validator validator = null;
         if (validateUpdates) {
 
             // make sure schemaDir exists
             if (!schemaDir.exists()) {
                 schemaDir.mkdirs();
                 if (!schemaDir.exists()) {
                     throw new ServerException("Cannot create schema dir: " 
                             + schemaDir.getPath());
                 }
             }
 
             Map<ValidatorOption, String> opts = new HashMap<ValidatorOption, String>();
             opts.put(ValidatorOption.CACHE_PARSED_GRAMMARS, "true");
             try {
                 validator = ValidatorFactory.getValidator(SchemaLanguage.XSD,
                                                           createLocator(schemaDir),
                                                           opts);
             } catch (Exception e) {
                 throw new ServerException("Unable to initialize schema "
                         + "validator", e);
             }
         }
 
         // finally, start the Updater thread
         m_updater = new Updater(m_driver, 
                                 this, 
                                 m_rcdb, 
                                 m_rcDisk,
                                 pollSeconds,
                                 maxWorkers,
                                 maxWorkBatchSize, 
                                 maxFailedRetries,  
                                 maxCommitQueueSize, 
                                 maxRecordsPerTransaction,
                                 validator);
         m_updater.start();
     }
 
     private SchemaLocator createLocator(File schemaDir) throws Exception {
 
         SchemaIndex index = new FileSchemaIndex(new File(schemaDir, 
                                                          "index.dat"));
         SchemaCatalog cacheCatalog = new DiskSchemaCatalog(index, schemaDir);
 
         // if not already there, add predefined schemas to cache catalog
         addToCatalog(cacheCatalog, OAI_RECORD_SCHEMA_URL, "schemas/OAI-PMH-record.xsd");
         for (int i = 0; i < EXAMPLE_SCHEMAS.length; i++) {
             addToCatalog(cacheCatalog, 
                          "http://example.org/" + EXAMPLE_SCHEMAS[i],
                          "schemas/" + EXAMPLE_SCHEMAS[i]);
         }
 
         return new CachingSchemaLocator(new MemorySchemaCatalog(),
                                         cacheCatalog,
                                         new URLSchemaLocator());
     }
 
     private static void addToCatalog(SchemaCatalog catalog, String url, String path) throws Exception {
 
         if (!catalog.contains(url)) {
             InputStream in = null;
             try {
                 in = RecordCache.class.getClassLoader().getResourceAsStream(path);
             } catch (Throwable th) {
                 in = ClassLoader.getSystemResourceAsStream(path);
             }
             catalog.put(url, in);
         }
     }
 
     /**
      * Get a connection from the pool.
      */
     protected static Connection getConnection() throws SQLException {
         if (s_pool == null) {
             throw new RuntimeException("RecordCache has not been constructed "
                     + "so the db connection pool does not exist!");
         }
         long startTime = System.currentTimeMillis();
         Connection conn = s_pool.getConnection(); // may block
         if (logger.isDebugEnabled()) {
             long delay = System.currentTimeMillis() - startTime;
             logger.debug("Got db connection from pool after " + delay 
                     + "ms.  Now idle = " + s_pool.getNumIdle() 
                     + " and active = " + s_pool.getNumActive());
         }
         return conn;
     }
 
     protected static void releaseConnection(Connection conn) {
         if (s_pool == null) {
             throw new RuntimeException("RecordCache has not been constructed "
                     + "so the db connection pool does not exist!");
         }
         if (conn != null) {
             try {
                 conn.close();
                 if (logger.isDebugEnabled()) {
                     logger.debug("Released db connection to pool.  Now idle = " 
                             + s_pool.getNumIdle() + " and active = "
                             + s_pool.getNumActive());
                 }
             } catch (Throwable th) {
                 logger.warn("Unable to release db connection to pool", th);
             }
         }
     }
 
     private static String getRequiredParam(Properties props, 
                                            String propName) throws ServerException {
         String val = props.getProperty(propName);
         if (val == null) {
             throw new ServerException(propMissing + propName);
         } else {
             logger.debug("Got required property: " + propName + " = " + val);
         }
         return val.trim();
     }
 
     private static int getRequiredInt(Properties props,
                                       String propName,
                                       int minValue,
                                       int maxValue) throws ServerException {
         String val = getRequiredParam(props, propName);
         try {
             int intVal = Integer.parseInt(val);
             if (intVal < minValue) {
                 throw new ServerException("Bad value for " + propName + ": smallest valid value is " + minValue);
             }
             if (intVal > maxValue) {
                 throw new ServerException("Bad value for " + propName + ": largest valid value is " + minValue);
             }
             return intVal;
         } catch (NumberFormatException nfe) {
             throw new ServerException("Bad value for " + propName + ": must be an integer");
         }
     }
 
     private static final Properties getDBProperties(Properties props, 
                                                     boolean conn) {
         Properties dbProps = new Properties();
         Enumeration<?> e = props.propertyNames();
         while (e.hasMoreElements()) {
             String name = (String) e.nextElement();
             if (name.startsWith(dbpfx)) {
                 String value = props.getProperty(name);
                 if (name.startsWith(dbconnpfx)) {
                     if (conn) {
                         String newPropName = name.substring(dbconnpfx.length());
                         logger.debug("Set per-connection property: " + newPropName + " = " + value);
                         dbProps.setProperty(newPropName, value);
                     }
                 } else {
                     if (!conn) {
                         String newPropName = name.substring(dbpfx.length());
                         logger.debug("Set connection pool property: " + newPropName + " = " + value);
                         dbProps.setProperty(newPropName, value);
                     }
                 }
             }
         }
         return dbProps;
     }
 
     //////////////////////////////////////////////////////////////////////////
 
     public File getFile(String cachePath) {
         return m_rcDisk.getFile(cachePath);
     }
 
     //////////////////////////////////////////////////////////////////////////
 
     /**
      * Return the specified record, or null if it doesn't exist.
      */
     public Writable getRecordContent(String identifier,
                                      String metadataPrefix) 
             throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             String[] info = m_rcdb.getRecordInfo(conn, identifier, metadataPrefix);
             if (info == null) return null;
             return new WritableWrapper("<GetRecord>\n", 
                                        m_rcDisk.getContent(info[0], info[1], false),
                                        "\n</GetRecord>");
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     public Writable getIdentifyContent() throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             String path = m_rcdb.getIdentifyPath(conn);
             if (path == null) {
                 throw new ServerException("Identify.xml does not yet exist in the cache");
             }
             return m_rcDisk.getContent(path);
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     public Writable getMetadataFormatsContent(String identifier) throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             List<CachedMetadataFormat> formats = m_rcdb.getFormats(conn, identifier);
             if (identifier != null && formats.size() == 0) return null;
             return new CachedContent(getFormatsXMLString(formats));
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     public CloseableIterator<SetInfo> getSetInfoContent() throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             List<SetInfo> list = m_rcdb.getSetInfo(conn);
             CloseableIterator<SetInfo> setInfo = new proai.driver.impl.RemoteIteratorImpl<SetInfo>(list.iterator());
             return setInfo;
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     public CloseableIterator<String[]> getSetInfoPaths() throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             List<String[]> list = m_rcdb.getSetInfoPaths(conn);
             CloseableIterator<String[]> setInfo = new proai.driver.impl.RemoteIteratorImpl<String[]>(list.iterator());
             return setInfo;
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     public CloseableIterator<CachedContent> getRecordsContent(Date from,
                                                Date until,
                                                String prefix,
                                                String set,
                                                boolean identifiers) throws ServerException {
         if (until == null) {
             // If given as null, use the current date as from date.
             // This is done so that records with dates after the request date
             // are not returned by the query.
             until = StreamUtil.nowUTC();
         }
         try {
             return new CachedRecordContentIterator(
                                m_rcdb.findRecordInfo(getConnection(), 
                                                       from, 
                                                       until, 
                                                       prefix, 
                                                       set),
                                m_rcDisk,
                                identifiers);
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         }
     }
 
     public CloseableIterator<String[]> getRecordsPaths(Date from,
                                              Date until,
                                              String prefix,
                                              String set) throws ServerException {
         if (until == null) {
             // If given as null, use the current date as from date.
             // This is done so that records with dates after the request date
             // are not returned by the query.
             until = StreamUtil.nowUTC();
         }
         try {
             return m_rcdb.findRecordInfo(getConnection(), 
                                          from, 
                                          until, 
                                          prefix, 
                                          set);
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         }
     }
 
     public boolean formatExists(String mdPrefix) throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             Iterator<CachedMetadataFormat> iter = m_rcdb.getFormats(conn).iterator();
             while (iter.hasNext()) {
                 MetadataFormat fmt = iter.next();
                 if (fmt.getPrefix().equals(mdPrefix)) return true;
             }
             return false;
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     private String getFormatsXMLString(List<? extends MetadataFormat> formats) {
         StringBuffer buf = new StringBuffer();
         buf.append("<ListMetadataFormats>\n");
         Iterator<? extends MetadataFormat> iter = formats.iterator();
         while (iter.hasNext()) {
             MetadataFormat fmt = iter.next();
             buf.append("  <metadataFormat>\n");
             buf.append("    <metadataPrefix>" + fmt.getPrefix() + "</metadataPrefix>\n");
             buf.append("    <schema>" + fmt.getSchemaLocation() + "</schema>\n");
             buf.append("    <metadataNamespace>" + fmt.getNamespaceURI() + "</metadataNamespace>\n");
             buf.append("  </metadataFormat>\n");
         }
         buf.append("</ListMetadataFormats>");
         return buf.toString();
     }
 
     public boolean itemExists(String identifier) throws ServerException {
         Connection conn = null;
         try {
             conn = getConnection();
             return m_rcdb.itemExists(conn, identifier);
         } catch (SQLException e) {
             throw new ServerException("Error getting a database connection", e);
         } finally {
             releaseConnection(conn);
         }
     }
 
     //////////////////////////////////////////////////////////////////////////
 
     public void close() throws ServerException {
 
         if (s_pool != null) {  // if it's not already closed
 
             m_updater.shutdown(true);
 
             // shut down db pool, etc.
             try {
                 s_pool.close();
                 s_pool = null;
             } catch (Exception e) {
                 throw new ServerException("Error closing DataSource", e);
             }
             logger.info("RecordCache shutdown complete.");
         }
     }
 
     /**
      * Ensure close has occurred at GC-time.
      */
     public void finalize() throws ServerException {
         close();
     }
 
 }
