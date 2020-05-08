 package at.ac.tuwien.swag.webapp.in;
 
 import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.Panel;
 
 import at.ac.tuwien.swag.webapp.in.admin.CreateMap;
 import at.ac.tuwien.swag.webapp.in.admin.DropDBPage;
 import at.ac.tuwien.swag.webapp.in.admin.ViewDBPage;
 import at.ac.tuwien.swag.webapp.in.admin.monitoring.Monitoring;
 
 @AuthorizeInstantiation("ADMIN")
 public class InSideNavigation extends Panel {
     private static final long serialVersionUID = 6811737388664263612L;
 
     public InSideNavigation(String id) {
         super(id);
        
         add(new BookmarkablePageLink<String>("createMap",  CreateMap.class));
         add(new BookmarkablePageLink<String>("monitoring", Monitoring.class));
         add(new BookmarkablePageLink<String>("viewDB",     ViewDBPage.class));
         add(new BookmarkablePageLink<String>("dropDB",     DropDBPage.class));
     }
 
 }
