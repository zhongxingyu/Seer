 package no.runsafe.worldeditbridge;
 
 import com.sk89q.worldedit.EditSession;
 import com.sk89q.worldedit.IncompleteRegionException;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.BukkitPlayer;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldedit.masks.Mask;
 import com.sk89q.worldedit.regions.Region;
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.log.IConsole;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 public class WorldEditCommands implements WorldEditInterface
 {
	public WorldEditCommands(IConsole output, IServer server)
 	{
 		worldEdit = server.getPlugin("WorldEdit");
 		console = output;
 	}
 
 	@Override
 	public void select(IPlayer player, ILocation pos1, ILocation pos2)
 	{
 		Vector pt1 = new Vector(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ());
 		Vector pt2 = new Vector(pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
 		final CuboidSelection selection = new CuboidSelection((World) ObjectUnwrapper.convert(player.getWorld()), pt1, pt2);
 		worldEdit.setSelection((Player) ObjectUnwrapper.convert(player), selection);
 	}
 
 	/*
 		@Override
 		public BoundingBox getSelection(IPlayer player)
 		{
 			Selection selection = worldEdit.getSelection((Player)ObjectUnwrapper.convert(player));
 			Vector min = selection.getNativeMinimumPoint();
 			return new BoundingBox(
 				min.getX(), min.getY(), min.getZ(),
 				selection.getWidth(), selection.getHeight(), selection.getLength()
 			);
 		}
 	*/
 	@Override
 	public boolean regenerate(IPlayer runsafePlayer, ILocation pos1, ILocation pos2, boolean enableUndo)
 	{
 		Player bukkitPlayer = ObjectUnwrapper.convert(runsafePlayer);
 		EditSession editSession = worldEdit.createEditSession(bukkitPlayer);
 		select(runsafePlayer, pos1, pos2);
 		Selection selection = worldEdit.getSelection(bukkitPlayer);
 		Region region;
 		try
 		{
 			region = selection.getRegionSelector().getRegion();
 			if (region == null)
 				return false;
 		}
 		catch (IncompleteRegionException e)
 		{
 			console.logException(e);
 			return false;
 		}
 		Mask mask = editSession.getMask();
 		editSession.setMask(null);
 		BukkitPlayer player = worldEdit.wrapPlayer(bukkitPlayer);
 		player.getWorld().regenerate(region, editSession);
 		editSession.setMask(mask);
 		worldEdit.remember(bukkitPlayer, editSession);
 		if (!enableUndo)
 			worldEdit.getSession(bukkitPlayer).clearHistory();
 		return true;
 	}
 
 	private final WorldEditPlugin worldEdit;
	private final IConsole console;
 }
