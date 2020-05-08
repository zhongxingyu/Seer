 package Lihad.Conflict.Information;
 
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Location;
 
 import Lihad.Conflict.Conflict;
 
 public class BeyondInfo {
 	public static Conflict plugin;
 	
 	public static List<String> something = new LinkedList<String>();
 
 	public BeyondInfo(Conflict instance) {
 		plugin = instance;
 	}
 	
 	public static void loader(){
         Conflict.ABATTON_PLAYERS.clear();
         Conflict.ABATTON_PLAYERS.addAll(Conflict.information.getStringList("Capitals.Abatton.Players"));
 
 		Conflict.ABATTON_LOCATION = toLocation("Capitals.Abatton.Location");
 		Conflict.ABATTON_LOCATION_SPAWN = toLocation("Capitals.Abatton.Spawn");
 		Conflict.ABATTON_LOCATION_DRIFTER = toLocation("Capitals.Abatton.Drifter");
 
         Conflict.ABATTON_GENERALS.clear();
 		Conflict.ABATTON_GENERALS.addAll(Conflict.information.getStringList("Capitals.Abatton.Generals"));
 
         Conflict.ABATTON_TRADES.clear();
 		Conflict.ABATTON_TRADES.addAll(Conflict.information.getStringList("Capitals.Abatton.Trades"));
 
         Conflict.ABATTON_PERKS.clear();
 		Conflict.ABATTON_PERKS.addAll(Conflict.information.getStringList("Capitals.Abatton.Perks"));
 
 		Conflict.ABATTON_WORTH = (Conflict.information.getInt("Capitals.Abatton.Worth"));
 		Conflict.ABATTON_PROTECTION = (Conflict.information.getInt("Capitals.Abatton.Protection"));
 
         Conflict.OCEIAN_PLAYERS.clear();
 		Conflict.OCEIAN_PLAYERS.addAll(Conflict.information.getStringList("Capitals.Oceian.Players"));
 
 		Conflict.OCEIAN_LOCATION = toLocation("Capitals.Oceian.Location");
 		Conflict.OCEIAN_LOCATION_SPAWN = toLocation("Capitals.Oceian.Spawn");
 		Conflict.OCEIAN_LOCATION_DRIFTER = toLocation("Capitals.Oceian.Drifter");
 
         Conflict.OCEIAN_GENERALS.clear();
 		Conflict.OCEIAN_GENERALS.addAll(Conflict.information.getStringList("Capitals.Oceian.Generals"));
 
         Conflict.OCEIAN_TRADES.clear();
 		Conflict.OCEIAN_TRADES.addAll(Conflict.information.getStringList("Capitals.Oceian.Trades"));
 
         Conflict.OCEIAN_PERKS.clear();
 		Conflict.OCEIAN_PERKS.addAll(Conflict.information.getStringList("Capitals.Oceian.Perks"));
 
 		Conflict.OCEIAN_WORTH = (Conflict.information.getInt("Capitals.Oceian.Worth"));
 		Conflict.OCEIAN_PROTECTION = (Conflict.information.getInt("Capitals.Oceian.Protection"));
 
         Conflict.SAVANIA_PLAYERS.clear();
 		Conflict.SAVANIA_PLAYERS.addAll(Conflict.information.getStringList("Capitals.Savania.Players"));
 
 		Conflict.SAVANIA_LOCATION = toLocation("Capitals.Savania.Location");
 		Conflict.SAVANIA_LOCATION_SPAWN = toLocation("Capitals.Savania.Spawn");
 		Conflict.SAVANIA_LOCATION_DRIFTER = toLocation("Capitals.Savania.Drifter");
 
         Conflict.SAVANIA_GENERALS.clear();
 		Conflict.SAVANIA_GENERALS.addAll(Conflict.information.getStringList("Capitals.Savania.Generals"));
 
         Conflict.SAVANIA_TRADES.clear();
 		Conflict.SAVANIA_TRADES.addAll(Conflict.information.getStringList("Capitals.Savania.Trades"));
 
         Conflict.SAVANIA_PERKS.clear();
 		Conflict.SAVANIA_PERKS.addAll(Conflict.information.getStringList("Capitals.Savania.Perks"));
 
 		Conflict.SAVANIA_WORTH = (Conflict.information.getInt("Capitals.Savania.Worth"));
 		Conflict.SAVANIA_PROTECTION = (Conflict.information.getInt("Capitals.Savania.Protection"));
 
 		Conflict.TRADE_BLACKSMITH = toLocation("Trades.blacksmith");
 		Conflict.TRADE_POTIONS = toLocation("Trades.potions");
 		Conflict.TRADE_ENCHANTMENTS = toLocation("Trades.enchantments");
 		Conflict.TRADE_RICHPORTAL = toLocation("Trades.richportal");
 		Conflict.TRADE_MYSTPORTAL = toLocation("Trades.mystportal");
         
        Conflict.nodes.clear();
         Conflict.nodes.add(new Conflict.Node(Conflict.information.getString("Nodes.0.Name", "Tower"),   toLocation("Nodes.0.Location", Conflict.TRADE_POTIONS)));
         Conflict.nodes.add(new Conflict.Node(Conflict.information.getString("Nodes.1.Name", "Cavern"),  toLocation("Nodes.1.Location", Conflict.TRADE_RICHPORTAL)));
         Conflict.nodes.add(new Conflict.Node(Conflict.information.getString("Nodes.2.Name", "Fort"),    toLocation("Nodes.2.Location", Conflict.TRADE_MYSTPORTAL)));
         Conflict.nodes.add(new Conflict.Node(Conflict.information.getString("Nodes.3.Name", "Derrick"), toLocation("Nodes.3.Location", Conflict.TRADE_BLACKSMITH)));
         Conflict.nodes.add(new Conflict.Node(Conflict.information.getString("Nodes.4.Name", "Outpost"), toLocation("Nodes.4.Location", Conflict.TRADE_ENCHANTMENTS)));
         
 	}
 
 	public static void writer(){
 		Conflict.information.set("Capitals.Abatton.Players", Conflict.ABATTON_PLAYERS);
 		Conflict.information.set("Capitals.Abatton.Location", toString(Conflict.ABATTON_LOCATION));
 		Conflict.information.set("Capitals.Abatton.Spawn", toString(Conflict.ABATTON_LOCATION_SPAWN));
 		Conflict.information.set("Capitals.Abatton.Drifter", toString(Conflict.ABATTON_LOCATION_DRIFTER));
 		Conflict.information.set("Capitals.Abatton.Generals", Conflict.ABATTON_GENERALS);
 		Conflict.information.set("Capitals.Abatton.Trades", Conflict.ABATTON_TRADES);
 		Conflict.information.set("Capitals.Abatton.Perks", Conflict.ABATTON_PERKS);
 		Conflict.information.set("Capitals.Abatton.Worth", Conflict.ABATTON_WORTH);
 		Conflict.information.set("Capitals.Abatton.Protection", Conflict.ABATTON_PROTECTION);
 		Conflict.information.set("Capitals.Oceian.Players", Conflict.OCEIAN_PLAYERS);
 		Conflict.information.set("Capitals.Oceian.Location", toString(Conflict.OCEIAN_LOCATION));
 		Conflict.information.set("Capitals.Oceian.Spawn", toString(Conflict.OCEIAN_LOCATION_SPAWN));
 		Conflict.information.set("Capitals.Oceian.Drifter", toString(Conflict.OCEIAN_LOCATION_DRIFTER));
 		Conflict.information.set("Capitals.Oceian.Generals", Conflict.OCEIAN_GENERALS);
 		Conflict.information.set("Capitals.Oceian.Trades", Conflict.OCEIAN_TRADES);
 		Conflict.information.set("Capitals.Oceian.Perks", Conflict.OCEIAN_PERKS);
 		Conflict.information.set("Capitals.Oceian.Worth", Conflict.OCEIAN_WORTH);
 		Conflict.information.set("Capitals.Oceian.Protection", Conflict.OCEIAN_PROTECTION);
 		Conflict.information.set("Capitals.Savania.Players", Conflict.SAVANIA_PLAYERS);
 		Conflict.information.set("Capitals.Savania.Location", toString(Conflict.SAVANIA_LOCATION));
 		Conflict.information.set("Capitals.Savania.Spawn", toString(Conflict.SAVANIA_LOCATION_SPAWN));
 		Conflict.information.set("Capitals.Savania.Drifter", toString(Conflict.SAVANIA_LOCATION_DRIFTER));
 		Conflict.information.set("Capitals.Savania.Generals", Conflict.SAVANIA_GENERALS);
 		Conflict.information.set("Capitals.Savania.Trades", Conflict.SAVANIA_TRADES);
 		Conflict.information.set("Capitals.Savania.Perks", Conflict.SAVANIA_PERKS);
 		Conflict.information.set("Capitals.Savania.Worth", Conflict.SAVANIA_WORTH);
 		Conflict.information.set("Capitals.Savania.Protection", Conflict.SAVANIA_PROTECTION);
 		Conflict.information.set("Trades.blacksmith", toString(Conflict.TRADE_BLACKSMITH));
 		Conflict.information.set("Trades.potions", toString(Conflict.TRADE_POTIONS));
 		Conflict.information.set("Trades.enchantments", toString(Conflict.TRADE_ENCHANTMENTS));
 		Conflict.information.set("Trades.richportal", toString(Conflict.TRADE_RICHPORTAL));
 		Conflict.information.set("Trades.mystportal", toString(Conflict.TRADE_MYSTPORTAL));
         
         int i = 0;
         for (Conflict.Node n : Conflict.nodes) {
             Conflict.information.set("Nodes." + i + ".Name", n.name);
             Conflict.information.set("Nodes." + i + ".Location", toString(n.location));
             i++;
         }
 	}
 	private static Location toLocation(String path){
 		String[] array;
 		String str = Conflict.information.getString(path);
 		if(str == null) return null;
 		array = str.split(",");
 		Location location = new Location(plugin.getServer().getWorld(array[3]), Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2]));
 		return location;
 	}
 	private static Location toLocation(String path, Location def) {
         Location l = toLocation(path);
         if (l == null) { return def; }
         return l;
     }
 	private static String toString(Location location){
 		if(location == null) return null;
 		return (location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+","+location.getWorld().getName());
 	}
 }
 	
