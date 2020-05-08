 package no.runsafe.toybox.command;
 
 import no.runsafe.framework.command.ExecutableCommand;
 import no.runsafe.framework.server.ICommandExecutor;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.Material;
 
 import java.util.HashMap;
 
 public class GiveItem extends ExecutableCommand
 {
 	public GiveItem()
 	{
 		super("give", "Gives a player an item", "runsafe.toybox.give");
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
 	{
 		return null;
 	}
 
 	@Override
 	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
 	{
 		if (arguments.length < 2)
 			return "&fUsage: /&3give &f[&eplayer&f] <&eitem&f> <&eamount&f>";
 
 		boolean shortHand = !(arguments.length > 2);
 		RunsafePlayer player;
 
 		if (shortHand)
 		{
 			if (executor instanceof RunsafePlayer)
 				player = (RunsafePlayer) executor;
 			else
 				return "&cYou need to supply a player to give items to.";
 		}
 		else
 		{
 			player = RunsafeServer.Instance.getPlayer(arguments[0]);
 			if (player == null)
 				return "&cThat player does not exist.";
 
 			if (player instanceof RunsafeAmbiguousPlayer)
 				return player.toString();
 		}
 
 		RunsafeItemStack item = this.getItemId((shortHand ? arguments[0] : arguments[1]));
 
 		if (item == null)
 			return "&cInvalid item name or ID.";
 
 		int amount = Integer.valueOf((shortHand ? arguments[1] : arguments[2]));
 		this.giveItems(player, item, amount);
 
 		return String.format("&fGave %sx %s to %s&f.", amount, item.getNormalName(), player.getPrettyName());
 	}
 
 	private RunsafeItemStack getItemId(String itemName)
 	{
 		for (Material material : Material.values())
 		{
 			if (material.name().replace("_", "").equalsIgnoreCase(itemName))
				return new RunsafeItemStack(material.getId());
 
 			if (itemName.matches("-?\\d+(\\.\\d+)?"))
 				if (material.getId() == Integer.valueOf(itemName))
					return new RunsafeItemStack(material.getId());

 		}
 		return null;
 	}
 
 	private void giveItems(RunsafePlayer player, RunsafeItemStack item, int amount)
 	{
 		int needed = amount;
 		while (needed > 0)
 		{
 			if (needed < item.getMaxStackSize())
 			{
 				item.setAmount(needed);
 				needed = 0;
 			}
 			else
 			{
 				item.setAmount(item.getMaxStackSize());
 				needed -= item.getMaxStackSize();
 			}
 			player.getInventory().addItems(item.clone());
 		}
 		player.updateInventory();
 	}
 }
