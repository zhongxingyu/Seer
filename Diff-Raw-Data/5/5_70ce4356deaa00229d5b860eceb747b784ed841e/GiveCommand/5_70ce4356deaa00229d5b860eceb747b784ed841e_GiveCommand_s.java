 package info.bytecraft.commands;
 
 import java.util.List;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import static org.bukkit.ChatColor.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 public class GiveCommand extends AbstractCommand
 {
     public GiveCommand(Bytecraft Bytecraft)
     {
         super(Bytecraft, "give");
     }
 
     @Override
     @SuppressWarnings("deprecation")
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
        if (args.length == 0)
             return true;
         if (!player.isAdmin())
             return true;

         String param = args[1].toUpperCase();
         List<BytecraftPlayer> cantidates = plugin.matchPlayer(args[0]);
         if (cantidates.size() != 1) {
             return true;
         }
 
         BytecraftPlayer target = cantidates.get(0);
 
         int materialId;
         try {
             materialId = Integer.parseInt(param);
         } catch (NumberFormatException e) {
             try {
                 Material material = Material.getMaterial(param);
                 materialId = material.getId();
             } catch (NullPointerException ne) {
                 player.sendMessage(DARK_AQUA
                         + "/item <id|name> <amount> <data>.");
                 return true;
             }
         }
 
         int amount;
         try {
             amount = Integer.parseInt(args[2]);
         } catch (ArrayIndexOutOfBoundsException e) {
             amount = 1;
         } catch (NumberFormatException e) {
             amount = 1;
         }
 
         int data;
         try {
             data = Integer.parseInt(args[3]);
         } catch (ArrayIndexOutOfBoundsException e) {
             data = 0;
         } catch (NumberFormatException e) {
             data = 0;
         }
 
         ItemStack item = new ItemStack(materialId, amount, (byte) data);
         if (item.getType() == Material.MONSTER_EGG
                 || item.getType() == Material.NAME_TAG) {
             return false;
         }
 
         PlayerInventory inv = target.getInventory();
         inv.addItem(item);
 
         Material material = Material.getMaterial(materialId);
         String materialName = material.toString();
 
         player.sendMessage("You gave " + amount + " of " + DARK_AQUA
                 + materialName.toLowerCase() + " to " + target.getName() + ".");
         target.sendMessage(YELLOW
                 + "You were gifted by the gods. Look in your " + "inventory!");
 
         return true;
     }
 
     @SuppressWarnings("deprecation")
     public boolean handleOther(Server server, String[] args)
     {
         if (args.length == 0)
             return true;
         String param = args[1].toUpperCase();
         Player delegate = Bukkit.getPlayer(args[0]);
         if (delegate != null) {
             BytecraftPlayer target = plugin.getPlayer(delegate);
 
             int materialId = 0;
             try {
                 materialId = Integer.parseInt(param);
             } catch (NumberFormatException e) {
                 try {
                     Material material = Material.getMaterial(param);
                     materialId = material.getId();
                 } catch (NullPointerException ne) {
                     return true;
                 }
             }
 
             int amount;
             try {
                 amount = Integer.parseInt(args[2]);
             } catch (ArrayIndexOutOfBoundsException e) {
                 amount = 1;
             } catch (NumberFormatException e) {
                 amount = 1;
             }
 
             int data;
             try {
                 data = Integer.parseInt(args[3]);
             } catch (ArrayIndexOutOfBoundsException e) {
                 data = 0;
             } catch (NumberFormatException e) {
                 data = 0;
             }
 
             ItemStack item = new ItemStack(materialId, amount, (byte) data);
             if (item.getType() == Material.MONSTER_EGG
                     || item.getType() == Material.NAME_TAG)
                 return true;
 
             PlayerInventory inv = target.getInventory();
             inv.addItem(item);
 
             Material material = Material.getMaterial(materialId);
             String materialName = material.toString();
 
             plugin.getLogger().info(
                     "You gave " + amount + " of " + DARK_AQUA
                             + materialName.toLowerCase() + " to "
                             + target.getName() + ".");
             target.sendMessage(YELLOW
                     + "You were gifted by the gods. Look in your "
                     + "inventory!");
 
         }
         return true;
     }
 }
