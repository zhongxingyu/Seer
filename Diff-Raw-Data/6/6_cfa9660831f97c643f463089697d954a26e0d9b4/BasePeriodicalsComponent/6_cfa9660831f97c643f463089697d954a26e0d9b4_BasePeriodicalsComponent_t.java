 package net.cyklotron.cms.modules.components.periodicals;
 
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.PathTreeElement;
 import org.objectledge.table.generic.PathTreeTableModel;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsComponentData;
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.files.FileResource;
 import net.cyklotron.cms.files.FilesService;
 import net.cyklotron.cms.modules.components.SkinableCMSComponent;
 import net.cyklotron.cms.periodicals.PeriodicalRenderer;
 import net.cyklotron.cms.periodicals.PeriodicalResource;
 import net.cyklotron.cms.periodicals.PeriodicalsException;
 import net.cyklotron.cms.periodicals.PeriodicalsService;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.skins.SkinService;
 
 /**
  * @author fil
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public abstract class BasePeriodicalsComponent
     extends SkinableCMSComponent
 {
     protected PeriodicalsService periodicalsService;
     
     protected FilesService cmsFilesService;
     
     protected TableStateManager tableStateManager;
 
     
     public BasePeriodicalsComponent(org.objectledge.context.Context context, Logger logger,
         Templating templating, CmsDataFactory cmsDataFactory, SkinService skinService,
         MVCFinder mvcFinder, PeriodicalsService periodicalsService, 
         FilesService cmsFilesService, TableStateManager tableStateManager)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder);
         this.periodicalsService = periodicalsService;
         this.cmsFilesService = cmsFilesService;
         this.tableStateManager = tableStateManager;
     }
     
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         prepareState(context);
     }
     
     protected abstract PeriodicalResource[] getPeriodicals(CoralSession coralSession, SiteResource site)
         throws ProcessingException;
         
     protected abstract String getSessionKey();
     
     public void prepareDefault(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         List periodicals = Arrays.asList(getPeriodicals(coralSession, getSite(context)));
         Collections.sort(periodicals, new NameComparator(i18nContext.getLocale()));
         templatingContext.put("periodicals", periodicals);
     }
 
     public void prepareDetails(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         Parameters parameters = RequestParameters.getRequestParameters(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         PeriodicalResource periodical = getPeriodical(parameters, httpContext, coralSession);
         templatingContext.put("periodical", periodical);
         Resource[] children = coralSession.getStore().getResource(periodical.getStorePlace());
         PeriodicalRenderer renderer = periodicalsService.getRenderer(periodical.getRenderer());
         String suffix = renderer.getFilenameSuffix();
         periodicalsService.releaseRenderer(renderer);
         try
         {
             TableColumn column = new TableColumn("element", PathTreeElement.getComparator("name", i18nContext.getLocale()));
             PathTreeTableModel model = new PathTreeTableModel(new TableColumn[] { column });
             model.bind("/", new PathTreeElement("archive","label"));
             Resource latest = null;
             for(int i = 0; i < children.length; i++)
             {
                 Resource res = children[i];
                 if(res instanceof FileResource && res.getName().endsWith(suffix))
                 {
                     if(latest == null || latest.getCreationTime().compareTo(res.getCreationTime()) < 0)
                     {
                         latest = res;
                     }
                     bindIssue(model, (FileResource)res);
                 }
             }
             if(latest != null)
             {
                 templatingContext.put("latest", latest);
             }
             
             CmsComponentData componentData = getCmsData().getComponent();
             String key = "table:"+getSessionKey()+":"+periodical.getIdString()+
                 ":"+componentData.getInstanceName();
             TableState state = tableStateManager.getState(context, key);
             if(state.isNew())
             {
                 state.setTreeView(true);
                 state.setRootId("0");
                 state.setShowRoot(true);
                 state.setExpanded("0");
                 state.setPageSize(0);
                 state.setSortColumnName("element");
             }
             templatingContext.put("table", new TableTool(state, null, model));
         }
         catch(Exception e)
         {
             throw new ProcessingException("failed to gather data", e);
         }
     }
     
     protected void bindIssue(PathTreeTableModel model, FileResource file)
     {
         Calendar cal = new GregorianCalendar();
         cal.setTime(file.getCreationTime());
         String name = Integer.toString(cal.get(Calendar.YEAR));
         String path = "/"+name;
         if(model.getObjectByPath(path) == null)
         {
             model.bind(path, new PathTreeElement(name, "year"));
         }
         name = Integer.toString(cal.get(Calendar.MONTH));
         path = path+"/"+name;
         if(model.getObjectByPath(path) == null)
         {
             model.bind(path, new PathTreeElement(name, "month"));
         }
         name = file.getName();
         path = path+"/"+name;
         PathTreeElement elm = new PathTreeElement(name, "item");
         elm.set("file", file);
         elm.set("date", file.getCreationTime());
         model.bind(path, elm);
     }
     
     protected PeriodicalResource getPeriodical(Parameters parameters, HttpContext httpContext,
         CoralSession coralSession)
         throws ProcessingException
     {
     	CmsData cmsData = cmsDataFactory.getCmsData(context);
     	
         CmsComponentData componentData = cmsData.getComponent();
         String key = getSessionKey()+":"+getNode().getIdString()+
             ":"+componentData.getInstanceName();
         String ci = parameters.get("ci","");
         long periodicalId = -1;
         if(ci.equals(componentData.getInstanceName()))
         {
             periodicalId = parameters.getLong("periodical", -1);
             if(periodicalId > 0)
             {
                 httpContext.setSessionAttribute(key, new Long(periodicalId));
             }
             else
             {
                 httpContext.removeSessionAttribute(key);
             }
         }
         else
         {
             Long stored = (Long)httpContext.getSessionAttribute(key);
             if(stored != null)
             {
                 periodicalId = stored.longValue();
             }
         }
         if(periodicalId > 0)
         {
             try
             {
                 return (PeriodicalResource)coralSession.getStore().getResource(periodicalId);
             }
             catch(EntityDoesNotExistException e)
             {
                 throw new ProcessingException("invalid peridical id", e);
             }
         }
         else
         {
 			try
 			{
 	            String periodicalName = getConfiguration().get("periodical","");
 	            if(periodicalName.length() > 0)
 	            {
 	                Resource[] res = coralSession.getStore().getResource(
 	                	getPeriodicalRoot(coralSession, cmsData.getSite()), periodicalName);
 	                if(res.length > 0)
 	                {
 	                    return (PeriodicalResource)res[0];
 	                }
 	                else
 	                {
 	                    return null;
 	                }
 	            }
 	            else
 	            {
 	                return null;
 	            }
 			}
 			catch (PeriodicalsException e)
 			{
 				throw new ProcessingException("cannot get periodcals root", e);
 			}
         }
     }
     
    public String getState(Context contex)
         throws ProcessingException
     {
        Parameters parameters = RequestParameters.getRequestParameters(contex);
        HttpContext httpContext = HttpContext.getHttpContext(contex);
        CoralSession coralSession = (CoralSession) context.getAttribute(CoralSession.class);
         return getPeriodical(parameters,httpContext, coralSession) == null ? "Default" : "Details";
     }
 
 	protected abstract Resource getPeriodicalRoot(CoralSession coralSession, SiteResource site)
 	throws PeriodicalsException;
 }
