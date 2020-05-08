 package com.guntherdw.bukkit.TweakWarp;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class WarpGroup {
 	
 	private String name;
 	private Map<String, Warp> warps;
 
 	public WarpGroup(String name) {
 		this.name = name;
 		warps = new HashMap<String, Warp>();
 	}
 	
 	public Warp matchWarp(String warpname) {
 		Warp rt = getWarp(warpname);
 		if(rt == null) {
 			int delta = Integer.MAX_VALUE;
 			for(Warp w : warps.values()) {
				if(w.getName().toLowerCase().contains(warpname) && Math.abs(w.getName().length() - warpname.length()) < delta) {
 					rt = w;
 					delta = Math.abs(w.getName().length() - warpname.length());
 					if(delta == 0) break;
 				}
 			}
 		}
 		
 		return rt;
 	}
 	
 	public Warp getWarp(String warpname) {
 		return warps.get(warpname);
 	}
 	
 	public boolean registerWarp(Warp warp) {
 		if(!warp.getWarpgroup().equals(getName())) {
 			TweakWarp.log.warning("[TweakWarp] trying to add warp to invalid group, warp: " + warp.getName() + "[" + warp.getWarpgroup() + "] to group " + getName() + ".");
 			return false;
 		}
 		warps.put(warp.getName().toLowerCase(), warp);
 		return true;
 	}
 	
 	public boolean forgetWarp(Warp warp) {
 		if(!warp.getWarpgroup().equals(getName())) {
 			TweakWarp.log.warning("[TweakWarp] trying to remove warp from invalid group, warp: " + warp.getName() + "[" + warp.getWarpgroup() + "] from group " + getName() + ".");
 			return false;
 		}
 		warps.remove(warp.getName().toLowerCase());
 		return true;
 	}
 	
 	public Collection<Warp> getWarps() {
 		return warps.values();
 	}
 	
 	public String getName() {
 		return name;
 	}
 }
