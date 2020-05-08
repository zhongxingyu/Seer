 package com.psychobit.hardcore;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.dthielke.herochat.Channel;
 import com.dthielke.herochat.Chatter;
 import com.dthielke.herochat.ChatterManager;
 import com.dthielke.herochat.Herochat;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.trc202.CombatTag.CombatTag;
 import com.trc202.CombatTagApi.CombatTagApi;
 
 /**
  * Hardcore
  * 
  * Official Hardcore plugin for the HCSMP community
  * Features include:
  *  - Ban upon death
  *  - Database integration
  *  - Last words
  *  - Survival time tracking 
  *  
  *  @author psychobit
  */
 public class Hardcore extends JavaPlugin implements Listener
 {
 	/**
 	 * Database access object
 	 */
 	private HardcoreDAO _dao;
 	
 	/**
 	 * Players last words before death
 	 */
 	private HashMap<String,String> _lastWords;
 	
 	/**
 	 * Player's time spent in protected area
 	 */
 	private HashMap<String,Integer> _protected;
 	
 	/**
 	 * Worldguard reference
 	 */
 	private WorldGuardPlugin _worldguard;
 	
 	/**
 	 * Herochat reference
 	 */
 	private Herochat _herochat;
 	
 	/**
 	 * Set up the environment
 	 */
 	public Hardcore()
 	{
 		this._dao = new HardcoreDAO();
 		this._lastWords = new HashMap<String,String>();
 		this._protected = new HashMap<String,Integer>();
 	}
 	
 	/**
 	 * Plugin enabled
 	 */
 	public void onEnable()
 	{
 		// Check the configuration
 		FileConfiguration config = this.getConfig();
 		if ( !config.contains( "hostname" ) ) config.set( "hostname", "localhost" );
 		if ( !config.contains( "database" ) ) config.set( "database", "hardcore" );
 		if ( !config.contains( "username" ) ) config.set( "username", "root" );
 		if ( !config.contains( "password" ) ) config.set( "password", "" );
 		this.saveConfig();
 		
 		// Connect to the database
 		this._dao.connect( config.getString( "hostname", "localhost" ), 
 				config.getString( "database", "hardcore" ), 
 				config.getString( "username", "root" ), 
 				config.getString( "password", "" ),
 				this.getDataFolder() );
 		
 		// Register events
 		this.getServer().getPluginManager().registerEvents( this, this );
 		
 		// Check for worldguard
 		Plugin p = this.getServer().getPluginManager().getPlugin( "WorldGuard" );
 		if ( p != null && p instanceof WorldGuardPlugin )
 		{
 			System.out.println( "[Hardcore] Found WorldGuard" );
 			this._worldguard= ( WorldGuardPlugin ) p;
 		}
 		
 		// Check for herochat
 		p = this.getServer().getPluginManager().getPlugin( "Herochat" );
 		if ( p != null && p instanceof Herochat )
 		{
 			System.out.println( "[Hardcore] Found Herochat" );
 			this._herochat = ( Herochat ) p;
 		}
 		
 		// Start the thread to check for protection
         final Hardcore plugin = this;
         this.getServer().getScheduler().scheduleAsyncRepeatingTask( this, new Runnable() {
             public void run() { plugin.trackProtectedTime(); }
         }, 20L, 20L ); // Update every second
         
         // Load last words
         this.loadLastWords();
 	}
 	
 	/**
 	 * Plugin disabled
 	 */
 	public void onDisable()
 	{
 		// Loop through all players
 		Player[] players = this.getServer().getOnlinePlayers();
 		for ( Player player : players )
 		{
 			// Ignore ops and admins
 			if ( player.isOp() ) continue;
 			if ( player.hasPermission("hardcore.immune" ) ) continue;
 			
 			// Calculate survival time and record it 
 			String playerName = player.getName();
 			double time = this._protected.containsKey( playerName ) ? this._protected.get( playerName ) : 0;
 			this._dao.addSurvivalTime( playerName, time );
 		}
 		
 		// Disconnect from the database and kill the protection check thread
 		this._dao.disconnect( this.getDataFolder() );
 		this.getServer().getScheduler().cancelTasks( this );
 		
 		// Save last words
 		this.saveLastWords();
 	}
 	
 	/**
 	 * Kick dead players
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.HIGH )
 	public void onPlayerLoginDeathCheck( PlayerLoginEvent e )
 	{
 		// Alias some stuff 
 		Player player = e.getPlayer();
 		String playerName = player.getName();
 		String reviveDate = this.getNextUnbanDate();
 		
 		// Kick out dead players
 		if ( this._dao.playerDead( playerName ) )
 		{
 			e.setKickMessage( "You are dead! Come back on " + reviveDate + "! - http://hcsmp.com" );
 			e.setResult( Result.KICK_OTHER );
 			return;
 		}
 	}
 	
 	/**
 	 * Add the player to the database if they aren't in it
 	 * Update their online time
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.NORMAL )
 	public void onPlayerLogin( PlayerLoginEvent e )
 	{
 		// Alias some stuff 
 		Player player = e.getPlayer();
 		String playerName = player.getName();
 		
 		// Update new players
 		if ( !e.getResult().equals( Result.ALLOWED) ) return;
 		boolean newPlayer = this._dao.checkPlayer( playerName );
 		if ( newPlayer ) this.getServer().broadcastMessage( ChatColor.GOLD + playerName + " is new this month! Say hi!" );
 	}
 	
 	/**
 	 * Update the player's survival time on logout
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.HIGH )
 	public void onPlayerLogout( PlayerQuitEvent e )
 	{
 		// Alias
 		Player player = e.getPlayer();
 		
 		// Ignore ops and admins
 		if ( player.isOp() ) return;
 		if ( player.hasPermission("hardcore.immune" ) ) return;
 		
 		String playerName = player.getName();
 		double time = this._protected.containsKey( playerName ) ? this._protected.get( playerName ) : 0;
 		this._dao.addSurvivalTime( player.getName(), time );
 		this._protected.put( playerName, 0 );
 	}
 	
 	
 	/**
 	 * Insert the death data into the database and update their status
 	 * Start a thread to kick them
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.HIGH )
 	public void onPlayerDeath( EntityDeathEvent e )
 	{
 		// Check that it was a player death
 		Entity entity = e.getEntity();
 		Player player = null;
 		if ( !( entity instanceof Player ) ) return;
 		player = ( Player ) e.getEntity();
 		PlayerDeathEvent de = ( PlayerDeathEvent ) e;
 		
 		// Check for an NPC
 		String npcPlayerName = "ERROR";
 		//boolean isNPC = ((CraftEntity)entity).getHandle() instanceof NPCEntity;
 		boolean isNPC = false;
 		CombatTagApi combatApi;
 		if (getServer().getPluginManager().getPlugin("CombatTag") != null) {
 			combatApi = new CombatTagApi((CombatTag) getServer().getPluginManager().getPlugin("CombatTag"));
 			if (combatApi != null) {
 				 isNPC = combatApi.isNPC(entity);
 			}
 		}
 		if ( isNPC )
 		{
 			Plugin p = this.getServer().getPluginManager().getPlugin( "CombatTag" );
 			if ( p != null && p instanceof CombatTag )
 			{
 				CombatTag ct = (CombatTag) p;
 				npcPlayerName = ct.getPlayerName( entity );
 			}
 		}
 		
 		// Ignore OPs and Admins
 		if ( !isNPC )
 		{
 			if ( player.isOp() ) return;
 			if ( player.hasPermission( "hardcore.immune" ) ) return;
 		}
 		
 		// Gather info
 		String playerName = isNPC ? npcPlayerName : player.getName();
 		Location location = player.getLocation();
 		Player killer = null;
 		Player witness = null;
         if( player.getLastDamageCause() instanceof EntityDamageByEntityEvent )
         {
             EntityDamageByEntityEvent nEvent = ( EntityDamageByEntityEvent ) player.getLastDamageCause();
             if( nEvent.getDamager() instanceof Player ) killer = ( Player ) nEvent.getDamager();
             if( nEvent.getDamager() instanceof Arrow )
             {
             	Arrow arrow = ( Arrow ) nEvent.getDamager();
             	if ( arrow.getShooter() instanceof Player ) killer = ( Player ) arrow.getShooter();
             }
         }
         if ( killer != null && killer.isOp() ) return;
 		double maxdist = this.getConfig().getDouble( "WitnessDistance", 50.0 );
 		Iterator<Entity> nearby = player.getNearbyEntities( maxdist, maxdist, maxdist ).iterator();
 		while( nearby.hasNext() )
 		{
 			Entity ent = nearby.next();
 			if ( !( ent instanceof Player ) ) continue;
 			Player p = ( Player ) ent;
 			if ( p.isOp() ) continue; 			// Ignore ops
 			if ( p.equals( player ) ) continue; // Ignore the player
 			if ( p.equals( killer ) ) continue; // Ignore the killer
 			double dist = p.getLocation().distance( player.getLocation() );
 			p.sendMessage( player.getName() + " was killed nearby!" );
 			if ( dist <= maxdist )
 			{
 				maxdist = dist;
 				witness = p;
 			}
 		}
 		
 		// Insert the death record
 		this._dao.killPlayer( playerName,
 				( killer == null ? de.getDeathMessage() : "Slain by Player" ),
 				( killer == null ? "" : killer.getName() ), 
 				( killer == null ? "" : killer.getItemInHand().getType().toString() ),
 				( witness == null ? "" : witness.getName() ), 
 				location.getBlockX(), 
 				location.getBlockY(), 
 				location.getBlockZ(), 
 				location.getWorld().getName(), 
 				( this._lastWords.containsKey( playerName ) ? this._lastWords.get( playerName ) : "" ),
 				this.getSurvivalTime( playerName )
 				
 		);
 		
 		// Tell console!
 		System.out.println( "[Hardcore] " + ChatColor.WHITE + playerName + " died: " + de.getDeathMessage() );
 		
 		// Start a new thread to kick the player on the next second
 		final Player target = player;
         this.getServer().getScheduler().scheduleSyncDelayedTask( this , new Runnable() {
             public void run()
             {
             	if ( target != null ) target.kickPlayer( "You have died!" );
             }
         }, 20L );
 	}
 	
 	
 	/**
 	 * Set a player's last words
 	 * @param e
 	 */
 	@EventHandler
 	public void playerChat( AsyncPlayerChatEvent e )
 	{
 		// Alias
 		Player player = e.getPlayer();
 		
 		// Make sure the server is running Herochat
 		if ( this._herochat != null )
 		{
 			try {
 				ChatterManager chatterManager = Herochat.getChatterManager();
 				Chatter sender = chatterManager.getChatter( player );
 				Channel channel = sender.getActiveChannel();
 				if ( !channel.getName().toLowerCase().equals( "global" ) ) return;
 			} catch( Exception ex ) {
 				System.out.println( "ERROR" );
 				return; // Ignore it
 			}
 		}
 		
 		// Log last words
 		this._lastWords.put( e.getPlayer().getName(), e.getMessage() );
 	}
 	
 	/**
      * Get the next unban date
      * @return Date at which everyone will be unbanned
      */
     public String getNextUnbanDate()
     {
         Calendar c = Calendar.getInstance();
         c.set( Calendar.DAY_OF_MONTH, 1 );
         c.set( Calendar.MONTH, c.get( Calendar.MONTH ) + 1 );
         c.set( Calendar.HOUR_OF_DAY, 0 );
         c.set( Calendar.MINUTE, 0 );
         c.set( Calendar.SECOND, 0 );
         return c.getTime().toString();
     }
     
     /**
      * Parse a command
      * @param sender User who sent the command
      * @param command Command that was send
      * @param label Command label
      * @param args Arguments
      */
     public boolean onCommand( CommandSender sender, Command command, String label, String args[] )
     {
     	// No command? Display the help
         if( args.length == 0 )
         {
             this.displayHelp( sender, command.getName() );
             return true;
         }
         
         // Slay a player
         if( args[0].equalsIgnoreCase( "slay" ) )
         {
         	// Check permissions
         	if ( !sender.hasPermission( "hardcore.slay" ) )
 			{
 				sender.sendMessage( ChatColor.RED + "You don't have permission to do that!" );
 				return true;
 			}
 			
 			// Check arguments
             if( args.length < 2 )
             {
                 sender.sendMessage( "You must specify a target to slay!" );
                 return true;
             }
             
             // Find the target
             final Player target = this.getServer().getPlayer( args[1] );
             if( target == null )
             {
                 sender.sendMessage( "Player not found!" );
                 return true;
             }
         	// Target found. Kill them
             String playerName = target.getName();
             target.sendMessage( "You have been slain by an admin!" );
             this._dao.killPlayer( playerName, playerName + " was slain by admin", sender.getName(), "", "", 0, 0, 0, "world", ( this._lastWords.containsKey( playerName ) ? this._lastWords.get( playerName ) : "" ), this.getSurvivalTime( playerName ) );
             sender.sendMessage( "Player added to the list of dead!" );
             this.getServer().getScheduler().scheduleSyncDelayedTask( this, new Runnable(){
 				@Override
 				public void run()
 				{
 					System.out.println( target.getName() );
 					target.kickPlayer( "Slain by admin" );
 				}
             	
             }, 20L );
             return true;
         }
         
         // Resurrect command
         if( args[0].equalsIgnoreCase( "res" ) )
         {
         	// Check permissions
         	if ( !sender.hasPermission( "hardcore.res" ) )
 			{
 				sender.sendMessage( ChatColor.RED + "You don't have permission to do that!" );
 				return true;
 			}
 			
 			// Check arguments
             if( args.length < 2 )
             {
             	sender.sendMessage( "You must specify a target to resurrect!" );
                 return true;
             }
                         
             // Check if they are dead
             if( !this._dao.playerActive( args[1] ) )
             {
             	sender.sendMessage( args[1] + " hasn't played here this month!" );
                 return true;
             }
             if( !this._dao.playerDead( args[1] ) )
             {
                 sender.sendMessage( args[1] + " is not dead!");
                 return true;
             }
         	
             // Resurrect them
             String adminName = "Console";
             if ( sender instanceof Player ) adminName = sender.getName();
             this._dao.revivePlayer( args[1], adminName );
             sender.sendMessage( args[1] + " was removed from the list of dead!" );
             return true;
         }
         
         // Get player info
         if( args[0].equalsIgnoreCase( "info" ) )
         {
         	// Check arguments
             if( args.length < 2 )
             {
             	sender.sendMessage( "You must specify a player!" );
                 return true;
             }
             
             // Check if they are dead
             if( !this._dao.playerActive( args[1] ) )
             {
             	sender.sendMessage( args[1] + " hasn't played here this month!" );
                 return true;
             }
             
             // Calculate their survival time
             double survivalTime = this.getSurvivalTime( args[1] );
             String survivalTimeUnits = "Seconds";
             if ( survivalTime >= 3600 )
             {
             	survivalTime = survivalTime / 3600;
             	survivalTimeUnits = "Hours";
             }else if ( survivalTime >= 60 ) {
             	survivalTime = survivalTime / 60;
             	survivalTimeUnits = "Minutes";
             }
             if( this._dao.playerDead( args[1] ) )
             {
             	sender.sendMessage( args[1] + " is dead!" );
             	sender.sendMessage( "Survived " + survivalTime + " " + survivalTimeUnits );
                 return true;
             }
             sender.sendMessage( args[1] + " is still alive!" );
             sender.sendMessage( "Survived " + survivalTime + " " + survivalTimeUnits );
             return true;
         }
         
         // Default to displaying help
         this.displayHelp( sender, command.getName() );
         return true;
     }
 
     /**
      * Display general help to the sender
      * @param sender User to send to
      */
     private void displayHelp( CommandSender sender, String command )
     {
         sender.sendMessage( "[" + ChatColor.RED + "Hardcore" + ChatColor.WHITE + "] Usage:" );
         sender.sendMessage( "/" + command + " info <player> - Check if a player is alive or dead" );
         if ( sender.hasPermission( "hardcore.res" ) ) sender.sendMessage( "/" + command + " res <player> - Remove a player from the dead list" );
         if ( sender.hasPermission( "hardcore.slay" ) ) sender.sendMessage( "/" + command + " slay <player> - Kill a player" );
     }
     
     /**
      * Track the amount of time spent in a protected zone
      */
     private void trackProtectedTime()
     {
     	// Make sure the server is running WorldGuard 
     	if ( this._worldguard == null ) return;
 		
     	// Loop through online players
 		Player[] players = this.getServer().getOnlinePlayers();
 		for( Player player : players )
 		{
 			// Ignore ops and admins
 			if ( player.isOp() ) continue;
 			if ( player.hasPermission("hardcore.immune" ) ) continue;
 			
 	    	
 			// Check if they are in NoPvP or Invincible regions
 			boolean inNoPvP = !this._worldguard.getRegionManager( player.getWorld() ).getApplicableRegions( player.getLocation() ).allows( DefaultFlag.PVP );
 			boolean inInvincible = this._worldguard.getRegionManager( player.getWorld() ).getApplicableRegions( player.getLocation() ).allows( DefaultFlag.INVINCIBILITY );
 				
 			// Update the time they've spent in a protected zone
 			if ( inNoPvP || inInvincible )
 			{
 				String playerName = player.getName();
 				int time = this._protected.containsKey( playerName ) ? this._protected.get( playerName ) : 0;
 				this._protected.put( playerName, time + 1 );
 			}
 		}
     }
     
     /**
 	 * Save last words to disk 
 	 */
 	public void saveLastWords()
 	{
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( this.getDataFolder() + "/lastwords.dat" ) );
 			oos.writeObject( this._lastWords );
 			oos.flush();
 			oos.close();
 		} catch ( Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Load last words from disk
 	 */
 	@SuppressWarnings("unchecked")
 	public void loadLastWords()
 	{
 		try {
 			ObjectInputStream ois = new ObjectInputStream( new FileInputStream( this.getDataFolder() + "/lastwords.dat" ) );
 			this._lastWords = ( HashMap<String,String> ) ois.readObject();
 			ois.close();
 		} catch ( FileNotFoundException e ) { // Ignore it
 		} catch ( Exception e ) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Poison players from cave spiders  
 	 * @param e
 	 */
 	@EventHandler( priority=EventPriority.NORMAL, ignoreCancelled=true )
 	public void onPlayerDamage( EntityDamageEvent e )
 	{
 		if ( !( e instanceof EntityDamageByEntityEvent ) ) return;
 		if ( !( e.getEntity() instanceof Player ) ) return;
 		EntityDamageByEntityEvent e2 = ( EntityDamageByEntityEvent ) e;
 		if ( !( e2.getDamager() instanceof CaveSpider ) ) return;
 		Player player = ( Player ) e.getEntity();
 		new PotionEffect( PotionEffectType.POISON, 20*15, 0 ).apply( player );
 	}
 	
 	
 	/**
 	 * Mark worldguard and herochat as enabled if they are enabled
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.LOW )
 	public void onPluginLoad( PluginEnableEvent e )
 	{
 		Plugin p = e.getPlugin();
 		if ( p.getDescription().getName().equals( "WorldGuard" ) && p instanceof WorldGuardPlugin )
 		{
 			System.out.println( "[Hardcore] Found WorldGuard!" );
 			this._worldguard = ( WorldGuardPlugin ) p;
 			return;
 		}
 		if ( p.getDescription().getName().equals( "Herochat" ) && p instanceof Herochat )
 		{
 			System.out.println( "[Hardcore] Found Herochat!" );
 			this._herochat = ( Herochat ) p;
 			return;
 		}
 	}
 	
 	/**
 	 * Mark worldguard and herochat as disabled if they are disabled
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.LOW )
 	public void onPluginunload( PluginDisableEvent e )
 	{
 		Plugin p = e.getPlugin(); 
 		if ( p.getDescription().getName().equals( "Herochat" ) && p instanceof Herochat )
 		{
 			System.out.println( "[Hardcore] Herochat disabled!" );
 			this._herochat = null;
 			return;
 		}
 		if ( p.getDescription().getName().equals( "WorldGuard" ) && p instanceof WorldGuardPlugin )
 		{
 			System.out.println( "[Hardcore] Worldguard disabled!" );
 			this._worldguard = null;
 			return;
 		}
 	}
 	
 	/**
 	 * Get survival time for a player
 	 * @param playerName
 	 * @return
 	 */
 	private int getSurvivalTime( String playerName )
 	{
 		int protectedTime = 0;
         Player player = this.getServer().getPlayer( playerName );
        if ( player != null ) protectedTime = this._protected.containsKey( player.getName() ) ? this._protected.get( player.getName() ) : 0;
        return this._dao.survivalTime( playerName ) - protectedTime;
 	}
 }
