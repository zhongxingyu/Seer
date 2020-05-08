 package controllers;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import com.avaje.ebean.Ebean;
 
 public class Event extends Controller {
 	
 	public static Result show(long id) {
 		return TODO;
 	}
 
 	public static Result index() {
 		List<models.Event> events = models.Event.findByProducerId(1, 10);
 		models.Event.find.fetch("sheep.producer");
 		for (models.Event event : events) {
 			event.sheep.producer = null;
 			event.sheep.events = null;
 		}
 		return ok(Json.toJson(events));
 	}
 
 	public static Result alarms() {
 		return TODO;
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
	public static Result recentAlarms(Integer num) {
 		List<models.Event> events = models.Event.findByProducerId(Long.valueOf(session("producerId")), num);
 		ObjectMapper mapper = new ObjectMapper();
 		StringWriter writer = new StringWriter();
 		try {
 			mapper.writeValue(writer, events);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ok(Json.toJson(events));
 	}
 
 	public static Result recentExceptions(Long num) {
 		return TODO;
 	}
 
 }
