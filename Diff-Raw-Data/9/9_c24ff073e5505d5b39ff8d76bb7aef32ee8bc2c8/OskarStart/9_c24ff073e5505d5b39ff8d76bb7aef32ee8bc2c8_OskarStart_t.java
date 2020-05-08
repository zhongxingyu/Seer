 package Oskar13;
 
 import java.lang.annotation.Annotation;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.java.games.util.plugins.Plugin;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.ITinyPacketHandler;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.proxyServer.proxyServer;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.SidedProxy;
 
 
 
 @Mod(modid = "OskarStart", name = "Klasa Startowa Oskara", version = "1.0")
 @NetworkMod(clientSideRequired=true, serverSideRequired=true,  packetHandler = PacketHandler.class, channels = { "Oskar13" })
 
 
 public class OskarStart {
 	
	@SidedProxy(clientSide="net.minecraft.client.proxyClient.proxyClient", serverSide="cpw.mods.fml.common.proxyServer.proxyServer")
	public static proxyServer proxy;
	
	
	@Instance("OskarStart")
 	public static OskarStart instance;
 
 	
 
    private static boolean debug = true;
 
 
 public static void debug(String deg) {
 	
 	if(debug == true) { 	 
 		System.out.println(deg);
 		
 	}
 }
 
 	 public String nazwy_bonusow(int id) {
 	    	String bonusy = null;
 	    if(id == 0) {
 	    	bonusy= "Sila";
 	    }
 	    if(id == 1){
 	    	bonusy = "Zamrazanie";
 	    }
 	    if(id == 2){
 	    	bonusy = "Ogien";
 	    }
 
 	    if(id == 3) {
 	    	
 	    	bonusy = "Zamrazanie";
 	    }
 	    	return bonusy;
 	    
 	    }
 
 	   @Init
 	   public void load(FMLInitializationEvent event) 
 	   {
 
 	
 	   }
 
 	 @PostInit
 	 public void PostINIT(FMLPostInitializationEvent event) { 
 
 		  
 
 		 
 	 }
 
 	
 }
 
 
