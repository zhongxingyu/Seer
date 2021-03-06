 /*
  * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.ui.swing;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.IconManager;
 import com.dmdirc.Main;
 import com.dmdirc.ServerManager;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.Plugin;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.PluginManager;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.interfaces.FrameManager;
 import com.dmdirc.ui.interfaces.FramemanagerPosition;
 import com.dmdirc.ui.interfaces.MainWindow;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.ui.swing.components.Frame;
 import com.dmdirc.ui.swing.components.InputFrame;
 import com.dmdirc.ui.swing.components.SwingStatusBar;
 import com.dmdirc.ui.swing.dialogs.FeedbackDialog;
 import com.dmdirc.ui.swing.dialogs.NewServerDialog;
 import com.dmdirc.ui.swing.dialogs.PluginDialog;
 import com.dmdirc.ui.swing.dialogs.PreferencesDialog;
 import com.dmdirc.ui.swing.dialogs.about.AboutDialog;
 import com.dmdirc.ui.swing.dialogs.actionseditor.ActionsManagerDialog;
 import com.dmdirc.ui.swing.dialogs.aliases.AliasManagerDialog;
 import com.dmdirc.ui.swing.dialogs.profiles.ProfileManagerDialog;
 import com.dmdirc.ui.swing.framemanager.buttonbar.ButtonBar;
 import com.dmdirc.ui.swing.framemanager.ctrltab.CtrlTabFrameManager;
 import com.dmdirc.ui.swing.framemanager.tree.TreeFrameManager;
 import com.dmdirc.ui.swing.framemanager.windowmenu.WindowMenuFrameManager;
 import static com.dmdirc.ui.swing.UIUtilities.SMALL_BORDER;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.MouseInfo;
 import java.awt.PointerInfo;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.beans.PropertyVetoException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 
 /**
  * The main application frame.
  */
 public final class MainFrame extends JFrame implements WindowListener,
         ActionListener, MainWindow, ConfigChangeListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 9;
     /** The number of pixels each new internal frame is offset by. */
     private static final int FRAME_OPENING_OFFSET = 30;
     /** Whether the internal frames are maximised or not. */
     private boolean maximised;
     /** The current number of pixels to displace new frames in the X
      * direction. */
     private int xOffset;
     /** The current number of pixels to displace new frames in the Y
      * direction. */
     private int yOffset;
     /** The main application icon. */
     private final ImageIcon imageIcon;
     /** The frame manager that's being used. */
     private FrameManager mainFrameManager;
     /** Dekstop pane. */
     private JDesktopPane desktopPane;
     /** Plugin menu item. */
     private JMenu pluginsMenu;
     /** Windows menu item. */
     private JMenu windowsMenu;
     /** Main panel. */
     private JPanel frameManagerPanel;
     /** Add server menu item. */
     private JMenuItem miAddServer;
     /** Preferences menu item. */
     private JMenuItem miPreferences;
     /** Toggle state menu item. */
     private JMenuItem toggleStateMenuItem;
     /** Frame manager position. */
     private FramemanagerPosition position;
     /** Plugins list. */
     private final Map<JMenuItem, String> pluginList;
     /** Show version? */
     private boolean showVersion;
 
     /**
      * Creates new form MainFrame.
      *
      * @param statusBar The status bar to use.
      */
     protected MainFrame(final SwingStatusBar statusBar) {
         super();
 
         pluginList =
                 new HashMap<JMenuItem, String>();
         
         initComponents(statusBar);
         initKeyHooks();
 
         setTitle(getTitlePrefix());
 
         imageIcon =
                 new ImageIcon(IconManager.getIconManager().getImage("icon"));
         setIconImage(imageIcon.getImage());
 
         // Get the Location of the mouse pointer
         final PointerInfo myPointerInfo = MouseInfo.getPointerInfo();
         // Get the Device (screen) the mouse pointer is on
         final GraphicsDevice myDevice = myPointerInfo.getDevice();
         // Get the configuration for the device
         final GraphicsConfiguration myGraphicsConfig =
                 myDevice.getDefaultConfiguration();
         // Get the bounds of the device
         final Rectangle gcBounds = myGraphicsConfig.getBounds();
         // Calculate the center of the screen
         // gcBounds.x and gcBounds.y give the co ordinates where the screen
         // starts. gcBounds.width and gcBounds.height return the size in pixels
         // of the screen.
         final int xPos = gcBounds.x + ((gcBounds.width - getWidth()) / 2);
         final int yPos = gcBounds.y + ((gcBounds.height - getHeight()) / 2);
         // Set the location of the window
         setLocation(xPos, yPos);
 
         setVisible(true);
 
         miAddServer.addActionListener(new ActionListener() {
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent actionEvent) {
                 NewServerDialog.showNewServerDialog();
             }
         });
 
         miPreferences.addActionListener(new ActionListener() {
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent actionEvent) {
                 PreferencesDialog.showPreferencesDialog();
             }
         });
 
         toggleStateMenuItem.addActionListener(new ActionListener() {
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent actionEvent) {
                 try {
                     getActiveFrame().setMaximum(!getActiveFrame().isMaximum());
                 } catch (PropertyVetoException ex) {
                     Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
                 }
             }
         });
 
         addWindowListener(this);
 
         checkWindowState();
 
         showVersion =
                 IdentityManager.getGlobalConfig().
                 getOptionBool("ui", "showversion", false);
         IdentityManager.getGlobalConfig().
                 addChangeListener("ui", "showversion", this);
     }
 
     /** {@inheritDoc}. */
     @Override
     public void addChild(final Window window) {
         final JInternalFrame frame = (JInternalFrame) window;
 
         // Add the frame
         desktopPane.add(frame);
 
         // Make sure it'll fit with our offsets
         if (frame.getWidth() + xOffset > desktopPane.getWidth()) {
             xOffset = 0;
         }
         if (frame.getHeight() + yOffset > desktopPane.getHeight()) {
             yOffset = 0;
         }
 
         // Position the frame
         frame.setLocation(xOffset, yOffset);
         frame.moveToFront();
 
         // Increase the offsets
         xOffset += FRAME_OPENING_OFFSET;
         yOffset += FRAME_OPENING_OFFSET;
     }
 
     /** {@inheritDoc}. */
     @Override
     public void delChild(final Window window) {
         if (desktopPane.getAllFrames().length == 1) {
             setTitle(getTitlePrefix());
         } else {
             setActiveFrame((Window) desktopPane.selectFrame(true));
         }
         desktopPane.remove((JInternalFrame) window);
     }
 
     /** {@inheritDoc}. */
     @Override
     public void setActiveFrame(final Window frame) {
         if (frame != null) {
             try {
                 ((JInternalFrame) frame).setVisible(true);
                 ((JInternalFrame) frame).setIcon(false);
                 ((JInternalFrame) frame).moveToFront();
                 ((JInternalFrame) frame).setSelected(true);
             } catch (PropertyVetoException ex) {
                 Logger.userError(ErrorLevel.LOW, "Unable to set active window");
             }
 
             if (maximised) {
                 setTitle(getTitlePrefix() + " - " + frame.getTitle());
             }
 
             ActionManager.processEvent(CoreActionType.CLIENT_FRAME_CHANGED, null,
                     frame.getContainer());
         }
         if (frame instanceof InputFrame) {
             ((InputFrame) frame).requestInputFieldFocus();
         }
     }
 
     /**
      * Returns the size of the frame manager.
      *
      * @return Frame manager size.
      */
     public int getFrameManagerSize() {
         if (position == FramemanagerPosition.LEFT ||
                 position == FramemanagerPosition.RIGHT) {
             return frameManagerPanel.getWidth();
         } else {
             return frameManagerPanel.getHeight();
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public ImageIcon getIcon() {
         return imageIcon;
     }
 
     /** {@inheritDoc}. */
     @Override
     public Window getActiveFrame() {
         if (desktopPane.getSelectedFrame() instanceof Window) {
             return (Window) desktopPane.getSelectedFrame();
         } else {
             return null;
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public void addPluginMenu(final JMenuItem menuItem) {
         if (pluginsMenu.getComponents().length == 1) {
             final JSeparator seperator = new JSeparator();
             pluginsMenu.add(seperator);
         }
 
         pluginsMenu.add(menuItem);
     }
 
     /** {@inheritDoc}. */
     @Override
     public void removePluginMenu(final JMenuItem menuItem) {
         pluginsMenu.remove(menuItem);
 
         if (pluginsMenu.getComponents().length == 2) {
             pluginsMenu.remove(2);
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public void setMaximised(final boolean max) {
         maximised = max;
 
         if (max) {
             if (getActiveFrame() != null) {
                 setTitle(getTitlePrefix() + " - " + getActiveFrame().getTitle());
             }
         } else {
             setTitle(getTitlePrefix());
             for (JInternalFrame frame : desktopPane.getAllFrames()) {
                 try {
                     frame.setMaximum(false);
                 } catch (PropertyVetoException ex) {
                     Logger.userError(ErrorLevel.LOW, "Unable to maximise window");
                 }
             }
         }
 
         checkWindowState();
     }
 
     /** {@inheritDoc}. */
     @Override
     public String getTitlePrefix() {
         if (showVersion) {
             return "DMDirc " + Main.VERSION;
         } else {
             return "DMDirc";
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public boolean getMaximised() {
         return maximised;
     }
     
     /**
      * Returns the desktop pane for the frame.
      * 
      * @return JDesktopPane for the frame
      */
     public JDesktopPane getDesktopPane() {
         return desktopPane;
     }
 
     /**
      * Checks the current state of the internal frames, and configures the
      * window menu to behave appropriately.
      */
     private void checkWindowState() {
         if (getActiveFrame() == null) {
             toggleStateMenuItem.setEnabled(false);
         } else {
             toggleStateMenuItem.setEnabled(true);
         }
 
         if (maximised) {
             toggleStateMenuItem.setText("Restore");
             toggleStateMenuItem.setMnemonic('r');
             toggleStateMenuItem.invalidate();
         } else {
             toggleStateMenuItem.setText("Maximise");
             toggleStateMenuItem.setMnemonic('m');
             toggleStateMenuItem.invalidate();
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public void windowOpened(final WindowEvent windowEvent) {
         //ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowClosing(final WindowEvent windowEvent) {
         quit();
     }
 
     /** {@inheritDoc}. */
     @Override
     public void windowClosed(final WindowEvent windowEvent) {
         //ignore
     }
 
     /** {@inheritDoc}. */
     @Override
     public void windowIconified(final WindowEvent windowEvent) {
         ActionManager.processEvent(CoreActionType.CLIENT_MINIMISED, null);
     }
 
     /** {@inheritDoc}. */
     @Override
     public void windowDeiconified(final WindowEvent windowEvent) {
         ActionManager.processEvent(CoreActionType.CLIENT_UNMINIMISED, null);
     }
 
     /** {@inheritDoc}. */
     @Override
     public void windowActivated(final WindowEvent windowEvent) {
         //ignore
     }
 
     /** {@inheritDoc}. */
     @Override
     public void windowDeactivated(final WindowEvent windowEvent) {
         //ignore
     }
 
     /** Initialiases the frame managers. */
     private void initFrameManagers() {
         final String manager =
                 IdentityManager.getGlobalConfig().
                 getOption("ui", "framemanager", "treeview");
 
         final FrameManager frameManager;
         if (manager.equalsIgnoreCase("buttonbar")) {
             frameManager = new ButtonBar();
             WindowManager.addFrameManager(frameManager);
         } else {
             frameManager = new TreeFrameManager();
             WindowManager.addFrameManager(frameManager);
         }
         mainFrameManager = frameManager;
         frameManager.setParent(frameManagerPanel);
 
         WindowManager.addFrameManager(new CtrlTabFrameManager(desktopPane));
         WindowManager.addFrameManager(new WindowMenuFrameManager());
     }
 
     /**
      * Initialises the components for this frame.
      *
      * @param statusBar The status bar to use
      */
     private void initComponents(final SwingStatusBar statusBar) {
         final JSplitPane mainSplitPane = new JSplitPane();
         final JPanel panel = new JPanel();
         
         frameManagerPanel = new JPanel();
         desktopPane = new JDesktopPane();
         desktopPane.setBackground(new Color(238, 238, 238));
         
         initFrameManagers();
 
         initSplitPane(mainSplitPane);
 
         initMenuBar();
 
         setPreferredSize(new Dimension(800, 600));
 
         panel.setLayout(new BorderLayout());
         panel.add(mainSplitPane, BorderLayout.CENTER);
         panel.add(statusBar, BorderLayout.SOUTH);
         
         getContentPane().setLayout(new BorderLayout(0, 0));
         
         getContentPane().add(panel, BorderLayout.CENTER);
 
         setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 
         setTitle("DMDirc");
         desktopPane.setBorder(UIManager.getBorder("TextField.border"));
 
         pack();
     }
 
     /**
      * Initialises the split pane.
      *
      * @param mainSplitPane JSplitPane to initialise
      */
     private void initSplitPane(final JSplitPane mainSplitPane) {
         mainSplitPane.setBorder(null);
 
         mainSplitPane.setBorder(BorderFactory.createEmptyBorder(SMALL_BORDER,
                 SMALL_BORDER, 0, SMALL_BORDER));
 
         mainSplitPane.setDividerSize(SMALL_BORDER);
         mainSplitPane.setOneTouchExpandable(false);
 
         position =
                 FramemanagerPosition.getPosition(IdentityManager.getGlobalConfig().
                 getOption("ui", "framemanagerPosition"));
 
         if (position == FramemanagerPosition.UNKNOWN) {
             position = FramemanagerPosition.LEFT;
         }
 
         if (!mainFrameManager.canPositionVertically() &&
                 (position == FramemanagerPosition.LEFT ||
                 position == FramemanagerPosition.RIGHT)) {
             position = FramemanagerPosition.BOTTOM;
         }
         if (!mainFrameManager.canPositionHorizontally() &&
                 (position == FramemanagerPosition.TOP ||
                 position == FramemanagerPosition.BOTTOM)) {
             position = FramemanagerPosition.LEFT;
         }
 
         switch (position) {
             case TOP:
                 mainSplitPane.setTopComponent(frameManagerPanel);
                 mainSplitPane.setBottomComponent(desktopPane);
                 mainSplitPane.setResizeWeight(0.0);
                 mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                 frameManagerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
                         IdentityManager.getGlobalConfig().
                         getOptionInt("ui", "frameManagerSize", 50)));
                 frameManagerPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE,
                         50));
                 desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                         Integer.MAX_VALUE));
                 desktopPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 300));
                 break;
             case LEFT:
                 mainSplitPane.setLeftComponent(frameManagerPanel);
                 mainSplitPane.setRightComponent(desktopPane);
                 mainSplitPane.setResizeWeight(0.0);
                 mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                 frameManagerPanel.setPreferredSize(new Dimension(IdentityManager.getGlobalConfig().
                         getOptionInt("ui", "frameManagerSize", 150),
                         Integer.MAX_VALUE));
                 frameManagerPanel.setMinimumSize(new Dimension(150,
                         Integer.MAX_VALUE));
                 desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                         Integer.MAX_VALUE));
                 desktopPane.setMinimumSize(new Dimension(300,
                         Integer.MAX_VALUE));
                 break;
             case BOTTOM:
                 mainSplitPane.setTopComponent(desktopPane);
                 mainSplitPane.setBottomComponent(frameManagerPanel);
                 mainSplitPane.setResizeWeight(1.0);
                 mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                 frameManagerPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,
                         IdentityManager.getGlobalConfig().
                         getOptionInt("ui", "frameManagerSize", 50)));
                 frameManagerPanel.setMinimumSize(new Dimension(Integer.MAX_VALUE,
                         50));
                 desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                         Integer.MAX_VALUE));
                 desktopPane.setMinimumSize(new Dimension(Integer.MAX_VALUE, 300));
                 break;
             case RIGHT:
                 mainSplitPane.setLeftComponent(desktopPane);
                 mainSplitPane.setRightComponent(frameManagerPanel);
                 mainSplitPane.setResizeWeight(1.0);
                 mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                 frameManagerPanel.setPreferredSize(new Dimension(IdentityManager.getGlobalConfig().
                         getOptionInt("ui", "frameManagerSize", 50),
                         Integer.MAX_VALUE));
                 frameManagerPanel.setMinimumSize(new Dimension(50,
                         Integer.MAX_VALUE));
                 desktopPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                         Integer.MAX_VALUE));
                 desktopPane.setMinimumSize(new Dimension(300,
                         Integer.MAX_VALUE));
                 break;
             default:
                 break;
         }
 
         mainSplitPane.setContinuousLayout(true);
     }
 
     /** Initialises the key hooks. */
     private void initKeyHooks() {
         final KeyStroke[] keyStrokes = new KeyStroke[12];
 
         keyStrokes[0] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
         keyStrokes[1] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
         keyStrokes[2] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
         keyStrokes[3] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
         keyStrokes[4] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
         keyStrokes[5] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
         keyStrokes[6] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
         keyStrokes[7] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
         keyStrokes[8] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
         keyStrokes[9] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
         keyStrokes[10] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
         keyStrokes[11] =
                 KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
 
         for (final KeyStroke keyStroke : keyStrokes) {
             getRootPane().getActionMap().
                     put(KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action",
                     new AbstractAction(KeyEvent.getKeyText(keyStroke.getKeyCode()) +
                     "Action") {
 
                 private static final long serialVersionUID = 5;
 
                 /** {@inheritDoc} */
                 @Override
                 public void actionPerformed(final ActionEvent evt) {
                     ActionManager.processEvent(CoreActionType.CLIENT_FKEY_PRESSED,
                             null,
                             KeyStroke.getKeyStroke(keyStroke.getKeyCode(),
                             evt.getModifiers()));
                 }
             });
             getRootPane().
                     getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                     put(KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0),
                     KeyEvent.getKeyText(keyStroke.getKeyCode()) + "Action");
         }
     }
 
     /** Initialises the menu bar. */
     private void initMenuBar() {
         final JMenuBar menuBar = new JMenuBar();
         JMenuItem menuItem;
 
         final JMenu fileMenu = new JMenu();
         miAddServer = new JMenuItem();
         miPreferences = new JMenuItem();
         final JMenu settingsMenu = new JMenu();
         final JMenu helpMenu = new JMenu();
         toggleStateMenuItem = new JMenuItem();
 
         settingsMenu.setText("Settings");
         settingsMenu.setMnemonic('e');
 
         fileMenu.setMnemonic('s');
         fileMenu.setText("Server");
 
         miAddServer.setText("New Server...");
         miAddServer.setMnemonic('n');
         fileMenu.add(miAddServer);
 
         miPreferences.setText("Preferences");
         miPreferences.setMnemonic('p');
         settingsMenu.add(miPreferences);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('m');
         menuItem.setText("Profile Manager");
         menuItem.setActionCommand("Profile");
         menuItem.addActionListener(this);
         settingsMenu.add(menuItem);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('a');
         menuItem.setText("Actions Manager");
         menuItem.setActionCommand("Actions");
         menuItem.addActionListener(this);
         settingsMenu.add(menuItem);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('l');
         menuItem.setText("Alias Manager");
         menuItem.setActionCommand("Aliases");
         menuItem.addActionListener(this);
         settingsMenu.add(menuItem);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('x');
         menuItem.setText("Exit");
         menuItem.setActionCommand("Exit");
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         populateWindowMenu(new HashMap<FrameContainer, JMenuItem>());
 
         helpMenu.setMnemonic('h');
         helpMenu.setText("Help");
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('a');
         menuItem.setText("About");
         menuItem.setActionCommand("About");
         menuItem.addActionListener(this);
         helpMenu.add(menuItem);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('j');
         menuItem.setText("Join Dev channel");
         menuItem.setActionCommand("JoinDevChat");
         menuItem.addActionListener(this);
         helpMenu.add(menuItem);
         
         menuItem = new JMenuItem();
         menuItem.setMnemonic('f');
         menuItem.setText("Send Feedback");
         menuItem.setActionCommand("feedback");
         menuItem.addActionListener(this);
         helpMenu.add(menuItem);
 
         pluginsMenu = new JMenu("Plugins");
         pluginsMenu.setMnemonic('p');
         settingsMenu.add(pluginsMenu);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('m');
         menuItem.setText("Manage plugins");
         menuItem.setActionCommand("ManagePlugins");
         menuItem.addActionListener(this);
         pluginsMenu.add(menuItem);
 
         final JMenu configureMenu = new JMenu("Configure plugins");
         configureMenu.setMnemonic('c');
         pluginsMenu.add(configureMenu);
        pluginsMenu.addMenuListener(new MenuListener() {
 
             @Override
             public void menuSelected(final MenuEvent e) {
                 populateConfigurePluginsMenu(configureMenu);
             }
 
             @Override
             public void menuDeselected(final MenuEvent e) {
                 //Ignore
             }
 
             @Override
             public void menuCanceled(final MenuEvent e) {
                 //Ignore
             }
         });
 
         menuBar.add(fileMenu);
         menuBar.add(settingsMenu);
         menuBar.add(windowsMenu);
         menuBar.add(helpMenu);
 
         setJMenuBar(menuBar);
     }
 
     /**
      * Populated the configure plugin menu.
      *
      * @param menu Menu to populate
      */
     private void populateConfigurePluginsMenu(final JMenu menu) {
         pluginList.clear();
         menu.removeAll();
 
         for (PluginInfo pluginInfo : PluginManager.getPluginManager().getPluginInfos()) {
             if (pluginInfo.isLoaded()) {
                 Plugin plugin = pluginInfo.getPlugin();
                 if (plugin.isConfigurable()) {
                     final JMenuItem mi = new JMenuItem(pluginInfo.getNiceName());
                     mi.setActionCommand("configurePlugin");
                     mi.addActionListener(this);
                     menu.add(mi);
                     pluginList.put(mi, pluginInfo.getFilename());
                 }
             }
         }
         
         if (menu.getItemCount() == 0) {
             menu.setEnabled(false);
        } else {
            menu.setEnabled(true);
         }
     }
 
     /**
      * Initialises the window menu.
      *
      * @param windows Map of windows
      */
     public void populateWindowMenu(final Map<FrameContainer, JMenuItem> windows) {
         if (windowsMenu == null) {
             windowsMenu = new JMenu();
         }
         if (windowsMenu.isShowing()) {
             windowsMenu.setSelected(false);
             windowsMenu.setPopupMenuVisible(false);
         }
         windowsMenu.removeAll();
 
         JMenuItem menuItem;
         final Collection<JMenuItem> values = windows.values();
 
         windowsMenu.setMnemonic('w');
         windowsMenu.setText("Window");
 
         if (values.isEmpty()) {
             final JMenuItem mi = new JMenuItem("No windows");
             mi.setEnabled(false);
             windowsMenu.add(mi);
             return;
         }
 
         checkWindowState();
         windowsMenu.add(toggleStateMenuItem);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('n');
         menuItem.setText("Minimise");
         menuItem.setActionCommand("Minimise");
         menuItem.addActionListener(this);
         windowsMenu.add(menuItem);
 
         menuItem = new JMenuItem();
         menuItem.setMnemonic('c');
         menuItem.setText("Close");
         menuItem.setActionCommand("Close");
         menuItem.addActionListener(this);
         windowsMenu.add(menuItem);
 
         windowsMenu.addSeparator();
 
         addToMenu(windowsMenu, values.iterator(),
                 (int) (windowsMenu.getX()
         + windowsMenu.getPreferredSize().getHeight()
         + windowsMenu.getPopupMenu().getPreferredSize().getHeight()));
     }
 
     /**
      * Adds the JMenuItems in the Iterator to the JMenu specified.
      *
      * @param menu Menu to add items to
      * @param it JMenuItem iterator
      * @param location X Location of the menu on screen
      */
     private void addToMenu(final JMenu menu,
             final Iterator<JMenuItem> it, final int location) {
         while (it.hasNext()) {
             if (location + menu.getPopupMenu().getPreferredSize().getHeight() >
                     Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
                 final JMenu subMenu = new JMenu("More ->");
                 menu.add(subMenu);
                 addToMenu(subMenu, it, 0);
                 break;
             }
             menu.add(it.next());
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public void quit() {
         if (IdentityManager.getGlobalConfig().
                 getOptionBool("ui", "confirmQuit", false) &&
                 JOptionPane.showConfirmDialog(this,
                 "You are about to quit DMDirc, are you sure?", "Quit confirm",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.WARNING_MESSAGE) !=
                 JOptionPane.YES_OPTION) {
             return;
         }
         ServerManager.getServerManager().
                 closeAll(IdentityManager.getGlobalConfig().
                 getOption("general", "closemessage"));
         IdentityManager.getConfigIdentity().
                 setOption("ui", "frameManagerSize",
                 String.valueOf(this.getFrameManagerSize()));
         Main.quit();
     }
 
     /** {@inheritDoc}. */
     @Override
     public void setVisible(final boolean visible) {
         //NOPMD
         super.setVisible(visible);
     }
 
     /** {@inheritDoc}. */
     @Override
     public boolean isVisible() {
         //NOPMD
         return super.isVisible();
     }
 
     /** {@inheritDoc}. */
     @Override
     public void setTitle(final String newTitle) {
         //NOPMD
         super.setTitle(newTitle);
     }
 
     /** {@inheritDoc}. */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (e.getActionCommand().equals("About")) {
             AboutDialog.showAboutDialog();
         } else if (e.getActionCommand().equals("Profile")) {
             ProfileManagerDialog.showProfileManagerDialog();
         } else if (e.getActionCommand().equals("Exit")) {
             quit();
         } else if (e.getActionCommand().equals("ManagePlugins")) {
             PluginDialog.showPluginDialog();
         } else if (e.getActionCommand().equals("Actions")) {
             ActionsManagerDialog.showActionsManagerDialog();
         } else if (e.getActionCommand().equals("Aliases")) {
             AliasManagerDialog.showAliasManagerDialog();
         } else if (e.getActionCommand().equals("Minimise")) {
             ((Frame) Main.getUI().getMainWindow().getActiveFrame()).minimise();
         } else if (e.getActionCommand().equals("Close")) {
             ((Frame) Main.getUI().getMainWindow().getActiveFrame()).close();
         } else if (e.getActionCommand().equals("JoinDevChat")) {
             ServerManager.getServerManager().joinDevChat();
         } else if (e.getActionCommand().equals("configurePlugin")) {
             PluginManager.getPluginManager().
                     getPluginInfo(pluginList.get(e.getSource())).getPlugin().
                     showConfig();
         } else if (e.getActionCommand().equals("feedback")) {
             FeedbackDialog.showFeedbackDialog();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         showVersion =
                 IdentityManager.getGlobalConfig().
                 getOptionBool("ui", "showversion", false);
     }
 }
