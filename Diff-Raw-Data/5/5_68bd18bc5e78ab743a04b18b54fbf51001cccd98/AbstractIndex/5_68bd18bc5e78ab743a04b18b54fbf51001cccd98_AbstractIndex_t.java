 package net.cyklotron.cms.search.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CheckIndex;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 import org.apache.lucene.util.Version;
 import org.jcontainer.dna.Logger;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.filesystem.LocalFileSystemProvider;
 
 public abstract class AbstractIndex<T>
 {
     protected final FileSystem fileSystem;
 
     protected final Logger logger;
 
     private final Analyzer analyzer;
 
     private final Directory directory;
 
     private IndexWriter writer;
 
     private IndexReader reader;
 
     private IndexSearcher searcher;
 
     private Thread updateThread = null;
 
     public AbstractIndex(FileSystem fileSystem, Logger logger, String indexPath)
         throws IOException
     {
         this.fileSystem = fileSystem;
         this.logger = logger;
         File indexLocation = ((LocalFileSystemProvider)fileSystem.getProvider("local"))
             .getFile(indexPath);
         directory = new NIOFSDirectory(indexLocation);
         analyzer = getAnalyzer(fileSystem);
         // remove stale write lock if one exists
         if(directory.fileExists("write.lock"))
         {
             directory.deleteFile("write.lock");
         }
         // if index directory is not there, create a blank index.
         if(!indexLocation.exists())
         {
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, analyzer);
            writer = new IndexWriter(directory, conf);
             writer.close();
         }
         // open reader
         try
         {
             reader = DirectoryReader.open(directory);
         }
         catch(CorruptIndexException e)
         {
             logger.error("corrupt index detected, attempting to recover", e);
             CheckIndex checkIndex = new CheckIndex(directory);
             checkIndex.checkIndex();
             // try to reopen index
             reader = DirectoryReader.open(directory);
         }
         searcher = new IndexSearcher(reader);
         writer = null;
     }
 
     /**
      * Called by the constructor, subclasses may override it to provide custom analyzer.
      * 
      * @param fileSystem ledge files system for loading stop word lists etc.
      * @return Analyzer instance.
      * @throws IOException
      */
     protected Analyzer getAnalyzer(FileSystem fileSystem)
         throws IOException
     {
         return new StandardAnalyzer(Version.LUCENE_30);
     }
 
     protected Searcher getSearcher()
     {
         return searcher;
     }
 
     protected abstract Document toDocument(T item);
 
     protected abstract T fromDocument(Document doc);
 
     protected T singleResult(TopDocs result)
         throws IOException
     {
         if(result.totalHits == 1)
         {
             return fromDocument(searcher.doc(result.scoreDocs[0].doc));
         }
         return null;
     }
 
     protected List<T> results(TopDocs result)
         throws IOException
     {
         List<T> results = new ArrayList<T>();
         for(ScoreDoc scoreDoc : result.scoreDocs)
         {
             results.add(fromDocument(searcher.doc(scoreDoc.doc)));
         }
         return results;
     }
 
     protected List<Term> analyze(String field, String value)
         throws IOException
     {
         List<Term> tokens = new ArrayList<Term>();
         TokenStream ts = analyzer.reusableTokenStream(field, new StringReader(value));
         ts.reset();
         TermAttribute ta = ts.getAttribute(TermAttribute.class);
         while(ts.incrementToken())
         {
             tokens.add(new Term(field, ta.term()));
         }
         ts.end();
         ts.close();
         return tokens;
     }
 
     public boolean isEmpty()
         throws IOException
     {
         return reader.numDocs() == 0;
     }
 
     /**
      * Returns all terms in given field of location index.
      * 
      * @param field field name.
      * @return list of distinct terms in the given field;
      * @throws IOException on index access problems.
      */
     public List<String> getAllTerms(String field)
     {
         try
         {
             List<String> values = new ArrayList<String>();
             TermEnum termEnum = reader.terms(new Term(field, ""));
             while(termEnum.next())
             {
                 Term term = termEnum.term();
                 if(!term.field().equals(field))
                 {
                     break;
                 }
                 values.add(term.text());
             }
             return values;
         }
         catch(IOException e)
         {
             logger.error("index access error", e);
             return Collections.emptyList();
         }
     }
 
     public synchronized void startUpdate()
         throws IOException
     {
         if(updateThread != null)
         {
             throw new IllegalStateException("update in progress");
         }
         updateThread = Thread.currentThread();
         writer = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.LIMITED);
         writer.deleteAll();
     }
 
     public synchronized void addItem(T item)
         throws IOException
     {
         if(updateThread != Thread.currentThread())
         {
             throw new IllegalStateException("update in progress");
         }
         writer.addDocument(toDocument(item));
     }
 
     public synchronized void endUpdate()
         throws CorruptIndexException, IOException
     {
         if(updateThread != Thread.currentThread())
         {
             throw new IllegalStateException("update in progress");
         }
         writer.optimize();
         writer.commit();
         writer.close();
         writer = null;
 
         IndexReader newReader = reader.reopen();
         IndexReader oldReader = reader;
         reader = newReader;
         oldReader.close();
 
         searcher = new IndexSearcher(reader);
 
         updateThread = null;
     }
 
     /**
      * Checks whether a Location exists with given field exactly matching a value.
      * 
      * @param field field name.
      * @param value field value.
      * @return boolean if at least one exact match exits.
      * @throws IOException on index access problems.
      */
     public boolean exactMatchExists(String field, String value)
         throws IOException
     {
         TopDocs result = searcher.search(new TermQuery(new Term(field, value)), 1);
         return result.totalHits > 0;
     }
 }
