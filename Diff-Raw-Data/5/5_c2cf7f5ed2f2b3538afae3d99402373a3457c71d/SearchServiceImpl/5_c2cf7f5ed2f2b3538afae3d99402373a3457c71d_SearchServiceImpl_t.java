 package net.cyklotron.cms.search.internal;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.jcontainer.dna.Configuration;
 import org.jcontainer.dna.ConfigurationException;
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.authentication.UserManager;
 import org.objectledge.context.Context;
 import org.objectledge.coral.datatypes.ResourceList;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityExistsException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.coral.table.filter.PathFilter;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.logging.LoggingConfigurator;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.statistics.AbstractMuninGraph;
 import org.objectledge.statistics.MuninGraph;
 import org.objectledge.statistics.RoundRobinAverage;
 import org.objectledge.statistics.StatisticsProvider;
 import org.objectledge.table.TableFilter;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.CmsNodeResourceImpl;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.search.IndexResource;
 import net.cyklotron.cms.search.IndexResourceImpl;
 import net.cyklotron.cms.search.IndexingFacility;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.RootResource;
 import net.cyklotron.cms.search.RootResourceImpl;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchingFacility;
 import net.cyklotron.cms.search.XRefsResource;
 import net.cyklotron.cms.search.analysis.PerFieldAnalyzer;
 import net.cyklotron.cms.search.analysis.StempelStemmerFactory;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * Implementation of Search Service
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: SearchServiceImpl.java,v 1.16 2007-11-18 21:23:23 rafal Exp $
  */
 public class SearchServiceImpl 
     implements SearchService, Startable
 {
     /** resources relation name */
     public static final String NODES_RELATION_NAME = "search.IndexedNodes";
     
     /** rc relation name */
     public static final String BRANCHES_RELATION_NAME = "search.IndexedBranches";
     
     /** path to stop words files location */
     private final String STOPWORDS_LOCATION = "/net/cyklotron/cms/search/";
 
     /** stop words default file */
     private final String STOPWORDS_DEFAULT = "stopwords-pl_PL.txt";
     
     /** stop words encoding */
     private final String STOPWORDS_ENCODING = "UTF-8";
     
     /** the context */
     private Context context;
     
     /** configuration */
     private Configuration config;
     
     /** logging facility */
     private Logger log;
     
         /** file service - for managing indexes */
     private FileSystem fileSystem;
 
     /** site service */
     private SiteService siteService;
     
     private PreferencesService preferencesService;
     
     private CategoryService categoryService;
     
     private UserManager userManager;
     
     private IntegrationService integrationService;
     
     /** resource containing x-references used by search */
     private XRefsResource searchXRefs;
     
     private CoralSessionFactory sessionFactory;
 
     /** system root subject */
     private Subject rootSubject;
 
     /** the searching facility. */
     private SearchingFacility searchingFacility;
 
     /** the indexing facility. */
     private IndexingFacility indexingFacility;
     
     private String[] acceptedPaths;
     
     private Relation branchesRelation;
     
     private Relation nodesRelation;    
 
     private final Statistics statistics;
     
     private final PerformanceMonitor performanceMonitor;
 
     private final StempelStemmerFactory stemmerFactory;
 
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Starts the service - the search service must be started on broker start in order to listen
      * to resource tree changes.
      */
     public SearchServiceImpl(Configuration config, Logger logger, Context context,
         CoralSessionFactory sessionFactory, FileSystem fileSystem, SiteService siteService,
         CategoryService categoryService, CategoryQueryService categoryQueryService,
         UserManager userManager, PreferencesService preferencesService,
         IntegrationService integrationService, LoggingConfigurator loggingConfigurator,
         SearchServiceImpl.Statistics statistics, StempelStemmerFactory stemmerFactory)
         throws ConfigurationException
     {
         this.config = config;
         this.log = logger;
         this.fileSystem = fileSystem;
         this.siteService = siteService;
         this.sessionFactory = sessionFactory;
         this.context = context;
         this.preferencesService = preferencesService;
         this.categoryService = categoryService;
         this.userManager = userManager;
         this.integrationService = integrationService;
         this.statistics = statistics;
         this.stemmerFactory = stemmerFactory;
         if(config.getChild("performance", false) != null)
         {
             this.performanceMonitor = new PerformanceMonitor(config, loggingConfigurator);
         }
         else
         {
             this.performanceMonitor = null;
         }
         Configuration[] paths = config.getChildren("accepted_path");
         acceptedPaths = new String[paths.length];
         for(int i = 0; i < paths.length; i++)
         {
             acceptedPaths[i] = paths[i].getValue();
         }
         // prepare indexing facility (registers listeners)
         indexingFacility = new IndexingFacilityImpl(context, sessionFactory,  log, this, fileSystem,
             preferencesService, categoryService,categoryQueryService, userManager, integrationService);
         // prepare searching facility
         searchingFacility = new SearchingFacilityImpl(log, indexingFacility);
     }
 
     public void start()
     {
         CoralSession coralSession = null;
         try
         {
             coralSession = sessionFactory.getRootSession();
             this.removeStaleWriteLocks(coralSession);
         }
         catch(SearchException e)
         {
             throw new RuntimeException(e);
         }
         catch(InvalidResourceNameException e)
         {
             throw new RuntimeException(e);
         }
         finally
         {
             if(coralSession != null)
             {
                 coralSession.close();
             }
         }
     }
     
     public void stop()
     {
         
     }
     
     // search service ////////////////////////////////////////////////////////
 
     public Configuration getConfiguration()
     {
         return config;
     }
     
     public IndexingFacility getIndexingFacility()
     {
         return indexingFacility;
     }
 
     public SearchingFacility getSearchingFacility()
     {
         return searchingFacility;
     }
     
     public Resource getIndexesRoot(CoralSession coralSession, SiteResource site) throws SearchException
     {
         Resource[] roots = null;
         if (site != null)
         {
             Resource searchRoot = getSearchRoot(coralSession, site);
             roots = coralSession.getStore().getResource(searchRoot, "indexes");
             if (roots.length == 0)
             {
                 try
                 {
                     return CmsNodeResourceImpl.createCmsNodeResource(coralSession, "indexes",
                         searchRoot);
                 }
                 catch(InvalidResourceNameException e)
                 {
                     throw new RuntimeException("unexpected exception", e);
                 }
             }
             if (roots.length > 1)
             {
                 throw new SearchException("multiple indexes roots for site " + site.getName());
             }
         }
         return roots[0];
     }
 
     public Resource getPoolsRoot(CoralSession coralSession, SiteResource site) throws SearchException
     {
         Resource[] roots = null;
         if (site != null)
         {
             Resource searchRoot = getSearchRoot(coralSession, site);
             roots = coralSession.getStore().getResource(searchRoot, "pools");
             if (roots.length == 0)
             {
                 try
                 {
                     return CmsNodeResourceImpl.createCmsNodeResource(coralSession, "pools",
                         searchRoot);
                 }
                 catch(InvalidResourceNameException e)
                 {
                     throw new RuntimeException("unexpected exception", e);
                 }
             }
             if (roots.length > 1)
             {
                 throw new SearchException("multiple index pools roots for site " + site.getName());
             }
         }
         return roots[0];
     }
 
     public RootResource getSearchRoot(CoralSession coralSession, SiteResource site) throws SearchException
     {
         Resource[] roots = null;
         roots = coralSession.getStore().getResource(site, "search");
         if (roots.length == 1)
         {
             return (RootResource)roots[0];
         }
         if (roots.length == 0)
         {
             try
             {
                 try
                 {
                     return RootResourceImpl.createRootResource(coralSession, "search", site,
                         new DefaultParameters());
                 }
                 catch(InvalidResourceNameException e)
                 {
                     throw new RuntimeException("unexpected exception", e);
                 }
             }
             catch (ValueRequiredException e)
             {
                 throw new SearchException("ValueRequiredException: ", e);
             }
         }
         throw new SearchException("Too many search root resources for site: " + site.getName());
     }
 
     public IndexResource createIndex(CoralSession coralSession, SiteResource site, String name) 
         throws SearchException, InvalidResourceNameException
     {
         Resource parent = getIndexesRoot(coralSession, site);
         if (coralSession.getStore().getResource(parent, name).length > 0)
         {
             throw new SearchException("cannot create many indexes with the same name '" + name + "'");
         }
 
         String indexDirectoryPath = indexingFacility.getIndexFilesPath(site, name);
 
         IndexResource index = null;
         try
         {
             index = IndexResourceImpl.createIndexResource(coralSession, name, parent, indexDirectoryPath);
         }
         catch (ValueRequiredException e)
         {
             throw new SearchException("ValueRequiredException: ", e);
         }
         return index;
     }
 
     public void deleteIndex(CoralSession coralSession,IndexResource index) throws SearchException
     {
         // save some data for later
         String indexResourcePath = index.getPath();
         String indexDirectoryPath = index.getFilesLocation();
 
         // remove from pools
         /* WARN: this is very unefficient!!  Maybe we should introduce an XRef for
          * index - pool relation, this would speed up index deleting
          *
          * Use a FIND RESOURCE FROM RESOURCECLASS=''; and store this prepared query.
          */
         SiteResource[] sites = siteService.getSites(coralSession);
         for (int i = 0; i < sites.length; i++)
         {
             Resource parent = getPoolsRoot(coralSession, sites[i]);
             Resource[] pools = coralSession.getStore().getResource(parent);
             for (int j = 0; j < pools.length; j++)
             {
                 Resource res = pools[j];
                 if(res instanceof PoolResource)
                 {
                     PoolResource pool = (PoolResource) (res);
                     if(pool.getIndexes() != null)
                     {
                         ResourceList indexes = new ResourceList(sessionFactory, pool.getIndexes());
                         if(indexes != null)
                         {
                             if (indexes.remove(index))
                             {
                                 pool.setIndexes(indexes);
                                 pool.update();
                             }
                         }
                     }
                 }
             }
         }
 
 
         try
         {
             // remove from index - branch x-ref
             List empty = new ArrayList();
             setIndexedNodes(coralSession, index, empty);
             setIndexedBranches(coralSession, index, empty);
             // delete index resource
             coralSession.getStore().deleteResource(index);
         }
         catch (EntityInUseException e)
         {
             throw new SearchException("cannot remove index resource", e);
         }
 
         // delete index files
         try
         {
             if(fileSystem.exists(indexDirectoryPath))
             {
                 String[] files = fileSystem.list(indexDirectoryPath);
                 for (int i = 0; i < files.length; i++)
                 {
                     fileSystem.delete(indexDirectoryPath + "/" + files[i]);
                 }
                 fileSystem.delete(indexDirectoryPath);
             }
         }
         catch (IOException e)
         {
             throw new SearchException("cannot delete index files '" + indexResourcePath + "'", e);
         }
     }
 
     public IndexResource[] getIndexes(CoralSession coralSession, Resource res)
     {
         Set indexesSet = new HashSet(); 
         getNodeIndexesInternal(coralSession, res, indexesSet);
         getBranchIndexesInternal(coralSession, res, indexesSet);
 
         IndexResource[] indexes = new IndexResource[indexesSet.size()];
         indexes = (IndexResource[]) (indexesSet.toArray(indexes));
         return indexes;
     }
 
     public IndexResource[] getBranchIndexes(CoralSession coralSession, Resource res)
     {
         Set indexesSet = new HashSet(); 
         getBranchIndexesInternal(coralSession, res, indexesSet);
 
         IndexResource[] indexes = new IndexResource[indexesSet.size()];
         indexes = (IndexResource[]) (indexesSet.toArray(indexes));
         return indexes;
     }
 
     public IndexResource[] getNodeIndexes(CoralSession coralSession, Resource res)
     {
         Set indexesSet = new HashSet(); 
         getNodeIndexesInternal(coralSession, res, indexesSet);
 
         IndexResource[] indexes = new IndexResource[indexesSet.size()];
         indexes = (IndexResource[]) (indexesSet.toArray(indexes));
         return indexes;
     }
     
     private void getNodeIndexesInternal(CoralSession coralSession, Resource res, Set indexesSet)
     {
         // add indexes indexing the resource as a node
         Relation relation = getIndexedNodesRelation(coralSession).getInverted();
         Resource[] tmp = relation.get(res);
         IndexResource[] indexes = new IndexResource[tmp.length];
         System.arraycopy(tmp, 0, indexes, 0, tmp.length);
         indexesSet.addAll(Arrays.asList(indexes));
     }
 
     private void getBranchIndexesInternal(CoralSession coralSession, Resource resource, Set indexesSet)
     {
         Resource[] tmp;
         IndexResource[] indexes;
         // add indexes indexing the resource as a part of a branch
         Relation relation = getIndexedBranchesRelation(coralSession).getInverted();
         while (resource != null)
         {
             tmp = relation.get(resource);
             indexes = new IndexResource[tmp.length];
             System.arraycopy(tmp, 0, indexes, 0, tmp.length);
             indexesSet.addAll(Arrays.asList(indexes));
             resource = resource.getParent();
         }
     }
 
     public Analyzer getAnalyzer(Locale locale)
     {
         // Implement it using an Analyzer registry for languages and language field in index. \\
         // analyser registry should be refactored out as an external component
         try
         {
             String path = null;
             // TODO guess this should be called factory and got in on constructor as dependency
             PerFieldAnalyzer perFieldAnalyzer = new PerFieldAnalyzer(fileSystem, STOPWORDS_ENCODING);
             if(locale != null && fileSystem.exists(STOPWORDS_LOCATION + locale.getDisplayName()))
             {
                 path = STOPWORDS_LOCATION + "/" + locale.getDisplayName();
             }
             else
             {
                 path = STOPWORDS_LOCATION + STOPWORDS_DEFAULT;
             }
             return perFieldAnalyzer.createPerFieldAnalyzer(path,
                 stemmerFactory.createStempelStemmer());
         }
         catch(UnsupportedEncodingException e)
         {
            throw new IllegalStateException("UnsupportedEncodingException" + e, e);
         }
         catch(IOException e)
         {
            throw new IllegalStateException("File not exists exception" + e, e);
         }
     }
 
     // /////////////////////////////////////////////////////////////////////////////////////////////
 
     public XRefsResource getXRefsResource(CoralSession coralSession)
     {
         if(searchXRefs == null)
         {
             Resource[] ress = coralSession.getStore().getResourceByPath("/cms/search");
             if (ress.length == 0)
             {
                 throw new ComponentInitializationError("cannot find x-references resource for search service");
             }
             else if (ress.length > 1)
             {
                 throw new ComponentInitializationError("too many x-reference resources for search service");
             }
             searchXRefs = (XRefsResource)ress[0];
         }
         return searchXRefs;
     }
     
     public List getIndexedBranches(CoralSession coralSession, IndexResource index)
     {
         return getXRef(getIndexedBranchesRelation(coralSession), index);
     }
 
     public void setIndexedBranches(CoralSession coralSession, IndexResource index, List resources)
     {
         Relation xref = getIndexedBranchesRelation(coralSession);
         modifyXRef(coralSession, xref, index, resources);
     }
 
     public List getIndexedNodes(CoralSession coralSession, IndexResource index)
     {
         return getXRef(getIndexedNodesRelation(coralSession), index);
     }
 
     public void setIndexedNodes(CoralSession coralSession, IndexResource index, List resources)
     {
         Relation xref = getIndexedNodesRelation(coralSession);
         modifyXRef(coralSession, xref, index, resources);
     }
 
 
     private List getXRef(Relation xref, Resource res)
     {
         return Arrays.asList(xref.get(res));
     }
 
     private void modifyXRef(CoralSession coralSession, Relation xref, IndexResource index, List resources)
     {
         RelationModification diff = new RelationModification();
         diff.remove(index);
         Resource[] ress = new Resource[resources.size()];
         ress = (Resource[]) (resources.toArray(ress));
         diff.add(index, ress);
         coralSession.getRelationManager().updateRelation(xref, diff);
     }
 
     // path filtering //////////////////////////////////////////////////////////////////////////////
 
     public TableFilter getBranchFilter(SiteResource site)
     {
         return new PathFilter(site, acceptedPaths);
     }
     
     
     /**
      * Return the resource-resource relation.
      * 
      * @param coralSession the coral session.
      * @return the relation.
      */
     public Relation getIndexedNodesRelation(CoralSession coralSession)
     {     
         if(nodesRelation != null)
         {
             return nodesRelation;
         }
         try
         {
             nodesRelation = coralSession.getRelationManager().
                                    getRelation(NODES_RELATION_NAME);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new IllegalStateException("ambiguous roles relation");
         }
         catch(EntityDoesNotExistException e)
         {
             //ignore it.
         }
         if(nodesRelation != null)
         {
             return nodesRelation;
         }
         try
         {
             createRelation(coralSession, NODES_RELATION_NAME);
         }
         catch(EntityExistsException e)
         {
             throw new IllegalStateException("the security relation already exists");
         }
         return nodesRelation;
     }
 
     /**
      * 
      * 
      * @param coralSession the coral session.
      * @return the rc relation.
      */
     public Relation getIndexedBranchesRelation(CoralSession coralSession)
     {     
         if(branchesRelation != null)
         {
             return branchesRelation;
         }
         try
         {
             branchesRelation = coralSession.getRelationManager().
                                    getRelation(BRANCHES_RELATION_NAME);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new IllegalStateException("ambiguous roles relation");
         }
         catch(EntityDoesNotExistException e)
         {
             //ignore it.
         }
         if(branchesRelation != null)
         {
             return branchesRelation;
         }
         try
         {
             createRelation(coralSession, BRANCHES_RELATION_NAME);
         }
         catch(EntityExistsException e)
         {
             throw new IllegalStateException("the security relation already exists");
         }
         return branchesRelation;
     }
     
     private void removeStaleWriteLocks(CoralSession coralSession)
         throws SearchException, InvalidResourceNameException
     {
         Resource[] sitesRoot = coralSession.getStore().getResourceByPath("/cms/sites");
         if(sitesRoot.length == 0)
         {
             // apparently, service is initialized by the installer
             log.warn("failed to lookup sites root, skipping stale write lock removal");
             return;
         }
         SiteResource[] sites = siteService.getSites(coralSession);
         for(int i = 0; i < sites.length; i++)
         {
             Resource parent = getIndexesRoot(coralSession, sites[i]);
             Resource[] indexes = coralSession.getStore().getResource(parent);
             for(Resource index : indexes)
             {
                 if(index instanceof IndexResource)
                 {
                     indexingFacility.removeStaleWriteLock((IndexResource)index);
                 }
             }
         }
     }
     
     /**
      * Create the security relation.
      * 
      * @param coralSession the coralSession. 
      */
     private synchronized void createRelation(CoralSession coralSession, String name)
         throws EntityExistsException
     {
         if(name.equals(NODES_RELATION_NAME))
         {
             if(nodesRelation == null)
             {
                 nodesRelation = coralSession.getRelationManager().
                     createRelation(NODES_RELATION_NAME);
             }
         }
         if(name.equals(BRANCHES_RELATION_NAME))
         {
             if(branchesRelation == null)
             {
                 branchesRelation = coralSession.getRelationManager().
                     createRelation(BRANCHES_RELATION_NAME);
             }
         }
     }
 
 	@Override
 	public void logQueryExecution(String query, long timeMillis, int resultsCount) {
 		statistics.update(timeMillis, resultsCount);
 		if(performanceMonitor != null)
 		{
 		    performanceMonitor.update(query, timeMillis, resultsCount);
 		}
 	} 
 	
     public static class Statistics
         implements StatisticsProvider
     {
         private final static int AVG_WINDOW = 300;
         
         private final MuninGraph[] graphs;
 
         private long queryCount;
 
         private long queryExecutionTime;
 
         private long queryResultsCount;
 
         private RoundRobinAverage avgQueryExecutionTime = new RoundRobinAverage(AVG_WINDOW);
 
         private RoundRobinAverage avgQueryResultsCount = new RoundRobinAverage(AVG_WINDOW);
 
         public Statistics(FileSystem fs)
         {
             graphs = new MuninGraph[] { new QueryCount(fs), new QueryExecutionTime(fs),
                             new QueryResultsCount(fs), new AverageQueryExecutionTime(fs),
                             new AverageQueryResultsCount(fs) };
         }
 
         public void update(long timeMillis, int resultsCount)
         {
             queryCount++;
             queryExecutionTime += timeMillis;
             avgQueryExecutionTime.addSample(timeMillis);
             queryResultsCount += resultsCount;
             avgQueryResultsCount.addSample(resultsCount);
         }
 
         @Override
         public MuninGraph[] getGraphs()
         {
             return graphs;
         }
 
         public class QueryCount
             extends AbstractMuninGraph
         {
             public QueryCount(FileSystem fs)
             {
                 super(fs);
             }
 
             @Override
             public String getId()
             {
                 return "searchQueryCount";
             }
 
             public long getQueryCount()
             {
                 return queryCount;
             }
         }
 
         public class QueryExecutionTime
             extends AbstractMuninGraph
         {
             public QueryExecutionTime(FileSystem fs)
             {
                 super(fs);
             }
 
             @Override
             public String getId()
             {
                 return "searchQueryExecutionTime";
             }
 
             public long getQueryExecutionTime()
             {
                 return queryExecutionTime;
             }
         }
 
         public class QueryResultsCount
             extends AbstractMuninGraph
         {
 
             public QueryResultsCount(FileSystem fs)
             {
                 super(fs);
             }
 
             @Override
             public String getId()
             {
                 return "searchQueryResultsCount";
             }
 
             public long getQueryResultsCount()
             {
                 return queryResultsCount;
             }
         }
 
         public class AverageQueryExecutionTime
             extends AbstractMuninGraph
         {
 
             public AverageQueryExecutionTime(FileSystem fs)
             {
                 super(fs);
             }
 
             @Override
             public String getId()
             {
                 return "searchAverageQueryExecutionTime";
             }
 
             public double getAverageQueryExecutionTime()
             {
                 return avgQueryExecutionTime.getAverage();
             }
         }
 
         public class AverageQueryResultsCount
             extends AbstractMuninGraph
         {
 
             public AverageQueryResultsCount(FileSystem fs)
             {
                 super(fs);
             }
 
             @Override
             public String getId()
             {
                 return "searchAverageQueryResultsCount";
             }
 
             public double getAverageQueryResultsCount()
             {
                 return avgQueryResultsCount.getAverage();
             }
         }
     }
     
     private static class PerformanceMonitor
     {
         private final Logger logger;
 
         private final int executionTimeThreshold;
 
         private final int resultsCountThreshold;
 
         public PerformanceMonitor(Configuration config, LoggingConfigurator loggingConfigurator)
             throws ConfigurationException
         {
             logger = loggingConfigurator.createLogger(config.getChild("performance")
                 .getChild("logger").getValue());
             Configuration thresholds = config.getChild("performance").getChild("thresholds");
             executionTimeThreshold = thresholds.getChild("executionTime").getValueAsInteger();
             resultsCountThreshold = thresholds.getChild("resultsCount").getValueAsInteger();
         }
 
         public void update(String query, long timeMillis, int resultsCount)
         {
             if((int)timeMillis > executionTimeThreshold || resultsCount > resultsCountThreshold)
             {
                 logger.info(query + " time: " + timeMillis+ "ms, results: " + resultsCount);
             }
         }
     }
 }
