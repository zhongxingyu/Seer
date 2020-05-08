 package com.wolvencraft.prison.updater;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.wolvencraft.prison.PrisonSuite;
 import com.wolvencraft.prison.hooks.PrisonPlugin;
 import com.wolvencraft.prison.settings.Settings;
 import com.wolvencraft.prison.util.Message;
 
 
 public class Updater {
 	private static Map<String, String> data;
 	private static List<String> needUpdating;
 	
 	public static boolean checkVersion() {
 		Settings settings = PrisonSuite.getSettings();
 		
 		if(!settings.UPDATE) return true;
 		
 		data = FetchSource.fetchSource();
 		if(data == null) return true;
 		
 		needUpdating = new ArrayList<String>();
 		for(PrisonPlugin plugin : PrisonSuite.getPlugins()) {
 			double newVersion = Double.parseDouble(data.get(plugin.getName()));
 			if(newVersion > plugin.getVersion()) needUpdating.add(plugin.getName());
 		}
 		
 		if(needUpdating.size() == 0) return true;
 		
 		List<String> pluginString = new ArrayList<String>();
 		String temp = "";
 		for(int i = 0; i < needUpdating.size(); i++) {
 			if((temp.length() + needUpdating.get(i).length() + 1) <= 40)
 				temp = temp + needUpdating.get(i) + " ";
 			else {
 				pluginString.add(centerString(temp.substring(0, temp.length() - 1)));
 				Message.debug("String added: " + temp);
 				temp = "";
 				i--;
 			}
 		}
 		
 		Message.log(" +------------------------------------------+");
 		Message.log(" |       PrisonSuite is not up to date!     |");
 		Message.log(" |          http://bit.ly/MineReset/        |");
 		Message.log(" |                                          |");
 		Message.log(" |   Following plugins need to be updated:  |");
 		Message.log(" |                                          |");
 		for(String displayReason : pluginString)
 			Message.log(" | " + displayReason + " |");
 		Message.log(" |                                          |");
 		Message.log(" +------------------------------------------+");
 		
 		return false;
 		
 	}
 	
 	private static String centerString(String str) {
 		while(str.length() < 40) {
 			str = " " + str + " ";
 		}
 		if(str.length() == 41)
 			str = str.substring(0, str.length() - 1);
 		return str;
 	}
 	
 	public static List<String> getOldPlugins() { return needUpdating; }
 }
