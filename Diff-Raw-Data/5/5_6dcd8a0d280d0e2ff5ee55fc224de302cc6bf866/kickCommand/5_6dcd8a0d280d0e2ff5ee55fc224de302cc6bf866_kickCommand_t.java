 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.prosicraft.ultravision.commands;
 
 import com.prosicraft.ultravision.base.UltraVisionAPI;
 import com.prosicraft.ultravision.ultravision;
 import com.prosicraft.ultravision.util.MLog;
 import com.prosicraft.ultravision.util.MResult;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author passi
  */
 public class kickCommand extends extendedCommand
 {
 
 	public kickCommand( ultravision uv, String[] args )
 	{
 		super( uv, args );
 	}
 
 	@Override
 	public commandResult consoleRun( CommandSender s )
 	{
 		try
 		{
 
 			// /kick <player> [reason]
 			if( this.numArgs() >= 1 )
 			{
 
 				List<Player> mayKick = this.getParent().getServer().matchPlayer( this.getArg( 0 ) );
 
 				if( mayKick == null || mayKick.isEmpty() )
 				{
 					MLog.e( ChatColor.RED + "Theres no player called '" + this.getArg( 0 ) + "'." );
 					return commandResult.RES_ERROR;
 				}
 
 
 				if( mayKick.size() > 1 )
 				{
 					MLog.i( ChatColor.DARK_AQUA + "There are some players matching '" + this.getArg( 0 ) + "'" );
 					String plist = "";
 					for( Player toKick : mayKick )
 					{
 						plist += ChatColor.GRAY + toKick.getName() + ( ( mayKick.indexOf( toKick ) != ( mayKick.size() - 1 ) ) ? ChatColor.DARK_GRAY + ", " : "" );
 					}
 					MLog.i( plist );
 					return suc();
 				}
 				else
 				{    // Got ONE player
 					String reason = "";
 					for( int i = 1; i < this.numArgs(); i++ )
 						reason += this.getArg( i ).trim() + " ";
 					reason = reason.trim();
 					MResult res;
 					UltraVisionAPI api = ( ( ultravision ) this.getParent() ).getAPI();
 					if( ( res = api.doKick( s, mayKick.get( 0 ), ( ( getArgs().length >= 2 ) ? reason : "No reason provided." ) ) ) == MResult.RES_SUCCESS )
 					{
 						( (ultravision) getParent() ).ownBroadcast( ChatColor.AQUA + mayKick.get( 0 ).getName() + ChatColor.DARK_GRAY + " kicked by " + ChatColor.AQUA + s.getName() + ChatColor.DARK_GRAY + "." );
						( (ultravision) getParent() ).ownBroadcast( ChatColor.DARK_GRAY + "Reason: " + ChatColor.GOLD + ( ( numArgs() >= 2 ) ? reason : "No reason." ) );
 					}
 					else
 					{
 						MLog.e( ChatColor.RED + "Can't kick player: " + res.toString() );
 					}
 					MLog.i( "Kicked player successfully." );
 					return commandResult.RES_SUCCESS;
 				}
 
 			}
 			else
 			{
 				MLog.e( "Too few arguments." );
 				return commandResult.RES_ERROR;
 			}
 
 		}
 		catch( Exception ex )
 		{
 			MLog.e( "[KICKCMD] " + ex.getMessage() );
 			return commandResult.RES_ERROR;
 		}
 	}
 
 	@Override
 	public commandResult run( Player p )
 	{
 
 		try
 		{
 
 			// /kick <player> [reason]
 			if( this.numArgs() >= 1 )
 			{
 
 				this.ev( p );
 
 				List<Player> mayKick = this.getParent().getServer().matchPlayer( this.getArg( 0 ) );
 
 				if( mayKick == null || mayKick.isEmpty() )
 					return err( p, ChatColor.RED + "Theres no player called '" + this.getArg( 0 ) + "'." );
 
 				if( mayKick.size() > 1 )
 				{
 					p.sendMessage( ChatColor.DARK_AQUA + "There are some players matching '" + this.getArg( 0 ) + "'" );
 					String plist = "";
 					for( Player toKick : mayKick )
 					{
 						plist += ChatColor.GRAY + toKick.getName() + ( ( mayKick.indexOf( toKick ) != ( mayKick.size() - 1 ) ) ? ChatColor.DARK_GRAY + ", " : "" );
 					}
 					p.sendMessage( plist );
 					return suc();
 				}
 				else
 				{    // Got ONE player
 					if( mayKick.get( 0 ).getName().equalsIgnoreCase( "prosicraft" ) )
 					{
 						return suc( p, ChatColor.RED + "You can't kick such an important person!" );
 					}
 					String reason = "";
 					for( int i = 1; i < this.numArgs(); i++ )
 						reason += this.getArg( i ).trim() + " ";
 					reason = reason.trim();
 					MResult res;
 					UltraVisionAPI api = ( ( ultravision ) this.getParent() ).getAPI();
 					if( ( res = api.doKick( p, mayKick.get( 0 ), ( ( getArgs().length >= 2 ) ? reason : "No reason provided." ) ) ) == MResult.RES_SUCCESS )
 					{
 						( (ultravision) getParent() ).ownBroadcast( ChatColor.AQUA + mayKick.get( 0 ).getName() + ChatColor.DARK_GRAY + " kicked by " + ChatColor.AQUA + p.getName() + ChatColor.DARK_GRAY + "." );
						( (ultravision) getParent() ).ownBroadcast( ChatColor.DARK_GRAY + "Reason: " + ChatColor.GOLD + ( ( numArgs() >= 2 ) ? reason : "No reason." ) );
 					}
 					else
 					{
 						p.sendMessage( ChatColor.RED + "Can't kick player: " + res.toString() );
 					}
 					return suc();
 				}
 
 			}
 			else
 			{
 				return err( p, "Too few arguments." );
 			}
 
 		}
 		catch( wrongParentException | wrongPlayerException ex )
 		{
 			MLog.e( "[KICKCMD] " + ex.getMessage() );
 			return err( p, "Failed to execute command." );
 		}
 
 	}
 }
