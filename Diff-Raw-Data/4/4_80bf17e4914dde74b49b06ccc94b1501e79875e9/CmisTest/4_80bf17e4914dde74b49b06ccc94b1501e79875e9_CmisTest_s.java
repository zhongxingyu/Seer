 package org.nuxeo.jmeter.cmis;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.jmeter.tools.RandomTextGenerator;
 
 /**
  * Unit test for simple App.
  */
 public class CmisTest extends TestCase {
 
     private static Log log = LogFactory.getLog(CmisTest.class);
 
     private static final String BASE_URL = "http://localhost:8080/nuxeo/atom/cmis";
 
     private static final String USERNAME = "Administrator";
 
     private static final String PASSWORD = "Administrator";
 
     private CmisClient cmis;
 
     private String baseFolderName;
 
     private String threadNum;
 
     private Integer sizeKB = 8;
 
     private boolean fromJmeter = false;
 
     private RandomTextGenerator gen;
 
     private static ThreadLocal<Integer> folderNum = new ThreadLocal<Integer>() {
         @Override
         protected Integer initialValue() {
             return 0;
         }
     };
 
     public CmisTest() {
         super();
     }
 
     /**
      * Jmeter create a test case for each sampler and each thread
      *
      * @throws Exception
      *
      * @params submited by jmeter
      */
     public CmisTest(String params) throws Exception {
         super(params);
         Map<String, String> map = getParamMap(params);
         String username = USERNAME;
         if (map.containsKey("username")) {
             username = map.get("username");
         }
         String password = PASSWORD;
         if (map.containsKey("password")) {
             password = map.get("password");
         }
         String baseUrl = BASE_URL;
         if (map.containsKey("base_url")) {
             baseUrl = map.get("base_url");
         }
         if (map.containsKey("thread_num")) {
             threadNum = map.get("thread_num");
             fromJmeter = true;
         }
         if (map.containsKey("folder_path")) {
             baseFolderName = map.get("folder_path");
             if (threadNum != null) {
                 baseFolderName = baseFolderName + "-" + threadNum;
             }
         } else {
             baseFolderName = "fd-" + System.currentTimeMillis();
         }
         if (map.containsKey("size_kb")) {
             String value = map.get("size_kb");
             sizeKB = Integer.parseInt(value);
             if (log.isDebugEnabled()) {
                 log.debug("Loading dico...");
             }
             gen = new RandomTextGenerator();
             gen.prefilCache();
             log.info("Dico loaded");
        } else {
            baseFolderName = "fd-" + System.currentTimeMillis();
         }
         // log.info("__init_ map : " + map.toString());
         log.info("Create CMIS session for thread: " + threadNum
                 + ", baseFolderName: " + baseFolderName);
         cmis = new CmisClient(username, password, baseUrl);
     }
 
     private String getFolderName() {
         return baseFolderName + "-" + folderNum.get().toString();
     }
 
     private String getFolderPath() {
         return "/" + getFolderName();
     }
 
     private String getNextFolderName() {
         Integer next = folderNum.get() + 1;
         return baseFolderName + "-" + next.toString();
     }
 
     public static Map<String, String> getParamMap(String params) {
         String[] items = params.split(",");
         Map<String, String> map = new HashMap<String, String>();
         for (String item : items) {
             if (item.indexOf('=') == -1) {
                 continue;
             }
             String name = item.split("=")[0];
             String value = item.split("=")[1];
             map.put(name, value);
         }
         return map;
     }
 
     // setUp and tearDown must be public to be used by JMeter
     @Override
     public void setUp() throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("setUp");
         }
         super.setUp();
     }
 
     @Override
     public void tearDown() throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("tearDown");
         }
         super.tearDown();
     }
 
     // ---------------------------------------------------------------------
     // test
     public void testCreateFolder() {
         String name = getNextFolderName();
         String root = cmis.getRootFolder();
         if (log.isDebugEnabled()) {
             log.debug("creatingFolder " + name);
         }
         String ret = cmis.createFolder(root, name);
         folderNum.set(folderNum.get() + 1);
         assertEquals(ret, getFolderPath());
     }
 
     /**
      * Create a document in the previously created folder.
      *
      * Note that the jmeter test runner keep the same test case instance between
      * test, so folderPath is not null.
      * @throws Exception
      */
     public void testCreateDocument() throws Exception {
         if (!fromJmeter) {
             gen = new RandomTextGenerator();
             gen.prefilCache();
             log.info("Dico loaded");
             testCreateFolder();
         }
         String docName = "doc-" + System.currentTimeMillis();
         String content = gen.getRandomText(sizeKB);
         String docPath = cmis.createDocument(getFolderPath(), docName, content.getBytes());
         if (log.isDebugEnabled()) {
             log.debug("createDocument " + docPath);
         }
         assertEquals(docPath, getFolderPath() + "/" + docName);
     }
 
     public void testGetChildren() throws Exception {
         if (!fromJmeter) {
            testCreateFolder();
             testCreateDocument();
         }
         int count = cmis.getChildren(getFolderPath());
         if (log.isDebugEnabled()) {
             log.debug("count " + Integer.valueOf(count).toString());
         }
         assertTrue(count > 0);
     }
 
 }
