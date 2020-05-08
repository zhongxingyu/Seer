 package com.modwiz.blessprotect;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
import java.util.HashSet;

 /**
  * Created with IntelliJ IDEA.
  * User: starbuck
  * Date: 1/3/13
  * Time: 12:05 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BlessProtect extends JavaPlugin{
     public ClaimManager claimManager;
 
     public void onEnable() {
         registerSerializables();
         claimManager = new ClaimManager(this);
         getServer().getPluginManager().registerEvents(new ClaimListener(this), this);
     }
 
     public void onDisable() {
         claimManager.disable();
         claimManager = null;
     }
 
 
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         String command = cmd.getName();
 
         if (!(sender instanceof Player)) {
             sender.sendMessage("Cannot be used from console");
             return true;
         }
 
         if (command.equalsIgnoreCase("bless")) {
             if (!sender.hasPermission("blessme.bless") &&
                     !sender.isOp()) {
                 sender.sendMessage("You don't have permission to do that");
                 return true;
             }
             if (args.length == 1) {
                 Player context = (Player) sender;
                HashSet<Byte> set = new HashSet<Byte>();
                set.add((byte)Material.AIR.getId());
                set.add((byte) Material.SNOW.getId());
                Block aimedAt = context.getTargetBlock(set, 20);
                 ClaimedBlock claimedBlock;
                 switch (aimedAt.getType()) {
                     case WOOD_BUTTON:
                     case STONE_BUTTON:
                         claimedBlock = new ClaimedBlock(aimedAt, args[0]);
                         claimManager.addClaim(claimedBlock);
                         sender.sendMessage("button has been claimed.");
                         break;
                     case IRON_DOOR_BLOCK:
                     case WOODEN_DOOR:
                     case WOOD_DOOR:
                         claimedBlock = new ClaimedBlock(aimedAt, args[0]);
                         claimManager.addClaim(claimedBlock);
                         Block down = aimedAt.getRelative(BlockFace.DOWN);
                         Block up = aimedAt.getRelative(BlockFace.UP);
 
                         if ((down.getType() == Material.IRON_DOOR_BLOCK) ||
                                 (down.getType()== Material.WOODEN_DOOR) ||
                                 (down.getType() == Material.WOOD_DOOR)) {
                             claimedBlock = new ClaimedBlock(down, args[0]);
                             claimManager.addClaim(claimedBlock);
                             break;
                         }
 
                         if ((up.getType() == Material.IRON_DOOR_BLOCK) ||
                                 (up.getType()== Material.WOODEN_DOOR) ||
                                 (up.getType() == Material.WOOD_DOOR)) {
                             claimedBlock = new ClaimedBlock(up, args[0]);
                             claimManager.addClaim(claimedBlock);
                             break;
                         }
 
                         sender.sendMessage("Door block has been claimed.");
                         break;
                     case CHEST:
                         break;
                     default:
                         sender.sendMessage("Thats not a button, chest or door sorry.");
                         break;
                 }
                 return true;
             } else {
                 sender.sendMessage("Sorry the format is /bless <player>");
                 return true;
             }
         }
 
         return false;
     }
 
     public void registerSerializables() {
         ConfigurationSerialization.registerClass(ClaimLocation.class);
         ConfigurationSerialization.registerClass(ClaimedBlock.class);
     }
 }
