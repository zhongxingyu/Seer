 package no.runsafe.mailbox.events;
 
 import no.runsafe.framework.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.RunsafeMeta;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.mailbox.MailHandler;
 
 public class PlayerInteract implements IPlayerInteractEvent
 {
 	public PlayerInteract(MailHandler mailHandler)
 	{
 		this.mailHandler = mailHandler;
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		if (event.hasItem())
 		{
 			RunsafeItemStack item = event.getItemStack();
			if (item.is(Item.Decoration.Chest) && item.hasItemMeta())
 			{
 				String displayName = ((RunsafeMeta) item).getDisplayName();
 				if (displayName != null && displayName.startsWith("Mail Package #"))
 				{
 					RunsafePlayer player = event.getPlayer();
 					String[] split = displayName.split("#");
 					this.mailHandler.openPackage(player, Integer.valueOf(split[1]));
 					player.getInventory().remove(item);
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	private final MailHandler mailHandler;
 }
