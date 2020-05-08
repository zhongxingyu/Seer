 package net.cyklotron.cms.modules.views.documents;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
import org.objectledge.context.Context;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.query.MalformedQueryException;
 import org.objectledge.coral.query.QueryResults;
 import org.objectledge.coral.relation.MalformedRelationQueryException;
 import org.objectledge.coral.relation.ResourceIdentifierResolver;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.table.InverseFilter;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.category.query.CategoryQueryPoolResource;
 import net.cyklotron.cms.category.query.CategoryQueryResource;
 import net.cyklotron.cms.documents.DocumentNodeResource;
import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
import net.cyklotron.cms.util.CmsResourceListTableModel;
 import net.cyklotron.cms.workflow.AutomatonResource;
 import net.cyklotron.cms.workflow.StateFilter;
 import net.cyklotron.cms.workflow.StateResource;
 import net.cyklotron.cms.workflow.StatefulResource;
 import net.cyklotron.cms.workflow.WorkflowException;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 import bak.pcj.LongIterator;
 import bak.pcj.set.LongOpenHashSet;
 import bak.pcj.set.LongSet;
 
 /**
  * This class contains logic shared between MyDocuments state of ProposeDocument screen and
  * MyDocuments component.
  * 
  * @author rafal.krzewski@caltha.pl
  */
 public class MyDocumentsImpl
     implements MyDocuments
 {
     private final CoralSessionFactory coralSessionFactory;
 
     private final CategoryService categoryService;
 
     private final SiteService siteService;
 
     private final WorkflowService workflowService;
 
    private final Context context;

    private final IntegrationService integrationService;

    public MyDocumentsImpl(Context context, CoralSessionFactory coralSessionFactory,
        CategoryService categoryService, IntegrationService integrationService,
        SiteService siteService, WorkflowService workflowService)
     {
        this.context = context;
        this.integrationService = integrationService;
         this.coralSessionFactory = coralSessionFactory;
         this.categoryService = categoryService;
         this.siteService = siteService;
         this.workflowService = workflowService;
     }
 
     public List<TableFilter<? super DocumentNodeResource>> excludeStatesFilter(String... excluded)
         throws EntityDoesNotExistException, AmbigousEntityNameException, WorkflowException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         List<TableFilter<? super DocumentNodeResource>> filters = new ArrayList<>();
         ResourceClass<?> navigationNodeClass = coralSession.getSchema().getResourceClass(
             "structure.navigation_node");
         Resource cmsRoot = coralSession.getStore().getUniqueResourceByPath("/cms");
         AutomatonResource automaton = workflowService.getPrimaryAutomaton(coralSession, cmsRoot,
             navigationNodeClass);
         Set<StateResource> rejectedStates = new HashSet<StateResource>();
         for(String exState : excluded)
         {
             rejectedStates.add(workflowService.getState(coralSession, automaton, exState));
         }
         filters.add(new InverseFilter<StatefulResource>(new StateFilter(rejectedStates, true)));
         return filters;
     }
 
     public TableModel<DocumentNodeResource> siteBasedModel(CmsData cmsData, Locale locale,
         String whereClause)
         throws MalformedQueryException, TableException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         if(!whereClause.isEmpty())
         {
             whereClause = " AND " + whereClause;
         }
         String query = "FIND RESOURCE FROM documents.document_node WHERE site ="
             + cmsData.getSite().getIdString() + whereClause;
         List<DocumentNodeResource> myDocuments = (List<DocumentNodeResource>)coralSession
             .getQuery().executeQuery(query).getList(1);
 
        return new CmsResourceListTableModel<DocumentNodeResource>(context, integrationService,
            myDocuments, locale);
     }
 
     public TableModel<DocumentNodeResource> queryBasedModel(CategoryQueryResource includeQuery,
         CategoryQueryResource excludeQuery, CmsData cmsData, Locale locale, String whereClause)
         throws SiteException, MalformedQueryException, MalformedRelationQueryException,
         EntityDoesNotExistException, TableException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         if(!whereClause.isEmpty())
         {
             whereClause = " WHERE " + whereClause;
         }
         String resQuery = "FIND RESOURCE FROM documents.document_node" + whereClause;
 
         QueryResults qr = coralSession.getQuery().executeQuery(resQuery);
         LongSet ids = new LongOpenHashSet(Math.max(1, qr.rowCount()));
         for(QueryResults.Row r : qr.getList())
         {
             ids.add(r.getId());
         }
 
         String catQuery;
         if(includeQuery.getQuery("").trim().length() > 0)
         {
             catQuery = includeQuery.getQuery().replace(";", "") + " * "
                 + siteClause(includeQuery, coralSession) + ";";
         }
         else
         {
             catQuery = siteClause(includeQuery, coralSession) + ";";
         }
 
         final ResourceIdentifierResolver resolver = getResolver(coralSession);
 
         LongSet included = coralSession.getRelationQuery().queryIds(catQuery, resolver, ids);
 
         if(excludeQuery != null)
         {
             LongSet excluded = coralSession.getRelationQuery().queryIds(excludeQuery.getQuery(),
                 resolver, included);
             included.removeAll(excluded);
         }
 
         List<DocumentNodeResource> documentList = new ArrayList<>(included.size());
         LongIterator i = included.iterator();
         while(i.hasNext())
         {
             documentList.add((DocumentNodeResource)coralSession.getStore().getResource(i.next()));
         }
 
        return new CmsResourceListTableModel<DocumentNodeResource>(context, integrationService,
            documentList, locale);
     }
 
     public LongSet queryPoolBasedSet(CategoryQueryPoolResource queryPool, String whereClause)
         throws SiteException, MalformedQueryException, MalformedRelationQueryException,
         EntityDoesNotExistException, TableException
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
         if(!whereClause.isEmpty())
         {
             whereClause = " WHERE " + whereClause;
         }
         String resQuery = "FIND RESOURCE FROM documents.document_node" + whereClause;
 
         QueryResults qr = coralSession.getQuery().executeQuery(resQuery);
         LongSet ids = new LongOpenHashSet(Math.max(1, qr.rowCount()));
         for(QueryResults.Row r : qr.getList())
         {
             ids.add(r.getId());
         }
 
         String catQuery = "";
         LongSet included = new LongOpenHashSet(Math.max(1, qr.rowCount()));
         final ResourceIdentifierResolver resolver = getResolver(coralSession);
         for(CategoryQueryResource query : (List<CategoryQueryResource>)queryPool.getQueries())
         {
             if(query.getQuery("").trim().length() > 0)
             {
                 catQuery = query.getQuery().replace(";", "") + " * "
                     + siteClause(query, coralSession) + ";";
             }
             else
             {
                 catQuery = siteClause(query, coralSession) + ";";
             }
             included.addAll(coralSession.getRelationQuery().queryIds(catQuery, resolver, ids));
         }
         ids.retainAll(included);
         return ids;
     }
 
     private String siteClause(CategoryQueryResource query, CoralSession coralSession)
         throws SiteException
     {
         StringBuilder buff = new StringBuilder();
         if(query.isAcceptedSitesDefined() && query.getAcceptedSites().trim().length() > 0)
         {
             buff.append("MAP('structure.SiteDocs') { ");
             String[] siteNames = query.getAcceptedSiteNames();
             for(int i = 0; i < siteNames.length; i++)
             {
                 SiteResource site = siteService.getSite(coralSession, siteNames[i]);
                 buff.append("RES(").append(site.getIdString()).append(")");
                 if(i < siteNames.length - 1)
                 {
                     buff.append(" + ");
                 }
             }
             buff.append(" }");
         }
         return buff.toString();
     }
 
     private ResourceIdentifierResolver getResolver(final CoralSession coralSession)
     {
         return new ResourceIdentifierResolver()
             {
                 @Override
                 public LongSet resolveIdentifier(String identifier)
                     throws EntityDoesNotExistException
                 {
                     LongSet ids;
                     Resource res = null;
                     if(identifier.startsWith("/"))
                     {
                         Resource[] ra = coralSession.getStore().getResourceByPath(identifier);
                         if(ra.length == 0)
                         {
                             throw new EntityDoesNotExistException("resource " + identifier
                                 + " does not exist");
                         }
                         if(ra.length > 0)
                         {
                             throw new EntityDoesNotExistException("path " + identifier
                                 + " is ambiguous");
                         }
                         res = ra[0];
                     }
                     else if(identifier.matches("\\d+"))
                     {
                         res = coralSession.getStore().getResource(Long.parseLong(identifier));
                     }
                     else
                     {
                         throw new EntityDoesNotExistException("malformed identifier " + identifier);
                     }
                     if(res instanceof SiteResource)
                     {
                         ids = new LongOpenHashSet(1);
                         ids.add(res.getId());
                     }
                     else if(res instanceof CategoryResource)
                     {
                         CategoryResource[] categories = categoryService.getSubCategories(
                             coralSession, (CategoryResource)res, true);
                         ids = new LongOpenHashSet(Math.max(1, categories.length));
                         for(int i = 0; i < categories.length; i++)
                         {
                             ids.add(categories[i].getId());
                         }
                     }
                     else
                     {
                         throw new EntityDoesNotExistException(identifier + " has unexpected type "
                             + res.getResourceClass().getName());
                     }
                     return ids;
                 }
             };
     }
 
     public CategoryQueryResource getQueryResource(String name, Parameters config)
     {
         CoralSession coralSession = coralSessionFactory.getCurrentSession();
 
         long id = config.getLong(name + "_query_id", -1l);
         if(id != -1l)
         {
             try
             {
                 return (CategoryQueryResource)coralSession.getStore().getResource(id);
             }
             catch(EntityDoesNotExistException | ClassCastException e)
             {
             }
         }
         return null;
     }
 }
