 package com.theminequest.MineQuest.Quest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Editable.CertainBlockEdit;
 import com.theminequest.MineQuest.Editable.CoordinateEdit;
 import com.theminequest.MineQuest.Editable.Edit;
 import com.theminequest.MineQuest.Editable.InsideAreaEdit;
 import com.theminequest.MineQuest.Editable.ItemInHandEdit;
 import com.theminequest.MineQuest.Editable.OutsideAreaEdit;
 import com.theminequest.MineQuest.Target.TargetDetails;
 import com.theminequest.MineQuest.Tasks.Task;
 
 public class QuestParser {
 	
 	protected static void parseDefinition(Quest q) throws FileNotFoundException{
 		MineQuest.log(Level.WARNING, "8");
 		LinkedHashMap<Integer, String> tasks = new LinkedHashMap<Integer, String>();
 		LinkedHashMap<Integer, String> events = new LinkedHashMap<Integer, String>();
 		LinkedHashMap<Integer, TargetDetails> targets = new LinkedHashMap<Integer, TargetDetails>();
 		LinkedHashMap<Integer, Edit> editables = new LinkedHashMap<Integer,Edit>();
 		MineQuest.log(Level.WARNING, "9");
 		File f = new File(MineQuest.questManager.locationofQuests + File.separator + q.questname
 				+ ".quest");
 		MineQuest.log(Level.WARNING, "10");
 		Scanner filereader = new Scanner(f);
 		MineQuest.log(Level.WARNING, "11");
 		while (filereader.hasNextLine()) {
 			MineQuest.log(Level.WARNING, "REPEAT");
 			String nextline = filereader.nextLine();
			ArrayList<String> ar = new ArrayList<String>();
			for (String s : nextline.split(":"))
				ar.add(s);
 			String type = ar.get(0).toLowerCase();
 			MineQuest.log(Level.WARNING, "REPEAT-1");
 			if (type.equals("name"))
 				q.displayname = ar.get(1);
 			else if (type.equals("repeatable"))
 				q.questRepeatable = (ar.get(1).equals("true"));
 			else if (type.equals("reset"))
 				q.spawnReset = (ar.get(1).equals("true"));
 			else if (type.equals("spawn")) {
 				if (!ar.get(1).equals(""))
 					q.spawnPoint[0] = Double.parseDouble(ar.get(1));
 				if (!ar.get(2).equals(""))
 					q.spawnPoint[1] = Double.parseDouble(ar.get(2));
 				if (!ar.get(3).equals(""))
 					q.spawnPoint[2] = Double.parseDouble(ar.get(3));
 			} else if (type.equals("areapreserve")) {
 				if (!ar.get(1).equals(""))
 					q.areaPreserve[0] = Double.parseDouble(ar.get(1));
 				if (!ar.get(2).equals(""))
 					q.areaPreserve[1] = Double.parseDouble(ar.get(2));
 				if (!ar.get(3).equals(""))
 					q.areaPreserve[2] = Double.parseDouble(ar.get(3));
 				if (!ar.get(4).equals(""))
 					q.areaPreserve[3] = Double.parseDouble(ar.get(4));
 				if (!ar.get(5).equals(""))
 					q.areaPreserve[4] = Double.parseDouble(ar.get(5));
 				if (!ar.get(6).equals(""))
 					q.areaPreserve[5] = Double.parseDouble(ar.get(6));
 			} else if (type.equals("editmessage"))
 				q.editMessage = ChatColor.GRAY + ar.get(1);
 			else if (type.equals("world"))
 				q.world = ar.get(1);
 			else if (type.equals("loadworld")) {
 				// I say YES to instances.
 				q.loadworld = true;
 				q.world = ar.get(2);
 				// I do NOT care about QuestArea, because
 				// I simply delete the world when done.
 			} else if (type.equals("event")) {
 				int number = Integer.parseInt(ar.get(1));
 				// T = targeted event
 				boolean targetedevent = false;
 				if (ar.get(2).equals("T")) {
 					ar.remove(2);
 					targetedevent = true;
 				}
 				String eventname = ar.get(2);
 				String details = "";
 				if (targetedevent)
 					details += "T:";
 				for (int i = 3; i < ar.size(); i++) {
 					details += ar.get(i);
 					if (i < ar.size() - 1) {
 						details += ":";
 					}
 				}
 				// final result: "eventname:T:details"
 				events.put(number, eventname + ":" + details);
 			} else if (type.equals("task")) {
 				// TODO apparently not implemented D:
 			} else if (type.equals("target")) {
 				int number = Integer.parseInt(ar.get(1));
 				String d = "";
 				for (int i=2; i<ar.size(); i++){
 					d += ar.get(i);
 					if (i!=ar.size()-1)
 						d+=":";
 				}
 				targets.put(number, new TargetDetails(q.questid,d));
 			} else if (type.equals("edit")) {
 				int number = Integer.parseInt(ar.get(1));
 				String edittype = ar.get(2);
 				String d = "";
 				for (int i=3; i<ar.size(); i++){
 					d += ar.get(i);
 					if (i!=ar.size()-1)
 						d+=":";
 				}
 				Edit e;
 				if (edittype.equalsIgnoreCase("CanEdit"))
 					e = new CoordinateEdit(q.questid,number,Integer.parseInt(d.split(":")[3]),d);
 				else if (edittype.equalsIgnoreCase("CanEditArea"))
 					e = new InsideAreaEdit(q.questid,number,Integer.parseInt(d.split(":")[6]),d);
 				else if (edittype.equalsIgnoreCase("CanEditOutsideArea"))
 					e = new OutsideAreaEdit(q.questid,number,Integer.parseInt(d.split(":")[6]),d);
 				else {
 					int taskid = Integer.parseInt(ar.get(3));
 					d = "";
 					for (int i=4; i<ar.size(); i++){
 						d += ar.get(i);
 						if (i!=ar.size()-1)
 							d+=":";
 					}
 					if (edittype.equalsIgnoreCase("CanEditTypesInHand"))
 						e = new ItemInHandEdit(q.questid,number,taskid,d);
 					else
 						e = new CertainBlockEdit(q.questid,number,taskid,d);
 				}
 				editables.put(number, e);
 			}
 		}
 		MineQuest.log(Level.WARNING, "12");
 		q.tasks = new TreeMap<Integer, String>(tasks);
 		q.events = new TreeMap<Integer, String>(events);
 		q.targets = new TreeMap<Integer, TargetDetails>(targets);
 		q.editables = new TreeMap<Integer, Edit>(editables);
 		MineQuest.log(Level.WARNING, "13");
 	}
 	
 	public static void parseYAMLDefinition(Quest q){
 		LinkedHashMap<Integer, String> tasks = new LinkedHashMap<Integer, String>();
 		LinkedHashMap<Integer, String> events = new LinkedHashMap<Integer, String>();
 		LinkedHashMap<Integer, TargetDetails> targets = new LinkedHashMap<Integer, TargetDetails>();
 		LinkedHashMap<Integer, Edit> editables = new LinkedHashMap<Integer,Edit>();
 		File f = new File(MineQuest.questManager.locationofQuests + File.separator + q.questname
 				+ ".yml");
 		if (!f.exists())
 			throw new RuntimeException(new FileNotFoundException("NO SUCH FILE " + f));
 		YamlConfiguration definition = YamlConfiguration.loadConfiguration(f);
 		
 		q.displayname = definition.getString("name","Quest");
 		q.displaydesc = definition.getString("description","This is a quest.");
 		q.displayaccept = definition.getString("accepttext","You have accepted the quest.");
 		q.displaycancel = definition.getString("canceltext","You have canceled the quest.");
 		q.displayfinish = definition.getString("finishtext","You have finished the quest.");
 		q.questRepeatable = definition.getBoolean("isRepeatable", false);
 		q.spawnReset = definition.getBoolean("resetSpawn",true);
 		
 		String[] setSpawn = definition.getString("setSpawn","0:64:0").split(":");
 		q.spawnPoint[0] = Double.parseDouble(setSpawn[0]);
 		q.spawnPoint[1] = Double.parseDouble(setSpawn[1]);
 		q.spawnPoint[2] = Double.parseDouble(setSpawn[2]);
 		
 		String[] areaPreserve = definition.getString("areaPreserve","0:64:0:0:64:0").split(":");
 		q.areaPreserve[0] = Double.parseDouble(areaPreserve[0]);
 		q.areaPreserve[1] = Double.parseDouble(areaPreserve[1]);
 		q.areaPreserve[2] = Double.parseDouble(areaPreserve[2]);
 		q.areaPreserve[3] = Double.parseDouble(areaPreserve[3]);
 		q.areaPreserve[4] = Double.parseDouble(areaPreserve[4]);
 		q.areaPreserve[5] = Double.parseDouble(areaPreserve[5]);
 		
 		q.editMessage = definition.getString("doNotEditMessage",ChatColor.GRAY+"You cannot edit while in the quest.");
 		q.world = definition.getString("world","world");
 		q.loadworld = definition.getBoolean("loadWorld",false);
 		
 		ConfigurationSection eventss = definition.getConfigurationSection("events");
 		for (int i : definition.getIntegerList("")){
 			events.put(i,eventss.getString(String.valueOf(i)));
 		}
 		
 		q.events = new TreeMap<Integer, String>(events);
 		
 		ConfigurationSection taskss = definition.getConfigurationSection("tasks");
 		for (int i : definition.getIntegerList("")){
 			tasks.put(i,taskss.getString(String.valueOf(i)));
 		}
 		
 		q.tasks = new TreeMap<Integer, String>(tasks);
 		
 		ConfigurationSection targetss = definition.getConfigurationSection("targets");
 		for (int i : definition.getIntegerList("")){
 			targets.put(i,new TargetDetails(q.questid,targetss.getString(String.valueOf(i))));
 		}
 		
 		q.targets = new TreeMap<Integer, TargetDetails>(targets);
 		
 		ConfigurationSection editss = definition.getConfigurationSection("edits");
 		for (int i : definition.getIntegerList("")){
 			editables.put(i,processEdit(editss.getString(String.valueOf(i))));
 		}
 		
 		q.editables = new TreeMap<Integer, Edit>(editables);
 		
 		/*
 		 * name: <String name>
 		 * description: <description of quest>
 		 * accepttext: <upon accepting quest>
 		 * canceltext: <upon canceling quest>
 		 * finishtext: <upon finishing quest>
 		 * isRepeatable: boolean
 		 * resetSpawn: boolean
 		 * setSpawn: String
 		 * areaPreserve: String
 		 * doNotEditMessage: String
 		 * world: String
 		 * loadWorld: boolean (is this dungeoned?)
 		 * events: Map<Integer,String>
 		 * tasks: Map<Integer,String>
 		 * targets: Map<Integer,String>
 		 * edits: Map<Integer,String>
 		 */
 	}
 	
 	private static Edit processEdit(String details){
 		return null;
 	}
 
 }
