 package me.exphc.EnchantMore;
 
 import java.util.Random;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.UUID;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Formatter;
 import java.lang.Byte;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.*;
 
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.entity.CraftSpider;
 import org.bukkit.craftbukkit.entity.CraftCaveSpider;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.craftbukkit.CraftWorld;
 
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.FurnaceRecipes;
 import net.minecraft.server.ItemDye;
 //import net.minecraft.server.ItemStack;        // import conflict
 
 class EnchantMoreListener implements Listener {
 
     // Better enchantment names more closely matching in-game display
     // TODO: replace with ItemStackX
     final Enchantment PROTECTION = Enchantment.PROTECTION_ENVIRONMENTAL;
     final Enchantment FIRE_PROTECTION = Enchantment.PROTECTION_FIRE;
     final Enchantment FEATHER_FALLING = Enchantment.PROTECTION_FALL;
     final Enchantment BLAST_PROTECTION = Enchantment.PROTECTION_EXPLOSIONS;
     final Enchantment PROJECTILE_PROTECTION = Enchantment.PROTECTION_PROJECTILE;
     final Enchantment RESPIRATION = Enchantment.OXYGEN;
     final Enchantment AQUA_AFFINITY = Enchantment.WATER_WORKER;
     final Enchantment SHARPNESS = Enchantment.DAMAGE_ALL;
     final Enchantment SMITE = Enchantment.DAMAGE_UNDEAD;
     final Enchantment BANE = Enchantment.DAMAGE_ARTHROPODS;
     final Enchantment KNOCKBACK = Enchantment.KNOCKBACK;
     final Enchantment FIRE_ASPECT = Enchantment.FIRE_ASPECT;
     final Enchantment LOOTING = Enchantment.LOOT_BONUS_MOBS;
     final Enchantment EFFICIENCY = Enchantment.DIG_SPEED;
     final Enchantment SILK_TOUCH = Enchantment.SILK_TOUCH;
     final Enchantment UNBREAKING = Enchantment.DURABILITY;
     final Enchantment FORTUNE = Enchantment.LOOT_BONUS_BLOCKS;
     final Enchantment POWER = Enchantment.ARROW_DAMAGE;
     final Enchantment PUNCH = Enchantment.ARROW_KNOCKBACK;
     final Enchantment FLAME = Enchantment.ARROW_FIRE;
     final Enchantment INFINITE = Enchantment.ARROW_INFINITE;
 
     Random random;
    
     EnchantMore plugin;
 
     public EnchantMoreListener(EnchantMore pl) {
         plugin = pl;
 
         random = new Random();
 
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block block = event.getClickedBlock();
         ItemStack item = event.getItem();
         Action action = event.getAction();
         Player player = event.getPlayer();
 
         if (item == null) {
             return;
         }
 
         if (block == null) {
             // TODO: we might need to handle non-block (air) events
             return;
         }
 
         World world = block.getWorld();
 
         if (item.getType() == Material.FLINT_AND_STEEL && action == Action.RIGHT_CLICK_BLOCK) {
         
             // Flint & Steel + Smite = strike lightning
             if (item.containsEnchantment(SMITE)) {
                 world.strikeLightning(block.getLocation());
             }
 
             // Flint & Steel + Fire Protection = player fire resistance
             if (item.containsEnchantment(FIRE_PROTECTION)) {
                 ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     12, // fireResistance - http://wiki.vg/Protocol#Effects
                     20*10*item.getEnchantmentLevel(FIRE_PROTECTION), // length
                     1)); // amplifier
             }
 
             // Flint & Steel + Aqua Affinity = vaporize water
             if (item.containsEnchantment(AQUA_AFFINITY)) {
                 // Can't actually click on water, the click "goes through" as if it was air
                 // Not like buckets filled or lily pads placements
                 /* 
                 if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
                     block.setType(Material.AIR);
                     plugin.log.info("water");
                 }
                 */
 
                 // Find water within ignited cube area
                 int r = item.getEnchantmentLevel(AQUA_AFFINITY);
 
                 Location loc = block.getLocation();
                 int x0 = loc.getBlockX();
                 int y0 = loc.getBlockY();
                 int z0 = loc.getBlockZ();
                
                 for (int dx = -r; dx <= r; dx += 1) {
                     for (int dy = -r; dy <= r; dy += 1) {
                         for (int dz = -r; dz <= r; dz += 1) {
                             Block b = world.getBlockAt(dx+x0, dy+y0, dz+z0);
                            
                             if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
                                 b.setType(Material.AIR);
                                 world.playEffect(b.getLocation(), Effect.SMOKE, 0); // TODO: direction
                             }
                         }
                     }
                 }
             }
 
             // Flint & Steel + Sharpness = firey explosion
             if (item.containsEnchantment(SHARPNESS)) {
                 float power = item.getEnchantmentLevel(SHARPNESS) * 1.0f;
 
                 world.createExplosion(block.getLocation(), power, true);
             }
 
             // Flint & Steel + Efficiency = burn faster (turn wood to grass)
             if (item.containsEnchantment(EFFICIENCY)) {
                 if (isWoodenBlock(block.getType(), block.getData())) {
                     block.setType(Material.LEAVES);
                     // TODO: data? just leaving as before, but type may be unexpected
                 }
             }
 
         } else if (isHoe(item.getType())) {
             // Hoe + Aqua Affinity = hydrate below
             // TODO: maybe should add water on side of, if air? better for farming
             if (item.containsEnchantment(AQUA_AFFINITY)) {
                 Block below = block.getRelative(BlockFace.DOWN, item.getEnchantmentLevel(AQUA_AFFINITY));
                 
                 if (below.getType() == Material.DIRT) {
                     below.setType(Material.STATIONARY_WATER);
                 }
             }
 
             // Hoe + Fortune = chance to drop seeds
             if (item.containsEnchantment(FORTUNE) && action == Action.RIGHT_CLICK_BLOCK) {
                 if (block.getType() == Material.DIRT || block.getType() == Material.GRASS) {
                     if (random.nextInt(2) != 0) {   // TODO: configurable, and depend on level
                         Material seedType;
 
                         // TODO: configurable probabilities
                         switch (random.nextInt(4)) {
                         case 2: seedType = Material.MELON_SEEDS; break;
                         case 3: seedType = Material.PUMPKIN_SEEDS; break;
                         default: seedType = Material.SEEDS; // wheat, 50%
                         }
 
                         // TODO: configurable and random quantity
                       
                         ItemStack drop = new ItemStack(seedType, 1);
 
                         world.dropItemNaturally(block.getRelative(BlockFace.UP).getLocation(), drop);
                     }
                 }
             }
 
             // Hoe + Efficiency = hoe larger area
             if (item.containsEnchantment(EFFICIENCY)) { // also can use left-click, for efficiency!
                 int r = item.getEnchantmentLevel(EFFICIENCY);
 
                 Location loc = block.getLocation();
                 int x0 = loc.getBlockX();
                 int y0 = loc.getBlockY();
                 int z0 = loc.getBlockZ();
                
                 for (int dx = -r; dx <= r; dx += 1) {
                     for (int dz = -r; dz <= r; dz += 1) {
                         Block b = world.getBlockAt(dx+x0, y0, dz+z0);
                        
                         if (b.getType() == Material.DIRT || b.getType() == Material.GRASS) {
                             b.setType(Material.SOIL);
                         }
                     }
                 }
                
             }
 
             // Hoe + Respiration = grow
             if (item.containsEnchantment(RESPIRATION) && action == Action.LEFT_CLICK_BLOCK) {
                 growStructure(block.getLocation(), player);
                 // TODO: use durability
             }
         } else if (isPickaxe(item.getType())) {
             // Pickaxe + Power = instant break anything (including bedrock)
             if (item.containsEnchantment(POWER)) {
                 // Note: this also works for bedrock!
                 block.breakNaturally(item);
             }
         }
     }
 
     // Return whether is a wooden block
     private boolean isWoodenBlock(Material m, byte data) {
         return m == Material.WOOD || 
             m == Material.WOOD_PLATE || 
             m == Material.WOOD_STAIRS ||
             m == Material.WOODEN_DOOR || 
             m == Material.LOG ||
             (m == Material.STEP && data == 2) ||      // wooden slab
             (m == Material.DOUBLE_STEP && data == 2);// wooden double slab
     }
 
     // Attempt to grow organic structure
     private void growStructure(Location loc, Player player) {
         int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
         World world = loc.getWorld();
 
         net.minecraft.server.ItemDye bonemealDye = new net.minecraft.server.ItemDye(15);
         CraftItemStack bonemealStack = (new CraftItemStack(Material.INK_SACK, 1, (short)15));
 
 
         bonemealDye.a(bonemealStack.getHandle(), ((CraftPlayer)player).getHandle(), ((CraftWorld)world).getHandle(), x, y, z, 0/*unused*/);
     }
 
     private boolean isHoe(Material m) {
         return m == Material.DIAMOND_HOE ||
             m == Material.GOLD_HOE || 
             m == Material.IRON_HOE ||
             m == Material.STONE_HOE ||
             m == Material.WOOD_HOE;
     }
 
     private boolean isSword(Material m) {
         return m == Material.DIAMOND_SWORD ||   
             m == Material.GOLD_SWORD ||
             m == Material.IRON_SWORD ||
             m == Material.STONE_SWORD ||
             m == Material.WOOD_SWORD;
     }
 
     private boolean isPickaxe(Material m) {
         return m == Material.DIAMOND_PICKAXE ||
             m == Material.GOLD_PICKAXE ||
             m == Material.IRON_PICKAXE ||
             m == Material.STONE_PICKAXE ||
             m == Material.WOOD_PICKAXE;
     }
 
     private boolean isShovel(Material m) {
         return m == Material.DIAMOND_SPADE ||
             m == Material.GOLD_SPADE ||
             m == Material.IRON_SPADE ||
             m == Material.STONE_SPADE ||
             m == Material.WOOD_SPADE;
     }
 
     private boolean isAxe(Material m) {
         return m == Material.DIAMOND_AXE ||
             m == Material.GOLD_AXE ||
             m == Material.IRON_AXE ||
             m == Material.STONE_AXE ||
             m == Material.WOOD_AXE;
     }
 
 
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Entity entity = event.getRightClicked();
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
 
         if (item.getType() == Material.FLINT_AND_STEEL) {
             if (entity == null) {
                 return;
             }
 
             // Flint & Steel + Fire Aspect = set mobs on fire
             if (item.containsEnchantment(FIRE_ASPECT)) {
                 entity.setFireTicks(getFireTicks(item.getEnchantmentLevel(FIRE_ASPECT)));
 
                 // TODO: fix
                 item.setDurability((short)(item.getDurability() - 1));
             }
 
             // Flint & Steel + Respiration = smoke inhalation (confusion)
             if (item.containsEnchantment(RESPIRATION)) {
                 World world = entity.getWorld();
 
                 world.playEffect(entity.getLocation(), Effect.SMOKE, 0);    // TOOD: smoke direction
                 world.playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);    // TOOD: smoke direction
 
                 // Confusion effect on players
                 if (entity instanceof CraftPlayer) {
                     ((CraftPlayer)entity).getHandle().addEffect(new net.minecraft.server.MobEffect(
                         9,      // confusion  - http://wiki.vg/Protocol#Effects
                         20*10*item.getEnchantmentLevel(RESPIRATION),  // length
                         1));    // amplifier
                 }
             }
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Smite = gouge eyes (blindness)
             if (item.containsEnchantment(SMITE)) {
                 if (entity instanceof CraftPlayer) {
                     ((CraftPlayer)entity).getHandle().addEffect(new net.minecraft.server.MobEffect(
                         15,     // blindness
                         20*10*item.getEnchantmentLevel(SMITE),  // length
                         1));    // amplifier
                 }
                 // TODO: use durability
             }
 
             // Shears + Bane of Arthropods = collect spider eyes
             if (item.containsEnchantment(BANE)) {
                 if (entity instanceof CaveSpider || entity instanceof Spider) {
                     Creature bug = (Creature)entity;
 
                     // If at least 50% health, cut out eyes, then drop health
                     if (bug.getHealth() >= bug.getMaxHealth() / 2) {
                         World world = player.getWorld();
 
                         world.dropItemNaturally(bug.getEyeLocation(), new ItemStack(Material.SPIDER_EYE, 1));
 
                         bug.setHealth(bug.getMaxHealth() / 2 - 1);
                     }
                 }
                 // TODO: use durability
             }
         }  else if (isSword(item.getType())) {
             /*
             // BLOCKED: Sword + Flame = night vision when blocking 
             // The visual effect plays (navy blue swirly particles), but doesn't actually do anything as of Minecraft 1.1
             if (item.containsEnchantment(FLAME)) {
                 ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     16,     // nightVision
                     20*10*item.getEnchantmentLevel(FLAME),  // length
                     10));    // amplifier
             }
 
             // BLOCKED: Sword + Infinity = invisibility when blocking 
             // Also has no implemented effect in Minecraft 1.1. Maybe a plugin could use?
             if (item.containsEnchantment(INFINITE)) {
                 ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     14,     // invisibility
                     20*2*item.getEnchantmentLevel(INFINITE),  // length
                     10));    // amplifier
 
             }
             */
 
 
             // Sword + Protection = resistance when blocking 
             if (item.containsEnchantment(PROTECTION)) {
                  ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     11,     // resistance
                     20*10*item.getEnchantmentLevel(PROTECTION),  // length
                     10));    // amplifier
             }
         }
     }
 
 
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerFish(PlayerFishEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
 
         PlayerFishEvent.State state = event.getState();
         World world = player.getWorld();
 
         if (state == PlayerFishEvent.State.CAUGHT_ENTITY) {
             Entity entity = event.getCaught();
 
             if (entity == null) {
                 return;
             }
 
             // Fishing Rod + Fire Aspect = set mobs on fire
             if (item.containsEnchantment(FIRE_ASPECT)) {
                 entity.setFireTicks(getFireTicks(item.getEnchantmentLevel(FIRE_ASPECT)));
 
                 // TODO: fix
                 item.setDurability((short)(item.getDurability() - 1));
             }
             
             // Fishing Rod + Smite = strike mobs with lightning
             if (item.containsEnchantment(SMITE)) {
                 world.strikeLightning(entity.getLocation());
             }
         } else if (state == PlayerFishEvent.State.CAUGHT_FISH) {
             // Fishing Rod + Flame = catch cooked fish
             if (item.containsEnchantment(FLAME)) {
                 event.setCancelled(true);
 
                 world.dropItemNaturally(player.getLocation(), new ItemStack(Material.COOKED_FISH, 1));
             }
         } else if (state == PlayerFishEvent.State.FAILED_ATTEMPT) {
             // TODO
 
             player.sendMessage("fail");
             if (item.containsEnchantment(SILK_TOUCH)) {
                 // TODO: always (or more reliably) catch fish
             }
         }
     }
 
     // Get time to burn entity for given enchantment level
     private int getFireTicks(int level) {
          // TODO: configurable ticks per level
         return 20 * 10 * level;
     }
 
     // Break all contiguous blocks of the same type
     private int breakContiguous(Block start, ItemStack tool, int limit) {
         Set<Block> result = new HashSet<Block>();
 
         plugin.log.info("collectContiguous starting");
         collectContiguous(start, limit, result);
         plugin.log.info("collectContiguous returned with "+result.size());
 
         for (Block block: result) {
             // TODO: accumulate same type to optimize drops?
             //drops.addAll(block.getDrops(tool));
 
             //block.setType(Material.AIR);
             block.breakNaturally(tool);  // no, infinite recurse
             //plugin.log.info("break"+block);
         }
 
         return result.size();
     }
 
     // Recursively find all contiguous blocks 
     // TODO: faster?
     private void collectContiguous(Block start, int limit, Set<Block> result) {
         if (limit < 0) {
             return;
         }
 
         result.add(start);
 
         for (int dx = -1; dx <= 1; dx += 1) {
             for (int dy = -1; dy <= 1; dy += 1) {
                 for (int dz = -1; dz <= 1; dz += 1) {
                     if (dx == 0 && dy == 0 && dz == 0) {
                         continue;
                     }
                     Block other = start.getRelative(dx, dy, dz);
 
                     limit -= 1;
                     if (limit < 0) {
                         return;
                     }
 
                     // Follow same type _and_ data (different leaves, etc.)
                     if (other.getType() == start.getType() && other.getData() == start.getData()) {
                         collectContiguous(other, limit - 1, result);
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL) 
     public void onBlockBreak(BlockBreakEvent event) {
         Player player = event.getPlayer();
         Block block = event.getBlock();
         ItemStack item = player.getItemInHand();
         World world = player.getWorld();
 
         if (item == null) {
             return;
         }
 
         if (isPickaxe(item.getType()) ||
             isShovel(item.getType()) ||
             isAxe(item.getType())) {
 
             // Pickaxe/shovel/axe + Flame = auto-smelt
             if (item.containsEnchantment(FLAME)) {
                 Collection<ItemStack> rawDrops = block.getDrops(item);
 
                 for (ItemStack rawDrop: rawDrops) {
                     ItemStack smeltedDrop = smelt(rawDrop);
 
                     world.dropItemNaturally(block.getLocation(), smeltedDrop);
                 }
 
 
                 block.setType(Material.AIR);
             }
 
             // Axe + Power = fell tree
             if (isAxe(item.getType()) && item.containsEnchantment(POWER) && block.getType() == Material.LOG) {
                 // Chop tree
                 breakContiguous(block, item, 100 * item.getEnchantmentLevel(POWER));
             }
 
             // Shovel + Power = excavation
             if (isShovel(item.getType()) && item.containsEnchantment(POWER) && 
                 (block.getType() == Material.DIRT ||
                 block.getType() == Material.GRASS ||
                 block.getType() == Material.GRAVEL)) { 
 
                 // Clear out those annoying veins of gravel (or dirt)
 
                 // too slow
                 //breakContiguous(block, item, 100 * item.getEnchantmentLevel(POWER));
 
                 // Dig a cube out, but no drops
                 int r = item.getEnchantmentLevel(POWER);
 
                 Location loc = block.getLocation();
                 int x0 = loc.getBlockX();
                 int y0 = loc.getBlockY();
                 int z0 = loc.getBlockZ();
                
                 for (int dx = -r; dx <= r; dx += 1) {
                     for (int dy = -r; dy <= r; dy += 1) {
                         for (int dz = -r; dz <= r; dz += 1) {
                             int x = dx + x0, y = dy + y0, z = dz + z0;
 
                             int type = world.getBlockTypeIdAt(x, y, z);
                             if (type == Material.DIRT.getId() ||
                                 type == Material.GRASS.getId() ||
                                 type == Material.GRAVEL.getId()) {
 
                                 Block b = world.getBlockAt(x, y, z);
                                 b.setType(Material.AIR);
                             }
                         }
                     }
                 }
 
             }
 
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Silk Touch = collect cobweb, dead bush
             if (item.containsEnchantment(SILK_TOUCH)) {
                 if (block.getType() == Material.DEAD_BUSH ||
                     block.getType() == Material.WEB) {
 
                     world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
 
                     block.setType(Material.AIR);
                 } 
                 // TODO: cut grass, turn into dirt (no drop)
                 /*
                 else if (block.getType() == Material.GRASS) {
                     block.setType(Material.DIRT);
                     event.setCancelled(true);
                 }
                 */
             }
 
             // Shears + Fortune = apples from leaves
             if (item.containsEnchantment(FORTUNE)) {
                 if (block.getType() == Material.LEAVES) {
                     Material dropType;
 
                     // TODO: different probabilities, depending on level too (higher, more golden)
                     switch (random.nextInt(10)) {
                     case 0: dropType = Material.GOLDEN_APPLE; break;
                     default: dropType = Material.APPLE;
                     }
 
                     world.dropItemNaturally(block.getLocation(), new ItemStack(dropType, 1));
                     
                     block.setType(Material.AIR);
                 }
             }
 
             // Shears + Power = hedge trimmer
             if (item.containsEnchantment(POWER) && block.getType() == Material.LEAVES) {
                 breakContiguous(block, item, 50 * item.getEnchantmentLevel(POWER));
             }
 
         } else if (isHoe(item.getType())) {
            // Hoe + Silk Touch = collect farmland, crop block, pumpkin/melon stem, cake block, sugarcane block (preserving data)
             if (item.containsEnchantment(SILK_TOUCH)) {
                 // Collect farm-related blocks, preserving the growth/wetness/eaten data
                 if (isFarmBlock(block.getType())) {
                     ItemStack drop = new ItemStack(block.getType(), 1);
 
                     // Store block data value
                     //drop.setDurability(block.getData());      // bukkit doesn't preserve
                     drop.addUnsafeEnchantment(SILK_TOUCH, block.getData());
 
 
                     world.dropItemNaturally(block.getLocation(), drop);
                     
                     block.setType(Material.AIR);
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onBlockPlace(BlockPlaceEvent event) {
         Block block = event.getBlockPlaced();
         Player player = event.getPlayer();
 
         // Item to place as a block
         // NOT event.getItemInHand(), see https://bukkit.atlassian.net/browse/BUKKIT-596 BlockPlaceEvent getItemInHand() loses enchantments
         ItemStack item = player.getItemInHand();
 
         // Set data of farm-related block
         if (item != null && item.containsEnchantment(SILK_TOUCH)) {
             if (isFarmBlock(item.getType())) {
                 block.setData((byte)item.getEnchantmentLevel(SILK_TOUCH));
             }
         }
 
     }
 
     // Get whether material is a farm-related block, either land or growing crops
     private boolean isFarmBlock(Material m) {
         return m == Material.SOIL ||     // Farmland
             m == Material.CROPS ||    // wheat TODO: update wiki, calls 'Wheat Seeds' though in-game 'Crops'
             m == Material.SUGAR_CANE_BLOCK ||
             m == Material.CAKE_BLOCK ||
             m == Material.PUMPKIN_STEM ||
             m == Material.MELON_STEM ||
            m == Material.NETHER_STALK; // TODO: test
     }
 
     // Get item as if it was smelted
     private ItemStack smelt(ItemStack raw) {
         net.minecraft.server.ItemStack smeltNMS = net.minecraft.server.FurnaceRecipes.getInstance().a(raw.getTypeId());
 
         ItemStack smelted = (ItemStack)(new CraftItemStack(smeltNMS));
     
         return smelted;
     }
 
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerShearEntity(PlayerShearEntityEvent event) {
         Player player = event.getPlayer();
         Entity entity = event.getEntity();
         ItemStack tool = player.getItemInHand();
         World world = player.getWorld();
 
         if (tool == null) {
             return;
         }
 
         if (!(entity instanceof Sheep)) {
             return;
         }
         // TODO: mooshroom?
 
         // Shears + Looting = more wool, random colors
         if (tool.getType() == Material.SHEARS && tool.containsEnchantment(LOOTING)) {
             Location loc = entity.getLocation();
 
             int quantity = random.nextInt(tool.getEnchantmentLevel(LOOTING) * 2);
             for (int i = 0; i < quantity; i += 1) {
                 short color = (short)random.nextInt(16);
 
                 world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.WOOL, 1, color));
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onProjectileHit(ProjectileHitEvent event) {
         Entity entity = event.getEntity();
 
         if (!(entity instanceof Arrow)) {
             return;
         }
 
         Arrow arrow = (Arrow)entity;
         LivingEntity shooter = arrow.getShooter();
         
         if (shooter == null || !(shooter instanceof Player)) {
             // shot from dispenser, skeleton, etc.
             return;
         }
 
         Player player = (Player)shooter;
         ItemStack item = player.getItemInHand();
 
         if (item == null || item.getType() != Material.BOW) {
             return;
         }
 
         Location dest = arrow.getLocation();
         World world = dest.getWorld();
 
         // Bow + Looting = steal 
         if (item.containsEnchantment(LOOTING)) {
             double s = 5.0 * item.getEnchantmentLevel(LOOTING);
 
             List<Entity> loots = arrow.getNearbyEntities(s, s, s);
             for (Entity loot: loots) {
                 // TODO: different levels, for only items, exp, mobs?
                 // This moves everything!
                 loot.teleport(player.getLocation());
             }
         }
 
         // Bow + Smite = lightning
         if (item.containsEnchantment(SMITE)) {
             world.strikeLightning(dest);
         }
 
         // Bow + Fire Aspect = firey explosions
         if (item.containsEnchantment(FIRE_ASPECT)) {
             float power = 1.0f * item.getEnchantmentLevel(FIRE_ASPECT);
 
             world.createExplosion(dest, power, true);
         }
 
         // Bow + Feather Falling = teleport
         if (item.containsEnchantment(FEATHER_FALLING)) {
             // use up the arrow (TODO: not at higher levels?) or set no pickup?
             arrow.remove();
 
             player.teleport(dest);
         }
     }
 }
 
 public class EnchantMore extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
     EnchantMoreListener listener;
 
     public void onEnable() {
         listener = new EnchantMoreListener(this);
     }
     
     public void onDisable() {
     }
 }
