 package jabberbot;
 
 import java.io.*;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jivesoftware.smack.XMPPException;
 
 public class Parse {
 
     public static void ParseConfigFile() throws FileNotFoundException, IOException {
 
         Properties prop = new Properties();
         String fileName = "config.cfg";
         InputStream is = new FileInputStream(fileName);
         prop.load(is);
         String jid = prop.getProperty("jid");
         String nick = prop.getProperty("login");
        String server = prop.getProperty("server");
         int port = Integer.parseInt(prop.getProperty("port", "5222"));
         if (jid != null) {
             StringTokenizer st = new StringTokenizer(jid, "@");
             while (st.hasMoreTokens()) {
                 nick = st.nextToken();
                server = st.nextToken();
             }
         }
        String domain = prop.getProperty("domain", server);
         String password = prop.getProperty("password");
         String adminB = prop.getProperty("admin");
         String resource = prop.getProperty("resource", "bot");
         String status = prop.getProperty("status", "Alpha version");
         if (nick == null || server == null || adminB == null) {
             System.out.println("Error! Login or server or admin JID is null!");
             System.exit(0);
         }
         while (password == null) {
             char passwd[];
             Console cons = System.console();
             passwd = cons.readPassword(" Enter password : ");
             password = new String(passwd);
         }
         XmppNet conXm;
 
         try {
             conXm = new XmppNet(status, nick, domain, server, password, resource, port);
             conXm.whoIsAdmin(adminB);
             System.out.println("Config parsed");
             Log.addToLog("Config parsed");
             while (true) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Parse.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 try {
                     conXm.incomeChat();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Parse.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
             }
 
         } catch (XMPPException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
     // Config parsed ^
 }
