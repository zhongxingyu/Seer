 package controllers;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import utils.Utils;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.MongoClient;
 import com.mongodb.MongoException;
 import com.mongodb.util.JSON;
 
 public class Application extends Controller {
 
 	private final static boolean XMPP_USER_REGISTRATION = true; 
 
 	
 
 	/**
 	 * GET /runs
 	 * @return
 	 */
 	public static Result getAllRuns() {
 		List<String> runs = new ArrayList<String>();
 		try {
 			DBCollection people = new MongoClient().getDB("roster").getCollection("people");
 			DBCursor allPeople = people.find();
 			try {
 				while (allPeople.hasNext()) {
 					String runName = allPeople.next().get("run").toString();
 					if (runName!=null && !runName.isEmpty() && !runs.contains(runName))
 						runs.add(runName);
 				}
 			} finally {
 				allPeople.close();
 			}
 		} catch (Exception e) {
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to connect to DB").toString()).as("application/json");
 		}
 		// Assemble runs and send out
 		BasicDBList runs_json = new BasicDBList();
 		for(String run : runs)
 			runs_json.add(new BasicDBObject("_id", run));
 		BasicDBObject data = new BasicDBObject("runs", runs_json);
 		return ok(new BasicDBObject("status", "success").append("data", data).toString()).as("application/json");
 
 	}
 
 	
 
 	/**
 	 * GET /runs/:run
 	 * @param run
 	 * @return
 	 */
 	public static Result getPeopleInRun(String run) {
 		BasicDBList roster = new BasicDBList();
 		try {
 			DBCollection people = new MongoClient().getDB("roster").getCollection("people");
 			DBCursor peopleInRun = people.find(new BasicDBObject("run", run), new BasicDBObject("run", 0));
 			try {
 				while (peopleInRun.hasNext()) {
					BasicDBObject p = (BasicDBObject) peopleInRun.next();
					Utils.stripRunFrom_id(p);
					roster.add(p);
 				}
 			} finally {
 				peopleInRun.close();
 			}
 		} catch (Exception e) {
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to connect to DB").toString()).as("application/json");
 		}
 		// Assemble the roster and send out
 		BasicDBObject data = new BasicDBObject("roster", roster); 
 		return ok(new BasicDBObject("status", "success").append("data", data).toString()).as("application/json");
 	}
 	
 	
 	
 	/**
 	 * GET /runs/:run/:person
 	 * @param run
 	 * @param person
 	 * @return
 	 */
 	public static Result getPersonInRun(String run, String person) {
 		BasicDBObject personInRun =  null;
 		try {
 			DBCollection people = new MongoClient().getDB("roster").getCollection("people");
 			personInRun = (BasicDBObject) people.findOne(new BasicDBObject("_id", person));
 			if (personInRun==null) 
 				return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("_id", "This person doesn't exist in this run")).toString()).as("application/json");
 			if (!personInRun.getString("run").equals(run))
 				return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("run", "Invalid run name")).toString()).as("application/json");
 		} catch (Exception e) {
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to connect to DB").toString()).as("application/json");
 		}
 		// Assemble the response and send out 
 		return ok(new BasicDBObject("status", "success").append("data", new BasicDBObject("person", personInRun)).toString()).as("application/json");
 	}
 
 	
 	
 	/**
 	 * POST /runs/:run
 	 * @param run
 	 * @return
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result addPersonToRun(String run) {
 		// Parse and validate
 		JsonNode body = request().body().asJson(); 
 		if (body==null)
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("message", "'Content-type' header needs to be 'application/json'")).toString()).as("application/json");
 		BasicDBObject json_body = (BasicDBObject) JSON.parse(body.toString());
 		if (json_body.get("_id")==null)
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("_id", "You need to specify at least this field")).toString()).as("application/json");
 		String nick =  json_body.get("_id").toString();
 		if (nick.isEmpty())
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("_id", "You need to specify at least this field")).toString()).as("application/json");
 		// Alter DB object
 		json_body.append("run", run);
 		Utils.appendRunTo_id(json_body, run);
 		// Store in DB
 		DBCollection people = null;
 		try {
 			people = new MongoClient().getDB("roster").getCollection("people");
 		} catch (UnknownHostException e) {
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to connect to DB").toString()).as("application/json");
 		}
 		try {
 			people.insert(json_body);
 		} catch (MongoException e) {
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("message", "A user with this '_id' already exists, pick a different one")).toString()).as("application/json");
 		}
 		// Create XMPP user (if plugin is active)
 		if (XMPP_USER_REGISTRATION) 
 			return OpenfireXMPPUserRegistration.registerXMPPUser(json_body);
 		Utils.stripRunFrom_id(json_body);
 		return ok(new BasicDBObject("status", "success").append("data", new BasicDBObject("person", json_body)).toString()).as("application/json");
 	}
 
 
 	
 	/**
 	 * DELETE /runs/:run/:person
 	 * @param run
 	 * @param person
 	 * @return
 	 */
 	public static Result deletePersonInRun(String run, String person) {
 		// Delete from DB
 		try {
 			DBCollection people = new MongoClient().getDB("roster").getCollection("people");
 			BasicDBObject p = new BasicDBObject("_id", person);
 			Utils.appendRunTo_id(p, run);
 			people.remove(p);
 		} catch (Exception e) {
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to connect to DB").toString()).as("application/json");
 		}
 		// Delete XMPP user (if plugin is active)
 		if (XMPP_USER_REGISTRATION) 
 			OpenfireXMPPUserRegistration.unregisterXMPPUser(person);
 		return ok(new BasicDBObject("status", "success").append("data", null).toString()).as("application/json");	
 	}
 
 
 	
 	/**
 	 * PUT /runs/:run/:person
 	 * @param run
 	 * @param person
 	 * @return
 	 */
 	public static Result updatePersonInRun(String run, String person) {
 		// Parse and validate
 		JsonNode body = request().body().asJson(); 
 		if (body==null)
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("message", "'Content-type' header needs to be 'application/json'")).toString()).as("application/json");
 		BasicDBObject json_body = (BasicDBObject) JSON.parse(body.toString());
 		if (json_body.get("_id")==null)
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("_id", "You need to specify at least this field")).toString()).as("application/json");
 		String nick =  json_body.get("_id").toString();
 		if (nick.isEmpty())
 			return badRequest(new BasicDBObject("status", "fail").append("data", new BasicDBObject("_id", "You need to specify at least this field")).toString()).as("application/json");
 		// Store changes in DB
 		DBCollection people = null;
 		BasicDBObject person_before_update = null;
 		BasicDBObject p = new BasicDBObject("_id", nick);
 		Utils.appendRunTo_id(p, run);
 		Utils.appendRunTo_id(json_body, run);
 		try {
 			people = new MongoClient().getDB("roster").getCollection("people");
 			person_before_update = (BasicDBObject) people.findOne(new BasicDBObject("_id", nick));
 			people.update(p, json_body.append("run", run), true, false);
 		} catch (UnknownHostException e) {
 			return internalServerError(new BasicDBObject("status", "error").append("message", "Impossible to connect to DB").toString()).as("application/json");
 		}
 		// Create XMPP user (if plugin is active)
 		if (XMPP_USER_REGISTRATION)
 			if (person_before_update==null)
 				return OpenfireXMPPUserRegistration.registerXMPPUser(json_body);
 		Utils.stripRunFrom_id(json_body);
 		return ok(new BasicDBObject("status", "success").append("data", new BasicDBObject("person", json_body)).toString()).as("application/json");
 	}
 
 }
