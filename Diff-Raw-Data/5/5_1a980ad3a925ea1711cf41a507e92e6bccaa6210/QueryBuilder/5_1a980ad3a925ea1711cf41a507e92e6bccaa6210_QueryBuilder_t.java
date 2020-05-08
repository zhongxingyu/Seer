 package uk.co.jacekk.bukkit.playerstats;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 
 import uk.co.jacekk.bukkit.playerstats.data.PlayerData;
 
 public class QueryBuilder {
 	
 	public static String updatePlayers(HashMap<String, PlayerData> players){
 		StringBuilder sql = new StringBuilder();
 		
 		sql.append("INSERT INTO `stats_players` (`player_name`, `player_first_join`, `player_last_join`, `player_time_online`, `player_total_commands`, `player_total_chat`) ");
 		sql.append("VALUES ");
 		
 		for (Entry<String, PlayerData> entry : players.entrySet()){
 			String playerName = entry.getKey();
 			PlayerData data = entry.getValue();
 			
 			sql.append("('");
 			sql.append(playerName);
 			sql.append("', ");
 			sql.append(data.lastJoinTime);
 			sql.append(", ");
 			sql.append(data.lastJoinTime);
 			sql.append(", ");
 			sql.append((System.currentTimeMillis() / 1000) - data.lastUpdate);
 			sql.append(", ");
 			sql.append(data.totalCommands);
 			sql.append(", ");
 			sql.append(data.totalChatMessages);
 			sql.append("), ");
 		}
 		
 		sql.replace(sql.length() - 2, sql.length(), " ");
 		
 		sql.append("ON DUPLICATE KEY UPDATE ");
 		sql.append("`player_name` = VALUES(`player_name`), ");
 		sql.append("`player_last_join` = VALUES(`player_last_join`), ");
 		sql.append("`player_time_online` = `player_time_online` + VALUES(`player_time_online`),");
 		sql.append("`player_total_commands` = `player_total_commands` + VALUES(`player_total_commands`),");
 		sql.append("`player_total_chat` = `player_total_chat` + VALUES(`player_total_chat`)");
 		
 		return sql.toString();
 	}
 	
 	public static String updateBlocksPlaced(HashMap<String, PlayerData> players){
 		StringBuilder sql = new StringBuilder();
 		
 		sql.append("INSERT INTO `stats_blocks_placed` (`player_id`, `block_id`, `total`) ");
 		sql.append("VALUES ");
 		
 		for (Entry<String, PlayerData> entry : players.entrySet()){
 			String playerName = entry.getKey();
 			PlayerData data = entry.getValue();
 			
 			for (Entry<Material, Integer> blocks : data.blocksPlaced.entrySet()){
 				sql.append("(");
 				sql.append("(SELECT `player_id` FROM `stats_players` WHERE `player_name` = '");
 				sql.append(playerName);
 				sql.append("'), ");
 				sql.append(blocks.getKey().getId());
 				sql.append(", ");
 				sql.append(blocks.getValue());
 				sql.append("), ");
 			}
 		}
 		
 		sql.replace(sql.length() - 2, sql.length(), " ");
 		
 		sql.append("ON DUPLICATE KEY UPDATE ");
 		sql.append("`total` = `total` + VALUES(`total`)");
 		
 		return sql.toString();
 	}
 	
 	public static String updateBlocksBroken(HashMap<String, PlayerData> players){
 		StringBuilder sql = new StringBuilder();
 		
 		sql.append("INSERT INTO `stats_blocks_broken` (`player_id`, `block_id`, `total`) ");
 		sql.append("VALUES ");
 		
 		for (Entry<String, PlayerData> entry : players.entrySet()){
 			String playerName = entry.getKey();
 			PlayerData data = entry.getValue();
 			
			for (Entry<Material, Integer> blocks : data.blocksBroken.entrySet()){
 				sql.append("(");
 				sql.append("(SELECT `player_id` FROM `stats_players` WHERE `player_name` = '");
 				sql.append(playerName);
 				sql.append("'), ");
 				sql.append(blocks.getKey().getId());
 				sql.append(", ");
 				sql.append(blocks.getValue());
 				sql.append("), ");
 			}
 		}
 		
 		sql.replace(sql.length() - 2, sql.length(), " ");
 		
 		sql.append("ON DUPLICATE KEY UPDATE ");
 		sql.append("`total` = `total` + VALUES(`total`)");
 		
 		return sql.toString();
 	}
 	
 }
