 /**
  * This is the core class that wraps Lucene and provides an interface for the application to 
  * issue commands to the search engine
  */
 package com.vmware.armyants;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.FieldType;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 /**
  * @author rukmani
  */
 public class LuceneIndexer {
 	
 	private static final Logger logger = LoggerFactory.getLogger(LuceneIndexer.class);
 	// The place from where you fetch the documents
 	private DocStore repo;
 	// The directory where Lucene will store its indexes - could be jdbc/inmemory/any db
 	private Directory dir;
 	private Analyzer analyzer;
 	
 	
 	public LuceneIndexer(DocStore repo) {
 		this.repo = repo;
 		this.repo.createDocStore(DocStore.CIVIC_COMMONS_COLLECTION);
 		this.repo.createDocStore(DocStore.RFP_COLLECTION);
 		analyzer = new StandardAnalyzer(Version.LUCENE_40);
 	}
 
 	/**
 	 * Takes docs from the repository and indexes them. This has to be done
 	 * only once
 	 * @throws IOException 
 	 * @throws LockObtainFailedException 
 	 * @throws CorruptIndexException 
 	 */
 	public void indexDocs(String collection) throws CorruptIndexException, LockObtainFailedException, IOException {
 		dir = new RAMDirectory();
 		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
 		IndexWriter writer = new IndexWriter(dir, config);
 		
 		for (Iterator<DocType> it = repo.retrieveDocs(collection); it.hasNext();) {
 			logger.info("added doc");
 			addDoc(writer, it.next());
 		}
 		writer.close();
 	}
 	
 	private void addDoc(IndexWriter writer, DocType document) throws IOException {
 		Document luceneDoc = new Document();
 		FieldType type = new FieldType();
 		type.setIndexed(true);
 		luceneDoc.add(new Field("title", document.getName(), type));
 		luceneDoc.add(new Field("body", document.getBody(), Field.Store.YES, Field.Index.ANALYZED));
 		writer.addDocument(luceneDoc);
 	}
 	
 	/**
 	 * The main search method
 	 * @param query
 	 * @return
 	 * @throws ParseException 
 	 * @throws IOException 
 	 */
 	public String[] search(String query) throws ParseException, IOException {
 		Query q = new QueryParser(Version.LUCENE_40, "body", analyzer).parse(query);
 		logger.info(q.toString());
 		int hitsPerPage = 10;
 	    DirectoryReader reader = DirectoryReader.open(dir);
 	    IndexSearcher searcher = new IndexSearcher(reader);
 	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
 	    searcher.search(q, collector);
 	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
 	    logger.info("# score docs = " + hits.length);
 	    String results[] = new String[hits.length];
 	    for(int i=0; i<hits.length; ++i) {
 	        int docId = hits[i].doc;
 	        Document d = searcher.doc(docId);
 	        results[i] = d.get("body");
 	        logger.info("collecting result");
 	      }
 	    return results;
 	}
 	
 	public void addRFPToStore(DocType doc) {
 		repo.addDocsToStore(DocStore.RFP_COLLECTION,doc.getName(), doc.getBody());
 		logger.info("added RFP");
 	}
 }
