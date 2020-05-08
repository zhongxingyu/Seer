 package com.norcode.bukkit.autocrafter;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Dropper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.util.HashSet;
 
 public class Autocrafter extends JavaPlugin implements Listener {
     private static EnumSet<BlockFace> sides = EnumSet.of(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH);
     private static final int itemFrameEntityId = EntityType.ITEM_FRAME.getTypeId();
     private boolean worldWhitelist = true;
     private boolean recipeWhitelist = false;
    private static EnumSet<Material> filledBuckets = EnumSet.of(Material.MILK_BUCKET, Material.LAVA_BUCKET, Material.WATER_BUCKET);
     EnumSet<Material> recipeList = EnumSet.noneOf(Material.class);
     private String noPermissionMsg;
     private List<String> worldList = new ArrayList<String>();
 
     private Permission wildcardPerm = null;
     @Override
     public void onEnable() {
         saveDefaultConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     public void loadConfig() {
         noPermissionMsg = getConfig().getString("messages.no-permission", null);
         // world list
         String listtype = getConfig().getString("world-selection", "whitelist").toLowerCase();
         if (listtype.equals("blacklist")) {
             this.worldWhitelist = false;
         } else {
             this.worldWhitelist = true;
         }
         this.worldList.clear();
         for (String wn: getConfig().getStringList("world-list")) {
             this.worldList.add(wn.toLowerCase());
         }
         // recipe list
         listtype = getConfig().getString("recipe-selection", "blacklist").toLowerCase();
         if (listtype.equals("whitelist")) {
             this.recipeWhitelist = true;
         } else {
             this.recipeWhitelist = false;
         }
         this.recipeList.clear();
         for (String r: getConfig().getStringList("recipe-list")) {
             this.recipeList.add(Material.valueOf(r));
         }
         wildcardPerm = getServer().getPluginManager().getPermission("autocrafter.create.*");
         if (wildcardPerm == null) {
             wildcardPerm = new Permission("autocrafter.create.*", PermissionDefault.FALSE);
             getServer().getPluginManager().addPermission(wildcardPerm);
         }
         Permission child = null;
         for (Material m: Material.values()) {
             if (recipeAllowed(new ItemStack(m))) {
                 getLogger().info("Creating node: " + m);
                 child = getServer().getPluginManager().getPermission("autocrafter.create." + m.name().toLowerCase());
                 if (child == null) {
                     child = new Permission("autocrafter.create." + m.name().toLowerCase(), PermissionDefault.FALSE);
                     getServer().getPluginManager().addPermission(child);
                 }
                 child.addParent(wildcardPerm, true);
                 child.recalculatePermissibles();
             }
         }
         wildcardPerm.recalculatePermissibles();
     }
 
     public void printInventoryContents(Inventory inv) {
         String s = "";
         for (int i=0;i<inv.getSize();i++) {
             ItemStack is = inv.getItem(i);
             if (is == null) {
                 s += "null, ";
             } else {
                 s += is + ", ";
             }
         }
         getLogger().info(s);
     }
 
 
     public boolean enabledInWorld(World w) {
         boolean enabled = ((worldWhitelist && worldList.contains(w.getName().toLowerCase())) || 
                 (!worldWhitelist && !worldList.contains(w.getName().toLowerCase())));
         return enabled;
     }
 
     public boolean recipeAllowed(ItemStack stack) {
         boolean inList = recipeList.contains(stack.getType());
         return ((recipeWhitelist && inList) || (!recipeWhitelist && !inList));
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onFrameInteract(PlayerInteractEntityEvent event) {
         if (!enabledInWorld(event.getPlayer().getWorld())) {
             return;
         }
         if (event.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
             ItemFrame frame = (ItemFrame) event.getRightClicked();
             Block attached = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
             if (attached.getType().equals(Material.DROPPER)) {
                 if (frame.getItem() == null || frame.getItem().getType().equals(Material.AIR)) {
                     ItemStack hand = event.getPlayer().getItemInHand();
                     if (!hasCraftPermission(event.getPlayer(), hand)) {
                         event.setCancelled(true);
                         if (noPermissionMsg != null) {
                             event.getPlayer().sendMessage(noPermissionMsg);
                         }
                     }
                 }
             }
         }
     }
 
     private boolean hasCraftPermission(Player player, ItemStack hand) {
         String node ="autocrafter.create." + hand.getType().name().toLowerCase(); 
         boolean b = player.hasPermission(node);
         return b;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onDropperMove(InventoryMoveItemEvent event) {
         if (event.getSource().getHolder() instanceof Dropper) {
             final Dropper dropper = ((Dropper) event.getSource().getHolder());
             ItemFrame frame = getAttachedFrame(dropper.getBlock());
             if (!enabledInWorld(dropper.getWorld())) return;
             if (!recipeAllowed(frame.getItem())) return;
             ItemStack result = null;
             Recipe recipe = null;
             if (frame != null && frame.getItem() != null && !frame.getItem().getType().equals(Material.AIR)) {
                 if (event.getItem().equals(frame.getItem())) {
                     return;
                 }
                 boolean crafted = false;
                 ItemStack[] ingredients = null;
                 for (Recipe r: getServer().getRecipesFor(frame.getItem())) {
                     if (r instanceof ShapedRecipe || r instanceof ShapelessRecipe) {
                         ingredients = CraftAttempt.getIngredients(r).toArray(new ItemStack[0]);
                         Inventory clone = CraftAttempt.cloneInventory(this, dropper.getInventory());
                         //clone.addItem(event.getItem());
                         crafted = true;
                         for (ItemStack ing: ingredients) {
                             if (!CraftAttempt.removeItem(clone, ing.getType(), ing.getData().getData(), ing.getAmount())) {
                                 crafted = false;
                                 break;
                             }
                         }
                         if (crafted) {
                             recipe = r;
                             result = r.getResult().clone();
                             break;
                         }
                     }
                 }
                 if (!crafted) {
                     event.setCancelled(true);
                 } else {
                     event.setItem(result);
                     final Recipe finalRecipe = recipe;
                     getServer().getScheduler().runTaskLater(this,  new Runnable() {
                         public void run () {
                             for (ItemStack ing: CraftAttempt.getIngredients(finalRecipe)) {
                                 CraftAttempt.removeItem(dropper.getInventory(), ing.getType(), ing.getData().getData(), ing.getAmount());
                                if (filledBuckets.contains(ing.getType())) {
                                    dropper.getInventory().addItem(new ItemStack(Material.BUCKET));
                                }
                             }
                         }
                     }, 0);
                 }
             }
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onDropperFire(BlockDispenseEvent event) {
         if (event.getBlock().getType().equals(Material.DROPPER)) {
             final Dropper dropper = ((Dropper) event.getBlock().getState());
             ItemFrame frame = getAttachedFrame(event.getBlock());
             if (!enabledInWorld(dropper.getWorld())) return;
             if (!recipeAllowed(frame.getItem())) return;
             ItemStack result = null;
             Recipe recipe = null;
             if (frame != null && frame.getItem() != null && !frame.getItem().getType().equals(Material.AIR)) {
                 if (event.getItem().equals(frame.getItem())) {
                     return;
                 }
                 boolean crafted = false;
                 ItemStack[] ingredients = null;
                 for (Recipe r: getServer().getRecipesFor(frame.getItem())) {
                     if (r instanceof ShapedRecipe || r instanceof ShapelessRecipe) {
                         ingredients = CraftAttempt.getIngredients(r).toArray(new ItemStack[0]);
                         Inventory clone = CraftAttempt.cloneInventory(this, dropper.getInventory());
                         clone.addItem(event.getItem());
                         crafted = true;
                         for (ItemStack ing: ingredients) {
                             if (!CraftAttempt.removeItem(clone, ing.getType(), ing.getData().getData(), ing.getAmount())) {
                                 crafted = false;
                                 break;
                             }
                         }
                         if (crafted) {
                             recipe = r;
                             result = r.getResult().clone();
                             break;
                         }
                     }
                 }
                 if (!crafted) {
                     event.setCancelled(true);
                 } else {
                     event.setItem(result);
                     final Recipe finalRecipe = recipe;
                     getServer().getScheduler().runTaskLater(this,  new Runnable() {
                         public void run () {
                             for (ItemStack ing: CraftAttempt.getIngredients(finalRecipe)) {
                                 CraftAttempt.removeItem(dropper.getInventory(), ing.getType(), ing.getData().getData(), ing.getAmount());
                                if (filledBuckets.contains(ing.getType())) {
                                    dropper.getInventory().addItem(new ItemStack(Material.BUCKET));
                                }
                             }
                         }
                     }, 0);
                 }
             }
         }
     }
 
     public ItemFrame getAttachedFrame(Block block) {
         Location dropperLoc = new Location(block.getWorld(), block.getX()+0.5, block.getY()+0.5, block.getZ()+0.5);
         Chunk thisChunk = block.getChunk();
         Chunk thatChunk = null;
         HashSet<Chunk> chunksToCheck = new HashSet<Chunk>();
         chunksToCheck.add(thisChunk);
         for (BlockFace side: sides) {
             thatChunk = block.getRelative(side).getChunk();
             if (!thisChunk.equals(thatChunk)) {
                 chunksToCheck.add(thatChunk);
             }
         }
         for (Chunk c: chunksToCheck) {
             for (Entity e: c.getEntities()) {
                 if (e.getType().getTypeId() == itemFrameEntityId  && e.getLocation().distanceSquared(dropperLoc) == 0.31640625) {
                     return ((ItemFrame) e);
                 }
             }
         }
         return null;
     }
 }
