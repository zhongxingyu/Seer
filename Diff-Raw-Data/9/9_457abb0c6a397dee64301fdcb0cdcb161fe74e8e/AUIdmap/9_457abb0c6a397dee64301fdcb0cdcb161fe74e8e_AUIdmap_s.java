 package com.qzx.au.idmap;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 
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
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 @Mod(modid="AUIdmap", name="Altered Unification ID Map", version="0.0.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class AUIdmap {
 	@Instance("AUIdmap")
 	public static AUIdmap instance;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event){}
 
 	@Init
 	public void load(FMLInitializationEvent event){}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event){
 		String dir;
 		String filename = "au_ID_map.txt";
 		try {
 			dir = Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
 		} catch(NoClassDefFoundError e){
 			dir = MinecraftServer.getServer().getFile("").getAbsolutePath();
 		}
 		File file = new File(dir, "au_ID_map.txt");
 		PrintWriter out;
 		try {
 			file.createNewFile();
 			out = new PrintWriter(new FileWriter(file));
 		} catch(Throwable e){
			System.out.println("AU IDMAP: Unable to create file: "+dir+"/"+filename);
 			return;
 		}
 
 		out.println("---------------------------------------------");
 
 		int start = -1, nr_blocks = Block.blocksList.length, nr_items = Item.itemsList.length;
 		for(int i = 0; i < nr_blocks; i++){
 			if(Block.blocksList[i] == null){
 				if(start == -1) start = i;
 			} else if(start != -1){
 				showFreeRange(out, "block", start, i-1);
 				start = -1;
 			}
 		}
 		if(start != -1)
 			showFreeRange(out, "block", start, nr_blocks-1);
 
 		out.println("---------------------------------------------");
 
 		start = -1;
 		for(int i = 0; i < nr_items; i++){
 			if(Item.itemsList[i] == null){
 				if(start == -1) start = i;
 			} else if(start != -1){
 				showFreeRange(out, " item", start, i-1);
 				start = -1;
 			}
 		}
 		if(start != -1)
 			showFreeRange(out, " item", start, nr_items-1);
 
 		out.println("---------------------------------------------");
 		out.close();
 	}
 
 	private void showFreeRange(PrintWriter out, String type, int start, int end){
 		if(start == end)
 			out.println("     1  "+type+"  available at  "+String.format("%5d", start));
 		else
 			out.println(String.format("%6d", end-start+1)+"  "+type+"s available at  "+String.format("%5d", start)+"  -  "+String.format("%5d", end));
 	}
 }
