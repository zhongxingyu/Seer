 package com.jseb.teleport.storage;
 
 import com.jseb.teleport.Teleport;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.World;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.File;
 import java.io.IOException;
 
 public class Storage {
 	public Map<Player, Location> back;
 	public Map<Player, Map<Player, Home>> homeAccept; 
 	public Map<Player, Player> playerAccept; 
 	public Map<Player, Location> deathLocations;
 
 	private File homeFile;
	private File areaFile;
	private Teleport plugin;
 
 	public Storage(Teleport plugin, String filePath) {
 		this.back = new HashMap<Player, Location>(); 
 		this.homeAccept = new HashMap<Player, Map<Player, Home>>();
 		this.playerAccept = new HashMap<Player, Player>();
 		this.deathLocations = new HashMap<Player, Location>();
 
 		this.plugin = plugin;
 
 		this.homeFile = new File(filePath + File.separator + "home-locations.bin");
 		this.areaFile = new File(filePath + File.separator + "area-locations.bin");
 
 		loadHomes();
 		loadAreas();
 	}
 
 	public void loadHomes() {
 		Double x = 0.0, y = 0.0, z = 0.0;
 		float pitch = 0, yaw = 0;
 		String name = "", list[], s, owner = "";
 		World world = null;
 		List<Home> homes;
 		boolean isDefault = false;
 
 		try {
 			BufferedReader br;
 			br  = new BufferedReader(new FileReader(homeFile));
 
 			s = br.readLine();
 			if (s == null) return;
 
 			if (!s.contains("home: ")) {
 				//old format
 
 				while (s != null) {
 					list = s.split(" ");
 					if (list.length != 3) {
 						for (int i = 2; i < list.length - 1; i++) {
 							list[1] += " " + list[i];
 						}
 
 						list[2] = list[list.length - 1];
 					}
 
 					String lstring[] = list[1].split(",");
 
 					if (list.length >= 3) {
 						name = list[2];
 					} else {
 						name = "temp";
 					}
 
 					world = plugin.getServer().getWorld(lstring[0].substring(lstring[0].lastIndexOf("=") + 1, lstring[0].length() - 1));
 					x = Double.parseDouble(lstring[1].substring(lstring[1].indexOf("=") + 1, lstring[1].length()));
 					y = Double.parseDouble(lstring[2].substring(lstring[2].indexOf("=") + 1, lstring[2].length()));
 					z = Double.parseDouble(lstring[3].substring(lstring[3].indexOf("=") + 1, lstring[3].length()));
 					pitch = Float.parseFloat(lstring[4].substring(lstring[4].indexOf("=") + 1, lstring[4].length()));
 					yaw = Float.parseFloat(lstring[5].substring(lstring[5].indexOf("=") + 1, lstring[5].length() - 1));
 
 					new Home(list[0], name, new Location(world, x, y, z, yaw, pitch), false);
 
 					s = br.readLine();
 				}
 
 				br.close();
 			} else {
 				//new format
 
 				while (s != null) {
 					do {
 						if (s.startsWith("home: ")) name = s.substring(s.indexOf(":") + 2, s.length());
 						else if (s.startsWith("owner: ")) owner = s.substring(s.indexOf(":") + 2, s.length()).toLowerCase().trim();
 						else if (s.startsWith("world: ")) world = plugin.getServer().getWorld(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("x: ")) x = Double.parseDouble(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("y: ")) y = Double.parseDouble(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("z: ")) z = Double.parseDouble(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("yaw: ")) yaw = Float.parseFloat(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("pitch: ")) pitch = Float.parseFloat(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("isDefault: ")) isDefault = Boolean.parseBoolean(s.substring(s.indexOf(":") + 2, s.length()));
 						
 						s = br.readLine();
 						if (s == null) break;
 					} while (!(s.startsWith("home: ")));
 
 					if ((world == null) || (owner == "") || (name == "")) {
 						System.out.println("[Teleport] something went wrong loading homes");
 					} else {
 						new Home(owner, name, new Location(world, x, y, z, yaw, pitch), isDefault);
 					}	
 				}
 
 				br.close();
 			}
 		} catch(ArrayIndexOutOfBoundsException e) {
 			
 		} catch(IOException e) {
 
 		}
 	}
 
 	public void loadAreas() {
 		Double x = 0.0, y = 0.0, z = 0.0;
 		float pitch = 0, yaw = 0;
 		String name = "", list[], s, author = "";
 		World world = null;
 		boolean permission = false;
 
 		try {
 			BufferedReader br;
 			br  = new BufferedReader(new FileReader(areaFile));
 
 			s = br.readLine();
 			if (s == null) return;
 
 			if (!s.contains("area: ")) {
 				while (s != null) {
 					list = s.split(" ");
 					if (list.length != 2) {
 						for (int i = 2; i < list.length; i++) {
 							list[1] += " " + list[i];
 						}
 					}
 
 					String lstring[] = list[1].split(",");
 					name = list[0];
 
 					
 
 					world = plugin.getServer().getWorld(lstring[0].substring(lstring[0].lastIndexOf("=") + 1, lstring[0].length() - 1));
 					x = Double.parseDouble(lstring[1].substring(lstring[1].indexOf("=") + 1, lstring[1].length()));
 					y = Double.parseDouble(lstring[2].substring(lstring[2].indexOf("=") + 1, lstring[2].length()));
 					z = Double.parseDouble(lstring[3].substring(lstring[3].indexOf("=") + 1, lstring[3].length()));
 					pitch = Float.parseFloat(lstring[4].substring(lstring[4].indexOf("=") + 1, lstring[4].length()));
 					yaw = Float.parseFloat(lstring[5].substring(lstring[5].indexOf("=") + 1, lstring[5].length() - 1));
 
 					new Area(name, new Location(world, x, y, z, yaw, pitch));
 
 					s = br.readLine();
 				}
 			} else {
 				//new format
 
 				while (s != null) {
 					do {
 						if (s.startsWith("area: ")) name = s.substring(s.indexOf(":") + 2, s.length());
 						else if (s.startsWith("author: ")) author = s.substring(s.indexOf(":") + 2, s.length()).toLowerCase().trim();
 						else if (s.startsWith("world: ")) world = plugin.getServer().getWorld(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("x: ")) x = Double.parseDouble(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("y: ")) y = Double.parseDouble(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("z: ")) z = Double.parseDouble(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("yaw: ")) yaw = Float.parseFloat(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("pitch: ")) pitch = Float.parseFloat(s.substring(s.indexOf(":") + 2, s.length()));
 						else if (s.startsWith("permission: ")) permission = Boolean.parseBoolean(s.substring(s.indexOf(":") + 2, s.length()));
 						
 						s = br.readLine();
 						if (s == null) break;
 					} while (!(s.startsWith("area: ")));
 
 					if ((world == null) || (name == "")) {
 						System.out.println("[TH] something went wrong loading areas");
 					} else {
 						new Area(name, new Location(world, x, y, z, yaw, pitch), author, permission);
 					}	
 				}
 			}
 
 			br.close();
 		} catch (IOException e) {
 
 		}
 	}
 
 	/*
 		home: <name>
 		owner: <owner>
 		world: <world>
 		x: <x>
 		y: <y>
 		z: <z>
 		yaw: <yaw>
 		pitch: <pitch>
 		residents: <resident list>
 		isDefault: <isDefault boolean>
 	*/
 
 	public void saveHomes() {
 		try {
 			BufferedWriter br;
 	    	br  = new BufferedWriter(new FileWriter(homeFile));
 	    	List<Home> list;
 
 	    	Iterator<String> players = Home.homeList.keySet().iterator();
 
 	   		while (players.hasNext()) {
 	        	String player = players.next();
 	        	list = Home.homeList.get(player);
 
 	        	Iterator<Home> homes = list.iterator();
 	       		while (homes.hasNext()) {
 		            Home home = homes.next();
 		            Location location = home.getLocation();
 		            br.write("home: " + home.getName() + "\n");
 		            br.write("owner: " + home.getOwner().toLowerCase() + "\n");
 		            br.write("world: " + location.getWorld().getName() + "\n");
 		            br.write("x: " + location.getX() + "\n");
 		            br.write("y: " + location.getY() + "\n");
 		            br.write("z: " + location.getZ() + "\n");
 		            br.write("yaw: " + location.getYaw() + "\n");
 		            br.write("pitch: " + location.getPitch() + "\n");
 		            br.write("isDefault: " + home.isDefault + "\n");
 		            //br.write("residents: ");
 		            //br.write("isDefault: ");
 		            br.flush();
 		        }
 	   		}
 
 	    	br.close();
 		} catch (IOException e) {
 
 		}
 	}
 
 	/*
 		area: <name>
 		world: <world>
 		x: <x>
 		y: <y>
 		z: <z>
 		yaw: <yaw>
 		pitch: <pitch>
 	*/
 
 	public void saveAreas() {
 		try {
 			BufferedWriter br;
 	    	br  = new BufferedWriter(new FileWriter(areaFile));
 
 	    	Iterator<Area> areaIt = Area.areaList.iterator();
 
 	   		while (areaIt.hasNext()) {
 	        	Area area = areaIt.next();
 	        	Location location = area.getLocation();
 	        	br.write("area: " + area.getName() + "\n");
 	        	br.write("author: " + area.getAuthor() + "\n");
 	            br.write("world: " + location.getWorld().getName() + "\n");
 	            br.write("x: " + location.getX() + "\n");
 	            br.write("y: " + location.getY() + "\n");
 	            br.write("z: " + location.getZ() + "\n");
 	            br.write("yaw: " + location.getYaw() + "\n");
	            br.write("pitch: " + location.getPitch() + "\n");
	            br.write("permission: " + area.getPermission() + "\n");
 	            br.flush();
 	   		}
 
 	    	br.close();
 		} catch (IOException e) {
 
 		}
 	}
 }
