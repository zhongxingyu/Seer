 package cinnamon.index;
 
 import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
 
 public interface Indexer {
 
 	void indexObject(ContentContainer xml, Document doc, String fieldname, String searchString, Boolean multipleResults);
    Field.Store getStore();
    void setStore(Field.Store store);
 	
 }
