 package com.madpcgaming.mt;
 
 import net.minecraft.creativetab.CreativeTabs;
 
 import com.madpcgaming.mt.blocks.ModBlocks;
 import com.madpcgaming.mt.core.proxy.CommonProxy;
 import com.madpcgaming.mt.handlers.ConfigurationHandler;
 import com.madpcgaming.mt.handlers.LocalizationHandler;
 import com.madpcgaming.mt.items.ModItems;
 import com.madpcgaming.mt.lib.Reference;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class MadTech
 {
 	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
 	public static CommonProxy	proxy;
 	@Instance("MT")
 	public static MadTech		instance;
 	public static CreativeTabs	tabsMT	= new CreativeTabMT(CreativeTabs.getNextID(), Reference.MOD_ID);
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		ConfigurationHandler.init(event.getSuggestedConfigurationFile());
 		LocalizationHandler.loadLanguages();
		NetworkRegistry.instance().registerGuiHandler(this, MadTech.proxy);
		instance = this;
 		ModBlocks.init();
 		ModItems.init();
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		
 	}
 	
 	@EventHandler
 	public void modsLoaded(FMLPostInitializationEvent event)
 	{
 		
 	}
 }
 
