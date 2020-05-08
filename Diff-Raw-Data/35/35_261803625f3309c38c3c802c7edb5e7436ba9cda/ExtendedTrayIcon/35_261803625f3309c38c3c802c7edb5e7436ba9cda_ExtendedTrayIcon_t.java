 package com.hexcoder.icarrus.ui;
 
 import com.hexcoder.icarrus.dao.ImageDAO;
 import com.hexcoder.icarrus.dao.LoggingDAO;
 import com.hexcoder.icarrus.dto.CredentialHandler;
 import com.hexcoder.icarrus.dto.MessageHandler;
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import com.hexcoder.imagelocator.*;
 import java.security.PrivateKey;
 import java.util.*;
 
 /**
  * User: 67726e
  * Date: 8/6/11
  * Time: 12:37 AM
  */
 public class ExtendedTrayIcon extends TrayIcon {
     private JPopupMenu popupMenu;                                                               // Swing popupmenu used in lieu of the AWT popup menu used by TrayIcon
     private JDialog popupDialog;
     private static JMenuItem login;
     private TrayIcon trayIcon = this;                                                           // Get a pointer to the TrayIcon so it can be removed on exit
     private LoginForm loginForm;                                                                // Form to allow the user to login
     private ControlPanelForm controlPanelForm;                                                  // Form for upload history, application settings, and application about page
     private DropForm dropForm;                                                                  // Form that accepts the user's file drops
 
     public static void setLoginStatus(String status) {login.setText(status);}
 
     public ExtendedTrayIcon(BufferedImage image) {
         super(image);
         this.setImageAutoSize(false);
         this.addMouseListener(new trayMouseListener());
 
         popupDialog = new JDialog();
         popupDialog.setAlwaysOnTop(true);
         popupDialog.setUndecorated(true);
 
         popupMenu = new JPopupMenu();
 
         // Create UI for the popupMenu
         SettingsMenuListener settingsMenuListener = new SettingsMenuListener();
         JMenu settingsMenu = new JMenu("Settings");
             JMenuItem history = new JMenuItem("History");
             history.addActionListener(settingsMenuListener);
             settingsMenu.add(history);
             JMenuItem settings = new JMenuItem("Settings");
             settings.addActionListener(settingsMenuListener);
             settingsMenu.add(settings);
             JMenuItem about = new JMenuItem("About");
             about.addActionListener(settingsMenuListener);
             settingsMenu.add(about);
         popupMenu.add(settingsMenu);
         JMenuItem calibrate = new JMenuItem("Calibrate");
         calibrate.addActionListener(new CalibrateListener());
         popupMenu.add(calibrate);
         login = new JMenuItem("Login");
         login.addActionListener(new LoginListener());
         popupMenu.add(login);
         JMenuItem exit = new JMenuItem("Exit");
         exit.addActionListener(new ExitListener());
         popupMenu.add(exit);
 
         try {
             SystemTray.getSystemTray().add(this);
         } catch (AWTException e) {
             MessageHandler.postMessage("System Tray Error", "The tray icon could not be added to your system tray.", LoggingDAO.FATAL_ERROR);
         }
 
         controlPanelForm = new ControlPanelForm();                                              // Create form to display control data
 
         dropForm = new DropForm(this.getSize(), this);                                          // Create new form to accept file drops
         dropForm.setVisible(true);
         calibrateDropForm();                                                                    // Determine the position of the TrayIcon and position the drop form over it
 
        loginForm = new LoginForm();
        loginForm.setVisible(true);

         java.util.Timer timer = new java.util.Timer();
         timer.scheduleAtFixedRate(new TimerTask() {                                             // Set a timer task to run every 1 second
             @Override
             public void run() {
                 if (!popupMenu.isVisible()) {                                                   // Check if the popupmenu is being shown
                     dropForm.toFront();                                                         // If not, bring the drop form to the front of the screen
                 }
             }
         }, 0, 1000);
     }
 
     public void showPopupMenu(MouseEvent event) {
         if (event.getButton() == 3 && popupMenu != null) {
             Dimension size = popupMenu.getPreferredSize();
             int x = event.getXOnScreen();
             int y = event.getYOnScreen() - size.height;                                         // Place the popupmenu so that it's bottom left coordinate is at the click location
 
             popupDialog.setLocation(x, y);
             popupDialog.setVisible(true);
             popupMenu.show(popupDialog.getContentPane(), 0, 0);
             popupDialog.toFront();
         }
     }
 
     private void calibrateDropForm() {
         dropForm.setVisible(false);                                                             // Hide the form so it doesn't interfere with location
 
         int width = trayIcon.getSize().width;
         int height = trayIcon.getSize().height;
         BufferedImage compare = new RandomImage(width, height,
                     RandomImage.ImageStyle.Stripe3Column).getImage();                           // Generate a random image to be used to locate the position of the tray icon
 
         try {
             Robot robot = new Robot();
             trayIcon.setImage(compare);                                                         // Temporarily set the compare image to get a screenshot
             BufferedImage base = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
             trayIcon.setImage(ImageDAO.getImage("tray_icon.png"));                              // Return the icon to the normal image
 
             LocateImage locator = new LocateImage(base, compare);
             locator.search(2);                                                                  // Search for the image with an RGB tolerance of +-2
 
             dropForm.setLocation((int)locator.getFirstOccurrence().getX(), (int)locator.getFirstOccurrence().getY());
             dropForm.setVisible(true);
             dropForm.toFront();
         } catch (Exception e) {
             MessageHandler.postMessage("Locator Error", "The tray icon could not be properly located. Please ensure it is in on the taskbar.", LoggingDAO.ERROR);
         }
     }
 
     private class trayMouseListener implements MouseListener {
         public void mouseClicked(MouseEvent event) {}
         public void mouseEntered(MouseEvent event) {}
         public void mouseExited(MouseEvent event) {}
         public void mousePressed(MouseEvent event) {}
         public void mouseReleased(MouseEvent event) {
             showPopupMenu(event);                                                               // Call action to show the Swing based popupmenu
         }
     }
 
 
 
     /****** Event Listener Classes *******/
 
     private class SettingsMenuListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             JMenuItem item = (JMenuItem)event.getSource();
             String text = item.getText();                                                       // Get the text of the item to determine which item was clicked
 
             if (text.equals("About")) {                                                         // Determine which item was clicked and switch to the appropriate panel
                 controlPanelForm.switchToTab(ControlPanelForm.ControlPanelTab.ABOUT);
             } else if (text.equals("History")) {
                 controlPanelForm.switchToTab(ControlPanelForm.ControlPanelTab.HISTORY);
             } else {
                 controlPanelForm.switchToTab(ControlPanelForm.ControlPanelTab.SETTINGS);
             }
 
             controlPanelForm.setVisible(true);
             controlPanelForm.toFront();
         }
     }
 
     private class CalibrateListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             calibrateDropForm();
         }
     }
 
     private class LoginListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             if (login.getText().equals("Login")) {
                loginForm.setVisible(true);                                                     // Display the login form
             } else {
                 if (loginForm != null) loginForm.dispose();                                     // Get rid of the current login form if one is somehow still displayed
                 login.setText("Login");                                                         // Change the text to show we are no longer logged in
 
                 CredentialHandler.setLoginStatus(false);                                        // Clear last logged in user's credentials
                 CredentialHandler.setToken("");
                 CredentialHandler.setUsername("");
                 CredentialHandler.setPassword("");
             }
         }
     }
 
     private class ExitListener implements ActionListener {
         public void actionPerformed(ActionEvent event) {
             SystemTray.getSystemTray().remove(trayIcon);                                        // Remove the TrayIcon from the system's tray
             System.exit(0);                                                                     // Clean exit
         }
     }
 }
