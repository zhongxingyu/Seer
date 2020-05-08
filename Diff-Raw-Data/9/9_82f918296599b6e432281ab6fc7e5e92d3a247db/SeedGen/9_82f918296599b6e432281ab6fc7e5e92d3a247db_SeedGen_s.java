 /*
  * This file is part of FruitTrees.
  *
  * Copyright © 2013 Visual Illusions Entertainment
  *
  * FruitTrees is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * FruitTrees is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with FruitTrees.
  * If not, see http://www.gnu.org/licenses/gpl.html.
  */
 package net.visualillusionsent.minecraft.server.mod.canary.plugin.fruittrees;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import net.canarymod.Canary;
 import net.canarymod.api.inventory.Item;
 import net.canarymod.api.inventory.ItemType;
 import net.canarymod.api.inventory.recipes.CraftingRecipe;
 import net.canarymod.api.inventory.recipes.Recipe;
 import net.canarymod.api.inventory.recipes.RecipeRow;
 import net.canarymod.api.nbt.CompoundTag;
 import net.visualillusionsent.fruittrees.TreeType;
 
 final class SeedGen {
 
     final static Item[] seeds = new Item[127]; //This is set to 127 for quick expansion durring development
     final static ArrayList<Recipe> recipes = new ArrayList<Recipe>(127);
     private final static String[] dyes = new String[] { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "Light_Gray", "Gray", "Pink", "Lime", "Yellow", "Light_Blue", "Magenta", "Orange", "White" };
 
     private SeedGen() {}
 
     static final void clearSeeds() {
         Arrays.fill(seeds, null);
         for (Recipe recipe : recipes) {
             if (recipe != null) {
                 Canary.getServer().removeRecipe(recipe);
             }
         }
         recipes.clear();
     }
 
     static final void genAll() {
         // Gen Seeds
         genAppleSeeds();
         genGoldAppleSeeds();
         genSpongeSeeds();
         genRecordSeeds();
         genDyeSeeds();
         genRedstoneSeeds();
         genIronSeeds();
         genGoldSeeds();
         genDiamondSeeds();
         genEmeraldSeeds();
         genCoalSeeds();
 
         //Gen Recipes
         addAppleSeedsRecipe();
         addGoldenAppleSeedsRecipe0();
         addGoldenAppleSeedsRecipe1();
         addSpongeSeedRecipe0();
         addSpongeSeedRecipe1();
         addRecordSeedRecipe();
         addDyeSeedsRecipe();
         addRedstoneSeedsRecipe();
         addIronSeedsRecipe();
         addGoldSeedsRecipe();
         addDiamondSeedsRecipe();
         addEmeraldSeedsRecipe();
         addCoalSeedsRecipe();
     }
 
     private static final void genAppleSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.APPLE)) {
             genSeeds("Apple", ItemType.MelonSeeds, 0, (byte) -1);
         }
     }
 
     private static final void genGoldAppleSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.GOLDEN_APPLE)) {
             genSeeds("Golden Apple", ItemType.MelonSeeds, 1, (byte) -1);
         }
     }
 
     private static final void genSpongeSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.SPONGE)) {
             genSeeds("Sponge", ItemType.PumpkinSeeds, 2, (byte) -1);
         }
     }
 
     private static final void genRecordSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.RECORD)) {
             genSeeds("Record", ItemType.PumpkinSeeds, 3, (byte) -1);
         }
     }
 
     private static final void genDyeSeeds() {
         for (int index = 0; index <= 15; index++) {
             if (CanaryFruitTrees.instance().checkEnabled(TreeType.valueOf("DYE_".concat(dyes[index].toUpperCase())))) {
                 genSeeds(dyes[index].concat(" Dye"), ItemType.Seeds, index + 4, (byte) index);
             }
         }
     }
 
     private static final void genRedstoneSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.REDSTONE)) {
             genSeeds("Redstone", ItemType.Seeds, 20, (byte) -1);
         }
     }
 
     private static final void genIronSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.IRON)) {
             genSeeds("Iron", ItemType.Seeds, 21, (byte) -1);
         }
     }
 
     private static final void genGoldSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.GOLD)) {
             genSeeds("Gold", ItemType.Seeds, 22, (byte) -1);
         }
     }
 
     private static final void genDiamondSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.GOLD)) {
             genSeeds("Diamond", ItemType.Seeds, 23, (byte) -1);
         }
     }
 
     private static final void genEmeraldSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.EMERALD)) {
             genSeeds("Emerald", ItemType.Seeds, 24, (byte) -1);
         }
     }
 
     private static final void genCoalSeeds() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.COAL)) {
             genSeeds("Coal", ItemType.MelonSeeds, 25, (byte) -1);
         }
     }
 
     private static final void genSeeds(String name, ItemType type, int seed_index, byte dye) {
         Item fruit_seeds = Canary.factory().getItemFactory().newItem(type, 0, 1);
         fruit_seeds.setDisplayName(String.format("%s Seeds", name));
         fruit_seeds.setLore(String.format("Plant to grow a %s Tree", name));
         CompoundTag fruit_trees_tag = Canary.factory().getNBTFactory().newCompoundTag("FruitTrees");
         fruit_trees_tag.put("TreeType", name.concat("Seeds").replace(" ", ""));
         if (dye != -1) {
             fruit_trees_tag.put("DyeColor", dye);
         }
         fruit_seeds.getMetaTag().put("FruitTrees", fruit_trees_tag);
         seeds[seed_index] = fruit_seeds;
     }
 
     private static final void addAppleSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.APPLE)) {
             Item apple = Canary.factory().getItemFactory().newItem(ItemType.Apple, 0, 1);
             Item aplSeeds = seeds[0].clone();
             aplSeeds.setAmount(3);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(aplSeeds, apple)));
         }
     }
 
     private static final void addGoldenAppleSeedsRecipe0() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.GOLDEN_APPLE)) {
             Item golden_apple = Canary.factory().getItemFactory().newItem(ItemType.GoldenApple, 0, 1);
             Item gld_apl_seeds = seeds[1].clone();
             gld_apl_seeds.setAmount(3);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(gld_apl_seeds, golden_apple)));
         }
     }
 
     private static final void addGoldenAppleSeedsRecipe1() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.GOLDEN_APPLE)) {
             Item gld_apl_seeds = seeds[1].clone();
             Item gold_nuggets = Canary.factory().getItemFactory().newItem(ItemType.GoldNugget, 0, 1);
             RecipeRow[] rows = new RecipeRow[] { new RecipeRow("NNN", new Item[] { gold_nuggets }),
                 new RecipeRow("NSN", new Item[] { gold_nuggets, seeds[0].clone() }),
                 new RecipeRow("NNN", new Item[] { gold_nuggets })
             };
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(gld_apl_seeds, rows)));
         }
     }
 
     private static final void addSpongeSeedRecipe0() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.SPONGE)) {
             Item sponge_seeds = seeds[2].clone();
             sponge_seeds.setAmount(2);
            Item yellow_wool = Canary.factory().getItemFactory().newItem(ItemType.Cloth, 4, 1);
             Item clay_ball = Canary.factory().getItemFactory().newItem(ItemType.ClayBall, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(sponge_seeds, yellow_wool, clay_ball)));
         }
     }
 
     private static final void addSpongeSeedRecipe1() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.SPONGE)) {
             Item sponge_seeds = seeds[2].clone();
             sponge_seeds.setAmount(3);
             Item sponge = Canary.factory().getItemFactory().newItem(ItemType.Sponge, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(sponge_seeds, sponge)));
         }
     }
 
     private static final void addRecordSeedRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.RECORD)) {
             Item record_seeds = seeds[3].clone();
             record_seeds.setAmount(1);
             Item record = Canary.factory().getItemFactory().newItem(ItemType.GoldRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.GreenRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.BlocksRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.ChirpRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.FarRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.MallRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.MellohiRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.StalRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.StradRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.WardRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.ElevenRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
             record = Canary.factory().getItemFactory().newItem(ItemType.WaitRecord, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(record_seeds.clone(), record)));
         }
     }
 
     private static final void addDyeSeedsRecipe() {
         for (int index = 0; index <= 15; index++) {
             if (CanaryFruitTrees.instance().checkEnabled(TreeType.valueOf("DYE_".concat(dyes[index].toUpperCase())))) {
                 Item dye_seeds = seeds[index + 4].clone();
                 dye_seeds.setAmount(2);
                 Item seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
                 Item dye = Canary.factory().getItemFactory().newItem(ItemType.InkSack, index, 1);
                 recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(dye_seeds.clone(), seeds, dye)));
             }
         }
     }
 
     private static final void addRedstoneSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.REDSTONE)) {
             Item redstone_seeds = seeds[20].clone();
             redstone_seeds.setAmount(2);
             Item redstone_dust = Canary.factory().getItemFactory().newItem(ItemType.RedStone, 0, 1);
             Item normal_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(redstone_seeds, redstone_dust, normal_seeds)));
         }
     }
 
     private static final void addIronSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.IRON)) {
             Item iron_seeds = seeds[21].clone();
             Item iron_ingot = Canary.factory().getItemFactory().newItem(ItemType.IronIngot, 0, 1);
             Item normal_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
             RecipeRow[] rows = new RecipeRow[] { new RecipeRow("III", new Item[] { iron_ingot }),
                 new RecipeRow("ISI", new Item[] { iron_ingot, normal_seeds }),
                 new RecipeRow("III", new Item[] { iron_ingot })
             };
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(iron_seeds, rows)));
         }
     }
 
     private static final void addGoldSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.GOLD)) {
             Item gold_seeds = seeds[22].clone();
             Item gold_ingot = Canary.factory().getItemFactory().newItem(ItemType.GoldIngot, 0, 1);
             Item normal_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
             RecipeRow[] rows = new RecipeRow[] { new RecipeRow("GGG", new Item[] { gold_ingot }),
                 new RecipeRow("GSG", new Item[] { gold_ingot, normal_seeds }),
                 new RecipeRow("GGG", new Item[] { gold_ingot })
             };
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(gold_seeds, rows)));
         }
     }
 
     private static final void addDiamondSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.DIAMOND)) {
             Item diamond_seeds = seeds[23].clone();
             Item diamond_gem = Canary.factory().getItemFactory().newItem(ItemType.Diamond, 0, 1);
             Item normal_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
             RecipeRow[] rows = new RecipeRow[] { new RecipeRow("DDD", new Item[] { diamond_gem }),
                 new RecipeRow("DSD", new Item[] { diamond_gem, normal_seeds }),
                 new RecipeRow("DDD", new Item[] { diamond_gem })
             };
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(diamond_seeds, rows)));
         }
     }
 
     private static final void addEmeraldSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.EMERALD)) {
             Item emerald_seeds = seeds[24].clone();
             Item emerald_gem = Canary.factory().getItemFactory().newItem(ItemType.Emerald, 0, 1);
             Item normal_seeds = Canary.factory().getItemFactory().newItem(ItemType.Seeds, 0, 1);
             RecipeRow[] rows = new RecipeRow[] { new RecipeRow("EEE", new Item[] { emerald_gem }),
                 new RecipeRow("ESE", new Item[] { emerald_gem, normal_seeds }),
                 new RecipeRow("EEE", new Item[] { emerald_gem })
             };
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(emerald_seeds, rows)));
         }
     }
 
     private static final void addCoalSeedsRecipe() {
         if (CanaryFruitTrees.instance().checkEnabled(TreeType.COAL)) {
             Item coal = Canary.factory().getItemFactory().newItem(ItemType.Coal, 0, 1);
             Item coal_seeds = seeds[25].clone();
             coal_seeds.setAmount(2);
             recipes.add(Canary.getServer().addRecipe(new CraftingRecipe(coal_seeds, coal)));
         }
     }
 }
