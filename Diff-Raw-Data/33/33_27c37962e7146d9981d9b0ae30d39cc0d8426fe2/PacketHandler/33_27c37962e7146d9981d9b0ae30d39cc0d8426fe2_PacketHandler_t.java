 package com.theswak.sleepytime.common;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 
 import com.theswak.sleepytime.SleepyTime;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.multiplayer.WorldClient;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.NetLoginHandler;
 import net.minecraft.network.packet.NetHandler;
 import net.minecraft.network.packet.Packet1Login;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.World;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class PacketHandler implements IPacketHandler, IConnectionHandler
 {
 	public static final byte PlayerStatus = 0;
 	private static final int STATUS_SLEEPING = 1; // player is sleeping
 	private static final int STATUS_AWAYFROMKEYBOARD = 2; // away from keyboard
 
 	@Override
 	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
 	{
 		PlayerManager.getInstance().Players.add(new PlayerInfo(((EntityPlayer)player).username, manager));
 	}
 
 	@Override
 	public String connectionReceived(NetLoginHandler netHandler,
 			INetworkManager manager) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void connectionOpened(NetHandler netClientHandler, String server,
 			int port, INetworkManager manager) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void connectionOpened(NetHandler netClientHandler,
 			MinecraftServer server, INetworkManager manager) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void connectionClosed(INetworkManager manager)
 	{
 		PlayerInfo pi = new PlayerInfo("", manager);
 
 		for(int i = 0; i < PlayerManager.getInstance().Players.size() && pi != null; i++)
 		{
 			if(PlayerManager.getInstance().Players.get(i).networkManager == manager) {
 				PlayerManager.getInstance().Players.remove(i);
 			}
 		}
 	}
 
 	@Override
 	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
 	{
 		PlayerManager.getInstance().Players.add(new PlayerInfo(clientHandler.getPlayer().username, manager));
 	}
 
 	
 	@Override
 	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
 	{
 		// server handles custom packets from the player
 		DataInputStream datastream = new DataInputStream(new ByteArrayInputStream(packet.data));
 		byte statusType = 0;
 		boolean isStatusActive;
 		byte type;
 		String sender;
 
 		try {
 			type = datastream.readByte();
 			EntityPlayer p = (EntityPlayer)player;
 			if(p == null) { // if player is not valid
 				return;
 			}
 
 			if(type == PlayerStatus) {
 				PlayerInfo pi = PlayerManager.getInstance().getPlayerInfoFromPlayer(p);
 				if(pi == null) {
 					return;
 				}
 				
 				statusType = datastream.readByte();
 				isStatusActive = datastream.readBoolean();
 				
 				if(statusType == STATUS_SLEEPING) {
 					sender = datastream.readUTF();
					p.addChatMessage("\u00a7C" + sender + " is sleeping.");
 					pi.isSleeping(true);
 				}
 				
 				if(statusType == STATUS_AWAYFROMKEYBOARD) {
 					p.addChatMessage(p.username + ": SETTING IDLE STATUS");
 					pi.isAwayFromKeyboard(true);
 				}
 				
 				/* DEBUG */
 				if(SleepyTime.DEBUG_MODE) { LogHandler.log(Level.INFO, "debug::statusType:: " + statusType); }
 			}
 		} catch (IOException error) {
 			return;
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public static void sendStatus(byte statusType, boolean isStatusActive)
 	{
 		Minecraft client = FMLClientHandler.instance().getClient();
 		WorldClient world = client.theWorld;
 		if(world == null || !world.isRemote) {
 			return;
 		} else {
 			try {
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				DataOutputStream datastream = new DataOutputStream(bos);
 				Packet250CustomPayload packet = new Packet250CustomPayload();
 
 				datastream.write(PlayerStatus);
 				datastream.writeByte(statusType);
 				datastream.writeBoolean(isStatusActive);
 				datastream.close();
 
 				packet.channel = SleepyTime.CHANNEL;
 				packet.data = bos.toByteArray();
 				packet.length = bos.size();
 				packet.isChunkDataPacket = false;
 
 				PacketDispatcher.sendPacketToServer(packet);
 				
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 		}
 	}
 }
