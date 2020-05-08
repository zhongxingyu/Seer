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
 
 import com.dannycrafts.myTitles.database.*;
 
 import java.io.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 public class Plugin extends JavaPlugin
 {	
 	public static String pluginName;
 	
 	private PlayerListener playerListener = new PlayerListener( this );
 	protected MyTitles mainInterface = getInterface( "" );
 
 	protected static com.dannycrafts.myTitles.database.Database playerDatabase;
 	protected static com.dannycrafts.myTitles.database.Database titleDatabase;
 	protected static com.dannycrafts.myTitles.database.Database collectionDatabase;
 	protected static com.dannycrafts.myTitles.database.Database titleVariationDatabase;
 	
 	protected void copyFile( InputStream original, OutputStream copy ) throws IOException, FileNotFoundException
 	{		
 		byte[] buffer = new byte[1024];
 		while ( true )
 		{
 			int read = original.read( buffer );
 			
 			if ( read == -1 )
 				break;
 			
 			copy.write( buffer, 0, read );
 		}
 		
 		copy.flush();
 		copy.close();
 		original.close();
 	}
 	
 	protected static String formatParameters( String message, String varName, String value )
 	{
 		return message.replace( "$" + varName + "$", "\u00A7f" + value + "\u00A7f" );
 	}
 	
 	protected static String joinMessages( String... messages )
 	{
 		String totalMessage = "\u00A7f";
 		for ( String message : messages )
 			totalMessage += message + "\u00A7f";
 				
 		return totalMessage;
 	}
 	
 	protected static String formatColors( String message )
 	{
 		return message.replace( "&", "\u00A7" );
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
 				catch ( Exception e )
 				{
 					printError( e );
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
 							Player _player = mainInterface.getPlayer( player );
 							Title title = mainInterface.getIndependentTitle( args[1] );
 							if ( title != null )
 								_player.useTitle( title );
 							else
 								sendMessage( sender, formatParameters( Messages.noTitle, "title_name", args[1] ) );
 						}
 						catch ( Exception e )
 						{
 							printError( e );
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
 						Player _player = mainInterface.getPlayer( player );
 						_player.resetTitle();
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
 								
 								Player _player = mainInterface.getPlayer( player );
 								_player.useTitle( selectedTitle );
 							}
 							else
 								player.sendMessage( "Number is out of range." );
 						}
 						catch ( Exception e )
 						{
 							printError( e );
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
 								if ( mainInterface.registerTitle( args[1], prefix, postfix ) == false )
 									sendMessage( sender, formatParameters( Messages.titleExists, "title_name", args[1] ) );
 								else
 									sendMessage( sender, formatParameters( Messages.titleRegistered, "title_name", args[1] ) );
 							}
 							catch ( Title.InvalidNameException e)
 							{
 								sendMessage( sender, formatParameters( Messages.invalidTitleName, "name", args[1] ) );
 							}
 							catch ( Exception e )
 							{
 								printError( e );
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
 							if ( mainInterface.unregisterTitle( args[1] ) == false )
 								sendMessage( sender, formatParameters( Messages.titleNotExists, "title_name", args[1] ) );
 							else
 								sendMessage( sender, formatParameters( Messages.titleUnregistered, "title_name", args[1] ) );
 						}
 						catch ( Exception e )
 						{
 							printError( e );
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
 							Player _player = mainInterface.getPlayer( args[1] );
 							if ( _player == null )
 								sendMessage( sender, formatParameters( Messages.playerNotExists, "player_name", args[1] ) );
 							else
 							{
 								Title title = mainInterface.getTitle( args[2] );
 								if ( title == null )
 									sendMessage( sender, formatParameters( Messages.titleNotExists, "title_name", args[2] ) );
 								else
 								{
 									if ( _player.giveTitle( title ) == false )
 										sendMessage( sender, formatParameters( formatParameters( Messages.playerOwnsTitle, "title_name", args[2] ), "player_name", args[1] ) );
 								}
 							}
 								
 							sender.sendMessage( formatParameters( formatParameters( Messages.titleGiven, "title_name", args[2] ), "player_name", args[1] ) );
 						}
 						catch ( Exception e )
 						{
 							printError( e );
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
 							Player _player = mainInterface.getPlayer( args[1] );
 							if ( _player == null )
 								sendMessage( sender, formatParameters( Messages.playerNotExists, "player_name", args[1] ) );
 							else
 							{
 								Title title = mainInterface.getTitle( args[2] );
 								if ( title == null )
 									sendMessage( sender, formatParameters( Messages.titleNotExists, "title_name", args[2] ) );
 								else
 								{
 									if ( _player.takeTitle( title ) == false )
 										sendMessage( sender, formatParameters( formatParameters( Messages.playerNotOwnsTitle, "title_name", args[2] ), "player_name", args[1] ) );
 								}
 							}
 						}
 						catch ( Exception e )
 						{
 							printError( e );
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
 			
 			// Close databases:
 			playerDatabase.close();
 			titleDatabase.close();
 			collectionDatabase.close();
 			titleVariationDatabase.close();
 			
 			print( "Gracefully disabled." );
 		}
 		catch ( Exception e ) {
 			
 			printError( e );
 		}
 	}
 	
 	public void onEnable() {
 		
 		try {
 
 			// Load config.yml
 			Configuration config = new Configuration( new File( this.getDataFolder() + "/config.yml" ) );
 			config.load();
 			
 			String defaultPrefix = config.getString( "default_prefix", "" );
 			String defaultSuffix = config.getString( "default_suffix", "" );
 			Settings.defaultAffixes = new Title.Affixes( defaultPrefix, defaultSuffix );
 
			Messages.commandPlayerOnly = formatColors( config.getString( "message_command_player_only", null ) );
 			Messages.invalidTitleName = formatColors( config.getString( "message_invalid_title_name", null ) );
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
 			Messages.titleUse = formatColors( config.getString( "message_title_use", null ) );
 			
 			// Open databases:
 			playerDatabase.open();
 			titleDatabase.open();
 			collectionDatabase.open();
 			titleVariationDatabase.open();
 			
 			// Register events:
 			PluginManager pluginManager = this.getServer().getPluginManager();
 			pluginManager.registerEvent( Type.PLAYER_JOIN, playerListener, Priority.Normal, this );
 			pluginManager.registerEvent( Type.PLAYER_QUIT, playerListener, Priority.Normal, this );
 			pluginManager.registerEvent( Type.PLAYER_CHAT, playerListener, Priority.Highest, this );
 			
 			print( "Enabled." );
 		}
 		catch ( Exception e ) {
 			
 			printError( e );
 		}
 	}
 	
 	public void onLoad() {
 		
 		try
 		{
 			Plugin.pluginName = this.getDescription().getName();
 			
 			// Make sure that folders exist:
 			if ( !getDataFolder().exists() )
 				getDataFolder().mkdir();
 			File dataFolder = new File( getDataFolder() + "/data" );
 			if ( !dataFolder.exists() )
 				dataFolder.mkdir();
 			
 			// Install config.yml
 			File configDestination = new File( getDataFolder() + "/config.yml" );
 			if ( !configDestination.exists() )
 			{
 				InputStream configResource = getClass().getResourceAsStream( "config.yml" );
 				copyFile( configResource, new FileOutputStream( configDestination ) );
 			}
 			
 			// Install player database:
 			Header header = new Header();
 			header.addString( (short)16 ); // Player name
 			header.addInt64(); // Title in use
 			playerDatabase = new com.dannycrafts.myTitles.database.Database( new File( this.getDataFolder() + "/data/players" ), (short)0, header );
 
 			// Install title database:
 			header = new Header();
 			header.addString( (short)32 ); // Plugin interface identifier
 			header.addString( (short)16 ); // Title name
 			header.addString( (short)32 ); // Prefix
 			header.addString( (short)32 ); // Suffix
 			titleDatabase = new com.dannycrafts.myTitles.database.Database( new File( this.getDataFolder() + "/data/titles" ), (short)0, header );
 			
 			// Install collection database:
 			header = new Header();
 			header.addInt64(); // Player index
 			header.addInt64(); // Title index
 			header.addInt64(); // Title variation index
 			collectionDatabase = new com.dannycrafts.myTitles.database.Database( new File( this.getDataFolder() + "/data/collections" ), (short)0, header );
 			
 			// Install title variation database:
 			header = new Header();
 			header.addInt64(); // Title index
 			header.addString( (short)16 ); // Variation name
 			header.addString( (short)32 ); // Prefix
 			header.addString( (short)32 ); // Suffix
 			titleVariationDatabase = new com.dannycrafts.myTitles.database.Database( new File( this.getDataFolder() + "/data/title_variations" ), (short)0, header );
 		}
 		catch ( com.dannycrafts.myTitles.database.Database.DifferentVersionException  e )
 		{
 			print( "Database version 0 is required, but " + e.actualVersion + " is used." );
 		}
 		catch ( Exception e )
 		{
 			printError( e );
 		}
 		
 		print( "v" + this.getDescription().getVersion() );
 	}
 	
 	public static void print( String message ) {
 		
 		System.out.println( "[" + pluginName + "] " + message );
 	}
 	
 	public static void printError( Exception e ) {
 		
 		print( "Unexpected error occurred." );
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
