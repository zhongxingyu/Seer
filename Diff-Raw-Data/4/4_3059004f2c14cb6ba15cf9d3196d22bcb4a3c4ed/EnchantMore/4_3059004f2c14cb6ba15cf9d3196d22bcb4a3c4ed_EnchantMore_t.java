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
 import net.minecraft.server.MobEffectList;
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
 
         // Shears + Power = cut grass (secondary effect)
         if (item.getType() == Material.SHEARS) {
             if (item.containsEnchantment(POWER)) {
                 if (block.getType() == Material.GRASS) {
                     block.setType(Material.DIRT);
                 }
                 damage(item);
             }
 
         } else if (item.getType() == Material.FLINT_AND_STEEL && action == Action.RIGHT_CLICK_BLOCK) {
         
             // Flint & Steel + Smite = strike lightning
             if (item.containsEnchantment(SMITE)) {
                 world.strikeLightning(block.getLocation());
                 damage(item, 9);
             }
 
             // Flint & Steel + Fire Protection = player fire resistance
             if (item.containsEnchantment(FIRE_PROTECTION)) {
                 ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     12, // fireResistance - http://wiki.vg/Protocol#Effects
                     20*10*item.getEnchantmentLevel(FIRE_PROTECTION), // length
                     1)); // amplifier
                 // no extra damage
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
                 // no extra damage
             }
 
             // Flint & Steel + Sharpness = firey explosion
             if (item.containsEnchantment(SHARPNESS)) {
                 float power = (item.getEnchantmentLevel(SHARPNESS) - 1) * 1.0f;
 
                 world.createExplosion(block.getLocation(), power, true);
 
                 damage(item);
             }
 
             // Flint & Steel + Efficiency = burn faster (turn wood to grass)
             if (item.containsEnchantment(EFFICIENCY)) {
                 if (isWoodenBlock(block.getType(), block.getData())) {
                     block.setType(Material.LEAVES);
                     // TODO: data? just leaving as before, but type may be unexpected
                 }
                 // no extra damage
             }
 
         } else if (isHoe(item.getType())) {
             // Hoe + Aqua Affinity = auto-hydrate
             if (item.containsEnchantment(AQUA_AFFINITY)) {
                 // As long as not in hell, hydrate nearby
                 if (world.getEnvironment() != World.Environment.NETHER) {
                     int n = item.getEnchantmentLevel(AQUA_AFFINITY);
 
                     // Change adjacent air blocks to water
                     for (int dx = -1; dx <= 1; dx += 1) {
                         for (int dz = -1; dz <= 1; dz += 1) {
                             Block near = block.getRelative(dx * n, 0, dz * n);
                             if (near.getType() == Material.AIR) {
                                 near.setType(Material.STATIONARY_WATER);
                             }
                         }
                     }
                 } else {
                     world.playEffect(block.getLocation(), Effect.SMOKE, 0); // TODO: direction
                 }
 
                 // If soil, moisten thoroughly
                 // This works in The Nether, though it does not add water and will dry out eventually
                 if (block.getType() == Material.SOIL) {
                     block.setData((byte)8);   
                 }
 
                 damage(item);
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
                     // no extra damage
                 }
             }
 
             // Hoe + Efficiency = till larger area
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
                 damage(item);
             }
 
             // Hoe + Respiration = grow
             if (item.containsEnchantment(RESPIRATION) && action == Action.LEFT_CLICK_BLOCK) {
                 growStructure(block.getLocation(), player);
                 damage(item);
             }
         } else if (isPickaxe(item.getType())) {
             // Pickaxe + Power = instantly break anything (including bedrock)
             if (item.containsEnchantment(POWER)) {
                 // Note: this also works for bedrock!
                 block.breakNaturally(item);
             }
 
             damage(item);
         }
     }
 
    
     // Use up a tool
     public static void damage(ItemStack tool) {
         damage(tool, 1);
     }
 
     public static void damage(ItemStack tool, int amount) {
         tool.setDurability((short)(tool.getDurability() + amount));
         // TODO: if reaches max, break? set to air or not?
     }
 
 
     // Attempt to grow organic structure
     private void growStructure(Location loc, Player player) {
         int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
         World world = loc.getWorld();
 
         // Use bonemeal (white dye/ink) to grow
         CraftItemStack bonemealStack = (new CraftItemStack(Material.INK_SACK, 1, (short)15));
 
         // 'a' unobfuscated = onItemUse
         net.minecraft.server.Item.INK_SACK.a(bonemealStack.getHandle(), ((CraftPlayer)player).getHandle(), ((CraftWorld)world).getHandle(), x, y, z, 0/*unused*/);
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
 
     // Get whether material is a farm-related block, either land or growing crops
     private boolean isFarmBlock(Material m) {
         return m == Material.SOIL ||     // Farmland
             m == Material.CROPS ||    // wheat TODO: update wiki, calls 'Wheat Seeds' though in-game 'Crops'
             m == Material.SUGAR_CANE_BLOCK ||
             m == Material.CAKE_BLOCK ||
             m == Material.PUMPKIN_STEM ||
             m == Material.MELON_STEM ||
             m == Material.NETHER_WARTS; // not the item, that is NETHER_STALK (confusingly)
     }
 
     // Get whether able to be excavated by shovel
     private boolean isExcavatable(int m) {
         return m == Material.DIRT.getId() ||
             m == Material.GRASS.getId() ||
             m == Material.GRAVEL.getId() ||
             m == Material.SOUL_SAND.getId() ||
             m == Material.NETHERRACK.getId(); // not normally diggable, but why not?
     }
 
     private boolean isExcavatable(Material m) {
         return isExcavatable(m.getId());
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
 
 
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Entity entity = event.getRightClicked();
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
 
         World world = player.getWorld();
         
         if (item.getType() == Material.FLINT_AND_STEEL) {
             if (entity == null) {
                 return;
             }
 
             // Flint & Steel + Fire Aspect = set mobs on fire
             if (item.containsEnchantment(FIRE_ASPECT)) {
                 entity.setFireTicks(getFireTicks(item.getEnchantmentLevel(FIRE_ASPECT)));
 
                 damage(item);
             }
 
             // Flint & Steel + Respiration = smoke inhalation (confusion effect on player)
             if (item.containsEnchantment(RESPIRATION)) {
                 world.playEffect(entity.getLocation(), Effect.SMOKE, 0);    // TOOD: smoke direction
                 world.playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);    // TOOD: smoke direction
 
                 // Confusion effect on players
                 if (entity instanceof CraftPlayer) {
                     ((CraftPlayer)entity).getHandle().addEffect(new net.minecraft.server.MobEffect(
                         9,      // confusion  - http://wiki.vg/Protocol#Effects
                         20*10*item.getEnchantmentLevel(RESPIRATION),  // length
                         1));    // amplifier
                     // TODO: can we used the predefined effects (w/ duration, amplifier) in MobEffectList?
                     // as suggested here: http://forums.bukkit.org/threads/potion-events.57086/#post-936679
                     // however, b() takes a MobEffect, but MobEffectList.CONFUSIOn is a MobEffectList
                     //(((CraftPlayer)entity).getHandle()).b(MobEffectList.CONFUSION);
 
                     damage(item);
                 }
             }
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Smite = gouge eyes (blindness effect on player)
             if (item.containsEnchantment(SMITE)) {
                 if (entity instanceof CraftPlayer) {
                     ((CraftPlayer)entity).getHandle().addEffect(new net.minecraft.server.MobEffect(
                         15,     // blindness
                         20*10*item.getEnchantmentLevel(SMITE),  // length
                         1));    // amplifier
 
                     damage(item);
                 }
             }
 
             // Shears + Bane of Arthropods = collect spider eyes
             if (item.containsEnchantment(BANE)) {
                 if (entity instanceof CaveSpider || entity instanceof Spider) {
                     Creature bug = (Creature)entity;
 
                     // If at least 50% health, cut out eyes, then drop health
                     if (bug.getHealth() >= bug.getMaxHealth() / 2) {
                         world.dropItemNaturally(bug.getEyeLocation(), new ItemStack(Material.SPIDER_EYE, 1));
 
                         bug.setHealth(bug.getMaxHealth() / 2 - 1);
                     }
 
                     damage(item);
                 }
             }
 
             // Shears + Looting = feathers from chicken (secondary)
             if (item.containsEnchantment(LOOTING)) {
                 if (entity instanceof Chicken) {
                     Creature bird = (Creature)entity;
 
                     // Pulling feathers damages the creature
                     if (bird.getHealth() >= bird.getMaxHealth() / 2) {
                         world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.FEATHER, random.nextInt(5) + 1));
 
                         bird.setHealth(bird.getMaxHealth() / 2 - 1);
                         // There isn't any "featherless chicken" sprite
                     }
                     
                     damage(item);
                 }
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
                 damage(item);
             }
 
             // BLOCKED: Sword + Infinity = invisibility when blocking 
             // Also has no implemented effect in Minecraft 1.1. Maybe a plugin could use?
             if (item.containsEnchantment(INFINITE)) {
                 ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     14,     // invisibility
                     20*2*item.getEnchantmentLevel(INFINITE),  // length
                     10));    // amplifier
                 damage(item);
             }
             */
 
 
             // Sword + Protection = resistance when blocking 
             if (item.containsEnchantment(PROTECTION)) {
                  ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
                     11,     // resistance
                     20*10*item.getEnchantmentLevel(PROTECTION),  // length
                     10));    // amplifier
                 damage(item);
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
 
             // Pickaxe + Flame = auto-smelt
             // Shovel + Flame = auto-smelt
             // Axe + Flame = auto-smelt
             if (item.containsEnchantment(FLAME)) {
                 Collection<ItemStack> rawDrops = block.getDrops(item);
 
                 for (ItemStack rawDrop: rawDrops) {
                     ItemStack smeltedDrop = smelt(rawDrop);
 
                    if (smeltedDrop != null && smeltedDrop.getType() != Material.AIR) {
                        world.dropItemNaturally(block.getLocation(), smeltedDrop);
                    }
                 }
 
 
                 block.setType(Material.AIR);
 
                 // no extra damage
             }
 
             // Axe + Power = fell tree
             if (isAxe(item.getType()) && item.containsEnchantment(POWER) && block.getType() == Material.LOG) {
                 // Chop tree
                 breakContiguous(block, item, 100 * item.getEnchantmentLevel(POWER));
                 // no extra damage
             }
 
             // Shovel + Power = excavation (dig large area, no drops)
             if (isShovel(item.getType()) && item.containsEnchantment(POWER) && 
                 isExcavatable(block.getType())) {
 
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
                             if (isExcavatable(type)) {
                                 Block b = world.getBlockAt(x, y, z);
                                 b.setType(Material.AIR);
                             }
                         }
                     }
                 }
                 // no extra damage
             }
 
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Silk Touch = collect cobweb, dead bush
             if (item.containsEnchantment(SILK_TOUCH)) {
                 if (block.getType() == Material.DEAD_BUSH ||
                     block.getType() == Material.WEB) {
 
                     world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
 
                     block.setType(Material.AIR);
                 } 
                 // no extra damage
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
                 // no extra damage
             }
 
             // Shears + Power = hedge trimmer; cut grass
             // see also secondary effect above
             if (item.containsEnchantment(POWER) && block.getType() == Material.LEAVES) {
                 breakContiguous(block, item, 50 * item.getEnchantmentLevel(POWER));
                 // no extra damage
             }
 
         } else if (isHoe(item.getType())) {
             // Hoe + Silk Touch = collect farmland, crop block, pumpkin/melon stem, cake block, sugarcane block, netherwart block (preserving data)
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
                 // no extra damage
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
 
         // Shears + Looting = more wool (random colors); feathers from chickens
         // see also secondary effect above
         if (tool.getType() == Material.SHEARS && tool.containsEnchantment(LOOTING)) {
             Location loc = entity.getLocation();
 
             int quantity = random.nextInt(tool.getEnchantmentLevel(LOOTING) * 2);
             for (int i = 0; i < quantity; i += 1) {
                 short color = (short)random.nextInt(16);
 
                 world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.WOOL, 1, color));
             }
             // no extra damage
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
 
         // Bow + Smite = strike lightning
         if (item.containsEnchantment(SMITE)) {
             world.strikeLightning(dest);
         }
 
         // Bow + Fire Aspect = firey explosions
         if (item.containsEnchantment(FIRE_ASPECT)) {
             float power = 1.0f * item.getEnchantmentLevel(FIRE_ASPECT);
 
             world.createExplosion(dest, power, true);
         }
 
         // Bow + Aqua Affinity = freeze water, stun players
         if (item.containsEnchantment(AQUA_AFFINITY)) {
             int r = item.getEnchantmentLevel(AQUA_AFFINITY);
 
             // freeze water 
             int x0 = dest.getBlockX();
             int y0 = dest.getBlockY();
             int z0 = dest.getBlockZ();
            
             // TODO: refactor
             for (int dx = -r; dx <= r; dx += 1) {
                 for (int dy = -r; dy <= r; dy += 1) {
                     for (int dz = -r; dz <= r; dz += 1) {
                         Block b = world.getBlockAt(dx+x0, dy+y0, dz+z0);
                        
                         if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
                             b.setType(Material.ICE);
                         }
                     }
                 }
             }
 
             // stun nearby players
             List<Entity> victims = arrow.getNearbyEntities(r, r, r);
             for (Entity victim: victims) {
                 if (victim instanceof CraftPlayer) {
                     CraftPlayer victimPlayer = (CraftPlayer)victim;
                     (victimPlayer).getHandle().addEffect(new net.minecraft.server.MobEffect(
                         2, // moveSlowdown - http://wiki.vg/Protocol#Effects
                         20*10*item.getEnchantmentLevel(AQUA_AFFINITY), // length
                         1)); // amplifier
                 }
             }
 
             // no extra damage
         }
 
         // TODO: phase, fire arrow through blocks
 
         // Bow + Feather Falling = teleport
         if (item.containsEnchantment(FEATHER_FALLING)) {
             // use up the arrow (TODO: not at higher levels?) or set no pickup?
             arrow.remove();
 
             player.teleport(dest);
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
 
                 damage(item);
             }
             
             // Fishing Rod + Smite = strike mobs with lightning
             if (item.containsEnchantment(SMITE)) {
                 world.strikeLightning(entity.getLocation());
 
                 damage(item);
             }
         } else if (state == PlayerFishEvent.State.CAUGHT_FISH) {
             // Fishing Rod + Flame = catch cooked fish
             if (item.containsEnchantment(FLAME)) {
                 event.setCancelled(true);
 
                 // replace raw with cooked (TODO: play well with all other enchantments)
                 world.dropItemNaturally(player.getLocation(), new ItemStack(Material.COOKED_FISH, 1));
             }
 
             // Fishing Rod + Looting = catch extra fish
             if (item.containsEnchantment(LOOTING)) {
                 // one extra per level
                 world.dropItemNaturally(player.getLocation(), new ItemStack(Material.RAW_FISH, item.getEnchantmentLevel(FORTUNE)));
             }
 
             // Fishing Rod + Fortune = catch junk
             if (item.containsEnchantment(FORTUNE)) {
                 int quantity  = item.getEnchantmentLevel(FORTUNE);
 
                 Material m;
 
                 // TODO: configurable, like Junkyard Creek http://dev.bukkit.org/server-mods/junkyardcreek/
                 switch(random.nextInt(19)) {
                 case 0: m = Material.MONSTER_EGGS; break;       // hidden silverfish block
                 case 1:
                 default:
                 case 2: m = Material.DIRT; break;
                 case 3: 
                 case 4: m = Material.WOOD; break;
                 case 5: m = Material.SPONGE; break;
                 case 6: m = Material.DEAD_BUSH; break;
                 case 7: m = Material.EYE_OF_ENDER; break;
                 case 8: m = Material.DIAMOND; break;
                 case 9:
                 case 10:
                 case 11: m = Material.IRON_INGOT; break;
                 case 12:
                 case 13: m = Material.GOLD_INGOT; break;
                 case 14: m = Material.CHAINMAIL_CHESTPLATE; break;
                 case 15: 
                 case 16: m = Material.WATER_BUCKET; break;
                 case 17: m = Material.BOAT; break;
                 case 18: m = Material.SLIME_BALL; break;
                 case 19: m = Material.FERMENTED_SPIDER_EYE; break;
                 }
 
                 world.dropItemNaturally(player.getLocation(), new ItemStack(m, quantity));
 
                 // TODO: should also cancel fish event as to not drop?
             }
 
             // no extra damage 
 
         } else if (state == PlayerFishEvent.State.FAILED_ATTEMPT) {
             // Fishing Rod + Silk Touch = catch more reliably
             if (item.containsEnchantment(SILK_TOUCH)) {
                 // probability
                 // TODO: configurable levels, maybe to 100?
                 // 4 = always
                 int n = 4 - item.getEnchantmentLevel(SILK_TOUCH);
                 if (n < 1) {
                     n = 1;
                 }
 
                 if (random.nextInt(n) == 0) {
                     // TODO: integrate with Flame to catch cooked, too
                     world.dropItemNaturally(player.getLocation(), new ItemStack(Material.RAW_FISH, 1));
                 }
             }
 
             // no extra damage
         } else if (state == PlayerFishEvent.State.FISHING) {
             // Fishing Rod + Efficiency = fish faster
             if (item.containsEnchantment(EFFICIENCY)) {
                
                 // 13 seconds for level 1, down to 1 for level 7
                 int delayTicks = (15 - item.getEnchantmentLevel(EFFICIENCY) * 2) * 20;
                 if (delayTicks < 0) {
                     delayTicks = 0;
                 }
                 // TODO: add some randomness
 
                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFishTask(player, world), delayTicks);
 
                 // TODO: cancel task if stop fishing (change state)
             }
         }
     }
 }
 
 // Task to efficiently drop fish after some time of fishing
 class EnchantMoreFishTask implements Runnable {
     Player player;
     World world;
 
     public EnchantMoreFishTask(Player p, World w) {
         player = p;
         world = w;
     }
 
 
     public void run() {
         ItemStack tool = player.getItemInHand();
         if (tool != null && tool.getType() == Material.FISHING_ROD) {
             world.dropItemNaturally(player.getLocation(), new ItemStack(Material.RAW_FISH, 1));
 
             EnchantMoreListener.damage(tool);
         }
 
         // TODO: reel in fishing line?
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
