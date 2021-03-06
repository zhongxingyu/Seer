 /* 
 @ITMillApache2LicenseForJavaFiles@
  */
 
 package com.itmill.toolkit;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.EventListener;
 import java.util.EventObject;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.Random;
 
 import com.itmill.toolkit.service.ApplicationContext;
 import com.itmill.toolkit.terminal.ApplicationResource;
 import com.itmill.toolkit.terminal.DownloadStream;
 import com.itmill.toolkit.terminal.ErrorMessage;
 import com.itmill.toolkit.terminal.ParameterHandler;
 import com.itmill.toolkit.terminal.SystemError;
 import com.itmill.toolkit.terminal.Terminal;
 import com.itmill.toolkit.terminal.URIHandler;
 import com.itmill.toolkit.terminal.VariableOwner;
 import com.itmill.toolkit.ui.AbstractComponent;
 import com.itmill.toolkit.ui.Component;
 import com.itmill.toolkit.ui.Window;
 import com.itmill.toolkit.ui.Component.Focusable;
 
 /**
  * <p>
  * Base class required for all IT Mill Toolkit applications. This class provides
  * all the basic services required by the toolkit. These services allow external
  * discovery and manipulation of the user,
  * {@link com.itmill.toolkit.ui.Window windows} and themes, and starting and
  * stopping the application.
  * </p>
  * 
  * <p>
  * As mentioned, all IT Mill Toolkit applications must inherit this class.
  * However, this is almost all of what one needs to do to create a fully
  * functional application. The only thing a class inheriting the
  * <code>Application</code> needs to do is implement the <code>init</code>
  * method where it creates the windows it needs to perform its function. Note
  * that all applications must have at least one window: the main window. The
  * first unnamed window constructed by an application automatically becomes the
  * main window which behaves just like other windows with one exception: when
  * accessing windows using URLs the main window corresponds to the application
  * URL whereas other windows correspond to a URL gotten by catenating the
  * window's name to the application URL.
  * </p>
  * 
  * <p>
  * See the class <code>com.itmill.toolkit.demo.HelloWorld</code> for a simple
  * example of a fully working application.
  * </p>
  * 
  * <p>
  * <strong>Window access.</strong> <code>Application</code> provides methods
  * to list, add and remove the windows it contains.
  * </p>
  * 
  * <p>
  * <strong>Execution control.</strong> This class includes method to start and
  * finish the execution of the application. Being finished means basically that
  * no windows will be available from the application anymore.
  * </p>
  * 
  * <p>
  * <strong>Theme selection.</strong> The theme selection process allows a theme
  * to be specified at three different levels. When a window's theme needs to be
  * found out, the window itself is queried for a preferred theme. If the window
  * does not prefer a specific theme, the application containing the window is
  * queried. If neither the application prefers a theme, the default theme for
  * the {@link com.itmill.toolkit.terminal.Terminal terminal} is used. The
  * terminal always defines a default theme.
  * </p>
  * 
  * @author IT Mill Ltd.
  * @version
  * @VERSION@
  * @since 3.0
  */
 public abstract class Application implements URIHandler, Terminal.ErrorListener {
 
     /**
      * Random window name generator.
      */
     private static Random nameGenerator = new Random();
 
     /**
      * Application context the application is running in.
      */
     private ApplicationContext context;
 
     /**
      * The current user or <code>null</code> if no user has logged in.
      */
     private Object user;
 
     /**
      * Mapping from window name to window instance.
      */
     private final Hashtable windows = new Hashtable();
 
     /**
      * Main window of the application.
      */
     private Window mainWindow = null;
 
     /**
      * The application's URL.
      */
     private URL applicationUrl;
 
     /**
      * Name of the theme currently used by the application.
      */
     private String theme = null;
 
     /**
      * Application status.
      */
     private boolean applicationIsRunning = false;
 
     /**
      * Application properties.
      */
     private Properties properties;
 
     /**
      * Default locale of the application.
      */
     private Locale locale;
 
     /**
      * List of listeners listening user changes.
      */
     private LinkedList userChangeListeners = null;
 
     /**
      * Window attach listeners.
      */
     private LinkedList windowAttachListeners = null;
 
     /**
      * Window detach listeners.
      */
     private LinkedList windowDetachListeners = null;
 
     /**
      * Application resource mapping: key <-> resource.
      */
     private final Hashtable resourceKeyMap = new Hashtable();
 
     private final Hashtable keyResourceMap = new Hashtable();
 
     private long lastResourceKeyNumber = 0;
 
     /**
      * URL where the user is redirected to on application close, or null if
      * application is just closed without redirection.
      */
     private String logoutURL = null;
 
     /**
      * Experimental API, not finalized. The default SystemMessages (read-only).
      * Change by overriding getSystemMessages() and returning
      * CustomizedSystemMessages
      */
     private static final SystemMessages DEFAULT_SYSTEM_MESSAGES = new SystemMessages();
 
     private Focusable pendingFocus;
 
     /**
      * <p>
      * Gets a window by name. Returns <code>null</code> if the application is
      * not running or it does not contain a window corresponding to the name.
      * </p>
      * 
      * <p>
      * Since version 5.0 all windows can be referenced by their names in url
      * <code>http://host:port/foo/bar/</code> where
      * <code>http://host:port/foo/</code> is the application url as returned
      * by getURL() and <code>bar</code> is the name of the window.
      * </p>
      * 
      * <p>
      * One should note that this method can, as a side effect create new windows
      * if needed by the application. This can be achieved by overriding the
      * default implementation.
      * </p>
      * 
      * <p>
      * The method should return null if the window does not exists (and is not
      * created as a side-effect) or if the application is not running anymore
      * </p>.
      * 
      * @param name
      *                the name of the window.
      * @return the window associated with the given URI or <code>null</code>
      */
     public Window getWindow(String name) {
 
         // For closed app, do not give any windows
         if (!isRunning()) {
             return null;
         }
 
         // Gets the window by name
         final Window window = (Window) windows.get(name);
 
         return window;
     }
 
     /**
      * Adds a new window to the application.
      * 
      * <p>
      * This implicitly invokes the
      * {@link com.itmill.toolkit.ui.Window#setApplication(Application)} method.
      * </p>
      * 
      * <p>
      * Note that all application-level windows can be accessed by their names in
      * url <code>http://host:port/foo/bar/</code> where
      * <code>http://host:port/foo/</code> is the application url as returned
      * by getURL() and <code>bar</code> is the name of the window. Also note
      * that not all windows should be added to application - one can also add
      * windows inside other windows - these windows show as smaller windows
      * inside those windows.
      * </p>
      * 
      * @param window
      *                the new <code>Window</code> to add. If the name of the
      *                window is <code>null</code>, an unique name is
      *                automatically given for the window.
      * @throws IllegalArgumentException
      *                 if a window with the same name as the new window already
      *                 exists in the application.
      * @throws NullPointerException
      *                 if the given <code>Window</code> is <code>null</code>.
      */
     public void addWindow(Window window) throws IllegalArgumentException,
             NullPointerException {
 
         // Nulls can not be added to application
         if (window == null) {
             return;
         }
 
         // Gets the naming proposal from window
         String name = window.getName();
 
         // Checks that the application does not already contain
         // window having the same name
         if (name != null && windows.containsKey(name)) {
 
             // If the window is already added
             if (window == windows.get(name)) {
                 return;
             }
 
             // Otherwise complain
             throw new IllegalArgumentException("Window with name '"
                     + window.getName()
                     + "' is already present in the application");
         }
 
         // If the name of the window is null, the window is automatically named
         if (name == null) {
             boolean accepted = false;
             while (!accepted) {
 
                 // Try another name
                 name = String.valueOf(Math.abs(nameGenerator.nextInt()));
                 if (!windows.containsKey(name)) {
                     accepted = true;
                 }
             }
             window.setName(name);
         }
 
         // Adds the window to application
         windows.put(name, window);
         window.setApplication(this);
 
         fireWindowAttachEvent(window);
 
         // If no main window is set, declare the window to be main window
         if (getMainWindow() == null) {
             mainWindow = window;
         }
     }
 
     /**
      * Send information to all listeners about new Windows associated with this
      * application.
      * 
      * @param window
      */
     private void fireWindowAttachEvent(Window window) {
         // Fires the window attach event
         if (windowAttachListeners != null) {
             final Object[] listeners = windowAttachListeners.toArray();
             final WindowAttachEvent event = new WindowAttachEvent(window);
             for (int i = 0; i < listeners.length; i++) {
                 ((WindowAttachListener) listeners[i]).windowAttached(event);
             }
         }
     }
 
     /**
      * Removes the specified window from the application.
      * 
      * @param window
      *                the window to be removed.
      */
     public void removeWindow(Window window) {
         if (window != null && windows.contains(window)) {
 
             // Removes the window from application
             windows.remove(window.getName());
 
             // If the window was main window, clear it
             if (getMainWindow() == window) {
                 setMainWindow(null);
             }
 
             // Removes the application from window
             if (window.getApplication() == this) {
                 window.setApplication(null);
             }
 
             fireWindowDetachEvent(window);
         }
     }
 
     private void fireWindowDetachEvent(Window window) {
         // Fires the window detach event
         if (windowDetachListeners != null) {
             final Object[] listeners = windowDetachListeners.toArray();
             final WindowDetachEvent event = new WindowDetachEvent(window);
             for (int i = 0; i < listeners.length; i++) {
                 ((WindowDetachListener) listeners[i]).windowDetached(event);
             }
         }
     }
 
     /**
      * Gets the user of the application.
      * 
      * @return the User of the application.
      */
     public Object getUser() {
         return user;
     }
 
     /**
      * <p>
      * Sets the user of the application instance. An application instance may
      * have a user associated to it. This can be set in login procedure or
      * application initialization.
      * </p>
      * <p>
      * A component performing the user login procedure can assign the user
      * property of the application and make the user object available to other
      * components of the application.
      * </p>
      * 
      * @param user
      *                the new user.
      */
     public void setUser(Object user) {
         final Object prevUser = this.user;
         if (user != prevUser && (user == null || !user.equals(prevUser))) {
             this.user = user;
             if (userChangeListeners != null) {
                 final Object[] listeners = userChangeListeners.toArray();
                 final UserChangeEvent event = new UserChangeEvent(this, user,
                         prevUser);
                 for (int i = 0; i < listeners.length; i++) {
                     ((UserChangeListener) listeners[i])
                             .applicationUserChanged(event);
                 }
             }
         }
     }
 
     /**
      * Gets the URL of the application.
      * 
      * @return the application's URL.
      */
     public URL getURL() {
         return applicationUrl;
     }
 
     /**
      * Ends the Application. In effect this will cause the application stop
      * returning any windows when asked.
      */
     public void close() {
         applicationIsRunning = false;
     }
 
     /**
      * Starts the application on the given URL.After this call the application
      * corresponds to the given URL and it will return windows when asked for
      * them.
      * 
      * Application properties are defined by servlet configuration object
      * {@link javax.servlet.ServletConfig} and they are overridden by
      * context-wide initialization parameters
      * {@link javax.servlet.ServletContext}.
      * 
      * @param applicationUrl
      *                the URL the application should respond to.
      * @param applicationProperties
      *                the Application properties as specified by the servlet
      *                configuration.
      * @param context
      *                the context application will be running in.
      * 
      */
     public void start(URL applicationUrl, Properties applicationProperties,
             ApplicationContext context) {
         this.applicationUrl = applicationUrl;
         properties = applicationProperties;
         this.context = context;
         init();
         applicationIsRunning = true;
     }
 
     /**
      * Tests if the application is running or if it has been finished.
      * 
      * @return <code>true</code> if the application is running,
      *         <code>false</code> if not.
      */
     public boolean isRunning() {
         return applicationIsRunning;
     }
 
     /**
      * Gets the set of windows contained by the application.
      * 
      * @return the Unmodifiable collection of windows.
      */
     public Collection getWindows() {
         return Collections.unmodifiableCollection(windows.values());
     }
 
     /**
      * <p>
      * Main initializer of the application. The <code>init</code> method is
      * called by the framework when the application is started, and it should
      * perform whatever initialization operations the application needs, such as
      * creating windows and adding components to them.
      * </p>
      */
     public abstract void init();
 
     /**
      * Gets the application's theme. The application's theme is the default
      * theme used by all the windows in it that do not explicitly specify a
      * theme. If the application theme is not explicitly set, the
      * <code>null</code> is returned.
      * 
      * @return the name of the application's theme.
      */
     public String getTheme() {
         return theme;
     }
 
     /**
      * Sets the application's theme.
      * <p>
      * Note that this theme can be overridden by the windows. <code>null</code>
      * implies the default terminal theme.
      * </p>
      * 
      * @param theme
      *                the new theme for this application.
      */
     public void setTheme(String theme) {
 
         // Collect list of windows not having the current or future theme
         final LinkedList toBeUpdated = new LinkedList();
         final String myTheme = getTheme();
         for (final Iterator i = getWindows().iterator(); i.hasNext();) {
             final Window w = (Window) i.next();
             final String windowTheme = w.getTheme();
             if ((windowTheme == null)
                     || (!theme.equals(windowTheme) && windowTheme
                             .equals(myTheme))) {
                 toBeUpdated.add(w);
             }
         }
 
         // Updates the theme
         this.theme = theme;
 
         // Ask windows to update themselves
         for (final Iterator i = toBeUpdated.iterator(); i.hasNext();) {
             ((Window) i.next()).requestRepaint();
         }
     }
 
     /**
      * Gets the mainWindow of the application.
      * 
      * @return the main window.
      */
     public Window getMainWindow() {
         return mainWindow;
     }
 
     /**
      * <p>
      * Sets the mainWindow. If the main window is not explicitly set, the main
      * window defaults to first created window. Setting window as a main window
      * of this application also adds the window to this application.
      * </p>
      * 
      * @param mainWindow
      *                the mainWindow to set.
      */
     public void setMainWindow(Window mainWindow) {
 
         addWindow(mainWindow);
         this.mainWindow = mainWindow;
     }
 
     /**
      * Returns an enumeration of all the names in this application.
      * 
      * See {@link #start(URL, Properties, ApplicationContext)} how properties
      * are defined.
      * 
      * @return an enumeration of all the keys in this property list, including
      *         the keys in the default property list.
      * 
      */
     public Enumeration getPropertyNames() {
         return properties.propertyNames();
     }
 
     /**
      * Searches for the property with the specified name in this application.
      * This method returns <code>null</code> if the property is not found.
      * 
      * See {@link #start(URL, Properties, ApplicationContext)} how properties
      * are defined.
      * 
      * @param name
      *                the name of the property.
      * @return the value in this property list with the specified key value.
      */
     public String getProperty(String name) {
         return properties.getProperty(name);
     }
 
     /**
      * Adds new resource to the application. The resource can be accessed by the
      * user of the application.
      * 
      * @param resource
      *                the resource to add.
      */
     public void addResource(ApplicationResource resource) {
 
         // Check if the resource is already mapped
         if (resourceKeyMap.containsKey(resource)) {
             return;
         }
 
         // Generate key
         final String key = String.valueOf(++lastResourceKeyNumber);
 
         // Add the resource to mappings
         resourceKeyMap.put(resource, key);
         keyResourceMap.put(key, resource);
     }
 
     /**
      * Removes the resource from the application.
      * 
      * @param resource
      *                the resource to remove.
      */
     public void removeResource(ApplicationResource resource) {
         final Object key = resourceKeyMap.get(resource);
         if (key != null) {
             resourceKeyMap.remove(resource);
             keyResourceMap.remove(key);
         }
     }
 
     /**
      * Gets the relative uri of the resource.
      * 
      * @param resource
      *                the resource to get relative location.
      * @return the relative uri of the resource.
      */
     public String getRelativeLocation(ApplicationResource resource) {
 
         // Gets the key
         final String key = (String) resourceKeyMap.get(resource);
 
         // If the resource is not registered, return null
         if (key == null) {
             return null;
         }
 
         final String filename = resource.getFilename();
         if (filename == null) {
             return "APP/" + key + "/";
         } else {
             return "APP/" + key + "/" + filename;
         }
     }
 
     /**
      * This method gets called by terminal. It has lots of duties like to pass
      * uri handler to proper uri handlers registered to windows etc.
      * 
      * In most situations developers should NOT OVERRIDE this method. Instead
      * developers should implement and register uri handlers to windows.
      * 
      * @see com.itmill.toolkit.terminal.URIHandler#handleURI(URL, String)
      */
     public DownloadStream handleURI(URL context, String relativeUri) {
 
         // If the relative uri is null, we are ready
         if (relativeUri == null) {
             return null;
         }
 
         // Resolves the prefix
         String prefix = relativeUri;
         final int index = relativeUri.indexOf('/');
         if (index >= 0) {
             prefix = relativeUri.substring(0, index);
         }
 
         // Handles the resource requests
         if (prefix.equals("APP")) {
 
             // Handles the resource request
             final int next = relativeUri.indexOf('/', index + 1);
             if (next < 0) {
                 return null;
             }
             final String key = relativeUri.substring(index + 1, next);
             final ApplicationResource resource = (ApplicationResource) keyResourceMap
                     .get(key);
             if (resource != null) {
                return resource.getStream();
             }
 
             // Resource requests override uri handling
             return null;
         }
 
         // If the uri is in some window, handle the window uri
         Window window = getWindow(prefix);
         if (window != null) {
             URL windowContext;
             try {
                 windowContext = new URL(context, prefix + "/");
                 final String windowUri = relativeUri.length() > prefix.length() + 1 ? relativeUri
                         .substring(prefix.length() + 1)
                         : "";
                 return window.handleURI(windowContext, windowUri);
             } catch (final MalformedURLException e) {
                 return null;
             }
         }
 
         // If the uri was not pointing to a window, handle the
         // uri in main window
         window = getMainWindow();
         if (window != null) {
             return window.handleURI(context, relativeUri);
         }
 
         return null;
     }
 
     /**
      * Gets the default locale for this application.
      * 
      * @return the locale of this application.
      */
     public Locale getLocale() {
         if (locale != null) {
             return locale;
         }
         return Locale.getDefault();
     }
 
     /**
      * Sets the default locale for this application.
      * 
      * @param locale
      *                the Locale object.
      * 
      */
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     /**
      * <p>
      * An event that characterizes a change in the current selection.
      * </p>
      * Application user change event sent when the setUser is called to change
      * the current user of the application.
      * 
      * @version
      * @VERSION@
      * @since 3.0
      */
     public class UserChangeEvent extends java.util.EventObject {
 
         /**
          * Serial generated by eclipse.
          */
         private static final long serialVersionUID = 3544951069307188281L;
 
         /**
          * New user of the application.
          */
         private final Object newUser;
 
         /**
          * Previous user of the application.
          */
         private final Object prevUser;
 
         /**
          * Constructor for user change event.
          * 
          * @param source
          *                the application source.
          * @param newUser
          *                the new User.
          * @param prevUser
          *                the previous User.
          */
         public UserChangeEvent(Application source, Object newUser,
                 Object prevUser) {
             super(source);
             this.newUser = newUser;
             this.prevUser = prevUser;
         }
 
         /**
          * Gets the new user of the application.
          * 
          * @return the new User.
          */
         public Object getNewUser() {
             return newUser;
         }
 
         /**
          * Gets the previous user of the application.
          * 
          * @return the previous Toolkit user, if user has not changed ever on
          *         application it returns <code>null</code>
          */
         public Object getPreviousUser() {
             return prevUser;
         }
 
         /**
          * Gets the application where the user change occurred.
          * 
          * @return the Application.
          */
         public Application getApplication() {
             return (Application) getSource();
         }
     }
 
     /**
      * The <code>UserChangeListener</code> interface for listening application
      * user changes.
      * 
      * @version
      * @VERSION@
      * @since 3.0
      */
     public interface UserChangeListener extends EventListener {
 
         /**
          * The <code>applicationUserChanged</code> method Invoked when the
          * application user has changed.
          * 
          * @param event
          *                the change event.
          */
         public void applicationUserChanged(Application.UserChangeEvent event);
     }
 
     /**
      * Adds the user change listener.
      * 
      * @param listener
      *                the user change listener to add.
      */
     public void addListener(UserChangeListener listener) {
         if (userChangeListeners == null) {
             userChangeListeners = new LinkedList();
         }
         userChangeListeners.add(listener);
     }
 
     /**
      * Removes the user change listener.
      * 
      * @param listener
      *                the user change listener to remove.
      */
     public void removeListener(UserChangeListener listener) {
         if (userChangeListeners == null) {
             return;
         }
         userChangeListeners.remove(listener);
         if (userChangeListeners.isEmpty()) {
             userChangeListeners = null;
         }
     }
 
     /**
      * Window detach event.
      */
     public class WindowDetachEvent extends EventObject {
 
         /**
          * Serial generated by eclipse.
          */
         private static final long serialVersionUID = 3544669568644691769L;
 
         private final Window window;
 
         /**
          * Creates a event.
          * 
          * @param window
          *                the Detached window.
          */
         public WindowDetachEvent(Window window) {
             super(Application.this);
             this.window = window;
         }
 
         /**
          * Gets the detached window.
          * 
          * @return the detached window.
          */
         public Window getWindow() {
             return window;
         }
 
         /**
          * Gets the application from which the window was detached.
          * 
          * @return the Application.
          */
         public Application getApplication() {
             return (Application) getSource();
         }
     }
 
     /**
      * Window attach event.
      */
     public class WindowAttachEvent extends EventObject {
 
         /**
          * Serial generated by eclipse.
          */
         private static final long serialVersionUID = 3977578104367822392L;
 
         private final Window window;
 
         /**
          * Creates a event.
          * 
          * @param window
          *                the Attached window.
          */
         public WindowAttachEvent(Window window) {
             super(Application.this);
             this.window = window;
         }
 
         /**
          * Gets the attached window.
          * 
          * @return the attached window.
          */
         public Window getWindow() {
             return window;
         }
 
         /**
          * Gets the application to which the window was attached.
          * 
          * @return the Application.
          */
         public Application getApplication() {
             return (Application) getSource();
         }
     }
 
     /**
      * Window attach listener interface.
      */
     public interface WindowAttachListener {
 
         /**
          * Window attached
          * 
          * @param event
          *                the window attach event.
          */
         public void windowAttached(WindowAttachEvent event);
     }
 
     /**
      * Window detach listener interface.
      */
     public interface WindowDetachListener {
 
         /**
          * Window detached.
          * 
          * @param event
          *                the window detach event.
          */
         public void windowDetached(WindowDetachEvent event);
     }
 
     /**
      * Adds the window attach listener.
      * 
      * @param listener
      *                the window attach listener to add.
      */
     public void addListener(WindowAttachListener listener) {
         if (windowAttachListeners == null) {
             windowAttachListeners = new LinkedList();
         }
         windowAttachListeners.add(listener);
     }
 
     /**
      * Adds the window detach listener.
      * 
      * @param listener
      *                the window detach listener to add.
      */
     public void addListener(WindowDetachListener listener) {
         if (windowDetachListeners == null) {
             windowDetachListeners = new LinkedList();
         }
         windowDetachListeners.add(listener);
     }
 
     /**
      * Removes the window attach listener.
      * 
      * @param listener
      *                the window attach listener to remove.
      */
     public void removeListener(WindowAttachListener listener) {
         if (windowAttachListeners != null) {
             windowAttachListeners.remove(listener);
             if (windowAttachListeners.isEmpty()) {
                 windowAttachListeners = null;
             }
         }
     }
 
     /**
      * Removes the window detach listener.
      * 
      * @param listener
      *                the window detach listener to remove.
      */
     public void removeListener(WindowDetachListener listener) {
         if (windowDetachListeners != null) {
             windowDetachListeners.remove(listener);
             if (windowDetachListeners.isEmpty()) {
                 windowDetachListeners = null;
             }
         }
     }
 
     /**
      * Returns the URL user is redirected to on application close. If the URL is
      * <code>null</code>, the application is closed normally as defined by
      * the application running environment.
      * <p>
      * Desktop application just closes the application window and
      * web-application redirects the browser to application main URL.
      * </p>
      * 
      * @return the URL.
      */
     public String getLogoutURL() {
         return logoutURL;
     }
 
     /**
      * Sets the URL user is redirected to on application close. If the URL is
      * <code>null</code>, the application is closed normally as defined by
      * the application running environment: Desktop application just closes the
      * application window and web-application redirects the browser to
      * application main URL.
      * 
      * @param logoutURL
      *                the logoutURL to set.
      */
     public void setLogoutURL(String logoutURL) {
         this.logoutURL = logoutURL;
     }
 
     /**
      * Experimental API, not finalized. Gets the SystemMessages for this
      * application. SystemMessages are used to notify the user of various
      * critical situations that can occur, such as session expiration,
      * client/server out of sync, and internal server error.
      * 
      * You can customize the messages by overriding this method and returning
      * CustomizedSystemMessages.
      * 
      * @return the SystemMessages for this application
      */
     public static SystemMessages getSystemMessages() {
         return DEFAULT_SYSTEM_MESSAGES;
     }
 
     /**
      * <p>
      * Invoked by the terminal on any exception that occurs in application and
      * is thrown by the <code>setVariable</code> to the terminal. The default
      * implementation sets the exceptions as <code>ComponentErrors</code> to
      * the component that initiated the exception and prints stack trace to
      * standard error stream.
      * </p>
      * <p>
      * You can safely override this method in your application in order to
      * direct the errors to some other destination (for example log).
      * </p>
      * 
      * @param event
      *                the change event.
      * @see com.itmill.toolkit.terminal.Terminal.ErrorListener#terminalError(com.itmill.toolkit.terminal.Terminal.ErrorEvent)
      */
     public void terminalError(Terminal.ErrorEvent event) {
         // throw it to standard error stream too
         event.getThrowable().printStackTrace();
 
         // Finds the original source of the error/exception
         Object owner = null;
         if (event instanceof VariableOwner.ErrorEvent) {
             owner = ((VariableOwner.ErrorEvent) event).getVariableOwner();
         } else if (event instanceof URIHandler.ErrorEvent) {
             owner = ((URIHandler.ErrorEvent) event).getURIHandler();
         } else if (event instanceof ParameterHandler.ErrorEvent) {
             owner = ((ParameterHandler.ErrorEvent) event).getParameterHandler();
         }
 
         // Shows the error in AbstractComponent
         if (owner instanceof AbstractComponent) {
             final Throwable e = event.getThrowable();
             if (e instanceof ErrorMessage) {
                 ((AbstractComponent) owner).setComponentError((ErrorMessage) e);
             } else {
                 ((AbstractComponent) owner)
                         .setComponentError(new SystemError(e));
             }
         }
     }
 
     /**
      * Gets the application context.
      * <p>
      * The application context is the environment where the application is
      * running in.
      * </p>
      * 
      * @return the application context.
      */
     public ApplicationContext getContext() {
         return context;
     }
 
     /**
      * @deprecated Call component's focus method instead.
      * 
      * @param focusable
      */
     public void setFocusedComponent(Focusable focusable) {
         pendingFocus = focusable;
     }
 
     /**
      * Gets and nulls focused component in this window
      * 
      * @deprecated This method will be replaced with focus listener in the
      *             future releases.
      * @return Focused component or null if none is focused.
      */
     public Component.Focusable consumeFocus() {
         final Component.Focusable f = pendingFocus;
         pendingFocus = null;
         return f;
     }
 
     /**
      * Override this method to return correct version number of your
      * Application. Version information is delivered for example to Testing
      * Tools test results.
      * 
      * @return version string
      */
     public String getVersion() {
         return "NONVERSIONED";
     }
 
     /**
      * Experimental API, not finalized. Contains the system messages used to
      * notify the user about various critical situations that can occur.
      * 
      * Customize by overriding the static Application.getSystemMessages() and
      * return CustomizedSystemMessages.
      */
     public static class SystemMessages {
         protected String sessionExpiredURL = null;
         protected String sessionExpiredCaption = "Session Expired";
         protected String sessionExpiredMessage = "Take note of any unsaved data, and <u>click here</u> to continue.";
 
         protected String internalErrorURL = null;
         protected String internalErrorCaption = "Internal Error";
         protected String internalErrorMessage = "Please notify the administrator.<br/>Take note of any unsaved data, and <u>click here</u> to continue.";
 
         protected String outOfSyncURL = null;
         protected String outOfSyncCaption = "Out of sync";
         protected String outOfSyncMessage = "Something has caused us to be out of sync with the server.<br/>Take note of any unsaved data, and <u>click here</u> to re-sync.";
 
         private SystemMessages() {
 
         }
 
         public String getSessionExpiredURL() {
             return sessionExpiredURL;
         }
 
         public String getSessionExpiredCaption() {
             return sessionExpiredCaption;
         }
 
         public String getSessionExpiredMessage() {
             return sessionExpiredMessage;
         }
 
         public String getInternalErrorURL() {
             return internalErrorURL;
         }
 
         public String getInternalErrorCaption() {
             return internalErrorCaption;
         }
 
         public String getInternalErrorMessage() {
             return internalErrorMessage;
         }
 
         public String getOutOfSyncURL() {
             return outOfSyncURL;
         }
 
         public String getOutOfSyncCaption() {
             return outOfSyncCaption;
         }
 
         public String getOutOfSyncMessage() {
             return outOfSyncMessage;
         }
 
     }
 
     /**
      * Experimental API, not finalized. Contains the system messages used to
      * notify the user about various critical situations that can occur.
      * 
      * Customize by overriding the static Application.getSystemMessages() and
      * return CustomizedSystemMessages.
      */
     public static class CustomizedSystemMessages extends SystemMessages {
 
         public void setSessionExpiredURL(String sessionExpiredURL) {
             this.sessionExpiredURL = sessionExpiredURL;
         }
 
         public void setSessionExpiredCaption(String sessionExpiredCaption) {
             this.sessionExpiredCaption = sessionExpiredCaption;
         }
 
         public void setSessionExpiredMessage(String sessionExpiredMessage) {
             this.sessionExpiredMessage = sessionExpiredMessage;
         }
 
         public void setInternalErrorURL(String internalErrorURL) {
             this.internalErrorURL = internalErrorURL;
         }
 
         public void setInternalErrorCaption(String internalErrorCaption) {
             this.internalErrorCaption = internalErrorCaption;
         }
 
         public void setInternalErrorMessage(String internalErrorMessage) {
             this.internalErrorMessage = internalErrorMessage;
         }
 
         public void setOutOfSyncURL(String outOfSyncURL) {
             this.outOfSyncURL = outOfSyncURL;
         }
 
         public void setOutOfSyncCaption(String outOfSyncCaption) {
             this.outOfSyncCaption = outOfSyncCaption;
         }
 
         public void setOutOfSyncMessage(String outOfSyncMessage) {
             this.outOfSyncMessage = outOfSyncMessage;
         }
 
     }
 
 }
