 package jk_5.nailed.map;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import cpw.mods.fml.common.network.FMLOutboundHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import cpw.mods.fml.relauncher.Side;
 import io.netty.channel.embedded.EmbeddedChannel;
 import jk_5.nailed.NailedLog;
 import jk_5.nailed.map.gameloop.InstructionController;
 import jk_5.nailed.map.mappack.Mappack;
 import jk_5.nailed.map.mappack.MappackMetadata;
 import jk_5.nailed.map.mappack.Spawnpoint;
 import jk_5.nailed.map.stat.StatManager;
 import jk_5.nailed.map.teleport.TeleportOptions;
 import jk_5.nailed.players.Player;
 import jk_5.nailed.players.PlayerRegistry;
 import jk_5.nailed.server.ProxyCommon;
 import jk_5.nailed.util.ChatColor;
 import lombok.Getter;
 import net.minecraft.util.ChatComponentText;
 import net.minecraft.util.IChatComponent;
 import net.minecraft.world.GameRules;
 import net.minecraft.world.World;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.network.ForgeMessage;
 
 import java.io.File;
 import java.util.List;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class Map {
 
     @Getter private int ID = DimensionManager.getNextFreeDimId();
     @Getter private final Mappack mappack;
     @Getter private World world;
     @Getter private boolean isLoaded = false;
     @Getter private final TeamManager teamManager;
     @Getter private final StatManager statManager;
     @Getter private final InstructionController gameController;
     @Getter private int joinedPlayers = 0;
 
     public Map(Mappack mappack, int id){
         this.ID = id;
         this.mappack = mappack;
         this.teamManager = new TeamManager(this);
         this.statManager = new StatManager(this);
         this.gameController = new InstructionController(this);
         MapLoader.instance().addMap(this);
     }
 
     void initMapServer(){
         if(this.isLoaded) return;
         NailedLog.info("Initializing %d", this.getID());
 
         DimensionManager.registerDimension(this.getID(), ProxyCommon.providerID);
         DimensionManager.initDimension(this.getID());
 
         ForgeMessage.DimensionRegisterMessage packet = new ForgeMessage.DimensionRegisterMessage();
         ReflectionHelper.setPrivateValue(ForgeMessage.DimensionRegisterMessage.class, packet, ProxyCommon.providerID, "providerId");
         ReflectionHelper.setPrivateValue(ForgeMessage.DimensionRegisterMessage.class, packet, this.getID(), "dimensionId");
         EmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
         channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
         channel.writeOutbound(packet);
     }
 
     public void setWorld(World world){
         Preconditions.checkNotNull(world);
         this.world = world;
         //world.worldScoreboard = MapLoader.instance().getLobby().world.worldScoreboard; //TODO: re-enable
         if(world.provider != null) this.ID = world.provider.dimensionId;
         this.isLoaded = true;
         this.teamManager.onWorldSet();
 
         if(this.mappack != null){
             MappackMetadata meta = this.mappack.getMappackMetadata();
             GameRules rules = world.getGameRules();
             for(java.util.Map.Entry<String, String> e : meta.getGameruleConfig().entrySet()){
                 if(rules.hasRule(e.getKey())){
                     rules.setOrCreateGameRule(e.getKey(), e.getValue());
                 }
             }
             world.difficultySetting = meta.getDifficulty();                                 //TODO: is this correct?
             world.setAllowedSpawnTypes(meta.isSpawnHostileMobs() && world.difficultySetting.func_151525_a() > 0, meta.isSpawnFriendlyMobs());
         }
 
         NailedLog.info("Registered map " + this.getSaveFileName());
     }
 
     public void unloadAndRemove(){
         MapLoader.instance().removeMap(this);
         this.getSaveFolder().delete();
     }
 
     public void reloadFromMappack(){
         for(Player player : this.getPlayers()){
             player.getEntity().playerNetServerHandler.func_147360_c("[" + ChatColor.GREEN + "Nailed" + ChatColor.RESET + "] Reloading the map you were in"); //kickPlayerFromServer
         }
         this.unloadAndRemove();
         this.mappack.prepareWorld(this.getSaveFolder());
         DimensionManager.registerDimension(this.getID(), ProxyCommon.providerID);
         DimensionManager.initDimension(this.getID());
     }
 
     public void onPlayerJoined(Player player){
         this.teamManager.onPlayerJoinedMap(player);
         this.joinedPlayers ++;
         MapLoader.instance().checkShouldStart(this);
     }
 
     public void onPlayerLeft(Player player){
         this.teamManager.onPlayerLeftMap(player);
         this.joinedPlayers --;
         MapLoader.instance().checkShouldStart(this);
     }
 
     public String getSaveFileName(){
         return PotentialMap.getSaveFileName(this);
     }
 
     public File getSaveFolder(){
         return new File(MapLoader.getMapsFolder(), this.getSaveFileName());
     }
 
     public TeleportOptions getSpawnTeleport(){
         if(this.mappack == null){
             return new TeleportOptions(this, this.world.getSpawnPoint(), 0, 0);
         }
         MappackMetadata meta = this.mappack.getMappackMetadata();
        Spawnpoint spawnpoint = new Spawnpoint(meta.getSpawnPoint());
        return new TeleportOptions(this, spawnpoint, spawnpoint.yaw, spawnpoint.pitch);
     }
 
     public void broadcastChatMessage(IChatComponent message){
         for(Player player : PlayerRegistry.instance().getPlayers()){
             if(player.getCurrentMap() == this){
                 player.sendChat(message);
             }
         }
     }
 
     public void broadcastChatMessage(String message){
         this.broadcastChatMessage(new ChatComponentText(message));
     }
 
     public List<Player> getPlayers(){
         List<Player> ret = Lists.newArrayList();
         for(Player player : PlayerRegistry.instance().getPlayers()){
             if(player.getCurrentMap() == this){
                 ret.add(player);
             }
         }
         return ret;
     }
 
     public void onGameStarted(){
         this.teamManager.onGameStarted();
     }
 
     public void onGameEnded(){
         this.teamManager.onGameEnded();
     }
 
     public Spawnpoint getRandomSpawnpoint(){
         List<Spawnpoint> spawnpoints = mappack.getMappackMetadata().getRandomSpawnpoints();
         return spawnpoints.get(MapLoader.instance().getRandomSpawnpointSelector().nextInt(spawnpoints.size()));
     }
 }
