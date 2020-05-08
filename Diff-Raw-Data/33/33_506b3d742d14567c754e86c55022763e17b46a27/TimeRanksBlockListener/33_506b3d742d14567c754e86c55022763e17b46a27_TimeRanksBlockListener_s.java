 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.crosant.timeranks;
 
 import java.util.logging.Logger;
 import net.milkbowl.vault.Vault;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.Plugin;
 import ru.tehkode.permissions.PermissionEntity;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 /**
  *
  * @author Florian
  */
 public class TimeRanksBlockListener implements Listener{
 
     
     private final TimeRanks plugin;
 
     TimeRanksBlockListener(TimeRanks p) {
         plugin = p;
     }
     
     
     
     
         @EventHandler
             public void onBlockPlace(BlockPlaceEvent event){
                 
                 
             
                 long blocks;
                 Player player = event.getPlayer();
                 Block block = event.getBlock();
                 Material mat = block.getType();
                 //blocks = SQL.getBlocks(player);
                 blocks = TimeRanks.player_blocks.get(player.getName());
                 long blocks1 = blocks + 1;
                 //SQL.setBlocks(player, blocks1);
                 TimeRanks.player_blocks.put(player.getName(), blocks1);
                 
                // player.sendMessage(TimeRanks.player_blocks.get(player).toString());
                 
               if(Bukkit.getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
               PermissionManager manager = PermissionsEx.getPermissionManager();
 
               PermissionManager pex = PermissionsEx.getPermissionManager();
               PermissionEntity entity = pex.getUser(player);
               
               
               if(plugin.getConfig().getBoolean("Basic.activated") == true){
                   
              for(int i = 0; i <=25; i++){
                  
                   if(plugin.getConfig().getString("Rank."+ i +".name") != null){
                       if (i > 1){
                       if (TimeRanks.player_blocks.get(player.getName()).equals(plugin.getConfig().getLong("Rank." + i + ".blocks"))){
                    
                    player.sendMessage(plugin.getConfig().getString("Messages.rankup").replace("%rank%", plugin.getConfig().getString("Rank." + i + ".name")) );
                     plugin.giveCash(player, plugin.getConfig().getLong("Rank." + i + ".money"));
                   manager.getUser(player).addGroup(plugin.getConfig().getString("Rank." + i + ".name"));  
                   int o = i-1;
                   if(manager.getUser(player).getGroupsNames().toString().contains(plugin.getConfig().getString("Rank." + o + ".name")))manager.getUser(player).removeGroup(plugin.getConfig().getString("Rank."+ o + ".name"));   
                       }
                       else {
                                                if (TimeRanks.player_blocks.get(player.getName()).equals(plugin.getConfig().getLong("Rank." + i + ".blocks"))){
                     
                    player.sendMessage(plugin.getConfig().getString("Messages.rankup").replace("%rank%", plugin.getConfig().getString("Rank." + i + ".name")) );
                     plugin.giveCash(player, plugin.getConfig().getLong("Rank." + i + ".money"));
                   manager.getUser(player).addGroup(plugin.getConfig().getString("Rank." + i + ".name")); 
                                                 }
                       }
                 }
                   }
                   
              }
                 
 
               }
               else{
                   
               }
               }
               else {
                   
                      Logger.getLogger("Minecraft").warning("PermissionsEx plugin are not found.");
 
               }
               //  player.sendMessage("You placed a block with ID : " + mat);
     
 }
 }
