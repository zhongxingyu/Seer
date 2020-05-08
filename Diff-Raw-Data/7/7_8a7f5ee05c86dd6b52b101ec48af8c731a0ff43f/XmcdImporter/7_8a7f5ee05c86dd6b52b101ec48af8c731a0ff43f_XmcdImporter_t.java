 package my.triviagame.xmcd;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Queues;
 import com.sun.xml.internal.xsom.impl.scd.Iterators;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import my.triviagame.dal.DALException;
 import my.triviagame.dal.IDAL;
 
 /**
  * Imports {@link XmcdDisc}s into the database.
  * 
  * Import one disc:
  * XmcdDisc myDisc = XmcdDisc.fromXmcdFile(...);
  * XmcdImporter importer = new XmcdImporter(dal);
  * importer.importFreedb(disc);
  * 
  * Import one or more FreeDB archive:
  * XmcdStream allMyDiscs = new XmcdDiscStream(myXmcdArchives, true);
  * XmcdImporter importer = new XmcdImporter(dal);
  * importer.importFreedb(allMyDiscs);
  * allMyDiscs.close();
  * 
  * Pro-tip: chain iterators to perform additional operations!
  * For instance, to get only the first 100 discs from an archive which have a year specified and are not by "Various":
  * XmcdStream discsInArchive = new XmcdDiscStream(myXmcdArchive);
  * Iterator<XmcdDisc> discsToImport = new XmcdFilters.Filter(discsInArchive)
  *     .hasYear()
  *     .notVarious()
  *     .firstN(100)
  *     .chain();
  * XmcdImporter importer = new XmcdImporter(dal);
  * importer.importFreedb(discsToImport);
  * discsInArchive.close();
  */
 public class XmcdImporter {
 
     /** By default, handle xmcd data in batches of 10,000 */
     public static final int DEFAULT_BATCH_SIZE = 10000;
     
     /** By default, buffer ahead up to 4 batches */
     public static final int DEFAULT_BUFFER_AHEAD = 4;
     
     /** Maximum wait period for the background thread to spool more data, in seconds. */
     public static final int MAX_WAIT_SEC = 60;
     
     public static final Logger logger = Logger.getLogger(XmcdImporter.class.getName());
     
     /**
      * @param dal the underlying {@link DAL} to use
      */
     public XmcdImporter(IDAL dal) {
         this(dal, DEFAULT_BATCH_SIZE, DEFAULT_BUFFER_AHEAD);
     }
     
     /**
      * @param dal the underlying {@link DAL} to use
      * @param batchSize count of discs to process in local index per batch before pushing them to the DB
      * @param bufferAhead count of batches to buffer ahead
      */
     public XmcdImporter(IDAL dal, int batchSize, int bufferAhead) {
         this.dal = dal;
         this.batchSize = batchSize;
         this.bufferAhead = bufferAhead;
     }
     
     /**
      * Import a Freedb archive (update or complete).
      * @param archive FreeDB archive to import
      */
     public void importFreedb(XmcdDisc disc) throws IOException, DALException {
         importFreedb(Iterators.singleton(disc));
     }
     
     /**
      * Import any number of Freedb archives (e.g. a series of updates).
      * 
      * @param archives FreeDB archives to import
      */
     public void importFreedb(final Iterator<XmcdDisc> discs) throws IOException, DALException {
         final BlockingQueue<List<XmcdDisc>> batchQueue = Queues.newArrayBlockingQueue(bufferAhead);
         
         // Background worker thread for making sure extraction & parsing are keeping the I/O & CPU busy
         Thread extractThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     while (discs.hasNext() && !stop) {
                         List<XmcdDisc> nextBatch = Lists.newArrayListWithCapacity(batchSize);
                         while (discs.hasNext() && nextBatch.size() < batchSize) {
                             try {
                                 nextBatch.add(discs.next());
                             } catch (XmcdException ex) {
                                 logger.log(Level.WARNING, "Error while parsing xmcd file", ex);
                             } catch (Exception ex) {
                                 logger.log(Level.SEVERE, "Unexpected exception", ex);
                                 // Stop extracting
                                 break;
                             }
                             filesParsed++;
                         }
                         batchQueue.put(nextBatch);
                     }
                 } catch (InterruptedException ex) {
                     // Stop extracting
                 } finally {
                     try {
                         // Signal to the consumer that there are no batches left to process
                         batchQueue.put(Collections.<XmcdDisc>emptyList());
                     } catch (InterruptedException ex) {
                         Thread.currentThread().interrupt();
                     }
                 }
             }
         });
         extractThread.start();
 
         List<XmcdDisc> nextBatch;
         try {
             while ((nextBatch = batchQueue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS)) != null &&
                     !nextBatch.isEmpty() && !stop) {
                 dal.importXmcdBatch(nextBatch);
                 filesImported += nextBatch.size();
             }
             if (nextBatch == null) {
                 logger.warning("Background thread did not respond in time");
             }
         } catch (InterruptedException ex) {
             Thread.currentThread().interrupt();
         } finally {
             // Make sure the extractor thread is not stuck if we're leaving
             extractThread.interrupt();
         }
     }
     
     /**
      * Gets a status report of how many files have been parsed so far.
      */
     public long getFilesParsedCount() {
         return filesParsed;
     }
     
     /** 
      * Gets a status report of how many files have been pushed to the DB so far.
      */
     public long getFilesImportedCount() {
         return filesImported;
     }
     
     /**
      * Orders the processing to stop.
      */
     public void stop() {
         stop = true;
     }
     
     /** Underlying DAL */
     private final IDAL dal;
     /** How many {@link XmcdDisc}s to process on each batch */
     private final int batchSize;
     /** How many batches to buffer ahead at most */
     private final int bufferAhead;
     /** Count of how many files have been parsed so far */
     private long filesParsed = 0;
     /** Count of how many files have been imported to the DB so far */
     private long filesImported = 0;
     /** Whether to stop execution */
     private boolean stop = false;
 }
