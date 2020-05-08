 package edu.wm.werewolf.dao;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.geo.GeoResults;
 import org.springframework.data.mongodb.core.mapping.Document;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.NearQuery;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.core.query.Update;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 import edu.wm.werewolf.domain.GPSLocation;
 import edu.wm.werewolf.domain.Player;
 import edu.wm.werewolf.exceptions.NoPlayerFoundException;
 import edu.wm.werewolf.mongoDB.SpringMongoConfig;
 
 public class MongoPlayerDAO implements IPlayerDAO {
 	
 	@Autowired DB db;
 	
 //	private MongoOperations mongoOperation;
 //	
 //	public MongoPlayerDAO() throws UnknownHostException {
 //	    ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
 //	    mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
 //	}
 
 	@Override
 	public void createPlayer(Player player) {
 		DBCollection table = db.getCollection("players");
 		BasicDBObject documentDetail = new BasicDBObject();
 		documentDetail.put("id", player.getId());
 		documentDetail.put("lat", player.getLat());
 		documentDetail.put("lng", player.getLng());
 		documentDetail.put("userID", player.getUserId());
 		documentDetail.put("isDead", player.isDead());
 		documentDetail.put("isWerewolf", player.isWerewolf());
 	 
 		table.insert(documentDetail);
 	}
 	
 	@Override
 	public List<Player> getAllAlive() {	
 		DBCollection table = db.getCollection("players");
 		BasicDBObject query = new BasicDBObject("isDead", false);
 		DBCursor cursor = table.find(query);
 		List <Player> players = new ArrayList<>();
 		while (cursor.hasNext())
 		{
 			DBObject player = cursor.next();
 			Player alivePlayer = new Player((String)player.get("id"), (boolean)player.get("isDead"),
					(float)player.get("lat"), (float)player.get("lng"), (String)player.get("userID"),
 					(boolean)player.get("isWerewolf"));
 			players.add(alivePlayer);
 		}
 		return players;
 	}
 
 	@Override
 	public void setDead(Player p) {
 		DBCollection table = db.getCollection("players");
 		
 		BasicDBObject newDocument = new BasicDBObject();
 		newDocument.put("isDead", true);
 		
 		BasicDBObject searchQuery = new BasicDBObject().append("id", p.getId());
 		table.update(searchQuery, newDocument);
 	}
 	
 	@Override
 	public void setPlayerLocation (String id, GPSLocation loc) {
 		DBCollection table = db.getCollection("players");
 		
 		BasicDBObject newDocument = new BasicDBObject();
 		newDocument.put("lat", loc.getLatitude());
 		newDocument.put("lng", loc.getLongitude());
 		newDocument.put("lastUpdate", loc.getTime());
 		
 		BasicDBObject searchQuery = new BasicDBObject().append("id", id);
 		table.update(searchQuery, newDocument);
 	}
 
 	@Override
 	public Player getPlayerByID(String id) throws NoPlayerFoundException {
 		DBCollection table = db.getCollection("players");
 		BasicDBObject query = new BasicDBObject("id", id);
 		DBCursor cursor = table.find(query);
 		Player playerObject = null;
 		while (cursor.hasNext())
 		{
 			DBObject player = cursor.next();
 			playerObject = new Player((String)player.get("id"), (boolean)player.get("isDead"),
 					(float)player.get("lat"), (float)player.get("lng"), (String)player.get("userID"),
 					(boolean)player.get("isWerewolf"));
 		}
 		return playerObject;	
 	}
 
 	//TODO fix this
 	@Override
 	public List<Player> getAllNear(Player player) {
 		if (player.isWerewolf()) 
 		{
 			DBCollection table = db.getCollection("players");
 			List <Player> allPlayersNear = new ArrayList <Player>();
 			BasicDBObject locQuery = new BasicDBObject();
 			locQuery.put("loc", new BasicDBObject("$near", new Double[]{player.getLng(), player.getLat()}));
 			DBCursor locCursor = table.find( locQuery );
 			
 			while (locCursor.hasNext())
 			{
 				DBObject result = locCursor.next();
 				try {
 					if ((String)result.get("isDead") != "false") {
 						allPlayersNear.add(getPlayerByID((String)result.get("id")));
 					}
 				} catch (NoPlayerFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}	
 			return allPlayersNear;	
 		}
 		else 
 		{
 			return null;
 		}	
 	}
 	
 	@Override
 	public void reset()
 	{
 		DBCollection table = db.getCollection("players");
 		table.drop();
 	}
 
 	@Override
 	public void vote(Player player, Player votee) {
 		DBCollection table = db.getCollection("players");
 		
 		BasicDBObject newDocument = new BasicDBObject();
 		newDocument.put("votedAgainst", player.getVotedAgainst());
 		
 		BasicDBObject searchQuery = new BasicDBObject().append("id", player.getId());
 		table.update(searchQuery, newDocument);
 	}
 
 	@Override
 	public List<Player> getAllWerewolves() {
 		DBCollection table = db.getCollection("players");
 		BasicDBObject query = new BasicDBObject("isWerewolf", true);
 		DBCursor cursor = table.find(query);
 		List <Player> players = new ArrayList<>();
 		while (cursor.hasNext())
 		{
 			DBObject player = cursor.next();
 			Player werewolves = new Player((String)player.get("id"), (boolean)player.get("isDead"),
 					(float)player.get("lat"), (float)player.get("lng"), (String)player.get("userID"),
 					(boolean)player.get("isWerewolf"));
 			players.add(werewolves);
 		}
 		return players;
 	}
 
 	@Override
 	public List<Player> getAllTownspeople() {
 		DBCollection table = db.getCollection("players");
 		BasicDBObject query = new BasicDBObject("isWerewolf", false);
 		DBCursor cursor = table.find(query);
 		List <Player> players = new ArrayList<>();
 		while (cursor.hasNext())
 		{
 			DBObject player = cursor.next();
 			Player townies = new Player((String)player.get("id"), (boolean)player.get("isDead"),
 					(float)player.get("lat"), (float)player.get("lng"), (String)player.get("userID"),
 					(boolean)player.get("isWerewolf"));
 			players.add(townies);
 		}
 		return players;
 	}
 
 }
