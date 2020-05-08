 package net.cyklotron.cms.modules.views.forum;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.table.CoralTableModel;
 import org.objectledge.coral.table.ResourceListTableModel;
 import org.objectledge.i18n.I18nContext;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.table.TableException;
 import org.objectledge.table.TableFilter;
 import org.objectledge.table.TableModel;
 import org.objectledge.table.TableState;
 import org.objectledge.table.TableStateManager;
 import org.objectledge.table.TableTool;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.finders.MVCFinder;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.forum.DiscussionResource;
 import net.cyklotron.cms.forum.DiscussionResourceImpl;
 import net.cyklotron.cms.forum.ForumException;
 import net.cyklotron.cms.forum.ForumResource;
 import net.cyklotron.cms.forum.ForumService;
 import net.cyklotron.cms.forum.MessageResource;
 import net.cyklotron.cms.forum.MessageResourceImpl;
 import net.cyklotron.cms.modules.views.BaseSkinableScreen;
 import net.cyklotron.cms.preferences.PreferencesService;
 import net.cyklotron.cms.skins.SkinService;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.style.StyleService;
 import net.cyklotron.cms.util.CollectionFilter;
 import net.cyklotron.cms.util.ProtectedViewFilter;
 
 /**
  * Stateful screen for forum application.
  *
  * @author <a href="mailto:pablo@caltha.pl">Pawe� Potempski</a>
  * @version $Id: Forum.java,v 1.16 2008-05-29 22:55:43 rafal Exp $
  */
 public class Forum
     extends BaseSkinableScreen
 {
     /** forum serivce. */
     protected ForumService forumService;
 
     private Set<String> allowedStates = new HashSet<String>();
 
     public Forum(org.objectledge.context.Context context, Logger logger,
         PreferencesService preferencesService, CmsDataFactory cmsDataFactory,
         StructureService structureService, StyleService styleService, SkinService skinService,
         MVCFinder mvcFinder, TableStateManager tableStateManager, ForumService forumService)
     {
         super(context, logger, preferencesService, cmsDataFactory, structureService, styleService,
                         skinService, mvcFinder, tableStateManager);
         this.forumService = forumService;
         allowedStates.add("Discussions");
         allowedStates.add("Messages");
         allowedStates.add("Message");
         allowedStates.add("NewMessage");
         allowedStates.add("NewDiscussion");
     }
     
     public String getState()
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         String state = parameters.get("state","Discussions");
         if(!allowedStates.contains(state))
         {
             return null;
         }
         return state;
     }
 
     public void prepareDiscussions(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         try
         {
             ForumResource forum = forumService.getForum(coralSession, getSite());
             templatingContext.put("forum",forum);
             
             Resource[] res = coralSession.getStore().getResource(forum, "discussions");
             if(res.length != 1)
             {
                 screenError(getNode(), context, "discussions node not found in "+forum.getPath());
             }
             Resource[] discussions = coralSession.getStore().getResource(res[0]);
             TableState state = tableStateManager.getState(context, "cms:screens:forum,Forum:discussions");
             if(state.isNew())
             {
             
                 state.setTreeView(false);
                 state.setPageSize(10);
                 state.setSortColumnName("creation.time");
                 state.setAscSort(false);
             }
             TableModel model = new ResourceListTableModel(discussions, i18nContext.getLocale());
             ArrayList<TableFilter> filters = new ArrayList<TableFilter>();
             filters.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             TableTool helper = new TableTool(state, filters, model);
             
             templatingContext.put("discussions_table", helper);
 
             res = coralSession.getStore().getResource(forum, "comments");
             if(res.length != 1)
             {
                 screenError(getNode(), context, "comments node not found in "+forum.getPath());
             }
             Resource[] comments = coralSession.getStore().getResource(res[0]);
             
             TableState state2 = tableStateManager.getState(context, "cms:screens:forum,Forum:comments");
             if(state2.isNew())
             {
             
                 state2.setTreeView(false);
                 state2.setPageSize(10);
                 state2.setSortColumnName("creation.time");
                 state2.setAscSort(false);
             }
             TableModel model2 = new ResourceListTableModel(comments, i18nContext.getLocale());
             ArrayList<TableFilter> filters2 = new ArrayList<TableFilter>();
             filters2.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             TableTool helper2 = new TableTool(state2, filters2, model2);
             templatingContext.put("comments_table", helper2);
         }
         catch(ForumException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }
         catch(TableException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }
     }
     
     public void prepareMessages(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
         long did = parameters.getLong("did", -1);
         if(did == -1)
         {
             screenError(getNode(), context, "discussion id not found");
             return;
         }
         try
         {
             DiscussionResource discussion = DiscussionResourceImpl.getDiscussionResource(coralSession,did);
             templatingContext.put("discussion",discussion);
             
             String tableInstance = "cms:screen:forum:ForumMessages:"+getNode().getIdString()+":"+discussion.getIdString();
 
             TableState state = tableStateManager.getState(context, tableInstance);
             if(state.isNew())
             {
                 state.setTreeView(true);
                 String rootId = discussion.getIdString();
                 state.setRootId(rootId);
                 state.setCurrentPage(0);
                 state.setShowRoot(false);
                 state.setExpanded(rootId);
                 state.setAllExpanded(parameters.getBoolean("expand_all", false));
                 state.setPageSize(10);
                 state.setSortColumnName("creation.time");
                 state.setAscSort(false);
             }
             TableModel model = new CoralTableModel(coralSession, i18nContext.getLocale());
             ArrayList<TableFilter> filters = new ArrayList<TableFilter>();
             filters.add(new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             TableTool helper = new TableTool(state, filters, model);
             
             templatingContext.put("table",helper);
             templatingContext.put("messageCount", forumService.getVisibleMessages(coralSession, discussion, coralSession.getUserSubject()));
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "resource not fount ", e);
         }
         catch(TableException e)
         {
             screenError(getNode(), context, "failed to initialize table toolkit: ", e);
         }
         catch(Exception e)
         {
             screenError(getNode(), context, "Component exception: ", e);
         }
     }
 
     public void prepareMessage(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         long mid = parameters.getLong("mid", -1);
         if(mid == -1)
         {
             screenError(getNode(), context, "Message id not found");
             return;
         }
         try
         {
             MessageResource message = MessageResourceImpl.getMessageResource(coralSession,mid);
             templatingContext.put("message",message);
             List<Resource> children = new ArrayList<Resource>(Arrays.asList(coralSession.getStore().getResource(
                           message)));
             CollectionFilter.apply(children, new ProtectedViewFilter(coralSession, coralSession.getUserSubject()));
             templatingContext.put("children", children);
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "Resource not found", e);
         }
     }
     
     public void prepareNewMessage(Context context)
         throws ProcessingException
     {
         Parameters parameters = RequestParameters.getRequestParameters(context);
         CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
         HttpContext httpContext = HttpContext.getHttpContext(context);
         I18nContext i18nContext = I18nContext.getI18nContext(context);
         TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
 
         long did = parameters.getLong("did", -1);
         long mid = parameters.getLong("mid", -1);
         long resid = parameters.getLong("resid", -1);
         if(mid == -1 && did == -1 && resid == -1)
         {
             screenError(getNode(), context, "Discussion nor Message nor Resource id not found");
         }
         try
         {
             if(did != -1)
             {
                 DiscussionResource discussion = DiscussionResourceImpl.getDiscussionResource(coralSession,did);
                 templatingContext.put("discussion",discussion);
                 templatingContext.put("parent",discussion);
             }
             else if(mid != -1)
             {
                 MessageResource message = MessageResourceImpl.getMessageResource(coralSession,mid);
                 DiscussionResource discussion = message.getDiscussion();
                 templatingContext.put("parent_content",prepareContent(message.getContent()));
                 templatingContext.put("discussion",discussion);
                 templatingContext.put("parent",message);
             }
             else
             {
                 templatingContext.put("resource", coralSession.getStore().getResource(resid));
             }
             ForumResource forum = forumService.getForum(coralSession, getSite());
            templatingContext.put("add_captcha",forum.getCaptchaEnabled(false));
         }
         catch(EntityDoesNotExistException e)
         {
             screenError(getNode(), context, "Resource not found", e);
         }
         catch(ForumException e)
         {
             screenError(getNode(), context, "resource not found", e);
         }
     }
     
     public void prepareNewDiscussion(Context context)
         throws ProcessingException
     {
         try
         {
             CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
             TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
             ForumResource forum = forumService.getForum(coralSession, getSite());
            templatingContext.put("add_captcha",forum.getCaptchaEnabled(false));
         }
         catch(ForumException e)
         {
             screenError(getNode(), context, "resource not found", e);
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
 }
