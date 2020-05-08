 /* 
 Copyright (c) 2012, Mushroom Hostage
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
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
 import org.bukkit.util.*;
 import org.bukkit.*;
 
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.craftbukkit.entity.CraftArrow;
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
 import net.minecraft.server.EntityArrow;
 import net.minecraft.server.EnumSkyBlock;
 
 class EnchantMoreListener implements Listener {
 
     // Better enchantment names more closely matching in-game display
     // TODO: replace with ItemStackX
     final static Enchantment PROTECTION = Enchantment.PROTECTION_ENVIRONMENTAL;
     final static Enchantment FIRE_PROTECTION = Enchantment.PROTECTION_FIRE;
     final static Enchantment FEATHER_FALLING = Enchantment.PROTECTION_FALL;
     final static Enchantment BLAST_PROTECTION = Enchantment.PROTECTION_EXPLOSIONS;
     final static Enchantment PROJECTILE_PROTECTION = Enchantment.PROTECTION_PROJECTILE;
     final static Enchantment RESPIRATION = Enchantment.OXYGEN;
     final static Enchantment AQUA_AFFINITY = Enchantment.WATER_WORKER;
     final static Enchantment SHARPNESS = Enchantment.DAMAGE_ALL;
     final static Enchantment SMITE = Enchantment.DAMAGE_UNDEAD;
     final static Enchantment BANE = Enchantment.DAMAGE_ARTHROPODS;
     final static Enchantment KNOCKBACK = Enchantment.KNOCKBACK;
     final static Enchantment FIRE_ASPECT = Enchantment.FIRE_ASPECT;
     final static Enchantment LOOTING = Enchantment.LOOT_BONUS_MOBS;
     final static Enchantment EFFICIENCY = Enchantment.DIG_SPEED;
     final static Enchantment SILK_TOUCH = Enchantment.SILK_TOUCH;
     final static Enchantment UNBREAKING = Enchantment.DURABILITY;
     final static Enchantment FORTUNE = Enchantment.LOOT_BONUS_BLOCKS;
     final static Enchantment POWER = Enchantment.ARROW_DAMAGE;
     final static Enchantment PUNCH = Enchantment.ARROW_KNOCKBACK;
     final static Enchantment FLAME = Enchantment.ARROW_FIRE;
     final static Enchantment INFINITE = Enchantment.ARROW_INFINITE;
 
     static Random random;
    
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
         
         final World world = player.getWorld();
 
         // Actions not requiring a block
 
         if (item.getType() == Material.BOW && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
             // Bow + Efficiency = instant shoot
             if (item.containsEnchantment(EFFICIENCY)) {
                 player.shootArrow();
             }
         } else if (isSword(item.getType())) {
             if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                 // Sword + Power = strike lightning 100+ meters away
                 if (item.containsEnchantment(POWER)) {
                     int maxDistance = 100;  // TODO: configurable
                     Block target = player.getTargetBlock(null, maxDistance * item.getEnchantmentLevel(FLAME));
 
                     if (target != null) {
                         world.strikeLightning(target.getLocation());
                     }
                 }
             } /*else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                 // TODO: Sword + Blast Protection = blocking summons summon fireballs
                 if (item.containsEnchantment(BLAST_PROTECTION)) {
                     // http://forums.bukkit.org/threads/summoning-a-fireball.40724/#post-738436
                     Location loc = event.getPlayer().getLocation();
                     Block b = event.getPlayer().getTargetBlock(null, 100 * item.getEnchantmentLevel(BLAST_PROTECTION));
                     if (b != null) {
                         Location target = b.getLocation();
                         Location from = lookAt(loc, target);
                         Entity fireball = from.getWorld().spawn(from, Fireball.class);
                         fireball.setVelocity(new Vector(0, -1, 0)); // TODO
                     } else {
                         plugin.log.info("no target?");
                     }
                 }
             }*/
 
             // TODO: Aqua Affinity = slowness
         } else if (isShovel(item.getType())) {
             // Shovel + Silk Touch II = harvest fire (secondary)
             if (item.containsEnchantment(SILK_TOUCH) && item.getEnchantmentLevel(SILK_TOUCH) >= 2 &&
                 (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                 Block target = player.getTargetBlock(null, 3 * item.getEnchantmentLevel(SILK_TOUCH));
 
                 if (target.getType() == Material.FIRE) {
                     world.dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1));
                 }
             }
         }
 
         if (block == null) {
             return;
         }
 
         // Everything else below requires a block
 
 
         if (item.getType() == Material.SHEARS) {
             // Shears + Power = cut grass (secondary effect)
             if (item.containsEnchantment(POWER)) {
                 if (block.getType() == Material.GRASS) {
                     block.setType(Material.DIRT);
                 }
                 damage(item);
             }
         } else if (item.getType() == Material.FLINT_AND_STEEL && action == Action.RIGHT_CLICK_BLOCK) {
             // Flint & Steel + Smite = strike lightning ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/8-fishing-rod-smite-strike-lightning/))
             if (item.containsEnchantment(SMITE)) {
                 world.strikeLightning(block.getLocation());
                 damage(item, 9);
             }
 
             // Flint & Steel + Fire Protection = fire resistance ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/10-flint-steel-fire-protection-fire-resistance/))
             if (item.containsEnchantment(FIRE_PROTECTION)) {
                 applyPlayerEffect(player, EFFECT_FIRE_RESISTANCE, item.getEnchantmentLevel(FIRE_PROTECTION));
                 // no extra damage
             }
 
             // Flint & Steel + Aqua Affinity = vaporize water ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/9-flint-steel-aqua-affinity-vaporize-water/))
             if (item.containsEnchantment(AQUA_AFFINITY)) {
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
 
             // Flint & Steel + Sharpness = fiery explosion
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
             // Hoe + Aqua Affinity = auto-hydrate ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/11-hoe-aqua-affinity-auto-hydrate/))
             if (item.containsEnchantment(AQUA_AFFINITY)) {
                 // As long as not in hell, hydrate nearby
                 if (world.getEnvironment() != World.Environment.NETHER) {
                     int n = item.getEnchantmentLevel(AQUA_AFFINITY);
 
                     // Change adjacent air blocks to water
                     for (int dx = -1; dx <= 1; dx += 1) {
                         for (int dz = -1; dz <= 1; dz += 1) {
                             Block near = block.getRelative(dx * n, 0, dz * n);
                             // if either air or flowing water, make stationary water
                             if (near.getType() == Material.AIR || near.getType() == Material.WATER) {
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
 
             // Hoe + Respiration = grow ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/12-hoe-respiration-grow/))
             // Note, left-click will also destroy sensitive plants (wheat, saplings, though interestingly not shrooms),
             // so it will only work on blocks like grass (which does not break instantly). For 
             // this reason, also allow right-click for grow, even though it means you cannot till.
             if (item.containsEnchantment(RESPIRATION)) {
                 growStructure(block.getLocation(), player);
                 damage(item);
 
                 // no need to cancel?
                 //event.setCancelled(true);
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
 
     /*
     // Aim function 
     // see http://forums.bukkit.org/threads/summoning-a-fireball.40724/#post-738436
     public static Location lookAt(Location from, Location to) {
         Location loc = from.clone();
 
         double dx = to.getX() - from.getX();
         double dy = to.getY() - from.getY();
         double dz = to.getZ() - from.getZ();
         if (dx != 0) {
             if (dx < 0) {
                 loc.setYaw((float)(1.5 * Math.PI));
             } else {
                 loc.setYaw((float)(0.5 * Math.PI));
             }
             loc.setYaw((float)loc.getYaw() - (float)Math.atan(dz / dx));
         } else if (dz < 0) {
             loc.setYaw((float)Math.PI);
         }
         double dxz = Math.sqrt(dx * dx + dz * dz);
         loc.setPitch((float)-Math.atan(dy / dxz));
         loc.setYaw(-loc.getYaw() * 180f / (float)Math.PI);
         loc.setPitch(loc.getPitch() * 180f / (float)Math.PI);
         return loc;
     }*/
 
 
     // Attempt to grow organic structure
     private void growStructure(Location loc, Player player) {
         int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
         World world = loc.getWorld();
 
         // Use bonemeal (white dye/ink) to grow
         CraftItemStack bonemealStack = (new CraftItemStack(Material.INK_SACK, 1, (short)15));
 
         // 'a' unobfuscated = onItemUse
         net.minecraft.server.Item.INK_SACK.a(bonemealStack.getHandle(), ((CraftPlayer)player).getHandle(), ((CraftWorld)world).getHandle(), x, y, z, 0/*unused*/);
     }
 
     public static boolean isHoe(Material m) {
         return m == Material.DIAMOND_HOE ||
             m == Material.GOLD_HOE || 
             m == Material.IRON_HOE ||
             m == Material.STONE_HOE ||
             m == Material.WOOD_HOE;
     }
 
     public static boolean isSword(Material m) {
         return m == Material.DIAMOND_SWORD ||   
             m == Material.GOLD_SWORD ||
             m == Material.IRON_SWORD ||
             m == Material.STONE_SWORD ||
             m == Material.WOOD_SWORD;
     }
 
     public static boolean isPickaxe(Material m) {
         return m == Material.DIAMOND_PICKAXE ||
             m == Material.GOLD_PICKAXE ||
             m == Material.IRON_PICKAXE ||
             m == Material.STONE_PICKAXE ||
             m == Material.WOOD_PICKAXE;
     }
 
     public static boolean isShovel(Material m) {
         return m == Material.DIAMOND_SPADE ||
             m == Material.GOLD_SPADE ||
             m == Material.IRON_SPADE ||
             m == Material.STONE_SPADE ||
             m == Material.WOOD_SPADE;
     }
 
     public static boolean isAxe(Material m) {
         return m == Material.DIAMOND_AXE ||
             m == Material.GOLD_AXE ||
             m == Material.IRON_AXE ||
             m == Material.STONE_AXE ||
             m == Material.WOOD_AXE;
     }
 
     // Get whether material is a farm-related block, either land or growing crops
     public static boolean isFarmBlock(Material m) {
         return m == Material.SOIL ||     // Farmland
             m == Material.CROPS ||    // wheat TODO: update wiki, calls 'Wheat Seeds' though in-game 'Crops'
             m == Material.SUGAR_CANE_BLOCK ||
             m == Material.CAKE_BLOCK ||
             m == Material.PUMPKIN_STEM ||
             m == Material.MELON_STEM ||
             m == Material.NETHER_WARTS; // not the item, that is NETHER_STALK (confusingly)
     }
 
     // Get whether able to be excavated by shovel
     public static boolean isExcavatable(int m) {
         return m == Material.DIRT.getId() ||
             m == Material.GRASS.getId() ||
             m == Material.GRAVEL.getId() ||
             m == Material.SOUL_SAND.getId() ||
             m == Material.NETHERRACK.getId(); // not normally diggable, but why not?
     }
 
     public static boolean isExcavatable(Material m) {
         return isExcavatable(m.getId());
     }
 
     // Return whether is a wooden block
     public static boolean isWoodenBlock(Material m, byte data) {
         return m == Material.WOOD || 
             m == Material.WOOD_PLATE || 
             m == Material.WOOD_STAIRS ||
             m == Material.WOODEN_DOOR || 
             m == Material.LOG ||
             (m == Material.STEP && data == 2) ||      // wooden slab
             (m == Material.DOUBLE_STEP && data == 2);// wooden double slab
     }
 
     // http://wiki.vg/Protocol#Effects
     private static final int EFFECT_MOVE_SPEED = 1;
     private static final int EFFECT_MOVE_SLOW_DOWN = 2;
     private static final int EFFECT_DIG_SPEED = 3;
     private static final int EFFECT_DIG_SLOW_DOWN = 4;
     private static final int EFFECT_DAMAGE_BOOST = 5;
     private static final int EFFECT_HEAL = 6;
     private static final int EFFECT_HARM = 7;
     private static final int EFFECT_JUMP = 8;
     private static final int EFFECT_CONFUSION = 9;
     private static final int EFFECT_REGENERATION = 10;
     private static final int EFFECT_RESISTANCE = 11;
     private static final int EFFECT_FIRE_RESISTANCE = 12;
     private static final int EFFECT_WATER_BREATHING = 13;
     private static final int EFFECT_INVISIBILITY = 14;  // sadly, no effect in 1.1
     private static final int EFFECT_BLINDNESS = 15;
     private static final int EFFECT_NIGHTVISION = 16;   // sadly, no effect in 1.1
     private static final int EFFECT_HUNGER = 17;
     private static final int EFFECT_WEAKNESS = 18;
     private static final int EFFECT_POISON = 19;
 
 
     private void applyPlayerEffect(Player player, int effect, int level) {
         ((CraftPlayer)player).getHandle().addEffect(new net.minecraft.server.MobEffect(
             effect,             // http://wiki.vg/Protocol#Effects
             20 * 10 * level,    // duration in ticks
             1));                // amplifier
 
         // TODO: can we used the predefined effects (w/ duration, amplifier) in MobEffectList?
         // as suggested here: http://forums.bukkit.org/threads/potion-events.57086/#post-936679
         // however, b() takes a MobEffect, but MobEffectList.CONFUSIOn is a MobEffectList
         //(((CraftPlayer)entity).getHandle()).b(MobEffectList.CONFUSION);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Entity entity = event.getRightClicked();
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
 
         final World world = player.getWorld();
         
         if (item.getType() == Material.FLINT_AND_STEEL) {
             if (entity == null) {
                 return;
             }
 
             // Flint & Steel + Fire Aspect = set mobs on fire
             if (item.containsEnchantment(FIRE_ASPECT)) {
                 entity.setFireTicks(getFireTicks(item.getEnchantmentLevel(FIRE_ASPECT)));
 
                 damage(item);
 
                 // Flint & Steel + Fire Protection = player fire resistance (secondary)
                 // We apply this for lighting blocks, too; this one is for attacking mobs
                 if (item.containsEnchantment(FIRE_PROTECTION)) {
                     applyPlayerEffect(player, EFFECT_FIRE_RESISTANCE, item.getEnchantmentLevel(FIRE_PROTECTION));
                     // no extra damage
                 }
 
             }
 
             // Flint & Steel + Respiration = smoke inhalation (confusion effect on player)
             if (item.containsEnchantment(RESPIRATION)) {
                 world.playEffect(entity.getLocation(), Effect.SMOKE, 0);    // TOOD: smoke direction
                 world.playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);    // TOOD: smoke direction
 
                 // Confusion effect on players
                 if (entity instanceof CraftPlayer) {
                     applyPlayerEffect((CraftPlayer)entity, EFFECT_CONFUSION, item.getEnchantmentLevel(RESPIRATION));
 
                     damage(item);
                 }
             }
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Smite = gouge eyes (blindness effect on player)
             if (item.containsEnchantment(SMITE)) {
                 if (entity instanceof CraftPlayer) {
                     applyPlayerEffect((CraftPlayer)entity, EFFECT_BLINDNESS, item.getEnchantmentLevel(SMITE));
 
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
             // BLOCKED: Sword + ? = night vision when blocking 
             // The visual effect plays (navy blue swirly particles), but doesn't actually do anything as of Minecraft 1.1
             if (item.containsEnchantment(FLAME)) {
                 applyPlayerEffect(player, EFFECT_NIGHT_VISION, item.getEnchantmentLevel(FLAME));
                 damage(item);
             }
 
             // BLOCKED: Sword + Infinity = invisibility when blocking 
             // Also has no implemented effect in Minecraft 1.1. Maybe a plugin could use?
             // TODO: use Vanish API in dev builts of Bukkit, that VanishNoPacket uses
             if (item.containsEnchantment(INFINITE)) {
                 applyPlayerEffect(player, EFFECT_INVISIBILITY, item.getEnchantmentLevel(INFINITE));
                 damage(item);
             }
             */
 
 
             // Sword + Protection = resistance when blocking 
             if (item.containsEnchantment(PROTECTION)) {
                 applyPlayerEffect(player, EFFECT_RESISTANCE, item.getEnchantmentLevel(PROTECTION));
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
         final World world = player.getWorld();
 
         if (item == null) {
             return;
         }
 
         if (isPickaxe(item.getType()) ||
             isShovel(item.getType()) ||
             isAxe(item.getType())) {
 
             // Pickaxe + Flame = auto-smelt ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/))
             // Shovel + Flame = auto-smelt ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/))
             // Axe + Flame = auto-smelt ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/))
             if (item.containsEnchantment(FLAME)) {
                 Collection<ItemStack> rawDrops = block.getDrops(item);
 
                 for (ItemStack rawDrop: rawDrops) {
                     // note: original smelted idea from Firelord tools http://dev.bukkit.org/server-mods/firelord/
                     // also see Superheat plugin? either way, coded this myself..
                     ItemStack smeltedDrop = smelt(rawDrop);
 
                     if (smeltedDrop != null && smeltedDrop.getType() != Material.AIR) {
                         world.dropItemNaturally(block.getLocation(), smeltedDrop);
                     }
                 }
 
 
                 block.setType(Material.AIR);
 
                 // no extra damage
             }
 
             if (isAxe(item.getType())) {
                 // Axe + Power = fell tree ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/3-axe-power-fell-tree/))
                 if (item.containsEnchantment(POWER) && block.getType() == Material.LOG) {
                     // Chop tree
                     breakContiguous(block, item, 100 * item.getEnchantmentLevel(POWER));
                     // no extra damage
                 }
             }
 
             if (isShovel(item.getType())) {
                 // Shovel + Power = excavation (dig large area, no drops)
                 if (item.containsEnchantment(POWER) && isExcavatable(block.getType())) {
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
 
                 // Shovel + Silk Touch II = harvest fallen snow, fire
                 // (fire elsewhere)
                 if (item.containsEnchantment(SILK_TOUCH) && item.getEnchantmentLevel(SILK_TOUCH) >= 2) {
                     if (block.getType() == Material.SNOW) {
                         world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                         block.setType(Material.AIR);
                         event.setCancelled(true);   // do not drop snowballs
                     }
                 }
 
             }
             // Pickaxe + Silk Touch II = harvest ice
             if (isPickaxe(item.getType())) {
                 if (item.containsEnchantment(SILK_TOUCH) && item.getEnchantmentLevel(SILK_TOUCH) >= plugin.getConfig().getInt("pickaxeSilkTouchIceLevel", 2)) {
                     if (block.getType() == Material.ICE) {
                         world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                         block.setType(Material.AIR);
                         // TODO craftbukkit 1.1-R3+MLP+MCF+IC2+BC2+RP2 NPE: at net.minecraft.server.ItemInWorldManager.breakBlock(ItemInWorldManager.java:254)
                         // if we don't do this, so do it
                         event.setCancelled(true); 
                         // no extra damage
                     }
                 }
             }
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Silk Touch = collect cobweb, dead bush
             if (item.containsEnchantment(SILK_TOUCH)) {
                 // Note: you can collect dead bush with shears on 12w05a!
                 // http://www.reddit.com/r/Minecraft/comments/pc2rs/just_noticed_dead_bush_can_be_collected_with/
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
         World world = block.getWorld();
         Player player = event.getPlayer();
 
         // Item to place as a block
         // NOT event.getItemInHand(), see https://bukkit.atlassian.net/browse/BUKKIT-596 BlockPlaceEvent getItemInHand() loses enchantments
         ItemStack item = player.getItemInHand();
 
         // Set data of farm-related block
         if (item != null && item.containsEnchantment(SILK_TOUCH)) {
             if (isFarmBlock(item.getType())) {
                 plugin.log.info("data"+item.getEnchantmentLevel(SILK_TOUCH));
                 // broken in 1.1-R2??
                 // TODO
                 block.setData((byte)item.getEnchantmentLevel(SILK_TOUCH));
             }
         }
 
         if (block != null && block.getType() == Material.ICE) {
             if (world.getEnvironment() == World.Environment.NETHER && plugin.getConfig().getBoolean("sublimateIce", false)) {
                 // sublimate ice to vapor
                 block.setType(Material.AIR);
 
                 // turn into smoke
                 world.playEffect(block.getLocation(), Effect.SMOKE, 0);
 
                 // Workaround type not changing, until fix is in a build:
                 // "Allow plugins to change ID and Data during BlockPlace event." Fixes BUKKIT-674
                 // https://github.com/Bukkit/CraftBukkit/commit/f29b84bf1579cf3af31ea3be6df0bc8917c1de0b
 
                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreAirTask(block));
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
         final World world = player.getWorld();
 
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
         final World world = dest.getWorld();
 
         // Bow + Looting = steal ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/6-bow-looting-steal/))
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
 
         // Bow + Fire Aspect = fiery explosions ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/5-bow-fire-aspect-fiery-explosions/))
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
             
             // TODO: only poison hit player!
 
             // stun nearby players
             List<Entity> victims = arrow.getNearbyEntities(r, r, r);
             for (Entity victim: victims) {
                 if (victim instanceof CraftPlayer) {
                     applyPlayerEffect((CraftPlayer)victim, EFFECT_MOVE_SLOW_DOWN, r);
                 }
             }
 
             // no extra damage
         }
 
         // Bow + Knockback = pierce blocks
         if (item.containsEnchantment(KNOCKBACK)) {
             class ArrowPierceTask implements Runnable {
                 Arrow arrow;
                 int depth;
 
                 public ArrowPierceTask(Arrow arrow, int depth) {
                     this.arrow = arrow;
                     this.depth = depth;
                 }
 
                 public void run() {
                     Vector velocity = arrow.getVelocity().clone();  // TODO: unit vector?
                     Block block = getArrowHit(arrow);
 
                     if (block.getType() == Material.BEDROCK) {
                         return; // bad news
                     }
                     // TODO: factor in hardness of material somehow?
 
                     // Pierce block, destroying it
                     block.setType(Material.AIR);
                     // TODO: should it drop items?
                   
                     // Trace through multiple blocks in same direction, up to enchantment level
                     if (depth > 1) {
                         Vector start = new Vector(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
                         BlockIterator it = new BlockIterator(world, start, velocity, 0, depth);
                         while (it.hasNext()) {
                             Block b = it.next();
                             if (b.getType() != Material.BEDROCK) {
                                 b.setType(Material.AIR);
                                 // TODO: figure out how to refresh lighting here
                                 //b.setData(b.getData(), true);
                             }
                         }
                     }
 
                     // if we don't remove, the arrow will fall down, then hit another
                     // block, and another..until it reaches bedrock!
                     arrow.remove();
                 }
             }
 
             Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ArrowPierceTask(arrow, item.getEnchantmentLevel(KNOCKBACK)));
         }
 
         // TODO: phase, arrow through blocks
 
         // TODO: fire protection = remove water (like flint & steel aqua affinity)
 
         // Bow + Bane of Arthropods = poison
         if (item.containsEnchantment(BANE)) {
             // TODO: only poison hit player!
 
             // poison nearby players
             int r = item.getEnchantmentLevel(BANE);
             List<Entity> victims = arrow.getNearbyEntities(r, r, r);
             for (Entity victim: victims) {
                 if (victim instanceof CraftPlayer) {
                     applyPlayerEffect((CraftPlayer)victim, EFFECT_POISON, r);
                 }
             }
 
         }
 
         // Bow + Feather Falling = teleport ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/4-bow-feather-falling-teleport/))
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
 
             // Fishing Rod + Fortune = catch junk ([screenshot](http://dev.bukkit.org/server-mods/enchantmore/images/7-fishing-rod-fortune-catch-sunken-treasure/))
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
                 // TODO: leather boot
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
 
     @EventHandler(priority = EventPriority.NORMAL) 
     public void onEntityShootBow(EntityShootBowEvent event) {
         ItemStack bow = event.getBow();
 
         if (bow == null) {
             // shot by skeleton
             return;
         }
 
         Entity projectile = event.getProjectile();
         if (!(projectile instanceof Arrow)) {
             return;
         }
 
         // Bow + Sharpness = increase velocity
         if (bow.containsEnchantment(SHARPNESS)) {
             double factor = 2.0 * bow.getEnchantmentLevel(SHARPNESS);   // TODO: configurable factor
 
             // TODO: instead of scalar multiplication, therefore also multiplying the 'shooting inaccuracy'
             // offset, should we instead try to straighten out the alignment vector?
             projectile.setVelocity(projectile.getVelocity().multiply(factor));
 
             event.setProjectile(projectile);
         }
     }
 
     // Get the block an arrow hit
     // see http://forums.bukkit.org/threads/on-how-to-get-the-block-an-arrow-lands-in.55768/#post-954542
     public Block getArrowHit(Arrow arrow) {
         World world = arrow.getWorld();
 
         net.minecraft.server.EntityArrow entityArrow = ((CraftArrow)arrow).getHandle();
 
         try {
             // saved to NBT tag as xTile,yTile,zTile
             Field fieldX = net.minecraft.server.EntityArrow.class.getDeclaredField("e");
             Field fieldY = net.minecraft.server.EntityArrow.class.getDeclaredField("f");
             Field fieldZ = net.minecraft.server.EntityArrow.class.getDeclaredField("g");
 
             fieldX.setAccessible(true);
             fieldY.setAccessible(true);
             fieldZ.setAccessible(true);
 
             int x = fieldX.getInt(entityArrow);
             int y = fieldY.getInt(entityArrow);
             int z = fieldZ.getInt(entityArrow);
 
             return world.getBlockAt(x, y, z);
         } catch (Exception e) {
             plugin.log.info("getArrowHit("+arrow+" reflection failed: "+e);
             throw new IllegalArgumentException(e);
         }
     }
     
     /*
     // TODO: attempt to cancel burning when swimming in lava - no effect
     @EventHandler(priority = EventPriority.NORMAL)
     public void onEntityCombust(EntityCombustEvent event) {
         Entity entity = event.getEntity();
         if (!(entity instanceof Player)) {
             return;
         }
 
         Player player = (Player)entity;
 
         ItemStack helmet = player.getInventory().getHelmet();
         if (helmet != null && helmet.containsEnchantment(FIRE_ASPECT)) {
             event.setCancelled(true);
         }
     }*/
 
     @EventHandler(priority = EventPriority.NORMAL) 
     public void onEntityDamage(EntityDamageEvent event) {
         Entity entity = event.getEntity();
         if (!(entity instanceof Player)) {
             return;
         }
 
         Player player = (Player)entity;
 
         ItemStack chestplate = player.getInventory().getChestplate();
 
         // Chestplate + Infinity = god mode (no damage)
         if (chestplate != null && chestplate.containsEnchantment(INFINITE)) {
             // no damage ever
             // TODO: also need to cancel death? can die elsewhere?
             event.setCancelled(true);
         }
 
         EntityDamageEvent.DamageCause cause = event.getCause();
 
         if (cause == EntityDamageEvent.DamageCause.LAVA ||
             cause == EntityDamageEvent.DamageCause.FIRE ||
             cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
             ItemStack helmet = player.getInventory().getHelmet();
             // Helmet + Fire Aspect = swim in lava
             if (helmet != null && helmet.containsEnchantment(FIRE_ASPECT)) {
                 event.setCancelled(true);   // stop knockback and damage
                 //event.setDamage(0);
                 player.setFireTicks(0);     // cool off immediately after exiting lava
 
                 // TODO: can we display air meter under lava? 
                 /*
                 player.setMaximumAir(20*10);
                 player.setRemainingAir(20*10);
                 */
 
                 // similar: http://dev.bukkit.org/server-mods/goldenchant/
                 // "golden chestplate = immunity to fire and lava damage" [like my Helmet with Fire Aspect]
                 // "golden helmet = breath underwater" [seems to overlap with Respiration, meh]
                 // "golden shoes = no fall damage" [ditto for Feather Falling]
             }
         }
 
         if (event instanceof EntityDamageByEntityEvent) {    // note: do not register directly
             EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent)event;
             Entity damager = e2.getDamager();
 
             if (damager instanceof Arrow) { // TODO: all projectiles?
                 Arrow arrow = (Arrow)damager;
 
                 // Chestplate + Knockback = reflect arrows
                 if (chestplate != null && chestplate.containsEnchantment(KNOCKBACK)) {
                     event.setCancelled(true);   // stop arrow damage
                     player.shootArrow();        // reflect arrow
 
                     // TODO: should we actually create a new arrow with the opposite velocity vector?
                     // I think so.. bounce, not reshoot
                     // not right
                     /*
                     Location location = player.getLocation();
                     World world = location.getWorld();
                     Vector velocity = arrow.getVelocity().multiply(-1);
                     float speed = 0.6f;  // "A recommend speed is 0.6"
                     float spread = 12f;  // "A recommend spread is 12"
 
 
                     world.spawnArrow(location, velocity, speed, spread);
                     */
 
                     damage(chestplate);
                 }
                 // TODO: Sword + Projectile Protection = reflect arrows while blocking
                 // make it as ^^ is, nerf above (sword direction control, chestplate not)
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerItemHeld(PlayerItemHeldEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItem(event.getNewSlot());
 
         if (item != null && isSword(item.getType())) {
             // Sword + Flame = create semi-permanent lit path
             if (item.containsEnchantment(FLAME)) {
                 // Task to light up player, as long as its holding the right tool
                 class EnchantMoreFlameLightTask implements Runnable {
                     Player player;
                     EnchantMore plugin;
 
                     public EnchantMoreFlameLightTask(EnchantMore plugin, Player player) {
                         this.plugin = plugin;
                         this.player = player;
                     }
 
                     public void run() {
                         ItemStack item = player.getItemInHand();
 
                         if (item != null && EnchantMoreListener.isSword(item.getType())) {
                             if (item.containsEnchantment(EnchantMoreListener.FLAME)) {
                                 Location to = player.getLocation();
                                 World world = to.getWorld();
 
                                 int x = to.getBlockX();
                                 int y = to.getBlockY();
                                 int z = to.getBlockZ();
 
                                 // Light up player like a torch 
                                 // http://forums.bukkit.org/threads/make-a-player-light-up-like-they-are-a-torch.58749/#post-952252
                                 // http://dev.bukkit.org/server-mods/head-lamp/
                                 ((CraftWorld)world).getHandle().a(net.minecraft.server.EnumSkyBlock.BLOCK, x, y+2, z, 15);
                                 //((CraftWorld)world).getHandle().notify(x, y+2, z);
                                 // Force update
                                 Location below = new Location(world, x, y+1, z);
                                 below.getBlock().setType(below.getBlock().getType());
                                 below.getBlock().setData(below.getBlock().getData());
 
                                 // Schedule another task to update again
                                 // This won't be scheduled if they didn't have the right tool, so it'll die off
                                 //plugin.log.info("LIT");
 
                                 // Updates faster if higher level
                                 int period = 20 * 2 / item.getEnchantmentLevel(EnchantMoreListener.FLAME);
                                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFlameLightTask(plugin, player), period);
                             }
                         }
                     }
                 }
 
 
 
 
                 EnchantMoreFlameLightTask task = new EnchantMoreFlameLightTask(plugin, player);
 
                 // run once to kick off, it will re-schedule itself if appropriate
                 // (note need to schedule to run, so will run after item actually changes in hand)
                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFlameLightTask(plugin, player));
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
         Player player = event.getPlayer();
         ItemStack boots = player.getInventory().getBoots();
 
         // Boots + Punch = shift to hover jump
         if (boots != null && boots.containsEnchantment(PUNCH)) {
             int n = boots.getEnchantmentLevel(PUNCH);
 
             player.setVelocity(new Vector(0, n, 0));
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
 
 class EnchantMoreAirTask implements Runnable {
     Block block;
 
     public EnchantMoreAirTask(Block block) {
         this.block = block;
     }
 
     public void run() {
         block.setType(Material.AIR);
     }
 }
 
 class EnchantMorePlayerMoveListener implements Listener {
     EnchantMore plugin;
 
     public EnchantMorePlayerMoveListener(EnchantMore plugin) {
         this.plugin = plugin;
 
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerMove(PlayerMoveEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) { 
             return;
         }
 
         // TODO: Boots + Efficiency  = no slow down walking on soul sand, ice 
         // idea from http://dev.bukkit.org/server-mods/elemental-armor/
         // how to speed up? or potion speed effect?
         // http://forums.bukkit.org/threads/req-useful-gold-armor-read-first.59430/
         // GoldenSprint? faster while sneaking? "feels too laggy" - listens to player move
         // GoldenEnchant? "golden pants = super speed & flying while holding shift" for 1.8 beta
         //  also on player move, but if sprinting multiples velocity vector
         //  odd diamond block enchant deal
         ItemStack boots = player.getInventory().getBoots();
 
         if (boots != null) {
             // Boots + Power = witch's broom (sprint flying)
             if (boots.containsEnchantment(EnchantMoreListener.POWER)) {
                 if (player.isSprinting()) {
                     Vector velocity = event.getTo().getDirection().normalize().multiply(boots.getEnchantmentLevel(EnchantMoreListener.POWER));
 
                     // may get kicked for flying TODO: enable flying for user
                     player.setVelocity(velocity);
 
                     // TODO: mitigate? only launch once, so can't really fly, just a boost?
                     // TODO: setSprinting(false)
                     // cool down period? 
 
                     // TODO: damage the boots? use up or infinite??
                 }
             }
 
             // Boots + Flame = firewalker (set ground on fire)
             if (boots.containsEnchantment(EnchantMoreListener.FLAME)) {
                 Location to = event.getTo();
                 Location from = event.getFrom();
                 World world = from.getWorld();
 
                 // get from where coming from
                 int dx = from.getBlockX() - to.getBlockX();
                 int dz = from.getBlockZ() - to.getBlockZ();
 
                 // a few blocks behind, further if higher level
                 dx *= boots.getEnchantmentLevel(EnchantMoreListener.FLAME) + 1;
                 dz *= boots.getEnchantmentLevel(EnchantMoreListener.FLAME) + 1;
 
                 // if moved from block (try not to set player on fire)
                 if (dx != 0 || dz != 0) {
                     Block block = world.getBlockAt(from.getBlockX() + dx, to.getBlockY(), from.getBlockZ() + dz);
                     if (block.getType() == Material.AIR) {
                         block.setType(Material.FIRE);
                     }
                 }
                 // http://dev.bukkit.org/server-mods/firelord/ "The boots set the ground on fire!"
             }
 
             // TODO: Boots + Aqua Affinity = walk on water
             /*
             if (boots.containsEnchantment(EnchantMoreListener.AQUA_AFFINITY)) {
                 World world = event.getTo().getWorld();
                 Block block = event.getTo().getBlock();
 
                 if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                     // why does this reset pitch/yaw?
                     //Location meniscus = new Location(world, event.getTo().getX(), block.getLocation().getY(), event.getTo().getZ());
                     //Location meniscus = new Location(world, event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
                     //event.setTo(meniscus);
                     // really annoying, keeps bouncing, can't move fast
                     event.setTo(event.getTo().clone().add(0, 0.1, 0));
                 }
                 // see also: God Powers jesus raft
                 // https://github.com/FriedTaco/godPowers/blob/master/godPowers/src/com/FriedTaco/taco/godPowers/Jesus.java
                 // creates a block underneath you, quite complex
             }*/
 
             // TODO: Boots + Knockback = bounce on fall
             /*
             if (boots.containsEnchantment(EnchantMoreListener.KNOCKBACK)) {
                 if (event.getTo().getY() < event.getFrom().getY()) {
                     Block block = event.getTo().getBlock();
                     Block land = block.getRelative(BlockFace.DOWN);
 
                     plugin.log.info("land="+land);
                     if (land.getType() != Material.AIR) {
                         int n = boots.getEnchantmentLevel(EnchantMoreListener.KNOCKBACK);
                         player.setVelocity(event.getPlayer().getVelocity().multiply(-n));
                     }
                 }
             }
             */
         }
     }
 }
 
 public class EnchantMore extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
 
     public void onEnable() {
         new EnchantMoreListener(this);
 
         if (getConfig().getBoolean("moveListener", true)) {
             new EnchantMorePlayerMoveListener(this);
         }
     }
     
     public void onDisable() {
     }
 }
