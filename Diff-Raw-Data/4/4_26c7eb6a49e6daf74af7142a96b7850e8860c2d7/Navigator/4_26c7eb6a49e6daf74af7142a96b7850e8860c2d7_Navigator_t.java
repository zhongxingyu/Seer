 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator;
 
 import Sirius.navigator.connection.Connection;
 import Sirius.navigator.connection.ConnectionFactory;
 import Sirius.navigator.connection.ConnectionSession;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.connection.proxy.ConnectionProxy;
 import Sirius.navigator.event.CatalogueActivationListener;
 import Sirius.navigator.event.CatalogueSelectionListener;
 import Sirius.navigator.exception.ConnectionException;
 import Sirius.navigator.exception.ExceptionManager;
 import Sirius.navigator.method.MethodManager;
 import Sirius.navigator.plugin.PluginRegistry;
 import Sirius.navigator.resource.PropertyManager;
 import Sirius.navigator.resource.ResourceManager;
 import Sirius.navigator.search.CidsSearchInitializer;
 import Sirius.navigator.search.dynamic.FormDataBean;
 import Sirius.navigator.search.dynamic.SearchDialog;
 import Sirius.navigator.types.treenode.RootTreeNode;
 import Sirius.navigator.ui.*;
 import Sirius.navigator.ui.attributes.AttributeViewer;
 import Sirius.navigator.ui.attributes.editor.AttributeEditor;
 import Sirius.navigator.ui.dialog.LoginDialog;
 import Sirius.navigator.ui.dnd.MetaTreeNodeDnDHandler;
 import Sirius.navigator.ui.progress.ProgressObserver;
 import Sirius.navigator.ui.status.MutableStatusBar;
 import Sirius.navigator.ui.status.StatusChangeListener;
 import Sirius.navigator.ui.tree.MetaCatalogueTree;
 import Sirius.navigator.ui.tree.SearchResultsTree;
 import Sirius.navigator.ui.tree.SearchResultsTreePanel;
 import Sirius.navigator.ui.widget.FloatingFrameConfigurator;
 
 import Sirius.server.middleware.types.*;
 import Sirius.server.newuser.UserException;
 import Sirius.server.newuser.permission.*;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.HttpConnection;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.jetty.handler.HandlerCollection;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 
 import org.openide.util.Lookup;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.*;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import java.net.URL;
 
 import java.util.*;
 import java.util.prefs.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import javax.swing.*;
 
 import de.cismet.cids.editors.NavigatorAttributeEditorGui;
 
 import de.cismet.cids.navigator.utils.CidsClientToolbarItem;
 
 import de.cismet.lookupoptions.gui.OptionsClient;
 
 import de.cismet.lookupoptions.options.ProxyOptionsPanel;
 
 import de.cismet.netutil.Proxy;
 
 import de.cismet.remote.RESTRemoteControlStarter;
 
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.JnlpTools;
 import de.cismet.tools.StaticDebuggingTools;
 
 import de.cismet.tools.configuration.ConfigurationManager;
 import de.cismet.tools.configuration.ShutdownHook;
 import de.cismet.tools.configuration.StartupHook;
 import de.cismet.tools.configuration.TakeoffHook;
 
 import de.cismet.tools.gui.CheckThreadViolationRepaintManager;
 import de.cismet.tools.gui.DefaultPopupMenuListener;
 import de.cismet.tools.gui.EventDispatchThreadHangMonitor;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 
 import static Sirius.navigator.Navigator.NAVIGATOR_HOME;
 import static Sirius.navigator.Navigator.NAVIGATOR_HOME_DIR;
 
 import static java.awt.Frame.MAXIMIZED_BOTH;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class Navigator extends JFrame {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final ResourceManager resourceManager = ResourceManager.getManager();
     public static final String NAVIGATOR_HOME_DIR = ".navigator"
                 + (((System.getProperty("directory.extension")) != null) ? (System.getProperty("directory.extension"))
                                                                          : "");
     public static final String NAVIGATOR_HOME = System.getProperty("user.home") + System.getProperty("file.separator")
                 + NAVIGATOR_HOME_DIR + System.getProperty("file.separator");
     private static volatile boolean startupFinished = false;
 
     //~ Instance fields --------------------------------------------------------
 
     private final Logger logger; // = Logger.getLogger(Navigator.class);
     private final PropertyManager propertyManager;
     private final ConfigurationManager configurationManager = new ConfigurationManager();
     private final ExceptionManager exceptionManager;
     private final ProgressObserver progressObserver;
     private LoginDialog loginDialog;
     private LayoutedContainer container;
     private MutableMenuBar menuBar;
     private MutableToolBar toolBar;
     private MutableStatusBar statusBar;
     private MutablePopupMenu popupMenu;
     private MetaCatalogueTree metaCatalogueTree;
     private SearchResultsTree searchResultsTree;
     private AttributeViewer attributeViewer;
     private AttributeEditor attributeEditor;
     private SearchDialog searchDialog;
     private Preferences preferences;
     /** Holds value of property disposed. */
     private boolean disposed = false;    // InfoNode
     // Panels
     private SearchResultsTreePanel searchResultsTreePanel;
     private DescriptionPane descriptionPane;
     private NavigatorSplashScreen splashScreen;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new Navigator object.
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public Navigator() throws Exception {
         this(new ProgressObserver());
     }
 
     /**
      * Creates a new instance of Navigator.
      *
      * @param   progressObserver  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public Navigator(final ProgressObserver progressObserver) throws Exception {
         this(progressObserver, null);
     }
 
     /**
      * Creates a new instance of Navigator.
      *
      * @param   progressObserver  DOCUMENT ME!
      * @param   splashScreen      DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public Navigator(final ProgressObserver progressObserver, final NavigatorSplashScreen splashScreen)
             throws Exception {
         this.logger = Logger.getLogger(this.getClass());
 
         this.progressObserver = progressObserver;
         this.splashScreen = splashScreen;
 
         this.propertyManager = PropertyManager.getManager();
 
         this.preferences = Preferences.userNodeForPackage(this.getClass());
 
         this.exceptionManager = ExceptionManager.getManager();
        StaticSwingTools.tweakUI();
         this.init();
 
         startupFinished = true;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void init() throws Exception {
         if (StaticDebuggingTools.checkHomeForFile("cismetDebuggingInitEventDispatchThreadHangMonitor")) { // NOI18N
             EventDispatchThreadHangMonitor.initMonitoring();
         }
         if (StaticDebuggingTools.checkHomeForFile("cismetBeansbindingDebuggingOn")) {                     // NOI18N
             System.setProperty("cismet.beansdebugging", "true");                                          // NOI18N
         }
         if (StaticDebuggingTools.checkHomeForFile("cismetCheckForEDThreadVialoation")) {                  // NOI18N
             RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
         }
 
         initTakeoffHooks();
 
         final ProxyOptionsPanel proxyOptions = new ProxyOptionsPanel();
         proxyOptions.setProxy(Proxy.fromPreferences());
 
         final String heavyComps = System.getProperty("contains.heavyweight.comps"); // NOI18N
         if ((heavyComps != null) && heavyComps.equals("true")) {                    // NOI18N
             JPopupMenu.setDefaultLightWeightPopupEnabled(false);
             ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
         }
 
         // splashscreen gesetzt?
         if (splashScreen != null) {
             // ProxyOptions panel soll im SplashScreen integriert werden
             // panel übergeben
             splashScreen.setProxyOptionsPanel(proxyOptions);
             // panel noch nicht anzeigen
             splashScreen.setProxyOptionsVisible(false);
 
             // auf Anwenden-Button horchen
             splashScreen.addApplyButtonActionListener(new ActionListener() {
 
                     // Anwenden wurde gedrückt
                     @Override
                     public void actionPerformed(final ActionEvent ae) {
                         // Proxy in den Preferences setzen
                         proxyOptions.getProxy().toPreferences();
                         // Panel wieder verstecken
                         splashScreen.setProxyOptionsVisible(false);
                     }
                 });
         }
 
         initConnection(Proxy.fromPreferences());
 
         try {
             checkNavigatorHome();
             initConfigurationManager();
             initUI();
             initWidgets();
             initDialogs();
             initPlugins();
             initToolbarExtensions();
             initEvents();
             initWindow();
             initSearch();
 
             configurationManager.addConfigurable(OptionsClient.getInstance());
             configurationManager.configure();
 
             SwingUtilities.invokeLater(new Runnable() {
 
                     // UGLY WINNING
                     @Override
                     public void run() {
                         container.loadLayout(
                             LayoutedContainer.DEFAULT_LAYOUT,
                             true,
                             Navigator.this);
                     }
                 });
             if (!StaticDebuggingTools.checkHomeForFile("cismetTurnOffInternalWebserver")) { // NOI18N
                 initHttpServer();
             }
 
             initStartupHooks();
         } catch (final InterruptedException iexp) {
             logger.error("navigator start interrupted: " + iexp.getMessage() + "\n disconnecting from server"); // NOI18N
             SessionManager.getSession().logout();
             SessionManager.getConnection().disconnect();
             this.progressObserver.reset();
         }
 
         // From Hell
         final KeyStroke configLoggerKeyStroke = KeyStroke.getKeyStroke(
                 'L',
                 InputEvent.CTRL_DOWN_MASK
                         + InputEvent.SHIFT_DOWN_MASK);
         final Action configAction = new AbstractAction() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     java.awt.EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 Log4JQuickConfig.getSingletonInstance().setVisible(true);
                             }
                         });
                 }
             };
         getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(configLoggerKeyStroke, "CONFIGLOGGING"); // NOI18N
         getRootPane().getActionMap().put("CONFIGLOGGING", configAction);                                          // NOI18N
     }
 
     /**
      * Initialises the configuration manager.
      */
     private void initConfigurationManager() {
         // TODO: Put in method which modifies progress
         String cismapconfig = null;
         String fallBackConfig = null;
 
         // Default
         if (cismapconfig == null) {
             cismapconfig = "defaultNavigatorProperties.xml"; // NOI18N
         }
 
         if (fallBackConfig == null) {
             fallBackConfig = "defaultNavigatorProperties.xml"; // NOI18N
         }
 
         configurationManager.setDefaultFileName(cismapconfig);
         configurationManager.setFallBackFileName(fallBackConfig);
 
         configurationManager.setFileName("configuration.xml");
         configurationManager.setClassPathFolder("/");
         configurationManager.setFolder(NAVIGATOR_HOME_DIR);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void checkNavigatorHome() {
         try {
             final File file = new File(NAVIGATOR_HOME);
             if (file.exists()) {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Navigator Directory exists.");                     // NOI18N
                 }
             } else {
                 if (logger.isDebugEnabled()) {
                     logger.debug("Navigator Directory does not exist --> creating"); // NOI18N
                 }
                 file.mkdir();
                 if (logger.isDebugEnabled()) {
                     logger.debug("Navigator Directory successfully created");        // NOI18N
                 }
             }
         } catch (Exception ex) {
             logger.error("Error while checking/creating Navigator home directory", ex); // NOI18N
         }
     }
 
     /**
      * #########################################################################
      *
      * @param   proxyConfig  DOCUMENT ME!
      *
      * @throws  ConnectionException   DOCUMENT ME!
      * @throws  InterruptedException  DOCUMENT ME!
      */
     private void initConnection(final Proxy proxyConfig) throws ConnectionException, InterruptedException {
         progressObserver.setProgress(
             25,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_25")); // NOI18N
         if (logger.isDebugEnabled()) {
             logger.debug("initialising connection using proxy: " + proxyConfig);
         }
         final Connection connection = ConnectionFactory.getFactory()
                     .createConnection(propertyManager.getConnectionClass(),
                         propertyManager.getConnectionInfo().getCallserverURL(),
                         proxyConfig);
         ConnectionSession session = null;
         ConnectionProxy proxy = null;
 
         progressObserver.setProgress(
             50,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_50")); // NOI18N
         // autologin
         if (propertyManager.isAutoLogin()) {
             if (logger.isInfoEnabled()) {
                 logger.info("performing autologin of user '" + propertyManager.getConnectionInfo().getUsername() + "'"); // NOI18N
             }
             try {
                 session = ConnectionFactory.getFactory()
                             .createSession(connection, propertyManager.getConnectionInfo(), true);
                 proxy = ConnectionFactory.getFactory().createProxy(propertyManager.getConnectionProxyClass(), session);
                 SessionManager.init(proxy);
             } catch (UserException uexp) {
                 logger.error("autologin failed", uexp);                                                                  // NOI18N
                 session = null;
             }
         }
 
         // autologin = false || autologin failed
         if (!propertyManager.isAutoLogin() || (session == null)) {
             if (logger.isInfoEnabled()) {
                 logger.info("performing login"); // NOI18N
             }
             try {
                 session = ConnectionFactory.getFactory()
                             .createSession(connection, propertyManager.getConnectionInfo(), false);
             } catch (UserException uexp) {
             }                                    // should never happen
             proxy = ConnectionFactory.getFactory().createProxy(propertyManager.getConnectionProxyClass(), session);
             SessionManager.init(proxy);
 
             loginDialog = new LoginDialog(this);
             StaticSwingTools.showDialog(loginDialog);
         }
 
         PropertyManager.getManager()
                 .setEditable(this.hasPermission(
                         SessionManager.getProxy().getClasses(),
                         PermissionHolder.WRITEPERMISSION));
         // PropertyManager.getManager().setEditable(true);
         if (logger.isInfoEnabled()) {
             logger.info("initConnection(): navigator editor enabled: " + PropertyManager.getManager().isEditable()); // NOI18N
         }
     }
     // #########################################################################
 
     /**
      * DOCUMENT ME!
      *
      * @throws  InterruptedException  DOCUMENT ME!
      */
     private void initUI() throws InterruptedException {
         progressObserver.setProgress(
             100,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_100")); // NOI18N
 
         menuBar = new MutableMenuBar();
         toolBar = new MutableToolBar(propertyManager.isAdvancedLayout());
         container = new LayoutedContainer(toolBar, menuBar, propertyManager.isAdvancedLayout());
         menuBar.registerLayoutManager(container);
         statusBar = new MutableStatusBar();
         popupMenu = new MutablePopupMenu();
 
         progressObserver.setProgress(
             150,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_150")); // NOI18N
         this.setContentPane(new JPanel(new BorderLayout(), true));
         this.setJMenuBar(menuBar);
 
         final JPanel panel = new JPanel(new BorderLayout());
         panel.add(toolBar, BorderLayout.NORTH);
         panel.add(container.getContainer(), BorderLayout.CENTER);
 
         this.getContentPane().add(panel, BorderLayout.CENTER);
         this.getContentPane().add(statusBar, BorderLayout.SOUTH);
     }
     // #########################################################################
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void initToolbarExtensions() throws Exception {
         final Collection<? extends CidsClientToolbarItem> customToolbarItems = Lookup.getDefault()
                     .lookupAll(CidsClientToolbarItem.class);
 
         if (logger.isDebugEnabled()) {
             logger.debug("Initializing " + customToolbarItems.size() + " toolbar extensions.");
         }
 
         final Comparator<CidsClientToolbarItem> comp = new Comparator<CidsClientToolbarItem>() {
 
                 @Override
                 public int compare(final CidsClientToolbarItem t, final CidsClientToolbarItem t1) {
                     try {
                         return t.getSorterString().compareTo(t1.getSorterString());
                     } catch (Exception e) {
                         logger.warn(
                             "Error during comparing ToolbarExtensions. (You should not return null in getSorterString()) --> returned 0",
                             e);
                         return 0;
                     }
                 }
             };
 
         final ArrayList<CidsClientToolbarItem> sorted = new ArrayList<CidsClientToolbarItem>(customToolbarItems);
 
         Collections.sort(sorted, comp);
 
         for (final CidsClientToolbarItem ccti : sorted) {
             if (logger.isDebugEnabled()) {
                 logger.debug("Adding CidsClientToolbarItem: " + ccti.getValue(Action.NAME) + " - class: '"
                             + ccti.getClass().toString() + "'? " + ccti.isVisible());
             }
             if (ccti.isVisible()) {
                 toolBar.getDefaultToolBar().add(ccti);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void initWidgets() throws Exception {
         // MetaCatalogueTree ---------------------------------------------------
         progressObserver.setProgress(
             200,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_200")); // NOI18N
         final RootTreeNode rootTreeNode = new RootTreeNode(SessionManager.getProxy().getRoots());
         metaCatalogueTree = new MetaCatalogueTree(
                 rootTreeNode,
                 PropertyManager.getManager().isEditable(),
                 true,
                 propertyManager.getMaxConnections());
         // dnd
         final MetaTreeNodeDnDHandler dndHandler = new MetaTreeNodeDnDHandler(metaCatalogueTree);
 
         final MutableConstraints catalogueTreeConstraints = new MutableConstraints(propertyManager.isAdvancedLayout());
         catalogueTreeConstraints.addAsScrollPane(
             ComponentRegistry.CATALOGUE_TREE,
             metaCatalogueTree,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.metaCatalogueTree.name"),    // NOI18N
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.metaCatalogueTree.tooltip"), // NOI18N
             resourceManager.getIcon("catalogue_tree_icon.gif"),                                           // NOI18N
             MutableConstraints.P1,
             MutableConstraints.ANY_INDEX,
             true);
         container.add(catalogueTreeConstraints);
 
         // SearchResultsTree ---------------------------------------------------
         progressObserver.setProgress(
             225,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_225")); // NOI18N
         searchResultsTree = new SearchResultsTree();
         searchResultsTreePanel = new SearchResultsTreePanel(searchResultsTree, propertyManager.isAdvancedLayout());
         // dnd
         new MetaTreeNodeDnDHandler(searchResultsTree);
 
         final MutableConstraints searchResultsTreeConstraints = new MutableConstraints(
                 propertyManager.isAdvancedLayout());
         searchResultsTreeConstraints.addAsComponent(
             ComponentRegistry.SEARCHRESULTS_TREE,
             searchResultsTreePanel,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.searchResultsTreePanel.name"),    // NOI18N
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.searchResultsTreePanel.tooltip"), // NOI18N
             resourceManager.getIcon("searchresults_tree_icon.gif"),                                            // NOI18N
             MutableConstraints.P1,
             MutableConstraints.ANY_INDEX);
         container.add(searchResultsTreeConstraints);
 
         // AttributePanel ------------------------------------------------------
         progressObserver.setProgress(
             250,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_250")); // NOI18N
 
         attributeViewer = new AttributeViewer();
         FloatingFrameConfigurator configurator = new FloatingFrameConfigurator(
                 ComponentRegistry.ATTRIBUTE_VIEWER,
                 org.openide.util.NbBundle.getMessage(
                     Navigator.class,
                     "Navigator.initWidgets().configurator.name.attributeViewer")); // NOI18N
         configurator.setTitleBarEnabled(false);
 
         final MutableConstraints attributePanelConstraints = new MutableConstraints(propertyManager.isAdvancedLayout());
         attributePanelConstraints.addAsFloatingFrame(
             ComponentRegistry.ATTRIBUTE_VIEWER,
             attributeViewer,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeviewer.name"),    // NOI18N
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeviewer.tooltip"), // NOI18N
             resourceManager.getIcon("attributetable_icon.gif"),
             MutableConstraints.P2,
             0,
             false,
             configurator,
             false);
         container.add(attributePanelConstraints);
 
         // AttributeEditor .....................................................
         if (PropertyManager.getManager().isEditable()) {
             progressObserver.setProgress(
                 275,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_275")); // NOI18N
             // HELL
 // if (StaticDebuggingTools.checkHomeForFile("cidsExperimentalBeanEditorsEnabled")) {
             attributeEditor = new NavigatorAttributeEditorGui();
 //            } else {
 //                attributeEditor = new AttributeEditor();
 //            }
             configurator = new FloatingFrameConfigurator(
                     ComponentRegistry.ATTRIBUTE_EDITOR,
                     org.openide.util.NbBundle.getMessage(
                         Navigator.class,
                         "Navigator.initWidgets().configurator.name.attributeEditor")); // NOI18N
             configurator.setTitleBarEnabled(false);
 
             final MutableConstraints attributeEditorConstraints = new MutableConstraints(true);
             attributeEditorConstraints.addAsFloatingFrame(
                 ComponentRegistry.ATTRIBUTE_EDITOR,
                 attributeEditor,
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeeditor.name"),    // NOI18N
                 org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.attributeeditor.tooltip"), // NOI18N
                 resourceManager.getIcon("attributetable_icon.gif"),
                 MutableConstraints.P3,
                 1,
                 false,
                 configurator,
                 false);
             container.add(attributeEditorConstraints);
 
             // verschieben nach position 1 oder zwei beim Dr�cken von
             // SHIFT + F2 / F3
             final InputMap inputMap = attributeEditor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
             final ActionMap actionMap = attributeEditor.getActionMap();
 
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK, true), MutableConstraints.P2);
             inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK, true), MutableConstraints.P3);
 
             actionMap.put(MutableConstraints.P2, new AbstractAction() {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         attributeEditorConstraints.setPosition(MutableConstraints.P2);
                     }
                 });
 
             actionMap.put(MutableConstraints.P3, new AbstractAction() {
 
                     @Override
                     public void actionPerformed(final ActionEvent e) {
                         attributeEditorConstraints.setPosition(MutableConstraints.P3);
                     }
                 });
 
             if (logger.isInfoEnabled()) {
                 logger.info("attribute editor enabled");  // NOI18N
             }
         } else {
             if (logger.isInfoEnabled()) {
                 logger.info("attribute editor disabled"); // NOI18N
             }
         }
 
         // DescriptionPane -----------------------------------------------------
         progressObserver.setProgress(
             325,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_325")); // NOI18N
 
         if (PropertyManager.getManager().getDescriptionPaneHtmlRenderer().equals(
                         PropertyManager.FLYING_SAUCER_HTML_RENDERER)) {
             descriptionPane = new DescriptionPaneFS();
         } else if (PropertyManager.getManager().getDescriptionPaneHtmlRenderer().equals(
                         PropertyManager.FX_HTML_RENDERER)) {
             try {
                 descriptionPane = new DescriptionPaneFX();
 //            } catch (NoClassDefFoundError e) {
             } catch (Error e) {
                 logger.error("Error during initialisation of Java FX Description Pane. Using Calpa as fallback.", e);
                 descriptionPane = new DescriptionPaneCalpa();
             } catch (Exception e) {
                 logger.error(
                     "Exception during initialisation of Java FX Description Pane. Using Calpa as fallback.",
                     e);
                 descriptionPane = new DescriptionPaneCalpa();
             }
         } else {
             descriptionPane = new DescriptionPaneCalpa();
         }
 
         configurator = new FloatingFrameConfigurator(
                 ComponentRegistry.DESCRIPTION_PANE,
                 org.openide.util.NbBundle.getMessage(
                     Navigator.class,
                     "Navigator.initWidgets().configurator.name.descriptionPane")); // NOI18N
         // configurator.setTitleBarEnabled(false);
 
         final MutableConstraints descriptionPaneConstraints = new MutableConstraints(propertyManager
                         .isAdvancedLayout());
         descriptionPaneConstraints.addAsFloatingFrame(
             ComponentRegistry.DESCRIPTION_PANE,
             descriptionPane,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.descriptionpane.name"),    // NOI18N
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.descriptionpane.tooltip"), // NOI18N
             resourceManager.getIcon("descriptionpane_icon.gif"),
             MutableConstraints.P3,
             0,
             false,
             configurator,
             false);
         container.add(descriptionPaneConstraints);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void initDialogs() throws Exception {
         progressObserver.setProgress(
             350,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_350")); // NOI18N
 
         searchDialog = new SearchDialog(
                 this,
                 SessionManager.getProxy().getSearchOptions(),
                 SessionManager.getProxy().getClassTreeNodes());
 
         progressObserver.setProgress(
             550,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_550")); // NOI18N
         ComponentRegistry.registerComponents(
             this,
             container,
             menuBar,
             toolBar,
             popupMenu,
             metaCatalogueTree,
             searchResultsTree,
             attributeViewer,
             attributeEditor,
             searchDialog,
             descriptionPane);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void initPlugins() throws Exception {
         progressObserver.setProgress(
             575,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_575")); // NOI18N
         PluginRegistry.getRegistry().preloadPlugins();
 
         progressObserver.setProgress(
             650,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_650")); // NOI18N
         PluginRegistry.getRegistry().loadPlugins();
 
         progressObserver.setProgress(
             850,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_850")); // NOI18N
         PluginRegistry.getRegistry().activatePlugins();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  InterruptedException  DOCUMENT ME!
      */
     private void initEvents() throws InterruptedException {
         progressObserver.setProgress(
             900,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_900")); // NOI18N
         final StatusChangeListener statusChangeListener = new StatusChangeListener(statusBar);
 
         metaCatalogueTree.addStatusChangeListener(statusChangeListener);
         descriptionPane.addStatusChangeListener(statusChangeListener);
         searchDialog.addStatusChangeListener(statusChangeListener);
 
         final CatalogueSelectionListener catalogueSelectionListener = new CatalogueSelectionListener(
                 attributeViewer,
                 descriptionPane);
         metaCatalogueTree.addTreeSelectionListener(catalogueSelectionListener);
         searchResultsTree.addTreeSelectionListener(catalogueSelectionListener);
 
         metaCatalogueTree.addComponentListener(new CatalogueActivationListener(
                 metaCatalogueTree,
                 attributeViewer,
                 descriptionPane));
         searchResultsTree.addComponentListener(new CatalogueActivationListener(
                 searchResultsTree,
                 attributeViewer,
                 descriptionPane));
 
         final DefaultPopupMenuListener cataloguePopupMenuListener = new DefaultPopupMenuListener(popupMenu);
         metaCatalogueTree.addMouseListener(cataloguePopupMenuListener);
         searchResultsTree.addMouseListener(cataloguePopupMenuListener);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  InterruptedException  DOCUMENT ME!
      */
     private void initWindow() throws InterruptedException {
         progressObserver.setProgress(
             950,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_950")); // NOI18N
         this.setTitle(org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.title"));              // NOI18N
         this.setIconImage(resourceManager.getIcon("navigator_icon.gif").getImage());                          // NOI18N
         this.restoreWindowState();
         this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         this.addWindowListener(new Navigator.ClosingListener());
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initSearch() {
         new CidsSearchInitializer();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  InterruptedException  DOCUMENT ME!
      */
     private void initStartupHooks() throws InterruptedException {
         progressObserver.setProgress(
             980,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_980")); // NOI18N
 
         final Collection<? extends StartupHook> hooks = Lookup.getDefault().lookupAll(StartupHook.class);
 
         for (final StartupHook hook : hooks) {
             hook.applicationStarted();
         }
 
         progressObserver.setProgress(
             1000,
             org.openide.util.NbBundle.getMessage(Navigator.class, "Navigator.progressObserver.message_1000")); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws  InterruptedException  DOCUMENT ME!
      */
     private void initTakeoffHooks() throws InterruptedException {
         final Collection<? extends TakeoffHook> hooks = Lookup.getDefault().lookupAll(TakeoffHook.class);
 
         for (final TakeoffHook hook : hooks) {
             hook.applicationTakeoff();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   classes     DOCUMENT ME!
      * @param   permission  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean hasPermission(final MetaClass[] classes,
             final Sirius.server.newuser.permission.Permission permission) {
         final String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
 
         for (int i = 0; i < classes.length; i++) {
             try {
                 // falsch aufgerufen schlob SessionManager.getSession().getUser().getUserGroup().getKey()
                 final PermissionHolder perm = classes[i].getPermissions();
                 if (logger.isDebugEnabled()) {
                     logger.debug(" usergroup can edit ?? " + key + " permissions :: " + perm);               // NOI18N          //logger.debug(perm +" \n" +key);
                 }
                 if ((perm != null) && perm.hasPermission(key, permission))                                   // xxxxxxxxxxxxxxxxxxxxxx user????
                 {
                     if (logger.isDebugEnabled()) {
                         logger.debug("permission '" + permission + "' found in class '" + classes[i] + "'"); // NOI18N
                     }
                     return true;
                 }
             } catch (final Exception exp) {
                 logger.error("hasPermission(): could not check permissions", exp);                           // NOI18N
             }
         }
 
         logger.warn("permission '" + permission + "' not found, disabling editor"); // NOI18N
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  visible  DOCUMENT ME!
      */
     @Override
     public void setVisible(final boolean visible) {
         if (logger.isInfoEnabled()) {
             logger.info("setting main window visible to '" + visible + "'"); // NOI18N
         }
 
         if (SwingUtilities.isEventDispatchThread()) {
             doSetVisible(visible);
         } else {
             if (logger.isDebugEnabled()) {
                 logger.debug("doSetVisible(): synchronizing method"); // NOI18N
             }
             SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         doSetVisible(visible);
                     }
                 });
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  visible  DOCUMENT ME!
      */
     private void doSetVisible(final boolean visible) {
         final Point location = this.getLocation();
         // issue #8 we assume a position of 0,0 to be the default position. thus we center the navigator window then.
         if ((location.x == 0) && (location.y == 0)) {
             this.setLocationRelativeTo(this.loginDialog);
         }
 
         super.setVisible(visible);
 
         if (visible) {
             this.searchResultsTreePanel.setButtonsEnabled();
             this.container.setDividerLocations(0.23, 0.60);
             this.menuBar.repaint();
             this.toolBar.repaint();
 
             this.toFront();
         }
 
         SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     PluginRegistry.getRegistry().setPluginsVisible(visible);
                 }
             });
     }
     // .........................................................................
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void dispose() {
         if (logger.isInfoEnabled()) {
             logger.info("dispose() called"); // NOI18N
             logger.info("saving Layout");    // NOI18N
         }
         container.saveLayout(LayoutedContainer.DEFAULT_LAYOUT, this);
         Navigator.this.saveWindowState();
 
         configurationManager.writeConfiguration();
 
         PluginRegistry.destroy();
 
         SessionManager.getConnection().disconnect();
         SessionManager.destroy();
         MethodManager.destroy();
         ComponentRegistry.destroy();
 
         if (!Navigator.this.isDisposed()) {
             Navigator.super.dispose();
             Navigator.this.setDisposed(true);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void saveWindowState() {
         final int windowHeight = this.getHeight();
         final int windowWidth = this.getWidth();
         final int windowX = (int)this.getLocation().getX();
         final int windowY = (int)this.getLocation().getY();
         final boolean windowMaximised = (this.getExtendedState() == MAXIMIZED_BOTH);
 
         if (logger.isInfoEnabled()) {
             logger.info("saving window state: \nwindowHeight=" + windowHeight + ", windowWidth=" + windowWidth
                         + ", windowX=" + windowX + ", windowY=" + windowY + ", windowMaximised=" + windowMaximised); // NOI18N
         }
 
         this.preferences.putInt("windowHeight", windowHeight);           // NOI18N
         this.preferences.putInt("windowWidth", windowWidth);             // NOI18N
         this.preferences.putInt("windowX", windowX);                     // NOI18N
         this.preferences.putInt("windowY", windowY);                     // NOI18N
         this.preferences.putBoolean("windowMaximised", windowMaximised); // NOI18N
 
         if (logger.isInfoEnabled()) {
             logger.info("saved window state"); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void restoreWindowState() {
         if (logger.isInfoEnabled()) {
             logger.info("restoring window state ..."); // NOI18N
         }
 
         final int windowHeight = this.preferences.getInt("windowHeight", PropertyManager.getManager().getHeight()); // NOI18N
         final int windowWidth = this.preferences.getInt("windowWidth", PropertyManager.getManager().getWidth());    // NOI18N
         final int windowX = this.preferences.getInt("windowX", 0);                                                  // NOI18N
         final int windowY = this.preferences.getInt("windowY", 0);                                                  // NOI18N
 
         final boolean windowMaximised;
         // issue #8: osx does to correctly determine the maximised state of a window, thus we ignore that on osx
         final String osName = System.getProperty("os.name"); // NOI18N
         if (osName.startsWith("Mac")) {
             windowMaximised = false;
         } else {
             windowMaximised = this.preferences.getBoolean(
                     "windowMaximised",                       // NOI18N
                     propertyManager.isMaximizeWindow());
         }
 
         if (logger.isInfoEnabled()) {
             logger.info("restoring window state: \nwindowHeight=" + windowHeight + ", windowWidth=" + windowWidth
                         + ", windowX=" + windowX + ", windowY=" + windowY + ", windowMaximised=" + windowMaximised); // NOI18N
         }
 
         this.setSize(windowWidth, windowHeight);
         this.setLocation(windowX, windowY);
 
         if (windowMaximised) {
             this.setExtendedState(MAXIMIZED_BOTH);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         Runtime.getRuntime().addShutdownHook(new Navigator.NavigatorShutdown());
         Thread.setDefaultUncaughtExceptionHandler(new DefaultNavigatorExceptionHandler());
 
         // There is no way to adjust the Locale using the Jnlp file.
         try {
             JnlpTools.adjustDefaultLocale();
 
             System.out.println("Using default Locale: " + Locale.getDefault());
         } catch (final SecurityException e) {
             System.err.println("You have insufficient rights to set the default locale."); // NOI18N
         }
 
         try {
             // cmdline arguments ...............................................
 
             if (args.length < 5) {
                 // FIXME: use correct error string
                 final String errorString = new String(
                         "\nusage: navigator %1 %2 %3 %4 %5 %6(%5)\n%1 = navigator config file \n%2 = navigator working directory \n%3 = plugin base directory \n%4 = navigator search forms base directory \n%5 navigator search profile store (optional, default: %1/profiles \n\nexample: java Sirius.navigator.Navigator c:\\programme\\cids\\navigator\\navigator.cfg c:\\programme\\cids\\navigator\\ c:\\programme\\cids\\navigator\\plugins\\ c:\\programme\\cids\\navigator\\search\\ c:\\programme\\cids\\navigator\\search\\profiles\\"); // NOI18N
                 System.out.println(errorString);
 
                 throw new Exception(errorString);
             } else {
                 System.out.println("-------------------------------------------------------"); // NOI18N
                 System.out.println("C I D S   N A V I G A T 0 R   C O N F I G U R A T I 0 N"); // NOI18N
                 System.out.println("-------------------------------------------------------"); // NOI18N
                 System.out.println("log4j.properties = " + args[0]);                           // NOI18N
                 System.out.println("navigator.cfg    = " + args[1]);                           // NOI18N
                 System.out.println("basedir          = " + args[2]);                           // NOI18N
                 System.out.println("plugindir        = " + args[3]);                           // NOI18N
                 System.out.println("-------------------------------------------------------"); // NOI18N
 
                 // log4j configuration .....................................
                 final Properties properties = new Properties();
                 boolean l4jinited = false;
                 try {
                     final URL log4jPropertiesURL = new URL(args[0]);
                     properties.load(log4jPropertiesURL.openStream());
 
                     l4jinited = true;
                 } catch (final Exception e) {
                     System.err.println("could not lode log4jproperties will try to load it from file" // NOI18N
                                 + e.getMessage());
                     e.printStackTrace();
                 }
 
                 try {
                     if (!l4jinited) {
                         properties.load(new BufferedInputStream(new FileInputStream(new File(args[0]))));
                     }
                 } catch (Exception e) {
                     System.err.println("could not lode log4jproperties " + e.getMessage()); // NOI18N
                     e.printStackTrace();
                 }
 
                 PropertyConfigurator.configure(properties);
 
                 // log4j configuration .....................................
 
                 PropertyManager.getManager()
                         .configure(args[1], args[2], args[3], null, ((args.length > 5) ? args[5] : null));
             }
 
             // configuration ...................................................
 
             // look and feel ...................................................
             LAFManager.getManager().changeLookAndFeel(PropertyManager.getManager().getLookAndFeel());
 
             final NavigatorSplashScreen navigatorSplashScreen = new NavigatorSplashScreen(PropertyManager.getManager()
                             .getSharedProgressObserver(),
                     // FIXME: illegal icon
                     resourceManager.getIcon("wundaLogo.png"));
 
             navigatorSplashScreen.pack();
             navigatorSplashScreen.setLocationRelativeTo(null);
             navigatorSplashScreen.toFront();
             navigatorSplashScreen.show();
             // run .............................................................
         } catch (final Throwable t) {
             // error .............................................................
             Logger.getLogger(Navigator.class).fatal("could not create navigator instance", t); // NOI18N
             ExceptionManager.getManager()
                     .showExceptionDialog(
                         ExceptionManager.FATAL,
                         org.openide.util.NbBundle.getMessage(
                             Navigator.class,
                             "Navigator.main(String[]).ExceptionManager_anon.name"),            // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             Navigator.class,
                             "Navigator.main(String[]).ExceptionManager_anon.message"),
                         t);                                                                    // NOI18N
 
             System.exit(1);
             // error .............................................................
         }
     }
 
     /**
      * Getter for property disposed.
      *
      * @return  Value of property disposed.
      */
     public boolean isDisposed() {
         return this.disposed;
     }
 
     /**
      * Setter for property disposed.
      *
      * @param  disposed  New value of property disposed.
      */
     private synchronized void setDisposed(final boolean disposed) {
         this.disposed = disposed;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initHttpServer() {
         try {
             RESTRemoteControlStarter.initRestRemoteControlMethods(propertyManager.getHttpInterfacePort());
         } catch (Throwable e) {
             logger.error("Error during initializion of remote control server", e);
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class NavigatorShutdown extends Thread {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final transient Logger LOG = Logger.getLogger(Navigator.NavigatorShutdown.class);
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          */
         @Override
         public void run() {
             if (startupFinished) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Shutting down Navigator..."); // NOI18N
                 }
 
                 final Collection<? extends ShutdownHook> hooks = Lookup.getDefault().lookupAll(ShutdownHook.class);
 
                 for (final ShutdownHook hook : hooks) {
                     hook.applicationFinished();
                 }
 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Shutting down Navigator... FINISHED"); // NOI18N
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ClosingListener extends WindowAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * Invoked when the user attempts to close the window from the window's system menu. If the program does not
          * explicitly hide or dispose the window while processing this event, the window close operation will be
          * cancelled.
          *
          * @param  e  DOCUMENT ME!
          */
         @Override
         public void windowClosing(final WindowEvent e) {
             if (exceptionManager.showExitDialog(Navigator.this)) {
                 dispose();
                 if (logger.isInfoEnabled()) {
                     logger.info("closing navigator"); // NOI18N
                 }
                 System.exit(0);
             }
         }
     }
 }
