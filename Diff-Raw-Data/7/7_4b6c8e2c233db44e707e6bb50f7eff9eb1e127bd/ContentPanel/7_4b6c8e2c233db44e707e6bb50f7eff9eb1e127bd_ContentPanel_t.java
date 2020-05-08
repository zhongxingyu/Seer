 package com.madalla.webapp.cms;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.Model;
 
 import com.madalla.service.cms.IContentService;
 import com.madalla.service.cms.IContentServiceProvider;
 import com.madalla.webapp.CmsSession;
 import com.madalla.webapp.pages.ContentEditPage;
 
 public class ContentPanel extends Panel implements IContentAware {
     private static final long serialVersionUID = 1L;
     private Log log = LogFactory.getLog(this.getClass());
     private Class<? extends Page> returnPage;
     private String nodeName;
     private String nodeId;
 
     /**
      * 
      * @param id
      * @param contentEditPage
      * @param contentService
      * @param contentAdmin
      */
     public ContentPanel(String id, String node, Class<? extends Page> returnPage) {
         super(id);
         this.nodeName = node;
         this.nodeId = id;
         this.returnPage = returnPage;
         log.debug("Content Panel being created for node=" + node + " id=" + id);
         IContentService contentService = ((IContentServiceProvider) getApplication()).getContentService();
         String contentBody = contentService.getContentData(node, id , getSession().getLocale());
         Component contentBlock = new ContentContainer("contentBlock", id, node, contentBody);
         contentBlock.add(new AttributeModifier("class", new AbstractReadOnlyModel() {
             public Object getObject() {
                 String cssClass;
                if (((CmsSession)getSession()).isCmsAdminMode()) {
                     cssClass = "contentEdit";
                 } else {
                     cssClass = "contentBlock";
                 }
                 return cssClass;
             }
         }));
         add(contentBlock);
     }
 
     public class ContentContainer extends WebMarkupContainer {
         private static final long serialVersionUID = 1L;
 
         public ContentContainer(String id, String contentId, String contentNode, String contentBody) {
             super(id);
 
             final Boolean adminMode = ((CmsSession)getSession()).isCmsAdminMode();
             final Model contentModel = new Model(contentBody);
             
             // add content
             Component label = new Label("contentBody", contentModel){
                 protected void onBeforeRender(){
                     IContentService contentService = ((IContentServiceProvider) getApplication()).getContentService();
                     String contentBody = contentService.getContentData(nodeName, nodeId, getSession().getLocale());
                     log.debug("onBeforeRender - setting new Content.");
                     contentModel.setObject(contentBody);
                     super.onBeforeRender();
                 }
             };
             label.setEscapeModelStrings(false);
             add(label);
 
             // add link to edit it
             Link link = new BookmarkablePageLink("contentLink", ContentEditPage.class, new PageParameters(CONTENT_NODE + "="
                     + contentNode + "," + CONTENT_ID + "=" + contentId + "," + CONTENT_PAGE + "="
                     + returnPage.getName())) {
                 private static final long serialVersionUID = 1801145612969874170L;
 
                 protected final void onBeforeRender() {
                    if (((CmsSession)getSession()).isCmsAdminMode()) {
                         setEnabled(true);
                     } else {
                         setEnabled(false);
                     }
                     super.onBeforeRender();
                 }
 
             };
             link.setBeforeDisabledLink("");
             link.setAfterDisabledLink("");
             add(link);
 
         }
  
     }
 
 }
