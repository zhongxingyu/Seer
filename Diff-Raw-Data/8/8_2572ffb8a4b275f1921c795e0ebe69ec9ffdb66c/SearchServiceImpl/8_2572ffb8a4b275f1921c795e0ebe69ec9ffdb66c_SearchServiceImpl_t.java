 package net.cyklotron.cms.search.internal;
 
 import java.io.IOException;
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
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.coral.table.filter.PathFilter;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.table.TableFilter;
 import org.picocontainer.Startable;
 
 import net.cyklotron.cms.CmsNodeResourceImpl;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.search.IndexResource;
 import net.cyklotron.cms.search.IndexResourceImpl;
 import net.cyklotron.cms.search.IndexableResource;
 import net.cyklotron.cms.search.IndexingFacility;
 import net.cyklotron.cms.search.PoolResource;
 import net.cyklotron.cms.search.RootResource;
 import net.cyklotron.cms.search.RootResourceImpl;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.SearchingFacility;
 import net.cyklotron.cms.search.XRefsResource;
 import net.cyklotron.cms.search.searching.CategoryAnalyzer;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 
 /**
  * Implementation of Search Service
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: SearchServiceImpl.java,v 1.7 2005-04-22 03:52:07 pablo Exp $
  */
 public class SearchServiceImpl 
     implements SearchService, Startable
 {
     /** resources relation name */
    public static final String NODES_RELATION_NAME = "search.IndexedNodes";
     
     /** rc relation name */
    public static final String BRANCHES_RELATION_NAME = "search.IndexedBranches";
     
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
 
     // initialization ////////////////////////////////////////////////////////
 
     /**
      * Starts the service - the search service must be started on broker start in order to listen
      * to resource tree changes.
      */
     public SearchServiceImpl(Configuration config, Logger logger, Context context, 
         CoralSessionFactory sessionFactory, FileSystem fileSystem,
         SiteService siteService, CategoryService categoryService, UserManager userManager,
         PreferencesService preferencesService, IntegrationService integrationService)
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
         Configuration[] paths = config.getChildren("accepted_path");
         acceptedPaths = new String[paths.length];
         for(int i = 0; i < paths.length; i++)
         {
             acceptedPaths[i] = paths[i].getValue();
         }
         // prepare indexing facility (registers listeners)
         indexingFacility = new IndexingFacilityImpl(context, sessionFactory, log, this, fileSystem,
             preferencesService, categoryService, userManager, integrationService);
         // prepare searching facility
         searchingFacility = new SearchingFacilityImpl(log, indexingFacility);
     }
 
     public void start()
     {
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
                 return CmsNodeResourceImpl.createCmsNodeResource(coralSession, "indexes", searchRoot);
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
                 return CmsNodeResourceImpl.createCmsNodeResource(coralSession, "pools", searchRoot);
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
                 return RootResourceImpl.createRootResource(coralSession, "search", site, new DefaultParameters());
             }
             catch (ValueRequiredException e)
             {
                 throw new SearchException("ValueRequiredException: ", e);
             }
         }
         throw new SearchException("Too many search root resources for site: " + site.getName());
     }
 
     public IndexResource createIndex(CoralSession coralSession, SiteResource site, String name) throws SearchException
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
          * TODO: Use a FIND RESOURCE FROM RESOURCECLASS=''; and store this prepared query.
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
                     ResourceList indexes = pool.getIndexes();
                     if (indexes.remove(index))
                     {
                         pool.setIndexes(indexes);
                         pool.update();
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
             String[] files = fileSystem.list(indexDirectoryPath);
             for (int i = 0; i < files.length; i++)
             {
                 fileSystem.delete(indexDirectoryPath + "/" + files[i]);
             }
             fileSystem.delete(indexDirectoryPath);
         }
         catch (IOException e)
         {
             throw new SearchException("cannot delete index files '" + indexResourcePath + "'", e);
         }
     }
 
     public IndexResource[] getIndex(CoralSession coralSession,IndexableResource res)
     {
         // add indexes indexing the resource as a node
         Relation relation = getIndexedNodesRelation(coralSession);
         Resource[] tmp = relation.getInverted().get(res);
         IndexResource[] indexes = new IndexResource[tmp.length];
         System.arraycopy(tmp, 0, indexes, 0, tmp.length);
         Set indexesSet = new HashSet(indexes.length * 2 + 4);
         indexesSet.addAll(Arrays.asList(indexes));
 
         // add indexes indexing the resource as a part of a branch
         Resource resource = res;
         relation = getIndexedBranchesRelation(coralSession).getInverted();
         while (resource != null)
         {
             tmp = relation.get(resource);
             indexes = new IndexResource[tmp.length];
             System.arraycopy(tmp, 0, indexes, 0, tmp.length);
             indexesSet.addAll(Arrays.asList(indexes));
             resource = resource.getParent();
         }
 
         indexes = new IndexResource[indexesSet.size()];
         indexes = (IndexResource[]) (indexesSet.toArray(indexes));
         return indexes;
     }
 
     public Analyzer getAnalyzer(Locale locale)
     {
         // TODO: Implement it using an Analyzer registry for languages and language field in index. \\
         // analyser registry should be refactored out as an external component
         return new CategoryAnalyzer();
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
 }
