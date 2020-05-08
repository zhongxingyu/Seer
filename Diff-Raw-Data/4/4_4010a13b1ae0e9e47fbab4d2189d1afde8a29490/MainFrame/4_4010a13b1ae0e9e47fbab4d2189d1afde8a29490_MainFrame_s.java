 package at.tuwien.sbc.feeder.gui.frames;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import javax.swing.BoxLayout;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 
 import at.tuwien.sbc.feeder.ControllerReference;
 import at.tuwien.sbc.feeder.NotificationsThread;
 import at.tuwien.sbc.feeder.common.Constants;
 import at.tuwien.sbc.feeder.gui.panels.EventOrganizationPanel;
 import at.tuwien.sbc.feeder.gui.panels.PeerEventsPanel;
 import at.tuwien.sbc.feeder.gui.panels.SearchPanel;
 import at.tuwien.sbc.feeder.gui.panels.TabbedPanel;
 import at.tuwien.sbc.feeder.interfaces.LoginCallback;
 import at.tuwien.sbc.model.Notification;
 import at.tuwien.sbc.model.Peer;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
  * Builder, which is free for non-commercial use. If Jigloo is being used
  * commercially (ie, by a corporation, company or business for any purpose
  * whatever) then you should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details. Use of Jigloo implies
  * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
  * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
  * ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 public class MainFrame extends javax.swing.JFrame implements ActionListener, LoginCallback {
 
     private JTabbedPane tbPnl;
     private JPanel pnlOverview;
     private JMenuItem itmRemoveNotifications;
     private JMenuItem itmRead;
     private JMenuItem itmClear;
     private JMenu mnNotifications;
     private SearchPanel pnlSearch;
     private JLabel lblGreet;
     private JPanel pnlGreet;
     private JMenuItem itmLogout;
     private JMenuItem itmHelp;
     private JMenuItem itmAbout;
     private JMenuItem itmQuit;
     private JMenuItem itmLogReg;
     private JMenu jMenu2;
     private JMenu jMenu1;
     private JMenuBar menuBar;
     private JPanel pnlMain;
     private JLabel lbl2;
     private JLabel lbl;
     private JPanel pnlOrg;
 
     private TabbedPanel tabs;
     private NotificationsThread notify;
 
     public MainFrame() {
         super();
         initGUI();
     }
 
     protected void processWindowEvent(WindowEvent evt) {
         if (evt.getID() == WindowEvent.WINDOW_CLOSING) {
            this.notify.setRunning(false);
             this.callback(false);
             this.dispose();
             System.exit(0);
         }
     }
 
     private void initGUI() {
         try {
             BorderLayout thisLayout = new BorderLayout();
             // this.setDefaultCloseOperation(EXIT_ON_CLOSE);
             getContentPane().setLayout(thisLayout);
             this.setTitle("Doodle");
             ImageIcon cal = new ImageIcon(ClassLoader.getSystemResource("images/cal.png"));
             this.setIconImage(cal.getImage());
             {
                 pnlGreet = new JPanel();
                 getContentPane().add(pnlGreet, BorderLayout.NORTH);
                 pnlGreet.setLayout(null);
                 pnlGreet.setPreferredSize(new java.awt.Dimension(725, 69));
                 {
                     lblGreet = new JLabel();
                     pnlGreet.add(lblGreet);
                     lblGreet.setText("You are currently not logged in");
                     lblGreet.setBounds(12, 13, 226, 15);
                 }
                 {
                     pnlSearch = new SearchPanel();
                     pnlGreet.add(pnlSearch);
                     pnlSearch.setBounds(318, 4, 300, 40);
                     pnlSearch.getTxtSearch().setText("peer name");
                 }
             }
             {
                 menuBar = new JMenuBar();
                 setJMenuBar(menuBar);
                 {
                     jMenu1 = new JMenu();
                     menuBar.add(jMenu1);
                     jMenu1.setText("Doodle");
                     {
                         itmLogReg = new JMenuItem();
                         jMenu1.add(itmLogReg);
                         itmLogReg.setText("Login/Register");
                         itmLogReg.setActionCommand(Constants.CMD_BTN_LOGIN);
                         itmLogReg.addActionListener(this);
                     }
                     {
                         itmLogout = new JMenuItem();
                         jMenu1.add(itmLogout);
                         itmLogout.setText("Logout");
                         itmLogout.setEnabled(false);
                         itmLogout.setActionCommand(Constants.CMD_MENU_LOGOUT);
                         itmLogout.addActionListener(this);
                     }
                     {
                         itmQuit = new JMenuItem();
                         jMenu1.add(itmQuit);
                         itmQuit.setText("Quit");
                         itmQuit.setActionCommand(Constants.CMD_MENU_QUIT);
                         itmQuit.addActionListener(this);
                     }
                 }
                 {
                     mnNotifications = new JMenu();
                     menuBar.add(mnNotifications);
                     mnNotifications.setText("Notifications");
                     {
                         itmRead = new JMenuItem();
                         mnNotifications.add(itmRead);
                         itmRead.setText("Read All Notifications");
                         itmRead.setActionCommand(Constants.CMD_MENU_READ_NOTIFICATIONS);
                         itmRead.addActionListener(this);
                     }
                     {
                         itmRemoveNotifications = new JMenuItem();
                         mnNotifications.add(itmRemoveNotifications);
                         itmRemoveNotifications.setText("Delete All Notifications");
                         itmRemoveNotifications.setActionCommand(Constants.CMD_MENU_REMOVE_NOTIFICATIONS);
                         itmRemoveNotifications.addActionListener(this);
                     }
                 }
                 {
                     jMenu2 = new JMenu();
                     menuBar.add(jMenu2);
                     jMenu2.setText("Help");
                     {
                         itmAbout = new JMenuItem();
                         jMenu2.add(itmAbout);
                         itmAbout.setText("About");
                         itmAbout.setActionCommand(Constants.CMD_MENU_ABOUT);
                         itmAbout.addActionListener(this);
                     }
                     {
                         itmClear = new JMenuItem();
                         jMenu2.add(itmClear);
                         itmClear.setText("Clear");
                         itmClear.setActionCommand(Constants.CMD_MENU_CLEAR);
                         itmClear.setBounds(-43, 19, 47, 23);
                         itmClear.addActionListener(this);
                     }
                     {
                         itmHelp = new JMenuItem();
                         jMenu2.add(itmHelp);
                         itmHelp.setText("Help");
                         itmHelp.setActionCommand(Constants.CMD_MENU_HELP);
                         itmHelp.addActionListener(this);
                     }
                 }
             }
             {
                 this.tabs = new TabbedPanel();
                 this.tabs.enableTab(-1, false);
                 getContentPane().add(this.tabs, BorderLayout.CENTER);
                 tabs.setPreferredSize(new java.awt.Dimension(725, 357));
             }
 
             pack();
             this.setSize(643, 560);
         } catch (Exception e) {
             // add your error handling code here
             e.printStackTrace();
         }
     }
 
     public void actionPerformed(ActionEvent evt) {
         String cmd = evt.getActionCommand();
 
         if (cmd.equals(Constants.CMD_BTN_LOGIN)) {
             this.itmLogReg.setEnabled(false);
             LoginFrame frame = new LoginFrame();
             frame.setVisible(true);
             frame.setLocationRelativeTo(this);
             frame.setCall(this);
         }
 
         if (cmd.equals(Constants.CMD_MENU_LOGOUT)) {
             this.callback(false);
         }
 
         if (cmd.equals(Constants.CMD_MENU_QUIT)) {
             this.notify.setRunning(false);
             this.callback(false);
             this.dispose();
             System.exit(0);
         }
 
         if (cmd.equals(Constants.CMD_MENU_ABOUT)) {
             JOptionPane.showMessageDialog(this, "Doodle - by Ivan Stojkovic and Petar Petrov");
         }
 
         if (cmd.equals(Constants.CMD_MENU_HELP)) {
             JOptionPane.showMessageDialog(this, "No help! Abandon ship...");
         }
 
         if (cmd.equals(Constants.CMD_MENU_CLEAR)) {
             int response = JOptionPane.showConfirmDialog(this,
                     "Are you sure, you want to clear the whole space? All data will be lost!");
 
             if (response == JOptionPane.YES_OPTION) {
                 this.callback(false);
                 ControllerReference.getInstance().clearAll();
             }
         }
 
         if (cmd.equals(Constants.CMD_MENU_READ_NOTIFICATIONS)) {
             Peer user = ControllerReference.getInstance().getUser();
             if (user != null) {
                 Notification[] notifications = ControllerReference.getInstance().readNotifications();
                 String msg = "";
                 if (notifications.length > 0) {
                     for (Notification n : notifications) {
                         msg += n.getRegarding() + "\n";
                     }
                 } else {
                     msg = "There are no new notifications yet!";
                 }
                 JOptionPane.showMessageDialog(this, msg);
             } else {
                 JOptionPane.showMessageDialog(this, "Please log in first");
             }
         }
 
         if (cmd.equals(Constants.CMD_MENU_REMOVE_NOTIFICATIONS)) {
             Peer user = ControllerReference.getInstance().getUser();
             if (user != null) {
                 int i = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all notifications");
                 if (i == JOptionPane.YES_OPTION) {
                     ControllerReference.getInstance().takeNotifications();
                 }
             } else {
                 JOptionPane.showMessageDialog(this, "Please log in first");
             }
         }
     }
 
     public void callback(boolean loggedIn) {
         if (loggedIn) {
             this.itmLogout.setEnabled(true);
             this.itmLogReg.setEnabled(false);
             this.tabs.enableTab(-1, true);
 
             Peer user = ControllerReference.getInstance().getUser();
             if (user == null) {
                 System.err.println("MainFrame: An error must have occurred");
                 this.lblGreet.setText("Welcome");
             } else {
                 this.lblGreet.setText("Welcome " + user.getName());
                 this.startNotificationThread();
             }
 
         } else {
             this.stopNotificationThread();
             ControllerReference.getInstance().logout();
             this.itmLogout.setEnabled(false);
             this.itmLogReg.setEnabled(true);
             this.tabs.enableTab(-1, false);
             this.lblGreet.setText("You are currently not logged in!");
             this.mnNotifications.setText("Notifications");
         }
 
         EventOrganizationPanel eop = (EventOrganizationPanel) this.tabs.getTabs().getComponentAt(0);
         PeerEventsPanel pep = (PeerEventsPanel) this.tabs.getTabs().getComponentAt(1);
         eop.refreshModel();
         eop.refresh();
         pep.refresh();
 
     }
 
     public void setNotificationsCount(int count) {
         this.mnNotifications.setText("Notifications (" + count + ")");
 
     }
 
     public void stopNotificationThread() {
         if (this.notify != null) {
             this.notify.setRunning(false);
         }
     }
 
     public void startNotificationThread() {
         this.notify = new NotificationsThread(ControllerReference.getInstance().getGigaSpace(), this);
         this.notify.start();
     }
 
 }
