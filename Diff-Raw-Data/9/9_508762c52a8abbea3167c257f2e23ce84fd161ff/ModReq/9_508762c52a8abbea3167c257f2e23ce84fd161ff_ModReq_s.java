 package nu.nerd.modreq;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import javax.persistence.PersistenceException;
 
 import nu.nerd.modreq.database.Request;
 import nu.nerd.modreq.database.Request.RequestStatus;
 import nu.nerd.modreq.database.RequestTable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permissible;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.avaje.ebean.PagingList;
 
 public class ModReq extends JavaPlugin {
     ModReqListener listener = new ModReqListener(this);
 
 	RequestTable reqTable;
 	
     @Override
     public void onEnable() {
     	setupDatabase();
     	reqTable = new RequestTable(this);
         getServer().getPluginManager().registerEvents(listener, this);
     }
 
     @Override
     public void onDisable() {
         // tear down
     }
     
     public void setupDatabase() {
 
         try {
             getDatabase().find(Request.class).findRowCount();
         } catch (PersistenceException ex) {
             getLogger().log(Level.INFO, "First run, initializing database.");
             installDDL();
         }
     }
     
     @Override
     public ArrayList<Class<?>> getDatabaseClasses() {
         ArrayList<Class<?>> list = new ArrayList<Class<?>>();
         list.add(Request.class);
         return list;
     }
 
 	@Override
     public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
 		String senderName = ChatColor.stripColor(sender.getName());
 		if (sender instanceof ConsoleCommandSender) {
 			senderName = "Console";
 		}
         if (command.getName().equalsIgnoreCase("modreq")) {
             if (args.length == 0) {
                 return false;
             }
 
             StringBuilder request = new StringBuilder(args[0]);
             for (int i = 1; i < args.length; i++) {
                 request.append(" ").append(args[i]);
             }
             
             if (sender instanceof Player) {
             	Player player = (Player)sender;
 	            Request req = new Request();
 	            req.setPlayerName(senderName);
 	            req.setRequest(request.toString());
 	            req.setRequestTime(System.currentTimeMillis());
 	            String location = String.format("%s,%f,%f,%f", player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
 	            req.setRequestLocation(location);
 	            req.setStatus(RequestStatus.OPEN);
 	            
 	            reqTable.save(req);
 	            
 	            messageMods(ChatColor.GREEN + "New request. Type /check for more");
             }
         }
         else if (command.getName().equalsIgnoreCase("check")) {
         	int page = 1;
         	int requestId = 0;
         	int totalRequests = 0;
         	String limitName = null;
         	
         	if (args.length > 0 && !args[0].startsWith("p:")) {
         		try {
                 	requestId = Integer.parseInt(args[0]);
                 	page = 0;
                 	
                 } catch (NumberFormatException ex) {
                 	requestId = -1;
                 }
         	}
         	
             if (sender.hasPermission("modreq.check")) {
             	
                 if (args.length == 0) {
                     page = 1;
                 }
                 else if (args[0].startsWith("p:")) {
                     try {
                     	page = Integer.parseInt(args[0].substring(3));
                     	
                     } catch (NumberFormatException ex) {
                     	page = -1;
                     }
                 }
             }
             else {
                 limitName = senderName;
             }
             
             List<Request> requests = new ArrayList<Request>();
             
             if (page > 0) {
             	if (limitName != null) {
             		requests.addAll(reqTable.getUserRequests(limitName));
             		totalRequests = requests.size();
             	} else {
             		requests.addAll(reqTable.getRequestPage(page - 1, 5, RequestStatus.OPEN));
             		totalRequests = reqTable.getTotalOpenRequest();
             	}
             } else if (requestId > 0) {
             	Request req = reqTable.getRequest(requestId);
             	totalRequests = 1;
             	if (limitName != null && req.getPlayerName().equalsIgnoreCase(limitName)) {
             		requests.add(req);
             	} else if (limitName == null) {
             		requests.add(req);
             	} else {
             		totalRequests = 0;
             	}
             }
             
             if (totalRequests == 0) {
             	if (limitName != null) {
             		sender.sendMessage(ChatColor.GREEN + "You don't have any outstanding mod requests.");
             	}
             	else {
             		sender.sendMessage(ChatColor.GREEN + "There are currently no open mod requests.");
             	}
             } else if (totalRequests == 1 && requestId > 0) {
             	messageRequestToPlayer(sender, requests.get(0));
             } else if (totalRequests > 0) {
             	messageRequestListToPlayer(sender, requests, page, totalRequests);
             } else {
             	// there was an error.
             }
         }
         else if (command.getName().equalsIgnoreCase("tp-id")) {
             if (args.length == 0) {
                 return false;
             }
             int requestId = 0;
             try {
             	requestId = Integer.parseInt(args[0]);
             	
 	            if (sender instanceof Player) {
 	            	Player player = (Player)sender;
 		            Request req = reqTable.getRequest(requestId);
 		            player.sendMessage(ChatColor.GREEN + "[ModReq] Teleporting you to request " + requestId);
 		            Location loc = stringToLocation(req.getRequestLocation());
 		            player.teleport(loc);
 	            }
             }
             catch (NumberFormatException ex) {
                 sender.sendMessage(ChatColor.RED + "[ModReq] Error: Expected a number for request.");
             }
         }
         else if (command.getName().equalsIgnoreCase("claim")) {
             if (args.length == 0) {
                 return false;
             }
             int requestId = 0;
             try {
             	requestId = Integer.parseInt(args[0]);
             	
 	            if (sender instanceof Player) {
 	            	Player player = (Player)sender;
 		            Request req = reqTable.getRequest(requestId);
 		            
 		            if (req.getStatus() == RequestStatus.OPEN) {
 		            	req.setStatus(RequestStatus.CLAIMED);
 		            	req.setAssignedMod(senderName);
 		            	reqTable.save(req);
 		            	
 		            	messageMods(String.format("%s[ModReq] %s is now handling request #%d", ChatColor.GREEN, senderName, requestId));
 		            }
 	            }
             }
             catch (NumberFormatException ex) {
                 sender.sendMessage(ChatColor.RED + "[ModReq] Error: Expected a number for request.");
             }
         }
         else if (command.getName().equalsIgnoreCase("unclaim")) {
             if (args.length == 0) {
                 return false;
             }
             int requestId = 0;
             
             try {
             	requestId = Integer.parseInt(args[0]);
             	
 	            if (sender instanceof Player) {
 	            	Player player = (Player)sender;
 		            Request req = reqTable.getRequest(requestId);
 		            if (req.getAssignedMod().equalsIgnoreCase(senderName) && req.getStatus() == RequestStatus.CLAIMED) {
 		            	req.setStatus(RequestStatus.OPEN);
 		            	req.setAssignedMod(null);
 		            	reqTable.save(req);
 		            	
 		            	messageMods(String.format("%s[ModReq] %s is no longer handling request #%d", ChatColor.GREEN, senderName, requestId));
 		            }
 	            }
             }
             catch (NumberFormatException ex) {
                 sender.sendMessage(ChatColor.RED + "[ModReq] Error: Expected a number for request.");
             }
         }
         else if (command.getName().equalsIgnoreCase("done")) {
             if (args.length == 0) {
                 return false;
             }
             
             int requestId = 0;
             
             try {
             	requestId = Integer.parseInt(args[0]);
             	
             	String doneMessage = null;
             	
             	if (args.length > 2) {
             		StringBuilder doneMessageBuilder = new StringBuilder(args[1]);
                     for (int i = 2; i < args.length; i++) {
                         doneMessageBuilder.append(" ").append(args[i]);
                     }
                     
                     doneMessage = doneMessageBuilder.toString();
             	}
             	
             	Request req = reqTable.getRequest(requestId);
             	
 		        if (sender.hasPermission("modreq.done")) {
 		        	String msg = "";
 		        	msg = String.format("%s[ModReq] Request #%d has been completed by %s", ChatColor.GREEN, requestId, senderName);
 	        		messageMods(msg);
 	        		
 			        if (doneMessage != null && !doneMessage.isEmpty()) {
 		        		msg = String.format("Close Message - %s%s", ChatColor.GRAY, doneMessage);
 		        		messageMods(msg);
 		        	}
 		        }
 		        else {
 		        	if (!req.getPlayerName().equalsIgnoreCase(senderName)) {
 		        		req = null;
 		        		
 		        		sender.sendMessage(String.format("%s[ModReq] Error, you can only close your own requests.", ChatColor.RED));
 		        	}
 		        }
 		        
 		        if (req != null) {
 		        	req.setStatus(RequestStatus.CLOSED);
 		        	req.setCloseTime(System.currentTimeMillis());
 		            req.setCloseMessage(doneMessage);
 		            req.setAssignedMod(senderName);
 		            
 		            Player requestCreator = getServer().getPlayerExact(req.getPlayerName());
 		            if (requestCreator != null) {
 		            	if (!requestCreator.getName().equalsIgnoreCase(senderName)) {
 		            		String message = "";
 		            		if (doneMessage != null && !doneMessage.isEmpty()) {
 		            			message = String.format("%s completed your request - %s%s", senderName, ChatColor.GRAY, doneMessage);
 		            		} else {
 		            			message = String.format("%s completed your request", senderName);
 		            		}
 		            		requestCreator.sendMessage(ChatColor.GREEN + message);
 		            	}
 		            	else {
		            		messageMods(ChatColor.GREEN + String.format("[ModReq] Request #%d no longer needs to be handled", requestId));
 		            	}
 		            	req.setCloseSeenByUser(true);
 		            }
 		            reqTable.save(req);
 		        }
             }
             catch (NumberFormatException ex) {
                 sender.sendMessage(ChatColor.RED + "[ModReq] Error: Expected a number for request.");
             }
         }
         else if (command.getName().equalsIgnoreCase("reopen")) {
             if (args.length == 0) {
                 return false;
             }
             int requestId = 0;
             
             try {
             	requestId = Integer.parseInt(args[0]);
             	
 	            if (sender instanceof Player) {
 	            	Player player = (Player)sender;
 		            Request req = reqTable.getRequest(requestId);
 		            if ((req.getAssignedMod().equalsIgnoreCase(senderName) && req.getStatus() == RequestStatus.CLAIMED) || req.getStatus() == RequestStatus.CLOSED) {
 		            	req.setStatus(RequestStatus.OPEN);
 		            	req.setAssignedMod(null);
 		            	req.setCloseSeenByUser(false);
 		            	reqTable.save(req);
 		            	
 		            	messageMods(ChatColor.GREEN + String.format("[ModReq] Request #%d is no longer claimed.", requestId));
 		            }
 	            }
             }
             catch (NumberFormatException ex) {
                 sender.sendMessage(ChatColor.RED + "[ModReq] Error: Expected a number for request.");
             }
         } else if (command.getName().equalsIgnoreCase("elevate")) {
         	if (args.length == 0) {
                 return false;
             }
             int requestId = 0;
             
             try {
             	requestId = Integer.parseInt(args[0]);
             	
 	            Request req = reqTable.getRequest(requestId);
 	            if (req.getStatus() == RequestStatus.OPEN) {
 	            	req.setFlagForAdmin(true);
 	            	reqTable.save(req);
 	            }
             }
             catch (NumberFormatException ex) {
                 sender.sendMessage(ChatColor.RED + "[ModReq] Error: Expected a number for request.");
             }
         }
 
         return true;
     }
 	
 	private Location stringToLocation(String requestLocation) {
 		Location loc;
 		double x, y, z;
 		String world;
 		String[] split = requestLocation.split(",");
 		world = split[0];
 		x = Double.parseDouble(split[1]);
 		y = Double.parseDouble(split[2]);
 		z = Double.parseDouble(split[3]);
 		
 		loc = new Location(getServer().getWorld(world), x, y, z);
 		
 		return loc;
 	}
 	
 	private String timestampToDateString(long timestamp) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(timestamp);
 		SimpleDateFormat format = new SimpleDateFormat("MMM.d@k.m.s");
 		return format.format(cal.getTime());
 	}
 
 	private void messageRequestToPlayer(CommandSender sender, Request req) {
 		List<String> messages = new ArrayList<String>();
 		ChatColor onlineStatus = ChatColor.RED;
 		
 		if (getServer().getPlayerExact(req.getPlayerName()) != null) {
 			onlineStatus = ChatColor.GREEN;
 		}
 		Location loc = stringToLocation(req.getRequestLocation());
 		String location = String.format("%s, %d, %d, %d", loc.getWorld().getName(), Math.round(loc.getX()), Math.round(loc.getY()), Math.round(loc.getZ()));
 		
 		messages.add(String.format("%sMod Request #%d - %s%s", ChatColor.AQUA, req.getId(), ChatColor.YELLOW, req.getStatus().toString() ));
 		messages.add(String.format("%sFiled by %s%s%s at %s%s%s at %s%s", ChatColor.YELLOW, onlineStatus, req.getPlayerName(), ChatColor.YELLOW, ChatColor.GREEN, timestampToDateString(req.getRequestTime()), ChatColor.YELLOW, ChatColor.GREEN, location));
 		messages.add(String.format("%s%s", ChatColor.GRAY, req.getRequest()));
 		
 		sender.sendMessage(messages.toArray(new String[1]));
 	}
 	
 	private void messageRequestListToPlayer(CommandSender sender, List<Request> reqs, int page, int totalRequests) {
 		List<String> messages = new ArrayList<String>();
 		
 		messages.add(String.format("%s---- %d Mod Requests ----", ChatColor.AQUA, totalRequests));
 		for (Request r : reqs) {
 			ChatColor onlineStatus = ChatColor.RED;
 			String message = "";
 			if (r.getRequest().length() > 20) {
 				message = r.getRequest().substring(1, 17) + "...";
 			} else {
 				message = r.getRequest();
 			}
 			if (getServer().getPlayerExact(r.getPlayerName()) != null) {
 				onlineStatus = ChatColor.GREEN;
 			}
 			try {
				messages.add(String.format("%s#%d. %s by %s%s%s - %s%s", ChatColor.GOLD, r.getId(), timestampToDateString(r.getRequestTime()), onlineStatus, r.getPlayerName(), ChatColor.GOLD, ChatColor.GRAY, message));
 			}
 			catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 		
 		sender.sendMessage(messages.toArray(new String[1]));
 	}
 	
 	public void messageMods(String message) {
         String permission = "modreq.mod";
         this.getServer().broadcast(message, permission);
 
         Set<Permissible> subs = getServer().getPluginManager().getPermissionSubscriptions(permission);
         for (Player player : getServer().getOnlinePlayers()) {
             if (player.hasPermission(permission) && !subs.contains(player)) {
                 player.sendMessage(message);
             }
         }
     }
 }
