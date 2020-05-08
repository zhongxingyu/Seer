 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eu.ac.susx.mlcl.erl.t9kb;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
import java.util.logging.Logger;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.apache.commons.lang.time.StopWatch;
 import org.mapdb.DB;
 import org.mapdb.DBMaker;
 import org.mapdb.HTreeMap;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  *
  * @author hiam20
  */
 public class T9KnowledgeBase {
 
    private static final Logger LOG = Logger.getLogger(T9KnowledgeBase.class.getName());
     private final DB db;
     private final HTreeMap<String, T9Entity> idIndex;
     private final HTreeMap<String, String> nameIndex;
 
     private T9KnowledgeBase(DB db, HTreeMap<String, T9Entity> idIndex, HTreeMap<String, String> nameIndex) {
         this.db = db;
         this.idIndex = idIndex;
         this.nameIndex = nameIndex;
     }
     
     private void checkState() throws IOException {
         if(db.isClosed())
             throw new IOException("The database is closed.");
     }
 
     public T9Entity getEntityById(String id) throws IOException {
         checkState();
         return idIndex.get(id);
     }
 
     public T9Entity getEntityByName(String name) throws IOException {
         checkState();
         return idIndex.get(getNameById(name));
     }
 
     public String getNameById(String name) throws IOException {
         checkState();
         return nameIndex.get(name);
     }
 
     public String getTextForId(String id) throws IOException {
         return getEntityById(id).getWikiText().orNull();
     }
 
     public void close() {
         db.commit();
         db.close();
     }
 
     private static DB openDB(File dbFile) {
         return DBMaker.newFileDB(dbFile)
                 .asyncWriteDisable()
                 .journalDisable()
                 .closeOnJvmShutdown()
                 .make();
     }
 
     public static T9KnowledgeBase open(File dbFile) {
         final DB db = openDB(dbFile);
         final HTreeMap<String, T9Entity> idIndex = db.getHashMap("entity-id-index");
         final HTreeMap<String, String> nameIndex = db.getHashMap("entity-name-index");
         return new T9KnowledgeBase(db, idIndex, nameIndex);
     }
 
     /**
      * Create a new knowledge base from the given raw source XML file(s).
      *
      * Path can be a single file, or a directory in which case every file inside is parsed.
      *
      * @param dbFile
      * @param dataPath
      * @return
      * @throws ParserConfigurationException
      * @throws SAXException
      * @throws IOException
      */
     public static T9KnowledgeBase create(File dbFile, File dataPath) throws ParserConfigurationException, SAXException, IOException {
         final DB db = openDB(dbFile);
         final HTreeMap<String, T9Entity> idIndex = db.createHashMap("entity-id-index", null, null);
         final HTreeMap<String, String> nameIndex = db.createHashMap("entity-name-index", null, null);
         final SAXParserFactory saxFactory = SAXParserFactory.newInstance();
         final SAXParser saxParser = saxFactory.newSAXParser();
         final DefaultHandler handler = new T9SaxHandler(new TacEntryHandler() {
             private int count = 0;
             private StopWatch tic = new StopWatch();
 
             {
                 tic.start();
             }
 
             @Override
             public void entry(T9Entity entry) {
                 idIndex.put(entry.getId(), entry);
                 nameIndex.put(entry.getName(), entry.getId());
                 if (count % 1000 == 0) {
                     db.commit();
                 }
                 if (count % 10000 == 0) {
                     LOG.info(String.format("Processed %d entities. (%f e/s)%n", count,
                                       count / ((tic.getTime() / 1000.0))));
                 }
                 count++;
             }
         });
 
 
         if (!dataPath.exists()) {
             throw new IOException("Data path does not exist: " + dataPath);
         } else if (!dataPath.canRead()) {
             throw new IOException("Data path is not readable: " + dataPath);
         }
 
         final File[] parts;
         if (dataPath.isDirectory()) {
             LOG.info("Parsing all files in directory: " + dataPath);
             parts = dataPath.listFiles(new FilenameFilter() {
                 @Override
                 public boolean accept(File dir, String name) {
                     return name.matches("kb_part-\\d+\\.xml");
                 }
             });
         } else {
             LOG.info("Parsing single file: " + dataPath);
             parts = new File[]{dataPath};
         }
 
         for (File part : parts) {
             LOG.info("Processing file: " + part);
             saxParser.parse(part, handler);
         }
         
         db.commit();
         
         return new T9KnowledgeBase(db, idIndex, nameIndex);
     }
 }
