 /*
  * File:   FrmMain.java
  * Author: David Brun <brundavid@gmail.com>
  *
  * Created on 4 sept. 2011, 16:20:28
  */
 
 package fr.davidbrun.wifiautoconnect.views;
 
 import com.apple.eawt.AboutHandler;
 import com.apple.eawt.AppEvent.AboutEvent;
 import com.apple.eawt.AppEvent.AppReOpenedEvent;
 import com.apple.eawt.AppEvent.PreferencesEvent;
 import com.apple.eawt.AppReOpenedListener;
 import com.apple.eawt.Application;
 import com.apple.eawt.PreferencesHandler;
 import fr.davidbrun.wifiautoconnect.controllers.FrmMainController;
 import fr.davidbrun.wifiautoconnect.structs.GenericMenuItem;
 import fr.davidbrun.wifiautoconnect.structs.GenericMenuItemSeparator;
 import fr.davidbrun.wifiautoconnect.structs.GenericPopupMenu;
 import fr.davidbrun.wifiautoconnect.structs.GenericTrayIcon;
 import fr.davidbrun.wifiautoconnect.utils.I18nUtil;
 import fr.davidbrun.wifiautoconnect.utils.OSUtil;
 import fr.davidbrun.wifiautoconnect.utils.ResourcesUtil;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.SystemTray;
 import java.awt.Toolkit;
 import java.awt.TrayIcon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Locale;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 
 /**
  * Represent the main window of the application
  * 
  * @author David Brun <brundavid@gmail.com>
  */
 public class FrmMain extends javax.swing.JFrame
 {
     // <editor-fold defaultstate="collapsed" desc="Fields">
     
     // Swing components
     private JButton buttonLaunchPause;
     private JButton buttonAutoManual;
     private JLabel labelProfile;
     private JLabel labelConnectionState;
     private JLabel labelAutoConnectState;
     private JLabel labelAuthMode;
     private WiFiAnimatedIcon wifiAnimatedIcon;
     private JLabel labelNotifications;
     private JLabel labelGroupSeparator;
     // Menu bar
     private JMenuBar menuBar;
     private JMenu menuFile;
     private JMenuItem menuFile_Parameters;
     private JMenuItem menuFile_Quit;
     private JMenu menuProfiles;
     private JMenu menuHelp;
     private JMenuItem menuHelp_Update;
     private JMenuItem menuHelp_Doc;
     private JMenuItem menuHelp_FAQ;
     private JMenuItem menuHelp_About;
     // The system tray
     private GenericTrayIcon systemTray;
     // The popup menu of the system tray
     private GenericPopupMenu popupMenu;
     private GenericMenuItem popupMenu_ShowApp;
     private GenericMenuItem popupMenu_PauseStart;
     private GenericMenuItem popupMenu_Profiles;
     private GenericMenuItem popupMenu_Logs;
     private GenericMenuItem popupMenu_Parameters;
     private GenericMenuItem popupMenu_Quit;
     private GenericMenuItemSeparator popupMenu_Blanc1;
     private GenericMenuItemSeparator popupMenu_Blanc2;
     private GenericMenuItemSeparator popupMenu_Blanc3;
     private GenericMenuItemSeparator popupMenu_Blanc4;
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Constructor">
     
     /**
      * Create a new main window
      */
     public FrmMain()
     {
         //Displays the menu bar on the top of the screen (mac style)
         System.setProperty("apple.laf.useScreenMenuBar", "true");
         // Build the GUI
         initComponents();
         // Load the texts
         initTextsI18n();
     }
     
     // </editor-fold>
     
     // <editor-fold defaultstate="collapsed" desc="Private methods">
     
     /**
      * This method is called from within the constructor to initialize the form
      */
     private void initComponents()
     {
         // Attributes of the window
         int windowWidth = 450;
         
         // Get the content pane
         Container pane = this.getContentPane();
         pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
         
         // The first row: wifi animated icon
         JPanel row0 = new JPanel();
         row0.setLayout(new BoxLayout(row0, BoxLayout.X_AXIS));
         wifiAnimatedIcon = new WiFiAnimatedIcon(250, FrmMainController.WINDOW_TOP_BACKGROUND);
         wifiAnimatedIcon.setPreferredSize(new Dimension(152, 252));
         row0.add(Box.createHorizontalGlue());
         row0.add(wifiAnimatedIcon);
         row0.add(Box.createHorizontalGlue());
         
         // The second row: notifications
         labelNotifications = new JLabel("Notifications ici", SwingConstants.CENTER);
         labelNotifications.setFont(new Font(labelNotifications.getFont().getFontName(), Font.ITALIC, labelNotifications.getFont().getSize()));
         labelNotifications.setForeground(Color.GRAY);
         labelNotifications.setAlignmentX(Component.CENTER_ALIGNMENT);
         labelNotifications.setAlignmentY(Component.TOP_ALIGNMENT);
         setSize(labelNotifications, new Dimension(windowWidth, 15));
         
         // The third row: profile + connection state
         JPanel row2 = new JPanel();
         row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
         labelProfile = new JLabel("SFR WiFi Public", SwingConstants.CENTER);
         labelConnectionState = new JLabel("Connecté", SwingConstants.CENTER);
         labelProfile.setFont(new Font(labelProfile.getFont().getName(), Font.BOLD, labelProfile.getFont().getSize()));
         labelConnectionState.setFont(new Font(labelConnectionState.getFont().getName(), Font.BOLD, labelConnectionState.getFont().getSize()));
         labelGroupSeparator = new JLabel("-", SwingConstants.CENTER);
         labelGroupSeparator.setFont(new Font(labelGroupSeparator.getFont().getName(), Font.BOLD, labelGroupSeparator.getFont().getSize()));
         setSize(labelGroupSeparator, new Dimension(8, 20));
         Dimension colDimension0 = new Dimension((windowWidth - 19 - labelGroupSeparator.getWidth()) / 2 - 2, 20);
         JPanel p1 = new JPanel();
         p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
         setSize(p1, colDimension0);
         JPanel p2 = new JPanel();
         p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
         setSize(p2, colDimension0);
         // Left and right columns
         p1.add(Box.createHorizontalGlue());
         p1.add(labelProfile);
         p1.add(Box.createHorizontalGlue());
         p2.add(Box.createHorizontalGlue());
         p2.add(labelConnectionState);
         p2.add(Box.createHorizontalGlue());
         // The row
         row2.add(p1);
         row2.add(labelGroupSeparator);
         row2.add(p2);
         // THe border
         row2.setBorder(BorderFactory.createEtchedBorder());
         // Set the sizes
         setSize(row2, new Dimension(windowWidth - 19, 45));
         
         // Last row: buttons
         JPanel row3 = new JPanel();
         row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
         // The last row, left column
         JPanel row3_col0 = new JPanel();
         row3_col0.setLayout(new BoxLayout(row3_col0, BoxLayout.Y_AXIS));
         buttonLaunchPause = new JButton("Marche");
         setSize(buttonLaunchPause, new Dimension(140, 35));
         buttonLaunchPause.setAlignmentX(Component.CENTER_ALIGNMENT);
         labelAutoConnectState = new JLabel("Auto Connect en pause", SwingConstants.CENTER);
         labelAutoConnectState.setForeground(Color.GRAY);
         labelAutoConnectState.setAlignmentX(Component.CENTER_ALIGNMENT);
         setSize(labelAutoConnectState, new Dimension(windowWidth / 2, 15));
         // Add components
         row3_col0.add(Box.createVerticalGlue());
         row3_col0.add(Box.createVerticalGlue());
         row3_col0.add(buttonLaunchPause);
         row3_col0.add(Box.createRigidArea(new Dimension(0, 3)));
         row3_col0.add(labelAutoConnectState);
         row3_col0.add(Box.createVerticalGlue());
         // The last row, right column
         JPanel row3_col1 = new JPanel();
         row3_col1.setLayout(new BoxLayout(row3_col1, BoxLayout.Y_AXIS));
 	buttonAutoManual = new JButton("Automatique");
         setSize(buttonAutoManual, new Dimension(140, 35));
         buttonAutoManual.setAlignmentX(Component.CENTER_ALIGNMENT);
         labelAuthMode = new JLabel("Authentification manuelle", SwingConstants.CENTER);
         labelAuthMode.setForeground(Color.GRAY);
         labelAuthMode.setAlignmentX(Component.CENTER_ALIGNMENT);
         setSize(labelAuthMode, new Dimension(windowWidth / 2, 15));
         // Add components
         row3_col1.add(Box.createVerticalGlue());
         row3_col1.add(Box.createVerticalGlue());
         row3_col1.add(buttonAutoManual);
         row3_col1.add(Box.createRigidArea(new Dimension(0, 3)));
         row3_col1.add(labelAuthMode);
         row3_col1.add(Box.createVerticalGlue());
         // A vertical border
         JLabel labelSeparator0 = new JLabel();
         Dimension labelSeparatorDimension = new Dimension(10, 70);
         setSize(labelSeparator0, labelSeparatorDimension);
         labelSeparator0.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         JLabel labelSeparator1 = new JLabel();
         setSize(labelSeparator1, new Dimension(6, 70));
         labelSeparator1.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         JLabel labelSeparator2 = new JLabel();
         setSize(labelSeparator2, labelSeparatorDimension);
         labelSeparator2.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         row3.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         // Add borders
         row3_col0.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
         row3_col1.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
         
         // Add components
         row3.add(labelSeparator0);
         row3.add(row3_col0);
         row3.add(labelSeparator1);
         row3.add(row3_col1);
         row3.add(labelSeparator2);
         // Set sizes
         Dimension colDimension1 = new Dimension(windowWidth / 2 - 2 * labelSeparator0.getWidth() / 2 - labelSeparator1.getWidth() / 2, 70);
         setSize(row3_col0, colDimension1);
         setSize(row3_col1, colDimension1);
         Dimension row3Dimension = new Dimension(windowWidth, row3.getHeight());
         row3.setMinimumSize(row3Dimension);
         row3.setSize(row3Dimension);
         row3.setMaximumSize(row3Dimension);
         // Set the color
         p1.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         p2.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         pane.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         row0.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         row2.setBackground(FrmMainController.WINDOW_TOP_BACKGROUND);
         row3_col0.setBackground(FrmMainController.WINDOW_BOTTOM_BACKGROUND);
         row3_col1.setBackground(FrmMainController.WINDOW_BOTTOM_BACKGROUND);
         
         // Add components
         pane.add(Box.createRigidArea(new Dimension(0, 15)));
         pane.add(row0);
         pane.add(Box.createRigidArea(new Dimension(0, 0)));
         pane.add(labelNotifications);
         pane.add(Box.createRigidArea(new Dimension(0, 8)));
         pane.add(row2);
         pane.add(Box.createRigidArea(new Dimension(0, 5)));
         pane.add(row3);
         pane.add(Box.createRigidArea(new Dimension(0, 10)));
         
         // Create the menu
         this.menuFile = new JMenu("Fichier");
         this.menuProfiles = new JMenu("Profils");
         this.menuHelp = new JMenu("Aide");
         this.menuFile_Parameters = new JMenuItem("Paramètres");
         this.menuFile_Quit = new JMenuItem("Quitter");
         this.menuFile.add(this.menuFile_Parameters);
         if (OSUtil.IS_WINDOWS)
             this.menuFile.addSeparator();
         else
             this.menuFile.add(new JSeparatorExt());
         this.menuFile.add(this.menuFile_Quit);
         this.menuHelp_Update = new JMenuItem("Vérifier les mises à jour");
         this.menuHelp_Doc = new JMenuItem("Documentation");
         this.menuHelp_FAQ = new JMenuItem("FAQ");
         this.menuHelp_About = new JMenuItem("À propos de WiFi Auto Connect");
         this.menuHelp.add(this.menuHelp_Update);
         if (OSUtil.IS_WINDOWS)
             this.menuHelp.addSeparator();
         else
             this.menuHelp.add(new JSeparatorExt());
         this.menuHelp.add(this.menuHelp_Doc);
         this.menuHelp.add(this.menuHelp_FAQ);
         if (!OSUtil.IS_MAC) // On mac os, the About menu is present in the application menu
         {
             if (OSUtil.IS_WINDOWS)
                 this.menuHelp.addSeparator();
             else
                 this.menuHelp.add(new JSeparatorExt());
             this.menuHelp.add(this.menuHelp_About);
         }
         this.menuBar = new JMenuBar();
         if (!OSUtil.IS_MAC) // On mac os, Preferences & Quit are already present in the application menu
             this.menuBar.add(this.menuFile);
         this.menuBar.add(this.menuProfiles);
         this.menuBar.add(this.menuHelp);
         
         // Add shortcuts (mnemonics are added when we load texts for internationalization)
         this.menuFile_Quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         this.menuHelp_Doc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
         // Add the menu bar
         this.setJMenuBar(this.menuBar);
         
         // Full integration of the application in Mac OS
         if (OSUtil.IS_MAC)
         {
             Application.getApplication().setAboutHandler(new AboutHandler()
             {
                 @Override
                 public void handleAbout(AboutEvent arg0)
                 {
                     new FrmAbout(FrmMain.this, true).setVisible(true);
                 }
             });
             Application.getApplication().setPreferencesHandler(new PreferencesHandler()
             {
                 @Override
                 public void handlePreferences(PreferencesEvent pe)
                 {
                     new FrmParameters(FrmMain.this, true).setVisible(true);
                 }
             });
             Application.getApplication().addAppEventListener(new AppReOpenedListener()
             {
                 @Override
                 public void appReOpened(AppReOpenedEvent aroe)
                 {
                     setState(FrmMain.NORMAL);
                     setVisible(true);
                 }
             });
         }
         
         // Pack before to display
         this.pack();
         
         // Add event listeners
         buttonLaunchPause.addMouseListener(new MouseAdapter()
         {
             @Override
             public void mouseClicked(MouseEvent evt)
             {
                 buttonLaunchPause_MouseClicked(evt);
             }
         });
         buttonAutoManual.addMouseListener(new MouseAdapter()
         {
             @Override
             public void mouseClicked(MouseEvent evt)
             {
                 buttonAutoManual_MouseClicked(evt);
             }
         });
         menuHelp_About.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 new FrmAbout(FrmMain.this, true).setVisible(true);
             }
         });
         menuFile_Quit.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 System.exit(0);
             }
         });
         menuFile_Parameters.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 new FrmParameters(FrmMain.this, true).setVisible(true);
             }
         });
         
         // Create the system tray
         this.addApplicationSystemTray();
         
         // Set the other properties of the window
         FrmMain.setDefaultLookAndFeelDecorated(true);
         this.setTitle(FrmMainController.APP_NAME);
         this.setResizable(false);
         this.setIconImage(ResourcesUtil.APPLICATION_IMAGE);
         // Just hide
         this.setDefaultCloseOperation(HIDE_ON_CLOSE);
         // Add the tooltip of the system tray
         this.updateSystemTrayToolTip();
         // Center the window on the screen
         this.setLocationRelativeTo(null);
     }
     
     /**
      * Handle a click on the launch / pause button
      * 
      * @param evt The mouse event args
      */
     private void buttonLaunchPause_MouseClicked(MouseEvent evt)
     {
         if (buttonLaunchPause.getText().equalsIgnoreCase(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonLaunchPause.Launch")))
         {
             buttonLaunchPause.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonLaunchPause.Pause"));
             labelAutoConnectState.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelAutoConnectState.Launched"));
         }
         else if (buttonLaunchPause.getText().equalsIgnoreCase(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonLaunchPause.Pause")))
         {
             buttonLaunchPause.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonLaunchPause.Launch"));
             labelAutoConnectState.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelAutoConnectState.Paused"));
         }
     }
     /**
      * Handle a click on the automatic / manual button
      * 
      * @param evt The mouse event args
      */
     private void buttonAutoManual_MouseClicked(MouseEvent evt)
     {
         if (buttonAutoManual.getText().equalsIgnoreCase(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonAutoManual.Auto")))
         {
             buttonAutoManual.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonAutoManual.Manual"));
             labelAuthMode.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelAuthMode.Auto"));
         }
         else if (buttonAutoManual.getText().equalsIgnoreCase(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonAutoManual.Manual")))
         {
             buttonAutoManual.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonAutoManual.Auto"));
             labelAuthMode.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelAuthMode.Manual"));
         }
     }
     
     /**
      * This method is called to change the displayed texts depending on the selected locale
      */
     private void initTextsI18n()
     {
         // Remember the different indentations to integrate better in different OS's
         String notMacLargeIndentation = (!OSUtil.IS_MAC ? "  " : "");
         String notMacSmallIndentation = (!OSUtil.IS_MAC ? " " : "");
        String linuxLargeIndentation = (OSUtil.IS_LINUX ? "     " : "");
         String linuxSmallIndentation = (OSUtil.IS_LINUX ? "     " : "");
 
         this.setLocale(Locale.FRANCE);
 //        this.setLocale(Locale.UK);
         I18nUtil.getInstance().setLocale(this.getLocale().getLanguage(), this.getLocale().getCountry());
         this.buttonAutoManual.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonAutoManual.Manual"));
         this.buttonLaunchPause.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.buttonLaunchPause.Pause"));
         this.labelAuthMode.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelAuthMode.Auto"));
         this.labelAutoConnectState.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelAutoConnectState.Launched"));
         this.labelConnectionState.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelConnectionState.Disconnected"));
         this.labelNotifications.setText(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelNotifications"));
         this.labelProfile.setForeground(Color.decode(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelProfile.Color")));
         this.labelGroupSeparator.setForeground(Color.decode(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelGroupSeparator.Color")));
         this.labelConnectionState.setForeground(Color.decode(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.labelConnectionState.Color")));
         this.menuFile.setText(notMacLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuFile") + notMacSmallIndentation);
         this.menuProfiles.setText(notMacSmallIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuProfiles") + notMacSmallIndentation);
         this.menuHelp.setText(notMacSmallIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp") + notMacSmallIndentation);
         this.menuFile_Parameters.setText(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuFile_Parameters"));
         this.menuFile_Quit.setText(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuFile_Quit"));
         this.menuHelp_Update.setText(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_Update"));
         this.menuHelp_Doc.setText(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_Doc"));
         this.menuHelp_FAQ.setText(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_FAQ"));
         this.menuHelp_About.setText(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_About"));
         this.popupMenu_ShowApp.setLabel(linuxSmallIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_ShowApp"));
         this.popupMenu_PauseStart.setLabel(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_PauseStart.Pause"));
         this.popupMenu_Profiles.setLabel(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_Profiles"));
         this.popupMenu_Logs.setLabel(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_Logs"));
         this.popupMenu_Parameters.setLabel(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_Parameters"));
         this.popupMenu_Quit.setLabel(linuxLargeIndentation + I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_Quit"));
         this.setMenuBarMnemonics();
     }
     
     /**
      * Set mnemonics to the menus of the menu bar (with the first letter of the captions)<br/>
      * No mnemonic on Mac OS
      */
     private void setMenuBarMnemonics()
     {
         if (!OSUtil.IS_MAC)
         {
             // Menus
             if (this.menuFile.getText().length() > 0)
                 this.menuFile.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuFile").charAt(0));
             if (this.menuProfiles.getText().length() > 0)
                 this.menuProfiles.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuProfiles").charAt(0));
             if (this.menuHelp.getText().length() > 0)
                 this.menuHelp.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp").charAt(0));
             // File sub menus
             if (this.menuFile_Parameters.getText().length() > 0)
                 this.menuFile_Parameters.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuFile_Parameters").charAt(0));
             if (this.menuFile_Quit.getText().length() > 0)
                 this.menuFile_Quit.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuFile_Quit").charAt(0));
             // Help sub menus
             if (this.menuHelp_Update.getText().length() > 0)
                 this.menuHelp_Update.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_Update").charAt(0));
             if (this.menuHelp_Doc.getText().length() > 0)
                 this.menuHelp_Doc.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_Doc").charAt(0));
             if (this.menuHelp_FAQ.getText().length() > 0)
                 this.menuHelp_FAQ.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_FAQ").charAt(0));
             if (this.menuHelp_About.getText().length() > 0)
                 this.menuHelp_About.setMnemonic(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.menuHelp_About").charAt(0));
         }
     }
     
     /**
      * This method is called from within the constructor to manage the system tray icon for the application
      */
     private void addApplicationSystemTray()
     {
         // Check if the OS has a system tray
         if (SystemTray.isSupported())
         {
             SystemTray tray = SystemTray.getSystemTray();
             
             // Create the popup menu of the system tray
             GenericPopupMenu.PopupMenuType popupMenuType = (OSUtil.IS_MAC ? GenericPopupMenu.PopupMenuType.AWT : GenericPopupMenu.PopupMenuType.SWING);
             this.popupMenu = new GenericPopupMenu(popupMenuType, "");
             GenericMenuItem.MenuItemType menuItemType = (OSUtil.IS_MAC ? GenericMenuItem.MenuItemType.AWT : GenericMenuItem.MenuItemType.SWING);
             GenericMenuItemSeparator.MenuItemSeparatorType menuItemSeparatorType = (OSUtil.IS_MAC ? GenericMenuItemSeparator.MenuItemSeparatorType.AWT : GenericMenuItemSeparator.MenuItemSeparatorType.SWING);
             this.popupMenu_ShowApp = new GenericMenuItem(menuItemType, "Afficher l'application");
             this.popupMenu_Blanc1 = new GenericMenuItemSeparator(menuItemSeparatorType);
             this.popupMenu_PauseStart = new GenericMenuItem(menuItemType, "Pause");
             this.popupMenu_Blanc2 = new GenericMenuItemSeparator(menuItemSeparatorType);
             this.popupMenu_Profiles = new GenericMenuItem(menuItemType, "Profils");
             this.popupMenu_Blanc3 = new GenericMenuItemSeparator(menuItemSeparatorType);
             this.popupMenu_Logs = new GenericMenuItem(menuItemType, "Consulter les logs");
             this.popupMenu_Parameters = new GenericMenuItem(menuItemType, "Paramètres");
             this.popupMenu_Blanc4 = new GenericMenuItemSeparator(menuItemSeparatorType);
             this.popupMenu_Quit = new GenericMenuItem(menuItemType, "Quitter");
             
            // Display the top item in bold only on Windows
            if (OSUtil.IS_WINDOWS)
                this.popupMenu_ShowApp.setFont(new Font(menuProfiles.getFont().getName(), Font.BOLD, menuProfiles.getFont().getSize()));
             
             this.popupMenu_Quit.addActionListener( new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     System.exit(0);
                 }
             });
             this.popupMenu_ShowApp.addActionListener( new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     setState(FrmMain.NORMAL);
                     setVisible(true);
                 }
             });
             this.popupMenu_PauseStart.addActionListener( new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     if (popupMenu_PauseStart.getLabel().equalsIgnoreCase(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_PauseStart.Start")))
                     {
                         popupMenu_PauseStart.setLabel(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_PauseStart.Pause"));
                     }
                     else if (popupMenu_PauseStart.getLabel().equalsIgnoreCase(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_PauseStart.Pause")))
                     {
                         popupMenu_PauseStart.setLabel(I18nUtil.getInstance().getI18nMsg("fr.davidbrun.wifiautoconnect.views.FrmMain.popupMenu_PauseStart.Start"));
                     }
                 }
             });
             this.popupMenu_Parameters.addActionListener( new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     new FrmParameters(FrmMain.this, true).setVisible(true);
                 }
             });
             this.popupMenu.addMenuItem(this.popupMenu_ShowApp);
             this.popupMenu.addMenuItemSeparator(this.popupMenu_Blanc1);
             this.popupMenu.addMenuItem(this.popupMenu_PauseStart);
             this.popupMenu.addMenuItemSeparator(this.popupMenu_Blanc2);
             this.popupMenu.addMenuItem(this.popupMenu_Profiles);
             this.popupMenu.addMenuItemSeparator(this.popupMenu_Blanc3);
             this.popupMenu.addMenuItem(this.popupMenu_Logs);
             this.popupMenu.addMenuItem(this.popupMenu_Parameters);
             this.popupMenu.addMenuItemSeparator(this.popupMenu_Blanc4);
             this.popupMenu.addMenuItem(this.popupMenu_Quit);
             
             // Create the system tray
             GenericTrayIcon.TrayIconType trayIconType = (OSUtil.IS_MAC ? GenericTrayIcon.TrayIconType.DEFAULT : GenericTrayIcon.TrayIconType.CUSTOM);
             systemTray = new GenericTrayIcon(trayIconType,
                     ResourcesUtil.SYSTEM_TRAY_IMAGE_ICON.getImage(),
                     FrmMainController.APP_NAME,
                     this.popupMenu.getCurrentPopupMenu());
             
             //To catch events on the popup menu
             systemTray.setImageAutoSize(true);
             systemTray.addActionListener(new ActionListener()
             {
                 @Override
                 public void actionPerformed(ActionEvent e)
                 {
                     // TODO: manage double-click
                 }
             });
             
             try
             {
                 if (OSUtil.IS_MAC)
                     tray.add((TrayIcon)systemTray.getCurrentTrayIcon());
                 else
                     tray.add((JTrayIcon)systemTray.getCurrentTrayIcon());
             }
             catch (Exception e)
             { }
         }
         else // If there is no system tray, we don't do anything
         { }
     }
     
     /**
      * Update the tool tip of the system tray depending on the state of the connection
      */
     private void updateSystemTrayToolTip()
     {
         if (OSUtil.IS_LINUX)
             this.systemTray.setToolTip("SFR WiFi Public : Connecté");
         else
             this.systemTray.setToolTip(FrmMainController.APP_NAME + " " + FrmMainController.APP_VERSION + FrmMainController.LINE_SEPARATOR + "SFR WiFi Public : Connecté");
     }
     
     /**
      * Fix the size of a swing component
      * 
      * @param target The component to fix the size
      * @param d The new size of the component
      */
     private void setSize(JComponent target, Dimension d)
     {
         target.setMinimumSize(d);
         target.setMaximumSize(d);
         target.setPreferredSize(d);
         target.setSize(d);
     }
     
     // </editor-fold>
 }
