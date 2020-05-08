 package dark.comecloser;
 
 import java.io.File;
 import java.util.Arrays;
 
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.NetLoginHandler;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet1Login;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.Metadata;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = ComeCloser.MOD_ID, name = ComeCloser.MOD_NAME, version = ComeCloser.VERSION, useMetadata = true)
 @NetworkMod(channels = { ComeCloser.CHANNEL }, clientSideRequired = true, serverSideRequired = false, connectionHandler = ComeCloser.class, packetHandler = ComeCloser.class)
 public class ComeCloser implements IPacketHandler, IConnectionHandler
 {
     /*Idea cache a list of boolean values in the player to control if the tag should be rendered fully or not
      *As well cache range values per player on the server and detect and send player data to client when players are in range
      *Use this as well to control render ranges per player server side doing ray traces to determine ranges
      *
      */
     // @Mod Prerequisites
     public static final String MAJOR_VERSION = "@MAJOR@";
     public static final String MINOR_VERSION = "@MINOR@";
     public static final String REVIS_VERSION = "@REVIS@";
     public static final String BUILD_VERSION = "@BUILD@";
 
     // @Mod
     public static final String MOD_ID = "ComeCloser";
     public static final String MOD_NAME = "Come Closer";
     public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVIS_VERSION + "." + BUILD_VERSION;
 
     public static final String CHANNEL = ComeCloser.MOD_ID;
 
     @Instance(ComeCloser.MOD_ID)
     public static ComeCloser Instance;
 
     @Metadata(ComeCloser.MOD_ID)
     public static ModMetadata meta;
 
     @SidedProxy(clientSide = "dark.comecloser.ClientProxy", serverSide = "dark.comecloser.CommonProxy")
     public static CommonProxy proxy;
 
     public static float standingRange = 64f;
     public static float sneakRange = 32f;
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event)
     {
         this.Instance = this;
         // // Config ////
         if (event.getSide() == Side.SERVER)
         {
             Configuration config = new Configuration(new File(cpw.mods.fml.common.Loader.instance().getConfigDir(), "ComeCloser.cfg"));
             config.load();
             sneakRange = config.get(Configuration.CATEGORY_GENERAL, "SneakRange", 32).getInt();
             standingRange = config.get(Configuration.CATEGORY_GENERAL, "NormalRange", 64).getInt();
             config.save();
         }
 
     }
 
     @Override
     public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
     {
         proxy.playerLoggedIn(player, netHandler, manager);
 
     }
 
     @Override
     public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
     {
         return proxy.connectionReceived(netHandler, manager);
     }
 
     @Override
     public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
     {
         proxy.connectionOpened(netClientHandler, server, port, manager);
 
     }
 
     @Override
     public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
     {
         proxy.connectionOpened(netClientHandler, server, manager);
 
     }
 
     @Override
     public void connectionClosed(INetworkManager manager)
     {
         proxy.connectionClosed(manager);
 
     }
 
     @Override
     public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
     {
         proxy.clientLoggedIn(clientHandler, manager, login);
 
     }
 
     @Override
     public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
     {
         proxy.onPacketData(manager, packet, player);
 
     }
 
     public void loadModMeta()
     {
         meta.modId = ComeCloser.MOD_ID;
         meta.name = ComeCloser.MOD_NAME;
        meta.description = "Help improve SMP play by changing how the player's name tag renders";
 
         meta.url = "www.BuildBroken.com";
 
         meta.logoFile = "/cc_logo.png";
         meta.version = ComeCloser.VERSION;
        meta.authorList = Arrays.asList(new String[] { "DarkGuardsman" });
         meta.credits = "Fans - without fans this mod would have died a long time ago";
         meta.autogenerated = false;
 
     }
 
 }
