 package epic_jdog.invisimod;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
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
 import epic_jdog.invisimod.proxy.CommonProxy;
import net.minecraftforge.common.EnumHelper;
 
 @Mod(modid = Invisimod.modID, name = "Invisimod", version = "0.0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class Invisimod {
     
    public static EnumToolMaterial InvisibladeMat = EnumHelper.addToolMaterial("Invisiblade", 3, 1000, 7.8F, 3, 15);

    
     private final static Item invisidustItem = new InvisidustItem(7055).setCreativeTab(CreativeTabs.tabMaterials)
             .setMaxStackSize(63).setUnlocalizedName("Invisidust");
     private final static Item invisidiamondItem = new InvisidustItem(7060).setCreativeTab(CreativeTabs.tabMaterials)
             .setMaxStackSize(63).setUnlocalizedName("Invisidiamond");
     private final static Item invisihelmetItem = new InvisiArmorItem(7056, EnumArmorMaterial.DIAMOND,
             ModLoader.addArmor("Invisiarmor"), 0).setCreativeTab(CreativeTabs.tabCombat).setUnlocalizedName(
             "Invisihelmet");
     private final static Item invisichestplateItem = new InvisiArmorItem(7057, EnumArmorMaterial.DIAMOND,
             ModLoader.addArmor("Invisiarmor"), 1).setCreativeTab(CreativeTabs.tabCombat).setUnlocalizedName(
             "Invisichestplate");
     private final static Item invisileggingsItem = new InvisiArmorItem(7058, EnumArmorMaterial.DIAMOND,
             ModLoader.addArmor("Invisiarmor"), 2).setCreativeTab(CreativeTabs.tabCombat).setUnlocalizedName(
             "Invisileggings");
     private final static Item invisibootsItem = new InvisiArmorItem(7059, EnumArmorMaterial.DIAMOND,
             ModLoader.addArmor("Invisiarmor"), 3).setCreativeTab(CreativeTabs.tabCombat).setUnlocalizedName(
             "Invisiboots");
     private final static Block invisiblockBlock = new InvisiblockBlock(1500, Material.rock).setCreativeTab(CreativeTabs.tabMisc)
             .setHardness(2F).setResistance(11).setUnlocalizedName("Invisiblock");
     
     public static final String modID = "Invisimod";
     // The instance of your mod that Forge uses.
     @Instance("Invisimod")
     public static Invisimod instance;
     
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide = "epic_jdog.invisimod.proxy.ClientProxy", serverSide = "epic_jdog.invisimod.proxy.CommonProxy")
     public static CommonProxy proxy;
     
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
         // Stub Method
     }
     
     @Init
     public void load(FMLInitializationEvent event) {
         proxy.registerRenderInformation();
         
         
         LanguageRegistry.addName(invisidustItem, "Invisidust");
         LanguageRegistry.addName(invisiblockBlock, "Invisiblock");
         LanguageRegistry.addName(invisihelmetItem, "Invisisuit Helmet");
         LanguageRegistry.addName(invisichestplateItem, "Invisisuit Chestplate");
         LanguageRegistry.addName(invisileggingsItem, "Invisisuit Leggings");
         LanguageRegistry.addName(invisibootsItem, "Invisisuit Boots");
         LanguageRegistry.addName(invisidiamondItem, "Invisidiamond");
         
         
         ItemStack diamondStack = new ItemStack(Item.diamond);
         ItemStack invisidiamondStack = new ItemStack(invisidiamondItem);
         ItemStack invisidustStack = new ItemStack(invisidustItem);
         ItemStack netherrackStack = new ItemStack(Block.netherrack);
         ItemStack cobbleStack = new ItemStack(Block.cobblestone);
         ItemStack stoneStack = new ItemStack(Block.stone);
         ItemStack glowstoneStack = new ItemStack(Item.lightStoneDust);
         ItemStack gunpowdahStack = new ItemStack(Item.gunpowder);
         ItemStack goldenCarrotStack = new ItemStack(Item.goldenCarrot);
         
         
         GameRegistry.registerBlock(invisiblockBlock, "invisiblockBlock");
         
         
         GameRegistry.addRecipe(new ItemStack(Invisimod.invisidustItem), "x  ", "  y", " z ", 'x', glowstoneStack, 'y',
                 gunpowdahStack, 'z', goldenCarrotStack);
     
         GameRegistry.addShapelessRecipe(new ItemStack(invisiblockBlock), netherrackStack, invisidustStack);
         GameRegistry.addShapelessRecipe(new ItemStack(invisiblockBlock), cobbleStack, invisidustStack);
         GameRegistry.addShapelessRecipe(new ItemStack(invisiblockBlock), stoneStack, invisidustStack);
         GameRegistry.addShapelessRecipe(invisidiamondStack, diamondStack, invisidustStack);
         
     }
     
     @PostInit
     public void postInit(FMLPostInitializationEvent event) {
         // Stub Method
     }
 }
