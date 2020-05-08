 package net.cyklotron.cms.modules.components.forum;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.security.Subject;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.CoralTableModel;
 import org.objectledge.coral.table.comparator.CreationTimeComparator;
 import org.objectledge.coral.table.comparator.NameComparator;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.DefaultParameters;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableColumn;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.table.generic.ListTableModel;
 import org.objectledge.templating.Templating;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsData;
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.forum.DiscussionResource;
 import net.cyklotron.cms.forum.DiscussionResourceImpl;
 import net.cyklotron.cms.forum.ForumException;
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.ForumResourceImpl;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.forum.MessageResourceImpl;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.util.ProtectedViewFilter;
 
 
 /**
  * The discussion list screen class.
  */
 public class Forum
     extends BaseForumComponent
 {
     public Forum(org.objectledge.context.Context context, Logger logger, Templating templating,
         CmsDataFactory cmsDataFactory, SkinService skinService, MVCFinder mvcFinder,
         TableStateManager tableStateManager, ForumService forumService)
     {
         super(context, logger, templating, cmsDataFactory, skinService, mvcFinder,
                         tableStateManager, forumService);
     }
 
     private static Map stateMap = new HashMap();
 
     static
     {
         stateMap.put("dl", "DiscussionList");
         stateMap.put("ml", "MessageList");
         stateMap.put("m", "Message");
         stateMap.put("am", "AddMessage");
     }
 
     public void process(Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, HttpContext httpContext, I18nContext i18nContext, CoralSession coralSession)
         throws ProcessingException
     {
         Parameters componentConfig = getConfiguration();
         boolean stateFull = componentConfig.getBoolean("statefull",true);
         String thisComponentInstance = cmsDataFactory.getCmsData(context).getComponent().getInstanceName();
         templatingContext.put("result_scope", "forum_"+thisComponentInstance);
         String sessionKey = getSessionKey();
 
         Parameters componentParameters = null;
         if(stateFull)
         {
             componentParameters = (Parameters)httpContext.getSessionAttribute(sessionKey);
         }
         if(componentParameters == null)
         {
             componentParameters = new DefaultParameters();
             long did  = componentConfig.getLong("did",-1);
             if(did != -1)
             {
                 componentParameters.add("did",did);
                 componentParameters.add("state","ml");
             }
         }
 
         String componentInstance = parameters.get("ci","");
         if(componentInstance.equals(thisComponentInstance))
         {
             componentParameters = new DefaultParameters(parameters);
         }
 
         if(stateFull)
         {
             httpContext.setSessionAttribute(sessionKey, componentParameters);
         }
         // retrieve parameters from session...if null take the default values
         templatingContext.put("parameters",componentParameters);
 
         prepareState(context);
     }
 
     public String getComponentName()
     {
         return this.getClass().getName(); 
     }
 
     public Map getStateMap()
     {
         return stateMap;
     }    
 
     public String getSessionKey()
         throws ProcessingException
     {
         CmsData cmsData = cmsDataFactory.getCmsData(context);
         NavigationNodeResource node = cmsData.getNode();
         String thisComponentInstance = cmsData.getComponent().getInstanceName();
         if(node != null)
         {
             return getComponentName()+":"+thisComponentInstance+":"+node.getIdString(); 
         }
         else
         {
             return getComponentName()+":"+thisComponentInstance;
         }
     }
     
     protected void resetParameters(HttpContext httpContext)
         throws ProcessingException
     {
         String sessionKey = getSessionKey();
         httpContext.setSessionAttribute(sessionKey, new DefaultParameters());
     }
 
     public void prepareDiscussionList(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         long fid = ((Parameters)templatingContext.get("parameters")).getLong("fid",-1);
         if(fid == -1)
         {
             if(getSite(context) != null)
             {
                 try
                 {
                     fid = forumService.getForum(coralSession, getSite(context)).getId();
                 }
                 catch(ForumException e)
                 {
                     resetParameters(httpContext);
                     componentError(context, "Failed to obtain the forum for the site", e);
                     return;
                 }
             }
             else
             {
                 componentError(context, "No site selected");
             }
         }
 
         try
         {
             TableColumn[] columns = new TableColumn[2];
             columns[0] = new TableColumn("name", new NameComparator(i18nContext.getLocale()));
             columns[1] = new TableColumn("creation_time", new CreationTimeComparator());
             
             String thisComponentInstance = cmsDataFactory.getCmsData(context).getComponent().getInstanceName();
             if(getSite(context) == null)
             {
                 componentError(context, "No site selected");
                 return;
             }
             String tableInstance = getComponentName()+":"+thisComponentInstance+":discussions:"+getSite(context).getIdString();
 
             ForumResource forum = ForumResourceImpl.getForumResource(coralSession,fid);
             templatingContext.put("forum",forum);
 
             Resource[] res = coralSession.getStore().getResource(forum, "discussions");
             if(res.length != 1)
             {
                 throw new ProcessingException("discussions node not found in "+forum.getPath());
             }
             Resource[] discussions = coralSession.getStore().getResource(res[0]);
             templatingContext.put("discussions", discussions);
             TableState state = tableStateManager.getState(context, tableInstance+":discussions");
             if(state.isNew())
             {
                 state.setTreeView(false);
                 state.setPageSize(10);
                 state.setSortColumnName("creation_time");
             }
             TableModel model = new ListTableModel(Arrays.asList(discussions), columns);
             ArrayList filters = new ArrayList();
             filters.add(new ProtectedViewFilter(context, coralSession.getUserSubject()));
             TableTool helper = new TableTool(state, filters, model);
             templatingContext.put("discussions_table", helper);
 
             res = coralSession.getStore().getResource(forum, "comments");
             if(res.length != 1)
             {
                 throw new ProcessingException("comments node not found in "+forum.getPath());
             }
             Resource[] comments = coralSession.getStore().getResource(res[0]);
             templatingContext.put("comments", discussions);
             state = tableStateManager.getState(context, tableInstance+":comments");
             if(state.isNew())
             {
             
                 state.setTreeView(false);
                 state.setPageSize(10);
                 state.setSortColumnName("creation_time");
             }
             model = new ListTableModel(Arrays.asList(comments), columns);
             ArrayList filters2 = new ArrayList();
             filters2.add(new ProtectedViewFilter(context, coralSession.getUserSubject()));
             TableTool helper2 = new TableTool(state, filters2, model);
             
             templatingContext.put("comments_table", helper2);
         }
         catch(EntityDoesNotExistException e)
         {
             resetParameters(httpContext);
             componentError(context, "Resource not found", e);
         }
         catch(TableException e)
         {
             resetParameters(httpContext);
             componentError(context, "failed to initialize table toolkit", e);
         }
         catch(Exception e)
         {
             resetParameters(httpContext);
             componentError(context, "Component exception", e);
         }
     }
 
     protected DiscussionResource getDiscussion(HttpContext httpContext, CoralSession coralSession,
         Context context, boolean errorOnNull)
         throws ProcessingException
     {
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         long did = ((Parameters)templatingContext.get("parameters")).getLong("did",-1);
         if(did == -1)
         {
         	if(errorOnNull)
         	{
             	resetParameters(httpContext);
             	componentError(context, "Discussion id not found");
             }
 			return null;
         }
         
         try
         {
             return DiscussionResourceImpl.getDiscussionResource(coralSession,did); 
         }
         catch(EntityDoesNotExistException e)
         {
             resetParameters(httpContext);
             componentError(context, "Resource not found", e);
             return null;
         }
     }
 
     public void prepareMessageList(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         Subject subject = coralSession.getUserSubject();
         try
         {
             DiscussionResource discussion = getDiscussion(httpContext, coralSession, context, true);
             if(discussion != null)
             {
                 templatingContext.put("discussion",discussion);
                 String thisComponentInstance = cmsDataFactory.getCmsData(context).getComponent().getInstanceName();
                 String tableInstance = getComponentName()+":"+thisComponentInstance+":messages:"+discussion.getIdString();
 
                 TableState state = tableStateManager.getState(context, tableInstance);
                 if(state.isNew())
                 {
                     state.setTreeView(true);
                     String rootId = discussion.getIdString();
                     state.setRootId(rootId);
                     state.setCurrentPage(0);
                     state.setShowRoot(false);
                     state.setExpanded(rootId);
 
                     // TODO: configure default
                     state.setPageSize(4);
                     state.setSortColumnName("creation.time");
                 }
                 TableModel model;
                 model = new CoralTableModel(coralSession, i18nContext.getLocale());
 
                 ArrayList filters = new ArrayList();
                 filters.add(new ProtectedViewFilter(context, subject));
                 TableTool helper = null;
                 helper = new TableTool(state, filters, model);
                 templatingContext.put("table", helper);
             }
         }
         catch(TableException e)
         {
             resetParameters(httpContext);
             componentError(context, "failed to initialize table toolkit", e);
         }
         catch(Exception e)
         {
             resetParameters(httpContext);
             componentError(context, "Component exception", e);
         }
     }
 
     public void prepareMessage(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         long mid = ((Parameters)templatingContext.get("parameters")).getLong("mid",-1);
         if(mid == -1)
         {
             resetParameters(httpContext);
             componentError(context, "Message id not found");
             return;
         }
         Subject subject = coralSession.getUserSubject();
         MessageResource message = null;
         try
         {
             message = MessageResourceImpl.getMessageResource(coralSession,mid);
         }
         catch(EntityDoesNotExistException e)
         {
             resetParameters(httpContext);
             componentError(context, "Resource not found", e);
         }
         if(message.canView(context,subject))
         {
             templatingContext.put("message",message);
             templatingContext.put("children", Arrays.asList(coralSession.getStore().getResource(message)));
         }
         else
         {
             resetParameters(httpContext);
             componentError(context, "User has no privileges to view this message");
         }
     }
 
     public void prepareAddMessage(Context context)
         throws ProcessingException
     {
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         long did = ((Parameters)templatingContext.get("parameters")).getLong("did",-1);
         long mid = ((Parameters)templatingContext.get("parameters")).getLong("mid",-1);
         long resid = ((Parameters)templatingContext.get("parameters")).getLong("resid",-1);
 
         if(mid == -1 && did == -1 && resid == -1)
         {
             resetParameters(httpContext);
             componentError(context, "Discussion id nor message id nor resource id defined");
             return;
         }
         try
         {
             DiscussionResource discussion = getDiscussion(httpContext, coralSession, context, false);
             if(discussion != null)
             {
                 templatingContext.put("discussion",discussion);
                 templatingContext.put("parent",discussion);
             }
             if(mid != -1)
             {
                 MessageResource message = MessageResourceImpl.getMessageResource(coralSession,mid);
                 discussion = message.getDiscussion();
                 templatingContext.put("parent_content",prepareContent(message.getContent()));
                 templatingContext.put("discussion",discussion);
                 templatingContext.put("parent",message);
             }
         }
         catch(EntityDoesNotExistException e)
         {
             resetParameters(httpContext);
             componentError(context, "Resource not found", e);
         }
         catch(Exception e)
         {
             resetParameters(httpContext);
             componentError(context, "Component exception", e);
         }
     }
 
     private String prepareContent(String content)
     {
         StringBuilder sb = new StringBuilder("");
         StringTokenizer st = new StringTokenizer(content, "\n", false);
         while (st.hasMoreTokens()) {
             sb.append(">");
             sb.append(st.nextToken());
             sb.append("\n");
         }
         return sb.toString();
     }
 
     public String getState(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         String thisComponentInstance = cmsDataFactory.getCmsData(context).getComponent().getInstanceName();
         String componentInstance = parameters.get("ci","");
         Parameters parametersX;
         if(componentInstance.equals(thisComponentInstance))
         {
             //parameters = new DefaultParameters(parameters);
             parametersX = parameters;
         }
         else
         {
             Parameters componentConfig = getConfiguration();
             boolean stateful = componentConfig.getBoolean("statefull",true);
             if(stateful)
             {
                 String sessionKey = getSessionKey();
                 parametersX = (Parameters)httpContext.getSessionAttribute(sessionKey);
                 // avoid NPE 
                 if(parametersX == null)
                 {
                     parametersX = new DefaultParameters();
                 }
             }
             else
             {
                 parametersX = new DefaultParameters();
             }
         }        
         
         String state = parametersX.get("state","");
 	    if(state.equals(""))
 	    {
 	        state = parameters.isDefined("did") ? "ml" : "dl";
 	    }
         String intState = (String)getStateMap().get(state);
         if(intState == null)
         {
             componentError(context, "invalid component state '"+intState+"'");
             return "Default";
         }
         return intState;
     }
 }
