 package ch9k.core.gui;
 
 import ch9k.chat.gui.ContactListView;
 import ch9k.core.ChatApplication;
 import ch9k.core.I18n;
 import ch9k.core.settings.ProxyPrefPane;
 import ch9k.core.settings.event.PreferencePaneEvent;
 import ch9k.core.settings.gui.PreferencesFrame;
 import ch9k.eventpool.EventPool;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.util.LinkedList;
 import java.util.Queue;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import org.apache.log4j.Logger;
 
 /**
  * Main application window
  * @author Pieter De Baets
  */
 public class ApplicationWindow extends JFrame {
     /**
      * Logger.
      */
     private static final Logger logger =
             Logger.getLogger(ApplicationWindow.class);
 
     private JPanel panel;
     private JLabel statusBar;
     private ContactListView contactList;
     private AccountPanel account;
     private JMenuBar menuBar;
     private PreferencesFrame prefFrame;
 
     /**
      * Create a new window
      */
     public ApplicationWindow() {
         super("ChatHammer 9000");
         // @TODO: check how this works out on mac (where windows can be 'hidden')
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         setPreferredSize(new Dimension(300, 520));
         setMinimumSize(new Dimension(300, 200));
         setSize(getPreferredSize());
         setLocationByPlatform(true);
 
         initSettings();
     }
 
     @Override
     public void dispose() {
         super.dispose();
        if(prefFrame != null) {
            prefFrame.dispose();
        }
     }
 
     private void initSettings() {
         // use a native look and feel
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception ex) {
             logger.warn("Unable to load native look & feel");
         }
 
         // set the menu bar on top of the screen on mac
         System.setProperty("apple.laf.useScreenMenuBar", "true");
     }
 
     /**
      * Init the 'real' application view, after login has completed
      */
     public void initApplicationView() {
         prefFrame = new PreferencesFrame();
         EventPool.getAppPool().raiseEvent(new PreferencePaneEvent(
                 I18n.get("ch9k.core", "proxy_title"),
                 new ProxyPrefPane(ChatApplication.getInstance().getSettings()))
         );
 
         // Request the plugin manager here, so it will add preference panes
         // for the different plugins.
         ChatApplication.getInstance().getPluginManager();
 
         panel = new JPanel();
         panel.setLayout(new BorderLayout());
 
         account = new AccountPanel();
         panel.add(account, BorderLayout.NORTH);
 
         contactList = new ContactListView();
         contactList.setBackground(getBackground());
         panel.add(contactList, BorderLayout.CENTER);
 
         statusBar = new JLabel();
         statusBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         panel.add(statusBar, BorderLayout.SOUTH);
         
         initMenu();
 
         setContentPane(panel);
         setVisible(true);
         repaint();
     }
 
     private void initMenu() {
         menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         
         // file-menu
         JMenu fileMenu = new JMenu(I18n.get("ch9k.core", "file"));
         fileMenu.add(new AbstractAction(I18n.get("ch9k.core", "preferences")){
             public void actionPerformed(ActionEvent e) {
                 prefFrame.setVisible(true);
             }
         });
         fileMenu.addSeparator();
         fileMenu.add(new AbstractAction(I18n.get("ch9k.core", "log_off")) {
             public void actionPerformed(ActionEvent e) {
                 ChatApplication.getInstance().logoff(true);
             }
         });
         fileMenu.add(new AbstractAction(I18n.get("ch9k.core", "exit")) {
             public void actionPerformed(ActionEvent e) {
                 EventQueue.invokeLater(new Runnable() {
                     public void run() {
                         ApplicationWindow.this.dispose();
                     }
                 });
             }
         });
         menuBar.add(fileMenu);
 
         // contacts-menu
         JMenu contactsMenu = new JMenu(I18n.get("ch9k.core", "contacts"));
         contactsMenu.add(new ContactListView.AddContactAction(this));
         contactsMenu.addSeparator();
         contactList.initMenu(contactsMenu);
         menuBar.add(contactsMenu);
 
         // window-menu
         menuBar.add(new WindowMenu(this));
 
         // about-menu
         JMenu aboutMenu = new JMenu(I18n.get("ch9k.core", "about"));
         aboutMenu.add(new AbstractAction(I18n.get("ch9k.core", "about")) {
             public void actionPerformed(ActionEvent e) {
                 new AboutDialog(ApplicationWindow.this);
             }
         });
         menuBar.add(aboutMenu);
     }
 
     private Queue<String> statusQueue = new LinkedList<String>();
 
     public synchronized void setStatus(String status) {
         if(status != null) {
             statusQueue.add(status);
         }
 
         if(statusQueue.size() == 1) {
             refreshStatus();
         }
     }
 
     private synchronized void refreshStatus() {
         if(!EventQueue.isDispatchThread()) {
             EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     refreshStatus();
                 }
             });
         } else {
             String status = null;
             if(statusBar != null) {
                 status = statusQueue.poll();
                 statusBar.setText(status);
             }
 
             if(status != null || statusQueue.size() > 0) {
                 new Thread(new Runnable() {
                     public void run() {
                         try {
                             Thread.sleep(3000);
                         } catch(InterruptedException ex) {}
 
                         refreshStatus();
                     }
                 }).start();
             }
         }
     }
 }
