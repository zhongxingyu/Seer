 package Model;
 
 import java.util.ArrayList;
 
 public class DataContainer {
 
 	static ArrayList<User> users;
 	static ArrayList<TaskProto> taskprotos;
 	static ArrayList<Task> tasks;
 	static ArrayList<Group> groups;
 
 	public DataContainer() {
 		users = new ArrayList<User>();
 		taskprotos = new ArrayList<TaskProto>();
 		tasks = new ArrayList<Task>();
 	}
 
 	public static void recieveMessage(String returnstring){
 		String[] breakcommand = returnstring.split("COMMANDEND");
 		if(breakcommand[0].equals("tasklist")){
			if(breakcommand.length == 1){
				
			}
			else{
				DataContainer.recieveTaskList(breakcommand[1]);
			}
 		}
 		else if(breakcommand[0].equals("groupinfo")){
 			String[] breakname = breakcommand[1].split("GNAME");
 			DataContainer.recieveGroupInfo(breakname[0], breakname[1]);
 		}
 		
 	}
 	
 	
 	public static void recieveTaskList(String tl) {
 		String[] first = tl.split("\\|");
 		for (int i = 0; i < first.length; i++) {
 			String thing = first[i];
 			String[] stuffs = thing.split("\\.");
 			// System.out.println(stuffs.length);
 
 			if (stuffs.length == 5) {
 
 				boolean notthere = true;
 				for (int j = 0; j < tasks.size(); j++) {
 
 					if (stuffs[0].equals(tasks.get(j).getutid())) {
 						notthere = false;
 
 					}
 				}
 				if (notthere) {
 					Task res = new Task(Integer.parseInt(stuffs[0]), stuffs[1],
 							stuffs[2], stuffs[3], stuffs[4]);
 					// System.out.println("got a task");
 					tasks.add(res);
 				}
 
 			} else {
 			}
 		}
 
 		System.out.println(tasks.size());
 	}
 	
 	public static boolean userExists(String username){
 		for(int i = 0; i < DataContainer.users.size(); i++){
 			if(DataContainer.users.get(i).getname() == username);
 			return true;
 		}
 		return false;
 	}
 	public static void recieveGroupInfo(String ginfo, String gname) {
 		String[] parts1 = ginfo.split("LEADERS:");
 		String[] parts2 = parts1[0].split("\\|");
 		String[] parts3 = parts1[1].split("\\|");
 
 		ArrayList<User> groupusers = new ArrayList<User>();
 		ArrayList<User> groupleaders = new ArrayList<User>();
 
 		for(int i = 0; i < parts2.length; i++){
 			String[] userparts = parts2[i].split(",");
 			if(userparts.length == 4){
 				User newuser = new User(userparts[0], userparts[1], userparts[2], Integer.parseInt(userparts[3]));
 				DataContainer.addUser(newuser);
 			}
 			else{
 				System.out.print("ERROR");
 			}
 		}
 		for(int i = 0; i < parts3.length; i++){
 			String[] userparts = parts3[i].split(",");
 			if(userparts.length == 4){
 				User newuser = new User(userparts[0], userparts[1], userparts[2], Integer.parseInt(userparts[3]));
 				DataContainer.addUser(newuser);
 			}
 			else{
 				System.out.print("ERROR");
 			}
 		}
 		
 		Group newgroup = new Group(gname, groupusers, groupleaders);
 		DataContainer.groups.add(newgroup);
 		
 	}
 
 	public static ArrayList<Task> getTasksByUname(String uname) {
 		ArrayList<Task> ret = new ArrayList<Task>();
 		for (Task t : tasks) {
 			if (t.getuser().equals(uname)) {
 				ret.add(t);
 			}
 		}
 		return ret;
 	}
 
 	public static void recieveUserList(String ul) {
 
 	}
 
 	public static ArrayList<Task> getTasks() {
 		return tasks;
 	}
 
 	public static void addUser(User u) {
 		users.add(u);
 	}
 
 	public static void addTaskProto(TaskProto tp) {
 		taskprotos.add(tp);
 	}
 
 	public static void addTask(Task t) {
 		tasks.add(t);
 	}
 
 	public static ArrayList<Task> getListByDay(String day) {
 		ArrayList<Task> stuffs = new ArrayList<Task>();
 		for (Task t : tasks) {
 			if (t.getTime().contains(day)) {
 				stuffs.add(t);
 			}
 		}
 		return stuffs;
 	}
 
 	public static ArrayList<Task> getTasksByGroup(String group) {
 		ArrayList<Task> ret = new ArrayList<Task>();
 		if(group.equals("All groups")){
 			return DataContainer.tasks;
 		}
 		for (Task t : tasks) {
 			if (t.getGroup().equals(group)) {
 				ret.add(t);
 			}
 		}
 		return ret;
 	}
 
 	public static ArrayList<Task> getTasksbyGroupandDay(String group, String day) {
 		ArrayList<Task> ret = new ArrayList<Task>();
 		for (Task t : tasks) {
 			if (t.getGroup().equals(group) && t.getTime().contains(day)) {
 				ret.add(t);
 			}
 		}
 		return ret;
 	}
 }
