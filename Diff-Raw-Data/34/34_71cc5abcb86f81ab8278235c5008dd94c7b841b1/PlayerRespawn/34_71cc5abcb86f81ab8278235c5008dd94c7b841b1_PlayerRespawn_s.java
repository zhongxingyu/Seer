 package no.runsafe.runsafeinventories.events;
 
import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.event.player.IPlayerRespawn;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.runsafeinventories.InventoryHandler;
 
 public class PlayerRespawn implements IPlayerRespawn
 {
 	public PlayerRespawn(InventoryHandler inventoryHandler, IDebug debugger)
 	{
 		this.inventoryHandler = inventoryHandler;
 		this.debugger = debugger;
 	}
 
 	@Override
 	public ILocation OnPlayerRespawn(IPlayer player, ILocation location, boolean isBed)
 	{
 		this.debugger.debugFine("Got respawn event for %s", player.getName());
 		this.inventoryHandler.handlePostWorldChange(player);
 		return location;
 	}
 
 	private final InventoryHandler inventoryHandler;
 	private final IDebug debugger;
 }
