 /**
  * Copyright (c) Scott Killen, 2012
  * 
  * This mod is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license
  * located in /MMPL-1.0.txt
  */
 
 package bunyan.config;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.ItemStack;
 import bunyan.Proxy;
 import bunyan.blocks.BunyanBlock;
 import bunyan.blocks.CustomLog;
 import bunyan.blocks.CustomWood;
 import bunyan.blocks.TurnableCustomLog;
 import bunyan.blocks.TurnableVanillaLog;
 import bunyan.blocks.WideLog;
 import bunyan.blocks.WideLogBarkOnBottom;
 import bunyan.blocks.WideLogBarkOnTop;
 
 public class ConfigureBlocks {
 
 	private static void addCustomLogNames() {
 		Proxy.addName(new ItemStack(BunyanBlock.wood, 1,
 				CustomLog.metaFir), "Fir Log");
 		Proxy.addName(new ItemStack(BunyanBlock.wood, 1,
 				CustomLog.metaAcacia), "Acacia Log");
 	}
 
 	private static void addDirectionalCustomLogNames() {
 		String name = Proxy.getObjectDisplayName(new ItemStack(
 				BunyanBlock.wood, 1, CustomLog.metaAcacia));
 		Proxy.addName(new ItemStack(BunyanBlock.turnableCustomWood, 1,
 				TurnableCustomLog.metaAcacia), name);
 		name = Proxy.getObjectDisplayName(new ItemStack(
 				BunyanBlock.wood, 1, CustomLog.metaFir));
 		Proxy.addName(new ItemStack(BunyanBlock.turnableCustomWood, 1,
 				TurnableCustomLog.metaFir), name);
 	}
 
 	private static void addDirectionalVanillaLogNames() {
 		String name = Proxy.getObjectDisplayName(new ItemStack(
 				Block.wood, 1, 0));
 		Proxy.addName(new ItemStack(BunyanBlock.turnableVanillaWood, 1,
 				TurnableVanillaLog.metaOak), name);
 		name = Proxy.getObjectDisplayName(new ItemStack(Block.wood, 1,
 				1));
 		Proxy.addName(new ItemStack(BunyanBlock.turnableVanillaWood, 1,
 				TurnableVanillaLog.metaPine), name);
 		name = Proxy.getObjectDisplayName(new ItemStack(Block.wood, 1,
 				2));
 		Proxy.addName(new ItemStack(BunyanBlock.turnableVanillaWood, 1,
 				TurnableVanillaLog.metaBirch), name);
 		name = Proxy.getObjectDisplayName(new ItemStack(Block.wood, 1,
 				3));
 		Proxy.addName(new ItemStack(BunyanBlock.turnableVanillaWood, 1,
 				TurnableVanillaLog.metaJungle), name);
 	}
 
 	public static void addNames() {
 		addCustomLogNames();
 		addWideLogNames();
 		addPlankNames();
 		addDirectionalVanillaLogNames();
 		addDirectionalCustomLogNames();
 	}
 
 	private static void addPlankNames() {
 		Proxy.addName(new ItemStack(BunyanBlock.planks, 1,
 				CustomWood.metaRedwood), "Redwood Planks");
 		Proxy.addName(new ItemStack(BunyanBlock.planks, 1,
 				CustomWood.metaFir), "Fir Planks");
 		Proxy.addName(new ItemStack(BunyanBlock.planks, 1,
 				CustomWood.metaAcacia), "Acacia Planks");
 	}
 
 	private static void addWideLogNames() {
 		Proxy.addName(new ItemStack(BunyanBlock.widewood, 1,
 				WideLog.metaRedwood), "Quarter Huge Redwood Log");
 		Proxy.addName(new ItemStack(BunyanBlock.widewood, 1,
 				WideLog.metaFir), "Quarter Huge Fir Log");
 		Proxy.addName(new ItemStack(BunyanBlock.widewood, 1,
 				WideLog.metaOak), "Quarter Huge Oak Log");
 		Proxy.addName(new ItemStack(BunyanBlock.widewoodBarkTop, 1,
 				WideLog.metaRedwood), "Quarter Huge Redwood Log");
 		Proxy.addName(new ItemStack(BunyanBlock.widewoodBarkTop, 1,
 				WideLog.metaFir), "Quarter Huge Fir Log");
 		Proxy.addName(new ItemStack(BunyanBlock.widewoodBarkTop, 1,
 				WideLog.metaOak), "Quarter Huge Oak Log");
 	}
 
 	public static void initialize() {
 		final int woodID = Config.getOrCreateBlockIdProperty("wood.id",
 				160);
 		BunyanBlock.wood = new CustomLog(woodID)
 				.setBlockName("bunyan.wood");
 
 		final int widewoodID = Config.getOrCreateBlockIdProperty(
 				"widewood.id", 161);
 		BunyanBlock.widewood = new WideLog(widewoodID)
 				.setBlockName("bunyan.widewood");
 
 		final int plankID = Config.getOrCreateBlockIdProperty(
 				"plank.id", 163);
 		BunyanBlock.planks = new CustomWood(plankID)
 				.setBlockName("bunyan.plank");
 
 		final int turnableVanillaWoodID = Config
 				.getOrCreateBlockIdProperty("vanilla.wood.alt.id", 164);
 		BunyanBlock.turnableVanillaWood = new TurnableVanillaLog(
 				turnableVanillaWoodID)
 				.setBlockName("bunyan.vanilla.wood");
 
 		final int turnableWoodID = Config.getOrCreateBlockIdProperty(
 				"turnable.wood.alt.id", 165);
 		BunyanBlock.turnableCustomWood = new TurnableCustomLog(
 				turnableWoodID).setBlockName("bunyan.wood.alt");
 
 		final int widewoodBarkTopID = Config
 				.getOrCreateBlockIdProperty("widewood.alt0.id", 166);
 		BunyanBlock.widewoodBarkTop = new WideLogBarkOnTop(
 				widewoodBarkTopID).setBlockName("bunyan.widewood.alt0");
 
 		final int widewoodBarkBottomID = Config
 				.getOrCreateBlockIdProperty("widewood.alt1.id", 167);
 		BunyanBlock.widewoodBarkBottom = new WideLogBarkOnBottom(
 				widewoodBarkBottomID)
 				.setBlockName("bunyan.widewood.alt1");
 
 		Proxy.registerBlock(BunyanBlock.wood,
 				bunyan.items.MultiItemBlock.class);
 		Proxy.registerBlock(BunyanBlock.widewood,
 				bunyan.items.MultiItemBlock.class);
 		Proxy.registerBlock(BunyanBlock.widewoodBarkTop,
 				bunyan.items.MultiItemBlock.class);
 		Proxy.registerBlock(BunyanBlock.widewoodBarkBottom,
 				bunyan.items.MultiItemBlock.class);
 		Proxy.registerBlock(BunyanBlock.planks,
 				bunyan.items.MultiItemBlock.class);
 		Proxy.registerBlock(BunyanBlock.turnableVanillaWood,
 				bunyan.items.MultiItemBlock.class);
 		Proxy.registerBlock(BunyanBlock.turnableCustomWood,
 				bunyan.items.MultiItemBlock.class);
 	}
 
 }
