 /***************************************************
  *
  * cismet GmbH, Saarbruecken, Germany
  *
  *              ... and it just works.
  *
  ****************************************************/
 /*
  * Main.java
  *
  * Created on 6. Januar 2005, 14:55
  */
 package de.cismet.verdis.gui;
 
 import Sirius.navigator.connection.*;
 import Sirius.navigator.connection.proxy.ConnectionProxy;
 import Sirius.navigator.plugin.context.PluginContext;
 import Sirius.navigator.plugin.interfaces.*;
 import Sirius.navigator.plugin.listener.MetaNodeSelectionListener;
 import Sirius.navigator.search.dynamic.FormDataBean;
 import Sirius.navigator.types.iterator.AttributeRestriction;
 import Sirius.navigator.types.iterator.ComplexAttributeRestriction;
 import Sirius.navigator.types.iterator.SingleAttributeIterator;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.DescriptionPane;
 import Sirius.navigator.ui.DescriptionPaneFS;
 import Sirius.server.middleware.types.MetaObject;
 import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
 import com.sun.jersey.api.container.ContainerFactory;
 import com.sun.net.httpserver.HttpHandler;
 import com.sun.net.httpserver.HttpServer;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.Point;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.dynamics.CidsBeanStore;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import de.cismet.cids.tools.search.clientstuff.CidsToolbarSearch;
 import de.cismet.cismap.commons.CrsTransformer;
 import de.cismet.cismap.commons.features.*;
 import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
 import de.cismet.cismap.commons.featureservice.SimplePostgisFeatureService;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.*;
 import de.cismet.cismap.commons.gui.simplelayerwidget.NewSimpleInternalLayerWidget;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.rasterservice.MapService;
 import de.cismet.cismap.commons.wfsforms.AbstractWFSForm;
 import de.cismet.cismap.navigatorplugin.BeanUpdatingCidsFeature;
 import de.cismet.extensions.timeasy.TimEasyDialog;
 import de.cismet.extensions.timeasy.TimEasyEvent;
 import de.cismet.extensions.timeasy.TimEasyListener;
 import de.cismet.extensions.timeasy.TimEasyPureNewFeature;
 import de.cismet.lookupoptions.gui.OptionsClient;
 import de.cismet.lookupoptions.gui.OptionsDialog;
 import de.cismet.rmplugin.RMPlugin;
 import de.cismet.tools.StaticDebuggingTools;
 import de.cismet.tools.configuration.Configurable;
 import de.cismet.tools.configuration.ConfigurationManager;
 import de.cismet.tools.gui.Static2DTools;
 import de.cismet.tools.gui.downloadmanager.DownloadManagerAction;
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 import de.cismet.tools.gui.startup.StaticStartupTools;
 import de.cismet.validation.Validator;
 import de.cismet.validation.ValidatorListener;
 import de.cismet.validation.ValidatorState;
 import de.cismet.validation.validator.AggregatedValidator;
 import de.cismet.verdis.AppModeListener;
 import de.cismet.verdis.CidsAppBackend;
 import de.cismet.verdis.FlaechenClipboard;
 import de.cismet.verdis.FlaechenClipboardListener;
 import de.cismet.verdis.constants.KassenzeichenPropertyConstants;
 import de.cismet.verdis.constants.VerdisMetaClassConstants;
 import de.cismet.verdis.crossover.VerdisCrossover;
 import de.cismet.verdis.data.AppPreferences;
 import de.cismet.verdis.interfaces.CidsBeanTable;
 import de.cismet.verdis.interfaces.Storable;
 import de.cismet.verdis.search.AlkisLandparcelSearch;
 import edu.umd.cs.piccolo.PCanvas;
 import edu.umd.cs.piccolo.PNode;
 import edu.umd.cs.piccolox.event.PNotification;
 import edu.umd.cs.piccolox.event.PNotificationCenter;
 import edu.umd.cs.piccolox.event.PSelectionEventHandler;
 import java.applet.AppletContext;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.*;
 import java.net.InetSocketAddress;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.prefs.Preferences;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import net.infonode.docking.*;
 import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
 import net.infonode.docking.properties.RootWindowProperties;
 import net.infonode.docking.theme.DockingWindowsTheme;
 import net.infonode.docking.theme.ShapedGradientDockingTheme;
 import net.infonode.docking.util.DeveloperUtil;
 import net.infonode.docking.util.DockingUtil;
 import net.infonode.docking.util.PropertiesUtil;
 import net.infonode.docking.util.StringViewMap;
 import net.infonode.gui.componentpainter.AlphaGradientComponentPainter;
 import net.infonode.gui.componentpainter.GradientComponentPainter;
 import net.infonode.util.Direction;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.JXLoginPane;
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.auth.DefaultUserNameStore;
 import org.jdesktop.swingx.auth.LoginService;
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.jdom.Element;
 
 /**
  * DOCUMENT ME!
  *
  * @author   hell
  * @version  $Revision$, $Date$
  */
 public final class Main extends javax.swing.JFrame implements PluginSupport,
         FloatingPluginUI,
         Storable,
         AppModeListener,
         Configurable,
         CidsBeanStore {
 
     //~ Static fields/initializers ---------------------------------------------
     private static boolean noLoginDuringDev = false;
     private static boolean loggedIn = false;
     public static int KASSENZEICHEN_CLASS_ID = 11;
     public static int GEOM_CLASS_ID = 0;
     public static int DMS_URL_BASE_ID = 1;
     public static int DMS_URL_ID = 2;
     public static double INITIAL_WMS_BB_X1 = 2569442.79;
     public static double INITIAL_WMS_BB_Y1 = 5668858.33;
     public static double INITIAL_WMS_BB_X2 = 2593744.91;
     public static double INITIAL_WMS_BB_Y2 = 5688416.22;
     public static final int PROPVAL_ART_DACH = 1;
     public static final int PROPVAL_ART_GRUENDACH = 2;
     public static final int PROPVAL_ART_VERSIEGELTEFLAECHE = 3;
     public static final int PROPVAL_ART_OEKOPFLASTER = 4;
     public static final int PROPVAL_ART_STAEDTISCHESTRASSENFLAECHE = 5;
     public static final int PROPVAL_ART_STAEDTISCHESTRASSENFLAECHEOEKOPLFASTER = 6;
 
     private static final String DIRECTORYPATH_HOME = System.getProperty("user.home");
     private static final String FILESEPARATOR = System.getProperty("file.separator");
     private static final String DIRECTORYEXTENSION = System.getProperty("directory.extension");
 
     private static final String DIRECTORY_VERDISHOME = ".verdis" + ((DIRECTORYEXTENSION != null) ? DIRECTORYEXTENSION : "");
     private static final String FILE_LAYOUT = "verdis.layout";
     private static final String FILE_SCREEN = "verdis.screen";
     private static final String FILE_PLUGINLAYOUT = "plugin.layout";
 
     private static final String DIRECTORYPATH_VERDIS = DIRECTORYPATH_HOME + FILESEPARATOR + DIRECTORY_VERDISHOME;
     private static final String FILEPATH_LAYOUT = DIRECTORYPATH_VERDIS + FILESEPARATOR + FILE_LAYOUT;
     private static final String FILEPATH_SCREEN = DIRECTORYPATH_VERDIS + FILESEPARATOR + FILE_SCREEN;
     private static final String FILEPATH_PLUGINLAYOUT = DIRECTORYPATH_VERDIS + FILESEPARATOR + FILE_PLUGINLAYOUT;
 
     private static Main THIS;
     private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Main.class);
     private static JFrame SPLASH;
     private CidsAppBackend.Mode currentMode = null;
     
     //~ Instance fields --------------------------------------------------------
     de.cismet.tools.ConnectionInfo connectionInfo = new de.cismet.tools.ConnectionInfo();
     private JDialog about = null;
     // Inserting Docking Window functionalty (Sebastian) 24.07.07
     private Icon icoKassenzeichen = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/kassenzeichen.png"));
     private Icon icoSummen = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/sum.png"));
     private Icon icoKanal = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/pipe.png"));
     private Icon icoDokumente = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/docs.png"));
     private Icon icoFlaechen = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/flaechen.png"));
     private Icon icoKarte = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/flaechen.png"));
     private Icon icoTabelle = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/flaechen.png"));
     private Icon icoDetails = new javax.swing.ImageIcon(getClass().getResource(
             "/de/cismet/verdis/res/images/titlebars/flaechen.png"));
     private Image banner = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/login.png")).getImage();
     // private Color myBlue=new java.awt.Color(0, 51, 153);
     private Color myBlue = new Color(124, 160, 221);
     private boolean editmode = false;
     private boolean plugin = false;
     private boolean readonly = true;
     /** Creates new form Main. */
     private String userString;
     private String kassenzeichenSuche = "kassenzeichenSuche";
     private String wmsBackgroundUrlTemplate = "";
     private String userGroup = "noGroup";
     private FlaechenClipboard flaechenClipboard;
     private AppPreferences prefs;
     // Inserting Docking Window functionalty (Sebastian) 24.07.07
     private View vKassenzeichen;
     private View vKanaldaten;
     private View vSummen;
     private View vDokumente;
     private View vKarte;
     private View vTabelleWDSR;
     private View vDetailsWDSR;
     private View vZusammenfassungWDSR;
     private View vInfoAllgemein;
     private View vTabelleRegen;
     private View vDetailsRegen;
     private View vHistory;
     private RootWindow rootWindow;
     private final StringViewMap viewMap = new StringViewMap();
     private final ArrayList<JMenuItem> menues = new ArrayList<JMenuItem>();
     private final ActiveLayerModel mappingModel = new ActiveLayerModel();
     private final String verdisConfig = ".verdisConfig";
     private final ConfigurationManager configurationManager = new ConfigurationManager();
     private final RegenFlaechenSummenPanel regenSumPanel;
     private final DokumentenPanel dokPanel;
     private final KassenzeichenPanel kassenzeichenPanel;
     private final KanaldatenPanel kanaldatenPanel;
     private final AllgemeineInfosPanel allgInfosPanel;
     private final RegenFlaechenDetailsPanel regenFlaechenDetailsPanel;
     private final RegenFlaechenTabellenPanel regenFlaechenTabellenPanel;
     private final KartenPanel kartenPanel;
     private final WDSRTabellenPanel wdsrFrontenTabellenPanel;
     private final WDSRDetailsPanel wdsrFrontenDetailsPanel;
     private final WDSRSummenPanel wdsrSummenPanel;
     private final AggregatedValidator aggValidator = new AggregatedValidator();
     private final Collection<CidsToolbarSearch> toolbarSearches = new ArrayList<CidsToolbarSearch>();
 //    private HistoryPanel historyPanel;
     private RMPlugin rmPlugin;
     private boolean fixMapExtent;
     private boolean isInit = true;
     private PluginContext context;
     private CidsBean kassenzeichenBean;
     private JDialog alkisRendererDialog;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnHistory;
     private javax.swing.JButton cmdAdd;
     private javax.swing.JButton cmdCancel;
     private javax.swing.JButton cmdCopyFlaeche;
     private javax.swing.JButton cmdCutFlaeche;
     private javax.swing.JButton cmdDeleteKassenzeichen;
     private javax.swing.JButton cmdDownloads;
     private javax.swing.JButton cmdEditMode;
     private javax.swing.JButton cmdFortfuehrung;
     private javax.swing.JButton cmdInfo;
     private javax.swing.JButton cmdLagisCrossover;
     private javax.swing.JButton cmdNewKassenzeichen;
     private javax.swing.JButton cmdOk;
     private javax.swing.JButton cmdPasteFlaeche;
     private javax.swing.JButton cmdPdf;
     private javax.swing.JButton cmdPutKassenzeichenToSearchTree;
     private javax.swing.JButton cmdRefreshEnumeration;
     private javax.swing.JButton cmdRemove;
     private javax.swing.JButton cmdTest;
     private javax.swing.JButton cmdTest2;
     private javax.swing.JButton cmdUndo;
     private javax.swing.JButton cmdWorkflow;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator10;
     private javax.swing.JSeparator jSeparator11;
     private javax.swing.JToolBar.Separator jSeparator12;
     private javax.swing.JToolBar.Separator jSeparator13;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JSeparator jSeparator5;
     private javax.swing.JSeparator jSeparator6;
     private javax.swing.JSeparator jSeparator7;
     private javax.swing.JSeparator jSeparator8;
     private javax.swing.JSeparator jSeparator9;
     private javax.swing.JMenu menEdit;
     private javax.swing.JMenu menExtras;
     private javax.swing.JMenu menFile;
     private javax.swing.JMenu menHelp;
     private javax.swing.JMenu menWindows;
     private javax.swing.JMenuItem mniClose;
     private javax.swing.JMenuItem mniDetails;
     private javax.swing.JMenuItem mniDokumente;
     private javax.swing.JMenuItem mniFlaechen;
     private javax.swing.JMenuItem mniKanalanschluss;
     private javax.swing.JMenuItem mniKarte;
     private javax.swing.JMenuItem mniKassenzeichen;
     private javax.swing.JMenuItem mniLoadLayout;
     private javax.swing.JMenuItem mniOptions;
     private javax.swing.JMenuItem mniResetWindowLayout;
     private javax.swing.JMenuItem mniSaveLayout;
     private javax.swing.JMenuItem mniSummen;
     private javax.swing.JMenuItem mniTabelle;
     private javax.swing.JMenuItem mnuChangeUser;
     private javax.swing.JMenuItem mnuEditMode;
     private javax.swing.JMenuItem mnuHelp;
     private javax.swing.JMenuItem mnuInfo;
     private javax.swing.JMenuItem mnuNewKassenzeichen;
     private javax.swing.JMenuItem mnuRenameKZ;
     private javax.swing.JPanel panMain;
     private javax.swing.JPopupMenu.Separator sepOptions;
     private javax.swing.JToolBar tobVerdis;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
     /**
      * Creates a new Main object.
      */
     public Main() {
         this(null);
     }
 
     /**
      * Creates a new Main object.
      *
      * @param  context  DOCUMENT ME!
      */
     public Main(final PluginContext context) {
         if (StaticDebuggingTools.checkHomeForFile("cismetBeansbindingDebuggingOn")) {                     // NOI18N
             System.setProperty("cismet.beansdebugging", "true");                                          // NOI18N
         }
 
         if (context == null) { // ACHTUNG
             try {
                 if (StaticDebuggingTools.checkHomeForFile("cismetCustomLog4JConfigurationInDotVerdis")) {
                     try {
                         org.apache.log4j.PropertyConfigurator.configure(DIRECTORYPATH_VERDIS + FILESEPARATOR
                                 + "custom.log4j.properties");
                         LOG.info("CustomLoggingOn");
                     } catch (Exception ex) {
                         org.apache.log4j.PropertyConfigurator.configure(ClassLoader.getSystemResource(
                                 "log4j.properties"));
                     }
                 } else {
                     org.apache.log4j.PropertyConfigurator.configure(getClass().getResource(
                             "log4j.properties"));
                 }
             } catch (Exception e) {
                 LOG.debug("Fehler bei Log4J-Config", e);
             }
         }
         System.setProperty("cismet.beansdebugging", "true");
 
         try {
             javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
         } catch (Exception ex) {
             LOG.error("error while setting setLookAndFeel", ex);
         }
 
 
         try {
             this.loadProperties();
         } catch (Exception propEx) {
             LOG.fatal("Fehler beim Laden der Properties!", propEx);
         }
 
         if (context == null) {
             login();
         } else {
             CidsAppBackend.init(Sirius.navigator.connection.SessionManager.getProxy());
         }
 
 
 //        toolbarSearches.add(new KassenzeichenToolbarSearch());
 //
 //        CidsSearchComboBar searchBar = new CidsSearchComboBar();
 //        searchBar.setSearches(toolbarSearches);
 //        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
 //        innerPanel.add(searchBar);
 //        tobVerdis.add(innerPanel, -1);
 
         // EventDispatchThreadHangMonitor.initMonitoring();
         // RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
 
         regenSumPanel = new de.cismet.verdis.gui.RegenFlaechenSummenPanel();
 
 
         dokPanel = new de.cismet.verdis.gui.DokumentenPanel();
 
 
 
         kassenzeichenPanel = new de.cismet.verdis.gui.KassenzeichenPanel();
 
         kanaldatenPanel = new de.cismet.verdis.gui.KanaldatenPanel();
 
         allgInfosPanel = new AllgemeineInfosPanel();
 
         regenFlaechenDetailsPanel = new RegenFlaechenDetailsPanel();
         regenFlaechenTabellenPanel = new RegenFlaechenTabellenPanel();
 
         kartenPanel = new KartenPanel();
 
         wdsrFrontenTabellenPanel = new WDSRTabellenPanel();
         wdsrFrontenDetailsPanel = new WDSRDetailsPanel();
         wdsrSummenPanel = new WDSRSummenPanel();
 
 //        historyPanel = new HistoryPanel();
 
         kanaldatenPanel.setMain(this);
         THIS = this;
         plugin = !(context == null);
         this.context = context;
 
         CidsAppBackend.getInstance().addCidsBeanStore(this);
         CidsAppBackend.getInstance().addCidsBeanStore(kassenzeichenPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(wdsrFrontenTabellenPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(wdsrSummenPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(kartenPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(dokPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(allgInfosPanel);
 //        CidsAppBackend.getInstance().addCidsBeanStore(historyPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(regenFlaechenTabellenPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(kanaldatenPanel);
         CidsAppBackend.getInstance().addCidsBeanStore(regenSumPanel);
         
         CidsAppBackend.getInstance().addEditModeListener(kassenzeichenPanel);
         CidsAppBackend.getInstance().addEditModeListener(wdsrFrontenDetailsPanel);
         CidsAppBackend.getInstance().addEditModeListener(allgInfosPanel);
         CidsAppBackend.getInstance().addEditModeListener(regenFlaechenDetailsPanel);
         CidsAppBackend.getInstance().addEditModeListener(kartenPanel);
         CidsAppBackend.getInstance().addEditModeListener(dokPanel);
         CidsAppBackend.getInstance().addEditModeListener(kanaldatenPanel);
 
         CidsAppBackend.getInstance().addAppModeListener(kartenPanel);
         CidsAppBackend.getInstance().addAppModeListener(this);
         CidsAppBackend.getInstance().addAppModeListener(kassenzeichenPanel);
 
         CidsAppBackend.getInstance().getMainMap().getFeatureCollection().addFeatureCollectionListener(regenFlaechenTabellenPanel);
         CidsAppBackend.getInstance().getMainMap().getFeatureCollection().addFeatureCollectionListener(wdsrFrontenTabellenPanel);
         CidsAppBackend.getInstance().getMainMap().getFeatureCollection().addFeatureCollectionListener(new FeatureCollectionAdapter() {
 
             @Override
             public void featureSelectionChanged(FeatureCollectionEvent fce) {
                 refreshItemButtons();
             }
 
             @Override
             public void featuresAdded(FeatureCollectionEvent fce) {
                 refreshItemButtons();
                 for (final Feature feature : fce.getEventFeatures()) {
                     if (feature instanceof PostgisFeature) {
                         final PostgisFeature postgisFeature = (PostgisFeature) feature;
                         LOG.fatal(postgisFeature.getFeatureType());
                         LOG.fatal(postgisFeature.getObjectName());
                         if (postgisFeature.getFeatureType().equals("Versiegelte Flächen")) {
                             //fce.get
                         }
                     }
                 }
         //        CidsAppBackend.getInstance().getMode()
             }
 
             @Override
             public void featuresChanged(FeatureCollectionEvent fce) {
                 refreshItemButtons();
             }
 
             @Override
             public void featuresRemoved(FeatureCollectionEvent fce) {
                 refreshItemButtons();
             }
         });
 
         CidsAppBackend.getInstance().setFeatureAttacher(CidsAppBackend.Mode.REGEN, regenFlaechenTabellenPanel);
         CidsAppBackend.getInstance().setFeatureAttacher(CidsAppBackend.Mode.ESW, wdsrFrontenTabellenPanel);
 
         final PCanvas pc = CidsAppBackend.getInstance().getMainMap().getSelectedObjectPresenter();
         pc.setBackground(this.getBackground());
         regenFlaechenDetailsPanel.setBackgroundPCanvas(pc);
         wdsrFrontenDetailsPanel.setBackgroundPCanvas(pc);
 
         wdsrFrontenTabellenPanel.setSelectedRowListener(wdsrFrontenDetailsPanel);
         regenFlaechenTabellenPanel.setSelectedRowListener(regenFlaechenDetailsPanel);
 
         try {
             try {
                 if ((context != null) && (context.getEnvironment() != null)
                         && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setProgress(0, "verdis Plugin laden...");
                 }
             } catch (Exception e) {
                 LOG.error("Keine Progressmeldung", e);
             }
 
 
 
 //            else {
 //                try {
 //                   String log4jProperties = context.getEnvironment().getParameter("log4j");//context.getEnvironment().getAppletContext().getApplet("cids - WuNDa Navigator Applet").getParameter("log4j");
 //                   org.apache.log4j.PropertyConfigurator.configure(new URL(context.getEnvironment().getCodeBase().toString() + '/' + log4jProperties));
 //                } catch (Exception e) {
 //                   try {
 //                       org.apache.log4j.PropertyConfigurator.configure(new URL(context.getEnvironment().getCodeBase().toString() + "/config/log4j.properties"));
 //                   }
 //                   catch(Throwable t) {
 //                       t.printStackTrace();
 //                   }
 //                   System.err.println("Error before");
 //                   e.printStackTrace();
 //                }
 //            }
 
             // ClearLookManager.setMode(ClearLookMode.ON);
             // PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
             try {
                 // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
                 javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                 // javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
                 // javax.swing.UIManager.setLookAndFeel(new com.jgoodies.plaf.plastic.PlasticXPLookAndFeel());
                 // UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
                 // UIManager.setLookAndFeel(new PlasticLookAndFeel());
             } catch (Exception e) {
                 LOG.warn("Fehler beim Einstellen des Look&Feels's!", e);
             }
 
 
             LOG.info("Verdis gestartet :-)");
             if ((context != null) && (context.getEnvironment() != null)
                     && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(100, "verdis Plugin DB Verbindung setzen...");
             }
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Gui kram erledigt");
 
 //                 UIDefaults uiDefaults = UIManager.getDefaults();
 //                java.util.Enumeration keys = uiDefaults.keys();
 //                while (keys.hasMoreElements())
 //                {
 //                    Object key=keys.nextElement();
 //                    System.out.println(key.toString()+":"+uiDefaults.get(key));
 //                }
                 LOG.debug("initComponents()");
             }
             if ((context != null) && (context.getEnvironment() != null)
                     && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(200, "verdis Plugin: Oberfl\u00E4che initialisieren ...");
             }
 
 
             initComponents();
 
             // dialog for alkis_landparcel
             DescriptionPane descriptionPane = new DescriptionPaneFS();
             ComponentRegistry.registerComponents(null, null, null, null, null, null, null, null, null, null, descriptionPane);
             alkisRendererDialog = new JDialog(Main.THIS, false);
             alkisRendererDialog.setTitle("Alkis Renderer");
             alkisRendererDialog.setContentPane(descriptionPane);
             alkisRendererDialog.setSize(1000, 800);
             alkisRendererDialog.setLocationRelativeTo(Main.THIS);
 
             // Menu for Navigator
             if (plugin) {
                 final JMenu navigatorMenue = new JMenu("Verdis");
                 navigatorMenue.add(mniKassenzeichen);
                 navigatorMenue.add(mniDokumente);
                 navigatorMenue.add(mniSummen);
                 navigatorMenue.add(mniFlaechen);
                 navigatorMenue.add(mniKanalanschluss);
                 // navigatorMenue.add(menWindows.getItem(4));
                 navigatorMenue.add(new JSeparator());
                 // navigatorMenue.add(menWindows.getItem(6));
                 navigatorMenue.add(mniLoadLayout);
                 navigatorMenue.add(mniSaveLayout);
                 navigatorMenue.add(mniResetWindowLayout);
                 menues.add(navigatorMenue);
             }
 
             if (!plugin) {
                 configurationManager.setFileName("configuration.xml");
             } else {
                 configurationManager.setFileName("configurationPlugin.xml");
             }
             configurationManager.setClassPathFolder("/verdis/");
             configurationManager.setFolder(DIRECTORY_VERDISHOME);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("mc:" + getMappingComponent());
             }
             configurationManager.addConfigurable(mappingModel);
             configurationManager.addConfigurable(getMappingComponent());
             configurationManager.addConfigurable(this);
 
             configurationManager.addConfigurable(OptionsClient.getInstance());
             configurationManager.configure(OptionsClient.getInstance());
 
 //            getMappingComponent().setPreferredSize(new Dimension(100,100));
             //validateTree();
             // is needed to compute the mappingComponent size, so that
             // the layers are displayed correctly
 
 
             // Anwendungslogik
             if ((context != null) && (context.getEnvironment() != null)
                     && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(300, "verdis Plugin: Widgets verbinden ...");
             }
 
 
 
             if ((context != null) && (context.getEnvironment() != null)
                     && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(500, "verdis Plugin: GIS Einstellungen ...");
             }
             LOG.info("Einstellungen der Karte vornehmen");
 //            flPanel.setCismapPreferences(prefs.getCismapPrefs());
             enableEditing(false);
             if (LOG.isDebugEnabled()) {
                 LOG.debug("fertig");
             }
             String host = "unknown";
             try {
                 final java.net.InetAddress i = java.net.InetAddress.getLocalHost();
                 host = i.getHostAddress();
                 host = i.getHostName();
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("kein Hostname", e);
                 }
             }
             if (LOG.isDebugEnabled()) {
                 LOG.debug(userString);
             }
 
             kassenzeichenPanel.setMainApp(this);
 
             final KeyStroke configLoggerKeyStroke = KeyStroke.getKeyStroke('L', InputEvent.CTRL_MASK);
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
             getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(configLoggerKeyStroke, "CONFIGLOGGING");
             getRootPane().getActionMap().put("CONFIGLOGGING", configAction);
 
             // WFSForms
 // Thread wfsformsThread=new Thread() {
 // public void run() {
             final Set<String> keySet = prefs.getWfsForms().keySet();
 //            JMenu wfsFormsMenu=new JMenu();
             if (LOG.isDebugEnabled()) {
                 LOG.debug("WFSForms " + keySet);
             }
             for (final String key : keySet) {
                 //
                 try {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("WFSForms: " + key);
                     }
                     final AbstractWFSForm form = prefs.getWfsForms().get(key);
 //                form.setPreferredSize(new Dimension(450,50));
                     final JDialog formView = new JDialog(this, form.getTitle());
                     formView.getContentPane().setLayout(new BorderLayout());
                     formView.getContentPane().add(form, BorderLayout.CENTER);
                     formView.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                     form.setMappingComponent(CidsAppBackend.getInstance().getMainMap());
 
                     formView.pack();
 //                final View formView=createView(form.getId(),form.getTitle(),form);
 //                wfsFormViews.add(formView);
 //                formView.setTabText("   "+formView.getTabText()+"   ");
 //                formView.setTabIcon(Static2DTools.borderIcon(form.getIcon(),10,0,3,0));
 //                formView.setIcon(form.getIcon());
                     // Menu
                     final JButton cmd = new JButton(null, form.getIcon());
                     cmd.setToolTipText(form.getMenuString());
                     cmd.addActionListener(new ActionListener() {
 
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             formView.setLocationRelativeTo(Main.this);
                             formView.setVisible(true);
                         }
                     });
 
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             tobVerdis.add(cmd);
                         }
                     });
                 } catch (Throwable thr) {
                     LOG.error("Fehler beim Hinzuf\u00FCgen einer WFSForm", thr);
                 }
             }
 //                }
 //            };
 //            wfsformsThread.setPriority(Thread.NORM_PRIORITY);
 //            wfsformsThread.start();
             // Inserting Docking Window functionalty (Sebastian) 24.07.07
             rootWindow = DockingUtil.createRootWindow(viewMap, true);
 
             // Views anlegen
 
             vKassenzeichen = new View("Kassenzeichen", Static2DTools.borderIcon(icoKassenzeichen, 0, 3, 0, 1), kassenzeichenPanel);
             viewMap.addView("Kassenzeichen", vKassenzeichen);
             vKassenzeichen.getCustomTitleBarComponents().addAll(kassenzeichenPanel.getCustomButtons());
 
             vSummen = new View("Summen", Static2DTools.borderIcon(icoSummen, 0, 3, 0, 1), regenSumPanel);
             viewMap.addView("Summen", vSummen);
 
             vKanaldaten = new View("Kanalanschluss", Static2DTools.borderIcon(icoKanal, 0, 3, 0, 1), kanaldatenPanel);
             viewMap.addView("Kanalanschluss", vKanaldaten);
 
             vDokumente = new View("Dokumente", Static2DTools.borderIcon(icoDokumente, 0, 3, 0, 1), dokPanel);
             viewMap.addView("Dokumente", vDokumente);
 
             vKarte = new View("Karte", Static2DTools.borderIcon(icoKarte, 0, 3, 0, 1), kartenPanel);
             viewMap.addView("Karte", vKarte);
 
             vTabelleWDSR = new View(
                     "Tabellenansicht (Fronten)",
                     Static2DTools.borderIcon(icoTabelle, 0, 3, 0, 1),
                     wdsrFrontenTabellenPanel);
             viewMap.addView("Tabellenansicht (Fronten)", vTabelleWDSR);
 
             vDetailsWDSR = new View(
                     "Details (Fronten)",
                     Static2DTools.borderIcon(icoDetails, 0, 3, 0, 1),
                     wdsrFrontenDetailsPanel);
             viewMap.addView("Details", vDetailsWDSR);
 
             vZusammenfassungWDSR = new View(
                     "ESW Zusammenfassung",
                     Static2DTools.borderIcon(icoTabelle, 0, 3, 0, 1),
                     wdsrSummenPanel);
             viewMap.addView("ESW Zusammenfassung", vZusammenfassungWDSR);
 
             vInfoAllgemein = new View(
                     "Informationen",
                     Static2DTools.borderIcon(icoTabelle, 0, 3, 0, 1),
                     allgInfosPanel);
             viewMap.addView("Informationen", vInfoAllgemein);
 
             vTabelleRegen = new View(
                     "Tabellenansicht (versiegelte Fl\u00E4chen)",
                     Static2DTools.borderIcon(icoTabelle, 0, 3, 0, 1),
                     regenFlaechenTabellenPanel);
             viewMap.addView("Tabellenansicht (versiegelte Flaechen)", vTabelleRegen);
 
             vDetailsRegen = new View(
                     "Details (versiegelte Fl\u00E4chen)",
                     Static2DTools.borderIcon(icoTabelle, 0, 3, 0, 1),
                     regenFlaechenDetailsPanel);
             viewMap.addView("Details (versiegelte Flaechen)", vDetailsRegen);
 
 //            vHistory = new View("Historie", Static2DTools.borderIcon(icoTabelle, 0, 3, 0, 1), historyPanel);
 //            viewMap.addView("Historie", vHistory);
 
             rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
 
             final DockingWindowsTheme theme = new ShapedGradientDockingTheme();
 
             rootWindow.getRootWindowProperties().addSuperObject(
                     theme.getRootWindowProperties());
 
             final RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
 
             rootWindow.getRootWindowProperties().addSuperObject(
                     titleBarStyleProperties);
 
             rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(true);
 
             final AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(
                     java.awt.SystemColor.inactiveCaptionText,
                     java.awt.SystemColor.activeCaptionText,
                     java.awt.SystemColor.activeCaptionText,
                     java.awt.SystemColor.inactiveCaptionText);
             // vMap.getViewProperties().getViewTitleBarProperties().getNormalProperties().getCloseButtonProperties().setVisible(true);
             rootWindow.getRootWindowProperties().getDragRectangleShapedPanelProperties().setComponentPainter(x);
             rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().getNormalProperties().getShapedPanelProperties().setComponentPainter(new GradientComponentPainter(
                     new Color(124, 160, 221),
                     new Color(236, 233, 216),
                     new Color(124, 160, 221),
                     new Color(236, 233, 216)));
 
             // Inserting Docking Window functionalty (Sebastian) 24.07.07
 
             // TODO UGLY PERHAPS CENTRAL HANDLer FOR THE CREATION OF CONFIGURATION
             final File verdisDir = new File(DIRECTORYPATH_VERDIS);
             if (!verdisDir.exists()) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Verdis Directory angelegt");
                 }
                 verdisDir.mkdir();
             }
 
             panMain.add(rootWindow);
             // doConfigKeystrokes();
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Crossover: starte server.");
             }
             initCrossoverServer(prefs.getVerdisCrossoverPort());
             isInit = false;
         } catch (Throwable t) {
             LOG.error("Fehler im Konstruktor", t);
         }
 
         if (context != null) {
             try {
                 this.cmdPutKassenzeichenToSearchTree.setEnabled(true);
                 if ((context != null) && (context.getEnvironment() != null)
                         && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setProgress(700, "verdis Plugin: Methoden initialisieren ...");
                 }
 
                 this.context.getMetadata().addMetaNodeSelectionListener(new NodeChangeListener());
                 userString = Sirius.navigator.connection.SessionManager.getSession().getUser().getName() + "@"
                         + Sirius.navigator.connection.SessionManager.getSession().getUser().getUserGroup().getName();
                 userGroup = Sirius.navigator.connection.SessionManager.getSession().getUser().getUserGroup().toString();
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("prefs: Vector index of " + (prefs.getRwGroups().indexOf(userGroup.toLowerCase()) >= 0));
                 }
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("prefs: userGroup " + userGroup.toLowerCase());
                 }
                 if (prefs.getRwGroups().indexOf(userGroup.toLowerCase()) >= 0) {
                     readonly = false;
                 } else {
                     readonly = true;
                 }
 //                kzPanel.setUserString(userString);
                 dokPanel.setAppletContext(context.getEnvironment().getAppletContext());
                 // java.lang.Runtime.getRuntime().addShutdownHook(hook)
                 if ((context != null) && (context.getEnvironment() != null)
                         && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setProgress(1000, "verdis Plugin fertig...");
                 }
                 if ((context != null) && (context.getEnvironment() != null)
                         && context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setFinished(true);
                 }
             } catch (Throwable t) {
                 LOG.error("Fehler im PluginKonstruktor", t);
             }
         } else {
             this.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/money_add.png")).getImage());
         }
 
         // TimEasy
         ((CreateGeometryListener) CidsAppBackend.getInstance().getMainMap().getInputListener(
                 MappingComponent.NEW_POLYGON)).setGeometryFeatureClass(TimEasyPureNewFeature.class);
 
         final MappingComponent mainMap = CidsAppBackend.getInstance().getMainMap();
         TimEasyDialog.addTimTimEasyListener(new TimEasyListener() {
 
             @Override
             public void timEasyObjectInserted(final TimEasyEvent tee) {
                 mainMap.getFeatureCollection().removeFeature(tee.getPureNewfeature());
                 mainMap.refresh();
             }
         });
         configurationManager.configure(mappingModel);
         mainMap.preparationSetMappingModel(mappingModel);
         ((NewSimpleInternalLayerWidget)mainMap.getInternalWidget(MappingComponent.LAYERWIDGET)).setMappingModel(mappingModel);
         configurationManager.configure(mainMap);
         mainMap.setMappingModel(mappingModel);
         kartenPanel.changeSelectedButtonAccordingToInteractionMode();
 
         mainMap.unlock();
 
         // CustomFeatureInfo
         final CustomFeatureInfoListener cfil = (CustomFeatureInfoListener) mainMap.getInputListener(
                 MappingComponent.CUSTOM_FEATUREINFO);
         cfil.setFeatureInforetrievalUrl(prefs.getAlbUrl());
 
         final File dotverdisDir = new File(DIRECTORYPATH_VERDIS);
         dotverdisDir.mkdir();
 
         FlaechenClipboardListener clipboardListener = new FlaechenClipboardListener() {
 
             @Override
             public void clipboardChanged() {
                 refreshClipboardButtons();
             }
         };
 
         flaechenClipboard = new FlaechenClipboard();
         flaechenClipboard.addListener(clipboardListener);
 
         flaechenClipboard.loadFromFile();
 
         if (flaechenClipboard.isPastable()) {
             JOptionPane.showMessageDialog(this.getComponent(),
                     "Der Inhalt der Zwischenablage steht Ihnen weiterhin zur Verf\u00FCgung.",
                     "Verdis wurde nicht ordnungsgem\u00E4\u00DF beendet.",
                     JOptionPane.INFORMATION_MESSAGE);
         }
 
         configurationManager.configure(this);
 
         // Piccolo Listener
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "coordinatesChanged",
                 SimpleMoveListener.COORDINATES_CHANGED,
                 getMappingComponent().getInputListener(MappingComponent.MOTION));
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "selectionChanged",
                 PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                 getMappingComponent().getInputListener(MappingComponent.SELECT));
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "selectionChanged",
                 FeatureMoveListener.SELECTION_CHANGED_NOTIFICATION,
                 getMappingComponent().getInputListener(MappingComponent.MOVE_POLYGON));
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "selectionChanged",
                 SplitPolygonListener.SELECTION_CHANGED,
                 getMappingComponent().getInputListener(MappingComponent.SPLIT_POLYGON));
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "featureDeleteRequested",
                 DeleteFeatureListener.FEATURE_DELETE_REQUEST_NOTIFICATION,
                 getMappingComponent().getInputListener(MappingComponent.REMOVE_POLYGON));
         PNotificationCenter.defaultCenter().addListener(
                 this,
                 "attachFeatureRequested",
                 AttachFeatureListener.ATTACH_FEATURE_NOTIFICATION,
                 getMappingComponent().getInputListener(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA));
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "splitPolygon",
                 SplitPolygonListener.SPLIT_FINISHED,
                 getMappingComponent().getInputListener(MappingComponent.SPLIT_POLYGON));
         PNotificationCenter.defaultCenter().addListener(
                 kartenPanel,
                 "joinPolygons",
                 JoinPolygonsListener.FEATURE_JOIN_REQUEST_NOTIFICATION,
                 getMappingComponent().getInputListener(MappingComponent.JOIN_POLYGONS));
         PNotificationCenter.defaultCenter()
                 .addListener(
                     kartenPanel,
                     "simpleGeometryCreated",
                     CreateGeometryListener.GEOMETRY_CREATED_NOTIFICATION,
                     getMappingComponent().getInputListener(MappingComponent.CREATE_SIMPLE_GEOMETRY));
 
         aggValidator.addListener(new ValidatorListener() {
 
             @Override
             public void stateChanged(final ValidatorState state) {
                 enableSave(!state.isError());
             }
         });
         aggValidator.add(kassenzeichenPanel.getValidator());
         aggValidator.add(regenFlaechenTabellenPanel.getValidator());
         aggValidator.add(wdsrFrontenTabellenPanel.getValidator());
         aggValidator.add(regenFlaechenDetailsPanel.getValidator());
         aggValidator.add(wdsrFrontenDetailsPanel.getValidator());
        
        setPostgisFlaechenSnappable(true);
     }
 
     public void attachFeatureRequested(final PNotification notification) {
         switch (currentMode) {
             case ESW: {
                 wdsrFrontenTabellenPanel.attachFeatureRequested(notification);
             }
             break;
             case REGEN: {
                 regenFlaechenTabellenPanel.attachFeatureRequested(notification);
             }
             break;
             case ALLGEMEIN: {
                 final Object o = notification.getObject();
                 if (o instanceof AttachFeatureListener) {
                     final AttachFeatureListener afl = (AttachFeatureListener) o;
                     final PFeature pf = afl.getFeatureToAttach();
                     if (pf.getFeature() instanceof PureNewFeature) {
                         setKZGeomFromFeature(pf.getFeature());
                     }
                 }
             }
             break;
         }
     }
 
     public RegenFlaechenTabellenPanel getRegenFlaechenTabellenPanel() {
         return regenFlaechenTabellenPanel;
     }
 
     //~ Methods ----------------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static Main getCurrentInstance() {
         return Main.THIS;
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      */
     private void setupDefaultLayout() {
         if (currentMode != null) {
             if (currentMode.equals(currentMode.ALLGEMEIN)) {
                 setupDefaultLayoutInfo();
             } else if (currentMode.equals(currentMode.ESW)) {
                 setupDefaultLayoutWDSR();
             } else {
                 setupDefaultLayoutRegen();
             }
         } else {
             CidsAppBackend.getInstance().setMode(CidsAppBackend.Mode.ALLGEMEIN);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     @Deprecated
     public void setupDefaultLayoutAll() {
         EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 rootWindow.setWindow(
                         new SplitWindow(
                         false,
                         0.4353147f,
                         new SplitWindow(
                         true,
                         0.24596775f,
                         vKassenzeichen,
                         new SplitWindow(
                         true,
                         0.67061925f,
                         new SplitWindow(true, 0.29148936f,
                         vSummen,
                         vKanaldaten),
                         vDokumente)),
                         new SplitWindow(
                         true,
                         0.68609864f,
                         new TabWindow(
                         new DockingWindow[]{
                             vKarte,
                             vTabelleWDSR,
                             vDetailsRegen,
                             vTabelleRegen,
                             vInfoAllgemein,
                             vZusammenfassungWDSR
 //                                                ,
 //                                        vHistory
                         }),
                         vDetailsWDSR)));
 
                 // log.debug("layout: "+flPanel.getCustomButtons());
                 // vFlaechen.getCustomTabComponents().addAll(flPanel.getCustomButtons());
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
             }
         });
     }
 
     private AbstractFeatureService getFleachenFeatureService() {
         for (final MapService mapService : mappingModel.getMapServices().values()) {
             if (checkForFlaechenFeatureService(mapService)) {
                 return (AbstractFeatureService) mapService;
             }
         }
         return null;
     }
 
     private static boolean checkForFlaechenFeatureService(final MapService mapService) {
         if (mapService instanceof SimplePostgisFeatureService) {
             final AbstractFeatureService featureService = (AbstractFeatureService) mapService;
             final String name = featureService.getName();
             if (name.equals("Versiegelte Flächen")) {
                 return true;
             }
         }
         return false;
     }
 
     private void setPostgisFlaechenSnappable(final boolean snappable) {
         final AbstractFeatureService featureService = getFleachenFeatureService();
         if (featureService != null) {
             setFeaturesSnappable(featureService, snappable);
         }
     }
 
     private static void setFeaturesSnappable(final MapService mapService, final boolean snappable) {
         final PNode pNode = mapService.getPNode();
         for (int index = 0; index < pNode.getChildrenCount(); index++) {
             final PNode child = pNode.getChild(index);
             if (child instanceof PFeature) {
                 final PFeature pFeature = (PFeature) child;
                 pFeature.setSnappable(snappable);
             }
         }
     }
 
     @Override
     public void appModeChanged() {
         //TODO : alter Kram abspeichern
         if (currentMode != null) {
             saveLayout(FILEPATH_LAYOUT + "." + currentMode.name());
         }
 
         final CidsAppBackend.Mode mode = CidsAppBackend.getInstance().getMode();
         if (mode.equals(mode.ALLGEMEIN)) {
             setupLayoutInfo();
         } else if (mode.equals(mode.ESW)) {
             setupLayoutWDSR();
         } else if (mode.equals(mode.REGEN)) {
             setupLayoutRegen();
         }
         currentMode = mode;
 
         refreshClipboardButtons();
         refreshItemButtons();
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setupLayoutRegen() {
         CidsAppBackend.Mode mode = CidsAppBackend.getInstance().getMode();
         String fileName = FILEPATH_LAYOUT + "." + mode.name();
         try {
             loadLayout(fileName);
         } catch (Exception e) {
             LOG.info("Problem beim Lesen des LayoutFiles " + fileName);
             setupDefaultLayoutRegen();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setupLayoutWDSR() {
         CidsAppBackend.Mode mode = CidsAppBackend.getInstance().getMode();
         String fileName = FILEPATH_LAYOUT + "." + mode.name();
         try {
             loadLayout(fileName);
         } catch (Exception e) {
             LOG.info("Problem beim Lesen des LayoutFiles " + fileName);
             setupDefaultLayoutWDSR();
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setupLayoutInfo() {
         CidsAppBackend.Mode mode = CidsAppBackend.getInstance().getMode();
         String fileName = FILEPATH_LAYOUT + "." + mode.name();
         try {
             loadLayout(fileName);
         } catch (Exception e) {
             LOG.info("Problem beim Lesen des LayoutFiles " + fileName);
             setupDefaultLayoutInfo();
         }
     }
 
     public void setupDefaultLayoutRegen() {
         EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 rootWindow.setWindow(
                         new SplitWindow(
                         false,
                         0.4353147f,
                         new SplitWindow(
                         true,
                         0.24596775f,
                         vKassenzeichen,
                         new SplitWindow(
                         true,
                         0.67061925f,
                         new SplitWindow(true, 0.29148936f,
                         vSummen,
                         vKanaldaten),
                         vDokumente)),
                         new SplitWindow(
                         true,
                         0.68609864f,
                         new TabWindow(
                         new DockingWindow[]{
                             vKarte,
                             vTabelleRegen
                         }),
                         vDetailsRegen)));
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
 //                    rootWindow.getWindowBar(Direction.RIGHT).addTab(vHistory);
             }
         });
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setupOldLayoutRegen() {
 //        EventQueue.invokeLater(new Runnable() {
 //
 //            @Override
 //            public void run() {
 //                rootWindow.setWindow(
 //                        new SplitWindow(
 //                        false,
 //                        0.4353147f,
 //                        new SplitWindow(
 //                        true,
 //                        0.24596775f,
 //                        vKassenzeichen,
 //                        new SplitWindow(
 //                        true,
 //                        0.67061925f,
 //                        new SplitWindow(true, 0.29148936f,
 //                        vSummen,
 //                        vKanaldaten),
 //                        vDokumente))
 //                        ));
 //                // log.debug("layout: "+flPanel.getCustomButtons());
 //
 //                rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
 //                rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
 ////                    rootWindow.getWindowBar(Direction.RIGHT).addTab(vHistory);
 //            }
 //        });
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setupDefaultLayoutWDSR() {
         EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 rootWindow.setWindow(
                         new SplitWindow(
                         false,
                         0.4353147f,
                         new SplitWindow(
                         true,
                         0.24596775f,
                         vKassenzeichen,
                         new SplitWindow(true, 0.30118892f,
                         vZusammenfassungWDSR,
                         vDokumente)),
                         new SplitWindow(
                         true,
                         0.66f,
                         new TabWindow(
                         new DockingWindow[]{
                             vKarte,
                             vTabelleWDSR
                         }),
                         vDetailsWDSR)));
 
                 // log.debug("layout: "+flPanel.getCustomButtons());
                 // vFlaechen.getCustomTabComponents().addAll(flPanel.getCustomButtons());
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
 //                    rootWindow.getWindowBar(Direction.RIGHT).addTab(vHistory);
             }
         });
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setupDefaultLayoutInfo() {
         EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 rootWindow.setWindow(
                         new SplitWindow(
                         false,
                         0.4353147f,
                         new SplitWindow(
                         true,
                         0.24596775f,
                         vKassenzeichen,
                         new SplitWindow(true, 0.51783353f,
                         vInfoAllgemein,
                         vDokumente)),
                         vKarte));
 
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
 //                    rootWindow.getWindowBar(Direction.RIGHT).addTab(vHistory);
             }
         });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  crossoverServerPort  DOCUMENT ME!
      */
     private void initCrossoverServer(final int crossoverServerPort) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Crossover: initCrossoverServer");
         }
         final int defaultServerPort = 8888;
         boolean defaultServerPortUsed = false;
         try {
             if ((crossoverServerPort < 0) || (crossoverServerPort > 65535)) {
                 LOG.warn("Crossover: Invalid Crossover serverport: " + crossoverServerPort
                         + ". Going to use default port: " + defaultServerPort);
                 defaultServerPortUsed = true;
                 initCrossoverServerImpl(defaultServerPort);
             } else {
                 initCrossoverServerImpl(crossoverServerPort);
             }
         } catch (Exception ex) {
             LOG.error("Crossover: Error while creating crossover server on port: " + crossoverServerPort, ex);
             if (!defaultServerPortUsed) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Crossover: Trying to create server with defaultPort: " + defaultServerPort);
                 }
                 defaultServerPortUsed = true;
                 try {
                     initCrossoverServerImpl(defaultServerPort);
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Crossover: Server started at port: " + defaultServerPort, ex);
                     }
                 } catch (Exception ex1) {
                     LOG.error("Crossover: Failed to initialize Crossover server on defaultport: " + defaultServerPort
                             + ". No Server is started",
                             ex);
                     cmdLagisCrossover.setEnabled(false);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   crossoverServerPort  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     private void initCrossoverServerImpl(final int crossoverServerPort) throws Exception {
         final HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, VerdisCrossover.class);
         final HttpServer server = HttpServer.create(new InetSocketAddress(crossoverServerPort), 0);
         server.createContext("/", handler);
         server.setExecutor(null);
         server.start();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void loadProperties() {
         // Read properties file.
         prefs = new AppPreferences(getClass().getResourceAsStream("/verdis2properties.xml"));
         if (LOG.isDebugEnabled()) {
             LOG.debug(getClass().getClassLoader());
         }
         KASSENZEICHEN_CLASS_ID = prefs.getKassenzeichenClassId();
         GEOM_CLASS_ID = prefs.getGeomClassId();
         DMS_URL_BASE_ID = prefs.getDmsUrlBaseClassId();
         DMS_URL_ID = prefs.getDmsUrlClassId();
 //        INITIAL_WMS_BB_X1 = prefs.getCismapPrefs().getGlobalPrefs().getInitialBoundingBox().getX1();
 //        INITIAL_WMS_BB_Y1 = prefs.getCismapPrefs().getGlobalPrefs().getInitialBoundingBox().getY1();
 //        INITIAL_WMS_BB_X2 = prefs.getCismapPrefs().getGlobalPrefs().getInitialBoundingBox().getX2();
 //        INITIAL_WMS_BB_Y2 = prefs.getCismapPrefs().getGlobalPrefs().getInitialBoundingBox().getY2();
 
         connectionInfo = prefs.getDbConnectionInfo();
         if (!plugin) {
             if (prefs.getMode().trim().toLowerCase().equals("readonly")) {
                 readonly = true;
             } else {
                 readonly = false;
             }
         }
     }
 
     @Override
     public void setEnabled(final boolean b) {
         kassenzeichenPanel.setEnabled(b);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void refreshLeftTitleBarColor() {
         if (editmode) {
             setLeftTitleBarColor(Color.red);
         } else {
             if (kassenzeichenPanel.isLocked()) {
                 setLeftTitleBarColor(Color.orange);
             } else {
                 setLeftTitleBarColor(myBlue);
             }
         }
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07 former all components are signaled to change the color
      * Now the docking framework will do that.
      *
      * @param  c  DOCUMENT ME!
      */
     public void setLeftTitleBarColor(final Color c) {
         if (!isInit) {
             rootWindow.getRootWindowProperties().getViewProperties().getViewTitleBarProperties().getNormalProperties().getShapedPanelProperties().setComponentPainter(new GradientComponentPainter(
                     c,
                     new Color(236, 233, 216),
                     c,
                     new Color(236, 233, 216)));
         }
 //        sumPanel.setLeftTitlebarColor(c);
 //        kanaldatenPanel.setLeftTitlebarColor(c);
 //        dokPanel.setLeftTitlebarColor(c);
 //        flPanel.setLeftTitlebarColor(c);
 //        kzPanel.setLeftTitlebarColor(c);
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         cmdTest = new javax.swing.JButton();
         cmdTest2 = new javax.swing.JButton();
         tobVerdis = new javax.swing.JToolBar();
         cmdPutKassenzeichenToSearchTree = new javax.swing.JButton();
         jSeparator5 = new javax.swing.JSeparator();
         cmdEditMode = new javax.swing.JButton();
         cmdCancel = new javax.swing.JButton();
         cmdOk = new javax.swing.JButton();
         cmdDeleteKassenzeichen = new javax.swing.JButton();
         cmdNewKassenzeichen = new javax.swing.JButton();
         jSeparator6 = new javax.swing.JSeparator();
         cmdCutFlaeche = new javax.swing.JButton();
         cmdCopyFlaeche = new javax.swing.JButton();
         cmdPasteFlaeche = new javax.swing.JButton();
         jSeparator4 = new javax.swing.JSeparator();
         cmdRefreshEnumeration = new javax.swing.JButton();
         jSeparator12 = new javax.swing.JToolBar.Separator();
         jSeparator7 = new javax.swing.JSeparator();
         cmdAdd = new javax.swing.JButton();
         cmdRemove = new javax.swing.JButton();
         cmdUndo = new javax.swing.JButton();
         jSeparator13 = new javax.swing.JToolBar.Separator();
         cmdPdf = new javax.swing.JButton();
         cmdWorkflow = new javax.swing.JButton();
         jSeparator3 = new javax.swing.JSeparator();
         cmdInfo = new javax.swing.JButton();
         jSeparator8 = new javax.swing.JSeparator();
         cmdLagisCrossover = new javax.swing.JButton();
         btnHistory = new javax.swing.JButton();
         cmdDownloads = new javax.swing.JButton();
         cmdFortfuehrung = new javax.swing.JButton();
         panMain = new javax.swing.JPanel();
         jMenuBar1 = new javax.swing.JMenuBar();
         menFile = new javax.swing.JMenu();
         jSeparator9 = new javax.swing.JSeparator();
         mniSaveLayout = new javax.swing.JMenuItem();
         mniLoadLayout = new javax.swing.JMenuItem();
         jSeparator10 = new javax.swing.JSeparator();
         mniClose = new javax.swing.JMenuItem();
         menEdit = new javax.swing.JMenu();
         mnuEditMode = new javax.swing.JMenuItem();
         mnuNewKassenzeichen = new javax.swing.JMenuItem();
         mnuRenameKZ = new javax.swing.JMenuItem();
         menExtras = new javax.swing.JMenu();
         mniOptions = new javax.swing.JMenuItem();
         sepOptions = new javax.swing.JPopupMenu.Separator();
         mnuChangeUser = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         menWindows = new javax.swing.JMenu();
         mniKassenzeichen = new javax.swing.JMenuItem();
         mniSummen = new javax.swing.JMenuItem();
         mniKanalanschluss = new javax.swing.JMenuItem();
         mniDokumente = new javax.swing.JMenuItem();
         mniFlaechen = new javax.swing.JMenuItem();
         jSeparator11 = new javax.swing.JSeparator();
         mniKarte = new javax.swing.JMenuItem();
         mniTabelle = new javax.swing.JMenuItem();
         mniDetails = new javax.swing.JMenuItem();
         mniResetWindowLayout = new javax.swing.JMenuItem();
         menHelp = new javax.swing.JMenu();
         mnuHelp = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         mnuInfo = new javax.swing.JMenuItem();
 
         cmdTest.setText("Test ClipboardStore");
         cmdTest.setFocusable(false);
         cmdTest.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdTest.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdTest.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdTestActionPerformed(evt);
             }
         });
 
         cmdTest2.setText("Test Clipboard Load");
         cmdTest2.setFocusable(false);
         cmdTest2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdTest2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdTest2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdTest2ActionPerformed(evt);
             }
         });
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(getTitle());
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosed(java.awt.event.WindowEvent evt) {
                 formWindowClosed(evt);
             }
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
             public void windowOpened(java.awt.event.WindowEvent evt) {
                 formWindowOpened(evt);
             }
         });
         addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 formKeyPressed(evt);
             }
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 formKeyReleased(evt);
             }
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 formKeyTyped(evt);
             }
         });
 
         tobVerdis.setRollover(true);
         tobVerdis.setAlignmentY(0.48387095F);
         tobVerdis.setMaximumSize(new java.awt.Dimension(679, 32769));
         tobVerdis.setMinimumSize(new java.awt.Dimension(667, 33));
         tobVerdis.setPreferredSize(new java.awt.Dimension(691, 35));
 
         cmdPutKassenzeichenToSearchTree.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/show_kassenzeichen_in_search_tree.png"))); // NOI18N
         cmdPutKassenzeichenToSearchTree.setToolTipText("Zeige Kassenzeichen im Navigator");
         cmdPutKassenzeichenToSearchTree.setEnabled(false);
         cmdPutKassenzeichenToSearchTree.setFocusPainted(false);
         cmdPutKassenzeichenToSearchTree.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPutKassenzeichenToSearchTreeActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdPutKassenzeichenToSearchTree);
 
         jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator5.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator5);
 
         cmdEditMode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/editmode.png"))); // NOI18N
         cmdEditMode.setToolTipText("Editormodus");
         cmdEditMode.setEnabled(false);
         cmdEditMode.setFocusPainted(false);
         cmdEditMode.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdEditModeActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdEditMode);
 
         cmdCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/cancel.png"))); // NOI18N
         cmdCancel.setToolTipText("Änderungen abbrechen");
         cmdCancel.setFocusPainted(false);
         cmdCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdCancelActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdCancel);
 
         cmdOk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/ok.png"))); // NOI18N
         cmdOk.setToolTipText("Änderungen annehmen");
         cmdOk.setFocusPainted(false);
         cmdOk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdOkActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdOk);
 
         cmdDeleteKassenzeichen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/deleteKassenzeichen.png"))); // NOI18N
         cmdDeleteKassenzeichen.setToolTipText("Kassenzeichen löschen");
         cmdDeleteKassenzeichen.setEnabled(false);
         cmdDeleteKassenzeichen.setFocusPainted(false);
         cmdDeleteKassenzeichen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdDeleteKassenzeichenActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdDeleteKassenzeichen);
 
         cmdNewKassenzeichen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/newKassenzeichen.png"))); // NOI18N
         cmdNewKassenzeichen.setToolTipText("Neues Kassenzeichen");
         cmdNewKassenzeichen.setFocusPainted(false);
         cmdNewKassenzeichen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNewKassenzeichenActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdNewKassenzeichen);
 
         jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator6.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator6);
 
         cmdCutFlaeche.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/cutFl.png"))); // NOI18N
         cmdCutFlaeche.setToolTipText("Fläche ausschneiden");
         cmdCutFlaeche.setEnabled(false);
         cmdCutFlaeche.setFocusPainted(false);
         cmdCutFlaeche.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdCutFlaecheActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdCutFlaeche);
 
         cmdCopyFlaeche.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/copyFl.png"))); // NOI18N
         cmdCopyFlaeche.setToolTipText("Fläche kopieren (Teileigentum erzeugen)");
         cmdCopyFlaeche.setEnabled(false);
         cmdCopyFlaeche.setFocusPainted(false);
         cmdCopyFlaeche.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdCopyFlaecheActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdCopyFlaeche);
 
         cmdPasteFlaeche.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/pasteFl.png"))); // NOI18N
         cmdPasteFlaeche.setToolTipText("Fläche einfügen");
         cmdPasteFlaeche.setEnabled(false);
         cmdPasteFlaeche.setFocusPainted(false);
         cmdPasteFlaeche.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPasteFlaecheActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdPasteFlaeche);
 
         jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator4.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator4);
 
         cmdRefreshEnumeration.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/refreshEnum.png"))); // NOI18N
         cmdRefreshEnumeration.setToolTipText("Alle Flächen neu nummerieren");
         cmdRefreshEnumeration.setEnabled(false);
         cmdRefreshEnumeration.setFocusPainted(false);
         cmdRefreshEnumeration.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRefreshEnumerationActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRefreshEnumeration);
         tobVerdis.add(jSeparator12);
 
         jSeparator7.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator7.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator7);
 
         cmdAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/add.png"))); // NOI18N
         cmdAdd.setEnabled(false);
         cmdAdd.setFocusable(false);
         cmdAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdAdd.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/add2.png"))); // NOI18N
         cmdAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdAddActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdAdd);
 
         cmdRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/remove.png"))); // NOI18N
         cmdRemove.setEnabled(false);
         cmdRemove.setFocusable(false);
         cmdRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdRemove.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/remove2.png"))); // NOI18N
         cmdRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRemoveActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdRemove);
 
         cmdUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/undo.png"))); // NOI18N
         cmdUndo.setEnabled(false);
         cmdUndo.setFocusable(false);
         cmdUndo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdUndo.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/undo2.png"))); // NOI18N
         cmdUndo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdUndo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdUndoActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdUndo);
         tobVerdis.add(jSeparator13);
 
         cmdPdf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/pdf.png"))); // NOI18N
         cmdPdf.setToolTipText("Drucken");
         cmdPdf.setFocusPainted(false);
         cmdPdf.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPdfActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdPdf);
 
         cmdWorkflow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/workflow.png"))); // NOI18N
         cmdWorkflow.setToolTipText("Workflow");
         cmdWorkflow.setEnabled(false);
         cmdWorkflow.setFocusPainted(false);
         cmdWorkflow.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdWorkflowActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdWorkflow);
 
         jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator3.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator3);
 
         cmdInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/info.png"))); // NOI18N
         cmdInfo.setToolTipText("Versionsanzeige");
         cmdInfo.setFocusPainted(false);
         cmdInfo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdInfoActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdInfo);
 
         jSeparator8.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator8.setMaximumSize(new java.awt.Dimension(2, 32767));
         tobVerdis.add(jSeparator8);
 
         cmdLagisCrossover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/lagisCrossover.png"))); // NOI18N
         cmdLagisCrossover.setToolTipText("Öffne zugehöriges Flurstück in LagIS");
         cmdLagisCrossover.setFocusPainted(false);
         cmdLagisCrossover.setFocusable(false);
         cmdLagisCrossover.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdLagisCrossover.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdLagisCrossover.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdLagisCrossoverActionPerformed(evt);
             }
         });
         tobVerdis.add(cmdLagisCrossover);
 
         btnHistory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/history.png"))); // NOI18N
         btnHistory.setToolTipText("öffne Kassenzeichen-Verlauf");
         btnHistory.setFocusPainted(false);
         btnHistory.setFocusable(false);
         btnHistory.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnHistory.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnHistory.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnHistoryActionPerformed(evt);
             }
         });
         tobVerdis.add(btnHistory);
 
         cmdDownloads.setAction(new DownloadManagerAction(this));
         cmdDownloads.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/toolbar/download.png"))); // NOI18N
         cmdDownloads.setFocusPainted(false);
         cmdDownloads.setFocusable(false);
         cmdDownloads.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdDownloads.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         tobVerdis.add(cmdDownloads);
 
         cmdFortfuehrung.setText("Fortführung");
         cmdFortfuehrung.setFocusable(false);
         cmdFortfuehrung.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         cmdFortfuehrung.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         cmdFortfuehrung.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdFortfuehrungActionPerformed(evt);
             }
         });
         if (StaticDebuggingTools.checkHomeForFile("cismetVerdisFortfuehrungOn")) {                     // NOI18N
             cmdFortfuehrung.setVisible(true);
         } else {
             cmdFortfuehrung.setVisible(false);
         }
         tobVerdis.add(cmdFortfuehrung);
 
         getContentPane().add(tobVerdis, java.awt.BorderLayout.NORTH);
 
         panMain.setLayout(new java.awt.BorderLayout());
         getContentPane().add(panMain, java.awt.BorderLayout.CENTER);
 
         menFile.setMnemonic('D');
         menFile.setText("Datei");
         menFile.add(jSeparator9);
 
         mniSaveLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         mniSaveLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/layout.png"))); // NOI18N
         mniSaveLayout.setText("Aktuelles Layout speichern");
         mniSaveLayout.setEnabled(false);
         mniSaveLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniSaveLayoutActionPerformed(evt);
             }
         });
         menFile.add(mniSaveLayout);
 
         mniLoadLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         mniLoadLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/layout.png"))); // NOI18N
         mniLoadLayout.setText("Layout laden");
         mniLoadLayout.setEnabled(false);
         mniLoadLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLoadLayoutActionPerformed(evt);
             }
         });
         menFile.add(mniLoadLayout);
         menFile.add(jSeparator10);
 
         mniClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
         mniClose.setText("Beenden");
         mniClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuExitActionPerformed(evt);
             }
         });
         menFile.add(mniClose);
 
         jMenuBar1.add(menFile);
 
         menEdit.setText("Bearbeiten");
 
         mnuEditMode.setText("In den Editormodus wechseln");
         mnuEditMode.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuEditModeActionPerformed(evt);
             }
         });
         menEdit.add(mnuEditMode);
 
         mnuNewKassenzeichen.setText("Neues Kassenzeichen");
         mnuNewKassenzeichen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuNewKassenzeichenActionPerformed(evt);
             }
         });
         menEdit.add(mnuNewKassenzeichen);
 
         mnuRenameKZ.setText("Kassenzeichen umbenennen");
         mnuRenameKZ.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuRenameKZActionPerformed(evt);
             }
         });
         menEdit.add(mnuRenameKZ);
 
         jMenuBar1.add(menEdit);
 
         menExtras.setMnemonic('E');
         menExtras.setText("Extras");
 
         mniOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/tooloptions.png"))); // NOI18N
         mniOptions.setText("Optionen");
         mniOptions.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniOptionsActionPerformed(evt);
             }
         });
         menExtras.add(mniOptions);
         menExtras.add(sepOptions);
 
         mnuChangeUser.setText("User wechseln");
         mnuChangeUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuChangeUserActionPerformed(evt);
             }
         });
         menExtras.add(mnuChangeUser);
 
         jMenuItem1.setText("WindowManagementTool");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         menExtras.add(jMenuItem1);
 
         jMenuBar1.add(menExtras);
 
         menWindows.setMnemonic('F');
         menWindows.setText("Fenster");
         menWindows.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menWindowsActionPerformed(evt);
             }
         });
 
         mniKassenzeichen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
         mniKassenzeichen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/kassenzeichen.png"))); // NOI18N
         mniKassenzeichen.setMnemonic('L');
         mniKassenzeichen.setText("Kassenzeichen");
         mniKassenzeichen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniKassenzeichenActionPerformed(evt);
             }
         });
         menWindows.add(mniKassenzeichen);
 
         mniSummen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
         mniSummen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/sum.png"))); // NOI18N
         mniSummen.setMnemonic('C');
         mniSummen.setText("Summen");
         mniSummen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniSummenActionPerformed(evt);
             }
         });
         menWindows.add(mniSummen);
 
         mniKanalanschluss.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
         mniKanalanschluss.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/pipe.png"))); // NOI18N
         mniKanalanschluss.setMnemonic('F');
         mniKanalanschluss.setText("Kanalanschluss");
         mniKanalanschluss.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniKanalanschlussActionPerformed(evt);
             }
         });
         menWindows.add(mniKanalanschluss);
 
         mniDokumente.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
         mniDokumente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/docs.png"))); // NOI18N
         mniDokumente.setMnemonic('a');
         mniDokumente.setText("Dokumente");
         mniDokumente.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniDokumenteActionPerformed(evt);
             }
         });
         menWindows.add(mniDokumente);
 
         mniFlaechen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, java.awt.event.InputEvent.CTRL_MASK));
         mniFlaechen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/flaechen.png"))); // NOI18N
         mniFlaechen.setMnemonic('S');
         mniFlaechen.setText("Flächen");
         mniFlaechen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniFlaechenActionPerformed(evt);
             }
         });
         menWindows.add(mniFlaechen);
         menWindows.add(jSeparator11);
 
         mniKarte.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_6, java.awt.event.InputEvent.CTRL_MASK));
         mniKarte.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/flaechen.png"))); // NOI18N
         mniKarte.setMnemonic('S');
         mniKarte.setText("Karte");
         mniKarte.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniKarteActionPerformed(evt);
             }
         });
         menWindows.add(mniKarte);
 
         mniTabelle.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_7, java.awt.event.InputEvent.CTRL_MASK));
         mniTabelle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/flaechen.png"))); // NOI18N
         mniTabelle.setMnemonic('T');
         mniTabelle.setText("Tabellenansicht");
         mniTabelle.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniTabelleActionPerformed(evt);
             }
         });
         menWindows.add(mniTabelle);
 
         mniDetails.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_8, java.awt.event.InputEvent.CTRL_MASK));
         mniDetails.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/flaechen.png"))); // NOI18N
         mniDetails.setMnemonic('D');
         mniDetails.setText("Details");
         mniDetails.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniDetailsActionPerformed(evt);
             }
         });
         menWindows.add(mniDetails);
 
         mniResetWindowLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
         mniResetWindowLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/verdis/res/images/titlebars/layout.png"))); // NOI18N
         mniResetWindowLayout.setText("Fensteranordnung zurücksetzen");
         mniResetWindowLayout.setToolTipText("Standard Fensteranordnung wiederherstellen");
         mniResetWindowLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniResetWindowLayoutActionPerformed(evt);
             }
         });
         menWindows.add(mniResetWindowLayout);
 
         jMenuBar1.add(menWindows);
 
         menHelp.setMnemonic('E');
         menHelp.setText("?");
 
         mnuHelp.setMnemonic('H');
         mnuHelp.setText("Hilfe");
         menHelp.add(mnuHelp);
         menHelp.add(jSeparator1);
 
         mnuInfo.setMnemonic('I');
         mnuInfo.setText("Info");
         mnuInfo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuInfoActionPerformed(evt);
             }
         });
         menHelp.add(mnuInfo);
 
         jMenuBar1.add(menHelp);
 
         setJMenuBar(jMenuBar1);
 
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         setBounds((screenSize.width-1024)/2, (screenSize.height-868)/2, 1024, 868);
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  evt  DOCUMENT ME!
      */
     private void menWindowsActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menWindowsActionPerformed
 // TODO add your handling code here:
     }//GEN-LAST:event_menWindowsActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniResetWindowLayoutActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniResetWindowLayoutActionPerformed
         setupDefaultLayout();
     }//GEN-LAST:event_mniResetWindowLayoutActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniFlaechenActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniFlaechenActionPerformed
     }//GEN-LAST:event_mniFlaechenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniDokumenteActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniDokumenteActionPerformed
         showOrHideView(vDokumente);
     }//GEN-LAST:event_mniDokumenteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniKanalanschlussActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniKanalanschlussActionPerformed
         showOrHideView(vKanaldaten);
     }//GEN-LAST:event_mniKanalanschlussActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSummenActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSummenActionPerformed
         showOrHideView(vSummen);
     }//GEN-LAST:event_mniSummenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniKassenzeichenActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniKassenzeichenActionPerformed
         showOrHideView(vKassenzeichen);
     }//GEN-LAST:event_mniKassenzeichenActionPerformed
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLoadLayoutActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadLayoutActionPerformed
 //        final JFileChooser fc = new JFileChooser(verdisDirectory);
 //        fc.setFileFilter(new FileFilter() {
 //
 //            @Override
 //            public boolean accept(final File f) {
 //                return f.getName().toLowerCase().endsWith(".layout");
 //            }
 //
 //            @Override
 //            public String getDescription() {
 //                return "Layout";
 //            }
 //        });
 //        fc.setMultiSelectionEnabled(false);
 //        final int state = fc.showOpenDialog(this);
 //        if (state == JFileChooser.APPROVE_OPTION) {
 //            final File file = fc.getSelectedFile();
 //            String name = file.getAbsolutePath();
 //            name = name.toLowerCase();
 //            if (name.endsWith(".layout")) {
 //                loadLayout(name);
 //            } else {
 //                JOptionPane.showMessageDialog(
 //                        this,
 //                        java.util.ResourceBundle.getBundle("de/cismet/verdis/res/i18n/Bundle").getString(
 //                        "CismapPlugin.InfoNode.format_failure_message"),
 //                        java.util.ResourceBundle.getBundle("de/cismet/verdis/res/i18n/Bundle").getString(
 //                        "CismapPlugin.InfoNode.message_title"),
 //                        JOptionPane.INFORMATION_MESSAGE);
 //            }
 //        }
     }//GEN-LAST:event_mniLoadLayoutActionPerformed
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSaveLayoutActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveLayoutActionPerformed
         final JFileChooser fc = new JFileChooser(DIRECTORYPATH_VERDIS);
         fc.setFileFilter(new FileFilter() {
 
             @Override
             public boolean accept(final File f) {
                 return f.getName().toLowerCase().endsWith(".layout");
             }
 
             @Override
             public String getDescription() {
                 return "Layout";
             }
         });
         fc.setMultiSelectionEnabled(false);
         final int state = fc.showSaveDialog(this);
         if (LOG.isDebugEnabled()) {
             LOG.debug("state:" + state);
         }
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             if (LOG.isDebugEnabled()) {
                 LOG.debug("file:" + file);
             }
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".layout")) {
                 saveLayout(name);
             } else {
                 saveLayout(name + ".layout");
             }
         }
     }//GEN-LAST:event_mniSaveLayoutActionPerformed
 
     /**
      * TODO Bundle Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  file  DOCUMENT ME!
      */
     public void loadLayout(final String file) throws Exception {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Load Layout.. from " + file);
         }
         final File layoutFile = new File(file);
 
         final FileInputStream layoutInput = new FileInputStream(layoutFile);
         final ObjectInputStream in = new ObjectInputStream(layoutInput);
         rootWindow.read(in);
         in.close();
         rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
         rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
         if (isInit) {
             final int count = viewMap.getViewCount();
             for (int i = 0; i < count; i++) {
                 final View current = viewMap.getViewAtIndex(i);
                 if ((current != null) && current.isUndocked()) {
                     current.dock();
                 }
             }
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("Loading Layout successfull");
         }
     }
     // Inserting Docking Window functionalty (Sebastian) 24.07.07
 
     /**
      * DOCUMENT ME!
      *
      * @param  file  DOCUMENT ME!
      */
     public void saveLayout(final String file) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Saving Layout.. to " + file);
         }
         final File layoutFile = new File(file);
         try {
             if (!layoutFile.exists()) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Saving Layout.. File does not exit");
                 }
                 final File verdisDir = new File(DIRECTORYPATH_VERDIS);
                 if (!verdisDir.exists()) {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Verdis Directory angelegt");
                     }
                     verdisDir.mkdir();
                 }
                 layoutFile.createNewFile();
             } else {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Saving Layout.. File does exit");
                 }
             }
             final FileOutputStream layoutOutput = new FileOutputStream(layoutFile);
             final ObjectOutputStream out = new ObjectOutputStream(layoutOutput);
             setLeftTitleBarColor(myBlue);
             rootWindow.write(out);
             out.flush();
             out.close();
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Saving Layout.. to " + file + " successfull");
             }
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(
                     this,
                     java.util.ResourceBundle.getBundle("de/cismet/verdis/res/i18n/Bundle").getString(
                     "CismapPlugin.InfoNode.saving_layout_failure"),
                     java.util.ResourceBundle.getBundle("de/cismet/verdis/res/i18n/Bundle").getString(
                     "CismapPlugin.InfoNode.message_title"),
                     JOptionPane.INFORMATION_MESSAGE);
             LOG.error("A failure occured during writing the layout file", ex);
         }
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  v  DOCUMENT ME!
      */
     private void showOrHideView(final View v) {
         ///irgendwas besser als Closable ??
         // Problem wenn floating --> close -> open  (muss zweimal open)
 
         try {
             if (v.isClosable()) {
                 v.close();
             } else {
                 v.restore();
             }
         } catch (Exception e) {
             LOG.error("problem during hide or view", e);
         }
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  icoFlaeche  DOCUMENT ME!
      */
     public void setFlaechenPanelIcon(final Icon icoFlaeche) {
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  letzteAenderung  DOCUMENT ME!
      */
     public void setLetzteAenderungTooltip(final String letzteAenderung) {
         if (!isInit) {
             // ((ImageIcon)vKassenzeichen.getIcon()).setDescription()
             // vKassenzeichen.get
             // vKassenzeichen.getViewProperties().getViewTitleBarProperties().getNormalProperties().
         }
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  icoSumme  DOCUMENT ME!
      */
     public void setSummenPanelIcon(final Icon icoSumme) {
         if (!isInit) {
             vSummen.getViewProperties().setIcon(icoSumme);
         }
     }
 
     /**
      * Inserting Docking Window functionalty (Sebastian) 24.07.07.
      *
      * @param  foreground  DOCUMENT ME!
      */
     public void setKanalTitleForeground(final Color foreground) {
         if (!isInit) {
             vKanaldaten.getViewProperties().getViewTitleBarProperties().getNormalProperties().getComponentProperties().setForegroundColor(foreground);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuChangeUserActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChangeUserActionPerformed
         formWindowOpened(null);
     }//GEN-LAST:event_mnuChangeUserActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuNewKassenzeichenActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewKassenzeichenActionPerformed
         cmdNewKassenzeichenActionPerformed(null);
     }//GEN-LAST:event_mnuNewKassenzeichenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuEditModeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuEditModeActionPerformed
         cmdEditModeActionPerformed(null);
     }//GEN-LAST:event_mnuEditModeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuExitActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
         dispose();
     }//GEN-LAST:event_mnuExitActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formWindowOpened(final java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
     }//GEN-LAST:event_formWindowOpened
 
     private void login() {
         if (noLoginDuringDev || plugin) {
             return;
         } else {
 
             final DefaultUserNameStore usernames = new DefaultUserNameStore();
             final Preferences appPrefs = Preferences.userNodeForPackage(this.getClass());
             usernames.setPreferences(appPrefs.node("login"));
             final CidsAuthentification cidsAuth = new CidsAuthentification();
             final JXLoginPane login = new JXLoginPane(cidsAuth, null, usernames) {
 
                 @Override
                 protected Image createLoginBanner() {
                     return getBannerImage();
                 }
             };
 
             String u = null;
             try {
                 u = usernames.getUserNames()[usernames.getUserNames().length - 1];
             } catch (Exception skip) {
             }
             if (u != null) {
                 login.setUserName(u);
             }
 
 
             JFrame parent = null;
             if (SPLASH == null) {
                 parent = Main.this;
             } else {
                 parent = SPLASH;
             }
 
             final JXLoginPane.JXLoginDialog d = new JXLoginPane.JXLoginDialog(parent, login);
 
 
             login.setPassword("".toCharArray());
             if (SPLASH != null) {
                 double x = SPLASH.getBounds().getCenterX();
                 double y = SPLASH.getBounds().getCenterY();
                 double dWidth = d.getWidth();
                 double dHeight = d.getHeight();
 
                 d.setLocation((int) (x - (dWidth / 2)), (int) (y - (dHeight / 2)));
             } else {
                 LOG.fatal("splash was null");
                 d.setLocationRelativeTo(parent);
             }
 
             try {
                 ((JXPanel) ((JXPanel) login.getComponent(1)).getComponent(1)).getComponent(3).requestFocus();
             } catch (Exception skip) {
             }
             d.setVisible(true);
 
             handleLoginStatus(d.getStatus(), usernames, login);
 
 
 
 
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  status     DOCUMENT ME!
      * @param  usernames  DOCUMENT ME!
      * @param  login      DOCUMENT ME!
      */
     private void handleLoginStatus(final JXLoginPane.Status status,
             final DefaultUserNameStore usernames,
             final JXLoginPane login) {
         if (status == JXLoginPane.Status.SUCCEEDED) {
             // Damit wird sichergestellt, dass dieser als erstes vorgeschlagen wird
             usernames.removeUserName(login.getUserName());
             usernames.saveUserNames();
             usernames.addUserName((login.getUserName()));
             usernames.saveUserNames();
             setLoggedIn(true);
             if (LOG.isDebugEnabled()) {
                 // Added for RM Plugin functionalty 22.07.2007 Sebastian Puhl
                 LOG.debug("Login erfolgreich");
             }
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Login fehlgeschlagen");
             }
             System.exit(0);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdRefreshEnumerationActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRefreshEnumerationActionPerformed
         regenFlaechenTabellenPanel.reEnumerateFlaechen();
     }//GEN-LAST:event_cmdRefreshEnumerationActionPerformed
 
     private void reEnumerateFlaechen() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdPdfActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPdfActionPerformed
         if (kassenzeichenBean != null) {
             final Integer kassenzeichenOld = (Integer) kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER_OLD);
             if (kassenzeichenOld != null) {
                 try {
                     final String gotoUrl = prefs.getReportUrl() + kassenzeichenOld;
                     AppletContext appletContext = null;
                     try {
                         appletContext = context.getEnvironment().getAppletContext();
                     } catch (Exception npe) {
                         // nothing to do
                     }
                     if (appletContext == null) {
                         de.cismet.tools.BrowserLauncher.openURL(gotoUrl);
                     } else {
                         final java.net.URL u = new java.net.URL(gotoUrl);
                         appletContext.showDocument(u, "verdisReportFrame");
                     }
                 } catch (Exception e) {
                     JOptionPane.showMessageDialog(
                             this,
                             "Fehler beim Anzeigen des VERDIS-Reports",
                             "Fehler",
                             JOptionPane.ERROR_MESSAGE);
                     LOG.error("Fehler beim Anzeigen des VERDIS-Reports", e);
                 }
             }
         }
     }//GEN-LAST:event_cmdPdfActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdDeleteKassenzeichenActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteKassenzeichenActionPerformed
         deleteKZ();
     }//GEN-LAST:event_cmdDeleteKassenzeichenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdPasteFlaecheActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPasteFlaecheActionPerformed
         if (flaechenClipboard != null) {
             flaechenClipboard.storeToFile();
             flaechenClipboard.paste();
         }
     }//GEN-LAST:event_cmdPasteFlaecheActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdCutFlaecheActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCutFlaecheActionPerformed
         if (flaechenClipboard != null) {
             flaechenClipboard.cut();
         }
     }//GEN-LAST:event_cmdCutFlaecheActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdInfoActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdInfoActionPerformed
 //        String info="Verdis Plugin\n"
 //                + "cismet GmbH\n\n"
 //                + de.cismet.verdis.Version.getVersion()+"\n"
 //                + de.cismet.cismap.commons.Version.getVersion();
 //        JOptionPane.showMessageDialog(this,info,"Info",JOptionPane.INFORMATION_MESSAGE);
 
         if (about == null) {
             final JDialog d = new JDialog(this, "Info");
             d.setLayout(new BorderLayout());
 
             // JLabel infoLabel=new JLabel(de.cismet.verdis.Version.getVersion()+"\n"+
             // de.cismet.cismap.commons.Version.getVersion());
 
             // d.add(infoLabel,BorderLayout.SOUTH);
 
             final JLabel image = new JLabel(new ImageIcon(getBannerImage()));
             d.add(image, BorderLayout.CENTER);
             final JLabel version = new JLabel(de.cismet.verdis.Version.getVersion());
 
             d.add(version, BorderLayout.SOUTH);
             d.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
             d.pack();
             about = d;
         }
         about.setLocationRelativeTo(this);
         about.setVisible(true);
     }//GEN-LAST:event_cmdInfoActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuInfoActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuInfoActionPerformed
         cmdInfoActionPerformed(null);
     }//GEN-LAST:event_mnuInfoActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formKeyReleased(final java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
         // TODO add your handling code here:
     }//GEN-LAST:event_formKeyReleased
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formKeyPressed(final java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
 
         if ((evt.getKeyCode() == KeyEvent.VK_F1) && evt.isControlDown()) {
         }
         // TODO add your handling code here:
     }//GEN-LAST:event_formKeyPressed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formKeyTyped(final java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyTyped
     }//GEN-LAST:event_formKeyTyped
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdPutKassenzeichenToSearchTreeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPutKassenzeichenToSearchTreeActionPerformed
         if ((kassenzeichenPanel.getShownKassenzeichen() != null) && !kassenzeichenPanel.getShownKassenzeichen().trim().equals("")) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Alle verf\u00FCgbaren Suchen:" + context.getSearch().getDataBeans().keySet());
             }
             final Object object = context.getSearch().getDataBeans().get(kassenzeichenSuche + "@" + CidsAppBackend.DOMAIN);
             if (object != null) {
                 final FormDataBean kassenzeichenSucheParam = (FormDataBean) object;
                 kassenzeichenSucheParam.setBeanParameter("Kassenzeichen", kassenzeichenPanel.getShownKassenzeichen());
                 final Vector v = new Vector();
                 final String cid = String.valueOf(this.KASSENZEICHEN_CLASS_ID) + "@" + CidsAppBackend.DOMAIN;
                 v.add(cid);
                 try {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("vor KassenzeichenSuche aus Plugin");
                     }
                     context.getSearch().performSearch(
                             v,
                             kassenzeichenSucheParam,
                             context.getUserInterface().getFrameFor((PluginUI) this),
                             true);
                 } catch (Exception e) {
                     kassenzeichenPanel.flashSearchField(java.awt.Color.red);
                 }
             } else {
                 LOG.warn("KassenzeichenSuche (" + kassenzeichenSuche + "@" + CidsAppBackend.DOMAIN + ") nicht vorhanden!!!");
             }
         }
     }//GEN-LAST:event_cmdPutKassenzeichenToSearchTreeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdWorkflowActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWorkflowActionPerformed
     }//GEN-LAST:event_cmdWorkflowActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formWindowClosing(final java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         LOG.info("formWindowClosing");
         if (editmode && !kassenzeichenPanel.isEmpty()) {
             if (changesPending()) {
                 final int answer = JOptionPane.showConfirmDialog(
                         this,
                         "Wollen Sie die gemachten \u00C4nderungen speichern?",
                         "Verdis \u00C4nderungen",
                         JOptionPane.YES_NO_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     storeChanges();
                 }
                 unlockDataset();
             } else {
                 unlockDataset();
             }
         }
         closeAllConnections();
     }//GEN-LAST:event_formWindowClosing
 
     /**
      * DOCUMENT ME!
      */
     private void closeAllConnections() {
         try {
         } catch (Exception e) {
             LOG.error("Fehler beim Schlie\u00DFen der Connections");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formWindowClosed(final java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
     }//GEN-LAST:event_formWindowClosed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNewKassenzeichenActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewKassenzeichenActionPerformed
         if (!readonly) {
             if (changesPending()) {
                 final int answer = JOptionPane.showConfirmDialog(
                         this,
                         "Wollen Sie die gemachten \u00C4nderungen zuerst speichern?",
                         "Neues Kassenzeichen",
                         JOptionPane.YES_NO_CANCEL_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     storeChanges();
                     newKassenzeichen();
                 } else if (answer == JOptionPane.NO_OPTION) {
                     newKassenzeichen();
                 }
             } else {
                 newKassenzeichen();
             }
         }
     }//GEN-LAST:event_cmdNewKassenzeichenActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdOkActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOkActionPerformed
         if (true || changesPending()) {
             storeChanges();
         }
     }//GEN-LAST:event_cmdOkActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCancelActionPerformed
         if (changesPending()) {
             final int answer = JOptionPane.showConfirmDialog(
                     this,
                     "Wollen Sie die gemachten \u00C4nderungen verwerfen?",
                     "Abbrechen",
                     JOptionPane.YES_NO_OPTION);
             if (answer == JOptionPane.YES_OPTION) {
                 fixMapExtent = CidsAppBackend.getInstance().getMainMap().isFixedMapExtent();
                 CidsAppBackend.getInstance().getMainMap().setFixedMapExtent(true);
             } else {
                 return;
             }
         }
         enableEditing(false);
         unlockDataset();
         kassenzeichenPanel.refresh();
     }//GEN-LAST:event_cmdCancelActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdEditModeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdEditModeActionPerformed
         if (!readonly) {
             if (!editmode && (kassenzeichenPanel.isEmpty() || lockDataset())) {
                 enableEditing(true);
             } else if (editmode && (changesPending() == false)) {
                 unlockDataset();
                 enableEditing(false);
             }
         }
     }//GEN-LAST:event_cmdEditModeActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdCopyFlaecheActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCopyFlaecheActionPerformed
         if (flaechenClipboard != null) {
             flaechenClipboard.storeToFile();
             flaechenClipboard.copy();
         }
     }//GEN-LAST:event_cmdCopyFlaecheActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdTestActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdTestActionPerformed
     }//GEN-LAST:event_cmdTestActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdTest2ActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdTest2ActionPerformed
     }//GEN-LAST:event_cmdTest2ActionPerformed
     /**
      * ToDo Threading and Progressbar.
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdLagisCrossoverActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdLagisCrossoverActionPerformed
         try {
             final JDialog dialog = new JDialog(this, "", true);
             final PopupLagisCrossoverPanel lcp = new PopupLagisCrossoverPanel(prefs.getLagisCrossoverPort(), this);
             dialog.add(lcp);
             dialog.pack();
             dialog.setIconImage(new javax.swing.ImageIcon(
                     getClass().getResource("/de/cismet/verdis/res/images/toolbar/lagisCrossover.png")).getImage());
             dialog.setTitle("Flurstück in LagIS öffnen.");
             dialog.setLocationRelativeTo(this);
             lcp.startSearch();
             dialog.setVisible(true);
         } catch (Exception ex) {
             LOG.error("Crossover: Fehler im LagIS Crossover", ex);
             // ToDo Meldung an Benutzer
         }
     }//GEN-LAST:event_cmdLagisCrossoverActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuRenameKZActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuRenameKZActionPerformed
         if (!readonly) {
             if (changesPending()) {
                 final int answer = JOptionPane.showConfirmDialog(
                         this,
                         "Wollen Sie die gemachten \u00C4nderungen zuerst speichern?",
                         "Kassenzeichen umbenennen",
                         JOptionPane.YES_NO_CANCEL_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     storeChanges();
                     renameKZ();
                 } else if (answer == JOptionPane.NO_OPTION) {
                     kassenzeichenPanel.refresh();
                     renameKZ();
                 }
             } else {
                 renameKZ();
             }
         }
     }//GEN-LAST:event_mnuRenameKZActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniKarteActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniKarteActionPerformed
         showOrHideView(vKarte);
     }//GEN-LAST:event_mniKarteActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniTabelleActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniTabelleActionPerformed
         showOrHideView(vTabelleWDSR);
     }//GEN-LAST:event_mniTabelleActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniDetailsActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniDetailsActionPerformed
         showOrHideView(vDetailsWDSR);
     }//GEN-LAST:event_mniDetailsActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void jMenuItem1ActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         DeveloperUtil.createWindowLayoutFrame("Momentanes Layout", rootWindow).setVisible(true);
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void btnHistoryActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistoryActionPerformed
         HistoryPanel historyPan = new HistoryPanel();
         historyPan.setCidsBean(kassenzeichenBean);
 
         JDialog dial = new JDialog(this, true);
         dial.setTitle("Kassenzeichen-Verlauf");
         dial.setContentPane(historyPan);
         dial.setSize(800, 600);
         dial.setLocationRelativeTo(this);
         dial.setVisible(true);
     }//GEN-LAST:event_btnHistoryActionPerformed
 
     private CidsBeanTable getCurrentCidsbeanTable() {
         CidsBeanTable cidsBeanTable = null;
         switch (CidsAppBackend.getInstance().getMode()) {
             case REGEN: {
                 cidsBeanTable = regenFlaechenTabellenPanel;
             }
             break;
             case ESW: {
                 cidsBeanTable = wdsrFrontenTabellenPanel;
             }
             break;
         }
         return cidsBeanTable;
     }
 
     private void setKZGeomFromSole() {
         final PFeature sole = getMappingComponent().getSolePureNewFeature();
         if (sole != null) {
             setKZGeomFromFeature(sole.getFeature());
         }
     }
 
     public static void transformToDefaultCrsNeeded(Geometry geom) {
         if (geom == null) {
             return;
         }
 
         // Srid des solefeatures prüfen
         int srid = geom.getSRID();
         final int defaultSrid = CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getDefaultCrs());
         if (srid == CismapBroker.getInstance().getDefaultCrsAlias()) {
             srid = defaultSrid;
         }
         //gegebenenfalls transformieren
         if (srid != defaultSrid) {
             final int ans = JOptionPane.showConfirmDialog(
                     Main.THIS,
                     "Die angegebene Geometrie befindet sich nicht im Standard-CRS. Soll die Geometrie konvertiert werden?",
                     "Geometrie konvertieren?",
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE);
 
             if (ans == JOptionPane.YES_OPTION) {
                 geom = CrsTransformer.transformToDefaultCrs(geom);
                 geom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
             }
         } else {
             geom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
         }
     }
 
     private void setKZGeomFromFeature(Feature feature) {
         try {
             Geometry geom = feature.getGeometry();
 
             setGeometry(geom);
             getMappingComponent().getFeatureCollection().removeFeature(feature);
             final Feature add = new BeanUpdatingCidsFeature(kassenzeichenBean, KassenzeichenPropertyConstants.PROP__GEOMETRIE__GEO_FIELD);
             final boolean editable = CidsAppBackend.getInstance().isEditable();
             add.setEditable(editable);
             getMappingComponent().getFeatureCollection().addFeature(add);
         } catch (Exception ex) {
             LOG.error("error while setting geometrie to kassenzeichen bean", ex);
         }
     }
 
     private void removeKZGeometrie() {
         try {
             final Feature remove = new BeanUpdatingCidsFeature(kassenzeichenBean, KassenzeichenPropertyConstants.PROP__GEOMETRIE__GEO_FIELD);
             setGeometry(null);
             getMappingComponent().getFeatureCollection().removeFeature(remove);
         } catch (Exception ex) {
             LOG.error("error while removing geometrie from kassenzeichen bean", ex);
         }
     }
 
     public Geometry getGeometry() {
         return getGeometry(getCidsBean());
     }
 
     private void setGeometry(final Geometry geom) throws Exception {
         setGeometry(geom, getCidsBean());
     }
 
     public static Geometry getGeometry(final CidsBean kassenzeichenBean) {
         if (kassenzeichenBean != null && kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE) != null) {
             return (Geometry) kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE__GEO_FIELD);
         } else {
             return null;
         }
     }
 
     public static void setGeometry(final Geometry geom, final CidsBean kassenzeichenBean) throws Exception {
         transformToDefaultCrsNeeded(geom);
         if (kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE) == null) {
             final CidsBean emptyGeoBean = CidsAppBackend.getInstance().getVerdisMetaClass(VerdisMetaClassConstants.MC_GEOM).getEmptyInstance().getBean();
             kassenzeichenBean.setProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE, emptyGeoBean);
         }
         kassenzeichenBean.setProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE__GEO_FIELD, geom);
     }
 
     private void cmdAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddActionPerformed
         if (CidsAppBackend.getInstance().getMode().equals(CidsAppBackend.Mode.ALLGEMEIN)) {
             setKZGeomFromSole();
         } else {
             CidsBeanTable cidsBeanTable = getCurrentCidsbeanTable();
             if (cidsBeanTable != null) {
                 cidsBeanTable.addNewBean();
             }
         }
     }//GEN-LAST:event_cmdAddActionPerformed
 
     private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveActionPerformed
         if (CidsAppBackend.getInstance().getMode().equals(CidsAppBackend.Mode.ALLGEMEIN)) {
             removeKZGeometrie();
         } else {
             CidsBeanTable cidsBeanTable = getCurrentCidsbeanTable();
             if (cidsBeanTable != null) {
                 cidsBeanTable.removeSelectedBeans();
             }
         }
     }//GEN-LAST:event_cmdRemoveActionPerformed
 
     private void cmdUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUndoActionPerformed
         CidsBeanTable cidsBeanTable = getCurrentCidsbeanTable();
         if (cidsBeanTable != null) {
             cidsBeanTable.restoreSelectedBeans();
         }
     }//GEN-LAST:event_cmdUndoActionPerformed
 
     private void mniOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniOptionsActionPerformed
         final OptionsDialog od = new OptionsDialog(this, true);
         od.setLocationRelativeTo(this);
         od.setVisible(true);
     }//GEN-LAST:event_mniOptionsActionPerformed
 
     private void cmdFortfuehrungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFortfuehrungActionPerformed
         FortfuehrungsanlaesseDialog.getInstance().setVisible(true);
     }//GEN-LAST:event_cmdFortfuehrungActionPerformed
 
    public void loadAlkisFlurstueck(final Point geom) {
         new SwingWorker<Integer, Void>() {
 
             @Override
             protected Integer doInBackground() throws Exception {
                 final Point transformedPoint = CrsTransformer.transformToGivenCrs(geom, "EPSG:25832");
                 transformedPoint.setSRID(25832);
                 final Collection<Integer> ids = SessionManager.getProxy().customServerSearch(SessionManager.getSession().getUser(), new AlkisLandparcelSearch(transformedPoint));
                 if (ids == null || ids.isEmpty()) {
                     JOptionPane.showMessageDialog(Main.THIS, "<html>Es wurden in dem markierten Bereich<br/>keine Flurstücke gefunden.", "Keine FLurstücke gefunden.", JOptionPane.INFORMATION_MESSAGE);
                     return null;
                 } else {
                     return ids.toArray(new Integer[0])[0];
                 }
             }
 
             @Override
             protected void done() {
                 try {
                     final Integer id = get();
                     if (id != null) {
                         try {
                             DescriptionPane descPane = ComponentRegistry.getRegistry().getDescriptionPane();
                             descPane.clearBreadCrumb();
                             descPane.clear();
                             descPane.gotoMetaObject(ClassCacheMultiple.getMetaClass("WUNDA_BLAU", "alkis_landparcel"), id, "");
                             if (!alkisRendererDialog.isVisible()) {
                                 alkisRendererDialog.setVisible(true);
                             }
                         } catch (Exception ex) {
                             LOG.error("error while loading renderer", ex);
                         }
                     }
                 } catch (Exception ex) {
                     LOG.error("error while searching flurstueck", ex);
                 }
             }
 
         }.execute();
     }
             
 
     /**
      * DOCUMENT ME!
      */
     public void renameKZ() {
         //final String oldKZ = this.kassenzeichenPanel.getShownKassenzeichen();
         final String newKZ = JOptionPane.showInputDialog(
                 this,
                 "Geben Sie das neue Kassenzeichens ein:",
                 "Kassenzeichen umbenennen",
                 JOptionPane.QUESTION_MESSAGE);
 
         if (!(newKZ == null)) {
             try {
                 final int newKZInt = new Integer(newKZ);
                 // prüfen ob kz bereits existiert
                 final CidsBean newBean = CidsAppBackend.getInstance().loadKassenzeichenByNummer(newKZInt);
                 if (newBean != null) {
                     JOptionPane.showMessageDialog(
                             Main.this,
                             "Dieses Kassenzeichen existiert bereits.",
                             "Fehler",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 this.unlockDataset();
                 if (this.lockDataset(newKZ)) {
                     try {
                         kassenzeichenBean.setProperty(KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER, newKZInt);
                         if (storeChanges(false, newKZ)) {
                             this.unlockDataset(newKZ);
                         } else {
                             if (LOG.isDebugEnabled()) {
                                 LOG.debug("storechanges error");
                             }
                         }
                     } catch (Exception ex) {
                         LOG.error("error while setting kassenzeichennummer", ex);
                     }
                 }
             } catch (NumberFormatException e) {
                 JOptionPane.showMessageDialog(
                         this,
                         "Kassenzeichen muss eine Zahl sein.",
                         "Fehler",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void newKassenzeichen() {
         final String newKassenzeichennummer = JOptionPane.showInputDialog(
                 this,
                 "Geben Sie das neue Kassenzeichen ein:",
                 "Neues Kassenzeichen",
                 JOptionPane.QUESTION_MESSAGE);
         if (!((newKassenzeichennummer == null) || newKassenzeichennummer.equals(""))) {
             try {
                 final int kzNummer = new Integer(newKassenzeichennummer);
 
                 new SwingWorker<Void, Void>() {
 
                     @Override
                     protected Void doInBackground() throws Exception {
                         // prüfen ob kz bereits existiert
                         final CidsBean newBean = CidsAppBackend.getInstance().loadKassenzeichenByNummer(kzNummer);
                         if (newBean != null) {
                             JOptionPane.showMessageDialog(
                                     Main.this,
                                     "Dieses Kassenzeichen existiert bereits.",
                                     "Fehler",
                                     JOptionPane.ERROR_MESSAGE);
                             return null;
                         }
 
                         unlockDataset();
                         if (lockDataset(newKassenzeichennummer)) {
                             final CidsBean kassenzeichen = createNewKassenzeichen(kzNummer);
                             kassenzeichen.persist();
 
                             unlockDataset(newKassenzeichennummer);
                             kassenzeichenPanel.setKZSearchField(newKassenzeichennummer);
                             kassenzeichenPanel.gotoKassenzeichen(newKassenzeichennummer);
 
                             enableEditing(true);
                         } else {
                             JOptionPane.showMessageDialog(
                                     Main.this,
                                     "Neues Kassenzeichen kann nicht gesperrt werden.",
                                     "Fehler",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                         return null;
                     }
 
                     @Override
                     protected void done() {
                     }
                 }.execute();
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(
                         this,
                         "Kassenzeichen muss eine Zahl sein.",
                         "Fehler",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
     }
 
     private CidsBean createNewKassenzeichen(int nummer) throws Exception {
         final Calendar cal = Calendar.getInstance();
         final java.sql.Date erfassungsdatum = new java.sql.Date(cal.getTimeInMillis());
         cal.add(Calendar.MONTH, 1);
         final SimpleDateFormat vDat = new SimpleDateFormat("yy/MM");
         final String veranlagungsdatum = "'" + vDat.format(cal.getTime()) + "'";
 
 
 
         final MetaObject kassenzeichenMo = CidsAppBackend.getInstance().getVerdisMetaClass(VerdisMetaClassConstants.MC_KASSENZEICHEN).getEmptyInstance();
         final MetaObject kanalanschlussMo = CidsAppBackend.getInstance().getVerdisMetaClass(VerdisMetaClassConstants.MC_KANALANSCHLUSS).getEmptyInstance();
         final MetaObject geomMo = CidsAppBackend.getInstance().getVerdisMetaClass(VerdisMetaClassConstants.MC_GEOM).getEmptyInstance();
 
         final CidsBean kassenzeichen = kassenzeichenMo.getBean();
         final CidsBean kanalanschluss = kanalanschlussMo.getBean();
         final CidsBean geomBean = geomMo.getBean();
 
         //TODO sobald die FEBs und DMS auf 8-stellige kassenzeichen umgestellt worden sind, kann diese zeile rausfliegen
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER_OLD, nummer);
        //--
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__KASSENZEICHENNUMMER, nummer);
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__KANALANSCHLUSS, kanalanschluss);
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__DATUM_VERANLAGUNG, veranlagungsdatum);
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__DATUM_ERFASSUNG, erfassungsdatum);
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__BEMERKUNG, "");
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__SPERRE, false);
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__BEMERKUNG_SPERRE, "");
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__LETZTE_AENDERUNG_TIMESTAMP, new Timestamp(new java.util.Date().getTime()));
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__LETZTE_AENDERUNG_USER, Main.THIS.getUserString());
         kassenzeichen.setProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE, geomBean);
 
         return kassenzeichen;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void deleteKZ() {
         if ((kassenzeichenPanel.getShownKassenzeichen() != null) && !(kassenzeichenPanel.getShownKassenzeichen().trim().equals(""))) {
             final int answer = JOptionPane.showConfirmDialog(
                     this,
                     "Wollen Sie wirklich das Kassenzeichen "
                     + kassenzeichenPanel.getShownKassenzeichen()
                     + " l\u00F6schen?",
                     "Kassenzeichen l\u00F6schen",
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE);
             if (answer == JOptionPane.YES_OPTION) {
                 try {
                     // flaechen loeschen
                     final Collection<CidsBean> flaechenBeans = regenFlaechenTabellenPanel.getAllBeans();
                     for (final CidsBean flaecheBean : flaechenBeans.toArray(new CidsBean[0])) {
                         regenFlaechenTabellenPanel.removeBean(flaecheBean);
                     }
 
                     // fronten loeschen
                     final Collection<CidsBean> frontenBeans = wdsrFrontenTabellenPanel.getAllBeans();
                     for (final CidsBean frontBean : frontenBeans.toArray(new CidsBean[0])) {
                         wdsrFrontenTabellenPanel.removeBean(frontBean);
                     }
 
                     // kanalanschluss löschen
                     final Collection<CidsBean> toDeleteBefrBeans = new ArrayList<CidsBean>();
                     final CidsBean kanalanschlussBean = (CidsBean) kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__KANALANSCHLUSS);
                     if (kanalanschlussBean != null) {
                         // befreiungen und erlaubnisse von kanalanschluss löschen
                         final Collection<CidsBean> befUndErlBeans = (Collection<CidsBean>) kanalanschlussBean.getProperty("befreiungenunderlaubnisse");
                         for (final CidsBean befUndErlBean : befUndErlBeans) {
                             toDeleteBefrBeans.add(befUndErlBean);
                         }
                         for (final CidsBean toDeleteBean : toDeleteBefrBeans) {
                             befUndErlBeans.remove(toDeleteBean);
                             toDeleteBean.delete();
                         }
                         kanalanschlussBean.delete();
                     }
 
                     // kassenzeichen-geometrie löschen
                     final CidsBean geomBean = (CidsBean) kassenzeichenBean.getProperty(KassenzeichenPropertyConstants.PROP__GEOMETRIE);
                     if (geomBean != null) {
                         geomBean.delete();
                     }
 
                     // kassenzeichen selbst löschen
                     kassenzeichenBean.delete();
 
                     // und ab dafür!
                     kassenzeichenBean.persist();
 
                     CidsAppBackend.getInstance().setCidsBean(null);
 
                     kassenzeichenPanel.setKZSearchField("");
                     Main.THIS.enableEditing(false);
                 } catch (final Exception ex) {
                     JOptionPane.showMessageDialog(this.getComponent(),
                             "Das Kassenzeichen konnte nicht gelöscht werden.",
                             "Fehler beim Löschen",
                             JOptionPane.ERROR_MESSAGE);
                     LOG.error("error while deleting kassenzeichen", ex);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  the command line arguments
      */
     public static void main(final String[] args) {
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
 
             @Override
             public void uncaughtException(final Thread t, final Throwable e) {
                 LOG.error("Uncaught Exception in " + t, e);
             }
         });
 
 //        EventQueue.invokeLater(new Runnable() {
 //
 //            @Override
 //            public void run() {
 //                JDialog d = new JDialog();
 //                d.setTitle("test");
 //                d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 //                JProgressBar pb = new JProgressBar();
 //                pb.setIndeterminate(true);
 //                d.getContentPane().add(pb);
 //                d.pack();
 //
 //                d.setVisible(true);
 
 
 
         try {
             SPLASH = StaticStartupTools.showGhostFrame(FILEPATH_SCREEN, "verdis [Startup]");
         } catch (Exception e) {
             LOG.warn("Problem beim Darstellen des Pre-Loading-Frame", e);
         }
 
         final Main m = new Main();
 
 
         if (SPLASH != null) {
 
 //            EventQueue.invokeLater(new Runnable() {
 //
 //                @Override
 //                public void run() {
             m.setBounds(SPLASH.getBounds());
 //                }
 //            });
         }
 //            }
 //        });
         ((JFrame) m).setVisible(true);
 
         if (SPLASH != null) {
             SPLASH.dispose();
         }
         SPLASH = null;
     }
 
     @Override
     public void setVisible(final boolean b) {
         if (!plugin) {
             super.setVisible(b);
         }
     }
 
     // Methoden f\u00FCr die Plugin Schnittstelle
     @Override
     public PluginUI getUI(final String str) {
         return this;
     }
 
     @Override
     public PluginMethod getMethod(final String str) {
         return null;
     }
 
     @Override
     public void setActive(final boolean param) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("setActive(" + param + ")");
         }
         if ((param == false) && editmode && !kassenzeichenPanel.isEmpty()) {
             if (changesPending()) {
                 final int answer = JOptionPane.showConfirmDialog(
                         this,
                         "Wollen Sie die gemachten \u00C4nderungen speichern?",
                         "Verdis \u00C4nderungen",
                         JOptionPane.YES_NO_OPTION);
                 if (answer == JOptionPane.YES_OPTION) {
                     storeChanges();
                 }
                 unlockDataset();
             } else {
                 unlockDataset();
             }
         }
         if (param == false) {
             closeAllConnections();
             // Inserting Docking Window functionalty (Sebastian) 24.07.07
             configurationManager.writeConfiguration();
             saveLayout(FILEPATH_PLUGINLAYOUT);
         }
         flaechenClipboard.deleteStoreFile();
     }
 
     @Override
     public java.util.Iterator getUIs() {
         final LinkedList ll = new LinkedList();
         ll.add(this);
         return ll.iterator();
     }
 
     @Override
     public PluginProperties getProperties() {
         return null;
     }
 
     @Override
     public java.util.Iterator getMethods() {
         final LinkedList ll = new LinkedList();
         return ll.iterator();
     }
 
     @Override
     public void shown() {
     }
 
     @Override
     public void resized() {
     }
 
     @Override
     public void moved() {
     }
 
     @Override
     public void hidden() {
     }
 
     @Override
     public java.util.Collection getMenus() {
         return menues;
     }
 
     @Override
     public String getId() {
         return "verdis";
     }
 
     @Override
     public JComponent getComponent() {
         return panMain;
     }
 
     @Override
     public java.util.Collection getButtons() {
         return Arrays.asList(this.tobVerdis.getComponents());
         // return null;
     }
 
     @Override
     public void floatingStopped() {
     }
 
     @Override
     public void floatingStarted() {
     }
 
     @Override
     public boolean changesPending() {
         if (kassenzeichenBean == null) {
             return false;
         }
         return kassenzeichenBean.getMetaObject().getStatus() == MetaObject.MODIFIED;
     }
 
     public void selectionChanged() {
         refreshClipboardButtons();
         refreshItemButtons();
     }
 
     public void refreshClipboardButtons() {
         final boolean isEditable = CidsAppBackend.getInstance().isEditable();
         final boolean isFlaechen = CidsAppBackend.getInstance().getMode() == CidsAppBackend.Mode.REGEN;
         cmdPasteFlaeche.setEnabled(isEditable && isFlaechen && flaechenClipboard.isPastable());
         cmdCopyFlaeche.setEnabled(isFlaechen && flaechenClipboard.isCopyable());
         cmdCutFlaeche.setEnabled(isEditable && isFlaechen && flaechenClipboard.isCutable());
     }
 
     public void refreshItemButtons() {
         final boolean isEditable = CidsAppBackend.getInstance().isEditable();
 
         if (isEditable) {
             List<CidsBean> selectedBeans = null;
 
             switch (CidsAppBackend.getInstance().getMode()) {
                 case REGEN: {
                     selectedBeans = regenFlaechenTabellenPanel.getSelectedBeans();
                 }
                 break;
                 case ESW: {
                     selectedBeans = wdsrFrontenTabellenPanel.getSelectedBeans();
                 }
                 break;
                 case ALLGEMEIN: {
                     final Geometry geom = getGeometry();
                     final PFeature sole = getMappingComponent().getSolePureNewFeature();
 
                     // hat noch keine geometrie und es ist ein feature in der karte
                     cmdAdd.setEnabled(geom == null && sole != null);
                     // hat eine geometrie
                     cmdRemove.setEnabled(geom != null);
 
                     //undo nie möglich
                     cmdUndo.setEnabled(false);
                 }
                 return;
             }
             final boolean hasItemsInSelection = selectedBeans != null && !selectedBeans.isEmpty();
 
             cmdAdd.setEnabled(true);
             cmdRemove.setEnabled(hasItemsInSelection);
             cmdUndo.setEnabled(hasItemsInSelection);
         } else {
             cmdAdd.setEnabled(false);
             cmdRemove.setEnabled(false);
             cmdUndo.setEnabled(false);
         }
     }
 
     public void refreshKassenzeichenButtons() {
         final boolean b = editmode;
 
         cmdOk.setEnabled(b && !aggValidator.getState().isError());
         cmdCancel.setEnabled(b);
         cmdDeleteKassenzeichen.setEnabled(b && kassenzeichenBean != null);
         cmdNewKassenzeichen.setEnabled(!b && !readonly);
         cmdEditMode.setEnabled(!b && !readonly && kassenzeichenBean != null);
 
         cmdRefreshEnumeration.setEnabled(b);
         cmdPdf.setEnabled(kassenzeichenBean != null && !kassenzeichenBean.getBeanCollectionProperty(KassenzeichenPropertyConstants.PROP__FLAECHEN).isEmpty());
     }
 
     @Override
     public void enableEditing(final boolean b) {
         CidsAppBackend.getInstance().setEditable(b);
         try {
             editmode = b;
 
             refreshKassenzeichenButtons();
             refreshClipboardButtons();
             refreshItemButtons();
 
             mnuRenameKZ.setEnabled(b);
             kartenPanel.setEnabled(b);
 
 //            final Iterator it = stores.iterator();
 //            while (it.hasNext()) {
 //                final Storable store = (Storable) it.next();
 //                store.enableEditing(b);
 //            }
             refreshLeftTitleBarColor();
 
             CidsAppBackend.getInstance().getMainMap().getMemRedo().clear();
             CidsAppBackend.getInstance().getMainMap().getMemUndo().clear();
         } catch (Exception e) {
             LOG.error("Fehler beim Wechseln in den EditMode", e);
         }
     }
 
     public void enableSave(final boolean b) {
         cmdOk.setEnabled(CidsAppBackend.getInstance().isEditable() && b);
     }
 
     @Override
     public boolean lockDataset() {
         return kassenzeichenPanel.lockDataset();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   object_id  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean lockDataset(final String object_id) {
         return kassenzeichenPanel.lockDataset(object_id);
     }
 
     @Override
     public void unlockDataset() {
         kassenzeichenPanel.unlockDataset();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  object_id  DOCUMENT ME!
      */
     public void unlockDataset(final String object_id) {
         kassenzeichenPanel.unlockDataset(object_id);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void storeChanges() {
         storeChanges(false);
     }
 
     public void disableKassenzeichenCmds() {
         cmdEditMode.setEnabled(false);
         cmdOk.setEnabled(false);
         cmdCancel.setEnabled(false);
         cmdDeleteKassenzeichen.setEnabled(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   statements               DOCUMENT ME!
      * @param   editModeAfterStoring     DOCUMENT ME!
      * @param   refreshingKassenzeichen  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean storeChanges(final boolean editModeAfterStoring,
             final String refreshingKassenzeichen) {
         try {
             disableKassenzeichenCmds();
             kassenzeichenBean.setProperty(KassenzeichenPropertyConstants.PROP__LETZTE_AENDERUNG_TIMESTAMP, new Timestamp(new java.util.Date().getTime()));
             kassenzeichenBean.setProperty(KassenzeichenPropertyConstants.PROP__LETZTE_AENDERUNG_USER, Main.THIS.getUserString());
             setCidsBean(kassenzeichenBean.persist());
 
             if (!editModeAfterStoring) {
                 unlockDataset();
             }
             enableEditing(editModeAfterStoring);
 
             fixMapExtent = CidsAppBackend.getInstance().getMainMap().isFixedMapExtent();
             CidsAppBackend.getInstance().getMainMap().setFixedMapExtent(true);
             if (refreshingKassenzeichen == null) {
                 getKzPanel().refresh();
             } else {
                 getKzPanel().gotoKassenzeichen(refreshingKassenzeichen);
             }
             return true;
         } catch (Exception e) {
             enableEditing(true);
             LOG.error("error during persist", e);
             JXErrorPane.showDialog(
                     this,
                     new ErrorInfo(
                     "Fehler beim Schreiben",
                     "Beim Speichern des Kassenzeichens kam es zu einem Fehler.",
                     null,
                     "",
                     e,
                     null,
                     null));
             return false;
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     public void resetFixedMapExtent() {
         CidsAppBackend.getInstance().getMainMap().setFixedMapExtent(fixMapExtent);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  statements            DOCUMENT ME!
      * @param  editModeAfterStoring  DOCUMENT ME!
      */
     public void storeChanges(final boolean editModeAfterStoring) {
         storeChanges(editModeAfterStoring, null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public de.cismet.verdis.gui.KassenzeichenPanel getKzPanel() {
         return kassenzeichenPanel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!c
      */
     public KanaldatenPanel getKanalPanel() {
         return kanaldatenPanel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isInEditMode() {
         return editmode;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getUserString() {
         return userString;
     }
 
     @Override
     public void configure(final Element parent) {
 
         try {
             final Element verdis = parent.getChild("verdis");
             final String modeString = verdis.getAttribute("mode").getValue();
             CidsAppBackend.Mode mode = CidsAppBackend.Mode.valueOf(modeString);
             CidsAppBackend.getInstance().setMode(mode);
 
         } catch (Exception e) {
             CidsAppBackend.getInstance().setMode(CidsAppBackend.Mode.ALLGEMEIN);
             LOG.warn("Problem beim Setzen des Modes", e);
 
         }
     }
 
     @Override
     public Element getConfiguration() {
         try {
             final Element verdis = new Element("verdis");
             verdis.setAttribute("mode", CidsAppBackend.getInstance().getMode().name());
             return verdis;
         } catch (Exception e) {
             LOG.error("Fehler beim Schreiben der Config", e);
             return null;
         }
 
     }
 
     @Override
     public void masterConfigure(final Element parent) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static MappingComponent getMappingComponent() {
         return CidsAppBackend.getInstance().getMainMap();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  userString  DOCUMENT ME!
      */
     public void setUserString(final String userString) {
         this.userString = userString;
         Main.this.setTitle("verdis [" + userString + "]");
 //        kzPanel.setUserString(userString);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Image getBannerImage() {
         return banner;
     }
 
     @Override
     public void dispose() {
         try {
             StaticStartupTools.saveScreenshotOfFrame(this, FILEPATH_SCREEN);
         } catch (Exception ex) {
             LOG.fatal("Fehler beim Capturen des App-Inhaltes", ex);
         }
         super.dispose();
         if (LOG.isDebugEnabled()) {
             LOG.debug("Dispose: Verdis wird beendet.");
         }
         if (rmPlugin != null) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Dispose: RMPlugin wird heruntergefahren");
             }
             rmPlugin.setActive(false);
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("Dispose: layout wird gespeichert.");
         }
         // Inserting Docking Window functionalty (Sebastian) 24.07.07
         configurationManager.writeConfiguration();
         saveLayout(FILEPATH_LAYOUT + "." + currentMode);
 
         flaechenClipboard.deleteStoreFile();
         System.exit(0);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public AppPreferences getPrefs() {
         return prefs;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean isLoggedIn() {
         return loggedIn || noLoginDuringDev;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  loggedIn  DOCUMENT ME!
      */
     public static void setLoggedIn(final boolean loggedIn) {
         Main.loggedIn = loggedIn;
     }
 
     @Override
     public CidsBean getCidsBean() {
         return kassenzeichenBean;
     }
 
     public Validator getValidatorKassenzeichen(final CidsBean kassenzeichenBean) {
         final AggregatedValidator aggVal = new AggregatedValidator();
         aggVal.add(kassenzeichenPanel.getValidator());
         aggVal.add(regenFlaechenTabellenPanel.getValidator());
         aggVal.add(wdsrFrontenTabellenPanel.getValidator());
         return aggVal;
     }
 
     @Override
     public void setCidsBean(final CidsBean cidsBean) {
         kassenzeichenBean = cidsBean;
     }
 
     //~ Inner Classes ----------------------------------------------------------
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class NodeChangeListener extends MetaNodeSelectionListener {
 
         //~ Instance fields ----------------------------------------------------
         private final SingleAttributeIterator attributeIterator;
         private final Collection classNames;
         private final Collection attributeNames;
         final private Object nodeSelectionChangedBlocker = new Object();
 
         //~ Constructors -------------------------------------------------------
         /**
          * Creates a new NodeChangeListener object.
          */
         private NodeChangeListener() {
             this.classNames = context.getEnvironment().getAttributeMappings("className");
             this.attributeNames = context.getEnvironment().getAttributeMappings("attributeName");
             if (this.attributeNames.isEmpty()) {
                 this.attributeNames.add("id");
             }
 
             final AttributeRestriction attributeRestriction = new ComplexAttributeRestriction(
                     AttributeRestriction.OBJECT,
                     AttributeRestriction.IGNORE,
                     null,
                     this.attributeNames,
                     null);
             this.attributeIterator = new SingleAttributeIterator(attributeRestriction, false);
         }
 
         //~ Methods ------------------------------------------------------------
         @Override
         protected void nodeSelectionChanged(final Collection collection) {
             final Thread t = new Thread() {
 
                 @Override
                 public void run() {
                     synchronized (nodeSelectionChangedBlocker) {
                         if ((collection != null) || (!collection.isEmpty())) {
                             final Object selectedNode = collection.iterator().next();
                             if (selectedNode instanceof ObjectTreeNode) {
                                 final ObjectTreeNode objectTreeNode = (ObjectTreeNode) selectedNode;
                                 try {
                                     if ((NodeChangeListener.this.classNames.isEmpty())
                                             || NodeChangeListener.this.classNames.contains(
                                             objectTreeNode.getMetaClass().getName())) {
                                         attributeIterator.init(objectTreeNode);
                                         Object kassenzeichen = null;
                                         if (attributeIterator.hasNext()) {
                                             kassenzeichen = attributeIterator.next().getValue();
                                             getKzPanel().gotoKassenzeichen(kassenzeichen.toString());
                                         } else {
                                             if (LOG.isDebugEnabled()) {
                                                 LOG.debug("falscher attribute name");
                                                 LOG.debug(kassenzeichen);
                                             }
                                         }
                                     } else {
                                         if (LOG.isDebugEnabled()) {
                                             LOG.debug("falscher class name");
                                             LOG.debug(objectTreeNode.getMetaClass().getName());
                                         }
                                         if (LOG.isDebugEnabled()) {
                                             LOG.debug(classNames);
                                         }
                                     }
                                 } catch (Throwable t) {
                                     LOG.error(t.getMessage(), t);
                                 }
                             } else {
                                 if (LOG.isDebugEnabled()) {
                                     LOG.debug("keine object node");
                                 }
                             }
                         }
                     }
                 }
             };
             t.setPriority(Thread.NORM_PRIORITY);
             t.start();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     class CidsAuthentification extends LoginService {
 
         //~ Static fields/initializers -----------------------------------------
         public static final String CONNECTION_PROXY_CLASS =
                 "Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler";
 
         //~ Methods ------------------------------------------------------------
         @Override
         public boolean authenticate(final String name, final char[] password, final String server) throws Exception {
             System.setProperty("sun.rmi.transport.connectionTimeout", "15");
             final String user = name.split("@")[0];
             final String group = name.split("@")[1];
 
             final String callServerURL = prefs.getAppbackendCallserverurl();
             if (LOG.isDebugEnabled()) {
                 LOG.debug("callServerUrl:" + callServerURL);
             }
             final String domain = prefs.getAppbackendDomain();
             final String connectionclass = prefs.getAppbackendConnectionclass();
 
             try {
                 final Connection connection = ConnectionFactory.getFactory().createConnection(connectionclass, callServerURL);
                 final ConnectionInfo connectionInfo = new ConnectionInfo();
                 connectionInfo.setCallserverURL(callServerURL);
                 connectionInfo.setPassword(new String(password));
                 connectionInfo.setUserDomain(domain);
                 connectionInfo.setUsergroup(group);
                 connectionInfo.setUsergroupDomain(domain);
                 connectionInfo.setUsername(user);
                 final ConnectionSession session = ConnectionFactory.getFactory().createSession(connection, connectionInfo, true);
                 final ConnectionProxy proxy = ConnectionFactory.getFactory().createProxy(CONNECTION_PROXY_CLASS, session);
                 CidsAppBackend.init(proxy);
 
                 final String tester = (group + "@" + domain).toLowerCase();
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("authentication: tester = :" + tester);
                     LOG.debug("authentication: name = :" + name);
                     LOG.debug("authentication: RM Plugin key = :" + name + "@" + domain);
                 }
                 if (prefs.getRwGroups().contains(tester)) {
                     Main.this.readonly = false;
                     setUserString(name);
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("RMPlugin: wird initialisiert (VerdisStandalone)");
                         LOG.debug("RMPlugin: Mainframe " + Main.this);
                         LOG.debug("RMPlugin: PrimaryPort " + prefs.getPrimaryPort());
                     }
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("RMPlugin: SecondaryPort " + prefs.getSecondaryPort());
                     }
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("RMPlugin: Username " + (name + "@" + prefs.getStandaloneDomainname()));
                     }
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("RMPlugin: RegistryPath " + prefs.getRmRegistryServerPath());
                     }
 
                     if (prefs.getRmRegistryServerPath() != null) {
                         rmPlugin = new RMPlugin(
                                 Main.this,
                                 prefs.getPrimaryPort(),
                                 prefs.getSecondaryPort(),
                                 prefs.getRmRegistryServerPath(),
                                 name
                                 + "@"
                                 + prefs.getStandaloneDomainname());
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("RMPlugin: erfolgreich initialisiert (VerdisStandalone)");
                         }
                     }
                     return true;
                 } else if (prefs.getUsergroups().contains(tester)) {
                     readonly = true;
                     setUserString(name);
                     if (prefs.getRmRegistryServerPath() != null) {
                         rmPlugin = new RMPlugin(
                                 Main.this,
                                 prefs.getPrimaryPort(),
                                 prefs.getSecondaryPort(),
                                 prefs.getRmRegistryServerPath(),
                                 name
                                 + "@"
                                 + prefs.getStandaloneDomainname());
                     }
                     return true;
                 } else {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("authentication else false");
                     }
                     return false;
                 }
             } catch (Throwable t) {
                 LOG.error("Fehler beim Anmelden", t);
                 return false;
             }
         }
     }
 }
