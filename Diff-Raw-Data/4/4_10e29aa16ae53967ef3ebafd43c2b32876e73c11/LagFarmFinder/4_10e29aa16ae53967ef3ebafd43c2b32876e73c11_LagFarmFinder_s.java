 /*
  * Author: John "Ubertweakstor" Board
  * Date: 21/12/12 14:10
  * Description: XRay Detector Plugin
  */
 
 package ubertweakstor.lagfarmfinder;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LagFarmFinder extends JavaPlugin {
     
     static final Logger log = Logger.getLogger("Minecraft");
 
     /*
      * Name: onEnable Description: Called when plugin is enabled. Returns: None
      * Parameters: None Requirements: None
      */
     @Override
     public void onEnable() {
         log.info("Enabled.");
     }
 
     /*
      * Name: onDisable Description: Called when plugin is disabled. Returns:
      * None Parameters: None Requirements: None
      */
     @Override
     public void onDisable() {
         log.info("Disabled.");
     }
 
     // =====[Util]=====//
 
     /*
      * Name: onCommand Description: Called when a command has been executed
      * Returns: boolean Parameters: CommandSender sender, Command cmd, String
      * label, String[] args Requirements: None
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         Player ply = (Player) sender;
         if (label.equalsIgnoreCase("mobcount")){
             if (args.length!=2){
                 ply.sendMessage(ChatColor.RED+"ERROR: Invalid Syntax.");
             }
             if (getServer().getPlayer(args[0])==null){
                 ply.sendMessage(ChatColor.RED+"ERROR: Player Not Online");
                 return true;
             }
             if(Integer.valueOf(args[1])>200){
                 ply.sendMessage(ChatColor.RED+"ERROR: Please do not select a radius higher than 200.");
                 return true;
             }
             try{
                 Integer.valueOf(args[1]);
             }catch (Exception ex){
                 ply.sendMessage(ChatColor.RED+"Radius not valid.");
                 return true;
             }
             ply.sendMessage(ChatColor.BLUE+"Number of mobs in a "+args[1]+
                     " radius around "+ChatColor.BLUE+args[0]+": "+
                     ChatColor.RED+String.valueOf(getNumberOfEntitiesNear(getServer().getPlayer(args[0]), Integer.valueOf(args[1]))));
             return true;
         } else if(label.equalsIgnoreCase("findlag")){ 
             HashMap<Player, Integer> top = new HashMap<Player, Integer>();
            if(Integer.valueOf(args[0])>200 && args.length==0){
                 ply.sendMessage(ChatColor.RED+"ERROR: Please do not select a radius higher than 200.");
                 return true;
             }
             for(Player p: getServer().getOnlinePlayers()){            
                 if (args.length >= 1){
                     top.put(p, getNumberOfEntitiesNear(p, Integer.valueOf(args[0])));                    
                 }                
                 else{
                     top.put(p, getNumberOfEntitiesNear(p, 80));                    
                 }
             }
             
             int iterations = 0;
             if (top.size() > 10){
                 iterations = 10;
             } else {
                 iterations = top.size();
             }            
             int cnt = 1;
             for(int x = 0; x!=iterations; x++){
                 Map.Entry<Player, Integer> maxEntry = null;            
                 for(Map.Entry<Player, Integer> entry: top.entrySet()){
                     if (maxEntry == null || entry.getValue()>maxEntry.getValue()){
                         maxEntry = entry;
                     }        
                 }                
                 ply.sendMessage(ChatColor.GREEN+String.valueOf(cnt)+
                         ". "+ChatColor.RED+((Player)maxEntry.getKey()).getName()+
                         ": "+ChatColor.BLUE+String.valueOf(maxEntry.getValue()));
                 top.remove(maxEntry.getKey());
                 cnt++;
             }
                                     
         }
         return true;
     }
     
     public int getNumberOfEntitiesNear(Player p, int radius){
         try{
             return p.getNearbyEntities(radius, radius, radius).size();
         } catch (Exception ex){
             return -1;
         }        
     }
 }
