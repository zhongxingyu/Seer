 package com.java.phondeux.team;
 
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Team for Bukkit
  *
  * @author Phondeux
  */
 public class Team extends JavaPlugin{
 	protected Logger log;
 	protected ConnectionManager cm;
 	protected TeamHandler th;
 	public EventHandler eh;
	protected StatsHandler sh;
 	
 	private final TeamPlayerListener playerListener = new TeamPlayerListener(this);
 	private final TeamEntityListener entityListener = new TeamEntityListener(this);
 
 	@Override
 	public void onDisable() {
 		System.out.println("[team] disabled");
 	}
 
 	@Override
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		getCommand("team").setExecutor(new TeamCommand(this));
 
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Monitor, this);
 
 		initialize();
 		
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 
 	private void initialize() {
         try {
         	log.info("[Team] Connecting to database..");
         	cm = new ConnectionManager("localhost/teamdata", "teamuser", "teampass");
         	log.info("[Team] Initializing TeamHandler..");
         	th = new TeamHandler(this, cm);
         	log.info("[Team] Initializing EventHandler..");
         	eh = new EventHandler(this, cm);
         } catch (SQLException e) {
         	e.printStackTrace();
         	log.severe("[Team] Initialization failed due to SQLException!");
         	getPluginLoader().disablePlugin(this);
         	return;
         } catch (ClassNotFoundException e) {
         	e.printStackTrace();
         	log.severe("[Team] Initialization failed due to the driver not being found!");
         	getPluginLoader().disablePlugin(this);
         	return;
         }
         initializeEvents();
 	}
 	
 	private void initializeEvents() {
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				getServer().broadcastMessage(ChatColor.GOLD + th.playerGetName(parent) + " created a new team, " + ChatColor.WHITE + th.teamGetName(child) + ChatColor.GOLD + "!");
 			}
 		}, EventHandler.Type.TeamCreate);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				getServer().broadcastMessage(ChatColor.GOLD + th.playerGetName(parent) + " disbanded the team " + ChatColor.WHITE + th.teamGetName(child) + ChatColor.GOLD + "!");
 				th.teamSendToMembers(child, ChatColor.RED + "disbanded.");
 			}
 		}, EventHandler.Type.TeamDisband);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
				th.teamSendToMembers(child, TeamUtils.formatTeam(sh.GetTeamStats(child), data));
 			}
 		}, EventHandler.Type.TeamMotd);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				th.teamSendToMembers(child, ChatColor.GOLD + th.playerGetName(parent) + " joined!");
 			}
 		}, EventHandler.Type.PlayerJoin);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				th.teamSendToMembers(child, ChatColor.GOLD + th.playerGetName(parent) + " left!");
 			}
 		}, EventHandler.Type.PlayerLeave);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				th.teamSendToMembers(child, ChatColor.GOLD + th.playerGetName(parent) + " is now invited!");
 			}
 		}, EventHandler.Type.PlayerInvite);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				th.teamSendToMembers(child, ChatColor.GOLD + th.playerGetName(parent) + " is no longer invited!");
 			}
 		}, EventHandler.Type.PlayerDeinvite);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				th.teamSendToMembers(child, ChatColor.GOLD + th.playerGetName(parent) + " was kicked!");
 			}
 		}, EventHandler.Type.PlayerKicked);
 		
 		eh.RegisterCallback(new EventHandler.EventCallback() {
 			public void run(int parent, int child, String data) {
 				if (parent != 0) {
 					getServer().broadcastMessage(th.playerGetName(parent) + ChatColor.GOLD + " killed " + ChatColor.WHITE + th.playerGetName(child) + ChatColor.GOLD + ", data: " + data);
 				} else {
 					getServer().broadcastMessage(th.playerGetName(child) + ChatColor.GOLD + " was killed, data: " + data);
 				}
 			}
 		}, EventHandler.Type.PlayerDeath);
 	}
 }
