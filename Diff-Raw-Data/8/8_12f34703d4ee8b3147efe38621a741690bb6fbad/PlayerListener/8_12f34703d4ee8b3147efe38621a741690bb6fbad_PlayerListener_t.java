 package com.insofar.actor.listeners;
 
 import java.util.ArrayList;
 
 import net.minecraft.server.Packet18ArmAnimation;
 import net.minecraft.server.Packet34EntityTeleport;
 import net.minecraft.server.Packet35EntityHeadRotation;
 import net.minecraft.server.Packet3Chat;
 import net.minecraft.server.Packet5EntityEquipment;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.player.PlayerToggleSprintEvent;
 
 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.ActorPlugin;
 import com.insofar.actor.EntityActor;
 
 public class PlayerListener implements Listener
 {
 
 	public ActorPlugin plugin;
 
 	public PlayerListener(ActorPlugin instance)
 	{
 		plugin = instance;
 	}
 
 	/**
 	 * Player spawn listener
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerLogin(PlayerJoinEvent event)
 	{
 		for (EntityActor actor : plugin.actors)
 		{
 			actor.spawn(event.getPlayer());
 		}
 	}
 
 
 	/**
 	 * Player spawn listener
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerLogout(PlayerQuitEvent event)
 	{
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Player quit");
 		}
 
 		ArrayList<EntityActor> removeActors = new ArrayList<EntityActor>();
 
 		for (EntityActor ea : ActorPlugin.getInstance().actors)
 		{
 			if (ea.getOwner() == event.getPlayer())
 			{
 				ActorAPI.actorRemove(ea);
 				removeActors.add(ea);
 			}
 		}
 
 		if (!removeActors.isEmpty())
 		{
 			// Remove them from the plugin's actors list
 			plugin.actors.removeAll(removeActors);
 		}
 	}
 
 	/**
 	 * Author player move listener
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerMove(PlayerMoveEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 
 		final Player p = event.getPlayer();
 		if (!ActorAPI.isPlayerBeingRecorded(p))
 		{
 			return;
 		}
 
 		// Create packet
 		Packet34EntityTeleport tp = new Packet34EntityTeleport();
 		Location to = event.getTo();
 		tp.a = p.getEntityId();
 		tp.b = floor_double(to.getX() * 32D);
 		tp.c = floor_double(to.getY() * 32D);
 		tp.d = floor_double(to.getZ() * 32D);
 		tp.e = (byte) (int) ((to.getYaw() * 256F) / 360F);
 		tp.f = (byte) (int) ((to.getPitch() * 256F) / 360F);
 		Packet35EntityHeadRotation hr = new Packet35EntityHeadRotation();
 
 		hr.a = p.getEntityId();
 		hr.b = tp.e;
 
 		ActorAPI.recordPlayerPacket(p, tp);
 		ActorAPI.recordPlayerPacket(p, hr);
 
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info(
 					"Player move recorded: (" + tp.b + "," + tp.c + ","
 					+ tp.d + ")" + " y: " + tp.e + " p: " + tp.f);
 		}
 	}
 
 	/**
 	 * Author player
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info(
 					"Player picked up " + event.getItem().toString());
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerChat(PlayerChatEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		final Player p = event.getPlayer();
 		if (!ActorAPI.isPlayerBeingRecorded(p))
 		{
 			return;
 		}
 
 		Packet3Chat cp = new Packet3Chat();
 		cp.message = event.getMessage();
 		ActorAPI.recordPlayerPacket(p, cp);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerInteract(PlayerInteractEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Player Interact");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Player Interact entity");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerEggThrow(PlayerEggThrowEvent event)
 	{
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Player Egg Throw");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onItemHeldChange(PlayerItemHeldEvent event)
 	{
 		final Player p = event.getPlayer();
 		if (!ActorAPI.isPlayerBeingRecorded(p))
 		{
 			return;
 		}
 
 		Packet5EntityEquipment packet = new Packet5EntityEquipment();
 		packet.b = 0;
 		packet.c = event.getPlayer().getInventory().getItemInHand()
 		.getTypeId();
 		packet.d = event.getPlayer().getInventory().getItemInHand()
 		.getData().getData();
 		if (packet.c == 0)
 			packet.c = -1;
 
 		ActorAPI.recordPlayerPacket(p, packet);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerDropItem(PlayerDropItemEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Player drop item");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Sneak toggle");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerToggleSprint(PlayerToggleSprintEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Sprint toggle");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerFish(PlayerFishEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Fish toggle");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerAnimation(PlayerAnimationEvent event)
 	{
 		if (event.isCancelled() && !plugin.getRootConfig().includeCancelledEvents)
 		{
 			return;
 		}
 		final Player p = event.getPlayer();
 		if (!ActorAPI.isPlayerBeingRecorded(p))
 		{
 			return;
 		}
 
 		// arm swing is the only animation bukkit supports?
 		if (event.getAnimationType().equals(PlayerAnimationType.ARM_SWING))
 		{
 			Packet18ArmAnimation packet = new Packet18ArmAnimation();
 			packet.b = 1;
 			ActorAPI.recordPlayerPacket(p, packet);
 		}
 		
 		if (plugin.getRootConfig().debugEvents)
 		{
 			plugin.getLogger().info("Player animation");
 		}
 	}
 
 	/**
 	 * Remove the player from the authors list
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		plugin.authors.remove(event.getPlayer().getName());
 	}
 
 	public static int floor_double(double d)
 	{
 		int i = (int) d;
 		return d >= i ? i : i - 1;
 	}
 
 }
