 package ictrobot.gems.module;
 
 import ictrobot.core.helper.tool.*;
 import ictrobot.core.item.*;
 import ictrobot.core.tool.*;
 import ictrobot.core.block.*;
 import ictrobot.core.helper.register.Register;
 import ictrobot.gems.colourgems.world.*;
 
 import java.io.File;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 
 public class ColourGemsModule {
     
    //Define IDs - Colour Gems
    public static int oreSapphireID;
    public static int oreGreenSapphireID;
    public static int oreRubyID;
    public static int sapphireID;
    public static int greenSapphireID;
    public static int rubyID;
    public static int sapphirePaxelID;
    public static int greenSapphirePaxelID;
    public static int rubyPaxelID;
    public static int sapphireSwordID;
    public static int greenSapphireSwordID;
    public static int rubySwordID;
    public static int blockSapphireID;
    public static int blockGreenSapphireID;
    public static int blockRubyID;
 
    //Define Blocks - Colour Gems
    public static Block oreSapphire;
    public static Block oreGreenSapphire;
    public static Block oreRuby;
    public static Block blockSapphire;
    public static Block blockGreenSapphire;
    public static Block blockRuby;
    
    //Define Items - Colour Gems
    public static Item sapphire;
    public static Item greenSapphire;
    public static Item ruby;
    public static Item sapphirePaxel;
    public static Item greenSapphirePaxel;
    public static Item rubyPaxel;
    public static Item sapphireSword;
    public static Item greenSapphireSword;
    public static Item rubySword;
    
    public static int sapphirePickaxeID;
    public static int greenSapphirePickaxeID;
    public static int rubyPickaxeID;
    public static int sapphireAxeID;
    public static int greenSapphireAxeID;
    public static int rubyAxeID;
    public static int sapphireShovelID;
    public static int greenSapphireShovelID;
    public static int rubyShovelID;
 
    public static Item sapphirePickaxe;
    public static Item greenSapphirePickaxe;
    public static Item rubyPickaxe;
    public static Item sapphireAxe;
    public static Item greenSapphireAxe;
    public static Item rubyAxe;
    public static Item sapphireShovel;
    public static Item greenSapphireShovel;
    public static Item rubyShovel;
    
    //Define World Gen - Colour Gems
    public static WorldSapphire worldSapphire = new WorldSapphire();
    public static WorldGreenSapphire worldGreenSapphire = new WorldGreenSapphire();
    public static WorldRuby worldRuby = new WorldRuby();
   
     public static void Config(File file) {
       Configuration config = new Configuration(file);
       config.load();
       //Items
       sapphireID = config.get(Configuration.CATEGORY_ITEM, "Sapphire", 5041).getInt();
       greenSapphireID = config.get(Configuration.CATEGORY_ITEM, "GreenSapphire", 5042).getInt();
       rubyID = config.get(Configuration.CATEGORY_ITEM, "Ruby", 5043).getInt();
       sapphirePaxelID = config.get(Configuration.CATEGORY_ITEM, "SapphirePaxel", 5044).getInt();
       greenSapphirePaxelID = config.get(Configuration.CATEGORY_ITEM, "GreenSapphirePaxel", 5045).getInt();
       rubyPaxelID = config.get(Configuration.CATEGORY_ITEM, "RubyPaxel", 5046).getInt();
       sapphireSwordID = config.get(Configuration.CATEGORY_ITEM, "SapphireSword", 5047).getInt();
       greenSapphireSwordID = config.get(Configuration.CATEGORY_ITEM, "GreenSapphireSword", 5048).getInt();
       rubySwordID = config.get(Configuration.CATEGORY_ITEM, "RubySword", 5049).getInt();
 
       sapphirePickaxeID = config.get(Configuration.CATEGORY_ITEM, "SapphirePickaxe", 5050).getInt();
       greenSapphirePickaxeID = config.get(Configuration.CATEGORY_ITEM, "GreenSapphirePickaxe", 5051).getInt();
       rubyPickaxeID = config.get(Configuration.CATEGORY_ITEM, "RubyPickaxe", 5052).getInt();
       sapphireAxeID = config.get(Configuration.CATEGORY_ITEM, "SapphireAxe", 5053).getInt();
       greenSapphireAxeID = config.get(Configuration.CATEGORY_ITEM, "GreenSapphireAxe", 5054).getInt();
       rubyAxeID = config.get(Configuration.CATEGORY_ITEM, "RubyAxe", 5055).getInt();
       sapphireShovelID = config.get(Configuration.CATEGORY_ITEM, "SapphireShovel", 5056).getInt();
       greenSapphireShovelID = config.get(Configuration.CATEGORY_ITEM, "GreenSapphireShovel", 5057).getInt();
       rubyShovelID = config.get(Configuration.CATEGORY_ITEM, "RubyShovel", 5058).getInt();
       //Blocks
       oreSapphireID = config.get(Configuration.CATEGORY_BLOCK, "OreSapphire", 1011).getInt();
       oreGreenSapphireID = config.get(Configuration.CATEGORY_BLOCK, "OreGreenSapphire", 1012).getInt();
       oreRubyID = config.get(Configuration.CATEGORY_BLOCK, "OreRuby", 1013).getInt();
       blockSapphireID = config.get(Configuration.CATEGORY_BLOCK, "BlockSapphire", 1014).getInt();
       blockGreenSapphireID = config.get(Configuration.CATEGORY_BLOCK, "BlockGreenSapphire", 1015).getInt();
       blockRubyID = config.get(Configuration.CATEGORY_BLOCK, "BlockRuby", 1016).getInt();
       config.save();
     }
 
     public static void Settings() {
       //Ore
       oreSapphire = (new Ore(oreSapphireID, "Sapphire", sapphireID)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreSapphire").setCreativeTab(CreativeTabs.tabBlock);
       oreGreenSapphire = (new Ore(oreGreenSapphireID, "GreenSapphire", greenSapphireID)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreGreenSapphire").setCreativeTab(CreativeTabs.tabBlock);
       oreRuby = (new Ore(oreRubyID, "Ruby", rubyID)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreRuby").setCreativeTab(CreativeTabs.tabBlock);
       //Gem Blocks
       blockSapphire = (new BasicBlock(blockSapphireID, "Sapphireblock", Material.iron)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockSapphire").setCreativeTab(CreativeTabs.tabBlock);
       blockGreenSapphire = (new BasicBlock(blockGreenSapphireID, "GreenSapphireblock", Material.iron)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockGreenSapphire").setCreativeTab(CreativeTabs.tabBlock);
       blockRuby = (new BasicBlock(blockRubyID, "Rubyblock", Material.iron)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("blockRuby").setCreativeTab(CreativeTabs.tabBlock);
       //Gems
       sapphire = (new Gem(sapphireID, "Sapphire"));
       greenSapphire = (new Gem(greenSapphireID, "GreenSapphire"));
       ruby = (new Gem(rubyID, "Ruby"));
       //Paxel
       sapphirePaxel = (new Paxel(sapphirePaxelID, ToolMaterials.Sapphire));
       greenSapphirePaxel = (new Paxel(greenSapphirePaxelID,  ToolMaterials.GreenSapphire));
       rubyPaxel = (new Paxel(rubyPaxelID,  ToolMaterials.Ruby));
       //Sword
       sapphireSword = (new Sword(sapphireSwordID, ToolMaterials.Sapphire));
       greenSapphireSword = (new Sword(greenSapphireSwordID, ToolMaterials.GreenSapphire));
       rubySword = (new Sword(rubySwordID, ToolMaterials.Ruby));
       //Pickaxe
       sapphirePickaxe = (new Pickaxe(sapphirePickaxeID, ToolMaterials.Sapphire));
       greenSapphirePickaxe = (new Pickaxe(greenSapphirePickaxeID, ToolMaterials.GreenSapphire));
       rubyPickaxe = (new Pickaxe(rubyPickaxeID, ToolMaterials.Ruby));
       //Axe
       sapphireAxe = (new Axe(sapphireAxeID, ToolMaterials.Sapphire));
       greenSapphireAxe = (new Axe(greenSapphireAxeID, ToolMaterials.GreenSapphire));
       rubyAxe = (new Axe(rubyAxeID, ToolMaterials.Ruby));
       //Shovel
       sapphireShovel = (new Shovel(sapphireShovelID, ToolMaterials.Sapphire));
       greenSapphireShovel = (new Shovel(greenSapphireShovelID, ToolMaterials.GreenSapphire));
       rubyShovel = (new Shovel(rubyShovelID, ToolMaterials.Ruby));
    }
     
     public static void WorldGen() {
       GameRegistry.registerWorldGenerator(worldSapphire);
       GameRegistry.registerWorldGenerator(worldGreenSapphire);
       GameRegistry.registerWorldGenerator(worldRuby);
     }
     
     public static void Register(){
       GameRegistry.addRecipe(new ItemStack(sapphirePaxel), "lbl", " s ", " s ", 'b', new ItemStack(blockSapphire), 'l', new ItemStack(sapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(greenSapphirePaxel), "lbl", " s ", " s ", 'b', new ItemStack(blockGreenSapphire), 'l', new ItemStack(greenSapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(rubyPaxel), "lbl", " s ", " s ", 'b', new ItemStack(blockRuby), 'l', new ItemStack(ruby), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(sapphirePickaxe), "ggg", " s ", " s ", 'g', new ItemStack(sapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(greenSapphirePickaxe), "ggg", " s ", " s ", 'g', new ItemStack(greenSapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(rubyPickaxe), "ggg", " s ", " s ", 'g', new ItemStack(ruby), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(sapphireAxe), "gg ", "gs ", " s ", 'g', new ItemStack(sapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(greenSapphireAxe), "gg ", "gs ", " s ", 'g', new ItemStack(greenSapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(rubyAxe), "gg ", "gs ", " s ", 'g', new ItemStack(ruby), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(sapphireShovel), " g ", " s ", " s ", 'g', new ItemStack(sapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(greenSapphireShovel), " g ", " s ", " s ", 'g', new ItemStack(greenSapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(rubyShovel), " g ", " s ", " s ", 'g', new ItemStack(ruby), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(sapphireSword), " g ", " g ", " s ", 'g', new ItemStack(sapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(greenSapphireSword), " g ", " g ", " s ", 'g', new ItemStack(greenSapphire), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(rubySword), " g ", " g ", " s ", 'g', new ItemStack(ruby), 's', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(blockSapphire), "ggg", "ggg", "ggg", 'g', new ItemStack(sapphire));
       GameRegistry.addRecipe(new ItemStack(blockGreenSapphire), "ggg", "ggg", "ggg", 'g', new ItemStack(greenSapphire));
       GameRegistry.addRecipe(new ItemStack(blockRuby), "ggg", "ggg", "ggg", 'g', new ItemStack(ruby));
       GameRegistry.addShapelessRecipe(new ItemStack(sapphire, 9), new ItemStack(blockSapphire));
       GameRegistry.addShapelessRecipe(new ItemStack(greenSapphire, 9), new ItemStack(blockGreenSapphire));
       GameRegistry.addShapelessRecipe(new ItemStack(ruby, 9), new ItemStack(blockRuby));
       //Blocks - Coloured Gems
       Register.Block(oreSapphire, "Sapphire Ore", "pickaxe", 2);
       Register.Block(oreGreenSapphire, "Green Sapphire Ore", "pickaxe", 2);
       Register.Block(oreRuby, "Ruby Ore", "pickaxe", 2);
       Register.Block(blockSapphire, "Sapphire Block", "pickaxe", 2);
       Register.Block(blockGreenSapphire, "Green Sapphire Block", "pickaxe", 2);
       Register.Block(blockRuby, "Ruby Block", "pickaxe", 2);
       //Items - Coloured Gems
       Register.Item(sapphire, "Sapphire");
       Register.Item(greenSapphire, "Green Sapphire");
       Register.Item(ruby, "Ruby");
       Register.Item(sapphirePaxel, "Sapphire Paxel");
       Register.Item(greenSapphirePaxel, "Green Sapphire Paxel");
       Register.Item(rubyPaxel, "Ruby Paxel");
      Register.Item(sapphireSword, "Sapphire Paxel");
      Register.Item(greenSapphireSword, "Green Sapphire Paxel");
      Register.Item(rubySword, "Ruby Paxel");
       
       Register.Item(sapphirePickaxe, "Sapphire Pickaxe");
       Register.Item(greenSapphirePickaxe, "Green Sapphire Pickaxe");
       Register.Item(rubyPickaxe, "Ruby Pickaxe");
       Register.Item(sapphireAxe, "Sapphire Axe");
       Register.Item(greenSapphireAxe, "Green Sapphire Axe");
       Register.Item(rubyAxe, "Ruby Axe");
       Register.Item(sapphireShovel, "Sapphire Shovel");
       Register.Item(greenSapphireShovel, "Green Sapphire Shovel");
       Register.Item(rubyShovel, "Ruby Shovel");
       
       Register.Ore("gemSapphire", sapphire);
       Register.Ore("gemGreenSapphire", greenSapphire);
       Register.Ore("gemRuby", ruby);
     }
 }
