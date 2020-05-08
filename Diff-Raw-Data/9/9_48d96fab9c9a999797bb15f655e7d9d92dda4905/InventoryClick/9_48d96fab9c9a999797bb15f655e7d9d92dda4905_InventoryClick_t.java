 package no.runsafe.mailbox.events;
 
 import no.runsafe.framework.event.inventory.IInventoryClick;
 import no.runsafe.framework.server.event.inventory.RunsafeInventoryClickEvent;
 import no.runsafe.framework.server.inventory.RunsafeAnvilInventory;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.RunsafeMeta;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 public class InventoryClick implements IInventoryClick
 {
 	@Override
 	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
 	{
 		RunsafeItemStack item = event.getCurrentItem();
		if (item == null || !item.hasItemMeta() || !(event.getInventory() instanceof RunsafeAnvilInventory))
 			return;
 
 		if (((RunsafeMeta)item).getDisplayName().startsWith("Mail Package #"))
 		{
 			RunsafePlayer player = event.getWhoClicked();
 			player.sendColouredMessage("&cYou cannot do that.");
 			event.setCancelled(true);
 		}
 	}
 }
