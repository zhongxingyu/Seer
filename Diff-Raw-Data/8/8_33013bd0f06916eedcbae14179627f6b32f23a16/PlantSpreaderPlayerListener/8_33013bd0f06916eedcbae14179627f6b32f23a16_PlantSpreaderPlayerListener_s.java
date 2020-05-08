 package com.bukkit.mg.plantspreader;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Handle events for all Player related events
  * @author Master-Guy
  */
 
 public class PlantSpreaderPlayerListener extends PlayerListener {
     private final PlantSpreader plugin;
 	private Integer I, X, Y, Z, T, minY, minZ, maxX, maxY, maxZ, foundBlock, rn, rn2, patchCount, pmxX, pmxY, pmxZ, pmnX, pmnY, pmnZ, possibleSpreads;
 	public HashMap<String, Integer> speadOptions = new HashMap<String, Integer>();
 
     public PlantSpreaderPlayerListener(PlantSpreader instance) {
         plugin = instance;
     }
     
     public void onPlayerMove(PlayerMoveEvent event) {
     	Random generator = new Random();
     	if(event.getPlayer().getLocation().getBlockX() == Math.round(event.getPlayer().getLocation().getBlockX())
     	|| event.getPlayer().getLocation().getBlockZ() == Math.round(event.getPlayer().getLocation().getBlockZ())) {
 			rn = generator.nextInt(300);
 			if(rn == 50) {
 				possibleSpreads = 0;
 	    		I = 0;
 		    	while (I < this.plugin.getServer().getOnlinePlayers().length) {
 		    		X = this.plugin.getServer().getOnlinePlayers()[I].getLocation().getBlockX()-25;
 		    		Y = this.plugin.getServer().getOnlinePlayers()[I].getLocation().getBlockY()-5;
 		    		Z = this.plugin.getServer().getOnlinePlayers()[I].getLocation().getBlockZ()-25;
 		    		minY = Y;
 		    		minZ = Z;
 		    		maxX = this.plugin.getServer().getOnlinePlayers()[I].getLocation().getBlockX()+25;
 		    		maxY = this.plugin.getServer().getOnlinePlayers()[I].getLocation().getBlockY()+5;
 		    		maxZ = this.plugin.getServer().getOnlinePlayers()[I].getLocation().getBlockZ()+25;
 		    		while(X < maxX) {
 		    			Y = minY;
 		    			while(Y < maxY) {
 		    				Z = minZ;
 		        			while(Z < maxZ) {
 		        				foundBlock = this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y, Z);
 		        				if(foundBlock == 37 || foundBlock == 38 || foundBlock == 39 || foundBlock == 40 || foundBlock == 86) {
 		        					patchCount = 0;
 		        					pmnX = X-9;
 		        					pmxX = X+9;
 		        					while(pmnX < pmxX) {
 			        					pmnY = Y-2;
 			        					pmxY = Y+2;
 			        					while(pmnY < pmxY) {
 				        					pmnZ = Z-9;
 				        					pmxZ = Z+9;
 				        					while(pmnZ < pmxZ) {
 				        						if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(pmnX, pmnY, pmnZ) == foundBlock) {
 				        							patchCount = patchCount + 1;
 				        						}
 				        						pmnZ = pmnZ + 1;
 				        					}
 			        						pmnY = pmnY + 1;
 			        					}
 		        						pmnX = pmnX + 1;
 		        					}
 		        					if(patchCount < 10) {
 			        					if (
 			        						(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X-1, Y, Z) == 0) ||
 			        						(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X+1, Y, Z) == 0) ||
 			        						(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y, Z-1) == 0) ||
 			        						(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y, Z+1) == 0)
 			        					) {
 		        							if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X-1, Y, Z) == 0) {
 		        								if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X-1, Y-1, Z) == this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y-1, Z)) {
 		        									possibleSpreads = possibleSpreads + 1;
 		        									speadOptions.put(possibleSpreads+"X", X-1);
 		        									speadOptions.put(possibleSpreads+"Y", Y);
 		        									speadOptions.put(possibleSpreads+"Z", Z);
 		        									speadOptions.put(possibleSpreads+"T", foundBlock);
 		        								} else {
 		        								}
 		        							}
 		        							if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X+1, Y, Z) == 0) {
 		        								if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X+1, Y-1, Z) == this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y-1, Z)) {
 		        									possibleSpreads = possibleSpreads + 1;
 		        									speadOptions.put(possibleSpreads+"X", X+1);
 		        									speadOptions.put(possibleSpreads+"Y", Y);
 		        									speadOptions.put(possibleSpreads+"Z", Z);
 		        									speadOptions.put(possibleSpreads+"T", foundBlock);
 		        								}
 		        							}
 		        							if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y, Z-1) == 0) {
 		        								if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y-1, Z-1) == this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y-1, Z)) {
 		        									possibleSpreads = possibleSpreads + 1;
 		        									speadOptions.put(possibleSpreads+"X", X);
 		        									speadOptions.put(possibleSpreads+"Y", Y);
 		        									speadOptions.put(possibleSpreads+"Z", Z-1);
 		        									speadOptions.put(possibleSpreads+"T", foundBlock);
 		        								}
 		        							}
 		        							if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y, Z+1) == 0) {
 		        								if(this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y-1, Z+1) == this.plugin.getServer().getWorlds()[0].getBlockTypeIdAt(X, Y-1, Z)) {
 				        							possibleSpreads = possibleSpreads + 1;
 		        									speadOptions.put(possibleSpreads+"X", X);
 		        									speadOptions.put(possibleSpreads+"Y", Y);
 		        									speadOptions.put(possibleSpreads+"Z", Z+1);
 		        									speadOptions.put(possibleSpreads+"T", foundBlock);
 		        								}
 		        							}
 		        						}
 		        					}
 		        				}
 		            			Z = Z + 1;
 		            		}
 		        			Y = Y + 1;
 		        		}
 		    			X = X + 1;
 		    		}
 		    		I = I + 1;
 		    	}
 		    	if(possibleSpreads > 0) {
 			    	rn2 = generator.nextInt(possibleSpreads);
 			    	X = speadOptions.get(rn2+"X");
 			    	Y = speadOptions.get(rn2+"Y");
 			    	Z = speadOptions.get(rn2+"Z");
 			    	T = speadOptions.get(rn2+"T");
 			    	try{
 			    		this.plugin.getServer().getWorlds()[0].getBlockAt(X, Y, Z).setTypeId(T);
			    	} finally {
 			    	}
			    	speadOptions.clear();
 		    	}
 			}
     	}
     }
     
     public void log(String logText) {
     	System.out.println(logText);
     }
 }
