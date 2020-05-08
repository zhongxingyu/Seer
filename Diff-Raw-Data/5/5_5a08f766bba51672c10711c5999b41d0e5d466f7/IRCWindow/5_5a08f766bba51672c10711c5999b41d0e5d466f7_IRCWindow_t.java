 package ircclient.gui.windows;
 
 import ircclient.gui.JCloseTabbedPane;
 import ircclient.gui.Menu;
 import ircclient.gui.ServerPanel;
 import ircclient.gui.Tray;
 import ircclient.irc.Server;
 import java.awt.BorderLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.WindowConstants;
 
 /**
  *
  * @author fc
  */
 public class IRCWindow extends JFrame {
 
     private Tray tray;
     private JTabbedPane servers;
     private ArrayList<ServerPanel> serverList = new ArrayList<ServerPanel>();
 
     public IRCWindow(String title) {
         super(title);
         this.addWindowListener(new WindowHandler(this));
         this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         this.setSize(640, 480);
         this.setResizable(true);
         init();
     }
 
     private void autojoin() {
         File network = new File("networks/");
         if(!network.exists()) {
             network.mkdir();
         }
         
         File[] f = network.listFiles();
         for (File file : f) {
             Server s = new Server();
             s.read(file.getName());
             System.out.println(s.getServer());
             if (s.isAutoConnect()) {
                 ServerPanel sp = new ServerPanel(this, s.getName(), s.getServer(),
                         s.getPort(), s.getNick(), s.getChannels(), s.getNickPass(),
                         s.isLogged(), s.isSSL());
                 servers.add(s.getName(), sp);
                 serverList.add(sp);
             }
         }
     }
 
     public void exit() {
         try {
             for (ServerPanel sp : serverList) {
                 sp.getServer().getOutput().quit();
             }
         } catch (IOException ioe) {
             System.err.println("Error quiting server");
         }
         System.exit(0);
     }
 
     public void joinServer(String name) {
         Server s = new Server();
         s.read(name);
         ServerPanel sp = new ServerPanel(this, s.getName(), s.getServer(),
                 s.getPort(), s.getNick(), s.getChannels(), s.getNickPass(),
                 s.isLogged(), s.isSSL());
         servers.add(s.getName(), sp);
         serverList.add(sp);
     }
 
     public void quitServer(int i) throws IOException {
         serverList.get(i).getServer().getOutput().quit();
         serverList.remove(i);
         servers.remove(i);
     }
 
     public JTabbedPane getTabbedPane() {
         return servers;
     }
 
     public ArrayList<ServerPanel> getServerList() {
         return serverList;
     }
 
     public Tray getTray() {
         return tray;
     }
 
     public void setTray(Tray t) {
         tray = t;
     }
 
     private void init() {
         Menu menu = new Menu(this);
         servers = new JCloseTabbedPane(this);
         serverList = new ArrayList<ServerPanel>();
 
         autojoin();
 
         this.add(menu, BorderLayout.NORTH);
         this.add(servers, BorderLayout.CENTER);
     }
 }
 
 class WindowHandler extends WindowAdapter {
 
     IRCWindow win;
 
     public WindowHandler(IRCWindow win) {
         this.win = win;
     }
 
    /*@Override
     public void windowClosing(WindowEvent evt) {
         win.exit();
     }
*/
     @Override
     public void windowIconified(WindowEvent evt) {
         win.setVisible(false);
     }
 }
