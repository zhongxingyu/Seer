 package org.nationsatwar.playtime;
 
 import java.util.*;
 import java.util.logging.Logger;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
public class Playtime extends JavaPlugin
 {
 	Logger log;
 	HashMap<String, PlaytimeEvent> map;
 	PlaytimeEvent temp;
 	
 	public void onEnable()
 	{
 		// read external file for current event stuff
 		log = this.getLogger();
 		map = new HashMap<String,PlaytimeEvent>();
 		log.info("Playtime has been enabled!");
 	}
 	
 	public void onDisable()
 	{
 		log.info("Playtime has been disabled!");
 	}
 	
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
     {
     	Player player = null;
     	if(sender instanceof Player)
     	{
     		player = (Player) sender;
     	}
     	
     	if(cmd.getName().equalsIgnoreCase("event"))
     	{ 
 			if(args.length >= 1)
 			{
 	    		if(args[0].equalsIgnoreCase("create"))
 	    		{
 					if(args.length >= 2)
 					{
 		    			if(player != null)
 		    			{
 			    			if(player.hasPermission("playtime.admins"))
 						    {
 		    					if(map.get(args[1]) == null) // makes sure event with name does not already exist
 		    					{
 		    						if(args.length >= 3)
 		    						{
 		    							if(args[2].equalsIgnoreCase("hidden"))
 		    							{
 			    							temp = new PlaytimeEvent(args[1], true);
 				    						map.put(args[1],temp);
 		    							}
 		    						}
 		    						else
 		    						{
 			    						temp = new PlaytimeEvent(args[1]);
 			    						map.put(args[1],temp);
 			    						// also put event in file
 		    						}
 		    						player.sendMessage("Event '"+args[1]+"' successfully created.");
 		    					}
 		    					else
 		    					{
 		    						// error; event already exists.
 		    						player.sendMessage("Error: event '"+args[1]+"' already exists.");
 		    					}
 						    }
 		    			}
 		    			else // user is server
 		    			{
 							if(map.get(args[1]) == null) // makes sure event with name does not already exist
 							{
 		    					temp = new PlaytimeEvent(args[1]);
 		    					map.put(args[1],temp);
 		    					// also put event in file
 		    					log.info("Event '"+args[1]+"' successfully created.");
 							}
 							else
 							{
 								// error; event already exists.
 								log.info("Error: event '"+args[1]+"' already exists.");
 							}
 		    			}
 					}
 					else
 					{
 						// instructions on use of command
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("end"))
 	    		{
 					if(args.length >= 2) // event name
 					{
 		    			if(player != null)
 		    			{
 			    			if(player.hasPermission("playtime.admins"))
 						    {
 		    					if(map.get(args[1]) != null) // if event with name is found
 		    					{
 		    						map.remove(args[1]);
 			    					// remove event from file
 			    					// remove subscriptions, too
 		    						// message to player using command
 		    						// general message to players (subscribed to event?) that event has ended
 		    						player.sendMessage("Event '"+args[1]+"' successfully ended.");
 		    					}
 		    					else
 		    					{
 		    						// error; event does not exist
 		    						player.sendMessage("Error: event '"+args[1]+"' does not exist.");
 		    					}
 						    }
 		    			}
 		    			else // user is server
 		    			{
 		    				if(args[1] != null)
 		    				{
 		    					if(map.get(args[1]) != null)
 		    					{
 		    						map.remove(args[1]);
 		        					// remove event from file
 		        					// remove subscriptions, too
 		    						// general message to players (subscribed to event?) that event has ended
 		    						log.info("Event '"+args[1]+"' successfully ended.");
 		    					}
 		    					else
 		    					{
 		    						log.info("Error: event '"+args[1]+"' does not exist.");
 		    					}
 		    				}
 		    			}
 					}
 					else
 					{
 						// instructions on use of command
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("setspawn"))
 	    		{
 					if(args.length >= 2) // event name
 					{
 		    			if(player != null) // user is player
 		    			{
 			    			if(player.hasPermission("playtime.admins"))
 						    {
 		    					if(map.get(args[1]) != null) // if event with name is found
 		    					{
 		    						if(args.length >= 3)
 		    						{
			    	    				if(args[2] != null) // player name or nothing
 			    	    				{
 			    	    					// player name was provided
 			    							Player p = null;
 			    	    					p = getServer().getPlayer(args[2]);
 				    						if(p != null)
 				    						{ 
 				    							// get player from server
 				    	    					// set player as event spawn
 				    	    					temp = map.get(args[1]);
 				    	    					temp.setSpawn(p);
 				    	    					map.put(args[1],temp);
 					    						player.sendMessage(p.getName()+" set as spawn for event "+args[1]);
 				    						}
 				    						else
 				    						{
 					    						player.sendMessage("Error: " + args[2] + " is not a valid player.");
 				    						}
 			    	    				}
			    	    				else
			    	    				{
			    	    					// no player provided, so take user's current location as spawn
			    	    					temp = map.get(args[1]);
			    	    					temp.setSpawn(player.getLocation());
			    	    					map.put(args[1],temp);
				    						player.sendMessage("Current location set as spawn for event "+args[1]);
			    	    				}
 		    						}
 		    					}
 		    					else
 		    					{
 		    						player.sendMessage("Error: event '"+args[1]+"' does not exist.");
 		    					}
 						    }
 		    			}
 		    			else // user is server
 		    			{
 	    					if(map.get(args[1]) != null) // if event with name is found
 	    					{
 	    						if(args.length >= 3)
 	    						{
 		    	    				if(args[2] != null) // player name or nothing
 		    	    				{
 		    	    					// player name was provided
 		    							Player p = null;
 		    	    					p = getServer().getPlayer(args[2]);
 			    						if(p != null)
 			    						{
 			    							// get player from server
 			    	    					// set player as event spawn
 			    	    					temp = map.get(args[1]);
 			    	    					temp.setSpawn(p);
 			    	    					map.put(args[1],temp);
 			    	    					log.info(p.getName()+" set as spawn for event "+args[1]);
 			    						}
 			    						else
 			    						{
 				    						log.info("Error: " + args[2] + " is not a valid player.");
 			    						}
 		    	    				}
 		    	    				else
 		    	    				{
 		    	    					// no player provided, so this can't be used
 		    	    					log.info("Error: must specify player");
 		    	    				}
 	    						}
 	    					}
 	    					else
 	    					{
 	    						log.info("Error: event '"+args[1]+"' does not exist.");
 	    					}
 		    			}
 					}
 					else
 					{
 						// help text
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("subscribe"))
 	    		{
 					if(args.length >= 2) // if event name has been provided
 					{
 						if(map.get(args[1]) != null) // if event is valid
 						{
 			    			if(player != null)
 			    			{
 				    			if(player.hasPermission("playtime.admins")) // check if player has permission to subscribe other players
 							    {
 		    						if(args.length >= 3)
 		    						{
 			    						// subscribing named player to event
 			    						// get player from name
 		    							Player p = null;
 		    							p = getServer().getPlayer(args[2]);
 			    						if(p != null)
 			    						{
 			    							// get player from server
 				    						boolean s = false;
 				    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 				    						{
 				    							if(t.isSubscribed(p.getName()))
 				    								s = true;
 				    						}
 			
 				    						if(!s) // subscribe player to event
 				    						{
 				    							temp.subscribe(p); 
 				    							map.put(args[1],temp);
 				    							player.sendMessage(p.getName() + " subscribed to event " + args[1] + ".");
 				    							p.sendMessage("You have been subscribed to event " + args[1] + " by " + player.getName() + ".");
 					    						// do not notify player if event is hidden?
 				    							// store in file
 				    						}
 				    						else
 				    						{
 				    							player.sendMessage(p.getName() + " is already subscribed to an event.");
 				    							// feedback on which event?
 				    						}
 			    						}
 			    						else
 			    						{
 				    						player.sendMessage("Error: " + args[2] + " is not a valid player.");
 			    						}
 		    						}
 			    					else // subscribing self to event
 			    					{
 			    						temp = map.get(args[1]);
 			    						boolean s = false;
 			    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 			    						{
 			    							if(t.isSubscribed(player.getName()))
 			    								s = true;
 			    						}
 		
 			    						if(!s) // if player is not subscribed
 			    						{
 			    							temp.subscribe(player);
 			    							map.put(args[1],temp);
 			    							player.sendMessage("You have subscribed to event " + args[1] + ".");
 			    							// store in file
 			    						}
 			    						else
 			    						{
 			    							player.sendMessage("You are already subscribed to an event.");
 			    						}
 			    					}
 							    }
 				    			else // player does not have permission; can only subscribe self to event
 				    			{
 			    					if(map.get(args[1]) != null) // check event exists
 			    					{
 			    						temp = map.get(args[1]);
 			    						boolean s = false;
 			    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 			    						{
 			    							if(t.isSubscribed(player.getName()))
 			    								s = true;
 			    						}
 		
 			    						if(!s) // if player is not subscribed
 			    						{
 			    							temp.subscribe(player);
 			    							map.put(args[1],temp);
 			    							player.sendMessage("You have subscribed to event " + args[1] + ".");
 			    							// store in file
 			    						}
 			    						else
 			    						{
 			    							player.sendMessage("You are already subscribed to an event.");
 			    						}
 			    					}
 			    					else
 			    					{
 			    						player.sendMessage("Error: event '"+args[1]+"' does not exist.");
 			    					}
 				    			}
 			    			}
 			    			else // server is user
 			    			{
 			    				// can only subscribe others
 	    						if(args.length >= 3)
 	    						{
 		    						// subscribing named player to event
 		    						// get player from name
 	    							Player p = null;
 	    							p = getServer().getPlayer(args[2]);
 		    						if(p != null)
 		    						{
 		    							// get player from server
 			    						boolean s = false;
 			    						for (PlaytimeEvent t : map.values()) // check whether player is already subscribed to an event
 			    						{
 			    							if(t.isSubscribed(p.getName()))
 			    								s = true;
 			    						}
 		
 			    						if(!s) // subscribe player to event
 			    						{
 			    							temp.subscribe(p); 
 			    							map.put(args[1],temp);
 			    							log.info(p.getName() + " subscribed to event " + args[1] + ".");
 			    							p.sendMessage("You have been subscribed to event " + args[1] + " by server.");
 				    						// do not notify player if event is hidden?
 			    							// store in file
 			    						}
 			    						else
 			    						{
 			    							log.info(p.getName() + " is already subscribed to an event.");
 			    							// feedback on which event?
 			    						}
 		    						}
 		    						else
 		    						{
 			    						log.info("Error: " + args[2] + " is not a valid player.");
 		    						}
 	    						}
 			    			}
 						}
 						else
 						{
 							if (player != null)
 							{
 								player.sendMessage("Error: " + args[1] + " is not a valid event");
 							}
 							else
 							{
 								log.info("Error: " + args[1] + " is not a valid event");
 							}
 						}
 					}
 					else
 					{
 						// help text
 					}
 					return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("unsubscribe"))
 	    		{
 	    			if(player != null)
 	    			{
 		    			if(player.hasPermission("playtime.admins"))
 					    {
     						if(args.length >= 3) // if a name has been provided, admin wants to unsubscribe player from event
     						{
     							Player p = null;
     							p = getServer().getPlayer(args[2]);
 	    						if(p != null)
 	    						{
 	    							boolean s = false;
 		    						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
 		    						{
 		    							if(t.isSubscribed(p.getName()))
 		    							{
 		    								t.unsubscribe(p);
 		    								player.sendMessage(p.getName() + " unsubscribed from event " + t.getName());
 		    								p.sendMessage("You have been unsubscribed from event " + t.getName() + " by " + player.getName());
 		    								s = true;
 		    							}
 		    						}
 		    						if(!s)
 		    						{
 		    							player.sendMessage("Error: " + p.getName() + " is not subscribed to any event.");
 		    						}
 	    						}
 	    						else
 	    						{
 		    						player.sendMessage("Error: " + args[2] + " is not a valid player.");
 	    						}
     						}
     						else // otherwise, admin just wants to unsubscribe from an event they're in
     						{
     							boolean s = false;
 	    						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
 	    						{
 	    							if(t.isSubscribed(player.getName()))
 	    							{
 	    								t.unsubscribe(player);
 	    								player.sendMessage("You have unsubscribed from event " + t.getName());
 	    								s = true;
 	    							}
 	    						}
 	    						if(!s)
 	    						{
 	    							player.sendMessage("Error: you are not subscribed to any event.");
 	    						}
     						}
 					    }
 		    			else // player can only unsubscribe themselves
 		    			{
 							boolean s = false;
     						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
     						{
     							if(t.isSubscribed(player.getName()))
     							{
     								t.unsubscribe(player);
     								player.sendMessage("You have unsubscribed from event " + t.getName());
     								s = true;
     							}
     						}
     						if(!s)
     						{
     							player.sendMessage("Error: you are not subscribed to any event.");
     						}
 		    			}
 	    			}
 	    			else
 	    			{
 	    				// server wants to unsubscribe someone
 	    				if(args.length >= 3) // if a name has been provided
 						{
 							Player p = null;
 							p = getServer().getPlayer(args[2]);
     						if(p != null)
     						{
     							boolean s = false;
 	    						for (PlaytimeEvent t : map.values()) // find if player is subscribed to event
 	    						{
 	    							if(t.isSubscribed(p.getName()))
 	    							{
 	    								t.unsubscribe(p);
 	    								log.info(p.getName() + " unsubscribed from event " + t.getName());
 	    								p.sendMessage("You have been unsubscribed from event " + t.getName() + " by server");
 	    								s = true;
 	    							}
 	    						}
 	    						if(!s)
 	    						{
 	    							log.info("Error: " + p.getName() + " is not subscribed to any event.");
 	    						}
     						}
     						else
     						{
     							log.info("Error: " + args[2] + " is not a valid player.");
     						}
 						}
 	    				else
 	    				{
 	    					log.info("Error: must provide player name");
 	    				}
 	    			}
 	    			return true;
 	    		}
 	    		else if(args[0].equalsIgnoreCase("list"))
 	    		{
 	
 	    			if(player != null)
 	    			{
 		    			player.sendMessage("Events in progress: "); // show something else if none are in progress
 		    			for (PlaytimeEvent value : map.values()) 
 		    			{
 		    				if(value.isHidden())
 		    				{
 		    					if(player.hasPermission("playtime.admins"))
 							    {
 			    					player.sendMessage(value.getInfo());
 							    }
 		    				}
 		    				else
 		    					player.sendMessage(value.getInfo());
 		    			}
 	    			}
 	    			else
 	    			{
 	    				log.info("Events in progress: "); // show something else if none are in progress
 		    			for (PlaytimeEvent value : map.values()) 
 		    			{
 		    				log.info(value.getInfo());
 		    			}
 	    			}
 	    			return true;
 	    		}
 				return true;
 	    	}
 			return true;
     	// remember to add return trues where necessary
     	}
 	    return false;
     }
 }
