 package de.minestar.buycraft.commands;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import de.minestar.buycraft.core.Core;
 import de.minestar.buycraft.manager.DatabaseManager;
 import de.minestar.buycraft.manager.ShopManager;
 import de.minestar.buycraft.shops.UserShop;
 import de.minestar.buycraft.shops.old.BCItemStack;
 import de.minestar.buycraft.shops.old.BCUserShop;
 import de.minestar.buycraft.units.BlockVector;
 import de.minestar.buycraft.units.PersistentBuyCraftStack;
 import de.minestar.buycraft.units.PersistentAlias;
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class ImportCommand extends AbstractCommand {
 
     private ShopManager shopManager;
 
     public ImportCommand(String syntax, String arguments, String node, ShopManager shopManager) {
         super(Core.NAME, syntax, arguments, node);
         this.description = "Import shops & aliases from old filesystem";
         this.shopManager = shopManager;
     }
 
     public void execute(String[] args, Player player) {
         PlayerUtils.sendInfo(player, Core.NAME, "Starting import...");
         this.importAliases(player);
         this.importUsershops(player);
         PlayerUtils.sendSuccess(player, Core.NAME, "Import complete.");
     }
 
     private void importUsershops(Player player) {
         File folder = new File("plugins/BuyCraft/UserShops");
         folder.mkdirs();
 
         File[] fileList = folder.listFiles();
         int count = 0, failed = 0;
 
         for (File file : fileList) {
             if (!file.isFile())
                 continue;
 
             if (!file.getName().endsWith(".bcf"))
                 continue;
 
             try {
                 String positionString = file.getName().replace(".bcf", "");
                 ObjectInputStream objIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file.getAbsolutePath())));
                 BCUserShop shop = (BCUserShop) objIn.readObject();
                 objIn.close();
 
                 if (shop != null) {
                     String[] split = positionString.split("__");
                     if (split.length == 2) {
                         String[] splitPos = split[1].split("_");
                         if (splitPos.length == 3) {
                             World world = Bukkit.getWorld(split[0]);
                             if (world != null) {
                                 BlockVector position = new BlockVector(split[0], Integer.valueOf(splitPos[0]), Integer.valueOf(splitPos[1]), Integer.valueOf(splitPos[2]));
                                 UserShop newShop = null;
                                 if (position != null) {
                                     newShop = this.shopManager.addUsershop(position);
                                 }
 
                                 if (newShop == null) {
                                     failed++;
                                     continue;
                                 }
 
                                 boolean error = false;
                                if (shop.getShopInventory() != null && shop.isActive()) {
                                     for (BCItemStack oldStack : shop.getShopInventory()) {
                                         if (oldStack.getAmount() < 1)
                                             continue;
                                         PersistentBuyCraftStack newStack = DatabaseManager.getInstance().createItemStack(newShop, oldStack.getId(), oldStack.getSubId(), oldStack.getAmount());
                                         if (newStack == null) {
                                             newShop.addItem(newStack);
                                         } else {
                                             error = true;
                                             break;
                                         }
                                     }
                                 }
 
                                 // do we have an error?
                                 if (error) {
                                     failed++;
                                     continue;
                                 }
 
                                 if (newShop.isValid()) {
                                    if (DatabaseManager.getInstance().setUsershopActive(newShop, shop.isActive()) && DatabaseManager.getInstance().setUsershopFinished(newShop, shop.isShopFinished())) {
                                        count++;
                                    } else {
                                        failed++;
                                    }
                                 } else {
                                     failed++;
                                 }
                             }
                         }
                     }
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 ConsoleUtils.printError(Core.NAME, "Error while reading file: " + file.getName());
                 failed++;
             }
         }
         PlayerUtils.sendInfo(player, Core.NAME, "Usershops imported: " + count + " / " + ChatColor.RED + failed);
     }
     @SuppressWarnings("unchecked")
     private void importAliases(Player player) {
         HashMap<String, String> aliasList;
         File folder = new File("plugins/BuyCraft/aliases.bcf");
         folder.mkdirs();
         if (!folder.exists()) {
             aliasList = new HashMap<String, String>();
             return;
         }
 
         try {
             ObjectInputStream objIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream("plugins/BuyCraft/aliases.bcf")));
             aliasList = (HashMap<String, String>) objIn.readObject();
             objIn.close();
 
             int count = 0, failed = 0;
             for (Map.Entry<String, String> entry : aliasList.entrySet()) {
                 PersistentAlias alias = this.shopManager.addAlias(entry.getKey(), entry.getValue());
                 if (alias != null) {
                     count++;
                 } else {
                     failed++;
                 }
             }
             PlayerUtils.sendInfo(player, Core.NAME, "Aliases imported: " + count + " / " + ChatColor.RED + failed);
         } catch (Exception e) {
             aliasList = new HashMap<String, String>();
             ConsoleUtils.printError(Core.NAME, "Error while reading file: plugins/BuyCraft/aliases.bcf");
         }
     }
 }
