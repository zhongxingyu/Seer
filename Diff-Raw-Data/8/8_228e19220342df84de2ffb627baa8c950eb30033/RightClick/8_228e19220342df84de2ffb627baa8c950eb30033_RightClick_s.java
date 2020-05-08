 package no.runsafe.inspectorgadget.events;
 
 import no.runsafe.framework.event.player.IPlayerRightClickBlock;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.block.BlockState;
 import org.bukkit.craftbukkit.v1_4_6.block.CraftCreatureSpawner;
 
 public class RightClick implements IPlayerRightClickBlock
 {
 	@Override
 	public boolean OnPlayerRightClick(RunsafePlayer player, RunsafeItemStack usingItem, RunsafeBlock targetBlock)
 	{
		if (player.hasPermission("runsafe.inspector.gadget.blockdump") && usingItem.getItemId() == 265)
 		{
 			player.sendColouredMessage(dumpData(targetBlock));
 		}
 		return true;
 	}
 
 	private String dumpData(RunsafeBlock block)
 	{
 		StringBuilder dump = new StringBuilder();
 		dump.append(
 			String.format(
 				"&2%s &r@&4%d&r,&2%d&r,&1%d&r\n",
 				block.getMaterialType().getRaw().name(),
 				block.getLocation().getBlockX(),
 				block.getLocation().getBlockY(),
 				block.getLocation().getBlockZ()
 			)
 		);
 		dump.append(String.format("&5Data&r: %s\n", block.getRaw().getData()));
 		BlockState state = block.getRaw().getState();
 		dump.append(String.format("&5Block state&r: %s\n", state.getClass().getCanonicalName()));
 		dump.append(String.format(" - RawData: %s\n", state.getRawData()));
 		dump.append(String.format(" - Type: %s\n", state.getType().name()));
		if(state instanceof CraftCreatureSpawner)
 		{
			CraftCreatureSpawner spawner = (CraftCreatureSpawner)state;
 			dump.append(String.format(" - Spawned Type: %s\n", spawner.getSpawnedType().name()));
 			dump.append(String.format(" - Creature Type Name: %s\n", spawner.getCreatureTypeName()));
 			dump.append(String.format(" - Delay: %d\n", spawner.getDelay()));
 		}
 		return dump.toString();
 	}
 }
