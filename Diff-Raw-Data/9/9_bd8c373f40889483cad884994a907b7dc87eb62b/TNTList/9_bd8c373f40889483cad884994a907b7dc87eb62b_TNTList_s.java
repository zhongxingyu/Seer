 package de.kumpelblase2.dragonslair.logging;
 
 import java.util.*;
 import org.bukkit.Location;
 
 public class TNTList
 {
 	private Set<TNTEntry> tntEntries = new HashSet<TNTEntry>();
 	
 	public class TNTEntry
 	{
 		private Location loc;
 		private String dungeon;
 		
 		public TNTEntry(Location l, String d)
 		{
 			this.loc = l;
 			this.dungeon = d;
 		}
 		
 		public Location getLocation()
 		{
 			return this.loc;
 		}
 		
 		public String getDungeon()
 		{
 			return this.dungeon;
 		}
 		
 		@Override
 		public boolean equals(Object o)
 		{
 			if(o instanceof String)
 			{
 				return this.dungeon.equals(o);
 			}
 			else
 			{
 				Location l;
				if(o instanceof TNTEntry) //TODO
 					l = ((TNTEntry)o).getLocation();
 				else if(o instanceof Location)
 					l = (Location)o;
 				else
 					return false;
 				
 				return l.getWorld().getName().equals(this.loc.getWorld().getName()) && l.getBlockX() == this.loc.getBlockX() && l.getBlockY() == this.loc.getBlockY() && l.getBlockZ() == this.loc.getBlockZ();
 			}
 		}
 	}
 	
 	public void addEntry(String dungeon, Location l)
 	{
 		this.tntEntries.add(new TNTEntry(l, dungeon));
 	}
 	
 	public TNTEntry getEntry(Location l)
 	{
 		for(TNTEntry t : this.tntEntries)
 		{
 			if(t.equals(l))
 				return t;
 		}
 		return null;
 	}
 	
 	public boolean hasEntry(Location l)
 	{
 		return this.getEntry(l) != null;
 	}
 	
 	public void removeEntry(Location l)
 	{
 		Iterator<TNTEntry> it = this.tntEntries.iterator();
 		while(it.hasNext())
 		{
 			TNTEntry e = it.next();
 			if(e.equals(l))
 			{
 				this.tntEntries.remove(e);
 				return;
 			}
 		}
 	}
 	
 	public boolean hasEntries()
 	{
 		return this.tntEntries.size() > 0;
 	}
 	
 	public Set<TNTEntry> getEntriesForDungeon(String dungeon)
 	{
 		Set<TNTEntry> subSet = new HashSet<TNTEntry>();
 		for(TNTEntry e : this.tntEntries)
 		{
 			if(e.equals(dungeon))
 				subSet.add(e);
 		}
 		return subSet;
 	}
 }
