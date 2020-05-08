 package no.runsafe.mailbox.events;
 
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
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
 			RunsafeMeta item = event.getItemStack();
 			if (item.is(Item.Decoration.Chest) && item.hasItemMeta())
 			{
 				String displayName = item.getDisplayName();
 				if (displayName != null && displayName.startsWith("Mail Package #"))
 				{
 					RunsafePlayer player = event.getPlayer();
 					String[] split = displayName.split("#");
 					this.mailHandler.openPackage(player, Integer.valueOf(split[1]));
 					player.getInventory().remove(item);
					event.cancel();
 				}
 			}
 		}
 	}
 
 	private final MailHandler mailHandler;
 }
