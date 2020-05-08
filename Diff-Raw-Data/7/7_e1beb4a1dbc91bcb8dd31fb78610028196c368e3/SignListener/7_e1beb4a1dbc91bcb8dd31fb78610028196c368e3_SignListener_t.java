 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.util.ParticleEffect;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class SignListener implements Listener {
 
     // Get Main Class
     public static BattleNight plugin;
 
     public SignListener(BattleNight instance) {
         plugin = instance;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event) {
 
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             Block block = event.getClickedBlock();
             Player player = event.getPlayer();
 
             if (block.getState() instanceof Sign) {
                 Sign sign = (Sign) block.getState();
                 String name = player.getName();
                 String title = sign.getLine(0);
 
                 if (BattleNight.BattleClasses.containsKey(title) && BattleNight.getBattle().usersTeam.containsKey(name)) {
                     addSign(sign);
 
                     if (BattleNight.getBattle().usersClass.containsKey(name)) {
                         if (BattleNight.getBattle().usersClass.get(name).equalsIgnoreCase(title)) {
                             BattleNight.getBattle().usersClass.remove(name);
 
                             if (removeName(player, sign)) {
                                 plugin.reset(player, true);
                             }
 
                             ParticleEffect.spiral(player);
                         } else {
                             BattleNight.tellPlayer(player, "You must first remove yourself from the other class!");
                         }
                     } else if (sign.getLine(2).trim().equals("")) {
                         BattleNight.getBattle().usersClass.put(name, title);
                         sign.setLine(2, name);
                         sign.update();
                         plugin.giveItems(player);
                         ParticleEffect.spiral(player);
                     } else if (sign.getLine(3).trim().equals("")) {
                         BattleNight.getBattle().usersClass.put(name, title);
                         sign.setLine(3, name);
                         sign.update();
                         plugin.giveItems(player);
                         ParticleEffect.spiral(player);
                     } else {
                         BattleNight.tellPlayer(player, "There are too many of this class, pick another class.");
                     }
                 }
             }
         }
     }
 
     private static void addSign(Sign sign) {
         if (!BattleNight.classSigns.contains(sign)) {
             BattleNight.classSigns.add(sign);
         }
     }
 
     public static boolean removeName(Player player, Sign sign) {
         boolean result = false;
 
        if (sign.getLine(3).contains(player.getName())) {
             sign.setLine(3, "");
             result = true;
         }
 
        if (sign.getLine(2).contains(player.getName())) {
            sign.setLine(2, sign.getLine(3));
             sign.setLine(3, "");
             result = true;
         }
 
         sign.update();
         return result;
     }
 }
