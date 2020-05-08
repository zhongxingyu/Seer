 // Copyright (C) 2011 by Danny de Jong
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files (the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions:
 // 
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 // 
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 
 package com.dannycrafts.myTitles;
 
 import java.io.*;
 import java.sql.SQLException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.dannycrafts.myTitles.Title.InvalidNameException;
 
 public class Plugin extends JavaPlugin {
 	
 	public String usagePluginId = "";
 	
 	private PlayerListener playerListener = new PlayerListener( this );
 	protected MyTitles mainInterface = getInterface( "" );
 	
 	protected void copyFile( InputStream original, OutputStream copy ) throws IOException, FileNotFoundException
 	{		
 		byte[] buffer = new byte[1024];
 		while ( true )
 		{
 			int read = original.read( buffer );
			copy.write( buffer, 0, read );
			if ( read < 1024 )
 				break;
 		}
 		
 		copy.flush();
 		copy.close();
 		original.close();
 	}
 	
 	protected String format( String message, String varName, String value )
 	{
 		return message.replace( "$" + varName + "$", value );
 	}
 	
 	public MyTitles getInterface( String usagePluginId )
 	{
 		return new MyTitles( this, usagePluginId );
 	}
 	
 	public boolean onCommand( CommandSender sender, Command cmd, String commandLabel, String[] args )
 	{
 		org.bukkit.entity.Player player = null;
 		if ( sender instanceof org.bukkit.entity.Player ) player = (org.bukkit.entity.Player)sender;
 		
 		if ( args.length == 0 )
 		{
 			if ( sender.hasPermission( "mytitles.player.list" ) )
 			{
 				try
 				{
 					Player myTitlesPlayer = mainInterface.getPlayer( player );
 					Title[] titles = myTitlesPlayer.getOwnedTitles();
 					if ( titles.length != 0 )
 					{
 						for ( int i = 0; i < titles.length; i++ )
 						{
 							Title.Info titleInfo = titles[i].getInfo();
 							sender.sendMessage( "#" + ( i + 1 ) + ": " + titleInfo.name + ": \"" + myTitlesPlayer.getDisplayName( titleInfo.affixes ) + "\"" );
 						}
 					}
 					else
 						sendMessage( sender, Messages.noTitles );
 				}
 				catch ( Player.DoesntExistException e )
 				{
 					printError( e );
 					sendMessage( sender, Messages.internalError );
 				}
 				catch ( SQLException e )
 				{
 					printSqlError( e );
 					sendMessage( sender, Messages.internalError );
 				}
 			}
 			else if ( player == null )
 				sender.sendMessage( this.getDescription().getName() + " v" + this.getDescription().getVersion() );
 			else
 				sendMessage( sender, Messages.noPermissions );
 		}
 		else if ( args.length > 0 )
 		{
 			if ( args[0].equalsIgnoreCase( "use" ) )
 			{
 				if ( sender.hasPermission( "mytitles.player.use" ) )
 				{
 					if ( args.length >= 2 )
 					{
 						try
 						{
 							int affected = Database.update( "UPDATE " + Database.formatTableName( "players" ) + " SET title_id = ( SELECT id FROM " + Database.formatTableName( "titles" ) + " WHERE name = '" + args[1] + "' ) WHERE name = '" + player.getName() + "';" );
 							if ( affected == 0 )
 								sendMessage( sender, format( Messages.noTitle, "title_name", args[1] ) );
 							else
 								sendMessage( sender, format( format( Messages.titleUse, "display_name",  mainInterface.getPlayer( player ).getDisplayName() ), "title_name", args[1] ) );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							printError( e );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"use\" title_name" );
 				}
 				else if ( player == null )
 					sendMessage( sender, Messages.commandPlayerOnly );
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "clear" ) )
 			{
 				if ( sender.hasPermission( "mytitles.player.use" ) )
 				{
 					try
 					{
 						Database.update( "UPDATE " + Database.formatTableName( "players" ) + " SET title_id = 0 WHERE name = '" + player.getName() + "';" );
 						player.sendMessage( format( Messages.titleClear, "name", mainInterface.getPlayer( player.getName() ).getDisplayName() ) );
 					}
 					catch ( SQLException e )
 					{
 						printSqlError( e );
 						sendMessage( sender, Messages.internalError );
 					}
 					catch ( Exception e )
 					{
 						printError( e );
 						sendMessage( sender, Messages.internalError );
 					}
 				}
 				else if ( player == null )
 					sendMessage( sender, Messages.commandPlayerOnly );
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "usenr" ) )
 			{
 				if ( sender.hasPermission( "mytitles.player.use" ) )
 				{
 					if ( args.length >= 2 )
 					{
 						try
 						{
 							int selectNr = Integer.parseInt( args[1] );
 							Title[] titles = mainInterface.getPlayer( player ).getOwnedTitles();
 							if ( selectNr > 0 && selectNr <= titles.length )
 							{
 								Title selectedTitle = titles[selectNr - 1];
 								
 								int affected = Database.update( "UPDATE " + Database.formatTableName( "players" ) + " SET title_id = " + selectedTitle.id + " WHERE name = '" + player.getName() + "';" );
 								if ( affected == 0 )
 									sendMessage( sender, format( Messages.noTitle, "title_name", selectedTitle.getName() ) );
 								else
 									sendMessage( sender, format( format( Messages.titleUse, "display_name",  mainInterface.getPlayer( player ).getDisplayName() ), "title_name", selectedTitle.getName() ) );
 							}
 							else
 								player.sendMessage( "Number is out of range." );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							printError( e );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"usenr\" title_number" );
 				}
 				else if ( player == null )
 					sendMessage( sender, Messages.commandPlayerOnly );
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "register" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.register" ) )
 				{
 					if ( args.length >= 3 )
 					{
 						if ( args[2].contains( ":" ) )
 						{
 							String prefix = args[2].substring( 0, args[2].indexOf( ':' ) );
 							String postfix = args[2].substring( prefix.length() + 1 );
 								
 							try
 							{
 								mainInterface.registerTitle( args[1], prefix, postfix );
 								sendMessage( sender, format( Messages.titleRegistered, "title_name", args[1] ) );
 							}
 							catch ( Title.AlreadyExistsException e )
 							{
 								sendMessage( sender, format( Messages.titleExists, "title_name", args[1] ) );
 							}
 							catch (InvalidNameException e)
 							{
 								sendMessage( sender, format( Messages.invalidTitleName, "name", args[1] ) );
 							}
 							catch ( SQLException e )
 							{
 								printSqlError( e );
 								sendMessage( sender, Messages.internalError );
 							}
 						}
 						else
 							sender.sendMessage( commandLabel + " \"register\" title_name prefix \":\" postfix" );
 					}
 					else
 						sender.sendMessage( commandLabel + " \"register\" title_name prefix \":\" postfix" );
 				}
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "unregister" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.register" ) )
 				{
 					if ( args.length >= 2 )
 					{
 						try
 						{
 							mainInterface.unregisterTitle( args[1] );
 							sendMessage( sender, format( Messages.titleUnregistered, "title_name", args[1] ) );
 						}
 						catch ( Title.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.titleNotExists, "title_name", args[1] ) );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"unregister\" title_name" );
 				}
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "give" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.distribute" ) )
 				{
 					if ( args.length >= 3 )
 					{
 						try
 						{
 							mainInterface.getPlayer( args[1] ).giveTitle( mainInterface.getTitle( args[2] ) );
 
 								
 							sender.sendMessage( format( format( Messages.titleGiven, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.playerNotExists, "player_name", args[1] ) );
 						}
 						catch ( Title.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.titleNotExists, "title_name", args[2] ) );
 						}
 						catch ( Player.AlreadyOwnsTitleException e )
 						{
 							sendMessage( sender, format( format( Messages.playerOwnsTitle, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"give\" player_name title_name" );
 				}
 				else
 					sendMessage( sender, Messages.noPermissions );
 			}
 			
 			else if ( args[0].equalsIgnoreCase( "take" ) )
 			{
 				if ( player == null || sender.hasPermission( "mytitles.admin.distribute" ) )
 				{
 					if ( args.length >= 3 )
 					{ 
 						try
 						{
 							mainInterface.getPlayer( args[1] ).takeTitle( mainInterface.getTitle( args[2] ) );
 							
 							sender.sendMessage( format( format( Messages.titleTaken, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( Player.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.playerNotExists, "player_name", args[1] ) );
 						}
 						catch ( Title.DoesntExistException e )
 						{
 							sendMessage( sender, format( Messages.titleNotExists, "title_name", args[2] ) );
 						}
 						catch ( Player.DoesntOwnTitleException e )
 						{
 							sendMessage( sender, format( format( Messages.playerNotOwnsTitle, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( SQLException e )
 						{
 							printSqlError( e );
 							sendMessage( sender, Messages.internalError );
 						}
 					}
 					else
 						sender.sendMessage( commandLabel + " \"take\" player_name title_name" );
 				}
 			}
 			
 			else
 				sender.sendMessage( "mt \"help\"" );
 		}
 		
 		return true;
 	}
 	
 	public void onDisable() {
 		
 		try {
 			
 			Database.disconnect();
 			
 			print( "Gracefully disabled." );
 		}
 		catch ( Exception e ) {
 			
 			printError( e );
 		}
 	}
 	
 	public void onEnable() {
 		
 		try {
 			
 			// Load database drivers
 			Class.forName( "com.mysql.jdbc.Driver" );
 			Class.forName( "org.h2.Driver" );
 			
 			// Make sure that the data folder exists:
 			if ( !getDataFolder().exists() )
 				getDataFolder().mkdir();
 			
 			// Install config.yml
 			File configDestination = new File( getDataFolder() + "/config.yml" );
 			if ( !configDestination.exists() )
 			{
 				InputStream configResource = getClass().getResourceAsStream( "config.yml" );
 				copyFile( configResource, new FileOutputStream( configDestination ) );
 			}
 
 			// Load config.yml
 			Configuration config = new Configuration( new File( this.getDataFolder() + "/config.yml" ) );
 			config.load();
 
 			String dbType = config.getString( "database_type", "h2" );
 			String dbHost = config.getString( "database_host", "localhost" );
 			String dbPort = config.getString( "database_port", "3306" );
 			String dbFile = config.getString( "database_file", "" );
 			String dbUsername = config.getString( "database_username", "root" );
 			String dbPassword = config.getString( "database_password", "" );
 			String dbDatabase = config.getString( "database_database", "my_titles" );
 			String dbTablePrefix = config.getString( "database_table_prefix", "" );
 
 			String defaultPrefix = config.getString( "default_prefix", "" );
 			String defaultSuffix = config.getString( "default_suffix", "" );
 			Settings.defaultAffixes = new Title.Affixes( defaultPrefix, defaultSuffix );
 
 			Messages.commandPlayerOnly = config.getString( "message_command_player_only", null );
 			Messages.internalError = config.getString( "message_internal_error", null );
 			Messages.invalidTitleName = config.getString( "message_invalid_title_name", null );
 			Messages.noPermissions = config.getString( "message_no_permissions", null );
 			Messages.noTitle = config.getString( "message_no_title", null );
 			Messages.noTitles = config.getString( "message_no_titles", null );
 			Messages.playerOwnsTitle = config.getString( "message_player_owns_title", null );
 			Messages.playerNotExists = config.getString( "message_player_not_exists", null );
 			Messages.playerNotOwnsTitle = config.getString( "message_player_not_owns_title", null );
 			Messages.titleClear = config.getString( "message_title_clear", null );
 			Messages.titleExists = config.getString( "message_title_exists", null );
 			Messages.titleGiven = config.getString( "message_title_given", null );
 			Messages.titleNotExists = config.getString( "message_title_not_exists", null );
 			Messages.titleRegistered = config.getString( "message_title_registered", null );
 			Messages.titleTaken = config.getString( "message_title_taken", null );
 			Messages.titleUnregistered = config.getString( "message_title_unregistered", null );
 			Messages.titleUse = config.getString( "message_title_use", null );
 			
 			// Connect to database:
 			if ( dbType.equals("h2") )
 			{
 				if ( dbFile == null || dbFile.equals( "" ) )
 					dbFile = this.getDataFolder() + "/data";
 				Database.connectFile( dbType, dbFile, dbUsername, dbPassword, dbTablePrefix );
 			}
 			else if ( dbType.equals("mysql") )
 				Database.connectHost( dbType, dbHost, dbPort, dbDatabase, dbUsername, dbPassword, dbTablePrefix );
 			
 			installDatabase();
 			
 			// Register events:
 			PluginManager pluginManager = this.getServer().getPluginManager();
 			pluginManager.registerEvent( Type.PLAYER_JOIN, playerListener, Priority.Normal, this );
 			pluginManager.registerEvent( Type.PLAYER_QUIT, playerListener, Priority.Normal, this );
 			pluginManager.registerEvent( Type.PLAYER_CHAT, playerListener, Priority.Highest, this );
 			
 			print( "Enabled." );
 		}
 		catch ( SQLException e )
 		{
 			printSqlError( e );
 			print( "Did you configure the database connection details correctly?" );
 		}
 		catch ( Exception e ) {
 			
 			printError( e );
 		}
 	}
 	
 	public void installDatabase() throws SQLException
 	{
 		try
 		{
 			Database.update( "CREATE TABLE " + Database.formatTableName( "collections" ) + " (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"player_id bigint unsigned NOT NULL," +
 					"title_id bigint unsigned DEFAULT NULL," +
 					"title_variation_id BIGINT UNSIGNED," +
 					"CONSTRAINT key_collections PRIMARY KEY( id )," +
 					"CONSTRAINT uni_collections UNIQUE (player_id,title_id)" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != Database.Codes.tableAlreadyExists )
 				throw e;
 		}
 		
 		try
 		{
 			Database.update( "CREATE TABLE " + Database.formatTableName( "players" ) + " (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"name varchar(16) NOT NULL," +
 					"title_id bigint unsigned NOT NULL DEFAULT 0," +
 					"CONSTRAINT key_players PRIMARY KEY( id )," +
 					"CONSTRAINT uni_players UNIQUE (name)" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != Database.Codes.tableAlreadyExists )
 				throw e;
 		}
 		
 		try
 		{			
 			Database.update( "CREATE TABLE " + Database.formatTableName( "titles" ) + " (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"plugin_id varchar(32) NOT NULL DEFAULT ''," +
 					"name varchar(16) NOT NULL," +
 					"prefix varchar(32) DEFAULT NULL," +
 					"suffix varchar(32) DEFAULT NULL," +
 					"CONSTRAINT key_titles PRIMARY KEY( id )," +
 					"CONSTRAINT uni_titles UNIQUE(name)" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != Database.Codes.tableAlreadyExists )
 				throw e;
 		}
 		
 		try
 		{
 			Database.update( "CREATE TABLE " + Database.formatTableName( "title_variations" ) + " (" +
 					"id bigint unsigned NOT NULL AUTO_INCREMENT," +
 					"title_id bigint unsigned NOT NULL," +
 					"name VARCHAR(16) NOT NULL," +
 					"prefix VARCHAR(32)," +
 					"suffix VARCHAR(32)," +
 					"CONSTRAINT key_title_variations PRIMARY KEY( id )," +
 					"CONSTRAINT uni_title_variation UNIQUE( title_id, name )" +
 				");"
 			);
 		}
 		catch ( SQLException e )
 		{
 			if ( e.getErrorCode() != Database.Codes.tableAlreadyExists )
 				throw e;
 		}
 	}
 	
 	public void onLoad() {
 		
 		print( "v" + this.getDescription().getVersion() );
 	}
 	
 	public void print( String message ) {
 		
 		System.out.println( "[" + this.getDescription().getName() + "] " + message );
 	}
 	
 	public void printError( Exception e ) {
 		
 		print( "Unexpected error occurred." );
 		e.printStackTrace();
 	}
 	
 	public void printSqlError( SQLException e ) {
 		
 		print( "Unexpected SQL error occurred (" + e.getErrorCode() + ")." );
 		e.printStackTrace();
 	}
 	
 	protected void sendMessage( CommandSender receiver, String message )
 	{
 		if ( message != null )
 			receiver.sendMessage( message );
 	}
 	
 	protected static class Messages
 	{
 		public static String commandPlayerOnly;
 		public static String internalError;
 		public static String invalidTitleName;
 		public static String noPermissions;
 		public static String noTitle;
 		public static String noTitles;
 		public static String playerOwnsTitle;
 		public static String playerNotExists;
 		public static String playerNotOwnsTitle;
 		public static String titleClear;
 		public static String titleExists;
 		public static String titleGiven;
 		public static String titleNotExists;
 		public static String titleRegistered;
 		public static String titleTaken;
 		public static String titleUnregistered;
 		public static String titleUse;
 	}
 	
 	protected static class Settings
 	{
 		public static Title.Affixes defaultAffixes;
 	}
 }
