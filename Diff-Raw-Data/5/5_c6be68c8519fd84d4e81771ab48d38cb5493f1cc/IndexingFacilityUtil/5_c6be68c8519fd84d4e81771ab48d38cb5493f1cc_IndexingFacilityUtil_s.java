 package net.cyklotron.cms.search.internal;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.MultiFields;
 import org.apache.lucene.index.Terms;
 import org.apache.lucene.index.TermsEnum;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.util.BytesRef;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.pipeline.ProcessingException;
 
 import net.cyklotron.cms.category.query.CategoryQueryBuilder;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.search.IndexResource;
 import net.cyklotron.cms.search.IndexResourceData;
 import net.cyklotron.cms.search.IndexableResource;
 import net.cyklotron.cms.search.SearchConstants;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.site.SiteResource;
 
 /**
  * Implementation of Indexing
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: IndexingFacilityUtil.java,v 1.5 2005-06-24 08:44:45 pablo Exp $
  */
 public class IndexingFacilityUtil 
 {
     // deps ----------------------------------------------------------------------------------------
 
     private static final String NUM_CHANGES = "numChanges";
 
     /** search service - for managing index resources */
     private SearchService searchService;
     
     /** category query service - for managing query resources */
     private CategoryQueryService categoryQueryService;
 
     /** file service - for managing index files */
     private FileSystem fileSystem;
 
     // config --------------------------------------------------------------------------------------
 
     /** path of base directory for site indexes */
     private String sitesIndexesDirPath;
 
     /** lucene's mergeFactor
      * (number of segments joined in one go, lucene default 10) */
     private int mergeFactor;
 
     /** lucene's minMergeDocs
      * (size of the segment during indexing - minimal number of docs in memory, lucene default 10) */
     private int minMergeDocs;
 
     /** lucene's maxMergeDocs
      * (maximal size of the segment after optimisation, lucene default Integer.MAX_VALUE) */
     private int maxMergeDocs;
 
 
     // local ---------------------------------------------------------------------------------------
 
     /**
      * Creates the indexing utility.
      * @param searchService
      * @param fileSystem
      */
     public IndexingFacilityUtil(SearchService searchService,CategoryQueryService categoryQueryService, FileSystem fileSystem,
          String sitesIndexesDirPath, int mergeFactor, int minMergeDocs, int maxMergeDocs)
     {
         this.searchService = searchService;
         this.categoryQueryService = categoryQueryService;
         this.fileSystem = fileSystem;
         this.sitesIndexesDirPath = sitesIndexesDirPath;
         try
         {
             checkDirectory(sitesIndexesDirPath);
         }
         catch (SearchException e)
         {
             throw new ComponentInitializationError("IndexingFacility: Cannot use base search directory", e);
         }
         this.mergeFactor = mergeFactor; 
         this.minMergeDocs = minMergeDocs;
         this.maxMergeDocs = maxMergeDocs;
     }
 
     // IndexingFacility methods --------------------------------------------------------------------
 
     public String getIndexFilesPath(SiteResource site, String indexName)
         throws SearchException
     {
         String path = sitesIndexesDirPath+"/"+site.getName()+"/"+indexName;
         try
         {
             checkDirectory(path);
         }
         catch (SearchException e)
         {
             throw new SearchException("IndexingFacility: Cannot use directory for index '"+indexName
                +"' in site '"+site.getName()+"'", e);
         }
         return path;
     }
 
     public void createIndexFiles(IndexResource index) throws SearchException
     {
         Directory dir = getIndexDirectory(index);
         synchronized(index)
         {
             IndexWriter indexWriter = openIndexWriter(dir, index, true, "creating empty index files");
             try
             {
                 indexWriter.close();
             }
             catch (IOException e)
             {
                 throw new SearchException(
                     "IndexingFacility: Could not close the index writer for index '"+
                     index.getPath()+"' while creating empty index files", e);
             }
         }
     }
 
     /**
      * Returns a lucene directory for a given index.
      *
      * @param index the index resource
      * @return the index directory object
      */
     public Directory getIndexDirectory(IndexResource index) throws SearchException
     {
         String path = index.getFilesLocation();
         checkDirectory(path);
         return new LedgeFSDirectory(fileSystem, path);
     }
 
     /**
      * Returns a temp lucene directory for a given index.
      *
      * @param index the index resource
      * @return the index directory object
      */
     public Directory getTempIndexDirectory(IndexResource index) throws SearchException
     {
         String path = index.getFilesLocation() + System.nanoTime();
         checkDirectory(path);
         return new LedgeFSDirectory(fileSystem, path);
     }
 
     // IndexWriter management ----------------------------------------------------------------------
     
     /**
      * Opens an index writer and sets configured parameters.
      * 
      * @param dir lucene direcotry with the index to be written
      * @param index index resource representing the index
      * @param createIndex set to <code>true</code> if the index should be newly created (emptied)
      * @param whileMsg a part of the exeception message to inform about preformed operation
      * @return the index writer
      * @throws SearchException on problems with index writer opening 
      */
     public IndexWriter openIndexWriter(Directory dir, IndexResource index, boolean createIndex,
         String whileMsg)
         throws SearchException
     {
         try
         {
             IndexWriter indexWriter = new IndexWriter(dir,
                 getAnalyzer(index), createIndex,
                 IndexWriter.MaxFieldLength.UNLIMITED);
             indexWriter.setMergeFactor(mergeFactor);
             indexWriter.setMaxBufferedDocs(minMergeDocs);
             indexWriter.setMaxMergeDocs(maxMergeDocs);
             return indexWriter;
         }
         catch (IOException e)
         {
             throw new SearchException("IndexingFacility: Could not create IndexWriter for " +
                     "index '"+index.getPath()+"' while "+whileMsg, e);
         }
     }
 
     public Map<String, String> getLastCommitUserData(IndexResource index, IndexWriter indexWriter,
         String whileMsg)
         throws SearchException
     {
         try
         {
            return indexWriter.getReader().getCommitUserData();
         }
         catch(IOException e)
         {
             throw new SearchException("IndexingFacility: Could not create IndexWriter for "
                 + "index '" + index.getPath() + "' while " + whileMsg, e);
         }
     }
 
     /**
      * Closes a given index writer.
      * 
      * @param indexWriter the index writer to be closed.
      * @param index index resource representing the index
      * @param whileMsg a part of the exeception message to inform about preformed operation
      * @throws SearchException on problems with index writer closing 
      */
     public void closeIndexWriter(IndexWriter indexWriter, IndexResource index, String whileMsg)
         throws SearchException
     {
         try
         {
             indexWriter.close();
         }
         catch (IOException e)
         {
             throw new SearchException("IndexingFacility: Could not close IndexWriter for " +
                     "index '"+index.getPath()+"' while "+whileMsg, e);
         }
     }
 
     public void commitIndexWriter(IndexWriter indexWriter, IndexResource index,
         Map<String, String> userData, String whileMsg)
         throws SearchException
     {
         try
         {
             if(userData != null)
             {
                 indexWriter.commit(userData);
             }
             else
             {
                 indexWriter.commit();
             }
         }
         catch(IOException e)
         {
             throw new SearchException("IndexingFacility: Could not commit IndexWriter for "
                 + "index '" + index.getPath() + "' while " + whileMsg, e);
         }
     }
 
     public static Map<String, String> resetChangeCounter()
     {
         Map<String, String> out = new HashMap<String, String>();
         out.put(NUM_CHANGES, "0");
         return out;
     }
 
     public static Map<String, String> incrementChangeCounter(Map<String, String> in, int count)
     {
         Map<String, String> out = new HashMap<String, String>();
         String prev = in.get(NUM_CHANGES);
         if(prev != null)
         {
             try
             {
                 out.put(NUM_CHANGES, Integer.toString(Integer.parseInt(prev) + count));
             }
             catch(NumberFormatException e)
             {
                 throw new IllegalStateException("invalid commit user data in index: numChanges "
                     + prev);
             }
         }
         else
         {
             out.put(NUM_CHANGES, "0");
         }
         return out;
     }
 
     public static int getChangeCounter(Map<String, String> in)
     {
         String prev = in.get(NUM_CHANGES);
         if(prev != null)
         {
             try
             {
                 return Integer.parseInt(prev);
             }
             catch(NumberFormatException e)
             {
                 throw new IllegalStateException("invalid commit user data in index: numChanges "
                     + prev);
             }
         }
         return 0;
     }
 
     // IndexReader management ---------------------------------------------------------------------------------------------
     
     /**
      * Opens an index reader.
      * 
      * @param index index resource representing the index
      * @param whileMsg a part of the exeception message to inform about preformed operation
      * @return the index reader
      * @throws SearchException on problems with index reader opening 
      */
     public IndexReader openIndexReader(IndexResource index, String whileMsg)
         throws SearchException
     {
         try
         {
             Directory dir = getIndexDirectory(index);
             return DirectoryReader.open(dir);
         }
         catch (SearchException e)
         {
             throw new SearchException("IndexingFacility: Cannot get directory for index '"+
                 index.getPath()+"' while "+whileMsg, e);
         }
         catch (IOException e)
         {
             throw new SearchException("IndexingFacility: Cannot get index reader for index '"+
                 index.getPath()+"' while "+whileMsg, e);
         }
     }
     
     /**
      * Closes a given index reader.
      * 
      * @param indexReader the index reader to be closed.
      * @param index index resource representing the index
      * @param whileMsg a part of the exeception message to inform about preformed operation
      * @throws SearchException on problems with index reader closing 
      */
     public void closeIndexReader(IndexReader indexReader, IndexResource index, String whileMsg)
         throws SearchException
     {
         try
         {
             indexReader.close();
         }
         catch (IOException e)
         {
             throw new SearchException("IndexingFacility: Could not close IndexReader for index '"+
                 index.getPath()+"' while "+whileMsg, e);
         }
     }
     
     // ---------------------------------------------------------------------------------------------
 
     public Set getIndexedResourceIds(IndexResource index)
     throws SearchException
     {
         // get index ids and exclude ids from the tree 
         IndexReader indexReader = openIndexReader(index, "getting indexed resources ids");
     
         // get index ids
         Set<Long> ids = new HashSet<>();
         
         try
         {
             final Terms terms = MultiFields.getTerms(indexReader, SearchConstants.FIELD_ID);
             if(terms != null)
             {
                 final TermsEnum termsEnum = terms.iterator(null);
                 while(termsEnum.next() != null)
                 {
                     ids.add(Long.valueOf(termsEnum.term().utf8ToString()));
                 }
             }
         }
         catch(IOException e)
         {
             throw new SearchException("IndexingFacility: Could not get id terms set from '"+
                 index.getPath()+"' while getting indexed resources ids", e);
         }
         
         closeIndexReader(indexReader, index, "getting indexed resources ids");
         
         return ids;
     }
     
     public Set getMissingResourceIds(CoralSession coralSession, IndexResource index)
         throws SearchException
     {
         // get tree ids and exclude ids from the index
         Set indexIds = getIndexedResourceIds(index);
     
         Set missingIds = new HashSet(128);
         // get ids existing only in the tree
         // go recursive on all branches
         List resources = searchService.getIndexedBranches(coralSession, index);
         for (Iterator i = resources.iterator(); i.hasNext();)
         {
             Resource branch = (Resource) (i.next());
             addIds(coralSession, branch, missingIds, indexIds, true);
         }
         // go locally on nodes
         resources = searchService.getIndexedNodes(coralSession, index);
         for (Iterator i = resources.iterator(); i.hasNext();)
         {
             Resource branch = (Resource) (i.next());
             addIds(coralSession, branch, missingIds, indexIds, false);
         }
     
         return missingIds;
     }
     
     private void addIds(CoralSession coralSession, Resource resource, Set ids, Set excludedIdsSet, boolean recursive)
     {
         Long id = resource.getIdObject();
         if(!excludedIdsSet.contains(id))
         {
             ids.add(id);
         }
         if (recursive)
         {
             Resource[] children = coralSession.getStore().getResource(resource);
             for (int i = 0; i < children.length; i++)
             {
                 addIds(coralSession, children[i], ids, excludedIdsSet, recursive);
             }
         }
     }
     
     public Set getDeletedResourcesIds(CoralSession coralSession, IndexResource index)
     throws SearchException
     {
         // get index ids
         Set ids = getIndexedResourceIds(index);
     
         // remove ids existing in the tree
         // go recursive on all branches
         List resources = searchService.getIndexedBranches(coralSession, index);
         for (Iterator i = resources.iterator(); i.hasNext();)
         {
             Resource branch = (Resource) (i.next());
             removeIds(coralSession, branch, ids, true);
         }
         // go locally on nodes
         resources = searchService.getIndexedNodes(coralSession, index);
         for (Iterator i = resources.iterator(); i.hasNext();)
         {
             Resource branch = (Resource) (i.next());
             removeIds(coralSession, branch, ids, false);
         }
     
         return ids;
     }
     
     private void removeIds(CoralSession coralSession, Resource resource, Set ids, boolean recursive)
     {
         ids.remove(resource.getIdObject());
         if (recursive)
         {
             Resource[] children = coralSession.getStore().getResource(resource);
             for (int i = 0; i < children.length; i++)
             {
                 removeIds(coralSession, children[i], ids, recursive);
             }
         }
     }
 
     public Set getDuplicateResourceIds(IndexResource index)
         throws SearchException
     {
         // get index ids and check duplicates 
         IndexReader indexReader = openIndexReader(index, "getting duplicate indexed resources ids");
     
         // duplicate index ids
         Set<Long> duplicateIds = new HashSet<>();
         
         try
         {
             final Terms terms = MultiFields.getTerms(indexReader, SearchConstants.FIELD_ID);
             if(terms != null)
             {
                 final TermsEnum termsEnum = terms.iterator(null); // no reuse
                 BytesRef idTerm;
                 while((idTerm = termsEnum.next()) != null)
                 {
                     if(termsEnum.docFreq() > 1)
                     {
                         duplicateIds.add(Long.valueOf(idTerm.utf8ToString()));
                     }
                 }
             }
         }
         catch(IOException e)
         {
             throw new SearchException("IndexingFacility: Could not get id terms set from '"+
                 index.getPath()+"' while getting duplicate indexed resources ids", e);
         }
         
         closeIndexReader(indexReader, index, "getting duplicate indexed resources ids");
         
         return duplicateIds;
     }
     
     // util ----------------------------------------------------------------------------------------
     
     public Resource getBranch(CoralSession coralSession, IndexResource index, IndexableResource resource)
     {
         Relation nodesXref = searchService.getIndexedNodesRelation(coralSession);
         Relation branchesXref = searchService.getIndexedBranchesRelation(coralSession);
             
         Resource branch = resource;
         while (branch != null)
         {
             if(nodesXref.hasRef(index, branch) || branchesXref.hasRef(index, branch))
             {
                 return branch; // return early to get the most specific (nearest) branch
             }
             else
             {
                 // get more general branch
                 branch = branch.getParent();
             }
         }
         return null;
     }
     
     public Set getQueryIndexResourceIds(CoralSession coralSession, IndexResource index)
         throws SearchException
     {
         IndexResourceData indexData = new IndexResourceData();
         indexData.init(coralSession, index, searchService, categoryQueryService);
         CategoryQueryBuilder parsedQuery;
         Set ids = new HashSet();
         try
         {
             if(!indexData.getCategoriesSelection().getIds().isEmpty())
             {
                 parsedQuery = new CategoryQueryBuilder(coralSession, indexData
                     .getCategoriesSelection(), true);
                 ids = new HashSet(Arrays.asList(categoryQueryService.forwardQuery(coralSession,
                     parsedQuery.getQuery())));
                 return ids;
             }
         }
         catch(ProcessingException e)
         {
             throw new SearchException("IndexingFacility: Could not get id terms set from '"
                 + index.getPath() + "' while forwarding index query", e);
         }
         catch(Exception e)
         {
             throw new SearchException("IndexingFacility: Could not get id terms set from '"
                 + index.getPath() + "' while forwarding index query", e);
         }
         return null;
     }
     
     // implementation ------------------------------------------------------------------------------
 
     /**
      * Checks if a directory with a given path exists, creates it checks if it can be
      * read and written. If not an exception with a proper message is thrown
      *
      * @param path path of a direcotry to be checked
      */
     public void checkDirectory(String path) throws SearchException
     {
         if (!fileSystem.exists(path))
         {
             try
             {
                 fileSystem.mkdirs(path);
             }
             catch (Exception e)
             {
                 throw new SearchException("IndexingFacility: cannot create directory '"+
                     path+"'", e);
             }
         }
         if (!fileSystem.canRead(path))
         {
             throw new SearchException("IndexingFacility: cannot read directory '"+path+"'");
         }
         if (!fileSystem.canWrite(path))
         {
             throw new SearchException("IndexingFacility: cannot write into directory '"+path+"'");
         }
     }
 
     private Analyzer getAnalyzer(IndexResource index)
     {
         return searchService.getAnalyzer((Locale) null);
     }
 }
