 package no.runsafe.moosic.customjukebox;
 
 import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.IRow;
 import no.runsafe.framework.api.database.Repository;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class CustomJukeboxRepository extends Repository
 {
	public CustomJukeboxRepository(IDatabase database, IOutput console, IServer server)
 	{
 		this.database = database;
 		this.console = console;
 		this.server = server;
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "moosic_jukeboxes";
 	}
 
 	public void storeJukebox(ILocation location, RunsafeMeta item)
 	{
 		RunsafeInventory holder = server.createInventory(null, RunsafeInventoryType.CHEST);
 		holder.addItems(item);
 
 		this.database.Execute(
 			"INSERT INTO `moosic_jukeboxes` (world, x, y, z, item) VALUES(?, ?, ?, ?, ?)",
 			location.getWorld().getName(),
 			location.getBlockX(),
 			location.getBlockY(),
 			location.getBlockZ(),
 			holder.serialize()
 		);
 	}
 
 	public void deleteJukeboxes(ILocation location)
 	{
 		this.database.Execute(
 			"DELETE FROM `moosic_jukeboxes` WHERE world = ? AND x = ? AND y = ? AND z = ?",
 			location.getWorld().getName(),
 			location.getBlockX(),
 			location.getBlockY(),
 			location.getBlockZ()
 		);
 	}
 
 	public List<CustomJukebox> getJukeboxes()
 	{
 		List<CustomJukebox> jukeboxes = new ArrayList<CustomJukebox>();
 		for (IRow node : this.database.Query("SELECT world, x, y, z, item FROM moosic_jukeboxes"))
 		{
 			ILocation location = node.Location();
 			if (location == null)
 			{
 				console.logError("Tried initializing jukebox at null location from database! (%s)", node.String("world"));
 				continue;
 			}
 			RunsafeInventory holder = server.createInventory(null, RunsafeInventoryType.CHEST);
 			holder.unserialize(node.String("item"));
 			jukeboxes.add(new CustomJukebox(location, holder.getContents().get(0)));
 		}
 		return jukeboxes;
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> versions = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE `moosic_jukeboxes` (" +
 				"`world` VARCHAR(50) NOT NULL," +
 				"`x` DOUBLE NOT NULL," +
 				"`y` DOUBLE NOT NULL," +
 				"`z` DOUBLE NOT NULL," +
 				"`item` longtext" +
 				")"
 		);
 		versions.put(1, sql);
 		return versions;
 	}
 
 	private final IDatabase database;
	private final IOutput console;
 	private final IServer server;
 }
