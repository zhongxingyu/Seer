 package org.wyona.yarep.impl.search.lucene;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.apache.avalon.framework.configuration.Configuration;
 
 import org.apache.log4j.Logger;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.TermQuery;
 
 import org.apache.tika.parser.Parser;
 import org.apache.tika.sax.BodyContentHandler;
 import org.apache.tika.sax.WriteOutContentHandler;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.search.Indexer;
 import org.wyona.yarep.core.search.Metadata;
 import org.wyona.yarep.core.search.SearchException;
 import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemNode;
 import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemRepository;
 
 /**
  * Version 2 of Lucene implementation of indexer (mixing fulltext and properties)
  */
 public class LuceneIndexerV2 implements Indexer {
     
     static Logger log = Logger.getLogger(LuceneIndexerV2.class);
     protected LuceneConfig config;
 
     /**
      * @see org.wyona.yarep.core.search.Indexer#configure(Configuration, File, Repository)
      */
     public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException {
         this.config = new LuceneConfig(searchIndexConfig, configFile.getParent(), repo);
     }
     
     /**
      * @see org.wyona.yarep.core.search.Indexer#index(Node)
      */
     public void index(Node node) throws SearchException {
         if (log.isDebugEnabled()) {
             try {
                 log.debug("Index fulltext of node: " + node.getPath());
             } catch(Exception e) {
                 log.warn(e, e);
             }
         }
         index(node, (Metadata)null);
     }
     
     /**
      * @see org.wyona.yarep.core.search.Indexer#index(Node, Metadata)
      */
     public void index(Node node, Metadata metaData) throws SearchException {
         try {
             String path = node.getPath();
             if (config.doIndexRevisions() && org.wyona.yarep.util.YarepUtil.isRevision(node)) {
                 String revisionName = ((org.wyona.yarep.core.Revision)node).getRevisionName();
                 log.debug("Trying to index revision: " + path + " (" + revisionName + "), " + node.getClass().getName());
                 path = path + "#revision=" + revisionName; // TODO: Discuss the separator
             } else {
                 log.warn("DEBUG: Trying to index node: " + path);
                 log.debug("Trying to index node: " + path);
             }
 
             if (metaData != null) {
                 log.warn("This indexer implementation '" + getClass().getName() + "' is currently not making use of the meta data argument!");
             }
 
             Document luceneDoc = getDocument(path);
 
             // INFO: Add fulltext and tika properties
             String mimeType = node.getMimeType();
             if (mimeType != null) {
                 if (log.isDebugEnabled()) log.debug("Mime type: " + mimeType);
                 luceneDoc = addFulltext(node, mimeType, luceneDoc);
             } else {
                 log.warn("Node '" + path + "' has no mime-type set and hence will not be added to fulltext index.");
             }
 
             // INFO: Add properties
             Property[] properties = node.getProperties();
             if (properties != null) {
                 for (int i = 0; i < properties.length; i++) {
                     //log.debug("Add property to fulltext index: " + properties[i].getName());
                     if (properties[i].getValueAsString() != null) {
                         luceneDoc.add(new Field(properties[i].getName(), properties[i].getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
                     }
                 }
             } else {
                 log.info("Node '" + path + "' has no properties.");
             }
 
             // INFO: Update index
             try {
                 updateDocument(getFulltextIndexSearcher(), createFulltextIndexWriter(), path, luceneDoc);
             } catch(org.apache.lucene.store.LockObtainFailedException e) {
                 log.warn("Could not init fulltext IndexWriter (maybe because of existing lock), hence content of node '" + path + "' will not be indexed!");
             }
         } catch (Exception e) {
             log.error(e, e);
             throw new SearchException(e.toString());
         }
     }
     
     /**
      * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node)
      */
     public void removeFromIndex(Node node) {
         IndexWriter indexWriter = null;
         VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
         String nodePath = "Could not get Path of node.";
         try {
             nodePath = node.getPath();
             indexWriter = createFulltextIndexWriter();
             indexWriter.deleteDocuments(new org.apache.lucene.index.Term("_PATH", nodePath));
             indexWriter.close();
         } catch(Exception e) {
             log.warn("Could not init IndexWriter, because of existing lock, hence content of node '" + nodePath + "' will not be deleted from the index!");
             try {
                 indexWriter.close();
             } catch (Exception e2) {
                 log.warn("Could not close indexWriter. Exception: " + e2.getMessage());
             }
         }
     }
     
     /**
      * Get index writer
      */
    protected IndexWriter createFulltextIndexWriter() throws Exception {
         log.debug("Fulltext search index directory: " + config.getFulltextSearchIndexFile());
         return createIndexWriter(config.getFulltextSearchIndexFile(), config.getFulltextAnalyzer());
        // IMPORTANT: This doesn't work within a clustered environment!
        //return this.indexWriter;
    }
 
    /**
     *
     */
    protected IndexWriter createPropertiesIndexWriter() throws Exception {
        return createIndexWriter(config.getPropertiesSearchIndexFile(), config.getPropertyAnalyzer());
        // IMPORTANT: This doesn't work within a clustered environment!
        //return this.propertiesIndexWriter;
    }
    
     /**
      * Init an IndexWriter
      * @param indexDir Directory where the index is located
      */
     private IndexWriter createIndexWriter(File indexDir, Analyzer analyzer) throws Exception {
        IndexWriter iw = null;
        if (indexDir != null) {
            //TODO: if (indexDir.isDirectory()) is probably not needed, try catch (FileNotFoundException e) should be enough
            if (indexDir.isDirectory()) {
                try {
                    iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, false);
                } catch (FileNotFoundException e) {
                    //probably it got an instance of the writer and didn't index anything so it's missing the segemnts files. 
                    //create a new index  
                    iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
                }
            } else {
                iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
            }
            // TODO: iw.setWriteLockTimeout(long ms)
            //log.debug("Max write.lock timeout: " + iw.getDefaultWriteLockTimeout() + " milliseconds");
            return iw;
        }
        return null;
     }
 
    /**
     * Get properties index reader
     */
    public IndexReader getPropertiesIndexReader() throws Exception {
        if (config.getPropertiesSearchIndexFile().exists() && IndexReader.indexExists(config.getPropertiesSearchIndexFile())) {
            return IndexReader.open(config.getPropertiesSearchIndexFile());
        } else {
            log.warn("No properties index exists yet: " + config.getPropertiesSearchIndexFile().getAbsolutePath());
            return null;
        }
    }
 
     /**
      * @see org.wyona.yarep.core.search.Indexer#index(Node, Property)
      */
     public void index(Node node, Property property) throws SearchException {
         try {
             String path = node.getPath();
             if (config.doIndexRevisions() && org.wyona.yarep.util.YarepUtil.isRevision(node)) {
                 String revisionName = ((org.wyona.yarep.core.Revision)node).getRevisionName();
                 log.debug("Index property '" + property.getName() + " of revision: " + path + " (" + revisionName + "), " + node.getClass().getName());
                 path = path + "#revision=" + revisionName; // TODO: Discuss the separator
             } else {
                 log.debug("Index property '" + property.getName() + " of node: " + path);
             }
 
             Document luceneDoc = getDocument(path);
 
             // TODO: Write typed property value to index. Is this actually possible?
             // INFO: As workaround Add the property as string value to the lucene document
             if (property.getValueAsString() != null) {
                 log.debug("Index property '" + property.getName() + "': " + property.getValueAsString());
                 //luceneDoc.add(new Field(property.getName(), new StringReader(property.getValueAsString())));
                 luceneDoc.add(new Field(property.getName(), property.getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
             } else {
                 log.warn("Property '" + property.getName() + "' has null as string value and hence will not be indexed (path: " + path + ")!");
             }
 
             // INFO: Re-add all other properties, whereas either get TermDocs from IndexReader or just use Node.getProperties
             // INFO: Add all other properties of node to lucene doc, whereas this is just a workaround, because the termDocs does not work (please see below)
 //
             Property[] properties = node.getProperties();
             for (int i = 0; i < properties.length; i++) {
                 if (!properties[i].getName().equals(property.getName())) {
                     if (properties[i].getValueAsString() != null) {
                         luceneDoc.add(new Field(properties[i].getName(), properties[i].getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
                     }
                 }
             }
 //
 
             // INFO: Now add lucene document containing all properties to index
             try {
                 updateDocument(getPropertiesIndexSearcher(), createPropertiesIndexWriter(), path, luceneDoc);
             } catch(org.apache.lucene.store.LockObtainFailedException e) {
                 log.warn("Could not init properties IndexWriter (maybe because of existing lock), hence properties of node '" + path + "' will not be indexed!");
             }
         } catch (Exception e) {
             log.error(e, e);
             throw new SearchException(e.getMessage());
         }
     }
   
     /**
      * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node, Property)
      */
     public void removeFromIndex(Node node, Property property) throws SearchException {
         log.warn("TODO: Not implemented yet.");
     }
 
     /**
      * Update document of a particular path within index
      *
      * @param indexSearcher Index searcher to check if document with this particular path already exists
      * @param indexWriter Index writer
      * @param path Path of node with which the fields and values are related to
      * @param document Lucene document containing the new fields and new values
      */
     private void updateDocument(IndexSearcher indexSearcher, IndexWriter indexWriter, String path, Document document) throws Exception {
         // TODO: Synchronize index writing and make sure that index writer is always closed (see LuceneIndexer.java)
         Term pathTerm = new Term("_PATH", path);
         Document oldDoc = null;
 
 /*
         // WARN: Please note that fields which are not stored (e.g. using StringReader) behave differently!
         // Also see http://hrycan.com/2009/11/26/updating-document-fields-in-lucene/
         if (indexSearcher != null) {
             org.apache.lucene.search.TopDocs hits = indexSearcher.search(new TermQuery(pathTerm), 3);
             if (hits.scoreDocs.length <= 0) {
                 log.warn("No matches in the index for the path: " + path);
             } else if (hits.scoreDocs.length > 1) {
                 log.warn("Given path '" + path + "' matches more than one document in the index!");
             } else {
                 log.warn("DEBUG: Retrieve old document and merge with new document: " + path);
                 oldDoc = indexSearcher.doc(hits.scoreDocs[0].doc);
                 java.util.List<Field> updatedFields = document.getFields();
                 for (Field field : updatedFields) {
                     if (oldDoc.getField(field.name()) != null) {
                         log.warn("DEBUG: Update existing field: " + field);
                         oldDoc.removeFields(field.name());
                         oldDoc.add(field);
                     } else {
                         log.warn("DEBUG: Add new field: " + field);
                         oldDoc.add(field);
                     }
 
                     if (field.stringValue() != null) {
                         log.warn("String value: " + field.stringValue());
                     } else if (field.readerValue() != null) {
                         log.warn("Reader value: " + field.readerValue());
                     } else if (field.binaryValue() != null) {
                         log.warn("Binary value: " + field.binaryValue());
                     } else {
                         log.warn("No value!");
                     }
                 }
             }
         } else {
             log.warn("No index seems to exist yet!");
         }
 */
 
         if (indexWriter != null) {
             if (log.isDebugEnabled()) log.debug("Node will be indexed: " + path);
             if (oldDoc != null) {
                 indexWriter.updateDocument(pathTerm, oldDoc); // INFO: Update old document which has been updated with new fields and values
             } else {
                 indexWriter.updateDocument(pathTerm, document); // INFO: Add new document (no old document for path exists)
             }
             indexWriter.optimize(); // TODO: Make this configurable because of performance problem?!
             indexWriter.close();
             //indexWriter.flush();
         } else {
             throw new Exception("IndexWriter is null and hence node will not be indexed: " + path);
             //log.warn("IndexWriter is null and hence node will not be indexed: " + path);
         }
     }
 
     /**
      * Init lucene document
      * @param path Node path for which fields and values are associated with
      */
     private Document getDocument(String path) {
         Document luceneDoc = new Document();
         // INFO: Add path as field such that found properties can be related to a path
         luceneDoc.add(new Field("_PATH", path, Field.Store.YES, Field.Index.UN_TOKENIZED));
         return luceneDoc;
     }
 
     /**
      * Add fulltext to lucene document
      */
     private Document addFulltext(Node node, String mimeType, Document luceneDoc) throws Exception {
         String fullText = null;
 
         // Extract/parse text content:
         Parser parser = config.getTikaConfig().getParser(mimeType);
         if (parser != null) {
             try {
                 org.apache.tika.metadata.Metadata tikaMetaData = new org.apache.tika.metadata.Metadata();
                 tikaMetaData.set("yarep-path", node.getPath());
 
                 StringWriter writer = new StringWriter();
 
 /*
                             // The WriteOutContentHandler writes all character content out to the writer. Please note that Tika also contains various other utility classes to extract content, such as for example the BodyContentHandler (see http://lucene.apache.org/tika/apidocs/org/apache/tika/sax/package-summary.html)
                             parser.parse(node.getInputStream(), new WriteOutContentHandler(writer), tikaMetaData);
                             // WARN: See http://www.mail-archive.com/tika-dev@lucene.apache.org/msg00743.html
                             log.warn("Fulltext generation with WriteOutContentHandler does seem to be buggy (because title and body are not separated with a space): " + fullText);
 */
 
                 // NOTE: The body content handler generates xhtml ... instead just the words ...
                 parser.parse(node.getInputStream(), new BodyContentHandler(writer), tikaMetaData);
                 fullText = writer.toString();
                 writer.close();
 /* INFO: Alternative to using a writer ...
                             BodyContentHandler textHandler = new BodyContentHandler();
                             parser.parse(node.getInputStream(), textHandler, tikaMetaData);
                             fullText = textHandler.toString();
                             log.warn("DEBUG: Body: " + fullText);
 */
 
                 log.debug("Remove all html tags: " + fullText);
                  fullText = fullText.replaceAll("\\<.*?>", " "); // INFO: Please make sure to replace by space, because otherwise words get concatenated and hence cannot be found anymore!
                 //fullText = fullText.replaceAll("\\<.*?>", " ").replace("&#160;", " ");
                 log.debug("Without HTML tags: " + fullText);
 
                 // TODO: Add more meta content to full text
                 String title = tikaMetaData.get(org.apache.tika.metadata.Metadata.TITLE);
                 if (title != null && title.trim().length() > 0) {
                     fullText = fullText + " " + title;
                 }
 
                 String keywords = tikaMetaData.get(org.apache.tika.metadata.Metadata.KEYWORDS);
                 if (keywords != null && keywords.trim().length() > 0) fullText = fullText + " " + keywords;
 
                 String description = tikaMetaData.get(org.apache.tika.metadata.Metadata.DESCRIPTION);
                 if (description != null && description.trim().length() > 0) fullText = fullText + " " + description;
 
                 //log.debug("debug: Fulltext including title and meta: " + fullText);
                 if (fullText != null && fullText.length() > 0) {
                     //luceneDoc.add(new Field(LuceneIndexer.INDEX_PROPERTY_FULL, new StringReader(fullText))); // INFO: http://lucene.apache.org/java/2_0_0/api/org/apache/lucene/document/Field.html#Field(java.lang.String,%20java.io.Reader)
                     //luceneDoc.add(new Field(LuceneIndexer.INDEX_PROPERTY_FULL, fullText, Field.Store.NO, Field.Index.TOKENIZED));
                     luceneDoc.add(new Field(LuceneIndexer.INDEX_PROPERTY_FULL, fullText, Field.Store.YES, Field.Index.TOKENIZED));
                 } else {
                     log.warn("No fulltext has been extracted to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
                 }
 
                 for (int i = 0; i < tikaMetaData.names().length; i++) {
                     String tikaPropName = tikaMetaData.names()[i];
                     if (tikaMetaData.isMultiValued(tikaPropName)) {
                         log.warn("Tika property is multi valued: " + tikaPropName);
                     }
                     luceneDoc.add(new Field("tika_" + tikaPropName, tikaMetaData.get(tikaPropName), Field.Store.YES, Field.Index.TOKENIZED));
                 }
             } catch (Exception e) {
                 log.error("Could not index node " + node.getPath() + ": error while extracting text: " + e, e);
                 // INFO: Don't throw exception in order to be fault tolerant
             }
         } else {
             log.warn("No parser available to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
         }
 
         return luceneDoc;
     }
 
     /**
      * Get fulltext index searcher
      * @return null if no index exists yet
      */
     private IndexSearcher getFulltextIndexSearcher() throws Exception {
         try {
             return new IndexSearcher(config.getFulltextSearchIndexFile().getAbsolutePath());
         } catch(Exception e) {
             log.error(e.getMessage());
             //log.error(e, e);
             return null;
         }
     }
 
     /**
      * Get properties index searcher
      * @return null if no index exists yet
      */
     private IndexSearcher getPropertiesIndexSearcher() throws Exception {
         try {
             return new IndexSearcher(config.getPropertiesSearchIndexFile().getAbsolutePath());
         } catch(Exception e) {
             log.error(e.getMessage());
             //log.error(e, e);
             return null;
         }
     }
 }
