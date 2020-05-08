 package me.arno.blocklog.logs;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 
 import me.arno.blocklog.util.Query;
 
 import org.bukkit.Location;
 
 
 public class InteractionEntry extends DataEntry {
 	private int block;
 	
 	public InteractionEntry(String player, Location location, int block) {
 		super(player, LogType.INTERACTION, location, null);
 		this.block = block;
 	}
 	
 	@Override
 	public void save(Connection conn) {
 		try {
 			Query query = new Query("blocklog_interactions");
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			
 			values.put("player", getPlayer());
 			values.put("block", getBlock());
 			values.put("world", getWorld());
			values.put("x", getZ());
 			values.put("y", getY());
			values.put("z", getX());
 			values.put("date", getDate());
 			
 			query.insert(values, conn);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getBlock() {
 		return block;
 	}
 }
