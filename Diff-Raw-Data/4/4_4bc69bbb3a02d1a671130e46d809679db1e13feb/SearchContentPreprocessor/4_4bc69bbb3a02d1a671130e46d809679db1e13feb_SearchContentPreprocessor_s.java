 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.search;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
 
 import javax.inject.Singleton;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.apache.lucene.util.Version;
 
 import jodd.lagarto.dom.jerry.Jerry;
 
 import com.meltmedia.cadmium.core.meta.ConfigProcessor;
 
 @Singleton
 public class SearchContentPreprocessor  implements ConfigProcessor, IndexSearcherProvider, Closeable {
   
   public static FileFilter HTML_FILE_FILTER = new FileFilter() {
     @Override
     public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getPath().matches(".*\\.htm[l]?\\Z");
     }
   };
   
   public static FileFilter DIR_FILTER = new FileFilter() {
     @Override
     public boolean accept(File pathname) {
       return pathname.isDirectory();
     }
   };
   
   public static FileFilter NOT_INF_DIR_FILTER = new FileFilter() {
     @Override
     public boolean accept(File pathname) {
       return pathname.isDirectory() && !pathname.getName().endsWith("-INF");
     }
   };
   
   public static Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
     @Override
     public int compare(File file1, File file2) {
       return file1.getName().compareTo(file2.getName());
     }
   };
   
   /**
    * A template class that scans the content directory, starting at the root, and
    * calls scan(File) for every file that matches the provided content filter.
    * 
    * @author Christian Trimble
    */
   public static abstract class ContentScanTemplate
   {
     private FileFilter contentFilter;
 
     public ContentScanTemplate(FileFilter contentFilter) {
       this.contentFilter = contentFilter;
     }
     
     public void scan( File contentRoot ) throws Exception {
       // create the frontier and add the content root.
       LinkedList<File> frontier = new LinkedList<File>();
       
       // scan the content root dir for html files.
       for( File htmlFile : contentRoot.listFiles(contentFilter)) {
         handleFile(htmlFile);
       }
       
       // add the non "-INF" directories, in a predictable order.
       frontier.subList(0, 0).addAll(Arrays.asList(sort(contentRoot.listFiles(NOT_INF_DIR_FILTER), FILE_NAME_COMPARATOR)));
       
       while( !frontier.isEmpty() ) {
         File dir = frontier.removeFirst();
         
         // scan the html files in the directory.
         for( File htmlFile : dir.listFiles(contentFilter)) {
           handleFile(htmlFile);
         }
    
         // add the directories, in a predictable order.
         frontier.subList(0, 0).addAll(Arrays.asList(sort(dir.listFiles(DIR_FILTER), FILE_NAME_COMPARATOR)));
       }
     }
     
     /**
      * An call to Arrays.sort(array, comparator) that returns the array argument after the sort.
      * 
      * @param array the array to sort.
      * @param comparator the comparator to sort with.
      * @return the array argument.
      */
     private static <T> T[] sort( T[] array, Comparator<T> comparator ) {
       Arrays.sort(array, comparator);
       return array;
     }
     
     public abstract void handleFile( File file )
       throws Exception;
   }
   
   private File indexDir;
   private File dataDir;
   private SearchHolder liveSearch = null;
   private SearchHolder stagedSearch = null;
   private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
   private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();
   private final ReadLock readLock = locker.readLock();
   private final WriteLock writeLock = locker.writeLock();
   
 
   @Override
   public synchronized void processFromDirectory(String metaDir) throws Exception {
     SearchHolder newStagedSearcher = new SearchHolder();
     indexDir = new File(metaDir, "lucene-index");
     dataDir = new File(metaDir).getParentFile();
     newStagedSearcher.directory = new NIOFSDirectory(indexDir);
     IndexWriter iwriter = null;
     try {
       iwriter = new IndexWriter(newStagedSearcher.directory, new IndexWriterConfig(Version.LUCENE_36, analyzer).setRAMBufferSizeMB(5));
       iwriter.deleteAll();
       writeIndex(iwriter, dataDir);
     }
     finally {
       IOUtils.closeQuietly(iwriter);
     }
     newStagedSearcher.indexReader = IndexReader.open(newStagedSearcher.directory);
     stagedSearch = newStagedSearcher;
   }
   
   void writeIndex( final IndexWriter indexWriter, File contentDir ) throws Exception {
     new ContentScanTemplate(HTML_FILE_FILTER) {
       @Override
       public void handleFile(File file) throws Exception {
         Jerry jerry = Jerry.jerry(FileUtils.readFileToString(file, "UTF-8"));
         String title = jerry.$("html > head > title").text();
         String textContent = jerry.$("html > body").text();
 
         Document doc = new Document();
         doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
         doc.add(new Field("content", textContent, Field.Store.YES, Field.Index.ANALYZED));
         doc.add(new Field("path", file.getPath().replaceFirst(dataDir.getPath(), ""), Field.Store.YES, Field.Index.NOT_ANALYZED));
         indexWriter.addDocument(doc);
       }
     }.scan(contentDir); 
     
   }
 
   @Override
   public synchronized void makeLive() {
     writeLock.lock();
     if( this.stagedSearch != null && this.stagedSearch.directory != null && this.stagedSearch.indexReader != null ) {
       SearchHolder oldLive = liveSearch;
       liveSearch = stagedSearch;
       IOUtils.closeQuietly(oldLive);
       stagedSearch = null;
     }
     writeLock.unlock();
   }
   
   public void finalize() {
     IOUtils.closeQuietly(liveSearch);
     IOUtils.closeQuietly(stagedSearch);
  }
 
   @Override
   public IndexSearcher startSearch() {
     readLock.lock();
     if(this.liveSearch != null) {
       if(this.liveSearch.indexSearcher == null) {
         IndexSearcher searcher = new IndexSearcher(this.liveSearch.indexReader);
         this.liveSearch.indexSearcher = searcher;
       }
       return this.liveSearch.indexSearcher;
     }
     return null;
   }
 
   @Override
   public void endSearch() {
     readLock.unlock();
   }
 
   @Override
   public Analyzer getAnalyzer() {
     return analyzer;
   }
   
   public File getIndexDir() {
     return indexDir;
   }
 
   public File getDataDir() {
     return dataDir;
   }
 
   private class SearchHolder implements Closeable {
     private Directory directory = null;
     private IndexReader indexReader = null;
     private IndexSearcher indexSearcher = null;
     public void close() {
       IOUtils.closeQuietly(indexSearcher);
       IOUtils.closeQuietly(indexReader);
       IOUtils.closeQuietly(directory);
     }
     public void finalize() {
       close();
     }
   }
 
   @Override
   public void close() throws IOException {
     finalize();
   }
 
 }
