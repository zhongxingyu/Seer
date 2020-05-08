 //The Package
 package com.java.phondeux.team;
 
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Team for Bukkit
  *
  * @author Phondeux
  */
 //Starts the class
 public class Team extends JavaPlugin{
 	protected Logger log;
 	protected TeamHandler tdbh;
 	
 	private final TeamPlayerListener playerListener = new TeamPlayerListener(this);
 
 	@Override
 	//When the plugin is disabled this method is called.
 	public void onDisable() {
 		//Print "Team Disabled" on the log.
 		System.out.println("Team Disabled");
 	}
 
 	@Override
 	//When the plugin is enabled this method is called.
 	public void onEnable() {
 		log = Logger.getLogger("Minecraft");
 		getCommand("team").setExecutor(new TeamCommand(this));
 
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
 
 		initialize();
 		//Get the infomation from the yml file.
         PluginDescriptionFile pdfFile = this.getDescription();
         //Print that the plugin has been enabled!
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 
 	private void initialize() {
 		// TODO Auto-generated method stub
 		try {
        		Class.forName("com.mysql.jdbc.Driver");
         } catch (Exception e) {
         	e.printStackTrace();
         	log.severe("Team: Couldn't find JDBC.");
         	getPluginLoader().disablePlugin(this);
         	return;
         }
         
         log.info("[Team] Initializing TeamHandler");
         try {
        	tdbh = new TeamHandler(this, "teamdata", "teamuser", "teampass");
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
 
 	}
 }
