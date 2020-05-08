 package edu.psu.ist.youseer;
 
 import java.io.IOException;
 
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 import java.io.*;
 import org.apache.solr.analysis.HTMLStripReader;
 import java.util.regex.*;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.apache.commons.lang.*;
 import net.htmlparser.jericho.*;
 
 
 import org.archive.io.*;
 import org.archive.io.arc.*;
 import org.archive.io.arc.*;
 import java.util.Iterator;
 import java.text.Format;
 
 import org.apache.tika.parser.*;
 import org.xml.sax.ContentHandler;
 import org.apache.tika.sax.*;
 
 import java.sql.*;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.*;
 
 import java.util.Vector;
 
 
 /**
  * <p>Title: ARCSubmitter</p>
  *
 * <p>Description: Read the configuration file, initialize the database, and create the tables if they don't exist
  * The default database is SQLite, but if you prefer to use a server side database, feel free to modify the configuration file
  * and provide the connection string for the database.
  * This class creates a thread pool of Worker runnables to handle the ARC records retrieved from the ARC file.
  * The order of tasks as follows: 1) parse the input parameters, 2) parse the configuration files and build a configuration object, 3)
 * Establish the database connection, 4) iterate through the input folder and process all the (new) ARC fields in it (Depth First). 5) for each ARC record
  *  in the ARC file, this class creates a SubmitterDocument object and submit the object to the thread pool for processing.
  * After processing the entire ARC file, the log is inserted to the database and the thread waits for the thread pool to
  * finish executing before reading the next ARC file.
  * When it finish processing all the files in the folder, the thread waits for a specified period (default is 5 minutes) before making another scan
  * in the folder to find new ARC files. Thus it can run in parallel with the crawler and waits for the new dumps.
  * </p>
  *
  * <p>Copyright: Copyright Madian Khabsa @ Penn State(c) 2009</p>
  *
  * <p>Company: Penn State</p>
  *
  * @author Madian Khabsa
  * @version 1.0
  */
 
 
 
 public class ARCSubmitter {
     public ARCSubmitter() {
     }
 
     /**
      * The root folder of the ARC files
      */
     public String OrgiginalPart;
     /**
      * The virtual path that will have the ARV files in it
      */
     public String CacheFolder;
     /**
      * URL of th eindex
      */
     public String URL;
     /**
      * Number of documents submitted so far
      */
     public long Count = 0;
     /**
      * Cinfiguration object
      */
     public SubmitterConfig Config;
     /**
      * The number of threads for processing the documents, default is 1
      */
     public int threadsCount = 1;
     public ExecutorService threadExecutor;
     /**
      * Queue containing the waiting jobs in the thread pool
      */
     public BlockingQueue<Runnable> WaitQueue;
     /**
      * List of documents that have been indexed but not yet inserted to the database
      */
     public Vector<SubmitterDocument> IndexedDocs = new Vector<SubmitterDocument>();
     /**
      * Line separator
      */
     public static final String LINE_SEP = System.getProperty("line.separator");
 
     /**
      *
      * @param args String[] Command-line arguments to process
      * Parameter 1: The URL of the indexer
      * Parameter 2: The folder to process which contains the ARC files
      * Parameter 3: The virtual directory under which the ARC files will be mapped to
      * Parameter 4: Number of threads to run
      * Parameter 5: [OPTIONAL] The waiting that the submitter will wait before iterating through the folder again
      */
     public static void main(String[] args) {
         ARCSubmitter submitter = new ARCSubmitter();
         if (args.length >= 4) {
             submitter.URL = args[0];
             submitter.OrgiginalPart = args[1];
             submitter.CacheFolder = args[2];
             int waitingTime = 1000 * 60 * 10; // Default is 10 minutes
             try {
                 submitter.threadsCount = Integer.parseInt(args[3]);
             } catch (Exception e) {
                 submitter.threadsCount = 1; // Default is 1
             }
             submitter.threadExecutor = Executors.newFixedThreadPool(submitter.
                     threadsCount);
             submitter.WaitQueue = ((ThreadPoolExecutor) submitter.
                                      threadExecutor).getQueue();
             if (args.length == 5) {
                 try {
                     waitingTime = Integer.parseInt(args[4]);
                 } catch (Exception e) {
                     waitingTime = 1000 * 60 * 10; // Ten minutes
                 }
             }
 
             File rootFolder = new File(submitter.OrgiginalPart);
             try {
                 Format formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
 
                 File f = new File("ARCSubmitterLog" + formatter.format(new Date()) +
                                   ".txt");
                 if (!f.exists())
                     f.createNewFile();
                 PrintStream sterr = new PrintStream(f);
                 System.setErr(sterr);
                 submitter.Config = new SubmitterConfig();
                 if (!submitter.Config.ReadConfigFile("SubmitterConfig.xml")) {
                     System.err.println("Couldn't parse Config File");
                     System.out.println("Couldn't parse Config File");
                     return;
                 }
                 submitter.Config.IndexURL = args[0];
                 submitter.Config.OriginalPart = args[1];
                 submitter.Config.CacheFolder = args[2];
                 if (!submitter.setupDBConnection()) {
                     System.out.println("Couldn't Setup Database Connection");
                     System.err.println(
                             "Couldn't Setup Database Connection ... Exiting");
 
                 }
             } catch (Exception ex) {
                 System.err.println("Error before setting standard error, check file permission in the current directory" +
                                    ex.toString());
                 System.out.println("Error before setting standard error, check file permission in the current directory" +
                                    ex.toString());
                 ex.printStackTrace();
                 return;
             }
 
             while (true) {
 
                 submitter.ProcessFolder(submitter.OrgiginalPart);
 
                 try {
 
                     // Wait for the other threads to finish
                     while (submitter.WaitQueue.size() > 0) {
 
                         Thread.currentThread().sleep(1000);
 
                     }
 
                     submitter.threadExecutor.shutdown();
                     submitter.threadExecutor.awaitTermination(60 * 60,
                             java.util.concurrent.TimeUnit.SECONDS); // Wait for thread pool to shut down
                     String result = submitter.sendPostCommand("<commit/>",
                             submitter.URL);
                     System.err.println("Commenting result is: " + result);
                     System.out.println("Commenting result is: " + result);
                     Format formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 
                     System.out.println(formatter.format(new Date()) +": Total Submitted Jobs: " +
                                        submitter.Count);
                     System.err.println(formatter.format(new Date()) + " : Total Submitted Jobs: " +
                                        submitter.Count);
 
                     if (waitingTime == 0)
                         break;
 
                     Thread.currentThread().sleep(waitingTime); // Wait the specified wait time from the input params
                 } catch (Exception ex) {
                     System.err.println(ex.getMessage());
                     ex.printStackTrace();
                 }
 
                 submitter.threadExecutor = Executors.newFixedThreadPool(
                         submitter.threadsCount); // Create a new thread pool
                 submitter.WaitQueue = ((ThreadPoolExecutor) submitter.
                                          threadExecutor).getQueue(); // The waiting jobs in the pool
                 submitter.Count = 0; // restart the counter
 
             }
 
         } else {
             System.out.println("Error, please enter correct parameter values");
         }
     }
 
     /**
      * Setup the database connection and create the mandatary tables
      * @return boolean true if connected, false otherwise
      * @throws Exception
      */
     public boolean setupDBConnection() throws Exception {
 
         /*
                Please note that the schema is by no means the best schema out there, nor the best storage efficiency schema
          that can be achieved. Though, it's a comprehensive schema that satisifies most of the requirments with reasonable
          storage requirements. It's, to a certin degree, tries to imitate how Google store its data in BigTable. We plan to
          support HBase in the future.
           Storing the URL might be the most compelling thing to change into some kind of DocID represented as
          Int64 or at least some hash value represented by 128 bits to provide uniform field size. However, generating the DocID
          from the database is not an optimal solution especially in the case of SQLlite. Besides, it's database dependent value
          and requesting this value varies from one database to another. Another con is finding the DocID in case the document has
          been crawled again, this will require querying the database (huge number of records) to find whether this URL corresponds
          to some DocID or not. Issueing this query for every URL will degrade the performance drastically, especially if the table doesn't
          fit in the main memory.
          Probably, the best trade off would be to use some kind of hashing fucntion (MD5, SHA1) for each URL, thus we don't
          need to query the database for any document as we compute the hash value in the application. The user would then need
          to store the mapping between each URL and its corresponding hash value in a separete table.
 
          All these ideas, or probably some other better ideas, are left to the user to implement. Or simply, rely on the simplified
          schema we provide. Please note that if you don't plan to crawl multiple versions of the same URL, then this schema will
          be very efficient, and you don't need any type of Unique ID or Hash Value.
          */
         Class.forName(this.Config.DatabaseProvider);
         Connection myConn = DriverManager.getConnection(this.Config.
                 DBConnectionString);
         Statement stat = myConn.createStatement();
         stat.executeUpdate(
                 "create table if not exists SubmittedARCFiles (Path VARCHAR(255) PRIMARY KEY, SubmitionTime DATE) ;");
         stat.executeUpdate(
                 "create table if not exists IndexedPages (Url VARCHAR(512), IndexingTime DATE ,FileType VARCHAR(100), ContainingFile VARCHAR(255),RecordOffset Integer, PRIMARY KEY (Url, IndexingTime));");
         stat.executeUpdate(
                 "create table if not exists SubmissionErrors (Url VARCHAR(512), IndexingTime DATE ,FileType VARCHAR(100), ContainingFile VARCHAR(255),RecordOffset Integer, ErrorMessage VARCHAR(500),PRIMARY KEY (Url, IndexingTime) ) ;");
         myConn.close();
         return true;
 
     }
 
     /**
      * Inserts this file to the database when the submitter completes processing all its records
      * @param fi File the file to be inserted to the database. It only inserts the full path of the file
      * @return boolean true if the insert was committed, otherwise false
      */
     private boolean InsertToDB(File fi) {
         try {
             Class.forName(this.Config.DatabaseProvider);
             Connection myConn = DriverManager.getConnection(
                     this.Config.DBConnectionString);
             Statement stat = myConn.createStatement();
             PreparedStatement prep = myConn.prepareStatement(
                     "insert into SubmittedARCFiles (Path, SubmitionTime) values (?, ?);");
             java.util.Date fromDate = new java.util.Date();
             prep.setString(1, fi.getAbsolutePath());
             prep.setTimestamp(2, new Timestamp(fromDate.getTime()));
             prep.addBatch();
             prep.executeBatch();
             return true;
 
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * Check whether the file has been already submitted to the index or not
      * @param fi File The file to be checked
      * @return boolean True if found in the database, false otherwise
      */
     private boolean IsIndexed(File fi) {
         try {
             Class.forName(this.Config.DatabaseProvider);
             Connection myConn = DriverManager.getConnection(
                     this.Config.DBConnectionString);
             Statement stat = myConn.createStatement();
             ResultSet rs = stat.executeQuery(
                     "select * from SubmittedARCFiles where Path ='" +
                     fi.getAbsolutePath() +
                     "';");
             boolean indexed = false;
             while (rs.next()) {
                 indexed = true;
                 break;
             }
             rs.close();
             myConn.close();
             return indexed;
 
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             return true;
         }
     }
 
     /**
      * Process a folder full of ARC files, or subfolders containing ARC files. The exploring follows depth first paradigm
      * For each file, the processor checks if the file has been indexed before, if so it ignores it. If the file is new then it
      * iterates through all the ARC records in this file. For each ARC record (which contains the downloaded document and metadata
      *  about it) it reads the document and its metadata, then it creates a SubmitterDocument object. This object will be submitted
      * to the thread pool that is responsible of parsing, extracting and handling the data before submitting it to the index.
      *
      * After processing a file and before moving to the next file, it waits for the processing thread pool to finish executing
      * and commit all the URLs to the database.
      *
      * @param path String the absolute path of the containing folder
      */
     public void ProcessFolder(String path) {
         System.out.println("Processing: " + path);
         System.err.println("Processing: " + path);
         File rootFolder = new File(path);
         String[] arcFiles = rootFolder.list();
         for (int i = 0; i < arcFiles.length; i++) {
             File fi = new File(path + "/" + arcFiles[i]);
             if (fi.isDirectory()) { // Going depth first
                 this.ProcessFolder(fi.getAbsolutePath());
                 continue;
             }
             if (fi.getAbsolutePath().endsWith("open")) {
                 continue; // File is still opened by Heritrix
 
             }
             if (this.IsIndexed(fi))
                 continue; // The ARC file has been already processed
             else {
 
                 String content = "";
                 String skippedContent = "";
                 String entryURL = "";
                 SubmitterDocument doc;
                 ARCReader r = null;
                 try {
 
                     r = org.archive.io.arc.ARCReaderFactory.get(new
                             java.io.File(fi.getAbsolutePath()));
                     System.out.println("Processing: " + fi.getAbsolutePath());
                     boolean digest = false;
                     boolean strict = false;
                     boolean parse = true;
                     r.setStrict(strict);
                     r.setParseHttpHeaders(parse);
                     r.setDigest(digest);
 
                     int recordIndex = 0; // To keep track of the offset inside the ARC file
                     for (Iterator iter = r.iterator(); iter.hasNext(); ) {
                         ARCRecord record = (ARCRecord) iter.next();
 
                         int offset = record.getBodyOffset();
 
                         ARCRecordMetaData meta = record.getMetaData();
                         entryURL = meta.getUrl();
                         long recordLength = meta.getLength();
                         if (!this.Config.IndexedTypes.contains(meta.getMimetype())) {
                             System.err.println("Url: " + entryURL +
                                                " was ignored as its data type is: " +
                                                meta.getMimetype());
                             recordIndex++;
                             continue; // File is not wanted
                         }
 
                         Integer responseCode = new Integer(meta.getStatusCode());
                         if (responseCode.intValue() >= 400) // Indicates an error
                         {
                             System.err.println("Url: " + entryURL +
                                                " was ignored since the status code indicates an error: "
                                                );
                             recordIndex++;
                             continue; // File is not wanted
 
                         }
 
                         // Just read and call the function
                         if (meta.getMimetype().startsWith("text")) {
                             content = this.ReadTextDocument(record, offset);
                             doc = new SubmitterDocument(entryURL, content,
                                     meta.getMimetype(),
                                     path + "/" + arcFiles[i], recordIndex++);
 
                         } else {
                             byte[] buffer = this.ReadBinaryDocument(record,
                                     offset,
                                     (int) meta.getLength());
                             if (buffer == null) {
                                 //log error to DB
                                 System.err.println("ARC record fot the URL " + entryURL + "Couldn't be read. An exception was encountered");
                                 continue;
                             }
                             doc = new SubmitterDocument(entryURL, buffer,
                                     meta.getMimetype(),
                                     path + "/" + arcFiles[i], recordIndex++);
 
                         }
 
                         Worker wrkr = new Worker(this, doc);
 
                         // Submit the job to the thread pool
                         this.threadExecutor.execute(wrkr);
 
                     }
                     this.InsertToDB(fi); // Insert the ARC file to the DB and mark it as processed
                     while (this.WaitQueue.size() > 0) {
 
                         Thread.currentThread().sleep(1000); // Wait for the thread pool to finish executing
 
                     }
                     String result = this.sendPostCommand("<commit/>",
                             this.URL); // Commit the submitted document to the index, in case the program crashed
                     this.FlushIndexedDocs(); // Insert the submitted URLs to the database
 
                 } catch (Exception ex) {
                     System.err.println("Exception at the ARC processing part: " +
                                        ex.getMessage());
                     ex.printStackTrace();
                 } finally {
                     try {
                         r.close();
                     } catch (Exception e) {}
                 }
 
             }
 
         }
 
     }
 
     /**
      * Insert all the processed URLs (ARC records) to the database. The database will contain the Url, file type, indexing time
      * containing folder, and the record offset within the ARC file
      */
     private void FlushIndexedDocs() {
         try {
             Class.forName(this.Config.DatabaseProvider);
             Connection myConn = DriverManager.getConnection(
                     this.Config.DBConnectionString);
             Statement stat = myConn.createStatement();
             PreparedStatement prep = myConn.prepareStatement(
                     "insert into IndexedPages (Url, IndexingTime,FileType, ContainingFile,RecordOffset ) values (?, ?, ?, ?, ?);");
             java.util.Date fromDate = new java.util.Date();
             Timestamp ts = new Timestamp(fromDate.getTime());
             while (this.IndexedDocs.size() > 0) {
                 SubmitterDocument doc = this.IndexedDocs.remove(0);
                 prep.setString(1, doc.getUrl());
                 prep.setTimestamp(2, ts);
                 prep.setString(3, doc.getDataType());
                 prep.setString(4, doc.getContainingFile());
                 prep.setInt(5, doc.getOffset());
                 prep.addBatch();
                 this.Count++;
 
             }
 
             prep.executeBatch();
 
         } catch (Exception e) {
             System.err.println("Couldn't Flush the Vector " + e.getMessage());
             e.printStackTrace();
         }
 
     }
 
     /**
      * Reads the content of the ARC record from the ARC file
      * @param record ARCRecord the record that its content is to be read
      * @param offset int the offset at which the content of the document begins
      * @param recordLength int
      * @return byte[] the byte array containing the document data
      */
     public byte[] ReadBinaryDocument(ARCRecord record, int offset,
                                      int recordLength) {
         try {
             int read = 0;
             byte[] buffer = new byte[recordLength - offset];
 
             int totalRead = 0;
             do {
                 byte[] bytearr = new byte[offset];
                 read = record.read(bytearr, 0, offset);
 
                 totalRead += read;
 
             } while (totalRead != offset);
             read = 0;
             totalRead = 0;
             do {
 
                 read = record.read(buffer, totalRead, 40960); // Using larger buffer, as usually binary files are large
 
                 totalRead += read;
 
             } while (read != -1);
             return buffer;
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             return null;
         }
 
     }
 
     /**
      * Reads a text document from the ARC record
      * @param record ARCRecord the record that its content is to be read
      * @param offset int the offset at which the content of the document begins
      * @return String the content of the text document
      */
     public String ReadTextDocument(ARCRecord record, int offset) {
         try {
             int read = 0;
             StringBuilder sb = new StringBuilder();
             do {
                 byte[] bytearr = new byte[4096];
                 read = record.read(bytearr, 0, 4096);
                 sb.append(new String(bytearr, 0,
                                      read == -1 ? 0 : read - offset));
                 offset = 0;
 
             } while (read != -1);
             return sb.toString();
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Sends a post request to the server
      * Courtesy of Grant Ingersoll @ IBM
      * @param command String the command to be sent
      * @param url String the URL of the server
      * @return String The result of the submit
      * @throws Exception
      */
     public String sendPostCommand(String command, String url) throws
             Exception {
         String results = null;
         HttpClient client = new HttpClient();
         PostMethod post = new PostMethod(url);
 
         RequestEntity re = new StringRequestEntity(command, "text/xml", "UTF-8");
         post.setRequestEntity(re);
         try {
             // Execute the method.
             int statusCode = client.executeMethod(post);
 
             if (statusCode != HttpStatus.SC_OK) {
                 System.err.println("Method failed: " + post.getStatusLine());
             }
 
             // Read the response body.
             byte[] responseBody = post.getResponseBody();
 
             // Deal with the response.
             // Use caution: ensure correct character encoding and is not binary data
             results = new String(responseBody);
         } catch (HttpException e) {
             //System.err.println("Fatal protocol violation: " + e.getMessage());
             //e.printStackTrace();
             throw e;
         } catch (IOException e) {
             //System.err.println("Fatal transport error: " + e.getMessage());
             //e.printStackTrace();
             throw e;
         } finally {
             // Release the connection.
             post.releaseConnection();
             client.getHttpConnectionManager().closeIdleConnections(0); // Don not remove this line
             // releaseConnection() is known to throw an exception, and leave the connection open, hence increase the possibility
             // of file descriptors leak in the system. Eventually exhasting all the open files limit
             // closeIdleConnections() will incure some overhead, but you won't risk running out of file descriptos
 
         }
         return results;
     }
 
 
 }
