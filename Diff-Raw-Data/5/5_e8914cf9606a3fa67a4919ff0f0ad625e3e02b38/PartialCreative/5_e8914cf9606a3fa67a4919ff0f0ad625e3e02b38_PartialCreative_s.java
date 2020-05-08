 package redcastlemedia.multitallented.bukkit.partialcreative;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PartialCreative extends JavaPlugin {
   private static HashMap<Player, ArrayList<ItemStack>> previousItems = new HashMap<Player, ArrayList<ItemStack>>();
   private static HashSet<Player> playerModes = new HashSet<Player>();
   private static HashSet<String> modePerms = new HashSet<String>();
   private static Permission perms;
   
   @Override
   public void onDisable() {
     System.out.println("[PartialCreative] has been disabled.");
     
     for (Player p : playerModes) {
       if (previousItems.containsKey(p)) {
         setPlayerInventory(p, previousItems.get(p));
       }
       togglePerms(p);
     }
     
   }
 
   @Override
   public void onEnable() {
     Bukkit.getPluginManager().registerEvents(new PCListener(this), this);
     modePerms = ConfigManager.getPermList(this);
     
     if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
         //Setup perm provider
         RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
         if (permissionProvider != null) {
             perms = permissionProvider.getProvider();
             if (perms != null) {
                 System.out.println("[PartialCreative] Hooked into " + perms.getName());
             }
         }
     } else {
       Bukkit.getPluginManager().registerEvents(
       new Listener() {
         @EventHandler
         public void onPluginEnable(PluginEnableEvent event) {
           if (event.getPlugin().getDescription().getName().equals("Vault")) {
 
             RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
             if (permissionProvider != null) {
               perms = permissionProvider.getProvider();
               if (perms != null) {
                 System.out.println("[PartialCreative] Hooked into " + perms.getName());
               }
             }
           }
         }
       }, this);
     }
     
     
     System.out.println("[PartialCreative] has been enabled.");
   }
   
   @Override
   public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
     if (label.equals("pc") && (cs instanceof Player)) {
       Player p = (Player) cs;
       if (playerModes.contains(p)) {
         ArrayList<ItemStack> oldItems = previousItems.containsKey(p) ? previousItems.get(p) : new ArrayList<ItemStack>();
         previousItems.put(p, storeInventory(p));
         setPlayerInventory(p, oldItems);
         togglePerms(p);
         playerModes.remove(p);
         p.sendMessage(ChatColor.GRAY + "[PartialCreative] You are now in partial creative mode.");
       } else {
         ArrayList<ItemStack> oldItems = previousItems.containsKey(p) ? previousItems.get(p) : new ArrayList<ItemStack>();
         previousItems.put(p, storeInventory(p));
         setPlayerInventory(p, oldItems);
         togglePerms(p);
         playerModes.add(p);
         p.sendMessage(ChatColor.GRAY + "[PartialCreative] You are now in non-partial creative mode.");
       }
       return true;
     }
     return true;
   }
   
   public static boolean isPlayerInMode(Player p) {
     return playerModes.contains(p);
   }
   
   private ArrayList<ItemStack> storeInventory(Player p) {
     ArrayList<ItemStack> iss = new ArrayList<ItemStack>();
     PlayerInventory pi = p.getInventory();
     iss.addAll(Arrays.asList(pi.getArmorContents()));
     iss.addAll(Arrays.asList(pi.getContents()));
     return iss;
   }
   
   private void setPlayerInventory(Player p, ArrayList<ItemStack> oldItems) {
     PlayerInventory pi = p.getInventory();
     pi.clear();
     pi.setHelmet(oldItems.get(0));
     pi.setChestplate(oldItems.get(1));
     pi.setLeggings(oldItems.get(2));
     pi.setBoots(oldItems.get(3));
     for (int i = 4; i< oldItems.size(); i++) {
       try {
         pi.addItem(oldItems.get(i));
       } catch (NullPointerException npe) {
         
       }
     }
   }
   
   public void handleQuittingPlayer(Player p) {
     if (!playerModes.contains(p)) {
       return;
     }
     if (previousItems.containsKey(p)) {
       ArrayList<ItemStack> oldItems = storeInventory(p);
       setPlayerInventory(p, previousItems.get(p));
       previousItems.put(p, oldItems);
     }
     togglePerms(p);
     playerModes.remove(p);
   }
   
   public void togglePerms(Player p) {
     if (PartialCreative.perms == null) {
       return;
     }
     
     if (playerModes.contains(p)) {
       for (String s : modePerms) {
        perms.playerRemove(p, s);
       }
     } else {
       for (String s : modePerms) {
        perms.playerAdd(p, s);
       }
     }
   }
 }
 
