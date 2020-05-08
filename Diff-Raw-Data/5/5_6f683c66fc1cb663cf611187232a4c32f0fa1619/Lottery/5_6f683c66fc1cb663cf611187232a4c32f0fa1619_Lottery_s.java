 package net.erbros.Lottery;
 //All the imports
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
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
 		
 		// Check if we got iConomy support. If not, no need in starting plugin.
 		Server = getServer();
 		PluginListener = new PluginListener();
 
 		// Event Registration
 		getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, PluginListener, Priority.Monitor, this);
 		
 		// Gets version number and writes out starting line to console.
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled" );
 		
 		// Start Registration. Thanks TheYeti.
 		getDataFolder().mkdirs();
 		
 		// Does config exist? If not, make it.
 		
 		makeConfig();
 		
 		String convert = c.getProperty("cost").toString();
 		cost = Integer.parseInt(convert);
 		convert = c.getProperty("hours").toString();
 		hours = Integer.parseInt(convert);
 		
 		// Listen for some player interaction perhaps? Thanks to cyklo :)
 		
 		getCommand("lottery").setExecutor(new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 				
 				// If its just /lottery, and no args.
 				if(args.length == 0) {
 					sender.sendMessage("[LOTTERY] " + timeUntil(nextexec));
 					sender.sendMessage("[LOTTERY] You can buy a ticket for " +  iConomy.getBank().format(cost) + " with /lottery buy");
 				} else {
 					if(args[0].equals("buy")) {
 						Player player = (Player) sender;
 						
 						if(addPlayer(player) == true) {
 							// You got your ticket. 
 							sender.sendMessage("[LOTTERY] You got your lottery ticket for " + iConomy.getBank().format(cost));
 						} else {
 							// You can't buy more than one ticket.
 							sender.sendMessage("[LOTTERY] You already had a ticket. Wait until next round to buy another.");
 						}
 					} else {
 						sender.sendMessage("[LOTTERY] Hoy, I don't recognize that command!");
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
 		StartTimerSchedule();
 		
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
 			StartTimerSchedule();
 		}
 	}
 
 	private void StartTimerSchedule() {
 		
 		long extendtime = 0;
 		//Cancel any existing timers.
 		if(timerStarted == true) {
 			timer.cancel();
 			timer.purge();
			extendtime = nextexec - System.currentTimeMillis();
 		} else {
 			// Get time until lottery drawing.
			extendtime = extendTime();
 		}
 		// If the time is passed (perhaps the server was offline?), draw lottery at once.
 		
 		if(extendtime <= 0) {
 			extendtime = 3000;
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
 		    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + "\\lotteryPlayers.txt"));
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
 		
 		try {
 		    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + "\\lotteryPlayers.txt",true));
 		    out.write(player.getName());
 		    out.newLine();
 		    out.close();
 		    
 		    // Removing coins from players account.
 		    Account account = iConomy.getBank().getAccount(player.getName());
 		    account.subtract(cost);
 		    
 		} catch (IOException e) {
 		}
 
 		return true;
 	}
 	
 	public void makeConfig() {
 		c = getConfiguration();
 	
 		if(c.getProperty("cost") == null) {
 			
 			c.setProperty("cost", "5");
 			c.setProperty("hours", "24");
 			
 		    if (!getConfiguration().save())
 		    {
 		        log.warning("Unable to persist configuration files, changes will not be saved.");
 		    }
 		}
 		
 	}
 	
 	public String timeUntil(long time) {
 
 		double timeLeft = Double.parseDouble(Long.toString(((time - System.currentTimeMillis()) / 1000)));
 		// How many days left?
 		String stringTimeLeft = "Pulling winner in";
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
 			if(stringTimeLeft.equalsIgnoreCase("Pulling winner in") == false) {
 				stringTimeLeft = stringTimeLeft.substring(0, stringTimeLeft.length()-1);
 			}
 		}
 		int secs = (int) timeLeft;
 		if(stringTimeLeft.equalsIgnoreCase("Pulling winner in") == false) {
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
 		ArrayList<String> players = new ArrayList<String>();
 		try {
 		    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + "\\lotteryPlayers.txt"));
 		    String str;
 		    while ((str = in.readLine()) != null) {
 		    	// add players to array.
 		    	players.add(str.toString());
 		    }
 		    in.close();
 		} catch (IOException e) {
 		}
 		if(players.isEmpty() == true) {
 			Server.broadcastMessage("[LOTTERY] No tickets sold this round. Thats a shame.");
 			return false;
 		} else {
 			int rand = new Random().nextInt(players.size());
 			int amount = players.size()*cost;
 			Account account = iConomy.getBank().getAccount(players.get(rand));
 			account.add(amount);
 			// Announce the winner:
 			Server.broadcastMessage("[LOTTERY] Congratulations to " + players.get(rand) + " for winning " + iConomy.getBank().format(amount));
 			Server.broadcastMessage("[LOTTERY] There was in total " + players.size() + " players with a lottery ticket.");
 			
 			// Clear file.
 			try {
 			    BufferedWriter out = new BufferedWriter(new FileWriter(getDataFolder() + "\\lotteryPlayers.txt",false));
 			    out.write("");
 			    out.close();
 			    
 			} catch (IOException e) {
 			}
 		}
 		return true;
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
 
 	
 }
