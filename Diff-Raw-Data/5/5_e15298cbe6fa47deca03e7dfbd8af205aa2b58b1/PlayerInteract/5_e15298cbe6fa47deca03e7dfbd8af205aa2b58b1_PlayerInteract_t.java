 package me.aaomidi.frisking;
 
 import java.util.HashMap;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 class PlayerInteract implements Listener {
 
     public static HashMap<Player, Location> jail = new HashMap<Player, Location>();
     public static Main plugin;
 
     @EventHandler
     public void EntityDamgeByEntityEvent(EntityDamageByEntityEvent e) {
 
        if (e.getDamager() instanceof Player){
            Player player=(Player) e.getDamager();
         if (Main.drug.containsKey(player)) {
             if (player.getItemInHand().getType() == Material.STICK) {
                 Player target = (Player) e.getEntity();
                 if (target.getInventory().contains(Material.RED_MUSHROOM) || target.getInventory().contains(Material.SUGAR_CANE) || target.getInventory().contains(Material.SUGAR) || target.getInventory().contains(Material.SEEDS) || target.getInventory().contains(Material.CACTUS)) {
                     if (!jail.containsKey(target)) {
                         player.sendMessage(String.format("DRUG ALERT", ChatColor.RED));
                         target.getInventory().remove(Material.RED_MUSHROOM);
                         target.getInventory().remove(Material.CACTUS);
                         target.getInventory().remove(Material.SEEDS);
                         target.getInventory().remove(Material.SUGAR_CANE);
                         target.getInventory().remove(Material.SUGAR);
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("eco take %1$s 100", target.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("nick %1$s %1$s_&4X", target.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("say %1$s was caught with drugs for the first time by %2$s!", target.getName(), player.getName()));
                         jail.put(target, null);
                         target.sendMessage(String.format("%1$sYou were caught with drugs 1/2", ChatColor.DARK_RED));
                         e.setDamage(0);
                     } else if (jail.containsKey(target)) {
                         player.sendMessage(ChatColor.DARK_RED + "DRUG ALERT");
                         target.getInventory().remove(Material.RED_MUSHROOM);
                         target.getInventory().remove(Material.CACTUS);
                         target.getInventory().remove(Material.SEEDS);
                         target.getInventory().remove(Material.SUGAR_CANE);
                         target.getInventory().remove(Material.SUGAR);
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("eco take %1$s 200", target.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("nick %1$s off", target.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("manuadd %1$s prisoner prison2", target.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("say %1$s was caught with drugs for the second time by %2$s and sent into the jail!", target.getName(), player.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("spawn %1$s", target.getName()));
                         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("ci %1$s", target.getName()));
                         target.sendMessage(String.format("%1$sYou were caught with drugs 2/2 JAIL TIME!", ChatColor.DARK_RED));
                         jail.remove(target);
                         ItemStack armor = new ItemStack(Material.AIR);
                         target.getInventory().setHelmet(armor);
                         target.getInventory().setChestplate(armor);
                         target.getInventory().setLeggings(armor);
                         target.getInventory().setBoots(armor);
                     }
                 } else {
                     player.sendMessage(String.format("%1$2%2$s is clean :)", ChatColor.BLUE, target.getName()));
                 }
             }
         }
     }
    }
 }
