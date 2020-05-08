 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web;
 
 import java.util.List;
 
 import org.apache.wicket.Application;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
 import org.apache.wicket.extensions.breadcrumb.BreadCrumbBar;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.StatelessForm;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.StringResourceModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.geoserver.web.acegi.GeoServerSession;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.config.GeoServer;
 import org.geoserver.web.admin.ServerAdminPage;
 import org.geoserver.web.services.ServicePageInfo;
 
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
         Form loginForm = new SignInForm("loginform");
         add(loginForm);
         loginForm.setVisible(GeoServerSession.get().getAuthentication() == null);
 
         Form logoutForm = new StatelessForm("logoutform"){
             @Override
             public void onSubmit(){
                 GeoServerSession.get().signout();
                 setResponsePage(GeoServerHomePage.class);
             }
         };
         logoutForm.setVisible(GeoServerSession.get().getAuthentication() != null);
 
         add(logoutForm);
         logoutForm.add(new Label("username", GeoServerSession.get().getAuthentication() == null ? "Nobody" : "Some guy"));
 
        // welcome page link
        add( new BookmarkablePageLink( "welcome", GeoServerHomePage.class )
            .add( new Label( "label", new StringResourceModel( "welcome", (Component)null, null ) )  ) );
         
         // server admin link
         add( new BookmarkablePageLink( "admin.server", ServerAdminPage.class ) 
             .add( new Label( "label", new StringResourceModel( "server", (Component)null, null ) ) ) );
         
         // list of services to administer
         List<ServicePageInfo> pages =
             getGeoServerApplication().getBeansOfType( ServicePageInfo.class);
         ListView services = new ListView("admin.services", pages) {
             protected void populateItem(ListItem item) {
                 ServicePageInfo page = (ServicePageInfo) item.getModelObject();
 
                 //add a link
                 BookmarkablePageLink link = new BookmarkablePageLink("admin.service",
                         page.getComponentClass());
                 link.add(new Image( "serviceIcon", new ResourceReference( page.getComponentClass(), page.getIcon() ) ) );
                 link.add(new Label("serviceLabel", new StringResourceModel( page.getTitleKey(), (Component) null, null ) ));
                 link.add(new AttributeModifier( "title", new StringResourceModel( page.getDescriptionKey(), (Component) null, null ) ) );
                 
                 item.add(link);
             }
         };
         add( services );
         
 
         List<ShortcutPageInfo> links =
             getGeoServerApplication().getBeansOfType(ShortcutPageInfo.class);
 
         ListView shortcuts =
             new ListView("shortcuts", links){
                 protected void populateItem(ListItem item) {
                     ShortcutPageInfo info = (ShortcutPageInfo) item.getModelObject();
 
                     BookmarkablePageLink link = new BookmarkablePageLink("link", info.getComponentClass());
                     item.add(link);
                     link.add(new Label("label", new StringResourceModel(info.getTitleKey(), (Component) null, null)));
                 }
             };
 
         add(shortcuts);
 
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
     }
 
     /**
      * Returns the application instance.
      */
     protected GeoServerApplication getGeoServerApplication() {
         return (GeoServerApplication) getApplication();
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
     
     private static class SignInForm extends StatelessForm {
         private String password;
         private String username;
 
         public SignInForm(final String id){
             super(id);
             setModel(new CompoundPropertyModel(this));
             add(new TextField("username"));
             add(new PasswordTextField("password"));
         }
 
         @Override
         public final void onSubmit(){
             if (signIn(username, password)){
                 if (!continueToOriginalDestination()) {
                     setResponsePage(getApplication().getHomePage());
                 }
             } else {
                 error("Unknown username/password");
             }
         }
 
         private final boolean signIn(String username, String password) {
             return GeoServerSession.get().authenticate(username, password);
         }
     }
 
 
 }
