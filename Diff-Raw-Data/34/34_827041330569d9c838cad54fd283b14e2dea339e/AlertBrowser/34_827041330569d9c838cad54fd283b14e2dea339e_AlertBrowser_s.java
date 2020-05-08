 /*
  * Created on Mar 21, 2006
  */
 package com.alertscape.browser.ui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jms.ConnectionFactory;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.BevelBorder;
 
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.XMLContext;
 
 import com.alertscape.AlertscapeException;
 import com.alertscape.browser.common.auth.Authentication;
 import com.alertscape.browser.common.auth.AuthenticationEvent;
 import com.alertscape.browser.common.auth.AuthenticationListener;
 import com.alertscape.browser.common.auth.AuthenticationEvent.AuthEventType;
 import com.alertscape.browser.localramp.firstparty.preferences.BackgroundPreferenceLoader;
 import com.alertscape.browser.localramp.firstparty.preferences.LoadPreferencesAction;
 import com.alertscape.browser.localramp.firstparty.preferences.SavePreferencesAction;
 import com.alertscape.browser.localramp.firstparty.preferences.UserPreferencesPanel;
 import com.alertscape.browser.model.AlertListenerExceptionListener;
 import com.alertscape.browser.model.BrowserContext;
 import com.alertscape.browser.model.JmsAlertListener;
 import com.alertscape.browser.model.tree.AlertTreeNode;
 import com.alertscape.browser.ui.swing.panel.AlertBrowserStatusPanel;
 import com.alertscape.browser.ui.swing.panel.collection.chart.CreateChartPanelAction;
 import com.alertscape.browser.ui.swing.panel.collection.filter.TextFilterPanel;
 import com.alertscape.browser.ui.swing.panel.collection.summary.AlertCollectionSummaryPanel;
 import com.alertscape.browser.ui.swing.panel.collection.table.AlertCollectionTablePanel;
 import com.alertscape.browser.ui.swing.panel.collection.tree.AlertTreePanel;
 import com.alertscape.browser.ui.swing.panel.common.ASPanelBuilder;
 import com.alertscape.browser.upramp.firstparty.ack.AcknowledgeAlertAction;
 import com.alertscape.browser.upramp.firstparty.clear.ClearAlertAction;
 import com.alertscape.browser.upramp.firstparty.customtag.CustomTagAction;
 import com.alertscape.browser.upramp.firstparty.login.LoginAction;
 import com.alertscape.browser.upramp.firstparty.mail.AlertMailAction;
 import com.alertscape.browser.upramp.firstparty.predefinedtag.PredefinedTagAction;
 import com.alertscape.browser.upramp.firstparty.unack.UnacknowledgeAlertAction;
 import com.alertscape.common.logging.ASLogger;
 import com.alertscape.common.model.Alert;
 import com.alertscape.common.model.AlertAttributeDefinition;
 import com.alertscape.common.model.AlertCollection;
 import com.alertscape.common.model.BinarySortAlertCollection;
 import com.alertscape.service.AlertService;
 import com.alertscape.service.AuthenticationService;
 import com.alertscape.util.ImageFinder;
 
 /**
  * @author josh
  * @version $Version: $
  */
 public class AlertBrowser extends JFrame {
   private static final long serialVersionUID = 1L;
   private static final ASLogger LOG = ASLogger.getLogger(AlertBrowser.class);
 
   private AlertCollection collection;
   private ConnectionFactory jmsFactory;
   private static AlertService alertService;
   private AuthenticationService authenticationService;
   private static BrowserContext currentContext = new BrowserContext();
   private static AlertCollectionTablePanel tablePanel;
   private AlertTreePanel treePanel;
   private static JTabbedPane tabbedPane;
   private static ImageIcon closeIcon;
   private static ImageIcon detailsIcon;
   private String filter = null; // "item like 'GET /pic.do%'";
   private JmsAlertListener listener;
 
   public AlertBrowser() {
   }
 
   public void init() {
     try {
       LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
       for (LookAndFeelInfo info : installedLookAndFeels) {
         LOG.debug(info.getName() + ":" + info.getClassName());
       }
       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       // UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
     } catch (ClassNotFoundException e) {
       LOG.error(e);
     } catch (InstantiationException e) {
       LOG.error(e);
     } catch (IllegalAccessException e) {
       LOG.error(e);
     } catch (UnsupportedLookAndFeelException e) {
       LOG.error(e);
     }
     setSize(800, 600);
     setDefaultCloseOperation(EXIT_ON_CLOSE);
 
     // Get the size of the screen
     Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    
     // Determine the new location of the window
     int w = this.getSize().width;
     int h = this.getSize().height;
    int x = (dim.width-w)/2;
    int y = (dim.height-h)/2;    
     // Move the window
     this.setLocation(x, y);
    
     treePanel = new AlertTreePanel();
     treePanel.setRootNode(getRootNode());
     treePanel.init();
 
     URL imageUrl = getClass().getResource("/com/alertscape/images/mini/close_tab.png");
     closeIcon = new ImageIcon(imageUrl);
 
     URL imageUrl2 = getClass().getResource("/com/alertscape/images/mini/page_text.gif");
     detailsIcon = new ImageIcon(imageUrl2);
 
     collection = new BinarySortAlertCollection();
 
     AlertCollection treeCollection = treePanel.setMasterCollection(collection);
 
     JPanel p = new JPanel();
     p.setLayout(new BorderLayout());
     TextFilterPanel filterPanel = new TextFilterPanel();
     filterPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
     AlertCollection filterCollection = filterPanel.setMasterCollection(treeCollection);
     AlertCollectionSummaryPanel summaryPanel = new AlertCollectionSummaryPanel();
     AlertCollection summaryCollection = summaryPanel.setMasterCollection(filterCollection);
     List<AlertAttributeDefinition> extendedAttributes = null;
     try {
       extendedAttributes = alertService.getAttributeDefinitions();
     } catch (Exception e) {
       LOG.error("Couldn't get attribute definitions", e);
     }
     tablePanel = new AlertCollectionTablePanel(summaryCollection, extendedAttributes);
     tablePanel.init();
 
     Icon bgImage = ImageFinder.getInstance().findImage("/com/alertscape/images/common/hdr_background_small.png");
 
     // Summary
     JPanel outerSummaryPanel = new JPanel();
     outerSummaryPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
     outerSummaryPanel.setLayout(new GridLayout(1, 1));
     outerSummaryPanel.add(summaryPanel);
 
     // toolbar
     JToolBar actionToolbar = new JToolBar();
     actionToolbar.setOpaque(false);
     actionToolbar.setFloatable(false);
 
     AcknowledgeAlertAction ackAction = new AcknowledgeAlertAction();
     ackAction.setParentFrame(this);
     JButton ackButton = actionToolbar.add(ackAction);
     ackButton.setOpaque(false);
 
     UnacknowledgeAlertAction unackAction = new UnacknowledgeAlertAction();
     unackAction.setParentFrame(this);
     JButton unackButton = actionToolbar.add(unackAction);
     unackButton.setOpaque(false);
 
     ClearAlertAction clearAction = new ClearAlertAction();
     clearAction.setParentFrame(this);
     JButton clearButton = actionToolbar.add(clearAction);
     clearButton.setOpaque(false);
 
     AlertMailAction mailAction = new AlertMailAction();
     mailAction.setParentFrame(this);
     JButton mailButton = actionToolbar.add(mailAction);
     mailButton.setOpaque(false);
 
     PredefinedTagAction predefinedTagAction = new PredefinedTagAction();
     predefinedTagAction.setParentFrame(this);
     JButton predefinedTagButton = actionToolbar.add(predefinedTagAction);
     predefinedTagButton.setOpaque(false);
 
     CustomTagAction customTagAction = new CustomTagAction();
     customTagAction.setParentFrame(this);
     JButton customTagButton = actionToolbar.add(customTagAction);
     customTagButton.setOpaque(false);
 
     CreateChartPanelAction chartAction = new CreateChartPanelAction();
     chartAction.setParentFrame(this);
     JButton chartButton = actionToolbar.add(chartAction);
     chartButton.setOpaque(false);
 
     LoginAction loginAction = new LoginAction();
     loginAction.setParentFrame(this);
     JButton loginButton = actionToolbar.add(loginAction);
     loginButton.setOpaque(false);
 
     // Table
     JPanel outerTablePanel = new JPanel();
     outerTablePanel.setLayout(new BorderLayout());
     JLabel hdrLabel = new JLabel();
     hdrLabel.setForeground(Color.white);
     hdrLabel.setText("Alerts");
     hdrLabel.setOpaque(false);
     Font f = new Font("Dialog", Font.ITALIC, 18);
     hdrLabel.setFont(f);
     JPanel headerPanel = new JPanel();
     headerPanel.setBorder(BorderFactory.createEmptyBorder(7, 15, 1, 3));
     headerPanel.setOpaque(false);
     BoxLayout hdrBox = new BoxLayout(headerPanel, BoxLayout.X_AXIS);
     headerPanel.setLayout(hdrBox);
     headerPanel.add(hdrLabel);
     headerPanel.add(Box.createHorizontalGlue());
     headerPanel.add(actionToolbar);
 
     JPanel imagePanel = ASPanelBuilder.wrapInBackgroundImage(headerPanel, bgImage);
     imagePanel.setBackground(Color.white);
     JPanel bgPanel = new JPanel();
     bgPanel.setLayout(new GridLayout(1, 1));
     bgPanel.add(imagePanel);
     bgPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
     outerTablePanel.add(bgPanel, BorderLayout.NORTH);
     outerTablePanel.add(tablePanel, BorderLayout.CENTER);
 
     tabbedPane = new JTabbedPane();
 
     addTabbedPanel("Details", detailsIcon, outerTablePanel, "Tabular view of alerts", false, false);
 
     // tabbedPane.addTab("Details", null, outerTablePanel, "Tabular view of alerts");
 
     // North
     JPanel northPanel = new JPanel();
     northPanel.setLayout(new GridLayout(1, 2));
     northPanel.add(filterPanel);
     northPanel.add(outerSummaryPanel);
 
     p.add(tabbedPane, BorderLayout.CENTER);
     // p.add(outerTablePanel, BorderLayout.CENTER);
     // p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
     JPanel overallPane = new JPanel();
     overallPane.setLayout(new BorderLayout());
     overallPane.add(northPanel, BorderLayout.NORTH);
     JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, p);
     splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.lightGray));
     overallPane.add(splitPane, BorderLayout.CENTER);
     overallPane.add(new AlertBrowserStatusPanel(), BorderLayout.SOUTH);
     setContentPane(overallPane);
 
     // create the menubar
     JMenuBar menubar = new JMenuBar();
 
     JMenu fileMenu = new JMenu("File");
     fileMenu.add(loginAction);
 
     JMenuItem exitItem = new JMenuItem("Exit", new ImageIcon(getClass().getResource(
         "/com/alertscape/images/mini/action_stop.gif")));
     exitItem.setMnemonic(KeyEvent.VK_X);
     exitItem.getAccessibleContext().setAccessibleDescription("Close AMP");
     exitItem.setToolTipText("Close AMP");
     fileMenu.add(exitItem);
 
     // other ideas for the file menu
     // - Switch user
     // - Logoff
     // - Export (to CSV, Excel)
     // - Print (selected alerts or all alerts in view)
 
     // the edit menu is where actions reside that actually modify the alert
     JMenu editMenu = new JMenu("Edit");
     editMenu.add(ackAction);
     editMenu.add(unackAction);
     editMenu.add(clearAction);
     editMenu.addSeparator();
     editMenu.add(mailAction);
     editMenu.add(predefinedTagAction);
     editMenu.add(customTagAction);
     // - Copy
     // - Paste
     // - Cut
     // - Select All
     // - assign alerts (name is just a tag)
     // - move alerts (folder is just a tag)
     // - add comment
 
     JMenu viewMenu = new JMenu("View");
     viewMenu.add(chartAction);
     // - Find (search for alert: in the tree, in the table, in the history)
     // - window configurations (dock type stuff)
     // - view history
     // - view ticket
     // - view comments
     // filter configuration
 
     JMenu preferencesMenu = new JMenu("Preferences");
     // build a list of all of the userpreferencepanels
     List<UserPreferencesPanel> prefPanels = new ArrayList<UserPreferencesPanel>();
     prefPanels.add(filterPanel);
     prefPanels.add(tablePanel);
     prefPanels.add(summaryPanel);
 
     SavePreferencesAction savePrefsAction = new SavePreferencesAction(prefPanels);
     savePrefsAction.setParentFrame(this);
     preferencesMenu.add(savePrefsAction);
     LoadPreferencesAction loadPrefsAction = new LoadPreferencesAction(prefPanels);
     loadPrefsAction.setParentFrame(this);
     preferencesMenu.add(loadPrefsAction);
 
     JMenu helpMenu = new JMenu("Help");
     // helpMenu.setMnemonic(KeyEvent.VK_H);
     // - About AMP
     // - Software Updates
     // - Report a bug or enhancement
     // - Release Notes
     // - Forums (Link to website Forums)
     // - User Guide (Link to versioned user guide doc online)
 
     menubar.add(fileMenu);
     menubar.add(editMenu);
     menubar.add(viewMenu);
     menubar.add(preferencesMenu);
     menubar.add(helpMenu);
 
     this.setJMenuBar(menubar);
 
     // create the popupmenu for the table
     JPopupMenu popup = new JPopupMenu();
     popup.add(ackAction);
     popup.add(unackAction);
     popup.add(clearAction);
     popup.addSeparator();
     popup.add(mailAction);
     popup.add(predefinedTagAction);
     popup.add(customTagAction);
     popup.addSeparator();
     popup.add(chartAction);
     tablePanel.setPopup(popup);
 
     // jframe housekeeping
     setTitle("AMP - Alertscape Management Portal");
     URL cevImageUrl = getClass().getResource("/com/alertscape/images/common/as_logo2_32.png");
     ImageIcon cevImage = new ImageIcon(cevImageUrl);
     setIconImage(cevImage.getImage());
     setVisible(true);
 
     // before getting the alerts, try to grab the default preferences if there are any
     BackgroundPreferenceLoader prefLoader = new BackgroundPreferenceLoader(prefPanels);
     prefLoader.loadDefaultPreferences();
 
     Authentication.addAuthenticationListener(new AuthenticationListener() {
       public void handleAuthEvent(AuthenticationEvent e) {
         if (e.getType() == AuthEventType.LOGIN) {
           // TODO: there is probably a better way to do this than throwing it into a thread, but this is at least an
           // improvement over locking the ui for 2 minutes while it DLs alerts...
           Thread t = new Thread() {
             public void run() {
               initJms();
             }
           };
 
           t.start();
         }
       }
     });
 
     // GenerateEvents gen = new GenerateEvents(collection);
     // int groupSize = 1000;
     // List<Alert> events = new ArrayList<Alert>(groupSize);
     // for (int i = 0; i < 30000; i++) {
     // events.add(gen.buildNewEvent());
     // if (i % groupSize == 0) {
     // ASLogger.debug(i);
     // collection.processAlerts(events);
     // events.clear();
     // }
     // }
     // collection.processAlerts(events);
     //
     // Thread t = new Thread(gen);
     // t.start();
   }
 
   private void initJms() {
     // filter = JOptionPane.showInputDialog("Alert filter:", filter);
     if (listener != null) {
       listener.disconnect();
     }
     listener = new JmsAlertListener();
     listener.setCollection(collection);
     listener.setFactory(jmsFactory);
     listener.setExceptionListener(new AlertListenerExceptionListener() {
       public void onException(Exception e) {
         LOG.error("Received exception from alert listener, reconnecting", e);
         collection.clearAlerts();
         connect(listener);
       }
     });
 
     connect(listener);
   }
 
   private void connect(JmsAlertListener listener) {
     try {
       listener.startListening(filter);
       // GET ALL ALERTS
       List<Alert> alerts = alertService.getAllAlerts(filter);
       LOG.debug("Retrieved " + alerts.size() + " open alerts, processing");
       collection.processAlerts(alerts);
       LOG.info("Processed " + alerts.size() + " open alerts");
       listener.startProcessing();
     } catch (AlertscapeException e) {
       JOptionPane.showMessageDialog(this, "Couldn't initalize alert listener", "Alert Listener Error",
           JOptionPane.ERROR_MESSAGE);
       listener.disconnect();
     }
   }
 
   // this method will create a new tab, with the given title, icon, and panel as contents, to the main browser.
   public static void addTabbedPanel(String title, ImageIcon icon, final JPanel panel, String tooltip,
       boolean showCloseButton, boolean selectNow) {
     tabbedPane.addTab(title, icon, panel, tooltip);
     int newindex = tabbedPane.getTabCount() - 1;
     JPanel pnl = new JPanel(new BorderLayout());
     JLabel label = new JLabel(title, icon, JLabel.LEFT);
     label.setOpaque(false);
     JButton button = new JButton();
     button.setIcon(closeIcon);
     button.setOpaque(false);
     button.setPreferredSize(new Dimension(17, 17));
     button.setBorder(BorderFactory.createEmptyBorder());
     button.setBorderPainted(false);
     button.setHorizontalAlignment(JButton.CENTER);
     button.setToolTipText("Close Tab");
     pnl.setOpaque(false);
 
     JToolBar closeBar = new JToolBar();
     closeBar.setOpaque(false);
     closeBar.setBorderPainted(false);
     closeBar.setFloatable(false);
     closeBar.add(button);
 
     pnl.add(label, BorderLayout.WEST);
     if (showCloseButton) {
       pnl.add(closeBar, BorderLayout.EAST);
     }
     tabbedPane.setTabComponentAt(newindex, pnl);
 
     button.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent arg0) {
         // tabbedPane.removeTabAt(newindex);
         tabbedPane.remove(panel);
       }
     });
 
     if (selectNow) {
       tabbedPane.setSelectedIndex(newindex);
     }
 
   }
 
   /**
    * @return the jmsFactory
    */
   public ConnectionFactory getJmsFactory() {
     return jmsFactory;
   }
 
   /**
    * @param jmsFactory
    *          the jmsFactory to set
    */
   public void setJmsFactory(ConnectionFactory jmsFactory) {
     this.jmsFactory = jmsFactory;
   }
 
   /**
    * @return the alertService
    */
   public AlertService getAlertService() {
     return alertService;
   }
 
   /**
    * @param alertService
    *          the alertService to set
    */
   public void setAlertService(AlertService alertService) {
     AlertBrowser.alertService = alertService;
   }
 
   public static void setCurrentContext(BrowserContext currentContext) {
     AlertBrowser.currentContext = currentContext;
   }
 
   public static BrowserContext getCurrentContext() {
     List<Alert> curAlerts = tablePanel.getSelectedAlerts();
     currentContext.setSelectedAlerts(curAlerts);
     return currentContext;
   }
 
   public AlertTreeNode getRootNode() {
     AlertTreeNode root = null;
     try {
       String configuration = alertService.getTreeConfiguration();
       XMLContext context = new XMLContext();
       Mapping mapping = new Mapping();
       URL mappingUrl = getClass().getResource("/treeConfigMapping.xml");
       mapping.loadMapping(mappingUrl);
       context.addMapping(mapping);
       Unmarshaller unmarshaller = context.createUnmarshaller();
       root = (AlertTreeNode) unmarshaller.unmarshal(new StringReader(configuration));
     } catch (AlertscapeException e) {
       LOG.error("Couldn't get tree configuration", e);
     } catch (Exception e) {
       LOG.error("Couldn't unmarshall tree configuration", e);
     }
     return root;
   }
 
   // TODO: We have to get rid of these static methods
   public static void clear(List<Alert> alerts) {
     try {
       alertService.clear(Authentication.getUser("CEV"), alerts);
     } catch (Exception e) {
       LOG.error("Couldn't clear alerts", e);
     }
   }
 
   public static void acknowledge(List<Alert> alerts) {
     try {
       alertService.acknowledge(Authentication.getUser("CEV"), alerts);
     } catch (Exception e) {
       LOG.error("Couldn't acknowledge alerts", e);
     }
   }
 
   public static void unacknowledge(List<Alert> alerts) {
     try {
       alertService.unacknowledge(Authentication.getUser("CEV"), alerts);
     } catch (Exception e) {
       LOG.error("Couldn't acknowledge alerts", e);
     }
   }
 
   public static Map getLabels() {
     Map val = null;
     try {
       val = alertService.getLabels(Authentication.getUser("CEV"));
     } catch (Exception e) {
       LOG.error("Couldn't initialize labels", e);
       val = new HashMap();
     }
 
     return val;
   }
 
   public static void label(List<Alert> alerts, String tagName, String tagValue) {
     try {
       alertService.label(Authentication.getUser("CEV"), alerts, tagName, tagValue);
     } catch (Exception e) {
       LOG.error("Couldn't label alerts", e);
     }
   }
 
   public static Map getCategories() {
     Map val = null;
     try {
       val = alertService.getCategories(Authentication.getUser("CEV"));
     } catch (Exception e) {
       LOG.error("Couldn't initialize categories", e);
       val = new HashMap();
     }
 
     return val;
   }
 
   public static void categorize(List<Alert> alerts, String tagName, String tagValue) {
     try {
       alertService.categorize(Authentication.getUser("CEV"), alerts, tagName, tagValue);
     } catch (Exception e) {
       LOG.error("Couldn't categorize alerts", e);
     }
   }
 
   /**
    * @return the authenticationService
    */
   public AuthenticationService getAuthenticationService() {
     return authenticationService;
   }
 
   /**
    * @param authenticationService
    *          the authenticationService to set
    */
   public void setAuthenticationService(AuthenticationService authenticationService) {
     this.authenticationService = authenticationService;
     // TODO: This whole static Authentication thing is a hack and should be taken out
     Authentication.setAuthenticationService(authenticationService);
   }
 }
