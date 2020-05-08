 
 package com.randomhumans.svnindex.indexing;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.tmatesoft.svn.core.SVNDirEntry;
 
 import com.randomhumans.svnindex.document.ContentDocument;
 import com.randomhumans.svnindex.parsing.ContentDocumentGenerator;
 import com.randomhumans.svnindex.util.Configuration;
 
 public class DirectoryEntryThreadPool implements Runnable
 {
     static Log log = LogFactory.getLog(DirectoryEntryThreadPool.class);
 
     private static final ExecutorService indexerPool = Executors.newFixedThreadPool(Configuration.getConfig().getDirectoryEntryThreadPoolPoolSize());
 
     String docUrl = "";
 
     SVNDirEntry dirEntry = null;
 
     public static void queueEntry(final String url, final SVNDirEntry entry)
     {
         DirectoryEntryThreadPool.indexerPool.submit(new DirectoryEntryThreadPool(url, entry));
     }
 
     public DirectoryEntryThreadPool(final String url, final SVNDirEntry entry)
     {
         this.docUrl = url;
         this.dirEntry = entry;
     }
 
     public void run()
     {
         DirectoryEntryThreadPool.log.debug(this.docUrl);
         try
         {
            final ContentDocument doc = ContentDocumentGenerator.createDocument(this.dirEntry, this.docUrl);            
             if (doc != null)
             {
                 ContentIndexerThread.queueDocument(doc);
             }
         }
         catch (final InterruptedException e)
         {
             DirectoryEntryThreadPool.log.error(e);
         }
        catch (RuntimeException e)
        {
            log.error(e,e);
            throw(e);
        }
     }
 
     public static void shutdown()
     {        
         DirectoryEntryThreadPool.indexerPool.shutdown();
         try
         {            
             while(!DirectoryEntryThreadPool.indexerPool.awaitTermination(20, TimeUnit.SECONDS))
             {
                 DirectoryEntryThreadPool.log.debug("waiting");
             }
         }
         catch (final InterruptedException e)
         {
             DirectoryEntryThreadPool.log.warn(e);
         }
     }
 
 }
