 package to.joe.strangeweapons.meta;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import to.joe.strangeweapons.Part;
 import to.joe.strangeweapons.Quality;
 import to.joe.strangeweapons.StrangeWeapons;
 import to.joe.strangeweapons.Util;
 import to.joe.strangeweapons.datastorage.DataStorageException;
 import to.joe.strangeweapons.datastorage.WeaponData;
 
 public class StrangeWeapon {
 
     public static StrangeWeapons plugin;
     public static List<String> idStrings = new ArrayList<String>();
 
     public static boolean isStrangeWeapon(ItemStack item) {
         if (item.hasItemMeta()) {
             ItemMeta meta = item.getItemMeta();
             if (meta.hasLore()) {
                 List<String> lore = item.getItemMeta().getLore();
                 String counter = ChatColor.stripColor(lore.get(lore.size() - 1));
                 for (String s : idStrings) {
                     Pattern p = Pattern.compile(s);
                     Matcher m = p.matcher(counter);
                     if (m.matches() && Integer.parseInt(m.group(1)) != 0) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private ItemStack item;
     private ItemMeta meta;
     private WeaponData data;
 
     public StrangeWeapon(ItemStack item) {
         this.item = item;
         meta = item.getItemMeta();
         int id = 0;
         String counter = ChatColor.stripColor(meta.getLore().get(meta.getLore().size() - 1));
         for (String s : idStrings) {
             Pattern p = Pattern.compile(s);
             Matcher m = p.matcher(counter);
             if (m.matches()) {
                 id = Integer.parseInt(m.group(1));
                 break;
             }
         }
         try {
             data = plugin.getDSI().getWeaponData(id).clone();
         } catch (DataStorageException e) {
             plugin.getLogger().log(Level.SEVERE, "Error reading strange weapon", e);
         }
     }
 
     public StrangeWeapon(ItemStack item, Quality quality, Part part) {
         data = new WeaponData();
         this.item = item;
         meta = item.getItemMeta();
         if (part != null) {
             LinkedHashMap<Part, Integer> firstPart = new LinkedHashMap<Part, Integer>();
             firstPart.put(part, 0);
             setParts(firstPart);
         }
         data.setQuality(quality);
         try {
             data = plugin.getDSI().saveNewWeaponData(data);
         } catch (DataStorageException e) {
             plugin.getLogger().log(Level.SEVERE, "Error creating strange weapon", e);
         }
     }
 
     public ItemStack previewItemStack() {
         List<String> lore = new ArrayList<String>();
         if (hasCustomName()) {
             meta.setDisplayName(getQuality().getPrefix() + ChatColor.ITALIC + getCustomName());
             if (getPrimary() != null) {
                 lore.add(ChatColor.WHITE + Util.getWeaponName(item, getPrimary().getValue(), getQuality()));
             }
         } else {
             if (getPrimary() != null) {
                 meta.setDisplayName(getQuality().getPrefix() + Util.getWeaponName(item, (int) (getPrimary().getValue() * getPrimary().getKey().getMultiplier()), getQuality()));
             } else {
                 meta.setDisplayName(getQuality().getPrefix() + Util.getWeaponName(item, 0, getQuality()));
             }
         }
         if (hasDescription()) {
             lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "Description: " + ChatColor.WHITE + data.getDescription());
         }
         if (getParts() != null) {
             for (Entry<Part, Integer> p : getParts().entrySet()) {
                 lore.add(ChatColor.WHITE + p.getKey().getName() + ": " + p.getValue());
             }
         }
         lore.add(ChatColor.WHITE + idStrings.get(0).replace("([0-9]+)", data.getWeaponId() + ""));
         meta.setLore(lore);
         item.setItemMeta(meta);
         return item;
     }
 
     public ItemStack getItemStack() {
         try {
             plugin.getDSI().updateWeaponData(data);
         } catch (DataStorageException e) {
             plugin.getLogger().log(Level.SEVERE, "Error updating strange weapon", e);
         }
         return previewItemStack();
     }
 
     @Override
     public ItemStack clone() {
         StrangeWeapon dupe = new StrangeWeapon(item);
         try {
             dupe.setWeaponData(plugin.getDSI().saveNewWeaponData(dupe.getWeaponData()));
         } catch (DataStorageException e) {
             plugin.getLogger().log(Level.SEVERE, "Error duplicating strange weapon", e);
         }
         return dupe.previewItemStack();
     }
 
     public Entry<Part, Integer> getPrimary() {
         if (getParts() != null) {
             for (Entry<Part, Integer> p : getParts().entrySet()) {
                 return p;
             }
         }
         return null;
     }
 
     public LinkedHashMap<Part, Integer> getParts() {
         return data.getParts();
     }
 
     public void setParts(LinkedHashMap<Part, Integer> parts) {
         data.setParts(parts);
     }
 
     public void incrementStat(Part part, int toAdd) {
        if (getParts() != null) {
            if (getParts().containsKey(part)) {
                int oldVal = getParts().get(part);
                getParts().put(part, oldVal + toAdd);
            }
         }
     }
 
     public boolean hasCustomName() {
         return data.getCustomName() == null ? false : true;
     }
 
     public void setCustomName(String name) {
         data.setCustomName(name);
     }
 
     public String getCustomName() {
         return data.getCustomName();
     }
 
     public boolean hasDescription() {
         return data.getDescription() == null ? false : true;
     }
 
     public void setDescription(String description) {
         data.setDescription(description);
     }
 
     public String getDescription() {
         return data.getDescription();
     }
 
     public void setQuality(Quality quality) {
         data.setQuality(quality);
     }
 
     public Quality getQuality() {
         return data.getQuality();
     }
 
     private void setWeaponData(WeaponData data) {
         this.data = data;
     }
 
     private WeaponData getWeaponData() {
         return data;
     }
 
 }
