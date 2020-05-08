 package com.zand.areaguard;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Area {
 	private static AreaDatabase ad = AreaDatabase.getInstance();
 	public static Area getArea(int id) {
 		return ad.getArea(id);
 	}
 	
 	public static Area getArea(int x, int y, int z) {
 		return ad.getArea(ad.getAreaId(x, y, z));
 	}
 	public static Area getArea(String name) {
 		return ad.getArea(ad.getAreaId(name));
 	}
 	public static Area getOwnedArea(String owner, String name) {
 		for (int id : ad.getAreaIdsFromListValues("owners", owner)) {
 			Area area = ad.getArea(id);
 			if (area != null)
 				if (area.getName().equals(name))
 					return area;
 		}
 		return null;
 	}
 	public static boolean remove(int id) {
 		return ad.removeArea(id);
 	}
 
 	private Integer id = -1;
 	
 	// Cache
 	private String name = "NOT FOUND";
 	private int priority = 0;
 	
 	private int[] coords = new int[6];
 	
 	protected Area(int id, String name, int priority, int[] coords) {
 		this.id = id;
 		this.name = name;
 		this.priority = priority;
 		if (coords.length == 6)
 			this.coords = coords;
 	}
 	
 	public Area(String name, int[] coords) {
 		this.name = name;
 		if (coords.length == 6)
 			this.coords = coords;
 		this.id = ad.addArea(name, coords);
 	}
 	
 	public boolean setMsg(String name, String msg) {
 		return ad.setMsg(id, name, msg); 
 	}
 
 	public boolean addList(String list, HashSet<String> values) {
 		return ad.addList(id, list, values);
 	}
 
 	public int[] getCoords() {
 		return coords;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	public String getMsg(String name) {
 		return ad.getMsg(id, name);
 	}
 	
 	public Set<String> getLists() {
 		return ad.getLists(id);
 	}
 	
 	public ArrayList<String> getList(String list) {
 		return ad.getList(id, list);
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public boolean remove() {
 		return remove(id);
 	}
 	
 	public boolean removeList(String list) {
 		return ad.removeList(id, list);
 	}
 	
 	public boolean removeList(String list, HashSet<String> values) {
 		return ad.removeList(id, list, values);
 	}
 	
 	public boolean setCoords(int[] coords) {
 		if (id == -1) return false;
 		if (coords.length != 6) return false;
 		this.coords = coords;
 		return ad.updateArea(this);
 	}
 
 	public boolean setName(String name) {
 		if (id == -1) return false;
 		this.name = name;
 		return ad.updateArea(this);
 	}
 	
 	public String toString() {
 		return "[" + id + "] \t" + name + " \t@ (" + 
 		coords[0] + ", " + coords[1] + ", " + coords[2] + ")-(" +
 		coords[3] + ", " + coords[4] + ", " + coords[5] + ")"; 
 	}
 	
 	public boolean listHas(String list, String value) {
 		return ad.listHas(id, list, value);
 	}
 	
 	public boolean playerCan(String player, String name, boolean checkAllow) {
		if (checkAllow && listHas("owners", player)) return true;
 		if (listHas("restrict", name)) {
			if (checkAllow && listHas("allow", player)) return true;
 			if (listHas(name, player)) return true;
 			return false;
 		}
 		if (listHas("no-allow", player)) return false;
 		if (listHas("no-"+name, player)) return false;
 		return true;
 	}
 
 	public HashMap<String, String> getMsgs() {
 		return ad.getMsgs(id);
 	}
 
 	public int getPriority() {
 		return priority;
 	}
 
 	public boolean setPriority(int priority) {
 		if (id == -1) return false;
 		this.priority = priority;
 		return ad.updateArea(this);
 	}
 }
