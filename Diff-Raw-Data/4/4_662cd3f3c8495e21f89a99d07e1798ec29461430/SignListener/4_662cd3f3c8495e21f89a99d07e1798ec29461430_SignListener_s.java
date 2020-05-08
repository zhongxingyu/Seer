 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
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
 
    public static final Map<Sign, String[]> classSigns = new HashMap<Sign, String[]>();
 
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
 
                     cleanSigns(player);
                     addName(player, sign);
 
                     BattleNight.getBattle().usersClass.put(name, title);
                     BattleNight.reset(player, true);
                     BattleNight.giveItems(player);
                     ParticleEffect.spiral(player);
 
                     if (BattleNight.getBattle().usersClass.containsKey(name)) {
                         if (!BattleNight.getBattle().usersClass.get(name).equals(title)) {
                             ParticleEffect.spiral(player);
                         }
                     }
                 }
             }
         }
     }
 
     private static void addSign(Sign sign) {
         if (!classSigns.containsKey(sign)) {
             classSigns.put(sign, new String[2]);
         }
     }
 
     private static void addName(Player player, Sign sign) {
         // Get the players from the HashMap
         String[] players = classSigns.get(sign);
 
         // Third line is not empty
         if (!sign.getLine(2).isEmpty()) {
             // Move the first name down
             sign.setLine(3, sign.getLine(2));
             players[1] = players[0];
         }
 
         // Add the players name
         String name = player.getName();
         sign.setLine(2, name);
         players[0] = name;
 
         // Update the sign
         sign.update();
 
         // Refresh the HashMap
         classSigns.put(sign, players);
     }
 
     private static void cleanName(String name, Sign sign) {
         // Get the players from the HashMap
         String[] players = classSigns.get(sign);
 
         // Forth line has the players name
         if (players[1] == name) {
             // Clear line four
             sign.setLine(3, "");
             players[1] = "";
             // Update the sign
             sign.update();
         }
 
         // Third line has the players name
         if (players[0] == name) {
             // Move the second name up
             sign.setLine(2, sign.getLine(3));
             sign.setLine(3, "");
             players[0] = players[1];
             players[1] = "";
 
             // Update the sign
             sign.update();
         }
 
         // Refresh the HashMap
         classSigns.put(sign, players);
     }
 
     public static void cleanSigns() {
         Iterator<Entry<Sign, String[]>> it = classSigns.entrySet().iterator();
         while (it.hasNext()) {
             Entry<Sign, String[]> entry = it.next();
             Sign sign = entry.getKey();
             String[] players = entry.getValue();
 
             if (sign != null) {
                 sign.setLine(2, "");
                 sign.setLine(3, "");
                 sign.update();
 
                 players[0] = "";
                 players[1] = "";
             } else {
                 it.remove();
             }
         }
     }
 
     public static void cleanSigns(Player player) {
         Iterator<Sign> it = classSigns.keySet().iterator();
         while (it.hasNext()) {
             Sign sign = it.next();
             if (sign != null) {
                 cleanName(player.getName(), sign);
             } else {
                 it.remove();
             }
         }
     }
 }
