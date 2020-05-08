 /* This file is part of Schematica.
  * Copyright (C) 2013 metalhedd <https://github.com/andrepl/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Schematica.  If not, see <http://www.gnu.org/licenses/>.
  *
  * This code is based heavily on WorldEdit by sk89q <http://www.sk89q.com>
  */
 package com.norcode.bukkit.schematica;
 
 import net.minecraft.server.v1_5_R3.*;
 
 import java.util.HashMap;
 
 public class MaterialID {
     public static final int AIR = 0;
     public static final int STONE = 1;
     public static final int GRASS = 2;
     public static final int DIRT = 3;
     public static final int COBBLESTONE = 4;
     public static final int WOOD = 5;
     public static final int SAPLING = 6;
     public static final int BEDROCK = 7;
     public static final int WATER = 8;
     public static final int STATIONARY_WATER = 9;
     public static final int LAVA = 10;
     public static final int STATIONARY_LAVA = 11;
     public static final int SAND = 12;
     public static final int GRAVEL = 13;
     public static final int GOLD_ORE = 14;
     public static final int IRON_ORE = 15;
     public static final int COAL_ORE = 16;
     public static final int LOG = 17;
     public static final int LEAVES = 18;
     public static final int SPONGE = 19;
     public static final int GLASS = 20;
     public static final int LAPIS_ORE = 21;
     public static final int LAPIS_BLOCK = 22;
     public static final int DISPENSER = 23;
     public static final int SANDSTONE = 24;
     public static final int NOTE_BLOCK = 25;
     public static final int BED_BLOCK = 26;
     public static final int POWERED_RAIL = 27;
     public static final int DETECTOR_RAIL = 28;
     public static final int PISTON_STICKY_BASE = 29;
     public static final int WEB = 30;
     public static final int LONG_GRASS = 31;
     public static final int DEAD_BUSH = 32;
     public static final int PISTON_BASE = 33;
     public static final int PISTON_EXTENSION = 34;
     public static final int WOOL = 35;
     public static final int PISTON_MOVING_PIECE = 36;
     public static final int YELLOW_FLOWER = 37;
     public static final int RED_ROSE = 38;
     public static final int BROWN_MUSHROOM = 39;
     public static final int RED_MUSHROOM = 40;
     public static final int GOLD_BLOCK = 41;
     public static final int IRON_BLOCK = 42;
     public static final int DOUBLE_STEP = 43;
     public static final int STEP = 44;
     public static final int BRICK = 45;
     public static final int TNT = 46;
     public static final int BOOKSHELF = 47;
     public static final int MOSSY_COBBLESTONE = 48;
     public static final int OBSIDIAN = 49;
     public static final int TORCH = 50;
     public static final int FIRE = 51;
     public static final int MOB_SPAWNER = 52;
     public static final int WOOD_STAIRS = 53;
     public static final int CHEST = 54;
     public static final int REDSTONE_WIRE = 55;
     public static final int DIAMOND_ORE = 56;
     public static final int DIAMOND_BLOCK = 57;
     public static final int WORKBENCH = 58;
     public static final int CROPS = 59;
     public static final int SOIL = 60;
     public static final int FURNACE = 61;
     public static final int BURNING_FURNACE = 62;
     public static final int SIGN_POST = 63;
     public static final int WOODEN_DOOR = 64;
     public static final int LADDER = 65;
     public static final int RAILS = 66;
     public static final int COBBLESTONE_STAIRS = 67;
     public static final int WALL_SIGN = 68;
     public static final int LEVER = 69;
     public static final int STONE_PLATE = 70;
     public static final int IRON_DOOR_BLOCK = 71;
     public static final int WOOD_PLATE = 72;
     public static final int REDSTONE_ORE = 73;
     public static final int GLOWING_REDSTONE_ORE = 74;
     public static final int REDSTONE_TORCH_OFF = 75;
     public static final int REDSTONE_TORCH_ON = 76;
     public static final int STONE_BUTTON = 77;
     public static final int SNOW = 78;
     public static final int ICE = 79;
     public static final int SNOW_BLOCK = 80;
     public static final int CACTUS = 81;
     public static final int CLAY = 82;
     public static final int SUGAR_CANE_BLOCK = 83;
     public static final int JUKEBOX = 84;
     public static final int FENCE = 85;
     public static final int PUMPKIN = 86;
     public static final int NETHERRACK = 87;
     public static final int SOUL_SAND = 88;
     public static final int GLOWSTONE = 89;
     public static final int PORTAL = 90;
     public static final int JACK_O_LANTERN = 91;
     public static final int CAKE_BLOCK = 92;
     public static final int DIODE_BLOCK_OFF = 93;
     public static final int DIODE_BLOCK_ON = 94;
     public static final int LOCKED_CHEST = 95;
     public static final int TRAP_DOOR = 96;
     public static final int MONSTER_EGGS = 97;
     public static final int SMOOTH_BRICK = 98;
     public static final int HUGE_MUSHROOM_1 = 99;
     public static final int HUGE_MUSHROOM_2 = 100;
     public static final int IRON_FENCE = 101;
     public static final int THIN_GLASS = 102;
     public static final int MELON_BLOCK = 103;
     public static final int PUMPKIN_STEM = 104;
     public static final int MELON_STEM = 105;
     public static final int VINE = 106;
     public static final int FENCE_GATE = 107;
     public static final int BRICK_STAIRS = 108;
     public static final int SMOOTH_STAIRS = 109;
     public static final int MYCEL = 110;
     public static final int WATER_LILY = 111;
     public static final int NETHER_BRICK = 112;
     public static final int NETHER_FENCE = 113;
     public static final int NETHER_BRICK_STAIRS = 114;
     public static final int NETHER_WARTS = 115;
     public static final int ENCHANTMENT_TABLE = 116;
     public static final int BREWING_STAND = 117;
     public static final int CAULDRON = 118;
     public static final int ENDER_PORTAL = 119;
     public static final int ENDER_PORTAL_FRAME = 120;
     public static final int ENDER_STONE = 121;
     public static final int DRAGON_EGG = 122;
     public static final int REDSTONE_LAMP_OFF = 123;
     public static final int REDSTONE_LAMP_ON = 124;
     public static final int WOOD_DOUBLE_STEP = 125;
     public static final int WOOD_STEP = 126;
     public static final int COCOA = 127;
     public static final int SANDSTONE_STAIRS = 128;
     public static final int EMERALD_ORE = 129;
     public static final int ENDER_CHEST = 130;
     public static final int TRIPWIRE_HOOK = 131;
     public static final int TRIPWIRE = 132;
     public static final int EMERALD_BLOCK = 133;
     public static final int SPRUCE_WOOD_STAIRS = 134;
     public static final int BIRCH_WOOD_STAIRS = 135;
     public static final int JUNGLE_WOOD_STAIRS = 136;
     public static final int COMMAND = 137;
     public static final int BEACON = 138;
     public static final int COBBLE_WALL = 139;
     public static final int FLOWER_POT = 140;
     public static final int CARROT = 141;
     public static final int POTATO = 142;
     public static final int WOOD_BUTTON = 143;
     public static final int SKULL = 144;
     public static final int ANVIL = 145;
     public static final int TRAPPED_CHEST = 146;
     public static final int GOLD_PLATE = 147;
     public static final int IRON_PLATE = 148;
     public static final int REDSTONE_COMPARATOR_OFF = 149;
     public static final int REDSTONE_COMPARATOR_ON = 150;
     public static final int DAYLIGHT_DETECTOR = 151;
     public static final int REDSTONE_BLOCK = 152;
     public static final int QUARTZ_ORE = 153;
     public static final int HOPPER = 154;
     public static final int QUARTZ_BLOCK = 155;
     public static final int QUARTZ_STAIRS = 156;
     public static final int ACTIVATOR_RAIL = 157;
     public static final int DROPPER = 158;
     public static final int IRON_SPADE = 256;
     public static final int IRON_PICKAXE = 257;
     public static final int IRON_AXE = 258;
     public static final int FLINT_AND_STEEL = 259;
     public static final int APPLE = 260;
     public static final int BOW = 261;
     public static final int ARROW = 262;
     public static final int COAL = 263;
     public static final int DIAMOND = 264;
     public static final int IRON_INGOT = 265;
     public static final int GOLD_INGOT = 266;
     public static final int IRON_SWORD = 267;
     public static final int WOOD_SWORD = 268;
     public static final int WOOD_SPADE = 269;
     public static final int WOOD_PICKAXE = 270;
     public static final int WOOD_AXE = 271;
     public static final int STONE_SWORD = 272;
     public static final int STONE_SPADE = 273;
     public static final int STONE_PICKAXE = 274;
     public static final int STONE_AXE = 275;
     public static final int DIAMOND_SWORD = 276;
     public static final int DIAMOND_SPADE = 277;
     public static final int DIAMOND_PICKAXE = 278;
     public static final int DIAMOND_AXE = 279;
     public static final int STICK = 280;
     public static final int BOWL = 281;
     public static final int MUSHROOM_SOUP = 282;
     public static final int GOLD_SWORD = 283;
     public static final int GOLD_SPADE = 284;
     public static final int GOLD_PICKAXE = 285;
     public static final int GOLD_AXE = 286;
     public static final int STRING = 287;
     public static final int FEATHER = 288;
     public static final int SULPHUR = 289;
     public static final int WOOD_HOE = 290;
     public static final int STONE_HOE = 291;
     public static final int IRON_HOE = 292;
     public static final int DIAMOND_HOE = 293;
     public static final int GOLD_HOE = 294;
     public static final int SEEDS = 295;
     public static final int WHEAT = 296;
     public static final int BREAD = 297;
     public static final int LEATHER_HELMET = 298;
     public static final int LEATHER_CHESTPLATE = 299;
     public static final int LEATHER_LEGGINGS = 300;
     public static final int LEATHER_BOOTS = 301;
     public static final int CHAINMAIL_HELMET = 302;
     public static final int CHAINMAIL_CHESTPLATE = 303;
     public static final int CHAINMAIL_LEGGINGS = 304;
     public static final int CHAINMAIL_BOOTS = 305;
     public static final int IRON_HELMET = 306;
     public static final int IRON_CHESTPLATE = 307;
     public static final int IRON_LEGGINGS = 308;
     public static final int IRON_BOOTS = 309;
     public static final int DIAMOND_HELMET = 310;
     public static final int DIAMOND_CHESTPLATE = 311;
     public static final int DIAMOND_LEGGINGS = 312;
     public static final int DIAMOND_BOOTS = 313;
     public static final int GOLD_HELMET = 314;
     public static final int GOLD_CHESTPLATE = 315;
     public static final int GOLD_LEGGINGS = 316;
     public static final int GOLD_BOOTS = 317;
     public static final int FLINT = 318;
     public static final int PORK = 319;
     public static final int GRILLED_PORK = 320;
     public static final int PAINTING = 321;
     public static final int GOLDEN_APPLE = 322;
     public static final int SIGN = 323;
     public static final int WOOD_DOOR = 324;
     public static final int BUCKET = 325;
     public static final int WATER_BUCKET = 326;
     public static final int LAVA_BUCKET = 327;
     public static final int MINECART = 328;
     public static final int SADDLE = 329;
     public static final int IRON_DOOR = 330;
     public static final int REDSTONE = 331;
     public static final int SNOW_BALL = 332;
     public static final int BOAT = 333;
     public static final int LEATHER = 334;
     public static final int MILK_BUCKET = 335;
     public static final int CLAY_BRICK = 336;
     public static final int CLAY_BALL = 337;
     public static final int SUGAR_CANE = 338;
     public static final int PAPER = 339;
     public static final int BOOK = 340;
     public static final int SLIME_BALL = 341;
     public static final int STORAGE_MINECART = 342;
     public static final int POWERED_MINECART = 343;
     public static final int EGG = 344;
     public static final int COMPASS = 345;
     public static final int FISHING_ROD = 346;
     public static final int WATCH = 347;
     public static final int GLOWSTONE_DUST = 348;
     public static final int RAW_FISH = 349;
     public static final int COOKED_FISH = 350;
     public static final int INK_SACK = 351;
     public static final int BONE = 352;
     public static final int SUGAR = 353;
     public static final int CAKE = 354;
     public static final int BED = 355;
     public static final int DIODE = 356;
     public static final int COOKIE = 357;
     public static final int MAP = 358;
     public static final int SHEARS = 359;
     public static final int MELON = 360;
     public static final int PUMPKIN_SEEDS = 361;
     public static final int MELON_SEEDS = 362;
     public static final int RAW_BEEF = 363;
     public static final int COOKED_BEEF = 364;
     public static final int RAW_CHICKEN = 365;
     public static final int COOKED_CHICKEN = 366;
     public static final int ROTTEN_FLESH = 367;
     public static final int ENDER_PEARL = 368;
     public static final int BLAZE_ROD = 369;
     public static final int GHAST_TEAR = 370;
     public static final int GOLD_NUGGET = 371;
     public static final int NETHER_STALK = 372;
     public static final int POTION = 373;
     public static final int GLASS_BOTTLE = 374;
     public static final int SPIDER_EYE = 375;
     public static final int FERMENTED_SPIDER_EYE = 376;
     public static final int BLAZE_POWDER = 377;
     public static final int MAGMA_CREAM = 378;
     public static final int BREWING_STAND_ITEM = 379;
     public static final int CAULDRON_ITEM = 380;
     public static final int EYE_OF_ENDER = 381;
     public static final int SPECKLED_MELON = 382;
     public static final int MONSTER_EGG = 383;
     public static final int EXP_BOTTLE = 384;
     public static final int FIREBALL = 385;
     public static final int BOOK_AND_QUILL = 386;
     public static final int WRITTEN_BOOK = 387;
     public static final int EMERALD = 388;
     public static final int ITEM_FRAME = 389;
     public static final int FLOWER_POT_ITEM = 390;
     public static final int CARROT_ITEM = 391;
     public static final int POTATO_ITEM = 392;
     public static final int BAKED_POTATO = 393;
     public static final int POISONOUS_POTATO = 394;
     public static final int EMPTY_MAP = 395;
     public static final int GOLDEN_CARROT = 396;
     public static final int SKULL_ITEM = 397;
     public static final int CARROT_STICK = 398;
     public static final int NETHER_STAR = 399;
     public static final int PUMPKIN_PIE = 400;
     public static final int FIREWORK = 401;
     public static final int FIREWORK_CHARGE = 402;
     public static final int ENCHANTED_BOOK = 403;
     public static final int REDSTONE_COMPARATOR = 404;
     public static final int NETHER_BRICK_ITEM = 405;
     public static final int QUARTZ = 406;
     public static final int EXPLOSIVE_MINECART = 407;
     public static final int HOPPER_MINECART = 408;
     public static final int GOLD_RECORD = 2256;
     public static final int GREEN_RECORD = 2257;
     public static final int RECORD_3 = 2258;
     public static final int RECORD_4 = 2259;
     public static final int RECORD_5 = 2260;
     public static final int RECORD_6 = 2261;
     public static final int RECORD_7 = 2262;
     public static final int RECORD_8 = 2263;
     public static final int RECORD_9 = 2264;
     public static final int RECORD_10 = 2265;
     public static final int RECORD_11 = 2266;
     public static final int RECORD_12 = 2267;
 
     public static final HashMap<Integer, Class<? extends TileEntity>> tileEntityTypes = new HashMap<Integer, Class<? extends TileEntity>>();
     public static final HashMap<Class<? extends TileEntity>, String> tileEntityIds = new HashMap<Class<? extends TileEntity>, String>();
     static {
         tileEntityTypes.put(MaterialID.FURNACE, TileEntityFurnace.class);
         tileEntityTypes.put(MaterialID.BURNING_FURNACE, TileEntityFurnace.class);
         tileEntityTypes.put(MaterialID.CHEST, TileEntityChest.class);
         tileEntityTypes.put(MaterialID.TRAPPED_CHEST, TileEntityChest.class);
         tileEntityTypes.put(MaterialID.ENDER_CHEST, TileEntityEnderChest.class);
         tileEntityTypes.put(MaterialID.JUKEBOX, TileEntityRecordPlayer.class);
         tileEntityTypes.put(MaterialID.DISPENSER, TileEntityDispenser.class);
         tileEntityTypes.put(MaterialID.DROPPER, TileEntityDropper.class);
         tileEntityTypes.put(MaterialID.WALL_SIGN, TileEntitySign.class);
         tileEntityTypes.put(MaterialID.SIGN_POST, TileEntitySign.class);
         tileEntityTypes.put(MaterialID.MOB_SPAWNER, TileEntityMobSpawner.class);
         tileEntityTypes.put(MaterialID.NOTE_BLOCK, TileEntityNote.class);
         tileEntityTypes.put(MaterialID.PISTON_BASE, TileEntityPiston.class);
         tileEntityTypes.put(MaterialID.PISTON_STICKY_BASE, TileEntity.class);
         tileEntityTypes.put(MaterialID.BREWING_STAND, TileEntityBrewingStand.class);
         tileEntityTypes.put(MaterialID.ENCHANTMENT_TABLE, TileEntityEnchantTable.class);
         tileEntityTypes.put(MaterialID.ENDER_PORTAL, TileEntityEnderPortal.class);
         tileEntityTypes.put(MaterialID.COMMAND, TileEntityCommand.class);
         tileEntityTypes.put(MaterialID.BEACON, TileEntityBeacon.class);
         tileEntityTypes.put(MaterialID.SKULL, TileEntitySkull.class);
         tileEntityTypes.put(MaterialID.DAYLIGHT_DETECTOR, TileEntityLightDetector.class);
         tileEntityTypes.put(MaterialID.HOPPER, TileEntityHopper.class);
         tileEntityTypes.put(MaterialID.REDSTONE_COMPARATOR_ON, TileEntityComparator.class);
         tileEntityTypes.put(MaterialID.REDSTONE_COMPARATOR_OFF, TileEntityComparator.class);
         tileEntityIds.put(TileEntityFurnace.class, "Furnace");
         tileEntityIds.put(TileEntityChest.class, "Chest");
         tileEntityIds.put(TileEntityEnderChest.class, "EnderChest");
         tileEntityIds.put(TileEntityRecordPlayer.class, "RecordPlayer");
         tileEntityIds.put(TileEntityDispenser.class, "Trap");
         tileEntityIds.put(TileEntityDropper.class, "Dropper");
         tileEntityIds.put(TileEntitySign.class, "Sign");
         tileEntityIds.put(TileEntityMobSpawner.class, "MobSpawner");
         tileEntityIds.put(TileEntityNote.class, "Music");
         tileEntityIds.put(TileEntityPiston.class, "Piston");
         tileEntityIds.put(TileEntityBrewingStand.class, "Cauldron");
         tileEntityIds.put(TileEntityEnchantTable.class, "EnchantTable");
         tileEntityIds.put(TileEntityEnderPortal.class, "Airportal");
         tileEntityIds.put(TileEntityCommand.class, "Control");
         tileEntityIds.put(TileEntityBeacon.class, "Beacon");
         tileEntityIds.put(TileEntitySkull.class, "Skull");
         tileEntityIds.put(TileEntityLightDetector.class, "DLDetector");
         tileEntityIds.put(TileEntityHopper.class, "Hopper");
         tileEntityIds.put(TileEntityComparator.class, "Comparator");
     }
     
 
     public static boolean isTileEntityBlock(int typeId) {
         return tileEntityTypes.containsKey(typeId);
     }
 
     public static Class<? extends TileEntity> getTileEntityClass(int typeId) {
         return tileEntityTypes.get(typeId);
     }
 
     public static String getTileEntityId(int typeId) {
         Class<? extends TileEntity> tec = tileEntityTypes.get(typeId);
         return getTileEntityId(tec);
     }
 
     public static String getTileEntityId(Class<? extends TileEntity> cls) {
         return tileEntityIds.get(cls);
     }
     public static int rotate90(int type, int data) {
         switch (type) {
         case TORCH:
         case REDSTONE_TORCH_OFF:
         case REDSTONE_TORCH_ON:
             switch (data) {
             case 1: return 3;
             case 2: return 4;
             case 3: return 2;
             case 4: return 1;
             }
             break;
 
         case RAILS:
             switch (data) {
             case 6: return 7;
             case 7: return 8;
             case 8: return 9;
             case 9: return 6;
             }
             /* FALL-THROUGH */
 
         case POWERED_RAIL:
         case DETECTOR_RAIL:
         case ACTIVATOR_RAIL:
             switch (data & 0x7) {
             case 0: return 1 | (data & ~0x7);
             case 1: return 0 | (data & ~0x7);
             case 2: return 5 | (data & ~0x7);
             case 3: return 4 | (data & ~0x7);
             case 4: return 2 | (data & ~0x7);
             case 5: return 3 | (data & ~0x7);
             }
             break;
 
         case WOOD_STAIRS:
         case COBBLESTONE_STAIRS:
         case BRICK_STAIRS:
         case SMOOTH_STAIRS:
         case NETHER_BRICK_STAIRS:
         case SANDSTONE_STAIRS:
         case SPRUCE_WOOD_STAIRS:
         case BIRCH_WOOD_STAIRS:
         case JUNGLE_WOOD_STAIRS:
         case QUARTZ_STAIRS:
             switch (data) {
             case 0: return 2;
             case 1: return 3;
             case 2: return 1;
             case 3: return 0;
             case 4: return 6;
             case 5: return 7;
             case 6: return 5;
             case 7: return 4;
             }
             break;
 
         case LEVER:
         case STONE_BUTTON:
         case WOOD_BUTTON:
             int thrown = data & 0x8;
             int withoutThrown = data & ~0x8;
             switch (withoutThrown) {
             case 1: return 3 | thrown;
             case 2: return 4 | thrown;
             case 3: return 2 | thrown;
             case 4: return 1 | thrown;
             case 5: return 6 | thrown;
             case 6: return 5 | thrown;
             case 7: return 0 | thrown;
             case 0: return 7 | thrown;
             }
             break;
 
         case WOODEN_DOOR:
        case IRON_DOOR:
             if ((data & 0x8) == 8) {
                 return data;
             }
             int doorExtra = data & ~0x3;
             int doorWithoutFlags = data & 0x3;
             switch (doorWithoutFlags) {
             case 0: return 1 | doorExtra;
             case 1: return 2 | doorExtra;
             case 2: return 3 | doorExtra;
             case 3: return 0 | doorExtra;
             }
             break;
 
         case COCOA:
         case TRIPWIRE_HOOK:
             int extra = data & ~0x3;
             int withoutFlags = data & 0x3;
             switch (withoutFlags) {
             case 0: return 1 | extra;
             case 1: return 2 | extra;
             case 2: return 3 | extra;
             case 3: return 0 | extra;
             }
             break;
 
         case SIGN_POST:
             return (data + 4) % 16;
 
         case LADDER:
         case WALL_SIGN:
         case CHEST:
         case FURNACE:
         case BURNING_FURNACE:
         case ENDER_CHEST:
         case TRAPPED_CHEST:
         case HOPPER:
             switch (data) {
             case 2: return 5;
             case 3: return 4;
             case 4: return 2;
             case 5: return 3;
             }
             break;
 
         case DISPENSER:
         case DROPPER:
             int dispPower = data & 0x8;
             switch (data & ~0x8) {
             case 2: return 5 | dispPower;
             case 3: return 4 | dispPower;
             case 4: return 2 | dispPower;
             case 5: return 3 | dispPower;
             }
             break;
 
         case PUMPKIN:
         case JACK_O_LANTERN:
             switch (data) {
             case 0: return 1;
             case 1: return 2;
             case 2: return 3;
             case 3: return 0;
             }
             break;
 
         case LOG:
             if (data >= 4 && data <= 11) data ^= 0xc;
             break;
 
         case REDSTONE_COMPARATOR_OFF:
         case REDSTONE_COMPARATOR_ON:
         case DIODE_BLOCK_OFF:
         case DIODE_BLOCK_ON:
             int dir = data & 0x03;
             int delay = data - dir;
             switch (dir) {
             case 0: return 1 | delay;
             case 1: return 2 | delay;
             case 2: return 3 | delay;
             case 3: return 0 | delay;
             }
             break;
 
         case TRAP_DOOR:
             int withoutOrientation = data & ~0x3;
             int orientation = data & 0x3;
             switch (orientation) {
             case 0: return 3 | withoutOrientation;
             case 1: return 2 | withoutOrientation;
             case 2: return 0 | withoutOrientation;
             case 3: return 1 | withoutOrientation;
             }
             break;
 
         case PISTON_BASE:
         case PISTON_STICKY_BASE:
         case PISTON_EXTENSION:
             final int rest = data & ~0x7;
             switch (data & 0x7) {
             case 2: return 5 | rest;
             case 3: return 4 | rest;
             case 4: return 2 | rest;
             case 5: return 3 | rest;
             }
             break;
 
         case HUGE_MUSHROOM_1:
         case HUGE_MUSHROOM_2:
             if (data >= 10) return data;
             return (data * 3) % 10;
 
         case VINE:
             return ((data << 1) | (data >> 3)) & 0xf;
 
         case FENCE_GATE:
             return ((data + 1) & 0x3) | (data & ~0x3);
 
         case ANVIL:
             return data ^ 0x1;
 
         case BED_BLOCK:
             if ((data & 7) < 4) {
                 return data & ~3 | (data+1) & 3;
             }
             break;
 
         case SKULL:
             switch (data) {
             case 2: return 5;
             case 3: return 4;
             case 4: return 2;
             case 5: return 3;
             }
         }
 
         return data;
     }
 
     public static int rotate90Reverse(int type, int data) {
         switch (type) {
         case TORCH:
         case REDSTONE_TORCH_OFF:
         case REDSTONE_TORCH_ON:
             switch (data) {
             case 3: return 1;
             case 4: return 2;
             case 2: return 3;
             case 1: return 4;
             }
             break;
 
         case RAILS:
             switch (data) {
             case 7: return 6;
             case 8: return 7;
             case 9: return 8;
             case 6: return 9;
             }
             /* FALL-THROUGH */
 
         case POWERED_RAIL:
         case DETECTOR_RAIL:
         case ACTIVATOR_RAIL:
             int power = data & ~0x7;
             switch (data & 0x7) {
             case 1: return 0 | power;
             case 0: return 1 | power;
             case 5: return 2 | power;
             case 4: return 3 | power;
             case 2: return 4 | power;
             case 3: return 5 | power;
             }
             break;
 
         case WOOD_STAIRS:
         case COBBLESTONE_STAIRS:
         case BRICK_STAIRS:
         case SMOOTH_STAIRS:
         case NETHER_BRICK_STAIRS:
         case SANDSTONE_STAIRS:
         case SPRUCE_WOOD_STAIRS:
         case BIRCH_WOOD_STAIRS:
         case JUNGLE_WOOD_STAIRS:
         case QUARTZ_STAIRS:
             switch (data) {
             case 2: return 0;
             case 3: return 1;
             case 1: return 2;
             case 0: return 3;
             case 6: return 4;
             case 7: return 5;
             case 5: return 6;
             case 4: return 7;
             }
             break;
 
         case LEVER:
         case STONE_BUTTON:
         case WOOD_BUTTON:
             int thrown = data & 0x8;
             int withoutThrown = data & ~0x8;
             switch (withoutThrown) {
             case 3: return 1 | thrown;
             case 4: return 2 | thrown;
             case 2: return 3 | thrown;
             case 1: return 4 | thrown;
             case 5: return 6 | thrown;
             case 6: return 5 | thrown;
             case 7: return 0 | thrown;
             case 0: return 7 | thrown;
             }
             break;
 
         case WOODEN_DOOR:
        case IRON_DOOR:
             if ((data & 0x8) == 8) {
                 return data;
             }
             int doorExtra = data & ~0x3;
             int doorWithoutFlags = data & 0x3;
             switch (doorWithoutFlags) {
             case 1: return 0 | doorExtra;
             case 2: return 1 | doorExtra;
             case 3: return 2 | doorExtra;
             case 0: return 3 | doorExtra;
             }
             break;
 
         case COCOA:
         case TRIPWIRE_HOOK:
             int extra = data & ~0x3;
             int withoutFlags = data & 0x3;
             switch (withoutFlags) {
             case 1: return 0 | extra;
             case 2: return 1 | extra;
             case 3: return 2 | extra;
             case 0: return 3 | extra;
             }
             break;
 
         case SIGN_POST:
             return (data + 12) % 16;
 
         case LADDER:
         case WALL_SIGN:
         case CHEST:
         case FURNACE:
         case BURNING_FURNACE:
         case ENDER_CHEST:
         case TRAPPED_CHEST:
         case HOPPER:
             switch (data) {
             case 5: return 2;
             case 4: return 3;
             case 2: return 4;
             case 3: return 5;
             }
             break;
 
         case DISPENSER:
         case DROPPER:
             int dispPower = data & 0x8;
             switch (data & ~0x8) {
             case 5: return 2 | dispPower;
             case 4: return 3 | dispPower;
             case 2: return 4 | dispPower;
             case 3: return 5 | dispPower;
             }
             break;
         case PUMPKIN:
         case JACK_O_LANTERN:
             switch (data) {
             case 1: return 0;
             case 2: return 1;
             case 3: return 2;
             case 0: return 3;
             }
             break;
 
         case LOG:
             if (data >= 4 && data <= 11) data ^= 0xc;
             break;
 
         case REDSTONE_COMPARATOR_OFF:
         case REDSTONE_COMPARATOR_ON:
         case DIODE_BLOCK_OFF:
         case DIODE_BLOCK_ON:
             int dir = data & 0x03;
             int delay = data - dir;
             switch (dir) {
             case 1: return 0 | delay;
             case 2: return 1 | delay;
             case 3: return 2 | delay;
             case 0: return 3 | delay;
             }
             break;
 
         case TRAP_DOOR:
             int withoutOrientation = data & ~0x3;
             int orientation = data & 0x3;
             switch (orientation) {
             case 3: return 0 | withoutOrientation;
             case 2: return 1 | withoutOrientation;
             case 0: return 2 | withoutOrientation;
             case 1: return 3 | withoutOrientation;
             }
 
         case PISTON_BASE:
         case PISTON_STICKY_BASE:
         case PISTON_EXTENSION:
             final int rest = data & ~0x7;
             switch (data & 0x7) {
             case 5: return 2 | rest;
             case 4: return 3 | rest;
             case 2: return 4 | rest;
             case 3: return 5 | rest;
             }
             break;
 
         case HUGE_MUSHROOM_1:
         case HUGE_MUSHROOM_2:
             if (data >= 10) return data;
             return (data * 7) % 10;
 
         case VINE:
             return ((data >> 1) | (data << 3)) & 0xf;
 
         case FENCE_GATE:
             return ((data + 3) & 0x3) | (data & ~0x3);
 
         case ANVIL:
             return data ^ 0x1;
 
         case  BED_BLOCK:
             if ((data & 7) < 4) {
                 return data & ~3 | (data-1) & 3;
             }
             break;
 
         case SKULL:
             switch (data) {
             case 2: return 4;
             case 3: return 5;
             case 4: return 3;
             case 5: return 2;
             }
         }
 
         return data;
     }
 }
