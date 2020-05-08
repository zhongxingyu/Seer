 package no.runsafe.ItemControl.trading.commands;
 
 import net.minecraft.server.v1_7_R1.EntityVillager;
 import no.runsafe.ItemControl.trading.TradingHandler;
 import no.runsafe.framework.api.command.argument.IArgumentList;
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.entity.LivingEntity;
 import no.runsafe.framework.minecraft.entity.RunsafeEntity;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftVillager;
 import org.bukkit.entity.Villager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class CreateTrader extends PlayerCommand implements IPlayerInteractEntityEvent
 {
 	public CreateTrader(TradingHandler handler)
 	{
 		super("createtrader", "Create a trader!", "runsafe.itemcontrol.traders.create");
 		this.handler = handler;
 	}
 
 	@Override
 	public String OnExecute(IPlayer executor, IArgumentList parameters)
 	{
 		String playerName = executor.getName();
 		if (!interactTrack.contains(playerName))
 			interactTrack.add(playerName);
 
 		return "&eRight-click on a villager to make it a trader!";
 	}
 
 	@Override
 	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
 	{
 		IPlayer player = event.getPlayer();
 		String playerName = player.getName();
 
 		if (interactTrack.contains(playerName))
 		{
 			RunsafeEntity entity = event.getRightClicked();
 			if (entity.getEntityType() == LivingEntity.Villager)
 			{
 				Villager bukkit = (Villager) entity.getRaw();
 				CraftVillager craft = (CraftVillager) bukkit;
 				EntityVillager nms = craft.getHandle();
 
 				nms.getOffers(null).clear();
 			}
			else
			{
				player.sendColouredMessage("&cThat is not a villager.");
			}
			interactTrack.remove(playerName);
 		}
 	}
 
 	private final List<String> interactTrack = new ArrayList<String>(0);
 	private final TradingHandler handler;
 }
