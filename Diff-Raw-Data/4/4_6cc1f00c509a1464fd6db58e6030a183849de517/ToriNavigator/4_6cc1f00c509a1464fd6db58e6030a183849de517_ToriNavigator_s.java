 package org.vaadin.tori;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.vaadin.tori.category.CategoryViewImpl;
 import org.vaadin.tori.dashboard.DashboardViewImpl;
 import org.vaadin.tori.mvp.AbstractView;
 import org.vaadin.tori.mvp.NullViewImpl;
 import org.vaadin.tori.mvp.View;
 import org.vaadin.tori.thread.ThreadViewImpl;
 
 import com.vaadin.Application;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UriFragmentUtility;
 import com.vaadin.ui.UriFragmentUtility.FragmentChangedEvent;
 import com.vaadin.ui.UriFragmentUtility.FragmentChangedListener;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 @SuppressWarnings("serial")
 public class ToriNavigator extends CustomComponent {
 
     private static final String URL_PREFIX = "!/";
 
     /**
      * All the views of Tori application that can be navigated to.
      */
     public enum ApplicationView {
         // @formatter:off
         DASHBOARD(URL_PREFIX+"dashboard", DashboardViewImpl.class),
         CATEGORIES(URL_PREFIX+"category", CategoryViewImpl.class),
         THREADS(URL_PREFIX+"thread", ThreadViewImpl.class),
         USERS(URL_PREFIX+"user", NullViewImpl.class)
         ;
         // @formatter:on
 
         private String url;
         private Class<? extends AbstractView<?, ?>> viewClass;
 
         private ApplicationView(final String url,
                 final Class<? extends AbstractView<?, ?>> viewClass) {
             this.url = url;
             this.viewClass = viewClass;
         }
 
         public String getUrl() {
             return url;
         }
 
         private static ApplicationView getDefault() {
             return DASHBOARD;
         }
     }
 
     private final HashMap<String, Class<? extends AbstractView<?, ?>>> uriToClass = new HashMap<String, Class<? extends AbstractView<?, ?>>>();
     private final HashMap<Class<? extends AbstractView<?, ?>>, String> classToUri = new HashMap<Class<? extends AbstractView<?, ?>>, String>();
     private final HashMap<Class<? extends AbstractView<?, ?>>, AbstractView<?, ?>> classToView = new HashMap<Class<? extends AbstractView<?, ?>>, AbstractView<?, ?>>();
     private String mainViewUri = null;
     private final VerticalLayout layout = new VerticalLayout();
     private final UriFragmentUtility uriFragmentUtil = new UriFragmentUtility();
     private String currentFragment = "";
     private View currentView = null;
     private final LinkedList<ViewChangeListener> listeners = new LinkedList<ViewChangeListener>();
     private boolean viewCacheEnabled = false;
 
     public ToriNavigator() {
         layout.setSizeFull();
         setSizeFull();
         layout.addComponent(uriFragmentUtil);
         setCompositionRoot(layout);
         uriFragmentUtil.addListener(new FragmentChangedListener() {
             @Override
             public void fragmentChanged(final FragmentChangedEvent source) {
                 ToriNavigator.this.fragmentChanged();
             }
         });
 
         // Register all views of the application
         for (final ApplicationView appView : ApplicationView.values()) {
             addView(appView.getUrl(), appView.viewClass);
         }
 
         setMainView(ApplicationView.getDefault().getUrl());
     }
 
     private void fragmentChanged() {
         String newFragment = uriFragmentUtil.getFragment();
         if ("".equals(newFragment)) {
             newFragment = mainViewUri;
         }
 
         final String[] dataFragment = getDataFromFragment(newFragment);
         final String uri = dataFragment[0];
         final String[] arguments = tail(dataFragment);
         if (uriToClass.containsKey(uri)) {
             final AbstractView<?, ?> newView = getOrCreateView(uri);
 
             final String warn = currentView == null ? null : currentView
                     .getWarningForNavigatingFrom();
             if (warn != null && warn.length() > 0) {
                 confirmedMoveToNewView(arguments, newView, warn);
             } else {
                 moveTo(newView, arguments, false);
             }
 
         } else {
             uriFragmentUtil.setFragment(currentFragment, false);
         }
     }
 
     /** Remove the first object in an array */
     private static String[] tail(final String[] array) {
         ToriUtil.checkForNull(array, "array must not be null");
 
         final List<String> list = new ArrayList<String>();
         for (int i = 1; i < array.length; i++) {
             list.add(array[i]);
         }
 
         return list.toArray(new String[list.size()]);
     }
 
     /**
      * parses the current fragment into the view address and its optional
      * arguments. ^_^
      */
     private static String[] getDataFromFragment(final String fragment) {
         final String trimmedFragment = fragment.substring(URL_PREFIX.length());
         final String[] data = trimmedFragment.split("/");
         data[0] = URL_PREFIX + data[0];
         return data;
     }
 
     private void confirmedMoveToNewView(final String[] arguments,
             final AbstractView<?, ?> newView, final String warn) {
         final VerticalLayout lo = new VerticalLayout();
         lo.setMargin(true);
         lo.setSpacing(true);
         lo.setWidth("400px");
         final Window wDialog = new Window("Warning", lo);
         wDialog.setModal(true);
         final Window main = getWindow();
         main.addWindow(wDialog);
         lo.addComponent(new Label(warn));
         lo.addComponent(new Label(
                 "If you do not want to navigate away from the current screen, press Cancel."));
         final Button cancel = new Button("Cancel", new Button.ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 uriFragmentUtil.setFragment(currentFragment, false);
                 main.removeWindow(wDialog);
             }
         });
         final Button cont = new Button("Continue", new Button.ClickListener() {
 
             @Override
             public void buttonClick(final ClickEvent event) {
                 main.removeWindow(wDialog);
                 moveTo(newView, arguments, false);
             }
 
         });
         final HorizontalLayout h = new HorizontalLayout();
         h.addComponent(cancel);
         h.addComponent(cont);
         h.setSpacing(true);
         lo.addComponent(h);
         lo.setComponentAlignment(h, Alignment.MIDDLE_RIGHT);
     }
 
     private AbstractView<?, ?> getOrCreateView(final String uri) {
         final Class<? extends AbstractView<?, ?>> newViewClass = uriToClass
                 .get(uri);
         if (!viewCacheEnabled || !classToView.containsKey(newViewClass)) {
             final AbstractView<?, ?> view = createView(newViewClass);
             if (viewCacheEnabled) {
                 classToView.put(newViewClass, view);
             } else {
                 return view;
             }
         }
         final AbstractView<?, ?> v = classToView.get(newViewClass);
         return v;
     }
 
     private <T extends AbstractView<?, ?>> T createView(final Class<T> viewClass) {
         try {
             final T view = viewClass.newInstance();
             view.init(this, getApplication());
             return view;
         } catch (final InstantiationException e) {
             throw new RuntimeException(e);
         } catch (final IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
     private void moveTo(final AbstractView<?, ?> v, final String[] arguments,
             final boolean noFragmentSetting) {
         currentFragment = classToUri.get(v.getClass());
         if (arguments != null) {
            currentFragment += "/" + arguments;
         }
         if (!noFragmentSetting
                 && !currentFragment.equals(uriFragmentUtil.getFragment())) {
             uriFragmentUtil.setFragment(currentFragment, false);
         }
         Component removeMe = null;
         for (final Iterator<Component> i = layout.getComponentIterator(); i
                 .hasNext();) {
             final Component c = i.next();
             if (c != uriFragmentUtil) {
                 removeMe = c;
             }
         }
         if (removeMe != null) {
             layout.removeComponent(removeMe);
         }
         layout.addComponent(v);
         layout.setExpandRatio(v, 1.0F);
         v.navigateTo(arguments);
         final View previousView = currentView;
         currentView = v;
 
         for (final ViewChangeListener l : listeners) {
             l.navigatorViewChange(previousView, currentView);
         }
     }
 
     /**
      * Get the main view.
      * 
      * Main view is the default view shown to user when he opens application
      * without specifying view uri.
      * 
      * @return Uri of the main view.
      */
     public String getMainView() {
         return mainViewUri;
     }
 
     /**
      * Set the main view.
      * 
      * Main view is the default view shown to user when he opens application
      * without specifying view uri. If main view has not been set, the first
      * view registered with addView() is used as main view. Note that the view
      * must be registered with addView() before calling this method.
      * 
      * @param mainViewUri
      *            Uri of the main view.
      */
     public void setMainView(final String mainViewUri) {
         if (uriToClass.containsKey(mainViewUri)) {
             this.mainViewUri = mainViewUri;
             if (currentView == null) {
                 moveTo(getOrCreateView(mainViewUri), null, true);
             }
         } else {
             throw new IllegalArgumentException(
                     "No view with given uri can be found in the navigator");
         }
     }
 
     /**
      * Add a new view to navigator.
      * 
      * Register a view to navigator.
      * 
      * @param uri
      *            String that identifies a view. This is the string that is
      *            shown in URL after #
      * @param viewClass
      *            Component class that implements Navigator.View interface
      */
     public void addView(final String uri,
             final Class<? extends AbstractView<?, ?>> viewClass) {
 
         // Check parameters
         if (!View.class.isAssignableFrom(viewClass)) {
             throw new IllegalArgumentException(
                     "viewClass must implemenent Navigator.View");
         }
 
         if (uri == null || viewClass == null || uri.length() == 0) {
             throw new IllegalArgumentException(
                     "viewClass and uri must be non-null and not empty");
         }
 
         if (uriToClass.containsKey(uri)) {
             if (uriToClass.get(uri) == viewClass) {
                 return;
             }
 
             throw new IllegalArgumentException(uriToClass.get(uri).getName()
                     + " is already mapped to '" + uri + "'");
         }
 
         if (classToUri.containsKey(viewClass)) {
             throw new IllegalArgumentException(
                     "Each view class can only be added to Navigator with one uri");
         }
 
         if (uri.indexOf('#') >= 0) {
             throw new IllegalArgumentException(
                     "Uri can not contain # characters");
         }
 
         uriToClass.put(uri, viewClass);
         classToUri.put(viewClass, uri);
 
         if (getMainView() == null) {
             setMainView(uri);
         }
     }
 
     /**
      * Remove view from navigator.
      * 
      * @param uri
      *            Uri of the view to remove.
      */
     public void removeView(final String uri) {
         final Class<? extends View> c = uriToClass.get(uri);
         if (c != null) {
             uriToClass.remove(uri);
             classToUri.remove(c);
             if (getMainView() == null || getMainView().equals(getMainView())) {
                 if (uriToClass.size() == 0) {
                     mainViewUri = null;
                 } else {
                     setMainView(uriToClass.keySet().iterator().next());
                 }
             }
         }
     }
 
     /**
      * Get the uri for given view implementation class.
      * 
      * @param viewClass
      *            Class that implements the view.
      * @return Uri registered for the view class.
      */
     public String getUri(final Class<? extends View> viewClass) {
         return classToUri.get(viewClass);
     }
 
     /**
      * Get the view class for given uri.
      * 
      * @param uri
      *            Uri to get view for
      * @return View that corresponds to the uri
      */
     public Class<? extends View> getViewClass(final String uri) {
         return uriToClass.get(uri);
     }
 
     /**
      * Switch to view identified with uri.
      * 
      * Uri can be either the exact uri registered previously with addView() or
      * it can also contain data id passed to the view. In case data id is
      * included, the format is 'uri/freeFormedArgumentsString'.
      * 
      * @param uri
      *            Uri where to navigate.
      */
     public void navigateTo(final String uri) {
         uriFragmentUtil.setFragment(uri);
     }
 
     /**
      * Switch to view implemented by given class.
      * 
      * Note that the view must be registered to navigator with addView() before
      * calling this method.
      * 
      * @param viewClass
      *            Class that implements the view.
      */
     public void navigateTo(final Class<? extends View> viewClass) {
         final String uri = getUri(viewClass);
         if (uri != null) {
             navigateTo(uri);
         }
     }
 
     public void navigateTo(final ApplicationView view) {
         this.navigateTo(view, null);
     }
 
     public void navigateTo(final ApplicationView view, final String arguments) {
         final String parsedArguments;
         if (arguments == null) {
             parsedArguments = "";
         } else {
             parsedArguments = "/" + arguments;
         }
         this.navigateTo(view.getUrl() + parsedArguments);
     }
 
     /**
      * Listen to the view changes.
      * 
      * The listener will get notified after the view has changed.
      * 
      * @param listener
      *            Listener to invoke after view changes.
      */
     public void addListener(final ViewChangeListener listener) {
         listeners.add(listener);
     }
 
     /**
      * Remove the view change listener.
      * 
      * @param listener
      *            Listener to remove.
      */
     public void removeListener(final ViewChangeListener listener) {
         listeners.remove(listener);
     }
 
     /**
      * Interface for listening to View changes.
      */
     public interface ViewChangeListener {
 
         /**
          * Invoked after the view has changed. Be careful for deadlocks if you
          * decide to change the view again in the listener.
          * 
          * @param previous
          *            Preview view before the change.
          * @param current
          *            New view after the change.
          */
         public void navigatorViewChange(View previous, View current);
 
     }
 
     /**
      * Interface implemented by all applications that uses Navigator.
      * 
      */
     public interface NavigableApplication {
 
         /**
          * Create a new browser window.
          * 
          * This method must construc a new window that could be used as a main
          * window for the application. Each call to this method must create a
          * new instance and your application should work when there are multiple
          * instances of concurrently. Each window can contain anything you like,
          * but at least they should contain a new Navigator instance for
          * controlling navigation within the window. Typically one also adds
          * somekind of menu for commanding navigator.
          * 
          * @return New window.
          */
         public Window createNewWindow();
     }
 
     /**
      * Helper for overriding Application.getWindow(String).
      * 
      * <p>
      * This helper makes implementing support for multiple browser tabs or
      * browser windows easy. Just override Application.getWindow(String) in your
      * application like this:
      * </p>
      * 
      * <pre>
      * &#064;Override
      * public Window getWindow(String name) {
      *     return Navigator.getWindow(this, name, super.getWindow(name));
      * }
      * </pre>
      * 
      * @param application
      *            Application instance, which implements
      *            Navigator.NavigableApplication interface.
      * @param name
      *            Name parameter from Application.getWindow(String name)
      * @param superGetWindow
      *            The window returned by super.getWindow(name)
      * @return
      * @throws IllegalArgumentException
      *             if <code>application</code> is not an instance of
      *             {@link Application}
      */
     public static Window getWindow(final NavigableApplication application,
             final String name, final Window superGetWindow) {
         if (superGetWindow != null) {
             return superGetWindow;
         }
 
         final Window w = application.createNewWindow();
         w.setName(name);
 
         if (application instanceof Application) {
             ((Application) application).addWindow(w);
         } else {
             throw new IllegalArgumentException(
                     "application must also be an instance of "
                             + Application.class.getName());
         }
         return w;
     }
 
     /**
      * In a URI fragment of <code>#foo/bar</code>, this will return
      * <code>foo</code>. If no fragment is set, this returns <code>null</code>
      */
     public String getCurrentUri() {
         final String fragment = uriFragmentUtil.getFragment();
         if (fragment != null) {
             return getDataFromFragment(fragment)[0];
         } else {
             return null;
         }
     }
 
     /**
      * In a URI fragment of <code>#foo/bar</code>, this will return
      * <code>bar</code>. If no fragment is set, this returns <code>null</code>
      */
     public String[] getCurrentArguments() {
         final String fragment = uriFragmentUtil.getFragment();
         if (fragment != null) {
             return tail(getDataFromFragment(fragment));
         } else {
             return null;
         }
     }
 
     public View getCurrentView() {
         return currentView;
     }
 
     /**
      * Returns {@code true} if the created {@link View} instances are cached and
      * reused or {@code false} if a new instance is always created when needed.
      * 
      * @return {@code true} if the {@link View} instances are cached,
      *         {@code false} if a new instance is always created.
      */
     public boolean isViewCacheEnabled() {
         return viewCacheEnabled;
     }
 
     /**
      * Sets whether this ToriNavigator should cache the created {@link View}
      * instances or always instantiate a new one. By default the caching is
      * disabled.
      * 
      * @param enabled
      *            should the cache be enabled or not.
      */
     public void setViewCacheEnabled(final boolean enabled) {
         this.viewCacheEnabled = enabled;
     }
 
     /**
      * Creates a new instance of the current {@link View} and replaces the old
      * one with the new one in the UI.
      */
     public void recreateCurrentView() {
         @SuppressWarnings("unchecked")
         final Class<? extends AbstractView<?, ?>> viewClass = (Class<? extends AbstractView<?, ?>>) ((AbstractView<?, ?>) getCurrentView())
                 .getClass();
         final AbstractView<?, ?> view = createView(viewClass);
         moveTo(view, getCurrentArguments(), true);
     }
 }
