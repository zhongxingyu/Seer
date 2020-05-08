 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 package com.andune.blockowner;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import com.andune.minecraft.commonlib.JarUtils;
 import com.andune.minecraft.commonlib.Logger;
 import com.andune.minecraft.commonlib.LoggerFactory;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BlockOwner extends JavaPlugin
 {
    private final Logger log = LoggerFactory.getLogger(BlockOwner.class);
 
 	private JarUtils jarUtils;
 	private String buildNumber = "unknown";
 	private GlobalChunkManager globalChunkManager;
 	
 	@Override
 	public void onEnable() {
     	jarUtils = new JarUtils(getDataFolder(), getFile());
 		buildNumber = jarUtils.getBuild();
 
         File debugFlagFile = new File(getDataFolder(), "debug");
         if (debugFlagFile.exists())
             LoggerFactory.getLogUtil().enableDebug("com.andune.blockowner");
 
 		Owners owners = new Owners(new YamlConfiguration(), new File(getDataFolder(), "owners.yml"));
 		try {
 			owners.loadConfig();
 		}
 		catch(Exception e) {
 			log.error("Error loading owners", e);
 		}
 		
 		globalChunkManager = new GlobalChunkManager(new FileChunkStorage(this), owners);
 		getServer().getPluginManager().registerEvents(globalChunkManager, this);
 		getServer().getPluginManager().registerEvents(new BlockListener(globalChunkManager), this);
 		getServer().getPluginManager().registerEvents(new PlayerListener(globalChunkManager), this);
 		
 		log.info("version " + getDescription().getVersion() + ", build " + buildNumber + " is enabled");
 	}
 	
 	@Override
 	public void onDisable() {
 		log.info("version " + getDescription().getVersion() + ", build " + buildNumber + " is disabled");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 	    if( "bosave".equalsIgnoreCase(label) ) {
 	        globalChunkManager.saveAll();
             sender.sendMessage("All chunks saved");
 	    }
 	    else if( "boq".equalsIgnoreCase(label) ) {
 	        if( sender instanceof Player ) {
 	            Player p = (Player) sender;
 	            List<Block> los = p.getLineOfSight(null, 100);
 	            if( los != null ) {
 	                Block b = null;
 	                for(int i=0; i < 99; i++) {
 	                    b = los.get(i);
 	                    if( !b.isEmpty() )
 	                        break;
 	                }
 	                if( b != null ) {
     	                String owner = globalChunkManager.getBlockOwner(b);
     	                sender.sendMessage("Block owner for block "+b+" is "+owner);
 	                }
 	            }
 	        }
 	    }
 	    else if( "boa".equalsIgnoreCase(label) ) {		// area report
 	        if( sender instanceof Player ) {
 	            final Player p = (Player) sender;
 	            final Location l = p.getLocation();
 	            final World world = l.getWorld();
 	            final String worldName = world.getName();
 	            final int playerX = (int) l.getX();
 	            final int playerY = (int) l.getY();
 	            final int playerZ = (int) l.getZ();
 	            final int radius = 10;
 	            
 	            long startTime = System.currentTimeMillis();
 	            int blocksChecked=0;
 	        	final HashMap<String, Integer> ownerCountMap = new HashMap<String, Integer>();
 	            for(int x=playerX-radius; x <= playerX+radius; x++) {
 		            for(int y=playerY-radius; y <= playerY+radius; y++) {
 			            for(int z=playerZ-radius; z <= playerZ+radius; z++) {
 			            	blocksChecked++;
 			            	// fast skip air blocks, they have no owner
 			            	if( world.getBlockTypeIdAt(x,y,z) == 0 )
 			            		continue;
 
 			            	String owner = globalChunkManager.getBlockOwner(worldName, x, y, z);
 			            	if( owner != null ) {
 			            		Integer i = ownerCountMap.get(owner);
 			            		if( i == null )
 			            			i = Integer.valueOf(1);
 			            		else
 			            			i++;
 			            		ownerCountMap.put(owner, i);
 			            	}
 			            }
 		            }
 	            }
 	            
 	            // sort the list
 	            TreeMap<Integer, ArrayList<String>> sortedMap = new TreeMap<Integer, ArrayList<String>>(new Comparator<Integer>() {
 					public int compare(Integer o1, Integer o2) {
 						return o2.compareTo(o1);	// ascending order
 					}
 				});
 	            for(Entry<String, Integer> entry : ownerCountMap.entrySet()) {
 	            	ArrayList<String> list = sortedMap.get(entry.getValue());
 	            	if( list == null ) {
 	            		list = new ArrayList<String>();
 	            		sortedMap.put(entry.getValue(), list);
 	            	}
 	            	list.add(entry.getKey());
 	            }
 	            
 	            int i=0;
 	            sender.sendMessage("Top 10 Block owner report for radius "+radius);
 	            for(Entry<Integer, ArrayList<String>> entry : sortedMap.entrySet()) {
 	            	StringBuffer sb = new StringBuffer("Owner(s) with "+entry.getKey()+" blocks: ");
 	            	for(String owner : entry.getValue()) {
 	            		sb.append(owner);
 	            		sb.append(",");
 	            	}
 	            	sender.sendMessage(sb.substring(0, sb.length()-1));
 	            	
 	            	// we only show the top 10 entries
 	            	if( ++i >= 10 )
 	            		break;
 	            }
 	            long endTime = System.currentTimeMillis();
 	            log.debug("/boa total time running = {}ms (total blocks checked {})", (endTime-startTime), blocksChecked);
 	            sender.sendMessage("/boa total time running = " + (endTime - startTime) + "ms (total blocks checked " + blocksChecked + ")");
 	        }
 	    }
 	    return true;
 	}
 	
 	/** Log a message.
 	 * 
 	 * @param msg
 	 */
     /*
 	public void info(String msg) {
 		log.info(msg);
 	}
 	
 	public void debug(Object...args) {
 		debug.debug(args);
 	}
 	*/
 }
