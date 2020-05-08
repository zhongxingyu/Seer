 package cinnamon.index;
 
 import org.apache.lucene.document.Document;
 
 public interface Indexer {
 
 	void indexObject(ContentContainer xml, Document doc, String fieldname, String searchString, Boolean multipleResults);
 	
 }
