 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.indiplex.virtualchests;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import net.minecraft.server.InventoryLargeChest;
 import org.bukkit.craftbukkit.inventory.CraftInventory;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author temp
  */
 public class VCManager {
 
     private HashMap<String, InventoryLargeChest> chests = new HashMap<String, InventoryLargeChest>();
     private HashMap<String, ItemStack[]> invs = new HashMap<String, ItemStack[]>();
     private File saveFolderChests;
     private File saveFolderInvs;
 
     public VCManager() {
         File dFolder = VCMain.getAPI().getDataFolder();
         saveFolderChests = new File(dFolder, "chests");
         saveFolderInvs = new File(dFolder, "inventories");
         if (!saveFolderChests.exists()) {
             saveFolderChests.mkdirs();
         }
         if (!saveFolderInvs.exists()) {
             saveFolderInvs.mkdirs();
         }
     }
 
     public boolean addChest(String id) {
         if (chests.get(id) != null) {
             return false;
         }
         chests.put(id, new InventoryLargeChest(id, new VCChest(), new VCChest()));
         return true;
     }
     
     public boolean add(Inventory inv, String id) {
         return add(inv.getContents().clone(), id);
     }
     
     public boolean add(ItemStack[] content, String id) {
         if (invs.get(id)!=null) {
             return false;
         }
         invs.put(id, content);
         return true;
     }
 
     public Inventory getChest(String id) {
         InventoryLargeChest get = chests.get(id);
         if (get==null) {
             return null;
         }
         return new CraftInventory(get);
     }
     
     public ItemStack[] getInventoryContent(String id) {
         return invs.get(id);
     }
     
     public boolean hasChest(String id) {
         return chests.containsKey(id);
     }
     
     public boolean hasInv(String id) {
         return invs.containsKey(id);
     }
 
     public boolean removeChest(String id) {
         InventoryLargeChest c = chests.remove(id);
         remove(id, saveFolderChests);
         
         if (c == null) {
             return false;
         }
         return true;
     }
     
     public boolean removeInventory(String id) {
         ItemStack[] inv = invs.remove(id);
         remove(id, saveFolderInvs);
         
         if (inv == null) {
             return false;
         }
         return true;
     }
     
     private void remove(String id, File saveFolder) {
         File f = new File(saveFolder, id);
         if (f.exists()) {
             f.delete();
         }
     }
 
     public void load() {
         chests.clear();
 
         int loadedChests = 0;
         int loadedInvs = 0;
         
         final FilenameFilter filter = new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".chest");
             }
         };
         final FilenameFilter filter2 = new FilenameFilter() {
 
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".inv");
             }
         };
         for (File chestFile : saveFolderChests.listFiles(filter)) {
             try {
                 final String chestName = chestFile.getName().substring(0, chestFile.getName().length() - 6);
                 final InventoryLargeChest chest = new InventoryLargeChest(chestName, new VCChest(), new VCChest());
 
                 final BufferedReader in = new BufferedReader(new FileReader(chestFile));
 
                 String line;
                 int field = 0;
                 while ((line = in.readLine()) != null) {
                     if (!line.equals("")) {
                         final String[] parts = line.split(":");
                         try {
                             int type = Integer.parseInt(parts[0]);
                             int amount = Integer.parseInt(parts[1]);
                             short damage = Short.parseShort(parts[2]);
                             if (type != 0) {
                                 chest.setItem(field, new net.minecraft.server.ItemStack(type, amount, damage));
                             }
                         } catch (NumberFormatException e) {
                             // ignore
                         }
                         ++field;
                     }
                 }
 
                 in.close();
                 chests.put(chestName, chest);                
 
                 ++loadedChests;
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         for (File chestFile : saveFolderInvs.listFiles(filter2)) {
             try {
                 ArrayList<ItemStack> iss = new ArrayList<ItemStack>();
                 final String invName = chestFile.getName().substring(0, chestFile.getName().length() - 4);
                 final BufferedReader in = new BufferedReader(new FileReader(chestFile));
 
                 String line;
                 int field = 0;
                 while ((line = in.readLine()) != null) {
                     if (!line.equals("")) {
                         final String[] parts = line.split(":");
                         try {
                             int type = Integer.parseInt(parts[0]);
                             int amount = Integer.parseInt(parts[1]);
                             short damage = Short.parseShort(parts[2]);
                             iss.add(new CraftItemStack(type, amount, damage));
                         } catch (NumberFormatException e) {
                             // ignore
                         }
                         ++field;
                     }
                 }
 
                 in.close();
                 ItemStack[] a = new ItemStack[iss.size()];
                 a = iss.toArray(a);
                 invs.put(invName, a);
 
                 ++loadedInvs;
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         VCMain.log.info(String.valueOf("[VC] "+loadedChests+" chests and "+loadedInvs+" inventories loaded!"));
     }
 
     public void save() {
         File f = VCMain.getAPI().getDataFolder();              
         
         if (!f.exists()) {
             f.mkdir();
         }
         File[] listFiles = f.listFiles();
         for(File fi : listFiles) {
             fi.delete();
         }
        
        File folder = new File(f, "Chests");
        if (!folder.exists()) {
            folder.mkdir();
        }
         for (int i = 0; i < chests.size(); i++) {
             try {
                 for (String cName : chests.keySet()) {
                     Inventory inv = getChest(cName);
 
                    File chestFile = new File(folder, cName + ".chest");
                     if (chestFile.exists()) {
                         chestFile.delete();
                     }
                     chestFile.createNewFile();
 
                     BufferedWriter bw = new BufferedWriter(new FileWriter(chestFile));
 
                     for (ItemStack stack:inv.getContents()) {
                         if (stack != null) {
                             bw.write(stack.getTypeId() + ":" + stack.getAmount() + ":" + stack.getDurability() + "\r\n");
                         } else {
                             bw.write("0:0:0\r\n");
                         }
                     }
 
                     bw.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public HashMap<String, InventoryLargeChest> get() {
         return chests;
     }
     
 }
