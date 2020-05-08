 package me.arno.blocklog.logs;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import me.arno.blocklog.managers.DatabaseManager;
 import me.arno.blocklog.util.Query;
 
 import org.bukkit.Location;
 
 
 public class InteractionEntry extends DataEntry {
 	private int block;
 	
 	public InteractionEntry(String player, Location location, int block) {
 		super(player, LogType.INTERACTION, location, null);
 		this.block = block;
 	}
 	
 	@Override
 	public HashMap<String, Object> getValues() {
 		if(this.getId() > 0)
 			return null;
 		
 		HashMap<String, Object> values = new HashMap<String, Object>();
 
 		values.put("player", getPlayer());
 		values.put("block", getBlock());
 		values.put("world", getWorld());
 		values.put("x", getX());
 		values.put("y", getY());
 		values.put("z", getZ());
 		values.put("date", getDate());
 		return values;
 	}
 	
 	@Override
 	public void save(Connection conn) {
 		try {
 			Query query = new Query(DatabaseManager.databasePrefix + "interactions");
 			query.insert(getValues(), conn);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getBlock() {
 		return block;
 	}
 }
