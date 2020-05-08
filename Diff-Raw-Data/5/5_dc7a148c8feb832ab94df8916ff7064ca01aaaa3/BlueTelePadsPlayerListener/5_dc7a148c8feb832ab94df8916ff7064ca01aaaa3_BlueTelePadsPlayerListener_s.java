 package Ne0nx3r0.BlueTelePads;
 
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Material;
 import org.bukkit.event.block.Action;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.Location;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class BlueTelePadsPlayerListener extends PlayerListener {
     private final BlueTelePads plugin;
     private static Map<String, int[]> mLapisLinks = new HashMap<String, int[]>();
 
     public BlueTelePadsPlayerListener(BlueTelePads instance){
         this.plugin = instance;
     }
 
     public static boolean isTelePadLapis(Block lapisBlock){
         if(lapisBlock.getType() == Material.LAPIS_BLOCK
         && lapisBlock.getFace(BlockFace.EAST).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.WEST).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.NORTH).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.SOUTH).getType() == Material.DOUBLE_STEP
         && lapisBlock.getFace(BlockFace.DOWN).getType() == Material.SIGN_POST
         && lapisBlock.getFace(BlockFace.UP).getType() == Material.STONE_PLATE){
             return true;
         }
         return false;
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event){
         if(event.getItem() != null 
         && event.getItem().getType() == Material.REDSTONE
         && isTelePadLapis(event.getClickedBlock().getFace(BlockFace.UP))){
             if(!mLapisLinks.containsKey(event.getPlayer().getName())){
                 Location lClicked = event.getClickedBlock().getFace(BlockFace.UP).getLocation();
 
                 mLapisLinks.put(event.getPlayer().getName(),new int[] {lClicked.getBlockX(),lClicked.getBlockY(),lClicked.getBlockZ()});
 
                 event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Telepad location stored!");
             }else{
                 int[] iLinkCoords = mLapisLinks.get(event.getPlayer().getName());
 
                 Block bFirstLapis = event.getClickedBlock().getWorld().getBlockAt(iLinkCoords[0],iLinkCoords[1],iLinkCoords[2]);
 
                 if(isTelePadLapis(bFirstLapis)){
                     Sign sFirstLink = (Sign) bFirstLapis.getFace(BlockFace.DOWN).getState();
                     Sign sSecondLink = (Sign) event.getClickedBlock().getState();
                     Location lSecondLink = event.getClickedBlock().getFace(BlockFace.UP).getLocation();
 
                     sFirstLink.setLine(1,Integer.toString(lSecondLink.getBlockX()));
                     sFirstLink.setLine(2,Integer.toString(lSecondLink.getBlockY()));
                     sFirstLink.setLine(3,Integer.toString(lSecondLink.getBlockZ()));
                     sFirstLink.update(true);
 
                     sSecondLink.setLine(1,Integer.toString(iLinkCoords[0]));
                     sSecondLink.setLine(2,Integer.toString(iLinkCoords[1]));
                     sSecondLink.setLine(3,Integer.toString(iLinkCoords[2]));
                     sSecondLink.update(true);
 
                     mLapisLinks.remove(event.getPlayer().getName());
 
                     event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Telepad location transferred!");
                 }
             }
         }
         else if(event.getAction() == Action.PHYSICAL
         && event.getClickedBlock().getType() == Material.STONE_PLATE
         && event.getClickedBlock().getFace(BlockFace.DOWN).getType() == Material.LAPIS_BLOCK){
 
             Block bLapis = event.getClickedBlock().getFace(BlockFace.DOWN);
 
             if(isTelePadLapis(bLapis)){
                 Sign sbPortalSign = (Sign) bLapis.getFace(BlockFace.DOWN).getState();
                 String[] sBlockLines = sbPortalSign.getLines();
 
                 Block bReceiverLapis;
 
                 try{
                     bReceiverLapis = sbPortalSign.getWorld().getBlockAt(Integer.parseInt(sBlockLines[1]),Integer.parseInt(sBlockLines[2]),Integer.parseInt(sBlockLines[3]));
                 }
                 catch(Exception e){
                     return;
                 }
 
                 if(isTelePadLapis(bReceiverLapis)){
                     Sign sbReceiverSign = (Sign) bReceiverLapis.getFace(BlockFace.DOWN).getState();
 
                     event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Telepad: Preparing to send you to "+sbReceiverSign.getLine(0)+", stand still!");
 
                    plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new BluePadTeleport(plugin,event.getPlayer(),event.getPlayer().getLocation(),bLapis,bReceiverLapis),60);
                 }
             }
         }
     }
 
     private static int getDistance(Location loc1,Location loc2){
         return (int) Math.sqrt(Math.pow(loc2.getBlockX()-loc1.getBlockX(),2)+Math.pow(loc2.getBlockY()-loc1.getBlockY(),2)+Math.pow(loc2.getBlockZ()-loc1.getBlockZ(),2));
     }
 
     private static class BluePadTeleport implements Runnable{
         private final BlueTelePads plugin;
         private final Player player;
         private final Location player_location;
         private final Block receiver;
         private final Block sender;
 
         BluePadTeleport(BlueTelePads plugin,Player player,Location player_location,Block senderLapis,Block receiverLapis){
             this.plugin = plugin;
             this.player = player;
             this.player_location = player_location;
             this.sender = senderLapis;
             this.receiver = receiverLapis;
         }
 
         public void run(){
             if(getDistance(player_location,player.getLocation()) > 1){
                 player.sendMessage(ChatColor.DARK_AQUA + "Telepad: You moved, cancelling teleport!");
                 return;
             }
             if(isTelePadLapis(sender) && isTelePadLapis(receiver)){
                 player.sendMessage(ChatColor.DARK_AQUA + "Telepad: Here goes nothing!");
 
                 Location lSendTo = receiver.getFace(BlockFace.UP,2).getFace(BlockFace.NORTH).getLocation();
                 lSendTo.setX(lSendTo.getX()+0.5);
                 lSendTo.setZ(lSendTo.getZ()+0.5);
 
                 player.teleport(lSendTo);
                 
                 receiver.getFace(BlockFace.UP,3).setType(Material.WATER);
                 
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){
                     public void run(){
                         receiver.getFace(BlockFace.UP,3).setType(Material.AIR);
                     }
                 },20);
                 
             }else{
                 player.sendMessage(ChatColor.DARK_AQUA + "Telepad: Something went wrong! Just be grateful you didn't get split in half!");
             }
         }
     }
 }
