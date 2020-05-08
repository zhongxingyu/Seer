 package vahdin;
 
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import vahdin.component.GoogleMap;
 import vahdin.component.OAuth2Button;
 import vahdin.component.OAuth2Button.AuthEvent;
 import vahdin.data.Bust;
 import vahdin.data.Mark;
 import vahdin.data.User;
 import vahdin.view.BustsView;
 import vahdin.view.MarksView;
 import vahdin.view.NewBustView;
 import vahdin.view.NewMarkView;
 import vahdin.view.SingleBustView;
import vahdin.view.SuggestedMarkView;
 
 import com.vaadin.annotations.JavaScript;
 import com.vaadin.annotations.Theme;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.event.MethodEventSource;
 import com.vaadin.navigator.Navigator;
 import com.vaadin.navigator.Navigator.ComponentContainerViewDisplay;
 import com.vaadin.server.ExternalResource;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Link;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * Main UI class
  */
 @Theme("vahdintheme")
 @JavaScript({ "component/js/jquery-1.9.1.min.js", "component/js/plugins.js",
         "component/js/google_map.js", "component/js/jso.js",
         "component/js/oauth2_button.js" })
 @SuppressWarnings("serial")
 public class VahdinUI extends UI implements MethodEventSource {
 
     private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD723LQ68aCdI37_yhUNDQVHj3zzAfPDVo";
     private static final Logger logger = Logger.getGlobal();
 
     private GoogleMap map;
 
     private User currentUser = User.guest();
     private Label score = new Label("");
 
     private Window loginWindow;
 
     /**
      * Initializes the UI.
      * 
      * @param request
      *            Vaadin Request
      */
     @Override
     protected void init(VaadinRequest request) {
 
         getPage().setTitle("Vahdin");
 
         map = new GoogleMap(GOOGLE_MAPS_API_KEY);
         map.setSizeFull();
 
         VerticalLayout sidebar = new VerticalLayout();
 
         CustomLayout layout = new CustomLayout("base-template");
         buildMenuBar(layout);
         layout.addComponent(map, "map-container");
         layout.addComponent(sidebar, "sidebar-container");
         layout.setSizeFull();
 
         setContent(layout);
         ComponentContainerViewDisplay viewDisplay = new ComponentContainerViewDisplay(
                 sidebar);
         Navigator navigator = new Navigator(UI.getCurrent(), viewDisplay);
         navigator.addView("", MarksView.class);
         navigator.addView("bust", SingleBustView.class);
         navigator.addView("busts", BustsView.class);
         navigator.addView("newbust", NewBustView.class);
         navigator.addView("newmark", NewMarkView.class);
        navigator.addView("suggestedmark", SuggestedMarkView.class);
     }
 
     /**
      * Adds a click listener to the map.
      * 
      * @param listener
      *            The listener to add.
      */
     public void addMapClickListener(GoogleMap.ClickListener listener) {
         map.addClickListener(listener);
     }
 
     /**
      * Removes a click listener from the map.
      * 
      * @param listener
      *            The listener to remove.
      */
     public void removeMapClickListener(GoogleMap.ClickListener listener) {
         map.removeClickListener(listener);
     }
 
     /**
      * Adds a marker to the map.
      * 
      * @param latitude
      * @param longitude
      */
     public GoogleMap.Marker addMarker(double latitude, double longitude) {
         return map.addMarker(latitude, longitude);
     }
 
     /**
      * Removes a marker from the map.
      * 
      * @param marker
      */
     public void removeMarker(GoogleMap.Marker marker) {
         map.removeMarker(marker);
     }
 
     /**
      * Centers the map on the given coordinates.
      * 
      * @param latitude
      * @param longitude
      */
     public void centerMapOn(double latitude, double longitude) {
         map.center(latitude, longitude);
     }
 
     /**
      * Shows the busts of the specified mark on the map.
      * 
      * @param mark
      */
     public void showBusts(Mark mark) {
         clearMap();
         List<Bust> busts = mark.getBusts();
         for (Bust bust : busts) {
             map.addMarker(bust.getLocationLat(), bust.getLocationLon());
         }
         if (busts.size() > 0) {
             Bust bust = busts.get(0);
             centerMapOn(bust.getLocationLat(), bust.getLocationLon());
         }
     }
 
     /** Clears the map. */
     public void clearMap() {
         map.removeMarkers();
     }
 
     /**
      * Builds the menu bar by adding the necessary components.
      * 
      * @param layout
      *            The layout to add the components to.
      */
     private String getScoreString() {
         String score;
         if (currentUser.isLoggedIn()) {
             score = ("(" + currentUser.getExperience() + " - "
                     + currentUser.getPrestigeRank() + ")");
         } else {
             score = ("");
         }
         return score;
     }
 
     private void buildMenuBar(CustomLayout layout) {
 
         final VahdinUI ui = (VahdinUI) UI.getCurrent();
 
         Button logoLink = new Button();
         logoLink.setSizeFull();
         logoLink.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 ui.getNavigator().navigateTo("/");
             }
         });
 
         Link userGuideLink = new Link("", new ExternalResource(
                 "https://dl.dropbox.com/u/733138/vahdin_user_guide.htm"));
         userGuideLink.setTargetName("_blank");
 
         loginWindow = buildLoginWindow();
 
         final Button loginLink = new Button();
         loginLink.addStyleName("login-link");
         loginLink.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 User currentUser = ui.getCurrentUser();
                 if (currentUser.isLoggedIn()) {
                     ui.setCurrentUser(User.guest());
                 } else {
                     openLoginWindow();
                 }
             }
         });
 
         final Label username = new Label(getCurrentUser().getName());
 
         addLoginListener(new LoginListener() {
             @Override
             public void login(LoginEvent event) {
                 User currentUser = ui.getCurrentUser();
                 if (currentUser.isLoggedIn()) {
                     loginLink.removeStyleName("login-link");
                     loginLink.addStyleName("logout-link");
                     score.setValue(getScoreString());
                 } else {
                     loginLink.removeStyleName("logout-link");
                     loginLink.addStyleName("login-link");
                     score.setValue("");
                 }
                 username.setValue(currentUser.getName());
             }
         });
 
         layout.addComponent(logoLink, "logo-link");
         layout.addComponent(userGuideLink, "stats-link");
         layout.addComponent(loginLink, "login-logout-link");
         layout.addComponent(score, "user-score");
         layout.addComponent(username, "username");
 
     }
 
     /**
      * Builds the registration window.
      * 
      * @param user
      *            The user in question
      * @return The window that was built.
      */
     private Window buildRegistrationWindow(final User user) {
         final Window window = new Window("Register");
         window.setModal(true);
         window.setStyleName("registration-window");
 
         final Label nicktitle = new Label("Invent your self an alias");
         final TextField alias = new TextField();
         final Button okgo = new Button("Ok, go!");
         okgo.setStyleName("submit-button");
 
         VerticalLayout layout = new VerticalLayout();
         layout.addComponent(nicktitle);
         layout.addComponent(alias);
         layout.addComponent(okgo);
 
         okgo.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 String name = alias.getValue();
                 if (name.length() > 0) {
                     // save and log in
                     user.setName(name);
                     try {
                         logger.info("Registering a new user: " + name);
                         user.save();
                         User.commit();
                         setCurrentUser(user);
                         UI.getCurrent().removeWindow(window);
                         return;
                     } catch (SQLException e) {
                         e.printStackTrace();
                         user.setName("");
                     }
                 }
                 // something went wrong, mark the name invalid
                 alias.addStyleName("invalid");
                 alias.addTextChangeListener(new TextChangeListener() {
                     @Override
                     public void textChange(TextChangeEvent event) {
                         alias.removeStyleName("invalid");
                         alias.addTextChangeListener(new TextChangeListener() {
                             @Override
                             public void textChange(TextChangeEvent event) {
                                 alias.removeStyleName("invalid");
                                 alias.removeTextChangeListener(this);
                             }
                         });
                     }
                 });
             }
         });
 
         window.setContent(layout);
 
         return window;
     }
 
     /**
      * Builds the login window.
      * 
      * @return The window that was built.
      */
     private Window buildLoginWindow() {
         final VahdinUI ui = (VahdinUI) UI.getCurrent();
 
         final Window window = new Window("Log in");
         window.setModal(true);
         window.setStyleName("login-window");
 
         OAuth2Button google = new OAuth2Button("google");
         google.addAuthListener(new OAuth2Button.AuthListener() {
             @Override
             public void auth(AuthEvent event) {
                 ui.removeWindow(window);
                 User user = User.load("google:" + event.userId);
                 if (user == null) { // new user
                     user = new User("google:" + event.userId);
                     ui.addWindow(buildRegistrationWindow(user));
                 } else {
                     ui.setCurrentUser(user);
                 }
             }
         });
 
         OAuth2Button facebook = new OAuth2Button("facebook");
         facebook.addAuthListener(new OAuth2Button.AuthListener() {
             @Override
             public void auth(AuthEvent event) {
                 ui.removeWindow(window);
                 User user = User.load("facebook:" + event.userId);
                 if (user == null) { // new user
                     user = new User("facebook:" + event.userId);
                     ui.addWindow(buildRegistrationWindow(user));
                 } else {
                     ui.setCurrentUser(user);
                 }
             }
         });
 
         VerticalLayout layout = new VerticalLayout();
         layout.addComponent(google);
         layout.addComponent(facebook);
 
         window.setContent(layout);
 
         return window;
     }
 
     /** Opens the login window. */
     public void openLoginWindow() {
         UI.getCurrent().addWindow(loginWindow);
     }
 
     /**
      * Gets the current user.
      * 
      * @return The currently logged in user, or a guest user if there is none.
      */
     public User getCurrentUser() {
         return currentUser;
     }
 
     /**
      * Sets the current user, marks them as logged in and fires the login event.
      * 
      * @param user
      *            The user to set as currently logged in.
      */
     public void setCurrentUser(User user) {
         currentUser.markLoggedOut();
         currentUser = user;
         if (!currentUser.isGuest()) {
             currentUser.markLoggedIn();
         }
         fireEvent(new LoginEvent(this));
     }
 
     /**
      * Adds a login listener.
      * 
      * @param listener
      *            The listener to add.
      */
     public void addLoginListener(LoginListener listener) {
         addListener(LoginEvent.class, listener, LoginListener.LOGIN_METHOD);
     }
 
     /**
      * Removes a login listener.
      * 
      * @param listener
      *            The listener to remove.
      */
     public void removeLoginListener(LoginListener listener) {
         removeListener(LoginEvent.class, listener, LoginListener.LOGIN_METHOD);
     }
 
     /** Login event listener. */
     public static abstract class LoginListener {
 
         public static final Method LOGIN_METHOD;
 
         static {
             try {
                 LOGIN_METHOD = LoginListener.class.getMethod("login",
                         LoginEvent.class);
             } catch (NoSuchMethodException e) {
                 throw new Error(e);
             }
         }
 
         /***/
         public abstract void login(LoginEvent event);
 
     }
 
     /** Login event. */
     public static class LoginEvent extends Component.Event {
 
         private LoginEvent(UI source) {
             super(source);
         }
     }
 }
