 package io.github.redinzane.playerhider;
 
 //Java Imports
 import java.lang.reflect.InvocationTargetException;
 import java.util.Map;
 import java.util.WeakHashMap;
 import org.bukkit.Bukkit;
 //Bukkit imports
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import com.comphenix.protocol.ProtocolLibrary;
 //ProtocolLib Imports
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.ConnectionSide;
 import com.comphenix.protocol.events.PacketAdapter;
 import com.comphenix.protocol.events.PacketEvent;
 import com.comphenix.protocol.wrappers.WrappedDataWatcher;
 //import com.comphenix.sneaky.AutoSneakers;
 //Imports from own packages
 import io.github.redinzane.playerhider.packets.Packet28EntityMetadata;
 
 public class PlayerHiderListener extends PacketAdapter implements Listener 
 {
     
 	private static final int ENTITY_CROUCHED = 0x02;
 	//Distance from which sneaking is visible
 	double sneakdistance;
 	boolean feature_LoS = false;
 		
 		
 	
 	// Whether or not a player can see autosneaking
 	private static final String PERMISSION_HIDE_AUTO = "playerhider.hide.autosneak";
 	
 	int updateCooldown = 500;
 	private long lastCall = 0;
 	
 	
 	// Last seen flag byte
 	private Map<Player, Byte> flagByte = new WeakHashMap<Player, Byte>();
 	
 	public PlayerHiderListener(Plugin plugin) 
 	{
 		super(plugin, ConnectionSide.SERVER_SIDE, Packet28EntityMetadata.ID);
 	}
 	
 	
 	/**
 	 * Update the given player.
 	 * @param manager - reference to ProtocolLib
 	 * @param player - player to refresh.
 	 * @throws InvocationTargetException If we are unable to send a packet.
 	 */
 	public void updatePlayer(ProtocolManager manager, Player player) throws InvocationTargetException
 	{
		if(player.isDead())
		{
			return;
		}
 		Byte flag = flagByte.get(player);
 		
 		// It doesn't matter much
 		if (flag == null) {
 			flag = 0;
 		}
 		
 		// Create the packet we will transmit
 		Packet28EntityMetadata packet = new Packet28EntityMetadata();
 		WrappedDataWatcher watcher = new WrappedDataWatcher();
 		watcher.setObject(0, flag);
 	
 		packet.setEntityId(player.getEntityId());
 		packet.setEntityMetadata(watcher.getWatchableObjects());
 		
 		// Broadcast the packet
 		for (Player observer : manager.getEntityTrackers(player)) 
 		{
 			manager.sendServerPacket(observer, packet.getHandle());
 		}
 	}
 	
 	@Override
 	public void onPacketSending(PacketEvent event) 
 	{
 		
 		//If a user has this permission, he will see players normally
 		if (event.getPlayer().hasPermission(PERMISSION_HIDE_AUTO)) 
 		{
 			return;
 		}
 		
 		//Initializing packets
 		Packet28EntityMetadata packet = new Packet28EntityMetadata(event.getPacket());
 		Entity entity = packet.getEntity(event);
 		
 		
 		if (entity instanceof Player) 
 		{
 			Player target = (Player) entity;
 			Player observer = event.getPlayer();
 			double distance = 0;
 			try
 			{
 				distance = target.getLocation().distance(observer.getLocation());
 			}
 			catch(IllegalArgumentException e)
 			{
 				return;
 			}
 			
 			
 			if (distance >= sneakdistance) 
 			{
 				WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 				Byte flag = watcher.getByte(0);
 				
 				if (flag != null) 
 				{
 					// Store the last seen flag byte
 					flagByte.put(target, flag);
 					
 					// Clone and update it
 					packet = new Packet28EntityMetadata(packet.getHandle().deepClone());
 					watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 					watcher.setObject(0, (byte) (flag | ENTITY_CROUCHED));
 					
 					event.setPacket(packet.getHandle());
 				}
 			}
 			else
 			{
 				if(feature_LoS == true)
 				{
 					if(observer.hasLineOfSight(target) == false)
 					{
 						WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 						Byte flag = watcher.getByte(0);
 						
 						if (flag != null) 
 						{
 							// Store the last seen flag byte
 							flagByte.put(target, flag);
 							
 							// Clone and update it
 							packet = new Packet28EntityMetadata(packet.getHandle().deepClone());
 							watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 							watcher.setObject(0, (byte) (flag | ENTITY_CROUCHED));
 							
 							event.setPacket(packet.getHandle());
 						}
 					}
 					else
 					{
 						WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 						Byte flag = watcher.getByte(0);
 
 						if (flag != null) 
 						{
 							// Store the last seen flag byte
 							flagByte.put(target, flag);
 
 							// Clone and update it
 							packet = new Packet28EntityMetadata(packet.getHandle().deepClone());
 							watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 							watcher.setObject(0, (byte) (flag));
 
 							event.setPacket(packet.getHandle());			
 						}
 					}
 				}
 				else
 				{
 					WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 					Byte flag = watcher.getByte(0);
 
 					if (flag != null) 
 					{
 						// Store the last seen flag byte
 						flagByte.put(target, flag);
 
 						// Clone and update it
 						packet = new Packet28EntityMetadata(packet.getHandle().deepClone());
 						watcher = new WrappedDataWatcher(packet.getEntityMetadata());
 						watcher.setObject(0, (byte) (flag));
 
 						event.setPacket(packet.getHandle());
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
     public void onPlayerMove(PlayerMoveEvent event) 
 	{
 		long time = System.currentTimeMillis();
 		if((time-lastCall)>updateCooldown)
 		{
 			lastCall = System.currentTimeMillis();
 			Player[] tempPlayers = Bukkit.getServer().getOnlinePlayers();
 			for(Player player: tempPlayers)
 			{			
 				try 
 				{
 					updatePlayer(ProtocolLibrary.getProtocolManager(), player);
 				}
 				catch (InvocationTargetException e) 
 				{
 					e.printStackTrace();
 				}
 			}							
 		}
 		else
 		{
 			
 		}
 	}
 }
