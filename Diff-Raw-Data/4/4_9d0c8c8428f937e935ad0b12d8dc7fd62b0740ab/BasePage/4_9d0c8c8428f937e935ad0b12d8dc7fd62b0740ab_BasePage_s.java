 package hu.sch.kurzuscsere.page;
 
 import hu.sch.kurzuscsere.WicketApplication;
 import hu.sch.kurzuscsere.authz.UserAuthorization;
 import hu.sch.kurzuscsere.domain.User;
 import hu.sch.kurzuscsere.logic.UserManager;
 import hu.sch.kurzuscsere.panel.DevUserSwitchPanel;
 import hu.sch.kurzuscsere.panel.MenuPanel;
 import hu.sch.kurzuscsere.panel.VirMenuPanel;
 import hu.sch.kurzuscsere.session.AppSession;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 
 /**
  *
  * @author Kresshy
  * @version
  */
 public abstract class BasePage extends WebPage {
 
     public BasePage() {
         super();
     }
 
     @Override
     protected void onInitialize() {
         super.onInitialize();
 
         loadUser();
 
         add(new FeedbackPanel("feedbackPanel"));
 
         add(new DevUserSwitchPanel("devPanel"));
 
         add(new Label("inPageTitle", getPageTitle()));
         add(new BookmarkablePageLink<HomePage>("appNameLink", HomePage.class));
         add(new MenuPanel("menuPanel"));
         add(new VirMenuPanel("virMenuPanel"));
     }
 
     private void loadUser() {
         String remUser = getAuthorizationComponent().getRemoteUser(getRequest());
 
         if (remUser == null || remUser.equals("")) { // no sso login
             getSession().setUserId(0L);
             return;
         }
 
         Long userId = UserManager.getInstance().getUserId(remUser);
         if (userId.equals(0L) || !userId.equals(getSession().getUserId())) {
             // nem egyezik, írjuk felül az eddig ismertet
             User userAttrs =
                     getAuthorizationComponent().getUserAttributes(getRequest());
             if (userAttrs != null) {
                 userAttrs.setId(userId);
                 UserManager.getInstance().updateUserAttributes(userAttrs);
             }
             getSession().setUserId(userId);
         }
     }
 
     protected UserAuthorization getAuthorizationComponent() {
         return ((WicketApplication) getApplication()).getAuthorizationComponent();
     }
 
     @Override
     public AppSession getSession() {
         return (AppSession) super.getSession();
     }
 
     protected abstract String getPageTitle();
 }
