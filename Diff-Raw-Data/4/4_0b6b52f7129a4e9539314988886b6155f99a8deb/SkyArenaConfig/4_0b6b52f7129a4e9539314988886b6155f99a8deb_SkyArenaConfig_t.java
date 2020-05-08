 /*
  * Copyright (C) 2013 daboross
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.skywars.api.arenaconfig;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import net.daboross.bukkitdev.skywars.api.Parentable;
 import net.daboross.bukkitdev.skywars.api.location.SkyPlayerLocation;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 /**
  *
  * @author daboross
  */
 @SerializableAs("SkyArenaConfig")
 public class SkyArenaConfig extends Parentable<SkyArenaConfig> implements ConfigurationSerializable, SkyArena {
 
     private List<SkyPlayerLocation> spawns;
     private Integer numPlayers;
     private final SkyBoundariesConfig boundaries = new SkyBoundariesConfig();
     private final SkyMessagesConfig messages = new SkyMessagesConfig();
     private File file;
 
     public SkyArenaConfig(SkyArenaConfig parent, List<SkyPlayerLocation> spawns, Integer numPlayers, SkyBoundariesConfig boundaries, SkyMessagesConfig messages) {
         super(parent);
         if (numPlayers != null && numPlayers < 2) {
             throw new IllegalArgumentException("Num players can't be smaller than 2");
         }
         this.parent = parent;
         this.spawns = spawns;
         this.numPlayers = numPlayers;
         if (parent != null) {
             this.boundaries.setParent(parent.getBoundaries());
         }
         if (boundaries != null) {
             this.boundaries.copyDataFrom(boundaries);
         }
         if (messages != null) {
             this.messages.copyDataFrom(messages);
         }
     }
 
     public SkyArenaConfig(List<SkyPlayerLocation> spawns, Integer numPlayers, SkyBoundariesConfig boundaries, SkyMessagesConfig messages) {
         if (numPlayers != null && numPlayers < 2) {
             throw new IllegalArgumentException("Num players can't be smaller than 2");
         }
         this.spawns = spawns;
         this.numPlayers = numPlayers;
         if (parent != null) {
             this.boundaries.setParent(parent.getBoundaries());
         }
         if (boundaries != null) {
             this.boundaries.copyDataFrom(boundaries);
         }
         if (messages != null) {
             this.messages.copyDataFrom(messages);
         }
     }
 
     @Override
     public void setParent(SkyArenaConfig parent) {
         super.setParent(parent);
         if (parent != null) {
             messages.setParent(parent.getMessages());
             boundaries.setParent(parent.getBoundaries());
         } else {
             messages.setParent(null);
             boundaries.setParent(null);
         }
     }
 
     /**
      * This is never used by SkyArenaConfig itself, only by things that use
      * getFile()
      *
      * @param file a value to be returned by getFile()
      */
     public void setFile(File file) {
         this.file = file;
     }
 
     /**
      * This is never set except for when something uses setFile()
      *
      * @return the value set with setFile()
      */
     public File getFile() {
         return file;
     }
 
     @Override
     public List<SkyPlayerLocation> getSpawns() {
         if (spawns == null) {
             if (parent == null) {
                 throw new IllegalStateException("Ultimate parent spawns not found.");
             } else {
                 return parent.getSpawns();
             }
         } else {
             return spawns;
         }
     }
 
     @Override
     public void setSpawns(List<SkyPlayerLocation> spawns) {
         this.spawns = spawns;
     }
 
     @Override
     public int getNumPlayers() {
         if (numPlayers == null) {
             if (parent == null) {
                 throw new IllegalStateException("Ultimate parent numPlayers not found.");
             } else {
                 return parent.getNumPlayers();
             }
         } else {
             return numPlayers.intValue();
         }
     }
 
     @Override
     public void setNumPlayers(Integer numPlayers) {
         if (numPlayers != null && numPlayers < 2) {
             throw new IllegalArgumentException("Num players can't be smaller than 2");
         }
         this.numPlayers = numPlayers;
     }
 
     @Override
     public SkyBoundariesConfig getBoundaries() {
         return boundaries;
     }
 
     @Override
     public SkyMessagesConfig getMessages() {
         return messages;
     }
 
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("spawns", spawns);
         map.put("num-players", numPlayers);
         if (boundaries.definesAnything()) {
             map.put("boundaries", boundaries);
         }
         if (messages.definesAnything()) {
             map.put("messages", messages);
         }
         return map;
     }
 
     public void serialize(ConfigurationSection section) {
         List<Map> spawnsList = new ArrayList<Map>(spawns.size());
         for (SkyPlayerLocation loc : spawns) {
             spawnsList.add(loc.serialize());
         }
         section.set("spawns", spawnsList);
         section.set("num-players", numPlayers);
         if (boundaries.definesAnything()) {
             boundaries.serialize(section.createSection("boundaries"));
         }
         if (messages.definesAnything()) {
             messages.serialize(section.createSection("messages"));
         }
     }
 
     public static SkyArenaConfig deserialize(Map<String, Object> map) {
         Object spawnsObj = map.get("spawns"),
                 numPlayersObj = map.get("num-players"),
                 boundariesObj = map.get("boundaries"),
                 messagesObj = map.get("messages");
         List<?> spawns = spawnsObj instanceof List ? (List) spawnsObj : null;
         if (spawns != null) {
             for (Object obj : spawns) {
                 if (!(obj instanceof SkyPlayerLocation)) {
                     Bukkit.getLogger().log(Level.WARNING, "[SkyWars] [SkyArenaConfig] Silently ignoring whole spawn list because one item in list is not a SkyPlayerLocation");
                     spawns = null;
                     break;
                 }
             }
         }
         Integer numPlayers = numPlayersObj instanceof Integer ? (Integer) numPlayersObj : null;
         SkyBoundariesConfig boundaries = boundariesObj instanceof SkyBoundariesConfig ? (SkyBoundariesConfig) boundariesObj : null;
         SkyMessagesConfig messages = messagesObj instanceof SkyMessagesConfig ? (SkyMessagesConfig) messagesObj : null;
         return new SkyArenaConfig((List<SkyPlayerLocation>) spawns, numPlayers, boundaries, messages);
     }
 
     public static SkyArenaConfig deserialize(ConfigurationSection configurationSection) {
         Object numPlayersObj = configurationSection.get("num-players");
         ConfigurationSection boundariesSection = configurationSection.getConfigurationSection("boundaries"),
                 messagesSection = configurationSection.getConfigurationSection("messages");
         List<?> spawnsObjList = configurationSection.getList("spawns");
         List<SkyPlayerLocation> spawns = null;
         if (spawnsObjList != null) {
             spawns = new ArrayList<SkyPlayerLocation>(spawnsObjList.size());
             for (Object obj : spawnsObjList) {
                 if (obj instanceof Map) {
                     SkyPlayerLocation loc = SkyPlayerLocation.deserialize((Map) obj);
                     if (loc == null) {
                         continue;
                     }
                     spawns.add(loc);
                 } else {
                     Bukkit.getLogger().log(Level.WARNING, "[SkyWars] [SkyArenaConfig] Non-Map object {0} found in arena configuration spawn list. Ignoring it", obj);
                 }
             }
         }
         Integer numPlayers = numPlayersObj instanceof Integer ? (Integer) numPlayersObj : null;
         SkyBoundariesConfig boundaries = boundariesSection != null ? SkyBoundariesConfig.deserialize(boundariesSection) : null;
         SkyMessagesConfig messages = messagesSection != null ? SkyMessagesConfig.deserialize(messagesSection) : null;
         return new SkyArenaConfig(spawns, numPlayers, boundaries, messages);
     }
 
     @Override
     public String toString() {
         return "ArenaConfig{parent=" + parent + ",spawns=" + spawns + ",numPlayers=" + numPlayers + ",boundaries=" + boundaries + ",messages=" + messages + "}";
     }
 
     public String toNiceString(int indent) {
         // This line is to make it farther down.
         // More
         // More
         return indent_(indent) + "ArenaConfig{\n"
                 + parent == null ? "" : (indent(indent) + "parent=" + parent.toNiceString(indent + 1) + ",\n")
                + spawns == null ? "" : (indent(indent) + "spawns=" + spawns + ",\n")
                + numPlayers == null ? "" : (indent(indent) + "numPlayers=" + numPlayers + ",\n")
                 + boundaries == null ? "" : (indent(indent) + "boundaries=" + boundaries.toNiceString(indent + 1) + ",\n")
                 + messages == null ? "" : (indent(indent) + "messages=" + messages.toNiceString(indent + 1) + "\n")
                 + indent_(indent) + "}";
     }
 
     /**
      * Undescriptive name for shortness
      */
     private String indent_(int indent) {
         return StringUtils.repeat("\t", indent);
     }
 
     /**
      * Undescriptive name for shortness
      */
     private String indent(int indent) {
         return StringUtils.repeat("\t", indent + 1);
     }
 }
