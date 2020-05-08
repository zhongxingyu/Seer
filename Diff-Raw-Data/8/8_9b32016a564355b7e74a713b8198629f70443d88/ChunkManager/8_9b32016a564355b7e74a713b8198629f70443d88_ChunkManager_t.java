 package uk.co.quartzcraft.kingdoms.managers;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.entity.Player;
 
 import uk.co.quartzcraft.core.entity.QPlayer;
 import uk.co.quartzcraft.kingdoms.QuartzKingdoms;
 import uk.co.quartzcraft.kingdoms.entity.QKPlayer;
 import uk.co.quartzcraft.kingdoms.kingdom.Kingdom;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class ChunkManager {
 	
 	public static boolean claimChunkKingdom(Player player) {
 		Player chunkClaimer = player;
         String kingdomName = QKPlayer.getKingdom(player);
 		Chunk chunk = chunkClaimer.getLocation().getChunk();
         int chunkX = chunk.getX();
         int chunkZ = chunk.getZ();
 		
 		if(isClaimed(chunk) == false) {
 			//claim chunk
             try {
                 Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
                 if(s.executeUpdate("INSERT INTO Chunks (kingdom, kingdom_id, X, Z) VALUES (1, " + Kingdom.getID(kingdomName) + ", '" + chunkX + "', '" + chunkZ + "');") == 1) {
                     return true;
                 } else {
                     return false;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                 return false;
             }
 		} else {
             return false;
 		}
 	}
 
 	public static boolean unClaimChunkKingdom(Player player) {
         Player chunkClaimer = player;
         String kingdomName = QKPlayer.getKingdom(player);
         int kingdomID = Kingdom.getID(kingdomName);
         Chunk chunk = chunkClaimer.getLocation().getChunk();
         int chunkX = chunk.getX();
         int chunkZ = chunk.getZ();
 
         if(isClaimed(chunk) == true) {
             //unclaim chunk
             try {
                 Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
                if(s.executeUpdate("DELETE * FROM Chunks WHERE X='" + chunkX + "' AND Z='" + chunkZ + "' AND kingdom_id=" + kingdomID + ";") == 1) {
                     return true;
                 } else {
                     return false;
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
                 return false;
             }
         } else {
             return false;
         }
 	}
 
 	public static boolean isClaimed(Chunk chunk) {
         int chunkX = chunk.getX();
         int chunkZ = chunk.getZ();
 
         try {
             Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
            ResultSet res = s.executeQuery("SELECT * FROM Chunks WHERE X='" + chunkX + "' AND Z='" + chunkZ + "';");
             if(res.next()) {
                 return true;
             } else {
                 return false;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return false;
         }
 	}
 
 	public static String getKingdomOwner(Chunk chunk) {
         int chunkX = chunk.getX();
         int chunkZ = chunk.getZ();
 
         try {
                 Statement s = QuartzKingdoms.MySQLking.openConnection().createStatement();
                ResultSet res = s.executeQuery("SELECT * FROM Chunks WHERE X='" + chunkX + "' AND Z='" + chunkZ + "';");
                 if(res.next()) {
                     int kingdomID = res.getInt("kingdom_id");
                     return Kingdom.getName(kingdomID);
                 } else {
                     return null;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
 	}
 	
 	
 }
