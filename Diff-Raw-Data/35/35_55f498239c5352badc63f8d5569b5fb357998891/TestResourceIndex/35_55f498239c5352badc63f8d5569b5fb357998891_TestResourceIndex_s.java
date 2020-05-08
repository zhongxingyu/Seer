 package fedora.server.resourceIndex;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.util.Map;
 
 import org.trippi.RDFFormat;
 import org.trippi.TriplestoreConnector;
 
 import fedora.common.Constants;
 import fedora.server.Parameterized;
 import fedora.server.TestLogging;
 import fedora.server.storage.ConnectionPool;
 import fedora.server.storage.translation.FOXMLDODeserializer;
 import fedora.server.storage.translation.METSLikeDODeserializer;
 import fedora.server.storage.types.BasicDigitalObject;
 import fedora.server.storage.types.DigitalObject;
 import fedora.server.utilities.ConfigurationLoader;
 import fedora.server.utilities.DDLConverter;
 import fedora.server.utilities.SQLUtility;
 import junit.framework.TestCase;
 
 /**
  * @author Edwin Shin
  */
 public abstract class TestResourceIndex extends TestCase implements Constants {
     protected static final String DEMO_OBJECTS_ROOT_DIR = "src/test/junit/foxmlTestObjects";
     
     protected static String m_triplestorePath;
     protected static String m_fedoraHome = System.getProperty("fedora.home");
     protected ResourceIndex m_ri;
     protected ConnectionPool m_cPool;
     protected TriplestoreConnector m_conn;
 
     public static void main(String[] args) {
         junit.textui.TestRunner.run(TestResourceIndexImpl.class);
     }
 
     /*
      * @see TestCase#setUp()
      */
     protected void setUp() throws Exception {
         if (m_fedoraHome == null || m_fedoraHome.equals("")) {
             m_fedoraHome = "dist";
         }
         
         ConfigurationLoader cl = new ConfigurationLoader(m_fedoraHome, "test.fcfg"); 
         
         Map riMP = cl.getModuleParameters("fedora.server.resourceIndex.ResourceIndex");
         int level = Integer.parseInt((String)riMP.get("level"));
         String datastore = (String)riMP.get("datastore");
         
         Map cpMP = cl.getModuleParameters("fedora.server.storage.ConnectionPoolManager");
         String cPoolName = (String)cpMP.get("defaultPoolName");
         
         Parameterized cpConf = cl.getDatastoreConfig(cPoolName);
         Map cpP = cpConf.getParameters();
         String cpUserName = (String)cpP.get("dbUsername");
         String cpPassword = (String)cpP.get("dbPassword");
         String cpURL = (String)cpP.get("jdbcURL");
         String cpDriver = (String)cpP.get("jdbcDriverClass");
         String cpDDLConverter = (String)cpP.get("ddlConverter");
         int cpIConn = Integer.parseInt((String)cpP.get("minPoolSize"));
         int cpMaxConn = Integer.parseInt((String)cpP.get("maxPoolSize"));
         DDLConverter ddlConverter=null;
         if (cpDDLConverter != null) {
             ddlConverter=(DDLConverter) Class.forName(cpDDLConverter).newInstance();
         }
         
         m_cPool = new ConnectionPool(cpDriver, cpURL, cpUserName, 
                                      cpPassword, cpIConn, cpMaxConn, true, ddlConverter);
         
         String dbSpec="src/dbspec/server/fedora/server/storage/resources/DefaultDOManager.dbspec";
         InputStream specIn = new FileInputStream(dbSpec);
         if (specIn==null) {
             throw new IOException("Cannot find required "
                 + "resource: " + dbSpec);
         }
         SQLUtility.createNonExistingTables(m_cPool, specIn, new TestLogging());
         
         Parameterized tsConf = cl.getDatastoreConfig(datastore);
         Map tsP = tsConf.getParameters();
         String connectorClassName = (String) tsP.get("connectorClassName");
         m_triplestorePath = (String)tsP.get("path");
         
         m_conn = TriplestoreConnector.init(connectorClassName, tsP);
         
        m_ri = new ResourceIndexImpl(level, m_conn, m_cPool, null);
         
        Map aliasMap = m_conn.getReader().getAliasMap();
        aliasMap.put("nsdl", "http://www.nsdl.org/ontologies/relationships#");
        m_conn.getReader().setAliasMap(aliasMap);
 
         // needed by the deserializer
         System.setProperty("fedoraServerHost", "localhost");
         System.setProperty("fedoraServerPort", "8080");
     }
 
     /*
      * @see TestCase#tearDown()
      */
     protected void tearDown() throws Exception {
         if (m_conn != null) {
             m_conn.close();
             m_conn = null;
         }
         if (m_cPool != null) {
             deleteRITables();
             m_cPool.closeAllConnections();
             m_cPool = null;
         }
         m_ri = null;
         deleteDirectory(m_triplestorePath);
     }
     
     protected void addDigitalObject(File file) throws Exception {
         FileInputStream in;
         DigitalObject obj = new BasicDigitalObject();
 
         in = new FileInputStream(file);
         FOXMLDODeserializer deser = new FOXMLDODeserializer();
         deser.deserialize(in, obj, "UTF-8", 0);
         addDigitalObject(obj);
     }
 
     protected void addDigitalObject(DigitalObject digitalObject) throws Exception {
         m_ri.addDigitalObject(digitalObject);
     }
     
     protected void addDigitalObjects(File dir) throws Exception {
         FilenameFilter filter = new FilenameFilter() {
             public boolean accept(File file, String name) {
                 return (!name.startsWith(".") && name.endsWith(".xml"));
             }
         };
         File[] files = dir.listFiles(filter);
         for (int i = 0; i < files.length; i++) {
             addDigitalObject(files[i]);
         }
     }
     
     protected boolean deleteDirectory(String directory) {
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
     
     protected void deleteRITables() throws Exception {
         Connection conn = m_cPool.getConnection();
         Statement stmt = conn.createStatement();
         String[] drop = {"DROP TABLE riMethodMimeType", "DROP TABLE riMethodImpl", 
                          "DROP TABLE riMethodPermutation", "DROP TABLE riMethod"};
         for (int i = 0; i < drop.length; i++) {
             stmt.execute(drop[i]);
         }
     }
 
     protected DigitalObject getFoxmlObject(File file) throws Exception {
         FileInputStream in;
         DigitalObject obj = new BasicDigitalObject();
 
         in = new FileInputStream(file);
         FOXMLDODeserializer deser = new FOXMLDODeserializer();
         deser.deserialize(in, obj, "UTF-8", 0);
         return obj;
     }
     
     protected DigitalObject getMetsObject(File file) throws Exception {
         FileInputStream in;
         DigitalObject obj = new BasicDigitalObject();
 
         in = new FileInputStream(file);
         METSLikeDODeserializer deser = new METSLikeDODeserializer();
         deser.deserialize(in, obj, "UTF-8", 0);
         return obj;
     }
     
     protected void export(String path) throws Exception {
         m_ri.export(new FileOutputStream(path), RDFFormat.RDF_XML);
     }
 }
