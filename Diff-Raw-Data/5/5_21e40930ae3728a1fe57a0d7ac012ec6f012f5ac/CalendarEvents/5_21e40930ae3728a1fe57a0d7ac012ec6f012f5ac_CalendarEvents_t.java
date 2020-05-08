 package net.cyklotron.cms.modules.components.documents;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.cache.CacheFactory;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableRow;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.EmptyTableModel;
 import org.objectledge.table.generic.ListTableModel;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.documents.internal.CalendarSearchMethod;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.modules.components.SkinableCMSComponent;
 import net.cyklotron.cms.search.SearchException;
 import net.cyklotron.cms.search.SearchService;
 import net.cyklotron.cms.search.searching.HitsViewPermissionFilter;
 import net.cyklotron.cms.search.searching.SearchHandler;
 import net.cyklotron.cms.search.searching.SearchHit;
 import net.cyklotron.cms.search.searching.SearchingException;
 import net.cyklotron.cms.search.searching.cms.LuceneSearchHandler;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * CalendarEvents component displays calendar events.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CalendarEvents.java,v 1.8 2007-11-18 20:55:43 rafal Exp $
  */
 public class CalendarEvents
     extends SkinableCMSComponent
 {
     protected StructureService structureService;
     protected SearchService searchService;
     protected CacheFactory cacheService;
     protected IntegrationService integrationService;
     protected TableStateManager tableStateManager;
     
     public CalendarEvents(org.objectledge.context.Context context, Logger logger,
         Templating templating, CmsDataFactory cmsDataFactory, SkinService skinService,
         MVCFinder mvcFinder, StructureService structureService, SearchService searchService,
         CacheFactory cacheFactory, IntegrationService integrationService,
         TableStateManager tableStateManager)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder);
         this.structureService = structureService;
         this.searchService = searchService;
         this.cacheService = cacheFactory;
         this.integrationService = integrationService;
         this.tableStateManager = tableStateManager;
         
         
     }
     
 
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         try
         {
 			Parameters config = getConfiguration();
 			TableState state = 
                 tableStateManager.getState(context, "cached.cms.documents.calendar.events.results");
 			state.setRootId(null);
             state.setTreeView(false);
             
 			// - execute seach and put results into the context
             SearchHit[] hits = getHits(config, coralSession, i18nContext, parameters);
             if(hits == null)
             {
                 return;
             }
 			TableTool hitsTable = new TableTool(state, null, new ListTableModel(hits, (TableColumn[])null));
             templatingContext.put("hits_table", hitsTable);
         }
         catch(TableException e)
         {
             cmsDataFactory.getCmsData(context).getComponent().error("Error preparing table tool", e);
         }
     }
 
     protected SearchHit[] getHits(Parameters config, CoralSession coralSession, 
         I18nContext i18nContext, Parameters parameters)
         throws ProcessingException
     {
         long cacheInterval = (long) config.getLong("cacheInterval",0L);
         if(cacheInterval > 0L)
         {
             // get cache instance
             Map cache = null;
             try
             {
                cache = cacheService.getInstance("calendarevents", "calendarevents");
             }
             catch(Exception e)
             {
                 throw new ProcessingException(e);
             }
             // create cached data key
             CmsData cmsData = cmsDataFactory.getCmsData(context); 
             String key = cmsData.getNode().getIdString()+"."+cmsData.getComponent().getInstanceName();
             // get cached data together with creation time
             CacheEntry entry = (CacheEntry) cache.get(key);
             // check entry validity
             if(entry == null ||
             System.currentTimeMillis() - entry.timeStamp > cacheInterval*1000L)
             {
                 SearchHit[] list = getHits2(config, coralSession, i18nContext, parameters);
                 if(list == null)
                 {
                     return null;
                 }
                 entry = new CacheEntry(list, System.currentTimeMillis());
                 synchronized (cache)
                 {
                     cache.put(key, entry);
                 }
             }
             return entry.list;
         }
         return getHits2(config, coralSession, i18nContext, parameters);
     }
     
     private class CacheEntry
     {
         private final SearchHit[] list;
         private final long timeStamp;
 
         public CacheEntry(SearchHit[] list, long timeStamp)
         {
             this.list = list;
             this.timeStamp = timeStamp;
         }
     }
 
     private SearchHit[] getHits2(Parameters config, CoralSession coralSession, 
         I18nContext i18nContext, Parameters parameters)
         throws ProcessingException
     {
         int startOffset = config.getInt("start_offset",0);
         int endOffset = config.getInt("end_offset",0);
 
         Calendar calendar = Calendar.getInstance(i18nContext.getLocale());
         CmsData cmsData = cmsDataFactory.getCmsData(context);
 
         Date startDate = null;
         Date endDate = null;
 
         calendar.setTime(cmsData.getDate());        
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.add(Calendar.DAY_OF_MONTH, -startOffset);
         startDate = calendar.getTime();
         calendar.setTime(cmsData.getDate());
         calendar.set(Calendar.HOUR_OF_DAY, 23);
         calendar.set(Calendar.MINUTE, 59);
         calendar.set(Calendar.SECOND, 59);
         calendar.add(Calendar.DAY_OF_MONTH, endOffset);
         endDate = calendar.getTime();
 
         Resource[] pools = null;
         try
         {
             long indexId = config.getLong("index_id",-1);
             if(indexId == -1)
             {
                 SiteResource site = cmsData.getSite();
                 Resource parent = searchService.getPoolsRoot(coralSession,site);
                 pools = coralSession.getStore().getResource(parent);
             }
             else
             {
                 Resource index = coralSession.getStore().getResource(indexId);
                 pools = new Resource[1];
                 pools[0] = index;
             }
         }
         catch(SearchException e)
         {
             cmsData.getComponent().error("Cannot get index pool", e);
             return null;
         }
         catch(EntityDoesNotExistException e)
         {
             cmsData.getComponent().error("No index pool with selected id", e);
             return null;
         }
 
         try
         {
             CalendarSearchMethod method = new CalendarSearchMethod(
                 searchService, config, i18nContext.getLocale(), logger, startDate, endDate);
             TableFilter filter = new HitsViewPermissionFilter(coralSession.getUserSubject(), coralSession);           
             TableState state = 
                 tableStateManager.getState(context, "cms.documents.calendar.events.results");
             method.setupTableState(state);
             
             
             // - prepare search handler
             SearchHandler searchHandler = 
                 new LuceneSearchHandler(context, searchService, integrationService, cmsDataFactory);
                     
             // - execute seach and put results into the context
             ArrayList filters = new ArrayList();
             filters.add(filter);
             TableTool hitsTable = searchHandler.search(coralSession, pools, method, state, filters, parameters, i18nContext);
             if(hitsTable == null)
             {
                 hitsTable = new TableTool(state, null, new EmptyTableModel());
             }
             List rows = hitsTable.getRows();
             SearchHit[] searchHits = new SearchHit[rows.size()];
             int i = 0;
             for (Iterator iter = rows.iterator(); iter.hasNext(); i++)
             {
                 TableRow row = (TableRow) iter.next();
                 searchHits[i] = (SearchHit) (row.getObject());
             }
             return searchHits;
         }
         catch(TableException e)
         {
             cmsData.getComponent().error("Error preparing table tool", e);
             return null;
         }
         catch(SearchingException e)
         {
             cmsData.getComponent().error("Error while searching", e);
             return null;
         }
     }
 }
