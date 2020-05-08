 package no.runsafe.runsafeinventories.commands;
 
 import no.runsafe.framework.command.player.PlayerCommand;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.InventoryHistory;
 
 import java.util.HashMap;
 
 public class SwitchInventory extends PlayerCommand
 {
 	public SwitchInventory(InventoryHistory history)
 	{
 		super("switchinventory", "Moves a players inventory to the target.", "runsafe.inventories.switch", "source", "target");
 		this.history = history;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
 	{
 		RunsafePlayer source = RunsafeServer.Instance.getPlayer(parameters.get("source"));
 		RunsafePlayer target = RunsafeServer.Instance.getPlayer(parameters.get("target"));
 
 		if (source == null)
 			return "&cCould not find the source player";
 
 		if (target == null)
 			return "&cCould not find the target player";
 
 		if (source instanceof RunsafeAmbiguousPlayer)
 			return source.toString();
 
 		if (target instanceof RunsafeAmbiguousPlayer)
 			return target.toString();
 
 		RunsafeInventory targetInventory = target.getInventory();
 		RunsafeInventory sourceInventory = source.getInventory();
 
		this.history.save(target);
		this.history.save(source);
 
 		targetInventory.clear();
 		targetInventory.unserialize(sourceInventory.serialize());
 
 		sourceInventory.clear();
 
 		source.updateInventory();
 		target.updateInventory();
 
 		return String.format("Inventory of %s moved to %s.", source.getPrettyName(), target.getPrettyName());
 	}
 
 	private InventoryHistory history;
 }
