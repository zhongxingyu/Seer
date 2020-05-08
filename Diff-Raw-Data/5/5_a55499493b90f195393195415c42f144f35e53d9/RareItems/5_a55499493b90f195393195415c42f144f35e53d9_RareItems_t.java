 package com.gmail.ne0nx3r0.rareitems;
 
 import com.gmail.ne0nx3r0.persistence.ProfileManager;
 import com.gmail.ne0nx3r0.rareitems.http.ApiMessenger;
 import com.gmail.ne0nx3r0.rareitems.item.ItemPropertyManager;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RareItems extends JavaPlugin{
     public static Plugin self;
     public static ItemPropertyManager rig;
     public static ApiMessenger am;
     //public static VirtualChestManager vcm;
     public static ProfileManager pm;
     
     public static final Logger logger = Logger.getLogger("RareItemsRegistration");
     
     public static final int COST_TYPE_FOOD = 0;
     public static final int COST_TYPE_XP = 1;
     
     public static int COST_TYPE;
     public static int COST_MULTIPLIER; 
     
     public static boolean USE_PERMISSIONS;
     
     public static int MAX_CHECKED_OUT_ITEMS;
 
     public static final String RID_PREFIX = ChatColor.DARK_GRAY+"RID: "+ChatColor.GRAY;
     
     @Override
     public void onEnable()
     {        
         RareItems.self = this;
         
         File check = new File(getDataFolder(), "config.yml");
         
         if(!check.exists())
         {
             saveDefaultConfig();
             reloadConfig();
         }
         
         if(getConfig().getString("costType").equalsIgnoreCase("food"))
         {
             COST_TYPE = COST_TYPE_FOOD;
         }
         else if(getConfig().getString("costType").equalsIgnoreCase("xp"))
         {
             COST_TYPE = COST_TYPE_XP;
         }
         
         COST_MULTIPLIER = getConfig().getInt("costMultiplier");
         
         USE_PERMISSIONS = getConfig().getBoolean("usePermissions");
         
         MAX_CHECKED_OUT_ITEMS = getConfig().getInt("maxItemsCheckedOut");
         
         RareItems.rig = new ItemPropertyManager();
         
         RareItems.am = new ApiMessenger();
         
         RareItems.pm = new ProfileManager();
         
         //RareItems.vcm = new VirtualChestManager();
         
         //register events
         getServer().getPluginManager().registerEvents(new RareItemsPlayerListener(), this);
         
         getCommand("ri").setExecutor(new RareItemsCommandExecutor());
 
        if(Bukkit.getOnlinePlayers().length > 0)
        {
            ApiMessenger.fetchPlayerRareItems(Bukkit.getOnlinePlayers(), false);
        }
     }    
     
     @Override
     public void onDisable()
     {      
         RareItems.pm.saveAllPlayerProfiles();
 
         Bukkit.getScheduler().cancelTasks(this);
     }
 }
