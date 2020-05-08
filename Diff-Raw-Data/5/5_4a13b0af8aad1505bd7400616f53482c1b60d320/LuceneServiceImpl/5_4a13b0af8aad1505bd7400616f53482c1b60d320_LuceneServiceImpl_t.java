 package com.amee.platform.search;
 
 import com.amee.base.domain.ResultsWrapper;
 import org.apache.commons.io.comparator.LastModifiedFileComparator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.*;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.LockReleaseFailedException;
 import org.apache.lucene.util.Version;
 import org.springframework.beans.factory.annotation.Value;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * LuceneIndexWrapper wraps a Lucene file system index and provides a simplified abstraction of
  * the Lucene API.
  */
 public class LuceneServiceImpl implements LuceneService {
 
     private final Log log = LogFactory.getLog(getClass());
 
     public final static int MAX_NUM_HITS = 1000;
 
     /**
      * Path to the snapshooter script
      */
     private String snapShooterPath = "";
 
     /**
      * Path to the dir containing lucene index
      */
     private String indexDirPath = "";
 
     /**
      * The primary Lucene Searcher.
      */
     private IndexSearcher searcher;
 
     /**
      * A Lucene Searcher used recently. A reference is kept to this when the primary Searcher
      * is re-opened.
      */
     private IndexSearcher lastSearcher;
 
     /**
      * The shared Lucene Analyzer.
      */
     private Analyzer analyzer;
 
     /**
      * The shared Lucene directory.
      */
     private Directory directory;
 
     /**
      * The shared Lucene IndexWriter.
      */
     private IndexWriter indexWriter;
 
     /**
      * Is this instance the master index node? There can be only one!
      */
     private boolean masterIndex = false;
 
     /**
      * Should index snapshots be created? Only required for replication.
      */
     private Boolean snapshotEnabled = false;
 
     /**
      * The time of the most recent index write.
      */
     private long lastWriteTime = 0L;
 
     /**
      * Lock objects for the index.
      */
     private ReadWriteLock rwLock = new ReentrantReadWriteLock(true);
     private Lock rLock = rwLock.readLock();
     private Lock wLock = rwLock.writeLock();
 
     /**
      * Conduct a search in the Lucene index based on the supplied Query, constrained by resultStart and resultLimit.
      * <p/>
      * At most this will allow up to MAX_RESULT_LIMIT search hits, with a return window based
      * on resultStart and resultLimit.
      *
      * @param query       to search with
      * @param resultStart 0 based index of first result
      * @param resultLimit results limit
      * @return a List of Lucene Documents
      */
     @Override
     public ResultsWrapper<Document> doSearch(Query query, final int resultStart, final int resultLimit) {
         rLock.lock();
         try {
             log.info("doSearch() query='" + query.toString() + "', resultStart=" + resultStart + ", resultLimit=" + resultLimit);
             long start = System.currentTimeMillis();
             // Cannot go above MAX_NUM_HITS.
             int numHits = resultStart + resultLimit;
             if (numHits > MAX_NUM_HITS) {
                 numHits = MAX_NUM_HITS;
             }
             // Get Collector limited to numHits + 1, so we can detect truncations.
             TopScoreDocCollector collector = TopScoreDocCollector.create(numHits + 1, true);
             // Get the IndexSearcher and do the search.
             Searcher searcher = getIndexSearcher();
             searcher.search(query, collector);
             // Get hits within our start and limit range.
             ScoreDoc[] hits = collector.topDocs(resultStart, resultLimit + 1).scoreDocs;
             // Assemble List of Documents.
             List<Document> documents = new ArrayList<Document>();
             for (ScoreDoc hit : hits) {
                 documents.add(searcher.doc(hit.doc));
             }
             // Trim resultLimit if we're close to MAX_NUM_HITS.
             int resultLimitWithCeiling = resultLimit;
             if (resultStart >= MAX_NUM_HITS) {
                 // Never return results.
                 resultLimitWithCeiling = 0;
             } else if ((resultStart + resultLimit) > MAX_NUM_HITS) {
                 // Only return those results from resultStart to MAX_NUM_HITS.
                 resultLimitWithCeiling = MAX_NUM_HITS - resultStart;
             }
             // Create ResultsWrapper appropriate for our limit.
             ResultsWrapper<Document> results = new ResultsWrapper<Document>(
                     documents.size() > resultLimitWithCeiling ? documents.subList(0, resultLimitWithCeiling) : documents,
                     (documents.size() > resultLimitWithCeiling) && !((resultStart + resultLimitWithCeiling) >= MAX_NUM_HITS),
                     resultStart,
                     resultLimit,
                     collector.getTotalHits() > MAX_NUM_HITS ? MAX_NUM_HITS : collector.getTotalHits());
             log.info("doSearch() Duration: " + (System.currentTimeMillis() - start));
             return results;
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             rLock.unlock();
         }
     }
 
     /**
      * Conduct a search in the Lucene index based on the supplied Query (unconstrained.
      * <p/>
      * At most this will allow up to MAX_RESULT_LIMIT search hits.
      *
      * @param query to search with
      * @return a List of Lucene Documents
      */
     @Override
     public ResultsWrapper<Document> doSearch(Query query) {
         rLock.lock();
         try {
             log.info("doSearch() query='" + query.toString() + "'");
             long start = System.currentTimeMillis();
             // Get Collector limited to numHits + 1, so we can detect truncations.
             TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_NUM_HITS + 1, true);
             // Get the IndexSearcher and do the search.
             Searcher searcher = getIndexSearcher();
             searcher.search(query, collector);
             // Get all hits.
             ScoreDoc[] hits = collector.topDocs().scoreDocs;
             // Assemble List of Documents.
             List<Document> documents = new ArrayList<Document>();
             for (ScoreDoc hit : hits) {
                 documents.add(searcher.doc(hit.doc));
             }
             // Create ResultsWrapper containing all Documents.
             ResultsWrapper<Document> results = new ResultsWrapper<Document>(
                     documents,
                     documents.size() > MAX_NUM_HITS);
             log.info("doSearch() Duration: " + (System.currentTimeMillis() - start));
             return results;
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             rLock.unlock();
         }
     }
 
     @Override
     public void addDocument(Document document) {
         if (!masterIndex || (document == null)) return;
         rLock.lock();
         try {
             getIndexWriter().addDocument(document);
             getIndexWriter().commit();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             lastWriteTime = System.currentTimeMillis();
             rLock.unlock();
         }
     }
 
     @Override
     public void addDocuments(Collection<Document> documents) {
         if (!masterIndex || (documents == null) || documents.isEmpty()) return;
         rLock.lock();
         try {
             for (Document document : documents) {
                 getIndexWriter().addDocument(document);
             }
             getIndexWriter().commit();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             lastWriteTime = System.currentTimeMillis();
             rLock.unlock();
         }
     }
 
     /**
      * This will remove the Document matching the supplied Terms and then add the supplied Document.
      *
      * @param document to add
      * @param terms    Terms to form Query to remove existing Document.
      */
     @Override
     public void updateDocument(Document document, Term... terms) {
         if (!masterIndex || (document == null)) return;
         BooleanQuery q = new BooleanQuery();
         for (Term t : terms) {
             if (t != null) {
                 q.add(new TermQuery(t), BooleanClause.Occur.MUST);
             }
         }
         rLock.lock();
         try {
             getIndexWriter().deleteDocuments(q);
             getIndexWriter().addDocument(document);
             getIndexWriter().commit();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             lastWriteTime = System.currentTimeMillis();
             rLock.unlock();
         }
     }
 
     @Override
     public void deleteDocuments(Term... terms) {
         if (!masterIndex) return;
         BooleanQuery q = new BooleanQuery();
         for (Term t : terms) {
             if (t != null) {
                 q.add(new TermQuery(t), BooleanClause.Occur.MUST);
             }
         }
         deleteDocuments(q);
     }
 
     @Override
     public void deleteDocuments(Query q) {
         if (!masterIndex) return;
         rLock.lock();
         try {
             getIndexWriter().deleteDocuments(q);
             getIndexWriter().commit();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             lastWriteTime = System.currentTimeMillis();
             rLock.unlock();
         }
     }
 
     /**
      * Clear the Lucene index.
      */
     @Override
     public void clearIndex() {
         if (!masterIndex) return;
         wLock.lock();
         try {
             // Ensure everything is closed.
             closeEverything();
             // Ensure index is not locked (perhaps from a crash).
             unlockIndex();
             // Create a new index.
             IndexWriter indexWriter = getNewIndexWriter(true);
             // Close the index.
             indexWriter.commit();
             indexWriter.close();
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             closeIndexWriter();
             wLock.unlock();
         }
     }
 
     /**
      * Unlocks the Lucene index. Useful following JVM crashes.
      */
     @Override
     public void unlockIndex() {
         wLock.lock();
         try {
             if (IndexReader.indexExists(getDirectory())) {
                 if (IndexWriter.isLocked(getDirectory())) {
                     log.info("unlockIndex() Unlocking index.");
                     IndexWriter.unlock(getDirectory());
                 }
             }
         } catch (LockReleaseFailedException e) {
             log.warn("unlockIndex() Caught LockReleaseFailedException: " + e.getMessage());
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         } finally {
             wLock.unlock();
         }
     }
 
     /**
      * On finalize, ensure Lucene objects are closed.
      *
      * @throws Throwable the <code>Exception</code> raised by this method
      */
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         closeEverything();
     }
 
     /**
      * Ensure all Lucene objects are closed.
      */
     @Override
     public void closeEverything() {
         wLock.lock();
         try {
             closeLastSearcher();
             closeSearcher();
             closeIndexWriter();
             unlockIndex();
             closeDirectory();
         } finally {
             wLock.unlock();
         }
     }
 
     /**
      * Get the Searcher.
      *
      * @return the Searcher
      */
     private IndexSearcher getIndexSearcher() {
         if (searcher == null) {
             synchronized (this) {
                 if (searcher == null) {
                     try {
                         searcher = new IndexSearcher(getDirectory(), true);
                     } catch (IOException e) {
                         throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
                     }
                 }
             }
         }
         return searcher;
     }
 
     /**
      * Check the Searcher to see if it needs re-opening.
      * <p/>
      * This method is called via cron.
      */
     @Override
     public void checkSearcher() {
         try {
             // Close the last Searcher.
             closeLastSearcher();
             // Should the current Searcher be re-opened?
             if ((searcher != null) && !searcher.getIndexReader().isCurrent()) {
                 // Store the current Searcher so it can be closed later.
                 // This allows other threads using the Searcher to finish their work.
                 lastSearcher = searcher;
                 // Set the Searcher to null means a new instance will be created later in getSearcher().
                 searcher = null;
             }
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Close the Searcher.
      */
     private void closeSearcher() {
         if (searcher == null) return;
         try {
             searcher.close();
             searcher = null;
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Close the last Searcher.
      */
     private void closeLastSearcher() {
         if (lastSearcher == null) return;
         try {
             lastSearcher.close();
             lastSearcher = null;
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Get the Analyzer. Will call getNewAnalyzer if it does not yet exist.
      *
      * @return the Analyzer
      */
     private Analyzer getAnalyzer() {
         if (analyzer == null) {
             synchronized (this) {
                 if (analyzer == null) {
                     analyzer = new StandardAnalyzer(Version.LUCENE_30);
                 }
             }
         }
         return analyzer;
     }
 
     /**
      * Get the Directory. Will call createDirectory if it does not yet exist.
      *
      * @return the Directory
      */
     private Directory getDirectory() {
         if (directory == null) {
             synchronized (this) {
                 try {
                     if (directory == null) {
                         String path = System.getProperty("amee.lucenePath", "/var/www/apps/platform-api/lucene/index");
                         directory = FSDirectory.open(new File(path));
                     }
                 } catch (IOException e) {
                     throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
                 }
             }
         }
         return directory;
     }
 
     /**
      * Closes the Lucene directory.
      */
     private synchronized void closeDirectory() {
         if (directory == null) return;
         try {
             directory.close();
             directory = null;
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Gets IndexWriter. Will call getNewIndexWriter if an IndexWriter is not yet created. Can
      * be called multiple times within a thread. The create parameter is only effective when the
      * IndexWriter has not previously been created.
      * <p/>
      * Later, closeIndexWriter must be called at least once.
      *
      * @return the IndexWriter
      */
     private IndexWriter getIndexWriter() {
         if (indexWriter == null) {
             synchronized (this) {
                 if (indexWriter == null) {
                     indexWriter = getNewIndexWriter(false);
                 }
             }
         }
         return indexWriter;
     }
 
     /**
      * Construct and set a new IndexWriter. Should only be called once within a thread.
      * <p/>
      * Later, closeIndexWriter must be called at least once.
      *
      * @param create a new index if true
      * @return IndexWriter
      */
     private IndexWriter getNewIndexWriter(boolean create) {
         try {
             return new IndexWriter(
                     getDirectory(),
                     getAnalyzer(),
                     create,
                     IndexWriter.MaxFieldLength.UNLIMITED);
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Flush the IndexWriter. Will optimise and commit the index if appropriate.
      */
     @Override
     public void flush() {
         if (!masterIndex) return;
         rLock.lock();
         try {
             log.info("flush() Starting.");
             if (!getIndexSearcher().getIndexReader().isOptimized()) {
                 getIndexWriter().optimize();
                 getIndexWriter().commit();
             } else {
                 log.info("flush() Index already optimized.");
             }
             log.info("flush() Done.");
         } catch (IOException e) {
            log.error("flush() Caught IOException: " + e.getMessage(), e);
         } finally {
             rLock.unlock();
         }
     }
 
     /**
      * Closes the IndexWriter. Will flush the index prior to closing.
      */
     private synchronized void closeIndexWriter() {
         if (indexWriter == null) return;
         log.info("closeIndexWriter()");
         try {
             flush();
             indexWriter.close();
             indexWriter = null;
         } catch (IOException e) {
             throw new RuntimeException("Caught IOException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Takes a snapshot of the lucene index using the solr snapshooter shell script.
      * http://wiki.apache.org/solr/SolrCollectionDistributionScripts
      */
     public void takeSnapshot() {
         if (!snapshotEnabled) return;
         Process p = null;
         Timer timer;
         String command;
         InterruptTimerTask interrupter;
         // Only take a snapshot if it is due.
         if (isSnapshotDue()) {
             // We need a write lock to ensure consistency.
             wLock.lock();
             // Setup time and command.
             timer = new Timer(true);
             command = getSnapShooterPath() + " -d " + getIndexDirPath();
             try {
                 log.info("takeSnapshot() Executing: " + command);
                 // Invoke the Snapshooter.
                 p = Runtime.getRuntime().exec(command);
                 // Use a Timer to interrupt later on timeout.
                 interrupter = new InterruptTimerTask(Thread.currentThread());
                 timer.schedule(interrupter, 30 * 1000); // 30 second timeout.
                 // Wait for process to complete (or until timeout is reached).
                 p.waitFor();
                 // If we get here then the snapshot completed.
                 log.info("takeSnapshot() Done.");
             } catch (IOException e) {
                log.error("takeSnapshot() Caught IOException: " + e.getMessage(), e);
             } catch (InterruptedException e) {
                 p.destroy();
                 log.warn("takeSnapshot() Timed out.");
             } finally {
                 // Tidy up.
                 timer.cancel();
                 Thread.interrupted();
                 wLock.unlock();
             }
         }
     }
 
     /**
      * A TimerTask used by takeSnapshot().
      */
     private class InterruptTimerTask extends TimerTask {
 
         private Thread thread;
 
         public InterruptTimerTask(Thread t) {
             this.thread = t;
         }
 
         public void run() {
             thread.interrupt();
         }
 
     }
 
     /**
      * @return true if the last snapshot was taken longer than the most recent write
      */
     private boolean isSnapshotDue() {
 
         // Unless this is the master index this test will always return false.
         return lastWriteTime > getLastSnapshotTime();
     }
 
     /**
      * Gets the last modified time of the latest snapshot.
      *
      * @return A long value representing the time the snapshot was taken,
      *         measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
      *         or 0L if no snapshot exists or if an I/O error occurs.
      */
     private long getLastSnapshotTime() {
         File snapshotDir = new File(getIndexDirPath());
         File[] snapshotFiles = snapshotDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.startsWith("snapshot");
             }
         });
         if (snapshotFiles != null && snapshotFiles.length > 0) {
             Arrays.sort(snapshotFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
             return snapshotFiles[0].lastModified();
         } else {
             return 0L;
         }
     }
 
     @Value("#{ systemProperties['amee.masterIndex'] }")
     public void setMasterIndex(Boolean masterIndex) {
         this.masterIndex = masterIndex;
     }
 
     @Value("#{ systemProperties['amee.snapshotEnabled'] }")
     public void setSnapshotEnabled(Boolean snapshotEnabled) {
         this.snapshotEnabled = snapshotEnabled;
     }
 
     public String getIndexDirPath() {
         return indexDirPath;
     }
 
     public void setIndexDirPath(String indexDirPath) {
         this.indexDirPath = indexDirPath;
     }
 
     public String getSnapShooterPath() {
         return snapShooterPath;
     }
 
     public void setSnapShooterPath(String snapShooterPath) {
         this.snapShooterPath = snapShooterPath;
     }
 }
