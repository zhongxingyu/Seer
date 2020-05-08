 package models;
 
 import utilities.*;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.List;
 import java.util.ArrayList;
 import redis.clients.jedis.*;
 import com.force.api.*;
 import play.*;
 
 public class PlayerManager {
 
 	private static final String PLAYER_QUERY_ROOT = "select id, name, Nights_Available__c, Positions__c from Player__c";
 	private static final String ALL_PLAYERS_CACHE_KEY = "all_players";
 
 	private ForceApi api;
 
 	public PlayerManager(ForceApi api) {
 		this.api = api;	
 	}
 
 	public List<Player> getAllPlayers() {
 		return findPlayers(null,null);	
 	}
 
 	public List<Player> findPlayers(String[] positions, String[] nightsAvailable) {
 		Map<String,Player> allPlayersMap = getAllPlayersMap();
 		if (( positions == null || positions.length == 0) && ( nightsAvailable == null || nightsAvailable.length == 0)){
 			return new ArrayList(allPlayersMap.values());
 		}
 		else {
 			JedisPool pool = RedisHelper.getPool();
 			Jedis jedis = pool.getResource();
 			try {
 				HashSet<String> keysToIntersect = new HashSet<String>();
 				for(String s : positions){
 					keysToIntersect.add(RedisHelper.buildSetMembersKey("position",s));
 				}
 				for(String s : nightsAvailable){
 					keysToIntersect.add(RedisHelper.buildSetMembersKey("nightAvailable",s));
 				}
 				//call Jeds SINTER
 				Logger.debug("searching with keys: " + keysToIntersect);
 				ArrayList<Player> players = new ArrayList<Player>();
 				for (String playerId : jedis.sinter(keysToIntersect.toArray(new String[0]))) {
 					players.add(allPlayersMap.get(playerId));	
 				}		
 				return players;
 			}
 			finally {
 				pool.returnResource(jedis);
 			}
 		}
 	}
 
 	//TODO where best to call this?  during individual save? or only on mass save?
 	public static boolean indexPlayers(List<Player> players) {
 		JedisPool pool = RedisHelper.getPool();
 		Jedis jedis = pool.getResource();
 		try {
 			Set<String> volatileKeys = jedis.smembers("volatileKeys");
 			if (volatileKeys.size() > 0){
 				jedis.del(volatileKeys.toArray(new String[0]));
 			}
 			for (Player player : players) {
 				String playerId = player.getId();
 				//TODO use a herlp to build these keys
 				jedis.set("player:" + playerId + ":name", player.getName());
 				for (String position : 	player.getPositionsAsSet()) {
 					String positionKey = "position:" + position + ":members";
 					jedis.sadd(positionKey, playerId);
 					jedis.sadd("volatileKeys", positionKey);			
 				}
 				for (String nightAvailable : 	player.getNightsAvailableAsSet()) {
 					String nightAvailableKey = "nightAvailable:" + nightAvailable + ":members";
 					jedis.sadd(nightAvailableKey, playerId);
 					jedis.sadd("volatileKeys", nightAvailableKey);			
 				}
 			}
 			return true;
 		}
 		finally {
 			pool.returnResource(jedis);
 		}
 	}
 
 	public static String[] getPitchersOnWednesday() {
 		JedisPool pool = RedisHelper.getPool();
 		Jedis jedis = pool.getResource();
 		Set<String> players = jedis.sinter("position:Pitcher:members", "nightAvailable:Wednesday:members");
 		HashSet<String> playerNames = new HashSet<String>();
 		for (String playerId : players) {
 			playerNames.add(jedis.get("player:"+playerId+":name"));	
 		}		
 		return playerNames.toArray(new String[0]);
 	}
 
 	private List<Player> getAllPlayersFromSFDC() {
 		//get the authentication from the session
 		//ForceApi api = AuthHelper.getPrivateForceApi();
 		QueryResult<Player> results = 	
 			api.query(PLAYER_QUERY_ROOT + " where Willing_to_Substitute__c = true", Player.class);
 		if (results != null) {
 			return results.getRecords();
 		}
 		else {
 			return null;
 		}
 	}
 
 	//get from cache, for build it up from SFDC
 	private Map<String,Player> getAllPlayersMap() {
 		//TODO maybe use getOrElse cache function
 		Map<String,Player> allPlayersMap = (Map<String,Player>)play.cache.Cache.get(ALL_PLAYERS_CACHE_KEY);;
 		if (allPlayersMap != null) {
 			return allPlayersMap;
 		}
 		else {
 			allPlayersMap = new HashMap<String,Player>();
 			List<Player> players = getAllPlayersFromSFDC();
 			for(Player p : players) {
 				allPlayersMap.put(p.id,p);
 			}
 			play.cache.Cache.set(ALL_PLAYERS_CACHE_KEY,allPlayersMap);
 			//also index the players in redis for searching
 			indexPlayers(players);
 			return allPlayersMap;
 		}
 	}
 }
