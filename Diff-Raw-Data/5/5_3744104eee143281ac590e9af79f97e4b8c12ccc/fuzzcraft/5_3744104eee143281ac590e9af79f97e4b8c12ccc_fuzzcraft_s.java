 package FuzzCraft.Base;
 
 import java.util.logging.Level;
 
import rpworldgen.rpworldgen;

 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import cpw.mods.fml.common.FMLLog;
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
 import FuzzCraft.Blocks.*;
 
 
 
 
 @Mod(modid="modinfo.MOD_ID", name="modinfo.MOD_NAME", version="modinfo.MOD_VERSION")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 
 public class fuzzcraft {
     
     private static Property blockIdFlower, blockIdStone, blockIdBrick, blockIdChisBrick;
     
         @Instance(modinfo.MOD_ID)
         public static fuzzcraft instance;
        
         @SidedProxy(clientSide=modinfo.CLIENT_PROXY, serverSide=modinfo.SERVER_PROXY)
         public static CommonProxy proxy;
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event) { 
             
             Configuration fc_config = new Configuration(event.getSuggestedConfigurationFile());
             
             try {
                 fc_config.load();
                 
                 blockIdStone = fc_config.getBlock("ID.Stone", 1500);
                 blockIdFlower = fc_config.getBlock("ID.Flower", 1501);
                 blockIdBrick = fc_config.getBlock("ID.Brick", 1502);
                blockIdChisBrick = fc_config.getBlock("ID.Chiseled Brick", 1503);
             }
             catch (Exception e) {
                 FMLLog.log(Level.SEVERE, e, "Error loading FuzzCraft configuration file!");
             }
             finally {
                 FMLLog.log(Level.INFO, "FuzzCraft configuration loaded sucessfully! ");
                 fc_config.save();
             }
         }
        
         @Init
         public void load(FMLInitializationEvent event) {
             
 //            CreativeTabs FuzzCraftTab = new CreativeTabs(CreativeTabs.getNextID(), "FuzzCraft Decorations");
           
             proxy.registerRenderers();
          
             // Init blocks
             EnderFlower enderFlower = new EnderFlower(blockIdFlower.getInt());
             StoneBlock stoneBlock = new StoneBlock(blockIdStone.getInt());
             BrickBlock brickBlock = new BrickBlock(blockIdBrick.getInt());
             ChisBrickBlock chisbrickBlock = new ChisBrickBlock(blockIdChisBrick.getInt());
             
             // Register Blocks //
             
             // Ender Flower
             LanguageRegistry.addName(enderFlower, "Ender Flower");
             GameRegistry.registerBlock(enderFlower, "enderFlower");
             
             // Colored Stone
             
             MinecraftForge.setBlockHarvestLevel(stoneBlock, "Pick", 0);
             GameRegistry.registerBlock(stoneBlock, StoneBlockItem.class, "stoneBlock");
             for (int i = 0; i < 15; i++) {
                 LanguageRegistry.addName(new ItemStack(stoneBlock, 1, i), 
                        StoneBlock.stoneBlockNames[i]); }   
             
             // Colored Brick
             
             MinecraftForge.setBlockHarvestLevel(brickBlock, "Pick", 0);
             GameRegistry.registerBlock(brickBlock, BrickBlockItem.class, "brickBlock");
             for (int i = 0; i < 15; i++){
                 LanguageRegistry.addName(new ItemStack(brickBlock, 1, i),
                         BrickBlock.brickBlockNames[i]); }   
             
             // Colored Chiseled Brick
             
             MinecraftForge.setBlockHarvestLevel(chisbrickBlock, "Pick", 0);
             GameRegistry.registerBlock(chisbrickBlock, ChisBrickBlockItem.class, "chisbrickBlock");
             for (int i = 0; i < 15; i++){
                 LanguageRegistry.addName(new ItemStack(chisbrickBlock, 1, i),
                         ChisBrickBlock.chisbrickBlockNames[i]); }
           
            
            // Brick Recipes
             
             for (int i = 0; i < 15; i++) {
                 ItemStack stoneStack = new ItemStack(stoneBlock, 1, i);
                 GameRegistry.addRecipe(new ItemStack(brickBlock, 1, i),
                        "xx", "xx", 'x', stoneStack); }
             
            // Chiseled Brick Recipes
             
             for (int i = 0; i <15; i++) {
                 ItemStack brickStack = new ItemStack(brickBlock, 1, i);
                 GameRegistry.addRecipe(new ItemStack(chisbrickBlock, 1, i),
                         "xx", "xx", 'x', brickStack); }
        
         }
             
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event) {        
         }
   }
