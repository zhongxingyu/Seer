 package net.medsouz.omg;
 
 import java.util.HashMap;
 
 import net.medsouz.omg.entity.EntityAmmunition;
 import net.medsouz.omg.gun.GunM16;
 import net.medsouz.omg.item.ItemGun;
 import net.medsouz.omg.item.ItemRenderGun;
 import net.medsouz.omg.net.PacketHandler;
 import net.medsouz.omg.proxy.CommonProxy;
 import net.minecraft.item.Item;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.client.MinecraftForgeClient;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "omg", name = "OpenModularGuns", version = "0.1")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, channels={"omg"}, packetHandler = PacketHandler.class)
 public class OpenModularGuns {
 	@Instance
 	public static OpenModularGuns instance;
 	@SidedProxy(clientSide="net.medsouz.omg.proxy.ClientProxy", serverSide="net.medsouz.omg.proxy.CommonProxy")
 	public static CommonProxy proxy;
 	public final static Item m16 = new ItemGun(4030, new GunM16());
 	//Anticheat: Makes sure people don't shoot too fast. This should only be used serverside. The client will also have its own version to prevent desync
 	public static HashMap<String, Long> lastShots = new HashMap<String, Long>();
 	
 	@EventHandler
 	public void init(FMLInitializationEvent evt) {
 		LanguageRegistry.addName(m16, "m16");
 		GameRegistry.registerItem(m16, "m16");
		EntityRegistry.registerModEntity(EntityAmmunition.class, "OMG Ammunition", 99, this, 255, 20, true);
 		proxy.load();
 		proxy.registerRenderers();
 	}
 	
 }
