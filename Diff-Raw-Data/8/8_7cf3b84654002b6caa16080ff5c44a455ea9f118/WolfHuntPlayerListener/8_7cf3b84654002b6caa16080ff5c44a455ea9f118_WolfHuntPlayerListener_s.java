 package me.Kruithne.WolfHunt;
 
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class WolfHuntPlayerListener implements Listener
 {
 	private WolfHunt wolfHuntPlugin = null;
 	
 	WolfHuntPlayerListener(WolfHunt plugin)
 	{
 		this.wolfHuntPlugin = plugin;
 		this.wolfHuntPlugin.server.getPluginManager().registerEvents(this, this.wolfHuntPlugin);
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
 	{
		if(shouldTrackPlayers(event))
 		{
 			wolfHuntPlugin.trackPlayersRelativeTo(event.getPlayer());
 			event.setCancelled(true);
 		}
 	}
 
 	private boolean shouldTrackPlayers(PlayerInteractEntityEvent event)
 	{
 		Player eventPlayer = event.getPlayer();
 		Entity target = event.getRightClicked();
 		
 		if (!this.isHoldingTrackingItem(event))
 			return false;
 		
 		if (!this.isWolf(target))
 			return false;
 		
 		if (this.playerIsVanished(eventPlayer))
 			return false;
 		
 		Wolf wolf = (Wolf)target;
 		
 		if (this.isBaby(wolf))
 			return false;
 		
 		if(!this.isPlayersWolf(wolf, eventPlayer))
 			return false;
 		
 		return this.allowedTrack(eventPlayer);
 	}
 	
 	private boolean isWolf(Entity entity)
 	{
 		return entity.getType() == EntityType.WOLF;
 	}
 
 	private boolean playerIsVanished(Player player)
 	{
 		return this.wolfHuntPlugin.config.enableVanishNoPacketSupport && this.wolfHuntPlugin.playerIsVanished(player);
 	}
 
 	private boolean isBaby(Wolf wolf)
 	{
		return this.wolfHuntPlugin.config.babyWolvesCanTrack || wolf.getAge() >= 0;
 	}
 
 	private boolean isHoldingTrackingItem(PlayerInteractEntityEvent event)
 	{
 		return event.getPlayer().getItemInHand().getTypeId() == this.wolfHuntPlugin.config.trackingItem;
 	}
 
 	private boolean isPlayersWolf(Wolf wolf, Player player)
 	{
 		return wolf.isTamed() && wolf.getOwner() == (AnimalTamer)player;
 	}
 
 	private boolean allowedTrack(Player player)
 	{
 		return this.wolfHuntPlugin.hasPermission("canTrack", player);
 	}
 }
