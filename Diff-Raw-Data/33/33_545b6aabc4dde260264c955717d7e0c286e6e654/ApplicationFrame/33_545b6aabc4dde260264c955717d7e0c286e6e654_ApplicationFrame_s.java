 package com.ribomation.droidAtScreen.gui;
 
 import com.ribomation.droidAtScreen.Application;
 import com.ribomation.droidAtScreen.cmd.Command;
 import com.ribomation.droidAtScreen.cmd.QuitCommand;
 import org.apache.log4j.Logger;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 /**
  * The main GUI window.
  */
 public class ApplicationFrame extends JFrame {
   private Logger log = Logger.getLogger(ApplicationFrame.class);
   private Application app;
   private StatusBar statusBar;
 
   private final String[] TOOLBAR = {"ImageDirectory", "-", "AdbRestart",
       "AdbReloadDevices", "-", "About"};
   private final String[] FILE_MENU = {"Quit"};
   private final String[] IMAGE_MENU = {"ImageDirectory", "ImageFormat",
       "AskBeforeScreenshot"};
   private final String[] ADB_MENU = {"AdbRestart", "AdbReloadDevices",
       "AdbExePath"};
   private final String[] OPTIONS_MENU = {"PreferredScale", "-", "HideEmulators",
       "SkipEmulator", "AskBeforeQuit", "-", "LookAndFeel", "-",
       "RemoveProperties"};
  private final String[] HELP_MENU = {"Help", "About"};
 
   public ApplicationFrame(Application app) throws HeadlessException {
     this.app = app;
   }
 
   public StatusBar getStatusBar() {
     return statusBar;
   }
 
   public void initGUI() {
     setIconImage(GuiUtil.loadIcon("device").getImage());
     setTitle(app.getInfo().getName() + ", Version " +
         app.getInfo().getVersion());
     setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
     addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent e) {
         Command.find(QuitCommand.class).execute();
       }
     });
     setJMenuBar(createMenubar());
     add(GuiUtil.createToolbar(TOOLBAR), BorderLayout.NORTH);
     add(createDevicesTable(), BorderLayout.CENTER);
     add(statusBar = new StatusBar(app), BorderLayout.SOUTH);
     pack();
     setLocationByPlatform(true);
   }
 
   protected JMenuBar createMenubar() {
     JMenuBar mb = new JMenuBar();
     mb.add(GuiUtil.createMenu("File", 'F', FILE_MENU));
     mb.add(GuiUtil.createMenu("Image", 'I', IMAGE_MENU));
     mb.add(GuiUtil.createMenu("ADB", 'A', ADB_MENU));
     mb.add(GuiUtil.createMenu("Options", 'O', OPTIONS_MENU));
     mb.add(GuiUtil.createMenu("Help", 'H', HELP_MENU));
     return mb;
   }
 
   private JComponent createDevicesTable() {
     JTable tbl = new JTable(app.getDeviceTableModel());
     tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
     tbl.setRowSelectionAllowed(true);
     tbl.setShowHorizontalLines(true);
     tbl.setFillsViewportHeight(true);
 
     JScrollPane pane = new JScrollPane(tbl);
     pane.setBorder(BorderFactory.createTitledBorder("Devices"));
 
     return pane;
   }
 }
