 package com.insofar.actor.author;
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.Player;
 
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.ItemInWorldManager;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.Packet;
 import net.minecraft.server.Packet18ArmAnimation;
 import net.minecraft.server.Packet33RelEntityMoveLook;
 import net.minecraft.server.Packet34EntityTeleport;
 import net.minecraft.server.Packet3Chat;
 import net.minecraft.server.Packet53BlockChange;
 import net.minecraft.server.Packet5EntityEquipment;
 import net.minecraft.server.World;
 
 
 public class EntityActor extends EntityPlayer {
 
 	public String name;
 	public Boolean isPlayback = false;
 	public int playbackTick = 0;
 	public Recording recording;
 	public MinecraftServer mcServer;
 	public Boolean loop = false;
 	public Boolean allPlayersView = false;
 	public ArrayList<Viewer> viewers = new ArrayList<Viewer>();
 
 	public EntityActor(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager)
 	{
 		super(minecraftserver, world, s, iteminworldmanager);
 		mcServer = minecraftserver;
 	}
 
 	public void tick()
 	{
 		if (isPlayback && recording != null)
 		{
 			doPlayback();
 		}
 	}
 
 	private void doPlayback()
 	{
 		// System.out.print(new StringBuilder("Playback entity: ").append(entityId));
 		if (recording.eof())
 		{
 			if (loop)
 			{
 				recording.rewind();
 			}
 			else
 			{
 				//System.out.print(new StringBuilder(" EOF"));
 				isPlayback = false;
 				return;
 			}
 		}
 
 		ArrayList<Packet>packets = recording.getNextPlaybackPackets();
 
 		if (packets != null && packets.size() > 0)
 		{
 			//System.out.println(new StringBuilder(" #packets=").append(packets.size()).append(": ").toString());
 
 			for (int i = 0; i < packets.size(); i++)
 			{
 				Packet p = packets.get(i);
 
 				//System.out.println(new StringBuilder("   ").append(p.b()).toString());
 
 				if (p instanceof Packet33RelEntityMoveLook)
 				{
 					//System.out.println(new StringBuilder("   Packet33"));
 					// Set the entity for this ghost on this packet
 					((Packet33RelEntityMoveLook)p).a = id;
 				}
 				else if (p instanceof Packet34EntityTeleport)
 				{
 					//System.out.println(new StringBuilder("   Packet34"));
 					((Packet34EntityTeleport)p).a = id;
 					setPosition(
 							(((Packet34EntityTeleport)p).b / 32),
 							(((Packet34EntityTeleport)p).c / 32),
 							(((Packet34EntityTeleport)p).d / 32));
 				}
 				else if (p instanceof Packet5EntityEquipment)
 				{
 					((Packet5EntityEquipment)p).a = id;
 				}
 				else if (p instanceof Packet18ArmAnimation)
 				{
 					((Packet18ArmAnimation)p).a = id;
 				}
 				else if (p instanceof Packet3Chat)
 				{
 					if (((Packet3Chat)p).message.indexOf(ChatColor.WHITE+"<") != 0)
 					{
 						((Packet3Chat)p).message = ChatColor.WHITE+"<" +
 						ChatColor.RED + name + ChatColor.WHITE +
 						"> "+((Packet3Chat)p).message;
 					}
 				}
 				else if (p instanceof Packet53BlockChange)
 				{
 					// Set the block in the server's world so it is in sync with the client
 					world.setRawTypeIdAndData(((Packet53BlockChange) p).a,
 							((Packet53BlockChange) p).b,
 							((Packet53BlockChange) p).c,
 							((Packet53BlockChange) p).material,
 							((Packet53BlockChange) p).data);
 				}
 				sendPacketToViewers(p);
 			}
 		}
 	}
 
 	/**
 	 * Rewind this actor - sending all rewind packets to viewers and a teleport to the jumpstart
 	 */
 	public void rewind()
 	{
 		// Send rewind packets
 		for (Packet p : recording.rewindPackets)
 		{
 			if (p instanceof Packet53BlockChange)
 			{
 				// Set the block in the server's world so it is in sync with the client
 				world.setRawTypeIdAndData(
 						((Packet53BlockChange) p).a,
 						((Packet53BlockChange) p).b,
 						((Packet53BlockChange) p).c,
						((Packet53BlockChange) p).data,
						((Packet53BlockChange) p).material);
 
 				sendPacketToViewers(p);
 			}
 		}
 
 		// Rewind the recording
 		recording.rewind();
 		Packet34EntityTeleport packet = recording.getJumpstart();
 		packet.a=id;
 
 		// Send packet to the viewers
 		sendPacketToViewers(packet);
 	}
 
 	/**
 	 * Send all viewers a packet.
 	 * @param p
 	 */
 	public void sendPacketToViewers(Packet p)
 	{
 		if (allPlayersView)
 		{
 			// Send to all in world
 			int dimension = world.worldProvider.dimension;
 			((CraftServer)Bukkit.getServer()).getServer().serverConfigurationManager.a(p,dimension);
 			return;
 		}
 
 		// Send packet to the viewer(s)
 		for (Viewer viewer : viewers)
 		{
 			viewer.sendPacket(p);
 		}
 	}
 
 	/**
 	 * True if player is a viewer of this actor.
 	 * @param player
 	 */
 	public boolean hasViewer(Player player)
 	{
 		if (allPlayersView)
 			return true;
 
 		for (Viewer viewer : viewers)
 		{
 			if (viewer.player.getName().equals(player.getName()))
 				return true;
 		}
 
 		return false;
 	}
 }
