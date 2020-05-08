 /**
  * 
  */
 package ltg.ps.helioroom;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import ltg.commons.LTGEvent;
 import ltg.commons.LTGEventHandler;
 import ltg.commons.LTGEventListener;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.JsonNodeFactory;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.MongoClient;
 
 /**
  * @author tebemis
  *
  */
 public class HelioRoomMasterAgent {
 
 	private LTGEventHandler eh = null;
 	private DBCollection db;
 
 
 	public HelioRoomMasterAgent() {
 
 		// -------------------
 		// Init network and DB
 		// -------------------
 		eh =  new LTGEventHandler("hr-master@54.243.60.48", "hr-master", "helio-sp-13@conference.54.243.60.48");
 		try {
 			db = new MongoClient("localhost").getDB("helio-sp-13").getCollection("notes");
 		} catch (UnknownHostException e1) {
 			System.err.println("Impossible to connect to MongoDB");
 			System.exit(0);
 		}
 
 
 		// -----------------
 		//Register listeners
 		// -----------------
 		eh.registerHandler("init_helio", new LTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				eh.generateEvent("init_helio_diff", e.getOrigin(), getDBDiff(e.getPayload()));
 
 			}
 		});
 
 		eh.registerHandler("new_observation", new LTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				storeObservation(e.getOrigin(), e.getPayload());
 			}
 		});		
 
 		eh.registerHandler("remove_observation", new LTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				deleteObservation(e.getOrigin(), e.getPayload());
 			}
 		});
 
 		eh.registerHandler("new_theory", new LTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				storeTheory(e.getOrigin(), e.getPayload());
 			}
 		});
 
 		eh.registerHandler("delete_theory", new LTGEventListener() {
 			public void processEvent(LTGEvent e) {
 				deleteTheory(e.getOrigin(), e.getPayload());
 			}
 		});
 
 
 		// ------------------
 		// Run event listener
 		// ------------------
 		eh.runSynchronously();
 	}
 
 
 	protected JsonNode getDBDiff(JsonNode payload) {
 		// Put all items into notes in payload into a list
 		List<Note> clientNotes = new ArrayList<Note>();
		Iterator<JsonNode> i = payload.get("notes").elements();
		while (i.hasNext())
			clientNotes.add(new Note(i.next()));
 		// Put all items in database into a list
 		List<Note> dbNotes = new ArrayList<Note>();
 		DBCursor notes = db.find(new BasicDBObject("active", true));
 		while(notes.hasNext())
 			dbNotes.add(new Note((BasicDBObject) notes.next()));
 		// Additions: which notes are in the DB and not in the client?
 		List<Note> adds = new ArrayList<Note>(dbNotes);
 		adds.removeAll(clientNotes);
 		// Deletions: which notes are in the client and not in the DB?
 		List<Note> dels = new ArrayList<Note>(clientNotes);
 		dels.removeAll(dbNotes);
 		// Compose the response
 		ArrayNode additions = JsonNodeFactory.instance.arrayNode();
 		for (Note n: adds)
 			additions.add(n.serialize());
 		ArrayNode deletions = JsonNodeFactory.instance.arrayNode();
 		for (Note n: dels)
 			deletions.add(n.serialize());
 		ObjectNode response = JsonNodeFactory.instance.objectNode();
 		response.put("additions", additions);
 		response.put("deletions", deletions);
 		return response;
 
 	}
 
 
 	protected void storeObservation(String origin, JsonNode payload) {
 		// Create query based on id fields: type, origin, anchor, color
 		BasicDBObject query = 
 				new BasicDBObject("type", "observation")
 		.append("origin", origin)
 		.append("color", payload.get("color").textValue())
 		.append("anchor", payload.get("anchor").textValue());
 		BasicDBObject update_status_and_reason = new BasicDBObject("$set", new BasicDBObject("active", true)
 		.append("reason", payload.get("reason").textValue()));
 		BasicDBObject update_history = 
 				new BasicDBObject("$push", new BasicDBObject("history", new BasicDBObject("ts", new Date()).append("action", "created").append("reason", payload.get("reason").textValue())));
 		// Create if not existent, set active and set reason
 		db.update(query, update_status_and_reason, true, false);
 		// Updates history by inserting the same reason and time stamping it
 		db.update(query, update_history);
 	}
 
 
 	protected void deleteObservation(String origin, JsonNode payload) {
 		// Create query based on id fields: type, origin, anchor, color
 		BasicDBObject query = 
 				new BasicDBObject("type", "observation")
 		.append("origin", origin)
 		.append("color", payload.get("color").textValue())
 		.append("anchor", payload.get("anchor").textValue());
 		BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("active", false));
 		BasicDBObject update_history = 
 				new BasicDBObject("$push", new BasicDBObject("history", new BasicDBObject("ts", new Date()).append("action", "deleted")));
 		// Set as inactive
 		db.update(query, update);
 		// Update history by inserting action and timestamp
 		db.update(query, update_history);
 	}
 
 
 	protected void storeTheory(String origin, JsonNode payload) {
 		// Create query based on id fields: type, origin, anchor, color
 		BasicDBObject query = 
 				new BasicDBObject("type", "theory")
 		.append("origin", origin)
 		.append("color", payload.get("color").textValue())
 		.append("anchor", payload.get("anchor").textValue());
 		BasicDBObject update_status_and_reason = new BasicDBObject("$set", new BasicDBObject("active", true)
 		.append("reason", payload.get("reason").textValue()));
 		BasicDBObject update_history = 
 				new BasicDBObject("$push", new BasicDBObject("history", new BasicDBObject("ts", new Date()).append("action", "created").append("reason", payload.get("reason").textValue())));
 		// Create if not existent, set active and set reason
 		db.update(query, update_status_and_reason, true, false);
 		// Updates history by inserting the same reason and time stamping it
 		db.update(query, update_history);
 	}
 
 
 	protected void deleteTheory(String origin, JsonNode payload) {
 		// Create query based on id fields: type, origin, anchor, color
 		BasicDBObject query = 
 				new BasicDBObject("type", "theory")
 		.append("origin", origin)
 		.append("color", payload.get("color").textValue())
 		.append("anchor", payload.get("anchor").textValue());
 		BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("active", false));
 		BasicDBObject update_history = 
 				new BasicDBObject("$push", new BasicDBObject("history", new BasicDBObject("ts", new Date()).append("action", "deleted")));
 		// Set as inactive
 		db.update(query, update);
 		// Update history by inserting action and timestamp
 		db.update(query, update_history);
 	}
 
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {	
 		new HelioRoomMasterAgent();
 	}
 
 
 
 
 
 
 }
