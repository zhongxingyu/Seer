 package net.cyklotron.cms.modules.views.forum;
 
 import java.util.Arrays;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.comparator.CreationTimeComparator;
 import org.objectledge.coral.table.comparator.CreatorNameComparator;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.ListTableModel;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.ForumResourceImpl;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.workflow.WorkflowService;
 
 /**
  * The discussion list screen class.
  */
 public class DiscussionList
     extends BaseForumScreen
 {
 
     public DiscussionList(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         TableStateManager tableStateManager, ForumService forumService,
         WorkflowService workflowService)
     {
         super(context, logger, preferencesService, cmsDataFactory, tableStateManager, forumService,
                         workflowService);
         
     }
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         Boolean fromComponent = (Boolean)httpContext.getSessionAttribute(FROM_COMPONENT);
         if(fromComponent != null && fromComponent.booleanValue())
         {
             try
             {
                 templatingContext.put("from_component",fromComponent);
                 Long nodeId = (Long)httpContext.getSessionAttribute(COMPONENT_NODE);
                 String instance = (String)httpContext.getSessionAttribute(COMPONENT_INSTANCE);
                 Parameters config;
                 if(nodeId != null)
                 {
                     NavigationNodeResource node = NavigationNodeResourceImpl.getNavigationNodeResource(coralSession, nodeId.longValue());
                     config= preferencesService.getNodePreferences(node);
                 }
                 else
                 {
                     config = preferencesService.getSystemPreferences(coralSession);
                 }
                 String app = config.get("component."+instance+".app");
                 String comp = config.get("component."+instance+".class");
                 config = config.getChild("component."+instance+
                     ".config."+app+"."+comp.replace(',','.')+".");                
                 templatingContext.put("component_configuration", config);
                 templatingContext.put("component_node",nodeId);
                 templatingContext.put("component_instance", instance);
             }
             catch(EntityDoesNotExistException e)
             {
                 throw new ProcessingException("ARL Exception ",e);
             }
         }
 
         long fid = parameters.getLong("fid", -1);
         if(fid == -1)
         {
             throw new ProcessingException("Forum id not found");
         }
         try
         {
             TableColumn[] columns = new TableColumn[3];
             columns[0] = new TableColumn("name", new NameComparator(i18nContext.getLocale()));
             columns[1] = new TableColumn("creator", new CreatorNameComparator(i18nContext.getLocale()));
             columns[2] = new TableColumn("creation_time", new CreationTimeComparator());
 
             ForumResource forum = ForumResourceImpl.getForumResource(coralSession,fid);
             templatingContext.put("forum",forum);
             // discussions
             Resource[] res = coralSession.getStore().getResource(forum, "discussions");
             if(res.length != 1)
             {
                 throw new ProcessingException("discussions node not found in "+forum.getPath());
             }
             Resource[] discussions = coralSession.getStore().getResource(res[0]);
             templatingContext.put("discussions", discussions);
             TableState state = tableStateManager.getState(context, "cms:screens:forum,DiscussionList:discussions");
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
                state.setSortColumnName("creation_time");
                state.setAscSort(false);
             }
             TableModel model = new ListTableModel(Arrays.asList(discussions), columns);
             templatingContext.put("discussions_table", new TableTool(state, null, model));
             
             // comments
             res = coralSession.getStore().getResource(forum, "comments");
             if(res.length != 1)
             {
                 throw new ProcessingException("comments node not found "+forum.getPath());
             }
             Resource[] comments = coralSession.getStore().getResource(res[0]);
             templatingContext.put("comments", comments);
             state = tableStateManager.getState(context, "cms:screens:forum,DiscussionList:comments");
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
                state.setSortColumnName("creation_time");
                state.setAscSort(false);
             }
             model = new ListTableModel(Arrays.asList(comments), columns);
             templatingContext.put("comments_table", new TableTool(state, null, model));
         }
         catch(EntityDoesNotExistException e)
         {
             throw new ProcessingException("Resource not found",e);
         }
         catch(TableException e)
         {
             throw new ProcessingException("failed to initialize table toolkit", e);
         }
     }
 }
