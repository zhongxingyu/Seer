 package com.theminequest.bukkit.frontend.cmd;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.group.Group;
 import com.theminequest.api.group.Group.QuestStatus;
 import com.theminequest.api.group.GroupException;
 import com.theminequest.api.platform.MQPlayer;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestDetails;
 import com.theminequest.api.quest.QuestDetailsUtils;
 import com.theminequest.api.quest.QuestUtils;
 import com.theminequest.api.statistic.LogStatus;
 import com.theminequest.api.statistic.QuestStatisticUtils;
 import com.theminequest.api.statistic.QuestStatisticUtils.QSException;
 import com.theminequest.api.util.ChatUtils;
 
 public class QuestCommandFrontend extends CommandFrontend {
 	
 	public static final String IRELOAD = ChatColor.YELLOW + _("Reloading Quest(s): check the server.log for status.");
 	public static final String INOHAVE = ChatColor.RED + _("You don't have this quest!");
 	public static final String INOSUCH = ChatColor.RED + _("No such quest!");
 	public static final String IPENDING = _("Pending Quests");
 	public static final String INONE = _("No quests!");
 	public static final String IWORLD = _("World Quests");
 	public static final String IUNAVAIL = ChatColor.AQUA + _("The quest seems to be unavailable right now.");
 	public static final String INOTACTIVE = ChatColor.RED + _("Not on an active quest!");
 	public static final String IFINISHED = ChatColor.YELLOW + _("The quest has already finished!");
 	public static final String IUNFINISHED = ChatColor.YELLOW + _("The quest isn't finished! To leave now, abandon the quest.");
 	public static final String INOTIN = ChatColor.YELLOW + _("You haven't entered the quest!");
 	public static final String IACTIVE = ChatColor.YELLOW + _("Already on an active quest!");
 	public static final String IACTIVELOAD = ChatColor.YELLOW + _("Loading the world...");
 	
 	public QuestCommandFrontend() {
 		super("quest");
 	}
 	
 	public void reload(Player p, String[] args) {
 		if (args.length > 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		if (args.length == 0)
 			Managers.getQuestManager().reloadQuests();
 		else
 			Managers.getQuestManager().reloadQuest(args[0]);
 		p.sendMessage(QuestCommandFrontend.IRELOAD);
 	}
 	
 	public void admindrop(Player p, String[] args) {
 		if (args.length != 3) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		String username = args[0];
 		String questname = args[1];
 		boolean complete = Boolean.parseBoolean(args[2]);
 		if (complete)
 			try {
 				QuestStatisticUtils.completeQuest(username, questname);
 				QuestDetails d = Managers.getQuestManager().getDetails(args[0]);
 				if ((d == null) || (d.getProperty(QuestDetails.QUEST_COMPLETE) == null))
 					p.sendMessage(ChatColor.AQUA + _("An administrator dropped quest {0} for you with completion.", questname));
 				else
 					p.sendMessage((String) d.getProperty(QuestDetails.QUEST_COMPLETE));
 			} catch (QSException e) {
 				p.sendMessage(QuestCommandFrontend.INOHAVE);
 			}
 		else
 			try {
 				QuestStatisticUtils.dropQuest(username, questname);
 				QuestDetails d = Managers.getQuestManager().getDetails(args[0]);
 				if ((d == null) || (d.getProperty(QuestDetails.QUEST_ABORT) == null))
 					p.sendMessage(ChatColor.AQUA + _("An administrator aborted quest {0} for you.", questname));
 				else
 					p.sendMessage(ChatUtils.chatify((String) d.getProperty(QuestDetails.QUEST_ABORT)));
 			} catch (QSException e) {
 				p.sendMessage(QuestCommandFrontend.INOHAVE);
 			}
 	}
 	
 	public void admininfo(Player p, String[] args) {
 		if (args.length != 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 		if (qd == null) {
 			p.sendMessage(QuestCommandFrontend.INOSUCH);
 			return;
 		}
 		p.sendMessage(QuestDetailsUtils.getOverviewString(qd).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 	}
 	
 	public void admingive(Player p, String[] args) {
 		if (args.length != 2) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		String username = args[0];
 		String questname = args[1];
 		
 		Player userPlayer = Bukkit.getPlayer(username);
 		if (userPlayer == null) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		
 		QuestDetails d = Managers.getQuestManager().getDetails(questname);
 		if (d == null) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		
 		if (QuestStatisticUtils.hasQuest(userPlayer.getName(), questname) == LogStatus.GIVEN) {
 			p.sendMessage(userPlayer.getName() + " already has this quest! Check their given quests!");
 			return;
 		}
 		
 		if (QuestStatisticUtils.hasQuest(userPlayer.getName(), questname) == LogStatus.ACTIVE) {
 			p.sendMessage(userPlayer.getName() + "already has this quest running actively in their world!");
 			return;
 		}
 		
 		if (QuestDetailsUtils.getRequirementsMet(d, Managers.getPlatform().toPlayer(userPlayer)))
 			try {
 				QuestStatisticUtils.giveQuest(userPlayer.getName(), questname);
 				userPlayer.sendMessage(ChatColor.GREEN + "Successfully added " + d.getProperty(QuestDetails.QUEST_DISPLAYNAME) + " to your quest list!");
 				p.sendMessage(ChatColor.GREEN + "Added to their quest list.");
 			} catch (QSException e) {
 				p.sendMessage("This quest doesn't seem to like them. Check the console for the error...");
 				e.printStackTrace();
 			}
 		else
 			p.sendMessage("This quest is currently " + ChatColor.BOLD + ChatColor.RED + "not available" + ChatColor.RESET + " to them.");
 	}
 	
 	public void userhas(Player sender, String[] args) {
 		if (args.length != 1) {
 			sender.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		
 		Player p = Bukkit.getPlayer(args[0]);
 		
 		/* Given/Pending Quests */
 		Map<String, Date> givenquests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.GIVEN);
 		
 		List<String> givenmessage = new ArrayList<String>();
 		givenmessage.add(ChatUtils.formatHeader(QuestCommandFrontend.IPENDING));
 		for (String q : givenquests.keySet()) {
 			if (q.isEmpty())
 				continue;
 			QuestDetails qd = Managers.getQuestManager().getDetails(q);
 			if (qd != null)
 				givenmessage.add(ChatColor.AQUA + q + " : " + ChatColor.GOLD + qd.getProperty(QuestDetails.QUEST_DISPLAYNAME));
 			else
 				givenmessage.add(ChatColor.GRAY + q + " : <?>");
 		}
 		
 		for (String m : givenmessage)
 			sender.sendMessage(m);
 		if (givenmessage.size() == 1)
 			sender.sendMessage(QuestCommandFrontend.INONE);
 		
 		/* Main World/"World" Quests */
 		Map<String, Date> mqquests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.ACTIVE);
 		List<String> mwmessage = new ArrayList<String>();
 		mwmessage.add(ChatUtils.formatHeader(QuestCommandFrontend.IWORLD));
 		for (String q : mqquests.keySet()) {
 			if (q.isEmpty())
 				continue;
 			Quest quest = Managers.getQuestManager().getMainWorldQuest(p.getName(), q);
 			mwmessage.add(ChatColor.LIGHT_PURPLE + q + " : " + ChatColor.GOLD + quest.getDetails().getProperty(QuestDetails.QUEST_DISPLAYNAME) + ChatColor.GRAY + " in " + quest.getDetails().getProperty(QuestDetails.QUEST_WORLD));
 		}
 		for (String m : mwmessage)
 			sender.sendMessage(m);
 		if (mwmessage.size() == 1)
 			sender.sendMessage(QuestCommandFrontend.INONE);
 	}
 	
 	public void drop(Player p, String[] args) {
 		if (args.length != 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		try {
 			QuestStatisticUtils.dropQuest(p.getName(), args[0]);
 			QuestDetails d = Managers.getQuestManager().getDetails(args[0]);
 			if ((d == null) || (d.getProperty(QuestDetails.QUEST_ABORT) == null))
 				p.sendMessage(ChatColor.AQUA + _("Dropped quest {0}.", args[0]));
 			else
 				p.sendMessage(ChatUtils.chatify((String) d.getProperty(QuestDetails.QUEST_ABORT)));
 		} catch (QSException e) {
 			p.sendMessage(QuestCommandFrontend.INOHAVE);
 		}
 	}
 	
 	public void info(Player p, String[] args) {
 		if (args.length != 1) {
 			p.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		
 		LogStatus infoQuest = QuestStatisticUtils.hasQuest(p.getName(), args[0]);
 		switch (infoQuest) {
 		case ACTIVE:
 			Quest q = Managers.getQuestManager().getMainWorldQuest(p.getName(), args[0]);
 			if (q == null)
 				p.sendMessage(QuestCommandFrontend.IUNAVAIL);
 			else
 				p.sendMessage(QuestUtils.getStatusString(q).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 			break;
 		case GIVEN:
 			QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 			if (qd == null)
 				p.sendMessage(QuestCommandFrontend.IUNAVAIL);
 			else
 				p.sendMessage(QuestDetailsUtils.getOverviewString(qd).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 			break;
 		default:
 			p.sendMessage(QuestCommandFrontend.INOHAVE);
 			break;
 		}
 		
 	}
 	
 	public void abandon(Player pl, String[] args) {
 		if (args.length != 0) {
 			pl.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		if (!g.getLeader().equals(p)) {
 			p.sendMessage(PartyCommandFrontend.INOTLEADER);
 			return;
 		}
 		if (g.getQuest() == null) {
 			p.sendMessage(QuestCommandFrontend.INOTACTIVE);
 			return;
 		}
 		if (g.getQuest().isFinished() != null) {
 			p.sendMessage(QuestCommandFrontend.IFINISHED);
 			return;
 		}
 		try {
 			g.abandonQuest();
 		} catch (GroupException e) {
 			throw new RuntimeException(e); // throw to CommandFrontend
 		}
 	}
 	
 	public void exit(Player pl, String[] args) {
 		if (args.length != 0) {
 			pl.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			p.sendMessage(PartyCommandFrontend.IOUTPARTY);
 			return;
 		}
 		Group g = Managers.getGroupManager().get(p);
 		if (g.getQuest() == null) {
 			p.sendMessage(QuestCommandFrontend.INOTACTIVE);
 			return;
 		}
 		if (!g.getLeader().equals(p)) {
 			p.sendMessage(PartyCommandFrontend.INOTLEADER);
 			return;
 		}
 		if (g.getQuestStatus() != QuestStatus.INQUEST) {
 			p.sendMessage(QuestCommandFrontend.INOTIN);
 			return;
 		}
 		if (g.getQuest().isFinished() == null) {
 			p.sendMessage(QuestCommandFrontend.IUNFINISHED);
 			return;
 		}
 		try {
 			g.exitQuest();
 		} catch (GroupException e) {
 			throw new RuntimeException(e); // toss to CommandFrontend
 		}
 	}
 	
 	public void start(final Player pl, final String[] args) {
 		if (args.length != 1) {
 			pl.sendMessage(CommandFrontend.IINVALID);
 			return;
 		}
 		
 		Map<String, Date> quests = QuestStatisticUtils.getQuests(pl.getName(), LogStatus.GIVEN);
 		
 		if (!quests.containsKey(args[0])) {
 			pl.sendMessage(QuestCommandFrontend.INOHAVE);
 			return;
 		}
 		
 		final QuestDetails qd = Managers.getQuestManager().getDetails(args[0]);
 		if (qd == null) {
 			pl.sendMessage(QuestCommandFrontend.IUNAVAIL);
 			return;
 		}
 		
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		if (Managers.getGroupManager().indexOf(p) == -1) {
 			Managers.getGroupManager().createNewGroup(p);
 			p.sendMessage(PartyCommandFrontend.ICREATE);
 		}
 		final Group g = Managers.getGroupManager().get(p);
 		if (!g.getLeader().equals(p)) {
 			p.sendMessage(PartyCommandFrontend.INOTLEADER);
 			return;
 		}
 		if (g.getQuest() != null) {
 			p.sendMessage(QuestCommandFrontend.IACTIVE);
 			return;
 		}
		
		Managers.getPlatform().scheduleSyncTask(new Runnable() {
 			
 			@Override
 			public void run() {
 				pl.sendMessage(QuestCommandFrontend.IACTIVELOAD);
 				try {
 					g.startQuest(qd);
 					g.enterQuest();
 				} catch (GroupException e) {
 					throw new RuntimeException(e); // throw to CommandFrontend
 				}
 			}
 			
		});
 	}
 	
 	@Override
 	public void help(CommandSender sender, String[] args) {
 		
 		Player p = (Player) sender;
 		
 		List<String> messages = new LinkedList<String>();
 		
 		/*
 		 * OP: reload [name]
 		 * OP: admindrop <username> <name> <true/false (complete?)>
 		 * OP: admininfo [name]
 		 * OP: admingive <username> <questname>
 		 * OP: userhas <username>
 		 * NoByDefault: drop <name>
 		 * 
 		 * info [name] // when called by non-admin, it checks if the player has
 		 * the actual quest
 		 * 
 		 * abandon
 		 * active // in instance only : shows instance status.
 		 * exit
 		 * start <name>
 		 */
 		
 		messages.add(ChatUtils.formatHeader(_("Quest Help Menu")));
 		
 		// admin
 		if (p.hasPermission("minequest.command.quest.reload"))
 			messages.add(ChatUtils.formatHelp("quest reload [name]", _("Reload one or all quests.")));
 		if (p.hasPermission("minequest.command.quest.admindrop"))
 			messages.add(ChatUtils.formatHelp("quest admindrop <user> <name> <true/false>", _("Drop a quest for a user with or without completion.")));
 		if (p.hasPermission("minequest.command.quest.admingive"))
 			messages.add(ChatUtils.formatHelp("quest admingive <user> <quest>", _("Give a quest to a user.")));
 		if (p.hasPermission("minequest.command.quest.admininfo"))
 			messages.add(ChatUtils.formatHelp("quest admininfo <name>", _("View information about a quest.")));
 		if (p.hasPermission("minequest.command.quest.userhas"))
 			messages.add(ChatUtils.formatHelp("quest userhas <user>", _("View quests a user has.")));
 		
 		// off by default
 		if (p.hasPermission("minequest.command.quest.drop"))
 			messages.add(ChatUtils.formatHelp("quest drop <name>", _("Drop a quest.")));
 		
 		MQPlayer mp = Managers.getPlatform().toPlayer(p);
 		
 		// basic commands
 		boolean inGroup = Managers.getGroupManager().get(mp) != null;
 		boolean isLeader = inGroup && Managers.getGroupManager().get(mp).getLeader().equals(mp);
 		QuestStatus hasQuest = inGroup ? Managers.getGroupManager().get(mp).getQuestStatus() : QuestStatus.NOQUEST;
 		CompleteStatus questStatus = hasQuest == QuestStatus.INQUEST ? Managers.getGroupManager().get(mp).getQuest().isFinished() : CompleteStatus.IGNORE;
 		
 		switch (hasQuest) {
 		case INQUEST:
 			messages.add(ChatUtils.formatHelp("quest", _("View information about the active quest.")));
 			if (isLeader)
 				if (questStatus == null)
 					messages.add(ChatUtils.formatHelp("quest abandon", _("Abandon the quest.")));
 				else
 					messages.add(ChatUtils.formatHelp("quest exit", _("Exit the quest.")));
 			break;
 		case MAINWORLDQUEST:
 			break;
 		case NOQUEST:
 		case NOTINQUEST:
 			if (p.hasPermission("minequest.command.quest.info"))
 				messages.add(ChatUtils.formatHelp("quest info [name]", _("Get information about a quest you have.")));
 			if (isLeader)
 				messages.add(ChatUtils.formatHelp("quest start [name]", _("Start a quest!")));
 			else if (!inGroup)
 				messages.add(ChatUtils.formatHelp("quest start [name]", _("Create a party and start a quest!")));
 			break;
 		default:
 			break;
 		}
 		
 		for (String m : messages)
 			p.sendMessage(m);
 	}
 	
 	@Override
 	public boolean allowConsole() {
 		return false;
 	}
 	
 	@Override
 	public void noOptionSpecified(CommandSender sender, String[] args) {
 		Player pl = (Player) sender;
 		
 		/*
 		 * Is the player in an active quest? If so, show the current quest.
 		 */
 		
 		MQPlayer p = Managers.getPlatform().toPlayer(pl);
 		
 		Group group = Managers.getGroupManager().get(p);
 		boolean inGroup = group != null;
 		boolean isLeader = inGroup && group.getLeader().equals(p);
 		QuestStatus hasQuest = inGroup ? group.getQuestStatus() : QuestStatus.NOQUEST;
 		CompleteStatus questStatus = hasQuest == QuestStatus.INQUEST ? group.getQuest().isFinished() : CompleteStatus.IGNORE;
 		
 		if (hasQuest == QuestStatus.INQUEST) {
 			
 			p.sendMessage(QuestUtils.getStatusString(group.getQuest()).split(QuestDetailsUtils.CODE_NEWLINE_SEQ));
 			
 			if (isLeader)
 				if (questStatus == null)
 					p.sendMessage(ChatUtils.formatHelp("quest abandon", _("Abandon the quest.")));
 				else
 					p.sendMessage(ChatUtils.formatHelp("quest exit", _("Exit the quest.")));
 			
 		} else {
 			
 			/* Given Quests */
 			Map<String, Date> givenquests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.GIVEN);
 			
 			List<String> givenmessage = new ArrayList<String>();
 			givenmessage.add(ChatUtils.formatHeader(QuestCommandFrontend.IPENDING));
 			for (String q : givenquests.keySet()) {
 				if (q.isEmpty())
 					continue;
 				QuestDetails qd = Managers.getQuestManager().getDetails(q);
 				if (qd != null)
 					givenmessage.add(ChatColor.AQUA + q + " : " + ChatColor.GOLD + qd.getProperty(QuestDetails.QUEST_DISPLAYNAME));
 				else
 					givenmessage.add(ChatColor.GRAY + q + " : <?>");
 			}
 			
 			for (String m : givenmessage)
 				p.sendMessage(m);
 			if (givenmessage.size() == 1)
 				p.sendMessage(QuestCommandFrontend.INONE);
 			
 			/* Main World Quests */
 			Map<String, Date> mqquests = QuestStatisticUtils.getQuests(p.getName(), LogStatus.ACTIVE);
 			List<String> mwmessage = new ArrayList<String>();
 			mwmessage.add(ChatUtils.formatHeader(QuestCommandFrontend.IWORLD));
 			for (String q : mqquests.keySet()) {
 				if (q.isEmpty())
 					continue;
 				Quest quest = Managers.getQuestManager().getMainWorldQuest(p.getName(), q);
 				mwmessage.add(ChatColor.LIGHT_PURPLE + q + " : " + ChatColor.GOLD + quest.getDetails().getProperty(QuestDetails.QUEST_DISPLAYNAME) + ChatColor.GRAY + " in " + quest.getDetails().getProperty(QuestDetails.QUEST_WORLD));
 			}
 			for (String m : mwmessage)
 				p.sendMessage(m);
 			if (mwmessage.size() == 1)
 				p.sendMessage(QuestCommandFrontend.INONE);
 			
 		}
 		
 		p.sendMessage(ChatUtils.formatHeader(_("Help Options: /quest help")));
 	}
 	
 }
