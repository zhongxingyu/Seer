 package iReport;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class iReport extends JavaPlugin {
 
     public iReport() {
     }
 
     @Override
     @SuppressWarnings("unused")
     public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
         if (cmd.getName().equalsIgnoreCase("greport") && args.length == 1) {
             String player = sender.getName();
             String target = args[0];
             Location loc = null;
             String already = (String) getConfig().get(new StringBuilder("reports.griefing.").append(player).toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("You successfully reported ").append(ChatColor.RED).append(target).toString());
             getConfig().set(new StringBuilder("reports.griefing.").append(player).toString(), new StringBuilder(Rlocation.getxyz(this, args[0])).append("; ").append(target).toString());
 
             saveConfig();
 
             return true;
         }
        if (cmd.getName().equalsIgnoreCase("hreport") && args.length == 1) {
             String player = sender.getName();
             String target = args[0];
             String already = (String) getConfig().get(new StringBuilder("reports.hacking.").append(player).toString());
            getConfig().set(new StringBuilder("reports.hacking.").append(player).toString(), new StringBuilder(String.valueOf(already)).append("; ").append(target).toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("You successfully reported ").append(ChatColor.RED).append(target).toString());
             saveConfig();
 
             return true;
         }
         if (cmd.getName().equalsIgnoreCase("sreport") && args.length == 1) {
             String player = sender.getName();
             String target = args[0];
             String already = (String) getConfig().get(new StringBuilder("reports.swearing.").append(player).toString());
            getConfig().set(new StringBuilder("reports.swearing.").append(player).toString(), new StringBuilder(String.valueOf(already)).append("; ").append(target).toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("You successfully reported ").append(ChatColor.RED).append(target).toString());
             saveConfig();
 
             return true;
 
         }
         if (cmd.getName().equalsIgnoreCase("ireport")) {
             sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append("==============================").toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("/greport - Report a griefer").toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("/hreport - Report a hacker").toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("/sreport - Report a swearer").toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append("/ireport - Show this help menu").toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append("==============================").toString());
             sender.sendMessage(new StringBuilder().append(ChatColor.GREEN).append("Created by tudse145").toString());
             return true;
         } else
             return false;
     }
 
     private FileConfiguration customConfig = null;
     private File customConfigFile = null;
 
     public void reloadCustomConfig() {
         if (customConfigFile == null)
             customConfigFile = new File(getDataFolder(), "customconfig.yml");
         customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 
         // Look for defaults in the jar
         InputStream defConfigStream = getResource("customconfig.yml");
         if (defConfigStream != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
             customConfig.setDefaults(defConfig);
         }
     }
 
     public FileConfiguration getCustomConfig() {
         if (customConfig == null)
             reloadCustomConfig();
         return customConfig;
     }
 
     public void saveCustomConfig() {
         if (customConfig == null || customConfigFile == null)
             return;
         try {
             getCustomConfig().save(customConfigFile);
         } catch (IOException ex) {
             getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
         }
     }
 
     public void createfile() {
         File List = new File("/iReport/costumconfig.yml");
         if (!List.exists())
             try {
                 List.createNewFile();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
     }
 
     @Override
     public void onEnable() {
         saveConfig();
         getConfig().options().copyDefaults(true);
         saveCustomConfig();
     }
 
 }
