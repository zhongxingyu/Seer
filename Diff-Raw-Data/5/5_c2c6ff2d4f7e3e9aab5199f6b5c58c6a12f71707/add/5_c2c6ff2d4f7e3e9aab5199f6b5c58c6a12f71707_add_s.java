 package nl.giantit.minecraft.GiantShop.Locationer.core.Commands.chat;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Locationer.Locationer;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.giantcore.Database.iDriver;
 import nl.giantit.minecraft.giantcore.perms.Permission;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author Giant
  */
 public class add {
 	
 	private static config conf = config.Obtain();
 	private static Permission perms = GiantShop.getPlugin().getPermHandler().getEngine();
 	private static Messages mH = GiantShop.getPlugin().getMsgHandler();
 	private static Locationer lH = GiantShop.getPlugin().getLocHandler();
 	
 	public static void add(Player player, String[] args) {
 		if(perms.has(player, "giantshop.location.add")) {
 			HashMap<String, Location> points = lH.getPlayerPoints(player);
 			if(points.size() < 2) {
 				Heraut.say(player, "You need to set 2 points to add a shop!");
 				return;
 			}
 			
 			if(!points.containsKey("min") || !points.containsKey("max")) {
 				Heraut.say(player, "Failed to add shop. Invalid points passed!");
 				return;
 			}
 			
 			String name = (args.length > 1) ? args[1] : "unkown";
 			Location loc1, loc2;
 			loc1 = points.get("min");
 			loc2 = points.get("max");
 			
 			if(!loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
 				Heraut.say(player, "Failed to add shop. Points not in same world!");
 				return;
 			}
 			
 			double minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
 			double minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
 			double minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
 			double maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
 			double maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
 			double maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
 			
 			while (maxX - minX < 4) maxX += 1;
 			while (maxY - minY < 2) maxY += 1;
 			while (maxZ - minZ < 4) maxZ += 1;
 			
 			Location l = new Location(loc1.getWorld(), minX, minY, minZ);
 			Location l2 = new Location(loc1.getWorld(), maxX, maxY, maxZ);
 			
 			if(!lH.inShop(l) && !lH.inShop(l2)) {
 				iDriver DB = GiantShop.getPlugin().getDB().getEngine();
 
 				ArrayList<String> fields = new ArrayList<String>();
 				fields.add("id");
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("name", name);
 				data.put("world", loc1.getWorld().getName());
 
 				DB.select(fields).from("#__shops").where(data);
 				if(DB.execQuery().size() == 0) {
 					fields = new ArrayList<String>();
 					fields.add("name");
 					fields.add("world");
 					fields.add("locMinX");
 					fields.add("locMinY");
 					fields.add("locMinZ");
 					fields.add("locMaxX");
 					fields.add("locMaxY");
 					fields.add("locMaxZ");
 
 					ArrayList<HashMap<Integer, HashMap<String, String>>> values = new ArrayList<HashMap<Integer, HashMap<String, String>>>();
 					HashMap<Integer, HashMap<String, String>> tmp = new HashMap<Integer, HashMap<String, String>>();
 					HashMap<String, String> temp = new HashMap<String, String>();
 					temp.put("data", name);
 					tmp.put(0, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", loc1.getWorld().getName());
 					tmp.put(1, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", String.valueOf(minX));
 					tmp.put(2, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", String.valueOf(minY));
 					tmp.put(3, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", String.valueOf(minZ));
 					tmp.put(4, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", String.valueOf(maxX));
 					tmp.put(5, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", String.valueOf(maxY));
 					tmp.put(6, temp);
 
 					temp = new HashMap<String, String>();
 					temp.put("data", String.valueOf(maxZ));
 					tmp.put(7, temp);
 
 					values.add(tmp);
 					DB.insert("#__shops", fields, values).updateQuery();
 
 					data = new HashMap<String, String>();
 					data.put("shop", name);
 					data.put("world", loc1.getWorld().getName());
 					data.put("minX", String.valueOf(minX));
 					data.put("minY", String.valueOf(minY));
 					data.put("minZ", String.valueOf(minZ));
 					data.put("maxX", String.valueOf(maxX));
 					data.put("maxY", String.valueOf(maxY));
 					data.put("maxZ", String.valueOf(maxZ));
 
 					ArrayList<Location> t = new ArrayList<Location>();
 					t.add(l);
 					t.add(l2);
 					
 					lH.addShop(t, name);
 					lH.remPlayerPoint(player);
 					Heraut.say(player, mH.getMsg(Messages.msgType.ADMIN, "shopAdded", data));
 				}else{
 					data = new HashMap<String, String>();
 					data.put("shop", name);
 					data.put("world", loc1.getWorld().getName());
 
 					Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "shopNameTaken", data));
 				}
 			}else{
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("shop", name);
 				data.put("world", loc1.getWorld().getName());
 				data.put("minX", String.valueOf(minX));
 				data.put("minY", String.valueOf(minY));
 				data.put("minZ", String.valueOf(minZ));
 				data.put("maxX", String.valueOf(maxX));
 				data.put("maxY", String.valueOf(maxY));
 				data.put("maxZ", String.valueOf(maxZ));
 
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "locTaken", data));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "loc add");
 
 			Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 }
