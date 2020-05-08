 package tutorial.generic;
 
 // This Import list will grow longer with each additional tutorial.
 // It's not pruned between full class postings, unlike other tutorial code.
 import java.util.ArrayList;
 
 import thaumcraft.api.EnumTag;
 import thaumcraft.api.ObjectTags;
 import thaumcraft.api.ThaumcraftApiHelper;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.ItemTool;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.DungeonHooks;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemAxe;
 import net.minecraft.item.ItemHoe;
 import net.minecraft.item.ItemSword;
 import net.minecraft.item.ItemSpade;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 
 @Mod(modid = "Generic", name = "Copper Mod", version = "1.0.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Generic
 {
     public final static Block CopperBlock = new CopperBlock(981, 1, Material.iron);
     public final static Block CreepOre = new CreepOre(980, 2, Material.iron);
     public final static Block genericOre = new GenericOre(900, 1, Material.iron);
     public final static Item genericItem = new GenericItem(990).setItemName("Copper Ingot");
     public static Item CopperPickaxe;
     public static Item CopperAxe;
     public static Item CopperShovel;
     public static Item CopperSword;
     public static Item CopperHoe;
     public static Item CopperPaxel;
     public static Item GlassShovel;
     public static Item CopperHelmet;
     public static Item CopperPlate;
     public static Item CopperLegs;
     public static Item CopperBoots;
     public static Item CopperBattleAxe;
     public static Item WoodenBattleAxe;
     public static Item StoneBattleAxe;
     public static Item IronBattleAxe;
     public static Item EmeraldBattleAxe;
     public static Item GoldenBattleAxe;
     public static int CopperIngotID;
     public static int CopperPickaxeID;
     public static int CopperAxeID;
     public static int CopperShovelID;
     public static int CopperSwordID;
     public static int CopperHoeID;
     public static int CopperPaxelID;
     public static int GlassShovelID;
     public static int CopperHelmetID;
     public static int CopperPlateID;
     public static int CopperLegsID;
     public static int CopperBootsID;
     public static int CopperOreID;
     public static int CopperBlockID;
 
     // sets the items
 
     @Instance("Generic")
     public static Generic instance;
 
     @SidedProxy(clientSide = "tutorial.generic.client.ClientProxy",
             serverSide = "tutorial.generic.CommonProxy")
     public static CommonProxy proxy;
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event)
     {
         Configuration config = new Configuration(event.getSuggestedConfigurationFile());
         config.load();
         CopperIngotID = config.getItem("Copper Ingot", 990).getInt();
         CopperPickaxeID = config.getItem("Copper Pickaxe", 550).getInt();
         CopperAxeID = config.getItem("Copper Axe", 551).getInt();
         CopperShovelID = config.getItem("Copper Shovel", 552).getInt();
         CopperSwordID = config.getItem("Copper Sword", 554).getInt();
         CopperHoeID = config.getItem("Copper Hoe", 553).getInt();
         CopperPaxelID = config.getItem("Copper Paxel", 555).getInt();
         GlassShovelID = config.getItem("Glass Shovel", 556).getInt();
         CopperHelmetID = config.getItem("Copper Helmet", 4000).getInt();
         CopperPlateID = config.getItem("Copper Plate", 4001).getInt();
         CopperLegsID = config.getItem("Copper Legs", 4002).getInt();
         CopperBootsID = config.getItem("Copper Boots", 4003).getInt();
         CopperBlockID = config.getBlock("Copper Block", 981).getInt();
         CopperOreID = config.getBlock("Copper Ore", 900).getInt();
         config.save();
     }
 
     @Init
     public void load(FMLInitializationEvent event)
     {
     	proxy.registerServerTickHandler();
     	proxy.registerRenderThings();  	
     	
         OreDictionary.registerOre("ingotCopper", new ItemStack(genericItem));
         OreDictionary.registerOre("oreCopper", new ItemStack(genericOre));
         // OreDictionary stuff
        
         LanguageRegistry.addName(genericOre, "Copper Ore");
         LanguageRegistry.addName(CreepOre, "Creep Ore");
         LanguageRegistry.addName(CopperBlock, "Copper Block");
         MinecraftForge.setBlockHarvestLevel(CopperBlock, "pickaxe", 1);
         MinecraftForge.setBlockHarvestLevel(genericOre, "pickaxe", 1);
         MinecraftForge.setBlockHarvestLevel(CreepOre, "pickaxe", 1);
         GameRegistry.registerBlock(CopperBlock, "Copper Block");
         GameRegistry.registerBlock(genericOre, "generic Ore");
         GameRegistry.registerBlock(CreepOre, "Creep Ore");
         LanguageRegistry.addName(genericItem, "Copper Ingot");
         proxy.registerRenderers();
         GameRegistry.addSmelting(Generic.genericOre.blockID, new ItemStack(Generic.genericItem), 2f);
 
         if (!Loader.isModLoaded("ThermalExpansion"))
         {
             GameRegistry.registerWorldGenerator(new WorldGeneratorTutorial());
             EnumArmorMaterial TutArmorMaterial = EnumHelper.addArmorMaterial("TutMaterial", 24, new int[] {1, 4, 4, 1}, 10);
             CopperHelmet = new CopperArmor(4000, TutArmorMaterial, proxy.addArmor("Tutorial"), 0).setItemName("Copper Helmet").setIconIndex(0);
             CopperPlate = new CopperArmor(4001, TutArmorMaterial, proxy.addArmor("Tutorial"), 1).setItemName("Copper Plate").setIconIndex(1);
             CopperLegs = new CopperArmor(4002, TutArmorMaterial, proxy.addArmor("Tutorial"), 2).setItemName("Copper Legs").setIconIndex(2);
             CopperBoots = new CopperArmor(4003, TutArmorMaterial, proxy.addArmor("Tutorial"), 3).setItemName("Copper Boots").setIconIndex(3);
             LanguageRegistry.addName(CopperHelmet, "Copper Helmet");
             LanguageRegistry.addName(CopperPlate, "Copper Plate");
             LanguageRegistry.addName(CopperLegs, "Copper Legs");
             LanguageRegistry.addName(CopperBoots, "Copper Boots");
             CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperHelmet, true, new Object[]
                     {
                         "FFF", "F F" , "   " , 'F', "ingotCopper"
                     }));
             CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperPlate, true, new Object[]
                     {
                         "F F", "FFF" , "FFF" , 'F', "ingotCopper"
                     }));
             CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperLegs, true, new Object[]
                     {
                         "FFF", "F F" , "F F" , 'F', "ingotCopper"
                     }));
             CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperBoots, true, new Object[]
                     {
                         "F F", "F F" , "   " , 'F', "ingotCopper"
                     }));
         }
 
         /*
         if (Loader.isModLoaded("ThermalExpansion")) {
         	try {
         		}
         	catch (Exception e) {
         		System.out.println("<your mod name>: Thermal Expansion integration was unsuccessful - please contact the author of this mod to let them know hat the API may have changed.");
         	}
         }
         */
         // worldgen
        
         EnumToolMaterial tutMaterial = EnumHelper.addToolMaterial("TutMaterial", 2, 200, 5.0F,(int) 1.5, 22);
         EnumToolMaterial Glass = EnumHelper.addToolMaterial("Glass", 0, 1, 1.5F, 0, 10);
         EnumToolMaterial BattleAxe = EnumHelper.addToolMaterial ("BattleAxe",2, 200, 5.0F, 2, 22);
         EnumToolMaterial WOOD = EnumHelper.addToolMaterial ("Wood",0, 59, 2.0F, 1, 15);
         EnumToolMaterial STONE = EnumHelper.addToolMaterial ("Stone",1, 131, 4.0F, 2, 5);
         EnumToolMaterial IRON = EnumHelper.addToolMaterial ("Iron",2, 250, 6.0F, 3, 14);
         EnumToolMaterial EMERALD = EnumHelper.addToolMaterial ("Emerald",3, 1561, 8.0F, 4, 10);
         EnumToolMaterial GOLD = EnumHelper.addToolMaterial ("Gold",0, 32, 12.0F, 1, 22);
         // Makes the new material
         
         WoodenBattleAxe = new WoodenBattleAxe(558, WOOD).setIconIndex(0).setItemName("Wooden Battleaxe");
         StoneBattleAxe = new StoneBattleAxe(559, STONE).setIconIndex(1).setItemName("Stone Battleaxe");
         IronBattleAxe = new IronBattleAxe(560, IRON).setIconIndex(2).setItemName("Iron Battleaxe");
         EmeraldBattleAxe = new EmeraldBattleAxe(561, EMERALD).setIconIndex(3).setItemName("Emerald Battleaxe");
         GoldenBattleAxe = new GoldenBattleAxe(562, GOLD).setIconIndex(4).setItemName("Golden Battleaxe");
         CopperBattleAxe = new CopperBattleAxe(557, BattleAxe).setIconIndex(5).setItemName("Copper Battleaxe");
         CopperPickaxe = new CopperPickaxe(550, tutMaterial).setIconIndex(3).setItemName("Copper Pickaxe");
         CopperAxe = new CopperAxe(551, tutMaterial).setIconIndex(3).setItemName("Copper Axe");
         CopperShovel = new CopperShovel(552, tutMaterial).setIconIndex(3).setItemName("Copper Shovel");
         CopperHoe = new CopperHoe(553, tutMaterial).setIconIndex(3).setItemName("Copper Hoe");
         CopperSword = new CopperSword(554, tutMaterial).setIconIndex(3).setItemName("Copper Sword");
         CopperPaxel = new CopperPaxel(555, tutMaterial).setIconIndex(3).setItemName("Copper Paxel");
         GlassShovel = new GlassShovel(556, Glass).setIconIndex(3).setItemName("Glass Shovel");
         LanguageRegistry.addName(CopperPickaxe, "Copper Pickaxe");
         LanguageRegistry.addName(CopperAxe, "Copper Axe");
         LanguageRegistry.addName(CopperShovel, "Copper Shovel");
         LanguageRegistry.addName(CopperSword, "Copper Sword");
         LanguageRegistry.addName(CopperHoe, "Copper Hoe");
         LanguageRegistry.addName(CopperPaxel, "Copper Paxel");
         LanguageRegistry.addName(GlassShovel, "Glass Shovel");
         LanguageRegistry.addName(CopperBattleAxe, "Copper Battleaxe");
         LanguageRegistry.addName(WoodenBattleAxe, "Wooden Battleaxe");
         LanguageRegistry.addName(StoneBattleAxe, "Stone Battleaxe");
         LanguageRegistry.addName(IronBattleAxe, "Iron Battleaxe");
         LanguageRegistry.addName(EmeraldBattleAxe, "Diamond Battleaxe");
         LanguageRegistry.addName(GoldenBattleAxe, "Golden Battleaxe");
         // Registry of the new tools
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperBlock, true, new Object[]
                 {
                     "FFF", "FFF" , "FFF" , 'F', "ingotCopper"
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperShovel, true, new Object[]
                 {
                     " F ", " X " , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperAxe, true, new Object[]
                 {
                     "FF ", "FX " , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperAxe, true, new Object[]
                 {
                     " FF", " XF" , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperPickaxe, true, new Object[]
                 {
                     "FFF", " X " , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperHoe, true, new Object[]
                 {
                     "FF ", " X " , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperHoe, true, new Object[]
                 {
                     " FF", " X " , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperSword, true, new Object[]
                 {
                     " F ", " F " , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(CopperBattleAxe, true, new Object[]
                 {
                     "FFF", "FXF" , " X " , 'F', "ingotCopper", 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(WoodenBattleAxe, true, new Object[]
                 {
                    "FFF", "FXF" , " X " , 'F', Block.wood, 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(StoneBattleAxe, true, new Object[]
                 {
                     "FFF", "FXF" , " X " , 'F', Block.cobblestone, 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(IronBattleAxe, true, new Object[]
                 {
                     "FFF", "FXF" , " X " , 'F', Item.ingotIron, 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(EmeraldBattleAxe, true, new Object[]
                 {
                     "FFF", "FXF" , " X " , 'F', Item.diamond, 'X', Item.stick
                 }));
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(GoldenBattleAxe, true, new Object[]
                 {
                     "FFF", "FXF" , " X " , 'F', Item.ingotGold, 'X', Item.stick
                 }));
         
         ItemStack GlassShovel = new ItemStack(Generic.GlassShovel);
         GlassShovel.addEnchantment(Enchantment.silkTouch, 2);
         CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(GlassShovel, true, new Object[]
                 {
                     " F ", " X " , " X " , 'F',  Block.glass , 'X', Item.stick
                 }));
         //Crafting recipes
         ItemStack CopperPaxel = new ItemStack(Generic.CopperPaxel);
         ItemStack CopperPickaxeStack = new ItemStack(Generic.CopperPickaxe);
         ItemStack CopperAxe = new ItemStack(Generic.CopperAxe);
         ItemStack CopperShovel = new ItemStack(Generic.CopperShovel);
         GameRegistry.addShapelessRecipe(new ItemStack(Generic.CopperPaxel),
                 CopperPickaxeStack, CopperShovel, CopperAxe);
         GameRegistry.addShapelessRecipe(new ItemStack(Generic.genericItem, 9),
                 CopperBlock);
         DungeonHooks.addDungeonLoot(CopperPaxel, 100, 1, 1);
     }
 
     @PostInit
     public void postInit(FMLPostInitializationEvent event)
     {
         // Stub Method
     }
 }
