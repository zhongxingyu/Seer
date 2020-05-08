 package com.lostaris.bukkit.MineralScanner;
 
 import java.util.Vector;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Handle events for all Player related events
  * @author Lostaris
  */
 public class MineralScannerPlayerListener extends PlayerListener {
     private final MineralScanner plugin;
     private Player player;
 
     public MineralScannerPlayerListener(MineralScanner instance) {
         plugin = instance;
     }
     
     // triggered when the item in a players hand changes
     public void onItemHeldChange (PlayerItemHeldEvent event) {
     	
     }
     
     // triggered when a player moves
 	public void onPlayerMove (PlayerMoveEvent event) {
 		player = event.getPlayer();
 		player.sendMessage("standing on " + blockUnderPlayer().getType());
 	}
 	
 	public Block blockUnderPlayer() {
 		Location loc = player.getLocation();
		Block blockUnder = player.getWorld().getBlockAt((int)Math.floor(loc.getX()),
				(int)Math.floor(loc.getY() - 0.5D), (int)Math.floor(loc.getZ()));		
 		return blockUnder;
 		
 	}
 
     //Insert Player related code here
 	public String direction() {
 		int r = (int)Math.abs((player.getLocation().getYaw() - 90.0F) % 360.0F);
 	    String dir;
 	    if (r < 23) { dir = "N";
 	    }
 	    else
 	    {
 	      if (r < 68) { dir = "NE";
 	      }
 	      else
 	      {
 	        if (r < 113) { dir = "E";
 	        }
 	        else
 	        {
 	          if (r < 158) { dir = "SE";
 	          }
 	          else
 	          {
 	            if (r < 203) { dir = "S";
 	            }
 	            else
 	            {
 	              if (r < 248) { dir = "SW";
 	              }
 	              else
 	              {
 	                if (r < 293) { dir = "W";
 	                }
 	                else
 	                {
 	                  if (r < 338) dir = "NW"; else
 	                    dir = "N"; 
 	                }
 	              }
 	            }
 	          }
 	        }
 	      }
 	    }
 	    return dir;
 	}
 	
 	public MineralScanner getPlugin() {
 		return plugin;
 	}
 }
 
