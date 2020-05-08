 package no.runsafe.eventengine.libraries;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.block.IChest;
 import no.runsafe.framework.api.chunk.IChunk;
 import no.runsafe.framework.api.lua.FunctionParameters;
 import no.runsafe.framework.api.lua.Library;
 import no.runsafe.framework.api.lua.RunsafeLuaFunction;
 import no.runsafe.framework.api.lua.VoidFunction;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.LegacyMaterial;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.Sound;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import org.luaj.vm2.LuaTable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class WorldLibrary extends Library
 {
 	public WorldLibrary(RunsafePlugin plugin)
 	{
 		super(plugin, "world");
 	}
 
 	@Override
 	protected LuaTable getAPI()
 	{
 		LuaTable lib = new LuaTable();
 		lib.set("setBlock", new SetBlock());
 		lib.set("getBlock", new GetBlock());
 		lib.set("getPlayers", new GetPlayers());
 		lib.set("playSound", new PlaySound());
 		lib.set("cloneChestToPlayer", new CloneChestToPlayer());
 		return lib;
 	}
 
 	private static void prepareLocationForEdit(ILocation location)
 	{
 		IChunk chunk = location.getChunk();
 		if (chunk.isUnloaded()) chunk.load();
 	}
 
 	private static class SetBlock extends VoidFunction
 	{
 		@Override
 		public void run(FunctionParameters parameters)
 		{
 			ILocation location = parameters.getLocation(0);
 			WorldLibrary.prepareLocationForEdit(location);
 
 			byte damage = 0;
 			if (parameters.hasParameter(5))
 				damage = (byte) (int) parameters.getInt(5);
 
 			IBlock block = location.getBlock();
 			block.set(Item.get(LegacyMaterial.getById(parameters.getInt(4)), damage));
 		}
 	}
 
 	private static class PlaySound extends VoidFunction
 	{
 		@Override
 		public void run(FunctionParameters parameters)
 		{
 			ILocation location = parameters.getLocation(0);
 			location.playSound(
 					Sound.Get(parameters.getString(4)),
 					parameters.getFloat(5),
 					parameters.getFloat(6)
 			);
 		}
 	}
 
 	private static class GetBlock extends RunsafeLuaFunction
 	{
 		@Override
 		public List<Object> run(FunctionParameters parameters)
 		{
 			List<Object> returns = new ArrayList<Object>();
 			ILocation location = parameters.getLocation(0);
 			WorldLibrary.prepareLocationForEdit(location);
 
 			Item block = location.getBlock().getMaterial();
 			returns.add(block.getItemID());
 			returns.add(block.getData());
 
 			return returns;
 		}
 	}
 
 	private static class GetPlayers extends RunsafeLuaFunction
 	{
 		@Override
 		public List<Object> run(FunctionParameters parameters)
 		{
 			List<Object> returns = new ArrayList<Object>();
 			IWorld world = parameters.getWorld(0);
 
 			for (IPlayer player : world.getPlayers())
 				returns.add(player.getName());
 
 			return returns;
 		}
 	}
 
 	private static class CloneChestToPlayer extends VoidFunction
 	{
 		@Override
 		public void run(FunctionParameters parameters)
 		{
 			ILocation location = parameters.getLocation(0);
			IPlayer player = parameters.getPlayer(4);

 			IBlock block = location.getBlock();
			player.sendColouredMessage(block instanceof IChest ? "Yes" : "no");
 
 			if (block instanceof IChest)
 			{
 				IChest chest = (IChest) block;
 
 				for (RunsafeMeta item : chest.getInventory().getContents())
 					player.give(item);
 			}
 		}
 	}
 }
