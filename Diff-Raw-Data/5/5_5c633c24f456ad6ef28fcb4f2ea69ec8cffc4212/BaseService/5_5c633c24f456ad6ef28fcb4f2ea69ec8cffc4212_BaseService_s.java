 package controllers;
 
 import play.Play;
 import play.mvc.Controller;
 import controllers.response.Ok;
 import controllers.response.TodoMal;
 import flexjson.JSONSerializer;
 
 public class BaseService extends Controller {
 	
 	public static final String CHUNK_SEPARATOR = Play.configuration.getProperty("chunk.separator");
 	public static final String CHUNK_FOR_REGISTER_SEPARATOR = Play.configuration.getProperty("chunk.registration.separator");
 	public static JSONSerializer serializer = new JSONSerializer();
 	
 	static {
 		
 	}
 	
 	protected static void jsonOk(Object obj) {
		String result = serializer.serialize(new Ok(obj));
 		play.Logger.info("result: %s", result);
		renderJSON(serializer.serialize(obj));
 	}
 	
 	protected static void jsonError(Object obj) {
 		renderJSON(serializer.serialize(new TodoMal(obj)));
 	}
 
 }
