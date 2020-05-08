 package com.ACStache.ArenaGodPlus;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.garbagemule.MobArena.framework.ArenaMaster;
 import com.garbagemule.MobArena.MobArena;
 
 import net.slipcor.pvparena.PVPArena;
 
 public class ArenaGodPlus extends JavaPlugin
 {
     private Logger log = Logger.getLogger("Minecraft");
     private PluginDescriptionFile info;
     private static File dir, file;
     public static ArenaMaster am;
     
     public void onEnable()
     {
         this.getServer().getPluginManager().registerEvents(new AGPListener(), this);
         info = getDescription();
         setupArenaStuff();
         
         dir = getDataFolder();
         file = new File(dir, "config.yml");
         if(!dir.exists())
         {
             dir.mkdir();
             AGPConfig.loadConfig(file);
         }
         else
         {
             AGPConfig.loadConfig(file);
         }
         
         for(World w : Bukkit.getWorlds())
             for(Player p : w.getPlayers())
                 if(AGPConfig.getPersGod(p))
                     AGPSetter.addGod(p);
 
         log.info("[" + info.getName() + "] v" + info.getVersion() + " Successfully Enabled! By: " + info.getAuthors());
     }
     
     public void onDisable()
     {
         log.info("[" + info.getName() + "] Sucessfully Disabled");
     }
     
     private void setupArenaStuff()
     {
         MobArena maPlugin = (MobArena)Bukkit.getServer().getPluginManager().getPlugin("MobArena");
         PVPArena pvpPlugin = (PVPArena)Bukkit.getServer().getPluginManager().getPlugin("pvparena");
         if(maPlugin != null && maPlugin.isEnabled()) {
             am = maPlugin.getArenaMaster();
             this.getServer().getPluginManager().registerEvents(new AGPMAListener(this), this);
             log.info("[" + info.getName() + "] Found MobArena!");
         }
         if(pvpPlugin != null && pvpPlugin.isEnabled()) {
             this.getServer().getPluginManager().registerEvents(new AGPPVPListener(this), this);
             log.info("[" + info.getName() + "] Found PVPArena!");
         }
     }
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if(command.getName().equalsIgnoreCase("god"))
         {
             if(args.length >= 1)
             {
                 if(args[0].equalsIgnoreCase("status"))
                 {
                     if(sender instanceof Player && ((Player)sender).hasPermission("ArenaGodPlus.status"))
                     {
                         Player player = (Player)sender;
                         if(AGPSetter.isGod(player))
                         {
                             player.sendMessage(ChatColor.AQUA + "AGP: You are a God");
                         }
                         else
                         {
                             player.sendMessage(ChatColor.AQUA + "AGP: You are not a God");
                         }
                     }
                     else
                     {
                         if(sender instanceof Player)
                         {
                             ((Player)sender).sendMessage(ChatColor.AQUA + "AGP: You don't have permission to do that.");
                         }
                         else
                         {
                             log.info("[" + info.getName() + "] You can't use that command from the console");
                         }
                     }
                 }
                 else if(args[0].equalsIgnoreCase("reload"))
                 {
                     if(sender instanceof Player && ((Player)sender).hasPermission("ArenaGodPlus.reload") || !(sender instanceof Player))
                     {
                         AGPConfig.loadConfig(file);
                         if(sender instanceof Player)
                         {
                             ((Player)sender).sendMessage(ChatColor.AQUA + "AGP: Config reloaded");
                         }
                         else
                         {
                             log.info("[" + info.getName() + "] Config reloaded");
                         }
                     }
                     else
                     {
                         ((Player)sender).sendMessage(ChatColor.AQUA + "AGP: You don't have permission to do that.");
                     }
                 }
                 else
                 {
                     if(sender instanceof Player)
                     {
                         ((Player)sender).sendMessage(ChatColor.AQUA + "AGP: Please type in '/mag', '/mag status', or '/mag reload'");
                     }
                     else
                     {
                         log.info("[" + info.getName() + "] You can't use that command from the console");
                     }
                 }
             }
             else
             {
                 if(sender instanceof Player && ((Player)sender).hasPermission("ArenaGodPlus.toggle"))
                 {
                     AGPSetter.setGod((Player)sender);
                 }
                 else
                 {
                     if(sender instanceof Player)
                     {
                         ((Player)sender).sendMessage(ChatColor.AQUA + "AGP: You don't have permission to do that.");
                     }
                     else
                     {
                         log.info("[" + info.getName() + "] You can't use that command from the console");
                     }
                 }
             }
         }
         return true;
     }
 }
