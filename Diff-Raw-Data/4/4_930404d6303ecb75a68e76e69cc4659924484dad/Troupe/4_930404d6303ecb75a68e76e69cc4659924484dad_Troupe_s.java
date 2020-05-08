 package com.insofar.actor.commands.author;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.Author;
 import com.insofar.actor.EntityActor;
 import com.insofar.actor.Recording;
 import com.insofar.actor.conversations.TroupeAddPrompt;
 import com.insofar.actor.permissions.PermissionHandler;
 import com.insofar.actor.permissions.PermissionNode;
 
 /**
  * ActorPlugin command to work with troupes
  * 
  * @author Joshua Weinberg
  * 
  */
 public class Troupe extends AuthorBaseCommand
 {
 
 	public Troupe()
 	{
 		super();
 	}
 
 	/*********************************************************************
 	 * 
 	 * BUKKIT COMMAND
 	 * 
 	 *********************************************************************/
 
 	@Override
 	/**
 	 * Troupe base command. Handles subcommands and help.
 	 */
 	public boolean execute()
 	{
 		if (!PermissionHandler.has(player, PermissionNode.COMMAND_TROUPE))
 		{
 			player.sendMessage("Lack permission: "
 					+ PermissionNode.COMMAND_TROUPE.getNode());
 			return true;
 		}
 		
 		if (args.length == 1)
 		{
			player.sendMessage("/troupe requires subcommand: show|add|remove|record|hire|fire");
 			return true;
 		}
 		
 		String subCommand = args[1];
 		
 		if (subCommand.equals("add"))
 		{
 			doAdd();
 		}
 		else if (subCommand.equals("show"))
 		{
 			doShow();
 		}
 		else if (subCommand.equals("remove"))
 		{
 			doRemove();
 		}
 		else if (subCommand.equals("record"))
 		{
 			doRecord();
 		}
 		else if (subCommand.equals("hire"))
 		{
 			doHire();
 		}
 		
 		return true;
 	}
 	
 	/*****************************************************************************************
 	 * 
 	 * SubCommand: Show
 	 * 
 	 *****************************************************************************************/
 	
 	/**
 	 * Handle subCommand show
 	 */
 	public void doShow()
 	{
 		if (ActorAPI.getAuthor(player).getTroupeMembers().size() == 0)
 		{
 			player.sendMessage("No players in troupe.");
 			return;
 		}
 
 		player.sendMessage("Players in troupe:");
 		for (Player member : ActorAPI.getAuthor(player).getTroupeMembers())
 		{
 			player.sendMessage(ChatColor.AQUA + " " + member.getDisplayName());
 		}
 	}
 	
 	/*****************************************************************************************
 	 * 
 	 * SubCommand: Add
 	 * 
 	 *****************************************************************************************/
 	
 	/**
 	 * Handle subCommand add
 	 */
 	public void doAdd()
 	{
 		if (args.length!=3)
 		{
 			player.sendMessage("/troupe add [playername]\n  adds a player to your troupe");
 			return;
 		}
 		
 		Player targetPlayer = Bukkit.getPlayer(args[2]);
 		
 		if (targetPlayer == null)
 		{
 			player.sendMessage("Cannot find player "+args[2]);
 			return;
 		}
 		
 		if (ActorAPI.getAuthor(player).getTroupeMembers().contains(targetPlayer))
 		{
 			player.sendMessage(ChatColor.AQUA + args[2] + ChatColor.WHITE +
 					" is already in your troupe.");
 			return;
 		}
 		
 		player.sendMessage("Requesting permission from " +
 				ChatColor.AQUA + args[2] + ChatColor.WHITE);
 		
 		final Map<Object, Object> map = new HashMap<Object, Object>();
 		map.put("requestor", player);
 		map.put("target", targetPlayer);
 		factory.withFirstPrompt(new TroupeAddPrompt(this))
 				.withInitialSessionData(map).withLocalEcho(false).buildConversation(targetPlayer)
 				.begin();
 		return;
 	}
 	
 	/**
 	 * Once the player confirms they are in the troupe, add them to the troupe set.
 	 * @param target
 	 * @return
 	 */
 	public void addRequestAccepted(Player target)
 	{
 		player.sendMessage(ChatColor.AQUA + target.getDisplayName() +
 				" added to troupe.");
 		ActorAPI.getAuthor(player).getTroupeMembers().add(target);
 	}
 
 	/**
 	 * If player denies the request...
 	 * @param target
 	 * @return
 	 */
 	public void addRequestDenied(Player target)
 	{
 		player.sendMessage("Troupe add request denied by " +
 				ChatColor.AQUA + target.getDisplayName());
 	}
 
 	/*****************************************************************************************
 	 * 
 	 * SubCommand: Remove
 	 * 
 	 *****************************************************************************************/
 	
 	/**
 	 * Handle subCommand remove
 	 */
 	public void doRemove()
 	{
 		if (args.length!=3)
 		{
 			player.sendMessage("/troupe remove [playername]\n  removes a player from your troupe");
 			return;
 		}
 		
 		Player targetPlayer = Bukkit.getPlayer(args[2]);
 		
 		if (ActorAPI.getAuthor(player).getTroupeMembers().contains(targetPlayer))
 		{
 			player.sendMessage(ChatColor.AQUA + args[2] + ChatColor.WHITE +
 					" removed from troupe.");
 			ActorAPI.getAuthor(player).getTroupeMembers().remove(targetPlayer);
 			return;
 		}
 		else
 		{
 			player.sendMessage("Player "+args[2]+" not in troupe");
 		}
 	}
 	
 	/*****************************************************************************************
 	 * 
 	 * SubCommand: Record
 	 * 
 	 *****************************************************************************************/
 	
 	/**
 	 * Handle subCommand record
 	 */
 	public void doRecord()
 	{
 		Author author = ActorAPI.getAuthor(player);
 		if (author.getTroupeMembers().size() < 1)
 		{
 			player.sendMessage("No troupe members to record.");
 			return;
 		}
 		
 		author.setTroupeRecording(true);
 		HashMap<String,Recording> recMap = author.getTroupRecMap();
 		
 		for (Player member : author.getTroupeMembers())
 		{
 			// If no recording exists make one
 			Recording r = recMap.get(member.getName());
 			if (r == null)
 			{
 				r = new Recording();
 				recMap.put(member.getName(), r);
 			}
 			
 			ActorAPI.record(member, author.getTroupRecMap().get(member.getName()));
 		}
 		
 		player.sendMessage("Started recording troupe");
 	}
 	
 	/*****************************************************************************************
 	 * 
 	 * SubCommand: Hire
 	 * 
 	 *****************************************************************************************/
 	
 	/**
 	 * Handle subCommand hire
 	 */
 	public void doHire()
 	{
 		Author author = ActorAPI.getAuthor(player);
 		HashMap<String,Recording> recMap = author.getTroupRecMap();
 		for (Player member : author.getTroupeMembers())
 		{
 			Recording r = recMap.get(member.getName());
 			if (r == null)
 			{
 				continue;
 			}
 			
 			EntityActor newActor = ActorAPI.actor(r, member.getName(), player.getWorld());
 			
 			if (newActor != null)
 			{
 				newActor.setOwner(player);
 				plugin.actors.add(newActor);
 			}
 		}
 	}
 }
