 package com.geNAZt.RegionShop.Listener;
 
 import com.geNAZt.RegionShop.Interface.ShopCommand;
 import com.geNAZt.RegionShop.Interface.SignCommand;
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Interface.Sign.Equip;
 import com.geNAZt.RegionShop.Util.Chat;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
 
 import static com.geNAZt.RegionShop.Util.Loader.loadFromJAR;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 09.06.13
  */
 public class SignChange implements Listener {
     private final RegionShopPlugin plugin;
    private CopyOnWriteArrayList<SignCommand> signCommands = new CopyOnWriteArrayList<SignCommand>();
 
     public SignChange(RegionShopPlugin pl) {
         plugin = pl;
 
         signCommands = loadFromJAR(pl, "com.geNAZt.RegionShop.Interface.Sign", SignCommand.class);
 
         for(Object command : signCommands) {
             SignCommand signCommand = (SignCommand) command;
 
             if(!pl.getConfig().getBoolean("interfaces.sign." + signCommand.getCommand(), true)) {
                 signCommands.remove(signCommands.indexOf(signCommand));
             }
         }
 
         pl.getLogger().info("Loaded SignCommands: " + signCommands.toString());
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onSignChange(SignChangeEvent e) {
         Player p = e.getPlayer();
 
         Block signBlock = e.getBlock();
         if (signBlock == null) {
             plugin.getLogger().warning("Player " + p.getName() + " tried to generate a fake sign.");
             return;
         }
 
         if(e.getLine(0).contains("[RegionShop]")) {
             for(SignCommand command : signCommands) {
                 if(command.getCommand().equalsIgnoreCase(e.getLine(1))) {
                     if(command.getPermissionNode() == null || p.hasPermission(command.getPermissionNode())) {
                         command.execute(p, signBlock, e.getLines());
                         return;
                     } else {
                         p.sendMessage(Chat.getPrefix() + ChatColor.RED + "You don't have the permission " + ChatColor.DARK_RED + command.getPermissionNode());
                         signBlock.breakNaturally();
                         return;
                     }
                 }
             }
 
 
             p.sendMessage(Chat.getPrefix() + ChatColor.RED + "Invalid RegionShop Sign");
             signBlock.breakNaturally();
         }
     }
 
 
 }
