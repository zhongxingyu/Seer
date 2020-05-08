 package me.ChrizC.lockbuy;
 
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public class LBBlockListener extends BlockListener {
     
     private LockBuy plugin;
     
     //Initiate variables
     Sign sign;
     Sign locketteSign;
     World world;
     Player player;
     Pattern p = Pattern.compile("([-+]?[0-9]\\d{0,2}(\\.\\d{1,2})?%?)");
     boolean signFound;
     
     //Initiate chat prefix.
     public static final String ERR_PREFIX = ChatColor.RED + "[LockBuy] ";
     
     public LBBlockListener(LockBuy instance) {
         plugin = instance;
     }
     
     @Override
     public void onSignChange(SignChangeEvent event) {
         sign = (Sign) event.getBlock().getState();
         world = event.getBlock().getWorld();
         player = event.getPlayer();
         signFound = false;
         
         //Is this sign one that we're looking for?
         if (event.getLine(0).equals("[LockBuy]")) {
             //Yes, it is!
             BlockFace[] array = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
             
             
             //Let's check every adjacent block, and see if we're next to a Lockette sign.
             for (BlockFace face : array) {
                 BlockState state = sign.getBlock().getFace(face).getState();
                 if (state instanceof Sign) {
                     Sign temp = (Sign) state;
                     if (temp.getLine(0).equalsIgnoreCase("[Private]")) {
                         locketteSign = temp;
                         signFound = true;
                     }
                 }
             }
             
             //Are we next to a Lockette sign?
             if (signFound == true) {
                 //Yes, we are!
                 
                 //Are we making a sign for ourselves?
                 if (!locketteSign.getLine(1).equals(player.getName())) {
                     //Does the player have permission to do this?
                     if (plugin.permissionsCheck(player, "lockbuy.admin.place", true) == true) {
                         //Yes, he does!
 
                         //Has the player provided a feasible number?
                         Matcher m = p.matcher(event.getLine(1));
                         if (m.find() == true) {
                             //Yes.
 
                             //Is number negative?
                            if (Double.parseDouble(m.group()) > 0) {
                                 //No.
                                 event.setLine(3, ChatColor.GREEN + "[Active]");
                             } else {
                                 //Yes.
                                 player.sendMessage(ERR_PREFIX + "You cannot define a negative value, or 0.");
                                 sign.getBlock().setType(Material.AIR);
                                 player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
                             }
                         } else {
                             //No.
                             player.sendMessage(ERR_PREFIX + "You must define a proper price.");
                             sign.getBlock().setType(Material.AIR);
                             player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
                         }
                     } else {
                         //No, he doesn't!
 
                         player.sendMessage(ERR_PREFIX + "Error! You do not have the correct Permission to place this sign.");
                         sign.getBlock().setType(Material.AIR);
                         player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
                     }
                 } else {
                     //Does the player have permission to do this?
                     if (plugin.permissionsCheck(player, "lockbuy.user.place", false) == true || plugin.permissionsCheck(player, "lockbuy.admin.place", true) == true) {
                         //Yes, he does!
 
                         //Has the player provided a feasible number?
                         Matcher m = p.matcher(event.getLine(1));
                         if (m.find() == true) {
                             //Yes.
 
                             //Is number negative?
                            if (Double.parseDouble(m.group()) > 0) {
                                 //No.
                                 event.setLine(3, ChatColor.GREEN + "[Active]");
                             } else {
                                 //Yes.
                                 player.sendMessage(ERR_PREFIX + "You cannot define a negative value, or 0.");
                                 sign.getBlock().setType(Material.AIR);
                                 player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
                             }
                         } else {
                             //No.
                             player.sendMessage(ERR_PREFIX + "You must define a proper price.");
                             sign.getBlock().setType(Material.AIR);
                             player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
                         }
                     } else {
                         //No, he doesn't!
 
                         player.sendMessage(ERR_PREFIX + "Error! You do not have the correct Permission to place this sign.");
                         sign.getBlock().setType(Material.AIR);
                         player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
                     }
                 }
             } else {
                 //No, we're not!
                 
                 player.sendMessage(ERR_PREFIX + "Error! No Lockette sign found!");
                 sign.getBlock().setType(Material.AIR);
                 player.getInventory().addItem(new ItemStack(Material.SIGN, 1));
             }
         }
     }
 }
