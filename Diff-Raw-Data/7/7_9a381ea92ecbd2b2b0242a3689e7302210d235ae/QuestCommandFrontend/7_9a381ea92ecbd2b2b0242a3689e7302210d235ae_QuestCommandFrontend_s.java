 package com.theminequest.MineQuest.Frontend.Command;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.Backend.BackendFailedException;
 import com.theminequest.MineQuest.Backend.GroupBackend;
 import com.theminequest.MineQuest.Backend.QuestAvailability;
 import com.theminequest.MineQuest.Backend.QuestBackend;
 import com.theminequest.MineQuest.Backend.BackendFailedException.BackendReason;
 import com.theminequest.MineQuest.Group.Group;
 import com.theminequest.MineQuest.Group.GroupException;
 import com.theminequest.MineQuest.Quest.Quest;
 import com.theminequest.MineQuest.Utils.ChatUtils;
 import com.theminequest.MineQuest.Utils.PropertiesFile;
 
 public class QuestCommandFrontend implements CommandExecutor {
 	
 	private PropertiesFile localization;
 
 	public QuestCommandFrontend(){
 		MineQuest.log("[CommandFrontend] Starting Command Frontend for \"quest\"...");
 		localization = MineQuest.configuration.localizationConfig;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
 			String[] arg3) {
 		if (!(arg0 instanceof Player)){
 			MineQuest.log(Level.WARNING,"[CommandFrontend] No console use for \"quest\"...");
 			return false;
 		}
 		Player player = (Player)arg0;
 
 		String cmd = arg3[0].toLowerCase();
 
 		String[] arguments = Arrays.copyOfRange(arg3, 1, arg3.length-1);
		
		if (cmd.equals(""))
			return help(player,arg3);
 
 		try {
 			Method m = this.getClass().getMethod(cmd, Player.class, String[].class);
 			return (Boolean)m.invoke(this, player, arguments);
 		} catch (Exception e) {
 			return false;
 		}
 	}
 	
 	public Boolean accept(Player p, String[] args) {
 		if (args.length!=1){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		try {
 			QuestBackend.acceptQuest(p, args[0]);
 			p.sendMessage(ChatUtils.chatify(localization.getString("quest_accept","Accepted quest!")));
 			return true;
 		} catch (BackendFailedException e) {
 			if (e.getReason()==BackendReason.NOTHAVEQUEST)
 				p.sendMessage(ChatUtils.chatify(localization.getString("quest_NOTHAVEQUEST","You don't have this quest available!")));
 			else {
 				e.printStackTrace();
 				p.sendMessage(ChatUtils.chatify(localization.getString("SQLException","Something went wrong server-side - call in the admins.")));
 			}
 			return false;
 		}
 	}
 	
 	public Boolean accepted(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		List<String> quests;
 		try {
 			quests = QuestBackend.getQuests(QuestAvailability.ACCEPTED, p);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatUtils.chatify(localization.getString("SQLException","Something went wrong server-side - call in the admins.")));
 			return false;
 		}
 		
 		List<String> message = new ArrayList<String>();
 		message.add(ChatUtils.formatHeader(localization.getString("quest_accepted","Accepted (Pending) Quests")));
 		for (String q : quests){
 			message.add(ChatColor.AQUA + q);
 		}
 		
 		for (String m : message)
 			p.sendMessage(m);
 		return true;
 	}
 	
 	public Boolean available(Player p, String[] args){
 		if (args.length!=0){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		List<String> quests;
 		try {
 			quests = QuestBackend.getQuests(QuestAvailability.AVAILABLE, p);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatUtils.chatify(localization.getString("SQLException","Something went wrong server-side - call in the admins.")));
 			return false;
 		}
 		
 		List<String> message = new ArrayList<String>();
 		message.add(ChatUtils.formatHeader(localization.getString("quest_available","Available Quests")));
 		for (String q : quests){
 			message.add(ChatColor.AQUA + q);
 		}
 		
 		for (String m : message)
 			p.sendMessage(m);
 		return true;
 	}
 	
 	public Boolean abandon(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOGROUP", "You're not in a group!"));
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOTLEADER", "not leader!"));
 			return false;
 		}
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOACTIVE", "no active quest!"));
 			return false;
 		}
 		try {
 			g.abandonQuest();
 			return true;
 		} catch (GroupException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public Boolean active(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOGROUP", "You're not in a group!"));
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOACTIVE", "no active quest!"));
 			return false;
 		}
 		p.sendMessage(ChatUtils.formatHeader(localization.getString("quest_active","Active Quest") + ": " + g.getQuest().getName()));
 		p.sendMessage(g.getQuest().getDescription());
 		return true;
 	}
 
 	public Boolean enter(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOGROUP", "You're not in a group!"));
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOACTIVE", "no active quest!"));
 			return false;
 		}
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOTLEADER", "not leader!"));
 			return false;
 		}
 		if (g.isInQuest()){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_INQUEST", "Already inside quest!"));
 			return false;
 		}
 		try {
 			g.enterQuest();
 			return true;
 		} catch (GroupException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public Boolean exit(Player p, String[] args) {
 		if (args.length!=0){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOGROUP", "You're not in a group!"));
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
 		if (g.getQuest()==null){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOACTIVE", "no active quest!"));
 			return false;
 		}
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOTLEADER", "not leader!"));
 			return false;
 		}
 		if (!g.isInQuest()){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOTINQUEST", "Not inside quest!"));
 			return false;
 		}
 		if (g.getQuest().isFinished()==null){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_exit_useabandon", "Quest not finished - to exit now, use abandon."));
 			return false;
 		}
 		try {
 			g.exitQuest();
 			return true;
 		} catch (GroupException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 			return false;
 		}
 	}
 	
 	public Boolean start(Player p, String[] args) {
 		if (args.length!=1){
 			p.sendMessage(ChatUtils.chatify(
 					localization.getString("quest_WRONGARGS", "Wrong number of arguments!")));
 			return false;
 		}
 		if (GroupBackend.teamID(p)==-1){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOGROUP", "You're not in a group!"));
 			return false;
 		}
 		Group g = GroupBackend.getCurrentGroup(p);
 		if (!g.getLeader().equals(p)){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_NOTLEADER", "not leader!"));
 			return false;
 		}
 		if (g.getQuest()!=null){
 			p.sendMessage(ChatColor.RED + localization.getString("quest_ONQUEST", "Already on a quest!"));
 			return false;
 		}
 		
 		List<String> quests;
 		try {
 			quests = QuestBackend.getQuests(QuestAvailability.ACCEPTED, p);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			p.sendMessage(ChatUtils.chatify(localization.getString("SQLException","Something went wrong server-side - call in the admins.")));
 			return false;
 		}
 		
 		if (quests.contains(args[0])){
 			try {
 				g.startQuest(args[0]);
 				return true;
 			} catch (GroupException e) {
 				e.printStackTrace();
 				p.sendMessage(ChatColor.GRAY + "ERR: " + e.getMessage());
 				return false;
 			}
 		}
 		
 		return false;
 	}
 
 	public Boolean help(Player p, String[] args) {
 
 		List<String> messages = new ArrayList<String>();
 		boolean inGroup = false;
 		boolean isLeader = false;
 		boolean inQuest = false;
 		Quest active = null;
 		if (GroupBackend.teamID(p)!=-1){
 			Group g = GroupBackend.getCurrentGroup(p);
 			inGroup = true;
 			isLeader = g.getLeader().equals(p);
 			inQuest = g.isInQuest();
 			active = g.getQuest();
 		}
 
 		/*
 		 * accept <name>
 		 * accepted
 		 * available
 		 * 
 		 * abandon
 		 * active
 		 * enter
 		 * exit
 		 * start <name>
 		 */
 		messages.add(ChatUtils.formatHeader(localization.getString("quest_help", "Quest Commands")));
 		messages.add(ChatUtils.formatHelp("quest accept <name>", localization.getString("quest_help_accept", "Accept a quest.")));
 		messages.add(ChatUtils.formatHelp("quest accepted", localization.getString("quest_help_accepted", "List accepted (pending) quests.")));
 		messages.add(ChatUtils.formatHelp("quest available", localization.getString("quest_help_available", "List available quests.")));
 
 		if (inGroup){
 			if (active != null && isLeader)
 				messages.add(ChatUtils.formatHelp("quest abandon", localization.getString("quest_help_abandon", "Abandon active quest.")));
 			else if (active != null)
 				messages.add(ChatColor.GRAY + "[quest abandon] " + localization.getString("quest_NOTLEADER", "not leader!"));
 			else
 				messages.add(ChatColor.GRAY + "[quest abandon] " + localization.getString("quest_NOACTIVE", "no active quest!"));
 			if (active!=null)
 				messages.add(ChatUtils.formatHelp("quest active", localization.getString("quest_help_active", "View active quest.")));
 			else
 				messages.add(ChatColor.GRAY + "[quest active] " + localization.getString("quest_NOACTIVE", "no active quest!"));
 			if (active!=null && !inQuest && isLeader)
 				messages.add(ChatUtils.formatHelp("quest enter", localization.getString("quest_help_enter", "Enter active quest.")));
 			else if (active!=null && inQuest && isLeader)
 				messages.add(ChatUtils.formatHelp("quest exit", localization.getString("quest_help_exit", "Exit active quest.")));
 			else if (active!=null && !isLeader)
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + localization.getString("quest_NOTLEADER", "not leader!"));
 			else
 				messages.add(ChatColor.GRAY + "[quest enter/exit] " + localization.getString("quest_NOACTIVE", "no active quest!"));
 			if (active == null && isLeader)
 				messages.add(ChatUtils.formatHelp("quest start <name>", localization.getString("quest_help_start", "Start pending quest.")));
 			else if (active == null)
 				messages.add(ChatColor.GRAY + "[quest start] " + localization.getString("quest_NOTLEADER", "not leader!"));
 			else
 				messages.add(ChatColor.GRAY + "[quest start] " + localization.getString("quest_ONQUEST", "Already on a quest!"));
 		} else
 			messages.add(ChatColor.AQUA + localization.getString("quest_help_joingroup", "Join a group to see all available options!"));
 
 		for (String m : messages) {
 			p.sendMessage(m);
 		}
 
 		return true;
 	}
 
 }
