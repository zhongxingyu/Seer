 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.dsdev.greencrate;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 /**
  *
  * @author Greenlock28
  */
 public class CrateListener implements Listener {
 
     public FileConfiguration config = null;
     public Random rand = new Random();
     public List<OpenCrateInstance> opencrates = new ArrayList();
 
     public CrateListener(FileConfiguration conf, Server s) {
         config = conf;
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onEntityDeath(EntityDeathEvent event) {
         if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
             for (String mob : config.getConfigurationSection("mobs").getKeys(false)) {
                 if (EntityType.fromName(mob) == event.getEntityType()) {
                     if (config.getString("mobs." + mob + ".percent-mode").equals("Individual")) {
                         for (String drop : config.getConfigurationSection("mobs." + mob + ".drops").getKeys(false)) {
                             if (rand.nextInt(100) < config.getInt("mobs." + mob + ".drops." + drop)) {
                                 event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), GreenCrate.GetCrateItemStack(drop, "null", config));
                             }
                         }
                     } else if (config.getString("mobs." + mob + ".percent-mode").equals("XOR")) {
                         int chanceint = rand.nextInt(100);
                         for (String drop : config.getConfigurationSection("mobs." + mob + ".drops").getKeys(false)) {
                             if (chanceint >= config.getInt("mobs." + mob + ".drops." + drop + ".lower") && chanceint < config.getInt("mobs." + mob + ".drops." + drop + ".upper")) {
                                 event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), GreenCrate.GetCrateItemStack(drop, "null", config));
                                 break;
                             }
                         }
                     }
                 }
             }
         /*for (String cratename : config.getConfigurationSection("crates").getKeys(false)) {
             if (config.getBoolean("crates." + cratename + ".mob-drop.enabled")) {
                 if (EntityType.fromName(config.getString("crates." + cratename + ".mob-drop.mob")) == event.getEntityType()) {
                     if (rand.nextInt(100) < config.getInt("crates." + cratename + ".mob-drop.chance")) {
                         event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), GetCrateItemStack(cratename));
                         break;
                     }
                 }
             }
         }*/
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerUse(PlayerInteractEvent event) {
         ItemStack iteminhand = event.getPlayer().getItemInHand();
         for (String cratename : config.getConfigurationSection("crates").getKeys(false)) {
             if (iteminhand.getTypeId() == config.getInt("crates." + cratename + ".item-id")) {
                 if (iteminhand.getDurability() == config.getInt("crates." + cratename + ".item-data")) {
                    
                     boolean matches = true;
                     
                     if (!config.getString("crates." + cratename + ".display-name").equals("none")) {
                         if (!config.getString("crates." + cratename + ".display-name").contains("{rand}")){
                             if (iteminhand.hasItemMeta() && iteminhand.getItemMeta().hasDisplayName() && iteminhand.getItemMeta().getDisplayName().equals(config.getString("crates." + cratename + ".display-name").replace("&", "§"))) { //.replace(".", "\\.").replace("+", "\\+").replace("*", "\\*") + "$")
                                 //nothing
                             } else {
                                 matches = false;
                             }
                        } else {
                            if (iteminhand.hasItemMeta() && iteminhand.getItemMeta().hasDisplayName() && iteminhand.getItemMeta().getDisplayName().indexOf(config.getString("crates." + cratename + ".display-name").replace("&", "§").split("\\{rand\\}")[0]) == 0) {
                                //nothing
                            } else {
                                matches = false;
                            }
                         }
                     }
                     
                     if (!config.getString("crates." + cratename + ".item-lore").equals("none")){
                        if (iteminhand.hasItemMeta() && iteminhand.getItemMeta().hasLore() && iteminhand.getItemMeta().getLore().get(0).equals(config.getString("crates." + cratename + ".item-lore").replace("&", "§"))) {
                             //nothing
                         } else {
                             matches = false;
                         }
                     }
                     
                     if (matches)
                         GiveCrate(cratename, event.getPlayer(), event);
                     else
                         if (config.getBoolean("crates." + cratename + ".cancel-event"))
                             event.setCancelled(true);
                     
                     /*if (config.getBoolean("crates." + cratename + ".enable-lore-name"))
                     {
                         if (event.getPlayer().getItemInHand().getItemMeta().hasLore() && event.getPlayer().getItemInHand().getItemMeta().getLore().get(0).equals(cratename)) {
                             GiveCrate(cratename, event.getPlayer(), event);
                         }
                     }
                     else
                     {
                         if (event.getPlayer().getItemInHand().getItemMeta().hasDisplayName() && event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals(config.getString("crates." + cratename + ".display-name").replace("&", "§"))) {
                             GiveCrate(cratename, event.getPlayer(), event);
                         }
                     }*/
                 }
             }
         }
     }
 
     @SuppressWarnings({"deprecation"})
     public void GiveCrate(String cratename, Player p, PlayerInteractEvent event) {
        p.getServer().broadcastMessage(cratename);
        
         if (config.getBoolean("crates." + cratename + ".cancel-event")) {
             event.setCancelled(true);
         }
         
         if (config.getBoolean("crates." + cratename + ".bind-to-player")) {
             boolean canuse = true;
             for (String lorenode : event.getPlayer().getItemInHand().getItemMeta().getLore())
                 if (lorenode.contains("§r§8Only for ") && !lorenode.equals("§r§8Only for " + event.getPlayer().getName()))
                     canuse = false;
             if (!canuse) {
                 p.sendMessage("§2[§aGreenCrate§2]§r This crate is locked!");
                 return;
             }
         }
         
         if (config.getBoolean("global.require-crate-perms")) {
             if (!p.hasPermission("greencrate.use." + cratename)) {
                 p.sendMessage("§2[§aGreenCrate§2]§r You do not have permission to open this!");
                 return;
             }
         }
         
         if (config.getBoolean("crates." + cratename + ".cooldown-enabled")) {
             if (!Cooldowns.tryCooldown(p.getName(), cratename, config.getInt("crates." + cratename + ".cooldown-period"))) {
                 p.sendMessage(config.getString("crates." + cratename + ".cooldown-message").replace("&", "§"));
                 return;
             }
         }
 
        ItemStack iinhand = p.getItemInHand();
        
         if (config.getBoolean("crates." + cratename + ".confiscate")) {
             if (p.getItemInHand().getAmount() == 1) {
                 p.getInventory().removeItem(new ItemStack[] { p.getItemInHand() });
             } else {
                 p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
             }
             p.updateInventory();
         }
 
         List<ItemStack> items = GetCrateItems(cratename);
 
         if (config.getBoolean("crates." + cratename + ".gui.enabled")) {
             Inventory inv = event.getPlayer().getServer().createInventory(null, config.getInt("crates." + cratename + ".gui.chest-rows") * 9, config.getString("crates." + cratename + ".gui.label"));
 
             for (int i = 0; i < items.size(); i++) {
                 inv.setItem(i, (ItemStack) items.toArray()[i]);
             }
 
             opencrates.add(new OpenCrateInstance(p.getName(), cratename, inv));
             
             p.openInventory(inv);
         } else {
             for (ItemStack i : items) {
                 p.getInventory().addItem(new ItemStack[] { i });
             }
 
             p.updateInventory();
         }
 
         if (config.getBoolean("crates." + cratename + ".notify-used")) {
            String dname = iinhand.getType().toString().toLowerCase();
            if (iinhand.hasItemMeta() && iinhand.getItemMeta().hasDisplayName()) {
                dname = iinhand.getItemMeta().getDisplayName();
            }
             p.sendMessage(config.getString("crates." + cratename + ".notify-msg")
                    .replace("<displayname>", dname + "&r")
                     .replace("<cratename>", cratename)
                     .replace("&", "§"));
         }
     }
 
     public List<ItemStack> GetCrateItems(String cratename) {
         List<ItemStack> ret = new ArrayList();
 
         for (String itemkey : config.getConfigurationSection("crates." + cratename + ".contents").getKeys(false)) {
             ItemStack item = new ItemStack(Material.POISONOUS_POTATO, 1);
             for (String itemvalue : config.getConfigurationSection("crates." + cratename + ".contents." + itemkey).getKeys(false)) {
                 switch (itemvalue) {
                     case "ID":
                         item.setTypeId(config.getInt("crates." + cratename + ".contents." + itemkey + ".ID"));
                         break;
                     case "DATA":
                         item.setDurability((short) config.getInt("crates." + cratename + ".contents." + itemkey + ".DATA"));
                         break;
                     case "AMOUNT":
                         item.setAmount(config.getInt("crates." + cratename + ".contents." + itemkey + ".AMOUNT"));
                         break;
                     case "ENCHANTMENTS":
                         //if (!config.getStringList("crates." + cratename + ".contents." + itemkey + ".ENCHANTMENTS").get(0).equals("none"))
                         for (String en : config.getStringList("crates." + cratename + ".contents." + itemkey + ".ENCHANTMENTS")) {
                             try {
                                 item.addUnsafeEnchantment(Enchantment.getByName(en.split("-")[0]), Integer.parseInt(en.split("-")[1]));
                             } catch (Exception ex) { }
                         }
                         break;
                     case "DISPLAYNAME": {
                         ItemMeta meta = item.getItemMeta();
                         meta.setDisplayName(config.getString("crates." + cratename + ".contents." + itemkey + ".DISPLAYNAME").replace("&", "§"));
                         item.setItemMeta(meta);
                         break;
                     }
                     case "LORE": {
                         ItemMeta meta = item.getItemMeta();
                         List<String> metalore = new ArrayList();
                         for (String lore : config.getStringList("crates." + cratename + ".contents." + itemkey + ".LORE")) {
                             metalore.add(lore.replace("&", "§"));
                         }
                         meta.setLore(metalore);
                         item.setItemMeta(meta);
                         break;
                     }
                 }
             }
             ret.add(item);
         }
 
         return ret;
     }
 
     public ItemStack GetCrateItemStackOld(String cratename) {
         ItemStack crate = new ItemStack(config.getInt("crates." + cratename + ".item-id"), 1, (short) config.getInt("crates." + cratename + ".item-data"));
         ItemMeta cratemeta = crate.getItemMeta();
 
         if (config.getBoolean("crates." + cratename + ".enable-crate-number")) {
             cratemeta.setDisplayName(config.getString("crates." + cratename + ".display-name").replace("&", "§") + " #" + Integer.toString(rand.nextInt(1000)));
         } else {
             cratemeta.setDisplayName(config.getString("crates." + cratename + ".display-name").replace("&", "§"));
         }
 
         if (config.getBoolean("crates." + cratename + ".enable-lore-name")) {
         ArrayList<String> lore = new ArrayList();
         lore.add(cratename.replace("_", " "));
         cratemeta.setLore(lore);
         }
         
         crate.setItemMeta(cratemeta);
 
         return crate;
     }
     
     @EventHandler(priority = EventPriority.LOW)
     public void onInventoryClick(InventoryClickEvent event) {
         for (OpenCrateInstance oci : opencrates) {
             if (oci.InvHandle.getName().equals(event.getInventory().getName()) && oci.PlayerName.equals(event.getWhoClicked().getName())) {
                 if (config.getBoolean("crates." + oci.CrateName + ".gui.persistent-items")) {
                     if (event.getRawSlot() < (config.getInt("crates." + oci.CrateName + ".gui.chest-rows")*9)) {
                         ItemStack stack = null;
                         if (event.getCurrentItem() != null)
                             stack = event.getCurrentItem();
                         else if (event.getCursor() != null)
                             stack = event.getCursor();
                         
                         if (stack != null) {
                             event.setCurrentItem(stack);
                             event.setCursor(stack);
 
                             event.setCancelled(true);
 
                             Player p = (Player)event.getWhoClicked();
 
                             p.updateInventory();
                         }
                     }
                 }
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onInventoryClose(InventoryCloseEvent event) {
         for (OpenCrateInstance oci : opencrates) {
             if (oci.InvHandle.getName().equals(event.getInventory().getName()) && oci.PlayerName.equals(event.getPlayer().getName())) {
                 Player p = (Player)event.getPlayer();
                 opencrates.remove(oci);
                 break;
             }
         }
     }
 }
