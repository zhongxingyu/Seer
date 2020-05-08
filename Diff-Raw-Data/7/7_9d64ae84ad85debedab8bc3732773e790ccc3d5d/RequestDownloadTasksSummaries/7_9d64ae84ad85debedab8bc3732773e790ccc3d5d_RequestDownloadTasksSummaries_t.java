 package tasktracker.model.elements;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import com.google.gson.Gson;
 
 import android.content.Context;
 import android.widget.Toast;
 import tasktracker.controller.DatabaseAdapter;
 import tasktracker.model.AccessURL;
 import tasktracker.model.NetworkRequestModel;
 import tasktracker.model.Preferences;
 
 /*
  * Creates an object to add a Task to Crowdsourcer when passed to ReadFromURL, THEN adds
  * the given task to the local SQL database with Crowdsourcer's returned ID.
  * 
  * Run by creating an instance.
  */
 public class RequestDownloadTasksSummaries implements NetworkRequestModel {
 	private Context context;
 	//private User user;
 	private String requestString;
 
     static final Gson gson = new Gson();
     /** index of 'content' for objects in database */
     
 	public RequestDownloadTasksSummaries(Context contex){
 		context = contex;
 		//user = use;
         //String content = gson.toJson(user);
         //http://crowdsourcer.softwareprocess.es/F12/CMPUT301F12T08/?action=list
         String command = "action=" + "list";
 
         requestString = AccessURL.turnCommandIntoURL(command);
     
 		AccessURL access = new AccessURL(this);
 		access.execute(getCrowdsourcerCommand());
 	}
 	
 	public Context getContext(){
 		return context;
 	}
 	
 	public String getCrowdsourcerCommand(){
 		//System.out.println("Request to network: " + requestString);
 		return requestString;
 	}
 	
 	public void runAfterExecution(String line){
     	DatabaseAdapter _dbHelper = new DatabaseAdapter(context);
 		// Add to SQL server
 		_dbHelper.open();
 		
 		Task task = new Task("null");
 		String taskName, description, date, creatorID, likes, content, id = "";
		boolean requiresPhoto, requiresText;
 		int pos = 0;
 		
 		//Get all of the Tasks from the downloaded string
 		while(pos < line.length()){
		    requiresPhoto = false;
		    requiresText = false;
 			pos = line.indexOf("{\"summary\":\"", pos);
 			if (pos == -1) break;
 			pos = pos + "{\"summary\":\"".length();
 			
 			taskName = AccessURL.getTag("<Task>", line, pos);
 			
 			if (taskName != null){
 				//Task found, parse for its summary...
 				creatorID = AccessURL.getTag("<CreatorID>", line, pos);
 				description = AccessURL.getTag("<Description>", line, pos);
 				date = AccessURL.getTag("<Date>", line, pos);
 				likes = AccessURL.getTag("<Likes>", line, pos);
 				//content = AccessURL.getTag("<Content>", line, pos);
 				id = AccessURL.getTag(",\"id\":\"", line, pos);
 				if ("true".equalsIgnoreCase(AccessURL.getTag("<RequiresPhoto>", line, pos)))
 				    requiresPhoto = true;
 				if ("true".equals(AccessURL.getTag("<RequiresText>", line, pos))) 
 				    requiresText = true;
 				//Create object...
 				if (creatorID != null
 					&& description != null
 					&& date != null
 					&& likes != null){
 					//TODO retrieve the user's name via their user ID from the Crowdsourcer users list
 					task = new Task(creatorID);
 					task.setName(taskName);
 					task.setDescription(description);
 					task.setID(id);
 					task.setIsDownloaded("No");
 					task.setDate(date);
 					task.setPhotoRequirement(requiresPhoto);
 					task.setTextRequirement(requiresText);
 					//TODO set likes
					task.setLikes(Integer.parseInt(likes));
 					//TODO set other members? Is this relevent for a downloaded task?
 					task.setOtherMembers("");
 					task.setIsPrivate(false);
 					//Add to local SQL database
 					addNewTask(task, task.getCreator(), _dbHelper);
 				}
 				//break;
 			}
 		}
 		//Toast toast = Toast.makeText(context, "Last item found at " + pos + ", it starts with " + task.getDescription(), Toast.LENGTH_SHORT);
 		//toast.show();
 		Toast toast = Toast.makeText(context, "Downloaded tasks from online", Toast.LENGTH_SHORT);
 		toast.show();
 		
 		//TODO refresh current page (TaskListView?)
 		
 		_dbHelper.close();
 	}
 	
     private void addNewTask(Task task, String _user, DatabaseAdapter _dbHelper){
 		List<String> others = task.getOtherMembers();
 		// Add to SQL server
 		_dbHelper.open();
 		//long taskID = _dbHelper.createTask(task);
 		_dbHelper.createTask(task);
 		
 		String taskName = task.getName();
 		String message = Notification.getMessage(_user, taskName,
 				Notification.Type.InformMembership);
 	
 		_dbHelper.createMember(task.getID(),
 				Preferences.getUsername(context));
 	
 		for (String member : others) {
 			_dbHelper.createMember(task.getID(), member);
 			_dbHelper.createNotification(task.getID(), member, message);
 		}
 	}
     
     /*
      * Puts the new user into the SQL table with the crowdsourcer ID as the user ID
      */
     private void addUserToSQL(String line){
     	DatabaseAdapter _dbHelper = new DatabaseAdapter(context);
 		// Add to SQL server
 		_dbHelper.open();
 		
 		//TODO: Put the user object in the database with the ID from crowdsourcer
 
 		//_dbHelper.createUser(username, email, password);
 		Toast toast = Toast.makeText(context, "Downloaded task summaries", Toast.LENGTH_SHORT);
 		toast.show();
 		
 		_dbHelper.close();
     }
 }
