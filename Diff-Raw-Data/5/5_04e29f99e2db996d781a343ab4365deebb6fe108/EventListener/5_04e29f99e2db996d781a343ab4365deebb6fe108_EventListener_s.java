 package me.limebyte.endercraftessentials;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.kitteh.tag.PlayerReceiveNameTagEvent;
 import org.kitteh.tag.TagAPI;
 
 public class EventListener implements Listener {
 
     private static final Material LIGHT_LEVEL_ITEM = Material.GLOWSTONE_DUST;
     private static final Material HUNGER_INFO_ITEM = Material.POISONOUS_POTATO;
 
     private static final String REI_PREFIX = "&0&0";
     private static final String REI_SUFFIX = "&e&f";
     @SuppressWarnings("unused")
     private static final String REI_CAVE_MAPPING = "&1";
     private static final String REI_PLAYER_RADAR = "&2";
     private static final String REI_ANIMAL_RADAR = "&3";
     @SuppressWarnings("unused")
     private static final String REI_MOB_RADAR = "&4";
     @SuppressWarnings("unused")
     private static final String REI_SLIME_RADAR = "&5";
     private static final String REI_SQUID_RADAR = "&6";
     @SuppressWarnings("unused")
     private static final String REI_LIVING_RADAR = "&7";
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             if (event.getItem() != null) {
                 if (event.getItem().getType() == LIGHT_LEVEL_ITEM) {
                     Block block = event.getClickedBlock().getRelative(event.getBlockFace());
                     int lightLevel = block.getLightLevel();
                     event.getPlayer().sendMessage(ChatColor.GOLD + "The light level of the selected block is " + lightLevel + ".");
                 }
 
                 if (event.getItem().getType() == HUNGER_INFO_ITEM) {
                     Player player = event.getPlayer();
                     String title = ChatColor.GOLD + "   --- " +
                             ChatColor.ITALIC + "Hunger Info" +
                             ChatColor.RESET + ChatColor.GOLD + " ---   ";
 
                     player.sendMessage(title);
                     player.sendMessage(ChatColor.WHITE + "FoodLevel: " + player.getFoodLevel());
                     player.sendMessage(ChatColor.WHITE + "Saturation: " + player.getSaturation());
                     player.sendMessage(ChatColor.WHITE + "Exhaustion: " + player.getExhaustion());
                     player.sendMessage(ChatColor.GOLD + "   -------------------   ");
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         setDisplayName(player);
         String welcome = ChatColor.DARK_PURPLE + "Welcome to Endercraft " + player.getDisplayName() + "!";
         String message = REI_PREFIX + REI_PLAYER_RADAR + REI_ANIMAL_RADAR + REI_SQUID_RADAR + REI_SUFFIX;
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message) + welcome);
         event.setJoinMessage(event.getJoinMessage().replaceAll(player.getName(), player.getDisplayName()));
 
         if (isPranked(player)) {
             player.sendMessage("You have pranked");
             player.sendMessage("but are now outranked.");
             player.sendMessage("Blocks for code,");
             player.sendMessage("pranks echoed.");
             player.sendMessage("Not to be rude,");
             player.sendMessage("but I have called you a noob.");
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         event.setQuitMessage(event.getQuitMessage().replaceAll(player.getName(), player.getDisplayName()));
     }
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         event.setDeathMessage(event.getDeathMessage().replaceAll(player.getName(), player.getDisplayName()));
     }
 
     @EventHandler
     public void onNameplate(PlayerReceiveNameTagEvent event) {
         if (!event.isModified()) {
             Player player = event.getNamedPlayer();
             String name = player.getName();
             String displayName = player.getDisplayName();
 
             if (name.equalsIgnoreCase("bj2864") || name.equalsIgnoreCase("bg1345") || isPranked(player)) {
                 event.setTag(displayName);
             }
         }
     }
 
     private void setDisplayName(Player player) {
         String name = player.getName();
 
         if (name.equalsIgnoreCase("limebyte")) {
             rename(player, "LimeByte");
         } else if (name.equalsIgnoreCase("bj2864")) {
             rename(player, "BennyBoi");
         } else if (name.equalsIgnoreCase("bg1345")) {
             rename(player, "Ashpof");
         } else if (name.equalsIgnoreCase("tegdim")) {
             rename(player, "Tegdim");
         }
 
         if (isPranked(player)) {
             rename(player, "Noob");
         }
     }
 
     private void rename(Player player, String name) {
         player.setDisplayName(name);
         TagAPI.refreshPlayer(player);
         setPlayerListName(player, name);
     }
 
     private void setPlayerListName(Player player, String name) {
         try {
             player.setPlayerListName(name);
         } catch (IllegalArgumentException e) {
             try {
                 String number = String.valueOf(System.currentTimeMillis() % 9);
 
                 if (16 - name.length() < 3) {
                     player.setPlayerListName(name.substring(0, 16 - 3) + " " + number);
                 } else {
                     player.setPlayerListName(name + " " + number);
                 }
             } catch (IllegalArgumentException e1) {
                 setPlayerListName(player, name);
             }
         }
     }
 
     private boolean isPranked(Player player) {
         for (String name : EndercraftEssentials.getPrankedNames()) {
             if (player.getName().equalsIgnoreCase(name)) return true;
         }
         return false;
     }
 
 }
