 package info.bytecraft.listener;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.zones.Lot;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.zones.Zone.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class SelectListener implements Listener
 {
     private Bytecraft plugin;
     
     public SelectListener(Bytecraft instance)
     {
         plugin = instance;
     }
     
     @EventHandler
     public void onSelectZone(PlayerInteractEvent event)
     {
         if(event.isCancelled())return;
         BytecraftPlayer player = plugin.getPlayer(event.getPlayer());
         if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.STICK){
             Block block = event.getClickedBlock();
            if(player.getCurrentZone() != null){
                Zone zone = player.getCurrentZone();
                 Permission perm = zone.getUser(player);
                 if((perm == null || perm != Permission.OWNER) && !player.getRank().canEditZones())return;
                 if(zone.findLot(block.getLocation()) != null){
                     Lot lot = zone.findLot(block.getLocation());
                     if(lot.isOwner(player) || perm == Permission.OWNER || player.getRank().canEditZones()){
                         player.sendMessage(ChatColor.YELLOW + "This lots name is: " + lot.getName());
                         return;
                     }
                 }
                 if(player.getLotBlock1() == null && player.getLotBlock2() == null){
                     //player has selected no blocks
                     player.setLotBlock1(block);
                     player.sendMessage(ChatColor.YELLOW + "First block of a new lot selected at " + format(block.getLocation()));
                     event.setCancelled(true);
                     return;
                 }else if(player.getLotBlock1() != null && player.getLotBlock2() == null){
                     //player has selected first block
                     player.setLotBlock2(block);
                     player.sendMessage(ChatColor.YELLOW + "Second block of a new lot selected at " + format(block.getLocation()));
                     event.setCancelled(true);
                 }else if(player.getLotBlock1() != null && player.getLotBlock2() != null){
                     //both blocks selected
                     player.setLotBlock2(null);
                     player.setLotBlock1(block);
                     player.sendMessage(ChatColor.YELLOW + "First block of a new lot selected at " + format(block.getLocation()));
                     event.setCancelled(true);
                     return;
                 }
             }else{
                 //Outside zone
                 if(!player.getRank().canCreateZones())return;
                 if(player.getZoneBlock1() == null && player.getZoneBlock2() == null){
                     //player has selected no blocks
                     player.setZoneBlock1(block);
                     player.sendMessage(ChatColor.YELLOW + "First block of a new zone selected at " + format(block.getLocation()));
                     event.setCancelled(true);
                     return;
                 }else if(player.getZoneBlock1() != null && player.getZoneBlock2() == null){
                     //player has selected first block
                     player.setZoneBlock2(block);
                     player.sendMessage(ChatColor.YELLOW + "Second block of a new zone selected at " + format(block.getLocation()));
                     event.setCancelled(true);
                 }else if(player.getZoneBlock1() != null && player.getZoneBlock2() != null){
                     //both blocks selected
                     player.setZoneBlock2(null);
                     player.setZoneBlock1(block);
                     player.sendMessage(ChatColor.YELLOW + "First block of a new zone selected at " + format(block.getLocation()));
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
     
     public String format(Location loc)
     {
         return String.format("[%d, %d]", loc.getBlockX(), loc.getBlockZ());
     }
 }
