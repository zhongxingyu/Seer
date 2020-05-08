 package nl.giantit.minecraft.giantshop.core.Logger;
 
 import nl.giantit.minecraft.giantshop.GiantShop;
 import nl.giantit.minecraft.giantshop.core.config;
 import nl.giantit.minecraft.giantcore.database.Driver;
 import nl.giantit.minecraft.giantcore.database.query.InsertQuery;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Logger {
 	
 	public static void Log(LoggerType type, String playerName, HashMap<String, String> data) {
 		config conf = config.Obtain();
 		if(conf.getBoolean("GiantShop.log.useLogging")) {
 			if(conf.getBoolean("GiantShop.log.log." + type.getName().toLowerCase())) {
 				String json = "{";
 				int i = 0;
 				for(Map.Entry<String, String> d : data.entrySet()) {
 					i++;
 					json += "\"" + d.getKey() + "\": \"" + d.getValue() + "\"";
 					if(i < data.size()) {
 						json += ",";
 					}
 				}
 				json += "}";
 				
 				Driver DB = GiantShop.getPlugin().getDB().getEngine();
 				int t = type.getID();
 				
 				ArrayList<String> fields = new ArrayList<String>();
				ArrayList<HashMap<Integer, HashMap<String, String>>> values = new ArrayList<HashMap<Integer, HashMap<String, String>>>();
 		
 				fields.add("type");
 				fields.add("user");
 				fields.add("data");
 				fields.add("date");
 				
 				InsertQuery iQ = DB.insert("#__log");
 				iQ.addFields(fields);
 				iQ.addRow();
				iQ.assignValue("type", json, InsertQuery.ValueType.RAW);
 				iQ.assignValue("user", playerName);
 				iQ.assignValue("data", json);
 				iQ.assignValue("date", String.valueOf(Logger.getTimestamp()));
 				iQ.exec();
 			}
 		}
 	}
 	
 	public static long getTimestamp() {
 		return System.currentTimeMillis() / 1000;
 	}
 }
