 package controllers;
 
 import static play.libs.Json.toJson;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonNode;
 import models.SGroup;
 import models.SUser;
 import play.libs.Json;
 import play.mvc.*;
 
 
 /**
  * @author Muhammad Fahied
  */
 
 
 
 public class SGroups extends Controller {
 	
 	/*
 	 * 
 	 * Authentication Services
 	 * 
 	 * */
 	
 
 public static Result GetGroupInfo()
   {
 	  	  //runId is hardcoded as there will be only one run
 		  final int runId = 3;
 		  List<SGroup> groups = SGroup.find.filter("runId", runId).asList();
 		  
		  List<Map<String, String>>  l1 = new LinkedList<Map<String, String>>();
 		 
 		  for(SGroup g : groups)
 		  {
			  Map<String, String> metaInfo = new HashMap<String, String>();
 			  metaInfo.put("id", g.id.toString());
 			  metaInfo.put("name", g.name);
 			  l1.add(metaInfo);
 		  }
 			
 			JsonNode node = Json.toJson(l1);
 			return ok(toJson(node));
 		  
   }
 		  
 	
 	
 	  public static Result connect(String groupId, String password) {
 		  
 		  if(SGroup.find.filter("id", groupId).filter("password", password).get() == null)
 		  return status(401, "Not Authorized");
 		  else {
 			  return status(200, "OK");
 		}
 		  
 		  
 		  
 		  /*  HTTP STatus Codes
 		   * public static final int OK = 200;
 		  public static final int CREATED = 201;
 		  public static final int ACCEPTED = 202;
 		  public static final int PARTIAL_INFO = 203;
 		  public static final int NO_RESPONSE = 204;
 		  public static final int MOVED = 301;
 		  public static final int FOUND = 302;
 		  public static final int METHOD = 303;
 		  public static final int NOT_MODIFIED = 304;
 		  public static final int BAD_REQUEST = 400;
 		  public static final int UNAUTHORIZED = 401;
 		  public static final int PAYMENT_REQUIERED = 402;
 		  public static final int FORBIDDEN = 403;
 		  public static final int NOT_FOUND = 404;
 		  public static final int INTERNAL_ERROR = 500;
 		  public static final int NOT_IMPLEMENTED = 501;
 		  public static final int OVERLOADED = 502;
 		  public static final int GATEWAY_TIMEOUT = 503;
 		   * 
 		   * 
 		   * */
 	    }
 	
 	
 	/*
 	 * 
 	 * Group Services
 	 * 
 	 * */
 	
 	public static Result fetchAllGroups() {
 
 		List<SGroup> groups = SGroup.find.asList();
 		
 //	     JSONSerializer modelSerializer = new JSONSerializer()
 //	     .include("name","_id","susers","susers.name","susers.id").exclude("*"); 
 //	    String text = modelSerializer.serialize(groups);
 	    response().setContentType("application/json");
 	     
 		return ok(toJson(groups));
 	}
 	
 	/* POST :  JSON Request
 	 
 	{
 		"name":"Group 1",
 		"runId":1
 	}
 	
 	*/
 	
 	public static Result createGroup() {
 		
 		//parse JSON from request body
     	JsonNode node =  ctx().request().body().asJson();
     	
     	String name = node.get("name").asText();
     	String password = node.get("password").asText();
     	int runId = node.get("runId").asInt();
 		
     	SGroup group = new SGroup(name,password, runId);
 		group.save();
 		// producing customized JSON response
 		//SGroup cGroup = group.datastore.createQuery(SGroup.class).retrievedFields(true, "name").get();
 
 		return ok(toJson(group));
 	}
 	
 	
 	
 	public static Result fetchGroupById( String groupId) {
 		
 		SGroup group = SGroup.find.byId(groupId);
 		return ok(toJson(group));
 		
 	}
 	
 	
 	
 	
 	public static Result fetchGroupsByRunId( String runId) {
 		
 		int runid = Integer.parseInt(runId);
 		List<SGroup> groups = SGroup.find.field("runId").equal(runid).asList();
 		return ok(toJson(groups));
 		
 	}
 	
 	
 	/*
 	 * 
 	 * Member Services
 	 * 
 	 * */
 	public static Result fetchGroupMembers(String groupId) {
 		
 		SGroup group = SGroup.find.byId(groupId);
 		List<SUser> users = group.susers;
 		return ok(toJson(users));
 		
 	}
 	
 	/* POST : JSON request
 
 	{
 		"groupId":"4fe424f7da063acbfc99d734" , 
 		"name": "Fahied", 
 		"email":"anonymous@tmail.com", 
 		"age":25 
 	}
 	
 	*/
 	public static Result addMember() {
 		
 		//parse JSON from request body
     	JsonNode node =  ctx().request().body().asJson();
     	String groupId = node.get("groupId").asText();
     	String name = node.get("name").asText();
     	String email = node.get("email").asText();
     	int age = node.get("age").asInt();
     	
 		SGroup group = SGroup.find.byId(groupId);
     	
     	//SGroup group = SGroup.find.filter("_id", groupId).get();
     	
 		SUser user = new SUser(name, email, age);
 		user.save();
 		//group.addMember(user);
 		if (group.susers == null) {
 			group.susers = new ArrayList<SUser>();
 		}
 		group.susers.add(user);
 		group.save();
 		
 		response().setContentType("application/json");
 		return ok(toJson(user));
 	}
 
 }
