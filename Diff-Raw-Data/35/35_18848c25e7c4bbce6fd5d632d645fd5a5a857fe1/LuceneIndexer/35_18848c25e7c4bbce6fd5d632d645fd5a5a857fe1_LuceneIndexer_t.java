 /*******************************************************************************
  * * Copyright 2011 Impetus Infotech.
  *  *
  *  * Licensed under the Apache License, Version 2.0 (the "License");
  *  * you may not use this file except in compliance with the License.
  *  * You may obtain a copy of the License at
  *  *
  *  *      http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  ******************************************************************************/
 package com.impetus.kundera.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 import com.impetus.kundera.Constants;
 import com.impetus.kundera.metadata.model.EmbeddedColumn;
 import com.impetus.kundera.metadata.model.EntityMetadata;
 import com.impetus.kundera.property.PropertyAccessException;
 import com.impetus.kundera.property.PropertyAccessorHelper;
 
 /**
  * Provides indexing functionality using lucene library
  * 
  * @author amresh.singh
  */
 public class LuceneIndexer extends DocumentIndexer
 {
     /** log for this class. */
     private static Log log = LogFactory.getLog(LuceneIndexer.class);
 
     private IndexWriter w;
 
     private IndexReader reader;
 
     private Directory index;
    
    private boolean isInitialized;
 
     /**
      * @param client
      * @param analyzer
      */
     public LuceneIndexer(Analyzer analyzer)
     {
         super(analyzer);
         try
         {
 
            index = new RAMDirectory();/* FSDirectory.open(getIndexDirectory()) */;
//            isInitialized
             /* writer */
             w = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_34, analyzer));
             /* reader = */
             w.getConfig().setRAMBufferSizeMB(100);
         }
         catch (CorruptIndexException e)
         {
             throw new IndexingException(e.getMessage());
         }
         catch (LockObtainFailedException e)
         {
             throw new IndexingException(e.getMessage());
         }
         catch (IOException e)
         {
             throw new IndexingException(e.getMessage());
         }
     }
 
     /**
      * Added for HBase support.
      * 
      * @return default index writer
      */
     private IndexWriter getIndexWriter()
     {
         return w;
     }
 
     /**
      * Returns default index reader.
      * 
      * @return index reader.
      */
     private IndexReader getIndexReader()
     {
         if (reader == null)
         {
             try
             {
                if(!isInitialized)
                {
                    Directory sourceDir = FSDirectory.open(getIndexDirectory()) ;
                    sourceDir.copy(sourceDir, index, true);
                    isInitialized = true;
                }
                 reader = IndexReader.open(
                 /* FSDirectory.open(getIndexDirectory()) */index, true);
             }
             catch (CorruptIndexException e)
             {
                 throw new IndexingException(e.getMessage());
             }
             catch (IOException e)
             {
                 throw new IndexingException(e.getMessage());
             }
         }
         return reader;
     }
 
     /**
      * Creates a Lucene index directory if it does not exist.
      * 
      * @return the index directory
      */
     private File getIndexDirectory()
     {
         String filePath = System.getProperty("user.home") + "/" + Constants.LUCENE_INDEX_DIRECTORY_NAME;
         File file = new File(filePath);
         if (!file.isDirectory())
         {
             file.mkdir();
         }
         return file;
     }
 
     @Override
     public final void index(EntityMetadata metadata, Object object)
     {
 
         if (!metadata.isIndexable())
         {
             return;
         }
 
         log.debug("Indexing @Entity[" + metadata.getEntityClazz().getName() + "] " + object);
 
         Document currentDoc = null;
         Object embeddedObject = null;
         // In case defined entity is Super column family.
         // we need to create seperate lucene document for indexing.
         if (metadata.getType().equals(EntityMetadata.Type.SUPER_COLUMN_FAMILY))
         {
             Map<String, EmbeddedColumn> superColMap = metadata.getEmbeddedColumnsMap();
 
             for (String superColumnName : superColMap.keySet())
             {
                 EmbeddedColumn superColumn = superColMap.get(superColumnName);
                 try
                 {
 
                     embeddedObject = PropertyAccessorHelper.getObject(object, superColumn.getField());
                     // if embeddedObject is not set.
                     if (embeddedObject == null)
                     {
                         return;
                     }
                     if (embeddedObject instanceof Collection<?>)
                     {
                         for (Object obj : (Collection<?>) embeddedObject)
                         {
                             currentDoc = prepareDocument(metadata, object, superColumnName);
                             indexSuperColumn(metadata, object, currentDoc, obj, superColumn);
                         }
                         return;
                     }
                     else
                     {
                         currentDoc = prepareDocument(metadata, object, superColumnName);
                     }
                 }
                 catch (PropertyAccessException e)
                 {
                     log.error("Error while accesing embedded Object:" + superColumnName);
                 }
                 indexSuperColumn(metadata, object, currentDoc,
                         metadata.isEmbeddable(embeddedObject.getClass()) ? embeddedObject : object, superColumn);
             }
         }
         else
         {
             currentDoc = new Document();
             prepareIndexDocument(metadata, object, currentDoc);
             addIndexProperties(metadata, object, currentDoc);
             indexDocument(metadata, currentDoc);
         }
 
         // TODO: Sadly this required to keep lucene happy, in case of indexing
         // and searching with same entityManager.
         // Other alternative would be to issue flush on each search
         try
         {
             w.commit();
            isInitialized =  true;
         }
         catch (CorruptIndexException e)
         {
             throw new IndexingException(e.getMessage());
         }
         catch (IOException e)
         {
             throw new IndexingException(e.getMessage());
         }
     }
 
     @Override
     public final void unindex(EntityMetadata metadata, String id)
     {
         log.debug("Unindexing @Entity[" + metadata.getEntityClazz().getName() + "] for key:" + id);
         try
         {
             /* String indexName, Query query, boolean autoCommit */
             getIndexWriter().deleteDocuments(new Term(KUNDERA_ID_FIELD, getKunderaId(metadata, id)));
         }
         catch (CorruptIndexException e)
         {
             throw new IndexingException(e.getMessage());
         }
         catch (IOException e)
         {
             throw new IndexingException(e.getMessage());
         }
     }
 
     @SuppressWarnings("deprecation")
     @Override
     public final Map<String, String> search(String luceneQuery, int start, int count)
     {
 
         getIndexReader();
         if (Constants.INVALID == count)
         {
             count = 100;
         }
 
         log.debug("Searching index with query[" + luceneQuery + "], start:" + start + ", count:" + count);
 
         // Set<String> entityIds = new HashSet<String>();
         Map<String, String> indexCol = new HashMap<String, String>();
 
         if (reader == null)
         {
             throw new RuntimeException("Index reader is not initialized!");
         }
         /*
          * org.apache.lucene.index.IndexReader indexReader = null;
          * 
          * try {
          * 
          * indexReader = getIndexReader();
          * 
          * } catch (Exception e) { throw new IndexingException(e.getMessage());
          * }
          */IndexSearcher searcher = new IndexSearcher(reader);
 
         QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, DEFAULT_SEARCHABLE_FIELD, analyzer);
         try
         {
             Query q = qp.parse(luceneQuery);
             TopDocs docs = searcher.search(q, count);
 
             int nullCount = 0;
             // Assuming Supercol will be null in case if alias only.
             // This is a quick fix
             for (ScoreDoc sc : docs.scoreDocs)
             {
                 Document doc = searcher.doc(sc.doc);
                 String entityId = doc.get(ENTITY_ID_FIELD);
                 String superCol = doc.get(SUPERCOLUMN_INDEX);
                 if (superCol == null)
                 {
                     superCol = "SuperCol" + nullCount++;
                 }
                 indexCol.put(superCol, entityId);
             }
         }
         catch (ParseException e)
         {
             new IndexingException(e.getMessage());
         }
         catch (IOException e)
         {
             new IndexingException(e.getMessage());
         }
 
         // log.debug("Result[" + entityIds + "]");
         return indexCol;
     }
 
     /**
      * Indexes document. For Cassandra it uses Lucandra library, for others it
      * simply indexes into file system using Lucene
      * 
      * @param metadata
      *            the metadata
      * @param document
      *            the document
      */
     public void indexDocument(EntityMetadata metadata, Document document)
     {
 
         log.debug("Indexing document: " + document + " for " + metadata.getDBType());
 
         log.debug("Indexing document in file system using lucene: " + document);
         indexDocumentUsingLucene(document);
 
     }
 
     /**
      * Indexes document in file system using lucene
      * 
      * @param document
      * @throws CorruptIndexException
      * @throws IOException
      */
     public void indexDocumentUsingLucene(Document document)
     {
         IndexWriter w = getIndexWriter();
         try
         {
             // w.setR
             w.addDocument(document, super.analyzer);
             // w.optimize();
             // w.commit();
             // w.close();
         }
         catch (CorruptIndexException e)
         {
             log.error("Error while indexing document " + document + " into Lucene. Details:" + e.getMessage());
         }
         catch (IOException e)
         {
             log.error("Error while indexing document " + document + " into Lucene. Details:" + e.getMessage());
         }
     }
 
     /**
      * Close of transaction.
      */
     public void close()
     {
         try
         {
             if (w != null)
             {
                 w.commit();
                 w.close();
                 index.copy(index, FSDirectory.open(getIndexDirectory()), true);
             }
         }
 
         catch (CorruptIndexException e)
         {
             log.error("Error while indexing document " + " into Lucene. Details:" + e.getMessage());
         }
         catch (IOException e)
         {
             log.error("Error while indexing document  into Lucene. Details:" + e.getMessage());
         }
     }
 
     @Override
     public void flush()
     {
         try
         {
             if (w != null)
             {
 
                 w.commit();
                 // w.close();
                 index.copy(index, FSDirectory.open(getIndexDirectory()), false);
             }
         }
         catch (CorruptIndexException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 }
