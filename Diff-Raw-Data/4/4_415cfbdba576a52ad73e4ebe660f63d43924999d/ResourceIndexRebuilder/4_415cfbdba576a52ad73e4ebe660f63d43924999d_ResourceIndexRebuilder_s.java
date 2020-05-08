 package fedora.server.resourceIndex;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 import org.trippi.TriplestoreConnector;
 import org.trippi.TrippiException;
 
 import fedora.server.DummyLogging;
 import fedora.server.utilities.DDLConverter;
 import fedora.server.utilities.SQLUtility;
 import fedora.server.utilities.rebuild.*;
 import fedora.server.config.DatastoreConfiguration;
 import fedora.server.config.ModuleConfiguration;
 import fedora.server.config.Parameter;
 import fedora.server.config.ServerConfiguration;
 import fedora.server.errors.InconsistentTableSpecException;
 import fedora.server.errors.ResourceIndexException;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.types.DigitalObject;
 
 /**
  * A Rebuilder for the resource index.
  * 
  * @version $Id$
  */
 public class ResourceIndexRebuilder implements Rebuilder {
     private static final String DB_SPEC = "fedora/server/storage/resources/DefaultDOManager.dbspec";
 
     private File m_serverDir;
     private ServerConfiguration m_serverConfig;
     
     private ResourceIndex m_ri;
     private ConnectionPool m_cPool;
     private TriplestoreConnector m_conn;
     
     /**
      * Get a short phrase describing what the user can do with this rebuilder.
      */
     public String getAction() {
         return "Rebuild the Resource Index.";
     }
 
     /**
      * Returns true is the server _must_ be shut down for this 
      * rebuilder to safely operate.
      */
     public boolean shouldStopServer()
     {
         return(true);
     }
     
     /**
      * Initialize the rebuilder, given the server configuration.
      *
      * @returns a map of option names to plaintext descriptions.
      */
     public Map init(File serverDir,
                     ServerConfiguration serverConfig) {
         m_serverDir = serverDir;
         m_serverConfig = serverConfig;
         Map m = new HashMap();
 
 //        m.put("startupDelay", 
 //              "Milliseconds to delay at start of rebuild. Default is zero.");
         return m;
     }
 
     /**
      * Validate the provided options and perform any necessary startup tasks.
      */
     public void start(Map options) throws ResourceIndexException {
         // validate options
         
         // do startup tasks
         ModuleConfiguration riMC = m_serverConfig.getModuleConfiguration("fedora.server.resourceIndex.ResourceIndex");      
         int riLevel = Integer.parseInt(riMC.getParameter("level").getValue());
         String riDatastore = riMC.getParameter("datastore").getValue();
         
         ModuleConfiguration cpMC = m_serverConfig.getModuleConfiguration("fedora.server.storage.ConnectionPoolManager");
         String cpName = cpMC.getParameter("defaultPoolName").getValue();
         DatastoreConfiguration cpDC = m_serverConfig.getDatastoreConfiguration(cpName);
         
         DatastoreConfiguration tsDC = m_serverConfig.getDatastoreConfiguration(riDatastore);
         String tsConnector = tsDC.getParameter("connectorClassName").getValue();
         boolean tsRemote = Boolean.valueOf(tsDC.getParameter("remote").getValue()).booleanValue();
         if (tsRemote) {
             throw new ResourceIndexException("Rebuilder does not currently support remote triplestores.");
         }
         String tsPath = tsDC.getParameter("path").getValueAsAbsolutePath();
         
         Iterator it;
         Parameter p;
         
         Map tsTC = new HashMap();
         it = tsDC.getParameters().iterator();
         while (it.hasNext()) {
             p = (Parameter)it.next();
             tsTC.put(p.getName(), p.getValue());
         }
         
         Map aliasMap = new HashMap();
         it = riMC.getParameters().iterator();
         while (it.hasNext()) {
             p = (Parameter)it.next();
             String pName = p.getName();
             String[] parts = pName.split(":");
             if ((parts.length == 2) && (parts[0].equals("alias"))) {
                 aliasMap.put(parts[1], p.getValue());
             }
         }
 
         try {
             m_cPool = getConnectionPool(cpDC);
             deleteRITables();
         } catch (SQLException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         System.out.println("Clearing directory " + tsPath + "...");
         deleteDirectory(tsPath);
         File cleanDir = new File(tsPath);
         cleanDir.mkdir();
 
         createDBTables();
         
         
         System.out.println("Initializing triplestore interface..."); 
         try {
             m_conn = TriplestoreConnector.init(tsConnector, tsTC);
             m_ri = new ResourceIndexImpl(riLevel, m_conn, m_cPool, aliasMap, null);
         } catch (Exception e) {
             throw new ResourceIndexException("Failed to initialize new Resource Index", e);
         }
     }
 
     /**
      * Add the data of interest for the given object.
      * @throws ResourceIndexException
      */
     public void addObject(DigitalObject object) throws ResourceIndexException {
         m_ri.addDigitalObject(object);
     }
 
     /**
      * Free up any system resources associated with rebuilding.
      */
     public void finish() throws Exception {
         if (m_ri != null) m_ri.close();
     }
     
     private boolean deleteDirectory(String directory) {
 
         boolean result = false;
 
         if (directory != null) {
             File file = new File(directory);
             if (file.exists() && file.isDirectory()) {
                 // 1. delete content of directory:
                 File[] files = file.listFiles();
                 result = true; //init result flag
                 int count = files.length;
                 for (int i = 0; i < count; i++) { //for each file:
                     File f = files[i];
                     if (f.isFile()) {
                         result = result && f.delete();
                     } else if (f.isDirectory()) {
                         result = result && deleteDirectory(f.getAbsolutePath());
                     }
                 }//next file
 
                 file.delete(); //finally delete (empty) input directory
             }//else: input directory does not exist or is not a directory
         }//else: no input value
 
         return result;
     }//deleteDirectory()
     
     private void deleteRITables() throws SQLException {
         System.out.println("Dropping old database tables...");
         Connection conn = m_cPool.getConnection();
         Statement stmt = conn.createStatement();
         String[] drop = {"DROP TABLE riMethodMimeType", 
                          "DROP TABLE riMethodImpl",
                          "DROP TABLE riMethodImplBinding", 
                          "DROP TABLE riMethodPermutation", 
                          "DROP TABLE riMethod",
                          "DROP SEQUENCE RIMETHODIMPLBINDING_S1",
                          "DROP SEQUENCE RIMETHODMIMETYPE_S1",
                          "DROP SEQUENCE RIMETHODPERMUTATION_S1"};
         for (int i = 0; i < drop.length; i++) {
             try { 
                 stmt.execute(drop[i]); 
             } catch (Throwable th) {
                 if (drop[i].startsWith("DROP TABLE")) {
                     System.out.println("WARNING: Failed running '" 
                             + drop[i] + "'.  Stack trace follows.");
                     th.printStackTrace();
                 } else {
                     // skip warning, DROP SEQUENCE is Oracle-only so it's OK
                 }
             }
         }
     }
     
     private ConnectionPool getConnectionPool(DatastoreConfiguration cpDC) throws SQLException {
         String cpUsername = cpDC.getParameter("dbUsername").getValue();
         String cpPassword = cpDC.getParameter("dbPassword").getValue();
         String cpURL = cpDC.getParameter("jdbcURL").getValue();
         String cpDriver = cpDC.getParameter("jdbcDriverClass").getValue();
         String cpDDLConverter = cpDC.getParameter("ddlConverter").getValue();
         //int cpMin = Integer.parseInt(cpDC.getParameter("minPoolSize").getValue());
         //int cpMax = Integer.parseInt(cpDC.getParameter("maxPoolSize").getValue());
         int cpMaxActive = Integer.parseInt(cpDC.getParameter("maxActive").getValue());
         int cpMaxIdle = Integer.parseInt(cpDC.getParameter("maxIdle").getValue());
         long cpMaxWait = Long.parseLong(cpDC.getParameter("maxWait").getValue()); 
         int cpMinIdle = Integer.parseInt(cpDC.getParameter("minIdle").getValue());
         long cpMinEvictableIdleTimeMillis = Long.parseLong(cpDC.getParameter("minEvictableIdleTimeMillis").getValue());
         int cpNumTestsPerEvictionRun = Integer.parseInt(cpDC.getParameter("numTestsPerEvictionRun").getValue());
         long cpTimeBetweenEvictionRunsMillis = Long.parseLong(cpDC.getParameter("timeBetweenEvictionRunsMillis").getValue());
         boolean cpTestOnBorrow = Boolean.getBoolean(cpDC.getParameter("testOnBorrow").getValue());
         boolean cpTestOnReturn = Boolean.getBoolean(cpDC.getParameter("testOnReturn").getValue());
         boolean cpTestWhileIdle = Boolean.getBoolean(cpDC.getParameter("testWhileIdle").getValue());
         byte cpWhenExhaustedAction = Byte.parseByte(cpDC.getParameter("whenExhaustedAction").getValue());
         
         DDLConverter ddlConverter = null;
         if (cpDDLConverter != null) {
             try {
                 ddlConverter=(DDLConverter) Class.forName(cpDDLConverter).newInstance();
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
         }
         return new ConnectionPool(cpDriver, cpURL, cpUsername, 
                 cpPassword, ddlConverter, cpMaxActive, cpMaxIdle, 
                 cpMaxWait, cpMinIdle, cpMinEvictableIdleTimeMillis, 
                 cpNumTestsPerEvictionRun, cpTimeBetweenEvictionRunsMillis, 
                 cpTestOnBorrow, cpTestOnReturn, cpTestWhileIdle, cpWhenExhaustedAction);
     }
     
     private void createDBTables() {
         System.out.println("Creating clean database tables...");
         InputStream specIn;
         
         try {
             specIn = getClass().getClassLoader().getResourceAsStream(DB_SPEC);
             if (specIn == null) {
                 throw new IOException("Cannot find required resource in classpath: " + DB_SPEC);
             }
     
             SQLUtility.createNonExistingTables(m_cPool, specIn, new DummyLogging());
         } catch (SQLException s) {
             s.printStackTrace();
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InconsistentTableSpecException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 }
