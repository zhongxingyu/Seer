 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.acegisecurity.Authentication;
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.Session;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.StatelessForm;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.StringResourceModel;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.config.GeoServer;
 import org.geoserver.config.GeoServerLoader;
 import org.geoserver.web.acegi.GeoServerSession;
 
 /**
  * Base class for web pages in GeoServer web application.
  * <ul>
  * <li>The basic layout</li>
  * <li>An OO infrastructure for common elements location</li>
  * <li>An infrastructure for locating subpages in the Spring context and
  * creating links</li>
  * </ul>
  * 
  * TODO: breadcrumb automated cration. This can be done by using a list of
  * {@link BookmarkablePageInfo} instances that needs to be passed to each page,
  * a custom PageLink subclass that provides that information, and some code
  * coming from {@link BreadCrumbBar}. <br>
  * See also this discussion on the wicket users mailing list:
  * http://www.nabble.com/Bread-crumbs-based-on-pages%2C-not-panels--tf2244730.html#a6225855
  * 
  * @author Andrea Aaime, The Open Planning Project
  * @author Justin Deoliveira, The Open Planning Project
  */
 public class GeoServerBasePage extends WebPage {
 
 	@SuppressWarnings("serial")
     public GeoServerBasePage() {
 
         // login form
         WebMarkupContainer loginForm = new WebMarkupContainer("loginform");
         add(loginForm);
         final Authentication user = GeoServerSession.get().getAuthentication();
         final boolean anonymous = user == null;
         loginForm.setVisible(anonymous);
 
         WebMarkupContainer logoutForm = new WebMarkupContainer("logoutform");
         logoutForm.setVisible(user != null);
 
         add(logoutForm);
         logoutForm.add(new Label("username", anonymous ? "Nobody" : user.getName()));
 
         // home page link
         add( new BookmarkablePageLink( "home", GeoServerHomePage.class )
             .add( new Label( "label", new StringResourceModel( "home", (Component)null, null ) )  ) );
         
         // dev buttons
         WebMarkupContainer devButtons = new WebMarkupContainer("devButtons");
         add(devButtons);
         devButtons.add(new AjaxFallbackLink("clearCache"){
             @Override
             public void onClick(AjaxRequestTarget target) {
                 getGeoServerApplication().clearWicketCaches();
             }
         });
         devButtons.setVisible(Application.DEVELOPMENT.equalsIgnoreCase(
                 getApplication().getConfigurationType()));
         
         final Map<Category,List<MenuPageInfo>> links = splitByCategory(
             filterSecured(getGeoServerApplication().getBeansOfType(MenuPageInfo.class))
         );
 
         List<MenuPageInfo> standalone = links.containsKey(null) 
             ? links.get(null)
             : new ArrayList<MenuPageInfo>();
         links.remove(null);
 
         List<Category> categories = new ArrayList(links.keySet());
         Collections.sort(categories);
 
         add(new ListView("category", categories){
             public void populateItem(ListItem item){
                 Category category = (Category)item.getModelObject();
                 item.add(new Label("category.header", new StringResourceModel(category.getNameKey(), (Component) null, null)));
                 item.add(new ListView("category.links", links.get(category)){
                     public void populateItem(ListItem item){
                         MenuPageInfo info = (MenuPageInfo)item.getModelObject();
                         BookmarkablePageLink link = new BookmarkablePageLink("link", info.getComponentClass());
                         link.add(new Label("link.label", new StringResourceModel(info.getTitleKey(), (Component) null, null)));
                         if(info.getIcon() != null) {
                             link.add(new Image("link.icon", new ResourceReference(info.getComponentClass(), info.getIcon())));
                         } else {
                             link.add(new Image("link.icon", new ResourceReference(GeoServerBasePage.class, "img/icons/silk/wrench.png")));
                         }
                         item.add(link);
                     }
                 });
             }
         });
 
         add(new ListView("standalone", standalone){
                     public void populateItem(ListItem item){
                         MenuPageInfo info = (MenuPageInfo)item.getModelObject();
                         item.add(new BookmarkablePageLink("link", info.getComponentClass())
                             .add(new Label("link.label", new StringResourceModel(info.getTitleKey(), (Component) null, null)))
                         );
                     }
                 }
         );
 
         //save + load
         AjaxLink save = new AjaxLink("save") {
             public void onClick(AjaxRequestTarget target) {
                 GeoServerLoader loader = 
                     getGeoServerApplication().getBeanOfType( GeoServerLoader.class );
                 try {
                     loader.persist();
                     Session.get().info( "Changes saved successfully." );
                 } 
                 catch (Exception e) {
                     Session.get().error( "Error occured during save: '" + e.getLocalizedMessage() + "'");
                 }
                 
                 setResponsePage(GeoServerHomePage.class);
             }
             
         };
         add(save);
         AjaxLink load = new AjaxLink("load"){
             public void onClick(AjaxRequestTarget target) {
                 GeoServerLoader loader = 
                     getGeoServerApplication().getBeanOfType( GeoServerLoader.class );
                 try {
                     loader.reload();
                     Session.get().info( "All outstanding changes reverted." );
                 } 
                 catch (Exception e) {
                     Session.get().error( "Error occured reverting changes: '" + e.getLocalizedMessage() + "'");
                 }
                 
                 setResponsePage(GeoServerHomePage.class);
             }
             
         };
         add(load);
         
         // Make sure the user has admin rights before showing these buttons
         if(!GeoServerSecuredPage.DEFAULT_AUTHORIZER.isAccessAllowed(this.getClass(),
                 getSession().getAuthentication())) {
             load.setVisible(false);
             save.setVisible(false);
         }
         
         add(new FeedbackPanel("feedback"));
     }
 
     /**
      * Returns the application instance.
      */
     protected GeoServerApplication getGeoServerApplication() {
         return (GeoServerApplication) getApplication();
     }
     
     @Override
     public GeoServerSession getSession() {
         return (GeoServerSession) super.getSession();
     }
 
     /**
      * Convenience method for pages to get access to the geoserver
      * configuration.
      */
     protected GeoServer getGeoServer() {
         return getGeoServerApplication().getGeoServer();
     }
 
     /**
      * Convenience method for pages to get access to the catalog.
      */
     protected Catalog getCatalog() {
         return getGeoServerApplication().getCatalog();
     }
     
     /**
      * Splits up the pages by category, turning the list into a map keyed by category
      * @param pages
      * @return
      */
     private Map<Category,List<MenuPageInfo>> splitByCategory(List<MenuPageInfo> pages){
         Collections.sort(pages);
         HashMap<Category,List<MenuPageInfo>> map = new HashMap<Category,List<MenuPageInfo>>();
 
         for (MenuPageInfo page : pages){
             Category cat = page.getCategory();
 
             if (!map.containsKey(cat)) 
                 map.put(cat, new ArrayList<MenuPageInfo>());
 
             map.get(cat).add(page);
         }
 
         return map;
     }
     
     /**
      * Filters out all of the pages that cannot be accessed by the current user
      * @param pageList
      * @return
      */
     private List<MenuPageInfo> filterSecured(List<MenuPageInfo> pageList) {
         Authentication user = getSession().getAuthentication();
         List<MenuPageInfo> result = new ArrayList<MenuPageInfo>();
         for (MenuPageInfo page : pageList) {
             final Class<GeoServerBasePage> pageClass = page.getComponentClass();
             if(GeoServerSecuredPage.class.isAssignableFrom(pageClass) &&
                     !page.getPageAuthorizer().isAccessAllowed(pageClass, user))
                 continue;
             result.add(page);
         }
         return result;
     }
 }
