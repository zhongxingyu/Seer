 package org.exoplatform.wiki.ext.impl;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.jcr.Node;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.exoplatform.commons.utils.ActivityTypeUtils;
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.portal.config.model.PortalConfig;
 import org.exoplatform.services.jcr.access.PermissionType;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.security.ConversationState;
 import org.exoplatform.services.security.IdentityConstants;
 import org.exoplatform.services.security.MembershipEntry;
 import org.exoplatform.social.core.activity.model.ExoSocialActivity;
 import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.ActivityManager;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.space.model.Space;
 import org.exoplatform.social.core.space.spi.SpaceService;
 import org.exoplatform.social.core.storage.SpaceStorageException;
 import org.exoplatform.webui.application.WebuiRequestContext;
 import org.exoplatform.wiki.mow.api.Page;
 import org.exoplatform.wiki.mow.api.WikiNodeType;
 import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
 import org.exoplatform.wiki.rendering.RenderingService;
 import org.exoplatform.wiki.service.BreadcrumbData;
 import org.exoplatform.wiki.service.WikiService;
 import org.exoplatform.wiki.service.listener.PageWikiListener;
 import org.exoplatform.wiki.utils.Utils;
 import org.xwiki.rendering.syntax.Syntax;
 
 public class WikiSpaceActivityPublisher extends PageWikiListener {
 
   public static final String WIKI_APP_ID       = "ks-wiki:spaces";
 
   public static final String ACTIVITY_TYPE_KEY = "act_key";
 
   public static final String PAGE_ID_KEY       = "page_id";
 
   public static final String PAGE_TYPE_KEY     = "page_type";
 
   public static final String PAGE_OWNER_KEY    = "page_owner";
 
   public static final String PAGE_TITLE_KEY    = "page_name";
 
   public static final String URL_KEY           = "page_url";
   
   public static final String PAGE_EXCERPT      = "page_exceprt";
   
   public static final String VIEW_CHANGE_URL_KEY = "view_change_url";
   
   public static final String VIEW_CHANGE_ANCHOR  = "#CompareRevision/changes";  
   
   public static final String WIKI_PAGE_NAME      = "wiki";
   
   public static final String WIKI_PAGE_VERSION   = "version";
 
   private static final int   EXCERPT_LENGTH    = 140;
 
   private static final Log   LOG               = ExoLogger.getExoLogger(WikiSpaceActivityPublisher.class);
 
   public WikiSpaceActivityPublisher() {
   }
   
   private ExoSocialActivityImpl createNewActivity(String ownerId) {
     ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
     activity.setUserId(ownerId);
     activity.setTitle("title");
     activity.setBody("body");
     activity.setType(WIKI_APP_ID);
     return activity;
   }
   
   private ExoSocialActivity generateActivity(Identity ownerStream, Identity ownerIdentity, String wikiType, String wikiOwner, String pageId, Page page, String spaceUrl, String activityType) throws Exception {
     // Get activity
     Node node = page.getJCRPageNode();
     ExoSocialActivity activity = null;
     boolean isNewActivity = false;
     ActivityManager activityManager = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
     if (node.isNodeType(ActivityTypeUtils.EXO_ACTIVITY_INFO)) {
       try {
         String nodeActivityID = node.getProperty(ActivityTypeUtils.EXO_ACTIVITY_ID).getString();
         activity = activityManager.getActivity(nodeActivityID);
         isNewActivity = false;
       } catch (Exception e) {
       }
     }
     
     if (activity == null) {
       if (page.isMinorEdit()) {
         return null;
       }
       activity = createNewActivity(ownerIdentity.getId());
       isNewActivity = true;
     }
     
     // Add UI params
     Map<String, String> templateParams = new HashMap<String, String>();
     templateParams.put(PAGE_ID_KEY, pageId);
     templateParams.put(ACTIVITY_TYPE_KEY, activityType);
     templateParams.put(PAGE_OWNER_KEY, wikiOwner);
     templateParams.put(PAGE_TYPE_KEY, wikiType);
     templateParams.put(PAGE_TITLE_KEY, page.getTitle());
     String pageURL = (page.getURL() == null) ? (spaceUrl != null ? (spaceUrl + "/" + WIKI_PAGE_NAME) : "") : page.getURL();
     templateParams.put(URL_KEY, pageURL);
     int versionsTotal = page.getVersionableMixin().getVersionHistory().getChildren().size() - 1;
     templateParams.put(WIKI_PAGE_VERSION, String.valueOf(versionsTotal));
     
     // Create page excerpt
     StringBuffer excerpt = new StringBuffer();
     RenderingService renderingService = (RenderingService) PortalContainer.getInstance().getComponentInstanceOfType(RenderingService.class);
     excerpt.append(renderingService.render(page.getContent().getText(), page.getSyntax(), Syntax.PLAIN_1_0.toIdString(), false));
     if (excerpt.length() > EXCERPT_LENGTH) {
       excerpt.replace(EXCERPT_LENGTH, excerpt.length(), "...");
     }
     templateParams.put(PAGE_EXCERPT, validateExcerpt(excerpt.toString()));
     templateParams.put(org.exoplatform.social.core.BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, PAGE_EXCERPT);
     activity.setTemplateParams(templateParams);
     
     // Save activity
     if (isNewActivity) {
       activityManager.saveActivityNoReturn(ownerStream, activity);
     } else {
       activityManager.updateActivity(activity);
     }
     
     // Check to add comment to activity
     WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
     ResourceBundle res = context.getApplicationResourceBundle();
     if (!ADD_PAGE_TYPE.equals(activityType)) {
       String verName = ((PageImpl) page).getVersionableMixin().getBaseVersion().getName();
       templateParams.put(VIEW_CHANGE_URL_KEY, Utils.getURL(page.getURL(), verName));
       
       
       if (EDIT_PAGE_TITLE_TYPE.equals(activityType) && !page.isMinorEdit()) {
         createAndSaveComment(activity, res.getString("WikiUIActivity.msg.update-page-title") + page.getTitle(), ownerIdentity.getId());
       } else if (EDIT_PAGE_CONTENT_TYPE.equals(activityType) && !page.isMinorEdit()) {
         String comment = page.getComment();
         if (StringUtils.isEmpty(comment)) {
           createAndSaveComment(activity, res.getString("WikiUIActivity.msg.update-page-content"), ownerIdentity.getId());
         } else {
           createAndSaveComment(activity, comment, ownerIdentity.getId());
         }
       } else if (EDIT_PAGE_CONTENT_AND_TITLE_TYPE.equals(activityType) && !page.isMinorEdit()) {
         StringBuffer commentContent = new StringBuffer();
         commentContent.append(res.getString("WikiUIActivity.msg.update-page-title"));
         commentContent.append(page.getTitle());
         commentContent.append("<br>");
         
         String comment = page.getComment();
         if (StringUtils.isEmpty(comment)) {
           commentContent.append(res.getString("WikiUIActivity.msg.update-page-content"));
         } else {
           commentContent.append(comment);
         }
         createAndSaveComment(activity, commentContent.toString(), ownerIdentity.getId());
       } else if (MOVE_PAGE_TYPE.equals(activityType)) {
         WikiService wikiService = (WikiService) PortalContainer.getInstance().getComponentInstanceOfType(WikiService.class);
         List<BreadcrumbData> breadcrumbDatas = wikiService.getBreadcumb(wikiType, wikiOwner, pageId);
         StringBuffer breadcrumText = new StringBuffer();
         for (int i = 0; i < breadcrumbDatas.size(); i++) {
           breadcrumText.append(breadcrumbDatas.get(i).getTitle());
           if (i < breadcrumbDatas.size() - 1) {
             breadcrumText.append(" > ");
           }
         }
         createAndSaveComment(activity, res.getString("WikiUIActivity.msg.move-page") + breadcrumText.toString(), ownerIdentity.getId());
       }
     }
     return activity;
   }
   
   private String validateExcerpt(String excerpt) {
     String[] lines = excerpt.split("\n");
     for (int i = 0; i < lines.length; i++) {
       if (lines[i].length() > EXCERPT_LENGTH) {
         lines[i] = lines[i].substring(0, EXCERPT_LENGTH) + "...";
       }
     }
     
     StringBuffer result = new StringBuffer();
     for (int i = 0; i < Math.min(lines.length, 4); i++) {
       result.append(lines[i]);
       result.append("\n");
     }
     
     if (lines.length > 4) {
       result.append("...");
     }
     return result.toString();
   }
   
   private void createAndSaveComment(ExoSocialActivity activity, String comment, String userId) {
     ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
     ExoSocialActivity newComment = new ExoSocialActivityImpl();
     newComment.setTitle(comment);
     newComment.setUserId(userId);
     activityM.saveComment(activity, newComment);
   }
   
   private boolean isPublic(Page page) throws Exception {
     HashMap<String, String[]> permissions = page.getPermission();
     // the page is public when it has permission: [any read]
     return permissions != null && permissions.containsKey(IdentityConstants.ANY) && ArrayUtils.contains(permissions.get(IdentityConstants.ANY), PermissionType.READ);
   }
   
   /**
    * Check If a page can be read by all users of a space
    * 
    * @param page Page
    * @param space Space
    * @return true : can, false : not can;
    * @throws Exception
    */
   private boolean isPublicInSpace(Page page, Space space) throws Exception {
     HashMap<String, String[]> pagePermissions = page.getPermission();
     String groupMemberShip = MembershipEntry.ANY_TYPE + ":" + space.getGroupId();
     return (pagePermissions.containsKey(groupMemberShip) && ArrayUtils.contains(pagePermissions.get(groupMemberShip), PermissionType.READ));
   }
   
   private void saveActivity(String wikiType, String wikiOwner, String pageId, Page page, String activityType) throws Exception {
     try {
       Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
     } catch (ClassNotFoundException e) {
       if (LOG.isDebugEnabled()) {
         LOG.debug("eXo Social components not found!", e);
       }
       return;
     }
     
     String username = ConversationState.getCurrent().getIdentity().getUserId();
     IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
     Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
     
     Identity ownerStream = null, authorActivity = userIdentity;
     ExoSocialActivity activity = null;
     String spaceUrl = null;
     if (PortalConfig.GROUP_TYPE.equals(wikiType)) {
       /* checking whether the page is in a space */
      String groupId = "/" + wikiOwner;
       SpaceService spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
       Space space = null;
       try {
        space = spaceService.getSpaceByGroupId(groupId);
         if (space != null) {
           if (!isPublicInSpace(page, space)) return;
           ownerStream = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
           spaceUrl = space.getUrl();
         }
       } catch (SpaceStorageException e) {
         if (LOG.isDebugEnabled()) {
          LOG.debug(String.format("Space %s not existed", groupId), e);
         }
       }
     }
     
     if (ownerStream == null && isPublic(page)) {
       // if the page is public, publishing the activity in the user stream.
       ownerStream = userIdentity;
     }
     
     if (ownerStream != null) {
       activity = generateActivity(ownerStream, authorActivity, wikiType, wikiOwner, pageId, page, spaceUrl, activityType);
       if (activity == null) {
         return;
       }
       
       // Attach activity id to jcr node
       Node node = page.getJCRPageNode();
       String activityId = activity.getId();
       if (!StringUtils.isEmpty(activityId)) {
         ActivityTypeUtils.attachActivityId(node, activityId);
       }
     }
   }
 
   @Override
   public void postAddPage(String wikiType, String wikiOwner, String pageId, Page page) throws Exception {
     if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId)) {
       // catch the case of the Wiki Home added as it's created by the system, not by users.
       return;
     }
     saveActivity(wikiType, wikiOwner, pageId, page, PageWikiListener.ADD_PAGE_TYPE);
   }
 
   @Override
   public void postDeletePage(String wikiType, String wikiOwner, String pageId, Page page) throws Exception {
     Node node = page.getJCRPageNode();
     ActivityManager activityManager = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
     if (node.isNodeType(ActivityTypeUtils.EXO_ACTIVITY_INFO)) {
       String nodeActivityID = node.getProperty(ActivityTypeUtils.EXO_ACTIVITY_ID).getString();
       activityManager.deleteActivity(nodeActivityID);
     }
   }
 
   @Override
   public void postUpdatePage(String wikiType, String wikiOwner, String pageId, Page page, String wikiUpdateType) throws Exception {
     if(page != null) {
       saveActivity(wikiType, wikiOwner, pageId, page, wikiUpdateType);
     }
   }
 }
