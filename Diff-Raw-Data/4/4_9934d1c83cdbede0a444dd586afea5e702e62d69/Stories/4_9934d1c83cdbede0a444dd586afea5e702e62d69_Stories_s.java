 package controllers;
 
 import java.util.List;
 
 import models.Story;
 import models.Task;
 import play.modules.objectify.ObjectifyModel;
 import play.mvc.Controller;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.googlecode.objectify.Key;
 
 public class Stories extends Controller{
 	
 	public static void getAll(){
 		List<Story> findAll = Story.findAll();
 		renderJSON(findAll);
 	}
 	
 	
 	public static void save(String json){
 		JsonElement fromJson = new JsonParser().parse(json.trim());
 		processArrayOfElements(fromJson);
 	}
 	
 	
 	
 	private static void processArrayOfElements(JsonElement fromJson){
 		if (fromJson.isJsonArray()){
 			JsonArray asJsonArray = fromJson.getAsJsonArray();
 			for (JsonElement jsonElement : asJsonArray) {
 				processElement(jsonElement);
 			}
 		} else {
 			processElement(fromJson);
 		}
 	}
 	
 	
 	private static void processElement(JsonElement jsonElement){
 		if (jsonElement.isJsonObject()){
 			JsonObject jsonObject = jsonElement.getAsJsonObject();
 			Story story = new Gson().fromJson(jsonObject, Story.class);
 			JsonElement deleted = jsonObject.get("deleted");
 			if (deleted != null && deleted.getAsBoolean()){
 				story.delete();
 			} else {
 				Key<Story> storyKey = story.save();
 				JsonElement tasks = jsonObject.get("tasks");
 				if (tasks != null){
 					if (tasks.isJsonArray()){
 						JsonArray tasksArray = tasks.getAsJsonArray();
 						for (JsonElement taskElement : tasksArray) {
 							processTaskElement(taskElement,storyKey);
 						}
 					}else{
 						processTaskElement(tasks,storyKey);
 					}
 				}
 			}
 		}
 		
 	}
 	
 	
 	private static void processTaskElement(JsonElement taskElement, Key<Story> storyKey){
 		if (taskElement.isJsonObject()){
 			JsonObject taskJsonObject = taskElement.getAsJsonObject();
 			Task task = new Gson().fromJson(taskJsonObject, Task.class);
 			JsonElement taskDeleted = taskJsonObject.get("deleted");
 			if (taskDeleted != null && taskDeleted.getAsBoolean()){
 				task.delete();
 			} else {
				task.story = storyKey;
 				task.save();
 			}
 		}
 	}
 	
 	
 	
 
 }
