 package com.martinbrook.tesseractuhc;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import org.bukkit.ChatColor;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.martinbrook.tesseractuhc.listeners.ChatListener;
 import com.martinbrook.tesseractuhc.listeners.LoginListener;
 import com.martinbrook.tesseractuhc.listeners.MatchListener;
 import com.martinbrook.tesseractuhc.listeners.SpectateListener;
 import com.martinbrook.tesseractuhc.notification.PlayerMessageNotification;
 import com.martinbrook.tesseractuhc.startpoint.UhcStartPoint;
 import com.martinbrook.tesseractuhc.util.MatchUtils;
 
 public class TesseractUHC extends JavaPlugin {
 	private static TesseractUHC instance = null;
 	public static final ChatColor MAIN_COLOR = ChatColor.GREEN, SIDE_COLOR = ChatColor.GOLD, OK_COLOR = ChatColor.GREEN, WARN_COLOR = ChatColor.LIGHT_PURPLE, ERROR_COLOR = ChatColor.RED,
 			DECISION_COLOR = ChatColor.GOLD, ALERT_COLOR = ChatColor.GREEN;
 	private static final boolean DEBUG_BUILD = false;
 	
 	private UhcMatch match;
 	
 	/**
 	 * Get the singleton instance of UhcTools
 	 * 
 	 * @return The plugin instance
 	 */
 	public static TesseractUHC getInstance() {
 		return instance;
 	}
 	
 	public void onEnable() {
 		
 		// Store singleton instance
 		instance = this;
 
 		saveDefaultConfig();
 		match = new UhcMatch(this, getServer().getWorlds().get(0), getConfig());
 	
 		getServer().getPluginManager().registerEvents(new ChatListener(match), this);
 		getServer().getPluginManager().registerEvents(new LoginListener(match), this);
 		getServer().getPluginManager().registerEvents(new MatchListener(match), this);
 		getServer().getPluginManager().registerEvents(new SpectateListener(match), this);
 		
 		
 		
 	}
 	
 	public void onDisable(){
 		match.saveMatchParameters();
 		this.match = null;
 		getServer().getScheduler().cancelTasks(this);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
 		boolean success = false;
 		String cmd = command.getName().toLowerCase();
 		
 		if (commandSender instanceof Player) {
 			UhcPlayer pl = match.getPlayer((Player) commandSender);
 			if (pl.isAdmin()) {
 				success = runCommandAsAdmin(pl, cmd, args)
 						|| runCommandAsSpectator(pl.getSpectator(), cmd, args)
 						|| runCommandAsConsole(commandSender, cmd, args)
 						|| runCommandAsPlayer(pl, cmd, args);
 
 			} else if (pl.isSpectator()) {
 				success = runCommandAsSpectator(pl.getSpectator(), cmd, args)
 						|| runCommandAsPlayer(pl, cmd, args);
 
 			} else {
 				success = runCommandAsPlayer(pl, cmd, args);
 			}
 		} else if (commandSender instanceof ConsoleCommandSender) {
 			success = runCommandAsConsole(commandSender, cmd, args);
 		}
 		
 		return success;
 	}
 
 	/**
 	 * Run a command sent from the console
 	 * 
 	 * @param sender The sender of the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
 	private boolean runCommandAsConsole(CommandSender sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 	
 		if(cmd.equals("tp")) {
 			response = sTp(args);
 		} else if(cmd.equals("butcher")) {
 			response = cButcher();
 		} else if (cmd.equals("heal")) {
 			response = cHeal(args);
 		} else if (cmd.equals("healall")) {
 			response = cHealall();
 		} else if (cmd.equals("feed")) {
 			response = cFeed(args);
 		} else if (cmd.equals("feedall")) {
 			response = cFeedall();
 		} else if (cmd.equals("clearinv")) {
 			response = cClearinv(args);
 		} else if (cmd.equals("clearinvall")) {
 			response = cClearinvall();
 		} else if (cmd.equals("renew")) {
 			response = cRenew(args);
 		} else if (cmd.equals("renewall")) {
 			response = cRenewall();
 		} else if (cmd.equals("ready")) {
 			response = cReady(args);
 		} else if (cmd.equals("cdwb")) {
 			response = cCdwb(args);
 		} else if (cmd.equals("cdc")) {
 			response = cCdc();
 		} else if (cmd.equals("chatscript")) {
 			response = cChatscript(args);
 		} else if (cmd.equals("muteall")) {
 			response = cMuteall(args);
 		} else if (cmd.equals("permaday")) {
 			response = cPermaday(args);
 		} else if (cmd.equals("uhc")) {
 			response = cUhc(null, args);
 		} else if (cmd.equals("launch")) {
 			response = cLaunch();
 		} else if (cmd.equals("relaunch")) {
 			response = cRelaunch(args);
 		} else if (cmd.equals("calcstarts")) {
 			response = cCalcstarts(args);
 		} else if (cmd.equals("setvanish")) {
 			response = cSetvanish();
 		} else if (cmd.equals("players")) {
 			response = cPlayers(args);
 		} else if (cmd.equals("teams")) {
 			response = cTeams(args);
 		} else if (cmd.equals("matchinfo") || cmd.equals("mi") || cmd.equals("match")) {
 			response = pMatchinfo();
 		} else {
 			success = false;
 		}
 	
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 
 	/**
 	 * Execute a command sent by an opped player on the server
 	 * 
 	 * @param sender The Player who sent the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
 	private boolean runCommandAsAdmin(UhcPlayer sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 	
 
 		if (cmd.equals("setspawn")) {
 			response = cSetspawn(sender);
 		} else if (cmd.equals("uhc")) {
 			response = cUhc(sender, args);
 		} else if (cmd.equals("interact")) {
 			response = cInteract(sender);
 		} else {
 			success = false;
 		}
 		
 		if (response != null)
 			sender.sendMessage(response);
 	
 		return success;
 		
 	}
 
 	/**
 	 * Run a command sent by a spectator
 	 * 
 	 * @param sender the Player who sent the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
 	private boolean runCommandAsSpectator(UhcSpectator sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 	
 		if(cmd.equals("tp")) {
 			response = cTp(sender,args);
 		} else if (cmd.equals("tpd")) {
 			response = cTpd(sender);
 		} else if (cmd.equals("tpl")) {
 			response = cTpl(sender);
 		} else if (cmd.equals("tpn")) {
 			response = cTpn(sender);
 		} else if(cmd.equals("tp0")) {
 			response = cTp0(sender);
 		} else if(cmd.equals("tps")) {
 			response = cTps(sender, args);
 		} else if (cmd.equals("tpcs")) {
 			response = cTpcs(sender,args);
 		} else if (cmd.equals("tpp")) {
 			response = cTpp(sender,args);
 		} else if (cmd.equals("tpnext")) {
 			response = cTpnext(sender);
 		} else if (cmd.equals("tpback")) {
 			response = cTpback(sender);
 		} else if (cmd.equals("gm")) {
 			response = cGm(sender);
 		} else if (cmd.equals("vi") || cmd.equals("pi")) {
 			response = cVi(sender, args);
 		} else {
 			success = false;
 		}
 	
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 	
 	/**
 	 * Run a command sent by a player
 	 * 
 	 * @param sender the Player who sent the command
 	 * @param cmd The command
 	 * @param args Command arguments
 	 * @return Whether the command succeeded
 	 */
 	private boolean runCommandAsPlayer(UhcPlayer sender, String cmd, String[] args) {
 		boolean success = true;
 		String response = null; // Stores any response to be given to the sender
 	
 		if (cmd.equals("kill")) {
 			response = ERROR_COLOR + "The kill command is disabled.";
 		} else if (cmd.equals("notify") || cmd.equals("n")) {
 			response = cNotify(sender, args);
 		} else if (cmd.equals("join")) {
 			response = pJoin(sender, args);
 		} else if (cmd.equals("team")) {
 			response = pTeam(sender, args);
 		} else if (cmd.equals("leave")) {
 			response = pLeave(sender, args);
 		} else if (cmd.equals("matchinfo") || cmd.equals("mi") || cmd.equals("match")) {
 			response = pMatchinfo();
 		} else if (cmd.equals("params")) {
 			response = cParams();
 		} else {
 			success = false;
 		}
 	
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return success;
 	}
 
 	private String cGm(UhcSpectator sender) {
 		sender.toggleGameMode();
 		return null;
 	}
 
 	private String cTpnext(UhcSpectator sender) {
 		Player to;
 		int cycleSize = match.countParticipantsInMatch();
 		int attempts = 0;
 		
 		do {
 			UhcParticipant up = match.getUhcParticipant(sender.nextCyclePoint(cycleSize));
 			to = getServer().getPlayerExact(up.getName());
 			attempts++;
 		} while ((to == null || !to.isOnline()) && attempts < cycleSize);
 		
 		if (to == null || !to.isOnline())
 			return ERROR_COLOR + "No player found";
 		
 		sender.teleport(to, OK_COLOR + "Teleported to " + to.getName());
 			
 		return null;
 	}
 
 	private String cTpback(UhcSpectator sender) {
 		sender.tpBack();
 		return null;
 	}
 	
 	private String cInteract(UhcPlayer sender) {
 		UhcSpectator us = sender.getSpectator();
 		if (us == null) return ERROR_COLOR + "You are not allowed to use that command";
 
 		boolean interacting = !us.isInteracting();
 		us.setInteracting(interacting);
 		
 		return OK_COLOR + "Interaction has been " + (interacting ? "enabled" : "disabled") + ".";
 	}
 
 	private String cTeams(String[] args) {
 		// teams - lists all teams
 		// teams add identifier name name name - adds a team
 		if (match.isFFA())
 			return ERROR_COLOR + "This is not a team match. Use /players to list players";
 		
 		if (args.length < 1) {
 			Collection<UhcTeam> allTeams = match.getTeams();
 			String response = ChatColor.GOLD + "" + allTeams.size() + " teams (" + match.countTeamsInMatch() + " still alive):\n";
 			
 			for (UhcTeam team : allTeams) {
 				response += (team.aliveCount()==0 ? ChatColor.RED + "[D] " : ChatColor.GREEN) + "" +
 						ChatColor.ITALIC + team.getName() + ChatColor.GRAY +
 						" [" + team.getIdentifier() + "]\n";
 			}
 			return response;
 		}
 		if (args[0].equalsIgnoreCase("add")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify team to add!";
 			
 			String identifier = args[1];
 			String name = "";
 			
 			if (args.length < 3)
 				name = identifier;
 			else {
 				for (int i = 2; i < args.length; i++) name += args[i] + " ";
 				name = name.substring(0,name.length()-1);
 			}
 				
 			if (!match.addTeam(identifier, name))
 				return ERROR_COLOR + "Could not add team";
 			
 			return OK_COLOR + "Team created";
 			
 		} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify team to remove!";
 			if (!match.removeTeam(args[1]))
 				return ERROR_COLOR + "Team could not be removed!";
 
 			return OK_COLOR + "Team removed";
 		}
 
 		
 		
 		return null;
 	}
 
 	private String cPlayers(String[] args) {
 		// players - lists all players
 		// players add playername
 		// players add playername teamidentifier
 		// players addall
 
 		if (args.length < 1) {
 
 			return match.getPlayerStatusReport();
 			
 		}
 		if (args[0].equalsIgnoreCase("add")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify player to add!";
 			
 			String response = "";
 			
 			UhcPlayer pl = match.getPlayer(args[1]);
 			
 			if (!pl.isOnline())
 				response += WARN_COLOR + "Warning: adding a player who is not currently online\n";
 
 			
 			
 			if (match.isFFA()) {
 				if (!match.addSoloParticipant(pl))
 					return ERROR_COLOR + "Failed to add player";
 				
 				return response + OK_COLOR + "Player added";
 			} else {
 				if (args.length < 3)
 					return ERROR_COLOR + "Please specify the team! /players add NAME TEAM";
 				if (!match.addParticipant(pl, args[2]))
 					return ERROR_COLOR + "Failed to add player " + args[1] + " to team " + args[2];
 				
 				return response + OK_COLOR + "Player added";
 			}
 				
 		} else if (args[0].equalsIgnoreCase("addall")) {
 			if (!match.isFFA())
 				return ERROR_COLOR + "Cannot auto-add players in a team match";
 			
 			int added = 0;
 			for (UhcPlayer pl : match.getOnlinePlayers()) {
 				if (!pl.isSpectator())
 					if (match.addSoloParticipant(pl)) added++;
 			}
 			if (added > 0)
 				return "" + OK_COLOR + added + " player" + (added == 1? "" : "s") + " added";
 			else
 				return ERROR_COLOR + "No players to add!";
 			
 			
 		} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) {
 			if (args.length < 2)
 				return ERROR_COLOR + "Specify player to remove!";
 			if (!match.removeParticipant(args[1]))
 				return ERROR_COLOR + "Player could not be removed!";
 
 			return OK_COLOR + "Player removed";
 		}
 		return null;
 	}
 
 	private String pLeave(UhcPlayer sender, String[] args) {
 		if (!match.removeParticipant(sender.getName()))
 			return ERROR_COLOR + "Leave failed";
 		else
 			return OK_COLOR + "You have left the match";
 	}
 
 	private String pTeam(UhcPlayer sender, String[] args) {
 		if (sender.isParticipant())
 			return ERROR_COLOR + "You have already joined this match. Please /leave before creating a new team.";
 		
 		if (match.getMatchPhase() != MatchPhase.PRE_MATCH)
 			return ERROR_COLOR + "The match is already underway. You cannot create a team.";
 		
 		if (match.isFFA())
 			return ERROR_COLOR + "This is a FFA match. There are no teams.";
 		
 		if (!match.roomForAnotherTeam())
 			return ERROR_COLOR + "There are no more team slots left.";
 		
 		if (args.length < 1)
 			return ERROR_COLOR + "Syntax: /team identifier [full name]";
 		
 		String identifier = args[0].toLowerCase();
 		String name = "";
 		
 		if (args.length < 2)
 			name = identifier;
 		else {
 			for (int i = 1; i < args.length; i++) name += args[i] + " ";
 			name = name.substring(0,name.length()-1);
 		}
 			
 		if (!match.addTeam(identifier, name))
 			return ERROR_COLOR + "Could not add a new team. Use /join to join an existing team.";
 		
 		if (!match.addParticipant(sender, identifier))
 			return ERROR_COLOR + "An error occurred. The team has been created but you could not be joined to it.";
 		
 		match.broadcast(ChatColor.GOLD + "Team " + ChatColor.AQUA + ChatColor.ITALIC + name + ChatColor.RESET 
 				+ ChatColor.GOLD + " was created by " + sender.getDisplayName() + ".\n"
 				+ "To join, type " + ChatColor.AQUA + ChatColor.ITALIC + "/join " + identifier);
 		
 		return OK_COLOR + "You have created the team: " + name;
 		
 	}
 
 	private String pJoin(UhcPlayer sender, String[] args) {
 		if (match.getMatchPhase() != MatchPhase.PRE_MATCH)
 			return ERROR_COLOR + "The match is already underway. You cannot join.";
 		
 		if (match.isFFA()) {
 			if (match.addSoloParticipant(sender)) {
 				return OK_COLOR + "You have joined the match";
 			} else {
 				return ERROR_COLOR + "Unable to join";
 			}
 		}
 		
 		UhcTeam teamToJoin = null;
 				
 		if(args.length > 0) teamToJoin = match.getTeam(args[0]);
 			
 		if (teamToJoin == null) {
 			String response = ERROR_COLOR + "Please specify a team to join. Available teams:\n";
 			for (UhcTeam t : match.getTeams()) {
 				response += t.getIdentifier() + ": " + t.getName() + "\n";
 			}
 			return response;
 		}
 		
 		if (match.addParticipant(sender, teamToJoin.getIdentifier()))
 			return OK_COLOR + "You are now a member of " + teamToJoin.getName();
 		else
 			return ERROR_COLOR + "Unable to join team. Are you already on a team?";
 	}
 
 	private String cSetvanish() {
 		match.setVanish();
 		return OK_COLOR + "Visibility of all players has been updated";
 	}
 
 	/**
 	 * Carry out the /vi or /pi command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cVi(UhcSpectator sender, String[] args) {
 		if (args.length < 1)
 			return ERROR_COLOR + "Please give the name of a player";
 		
 		Player p = getServer().getPlayer(args[0]);
 		
 		if (p == null)
 			return ERROR_COLOR + "Player " + args[0] + " not found.";
 
 		if (!sender.showInventory(p))
 			return ERROR_COLOR + "Unable to view inventory";
 		
 		return null;
 
 	}
 
 
 	private String cTpp(UhcSpectator sender, String[] args) {
 		ArrayList<UhcPOI> pois = match.getPOIs();
 		if (args.length != 1)
 			return ERROR_COLOR + "Please give the POI number - see /uhc pois";
 		
 		try {
 			int poiNo = Integer.parseInt(args[0]);
 			sender.teleport(pois.get(poiNo - 1).getLocation());
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "Please give the POI number - see /uhc pois";
 		} catch (IndexOutOfBoundsException e) {
 			return ERROR_COLOR + "That POI does not exist";
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpcs command
 	 * 
 	 * @param sender player who sent the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cTpcs(UhcSpectator sender, String[] args) {
 		ArrayList<Location> starts = match.getCalculatedStarts();
 		if (starts == null)
 			return ERROR_COLOR + "Start points have not been calculated";
 		
 		if (args.length != 1)
 			return ERROR_COLOR + "Please give the start number";
 		
 		
 		try {
 			int startNumber = Integer.parseInt(args[0]);
 			sender.teleport(starts.get(startNumber - 1));
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "Please give the start number";
 		} catch (IndexOutOfBoundsException e) {
 			return ERROR_COLOR + "That start does not exist";
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Carry out the /calcstarts
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cCalcstarts(String[] args) {
 		ArrayList<Location> starts = MatchUtils.calculateStarts(args);
 		if (starts == null) return ERROR_COLOR + "No start locations were calculated";
 		
 		String response = OK_COLOR + "" + starts.size() + " start locations calculated: \n";
 		for(int i = 0 ; i < starts.size() ; i++) {
 			Location l = starts.get(i);
 			response += (i+1) + ": x=" + l.getX() + " z=" + l.getZ() + "\n";
 		}
 		match.setCalculatedStarts(starts);
 		return response;
 		
 		
 	}
 
 
 
 	/**
 	 * Carry out the /setspawn command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cSetspawn(UhcPlayer sender) {
 		Location newSpawn = sender.getLocation();
 		newSpawn.getWorld().setSpawnLocation(newSpawn.getBlockX(), newSpawn.getBlockY(), newSpawn.getBlockZ());
 		return OK_COLOR + "This world's spawn point has been set to " + newSpawn.getBlockX() + "," + newSpawn.getBlockY() + "," + newSpawn.getBlockZ();
 	}
 
 
 	
 	/**
 	 * Carry out the /cdc command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cCdc() {
 		if (match.cancelMatchCountdown())
 			return OK_COLOR + "Match countdown cancelled!";
 		
 		if (match.cancelBorderCountdown())
 			return OK_COLOR + "Border countdown cancelled!";
 
 		return ERROR_COLOR + "No countdown is in progress";
 		
 	}
 	
 	/**
 	 * Carry out the /muteall command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cMuteall(String[] args) {
 		if (args.length < 1)
 			return ERROR_COLOR +"Please specify 'on' or 'off'";
 
 		if (args[0].equalsIgnoreCase("on")) {
 			match.setChatMuted(true);
 			return OK_COLOR + "Chat muted!";
 		}
 		if (args[0].equalsIgnoreCase("off")) {
 			match.setChatMuted(false);
 			return OK_COLOR + "Chat unmuted!";
 		}
 		
 		return ERROR_COLOR + "Please specify 'on' or 'off'";
 
 	}
 	
 	/**
 	 * Carry out the /chatscript command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cChatscript(String[] args) {
 		String scriptFile;
 		if (args.length < 1)
 			scriptFile = "announcement.txt"; 
 		else
 			scriptFile = args[0];
 		match.playChatScript(scriptFile, true);
 		return OK_COLOR + "Starting chat script";
 	}
 
 	
 	/**
 	 * Carry out the /permaday command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cPermaday(String[] args) {
 		if (args.length < 1)
 			return OK_COLOR + "Permaday is " + (match.getPermaday() ? "on" : "off");
 		
 		if (args[0].equalsIgnoreCase("off") || args[0].equals("0")) {
 			match.setPermaday(false);
 		} else if (args[0].equalsIgnoreCase("on") || args[0].equals("1")) {
 			match.setPermaday(true);
 		} else {
 			return ERROR_COLOR + "Argument '" + args[0] + "' not understood";
 		}
 		return null;
 
 	}
 
 	/**
 	 * Carry out the /uhc command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cUhc(UhcPlayer sender, String[] args) {
 		if (args.length<1)
 			return ERROR_COLOR + "Please specify an action.";
 		
 		if ("startpoint".equalsIgnoreCase(args[0]) || "sp".equalsIgnoreCase(args[0])) {
 			if (sender != null) return cUhcStartpoint(sender, args);
 			else return ERROR_COLOR + "That command is not available from the console";
 		} else if ("poi".equalsIgnoreCase(args[0])) {
 			if (sender != null) return cUhcPoi(sender, args);
 			else return ERROR_COLOR + "That command is not available from the console";
 		} else if ("setbonus".equalsIgnoreCase(args[0])) {
 			if (sender != null) return cUhcSetbonus(sender);
 			else return ERROR_COLOR + "That command is not available from the console";
 		} else if ("getbonus".equalsIgnoreCase(args[0])) {
 			if (sender != null) return cUhcGetbonus(sender);
 			else return ERROR_COLOR + "That command is not available from the console";
 		} else if ("reset".equalsIgnoreCase(args[0])) {
 			return cUhcReset(args);
 		} else if ("save".equalsIgnoreCase(args[0])) {
 			return cUhcSave(args);
 		} else if ("starts".equalsIgnoreCase(args[0])) {
 			return cUhcStarts();
 		} else if ("pois".equalsIgnoreCase(args[0])) {
 			return cUhcPois();
 		} else if ("params".equalsIgnoreCase(args[0])) {
 			return cUhcParams();
 		} else if ("set".equalsIgnoreCase(args[0])) {
 			return cUhcSet(args);
 		} 
 		
 		return ERROR_COLOR + "Command not understood";
 	}
 
 
 	/**
 	 * @param sender
 	 * @return
 	 */
 	private String cUhcGetbonus(UhcPlayer sender) {
 		((Player) sender).getEnderChest().setContents(match.getBonusChest());
 		return OK_COLOR + "Bonus chest loaded into your ender chest";
 	}
 
 	/**
 	 * @param sender
 	 * @return
 	 */
 	private String cUhcSetbonus(UhcPlayer sender) {
 		match.setBonusChest(((Player) sender).getEnderChest().getContents());
 		return OK_COLOR + "Bonus chest saved from your ender chest";
 	}
 
 
 	/**
 	 * @param args
 	 * @return
 	 */
 	private String cUhcSet(String[] args) {
 		if (args.length < 3)
 			return ERROR_COLOR + "Invalid command";
 
 		String parameter = args[1].toLowerCase();
 
 		String value = "";
 		for (int i = 2; i < args.length; i++) {
 			value += args[i] + " ";
 		}
 		value = value.substring(0, value.length()-1);
 		if (this.setMatchParameter(parameter, value)) {
 			return formatMatchParameter(parameter);
 		} else
 			return ERROR_COLOR + "Unable to set value of " + parameter;
 	}
 
 	/**
 	 * @return
 	 */
 	private String cUhcPois() {
 		ArrayList<UhcPOI> pois = match.getPOIs();
 		String response = "";
 		for(int i = 0; i < pois.size(); i++) {
 			response += (i + 1) + ": " + pois.get(i).getName() + " (" + pois.get(i).toString() + ")\n"; 
 		}
 		return response;
 	}
 
 	/**
 	 * @return
 	 */
 	private String cUhcStarts() {
 		HashMap<Integer, UhcStartPoint> startPoints = match.getStartPoints();
 		if (startPoints.size()==0)
 			return ERROR_COLOR + "There are no starts";
 
 		String response = "";
 		for (UhcStartPoint sp : startPoints.values()) {
 			UhcTeam t = sp.getTeam();
 			
 			response += (sp.getNumber());
 			
 			if (t != null) response += " (" + t.getName() + ")";
 			
 			response += ": " + sp.getX() + "," + sp.getY() + "," + sp.getZ() + "\n";
 		}
 		return response;
 	}
 
 	/**
 	 * @param args
 	 * @return
 	 */
 	private String cUhcSave(String[] args) {
 		if (args.length < 2 || "params".equalsIgnoreCase(args[1])) {
 			match.saveMatchParameters();
 			return OK_COLOR + "If no errors appear above, match parameters have been saved";
 		} else if ("teams".equalsIgnoreCase(args[1]) || "players".equalsIgnoreCase(args[1])) {
 			match.saveTeams();
 			return OK_COLOR + "If no errors appear above, teams and players have been saved";
 		}
 		return ERROR_COLOR + "Argument not understood. Please use " + SIDE_COLOR + "/uhc save params" 
 		+ ERROR_COLOR + " or " + SIDE_COLOR + "/uhc save teams";
 	}
 
 	/**
 	 * @param args
 	 */
 	private String cUhcReset(String[] args) {
 		if (args.length < 2 || "params".equalsIgnoreCase(args[1])) {
 			match.resetMatchParameters();
 			return OK_COLOR + "Match data reset to default values";
 		} else if ("teams".equalsIgnoreCase(args[1]) || "players".equalsIgnoreCase(args[1])) {
 			if (!match.clearTeams())
 				return ERROR_COLOR + "Failed to clear teams and players";
 			else
 				return OK_COLOR + "Teams and players have been reset";
 		}
 		return ERROR_COLOR + "Argument not understood. Please use " + SIDE_COLOR + "/uhc reset params" 
 				+ ERROR_COLOR + " or " + SIDE_COLOR + "/uhc reset teams";
 	}
 
 	/**
 	 * @param sender
 	 * @param args
 	 * @return
 	 */
 	private String cUhcPoi(UhcPlayer sender, String[] args) {
 		if (args.length<2) return ERROR_COLOR + "Please give a description/name";
 		String name = "";
 		for(int i = 1; i < args.length; i++) name += args[i] + " ";
 		name = name.substring(0, name.length()-1);
 		match.addPOI(((Player) sender).getLocation(), name);
 		return OK_COLOR + "POI added at your current location";
 	}
 
 	/**
 	 * @param sender
 	 * @param args
 	 * @return
 	 */
 	private String cUhcStartpoint(UhcPlayer sender, String[] args) {
 		Location l = sender.getLocation();
 		double x = l.getBlockX() + 0.5;
 		double y = l.getBlockY();
 		double z = l.getBlockZ() + 0.5;
 		
 		UhcStartPoint startPoint = match.addStartPoint(x, y, z, args.length < 2 || !("-n".equalsIgnoreCase(args[1])));
 		
 		return OK_COLOR + "Start point " + startPoint.getNumber() + " added at your current location";
 	}
 	
 	
 	private String cUhcParams() {
 		String response = ChatColor.GOLD + "Match details:\n";
 		response += ChatColor.RESET + "[uhc]           " + formatMatchParameter("uhc") + "\n";
 		response += ChatColor.RESET + "[ffa]           " + formatMatchParameter("ffa") + "\n";
 		response += ChatColor.RESET + "[nopvp]         " + formatMatchParameter("nopvp") + "\n";
 		response += ChatColor.RESET + "[killerbonus]   " + formatMatchParameter("killerbonus") + "\n";
 		response += ChatColor.RESET + "[miningfatigue] " + formatMatchParameter("miningfatigue") + "\n";
 		response += ChatColor.RESET + "[deathban]      " + formatMatchParameter("deathban") + "\n";
 		response += ChatColor.RESET + "[autospectate]  " + formatMatchParameter("autospectate") + "\n";
 		response += ChatColor.RESET + "[nolatecomers]  " + formatMatchParameter("nolatecomers") + "\n";
 		
 		return response;
 	}
 	
 	private String cParams() {
 		String response = ChatColor.GOLD + "Match details:\n";
 		response += "   " + formatMatchParameter("uhc") + "\n";
 		response += "   " + formatMatchParameter("ffa") + "\n";
 		response += "   " + formatMatchParameter("nopvp") + "\n";
 		response += "   " + formatMatchParameter("killerbonus") + "\n";
 		response += "   " + formatMatchParameter("miningfatigue") + "\n";
 		
 		return response;
 	}
 
 	/**
 	 * Return a human-friendly representation of a specified match parameter
 	 * 
 	 * @param parameter The match parameter to look up
 	 * @return A human-readable version of the parameter's value
 	 */
 	private String formatMatchParameter(String parameter) {
 		String response = ERROR_COLOR + "Unknown parameter";
 		String param = ChatColor.AQUA.toString();
 		String value = ChatColor.GOLD.toString();
 		String desc = "\n" + ChatColor.GRAY + ChatColor.ITALIC + "      ";
 		
 		
 		if ("deathban".equalsIgnoreCase(parameter)) {
 			response = param + "Deathban: " + value;
 			if (match.getDeathban())
 				response += "Enabled" + desc + "Dead players will be prevented from logging back into the server";
 			else 
 				response += "Disabled" + desc + "Dead players will be allowed to stay on the server";
 			
 		} else if ("killerbonus".equalsIgnoreCase(parameter)) {
 			response = param + "Killer bonus: " + value;
 			ItemStack kb = match.getKillerBonus();
 			if (kb == null) 
 				response += "Disabled" + desc + "No additional bonus dropped after a PvP kill";
 			else
 				response += kb.getAmount() + " x " + kb.getType().toString() + desc 
 				+ "Additional items dropped when a player is killed by PvP";
 				
 		} else if ("miningfatigue".equalsIgnoreCase(parameter)) {
 			response = param + "Mining hunger: " + value;
 			double mfg = match.getMiningFatigueGold();
 			double mfd = match.getMiningFatigueDiamond();
 			if (mfg > 0 || mfd > 0)
 				response += (mfg>0 ? (mfg / 8.0) + " (below y=32) " : "") + (mfd > 0 ? (mfd / 8.0) + " (below y=16)" : "" ) + desc;
 			else
 				response += "Disabled" + desc;
 			
 			response += "Hunger penalty per block mined at those depths (stone\n      blocks only)";
 					
 		} else if ("nopvp".equalsIgnoreCase(parameter)) {
 			response = param + "No-PvP period: " + value;
 			int n = match.getNopvp();
 			int mins = n / 60;
 			int secs = n % 60;
 			if (n > 0)
 				response += (mins > 0 ? mins + " minutes" : "")
 						+ (secs > 0 ? secs + " seconds" : "")
 						+ desc +  "Period at the start of the match during which PvP is\n      disabled";
 			else 
 				response += "None" + desc + "PvP will be enabled from the start";
 			
 			
 		} else if ("ffa".equalsIgnoreCase(parameter)) {
 			response = param + "Teams: " + value;
 			if (match.isFFA())
 				response += "Free for all" + desc + "No teams, no alliances, every player for themselves";
 			else
 				response += "Teams" + desc + "Teams work together, last team with a survivor wins";
 			
 		} else if ("uhc".equalsIgnoreCase(parameter)) {
 			response = param + "UHC: " + value;
 			if (match.isUHC())
 				response += "Enabled" + desc + "No health regeneration, and modified recipes for golden\n      apple and glistering melon";
 			else
 				response += "Disabled" + desc + "Health regeneration and crafting recipes are unchanged";
 		} else if ("autospectate".equalsIgnoreCase(parameter)) {
 			response = param + "AutoSpectate: " + value;
			if (match.isUHC())
 				response += "Enabled" + desc + "Dead players will become invisible spectators";
 			else
 				response += "Disabled" + desc + "Dead players will not be able to spectate";
 		} else if ("nolatecomers".equalsIgnoreCase(parameter)) {
 			response = param + "NoLatecomers: " + value;
			if (match.isUHC())
 				response += "Enabled" + desc + "Late arriving players will not be able to connect";
 			else
 				response += "Disabled" + desc + "Late arriving players will be able to join";
 		}
 		
 		
 		return response;
 	}
 
 	private boolean setMatchParameter(String parameter, String value) {
 		// Look up the parameter.
 		
 		if ("deathban".equalsIgnoreCase(parameter)) {
 
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setDeathban(v);
 			return true;
 			
 		} else if ("killerbonus".equalsIgnoreCase(parameter)) {
 			Boolean b = MatchUtils.stringToBoolean(value);
 			if (b != null && !b) {
 				match.setKillerBonus(0);
 				return true;
 			}
 			String[] split = value.split(" ");
 			if (split.length > 2)
 				return false;
 			
 			int quantity = 1;
 			
 			try {
 				int id = Integer.parseInt(split[0]);
 				if (split.length > 1)
 					quantity = Integer.parseInt(split[1]);
 				
 				match.setKillerBonus(id, quantity);
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			
 		
 			
 		} else if ("miningfatigue".equalsIgnoreCase(parameter)) {
 			Boolean b = MatchUtils.stringToBoolean(value);
 			if (b != null && !b) {
 				match.setMiningFatigue(0,0);
 				return true;
 			}
 			
 			String[] split = value.split(" ");
 			if (split.length != 2)
 				return false;
 			
 			try {
 				double gold = Double.parseDouble(split[0]);
 				double diamond = Double.parseDouble(split[1]);
 				match.setMiningFatigue(gold, diamond);
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 			
 			
 		} else if ("nopvp".equalsIgnoreCase(parameter)) {
 			try {
 				match.setNopvp(Integer.parseInt(value));
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 		} else if ("ffa".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setFFA(v);
 			return true;
 		} else if ("uhc".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setUHC(v);
 			return true;
 		} else if ("autospectate".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setAutoSpectate(v);
 			return true;
 		} else if ("nolatecomers".equalsIgnoreCase(parameter)) {
 			Boolean v = MatchUtils.stringToBoolean(value);
 			if (v == null) return false;
 			match.setNoLatecomers(v);
 			return true;
 		} else {
 			return false;
 		}
 		
 		
 	}
 
 	/**
 	 * Carry out the /matchinfo, /mi or /match command
 	 * 
 	 * @return response
 	 */
 	private String pMatchinfo() {
 		return match.matchTimeAnnouncement(true) + "\n" + match.getPlayerStatusReport(); 
 	}
 
 	/**
 	 * Carry out the /launch command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cLaunch()  {
 		// launch all players
 		match.launchAll();
 		return OK_COLOR + "Player launching has begun";
 	}
 	
 	/**
 	 * Carry out the /relaunch command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cRelaunch(String[] args)  {
 		if (args.length == 0) {
 			return ERROR_COLOR + "Please specify player to relaunch";
 		} else {
 			Player p = getServer().getPlayer(args[0]);
 			if (p == null)
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			
 			if (p.isOp())
 				return ERROR_COLOR + "Player should be deopped before launching";
 			
 			boolean success = match.sendToStartPoint(p);
 			if (success)
 				return OK_COLOR + "Relaunched " + p.getDisplayName();
 			else 
 				return ERROR_COLOR + "Player could not be relaunched";
 		}
 	}
 	
 	/**
 	 * Execute the /cdwb command
 	 * 
 	 * @param args Arguments passed
 	 * @return Message to be displayed
 	 */
 	private String cCdwb(String[] args) {
 		
 		int newRadius;
 		
 		if (args.length == 0 || args.length > 2)
 			return ERROR_COLOR + "Specify world radius and countdown duration";
 		
 		try {
 			newRadius = Integer.parseInt(args[0]);
 		} catch (NumberFormatException e) {
 			return ERROR_COLOR + "World radius must be specified as an integer";
 		}
 		
 		int countLength = 300;
 		
 		if (args.length == 2)
 			countLength = Integer.parseInt(args[1]);
 		
 		if (match.startBorderCountdown(countLength, newRadius))
 			return OK_COLOR + "Countdown started";
 		else
 			return ERROR_COLOR + "Unable to start border countdown";
 		 
 	}
 
 	/**
 	 * Carry out the /ready command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cReady(String[] args) {
 		if (args.length > 1)
 			return ERROR_COLOR + "Usage: /ready [seconds]";
 		
 		int countLength = 300;
 		
 		if (args.length == 1)
 			countLength = Integer.parseInt(args[0]);
 		
 		if (countLength < 120 && match.getMatchPhase() == MatchPhase.PRE_MATCH && !DEBUG_BUILD)
 			return ERROR_COLOR + "Countdown less than 2 minutes - you must launch players first!";
 		
 		if (match.startMatchCountdown(countLength))
 			return OK_COLOR + "Countdown started";
 		else 
 			return ERROR_COLOR + "Countdown already in progress!"; 
 	}
 
 	/**
 	 * Carry out the /notify command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cNotify(UhcPlayer sender, String[] args) {
 		String s = "";
 		if (args.length == 0)
 			s = "no text";
 		else {
 			for (int i = 0; i < args.length; i++) {
 				s += args[i] + " ";
 			}
 			s = s.substring(0, s.length() - 1);
 		}
 
 		match.sendSpectatorNotification(new PlayerMessageNotification(sender, s), sender.getLocation());
 
 		return sender.isSpectator() ? null : OK_COLOR + "Message sent";
 	}
 	
 	/**
 	 * Carry out the /tpn command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpn(UhcSpectator sender) {
 		Location l = match.getLastNotifierLocation();
 		if (l == null)
 			return ERROR_COLOR + "No notification.";
 
 		sender.teleport(l);
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpd command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpd(UhcSpectator sender) {
 
 		Location l = match.getLastDeathLocation();
 		if (l == null)
 			return ERROR_COLOR + "Nobody has died.";
 
 		sender.teleport(l);
 		return null;
 	}
 
 	/**
 	 * Carry out the /tpl command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTpl(UhcSpectator sender) {
 		Location l = match.getLastLogoutLocation();
 		if (l == null)
 			return ERROR_COLOR + "Nobody has logged out.";
 
 		sender.teleport(l);
 		return null;
 	}
 
 
 	/**
 	 * Carry out the /tps command
 	 * 
 	 * @param sender the sender of the command
 	 * @return response
 	 */
 	private String cTps(UhcSpectator sender, String[] args) {
 		// Teleport sender to the specified start point, either by player name or by number
 		if (args.length != 1)
 			return ERROR_COLOR + "Incorrect number of arguments for /tps";
 		
 		UhcStartPoint destination = match.findStartPoint(args[0]);
 		
 		if (destination != null) {
 			sender.teleport(destination.getLocation());
 			return null;
 		} else {
 			return ERROR_COLOR + "Unable to find that start point";
 		}
 	}
 	
 	/**
 	 * Carry out the /tp command for a player
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cTp(UhcSpectator sender, String[] args) {
 		
 		if (args.length == 0) {
 			if (match.getLastEventLocation() == null)
 				return ERROR_COLOR + "You haven't specified to who you want to teleport.";
 
 			sender.teleport(match.getLastEventLocation());
 			return null;
 		}
 		
 		if(args.length == 1){
 			Player to = getServer().getPlayer(args[0]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			sender.teleport(to,OK_COLOR + "Teleported to " + to.getName());
 			
 			return null;
 		}
 		
 		if(args.length == 2){
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Player to = getServer().getPlayer(args[1]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[1] + " not found";
 			from.teleport(to);
 			
 			return OK_COLOR + "Teleported " + from.getName() + " to " + to.getName();
 		}
 		if(args.length==3){
 			// Teleport sender to coords in their current world
 			Double x;
 			Double y;
 			Double z;
 			try {
 				x = new Double (args[0]);
 				y = new Double (args[1]);
 				z = new Double (args[2]);
 			} catch (NumberFormatException e) {
 				return ERROR_COLOR + "Invalid coordinates";
 			}
 			
 			sender.teleport(x,y,z);
 			return null;
 		}
 		if(args.length==4){
 			// Teleport a player to coords in their current world
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Double x;
 			Double y;
 			Double z;
 			try {
 				x = new Double (args[1]);
 				y = new Double (args[2]);
 				z = new Double (args[3]);
 			} catch (NumberFormatException e) {
 				return ERROR_COLOR + "Invalid coordinates";
 			}
 			
 			Location to = new Location(from.getWorld(),x,y,z);
 			from.teleport(to);
 			return OK_COLOR + from.getName() + " has been teleported";
 			
 		}
 		return ERROR_COLOR + "Incorrect number of arguments";
 	}
 
 	/**
 	 * Carry out the /tp command for a console user
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String sTp(String[] args) {
 		
 		if(args.length == 2){
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Player to = getServer().getPlayer(args[1]);
 			if (to == null || !to.isOnline())
 				return ERROR_COLOR + "Player " + args[1] + " not found";
 			from.teleport(to);
 			
 			return OK_COLOR + "Teleported " + from.getName() + " to " + to.getName();
 		}
 
 		if(args.length==4){
 			// Teleport a player to coords in their current world
 			Player from = getServer().getPlayer(args[0]);
 			if (from == null || !from.isOnline())
 				return ERROR_COLOR + "Player " + args[0] + " not found";
 			Double x;
 			Double y;
 			Double z;
 			try {
 				x = new Double (args[1]);
 				y = new Double (args[2]);
 				z = new Double (args[3]);
 			} catch (NumberFormatException e) {
 				return ERROR_COLOR + "Invalid coordinates";
 			}
 			
 			Location to = new Location(from.getWorld(),x,y,z);
 			from.teleport(to);
 			return OK_COLOR + from.getName() + " has been teleported";
 			
 		}
 		return ERROR_COLOR + "Incorrect number of arguments";
 	}
 	
 	/**
 	 * Carry out the /tp0 command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cTp0(UhcSpectator sender) {
 		sender.teleport(match.getStartingWorld().getSpawnLocation());
 		return OK_COLOR + "Teleported to spawn";
 	}
 	
 
 	/**
 	 * Carry out the /renewall command
 	 * 
 	 * @return response
 	 */
 	private String cRenewall() {
 		for (UhcPlayer pl : match.getOnlinePlayers())
 			if (pl.renew()) pl.sendMessage(OK_COLOR + "You have been healed and fed!");
 
 		return OK_COLOR + "Renewed all players";
 	}
 
 	/**
 	 * Carry out the /renew command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cRenew(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player to renew, or use /renewall";
 
 		UhcPlayer up = match.getPlayer(args[0]);
 		
 		if (up.isOnline()) {
 			if (up.renew()) {
 				up.sendMessage(OK_COLOR + "You have been healed and fed!");
 				return OK_COLOR + "Renewed " + up.getName();
 			} else {
 				return ERROR_COLOR + "Could not renew " + up.getName();
 			}
 		} else {
 			return ERROR_COLOR + "Player " + args[0] + " is not registered.";
 		}
 
 	}
 
 	/**
 	 * Carry out the /clearinvall command
 	 * 
 	 * @return response
 	 */
 	private String cClearinvall() {
 		for (UhcPlayer pl : match.getOnlinePlayers())
 			if (pl.clearInventory()) pl.sendMessage(OK_COLOR + "Your inventory has been cleared");
 
 		return OK_COLOR + "Cleared all players' inventories.";
 	}
 
 	/**
 	 * Carry out the /clearinv command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cClearinv(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player to clear, or use /clearinvall";
 
 		UhcPlayer up = match.getPlayer(args[0]);
 		
 		if (up.isOnline()) {
 			if (up.clearInventory()) {
 				up.sendMessage(OK_COLOR + "Your inventory has been cleared");
 				return OK_COLOR + "Cleared inventory of " + up.getName();
 			} else {
 				return ERROR_COLOR + "Could not clear inventory of " + up.getName();
 			}
 		} else {
 			return ERROR_COLOR + "Player " + args[0] + " is not registered.";
 		}
 	}
 
 	/**
 	 * Carry out the /feedall command
 	 * 
 	 * @return response
 	 */
 	private String cFeedall() {
 		for (UhcPlayer pl : match.getOnlinePlayers())
 			if (pl.feed()) pl.sendMessage(OK_COLOR + "You have been fed");
 
 		return OK_COLOR + "Fed all players.";
 	}
 
 	/**
 	 * Carry out the /feed command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cFeed(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player to feed, or use /feedall";
 
 		UhcPlayer up = match.getPlayer(args[0]);
 		
 		if (up.isOnline()) {
 			if (up.feed()) {
 				up.sendMessage(OK_COLOR + "You have been fed");
 				return OK_COLOR + "Fed " + up.getName();
 			} else {
 				return ERROR_COLOR + "Could not feed " + up.getName();
 			}
 		} else {
 			return ERROR_COLOR + "Player " + args[0] + " is not registered.";
 		}
 		
 	}
 
 	/**
 	 * Carry out the /healall command
 	 * 
 	 * @return response
 	 */
 	private String cHealall() {
 		for (UhcPlayer pl : match.getOnlinePlayers())
 			if (pl.heal()) pl.sendMessage(OK_COLOR + "You have been healed");
 
 		return OK_COLOR + "Healed all players.";
 	}
 
 	/**
 	 * Carry out the /heal command
 	 * 
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cHeal(String[] args) {
 		if (args.length == 0)
 			return ERROR_COLOR + "Please specify player to heal, or use /healall";
 
 		UhcPlayer up = match.getPlayer(args[0]);
 		
 		if (up.isOnline()) {
 			if (up.heal()) {
 				up.sendMessage(OK_COLOR + "You have been healed");
 				return OK_COLOR + "Healed " + up.getName();
 			} else {
 				return ERROR_COLOR + "Could not heal " + up.getName();
 			}
 		} else {
 			return ERROR_COLOR + "Player " + args[0] + " is not registered.";
 		}
 		
 	}
 
 	/**
 	 * Carry out the /butcher command
 	 * 
 	 * @param sender the sender of the command
 	 * @param args arguments
 	 * @return response
 	 */
 	private String cButcher() {
 		match.butcherHostile();
 		return "Hostile mobs have been butchered";
 	}
 
 	
 
 
 	public UhcMatch getMatch() {
 		return match;
 	}
 
 
 	
 
 }
