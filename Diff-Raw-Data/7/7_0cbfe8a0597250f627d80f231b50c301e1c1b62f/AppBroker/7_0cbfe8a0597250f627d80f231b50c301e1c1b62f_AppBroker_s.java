 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * AppBroker.java
  *
  * Created on 20. April 2007, 13:16
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package de.cismet.watergis.broker;
 
 import Sirius.navigator.connection.ConnectionSession;
 
 import net.infonode.docking.RootWindow;
 import net.infonode.gui.componentpainter.GradientComponentPainter;
 
 import org.jdom.Element;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 
 import javax.swing.Action;
 
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.RubberBandZoomListener;
 
 import de.cismet.tools.configuration.Configurable;
 import de.cismet.tools.configuration.ConfigurationManager;
 
 import de.cismet.watergis.gui.WatergisApp;
 import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;
 
 import de.cismet.watergis.utils.BookmarkManager;
 
 /**
  * DOCUMENT ME!
  *
  * @author   Puhl
  * @version  $Revision$, $Date$
  */
 public class AppBroker implements Configurable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AppBroker.class);
     // COLORS
     private static final Color blue = new Color(124, 160, 221);
     public static final Color DEFAULT_MODE_COLOR = blue;
     private static ConfigurationManager configManager;
 
     //~ Instance fields --------------------------------------------------------
 
     private RecentlyOpenedFilesList recentlyOpenedFilesList;
 
     private transient ConnectionSession session;
     private MappingComponent mappingComponent;
     private boolean loggedIn;
     private String domain;
     private String callserverUrl;
     private String connectionClass;
     private RootWindow rootWindow;
     private BookmarkManager bookmarkManager;
     private EnumMap<ComponentName, Component> components = new EnumMap<ComponentName, Component>(ComponentName.class);
     private HashMap<String, Action> mapModeSelectionActions = new HashMap<String, Action>();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new AppBroker object.
      */
     private AppBroker() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public ConnectionSession getSession() {
         return session;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  session  DOCUMENT ME!
      */
     public void setSession(final ConnectionSession session) {
         this.session = session;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static AppBroker getInstance() {
         return LazyInitialiser.INSTANCE;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public MappingComponent getMappingComponent() {
         return mappingComponent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  aMappingComponent  DOCUMENT ME!
      */
     public void setMappingComponent(final MappingComponent aMappingComponent) {
         mappingComponent = aMappingComponent;
     }
 
     @Override
     public Element getConfiguration() {
         return null;
     }
 
     @Override
     public void masterConfigure(final Element parent) {
     }
 
     @Override
     public void configure(final Element parent) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isLoggedIn() {
         return loggedIn;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  loggedIn  DOCUMENT ME!
      */
     public void setLoggedIn(final boolean loggedIn) {
         this.loggedIn = loggedIn;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getConnectionClass() {
         return connectionClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  connectionClass  DOCUMENT ME!
      */
     public void setConnectionClass(final String connectionClass) {
         this.connectionClass = connectionClass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getCallserverUrl() {
         return callserverUrl;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  callserverUrl  DOCUMENT ME!
      */
     public void setCallserverUrl(final String callserverUrl) {
         this.callserverUrl = callserverUrl;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDomain() {
         return domain;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  domain  DOCUMENT ME!
      */
     public void setDomain(final String domain) {
         this.domain = domain;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  rootWindow  DOCUMENT ME!
      */
     public void setRootWindow(final RootWindow rootWindow) {
         this.rootWindow = rootWindow;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public RootWindow getRootWindow() {
         return rootWindow;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  color  DOCUMENT ME!
      */
     public void setTitleBarComponentpainter(final Color color) {
         getRootWindow().getRootWindowProperties()
                 .getViewProperties()
                 .getViewTitleBarProperties()
                 .getNormalProperties()
                 .getShapedPanelProperties()
                 .setComponentPainter(new GradientComponentPainter(
                         color,
                         new Color(236, 233, 216),
                         color,
                         new Color(236, 233, 216)));
     }
     /**
      * DOCUMENT ME!
      *
      * @param   name  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Component getComponent(final ComponentName name) {
         return components.get(name);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public WatergisApp getWatergisApp() {
         return (WatergisApp)components.get(ComponentName.MAIN);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  name       DOCUMENT ME!
      * @param  component  DOCUMENT ME!
      */
     public void addComponent(final ComponentName name, final Component component) {
         components.put(name, component);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public RecentlyOpenedFilesList getRecentlyOpenedFilesList() {
         return recentlyOpenedFilesList;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  recentlyOpenedFilesList  DOCUMENT ME!
      */
     public void setRecentlyOpenedFilesList(final RecentlyOpenedFilesList recentlyOpenedFilesList) {
         this.recentlyOpenedFilesList = recentlyOpenedFilesList;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isActionsAlwaysEnabled() {
         return true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  name    DOCUMENT ME!
      * @param  action  DOCUMENT ME!
      */
     public void addMapMode(final String name, final Action action) {
         mapModeSelectionActions.put(name, action);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  mode  DOCUMENT ME!
      */
     public void switchMapMode(final String mode) {
        mapModeSelectionActions.get(mode).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, mode));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  factor  DOCUMENT ME!
      */
     public void simpleZoom(final float factor) {
         final RubberBandZoomListener r = (RubberBandZoomListener)AppBroker.getInstance().getMappingComponent()
                     .getInputListener(MappingComponent.ZOOM);
         r.zoom(factor, mappingComponent.getCamera(), mappingComponent.getAnimationDuration(), 500);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static ConfigurationManager getConfigManager() {
         return configManager;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  configManager  DOCUMENT ME!
      */
     public static void setConfigManager(final ConfigurationManager configManager) {
         AppBroker.configManager = configManager;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BookmarkManager getBookmarkManager() {
         return bookmarkManager;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  bookmarkManager  DOCUMENT ME!
      */
     public void setBookmarkManager(final BookmarkManager bookmarkManager) {
         this.bookmarkManager = bookmarkManager;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class LazyInitialiser {
 
         //~ Static fields/initializers -----------------------------------------
 
         static final AppBroker INSTANCE = new AppBroker();
     }
 }
