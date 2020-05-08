 package com.delegator;
 
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.LinkedList;
 import java.util.List;
 
import org.json.JSONException;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import android.util.Log;
 
 public class InOutHelper {
 	/**
      * Object to JSON Object
      * TODO private String writeJSON(List<Task> taskList)
      * @author NNMN
      */
 	public static void writeJSON(Task currentTask, String file){
 		JSONObject loadedFile = (JSONObject) loadJSON(file);
 		 
     	JSONObject taskToFile = new JSONObject();
     	JSONArray list1 = new JSONArray();
     	JSONArray list2 = new JSONArray();
 
 		taskToFile.put("title", currentTask.title);
 		taskToFile.put("description", currentTask.description);
 		taskToFile.put("deadline", currentTask.deadline);
 		taskToFile.put("estimatedTime", currentTask.estimatedTime);
 		taskToFile.put("category", currentTask.category);
 		taskToFile.put("finished", currentTask.finished);
 		list1.add(0);
 		taskToFile.put("collaboratorTime", list1);
 		//list1.clear();
 		list2.add(DelegatorActivity.localUser.email);
 		taskToFile.put("collaborator", list2);
 		Log.w("WriteJSON", "JSON String created");
 		
 		JSONObject jsonTasksObject;
 	
 		
 		Log.w("WriteJSON", "JSON Loaded");
 		int size;
 		
 		if(loadedFile == null || loadedFile.get("tasks") == null) {
 			Log.w("WriteJSON", "No Old tasks");
 			jsonTasksObject = new JSONObject();
 			jsonTasksObject.put("task1", taskToFile);			
 		} else {
 			Log.w("WriteJSON", "Adding tasks");
 			Object tasksObject = loadedFile.get("tasks");
 			jsonTasksObject = (JSONObject) tasksObject;
 			size = jsonTasksObject.size() + 1;
 			jsonTasksObject.put("task" + size , taskToFile);
 		}
 		JSONObject tasksToFile = new JSONObject();
 		
 		tasksToFile.put("tasks", jsonTasksObject);
 		Log.w("WriteJSON", "meta JSON String created");
 		try {
 			FileWriter fileOut = new FileWriter(file);
 			fileOut.write(tasksToFile.toJSONString());
 			fileOut.flush();
 			fileOut.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Log.w("WriteJSON", "JSON file saved");
 		///Log.w("WriteJSON", taskToFile.toString());
 		//return taskToFile.toString();
     	 
     }
 	
 	
 	
 	static JSONObject loadJSON(String file) {
 		
 		try {
 			FileReader fileIn = new FileReader(file);
 			JSONParser parser = new JSONParser();
 		
 			Object obj;
 			//Log.w("LoadJSON", file.toString());
 			obj = parser.parse(fileIn);
 			fileIn.close();
 			JSONObject jsonObject = (JSONObject) obj;
 			Log.w("LoadJSON", "JSON String loaded");
 			Log.w("LoadJSON", jsonObject.toString());
 			return jsonObject;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			
 			Log.w("LoadJSON", "no valid tasks in file found");
 			e.printStackTrace();
 			
 			return null;
 		} 
 		
 	}
 	
 
 
 	public static List<Task> loadToTask(String file) {
 		JSONObject json = File2JSON(file);
 		JSONObject jsonTasksObject = (JSONObject) json.get("tasks");
 		Log.w("Load2TaskLOOP", jsonTasksObject.toString());
 		
 		List<Task> taskList = new ArrayList<Task>();
 		for (int i = 1; i <= jsonTasksObject.size(); i++) {
 			JSONObject obj = (JSONObject) jsonTasksObject.get("task" + i);
 			Task t = new Task(obj.get("title").toString(), DelegatorActivity.localUser);
 			t.description = obj.get("description").toString();
 			t.estimatedTime = Integer.parseInt(obj.get("estimatedTime").toString());
 			t.category = Integer.parseInt(obj.get("category").toString());
 			t.finished = Boolean.parseBoolean(obj.get("category").toString());
 			String delims = "[\\[\\]]";
 			int[] colT = {Integer.parseInt(obj.get("collaboratorTime").toString().split(delims)[1])};
 			t.collaboratorTime = colT;
 
 			taskList.add(t);
 		}
 		return taskList;
 	}
 	
 
 	public static void removeJSON(Task currentTask, String file) {
 		
 		try {
 			JSONObject obj = File2JSON(file);
 			JSONObject newTasks = new JSONObject();
 			JSONObject tasks = (JSONObject) obj.get("tasks");
 			
 			int j = 1;
 			boolean found = false;
 			int size = tasks.size();
 			Object obt;
 			for(int i = 1; i <= size; i++) {
 				JSONObject item = (JSONObject) tasks.get("task" + i);
 				
 				String title = (String) item.get("title");
 				String titleT = currentTask.title;
 				
 				if (found){
 					obt = tasks.remove("task"+i);
 					int num = i-1;
 					tasks.put("task" + num, obt);
 				}
 				if(!found && title.equals(titleT)){
 					obt = tasks.remove("task"+i);
 					found = true;
 				} 
 			}
 			newTasks.put("tasks", tasks);
 
 			FileWriter fileOut = new FileWriter(file);
 			fileOut.write(newTasks.toJSONString());
 			fileOut.flush();
 			fileOut.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	public static void updateJSON(Task currentTask, String file) {
 		
 		try {
 			
 			JSONObject newTasks = new JSONObject();
 			JSONObject tasks = (JSONObject) loadJSON(file).get("tasks");
 			Log.w("updateJSON1", tasks.toJSONString());
 			int j = 1;
 			boolean found = false;
 			int size;
 			size = tasks.size();
 			for(int i = 1; i <= size; i++) {
 				JSONObject item = (JSONObject) tasks.get("task" + i);
 				Log.w("updateJSON2", item.toJSONString());
 				String title = (String) item.get("title");
 				String titleT = currentTask.title;
 				Log.w("updateJSON2a", title);
 				Log.w("updateJSON2b", titleT);
 				Object obt;
 				if(title.equals(titleT)){
 					obt = tasks.remove("task"+i);
 					JSONObject obt2 = (JSONObject) obt;
 					obt2.remove("finished");
 					obt2.remove("description");
 					obt2.remove("estimatedTime");
 					obt2.remove("collaboratorTime");
 					obt2.put("finished", currentTask.finished);
 					obt2.put("description", currentTask.description);
 					obt2.put("estimatedTime", currentTask.estimatedTime);
 					JSONArray list1 = new JSONArray();
 					list1.add(currentTask.collaboratorTime[0]);
 					obt2.put("collaboratorTime", list1);
 					
 					
 					
 					Log.w("updateJSON", obt2.toString());
 					tasks.put("task"+i, obt2);
 					break;
 				} 
 				Log.w("updateJSON3", tasks.toJSONString());
 			}
 			
 			newTasks.put("tasks", tasks);
 			FileWriter fileOut = new FileWriter(file);
 
 			fileOut.write(newTasks.toJSONString());
 			fileOut.flush();
 			fileOut.close();
 			Log.w("updateJSON4", newTasks.toJSONString());
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 	
 	public static void JSON2File(String file, JSONObject json){
 		try {
 			FileWriter fileOut = new FileWriter(file);
 			fileOut.write(json.toJSONString());
 			fileOut.flush();
 			fileOut.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static JSONObject File2JSON(String file) {
 		try {
 			FileReader fileIn = new FileReader(file);
 			JSONParser parser = new JSONParser();
 			JSONObject jsonObject = (JSONObject) parser.parse(fileIn);
 			fileIn.close();
 			return jsonObject;
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		} 
 	}
 }
