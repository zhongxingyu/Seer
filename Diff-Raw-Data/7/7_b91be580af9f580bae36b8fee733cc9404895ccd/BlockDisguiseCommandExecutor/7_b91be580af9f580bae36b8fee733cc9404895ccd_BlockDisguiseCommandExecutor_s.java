 package com.ne0nx3r0.blockdisguise;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class BlockDisguiseCommandExecutor implements CommandExecutor {
     private BlockDisguise p;
     public static String USAGE = "Usage: /bd <blockId|blockName> [blockData]";
 
     public BlockDisguiseCommandExecutor(BlockDisguise plugin){
         this.p = plugin;
     }
     
     @Override
     public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
         if (!(cs instanceof Player)){
             System.out.println("BlockDiguise cannot be run from the console.");
             return true;
         }
         
         Player player = (Player) cs;
         String sPlayer = player.getName();
         
         if(player.hasPermission("BlockDisguise.disguise")){
             if(args.length < 1){
                 p.msg(player,USAGE);
             }
             
             if(!BlockDisguise.enabledFor.containsKey(sPlayer)){
                 Integer[] iBlockData = new Integer[2];
 
                 Material m = null;
                 try{
                     m = Material.getMaterial(Integer.parseInt(args[0]));
                 }catch(Exception e){
                     m = Material.matchMaterial(args[0]);
                    
                    if(m == null){
                        p.msg(player,"'"+args[0]+"' is not a valid material!");
                         return true;
                     } 
                 }
                 
                 iBlockData[0] = m.getId();
                 
                 if(args.length > 1){
                     try{
                         iBlockData[1] = Integer.parseInt(args[1]);
                     }catch(Exception e){              
                         p.msg(player, USAGE);
                         return true;
                     }
                 }else{
                     iBlockData[1] = 0;
                 }
 
                 BlockDisguise.enabledFor.put(sPlayer,iBlockData);
                 BlockDisguise.pending.add(sPlayer);
                 
                 for(Player pHideFrom: p.getServer().getOnlinePlayers()){
                     pHideFrom.hidePlayer(player);
                 }
                 
                 p.msg(player,"Disguised as a "+m.name()+"!");
                 
                 if(p.getConfig().getBoolean("verbose-logging")){
                     p.log(sPlayer + " disguised as a "+m.name());
                 }
             }else{
                 p.msg(player,"Undisguised");
                 
                 Block lastUpdate = BlockDisguise.lastUpdate.get(sPlayer);
                 
                 BlockDisguise.lastUpdate.remove(sPlayer);
                 BlockDisguise.enabledFor.remove(sPlayer);
                 
                 if(lastUpdate != null){
                     for(Player pHideFrom : player.getWorld().getPlayers()){
                         pHideFrom.sendBlockChange(lastUpdate.getLocation(), lastUpdate.getType(), lastUpdate.getData());
                     }
                 }
                 
                 if(p.MAKE_PLAYERS_INVISIBLE){
                     for(Player pHideFrom: p.getServer().getOnlinePlayers()){
                         pHideFrom.showPlayer(player);
                     }
                 }
             }
         }else{
             p.msg(player, ChatColor.RED+"You do not have permission to use this command.");
         }
         
         return true;
     }//End onCommand
 }
