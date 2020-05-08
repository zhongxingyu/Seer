 package info.gomeow.chester;
 
 import info.gomeow.chester.API.ChesterBroadcastEvent;
 import info.gomeow.chester.API.ChesterLogEvent;
 import info.gomeow.chester.util.Metrics;
 import info.gomeow.chester.util.Updater;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.jibble.jmegahal.JMegaHal;
 
 public class Chester extends JavaPlugin implements Listener {
 
     public static String LINK;
     public static boolean UPDATE;
     public static String NEWVERSION;
 
     JMegaHal hal = new JMegaHal();
 
     List<String> triggerwords;
 
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(this, this);
         saveDefaultConfig();
         if(getConfig().getString("chatcolor") == null) {
             getConfig().set("chatcolor", "r");
         }
         if(getConfig().getString("check-update") == null) {
             getConfig().set("check-update", true);
         }
         triggerwords = getConfig().getStringList("triggerwords");
         if(triggerwords.size() == 0) {
             triggerwords.add("chester");
         }
         System.out.println(triggerwords);
         startChester();
         startMetrics();
         checkUpdate();
     }
 
     public void firstRun(File f) {
         try {
             f.createNewFile();
         } catch(IOException ioe) {
             ioe.printStackTrace();
         }
         hal.add("Hello World");
         hal.add("Can I have some coffee?");
         hal.add("Please slap me");
     }
 
     public void transfer(ObjectInputStream in) throws ClassNotFoundException, IOException {
         hal = (JMegaHal) in.readObject();
         if(in != null) {
             in.close();
         }
     }
 
     public void checkUpdate() {
         new BukkitRunnable() {
 
             public void run() {
                 if(getConfig().getBoolean("check-update", true)) {
                     try {
                         Updater u = new Updater(getDescription().getVersion());
                         if(UPDATE = u.getUpdate()) {
                             LINK = u.getLink();
                             NEWVERSION = u.getNewVersion();
                         }
                     } catch(Exception e) {
                         getLogger().log(Level.WARNING, "Failed to check for updates.");
                         getLogger().log(Level.WARNING, "Report this stack trace to gomeow.");
                         e.printStackTrace();
                     }
                 }
             }
         }.runTaskAsynchronously(this);
     }
 
     public void startMetrics() {
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch(IOException e) {
             e.printStackTrace();
         }
     }
 
     public void startChester() {
         try {
             File chesterFile = new File(this.getDataFolder(), "brain.chester");
             File dir = new File("plugins" + File.separator + "Chester");
             if(!dir.exists()) {
                 dir.mkdirs();
             }
             File old = new File(this.getDataFolder(), "chester.brain");
             if(old.exists()) {
                 transfer(new ObjectInputStream(new FileInputStream(old)));
             }
             if(chesterFile.exists()) {
                 FileReader fr = new FileReader(chesterFile);
                 BufferedReader br = new BufferedReader(fr);
                 String line = null;
                 while((line = br.readLine()) != null) {
                     hal.add(line);
                 }
                 br.close();
             } else {
                 firstRun(chesterFile);
             }
         } catch(IOException ioe) {
         } catch(ClassNotFoundException cnfe) {
         }
     }
 
     public String clean(String string) {
         if(string != null && string.length() > 300) {
             string = string.substring(0, 300);
         }
         String newstring = string.replaceAll("<.*?>", "").replaceAll("\\[.*?\\]", "");
         return newstring;
     }
 
     public void write(String sentence) {
         File chesterFile = new File(this.getDataFolder(), "brain.chester");
         try {
             FileWriter fw = new FileWriter(chesterFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             bw.write(sentence + "\n");
             bw.close();
         } catch(IOException ioe) {
             ioe.printStackTrace();
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         if(player.isOp() && UPDATE) {
             player.sendMessage(ChatColor.DARK_AQUA + "Version " + NEWVERSION + " of Chester is up for download!");
             player.sendMessage(ChatColor.DARK_AQUA + LINK + " to view the changelog and download!");
         }
     }
 
     @SuppressWarnings("deprecation")
     @EventHandler
     public void onChat(final PlayerChatEvent event) {
         Player player = event.getPlayer();
         final String message = event.getMessage();
        ChesterLogEvent logEvent = new ChesterLogEvent(player, message);
        getServer().getPluginManager().callEvent(logEvent);
        if(player.hasPermission("chester.log") || logEvent.isCancelled()) {
             write(clean(message));
         }
         if(player.hasPermission("chester.trigger")) {
             boolean cancel = false;
             for(String trigger:triggerwords) {
                 if(message.matches("^.*(?i)" + trigger + ".*$")) {
                     cancel = true;
                     break;
                 }
             }
             if(!cancel) {
                 hal.add(message);
             }
         }
         for(final String trigger:triggerwords) {
             if(message.matches("^.*(?i)" + trigger + ".*$")) {
                 String sentence = hal.getSentence();
                 while(sentence.matches("^.*(?i)" + trigger + ".*$")) {
                     sentence = hal.getSentence();
                 }
                 ChesterBroadcastEvent cbe = new ChesterBroadcastEvent(sentence);
                 for(Player plyer:cbe.getRecipients()) {
                     plyer.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("nickname")) + ChatColor.getByChar(getConfig().getString("chatcolor")) + " " + ChatColor.translateAlternateColorCodes('&', sentence));
                 }
                 break;
             }
         }
     }
 }
