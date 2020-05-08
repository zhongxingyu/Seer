 package com.qzx.au.core;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 //import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 
@Mod(modid="AUCore", name="Altered Unification CORE", version=AUCore.modVersion)
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class AUCore {
 	@Instance("AUCore")
 	public static AUCore instance;
 
 //	@SidedProxy(clientSide="com.qzx.au.core.ClientProxy", serverSide="com.qzx.au.core.CommonProxy")
 //	public static CommonProxy proxy;
 
	public static final String modVersion = "0.0.0";

 	@PreInit
 	public void preInit(FMLPreInitializationEvent event){}
 
 	@Init
 	public void load(FMLInitializationEvent event){}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event){}
 }
