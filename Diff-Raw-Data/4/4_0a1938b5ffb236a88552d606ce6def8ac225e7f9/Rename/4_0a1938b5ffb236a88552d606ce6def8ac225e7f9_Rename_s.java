 package com.insofar.actor.commands.author;
 
import java.util.ArrayList;

 import com.insofar.actor.ActorAPI;
 import com.insofar.actor.EntityActor;
import com.insofar.actor.Recording;
 import com.insofar.actor.permissions.PermissionHandler;
 import com.insofar.actor.permissions.PermissionNode;
 
 /**
  * ActorPlugin command to rename an actor
  * 
  * @author Joshua Weinberg
  *
  */
 public class Rename extends AuthorBaseCommand {
 
 	public Rename()
 	{
 		super();
 	}
 	
 	/*************************************************************************
 	 * 
 	 * BUKKIT COMMAND
 	 * 
 	*************************************************************************/
 
 	@Override
 	/**
 	 * rename an actor
 	 */
 	public boolean execute()
 	{
 		if (!PermissionHandler.has(player, PermissionNode.COMMAND_RENAME))
 		{
 			player.sendMessage("Lack permission: "
 					+ PermissionNode.COMMAND_RENAME.getNode());
 			return true;
 		}
 		if (args.length != 3)
 		{
 			player.sendMessage("usage: /actor rename currentName newName");
 			return true;
 		}
 		
 		String actorName = args[1];
 		String newName = args[2];
 		
 		for (EntityActor actor : plugin.actors)
 		{
 			if (actor.getOwner() == player && (actor.getActorName().equals(actorName)))
 			{
 				ActorAPI.actorRemove(actor);
 				actor.setActorName(newName);
 				actor.spawn();
 			}
 		}
 		
 		/*
 		// To rename we must remove and re-spawn 
 		
 		
 		int count = 0;
 		// Need cloned copy since doFire will remove from plugin.actors
 		ArrayList<EntityActor> originalActorList  = (ArrayList<EntityActor>)plugin.actors.clone();
 		
 		for (EntityActor actor : originalActorList)
 		{
 			if (actor.getOwner() == player && (actor.getActorName().equals(actorName)))
 			{
 				Recording recording = actor.getRecording();
 				Fire.doFire(actor, player, true);
 				ActorAPI.actor(recording, newName, player.getWorld());
 				count++;
 			}
 		}
 		
 		if (count>0)
 		{
 			player.sendMessage("Renamed "+count+" actor" + (count == 1 ? "" : "s") + ".");
 		}
 		*/
 		
 		return true;
 	}
 }
