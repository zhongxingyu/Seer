 package com.lebelw.Tickets;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandException;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.Event;
 
 import com.lebelw.Tickets.commands.TemplateCmd;
 import com.lebelw.Tickets.extras.CommandManager;
 import com.lebelw.Tickets.extras.DataManager;
 
 import com.iConomy.*;
 import cosine.boseconomy.*;
 
 public class Tickets extends JavaPlugin {
 	private int debug = 0;
 	public static String name;
     public static String version;
 	private final TServerListener serverListener = new TServerListener(this);
 	private final TPlayerListener playerListener = new TPlayerListener(this);
 	private final CommandManager commandManager = new CommandManager(this);
 	public iConomy iConomy = null;
 	public BOSEconomy BOSEconomy = null;
 	public static DataManager dbm;
 	public void onEnable() {
 		name = this.getDescription().getName();
 		version = this.getDescription().getVersion();
 		PluginManager pm = getServer().getPluginManager();
         // Makes sure all plugins are correctly loaded.
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
 		TLogger.initialize(Logger.getLogger("Minecraft"));
 		TConfig TConfig = new TConfig(this);
 		TConfig.configCheck();
 		TDatabase.initialize(this);
 		TPermissions.initialize(this);
 		TLogger.info("Enabled");
 		
 		dbm = TDatabase.dbm;
 		//Let's setup commands
 		addCommand("ticket", new TemplateCmd(this));
 	}
 	
 	public void onDisable(){
 		TDatabase.disable();
 		TLogger.info("Disabled");
 	}
 	public boolean inDebugMode(){
 		if (debug == 0){
 			return false;
 		}
 		else{
 			return true;
 		}
 	}
 	/*
      * Executes a command when a command event is received.
      * 
      * @param sender    The thing that sent the command.
      * @param cmd       The complete command object.
      * @param label     The label of the command.
      * @param args      The arguments of the command.
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         return commandManager.dispatch(sender, cmd, label, args);
     }
 
     /*
      * Adds the specified command to the command manager and server.
      * 
      * @param command   The label of the command.
      * @param executor  The command class that excecutes the command.
      */
     private void addCommand(String command, CommandExecutor executor) {
         getCommand(command).setExecutor(executor);
         commandManager.addCommand(command, executor);
     }
     /**
     * Match player names.
     *
     * @param filter
     * @return
     */
         public List<Player> matchPlayerNames(String filter) {
             Player[] players = getServer().getOnlinePlayers();
 
             filter = filter.toLowerCase();
             
             // Allow exact name matching
             if (filter.charAt(0) == '@' && filter.length() >= 2) {
                 filter = filter.substring(1);
                 
                 for (Player player : players) {
                     if (player.getName().equalsIgnoreCase(filter)) {
                         List<Player> list = new ArrayList<Player>();
                         list.add(player);
                         return list;
                     }
                 }
                 
                 return new ArrayList<Player>();
             // Allow partial name matching
             } else if (filter.charAt(0) == '*' && filter.length() >= 2) {
                 filter = filter.substring(1);
                 
                 List<Player> list = new ArrayList<Player>();
                 
                 for (Player player : players) {
                     if (player.getName().toLowerCase().contains(filter)) {
                         list.add(player);
                     }
                 }
                 
                 return list;
             
             // Start with name matching
             } else {
                 List<Player> list = new ArrayList<Player>();
                 
                 for (Player player : players) {
                     if (player.getName().toLowerCase().startsWith(filter)) {
                         list.add(player);
                     }
                 }
                 
                 return list;
             }
         }
     /**
     * Checks to see if the sender is a player, otherwise throw an exception.
     *
     * @param sender
     * @return
     * @throws CommandException
     */
         public Player checkPlayer(CommandSender sender)
                 throws CommandException {
             if (sender instanceof Player) {
                 return (Player) sender;
             } else {
                 throw new CommandException("A player context is required. (Specify a world or player if the command supports it.)");
             }
         }
     /**
     * Checks if the given list of players is greater than size 0, otherwise
     * throw an exception.
     *
     * @param players
     * @return
     * @throws CommandException
     */
         protected Iterable<Player> checkPlayerMatch(List<Player> players)
                 throws CommandException {
             // Check to see if there were any matches
             if (players.size() == 0) {
                 throw new CommandException("No players matched query.");
             }
             
             return players;
         }
     /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param source
     * @param filter
     * @return iterator for players
     * @throws CommandException no matches found
     */
     public Iterable<Player> matchPlayers(CommandSender source, String filter)
             throws CommandException {
         
         if (getServer().getOnlinePlayers().length == 0) {
             throw new CommandException("No players matched query.");
         }
         
         if (filter.equals("*")) {
             return checkPlayerMatch(Arrays.asList(getServer().getOnlinePlayers()));
         }
 
         // Handle special hash tag groups
         if (filter.charAt(0) == '#') {
             // Handle #world, which matches player of the same world as the
             // calling source
             if (filter.equalsIgnoreCase("#world")) {
                 List<Player> players = new ArrayList<Player>();
                 Player sourcePlayer = checkPlayer(source);
                 World sourceWorld = sourcePlayer.getWorld();
                 
                 for (Player player : getServer().getOnlinePlayers()) {
                     if (player.getWorld().equals(sourceWorld)) {
                         players.add(player);
                     }
                 }
 
                 return checkPlayerMatch(players);
             
             // Handle #near, which is for nearby players.
             } else if (filter.equalsIgnoreCase("#near")) {
                 List<Player> players = new ArrayList<Player>();
                 Player sourcePlayer = checkPlayer(source);
                 World sourceWorld = sourcePlayer.getWorld();
                 org.bukkit.util.Vector sourceVector
                         = sourcePlayer.getLocation().toVector();
                 
                 for (Player player : getServer().getOnlinePlayers()) {
                     if (player.getWorld().equals(sourceWorld)
                             && player.getLocation().toVector().distanceSquared(
                                     sourceVector) < 900) {
                         players.add(player);
                     }
                 }
 
                 return checkPlayerMatch(players);
             
             } else {
                 throw new CommandException("Invalid group '" + filter + "'.");
             }
         }
         
         List<Player> players = matchPlayerNames(filter);
         
         return checkPlayerMatch(players);
     }
     /**
     * Match only a single player.
     *
     * @param sender
     * @param filter
     * @return
     * @throws CommandException
     */
     public Player matchSinglePlayer(CommandSender sender, String filter)
             throws CommandException {
     	//TODO:Add player database checkup
         // This will throw an exception if there are no matches
         Iterator<Player> players = matchPlayers(sender, filter).iterator();
         
         Player match = players.next();
         
         // We don't want to match the wrong person, so fail if if multiple
         // players were found (we don't want to just pick off the first one,
         // as that may be the wrong player)
         if (players.hasNext()) {
             throw new CommandException("More than one player found! " +
              "Use @<name> for exact matching.");
         }
         
         return match;
     }
     // Checks if the current user is actually a player and returns the name of that player.
     public String getName(CommandSender sender) {
         String name = "";
         if (isPlayer(sender)) {
             Player player = (Player) sender;
             name = player.getName();
         }
         return name;
     }
 
     // Gets the player if the current user is actually a player.
     public Player getPlayer(CommandSender sender) {
         Player player = null;
         if (isPlayer(sender)) {
             player = (Player) sender;
         }
         return player;
     }
 
     public String colorizeText(String text, ChatColor color) {
         return color + text + ChatColor.WHITE;
     }
     /*
      * Checks if a player account exists
      * 
      * @param name    The full name of the player.
      */
     public boolean checkIfPlayerExists(String name)
     {
     	ResultSet result = dbm.query("SELECT id FROM players WHERE name = '" + name + "'");
 		try {
 			if (result != null  && result.next()){
 				return true;
 			}else
 				throw new CommandException("You do not have a ticket account! Please reconnect.");
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 			return false;
 		}
     }
     /*
      * Get the amount of tickets a player have
      * 
      * @param name    The full name of the player.
      */
     public ResultSet getPlayerTicket(String name){
     	if (checkIfPlayerExists(name)){
     		int id = getPlayerId(name);
     		if (id < 0)
     			return null;
     		ResultSet result = dbm.query("SELECT business.name, tickets.tickets FROM tickets LEFT JOIN business ON (tickets.business_id = business.id) WHERE user_id= '" + id + "'");
     			return result;
     	}else
     		return null;
     }
     public int getBusinessPlayerTicket(String name, String business){
     	if (checkIfPlayerExists(name)){
     		int id = getPlayerId(name);
     		if (id < 0)
     			throw new CommandException("No tickets account found under your name. Please reconnect.");
     		int idbusiness = TBusiness.getBusinessId(business);
     		if (id < 0)
     			throw new CommandException("No business found!");
     		ResultSet result = dbm.query("SELECT tickets FROM tickets WHERE user_id="+ id +" AND business_id="+ idbusiness +"");
     		try {
     			if (result != null  && result.next()){
     				return result.getInt("tickets");
     			}else
     				return 0;
     		} catch (SQLException e) {
     			TLogger.warning(e.getMessage());
     		}
     	}
     	return -1;
     }
     /*
      * Create a player ticket account
      * 
      * @param name    The full name of the player.
      */
     public boolean createPlayerTicketAccount(String name){
     	if (!checkIfPlayerExists(name)){
     		if(dbm.insert("INSERT INTO players(name) VALUES('" + name + "')")){
     			return true;
     		}else{
     			throw new CommandException("Error while adding " + name + " ticket account.");
     		}
     	}else
     		throw new CommandException("Account already exists!");
     }
     public boolean removePlayerTicket(String name, Integer amount,String businessname){
     	int currentticket;
     	if (checkIfPlayerExists(name)){
     		currentticket = getBusinessPlayerTicket(name,businessname);
     		int businessid = TBusiness.getBusinessId(businessname);
     		if (businessid > 0){
     			amount = currentticket - amount;
     			if (amount < 0)
     				throw new CommandException("You can't remove "+ amount +" ticket from " + name + " ticket account! He only haves" + currentticket + "");
     			return dbm.update("UPDATE tickets SET tickets=" + amount + ", business_id="+ businessid +" WHERE user_id = '" + name + "'");
     		}
     	}
     	return false;
     }
     public boolean givePlayerTicket(String name, Integer amount,String businessname){
     	int currentticket;
     	if (checkIfPlayerExists(name)){
     		currentticket = getBusinessPlayerTicket(name,businessname);
     		int businessid = TBusiness.getBusinessId(businessname);
     		if (businessid > 0){
     			amount = currentticket + amount;
    			return dbm.update("UPDATE tickets SET tickets=" + amount + ", business_id="+ businessid +"  WHERE user_id = '" + name + "'");
     		}
     	}
     	return false;
     }
     public int getPlayerId(String name){
     	if (!checkIfPlayerExists(name))
     		return -1;
     	ResultSet result = dbm.query("SELECT id FROM players WHERE name = '" + name + "'");
     	try {
 			if (result != null  && result.next()){
 				return result.getInt("id");
 			}
 		} catch (SQLException e) {
 			TLogger.warning(e.getMessage());
 		}
     	return -2;
     	
     }
     public boolean isPlayer(CommandSender sender) {
         return sender != null && sender instanceof Player;
     }
 }
