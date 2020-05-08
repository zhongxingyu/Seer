 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Iterator;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  *
  * @author radim
  */
 public class MainFrame extends JFrame {
 
     JMenuBar menubar;
 
     public MainFrame(Role r) {
         // UI design&layout
         setTitle("Systém SPRÁVCE");
 
         menubar = new JMenuBar();
         menubar = new JMenuBar();
         setJMenuBar(menubar);
         JMenu session = new JMenu("Session");
         menubar.add(session);
         JMenuItem changePass = new JMenuItem("Change password");
         session.add(changePass);
         JMenuItem logout = new JMenuItem("Logout");
         session.add(logout);
         JMenuItem exit = new JMenuItem("EXIT");
         session.add(exit);
 
         JMenu functions = new JMenu("Functions");
         menubar.add(functions);
         Iterator<JMenuItem> items = r.getMenuItems().iterator();
         while (items.hasNext()) {
             functions.add(items.next());
         }
 
         JMenu help = new JMenu("Help");
         menubar.add(help);
 
         help.add(Main.getAboutMenuItem());
 
         // TODO Add profile specific menus / functions
         // TODO David - Add employee profile panel
         pack();
         // UI event handling
         setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent e) {
                 logoutAndExit();
             }
         });
         exit.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 logoutAndExit();
             }
         });
         logout.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 logout();
                 setVisible(false);
                 dispose();
                 new LoginFrame().setVisible(true);
             }
         });
         changePass.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 new ChangePassDialog().setVisible(true);
             }
         });
     }
 
     private void logoutAndExit() {
         try { // since ServerConnection.sendMsg crashes in demo runs
             logout();
         } finally {
             System.exit(0);
         }
     }
 
     private void logout() {
        ServerConnection c = ServerConnection.getInstance();
        String r = c.sendMSG("LOGOUT");
        if (!r.equals("OK")) {
            System.err.println("Error logging out: " + r);
        }
     }
 
     public static void main(String[] args) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 new MainFrame(new AdminRole()).setVisible(true);
             }
         });
     }
 }
