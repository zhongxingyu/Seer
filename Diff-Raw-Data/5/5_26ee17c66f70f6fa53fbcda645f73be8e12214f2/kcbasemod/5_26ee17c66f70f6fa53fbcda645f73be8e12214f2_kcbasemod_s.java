 package com.polegamers.kitchencraft;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.src.BaseMod;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.network.NetworkMod;
 
@Mod(modid="kitchencraft", name="KitchenCraft", version="1.0.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 
 public class kcbasemod extends BaseMod{
 	
 	public static CreativeTabs kcTab = new CreativeTabKitchenCraft("kctab");
 
 
 
 @Override
 public String getVersion() {
	return "1.0.0";
 	
 }
 
 @Init
 public void load(){
 	
 	
 
 }
 
 
 
 }
 	
 
 	
 
