 package mods.cc.rock;
 
 import mods.cc.rock.core.proxy.CommonProxy;
import mods.cc.rock.lib.Reference;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
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
 
 @Mod(modid = "CC", name = "CookingCraft")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class CookingCraft
 {
        @Instance(Reference.MOD_ID)
         public static CookingCraft instance;
         
     	@SidedProxy(clientSide = "mods.cc.rock.core.proxy.ClientProxy", serverSide = "mods.cc.rock.core.proxy.CommonProxy")
     	public static CommonProxy proxy;
     	
         
         @SuppressWarnings("unchecked")
         public static void addOreRecipe(ItemStack output, Object[] input) 
     	{
     		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(output, new Object[] { Boolean.valueOf(true), input }));
     	}
     
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event)
         {
             
             
         }
         
         
         @Init
         public void load(FMLInitializationEvent event)
         {
            
             
         }
         
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event)
         {
             
             
         }
 }
