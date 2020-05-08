 package com.theswak.sleepytime.common;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Level;
 
 import com.theswak.sleepytime.SleepyTime;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.multiplayer.WorldClient;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.world.World;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerInteractEvent;
 import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
 
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 
 public class EventHooksHandler
 {	
 	@ForgeSubscribe
 	public void onSleepyTime(PlayerSleepInBedEvent event)
 	{
 		if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer instanceof EntityPlayerMP) {
 			EntityPlayerMP playerMP = (EntityPlayerMP)event.entityPlayer;
 			World world = playerMP.worldObj;
 			try {
				if(event.result.OK != null)  { // if event result is ok
 					sendSleepingStatus(playerMP, true);
 					/* DEBUG */
 					if(SleepyTime.DEBUG_MODE) { debugPlayerInfo(playerMP); }
				} else {
					LogHandler.log(Level.INFO, "EVENT.RESULT.OK is null!");
 				}
			}
			catch (Exception ex)
			{
 				ex.printStackTrace();
 			}
 		}
 	}
 	
 	private void sendSleepingStatus(EntityPlayer player, boolean state)
 	{
 		Byte PlayerStatus = 0;
 		World world = player.worldObj;
 		String playerName = player.username;
		
 		if(world == null || !world.isRemote) {
 			try {
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				DataOutputStream datastream = new DataOutputStream(bos);
 				Packet250CustomPayload packet = new Packet250CustomPayload();
 
 				datastream.write(PlayerStatus);
 				datastream.writeByte((byte)1);
 				datastream.writeBoolean(state);
 				datastream.writeUTF(playerName);
 				datastream.close();
 
 				packet.channel = SleepyTime.CHANNEL;
 				packet.data = bos.toByteArray();
 				packet.length = bos.size();
 				packet.isChunkDataPacket = false;
 
 				PacketDispatcher.sendPacketToAllInDimension(packet, player.worldObj.getWorldInfo().getDimension());
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 			
 			if(SleepyTime.DEBUG_MODE) { LogHandler.log(Level.INFO, player.toString() + " is sending SleepStatus"); }
 		} else {
 			LogHandler.log(Level.INFO, "world is null!");
 		}
 	}
 	
 	//////////////////////// DEBUG ////////////////////////
 	private void debugPlayerInfo(EntityPlayer player)
 	{
 		if(!player.equals(null)) {
 			String sender = player.getEntityName();
 			LogHandler.log(Level.INFO, "DEBUG SENDER :: " + sender); // sender player
 			LogHandler.log(Level.INFO, "DEBUG PLAYERENTITY :: " + player.worldObj.playerEntities.get(0).toString());
 		} else {
 			LogHandler.log(Level.WARNING, "EntityPlayer not found!");
 		}
 	}
 }
