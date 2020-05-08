 package demonmodders.Crymod.Common.Karma;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.StringTranslate;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.ServerChatEvent;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.registry.GameRegistry;
 import demonmodders.Crymod.Common.Network.PacketPlayerKarma;
 
 /**
  * Class that manages player karma on the server
  *
  */
 public class PlayerKarmaManager implements IPlayerTracker {
 	
 	private PlayerKarmaManager() {
 		GameRegistry.registerPlayerTracker(this);
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	private static final PlayerKarmaManager instance = new PlayerKarmaManager();
 	
 	public static PlayerKarmaManager instance() {
 		return instance;
 	}
 	
 	public static void init() {
 		KarmaEventHandler.instance();
 	}
 	
 	public static final int MAX_KARMA_VALUE = 50;
 	
 	private Map<EntityPlayer, PlayerKarma> karmas = new HashMap<EntityPlayer, PlayerKarma>();
 	
 	public PlayerKarma getPlayerKarma(EntityPlayer player) {
 		return karmas.get(player);
 	}
 	
 	public static PlayerKarma playerKarma(EntityPlayer player) {
 		return instance.getPlayerKarma(player);
 	}
 	
 	public void updateClientKarma(EntityPlayer player) {
 		if (player == null) {
 			return;
 		}
 		new PacketPlayerKarma(karmas.get(player)).sendToPlayer(player);
 	}
 
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {		
 		PlayerKarma karma = new PlayerKarma(player);
 		karma.read(player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getCompoundTag("summoningmod"));
 		karmas.put(player, karma);
 		updateClientKarma(player);
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {
 		NBTTagCompound karmaNbt = new NBTTagCompound();
 		getPlayerKarma(player).write(karmaNbt);
 		NBTTagCompound persistedNbt = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
 		persistedNbt.setCompoundTag("summoningmod", karmaNbt);
 		player.getEntityData().setCompoundTag(EntityPlayer.PERSISTED_NBT_TAG, persistedNbt);
 		karmas.remove(player);
 	}
 	
 	private static final int[] MESSAGE_BORDERS = {50, 40, 25, 10};
 	
 	@ForgeSubscribe
 	public void onServerChat(ServerChatEvent evt) {
 		float karma = getPlayerKarma(evt.player).getKarma();
 		if (karma == 0) {
 			return;
 		}
 		int useBorder = -1;
 		for (int border : MESSAGE_BORDERS) {
 			if (karma > 0 && karma > border || karma < 0 && karma < -border && useBorder < border) {
 				useBorder = border;
 			}
 		}
 		if (useBorder != -1) {
 			String messageKey = "chat.prefix." + (karma < 0 ? "bad" : "good") + "." + useBorder;
			String messageColor = karma < 0 ? "4" : "1";
			evt.line = evt.line.replace(evt.username, messageColor + StringTranslate.getInstance().translateKey(messageKey) + "f " + evt.username);
 		}
 	}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) { }
 
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) { }
 }
