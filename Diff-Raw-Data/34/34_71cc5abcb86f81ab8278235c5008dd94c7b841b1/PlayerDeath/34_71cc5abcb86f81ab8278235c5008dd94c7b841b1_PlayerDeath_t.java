 package no.runsafe.runsafeinventories.events;
 
 import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
 import no.runsafe.runsafeinventories.InventoryHandler;
 
 public class PlayerDeath implements IPlayerDeathEvent
 {
 	public PlayerDeath(InventoryHandler inventoryHandler, IDebug output)
 	{
 		this.inventoryHandler = inventoryHandler;
 		this.debugger = output;
 	}
 
 	@Override
 	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
 	{
 		this.debugger.debugFine("Detected death event for " + event.getEntity().getName());
 		this.inventoryHandler.wipeInventory(event.getEntity());
 		this.inventoryHandler.saveInventory(event.getEntity());
 	}
 
 	private final InventoryHandler inventoryHandler;
 	private final IDebug debugger;
 }
