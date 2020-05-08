 package net.erbros.Lottery;
 //All the imports
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Account;
 
 import net.erbros.Lottery.PluginListener;
 
 
 
 
 public class Lottery extends JavaPlugin{
 
 	protected Integer cost;
 	protected Integer hours;
 	protected Long nextexec;
 	protected Boolean timerStarted;
 	protected Boolean useiConomy;
 	protected Integer material;
 	protected Boolean broadcastBuying;
 	protected Configuration c;
 	// Starting timer we are going to use for scheduling.
 	Timer timer;
 	// The iConomy variables.
 	private static PluginListener PluginListener = null;
 	private static iConomy iConomy = null;
 	private static org.bukkit.Server Server = null;
 
 	
 	// Doing some logging. Thanks cyklo 
 	protected final Logger log;
 	
 	public Lottery() {
 		log = Logger.getLogger("Minecraft");
 		cost = 5;
 		hours = 24;
 		timerStarted = false;
 	}
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " has been unloaded." );
 		//log.info(getDescription().getName() + ": has been disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		
 		// Gets version number and writes out starting line to console.
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
 		
 		// Start Registration. Thanks TheYeti.
 		getDataFolder().mkdirs();
 		
 		// Does config exist? If not, make it.
 		
 		makeConfig();
 		
 		cost = Integer.parseInt(c.getProperty("cost").toString());
 		hours = Integer.parseInt(c.getProperty("hours").toString());
 		useiConomy = Boolean.parseBoolean(c.getProperty("useiConomy").toString());
 		material = Integer.parseInt(c.getProperty("material").toString());
 		broadcastBuying = Boolean.parseBoolean(c.getProperty("broadcastBuying").toString());
 		
 		
 		Server = getServer();
 		// Do we need iConomy?
 		if(useiConomy == true) {
 			// Check if we got iConomy support. If not, no need in starting plugin.
 			
 			PluginListener = new PluginListener();
 		
 			// Event Registration
 			getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, PluginListener, Priority.Monitor, this);
 			
 		}
 		
 		// Listen for some player interaction perhaps? Thanks to cyklo :)
 		
 		getCommand("lottery").setExecutor(new CommandExecutor() {
 			@Override
 			
 			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 				c = getConfiguration();
 				// If its just /lottery, and no args.
 				if(args.length == 0) {
					// Check if we got any money/items in the pot.
					ArrayList<String> players = playersInFile("lotteryPlayers.txt");
					int amount = players.size() * cost;
 					sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Draw in: " + ChatColor.RED + timeUntil(nextexec));
 					if(useiConomy == false) {
 						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Buy a ticket for " + ChatColor.RED +  cost + " " + formatMaterialName(material) + ChatColor.WHITE + " with " + ChatColor.RED + "/lottery buy");
						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There is currently " + ChatColor.GREEN +  amount + " " + formatMaterialName(material) + ChatColor.WHITE + " in the pot.");
 					} else {
 						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Buy a ticket for " + ChatColor.RED + iConomy.getBank().format(cost) + ChatColor.WHITE + " with " + ChatColor.RED + "/lottery buy");
						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There is currently " + ChatColor.GREEN +  iConomy.getBank().format(amount) + " " + formatMaterialName(material) + ChatColor.WHITE + " in the pot.");
 					}
 					sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.RED + "/lottery help" + ChatColor.WHITE + " for other commands");
 					// Does lastwinner exist and != null? Show.
 					// Show different things if we are using iConomy over material.
 					if(useiConomy == true) {
 						if(c.getProperty("lastwinner") != null) {
 							sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Last winner: " + c.getProperty("lastwinner") + " (" + iConomy.getBank().format(Integer.parseInt(c.getProperty("lastwinneramount").toString())) + ")");
 						} 
 						
 					} else {
 						if(c.getProperty("lastwinner") != null) {
 							sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Last winner: " + c.getProperty("lastwinner") + " (" + c.getProperty("lastwinneramount").toString() + " " + formatMaterialName(material) + ")");
 						} 
 					}
 					
 					// if not iConomy, make players check for claims.
 					if(useiConomy == false) {
 						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Check if you have won with " + ChatColor.RED + "/lottery claim");
 					} 
 					
 				} else {
 					if(args[0].equalsIgnoreCase("buy")) {
 						Player player = (Player) sender;
 						
 						if(addPlayer(player) == true) {
 							// You got your ticket. 
 							if(useiConomy == false) {
 								sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You got your lottery ticket for " + ChatColor.RED +  cost + " " + formatMaterialName(material));
 							} else {
 								sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You got your lottery ticket for " + ChatColor.RED + iConomy.getBank().format(cost));
 							}
 							if(broadcastBuying == true) {
 								Server.broadcastMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + player.getDisplayName() + " just bought a ticket.");
 							}
 							
 						} else {
 							// You can't buy more than one ticket.
 							sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Either you can't afford a ticket, or you got one already.");
 						}
 					} else if(args[0].equalsIgnoreCase("claim")) {
 						removeFromClaimList((Player) sender);
 					} else if(args[0].equalsIgnoreCase("draw")) {
 						// Later add permissions. As of now, is the player op?
 						if(sender.isOp()) {
 							// Start a timer that ends in 3 secs.
 							sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Lottery will be drawn at once.");
 							StartTimerSchedule(true);
 						}
 						
 					} else if(args[0].equalsIgnoreCase("help")) {
 						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Help commands");
 						sender.sendMessage(ChatColor.RED + "/lottery" + ChatColor.WHITE + " : Basic lottery info.");
 						sender.sendMessage(ChatColor.RED + "/lottery buy" + ChatColor.WHITE + " : Buy a ticket.");
 						sender.sendMessage(ChatColor.RED + "/lottery claim" + ChatColor.WHITE + " : Claim outstandig wins.");
 						sender.sendMessage(ChatColor.RED + "/lottery winners" + ChatColor.WHITE + " : Check last winners.");
 					} else if(args[0].equalsIgnoreCase("winners")) {
 						// Get the winners.
 						ArrayList<String> winnerArray = new ArrayList<String>();
 						try {
 						    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + File.separator + "lotteryWinners.txt"));
 						    String str;
 						    while ((str = in.readLine()) != null) {
 						    	winnerArray.add(str);
 						    }
 						    in.close();
 						} catch (IOException e) {
 						}
 						String[] split;
 						String winListPrice;
 						for (int i = 0; i < winnerArray.size(); i++) {
 							split = winnerArray.get(i).split(":");
 							if(split[2].equalsIgnoreCase("0")) {
 								winListPrice = iConomy.getBank().format(Double.parseDouble(split[1]));
 							} else {
 								winListPrice = split[1] + " " + formatMaterialName(Integer.parseInt(split[2])).toString();
 							}
 							sender.sendMessage((i + 1) + ". " + split[0] + " " + winListPrice);
 						}
 					} else {
 						sender.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Hey, I don't recognize that command!");
 					}
 				}
 				
 				return true;
 			}
         });
 
 		
 		
 		// Is the date we are going to draw the lottery set? If not, we should do it.
 		if(c.getProperty("nextexec") == null) {
 			
 			// Set first time to be config hours later? Millisecs, * 1000.
 			nextexec = System.currentTimeMillis() + extendTime();
 			c.setProperty("nextexec", nextexec);
 			
 	        if (!getConfiguration().save())
 	        {
 	            getServer().getLogger().warning("Unable to persist configuration files, changes will not be saved.");
 	        }
 		} else {
 			nextexec = Long.parseLong(c.getProperty("nextexec").toString());
 		}
 		
 		// Start the timer for the first time.
 		StartTimerSchedule(false);
 		
 		// This could, and should, probably be fixed nicer, but for now it'll have to do.
 		// Adding timer that waits the time between nextexec and time now.
 		
 	}
 
 
 	public long extendTime() {
 		Configuration c = getConfiguration();
 		hours = Integer.parseInt(c.getProperty("hours").toString());
 		Long extendTime = Long.parseLong(hours.toString()) * 60 * 60 * 1000;
 		return extendTime;
 	}
 
 	class LotteryDraw extends TimerTask {
 		public void run() {
 			// Cancel timer.
 			// Get new config.
 			c = getConfiguration();
 			nextexec = Long.parseLong(c.getProperty("nextexec").toString());
 			
 			if(nextexec > 0 && System.currentTimeMillis() > nextexec) {
 				// Get the winner, if any. And remove file so we are ready for new round.
 				getWinner();
 				nextexec = System.currentTimeMillis() + extendTime();
 	
 				c.setProperty("nextexec",nextexec);
 				if (!getConfiguration().save())
 		        {
 		            getServer().getLogger().warning("Unable to persist configuration files, changes will not be saved.");
 		        }
 			}
 			// Call a new timer.
 			StartTimerSchedule(false);
 		}
 	}
 
 	private void StartTimerSchedule(boolean drawAtOnce) {
 		
 		
 		long extendtime = 0;
 		//Cancel any existing timers.
 		if(timerStarted == true) {
 			timer.cancel();
 			timer.purge();
 			extendtime = extendTime();
 		} else {
 			// Get time until lottery drawing.
 			extendtime = nextexec - System.currentTimeMillis();
 		}
 		// If the time is passed (perhaps the server was offline?), draw lottery at once.
 		
 		if(extendtime <= 0) {
 			extendtime = 3000;
 		}
 		
 		// Is the drawAtOnce boolean set to true? In that case, do drawing in a few secs.
 		if(drawAtOnce) {
 			extendtime = 1000;
 			c = getConfiguration();
 			c.setProperty("nextexec", System.currentTimeMillis()+1000);
 			nextexec = System.currentTimeMillis()+1000;
 			log.info("DRAW NOW");
 		}
 		
 		
 		// Start new timer.
 		timer = new Timer();
 		timer.schedule(new LotteryDraw(), extendtime);
 		// Timer is now started, let it know.
 		timerStarted = true;
 			
 	}
 	public boolean addPlayer(Player player) {
 
 		// Is the player already listed, and thus already have a ticket?
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + File.separator + "lotteryPlayers.txt"));
 		    String str;
 		    while ((str = in.readLine()) != null) {
 		    	
 		        if(str.equalsIgnoreCase(player.getName())) {
 		        	// Player have bought earlier. Will send false signal to tell.
 		        	in.close();
 		        	return false;
 		        }
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 		
 		// Do the ticket cost money or item?
 	    if(useiConomy == false) {
 	    	// Do the user have the item?
 	    	if(player.getInventory().contains(material, cost)) {
 	    		// Remove items.
 	    		player.getInventory().removeItem( new ItemStack(material, cost));
 	    	} else {
 	    		return false;
 	    	}
 	    } else {
 	    	// Do the player have money?
 	    	Account account = iConomy.getBank().getAccount(player.getName());
 	    	if(account.hasOver(cost-1)) {
 	    		// Removing coins from players account.
 	    		account.subtract(cost);
 	    	} else {
 	    		return false;
 	    	}
 	    	
 	    }
 	    // If the user paid, continue. Else we would already have sent 
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + File.separator + "lotteryPlayers.txt",true));
 		    out.write(player.getName());
 		    out.newLine();
 		    out.close();
 		    
 		    
 		} catch (IOException e) {
 		}
 
 		return true;
 	}
 	
 	public void makeConfig() {
 		c = getConfiguration();
 	
 		if(c.getProperty("broadcastBuying") == null || c.getProperty("cost") == null || c.getProperty("hours") == null || c.getProperty("material") == null  || c.getProperty("useiConomy") == null ) {
 			
 			if(c.getProperty("cost") == null) {
 				c.setProperty("cost", "5");
 			}
 			if(c.getProperty("hours") == null) {
 				c.setProperty("hours", "24");
 			}
 			if(c.getProperty("material") == null) {
 				c.setProperty("material", "266");
 			}
 			if(c.getProperty("useiConomy") == null) {
 				c.setProperty("useiConomy", "true");
 			}
 			
 			if(c.getProperty("broadcastBuying") == null) {
 				c.setProperty("broadcastBuying", "true");
 			}
 			
 		    if (!getConfiguration().save())
 		    {
 		        log.warning("Unable to persist configuration files, changes will not be saved.");
 		    }
 		}
 		
 	}
 	
 	public String timeUntil(long time) {
 
 		double timeLeft = Double.parseDouble(Long.toString(((time - System.currentTimeMillis()) / 1000)));
 		// How many days left?
 		String stringTimeLeft = "";
 		if(timeLeft >= 60 * 60 * 24) {
 			int days = (int) Math.floor(timeLeft / (60 * 60 * 24));
 			timeLeft -= 60 * 60 * 24 * days;
 			if(days == 1) {
 				stringTimeLeft += Integer.toString(days) + " day, ";
 			} else {
 				stringTimeLeft += Integer.toString(days) + " days, ";
 			}
 		}
 		if(timeLeft >= 60 * 60) {
 			int hours = (int) Math.floor(timeLeft / (60 * 60));
 			timeLeft -= 60 * 60 * hours;
 			if(hours == 1) {
 				stringTimeLeft += Integer.toString(hours) + " hour, ";
 			} else {
 				stringTimeLeft += Integer.toString(hours) + " hours, ";
 			}
 		}
 		if(timeLeft >= 60) {
 			int minutes = (int) Math.floor(timeLeft / (60));
 			timeLeft -= 60 * minutes;
 			if(minutes == 1) {
 				stringTimeLeft += Integer.toString(minutes) + " minute ";
 			} else {
 				stringTimeLeft += Integer.toString(minutes) + " minutes ";
 			}
 		} else {
 			// Lets remove the last comma, since it will look bad with 2 days, 3 hours, and 14 seconds.
 			if(stringTimeLeft.equalsIgnoreCase("") == false) {
 				stringTimeLeft = stringTimeLeft.substring(0, stringTimeLeft.length()-1);
 			}
 		}
 		int secs = (int) timeLeft;
 		if(stringTimeLeft.equalsIgnoreCase("") == false) {
 			stringTimeLeft += "and ";
 		}
 		if(secs == 1) {
 			stringTimeLeft += secs + " second.";
 		} else {
 			stringTimeLeft += secs + " seconds.";
 		}
 		
 		return stringTimeLeft;
 	}
 	
 	public boolean getWinner() {
 
 		ArrayList<String> players = playersInFile("lotteryPlayers.txt");
 		
 		if(players.isEmpty() == true) {
 			Server.broadcastMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "No tickets sold this round. Thats a shame.");
 			return false;
 		} else {
 			// Find rand. Do minus 1 since its a zero based array.
 			int rand = 0;
 			if(players.size() == 1) {
 				rand = 0;
 			} else {
 				rand = new Random().nextInt(players.size());
 			}
 			
 			log.info("Rand: " + Integer.toString(rand));
 			int amount = players.size()*cost;
 			if(useiConomy == true) {
 				Account account = iConomy.getBank().getAccount(players.get(rand));
 				account.add(amount);
 				// Announce the winner:
 				Server.broadcastMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Congratulations to " + players.get(rand) + " for winning " + ChatColor.RED + iConomy.getBank().format(amount) + ".");
 				addToWinnerList(players.get(rand), amount, 0);
 			} else {
 				Server.broadcastMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Congratulations to " + players.get(rand) + " for winning " + ChatColor.RED + amount + " " + formatMaterialName(material) + ".");
 				Server.broadcastMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "Use " + ChatColor.RED + "/lottery claim" + ChatColor.WHITE + " to claim the winnings.");
 				addToWinnerList(players.get(rand), amount, material);
 				addToClaimList(players.get(rand), amount, material.intValue());
 			}
 			Server.broadcastMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "There was in total " + players.size() + " players with a lottery ticket.");
 			
 			// Add last winner to config.
 			c = getConfiguration();
 			c.setProperty("lastwinner", players.get(rand));
 			c.setProperty("lastwinneramount", amount);
 			
 			// Clear file.
 			try {
 			    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + File.separator + "lotteryPlayers.txt",false));
 			    out.write("");
 			    out.close();
 			    
 			} catch (IOException e) {
 			}
 		}
 		return true;
 	}
 	
 	public boolean addToWinnerList(String playerName, int winningAmount, int winningMaterial) {
 		// This list should be 10 players long. 
 		ArrayList<String> winnerArray = new ArrayList<String>();
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + File.separator + "lotteryWinners.txt"));
 		    String str;
 		    while ((str = in.readLine()) != null) {
 		    	winnerArray.add(str);
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 		// Then first add new winner, and after that the old winners.
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + File.separator + "lotteryWinners.txt"));
 		    out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
 		    out.newLine();
 		    //How long is the array? We just want the top 9. Removing index 9 since its starting at 0.
 		    if(winnerArray.size() > 0) {
 			    if(winnerArray.size() > 9) {
 					winnerArray.remove(9);
 			    }
 			    // Go trough list and output lines.
 				for (int i = 0; i < winnerArray.size(); i++) {
 					out.write(winnerArray.get(i));
 					out.newLine();
 				}
 		    }
 			out.close();
 		    
 		    
 		} catch (IOException e) {
 		}
 		return true;
 	}
 	
 	public boolean addToClaimList(String playerName, int winningAmount, int winningMaterial) {
 		// Then first add new winner, and after that the old winners.
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + File.separator + "lotteryClaim.txt",true));
 		    out.write(playerName + ":" + winningAmount + ":" + winningMaterial);
 		    out.newLine();
 			out.close();
 		} catch (IOException e) {
 		}
 		return true;
 	}
 	
 	public boolean removeFromClaimList(Player player) {
 		// Do the player have something to claim?
 		ArrayList<String> otherPlayersClaims = new ArrayList<String>();
 		ArrayList<String> claimArray = new ArrayList<String>();
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + File.separator + "lotteryClaim.txt"));
 		    String str;
 		    while ((str = in.readLine()) != null) {
 		    	String[] split = str.split(":");
 		        if(split[0].equals(player.getName())) {
 		        	// Adding this to player claim.
 		        	claimArray.add(str);
 		        } else {
 		        	otherPlayersClaims.add(str);
 		        }
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 		
 		// Did the user have any claims?
 		if(claimArray.size() == 0) {
 			player.sendMessage(ChatColor.GOLD + "[LOTTERY] " + ChatColor.WHITE + "You did not have anything unclaimed.");
 			return false;
 		}
 		// Do a bit payout.
 		for(int i = 0; i < claimArray.size(); i++) {
 			String[] split = claimArray.get(i).split(":");
 			int claimAmount = Integer.parseInt(split[1]);
 			int claimMaterial = Integer.parseInt(split[2]);
 			player.getInventory().addItem( new ItemStack(claimMaterial, claimAmount));
 			player.sendMessage("You just claimed " + claimAmount + " " + formatMaterialName(claimMaterial) + ".");
 		}
 		
 		
 	    // Add the other players claims to the file again.
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + File.separator + "lotteryClaim.txt"));
 		    for(int i = 0; i < otherPlayersClaims.size(); i++) {
 		    	out.write(otherPlayersClaims.get(i));
 		    	out.newLine();
 		    }
 		    
 		    out.close();
 		    
 		    
 		} catch (IOException e) {
 		}
 		return true;
 	}
 	
 	public String formatMaterialName(int materialId) {
 		String returnMaterialName = "";
 		String rawMaterialName = Material.getMaterial(materialId).toString();
 		rawMaterialName = rawMaterialName.toLowerCase();
 		// Large first letter.
 		String firstLetterCapital = rawMaterialName.substring(0,1).toUpperCase();
 		rawMaterialName = firstLetterCapital + rawMaterialName.substring(1,rawMaterialName.length());
 		returnMaterialName = rawMaterialName.replace("_", " ");
 		
 		return returnMaterialName;
 	}
 	
     public static org.bukkit.Server getBukkitServer() {
         return Server;
     }
 
     public static iConomy getiConomy() {
         return iConomy;
     }
     
     public static boolean setiConomy(iConomy plugin) {
         if (iConomy == null) {
             iConomy = plugin;
         } else {
             return false;
         }
         return true;
     }
     
     public ArrayList<String> playersInFile(String file) {
     	ArrayList<String> players = new ArrayList<String>();
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + File.separator + file));
 		    String str;
 		    while ((str = in.readLine()) != null) {
 		    	// add players to array.
 		    	players.add(str.toString());
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 		return players;
     }
 }
